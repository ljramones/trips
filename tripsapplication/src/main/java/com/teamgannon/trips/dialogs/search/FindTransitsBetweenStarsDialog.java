package com.teamgannon.trips.dialogs.search;

import com.teamgannon.trips.dialogs.search.model.DistanceRoutes;
import com.teamgannon.trips.jpa.model.TransitSettings;
import com.teamgannon.trips.service.DatabaseManagementService;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

public class FindTransitsBetweenStarsDialog extends Dialog<DistanceRoutes> {

    private final TextField upperDistanceMeasure = new TextField();
    private final TextField lowerDistanceMeasure = new TextField();

    private final ColorPicker colorPicker = new ColorPicker();

    private final TextField lineWidth = new TextField();
    private final DatabaseManagementService databaseManagementService;

    private final TransitSettings transitSettings;


    public FindTransitsBetweenStarsDialog(DatabaseManagementService databaseManagementService, TransitSettings transitSettings) {

        this.databaseManagementService = databaseManagementService;
        this.transitSettings = transitSettings;

        this.setTitle("Select a Range to Find Transits");
        this.setHeight(300);
        this.setWidth(500);

        VBox vBox = new VBox();
        Font font = Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 13);
        vBox.getChildren().add(new Separator());

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setVgap(5);
        gridPane.setHgap(5);

        Label upperDistanceLabel = new Label("Select Upper Distance Between Stars: ");
        upperDistanceLabel.setFont(font);


        Label lowerDistanceLabel = new Label("Select Lower Distance Between Stars: ");
        lowerDistanceLabel.setFont(font);

        Label colorLabel = new Label("Select color for transit: ");
        colorLabel.setFont(font);

        Label lineWidthLabel = new Label("Select transit line width: ");
        lineWidthLabel.setFont(font);

        gridPane.add(upperDistanceLabel, 0, 1);
        gridPane.add(upperDistanceMeasure, 1, 1);
        upperDistanceMeasure.setText(Double.toString(transitSettings.getUpperDistance()));

        gridPane.add(lowerDistanceLabel, 0, 2);
        gridPane.add(lowerDistanceMeasure, 1, 2);
        lowerDistanceMeasure.setText(Double.toString(transitSettings.getLowerDistance()));

        gridPane.add(colorLabel, 0, 3);
        gridPane.add(colorPicker, 1, 3);
        Color transitColor = Color.valueOf(transitSettings.getLineColor());
        colorPicker.setValue(transitColor);

        gridPane.add(lineWidthLabel, 0, 4);
        gridPane.add(lineWidth, 1, 4);
        lineWidth.setText(Double.toString(transitSettings.getLineWidth()));

        vBox.getChildren().add(gridPane);

        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(10.0);

        HBox hBox1 = new HBox();
        hBox1.setAlignment(Pos.CENTER);
        vBox.getChildren().add(hBox1);

        Button gotToStarButton = new Button("Generate Transits");
        gotToStarButton.setOnAction(this::goToStarClicked);
        hBox1.getChildren().add(gotToStarButton);

        Button cancelDataSetButton = new Button("Cancel");
        cancelDataSetButton.setOnAction(this::close);
        hBox1.getChildren().add(cancelDataSetButton);

        this.getDialogPane().setContent(vBox);

        // set the dialog as a utility
        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        stage.setOnCloseRequest(this::close);
    }

    private void close(WindowEvent windowEvent) {
        DistanceRoutes distanceRoutes = DistanceRoutes.builder().selected(false).build();
        setResult(distanceRoutes);
    }

    private void close(ActionEvent actionEvent) {
        DistanceRoutes distanceRoutes = DistanceRoutes.builder().selected(false).build();
        setResult(distanceRoutes);
    }

    private void goToStarClicked(ActionEvent actionEvent) {
        try {
            double upperDistance = Double.parseDouble(upperDistanceMeasure.getText());
            double lowerDistance = Double.parseDouble(lowerDistanceMeasure.getText());
            Color transitColor = colorPicker.getValue();
            double transitLineWidth = Double.parseDouble(lineWidth.getText());
            DistanceRoutes distanceRoutes = DistanceRoutes
                    .builder()
                    .upperDistance(upperDistance)
                    .lowerDistance(lowerDistance)
                    .color(transitColor)
                    .lineWidth(transitLineWidth)
                    .selected(true)
                    .build();
            TransitSettings transitSettings = gatherValues();
            databaseManagementService.setTransitSettings(transitSettings);
            setResult(distanceRoutes);
        } catch (NumberFormatException nfe) {
            showErrorAlert("Find Distance Between Stars",
                    "Not a valid floating point:" + upperDistanceMeasure.getText());
        }
    }

    private TransitSettings gatherValues() {
        try {
            transitSettings.setUpperDistance(Double.parseDouble(upperDistanceMeasure.getText()));
            transitSettings.setLowerDistance(Double.parseDouble(lowerDistanceMeasure.getText()));
            transitSettings.setLineWidth(Double.parseDouble(lineWidth.getText()));
            transitSettings.setLineColor(colorPicker.getValue().toString());
        } catch (Exception e) {
            showErrorAlert("Transit settings", "Must be a double value");
        }
        return transitSettings;
    }

}
