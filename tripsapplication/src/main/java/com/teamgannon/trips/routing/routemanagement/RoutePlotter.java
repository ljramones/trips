package com.teamgannon.trips.routing.routemanagement;

import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.events.NewRouteEvent;
import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.graphics.entities.RouteVisibility;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.measure.TrackExecutionTime;
import com.teamgannon.trips.routing.model.Route;
import com.teamgannon.trips.routing.model.RouteSegment;
import com.teamgannon.trips.routing.model.RoutingMetric;
import javafx.scene.Group;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

/**
 * Handles the plotting (rendering) of routes in the 3D visualization.
 * <p>
 * This class is responsible for:
 * <ul>
 *   <li>Converting database route models to visual route descriptors</li>
 *   <li>Plotting full routes when all stars are visible</li>
 *   <li>Delegating partial route handling when some stars are off-screen</li>
 *   <li>Managing route segment deduplication to avoid overlapping lines</li>
 * </ul>
 */
@Slf4j
public class RoutePlotter {

    private final TripsContext tripsContext;
    private final RouteDisplay routeDisplay;
    private final RouteBuilderUtils routeBuilderUtils;
    private final PartialRouteUtils partialRouteUtils;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Creates a new RoutePlotter.
     *
     * @param tripsContext      the application context
     * @param routeDisplay      the route display manager
     * @param routeBuilderUtils utility for building route graphics
     * @param partialRouteUtils utility for handling partial routes
     * @param eventPublisher    for publishing route events
     */
    public RoutePlotter(TripsContext tripsContext,
                        RouteDisplay routeDisplay,
                        RouteBuilderUtils routeBuilderUtils,
                        PartialRouteUtils partialRouteUtils,
                        ApplicationEventPublisher eventPublisher) {
        this.tripsContext = tripsContext;
        this.routeDisplay = routeDisplay;
        this.routeBuilderUtils = routeBuilderUtils;
        this.partialRouteUtils = partialRouteUtils;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Plots multiple routes from database models.
     * <p>
     * Typically called when loading routes from a dataset.
     *
     * @param routeList the list of routes to plot
     */
    @TrackExecutionTime
    public void plotRoutes(@NotNull List<Route> routeList) {
        routeList.forEach(this::plotRoute);

        // Log spatial index statistics after bulk route plotting
        if (!routeList.isEmpty()) {
            logRouteSpatialIndexStatistics();
        }
    }

    /**
     * Logs route spatial index statistics for performance monitoring.
     */
    private void logRouteSpatialIndexStatistics() {
        var currentPlot = tripsContext.getCurrentPlot();
        if (currentPlot != null) {
            var index = currentPlot.getRouteSpatialIndex();
            if (index != null && !index.isEmpty()) {
                log.info("Route spatial index built: {} routes, {} segments",
                        index.getRouteCount(), index.getTotalSegments());
            }
        }
    }

    /**
     * Plots routes from automated route finding results.
     * <p>
     * Each route is plotted and a {@link NewRouteEvent} is published to persist it.
     *
     * @param currentDataSet the current dataset descriptor
     * @param routingMetrics the list of routing metrics containing route descriptors
     */
    @TrackExecutionTime
    public void plotRouteDescriptors(DataSetDescriptor currentDataSet,
                                     @NotNull List<RoutingMetric> routingMetrics) {
        log.info("Plotting {} routes", routingMetrics.size());

        for (RoutingMetric routingMetric : routingMetrics) {
            RouteDescriptor routeDescriptor = routingMetric.getRouteDescriptor();
            Route route = routeDescriptor.toRoute();
            plotRoute(route);

            // Publish event to persist the route
            eventPublisher.publishEvent(new NewRouteEvent(this, currentDataSet, routeDescriptor));
        }

        routeDisplay.updateLabels();
    }

    /**
     * Plots a single route.
     * <p>
     * Determines whether all stars in the route are visible:
     * <ul>
     *   <li>If all visible: plots as a full route</li>
     *   <li>If some hidden: delegates to partial route handling</li>
     * </ul>
     *
     * @param route the route to plot
     */
    @TrackExecutionTime
    public void plotRoute(@NotNull Route route) {
        if (canPlotFullRoute(route)) {
            plotFullRoute(route);
        } else {
            log.info("Route '{}' is partial (some stars not visible)", route.getRouteName());
            partialRouteUtils.findPartialRoutes(route);
        }
        routeDisplay.updateLabels();
        log.info("Plot done for route: {}", route.getRouteName());
    }

    /**
     * Checks if all stars in a route are currently visible.
     *
     * @param route the route to check
     * @return true if all stars can be displayed
     */
    public boolean canPlotFullRoute(@NotNull Route route) {
        return routeBuilderUtils.checkIfWholeRouteCanBePlotted(route);
    }

    /**
     * Plots a full route (all stars visible).
     */
    private void plotFullRoute(@NotNull Route route) {
        log.info("Plotting full route: {}", route.getRouteName());

        RouteDescriptor routeDescriptor = toRouteDescriptor(route);
        routeDescriptor.setVisibility(RouteVisibility.FULL);

        // Handle segment deduplication
        handleSegmentDeduplication(routeDescriptor);

        // Create and display the route graphic
        Group routeGraphic = routeBuilderUtils.createRoute(routeDescriptor);
        routeDisplay.addRouteToDisplay(routeDescriptor, routeGraphic);
        routeDisplay.toggleRouteVisibility(true);

        // Register route in current plot context
        tripsContext.getCurrentPlot().addRoute(routeDescriptor.getId(), routeDescriptor);
    }

    /**
     * Handles route segment deduplication.
     * <p>
     * When multiple routes share the same segment (e.g., two routes passing through
     * the same star pair), the overlapping segments are adjusted to prevent visual
     * overlap.
     *
     * @param routeDescriptor the route descriptor to check
     */
    private void handleSegmentDeduplication(@NotNull RouteDescriptor routeDescriptor) {
        List<RouteSegment> routeSegments = routeDescriptor.getRouteSegments();

        for (RouteSegment segment : routeSegments) {
            if (routeDisplay.contains(segment)) {
                // Adjust segment to avoid overlap
                log.debug("Adjusting overlapping segment: {}", segment);
                routeDescriptor.mutateCoordinates(segment);
            } else {
                // New segment, add to tracking
                routeDisplay.addSegment(segment);
                log.debug("Added new segment: {}", segment);
            }
        }
    }

    /**
     * Converts a database route model to a graphical route descriptor.
     * <p>
     * Populates the descriptor with star coordinates from the current display,
     * verifying each star is currently visible.
     *
     * @param route the database route model
     * @return the graphical route descriptor
     */
    private RouteDescriptor toRouteDescriptor(@NotNull Route route) {
        RouteDescriptor routeDescriptor = RouteDescriptor.toRouteDescriptor(route);

        int lengthIndex = 0;
        for (String starId : route.getRouteStars()) {
            StarDisplayRecord star = routeBuilderUtils.getStar(starId);
            if (star != null) {
                routeDescriptor.getRouteList().add(starId);
                routeDescriptor.getRouteCoordinates().add(star.getCoordinates());

                if (lengthIndex < route.getRouteLengths().size()) {
                    routeDescriptor.getLengthList().add(route.getRouteLengths().get(lengthIndex++));
                }
            }
        }

        return routeDescriptor;
    }
}
