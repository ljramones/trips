# Procedural Planet Generator

Java port of [VictorGordan/4x_planet_generator_godot](https://github.com/VictorGordan/4x_planet_generator_godot) for TRIPS.

Generates procedural planets with tectonic plate simulation, producing spherical meshes of hexagons and pentagons (Goldberg polyhedron) with realistic terrain features.

## Package

```
com.teamgannon.trips.planetarymodelling.procedural
```

## FastNoiseLite Integration Summary

The procedural planet generator integrates with the extended FastNoiseLite noise system:

| Feature | FastNoiseLite Usage | Extension Used |
|---------|---------------------|----------------|
| Terrain height base | OpenSimplex2 + HybridMulti / Ridged | `HierarchicalNoise`, `RidgeTransform`, `PowerTransform` |
| Crater/volcano placement | Cellular (Voronoi) + domain warp | `CellularNoiseGen`, `DomainWarpProcessor` |
| Rainfall/wind patterns | 2D OpenSimplex2 + wind direction tracing | `TiledNoise` (seamless global) |
| Erosion flow direction | Downhill neighbor selection + noise perturbation | — |
| Atmosphere motion | Curl noise for divergence-free flow | `TurbulenceNoise.curl3D()` |
| Lighting/bump mapping | Analytical normals from noise gradients | `NoiseDerivatives.computeNormal3D()` |
| Large coordinate precision | Chunked evaluation for galaxy-scale coords | `ChunkedNoise`, `DoublePrecisionNoise` |
| LOD/streaming | Adaptive detail by view distance | `HierarchicalNoise.sampleAdaptive()`, `LODNoise` |

**Key Classes:**
- `FastNoiseLite` — Facade maintaining backward compatibility
- `HierarchicalNoise` — Quadtree/octree LOD with `sampleAdaptive()` for view-distance scaling
- `TiledNoise` — Seamless wrapping via 4D torus projection
- `TurbulenceNoise` — Curl noise (`curl2D`, `curl3D`) for incompressible flow fields
- `NoiseDerivatives` — Analytical gradients and normal computation
- `DomainWarpProcessor` — Coordinate distortion for organic features
- `ChainedTransform` — Composable transforms: `RidgeTransform` → `PowerTransform` → `RangeTransform`

## Features

- **Goldberg Polyhedron Mesh**: Subdivided icosahedron producing 12 pentagons + hexagons
- **Tectonic Plate Simulation**: Flood-fill plate assignment with configurable plate count (7-21)
- **Plate Boundary Interactions**: Convergent, divergent, and transform boundaries
- **Terrain Generation**: Heights from -4 (deep ocean) to +4 (high mountains)
- **Impact Features**: Craters and volcanic features with configurable density and profiles
- **Continuous Heights**: Optional continuous height field with relief normalization
- **Hydrology**: Flow accumulation, river width scaling, and lake filling
- **Climate Zones**: Multiple climate models including seasonal insolation with axial tilt
- **Biome Classification**: 16 biome types based on elevation, climate, and rainfall
- **City Suitability Analysis**: Post-generation analysis for settlement placement
- **Reproducible**: Seed-based generation for consistent results
- **JavaFX Integration**: Native JavaFX 3D rendering via `JavaFxPlanetMeshConverter`

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
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

// Generate planet
PlanetConfig config = PlanetConfig.builder()
    .seed(12345L)
    .size(PlanetConfig.Size.STANDARD)  // 4412 polygons
    .plateCount(14)
    .waterFraction(0.66)
    .build();

PlanetGenerator.GeneratedPlanet planet = PlanetGenerator.generate(config);

// Render in JavaFX
TriangleMesh mesh = JavaFxPlanetMeshConverter.convert(planet.polygons(), planet.heights(), 1.0);
MeshView meshView = new MeshView(mesh);
scene.getRoot().getChildren().add(meshView);
```

## Integration with Accrete

```java
// Direct generation from Accrete planet (recommended)
PlanetGenerator.GeneratedPlanet planet = PlanetGenerator.generateFromAccrete(
    accretePlanet, seed);

// Or manual config with TectonicBias
TectonicBias bias = TectonicBias.fromAccretePlanet(accretePlanet);
PlanetConfig config = PlanetConfig.builder()
    .seed(seed)
    .fromAccreteRadius(accretePlanet.getRadius())
    .waterFraction(accretePlanet.getHydrosphere() / 100.0)
    .build();
PlanetConfig biasedConfig = bias.applyTo(config, seed);
PlanetGenerator.GeneratedPlanet planet = PlanetGenerator.generate(biasedConfig);
```

## Classes

| Class | Purpose |
|-------|---------|
| `PlanetConfig` | Immutable configuration record with builder |
| `PlanetGenerator` | Facade orchestrating the generation pipeline |
| `PlanetGenerator.GeneratedPlanet` | Result record containing all generated data |
| `Polygon` | Single hex/pentagon with center + vertices |
| `IcosahedralMesh` | Goldberg polyhedron geometry generation |
| `AdjacencyGraph` | Polygon neighbor relationships |
| `PlateAssigner` | Tectonic plate flood-fill assignment |
| `BoundaryDetector` | Plate types and boundary interactions |
| `ElevationCalculator` | Terrain height from plate tectonics |
| `ClimateCalculator` | Climate zones (6 models, incl. seasonal insolation) |
| `ErosionCalculator` | Erosion simulation, rivers, rainfall, flow accumulation |
| `GenerationProgressListener` | Progress callback interface for UI updates (8 phases) |
| `TectonicBias` | Translates Accrete parameters to tectonic settings |
| `JavaFxPlanetMeshConverter` | Converts mesh to JavaFX TriangleMesh |
| `PlanetRenderer` | Jzy3d renderer (optional) |
| `TectonicService` | Spring-compatible service with caching (in `service/` subpackage) |
| `ProceduralPlanetPersistenceHelper` | Stores/restores procedural planet data in ExoPlanet entities |
| **Impact Features** (`impact/` subpackage) | |
| `CraterProfile` | Enum with 8 crater/volcano height profiles (radial functions) |
| `CraterCalculator` | Places craters and volcanoes using FastNoiseLite and BFS traversal |
| `ImpactResult` | Immutable record with crater/volcano positions and metadata |
| **Biome Classification** (`biome/` subpackage) | |
| `BiomeType` | Enum with 16 biome types including habitability scores |
| `BiomeClassifier` | Maps climate + rainfall + elevation to biome types |
| **Post-Generation Analysis** (`analysis/` subpackage) | |
| `CitySuitabilityAnalyzer` | Settlement suitability analysis based on terrain factors |

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

Continuous heights can be enabled with `useContinuousHeights` and normalized
to a configurable relief range (`continuousReliefMin`, `continuousReliefMax`).

## Climate Zones (Simple Latitude)

| Zone | Latitude Range |
|------|----------------|
| TROPICAL | 0° - 30° |
| TEMPERATE | 30° - 60° |
| POLAR | 60° - 90° |

Other climate models adjust or replace these latitude bands.

## Accessing Generated Data

```java
GeneratedPlanet planet = PlanetGenerator.generate(config);

// Geometry
List<Polygon> polygons = planet.polygons();
for (Polygon p : polygons) {
    Vector3D center = p.center();
    List<Vector3D> vertices = p.vertices();
    boolean isPentagon = p.isPentagon();
}

// Terrain heights (-4 to +4)
int[] heights = planet.heights();

// Pre-erosion baseline heights (before rainfall/river carving)
double[] baseHeights = planet.baseHeights();

// High-precision heights (for smooth rendering)
double[] preciseHeights = planet.preciseHeights();

// Climate zones
ClimateCalculator.ClimateZone[] climates = planet.climates();

// Plate data
PlateAssigner.PlateAssignment plates = planet.plateAssignment();
int[] polyToPlate = plates.plateIndex();  // polygon -> plate mapping
List<List<Integer>> platePolygons = plates.plates();  // plate -> polygon list

// Plate boundaries
BoundaryDetector.BoundaryAnalysis boundaries = planet.boundaryAnalysis();
BoundaryDetector.PlateType[] plateTypes = boundaries.plateTypes();  // OCEANIC or CONTINENTAL

// Erosion data
ErosionCalculator.ErosionResult erosion = planet.erosionResult();
double[] rainfall = planet.rainfall();           // precipitation per polygon
double[] flow = planet.flowAccumulation();       // flow accumulation for rivers
boolean[] lakes = planet.lakeMask();              // lake-filled basins
List<List<Integer>> rivers = planet.rivers();    // river paths as polygon index lists
boolean[] frozen = planet.frozenRiverTerminus(); // which rivers end frozen

// Biome classification (16 types)
BiomeType[] biomes = planet.biomes();
Map<BiomeType, Integer> distribution = planet.biomeDistribution();  // count per type
Map<BiomeType, Double> landPercentages = planet.landBiomePercentages();  // % of land area

// City suitability analysis (post-generation)
double[] suitability = CitySuitabilityAnalyzer.analyze(planet);
CitySuitabilityAnalyzer analyzer = new CitySuitabilityAnalyzer(planet);
CitySuitabilityAnalyzer.SuitabilityResult result = analyzer.analyzeWithStatistics(10);
List<Integer> bestLocations = result.bestLocations();  // top N polygon indices
double maxSuitability = result.maxSuitability();
double avgSuitability = result.averageSuitability();
```

## Configuration Examples

```java
// Basic configuration with climate and terrain
PlanetConfig config = PlanetConfig.builder()
    .seed(12345L)
    .size(PlanetConfig.Size.STANDARD)
    .plateCount(14)
    .waterFraction(0.66)
    .useContinuousHeights(true)
    .continuousReliefMin(-3.0)
    .continuousReliefMax(5.0)
    .climateModel(ClimateCalculator.ClimateModel.SEASONAL)
    .axialTiltDegrees(23.5)
    .seasonalOffsetDegrees(0.0)
    .build();

// With impact craters and volcanoes
PlanetConfig cratered = PlanetConfig.builder()
    .seed(42L)
    .size(PlanetConfig.Size.STANDARD)
    .craterDensity(0.3)           // 0.0-1.0, probability of crater placement
    .craterDepthMultiplier(1.0)   // Scale crater depth
    .craterMaxRadius(8)           // Maximum polygon hops from center
    .enableVolcanos(true)         // Enable volcanic features
    .volcanoDensity(0.2)          // 0.0-1.0, volcano placement density
    .build();
```

### Impact Feature Parameters

| Parameter | Default | Description |
|-----------|---------|-------------|
| `craterDensity` | 0.0 | Probability of crater placement (0.0 = none, 1.0 = dense) |
| `craterDepthMultiplier` | 1.0 | Scales crater depth (affects terrain modification) |
| `craterMaxRadius` | 8 | Maximum crater size in polygon hops |
| `enableVolcanos` | false | Enable volcanic feature placement |
| `volcanoDensity` | 0.0 | Probability of volcano placement (0.0-1.0) |

## Reproducibility

Same seed + config = identical planet:

```java
var config = PlanetConfig.builder().seed(42L).build();
GeneratedPlanet p1 = PlanetGenerator.generate(config);
GeneratedPlanet p2 = PlanetGenerator.generate(config);
// p1 and p2 are identical
```

Sub-seeds derived for each generation phase:
- Phase 1: Mesh generation
- Phase 2: Adjacency graph
- Phase 3: Plate assignment
- Phase 4: Boundary detection
- Phase 5: Elevation calculation
- Phase 6: Impact features (craters/volcanoes)
- Phase 7: Climate calculation
- Phase 8: Erosion calculation

## Persistence Layer

Store and restore procedural planet configurations using `ProceduralPlanetPersistenceHelper`:

```java
// After generating a planet, store metadata in ExoPlanet entity
ProceduralPlanetPersistenceHelper.populateProceduralMetadata(
    exoPlanet,           // Target entity
    config,              // PlanetConfig used
    seed,                // Generation seed
    planet,              // GeneratedPlanet result
    "manual"             // Source label (e.g., "accrete", "manual", "imported")
);
// Saves: seed, generator version, Accrete snapshot, config overrides, preview PNG

// Later, rebuild the config from stored data
PlanetConfig restored = ProceduralPlanetPersistenceHelper.buildConfigFromSnapshots(exoPlanet);
PlanetGenerator.GeneratedPlanet regenerated = PlanetGenerator.generate(restored);
// Result matches original planet (reproducible from stored snapshots)

// Or build config from ExoPlanet physical properties only
PlanetConfig fromPhysicals = ProceduralPlanetPersistenceHelper.buildConfigFromExoPlanet(
    exoPlanet, seed);
```

**Stored Data:**
- `proceduralSeed` - Generation seed for reproducibility
- `proceduralGeneratorVersion` - Version string for migration compatibility
- `proceduralAccreteSnapshot` - JSON snapshot of physical parameters (mass, radius, gravity, etc.)
- `proceduralOverrides` - JSON snapshot of PlanetConfig overrides
- `proceduralGeneratedAt` - ISO timestamp of generation
- `proceduralPreview` - PNG byte array (256x256 preview image)
- `proceduralSource` - Label indicating generation source

## Service Layer

For Spring Boot integration (in `service/` subpackage):

```java
@Service
public class MyTectonicService extends TectonicService {}

// Usage
@Autowired TectonicService tectonicService;

GeneratedPlanet planet = tectonicService.generateFromAccrete(
    seed, radiusKm, massEarths, waterFraction
);

// Caching built-in
GeneratedPlanet cached = tectonicService.getCached(planet.config());
tectonicService.evict(planet.config());
tectonicService.clearCache();
```

## JavaFX Rendering

```java
// Convert to JavaFX mesh
TriangleMesh mesh = JavaFxPlanetMeshConverter.convert(planet.polygons(), planet.heights(), 1.0);
MeshView meshView = new MeshView(mesh);
meshView.setScaleX(100);
meshView.setScaleY(100);
meshView.setScaleZ(100);

// Smooth rendering with precise heights
TriangleMesh smoothMesh = JavaFxPlanetMeshConverter.convertSmooth(
    planet.polygons(), planet.preciseHeights(), 1.0);
MeshView smoothView = new MeshView(smoothMesh);

// Wireframe for debugging (JavaFX)
meshView.setDrawMode(javafx.scene.shape.DrawMode.LINE);

// Add to SubScene with PerspectiveCamera
SubScene subScene = new SubScene(meshView, 800, 600, true, SceneAntialiasing.BALANCED);
subScene.setCamera(new PerspectiveCamera(true));

// With progress listener for UI feedback
GenerationProgressListener listener = new GenerationProgressListener() {
    @Override
    public void onPhaseStarted(Phase phase, String description) {
        Platform.runLater(() -> statusLabel.setText(description));
    }
    @Override
    public void onProgressUpdate(Phase phase, double progress) {
        Platform.runLater(() -> progressBar.setProgress(
            GenerationProgressListener.calculateOverallProgress(phase, progress)));
    }
    @Override
    public void onPhaseCompleted(Phase phase) {}
    @Override
    public void onGenerationCompleted() {
        Platform.runLater(() -> statusLabel.setText("Complete!"));
    }
};
GeneratedPlanet planet = PlanetGenerator.generate(config, listener);
```

### Procedural Viewer Dialog

For the in-app experience, use `ProceduralPlanetViewerDialog`, which provides
interactive zoom/rotation, axial tilt + pole markers, and multiple visualization
modes (terrain, rainfall, smooth terrain, rivers):

```java
ProceduralPlanetViewerDialog dialog = new ProceduralPlanetViewerDialog("My Planet", planet);
dialog.showAndWait();
```

#### Keyboard Controls (Flight Simulator Style)

Click on the 3D view to give it focus, then use:

| Key | Action |
|-----|--------|
| W | Zoom in (move camera forward) |
| S | Zoom out (move camera back) |
| A | Rotate left (Y-axis) |
| D | Rotate right (Y-axis) |
| Q | Rotate up (X-axis) |
| E | Rotate down (X-axis) |
| R | Reset view to default |
| SPACE | Toggle auto-spin |

#### UI Controls (Current)

**Generation**
- Seed (manual + randomize), Size, Plates, Water %, Erosion iterations
- River threshold, Height scale
- Axial tilt (degrees), Seasonal offset (degrees)
- Continuous heights toggle + Relief min/max
- Climate model dropdown

**View**
- Zoom slider, Auto-spin checkbox, Reset view button

**Overlays**
- Rivers, Lakes, Flow-scaled rivers
- Plate boundaries, Climate zones
- Pole marker, Atmosphere

**Render**
- Terrain colors vs Rainfall heatmap
- Smooth terrain, Wireframe

**Info**
- Polygons/Rivers/Plates counts
- Save Screenshot

Axial tilt rotates the globe around the tilt axis; auto-spin uses that same axis,
and pole markers show the current north/south poles.

## Generation Pipeline

```
PlanetConfig
    ↓
IcosahedralMesh.generate()      → List<Polygon>           [Phase 1: MESH_GENERATION]
    ↓
AdjacencyGraph                   → neighbor relationships  [Phase 2: ADJACENCY_GRAPH]
    ↓
PlateAssigner.assign()           → polygon-to-plate        [Phase 3: PLATE_ASSIGNMENT]
    ↓
BoundaryDetector.analyze()       → plate/boundary types    [Phase 4: BOUNDARY_DETECTION]
    ↓
ElevationCalculator.calculate()  → int[] heights           [Phase 5: ELEVATION_CALCULATION]
    ↓
CraterCalculator.calculate()     → craters + volcanoes     [Phase 6: IMPACT_FEATURES] (optional)
    ↓
ClimateCalculator.calculate()    → ClimateZone[] climates  [Phase 7: CLIMATE_CALCULATION]
    ↓
ErosionCalculator.calculate()    → rivers, rainfall, etc.  [Phase 8: EROSION_CALCULATION]
    ↓
BiomeClassifier.classify()       → BiomeType[] biomes      (post-processing)
    ↓
GeneratedPlanet (result record)
```

---

## Scientific & Geological Background

This section documents the geophysical basis for the procedural terrain generation algorithms.

### Tectonic Plate Theory

The generator uses a simplified model of plate tectonics, the dominant theory explaining Earth's large-scale geological features.

**Plate Count (7-21):**
- Earth has 7 major plates and ~8 minor plates
- Small planets/moons may have fewer plates or a "stagnant lid" (single plate)
- Larger, hotter planets may have more vigorous convection and more plates
- The generator's range of 7-21 plates reflects this variability

**Oceanic vs Continental Crust:**
- `oceanicPlateRatio` defaults to 0.65 (65% oceanic), matching Earth's ~60-70% oceanic coverage
- Oceanic crust is denser (basalt, ~3.0 g/cm³) and thinner (5-10 km)
- Continental crust is lighter (granite, ~2.7 g/cm³) and thicker (30-50 km)
- This density difference drives subduction at convergent boundaries

**Plate Assignment Algorithm:**
- Uses flood-fill from random seed points (analogous to mantle plumes)
- Each plate grows from its seed until meeting another plate
- Produces irregular plate shapes similar to Earth's actual plates

### Boundary Types & Terrain Effects

The three fundamental boundary types create distinct geological features:

#### Convergent Boundaries (Plates Colliding)

**Oceanic-Oceanic Convergence:**
- One plate subducts beneath the other
- Creates deep ocean trenches (height: -4 to -3)
- Volcanic island arcs form from melting subducted material
- Real examples: Mariana Trench, Aleutian Islands

**Oceanic-Continental Convergence:**
- Oceanic plate always subducts (denser)
- Creates coastal mountain ranges (height: +3 to +4)
- Volcanic activity from subduction-zone melting
- Real examples: Andes, Cascades

**Continental-Continental Convergence:**
- Neither plate subducts; instead, crust crumples and thickens
- Creates the highest mountain ranges (height: +4)
- No volcanic activity (no subduction)
- Real examples: Himalayas, Alps

#### Divergent Boundaries (Plates Spreading)

**Oceanic Divergence:**
- Mid-ocean ridges form as magma rises to fill the gap
- Slight elevation increase along the ridge (height: -2 to -1)
- Creates new oceanic crust continuously
- Real examples: Mid-Atlantic Ridge, East Pacific Rise

**Continental Divergence:**
- Creates rift valleys with normal faulting (height: -1 to 0)
- May eventually split a continent, forming a new ocean
- Associated volcanism as mantle rises
- Real examples: East African Rift, Rio Grande Rift

#### Transform Boundaries (Plates Sliding)

- Horizontal sliding motion, no significant elevation change
- Creates fault zones with earthquake activity
- May form pull-apart basins at restraining bends
- Real examples: San Andreas Fault, Alpine Fault (NZ)

### Elevation Physics

**Base Height Calculation:**
- Oceanic plates start at depth (height: -3)
- Continental plates start elevated (height: 0)
- Boundary effects modify these base elevations

**Mountain Chain Generation:**
- Length scales with plate size (larger plates = longer mountain chains)
- `mountainLength = 0.12 × (platePolygons.size())` based on empirical analysis of Earth's ranges
- Andes: ~7000 km along ~3,000,000 km² plate margin

**Island Chain Generation:**
- Forms at oceanic-oceanic subduction zones
- `islandLength = 0.08 × (platePolygons.size())`
- Islands are positive elevation anomalies in oceanic context

**Hotspot Volcanism:**
- `hotspotProbability = 0.12` produces ~1-2 hotspots per planet
- Creates isolated volcanic islands mid-plate
- Real examples: Hawaiian Islands, Yellowstone

### Climate & Atmospheric Circulation

The climate models simulate Earth's atmospheric circulation patterns:

#### Hadley Cells Model

Earth's atmosphere divides into three circulation cells per hemisphere:

```
            Polar Cell
           (60°-90°)
    ────────────────────── 60°N
         Ferrel Cell
          (30°-60°)
    ────────────────────── 30°N
         Hadley Cell
          (0°-30°)
    ══════════════════════ 0° (Equator / ITCZ)
         Hadley Cell
          (0°-30°)
    ────────────────────── 30°S
         Ferrel Cell
          (30°-60°)
    ────────────────────── 60°S
           Polar Cell
           (60°-90°)
```

**Climate Zone Effects:**
- **ITCZ (0-10°)**: Rising warm air, high rainfall, tropical rainforests
- **Subtropical highs (20-30°)**: Descending dry air, deserts (Sahara, Arabian)
- **Temperate (30-60°)**: Prevailing westerlies, moderate rainfall
- **Polar (60-90°)**: Cold, descending air, low precipitation

#### Prevailing Wind Directions

Wind direction is critical for the rain shadow effect:

| Latitude Zone | Wind Direction | Name |
|---------------|----------------|------|
| 0° - 30° | From East | Trade Winds |
| 30° - 60° | From West | Westerlies |
| 60° - 90° | From East | Polar Easterlies |

These patterns result from the Coriolis effect deflecting air moving away from equatorial high pressure.

### Rain Shadow Effect

The orographic effect creates rain shadows on the leeward side of mountains:

```
Prevailing Wind →
                        ↗ Moist air rises
                      /   ↓ Cooling & precipitation
                    /     ▓▓▓ Mountain range
                  /        \
 Moist ocean air →          \ Dry air descends (rain shadow)
                             \
                              → Arid leeward region
```

**Implementation:**
- `traceUpwindForMountains()` checks 5 polygons in the upwind direction
- If mountains (height ≥ 2) are found, rainfall is reduced by up to 80%
- Effect is proportional to mountain height and proximity

**Real Examples:**
- Atacama Desert (Chilean Andes)
- Gobi Desert (Tibetan Plateau)
- Death Valley (Sierra Nevada)

### Erosion & Hydrology

The erosion simulation models the water cycle's effect on terrain:

#### Rainfall Distribution

- Base rainfall proportional to climate zone (tropical > temperate > polar)
- Modified by rain shadow effect (see above)
- Modified by divergent ocean boundaries (+30% moisture from volcanic steam)
- Upwind ocean proximity adds a moisture boost (simple fetch-based model)
- `rainfallThreshold = 0.3`: Minimum rainfall for significant erosion

#### River Formation

Rivers form through a priority-based downhill tracing algorithm:

1. **Source Selection:**
   - Requires rainfall ≥ `riverSourceThreshold` (0.7) and elevation ≥ `riverSourceElevationMin` (0.5)
   - Prevents rivers from starting in dry or low areas

2. **Flow Path:**
   - Water flows to the lowest neighboring polygon
   - Flow accumulates, increasing river width downstream
   - Rivers terminate at ocean (height < 0) or lakes (no downhill path)

3. **Frozen Rivers:**
   - Rivers ending in polar zones are marked as frozen
   - Rendered with ice-blue gradient instead of water-blue

#### Sediment Transport

The sediment model conserves mass during erosion:

**Carrying Capacity:**
```
capacity = slope × rainfall × SEDIMENT_CAPACITY_FACTOR
```
- Steeper slopes and higher water flow can carry more sediment
- When slope decreases, excess sediment is deposited

**Erosion Process:**
1. Calculate carrying capacity at each polygon
2. Pick up sediment up to capacity limit (`erosionCap = 0.3`)
3. Transport downstream, depositing when capacity decreases
4. Final deposition at river mouths creates deltas/alluvial fans

**Height Modification:**
- Erosion lowers terrain by up to `erosionCap` (0.3)
- Deposition raises terrain by `depositionFactor × sediment` (50%)
- River channels carved by additional `riverCarveDepth` (0.3)

#### Lake Filling and Basins

Closed basins are detected and filled until a spill point exists:

1. Identify sinks with no downhill neighbor
2. Raise the local water surface until an outflow neighbor exists
3. Mark polygons as lakes and re-trace rivers to the ocean

This produces interior lakes and more realistic drainage networks.

### Seasonal Insolation and Axial Tilt

Seasonal climates are computed by averaging insolation over a year:

```
cos(zenith) = sin(lat) * sin(subsolarLat)
           + cos(lat) * cos(subsolarLat)
```

Where `subsolarLat = axialTilt * sin(phase)`. Higher axial tilt expands
temperate zones into high latitudes and reduces permanent polar caps.

### Impact Craters and Volcanic Features

The impact system simulates meteorite craters and volcanic landforms on the polygon mesh.

#### Crater Placement Algorithm

Craters are placed using FastNoiseLite's Cellular noise:
1. Each polygon's center is evaluated against cellular noise
2. Noise values above `(1.0 - craterDensity)` threshold become crater centers
3. BFS traversal identifies all polygons within the crater radius
4. Height profiles are applied based on normalized distance from center

#### Crater Profiles (8 Types)

| Profile | Description | Typical Size |
|---------|-------------|--------------|
| **SIMPLE_ROUND** | Bowl-shaped with raised rim | Small impacts (<15km) |
| **SIMPLE_FLAT** | Flat floor with raised rim | Sedimentary terrain |
| **COMPLEX_FLAT** | Central peak + flat annular floor | Large impacts |
| **COMPLEX_STEPS** | Terraced interior walls | Very large impacts |
| **COMPLEX_RINGS** | Multi-ring basin | Massive impacts |
| **DOME_VOLCANO** | Gentle Gaussian dome | Viscous lava buildup |
| **STRATO_VOLCANO** | Steep conical slopes | Explosive eruptions |
| **SHIELD_VOLCANO** | Broad, gently sloping | Fluid basaltic lava |

Each profile implements a radial height function returning values from -1.0 (crater floor) to +1.0 (rim/cone peak), scaled by depth/height multipliers.

#### Volcano Placement

Volcanoes are placed based on plate tectonics:

**Boundary-Based:**
- **Convergent (subduction zones)** → Strato volcanoes (steep, explosive)
- **Divergent (rifts)** → Shield volcanoes (broad, fluid lava)
- **Transform** → Occasional dome volcanoes

**Hotspot-Based:**
- Random placement on land using OpenSimplex2 noise
- 70% shield volcanoes, 30% dome volcanoes
- Creates mid-plate volcanic islands (e.g., Hawaiian chain)

### Biome Classification System

Biomes are classified post-generation based on elevation, climate, and rainfall.

#### Classification Factors

| Factor | Weight | Source |
|--------|--------|--------|
| Elevation | Primary | `heights[]` array |
| Climate Zone | Primary | `ClimateCalculator.ClimateZone` |
| Rainfall | Secondary | `ErosionCalculator.rainfall[]` |
| Water Proximity | Tertiary | Adjacency to negative-height polygons |

#### Biome Types (16 Categories)

**Water Bodies:**
- DEEP_OCEAN (height ≤ -3)
- OCEAN (height -2)
- COASTAL (height -1)
- FRESHWATER (adjacent to rivers/lakes)

**Cold Biomes:**
- ICE_CAP (polar + low rainfall)
- TUNDRA (polar zone)
- BOREAL_FOREST (polar + rainfall > 0.3)

**Temperate Biomes:**
- TEMPERATE_GRASSLAND (rainfall < 0.4)
- TEMPERATE_FOREST (rainfall 0.4-0.7)
- TEMPERATE_RAINFOREST (rainfall > 0.7)

**Hot Biomes:**
- DESERT (rainfall < 0.2)
- SAVANNA (tropical + rainfall 0.2-0.7)
- TROPICAL_RAINFOREST (tropical + rainfall > 0.7)

**Elevation Biomes:**
- ALPINE (height 2 + non-tropical)
- MOUNTAIN (height ≥ 3)
- WETLAND (low elevation + high rainfall + water proximity)

Each biome has `habitabilityScore` (0.0-1.0) and `agriculturalPotential` (0.0-1.0) for downstream analysis.

### City Suitability Analysis

The `CitySuitabilityAnalyzer` evaluates settlement potential as a post-generation step.

#### Scoring Factors

| Factor | Weight | Best Score |
|--------|--------|------------|
| Elevation | 0.25 | Lowlands (height 0-1) |
| Climate | 0.30 | Temperate zone |
| Coastal Proximity | 0.25 | Adjacent to ocean |
| River Proximity | 0.20 | Near rivers (not in flood zone) |

**Suitability Formula:**
```
score = (elevation × 0.25) + (climate × 0.30) + (coastal × 0.25) + (river × 0.20)
```

**Output:**
- Per-polygon suitability scores (0.0-1.0)
- Top N best locations
- Statistics (max, average suitability)
- `isSuitable()` (score > 0.5) and `isHighlySuitable()` (score > 0.8) helpers

### Integration with Accrete Physical Parameters

When generating terrain from Accrete++ simulation data, physical parameters translate to geological activity:

| Accrete Parameter | Geological Effect |
|-------------------|-------------------|
| **Surface Gravity** | Higher gravity = less dramatic mountains (compression) |
| **Surface Temperature** | Affects erosion rates and climate model |
| **Day Length** | Very slow rotation may produce "tidally locked" climate |
| **Mass** | Larger planets have more convection → more plates |
| **Hydrosphere %** | Directly sets `waterFraction` for sea level |
| **Age** | Older planets may have less tectonic activity |

**TectonicBias Calculation:**
- `TectonicBias.fromAccretePlanet()` analyzes physical parameters
- Adjusts plate count, activity level, and oceanic ratio
- Hot young planets get more plates and volcanic activity
- Cold old planets get fewer plates and gentler terrain

### Scientific Limitations

The generator simplifies complex geophysics for practical terrain generation:

1. **Static Snapshot**: Plates don't move over time; terrain represents a single moment
2. **No Mantle Dynamics**: Convection cells are implicit in plate assignment, not simulated
3. **Simplified Subduction**: Oceanic-oceanic subduction is random (not density-based)
4. **No Isostasy**: Crustal buoyancy equilibrium is not modeled
5. **Instantaneous Erosion**: Millions of years of erosion simulated in single pass
6. **No Glaciation**: Ice ages and glacial erosion not modeled separately
7. **No Ocean Currents**: Ocean heat transport and currents are not modeled
8. **Simplified Hydrology**: No groundwater, aquifers, or evapotranspiration
9. **No Crater Erosion**: Craters don't weather over time
10. **Static Volcanism**: Volcanoes don't erupt or grow; placement is static
11. **Simplified Biomes**: No microclimate variations or soil composition modeling
12. **No Population Dynamics**: City suitability is static; no growth simulation

Despite these simplifications, the generator produces visually convincing Earth-like terrain with recognizable geological features (mountain ranges, island arcs, rift valleys, craters, volcanic peaks, etc.).

---

## Origin

Ported from GDScript (Godot 3.x) to Java 25.

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
| [`HelperFunctions/DrawFunctions/draw.gd`](../../../../../../../../../../procedural/4x_planet_generator_godot-main/HelperFunctions/DrawFunctions/draw.gd) | `JavaFxPlanetMeshConverter` | Godot mesh → JavaFX TriangleMesh |
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
| `PlanetGenerator.GeneratedPlanet` | Result record with all generated data |
| `ErosionCalculator` | Erosion, rivers, rainfall (not in original) |
| `GenerationProgressListener` | Progress callbacks for UI integration |
| `TectonicBias` | Accrete physical parameter translation |
| `JavaFxPlanetMeshConverter` | JavaFX TriangleMesh conversion |
| `ProceduralPlanetPersistenceHelper` | Store/restore procedural config in ExoPlanet entities |
| Sub-seed derivation | `config.subSeed(phase)` for reproducibility |
| `fromAccreteRadius()` | Auto-select mesh resolution from Accrete data |
| Multiple climate models | HADLEY_CELLS, ICE_WORLD, TROPICAL_WORLD, TIDALLY_LOCKED, SEASONAL |
| `impact/CraterProfile` | 8 crater/volcano height profiles (radial functions) |
| `impact/CraterCalculator` | Crater/volcano placement using FastNoiseLite |
| `impact/ImpactResult` | Immutable result record for impact features |
| `biome/BiomeType` | 16 biome types with habitability scores |
| `biome/BiomeClassifier` | Biome classification from climate + rainfall + elevation |
| `analysis/CitySuitabilityAnalyzer` | Settlement suitability analysis |

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

### ✅ MEDIUM PRIORITY (Completed)

#### 6. Expand Test Coverage (Completed)
**Directory:** `src/test/java/.../procedural/`

**Test Files:**
- `TectonicBiasTest.java` (22 tests) - NEW
- `ErosionCalculatorTest.java` (25 tests) - Extended
- `AccreteBridgeIntegrationTest.java`
- `EdgeCaseTest.java`
- `ConfigInteractionTest.java`
- `ValidationTest.java`
- `ProgressListenerTest.java`

**Added Tests:**
- **TectonicBias tests (NEW):**
  - Factory presets (`earthLike()`, `marsLike()`, `venusLike()`)
  - `fromAccretePlanet()` for various planet types (Earth-like, gas giant, Super-Earth, ocean world)
  - Gravity effects on mountain height multiplier
  - `applyTo()` config modification
  - Stagnant lid vs active tectonics detection
  - Edge cases (zero gravity, extreme mass/hydrosphere)

- **Erosion tests (Extended):**
  - Frozen rivers in polar zones
  - `isRiverFrozen()` method validation
  - Precise heights correlation with integer heights
  - Rain shadow effect variance
  - Sediment mass conservation
  - River source threshold effects
  - Full pipeline integration with plate data

- **Integration tests for Accrete bridge:**
  - `PlanetGenerator.generateFromAccrete()` with various planet types
  - `PlanetGenerator.createBiasedConfig()` validation

- **Edge case tests:**
  - `waterFraction = 0.0` (desert world)
  - `waterFraction = 1.0` (ocean world)
  - `plateCount = 7` (minimum clamped) and `plateCount = 21` (maximum)
  - `Size.COLOSSAL` performance

- **Configuration interaction tests:**
  - `heightScaleMultiplier` effects
  - `erosionIterations = 0` (no erosion)
  - `rainfallScale = 2.0` (extreme rainfall)
  - `enableRivers = false`

---

#### 7. Improve Error Handling (Completed)
**Files:** `PlanetGenerator.java`

**Implemented:**
- Validation in `PlanetGenerator.generate()` for plate count vs mesh size
- Intermediate result checks:
  - Plate assignment completeness
  - Height range validity
  - Climate zone coverage
- Progress callbacks already provided via `GenerationProgressListener` (phase start/update/completion + error)

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
| PlanetGenerator | ~375 | Low | ✅ Good | — |
| IcosahedralMesh | ~370 | Medium | ✅ Good | Fixed float comparison |
| PlateAssigner | ~130 | Low | ✅ Good | — |
| BoundaryDetector | ~255 | Medium | ✅ Good | Documented |
| ElevationCalculator | ~640 | Medium | ✅ Good | Refactored |
| ClimateCalculator | ~280 | Medium | ✅ Good | 6 climate models (incl. SEASONAL) |
| ErosionCalculator | ~1050 | Medium | ✅ Good | Rain shadow + flow routing + lake fill |
| GenerationProgressListener | ~145 | Low | ✅ Good | Progress callbacks |
| TectonicBias | ~150 | Low | ✅ Good | Accrete parameter translation |
| PlanetRenderer | ~440 | Low | ✅ Good | — |
| JavaFxPlanetMeshConverter | ~1200 | Medium | ✅ Good | Consolidated |
| ProceduralPlanetPersistenceHelper | ~530 | Low | ✅ Good | Store/restore config |
| PlanetConfig | ~580 | Low | ✅ Good | Climate model + erosion + impact config |
| ProceduralPlanetViewerDialog* | ~950 | Medium | ✅ Good | Side panel, interactive regeneration |
| **Impact Features** (`impact/` subpackage) | | | | |
| CraterProfile | ~340 | Low | ✅ Good | 8 height profiles |
| CraterCalculator | ~460 | Medium | ✅ Good | Crater/volcano placement |
| ImpactResult | ~70 | Low | ✅ Good | Immutable result record |
| **Biome Classification** (`biome/` subpackage) | | | | |
| BiomeType | ~130 | Low | ✅ Good | 16 biome types |
| BiomeClassifier | ~180 | Medium | ✅ Good | Classification logic |
| **Analysis** (`analysis/` subpackage) | | | | |
| CitySuitabilityAnalyzer | ~180 | Low | ✅ Good | Settlement analysis |

*Located in `dialogs/solarsystem/` package

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

### Planetgen Features Integration

Major integration of features from the legacy `planetgen` package into the modern procedural system.

#### Impact Features System (NEW)
- **CraterProfile enum** with 8 crater/volcano height profiles
- **CraterCalculator** using FastNoiseLite Cellular noise for placement
- **ImpactResult** immutable record for results
- Polygon-based BFS traversal for radial distance measurement
- Boundary-aware volcano placement (convergent → strato, divergent → shield)
- Hotspot volcanism with OpenSimplex2 noise

#### Biome Classification System (NEW)
- **BiomeType enum** with 16 biome types
- Classification based on elevation + climate + rainfall
- Habitability and agricultural potential scores per biome
- `biomes()`, `biomeDistribution()`, `landBiomePercentages()` accessors on GeneratedPlanet

#### City Suitability Analysis (NEW)
- **CitySuitabilityAnalyzer** for post-generation settlement scoring
- Scoring weights: elevation (0.25), climate (0.30), coastal (0.25), river (0.20)
- `SuitabilityResult` record with statistics and best locations

#### Pipeline Updates
- Added IMPACT_FEATURES phase (Phase 6, between elevation and climate)
- 8 generation phases total (was 7)
- New config parameters: `craterDensity`, `craterDepthMultiplier`, `craterMaxRadius`, `enableVolcanos`, `volcanoDensity`

### Low Priority Features Completed

#### Rain Shadow Effect
- Wind direction varies by latitude (trade winds, westerlies, polar easterlies)
- `traceUpwindForMountains()` checks 5 steps upwind for blocking terrain
- Rainfall reduced up to 80% in mountain shadows
- Parallelized for large meshes

#### Multiple Climate Models
- Added `ClimateModel` enum: SIMPLE_LATITUDE, HADLEY_CELLS, ICE_WORLD, TROPICAL_WORLD, TIDALLY_LOCKED, SEASONAL
- Tidally locked model uses star-facing direction instead of latitude
- Ice world has 60%+ polar coverage
- Tropical world extends tropical zone to 45° latitude
- Seasonal model averages insolation with axial tilt

#### Continuous Heights
- Added optional continuous height field alongside the 9-band terrain
- Normalized relief range with `continuousReliefMin`/`continuousReliefMax`
- Smooth rendering uses continuous heights when enabled

#### Hydrology Basins and Flow Accumulation
- Flow accumulation computed by routing to lowest neighbor
- Basin filling raises sinks until a spill point exists
- Lakes are exposed as a mask and renderable overlay

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
- **Keyboard controls** (WASD/QE/R/SPACE) for flight simulator style navigation
- Screenshot export (PNG) via "Save" button
- Color legend overlay with elevation and boundary colors
- Plate boundary visualization (convergent=red, divergent=cyan, transform=yellow)
- Climate zone latitude rings (±30°, ±60°)
- Auto-rotate animation with "Spin" checkbox
- Axial tilt slider and pole marker overlay
- Flow-accumulation river sizing and lake visibility toggles

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
