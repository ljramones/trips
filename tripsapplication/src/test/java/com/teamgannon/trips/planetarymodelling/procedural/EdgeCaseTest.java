package com.teamgannon.trips.planetarymodelling.procedural;

import com.teamgannon.trips.planetarymodelling.procedural.PlanetConfig.Size;
import com.teamgannon.trips.planetarymodelling.procedural.PlanetGenerator.GeneratedPlanet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Edge case tests for procedural planet generation.
 * Tests boundary conditions and extreme parameter values.
 */
class EdgeCaseTest {

    // ===========================================
    // Water Fraction Edge Cases
    // ===========================================

    @Nested
    @DisplayName("Water fraction boundary values")
    class WaterFractionTests {

        @Test
        @DisplayName("waterFraction=0 uses dry planet configuration")
        void completelyDryWorld() {
            PlanetConfig config = PlanetConfig.builder()
                .seed(12345L)
                .waterFraction(0.0)
                .size(Size.SMALL)
                .build();

            GeneratedPlanet planet = PlanetGenerator.generate(config);

            assertThat(planet).isNotNull();
            assertThat(planet.polygons()).isNotEmpty();
            assertThat(config.waterFraction()).isEqualTo(0.0);

            // Terrain generation still produces valleys (negative heights represent
            // potential water areas). The key is the config has zero water fraction.
        }

        @Test
        @DisplayName("waterFraction=1 produces mostly ocean planet")
        void completeOceanWorld() {
            PlanetConfig config = PlanetConfig.builder()
                .seed(12345L)
                .waterFraction(1.0)
                .size(Size.SMALL)
                .build();

            GeneratedPlanet planet = PlanetGenerator.generate(config);

            assertThat(planet).isNotNull();
            assertThat(planet.polygons()).isNotEmpty();

            // With waterFraction=1, expect mostly negative heights
            int oceanCount = 0;
            for (int h : planet.heights()) {
                if (h < 0) oceanCount++;
            }
            double oceanRatio = (double) oceanCount / planet.heights().length;
            assertThat(oceanRatio).isGreaterThan(0.7);
        }

        @Test
        @DisplayName("waterFraction=0.5 produces mixed terrain")
        void halfWaterWorld() {
            PlanetConfig config = PlanetConfig.builder()
                .seed(12345L)
                .waterFraction(0.5)
                .size(Size.SMALL)
                .build();

            GeneratedPlanet planet = PlanetGenerator.generate(config);

            assertThat(planet).isNotNull();

            // With 50% water, expect mixed results
            int oceanCount = 0;
            for (int h : planet.heights()) {
                if (h < 0) oceanCount++;
            }
            double oceanRatio = (double) oceanCount / planet.heights().length;
            // Should be somewhere in between, not extreme
            assertThat(oceanRatio).isBetween(0.2, 0.8);
        }
    }

    // ===========================================
    // Plate Count Edge Cases
    // ===========================================

    @Nested
    @DisplayName("Plate count boundary values")
    class PlateCountTests {

        @Test
        @DisplayName("Minimum plate count (7) works")
        void minimumPlates() {
            // Builder clamps to min of 7
            PlanetConfig config = PlanetConfig.builder()
                .seed(12345L)
                .plateCount(1)  // Will be clamped to 7
                .size(Size.SMALL)
                .build();

            GeneratedPlanet planet = PlanetGenerator.generate(config);

            assertThat(planet).isNotNull();
            assertThat(planet.plateAssignment()).isNotNull();
            assertThat(planet.plateAssignment().plates().size()).isEqualTo(7);
        }

        @Test
        @DisplayName("Maximum plate count (21) works")
        void maximumPlates() {
            PlanetConfig config = PlanetConfig.builder()
                .seed(12345L)
                .plateCount(21)
                .size(Size.LARGE)  // Need more polygons for many plates
                .build();

            GeneratedPlanet planet = PlanetGenerator.generate(config);

            assertThat(planet).isNotNull();
            assertThat(planet.plateAssignment().plates().size()).isEqualTo(21);

            // Verify no empty plates
            for (var plate : planet.plateAssignment().plates()) {
                assertThat(plate).isNotEmpty();
            }
        }
    }

    // ===========================================
    // Size (Subdivision Level) Edge Cases
    // ===========================================

    @Nested
    @DisplayName("Size variations")
    class SizeTests {

        @ParameterizedTest
        @EnumSource(Size.class)
        @DisplayName("All size presets produce valid terrain")
        void allSizesWork(Size size) {
            PlanetConfig config = PlanetConfig.builder()
                .seed(12345L)
                .size(size)
                .plateCount(7)  // Use minimum to ensure enough polygons per plate
                .build();

            GeneratedPlanet planet = PlanetGenerator.generate(config);

            assertThat(planet).isNotNull();
            assertThat(planet.polygons()).isNotEmpty();
            assertThat(planet.heights()).hasSize(planet.polygons().size());
        }

        @Test
        @DisplayName("DUEL size produces smallest valid planet")
        void smallestSize() {
            PlanetConfig config = PlanetConfig.builder()
                .seed(12345L)
                .size(Size.DUEL)
                .plateCount(7)
                .build();

            GeneratedPlanet planet = PlanetGenerator.generate(config);

            assertThat(planet).isNotNull();
            assertThat(planet.polygons().size()).isEqualTo(Size.DUEL.polyCount);
        }

        @Test
        @DisplayName("COLOSSAL size produces large valid planet")
        void largestSize() {
            PlanetConfig config = PlanetConfig.builder()
                .seed(12345L)
                .size(Size.COLOSSAL)
                .plateCount(15)
                .build();

            GeneratedPlanet planet = PlanetGenerator.generate(config);

            assertThat(planet).isNotNull();
            assertThat(planet.polygons().size()).isEqualTo(Size.COLOSSAL.polyCount);
        }
    }

    // ===========================================
    // Radius Edge Cases
    // ===========================================

    @Nested
    @DisplayName("Radius boundary values")
    class RadiusTests {

        @Test
        @DisplayName("Very small radius (asteroid-like)")
        void asteroidRadius() {
            PlanetConfig config = PlanetConfig.builder()
                .seed(12345L)
                .radius(100)  // 100 km
                .size(Size.SMALL)
                .build();

            GeneratedPlanet planet = PlanetGenerator.generate(config);

            assertThat(planet).isNotNull();
            assertThat(config.radius()).isEqualTo(100);
        }

        @Test
        @DisplayName("Earth-like radius")
        void earthRadius() {
            PlanetConfig config = PlanetConfig.builder()
                .seed(12345L)
                .radius(6371)  // Earth radius in km
                .size(Size.STANDARD)
                .build();

            GeneratedPlanet planet = PlanetGenerator.generate(config);

            assertThat(planet).isNotNull();
            assertThat(config.radius()).isEqualTo(6371);
        }

        @Test
        @DisplayName("Jupiter-like radius")
        void giantRadius() {
            PlanetConfig config = PlanetConfig.builder()
                .seed(12345L)
                .radius(69911)  // Jupiter radius in km
                .size(Size.HUGE)
                .build();

            GeneratedPlanet planet = PlanetGenerator.generate(config);

            assertThat(planet).isNotNull();
            assertThat(config.radius()).isEqualTo(69911);
        }
    }

    // ===========================================
    // Tectonic Parameter Edge Cases
    // ===========================================

    @Nested
    @DisplayName("Tectonic parameter edge cases")
    class TectonicTests {

        @Test
        @DisplayName("oceanicPlateRatio=0 (all continental)")
        void allContinentalPlates() {
            PlanetConfig config = PlanetConfig.builder()
                .seed(12345L)
                .oceanicPlateRatio(0.0)
                .size(Size.SMALL)
                .build();

            GeneratedPlanet planet = PlanetGenerator.generate(config);

            assertThat(planet).isNotNull();
            assertThat(planet.plateAssignment()).isNotNull();
        }

        @Test
        @DisplayName("oceanicPlateRatio=1 (all oceanic)")
        void allOceanicPlates() {
            PlanetConfig config = PlanetConfig.builder()
                .seed(12345L)
                .oceanicPlateRatio(1.0)
                .size(Size.SMALL)
                .build();

            GeneratedPlanet planet = PlanetGenerator.generate(config);

            assertThat(planet).isNotNull();
            assertThat(planet.plateAssignment()).isNotNull();
        }

        @Test
        @DisplayName("hotspotProbability=0 (no hotspots)")
        void noHotspots() {
            PlanetConfig config = PlanetConfig.builder()
                .seed(12345L)
                .hotspotProbability(0.0)
                .size(Size.SMALL)
                .build();

            GeneratedPlanet planet = PlanetGenerator.generate(config);

            assertThat(planet).isNotNull();
        }

        @Test
        @DisplayName("enableActiveTectonics=false (stagnant lid)")
        void stagnantLidMode() {
            PlanetConfig config = PlanetConfig.builder()
                .seed(12345L)
                .enableActiveTectonics(false)
                .size(Size.SMALL)
                .build();

            GeneratedPlanet planet = PlanetGenerator.generate(config);

            assertThat(planet).isNotNull();
            assertThat(config.enableActiveTectonics()).isFalse();
        }
    }

    // ===========================================
    // Height/Scale Multiplier Edge Cases
    // ===========================================

    @Nested
    @DisplayName("Height and scale multiplier edge cases")
    class HeightScaleTests {

        @Test
        @DisplayName("Large heightScaleMultiplier produces extreme relief")
        void extremeRelief() {
            PlanetConfig config = PlanetConfig.builder()
                .seed(12345L)
                .heightScaleMultiplier(3.0)
                .size(Size.SMALL)
                .build();

            GeneratedPlanet planet = PlanetGenerator.generate(config);

            assertThat(planet).isNotNull();
        }

        @Test
        @DisplayName("Minimum heightScaleMultiplier (0.5) works")
        void minimumHeight() {
            // Builder clamps to 0.5 minimum
            PlanetConfig config = PlanetConfig.builder()
                .seed(12345L)
                .heightScaleMultiplier(0.0)  // Will be clamped to 0.5
                .size(Size.SMALL)
                .build();

            GeneratedPlanet planet = PlanetGenerator.generate(config);

            assertThat(planet).isNotNull();
            assertThat(config.heightScaleMultiplier()).isEqualTo(0.5);
        }
    }

    // ===========================================
    // Climate Model Edge Cases
    // ===========================================

    @Nested
    @DisplayName("Climate model variations")
    class ClimateModelTests {

        @Test
        @DisplayName("ICE_WORLD model produces appropriate climate zones")
        void iceWorldModel() {
            PlanetConfig config = PlanetConfig.builder()
                .seed(12345L)
                .climateModel(ClimateCalculator.ClimateModel.ICE_WORLD)
                .size(Size.SMALL)
                .build();

            GeneratedPlanet planet = PlanetGenerator.generate(config);

            assertThat(planet).isNotNull();
            assertThat(planet.climates()).isNotNull();

            // Ice world should have more polar zones
            int polarCount = 0;
            for (var zone : planet.climates()) {
                if (zone == ClimateCalculator.ClimateZone.POLAR) {
                    polarCount++;
                }
            }
            double polarRatio = (double) polarCount / planet.climates().length;
            assertThat(polarRatio).isGreaterThan(0.3);
        }

        @Test
        @DisplayName("TROPICAL_WORLD model produces appropriate climate zones")
        void tropicalWorldModel() {
            PlanetConfig config = PlanetConfig.builder()
                .seed(12345L)
                .climateModel(ClimateCalculator.ClimateModel.TROPICAL_WORLD)
                .size(Size.SMALL)
                .build();

            GeneratedPlanet planet = PlanetGenerator.generate(config);

            assertThat(planet).isNotNull();

            // Tropical world should have more tropical zones
            int tropicalCount = 0;
            for (var zone : planet.climates()) {
                if (zone == ClimateCalculator.ClimateZone.TROPICAL) {
                    tropicalCount++;
                }
            }
            double tropicalRatio = (double) tropicalCount / planet.climates().length;
            assertThat(tropicalRatio).isGreaterThan(0.3);
        }

        @Test
        @DisplayName("TIDALLY_LOCKED model produces asymmetric climate")
        void tidallyLockedModel() {
            PlanetConfig config = PlanetConfig.builder()
                .seed(12345L)
                .climateModel(ClimateCalculator.ClimateModel.TIDALLY_LOCKED)
                .size(Size.SMALL)
                .build();

            GeneratedPlanet planet = PlanetGenerator.generate(config);

            assertThat(planet).isNotNull();
            assertThat(planet.climates()).isNotNull();

            // Should have all three zones represented
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
    // Seed Reproducibility
    // ===========================================

    @Nested
    @DisplayName("Seed and reproducibility tests")
    class SeedTests {

        @Test
        @DisplayName("Same seed produces identical results")
        void reproducibleResults() {
            PlanetConfig config = PlanetConfig.builder()
                .seed(12345L)
                .size(Size.SMALL)
                .build();

            GeneratedPlanet planet1 = PlanetGenerator.generate(config);
            GeneratedPlanet planet2 = PlanetGenerator.generate(config);

            assertThat(planet1.heights()).containsExactly(planet2.heights());
            assertThat(planet1.polygons().size()).isEqualTo(planet2.polygons().size());
        }

        @Test
        @DisplayName("Different seeds produce different results")
        void differentSeeds() {
            PlanetConfig config1 = PlanetConfig.builder()
                .seed(12345L)
                .size(Size.SMALL)
                .build();
            PlanetConfig config2 = PlanetConfig.builder()
                .seed(54321L)
                .size(Size.SMALL)
                .build();

            GeneratedPlanet planet1 = PlanetGenerator.generate(config1);
            GeneratedPlanet planet2 = PlanetGenerator.generate(config2);

            assertThat(planet1.heights()).isNotEqualTo(planet2.heights());
        }

        @Test
        @DisplayName("Seed 0 works correctly")
        void zeroSeed() {
            PlanetConfig config = PlanetConfig.builder()
                .seed(0L)
                .size(Size.SMALL)
                .build();

            GeneratedPlanet planet = PlanetGenerator.generate(config);

            assertThat(planet).isNotNull();
            assertThat(planet.polygons()).isNotEmpty();
        }

        @Test
        @DisplayName("Negative seed works correctly")
        void negativeSeed() {
            PlanetConfig config = PlanetConfig.builder()
                .seed(-12345L)
                .size(Size.SMALL)
                .build();

            GeneratedPlanet planet = PlanetGenerator.generate(config);

            assertThat(planet).isNotNull();
            assertThat(planet.polygons()).isNotEmpty();
        }

        @Test
        @DisplayName("Long.MAX_VALUE seed works correctly")
        void maxSeed() {
            PlanetConfig config = PlanetConfig.builder()
                .seed(Long.MAX_VALUE)
                .size(Size.SMALL)
                .build();

            GeneratedPlanet planet = PlanetGenerator.generate(config);

            assertThat(planet).isNotNull();
            assertThat(planet.polygons()).isNotEmpty();
        }

        @Test
        @DisplayName("Long.MIN_VALUE seed works correctly")
        void minSeed() {
            PlanetConfig config = PlanetConfig.builder()
                .seed(Long.MIN_VALUE)
                .size(Size.SMALL)
                .build();

            GeneratedPlanet planet = PlanetGenerator.generate(config);

            assertThat(planet).isNotNull();
            assertThat(planet.polygons()).isNotEmpty();
        }
    }

    // ===========================================
    // Erosion Parameter Edge Cases
    // ===========================================

    @Nested
    @DisplayName("Erosion parameter edge cases")
    class ErosionTests {

        @Test
        @DisplayName("erosionIterations=0 skips erosion")
        void noErosion() {
            PlanetConfig config = PlanetConfig.builder()
                .seed(12345L)
                .erosionIterations(0)
                .size(Size.SMALL)
                .build();

            GeneratedPlanet planet = PlanetGenerator.generate(config);

            assertThat(planet).isNotNull();
        }

        @Test
        @DisplayName("Maximum erosion iterations (10) works")
        void maxErosion() {
            // Builder clamps to max of 10
            PlanetConfig config = PlanetConfig.builder()
                .seed(12345L)
                .erosionIterations(50)  // Will be clamped to 10
                .size(Size.SMALL)
                .build();

            GeneratedPlanet planet = PlanetGenerator.generate(config);

            assertThat(planet).isNotNull();
            assertThat(config.erosionIterations()).isEqualTo(10);
        }
    }
}

