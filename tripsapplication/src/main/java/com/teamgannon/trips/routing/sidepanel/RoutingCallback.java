package com.teamgannon.trips.routing.sidepanel;

import com.teamgannon.trips.routing.tree.treemodel.RouteTree;
import javafx.scene.paint.Color;

public interface RoutingCallback {

    void triggerColorChange(Object routeTree, Color color);
}
