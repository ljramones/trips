package com.teamgannon.trips.routing;

import com.teamgannon.trips.dialogs.search.model.DistanceRoutes;
import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.routing.automation.RouteBuilderHelper;
import com.teamgannon.trips.routing.automation.RouteGraph;
import com.teamgannon.trips.routing.model.*;
import com.teamgannon.trips.service.measure.StarMeasurementService;
import com.teamgannon.trips.transits.TransitRoute;
import javafx.scene.paint.Color;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.teamgannon.trips.routing.RoutingConstants.KDTREE_THRESHOLD;

/**
 * Service for finding routes between stars.
 * <p>
 * This service encapsulates the route-finding algorithm without any UI dependencies.
 * It can be used by dialogs, automated processes, or tests.
 * <p>
 * <b>Caching:</b> Results are cached to avoid recalculating the same routes.
 * The cache key includes origin, destination, distance bounds, number of paths,
 * exclusions, and a hash of the available stars.
 * <p>
 * The algorithm:
 * <ol>
 *   <li>Check cache for existing result</li>
 *   <li>Prunes stars based on spectral class and polity exclusions</li>
 *   <li>Calculates transits (possible jumps) within distance bounds</li>
 *   <li>Builds a graph from the transits</li>
 *   <li>Finds K-shortest paths using Yen's algorithm</li>
 *   <li>Creates route descriptors for each path</li>
 *   <li>Cache successful results</li>
 * </ol>
 */
@Slf4j
@Service
public class RouteFindingService {

    /**
     * Maximum number of stars for route finding. Beyond this, performance degrades.
     * @deprecated Use {@link RoutingConstants#GRAPH_THRESHOLD} instead
     */
    @Deprecated
    public static final int GRAPH_THRESHOLD = RoutingConstants.GRAPH_THRESHOLD;

    private final StarMeasurementService starMeasurementService;
    private final RouteCache routeCache;

    public RouteFindingService(StarMeasurementService starMeasurementService,
                               RouteCache routeCache) {
        this.starMeasurementService = starMeasurementService;
        this.routeCache = routeCache;
    }

    /**
     * Find routes between origin and destination stars.
     *
     * @param options     the route finding options (origin, destination, bounds, exclusions)
     * @param starsInView the stars available for routing
     * @param dataSet     the current dataset (for route metadata)
     * @return result containing either the found routes or an error message
     */
    public @NotNull RouteFindingResult findRoutes(@NotNull RouteFindingOptions options,
                                                   @NotNull List<StarDisplayRecord> starsInView,
                                                   @NotNull DataSetDescriptor dataSet) {
        return findRoutes(options, starsInView, dataSet, true);
    }

    /**
     * Find routes between origin and destination stars.
     *
     * @param options     the route finding options (origin, destination, bounds, exclusions)
     * @param starsInView the stars available for routing
     * @param dataSet     the current dataset (for route metadata)
     * @param useCache    whether to use the route cache
     * @return result containing either the found routes or an error message
     */
    public @NotNull RouteFindingResult findRoutes(@NotNull RouteFindingOptions options,
                                                   @NotNull List<StarDisplayRecord> starsInView,
                                                   @NotNull DataSetDescriptor dataSet,
                                                   boolean useCache) {
        try {
            log.info("Finding route from {} to {}", options.getOriginStarName(), options.getDestinationStarName());

            // Check cache first
            RouteCacheKey cacheKey = RouteCacheKey.fromOptionsWithStars(options, starsInView);
            if (useCache) {
                Optional<RouteFindingResult> cachedResult = routeCache.get(cacheKey);
                if (cachedResult.isPresent()) {
                    log.info("Returning cached route result for {} → {}",
                            options.getOriginStarName(), options.getDestinationStarName());
                    return cachedResult.get();
                }
            }

            String origin = options.getOriginStarName();
            String destination = options.getDestinationStarName();

            // Prune stars based on exclusions
            List<StarDisplayRecord> prunedStars = pruneStars(starsInView, options);
            log.info("Pruned stars from {} to {}", starsInView.size(), prunedStars.size());

            // Validate star count
            if (prunedStars.size() > GRAPH_THRESHOLD) {
                return RouteFindingResult.failure(
                        "Too many stars (%d) to plan a route. Maximum is %d.".formatted(
                                prunedStars.size(), GRAPH_THRESHOLD));
            }

            // Build helper for star lookup
            RouteBuilderHelper routeBuilderHelper = new RouteBuilderHelper(prunedStars);

            // Validate origin star
            if (!routeBuilderHelper.has(origin)) {
                return RouteFindingResult.failure(
                        "Origin star '%s' is not in the available stars (may have been excluded).".formatted(origin));
            }

            // Validate destination star
            if (!routeBuilderHelper.has(destination)) {
                return RouteFindingResult.failure(
                        "Destination star '%s' is not in the available stars (may have been excluded).".formatted(destination));
            }

            // Build route graph using appropriate algorithm
            RouteGraph routeGraph = buildRouteGraph(prunedStars, options);

            if (routeGraph.getRoutingGraph().edgeSet().isEmpty()) {
                return RouteFindingResult.failure(
                        "No transits found with the given distance bounds. Try adjusting upper/lower bounds.");
            }

            log.info("Built graph with {} vertices and {} edges",
                    routeGraph.getRoutingGraph().vertexSet().size(),
                    routeGraph.getRoutingGraph().edgeSet().size());

            // Check connectivity
            if (!routeGraph.isConnected(origin, destination)) {
                return RouteFindingResult.failure(
                        """
                        No path exists between origin and destination with the given parameters. \
                        Try adjusting distance bounds or removing exclusions.\
                        """);
            }

            // Find K-shortest paths
            PossibleRoutes possibleRoutes = findKShortestPaths(
                    options, origin, destination, routeBuilderHelper, routeGraph, dataSet);

            if (possibleRoutes.getRoutes().isEmpty()) {
                return RouteFindingResult.failure("No valid routes found.");
            }

            log.info("Found {} routes from {} to {}", possibleRoutes.getRoutes().size(), origin, destination);
            RouteFindingResult result = RouteFindingResult.success(possibleRoutes);

            // Cache successful result
            if (useCache) {
                routeCache.put(cacheKey, result);
            }

            return result;

        } catch (Exception e) {
            log.error("Failed to find routes: {}", e.getMessage(), e);
            return RouteFindingResult.failure("Route finding failed: " + e.getMessage());
        }
    }

    /**
     * Clears the route cache.
     * <p>
     * Should be called when the dataset changes or star data is modified.
     */
    public void clearCache() {
        routeCache.clear();
    }

    /**
     * Gets cache statistics for monitoring.
     *
     * @return cache statistics string
     */
    public String getCacheStatistics() {
        return routeCache.getStatistics();
    }

    /**
     * Prune stars based on spectral class and polity exclusions.
     *
     * @param starsInView the original list of stars
     * @param options     the options containing exclusions
     * @return the filtered list of stars
     */
    public @NotNull List<StarDisplayRecord> pruneStars(@NotNull List<StarDisplayRecord> starsInView,
                                                        @NotNull RouteFindingOptions options) {
        Set<String> starExclusions = options.getStarExclusions();
        Set<String> polityExclusions = options.getPolityExclusions();

        List<StarDisplayRecord> prunedStars = new ArrayList<>();
        for (StarDisplayRecord star : starsInView) {
            if (star == null) {
                continue;
            }

            // Check spectral class exclusion
            String spectralClass = star.getSpectralClass();
            if (spectralClass != null && !spectralClass.isEmpty()) {
                String spectralType = spectralClass.substring(0, 1);
                if (starExclusions.contains(spectralType)) {
                    continue;
                }
            }

            // Check polity exclusion
            String polity = star.getPolity();
            if (polity != null && polityExclusions.contains(polity)) {
                continue;
            }

            prunedStars.add(star);
        }
        return prunedStars;
    }

    /**
     * Build route graph using the appropriate algorithm based on dataset size.
     * <p>
     * For small datasets (≤ KDTREE_THRESHOLD), uses brute-force transit calculation
     * which has lower overhead. For larger datasets, uses KD-Tree spatial indexing
     * which provides O(n log n) complexity vs O(n²) brute-force.
     *
     * @param stars   the stars to include in the graph
     * @param options the route finding options with distance bounds
     * @return the constructed route graph
     */
    private @NotNull RouteGraph buildRouteGraph(@NotNull List<StarDisplayRecord> stars,
                                                 @NotNull RouteFindingOptions options) {
        double lowerBound = options.getLowerBound();
        double upperBound = options.getUpperBound();

        if (stars.size() <= KDTREE_THRESHOLD) {
            // Use brute-force for small datasets (lower overhead)
            log.debug("Using brute-force graph building for {} stars", stars.size());
            return buildRouteGraphBruteForce(stars, lowerBound, upperBound);
        } else {
            // Use KD-Tree for larger datasets (O(n log n) vs O(n²))
            log.debug("Using KD-Tree graph building for {} stars", stars.size());
            return RouteGraph.buildWithKDTree(stars, lowerBound, upperBound);
        }
    }

    /**
     * Build route graph using brute-force transit calculation.
     * Optimal for small datasets (< 100 stars).
     */
    private @NotNull RouteGraph buildRouteGraphBruteForce(@NotNull List<StarDisplayRecord> stars,
                                                           double lowerBound,
                                                           double upperBound) {
        DistanceRoutes distanceRoutes = DistanceRoutes
                .builder()
                .upperDistance(upperBound)
                .lowerDistance(lowerBound)
                .build();

        List<TransitRoute> transitRoutes = starMeasurementService.calculateDistances(distanceRoutes, stars);
        log.debug("Brute-force calculated {} transits", transitRoutes.size());

        RouteGraph routeGraph = new RouteGraph();
        routeGraph.calculateGraphForTransit(transitRoutes);
        return routeGraph;
    }

    /**
     * Find K-shortest paths and create route descriptors.
     */
    private @NotNull PossibleRoutes findKShortestPaths(@NotNull RouteFindingOptions options,
                                                        @NotNull String origin,
                                                        @NotNull String destination,
                                                        @NotNull RouteBuilderHelper routeBuilderHelper,
                                                        @NotNull RouteGraph routeGraph,
                                                        @NotNull DataSetDescriptor dataSet) {
        // Find the k shortest paths (add 1 because first may be null)
        List<String> kShortestPaths = routeGraph.findKShortestPaths(
                origin, destination, options.getNumberPaths() + 1);

        PossibleRoutes possibleRoutes = new PossibleRoutes();
        possibleRoutes.setDesiredPath("Route %s to %s".formatted(origin, destination));

        int rank = 1;
        for (String path : kShortestPaths) {
            if (path == null || path.contains("null")) {
                continue;
            }

            // First route uses the specified color, others get random colors
            Color color = (rank == 1) ? options.getColor() : randomColor();

            RouteDescriptor route = routeBuilderHelper.buildPath(
                    origin, destination, Integer.toString(rank),
                    color, options.getLineWidth(), path);

            route.setDescriptor(dataSet);

            RoutingMetric metric = RoutingMetric
                    .builder()
                    .totalLength(route.getTotalLength())
                    .routeDescriptor(route)
                    .path(path)
                    .rank(rank)
                    .numberOfSegments(route.getRouteCoordinates().size())
                    .build();

            possibleRoutes.getRoutes().add(metric);
            rank++;
        }

        return possibleRoutes;
    }

    /**
     * Generate a random color for alternate routes.
     */
    private @NotNull Color randomColor() {
        return Color.color(Math.random(), Math.random(), Math.random());
    }

    /**
     * Get the graph threshold for star count.
     *
     * @return the maximum number of stars for route finding
     */
    public int getGraphThreshold() {
        return GRAPH_THRESHOLD;
    }
}
