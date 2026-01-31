package com.teamgannon.trips.noisegen.spatial;

import com.teamgannon.trips.noisegen.FastNoiseLite;

/**
 * Chunked noise wrapper for infinite worlds without float precision issues.
 *
 * <p>Automatically divides world space into chunks, using local coordinates
 * within each chunk combined with chunk-based seed mixing. This avoids
 * float precision degradation far from the origin.
 *
 * <p>Without chunking, float precision degrades around 100,000+ units from origin.
 * With chunking, you can have coordinates in the billions with consistent quality.
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * FastNoiseLite baseNoise = new FastNoiseLite(1337);
 * baseNoise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
 * baseNoise.SetFractalType(FastNoiseLite.FractalType.FBm);
 * baseNoise.SetFractalOctaves(6);
 *
 * ChunkedNoise chunked = new ChunkedNoise(baseNoise, 1000.0); // 1000 unit chunks
 *
 * // Works correctly even at huge coordinates
 * double worldX = 1_000_000_000.0;
 * double worldY = 2_500_000_000.0;
 * float value = chunked.getNoise(worldX, worldY);
 * }</pre>
 *
 * <p><b>Extension:</b> This class is not part of the original FastNoiseLite library.
 */
public class ChunkedNoise {

    private final FastNoiseLite noise;
    private final double chunkSize;
    private final int baseSeed;

    // Prime numbers for seed mixing (different from noise primes to avoid correlation)
    private static final int PRIME_X = 73856093;
    private static final int PRIME_Y = 19349663;
    private static final int PRIME_Z = 83492791;
    private static final int PRIME_W = 39916801;

    /**
     * Create a chunked noise wrapper with default chunk size (1000 units).
     *
     * @param noise The base FastNoiseLite instance to use
     */
    public ChunkedNoise(FastNoiseLite noise) {
        this(noise, 1000.0);
    }

    /**
     * Create a chunked noise wrapper with specified chunk size.
     *
     * @param noise The base FastNoiseLite instance to use
     * @param chunkSize Size of each chunk in world units (recommended: 100-10000)
     */
    public ChunkedNoise(FastNoiseLite noise, double chunkSize) {
        this.noise = noise;
        this.chunkSize = chunkSize;
        this.baseSeed = 1337; // Will be combined with chunk coordinates
    }

    /**
     * Create a chunked noise wrapper with specified chunk size and base seed.
     *
     * @param noise The base FastNoiseLite instance to use
     * @param chunkSize Size of each chunk in world units
     * @param baseSeed Base seed to combine with chunk coordinates
     */
    public ChunkedNoise(FastNoiseLite noise, double chunkSize, int baseSeed) {
        this.noise = noise;
        this.chunkSize = chunkSize;
        this.baseSeed = baseSeed;
    }

    /**
     * Get 2D noise at double-precision world coordinates.
     *
     * @param worldX X coordinate in world space (can be very large)
     * @param worldY Y coordinate in world space (can be very large)
     * @return Noise value in range [-1, 1]
     */
    public float getNoise(double worldX, double worldY) {
        // Calculate chunk coordinates
        int chunkX = (int) Math.floor(worldX / chunkSize);
        int chunkY = (int) Math.floor(worldY / chunkSize);

        // Calculate local coordinates within chunk
        float localX = (float) (worldX - (chunkX * chunkSize));
        float localY = (float) (worldY - (chunkY * chunkSize));

        // Mix seed with chunk coordinates
        int chunkSeed = mixSeed(baseSeed, chunkX, chunkY, 0, 0);
        noise.SetSeed(chunkSeed);

        return noise.GetNoise(localX, localY);
    }

    /**
     * Get 3D noise at double-precision world coordinates.
     *
     * @param worldX X coordinate in world space
     * @param worldY Y coordinate in world space
     * @param worldZ Z coordinate in world space
     * @return Noise value in range [-1, 1]
     */
    public float getNoise(double worldX, double worldY, double worldZ) {
        int chunkX = (int) Math.floor(worldX / chunkSize);
        int chunkY = (int) Math.floor(worldY / chunkSize);
        int chunkZ = (int) Math.floor(worldZ / chunkSize);

        float localX = (float) (worldX - (chunkX * chunkSize));
        float localY = (float) (worldY - (chunkY * chunkSize));
        float localZ = (float) (worldZ - (chunkZ * chunkSize));

        int chunkSeed = mixSeed(baseSeed, chunkX, chunkY, chunkZ, 0);
        noise.SetSeed(chunkSeed);

        return noise.GetNoise(localX, localY, localZ);
    }

    /**
     * Get 4D noise at double-precision world coordinates.
     *
     * @param worldX X coordinate in world space
     * @param worldY Y coordinate in world space
     * @param worldZ Z coordinate in world space
     * @param worldW W coordinate in world space
     * @return Noise value in range [-1, 1]
     */
    public float getNoise(double worldX, double worldY, double worldZ, double worldW) {
        int chunkX = (int) Math.floor(worldX / chunkSize);
        int chunkY = (int) Math.floor(worldY / chunkSize);
        int chunkZ = (int) Math.floor(worldZ / chunkSize);
        int chunkW = (int) Math.floor(worldW / chunkSize);

        float localX = (float) (worldX - (chunkX * chunkSize));
        float localY = (float) (worldY - (chunkY * chunkSize));
        float localZ = (float) (worldZ - (chunkZ * chunkSize));
        float localW = (float) (worldW - (chunkW * chunkSize));

        int chunkSeed = mixSeed(baseSeed, chunkX, chunkY, chunkZ, chunkW);
        noise.SetSeed(chunkSeed);

        return noise.GetNoise(localX, localY, localZ, localW);
    }

    /**
     * Get chunk coordinates for a world position.
     * Useful for chunk-based world generation systems.
     *
     * @param worldX X coordinate in world space
     * @param worldY Y coordinate in world space
     * @return Array of [chunkX, chunkY]
     */
    public int[] getChunkCoords(double worldX, double worldY) {
        return new int[] {
            (int) Math.floor(worldX / chunkSize),
            (int) Math.floor(worldY / chunkSize)
        };
    }

    /**
     * Get chunk coordinates for a 3D world position.
     *
     * @param worldX X coordinate in world space
     * @param worldY Y coordinate in world space
     * @param worldZ Z coordinate in world space
     * @return Array of [chunkX, chunkY, chunkZ]
     */
    public int[] getChunkCoords(double worldX, double worldY, double worldZ) {
        return new int[] {
            (int) Math.floor(worldX / chunkSize),
            (int) Math.floor(worldY / chunkSize),
            (int) Math.floor(worldZ / chunkSize)
        };
    }

    /**
     * Get the seed that would be used for a specific chunk.
     * Useful for debugging or pre-generating chunk data.
     *
     * @param chunkX Chunk X coordinate
     * @param chunkY Chunk Y coordinate
     * @return The mixed seed for this chunk
     */
    public int getChunkSeed(int chunkX, int chunkY) {
        return mixSeed(baseSeed, chunkX, chunkY, 0, 0);
    }

    /**
     * Get the seed that would be used for a specific 3D chunk.
     *
     * @param chunkX Chunk X coordinate
     * @param chunkY Chunk Y coordinate
     * @param chunkZ Chunk Z coordinate
     * @return The mixed seed for this chunk
     */
    public int getChunkSeed(int chunkX, int chunkY, int chunkZ) {
        return mixSeed(baseSeed, chunkX, chunkY, chunkZ, 0);
    }

    /**
     * Get the chunk size.
     *
     * @return Chunk size in world units
     */
    public double getChunkSize() {
        return chunkSize;
    }

    /**
     * Get the base seed.
     *
     * @return The base seed used for mixing
     */
    public int getBaseSeed() {
        return baseSeed;
    }

    /**
     * Get the underlying FastNoiseLite instance.
     *
     * @return The noise generator
     */
    public FastNoiseLite getNoise() {
        return noise;
    }

    /**
     * Mix seed with chunk coordinates using prime multiplication.
     * This ensures different chunks have uncorrelated noise patterns.
     */
    private static int mixSeed(int seed, int chunkX, int chunkY, int chunkZ, int chunkW) {
        int hash = seed;
        hash ^= chunkX * PRIME_X;
        hash ^= chunkY * PRIME_Y;
        hash ^= chunkZ * PRIME_Z;
        hash ^= chunkW * PRIME_W;
        // Additional mixing for better distribution
        hash = ((hash >> 16) ^ hash) * 0x45d9f3b;
        hash = ((hash >> 16) ^ hash) * 0x45d9f3b;
        hash = (hash >> 16) ^ hash;
        return hash;
    }
}
