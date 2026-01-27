package com.teamgannon.trips.routing.routemanagement;

import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.transits.kdtree.KDPoint;
import com.teamgannon.trips.transits.kdtree.KDTree3D;
import javafx.geometry.Point3D;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Spatial index for route segments using a KD-tree.
 * <p>
 * This index enables efficient spatial queries on route segments:
 * <ul>
 *   <li>Find all segments within a given radius of a point</li>
 *   <li>Find segments potentially visible in a viewport</li>
 *   <li>Cull segments that are completely outside the view</li>
 * </ul>
 * <p>
 * Segments are indexed by their midpoint, with a bounding radius stored for
 * accurate intersection testing. This provides O(log n + k) query performance
 * instead of O(n) for checking all segments.
 * <p>
 * <b>Usage:</b>
 * <pre>
 * RouteSegmentSpatialIndex index = new RouteSegmentSpatialIndex();
 * index.addRoute(routeDescriptor);
 * List&lt;IndexedRouteSegment&gt; visible = index.findSegmentsWithinRadius(x, y, z, viewportRadius);
 * </pre>
 * <p>
 * <b>Performance:</b>
 * <ul>
 *   <li>Build: O(n log n) where n is total segments</li>
 *   <li>Query: O(log n + k) where k is segments returned</li>
 *   <li>Memory: O(n) for segment storage + O(n) for KD-tree</li>
 * </ul>
 */
@Slf4j
public class RouteSegmentSpatialIndex {

    /**
     * All indexed segments, keyed by route ID then segment index.
     */
    private final Map<UUID, List<IndexedRouteSegment>> segmentsByRoute = new HashMap<>();

    /**
     * The KD-tree for spatial queries.
     * Rebuilt when routes are added/removed (lazy rebuild on next query).
     */
    private KDTree3D<IndexedRouteSegment> kdTree;

    /**
     * Flag indicating the tree needs rebuilding.
     */
    private boolean dirty = true;

    /**
     * Total number of segments indexed.
     */
    @Getter
    private int totalSegments = 0;

    /**
     * Statistics for performance monitoring.
     */
    @Getter
    private int queryCount = 0;
    @Getter
    private int segmentsChecked = 0;
    @Getter
    private int segmentsReturned = 0;

    // =========================================================================
    // Index Building
    // =========================================================================

    /**
     * Adds a route to the index.
     * <p>
     * Extracts all segments from the route's coordinates and indexes them.
     * The KD-tree is marked dirty and will be rebuilt on next query.
     *
     * @param routeDescriptor the route to add
     */
    public void addRoute(@NotNull RouteDescriptor routeDescriptor) {
        List<Point3D> coordinates = routeDescriptor.getRouteCoordinates();
        if (coordinates == null || coordinates.size() < 2) {
            log.debug("Route {} has insufficient coordinates for indexing", routeDescriptor.getName());
            return;
        }

        List<IndexedRouteSegment> segments = new ArrayList<>();
        UUID routeId = routeDescriptor.getId();

        for (int i = 0; i < coordinates.size() - 1; i++) {
            Point3D start = coordinates.get(i);
            Point3D end = coordinates.get(i + 1);

            // Skip invalid coordinates
            if (start == null || end == null) {
                log.warn("Null coordinate in route {} at index {}", routeDescriptor.getName(), i);
                continue;
            }

            IndexedRouteSegment segment = IndexedRouteSegment.create(routeId, i, start, end);
            segments.add(segment);
        }

        if (!segments.isEmpty()) {
            segmentsByRoute.put(routeId, segments);
            totalSegments += segments.size();
            dirty = true;
            log.debug("Indexed {} segments for route '{}'", segments.size(), routeDescriptor.getName());
        }
    }

    /**
     * Adds multiple routes to the index.
     *
     * @param routes the routes to add
     */
    public void addRoutes(@NotNull Collection<RouteDescriptor> routes) {
        for (RouteDescriptor route : routes) {
            addRoute(route);
        }
    }

    /**
     * Removes a route from the index.
     *
     * @param routeId the route ID to remove
     */
    public void removeRoute(@NotNull UUID routeId) {
        List<IndexedRouteSegment> removed = segmentsByRoute.remove(routeId);
        if (removed != null) {
            totalSegments -= removed.size();
            dirty = true;
            log.debug("Removed {} segments for route {}", removed.size(), routeId);
        }
    }

    /**
     * Clears all routes from the index.
     */
    public void clear() {
        segmentsByRoute.clear();
        kdTree = null;
        dirty = true;
        totalSegments = 0;
        resetStatistics();
        log.debug("Cleared route spatial index");
    }

    /**
     * Rebuilds the KD-tree from current segments.
     * Called lazily on first query after modifications.
     */
    private void rebuildTreeIfNeeded() {
        if (!dirty) {
            return;
        }

        List<KDPoint<IndexedRouteSegment>> points = new ArrayList<>(totalSegments);

        for (List<IndexedRouteSegment> segments : segmentsByRoute.values()) {
            for (IndexedRouteSegment segment : segments) {
                KDPoint<IndexedRouteSegment> point = new KDPoint<>(
                        segment.getMidpointCoordinates(),
                        segment
                );
                points.add(point);
            }
        }

        if (points.isEmpty()) {
            kdTree = null;
        } else {
            long startTime = System.currentTimeMillis();
            kdTree = new KDTree3D<>(points);
            long buildTime = System.currentTimeMillis() - startTime;
            log.debug("Built KD-tree with {} segments in {}ms", points.size(), buildTime);
        }

        dirty = false;
    }

    // =========================================================================
    // Spatial Queries
    // =========================================================================

    /**
     * Finds all segments whose bounding sphere intersects with the query sphere.
     * <p>
     * This is the primary method for viewport culling. Use a query radius that
     * encompasses the visible viewport diagonal.
     *
     * @param centerX query center X coordinate
     * @param centerY query center Y coordinate
     * @param centerZ query center Z coordinate
     * @param radius  query sphere radius
     * @return list of segments potentially visible (bounding sphere intersection)
     */
    public @NotNull List<IndexedRouteSegment> findSegmentsWithinRadius(
            double centerX, double centerY, double centerZ, double radius) {

        rebuildTreeIfNeeded();
        queryCount++;

        if (kdTree == null || kdTree.isEmpty()) {
            return Collections.emptyList();
        }

        // Query with expanded radius to account for segment bounding spheres
        // We expand by the maximum possible segment radius to ensure we don't miss any
        double maxSegmentRadius = getMaxSegmentRadius();
        double searchRadius = radius + maxSegmentRadius;

        List<KDPoint<IndexedRouteSegment>> candidates = kdTree.rangeSearch(
                centerX, centerY, centerZ, searchRadius);

        segmentsChecked += candidates.size();

        // Filter candidates by actual bounding sphere intersection
        List<IndexedRouteSegment> results = new ArrayList<>();
        for (KDPoint<IndexedRouteSegment> candidate : candidates) {
            IndexedRouteSegment segment = candidate.data();
            if (segment.intersectsBoundingSphere(centerX, centerY, centerZ, radius)) {
                results.add(segment);
            }
        }

        segmentsReturned += results.size();
        return results;
    }

    /**
     * Finds all segments for a specific route within the query sphere.
     *
     * @param routeId route to filter by
     * @param centerX query center X
     * @param centerY query center Y
     * @param centerZ query center Z
     * @param radius  query radius
     * @return segments from the specified route within the radius
     */
    public @NotNull List<IndexedRouteSegment> findSegmentsForRouteWithinRadius(
            @NotNull UUID routeId,
            double centerX, double centerY, double centerZ, double radius) {

        List<IndexedRouteSegment> all = findSegmentsWithinRadius(centerX, centerY, centerZ, radius);
        return all.stream()
                .filter(s -> s.routeId().equals(routeId))
                .toList();
    }

    /**
     * Gets all segments for a specific route (no spatial filtering).
     *
     * @param routeId the route ID
     * @return list of segments, or empty list if route not found
     */
    public @NotNull List<IndexedRouteSegment> getSegmentsForRoute(@NotNull UUID routeId) {
        List<IndexedRouteSegment> segments = segmentsByRoute.get(routeId);
        return segments != null ? new ArrayList<>(segments) : Collections.emptyList();
    }

    /**
     * Finds the nearest segment to a point.
     *
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     * @return the nearest segment, or null if index is empty
     */
    public @Nullable IndexedRouteSegment findNearestSegment(double x, double y, double z) {
        rebuildTreeIfNeeded();

        if (kdTree == null || kdTree.isEmpty()) {
            return null;
        }

        KDPoint<IndexedRouteSegment> nearest = kdTree.nearestNeighbor(new double[]{x, y, z});
        return nearest != null ? nearest.data() : null;
    }

    /**
     * Gets the set of route IDs that have segments within the query sphere.
     *
     * @param centerX query center X
     * @param centerY query center Y
     * @param centerZ query center Z
     * @param radius  query radius
     * @return set of route IDs with visible segments
     */
    public @NotNull Set<UUID> getVisibleRouteIds(
            double centerX, double centerY, double centerZ, double radius) {

        List<IndexedRouteSegment> visible = findSegmentsWithinRadius(centerX, centerY, centerZ, radius);
        Set<UUID> routeIds = new HashSet<>();
        for (IndexedRouteSegment segment : visible) {
            routeIds.add(segment.routeId());
        }
        return routeIds;
    }

    // =========================================================================
    // Statistics and Utilities
    // =========================================================================

    /**
     * Returns the maximum bounding radius among all indexed segments.
     * Used to expand query radius for accurate results.
     */
    private double getMaxSegmentRadius() {
        double max = 0;
        for (List<IndexedRouteSegment> segments : segmentsByRoute.values()) {
            for (IndexedRouteSegment segment : segments) {
                max = Math.max(max, segment.boundingRadius());
            }
        }
        return max;
    }

    /**
     * Returns the number of routes in the index.
     */
    public int getRouteCount() {
        return segmentsByRoute.size();
    }

    /**
     * Checks if the index is empty.
     */
    public boolean isEmpty() {
        return totalSegments == 0;
    }

    /**
     * Resets query statistics.
     */
    public void resetStatistics() {
        queryCount = 0;
        segmentsChecked = 0;
        segmentsReturned = 0;
    }

    /**
     * Returns a statistics summary string.
     */
    public @NotNull String getStatistics() {
        double avgReturned = queryCount > 0 ? (double) segmentsReturned / queryCount : 0;
        double cullRate = segmentsChecked > 0
                ? (1.0 - (double) segmentsReturned / segmentsChecked) * 100
                : 0;
        return (
                """
                RouteSegmentSpatialIndex[routes=%d, segments=%d, queries=%d, \
                avgReturned=%.1f, cullRate=%.1f%%]\
                """).formatted(
                segmentsByRoute.size(), totalSegments, queryCount, avgReturned, cullRate);
    }

    /**
     * Logs statistics at debug level.
     */
    public void logStatistics() {
        if (queryCount > 0) {
            log.debug(getStatistics());
        }
    }
}
