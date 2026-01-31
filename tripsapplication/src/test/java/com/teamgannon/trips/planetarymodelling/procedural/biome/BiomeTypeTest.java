package com.teamgannon.trips.planetarymodelling.procedural.biome;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for BiomeType enum.
 */
class BiomeTypeTest {

    // ===========================================
    // Basic Property Tests
    // ===========================================

    @ParameterizedTest
    @EnumSource(BiomeType.class)
    void testAllBiomesHaveDisplayName(BiomeType biome) {
        assertNotNull(biome.getDisplayName());
        assertFalse(biome.getDisplayName().isEmpty());
    }

    @ParameterizedTest
    @EnumSource(BiomeType.class)
    void testAllBiomesHaveDefaultColor(BiomeType biome) {
        assertNotNull(biome.getDefaultColor());
    }

    @Test
    void testWaterBiomesAreWater() {
        assertTrue(BiomeType.DEEP_OCEAN.isWater());
        assertTrue(BiomeType.OCEAN.isWater());
        assertTrue(BiomeType.FRESHWATER.isWater());

        assertFalse(BiomeType.DEEP_OCEAN.isLand());
        assertFalse(BiomeType.OCEAN.isLand());
        assertFalse(BiomeType.FRESHWATER.isLand());
    }

    @Test
    void testLandBiomesAreLand() {
        BiomeType[] landBiomes = {
            BiomeType.COASTAL,
            BiomeType.ICE_CAP,
            BiomeType.TUNDRA,
            BiomeType.BOREAL_FOREST,
            BiomeType.TEMPERATE_GRASSLAND,
            BiomeType.TEMPERATE_FOREST,
            BiomeType.TEMPERATE_RAINFOREST,
            BiomeType.DESERT,
            BiomeType.SAVANNA,
            BiomeType.TROPICAL_RAINFOREST,
            BiomeType.ALPINE,
            BiomeType.MOUNTAIN,
            BiomeType.WETLAND
        };

        for (BiomeType biome : landBiomes) {
            assertTrue(biome.isLand(), biome.name() + " should be land");
            assertFalse(biome.isWater(), biome.name() + " should not be water");
        }
    }

    @Test
    void testVegetationSupport() {
        // Biomes that should NOT support vegetation
        assertFalse(BiomeType.DEEP_OCEAN.supportsVegetation());
        assertFalse(BiomeType.OCEAN.supportsVegetation());
        assertFalse(BiomeType.FRESHWATER.supportsVegetation());
        assertFalse(BiomeType.ICE_CAP.supportsVegetation());
        assertFalse(BiomeType.DESERT.supportsVegetation());
        assertFalse(BiomeType.MOUNTAIN.supportsVegetation());

        // Biomes that SHOULD support vegetation
        assertTrue(BiomeType.TUNDRA.supportsVegetation());
        assertTrue(BiomeType.BOREAL_FOREST.supportsVegetation());
        assertTrue(BiomeType.TEMPERATE_FOREST.supportsVegetation());
        assertTrue(BiomeType.TROPICAL_RAINFOREST.supportsVegetation());
        assertTrue(BiomeType.SAVANNA.supportsVegetation());
    }

    @Test
    void testHabitabilityScores() {
        // Temperate forest should be most habitable
        assertEquals(1.0, BiomeType.TEMPERATE_FOREST.getHabitabilityScore());

        // Water biomes should have zero habitability
        assertEquals(0.0, BiomeType.OCEAN.getHabitabilityScore());
        assertEquals(0.0, BiomeType.DEEP_OCEAN.getHabitabilityScore());

        // Extreme environments should have low habitability
        assertTrue(BiomeType.ICE_CAP.getHabitabilityScore() < 0.2);
        assertTrue(BiomeType.DESERT.getHabitabilityScore() < 0.3);
    }

    @Test
    void testAgriculturalPotential() {
        // Grasslands should be best for agriculture
        assertEquals(1.0, BiomeType.TEMPERATE_GRASSLAND.getAgriculturalPotential());

        // Water and extreme environments should have zero agricultural potential
        assertEquals(0.0, BiomeType.OCEAN.getAgriculturalPotential());
        assertEquals(0.0, BiomeType.ICE_CAP.getAgriculturalPotential());
        assertEquals(0.0, BiomeType.DESERT.getAgriculturalPotential());
    }

    @ParameterizedTest
    @EnumSource(BiomeType.class)
    void testHabitabilityScoreInRange(BiomeType biome) {
        double score = biome.getHabitabilityScore();
        assertTrue(score >= 0.0 && score <= 1.0,
            biome.name() + " habitability score should be 0-1");
    }

    @ParameterizedTest
    @EnumSource(BiomeType.class)
    void testAgriculturalPotentialInRange(BiomeType biome) {
        double potential = biome.getAgriculturalPotential();
        assertTrue(potential >= 0.0 && potential <= 1.0,
            biome.name() + " agricultural potential should be 0-1");
    }

    @Test
    void testToStringReturnsDisplayName() {
        for (BiomeType biome : BiomeType.values()) {
            assertEquals(biome.getDisplayName(), biome.toString());
        }
    }

    // ===========================================
    // Comprehensive Habitability Score Tests
    // ===========================================

    @Nested
    @DisplayName("Habitability scores for all biomes")
    class HabitabilityScoreTests {

        @ParameterizedTest
        @CsvSource({
            "DEEP_OCEAN, 0.0",
            "OCEAN, 0.0",
            "FRESHWATER, 0.0",
            "COASTAL, 0.9",
            "ICE_CAP, 0.05",
            "TUNDRA, 0.2",
            "BOREAL_FOREST, 0.6",
            "TEMPERATE_GRASSLAND, 0.95",
            "TEMPERATE_FOREST, 1.0",
            "TEMPERATE_RAINFOREST, 0.85",
            "DESERT, 0.15",
            "SAVANNA, 0.7",
            "TROPICAL_RAINFOREST, 0.5",
            "ALPINE, 0.3",
            "MOUNTAIN, 0.1",
            "WETLAND, 0.4"
        })
        @DisplayName("Habitability score matches expected value")
        void habitabilityScoreMatchesExpected(String biomeName, double expectedScore) {
            BiomeType biome = BiomeType.valueOf(biomeName);
            assertEquals(expectedScore, biome.getHabitabilityScore(), 0.001,
                biomeName + " should have habitability score " + expectedScore);
        }

        @Test
        @DisplayName("Temperate forest has maximum habitability")
        void temperateForestHasMaxHabitability() {
            BiomeType maxBiome = null;
            double maxScore = -1;

            for (BiomeType biome : BiomeType.values()) {
                if (biome.getHabitabilityScore() > maxScore) {
                    maxScore = biome.getHabitabilityScore();
                    maxBiome = biome;
                }
            }

            assertEquals(BiomeType.TEMPERATE_FOREST, maxBiome,
                "Temperate forest should have highest habitability");
            assertEquals(1.0, maxScore);
        }

        @Test
        @DisplayName("Water biomes have zero habitability")
        void waterBiomesHaveZeroHabitability() {
            BiomeType[] waterBiomes = {
                BiomeType.DEEP_OCEAN,
                BiomeType.OCEAN,
                BiomeType.FRESHWATER
            };

            for (BiomeType biome : waterBiomes) {
                assertEquals(0.0, biome.getHabitabilityScore(),
                    biome.name() + " should have zero habitability");
            }
        }

        @Test
        @DisplayName("Hostile environments have low habitability")
        void hostileEnvironmentsHaveLowHabitability() {
            assertTrue(BiomeType.ICE_CAP.getHabitabilityScore() <= 0.2);
            assertTrue(BiomeType.DESERT.getHabitabilityScore() <= 0.3);
            assertTrue(BiomeType.MOUNTAIN.getHabitabilityScore() <= 0.2);
        }

        @Test
        @DisplayName("Temperate biomes have high habitability")
        void temperateBiomesHaveHighHabitability() {
            assertTrue(BiomeType.TEMPERATE_GRASSLAND.getHabitabilityScore() >= 0.8);
            assertTrue(BiomeType.TEMPERATE_FOREST.getHabitabilityScore() >= 0.8);
            assertTrue(BiomeType.TEMPERATE_RAINFOREST.getHabitabilityScore() >= 0.7);
        }
    }

    // ===========================================
    // Comprehensive Agricultural Potential Tests
    // ===========================================

    @Nested
    @DisplayName("Agricultural potential for all biomes")
    class AgriculturalPotentialTests {

        @ParameterizedTest
        @CsvSource({
            "DEEP_OCEAN, 0.0",
            "OCEAN, 0.0",
            "FRESHWATER, 0.0",
            "COASTAL, 0.4",
            "ICE_CAP, 0.0",
            "TUNDRA, 0.05",
            "BOREAL_FOREST, 0.25",
            "TEMPERATE_GRASSLAND, 1.0",
            "TEMPERATE_FOREST, 0.9",
            "TEMPERATE_RAINFOREST, 0.6",
            "DESERT, 0.0",
            "SAVANNA, 0.7",
            "TROPICAL_RAINFOREST, 0.5",
            "ALPINE, 0.1",
            "MOUNTAIN, 0.0",
            "WETLAND, 0.3"
        })
        @DisplayName("Agricultural potential matches expected value")
        void agriculturalPotentialMatchesExpected(String biomeName, double expectedPotential) {
            BiomeType biome = BiomeType.valueOf(biomeName);
            assertEquals(expectedPotential, biome.getAgriculturalPotential(), 0.001,
                biomeName + " should have agricultural potential " + expectedPotential);
        }

        @Test
        @DisplayName("Temperate grassland has maximum agricultural potential")
        void temperateGrasslandHasMaxAgricultural() {
            BiomeType maxBiome = null;
            double maxPotential = -1;

            for (BiomeType biome : BiomeType.values()) {
                if (biome.getAgriculturalPotential() > maxPotential) {
                    maxPotential = biome.getAgriculturalPotential();
                    maxBiome = biome;
                }
            }

            assertEquals(BiomeType.TEMPERATE_GRASSLAND, maxBiome,
                "Temperate grassland should have highest agricultural potential");
            assertEquals(1.0, maxPotential);
        }

        @Test
        @DisplayName("Inhospitable biomes have zero agricultural potential")
        void inhospitableBiomesHaveZeroAgricultural() {
            BiomeType[] inhospitable = {
                BiomeType.DEEP_OCEAN,
                BiomeType.OCEAN,
                BiomeType.FRESHWATER,
                BiomeType.ICE_CAP,
                BiomeType.DESERT,
                BiomeType.MOUNTAIN
            };

            for (BiomeType biome : inhospitable) {
                assertEquals(0.0, biome.getAgriculturalPotential(),
                    biome.name() + " should have zero agricultural potential");
            }
        }

        @Test
        @DisplayName("Farmable biomes have positive agricultural potential")
        void farmableBiomesHavePositiveAgricultural() {
            BiomeType[] farmable = {
                BiomeType.TEMPERATE_GRASSLAND,
                BiomeType.TEMPERATE_FOREST,
                BiomeType.SAVANNA
            };

            for (BiomeType biome : farmable) {
                assertTrue(biome.getAgriculturalPotential() >= 0.5,
                    biome.name() + " should have significant agricultural potential");
            }
        }
    }

    // ===========================================
    // Biome Category Tests
    // ===========================================

    @Nested
    @DisplayName("Biome categories")
    class BiomeCategoryTests {

        @Test
        @DisplayName("Exactly 16 biomes exist")
        void exactly16BiomesExist() {
            assertEquals(16, BiomeType.values().length);
        }

        @Test
        @DisplayName("Count of water biomes")
        void countWaterBiomes() {
            int waterCount = 0;
            for (BiomeType biome : BiomeType.values()) {
                if (biome.isWater()) waterCount++;
            }
            assertEquals(3, waterCount, "Should have 3 water biomes");
        }

        @Test
        @DisplayName("Count of land biomes")
        void countLandBiomes() {
            int landCount = 0;
            for (BiomeType biome : BiomeType.values()) {
                if (biome.isLand()) landCount++;
            }
            assertEquals(13, landCount, "Should have 13 land biomes");
        }

        @Test
        @DisplayName("Water and land are mutually exclusive")
        void waterAndLandMutuallyExclusive() {
            for (BiomeType biome : BiomeType.values()) {
                assertNotEquals(biome.isWater(), biome.isLand(),
                    biome.name() + " should be either water or land, not both or neither");
            }
        }

        @Test
        @DisplayName("Count of vegetation-supporting biomes")
        void countVegetationBiomes() {
            int vegCount = 0;
            for (BiomeType biome : BiomeType.values()) {
                if (biome.supportsVegetation()) vegCount++;
            }
            // Land biomes minus hostile ones (ice_cap, desert, mountain, coastal, alpine)
            assertTrue(vegCount >= 7 && vegCount <= 10,
                "Should have 7-10 vegetation-supporting biomes");
        }
    }

    // ===========================================
    // Coastal Biome Tests
    // ===========================================

    @Nested
    @DisplayName("Coastal biome special properties")
    class CoastalBiomeTests {

        @Test
        @DisplayName("Coastal is land, not water")
        void coastalIsLand() {
            assertTrue(BiomeType.COASTAL.isLand());
            assertFalse(BiomeType.COASTAL.isWater());
        }

        @Test
        @DisplayName("Coastal has high habitability")
        void coastalHasHighHabitability() {
            double score = BiomeType.COASTAL.getHabitabilityScore();
            assertTrue(score >= 0.7 && score <= 1.0,
                "Coastal should have high habitability (0.9)");
        }

        @Test
        @DisplayName("Coastal has some agricultural potential")
        void coastalHasSomeAgriculturalPotential() {
            assertTrue(BiomeType.COASTAL.getAgriculturalPotential() > 0.0,
                "Coastal should have some agricultural potential");
        }
    }

    // ===========================================
    // Wetland Biome Tests
    // ===========================================

    @Nested
    @DisplayName("Wetland biome special properties")
    class WetlandBiomeTests {

        @Test
        @DisplayName("Wetland is land despite water association")
        void wetlandIsLand() {
            assertTrue(BiomeType.WETLAND.isLand());
            assertFalse(BiomeType.WETLAND.isWater());
        }

        @Test
        @DisplayName("Wetland supports vegetation")
        void wetlandSupportsVegetation() {
            assertTrue(BiomeType.WETLAND.supportsVegetation());
        }

        @Test
        @DisplayName("Wetland has moderate habitability")
        void wetlandHasModerateHabitability() {
            double score = BiomeType.WETLAND.getHabitabilityScore();
            assertTrue(score >= 0.3 && score <= 0.7,
                "Wetland should have moderate habitability");
        }
    }

    // ===========================================
    // Color Tests
    // ===========================================

    @Nested
    @DisplayName("Biome colors")
    class ColorTests {

        @ParameterizedTest
        @EnumSource(BiomeType.class)
        @DisplayName("All biomes have non-null colors")
        void allBiomesHaveColors(BiomeType biome) {
            assertNotNull(biome.getDefaultColor());
        }

        @Test
        @DisplayName("Water biomes have blue-ish colors")
        void waterBiomesHaveBluishColors() {
            // Just verify colors exist - actual color values would be visual verification
            assertNotNull(BiomeType.DEEP_OCEAN.getDefaultColor());
            assertNotNull(BiomeType.OCEAN.getDefaultColor());
            assertNotNull(BiomeType.FRESHWATER.getDefaultColor());
        }
    }

    // ===========================================
    // Logical Consistency Tests
    // ===========================================

    @Nested
    @DisplayName("Logical consistency")
    class LogicalConsistencyTests {

        @Test
        @DisplayName("High habitability implies moderate agricultural potential")
        void highHabitabilityImpliesModerateAgricultural() {
            for (BiomeType biome : BiomeType.values()) {
                if (biome.getHabitabilityScore() >= 0.8) {
                    assertTrue(biome.getAgriculturalPotential() >= 0.3,
                        "High habitability biome " + biome.name() +
                        " should have at least moderate agricultural potential");
                }
            }
        }

        @Test
        @DisplayName("Zero habitability implies zero or low agricultural potential")
        void zeroHabitabilityImpliesLowAgricultural() {
            for (BiomeType biome : BiomeType.values()) {
                if (biome.getHabitabilityScore() == 0.0) {
                    assertTrue(biome.getAgriculturalPotential() <= 0.1,
                        "Zero habitability biome " + biome.name() +
                        " should have zero or minimal agricultural potential");
                }
            }
        }

        @Test
        @DisplayName("Vegetation support correlates with agricultural potential")
        void vegetationCorrelatesWithAgricultural() {
            for (BiomeType biome : BiomeType.values()) {
                if (biome.getAgriculturalPotential() >= 0.5) {
                    assertTrue(biome.supportsVegetation(),
                        "High agricultural potential biome " + biome.name() +
                        " should support vegetation");
                }
            }
        }
    }
}
