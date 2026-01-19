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

### ✅ MEDIUM PRIORITY (COMPLETED)

#### 4. ~~Fix Floating-Point Comparisons~~ ✅ DONE (January 2026)
**Files:** `IcosahedralMesh.java`, `ClimateCalculator.java`

**Completed:**
- [x] Added `EPSILON` constant (`1e-10`)
- [x] Replaced `v1.equals(v2)` with `v1.distance(v2) < EPSILON`
- [x] Replaced `norm == 0.0` with `norm < EPSILON`
- [x] Added unit tests for edge cases

---

#### 5. ~~Add Configuration for Erosion Thresholds~~ ✅ DONE (January 2026)
**File:** `ErosionCalculator.java`, `PlanetConfig.java`

**Completed:**
- [x] Added to `PlanetConfig`:
  - `rainfallThreshold` (default 0.3)
  - `riverSourceThreshold` (default 0.7)
  - `riverSourceElevationMin` (default 0.5)
  - `erosionCap` (default 0.3)
  - `depositionFactor` (default 0.5)
  - `riverCarveDepth` (default 0.3)
- [x] Updated `ErosionCalculator` to use config values
- [x] Added validation in `PlanetConfig.Builder`

---

### 🟡 MEDIUM PRIORITY (Remaining)

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

### ✅ LOW PRIORITY (ALL COMPLETED)

#### 8. ~~Performance Optimizations~~ ✅ DONE (January 2026)
**Files:** `ErosionCalculator.java`

**Completed:**
- [x] Parallelized rain shadow calculation for large meshes (>5000 polygons)
- [x] Added `Arrays.parallelSort()` for height-ordered erosion processing
- [x] Extracted `calculateRainShadowForPolygon()` for parallel execution
- [x] Added `getSortedByHeightDescending()` with parallel sort option

---

#### 9. ~~Add Rain Shadow Effect~~ ✅ DONE (January 2026)
**File:** `ErosionCalculator.java`

**Completed:**
- [x] Implemented prevailing wind direction based on latitude:
  - Trade winds (0-30°): easterly
  - Westerlies (30-60°): from west
  - Polar easterlies (60-90°): easterly
- [x] Added `traceUpwindForMountains()` to detect blocking terrain
- [x] Rain shadow reduces rainfall by up to 80% on leeward slopes
- [x] Wind direction calculated per-polygon based on local "east" tangent

---

#### 10. ~~Support Multiple Climate Models~~ ✅ DONE (January 2026)
**File:** `ClimateCalculator.java`, `PlanetConfig.java`

**Completed:**
- [x] Created `ClimateModel` enum with 5 models:
  - `SIMPLE_LATITUDE` - Default Earth-like (0-30° tropical, 30-60° temperate, 60°+ polar)
  - `HADLEY_CELLS` - Realistic with ITCZ and subtropical high pressure zones
  - `ICE_WORLD` - Extensive polar caps, narrow temperate equatorial band
  - `TROPICAL_WORLD` - Extended tropical zone (0-45°), small polar caps
  - `TIDALLY_LOCKED` - Day/night sides based on star-facing direction
- [x] Added `climateModel` to `PlanetConfig` with builder support
- [x] Added 9 new climate model tests

---

#### 11. ~~Add River Width Variation~~ ✅ DONE (January 2026)
**File:** `ProceduralPlanetViewerDialog.java`

**Completed:**
- [x] Added `calculateCumulativeFlow()` to track accumulated rainfall along river path
- [x] Created `createFlowBasedRiverSegment()` with flow-weighted width
- [x] River width uses square root scaling (0.002 → 0.008 radius)
- [x] Color gradient from light blue (source) to dark blue (mouth)

---

#### 12. ~~Fix Sediment Mass Conservation~~ ✅ DONE (January 2026)
**File:** `ErosionCalculator.java`

**Completed:**
- [x] Implemented carrying capacity model in `applySedimentFlow()`
- [x] Tracks sediment being carried at each polygon
- [x] Deposits excess when downstream capacity decreases
- [x] All sediment eventually deposited at lowest points (mass conserved)
- [x] Process polygons in height order for proper flow simulation

---

## Component Status

| Component | Lines | Complexity | Status | Next Action |
|-----------|-------|------------|--------|-------------|
| PlanetGenerator | 293 | Low | ✅ Good | — |
| IcosahedralMesh | 370 | Medium | ✅ Good | Fixed float comparison |
| PlateAssigner | 129 | Low | ✅ Good | — |
| BoundaryDetector | 255 | Medium | ✅ Good | Documented |
| ElevationCalculator | 640 | Medium | ✅ Good | Refactored |
| ClimateCalculator | 220 | Medium | ✅ Good | 5 climate models |
| ErosionCalculator | 750 | Medium | ✅ Good | Rain shadow + mass conservation |
| PlanetRenderer | 440 | Low | ✅ Good | — |
| JavaFxPlanetMeshConverter | 1200 | Medium | ✅ Good | Consolidated |
| PlanetConfig | 450 | Low | ✅ Good | Climate model + erosion config |
| ProceduralPlanetViewerDialog | 950 | Medium | ✅ Good | Legend, screenshot, flow rivers |

---

## Estimated Total Effort (Remaining)

| Priority | Tasks | Estimated Time |
|----------|-------|----------------|
| ✅ High | 0 (3 completed) | Done |
| ✅ Medium | 2 of 4 completed | — |
| 🟡 Medium | 2 remaining | 1-2 weeks |
| ✅ Low | 5 (all completed) | Done |
| **Total Remaining** | **2** | **1-2 weeks** |

---

## Recent Changes (January 2026)

### Low Priority Features Completed

#### Rain Shadow Effect
- Wind direction varies by latitude (trade winds, westerlies, polar easterlies)
- `traceUpwindForMountains()` checks 5 steps upwind for blocking terrain
- Rainfall reduced up to 80% in mountain shadows
- Parallelized for large meshes

#### Multiple Climate Models
- Added `ClimateModel` enum: SIMPLE_LATITUDE, HADLEY_CELLS, ICE_WORLD, TROPICAL_WORLD, TIDALLY_LOCKED
- Tidally locked model uses star-facing direction instead of latitude
- Ice world has 60%+ polar coverage
- Tropical world extends tropical zone to 45° latitude

#### River Width Variation
- Cumulative flow calculated from rainfall along river path
- Width scales with square root of flow (natural appearance)
- Gradient coloring from light blue (source) to dark blue (mouth)
- Frozen rivers use ice-blue gradient

#### Sediment Mass Conservation
- Carrying capacity model: steeper slopes carry more sediment
- Excess sediment deposited when slope decreases
- All sediment eventually deposited (no mass loss)
- Height-ordered processing for proper downhill flow

#### Performance Optimizations
- Rain shadow calculation parallelized for >5000 polygons
- Height sorting uses `Arrays.parallelSort()` for large meshes
- Extracted per-polygon methods for parallel execution

### Viewer Polish Features
- Screenshot export (PNG) via "Save" button
- Color legend overlay with elevation and boundary colors
- Plate boundary visualization (convergent=red, divergent=cyan, transform=yellow)
- Climate zone latitude rings (±30°, ±60°)
- Auto-rotate animation with "Spin" checkbox

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

