package com.teamgannon.trips.screenobjects;

import com.teamgannon.trips.controller.MainPane;
import com.teamgannon.trips.events.DisplayStarEvent;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.listener.DatabaseListener;
import com.teamgannon.trips.listener.ListSelectorActionsListener;
import com.teamgannon.trips.listener.RedrawListener;
import com.teamgannon.trips.listener.ReportGenerator;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Comparator;

@Slf4j
public class ObjectViewPane extends Pane {

    private final ListView<StarDisplayRecord> stellarObjectsListView = new ListView<>();
    private final ApplicationEventPublisher eventPublisher;
    private DatabaseListener databaseListener;
    private ListSelectorActionsListener listSelectorActionsListener;
    private ReportGenerator reportGenerator;
    private RedrawListener redrawListener;

    public ObjectViewPane(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;

        stellarObjectsListView.setPrefHeight(600);
        stellarObjectsListView.setPrefWidth(MainPane.SIDE_PANEL_SIZE);
        stellarObjectsListView.setMaxHeight(800);

    }

    public void setListeners(DatabaseListener databaseListener,
                             ListSelectorActionsListener listSelectorActionsListener,
                             ReportGenerator reportGenerator,
                             RedrawListener redrawListener) {

        this.databaseListener = databaseListener;
        this.listSelectorActionsListener = listSelectorActionsListener;
        this.reportGenerator = reportGenerator;
        this.redrawListener = redrawListener;

        stellarObjectsListView.setCellFactory(
                new StarDisplayRecordCellFactory(
                        databaseListener,
                        listSelectorActionsListener,
                        reportGenerator,
                        redrawListener,
                        eventPublisher));

        stellarObjectsListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                StarObject starObject = databaseListener.getStar(newValue.getRecordId());
                eventPublisher.publishEvent(new DisplayStarEvent(this, starObject));
            }
        });
        stellarObjectsListView.setPlaceholder(new Label("No stars in view"));

        this.getChildren().add(stellarObjectsListView);
    }

    public void clear() {
        stellarObjectsListView.getItems().clear();
    }


    public void add(StarDisplayRecord starDisplayRecord) {
        stellarObjectsListView.getItems().add(starDisplayRecord);
        stellarObjectsListView.getItems().sort(Comparator.comparing(StarDisplayRecord::getStarName));
    }
}
