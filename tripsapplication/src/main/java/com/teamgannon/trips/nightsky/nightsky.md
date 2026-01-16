# Night Sky System

The night sky system renders a realistic star-filled sky as seen from the surface of any planet in the TRIPS database. It handles coordinate transformations, magnitude calculations, atmospheric effects, and efficient star queries.

## Package Structure

```
nightsky/
├── bridge/
│   ├── NightSkyFrameBridge.java    # Coordinate frame translation (AU to ly)
│   └── PlanetarySkyBridgeService.java  # Main orchestrator service
├── math/
│   ├── NightSkyMath.java           # Core horizon coordinate transformations
│   ├── HorizonBasis.java           # East/North/Up basis vectors
│   ├── PlanetRotationModel.java    # Planet rotation parameters
│   ├── ObserverLocation.java       # Lat/lon on planet surface
│   ├── AltAz.java                  # Altitude-azimuth coordinates
│   ├── EquatorialCoordinates.java  # RA/Dec coordinates
│   └── AstroTime.java              # Julian date, sidereal time utilities
├── model/
│   ├── PlanetarySkyModel.java      # Output model with visible stars
│   ├── VisibleStarResult.java      # Single visible star data
│   ├── StarRenderRow.java          # Lightweight star for rendering
│   ├── AtmosphereModel.java        # Atmospheric extinction parameters
│   ├── LevelOfDetail.java          # LOD settings (star count, mag limit)
│   ├── NightSkyRequest.java        # Request parameters
│   ├── NightSkyResult.java         # Full result with metadata
│   └── SkyStarPoint.java           # Star point in sky coordinates
└── service/
    ├── StarQueryService.java       # Spatial star queries
    ├── PhotometryService.java      # Magnitude and color calculations
    ├── NightSkyCacheService.java   # Result caching
    ├── SkyTransformService.java    # Coordinate transformations
    ├── TimeService.java            # Time-related utilities
    └── EphemerisService.java       # Orbital position calculations
```

## How It Works

### 1. Entry Point: PlanetarySkyBridgeService

The `PlanetarySkyBridgeService` is the main orchestrator that coordinates all sky computations. It is called from `PlanetarySpacePane.recomputeSky()`.

```java
PlanetarySkyModel model = skyBridgeService.computeSky(currentContext, datasetName);
```

### 2. Observer Position Calculation

The observer's position in 3D space (light years from Sol) is computed from:
- Host star position (from `StarObject.x, y, z`)
- Planet's orbital offset (semi-major axis in AU, converted to light years)

```java
// From NightSkyFrameBridge
Vector3D observerPosition = NightSkyFrameBridge.observerPositionLyFromAu(hostStar, offsetAu);
```

The conversion factor is: **1 AU = 1/63241.077 light years**

### 3. Horizon Basis Computation

The `NightSkyMath.computeHorizonBasis()` method calculates the local East-North-Up (ENU) coordinate frame for the observer based on:

- **Planet rotation model**: Obliquity (axial tilt), rotation period, prime meridian position
- **Observer location**: Latitude and longitude on planet surface
- **Time**: Seconds since epoch (local time * 3600)

The horizon basis consists of three orthonormal vectors:
- **East**: Points east along the horizon
- **North**: Points north along the horizon
- **Up**: Points toward zenith (local vertical)

### 4. Star Visibility Calculation

For each star in the database:

1. **Calculate direction vector** from observer to star
2. **Transform to horizon coordinates** using `NightSkyMath.toHorizonCoords()`
3. **Compute altitude and azimuth**:
   - Altitude: angle above horizon (0° = horizon, 90° = zenith)
   - Azimuth: compass direction (0° = North, 90° = East)
4. **Filter stars** below horizon (altitude < 0°)
5. **Apply magnitude limit** to filter dim stars

### 5. Day/Night Detection

The host star's altitude determines day/night:

```java
double hostStarAltitude = computeHostStarHorizon(context, observerPosition);
boolean isDay = hostStarAltitude > 0.0;
```

- **Daytime** (host star above horizon): Only show stars brighter than magnitude -3.0
- **Nighttime** (host star below horizon): Use user-configured magnitude limit (default 6.0)

### 6. Magnitude Selection

Stars have multiple magnitude fields in the database. The system uses a fallback chain:

1. `magv` (V-band visual magnitude) - preferred
2. `grp` (Gaia G-band red)
3. `bpg` (Gaia BP-G color)
4. `bprp` (Gaia BP-RP color)
5. `magb`, `magr`, `magi`, `magu` (other bands)
6. `apparentMagnitude` (string field)
7. `absoluteMagnitude` (string field)
8. Default: 10.0 (dim)

### 7. Atmospheric Effects

When enabled, the `PhotometryService` applies atmospheric extinction near the horizon:

```java
// Kasten & Young (1989) airmass formula
double airmass = 1.0 / (cos(zenithAngle) + 0.50572 * pow(96.07995 - zenithAngle, -1.6364));
double extinction = extinctionCoefficient * airmass;  // magnitudes
```

This makes stars appear dimmer when viewed through more atmosphere (near horizon).

## Coordinate Systems

### World Coordinates (Light Years)
- **Origin**: Sol (Earth's sun)
- **Axes**: Right-handed Cartesian (X, Y, Z)
- All star positions (`StarObject.x, y, z`) use this frame

### Horizon Coordinates (Observer Frame)
- **Altitude**: Angle above horizon in degrees (-90 to +90)
- **Azimuth**: Compass direction in degrees (0-360, 0=North, 90=East)

### Conversion Flow
```
Star World Position (ly)
       ↓
   - Observer Position (ly)
       ↓
Direction Vector (normalized)
       ↓
   NightSkyMath.toHorizonCoords()
       ↓
Horizon Vector (East, North, Up)
       ↓
   altitudeDeg(), azimuthDeg()
       ↓
Alt/Az for sky dome placement
```

## Key Classes

### PlanetaryContext
Input data container with:
- `planet`: The ExoPlanet being viewed from
- `hostStar`: The star the planet orbits
- `localTime`: Time of day (0-24 hours)
- `magnitudeLimit`: Faintest stars to show
- `viewingAzimuth`, `viewingAltitude`: Camera direction
- `showAtmosphereEffects`: Enable/disable extinction

### PlanetarySkyModel
Output data container with:
- `visibleStars`: List of all visible stars with positions
- `topBrightest`: Top 20 brightest stars
- `hostStarAltitudeDeg`: Sun's altitude (for day/night)
- `effectiveMagnitudeLimit`: Actual magnitude cutoff used

### VisibleStarResult
Per-star result:
- `star`: Reference to StarObject
- `altitudeDeg`, `azimuthDeg`: Position in sky
- `magnitude`: Apparent magnitude
- `distanceLy`: Distance from observer

## Rendering Pipeline

```
PlanetarySpacePane.setContext()
         ↓
   recomputeSky()
         ↓
PlanetarySkyBridgeService.computeSky()
   ├── computeObserverPositionLy()
   ├── computeHostStarHorizon()  → day/night
   ├── queryAllStars()
   ├── NightSkyMath.computeHorizonBasis()
   └── transformAndFilter()  → List<VisibleStarResult>
         ↓
   PlanetarySkyModel
         ↓
PlanetarySkyRenderer.render()
   ├── renderGroundMask()
   ├── renderHostStar()
   └── renderStars()  → 3D spheres on sky dome
```

## Performance Considerations

### Caching
`NightSkyCacheService` caches computed results with:
- 5-minute TTL
- Max 100 entries
- Key: planet + time (rounded to minute) + position + LOD

### Level of Detail
The `LevelOfDetail` enum controls trade-offs:

| LOD    | Max Stars | Mag Limit |
|--------|-----------|-----------|
| ULTRA  | 100,000   | 10.0      |
| HIGH   | 50,000    | 8.0       |
| MEDIUM | 20,000    | 6.5       |
| LOW    | 5,000     | 5.0       |

### Current Implementation Note
The current implementation loads ALL stars from the database (matching legacy behavior for correctness). Future optimization could use spatial queries via `StarQueryService.queryStarsInRadius()`.

## Usage Example

```java
// Build context
PlanetaryContext context = PlanetaryContext.builder()
    .planet(exoPlanet)
    .hostStar(starRecord)
    .system(solarSystemDescription)
    .localTime(22.0)  // 10 PM
    .magnitudeLimit(6.0)
    .showAtmosphereEffects(true)
    .build();

// Compute sky
PlanetarySkyModel model = bridgeService.computeSky(context, datasetName);

// Access results
List<VisibleStarResult> stars = model.getVisibleStars();
boolean isDay = model.getHostStarAltitudeDeg() > 0;
```

## Mathematical Foundation

### Horizon Basis (ENU Frame)

Given planet rotation parameters and observer location:

1. Apply obliquity rotation to get spin axis
2. Apply planetary rotation based on time
3. Compute local vertical (up) from lat/lon
4. East = up × spinAxis (normalized)
5. North = east × up (normalized)

### Horizon Transformation

For a direction vector `d` in world coordinates:
```
east  = d · eastUnit
north = d · northUnit
up    = d · upUnit
altitude = arcsin(up)
azimuth  = atan2(east, north)
```

### Atmospheric Extinction

Magnitude adjustment for atmosphere:
```
m_observed = m_true + k × X
```
Where:
- `k` = extinction coefficient (0.2 mag/airmass for clear sky)
- `X` = airmass ≈ sec(zenith angle) with Kasten-Young correction

## Related Files

- `PlanetarySpacePane.java` - UI component that hosts the sky view
- `PlanetarySkyRenderer.java` - 3D rendering of stars on sky dome
- `PlanetaryContext.java` - Input context data container
