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

