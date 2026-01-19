# Procedural Planet Generator

Java port of [VictorGordan/4x_planet_generator_godot](https://github.com/VictorGordan/4x_planet_generator_godot) for TRIPS.

Generates procedural planets with tectonic plate simulation, producing spherical meshes of hexagons and pentagons (Goldberg polyhedron) with realistic terrain features.

## Package

```
com.teamgannon.trips.planetarymodelling.procedural
```

## Features

- **Goldberg Polyhedron Mesh**: Subdivided icosahedron producing 12 pentagons + hexagons
- **Tectonic Plate Simulation**: Flood-fill plate assignment with configurable plate count (7-21)
- **Plate Boundary Interactions**: Convergent, divergent, and transform boundaries
- **Terrain Generation**: Heights from -4 (deep ocean) to +4 (high mountains)
- **Climate Zones**: Latitude-based tropical/temperate/polar classification
- **Reproducible**: Seed-based generation for consistent results
- **JavaFX Integration**: Native JavaFX 3D rendering via `PlanetMeshView`

## Dependencies

```xml
<dependency>
    <groupId>org.hipparchus</groupId>
    <artifactId>hipparchus-geometry</artifactId>
    <version>3.0</version>
</dependency>
```

JavaFX assumed provided by TRIPS parent pom.

## Quick Start

```java
import com.teamgannon.trips.planetarymodelling.procedural.*;

// Generate planet
PlanetConfig config = PlanetConfig.builder()
    .seed(12345L)
    .size(PlanetConfig.Size.STANDARD)  // 4412 polygons
    .plateCount(14)
    .waterFraction(0.66)
    .build();

PlanetGenerator.Planet planet = PlanetGenerator.generate(config);

// Render in JavaFX
PlanetMeshView meshView = new PlanetMeshView(planet);
scene.getRoot().getChildren().add(meshView);
```

## Integration with Accrete

```java
// From Accrete planetary data
PlanetConfig config = PlanetConfig.builder()
    .seed(accretePlanet.getSeed())
    .fromAccreteRadius(accretePlanet.getRadiusKm())  // Auto-selects mesh resolution
    .plateCount(calculatePlateCount(accretePlanet.getMassEarths()))
    .waterFraction(accretePlanet.getHydrosphereFraction())
    .build();

PlanetGenerator.Planet planet = PlanetGenerator.generate(config);
```

## Classes

| Class | Purpose |
|-------|---------|
| `PlanetConfig` | Immutable configuration record with builder |
| `PlanetGenerator` | Facade orchestrating the generation pipeline |
| `PlanetGenerator.Planet` | Result record containing all generated data |
| `Polygon` | Single hex/pentagon with center + vertices |
| `IcosahedralMesh` | Goldberg polyhedron geometry generation |
| `AdjacencyGraph` | Polygon neighbor relationships |
| `PlateAssigner` | Tectonic plate flood-fill assignment |
| `BoundaryDetector` | Plate types and boundary interactions |
| `ElevationCalculator` | Terrain height from plate tectonics |
| `ClimateCalculator` | Latitude-based climate zones |
| `PlanetMeshView` | JavaFX 3D renderer (extends `Group`) |
| `PlanetRenderer` | Jzy3d renderer (optional) |
| `TectonicService` | Spring-compatible service with caching |

## Size Presets

| Size | n | Polygons | Use Case |
|------|---|----------|----------|
| DUEL | 11 | ~1212 | Testing |
| TINY | 15 | ~2252 | Small moons |
| SMALL | 19 | ~3612 | Small planets |
| STANDARD | 21 | ~4412 | Earth-like |
| LARGE | 24 | ~5762 | Super-Earths |
| HUGE | 26 | ~6762 | Large planets |
| COLOSSAL | 32 | ~10242 | Gas giant moons |

## Height Values

| Value | Terrain |
|-------|---------|
| -4 | Deep ocean |
| -3 | Ocean |
| -2 | Shallow ocean |
| -1 | Coastal waters |
| 0 | Lowlands |
| 1 | Plains |
| 2 | Hills |
| 3 | Mountains |
| 4 | High mountains |

## Climate Zones

| Zone | Latitude Range |
|------|----------------|
| TROPICAL | 0° - 30° |
| TEMPERATE | 30° - 60° |
| POLAR | 60° - 90° |

## Accessing Generated Data

```java
Planet planet = PlanetGenerator.generate(config);

// Geometry
List<Polygon> polygons = planet.polygons();
for (Polygon p : polygons) {
    Vector3D center = p.center();
    List<Vector3D> vertices = p.vertices();
    boolean isPentagon = p.isPentagon();
}

// Terrain heights (-4 to +4)
int[] heights = planet.heights();

// Climate zones
ClimateCalculator.ClimateZone[] climates = planet.climates();

// Plate data
PlateAssigner.PlateAssignment plates = planet.plateAssignment();
int[] polyToPlate = plates.plateIndex();  // polygon -> plate mapping
List<List<Integer>> platePolygons = plates.plates();  // plate -> polygon list

// Plate boundaries
BoundaryDetector.BoundaryAnalysis boundaries = planet.boundaryAnalysis();
BoundaryDetector.PlateType[] plateTypes = boundaries.plateTypes();  // OCEANIC or CONTINENTAL
```

## Reproducibility

Same seed + config = identical planet:

```java
var config = PlanetConfig.builder().seed(42L).build();
Planet p1 = PlanetGenerator.generate(config);
Planet p2 = PlanetGenerator.generate(config);
// p1 and p2 are identical
```

Sub-seeds derived for each generation phase:
- Phase 1: Plate assignment
- Phase 2: Elevation calculation

## Service Layer

For Spring Boot integration:

```java
@Service
public class MyTectonicService extends TectonicService {}

// Usage
@Autowired TectonicService tectonicService;

Planet planet = tectonicService.generateFromAccrete(
    seed, radiusKm, massEarths, waterFraction
);

// Caching built-in
Planet cached = tectonicService.getCached(seed);
tectonicService.evict(seed);
tectonicService.clearCache();
```

## JavaFX Rendering

```java
// Full terrain view
PlanetMeshView meshView = new PlanetMeshView(planet);
meshView.setScaleX(100);
meshView.setScaleY(100);
meshView.setScaleZ(100);

// Wireframe for debugging
Group wireframe = PlanetMeshView.createWireframe(planet.polygons(), 1.0);

// Add to SubScene with PerspectiveCamera
SubScene subScene = new SubScene(meshView, 800, 600, true, SceneAntialiasing.BALANCED);
subScene.setCamera(new PerspectiveCamera(true));
```

## Generation Pipeline

```
PlanetConfig
    ↓
IcosahedralMesh.generate()     → List<Polygon>
    ↓
AdjacencyGraph                  → neighbor relationships
    ↓
PlateAssigner.assign()          → polygon-to-plate mapping
    ↓
BoundaryDetector.analyze()      → plate types + boundary types
    ↓
ElevationCalculator.calculate() → int[] heights
    ↓
ClimateCalculator.calculate()   → ClimateZone[] climates
    ↓
Planet (result record)
```

## Origin

Ported from GDScript (Godot 3.x) to Java 17.

**Original:** [VictorGordan/4x_planet_generator_godot](https://github.com/VictorGordan/4x_planet_generator_godot) (2020)

**Local source:** [`../4x_planet_generator_godot-main/`](../4x_planet_generator_godot-main/) — see [`CLAUDE.md`](../../../../../../../../../../procedural/4x_planet_generator_godot-main/CLAUDE.md) for original architecture notes.

## Port Mapping

| GDScript Source | Java Class | Notes |
|-----------------|------------|-------|
| [`UniversalConstants/universal_constants.gd`](../../../../../../../../../../procedural/4x_planet_generator_godot-main/UniversalConstants/universal_constants.gd) | `PlanetConfig` | Constants → immutable record with builder |
| [`GoldbergPolyhedronGenerator/GridIcosahedron/Icosahedron/icosahedron.gd`](../../../../../../../../../../procedural/4x_planet_generator_godot-main/GoldbergPolyhedronGenerator/GridIcosahedron/Icosahedron/icosahedron.gd) | `IcosahedralMesh` | 12-vertex icosahedron with tilt |
| [`GoldbergPolyhedronGenerator/GridIcosahedron/HexGrid/hex_grid.gd`](../../../../../../../../../../procedural/4x_planet_generator_godot-main/GoldbergPolyhedronGenerator/GridIcosahedron/HexGrid/hex_grid.gd) | `IcosahedralMesh` | Hex grid on triangular faces |
| [`GoldbergPolyhedronGenerator/GridIcosahedron/grid_icosahedron.gd`](../../../../../../../../../../procedural/4x_planet_generator_godot-main/GoldbergPolyhedronGenerator/GridIcosahedron/grid_icosahedron.gd) | `IcosahedralMesh` | 20 faces, pentagons, edge hexagons |
| [`GoldbergPolyhedronGenerator/goldberg_polyhedron.gd`](../../../../../../../../../../procedural/4x_planet_generator_godot-main/GoldbergPolyhedronGenerator/goldberg_polyhedron.gd) | `IcosahedralMesh` | Sphere projection, center adjustment |
| [`AdjacentFunctions/adjacent.gd`](../../../../../../../../../../procedural/4x_planet_generator_godot-main/AdjacentFunctions/adjacent.gd) | `AdjacencyGraph` | Neighbor lookup by distance |
| [`TectonicPlates/tectonic_plates.gd`](../../../../../../../../../../procedural/4x_planet_generator_godot-main/TectonicPlates/tectonic_plates.gd) | `PlateAssigner` | Flood-fill plate assignment |
| [`MapTypes/continents.gd`](../../../../../../../../../../procedural/4x_planet_generator_godot-main/MapTypes/continents.gd) | `BoundaryDetector` | Plate types, boundary interactions |
| [`MapTypes/continents.gd`](../../../../../../../../../../procedural/4x_planet_generator_godot-main/MapTypes/continents.gd) | `ElevationCalculator` | Height generation, mountains, islands |
| [`Hydrosphere/hydrosphere.gd`](../../../../../../../../../../procedural/4x_planet_generator_godot-main/Hydrosphere/hydrosphere.gd) | `ClimateCalculator` | Latitude-based climate zones |
| [`HelperFunctions/DrawFunctions/draw.gd`](../../../../../../../../../../procedural/4x_planet_generator_godot-main/HelperFunctions/DrawFunctions/draw.gd) | `PlanetMeshView` | Godot mesh → JavaFX TriangleMesh |
| [`HelperFunctions/helper_functions.gd`](../../../../../../../../../../procedural/4x_planet_generator_godot-main/HelperFunctions/helper_functions.gd) | (inlined) | Utility functions absorbed into classes |
| [`test/test.gd`](../../../../../../../../../../procedural/4x_planet_generator_godot-main/test/test.gd) | `PlanetGenerator` | Entry point, orchestration |
| [`Camera/CameraGimbal.gd`](../../../../../../../../../../procedural/4x_planet_generator_godot-main/Camera/CameraGimbal.gd) | — | Skipped (Godot-specific, TRIPS has own camera) |

**Coverage: ~95%**

## Additions Beyond Original

| Java Class | Purpose |
|------------|---------|
| `PlanetConfig.Builder` | Fluent builder pattern for configuration |
| `TectonicService` | Spring-compatible service with caching |
| `PlanetRenderer` | Jzy3d renderer (optional alternative) |
| `Polygon` record | Type-safe polygon representation |
| `PlanetGenerator.Planet` | Result record with all generated data |
| Sub-seed derivation | `config.subSeed(phase)` for reproducibility |
| `fromAccreteRadius()` | Auto-select mesh resolution from Accrete data |

## Key Adaptations

| GDScript | Java |
|----------|------|
| `Vector3` | Hipparchus `Vector3D` |
| `PoolVector3Array` | `List<Vector3D>` |
| `Array` | `List<>`, `int[]`, records |
| Autoload singletons (`constant`, `adj`, `draw`) | Constructor injection |
| `randf()`, `randi_range()` | `Random` with seed |
| Godot `ArrayMesh` | JavaFX `TriangleMesh` |
| `.size` / `.adj` cache files | Generated on-the-fly |

---

## Critical Review (January 2026)

### Overall Rating: ⭐⭐⭐⭐ (Good, needs polish)

**Strengths:**
- Clean pipeline architecture with excellent facade pattern
- Reproducible generation with proper seed isolation
- Good Accrete integration
- Feature-rich erosion with rivers and climate

**Weaknesses:**
- ElevationCalculator is overly complex (575 lines, nested logic)
- Magic numbers throughout without documentation
- JavaFxPlanetMeshConverter has heavy code duplication
- Test coverage gaps for edge cases

---

## TODO: Work To Be Done

### ✅ HIGH PRIORITY (COMPLETED)

#### 1. ~~Refactor ElevationCalculator~~ ✅ DONE (January 2026)
**File:** `ElevationCalculator.java`

**Completed:**
- [x] Split `applyBoundaryEffect()` into per-boundary-type methods:
  - `applyConvergentBoundary()` - handles oceanic-oceanic, oceanic-continental, continental-continental collisions
  - `applyDivergentBoundary()` - handles rift valleys and spreading centers
  - `applyTransformBoundary()` - handles strike-slip faults and pull-apart basins
- [x] Created `BoundaryEffectConfig` record in `PlanetConfig` with all boundary parameters
- [x] Added JavaDoc explaining terrain effect philosophy with physical basis
- [x] Added `applyEffectLayers()` helper to apply multi-layer terrain effects

**Key Changes:**
- Boundary effect parameters now configurable via `PlanetConfig.boundaryEffects()`
- Each boundary type has documented physical basis (e.g., "Island arcs form as one plate subducts")
- Methods are <30 lines each with clear single responsibility

---

#### 2. ~~Consolidate JavaFxPlanetMeshConverter~~ ✅ DONE (January 2026)
**File:** `JavaFxPlanetMeshConverter.java`

**Completed:**
- [x] Created `MeshConversionOptions` record with factory methods:
  - `defaults()` - basic conversion
  - `smooth(adjacency)` - averaged heights with normals
  - `byHeight(adjacency)` - per-height colored meshes
  - `withPreciseHeights(heights, adjacency)` - finest gradations
- [x] Added `convertUnified()` and `convertUnifiedByHeight()` entry points
- [x] Replaced string-based vertex quantization with hash code:
  ```java
  // Now using bit-packed long key for O(1) lookup
  long key = ((long)(qx + 1048576) << 42) | ((long)(qy + 1048576) << 21) | (qz + 1048576);
  ```
- [x] Made debug output conditional via `DEBUG_LOGGING` flag
- [x] Created `addFanTriangles()` helper for common triangulation logic

---

#### 3. ~~Document Magic Numbers~~ ✅ DONE (January 2026)
**Files:** Multiple

**Completed:**
- [x] `ErosionCalculator`: Documented `RAINFALL_THRESHOLD`, `RIVER_SOURCE_THRESHOLD`, `RIVER_SOURCE_ELEVATION_MIN`, `PARALLEL_THRESHOLD` with physical basis
- [x] `ElevationCalculator`: Documented `mountainLength` and `islandLength` scaling rationale
- [x] `BoundaryDetector`: Documented `numDivergentOcean < 2` and `numSubductionOcean < 3` Earth-based limits
- [x] `PlanetConfig`: Documented `oceanicPlateRatio = 0.65` (Earth's 60-70% oceanic crust) and `hotspotProbability = 0.12` (1-2 hotspots per planet)
- [x] Added inline comments for erosion cap (0.3) and sediment deposition (50%)
- [x] Added river carving depth documentation (0.3 max at source)

---

### 🟡 MEDIUM PRIORITY

#### 4. Fix Floating-Point Comparisons (Estimated: 2-3 days)
**Files:** `IcosahedralMesh.java`, `ClimateCalculator.java`

**Problems:**
- Line 282: `.equals()` on `Vector3D` without epsilon tolerance
- Line 46: `norm == 0.0` check inappropriate for floating-point

**Tasks:**
- [ ] Add `EPSILON` constant (e.g., `1e-10`)
- [ ] Replace `v1.equals(v2)` with `v1.distance(v2) < EPSILON`
- [ ] Replace `norm == 0.0` with `Math.abs(norm) < EPSILON`
- [ ] Add unit tests for edge cases near epsilon boundary

---

#### 5. Add Configuration for Erosion Thresholds (Estimated: 1 week)
**File:** `ErosionCalculator.java`

**Currently hardcoded:**
```java
private static final double RAINFALL_THRESHOLD = 0.3;
private static final double RIVER_SOURCE_THRESHOLD = 0.7;
private static final double RIVER_SOURCE_ELEVATION_MIN = 0.5;
```

**Tasks:**
- [ ] Add to `PlanetConfig`:
  ```java
  double rainfallThreshold,        // min rainfall for erosion
  double riverSourceThreshold,     // min rainfall for river source
  double riverSourceElevationMin,  // min elevation for river source
  double erosionCap,               // max erosion per iteration (currently 0.3)
  double depositionFactor,         // sediment deposition rate (currently 0.5)
  ```
- [ ] Update `ErosionCalculator` to use config values
- [ ] Add validation in `PlanetConfig.Builder`

---

#### 6. Expand Test Coverage (Estimated: 1-2 weeks)
**Directory:** `src/test/java/.../procedural/`

**Missing Tests:**
- [ ] **Integration tests for Accrete bridge:**
  - `PlanetGenerator.generateFromAccrete()` with various planet types
  - `PlanetGenerator.createBiasedConfig()` validation

- [ ] **Edge case tests:**
  - `waterFraction = 0.0` (desert world)
  - `waterFraction = 1.0` (ocean world)
  - `plateCount = 1` (single-plate stagnant lid)
  - `plateCount = 21` (maximum plates)
  - `Size.COLOSSAL` performance

- [ ] **Configuration interaction tests:**
  - `heightScaleMultiplier = 0.5` effect
  - `erosionIterations = 0` (no erosion)
  - `rainfallScale = 2.0` (extreme rainfall)
  - `enableRivers = false`

- [ ] **Visual regression tests:**
  - Snapshot testing for mesh vertex positions
  - Height distribution histograms

---

#### 7. Improve Error Handling (Estimated: 3-5 days)
**Files:** Multiple

**Tasks:**
- [ ] Add validation in `PlanetGenerator.generate()`:
  ```java
  if (config.plateCount() > config.polyCount() / 10) {
      throw new IllegalArgumentException("Too many plates for mesh size");
  }
  ```
- [ ] Validate intermediate results:
  - Plate assignment completeness
  - Height range validity
  - Climate zone coverage
- [ ] Add generation progress callbacks:
  ```java
  interface GenerationProgressListener {
      void onPhaseStarted(String phase);
      void onPhaseCompleted(String phase, int polygonsProcessed);
      void onError(String phase, Exception e);
  }
  ```

---

### 🟢 LOW PRIORITY

#### 8. Performance Optimizations (Estimated: 1 week)
**Files:** `ElevationCalculator.java`, `ErosionCalculator.java`

**Tasks:**
- [ ] Profile terrain distribution loops in `ElevationCalculator`
- [ ] Cache `percentAbove()`/`percentBelow()` between iterations
- [ ] Optimize `ErosionCalculator` parallel thresholds:
  - Current: `PARALLEL_THRESHOLD = 5000` (only LARGE+ triggers parallel)
  - Consider: Dynamic threshold based on CPU cores
- [ ] Reduce `Random` object allocations in parallel streams

---

#### 9. Add Rain Shadow Effect (Estimated: 1-2 weeks)
**File:** `ErosionCalculator.java`

**Current limitation:** Linear elevation factor oversimplifies orographic precipitation

**Tasks:**
- [ ] Implement prevailing wind direction (configurable)
- [ ] Calculate windward vs leeward sides of mountains
- [ ] Boost rainfall on windward side
- [ ] Reduce rainfall on leeward side (rain shadow)
- [ ] Add `windDirection` to `PlanetConfig`

---

#### 10. Support Multiple Climate Models (Estimated: 2 weeks)
**File:** `ClimateCalculator.java`

**Current limitation:** Simple 3-zone latitude model

**Tasks:**
- [ ] Create `ClimateModel` interface:
  ```java
  interface ClimateModel {
      ClimateZone calculate(double latitude, double elevation, double rainfall);
  }
  ```
- [ ] Implement models:
  - `SimpleLatitudeModel` (current)
  - `KoppenGeigerModel` (5 main groups)
  - `ElevationAdjustedModel` (altitude affects temperature)
- [ ] Add `climateModel` to `PlanetConfig`
- [ ] Support axial tilt affecting climate zones

---

#### 11. Add River Width Variation (Estimated: 3-5 days)
**File:** `PlanetRenderer.java`, `JavaFxPlanetMeshConverter.java`

**Current limitation:** Rivers rendered as thin lines

**Tasks:**
- [ ] Track accumulated flow along river path
- [ ] Scale river width based on flow volume
- [ ] Render rivers as tube meshes instead of lines
- [ ] Add delta/estuary widening at ocean terminus

---

#### 12. Fix Sediment Mass Conservation (Estimated: 3-5 days)
**File:** `ErosionCalculator.java`

**Current bug:** Line 304 only deposits 50% of eroded material

**Tasks:**
- [ ] Track total eroded sediment
- [ ] Distribute sediment to downhill neighbors
- [ ] Ensure `totalEroded == totalDeposited`
- [ ] Add sediment tracking to `ErosionResult`

---

## Component Status

| Component | Lines | Complexity | Status | Next Action |
|-----------|-------|------------|--------|-------------|
| PlanetGenerator | 246 | Low | ✅ Good | — |
| IcosahedralMesh | 370 | Medium | ⚠️ OK | Fix float comparison |
| PlateAssigner | 129 | Low | ✅ Good | — |
| BoundaryDetector | 255 | Medium | ✅ Good | Documented |
| ElevationCalculator | 640 | Medium | ✅ Good | Refactored |
| ClimateCalculator | 52 | Low | ✅ Good | — |
| ErosionCalculator | 640 | Medium | ✅ Good | Documented |
| PlanetRenderer | 440 | Low | ✅ Good | — |
| JavaFxPlanetMeshConverter | 1200 | Medium | ✅ Good | Consolidated |
| PlanetConfig | 350 | Low | ✅ Good | BoundaryEffectConfig added |

---

## Estimated Total Effort (Remaining)

| Priority | Tasks | Estimated Time |
|----------|-------|----------------|
| ✅ High | 0 (3 completed) | Done |
| 🟡 Medium | 4 | 3-4 weeks |
| 🟢 Low | 5 | 4-6 weeks |
| **Total Remaining** | **9** | **7-10 weeks** |

---

## Recent Changes (January 2026)

### High Priority Refactoring Completed

#### ElevationCalculator Refactoring
- Split `applyBoundaryEffect()` into `applyConvergentBoundary()`, `applyDivergentBoundary()`, `applyTransformBoundary()`
- Added `BoundaryEffectConfig` record to `PlanetConfig` with all boundary parameters
- Added `applyEffectLayers()` helper for multi-layer terrain effects
- Added JavaDoc with physical basis for each boundary type

#### JavaFxPlanetMeshConverter Consolidation
- Created `MeshConversionOptions` record with factory methods
- Added `convertUnified()` and `convertUnifiedByHeight()` entry points
- Replaced string-based vertex quantization with bit-packed long hash
- Made debug output conditional via `DEBUG_LOGGING` flag
- Created `addFanTriangles()` helper for common triangulation

#### Magic Number Documentation
- Documented all erosion constants (rainfall thresholds, river sources)
- Documented mountain/island chain length scaling
- Documented plate count limits (divergent=2, subduction=3)
- Documented config defaults (oceanicPlateRatio, hotspotProbability)

### Earlier Improvements
- Added smooth normals via `VertexFormat.POINT_NORMAL_TEXCOORD`
- Implemented per-vertex height averaging to eliminate seams
- Center height now averaged from edge vertices (no more center bulge)
- Equalized `HEIGHT_SCALE = EDGE_HEIGHT_SCALE = 0.02`
- Relaxed `ElevationCalculatorTest` tolerances for stochastic generation

