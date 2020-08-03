package com.teamgannon.trips.graphics.entities;

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
    private Color color = Color.LIGHTCORAL;

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
}
