package com.teamgannon.trips.noisegen.spatial;

import com.teamgannon.trips.noisegen.FastNoiseLite;

/**
 * Level-of-Detail (LOD) aware noise that adjusts detail based on distance or scale.
 *
 * <p>Automatically reduces fractal octaves for distant/small features,
 * improving performance without visible quality loss. Near features get
 * full detail, distant features get fewer octaves.
 *
 * <h2>Use Cases:</h2>
 * <ul>
 *   <li>Terrain LOD - Less detail on distant mountains</li>
 *   <li>Cloud rendering - Simplified clouds at horizon</li>
 *   <li>Texture generation - Fewer octaves for mipmaps</li>
 *   <li>Real-time procedural - Adaptive quality for performance</li>
 * </ul>
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * FastNoiseLite baseNoise = new FastNoiseLite(1337);
 * baseNoise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
 * baseNoise.SetFractalType(FastNoiseLite.FractalType.FBm);
 * baseNoise.SetFractalOctaves(8);  // Maximum octaves
 *
 * LODNoise lod = new LODNoise(baseNoise, 8);  // 8 max octaves
 *
 * // Near terrain (full detail)
 * float nearValue = lod.getNoise(x, y, 0.0f);  // distance = 0, uses 8 octaves
 *
 * // Far terrain (reduced detail)
 * float farValue = lod.getNoise(x, y, 500.0f);  // distance = 500, uses fewer octaves
 * }</pre>
 *
 * <p><b>Extension:</b> This class is not part of the original FastNoiseLite library.
 */
public class LODNoise {

    private final FastNoiseLite noise;
    private final int maxOctaves;
    private final int minOctaves;
    private final float nearDistance;
    private final float farDistance;

    /**
     * Create LOD noise with default distance settings.
     * Near distance: 0, Far distance: 1000
     *
     * @param noise The base FastNoiseLite instance
     * @param maxOctaves Maximum octaves at near distance
     */
    public LODNoise(FastNoiseLite noise, int maxOctaves) {
        this(noise, maxOctaves, 1, 0f, 1000f);
    }

    /**
     * Create LOD noise with specified octave range.
     *
     * @param noise The base FastNoiseLite instance
     * @param maxOctaves Maximum octaves at near distance
     * @param minOctaves Minimum octaves at far distance
     */
    public LODNoise(FastNoiseLite noise, int maxOctaves, int minOctaves) {
        this(noise, maxOctaves, minOctaves, 0f, 1000f);
    }

    /**
     * Create LOD noise with full configuration.
     *
     * @param noise The base FastNoiseLite instance
     * @param maxOctaves Maximum octaves at near distance
     * @param minOctaves Minimum octaves at far distance
     * @param nearDistance Distance at which max octaves are used
     * @param farDistance Distance at which min octaves are used
     */
    public LODNoise(FastNoiseLite noise, int maxOctaves, int minOctaves,
                    float nearDistance, float farDistance) {
        this.noise = noise;
        this.maxOctaves = Math.max(1, maxOctaves);
        this.minOctaves = Math.max(1, Math.min(minOctaves, maxOctaves));
        this.nearDistance = nearDistance;
        this.farDistance = Math.max(nearDistance + 1, farDistance);
    }

    /**
     * Get 2D noise with LOD based on distance.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param distance Distance from viewer/camera
     * @return Noise value in range [-1, 1]
     */
    public float getNoise(float x, float y, float distance) {
        int octaves = calculateOctaves(distance);
        noise.SetFractalOctaves(octaves);
        return noise.GetNoise(x, y);
    }

    /**
     * Get 3D noise with LOD based on distance.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @param distance Distance from viewer/camera
     * @return Noise value in range [-1, 1]
     */
    public float getNoise(float x, float y, float z, float distance) {
        int octaves = calculateOctaves(distance);
        noise.SetFractalOctaves(octaves);
        return noise.GetNoise(x, y, z);
    }

    /**
     * Get 4D noise with LOD based on distance.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @param w W coordinate
     * @param distance Distance from viewer/camera
     * @return Noise value in range [-1, 1]
     */
    public float getNoise(float x, float y, float z, float w, float distance) {
        int octaves = calculateOctaves(distance);
        noise.SetFractalOctaves(octaves);
        return noise.GetNoise(x, y, z, w);
    }

    /**
     * Get 2D noise with LOD based on scale factor.
     * Smaller scale = less detail needed.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param scale Scale factor (1.0 = full size, 0.1 = 10% size)
     * @return Noise value in range [-1, 1]
     */
    public float getNoiseByScale(float x, float y, float scale) {
        int octaves = calculateOctavesByScale(scale);
        noise.SetFractalOctaves(octaves);
        return noise.GetNoise(x, y);
    }

    /**
     * Get 3D noise with LOD based on scale factor.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @param scale Scale factor (1.0 = full size, 0.1 = 10% size)
     * @return Noise value in range [-1, 1]
     */
    public float getNoiseByScale(float x, float y, float z, float scale) {
        int octaves = calculateOctavesByScale(scale);
        noise.SetFractalOctaves(octaves);
        return noise.GetNoise(x, y, z);
    }

    /**
     * Get 2D noise with explicit octave count.
     * Useful when you've pre-calculated the LOD level.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param octaves Number of octaves to use
     * @return Noise value in range [-1, 1]
     */
    public float getNoiseWithOctaves(float x, float y, int octaves) {
        noise.SetFractalOctaves(clampOctaves(octaves));
        return noise.GetNoise(x, y);
    }

    /**
     * Get 3D noise with explicit octave count.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @param octaves Number of octaves to use
     * @return Noise value in range [-1, 1]
     */
    public float getNoiseWithOctaves(float x, float y, float z, int octaves) {
        noise.SetFractalOctaves(clampOctaves(octaves));
        return noise.GetNoise(x, y, z);
    }

    /**
     * Calculate octaves for a given distance.
     *
     * @param distance Distance from viewer
     * @return Number of octaves to use
     */
    public int calculateOctaves(float distance) {
        if (distance <= nearDistance) {
            return maxOctaves;
        }
        if (distance >= farDistance) {
            return minOctaves;
        }

        // Linear interpolation between near and far
        float t = (distance - nearDistance) / (farDistance - nearDistance);
        int octaves = Math.round(maxOctaves - t * (maxOctaves - minOctaves));
        return clampOctaves(octaves);
    }

    /**
     * Calculate octaves based on scale factor.
     * Uses log2 relationship: halving scale removes ~1 octave.
     *
     * @param scale Scale factor (1.0 = full detail)
     * @return Number of octaves to use
     */
    public int calculateOctavesByScale(float scale) {
        if (scale <= 0) {
            return minOctaves;
        }
        if (scale >= 1.0f) {
            return maxOctaves;
        }

        // Each halving of scale removes one octave
        // log2(scale) gives us how many halvings
        float log2Scale = (float) (Math.log(scale) / Math.log(2));
        int reduction = (int) Math.ceil(-log2Scale);
        return clampOctaves(maxOctaves - reduction);
    }

    /**
     * Get the LOD level (0 = max detail, higher = less detail).
     *
     * @param distance Distance from viewer
     * @return LOD level
     */
    public int getLODLevel(float distance) {
        return maxOctaves - calculateOctaves(distance);
    }

    /**
     * Get maximum octaves setting.
     */
    public int getMaxOctaves() {
        return maxOctaves;
    }

    /**
     * Get minimum octaves setting.
     */
    public int getMinOctaves() {
        return minOctaves;
    }

    /**
     * Get near distance setting.
     */
    public float getNearDistance() {
        return nearDistance;
    }

    /**
     * Get far distance setting.
     */
    public float getFarDistance() {
        return farDistance;
    }

    /**
     * Get the underlying FastNoiseLite instance.
     */
    public FastNoiseLite getNoise() {
        return noise;
    }

    private int clampOctaves(int octaves) {
        return Math.max(minOctaves, Math.min(maxOctaves, octaves));
    }

    /**
     * Builder for creating LODNoise with fluent API.
     */
    public static class Builder {
        private FastNoiseLite noise;
        private int maxOctaves = 8;
        private int minOctaves = 1;
        private float nearDistance = 0f;
        private float farDistance = 1000f;

        public Builder(FastNoiseLite noise) {
            this.noise = noise;
        }

        public Builder maxOctaves(int maxOctaves) {
            this.maxOctaves = maxOctaves;
            return this;
        }

        public Builder minOctaves(int minOctaves) {
            this.minOctaves = minOctaves;
            return this;
        }

        public Builder nearDistance(float nearDistance) {
            this.nearDistance = nearDistance;
            return this;
        }

        public Builder farDistance(float farDistance) {
            this.farDistance = farDistance;
            return this;
        }

        public Builder distanceRange(float near, float far) {
            this.nearDistance = near;
            this.farDistance = far;
            return this;
        }

        public LODNoise build() {
            return new LODNoise(noise, maxOctaves, minOctaves, nearDistance, farDistance);
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
