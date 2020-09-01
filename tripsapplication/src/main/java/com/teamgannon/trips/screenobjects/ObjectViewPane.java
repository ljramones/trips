package com.teamgannon.trips.screenobjects;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.graphics.operators.StellarPropertiesDisplayer;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ObjectViewPane extends Pane {

    private final ListView<StarDisplayRecord> stellarObjectsListView = new ListView<>();

    private final StellarPropertiesDisplayer updater;
    private ListSelecterActionsListener listSelecterActionsListener;


    public ObjectViewPane(StellarPropertiesDisplayer updater, ListSelecterActionsListener listSelecterActionsListener) {
        this.updater = updater;
        this.listSelecterActionsListener = listSelecterActionsListener;

        stellarObjectsListView.setPrefHeight(200);
        stellarObjectsListView.setPrefWidth(255);
        stellarObjectsListView.setCellFactory(new StarDisplayRecordCellFactory(listSelecterActionsListener));
        stellarObjectsListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            updater.displayStellarProperties(newValue);
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
