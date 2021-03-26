package com.teamgannon.trips.routing.tree;


import com.teamgannon.trips.controller.MainPane;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.listener.RouteUpdaterListener;
import com.teamgannon.trips.routing.Route;
import com.teamgannon.trips.routing.RouteCellFactory;
import javafx.beans.Observable;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Pane;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class RoutiingTreeViewPanel extends Pane {

    /**
     * the set of routings as a tree
     */
    private final TreeView<Route> routingListView = new TreeView<>();

    /**
     * updater
     */
    private final RouteUpdaterListener routeUpdaterListener;

    public RoutiingTreeViewPanel(RouteUpdaterListener routeUpdaterListener) {
        this.routeUpdaterListener = routeUpdaterListener;

        routingListView.setPrefHeight(800);
        routingListView.setPrefWidth(MainPane.SIDE_PANEL_SIZE);
        routingListView.getSelectionModel().selectedItemProperty().addListener(this::routingChanged);
        routingListView.getSelectionModel().selectedItemProperty().addListener(this::routingChanged);
        routingListView.setCellFactory(new RouteTreeCellFactory(routeUpdaterListener));


        this.getChildren().add(routingListView);
//        routingListView.setPlaceholder(new Label("No routes in this dataset"));

    }

    private void routingChanged(Observable observable) {

    }

    /**
     * set the dataset context
     *
     * @param descriptor the descriptor
     */
    public void setContext(@Nullable DataSetDescriptor descriptor) {
        routingListView.getRoot().getChildren().clear();
        if (descriptor != null) {
            List<Route> routeList = descriptor.getRoutes();
            if (routeList.size() != 0) {
//                routeList.forEach(route -> routingListView.getItems().add(route));
                // add a tree

                log.info("adding routes");
            }

        } else {
            log.error("This dataset should be there but we couldn't find it");
        }

    }




}
