# Solar System View - Technical Documentation

This document describes the solar system visualization subsystem in TRIPS. It covers the rendering pipeline, planetary ring support, system features (asteroid belts, stations, etc.), and animation capabilities.

> **Related Documentation**: For the procedural solar system generation model (ACCRETE), orbital dynamics theory, and habitable zone calculations, see [`../solarsysmodelling/solarsystem_generation.md`](../solarsysmodelling/solarsystem_generation.md).

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Rendering Pipeline](#rendering-pipeline)
3. [Scale Management](#scale-management)
4. [Orbital Visualization](#orbital-visualization)
5. [Planetary Rings](#planetary-rings)
6. [Solar System Features](#solar-system-features)
7. [Animation System](#animation-system)
8. [Context Menus and Interaction](#context-menus-and-interaction)
9. [Side Panel and Controls](#side-panel-and-controls)

---

## Architecture Overview

The solar system view is activated when users select "Jump Into..." from a star's context menu in the interstellar view. The system uses a layered architecture:

```
┌─────────────────────────────────────────────────────────────────┐
│                     SolarSystemSpacePane                        │
│  (JavaFX Pane - hosts SubScene with 3D content)                │
├─────────────────────────────────────────────────────────────────┤
│                     SolarSystemRenderer                         │
│  (Orchestrates all rendering - stars, planets, orbits, etc.)   │
├──────────────────┬──────────────────┬───────────────────────────┤
│  ScaleManager    │  OrbitVisualizer │  GridAndZoneRenderer      │
│  (AU ↔ Screen)   │  (Keplerian      │  (Scale grid, habitable   │
│                  │   orbit paths)   │   zone ring)              │
├──────────────────┴──────────────────┴───────────────────────────┤
│                     SolarSystemService                          │
│  (Data access - loads planets, features from database)         │
├─────────────────────────────────────────────────────────────────┤
│  ExoPlanetRepository  │  SolarSystemFeatureRepository           │
│  (Planet persistence) │  (Feature persistence)                  │
└─────────────────────────────────────────────────────────────────┘
```

### Key Components

| Component | Location | Purpose |
|-----------|----------|---------|
| `SolarSystemSpacePane` | `graphics/panes/` | Main 3D viewport for solar system view |
| `SolarSystemRenderer` | `solarsystem/rendering/` | Creates all visual elements |
| `ScaleManager` | `solarsystem/rendering/` | Coordinate conversion (AU ↔ screen) |
| `OrbitVisualizer` | `solarsystem/rendering/` | Generates orbital ellipse geometry |
| `SolarSystemService` | `service/` | Database access and business logic |
| `SolarSystemDescription` | `planetarymodelling/` | Display model aggregate |

---

## Rendering Pipeline

When a solar system is displayed, the following sequence occurs:

```
User clicks "Jump Into..." on star
        │
        ▼
SolarSystemSpacePane.setSystemToDisplay(StarDisplayRecord)
        │
        ▼
SolarSystemService.getSolarSystem(starRecord)
  ├─> Query SolarSystemRepository for existing system
  ├─> Load ExoPlanet entities via ExoPlanetRepository
  ├─> Load SolarSystemFeature entities via FeatureRepository
  ├─> Convert to description objects (PlanetDescription, FeatureDescription)
  └─> Return SolarSystemDescription with all data
        │
        ▼
SolarSystemRenderer.render(SolarSystemDescription)
  ├─> Clear previous content
  ├─> ScaleManager.setSystemExtent() - configure scaling
  ├─> renderScaleGrid() - distance reference circles
  ├─> renderHabitableZone() - green translucent ring
  ├─> renderCentralStar() - primary star sphere
  ├─> renderCompanionStars() - secondary stars if binary/trinary
  ├─> For each planet:
  │     ├─> createOrbitPath() - orbital ellipse
  │     ├─> calculateOrbitalPosition() - current position
  │     ├─> Create planet sphere with material
  │     ├─> renderPlanetRing() - if planet has rings
  │     └─> Add to planetNodes map for animation
  └─> renderFeatures() - asteroid belts, stations, etc.
        │
        ▼
Group added to SolarSystemSpacePane.systemEntityGroup
```

### Display Model Pattern

The rendering system uses a display model pattern to decouple persistence from rendering:

- **Entity** (`ExoPlanet`, `SolarSystemFeature`) → JPA persistence
- **Description** (`PlanetDescription`, `FeatureDescription`) → Rendering data
- **Converter** (`PlanetDescriptionConverter`, `FeatureDescriptionConverter`) → Transformation

This allows the renderer to work with simple POJOs while the service layer handles database concerns.

---

## Scale Management

The `ScaleManager` class handles coordinate conversion between astronomical units (AU) and screen coordinates.

### Scale Modes

**Linear Scale** (default for compact systems):
```java
screenCoord = au * baseScale * zoomLevel
```

**Logarithmic Scale** (auto-enabled when orbit ratio > 20x):
```java
screenCoord = log10(1 + au * 10) * baseScale * zoomLevel * 30
```

### Radial Scaling (Critical)

Non-linear scaling must be applied to radial distance, not per-axis:

```java
// CORRECT - scale radial distance, preserve direction
private double[] toScreen(double xAu, double yAu, double zAu) {
    double r = Math.sqrt(xAu * xAu + yAu * yAu + zAu * zAu);
    if (r == 0) return new double[]{0, 0, 0};

    double scaledR = scaleManager.auToScreen(r);
    double factor = scaledR / r;
    return new double[]{xAu * factor, yAu * factor, zAu * factor};
}
```

This preserves orbit shapes when using logarithmic compression.

### Smart Angular Distribution

Planets with similar orbits (within 15%) are placed 180° apart:

```java
if (prevSma > 0 && Math.abs(currSma - prevSma) / prevSma < 0.15) {
    baseAngle = angles[i - 1] + 180;  // Opposite side
}
```

---

## Orbital Visualization

The `OrbitVisualizer` class creates 3D orbital ellipses from Keplerian elements.

### Keplerian Elements

| Parameter | Symbol | Description |
|-----------|--------|-------------|
| Semi-major axis | a | Half the longest diameter (AU) |
| Eccentricity | e | Shape: 0=circle, 0-1=ellipse |
| Inclination | i | Tilt from reference plane (degrees) |
| Longitude of ascending node | Ω | Where orbit crosses upward (degrees) |
| Argument of periapsis | ω | Angle from ascending node to perihelion (degrees) |

### Rotation Sequence

Transforms are applied in this order (first-to-last in JavaFX):
1. Argument of periapsis (ω) - rotate around Z
2. Inclination (i) - rotate around X
3. Longitude of ascending node (Ω) - rotate around Z

```java
orbitGroup.getTransforms().addAll(rotateArgPeri, rotateInclination, rotateLAN);
```

### Position Calculation

Planet position at true anomaly θ:

```java
double r = a * (1 - e*e) / (1 + e * cos(θ));
double x = r * cos(θ);
double y = r * sin(θ);
// Then apply 3D rotations...
```

---

## Planetary Rings

Ring support was added to the `ExoPlanet` entity to persist ring data with planets.

### Ring Types

| Type | Description |
|------|-------------|
| `SATURN` | Broad, bright, multi-banded rings |
| `URANUS` | Thin, dark rings with gaps |
| `NEPTUNE` | Faint, dusty ring arcs |
| `CUSTOM` | User-defined parameters |

### ExoPlanet Ring Fields

```java
// In ExoPlanet.java
private Boolean hasRings;
private String ringType;           // SATURN, URANUS, NEPTUNE, CUSTOM
private Double ringInnerRadiusAU;  // Inner edge (planetary radii or AU)
private Double ringOuterRadiusAU;  // Outer edge
private Double ringThickness;      // Vertical thickness
private Double ringInclination;    // Tilt from equatorial plane
private String ringPrimaryColor;   // Main ring color (#RRGGBB)
private String ringSecondaryColor; // Secondary/gap color
```

### Ring Rendering

Rings are rendered using the `RingFieldRenderer` class:

```java
// In SolarSystemRenderer.renderPlanetRing()
if (planet.isHasRings()) {
    RingConfiguration config = getRingConfiguration(planet.getRingType());
    // Apply custom parameters if CUSTOM type
    RingFieldRenderer renderer = new RingFieldRenderer(config);
    Group ringGroup = renderer.render();
    ringGroup.setTranslateX(position[0]);
    ringGroup.setTranslateY(position[1]);
    ringGroup.setTranslateZ(position[2]);
    planetGroup.getChildren().add(ringGroup);
}
```

### Editing Rings

The `PlanetPropertiesDialog` includes a Ring System tab:

- **Has Rings** checkbox enables/disables ring fields
- **Ring Type** dropdown selects preset or CUSTOM
- Custom fields: inner radius, outer radius, thickness, inclination, colors
- Changes trigger re-rendering of the solar system

---

## Solar System Features

Features are system-level structures beyond individual planets: asteroid belts, stations, jump gates, etc.

### Feature Types

**Natural Features** (NATURAL category):
- `ASTEROID_BELT` - Rocky debris belt (like our Main Belt)
- `KUIPER_BELT` - Icy bodies beyond giant planets
- `DEBRIS_DISK` - Dust and debris from collisions
- `OORT_CLOUD` - Distant spherical shell of icy bodies
- `ZODIACAL_DUST` - Inner system dust cloud
- `TROJAN_CLUSTER` - Bodies at Lagrange points

**Artificial Features** (ARTIFICIAL category):
- `ORBITAL_HABITAT` - Space stations, O'Neill cylinders
- `JUMP_GATE` - FTL transit infrastructure
- `SHIPYARD` - Ship construction facility
- `RESEARCH_STATION` - Scientific outpost
- `MINING_OPERATION` - Resource extraction
- `DYSON_SWARM` - Energy collection array
- `DEFENSE_PERIMETER` - Military installations
- `SENSOR_NETWORK` - Detection/communication grid

### Feature Entity

```java
@Entity(name = "SOLAR_SYSTEM_FEATURE")
public class SolarSystemFeature {
    private String id;                  // UUID
    private String solarSystemId;       // FK to SolarSystem
    private String name;
    private String featureType;         // From FeatureType constants
    private String featureCategory;     // NATURAL or ARTIFICIAL

    // Belt/disk spatial properties
    private Double innerRadiusAU;
    private Double outerRadiusAU;
    private Double thickness;
    private Double inclinationDeg;
    private Double eccentricity;

    // Point feature spatial properties
    private Double orbitalRadiusAU;
    private Double orbitalAngleDeg;
    private Double orbitalHeightAU;
    private String associatedPlanetId;  // For Trojans, moons
    private String lagrangePoint;       // L1-L5

    // Visual properties
    private Integer particleCount;
    private Double minParticleSize;
    private Double maxParticleSize;
    private String primaryColor;
    private String secondaryColor;
    private Double opacity;
    private Boolean animated;
    private Double animationSpeed;

    // Sci-fi properties
    private String controllingPolity;
    private Long population;
    private String purpose;
    private Integer techLevel;
    private Integer yearEstablished;
    private String status;              // ACTIVE, ABANDONED, etc.
    private Integer strategicImportance;
    private Integer defensiveRating;
    private String productionCapacity;
    private String transitDestinations; // For jump gates
    private String notes;

    // Hazard properties
    private Boolean navigationHazard;
    private String hazardType;
    private Integer hazardSeverity;
}
```

### Feature Rendering

Features are classified as belt-type or point-type:

**Belt Features** (rendered as particle rings):
```java
private void renderBeltFeature(FeatureDescription feature) {
    RingConfiguration config = new RingConfiguration();
    config.setInnerRadius(scaleManager.auToScreen(feature.getInnerRadiusAU()));
    config.setOuterRadius(scaleManager.auToScreen(feature.getOuterRadiusAU()));
    config.setParticleCount(feature.getParticleCount());
    // ... set colors, opacity

    RingFieldRenderer renderer = new RingFieldRenderer(config);
    Group belt = renderer.render();
    featuresGroup.getChildren().add(belt);
}
```

**Point Features** (rendered as icons/shapes):
```java
private void renderPointFeature(FeatureDescription feature) {
    // Calculate position from orbital parameters
    double angle = Math.toRadians(feature.getOrbitalAngleDeg());
    double r = scaleManager.auToScreen(feature.getOrbitalRadiusAU());
    double x = r * Math.cos(angle);
    double z = r * Math.sin(angle);

    // Create appropriate 3D shape based on type
    Node shape = createFeatureShape(feature);
    shape.setTranslateX(x);
    shape.setTranslateZ(z);
    featuresGroup.getChildren().add(shape);
}
```

### Service Methods

```java
// In SolarSystemService
// CRUD operations
SolarSystemFeature addFeature(SolarSystemFeature feature);
SolarSystemFeature updateFeature(SolarSystemFeature feature);
void deleteFeature(String featureId);

// Queries
List<SolarSystemFeature> findFeaturesBySolarSystem(String solarSystemId);
List<SolarSystemFeature> findBeltFeatures(String solarSystemId);
List<SolarSystemFeature> findJumpGates(String solarSystemId);
List<SolarSystemFeature> findNavigationHazards(String solarSystemId);

// Convenience factory methods
SolarSystemFeature createAsteroidBelt(String solarSystemId, String name,
    double innerAU, double outerAU);
SolarSystemFeature createJumpGate(String solarSystemId, String name,
    double orbitalRadiusAU, String destinations);
SolarSystemFeature createOrbitalHabitat(String solarSystemId, String name,
    double orbitalRadiusAU, String polity, long population);
```

---

## Animation System

The animation system allows orbital motion simulation.

### Components

| Class | Purpose |
|-------|---------|
| `AnimationTimeModel` | Tracks simulation time and time acceleration |
| `OrbitalAnimationController` | Coordinates planet position updates |
| `SimulationControlPane` | UI controls (play/pause, speed, time display) |

### Animation Flow

```
SimulationControlPane
  ├─> Play button → OrbitalAnimationController.start()
  ├─> Speed slider → AnimationTimeModel.setTimeAcceleration()
  └─> Time display ← AnimationTimeModel.getCurrentTime()
        │
        ▼
OrbitalAnimationController (JavaFX AnimationTimer)
  ├─> Each frame: timeModel.advance(deltaTime)
  ├─> For each planet: calculate new true anomaly
  ├─> OrbitVisualizer.calculateOrbitalPosition(newAnomaly)
  └─> Update planet node positions
```

### Orbit Sampling

The `orbits/` package provides orbital position calculation:

- `OrbitSamplingProvider` - Interface for position calculation
- `KeplerOrbitSamplingProvider` - Keplerian (2-body) solution
- Future: Orekit-based N-body propagation

---

## Context Menus and Interaction

Right-click context menus provide editing capabilities.

### Handler Interface

```java
public interface SolarSystemContextMenuHandler {
    void onPlanetContextMenu(PlanetDescription planet, ContextMenuEvent event);
    void onStarContextMenu(StarDisplayRecord star, ContextMenuEvent event);
    void onOrbitContextMenu(PlanetDescription planet, ContextMenuEvent event);
    void onBackgroundContextMenu(ContextMenuEvent event);
}
```

### Menu Factory

`SolarSystemContextMenuFactory` creates context menus:

**Planet Menu**:
- Properties... → `PlanetPropertiesDialog`
- Edit Orbit... → Same dialog, orbit tab focused
- Delete Planet... → Confirmation, then delete

**Star Menu**:
- Star Properties... → Star info display
- Return to Interstellar View → Fire `ContextSelectorEvent`

### Edit Workflow

```
Right-click planet → Context menu
        │
        ▼
Select "Properties..." → PlanetPropertiesDialog.showAndWait()
        │
        ▼
User edits values → OK button
        │
        ▼
PlanetEditResult returned
  ├─> orbitalChanged: true → Full re-render
  ├─> hasRingsChanged: true → Re-render rings
  └─> physicalChanged: true → Update tooltip
        │
        ▼
SolarSystemService.updateExoPlanet() → Persist changes
        │
        ▼
refreshCurrentSystem() → Redraw visualization
```

---

## Side Panel and Controls

The `SolarSystemSidePane` provides information and controls.

### Panes

| Pane | Purpose |
|------|---------|
| `SystemOverviewPane` | Star type, planet count, habitable zone info |
| `SolarSystemPlanetListPane` | Sortable table of planets |
| `SolarSystemObjectPropertiesPane` | Selected object details |
| `SimulationControlPane` | Animation play/pause, speed |
| `ReferenceCueControlPane` | Toggle grid, labels, orbits |

### Reference Cues

Users can toggle visual elements:
- Scale grid circles
- Distance labels
- Orbit paths
- Planet labels
- Habitable zone ring
- Feature visibility

---

## File Organization

```
solarsystem/
├── SOLAR_SYSTEM_VIEW.md          # This documentation
├── SolarSystemContextMenuFactory.java
├── SolarSystemContextMenuHandler.java
├── SolarSystemGenOptions.java
├── SolarSystemGenerationDialog.java
├── SolarSystemReport.java
├── SolarSystemReportDialog.java
├── SolarSystemSaveResult.java
├── PlanetDialog.java
├── PlanetTab.java
├── animation/
│   ├── AnimationTimeModel.java
│   └── OrbitalAnimationController.java
├── orbits/
│   ├── OrbitSamplingProvider.java
│   ├── KeplerOrbitSamplingProvider.java
│   └── OrbitSamplingProviders.java
├── rendering/
│   ├── SolarSystemRenderer.java      # Main orchestrator
│   ├── ScaleManager.java             # AU ↔ screen conversion
│   ├── OrbitVisualizer.java          # Orbital ellipse creation
│   ├── GridAndZoneRenderer.java      # Scale grid, HZ ring
│   ├── OrbitMarkerRenderer.java      # Aphelion/perihelion markers
│   └── SelectionStyleManager.java    # Selection highlighting
└── sol/
    ├── SolSolarSystem.java           # Sol system preset data
    ├── SolSolarSystemPlanetModel.java
    └── SolSolarSystemCometModel.java
```

---

## See Also

- [`../solarsysmodelling/solarsystem_generation.md`](../solarsysmodelling/solarsystem_generation.md) - ACCRETE model, orbital dynamics theory
- [`../../jpa/model/ExoPlanet.java`](../../jpa/model/ExoPlanet.java) - Planet persistence entity
- [`../../jpa/model/SolarSystemFeature.java`](../../jpa/model/SolarSystemFeature.java) - Feature persistence entity
- [`../../service/SolarSystemService.java`](../../service/SolarSystemService.java) - Data access service
- [`../../graphics/panes/SolarSystemSpacePane.java`](../../graphics/panes/SolarSystemSpacePane.java) - 3D viewport
