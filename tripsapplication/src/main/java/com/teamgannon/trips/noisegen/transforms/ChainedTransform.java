package com.teamgannon.trips.noisegen.transforms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Chains multiple transforms together, applying them in sequence.
 *
 * <p>The transforms are applied in the order they are added, with each
 * transform receiving the output of the previous one.
 *
 * <p>Example usage:
 * <pre>{@code
 * // Create a chain: ridge -> power -> normalize to [0, 1]
 * NoiseTransform chain = new ChainedTransform(
 *     new RidgeTransform(),
 *     new PowerTransform(2.0f, false),
 *     new RangeTransform(0, 1, 0, 255)
 * );
 *
 * float noise = generator.GetNoise(x, y);
 * float result = chain.apply(noise); // Result in [0, 255]
 * }</pre>
 */
public class ChainedTransform implements NoiseTransform {

    private final List<NoiseTransform> transforms;

    /**
     * Create a chained transform from an array of transforms.
     *
     * @param transforms The transforms to chain, applied in order
     */
    public ChainedTransform(NoiseTransform... transforms) {
        this.transforms = new ArrayList<>(Arrays.asList(transforms));
    }

    /**
     * Create a chained transform from a list of transforms.
     *
     * @param transforms The transforms to chain, applied in order
     */
    public ChainedTransform(List<NoiseTransform> transforms) {
        this.transforms = new ArrayList<>(transforms);
    }

    /**
     * Add a transform to the end of the chain.
     *
     * @param transform The transform to add
     * @return This ChainedTransform for method chaining
     */
    public ChainedTransform add(NoiseTransform transform) {
        transforms.add(transform);
        return this;
    }

    /**
     * Add a transform at the beginning of the chain.
     *
     * @param transform The transform to add
     * @return This ChainedTransform for method chaining
     */
    public ChainedTransform prepend(NoiseTransform transform) {
        transforms.add(0, transform);
        return this;
    }

    @Override
    public float apply(float value) {
        float result = value;
        for (NoiseTransform transform : transforms) {
            result = transform.apply(result);
        }
        return result;
    }

    @Override
    public String getDescription() {
        StringBuilder sb = new StringBuilder("ChainedTransform[");
        for (int i = 0; i < transforms.size(); i++) {
            if (i > 0) sb.append(" -> ");
            sb.append(transforms.get(i).getDescription());
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Get the number of transforms in the chain.
     */
    public int size() {
        return transforms.size();
    }

    /**
     * Check if the chain is empty.
     */
    public boolean isEmpty() {
        return transforms.isEmpty();
    }
}
