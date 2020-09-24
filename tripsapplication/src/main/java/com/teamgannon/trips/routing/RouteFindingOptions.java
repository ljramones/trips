package com.teamgannon.trips.routing;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RouteFindingOptions {

    private double upperBound;

    private double lowerBound;

    private String originStar;

    private String destinationStar;

    private boolean selected;

}
