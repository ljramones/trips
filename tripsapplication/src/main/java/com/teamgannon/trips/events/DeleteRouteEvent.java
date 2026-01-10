package com.teamgannon.trips.events;

import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event triggered when a route is deleted.
 */
@Getter
public class DeleteRouteEvent extends ApplicationEvent {

    private final RouteDescriptor routeDescriptor;

    /**
     * Creates a new DeleteRouteEvent.
     *
     * @param source          The object that fired the event
     * @param routeDescriptor The route descriptor to delete
     */
    public DeleteRouteEvent(Object source, RouteDescriptor routeDescriptor) {
        super(source);
        this.routeDescriptor = routeDescriptor;
    }

}
