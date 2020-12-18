package com.teamgannon.trips.screenobjects;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import org.jetbrains.annotations.NotNull;

public class StellarPane extends Pane {

    private  final Label starName = new Label();
    private  final Label color = new Label();
    private  final Label radius = new Label();
    private  final Label x = new Label();
    private  final Label y = new Label();
    private  final Label z = new Label();

    public StellarPane() {
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setVgap(5);
        gridPane.setHgap(5);

        this.getChildren().add(gridPane);

        gridPane.add(new Label("Name"), 0,0);
        gridPane.add(starName, 1,0);

        gridPane.add(new Label("Color"), 0,1);
        gridPane.add(color, 1,1);

        gridPane.add(new Label("Radius"), 0,2);
        gridPane.add(radius, 1,2);

        gridPane.add(new Label("X"), 0,3);
        gridPane.add(x, 1,3);

        gridPane.add(new Label("Y"), 0,4);
        gridPane.add(y, 1,4);

        gridPane.add(new Label("Z"), 0,5);
        gridPane.add(z, 1,5);

    }

    public void setRecord(@NotNull StarDisplayRecord starDisplayRecord) {
        starName.setText(starDisplayRecord.getStarName());
        color.setText(starDisplayRecord.getStarColor().toString());
        radius.setText(String.format("%.2f", starDisplayRecord.getRadius()));

        double[] actualCoordinates = starDisplayRecord.getActualCoordinates();
        x.setText(String.format("%.2f", actualCoordinates[0]));
        y.setText(String.format("%.2f", actualCoordinates[1]));
        z.setText(String.format("%.2f", actualCoordinates[2]));
    }
}
