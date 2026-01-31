package com.teamgannon.trips.noisegen.spatial;

/**
 * Sparse convolution noise generator.
 *
 * <p>Generates noise using the sparse convolution method: placing random
 * "impulses" at sparse locations and convolving with a smooth kernel.
 * This produces smooth, high-quality noise with predictable memory usage.
 *
 * <h2>Advantages over standard noise:</h2>
 * <ul>
 *   <li>Constant memory usage regardless of world size</li>
 *   <li>True infinite extent without tiling artifacts</li>
 *   <li>Controllable kernel shape for different effects</li>
 *   <li>Good for detail synthesis and particle-like features</li>
 * </ul>
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * SparseConvolutionNoise noise = new SparseConvolutionNoise(1337);
 *
 * // Sample smooth noise
 * float value = noise.getNoise(x, y);
 *
 * // FBm with sparse convolution
 * float fbmValue = noise.getFBm(x, y, 4, 2.0f, 0.5f);
 *
 * // Configure density (more impulses = more detail)
 * SparseConvolutionNoise dense = new SparseConvolutionNoise(1337, 0.3f, 2.0f);
 * }</pre>
 *
 * <p><b>Extension:</b> This class is not part of the original FastNoiseLite library.
 */
public class SparseConvolutionNoise {

    private final int seed;
    private final float density;      // Impulses per unit area
    private final float kernelRadius; // Influence radius of each impulse

    // Hash primes for pseudo-random impulse placement
    private static final int PRIME1 = 198491317;
    private static final int PRIME2 = 6542989;
    private static final int PRIME3 = 357239;
    private static final int PRIME4 = 1183186591;

    /**
     * Create sparse convolution noise with default parameters.
     *
     * @param seed Random seed
     */
    public SparseConvolutionNoise(int seed) {
        this(seed, 0.2f, 2.5f);
    }

    /**
     * Create sparse convolution noise with specified parameters.
     *
     * @param seed Random seed
     * @param density Impulses per unit area (0.1 = sparse, 0.5 = dense)
     * @param kernelRadius Influence radius of each impulse
     */
    public SparseConvolutionNoise(int seed, float density, float kernelRadius) {
        this.seed = seed;
        this.density = density;
        this.kernelRadius = kernelRadius;
    }

    /**
     * Get 2D noise at the specified coordinates.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @return Noise value in range approximately [-1, 1]
     */
    public float getNoise(float x, float y) {
        return sample2D(x, y);
    }

    /**
     * Get 3D noise at the specified coordinates.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @return Noise value in range approximately [-1, 1]
     */
    public float getNoise(float x, float y, float z) {
        return sample3D(x, y, z);
    }

    /**
     * Get 2D FBm (fractal Brownian motion) noise.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param octaves Number of octaves
     * @param lacunarity Frequency multiplier per octave
     * @param gain Amplitude multiplier per octave
     * @return Noise value in range approximately [-1, 1]
     */
    public float getFBm(float x, float y, int octaves, float lacunarity, float gain) {
        float value = 0f;
        float amplitude = 1f;
        float frequency = 1f;
        float maxValue = 0f;

        for (int i = 0; i < octaves; i++) {
            value += amplitude * sample2D(x * frequency, y * frequency);
            maxValue += amplitude;
            amplitude *= gain;
            frequency *= lacunarity;
        }

        return value / maxValue;
    }

    /**
     * Get 3D FBm noise.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @param octaves Number of octaves
     * @param lacunarity Frequency multiplier per octave
     * @param gain Amplitude multiplier per octave
     * @return Noise value in range approximately [-1, 1]
     */
    public float getFBm(float x, float y, float z, int octaves, float lacunarity, float gain) {
        float value = 0f;
        float amplitude = 1f;
        float frequency = 1f;
        float maxValue = 0f;

        for (int i = 0; i < octaves; i++) {
            value += amplitude * sample3D(x * frequency, y * frequency, z * frequency);
            maxValue += amplitude;
            amplitude *= gain;
            frequency *= lacunarity;
        }

        return value / maxValue;
    }

    /**
     * Get 2D noise with double-precision coordinates.
     *
     * @param x X coordinate (double precision)
     * @param y Y coordinate (double precision)
     * @return Noise value in range approximately [-1, 1]
     */
    public float getNoise(double x, double y) {
        // Handle double precision by using local coordinates
        double cellSize = kernelRadius * 2;
        double cellX = Math.floor(x / cellSize);
        double cellY = Math.floor(y / cellSize);

        float localX = (float) (x - cellX * cellSize);
        float localY = (float) (y - cellY * cellSize);

        // Offset seed based on cell for variation
        int cellSeed = hashCell2D((int) cellX, (int) cellY);

        return sample2DWithSeed(localX, localY, cellSeed);
    }

    /**
     * Sample sparse convolution noise at 2D coordinates.
     */
    private float sample2D(float x, float y) {
        return sample2DWithSeed(x, y, seed);
    }

    /**
     * Sample 2D noise with specific seed.
     */
    private float sample2DWithSeed(float x, float y, int localSeed) {
        float sum = 0f;

        // Determine which cells to check
        int cellRadius = (int) Math.ceil(kernelRadius);
        int cellX = (int) Math.floor(x);
        int cellY = (int) Math.floor(y);

        // Check surrounding cells for impulses
        for (int cy = cellY - cellRadius; cy <= cellY + cellRadius; cy++) {
            for (int cx = cellX - cellRadius; cx <= cellX + cellRadius; cx++) {
                sum += evaluateCell2D(x, y, cx, cy, localSeed);
            }
        }

        // Normalize to approximately [-1, 1]
        return sum * 0.5f;
    }

    /**
     * Sample sparse convolution noise at 3D coordinates.
     */
    private float sample3D(float x, float y, float z) {
        float sum = 0f;

        int cellRadius = (int) Math.ceil(kernelRadius);
        int cellX = (int) Math.floor(x);
        int cellY = (int) Math.floor(y);
        int cellZ = (int) Math.floor(z);

        // Check surrounding cells for impulses
        for (int cz = cellZ - cellRadius; cz <= cellZ + cellRadius; cz++) {
            for (int cy = cellY - cellRadius; cy <= cellY + cellRadius; cy++) {
                for (int cx = cellX - cellRadius; cx <= cellX + cellRadius; cx++) {
                    sum += evaluateCell3D(x, y, z, cx, cy, cz);
                }
            }
        }

        // Normalize to approximately [-1, 1]
        return sum * 0.3f;
    }

    /**
     * Evaluate contribution from a single 2D cell.
     */
    private float evaluateCell2D(float x, float y, int cellX, int cellY, int localSeed) {
        // Hash to determine number of impulses in this cell
        int cellHash = hash2D(cellX, cellY, localSeed);
        int numImpulses = poissonCount(cellHash, density);

        if (numImpulses == 0) {
            return 0f;
        }

        float contribution = 0f;

        // Evaluate each impulse
        for (int i = 0; i < numImpulses; i++) {
            int impulseHash = hash2D(cellX, cellY, localSeed + i * PRIME4);

            // Random position within cell
            float ix = cellX + hashToFloat(impulseHash);
            float iy = cellY + hashToFloat(impulseHash * PRIME1);

            // Random amplitude (-1 to 1)
            float amplitude = hashToFloat(impulseHash * PRIME2) * 2f - 1f;

            // Distance from sample point to impulse
            float dx = x - ix;
            float dy = y - iy;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);

            // Apply kernel
            contribution += amplitude * kernel(dist / kernelRadius);
        }

        return contribution;
    }

    /**
     * Evaluate contribution from a single 3D cell.
     */
    private float evaluateCell3D(float x, float y, float z, int cellX, int cellY, int cellZ) {
        int cellHash = hash3D(cellX, cellY, cellZ, seed);
        int numImpulses = poissonCount(cellHash, density);

        if (numImpulses == 0) {
            return 0f;
        }

        float contribution = 0f;

        for (int i = 0; i < numImpulses; i++) {
            int impulseHash = hash3D(cellX, cellY, cellZ, seed + i * PRIME4);

            float ix = cellX + hashToFloat(impulseHash);
            float iy = cellY + hashToFloat(impulseHash * PRIME1);
            float iz = cellZ + hashToFloat(impulseHash * PRIME2);

            float amplitude = hashToFloat(impulseHash * PRIME3) * 2f - 1f;

            float dx = x - ix;
            float dy = y - iy;
            float dz = z - iz;
            float dist = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);

            contribution += amplitude * kernel(dist / kernelRadius);
        }

        return contribution;
    }

    /**
     * Smooth kernel function (Wendland C2).
     * Has compact support (returns 0 for r >= 1).
     */
    private float kernel(float r) {
        if (r >= 1f) {
            return 0f;
        }
        // Wendland C2: (1-r)^4 * (4r + 1)
        float t = 1f - r;
        float t2 = t * t;
        return t2 * t2 * (4f * r + 1f);
    }

    /**
     * Approximate Poisson-distributed count based on hash.
     */
    private int poissonCount(int hash, float mean) {
        // Simple approximation: use hash to sample from Poisson-like distribution
        float u = hashToFloat(hash);

        // Inverse CDF of Poisson (approximation)
        int count = 0;
        float p = (float) Math.exp(-mean);
        float cdf = p;

        while (u > cdf && count < 10) {
            count++;
            p *= mean / count;
            cdf += p;
        }

        return count;
    }

    /**
     * Hash a 2D cell coordinate.
     */
    private int hash2D(int x, int y, int localSeed) {
        int h = localSeed;
        h ^= x * PRIME1;
        h ^= y * PRIME2;
        h = ((h >> 16) ^ h) * 0x45d9f3b;
        h = ((h >> 16) ^ h) * 0x45d9f3b;
        h = (h >> 16) ^ h;
        return h;
    }

    /**
     * Hash for cell mixing with double precision support.
     */
    private int hashCell2D(int cellX, int cellY) {
        return hash2D(cellX, cellY, seed * 31 + 17);
    }

    /**
     * Hash a 3D cell coordinate.
     */
    private int hash3D(int x, int y, int z, int localSeed) {
        int h = localSeed;
        h ^= x * PRIME1;
        h ^= y * PRIME2;
        h ^= z * PRIME3;
        h = ((h >> 16) ^ h) * 0x45d9f3b;
        h = ((h >> 16) ^ h) * 0x45d9f3b;
        h = (h >> 16) ^ h;
        return h;
    }

    /**
     * Convert hash to float in [0, 1).
     */
    private float hashToFloat(int hash) {
        return (hash & 0x7FFFFFFF) / (float) Integer.MAX_VALUE;
    }

    /**
     * Get the seed.
     */
    public int getSeed() {
        return seed;
    }

    /**
     * Get the density.
     */
    public float getDensity() {
        return density;
    }

    /**
     * Get the kernel radius.
     */
    public float getKernelRadius() {
        return kernelRadius;
    }

    /**
     * Create a new instance with different density.
     *
     * @param newDensity New density value
     * @return New SparseConvolutionNoise with the specified density
     */
    public SparseConvolutionNoise withDensity(float newDensity) {
        return new SparseConvolutionNoise(seed, newDensity, kernelRadius);
    }

    /**
     * Create a new instance with different kernel radius.
     *
     * @param newRadius New kernel radius
     * @return New SparseConvolutionNoise with the specified radius
     */
    public SparseConvolutionNoise withKernelRadius(float newRadius) {
        return new SparseConvolutionNoise(seed, density, newRadius);
    }
}
