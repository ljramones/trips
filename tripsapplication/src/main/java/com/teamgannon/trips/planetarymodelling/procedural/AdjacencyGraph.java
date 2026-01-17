package com.teamgannon.trips.planetarymodelling.procedural;

import org.hipparchus.geometry.euclidean.threed.Vector3D;

import java.util.ArrayList;
import java.util.List;

/**
 * Computes and stores neighbor relationships between polygons.
 */
public class AdjacencyGraph {

    private static final double NEIGHBOR_DISTANCE_SQ = 2.5 * 2.5;

    private final List<int[]> adjacencies;

    public AdjacencyGraph(List<Polygon> polygons) {
        this.adjacencies = computeAdjacencies(polygons);
    }

    private List<int[]> computeAdjacencies(List<Polygon> polygons) {
        int n = polygons.size();
        List<int[]> result = new ArrayList<>(n);

        for (int i = 0; i < n; i++) {
            Vector3D center = polygons.get(i).center();
            List<Integer> neighbors = new ArrayList<>();
            neighbors.add(i);

            for (int j = 0; j < n; j++) {
                if (i != j) {
                    Vector3D otherCenter = polygons.get(j).center();
                    double distSq = Vector3D.distanceSq(center, otherCenter);
                    if (distSq < NEIGHBOR_DISTANCE_SQ) {
                        neighbors.add(j);
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
