package com.teamgannon.trips.dialogs;

import com.teamgannon.trips.config.application.ApplicationPreferences;
import com.teamgannon.trips.config.application.ColorPalette;
import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.dialogs.support.ChangeTypeEnum;
import com.teamgannon.trips.dialogs.support.ColorChangeResult;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

@Slf4j
public class ApplicationPreferencesDialog extends Dialog<ColorChangeResult> {

    private final ColorPalette colorPalette;
    private final TripsContext tripsContext;
    private final ApplicationPreferences preferences;

    public Button changeColorsButton = new Button("Change colors");

    public Button resetColorsButton = new Button("Reset to defaults");

    private final TextField labelColorTextField = new TextField();
    private final TextField gridColorTextField = new TextField();
    private final TextField extensionColorTextField = new TextField();
    private final TextField legendColorTextField = new TextField();

    public ApplicationPreferencesDialog(TripsContext tripsContext) {
        this.tripsContext = tripsContext;
        this.colorPalette = tripsContext.getColorPallete();
        this.preferences = tripsContext.getAppPreferences();

        this.setTitle("Change Graph Colors Dialog");
        this.setHeight(300);
        this.setWidth(400);

        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(10.0);
        this.getDialogPane().setContent(vBox);

        HBox prefsBox = new HBox();

        TitledPane colorPane = createColorPane();
        colorPane.setCollapsible(false);
        prefsBox.getChildren().add(colorPane);

        TitledPane preferencesPane = createViewPreferences();
        preferencesPane.setCollapsible(false);
        prefsBox.getChildren().add(preferencesPane);

        vBox.getChildren().add(prefsBox);

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        vBox.getChildren().add(hBox);

        changeColorsButton.setOnAction(this::changeColorClicked);
        hBox.getChildren().add(changeColorsButton);

        resetColorsButton.setOnAction(this::resetColorsClicked);
        hBox.getChildren().add(resetColorsButton);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setOnAction(this::cancel);
        hBox.getChildren().add(cancelBtn);

        // set the dialog as a utility
        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        stage.setOnCloseRequest(this::close);

        setData();
    }

    private TitledPane createColorPane() {
        GridPane gridPane = new GridPane();
        gridPane.setVgap(5);
        gridPane.setHgap(5);

        Label starNameLabel = new Label("Label color:");
        gridPane.add(starNameLabel, 0, 0);
        gridPane.add(labelColorTextField, 1, 0);

        Label distanceToEarthLabel = new Label("Grid color:");
        gridPane.add(distanceToEarthLabel, 0, 1);
        gridPane.add(gridColorTextField, 1, 1);

        Label spectraLabel = new Label("Extension color:");
        gridPane.add(spectraLabel, 0, 2);
        gridPane.add(extensionColorTextField, 1, 2);

        Label radiusLabel = new Label("Legend color");
        gridPane.add(radiusLabel, 0, 3);
        gridPane.add(legendColorTextField, 1, 3);
        return new TitledPane("Change Graph Colors ", gridPane);
    }

    private TitledPane createViewPreferences() {
        VBox mainPane = new VBox();
        VBox gridBox = new VBox();
        GridPane gridPane = new GridPane();

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
        return new TitledPane("Change View Preferences", mainPane);
    }


    private void resetColorsClicked(ActionEvent actionEvent) {
        setResult(new ColorChangeResult(ChangeTypeEnum.RESET, null));
    }

    private void close(WindowEvent windowEvent) {
        setResult(new ColorChangeResult(ChangeTypeEnum.CANCEL, null));
    }

    private void cancel(ActionEvent actionEvent) {
        setResult(new ColorChangeResult(ChangeTypeEnum.CANCEL, null));
    }

    private void changeColorClicked(ActionEvent actionEvent) {
        // pull the data from the controls
        getData();

        setResult(new ColorChangeResult(ChangeTypeEnum.CHANGE, colorPalette));
    }

    private void getData() {
        String labelColor = labelColorTextField.getText();
        if (labelColor.isEmpty()) {
            try {
                showErrorAlert("Change color", "Label color cannot be left blank");
            } catch (IllegalArgumentException ie) {
                showErrorAlert("Change color", "Label color:<" + labelColor + "> is not a valid color");
            }
        }
        colorPalette.setLabelColor(labelColor);

        String gridColor = gridColorTextField.getText();
        if (gridColor.isEmpty()) {
            try {
                showErrorAlert("Change color", "Grid color cannot be left blank");
            } catch (IllegalArgumentException ie) {
                showErrorAlert("Change color", "Grid color:<" + gridColor + "> is not a valid color");
            }
        }
        colorPalette.setGridColor(gridColor);

        String extensionColor = extensionColorTextField.getText();
        if (gridColor.isEmpty()) {
            try {
                showErrorAlert("Change color", "Extension color cannot be left blank");
            } catch (IllegalArgumentException ie) {
                showErrorAlert("Change color", "Extension color:<" + extensionColor + "> is not a valid color");
            }
        }
        colorPalette.setExtensionColor(extensionColor);

        String legendColor = legendColorTextField.getText();
        if (gridColor.isEmpty()) {
            try {
                showErrorAlert("Change color", "Legend color cannot be left blank");
            } catch (IllegalArgumentException ie) {
                showErrorAlert("Change color", "Legend color:<" + legendColor + "> is not a valid color");
            }
        }
        colorPalette.setLegendColor(legendColor);

    }

    private void setData() {
        labelColorTextField.setText(colorPalette.getLabelColor().toString());
        gridColorTextField.setText(colorPalette.getGridColor().toString());
        extensionColorTextField.setText(colorPalette.getExtensionColor().toString());
        legendColorTextField.setText(colorPalette.getLegendColor().toString());
    }

}
