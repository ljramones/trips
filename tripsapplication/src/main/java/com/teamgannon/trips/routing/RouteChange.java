package com.teamgannon.trips.routing;

import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RouteChange {

    private boolean changed;

    private Route route;

}
