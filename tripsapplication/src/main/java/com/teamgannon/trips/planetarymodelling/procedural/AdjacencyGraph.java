package com.teamgannon.trips.planetarymodelling.procedural;

import org.hipparchus.geometry.euclidean.threed.Vector3D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Computes and stores neighbor relationships between polygons.
 * Optimized with a spatial grid to reduce neighbor search complexity from O(n^2).
 */
public class AdjacencyGraph {

    private static final double NEIGHBOR_DISTANCE = 2.5;
    private static final double NEIGHBOR_DISTANCE_SQ = NEIGHBOR_DISTANCE * NEIGHBOR_DISTANCE;

    private final List<int[]> adjacencies;

    /**
     * Represents the integer coordinates of a cell in the spatial grid.
     */
    private record GridKey(int x, int y, int z) {}

    public AdjacencyGraph(List<Polygon> polygons) {
        this.adjacencies = computeAdjacencies(polygons);
    }

    private List<int[]> computeAdjacencies(List<Polygon> polygons) {
        int n = polygons.size();
        List<int[]> result = new ArrayList<>(n);
        if (n == 0) {
            return result;
        }

        // 1. Initialize Spatial Grid
        // The cell size is chosen to be the neighbor search radius, ensuring any
        // potential neighbor must be in an adjacent grid cell.
        double cellSize = NEIGHBOR_DISTANCE;
        Map<GridKey, List<Integer>> grid = new HashMap<>();

        // 2. Binning Pass: Place all polygon indices into the grid
        for (int i = 0; i < n; i++) {
            Vector3D center = polygons.get(i).center();
            GridKey key = new GridKey(
                (int) Math.floor(center.getX() / cellSize),
                (int) Math.floor(center.getY() / cellSize),
                (int) Math.floor(center.getZ() / cellSize)
            );
            grid.computeIfAbsent(key, k -> new ArrayList<>()).add(i);
        }

        // 3. Neighbor Search Pass
        for (int i = 0; i < n; i++) {
            Vector3D center = polygons.get(i).center();
            List<Integer> neighbors = new ArrayList<>();
            neighbors.add(i); // A polygon is its own neighbor

            GridKey centerKey = new GridKey(
                (int) Math.floor(center.getX() / cellSize),
                (int) Math.floor(center.getY() / cellSize),
                (int) Math.floor(center.getZ() / cellSize)
            );

            // Iterate through the 3x3x3 cube of cells around the polygon's cell
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        GridKey searchKey = new GridKey(centerKey.x + dx, centerKey.y + dy, centerKey.z + dz);
                        List<Integer> candidates = grid.get(searchKey);

                        if (candidates == null) {
                            continue;
                        }

                        // For each candidate in the cell, check the actual distance
                        for (int j : candidates) {
                            if (i == j) {
                                continue;
                            }
                            Vector3D otherCenter = polygons.get(j).center();
                            double distSq = Vector3D.distanceSq(center, otherCenter);
                            if (distSq < NEIGHBOR_DISTANCE_SQ) {
                                neighbors.add(j);
                            }
                        }
                    }
                }
            }
            result.add(neighbors.stream().mapToInt(Integer::intValue).toArray());
        }

        return result;
    }

    public int[] neighbors(int idx) {
        return adjacencies.get(idx);
    }

    public int[] neighborsOnly(int idx) {
        int[] full = adjacencies.get(idx);
        int[] neighbors = new int[full.length - 1];
        System.arraycopy(full, 1, neighbors, 0, neighbors.length);
        return neighbors;
    }

    public boolean areNeighbors(int idx1, int idx2) {
        for (int neighbor : neighborsOnly(idx1)) {
            if (neighbor == idx2) return true;
        }
        return false;
    }

    public int size() {
        return adjacencies.size();
    }
}
