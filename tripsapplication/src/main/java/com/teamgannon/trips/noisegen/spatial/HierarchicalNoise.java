package com.teamgannon.trips.noisegen.spatial;

import com.teamgannon.trips.noisegen.FastNoiseLite;

/**
 * Hierarchical (quadtree/octree) noise for adaptive detail levels.
 *
 * <p>Provides noise sampling at multiple resolutions, where each level of the
 * hierarchy contributes progressively finer detail. This is useful for:
 * <ul>
 *   <li>Terrain systems with view-dependent detail</li>
 *   <li>Progressive refinement as the camera zooms in</li>
 *   <li>Memory-efficient large world generation</li>
 *   <li>Streaming terrain systems</li>
 * </ul>
 *
 * <p>Unlike standard FBm which always computes all octaves, hierarchical noise
 * lets you sample only the levels you need at any given location.
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * FastNoiseLite baseNoise = new FastNoiseLite(1337);
 * baseNoise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
 *
 * HierarchicalNoise hierarchical = new HierarchicalNoise(baseNoise, 8);
 *
 * // Sample at a specific level of detail
 * float coarse = hierarchical.sampleLevel(x, y, 0);  // Coarsest level
 * float fine = hierarchical.sampleLevel(x, y, 7);    // Finest level
 *
 * // Sample with automatic level selection based on scale
 * float adaptive = hierarchical.sampleAdaptive(x, y, scale);
 *
 * // Get cumulative noise up to a certain level (like FBm)
 * float cumulative = hierarchical.sampleCumulative(x, y, maxLevel);
 * }</pre>
 *
 * <p><b>Extension:</b> This class is not part of the original FastNoiseLite library.
 */
public class HierarchicalNoise {

    private final FastNoiseLite noise;
    private final int maxLevels;
    private final float baseFrequency;
    private final float lacunarity;
    private final float persistence;

    // Pre-computed values for each level
    private final float[] levelFrequencies;
    private final float[] levelAmplitudes;
    private final float[] cumulativeAmplitudes;

    // Seed offsets for each level to avoid correlation
    private final int[] levelSeedOffsets;

    private static final int[] SEED_PRIMES = {
        198491317, 6542989, 357239, 1183186591,
        314159, 271828, 141421, 173205
    };

    /**
     * Create hierarchical noise with default parameters.
     *
     * @param noise Base noise generator
     * @param maxLevels Maximum hierarchy levels (each level doubles frequency)
     */
    public HierarchicalNoise(FastNoiseLite noise, int maxLevels) {
        this(noise, maxLevels, 0.01f, 2.0f, 0.5f);
    }

    /**
     * Create hierarchical noise with specified parameters.
     *
     * @param noise Base noise generator
     * @param maxLevels Maximum hierarchy levels
     * @param baseFrequency Frequency at level 0
     * @param lacunarity Frequency multiplier per level (typically 2.0)
     * @param persistence Amplitude multiplier per level (typically 0.5)
     */
    public HierarchicalNoise(FastNoiseLite noise, int maxLevels,
                              float baseFrequency, float lacunarity, float persistence) {
        this.noise = noise;
        this.maxLevels = Math.max(1, maxLevels);
        this.baseFrequency = baseFrequency;
        this.lacunarity = lacunarity;
        this.persistence = persistence;

        // Pre-compute level values
        this.levelFrequencies = new float[maxLevels];
        this.levelAmplitudes = new float[maxLevels];
        this.cumulativeAmplitudes = new float[maxLevels];
        this.levelSeedOffsets = new int[maxLevels];

        float freq = baseFrequency;
        float amp = 1.0f;
        float cumAmp = 0f;

        for (int i = 0; i < maxLevels; i++) {
            levelFrequencies[i] = freq;
            levelAmplitudes[i] = amp;
            cumAmp += amp;
            cumulativeAmplitudes[i] = cumAmp;
            levelSeedOffsets[i] = SEED_PRIMES[i % SEED_PRIMES.length] * (i + 1);

            freq *= lacunarity;
            amp *= persistence;
        }
    }

    /**
     * Sample noise at a specific hierarchy level.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param level Hierarchy level (0 = coarsest, maxLevels-1 = finest)
     * @return Noise value in range [-1, 1]
     */
    public float sampleLevel(float x, float y, int level) {
        level = clampLevel(level);
        float freq = levelFrequencies[level];

        int originalSeed = noise.GetSeed();
        noise.SetSeed(originalSeed + levelSeedOffsets[level]);
        float value = noise.GetNoise(x * freq, y * freq);
        noise.SetSeed(originalSeed);

        return value;
    }

    /**
     * Sample 3D noise at a specific hierarchy level.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @param level Hierarchy level
     * @return Noise value in range [-1, 1]
     */
    public float sampleLevel(float x, float y, float z, int level) {
        level = clampLevel(level);
        float freq = levelFrequencies[level];

        int originalSeed = noise.GetSeed();
        noise.SetSeed(originalSeed + levelSeedOffsets[level]);
        float value = noise.GetNoise(x * freq, y * freq, z * freq);
        noise.SetSeed(originalSeed);

        return value;
    }

    /**
     * Sample cumulative noise up to a specified level (similar to FBm).
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param maxLevel Maximum level to include (0 to maxLevels-1)
     * @return Noise value in range approximately [-1, 1]
     */
    public float sampleCumulative(float x, float y, int maxLevel) {
        maxLevel = clampLevel(maxLevel);

        int originalSeed = noise.GetSeed();
        float value = 0f;

        for (int i = 0; i <= maxLevel; i++) {
            float freq = levelFrequencies[i];
            float amp = levelAmplitudes[i];
            noise.SetSeed(originalSeed + levelSeedOffsets[i]);
            value += noise.GetNoise(x * freq, y * freq) * amp;
        }

        noise.SetSeed(originalSeed);

        return value / cumulativeAmplitudes[maxLevel];
    }

    /**
     * Sample 3D cumulative noise up to a specified level.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @param maxLevel Maximum level to include
     * @return Noise value in range approximately [-1, 1]
     */
    public float sampleCumulative(float x, float y, float z, int maxLevel) {
        maxLevel = clampLevel(maxLevel);

        int originalSeed = noise.GetSeed();
        float value = 0f;

        for (int i = 0; i <= maxLevel; i++) {
            float freq = levelFrequencies[i];
            float amp = levelAmplitudes[i];
            noise.SetSeed(originalSeed + levelSeedOffsets[i]);
            value += noise.GetNoise(x * freq, y * freq, z * freq) * amp;
        }

        noise.SetSeed(originalSeed);

        return value / cumulativeAmplitudes[maxLevel];
    }

    /**
     * Sample noise with automatic level selection based on scale factor.
     *
     * <p>Useful for LOD systems where the sampling scale varies with distance.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param scale Scale factor (1.0 = full detail, smaller = less detail needed)
     * @return Noise value in range approximately [-1, 1]
     */
    public float sampleAdaptive(float x, float y, float scale) {
        int level = scaleToLevel(scale);
        return sampleCumulative(x, y, level);
    }

    /**
     * Sample 3D noise with automatic level selection.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @param scale Scale factor
     * @return Noise value in range approximately [-1, 1]
     */
    public float sampleAdaptive(float x, float y, float z, float scale) {
        int level = scaleToLevel(scale);
        return sampleCumulative(x, y, z, level);
    }

    /**
     * Sample noise within a quadtree region.
     *
     * <p>Useful when you have a quadtree-based terrain system and want
     * noise appropriate for a specific node.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param nodeLevel Quadtree node level (0 = root, higher = smaller nodes)
     * @param maxDetailLevel Maximum detail level to add within the node
     * @return Noise value in range approximately [-1, 1]
     */
    public float sampleQuadtreeNode(float x, float y, int nodeLevel, int maxDetailLevel) {
        int startLevel = Math.min(nodeLevel, maxLevels - 1);
        int endLevel = Math.min(startLevel + maxDetailLevel, maxLevels - 1);

        int originalSeed = noise.GetSeed();
        float value = 0f;
        float totalAmp = 0f;

        for (int i = startLevel; i <= endLevel; i++) {
            float freq = levelFrequencies[i];
            float amp = levelAmplitudes[i];
            noise.SetSeed(originalSeed + levelSeedOffsets[i]);
            value += noise.GetNoise(x * freq, y * freq) * amp;
            totalAmp += amp;
        }

        noise.SetSeed(originalSeed);

        return totalAmp > 0 ? value / totalAmp : 0f;
    }

    /**
     * Sample 3D noise within an octree region.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @param nodeLevel Octree node level (0 = root)
     * @param maxDetailLevel Maximum detail levels to add
     * @return Noise value in range approximately [-1, 1]
     */
    public float sampleOctreeNode(float x, float y, float z, int nodeLevel, int maxDetailLevel) {
        int startLevel = Math.min(nodeLevel, maxLevels - 1);
        int endLevel = Math.min(startLevel + maxDetailLevel, maxLevels - 1);

        int originalSeed = noise.GetSeed();
        float value = 0f;
        float totalAmp = 0f;

        for (int i = startLevel; i <= endLevel; i++) {
            float freq = levelFrequencies[i];
            float amp = levelAmplitudes[i];
            noise.SetSeed(originalSeed + levelSeedOffsets[i]);
            value += noise.GetNoise(x * freq, y * freq, z * freq) * amp;
            totalAmp += amp;
        }

        noise.SetSeed(originalSeed);

        return totalAmp > 0 ? value / totalAmp : 0f;
    }

    /**
     * Get the delta (difference) between two adjacent levels.
     *
     * <p>Useful for progressive refinement where you want to add
     * detail incrementally as you zoom in.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param level The level whose contribution to return
     * @return Scaled noise value for this level only
     */
    public float sampleLevelDelta(float x, float y, int level) {
        level = clampLevel(level);
        float freq = levelFrequencies[level];
        float amp = levelAmplitudes[level];

        int originalSeed = noise.GetSeed();
        noise.SetSeed(originalSeed + levelSeedOffsets[level]);
        float value = noise.GetNoise(x * freq, y * freq) * amp;
        noise.SetSeed(originalSeed);

        return value;
    }

    /**
     * Get 3D level delta.
     */
    public float sampleLevelDelta(float x, float y, float z, int level) {
        level = clampLevel(level);
        float freq = levelFrequencies[level];
        float amp = levelAmplitudes[level];

        int originalSeed = noise.GetSeed();
        noise.SetSeed(originalSeed + levelSeedOffsets[level]);
        float value = noise.GetNoise(x * freq, y * freq, z * freq) * amp;
        noise.SetSeed(originalSeed);

        return value;
    }

    /**
     * Convert scale factor to hierarchy level.
     *
     * @param scale Scale factor (1.0 = full detail)
     * @return Appropriate hierarchy level
     */
    public int scaleToLevel(float scale) {
        if (scale >= 1.0f) {
            return maxLevels - 1;
        }
        if (scale <= 0) {
            return 0;
        }

        // log2(1/scale) gives how many levels to skip from max
        float log2InvScale = (float) (-Math.log(scale) / Math.log(lacunarity));
        int level = maxLevels - 1 - (int) log2InvScale;

        return clampLevel(level);
    }

    /**
     * Convert hierarchy level to scale factor.
     *
     * @param level Hierarchy level
     * @return Approximate scale at this level
     */
    public float levelToScale(int level) {
        level = clampLevel(level);
        int levelsFromMax = maxLevels - 1 - level;
        return (float) Math.pow(1.0 / lacunarity, levelsFromMax);
    }

    /**
     * Get the frequency at a specific level.
     *
     * @param level Hierarchy level
     * @return Frequency at this level
     */
    public float getFrequency(int level) {
        return levelFrequencies[clampLevel(level)];
    }

    /**
     * Get the amplitude at a specific level.
     *
     * @param level Hierarchy level
     * @return Amplitude at this level
     */
    public float getAmplitude(int level) {
        return levelAmplitudes[clampLevel(level)];
    }

    /**
     * Get the maximum number of levels.
     */
    public int getMaxLevels() {
        return maxLevels;
    }

    /**
     * Get the base frequency.
     */
    public float getBaseFrequency() {
        return baseFrequency;
    }

    /**
     * Get the lacunarity.
     */
    public float getLacunarity() {
        return lacunarity;
    }

    /**
     * Get the persistence.
     */
    public float getPersistence() {
        return persistence;
    }

    /**
     * Get the underlying FastNoiseLite instance.
     */
    public FastNoiseLite getNoise() {
        return noise;
    }

    private int clampLevel(int level) {
        return Math.max(0, Math.min(maxLevels - 1, level));
    }

    /**
     * Builder for creating HierarchicalNoise with fluent API.
     */
    public static class Builder {
        private FastNoiseLite noise;
        private int maxLevels = 8;
        private float baseFrequency = 0.01f;
        private float lacunarity = 2.0f;
        private float persistence = 0.5f;

        public Builder(FastNoiseLite noise) {
            this.noise = noise;
        }

        public Builder maxLevels(int maxLevels) {
            this.maxLevels = maxLevels;
            return this;
        }

        public Builder baseFrequency(float baseFrequency) {
            this.baseFrequency = baseFrequency;
            return this;
        }

        public Builder lacunarity(float lacunarity) {
            this.lacunarity = lacunarity;
            return this;
        }

        public Builder persistence(float persistence) {
            this.persistence = persistence;
            return this;
        }

        public HierarchicalNoise build() {
            return new HierarchicalNoise(noise, maxLevels, baseFrequency, lacunarity, persistence);
        }
    }

    /**
     * Create a builder for fluent configuration.
     *
     * @param noise The base FastNoiseLite instance
     * @return A new Builder
     */
    public static Builder builder(FastNoiseLite noise) {
        return new Builder(noise);
    }
}
