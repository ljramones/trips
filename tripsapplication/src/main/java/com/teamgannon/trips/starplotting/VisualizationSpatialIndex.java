package com.teamgannon.trips.starplotting;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.transits.kdtree.KDPoint;
import com.teamgannon.trips.transits.kdtree.KDTree3D;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Spatial index for star visualization using KD-Tree.
 * <p>
 * Provides efficient spatial queries for:
 * <ul>
 *   <li>Viewport frustum culling - only render stars within view distance</li>
 *   <li>Distance-based filtering - find stars within a range</li>
 *   <li>Nearest neighbor queries - find closest stars to a point</li>
 *   <li>Label visibility optimization - find top N stars by score within range</li>
 * </ul>
 * <p>
 * Complexity:
 * <ul>
 *   <li>Construction: O(n log n)</li>
 *   <li>Range queries: O(log n + k) where k = results returned</li>
 *   <li>Nearest neighbor: O(log n)</li>
 * </ul>
 * <p>
 * The index is immutable after construction. Rebuild when the star list changes.
 *
 * @see KDTree3D
 */
@Slf4j
public class VisualizationSpatialIndex {

    /**
     * Minimum star count to justify building a KD-tree.
     * Below this threshold, linear search is faster due to lower overhead.
     */
    private static final int MIN_STARS_FOR_TREE = 50;

    private final KDTree3D<StarDisplayRecord> tree;
    private final List<StarDisplayRecord> stars;
    private final boolean useTree;

    /**
     * Creates a spatial index from the given stars.
     *
     * @param stars the stars to index
     */
    public VisualizationSpatialIndex(@NotNull List<StarDisplayRecord> stars) {
        this.stars = new ArrayList<>(stars);
        this.useTree = stars.size() >= MIN_STARS_FOR_TREE;

        if (useTree) {
            long startTime = System.nanoTime();
            List<KDPoint<StarDisplayRecord>> points = stars.stream()
                    .map(star -> new KDPoint<>(star.getActualCoordinates(), star))
                    .collect(Collectors.toList());
            this.tree = new KDTree3D<>(points);
            long buildTime = System.nanoTime() - startTime;
            log.debug("Built spatial index for {} stars in {:.2f} ms", stars.size(), buildTime / 1_000_000.0);
        } else {
            this.tree = null;
            log.debug("Using linear search for {} stars (below threshold)", stars.size());
        }
    }

    /**
     * Returns the total number of indexed stars.
     */
    public int size() {
        return stars.size();
    }

    /**
     * Returns true if the index is empty.
     */
    public boolean isEmpty() {
        return stars.isEmpty();
    }

    /**
     * Returns all indexed stars.
     */
    public @NotNull List<StarDisplayRecord> getAllStars() {
        return new ArrayList<>(stars);
    }

    // =========================================================================
    // Range Queries
    // =========================================================================

    /**
     * Finds all stars within the specified radius of a point.
     * <p>
     * Use this for viewport frustum culling - only render stars within view distance.
     *
     * @param centerX center X coordinate
     * @param centerY center Y coordinate
     * @param centerZ center Z coordinate
     * @param radius  the search radius (in light-years)
     * @return stars within the radius
     */
    public @NotNull List<StarDisplayRecord> findStarsWithinRadius(double centerX, double centerY, double centerZ, double radius) {
        if (useTree) {
            return tree.rangeSearch(centerX, centerY, centerZ, radius).stream()
                    .map(KDPoint::data)
                    .collect(Collectors.toList());
        } else {
            return findStarsWithinRadiusLinear(centerX, centerY, centerZ, radius);
        }
    }

    /**
     * Finds all stars within the specified radius of a point.
     *
     * @param center the center coordinates [x, y, z]
     * @param radius the search radius
     * @return stars within the radius
     */
    public @NotNull List<StarDisplayRecord> findStarsWithinRadius(double[] center, double radius) {
        return findStarsWithinRadius(center[0], center[1], center[2], radius);
    }

    /**
     * Finds all stars within a distance range (ring query).
     * <p>
     * Useful for finding stars in a specific distance band.
     *
     * @param centerX   center X coordinate
     * @param centerY   center Y coordinate
     * @param centerZ   center Z coordinate
     * @param minRadius minimum distance (exclusive)
     * @param maxRadius maximum distance (inclusive)
     * @return stars within the distance range
     */
    public @NotNull List<StarDisplayRecord> findStarsInRange(double centerX, double centerY, double centerZ,
                                                              double minRadius, double maxRadius) {
        List<StarDisplayRecord> candidates = findStarsWithinRadius(centerX, centerY, centerZ, maxRadius);

        if (minRadius <= 0) {
            return candidates;
        }

        double minRadiusSq = minRadius * minRadius;
        return candidates.stream()
                .filter(star -> {
                    double[] coords = star.getActualCoordinates();
                    double dx = coords[0] - centerX;
                    double dy = coords[1] - centerY;
                    double dz = coords[2] - centerZ;
                    double distSq = dx * dx + dy * dy + dz * dz;
                    return distSq > minRadiusSq;
                })
                .collect(Collectors.toList());
    }

    // =========================================================================
    // Nearest Neighbor Queries
    // =========================================================================

    /**
     * Finds the nearest star to the given point.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @return the nearest star, or null if index is empty
     */
    public @Nullable StarDisplayRecord findNearestStar(double x, double y, double z) {
        if (stars.isEmpty()) {
            return null;
        }

        if (useTree) {
            KDPoint<StarDisplayRecord> nearest = tree.nearestNeighbor(new double[]{x, y, z});
            return nearest != null ? nearest.data() : null;
        } else {
            return findNearestStarLinear(x, y, z);
        }
    }

    /**
     * Finds the N nearest stars to the given point.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @param n maximum number of stars to return
     * @return up to N nearest stars, sorted by distance (closest first)
     */
    public @NotNull List<StarDisplayRecord> findNearestStars(double x, double y, double z, int n) {
        if (stars.isEmpty() || n <= 0) {
            return List.of();
        }

        // For small N, use expanding radius search
        // For large N, sort all by distance
        if (n <= stars.size() / 4) {
            return findNearestStarsExpandingRadius(x, y, z, n);
        } else {
            return findNearestStarsSorted(x, y, z, n);
        }
    }

    // =========================================================================
    // Label Visibility Optimization
    // =========================================================================

    /**
     * Finds the top N stars by label display score within a radius.
     * <p>
     * More efficient than sorting all stars when only a subset is needed.
     *
     * @param centerX center X coordinate
     * @param centerY center Y coordinate
     * @param centerZ center Z coordinate
     * @param radius  the search radius
     * @param topN    maximum number of stars to return
     * @return top N stars by score within radius, sorted by score descending
     */
    public @NotNull List<StarDisplayRecord> findTopScoringStarsInRadius(double centerX, double centerY, double centerZ,
                                                                         double radius, int topN) {
        List<StarDisplayRecord> candidates = findStarsWithinRadius(centerX, centerY, centerZ, radius);

        if (candidates.size() <= topN) {
            candidates.sort(Comparator.comparing(StarDisplayRecord::getCurrentLabelDisplayScore).reversed());
            return candidates;
        }

        // Partial sort for top N
        return candidates.stream()
                .sorted(Comparator.comparing(StarDisplayRecord::getCurrentLabelDisplayScore).reversed())
                .limit(topN)
                .collect(Collectors.toList());
    }

    /**
     * Finds stars within radius that should display labels.
     * <p>
     * A star should display a label if:
     * <ul>
     *   <li>It is the center star</li>
     *   <li>It has a forced label</li>
     *   <li>It is in the top N by display score</li>
     * </ul>
     *
     * @param centerX    center X coordinate
     * @param centerY    center Y coordinate
     * @param centerZ    center Z coordinate
     * @param radius     the search radius
     * @param labelCount maximum number of scored labels to show
     * @return stars that should display labels
     */
    public @NotNull List<StarDisplayRecord> findStarsForLabeling(double centerX, double centerY, double centerZ,
                                                                  double radius, int labelCount) {
        List<StarDisplayRecord> candidates = findStarsWithinRadius(centerX, centerY, centerZ, radius);

        // Separate forced labels and scored labels
        List<StarDisplayRecord> forcedLabels = new ArrayList<>();
        List<StarDisplayRecord> scoredCandidates = new ArrayList<>();

        for (StarDisplayRecord star : candidates) {
            if (star.isCenter() || star.isLabelForced()) {
                forcedLabels.add(star);
            } else {
                scoredCandidates.add(star);
            }
        }

        // Get top N scored labels
        int remainingSlots = Math.max(0, labelCount - forcedLabels.size());
        List<StarDisplayRecord> topScored = scoredCandidates.stream()
                .sorted(Comparator.comparing(StarDisplayRecord::getCurrentLabelDisplayScore).reversed())
                .limit(remainingSlots)
                .collect(Collectors.toList());

        // Combine results
        List<StarDisplayRecord> result = new ArrayList<>(forcedLabels);
        result.addAll(topScored);
        return result;
    }

    // =========================================================================
    // Statistics
    // =========================================================================

    /**
     * Returns statistics about the index.
     */
    public @NotNull String getStatistics() {
        return "VisualizationSpatialIndex[stars=%d, algorithm=%s]".formatted(
                stars.size(), useTree ? "KD-Tree" : "Linear");
    }

    // =========================================================================
    // Private Implementation
    // =========================================================================

    private @NotNull List<StarDisplayRecord> findStarsWithinRadiusLinear(double cx, double cy, double cz, double radius) {
        double radiusSq = radius * radius;
        List<StarDisplayRecord> results = new ArrayList<>();

        for (StarDisplayRecord star : stars) {
            double[] coords = star.getActualCoordinates();
            double dx = coords[0] - cx;
            double dy = coords[1] - cy;
            double dz = coords[2] - cz;
            double distSq = dx * dx + dy * dy + dz * dz;
            if (distSq <= radiusSq) {
                results.add(star);
            }
        }

        return results;
    }

    private @Nullable StarDisplayRecord findNearestStarLinear(double x, double y, double z) {
        StarDisplayRecord nearest = null;
        double nearestDistSq = Double.MAX_VALUE;

        for (StarDisplayRecord star : stars) {
            double[] coords = star.getActualCoordinates();
            double dx = coords[0] - x;
            double dy = coords[1] - y;
            double dz = coords[2] - z;
            double distSq = dx * dx + dy * dy + dz * dz;
            if (distSq < nearestDistSq) {
                nearestDistSq = distSq;
                nearest = star;
            }
        }

        return nearest;
    }

    private @NotNull List<StarDisplayRecord> findNearestStarsExpandingRadius(double x, double y, double z, int n) {
        // Start with a reasonable radius and expand if needed
        double radius = 10.0; // 10 light-years initial radius
        List<StarDisplayRecord> results;

        do {
            results = findStarsWithinRadius(x, y, z, radius);
            radius *= 2;
        } while (results.size() < n && radius < 10000);

        // Sort by distance and take top N
        return sortByDistanceAndLimit(results, x, y, z, n);
    }

    private @NotNull List<StarDisplayRecord> findNearestStarsSorted(double x, double y, double z, int n) {
        return sortByDistanceAndLimit(stars, x, y, z, n);
    }

    private @NotNull List<StarDisplayRecord> sortByDistanceAndLimit(@NotNull List<StarDisplayRecord> candidates,
                                                                     double x, double y, double z, int n) {
        return candidates.stream()
                .sorted((a, b) -> {
                    double[] ca = a.getActualCoordinates();
                    double[] cb = b.getActualCoordinates();
                    double distA = (ca[0] - x) * (ca[0] - x) + (ca[1] - y) * (ca[1] - y) + (ca[2] - z) * (ca[2] - z);
                    double distB = (cb[0] - x) * (cb[0] - x) + (cb[1] - y) * (cb[1] - y) + (cb[2] - z) * (cb[2] - z);
                    return Double.compare(distA, distB);
                })
                .limit(n)
                .collect(Collectors.toList());
    }
}
