package com.teamgannon.trips.graphics.entities;

import com.teamgannon.trips.routing.Route;
import javafx.geometry.Point3D;
import javafx.scene.paint.Color;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class RouteDescriptor {

    /**
     * name of the route
     */
    private String name;

    /**
     * max length of each segment
     */
    private double maxLength;

    /**
     * defined color of route
     */
    @Builder.Default
    private Color color = Color.WHITE;

    /**
     * list of ordered routes, min length is 2
     */
    @Builder.Default
    private List<UUID> routeList = new ArrayList<>();

    /**
     * start star name
     */
    private String startStar;

    /**
     * the notes for the route
     */
    private String routeNotes;

    /**
     * the list of line segments joined by 3D points
     */
    @Builder.Default
    private List<Point3D> lineSegments = new ArrayList<>();

    public void clear() {
        name = "";
        routeList.clear();
        color = Color.LIGHTCORAL;
        lineSegments.clear();
    }

    public Route toRoute() {
        Route route = new Route();
        route.setRouteName(this.name);
        route.getRouteStars().addAll(routeList);
        route.setRouteNotes(this.routeNotes);
        route.setStartingStar(this.startStar);
        route.setRouteColor(this.color.toString());
        return route;
    }
    public static RouteDescriptor toRouteDescriptor(Route route) {
        return RouteDescriptor
                .builder()
                .name(route.getRouteName())
                .maxLength(route.getRouteStars().size())
                .color(Color.valueOf(route.getRouteColor()))
                .routeNotes(route.getRouteNotes())
                .startStar(route.getStartingStar())
                .build();
    }




}
