package com.teamgannon.trips.noisegen.generators;

// MIT License
//
// Copyright(c) 2023 Jordan Peck (jordan.me2@gmail.com)
// Copyright(c) 2023 Contributors

/**
 * Interface for noise generation algorithms.
 * Implementations provide 2D and 3D noise sampling at single points.
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
}
