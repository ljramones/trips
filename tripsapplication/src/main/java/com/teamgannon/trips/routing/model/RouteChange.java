package com.teamgannon.trips.routing.model;

import com.teamgannon.trips.routing.tree.treemodel.RouteTree;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RouteChange {

    private boolean changed;

    private RouteTree routeTree;

}
