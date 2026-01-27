package com.teamgannon.trips.routing.tree.treemodel;

import lombok.Builder;
import lombok.Data;

/**
 * Display-oriented representation of a route segment.
 * <p>
 * This class stores human-readable information about a route segment
 * for UI display purposes, including star names and distance.
 * <p>
 * Note: This is distinct from {@link com.teamgannon.trips.routing.model.RouteSegment}
 * which stores 3D coordinates for geometric calculations.
 */
@Data
@Builder
public class RouteSegmentDisplay {

    /**
     * Name of the starting star for this segment.
     */
    private String fromStar;

    /**
     * Name of the destination star for this segment.
     */
    private String toStar;

    /**
     * Distance of this segment in light years.
     */
    private double length;

    @Override
    public String toString() {
        return "%s --> %s :: %.2f ly".formatted(fromStar, toStar, length);
    }

}
