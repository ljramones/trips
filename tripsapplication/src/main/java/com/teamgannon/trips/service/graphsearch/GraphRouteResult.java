package com.teamgannon.trips.service.graphsearch;

import com.teamgannon.trips.routing.model.PossibleRoutes;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GraphRouteResult {

    private boolean routeFound;

    private boolean searchCancelled;

    private String message;

    private PossibleRoutes possibleRoutes;

}
