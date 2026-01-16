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
    └── (reserved for future utilities)
```

## Overview

The ACCRETE algorithm simulates planetary formation through the following phases:

1. **Initialization** - Set up protoplanetary disk with dust and gas
2. **Accretion** - Planetesimals grow by sweeping up dust
3. **Coalescence** - Planetesimals collide and merge
4. **Validation** - Handle failed planetesimals and escaped moons
5. **Migration** - (Placeholder for planetary migration)
6. **Environment** - Calculate atmospheres, temperatures, habitability

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
    centralBody.setAge();

    distributePlanetaryMasses();  // Phase 1-3: Accretion & coalescence
    checkPlanets();               // Phase 4: Validation
    migratePlanets();             // Phase 5: Migration (stub)
    setEnvironments();            // Phase 6: Finalize planets
}
```

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
