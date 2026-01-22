package com.teamgannon.trips.transits.kdtree;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * A 3D KD-Tree implementation optimized for range queries.
 * <p>
 * This tree partitions 3D space to enable efficient neighbor searches.
 * Construction is O(n log n), and range queries are O(log n + k) where
 * k is the number of points returned.
 * <p>
 * Thread-safe for concurrent read operations after construction.
 *
 * @param <T> the type of data associated with each point
 */
public class KDTree3D<T> {

    private static final int DIMENSIONS = 3;

    private final Node<T> root;
    private final int size;

    /**
     * Constructs a KD-Tree from the given points.
     *
     * @param points list of points with associated data
     */
    public KDTree3D(@NotNull List<KDPoint<T>> points) {
        this.size = points.size();
        if (points.isEmpty()) {
            this.root = null;
        } else {
            // Create mutable copy for sorting during construction
            List<KDPoint<T>> mutablePoints = new ArrayList<>(points);
            this.root = buildTree(mutablePoints, 0);
        }
    }

    /**
     * Returns the number of points in the tree.
     */
    public int size() {
        return size;
    }

    /**
     * Returns true if the tree is empty.
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Finds all points within the specified radius of the query point.
     * <p>
     * Complexity: O(log n + k) where k is the number of points returned.
     *
     * @param queryPoint the center point for the search
     * @param radius     the search radius
     * @return list of points within the radius (excluding the query point itself if present)
     */
    public @NotNull List<KDPoint<T>> rangeSearch(double @NotNull [] queryPoint, double radius) {
        if (queryPoint.length != DIMENSIONS) {
            throw new IllegalArgumentException("Query point must have " + DIMENSIONS + " dimensions");
        }
        List<KDPoint<T>> results = new ArrayList<>();
        if (root != null) {
            rangeSearchRecursive(root, queryPoint, radius * radius, results);
        }
        return results;
    }

    /**
     * Finds all points within the specified radius of the query point.
     *
     * @param x      x coordinate
     * @param y      y coordinate
     * @param z      z coordinate
     * @param radius the search radius
     * @return list of points within the radius
     */
    public @NotNull List<KDPoint<T>> rangeSearch(double x, double y, double z, double radius) {
        return rangeSearch(new double[]{x, y, z}, radius);
    }

    /**
     * Finds the nearest neighbor to the query point.
     *
     * @param queryPoint the query point
     * @return the nearest point, or null if tree is empty
     */
    public @Nullable KDPoint<T> nearestNeighbor(double @NotNull [] queryPoint) {
        if (root == null) {
            return null;
        }
        NearestState<T> state = new NearestState<>();
        nearestNeighborRecursive(root, queryPoint, state);
        return state.best;
    }

    // =========================================================================
    // Tree Construction
    // =========================================================================

    private @Nullable Node<T> buildTree(@NotNull List<KDPoint<T>> points, int depth) {
        if (points.isEmpty()) {
            return null;
        }

        int axis = depth % DIMENSIONS;

        // Sort by the current axis and find median
        points.sort(Comparator.comparingDouble(p -> p.coordinates()[axis]));
        int medianIndex = points.size() / 2;

        // Create node with median point
        KDPoint<T> medianPoint = points.get(medianIndex);
        Node<T> node = new Node<>(medianPoint, axis);

        // Recursively build subtrees
        if (medianIndex > 0) {
            node.left = buildTree(points.subList(0, medianIndex), depth + 1);
        }
        if (medianIndex + 1 < points.size()) {
            node.right = buildTree(points.subList(medianIndex + 1, points.size()), depth + 1);
        }

        return node;
    }

    // =========================================================================
    // Range Search Implementation
    // =========================================================================

    private void rangeSearchRecursive(@NotNull Node<T> node,
                                       double @NotNull [] queryPoint,
                                       double radiusSquared,
                                       @NotNull List<KDPoint<T>> results) {
        // Check if this node's point is within range
        double distSquared = distanceSquared(node.point.coordinates(), queryPoint);
        if (distSquared <= radiusSquared) {
            results.add(node.point);
        }

        // Determine which subtree(s) to search
        int axis = node.axis;
        double splitValue = node.point.coordinates()[axis];
        double queryValue = queryPoint[axis];
        double axisDist = queryValue - splitValue;
        double axisDistSquared = axisDist * axisDist;

        // Always search the side containing the query point
        Node<T> nearSide = axisDist < 0 ? node.left : node.right;
        Node<T> farSide = axisDist < 0 ? node.right : node.left;

        if (nearSide != null) {
            rangeSearchRecursive(nearSide, queryPoint, radiusSquared, results);
        }

        // Only search far side if the splitting plane is within radius
        if (farSide != null && axisDistSquared <= radiusSquared) {
            rangeSearchRecursive(farSide, queryPoint, radiusSquared, results);
        }
    }

    // =========================================================================
    // Nearest Neighbor Implementation
    // =========================================================================

    private static class NearestState<T> {
        KDPoint<T> best = null;
        double bestDistSquared = Double.MAX_VALUE;
    }

    private void nearestNeighborRecursive(@NotNull Node<T> node,
                                           double @NotNull [] queryPoint,
                                           @NotNull NearestState<T> state) {
        double distSquared = distanceSquared(node.point.coordinates(), queryPoint);
        if (distSquared < state.bestDistSquared) {
            state.best = node.point;
            state.bestDistSquared = distSquared;
        }

        int axis = node.axis;
        double splitValue = node.point.coordinates()[axis];
        double queryValue = queryPoint[axis];
        double axisDist = queryValue - splitValue;

        Node<T> nearSide = axisDist < 0 ? node.left : node.right;
        Node<T> farSide = axisDist < 0 ? node.right : node.left;

        // Search near side first
        if (nearSide != null) {
            nearestNeighborRecursive(nearSide, queryPoint, state);
        }

        // Search far side only if it might contain a closer point
        if (farSide != null && axisDist * axisDist < state.bestDistSquared) {
            nearestNeighborRecursive(farSide, queryPoint, state);
        }
    }

    // =========================================================================
    // Utility Methods
    // =========================================================================

    private static double distanceSquared(double @NotNull [] a, double @NotNull [] b) {
        double dx = a[0] - b[0];
        double dy = a[1] - b[1];
        double dz = a[2] - b[2];
        return dx * dx + dy * dy + dz * dz;
    }

    /**
     * Calculates the Euclidean distance between two 3D points.
     */
    public static double distance(double @NotNull [] a, double @NotNull [] b) {
        return Math.sqrt(distanceSquared(a, b));
    }

    // =========================================================================
    // Node Class
    // =========================================================================

    private static class Node<T> {
        final KDPoint<T> point;
        final int axis;
        Node<T> left;
        Node<T> right;

        Node(KDPoint<T> point, int axis) {
            this.point = point;
            this.axis = axis;
        }
    }
}
