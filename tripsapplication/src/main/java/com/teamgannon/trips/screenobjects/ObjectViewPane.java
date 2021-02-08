package com.teamgannon.trips.screenobjects;

import com.teamgannon.trips.controller.MainPane;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.listener.*;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class ObjectViewPane extends Pane {

    private final ListView<StarDisplayRecord> stellarObjectsListView = new ListView<>();

    public ObjectViewPane(@NotNull StellarPropertiesDisplayerListener propertiesDisplayer,
                          @NotNull DatabaseListener databaseListener,
                          ListSelectorActionsListener listSelectorActionsListener,
                          ReportGenerator reportGenerator,
                          RedrawListener redrawListener) {

        stellarObjectsListView.setPrefHeight(600);
        stellarObjectsListView.setPrefWidth(MainPane.SIDE_PANEL_SIZE);
        stellarObjectsListView.setMaxHeight(800);
        stellarObjectsListView.setCellFactory(
                new StarDisplayRecordCellFactory(
                        databaseListener,
                        listSelectorActionsListener,
                        reportGenerator,
                        redrawListener));

        stellarObjectsListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                StarObject starObject = databaseListener.getStar(newValue.getRecordId());
                propertiesDisplayer.displayStellarProperties(starObject);
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
    }
}
