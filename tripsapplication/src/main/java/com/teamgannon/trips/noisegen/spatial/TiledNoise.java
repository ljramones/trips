package com.teamgannon.trips.noisegen.spatial;

import com.teamgannon.trips.noisegen.FastNoiseLite;

/**
 * Seamlessly tileable noise generator.
 *
 * <p>Creates noise that tiles perfectly at specified intervals, useful for:
 * <ul>
 *   <li>Texture generation that wraps seamlessly</li>
 *   <li>Infinite scrolling backgrounds</li>
 *   <li>Procedural patterns that repeat</li>
 *   <li>Spherical/cylindrical mapping without seams</li>
 * </ul>
 *
 * <p>Uses the "sample on a torus" technique: maps 2D coordinates to a 4D torus
 * where the edges naturally connect. For 3D, uses 6D sampling (more expensive).
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * FastNoiseLite baseNoise = new FastNoiseLite(1337);
 * baseNoise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
 * baseNoise.SetFractalType(FastNoiseLite.FractalType.FBm);
 * baseNoise.SetFractalOctaves(4);
 *
 * // Create 256x256 tileable noise
 * TiledNoise tiled = new TiledNoise(baseNoise, 256, 256);
 *
 * // Sample - edges will match perfectly
 * float v1 = tiled.getNoise(0, 128);      // Left edge
 * float v2 = tiled.getNoise(256, 128);    // Right edge (same as left)
 * // v1 == v2 (seamless!)
 * }</pre>
 *
 * <p><b>Extension:</b> This class is not part of the original FastNoiseLite library.
 */
public class TiledNoise {

    private final FastNoiseLite noise;
    private final float tileWidth;
    private final float tileHeight;
    private final float tileDepth;
    private final float radiusX;
    private final float radiusY;
    private final float radiusZ;

    private static final float TWO_PI = (float) (2.0 * Math.PI);

    /**
     * Create 2D tileable noise with square tiles.
     *
     * @param noise The base FastNoiseLite instance
     * @param tileSize Size of the tile (width and height)
     */
    public TiledNoise(FastNoiseLite noise, float tileSize) {
        this(noise, tileSize, tileSize, 0);
    }

    /**
     * Create 2D tileable noise with rectangular tiles.
     *
     * @param noise The base FastNoiseLite instance
     * @param tileWidth Width of the tile
     * @param tileHeight Height of the tile
     */
    public TiledNoise(FastNoiseLite noise, float tileWidth, float tileHeight) {
        this(noise, tileWidth, tileHeight, 0);
    }

    /**
     * Create 3D tileable noise.
     *
     * @param noise The base FastNoiseLite instance
     * @param tileWidth Width of the tile (X)
     * @param tileHeight Height of the tile (Y)
     * @param tileDepth Depth of the tile (Z), use 0 for 2D-only
     */
    public TiledNoise(FastNoiseLite noise, float tileWidth, float tileHeight, float tileDepth) {
        this.noise = noise;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.tileDepth = tileDepth;

        // Calculate torus radii based on tile dimensions
        // Larger tiles need larger radii to maintain detail
        this.radiusX = tileWidth / TWO_PI;
        this.radiusY = tileHeight / TWO_PI;
        this.radiusZ = tileDepth > 0 ? tileDepth / TWO_PI : 0;
    }

    /**
     * Get seamlessly tileable 2D noise.
     *
     * <p>Maps 2D coordinates to a 4D torus, ensuring edges connect perfectly.
     *
     * @param x X coordinate (0 to tileWidth tiles seamlessly)
     * @param y Y coordinate (0 to tileHeight tiles seamlessly)
     * @return Noise value in range [-1, 1]
     */
    public float getNoise(float x, float y) {
        // Convert to angles around the torus
        float angleX = (x / tileWidth) * TWO_PI;
        float angleY = (y / tileHeight) * TWO_PI;

        // Sample on 4D torus surface
        // The torus is parameterized as:
        // (rx*cos(ax), rx*sin(ax), ry*cos(ay), ry*sin(ay))
        float nx = radiusX * (float) Math.cos(angleX);
        float ny = radiusX * (float) Math.sin(angleX);
        float nz = radiusY * (float) Math.cos(angleY);
        float nw = radiusY * (float) Math.sin(angleY);

        return noise.GetNoise(nx, ny, nz, nw);
    }

    /**
     * Get seamlessly tileable 3D noise.
     *
     * <p>Note: This requires 6D sampling, which is approximated using
     * multiple 4D samples. More expensive than 2D tiling.
     *
     * @param x X coordinate (0 to tileWidth tiles seamlessly)
     * @param y Y coordinate (0 to tileHeight tiles seamlessly)
     * @param z Z coordinate (0 to tileDepth tiles seamlessly)
     * @return Noise value in range [-1, 1]
     */
    public float getNoise(float x, float y, float z) {
        if (tileDepth <= 0) {
            // Fall back to 2D tiling with Z as non-tiled dimension
            return getNoise2DWithZ(x, y, z);
        }

        // For true 3D tiling, we need to sample a 6D hypertorus
        // Approximation: combine two 4D samples
        float angleX = (x / tileWidth) * TWO_PI;
        float angleY = (y / tileHeight) * TWO_PI;
        float angleZ = (z / tileDepth) * TWO_PI;

        // First 4D sample (X-Y torus)
        float nx1 = radiusX * (float) Math.cos(angleX);
        float ny1 = radiusX * (float) Math.sin(angleX);
        float nz1 = radiusY * (float) Math.cos(angleY);
        float nw1 = radiusY * (float) Math.sin(angleY);

        // Second 4D sample (Y-Z torus, offset to avoid correlation)
        float nx2 = radiusY * (float) Math.cos(angleY) + 1000f;
        float ny2 = radiusY * (float) Math.sin(angleY);
        float nz2 = radiusZ * (float) Math.cos(angleZ);
        float nw2 = radiusZ * (float) Math.sin(angleZ);

        // Blend the two samples
        float sample1 = noise.GetNoise(nx1, ny1, nz1, nw1);
        float sample2 = noise.GetNoise(nx2, ny2, nz2, nw2);

        return (sample1 + sample2) * 0.5f;
    }

    /**
     * Get 2D tileable noise with a non-tiling Z dimension.
     * Useful for animated 2D textures where time doesn't need to loop.
     *
     * @param x X coordinate (tiles)
     * @param y Y coordinate (tiles)
     * @param z Z coordinate (does not tile)
     * @return Noise value in range [-1, 1]
     */
    public float getNoise2DWithZ(float x, float y, float z) {
        float angleX = (x / tileWidth) * TWO_PI;
        float angleY = (y / tileHeight) * TWO_PI;

        // 4D torus in XY, with Z as offset
        float nx = radiusX * (float) Math.cos(angleX);
        float ny = radiusX * (float) Math.sin(angleX);
        float nz = radiusY * (float) Math.cos(angleY) + z;
        float nw = radiusY * (float) Math.sin(angleY);

        return noise.GetNoise(nx, ny, nz, nw);
    }

    /**
     * Get tileable noise with normalized coordinates.
     * Input should be in range [0, 1] for one tile.
     *
     * @param u U coordinate (0 to 1)
     * @param v V coordinate (0 to 1)
     * @return Noise value in range [-1, 1]
     */
    public float getNoiseNormalized(float u, float v) {
        return getNoise(u * tileWidth, v * tileHeight);
    }

    /**
     * Get 3D tileable noise with normalized coordinates.
     *
     * @param u U coordinate (0 to 1)
     * @param v V coordinate (0 to 1)
     * @param w W coordinate (0 to 1)
     * @return Noise value in range [-1, 1]
     */
    public float getNoiseNormalized(float u, float v, float w) {
        return getNoise(u * tileWidth, v * tileHeight, w * tileDepth);
    }

    /**
     * Generate a complete 2D tile as an array.
     *
     * @param width Width of the output array
     * @param height Height of the output array
     * @return 2D array of noise values in range [-1, 1]
     */
    public float[][] generateTile(int width, int height) {
        float[][] tile = new float[width][height];
        float scaleX = tileWidth / width;
        float scaleY = tileHeight / height;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                tile[x][y] = getNoise(x * scaleX, y * scaleY);
            }
        }

        return tile;
    }

    /**
     * Generate a complete 2D tile as a 1D array (row-major order).
     *
     * @param width Width of the output
     * @param height Height of the output
     * @return 1D array of noise values in range [-1, 1]
     */
    public float[] generateTileFlat(int width, int height) {
        float[] tile = new float[width * height];
        float scaleX = tileWidth / width;
        float scaleY = tileHeight / height;

        int index = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                tile[index++] = getNoise(x * scaleX, y * scaleY);
            }
        }

        return tile;
    }

    /**
     * Get tile width.
     */
    public float getTileWidth() {
        return tileWidth;
    }

    /**
     * Get tile height.
     */
    public float getTileHeight() {
        return tileHeight;
    }

    /**
     * Get tile depth.
     */
    public float getTileDepth() {
        return tileDepth;
    }

    /**
     * Check if 3D tiling is enabled.
     */
    public boolean is3DTiling() {
        return tileDepth > 0;
    }

    /**
     * Get the underlying FastNoiseLite instance.
     */
    public FastNoiseLite getNoise() {
        return noise;
    }
}
