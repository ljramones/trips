package com.teamgannon.trips.graphics.operators;

import com.teamgannon.trips.graphics.entities.RouteDescriptor;

public interface RouteUpdater {

    /**
     * triggered when a new route is created
     *
     * @param routeDescriptor the route descriptor
     */
    void newRoute(RouteDescriptor routeDescriptor);

    /**
     * triggered when an existing route changes
     *
     * @param routeDescriptor the route descriptor
     */
    void updateRoute(RouteDescriptor routeDescriptor);

    /**
     * triggered when a route is removed
     *
     * @param routeDescriptor the route descriptor
     */
    void deleteRoute(RouteDescriptor routeDescriptor);

}
