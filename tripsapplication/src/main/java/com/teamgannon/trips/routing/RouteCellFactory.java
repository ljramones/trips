package com.teamgannon.trips.routing;

import com.teamgannon.trips.listener.RouteUpdaterListener;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import org.jetbrains.annotations.NotNull;

public class RouteCellFactory implements Callback<ListView<Route>, ListCell<Route>> {

    private RouteUpdaterListener routeUpdaterListener;

    public RouteCellFactory(RouteUpdaterListener routeUpdaterListener) {
        this.routeUpdaterListener = routeUpdaterListener;
    }

    @Override
    public @NotNull ListCell<Route> call(ListView<Route> routeListView) {
        return new RouteCell(routeUpdaterListener);
    }

}