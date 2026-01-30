package com.teamgannon.trips.jpa.model;

/**
 * Categorizes different types of nebulae for the interstellar view.
 * Each type has distinct visual characteristics and generation parameters.
 */
public enum NebulaType {

    /**
     * Emission nebula: Glowing gas cloud ionized by nearby hot stars.
     * Characterized by H-alpha (pink/red) and OIII (blue-green) emission.
     * Examples: Orion Nebula (M42), Eagle Nebula (M16), Lagoon Nebula (M8)
     */
    EMISSION("Emission Nebula", "Glowing ionized gas cloud"),

    /**
     * Dark nebula: Dense dust cloud that obscures background stars.
     * Appears as dark patches against the stellar background.
     * Examples: Horsehead Nebula, Coalsack, Barnard 68
     */
    DARK("Dark Nebula", "Obscuring dust cloud"),

    /**
     * Reflection nebula: Dust cloud reflecting light from nearby stars.
     * Appears blue due to preferential scattering of shorter wavelengths.
     * Examples: Witch Head Nebula, Pleiades nebulosity
     */
    REFLECTION("Reflection Nebula", "Dust reflecting starlight"),

    /**
     * Planetary nebula: Expanding shell of gas ejected by a dying star.
     * Often shows ionized oxygen (green) and nitrogen (purple) emission.
     * Examples: Ring Nebula (M57), Helix Nebula, Cat's Eye Nebula
     */
    PLANETARY("Planetary Nebula", "Expanding shell from dying star"),

    /**
     * Supernova remnant: Expanding debris from a stellar explosion.
     * Shows complex filamentary structure from shock waves.
     * Examples: Crab Nebula (M1), Veil Nebula, Cassiopeia A
     */
    SUPERNOVA_REMNANT("Supernova Remnant", "Stellar explosion debris");

    private final String displayName;
    private final String description;

    NebulaType(String displayName, String description) {
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
     * Get default primary color for this nebula type (as hex string).
     */
    public String getDefaultPrimaryColor() {
        return switch (this) {
            case EMISSION -> "#FF6496";         // H-alpha pink
            case DARK -> "#282320";             // Very dark brown
            case REFLECTION -> "#6496FF";       // Blue
            case PLANETARY -> "#64FFAA";        // Ionized oxygen green
            case SUPERNOVA_REMNANT -> "#FF9664"; // Orange-red
        };
    }

    /**
     * Get default secondary color for this nebula type (as hex string).
     */
    public String getDefaultSecondaryColor() {
        return switch (this) {
            case EMISSION -> "#64C8FF";         // OIII blue-green
            case DARK -> "#14120F";             // Nearly black
            case REFLECTION -> "#96B4FF";       // Lighter blue
            case PLANETARY -> "#C864FF";        // Ionized nitrogen purple
            case SUPERNOVA_REMNANT -> "#FFDC64"; // Yellow
        };
    }

    /**
     * Get default radial power (density falloff) for this nebula type.
     * Lower values = denser core, higher values = more shell-like.
     */
    public double getDefaultRadialPower() {
        return switch (this) {
            case EMISSION -> 0.4;           // Moderate core concentration
            case DARK -> 0.5;               // Fairly uniform
            case REFLECTION -> 0.35;        // Concentrated near illuminating star
            case PLANETARY -> 0.7;          // Shell-like (hollow center)
            case SUPERNOVA_REMNANT -> 0.65; // Expanding shell
        };
    }

    /**
     * Get default noise strength for this nebula type.
     * Higher values = more filamentary structure.
     */
    public double getDefaultNoiseStrength() {
        return switch (this) {
            case EMISSION -> 0.4;           // Moderate filaments
            case DARK -> 0.3;               // Some structure
            case REFLECTION -> 0.2;         // Smoother
            case PLANETARY -> 0.25;         // Some asymmetry
            case SUPERNOVA_REMNANT -> 0.6;  // Complex filaments
        };
    }

    /**
     * Get default noise octaves (detail level) for this nebula type.
     * More octaves = finer detail but higher computational cost.
     */
    public int getDefaultNoiseOctaves() {
        return switch (this) {
            case EMISSION -> 3;             // Good detail
            case DARK -> 3;                 // Moderate
            case REFLECTION -> 2;           // Smoother
            case PLANETARY -> 3;            // Good detail
            case SUPERNOVA_REMNANT -> 4;    // High detail for filaments
        };
    }

    /**
     * Get default particle density (particles per cubic light-year).
     */
    public double getDefaultParticleDensity() {
        return switch (this) {
            case EMISSION -> 0.005;
            case DARK -> 0.008;
            case REFLECTION -> 0.004;
            case PLANETARY -> 0.01;
            case SUPERNOVA_REMNANT -> 0.006;
        };
    }

    /**
     * Whether animation should be enabled by default.
     */
    public boolean isAnimationEnabledByDefault() {
        return switch (this) {
            case EMISSION, REFLECTION -> true;  // Slow turbulent motion
            case DARK -> false;                  // Static
            case PLANETARY, SUPERNOVA_REMNANT -> true;  // Expanding
        };
    }
}
