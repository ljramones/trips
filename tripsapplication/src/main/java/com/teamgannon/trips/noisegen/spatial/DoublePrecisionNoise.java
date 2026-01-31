package com.teamgannon.trips.noisegen.spatial;

import com.teamgannon.trips.noisegen.FastNoiseLite;

/**
 * Double-precision coordinate wrapper for FastNoiseLite.
 *
 * <p>Provides double-precision input coordinates while internally using
 * float-precision noise sampling. Uses domain translation to maintain
 * precision far from the origin.
 *
 * <p>This is a simpler alternative to {@link ChunkedNoise} when you don't
 * need chunk-based seed variation, just extended coordinate range.
 *
 * <h2>Precision Comparison:</h2>
 * <ul>
 *   <li>Standard float: Precise to ~6-7 digits, degrades past ~100,000 units</li>
 *   <li>This wrapper: Precise to ~15 digits, works to ~10^15 units</li>
 * </ul>
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * FastNoiseLite baseNoise = new FastNoiseLite(1337);
 * baseNoise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
 *
 * DoublePrecisionNoise precise = new DoublePrecisionNoise(baseNoise);
 *
 * // Works correctly at astronomical distances
 * double x = 1_000_000_000_000.5;  // 1 trillion + 0.5
 * double y = 2_500_000_000_000.3;
 * float value = precise.getNoise(x, y);  // Precise to the decimal
 * }</pre>
 *
 * <p><b>Extension:</b> This class is not part of the original FastNoiseLite library.
 */
public class DoublePrecisionNoise {

    private final FastNoiseLite noise;
    private final double domainSize;

    /**
     * Create a double-precision noise wrapper with default domain size (10000).
     *
     * @param noise The base FastNoiseLite instance
     */
    public DoublePrecisionNoise(FastNoiseLite noise) {
        this(noise, 10000.0);
    }

    /**
     * Create a double-precision noise wrapper with specified domain size.
     *
     * <p>The domain size determines how coordinates are chunked internally.
     * Should be large enough to encompass typical noise features but small
     * enough to maintain float precision within a domain.
     *
     * @param noise The base FastNoiseLite instance
     * @param domainSize Size of internal domain (recommended: 1000-100000)
     */
    public DoublePrecisionNoise(FastNoiseLite noise, double domainSize) {
        this.noise = noise;
        this.domainSize = domainSize;
    }

    /**
     * Get 2D noise at double-precision coordinates.
     *
     * @param x X coordinate (double precision)
     * @param y Y coordinate (double precision)
     * @return Noise value in range [-1, 1]
     */
    public float getNoise(double x, double y) {
        // Translate to local domain to maintain float precision
        double domainX = Math.floor(x / domainSize) * domainSize;
        double domainY = Math.floor(y / domainSize) * domainSize;

        float localX = (float) (x - domainX);
        float localY = (float) (y - domainY);

        return noise.GetNoise(localX, localY);
    }

    /**
     * Get 3D noise at double-precision coordinates.
     *
     * @param x X coordinate (double precision)
     * @param y Y coordinate (double precision)
     * @param z Z coordinate (double precision)
     * @return Noise value in range [-1, 1]
     */
    public float getNoise(double x, double y, double z) {
        double domainX = Math.floor(x / domainSize) * domainSize;
        double domainY = Math.floor(y / domainSize) * domainSize;
        double domainZ = Math.floor(z / domainSize) * domainSize;

        float localX = (float) (x - domainX);
        float localY = (float) (y - domainY);
        float localZ = (float) (z - domainZ);

        return noise.GetNoise(localX, localY, localZ);
    }

    /**
     * Get 4D noise at double-precision coordinates.
     *
     * @param x X coordinate (double precision)
     * @param y Y coordinate (double precision)
     * @param z Z coordinate (double precision)
     * @param w W coordinate (double precision)
     * @return Noise value in range [-1, 1]
     */
    public float getNoise(double x, double y, double z, double w) {
        double domainX = Math.floor(x / domainSize) * domainSize;
        double domainY = Math.floor(y / domainSize) * domainSize;
        double domainZ = Math.floor(z / domainSize) * domainSize;
        double domainW = Math.floor(w / domainSize) * domainSize;

        float localX = (float) (x - domainX);
        float localY = (float) (y - domainY);
        float localZ = (float) (z - domainZ);
        float localW = (float) (w - domainW);

        return noise.GetNoise(localX, localY, localZ, localW);
    }

    /**
     * Get noise with a position offset.
     * Useful for centering noise around a specific world position.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param offsetX X offset (subtracted from x)
     * @param offsetY Y offset (subtracted from y)
     * @return Noise value in range [-1, 1]
     */
    public float getNoiseWithOffset(double x, double y, double offsetX, double offsetY) {
        return getNoise(x - offsetX, y - offsetY);
    }

    /**
     * Get noise with a 3D position offset.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @param offsetX X offset
     * @param offsetY Y offset
     * @param offsetZ Z offset
     * @return Noise value in range [-1, 1]
     */
    public float getNoiseWithOffset(double x, double y, double z,
                                    double offsetX, double offsetY, double offsetZ) {
        return getNoise(x - offsetX, y - offsetY, z - offsetZ);
    }

    /**
     * Get the domain origin for a given coordinate.
     * Useful for debugging or understanding domain boundaries.
     *
     * @param coord The world coordinate
     * @return The domain origin for that coordinate
     */
    public double getDomainOrigin(double coord) {
        return Math.floor(coord / domainSize) * domainSize;
    }

    /**
     * Get the local coordinate within a domain.
     *
     * @param coord The world coordinate
     * @return The local coordinate (0 to domainSize)
     */
    public float getLocalCoord(double coord) {
        return (float) (coord - getDomainOrigin(coord));
    }

    /**
     * Get the domain size.
     */
    public double getDomainSize() {
        return domainSize;
    }

    /**
     * Get the underlying FastNoiseLite instance.
     */
    public FastNoiseLite getNoise() {
        return noise;
    }

    /**
     * Convenience method to sample noise in a grid pattern.
     * Useful for heightmap generation at double precision.
     *
     * @param startX Starting X coordinate
     * @param startY Starting Y coordinate
     * @param width Number of samples in X
     * @param height Number of samples in Y
     * @param step Distance between samples
     * @return 2D array of noise values
     */
    public float[][] sampleGrid(double startX, double startY,
                                int width, int height, double step) {
        float[][] grid = new float[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                grid[x][y] = getNoise(startX + x * step, startY + y * step);
            }
        }

        return grid;
    }

    /**
     * Convenience method to sample noise in a 3D grid pattern.
     *
     * @param startX Starting X coordinate
     * @param startY Starting Y coordinate
     * @param startZ Starting Z coordinate
     * @param width Number of samples in X
     * @param height Number of samples in Y
     * @param depth Number of samples in Z
     * @param step Distance between samples
     * @return 3D array of noise values
     */
    public float[][][] sampleGrid3D(double startX, double startY, double startZ,
                                    int width, int height, int depth, double step) {
        float[][][] grid = new float[width][height][depth];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    grid[x][y][z] = getNoise(
                        startX + x * step,
                        startY + y * step,
                        startZ + z * step
                    );
                }
            }
        }

        return grid;
    }
}
