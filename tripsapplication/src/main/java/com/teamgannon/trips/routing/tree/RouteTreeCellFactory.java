package com.teamgannon.trips.routing.tree;

import com.teamgannon.trips.listener.RouteUpdaterListener;
import com.teamgannon.trips.routing.Route;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

public class RouteTreeCellFactory implements Callback<TreeView<Route>, TreeCell<Route>> {

    private RouteUpdaterListener routeUpdaterListener;

    public RouteTreeCellFactory(RouteUpdaterListener routeUpdaterListener) {
        this.routeUpdaterListener = routeUpdaterListener;
    }


    @Override
    public TreeCell<Route> call(TreeView<Route> routeTreeView) {
        return new RouteTreeCell(routeUpdaterListener);
    }
}
