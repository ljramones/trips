package com.teamgannon.trips.noisegen.transforms;

/**
 * Transform that creates terrace/step patterns in noise.
 *
 * <p>Converts smooth noise gradients into discrete stepped levels,
 * useful for:
 * <ul>
 *   <li>Terraced terrain (rice paddies, geological strata)</li>
 *   <li>Stylized contour maps</li>
 *   <li>Posterization effects</li>
 *   <li>Level-based game terrain</li>
 * </ul>
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * // Create 8-level terrace
 * NoiseTransform terrace = new TerraceTransform(8);
 * float stepped = terrace.apply(noiseValue);
 *
 * // Smooth terraces (blend between steps)
 * NoiseTransform smooth = new TerraceTransform(8, 0.3f);
 *
 * // Inverted terraces (peaks instead of plateaus)
 * NoiseTransform inverted = new TerraceTransform(8, 0f, true);
 * }</pre>
 *
 * <p><b>Extension:</b> This class is not part of the original FastNoiseLite library.
 */
public class TerraceTransform implements NoiseTransform {

    private final int levels;
    private final float smoothness;
    private final boolean inverted;

    /**
     * Create a terrace transform with sharp steps.
     *
     * @param levels Number of discrete levels (2 or more)
     */
    public TerraceTransform(int levels) {
        this(levels, 0f, false);
    }

    /**
     * Create a terrace transform with configurable smoothness.
     *
     * @param levels Number of discrete levels (2 or more)
     * @param smoothness Blend amount between steps (0 = sharp, 1 = smooth)
     */
    public TerraceTransform(int levels, float smoothness) {
        this(levels, smoothness, false);
    }

    /**
     * Create a fully configurable terrace transform.
     *
     * @param levels Number of discrete levels (2 or more)
     * @param smoothness Blend amount between steps (0 = sharp, 1 = smooth)
     * @param inverted If true, creates peaks instead of plateaus
     */
    public TerraceTransform(int levels, float smoothness, boolean inverted) {
        this.levels = Math.max(2, levels);
        this.smoothness = Math.max(0f, Math.min(1f, smoothness));
        this.inverted = inverted;
    }

    @Override
    public float apply(float value) {
        // Normalize to [0, 1] range
        float normalized = (value + 1f) * 0.5f;

        // Calculate step
        float scaled = normalized * (levels - 1);
        int lower = (int) Math.floor(scaled);
        int upper = Math.min(lower + 1, levels - 1);
        float fraction = scaled - lower;

        // Apply smoothness (smoothstep interpolation)
        float blendedFraction;
        if (smoothness > 0f) {
            // Smoothstep for gradual transitions
            float t = fraction * fraction * (3f - 2f * fraction);
            blendedFraction = smoothness * t + (1f - smoothness) * (fraction < 0.5f ? 0f : 1f);
        } else {
            // Sharp steps
            blendedFraction = fraction < 0.5f ? 0f : 1f;
        }

        float stepped;
        if (inverted) {
            // Create peaks at step boundaries
            float peakFraction = 1f - Math.abs(fraction - 0.5f) * 2f;
            stepped = (lower + peakFraction) / (levels - 1);
        } else {
            // Standard terracing (plateaus)
            stepped = (lower + blendedFraction) / (levels - 1);
        }

        // Return to [-1, 1] range
        return stepped * 2f - 1f;
    }

    /**
     * Get number of levels.
     */
    public int getLevels() {
        return levels;
    }

    /**
     * Get smoothness value.
     */
    public float getSmoothness() {
        return smoothness;
    }

    /**
     * Check if inverted.
     */
    public boolean isInverted() {
        return inverted;
    }

    /**
     * Create a terrace transform for contour lines.
     *
     * @param levels Number of contour levels
     * @return TerraceTransform configured for contour-like output
     */
    public static TerraceTransform contours(int levels) {
        return new TerraceTransform(levels, 0f, false);
    }

    /**
     * Create a smooth terrace transform.
     *
     * @param levels Number of levels
     * @return TerraceTransform with smooth transitions
     */
    public static TerraceTransform smooth(int levels) {
        return new TerraceTransform(levels, 0.5f, false);
    }
}
