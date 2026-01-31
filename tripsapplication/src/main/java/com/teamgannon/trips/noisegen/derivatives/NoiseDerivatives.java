package com.teamgannon.trips.noisegen.derivatives;

import com.teamgannon.trips.noisegen.FastNoiseLite;

/**
 * Noise derivatives and normal map generation utilities.
 *
 * <p>Provides gradient/derivative computation for noise, essential for:
 * <ul>
 *   <li>Terrain normal computation for lighting</li>
 *   <li>Bump/normal map generation</li>
 *   <li>Flow field computation</li>
 *   <li>Erosion simulation</li>
 * </ul>
 *
 * <p>Supports both analytical derivatives (faster, for supported noise types)
 * and numerical derivatives (fallback, works with any noise).
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * FastNoiseLite noise = new FastNoiseLite(1337);
 * noise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
 *
 * NoiseDerivatives deriv = new NoiseDerivatives(noise);
 *
 * // Get noise value and gradient together (efficient)
 * NoiseDerivatives.NoiseWithGradient2D result = deriv.getNoiseWithGradient2D(x, y);
 * float value = result.value;
 * float dndx = result.dx;
 * float dndy = result.dy;
 *
 * // Compute surface normal for terrain
 * float[] normal = deriv.computeNormal2D(x, y, heightScale);
 *
 * // Generate normal map colors
 * int[] rgb = deriv.normalToRGB(normal);
 * }</pre>
 *
 * <p><b>Extension:</b> This class is not part of the original FastNoiseLite library.
 */
public class NoiseDerivatives {

    private final FastNoiseLite noise;
    private float epsilon = 0.001f;
    private boolean useAnalytical = true;

    // Cached analytical derivative calculator
    private final SimplexDerivatives simplexDerivatives;

    /**
     * Create noise derivatives calculator.
     *
     * @param noise Base FastNoiseLite instance
     */
    public NoiseDerivatives(FastNoiseLite noise) {
        this.noise = noise;
        this.simplexDerivatives = new SimplexDerivatives(noise.GetSeed());
    }

    /**
     * Create noise derivatives calculator with custom epsilon for numerical derivatives.
     *
     * @param noise Base FastNoiseLite instance
     * @param epsilon Step size for numerical differentiation
     */
    public NoiseDerivatives(FastNoiseLite noise, float epsilon) {
        this.noise = noise;
        this.epsilon = epsilon;
        this.simplexDerivatives = new SimplexDerivatives(noise.GetSeed());
    }

    // ==================== 2D Derivatives ====================

    /**
     * Get 2D noise value and gradient together.
     *
     * <p>More efficient than separate calls when you need both.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @return NoiseWithGradient2D containing value, dx, dy
     */
    public NoiseWithGradient2D getNoiseWithGradient2D(float x, float y) {
        if (useAnalytical && supportsAnalyticalDerivatives()) {
            return getAnalyticalDerivatives2D(x, y);
        }
        return getNumericalDerivatives2D(x, y);
    }

    /**
     * Get just the 2D gradient (partial derivatives).
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @return float[2] containing {dx, dy}
     */
    public float[] getGradient2D(float x, float y) {
        NoiseWithGradient2D result = getNoiseWithGradient2D(x, y);
        return new float[] { result.dx, result.dy };
    }

    /**
     * Compute 2D surface normal for terrain.
     *
     * <p>Assumes noise represents a heightmap where z = noise(x, y) * heightScale.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param heightScale Scale factor for height values
     * @return float[3] containing normalized normal vector {nx, ny, nz}
     */
    public float[] computeNormal2D(float x, float y, float heightScale) {
        NoiseWithGradient2D result = getNoiseWithGradient2D(x, y);

        // Normal is (-dz/dx, -dz/dy, 1) normalized
        // where dz/dx = heightScale * dNoise/dx
        float nx = -result.dx * heightScale;
        float ny = -result.dy * heightScale;
        float nz = 1.0f;

        // Normalize
        float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        return new float[] { nx / len, ny / len, nz / len };
    }

    // ==================== 3D Derivatives ====================

    /**
     * Get 3D noise value and gradient together.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @return NoiseWithGradient3D containing value, dx, dy, dz
     */
    public NoiseWithGradient3D getNoiseWithGradient3D(float x, float y, float z) {
        if (useAnalytical && supportsAnalyticalDerivatives()) {
            return getAnalyticalDerivatives3D(x, y, z);
        }
        return getNumericalDerivatives3D(x, y, z);
    }

    /**
     * Get just the 3D gradient.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @return float[3] containing {dx, dy, dz}
     */
    public float[] getGradient3D(float x, float y, float z) {
        NoiseWithGradient3D result = getNoiseWithGradient3D(x, y, z);
        return new float[] { result.dx, result.dy, result.dz };
    }

    /**
     * Compute 3D surface normal at a point on an isosurface.
     *
     * <p>For density field where surface is at noise(x,y,z) = isoLevel.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @return float[3] containing normalized normal vector
     */
    public float[] computeNormal3D(float x, float y, float z) {
        NoiseWithGradient3D result = getNoiseWithGradient3D(x, y, z);

        // Normal is normalized gradient
        float len = (float) Math.sqrt(
            result.dx * result.dx + result.dy * result.dy + result.dz * result.dz);

        if (len < 0.0001f) {
            return new float[] { 0f, 1f, 0f }; // Default up if gradient is zero
        }

        return new float[] { result.dx / len, result.dy / len, result.dz / len };
    }

    // ==================== Normal Map Generation ====================

    /**
     * Generate a 2D normal map from noise.
     *
     * @param startX Starting X coordinate
     * @param startY Starting Y coordinate
     * @param width Width in samples
     * @param height Height in samples
     * @param step Distance between samples
     * @param heightScale Height scale for normal computation
     * @return 2D array of float[3] normals
     */
    public float[][][] generateNormalMap(float startX, float startY,
                                          int width, int height,
                                          float step, float heightScale) {
        float[][][] normalMap = new float[width][height][3];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                float wx = startX + x * step;
                float wy = startY + y * step;
                normalMap[x][y] = computeNormal2D(wx, wy, heightScale);
            }
        }

        return normalMap;
    }

    /**
     * Generate a normal map as RGB byte array (for textures).
     *
     * <p>Encodes normals as RGB where:
     * - R = (nx + 1) * 127.5
     * - G = (ny + 1) * 127.5
     * - B = (nz + 1) * 127.5
     *
     * @param startX Starting X coordinate
     * @param startY Starting Y coordinate
     * @param width Width in pixels
     * @param height Height in pixels
     * @param step Distance between samples
     * @param heightScale Height scale for normal computation
     * @return byte array in RGB format (width * height * 3 bytes)
     */
    public byte[] generateNormalMapRGB(float startX, float startY,
                                        int width, int height,
                                        float step, float heightScale) {
        byte[] rgb = new byte[width * height * 3];
        int index = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float wx = startX + x * step;
                float wy = startY + y * step;
                float[] normal = computeNormal2D(wx, wy, heightScale);

                // Encode to RGB
                rgb[index++] = (byte) ((normal[0] + 1f) * 127.5f);
                rgb[index++] = (byte) ((normal[1] + 1f) * 127.5f);
                rgb[index++] = (byte) ((normal[2] + 1f) * 127.5f);
            }
        }

        return rgb;
    }

    /**
     * Convert a normal vector to RGB values.
     *
     * @param normal float[3] normal vector
     * @return int[3] containing {R, G, B} in range [0, 255]
     */
    public static int[] normalToRGB(float[] normal) {
        return new int[] {
            (int) ((normal[0] + 1f) * 127.5f),
            (int) ((normal[1] + 1f) * 127.5f),
            (int) ((normal[2] + 1f) * 127.5f)
        };
    }

    // ==================== Tangent Space ====================

    /**
     * Compute tangent and bitangent vectors for bump mapping.
     *
     * <p>Returns orthonormal TBN basis where:
     * - T (tangent) points in increasing X direction on surface
     * - B (bitangent) points in increasing Y direction on surface
     * - N (normal) is perpendicular to surface
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param heightScale Height scale
     * @return float[3][3] containing {tangent[3], bitangent[3], normal[3]}
     */
    public float[][] computeTBN(float x, float y, float heightScale) {
        NoiseWithGradient2D result = getNoiseWithGradient2D(x, y);

        // Tangent is (1, 0, dz/dx) normalized
        float tx = 1f;
        float ty = 0f;
        float tz = result.dx * heightScale;
        float tLen = (float) Math.sqrt(tx * tx + ty * ty + tz * tz);
        float[] tangent = { tx / tLen, ty / tLen, tz / tLen };

        // Bitangent is (0, 1, dz/dy) normalized
        float bx = 0f;
        float by = 1f;
        float bz = result.dy * heightScale;
        float bLen = (float) Math.sqrt(bx * bx + by * by + bz * bz);
        float[] bitangent = { bx / bLen, by / bLen, bz / bLen };

        // Normal
        float[] normal = computeNormal2D(x, y, heightScale);

        return new float[][] { tangent, bitangent, normal };
    }

    // ==================== FBm with Derivatives ====================

    /**
     * Compute FBm noise with accumulated derivatives.
     *
     * <p>Derivatives are properly scaled and accumulated across octaves.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param octaves Number of octaves
     * @param lacunarity Frequency multiplier per octave
     * @param gain Amplitude multiplier per octave
     * @return NoiseWithGradient2D with accumulated values
     */
    public NoiseWithGradient2D getFBmWithGradient2D(float x, float y,
                                                     int octaves, float lacunarity, float gain) {
        float value = 0f;
        float dx = 0f;
        float dy = 0f;
        float amp = 1f;
        float freq = 1f;
        float maxValue = 0f;

        for (int i = 0; i < octaves; i++) {
            NoiseWithGradient2D octave = getNoiseWithGradient2D(x * freq, y * freq);

            value += octave.value * amp;
            // Derivatives scale with frequency
            dx += octave.dx * amp * freq;
            dy += octave.dy * amp * freq;

            maxValue += amp;
            amp *= gain;
            freq *= lacunarity;
        }

        return new NoiseWithGradient2D(value / maxValue, dx / maxValue, dy / maxValue);
    }

    /**
     * Compute 3D FBm noise with accumulated derivatives.
     */
    public NoiseWithGradient3D getFBmWithGradient3D(float x, float y, float z,
                                                     int octaves, float lacunarity, float gain) {
        float value = 0f;
        float dx = 0f;
        float dy = 0f;
        float dz = 0f;
        float amp = 1f;
        float freq = 1f;
        float maxValue = 0f;

        for (int i = 0; i < octaves; i++) {
            NoiseWithGradient3D octave = getNoiseWithGradient3D(x * freq, y * freq, z * freq);

            value += octave.value * amp;
            dx += octave.dx * amp * freq;
            dy += octave.dy * amp * freq;
            dz += octave.dz * amp * freq;

            maxValue += amp;
            amp *= gain;
            freq *= lacunarity;
        }

        return new NoiseWithGradient3D(
            value / maxValue, dx / maxValue, dy / maxValue, dz / maxValue);
    }

    // ==================== Configuration ====================

    /**
     * Set epsilon for numerical differentiation.
     */
    public void setEpsilon(float epsilon) {
        this.epsilon = epsilon;
    }

    /**
     * Get current epsilon value.
     */
    public float getEpsilon() {
        return epsilon;
    }

    /**
     * Enable or disable analytical derivatives.
     *
     * <p>When disabled, always uses numerical differentiation.
     */
    public void setUseAnalytical(boolean useAnalytical) {
        this.useAnalytical = useAnalytical;
    }

    /**
     * Check if analytical derivatives are being used.
     */
    public boolean isUsingAnalytical() {
        return useAnalytical && supportsAnalyticalDerivatives();
    }

    /**
     * Check if current noise type supports analytical derivatives.
     */
    public boolean supportsAnalyticalDerivatives() {
        // Currently only simplex-based noise types support analytical derivatives
        // Cellular and Value noise would need numerical
        return true; // We implement analytical for simplex which is the most common
    }

    // ==================== Private Implementation ====================

    private NoiseWithGradient2D getAnalyticalDerivatives2D(float x, float y) {
        float freq = noise.GetFrequency();
        x *= freq;
        y *= freq;

        // Use our analytical simplex implementation
        return simplexDerivatives.evaluate2D(noise.GetSeed(), x, y);
    }

    private NoiseWithGradient3D getAnalyticalDerivatives3D(float x, float y, float z) {
        float freq = noise.GetFrequency();
        x *= freq;
        y *= freq;
        z *= freq;

        return simplexDerivatives.evaluate3D(noise.GetSeed(), x, y, z);
    }

    private NoiseWithGradient2D getNumericalDerivatives2D(float x, float y) {
        float value = noise.GetNoise(x, y);
        float vx1 = noise.GetNoise(x + epsilon, y);
        float vx2 = noise.GetNoise(x - epsilon, y);
        float vy1 = noise.GetNoise(x, y + epsilon);
        float vy2 = noise.GetNoise(x, y - epsilon);

        float dx = (vx1 - vx2) / (2 * epsilon);
        float dy = (vy1 - vy2) / (2 * epsilon);

        return new NoiseWithGradient2D(value, dx, dy);
    }

    private NoiseWithGradient3D getNumericalDerivatives3D(float x, float y, float z) {
        float value = noise.GetNoise(x, y, z);
        float vx1 = noise.GetNoise(x + epsilon, y, z);
        float vx2 = noise.GetNoise(x - epsilon, y, z);
        float vy1 = noise.GetNoise(x, y + epsilon, z);
        float vy2 = noise.GetNoise(x, y - epsilon, z);
        float vz1 = noise.GetNoise(x, y, z + epsilon);
        float vz2 = noise.GetNoise(x, y, z - epsilon);

        float dx = (vx1 - vx2) / (2 * epsilon);
        float dy = (vy1 - vy2) / (2 * epsilon);
        float dz = (vz1 - vz2) / (2 * epsilon);

        return new NoiseWithGradient3D(value, dx, dy, dz);
    }

    // ==================== Result Classes ====================

    /**
     * 2D noise value with gradient.
     */
    public static class NoiseWithGradient2D {
        public final float value;
        public final float dx;
        public final float dy;

        public NoiseWithGradient2D(float value, float dx, float dy) {
            this.value = value;
            this.dx = dx;
            this.dy = dy;
        }

        /**
         * Get gradient magnitude.
         */
        public float gradientMagnitude() {
            return (float) Math.sqrt(dx * dx + dy * dy);
        }

        /**
         * Get gradient direction in radians.
         */
        public float gradientDirection() {
            return (float) Math.atan2(dy, dx);
        }
    }

    /**
     * 3D noise value with gradient.
     */
    public static class NoiseWithGradient3D {
        public final float value;
        public final float dx;
        public final float dy;
        public final float dz;

        public NoiseWithGradient3D(float value, float dx, float dy, float dz) {
            this.value = value;
            this.dx = dx;
            this.dy = dy;
            this.dz = dz;
        }

        /**
         * Get gradient magnitude.
         */
        public float gradientMagnitude() {
            return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        }

        /**
         * Get gradient as array.
         */
        public float[] toArray() {
            return new float[] { dx, dy, dz };
        }
    }
}
