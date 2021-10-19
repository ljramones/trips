package com.teamgannon.trips.routing.list;

import com.teamgannon.trips.listener.RouteUpdaterListener;
import com.teamgannon.trips.routing.tree.treemodel.RouteTree;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import org.jetbrains.annotations.NotNull;

public class RouteCellFactory implements Callback<ListView<RouteTree>, ListCell<RouteTree>> {

    private RouteUpdaterListener routeUpdaterListener;

    public RouteCellFactory(RouteUpdaterListener routeUpdaterListener) {
        this.routeUpdaterListener = routeUpdaterListener;
    }

    @Override
    public @NotNull ListCell<RouteTree> call(ListView<RouteTree> routeListView) {
        return new RouteCell(routeUpdaterListener);
    }

}