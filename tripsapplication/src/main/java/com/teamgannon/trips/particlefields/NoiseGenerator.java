package com.teamgannon.trips.particlefields;

/**
 * Enhanced 3D noise generator for procedural nebula structure.
 * <p>
 * Uses a hash-based approach for fast, deterministic noise generation.
 * Supports layered octaves for fractal-like detail at multiple scales.
 * <p>
 * Features:
 * <ul>
 *   <li>Configurable persistence and lacunarity</li>
 *   <li>Ridged noise for sharp filaments</li>
 *   <li>Multi-scale filament displacement</li>
 *   <li>Anisotropic stretching for directional structures</li>
 * </ul>
 */
public class NoiseGenerator {

    // Large primes for hashing (spatial locality mixing)
    private static final long PRIME_X = 73856093L;
    private static final long PRIME_Y = 19349663L;
    private static final long PRIME_Z = 83492791L;
    private static final long HASH_MULT = 0x27d4eb2dL;

    private final long seed;

    // Configurable noise parameters
    private double persistence = 0.5;     // Amplitude decay per octave (0.0-1.0)
    private double lacunarity = 2.2;      // Frequency multiplier per octave (1.0-3.0)

    /**
     * Creates a noise generator with the given seed.
     *
     * @param seed random seed for reproducible noise
     */
    public NoiseGenerator(long seed) {
        this.seed = seed;
    }

    /**
     * Creates a noise generator with configurable parameters.
     *
     * @param seed        random seed for reproducible noise
     * @param persistence amplitude decay per octave (0.0-1.0, default 0.5)
     * @param lacunarity  frequency multiplier per octave (1.0-3.0, default 2.2)
     */
    public NoiseGenerator(long seed, double persistence, double lacunarity) {
        this.seed = seed;
        this.persistence = Math.max(0.1, Math.min(1.0, persistence));
        this.lacunarity = Math.max(1.0, Math.min(4.0, lacunarity));
    }

    /**
     * Sets the persistence (amplitude decay per octave).
     * Lower values = smoother, higher values = more detail preserved.
     *
     * @param persistence value between 0.0 and 1.0
     */
    public void setPersistence(double persistence) {
        this.persistence = Math.max(0.1, Math.min(1.0, persistence));
    }

    /**
     * Sets the lacunarity (frequency multiplier per octave).
     * Higher values = more rapid frequency increase = finer detail.
     *
     * @param lacunarity value between 1.0 and 4.0
     */
    public void setLacunarity(double lacunarity) {
        this.lacunarity = Math.max(1.0, Math.min(4.0, lacunarity));
    }

    public double getPersistence() {
        return persistence;
    }

    public double getLacunarity() {
        return lacunarity;
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
     * Uses the configured persistence and lacunarity values.
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
        double freq = frequency;

        for (int i = 0; i < octaves; i++) {
            total += noise3D(x * freq, y * freq, z * freq) * amplitude;
            maxValue += amplitude;
            amplitude *= persistence;
            freq *= lacunarity;
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

        for (int i = 0; i < octaves; i++) {
            total += Math.abs(noise3D(x * frequency, y * frequency, z * frequency)) * amplitude;
            maxValue += amplitude;
            amplitude *= persistence;
            frequency *= lacunarity;
        }

        return total / maxValue;
    }

    /**
     * Generate ridged noise for sharp, dramatic filaments.
     * Ridged noise inverts the absolute value to create sharp ridges.
     *
     * @param x       x coordinate
     * @param y       y coordinate
     * @param z       z coordinate
     * @param octaves number of layers
     * @return ridged noise value in [0, 1]
     */
    public double ridgedNoise(double x, double y, double z, int octaves) {
        double total = 0;
        double amplitude = 1.0;
        double maxValue = 0;
        double frequency = 1.0;
        double weight = 1.0;

        for (int i = 0; i < octaves; i++) {
            // Get absolute noise and invert it (1 - |noise|)
            double signal = 1.0 - Math.abs(noise3D(x * frequency, y * frequency, z * frequency));
            // Square the signal to sharpen the ridges
            signal = signal * signal;
            // Weight by previous amplitude for self-similar ridges
            signal *= weight;
            weight = Math.min(1.0, signal * 2.0);

            total += signal * amplitude;
            maxValue += amplitude;
            amplitude *= persistence;
            frequency *= lacunarity;
        }

        return total / maxValue;
    }

    /**
     * Generate multi-scale filament displacement with both coarse and fine detail.
     * Combines low-frequency large displacement with high-frequency fine detail.
     *
     * @param x              x coordinate
     * @param y              y coordinate
     * @param z              z coordinate
     * @param octaves        number of noise layers for fine detail
     * @param strength       overall displacement strength
     * @param coarseWeight   weight of coarse displacement (0.0-1.0, default 0.7)
     * @param fineWeight     weight of fine displacement (0.0-1.0, default 0.3)
     * @param anisotropy     anisotropic factors [xFactor, yFactor, zFactor]
     * @return displacement vector [dx, dy, dz]
     */
    public double[] multiScaleFilamentDisplacement(double x, double y, double z,
                                                    int octaves, double strength,
                                                    double coarseWeight, double fineWeight,
                                                    double[] anisotropy) {
        // Coarse displacement (low frequency, large scale)
        double coarseScale = 0.3;
        double nxCoarse = layeredNoise(x * coarseScale, y * coarseScale, z * coarseScale, 2, 1.0);
        double nyCoarse = layeredNoise((x + 1000) * coarseScale, (y + 1000) * coarseScale, (z + 1000) * coarseScale, 2, 1.0);
        double nzCoarse = layeredNoise((x + 2000) * coarseScale, (y + 2000) * coarseScale, (z + 2000) * coarseScale, 2, 1.0);

        // Fine displacement (high frequency, small scale)
        double fineScale = 1.5;
        double nxFine = ridgedNoise(x * fineScale, y * fineScale, z * fineScale, octaves);
        double nyFine = ridgedNoise((x + 1000) * fineScale, (y + 1000) * fineScale, (z + 1000) * fineScale, octaves);
        double nzFine = ridgedNoise((x + 2000) * fineScale, (y + 2000) * fineScale, (z + 2000) * fineScale, octaves);

        // Combine with weights
        double nx = nxCoarse * coarseWeight + (nxFine * 2 - 1) * fineWeight;
        double ny = nyCoarse * coarseWeight + (nyFine * 2 - 1) * fineWeight;
        double nz = nzCoarse * coarseWeight + (nzFine * 2 - 1) * fineWeight;

        return new double[]{
                nx * strength * anisotropy[0],
                ny * strength * anisotropy[1],
                nz * strength * anisotropy[2]
        };
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
