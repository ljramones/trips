package com.teamgannon.trips.noisegen.transforms;

/**
 * Interface for noise value transformations.
 * Transforms are applied after noise generation to modify the output values.
 *
 * <p>Common use cases:
 * <ul>
 *   <li>Remapping noise from [-1,1] to [0,1] or custom ranges</li>
 *   <li>Applying power curves for sharper/softer features</li>
 *   <li>Creating ridged patterns from standard noise</li>
 *   <li>Chaining multiple transforms together</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>{@code
 * NoiseTransform ridge = new RidgeTransform();
 * NoiseTransform power = new PowerTransform(2.0f);
 * NoiseTransform chain = new ChainedTransform(ridge, power);
 *
 * float noise = generator.GetNoise(x, y);
 * float transformed = chain.apply(noise);
 * }</pre>
 */
public interface NoiseTransform {

    /**
     * Apply the transformation to a noise value.
     *
     * @param value The input noise value, typically in range [-1, 1]
     * @return The transformed noise value
     */
    float apply(float value);

    /**
     * Get a description of this transform for debugging/logging.
     *
     * @return A human-readable description
     */
    default String getDescription() {
        return getClass().getSimpleName();
    }
}
