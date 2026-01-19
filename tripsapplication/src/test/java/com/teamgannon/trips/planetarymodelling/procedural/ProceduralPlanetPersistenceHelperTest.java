package com.teamgannon.trips.planetarymodelling.procedural;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamgannon.trips.jpa.model.ExoPlanet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for ProceduralPlanetPersistenceHelper.
 * Validates storing/restoring procedural planet configurations.
 */
class ProceduralPlanetPersistenceHelperTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Nested
    @DisplayName("createAccreteSnapshot()")
    class AccreteSnapshotTests {

        @Test
        @DisplayName("Creates valid JSON with all physical parameters")
        void createsValidJsonSnapshot() throws Exception {
            ExoPlanet exo = createEarthLikeExoPlanet();
            long seed = 12345L;

            String json = ProceduralPlanetPersistenceHelper.createAccreteSnapshot(exo, seed);

            assertThat(json).isNotNull();
            Map<String, Object> snapshot = MAPPER.readValue(json, Map.class);

            assertThat(snapshot).containsKey("seed");
            assertThat(snapshot).containsKey("mass");
            assertThat(snapshot).containsKey("radius");
            assertThat(snapshot).containsKey("surfaceGravity");
            assertThat(snapshot).containsKey("hydrosphere");
            assertThat(((Number) snapshot.get("seed")).longValue()).isEqualTo(12345L);
            assertThat(((Number) snapshot.get("mass")).doubleValue()).isEqualTo(1.0);
            assertThat(((Number) snapshot.get("radius")).doubleValue()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("Handles null fields gracefully")
        void handlesNullFieldsGracefully() throws Exception {
            ExoPlanet exo = new ExoPlanet();
            exo.setId(java.util.UUID.randomUUID().toString());

            String json = ProceduralPlanetPersistenceHelper.createAccreteSnapshot(exo, 0L);

            assertThat(json).isNotNull();
            Map<String, Object> snapshot = MAPPER.readValue(json, Map.class);
            assertThat(snapshot).containsKey("seed");
            // Null values should be preserved
            assertThat(snapshot.get("mass")).isNull();
        }

        @Test
        @DisplayName("Includes gas giant flag")
        void includesGasGiantFlag() throws Exception {
            ExoPlanet exo = createGasGiantExoPlanet();

            String json = ProceduralPlanetPersistenceHelper.createAccreteSnapshot(exo, 0L);

            Map<String, Object> snapshot = MAPPER.readValue(json, Map.class);
            assertThat(snapshot.get("gasGiant")).isEqualTo(true);
        }
    }

    @Nested
    @DisplayName("createOverridesSnapshot()")
    class OverridesSnapshotTests {

        @Test
        @DisplayName("Creates valid JSON with all config parameters")
        void createsValidConfigSnapshot() throws Exception {
            PlanetConfig config = PlanetConfig.builder()
                .seed(42L)
                .size(PlanetConfig.Size.LARGE)
                .plateCount(16)
                .waterFraction(0.7)
                .oceanicPlateRatio(0.6)
                .erosionIterations(8)
                .enableRivers(true)
                .climateModel(ClimateCalculator.ClimateModel.SEASONAL)
                .axialTiltDegrees(25.0)
                .build();

            String json = ProceduralPlanetPersistenceHelper.createOverridesSnapshot(config);

            assertThat(json).isNotNull();
            Map<String, Object> snapshot = MAPPER.readValue(json, Map.class);

            assertThat(snapshot.get("size")).isEqualTo("LARGE");
            assertThat(((Number) snapshot.get("plateCount")).intValue()).isEqualTo(16);
            assertThat(((Number) snapshot.get("waterFraction")).doubleValue()).isEqualTo(0.7);
            assertThat(snapshot.get("climateModel")).isEqualTo("SEASONAL");
            assertThat(((Number) snapshot.get("axialTiltDegrees")).doubleValue()).isEqualTo(25.0);
        }

        @Test
        @DisplayName("Includes erosion thresholds")
        void includesErosionThresholds() throws Exception {
            PlanetConfig config = PlanetConfig.builder()
                .rainfallThreshold(0.4)
                .riverSourceThreshold(0.8)
                .erosionCap(0.25)
                .build();

            String json = ProceduralPlanetPersistenceHelper.createOverridesSnapshot(config);

            Map<String, Object> snapshot = MAPPER.readValue(json, Map.class);
            assertThat(((Number) snapshot.get("rainfallThreshold")).doubleValue()).isEqualTo(0.4);
            assertThat(((Number) snapshot.get("riverSourceThreshold")).doubleValue()).isEqualTo(0.8);
            assertThat(((Number) snapshot.get("erosionCap")).doubleValue()).isEqualTo(0.25);
        }

        @Test
        @DisplayName("Includes seasonal parameters")
        void includesSeasonalParameters() throws Exception {
            PlanetConfig config = PlanetConfig.builder()
                .climateModel(ClimateCalculator.ClimateModel.SEASONAL)
                .axialTiltDegrees(30.0)
                .seasonalOffsetDegrees(45.0)
                .seasonalSamples(24)
                .build();

            String json = ProceduralPlanetPersistenceHelper.createOverridesSnapshot(config);

            Map<String, Object> snapshot = MAPPER.readValue(json, Map.class);
            assertThat(((Number) snapshot.get("axialTiltDegrees")).doubleValue()).isEqualTo(30.0);
            assertThat(((Number) snapshot.get("seasonalOffsetDegrees")).doubleValue()).isEqualTo(45.0);
            assertThat(((Number) snapshot.get("seasonalSamples")).intValue()).isEqualTo(24);
        }
    }

    @Nested
    @DisplayName("buildConfigFromExoPlanet()")
    class BuildConfigFromExoPlanetTests {

        @Test
        @DisplayName("Creates config from Earth-like physical properties")
        void createsConfigFromEarthLike() {
            ExoPlanet exo = createEarthLikeExoPlanet();

            PlanetConfig config = ProceduralPlanetPersistenceHelper.buildConfigFromExoPlanet(exo, 12345L);

            assertThat(config).isNotNull();
            assertThat(config.seed()).isEqualTo(12345L);
            assertThat(config.waterFraction()).isBetween(0.6, 0.7);
            assertThat(config.enableActiveTectonics()).isTrue();
            assertThat(config.plateCount()).isBetween(10, 14);
        }

        @Test
        @DisplayName("Creates config for gas giant (no tectonics)")
        void createsConfigForGasGiant() {
            ExoPlanet exo = createGasGiantExoPlanet();

            PlanetConfig config = ProceduralPlanetPersistenceHelper.buildConfigFromExoPlanet(exo, 12345L);

            // Gas giants should have muted tectonics
            assertThat(config.enableActiveTectonics()).isFalse();
            // Due to high mass (>3.0), plate count falls into else branch
            // Gas giant override sets to 1, but verify it's reasonably low
            assertThat(config.plateCount()).isLessThanOrEqualTo(10);
            assertThat(config.heightScaleMultiplier()).isLessThan(1.0);
        }

        @Test
        @DisplayName("Creates config for small dry planet")
        void createsConfigForSmallDryPlanet() {
            ExoPlanet exo = new ExoPlanet();
            exo.setId(java.util.UUID.randomUUID().toString());
            exo.setMass(0.2);
            exo.setRadius(0.5);
            exo.setHydrosphere(0.0);
            exo.setSurfaceGravity(0.8);

            PlanetConfig config = ProceduralPlanetPersistenceHelper.buildConfigFromExoPlanet(exo, 0L);

            assertThat(config.waterFraction()).isEqualTo(0.0);
            assertThat(config.enableActiveTectonics()).isFalse(); // Too dry
            assertThat(config.plateCount()).isLessThan(8);
        }

        @Test
        @DisplayName("Scales height based on gravity")
        void scalesHeightBasedOnGravity() {
            ExoPlanet lowGravity = createEarthLikeExoPlanet();
            lowGravity.setSurfaceGravity(0.5);

            ExoPlanet highGravity = createEarthLikeExoPlanet();
            highGravity.setSurfaceGravity(2.0);

            PlanetConfig lowConfig = ProceduralPlanetPersistenceHelper.buildConfigFromExoPlanet(lowGravity, 0L);
            PlanetConfig highConfig = ProceduralPlanetPersistenceHelper.buildConfigFromExoPlanet(highGravity, 0L);

            // Lower gravity = higher mountains
            assertThat(lowConfig.heightScaleMultiplier())
                .isGreaterThan(highConfig.heightScaleMultiplier());
        }

        @Test
        @DisplayName("Handles hydrosphere in percentage format (>1)")
        void handlesHydrospherePercentageFormat() {
            ExoPlanet exo = createEarthLikeExoPlanet();
            exo.setHydrosphere(66.0); // Percentage format

            PlanetConfig config = ProceduralPlanetPersistenceHelper.buildConfigFromExoPlanet(exo, 0L);

            assertThat(config.waterFraction()).isBetween(0.6, 0.7);
        }
    }

    @Nested
    @DisplayName("buildConfigFromSnapshots()")
    class BuildConfigFromSnapshotsTests {

        @Test
        @DisplayName("Rebuilds config from stored snapshots")
        void rebuildsConfigFromSnapshots() {
            ExoPlanet exo = createEarthLikeExoPlanet();
            long seed = 42L;

            // Create initial config and store snapshots
            PlanetConfig original = PlanetConfig.builder()
                .seed(seed)
                .size(PlanetConfig.Size.LARGE)
                .plateCount(16)
                .waterFraction(0.7)
                .climateModel(ClimateCalculator.ClimateModel.HADLEY_CELLS)
                .build();

            exo.setProceduralSeed(seed);
            exo.setProceduralAccreteSnapshot(
                ProceduralPlanetPersistenceHelper.createAccreteSnapshot(exo, seed));
            exo.setProceduralOverrides(
                ProceduralPlanetPersistenceHelper.createOverridesSnapshot(original));

            // Rebuild config
            PlanetConfig restored = ProceduralPlanetPersistenceHelper.buildConfigFromSnapshots(exo);

            assertThat(restored).isNotNull();
            assertThat(restored.seed()).isEqualTo(seed);
            assertThat(restored.plateCount()).isEqualTo(16);
            assertThat(restored.waterFraction()).isEqualTo(0.7);
            assertThat(restored.climateModel()).isEqualTo(ClimateCalculator.ClimateModel.HADLEY_CELLS);
        }

        @Test
        @DisplayName("Uses ExoPlanet properties when no snapshot available")
        void usesExoPlanetPropertiesWithoutSnapshot() {
            ExoPlanet exo = createEarthLikeExoPlanet();
            exo.setProceduralSeed(123L);
            // No snapshots set

            PlanetConfig config = ProceduralPlanetPersistenceHelper.buildConfigFromSnapshots(exo);

            assertThat(config).isNotNull();
            assertThat(config.seed()).isEqualTo(123L);
            assertThat(config.waterFraction()).isBetween(0.6, 0.7);
        }

        @Test
        @DisplayName("Derives seed from ID when not set")
        void derivesSeedFromIdWhenNotSet() {
            ExoPlanet exo = createEarthLikeExoPlanet();
            // No seed set

            PlanetConfig config = ProceduralPlanetPersistenceHelper.buildConfigFromSnapshots(exo);

            assertThat(config).isNotNull();
            assertThat(config.seed()).isEqualTo(exo.getId().hashCode());
        }

        @Test
        @DisplayName("Returns null for null ExoPlanet")
        void returnsNullForNullExoPlanet() {
            PlanetConfig config = ProceduralPlanetPersistenceHelper.buildConfigFromSnapshots(null);

            assertThat(config).isNull();
        }

        @Test
        @DisplayName("Applies seasonal override parameters")
        void appliesSeasonalOverrides() {
            ExoPlanet exo = createEarthLikeExoPlanet();
            exo.setProceduralSeed(42L);

            PlanetConfig original = PlanetConfig.builder()
                .seed(42L)
                .climateModel(ClimateCalculator.ClimateModel.SEASONAL)
                .axialTiltDegrees(45.0)
                .seasonalOffsetDegrees(90.0)
                .seasonalSamples(24)
                .build();

            exo.setProceduralOverrides(
                ProceduralPlanetPersistenceHelper.createOverridesSnapshot(original));

            PlanetConfig restored = ProceduralPlanetPersistenceHelper.buildConfigFromSnapshots(exo);

            assertThat(restored.climateModel()).isEqualTo(ClimateCalculator.ClimateModel.SEASONAL);
            assertThat(restored.axialTiltDegrees()).isEqualTo(45.0);
            assertThat(restored.seasonalOffsetDegrees()).isEqualTo(90.0);
            assertThat(restored.seasonalSamples()).isEqualTo(24);
        }
    }

    @Nested
    @DisplayName("Round-trip tests")
    class RoundTripTests {

        @Test
        @DisplayName("Config survives save/restore round-trip")
        void configSurvivesRoundTrip() {
            ExoPlanet exo = createEarthLikeExoPlanet();
            long seed = 999L;

            PlanetConfig original = PlanetConfig.builder()
                .seed(seed)
                .size(PlanetConfig.Size.HUGE)
                .plateCount(18)
                .waterFraction(0.45)
                .oceanicPlateRatio(0.55)
                .heightScaleMultiplier(1.2)
                .riftDepthMultiplier(0.9)
                .hotspotProbability(0.15)
                .enableActiveTectonics(true)
                .erosionIterations(7)
                .rainfallScale(1.5)
                .enableRivers(true)
                .useContinuousHeights(true)
                .continuousReliefMin(-3.5)
                .continuousReliefMax(4.5)
                .rainfallThreshold(0.35)
                .riverSourceThreshold(0.75)
                .riverSourceElevationMin(0.6)
                .erosionCap(0.25)
                .depositionFactor(0.6)
                .riverCarveDepth(0.35)
                .climateModel(ClimateCalculator.ClimateModel.TROPICAL_WORLD)
                .axialTiltDegrees(28.0)
                .seasonalOffsetDegrees(15.0)
                .seasonalSamples(16)
                .build();

            // Save
            exo.setProceduralSeed(seed);
            exo.setProceduralOverrides(
                ProceduralPlanetPersistenceHelper.createOverridesSnapshot(original));

            // Restore
            PlanetConfig restored = ProceduralPlanetPersistenceHelper.buildConfigFromSnapshots(exo);

            // Verify key fields
            assertThat(restored.seed()).isEqualTo(original.seed());
            assertThat(restored.plateCount()).isEqualTo(original.plateCount());
            assertThat(restored.waterFraction()).isEqualTo(original.waterFraction());
            assertThat(restored.oceanicPlateRatio()).isEqualTo(original.oceanicPlateRatio());
            assertThat(restored.enableActiveTectonics()).isEqualTo(original.enableActiveTectonics());
            assertThat(restored.erosionIterations()).isEqualTo(original.erosionIterations());
            assertThat(restored.rainfallScale()).isEqualTo(original.rainfallScale());
            assertThat(restored.enableRivers()).isEqualTo(original.enableRivers());
            assertThat(restored.useContinuousHeights()).isEqualTo(original.useContinuousHeights());
            assertThat(restored.rainfallThreshold()).isEqualTo(original.rainfallThreshold());
            assertThat(restored.climateModel()).isEqualTo(original.climateModel());
            assertThat(restored.axialTiltDegrees()).isEqualTo(original.axialTiltDegrees());
            assertThat(restored.seasonalOffsetDegrees()).isEqualTo(original.seasonalOffsetDegrees());
            assertThat(restored.seasonalSamples()).isEqualTo(original.seasonalSamples());
        }
    }

    // Helper methods

    private ExoPlanet createEarthLikeExoPlanet() {
        ExoPlanet exo = new ExoPlanet();
        exo.setId(java.util.UUID.randomUUID().toString());
        exo.setName("Earth-like Test");
        exo.setMass(1.0);
        exo.setRadius(1.0);
        exo.setSurfaceGravity(1.0);
        exo.setHydrosphere(0.66);
        exo.setSurfaceTemperature(288.0);
        exo.setDayLength(24.0);
        exo.setGasGiant(false);
        return exo;
    }

    private ExoPlanet createGasGiantExoPlanet() {
        ExoPlanet exo = new ExoPlanet();
        exo.setId(java.util.UUID.randomUUID().toString());
        exo.setName("Jupiter-like Test");
        exo.setMass(317.8);
        exo.setRadius(11.2);
        exo.setSurfaceGravity(2.4);
        exo.setHydrosphere(0.0);
        exo.setGasGiant(true);
        return exo;
    }
}
