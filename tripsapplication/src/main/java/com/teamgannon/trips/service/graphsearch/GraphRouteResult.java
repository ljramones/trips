package com.teamgannon.trips.service.graphsearch;

import com.teamgannon.trips.routing.PossibleRoutes;
import com.teamgannon.trips.routing.model.SparseStarRecord;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class GraphRouteResult {

    private boolean routeFound;

    private boolean searchCancelled;

    private String message;

    private PossibleRoutes possibleRoutes;

}
