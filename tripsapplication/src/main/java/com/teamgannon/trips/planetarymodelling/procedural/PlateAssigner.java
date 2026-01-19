package com.teamgannon.trips.planetarymodelling.procedural;

import java.util.*;

/**
 * Assigns polygons to tectonic plates using flood-fill with distortion.
 */
public class PlateAssigner {

    private static final int UNASSIGNED = -1;
    private static final int CENTER_ADJACENT = -2;

    private final PlanetConfig config;
    private final AdjacencyGraph adjacency;
    private final Random random;

    private int[] plateIndex;
    private List<List<Integer>> plates;
    private List<Set<Integer>> adjacentPlates;
    private int assignedCount;

    public PlateAssigner(PlanetConfig config, AdjacencyGraph adjacency) {
        this.config = config;
        this.adjacency = adjacency;
        this.random = new Random(config.subSeed(1));
    }

    public record PlateAssignment(
        int[] plateIndex,
        List<List<Integer>> plates,
        List<Set<Integer>> adjacentPlates
    ) {}

    public PlateAssignment assign() {
        int polyCount = adjacency.size();
        int plateCount = config.plateCount();

        plateIndex = new int[polyCount];
        Arrays.fill(plateIndex, UNASSIGNED);

        plates = new ArrayList<>(plateCount);
        adjacentPlates = new ArrayList<>(plateCount);
        for (int i = 0; i < plateCount; i++) {
            plates.add(new ArrayList<>());
            adjacentPlates.add(new HashSet<>());
        }

        assignedCount = 0;
        placePlateCenters(plateCount, polyCount);

        while (assignedCount < polyCount) {
            for (int p = 0; p < plateCount; p++) {
                expandPlate(p, polyCount);
            }
        }

        return new PlateAssignment(plateIndex, plates, adjacentPlates);
    }

    private void placePlateCenters(int plateCount, int polyCount) {
        boolean[] done = new boolean[plateCount];
        int placed = 0;

        while (placed < plateCount) {
            for (int p = 0; p < plateCount; p++) {
                if (done[p]) continue;

                int candidate = random.nextInt(polyCount);

                if (plateIndex[candidate] == UNASSIGNED) {
                    plateIndex[candidate] = p;
                    plates.get(p).add(candidate);
                    assignedCount++;
                    done[p] = true;
                    placed++;

                    for (int neighbor : adjacency.neighborsOnly(candidate)) {
                        if (plateIndex[neighbor] == UNASSIGNED) {
                            plateIndex[neighbor] = CENTER_ADJACENT;
                        }
                    }
                }
            }
        }

        for (int i = 0; i < polyCount; i++) {
            if (plateIndex[i] == CENTER_ADJACENT) {
                plateIndex[i] = UNASSIGNED;
            }
        }
    }

    private void expandPlate(int plateIdx, int polyCount) {
        List<Integer> currentPolys = plates.get(plateIdx);
        List<Integer> toAdd = new ArrayList<>();

        double progress = (double) assignedCount / polyCount;
        double distortion = calculateDistortion(progress);

        for (int polyIdx : currentPolys) {
            for (int neighbor : adjacency.neighborsOnly(polyIdx)) {
                if (plateIndex[neighbor] == UNASSIGNED) {
                    if (random.nextDouble() < distortion) {
                        plateIndex[neighbor] = plateIdx;
                        toAdd.add(neighbor);
                        assignedCount++;
                    }
                } else if (plateIndex[neighbor] != plateIdx && plateIndex[neighbor] >= 0) {
                    adjacentPlates.get(plateIdx).add(plateIndex[neighbor]);
                    adjacentPlates.get(plateIndex[neighbor]).add(plateIdx);
                }
            }
        }

        plates.get(plateIdx).addAll(toAdd);
    }

    private double calculateDistortion(double progress) {
        List<Double> thresholds = config.distortionProgressThresholds();
        List<Double> values = config.distortionValues();

        for (int i = 0; i < thresholds.size(); i++) {
            if (progress < thresholds.get(i)) {
                return values.get(i);
            }
        }
        // If progress is 1.0 or greater, return the last value or a default
        return values.get(values.size() - 1);
    }
}
