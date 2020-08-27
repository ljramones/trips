package com.teamgannon.trips.routing;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class RouteCellFactory implements Callback<ListView<Route>, ListCell<Route>> {


    public RouteCellFactory() {
    }

    @Override
    public ListCell<Route> call(ListView<Route> routeListView) {
        return new RouteCell();
    }

}