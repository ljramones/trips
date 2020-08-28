package com.teamgannon.trips.screenobjects;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import javafx.beans.Observable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;

public class ObjectViewPane extends Pane {

    private final ListView<StarDisplayRecord> stellarObjectsListView = new ListView<>();


    public ObjectViewPane() {
        stellarObjectsListView.setPrefHeight(200);
        stellarObjectsListView.setPrefWidth(255);
        stellarObjectsListView.setCellFactory(new StarDisplayRecordCellFactory());
        stellarObjectsListView.getSelectionModel().selectedItemProperty().addListener(this::listingChanged);
        stellarObjectsListView.setPlaceholder(new Label("No stars in view"));

        this.getChildren().add(stellarObjectsListView);
    }

    public void clear() {
        stellarObjectsListView.getItems().clear();
    }

    private void listingChanged(Observable observable) {

    }


    public void add(StarDisplayRecord starDisplayRecord) {
        stellarObjectsListView.getItems().add(starDisplayRecord);
    }
}
