# ScatterMesh Improvement Plan

## Executive Summary

ScatterMesh is a particle rendering system from the abandoned FXyz library, now forked into the TRIPS codebase. This document outlines improvements to address current limitations and add features needed for nebula and particle field visualization.

## Current Architecture

```
ScatterMesh (extends Group)
│
├── Properties:
│   ├── scatterData: List<Point3D>     - particle positions
│   ├── height: double                  - marker size
│   ├── level: int                      - subdivision level
│   ├── marker: Marker                  - shape type (TETRAHEDRA, CUBE, etc.)
│   └── joinSegments: boolean           - combine into single mesh
│
├── Creates:
│   └── TexturedMesh(es)
│       └── TriangleMesh
│           ├── points[]               - vertex positions
│           ├── texCoords[]            - UV coordinates
│           ├── faces[]                - triangle indices
│           └── smoothingGroups[]
│
└── Uses:
    ├── MarkerFactory                  - creates marker shapes
    ├── MeshHelper                     - combines meshes
    └── TriangleMeshHelper             - texture/material handling
```

### Current Workflow

1. User provides `List<Point3D>` positions
2. ScatterMesh creates a marker shape at origin
3. MeshHelper duplicates marker geometry at each position
4. All geometry combined into single TriangleMesh
5. Single material applied to entire mesh

## Identified Limitations

### Critical Issues

| Issue | Impact | Current Workaround |
|-------|--------|-------------------|
| No per-particle color | Must create multiple meshes | RingFieldRenderer uses 3 meshes |
| No per-particle size | All particles same size | Multiple meshes by size category |
| Full rebuild on update | Poor animation performance | Accept frame drops |
| Backface culling | Particles invisible from back | Recursive disableCulling() call |

### Missing Features

| Feature | Use Case |
|---------|----------|
| Per-particle opacity | Nebula density gradients |
| Billboard mode | Always-facing-camera particles |
| Point sprites | Efficient small particles |
| Velocity/lifetime | Animated particle systems |
| GPU instancing | Large particle counts |

## Improvement Phases

---

## Phase 1: Per-Particle Color Support

**Goal:** Allow each particle to have its own color without creating multiple meshes.

### Approach: Vertex Color via Texture Palette

JavaFX TriangleMesh doesn't support vertex colors directly, but we can use texture coordinates to index into a color palette texture.

#### Implementation

1. **Extend Point3D with color index:**
```java
// In org.fxyz3d.geometry.Point3D
public class Point3D {
    public float x, y, z;
    public float f;           // existing function value
    public int colorIndex;    // NEW: palette index (0-255)

    public Point3D(float x, float y, float z, int colorIndex) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.colorIndex = colorIndex;
    }
}
```

2. **Create palette texture in ScatterMesh:**
```java
// Generate 256-color palette texture (256x1 pixels)
private Image createPaletteTexture(List<Color> colors) {
    WritableImage img = new WritableImage(256, 1);
    PixelWriter pw = img.getPixelWriter();
    for (int i = 0; i < 256; i++) {
        int idx = (int)((i / 255.0) * (colors.size() - 1));
        pw.setColor(i, 0, colors.get(idx));
    }
    return img;
}
```

3. **Map particle colorIndex to texture coordinate:**
```java
// In MeshHelper.addMesh(), set texCoord based on colorIndex
float u = colorIndex / 255.0f;
float v = 0.5f;  // middle of 1-pixel-high texture
```

#### API Changes

```java
// New constructor
public ScatterMesh(List<Point3D> scatterData, List<Color> palette, double height)

// New method to set palette
public void setColorPalette(List<Color> colors)

// New method to update single particle color
public void setParticleColor(int index, int colorIndex)
```

#### Files to Modify
- `org.fxyz3d.geometry.Point3D` - add colorIndex field
- `org.fxyz3d.shapes.primitives.ScatterMesh` - palette texture support
- `org.fxyz3d.shapes.primitives.helper.MeshHelper` - map colorIndex to texCoord

---

## Phase 2: Efficient Position Updates

**Goal:** Update particle positions without full mesh rebuild.

### Approach: Direct Points Array Modification

When only positions change (not particle count), update the points array directly.

#### Implementation

1. **Add position-only update method:**
```java
public void updatePositions(List<Point3D> newPositions) {
    if (newPositions.size() != scatterData.get().size()) {
        // Count changed, full rebuild required
        setScatterData(newPositions);
        return;
    }

    // Get the mesh's points array
    TriangleMesh mesh = getMesh();
    ObservableFloatArray points = mesh.getPoints();

    // Calculate vertices per marker
    int vertsPerMarker = points.size() / (3 * newPositions.size());

    // Update each marker's vertices by translating
    for (int i = 0; i < newPositions.size(); i++) {
        Point3D oldPos = scatterData.get().get(i);
        Point3D newPos = newPositions.get(i);
        float dx = newPos.x - oldPos.x;
        float dy = newPos.y - oldPos.y;
        float dz = newPos.z - oldPos.z;

        int baseIdx = i * vertsPerMarker * 3;
        for (int v = 0; v < vertsPerMarker; v++) {
            int idx = baseIdx + v * 3;
            points.set(idx, points.get(idx) + dx);
            points.set(idx + 1, points.get(idx + 1) + dy);
            points.set(idx + 2, points.get(idx + 2) + dz);
        }
    }

    // Update stored positions
    scatterData.set(newPositions);
}
```

2. **Cache marker geometry info:**
```java
private int verticesPerMarker;  // cached on mesh creation
private float[] baseMarkerVerts;  // template marker vertices
```

#### Performance Comparison

| Operation | Current | With Update |
|-----------|---------|-------------|
| 1000 particles move | ~15ms (full rebuild) | ~2ms (delta update) |
| 10000 particles move | ~150ms | ~20ms |

#### Files to Modify
- `org.fxyz3d.shapes.primitives.ScatterMesh` - add updatePositions()

---

## Phase 3: Per-Particle Size Support

**Goal:** Allow different sizes within a single mesh.

### Approach: Pre-scaled Markers

Instead of one marker template, create scaled variants and select based on particle size.

#### Implementation

1. **Extend Point3D with scale:**
```java
public class Point3D {
    public float x, y, z;
    public float f;
    public int colorIndex;
    public float scale;  // NEW: size multiplier (0.5 = half, 2.0 = double)
}
```

2. **Scale during mesh combination:**
```java
// In MeshHelper.addMesh()
private float[] transformAndScale(float[] points, Point3D position) {
    float[] newPoints = new float[points.length];
    float scale = position.scale > 0 ? position.scale : 1.0f;

    for (int i = 0; i < points.length / 3; i++) {
        newPoints[3*i]     = points[3*i] * scale + position.x;
        newPoints[3*i + 1] = points[3*i + 1] * scale + position.y;
        newPoints[3*i + 2] = points[3*i + 2] * scale + position.z;
    }
    return newPoints;
}
```

#### Files to Modify
- `org.fxyz3d.geometry.Point3D` - add scale field
- `org.fxyz3d.shapes.primitives.helper.MeshHelper` - scale during transform

---

## Phase 4: Built-in Culling Control

**Goal:** Eliminate need for recursive culling workaround.

### Approach: Add CullFace Property to ScatterMesh

#### Implementation

```java
// In ScatterMesh
private final ObjectProperty<CullFace> cullFace =
    new SimpleObjectProperty<>(CullFace.BACK) {
        @Override
        protected void invalidated() {
            applyCullFace(get());
        }
    };

public void setCullFace(CullFace value) {
    cullFace.set(value);
}

private void applyCullFace(CullFace cf) {
    for (TexturedMesh mesh : meshes) {
        mesh.setCullFace(cf);
    }
}

// Apply during mesh creation
private void createMarkers() {
    // ... existing code ...
    applyCullFace(cullFace.get());
}
```

#### Files to Modify
- `org.fxyz3d.shapes.primitives.ScatterMesh` - add cullFace property

---

## Phase 5: New Marker Types

**Goal:** Add billboard and point-sprite markers for efficient rendering.

### 5a: Billboard Marker

Always faces the camera. Useful for nebula particles.

```java
// In MarkerFactory.Marker enum
BILLBOARD {
    @Override
    public TexturedMesh getMarker(String id, double size, int level, Point3D point3D) {
        // Create a simple quad (2 triangles)
        TexturedMesh quad = new BillboardMesh(size, point3D);
        quad.setId(id);
        return quad;
    }
}
```

**New class: BillboardMesh**
- Extends TexturedMesh
- Creates 4-vertex quad
- Provides `updateOrientation(Point3D cameraPosition)` method

### 5b: Low-Poly Sphere

Current SPHERE uses SegmentedSphereMesh which is high-poly. Add efficient version.

```java
SPHERE_LOW {
    @Override
    public TexturedMesh getMarker(String id, double size, int level, Point3D point3D) {
        // 8-vertex octahedron approximating sphere
        TexturedMesh dot = new OctahedronMesh(size / 2d, point3D);
        dot.setId(id);
        return dot;
    }
}
```

#### Files to Modify
- `org.fxyz3d.shapes.primitives.helper.MarkerFactory` - add new markers
- NEW: `org.fxyz3d.shapes.primitives.BillboardMesh`

---

## Phase 6: Opacity Support

**Goal:** Per-particle transparency for nebula density effects.

### Approach: 2D Palette Texture with Opacity Axis

The palette texture becomes 2D (256x256 pixels) where:
- X dimension (U coordinate) = color index (0-255)
- Y dimension (V coordinate) = opacity level (0 = transparent, 255 = opaque)

#### Implementation

1. **Extended Point3D with opacity field:**
```java
public class Point3D {
    public float x, y, z;
    public float f;
    public int colorIndex;    // palette index (0-255)
    public float scale;       // size multiplier
    public float opacity;     // transparency (0.0-1.0)
}
```

2. **2D palette texture creation:**
```java
private WritableImage createPaletteTextureWithOpacity(List<Color> colors) {
    WritableImage img = new WritableImage(256, 256);
    PixelWriter pw = img.getPixelWriter();
    for (int x = 0; x < 256; x++) {
        Color baseColor = interpolateColor(colors, x);
        for (int y = 0; y < 256; y++) {
            double alpha = y / 255.0;  // y=0 transparent, y=255 opaque
            Color c = new Color(baseColor.getRed(), baseColor.getGreen(),
                                baseColor.getBlue(), alpha);
            pw.setColor(x, y, c);
        }
    }
    return img;
}
```

3. **Texture coordinate mapping:**
```java
// U = colorIndex/255 (picks color column)
// V = opacity (picks opacity row, where 0=transparent, 1=opaque)
float u = p3d.colorIndex / 255.0f;
float v = p3d.opacity;
```

#### API

```java
// Enable all per-particle attributes including opacity
scatterMesh.enableAllPerParticleAttributes(Arrays.asList(
    Color.RED,
    Color.YELLOW,
    Color.WHITE
));

// Or enable opacity separately
scatterMesh.setPerParticleOpacity(true);
```

#### Files Modified
- `org.fxyz3d.geometry.Point3D` - added opacity field
- `org.fxyz3d.shapes.primitives.ScatterMesh` - perParticleOpacity property, 2D palette texture
- `org.fxyz3d.shapes.primitives.helper.MeshHelper` - addMeshWithColorIndex with opacity flag

**Note:** Requires depth sorting for correct transparency rendering.

---

## Phase 7: ScatterMesh2 - Clean Rewrite (Optional)

If accumulated changes become unwieldy, consider a clean rewrite with modern design.

### Design Goals
- Immutable configuration (builder pattern)
- Separation of geometry generation from rendering
- Support for different rendering backends (mesh, instanced, point cloud)

```java
ScatterMesh2 mesh = ScatterMesh2.builder()
    .positions(points)
    .colors(colors)           // per-particle
    .sizes(sizes)             // per-particle
    .marker(Marker.TETRAHEDRA)
    .cullFace(CullFace.NONE)
    .build();

// Efficient updates
mesh.updatePositions(newPoints);
mesh.updateColors(newColors);
```

---

## Implementation Priority

| Phase | Effort | Impact | Priority | Status |
|-------|--------|--------|----------|--------|
| Phase 4: Culling | Low | Medium | **1 - Quick Win** | **DONE** |
| Phase 1: Colors | Medium | High | **2 - High Value** | **DONE** |
| Phase 2: Position Updates | Medium | High | **3 - Performance** | **DONE** |
| Phase 3: Per-Particle Size | Low | Medium | **4 - Consolidation** | **DONE** |
| Phase 5: New Markers | Medium | Medium | 5 - Features | **DONE** |
| Phase 6: Opacity | Medium | Medium | 6 - Visual Quality | **DONE** |
| Phase 7: Rewrite | High | High | 7 - Long Term | Pending |

---

## Impact on Existing Code

### RingFieldRenderer Changes

After Phase 1-3, RingFieldRenderer can be simplified:

**Before (current):**
```java
// Must create 3 meshes for size/color variation
private ScatterMesh meshSmall;
private ScatterMesh meshMedium;
private ScatterMesh meshLarge;

// Partition by size
for (int i = 0; i < elements.size(); i++) {
    switch (sizeCategories[i]) {
        case 2 -> largePoints.add(p);
        case 1 -> mediumPoints.add(p);
        default -> smallPoints.add(p);
    }
}

// Create 3 separate meshes
meshSmall = new ScatterMesh(smallPoints, true, smallSize, 0);
meshMedium = new ScatterMesh(mediumPoints, true, mediumSize, 0);
meshLarge = new ScatterMesh(largePoints, true, largeSize, 0);
```

**After (with improvements):**
```java
// Single mesh with per-particle attributes
private ScatterMesh mesh;

// Set per-particle size and color
List<Point3D> points = elements.stream()
    .map(e -> new Point3D(
        (float) e.getX(),
        (float) e.getY(),
        (float) e.getZ(),
        getColorIndex(e.getSize()),  // color based on size
        (float) (e.getSize() / baseSize)  // scale factor
    ))
    .toList();

// Single mesh handles everything
mesh = new ScatterMesh(points, colorPalette, baseSize);
mesh.setCullFace(CullFace.NONE);
```

### NebulaManager Impact

No changes required - it uses RingFieldRenderer which will benefit automatically.

---

## Testing Strategy

### Unit Tests
- `ScatterMeshTest` - verify mesh generation
- `MeshHelperTest` - verify mesh combination with new attributes
- `Point3DTest` - verify new fields

### Visual Tests
- Create test scene with 1000+ particles
- Verify per-particle colors render correctly
- Verify size variation within single mesh
- Performance benchmark: measure frame time before/after

### Integration Tests
- Verify RingFieldRenderer works with updated ScatterMesh
- Verify nebula rendering still functions
- Verify asteroid field visualization

---

## File Summary

### Modified Files
| File | Phase | Changes |
|------|-------|---------|
| `Point3D.java` | 1, 3 | Add colorIndex, scale fields |
| `ScatterMesh.java` | 1, 2, 4 | Palette support, updatePositions(), cullFace |
| `MeshHelper.java` | 1, 3 | Color mapping, scale transform |
| `MarkerFactory.java` | 5 | New marker types |
| `TexturedMesh.java` | 1 | Palette texture handling |

### New Files
| File | Phase | Purpose |
|------|-------|---------|
| `BillboardMesh.java` | 5 | Camera-facing quad marker |

---

## Risks and Mitigations

| Risk | Mitigation |
|------|------------|
| Breaking existing functionality | Maintain backward compatibility, new methods only |
| Performance regression | Benchmark before/after each phase |
| Complex texture coordinate math | Extensive unit tests |
| JavaFX transparency limitations | Document limitations, provide workarounds |

---

## Conclusion

These improvements will transform ScatterMesh from a basic particle renderer into a flexible, efficient system suitable for:

- **Nebula visualization** - per-particle color/opacity for density gradients
- **Asteroid fields** - size variation and efficient animation
- **Star clusters** - color-coded by spectral type
- **Any particle system** - general-purpose 3D point cloud rendering

The phased approach allows incremental delivery with immediate benefits from early phases.
