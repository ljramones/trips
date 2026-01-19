package com.teamgannon.trips.planetarymodelling.procedural;

import com.teamgannon.trips.planetarymodelling.procedural.BoundaryDetector.*;

import java.util.*;

/**
 * Calculates terrain elevation based on plate interactions.
 */
public class ElevationCalculator {

    public static final int DEEP_OCEAN = -4;
    public static final int OCEAN = -3;
    public static final int SHALLOW_OCEAN = -2;
    public static final int COASTAL = -1;
    public static final int LOWLAND = 0;
    public static final int PLAINS = 1;
    public static final int HILLS = 2;
    public static final int MOUNTAINS = 3;
    public static final int HIGH_MOUNTAINS = 4;
    private static final int UNASSIGNED = -42;

    private final PlanetConfig config;
    private final AdjacencyGraph adjacency;
    private final PlateAssigner.PlateAssignment plateAssignment;
    private final BoundaryAnalysis boundaryAnalysis;
    private final Random random;

    public record ElevationResult(int[] heights, double[] continuousHeights) {}

    private int[] heights;
    private double[] continuousHeights;
    private int[] massMarker;

    public ElevationCalculator(
            PlanetConfig config,
            AdjacencyGraph adjacency,
            PlateAssigner.PlateAssignment plateAssignment,
            BoundaryAnalysis boundaryAnalysis) {
        this.config = config;
        this.adjacency = adjacency;
        this.plateAssignment = plateAssignment;
        this.boundaryAnalysis = boundaryAnalysis;
        this.random = new Random(config.subSeed(2));
    }

    public int[] calculate() {
        return calculateResult().heights();
    }

    public ElevationResult calculateResult() {
        int polyCount = adjacency.size(); // Use actual polygon count
        int plateCount = config.plateCount();

        heights = new int[polyCount];
        continuousHeights = new double[polyCount];
        Arrays.fill(heights, UNASSIGNED);
        massMarker = new int[polyCount];

        List<List<Integer>> plates = plateAssignment.plates();
        PlateType[] types = boundaryAnalysis.plateTypes();
        List<Integer> sizeOrder = boundaryAnalysis.sizeOrder();

        for (int p = 0; p < plateCount; p++) {
            int baseHeight = (types[p] == PlateType.OCEANIC) ? DEEP_OCEAN : PLAINS;
            for (int polyIdx : plates.get(p)) {
                heights[polyIdx] = baseHeight;
                continuousHeights[polyIdx] = baseHeight;
            }
        }

        for (int p = 0; p < plateCount; p++) {
            Set<Integer> adjPlates = plateAssignment.adjacentPlates().get(p);
            for (int adjPlate : adjPlates) {
                if (adjPlate <= p) continue;

                BoundaryType boundary = boundaryAnalysis.boundaries()
                    .get(new PlatePair(p, adjPlate));
                if (boundary == null) continue;

                applyBoundaryEffect(p, adjPlate, types[p], types[adjPlate], boundary, plates);
            }
        }

        // Mountain range length scales with mesh resolution.
        // Smaller meshes (n<21) use shorter ranges (5 polygons) to avoid
        // ranges spanning too much of the planet. Larger meshes (n≥21)
        // use longer ranges (7 polygons) for proportional detail.
        // Values derived from visual testing at each mesh resolution.
        int mountainLength = (config.n() < 21) ? 5 : 7;
        for (int p = 0; p < plateCount; p++) {
            if (types[p] == PlateType.CONTINENTAL && random.nextDouble() > 0.25) {
                generateMountainRange(p, plates.get(p), 2 + random.nextInt(mountainLength - 1));
            }
        }

        adjustWaterLevel();
        adjustTerrainDistribution();

        // Generate island chain in largest oceanic plate (hotspot volcanism like Hawaii).
        // Chain length scales with mesh resolution: shorter chains (3) on small meshes
        // to avoid dominating the ocean, longer chains (7) on large meshes.
        // Values tuned to produce visually realistic archipelagos at each scale.
        int largestOceanic = findLargestOceanicPlate(types, sizeOrder);
        if (largestOceanic >= 0) {
            int islandLength = (config.n() < 21) ? 3 : 7;
            generateIslandChain(plates.get(largestOceanic), islandLength);
        }

        // Generate volcanic hotspots based on probability from config
        generateHotspots(plates, types);

        return new ElevationResult(heights, continuousHeights);
    }

    /**
     * Applies terrain effects at the boundary between two plates.
     * Dispatches to specialized methods based on boundary type and plate types.
     *
     * @param plate1   First plate index
     * @param plate2   Second plate index
     * @param type1    Type of first plate (OCEANIC or CONTINENTAL)
     * @param type2    Type of second plate
     * @param boundary Type of boundary interaction
     * @param plates   List of polygon indices for each plate
     */
    private void applyBoundaryEffect(int plate1, int plate2,
            PlateType type1, PlateType type2, BoundaryType boundary,
            List<List<Integer>> plates) {

        switch (boundary) {
            case CONVERGENT -> applyConvergentBoundary(plate1, plate2, type1, type2, plates);
            case DIVERGENT -> applyDivergentBoundary(plate1, plate2, type1, type2, plates);
            case TRANSFORM -> applyTransformBoundary(plate1, plate2, type1, type2, plates);
            case INACTIVE -> {
                // Inactive/dead zone: no significant plate interaction.
                // Typical of stagnant-lid worlds (Venus, Mars).
            }
        }
    }

    /**
     * Applies terrain effects for convergent (colliding) plate boundaries.
     *
     * <p>Physical basis:
     * <ul>
     *   <li>Oceanic-Oceanic: One plate subducts, creating island arcs (Japan, Philippines)</li>
     *   <li>Oceanic-Continental: Oceanic plate subducts, creating coastal mountains (Andes)</li>
     *   <li>Continental-Continental: Neither subducts, creating high mountains (Himalayas)</li>
     * </ul>
     */
    private void applyConvergentBoundary(int plate1, int plate2,
            PlateType type1, PlateType type2, List<List<Integer>> plates) {

        boolean oceanic1 = (type1 == PlateType.OCEANIC);
        boolean oceanic2 = (type2 == PlateType.OCEANIC);
        double heightMult = config.heightScaleMultiplier();
        var effects = config.boundaryEffects();

        if (oceanic1 && oceanic2) {
            // Island arc formation: small uplift where one plate subducts
            applyEffectLayers(plates.get(plate1), plate1, plate2,
                effects.convergentOceanicOceanic(), heightMult);
        } else if (oceanic1 && !oceanic2) {
            // Subduction with coastal mountains on continental side
            applyEffectLayers(plates.get(plate1), plate1, plate2,
                effects.convergentOceanicContinental(), heightMult);
        } else if (!oceanic1 && oceanic2) {
            // Reverse subduction - continental overrides oceanic
            applyEffectLayers(plates.get(plate1), plate1, plate2,
                effects.convergentContinentalOceanic(), heightMult);
        } else {
            // Continental collision - major mountain building
            applyEffectLayers(plates.get(plate1), plate1, plate2,
                effects.convergentContinentalContinental(), heightMult);
            // Random chance of extra uplift (double collision effect)
            if (random.nextDouble() < effects.continentalCollisionExtraChance()) {
                var extraEffect = effects.convergentContinentalContinental();
                if (extraEffect.layerCount() > 1) {
                    applyMassScaled(plates.get(plate1), plate1, plate2,
                        extraEffect.percents()[1], extraEffect.deltas()[1],
                        extraEffect.distortions()[1], heightMult);
                }
            }
        }
    }

    /**
     * Applies terrain effects for divergent (spreading) plate boundaries.
     *
     * <p>Physical basis:
     * <ul>
     *   <li>Oceanic-Continental: Passive margin with mild uplift</li>
     *   <li>Continental-Oceanic: Rift initiation, early spreading</li>
     *   <li>Continental-Continental: Rift valley formation (East African Rift)</li>
     * </ul>
     */
    private void applyDivergentBoundary(int plate1, int plate2,
            PlateType type1, PlateType type2, List<List<Integer>> plates) {

        boolean oceanic1 = (type1 == PlateType.OCEANIC);
        boolean oceanic2 = (type2 == PlateType.OCEANIC);
        double heightMult = config.heightScaleMultiplier();
        double riftMult = config.riftDepthMultiplier();
        var effects = config.boundaryEffects();

        if (oceanic1 && !oceanic2) {
            // Passive margin uplift
            applyEffectLayers(plates.get(plate1), plate1, plate2,
                effects.divergentOceanicContinental(), heightMult);
        } else if (!oceanic1 && oceanic2) {
            // Rift initiation - uses rift depth multiplier
            applyEffectLayers(plates.get(plate1), plate1, plate2,
                effects.divergentContinentalOceanic(), riftMult);
        } else if (!oceanic1 && !oceanic2) {
            // Continental rift valley - deep and wide
            applyEffectLayers(plates.get(plate1), plate1, plate2,
                effects.divergentContinentalContinental(), riftMult);
        }
        // Oceanic-Oceanic divergent: mid-ocean ridge, no significant surface effect
    }

    /**
     * Applies terrain effects for transform (sliding) plate boundaries.
     *
     * <p>Physical basis:
     * <ul>
     *   <li>Transform faults create minor uplift or depression</li>
     *   <li>Continental-Continental: Rarely creates pull-apart basins</li>
     *   <li>Less dramatic than convergent/divergent boundaries</li>
     * </ul>
     */
    private void applyTransformBoundary(int plate1, int plate2,
            PlateType type1, PlateType type2, List<List<Integer>> plates) {

        boolean oceanic1 = (type1 == PlateType.OCEANIC);
        boolean oceanic2 = (type2 == PlateType.OCEANIC);
        double heightMult = config.heightScaleMultiplier();
        double riftMult = config.riftDepthMultiplier();
        var effects = config.boundaryEffects();

        if (oceanic1 && !oceanic2) {
            // Coastal fault uplift
            applyEffectLayers(plates.get(plate1), plate1, plate2,
                effects.transformOceanicContinental(), heightMult);
        } else if (!oceanic1 && oceanic2) {
            // Coastal fault depression
            applyEffectLayers(plates.get(plate1), plate1, plate2,
                effects.transformContinentalOceanic(), riftMult);
        } else if (!oceanic1 && !oceanic2) {
            // Pull-apart basin - only 25% of the time
            if (random.nextDouble() > 0.75) {
                applyEffectLayers(plates.get(plate1), plate1, plate2,
                    effects.transformContinentalContinental(), riftMult);
            }
        }
        // Oceanic-Oceanic transform: fracture zone, no significant surface effect
    }

    /**
     * Applies multiple terrain effect layers from configuration.
     *
     * @param platePolys Polygon indices for the source plate
     * @param plate1Idx  First plate index
     * @param plate2Idx  Second plate index
     * @param params     Effect parameters (percents, deltas, distortions)
     * @param multiplier Height scale multiplier
     */
    private void applyEffectLayers(List<Integer> platePolys, int plate1Idx, int plate2Idx,
            PlanetConfig.BoundaryEffectConfig.EffectParams params, double multiplier) {

        for (int i = 0; i < params.layerCount(); i++) {
            applyMassScaled(platePolys, plate1Idx, plate2Idx,
                params.percents()[i], params.deltas()[i],
                params.distortions()[i], multiplier);
        }
    }

    private List<Integer> findBoundaryPolygons(List<Integer> plate1Polys, int plate1Idx, int plate2Idx) {
        int[] plateIndex = plateAssignment.plateIndex();
        List<Integer> boundary = new ArrayList<>();

        for (int polyIdx : plate1Polys) {
            for (int neighbor : adjacency.neighborsOnly(polyIdx)) {
                if (plateIndex[neighbor] == plate2Idx && massMarker[polyIdx] != plate1Idx) {
                    boundary.add(polyIdx);
                    massMarker[polyIdx] = plate1Idx;
                    break;
                }
            }
        }
        return boundary;
    }

    private void applyMass(List<Integer> plate1Polys, int plate1Idx, int plate2Idx,
            double maxPercent, int heightDelta, double distortion, double continuousDelta) {
        if (maxPercent <= 0) return;

        Arrays.fill(massMarker, -1);
        List<Integer> mass = findBoundaryPolygons(plate1Polys, plate1Idx, plate2Idx);
        int[] plateIndex = plateAssignment.plateIndex();

        double percent = (double) mass.size() / plate1Polys.size();

        while (percent < maxPercent && !mass.isEmpty()) {
            List<Integer> toAdd = new ArrayList<>();

            for (int polyIdx : mass) {
                for (int neighbor : adjacency.neighborsOnly(polyIdx)) {
                    if (massMarker[neighbor] == -1 && plateIndex[neighbor] == plate1Idx) {
                        if (random.nextDouble() < distortion) {
                            toAdd.add(neighbor);
                            massMarker[neighbor] = plate1Idx;
                        }
                    }
                }
            }

            mass.addAll(toAdd);
            percent = (double) mass.size() / plate1Polys.size();

            if (toAdd.isEmpty()) break;
        }

        for (int polyIdx : mass) {
            if (heights[polyIdx] == UNASSIGNED) {
                heights[polyIdx] = LOWLAND;
            }
            int newHeight = heights[polyIdx] + heightDelta;
            heights[polyIdx] = Math.max(DEEP_OCEAN, Math.min(HIGH_MOUNTAINS, newHeight));
            continuousHeights[polyIdx] = clampContinuous(continuousHeights[polyIdx] + continuousDelta);
        }
    }

    /**
     * Applies terrain mass with a scaling multiplier for physically-derived terrain.
     * The multiplier affects how many height levels are added/removed.
     * A multiplier of 1.0 gives standard behavior, >1.0 creates more extreme terrain,
     * <1.0 creates flatter terrain.
     */
    private void applyMassScaled(List<Integer> plate1Polys, int plate1Idx, int plate2Idx,
            double maxPercent, int baseHeightDelta, double distortion, double multiplier) {
        if (maxPercent <= 0) return;

        // Scale the height delta by the multiplier.
        // Values < 1.0 reduce the chance/magnitude of height changes.
        double scaledMagnitude = Math.abs(baseHeightDelta) * multiplier;
        int scaledWhole = (int) Math.floor(scaledMagnitude);
        double scaledFraction = scaledMagnitude - scaledWhole;
        int sign = baseHeightDelta >= 0 ? 1 : -1;

        int scaledDelta = sign * scaledWhole;
        if (scaledFraction > 0 && random.nextDouble() < scaledFraction) {
            scaledDelta += sign;
        }

        double continuousDelta = baseHeightDelta * multiplier;
        if (scaledDelta == 0 && continuousDelta == 0.0) return;

        applyMass(plate1Polys, plate1Idx, plate2Idx, maxPercent, scaledDelta, distortion, continuousDelta);
    }

    private void generateMountainRange(int plateIdx, List<Integer> platePolys, int length) {
        int[] plateIndex = plateAssignment.plateIndex();
        Set<Integer> exclude = new HashSet<>();

        for (int polyIdx : platePolys) {
            if (heights[polyIdx] != PLAINS) continue;

            int[] neighbors = adjacency.neighbors(polyIdx);
            boolean allPlains = true;
            for (int neighbor : neighbors) {
                if (heights[neighbor] != PLAINS) {
                    allPlains = false;
                    break;
                }
            }

            if (!allPlains) continue;

            List<Integer> range = new ArrayList<>();
            int current = polyIdx;

            while (range.size() < length) {
                range.add(current);
                int[] currentNeighbors = adjacency.neighborsOnly(current);

                int chosen = -1;
                int skip = 0;
                boolean flip = random.nextBoolean();

                for (int neighbor : currentNeighbors) {
                    if (!exclude.contains(neighbor) && plateIndex[neighbor] == plateIdx) {
                        if (chosen == -1) {
                            if ((flip && skip >= 1) || (!flip && skip >= 2)) {
                                chosen = neighbor;
                            } else {
                                skip++;
                            }
                        }
                    }
                }

                if (chosen == -1) break;

                for (int neighbor : currentNeighbors) {
                    if (neighbor != chosen) {
                        exclude.add(neighbor);
                    }
                }

                current = chosen;
            }

            for (int idx : range) {
                heights[idx] = MOUNTAINS;
                continuousHeights[idx] = MOUNTAINS;
            }

            break;
        }
    }

    private void generateIslandChain(List<Integer> platePolys, int length) {
        Set<Integer> exclude = new HashSet<>();

        for (int polyIdx : platePolys) {
            if (heights[polyIdx] != DEEP_OCEAN) continue;

            int[] neighbors = adjacency.neighbors(polyIdx);
            boolean allDeep = true;
            for (int neighbor : neighbors) {
                if (heights[neighbor] != DEEP_OCEAN) {
                    allDeep = false;
                    break;
                }
            }

            if (!allDeep) continue;

            List<Integer> chain = new ArrayList<>();
            int current = polyIdx;

            while (chain.size() < length) {
                if (random.nextDouble() < 0.25) {
                    chain.add(current);
                }

                int[] currentNeighbors = adjacency.neighborsOnly(current);
                int chosen = -1;

                for (int neighbor : currentNeighbors) {
                    if (!exclude.contains(neighbor) && chosen == -1) {
                        chosen = neighbor;
                    } else if (!exclude.contains(neighbor)) {
                        exclude.add(neighbor);
                    }
                }

                if (chosen == -1) break;
                current = chosen;
            }

            for (int i = 0; i < chain.size(); i++) {
                int idx = chain.get(i);
                if (i < 2) heights[idx] = MOUNTAINS;
                else if (i < 5) heights[idx] = HILLS;
                else heights[idx] = PLAINS;
                continuousHeights[idx] = heights[idx];
            }

            break;
        }
    }

    /**
     * Generates volcanic hotspots scattered across plates.
     * Hotspots create localized high terrain (mountains/hills) that can appear
     * anywhere on a plate, representing mantle plume volcanism.
     * More active on planets with higher hotspotProbability (younger planets,
     * larger planets with more internal heat, or stagnant lid worlds).
     */
    private void generateHotspots(List<List<Integer>> plates, PlateType[] types) {
        double hotspotProb = config.hotspotProbability();
        if (hotspotProb <= 0) return;

        int plateCount = config.plateCount();
        int hotspotSize = Math.max(2, config.n() / 7);  // Scale hotspot size with mesh detail

        for (int p = 0; p < plateCount; p++) {
            List<Integer> platePolys = plates.get(p);
            if (platePolys.size() < hotspotSize * 2) continue;  // Too small for hotspots

            // Roll for each plate if it gets a hotspot
            if (random.nextDouble() < hotspotProb) {
                // Pick a random polygon as hotspot center
                int centerIdx = platePolys.get(random.nextInt(platePolys.size()));

                // Create hotspot terrain
                createHotspot(centerIdx, types[p], hotspotSize);
            }

            // Larger plates have chance of additional hotspots
            if (platePolys.size() > 200 && random.nextDouble() < hotspotProb * 0.5) {
                int centerIdx = platePolys.get(random.nextInt(platePolys.size()));
                createHotspot(centerIdx, types[p], hotspotSize / 2);
            }
        }
    }

    /**
     * Creates a volcanic hotspot at the given center polygon.
     * Oceanic hotspots create island chains, continental hotspots create volcanic highlands.
     */
    private void createHotspot(int centerIdx, PlateType plateType, int size) {
        Set<Integer> hotspotPolys = new HashSet<>();
        hotspotPolys.add(centerIdx);

        // Expand outward from center using BFS with iteration limit
        List<Integer> frontier = new ArrayList<>();
        frontier.add(centerIdx);
        int maxIterations = size * 3;  // Safety limit to prevent infinite loops

        for (int iter = 0; iter < maxIterations && hotspotPolys.size() < size && !frontier.isEmpty(); iter++) {
            List<Integer> nextFrontier = new ArrayList<>();
            for (int polyIdx : frontier) {
                for (int neighbor : adjacency.neighborsOnly(polyIdx)) {
                    if (!hotspotPolys.contains(neighbor)) {
                        if (random.nextDouble() < 0.6) {  // Irregular shape
                            hotspotPolys.add(neighbor);
                            nextFrontier.add(neighbor);
                        }
                    }
                }
            }
            frontier = nextFrontier;
        }

        // Apply height based on plate type
        for (int polyIdx : hotspotPolys) {
            if (plateType == PlateType.OCEANIC) {
                // Oceanic hotspot: raise from deep ocean to create volcanic island
                if (heights[polyIdx] <= OCEAN) {
                    heights[polyIdx] = random.nextDouble() < 0.3 ? MOUNTAINS : HILLS;
                    continuousHeights[polyIdx] = heights[polyIdx];
                }
            } else {
                // Continental hotspot: create volcanic highlands
                if (heights[polyIdx] < MOUNTAINS) {
                    heights[polyIdx] = random.nextDouble() < 0.4 ? HIGH_MOUNTAINS : MOUNTAINS;
                    continuousHeights[polyIdx] = heights[polyIdx];
                }
            }
        }
    }

    private void adjustWaterLevel() {
        int polyCount = heights.length;  // Use actual count, not config preset
        double targetWater = config.waterFraction();
        int maxIterations = polyCount * (targetWater <= 0.05 ? 5 : 2);  // Safety limit

        double seaPercent = percentBelow(LOWLAND);

        // Reduce water if too high
        int iterations = 0;
        while (seaPercent > targetWater + 0.05 && iterations < maxIterations) {
            int modified = 0;
            for (int i = 0; i < polyCount; i++) {
                if (heights[i] < LOWLAND) {
                    double raiseChance = 0.15 + 0.05 * (heights[i] - DEEP_OCEAN);
                    raiseChance = Math.min(0.35, Math.max(0.15, raiseChance));
                    if (random.nextDouble() < raiseChance) {
                        heights[i]++;
                        continuousHeights[i] = clampContinuous(continuousHeights[i] + 1.0);
                        modified++;
                        if (heights[i] >= LOWLAND) {
                            seaPercent = percentBelow(LOWLAND);
                            if (seaPercent <= targetWater + 0.05) break;
                        }
                    }
                }
            }
            if (modified == 0 || percentBelow(LOWLAND) >= seaPercent) break;
            seaPercent = percentBelow(LOWLAND);
            iterations++;
        }

        // Increase water if too low
        iterations = 0;
        while (seaPercent < targetWater - 0.01 && iterations < maxIterations) {
            int modified = 0;
            for (int i = 0; i < polyCount; i++) {
                if (heights[i] == LOWLAND) {
                    if (random.nextDouble() < 0.35) {
                        heights[i]--;
                        continuousHeights[i] = clampContinuous(continuousHeights[i] - 1.0);
                        modified++;
                        seaPercent = percentBelow(LOWLAND);
                        if (seaPercent >= targetWater - 0.01) break;
                    }
                }
            }
            if (modified == 0 || percentBelow(LOWLAND) <= seaPercent) break;
            seaPercent = percentBelow(LOWLAND);
            iterations++;
        }
    }

    private void adjustTerrainDistribution() {
        int polyCount = heights.length;  // Use actual count, not config preset
        int maxIterations = polyCount * 2;  // Safety limit based on mesh size

        // Reduce mountains/high mountains to < 5%
        int iterations = 0;
        while (percentAbove(HILLS) >= 0.05 && iterations < maxIterations) {
            int modified = 0;
            for (int i = 0; i < polyCount; i++) {
                if (heights[i] > HILLS && random.nextDouble() < 0.65) {
                    heights[i]--;
                    continuousHeights[i] = clampContinuous(continuousHeights[i] - 1.0);
                    modified++;
                    if (percentAbove(HILLS) < 0.05) break;
                }
            }
            if (modified == 0) break;  // No progress possible
            iterations++;
        }

        // Ensure at least 15% farmable land (plains + lowland)
        double farmable = percentEqual(PLAINS) + percentEqual(LOWLAND);
        iterations = 0;
        while (farmable < 0.15 && iterations < maxIterations) {
            int modified = 0;
            for (int i = 0; i < polyCount; i++) {
                if (heights[i] == HILLS && random.nextDouble() < 0.75) {
                    heights[i]--;
                    continuousHeights[i] = clampContinuous(continuousHeights[i] - 1.0);
                    modified++;
                    farmable = percentEqual(PLAINS) + percentEqual(LOWLAND);
                    if (farmable >= 0.15) break;
                }
            }
            if (modified == 0) break;  // No hills left to convert
            iterations++;
        }

        // Limit hills to < 14%
        iterations = 0;
        while (percentEqual(HILLS) > 0.14 && iterations < maxIterations) {
            int modified = 0;
            for (int i = 0; i < polyCount; i++) {
                if (heights[i] == HILLS && random.nextDouble() < 0.35) {
                    heights[i]--;
                    continuousHeights[i] = clampContinuous(continuousHeights[i] - 1.0);
                    modified++;
                    if (percentEqual(HILLS) <= 0.14) break;
                }
            }
            if (modified == 0) break;  // No progress possible
            iterations++;
        }

        // Limit lowland to < 8%
        iterations = 0;
        while (percentEqual(LOWLAND) > 0.08 && iterations < maxIterations) {
            int modified = 0;
            for (int i = 0; i < polyCount; i++) {
                if (heights[i] == LOWLAND && random.nextDouble() < 0.35) {
                    heights[i]++;
                    continuousHeights[i] = clampContinuous(continuousHeights[i] + 1.0);
                    modified++;
                    if (percentEqual(LOWLAND) <= 0.08) break;
                }
            }
            if (modified == 0) break;  // No progress possible
            iterations++;
        }
    }

    private double percentBelow(int threshold) {
        int count = 0;
        for (int h : heights) if (h < threshold) count++;
        return (double) count / heights.length;
    }

    private double percentAbove(int threshold) {
        int count = 0;
        for (int h : heights) if (h > threshold) count++;
        return (double) count / heights.length;
    }

    private double percentEqual(int value) {
        int count = 0;
        for (int h : heights) if (h == value) count++;
        return (double) count / heights.length;
    }

    private int findLargestOceanicPlate(PlateType[] types, List<Integer> sizeOrder) {
        for (int plateIdx : sizeOrder) {
            if (types[plateIdx] == PlateType.OCEANIC) return plateIdx;
        }
        return -1;
    }

    private double clampContinuous(double value) {
        return Math.max(DEEP_OCEAN, Math.min(HIGH_MOUNTAINS, value));
    }

}
