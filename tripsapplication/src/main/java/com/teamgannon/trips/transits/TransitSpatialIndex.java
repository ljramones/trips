package com.teamgannon.trips.transits;

import com.teamgannon.trips.transits.kdtree.KDPoint;
import com.teamgannon.trips.transits.kdtree.KDTree3D;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Spatial index for transit segments using a KD-tree.
 * <p>
 * This index enables efficient spatial queries on transit segments:
 * <ul>
 *   <li>Find all transits within a given radius of a point</li>
 *   <li>Find transits potentially visible in a viewport</li>
 *   <li>Cull transits that are completely outside the view</li>
 *   <li>Find transits by band for selective rendering</li>
 * </ul>
 * <p>
 * Transits are indexed by their midpoint, with a bounding radius stored for
 * accurate intersection testing. This provides O(log n + k) query performance
 * instead of O(n) for checking all transits.
 * <p>
 * <b>Usage:</b>
 * <pre>
 * TransitSpatialIndex index = new TransitSpatialIndex();
 * index.addTransits("band1", transitRoutes);
 * List&lt;IndexedTransit&gt; visible = index.findTransitsWithinRadius(x, y, z, viewportRadius);
 * </pre>
 * <p>
 * <b>Performance:</b>
 * <ul>
 *   <li>Build: O(n log n) where n is total transits</li>
 *   <li>Query: O(log n + k) where k is transits returned</li>
 *   <li>Memory: O(n) for transit storage + O(n) for KD-tree</li>
 * </ul>
 */
@Slf4j
public class TransitSpatialIndex {

    /**
     * All indexed transits, keyed by band ID.
     */
    private final Map<String, List<IndexedTransit>> transitsByBand = new HashMap<>();

    /**
     * The KD-tree for spatial queries.
     * Rebuilt when transits are added/removed (lazy rebuild on next query).
     */
    private KDTree3D<IndexedTransit> kdTree;

    /**
     * Flag indicating the tree needs rebuilding.
     */
    private boolean dirty = true;

    /**
     * Total number of transits indexed.
     */
    @Getter
    private int totalTransits = 0;

    /**
     * Statistics for performance monitoring.
     */
    @Getter
    private int queryCount = 0;
    @Getter
    private int transitsChecked = 0;
    @Getter
    private int transitsReturned = 0;

    // =========================================================================
    // Index Building
    // =========================================================================

    /**
     * Adds transits for a band to the index.
     *
     * @param bandId        the band identifier
     * @param transitRoutes the transit routes to add
     */
    public void addTransits(@NotNull String bandId, @NotNull Collection<TransitRoute> transitRoutes) {
        List<IndexedTransit> indexed = new ArrayList<>();

        for (TransitRoute transitRoute : transitRoutes) {
            if (transitRoute.getSource() == null || transitRoute.getTarget() == null) {
                log.warn("Transit with null source/target skipped");
                continue;
            }
            if (transitRoute.getSourceEndpoint() == null || transitRoute.getTargetEndpoint() == null) {
                log.warn("Transit with null endpoints skipped: {}", transitRoute.getName());
                continue;
            }

            IndexedTransit transit = IndexedTransit.create(bandId, transitRoute);
            indexed.add(transit);
        }

        if (!indexed.isEmpty()) {
            transitsByBand.put(bandId, indexed);
            totalTransits += indexed.size();
            dirty = true;
            log.debug("Indexed {} transits for band '{}'", indexed.size(), bandId);
        }
    }

    /**
     * Removes all transits for a band from the index.
     *
     * @param bandId the band identifier
     */
    public void removeBand(@NotNull String bandId) {
        List<IndexedTransit> removed = transitsByBand.remove(bandId);
        if (removed != null) {
            totalTransits -= removed.size();
            dirty = true;
            log.debug("Removed {} transits for band '{}'", removed.size(), bandId);
        }
    }

    /**
     * Clears all transits from the index.
     */
    public void clear() {
        transitsByBand.clear();
        kdTree = null;
        dirty = true;
        totalTransits = 0;
        resetStatistics();
        log.debug("Cleared transit spatial index");
    }

    /**
     * Rebuilds the KD-tree from current transits.
     * Called lazily on first query after modifications.
     */
    private void rebuildTreeIfNeeded() {
        if (!dirty) {
            return;
        }

        List<KDPoint<IndexedTransit>> points = new ArrayList<>(totalTransits);

        for (List<IndexedTransit> transits : transitsByBand.values()) {
            for (IndexedTransit transit : transits) {
                KDPoint<IndexedTransit> point = new KDPoint<>(
                        transit.getMidpointCoordinates(),
                        transit
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
            log.debug("Built KD-tree with {} transits in {}ms", points.size(), buildTime);
        }

        dirty = false;
    }

    // =========================================================================
    // Spatial Queries
    // =========================================================================

    /**
     * Finds all transits whose bounding sphere intersects with the query sphere.
     * <p>
     * This is the primary method for viewport culling.
     *
     * @param centerX query center X coordinate
     * @param centerY query center Y coordinate
     * @param centerZ query center Z coordinate
     * @param radius  query sphere radius
     * @return list of transits potentially visible
     */
    public @NotNull List<IndexedTransit> findTransitsWithinRadius(
            double centerX, double centerY, double centerZ, double radius) {

        rebuildTreeIfNeeded();
        queryCount++;

        if (kdTree == null || kdTree.isEmpty()) {
            return Collections.emptyList();
        }

        // Query with expanded radius to account for transit bounding spheres
        double maxTransitRadius = getMaxTransitRadius();
        double searchRadius = radius + maxTransitRadius;

        List<KDPoint<IndexedTransit>> candidates = kdTree.rangeSearch(
                centerX, centerY, centerZ, searchRadius);

        transitsChecked += candidates.size();

        // Filter candidates by actual bounding sphere intersection
        List<IndexedTransit> results = new ArrayList<>();
        for (KDPoint<IndexedTransit> candidate : candidates) {
            IndexedTransit transit = candidate.data();
            if (transit.intersectsBoundingSphere(centerX, centerY, centerZ, radius)) {
                results.add(transit);
            }
        }

        transitsReturned += results.size();
        return results;
    }

    /**
     * Finds transits for a specific band within the query sphere.
     *
     * @param bandId  band to filter by
     * @param centerX query center X
     * @param centerY query center Y
     * @param centerZ query center Z
     * @param radius  query radius
     * @return transits from the specified band within the radius
     */
    public @NotNull List<IndexedTransit> findTransitsForBandWithinRadius(
            @NotNull String bandId,
            double centerX, double centerY, double centerZ, double radius) {

        List<IndexedTransit> all = findTransitsWithinRadius(centerX, centerY, centerZ, radius);
        return all.stream()
                .filter(t -> t.bandId().equals(bandId))
                .toList();
    }

    /**
     * Gets all transits for a specific band (no spatial filtering).
     *
     * @param bandId the band ID
     * @return list of transits, or empty list if band not found
     */
    public @NotNull List<IndexedTransit> getTransitsForBand(@NotNull String bandId) {
        List<IndexedTransit> transits = transitsByBand.get(bandId);
        return transits != null ? new ArrayList<>(transits) : Collections.emptyList();
    }

    /**
     * Finds the nearest transit to a point.
     *
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     * @return the nearest transit, or null if index is empty
     */
    public @Nullable IndexedTransit findNearestTransit(double x, double y, double z) {
        rebuildTreeIfNeeded();

        if (kdTree == null || kdTree.isEmpty()) {
            return null;
        }

        KDPoint<IndexedTransit> nearest = kdTree.nearestNeighbor(new double[]{x, y, z});
        return nearest != null ? nearest.data() : null;
    }

    /**
     * Gets the set of band IDs that have transits within the query sphere.
     *
     * @param centerX query center X
     * @param centerY query center Y
     * @param centerZ query center Z
     * @param radius  query radius
     * @return set of band IDs with visible transits
     */
    public @NotNull Set<String> getVisibleBandIds(
            double centerX, double centerY, double centerZ, double radius) {

        List<IndexedTransit> visible = findTransitsWithinRadius(centerX, centerY, centerZ, radius);
        Set<String> bandIds = new HashSet<>();
        for (IndexedTransit transit : visible) {
            bandIds.add(transit.bandId());
        }
        return bandIds;
    }

    // =========================================================================
    // Statistics and Utilities
    // =========================================================================

    /**
     * Returns the maximum bounding radius among all indexed transits.
     */
    private double getMaxTransitRadius() {
        double max = 0;
        for (List<IndexedTransit> transits : transitsByBand.values()) {
            for (IndexedTransit transit : transits) {
                max = Math.max(max, transit.boundingRadius());
            }
        }
        return max;
    }

    /**
     * Returns the number of bands in the index.
     */
    public int getBandCount() {
        return transitsByBand.size();
    }

    /**
     * Checks if the index is empty.
     */
    public boolean isEmpty() {
        return totalTransits == 0;
    }

    /**
     * Checks if a band exists in the index.
     *
     * @param bandId the band ID to check
     * @return true if the band exists
     */
    public boolean hasBand(@NotNull String bandId) {
        return transitsByBand.containsKey(bandId);
    }

    /**
     * Gets the transit count for a specific band.
     *
     * @param bandId the band ID
     * @return transit count, or 0 if band not found
     */
    public int getTransitCountForBand(@NotNull String bandId) {
        List<IndexedTransit> transits = transitsByBand.get(bandId);
        return transits != null ? transits.size() : 0;
    }

    /**
     * Resets query statistics.
     */
    public void resetStatistics() {
        queryCount = 0;
        transitsChecked = 0;
        transitsReturned = 0;
    }

    /**
     * Returns a statistics summary string.
     */
    public @NotNull String getStatistics() {
        double avgReturned = queryCount > 0 ? (double) transitsReturned / queryCount : 0;
        double cullRate = transitsChecked > 0
                ? (1.0 - (double) transitsReturned / transitsChecked) * 100
                : 0;
        return String.format(
                "TransitSpatialIndex[bands=%d, transits=%d, queries=%d, " +
                        "avgReturned=%.1f, cullRate=%.1f%%]",
                transitsByBand.size(), totalTransits, queryCount, avgReturned, cullRate);
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
