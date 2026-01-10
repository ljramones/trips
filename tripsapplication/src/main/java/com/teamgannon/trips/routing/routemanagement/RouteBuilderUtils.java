package com.teamgannon.trips.routing.routemanagement;

import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.routing.model.Route;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


@Slf4j
public class RouteBuilderUtils {

    private final TripsContext tripsContext;
    /**
     * route display vars
     */
    private final RouteDisplay routeDisplay;
    private final RouteGraphicsUtil routeGraphicsUtil;


    /**
     * constructor
     *
     * @param routeDisplay the route display
     */
    public RouteBuilderUtils(TripsContext tripsContext,
                             RouteDisplay routeDisplay,
                             RouteGraphicsUtil routeGraphicsUtil) {
        this.tripsContext = tripsContext;
        this.routeDisplay = routeDisplay;
        this.routeGraphicsUtil = routeGraphicsUtil;
    }


    /**
     * get the embedded object associated with the star in the lookup
     *
     * @param starId the id
     * @return the embedded object
     */
    public @Nullable StarDisplayRecord getStar(String starId) {
        Node star = tripsContext.getCurrentPlot().getStar(starId);
        if (star != null) {
            return (StarDisplayRecord) star.getUserData();
        } else {
            return null;
        }
    }

    /**
     * this checks if all the stars on the route can be seen for a route
     *
     * @param route the route definition
     * @return true means we can plot the whole route
     */
    public boolean checkIfWholeRouteCanBePlotted(@NotNull Route route) {
        return route.getRouteStars().stream().allMatch(tripsContext.getCurrentPlot().getStarLookup()::containsKey);
    }

    /**
     * plot a route descriptor
     *
     * @param routeDescriptor the route descriptor
     */
    public void plotRouteDescriptor(@NotNull RouteDescriptor routeDescriptor) {

        Group routeGraphic = createRoute(routeDescriptor);

        // affix the route to the display
        routeDisplay.addRouteToDisplay(routeDescriptor, routeGraphic);
        routeDisplay.toggleRouteVisibility(true);
        routeDisplay.setManualRoutingActive(false);

        // register the route in the current plot
        tripsContext.getCurrentPlot().addRoute(routeDescriptor.getId(), routeDescriptor);
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
        for (Point3D point3D : routeDescriptor.getRouteCoordinates()) {
            if (firstLink) {
                previousPoint = point3D;
                firstLink = false;
            } else {
                double length = routeDescriptor.getLengthList().get(i++);
                Label lengthLabel = routeGraphicsUtil.createLabel(firstLink, length);
                // create the line segment
                Node lineSegment = routeGraphicsUtil.createLineSegment(previousPoint, point3D, routeDescriptor.getLineWidth(), routeDescriptor.getColor(), lengthLabel);
                // step along the segment
                previousPoint = point3D;

                // add the completed line segment to overall list
                route.getChildren().add(lineSegment);
            }

        }
        return route;
    }

}
