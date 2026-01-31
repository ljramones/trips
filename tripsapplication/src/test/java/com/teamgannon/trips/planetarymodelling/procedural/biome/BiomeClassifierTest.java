package com.teamgannon.trips.planetarymodelling.procedural.biome;

import com.teamgannon.trips.planetarymodelling.procedural.ClimateCalculator;
import com.teamgannon.trips.planetarymodelling.procedural.ErosionCalculator;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for BiomeClassifier.
 */
class BiomeClassifierTest {

    @Test
    void testDeepOceanClassification() {
        int[] heights = {-4, -3};
        ClimateCalculator.ClimateZone[] climates = {
            ClimateCalculator.ClimateZone.TEMPERATE,
            ClimateCalculator.ClimateZone.TEMPERATE
        };

        BiomeType[] biomes = BiomeClassifier.classify(heights, climates, null, null);

        assertEquals(BiomeType.DEEP_OCEAN, biomes[0]);
        assertEquals(BiomeType.DEEP_OCEAN, biomes[1]);
    }

    @Test
    void testOceanClassification() {
        int[] heights = {-2, -1};
        ClimateCalculator.ClimateZone[] climates = {
            ClimateCalculator.ClimateZone.TROPICAL,
            ClimateCalculator.ClimateZone.TROPICAL
        };

        BiomeType[] biomes = BiomeClassifier.classify(heights, climates, null, null);

        assertEquals(BiomeType.OCEAN, biomes[0]);
        assertEquals(BiomeType.OCEAN, biomes[1]);
    }

    @Test
    void testMountainClassification() {
        int[] heights = {3, 4};
        ClimateCalculator.ClimateZone[] climates = {
            ClimateCalculator.ClimateZone.TEMPERATE,
            ClimateCalculator.ClimateZone.TROPICAL
        };

        BiomeType[] biomes = BiomeClassifier.classify(heights, climates, null, null);

        assertEquals(BiomeType.MOUNTAIN, biomes[0]);
        assertEquals(BiomeType.MOUNTAIN, biomes[1]);
    }

    @Test
    void testAlpineClassification() {
        int[] heights = {2, 2};
        ClimateCalculator.ClimateZone[] climates = {
            ClimateCalculator.ClimateZone.TEMPERATE,
            ClimateCalculator.ClimateZone.POLAR
        };

        BiomeType[] biomes = BiomeClassifier.classify(heights, climates, null, null);

        assertEquals(BiomeType.ALPINE, biomes[0]);
        assertEquals(BiomeType.ALPINE, biomes[1]);
    }

    @Test
    void testDesertLowRainfall() {
        // Create mock erosion result with low rainfall
        double[] rainfall = {0.1, 0.15};
        ErosionCalculator.ErosionResult erosion = new ErosionCalculator.ErosionResult(
            new int[]{1, 1},    // erodedHeights
            new double[]{1.0, 1.0},  // preciseHeights
            List.of(),         // rivers
            rainfall,          // rainfall - LOW
            null,             // frozenRiverTerminus
            null,             // flowAccumulation
            null              // lakeMask
        );

        int[] heights = {1, 1};
        ClimateCalculator.ClimateZone[] climates = {
            ClimateCalculator.ClimateZone.TROPICAL,
            ClimateCalculator.ClimateZone.TEMPERATE
        };

        BiomeType[] biomes = BiomeClassifier.classify(heights, climates, erosion, null);

        assertEquals(BiomeType.DESERT, biomes[0]);
        assertEquals(BiomeType.DESERT, biomes[1]);
    }

    @Test
    void testPolarClassifications() {
        double[] lowRainfall = {0.25, 0.5, 0.7};
        ErosionCalculator.ErosionResult erosion = new ErosionCalculator.ErosionResult(
            new int[]{1, 1, 1}, null, List.of(), lowRainfall, null, null, null
        );

        int[] heights = {1, 1, 1};
        ClimateCalculator.ClimateZone[] climates = {
            ClimateCalculator.ClimateZone.POLAR,
            ClimateCalculator.ClimateZone.POLAR,
            ClimateCalculator.ClimateZone.POLAR
        };

        BiomeType[] biomes = BiomeClassifier.classify(heights, climates, erosion, null);

        assertEquals(BiomeType.ICE_CAP, biomes[0], "Low rainfall polar should be ice cap");
        assertEquals(BiomeType.TUNDRA, biomes[1], "Moderate rainfall polar should be tundra");
        assertEquals(BiomeType.BOREAL_FOREST, biomes[2], "High rainfall polar should be boreal forest");
    }

    @Test
    void testTemperateClassifications() {
        double[] rainfalls = {0.3, 0.5, 0.7, 0.9};
        ErosionCalculator.ErosionResult erosion = new ErosionCalculator.ErosionResult(
            new int[]{1, 1, 1, 1}, null, List.of(), rainfalls, null, null, null
        );

        int[] heights = {1, 1, 1, 1};
        ClimateCalculator.ClimateZone[] climates = {
            ClimateCalculator.ClimateZone.TEMPERATE,
            ClimateCalculator.ClimateZone.TEMPERATE,
            ClimateCalculator.ClimateZone.TEMPERATE,
            ClimateCalculator.ClimateZone.TEMPERATE
        };

        BiomeType[] biomes = BiomeClassifier.classify(heights, climates, erosion, null);

        assertEquals(BiomeType.TEMPERATE_GRASSLAND, biomes[0], "Low rainfall should be grassland");
        assertEquals(BiomeType.TEMPERATE_GRASSLAND, biomes[1], "Moderate-low should be grassland");
        assertEquals(BiomeType.TEMPERATE_FOREST, biomes[2], "Moderate-high should be forest");
        assertEquals(BiomeType.TEMPERATE_RAINFOREST, biomes[3], "High rainfall should be rainforest");
    }

    @Test
    void testTropicalClassifications() {
        double[] rainfalls = {0.3, 0.5, 0.9};
        ErosionCalculator.ErosionResult erosion = new ErosionCalculator.ErosionResult(
            new int[]{1, 1, 1}, null, List.of(), rainfalls, null, null, null
        );

        int[] heights = {1, 1, 1};
        ClimateCalculator.ClimateZone[] climates = {
            ClimateCalculator.ClimateZone.TROPICAL,
            ClimateCalculator.ClimateZone.TROPICAL,
            ClimateCalculator.ClimateZone.TROPICAL
        };

        BiomeType[] biomes = BiomeClassifier.classify(heights, climates, erosion, null);

        assertEquals(BiomeType.DESERT, biomes[0], "Low rainfall tropical should be desert");
        assertEquals(BiomeType.SAVANNA, biomes[1], "Moderate rainfall tropical should be savanna");
        assertEquals(BiomeType.TROPICAL_RAINFOREST, biomes[2], "High rainfall tropical should be rainforest");
    }

    @Test
    void testFreshwaterLakeClassification() {
        boolean[] lakeMask = {true, false};
        ErosionCalculator.ErosionResult erosion = new ErosionCalculator.ErosionResult(
            new int[]{1, 1}, null, List.of(), new double[]{0.5, 0.5}, null, null, lakeMask
        );

        int[] heights = {1, 1};
        ClimateCalculator.ClimateZone[] climates = {
            ClimateCalculator.ClimateZone.TEMPERATE,
            ClimateCalculator.ClimateZone.TEMPERATE
        };

        BiomeType[] biomes = BiomeClassifier.classify(heights, climates, erosion, null);

        assertEquals(BiomeType.FRESHWATER, biomes[0], "Lake polygon should be freshwater");
        assertNotEquals(BiomeType.FRESHWATER, biomes[1], "Non-lake polygon should not be freshwater");
    }

    @Test
    void testCoastalClassification() {
        int[] heights = {0, 0, -1};  // Two land, one ocean
        ClimateCalculator.ClimateZone[] climates = {
            ClimateCalculator.ClimateZone.TEMPERATE,
            ClimateCalculator.ClimateZone.TEMPERATE,
            ClimateCalculator.ClimateZone.TEMPERATE
        };

        // Adjacency: polygon 0 is adjacent to ocean (polygon 2)
        int[][] adjacencies = {
            {0, 2},     // Polygon 0 neighbors: self, ocean (2)
            {1},        // Polygon 1 neighbors: just self (inland)
            {2, 0}      // Polygon 2 neighbors: self, land (0)
        };

        double[] rainfall = {0.5, 0.5, 0.5};
        ErosionCalculator.ErosionResult erosion = new ErosionCalculator.ErosionResult(
            heights, null, List.of(), rainfall, null, null, null
        );

        BiomeType[] biomes = BiomeClassifier.classify(heights, climates, erosion, adjacencies);

        assertEquals(BiomeType.COASTAL, biomes[0], "Polygon adjacent to ocean should be coastal");
        assertEquals(BiomeType.OCEAN, biomes[2], "Ocean polygon should be ocean");
    }

    @Test
    void testGetDistribution() {
        BiomeType[] biomes = {
            BiomeType.OCEAN,
            BiomeType.OCEAN,
            BiomeType.TEMPERATE_FOREST,
            BiomeType.DESERT
        };

        Map<BiomeType, Integer> distribution = BiomeClassifier.getDistribution(biomes);

        assertEquals(2, distribution.get(BiomeType.OCEAN));
        assertEquals(1, distribution.get(BiomeType.TEMPERATE_FOREST));
        assertEquals(1, distribution.get(BiomeType.DESERT));
        assertEquals(0, distribution.get(BiomeType.TUNDRA));
    }

    @Test
    void testGetLandDistribution() {
        BiomeType[] biomes = {
            BiomeType.OCEAN,
            BiomeType.OCEAN,
            BiomeType.TEMPERATE_FOREST,
            BiomeType.TEMPERATE_FOREST,
            BiomeType.DESERT,
            BiomeType.DESERT
        };

        Map<BiomeType, Double> percentages = BiomeClassifier.getLandDistribution(biomes);

        // 4 land polygons total: 2 forest, 2 desert
        assertEquals(0.5, percentages.get(BiomeType.TEMPERATE_FOREST), 0.01);
        assertEquals(0.5, percentages.get(BiomeType.DESERT), 0.01);
        assertEquals(0.0, percentages.get(BiomeType.OCEAN), 0.01);  // Water not included in land %
    }
}
