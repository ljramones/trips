package com.teamgannon.trips.events;

import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event triggered when a new route is created.
 */
@Getter
public class NewRouteEvent extends ApplicationEvent {

    private final DataSetDescriptor dataSetDescriptor;
    private final RouteDescriptor routeDescriptor;

    /**
     * Creates a new NewRouteEvent.
     *
     * @param source            The object that fired the event
     * @param dataSetDescriptor The dataset containing the route
     * @param routeDescriptor   The new route descriptor
     */
    public NewRouteEvent(Object source, DataSetDescriptor dataSetDescriptor, RouteDescriptor routeDescriptor) {
        super(source);
        this.dataSetDescriptor = dataSetDescriptor;
        this.routeDescriptor = routeDescriptor;
    }

}
