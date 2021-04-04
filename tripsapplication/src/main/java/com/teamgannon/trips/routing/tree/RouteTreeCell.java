package com.teamgannon.trips.routing.tree;

import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.listener.RouteUpdaterListener;
import com.teamgannon.trips.routing.Route;
import com.teamgannon.trips.routing.RouteChange;
import com.teamgannon.trips.routing.dialogs.RouteEditDialog;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@Slf4j
public class RouteTreeCell extends TreeCell<Route> {

    // We want to create a single Tooltip that will be reused, as needed. We will simply update the text
    // for the Tooltip for each cell
    final Tooltip tooltip = new Tooltip();
    private final RouteUpdaterListener routeUpdaterListener;

    public RouteTreeCell(RouteUpdaterListener routeUpdaterListener) {
        this.routeUpdaterListener = routeUpdaterListener;
    }

    @Override
    public void updateItem(@Nullable Route route, boolean empty) {
        super.updateItem(route, empty);

        int index = this.getIndex();
        String name = null;

        ContextMenu contextMenu = new ContextMenu();
        MenuItem editMenuItem = new MenuItem("Edit");
        editMenuItem.setOnAction((event) -> {
            log.info("edit Route");
            editRoute(route);
        });
        contextMenu.getItems().add(editMenuItem);

        MenuItem deleteMenuItem = new MenuItem("Delete");
        deleteMenuItem.setOnAction(event -> {
            log.info("delete Route");
            deleteRoute(route);
        });
        contextMenu.getItems().add(deleteMenuItem);

        // Format name
        if (route != null && !empty) {

        }

        this.setText(name);
        setGraphic(null);
    }

    private String showRoute(int count, Route route) {
        return "Route: " + (count + 1) + ": \n\t" + route.getRouteName() + "\n\t has " +
                (route.getRouteStars().size() - 1) + " segments\n" +
                "\tlength of route =" + String.format("%.2f", getTotalRouter(route)) + " ly\n" +
                routeItinerary(route) + "\n";
    }

    private double getTotalRouter(Route route) {
        double totalLength = 0;
        for (double length : route.getRouteLengths()) {
            totalLength += length;
        }
        return totalLength;
    }

    private String routeItinerary(Route route) {
        boolean first = true;
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < route.getRouteStarNames().size(); i++) {
            String length;
            if (first) {
                length = "Start";
                first = false;
            } else {
                length = String.format("%.2f", route.getRouteLengths().get(i - 1)) + " ly";
            }
            str.append("\t ->").append(route.getRouteStarNames().get(i)).append(" - ").append(length).append("\n");
        }
        return str.toString();
    }

    private void deleteRoute(Route route) {
        routeUpdaterListener.deleteRoute(RouteDescriptor.toRouteDescriptor(route));
    }

    private void editRoute(Route route) {
        RouteEditDialog routeEditDialog = new RouteEditDialog(route);
        Optional<RouteChange> routeChangeOptional = routeEditDialog.showAndWait();
        if (routeChangeOptional.isPresent()) {
            RouteChange routeChange = routeChangeOptional.get();
            if (routeChange.isChanged()) {
                routeUpdaterListener.updateRoute(RouteDescriptor.toRouteDescriptor(route));
            }
        }
    }
}
