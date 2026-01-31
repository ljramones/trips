package com.teamgannon.trips.planetarymodelling.procedural.biome;

import com.teamgannon.trips.planetarymodelling.procedural.ClimateCalculator;
import com.teamgannon.trips.planetarymodelling.procedural.ErosionCalculator;

import java.util.List;

/**
 * Classifies polygons into biome types based on climate, elevation, and rainfall.
 *
 * <p>Classification follows a Whittaker-inspired model where biomes are determined by:
 * <ul>
 *   <li>Temperature (via climate zone from latitude)</li>
 *   <li>Precipitation (from erosion calculation)</li>
 *   <li>Elevation (height value)</li>
 * </ul>
 *
 * <p>The classifier handles edge cases like:
 * <ul>
 *   <li>Ocean depths (deep ocean vs shelf)</li>
 *   <li>Lake basins (freshwater biome)</li>
 *   <li>Alpine zones at high elevation</li>
 *   <li>Coastal regions at land-water boundaries</li>
 * </ul>
 */
public class BiomeClassifier {

    // Rainfall thresholds (normalized values from erosion calculator)
    private static final double RAINFALL_VERY_LOW = 0.2;   // Desert threshold
    private static final double RAINFALL_LOW = 0.4;        // Semi-arid
    private static final double RAINFALL_MODERATE = 0.6;   // Moderate
    private static final double RAINFALL_HIGH = 0.8;       // Humid

    // Height thresholds (integer scale from elevation calculator)
    private static final int HEIGHT_DEEP_OCEAN = -3;
    private static final int HEIGHT_OCEAN = -1;
    private static final int HEIGHT_COASTAL_MAX = 0;
    private static final int HEIGHT_LOWLAND_MAX = 1;
    private static final int HEIGHT_HIGHLAND_MIN = 2;
    private static final int HEIGHT_MOUNTAIN_MIN = 3;

    private final int[] heights;
    private final ClimateCalculator.ClimateZone[] climates;
    private final double[] rainfall;
    private final boolean[] lakeMask;
    private final int[] coastalMask;  // Distance to ocean (0 = coastal, -1 = inland)

    /**
     * Creates a new BiomeClassifier.
     *
     * @param heights Integer height values for each polygon
     * @param climates Climate zone assignments
     * @param erosionResult Erosion result containing rainfall and lake data
     * @param adjacency Adjacency information for coastal detection (indices)
     */
    public BiomeClassifier(
            int[] heights,
            ClimateCalculator.ClimateZone[] climates,
            ErosionCalculator.ErosionResult erosionResult,
            int[][] adjacencies) {

        this.heights = heights;
        this.climates = climates;
        this.rainfall = erosionResult != null ? erosionResult.rainfall() : new double[heights.length];
        this.lakeMask = erosionResult != null ? erosionResult.lakeMask() : null;
        this.coastalMask = computeCoastalMask(adjacencies);
    }

    /**
     * Classifies all polygons into biome types.
     *
     * @return Array of BiomeType for each polygon
     */
    public BiomeType[] classify() {
        BiomeType[] biomes = new BiomeType[heights.length];

        for (int i = 0; i < heights.length; i++) {
            biomes[i] = classifyPolygon(i);
        }

        return biomes;
    }

    /**
     * Classifies a single polygon.
     */
    public BiomeType classifyPolygon(int index) {
        int height = heights[index];
        ClimateCalculator.ClimateZone climate = climates[index];
        double rain = rainfall != null && index < rainfall.length ? rainfall[index] : 0.5;

        // Check for lake (from erosion basin filling)
        if (lakeMask != null && lakeMask[index]) {
            return BiomeType.FRESHWATER;
        }

        // Water biomes (ocean/sea)
        if (height <= HEIGHT_OCEAN) {
            if (height <= HEIGHT_DEEP_OCEAN) {
                return BiomeType.DEEP_OCEAN;
            }
            return BiomeType.OCEAN;
        }

        // High elevation biomes (supersede climate)
        if (height >= HEIGHT_MOUNTAIN_MIN) {
            return BiomeType.MOUNTAIN;
        }

        if (height >= HEIGHT_HIGHLAND_MIN && climate != ClimateCalculator.ClimateZone.TROPICAL) {
            return BiomeType.ALPINE;
        }

        // Coastal check (adjacent to ocean)
        if (coastalMask != null && coastalMask[index] == 0 && height <= HEIGHT_COASTAL_MAX) {
            return BiomeType.COASTAL;
        }

        // Very dry regions become desert regardless of temperature
        if (rain < RAINFALL_VERY_LOW) {
            return BiomeType.DESERT;
        }

        // Climate-based classification
        return switch (climate) {
            case POLAR -> classifyPolar(rain);
            case TEMPERATE -> classifyTemperate(rain, height);
            case TROPICAL -> classifyTropical(rain, height);
        };
    }

    /**
     * Classifies polar regions.
     */
    private BiomeType classifyPolar(double rain) {
        if (rain < RAINFALL_LOW) {
            return BiomeType.ICE_CAP;
        }
        if (rain < RAINFALL_MODERATE) {
            return BiomeType.TUNDRA;
        }
        // Wetter polar regions have boreal forest at lower latitudes
        return BiomeType.BOREAL_FOREST;
    }

    /**
     * Classifies temperate regions.
     */
    private BiomeType classifyTemperate(double rain, int height) {
        if (rain < RAINFALL_LOW) {
            return BiomeType.TEMPERATE_GRASSLAND;  // Prairie/steppe
        }

        if (rain < RAINFALL_MODERATE) {
            // Low-lying wet areas become wetlands
            if (height == 0 && rain > RAINFALL_LOW + 0.1) {
                return BiomeType.WETLAND;
            }
            return BiomeType.TEMPERATE_GRASSLAND;
        }

        if (rain < RAINFALL_HIGH) {
            return BiomeType.TEMPERATE_FOREST;
        }

        return BiomeType.TEMPERATE_RAINFOREST;
    }

    /**
     * Classifies tropical regions.
     */
    private BiomeType classifyTropical(double rain, int height) {
        if (rain < RAINFALL_LOW) {
            return BiomeType.DESERT;  // Hot desert
        }

        if (rain < RAINFALL_MODERATE) {
            return BiomeType.SAVANNA;
        }

        if (rain < RAINFALL_HIGH) {
            // Low-lying tropical wet areas
            if (height == 0) {
                return BiomeType.WETLAND;
            }
            return BiomeType.SAVANNA;
        }

        return BiomeType.TROPICAL_RAINFOREST;
    }

    /**
     * Computes which polygons are coastal (adjacent to ocean).
     *
     * @return Array where 0 = coastal, -1 = not coastal
     */
    private int[] computeCoastalMask(int[][] adjacencies) {
        if (adjacencies == null) {
            return null;
        }

        int[] mask = new int[heights.length];
        java.util.Arrays.fill(mask, -1);

        for (int i = 0; i < heights.length; i++) {
            // Only consider land polygons
            if (heights[i] < 0) continue;

            // Check if any neighbor is ocean
            int[] neighbors = adjacencies[i];
            for (int j = 1; j < neighbors.length; j++) {  // Skip first (self)
                int neighbor = neighbors[j];
                if (heights[neighbor] < 0) {
                    mask[i] = 0;  // This polygon is coastal
                    break;
                }
            }
        }

        return mask;
    }

    /**
     * Static factory method for easy classification.
     *
     * @param heights Height values
     * @param climates Climate zones
     * @param erosionResult Erosion calculation results
     * @param adjacencies Neighbor arrays (from AdjacencyGraph)
     * @return Classified biome array
     */
    public static BiomeType[] classify(
            int[] heights,
            ClimateCalculator.ClimateZone[] climates,
            ErosionCalculator.ErosionResult erosionResult,
            int[][] adjacencies) {

        return new BiomeClassifier(heights, climates, erosionResult, adjacencies).classify();
    }

    /**
     * Gets biome distribution statistics.
     *
     * @param biomes Classified biome array
     * @return Map of biome type to count
     */
    public static java.util.Map<BiomeType, Integer> getDistribution(BiomeType[] biomes) {
        java.util.Map<BiomeType, Integer> distribution = new java.util.EnumMap<>(BiomeType.class);
        for (BiomeType biome : BiomeType.values()) {
            distribution.put(biome, 0);
        }
        for (BiomeType biome : biomes) {
            distribution.merge(biome, 1, Integer::sum);
        }
        return distribution;
    }

    /**
     * Calculates the land percentage covered by each biome.
     *
     * @param biomes Classified biome array
     * @return Map of biome type to percentage (0.0 to 1.0)
     */
    public static java.util.Map<BiomeType, Double> getLandDistribution(BiomeType[] biomes) {
        java.util.Map<BiomeType, Integer> counts = getDistribution(biomes);

        // Count total land polygons
        int landTotal = 0;
        for (BiomeType biome : BiomeType.values()) {
            if (biome.isLand()) {
                landTotal += counts.get(biome);
            }
        }

        // Calculate percentages
        java.util.Map<BiomeType, Double> percentages = new java.util.EnumMap<>(BiomeType.class);
        for (BiomeType biome : BiomeType.values()) {
            if (biome.isLand() && landTotal > 0) {
                percentages.put(biome, counts.get(biome) / (double) landTotal);
            } else {
                percentages.put(biome, 0.0);
            }
        }

        return percentages;
    }
}
