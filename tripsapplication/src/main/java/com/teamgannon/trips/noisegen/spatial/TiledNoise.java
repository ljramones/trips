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

    // ========================================================================
    // Seamless Image Generation (Godot-style convenience methods)
    // ========================================================================

    /**
     * Generate a seamless grayscale image.
     *
     * <p>Similar to Godot's {@code get_seamless_image()} - returns image data
     * ready for texture creation.
     *
     * @param width Image width in pixels
     * @param height Image height in pixels
     * @return Grayscale pixel data (width * height bytes, values 0-255)
     */
    public byte[] getSeamlessImage(int width, int height) {
        byte[] pixels = new byte[width * height];
        float scaleX = tileWidth / width;
        float scaleY = tileHeight / height;

        int index = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float value = getNoise(x * scaleX, y * scaleY);
                // Map from [-1, 1] to [0, 255]
                int gray = (int) ((value + 1.0f) * 0.5f * 255.0f);
                pixels[index++] = (byte) Math.max(0, Math.min(255, gray));
            }
        }

        return pixels;
    }

    /**
     * Generate a seamless RGBA image.
     *
     * <p>Returns 4 bytes per pixel (R, G, B, A) with noise mapped to grayscale
     * and full opacity.
     *
     * @param width Image width in pixels
     * @param height Image height in pixels
     * @return RGBA pixel data (width * height * 4 bytes)
     */
    public byte[] getSeamlessImageRGBA(int width, int height) {
        byte[] pixels = new byte[width * height * 4];
        float scaleX = tileWidth / width;
        float scaleY = tileHeight / height;

        int index = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float value = getNoise(x * scaleX, y * scaleY);
                int gray = (int) ((value + 1.0f) * 0.5f * 255.0f);
                gray = Math.max(0, Math.min(255, gray));

                pixels[index++] = (byte) gray;  // R
                pixels[index++] = (byte) gray;  // G
                pixels[index++] = (byte) gray;  // B
                pixels[index++] = (byte) 255;   // A (fully opaque)
            }
        }

        return pixels;
    }

    /**
     * Generate a seamless image with custom value mapping.
     *
     * <p>Allows custom transformation of noise values before conversion to pixels.
     *
     * @param width Image width in pixels
     * @param height Image height in pixels
     * @param mapper Function to map noise value [-1,1] to byte [0,255]
     * @return Grayscale pixel data (width * height bytes)
     */
    public byte[] getSeamlessImage(int width, int height, NoiseToByteMapper mapper) {
        byte[] pixels = new byte[width * height];
        float scaleX = tileWidth / width;
        float scaleY = tileHeight / height;

        int index = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float value = getNoise(x * scaleX, y * scaleY);
                pixels[index++] = mapper.map(value);
            }
        }

        return pixels;
    }

    /**
     * Generate a seamless RGB image with custom color mapping.
     *
     * @param width Image width in pixels
     * @param height Image height in pixels
     * @param mapper Function to map noise value [-1,1] to RGB values
     * @return RGB pixel data (width * height * 3 bytes)
     */
    public byte[] getSeamlessImageRGB(int width, int height, NoiseToRGBMapper mapper) {
        byte[] pixels = new byte[width * height * 3];
        float scaleX = tileWidth / width;
        float scaleY = tileHeight / height;

        int index = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float value = getNoise(x * scaleX, y * scaleY);
                byte[] rgb = mapper.map(value);
                pixels[index++] = rgb[0];  // R
                pixels[index++] = rgb[1];  // G
                pixels[index++] = rgb[2];  // B
            }
        }

        return pixels;
    }

    /**
     * Generate a seamless 3D texture slice.
     *
     * <p>Generates a 2D slice of seamlessly tiling 3D noise at a given Z depth.
     *
     * @param width Image width in pixels
     * @param height Image height in pixels
     * @param z Z coordinate for the slice
     * @return Grayscale pixel data (width * height bytes)
     */
    public byte[] getSeamlessImage3DSlice(int width, int height, float z) {
        byte[] pixels = new byte[width * height];
        float scaleX = tileWidth / width;
        float scaleY = tileHeight / height;

        int index = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float value = getNoise(x * scaleX, y * scaleY, z);
                int gray = (int) ((value + 1.0f) * 0.5f * 255.0f);
                pixels[index++] = (byte) Math.max(0, Math.min(255, gray));
            }
        }

        return pixels;
    }

    /**
     * Generate seamless image as float array (for further processing).
     *
     * @param width Image width in pixels
     * @param height Image height in pixels
     * @return Float array with noise values in range [-1, 1]
     */
    public float[] getSeamlessImageFloat(int width, int height) {
        return generateTileFlat(width, height);
    }

    /**
     * Generate seamless image with normalized float values [0, 1].
     *
     * @param width Image width in pixels
     * @param height Image height in pixels
     * @return Float array with noise values in range [0, 1]
     */
    public float[] getSeamlessImageFloatNormalized(int width, int height) {
        float[] raw = generateTileFlat(width, height);
        for (int i = 0; i < raw.length; i++) {
            raw[i] = (raw[i] + 1.0f) * 0.5f;
        }
        return raw;
    }

    // ========================================================================
    // Functional Interfaces for Custom Mapping
    // ========================================================================

    /**
     * Maps a noise value to a single byte (grayscale).
     */
    @FunctionalInterface
    public interface NoiseToByteMapper {
        /**
         * Map noise value to byte.
         * @param noiseValue Noise value in range [-1, 1]
         * @return Byte value [0, 255] (returned as byte)
         */
        byte map(float noiseValue);
    }

    /**
     * Maps a noise value to RGB bytes.
     */
    @FunctionalInterface
    public interface NoiseToRGBMapper {
        /**
         * Map noise value to RGB.
         * @param noiseValue Noise value in range [-1, 1]
         * @return Array of 3 bytes [R, G, B]
         */
        byte[] map(float noiseValue);
    }

    // ========================================================================
    // Built-in Mappers
    // ========================================================================

    /**
     * Standard grayscale mapper: [-1,1] → [0,255].
     */
    public static final NoiseToByteMapper GRAYSCALE = value -> {
        int gray = (int) ((value + 1.0f) * 0.5f * 255.0f);
        return (byte) Math.max(0, Math.min(255, gray));
    };

    /**
     * Inverted grayscale mapper: [-1,1] → [255,0].
     */
    public static final NoiseToByteMapper GRAYSCALE_INVERTED = value -> {
        int gray = (int) ((1.0f - value) * 0.5f * 255.0f);
        return (byte) Math.max(0, Math.min(255, gray));
    };

    /**
     * High contrast mapper with adjustable threshold.
     */
    public static NoiseToByteMapper threshold(float threshold) {
        return value -> (byte) (value > threshold ? 255 : 0);
    }

    /**
     * Terrain color gradient mapper (blue → green → brown → white).
     */
    public static final NoiseToRGBMapper TERRAIN_GRADIENT = value -> {
        // Normalize to [0, 1]
        float t = (value + 1.0f) * 0.5f;

        byte r, g, b;
        if (t < 0.3f) {
            // Deep water to shallow water (dark blue to light blue)
            float lt = t / 0.3f;
            r = (byte) (int) (30 + lt * 50);
            g = (byte) (int) (60 + lt * 100);
            b = (byte) (int) (150 + lt * 50);
        } else if (t < 0.5f) {
            // Beach/lowland (tan to green)
            float lt = (t - 0.3f) / 0.2f;
            r = (byte) (int) (80 - lt * 40);
            g = (byte) (int) (160 + lt * 40);
            b = (byte) (int) (80 - lt * 30);
        } else if (t < 0.7f) {
            // Hills (green to brown)
            float lt = (t - 0.5f) / 0.2f;
            r = (byte) (int) (40 + lt * 100);
            g = (byte) (int) (200 - lt * 100);
            b = (byte) (int) (50 - lt * 20);
        } else if (t < 0.85f) {
            // Mountains (brown to gray)
            float lt = (t - 0.7f) / 0.15f;
            r = (byte) (int) (140 - lt * 20);
            g = (byte) (int) (100 + lt * 20);
            b = (byte) (int) (30 + lt * 70);
        } else {
            // Snow caps (gray to white)
            float lt = (t - 0.85f) / 0.15f;
            r = (byte) (int) (120 + lt * 135);
            g = (byte) (int) (120 + lt * 135);
            b = (byte) (int) (100 + lt * 155);
        }

        return new byte[]{r, g, b};
    };

    /**
     * Heat map gradient (blue → cyan → green → yellow → red).
     */
    public static final NoiseToRGBMapper HEAT_GRADIENT = value -> {
        float t = (value + 1.0f) * 0.5f;
        byte r, g, b;

        if (t < 0.25f) {
            float lt = t / 0.25f;
            r = 0;
            g = (byte) (int) (lt * 255);
            b = (byte) 255;
        } else if (t < 0.5f) {
            float lt = (t - 0.25f) / 0.25f;
            r = 0;
            g = (byte) 255;
            b = (byte) (int) ((1 - lt) * 255);
        } else if (t < 0.75f) {
            float lt = (t - 0.5f) / 0.25f;
            r = (byte) (int) (lt * 255);
            g = (byte) 255;
            b = 0;
        } else {
            float lt = (t - 0.75f) / 0.25f;
            r = (byte) 255;
            g = (byte) (int) ((1 - lt) * 255);
            b = 0;
        }

        return new byte[]{r, g, b};
    };
}
