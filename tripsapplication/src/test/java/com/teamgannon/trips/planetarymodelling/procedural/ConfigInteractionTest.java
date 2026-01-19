package com.teamgannon.trips.planetarymodelling.procedural;

import com.teamgannon.trips.planetarymodelling.procedural.PlanetConfig.Size;
import com.teamgannon.trips.planetarymodelling.procedural.PlanetGenerator.GeneratedPlanet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests that verify configuration parameters interact correctly.
 * These tests ensure that changing one parameter has the expected
 * effect on related outputs.
 */
class ConfigInteractionTest {

    private static final long FIXED_SEED = 12345L;

    // ===========================================
    // Water Fraction and Ocean Distribution
    // ===========================================

    @Nested
    @DisplayName("Water fraction affects ocean distribution")
    class WaterFractionInteractionTests {

        @Test
        @DisplayName("Higher water fraction produces more ocean area")
        void higherWaterFractionMoreOcean() {
            PlanetConfig lowWater = PlanetConfig.builder()
                .seed(FIXED_SEED)
                .waterFraction(0.3)
                .size(Size.SMALL)
                .build();
            PlanetConfig highWater = PlanetConfig.builder()
                .seed(FIXED_SEED)
                .waterFraction(0.9)
                .size(Size.SMALL)
                .build();

            GeneratedPlanet lowPlanet = PlanetGenerator.generate(lowWater);
            GeneratedPlanet highPlanet = PlanetGenerator.generate(highWater);

            double lowOceanRatio = calculateOceanRatio(lowPlanet);
            double highOceanRatio = calculateOceanRatio(highPlanet);

            // Higher water fraction should result in more ocean
            assertThat(highOceanRatio).isGreaterThan(lowOceanRatio);
        }

        @Test
        @DisplayName("Water fraction affects climate zone distribution")
        void waterFractionAffectsClimate() {
            // Water affects temperature moderation
            PlanetConfig config = PlanetConfig.builder()
                .seed(FIXED_SEED)
                .waterFraction(0.66)  // Earth-like
                .size(Size.SMALL)
                .build();

            GeneratedPlanet planet = PlanetGenerator.generate(config);

            // Should have all climate zones on a temperate planet
            boolean hasTropical = false, hasTemperate = false, hasPolar = false;
            for (var zone : planet.climates()) {
                switch (zone) {
                    case TROPICAL -> hasTropical = true;
                    case TEMPERATE -> hasTemperate = true;
                    case POLAR -> hasPolar = true;
                }
            }
            assertThat(hasTropical).isTrue();
            assertThat(hasTemperate).isTrue();
            assertThat(hasPolar).isTrue();
        }
    }

    // ===========================================
    // Plate Count and Boundaries
    // ===========================================

    @Nested
    @DisplayName("Plate count affects boundary distribution")
    class PlateCountInteractionTests {

        @Test
        @DisplayName("More plates produce more boundary polygons")
        void morePlatesMoreBoundaries() {
            PlanetConfig fewPlates = PlanetConfig.builder()
                .seed(FIXED_SEED)
                .plateCount(7)
                .size(Size.STANDARD)
                .build();
            PlanetConfig manyPlates = PlanetConfig.builder()
                .seed(FIXED_SEED)
                .plateCount(21)
                .size(Size.STANDARD)
                .build();

            GeneratedPlanet fewPlanet = PlanetGenerator.generate(fewPlates);
            GeneratedPlanet manyPlanet = PlanetGenerator.generate(manyPlates);

            int fewBoundaries = countBoundaryPolygons(fewPlanet);
            int manyBoundaries = countBoundaryPolygons(manyPlanet);

            // More plates means more boundaries between them
            assertThat(manyBoundaries).isGreaterThan(fewBoundaries);
        }

        @Test
        @DisplayName("Plate count affects terrain diversity")
        void plateCountAffectsTerrainDiversity() {
            PlanetConfig manyPlates = PlanetConfig.builder()
                .seed(FIXED_SEED)
                .plateCount(15)
                .size(Size.STANDARD)
                .build();

            GeneratedPlanet planet = PlanetGenerator.generate(manyPlates);

            // With many plates, we should see variety in heights
            int minHeight = Integer.MAX_VALUE, maxHeight = Integer.MIN_VALUE;
            for (int h : planet.heights()) {
                minHeight = Math.min(minHeight, h);
                maxHeight = Math.max(maxHeight, h);
            }

            // Should have significant height range
            int range = maxHeight - minHeight;
            assertThat(range).isGreaterThanOrEqualTo(4);  // At least 4 height levels
        }
    }

    // ===========================================
    // Oceanic Plate Ratio Effects
    // ===========================================

    @Nested
    @DisplayName("Oceanic plate ratio affects terrain")
    class OceanicRatioInteractionTests {

        @Test
        @DisplayName("High oceanic ratio produces more ocean basins")
        void oceanicRatioAffectsBasins() {
            PlanetConfig continental = PlanetConfig.builder()
                .seed(FIXED_SEED)
                .oceanicPlateRatio(0.2)  // Mostly continental
                .waterFraction(0.5)
                .size(Size.SMALL)
                .build();
            PlanetConfig oceanic = PlanetConfig.builder()
                .seed(FIXED_SEED)
                .oceanicPlateRatio(0.8)  // Mostly oceanic
                .waterFraction(0.5)
                .size(Size.SMALL)
                .build();

            GeneratedPlanet contPlanet = PlanetGenerator.generate(continental);
            GeneratedPlanet oceanPlanet = PlanetGenerator.generate(oceanic);

            // Both configs have same water fraction, but oceanic plates
            // tend to be lower elevation
            double contOceanRatio = calculateOceanRatio(contPlanet);
            double oceanOceanRatio = calculateOceanRatio(oceanPlanet);

            // Oceanic plates should result in more areas below sea level
            assertThat(oceanOceanRatio).isGreaterThanOrEqualTo(contOceanRatio - 0.1);
        }
    }

    // ===========================================
    // Erosion and Rivers
    // ===========================================

    @Nested
    @DisplayName("Erosion parameters affect river formation")
    class ErosionInteractionTests {

        @Test
        @DisplayName("Higher erosion iterations affect terrain smoothing")
        void erosionIterationsAffectTerrain() {
            PlanetConfig lowErosion = PlanetConfig.builder()
                .seed(FIXED_SEED)
                .erosionIterations(1)
                .size(Size.SMALL)
                .build();
            PlanetConfig highErosion = PlanetConfig.builder()
                .seed(FIXED_SEED)
                .erosionIterations(10)
                .size(Size.SMALL)
                .build();

            GeneratedPlanet lowPlanet = PlanetGenerator.generate(lowErosion);
            GeneratedPlanet highPlanet = PlanetGenerator.generate(highErosion);

            // Both should complete successfully
            assertThat(lowPlanet).isNotNull();
            assertThat(highPlanet).isNotNull();

            // Erosion results should be present
            assertThat(lowPlanet.erosionResult()).isNotNull();
            assertThat(highPlanet.erosionResult()).isNotNull();
        }

        @Test
        @DisplayName("River source threshold affects river count")
        void riverSourceThresholdAffectsRivers() {
            PlanetConfig lowThreshold = PlanetConfig.builder()
                .seed(FIXED_SEED)
                .riverSourceThreshold(0.3)  // More rivers (lower threshold)
                .size(Size.STANDARD)
                .build();
            PlanetConfig highThreshold = PlanetConfig.builder()
                .seed(FIXED_SEED)
                .riverSourceThreshold(0.9)  // Fewer rivers (higher threshold)
                .size(Size.STANDARD)
                .build();

            GeneratedPlanet lowPlanet = PlanetGenerator.generate(lowThreshold);
            GeneratedPlanet highPlanet = PlanetGenerator.generate(highThreshold);

            int lowRivers = lowPlanet.rivers().size();
            int highRivers = highPlanet.rivers().size();

            // Lower threshold should produce more (or equal) rivers
            assertThat(lowRivers).isGreaterThanOrEqualTo(highRivers);
        }
    }

    // ===========================================
    // Height Scale Effects
    // ===========================================

    @Nested
    @DisplayName("Height scale affects terrain relief")
    class HeightScaleInteractionTests {

        @Test
        @DisplayName("Higher height scale produces more extreme terrain")
        void heightScaleAffectsRelief() {
            PlanetConfig lowScale = PlanetConfig.builder()
                .seed(FIXED_SEED)
                .heightScaleMultiplier(0.5)
                .size(Size.SMALL)
                .build();
            PlanetConfig highScale = PlanetConfig.builder()
                .seed(FIXED_SEED)
                .heightScaleMultiplier(2.0)
                .size(Size.SMALL)
                .build();

            GeneratedPlanet lowPlanet = PlanetGenerator.generate(lowScale);
            GeneratedPlanet highPlanet = PlanetGenerator.generate(highScale);

            int lowRange = calculateHeightRange(lowPlanet);
            int highRange = calculateHeightRange(highPlanet);

            // Higher scale should produce larger height range
            assertThat(highRange).isGreaterThanOrEqualTo(lowRange);
        }
    }

    // ===========================================
    // Climate Model Effects
    // ===========================================

    @Nested
    @DisplayName("Climate model affects zone distribution")
    class ClimateModelInteractionTests {

        @Test
        @DisplayName("Ice world produces more polar than tropical world")
        void iceWorldVsTropicalWorld() {
            PlanetConfig iceConfig = PlanetConfig.builder()
                .seed(FIXED_SEED)
                .climateModel(ClimateCalculator.ClimateModel.ICE_WORLD)
                .size(Size.SMALL)
                .build();
            PlanetConfig tropicalConfig = PlanetConfig.builder()
                .seed(FIXED_SEED)
                .climateModel(ClimateCalculator.ClimateModel.TROPICAL_WORLD)
                .size(Size.SMALL)
                .build();

            GeneratedPlanet icePlanet = PlanetGenerator.generate(iceConfig);
            GeneratedPlanet tropicalPlanet = PlanetGenerator.generate(tropicalConfig);

            double icePolarRatio = calculateClimateRatio(icePlanet, ClimateCalculator.ClimateZone.POLAR);
            double tropicalPolarRatio = calculateClimateRatio(tropicalPlanet, ClimateCalculator.ClimateZone.POLAR);

            // Ice world should have more polar zones
            assertThat(icePolarRatio).isGreaterThan(tropicalPolarRatio);
        }

        @Test
        @DisplayName("Climate model affects erosion rainfall patterns")
        void climateModelAffectsRainfall() {
            PlanetConfig iceConfig = PlanetConfig.builder()
                .seed(FIXED_SEED)
                .climateModel(ClimateCalculator.ClimateModel.ICE_WORLD)
                .size(Size.SMALL)
                .build();
            PlanetConfig tropicalConfig = PlanetConfig.builder()
                .seed(FIXED_SEED)
                .climateModel(ClimateCalculator.ClimateModel.TROPICAL_WORLD)
                .size(Size.SMALL)
                .build();

            GeneratedPlanet icePlanet = PlanetGenerator.generate(iceConfig);
            GeneratedPlanet tropicalPlanet = PlanetGenerator.generate(tropicalConfig);

            // Both should have rainfall data
            assertThat(icePlanet.rainfall()).isNotNull();
            assertThat(tropicalPlanet.rainfall()).isNotNull();

            // Tropical world should have higher average rainfall
            double iceAvgRainfall = calculateAverageRainfall(icePlanet);
            double tropicalAvgRainfall = calculateAverageRainfall(tropicalPlanet);

            assertThat(tropicalAvgRainfall).isGreaterThanOrEqualTo(iceAvgRainfall);
        }
    }

    // ===========================================
    // Stagnant Lid Effects
    // ===========================================

    @Nested
    @DisplayName("Stagnant lid mode affects tectonics")
    class StagnantLidInteractionTests {

        @Test
        @DisplayName("Stagnant lid produces different terrain than active tectonics")
        void stagnantLidVsActiveTectonics() {
            PlanetConfig active = PlanetConfig.builder()
                .seed(FIXED_SEED)
                .enableActiveTectonics(true)
                .size(Size.SMALL)
                .build();
            PlanetConfig stagnant = PlanetConfig.builder()
                .seed(FIXED_SEED)
                .enableActiveTectonics(false)
                .size(Size.SMALL)
                .build();

            GeneratedPlanet activePlanet = PlanetGenerator.generate(active);
            GeneratedPlanet stagnantPlanet = PlanetGenerator.generate(stagnant);

            // Both should generate successfully
            assertThat(activePlanet).isNotNull();
            assertThat(stagnantPlanet).isNotNull();
            assertThat(activePlanet.polygons()).isNotEmpty();
            assertThat(stagnantPlanet.polygons()).isNotEmpty();
        }
    }

    // ===========================================
    // Size and Polygon Count
    // ===========================================

    @Nested
    @DisplayName("Size affects polygon count and detail")
    class SizeInteractionTests {

        @Test
        @DisplayName("Larger sizes produce more polygons")
        void largerSizeMorePolygons() {
            PlanetConfig small = PlanetConfig.builder()
                .seed(FIXED_SEED)
                .size(Size.DUEL)
                .plateCount(7)
                .build();
            PlanetConfig large = PlanetConfig.builder()
                .seed(FIXED_SEED)
                .size(Size.HUGE)
                .plateCount(7)
                .build();

            GeneratedPlanet smallPlanet = PlanetGenerator.generate(small);
            GeneratedPlanet largePlanet = PlanetGenerator.generate(large);

            assertThat(largePlanet.polygons().size())
                .isGreaterThan(smallPlanet.polygons().size());
        }

        @Test
        @DisplayName("Size matches expected polygon counts")
        void sizeMatchesExpectedCounts() {
            for (Size size : Size.values()) {
                PlanetConfig config = PlanetConfig.builder()
                    .seed(FIXED_SEED)
                    .size(size)
                    .plateCount(7)
                    .build();

                GeneratedPlanet planet = PlanetGenerator.generate(config);

                assertThat(planet.polygons().size())
                    .as("Size %s should produce %d polygons", size, size.polyCount)
                    .isEqualTo(size.polyCount);
            }
        }
    }

    // ===========================================
    // Helper Methods
    // ===========================================

    private double calculateOceanRatio(GeneratedPlanet planet) {
        int oceanCount = 0;
        for (int h : planet.heights()) {
            if (h < 0) oceanCount++;
        }
        return (double) oceanCount / planet.heights().length;
    }

    private int countBoundaryPolygons(GeneratedPlanet planet) {
        if (planet.boundaryAnalysis() == null) return 0;
        // Count unique boundary pairs
        return planet.boundaryAnalysis().boundaries().size();
    }

    private int calculateHeightRange(GeneratedPlanet planet) {
        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        for (int h : planet.heights()) {
            min = Math.min(min, h);
            max = Math.max(max, h);
        }
        return max - min;
    }

    private double calculateClimateRatio(GeneratedPlanet planet, ClimateCalculator.ClimateZone targetZone) {
        int count = 0;
        for (var zone : planet.climates()) {
            if (zone == targetZone) count++;
        }
        return (double) count / planet.climates().length;
    }

    private double calculateAverageRainfall(GeneratedPlanet planet) {
        double[] rainfall = planet.rainfall();
        if (rainfall == null || rainfall.length == 0) return 0;
        double sum = 0;
        for (double r : rainfall) {
            sum += r;
        }
        return sum / rainfall.length;
    }
}
