package com.teamgannon.trips.routing.tree.treemodel;

import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.graphics.entities.RouteVisibility;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.routing.model.Route;
import javafx.scene.paint.Color;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class RouteTree {

    /**
     * unique id for reference
     */
    private UUID uuid;

    /**
     * Name given to this route.  May default to “Start star to End Star” when generated, but it can be
     * user edited to anything.
     */
    private String routeName;

    /**
     * the embedded descriptor
     */
    private DataSetDescriptor descriptor;

    /**
     * the original route
     */
    private String embeddedRoute;

    /**
     * whether this is displayed or not
     */
    private boolean checked;

    /**
     * the visibility of the route
     * FULL : all stars in the route visible
     * PARTIAL: only a limited number of stars visible
     * INVISIBLE: none of the stars visible
     */
    private RouteVisibility visibility;

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

    private int count = 0;

    /**
     * the line width to draw
     */
    private double lineWidth;

    public String getRoute() {
        return "Route: " + (count + 1) + ": \n\t" + this.getRouteName() + "\n\t has " +
                (this.getRouteSegmentList().size()) + " segments\n" +
                "\tlength of route =" + String.format("%.2f", this.getTotalLength()) + " ly\n" +
                "\tRoute segments are \n" +
                routeItinerary(this) + "\n";
    }

    private String routeItinerary(RouteTree routeTree) {
        return routeTree.getRouteSegmentList().stream().map(routeSegment -> "\t\t" + routeSegment + "\n").collect(Collectors.joining());
    }

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
                .visibility(routeTree.getVisibility())
                .totalLength(routeTree.getTotalLength())
                .build();
    }

    public void addRouteSegment(RouteSegment routeSegment) {
        routeSegmentList.add(routeSegment);
    }

    public static RouteTree createRouteTree(Route route, RouteVisibility routeVisibility) {
        RouteTree routeTree = new RouteTree();

        routeTree.setUuid(route.getUuid());
        routeTree.setVisibility(Objects.requireNonNullElse(routeVisibility, RouteVisibility.OFFSCREEN));
        routeTree.setRouteName(route.getRouteName());
        routeTree.setChecked(true);
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
