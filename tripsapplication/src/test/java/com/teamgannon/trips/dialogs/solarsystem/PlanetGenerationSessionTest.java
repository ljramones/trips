package com.teamgannon.trips.dialogs.solarsystem;

import com.teamgannon.trips.planetary.modelling.procedural.ClimateCalculator;
import com.teamgannon.trips.planetary.modelling.procedural.ErosionCalculator;
import com.teamgannon.trips.planetary.modelling.procedural.JavaFxPlanetMeshConverter.TerrainType;
import com.teamgannon.trips.planetary.modelling.procedural.PlanetConfig;
import com.teamgannon.trips.planetary.modelling.procedural.PlanetGenerator.GeneratedPlanet;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PlanetGenerationSessionTest {

    @Test
    void initializesFromPlanetConfigAndDerivedData() {
        PlanetConfig config = PlanetConfig.builder()
                .seed(42L)
                .size(PlanetConfig.Size.LARGE)
                .plateCount(18)
                .waterFraction(0.72)
                .erosionIterations(8)
                .riverSourceThreshold(0.62)
                .heightScaleMultiplier(1.7)
                .useContinuousHeights(true)
                .continuousReliefMin(-3.5)
                .continuousReliefMax(5.2)
                .axialTiltDegrees(31.0)
                .seasonalOffsetDegrees(120.0)
                .climateModel(ClimateCalculator.ClimateModel.SEASONAL)
                .build();

        PlanetGenerationSession session = PlanetGenerationSession.from(planet(config,
                erosion(new double[]{0.1, 0.8}, new double[]{-0.2, 0.5}, new double[]{4.0}, new boolean[]{false, true})));

        assertThat(session.seed()).isEqualTo(42L);
        assertThat(session.size()).isEqualTo(PlanetConfig.Size.LARGE);
        assertThat(session.plateCount()).isEqualTo(18);
        assertThat(session.waterFraction()).isEqualTo(0.72);
        assertThat(session.erosionIterations()).isEqualTo(8);
        assertThat(session.riverThreshold()).isEqualTo(0.62);
        assertThat(session.heightScale()).isEqualTo(1.7);
        assertThat(session.useContinuousHeights()).isTrue();
        assertThat(session.reliefMin()).isEqualTo(-3.5);
        assertThat(session.reliefMax()).isEqualTo(5.2);
        assertThat(session.axialTilt()).isEqualTo(31.0);
        assertThat(session.seasonalOffset()).isEqualTo(120.0);
        assertThat(session.climateModel()).isEqualTo(ClimateCalculator.ClimateModel.SEASONAL);
        assertThat(session.hasRainfall()).isTrue();
        assertThat(session.hasPreciseHeights()).isTrue();
        assertThat(session.hasFlowAccumulation()).isTrue();
        assertThat(session.hasLakes()).isTrue();
    }

    @Test
    void capturesControlValuesAndBuildsGenerationConfig() {
        PlanetGenerationSession session = PlanetGenerationSession.from(planet(PlanetConfig.builder().build(),
                erosion(new double[0], new double[0], new double[0], null)));

        session.captureGenerationControls(
                99L,
                20,
                0.41,
                3,
                0.83,
                2.4,
                true,
                -2.0,
                4.0,
                11.5,
                270.0,
                PlanetConfig.Size.SMALL,
                ClimateCalculator.ClimateModel.HADLEY_CELLS);

        PlanetConfig config = session.buildConfig();

        assertThat(config.seed()).isEqualTo(99L);
        assertThat(config.n()).isEqualTo(PlanetConfig.Size.SMALL.n);
        assertThat(config.plateCount()).isEqualTo(20);
        assertThat(config.waterFraction()).isEqualTo(0.41);
        assertThat(config.erosionIterations()).isEqualTo(3);
        assertThat(config.riverSourceThreshold()).isEqualTo(0.83);
        assertThat(config.heightScaleMultiplier()).isEqualTo(2.4);
        assertThat(config.useContinuousHeights()).isTrue();
        assertThat(config.continuousReliefMin()).isEqualTo(-2.0);
        assertThat(config.continuousReliefMax()).isEqualTo(4.0);
        assertThat(config.axialTiltDegrees()).isEqualTo(11.5);
        assertThat(config.seasonalOffsetDegrees()).isEqualTo(270.0);
        assertThat(config.climateModel()).isEqualTo(ClimateCalculator.ClimateModel.HADLEY_CELLS);
    }

    @Test
    void classifiesTerrainFromPlanetPhysicalContext() {
        PlanetGenerationSession session = PlanetGenerationSession.from(planet(
                PlanetConfig.builder().waterFraction(0.0).build(),
                erosion(new double[0], new double[0], new double[0], null)));

        assertThat(session.determineTerrainType()).isEqualTo(TerrainType.DRY);

        session.setPlanetType("Gas Giant");
        assertThat(session.determineTerrainType()).isEqualTo(TerrainType.JOVIAN);

        session.setPlanetType(null);
        session.setIceCover(0.6);
        assertThat(session.determineTerrainType()).isEqualTo(TerrainType.ICE);
    }

    @Test
    void applyPlanetRefreshesDerivedAvailability() {
        PlanetGenerationSession session = PlanetGenerationSession.from(planet(PlanetConfig.builder().build(),
                erosion(new double[0], new double[0], new double[0], null)));
        assertThat(session.hasLakes()).isFalse();
        assertThat(session.hasRainfall()).isFalse();

        GeneratedPlanet replacement = planet(PlanetConfig.builder().waterFraction(0.25).build(),
                erosion(new double[]{0.2}, new double[]{0.4}, new double[]{7.0}, new boolean[]{true}));
        session.applyPlanet(replacement);

        assertThat(session.planet()).isSameAs(replacement);
        assertThat(session.waterFractionForRendering()).isEqualTo(0.25);
        assertThat(session.hasLakes()).isTrue();
        assertThat(session.hasRainfall()).isTrue();
        assertThat(session.hasPreciseHeights()).isTrue();
        assertThat(session.hasFlowAccumulation()).isTrue();
    }

    private static GeneratedPlanet planet(PlanetConfig config, ErosionCalculator.ErosionResult erosionResult) {
        return new GeneratedPlanet(
                config,
                List.of(),
                new int[0],
                new double[0],
                new ClimateCalculator.ClimateZone[0],
                null,
                null,
                erosionResult,
                null);
    }

    private static ErosionCalculator.ErosionResult erosion(double[] rainfall,
                                                           double[] preciseHeights,
                                                           double[] flowAccumulation,
                                                           boolean[] lakeMask) {
        return new ErosionCalculator.ErosionResult(
                new int[0],
                preciseHeights,
                List.of(),
                rainfall,
                new boolean[0],
                flowAccumulation,
                lakeMask);
    }
}
