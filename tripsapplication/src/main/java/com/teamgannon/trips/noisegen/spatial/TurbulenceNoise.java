package com.teamgannon.trips.noisegen.spatial;

import com.teamgannon.trips.noisegen.FastNoiseLite;

/**
 * Advanced turbulence and curl noise utilities.
 *
 * <p>Provides various turbulence effects beyond simple FBm:
 * <ul>
 *   <li>Curl noise for incompressible flow fields</li>
 *   <li>Perlin turbulence (absolute value sum)</li>
 *   <li>Warped turbulence (domain warping + turbulence)</li>
 *   <li>Multiscale turbulence with independent parameters</li>
 * </ul>
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * FastNoiseLite base = new FastNoiseLite(1337);
 * base.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
 *
 * TurbulenceNoise turbulence = new TurbulenceNoise(base);
 *
 * // Classic Perlin turbulence
 * float turb = turbulence.perlinTurbulence(x, y, 4);
 *
 * // Curl noise for fluid-like motion
 * float[] curl2D = turbulence.curl2D(x, y);
 * float[] curl3D = turbulence.curl3D(x, y, z);
 *
 * // Warped turbulence
 * float warped = turbulence.warpedTurbulence(x, y, 4, 30f);
 * }</pre>
 *
 * <p><b>Extension:</b> This class is not part of the original FastNoiseLite library.
 */
public class TurbulenceNoise {

    private final FastNoiseLite noise;
    private float frequency = 0.01f;
    private float lacunarity = 2.0f;
    private float persistence = 0.5f;

    // Small epsilon for derivative calculation
    private static final float EPSILON = 0.001f;

    /**
     * Create turbulence noise with a base noise generator.
     *
     * @param noise Base FastNoiseLite instance
     */
    public TurbulenceNoise(FastNoiseLite noise) {
        this.noise = noise;
    }

    /**
     * Create turbulence noise with specified frequency.
     *
     * @param noise Base FastNoiseLite instance
     * @param frequency Base frequency for turbulence
     */
    public TurbulenceNoise(FastNoiseLite noise, float frequency) {
        this.noise = noise;
        this.frequency = frequency;
    }

    // ==================== Perlin Turbulence ====================

    /**
     * Classic Perlin turbulence (sum of absolute value noise).
     *
     * <p>Creates billowy, cloud-like patterns by summing the absolute
     * value of noise at multiple frequencies.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param octaves Number of octaves
     * @return Turbulence value in range [0, 1]
     */
    public float perlinTurbulence(float x, float y, int octaves) {
        float sum = 0f;
        float amp = 1f;
        float freq = frequency;
        float maxValue = 0f;

        int originalSeed = noise.GetSeed();

        for (int i = 0; i < octaves; i++) {
            noise.SetSeed(originalSeed + i * 31);
            float n = Math.abs(noise.GetNoise(x * freq, y * freq));
            sum += n * amp;
            maxValue += amp;

            freq *= lacunarity;
            amp *= persistence;
        }

        noise.SetSeed(originalSeed);
        return sum / maxValue;
    }

    /**
     * 3D Perlin turbulence.
     */
    public float perlinTurbulence(float x, float y, float z, int octaves) {
        float sum = 0f;
        float amp = 1f;
        float freq = frequency;
        float maxValue = 0f;

        int originalSeed = noise.GetSeed();

        for (int i = 0; i < octaves; i++) {
            noise.SetSeed(originalSeed + i * 31);
            float n = Math.abs(noise.GetNoise(x * freq, y * freq, z * freq));
            sum += n * amp;
            maxValue += amp;

            freq *= lacunarity;
            amp *= persistence;
        }

        noise.SetSeed(originalSeed);
        return sum / maxValue;
    }

    // ==================== Curl Noise ====================

    /**
     * 2D curl noise for incompressible flow fields.
     *
     * <p>Returns a 2D vector that is always perpendicular to the gradient
     * of the potential field, creating divergence-free flow.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @return float[2] containing (curl_x, curl_y)
     */
    public float[] curl2D(float x, float y) {
        // Compute partial derivatives using central differences
        float n1 = noise.GetNoise(x, y + EPSILON);
        float n2 = noise.GetNoise(x, y - EPSILON);
        float n3 = noise.GetNoise(x + EPSILON, y);
        float n4 = noise.GetNoise(x - EPSILON, y);

        float dndx = (n3 - n4) / (2 * EPSILON);
        float dndy = (n1 - n2) / (2 * EPSILON);

        // Curl is perpendicular to gradient
        return new float[] { dndy, -dndx };
    }

    /**
     * 3D curl noise for incompressible 3D flow fields.
     *
     * <p>Returns a 3D vector representing the curl of a potential field,
     * useful for fluid simulations, particle effects, and realistic flow.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @return float[3] containing (curl_x, curl_y, curl_z)
     */
    public float[] curl3D(float x, float y, float z) {
        // Sample noise at offset positions for gradient estimation
        // Using two independent noise fields for 3D curl

        int originalSeed = noise.GetSeed();

        // First potential field (for curl y and z components)
        noise.SetSeed(originalSeed);
        float p1_y1 = noise.GetNoise(x, y + EPSILON, z);
        float p1_y2 = noise.GetNoise(x, y - EPSILON, z);
        float p1_z1 = noise.GetNoise(x, y, z + EPSILON);
        float p1_z2 = noise.GetNoise(x, y, z - EPSILON);

        // Second potential field (for curl x and z components)
        noise.SetSeed(originalSeed + 1000);
        float p2_x1 = noise.GetNoise(x + EPSILON, y, z);
        float p2_x2 = noise.GetNoise(x - EPSILON, y, z);
        float p2_z1 = noise.GetNoise(x, y, z + EPSILON);
        float p2_z2 = noise.GetNoise(x, y, z - EPSILON);

        // Third potential field (for curl x and y components)
        noise.SetSeed(originalSeed + 2000);
        float p3_x1 = noise.GetNoise(x + EPSILON, y, z);
        float p3_x2 = noise.GetNoise(x - EPSILON, y, z);
        float p3_y1 = noise.GetNoise(x, y + EPSILON, z);
        float p3_y2 = noise.GetNoise(x, y - EPSILON, z);

        noise.SetSeed(originalSeed);

        // Compute curl components
        // curl_x = dPz/dy - dPy/dz
        float curlX = (p3_y1 - p3_y2) / (2 * EPSILON) - (p2_z1 - p2_z2) / (2 * EPSILON);

        // curl_y = dPx/dz - dPz/dx
        float curlY = (p1_z1 - p1_z2) / (2 * EPSILON) - (p3_x1 - p3_x2) / (2 * EPSILON);

        // curl_z = dPy/dx - dPx/dy
        float curlZ = (p2_x1 - p2_x2) / (2 * EPSILON) - (p1_y1 - p1_y2) / (2 * EPSILON);

        return new float[] { curlX, curlY, curlZ };
    }

    /**
     * Multi-octave curl noise for more detailed flow.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param octaves Number of octaves
     * @return float[2] containing summed curl vector
     */
    public float[] curlFBm2D(float x, float y, int octaves) {
        float curlX = 0f, curlY = 0f;
        float amp = 1f;
        float freq = frequency;
        float totalAmp = 0f;

        for (int i = 0; i < octaves; i++) {
            float[] c = curl2D(x * freq, y * freq);
            curlX += c[0] * amp;
            curlY += c[1] * amp;
            totalAmp += amp;

            freq *= lacunarity;
            amp *= persistence;
        }

        return new float[] { curlX / totalAmp, curlY / totalAmp };
    }

    /**
     * Multi-octave 3D curl noise.
     */
    public float[] curlFBm3D(float x, float y, float z, int octaves) {
        float curlX = 0f, curlY = 0f, curlZ = 0f;
        float amp = 1f;
        float freq = frequency;
        float totalAmp = 0f;

        for (int i = 0; i < octaves; i++) {
            float[] c = curl3D(x * freq, y * freq, z * freq);
            curlX += c[0] * amp;
            curlY += c[1] * amp;
            curlZ += c[2] * amp;
            totalAmp += amp;

            freq *= lacunarity;
            amp *= persistence;
        }

        return new float[] { curlX / totalAmp, curlY / totalAmp, curlZ / totalAmp };
    }

    // ==================== Warped Turbulence ====================

    /**
     * Turbulence with domain warping for organic distortion.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param octaves Number of turbulence octaves
     * @param warpAmplitude Amount of domain warping
     * @return Warped turbulence value
     */
    public float warpedTurbulence(float x, float y, int octaves, float warpAmplitude) {
        int originalSeed = noise.GetSeed();

        // First pass: generate warp offsets
        noise.SetSeed(originalSeed + 100);
        float warpX = noise.GetNoise(x * frequency, y * frequency) * warpAmplitude;
        noise.SetSeed(originalSeed + 200);
        float warpY = noise.GetNoise(x * frequency, y * frequency) * warpAmplitude;

        noise.SetSeed(originalSeed);

        // Second pass: sample turbulence at warped coordinates
        return perlinTurbulence(x + warpX, y + warpY, octaves);
    }

    /**
     * 3D warped turbulence.
     */
    public float warpedTurbulence(float x, float y, float z, int octaves, float warpAmplitude) {
        int originalSeed = noise.GetSeed();

        noise.SetSeed(originalSeed + 100);
        float warpX = noise.GetNoise(x * frequency, y * frequency, z * frequency) * warpAmplitude;
        noise.SetSeed(originalSeed + 200);
        float warpY = noise.GetNoise(x * frequency, y * frequency, z * frequency) * warpAmplitude;
        noise.SetSeed(originalSeed + 300);
        float warpZ = noise.GetNoise(x * frequency, y * frequency, z * frequency) * warpAmplitude;

        noise.SetSeed(originalSeed);

        return perlinTurbulence(x + warpX, y + warpY, z + warpZ, octaves);
    }

    // ==================== Marble/Wood Patterns ====================

    /**
     * Generate marble-like pattern using turbulence.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param octaves Turbulence octaves
     * @param turbulenceScale Amount of turbulence distortion
     * @return Value for marble pattern (apply sin for veins)
     */
    public float marble(float x, float y, int octaves, float turbulenceScale) {
        float turb = perlinTurbulence(x, y, octaves);
        return x * frequency + turb * turbulenceScale;
    }

    /**
     * Generate wood grain pattern using turbulence.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param octaves Turbulence octaves
     * @param ringFrequency Frequency of wood rings
     * @return Value for wood pattern (apply sin/fract for rings)
     */
    public float wood(float x, float y, int octaves, float ringFrequency) {
        float turb = perlinTurbulence(x, y, octaves);
        float dist = (float) Math.sqrt(x * x + y * y) * ringFrequency;
        return dist + turb * 5f;
    }

    // ==================== Configuration ====================

    /**
     * Set base frequency.
     */
    public TurbulenceNoise setFrequency(float frequency) {
        this.frequency = frequency;
        return this;
    }

    /**
     * Set lacunarity (frequency multiplier per octave).
     */
    public TurbulenceNoise setLacunarity(float lacunarity) {
        this.lacunarity = lacunarity;
        return this;
    }

    /**
     * Set persistence (amplitude multiplier per octave).
     */
    public TurbulenceNoise setPersistence(float persistence) {
        this.persistence = persistence;
        return this;
    }

    /**
     * Get base frequency.
     */
    public float getFrequency() {
        return frequency;
    }

    /**
     * Get lacunarity.
     */
    public float getLacunarity() {
        return lacunarity;
    }

    /**
     * Get persistence.
     */
    public float getPersistence() {
        return persistence;
    }

    /**
     * Get underlying noise generator.
     */
    public FastNoiseLite getNoise() {
        return noise;
    }
}
