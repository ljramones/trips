package com.teamgannon.trips.planetarymodelling.procedural;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive tests for PlanetConfig record and Builder.
 */
class PlanetConfigTest {

    // ===========================================
    // Builder Default Values
    // ===========================================

    @Nested
    @DisplayName("Builder default values")
    class DefaultValueTests {

        @Test
        @DisplayName("Default config has expected values")
        void defaultConfigHasExpectedValues() {
            PlanetConfig config = PlanetConfig.builder().build();

            // Size defaults
            assertThat(config.n()).isEqualTo(PlanetConfig.Size.STANDARD.n);
            assertThat(config.polyCount()).isEqualTo(PlanetConfig.Size.STANDARD.polyCount);

            // Tectonic defaults
            assertThat(config.plateCount()).isEqualTo(14);
            assertThat(config.radius()).isEqualTo(6371.0);
            assertThat(config.waterFraction()).isEqualTo(0.66);
            assertThat(config.oceanicPlateRatio()).isEqualTo(0.65);
            assertThat(config.heightScaleMultiplier()).isEqualTo(1.0);
            assertThat(config.riftDepthMultiplier()).isEqualTo(1.0);
            assertThat(config.hotspotProbability()).isEqualTo(0.12);
            assertThat(config.enableActiveTectonics()).isTrue();

            // Erosion defaults
            assertThat(config.erosionIterations()).isEqualTo(5);
            assertThat(config.rainfallScale()).isEqualTo(1.0);
            assertThat(config.enableRivers()).isTrue();

            // Continuous height defaults
            assertThat(config.useContinuousHeights()).isFalse();
            assertThat(config.continuousReliefMin()).isEqualTo(-4.0);
            assertThat(config.continuousReliefMax()).isEqualTo(4.0);

            // Erosion threshold defaults
            assertThat(config.rainfallThreshold()).isEqualTo(0.3);
            assertThat(config.riverSourceThreshold()).isEqualTo(0.7);
            assertThat(config.riverSourceElevationMin()).isEqualTo(0.5);
            assertThat(config.erosionCap()).isEqualTo(0.3);
            assertThat(config.depositionFactor()).isEqualTo(0.5);
            assertThat(config.riverCarveDepth()).isEqualTo(0.3);

            // Climate defaults
            assertThat(config.climateModel()).isEqualTo(ClimateCalculator.ClimateModel.SIMPLE_LATITUDE);
            assertThat(config.axialTiltDegrees()).isEqualTo(23.5);
            assertThat(config.seasonalOffsetDegrees()).isEqualTo(0.0);
            assertThat(config.seasonalSamples()).isEqualTo(12);

            // Impact feature defaults (disabled)
            assertThat(config.craterDensity()).isEqualTo(0.0);
            assertThat(config.craterDepthMultiplier()).isEqualTo(1.0);
            assertThat(config.craterMaxRadius()).isEqualTo(8);
            assertThat(config.enableVolcanos()).isFalse();
            assertThat(config.volcanoDensity()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Boundary effects have defaults")
        void boundaryEffectsHaveDefaults() {
            PlanetConfig config = PlanetConfig.builder().build();

            assertThat(config.boundaryEffects()).isNotNull();
            assertThat(config.boundaryEffects().convergentOceanicOceanic()).isNotNull();
            assertThat(config.boundaryEffects().convergentContinentalContinental()).isNotNull();
            assertThat(config.boundaryEffects().divergentContinentalContinental()).isNotNull();
        }
    }

    // ===========================================
    // Parameter Clamping Tests
    // ===========================================

    @Nested
    @DisplayName("Parameter clamping")
    class ClampingTests {

        @Test
        @DisplayName("Plate count is clamped to 7-21")
        void plateCountClamped() {
            assertThat(PlanetConfig.builder().plateCount(1).build().plateCount()).isEqualTo(7);
            assertThat(PlanetConfig.builder().plateCount(7).build().plateCount()).isEqualTo(7);
            assertThat(PlanetConfig.builder().plateCount(14).build().plateCount()).isEqualTo(14);
            assertThat(PlanetConfig.builder().plateCount(21).build().plateCount()).isEqualTo(21);
            assertThat(PlanetConfig.builder().plateCount(100).build().plateCount()).isEqualTo(21);
        }

        @Test
        @DisplayName("Water fraction is clamped to 0-1")
        void waterFractionClamped() {
            assertThat(PlanetConfig.builder().waterFraction(-0.5).build().waterFraction()).isEqualTo(0.0);
            assertThat(PlanetConfig.builder().waterFraction(0.0).build().waterFraction()).isEqualTo(0.0);
            assertThat(PlanetConfig.builder().waterFraction(0.5).build().waterFraction()).isEqualTo(0.5);
            assertThat(PlanetConfig.builder().waterFraction(1.0).build().waterFraction()).isEqualTo(1.0);
            assertThat(PlanetConfig.builder().waterFraction(1.5).build().waterFraction()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("Oceanic plate ratio is clamped to 0-1")
        void oceanicPlateRatioClamped() {
            assertThat(PlanetConfig.builder().oceanicPlateRatio(-0.1).build().oceanicPlateRatio()).isEqualTo(0.0);
            assertThat(PlanetConfig.builder().oceanicPlateRatio(1.5).build().oceanicPlateRatio()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("Height scale multiplier has minimum of 0.5")
        void heightScaleMultiplierMinimum() {
            assertThat(PlanetConfig.builder().heightScaleMultiplier(0.1).build().heightScaleMultiplier()).isEqualTo(0.5);
            assertThat(PlanetConfig.builder().heightScaleMultiplier(2.0).build().heightScaleMultiplier()).isEqualTo(2.0);
        }

        @Test
        @DisplayName("Erosion iterations clamped to 0-10")
        void erosionIterationsClamped() {
            assertThat(PlanetConfig.builder().erosionIterations(-5).build().erosionIterations()).isEqualTo(0);
            assertThat(PlanetConfig.builder().erosionIterations(15).build().erosionIterations()).isEqualTo(10);
        }

        @Test
        @DisplayName("Rainfall scale clamped to 0-2")
        void rainfallScaleClamped() {
            assertThat(PlanetConfig.builder().rainfallScale(-0.5).build().rainfallScale()).isEqualTo(0.0);
            assertThat(PlanetConfig.builder().rainfallScale(3.0).build().rainfallScale()).isEqualTo(2.0);
        }

        @Test
        @DisplayName("Axial tilt clamped to 0-60 degrees")
        void axialTiltClamped() {
            assertThat(PlanetConfig.builder().axialTiltDegrees(-10).build().axialTiltDegrees()).isEqualTo(0.0);
            assertThat(PlanetConfig.builder().axialTiltDegrees(90).build().axialTiltDegrees()).isEqualTo(60.0);
        }

        @Test
        @DisplayName("Seasonal samples clamped to 4-48")
        void seasonalSamplesClamped() {
            assertThat(PlanetConfig.builder().seasonalSamples(1).build().seasonalSamples()).isEqualTo(4);
            assertThat(PlanetConfig.builder().seasonalSamples(100).build().seasonalSamples()).isEqualTo(48);
        }

        @Test
        @DisplayName("Crater density clamped to 0-1")
        void craterDensityClamped() {
            assertThat(PlanetConfig.builder().craterDensity(-0.5).build().craterDensity()).isEqualTo(0.0);
            assertThat(PlanetConfig.builder().craterDensity(1.5).build().craterDensity()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("Crater max radius clamped to 2-20")
        void craterMaxRadiusClamped() {
            assertThat(PlanetConfig.builder().craterMaxRadius(0).build().craterMaxRadius()).isEqualTo(2);
            assertThat(PlanetConfig.builder().craterMaxRadius(50).build().craterMaxRadius()).isEqualTo(20);
        }

        @Test
        @DisplayName("Crater depth multiplier has minimum of 0.1")
        void craterDepthMultiplierMinimum() {
            assertThat(PlanetConfig.builder().craterDepthMultiplier(0.01).build().craterDepthMultiplier()).isEqualTo(0.1);
            assertThat(PlanetConfig.builder().craterDepthMultiplier(5.0).build().craterDepthMultiplier()).isEqualTo(5.0);
        }

        @Test
        @DisplayName("Volcano density clamped to 0-1")
        void volcanoDensityClamped() {
            assertThat(PlanetConfig.builder().volcanoDensity(-0.5).build().volcanoDensity()).isEqualTo(0.0);
            assertThat(PlanetConfig.builder().volcanoDensity(2.0).build().volcanoDensity()).isEqualTo(1.0);
        }
    }

    // ===========================================
    // SubSeed Tests
    // ===========================================

    @Nested
    @DisplayName("SubSeed derivation")
    class SubSeedTests {

        @Test
        @DisplayName("SubSeed is deterministic for same seed and phase")
        void subSeedIsDeterministic() {
            PlanetConfig config = PlanetConfig.builder().seed(12345L).build();

            long subSeed1 = config.subSeed(1);
            long subSeed2 = config.subSeed(1);

            assertThat(subSeed1).isEqualTo(subSeed2);
        }

        @Test
        @DisplayName("Different phases produce different subSeeds")
        void differentPhasesDifferentSubSeeds() {
            PlanetConfig config = PlanetConfig.builder().seed(12345L).build();

            long phase1 = config.subSeed(1);
            long phase2 = config.subSeed(2);
            long phase3 = config.subSeed(3);

            assertThat(phase1).isNotEqualTo(phase2);
            assertThat(phase2).isNotEqualTo(phase3);
            assertThat(phase1).isNotEqualTo(phase3);
        }

        @Test
        @DisplayName("Different seeds produce different subSeeds for same phase")
        void differentSeedsDifferentSubSeeds() {
            PlanetConfig config1 = PlanetConfig.builder().seed(12345L).build();
            PlanetConfig config2 = PlanetConfig.builder().seed(54321L).build();

            assertThat(config1.subSeed(1)).isNotEqualTo(config2.subSeed(1));
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8})
        @DisplayName("All generation phases have unique subSeeds")
        void allPhasesHaveUniqueSubSeeds(int phase) {
            PlanetConfig config = PlanetConfig.builder().seed(42L).build();

            // Collect all phase subSeeds
            long[] allSubSeeds = new long[8];
            for (int i = 0; i < 8; i++) {
                allSubSeeds[i] = config.subSeed(i + 1);
            }

            // Verify this phase's subSeed is unique
            long thisSubSeed = config.subSeed(phase);
            int matches = 0;
            for (long subSeed : allSubSeeds) {
                if (subSeed == thisSubSeed) matches++;
            }
            assertThat(matches).isEqualTo(1);
        }
    }

    // ===========================================
    // toBuilder Tests
    // ===========================================

    @Nested
    @DisplayName("toBuilder round-trip")
    class ToBuilderTests {

        @Test
        @DisplayName("toBuilder preserves all values")
        void toBuilderPreservesAllValues() {
            PlanetConfig original = PlanetConfig.builder()
                .seed(99999L)
                .size(PlanetConfig.Size.HUGE)
                .plateCount(18)
                .radius(8000.0)
                .waterFraction(0.8)
                .oceanicPlateRatio(0.7)
                .heightScaleMultiplier(1.5)
                .riftDepthMultiplier(1.2)
                .hotspotProbability(0.2)
                .enableActiveTectonics(false)
                .erosionIterations(8)
                .rainfallScale(1.5)
                .enableRivers(false)
                .useContinuousHeights(true)
                .continuousReliefMin(-5.0)
                .continuousReliefMax(6.0)
                .rainfallThreshold(0.4)
                .riverSourceThreshold(0.8)
                .riverSourceElevationMin(0.6)
                .erosionCap(0.4)
                .depositionFactor(0.6)
                .riverCarveDepth(0.4)
                .climateModel(ClimateCalculator.ClimateModel.SEASONAL)
                .axialTiltDegrees(30.0)
                .seasonalOffsetDegrees(45.0)
                .seasonalSamples(24)
                .craterDensity(0.5)
                .craterDepthMultiplier(2.0)
                .craterMaxRadius(12)
                .enableVolcanos(true)
                .volcanoDensity(0.3)
                .build();

            PlanetConfig rebuilt = original.toBuilder().build();

            assertThat(rebuilt.seed()).isEqualTo(original.seed());
            assertThat(rebuilt.n()).isEqualTo(original.n());
            assertThat(rebuilt.plateCount()).isEqualTo(original.plateCount());
            assertThat(rebuilt.radius()).isEqualTo(original.radius());
            assertThat(rebuilt.waterFraction()).isEqualTo(original.waterFraction());
            assertThat(rebuilt.oceanicPlateRatio()).isEqualTo(original.oceanicPlateRatio());
            assertThat(rebuilt.heightScaleMultiplier()).isEqualTo(original.heightScaleMultiplier());
            assertThat(rebuilt.riftDepthMultiplier()).isEqualTo(original.riftDepthMultiplier());
            assertThat(rebuilt.hotspotProbability()).isEqualTo(original.hotspotProbability());
            assertThat(rebuilt.enableActiveTectonics()).isEqualTo(original.enableActiveTectonics());
            assertThat(rebuilt.erosionIterations()).isEqualTo(original.erosionIterations());
            assertThat(rebuilt.rainfallScale()).isEqualTo(original.rainfallScale());
            assertThat(rebuilt.enableRivers()).isEqualTo(original.enableRivers());
            assertThat(rebuilt.useContinuousHeights()).isEqualTo(original.useContinuousHeights());
            assertThat(rebuilt.continuousReliefMin()).isEqualTo(original.continuousReliefMin());
            assertThat(rebuilt.continuousReliefMax()).isEqualTo(original.continuousReliefMax());
            assertThat(rebuilt.rainfallThreshold()).isEqualTo(original.rainfallThreshold());
            assertThat(rebuilt.riverSourceThreshold()).isEqualTo(original.riverSourceThreshold());
            assertThat(rebuilt.riverSourceElevationMin()).isEqualTo(original.riverSourceElevationMin());
            assertThat(rebuilt.erosionCap()).isEqualTo(original.erosionCap());
            assertThat(rebuilt.depositionFactor()).isEqualTo(original.depositionFactor());
            assertThat(rebuilt.riverCarveDepth()).isEqualTo(original.riverCarveDepth());
            assertThat(rebuilt.climateModel()).isEqualTo(original.climateModel());
            assertThat(rebuilt.axialTiltDegrees()).isEqualTo(original.axialTiltDegrees());
            assertThat(rebuilt.seasonalOffsetDegrees()).isEqualTo(original.seasonalOffsetDegrees());
            assertThat(rebuilt.seasonalSamples()).isEqualTo(original.seasonalSamples());
            assertThat(rebuilt.craterDensity()).isEqualTo(original.craterDensity());
            assertThat(rebuilt.craterDepthMultiplier()).isEqualTo(original.craterDepthMultiplier());
            assertThat(rebuilt.craterMaxRadius()).isEqualTo(original.craterMaxRadius());
            assertThat(rebuilt.enableVolcanos()).isEqualTo(original.enableVolcanos());
            assertThat(rebuilt.volcanoDensity()).isEqualTo(original.volcanoDensity());
        }

        @Test
        @DisplayName("toBuilder allows modification")
        void toBuilderAllowsModification() {
            PlanetConfig original = PlanetConfig.builder()
                .seed(12345L)
                .plateCount(14)
                .craterDensity(0.0)
                .build();

            PlanetConfig modified = original.toBuilder()
                .plateCount(18)
                .craterDensity(0.5)
                .build();

            assertThat(modified.seed()).isEqualTo(12345L); // Preserved
            assertThat(modified.plateCount()).isEqualTo(18); // Changed
            assertThat(modified.craterDensity()).isEqualTo(0.5); // Changed
        }
    }

    // ===========================================
    // Size Enum Tests
    // ===========================================

    @Nested
    @DisplayName("Size enum")
    class SizeEnumTests {

        @ParameterizedTest
        @EnumSource(PlanetConfig.Size.class)
        @DisplayName("All sizes have positive n and polyCount")
        void allSizesHavePositiveValues(PlanetConfig.Size size) {
            assertThat(size.n).isGreaterThan(0);
            assertThat(size.polyCount).isGreaterThan(0);
        }

        @Test
        @DisplayName("Sizes are ordered by polygon count")
        void sizesAreOrderedByPolyCount() {
            PlanetConfig.Size[] sizes = PlanetConfig.Size.values();

            for (int i = 1; i < sizes.length; i++) {
                assertThat(sizes[i].polyCount)
                    .as("Size %s should have more polygons than %s", sizes[i], sizes[i-1])
                    .isGreaterThan(sizes[i-1].polyCount);
            }
        }

        @Test
        @DisplayName("Size n values increase with size")
        void sizeNValuesIncrease() {
            PlanetConfig.Size[] sizes = PlanetConfig.Size.values();

            for (int i = 1; i < sizes.length; i++) {
                assertThat(sizes[i].n)
                    .as("Size %s should have higher n than %s", sizes[i], sizes[i-1])
                    .isGreaterThan(sizes[i-1].n);
            }
        }

        @ParameterizedTest
        @CsvSource({
            "DUEL, 11, 1212",
            "TINY, 15, 2252",
            "SMALL, 19, 3612",
            "STANDARD, 21, 4412",
            "LARGE, 24, 5762",
            "HUGE, 26, 6762",
            "COLOSSAL, 32, 10242"
        })
        @DisplayName("Size enum has expected values")
        void sizeEnumHasExpectedValues(String name, int n, int polyCount) {
            PlanetConfig.Size size = PlanetConfig.Size.valueOf(name);
            assertThat(size.n).isEqualTo(n);
            assertThat(size.polyCount).isEqualTo(polyCount);
        }
    }

    // ===========================================
    // Goldberg Radius Tests
    // ===========================================

    @Nested
    @DisplayName("Goldberg radius calculation")
    class GoldbergRadiusTests {

        @Test
        @DisplayName("Goldberg radius is positive")
        void goldbergRadiusIsPositive() {
            for (PlanetConfig.Size size : PlanetConfig.Size.values()) {
                PlanetConfig config = PlanetConfig.builder().size(size).build();
                assertThat(config.goldbergRadius())
                    .as("Size %s should have positive goldberg radius", size)
                    .isGreaterThan(0.0);
            }
        }

        @Test
        @DisplayName("Goldberg radius scales with subdivision level")
        void goldbergRadiusScalesWithN() {
            // Goldberg radius is the circumradius of the polyhedron, not a unit sphere
            // Larger n values (more subdivisions) produce larger radii
            PlanetConfig small = PlanetConfig.builder().size(PlanetConfig.Size.DUEL).build();
            PlanetConfig large = PlanetConfig.builder().size(PlanetConfig.Size.COLOSSAL).build();

            assertThat(large.goldbergRadius()).isGreaterThan(small.goldbergRadius());
        }

        @Test
        @DisplayName("Goldberg radius is deterministic")
        void goldbergRadiusIsDeterministic() {
            PlanetConfig config = PlanetConfig.builder().size(PlanetConfig.Size.LARGE).build();

            double radius1 = config.goldbergRadius();
            double radius2 = config.goldbergRadius();

            assertThat(radius1).isEqualTo(radius2);
        }
    }

    // ===========================================
    // fromAccreteRadius Tests
    // ===========================================

    @Nested
    @DisplayName("fromAccreteRadius size selection")
    class FromAccreteRadiusTests {

        @Test
        @DisplayName("Small radius selects SMALL size")
        void smallRadiusSelectsSmall() {
            PlanetConfig config = PlanetConfig.builder().fromAccreteRadius(2000).build();
            assertThat(config.n()).isEqualTo(PlanetConfig.Size.SMALL.n);
        }

        @Test
        @DisplayName("Earth-like radius selects STANDARD size")
        void earthRadiusSelectsStandard() {
            PlanetConfig config = PlanetConfig.builder().fromAccreteRadius(6371).build();
            assertThat(config.n()).isEqualTo(PlanetConfig.Size.LARGE.n); // >6000
        }

        @Test
        @DisplayName("Large radius selects HUGE size")
        void largeRadiusSelectsHuge() {
            PlanetConfig config = PlanetConfig.builder().fromAccreteRadius(15000).build();
            assertThat(config.n()).isEqualTo(PlanetConfig.Size.HUGE.n);
        }

        @Test
        @DisplayName("Radius is stored correctly")
        void radiusIsStoredCorrectly() {
            double radiusKm = 7500.0;
            PlanetConfig config = PlanetConfig.builder().fromAccreteRadius(radiusKm).build();
            assertThat(config.radius()).isEqualTo(radiusKm);
        }
    }

    // ===========================================
    // Validation Tests
    // ===========================================

    @Nested
    @DisplayName("Builder validation")
    class ValidationTests {

        @Test
        @DisplayName("Mismatched distortion arrays throw exception")
        void mismatchedDistortionArraysThrow() {
            assertThatThrownBy(() ->
                PlanetConfig.builder()
                    .distortionProgressThresholds(List.of(0.25, 0.5))
                    .distortionValues(List.of(0.1, 0.2, 0.3))
                    .build()
            ).isInstanceOf(IllegalArgumentException.class)
             .hasMessageContaining("same size");
        }

        @Test
        @DisplayName("Continuous relief min/max are normalized")
        void continuousReliefNormalized() {
            // Set min > max, should be normalized
            PlanetConfig config = PlanetConfig.builder()
                .continuousReliefMin(5.0)
                .continuousReliefMax(-5.0)
                .build();

            assertThat(config.continuousReliefMin()).isEqualTo(-5.0);
            assertThat(config.continuousReliefMax()).isEqualTo(5.0);
        }

        @Test
        @DisplayName("Null climate model defaults to SIMPLE_LATITUDE")
        void nullClimateModelDefaultsToSimple() {
            PlanetConfig config = PlanetConfig.builder()
                .climateModel(null)
                .build();

            assertThat(config.climateModel()).isEqualTo(ClimateCalculator.ClimateModel.SIMPLE_LATITUDE);
        }
    }

    // ===========================================
    // BoundaryEffectConfig Tests
    // ===========================================

    @Nested
    @DisplayName("BoundaryEffectConfig")
    class BoundaryEffectConfigTests {

        @Test
        @DisplayName("Defaults have valid layer counts")
        void defaultsHaveValidLayerCounts() {
            var defaults = PlanetConfig.BoundaryEffectConfig.defaults();

            assertThat(defaults.convergentOceanicOceanic().layerCount()).isGreaterThan(0);
            assertThat(defaults.convergentOceanicContinental().layerCount()).isGreaterThan(0);
            assertThat(defaults.convergentContinentalContinental().layerCount()).isGreaterThan(0);
            assertThat(defaults.divergentContinentalContinental().layerCount()).isGreaterThan(0);
        }

        @Test
        @DisplayName("EffectParams arrays have matching lengths")
        void effectParamsArraysMatch() {
            var defaults = PlanetConfig.BoundaryEffectConfig.defaults();

            var effect = defaults.convergentOceanicContinental();
            assertThat(effect.percents().length).isEqualTo(effect.deltas().length);
            assertThat(effect.deltas().length).isEqualTo(effect.distortions().length);
        }

        @Test
        @DisplayName("Continental collision chance is between 0 and 1")
        void collisionChanceInRange() {
            var defaults = PlanetConfig.BoundaryEffectConfig.defaults();
            assertThat(defaults.continentalCollisionExtraChance()).isBetween(0.0, 1.0);
        }
    }
}
