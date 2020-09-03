package com.teamgannon.trips.screenobjects;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.AstrographicObject;
import com.teamgannon.trips.listener.DatabaseListener;
import com.teamgannon.trips.listener.ListSelectorActionsListener;
import com.teamgannon.trips.listener.StellarPropertiesDisplayer;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ObjectViewPane extends Pane {

    private final ListView<StarDisplayRecord> stellarObjectsListView = new ListView<>();

    public ObjectViewPane(StellarPropertiesDisplayer propertiesDisplayer,
                          DatabaseListener databaseListener,
                          ListSelectorActionsListener listSelectorActionsListener) {

        stellarObjectsListView.setPrefHeight(600);
        stellarObjectsListView.setPrefWidth(255);
        stellarObjectsListView.setCellFactory(new StarDisplayRecordCellFactory(databaseListener, listSelectorActionsListener));
        stellarObjectsListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            AstrographicObject astrographicObject = databaseListener.getStar(newValue.getRecordId());
            propertiesDisplayer.displayStellarProperties(astrographicObject);
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
