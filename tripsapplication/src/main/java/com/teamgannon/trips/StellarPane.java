package com.teamgannon.trips;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

public class StellarPane extends Pane {

    private  Label starName = new Label();

    public StellarPane() {
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setVgap(5);
        gridPane.setHgap(5);

        this.getChildren().add(gridPane);

        Label name = new Label("Name");

        gridPane.add(name, 0,0);
        gridPane.add(starName, 1,0);

    }

    public void setRecord(StarDisplayRecord starDisplayRecord) {
        starName.setText(starDisplayRecord.getStarName());
    }
}
