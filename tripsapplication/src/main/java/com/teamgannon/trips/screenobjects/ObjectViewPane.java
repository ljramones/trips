package com.teamgannon.trips.screenobjects;

import com.teamgannon.trips.controller.MainPane;
import com.teamgannon.trips.events.ClearListEvent;
import com.teamgannon.trips.events.DisplayStarEvent;
import com.teamgannon.trips.events.UpdateSidePanelListEvent;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.service.StarService;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Comparator;

@Slf4j
@Component
public class ObjectViewPane extends VBox {

    private final ListView<StarDisplayRecord> stellarObjectsListView = new ListView<>();
    private final ApplicationEventPublisher eventPublisher;
    private final StarService starService;

    public ObjectViewPane(ApplicationEventPublisher eventPublisher, StarService starService) {
        this.eventPublisher = eventPublisher;
        this.starService = starService;

        setPrefWidth(MainPane.SIDE_PANEL_SIZE);
        setPrefHeight(600);
        stellarObjectsListView.setPrefWidth(MainPane.SIDE_PANEL_SIZE);
        stellarObjectsListView.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(stellarObjectsListView, Priority.ALWAYS);

        stellarObjectsListView.setCellFactory(
                new StarDisplayRecordCellFactory(
                        starService,
                        eventPublisher));

        stellarObjectsListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                StarObject starObject = starService.getStar(newValue.getRecordId());
                eventPublisher.publishEvent(new DisplayStarEvent(this, starObject));
            }
        });
        stellarObjectsListView.setPlaceholder(new Label("No stars in view"));

        getChildren().add(stellarObjectsListView);
    }

    public void clear() {
        stellarObjectsListView.getItems().clear();
    }


    public void add(StarDisplayRecord starDisplayRecord) {
        Platform.runLater(() -> {
//            log.debug("Add star display record: " + starDisplayRecord.getStarName());
            stellarObjectsListView.getItems().add(starDisplayRecord);
            stellarObjectsListView.getItems().sort(Comparator.comparing(StarDisplayRecord::getStarName));
        });
    }

    /**
     * An event listener method that listens for the occurrence of a ClearListEvent event.
     * Once the event is received, this method clears the list of items in the stellarObjectsListView.
     *
     * @param event the ClearListEvent to handle
     */
    @EventListener
    public void onClearListEvent(ClearListEvent event) {
        Platform.runLater(() -> {
            log.info("OBJECT VIEW PANE ::: Received clear list event");
            clear();
        });
    }

    @EventListener
    public void onUpdateSidePanelListEvent(UpdateSidePanelListEvent event) {
        Platform.runLater(() -> {
//            log.debug("OBJECT VIEW PANE ::: Received add to list event");
            add(event.getStarDisplayRecord());
        });
    }
}
