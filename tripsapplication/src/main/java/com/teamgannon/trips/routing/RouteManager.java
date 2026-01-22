/*
 *     Copyright 2016-2020 TRIPS https://github.com/ljramones/trips
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.teamgannon.trips.routing;

import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.events.RoutingStatusEvent;
import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.graphics.panes.InterstellarSpacePane;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.routing.model.Route;
import com.teamgannon.trips.routing.model.RoutingMetric;
import com.teamgannon.trips.routing.model.RoutingType;
import com.teamgannon.trips.routing.routemanagement.*;
import javafx.scene.Group;
import javafx.scene.SubScene;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Central coordinator for route visualization and management.
 * <p>
 * This class orchestrates route operations by delegating to specialized components:
 * <ul>
 *   <li>{@link RoutePlotter} - handles route plotting and rendering</li>
 *   <li>{@link CurrentManualRoute} - handles interactive manual route creation</li>
 *   <li>{@link RouteDisplay} - manages route display state and visibility</li>
 * </ul>
 * <p>
 * <b>Responsibilities:</b>
 * <ul>
 *   <li>Initializing route visualization components</li>
 *   <li>Coordinating route state (routing type, visibility)</li>
 *   <li>Providing access to manual routing operations</li>
 *   <li>Delegating plotting operations to RoutePlotter</li>
 * </ul>
 */
@Slf4j
@Component
public class RouteManager {

    private final TripsContext tripsContext;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Manages route display state and visibility.
     */
    private RouteDisplay routeDisplay;

    /**
     * Handles route plotting operations.
     */
    private RoutePlotter routePlotter;

    /**
     * Handles manual route creation.
     */
    @Getter
    private CurrentManualRoute manualRoute;

    /**
     * Current routing mode.
     */
    @Getter
    private RoutingType routingType = RoutingType.NONE;

    /**
     * Creates a new RouteManager.
     *
     * @param tripsContext   the application context
     * @param eventPublisher for publishing route events
     */
    public RouteManager(TripsContext tripsContext, ApplicationEventPublisher eventPublisher) {
        this.tripsContext = tripsContext;
        this.eventPublisher = eventPublisher;
    }

    // =========================================================================
    // Initialization
    // =========================================================================

    /**
     * Initializes the route visualization components.
     * <p>
     * Must be called after the 3D scene is set up.
     *
     * @param sceneRoot             the 2D scene root (for labels)
     * @param world                 the 3D world group (for route geometry)
     * @param subScene              the 3D subscene
     * @param interstellarSpacePane the interstellar space pane
     */
    public void setGraphics(Group sceneRoot,
                            Group world,
                            SubScene subScene,
                            InterstellarSpacePane interstellarSpacePane) {

        // Initialize display and utilities
        routeDisplay = new RouteDisplay(tripsContext, subScene, interstellarSpacePane);
        RouteGraphicsUtil routeGraphicsUtil = new RouteGraphicsUtil(routeDisplay);
        RouteBuilderUtils routeBuilderUtils = new RouteBuilderUtils(tripsContext, routeDisplay, routeGraphicsUtil);
        PartialRouteUtils partialRouteUtils = new PartialRouteUtils(tripsContext, routeDisplay, routeGraphicsUtil, routeBuilderUtils);

        // Initialize specialized handlers
        routePlotter = new RoutePlotter(tripsContext, routeDisplay, routeBuilderUtils, partialRouteUtils, eventPublisher);
        manualRoute = new CurrentManualRoute(tripsContext, routeDisplay, routeGraphicsUtil, routeBuilderUtils, eventPublisher);

        // Add route groups to scene
        world.getChildren().add(routeDisplay.getRoutesGroup());
        sceneRoot.getChildren().add(routeDisplay.getLabelDisplayGroup());
    }

    // =========================================================================
    // Route State Management
    // =========================================================================

    /**
     * Checks if manual routing is currently active.
     *
     * @return true if a manual route is being created
     */
    public boolean isManualRoutingActive() {
        return routeDisplay.isManualRoutingActive();
    }

    /**
     * Sets the manual routing active state.
     *
     * @param active true to enable manual routing mode
     */
    public void setManualRoutingActive(boolean active) {
        routeDisplay.setManualRoutingActive(active);
        if (!active) {
            eventPublisher.publishEvent(new RoutingStatusEvent(this, false));
        }
    }

    /**
     * Sets the current routing type.
     *
     * @param type the routing type
     */
    public void setRoutingType(RoutingType type) {
        this.routingType = type;
    }

    /**
     * Clears all routes from the display.
     */
    public void clearRoutes() {
        routeDisplay.clear();
        tripsContext.getCurrentPlot().clearRoutes();
    }

    // =========================================================================
    // Route Visibility
    // =========================================================================

    /**
     * Toggles visibility of all routes and their labels.
     *
     * @param visible true to show routes
     */
    public void toggleRoutes(boolean visible) {
        routeDisplay.toggleRoutes(visible);
    }

    /**
     * Toggles visibility of route length labels only.
     *
     * @param visible true to show labels
     */
    public void toggleRouteLengths(boolean visible) {
        routeDisplay.toggleRouteLengths(visible);
    }

    /**
     * Changes the display state of a specific route.
     *
     * @param routeDescriptor the route to modify
     * @param visible         true to show the route
     */
    public void changeDisplayStateOfRoute(RouteDescriptor routeDescriptor, boolean visible) {
        routeDisplay.changeDisplayStateOfRoute(routeDescriptor, visible);
    }

    // =========================================================================
    // Label Management
    // =========================================================================

    /**
     * Updates label positions after view changes.
     */
    public void updateLabels() {
        routeDisplay.updateLabels();
    }

    /**
     * Sets the control pane offset for label positioning.
     *
     * @param offset the offset in pixels
     */
    public void setControlPaneOffset(double offset) {
        routeDisplay.setControlPaneOffset(offset);
    }

    // =========================================================================
    // Route Plotting (Delegated to RoutePlotter)
    // =========================================================================

    /**
     * Plots multiple routes from database models.
     *
     * @param routeList the routes to plot
     */
    public void plotRoutes(@NotNull List<Route> routeList) {
        routePlotter.plotRoutes(routeList);
    }

    /**
     * Plots routes from automated route finding.
     *
     * @param dataSet        the current dataset
     * @param routingMetrics the routes to plot
     */
    public void plotRouteDescriptors(DataSetDescriptor dataSet, @NotNull List<RoutingMetric> routingMetrics) {
        routePlotter.plotRouteDescriptors(dataSet, routingMetrics);
    }

    /**
     * Plots a single route.
     *
     * @param route the route to plot
     */
    public void plotRoute(@NotNull Route route) {
        routePlotter.plotRoute(route);
    }

    /**
     * Checks if a route can be fully plotted (all stars visible).
     *
     * @param route the route to check
     * @return true if all stars are visible
     */
    public boolean checkIfWholeRouteCanBePlotted(@NotNull Route route) {
        return routePlotter.canPlotFullRoute(route);
    }

    // =========================================================================
    // Manual Routing Operations
    // =========================================================================

    /**
     * Starts a new manual route from the specified star.
     *
     * @param dataSet         the current dataset
     * @param routeDescriptor the route descriptor (pre-configured with color, width, etc.)
     * @param startStar       the starting star
     */
    public void startRoute(DataSetDescriptor dataSet, RouteDescriptor routeDescriptor, @NotNull StarDisplayRecord startStar) {
        manualRoute.resetRoute(routeDescriptor);
        manualRoute.startRoute(dataSet, routeDescriptor, startStar);
    }

    /**
     * Continues the manual route to the specified star.
     *
     * @param star the next star in the route
     */
    public void continueRoute(@NotNull StarDisplayRecord star) {
        manualRoute.continueRoute(star);
    }

    /**
     * Removes the current manual route being created.
     */
    public void removeRoute() {
        manualRoute.removeRoute();
    }

    /**
     * Removes the last segment from the manual route (undo).
     *
     * @return the star that was removed
     */
    public StarDisplayRecord removeLastSegment() {
        return manualRoute.removeLastSegment();
    }

    /**
     * Finishes the manual route at the specified star.
     *
     * @param endStar the final star
     */
    public void finishRoute(@NotNull StarDisplayRecord endStar) {
        manualRoute.finishRoute(endStar);
    }

    /**
     * Finishes the manual route at the current position.
     */
    public void finishRoute() {
        manualRoute.finishRoute();
    }

    /**
     * Resets/cancels the current manual route.
     */
    public void resetRoute() {
        manualRoute.resetRoute();
    }
}
