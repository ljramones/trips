package com.teamgannon.trips.dialogs.routing;

import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RouteSelector {

    /**
     * if true then we picked a route
     */
    private boolean selected;

    /**
     * the route descriptor we want to plot
     */
    private RouteDescriptor routeDescriptor;

}
