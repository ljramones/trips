package com.teamgannon.trips.controls;

import com.teamgannon.trips.config.application.ApplicationPreferences;
import com.teamgannon.trips.config.application.TripsContext;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Component;

@Component
public class ApplicationPreferencesPane extends Pane {

    private final VBox mainPane = new VBox();
    private final GridPane gridPane = new GridPane();
    private final TripsContext tripsContext;
    ApplicationPreferences preferences;


    public ApplicationPreferencesPane(TripsContext tripsContext) {
        this.tripsContext = tripsContext;
        this.preferences = tripsContext.getAppPreferences();

        VBox gridBox = new VBox();

        Label centerCoordLabel = new Label("Center Coordinates: ");
        Label centerCoordValueLabel = new Label(preferences.currentCenterToString());
        gridPane.addRow(0, centerCoordLabel, centerCoordValueLabel);


        Label centerStarNameLabel = new Label("Center Star: ");
        Label centerStarNameValueLabel = new Label(preferences.getCenterStarName());
        gridPane.addRow(1, centerStarNameLabel, centerStarNameValueLabel);

        Label centerStarIDLabel = new Label("Star Id: ");
        Label centerStarIDValueLabel = new Label(preferences.centerStarIdAsString());
        gridPane.addRow(2, centerStarIDLabel, centerStarIDValueLabel);

        Label distanceCenterLabel = new Label("Distance Limit: ");
        Label distanceCenterValueLabel = new Label(Integer.toString(preferences.getDistanceFromCenter()));
        gridPane.addRow(3, distanceCenterLabel, distanceCenterValueLabel);

        Label routeLengthLabel = new Label("Route Segment Length: ");
        Label routeLengthValueLabel = new Label(Integer.toString(preferences.getRouteLength()));
        gridPane.addRow(4, routeLengthLabel, routeLengthValueLabel);

        Label routeColorLabel = new Label("Route Color: ");
        Label routeColorValueLabel = preferences.getRouteColorAsLabel();
        gridPane.addRow(5, routeColorLabel, routeColorValueLabel);

        Label gridSizeLabel = new Label("Grid Size (ly): ");
        Label gridSizeValueLabel = new Label(Integer.toString(preferences.getGridsize()));
        gridPane.addRow(6, gridSizeLabel, gridSizeValueLabel);
        gridBox.getChildren().add(gridPane);

        gridBox.getChildren().add(new Separator());

        mainPane.getChildren().add(gridBox);

        VBox buttonBox = new VBox();

        Button setButton = new Button("Change");
        setButton.setOnAction(this::changePreferences);
        buttonBox.getChildren().add(setButton);
        buttonBox.setAlignment(Pos.CENTER);

        mainPane.getChildren().add(buttonBox);

        this.getChildren().add(mainPane);

    }

    public void changePreferences(ActionEvent event) {

    }

}
