package com.teamgannon.trips.transits.kdtree;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.measure.TrackExecutionTime;
import com.teamgannon.trips.transits.ITransitDistanceCalculator;
import com.teamgannon.trips.transits.TransitRangeDef;
import com.teamgannon.trips.transits.TransitRoute;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * High-performance transit calculator using KD-Tree spatial indexing with parallel queries.
 * <p>
 * Complexity:
 * <ul>
 *   <li>Construction: O(n log n)</li>
 *   <li>All queries: O(n log n) sequential, O(n log n / p) parallel where p = processors</li>
 * </ul>
 * <p>
 * This is significantly faster than the O(n²) brute-force approach for large star counts.
 * For small counts (< 100 stars), the overhead may not be worth it.
 *
 * @see ITransitDistanceCalculator
 * @see KDTree3D
 */
@Slf4j
public class KDTreeTransitCalculator implements ITransitDistanceCalculator {

    /**
     * Minimum star count to use parallel processing.
     * Below this threshold, sequential processing is faster due to overhead.
     */
    private static final int PARALLEL_THRESHOLD = 500;

    /**
     * Whether to use parallel processing for large datasets.
     */
    private final boolean enableParallel;

    /**
     * Creates a calculator with parallel processing enabled.
     */
    public KDTreeTransitCalculator() {
        this(true);
    }

    /**
     * Creates a calculator with configurable parallel processing.
     *
     * @param enableParallel true to enable parallel processing for large datasets
     */
    public KDTreeTransitCalculator(boolean enableParallel) {
        this.enableParallel = enableParallel;
    }

    @Override
    @TrackExecutionTime
    public @NotNull List<TransitRoute> calculateDistances(@NotNull TransitRangeDef transitRangeDef,
                                                          @NotNull List<StarDisplayRecord> starsInView) {
        if (starsInView.isEmpty()) {
            return List.of();
        }

        log.debug("Calculating transits for {} stars using KD-Tree (range: {}-{} ly)",
                starsInView.size(),
                transitRangeDef.getLowerRange(),
                transitRangeDef.getUpperRange());

        // Build KD-Tree - O(n log n)
        long startBuild = System.nanoTime();
        KDTree3D<StarDisplayRecord> tree = buildTree(starsInView);
        long buildTime = System.nanoTime() - startBuild;
        log.debug("KD-Tree built in {} ms", buildTime / 1_000_000.0);

        // Query for all transits - O(n log n) or O(n log n / p) parallel
        long startQuery = System.nanoTime();
        List<TransitRoute> routes;
        if (enableParallel && starsInView.size() >= PARALLEL_THRESHOLD) {
            routes = findTransitsParallel(tree, starsInView, transitRangeDef);
        } else {
            routes = findTransitsSequential(tree, starsInView, transitRangeDef);
        }
        long queryTime = System.nanoTime() - startQuery;
        log.debug("Transit queries completed in {} ms, found {} routes",
                queryTime / 1_000_000.0, routes.size());

        return routes;
    }

    /**
     * Calculates transits for multiple bands efficiently by reusing the KD-Tree.
     *
     * @param bands       the transit band definitions
     * @param starsInView the stars to check
     * @return map of band ID to transit routes
     */
    @TrackExecutionTime
    public @NotNull List<TransitRoute> calculateDistancesMultiBand(
            @NotNull List<TransitRangeDef> bands,
            @NotNull List<StarDisplayRecord> starsInView) {

        if (starsInView.isEmpty() || bands.isEmpty()) {
            return List.of();
        }

        // Find maximum range across all enabled bands
        double maxRange = bands.stream()
                .filter(TransitRangeDef::isEnabled)
                .mapToDouble(TransitRangeDef::getUpperRange)
                .max()
                .orElse(0);

        if (maxRange <= 0) {
            return List.of();
        }

        // Build ONE tree for all bands
        KDTree3D<StarDisplayRecord> tree = buildTree(starsInView);

        // Find all pairs within max range, then filter by band
        Set<String> seen = StarPairKey.createTrackingSet();
        List<TransitRoute> allRoutes = new ArrayList<>();

        for (StarDisplayRecord star : starsInView) {
            double[] coords = star.getActualCoordinates();
            List<KDPoint<StarDisplayRecord>> neighbors = tree.rangeSearch(coords, maxRange);

            for (KDPoint<StarDisplayRecord> neighbor : neighbors) {
                StarDisplayRecord target = neighbor.data();
                if (star == target) continue;

                if (!StarPairKey.addIfAbsent(seen, star.getStarName(), target.getStarName())) continue;

                double distance = neighbor.distanceTo(coords);

                // Find which band this distance belongs to
                for (TransitRangeDef band : bands) {
                    if (!band.isEnabled()) continue;
                    if (distance > band.getLowerRange() && distance < band.getUpperRange()) {
                        allRoutes.add(createRoute(star, target, distance, band));
                        break; // Only add to first matching band
                    }
                }
            }
        }

        return allRoutes;
    }

    // =========================================================================
    // Private Implementation
    // =========================================================================

    private @NotNull KDTree3D<StarDisplayRecord> buildTree(@NotNull List<StarDisplayRecord> stars) {
        List<KDPoint<StarDisplayRecord>> points = stars.stream()
                .map(star -> new KDPoint<>(star.getActualCoordinates(), star))
                .collect(Collectors.toList());
        return new KDTree3D<>(points);
    }

    private @NotNull List<TransitRoute> findTransitsSequential(
            @NotNull KDTree3D<StarDisplayRecord> tree,
            @NotNull List<StarDisplayRecord> stars,
            @NotNull TransitRangeDef rangeDef) {

        Set<String> seen = StarPairKey.createTrackingSet();
        List<TransitRoute> routes = new ArrayList<>();
        double upperRange = rangeDef.getUpperRange();
        double lowerRange = rangeDef.getLowerRange();

        for (StarDisplayRecord star : stars) {
            double[] coords = star.getActualCoordinates();
            List<KDPoint<StarDisplayRecord>> neighbors = tree.rangeSearch(coords, upperRange);

            for (KDPoint<StarDisplayRecord> neighbor : neighbors) {
                StarDisplayRecord target = neighbor.data();
                if (star == target) continue;

                if (!StarPairKey.addIfAbsent(seen, star.getStarName(), target.getStarName())) continue;

                double distance = neighbor.distanceTo(coords);
                if (distance > lowerRange) {
                    routes.add(createRoute(star, target, distance, rangeDef));
                }
            }
        }

        return routes;
    }

    private @NotNull List<TransitRoute> findTransitsParallel(
            @NotNull KDTree3D<StarDisplayRecord> tree,
            @NotNull List<StarDisplayRecord> stars,
            @NotNull TransitRangeDef rangeDef) {

        Set<String> seen = StarPairKey.createTrackingSet();
        double upperRange = rangeDef.getUpperRange();
        double lowerRange = rangeDef.getLowerRange();

        return stars.parallelStream()
                .flatMap(star -> {
                    double[] coords = star.getActualCoordinates();
                    List<KDPoint<StarDisplayRecord>> neighbors = tree.rangeSearch(coords, upperRange);

                    return neighbors.stream()
                            .filter(neighbor -> neighbor.data() != star)
                            .filter(neighbor -> StarPairKey.addIfAbsent(seen, star.getStarName(), neighbor.data().getStarName()))
                            .map(neighbor -> {
                                double distance = neighbor.distanceTo(coords);
                                if (distance > lowerRange) {
                                    return createRoute(star, neighbor.data(), distance, rangeDef);
                                }
                                return null;
                            })
                            .filter(route -> route != null);
                })
                .collect(Collectors.toList());
    }

    private @NotNull TransitRoute createRoute(@NotNull StarDisplayRecord source,
                                               @NotNull StarDisplayRecord target,
                                               double distance,
                                               @NotNull TransitRangeDef rangeDef) {
        return TransitRoute.builder()
                .good(true)
                .source(source)
                .target(target)
                .distance(distance)
                .lineWeight(rangeDef.getLineWidth())
                .color(rangeDef.getBandColor())
                .build();
    }
}
