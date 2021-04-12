package com.teamgannon.trips.routing.tree.treemodel;

import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.routing.Route;
import javafx.scene.paint.Color;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class RouteTree {

    /**
     * unique id for reference
     */
    private UUID uuid;

    /**
     * Name given to this route.  May default to “Start star to End Star” when generated but it can be
     * user edited to anything.
     */
    private String routeName;

    /**
     * the embded descriptor
     */
    private DataSetDescriptor descriptor;

    /**
     * the original route
     */
    private String embeddedRoute;

    /**
     * the star this route starts at
     */
    private String startingStar;

    /**
     * the star this route starts at
     */
    private String endingStar;

    /**
     * the total length of the star
     */
    private double totalLength;

    /**
     * the notes for the route
     */
    private String routeNotes;

    /**
     * the color we selected for the route
     */
    private Color routeColor;

    /**
     * the line width to draw
     */
    private double lineWidth;

    /**
     * the list of route segments
     */
    private List<RouteSegment> routeSegmentList = new ArrayList<>();

    public static RouteDescriptor toRouteDescriptor(RouteTree routeTree) {
        return RouteDescriptor
                .builder()
                .id(routeTree.getUuid())
                .descriptor(routeTree.getDescriptor())
                .name(routeTree.getRouteName())
                .color(routeTree.getRouteColor())
                .lineWidth(routeTree.getLineWidth())
                .routeNotes(routeTree.getRouteNotes())
                .maxLength(routeTree.getRouteSegmentList().size())
                .totalLength(routeTree.getTotalLength())
                .build();
    }

    public void addRouteSegment(RouteSegment routeSegment) {
        routeSegmentList.add(routeSegment);
    }

    public static RouteTree createRouteTree(Route route) {
        RouteTree routeTree = new RouteTree();

        routeTree.setUuid(route.getUuid());
        routeTree.setRouteName(route.getRouteName());
        routeTree.setRouteColor(Color.valueOf(route.getRouteColor()));
        routeTree.setRouteNotes(route.getRouteNotes());
        routeTree.setLineWidth(route.getLineWidth());
        routeTree.setStartingStar(route.getStartingStar());
        routeTree.setEndingStar(route.getRouteStarNames().get(route.getRouteStarNames().size() - 1));
        double tLength = route.getRouteLengths().stream().mapToDouble(len -> len).sum();
        routeTree.setTotalLength(tLength);

        // create route segments
        String startSegStar = routeTree.startingStar;
        for (int i = 0; i < route.getRouteLengths().size(); i++) {
            String endSegStar = route.getRouteStarNames().get(i + 1);
            double length = route.getRouteLengths().get(i);
            RouteSegment routeSegment = RouteSegment.builder().fromStar(startSegStar).toStar(endSegStar).length(length).build();
            routeTree.addRouteSegment(routeSegment);
            startSegStar = endSegStar;
        }

        return routeTree;
    }

}
