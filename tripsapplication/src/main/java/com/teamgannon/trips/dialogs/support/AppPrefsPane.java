package com.teamgannon.trips.dialogs.support;


import com.teamgannon.trips.config.application.ApplicationPreferences;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

@Slf4j
public class AppPrefsPane extends Pane {

    private final static String APP_PANE_TITLE = "Change View Preferences";
    private final static String APP_PANE_TITLE_MODIFIED = "Change View Preferences - *modified*";

    private final ApplicationPreferences applicationPreferences;

    private boolean appChangeDetected = false;

    private final TitledPane appPreferencesPane = new TitledPane();

    // app preferences
    private final TextField routeSegmentLengthTextField = new TextField();
    private final TextField routeColorTextField = new TextField();
    private final TextField gridSizeTextField = new TextField();

    private final ColorPicker routeColorPicker = new ColorPicker();


    public AppPrefsPane(ApplicationPreferences applicationPreferences) {
        this.applicationPreferences = applicationPreferences;

        GridPane gridPane = new GridPane();

        Label centerCoordLabel = new Label("Center Coordinates: ");
        Label centerCoordValueLabel = new Label(applicationPreferences.currentCenterToString());
        gridPane.addRow(0, centerCoordLabel, centerCoordValueLabel);

        Label centerStarNameLabel = new Label("Center Star: ");
        Label centerStarNameValueLabel = new Label(applicationPreferences.getCenterStarName());
        gridPane.addRow(1, centerStarNameLabel, centerStarNameValueLabel);

        Label centerStarIDLabel = new Label("Star Id: ");
        Label centerStarIDValueLabel = new Label(applicationPreferences.centerStarIdAsString());
        gridPane.addRow(2, centerStarIDLabel, centerStarIDValueLabel);

        Label routeLengthLabel = new Label("Route Segment Length: ");
        gridPane.addRow(3, routeLengthLabel, routeSegmentLengthTextField);
        Label routeColorLabel = new Label("Route Color: ");

        routeColorPicker.setOnAction(e -> {
            // color
            Color c = routeColorPicker.getValue();

            // set text of the label to RGB value of color
            routeColorTextField.setText(c.toString());
        });
        gridPane.addRow(4, routeColorLabel, routeColorTextField, routeColorPicker);
        Label gridSizeLabel = new Label("Grid Size (ly): ");
        gridPane.addRow(5, gridSizeLabel, gridSizeTextField);

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        Button resetBtn = new Button("Reset");
        resetBtn.setOnAction(this::resetClicked);
        hBox.getChildren().add(resetBtn);
        gridPane.add(hBox, 0, 6, 3, 1);

        routeSegmentLengthTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                routeSegmentChanged();
            }
        });

        routeColorTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                routeColorChanged();
            }
        });

        gridSizeTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                gridSizeChanged();
            }
        });

        routeSegmentLengthTextField.setText(Integer.toString(applicationPreferences.getRouteLength()));
        routeColorTextField.setText(applicationPreferences.getRouteColor().toString());
        routeColorPicker.setValue(applicationPreferences.getRouteColor());
        gridSizeTextField.setText(Integer.toString(applicationPreferences.getGridsize()));

        appPreferencesPane.setText(APP_PANE_TITLE);
        appPreferencesPane.setContent(gridPane);
        appPreferencesPane.setCollapsible(false);

        this.getChildren().add(appPreferencesPane);

    }

    private void resetClicked(ActionEvent actionEvent) {
        routeSegmentLengthTextField.setText(Integer.toString(applicationPreferences.getRouteLength()));
        routeColorTextField.setText(applicationPreferences.getRouteColor().toString());
        routeColorPicker.setValue(applicationPreferences.getRouteColor());
        gridSizeTextField.setText(Integer.toString(applicationPreferences.getGridsize()));

        appPreferencesPane.setText(APP_PANE_TITLE);
    }

    private void gridSizeChanged() {
        try {
            Integer.parseInt(gridSizeTextField.getText());
            appChangeDetected = true;
        } catch (Exception e) {
            log.error("{} is an invalid number", gridSizeTextField.getText());
            showErrorAlert("Change Grid Size", gridSizeTextField.getText() + " is an invalid number");
        }
    }

    private void routeColorChanged() {
        try {
            Color color = Color.valueOf(routeColorTextField.getText());
            routeColorPicker.setValue(color);
            appPreferencesPane.setText(APP_PANE_TITLE_MODIFIED);
            appChangeDetected = true;
        } catch (Exception e) {
            log.error("{} is an invalid color", routeColorTextField.getText());
            showErrorAlert("Change Route Color", routeColorTextField.getText() + " is an invalid color");
        }
    }

    private void routeSegmentChanged() {
        try {
            Integer.parseInt(routeSegmentLengthTextField.getText());
            appChangeDetected = true;
        } catch (Exception e) {
            log.error("{} is an invalid number", routeSegmentLengthTextField.getText());
            showErrorAlert("Change Route Segment Size", routeSegmentLengthTextField.getText() + " is an invalid number");
        }
    }

    public boolean isChanged() {
        return appChangeDetected;
    }

    public ApplicationPreferences getAppPrefs() {
        ApplicationPreferences appPreferences = SerializationUtils.clone(applicationPreferences);

        appPreferences.setRouteLength(Integer.parseInt(routeSegmentLengthTextField.getText()));
        appPreferences.setGridsize(Integer.parseInt(gridSizeTextField.getText()));
        appPreferences.setDistanceFromCenter(applicationPreferences.getDistanceFromCenter());
        appPreferences.setRouteColor(routeColorPicker.getValue());

        return appPreferences;
    }
}
