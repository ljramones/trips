# Particle Fields - Technical Documentation

This document describes the particle field rendering system used to visualize distributed structures like planetary rings, asteroid belts, debris disks, nebulae, and accretion disks.

> **Related Documentation**:
> - [Solar System View](../solarsystem/SOLAR_SYSTEM_VIEW.md) - How particle fields integrate with solar system rendering
> - [Solar System Generation](../solarsysmodelling/solarsystem_generation.md) - ACCRETE model for procedural system generation

---

## Table of Contents

1. [Overview](#overview)
2. [Structure Types](#structure-types)
3. [Architecture](#architecture)
4. [Configuration System](#configuration-system)
5. [Generators](#generators)
6. [Rendering Pipeline](#rendering-pipeline)
7. [Scale Adapters](#scale-adapters)
8. [Animation](#animation)
9. [Presets](#presets)

---

## Overview

The particle field system renders thousands of particles in 3D space with realistic orbital mechanics. Each particle follows Keplerian orbital motion with configurable:

- **Orbital elements**: Semi-major axis, eccentricity, inclination, argument of periapsis, longitude of ascending node
- **Visual properties**: Size, color (with gradients)
- **Motion**: Angular velocity with proper Keplerian speed (inner particles orbit faster)

The system is used for:
- **Planetary rings** around gas giants in solar system view
- **Asteroid belts** (Main Belt, Kuiper Belt) as solar system features
- **Debris disks** for young stellar systems
- **Nebulae** in interstellar view
- **Accretion disks** around compact objects (black holes, neutron stars)

---

## Structure Types

The `RingType` enum defines five categories of particle structures:

| Type | Characteristics | Examples |
|------|-----------------|----------|
| `PLANETARY_RING` | Extremely thin, dense, nearly circular orbits, fast rotation | Saturn's rings, Uranus rings |
| `ASTEROID_BELT` | Thick vertical distribution, sparse, eccentric orbits, large rocky bodies | Main Belt, Kuiper Belt |
| `DEBRIS_DISK` | Moderate thickness, mix of dust and planetesimals, may show density waves | Protoplanetary disks, collision remnants |
| `DUST_CLOUD` | 3D spherical distribution, very diffuse, slow turbulent motion | Emission nebulae, dark nebulae |
| `ACCRETION_DISK` | Thin, extremely fast rotation, temperature gradient (hot inner edge) | Black hole accretion, neutron star accretion |

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                      RingFieldWindow                            │
│            (Standalone visualization window)                    │
├─────────────────────────────────────────────────────────────────┤
│                      RingFieldRenderer                          │
│  (Core renderer - can be embedded in any JavaFX 3D scene)      │
├──────────────────┬──────────────────────────────────────────────┤
│ RingFieldFactory │              RingConfiguration               │
│ (Presets)        │              (Parameters)                    │
├──────────────────┴──────────────────────────────────────────────┤
│                    RingElementGenerator                         │
│  (Interface - type-specific particle generation)               │
├────────────┬────────────┬────────────┬────────────┬────────────┤
│ Planetary  │ Asteroid   │ Debris     │ DustCloud  │ Accretion  │
│ Ring       │ Belt       │ Disk       │ Generator  │ Disk       │
│ Generator  │ Generator  │ Generator  │            │ Generator  │
└────────────┴────────────┴────────────┴────────────┴────────────┘
```

### Key Classes

| Class | Purpose |
|-------|---------|
| `RingFieldRenderer` | Core rendering engine - generates meshes from particles |
| `RingConfiguration` | Immutable record holding all configuration parameters |
| `RingElement` | Single particle with orbital parameters and position |
| `RingElementGenerator` | Interface for type-specific particle generation |
| `RingFieldFactory` | Factory with preset configurations |
| `RingFieldWindow` | Standalone 3D visualization window |

---

## Configuration System

`RingConfiguration` is a Java record containing all parameters:

```java
RingConfiguration config = RingConfiguration.builder()
    .type(RingType.PLANETARY_RING)
    .innerRadius(15)           // Visual units
    .outerRadius(45)
    .numElements(10000)        // Particle count
    .minSize(0.2)              // Particle size range
    .maxSize(0.8)
    .thickness(0.1)            // Vertical spread
    .maxInclinationDeg(0.5)    // Orbital tilt variation
    .maxEccentricity(0.01)     // Orbit shape variation
    .baseAngularSpeed(0.004)   // Rotation speed
    .centralBodyRadius(10)     // For reference
    .primaryColor(Color.rgb(230, 220, 200))
    .secondaryColor(Color.rgb(180, 170, 160))
    .name("Saturn-like Ring")
    .build();
```

---

## Generators

Each `RingType` has a specialized generator that creates particles with appropriate physics:

### PlanetaryRingGenerator
- Gaussian radial distribution (favors middle of ring)
- Very low eccentricity (nearly circular orbits)
- Minimal inclination (flat disk)
- Keplerian speed: inner particles faster than outer

### AsteroidBeltGenerator
- Uniform radial distribution
- Moderate eccentricity (elliptical orbits)
- Significant inclination variation (thick belt)
- Biased size distribution (medium sizes more common)

### DebrisDiskGenerator
- Radial distribution with density wave structure
- Mix of circular (settled dust) and eccentric (recent collisions) orbits
- Bimodal size distribution (lots of dust, some planetesimals)

### DustCloudGenerator
- 3D spherical distribution (not a flat disk)
- Very slow, random motion (turbulent, not orbital)
- Random direction of motion (some particles orbit backwards)
- Color opacity variation for nebula effect

### AccretionDiskGenerator
- Power-law radial distribution (denser toward center)
- Very low eccentricity (viscosity circularizes)
- Temperature-based color gradient (blue-white inner, red-orange outer)
- Disk flares slightly at outer edge

---

## Rendering Pipeline

The `RingFieldRenderer` uses a size-based mesh partitioning strategy for performance:

```
1. Generate particles via RingElementGenerator
        │
        ▼
2. Categorize by size (small/medium/large)
        │
        ▼
3. Create three ScatterMesh objects (FXyz3D library)
   - Small particles: primary color
   - Medium particles: interpolated color
   - Large particles: secondary color
        │
        ▼
4. Add meshes to Group for scene graph
```

### Mesh Refresh Strategy

Rebuilding meshes is expensive. The animation system:
- Updates particle positions every frame (`update(timeScale)`)
- Rebuilds meshes only periodically (`refreshMeshes()` every 5 frames)

---

## Scale Adapters

Adapters convert between real-world units and visual/screen units:

### SolarSystemRingAdapter
- Converts **Astronomical Units (AU)** to screen coordinates
- Uses the existing `ScaleManager` from solar system view
- Methods: `createPlanetaryRing()`, `createAsteroidBelt()`, `createDebrisDisk()`

### InterstellarRingAdapter
- Converts **Light-years** to screen coordinates
- For nebulae and large-scale structures
- Methods: `createEmissionNebula()`, `createDarkNebula()`, `createReflectionNebula()`, `createPlanetaryNebula()`

```java
// Solar system example
SolarSystemRingAdapter adapter = new SolarSystemRingAdapter(scaleManager);
RingConfiguration belt = adapter.createAsteroidBelt(2.1, 3.3, "Main Belt");

// Interstellar example
InterstellarRingAdapter adapter = new InterstellarRingAdapter(50.0);
RingConfiguration nebula = adapter.createEmissionNebula(2.0, 15.0, "Orion Nebula");
```

---

## Animation

Particle animation uses Keplerian orbital mechanics:

```java
// In RingElement.advance(timeScale)
currentAngle += angularSpeed * timeScale;

// Calculate radius at current true anomaly
double r = semiMajorAxis * (1 - e²) / (1 + e * cos(currentAngle));

// Position in orbital plane
double x = r * cos(currentAngle);
double y = r * sin(currentAngle);

// Apply 3D rotations: ω, i, Ω
// ... rotation matrices ...
```

The `RingFieldWindow` runs an `AnimationTimer` that:
1. Computes delta time since last frame
2. Calls `renderer.update(timeScale)` to advance all particles
3. Periodically calls `renderer.refreshMeshes()` to update visuals

---

## Presets

`RingFieldFactory` provides 10 ready-to-use presets:

| Preset Name | Type | Description |
|-------------|------|-------------|
| `Saturn Ring` | PLANETARY_RING | Bright, icy, dense ring system |
| `Uranus Ring` | PLANETARY_RING | Dark, narrow ring bands |
| `Main Asteroid Belt` | ASTEROID_BELT | Rocky debris between Mars and Jupiter |
| `Kuiper Belt` | ASTEROID_BELT | Icy bodies beyond Neptune |
| `Protoplanetary Disk` | DEBRIS_DISK | Young star's planet-forming disk |
| `Collision Debris` | DEBRIS_DISK | Aftermath of planetary collision |
| `Emission Nebula` | DUST_CLOUD | Glowing pink/blue gas cloud |
| `Dark Nebula` | DUST_CLOUD | Light-blocking dust cloud |
| `Black Hole Accretion` | ACCRETION_DISK | Hot disk around black hole |
| `Neutron Star Accretion` | ACCRETION_DISK | Compact, extremely hot disk |

Access via menu: **Experimental → Ring Field Presets**

Or programmatically:
```java
RingFieldWindow window = RingFieldWindow.fromPreset("Saturn Ring");
window.show();
```

---

## Keyboard Controls (RingFieldWindow)

| Key | Action |
|-----|--------|
| Space | Pause/resume animation |
| R | Reset view |
| 1 | Saturn Ring preset |
| 2 | Main Asteroid Belt preset |
| 3 | Protoplanetary Disk preset |
| 4 | Emission Nebula preset |
| 5 | Black Hole Accretion preset |

---

## See Also

- [Solar System View](../solarsystem/SOLAR_SYSTEM_VIEW.md) - Solar system rendering details
- [Solar System Generation](../solarsysmodelling/solarsystem_generation.md) - ACCRETE procedural generation
- [Star Plotting](../starplotting/starplotting.md) - Interstellar view rendering
