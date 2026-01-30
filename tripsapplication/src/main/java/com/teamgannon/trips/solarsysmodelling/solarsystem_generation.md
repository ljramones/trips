# Solar System Generation (ACCRETE Model)

This document describes the planetary system generation process implemented in the `solarsysmodelling` package. The core algorithm is based on the ACCRETE model from Stephen Dole's 1969 paper and subsequent implementations.

## Package Structure

```
solarsysmodelling/
├── accrete/
│   ├── StarSystem.java          # Main simulation orchestrator
│   ├── Planet.java              # Planet physics and environment model
│   ├── SimStar.java             # Simulated star properties
│   ├── SystemObject.java        # Base class with physical constants
│   ├── Utils.java               # Random number generation, star/chemical tables
│   ├── DustRecord.java          # Protoplanetary disk dust band tracking
│   ├── PostAccretionGenerator.java # Generates rings, asteroid/Kuiper belts
│   ├── Chemical.java            # Atmospheric chemical element data
│   ├── AtmosphericChemical.java # Chemical in planet atmosphere
│   ├── PlanetTypeEnum.java      # Planet classification types
│   ├── AtmosphereTypeEnum.java  # Atmosphere breathability classification
│   ├── AccreteJ.java            # CLI entry point for testing
│   └── WDouble.java             # Wrapper for mutable double values
├── habitable/
│   ├── HabitableZoneCalculator.java  # Kopparapu HZ boundary calculations
│   ├── HabitableZoneFluxes.java      # Stellar flux coefficients for HZ
│   ├── HabitableZone.java            # HZ inner/outer radius container
│   └── HabitableZoneTypesEnum.java   # OPTIMAL vs MAX zone types
└── utils/
    ├── OrbitalDynamics.java              # Interface with gravitational constants
    ├── OrbitalTypeEnum.java              # Orbit classification (Circular, Elliptical, etc.)
    ├── OrbitalDynamicsUtils.java         # General orbital mechanics calculations
    ├── OrbitalDynamicsCircularUtils.java # Circular orbit specialization (e=0)
    ├── OrbitalDynamicsParabolicUtils.java# Parabolic trajectory calculations (e=1)
    └── OrbitalDynamicsHyperbolicUtils.java# Hyperbolic orbit calculations (e>1)
```

Related packages used by the solar system view:

```
solarsystem/animation/          # Real-time orbit animation controller
solarsystem/orbits/             # Orbit sampling providers (Kepler/Orekit seam)
dynamics/                       # Orekit-based propagators and orbit descriptors
nightsky/service/               # Orekit bootstrap + time conversions
```

## Overview

The ACCRETE algorithm simulates planetary formation through the following phases:

1. **Initialization** - Set up protoplanetary disk with dust and gas
2. **Accretion** - Planetesimals grow by sweeping up dust
3. **Coalescence** - Planetesimals collide and merge
4. **Validation** - Handle failed planetesimals and escaped moons
5. **Migration** - (Placeholder for planetary migration)
6. **Environment** - Calculate atmospheres, temperatures, habitability
7. **Post-Accretion** - Generate planetary rings, asteroid belts, and Kuiper belts

## TRIPS Extensions to Classic Accrete

TRIPS extends the original ACCRETE implementation with additional physics,
habitability models, and data consistency checks:

- **Kopparapu Habitable Zones**: Uses `HabitableZoneCalculator` in `SimStar`
  to compute OPTIMAL/MAX HZ boundaries, and sets `inOptimalHZ`/`inMaxHZ` flags.
- **HZ-Centered Ecosphere Radius**: `radiusEcosphere` is derived from the
  midpoint of the OPTIMAL zone when available (fallback to √L).
- **Expanded Planet Types**: `PlanetTypeEnum` and `AtmosphereTypeEnum` provide
  richer classifications (breathability, greenhouse, tidal lock, etc.).
- **Physics Validation**: `Planet.validatePhysics()` reconciles density,
  escape velocity, and core-radius caps to keep values self-consistent.
- **Temperature Model Guards**: explicit `albedoCooling` and greenhouse rise
  are tracked separately to prevent sign errors in temperature calculations.
- **Habitable/earthlike flags**: computed from environmental results and HZ status.
- **Orbital Dynamics Framework**: New `utils/` package with comprehensive orbital mechanics calculations.
- **Orekit Dynamics Integration**: Orekit time and propagator utilities are integrated in `dynamics/`.
- **Stellar Parameter Validation**: `StarSystem` validates and fixes invalid stellar parameters.
- **Procedural Planet Integration**: Bridge to `PlanetGenerator` for terrain generation from Accrete data.
- **Post-Accretion Structure Generation**: `PostAccretionGenerator` creates planetary rings, asteroid belts, and Kuiper belts based on the final planet configuration.

## Orbital Dynamics Framework

The `utils/` package provides a comprehensive orbital mechanics library for calculating orbital parameters, velocities, and anomalies.

### OrbitalDynamics Interface

Defines fundamental constants:

```java
public interface OrbitalDynamics {
    double G = 6.674e-11;      // Gravitational constant (N·m²/kg²)
    double AU = 1.4960E11;      // Astronomical unit (meters)
    double secsInYear = 3.558E7; // Seconds in one year
}
```

### OrbitalTypeEnum

Classifies orbits by eccentricity:

| Type | Eccentricity | Shape |
|------|--------------|-------|
| `Circular` | e = 0 | Circle |
| `Elliptical` | 0 < e < 1 | Ellipse |
| `Parabolic` | e = 1 | Parabola (escape trajectory) |
| `Hyperbolic` | e > 1 | Hyperbola (flyby trajectory) |

### OrbitalDynamicsUtils

General orbital mechanics calculations:

```java
// Escape velocity: v_esc = √(2GM/r)
double escapeVelocity(double mass, double radius);

// First cosmic velocity (orbital velocity): v = √(GM/r)
double firstCosmicVelocity(double mass, double radius);

// Orbital period for two-body system: P = 2π√(a³/G(m₁+m₂))
double orbitalPeriod(double mass1, double mass2, double semiMajorAxis);

// Eccentricity from semi-axes: e = √(1 - b²/a²)
double eccentricity(double semiMajorAxis, double semiMinorAxis);

// Periapsis distance: q = a(1-e)
double periapsisDistance(double semiMajorAxis, double eccentricity);

// Semi-latus rectum: p = a(1-e²)
double semiLatusRectum(double semiMajorAxis, double eccentricity);

// Distance from central body at angle θ: r = p/(1 + e·cos(θ))
double distanceFromCentralBody(double semiLatusRectum, double eccentricity, double angle);

// Velocity at angle θ
double velocity(double mass, double semiMajorAxis, double eccentricity, double angle);

// Periapsis velocity: vq = √((k/a)(1+e)/(1-e))
double periapsisVelocity(double mass, double semiMajorAxis, double eccentricity);

// Eccentric anomaly: cos(E) = (e + cos(θ))/(1 + e·cos(θ))
double eccentricAnomaly(double angle, double eccentricity);

// Mean anomaly: M = E - e·sin(E)
double meanAnomaly(double angle, double eccentricity);

// Classify orbit type by eccentricity
OrbitalTypeEnum whatTypeOfOrbit(double eccentricity);
```

### Specialized Orbit Calculators

**OrbitalDynamicsCircularUtils** - For circular orbits (e=0):
- Periapsis/semi-latus rectum both equal semi-major axis
- Constant orbital velocity
- Simplified calculations

**OrbitalDynamicsParabolicUtils** - For parabolic trajectories (e=1):
- Escape trajectory calculations
- Zero total orbital energy

**OrbitalDynamicsHyperbolicUtils** - For hyperbolic orbits (e>1):
- Flyby trajectory calculations
- Positive total orbital energy

## Entry Point

### StarSystem Constructor

```java
// For testing with random star
StarSystem system = new StarSystem(doMoons, verbose, extraVerbose);

// For actual star from database
StarSystem system = new StarSystem(starObject, doMoons, verbose, extraVerbose);
```

The constructor executes the full pipeline:

```java
public StarSystem(StarObject starObject, boolean doMoons, boolean verbose, boolean extraVerbose) {
    centralBody = starObject.toSimStar();
    validateAndFixStarParams(starObject.getDisplayName());  // Parameter validation
    centralBody.setAge();

    distributePlanetaryMasses();       // Phase 1-3: Accretion & coalescence
    checkPlanets();                    // Phase 4: Validation
    migratePlanets();                  // Phase 5: Migration (stub)
    setEnvironments();                 // Phase 6: Finalize planets
    generatePostAccretionStructures(); // Phase 7: Rings, asteroid belts, Kuiper belt
}
```

### Stellar Parameter Validation

`validateAndFixStarParams()` ensures the central star has valid properties for simulation:

```java
private void validateAndFixStarParams(String starName) {
    boolean needsRecalc = false;

    if (centralBody.mass <= 0) {
        log.warn("Star {} has invalid mass {}, defaulting to 1.0 solar", starName, centralBody.mass);
        centralBody.mass = 1.0;
        needsRecalc = true;
    }

    if (centralBody.luminosity <= 0) {
        log.warn("Star {} has invalid luminosity {}, defaulting to 1.0 solar", starName, centralBody.luminosity);
        centralBody.luminosity = 1.0;
        needsRecalc = true;
    }

    if (centralBody.temperature <= 0) {
        log.warn("Star {} has invalid temperature {}, defaulting to 5772 K", starName, centralBody.temperature);
        centralBody.temperature = 5772;
        needsRecalc = true;
    }

    if (centralBody.radius <= 0) {
        log.warn("Star {} has invalid radius {}, defaulting to 1.0 solar", starName, centralBody.radius);
        centralBody.radius = 1.0;
        needsRecalc = true;
    }

    if (needsRecalc) {
        centralBody.recalc();  // Recalculate dependent properties (HZ, ecosphere, etc.)
    }
}
```

**Default Values:**
| Parameter | Invalid If | Default Value |
|-----------|-----------|---------------|
| Mass | ≤ 0 | 1.0 M☉ |
| Luminosity | ≤ 0 | 1.0 L☉ |
| Temperature | ≤ 0 | 5772 K |
| Radius | ≤ 0 | 1.0 R☉ |

This prevents simulation crashes when database stars have missing or zero values.

## Phase 1: Disk Initialization

### setInitialConditions()

Creates the protoplanetary disk model:

```java
private void setInitialConditions() {
    this.dustHead = new DustRecord();
    this.dustHead.dustPresent = true;
    this.dustHead.gasPresent = true;
    this.dustHead.outerEdge = centralBody.stellarDustLimit();
    this.dustLeft = true;
    this.cloudEccentricity = 0.2;
}
```

Key parameters:
- **Dust limit**: `200.0 × M^(1/3)` AU (M = stellar mass in solar masses)
- **Inner planet limit**: `0.3 × M^(1/3)` AU
- **Outer planet limit**: `50.0 × M^(1/3)` AU

### DustRecord Structure

Dust bands are tracked as a linked list of `DustRecord` nodes:

```java
class DustRecord {
    DustRecord next = null;
    double innerEdge = 0.0;
    double outerEdge = 0.0;
    boolean dustPresent = false;
    boolean gasPresent = false;
}
```

As planetesimals accrete material, dust bands are split and marked as depleted.

## Phase 2: Dust Accretion

### distributePlanetaryMasses()

Main accretion loop that injects protoplanets and grows them:

```java
while (dustLeft) {
    // Random injection point
    sma = Utils.instance().randomNumber(innerBound, outerBound);
    ecc = Utils.instance().randomEccentricity();
    mass = PROTOPLANET_MASS;  // 1.0E-15 solar masses

    if (dustAvailable(innerEffectLimit, outerEffectLimit)) {
        // Calculate dust density at this distance
        dustDensity = DUST_DENSITY_COEFF * sqrt(stellarMass) *
                      exp(-ALPHA * pow(sma, 1.0/N));

        // Determine critical mass for gas accretion
        criticalMass = criticalMass(sma, ecc);

        // Grow the planetesimal
        mass = accreteDust(mass, dustMass, gasMass, sma, ecc,
                          criticalMass, innerBound, outerBound);

        if (mass > PROTOPLANET_MASS) {
            coalescePlanetesimals(...);
        }
    }
}
```

### Dust Density Formula

The dust surface density follows Dole's exponential model:

```
ρ_dust = A × √M_star × e^(-α × a^(1/n))
```

Where:
- `A = 2.0E-3` (DUST_DENSITY_COEFF)
- `α = 5.0` (ALPHA)
- `n = 3.0` (N)
- `a` = semi-major axis in AU

### Critical Mass

The critical mass determines when a planetesimal can accrete gas:

```java
private double criticalMass(double sma, double ecc) {
    double periapsis = sma * (1.0 - ecc);
    double temp = periapsis * sqrt(luminosity);
    return B * pow(temp, -0.75);  // B = 1.2E-5
}
```

Planetesimals exceeding critical mass become gas giants.

### Feeding Zone

A planetesimal sweeps dust from its "feeding zone":

```java
// Inner limit of effect
innerEffectLimit = sma × (1 - ecc) × (1 - mass) / (1 + cloudEccentricity)

// Outer limit of effect
outerEffectLimit = sma × (1 + ecc) × (1 + mass) / (1 - cloudEccentricity)
```

### accreteDust()

Iteratively grows the planetesimal until convergence:

```java
do {
    tempMass = newMass;
    newMass = collectDust(newMass, newDust, newGas, sma, ecc, criticalMass, dustHead);
} while (!(newMass - tempMass < 0.0001 * tempMass));
```

The `collectDust()` method calculates mass swept from each dust band.

## Phase 3: Planetesimal Coalescence

### coalescePlanetesimals()

When planetesimals have overlapping orbits, they merge:

```java
for (Planet planet : planets) {
    diff = thePlanet.sma - sma;

    // Calculate gravitational reach
    if (diff > 0.0) {
        dist1 = sma × (1 + ecc) × (1 + reducedMass) - sma;  // apoapsis
        dist2 = thePlanet.sma - periapsis_reach;
    } else {
        dist1 = sma - periapsis_reach;  // periapsis
        dist2 = thePlanet.apoapsis_reach - thePlanet.sma;
    }

    if (|diff| <= |dist1| || |diff| <= |dist2|) {
        // Collision! Merge planetesimals
        newSMA = weighted_average(thePlanet.sma, sma);
        newEcc = calculate_new_eccentricity();

        if (doMoons && mass < criticalMass && mass_in_range) {
            // Capture as moon instead of merging
            theMoon = new Planet(centralBody);
            thePlanet.getMoons().add(theMoon);
        } else {
            // Merge masses
            thePlanet.mass += mass;
            thePlanet.dustMass += dustMass;
            thePlanet.gasMass += gasMass;
        }
    }
}
```

### Moon Capture Criteria

A planetesimal becomes a moon if:
- Mass < critical mass (not a gas giant seed)
- `0.0001 < mass < 2.5` Earth masses
- Existing moon mass < 5% of planet mass

Otherwise it escapes and is reinjected later.

## Phase 4: Validation

### checkPlanets()

Cleans up the planetary system:

1. **Adjust escaped moon eccentricities**
2. **Reinject failed planetesimals** - Planetesimals that failed due to larger neighbors
3. **Reinject escaped moons** - Moons that escaped capture
4. **Check moon hierarchies** - Ensure no moon is larger than its planet
5. **Sort planets by SMA**
6. **Validate each planet** - Check mass consistency

## Phase 5: Migration

### migratePlanets()

Currently a placeholder for future planetary migration simulation:

```java
private void migratePlanets() {
    // TODO: Implement Type I/II migration
}
```

## Phase 6: Environment Calculation

### setEnvironments()

Finalizes each planet's physical properties:

```java
private void setEnvironments() {
    for (Planet p : planets) {
        p.finalize(doMoons);
        if (p.isHabitable() || p.isHabitableMoon()) {
            this.habitable = true;
        }
    }
}
```

### Planet.finalize()

The core physics calculation method (~200 lines). Key steps:

#### 1. Orbital Parameters
```java
this.orbitalZone = orbitalZone(primary.luminosity);
this.orbitalPeriod = orbitalPeriod(primary);  // Kepler's 3rd law
this.axialTilt = axialTilt();  // Random generation
this.exosphericTemperature = EARTH_EXOSPHERIC_TEMP / pow(sma / radiusEcosphere, 2.0);
```

#### 2. Radius Calculation

**Kothari Radius** (for rocky planets):
Based on Kothari (1936), calculates radius from mass and composition:

```java
public double kothariRadius() {
    // Atomic weight/number based on orbital zone and gas giant status
    // Zone 1 (inner): heavier elements
    // Zone 2 (middle): intermediate
    // Zone 3 (outer): lighter elements

    temp = (2.0 × BETA × M_sun^(1/3)) / (A1 × (Aw × An)^(1/3));
    temp2 = A2 × Aw^(4/3) × M_sun^(2/3) × mass^(2/3) / (A1 × An^2);
    radius = temp × mass^(1/3) / (1 + temp2) / CM_PER_KM;
    return radius / 1.004;  // Earth calibration factor
}
```

**Volume Radius** (for gas giants):
```java
public double volumeRadius() {
    return pow(((massInGrams / density) / PI) × (3.0/4.0), 1.0/3.0) / CM_PER_KM;
}
```

**Radius to Earth Ratio**:
```java
public double ratioRadiusToEarth() {
    return radiusInMeters() / EARTH_RADIUS;  // planet/Earth (NOT Earth/planet)
}
```

#### 3. Density

**Volume Density** (physically derived):
```java
// density = mass / volume = mass / (4/3 × π × r³)
public double volumeDensity() {
    return massInGrams() / ((4.0 * PI * pow(radius * CM_PER_KM, 3.0)) / 3.0);
}
```

**Empirical Density** (initial estimate):
```java
empiricalDensity = mass^(1/8) × sqrt(sqrt(radiusEcosphere / sma));
if (gasGiant) density *= 1.2;
else density *= 5.5;
```

#### 4. Surface Gravity

```java
// Gravitational acceleration (m/s²)
surfaceAcceleration = G × mass / r²;

// Surface gravity in Earth gravities
surfaceGravity = surfaceAcceleration / 9.80655;
```

**Gas Giant Gravity**: For gas giants, this represents the gravity at the 1-bar pressure level (cloud-top), NOT a solid surface.

#### 5. Escape Velocity

```java
// v_esc = √(2GM/R) = √(2μ/R)
public double escapeVelocity() {
    return sqrt((2.0 * mu()) / radiusInMeters());  // Returns m/s
}
```

#### 6. Day Length

Based on Dole (1964) and Goldreich & Soter (1966):

```java
// Base angular velocity
ω_base = sqrt(2 × J × mass / (k2 × r²));
// J = 1.46E-19, k2 = 0.24 (gas) or 0.33 (rock)

// Tidal braking from the star
Δω = EARTH_Δω × (ρ/ρ_Earth) × (r/r_Earth) × (1/m) × M_star² × (1/a⁶);

// Final angular velocity
ω = ω_base + Δω × age;

// Day length = 2π/ω (NOT 2π×ω!)
if (ω > 0) {
    dayLength = 2π / ω;  // seconds
} else {
    dayLength = MAX_VALUE;  // stopped rotating
}
```

**Tidal Locking**: If `dayLength >= orbitalPeriod`, the planet is tidally locked:
- For low eccentricity (e < 0.1): `dayLength = orbitalPeriod`
- For higher eccentricity: Spin-orbit resonance factor applied

#### 7. Orbital Period

Using Kepler's 3rd Law:

```java
// P = 2π × √(a³/μ)
public double orbitalPeriod(SystemObject largerBody) {
    return sqrt(pow(smaInMeters(), 3.0) / largerBody.mu()) * PI * 2.0;
}

// Gravitational parameter μ = GM
public double mu() {
    return massInKg() * G;
}
```

**Verification**: For a planet at 46.82 AU around a Sun-like star:
```
P = 2π × √((46.82 × 1.496×10¹¹)³ / (6.674×10⁻¹¹ × 1.989×10³⁰))
P ≈ 1.01×10¹⁰ seconds ≈ 117,000 days ≈ 320 years ✓
```

#### 8. Gas Giant Classification

```java
if (mass > 1 Earth && gasMass/mass > 0.05 && minMolWeight <= 4.0) {
    if (gasMass/mass < 0.20)      type = tSubSubGasGiant;  // "Semi Gas Giant"
    else if (mass < 20 Earths)    type = tSubGasGiant;     // "Sub Gas Giant"
    else                           type = tGasGiant;        // "Gas Giant"
}
```

#### 9. Atmospheric Pressure

Fogg's eq.18:
```java
pressure = volatileGasInventory × surfaceGravity ×
           (EARTH_PRESSURE / 1000.0) / (r/r_Earth)²;
```

#### 10. Temperature Calculation

**Effective Temperature** (Fogg's eq.19):
```java
T_eff = sqrt(radiusEcosphere / sma) ×
        sqrt(sqrt((1 - albedo) / (1 - EARTH_ALBEDO))) × 250K;
```

**Greenhouse Rise** (Fogg's eq.20, Hart's eq.20):
```java
convectionFactor = 0.43 × pow(pressure / EARTH_PRESSURE, 0.4);
rise = (sqrt(sqrt(1 + 0.75 × opticalDepth)) - 1) × T_eff × convectionFactor;
```

**Important**: Greenhouse effect requires atmosphere:
- `pressure > 0` is a prerequisite
- If `velocityRatio < GAS_RETENTION_THRESHOLD`, planet cannot retain atmosphere

**Albedo Cooling**: When surface temperature < equilibrium temperature (due to high albedo/cloud cover), the negative adjustment is stored in `albedoCooling` field (always ≤ 0), while `greenhouseRise` remains ≥ 0.

```java
if (netAdjustment >= 0) {
    this.greenhouseRise = netAdjustment;
    this.albedoCooling = 0.0;
} else {
    this.greenhouseRise = 0.0;
    this.albedoCooling = netAdjustment;  // Negative value
}
```

#### 11. Planet Classification

Final type assignment based on all calculated properties:

| Type | Criteria |
|------|----------|
| tGasGiant | Mass > 20 Earth, retains He, >20% gas |
| tSubGasGiant | Mass < 20 Earth, retains He, >20% gas |
| tSubSubGasGiant | Low density or 5-20% gas |
| tSuperEarth | High pressure, retains H2, rock density |
| tTerrestrial | 5-95% water coverage |
| tWater | >95% water coverage |
| tIce | >95% ice coverage or frozen atmosphere |
| tVenusian | Hot, water boiled off |
| tMartian | Thin atmosphere (< 250 mbar) |
| tTidallyLocked | Day = year or resonant |
| tRock | No significant atmosphere |
| tAsteroids | Mass < 0.001 Earth, no atmosphere |

#### 12. Habitability Assessment

```java
if (breathableAtmosphere() == BREATHABLE) {
    if (!tidallyLocked && inMaxHZ) {
        habitable = true;

        // Earthlike requires stricter criteria:
        if (inOptimalHZ &&
            0.8 <= gravity <= 1.2 &&
            |temp - 287K| <= 5K &&
            iceCover <= 0.1 &&
            0.5 <= pressure <= 2.0 bar &&
            0.4 <= cloudCover <= 0.8 &&
            0.5 <= hydrosphere <= 0.8) {
            earthlike = true;
        }
    }
}
```

### Planet Habitable Zone Fields

Each planet tracks its habitable zone status:

```java
private boolean inOptimalHZ = false;  // Conservative HZ (Runaway GH to Max GH)
private boolean inMaxHZ = false;       // Optimistic HZ (Recent Venus to Early Mars)
```

**Accessor Methods:**

```java
// Check if planet is in optimal (conservative) habitable zone
boolean isInOptimalHZ();

// Check if planet is in maximum (optimistic) habitable zone
boolean isInMaxHZ();

// Get descriptive status string
String getHabitableZoneStatus();
// Returns: "Optimal HZ", "Extended HZ", or "Outside HZ"
```

### Radiative Physics Tracking

Separates greenhouse warming from albedo cooling:

```java
private double greenhouseRise = 0.0;   // Always >= 0 (warming)
private double albedoCooling = 0.0;    // Always <= 0 (cooling)
```

**Accessor Methods:**

```java
// Net temperature adjustment (greenhouse + albedo)
double getNetRadiativeAdjustment();  // Returns greenhouseRise + albedoCooling

// Check if planet has net cooling effect
boolean hasNetCooling();  // Returns albedoCooling < 0

// Get the cooling component
double getAlbedoCooling();  // Returns value <= 0
```

**Why Separate Tracking:**
- Prevents sign errors in temperature calculations
- Allows UI to show greenhouse effect vs albedo cooling independently
- Maintains physical consistency (greenhouse can't be negative)

## Physics Validation

### validatePhysics()

Called at the end of `finalize()` to ensure all values are self-consistent:

```java
private void validatePhysics() {
    // 1. Density consistency: ρ = M / (4/3 × π × r³)
    if (radius > 0 && mass > 0) {
        double calculatedDensity = volumeDensity();
        if (abs(calculatedDensity - density) / density > 0.10) {
            density = calculatedDensity;
            // Recalculate dependent values
            surfaceAcceleration = gravitationalAcceleration();
            surfaceGravity = gravity();
        }
    }

    // 2. Escape velocity: v_esc = √(2gr)
    if (radius > 0 && surfaceGravity > 0 && surfaceGravity < MAX_VALUE) {
        double expectedVesc = sqrt(2.0 * surfaceAcceleration * radiusInMeters());
        if (abs(expectedVesc - escapeVelocity) / escapeVelocity > 0.05) {
            escapeVelocity = expectedVesc;
        }
    }

    // 3. Core radius cap: ≤ 30% for rocky, ≤ 20% for gas giants
    if (coreRadius > radius * 0.30 && !gasGiant) {
        coreRadius = radius * 0.25;
    }
    // Gas giant core cap is applied earlier in finalize()
}
```

### Common Physics Issues and Fixes

| Issue | Root Cause | Fix |
|-------|------------|-----|
| Mass 0.00 M⊕ with gravity 0.08 g | Independent calculation | Enforce `mass = ρ × (4/3)πr³` |
| Radius showing 547 R⊕ | Inverted ratio | Use `planet_radius / EARTH_RADIUS` |
| Escape velocity 943 km/s | Calculation error | Derive from `√(2GM/R)` |
| Day length astronomically long | Formula error | Use `2π/ω` not `2π×ω` |
| Gas giant gravity "--" | MAX_VALUE override | Calculate at 1-bar level |
| Negative greenhouse rise | Albedo cooling | Store in separate `albedoCooling` field |
| Core radius > total radius | No cap | Cap at 20% for gas giants |

## Habitable Zone Calculations

The `habitable` package implements the Kopparapu et al. (2013/2014) model.

### SimStar Habitable Zone Integration

`SimStar` now has four HZ boundary fields (set by `calculateHabitableZones()`):

```java
protected double hzInnerMax = 0.0;      // Recent Venus boundary (optimistic inner)
protected double hzOuterMax = 0.0;      // Early Mars boundary (optimistic outer)
protected double hzInnerOptimal = 0.0;  // Runaway Greenhouse boundary (conservative inner)
protected double hzOuterOptimal = 0.0;  // Max Greenhouse boundary (conservative outer)
```

**Accessor Methods:**

```java
// Get HZ boundaries in AU
double getHzInnerMax();     // Recent Venus
double getHzOuterMax();     // Early Mars
double getHzInnerOptimal(); // Runaway Greenhouse
double getHzOuterOptimal(); // Max Greenhouse

// Check if orbital distance is in HZ
boolean isInOptimalHZ(double semiMajorAxis);  // Conservative zone
boolean isInMaxHZ(double semiMajorAxis);       // Optimistic zone
```

**Integration in `recalc()`:**

```java
public void recalc() {
    // ... stellar property calculations ...
    calculateHabitableZones();  // Sets all four HZ boundaries
}

private void calculateHabitableZones() {
    HabitableZoneCalculator calculator = new HabitableZoneCalculator();
    Map<HabitableZoneTypesEnum, HabitableZone> zones =
        calculator.getHabitableZones(temperature, luminosity);

    HabitableZone optimal = zones.get(HabitableZoneTypesEnum.OPTIMAL);
    HabitableZone max = zones.get(HabitableZoneTypesEnum.MAX);

    hzInnerOptimal = optimal.getInnerRadius();
    hzOuterOptimal = optimal.getOuterRadius();
    hzInnerMax = max.getInnerRadius();
    hzOuterMax = max.getOuterRadius();

    // Backward compatibility: set ecosphere to center of optimal HZ
    radiusEcosphere = (hzInnerOptimal + hzOuterOptimal) / 2.0;
}
```

`SimStar.calculateHabitableZones()` uses this calculator to set:
- `hzInnerMax`/`hzOuterMax` (optimistic HZ)
- `hzInnerOptimal`/`hzOuterOptimal` (conservative HZ)
- `radiusEcosphere` as the midpoint of the optimal HZ when available

### HabitableZoneFluxes

Temperature-dependent stellar flux coefficients:

```java
// Effective stellar flux: S_eff = S_eff☉ + a×T* + b×T*² + c×T*³ + d×T*⁴
// where T* = T_eff - 5780K

double[] seffsun = {1.776, 1.107, 0.356, 0.320, 1.188, 0.99};
// Indices: [RecentVenus, RunawayGreenhouse, MaxGreenhouse, EarlyMars, ...]
```

### HabitableZone Boundaries

```java
public Map<HabitableZoneTypesEnum, HabitableZone> getHabitableZones(double teff, double luminosity) {
    fluxes.findStellarFluxes(teff);

    // Optimistic (MAX) zone: Recent Venus to Early Mars
    HabitableZone maxZone = new HabitableZone(
        sqrt(L / recentVenus),   // inner
        sqrt(L / earlyMars)      // outer
    );

    // Conservative (OPTIMAL) zone: Runaway Greenhouse to Max Greenhouse
    HabitableZone optimalZone = new HabitableZone(
        sqrt(L / runawayGreenhouse),  // inner
        sqrt(L / maxGreenhouse)       // outer
    );

    return zones;
}
```

### Zone Boundaries for Sun-like Star (5780K)

| Boundary | S_eff | Distance (AU) |
|----------|-------|---------------|
| Recent Venus | 1.776 | 0.75 |
| Runaway Greenhouse | 1.107 | 0.95 |
| Maximum Greenhouse | 0.356 | 1.67 |
| Early Mars | 0.320 | 1.77 |

## UI Display (PlanetTab.java)

### Unit Conversions

| Property | Internal Unit | Display Unit | Conversion |
|----------|---------------|--------------|------------|
| Mass | Solar masses | M⊕ or M♃ | `× SUN_MASS / EARTH_MASS` |
| Radius | km | R⊕ or R♃ | `/ 6371` or `/ 69911` |
| Escape velocity | m/s | km/s | `/ 1000` |
| Day length | seconds | hours or days | `/ 3600` or `/ 86400` |
| Orbital period | seconds | days or years | `/ 86400` or `/ 31557600` |
| Pressure | millibars | atm | `/ 1013.25` |
| Temperature | Kelvin | K and °C | `- 273.15` for °C |

### Display Logic

**Mass**: Show in Jupiter masses for large planets (≥ 0.1 M♃):
```java
if (massJupiter >= 0.1) {
    "1.23 M♃ (391 M⊕)"
} else {
    "0.85 M⊕"
}
```

**Radius**: Show in Jupiter radii for large planets (≥ 0.5 R♃):
```java
if (radiusJupiter >= 0.5) {
    "0.98 R♃ (10.97 R⊕)"
} else {
    "1.05 R⊕ (6,690 km)"
}
```

**Day Length**: Show in days for very long periods (> 1000 hours):
```java
if (dayLengthHrs > 1000) {
    "115.74 days (2,778 hrs)"
} else {
    "24.00 hrs"
}
```

**Orbital Period**: Show in years for long periods (≥ 1 year):
```java
if (orbitalPeriodYears >= 1.0) {
    "11.86 yrs (4,333 days)"
} else {
    "224.7 days"
}
```

### Status Tags

| Tag | Color | Condition |
|-----|-------|-----------|
| Planet Type | Blue (#1976D2) | Always shown |
| Optimal HZ | Dark Green (#2E7D32) | `inOptimalHZ` |
| Extended HZ | Light Green (#66BB6A) | `inMaxHZ && !inOptimalHZ` |
| Outside HZ | Gray (#f5f5f5) | `!inMaxHZ` |
| Habitable | Green (#4CAF50) | `habitable` |
| Earthlike | Green (#4CAF50) | `earthlike` |
| Tidally Locked | Orange (#FF9800) | `resonantPeriod` |
| Greenhouse | Red (#F44336) | `greenhouseEffect && pressure > 0` |

**Important**: Greenhouse tag only shows when `surfacePressure > 0.001` (actual atmosphere present).

## Phase 7: Post-Accretion Structure Generation

After all planets are finalized, the system generates secondary structures that don't form through direct accretion: planetary rings, asteroid belts, and Kuiper belts.

### generatePostAccretionStructures()

Orchestrates post-accretion structure generation:

```java
private void generatePostAccretionStructures() {
    postAccretionGenerator = new PostAccretionGenerator(centralBody, Utils.instance().getSeed());
    postAccretionGenerator.generate(planets);

    asteroidBelt = postAccretionGenerator.getAsteroidBelt();
    kuiperBelt = postAccretionGenerator.getKuiperBelt();

    // Log results
    if (asteroidBelt != null) {
        log.info("System has asteroid belt: {} - {} AU",
                asteroidBelt.getInnerRadiusAU(), asteroidBelt.getOuterRadiusAU());
    }
    if (kuiperBelt != null) {
        log.info("System has Kuiper belt: {} - {} AU",
                kuiperBelt.getInnerRadiusAU(), kuiperBelt.getOuterRadiusAU());
    }
}
```

### PostAccretionGenerator

The `PostAccretionGenerator` class analyzes the final planet configuration and creates structures based on physical principles.

#### Asteroid Belt Generation

Asteroid belts form where a massive planet (gas giant) gravitationally prevented planetesimal accretion. The algorithm:

1. **Find the innermost giant planet** - Any planet with mass ≥ 10 Earth masses or flagged as gas giant
2. **Calculate the resonance zone** - The region at 35-65% of the giant's semi-major axis (roughly the 3:1 resonance)
3. **Check if region is clear** - No existing planet's orbit overlaps with the belt zone
4. **Scale by stellar luminosity** - More luminous stars have wider planet spacing

```java
private void generateAsteroidBelt(List<Planet> planets, Planet innermostGiant) {
    double giantSma = innermostGiant.getSma();
    double luminosityFactor = sqrt(centralBody.luminosity);

    // Belt region (roughly 3:1 resonance with giant)
    double beltInner = giantSma * 0.35;  // ASTEROID_BELT_GAP_RATIO_MIN
    double beltOuter = giantSma * 0.65;  // ASTEROID_BELT_GAP_RATIO_MAX

    // Check no planet exists in this region
    boolean regionClear = true;
    for (Planet p : planets) {
        double periapsis = p.getSma() * (1 - p.getEccentricity());
        double apoapsis = p.getSma() * (1 + p.getEccentricity());
        if (periapsis < beltOuter && apoapsis > beltInner) {
            regionClear = false;
            break;
        }
    }

    if (regionClear && beltInner > 0.3 * luminosityFactor) {
        asteroidBelt = new AsteroidBeltData(
            "Main Asteroid Belt",
            beltInner, beltOuter,
            estimateAsteroidBeltMass(beltInner, beltOuter),
            8.0 + random.nextDouble() * 4.0,    // Inclination 8-12°
            0.05 + random.nextDouble() * 0.06   // Eccentricity 0.05-0.11
        );
    }
}
```

**Real-World Comparison**: In our solar system, Jupiter is at 5.2 AU, and the asteroid belt spans 2.1-3.3 AU (40-63% of Jupiter's orbit), matching this model.

#### Kuiper Belt Generation

The Kuiper belt forms from primordial icy material beyond the outermost giant planet:

1. **Find the outermost giant planet** - Any planet with mass ≥ 10 Earth masses
2. **Check minimum distance** - The outer giant must be at least 5 AU out (scaled by √luminosity)
3. **Calculate belt region** - Extends from 1.0× to 1.7× the outer giant's orbit
4. **Allow small bodies** - Dwarf planets (< 0.01 Earth masses) don't block belt formation

```java
private void generateKuiperBelt(List<Planet> planets, Planet outermostGiant) {
    double giantSma = outermostGiant.getSma();
    double luminosityFactor = sqrt(centralBody.luminosity);
    double minSma = 5.0 * luminosityFactor;  // MIN_GIANT_SMA_FOR_KUIPER

    if (giantSma < minSma) {
        return;  // Giant too close for Kuiper belt
    }

    double kuiperInner = giantSma * 1.0;   // KUIPER_BELT_INNER_RATIO
    double kuiperOuter = giantSma * 1.7;   // KUIPER_BELT_OUTER_RATIO

    // Check region (allow small dwarf planets)
    boolean regionClear = true;
    for (Planet p : planets) {
        if (p.getSma() > kuiperInner && p.getSma() < kuiperOuter) {
            if (p.massInEarthMasses() > 0.01) {
                regionClear = false;
                break;
            }
        }
    }

    if (regionClear) {
        kuiperBelt = new KuiperBeltData(
            "Kuiper Belt",
            kuiperInner, kuiperOuter,
            estimateKuiperBeltMass(kuiperInner, kuiperOuter),
            10.0 + random.nextDouble() * 10.0,  // Inclination 10-20°
            0.05 + random.nextDouble() * 0.1    // Eccentricity 0.05-0.15
        );
    }
}
```

**Real-World Comparison**: Neptune is at 30 AU, and the Kuiper belt spans 30-50 AU (1.0-1.7× Neptune's orbit).

#### Planetary Ring Generation

Rings form through several mechanisms: tidal disruption of moons, captured material, or primordial debris inside the Roche limit. The algorithm assigns rings probabilistically based on planet properties:

**Ring Probability by Planet Type:**

| Planet Type | Mass Range | Ring Probability |
|-------------|------------|------------------|
| Gas Giant | ≥ 50 M⊕ | 85% |
| Ice Giant | 10-50 M⊕ | 60% |
| Terrestrial | < 10 M⊕ | 2% |

**Ring Type Selection:**

For gas giants (≥ 50 M⊕):
- **Saturn-like** (25%): Bright, icy, prominent rings
- **Uranus-like** (35%): Dark, narrow ring bands
- **Neptune-like** (25%): Faint, dusty rings
- **Jupiter-like** (15%): Very faint dust rings

For ice giants (10-50 M⊕):
- **Uranus-like** (50%): Dark, narrow rings
- **Neptune-like** (35%): Faint rings
- **Saturn-like** (15%): Rare bright rings

```java
private void generatePlanetaryRing(Planet planet) {
    double mass = planet.massInEarthMasses();
    double ringProbability = mass >= 50 ? 0.85 : mass >= 10 ? 0.60 : 0.02;

    if (random.nextDouble() >= ringProbability) {
        return;  // No ring
    }

    RingType ringType = determineRingType(planet);
    double planetRadiusAU = planet.getRadius() / SystemObject.KM_PER_AU;
    double rocheLimit = calculateRocheLimit(planet, planetRadiusAU);

    // Ring dimensions based on type
    double innerRadius = planetRadiusAU * ringType.innerRadiusFactor;
    double outerRadius = min(rocheLimit, planetRadiusAU * ringType.outerRadiusFactor);

    if (outerRadius > innerRadius) {
        planet.setRingType(ringType.name());
        planet.setRingInnerRadiusAU(innerRadius);
        planet.setRingOuterRadiusAU(outerRadius);
    }
}
```

**Ring Type Dimensions:**

| Ring Type | Inner Radius | Outer Radius | Visual Appearance |
|-----------|--------------|--------------|-------------------|
| SATURN | 1.2 × R | 2.5 × R | Bright, icy, prominent |
| URANUS | 1.5 × R | 2.0 × R | Dark, narrow bands |
| NEPTUNE | 1.3 × R | 2.3 × R | Faint, dusty |
| JUPITER | 1.3 × R | 1.8 × R | Very faint dust |

**Roche Limit Calculation:**

The Roche limit determines the maximum stable ring radius:

```java
private double calculateRocheLimit(Planet planet, double planetRadiusAU) {
    // Roche limit = 2.44 × R_planet × (ρ_planet / ρ_ring)^(1/3)
    double ringDensity = 0.9;  // Icy material (g/cm³)
    double planetDensity = planet.getDensity() > 0 ? planet.getDensity() : 1.3;
    return 2.44 * planetRadiusAU * pow(planetDensity / ringDensity, 1.0/3.0);
}
```

### Planet Ring Fields

The `Planet` class stores ring data:

```java
// Ring system properties (set by PostAccretionGenerator)
private String ringType = null;           // "SATURN", "URANUS", "NEPTUNE", "JUPITER"
private double ringInnerRadiusAU = 0.0;   // Inner ring radius in AU
private double ringOuterRadiusAU = 0.0;   // Outer ring radius in AU
```

### Belt Data Classes

**AsteroidBeltData:**
```java
public static class AsteroidBeltData {
    private final String name;
    private final double innerRadiusAU;
    private final double outerRadiusAU;
    private final double massEarthMasses;
    private final double inclinationDeg;
    private final double eccentricity;
}
```

**KuiperBeltData:**
```java
public static class KuiperBeltData {
    private final String name;
    private final double innerRadiusAU;
    private final double outerRadiusAU;
    private final double massEarthMasses;
    private final double inclinationDeg;
    private final double eccentricity;
}
```

### Mass Estimation

**Asteroid Belt Mass:**
```java
// Real main belt is ~0.0005 Earth masses
private double estimateAsteroidBeltMass(double innerAU, double outerAU) {
    double width = outerAU - innerAU;
    double baseMass = 0.0005;
    return baseMass * (width / 1.2) * (0.5 + random.nextDouble());
}
```

**Kuiper Belt Mass:**
```java
// Real Kuiper belt is estimated at ~0.1 Earth masses
private double estimateKuiperBeltMass(double innerAU, double outerAU) {
    double width = outerAU - innerAU;
    double baseMass = 0.1;
    return baseMass * (width / 20.0) * (0.5 + random.nextDouble());
}
```

### Persistence via SolarSystemService

The generated structures are persisted to the database:

```java
// Save planets and post-accretion structures
solarSystemService.saveGeneratedSystem(sourceStar, starSystem);

// Internally calls:
// 1. saveGeneratedPlanets() - Converts Planet → ExoPlanet with ring data
// 2. saveGeneratedBelts() - Creates SolarSystemFeature entries for belts
```

**AccretePlanetConverter** transfers ring properties:
```java
private static void populateRingProperties(ExoPlanet exoPlanet, Planet planet) {
    String ringType = planet.getRingType();
    if (ringType != null && !ringType.isEmpty()) {
        exoPlanet.setRingType(ringType);
        exoPlanet.setRingInnerRadiusAU(planet.getRingInnerRadiusAU());
        exoPlanet.setRingOuterRadiusAU(planet.getRingOuterRadiusAU());
    }
}
```

**Belt features** are stored as `SolarSystemFeature` entities with types `ASTEROID_BELT` and `KUIPER_BELT`.

### Checking Generated Structures

```java
StarSystem system = new StarSystem(starObject, true, false, false);

// Check for belts
if (system.hasAsteroidBelt()) {
    AsteroidBeltData belt = system.getAsteroidBelt();
    System.out.println("Asteroid belt: " + belt.getInnerRadiusAU() +
                       " - " + belt.getOuterRadiusAU() + " AU");
}

if (system.hasKuiperBelt()) {
    KuiperBeltData belt = system.getKuiperBelt();
    System.out.println("Kuiper belt: " + belt.getInnerRadiusAU() +
                       " - " + belt.getOuterRadiusAU() + " AU");
}

// Check for planetary rings
for (Planet p : system.getPlanets()) {
    if (p.getRingType() != null) {
        System.out.println(p.getPlanetType() + " at " + p.getSma() +
                          " AU has " + p.getRingType() + " rings");
    }
}
```

### Number Formatting

```java
private String checkValue(double value) {
    if (value > 10e10 || isNaN || isInfinite) return "--";
    if (value >= 1_000_000) return "1.23e+06";      // Scientific
    if (value >= 1000)      return "12,345";         // Comma-separated
    if (value >= 1)         return "12.34";          // 2 decimals
    if (value >= 0.0001)    return "0.0012";         // 4 decimals
    if (value > 0)          return "1.23e-05";       // Scientific
    return "0";
}
```

## Physical Constants

Key constants from `SystemObject.java`:

| Constant | Value | Description |
|----------|-------|-------------|
| G | 6.67408E-11 | Gravitational constant (m³/kg/s²) |
| SUN_MASS | 1.989E30 kg | Solar mass |
| EARTH_MASS | 5.97237E24 kg | Earth mass |
| EARTH_RADIUS | 6.371E6 m | Earth radius |
| JUPITER_MASS | 1.8982E27 kg | Jupiter mass |
| JUPITER_RADIUS | 6.9911E7 m | Jupiter radius |
| KM_PER_AU | 149,597,870.7 | AU in km |
| MOLAR_GAS_CONST | 8.3144621 J/mol/K | Molar gas constant |
| EARTH_ACCELERATION | 9.80655 m/s² | Earth surface gravity |
| EARTH_DENSITY | 5.514 g/cc | Earth mean density |
| FREEZING_POINT_OF_WATER | 273.15 K | |
| EARTH_AVERAGE_KELVIN | 287.15 K | Earth mean temperature |
| EARTH_SURF_PRES | 1013.25 mbar | Earth surface pressure |
| GAS_RETENTION_THRESHOLD | 6.0 | v_esc/v_rms ratio for gas retention |

## Stellar Data

The `Utils` class loads stellar properties from CSV files:

```
planetsim/MV_Stars.csv   # M-type main sequence (75.1% of MS)
planetsim/KV_Stars.csv   # K-type main sequence (13.6% of MS)
planetsim/GV_Stars.csv   # G-type main sequence (7.3% of MS)
planetsim/FV_Stars.csv   # F-type main sequence (3.1% of MS)
planetsim/AV_Stars.csv   # A-type main sequence (0.9% of MS)
planetsim/B_Stars.csv    # B-type stars
planetsim/O_Stars.csv    # O-type stars
planetsim/WR_Stars.csv   # Wolf-Rayet stars
planetsim/C_Stars.csv    # Carbon stars
planetsim/S_Stars.csv    # S-type stars
planetsim/White_Dwarves.csv
planetsim/Giants.csv
```

CSV format: `type,mass,luminosity,radius,temperature,?,magnitude,?,?,red,green,blue`

### Random Star Selection

Weighted by stellar population:
- 90.7% main sequence → 75.1% M, 13.6% K, 7.3% G, 3.1% F, 0.9% A
- 6.2% white dwarfs
- 2.9% giants
- 0.2% other (B, O, Wolf-Rayet, Carbon, S-type)

## Usage Example

```java
// Create star system from StarObject
StarObject star = starRepository.findByName("Alpha Centauri A");
StarSystem system = new StarSystem(star, true, false, false);

// Check results
System.out.println("Planets: " + system.getPlanets().size());
System.out.println("Habitable: " + system.isHabitable());

for (Planet p : system.getPlanets()) {
    System.out.println(p.datasheet());
}
```

## Accrete-to-Procedural Integration

The Accrete simulation output can be used to generate detailed 3D terrain via the
`procedural` package. This creates a bridge from physical simulation to visual rendering.

### Direct Generation

```java
import com.teamgannon.trips.planetarymodelling.procedural.PlanetGenerator;

// Generate procedural terrain directly from Accrete planet
Planet accretePlanet = system.getPlanets().get(0);
long seed = accretePlanet.hashCode();  // Or use orbital hash for reproducibility

PlanetGenerator.GeneratedPlanet terrain =
    PlanetGenerator.generateFromAccrete(accretePlanet, seed);

// Render the terrain
Shape planetShape = terrain.createShape();
```

### TectonicBias Translation

`TectonicBias` translates Accrete physical parameters to procedural config:

| Accrete Parameter | Procedural Effect |
|-------------------|-------------------|
| `planet.getRadius()` | Mesh resolution (larger = more polygons) |
| `planet.getHydrosphere()` | Water fraction (sea level) |
| `planet.getMass()` | Plate count (more mass = more plates) |
| `planet.getSurfaceGravity()` | Height scale (higher gravity = flatter terrain) |
| `planet.getDayLength()` | Climate model (slow rotation = tidally locked) |
| `planet.isGasGiant()` | Minimal terrain (no solid surface) |

```java
// Manual config with TectonicBias
TectonicBias bias = TectonicBias.fromAccretePlanet(accretePlanet);

PlanetConfig config = PlanetConfig.builder()
    .seed(seed)
    .fromAccreteRadius(accretePlanet.getRadius())
    .waterFraction(accretePlanet.getHydrosphere() / 100.0)
    .build();

PlanetConfig biasedConfig = bias.applyTo(config, seed);
PlanetGenerator.GeneratedPlanet terrain = PlanetGenerator.generate(biasedConfig);
```

### Parameter Mapping Details

**Plate Count from Mass:**
```java
if (mass < 0.3) plateCount = 5;       // Small rocky body
else if (mass < 0.7) plateCount = 8;  // Mars-like
else if (mass < 1.5) plateCount = 12; // Earth-like
else if (mass < 3.0) plateCount = 16; // Super-Earth
else plateCount = 10;                  // Large planet (convection limited)
```

**Height Scale from Gravity:**
```java
heightMultiplier = 1.0 / surfaceGravity;  // High gravity = lower mountains
```

**Gas Giant Handling:**
```java
if (isGasGiant) {
    enableActiveTectonics = false;
    plateCount = 1;
    heightScaleMultiplier = 0.2;  // Minimal terrain
}
```

### Persistence with ExoPlanet

For database storage, use `ProceduralPlanetPersistenceHelper`:

```java
import com.teamgannon.trips.planetarymodelling.procedural.ProceduralPlanetPersistenceHelper;

// Store procedural config in ExoPlanet entity
ProceduralPlanetPersistenceHelper.populateProceduralMetadata(
    exoPlanet,      // Target entity
    config,         // PlanetConfig used
    seed,           // Generation seed
    terrain,        // GeneratedPlanet result
    "accrete"       // Source label
);

// Later, restore config from stored data
PlanetConfig restored = ProceduralPlanetPersistenceHelper.buildConfigFromSnapshots(exoPlanet);
PlanetGenerator.GeneratedPlanet regenerated = PlanetGenerator.generate(restored);
```

**Stored Fields in ExoPlanet:**
- `proceduralSeed` - Generation seed
- `proceduralGeneratorVersion` - Version for migration
- `proceduralAccreteSnapshot` - JSON of physical parameters
- `proceduralOverrides` - JSON of config overrides
- `proceduralPreview` - PNG preview image (256×256)

## Orbital Animation System

The `solarsystem/animation` package provides real-time orbital animation for the solar system visualization, with physically accurate Keplerian motion.

### Package Structure

```
solarsystem/
├── animation/
│   ├── AnimationTimeModel.java        # Simulation time tracking with speed control
│   └── OrbitalAnimationController.java # Animation loop and position updates
├── orbits/
│   ├── OrbitSamplingProvider.java     # Interface for orbit calculations
│   ├── KeplerOrbitSamplingProvider.java # Keplerian position calculations
│   └── OrbitSamplingProviders.java    # Factory for orbit providers
└── rendering/
    └── SolarSystemRenderer.java       # 3D rendering (includes updatePlanetPositions)
```

### Architecture

The animation system uses a decoupled event-driven architecture:

```
SimulationControlPane (UI)
        │
        │ publishes SolarSystemAnimationEvent
        ▼
SolarSystemSpacePane (@EventListener)
        │
        │ controls
        ▼
OrbitalAnimationController
        │
        ├── AnimationTimeModel (time tracking)
        ├── KeplerOrbitSamplingProvider (position math)
        └── SolarSystemRenderer.updatePlanetPositions()
```

### AnimationTimeModel

Tracks simulation time with configurable speed multiplier:

```java
public class AnimationTimeModel {
    private Instant epoch;           // Simulation start (default: J2000)
    private Instant simulationTime;  // Current simulation time
    private double speedMultiplier;  // 1.0 = real-time, 86400 = 1 day/sec

    // Called each frame by AnimationTimer
    public void update(long nowNanos) {
        long deltaNanos = nowNanos - lastUpdateNanos;
        double deltaSeconds = deltaNanos / 1_000_000_000.0;
        double simDeltaSeconds = deltaSeconds * speedMultiplier;
        simulationTime = simulationTime.plusMillis((long)(simDeltaSeconds * 1000));
    }

    // Elapsed time since epoch in days (for orbital calculations)
    public double getElapsedDays() {
        return (simulationTime.toEpochMilli() - epoch.toEpochMilli()) / (1000.0 * 60 * 60 * 24);
    }
}
```

### OrbitalAnimationController

Manages the JavaFX `AnimationTimer` and coordinates position updates:

```java
public class OrbitalAnimationController {
    private final AnimationTimer timer;
    private final AnimationTimeModel timeModel;
    private final OrbitSamplingProvider orbitSampler;

    private void updateFrame(long nowNanos) {
        timeModel.update(nowNanos);
        double elapsedDays = timeModel.getElapsedDays();

        Map<String, double[]> newPositions = new HashMap<>();
        for (PlanetDescription planet : planets) {
            double trueAnomaly = calculateTrueAnomaly(planet, elapsedDays);
            double[] posAu = orbitSampler.calculatePositionAu(
                planet.getSemiMajorAxis(),
                planet.getEccentricity(),
                planet.getInclination(),
                planet.getLongitudeOfAscendingNode(),
                planet.getArgumentOfPeriapsis(),
                trueAnomaly
            );
            newPositions.put(planet.getName(), posAu);
        }

        renderer.updatePlanetPositions(newPositions);
        onUpdate.run();  // Updates labels
    }
}
```

### Kepler's Equation Solver

The animation calculates true anomaly from elapsed time using Newton-Raphson iteration:

```java
private double calculateTrueAnomaly(PlanetDescription planet, double elapsedDays) {
    double period = planet.getOrbitalPeriod(); // days
    if (period <= 0) {
        // Kepler's 3rd law fallback: P² = a³ (solar mass, years/AU)
        period = Math.pow(planet.getSemiMajorAxis(), 1.5) * 365.25;
    }

    // Mean anomaly: M = n × t (degrees)
    double meanMotion = 360.0 / period;
    double meanAnomaly = (meanMotion * elapsedDays) % 360.0;

    // Solve Kepler's equation: M = E - e×sin(E)
    double E = Math.toRadians(meanAnomaly);
    for (int i = 0; i < 10; i++) {
        double deltaE = (E - e * Math.sin(E) - M) / (1 - e * Math.cos(E));
        E -= deltaE;
        if (Math.abs(deltaE) < 1e-10) break;
    }

    // Eccentric anomaly to true anomaly
    double trueAnomaly = Math.atan2(
        Math.sqrt(1 - e*e) * Math.sin(E),
        Math.cos(E) - e
    );
    return Math.toDegrees(trueAnomaly);
}
```

### Event-Driven Control

Animation is controlled via Spring events from `SimulationControlPane`:

```java
// SimulationControlPane publishes events
playPauseButton.setOnAction(e -> {
    eventPublisher.publishEvent(new SolarSystemAnimationEvent(
        this, SolarSystemAnimationEvent.AnimationAction.TOGGLE_PLAY_PAUSE));
});

// SolarSystemSpacePane listens for events
@EventListener
public void onSolarSystemAnimationEvent(SolarSystemAnimationEvent event) {
    switch (event.getAction()) {
        case PLAY -> animationController.play();
        case PAUSE -> animationController.pause();
        case TOGGLE_PLAY_PAUSE -> animationController.togglePlayPause();
        case RESET -> animationController.stop();
        case SET_SPEED -> animationController.setSpeed(event.getSpeedMultiplier());
    }
}
```

### SolarSystemAnimationEvent

Event class for animation control:

```java
public class SolarSystemAnimationEvent extends ApplicationEvent {
    public enum AnimationAction {
        PLAY, PAUSE, TOGGLE_PLAY_PAUSE, RESET, SET_SPEED
    }

    private final AnimationAction action;
    private final double speedMultiplier;  // For SET_SPEED action
}
```

### Speed Multipliers

| UI Label | Speed Multiplier | Effect |
|----------|------------------|--------|
| 0.1x | 8,640 | ~2.4 hours/sec |
| 1.0x | 86,400 | 1 day/sec |
| 10.0x | 864,000 | 10 days/sec |

At 1 day/sec, Earth would complete one orbit in ~6 minutes of wall-clock time.

### Integration with Renderer

The renderer's `updatePlanetPositions()` method applies new positions:

```java
public void updatePlanetPositions(Map<String, double[]> positionsAu) {
    for (Map.Entry<String, double[]> entry : positionsAu.entrySet()) {
        Sphere sphere = planetNodes.get(entry.getKey());
        if (sphere != null) {
            double[] screen = scaleManager.auVectorToScreen(
                posAu[0], posAu[1], posAu[2]);
            sphere.setTranslateX(screen[0]);
            sphere.setTranslateY(screen[1]);
            sphere.setTranslateZ(screen[2]);
        }
    }
}
```

### Lifecycle Management

Animation is initialized when entering a solar system and cleaned up when leaving:

```java
// In SolarSystemSpacePane
private void initializeAnimation(SolarSystemDescription system) {
    animationController = new OrbitalAnimationController(renderer, this::updateLabels);
    animationController.setPlanets(system.getPlanetDescriptionList());
}

private void cleanupAnimation() {
    if (animationController != null) {
        animationController.dispose();
        animationController = null;
    }
}
```

### Orekit Dynamics Integration

Orekit is integrated for high-precision propagation and time handling, while
the solar system view keeps a clean seam for swapping orbit samplers:

- `dynamics/DynamicsCalculator` wraps Orekit `KeplerianPropagator` for orbit state updates.
- `nightsky/service/OrekitBootstrapService` initializes Orekit data at startup.
- `nightsky/service/TimeService` converts between Java time and Orekit `AbsoluteDate`.
- `solarsystem/orbits/OrbitSamplingProvider` is the plug point; current default uses
  `KeplerOrbitSamplingProvider`, and an Orekit-backed provider can be wired in without
  touching renderers.

### Future Enhancements

- **Orekit Perturbations**: Add J2, third-body, and SRP perturbation models to the
  Orekit propagator path.
- **Moon Animation**: Animate moons orbiting their parent planets
- **Orbital Trails**: Show fading paths behind moving planets
- **Time Scrubber**: Slider to jump to specific dates
- **Camera Follow**: Track a specific planet during animation

## References

1. Dole, S. H. (1969). "Computer Simulation of the Formation of Planetary Systems". *Icarus*, 13(3), 494-508.
2. Dole, S. H. (1964). *Habitable Planets for Man*. Blaisdell Publishing Company.
3. Kothari, D. S. (1936). "The Internal Constitution of the Planets". *MNRAS*, 96, 833-843.
4. Hart, M. H. (1978). "The Evolution of the Atmosphere of the Earth". *Icarus*, 33(1), 23-39.
5. Goldreich, P. & Soter, S. (1966). "Q in the Solar System". *Icarus*, 5(1-6), 375-389.
6. Kopparapu, R. K. et al. (2013). "Habitable Zones around Main-sequence Stars". *ApJ*, 765(2), 131.
7. Kopparapu, R. K. et al. (2014). "Habitable Zones around Main-sequence Stars: Dependence on Planetary Mass". *ApJ Letters*, 787(2), L29.
8. Fogg, M. J. (1985). "Extra-solar Planetary Systems: A Microcomputer Simulation". *JBIS*, 38, 501-514.

## Changelog

### Version 2.3 (January 2026)

**New: Post-Accretion Structure Generation** (`PostAccretionGenerator.java`):

Automatic generation of secondary structures after planet formation:

- **Planetary Rings**:
  - Probabilistic ring assignment based on planet mass
  - Gas giants (≥50 M⊕): 85% probability
  - Ice giants (10-50 M⊕): 60% probability
  - Four ring types: SATURN (bright, icy), URANUS (dark, narrow), NEPTUNE (faint), JUPITER (very faint)
  - Ring dimensions based on Roche limit calculation
  - Added `ringType`, `ringInnerRadiusAU`, `ringOuterRadiusAU` fields to `Planet`

- **Asteroid Belt**:
  - Forms in resonance zone (35-65% of innermost giant's orbit)
  - Only generated if region is clear of planets
  - Scaled by stellar luminosity
  - Typical inclination 8-12°, eccentricity 0.05-0.11
  - Mass estimate based on region width

- **Kuiper Belt**:
  - Forms beyond outermost giant (1.0-1.7× giant's orbit)
  - Requires outer giant at ≥5 AU (luminosity-scaled)
  - Allows dwarf planets (<0.01 M⊕) in belt region
  - Typical inclination 10-20°, eccentricity 0.05-0.15
  - Mass estimate ~0.1 M⊕ baseline

**Integration Points**:
- `StarSystem` now calls `generatePostAccretionStructures()` after `setEnvironments()`
- `StarSystem` has `getAsteroidBelt()`, `getKuiperBelt()`, `hasAsteroidBelt()`, `hasKuiperBelt()` accessors
- `AccretePlanetConverter.populateRingProperties()` transfers ring data to `ExoPlanet`
- `SolarSystemService.saveGeneratedSystem()` persists planets and belts together
- `SolarSystemService.saveGeneratedBelts()` creates `SolarSystemFeature` entries for belts

**UI Display**:
- Planetary rings rendered via `RingFieldRenderer` in solar system view
- Asteroid/Kuiper belts rendered as particle fields
- Display toggles: "Show Planetary Rings", "Show Asteroid Belt", "Show Kuiper Belt"

---

### Version 2.2 (January 2026)

**New: Real-Time Orbital Animation** (`solarsystem/animation/` package):
- `AnimationTimeModel` - Simulation time tracking with configurable speed multiplier
- `OrbitalAnimationController` - JavaFX AnimationTimer-based animation loop
- Kepler's equation solver using Newton-Raphson iteration for true anomaly
- Event-driven control via `SolarSystemAnimationEvent`
- Integration with `SimulationControlPane` play/pause/reset/speed controls
- Automatic cleanup when leaving solar system view

**Animation Features**:
- Speed range: 0.1x to 10x (8,640 to 864,000 seconds per wall-clock second)
- Default speed: 1 day per second (Earth orbit in ~6 minutes)
- Physically accurate Keplerian orbital motion
- Labels track planet positions during animation
- Support for eccentric orbits with proper anomaly calculations

---

### Version 2.1 (January 2026)

**New: Orbital Dynamics Framework** (`utils/` package):
- `OrbitalDynamics` interface with gravitational constants
- `OrbitalTypeEnum` for orbit classification (Circular, Elliptical, Parabolic, Hyperbolic)
- `OrbitalDynamicsUtils` - General orbital mechanics (escape velocity, orbital period, eccentricity, anomalies)
- `OrbitalDynamicsCircularUtils` - Specialized circular orbit calculations
- `OrbitalDynamicsParabolicUtils` - Escape trajectory calculations
- `OrbitalDynamicsHyperbolicUtils` - Flyby trajectory calculations

**Enhanced SimStar Habitable Zone Integration**:
- Added four HZ boundary fields: `hzInnerMax`, `hzOuterMax`, `hzInnerOptimal`, `hzOuterOptimal`
- New `calculateHabitableZones()` method using Kopparapu model
- HZ checking methods: `isInOptimalHZ()`, `isInMaxHZ()`
- Updated `toString()` to include HZ information

**Enhanced Planet Habitable Zone Tracking**:
- Added `inOptimalHZ` and `inMaxHZ` boolean fields
- New accessor methods: `isInOptimalHZ()`, `isInMaxHZ()`, `getHabitableZoneStatus()`
- Radiative physics separation: `greenhouseRise` vs `albedoCooling`
- New methods: `getNetRadiativeAdjustment()`, `hasNetCooling()`, `getAlbedoCooling()`

**StarSystem Parameter Validation**:
- Added `validateAndFixStarParams()` method
- Validates mass, luminosity, temperature, radius
- Applies sensible defaults for invalid values
- Logs warnings for corrected parameters

**Accrete-to-Procedural Integration**:
- `PlanetGenerator.generateFromAccrete()` for direct terrain generation
- `TectonicBias.fromAccretePlanet()` translates physical parameters
- `ProceduralPlanetPersistenceHelper` for storing/restoring configs
- Parameter mapping: mass→plates, gravity→height, hydrosphere→water

---

### Version 2.0 (2026)

**Physics Fixes**:
- Fixed day length formula: `2π/ω` not `2π×ω` (Planet.java:698)
- Fixed `ratioRadiusToEarth()`: `planet/Earth` not `Earth/planet` (Planet.java:510)
- Fixed gas giant gravity: Removed erroneous `MAX_VALUE` override (Planet.java:229)
- Added core radius cap (20%) for gas giants (Planet.java:236-239)
- Added `albedoCooling` field for negative greenhouse values (Planet.java:44)
- Added `validatePhysics()` method for consistency checks (Planet.java:389-427)
- Fixed ice/hydrosphere consistency for frozen worlds (Planet.java:1175-1185)
- Integrated Kopparapu habitable zone calculations (SimStar.java:84-118)

**UI Improvements** (PlanetTab.java):
- Greenhouse tag now requires `surfacePressure > 0.001` (line 154)
- Long day lengths show in both days and hours (lines 248-252)
- Tidally locked planets have styled day length display (lines 255-259)
- Orbital periods ≥1 year show in both years and days (lines 267-271)
- Mass shows in M♃ for large planets (lines 196-200)
- Radius shows in R♃ for large planets (lines 229-233)
- Gas giant gravity labeled "Cloud-top gravity" (line 226)
- Duplicate Gas Giant status tag removed (line 138)
- Number formatting with commas for values <1,000,000 (lines 431-433)

**Documentation**:
- Created comprehensive `solarsystem_generation.md`
- Added physics validation section
- Added UI display logic documentation
- Added common issues and fixes table
