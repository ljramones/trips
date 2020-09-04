package com.teamgannon.trips.routing;

import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.service.DatabaseManagementService;
import javafx.beans.Observable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class RoutingPanel extends Pane {

    private String datasetname;

    private final DatabaseManagementService databaseManagementService;

    private final ListView<Route> routingListView = new ListView<>();


    public RoutingPanel(DatabaseManagementService databaseManagementService) {
        this.databaseManagementService = databaseManagementService;

        routingListView.setPrefHeight(200);
        routingListView.setPrefWidth(255);
        routingListView.setCellFactory(new RouteCellFactory());
        routingListView.getSelectionModel().selectedItemProperty().addListener(this::routingChanged);
        routingListView.setPlaceholder(new Label("No routes in this dataset"));

        this.getChildren().add(routingListView);
    }

    private void routingChanged(Observable observable) {
    }

    public void setContext(DataSetDescriptor descriptor) {

        if (descriptor != null) {
            List<Route> routeList = descriptor.getRoutes();
            if (routeList.size() != 0) {
                routeList.forEach(route -> routingListView.getItems().add(route));
                log.info("adding routes");
            }

        } else {
            log.error("This dataset should be there but we couldn't find it: {}", datasetname);
        }

    }

}
