# FastNoiseLite Package Guide

A comprehensive guide to using the FastNoiseLite noise generation library for procedural content generation.

## Table of Contents

1. [Overview](#overview)
2. [What's New (Extensions)](#whats-new-extensions)
3. [Quick Start](#quick-start)
4. [Noise Types](#noise-types)
5. [Fractal Noise](#fractal-noise)
6. [Domain Warp](#domain-warp)
7. [4D Noise](#4d-noise) *(Extension)*
8. [Noise Transforms](#noise-transforms) *(Extension)*
9. [Spatial Utilities](#spatial-utilities) *(Extension)*
10. [Advanced Algorithms](#advanced-algorithms) *(Extension)*
11. [Noise Derivatives](#noise-derivatives) *(Extension)*
12. [Configuration Reference](#configuration-reference)
13. [Advanced Usage](#advanced-usage)
14. [Package Structure](#package-structure)

---

## Overview

FastNoiseLite is a fast, portable noise library that provides multiple noise algorithms for procedural generation. This implementation is a modular refactoring of the original FastNoiseLite library, maintaining 100% API compatibility while improving maintainability.

**Key Features (Original FastNoiseLite):**
- Multiple noise types (Simplex, Perlin, Cellular, Value)
- Fractal noise combining (FBm, Ridged, PingPong)
- Domain warping for organic distortion
- Deterministic output (same seed = same results)
- Fast performance suitable for real-time applications

---

## What's New (Extensions)

This implementation extends the original FastNoiseLite with additional features not found in the upstream library:

### New Features

| Feature | Description | Section |
|---------|-------------|---------|
| **4D Simplex Noise** | Full 4D noise support for animations and looping effects | [4D Noise](#4d-noise) |
| **4D Fractal Support** | FBm, Ridged, and PingPong fractals work in 4D | [4D Noise](#4d-noise) |
| **Noise Transforms** | Post-processing transforms for noise values | [Noise Transforms](#noise-transforms) |
| **Transform Chaining** | Combine multiple transforms in pipelines | [Noise Transforms](#noise-transforms) |
| **Chunked Noise** | Infinite worlds without float precision issues | [Spatial Utilities](#spatial-utilities) |
| **LOD Noise** | Distance-based octave reduction for performance | [Spatial Utilities](#spatial-utilities) |
| **Tiled Noise** | Seamlessly tileable noise for textures | [Spatial Utilities](#spatial-utilities) |
| **Seamless Image API** | Godot-style getSeamlessImage() for textures | [Spatial Utilities](#spatial-utilities) |
| **Double Precision** | Double-precision coordinates for astronomical scales | [Spatial Utilities](#spatial-utilities) |
| **Billow Fractal** | Soft, cloud-like fractal (inverted ridged) | [Fractal Noise](#fractal-noise) |
| **Hybrid Multifractal** | Multiplicative/additive blend for terrain | [Fractal Noise](#fractal-noise) |
| **Terrace Transform** | Stepped/terraced patterns | [Noise Transforms](#noise-transforms) |
| **Quantize Transform** | Discrete level quantization | [Noise Transforms](#noise-transforms) |
| **Wavelet Noise** | Band-limited noise for mipmapping and filtering | [Advanced Algorithms](#advanced-algorithms) |
| **Sparse Convolution** | Memory-efficient procedural detail | [Advanced Algorithms](#advanced-algorithms) |
| **Hierarchical Noise** | Quadtree/octree-based adaptive sampling | [Advanced Algorithms](#advanced-algorithms) |
| **Turbulence Noise** | Curl noise and advanced turbulence effects | [Advanced Algorithms](#advanced-algorithms) |
| **Analytical Derivatives** | Exact noise gradients without numerical differentiation | [Noise Derivatives](#noise-derivatives) |
| **Normal Map Generation** | GPU-ready normal maps from noise | [Noise Derivatives](#noise-derivatives) |
| **TBN Basis Computation** | Tangent-Bitangent-Normal for bump mapping | [Noise Derivatives](#noise-derivatives) |

### New Classes

```
generators/
├── Simplex4DNoiseGen.java   # [NEW] 4D Simplex noise generator
└── WaveletNoiseGen.java     # [NEW] Band-limited wavelet noise

transforms/                   # [NEW] Entire package
├── NoiseTransform.java       # Transform interface
├── RangeTransform.java       # Range remapping [-1,1] -> [0,1], etc.
├── PowerTransform.java       # Power curves for sharpening/softening
├── RidgeTransform.java       # Ridge patterns from noise
├── TurbulenceTransform.java  # Billowy turbulence effects
├── ClampTransform.java       # Value clamping
├── InvertTransform.java      # Value inversion/negation
├── ChainedTransform.java     # Combine transforms in sequence
├── TerraceTransform.java     # Stepped/terraced patterns
└── QuantizeTransform.java    # Discrete level quantization

spatial/                      # [NEW] Entire package
├── ChunkedNoise.java         # Infinite worlds with seed mixing
├── LODNoise.java             # Level-of-detail octave reduction
├── TiledNoise.java           # Seamlessly tileable noise
├── DoublePrecisionNoise.java # Double-precision coordinate wrapper
├── SparseConvolutionNoise.java # Memory-efficient sparse sampling
├── HierarchicalNoise.java    # Quadtree/octree adaptive sampling
└── TurbulenceNoise.java      # Curl noise and turbulence effects

derivatives/                  # [NEW] Entire package
├── NoiseDerivatives.java     # Main utility: gradients, normals, FBm+derivatives
└── SimplexDerivatives.java   # Analytical simplex derivative computation
```

### New API Methods

```java
// [NEW] 4D noise sampling
float value = noise.GetNoise(x, y, z, w);

// [NEW] NoiseGenerator interface additions
float value4D = generator.single4D(seed, x, y, z, w);
boolean has4D = generator.supports4D();
```

### New Gradient Data

```java
// [NEW] 4D gradients in NoiseGradients.java
NoiseGradients.Gradients4D  // 32 4D gradient vectors

// [NEW] 4D hashing prime in NoiseUtils.java
NoiseUtils.PrimeW           // Prime constant for W dimension
```

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

### Billow *(Extension)*

> **Extension**: Not part of the original FastNoiseLite library.

Creates soft, cloud-like patterns. The "inverse" of ridged - where ridged creates sharp peaks, billow creates soft bumps.

```java
noise.SetFractalType(FastNoiseLite.FractalType.Billow);
noise.SetFractalOctaves(4);
```

**Best for:** Clouds, soft hills, billowy smoke, organic textures

### Hybrid Multifractal *(Extension)*

> **Extension**: Not part of the original FastNoiseLite library.

Combines multiplicative and additive octave blending. First octave is added normally, subsequent octaves are weighted by previous values. This creates terrain-like features where flat areas stay flat but detailed areas get more detail.

```java
noise.SetFractalType(FastNoiseLite.FractalType.HybridMulti);
noise.SetFractalOctaves(4);
```

**Best for:** Realistic terrain, erosion-like detail distribution, natural landscapes

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

## 4D Noise

> **Extension**: This feature is not part of the original FastNoiseLite library.

4D noise adds a fourth dimension (W), typically used for time-based animations or looping effects.

### Basic 4D Usage

```java
FastNoiseLite noise = new FastNoiseLite(1337);
noise.SetFrequency(0.01f);

// Generate 4D noise (x, y, z, w)
float value = noise.GetNoise(x, y, z, time);
```

**Note:** 4D noise uses Simplex algorithm regardless of the noise type setting, as other algorithms don't support 4D.

### Animated 3D Noise

Use the W dimension as time for smooth animation:

```java
public float getAnimatedValue(float x, float y, float z, float time) {
    FastNoiseLite noise = new FastNoiseLite(1337);
    noise.SetFractalType(FastNoiseLite.FractalType.FBm);
    noise.SetFractalOctaves(4);
    noise.SetFrequency(0.01f);

    // W dimension = time creates smooth animation
    return noise.GetNoise(x, y, z, time * 0.5f);
}
```

### Looping Animation

Create seamlessly looping animations by moving in a circle through the W plane:

```java
public float getLoopingNoise(float x, float y, float z, float progress, float loopRadius) {
    FastNoiseLite noise = new FastNoiseLite(1337);
    noise.SetFrequency(0.02f);

    // Move in a circle through W space for seamless loop
    float angle = progress * 2.0f * (float) Math.PI;  // progress: 0.0 to 1.0
    float w = (float) Math.sin(angle) * loopRadius;
    float extraDim = (float) Math.cos(angle) * loopRadius;

    // Use circular path in 4D space
    return noise.GetNoise(x, y, z + extraDim, w);
}
```

### 4D Fractal Types

All fractal types work with 4D noise:

```java
// FBm 4D
noise.SetFractalType(FastNoiseLite.FractalType.FBm);
float fbmValue = noise.GetNoise(x, y, z, w);

// Ridged 4D
noise.SetFractalType(FastNoiseLite.FractalType.Ridged);
float ridgedValue = noise.GetNoise(x, y, z, w);

// PingPong 4D
noise.SetFractalType(FastNoiseLite.FractalType.PingPong);
float pingPongValue = noise.GetNoise(x, y, z, w);
```

### Direct 4D Generator Access

For advanced use cases, access the 4D generator directly:

```java
import com.teamgannon.trips.noisegen.generators.Simplex4DNoiseGen;

Simplex4DNoiseGen gen = new Simplex4DNoiseGen();
float value = gen.single4D(seed, x, y, z, w);

// Check if generator supports 4D
boolean supports4D = gen.supports4D();  // true for Simplex4DNoiseGen
```

---

## Noise Transforms

> **Extension**: This entire feature set is not part of the original FastNoiseLite library.

Transforms modify noise values after generation. They're useful for remapping ranges, creating special effects, and combining operations.

### Available Transforms

```java
import com.teamgannon.trips.noisegen.transforms.*;
```

#### RangeTransform

Remaps noise from one range to another:

```java
// Default: [-1, 1] -> [0, 1]
NoiseTransform normalize = new RangeTransform();

// Custom range: [-1, 1] -> [0, 255]
NoiseTransform toColor = new RangeTransform(-1f, 1f, 0f, 255f);

// Factory methods
NoiseTransform norm = RangeTransform.normalize();      // [-1,1] -> [0,1]
NoiseTransform scaled = RangeTransform.toMax(100f);    // [-1,1] -> [0,100]

float value = normalize.apply(noiseValue);  // Input: -0.5 -> Output: 0.25
```

#### PowerTransform

Applies power curves to sharpen or soften features:

```java
// Sharpen features (exponent > 1)
NoiseTransform sharp = new PowerTransform(2.0f);

// Soften features (exponent < 1)
NoiseTransform soft = new PowerTransform(0.5f);

// Unsigned output (stays in [0, 1])
NoiseTransform unsignedPower = new PowerTransform(2.0f, false);
```

#### RidgeTransform

Creates ridge-like patterns from noise:

```java
// Standard ridges (peaks at zero crossings)
NoiseTransform ridges = new RidgeTransform();

// Inverted ridges (valleys at zero crossings)
NoiseTransform valleys = new RidgeTransform(true);

// Sharp ridges with power curve
NoiseTransform sharpRidges = new RidgeTransform(false, 2.0f);
```

#### TurbulenceTransform

Creates turbulent, billowy patterns:

```java
// Standard turbulence
NoiseTransform turbulence = new TurbulenceTransform();

// Scaled turbulence
NoiseTransform scaledTurbulence = new TurbulenceTransform(2.0f, 0.1f);
```

#### ClampTransform

Clamps values to a range:

```java
// Standard [-1, 1] clamp
NoiseTransform clamp = new ClampTransform();

// Custom clamp
NoiseTransform customClamp = new ClampTransform(0f, 1f);
```

#### InvertTransform

Inverts (negates) noise values:

```java
NoiseTransform invert = new InvertTransform();
float inverted = invert.apply(0.5f);  // Returns -0.5
```

#### TerraceTransform *(Extension)*

Creates stepped/terraced patterns:

```java
// 8-level sharp terracing
NoiseTransform terrace = new TerraceTransform(8);

// Smooth terraces (gradual transitions)
NoiseTransform smooth = new TerraceTransform(8, 0.5f);

// Inverted (peaks instead of plateaus)
NoiseTransform peaks = new TerraceTransform(8, 0f, true);

// Factory methods
NoiseTransform contours = TerraceTransform.contours(8);
NoiseTransform smoothTerrace = TerraceTransform.smooth(8);
```

**Best for:** Rice paddy terrain, geological strata, contour maps, stylized landscapes

#### QuantizeTransform *(Extension)*

Quantizes values to discrete levels:

```java
// Quantize to 16 levels
NoiseTransform quant = new QuantizeTransform(16);

// With dithering (reduces banding)
NoiseTransform dithered = new QuantizeTransform(8, true);

// Custom step values
float[] steps = {-1f, -0.5f, 0f, 0.5f, 1f};
NoiseTransform custom = new QuantizeTransform(steps);

// Factory methods
NoiseTransform posterize = QuantizeTransform.posterize(8);
NoiseTransform ditheredQuant = QuantizeTransform.dithered(8);
NoiseTransform exponential = QuantizeTransform.exponential(8, 2.0f);  // More levels at low end
```

**Best for:** Posterization effects, level-based terrain, retro graphics, height bands

### Chaining Transforms

Combine multiple transforms in sequence:

```java
// Chain: ridge -> power -> normalize to [0, 255]
ChainedTransform pipeline = new ChainedTransform(
    new RidgeTransform(),
    new PowerTransform(2.0f, false),
    new RangeTransform(0f, 1f, 0f, 255f)
);

float noise = generator.GetNoise(x, y);
float result = pipeline.apply(noise);  // Result in [0, 255]

// Add more transforms dynamically
pipeline.add(new ClampTransform(0f, 255f));
pipeline.prepend(new InvertTransform());
```

### Complete Transform Example

```java
public int[][] generateTerrainColors(int width, int height, int seed) {
    FastNoiseLite noise = new FastNoiseLite(seed);
    noise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
    noise.SetFractalType(FastNoiseLite.FractalType.FBm);
    noise.SetFractalOctaves(6);
    noise.SetFrequency(0.005f);

    // Create terrain color pipeline
    ChainedTransform terrain = new ChainedTransform(
        new RidgeTransform(true),              // Valleys at zero crossings
        new PowerTransform(1.2f, false),       // Slight sharpening
        new RangeTransform(0f, 1f, 0f, 255f),  // Map to color range
        new ClampTransform(0f, 255f)           // Ensure valid color
    );

    int[][] colorMap = new int[width][height];

    for (int x = 0; x < width; x++) {
        for (int y = 0; y < height; y++) {
            float noiseValue = noise.GetNoise(x, y);
            int color = (int) terrain.apply(noiseValue);
            colorMap[x][y] = color;
        }
    }

    return colorMap;
}
```

---

## Spatial Utilities

> **Extension**: This entire feature set is not part of the original FastNoiseLite library.

The spatial package provides utilities for working with noise in large-scale or specialized coordinate systems.

```java
import com.teamgannon.trips.noisegen.spatial.*;
```

### ChunkedNoise

Handles infinite worlds without float precision degradation. Standard float precision breaks down around 100,000+ units from origin; ChunkedNoise works correctly at billions of units.

```java
FastNoiseLite baseNoise = new FastNoiseLite(1337);
baseNoise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
baseNoise.SetFractalType(FastNoiseLite.FractalType.FBm);
baseNoise.SetFractalOctaves(6);

// Create chunked noise with 1000-unit chunks
ChunkedNoise chunked = new ChunkedNoise(baseNoise, 1000.0);

// Works correctly at huge coordinates
double worldX = 1_000_000_000.0;  // 1 billion units
double worldY = 2_500_000_000.0;
float value = chunked.getNoise(worldX, worldY);

// Also supports 3D and 4D
float value3D = chunked.getNoise(worldX, worldY, worldZ);
float value4D = chunked.getNoise(worldX, worldY, worldZ, worldW);

// Get chunk coordinates for a position (useful for chunk-based systems)
int[] chunkCoords = chunked.getChunkCoords(worldX, worldY);
```

**How it works:** Divides world space into chunks, using local coordinates within each chunk combined with chunk-based seed mixing. Different chunks get unique seeds, so patterns don't repeat.

**Best for:** Minecraft-style infinite worlds, space games, very large procedural maps.

### LODNoise

Automatically reduces fractal octaves based on distance or scale, improving performance for distant features without visible quality loss.

```java
FastNoiseLite baseNoise = new FastNoiseLite(1337);
baseNoise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
baseNoise.SetFractalType(FastNoiseLite.FractalType.FBm);
baseNoise.SetFractalOctaves(8);  // Maximum octaves

// Create LOD noise (8 max octaves, drops to 1 at distance 1000)
LODNoise lod = new LODNoise(baseNoise, 8);

// Near terrain - full 8 octaves
float nearValue = lod.getNoise(x, y, 0.0f);

// Far terrain - fewer octaves (faster)
float farValue = lod.getNoise(x, y, 500.0f);

// Custom configuration with builder
LODNoise customLod = LODNoise.builder(baseNoise)
    .maxOctaves(8)
    .minOctaves(2)
    .nearDistance(0f)
    .farDistance(2000f)
    .build();

// Scale-based LOD (for mipmapping)
float scaledValue = lod.getNoiseByScale(x, y, 0.5f);  // Half scale = fewer octaves
```

**Best for:** Terrain LOD systems, cloud rendering, real-time procedural generation.

### TiledNoise

Creates seamlessly tileable noise for textures and repeating patterns using the "sample on a torus" technique.

```java
FastNoiseLite baseNoise = new FastNoiseLite(1337);
baseNoise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
baseNoise.SetFractalType(FastNoiseLite.FractalType.FBm);
baseNoise.SetFractalOctaves(4);

// Create 256x256 tileable noise
TiledNoise tiled = new TiledNoise(baseNoise, 256, 256);

// Sample - edges match perfectly
float v1 = tiled.getNoise(0, 128);      // Left edge
float v2 = tiled.getNoise(256, 128);    // Right edge (same as left!)
// v1 == v2 (seamless)

// Rectangular tiles
TiledNoise rectTiled = new TiledNoise(baseNoise, 512, 256);

// 3D tiling (uses approximation - more expensive)
TiledNoise tiled3D = new TiledNoise(baseNoise, 64, 64, 64);
float v3D = tiled3D.getNoise(x, y, z);

// Generate complete tile as array
float[][] tile = tiled.generateTile(256, 256);

// Normalized coordinates (0 to 1)
float vNorm = tiled.getNoiseNormalized(0.5f, 0.5f);  // Center of tile

// 2D tiling with non-tiling Z (for animated 2D textures)
float animated = tiled.getNoise2DWithZ(x, y, time);
```

#### Seamless Image Generation (Godot-style API)

Generate ready-to-use image data directly:

```java
TiledNoise tiled = new TiledNoise(baseNoise, 256, 256);

// Grayscale image (1 byte per pixel)
byte[] grayscale = tiled.getSeamlessImage(256, 256);

// RGBA image (4 bytes per pixel, full opacity)
byte[] rgba = tiled.getSeamlessImageRGBA(256, 256);

// RGB with custom color mapping
byte[] terrain = tiled.getSeamlessImageRGB(256, 256, TiledNoise.TERRAIN_GRADIENT);
byte[] heatmap = tiled.getSeamlessImageRGB(256, 256, TiledNoise.HEAT_GRADIENT);

// Custom grayscale mapping
byte[] binary = tiled.getSeamlessImage(256, 256, TiledNoise.threshold(0f));
byte[] inverted = tiled.getSeamlessImage(256, 256, TiledNoise.GRAYSCALE_INVERTED);

// Float arrays for further processing
float[] rawValues = tiled.getSeamlessImageFloat(256, 256);        // Range [-1, 1]
float[] normalized = tiled.getSeamlessImageFloatNormalized(256, 256);  // Range [0, 1]

// 3D texture slice
byte[] slice = tiled.getSeamlessImage3DSlice(256, 256, zDepth);
```

**Built-in Color Mappers:**
- `GRAYSCALE` - Standard grayscale mapping
- `GRAYSCALE_INVERTED` - Inverted grayscale
- `TERRAIN_GRADIENT` - Blue (water) → Green (land) → Brown (hills) → White (snow)
- `HEAT_GRADIENT` - Blue → Cyan → Green → Yellow → Red
- `threshold(float t)` - Binary black/white at threshold

**How it works:** Maps 2D coordinates onto a 4D torus surface where opposite edges naturally connect. For 3D tiling, approximates with blended 4D samples.

**Best for:** Tileable textures, infinite scrolling backgrounds, spherical mapping, texture atlases.

### DoublePrecisionNoise

Provides double-precision input coordinates while maintaining float-precision noise. Simpler than ChunkedNoise when you don't need chunk-based seed variation.

```java
FastNoiseLite baseNoise = new FastNoiseLite(1337);
baseNoise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);

DoublePrecisionNoise precise = new DoublePrecisionNoise(baseNoise);

// Works at astronomical distances
double x = 1_000_000_000_000.5;  // 1 trillion + 0.5
double y = 2_500_000_000_000.3;
float value = precise.getNoise(x, y);  // Precise to the decimal

// 3D and 4D support
float v3D = precise.getNoise(x, y, z);
float v4D = precise.getNoise(x, y, z, w);

// Sample a grid efficiently
float[][] grid = precise.sampleGrid(
    startX, startY,   // Starting position (double precision)
    width, height,    // Grid dimensions
    step              // Step size between samples
);

// Get noise centered around a specific position
float centered = precise.getNoiseWithOffset(x, y, centerX, centerY);
```

**Precision comparison:**
- Standard float: ~6-7 significant digits, degrades past ~100,000 units
- DoublePrecisionNoise: ~15 significant digits, works to ~10^15 units

**Best for:** Space simulations, astronomical-scale procedural generation, scientific visualization.

### Spatial Utilities Comparison

| Utility | Double Precision | Seamless Tiling | LOD Support | Seed Variation |
|---------|-----------------|-----------------|-------------|----------------|
| ChunkedNoise | Yes | No | No | Per-chunk seeds |
| LODNoise | No | No | Yes | Same seed |
| TiledNoise | No | Yes | No | Same seed |
| DoublePrecisionNoise | Yes | No | No | Same seed |

**Combining utilities:** You can layer these utilities for specific needs:

```java
// Double-precision LOD noise
FastNoiseLite base = new FastNoiseLite(1337);
base.SetFractalType(FastNoiseLite.FractalType.FBm);
base.SetFractalOctaves(8);

LODNoise lod = new LODNoise(base, 8, 2, 0f, 1000f);

// Manually handle double precision with LOD
double worldX = 1_000_000_000.0;
double worldY = 2_500_000_000.0;
double domainSize = 10000.0;

// Translate to local domain
double domainX = Math.floor(worldX / domainSize) * domainSize;
double domainY = Math.floor(worldY / domainSize) * domainSize;
float localX = (float)(worldX - domainX);
float localY = (float)(worldY - domainY);

// Sample with LOD
float distance = calculateDistanceFromCamera(worldX, worldY);
float value = lod.getNoise(localX, localY, distance);
```

---

## Advanced Algorithms

> **Extension**: This entire feature set is not part of the original FastNoiseLite library.

Advanced noise algorithms for specialized use cases requiring band-limiting, memory efficiency, or adaptive detail.

```java
import com.teamgannon.trips.noisegen.generators.WaveletNoiseGen;
import com.teamgannon.trips.noisegen.spatial.SparseConvolutionNoise;
import com.teamgannon.trips.noisegen.spatial.HierarchicalNoise;
```

### WaveletNoiseGen

Band-limited noise with controlled frequency spectrum. Ideal for textures that mipmap cleanly and procedural content without aliasing.

```java
// Create wavelet noise generator
WaveletNoiseGen wavelet = new WaveletNoiseGen(1337, 128); // seed, tile size

// Sample band-limited noise
float value = wavelet.sample2D(x, y);
float value3D = wavelet.sample3D(x, y, z);

// Sample specific frequency band
float band0 = wavelet.sampleBand2D(x, y, 0);  // Base frequency
float band2 = wavelet.sampleBand2D(x, y, 2);  // Higher frequency

// FBm with band-limited octaves (no aliasing when downsampled)
float fbm = wavelet.sampleFBm2D(x, y, 4, 2.0f, 0.5f);
float fbm3D = wavelet.sampleFBm3D(x, y, z, 4, 2.0f, 0.5f);
```

**How it works:** Pre-generates a noise tile using wavelet decomposition, ensuring each octave contains only its target frequency band. When downsampled, higher frequency content is cleanly filtered out.

**Best for:** Texture synthesis, procedural mipmaps, fluid simulations, any application requiring clean frequency separation.

### SparseConvolutionNoise

Memory-efficient noise using sparse impulse placement with smooth kernel convolution. Produces high-quality noise with constant memory regardless of world size.

```java
// Create sparse convolution noise
SparseConvolutionNoise sparse = new SparseConvolutionNoise(1337);

// Sample noise
float value = sparse.getNoise(x, y);
float value3D = sparse.getNoise(x, y, z);

// FBm with sparse convolution
float fbm = sparse.getFBm(x, y, 4, 2.0f, 0.5f);
float fbm3D = sparse.getFBm(x, y, z, 4, 2.0f, 0.5f);

// Double-precision coordinates (works at astronomical scales)
double hugeX = 1_000_000_000_000.5;
double hugeY = 2_500_000_000_000.3;
float farValue = sparse.getNoise(hugeX, hugeY);

// Configure density and kernel radius
SparseConvolutionNoise custom = new SparseConvolutionNoise(1337, 0.3f, 3.0f);
// 0.3 = impulses per unit area (higher = more detail)
// 3.0 = kernel radius (higher = smoother)

// Create variations
SparseConvolutionNoise denser = sparse.withDensity(0.5f);
SparseConvolutionNoise smoother = sparse.withKernelRadius(4.0f);
```

**How it works:** Places random "impulses" at sparse locations determined by hashing, then convolves with a smooth Wendland kernel. Only nearby impulses contribute to each sample.

**Best for:** Infinite worlds without memory constraints, detail synthesis, particle-like procedural features.

### HierarchicalNoise

Quadtree/octree-based noise with explicit level-of-detail control. Sample only the octaves you need at each location.

```java
FastNoiseLite base = new FastNoiseLite(1337);
base.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);

// Create 8-level hierarchy
HierarchicalNoise hierarchical = new HierarchicalNoise(base, 8);

// Sample specific detail level
float coarse = hierarchical.sampleLevel(x, y, 0);  // Coarsest (continents)
float fine = hierarchical.sampleLevel(x, y, 7);    // Finest (pebbles)

// Sample cumulative (like FBm, but only up to specified level)
float terrain = hierarchical.sampleCumulative(x, y, 4);  // Levels 0-4

// Automatic level selection based on scale/distance
float adaptive = hierarchical.sampleAdaptive(x, y, viewScale);  // viewScale: 0.0-1.0

// For quadtree terrain systems
float nodeNoise = hierarchical.sampleQuadtreeNode(x, y, nodeLevel, 3);
// nodeLevel: which quadtree level this node represents
// 3: how many detail levels to add within this node

// For octree systems (3D)
float octreeNoise = hierarchical.sampleOctreeNode(x, y, z, nodeLevel, 3);

// Get individual level contribution (for progressive refinement)
float delta = hierarchical.sampleLevelDelta(x, y, 5);  // Just level 5's contribution

// Builder pattern for configuration
HierarchicalNoise custom = HierarchicalNoise.builder(base)
    .maxLevels(10)
    .baseFrequency(0.005f)
    .lacunarity(2.0f)
    .persistence(0.5f)
    .build();
```

**How it works:** Pre-computes frequency and amplitude for each level. Each level uses a different seed offset to avoid correlation. Only requested levels are computed, saving work for distant terrain.

**Best for:** LOD terrain systems, streaming worlds, progressive refinement, view-dependent detail.

### TurbulenceNoise

Advanced turbulence effects including curl noise for fluid-like motion.

```java
FastNoiseLite base = new FastNoiseLite(1337);
base.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);

TurbulenceNoise turbulence = new TurbulenceNoise(base);
turbulence.setFrequency(0.01f);

// Classic Perlin turbulence (absolute value sum)
float turb = turbulence.perlinTurbulence(x, y, 4);  // Returns [0, 1]
float turb3D = turbulence.perlinTurbulence(x, y, z, 4);

// Curl noise for incompressible flow (fluid, smoke)
float[] curl2D = turbulence.curl2D(x, y);     // Returns [vx, vy]
float[] curl3D = turbulence.curl3D(x, y, z);  // Returns [vx, vy, vz]

// Multi-octave curl noise
float[] curlFBm = turbulence.curlFBm2D(x, y, 4);
float[] curlFBm3D = turbulence.curlFBm3D(x, y, z, 4);

// Warped turbulence (domain warping + turbulence)
float warped = turbulence.warpedTurbulence(x, y, 4, 30f);  // warpAmplitude = 30

// Marble pattern
float marble = turbulence.marble(x, y, 4, 5f);
float veins = (float) Math.sin(marble);  // Apply sin for vein pattern

// Wood grain pattern
float wood = turbulence.wood(x, y, 4, 0.1f);
float rings = wood - (float) Math.floor(wood);  // Fractional part for rings

// Method chaining for configuration
turbulence.setFrequency(0.02f)
          .setLacunarity(2.0f)
          .setPersistence(0.5f);
```

**Curl noise explained:** Curl noise computes the curl (rotational component) of a potential field, producing divergence-free flow. This means particles following curl noise paths never converge or diverge - perfect for incompressible fluids.

**Best for:** Fluid simulations, smoke/fire effects, particle motion, marble/wood textures, organic animations.

### Algorithm Comparison

| Algorithm | Memory | Precision | Band-Limited | Adaptive LOD | Curl Support |
|-----------|--------|-----------|--------------|--------------|--------------|
| WaveletNoiseGen | Fixed tile | Float | Yes | No | No |
| SparseConvolutionNoise | Constant | Double | No | No | No |
| HierarchicalNoise | Constant | Float | No | Yes | No |
| TurbulenceNoise | Constant | Float | No | No | Yes |

**Combining algorithms:**

```java
// Hierarchical noise with wavelet-quality base
WaveletNoiseGen waveletBase = new WaveletNoiseGen(1337, 128);
// Use wavelet for fine detail, hierarchical structure for LOD

// Sparse convolution for infinite detail overlay
SparseConvolutionNoise detailNoise = new SparseConvolutionNoise(42, 0.4f, 2.0f);
// Add sparse detail on top of base terrain for variation
```

---

## Noise Derivatives

> **Extension**: This entire feature set is not part of the original FastNoiseLite library.

Compute analytical noise gradients (derivatives) and generate normal maps directly from noise. Essential for terrain lighting, bump mapping, and rock/cliff shaders that need surface normals without expensive numerical differentiation.

```java
import com.teamgannon.trips.noisegen.derivatives.NoiseDerivatives;
import com.teamgannon.trips.noisegen.derivatives.SimplexDerivatives;
```

### Why Analytical Derivatives?

Traditional normal map generation requires sampling noise at 2-6 additional points per pixel to compute finite differences. Analytical derivatives compute exact gradients in a single pass, significantly faster for large normal map textures or real-time terrain rendering.

| Method | 2D Samples | 3D Samples | Accuracy |
|--------|------------|------------|----------|
| Numerical (central diff) | 5 | 7 | Approximate |
| Numerical (forward diff) | 3 | 4 | Less accurate |
| Analytical | 1 | 1 | Exact |

### Basic Usage

```java
FastNoiseLite noise = new FastNoiseLite(1337);
noise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
noise.SetFrequency(0.02f);

// Create derivatives utility
NoiseDerivatives deriv = new NoiseDerivatives(noise);

// Get noise value and gradient in one call
NoiseDerivatives.NoiseWithGradient2D result = deriv.getNoiseWithGradient2D(x, y);
float value = result.value;   // Noise value at (x, y)
float dx = result.dx;         // Partial derivative dN/dx
float dy = result.dy;         // Partial derivative dN/dy

// 3D version
NoiseDerivatives.NoiseWithGradient3D result3D = deriv.getNoiseWithGradient3D(x, y, z);
```

### Analytical vs Numerical Mode

```java
// Use analytical derivatives (faster, uses SimplexDerivatives)
NoiseDerivatives analytical = new NoiseDerivatives(noise);
analytical.setUseAnalytical(true);  // Default

// Use numerical derivatives (slower, but matches FastNoiseLite exactly)
NoiseDerivatives numerical = new NoiseDerivatives(noise, 0.001f);  // epsilon for finite diff
numerical.setUseAnalytical(false);
```

Note: Analytical mode uses a simplex noise implementation, which may produce slightly different values than FastNoiseLite's OpenSimplex2. For terrain rendering where visual quality matters more than exact matching, analytical mode is recommended.

### Computing Surface Normals

```java
// 2D heightmap normal (for terrain)
float heightScale = 10.0f;  // How much height affects normal
float[] normal = deriv.computeNormal2D(x, y, heightScale);
// Returns [nx, ny, nz] normalized vector pointing "up" from terrain

// 3D volumetric normal (for isosurfaces/marching cubes)
float[] normal3D = deriv.computeNormal3D(x, y, z);
// Returns gradient direction (points toward higher values)
```

### Generating Normal Maps

```java
// Generate 256x256 normal map
int width = 256, height = 256;
float worldSize = 10.0f;      // Covers 10x10 world units
float heightScale = 5.0f;     // Height multiplier for normal intensity

byte[] rgbData = deriv.generateNormalMapRGB(width, height, worldSize, heightScale);
// Returns width*height*3 bytes (RGB format, ready for GPU upload)

// Save to file (example with JavaFX)
WritableImage image = new WritableImage(width, height);
PixelWriter writer = image.getPixelWriter();
for (int y = 0; y < height; y++) {
    for (int x = 0; x < width; x++) {
        int i = (y * width + x) * 3;
        int r = rgbData[i] & 0xFF;
        int g = rgbData[i + 1] & 0xFF;
        int b = rgbData[i + 2] & 0xFF;
        writer.setColor(x, y, Color.rgb(r, g, b));
    }
}
```

### FBm with Accumulated Derivatives

For fractal noise, derivatives accumulate across octaves:

```java
// FBm noise with derivatives (matches FastNoiseLite's FBm behavior)
NoiseDerivatives.NoiseWithGradient2D fbmResult =
    deriv.getFBmWithGradient2D(x, y, 4, 2.0f, 0.5f);
// 4 octaves, lacunarity 2.0, gain 0.5

NoiseDerivatives.NoiseWithGradient3D fbmResult3D =
    deriv.getFBmWithGradient3D(x, y, z, 6, 2.0f, 0.5f);
```

### TBN Basis Computation

For proper bump mapping with rotated textures, compute the Tangent-Bitangent-Normal basis:

```java
// Compute TBN basis for bump mapping
float[][] tbn = deriv.computeTBN(x, y, heightScale);
float[] tangent = tbn[0];    // T vector
float[] bitangent = tbn[1];  // B vector
float[] normal = tbn[2];     // N vector

// Transform detail normal map sample into world space
float detailNx = ..., detailNy = ..., detailNz = ...;  // From detail normal map
float worldNx = tangent[0]*detailNx + bitangent[0]*detailNy + normal[0]*detailNz;
float worldNy = tangent[1]*detailNx + bitangent[1]*detailNy + normal[1]*detailNz;
float worldNz = tangent[2]*detailNx + bitangent[2]*detailNy + normal[2]*detailNz;
```

### Direct SimplexDerivatives Access

For maximum control, use the underlying analytical implementation directly:

```java
SimplexDerivatives simplex = new SimplexDerivatives(1337);

// Evaluate 2D simplex with analytical derivatives
NoiseDerivatives.NoiseWithGradient2D result = simplex.evaluate2D(seed, x, y);

// Evaluate 3D simplex with analytical derivatives
NoiseDerivatives.NoiseWithGradient3D result3D = simplex.evaluate3D(seed, x, y, z);
```

### Terrain Lighting Example

```java
public Color getTerrainColor(float x, float y) {
    // Get terrain height and normal
    NoiseDerivatives.NoiseWithGradient2D result = deriv.getNoiseWithGradient2D(x, y);
    float height = result.value;
    float[] normal = deriv.computeNormal2D(x, y, 1.0f);

    // Simple directional lighting
    float[] lightDir = {0.5f, 0.5f, 0.707f};  // Normalized
    float diffuse = Math.max(0,
        normal[0]*lightDir[0] + normal[1]*lightDir[1] + normal[2]*lightDir[2]);

    // Base color based on height
    Color baseColor = height > 0.3f ? Color.GRAY : Color.GREEN;

    // Apply lighting
    float ambient = 0.3f;
    float brightness = ambient + (1 - ambient) * diffuse;
    return baseColor.deriveColor(0, 1, brightness, 1);
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

Items marked with `[EXT]` are extensions beyond the original FastNoiseLite.

```
noisegen/
├── FastNoiseLite.java       # Main facade class (use this!)
├── NoiseTypes.java          # All enum definitions
├── NoiseConfig.java         # Configuration holder
├── NoiseGradients.java      # Gradient lookup tables (2D, 3D, 4D [EXT])
├── NoiseUtils.java          # Utility functions (+ PrimeW [EXT])
├── Vector2.java             # 2D vector for domain warp
├── Vector3.java             # 3D vector for domain warp
├── generators/
│   ├── NoiseGenerator.java      # Generator interface (+ 4D methods [EXT])
│   ├── SimplexNoiseGen.java     # OpenSimplex2/2S (2D, 3D)
│   ├── Simplex4DNoiseGen.java   # [EXT] 4D Simplex noise
│   ├── WaveletNoiseGen.java     # [EXT] Band-limited wavelet noise
│   ├── CellularNoiseGen.java    # Cellular/Voronoi
│   ├── PerlinNoiseGen.java      # Classic Perlin
│   └── ValueNoiseGen.java       # Value/ValueCubic
├── fractal/
│   └── FractalProcessor.java    # FBm, Ridged, PingPong
├── transforms/                   # [EXT] Entire package
│   ├── NoiseTransform.java      # Transform interface
│   ├── RangeTransform.java      # Range remapping
│   ├── PowerTransform.java      # Power curves
│   ├── RidgeTransform.java      # Ridge patterns
│   ├── TurbulenceTransform.java # Turbulence effects
│   ├── ClampTransform.java      # Value clamping
│   ├── InvertTransform.java     # Value inversion
│   ├── ChainedTransform.java    # Transform chaining
│   ├── TerraceTransform.java    # [EXT] Stepped/terraced patterns
│   └── QuantizeTransform.java   # [EXT] Discrete level quantization
├── spatial/                      # [EXT] Entire package
│   ├── ChunkedNoise.java        # Infinite worlds with seed mixing
│   ├── LODNoise.java            # Level-of-detail octave reduction
│   ├── TiledNoise.java          # Seamlessly tileable noise
│   ├── DoublePrecisionNoise.java # Double-precision coordinates
│   ├── SparseConvolutionNoise.java # Memory-efficient sparse sampling
│   ├── HierarchicalNoise.java   # Quadtree/octree adaptive LOD
│   └── TurbulenceNoise.java     # [EXT] Curl noise and turbulence
├── derivatives/                  # [EXT] Entire package
│   ├── NoiseDerivatives.java    # Main utility: gradients, normals, normal maps
│   └── SimplexDerivatives.java  # Analytical simplex derivative computation
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
