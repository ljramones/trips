package com.teamgannon.trips.routing.list;

import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.listener.RouteUpdaterListener;
import com.teamgannon.trips.routing.RouteChange;
import com.teamgannon.trips.routing.dialogs.RouteEditDialog;
import com.teamgannon.trips.routing.tree.treemodel.RouteSegment;
import com.teamgannon.trips.routing.tree.treemodel.RouteTree;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.shape.Rectangle;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class RouteCell extends ListCell<RouteTree> {

    // We want to create a single Tooltip that will be reused, as needed. We will simply update the text
    // for the Tooltip for each cell
    final Tooltip tooltip = new Tooltip();
    private final RouteUpdaterListener routeUpdaterListener;

    public RouteCell(RouteUpdaterListener routeUpdaterListener) {
        this.routeUpdaterListener = routeUpdaterListener;
    }

    @Override
    public void updateItem(@Nullable RouteTree routeTree, boolean empty) {
        if (routeTree != null) {
            super.updateItem(routeTree, empty);

            int index = this.getIndex();
            String name = null;

            ContextMenu contextMenu = new ContextMenu();
            MenuItem editMenuItem = new MenuItem("Edit");
            editMenuItem.setOnAction((event) -> {
                log.info("edit Route");
                editRoute(routeTree);
            });
            contextMenu.getItems().add(editMenuItem);

            MenuItem deleteMenuItem = new MenuItem("Delete");
            deleteMenuItem.setOnAction(event -> {
                log.info("delete Route");
                deleteRoute(routeTree);
            });
            contextMenu.getItems().add(deleteMenuItem);

            // Format name
            if (routeTree != null && !empty) {
                name = showRoute(index, routeTree);

                log.info("show route:{}", name);

                tooltip.setText("tooltip here");
                setTooltip(tooltip);
                setContextMenu(contextMenu);
            }

            this.setText(name);
//            if (routeTree.getRouteColor() != null) {
//                this.setBackground(routeTree.getRouteColor());
//            }
        }
    }

    private String showRoute(int count, RouteTree routeTree) {
        return "Route: " + (count + 1) + ": \n\t" + routeTree.getRouteName() + "\n\t has " +
                (routeTree.getRouteSegmentList().size()) + " segments\n" +
                "\tlength of route =" + String.format("%.2f", routeTree.getTotalLength()) + " ly\n" +
                "\tRoute segments are \n"+
                routeItinerary(routeTree) + "\n";
    }

    private String routeItinerary(RouteTree routeTree) {
        return routeTree.getRouteSegmentList().stream().map(routeSegment -> "\t\t" + routeSegment + "\n").collect(Collectors.joining());
    }

    private void deleteRoute(RouteTree routeTree) {
        routeUpdaterListener.deleteRoute(RouteDescriptor.toRouteDescriptor(routeTree));
    }

    private void editRoute(RouteTree routeTree) {
        RouteEditDialog routeEditDialog = new RouteEditDialog(routeTree);
        Optional<RouteChange> routeChangeOptional = routeEditDialog.showAndWait();
        if (routeChangeOptional.isPresent()) {
            RouteChange routeChange = routeChangeOptional.get();
            if (routeChange.isChanged()) {
                routeUpdaterListener.updateRoute(RouteTree.toRouteDescriptor(routeTree));
            }
        }
    }

}
