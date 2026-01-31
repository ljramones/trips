package com.teamgannon.trips.particlefields;

/**
 * Color gradient modes for nebula particle coloring.
 */
public enum ColorGradientMode {
    /**
     * Linear gradient between primary and secondary colors.
     * Simple, fast, random color assignment along the gradient.
     */
    LINEAR("Linear", "Simple gradient between two colors"),

    /**
     * Radial gradient: core color at center, edge color at boundary.
     * Color index based on distance from center.
     */
    RADIAL("Radial", "Core color fades to edge color"),

    /**
     * Noise-based coloring: color varies with noise value.
     * Creates patchy, organic-looking color distribution.
     */
    NOISE_BASED("Noise-Based", "Color varies with noise pattern"),

    /**
     * Temperature-based: hot colors at core, cool colors at edge.
     * Simulates emission nebula temperature distribution.
     */
    TEMPERATURE("Temperature", "Hot core, cool edges"),

    /**
     * Multi-zone: three color bands from core to edge.
     * Uses primary, tertiary, and secondary colors in bands.
     */
    MULTI_ZONE("Multi-Zone", "Three color bands");

    private final String displayName;
    private final String description;

    ColorGradientMode(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Parse a string to ColorGradientMode, defaulting to LINEAR.
     */
    public static ColorGradientMode fromString(String value) {
        if (value == null || value.isBlank()) {
            return LINEAR;
        }
        try {
            return valueOf(value.toUpperCase().replace("-", "_"));
        } catch (IllegalArgumentException e) {
            return LINEAR;
        }
    }
}
