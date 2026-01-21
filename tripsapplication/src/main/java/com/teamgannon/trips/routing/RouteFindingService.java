package com.teamgannon.trips.routing;

import com.teamgannon.trips.dialogs.search.model.DistanceRoutes;
import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.routing.automation.RouteBuilderHelper;
import com.teamgannon.trips.routing.automation.RouteGraph;
import com.teamgannon.trips.routing.model.PossibleRoutes;
import com.teamgannon.trips.routing.model.RouteFindingOptions;
import com.teamgannon.trips.routing.model.RouteFindingResult;
import com.teamgannon.trips.routing.model.RoutingMetric;
import com.teamgannon.trips.service.measure.StarMeasurementService;
import com.teamgannon.trips.transits.TransitRoute;
import javafx.scene.paint.Color;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Service for finding routes between stars.
 * <p>
 * This service encapsulates the route-finding algorithm without any UI dependencies.
 * It can be used by dialogs, automated processes, or tests.
 * <p>
 * The algorithm:
 * <ol>
 *   <li>Prunes stars based on spectral class and polity exclusions</li>
 *   <li>Calculates transits (possible jumps) within distance bounds</li>
 *   <li>Builds a graph from the transits</li>
 *   <li>Finds K-shortest paths using Yen's algorithm</li>
 *   <li>Creates route descriptors for each path</li>
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

    public RouteFindingService(StarMeasurementService starMeasurementService) {
        this.starMeasurementService = starMeasurementService;
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
        try {
            log.info("Finding route from {} to {}", options.getOriginStarName(), options.getDestinationStarName());

            String origin = options.getOriginStarName();
            String destination = options.getDestinationStarName();

            // Prune stars based on exclusions
            List<StarDisplayRecord> prunedStars = pruneStars(starsInView, options);
            log.info("Pruned stars from {} to {}", starsInView.size(), prunedStars.size());

            // Validate star count
            if (prunedStars.size() > GRAPH_THRESHOLD) {
                return RouteFindingResult.failure(
                        String.format("Too many stars (%d) to plan a route. Maximum is %d.",
                                prunedStars.size(), GRAPH_THRESHOLD));
            }

            // Build helper for star lookup
            RouteBuilderHelper routeBuilderHelper = new RouteBuilderHelper(prunedStars);

            // Validate origin star
            if (!routeBuilderHelper.has(origin)) {
                return RouteFindingResult.failure(
                        String.format("Origin star '%s' is not in the available stars (may have been excluded).", origin));
            }

            // Validate destination star
            if (!routeBuilderHelper.has(destination)) {
                return RouteFindingResult.failure(
                        String.format("Destination star '%s' is not in the available stars (may have been excluded).", destination));
            }

            // Calculate transits
            List<TransitRoute> transitRoutes = calculateTransits(options, prunedStars);
            log.info("Calculated {} transits", transitRoutes.size());

            if (transitRoutes.isEmpty()) {
                return RouteFindingResult.failure(
                        "No transits found with the given distance bounds. Try adjusting upper/lower bounds.");
            }

            // Build route graph
            RouteGraph routeGraph = new RouteGraph();
            routeGraph.calculateGraphForTransit(transitRoutes);

            // Check connectivity
            if (!routeGraph.isConnected(origin, destination)) {
                return RouteFindingResult.failure(
                        "No path exists between origin and destination with the given parameters. " +
                        "Try adjusting distance bounds or removing exclusions.");
            }

            // Find K-shortest paths
            PossibleRoutes possibleRoutes = findKShortestPaths(
                    options, origin, destination, routeBuilderHelper, routeGraph, dataSet);

            if (possibleRoutes.getRoutes().isEmpty()) {
                return RouteFindingResult.failure("No valid routes found.");
            }

            log.info("Found {} routes from {} to {}", possibleRoutes.getRoutes().size(), origin, destination);
            return RouteFindingResult.success(possibleRoutes);

        } catch (Exception e) {
            log.error("Failed to find routes: {}", e.getMessage(), e);
            return RouteFindingResult.failure("Route finding failed: " + e.getMessage());
        }
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
     * Calculate transits between stars within the distance bounds.
     */
    private @NotNull List<TransitRoute> calculateTransits(@NotNull RouteFindingOptions options,
                                                           @NotNull List<StarDisplayRecord> stars) {
        DistanceRoutes distanceRoutes = DistanceRoutes
                .builder()
                .upperDistance(options.getUpperBound())
                .lowerDistance(options.getLowerBound())
                .build();

        return starMeasurementService.calculateDistances(distanceRoutes, stars);
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
        possibleRoutes.setDesiredPath(String.format("Route %s to %s", origin, destination));

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
