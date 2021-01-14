package com.teamgannon.trips.graphics.entities;

import com.teamgannon.trips.routing.Route;
import javafx.geometry.Point3D;
import javafx.scene.paint.Color;
import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import javax.persistence.Transient;
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
     * total length of the route
     */
    private double totalLength;

    /**
     * width of the routing line
     */
    private double lineWidth;

    /**
     * defined color of route
     */
    @Builder.Default
    private @NotNull Color color = Color.WHITE;

    /**
     * list of ordered routes, min length is 2
     */
    @Builder.Default
    private @NotNull List<UUID> routeList = new ArrayList<>();

    /**
     * list of star names
     */
    @Builder.Default
    private @NotNull List<String> nameList = new ArrayList<>();

    /**
     * list of ordered lengths, min length is 2
     */
    @Builder.Default
    private @NotNull List<Double> lengthList = new ArrayList<>();

    /**
     * we keep track of the from star for calculations
     */
    @Transient
    private StarDisplayRecord lastStar;

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
    private @NotNull List<Point3D> lineSegments = new ArrayList<>();

    public void addLineSegment(Point3D point3D) {
        lineSegments.add(point3D);
    }

    public void addLengthSegment(Double length) {
        lengthList.add(length);
    }

    public void clear() {
        name = "";
        routeList.clear();
        color = Color.LIGHTCORAL;
        lineSegments.clear();
    }

    public @NotNull Route toRoute() {
        Route route = new Route();
        route.setRouteName(this.name);
        route.getRouteStars().addAll(routeList);
        route.getRouteLengths().addAll(lengthList);
        route.setRouteNotes(this.routeNotes);
        route.setStartingStar(this.startStar);
        route.setRouteColor(this.color.toString());
        return route;
    }

    public static RouteDescriptor toRouteDescriptor(@NotNull Route route) {
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
