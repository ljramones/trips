# FastNoiseLite Package Guide

A comprehensive guide to using the FastNoiseLite noise generation library for procedural content generation.

## Table of Contents

1. [Overview](#overview)
2. [Quick Start](#quick-start)
3. [Noise Types](#noise-types)
4. [Fractal Noise](#fractal-noise)
5. [Domain Warp](#domain-warp)
6. [Configuration Reference](#configuration-reference)
7. [Advanced Usage](#advanced-usage)
8. [Package Structure](#package-structure)

---

## Overview

FastNoiseLite is a fast, portable noise library that provides multiple noise algorithms for procedural generation. This implementation is a modular refactoring of the original FastNoiseLite library, maintaining 100% API compatibility while improving maintainability.

**Key Features:**
- Multiple noise types (Simplex, Perlin, Cellular, Value)
- Fractal noise combining (FBm, Ridged, PingPong)
- Domain warping for organic distortion
- Deterministic output (same seed = same results)
- Fast performance suitable for real-time applications

---

## Quick Start

### Basic Usage

```java
import com.teamgannon.trips.noisegen.FastNoiseLite;

// Create noise generator with seed
FastNoiseLite noise = new FastNoiseLite(1337);

// Generate 2D noise
float value2D = noise.GetNoise(x, y);

// Generate 3D noise
float value3D = noise.GetNoise(x, y, z);
```

### Complete Example: Generating a Height Map

```java
import com.teamgannon.trips.noisegen.FastNoiseLite;

public class HeightMapGenerator {

    public float[][] generateHeightMap(int width, int height, int seed) {
        FastNoiseLite noise = new FastNoiseLite(seed);

        // Configure for terrain-like noise
        noise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
        noise.SetFractalType(FastNoiseLite.FractalType.FBm);
        noise.SetFractalOctaves(6);
        noise.SetFrequency(0.005f);

        float[][] heightMap = new float[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // Noise returns values in range [-1, 1]
                // Normalize to [0, 1] for height map
                float noiseValue = noise.GetNoise(x, y);
                heightMap[x][y] = (noiseValue + 1.0f) / 2.0f;
            }
        }

        return heightMap;
    }
}
```

---

## Noise Types

### OpenSimplex2 (Default)

The recommended general-purpose noise. Produces smooth, natural-looking patterns with good performance.

```java
noise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
```

**Best for:** Terrain, clouds, general procedural textures

### OpenSimplex2S

A smoother variant of OpenSimplex2 with slightly different characteristics.

```java
noise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2S);
```

**Best for:** When you need extra smoothness

### Perlin

Classic Perlin noise. Has a slightly different visual character than Simplex.

```java
noise.SetNoiseType(FastNoiseLite.NoiseType.Perlin);
```

**Best for:** Classic procedural textures, compatibility with existing algorithms

### Cellular (Voronoi)

Creates cell-like patterns based on distance to random points.

```java
noise.SetNoiseType(FastNoiseLite.NoiseType.Cellular);

// Configure distance function
noise.SetCellularDistanceFunction(FastNoiseLite.CellularDistanceFunction.Euclidean);
// Options: Euclidean, EuclideanSq, Manhattan, Hybrid

// Configure return type
noise.SetCellularReturnType(FastNoiseLite.CellularReturnType.Distance);
// Options: CellValue, Distance, Distance2, Distance2Add, Distance2Sub, Distance2Mul, Distance2Div

// Adjust cell randomness (0.0 = regular grid, 1.0 = fully random)
noise.SetCellularJitter(1.0f);
```

**Best for:** Stone textures, biological patterns, cracked surfaces, Voronoi diagrams

#### Cellular Return Types Explained

| Return Type | Description | Visual Result |
|-------------|-------------|---------------|
| `CellValue` | Returns same value for entire cell | Flat colored regions |
| `Distance` | Distance to nearest point | Gradient from cell center |
| `Distance2` | Distance to second-nearest point | Inverted cell pattern |
| `Distance2Add` | Distance + Distance2 | Rounded cell edges |
| `Distance2Sub` | Distance2 - Distance | Cell outlines |
| `Distance2Mul` | Distance * Distance2 | Complex patterns |
| `Distance2Div` | Distance / Distance2 | Highlighted edges |

### Value

Simple value noise using interpolated random values at grid points.

```java
noise.SetNoiseType(FastNoiseLite.NoiseType.Value);
```

**Best for:** Simple noise needs, retro/pixelated effects

### ValueCubic

Value noise with cubic interpolation for smoother results.

```java
noise.SetNoiseType(FastNoiseLite.NoiseType.ValueCubic);
```

**Best for:** Smoother value noise when gradient noise isn't needed

---

## Fractal Noise

Fractal noise combines multiple octaves of noise at different frequencies to create more detailed, natural-looking patterns.

### FBm (Fractional Brownian Motion)

The most common fractal type. Each octave adds smaller details.

```java
noise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
noise.SetFractalType(FastNoiseLite.FractalType.FBm);
noise.SetFractalOctaves(4);      // Number of noise layers (1-10)
noise.SetFractalLacunarity(2.0f); // Frequency multiplier per octave
noise.SetFractalGain(0.5f);       // Amplitude multiplier per octave

float value = noise.GetNoise(x, y);
```

**Parameters:**
- **Octaves**: More octaves = more detail (but slower)
- **Lacunarity**: Higher = faster frequency increase (2.0 is standard)
- **Gain**: Lower = faster amplitude decrease (0.5 is standard)

### Ridged

Creates ridge-like patterns, great for mountains and veins.

```java
noise.SetFractalType(FastNoiseLite.FractalType.Ridged);
noise.SetFractalOctaves(4);
```

**Best for:** Mountain ridges, river networks, veins, cracks

### PingPong

Creates banded, terraced patterns.

```java
noise.SetFractalType(FastNoiseLite.FractalType.PingPong);
noise.SetFractalOctaves(4);
noise.SetFractalPingPongStrength(2.0f); // Controls band intensity
```

**Best for:** Terraced terrain, abstract patterns, wood grain

### Weighted Strength

Fine-tune how octaves combine based on the previous octave's value.

```java
noise.SetFractalWeightedStrength(0.5f); // 0.0 to 1.0
```

Higher values make the noise more varied in detailed areas.

---

## Domain Warp

Domain warping distorts the input coordinates before sampling noise, creating organic, swirling patterns.

### Basic Domain Warp

```java
import com.teamgannon.trips.noisegen.FastNoiseLite;
import com.teamgannon.trips.noisegen.Vector2;
import com.teamgannon.trips.noisegen.Vector3;

FastNoiseLite noise = new FastNoiseLite(1337);
noise.SetDomainWarpType(FastNoiseLite.DomainWarpType.OpenSimplex2);
noise.SetDomainWarpAmp(30.0f);  // Warp distance
noise.SetFrequency(0.01f);

// 2D warp
Vector2 coord2D = new Vector2(x, y);
noise.DomainWarp(coord2D);
float value = noise.GetNoise(coord2D.x, coord2D.y);

// 3D warp
Vector3 coord3D = new Vector3(x, y, z);
noise.DomainWarp(coord3D);
float value3D = noise.GetNoise(coord3D.x, coord3D.y, coord3D.z);
```

### Warp Types

| Type | Description |
|------|-------------|
| `OpenSimplex2` | Smooth, organic warping |
| `OpenSimplex2Reduced` | Faster, slightly less quality |
| `BasicGrid` | Grid-based warping |

### Fractal Domain Warp

For more complex warping, use fractal warp modes:

```java
// Progressive: Each octave warps the already-warped coordinates
noise.SetFractalType(FastNoiseLite.FractalType.DomainWarpProgressive);

// Independent: Each octave warps from original coordinates
noise.SetFractalType(FastNoiseLite.FractalType.DomainWarpIndependent);

noise.SetFractalOctaves(3);
noise.SetFractalLacunarity(2.0f);
noise.SetFractalGain(0.5f);

Vector2 coord = new Vector2(x, y);
noise.DomainWarp(coord);  // Applies fractal warp
```

### Complete Domain Warp Example

```java
public float[][] generateWarpedTerrain(int width, int height, int seed) {
    FastNoiseLite warpNoise = new FastNoiseLite(seed);
    FastNoiseLite terrainNoise = new FastNoiseLite(seed + 1);

    // Configure warp
    warpNoise.SetDomainWarpType(FastNoiseLite.DomainWarpType.OpenSimplex2);
    warpNoise.SetDomainWarpAmp(50.0f);
    warpNoise.SetFrequency(0.003f);
    warpNoise.SetFractalType(FastNoiseLite.FractalType.DomainWarpProgressive);
    warpNoise.SetFractalOctaves(3);

    // Configure terrain
    terrainNoise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
    terrainNoise.SetFractalType(FastNoiseLite.FractalType.FBm);
    terrainNoise.SetFractalOctaves(5);
    terrainNoise.SetFrequency(0.005f);

    float[][] terrain = new float[width][height];

    for (int x = 0; x < width; x++) {
        for (int y = 0; y < height; y++) {
            // Warp coordinates
            Vector2 coord = new Vector2(x, y);
            warpNoise.DomainWarp(coord);

            // Sample terrain at warped position
            terrain[x][y] = terrainNoise.GetNoise(coord.x, coord.y);
        }
    }

    return terrain;
}
```

---

## Configuration Reference

### All Settings with Defaults

```java
FastNoiseLite noise = new FastNoiseLite();

// Core settings
noise.SetSeed(1337);                    // Default: 1337
noise.SetFrequency(0.01f);              // Default: 0.01

// Noise type
noise.SetNoiseType(NoiseType.OpenSimplex2);  // Default: OpenSimplex2

// 3D rotation (reduces directional artifacts)
noise.SetRotationType3D(RotationType3D.None);  // Default: None
// Options: None, ImproveXYPlanes, ImproveXZPlanes

// Fractal settings
noise.SetFractalType(FractalType.None);       // Default: None
noise.SetFractalOctaves(3);                   // Default: 3
noise.SetFractalLacunarity(2.0f);             // Default: 2.0
noise.SetFractalGain(0.5f);                   // Default: 0.5
noise.SetFractalWeightedStrength(0.0f);       // Default: 0.0
noise.SetFractalPingPongStrength(2.0f);       // Default: 2.0

// Cellular settings
noise.SetCellularDistanceFunction(CellularDistanceFunction.EuclideanSq);  // Default
noise.SetCellularReturnType(CellularReturnType.Distance);                 // Default
noise.SetCellularJitter(1.0f);                                            // Default: 1.0

// Domain warp settings
noise.SetDomainWarpType(DomainWarpType.OpenSimplex2);  // Default
noise.SetDomainWarpAmp(1.0f);                          // Default: 1.0
```

### Frequency Guidelines

| Scale | Frequency | Use Case |
|-------|-----------|----------|
| Continent | 0.001 - 0.003 | Large landmasses |
| Region | 0.003 - 0.01 | Mountain ranges, biomes |
| Local | 0.01 - 0.05 | Hills, forests |
| Detail | 0.05 - 0.2 | Rocks, grass |
| Fine | 0.2 - 1.0 | Textures, small details |

---

## Advanced Usage

### Combining Multiple Noise Layers

```java
public float getTerrainHeight(float x, float y) {
    FastNoiseLite continentNoise = new FastNoiseLite(seed);
    FastNoiseLite mountainNoise = new FastNoiseLite(seed + 1);
    FastNoiseLite detailNoise = new FastNoiseLite(seed + 2);

    // Large-scale continent shapes
    continentNoise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
    continentNoise.SetFractalType(FastNoiseLite.FractalType.FBm);
    continentNoise.SetFractalOctaves(3);
    continentNoise.SetFrequency(0.002f);

    // Medium-scale mountains
    mountainNoise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
    mountainNoise.SetFractalType(FastNoiseLite.FractalType.Ridged);
    mountainNoise.SetFractalOctaves(4);
    mountainNoise.SetFrequency(0.008f);

    // Small-scale detail
    detailNoise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
    detailNoise.SetFractalType(FastNoiseLite.FractalType.FBm);
    detailNoise.SetFractalOctaves(4);
    detailNoise.SetFrequency(0.03f);

    float continent = continentNoise.GetNoise(x, y);
    float mountain = mountainNoise.GetNoise(x, y);
    float detail = detailNoise.GetNoise(x, y);

    // Combine with weights
    return continent * 0.6f + mountain * 0.3f + detail * 0.1f;
}
```

### 3D Noise for Caves

```java
public boolean isCave(float x, float y, float z) {
    FastNoiseLite caveNoise = new FastNoiseLite(seed);
    caveNoise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
    caveNoise.SetFractalType(FastNoiseLite.FractalType.FBm);
    caveNoise.SetFractalOctaves(3);
    caveNoise.SetFrequency(0.02f);

    float density = caveNoise.GetNoise(x, y, z);

    // Threshold determines cave size
    return density > 0.3f;
}
```

### Biome Selection with Cellular Noise

```java
public int getBiome(float x, float y) {
    FastNoiseLite biomeNoise = new FastNoiseLite(seed);
    biomeNoise.SetNoiseType(FastNoiseLite.NoiseType.Cellular);
    biomeNoise.SetCellularReturnType(FastNoiseLite.CellularReturnType.CellValue);
    biomeNoise.SetCellularJitter(0.8f);
    biomeNoise.SetFrequency(0.005f);

    float biomeValue = biomeNoise.GetNoise(x, y);

    // Map noise value to biome ID
    if (biomeValue < -0.5f) return BIOME_DESERT;
    if (biomeValue < 0.0f) return BIOME_PLAINS;
    if (biomeValue < 0.5f) return BIOME_FOREST;
    return BIOME_MOUNTAINS;
}
```

### Animated Noise (Time-based)

```java
public float getAnimatedNoise(float x, float y, float time) {
    FastNoiseLite noise = new FastNoiseLite(seed);
    noise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
    noise.SetFractalType(FastNoiseLite.FractalType.FBm);
    noise.SetFractalOctaves(4);
    noise.SetFrequency(0.01f);

    // Use time as Z coordinate for smooth animation
    return noise.GetNoise(x, y, time * 0.5f);
}
```

---

## Package Structure

```
noisegen/
├── FastNoiseLite.java       # Main facade class (use this!)
├── NoiseTypes.java          # All enum definitions
├── NoiseConfig.java         # Configuration holder
├── NoiseGradients.java      # Gradient lookup tables
├── NoiseUtils.java          # Utility functions
├── Vector2.java             # 2D vector for domain warp
├── Vector3.java             # 3D vector for domain warp
├── generators/
│   ├── NoiseGenerator.java      # Generator interface
│   ├── SimplexNoiseGen.java     # OpenSimplex2/2S
│   ├── CellularNoiseGen.java    # Cellular/Voronoi
│   ├── PerlinNoiseGen.java      # Classic Perlin
│   └── ValueNoiseGen.java       # Value/ValueCubic
├── fractal/
│   └── FractalProcessor.java    # FBm, Ridged, PingPong
└── warp/
    └── DomainWarpProcessor.java # Domain warp algorithms
```

### Direct Component Usage (Advanced)

For advanced users who need direct access to individual components:

```java
import com.teamgannon.trips.noisegen.NoiseConfig;
import com.teamgannon.trips.noisegen.generators.SimplexNoiseGen;
import com.teamgannon.trips.noisegen.fractal.FractalProcessor;

// Create configuration
NoiseConfig config = new NoiseConfig();
config.setSeed(1337);
config.setOctaves(4);
config.setLacunarity(2.0f);
config.setGain(0.5f);

// Create generator directly
SimplexNoiseGen simplex = new SimplexNoiseGen(false);  // false = OpenSimplex2

// Use fractal processor
FractalProcessor fractal = new FractalProcessor(config, simplex);
float value = fractal.genFractalFBm2D(x, y);
```

---

## Performance Tips

1. **Reuse FastNoiseLite instances** - Don't create new instances per sample
2. **Use appropriate octaves** - More octaves = more detail but slower (4-6 is usually enough)
3. **Consider OpenSimplex2Reduced for warp** - Faster than full OpenSimplex2
4. **Batch processing** - Sample in loops rather than individual calls
5. **Lower frequency for large areas** - Reduces the effective sampling density

---

## License

MIT License - See source files for full license text.

Based on FastNoiseLite by Jordan Peck (https://github.com/Auburn/FastNoiseLite)
