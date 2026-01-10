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
import com.teamgannon.trips.events.NewRouteEvent;
import com.teamgannon.trips.events.RoutingStatusEvent;
import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.graphics.entities.RouteVisibility;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.graphics.panes.InterstellarSpacePane;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.measure.TrackExecutionTime;
import com.teamgannon.trips.routing.model.Route;
import com.teamgannon.trips.routing.model.RouteSegment;
import com.teamgannon.trips.routing.model.RoutingMetric;
import com.teamgannon.trips.routing.model.RoutingType;
import com.teamgannon.trips.routing.routemanagement.*;
import javafx.scene.Group;
import javafx.scene.SubScene;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class RouteManager {

    /**
     * holds all the route display vars
     */
    private RouteDisplay routeDisplay;

    /**
     * the tripscontext
     */
    private final TripsContext tripsContext;

    /**
     * event publisher for route events
     */
    private final ApplicationEventPublisher eventPublisher;

    /**
     * utility class for creating manual routes
     */
    private CurrentManualRoute currentManualRoute;

    /**
     * Utility class for drawing the routes
     */
    private RouteBuilderUtils routeBuilderUtils;

    /**
     * Utility class for creating partial routes
     */
    private PartialRouteUtils partialRouteUtils;

    /**
     * routing type
     */
    private RoutingType routingType = RoutingType.NONE;

    private RouteGraphicsUtil routeGraphicsUtil;


    ///////////////////////

    /**
     * the constructor
     */
    public RouteManager(TripsContext tripsContext, ApplicationEventPublisher eventPublisher) {
        this.tripsContext = tripsContext;
        this.eventPublisher = eventPublisher;
    }


    public void setGraphics(Group sceneRoot,
                            Group world,
                            SubScene subScene,
                            InterstellarSpacePane interstellarSpacePane) {

        routeDisplay = new RouteDisplay(tripsContext, subScene, interstellarSpacePane);
        routeGraphicsUtil = new RouteGraphicsUtil(routeDisplay);
        routeBuilderUtils = new RouteBuilderUtils(tripsContext, routeDisplay, routeGraphicsUtil);

        currentManualRoute = new CurrentManualRoute(tripsContext, routeDisplay, routeGraphicsUtil, routeBuilderUtils, eventPublisher);
        partialRouteUtils = new PartialRouteUtils(tripsContext, routeDisplay, routeGraphicsUtil, routeBuilderUtils);

        world.getChildren().add(routeDisplay.getRoutesGroup());
        sceneRoot.getChildren().add(routeDisplay.getLabelDisplayGroup());
    }

    /////////////// general

    /**
     * Is routing active?
     *
     * @return true if yes
     */
    public boolean isManualRoutingActive() {
        return routeDisplay.isManualRoutingActive();
    }

    /**
     * clear the routes
     */
    public void clearRoutes() {
        // clear the routes
        routeDisplay.clear();
        tripsContext.getCurrentPlot().clearRoutes();
    }

    /**
     * toggle the routes
     *
     * @param routesFlag the status of the routes
     */
    public void toggleRoutes(boolean routesFlag) {
        routeDisplay.toggleRoutes(routesFlag);
    }

    /**
     * toggle the route lengths marker
     *
     * @param routesLengthsFlag the route lengths marker
     */
    public void toggleRouteLengths(boolean routesLengthsFlag) {
        routeDisplay.toggleRouteLengths(routesLengthsFlag);
    }

    /**
     * set the routing type
     *
     * @param type the type
     */
    public void setRoutingType(RoutingType type) {
        routingType = type;
    }

    /**
     * get the routing type
     *
     * @return the routing type
     */
    public RoutingType getRoutingType() {
        return routingType;
    }

    /**
     * set the control offset for the screen
     * this is required since the screen coordinates are global and calculation has to take into account the control
     * panel height on top of the scree
     *
     * @param controlPaneOffset the height of the control plane
     */
    public void setControlPaneOffset(double controlPaneOffset) {
        routeDisplay.setControlPaneOffset(controlPaneOffset);
    }

    /**
     * change the state of a displayed route
     *
     * @param routeDescriptor the route
     * @param state           the state
     */
    public void changeDisplayStateOfRoute(RouteDescriptor routeDescriptor, boolean state) {
        routeDisplay.changeDisplayStateOfRoute(routeDescriptor, state);
    }

    /**
     * set whether we are engaged in an active manual routing plot function
     *
     * @param state the state - true is active, false is inactive
     */
    public void setManualRoutingActive(boolean state) {
        routeDisplay.setManualRoutingActive(state);
        if (!state) {
            eventPublisher.publishEvent(new RoutingStatusEvent(this, state));
        }
    }

    /**
     * update the labels for all routes
     */
    public void updateLabels() {
        routeDisplay.updateLabels();
    }


    ///////////////////////////////////////////////////////////////////////////////////////
    //
    //      Manual platting
    //
    //////////////////////////////////////////////////////////////////////////////////////

    public void initializeManualRoute() {

    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Plotting routines
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * plot the routes
     * this is usually caused by a request to do a fresh re-plot of all routes
     *
     * @param routeList the list of routes
     */
    @TrackExecutionTime
    public void plotRoutes(@NotNull List<Route> routeList) {
        routeList.forEach(this::plotRoute);
    }

    /**
     * used to plot routes that come from automated routing
     *
     * @param currentDataSet      current data descriptor
     * @param routeDescriptorList the list of things to plot
     */
    @TrackExecutionTime
    public void plotRouteDescriptors(DataSetDescriptor currentDataSet,
                                     @NotNull List<RoutingMetric> routeDescriptorList) {

        log.info("plotting routes");

        // plot route
        for (RoutingMetric routingMetric : routeDescriptorList) {
            RouteDescriptor routeDescriptor = routingMetric.getRouteDescriptor();

            Route route = routeDescriptor.toRoute();
            plotRoute(route);
            // update route and save
            eventPublisher.publishEvent(new NewRouteEvent(this, currentDataSet, routeDescriptor));
        }
        //  routeDisplay
        routeDisplay.updateLabels();
    }

    /**
     * plot a single route
     *
     * @param route the route to plot
     */
    @TrackExecutionTime
    public void plotRoute(@NotNull Route route) {
        if (checkIfWholeRouteCanBePlotted(route)) {
            // handle a full route
            log.info(">>>route {} is a full route", route.getRouteName());
            RouteDescriptor routeDescriptor = toRouteDescriptor(route);
            routeDescriptor.setVisibility(RouteVisibility.FULL);
            List<RouteSegment> routeSegmentList = routeDescriptor.getRouteSegments();

            // @TODO look for an existing match in the global list and adjust this segment if it matches
            // then save to global list
            for (RouteSegment routeSegment : routeSegmentList) {
                if (routeDisplay.contains(routeSegment)) {
                    // adjust route segment by a line width and a few pixels, then store the adjusted route segment
                    log.info("need to adjust this route segment:{}", routeSegment);
                    routeDescriptor.mutateCoordinates(routeSegment);
                } else {
                    // no match so add to segment
                    routeDisplay.addSegment(routeSegment);
                    log.info("no match on this route segment:{}", routeSegment);
                }
            }
            Group routeGraphic = routeBuilderUtils.createRoute(routeDescriptor);
            routeDisplay.addRouteToDisplay(routeDescriptor, routeGraphic);
            routeDisplay.toggleRouteVisibility(true);

            // register route in current plot
            tripsContext.getCurrentPlot().addRoute(routeDescriptor.getId(), routeDescriptor);
        } else {
            //  figure out what part of the partial route is here
            log.info(">>>route {} is a partial route", route.getRouteName());
            partialRouteUtils.findPartialRoutes(route);
        }
        routeDisplay.updateLabels();
        log.info("Plot done");
    }

    /**
     * this checks if all the stars on the route can be seen for a route
     *
     * @param route the route definition
     * @return true means we can plot the whole route
     */
    public boolean checkIfWholeRouteCanBePlotted(@NotNull Route route) {
        return routeBuilderUtils.checkIfWholeRouteCanBePlotted(route);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Manual routing
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * start the route from a selected star
     *
     * @param dataSetDescriptor the data set descriptor
     * @param routeDescriptor   the route descriptor which is filled before
     * @param firstPlottedStar  the star to plot to
     */
    public void startRoute(DataSetDescriptor dataSetDescriptor, RouteDescriptor routeDescriptor, @NotNull StarDisplayRecord firstPlottedStar) {
        currentManualRoute.resetRoute(routeDescriptor);
        currentManualRoute.startRoute(dataSetDescriptor, routeDescriptor, firstPlottedStar);
    }

    /**
     * continue the route
     *
     * @param starDisplayRecord the star to route to
     */
    public void continueRoute(@NotNull StarDisplayRecord starDisplayRecord) {
        currentManualRoute.continueRoute(starDisplayRecord);
    }

    /**
     * remove the current manual route
     */
    public void removeRoute() {
        currentManualRoute.removeRoute();
    }

    /**
     * remove the last segment
     */
    public StarDisplayRecord removeLastSegment() {
        return currentManualRoute.removeLastSegment();
    }

    /**
     * finish the route
     *
     * @param endingStar the star record to terminate at
     */
    public void finishRoute(@NotNull StarDisplayRecord endingStar) {
        currentManualRoute.finishRoute(endingStar);
    }

    public void finishRoute() {
        currentManualRoute.finishRoute();
    }

    /**
     * reset the route and remove the parts that were partially drawn
     */
    public void resetRoute() {
        currentManualRoute.resetRoute();
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Utility routines
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * convert a database description of a route to a graphical one.
     * check that all the stars in the original route are present because we can't display the route
     *
     * @param route the db description of a route
     * @return the graphical descriptor
     */
    private RouteDescriptor toRouteDescriptor(@NotNull Route route) {
        RouteDescriptor routeDescriptor = RouteDescriptor.toRouteDescriptor(route);
        int i = 0;
        for (String id : route.getRouteStars()) {
            StarDisplayRecord starDisplayRecord = routeBuilderUtils.getStar(id);
            if (starDisplayRecord != null) {
                routeDescriptor.getRouteList().add(id);
                routeDescriptor.getRouteCoordinates().add(starDisplayRecord.getCoordinates());
                if (i < route.getRouteLengths().size()) {
                    routeDescriptor.getLengthList().add(route.getRouteLengths().get(i++));
                }
            }
        }

        return routeDescriptor;
    }


}
