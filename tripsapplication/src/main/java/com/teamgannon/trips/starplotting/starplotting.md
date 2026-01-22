# Star Plotting System

This document describes how TRIPS renders stars in the 3D visualization, including the spatial indexing and level-of-detail (LOD) optimizations.

## Overview

The star plotting pipeline transforms database star records into 3D visual objects. For large datasets (1,000+ stars), several optimizations ensure smooth performance:

```
Database Query
     │
     ▼
┌─────────────────┐
│   PlotManager   │  Coordinates transformation, validation
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│   CurrentPlot   │  Star storage, spatial index, distance sorting
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ StarPlotManager │  3D rendering with LOD, batching, caching
└────────┬────────┘
         │
         ▼
    JavaFX Scene
```

---

## Coordinate Systems

The system uses two coordinate spaces:

| Coordinate Type | Field | Units | Used For |
|----------------|-------|-------|----------|
| **Actual** | `actualCoordinates` | Light-years | Spatial queries, distance filtering |
| **Screen** | `coordinates` | Screen units | 3D rendering, LOD determination |

The `AstrographicTransformer` converts actual coordinates to screen coordinates based on the current view settings.

---

## KD-Tree Spatial Index

### What It Does

The KD-tree (k-dimensional tree) enables fast spatial queries on star positions. Instead of checking every star to find nearby ones, it organizes stars in a tree structure that can be searched efficiently.

### Performance

| Operation | Without KD-Tree | With KD-Tree |
|-----------|----------------|--------------|
| Find stars within radius | O(n) - check all | O(log n + k) |
| Find nearest star | O(n) | O(log n) |
| Build index | N/A | O(n log n) |

For 10,000 stars finding 100 nearby: ~10,000 checks → ~100 checks

### Implementation

Located in `VisualizationSpatialIndex.java`:

```java
// Building the index (automatic, lazy)
VisualizationSpatialIndex index = currentPlot.getSpatialIndex();

// Range query - find stars within 50 light-years
List<StarDisplayRecord> nearby = index.findStarsWithinRadius(centerX, centerY, centerZ, 50.0);

// Nearest neighbor
StarDisplayRecord closest = index.findNearestStar(x, y, z);
```

### When It's Used

- **Viewport culling**: Only render stars within view distance
- **Label selection**: Find top-scoring stars within radius for labeling
- **Distance filtering**: `getStarsWithinRadius()` queries

The index is built lazily on first access and rebuilt when stars change (dirty flag pattern).

---

## Level of Detail (LOD) System

### What It Does

LOD reduces rendering complexity by using simpler geometry for distant or dim stars. A star 500 light-years away appears as a tiny dot—it doesn't need 8,000 triangles.

### LOD Levels

| Level | Sphere Divisions | Triangles | When Used |
|-------|-----------------|-----------|-----------|
| **HIGH** | 64 (default) | ~8,192 | Center star, very bright nearby stars |
| **MEDIUM** | 32 | ~2,048 | Most visible stars |
| **LOW** | 16 | ~512 | Distant stars |
| **MINIMAL** | 8 | ~128 | Very distant or dim stars |

### Distance Thresholds

```
         0          50         200         400        ∞
         │──────────│──────────│───────────│──────────│
           HIGH      MEDIUM       LOW        MINIMAL
```

Thresholds are adjusted by zoom level and star magnitude (brighter stars get higher detail).

### Efficiency Example

For 1,000 stars with typical distribution:

| Without LOD | With LOD |
|-------------|----------|
| 8,192,000 triangles | ~700,000 triangles |
| 100% GPU load | ~9% GPU load |

**Result: ~90% reduction in rendering complexity**

### Implementation

Located in `StarLODManager.java`:

```java
// Determine LOD for a star
LODLevel level = lodManager.determineLODLevel(starRecord, isCenter);

// Create geometry with appropriate detail
Node starNode = lodManager.createStarWithLOD(record, radius, material, level);
```

### Statistics Logging

After each render, LOD statistics are logged:

```
╔══════════════════════════════════════════════════════════════╗
║                    LOD RENDERING STATISTICS                   ║
╠══════════════════════════════════════════════════════════════╣
║ Level    │ Count │ Percent │ Triangles                       ║
║ HIGH     │    20 │    2.0% │      163,840                    ║
║ MEDIUM   │   150 │   15.0% │      307,200                    ║
║ LOW      │   300 │   30.0% │      153,600                    ║
║ MINIMAL  │   530 │   53.0% │       67,840                    ║
╠══════════════════════════════════════════════════════════════╣
║ TOTAL STARS: 1,000                                           ║
║ ACTUAL TRIANGLES: 692,480                                    ║
║ WITHOUT LOD WOULD BE: 8,192,000                              ║
║ EFFICIENCY GAIN: 91.5% triangle reduction                    ║
╚══════════════════════════════════════════════════════════════╝
```

---

## How KD-Tree and LOD Work Together

They solve different problems in the rendering pipeline:

| Step | Optimization | Problem Solved |
|------|--------------|----------------|
| 1. Query | — | Get stars from database |
| 2. Filter | **KD-Tree** | Which stars are in view? |
| 3. Sort | **KD-Tree** | Order by distance (nearest first) |
| 4. Render | **LOD** | How detailed should each star be? |

```
10,000 stars in database
        │
        ▼ KD-tree filters to viewport
    1,000 stars to render
        │
        ▼ LOD determines detail level
    HIGH: 20 │ MEDIUM: 150 │ LOW: 300 │ MINIMAL: 530
        │
        ▼ Render with appropriate geometry
    692,480 triangles (not 8,192,000)
```

---

## Additional Performance Optimizations

### Material Caching

Instead of creating a new `PhongMaterial` for each star, materials are cached by color:

```java
// Before: 1,000 materials for 1,000 stars
PhongMaterial material = new PhongMaterial();

// After: ~15 materials for 1,000 stars (one per spectral class color)
PhongMaterial material = getCachedMaterial(star.getColor());
```

**Savings**: ~98% reduction in material objects

### Object Pooling for Star Nodes

The `StarNodePool` class manages reusable `Sphere` objects to reduce garbage collection pressure:

```java
// Acquiring a sphere from the pool
Sphere star = pool.acquire(LODLevel.MEDIUM, radius, material);

// Releasing spheres back to pool (on clearStars)
pool.release(sphere, LODLevel.MEDIUM);
```

**How It Works:**
- Separate pools maintained for each LOD level (spheres have fixed divisions at construction)
- Pre-warmed at startup with 100 spheres per LOD level (400 total)
- Max pool size of 5,000 per level prevents unbounded growth
- Released spheres have materials cleared to help GC

**Pool Statistics** (logged after each render):
```
╠══════════════════════════════════════════════════════════════╣
║ OBJECT POOL: ENABLED                                         ║
║ StarNodePool[created=400, reused=600, released=1000,         ║
║              pooled=400, reuseRate=60.0%]                    ║
╚══════════════════════════════════════════════════════════════╝
```

**Benefits:**
- Eliminates repeated `Sphere` allocation for recurring renders
- Reduces GC pressure during plot transitions
- Higher reuse rate on subsequent renders (first render creates, later renders reuse)

**Savings**: After initial render, nearly 100% sphere reuse

### Batch Scene Graph Updates

Instead of adding nodes one at a time (causes N scene graph restructures):

```java
// Before: O(n) scene graph updates
for (star : stars) {
    group.getChildren().add(createNode(star));
}

// After: O(1) scene graph update
List<Node> nodes = new ArrayList<>();
for (star : stars) {
    nodes.add(createNode(star));
}
group.getChildren().addAll(nodes);  // Single batch add
```

### Lazy Context Menus

Context menus are created on-demand when right-clicked, not upfront:

```java
// Before: 1,000 context menus created at render time
ContextMenu menu = createPopup(star);
node.setOnClick(e -> menu.show(...));

// After: 0 context menus until needed
node.setOnClick(e -> {
    if (rightClick) {
        ContextMenu menu = createPopup(star);  // Created on demand
        menu.show(...);
    }
});
```

### Lazy Tooltips

Tooltips are installed on first hover, not at creation:

```java
// Before: 1,000 tooltips created
Tooltip.install(node, new Tooltip(star.getName()));

// After: Tooltip created only when user hovers
node.setOnMouseEntered(e -> {
    if (!hasTooltip(node)) {
        Tooltip.install(node, new Tooltip(star.getName()));
    }
});
```

### Pre-Validation

Stars are validated before the render loop to avoid try-catch overhead per star:

```java
// Before: Try-catch for each star
for (star : stars) {
    try {
        render(star);
    } catch (Exception e) {
        log.error("Bad star");
    }
}

// After: Filter once, render clean
List<Star> validStars = filterValidStars(stars);  // Single validation pass
for (star : validStars) {
    render(star);  // No exception handling needed
}
```

### Throttled Label Updates

Label positions must be recalculated during camera rotation and zoom. Without throttling, this happens on every mouse event (potentially 100+ times per second during smooth drags):

```java
// Before: Update on every mouse event
mouseDragEventHandler = event -> {
    // ... rotation logic ...
    updateLabels();  // Called 100+ times/second during drag
};

// After: Throttle to ~60fps
private static final long LABEL_UPDATE_THROTTLE_MS = 16;  // ~60fps
private long lastLabelUpdateTime = 0;

private void throttledUpdateLabels() {
    long now = System.currentTimeMillis();
    if (now - lastLabelUpdateTime >= LABEL_UPDATE_THROTTLE_MS) {
        lastLabelUpdateTime = now;
        updateLabels();
    }
}

mouseDragEventHandler = event -> {
    // ... rotation logic ...
    throttledUpdateLabels();  // Max 60 updates/second
};
```

**Applies to:**
- `InterstellarSpacePane` - main 3D visualization
- `SolarSystemSpacePane` - solar system view

**Savings**: Reduces label update frequency from potentially 100+ fps to a consistent 60 fps during continuous interactions.

---

## Route Spatial Index

### What It Does

Similar to the star KD-tree, the route spatial index enables fast spatial queries on route segments. This is useful for:
- Finding routes visible in a given viewport
- Click detection on route segments
- Route culling for performance

### Implementation

Located in `RouteSegmentSpatialIndex.java`:

```java
// Route segments are indexed by their midpoint
public record IndexedRouteSegment(
    UUID routeId,
    int segmentIndex,
    Point3D startPoint,
    Point3D endPoint,
    Point3D midpoint,
    double boundingRadius  // Half the segment length
) { ... }

// Building the index (automatic via CurrentPlot)
RouteSegmentSpatialIndex index = currentPlot.getRouteSpatialIndex();

// Range query - find segments within 100 light-years
List<IndexedRouteSegment> segments = index.findSegmentsWithinRadius(x, y, z, 100.0);

// Get visible route IDs for a viewport
Set<UUID> visibleRoutes = index.getVisibleRouteIds(centerX, centerY, centerZ, radius);

// Nearest segment query
IndexedRouteSegment nearest = index.findNearestSegment(x, y, z);
```

### How It Works

1. Each route segment is represented by `IndexedRouteSegment` containing:
   - Route UUID and segment index within the route
   - Start/end points of the segment
   - Midpoint (used as the index key)
   - Bounding radius (half the segment length)

2. Segments are inserted into a KD-tree by midpoint coordinates

3. Queries use bounding sphere intersection:
   - Query sphere (center + radius) vs segment bounding sphere (midpoint + half-length)
   - This provides fast culling before detailed line-intersection tests

### Statistics

```
RouteSegmentSpatialIndex[routes=12, segments=156, queries=5, avgReturned=23.4, cullRate=85.0%]
```

---

## Transit Spatial Index

### What It Does

Transits are possible jump connections between stars within a distance band. Like routes, transits benefit from spatial indexing for:
- Viewport culling (only render visible transits)
- Click detection on transit lines
- Finding transits by band for selective rendering

### Implementation

Located in `TransitSpatialIndex.java`:

```java
// Transit segments indexed by midpoint
public record IndexedTransit(
    String bandId,           // Transit band (e.g., "0-5 ly", "5-10 ly")
    String sourceName,
    String targetName,
    Point3D startPoint,
    Point3D endPoint,
    Point3D midpoint,
    double boundingRadius,   // Half the transit distance
    double distance,         // Actual distance in light-years
    TransitRoute transitRoute
) { ... }

// Building the index (managed by TransitManager)
TransitSpatialIndex index = transitManager.getSpatialIndex();

// Range query - find transits within viewport
List<IndexedTransit> transits = index.findTransitsWithinRadius(x, y, z, 100.0);

// Filter by band
List<IndexedTransit> band1Transits = index.findTransitsForBandWithinRadius(
    "band1", x, y, z, 100.0);

// Get visible band IDs
Set<String> visibleBands = index.getVisibleBandIds(centerX, centerY, centerZ, radius);

// Nearest transit (for click detection)
IndexedTransit nearest = index.findNearestTransit(x, y, z);
```

### Key Features

| Feature | Description |
|---------|-------------|
| **Band Organization** | Transits grouped by distance band |
| **Lazy Rebuild** | KD-tree rebuilt only when transits change |
| **Bidirectional Keys** | A-B equals B-A for deduplication |
| **Statistics Tracking** | Query count, checked count, cull rate |

### Statistics

```
TransitSpatialIndex[bands=3, transits=2456, queries=12, avgReturned=156.2, cullRate=93.6%]
```

### Integration with TransitManager

The `TransitManager` automatically maintains the spatial index:

```java
// After computing transits
transitManager.findTransits(transitDefinitions, starsInView);
// Index is automatically rebuilt

// Query methods delegate to the index
List<IndexedTransit> visible = transitManager.findTransitsWithinRadius(x, y, z, radius);
IndexedTransit nearest = transitManager.findNearestTransit(x, y, z);
Set<String> bands = transitManager.getVisibleBandIds(x, y, z, radius);
```

---

## Performance Summary

| Optimization | What It Reduces | Typical Savings |
|--------------|-----------------|-----------------|
| KD-Tree spatial index | Distance calculations | 90-99% |
| LOD system | Triangle count | 85-95% |
| Object pooling | Sphere allocations | 60-100%* |
| Material caching | Material objects | 95-99% |
| Batch scene graph | Graph restructures | 99% |
| Lazy context menus | Upfront allocations | 100% |
| Lazy tooltips | Upfront allocations | 90-99% |
| Pre-validation | Exception overhead | Per-star overhead |
| Throttled label updates | Label recalculations | ~40% (100fps → 60fps) |
| Route spatial index | Route intersection checks | 80-95% |
| Transit spatial index | Transit intersection checks | 85-95% |

\* Object pooling savings increase after initial render; first render creates spheres, subsequent renders reuse them.

---

## Key Classes

| Class | Responsibility |
|-------|---------------|
| `PlotManager` | Orchestrates plotting, transforms coordinates |
| `CurrentPlot` | Stores stars, manages spatial index, distance sorting, route spatial index |
| `StarPlotManager` | Renders stars, manages LOD, batching, caching |
| `VisualizationSpatialIndex` | KD-tree spatial queries for stars |
| `StarLODManager` | Determines detail level, creates geometry, manages pool |
| `StarNodePool` | Object pool for reusing Sphere nodes |
| `StarLabelManager` | Label creation and positioning |
| `StarExtensionManager` | Extension lines from grid to stars |
| `InterstellarSpacePane` | Main 3D visualization, throttled label updates |
| `SolarSystemSpacePane` | Solar system visualization, throttled label updates |
| `RouteSegmentSpatialIndex` | KD-tree spatial queries for route segments |
| `IndexedRouteSegment` | Route segment data for spatial indexing |
| `TransitSpatialIndex` | KD-tree spatial queries for transits |
| `IndexedTransit` | Transit segment data for spatial indexing |
| `TransitManager` | Manages transit visualization, spatial index integration |

---

## Timing Metrics

Methods annotated with `@TrackExecutionTime` log their execution time:

```
Metrics:: Class Name: PlotManager. Method Name: plotStars. execution time is: 245ms
Metrics:: Class Name: PlotManager. Method Name: filterValidStars. execution time is: 12ms
Metrics:: Class Name: CurrentPlot. Method Name: getStarsSortedByDistance. execution time is: 18ms
Metrics:: Class Name: StarPlotManager. Method Name: drawStars. execution time is: 180ms
```

Use these logs to identify performance bottlenecks.
