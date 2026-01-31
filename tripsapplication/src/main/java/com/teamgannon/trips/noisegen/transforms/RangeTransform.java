package com.teamgannon.trips.noisegen.transforms;

/**
 * Transforms noise values from one range to another.
 *
 * <p>By default, noise values are in the range [-1, 1]. This transform
 * can remap them to any desired range.
 *
 * <p>Example usage:
 * <pre>{@code
 * // Remap from [-1, 1] to [0, 1]
 * NoiseTransform toPositive = new RangeTransform(-1, 1, 0, 1);
 *
 * // Remap from [-1, 1] to [0, 255] for color values
 * NoiseTransform toColor = new RangeTransform(-1, 1, 0, 255);
 * }</pre>
 */
public class RangeTransform implements NoiseTransform {

    private final float inputMin;
    private final float inputMax;
    private final float outputMin;
    private final float outputMax;
    private final float scale;
    private final float offset;

    /**
     * Create a range transform from [-1, 1] to [0, 1].
     */
    public RangeTransform() {
        this(-1f, 1f, 0f, 1f);
    }

    /**
     * Create a range transform.
     *
     * @param inputMin  The minimum expected input value (default: -1)
     * @param inputMax  The maximum expected input value (default: 1)
     * @param outputMin The minimum output value
     * @param outputMax The maximum output value
     */
    public RangeTransform(float inputMin, float inputMax, float outputMin, float outputMax) {
        this.inputMin = inputMin;
        this.inputMax = inputMax;
        this.outputMin = outputMin;
        this.outputMax = outputMax;

        // Precompute scale and offset for efficiency
        float inputRange = inputMax - inputMin;
        float outputRange = outputMax - outputMin;
        this.scale = outputRange / inputRange;
        this.offset = outputMin - (inputMin * scale);
    }

    @Override
    public float apply(float value) {
        return value * scale + offset;
    }

    @Override
    public String getDescription() {
        return String.format("RangeTransform[%.2f,%.2f] -> [%.2f,%.2f]",
                inputMin, inputMax, outputMin, outputMax);
    }

    /**
     * Convenience factory for creating a [0, 1] normalized transform.
     */
    public static RangeTransform normalize() {
        return new RangeTransform(-1f, 1f, 0f, 1f);
    }

    /**
     * Convenience factory for creating a [0, max] transform.
     */
    public static RangeTransform toMax(float max) {
        return new RangeTransform(-1f, 1f, 0f, max);
    }

    /**
     * Convenience factory for creating a [min, max] transform from normalized [0, 1] input.
     */
    public static RangeTransform fromNormalized(float min, float max) {
        return new RangeTransform(0f, 1f, min, max);
    }
}
