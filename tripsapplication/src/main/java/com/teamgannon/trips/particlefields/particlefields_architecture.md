# Particle Fields Architecture

## Overview

The `particlefields` package provides a flexible, reusable system for rendering particle-based astronomical phenomena including:

- **Planetary rings** (Saturn-like, Uranus-like)
- **Asteroid belts** (Main belt, Kuiper belt)
- **Debris disks** (Protoplanetary, collision remnants)
- **Dust clouds / Nebulae** (Emission, dark, reflection, planetary)
- **Accretion disks** (Black hole, neutron star)

The architecture separates concerns into distinct layers: configuration, generation, animation, and rendering. This allows the same particle system to be used in multiple contexts (standalone windows, solar system view, interstellar view) with appropriate scaling.

---

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           PERSISTENCE LAYER                                  │
│  ┌─────────────┐                                                            │
│  │   Nebula    │  JPA Entity (NEW) - stores procedural generation params    │
│  │  (Entity)   │  for interstellar nebulae in the database                  │
│  └──────┬──────┘                                                            │
│         │                                                                    │
│         │ converted by NebulaConfigConverter                                │
└─────────┼────────────────────────────────────────────────────────────────────┘
          ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                          CONFIGURATION LAYER                                 │
│                                                                              │
│  ┌──────────────────┐     ┌─────────────────┐     ┌────────────────┐        │
│  │ RingConfiguration │◄────│ RingFieldFactory │     │    RingType    │        │
│  │     (Record)      │     │   (Presets)      │     │     (Enum)     │        │
│  └────────┬──────────┘     └──────────────────┘     └────────────────┘        │
│           │                                                                   │
│           │ Adapters scale for different contexts:                           │
│           │                                                                   │
│  ┌────────┴─────────┐                                                        │
│  │ RingScaleAdapter │  Interface for unit conversion                         │
│  └────────┬─────────┘                                                        │
│           │                                                                   │
│     ┌─────┴─────────────────────────┐                                        │
│     │                               │                                        │
│  ┌──▼──────────────────┐    ┌───────▼────────────────┐                       │
│  │InterstellarRingAdapter│    │ SolarSystemRingAdapter │                      │
│  │  (light-years)       │    │   (AU → screen units)   │                      │
│  └──────────────────────┘    └──────────────────────────┘                     │
└─────────────────────────────────────────────────────────────────────────────┘
          │
          │ used by
          ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                           GENERATION LAYER                                   │
│                                                                              │
│  ┌────────────────────────┐                                                  │
│  │ RingElementGenerator   │  Interface for particle generation               │
│  │     (Interface)        │                                                  │
│  └───────────┬────────────┘                                                  │
│              │                                                               │
│     ┌────────┼────────┬────────────┬─────────────┬──────────────┐            │
│     ▼        ▼        ▼            ▼             ▼              ▼            │
│  ┌──────┐ ┌──────┐ ┌──────┐   ┌────────┐   ┌──────────┐   ┌──────────┐      │
│  │Planet│ │Astero│ │Debris│   │  Dust  │   │Accretion │   │  Nebula  │      │
│  │ Ring │ │ Belt │ │ Disk │   │ Cloud  │   │   Disk   │   │Generator │      │
│  │ Gen  │ │ Gen  │ │ Gen  │   │  Gen   │   │   Gen    │   │  (NEW)   │      │
│  └──────┘ └──────┘ └──────┘   └────────┘   └──────────┘   └──────────┘      │
│                                    │                                         │
│                                    │ produces                                │
│                                    ▼                                         │
│                            ┌──────────────┐                                  │
│                            │ RingElement  │  Individual particle data        │
│                            │   (Class)    │  (orbital params, size, color)   │
│                            └──────────────┘                                  │
└─────────────────────────────────────────────────────────────────────────────┘
          │
          │ List<RingElement>
          ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                           RENDERING LAYER                                    │
│                                                                              │
│  ┌─────────────────────┐                                                     │
│  │  RingFieldRenderer  │  Converts elements to JavaFX 3D scene graph        │
│  │      (Class)        │                                                     │
│  └──────────┬──────────┘                                                     │
│             │                                                                │
│             │ partitions by size, creates                                    │
│             ▼                                                                │
│  ┌──────────────────────────────────────────────────────────────┐            │
│  │                    ScatterMesh (FXyz3D)                       │            │
│  │  ┌────────────┐    ┌─────────────┐    ┌────────────┐         │            │
│  │  │ meshSmall  │    │ meshMedium  │    │ meshLarge  │         │            │
│  │  │ (primary   │    │ (blended    │    │ (secondary │         │            │
│  │  │  color)    │    │  color)     │    │  color)    │         │            │
│  │  └────────────┘    └─────────────┘    └────────────┘         │            │
│  └──────────────────────────────────────────────────────────────┘            │
│             │                                                                │
│             │ added to                                                       │
│             ▼                                                                │
│       ┌───────────┐                                                          │
│       │   Group   │  JavaFX 3D container                                     │
│       │ (JavaFX)  │  → Added to InterstellarSpacePane.world                  │
│       └───────────┘     or SolarSystemSpacePane.systemEntityGroup            │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Component Details

### RingType (Enum)

Categorizes particle field types with distinct physical characteristics:

| Type | Description | Distribution | Motion |
|------|-------------|--------------|--------|
| `PLANETARY_RING` | Saturn-like rings | Very thin, flat disk | Fast Keplerian |
| `ASTEROID_BELT` | Rocky/icy bodies | Thick vertical spread | Moderate Keplerian |
| `DEBRIS_DISK` | Collision/protoplanetary | Moderate thickness | Slow Keplerian |
| `DUST_CLOUD` | Nebulae, gas clouds | 3D spherical/ellipsoidal | Slow turbulent |
| `ACCRETION_DISK` | Around compact objects | Very thin, dense | Very fast |

### RingConfiguration (Record)

Immutable configuration parameters for particle generation:

```java
public record RingConfiguration(
    RingType type,              // Determines which generator to use
    double innerRadius,         // Inner extent (visual units)
    double outerRadius,         // Outer extent (visual units)
    int numElements,            // Particle count
    double minSize,             // Minimum particle size
    double maxSize,             // Maximum particle size
    double thickness,           // Vertical spread
    double maxInclinationDeg,   // Orbital tilt range (90° = full sphere)
    double maxEccentricity,     // Orbital eccentricity range
    double baseAngularSpeed,    // Rotation rate multiplier
    double centralBodyRadius,   // Central object size
    Color primaryColor,         // Main particle color
    Color secondaryColor,       // Accent/gradient color
    String name                 // Display name
)
```

**Builder pattern** provided for fluent construction with defaults.

### RingElement (Class)

Represents a single particle with:

- **Immutable orbital parameters**: semi-major axis, eccentricity, inclination, argument of periapsis, longitude of ascending node, angular speed
- **Mutable state**: current orbital angle, cached x/y/z position
- **Visual properties**: size, height offset, color

The `advance(timeScale)` method updates position using Keplerian orbital mechanics.

### RingElementGenerator (Interface)

Contract for particle generation:

```java
public interface RingElementGenerator {
    List<RingElement> generate(RingConfiguration config, Random random);
    RingType getRingType();
    String getDescription();
}
```

### Generator Implementations

| Generator | Strategy |
|-----------|----------|
| `PlanetaryRingGenerator` | Thin disk, circular orbits, high density |
| `AsteroidBeltGenerator` | Thick distribution, eccentric/inclined orbits |
| `DebrisDiskGenerator` | Moderate spread, some gaps/structure |
| `DustCloudGenerator` | 3D spherical, Gaussian radial falloff, turbulent motion |
| `AccretionDiskGenerator` | Thin, fast, temperature-based color gradient |

### RingFieldRenderer (Class)

Converts `List<RingElement>` to renderable JavaFX scene graph:

1. **Partitions elements by size** into small/medium/large categories
2. **Creates ScatterMesh** (FXyz3D) for each category with appropriate colors
3. **Disables backface culling** for visibility from all angles
4. **Provides API** for positioning, scaling, visibility, animation

**Key methods**:
- `initialize(config, random)` - Generate elements and build meshes
- `update(timeScale)` - Advance all particle positions (call every frame)
- `refreshMeshes()` - Rebuild ScatterMesh objects (call periodically, not every frame)
- `getGroup()` - Returns JavaFX Group to add to scene

### RingFieldFactory (Class)

Central factory providing:
- **Generator registry**: Maps RingType → Generator
- **Preset configurations**: Pre-built configs for common scenarios
- **Convenience methods**: `generateElements(config)`, `getPreset(name)`

### Scale Adapters

Adapters implement `RingScaleAdapter` interface for context-specific scaling:

**InterstellarRingAdapter** (light-years → screen units):
- Used for nebulae in the interstellar view
- Provides factory methods: `createEmissionNebula()`, `createDarkNebula()`, `createReflectionNebula()`, `createPlanetaryNebula()`
- Handles zoom level adjustments

**SolarSystemRingAdapter** (AU → screen units):
- Used for planetary rings, asteroid/Kuiper belts in solar system view
- Scales particle sizes for appropriate visibility

---

## Nebula Persistence Extension (NEW)

### Motivation

The existing particle field system is **runtime-only** - configurations exist transiently in memory. For the interstellar view, we need to **persist nebulae in the database** so they can be:
- Saved with datasets (each dataset = different universe)
- Loaded when plotting stars
- Edited by users
- Imported from astronomical catalogs (Messier, NGC)

### Nebula JPA Entity

```java
@Entity
@Table(name = "NEBULA")
public class Nebula {
    @Id
    private String id;                    // UUID

    private String name;                  // "Orion Nebula"
    private String dataSetName;           // Dataset association

    // Position (light years from Sol)
    private double centerX;
    private double centerY;
    private double centerZ;

    // Shape
    private double innerRadius;           // 0 for solid, >0 for shells
    private double outerRadius;           // Extent in light-years

    // Type & Generation
    @Enumerated(EnumType.STRING)
    private NebulaType type;              // EMISSION, DARK, REFLECTION, etc.
    private long seed;                    // For reproducible generation

    // Density & Structure
    private double particleDensity;       // Particles per cubic light-year
    private Integer numElementsOverride;  // User override (null = calculate)
    private double radialPower;           // Density falloff (0.3=core, 0.7=shell)
    private double noiseStrength;         // Filament intensity (0.0-1.0)
    private int noiseOctaves;             // Detail level (2-4)

    // Appearance
    private String primaryColor;          // Hex color
    private String secondaryColor;        // Hex color
    private double opacity;               // Base opacity

    // Animation
    private boolean enableAnimation;      // Default true

    // Import tracking
    private String sourceCatalog;         // "Messier", "NGC", "User-defined"
    private String catalogId;             // "M42", "NGC 1976"
}
```

### NebulaType Enum

```java
public enum NebulaType {
    EMISSION("Emission Nebula"),          // H-II regions, star-forming
    DARK("Dark Nebula"),                  // Obscuring dust clouds
    REFLECTION("Reflection Nebula"),      // Blue-shifted starlight
    PLANETARY("Planetary Nebula"),        // Dying star shells
    SUPERNOVA_REMNANT("Supernova Remnant") // Explosion debris
}
```

### Conversion Flow

```
┌──────────────┐     ┌─────────────────────┐     ┌───────────────────┐
│    Nebula    │────►│NebulaConfigConverter│────►│ RingConfiguration │
│  (JPA Entity)│     │                     │     │                   │
└──────────────┘     └─────────────────────┘     └─────────┬─────────┘
                                                           │
                                                           ▼
                     ┌─────────────────────┐     ┌───────────────────┐
                     │   DustCloudGenerator │◄────│  RingFieldFactory │
                     │   (Enhanced)         │     │                   │
                     └──────────┬──────────┘     └───────────────────┘
                                │
                                ▼
                     ┌─────────────────────┐
                     │  List<RingElement>  │
                     └──────────┬──────────┘
                                │
                                ▼
                     ┌─────────────────────┐
                     │  RingFieldRenderer  │
                     └──────────┬──────────┘
                                │
                                ▼
                     ┌─────────────────────┐
                     │   JavaFX Group      │───► InterstellarSpacePane.world
                     └─────────────────────┘
```

### NebulaConfigConverter

```java
public class NebulaConfigConverter {

    public static RingConfiguration toRingConfiguration(
            Nebula nebula,
            InterstellarRingAdapter adapter,
            double cameraDistance) {

        // Calculate particle count
        int numElements = calculateNumElements(nebula, cameraDistance);

        // Convert light-year coordinates to screen units
        double screenInner = adapter.toVisualUnits(nebula.getInnerRadius());
        double screenOuter = adapter.toVisualUnits(nebula.getOuterRadius());

        return RingConfiguration.builder()
            .type(RingType.DUST_CLOUD)
            .innerRadius(screenInner)
            .outerRadius(screenOuter)
            .numElements(numElements)
            // ... map all other fields
            .build();
    }

    private static int calculateNumElements(Nebula nebula, double cameraDistance) {
        // 1. Base from density * volume
        double volume = (4.0/3.0) * Math.PI *
            (Math.pow(nebula.getOuterRadius(), 3) -
             Math.pow(nebula.getInnerRadius(), 3));
        int baseCount = (int)(nebula.getParticleDensity() * volume);

        // 2. User override
        if (nebula.getNumElementsOverride() != null) {
            baseCount = nebula.getNumElementsOverride();
        }

        // 3. LOD scaling based on camera distance
        double lodFactor = calculateLodFactor(cameraDistance);
        int finalCount = (int)(baseCount * lodFactor);

        // Clamp to reasonable bounds
        return Math.max(1000, Math.min(500_000, finalCount));
    }

    private static double calculateLodFactor(double distanceLy) {
        if (distanceLy < 10) return 1.0;
        if (distanceLy < 50) return 0.5;
        if (distanceLy < 100) return 0.2;
        return 0.1;
    }
}
```

---

## Enhanced DustCloudGenerator

The current `DustCloudGenerator` uses simple Gaussian radial distribution. For more realistic nebulae, enhance with:

### 1. Configurable Radial Power (Density Falloff)

```java
// Current: Gaussian (uniform-ish blob)
double r = config.innerRadius() + Math.abs(random.nextGaussian() * 0.4) * radialRange;

// Enhanced: Power-law (configurable concentration)
double u = random.nextDouble();
double r = config.innerRadius() + radialRange * Math.pow(u, config.radialPower());
// radialPower < 0.5 → dense core, gradual falloff
// radialPower > 0.5 → shell-like, hollow center
```

### 2. Noise Displacement (Filamentary Structure)

```java
// Simple hash-based 3D noise
private double noise3D(double x, double y, double z, long seed) {
    long h = seed + (long)(x * 73856093) ^ (long)(y * 19349663) ^ (long)(z * 83492791);
    h = (h ^ (h >> 13)) * 0x27d4eb2d;
    return ((h & 0xFFFFFFFFL) / (double) 0xFFFFFFFFL) * 2 - 1;
}

// Apply displacement after computing cartesian position
double px = r * Math.sin(phi) * Math.cos(theta);
double py = r * Math.sin(phi) * Math.sin(theta);
double pz = r * Math.cos(phi);

double n = 0, amp = 1.0, freq = 0.8;
for (int oct = 0; oct < config.noiseOctaves(); oct++) {
    n += noise3D(px * freq, py * freq, pz * freq, config.seed()) * amp;
    amp *= 0.5;
    freq *= 2.2;
}

double displace = radialRange * 0.12 * config.noiseStrength();
px += n * displace;
py += n * displace * 0.7;  // Anisotropic for filaments
pz += n * displace * 0.4;
```

### 3. Core-Biased Color/Opacity

```java
// Brighter/hotter near core
double coreFactor = 1.0 - (r - config.innerRadius()) / radialRange;
Color base = interpolateColor(config.primaryColor(), config.secondaryColor(), colorFactor);

// Boost saturation/brightness near core
base = base.deriveColor(0, 1.0 + coreFactor * 0.4, 1.0 + coreFactor * 0.6, 1.0);

// Opacity gradient (fade at edges)
double opacity = 0.4 + (0.6 * coreFactor) + random.nextDouble() * 0.3;
```

---

## Integration with Interstellar View

### NebulaManager (NEW)

Orchestrates nebula rendering in `InterstellarSpacePane`:

```java
@Component
public class NebulaManager {

    private final NebulaRepository nebulaRepository;
    private final InterstellarRingAdapter adapter;
    private final Map<String, RingFieldRenderer> activeRenderers = new HashMap<>();

    public void renderNebulaeInPlotRange(
            Group worldGroup,
            double centerX, double centerY, double centerZ,
            double plotRadiusLy,
            String datasetName) {

        // Query nebulae that intersect the plot sphere
        List<Nebula> nebulae = nebulaRepository.findInPlotRange(
            datasetName, centerX, centerY, centerZ, plotRadiusLy);

        // Clear previous renderers
        clearRenderers(worldGroup);

        for (Nebula nebula : nebulae) {
            // Calculate camera distance for LOD
            double distance = calculateDistance(centerX, centerY, centerZ, nebula);

            // Convert to RingConfiguration
            RingConfiguration config = NebulaConfigConverter.toRingConfiguration(
                nebula, adapter, distance);

            // Create renderer
            RingFieldRenderer renderer = new RingFieldRenderer(config, new Random(nebula.getSeed()));

            // Position in world coordinates
            renderer.setPosition(
                adapter.toVisualUnits(nebula.getCenterX()),
                adapter.toVisualUnits(nebula.getCenterY()),
                adapter.toVisualUnits(nebula.getCenterZ())
            );

            // Add to scene
            worldGroup.getChildren().add(renderer.getGroup());
            activeRenderers.put(nebula.getId(), renderer);
        }
    }

    public void updateAnimation(double timeScale) {
        for (RingFieldRenderer renderer : activeRenderers.values()) {
            renderer.update(timeScale);
        }
    }

    public void refreshMeshes() {
        for (RingFieldRenderer renderer : activeRenderers.values()) {
            renderer.refreshMeshes();
        }
    }
}
```

### PlotManager Integration

```java
// In PlotManager.drawStars() or similar
public void plotStarsAndNebulae(CurrentPlot plot) {
    // Existing star plotting
    starPlotManager.drawStars(plot, showGridStems);

    // NEW: Plot nebulae in range
    nebulaManager.renderNebulaeInPlotRange(
        interstellarSpacePane.getWorld(),
        plot.getCenterX(),
        plot.getCenterY(),
        plot.getCenterZ(),
        plot.getPlotRadius(),
        plot.getDatasetName()
    );
}
```

---

## Catalog Import Support

### Data Sources

| Catalog | Objects | Format | Source |
|---------|---------|--------|--------|
| Messier | 110 | CSV/JSON | OpenNGC, Kaggle |
| NGC/IC | ~13,000 | CSV | OpenNGC GitHub |
| Custom | User-defined | UI/Import | Application |

### NebulaCatalogImporter

```java
@Service
public class NebulaCatalogImporter {

    public List<Nebula> importMessierCatalog(InputStream csvStream, String datasetName) {
        List<Nebula> nebulae = new ArrayList<>();

        // Parse CSV with columns: id, name, ra, dec, distance, type, size_arcmin
        try (CSVReader reader = new CSVReader(new InputStreamReader(csvStream))) {
            for (String[] row : reader) {
                if (!isNebula(row)) continue;  // Skip galaxies, clusters

                Nebula nebula = new Nebula();
                nebula.setId(UUID.randomUUID().toString());
                nebula.setName(row[1]);  // e.g., "Orion Nebula"
                nebula.setCatalogId(row[0]);  // e.g., "M42"
                nebula.setSourceCatalog("Messier");
                nebula.setDataSetName(datasetName);

                // Convert RA/Dec/Distance to X/Y/Z
                double[] xyz = convertEquatorialToCartesian(
                    parseRA(row[2]), parseDec(row[3]), parseDistance(row[4]));
                nebula.setCenterX(xyz[0]);
                nebula.setCenterY(xyz[1]);
                nebula.setCenterZ(xyz[2]);

                // Convert angular size to linear size
                nebula.setOuterRadius(angularToLinearSize(row[5], row[4]));

                // Set type-specific defaults
                configureByType(nebula, row[6]);

                nebulae.add(nebula);
            }
        }

        return nebulae;
    }
}
```

---

## Performance Considerations

### Particle Count Guidelines

| Nebula Size | Distance | Suggested Count |
|-------------|----------|-----------------|
| Small (<5 ly) | Close (<20 ly) | 5,000 - 10,000 |
| Medium (5-20 ly) | Medium (20-50 ly) | 3,000 - 8,000 |
| Large (>20 ly) | Far (>50 ly) | 1,000 - 5,000 |

### LOD Strategy

```java
private double calculateLodFactor(double distanceLy) {
    if (distanceLy < 10) return 1.0;      // Full detail
    if (distanceLy < 50) return 0.5;      // Half particles
    if (distanceLy < 100) return 0.2;     // 20% particles
    return 0.1;                            // Minimal (far background)
}
```

### Animation Optimization

- Call `update(timeScale)` every frame for smooth motion
- Call `refreshMeshes()` every 5-10 frames (mesh rebuild is expensive)
- Disable animation for distant nebulae (`enableAnimation = false` when LOD < 0.3)

---

## Future Enhancements

1. **Volumetric Rendering**: Ray-marching shaders for true volumetric nebulae
2. **Glow Post-Processing**: Bloom filter for ethereal nebula appearance
3. **Star-Nebula Interaction**: Young stars embedded in nebulae, illumination effects
4. **Dynamic Evolution**: Nebulae that expand/contract over simulated time
5. **Multi-Component Nebulae**: Layered structures (core + halo + filaments)

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2024-01 | Initial particle field system (rings, belts, disks) |
| 1.1 | 2024-01 | Added InterstellarRingAdapter, nebula factory methods |
| 2.0 | 2025-01 | Nebula JPA persistence, enhanced generators, catalog import |
