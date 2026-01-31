package com.teamgannon.trips.planetarymodelling.procedural.impact;

import com.teamgannon.trips.noisegen.FastNoiseLite;
import com.teamgannon.trips.planetarymodelling.procedural.AdjacencyGraph;
import com.teamgannon.trips.planetarymodelling.procedural.BoundaryDetector;
import com.teamgannon.trips.planetarymodelling.procedural.PlanetConfig;
import com.teamgannon.trips.planetarymodelling.procedural.PlateAssigner;
import com.teamgannon.trips.planetarymodelling.procedural.Polygon;
import org.hipparchus.geometry.euclidean.threed.Vector3D;

import java.util.*;

/**
 * Calculates and places impact craters and volcanic features on a polygon mesh.
 *
 * <p>This calculator adapts the grid-based approach from planetgen to work with
 * the polygon mesh used in the procedural generation system:
 * <ul>
 *   <li>Uses FastNoiseLite Cellular noise for crater center selection</li>
 *   <li>Polygon neighbor traversal (BFS) for radial distance measurement</li>
 *   <li>Height modifications applied directly to polygon heights array</li>
 * </ul>
 *
 * <p>Crater placement considers:
 * <ul>
 *   <li>Density parameters from config (craterDensity, volcanoDensity)</li>
 *   <li>Terrain type (ocean vs land)</li>
 *   <li>Plate boundaries for volcano placement (convergent/divergent zones)</li>
 * </ul>
 */
public class CraterCalculator {

    private static final int MIN_CRATER_RADIUS = 2;
    private static final int MAX_CRATER_RADIUS_DEFAULT = 8;

    private final PlanetConfig config;
    private final List<Polygon> polygons;
    private final AdjacencyGraph adjacency;
    private final double[] heights;
    private final PlateAssigner.PlateAssignment plateAssignment;
    private final BoundaryDetector.BoundaryAnalysis boundaryAnalysis;
    private final Random random;

    // Noise generator for crater placement
    private final FastNoiseLite cellularNoise;

    // Results tracking
    private final List<Integer> craterCenters = new ArrayList<>();
    private final List<Integer> volcanoCenters = new ArrayList<>();
    private final List<CraterProfile> craterProfiles = new ArrayList<>();
    private final List<CraterProfile> volcanoProfiles = new ArrayList<>();
    private final List<Integer> craterRadii = new ArrayList<>();
    private final List<Integer> volcanoRadii = new ArrayList<>();

    /**
     * Creates a new CraterCalculator.
     *
     * @param config PlanetConfig with crater/volcano parameters
     * @param polygons List of polygons in the mesh
     * @param adjacency Adjacency graph for neighbor relationships
     * @param heights Height values for each polygon (will be modified)
     * @param plateAssignment Plate assignment for volcano placement (may be null)
     * @param boundaryAnalysis Boundary analysis for volcano placement (may be null)
     */
    public CraterCalculator(
            PlanetConfig config,
            List<Polygon> polygons,
            AdjacencyGraph adjacency,
            double[] heights,
            PlateAssigner.PlateAssignment plateAssignment,
            BoundaryDetector.BoundaryAnalysis boundaryAnalysis) {

        this.config = config;
        this.polygons = polygons;
        this.adjacency = adjacency;
        this.heights = heights;
        this.plateAssignment = plateAssignment;
        this.boundaryAnalysis = boundaryAnalysis;
        this.random = new Random(config.subSeed(7));  // Phase 7 for impacts

        // Initialize cellular noise for crater placement
        // Cellular noise naturally creates distinct "cells" - good for spacing craters
        this.cellularNoise = new FastNoiseLite((int) config.subSeed(7));
        this.cellularNoise.SetNoiseType(FastNoiseLite.NoiseType.Cellular);
        this.cellularNoise.SetCellularReturnType(FastNoiseLite.CellularReturnType.CellValue);
        this.cellularNoise.SetFrequency(0.5f);  // Adjust for crater spacing
    }

    /**
     * Calculates and applies impact features to the heights array.
     *
     * @return ImpactResult containing modified heights and feature metadata
     */
    public ImpactResult calculate() {
        double craterDensity = config.craterDensity();
        double volcanoDensity = config.volcanoDensity();

        // Skip if no features requested
        if (craterDensity <= 0 && volcanoDensity <= 0) {
            return ImpactResult.empty(heights);
        }

        // Phase 1: Place craters
        if (craterDensity > 0) {
            placeCraters(craterDensity);
        }

        // Phase 2: Place volcanoes (only if enabled)
        if (config.enableVolcanos() && volcanoDensity > 0) {
            placeVolcanoes(volcanoDensity);
        }

        return new ImpactResult(
            heights,
            List.copyOf(craterCenters),
            List.copyOf(volcanoCenters),
            List.copyOf(craterProfiles),
            List.copyOf(volcanoProfiles),
            List.copyOf(craterRadii),
            List.copyOf(volcanoRadii)
        );
    }

    /**
     * Places craters based on cellular noise and density parameter.
     */
    private void placeCraters(double density) {
        // Use cellular noise to select crater centers
        // Cells with noise value above threshold become craters
        double threshold = 1.0 - density;

        // Track which polygons are already part of a crater to prevent overlap
        boolean[] craterMask = new boolean[polygons.size()];

        for (int i = 0; i < polygons.size(); i++) {
            // Skip polygons already in a crater
            if (craterMask[i]) continue;

            Vector3D center = polygons.get(i).center();
            float cellValue = cellularNoise.GetNoise(
                (float) center.getX() * 10,
                (float) center.getY() * 10,
                (float) center.getZ() * 10
            );

            // Normalize cellValue from [-1,1] to [0,1]
            double normalizedValue = (cellValue + 1.0) / 2.0;

            if (normalizedValue > threshold) {
                // This polygon is a crater center
                int radius = calculateCraterRadius(i);
                CraterProfile profile = selectCraterProfile(i, radius);

                // Mark affected polygons
                Set<Integer> affected = getPolygonsWithinRadius(i, radius);
                for (int idx : affected) {
                    craterMask[idx] = true;
                }

                // Apply crater profile to heights
                applyCraterProfile(i, radius, profile);

                // Record crater
                craterCenters.add(i);
                craterProfiles.add(profile);
                craterRadii.add(radius);
            }
        }
    }

    /**
     * Places volcanoes at plate boundaries and hotspots.
     */
    private void placeVolcanoes(double density) {
        // Volcanoes are placed at:
        // 1. Convergent plate boundaries (subduction zones) -> strato volcanoes
        // 2. Divergent plate boundaries (rifts) -> shield volcanoes
        // 3. Hotspots (random placement based on density) -> shield/dome volcanoes

        boolean[] volcanoMask = new boolean[polygons.size()];

        // Boundary-based volcano placement
        if (plateAssignment != null && boundaryAnalysis != null) {
            placeBoundaryVolcanoes(volcanoMask, density);
        }

        // Hotspot-based volcano placement (fills remaining density)
        placeHotspotVolcanoes(volcanoMask, density);
    }

    /**
     * Places volcanoes at plate boundaries.
     */
    private void placeBoundaryVolcanoes(boolean[] volcanoMask, double density) {
        int[] plateIndex = plateAssignment.plateIndex();
        var boundaries = boundaryAnalysis.boundaries();

        // Reduced density for boundary volcanoes (they should be special)
        double boundaryDensity = density * 0.3;

        for (int i = 0; i < polygons.size(); i++) {
            if (volcanoMask[i]) continue;

            // Check if at plate boundary
            int plate = plateIndex[i];
            BoundaryDetector.BoundaryType boundaryType = null;

            for (int neighbor : adjacency.neighborsOnly(i)) {
                int neighborPlate = plateIndex[neighbor];
                if (neighborPlate != plate) {
                    BoundaryDetector.PlatePair pair = new BoundaryDetector.PlatePair(
                        Math.min(plate, neighborPlate),
                        Math.max(plate, neighborPlate)
                    );
                    boundaryType = boundaries.get(pair);
                    break;
                }
            }

            if (boundaryType == null) continue;

            // Only land polygons get visible volcanoes
            if (heights[i] < 0) continue;

            // Random check against density
            if (random.nextDouble() > boundaryDensity) continue;

            // Select volcano type based on boundary
            CraterProfile profile = switch (boundaryType) {
                case CONVERGENT -> CraterProfile.STRATO_VOLCANO;  // Subduction volcanism
                case DIVERGENT -> CraterProfile.SHIELD_VOLCANO;   // Rift volcanism
                case TRANSFORM -> random.nextBoolean() ? CraterProfile.DOME_VOLCANO : null;
                case INACTIVE -> null;  // No volcanism at inactive boundaries
            };

            if (profile == null) continue;

            int radius = calculateVolcanoRadius(profile);

            // Mark affected polygons
            Set<Integer> affected = getPolygonsWithinRadius(i, radius);
            for (int idx : affected) {
                volcanoMask[idx] = true;
            }

            // Apply volcano profile
            applyVolcanoProfile(i, radius, profile);

            // Record volcano
            volcanoCenters.add(i);
            volcanoProfiles.add(profile);
            volcanoRadii.add(radius);
        }
    }

    /**
     * Places hotspot volcanoes at random locations.
     */
    private void placeHotspotVolcanoes(boolean[] volcanoMask, double density) {
        // Use different noise frequency for hotspot placement
        FastNoiseLite hotspotNoise = new FastNoiseLite((int) config.subSeed(8));
        hotspotNoise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
        hotspotNoise.SetFrequency(0.3f);

        double threshold = 1.0 - (density * 0.5);  // Reduced for hotspots

        for (int i = 0; i < polygons.size(); i++) {
            if (volcanoMask[i]) continue;

            // Only land polygons
            if (heights[i] < 0) continue;

            Vector3D center = polygons.get(i).center();
            float noiseValue = hotspotNoise.GetNoise(
                (float) center.getX() * 8,
                (float) center.getY() * 8,
                (float) center.getZ() * 8
            );

            double normalizedValue = (noiseValue + 1.0) / 2.0;

            if (normalizedValue > threshold) {
                // Hotspot volcano (usually shield type)
                CraterProfile profile = random.nextDouble() < 0.7
                    ? CraterProfile.SHIELD_VOLCANO
                    : CraterProfile.DOME_VOLCANO;

                int radius = calculateVolcanoRadius(profile);

                Set<Integer> affected = getPolygonsWithinRadius(i, radius);
                for (int idx : affected) {
                    volcanoMask[idx] = true;
                }

                applyVolcanoProfile(i, radius, profile);

                volcanoCenters.add(i);
                volcanoProfiles.add(profile);
                volcanoRadii.add(radius);
            }
        }
    }

    /**
     * Calculates appropriate crater radius based on location and config.
     */
    private int calculateCraterRadius(int centerIdx) {
        int maxRadius = config.craterMaxRadius();
        if (maxRadius <= 0) {
            maxRadius = MAX_CRATER_RADIUS_DEFAULT;
        }

        // Random radius within range, weighted towards smaller craters
        // (small craters are more common than large ones)
        double r = random.nextDouble();
        r = r * r;  // Square to bias towards smaller values

        return MIN_CRATER_RADIUS + (int) (r * (maxRadius - MIN_CRATER_RADIUS));
    }

    /**
     * Calculates appropriate volcano radius based on type.
     */
    private int calculateVolcanoRadius(CraterProfile profile) {
        return switch (profile) {
            case STRATO_VOLCANO -> MIN_CRATER_RADIUS + random.nextInt(3);
            case SHIELD_VOLCANO -> MIN_CRATER_RADIUS + 2 + random.nextInt(4);  // Larger
            case DOME_VOLCANO -> MIN_CRATER_RADIUS + random.nextInt(2);
            default -> MIN_CRATER_RADIUS;
        };
    }

    /**
     * Selects appropriate crater profile based on location and size.
     */
    private CraterProfile selectCraterProfile(int centerIdx, int radius) {
        // Larger craters are more likely to be complex
        if (radius >= 5) {
            double r = random.nextDouble();
            if (r < 0.3) return CraterProfile.COMPLEX_RINGS;
            if (r < 0.5) return CraterProfile.COMPLEX_STEPS;
            if (r < 0.7) return CraterProfile.COMPLEX_FLAT;
        }

        // Smaller craters are usually simple
        return random.nextDouble() < 0.6
            ? CraterProfile.SIMPLE_ROUND
            : CraterProfile.SIMPLE_FLAT;
    }

    /**
     * Gets all polygon indices within a given radius (in polygon hops) from center.
     * Uses BFS traversal through the adjacency graph.
     */
    private Set<Integer> getPolygonsWithinRadius(int centerIdx, int maxRadius) {
        Set<Integer> result = new HashSet<>();
        Map<Integer, Integer> distances = new HashMap<>();

        Queue<Integer> queue = new LinkedList<>();
        queue.add(centerIdx);
        distances.put(centerIdx, 0);
        result.add(centerIdx);

        while (!queue.isEmpty()) {
            int current = queue.poll();
            int currentDist = distances.get(current);

            if (currentDist >= maxRadius) continue;

            for (int neighbor : adjacency.neighborsOnly(current)) {
                if (!distances.containsKey(neighbor)) {
                    distances.put(neighbor, currentDist + 1);
                    result.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        return result;
    }

    /**
     * Computes BFS distances from center polygon to all polygons within radius.
     *
     * @return Map of polygon index to hop distance from center
     */
    private Map<Integer, Integer> computeDistances(int centerIdx, int maxRadius) {
        Map<Integer, Integer> distances = new HashMap<>();

        Queue<Integer> queue = new LinkedList<>();
        queue.add(centerIdx);
        distances.put(centerIdx, 0);

        while (!queue.isEmpty()) {
            int current = queue.poll();
            int currentDist = distances.get(current);

            if (currentDist >= maxRadius) continue;

            for (int neighbor : adjacency.neighborsOnly(current)) {
                if (!distances.containsKey(neighbor)) {
                    distances.put(neighbor, currentDist + 1);
                    queue.add(neighbor);
                }
            }
        }

        return distances;
    }

    /**
     * Applies a crater profile to the heights array.
     */
    private void applyCraterProfile(int centerIdx, int radius, CraterProfile profile) {
        Map<Integer, Integer> distances = computeDistances(centerIdx, radius);
        double depthMultiplier = config.craterDepthMultiplier();
        double profileMultiplier = profile.getTypicalHeightMultiplier();

        for (Map.Entry<Integer, Integer> entry : distances.entrySet()) {
            int polyIdx = entry.getKey();
            int hops = entry.getValue();

            // Normalize distance to [0, 1]
            double normalizedDist = (double) hops / radius;

            // Get height modification from profile
            double heightMod = profile.getHeight(normalizedDist);

            // Scale by depth multiplier and profile characteristics
            heightMod *= depthMultiplier * profileMultiplier;

            // Apply modification (craters dig down, so height is negative)
            heights[polyIdx] += heightMod;
        }
    }

    /**
     * Applies a volcano profile to the heights array.
     */
    private void applyVolcanoProfile(int centerIdx, int radius, CraterProfile profile) {
        Map<Integer, Integer> distances = computeDistances(centerIdx, radius);
        double profileMultiplier = profile.getTypicalHeightMultiplier();

        for (Map.Entry<Integer, Integer> entry : distances.entrySet()) {
            int polyIdx = entry.getKey();
            int hops = entry.getValue();

            double normalizedDist = (double) hops / radius;
            double heightMod = profile.getHeight(normalizedDist);

            // Volcanoes add height (positive modification)
            heightMod *= profileMultiplier;

            // Use max to prevent weird interactions with existing terrain
            heights[polyIdx] = Math.max(heights[polyIdx], heights[polyIdx] + heightMod);
        }
    }
}
