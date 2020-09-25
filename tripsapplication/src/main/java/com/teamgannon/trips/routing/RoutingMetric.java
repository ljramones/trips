package com.teamgannon.trips.routing;

import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoutingMetric {

    private String path;

    private RouteDescriptor routeDescriptor;

    private int rank;

    private int numberOfSegments;

    private double totalLength;

}
