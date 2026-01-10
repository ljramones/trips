package com.teamgannon.trips.events;

import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event triggered when an existing route is updated.
 */
@Getter
public class UpdateRouteEvent extends ApplicationEvent {

    private final RouteDescriptor routeDescriptor;

    /**
     * Creates a new UpdateRouteEvent.
     *
     * @param source          The object that fired the event
     * @param routeDescriptor The updated route descriptor
     */
    public UpdateRouteEvent(Object source, RouteDescriptor routeDescriptor) {
        super(source);
        this.routeDescriptor = routeDescriptor;
    }

}
