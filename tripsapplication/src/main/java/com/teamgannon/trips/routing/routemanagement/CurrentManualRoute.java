package com.teamgannon.trips.routing.routemanagement;

import com.teamgannon.trips.algorithms.StarMath;
import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.listener.RouteUpdaterListener;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

@Slf4j
@Data
public class CurrentManualRoute {

    /**
     * used to keep track of node that are in the current route
     */
    private final List<Node> currentRouteNodePoints = new ArrayList<>();

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

    private TripsContext tripsContext;
    private RouteDisplay routeDisplay;
    private RouteGraphicsUtil routeGraphicsUtil;
    private RouteBuilderUtils routeBuilderUtils;
    private RouteUpdaterListener routeUpdaterListener;

    /**
     * the constructor
     *
     * @param tripsContext      the trips context
     * @param routeDisplay      the route display
     * @param routeGraphicsUtil the drawing facility for routing
     */
    public CurrentManualRoute(TripsContext tripsContext,
                              RouteDisplay routeDisplay,
                              RouteGraphicsUtil routeGraphicsUtil,
                              RouteBuilderUtils routeBuilderUtils) {
        this.tripsContext = tripsContext;
        this.routeDisplay = routeDisplay;
        this.routeGraphicsUtil = routeGraphicsUtil;
        this.routeBuilderUtils = routeBuilderUtils;
    }

    /**
     * clear this structure of content
     */
    public void clear() {
        currentRoute.clear();
        currentRoute = null;
        currentRouteDisplay.getChildren().clear();
        routeDisplay.setManualRoutingActive(false);
    }

    public void setup(DataSetDescriptor dataSetDescriptor, RouteDescriptor routeDescriptor) {
        currentRoute = routeDescriptor;
        currentRoute.setDescriptor(dataSetDescriptor);
        routeDisplay.setManualRoutingActive(true);
    }


    ///////////


    /**
     * start the route from a selected star
     *
     * @param dataSetDescriptor the data set descriptor
     * @param routeDescriptor   the route decriptor which is filled before
     * @param firstPlottedStar  the star to plot to
     */
    public void startRoute(DataSetDescriptor dataSetDescriptor, RouteDescriptor routeDescriptor, @NotNull StarDisplayRecord firstPlottedStar) {
        setup(dataSetDescriptor, routeDescriptor);

        log.info("Start charting the route:" + routeDescriptor);
        Point3D startStar = firstPlottedStar.getCoordinates();
        if (routeDescriptor != null) {
            addLink(firstPlottedStar, startStar, 0, null, null);
            /// add to route lookup
            routeDisplay.addRouteToDisplay(getCurrentRoute(), getCurrentRouteDisplay());
            routeDisplay.toggleRouteVisibility(true);
            routeUpdaterListener.routingStatus(true);
        }
    }

    private Group getCurrentRouteDisplay() {
        if (currentRouteDisplay == null) {
            currentRouteDisplay = new Group();
        }
        return currentRouteDisplay;
    }

    /**
     * continue the route
     *
     * @param starDisplayRecord the the star to route to
     */
    public void continueRoute(@NotNull StarDisplayRecord starDisplayRecord) {
        if (routeDisplay.isManualRoutingActive()) {
            log.info("manual routing active");
            log.info("route to {}", starDisplayRecord.getStarName());
            createRouteSegment(starDisplayRecord);
            routeDisplay.updateLabels();
            log.info("Next Routing step:{}", getCurrentRoute());
        } else {
            showErrorAlert("Routing", "start a route first");
        }
    }

    /**
     * remoe the current manual route
     */
    public void removeRoute() {
        log.info("removing route");
        if (routeDisplay.isManualRoutingActive()) {
            log.info("manual routing is active so go ahead");
            // get last line segment from list
            Node lineSegment = getLastSegment();
            Label label = getLastLabel();
            if (label != null) {
                routeDisplay.removeLabel(label);
            }
            // remove last entry
            if (!removeLast()) {

                log.info("Remove last Routing step:{}", getCurrentRoute());
            } else {
                // no more elements
                clear();
                log.info("Removed all segments");
                routeUpdaterListener.routingStatus(false);
            }
            // remove last line segment
            removeRouteSegment(lineSegment);
            // remove label to line segment
            routeDisplay.removeObject(lineSegment);
        } else {
            showErrorAlert("Routing", "start a route first");
        }
    }

    /**
     * finish the route
     *
     * @param endingStar the star record to terminate at
     */
    public void finishRoute(@NotNull StarDisplayRecord endingStar) {
        log.info("finishing route");
        if (routeDisplay.isManualRoutingActive()) {
            log.info("manual routing is active so finish it");
            createRouteSegment(endingStar);
            routeDisplay.setManualRoutingActive(false);
            routeDisplay.updateLabels();
            routeUpdaterListener.newRoute(getCurrentRoute().getDescriptor(), getCurrentRoute());
        } else {
            showErrorAlert("Routing", "start a route first");
        }
    }

    public void finishRoute() {
        log.info("Fininshing manual route");
        if (routeDisplay.isManualRoutingActive()) {
            log.info("manual route is active, so turn it off");
            routeDisplay.setManualRoutingActive(false);
            routeUpdaterListener.newRoute(getCurrentRoute().getDescriptor(), getCurrentRoute());
            routeDisplay.updateLabels();
        } else {
            log.info("Manual routing is not active");
        }
    }


    /**
     * route segment helper
     * used to extend a route by one segement from last plot position to a target star
     *
     * @param destinationStar the destinationt to plot to
     */
    private void createRouteSegment(@NotNull StarDisplayRecord destinationStar) {
        Point3D toStarLocation = destinationStar.getCoordinates();

        if (getCurrentRoute() != null) {
            int size = getNumberSegments();
            Point3D fromPoint = getLastPoint();

            double length = calculateDistance(
                    getLastStar(),
                    destinationStar.getActualCoordinates());

            Label lengthLabel = routeGraphicsUtil.createLabel((size == 1), length);

            Node lineSegment = routeGraphicsUtil.createLineSegment(fromPoint,
                    toStarLocation,
                    getLineWidth(),
                    getRouteColor(),
                    lengthLabel);

            addLink(destinationStar, toStarLocation, length, lineSegment, lengthLabel);

            addRouteSegment(lineSegment);
            log.info("current route: {}", getCurrentRoute());
            log.info("route continued");
        }
    }

    /**
     * calculate the distance form star to star
     *
     * @param fromStar     the source star
     * @param toStarCoords the target star coordinates
     * @return the distance
     */
    private double calculateDistance(@NotNull StarDisplayRecord fromStar, double[] toStarCoords) {
        double[] fromStarCoords = fromStar.getActualCoordinates();
        // calculate the actual distance
        return StarMath.getDistance(fromStarCoords, toStarCoords);
    }


    /**
     * reset the route and remove the parts that were partially drawn
     */
    public void resetRoute() {

        if (routeDisplay.isManualRoutingActive()) {
            log.info("manual routing is active, so reset it");
            List<Label> labels = getLabels();
            for (Label label : labels) {
                routeDisplay.removeLabel(label);
            }
            clear();
            Group routeToRemove = routeDisplay.getRoute(getRouteId());
            routeDisplay.removeRouteId(getRouteId());
            routeDisplay.removeRouteFromDisplay(routeToRemove);
        }

        routeDisplay.removeRouteFromDisplay(getCurrentRouteDisplay());
        routeDisplay.addRouteToDisplay(getCurrentRoute(), getCurrentRouteDisplay());

        log.info("Resetting the route");
    }

    ////////////////////  HELPER FUNCTIONS  ///////////////

    public void addLink(StarDisplayRecord toStar, Point3D starPosition,
                        double routeLength, Node lineSegment, Label lengthLabel) {
        currentRoute.addLink(toStar, starPosition, routeLength, lineSegment, lengthLabel);
    }

    public int getNumberSegments() {
        return currentRoute.getRouteCoordinates().size();
    }

    public void addRouteSegment(Node lineSegment) {
        currentRouteDisplay.getChildren().add(lineSegment);
        currentRouteDisplay.setVisible(true);
    }

    public void removeRouteSegment(Node lineSegment) {
        currentRouteDisplay.getChildren().remove(lineSegment);
    }

    public Node getLastSegment() {
        return currentRoute.getLineSegmentList().get(getNumberSegments() - 1);
    }

    public Point3D getLastPoint() {
        return currentRoute.getRouteCoordinates().get(getNumberSegments() - 1);
    }

    public StarDisplayRecord getLastStar() {
        return currentRoute.getLastStar();
    }

    public Color getRouteColor() {
        return currentRoute.getColor();
    }

    public double getLineWidth() {
        return currentRoute.getLineWidth();
    }

    public Label getLastLabel() {
        return currentRoute.getLastLabel();
    }

    public List<Label> getLabels() {
        return currentRoute.getLabelList();
    }

    public boolean removeLast() {
        return currentRoute.removeLast();
    }

    public UUID getRouteId() {
        return currentRoute.getId();
    }

}
