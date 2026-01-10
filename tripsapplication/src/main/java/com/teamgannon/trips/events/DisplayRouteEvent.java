package com.teamgannon.trips.events;

import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event triggered when a route's display state changes.
 */
@Getter
public class DisplayRouteEvent extends ApplicationEvent {

    private final RouteDescriptor routeDescriptor;
    private final boolean visible;

    /**
     * Creates a new DisplayRouteEvent.
     *
     * @param source          The object that fired the event
     * @param routeDescriptor The route descriptor
     * @param visible         true to show the route, false to hide
     */
    public DisplayRouteEvent(Object source, RouteDescriptor routeDescriptor, boolean visible) {
        super(source);
        this.routeDescriptor = routeDescriptor;
        this.visible = visible;
    }

}
