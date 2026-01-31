package com.teamgannon.trips.noisegen.generators;

import java.util.Random;

/**
 * Band-limited wavelet noise generator.
 *
 * <p>Wavelet noise is a coherent noise with a controlled frequency spectrum,
 * making it ideal for:
 * <ul>
 *   <li>Texture synthesis without aliasing</li>
 *   <li>Procedural detail that mipmaps cleanly</li>
 *   <li>Fluid simulations requiring specific frequency bands</li>
 *   <li>Procedural terrain at multiple scales</li>
 * </ul>
 *
 * <p>Based on the technique from Bridson et al. "Wavelet Noise" (2007).
 * The noise has finite support (band-limited) meaning it contains only
 * frequencies within a specific range, eliminating aliasing artifacts.
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * WaveletNoiseGen wavelet = new WaveletNoiseGen(1337, 128);
 *
 * // Sample band-limited noise
 * float value = wavelet.sample2D(x, y);
 *
 * // Get noise at specific band (0 = base, 1 = coarser, etc.)
 * float bandValue = wavelet.sampleBand2D(x, y, 2);
 *
 * // Multi-octave sampling with automatic band selection
 * float fbmValue = wavelet.sampleFBm2D(x, y, octaves, lacunarity, gain);
 * }</pre>
 *
 * <p><b>Extension:</b> This class is not part of the original FastNoiseLite library.
 */
public class WaveletNoiseGen implements NoiseGenerator {

    private final int tileSize;
    private final int tileMask;
    private final float[] noiseTile;
    private final float[] noiseTile3D;
    private final int seed;

    // Wavelet downsampling coefficients (B-spline wavelet)
    private static final float[] DOWNSAMPLE_COEFFS = {
        0.125f, 0.375f, 0.375f, 0.125f
    };

    // Wavelet upsampling coefficients
    private static final float[] UPSAMPLE_COEFFS = {
        0.5f, 1.0f, 0.5f
    };

    /**
     * Create wavelet noise with default tile size (128).
     *
     * @param seed Random seed
     */
    public WaveletNoiseGen(int seed) {
        this(seed, 128);
    }

    /**
     * Create wavelet noise with specified tile size.
     *
     * <p>Larger tiles provide more detail but use more memory.
     * Tile size must be a power of 2.
     *
     * @param seed Random seed
     * @param tileSize Size of the noise tile (must be power of 2)
     */
    public WaveletNoiseGen(int seed, int tileSize) {
        // Ensure power of 2
        if ((tileSize & (tileSize - 1)) != 0) {
            throw new IllegalArgumentException("Tile size must be a power of 2");
        }

        this.seed = seed;
        this.tileSize = tileSize;
        this.tileMask = tileSize - 1;
        this.noiseTile = generateWaveletNoiseTile2D(seed, tileSize);
        this.noiseTile3D = generateWaveletNoiseTile3D(seed, tileSize);
    }

    /**
     * Sample 2D wavelet noise at the given coordinates.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @return Noise value in range [-1, 1]
     */
    public float sample2D(float x, float y) {
        return evaluate2D(noiseTile, x, y);
    }

    /**
     * Sample 3D wavelet noise at the given coordinates.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @return Noise value in range [-1, 1]
     */
    public float sample3D(float x, float y, float z) {
        return evaluate3D(noiseTile3D, x, y, z);
    }

    /**
     * Sample noise at a specific frequency band.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param band Band index (0 = base frequency, 1 = half frequency, etc.)
     * @return Noise value in range [-1, 1]
     */
    public float sampleBand2D(float x, float y, int band) {
        float scale = 1.0f / (1 << band);
        return evaluate2D(noiseTile, x * scale, y * scale);
    }

    /**
     * Sample 3D noise at a specific frequency band.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @param band Band index (0 = base frequency, 1 = half frequency, etc.)
     * @return Noise value in range [-1, 1]
     */
    public float sampleBand3D(float x, float y, float z, int band) {
        float scale = 1.0f / (1 << band);
        return evaluate3D(noiseTile3D, x * scale, y * scale, z * scale);
    }

    /**
     * Sample FBm (fractional Brownian motion) using wavelet noise bands.
     *
     * <p>Unlike standard FBm, wavelet FBm uses band-limited octaves
     * that don't alias when downsampled.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param octaves Number of octaves
     * @param lacunarity Frequency multiplier per octave (typically 2.0)
     * @param gain Amplitude multiplier per octave (typically 0.5)
     * @return Noise value (approximately in range [-1, 1])
     */
    public float sampleFBm2D(float x, float y, int octaves, float lacunarity, float gain) {
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
     * Sample 3D FBm using wavelet noise bands.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @param octaves Number of octaves
     * @param lacunarity Frequency multiplier per octave
     * @param gain Amplitude multiplier per octave
     * @return Noise value (approximately in range [-1, 1])
     */
    public float sampleFBm3D(float x, float y, float z, int octaves, float lacunarity, float gain) {
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
     * Get the tile size.
     */
    public int getTileSize() {
        return tileSize;
    }

    /**
     * Get the seed.
     */
    public int getSeed() {
        return seed;
    }

    // NoiseGenerator interface implementation

    @Override
    public float single2D(int seed, float x, float y) {
        // Add seed offset for variation
        float offset = seed * 1000.5f;
        return sample2D(x + offset, y + offset);
    }

    @Override
    public float single3D(int seed, float x, float y, float z) {
        float offset = seed * 1000.5f;
        return sample3D(x + offset, y + offset, z + offset);
    }

    @Override
    public float single4D(int seed, float x, float y, float z, float w) {
        // 4D wavelet noise is very expensive - use 3D with w offset
        float offset = seed * 1000.5f;
        return sample3D(x + offset + w * 0.7071f, y + offset + w * 0.7071f, z + offset);
    }

    @Override
    public boolean supports4D() {
        return false; // True 4D not implemented, approximated via 3D
    }

    // Private methods for wavelet noise generation

    /**
     * Generate a 2D wavelet noise tile.
     */
    private float[] generateWaveletNoiseTile2D(int seed, int size) {
        Random random = new Random(seed);
        float[] tile = new float[size * size];

        // Generate white noise
        for (int i = 0; i < tile.length; i++) {
            tile[i] = random.nextFloat() * 2f - 1f;
        }

        // Apply wavelet transform to make band-limited
        float[] temp = new float[size * size];
        float[] downsampled = new float[(size / 2) * (size / 2)];
        float[] upsampled = new float[size * size];

        // Downsample
        downsample2D(tile, downsampled, size, size / 2);

        // Upsample
        upsample2D(downsampled, upsampled, size / 2, size);

        // Subtract to get band-limited noise
        for (int i = 0; i < tile.length; i++) {
            tile[i] = tile[i] - upsampled[i];
        }

        // Normalize
        float maxAbs = 0f;
        for (float v : tile) {
            maxAbs = Math.max(maxAbs, Math.abs(v));
        }
        if (maxAbs > 0) {
            for (int i = 0; i < tile.length; i++) {
                tile[i] /= maxAbs;
            }
        }

        return tile;
    }

    /**
     * Generate a 3D wavelet noise tile.
     */
    private float[] generateWaveletNoiseTile3D(int seed, int size) {
        Random random = new Random(seed + 12345);
        float[] tile = new float[size * size * size];

        // Generate white noise
        for (int i = 0; i < tile.length; i++) {
            tile[i] = random.nextFloat() * 2f - 1f;
        }

        // Apply wavelet transform to make band-limited
        int halfSize = size / 2;
        float[] downsampled = new float[halfSize * halfSize * halfSize];
        float[] upsampled = new float[size * size * size];

        // Downsample
        downsample3D(tile, downsampled, size, halfSize);

        // Upsample
        upsample3D(downsampled, upsampled, halfSize, size);

        // Subtract to get band-limited noise
        for (int i = 0; i < tile.length; i++) {
            tile[i] = tile[i] - upsampled[i];
        }

        // Normalize
        float maxAbs = 0f;
        for (float v : tile) {
            maxAbs = Math.max(maxAbs, Math.abs(v));
        }
        if (maxAbs > 0) {
            for (int i = 0; i < tile.length; i++) {
                tile[i] /= maxAbs;
            }
        }

        return tile;
    }

    /**
     * Downsample a 2D array using wavelet coefficients.
     */
    private void downsample2D(float[] src, float[] dst, int srcSize, int dstSize) {
        // Downsample rows
        float[] temp = new float[srcSize * dstSize];
        for (int y = 0; y < srcSize; y++) {
            for (int x = 0; x < dstSize; x++) {
                float sum = 0f;
                int srcX = x * 2;
                for (int k = 0; k < 4; k++) {
                    int sx = (srcX + k - 1 + srcSize) % srcSize;
                    sum += DOWNSAMPLE_COEFFS[k] * src[y * srcSize + sx];
                }
                temp[y * dstSize + x] = sum;
            }
        }

        // Downsample columns
        for (int x = 0; x < dstSize; x++) {
            for (int y = 0; y < dstSize; y++) {
                float sum = 0f;
                int srcY = y * 2;
                for (int k = 0; k < 4; k++) {
                    int sy = (srcY + k - 1 + srcSize) % srcSize;
                    sum += DOWNSAMPLE_COEFFS[k] * temp[sy * dstSize + x];
                }
                dst[y * dstSize + x] = sum;
            }
        }
    }

    /**
     * Upsample a 2D array using wavelet coefficients.
     */
    private void upsample2D(float[] src, float[] dst, int srcSize, int dstSize) {
        // Upsample columns
        float[] temp = new float[dstSize * srcSize];
        for (int x = 0; x < srcSize; x++) {
            for (int y = 0; y < dstSize; y++) {
                int srcY = y / 2;
                int phase = y % 2;
                float sum = 0f;
                for (int k = 0; k < 3; k++) {
                    int sy = (srcY + k - 1 + srcSize) % srcSize;
                    float coeff = (phase == 0) ? UPSAMPLE_COEFFS[k] : UPSAMPLE_COEFFS[2 - k];
                    sum += coeff * src[sy * srcSize + x];
                }
                temp[y * srcSize + x] = sum;
            }
        }

        // Upsample rows
        for (int y = 0; y < dstSize; y++) {
            for (int x = 0; x < dstSize; x++) {
                int srcX = x / 2;
                int phase = x % 2;
                float sum = 0f;
                for (int k = 0; k < 3; k++) {
                    int sx = (srcX + k - 1 + srcSize) % srcSize;
                    float coeff = (phase == 0) ? UPSAMPLE_COEFFS[k] : UPSAMPLE_COEFFS[2 - k];
                    sum += coeff * temp[y * srcSize + sx];
                }
                dst[y * dstSize + x] = sum;
            }
        }
    }

    /**
     * Downsample a 3D array using wavelet coefficients.
     */
    private void downsample3D(float[] src, float[] dst, int srcSize, int dstSize) {
        int srcSize2 = srcSize * srcSize;
        int dstSize2 = dstSize * dstSize;

        // Downsample X
        float[] temp1 = new float[dstSize * srcSize * srcSize];
        for (int z = 0; z < srcSize; z++) {
            for (int y = 0; y < srcSize; y++) {
                for (int x = 0; x < dstSize; x++) {
                    float sum = 0f;
                    int srcX = x * 2;
                    for (int k = 0; k < 4; k++) {
                        int sx = (srcX + k - 1 + srcSize) % srcSize;
                        sum += DOWNSAMPLE_COEFFS[k] * src[z * srcSize2 + y * srcSize + sx];
                    }
                    temp1[z * dstSize * srcSize + y * dstSize + x] = sum;
                }
            }
        }

        // Downsample Y
        float[] temp2 = new float[dstSize * dstSize * srcSize];
        for (int z = 0; z < srcSize; z++) {
            for (int y = 0; y < dstSize; y++) {
                for (int x = 0; x < dstSize; x++) {
                    float sum = 0f;
                    int srcY = y * 2;
                    for (int k = 0; k < 4; k++) {
                        int sy = (srcY + k - 1 + srcSize) % srcSize;
                        sum += DOWNSAMPLE_COEFFS[k] * temp1[z * dstSize * srcSize + sy * dstSize + x];
                    }
                    temp2[z * dstSize2 + y * dstSize + x] = sum;
                }
            }
        }

        // Downsample Z
        for (int z = 0; z < dstSize; z++) {
            for (int y = 0; y < dstSize; y++) {
                for (int x = 0; x < dstSize; x++) {
                    float sum = 0f;
                    int srcZ = z * 2;
                    for (int k = 0; k < 4; k++) {
                        int sz = (srcZ + k - 1 + srcSize) % srcSize;
                        sum += DOWNSAMPLE_COEFFS[k] * temp2[sz * dstSize2 + y * dstSize + x];
                    }
                    dst[z * dstSize2 + y * dstSize + x] = sum;
                }
            }
        }
    }

    /**
     * Upsample a 3D array using wavelet coefficients.
     */
    private void upsample3D(float[] src, float[] dst, int srcSize, int dstSize) {
        int srcSize2 = srcSize * srcSize;
        int dstSize2 = dstSize * dstSize;

        // Upsample Z
        float[] temp1 = new float[dstSize * srcSize * srcSize];
        for (int z = 0; z < dstSize; z++) {
            for (int y = 0; y < srcSize; y++) {
                for (int x = 0; x < srcSize; x++) {
                    int srcZ = z / 2;
                    int phase = z % 2;
                    float sum = 0f;
                    for (int k = 0; k < 3; k++) {
                        int sz = (srcZ + k - 1 + srcSize) % srcSize;
                        float coeff = (phase == 0) ? UPSAMPLE_COEFFS[k] : UPSAMPLE_COEFFS[2 - k];
                        sum += coeff * src[sz * srcSize2 + y * srcSize + x];
                    }
                    temp1[z * srcSize2 + y * srcSize + x] = sum;
                }
            }
        }

        // Upsample Y
        float[] temp2 = new float[dstSize * dstSize * srcSize];
        for (int z = 0; z < dstSize; z++) {
            for (int y = 0; y < dstSize; y++) {
                for (int x = 0; x < srcSize; x++) {
                    int srcY = y / 2;
                    int phase = y % 2;
                    float sum = 0f;
                    for (int k = 0; k < 3; k++) {
                        int sy = (srcY + k - 1 + srcSize) % srcSize;
                        float coeff = (phase == 0) ? UPSAMPLE_COEFFS[k] : UPSAMPLE_COEFFS[2 - k];
                        sum += coeff * temp1[z * srcSize2 + sy * srcSize + x];
                    }
                    temp2[z * dstSize * srcSize + y * srcSize + x] = sum;
                }
            }
        }

        // Upsample X
        for (int z = 0; z < dstSize; z++) {
            for (int y = 0; y < dstSize; y++) {
                for (int x = 0; x < dstSize; x++) {
                    int srcX = x / 2;
                    int phase = x % 2;
                    float sum = 0f;
                    for (int k = 0; k < 3; k++) {
                        int sx = (srcX + k - 1 + srcSize) % srcSize;
                        float coeff = (phase == 0) ? UPSAMPLE_COEFFS[k] : UPSAMPLE_COEFFS[2 - k];
                        sum += coeff * temp2[z * dstSize * srcSize + y * srcSize + sx];
                    }
                    dst[z * dstSize2 + y * dstSize + x] = sum;
                }
            }
        }
    }

    /**
     * Evaluate 2D noise at the given coordinates using tricubic interpolation.
     */
    private float evaluate2D(float[] tile, float x, float y) {
        // Wrap to tile coordinates
        float tx = x - (float) Math.floor(x / tileSize) * tileSize;
        float ty = y - (float) Math.floor(y / tileSize) * tileSize;

        // Get integer and fractional parts
        int ix = (int) tx;
        int iy = (int) ty;
        float fx = tx - ix;
        float fy = ty - iy;

        // Cubic interpolation
        float result = 0f;
        for (int j = -1; j <= 2; j++) {
            float cy = cubicWeight(fy - j);
            int py = (iy + j) & tileMask;
            for (int i = -1; i <= 2; i++) {
                float cx = cubicWeight(fx - i);
                int px = (ix + i) & tileMask;
                result += tile[py * tileSize + px] * cx * cy;
            }
        }

        return result;
    }

    /**
     * Evaluate 3D noise at the given coordinates using tricubic interpolation.
     */
    private float evaluate3D(float[] tile, float x, float y, float z) {
        // Wrap to tile coordinates
        float tx = x - (float) Math.floor(x / tileSize) * tileSize;
        float ty = y - (float) Math.floor(y / tileSize) * tileSize;
        float tz = z - (float) Math.floor(z / tileSize) * tileSize;

        // Get integer and fractional parts
        int ix = (int) tx;
        int iy = (int) ty;
        int iz = (int) tz;
        float fx = tx - ix;
        float fy = ty - iy;
        float fz = tz - iz;

        int tileSize2 = tileSize * tileSize;

        // Cubic interpolation
        float result = 0f;
        for (int k = -1; k <= 2; k++) {
            float cz = cubicWeight(fz - k);
            int pz = (iz + k) & tileMask;
            for (int j = -1; j <= 2; j++) {
                float cy = cubicWeight(fy - j);
                int py = (iy + j) & tileMask;
                for (int i = -1; i <= 2; i++) {
                    float cx = cubicWeight(fx - i);
                    int px = (ix + i) & tileMask;
                    result += tile[pz * tileSize2 + py * tileSize + px] * cx * cy * cz;
                }
            }
        }

        return result;
    }

    /**
     * Cubic interpolation weight (Catmull-Rom spline).
     */
    private static float cubicWeight(float x) {
        x = Math.abs(x);
        if (x < 1f) {
            return (1.5f * x - 2.5f) * x * x + 1f;
        } else if (x < 2f) {
            return ((-0.5f * x + 2.5f) * x - 4f) * x + 2f;
        }
        return 0f;
    }
}
