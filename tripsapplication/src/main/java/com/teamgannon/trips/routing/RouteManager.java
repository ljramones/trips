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

import com.teamgannon.trips.algorithms.StarMath;
import com.teamgannon.trips.graphics.CurrentPlot;
import com.teamgannon.trips.graphics.entities.CustomObjectFactory;
import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.graphics.entities.StellarEntityFactory;
import com.teamgannon.trips.graphics.panes.InterstellarSpacePane;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.listener.RouteUpdaterListener;
import javafx.geometry.Bounds;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SubScene;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

@Slf4j
public class RouteManager {

    private DataSetDescriptor dataSetDescriptor;
    private Map<Node, Label> shapeToLabel = new HashMap<>();

    /**
     * the label display
     */
    private final Group labelDisplayGroup = new Group();

    private final Group sceneRoot;
    private final SubScene subScene;
    private InterstellarSpacePane interstellarSpacePane;
    private final RouteUpdaterListener routeUpdaterListener;
    private final CurrentPlot currentPlot;

    /**
     * this is the descriptor of the current route
     */
    private RouteDescriptor currentRoute;
    /**
     * the graphic portion of the current route
     */
    private Group currentRouteDisplay;
    /**
     * whether there is a route being traced, true is yes
     */
    private boolean routingActive = false;

    /**
     * the total set of all routes
     */
    private final Group routesGroup = new Group();

    private double controlPaneOffset;

    private final boolean routeLabelsOn = true;


    ///////////////////////

    /**
     * the constructor
     *
     * @param routeUpdaterListener the route update listener
     */
    public RouteManager(Group world,
                        Group sceneRoot,
                        SubScene subScene,
                        InterstellarSpacePane interstellarSpacePane,
                        RouteUpdaterListener routeUpdaterListener,
                        CurrentPlot currentPlot) {

        this.sceneRoot = sceneRoot;
        this.subScene = subScene;
        this.interstellarSpacePane = interstellarSpacePane;

        this.routeUpdaterListener = routeUpdaterListener;
        this.currentPlot = currentPlot;

        currentRouteDisplay = new Group();

        world.getChildren().add(routesGroup);
        sceneRoot.getChildren().add(labelDisplayGroup);

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
        labelDisplayGroup.getChildren().clear();
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
        labelDisplayGroup.setVisible(routesOn);
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
            currentRoute.setLastStar(starDisplayRecord);
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
            updateLabels(interstellarSpacePane);
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
//            Group routeGraphic = createRoute(currentRoute);
//            routesGroup.getChildren().add(routeGraphic);
//            routesGroup.setVisible(true);
            //
//            makeRoutePermanent(currentRoute);
            updateLabels(interstellarSpacePane);
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
        Group displayRoute = createDisplayRoute(currentRoute);

        // add this created one to the routes group
        routesGroup.getChildren().add(displayRoute);
    }


    private Group createDisplayRoute(RouteDescriptor currentRoute) {
        Group route = new Group();
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

            double length = calculateDistance(currentRoute.getLastStar(), starDisplayRecord.getActualCoordinates());
            Label lengthLabel = createLabel(length);
            Node lineSegment = createLineSegment(fromPoint, toStarLocation, currentRoute.getLineWidth(), currentRoute.getColor(), lengthLabel);
            currentRoute.getLineSegments().add(toStarLocation);
            currentRoute.getRouteList().add(id);
            currentRoute.setLastStar(starDisplayRecord);

            currentRouteDisplay.getChildren().add(lineSegment);
            currentRouteDisplay.setVisible(true);
            log.info("route continued");
        }
    }

    private double calculateDistance(StarDisplayRecord fromStar, double[] toStarCoords) {
        double[] fromStarCoords = fromStar.getActualCoordinates();
        // calculate the actual distance
        return StarMath.getDistance(fromStarCoords, toStarCoords);
    }

    private Node createLineSegment(Point3D origin, Point3D target, double lineWeight, Color color, Label lengthLabel) {
        Point3D yAxis = new Point3D(0, 1, 0);
        Point3D diff = target.subtract(origin);
        double height = diff.magnitude();

        Point3D mid = target.midpoint(origin);
        Translate moveToMidpoint = new Translate(mid.getX(), mid.getY(), mid.getZ());

        Point3D axisOfRotation = diff.crossProduct(yAxis);
        double angle = Math.acos(diff.normalize().dotProduct(yAxis));
        Rotate rotateAroundCenter = new Rotate(-Math.toDegrees(angle), axisOfRotation);

        // create cylinder and color it with phong material
        Cylinder line = StellarEntityFactory.createCylinder(lineWeight, color, height);

        Group lineGroup = new Group();

        line.getTransforms().addAll(moveToMidpoint, rotateAroundCenter);
        lineGroup.getChildren().add(line);

        if (routeLabelsOn) {
            // attach label
            Sphere pointSphere = createPointSphere(lengthLabel);
            pointSphere.setTranslateX(mid.getX());
            pointSphere.setTranslateY(mid.getY());
            pointSphere.setTranslateZ(mid.getZ());
            lengthLabel.setTextFill(color);
            lineGroup.getChildren().add(pointSphere);
            if (!shapeToLabel.containsValue(lengthLabel)) {
                shapeToLabel.put(pointSphere, lengthLabel);
                labelDisplayGroup.getChildren().add(lengthLabel);
            } else {
                log.warn("what is <{}> present twice", lengthLabel.getText());
            }
        }

        return lineGroup;
    }

    private Sphere createPointSphere(Label label) {
        final PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(Color.WHEAT);
        material.setSpecularColor(Color.WHEAT);
        Sphere sphere = new Sphere(1);
        sphere.setMaterial(material);
        label.setLabelFor(sphere);
        labelDisplayGroup.getChildren().add(label);
        shapeToLabel.put(sphere, label);
        return sphere;
    }

    private Label createLabel(double length) {
        Label label = new Label(String.format("%.2fly", length));
        Font font = Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 6);
        label.setFont(font);
        return label;
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
        currentRouteDisplay = new Group();
    }

    /**
     * reset the current route
     */
    private void resetCurrentRoute() {
        if (currentRoute != null) {
            currentRoute.clear();
        }
        currentRouteDisplay = new Group();
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
        updateLabels(interstellarSpacePane);
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
            Group routeGraphic = createRoute(routeDescriptor);
            routesGroup.getChildren().add(routeGraphic);
            routesGroup.setVisible(true);
        }
        updateLabels(interstellarSpacePane);
        log.info("Plot done");
    }

    public void plotRouteDescriptor(RouteDescriptor routeDescriptor) {
        Group routeGraphic = createRoute(routeDescriptor);
        routesGroup.getChildren().add(routeGraphic);
        routesGroup.setVisible(true);
    }

    /**
     * this creates an independent connect series of lines related to the route
     *
     * @param routeDescriptor the route descriptor
     * @return the route Xform
     */
    public Group createRoute(RouteDescriptor routeDescriptor) {
        Group route = new Group();
        boolean firstLink = true;

        int i = 0;
        Point3D previousPoint = new Point3D(0, 0, 0);
        for (Point3D point3D : routeDescriptor.getLineSegments()) {
            if (firstLink) {
                previousPoint = point3D;
                firstLink = false;
            } else {
                double length = routeDescriptor.getLengthList().get(i++);
                Label lengthLabel = createLabel(length);
                // create the line segment
                Node lineSegment = createLineSegment(previousPoint, point3D, 0.5, routeDescriptor.getColor(), lengthLabel);
                // step along the segment
                previousPoint = point3D;

                // add the completed line segment to overall list
                route.getChildren().add(lineSegment);
            }

        }
        return route;
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

    public void updateLabels(InterstellarSpacePane interstellarSpacePane) {
        shapeToLabel.forEach((node, label) -> {
            Point3D coordinates = node.localToScene(Point3D.ZERO, true);

            //Clipping Logic
            //if coordinates are outside of the scene it could
            //stretch the screen so don't transform them
            double xs = coordinates.getX();
            double ys = coordinates.getY();

            double x;
            double y;

            Bounds ofParent = interstellarSpacePane.getBoundsInParent();
            if (ofParent.getMinX() > 0) {
                x = xs - ofParent.getMinX();
            } else {
                x = xs;
            }
            if (ofParent.getMinY() >= 0) {
                y = ys - ofParent.getMinY() - controlPaneOffset;
            } else {
                y = ys < 0 ? ys - controlPaneOffset : ys + controlPaneOffset;
            }

            // is it left of the view?
            if (x < 0) {
                x = 0;
            }

            // is it right of the view?
            if ((x + label.getWidth() + 5) > subScene.getWidth()) {
                x = subScene.getWidth() - (label.getWidth() + 5);
            }

            // is it above the view?
            if (y < 0) {
                y = 0;
            }

            // is it below the view
            if ((y + label.getHeight()) > subScene.getHeight()) {
                y = subScene.getHeight() - (label.getHeight() + 5);
            }

            //update the local transform of the label.
            label.getTransforms().setAll(new Translate(x, y));
        });

    }

    public void setControlPaneOffset(double controlPaneOffset) {
        this.controlPaneOffset = controlPaneOffset;
    }

}
