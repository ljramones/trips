package com.teamgannon.trips.events;

import com.teamgannon.trips.graphics.entities.RouteVisibility;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Map;
import java.util.UUID;


@Getter
public class RoutingPanelUpdateEvent extends ApplicationEvent {
    private final DataSetDescriptor dataSetDescriptor;
    private final Map<UUID, RouteVisibility> routeVisibilityMap;

    public RoutingPanelUpdateEvent(Object source, DataSetDescriptor dataSetDescriptor, Map<UUID, RouteVisibility> routeVisibilityMap) {
        super(source);
        this.dataSetDescriptor = dataSetDescriptor;
        this.routeVisibilityMap = routeVisibilityMap;
    }
}
