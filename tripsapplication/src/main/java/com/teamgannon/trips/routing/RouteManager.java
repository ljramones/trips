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

import com.teamgannon.trips.graphics.CurrentPlot;
import com.teamgannon.trips.graphics.entities.*;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.listener.RouteUpdaterListener;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

@Slf4j
public class RouteManager {

    private DataSetDescriptor dataSetDescriptor;
    private final RouteUpdaterListener routeUpdaterListener;
    private final CurrentPlot currentPlot;

    /**
     * this is the descriptor of the current route
     */
    private RouteDescriptor currentRoute;
    /**
     * the graphic portion of the current route
     */
    private Xform currentRouteDisplay;
    /**
     * whether there is a route being traced, true is yes
     */
    private boolean routingActive = false;

    /**
     * the total set of all routes
     */
    private final Xform routesGroup = new Xform();

    ///////////////////////

    /**
     * the constructor
     *
     * @param routeUpdaterListener the route update listener
     */
    public RouteManager(Xform world,
                        RouteUpdaterListener routeUpdaterListener,
                        CurrentPlot currentPlot) {

        this.routeUpdaterListener = routeUpdaterListener;
        this.currentPlot = currentPlot;

        currentRouteDisplay = new Xform();
        currentRouteDisplay.setWhatAmI("Current Route");

        // define the
        routesGroup.setWhatAmI("Star Routes");
        world.getChildren().add(routesGroup);

    }

    /////////////// general

    /**
     * Is routing active?
     *
     * @return true if yes
     */
    public boolean isRoutingActive() {
        return routingActive;
    }

    /**
     * set the data descriptor descriptor context
     *
     * @param dataSetDescriptor the new current context
     */
    public void setDatasetContext(DataSetDescriptor dataSetDescriptor) {
        this.dataSetDescriptor = dataSetDescriptor;
    }

    /**
     * clear the routes
     */
    public void clearRoutes() {
        // clear the routes
        routesGroup.getChildren().clear();
    }

    /**
     * get the total routes being displayed
     *
     * @return the entire routes set
     */
    public Node getRoutesGroup() {
        return this.routesGroup;
    }

    /**
     * toggle the routes
     *
     * @param routesOn the status of the routes
     */
    public void toggleRoutes(boolean routesOn) {
        routesGroup.setVisible(routesOn);
    }

    ///////////// routing functions

    public void startRoute(RouteDescriptor routeDescriptor, StarDisplayRecord starDisplayRecord) {
        resetRoute();
        routingActive = true;
        currentRoute = routeDescriptor;
        log.info("Start charting the route:" + routeDescriptor);
        Point3D startStar = starDisplayRecord.getCoordinates();
        UUID id = starDisplayRecord.getRecordId();
        if (currentRoute != null) {
            currentRoute.getLineSegments().add(startStar);
            currentRoute.getRouteList().add(id);
            routesGroup.getChildren().add(currentRouteDisplay);
            routesGroup.setVisible(true);
            routeUpdaterListener.routingStatus(true);
        }
    }

    /**
     * continue the route
     *
     * @param starDisplayRecord the the star to route to
     */
    public void continueRoute(StarDisplayRecord starDisplayRecord) {
        if (routingActive) {
            createRouteSegment(starDisplayRecord);
            log.info("Next Routing step:{}", currentRoute);
        } else {
            showErrorAlert("Routing", "start a route first");
        }
    }

    /**
     * finish the route
     *
     * @param starDisplayRecord the star record to terminate at
     */
    public void finishRoute(StarDisplayRecord starDisplayRecord) {
        if (routingActive) {
            createRouteSegment(starDisplayRecord);
            routingActive = false;
            //
            Xform routeGraphic = StellarEntityFactory.createRoute(currentRoute);
            routesGroup.getChildren().add(routeGraphic);
            routesGroup.setVisible(true);
            //
            makeRoutePermanent(currentRoute);
            routeUpdaterListener.newRoute(dataSetDescriptor, currentRoute);
        } else {
            showErrorAlert("Routing", "start a route first");
        }
    }

    /**
     * make the route permanent
     *
     * @param currentRoute the current route
     */
    private void makeRoutePermanent(RouteDescriptor currentRoute) {
        // remove our hand drawn route
        routesGroup.getChildren().remove(currentRouteDisplay);

        // create a new one based on descriptor
        Xform displayRoute = createDisplayRoute(currentRoute);

        // add this created one to the routes group
        routesGroup.getChildren().add(displayRoute);
    }


    private Xform createDisplayRoute(RouteDescriptor currentRoute) {
        Xform route = new Xform();
        route.setWhatAmI(currentRoute.getName());
        Point3D previousPoint = new Point3D(0, 0, 0);
        boolean firstPoint = true;
        for (Point3D point3D : currentRoute.getLineSegments()) {
            if (firstPoint) {
                firstPoint = false;
            } else {
                double lineWidth = 0.5;
                Node lineSegment = CustomObjectFactory.createLineSegment(previousPoint, point3D, lineWidth, currentRoute.getColor());
                route.getChildren().add(lineSegment);
            }
            previousPoint = point3D;
        }
        return route;
    }

    private void createRouteSegment(StarDisplayRecord starDisplayRecord) {

        UUID id = starDisplayRecord.getRecordId();
        Point3D toStarLocation = starDisplayRecord.getCoordinates();

        if (currentRoute != null) {
            int size = currentRoute.getLineSegments().size();
            Point3D fromPoint = currentRoute.getLineSegments().get(size - 1);

            Node lineSegment = CustomObjectFactory.createLineSegment(
                    fromPoint, toStarLocation, currentRoute.getLineWidth(), currentRoute.getColor()
            );
            currentRoute.getLineSegments().add(toStarLocation);
            currentRoute.getRouteList().add(id);

            currentRouteDisplay.getChildren().add(lineSegment);
            currentRouteDisplay.setVisible(true);
            log.info("route continued");
        }
    }

    /**
     * reset the route and remove the parts that were partially drawn
     */
    public void resetRoute() {
        if (currentRoute != null) {
            currentRoute.clear();
        }
        routesGroup.getChildren().remove(currentRouteDisplay);
        routingActive = false;
        createCurrentRouteDisplay();
        resetCurrentRoute();
        log.info("Resetting the route");
    }


    private void createCurrentRouteDisplay() {
        currentRouteDisplay = new Xform();
        currentRouteDisplay.setWhatAmI("Current Route");
    }

    /**
     * reset the current route
     */
    private void resetCurrentRoute() {
        if (currentRoute != null) {
            currentRoute.clear();
        }
        currentRouteDisplay = new Xform();
        currentRouteDisplay.setWhatAmI("Current Route");
    }

    ////////////  redraw the routes

    /**
     * plot the routes
     *
     * @param routeList the list of routes
     */
    public void plotRoutes(List<Route> routeList) {
        // clear existing routes
        routesGroup.getChildren().clear();
        routeList.forEach(this::plotRoute);
    }

    public void plotRouteDescriptors(List<RoutingMetric> routeDescriptorList) {
        routeDescriptorList.stream().map(RoutingMetric::getRouteDescriptor).forEach(this::plotRouteDescriptor);
    }

    ////////

    /**
     * plot a single route
     *
     * @param route the route to plot
     */
    public void plotRoute(Route route) {
        if (checkIfRouteCanBePlotted(route)) {
            // do actual plot
            RouteDescriptor routeDescriptor = toRouteDescriptor(route);
            Xform routeGraphic = StellarEntityFactory.createRoute(routeDescriptor);
            routesGroup.getChildren().add(routeGraphic);
            routesGroup.setVisible(true);
        }
        log.info("Plot done");
    }

    public void plotRouteDescriptor(RouteDescriptor routeDescriptor) {
        Xform routeGraphic = StellarEntityFactory.createRoute(routeDescriptor);
        routesGroup.getChildren().add(routeGraphic);
        routesGroup.setVisible(true);
    }

    /**
     * this checks if all the stars on the route can be seen for a route
     *
     * @param route the route definition
     * @return true means we can plot the whole route
     */
    public boolean checkIfRouteCanBePlotted(Route route) {
        return route.getRouteStars().stream().allMatch(currentPlot.getStarLookup()::containsKey);
    }

    /**
     * convert a database description of a route to a graphical one.
     * check that all the stars in the original route are present because we can't display the route
     *
     * @param route the db description of a route
     * @return the graphical descriptor
     */
    private RouteDescriptor toRouteDescriptor(Route route) {
        RouteDescriptor routeDescriptor = RouteDescriptor.toRouteDescriptor(route);
        for (UUID id : route.getRouteStars()) {
            StarDisplayRecord starDisplayRecord = getStar(id);
            if (starDisplayRecord != null) {
                routeDescriptor.getRouteList().add(id);
                routeDescriptor.getLineSegments().add(starDisplayRecord.getCoordinates());
            }
        }

        return routeDescriptor;
    }

    /**
     * get the embedded object associated with the star in the lookup
     *
     * @param starId the id
     * @return the embedded object
     */
    private StarDisplayRecord getStar(UUID starId) {
        Node star = currentPlot.getStar(starId);
        if (star != null) {
            return (StarDisplayRecord) star.getUserData();
        } else {
            return null;
        }
    }

}
