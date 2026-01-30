package com.teamgannon.trips.particlefields;

/**
 * Simple 3D noise generator for procedural nebula structure.
 * <p>
 * Uses a hash-based approach for fast, deterministic noise generation.
 * Supports layered octaves for fractal-like detail at multiple scales.
 * <p>
 * This is intentionally simple and fast rather than high-quality.
 * For interstellar-scale nebulae, the noise doesn't need to be smooth
 * since individual particles are not closely examined.
 */
public class NoiseGenerator {

    // Large primes for hashing (spatial locality mixing)
    private static final long PRIME_X = 73856093L;
    private static final long PRIME_Y = 19349663L;
    private static final long PRIME_Z = 83492791L;
    private static final long HASH_MULT = 0x27d4eb2dL;

    private final long seed;

    /**
     * Creates a noise generator with the given seed.
     *
     * @param seed random seed for reproducible noise
     */
    public NoiseGenerator(long seed) {
        this.seed = seed;
    }

    /**
     * Generate a single noise value at the given 3D position.
     * Returns a value in the range [-1, 1].
     *
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     * @return noise value in [-1, 1]
     */
    public double noise3D(double x, double y, double z) {
        // Hash the coordinates
        long ix = Double.doubleToLongBits(x);
        long iy = Double.doubleToLongBits(y);
        long iz = Double.doubleToLongBits(z);

        long h = seed + ix * PRIME_X + iy * PRIME_Y + iz * PRIME_Z;
        h = (h ^ (h >> 13)) * HASH_MULT;
        h = h ^ (h >> 15);

        // Convert to [-1, 1] range
        return (h & 0xFFFFFFFFL) / (double) 0x80000000L - 1.0;
    }

    /**
     * Generate layered noise with multiple octaves for fractal-like detail.
     * Each octave adds finer detail at higher frequency but lower amplitude.
     *
     * @param x         x coordinate
     * @param y         y coordinate
     * @param z         z coordinate
     * @param octaves   number of layers (1-6 typical)
     * @param frequency initial frequency multiplier
     * @return combined noise value (normalized to approximately [-1, 1])
     */
    public double layeredNoise(double x, double y, double z, int octaves, double frequency) {
        double total = 0;
        double amplitude = 1.0;
        double maxValue = 0;  // For normalization
        double persistence = 0.5;  // Amplitude decay per octave
        double lacunarity = 2.2;   // Frequency increase per octave

        for (int i = 0; i < octaves; i++) {
            total += noise3D(x * frequency, y * frequency, z * frequency) * amplitude;
            maxValue += amplitude;
            amplitude *= persistence;
            frequency *= lacunarity;
        }

        // Normalize to approximately [-1, 1]
        return total / maxValue;
    }

    /**
     * Generate layered noise with default frequency.
     */
    public double layeredNoise(double x, double y, double z, int octaves) {
        return layeredNoise(x, y, z, octaves, 1.0);
    }

    /**
     * Generate turbulence (absolute value of layered noise).
     * Creates sharp ridges and valleys useful for filamentary structure.
     *
     * @param x       x coordinate
     * @param y       y coordinate
     * @param z       z coordinate
     * @param octaves number of layers
     * @return turbulence value in [0, 1]
     */
    public double turbulence(double x, double y, double z, int octaves) {
        double total = 0;
        double amplitude = 1.0;
        double maxValue = 0;
        double frequency = 1.0;
        double persistence = 0.5;
        double lacunarity = 2.0;

        for (int i = 0; i < octaves; i++) {
            total += Math.abs(noise3D(x * frequency, y * frequency, z * frequency)) * amplitude;
            maxValue += amplitude;
            amplitude *= persistence;
            frequency *= lacunarity;
        }

        return total / maxValue;
    }

    /**
     * Generate displacement vector for a point in space.
     * Used to create filamentary structure by displacing particle positions.
     *
     * @param x             x coordinate
     * @param y             y coordinate
     * @param z             z coordinate
     * @param octaves       number of noise layers
     * @param strength      displacement strength (0.0 - 1.0)
     * @param anisotropy    anisotropic factors [xFactor, yFactor, zFactor]
     *                      to create directional filaments
     * @return displacement vector [dx, dy, dz]
     */
    public double[] displacement(double x, double y, double z,
                                   int octaves, double strength,
                                   double[] anisotropy) {
        // Use different seed offsets for each axis to decorrelate
        double nx = layeredNoise(x, y, z, octaves);
        double ny = layeredNoise(x + 1000, y + 1000, z + 1000, octaves);
        double nz = layeredNoise(x + 2000, y + 2000, z + 2000, octaves);

        return new double[]{
                nx * strength * anisotropy[0],
                ny * strength * anisotropy[1],
                nz * strength * anisotropy[2]
        };
    }

    /**
     * Generate displacement with default isotropic behavior.
     */
    public double[] displacement(double x, double y, double z, int octaves, double strength) {
        return displacement(x, y, z, octaves, strength, new double[]{1.0, 1.0, 1.0});
    }

    /**
     * Generate displacement for filamentary nebula structure.
     * Uses anisotropic factors to create elongated filaments.
     *
     * @param x        x coordinate
     * @param y        y coordinate
     * @param z        z coordinate
     * @param octaves  number of noise layers
     * @param strength displacement strength
     * @return displacement vector [dx, dy, dz]
     */
    public double[] filamentDisplacement(double x, double y, double z,
                                           int octaves, double strength) {
        // Anisotropic factors create elongated filaments
        // Higher x factor, lower y/z creates horizontal streaks
        return displacement(x, y, z, octaves, strength, new double[]{1.0, 0.7, 0.4});
    }

    /**
     * Apply density threshold based on noise.
     * Returns true if the point should be kept based on noise-modulated density.
     *
     * @param x            x coordinate
     * @param y            y coordinate
     * @param z            z coordinate
     * @param baseDensity  base density (0.0 - 1.0)
     * @param noiseInfluence how much noise affects density (0.0 - 1.0)
     * @return true if point passes density threshold
     */
    public boolean densityThreshold(double x, double y, double z,
                                     double baseDensity, double noiseInfluence) {
        double noise = (layeredNoise(x * 0.5, y * 0.5, z * 0.5, 3) + 1) * 0.5; // [0, 1]
        double threshold = baseDensity + (noise - 0.5) * noiseInfluence * 2;
        threshold = Math.max(0, Math.min(1, threshold));

        // Use different noise sample for random test
        double test = (noise3D(x * 7.3, y * 7.3, z * 7.3) + 1) * 0.5;
        return test < threshold;
    }

    /**
     * Calculate noise-based opacity modifier.
     * Useful for creating density variations within nebulae.
     *
     * @param x       x coordinate
     * @param y       y coordinate
     * @param z       z coordinate
     * @param minOpacity minimum opacity (0.0 - 1.0)
     * @param maxOpacity maximum opacity (0.0 - 1.0)
     * @return opacity value in [minOpacity, maxOpacity]
     */
    public double opacityModifier(double x, double y, double z,
                                    double minOpacity, double maxOpacity) {
        double noise = (layeredNoise(x * 0.3, y * 0.3, z * 0.3, 2) + 1) * 0.5;
        return minOpacity + noise * (maxOpacity - minOpacity);
    }
}
