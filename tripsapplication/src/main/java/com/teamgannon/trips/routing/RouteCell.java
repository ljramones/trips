package com.teamgannon.trips.routing;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

@Slf4j
public class RouteCell extends ListCell<Route> {

    // We want to create a single Tooltip that will be reused, as needed. We will simply update the text
    // for the Tooltip for each cell
    final Tooltip tooltip = new Tooltip();


    @Override
    public void updateItem(@Nullable Route route, boolean empty) {
        super.updateItem(route, empty);

        int index = this.getIndex();
        String name = null;

        ContextMenu contextMenu = new ContextMenu();
        MenuItem eitMenuItem = new MenuItem("Edit");
        eitMenuItem.setOnAction((event) -> {
            log.info("edit Route");
//            updater.showNewStellarData(true, false);
        });

        contextMenu.getItems().addAll(eitMenuItem);

        // Format name
        if (route != null && !empty) {
            name = "Route: " + (index + 1) + ": " +
                    route.getRouteName() + " has " +
                    (route.getRouteStars().size() - 1) + " segments";

            log.info("show route:{}", name);

            tooltip.setText("tooltip here");
            setTooltip(tooltip);
            setContextMenu(contextMenu);
        }

        this.setText(name);
        setGraphic(null);
    }

}
