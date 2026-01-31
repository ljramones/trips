package com.teamgannon.trips.noisegen.transforms;

/**
 * Creates turbulent, billowy patterns using absolute value.
 *
 * <p>Turbulence is similar to ridge transform but focuses on creating
 * cloud-like or smoke-like effects. It takes the absolute value of
 * the noise, creating sharp boundaries at zero crossings.
 *
 * <p>Use cases:
 * <ul>
 *   <li>Cloud and smoke effects</li>
 *   <li>Marble textures</li>
 *   <li>Fire and flame patterns</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>{@code
 * NoiseTransform turbulence = new TurbulenceTransform();
 * float value = turbulence.apply(noise.GetNoise(x, y));
 * }</pre>
 */
public class TurbulenceTransform implements NoiseTransform {

    private final float scale;
    private final float offset;

    /**
     * Create a standard turbulence transform.
     * Output range is [0, 1].
     */
    public TurbulenceTransform() {
        this(1.0f, 0.0f);
    }

    /**
     * Create a turbulence transform with scale and offset.
     *
     * @param scale  Scale factor applied after absolute value
     * @param offset Offset added to the result
     */
    public TurbulenceTransform(float scale, float offset) {
        this.scale = scale;
        this.offset = offset;
    }

    @Override
    public float apply(float value) {
        return Math.abs(value) * scale + offset;
    }

    @Override
    public String getDescription() {
        return String.format("TurbulenceTransform[scale=%.2f, offset=%.2f]", scale, offset);
    }
}
