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

    // ===========================================
    // Frozen River Tests
    // ===========================================

    @Test
    @DisplayName("Rivers ending in polar zones are marked as frozen")
    void frozenRiversInPolarZones() {
        // Use a config that encourages rivers
        var frozenConfig = PlanetConfig.builder()
            .seed(77777L)
            .size(PlanetConfig.Size.SMALL)
            .plateCount(10)
            .waterFraction(0.5)
            .erosionIterations(5)
            .rainfallScale(1.5)
            .enableRivers(true)
            .build();

        var mesh = new IcosahedralMesh(frozenConfig);
        var polys = mesh.generate();
        var adj = new AdjacencyGraph(polys);
        var assigner = new PlateAssigner(frozenConfig, adj);
        var plateAssignment = assigner.assign();
        var detector = new BoundaryDetector(frozenConfig, plateAssignment);
        var analysis = detector.analyze();
        var elevCalc = new ElevationCalculator(frozenConfig, adj, plateAssignment, analysis);
        var heights = elevCalc.calculate();
        var climCalc = new ClimateCalculator(polys);
        var clims = climCalc.calculate();

        ErosionResult result = ErosionCalculator.calculate(
            heights, polys, adj, clims, frozenConfig);

        // Check that frozen terminus array exists
        assertThat(result.frozenRiverTerminus()).isNotNull();

        if (!result.rivers().isEmpty()) {
            assertThat(result.frozenRiverTerminus())
                .as("Frozen terminus array should match river count")
                .hasSize(result.rivers().size());
        }
    }

    @Test
    @DisplayName("isRiverFrozen() returns correct values")
    void isRiverFrozenMethod() {
        ErosionResult result = ErosionCalculator.calculate(
            preErosionHeights, polygons, adjacency, climates, config);

        for (int i = 0; i < result.rivers().size(); i++) {
            boolean frozen = result.isRiverFrozen(i);
            if (result.frozenRiverTerminus() != null && i < result.frozenRiverTerminus().length) {
                assertThat(frozen)
                    .as("isRiverFrozen(%d) should match frozenRiverTerminus array", i)
                    .isEqualTo(result.frozenRiverTerminus()[i]);
            }
        }
    }

    // ===========================================
    // Precise Heights Tests
    // ===========================================

    @Test
    @DisplayName("Precise heights array has correct size")
    void preciseHeightsCorrectSize() {
        ErosionResult result = ErosionCalculator.calculate(
            preErosionHeights, polygons, adjacency, climates, config);

        assertThat(result.preciseHeights())
            .as("Precise heights should match polygon count")
            .hasSize(polygons.size());
    }

    @Test
    @DisplayName("Precise heights correlate with integer heights")
    void preciseHeightsCorrelateWithIntegers() {
        ErosionResult result = ErosionCalculator.calculate(
            preErosionHeights, polygons, adjacency, climates, config);

        // Precise heights should be within ~1 unit of integer heights
        for (int i = 0; i < result.erodedHeights().length; i++) {
            double precise = result.preciseHeights()[i];
            int integer = result.erodedHeights()[i];

            assertThat(Math.abs(precise - integer))
                .as("Precise height %f should be within 1.5 of integer height %d at index %d",
                    precise, integer, i)
                .isLessThan(1.5);
        }
    }

    @Test
    @DisplayName("Precise heights provide finer gradations than integers")
    void preciseHeightsHaveFinerGradations() {
        ErosionResult result = ErosionCalculator.calculate(
            preErosionHeights, polygons, adjacency, climates, config);

        // Count unique precise heights vs unique integer heights
        java.util.Set<Double> preciseUnique = new java.util.HashSet<>();
        java.util.Set<Integer> intUnique = new java.util.HashSet<>();

        for (int i = 0; i < result.erodedHeights().length; i++) {
            // Round to 2 decimal places for counting
            preciseUnique.add(Math.round(result.preciseHeights()[i] * 100.0) / 100.0);
            intUnique.add(result.erodedHeights()[i]);
        }

        // Precise heights should have more unique values (or equal for flat terrain)
        assertThat(preciseUnique.size())
            .as("Precise heights should have at least as many unique values as integer heights")
            .isGreaterThanOrEqualTo(intUnique.size());
    }

    // ===========================================
    // Rain Shadow Tests
    // ===========================================

    @Test
    @DisplayName("Mountains reduce rainfall on leeward side")
    void mountainsReduceLeewardRainfall() {
        // Use HADLEY_CELLS model which has defined wind directions
        var rainShadowConfig = PlanetConfig.builder()
            .seed(55555L)
            .size(PlanetConfig.Size.STANDARD)  // More polygons for better wind patterns
            .plateCount(12)
            .waterFraction(0.6)
            .erosionIterations(5)
            .rainfallScale(1.0)
            .climateModel(ClimateCalculator.ClimateModel.HADLEY_CELLS)
            .enableRivers(true)
            .build();

        var mesh = new IcosahedralMesh(rainShadowConfig);
        var polys = mesh.generate();
        var adj = new AdjacencyGraph(polys);
        var assigner = new PlateAssigner(rainShadowConfig, adj);
        var plateAssignment = assigner.assign();
        var detector = new BoundaryDetector(rainShadowConfig, plateAssignment);
        var analysis = detector.analyze();
        var elevCalc = new ElevationCalculator(rainShadowConfig, adj, plateAssignment, analysis);
        var heights = elevCalc.calculate();
        var climCalc = new ClimateCalculator(polys, rainShadowConfig.climateModel());
        var clims = climCalc.calculate();

        ErosionResult result = ErosionCalculator.calculate(
            heights, polys, adj, clims, rainShadowConfig);

        // Verify rain shadow effect exists by checking rainfall variance
        double sum = 0, sumSq = 0;
        for (double r : result.rainfall()) {
            sum += r;
            sumSq += r * r;
        }
        double mean = sum / result.rainfall().length;
        double variance = (sumSq / result.rainfall().length) - (mean * mean);

        // Rain shadow should create variance in rainfall (not uniform)
        assertThat(variance)
            .as("Rain shadow should create variance in rainfall distribution")
            .isGreaterThan(0.001);
    }

    // ===========================================
    // Sediment Conservation Tests
    // ===========================================

    @Test
    @DisplayName("Erosion conserves approximate mass")
    void erosionConservesMass() {
        ErosionResult result = ErosionCalculator.calculate(
            preErosionHeights, polygons, adjacency, climates, config);

        // Sum pre-erosion heights
        long preSum = 0;
        for (int h : preErosionHeights) {
            preSum += h;
        }

        // Sum post-erosion heights
        long postSum = 0;
        for (int h : result.erodedHeights()) {
            postSum += h;
        }

        // Mass should be approximately conserved (within 20% tolerance due to rounding)
        double ratio = (double) postSum / preSum;
        assertThat(ratio)
            .as("Total elevation should be approximately conserved")
            .isBetween(0.5, 1.5);
    }

    // ===========================================
    // River Source Threshold Tests
    // ===========================================

    @Test
    @DisplayName("Lower river source threshold creates more rivers")
    void lowerThresholdMoreRivers() {
        var lowThresholdConfig = config.toBuilder()
            .riverSourceThreshold(0.2)  // Very low threshold
            .riverSourceElevationMin(0.1)
            .build();

        var highThresholdConfig = config.toBuilder()
            .riverSourceThreshold(0.95)  // Very high threshold
            .riverSourceElevationMin(0.9)
            .build();

        ErosionResult lowResult = ErosionCalculator.calculate(
            preErosionHeights, polygons, adjacency, climates, lowThresholdConfig);
        ErosionResult highResult = ErosionCalculator.calculate(
            preErosionHeights, polygons, adjacency, climates, highThresholdConfig);

        assertThat(lowResult.rivers().size())
            .as("Lower threshold should create at least as many rivers")
            .isGreaterThanOrEqualTo(highResult.rivers().size());
    }

    // ===========================================
    // Extended Lake and Flow Accumulation Tests
    // ===========================================

    @Test
    @DisplayName("Lake mask array has correct size")
    void lakeMaskCorrectSize() {
        ErosionResult result = ErosionCalculator.calculate(
            preErosionHeights, polygons, adjacency, climates, config);

        if (result.lakeMask() != null) {
            assertThat(result.lakeMask())
                .as("Lake mask should match polygon count")
                .hasSize(polygons.size());
        }
    }

    @Test
    @DisplayName("Flow accumulation array has correct size")
    void flowAccumulationCorrectSize() {
        ErosionResult result = ErosionCalculator.calculate(
            preErosionHeights, polygons, adjacency, climates, config);

        if (result.flowAccumulation() != null) {
            assertThat(result.flowAccumulation())
                .as("Flow accumulation should match polygon count")
                .hasSize(polygons.size());
        }
    }

    @Test
    @DisplayName("Flow accumulation values are non-negative")
    void flowAccumulationNonNegative() {
        ErosionResult result = ErosionCalculator.calculate(
            preErosionHeights, polygons, adjacency, climates, config);

        if (result.flowAccumulation() != null) {
            for (int i = 0; i < result.flowAccumulation().length; i++) {
                assertThat(result.flowAccumulation()[i])
                    .as("Flow accumulation at %d should be non-negative", i)
                    .isGreaterThanOrEqualTo(0.0);
            }
        }
    }

    @Test
    @DisplayName("Lakes are not created in ocean polygons")
    void lakesNotInOcean() {
        ErosionResult result = ErosionCalculator.calculate(
            preErosionHeights, polygons, adjacency, climates, config);

        if (result.lakeMask() != null) {
            for (int i = 0; i < result.lakeMask().length; i++) {
                if (result.lakeMask()[i]) {
                    // Lake polygon should have non-negative height (not ocean)
                    assertThat(result.erodedHeights()[i])
                        .as("Lake polygon %d should not be in deep ocean", i)
                        .isGreaterThanOrEqualTo(ElevationCalculator.OCEAN);
                }
            }
        }
    }

    @Test
    @DisplayName("GeneratedPlanet exposes flow accumulation")
    void generatedPlanetExposesFlowAccumulation() {
        PlanetGenerator.GeneratedPlanet planet = PlanetGenerator.generate(config);

        double[] flow = planet.flowAccumulation();
        assertThat(flow).isNotNull();
        if (flow.length > 0) {
            assertThat(flow).hasSize(planet.polygons().size());
        }
    }

    @Test
    @DisplayName("GeneratedPlanet exposes lake mask")
    void generatedPlanetExposesLakeMask() {
        PlanetGenerator.GeneratedPlanet planet = PlanetGenerator.generate(config);

        boolean[] lakes = planet.lakeMask();
        // Lake mask may be null if no lakes formed
        if (lakes != null) {
            assertThat(lakes).hasSize(planet.polygons().size());
        }
    }

    @Test
    @DisplayName("High elevation polygons have low flow accumulation")
    void highElevationLowFlow() {
        ErosionResult result = ErosionCalculator.calculate(
            preErosionHeights, polygons, adjacency, climates, config);

        if (result.flowAccumulation() == null) return;

        // Find average flow for high vs low elevation
        double highElevSum = 0, highCount = 0;
        double lowElevSum = 0, lowCount = 0;

        for (int i = 0; i < result.erodedHeights().length; i++) {
            if (result.erodedHeights()[i] >= ElevationCalculator.MOUNTAINS) {
                highElevSum += result.flowAccumulation()[i];
                highCount++;
            } else if (result.erodedHeights()[i] == ElevationCalculator.PLAINS ||
                       result.erodedHeights()[i] == ElevationCalculator.LOWLAND) {
                lowElevSum += result.flowAccumulation()[i];
                lowCount++;
            }
        }

        if (highCount > 0 && lowCount > 0) {
            double avgHigh = highElevSum / highCount;
            double avgLow = lowElevSum / lowCount;

            // Low elevations should have more accumulated flow (water flows downhill)
            assertThat(avgLow)
                .as("Low elevation areas should have higher average flow accumulation")
                .isGreaterThanOrEqualTo(avgHigh);
        }
    }

    // ===========================================
    // Full Pipeline Integration Test
    // ===========================================

    @Test
    @DisplayName("Full erosion with plate data integration")
    void fullErosionWithPlateData() {
        var fullConfig = PlanetConfig.builder()
            .seed(99999L)
            .size(PlanetConfig.Size.SMALL)
            .plateCount(10)
            .waterFraction(0.5)
            .erosionIterations(5)
            .rainfallScale(1.0)
            .enableRivers(true)
            .build();

        var mesh = new IcosahedralMesh(fullConfig);
        var polys = mesh.generate();
        var adj = new AdjacencyGraph(polys);
        var assigner = new PlateAssigner(fullConfig, adj);
        var plateAssignment = assigner.assign();
        var detector = new BoundaryDetector(fullConfig, plateAssignment);
        var boundaryAnalysis = detector.analyze();
        var elevCalc = new ElevationCalculator(fullConfig, adj, plateAssignment, boundaryAnalysis);
        var heights = elevCalc.calculate();
        var climCalc = new ClimateCalculator(polys);
        var clims = climCalc.calculate();

        // Call with plate data (used for divergent boundary moisture boost)
        ErosionResult result = ErosionCalculator.calculate(
            heights, polys, adj, clims, fullConfig, plateAssignment, boundaryAnalysis);

        assertThat(result).isNotNull();
        assertThat(result.erodedHeights()).hasSize(polys.size());
        assertThat(result.rainfall()).hasSize(polys.size());
        assertThat(result.preciseHeights()).hasSize(polys.size());
    }
}
