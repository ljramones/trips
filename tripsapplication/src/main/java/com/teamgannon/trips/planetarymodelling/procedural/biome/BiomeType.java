package com.teamgannon.trips.planetarymodelling.procedural.biome;

import javafx.scene.paint.Color;

/**
 * Enumeration of biome types for detailed terrain classification.
 * Maps combinations of climate zone, elevation, and rainfall to specific biomes.
 *
 * <p>Biome classification is inspired by Whittaker's biome diagram and
 * the Holdridge life zones system, adapted for procedural planet generation.
 */
public enum BiomeType {

    // ==================== Water Biomes ====================

    /**
     * Deep ocean far from continental shelf.
     * Height <= -2 (deep water).
     */
    DEEP_OCEAN("Deep Ocean", Color.rgb(0, 0, 80), false, true),

    /**
     * Continental shelf and shallow seas.
     * Height = -1 (shallow water).
     */
    OCEAN("Ocean", Color.rgb(0, 50, 150), false, true),

    /**
     * Coastal/tidal zone at ocean margins.
     * Height near 0 adjacent to water.
     */
    COASTAL("Coastal", Color.rgb(180, 180, 140), true, false),

    // ==================== Cold/Polar Biomes ====================

    /**
     * Permanent ice coverage at polar regions.
     * Polar climate with very low rainfall.
     */
    ICE_CAP("Ice Cap", Color.rgb(240, 245, 255), true, false),

    /**
     * Frozen treeless plain with permafrost.
     * Polar climate with some precipitation.
     */
    TUNDRA("Tundra", Color.rgb(180, 190, 200), true, false),

    /**
     * Northern coniferous forest (taiga).
     * Cold climate with moderate precipitation.
     */
    BOREAL_FOREST("Boreal Forest", Color.rgb(30, 80, 60), true, false),

    // ==================== Temperate Biomes ====================

    /**
     * Temperate prairie and steppe.
     * Temperate climate with moderate-low rainfall.
     */
    TEMPERATE_GRASSLAND("Temperate Grassland", Color.rgb(160, 180, 80), true, false),

    /**
     * Deciduous and mixed forest.
     * Temperate climate with moderate rainfall.
     */
    TEMPERATE_FOREST("Temperate Forest", Color.rgb(60, 120, 40), true, false),

    /**
     * Temperate rain forest (e.g., Pacific Northwest).
     * Temperate climate with high rainfall.
     */
    TEMPERATE_RAINFOREST("Temperate Rainforest", Color.rgb(40, 100, 60), true, false),

    // ==================== Hot/Arid Biomes ====================

    /**
     * Hot and cold deserts with minimal vegetation.
     * Any climate with very low rainfall.
     */
    DESERT("Desert", Color.rgb(220, 200, 140), true, false),

    /**
     * Tropical grassland with scattered trees.
     * Tropical climate with moderate rainfall and distinct dry season.
     */
    SAVANNA("Savanna", Color.rgb(180, 160, 80), true, false),

    /**
     * Dense equatorial forest.
     * Tropical climate with high rainfall.
     */
    TROPICAL_RAINFOREST("Tropical Rainforest", Color.rgb(20, 80, 30), true, false),

    // ==================== Elevation Biomes ====================

    /**
     * High altitude vegetation above treeline.
     * High elevation (height = 2) in non-tropical climates.
     */
    ALPINE("Alpine", Color.rgb(140, 130, 120), true, false),

    /**
     * High mountain peaks with bare rock and snow.
     * Very high elevation (height >= 3).
     */
    MOUNTAIN("Mountain", Color.rgb(120, 100, 90), true, false),

    // ==================== Special Biomes ====================

    /**
     * Freshwater lakes and rivers.
     * For lake-filled basins from erosion.
     */
    FRESHWATER("Freshwater", Color.rgb(100, 150, 200), false, true),

    /**
     * Wetland marshes and swamps.
     * Low elevation with high water table.
     */
    WETLAND("Wetland", Color.rgb(80, 120, 80), true, false);

    private final String displayName;
    private final Color defaultColor;
    private final boolean isLand;
    private final boolean isWater;

    BiomeType(String displayName, Color defaultColor, boolean isLand, boolean isWater) {
        this.displayName = displayName;
        this.defaultColor = defaultColor;
        this.isLand = isLand;
        this.isWater = isWater;
    }

    /**
     * Returns a human-readable display name for this biome.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the default visualization color for this biome.
     */
    public Color getDefaultColor() {
        return defaultColor;
    }

    /**
     * Returns true if this biome is a land biome (not water).
     */
    public boolean isLand() {
        return isLand;
    }

    /**
     * Returns true if this biome is a water biome.
     */
    public boolean isWater() {
        return isWater;
    }

    /**
     * Returns true if this biome supports vegetation growth.
     */
    public boolean supportsVegetation() {
        return switch (this) {
            case DEEP_OCEAN, OCEAN, FRESHWATER, ICE_CAP, DESERT, MOUNTAIN -> false;
            default -> true;
        };
    }

    /**
     * Returns the relative habitability score (0.0 to 1.0) for human settlement.
     * Higher values indicate more suitable conditions.
     */
    public double getHabitabilityScore() {
        return switch (this) {
            case TEMPERATE_FOREST -> 1.0;
            case TEMPERATE_GRASSLAND -> 0.95;
            case TEMPERATE_RAINFOREST -> 0.85;
            case COASTAL -> 0.9;
            case SAVANNA -> 0.7;
            case BOREAL_FOREST -> 0.6;
            case TROPICAL_RAINFOREST -> 0.5;
            case WETLAND -> 0.4;
            case ALPINE -> 0.3;
            case TUNDRA -> 0.2;
            case DESERT -> 0.15;
            case MOUNTAIN -> 0.1;
            case ICE_CAP -> 0.05;
            case OCEAN, DEEP_OCEAN, FRESHWATER -> 0.0;
        };
    }

    /**
     * Returns the relative agricultural potential (0.0 to 1.0).
     */
    public double getAgriculturalPotential() {
        return switch (this) {
            case TEMPERATE_GRASSLAND -> 1.0;
            case TEMPERATE_FOREST -> 0.9;
            case SAVANNA -> 0.7;
            case TEMPERATE_RAINFOREST -> 0.6;
            case TROPICAL_RAINFOREST -> 0.5;
            case COASTAL -> 0.4;
            case WETLAND -> 0.3;
            case BOREAL_FOREST -> 0.25;
            case ALPINE -> 0.1;
            case TUNDRA -> 0.05;
            case DESERT, MOUNTAIN, ICE_CAP, OCEAN, DEEP_OCEAN, FRESHWATER -> 0.0;
        };
    }

    @Override
    public String toString() {
        return displayName;
    }
}
