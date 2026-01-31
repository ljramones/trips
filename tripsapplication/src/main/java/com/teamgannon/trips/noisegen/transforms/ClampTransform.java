package com.teamgannon.trips.noisegen.transforms;

/**
 * Clamps noise values to a specified range.
 *
 * <p>This transform ensures that values stay within bounds, which is useful
 * when combining noise with other operations or when you need guaranteed limits.
 *
 * <p>Example usage:
 * <pre>{@code
 * // Clamp to standard [-1, 1] range
 * NoiseTransform clamp = new ClampTransform();
 *
 * // Clamp to [0, 1] range
 * NoiseTransform clampPositive = new ClampTransform(0, 1);
 * }</pre>
 */
public class ClampTransform implements NoiseTransform {

    private final float min;
    private final float max;

    /**
     * Create a clamp transform for the standard [-1, 1] range.
     */
    public ClampTransform() {
        this(-1.0f, 1.0f);
    }

    /**
     * Create a clamp transform for a custom range.
     *
     * @param min The minimum allowed value
     * @param max The maximum allowed value
     */
    public ClampTransform(float min, float max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public float apply(float value) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    @Override
    public String getDescription() {
        return String.format("ClampTransform[%.2f, %.2f]", min, max);
    }

    /**
     * Get the minimum clamp value.
     */
    public float getMin() {
        return min;
    }

    /**
     * Get the maximum clamp value.
     */
    public float getMax() {
        return max;
    }
}
