package com.teamgannon.trips.noisegen.transforms;

/**
 * Applies a power curve to noise values.
 *
 * <p>Power transforms are useful for:
 * <ul>
 *   <li>Sharpening features (exponent > 1)</li>
 *   <li>Softening features (exponent < 1)</li>
 *   <li>Creating more dramatic terrain variations</li>
 * </ul>
 *
 * <p>The transform first normalizes the input to [0, 1], applies the power,
 * then optionally remaps back to [-1, 1].
 *
 * <p>Example usage:
 * <pre>{@code
 * // Sharpen features (more extreme values)
 * NoiseTransform sharp = new PowerTransform(2.0f);
 *
 * // Soften features (more mid-range values)
 * NoiseTransform soft = new PowerTransform(0.5f);
 * }</pre>
 */
public class PowerTransform implements NoiseTransform {

    private final float exponent;
    private final boolean keepSignedRange;

    /**
     * Create a power transform with signed output (back to [-1, 1]).
     *
     * @param exponent The power exponent (1.0 = no change)
     */
    public PowerTransform(float exponent) {
        this(exponent, true);
    }

    /**
     * Create a power transform.
     *
     * @param exponent         The power exponent (1.0 = no change)
     * @param keepSignedRange If true, output is in [-1, 1]; if false, output is in [0, 1]
     */
    public PowerTransform(float exponent, boolean keepSignedRange) {
        this.exponent = exponent;
        this.keepSignedRange = keepSignedRange;
    }

    @Override
    public float apply(float value) {
        // Normalize to [0, 1]
        float normalized = (value + 1f) * 0.5f;

        // Apply power (preserve sign for negative normalized values near 0)
        float powered = (float) Math.pow(normalized, exponent);

        if (keepSignedRange) {
            // Remap back to [-1, 1]
            return powered * 2f - 1f;
        } else {
            return powered;
        }
    }

    @Override
    public String getDescription() {
        return String.format("PowerTransform[exp=%.2f, signed=%b]", exponent, keepSignedRange);
    }

    /**
     * Get the exponent value.
     */
    public float getExponent() {
        return exponent;
    }
}
