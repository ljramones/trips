package com.teamgannon.trips.routing.model;

import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.routing.model.SparseStarRecord;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class RoutingMetric {

    private String path;

    private RouteDescriptor routeDescriptor;

    private int rank;

    private int numberOfSegments;

    private double totalLength;

    private Map<String, SparseStarRecord> starRecordMap;

}
