package com.teamgannon.trips.noisegen.transforms;

/**
 * Transform that quantizes noise values to discrete steps.
 *
 * <p>Similar to {@link TerraceTransform} but with more control over
 * the quantization process and step distribution.
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * // Quantize to 16 levels
 * NoiseTransform quant = new QuantizeTransform(16);
 *
 * // Quantize with dithering (reduces banding artifacts)
 * NoiseTransform dithered = new QuantizeTransform(8, true);
 *
 * // Custom step values
 * float[] customSteps = {-1f, -0.5f, 0f, 0.5f, 1f};
 * NoiseTransform custom = new QuantizeTransform(customSteps);
 * }</pre>
 *
 * <p><b>Extension:</b> This class is not part of the original FastNoiseLite library.
 */
public class QuantizeTransform implements NoiseTransform {

    private final float[] steps;
    private final boolean dithering;
    private final float ditherAmount;

    // Simple pseudo-random for dithering
    private int ditherState = 0;

    /**
     * Create a quantize transform with uniform steps.
     *
     * @param levels Number of quantization levels
     */
    public QuantizeTransform(int levels) {
        this(levels, false, 0f);
    }

    /**
     * Create a quantize transform with optional dithering.
     *
     * @param levels Number of quantization levels
     * @param dithering If true, add small random offset to reduce banding
     */
    public QuantizeTransform(int levels, boolean dithering) {
        this(levels, dithering, 0.5f);
    }

    /**
     * Create a quantize transform with configurable dithering.
     *
     * @param levels Number of quantization levels
     * @param dithering If true, add small random offset
     * @param ditherAmount Dither strength (0 to 1)
     */
    public QuantizeTransform(int levels, boolean dithering, float ditherAmount) {
        this.steps = generateUniformSteps(Math.max(2, levels));
        this.dithering = dithering;
        this.ditherAmount = ditherAmount;
    }

    /**
     * Create a quantize transform with custom step values.
     *
     * @param steps Array of step values in ascending order (must span input range)
     */
    public QuantizeTransform(float[] steps) {
        this(steps, false, 0f);
    }

    /**
     * Create a quantize transform with custom steps and optional dithering.
     *
     * @param steps Array of step values in ascending order
     * @param dithering If true, add small random offset
     * @param ditherAmount Dither strength
     */
    public QuantizeTransform(float[] steps, boolean dithering, float ditherAmount) {
        if (steps == null || steps.length < 2) {
            throw new IllegalArgumentException("Steps array must have at least 2 values");
        }
        this.steps = steps.clone();
        this.dithering = dithering;
        this.ditherAmount = ditherAmount;
    }

    @Override
    public float apply(float value) {
        // Apply dithering if enabled
        if (dithering) {
            float stepSize = (steps[steps.length - 1] - steps[0]) / (steps.length - 1);
            float dither = nextDither() * stepSize * ditherAmount;
            value += dither;
        }

        // Find the nearest step value
        return findNearestStep(value);
    }

    /**
     * Find the nearest step value for the given input.
     */
    private float findNearestStep(float value) {
        // Clamp to step range
        if (value <= steps[0]) {
            return steps[0];
        }
        if (value >= steps[steps.length - 1]) {
            return steps[steps.length - 1];
        }

        // Binary search for the appropriate step
        int low = 0;
        int high = steps.length - 1;

        while (low < high - 1) {
            int mid = (low + high) / 2;
            if (value < steps[mid]) {
                high = mid;
            } else {
                low = mid;
            }
        }

        // Return the nearest of the two bounding steps
        float distLow = Math.abs(value - steps[low]);
        float distHigh = Math.abs(value - steps[high]);
        return distLow <= distHigh ? steps[low] : steps[high];
    }

    /**
     * Generate uniform step values from -1 to 1.
     */
    private static float[] generateUniformSteps(int levels) {
        float[] steps = new float[levels];
        for (int i = 0; i < levels; i++) {
            steps[i] = -1f + (2f * i / (levels - 1));
        }
        return steps;
    }

    /**
     * Simple pseudo-random dither value in range [-0.5, 0.5].
     */
    private float nextDither() {
        // Simple LCG for deterministic but varied dithering
        ditherState = ditherState * 1103515245 + 12345;
        return ((ditherState >> 16) & 0xFFFF) / 65535f - 0.5f;
    }

    /**
     * Get the number of quantization levels.
     */
    public int getLevels() {
        return steps.length;
    }

    /**
     * Get a copy of the step values.
     */
    public float[] getSteps() {
        return steps.clone();
    }

    /**
     * Check if dithering is enabled.
     */
    public boolean isDithering() {
        return dithering;
    }

    /**
     * Create a posterize transform (for image-like effects).
     *
     * @param levels Number of levels (typical: 4, 8, 16)
     * @return QuantizeTransform configured for posterization
     */
    public static QuantizeTransform posterize(int levels) {
        return new QuantizeTransform(levels, false, 0f);
    }

    /**
     * Create a dithered quantize transform (reduces banding).
     *
     * @param levels Number of levels
     * @return QuantizeTransform with dithering enabled
     */
    public static QuantizeTransform dithered(int levels) {
        return new QuantizeTransform(levels, true, 0.5f);
    }

    /**
     * Create a quantize transform with exponential step distribution.
     * Useful for heightmaps where more detail is needed at lower values.
     *
     * @param levels Number of levels
     * @param exponent Exponent for step distribution (> 1 concentrates at low end)
     * @return QuantizeTransform with exponential steps
     */
    public static QuantizeTransform exponential(int levels, float exponent) {
        float[] steps = new float[levels];
        for (int i = 0; i < levels; i++) {
            float t = (float) i / (levels - 1);
            float curved = (float) Math.pow(t, exponent);
            steps[i] = -1f + curved * 2f;
        }
        return new QuantizeTransform(steps, false, 0f);
    }
}
