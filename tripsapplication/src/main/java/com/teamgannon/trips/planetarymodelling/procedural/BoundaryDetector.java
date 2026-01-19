package com.teamgannon.trips.planetarymodelling.procedural;

import java.util.*;

/**
 * Determines plate types (oceanic/continental) and boundary interactions.
 */
public class BoundaryDetector {

    public enum PlateType { OCEANIC, CONTINENTAL }
    public enum BoundaryType { CONVERGENT, DIVERGENT, TRANSFORM, INACTIVE }

    private final PlanetConfig config;
    private final PlateAssigner.PlateAssignment assignment;
    private final Random random;

    private PlateType[] plateTypes;
    private List<Integer> sizeOrder;
    private List<List<Integer>> orderedAdjacentPlates;
    private Map<PlatePair, BoundaryType> boundaries;

    public record PlatePair(int plate1, int plate2) {
        public PlatePair {
            if (plate1 > plate2) {
                int tmp = plate1;
                plate1 = plate2;
                plate2 = tmp;
            }
        }
    }

    public record BoundaryAnalysis(
        PlateType[] plateTypes,
        Map<PlatePair, BoundaryType> boundaries,
        List<Integer> sizeOrder
    ) {}

    public BoundaryDetector(PlanetConfig config, PlateAssigner.PlateAssignment assignment) {
        this.config = config;
        this.assignment = assignment;
        this.random = new Random(config.subSeed(3));  // Different seed from other components
    }

    public BoundaryAnalysis analyze() {
        computeSizeOrder();
        orderAdjacentPlates();
        assignPlateTypes();
        computeInteractions();
        return new BoundaryAnalysis(plateTypes, boundaries, sizeOrder);
    }

    private void computeSizeOrder() {
        int plateCount = config.plateCount();
        List<Integer> plates = assignment.plates().stream()
            .map(List::size)
            .toList();

        List<int[]> plateSizes = new ArrayList<>();
        for (int i = 0; i < plateCount; i++) {
            plateSizes.add(new int[]{i, plates.get(i)});
        }

        plateSizes.sort((a, b) -> Integer.compare(b[1], a[1]));
        sizeOrder = plateSizes.stream().map(arr -> arr[0]).toList();
    }

    private void orderAdjacentPlates() {
        int plateCount = config.plateCount();
        orderedAdjacentPlates = new ArrayList<>(plateCount);

        for (int p = 0; p < plateCount; p++) {
            Set<Integer> adjSet = assignment.adjacentPlates().get(p);
            List<Integer> ordered = new ArrayList<>();
            ordered.add(p);

            for (int plateIdx : sizeOrder) {
                if (adjSet.contains(plateIdx) && plateIdx != p) {
                    ordered.add(plateIdx);
                }
            }

            orderedAdjacentPlates.add(ordered);
        }
    }

    private void assignPlateTypes() {
        int plateCount = config.plateCount();
        List<List<Integer>> plates = assignment.plates();

        // Calculate actual polygon count from assignment (more accurate than config preset)
        int polyCount = plates.stream().mapToInt(List::size).sum();

        plateTypes = new PlateType[plateCount];

        // Use oceanicPlateRatio from config (default 0.5, range 0.0-1.0)
        double targetOceanicRatio = config.oceanicPlateRatio();
        if (targetOceanicRatio <= 0.0) {
            Arrays.fill(plateTypes, PlateType.CONTINENTAL);
            return;
        }

        int largestPlate = sizeOrder.get(0);
        plateTypes[largestPlate] = PlateType.OCEANIC;
        int oceanPolys = plates.get(largestPlate).size();

        List<Integer> adjToLargest = orderedAdjacentPlates.get(largestPlate);
        int adjIdx = adjToLargest.size() - 1;

        double oceanPercent = (double) oceanPolys / polyCount;
        int fallbackIdx = plateCount - 1;

        // Add oceanic plates until we reach the target ratio
        while (oceanPercent < targetOceanicRatio) {
            if (adjIdx > 0) {
                int plateIdx = adjToLargest.get(adjIdx);
                if (plateTypes[plateIdx] == null) {
                    plateTypes[plateIdx] = PlateType.OCEANIC;
                    oceanPolys += plates.get(plateIdx).size();
                }
                adjIdx--;
            } else {
                int plateIdx = sizeOrder.get(fallbackIdx);
                if (plateTypes[plateIdx] == null) {
                    plateTypes[plateIdx] = PlateType.OCEANIC;
                    oceanPolys += plates.get(plateIdx).size();
                }
                fallbackIdx--;
                if (fallbackIdx < 0) break;
            }
            oceanPercent = (double) oceanPolys / polyCount;
        }

        for (int i = 0; i < plateCount; i++) {
            if (plateTypes[i] == null) {
                plateTypes[i] = PlateType.CONTINENTAL;
            }
        }
    }

    private void computeInteractions() {
        int plateCount = config.plateCount();
        boundaries = new HashMap<>();

        int numDivergentOcean = 0;
        int numSubductionOcean = 0;

        for (int i = 0; i < plateCount; i++) {
            if (plateTypes[i] != PlateType.OCEANIC) continue;

            List<Integer> adjPlates = orderedAdjacentPlates.get(i);
            for (int j = 1; j < adjPlates.size(); j++) {
                int adjPlate = adjPlates.get(j);
                PlatePair pair = new PlatePair(i, adjPlate);

                if (boundaries.containsKey(pair)) continue;

                if (plateTypes[adjPlate] == PlateType.OCEANIC) {
                    boundaries.put(pair, BoundaryType.TRANSFORM);
                } else {
                    // Limit divergent oceanic-continental boundaries to 2.
                    // Earth has ~2-3 major spreading centers (Atlantic, East Pacific, Indian).
                    // More would fragment continents unrealistically.
                    if (numDivergentOcean < 2) {
                        boundaries.put(pair, BoundaryType.DIVERGENT);
                        numDivergentOcean++;
                        markOppositePlates(i, adjPlate, BoundaryType.DIVERGENT);
                    // Limit subduction zones to 3.
                    // Earth has ~3-4 major subduction zones (Pacific Ring of Fire segments).
                    // This creates concentrated mountain building without overdoing it.
                    } else if (numSubductionOcean < 3) {
                        boundaries.put(pair, BoundaryType.CONVERGENT);
                        numSubductionOcean++;
                        markOppositePlates(i, adjPlate, BoundaryType.DIVERGENT);
                    } else {
                        boundaries.put(pair, BoundaryType.CONVERGENT);
                    }
                }
            }
        }

        for (int i = 0; i < plateCount; i++) {
            if (plateTypes[i] != PlateType.CONTINENTAL) continue;

            List<Integer> adjPlates = orderedAdjacentPlates.get(i);
            for (int j = 1; j < adjPlates.size(); j++) {
                int adjPlate = adjPlates.get(j);
                PlatePair pair = new PlatePair(i, adjPlate);

                if (boundaries.containsKey(pair)) {
                    BoundaryType existing = boundaries.get(pair);
                    if (existing == BoundaryType.DIVERGENT) {
                        markOppositePlates(i, adjPlate, BoundaryType.CONVERGENT);
                    } else if (existing == BoundaryType.CONVERGENT) {
                        markOppositePlates(i, adjPlate, BoundaryType.DIVERGENT);
                    }
                }
            }
        }

        for (int i = 0; i < plateCount; i++) {
            List<Integer> adjPlates = orderedAdjacentPlates.get(i);
            for (int j = 1; j < adjPlates.size(); j++) {
                int adjPlate = adjPlates.get(j);
                PlatePair pair = new PlatePair(i, adjPlate);
                boundaries.putIfAbsent(pair, BoundaryType.TRANSFORM);
            }
        }

        // Apply stagnant-lid mode biasing if active tectonics is disabled
        // Venus/Mars-like: mostly transform + inactive, very few convergent/divergent
        if (!config.enableActiveTectonics()) {
            applyStagnantLidBiasing();
        }
    }

    /**
     * Rebiases boundary types for stagnant-lid planets (no active plate tectonics).
     * Produces Venus/Mars-like behavior: mostly transform and inactive boundaries,
     * with rare rifting or subduction. Hotspot volcanism (handled elsewhere) becomes
     * the dominant volcanic mechanism.
     */
    private void applyStagnantLidBiasing() {
        for (PlatePair pair : boundaries.keySet()) {
            double roll = random.nextDouble();

            if (roll < 0.65) {
                // Dominant: transform/strike-slip (plates slide past each other slowly)
                boundaries.put(pair, BoundaryType.TRANSFORM);
            } else if (roll < 0.85) {
                // Common: inactive/dead zones (no significant interaction)
                boundaries.put(pair, BoundaryType.INACTIVE);
            } else if (roll < 0.95) {
                // Rare: divergent rifting (some spreading, like Valles Marineris)
                boundaries.put(pair, BoundaryType.DIVERGENT);
            } else {
                // Very rare: convergent/subduction (almost never on stagnant-lid worlds)
                boundaries.put(pair, BoundaryType.CONVERGENT);
            }
        }
    }

    private void markOppositePlates(int plate, int adjPlate, BoundaryType type) {
        Set<Integer> plateAdj = assignment.adjacentPlates().get(plate);
        Set<Integer> adjPlateAdj = assignment.adjacentPlates().get(adjPlate);

        for (int opposite : adjPlateAdj) {
            if (opposite != plate && !plateAdj.contains(opposite)) {
                PlatePair pair = new PlatePair(adjPlate, opposite);
                boundaries.putIfAbsent(pair, type);
            }
        }
    }

    public PlateType getPlateType(int plateIdx) {
        return plateTypes[plateIdx];
    }

    public BoundaryType getBoundary(int plate1, int plate2) {
        return boundaries.get(new PlatePair(plate1, plate2));
    }
}
