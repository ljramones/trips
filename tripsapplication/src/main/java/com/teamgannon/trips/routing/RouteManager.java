package com.teamgannon.trips.routing;

import com.teamgannon.trips.graphics.entities.CustomObjectFactory;
import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.graphics.entities.StellarEntityFactory;
import com.teamgannon.trips.graphics.entities.Xform;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.listener.RouteUpdaterListener;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class RouteManager {

    private DataSetDescriptor dataSetDescriptor;
    private final RouteUpdaterListener routeUpdaterListener;

    private final double lineWidth = 0.5;

    /**
     * this is the descriptor of the current route
     */
    private RouteDescriptor currentRoute;
    /**
     * the graphic portion of the current route
     */
    private Xform currentRouteDisplay = new Xform();
    /**
     * whether there is a route being traced, true is yes
     */
    private boolean routingActive = false;

    private final Xform routesGroup = new Xform();

    public RouteManager(RouteUpdaterListener routeUpdaterListener) {
        this.routeUpdaterListener = routeUpdaterListener;

        currentRouteDisplay = new Xform();
        currentRouteDisplay.setWhatAmI("Current Route");

        routesGroup.setWhatAmI("Star Routes");
    }


    public void finishRoute(Map<String, String> properties) {
        createRouteSegment(properties);
        routingActive = false;
        makeRoutePermanent(currentRoute);
        routeUpdaterListener.newRoute(dataSetDescriptor, currentRoute);
    }

    public void makeRoutePermanent(com.teamgannon.trips.graphics.entities.RouteDescriptor currentRoute) {
        // remove our hand drawn route
        routesGroup.getChildren().remove(currentRouteDisplay);

        // create a new one based on descriptor
        Xform displayRoute = createDisplayRoute(currentRoute);

        // add this created one to the routes group
        routesGroup.getChildren().add(displayRoute);
    }


    public Xform createDisplayRoute(com.teamgannon.trips.graphics.entities.RouteDescriptor currentRoute) {
        Xform route = new Xform();
        route.setWhatAmI(currentRoute.getName());
        Point3D previousPoint = new Point3D(0, 0, 0);
        boolean firstPoint = true;
        for (Point3D point3D : currentRoute.getLineSegments()) {
            if (firstPoint) {
                firstPoint = false;
            } else {
                Node lineSegment = CustomObjectFactory.createLineSegment(previousPoint, point3D, lineWidth, currentRoute.getColor());
                route.getChildren().add(lineSegment);
            }
            previousPoint = point3D;
        }
        return route;
    }

    public void startRoute(RouteDescriptor routeDescriptor, Map<String, String> properties) {
        routingActive = true;
        currentRoute = routeDescriptor;
        log.info("Start charting the route:" + routeDescriptor);
        double x = Double.parseDouble(properties.get("x"));
        double y = Double.parseDouble(properties.get("y"));
        double z = Double.parseDouble(properties.get("z"));
        UUID id = UUID.fromString(properties.get("recordId"));
        if (currentRoute != null) {
            Point3D toPoint3D = new Point3D(x, y, z);
            currentRoute.getLineSegments().add(toPoint3D);
            currentRoute.getRouteList().add(id);
            routesGroup.getChildren().add(currentRouteDisplay);
            routesGroup.setVisible(true);
        }
    }


    public void continueRoute(Map<String, String> properties) {
        if (routingActive) {
            createRouteSegment(properties);
            log.info("Next Routing step:{}", currentRoute);
        }
    }

    public void createRouteSegment(Map<String, String> properties) {
        double x = Double.parseDouble(properties.get("x"));
        double y = Double.parseDouble(properties.get("y"));
        double z = Double.parseDouble(properties.get("z"));
        UUID id = UUID.fromString(properties.get("recordId"));

        if (currentRoute != null) {
            int size = currentRoute.getLineSegments().size();
            Point3D fromPoint = currentRoute.getLineSegments().get(size - 1);
            Point3D toPoint3D = new Point3D(x, y, z);
            Node lineSegment = CustomObjectFactory.createLineSegment(
                    fromPoint, toPoint3D, 0.5, currentRoute.getColor()
            );
            currentRouteDisplay.getChildren().add(lineSegment);
            currentRoute.getLineSegments().add(toPoint3D);
            currentRoute.getRouteList().add(id);
            currentRouteDisplay.setVisible(true);
        }
    }

    /**
     * reset the route and remove the parts that were partially drawn
     *
     * @param properties the properties
     */
    public void resetRoute(Map<String, String> properties) {
        if (currentRoute != null) {
            currentRoute.clear();
        }
        routesGroup.getChildren().remove(currentRouteDisplay);
        routingActive = false;
        createCurrentRouteDisplay();
        resetCurrentRoute();
        log.info("Resetting the route");
    }


    public void createCurrentRouteDisplay() {
        currentRouteDisplay = new Xform();
        currentRouteDisplay.setWhatAmI("Current Route");
    }

    /**
     * reset the current route
     */
    public void resetCurrentRoute() {
        currentRoute.clear();
        currentRouteDisplay = new Xform();
        currentRouteDisplay.setWhatAmI("Current Route");
    }

    public void setDatasetContext(DataSetDescriptor dataSetDescriptor) {
        this.dataSetDescriptor = dataSetDescriptor;
    }


    /**
     * plot the routes
     *
     * @param routeList the list of routes
     */
    public void plotRoutes(List<RouteDescriptor> routeList) {
        // clear existing routes
        routesGroup.getChildren().clear();
        routeList.forEach(this::plotRoute);
    }

    public void plotRoute(RouteDescriptor routeDescriptor) {
        Xform route = StellarEntityFactory.createRoute(routeDescriptor);
        routesGroup.getChildren().add(route);
        routesGroup.setVisible(true);
    }


    public void createRoute(RouteDescriptor currentRoute) {
        this.currentRoute = currentRoute;
    }


    public void completeRoute() {

        // trigger that a new route has been created
        if (routeUpdaterListener != null) {
            routeUpdaterListener.newRoute(dataSetDescriptor, currentRoute);
        }
    }

    /**
     * clear the routes
     */
    public void clearRoutes() {
        // clear the routes
        routesGroup.getChildren().clear();
    }


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


    public void redrawRoutes() {
//        if (dataSetDescriptor != null) {
//            List<Route> routeList = dataSetDescriptor.getRoutes();
//            for (Route routeDescriptor : routeList) {
//                Xform route = new Xform();
//                List<UUID> stars = routeDescriptor.getRouteStars();
//                UUID firstStar = stars.get(0);
//                Node star = starLookup.get(firstStar);
//                for (int i = 1; i < stars.size(); i++) {
//                    UUID nextStar = stars.get(i);
//                    drawSegment(firstStar, nextStar, routeDescriptor.getRouteColor(), routeDescriptor.getRouteType());
//                    firstStar = nextStar;
//                }
//
//            }
//        }
    }

    private void drawSegment(UUID firstStar, UUID nextStar, String routeColor, UUID routeType) {

    }

}
