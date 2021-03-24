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
import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.config.application.model.SerialFont;
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
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

@Slf4j
public class RouteManager {

    private final Map<Node, Label> shapeToLabel = new HashMap<>();

    private final Map<Label, Node> reverseLabelLookup = new HashMap<>();

    /**
     * the label display
     */
    private final Group labelDisplayGroup = new Group();
    private final SubScene subScene;
    private final InterstellarSpacePane interstellarSpacePane;
    private final RouteUpdaterListener routeUpdaterListener;

    /**
     * used to keep track of node that are in the current route
     */
    private final List<Node> currentRouteNodePoints = new ArrayList<>();

    /**
     * the total set of all routes
     */
    private final Group routesGroup = new Group();
    private final boolean routeLabelsOn = true;
    private final TripsContext tripsContext;
    private final ColorPalette colorPalette;

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
     * to deal with the offset based on the control panel
     */
    private double controlPaneOffset;


    ///////////////////////

    /**
     * the constructor
     *
     * @param routeUpdaterListener the route update listener
     */
    public RouteManager(@NotNull Group world,
                        @NotNull Group sceneRoot,
                        SubScene subScene,
                        InterstellarSpacePane interstellarSpacePane,
                        RouteUpdaterListener routeUpdaterListener,
                        TripsContext tripsContext,
                        ColorPalette colorPalette) {

        this.subScene = subScene;
        this.interstellarSpacePane = interstellarSpacePane;

        this.routeUpdaterListener = routeUpdaterListener;
        this.tripsContext = tripsContext;
        this.colorPalette = colorPalette;

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
     * clear the routes
     */
    public void clearRoutes() {
        // clear the routes
        routesGroup.getChildren().clear();
        labelDisplayGroup.getChildren().clear();
        shapeToLabel.clear();
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

    public void startRoute(DataSetDescriptor dataSetDescriptor, RouteDescriptor routeDescriptor, @NotNull StarDisplayRecord starDisplayRecord) {
        resetRoute();
        routingActive = true;
        currentRoute = routeDescriptor;
        currentRoute.setDescriptor(dataSetDescriptor);
        log.info("Start charting the route:" + routeDescriptor);
        Point3D startStar = starDisplayRecord.getCoordinates();
        if (currentRoute != null) {
            currentRoute.addLink(starDisplayRecord, startStar, 0, null, null);
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
    public void continueRoute(@NotNull StarDisplayRecord starDisplayRecord) {
        if (routingActive) {
            createRouteSegment(starDisplayRecord);
            updateLabels(interstellarSpacePane);
            log.info("Next Routing step:{}", currentRoute);
        } else {
            showErrorAlert("Routing", "start a route first");
        }
    }

    public void removeRoute() {
        if (routingActive) {
            // get last line segment from list
            Node lineSegment = currentRoute.getLineSegmentList().get(currentRoute.getLineSegmentList().size() - 1);
            Label label = currentRoute.getLastLabel();
            if (label != null) {
                removeLabel(label);
            }
            // remove last entry
            if (!currentRoute.removeLast()) {

                log.info("Remove last Routing step:{}", currentRoute);
            } else {
                // no more elements
                routingActive = false;
                log.info("Removed all segments");
                routeUpdaterListener.routingStatus(false);
            }
            // remove last line segment
            currentRouteDisplay.getChildren().remove(lineSegment);
            // remove label to line segment
            shapeToLabel.remove(lineSegment);
        } else {
            showErrorAlert("Routing", "start a route first");
        }
    }

    /**
     * finish the route
     *
     * @param starDisplayRecord the star record to terminate at
     */
    public void finishRoute(@NotNull StarDisplayRecord starDisplayRecord) {
        if (routingActive) {
            createRouteSegment(starDisplayRecord);
            routingActive = false;
            makeRoutePermanent(currentRoute);
            updateLabels(interstellarSpacePane);
            routeUpdaterListener.newRoute(currentRoute.getDescriptor(), currentRoute);
        } else {
            showErrorAlert("Routing", "start a route first");
        }
    }

    /**
     * make the route permanent
     *
     * @param currentRoute the current route
     */
    private void makeRoutePermanent(@NotNull RouteDescriptor currentRoute) {
        routesGroup.getChildren().remove(currentRouteDisplay);
        for (Node node : currentRouteNodePoints) {
            shapeToLabel.remove(node);
        }
        currentRouteNodePoints.clear();

        // create a new one based on descriptor
        plotRouteDescriptor(currentRoute);

    }


    private void createRouteSegment(@NotNull StarDisplayRecord starDisplayRecord) {
        Point3D toStarLocation = starDisplayRecord.getCoordinates();

        if (currentRoute != null) {
            int size = currentRoute.getLineSegments().size();
            Point3D fromPoint = currentRoute.getLineSegments().get(size - 1);

            double length = calculateDistance(currentRoute.getLastStar(),
                    starDisplayRecord.getActualCoordinates());

            Label lengthLabel = createLabel((size == 1), length);

            Node lineSegment = createLineSegment(fromPoint, toStarLocation, currentRoute.getLineWidth(),
                    currentRoute.getColor(), lengthLabel);

            currentRoute.addLink(starDisplayRecord, toStarLocation, length, lineSegment, lengthLabel);

            currentRouteDisplay.getChildren().add(lineSegment);
            currentRouteDisplay.setVisible(true);
            log.info("current route: {}", currentRoute);
            log.info("route continued");
        }
    }

    private double calculateDistance(@NotNull StarDisplayRecord fromStar, double[] toStarCoords) {
        double[] fromStarCoords = fromStar.getActualCoordinates();
        // calculate the actual distance
        return StarMath.getDistance(fromStarCoords, toStarCoords);
    }

    private @NotNull Node createLineSegment(Point3D origin, @NotNull Point3D target,
                                            double lineWeight, Color color, @NotNull Label lengthLabel) {
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
                if (routingActive) {
                    // we add this to the active routing list so we can undo any current routing work after complete
                    currentRouteNodePoints.add(pointSphere);
                }
                shapeToLabel.put(pointSphere, lengthLabel);
                reverseLabelLookup.put(lengthLabel, pointSphere);
                labelDisplayGroup.getChildren().add(lengthLabel);
            } else {
                log.warn("what is <{}> present twice", lengthLabel.getText());
            }
        }

        return lineGroup;
    }

    /**
     * remove the label from everything
     *
     * @param label the label
     */
    private void removeLabel(Label label) {
        Node node = reverseLabelLookup.get(label);
        shapeToLabel.remove(node);
        reverseLabelLookup.remove(label);
        labelDisplayGroup.getChildren().remove(label);
    }

    private @NotNull Sphere createPointSphere(@NotNull Label label) {
        final PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(Color.WHEAT);
        material.setSpecularColor(Color.WHEAT);
        Sphere sphere = new Sphere(1);
        sphere.setMaterial(material);
        label.setLabelFor(sphere);
        labelDisplayGroup.getChildren().add(label);
        shapeToLabel.put(sphere, label);
        reverseLabelLookup.put(label, sphere);
        return sphere;
    }

    private @NotNull Label createLabel(boolean firstLink, double length) {
        Label label = new Label(((firstLink) ? "Start -> " : "") + String.format("%.2fly", length));
        SerialFont serialFont = colorPalette.getLabelFont();
        label.setFont(serialFont.toFont());
        return label;
    }

    /**
     * reset the route and remove the parts that were partially drawn
     */
    public void resetRoute() {
        if (currentRoute != null) {
            List<Label> labels = currentRoute.getLabelList();
            for (Label label : labels) {
                removeLabel(label);
            }
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
        currentRouteNodePoints.clear();
        currentRouteDisplay = new Group();
    }

    ////////////  redraw the routes

    /**
     * plot the routes
     *
     * @param routeList the list of routes
     */
    public void plotRoutes(@NotNull List<Route> routeList) {
        // clear existing routes
        routesGroup.getChildren().clear();
        routeList.forEach(this::plotRoute);
    }

    /**
     * used to plot routes that come from automated routing
     *
     * @param currentDataSet      current data descriptor
     * @param routeDescriptorList the list of things to plot
     */
    public void plotRouteDescriptors(DataSetDescriptor currentDataSet, @NotNull List<RoutingMetric> routeDescriptorList) {
        // plot route
        routeDescriptorList.stream().map(RoutingMetric::getRouteDescriptor).forEach(routeDescriptor -> {
            plotRouteDescriptor(routeDescriptor);
            // update route and save
            routeUpdaterListener.newRoute(currentDataSet, routeDescriptor);
        });
        updateLabels(interstellarSpacePane);
    }

    ////////

    /**
     * plot a single route
     *
     * @param route the route to plot
     */
    public void plotRoute(@NotNull Route route) {
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

    public void plotRouteDescriptor(@NotNull RouteDescriptor routeDescriptor) {
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
    public @NotNull Group createRoute(@NotNull RouteDescriptor routeDescriptor) {
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
                Label lengthLabel = createLabel(firstLink, length);
                // create the line segment
                Node lineSegment = createLineSegment(previousPoint, point3D, routeDescriptor.getLineWidth(), routeDescriptor.getColor(), lengthLabel);
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
    public boolean checkIfRouteCanBePlotted(@NotNull Route route) {
        return route.getRouteStars().stream().allMatch(tripsContext.getCurrentPlot().getStarLookup()::containsKey);
    }

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
        for (UUID id : route.getRouteStars()) {
            StarDisplayRecord starDisplayRecord = getStar(id);
            if (starDisplayRecord != null) {
                routeDescriptor.getRouteList().add(id);
                routeDescriptor.getLineSegments().add(starDisplayRecord.getCoordinates());
                if (i < route.getRouteLengths().size()) {
                    routeDescriptor.getLengthList().add(route.getRouteLengths().get(i++));
                }
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
    private @Nullable StarDisplayRecord getStar(UUID starId) {
        Node star = tripsContext.getCurrentPlot().getStar(starId);
        if (star != null) {
            return (StarDisplayRecord) star.getUserData();
        } else {
            return null;
        }
    }

    public void updateLabels(@NotNull InterstellarSpacePane interstellarSpacePane) {
        Bounds ofParent = interstellarSpacePane.getBoundsInParent();

        for (Map.Entry<Node, Label> entry : shapeToLabel.entrySet()) {
            Node node = entry.getKey();
            Label label = entry.getValue();
            Point3D coordinates = node.localToScene(Point3D.ZERO, true);

            //Clipping Logic
            //if coordinates are outside of the scene it could
            //stretch the screen so don't transform them
            double xs = coordinates.getX();
            double ys = coordinates.getY();

            // configure visibility
            if (xs < (ofParent.getMinX() + 20) || xs > (ofParent.getMaxX() - 20)) {
                label.setVisible(false);
                continue;
            } else {
                label.setVisible(true);
            }
            if (ys < (controlPaneOffset + 20) || (ys > ofParent.getMaxY() - 20)) {
                label.setVisible(false);
                continue;
            } else {
                label.setVisible(true);
            }

            double x;
            double y;

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
        }

    }

    public void setControlPaneOffset(double controlPaneOffset) {
        this.controlPaneOffset = controlPaneOffset;
    }


}
