package com.teamgannon.trips.planetarymodelling.procedural;

import com.teamgannon.trips.solarsysmodelling.accrete.Planet;
import com.teamgannon.trips.solarsysmodelling.accrete.SimStar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for input validation and error handling in the procedural planet generation pipeline.
 */
class ValidationTest {

    private SimStar testStar;

    @BeforeEach
    void setUp() {
        testStar = new SimStar(1.0, 1.0, 1.0, 5778, 4.83);
        testStar.setAge();
    }

    // ===========================================
    // PlanetConfig Validation
    // ===========================================

    @Nested
    @DisplayName("PlanetConfig validation")
    class PlanetConfigValidationTests {

        @Test
        @DisplayName("Distortion arrays must have consistent sizes")
        void distortionArraysMustMatch() {
            assertThatThrownBy(() ->
                PlanetConfig.builder()
                    .distortionProgressThresholds(List.of(0.25, 0.50))  // 2 elements
                    .distortionValues(List.of(0.1, 0.2, 0.3))           // 3 elements - mismatch!
                    .build()
            ).isInstanceOf(IllegalArgumentException.class)
             .hasMessageContaining("Distortion");
        }

        @Test
        @DisplayName("Valid distortion arrays build successfully")
        void validDistortionArrays() {
            PlanetConfig config = PlanetConfig.builder()
                .distortionProgressThresholds(List.of(0.25, 0.50, 0.75))
                .distortionValues(List.of(0.1, 0.2, 0.3))
                .build();

            assertThat(config).isNotNull();
            assertThat(config.distortionProgressThresholds()).hasSize(3);
        }

        @Test
        @DisplayName("Water fraction is clamped to valid range")
        void waterFractionClamped() {
            PlanetConfig configHigh = PlanetConfig.builder()
                .waterFraction(2.0)  // Above 1.0
                .build();
            PlanetConfig configLow = PlanetConfig.builder()
                .waterFraction(-0.5)  // Below 0.0
                .build();

            assertThat(configHigh.waterFraction()).isLessThanOrEqualTo(1.0);
            assertThat(configLow.waterFraction()).isGreaterThanOrEqualTo(0.0);
        }

        @Test
        @DisplayName("Plate count is clamped to valid range")
        void plateCountClamped() {
            PlanetConfig configHigh = PlanetConfig.builder()
                .plateCount(100)  // Above 21
                .build();
            PlanetConfig configLow = PlanetConfig.builder()
                .plateCount(1)  // Below 7
                .build();

            assertThat(configHigh.plateCount()).isLessThanOrEqualTo(21);
            assertThat(configLow.plateCount()).isGreaterThanOrEqualTo(7);
        }

        @Test
        @DisplayName("Erosion iterations are clamped to valid range")
        void erosionIterationsClamped() {
            PlanetConfig configHigh = PlanetConfig.builder()
                .erosionIterations(50)  // Above 10
                .build();
            PlanetConfig configLow = PlanetConfig.builder()
                .erosionIterations(-5)  // Below 0
                .build();

            assertThat(configHigh.erosionIterations()).isLessThanOrEqualTo(10);
            assertThat(configLow.erosionIterations()).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("Height scale multiplier has minimum value")
        void heightScaleHasMinimum() {
            PlanetConfig config = PlanetConfig.builder()
                .heightScaleMultiplier(0.0)  // Below minimum
                .build();

            assertThat(config.heightScaleMultiplier()).isGreaterThanOrEqualTo(0.5);
        }

        @Test
        @DisplayName("Rift depth multiplier has minimum value")
        void riftDepthHasMinimum() {
            PlanetConfig config = PlanetConfig.builder()
                .riftDepthMultiplier(0.0)  // Below minimum
                .build();

            assertThat(config.riftDepthMultiplier()).isGreaterThanOrEqualTo(0.5);
        }

        @Test
        @DisplayName("Oceanic plate ratio is clamped to valid range")
        void oceanicPlateRatioClamped() {
            PlanetConfig configHigh = PlanetConfig.builder()
                .oceanicPlateRatio(1.5)
                .build();
            PlanetConfig configLow = PlanetConfig.builder()
                .oceanicPlateRatio(-0.5)
                .build();

            assertThat(configHigh.oceanicPlateRatio()).isLessThanOrEqualTo(1.0);
            assertThat(configLow.oceanicPlateRatio()).isGreaterThanOrEqualTo(0.0);
        }

        @Test
        @DisplayName("Hotspot probability is clamped to valid range")
        void hotspotProbabilityClamped() {
            PlanetConfig configHigh = PlanetConfig.builder()
                .hotspotProbability(1.5)
                .build();
            PlanetConfig configLow = PlanetConfig.builder()
                .hotspotProbability(-0.5)
                .build();

            assertThat(configHigh.hotspotProbability()).isLessThanOrEqualTo(1.0);
            assertThat(configLow.hotspotProbability()).isGreaterThanOrEqualTo(0.0);
        }

        @Test
        @DisplayName("Null climate model defaults to SIMPLE_LATITUDE")
        void nullClimateModelDefaults() {
            PlanetConfig config = PlanetConfig.builder()
                .climateModel(null)
                .build();

            assertThat(config.climateModel()).isEqualTo(ClimateCalculator.ClimateModel.SIMPLE_LATITUDE);
        }
    }

    // ===========================================
    // PlanetGenerator Validation
    // ===========================================

    @Nested
    @DisplayName("PlanetGenerator validation")
    class PlanetGeneratorValidationTests {

        @Test
        @DisplayName("Null config throws IllegalArgumentException")
        void nullConfigThrows() {
            assertThatThrownBy(() -> PlanetGenerator.generate(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null");
        }

        @Test
        @DisplayName("Null config in constructor throws IllegalArgumentException")
        void nullConfigInConstructorThrows() {
            assertThatThrownBy(() -> new PlanetGenerator(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null");
        }
    }

    // ===========================================
    // Accrete Bridge Validation
    // ===========================================

    @Nested
    @DisplayName("Accrete bridge validation")
    class AccreteBridgeValidationTests {

        @Test
        @DisplayName("Null planet throws IllegalArgumentException")
        void nullPlanetThrows() {
            assertThatThrownBy(() -> PlanetGenerator.createBiasedConfig(null, 12345L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null");
        }

        @Test
        @DisplayName("Planet with zero radius throws IllegalArgumentException")
        void zeroRadiusThrows() {
            Planet planet = new Planet(testStar);
            planet.setRadius(0);

            assertThatThrownBy(() -> PlanetGenerator.createBiasedConfig(planet, 12345L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("radius");
        }

        @Test
        @DisplayName("Planet with negative radius throws IllegalArgumentException")
        void negativeRadiusThrows() {
            Planet planet = new Planet(testStar);
            planet.setRadius(-1000);

            assertThatThrownBy(() -> PlanetGenerator.createBiasedConfig(planet, 12345L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("radius");
        }

        @Test
        @DisplayName("generateFromAccrete validates planet")
        void generateFromAccreteValidates() {
            assertThatThrownBy(() -> PlanetGenerator.generateFromAccrete(null, 12345L))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Hydrosphere values outside 0-100 are clamped")
        void hydrosphereOutOfRangeClamped() {
            Planet planetHigh = new Planet(testStar);
            planetHigh.setRadius(6371);
            planetHigh.setHydrosphere(150);  // Above 100
            planetHigh.setSurfaceGravity(1.0);
            planetHigh.setSurfaceTemperature(288);

            Planet planetLow = new Planet(testStar);
            planetLow.setRadius(6371);
            planetLow.setHydrosphere(-50);  // Below 0
            planetLow.setSurfaceGravity(1.0);
            planetLow.setSurfaceTemperature(288);

            PlanetConfig configHigh = PlanetGenerator.createBiasedConfig(planetHigh, 12345L);
            PlanetConfig configLow = PlanetGenerator.createBiasedConfig(planetLow, 12345L);

            assertThat(configHigh.waterFraction()).isLessThanOrEqualTo(1.0);
            assertThat(configLow.waterFraction()).isGreaterThanOrEqualTo(0.0);
        }

        @Test
        @DisplayName("Gas giant planet generates successfully")
        void gasGiantGenerates() {
            Planet gasGiant = new Planet(testStar);
            gasGiant.setRadius(69911);
            gasGiant.setGasGiant(true);
            gasGiant.setSurfaceGravity(2.4);
            gasGiant.setSurfaceTemperature(165);

            // Should not throw - gas giants are allowed for visualization
            assertThatCode(() -> PlanetGenerator.generateFromAccrete(gasGiant, 12345L))
                .doesNotThrowAnyException();
        }
    }

    // ===========================================
    // Edge Case Robustness
    // ===========================================

    @Nested
    @DisplayName("Edge case robustness")
    class RobustnessTests {

        @Test
        @DisplayName("Planet with extreme surface gravity is handled")
        void extremeSurfaceGravity() {
            Planet planet = new Planet(testStar);
            planet.setRadius(6371);
            planet.setHydrosphere(50);
            planet.setSurfaceGravity(100.0);  // Extreme gravity
            planet.setSurfaceTemperature(288);

            assertThatCode(() -> PlanetGenerator.createBiasedConfig(planet, 12345L))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Planet with zero surface gravity is handled")
        void zeroSurfaceGravity() {
            Planet planet = new Planet(testStar);
            planet.setRadius(6371);
            planet.setHydrosphere(50);
            planet.setSurfaceGravity(0.0);  // Zero gravity
            planet.setSurfaceTemperature(288);

            // May produce infinite/NaN in calculations, should be handled gracefully
            assertThatCode(() -> PlanetGenerator.createBiasedConfig(planet, 12345L))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Planet with extreme temperature is handled")
        void extremeTemperature() {
            Planet hotPlanet = new Planet(testStar);
            hotPlanet.setRadius(6371);
            hotPlanet.setHydrosphere(0);
            hotPlanet.setSurfaceGravity(1.0);
            hotPlanet.setSurfaceTemperature(5000);  // Very hot

            Planet coldPlanet = new Planet(testStar);
            coldPlanet.setRadius(6371);
            coldPlanet.setHydrosphere(0);
            coldPlanet.setSurfaceGravity(1.0);
            coldPlanet.setSurfaceTemperature(10);  // Very cold

            assertThatCode(() -> PlanetGenerator.createBiasedConfig(hotPlanet, 12345L))
                .doesNotThrowAnyException();
            assertThatCode(() -> PlanetGenerator.createBiasedConfig(coldPlanet, 12345L))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Default config generates successfully")
        void defaultConfigGenerates() {
            assertThatCode(() -> PlanetGenerator.generateDefault())
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Minimum size with maximum plates is handled")
        void minSizeMaxPlates() {
            // This could cause issues if there aren't enough polygons for all plates
            PlanetConfig config = PlanetConfig.builder()
                .size(PlanetConfig.Size.DUEL)  // Smallest: 1212 polygons
                .plateCount(21)  // Maximum plates
                .seed(12345L)
                .build();

            // Should still work - plates will be smaller
            assertThatCode(() -> PlanetGenerator.generate(config))
                .doesNotThrowAnyException();
        }
    }

    // ===========================================
    // Thread Safety
    // ===========================================

    @Nested
    @DisplayName("Thread safety")
    class ThreadSafetyTests {

        @Test
        @DisplayName("Multiple generations with same config produce identical results")
        void identicalResultsWithSameConfig() {
            PlanetConfig config = PlanetConfig.builder()
                .seed(12345L)
                .size(PlanetConfig.Size.SMALL)
                .build();

            // Generate multiple times
            var planet1 = PlanetGenerator.generate(config);
            var planet2 = PlanetGenerator.generate(config);
            var planet3 = PlanetGenerator.generate(config);

            // All should be identical
            assertThat(planet1.heights()).containsExactly(planet2.heights());
            assertThat(planet2.heights()).containsExactly(planet3.heights());
        }
    }
}
