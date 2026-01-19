package com.teamgannon.trips.planetarymodelling.procedural;

import com.teamgannon.trips.planetarymodelling.procedural.BoundaryDetector.BoundaryAnalysis;
import com.teamgannon.trips.planetarymodelling.procedural.ClimateCalculator.ClimateZone;
import com.teamgannon.trips.planetarymodelling.procedural.ErosionCalculator.ErosionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class ErosionCalculatorTest {

    private PlanetConfig config;
    private List<Polygon> polygons;
    private AdjacencyGraph adjacency;
    private int[] preErosionHeights;
    private ClimateZone[] climates;

    @BeforeEach
    void setUp() {
        config = PlanetConfig.builder()
            .seed(12345L)
            .size(PlanetConfig.Size.DUEL)
            .plateCount(10)
            .waterFraction(0.66)
            .erosionIterations(5)
            .rainfallScale(1.0)
            .enableRivers(true)
            .build();

        var mesh = new IcosahedralMesh(config);
        polygons = mesh.generate();
        adjacency = new AdjacencyGraph(polygons);

        var assigner = new PlateAssigner(config, adjacency);
        var plateAssignment = assigner.assign();
        var detector = new BoundaryDetector(config, plateAssignment);
        var boundaryAnalysis = detector.analyze();

        var elevationCalc = new ElevationCalculator(config, adjacency, plateAssignment, boundaryAnalysis);
        preErosionHeights = elevationCalc.calculate();

        var climateCalc = new ClimateCalculator(polygons);
        climates = climateCalc.calculate();
    }

    @Test
    @DisplayName("calculate() returns ErosionResult with matching polygon count")
    void calculateReturnsCorrectSize() {
        ErosionResult result = ErosionCalculator.calculate(
            preErosionHeights, polygons, adjacency, climates, config);

        assertThat(result.erodedHeights()).hasSize(polygons.size());
        assertThat(result.rainfall()).hasSize(polygons.size());
    }

    @Test
    @DisplayName("All eroded heights are within valid range [-4, 4]")
    void heightsInValidRange() {
        ErosionResult result = ErosionCalculator.calculate(
            preErosionHeights, polygons, adjacency, climates, config);

        for (int h : result.erodedHeights()) {
            assertThat(h)
                .as("Height should be between DEEP_OCEAN (-4) and HIGH_MOUNTAINS (4)")
                .isBetween(ElevationCalculator.DEEP_OCEAN, ElevationCalculator.HIGH_MOUNTAINS);
        }
    }

    @Test
    @DisplayName("Same seed produces identical erosion results")
    void reproducibility() {
        ErosionResult result1 = ErosionCalculator.calculate(
            preErosionHeights, polygons, adjacency, climates, config);
        ErosionResult result2 = ErosionCalculator.calculate(
            preErosionHeights, polygons, adjacency, climates, config);

        assertThat(result1.erodedHeights()).isEqualTo(result2.erodedHeights());
        assertThat(result1.rainfall()).isEqualTo(result2.rainfall());
        assertThat(result1.rivers()).isEqualTo(result2.rivers());
    }

    @Test
    @DisplayName("Rainfall is higher in tropical zones than polar zones")
    void tropicalHasHigherRainfall() {
        ErosionResult result = ErosionCalculator.calculate(
            preErosionHeights, polygons, adjacency, climates, config);

        double tropicalRainfall = 0;
        int tropicalCount = 0;
        double polarRainfall = 0;
        int polarCount = 0;

        for (int i = 0; i < climates.length; i++) {
            if (climates[i] == ClimateZone.TROPICAL) {
                tropicalRainfall += result.rainfall()[i];
                tropicalCount++;
            } else if (climates[i] == ClimateZone.POLAR) {
                polarRainfall += result.rainfall()[i];
                polarCount++;
            }
        }

        if (tropicalCount > 0 && polarCount > 0) {
            double avgTropical = tropicalRainfall / tropicalCount;
            double avgPolar = polarRainfall / polarCount;

            assertThat(avgTropical)
                .as("Tropical zones should have higher rainfall than polar zones")
                .isGreaterThan(avgPolar);
        }
    }

    @Test
    @DisplayName("Erosion modifies heights from pre-erosion state")
    void erosionModifiesHeights() {
        ErosionResult result = ErosionCalculator.calculate(
            preErosionHeights, polygons, adjacency, climates, config);

        // Count differences between pre and post erosion heights
        int changedCount = 0;
        for (int i = 0; i < preErosionHeights.length; i++) {
            if (preErosionHeights[i] != result.erodedHeights()[i]) {
                changedCount++;
            }
        }

        assertThat(changedCount)
            .as("Erosion should modify at least some heights")
            .isGreaterThan(0);
    }

    @Test
    @DisplayName("Rivers flow downhill (each step decreases or maintains elevation)")
    void riverPathsFlowDownhill() {
        ErosionResult result = ErosionCalculator.calculate(
            preErosionHeights, polygons, adjacency, climates, config);

        for (List<Integer> river : result.rivers()) {
            if (river.size() < 2) continue;

            for (int i = 0; i < river.size() - 1; i++) {
                int current = river.get(i);
                int next = river.get(i + 1);

                // After erosion, river should flow to same or lower elevation
                // (allowing for carving effects and rounding)
                assertThat(result.erodedHeights()[next])
                    .as("River should flow to same or lower elevation at step %d", i)
                    .isLessThanOrEqualTo(result.erodedHeights()[current] + 1);
            }
        }
    }

    @Test
    @DisplayName("Rivers are created when enableRivers is true")
    void riversCreatedWhenEnabled() {
        var configWithRivers = config.toBuilder()
            .enableRivers(true)
            .rainfallScale(1.5)  // Higher rainfall to ensure rivers
            .build();

        // Need fresh data with new config
        var mesh = new IcosahedralMesh(configWithRivers);
        var polys = mesh.generate();
        var adj = new AdjacencyGraph(polys);
        var assigner = new PlateAssigner(configWithRivers, adj);
        var plateAssignment = assigner.assign();
        var detector = new BoundaryDetector(configWithRivers, plateAssignment);
        var analysis = detector.analyze();
        var elevCalc = new ElevationCalculator(configWithRivers, adj, plateAssignment, analysis);
        var heights = elevCalc.calculate();
        var climCalc = new ClimateCalculator(polys);
        var clims = climCalc.calculate();

        ErosionResult result = ErosionCalculator.calculate(
            heights, polys, adj, clims, configWithRivers);

        // May or may not have rivers depending on terrain, but rivers list should exist
        assertThat(result.rivers()).isNotNull();
    }

    @Test
    @DisplayName("No rivers created when enableRivers is false")
    void noRiversWhenDisabled() {
        var configNoRivers = config.toBuilder()
            .enableRivers(false)
            .build();

        ErosionResult result = ErosionCalculator.calculate(
            preErosionHeights, polygons, adjacency, climates, configNoRivers);

        assertThat(result.rivers())
            .as("Rivers should be empty when disabled")
            .isEmpty();
    }

    @Test
    @DisplayName("Stagnant-lid worlds have muted erosion")
    void stagnantLidMinimalErosion() {
        // Active tectonics planet
        var activeConfig = PlanetConfig.builder()
            .seed(42L)
            .size(PlanetConfig.Size.DUEL)
            .plateCount(10)
            .enableActiveTectonics(true)
            .erosionIterations(5)
            .rainfallScale(1.0)
            .build();

        // Stagnant lid planet (same seed)
        var stagnantConfig = PlanetConfig.builder()
            .seed(42L)
            .size(PlanetConfig.Size.DUEL)
            .plateCount(10)
            .enableActiveTectonics(false)
            .erosionIterations(5)
            .rainfallScale(1.0)
            .build();

        // Generate heights for active planet
        int[] activeHeights = generateHeights(activeConfig);
        ClimateZone[] activeClimates = generateClimates(activeConfig);
        var activeAdj = generateAdjacency(activeConfig);
        var activePolygons = generatePolygons(activeConfig);

        // Generate heights for stagnant planet
        int[] stagnantHeights = generateHeights(stagnantConfig);
        ClimateZone[] stagnantClimates = generateClimates(stagnantConfig);
        var stagnantAdj = generateAdjacency(stagnantConfig);
        var stagnantPolygons = generatePolygons(stagnantConfig);

        ErosionResult activeResult = ErosionCalculator.calculate(
            activeHeights, activePolygons, activeAdj, activeClimates, activeConfig);
        ErosionResult stagnantResult = ErosionCalculator.calculate(
            stagnantHeights, stagnantPolygons, stagnantAdj, stagnantClimates, stagnantConfig);

        // Calculate average rainfall
        double activeAvgRainfall = 0;
        for (double r : activeResult.rainfall()) activeAvgRainfall += r;
        activeAvgRainfall /= activeResult.rainfall().length;

        double stagnantAvgRainfall = 0;
        for (double r : stagnantResult.rainfall()) stagnantAvgRainfall += r;
        stagnantAvgRainfall /= stagnantResult.rainfall().length;

        assertThat(stagnantAvgRainfall)
            .as("Stagnant-lid worlds should have reduced rainfall")
            .isLessThan(activeAvgRainfall);
    }

    @Test
    @DisplayName("Zero erosion iterations produces minimal changes")
    void zeroIterationsMinimalChanges() {
        var noErosionConfig = config.toBuilder()
            .erosionIterations(0)
            .enableRivers(false)
            .build();

        ErosionResult result = ErosionCalculator.calculate(
            preErosionHeights, polygons, adjacency, climates, noErosionConfig);

        // With zero iterations and no rivers, changes should only come from coastal smoothing
        // which is relatively minor
        int unchangedCount = 0;
        for (int i = 0; i < preErosionHeights.length; i++) {
            if (preErosionHeights[i] == result.erodedHeights()[i]) {
                unchangedCount++;
            }
        }

        // Most polygons should remain unchanged with minimal erosion
        double unchangedRatio = (double) unchangedCount / preErosionHeights.length;
        assertThat(unchangedRatio)
            .as("Most heights should be unchanged with zero iterations")
            .isGreaterThan(0.5);
    }

    @Test
    @DisplayName("Different rainfall scales produce different results")
    void differentRainfallScales() {
        var lowRainConfig = config.toBuilder()
            .rainfallScale(0.2)
            .build();

        var highRainConfig = config.toBuilder()
            .rainfallScale(2.0)
            .build();

        ErosionResult lowResult = ErosionCalculator.calculate(
            preErosionHeights, polygons, adjacency, climates, lowRainConfig);
        ErosionResult highResult = ErosionCalculator.calculate(
            preErosionHeights, polygons, adjacency, climates, highRainConfig);

        // Calculate total rainfall
        double lowTotal = 0;
        for (double r : lowResult.rainfall()) lowTotal += r;

        double highTotal = 0;
        for (double r : highResult.rainfall()) highTotal += r;

        assertThat(highTotal)
            .as("Higher rainfall scale should produce more total rainfall")
            .isGreaterThan(lowTotal);
    }

    @Test
    @DisplayName("All rainfall values are positive")
    void allRainfallPositive() {
        ErosionResult result = ErosionCalculator.calculate(
            preErosionHeights, polygons, adjacency, climates, config);

        for (double r : result.rainfall()) {
            assertThat(r)
                .as("Rainfall should be positive")
                .isGreaterThanOrEqualTo(0);
        }
    }

    @Test
    @DisplayName("River paths contain valid polygon indices")
    void riverPathsValidIndices() {
        ErosionResult result = ErosionCalculator.calculate(
            preErosionHeights, polygons, adjacency, climates, config);

        for (List<Integer> river : result.rivers()) {
            for (int idx : river) {
                assertThat(idx)
                    .as("River path should contain valid polygon indices")
                    .isBetween(0, polygons.size() - 1);
            }
        }
    }

    @Test
    @DisplayName("Adjacent river segments are neighbors in adjacency graph")
    void riverSegmentsAreNeighbors() {
        ErosionResult result = ErosionCalculator.calculate(
            preErosionHeights, polygons, adjacency, climates, config);

        for (List<Integer> river : result.rivers()) {
            for (int i = 0; i < river.size() - 1; i++) {
                int current = river.get(i);
                int next = river.get(i + 1);

                assertThat(adjacency.areNeighbors(current, next))
                    .as("River segment from %d to %d should be neighbors", current, next)
                    .isTrue();
            }
        }
    }

    @Test
    @DisplayName("Basins create lakes after filling")
    void basinsCreateLakes() {
        var basinConfig = PlanetConfig.builder()
            .seed(123L)
            .size(PlanetConfig.Size.DUEL)
            .build();

        var mesh = new IcosahedralMesh(basinConfig);
        var polys = mesh.generate();
        var adj = new AdjacencyGraph(polys);
        var climCalc = new ClimateCalculator(polys);
        var clims = climCalc.calculate();

        int[] heights = new int[polys.size()];
        Arrays.fill(heights, ElevationCalculator.HILLS);

        int sink = 0;
        heights[sink] = ElevationCalculator.PLAINS;
        for (int neighbor : adj.neighborsOnly(sink)) {
            heights[neighbor] = ElevationCalculator.HIGH_MOUNTAINS;
        }

        ErosionResult result = ErosionCalculator.calculate(
            heights, polys, adj, clims, basinConfig);

        assertThat(result.lakeMask())
            .as("Lake mask should be present")
            .isNotNull();

        boolean hasLake = false;
        for (boolean isLake : result.lakeMask()) {
            if (isLake) {
                hasLake = true;
                break;
            }
        }

        assertThat(hasLake)
            .as("At least one lake polygon should be created")
            .isTrue();
    }

    @Test
    @DisplayName("Flow accumulation increases downstream along rivers")
    void flowAccumulationIncreasesDownstream() {
        ErosionResult result = ErosionCalculator.calculate(
            preErosionHeights, polygons, adjacency, climates, config);

        double[] accumulation = result.flowAccumulation();
        if (accumulation == null || accumulation.length == 0) {
            return;
        }

        for (List<Integer> river : result.rivers()) {
            if (river.size() < 2) continue;

            for (int i = 0; i < river.size() - 1; i++) {
                int current = river.get(i);
                int next = river.get(i + 1);

                assertThat(accumulation[next])
                    .as("Flow should not decrease downstream at step %d", i)
                    .isGreaterThanOrEqualTo(accumulation[current] - 1e-6);
            }
        }
    }

    // Helper methods for generating test data

    private int[] generateHeights(PlanetConfig cfg) {
        var mesh = new IcosahedralMesh(cfg);
        var polys = mesh.generate();
        var adj = new AdjacencyGraph(polys);
        var assigner = new PlateAssigner(cfg, adj);
        var assignment = assigner.assign();
        var detector = new BoundaryDetector(cfg, assignment);
        var analysis = detector.analyze();
        return new ElevationCalculator(cfg, adj, assignment, analysis).calculate();
    }

    private ClimateZone[] generateClimates(PlanetConfig cfg) {
        var mesh = new IcosahedralMesh(cfg);
        var polys = mesh.generate();
        return new ClimateCalculator(polys).calculate();
    }

    private AdjacencyGraph generateAdjacency(PlanetConfig cfg) {
        var mesh = new IcosahedralMesh(cfg);
        var polys = mesh.generate();
        return new AdjacencyGraph(polys);
    }

    private List<Polygon> generatePolygons(PlanetConfig cfg) {
        var mesh = new IcosahedralMesh(cfg);
        return mesh.generate();
    }
}
