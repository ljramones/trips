package com.teamgannon.trips.noisegen.transforms;

/**
 * Inverts noise values by negating them.
 *
 * <p>This simple transform flips the sign of values, turning peaks into valleys
 * and vice versa. Useful for creating inverse patterns.
 *
 * <p>Example usage:
 * <pre>{@code
 * NoiseTransform invert = new InvertTransform();
 * float inverted = invert.apply(0.5f); // Returns -0.5f
 * }</pre>
 */
public class InvertTransform implements NoiseTransform {

    /**
     * Create an invert transform.
     */
    public InvertTransform() {
    }

    @Override
    public float apply(float value) {
        return -value;
    }

    @Override
    public String getDescription() {
        return "InvertTransform";
    }
}
