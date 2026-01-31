package com.teamgannon.trips.planetarymodelling.procedural.analysis;

import com.teamgannon.trips.planetarymodelling.procedural.AdjacencyGraph;
import com.teamgannon.trips.planetarymodelling.procedural.ClimateCalculator;
import com.teamgannon.trips.planetarymodelling.procedural.ErosionCalculator;
import com.teamgannon.trips.planetarymodelling.procedural.PlanetGenerator.GeneratedPlanet;

import java.util.*;

/**
 * Post-generation analysis for determining city/settlement suitability scores.
 *
 * <p>Evaluates each land polygon based on factors important for human habitation:
 * <ul>
 *   <li>Elevation: Prefer lowlands to plains (height 0-2)</li>
 *   <li>Climate: Prefer temperate climates</li>
 *   <li>Coastal proximity: Bonus for ocean access (trade, fishing)</li>
 *   <li>River proximity: Bonus for freshwater access (but not flood zones)</li>
 * </ul>
 *
 * <p>Inspired by planetgen's CityLayer but adapted for polygon mesh output.
 * This is a world-building tool, not a physical simulation.
 */
public class CitySuitabilityAnalyzer {

    // Scoring weights (can be adjusted for different scenarios)
    private static final double WEIGHT_ELEVATION = 0.25;
    private static final double WEIGHT_CLIMATE = 0.30;
    private static final double WEIGHT_COASTAL = 0.25;
    private static final double WEIGHT_RIVER = 0.20;

    // Elevation scoring preferences
    private static final int IDEAL_HEIGHT_MIN = 0;
    private static final int IDEAL_HEIGHT_MAX = 1;

    private final GeneratedPlanet planet;
    private final int[] heights;
    private final ClimateCalculator.ClimateZone[] climates;
    private final AdjacencyGraph adjacency;
    private final ErosionCalculator.ErosionResult erosionResult;

    // Cached calculations
    private boolean[] coastalMask;
    private boolean[] riverAdjacentMask;

    /**
     * Creates a new analyzer for the given planet.
     *
     * @param planet The generated planet to analyze
     */
    public CitySuitabilityAnalyzer(GeneratedPlanet planet) {
        this.planet = planet;
        this.heights = planet.heights();
        this.climates = planet.climates();
        this.adjacency = planet.adjacency();
        this.erosionResult = planet.erosionResult();
    }

    /**
     * Analyzes suitability for all polygons.
     *
     * @return Array of suitability scores (0.0 to 1.0) for each polygon
     */
    public double[] analyze() {
        // Pre-compute coastal and river proximity
        computeCoastalMask();
        computeRiverProximity();

        double[] suitability = new double[heights.length];

        for (int i = 0; i < heights.length; i++) {
            suitability[i] = analyzeSinglePolygon(i);
        }

        return suitability;
    }

    /**
     * Analyzes suitability for a single polygon.
     */
    private double analyzeSinglePolygon(int index) {
        // Water polygons have zero suitability
        if (heights[index] < 0) {
            return 0.0;
        }

        double elevationScore = scoreElevation(heights[index]);
        double climateScore = scoreClimate(climates[index]);
        double coastalScore = scoreCoastalProximity(index);
        double riverScore = scoreRiverProximity(index);

        // Weighted combination
        double combined = (elevationScore * WEIGHT_ELEVATION)
            + (climateScore * WEIGHT_CLIMATE)
            + (coastalScore * WEIGHT_COASTAL)
            + (riverScore * WEIGHT_RIVER);

        return Math.max(0.0, Math.min(1.0, combined));
    }

    /**
     * Scores elevation suitability (prefer lowlands/plains).
     */
    private double scoreElevation(int height) {
        if (height < 0) {
            return 0.0;  // Water
        }
        if (height >= IDEAL_HEIGHT_MIN && height <= IDEAL_HEIGHT_MAX) {
            return 1.0;  // Ideal elevation
        }
        if (height == 2) {
            return 0.6;  // Hills - still OK
        }
        if (height >= 3) {
            return 0.2;  // Mountains - challenging
        }
        return 0.5;  // Default
    }

    /**
     * Scores climate suitability (prefer temperate).
     *
     * Based on planetgen's CityLayer which uses temperature optimum of 15C.
     */
    private double scoreClimate(ClimateCalculator.ClimateZone climate) {
        return switch (climate) {
            case TEMPERATE -> 1.0;   // Ideal for human settlement
            case TROPICAL -> 0.7;    // Warm but habitable
            case POLAR -> 0.3;       // Cold, challenging
        };
    }

    /**
     * Scores coastal proximity (bonus for ocean access).
     */
    private double scoreCoastalProximity(int index) {
        if (coastalMask == null || !coastalMask[index]) {
            return 0.0;  // Not coastal
        }
        return 1.0;  // Coastal bonus
    }

    /**
     * Scores river proximity (bonus for freshwater access).
     */
    private double scoreRiverProximity(int index) {
        if (riverAdjacentMask == null || !riverAdjacentMask[index]) {
            return 0.0;  // Not river-adjacent
        }

        // Check if this is actually IN the river (flood zone) - reduced score
        if (isRiverPolygon(index)) {
            return 0.5;  // On river - flood risk
        }

        return 1.0;  // Adjacent to river - ideal
    }

    /**
     * Computes which polygons are coastal (adjacent to ocean).
     */
    private void computeCoastalMask() {
        coastalMask = new boolean[heights.length];

        for (int i = 0; i < heights.length; i++) {
            // Only land polygons can be coastal
            if (heights[i] < 0) continue;

            // Check neighbors for ocean
            for (int neighbor : adjacency.neighborsOnly(i)) {
                if (heights[neighbor] < 0) {
                    coastalMask[i] = true;
                    break;
                }
            }
        }
    }

    /**
     * Computes which polygons are adjacent to rivers.
     */
    private void computeRiverProximity() {
        riverAdjacentMask = new boolean[heights.length];

        if (erosionResult == null) return;

        List<List<Integer>> rivers = erosionResult.rivers();
        if (rivers == null || rivers.isEmpty()) return;

        // Mark all river polygons
        Set<Integer> riverPolygons = new HashSet<>();
        for (List<Integer> river : rivers) {
            riverPolygons.addAll(river);
        }

        // Mark river polygons and their neighbors
        for (int riverPoly : riverPolygons) {
            riverAdjacentMask[riverPoly] = true;
            for (int neighbor : adjacency.neighborsOnly(riverPoly)) {
                if (heights[neighbor] >= 0) {  // Only land
                    riverAdjacentMask[neighbor] = true;
                }
            }
        }
    }

    /**
     * Checks if polygon is part of a river path.
     */
    private boolean isRiverPolygon(int index) {
        if (erosionResult == null) return false;

        List<List<Integer>> rivers = erosionResult.rivers();
        if (rivers == null) return false;

        for (List<Integer> river : rivers) {
            if (river.contains(index)) {
                return true;
            }
        }
        return false;
    }

    // ==================== Analysis Results ====================

    /**
     * Result of suitability analysis with summary statistics.
     */
    public record SuitabilityResult(
        double[] scores,
        List<Integer> bestLocations,
        double averageSuitability,
        double maxSuitability,
        int highSuitabilityCount
    ) {
        /**
         * Returns true if the location is suitable (score > 0.5).
         */
        public boolean isSuitable(int index) {
            return scores[index] > 0.5;
        }

        /**
         * Returns true if the location is highly suitable (score > 0.8).
         */
        public boolean isHighlySuitable(int index) {
            return scores[index] > 0.8;
        }
    }

    /**
     * Performs full analysis and returns detailed results.
     *
     * @param topN Number of best locations to return
     * @return SuitabilityResult with scores and statistics
     */
    public SuitabilityResult analyzeWithStatistics(int topN) {
        double[] scores = analyze();

        // Find statistics
        double sum = 0;
        double max = 0;
        int highCount = 0;

        // Priority queue for top N locations (min-heap by score)
        PriorityQueue<Map.Entry<Integer, Double>> topLocations =
            new PriorityQueue<>(Comparator.comparingDouble(Map.Entry::getValue));

        for (int i = 0; i < scores.length; i++) {
            if (scores[i] > 0) {
                sum += scores[i];
                max = Math.max(max, scores[i]);
                if (scores[i] > 0.7) highCount++;

                topLocations.offer(Map.entry(i, scores[i]));
                if (topLocations.size() > topN) {
                    topLocations.poll();  // Remove lowest
                }
            }
        }

        // Extract top locations in descending order
        List<Integer> bestLocations = new ArrayList<>();
        while (!topLocations.isEmpty()) {
            bestLocations.add(topLocations.poll().getKey());
        }
        Collections.reverse(bestLocations);

        // Calculate average (only considering land polygons with score > 0)
        long landCount = Arrays.stream(scores).filter(s -> s > 0).count();
        double average = landCount > 0 ? sum / landCount : 0;

        return new SuitabilityResult(scores, bestLocations, average, max, highCount);
    }

    /**
     * Static factory method for quick analysis.
     */
    public static double[] analyze(GeneratedPlanet planet) {
        return new CitySuitabilityAnalyzer(planet).analyze();
    }
}
