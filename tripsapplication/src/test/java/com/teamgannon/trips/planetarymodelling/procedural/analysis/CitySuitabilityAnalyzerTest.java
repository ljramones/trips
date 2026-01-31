package com.teamgannon.trips.planetarymodelling.procedural.analysis;

import com.teamgannon.trips.planetarymodelling.procedural.PlanetConfig;
import com.teamgannon.trips.planetarymodelling.procedural.PlanetGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for CitySuitabilityAnalyzer.
 */
class CitySuitabilityAnalyzerTest {

    private PlanetGenerator.GeneratedPlanet planet;

    @BeforeEach
    void setUp() {
        // Generate a small test planet
        PlanetConfig config = PlanetConfig.builder()
            .seed(42L)
            .size(PlanetConfig.Size.DUEL)  // Smallest for fast tests
            .build();

        planet = PlanetGenerator.generate(config);
    }

    @Test
    void testAnalyzeReturnsCorrectSize() {
        double[] suitability = CitySuitabilityAnalyzer.analyze(planet);

        assertEquals(planet.heights().length, suitability.length,
            "Suitability array should match polygon count");
    }

    @Test
    void testWaterPolygonsHaveZeroSuitability() {
        double[] suitability = CitySuitabilityAnalyzer.analyze(planet);

        int[] heights = planet.heights();
        for (int i = 0; i < heights.length; i++) {
            if (heights[i] < 0) {
                assertEquals(0.0, suitability[i], 0.001,
                    "Water polygon " + i + " should have zero suitability");
            }
        }
    }

    @Test
    void testLandPolygonsHavePositiveSuitability() {
        double[] suitability = CitySuitabilityAnalyzer.analyze(planet);

        int[] heights = planet.heights();
        int landWithPositive = 0;
        int landCount = 0;

        for (int i = 0; i < heights.length; i++) {
            if (heights[i] >= 0) {
                landCount++;
                if (suitability[i] > 0) {
                    landWithPositive++;
                }
            }
        }

        assertTrue(landWithPositive > 0,
            "At least some land polygons should have positive suitability");
    }

    @Test
    void testSuitabilityInRange() {
        double[] suitability = CitySuitabilityAnalyzer.analyze(planet);

        for (int i = 0; i < suitability.length; i++) {
            assertTrue(suitability[i] >= 0.0 && suitability[i] <= 1.0,
                "Suitability should be in [0,1] range at polygon " + i);
        }
    }

    @Test
    void testAnalyzeWithStatistics() {
        CitySuitabilityAnalyzer analyzer = new CitySuitabilityAnalyzer(planet);
        CitySuitabilityAnalyzer.SuitabilityResult result = analyzer.analyzeWithStatistics(10);

        assertNotNull(result.scores());
        assertEquals(planet.heights().length, result.scores().length);

        // Best locations should be valid indices
        for (int location : result.bestLocations()) {
            assertTrue(location >= 0 && location < planet.heights().length,
                "Best location should be valid polygon index");
        }

        // Statistics should be reasonable
        assertTrue(result.maxSuitability() >= 0.0 && result.maxSuitability() <= 1.0);
        assertTrue(result.averageSuitability() >= 0.0 && result.averageSuitability() <= 1.0);
        assertTrue(result.averageSuitability() <= result.maxSuitability());
    }

    @Test
    void testBestLocationsAreSorted() {
        CitySuitabilityAnalyzer analyzer = new CitySuitabilityAnalyzer(planet);
        CitySuitabilityAnalyzer.SuitabilityResult result = analyzer.analyzeWithStatistics(5);

        double[] scores = result.scores();
        var bestLocations = result.bestLocations();

        // Best locations should be in descending order of suitability
        for (int i = 1; i < bestLocations.size(); i++) {
            double prev = scores[bestLocations.get(i - 1)];
            double curr = scores[bestLocations.get(i)];
            assertTrue(prev >= curr,
                "Best locations should be sorted by suitability (descending)");
        }
    }

    @Test
    void testIsSuitableMethod() {
        CitySuitabilityAnalyzer analyzer = new CitySuitabilityAnalyzer(planet);
        CitySuitabilityAnalyzer.SuitabilityResult result = analyzer.analyzeWithStatistics(10);

        for (int i = 0; i < result.scores().length; i++) {
            boolean expected = result.scores()[i] > 0.5;
            assertEquals(expected, result.isSuitable(i),
                "isSuitable should match score > 0.5 threshold");
        }
    }

    @Test
    void testIsHighlySuitableMethod() {
        CitySuitabilityAnalyzer analyzer = new CitySuitabilityAnalyzer(planet);
        CitySuitabilityAnalyzer.SuitabilityResult result = analyzer.analyzeWithStatistics(10);

        for (int i = 0; i < result.scores().length; i++) {
            boolean expected = result.scores()[i] > 0.8;
            assertEquals(expected, result.isHighlySuitable(i),
                "isHighlySuitable should match score > 0.8 threshold");
        }
    }

    @Test
    void testStaticAnalyzeMethod() {
        double[] suitability1 = CitySuitabilityAnalyzer.analyze(planet);

        CitySuitabilityAnalyzer analyzer = new CitySuitabilityAnalyzer(planet);
        double[] suitability2 = analyzer.analyze();

        assertArrayEquals(suitability1, suitability2, 0.001,
            "Static and instance analyze methods should produce same results");
    }

    @Test
    void testDifferentPlanetsHaveDifferentSuitability() {
        // Generate another planet with different seed
        PlanetConfig config2 = PlanetConfig.builder()
            .seed(999L)
            .size(PlanetConfig.Size.DUEL)
            .build();
        PlanetGenerator.GeneratedPlanet planet2 = PlanetGenerator.generate(config2);

        double[] suit1 = CitySuitabilityAnalyzer.analyze(planet);
        double[] suit2 = CitySuitabilityAnalyzer.analyze(planet2);

        // They should have different distributions (not identical)
        double sum1 = 0, sum2 = 0;
        for (int i = 0; i < Math.min(suit1.length, suit2.length); i++) {
            sum1 += suit1[i];
            sum2 += suit2[i];
        }

        assertNotEquals(sum1, sum2, 0.001,
            "Different planets should have different suitability totals");
    }
}
