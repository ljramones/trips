package com.teamgannon.trips.noisegen.transforms;

/**
 * Creates ridge-like patterns from noise by reflecting negative values.
 *
 * <p>Ridge transforms are useful for:
 * <ul>
 *   <li>Creating mountain ridge patterns</li>
 *   <li>Sharp terrain features</li>
 *   <li>Vein-like structures</li>
 * </ul>
 *
 * <p>The basic ridge operation takes the absolute value and optionally inverts it
 * to create valleys instead of ridges.
 *
 * <p>Example usage:
 * <pre>{@code
 * // Standard ridges (peaks at zero crossings)
 * NoiseTransform ridges = new RidgeTransform();
 *
 * // Inverted ridges (valleys at zero crossings)
 * NoiseTransform valleys = new RidgeTransform(true);
 *
 * // Sharp ridges with power curve
 * NoiseTransform sharpRidges = new RidgeTransform(false, 2.0f);
 * }</pre>
 */
public class RidgeTransform implements NoiseTransform {

    private final boolean invert;
    private final float exponent;

    /**
     * Create a standard ridge transform.
     */
    public RidgeTransform() {
        this(false, 1.0f);
    }

    /**
     * Create a ridge transform with optional inversion.
     *
     * @param invert If true, creates valleys instead of ridges
     */
    public RidgeTransform(boolean invert) {
        this(invert, 1.0f);
    }

    /**
     * Create a ridge transform with inversion and power curve.
     *
     * @param invert   If true, creates valleys instead of ridges
     * @param exponent Power curve to apply (1.0 = linear, 2.0 = sharper)
     */
    public RidgeTransform(boolean invert, float exponent) {
        this.invert = invert;
        this.exponent = exponent;
    }

    @Override
    public float apply(float value) {
        // Take absolute value to create ridge at zero crossing
        float ridge = Math.abs(value);

        // Apply power curve if specified
        if (exponent != 1.0f) {
            ridge = (float) Math.pow(ridge, exponent);
        }

        // Invert if requested (ridges become valleys)
        if (invert) {
            ridge = 1.0f - ridge;
        }

        // Return in range [0, 1]
        return ridge;
    }

    @Override
    public String getDescription() {
        return String.format("RidgeTransform[invert=%b, exp=%.2f]", invert, exponent);
    }
}
