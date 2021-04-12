package com.teamgannon.trips.routing.tree.treemodel;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RouteSegment {

    private String fromStar;

    private String toStar;

    private double length;

    public String toString() {
        return String.format("%s --> %s :: %.2f ly",fromStar, toStar,length);
    }

}
