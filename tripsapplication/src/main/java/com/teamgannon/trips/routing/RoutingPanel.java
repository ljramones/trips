package com.teamgannon.trips.routing;

import com.teamgannon.trips.controller.MainPane;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.listener.RouteUpdaterListener;
import javafx.beans.Observable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class RoutingPanel extends Pane {

    /**
     * the set of routings
     */
    private final ListView<Route> routingListView = new ListView<>();

    /**
     * the constructor
     */
    public RoutingPanel(RouteUpdaterListener routeUpdaterListener) {
        routingListView.setPrefHeight(800);
        routingListView.setPrefWidth(MainPane.SIDE_PANEL_SIZE);
        routingListView.setCellFactory(new RouteCellFactory(routeUpdaterListener));
        routingListView.getSelectionModel().selectedItemProperty().addListener(this::routingChanged);
        routingListView.setPlaceholder(new Label("No routes in this dataset"));

        this.getChildren().add(routingListView);
    }

    private void routingChanged(Observable observable) {

    }

    /**
     * set the dataset context
     *
     * @param descriptor the descriptor
     */
    public void setContext(@Nullable DataSetDescriptor descriptor) {

        routingListView.getItems().clear();

        if (descriptor != null) {
            List<Route> routeList = descriptor.getRoutes();
            if (routeList.size() != 0) {
                routeList.forEach(route -> routingListView.getItems().add(route));
                log.info("adding routes");
            }

        } else {
            log.error("This dataset should be there but we couldn't find it");
        }

    }

}
