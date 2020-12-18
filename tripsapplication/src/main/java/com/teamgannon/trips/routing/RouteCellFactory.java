package com.teamgannon.trips.routing;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import org.jetbrains.annotations.NotNull;

public class RouteCellFactory implements Callback<ListView<Route>, ListCell<Route>> {

    @Override
    public @NotNull ListCell<Route> call(ListView<Route> routeListView) {
        return new RouteCell();
    }

}