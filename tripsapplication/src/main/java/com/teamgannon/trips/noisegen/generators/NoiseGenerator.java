package com.teamgannon.trips.noisegen.generators;

// MIT License
//
// Copyright(c) 2023 Jordan Peck (jordan.me2@gmail.com)
// Copyright(c) 2023 Contributors

/**
 * Interface for noise generation algorithms.
 * Implementations provide 2D, 3D, and optionally 4D noise sampling at single points.
 */
public interface NoiseGenerator {

    /**
     * Generate noise at a 2D coordinate.
     *
     * @param seed The random seed
     * @param x X coordinate
     * @param y Y coordinate
     * @return Noise value typically in range [-1, 1]
     */
    float single2D(int seed, float x, float y);

    /**
     * Generate noise at a 3D coordinate.
     *
     * @param seed The random seed
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @return Noise value typically in range [-1, 1]
     */
    float single3D(int seed, float x, float y, float z);

    /**
     * Generate noise at a 4D coordinate.
     * Default implementation throws UnsupportedOperationException.
     * Override in implementations that support 4D noise.
     *
     * @param seed The random seed
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @param w W coordinate (often used for time-based animation)
     * @return Noise value typically in range [-1, 1]
     */
    default float single4D(int seed, float x, float y, float z, float w) {
        throw new UnsupportedOperationException("4D noise not supported by this generator");
    }

    /**
     * Check if this generator supports 4D noise.
     *
     * @return true if single4D is implemented
     */
    default boolean supports4D() {
        return false;
    }
}
