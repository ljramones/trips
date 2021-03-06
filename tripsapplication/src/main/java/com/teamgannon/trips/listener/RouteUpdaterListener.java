package com.teamgannon.trips.listener;

import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;

public interface RouteUpdaterListener {

    /**
     * updates the routing status
     */
    void routingStatus(boolean statusFlag);

    /**
     * triggered when a new route is created
     *
     * @param routeDescriptor the route descriptor
     */
    void newRoute(DataSetDescriptor dataSetDescriptor, RouteDescriptor routeDescriptor);

    /**
     * triggered when an existing route changes
     *
     * @param routeDescriptor the route descriptor
     */
    void updateRoute(RouteDescriptor routeDescriptor);

    /**
     * choose which routes to display
     *
     * @param routeDescriptor the route descriptor
     * @param state           true is show, false is hide
     */
    void displayRoute(RouteDescriptor routeDescriptor, boolean state);

    /**
     * triggered when a route is removed
     *
     * @param routeDescriptor the route descriptor
     */
    void deleteRoute(RouteDescriptor routeDescriptor);

}
