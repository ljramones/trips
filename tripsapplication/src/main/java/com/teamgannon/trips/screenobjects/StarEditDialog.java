package com.teamgannon.trips.screenobjects;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class StarEditDialog extends Dialog<StarDisplayRecord> {


    private final StarDisplayRecord record;

    TabPane tabPane;

    public StarEditDialog(StarDisplayRecord record) {
        this.record = record;

        VBox vBox = new VBox();

        tabPane = new TabPane();

        Tab overviewTab = new Tab("Overview");
        overviewTab.setContent(createOverviewTab());
        tabPane.getTabs().add(overviewTab);

        Tab secondaryTab = new Tab("Secondary");
        secondaryTab.setContent(createSecondaryTab());
        tabPane.getTabs().add(secondaryTab);

        Tab fictionalTab = new Tab("Fictional");
        fictionalTab.setContent(createFictionalTab());
        tabPane.getTabs().add(fictionalTab);

        vBox.getChildren().add(tabPane);

        // setup button boxes
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        Button resetBtn = new Button("Dismiss");
        resetBtn.setOnAction(this::dismissClicked);
        hBox.getChildren().add(resetBtn);
        Button addBtn = new Button("Change");
        addBtn.setOnAction(this::changeClicked);
        hBox.getChildren().add(addBtn);
        vBox.getChildren().add(hBox);

        this.setTitle("Change attributes for " + record.getStarName());
        this.getDialogPane().setContent(vBox);
    }

    private Pane createFictionalTab() {
        // setup grid structure
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setVgap(5);
        gridPane.setHgap(5);

        gridPane.add(new Label("polity"), 0, 1);
        TextField polityTextField = new TextField();
        polityTextField.setText(record.getPolity());
        gridPane.add(polityTextField, 1, 1);

        gridPane.add(new Label("world type"), 0, 2);
        TextField worldTypeTextField = new TextField();
        worldTypeTextField.setText(record.getWorldType());
        gridPane.add(worldTypeTextField, 1, 2);

        gridPane.add(new Label("fuel type"), 0, 3);
        TextField fuelTypeTextField = new TextField();
        fuelTypeTextField.setText(record.getFuelType());
        gridPane.add(fuelTypeTextField, 1, 3);

        gridPane.add(new Label("tech type"), 0, 4);
        TextField techTypeTextField = new TextField();
        techTypeTextField.setText(record.getTechType());
        gridPane.add(techTypeTextField, 1, 4);

        gridPane.add(new Label("port type"), 0, 5);
        TextField portTypeTextField = new TextField();
        polityTextField.setText(record.getPortType());
        gridPane.add(portTypeTextField, 1, 5);

        gridPane.add(new Label("population type"), 0, 6);
        TextField popTypeTextField = new TextField();
        popTypeTextField.setText(record.getPopulationType());
        gridPane.add(popTypeTextField, 1, 6);

        gridPane.add(new Label("product type"), 0, 7);
        TextField prodField = new TextField();
        prodField.setText(record.getProductType());
        gridPane.add(prodField, 1, 7);

        gridPane.add(new Label("milspace type"), 0, 8);
        TextField milspaceTextField = new TextField();
        milspaceTextField.setText(record.getMilSpaceType());
        gridPane.add(milspaceTextField, 1, 8);

        gridPane.add(new Label("milplan type"), 0, 9);
        TextField milplanTextField = new TextField();
        milplanTextField.setText(record.getMilPlanType());
        gridPane.add(milplanTextField, 1, 9);

        gridPane.add(new Label("anomaly"), 0, 10);
        CheckBox anomalyTextField = new CheckBox();
        anomalyTextField.setSelected(record.isAnomaly());
        gridPane.add(anomalyTextField, 1, 10);

        gridPane.add(new Label("other"), 0, 11);
        CheckBox otherTextField = new CheckBox();
        otherTextField.setSelected(record.isOther());
        gridPane.add(otherTextField, 1, 11);

        return gridPane;
    }

    private Pane createSecondaryTab() {

        // setup grid structure
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setVgap(5);
        gridPane.setHgap(5);

        // items for right grid
        gridPane.add(new Label("ra"), 0, 1);
        TextField raLabel = new TextField();
        raLabel.setText(Double.toString(record.getRa()));
        raLabel.setPromptText("right ascension");
        gridPane.add(raLabel, 1, 1);

        gridPane.add(new Label("pmra"), 0, 2);
        TextField pmraLabel = new TextField();
        pmraLabel.setText(Double.toString(record.getPmra()));
        gridPane.add(pmraLabel, 1, 2);

        gridPane.add(new Label("declination"), 0, 3);
        TextField decLabel = new TextField();
        decLabel.setText(Double.toString(record.getDeclination()));
        decLabel.setPromptText("declination");
        gridPane.add(decLabel, 1, 3);

        gridPane.add(new Label("pmdec"), 0, 4);
        TextField pmdecLabel = new TextField();
        pmdecLabel.setText(Double.toString(record.getPmdec()));
        gridPane.add(pmdecLabel, 1, 4);

        gridPane.add(new Label("dec_deg"), 0, 5);
        TextField decdegLabel = new TextField();
        decdegLabel.setText(Double.toString(record.getDec_deg()));
        gridPane.add(decdegLabel, 1, 5);

        gridPane.add(new Label("rs_cdeg"), 0, 6);
        TextField rsLabel = new TextField();
        rsLabel.setText(Double.toString(record.getRs_cdeg()));
        gridPane.add(rsLabel, 1, 6);

        gridPane.add(new Label("Parallax"), 0, 7);
        TextField parallaxLabel = new TextField();
        parallaxLabel.setText(Double.toString(record.getParallax()));
        gridPane.add(parallaxLabel, 1, 7);

        gridPane.add(new Label("Radial velocity"), 0, 8);
        TextField radialVelocityLabel = new TextField();
        radialVelocityLabel.setText(Double.toString(record.getRadialVelocity()));
        gridPane.add(radialVelocityLabel, 1, 8);

        gridPane.add(new Label("bprp"), 0, 9);
        TextField bprpLabel = new TextField();
        bprpLabel.setText(Double.toString(record.getBprp()));
        gridPane.add(bprpLabel, 1, 9);

        gridPane.add(new Label("bpg"), 0, 10);
        TextField bpgLabel = new TextField();
        bpgLabel.setText(Double.toString(record.getBpg()));
        gridPane.add(bpgLabel, 1, 10);

        gridPane.add(new Label("grp"), 0, 11);
        TextField grpLabel = new TextField();
        grpLabel.setText(Double.toString(record.getGrp()));
        gridPane.add(grpLabel, 1, 11);

        return gridPane;

    }

    private Pane createOverviewTab() {

        // setup grid structure
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setVgap(5);
        gridPane.setHgap(5);

        gridPane.add(new Label("Record id"), 0, 1);
        Label recordIdLabel = new Label();
        recordIdLabel.setText(record.getRecordId().toString());
        gridPane.add(recordIdLabel, 1, 1);

        gridPane.add(new Label("Data set name"), 0, 2);
        Label dataSetLabel = new Label();
        dataSetLabel.setText(record.getDataSetName());
        gridPane.add(dataSetLabel, 1, 2);

        gridPane.add(new Label("Star name"), 0, 3);
        TextField starNameTextField = new TextField();
        starNameTextField.setText(record.getStarName());
        gridPane.add(starNameTextField, 1, 3);

        gridPane.add(new Label("Color"), 0, 4);
        ColorPicker starColorPicker = new ColorPicker();
        starColorPicker.setValue(record.getStarColor());
        gridPane.add(starColorPicker, 1, 4);

        gridPane.add(new Label("Radius"), 0, 5);
        TextField radiusTextField = new TextField();
        radiusTextField.setText(Double.toString(record.getRadius()));
        radiusTextField.setPromptText("the radius in Sol units");
        gridPane.add(radiusTextField, 1, 5);

        gridPane.add(new Label("Distance"), 0, 6);
        TextField distanceNameTextField = new TextField();
        distanceNameTextField.setText(Double.toString(record.getDistance()));
        distanceNameTextField.setPromptText("the distance from Sol in ly");
        gridPane.add(distanceNameTextField, 1, 6);

        gridPane.add(new Label("Spectral class"), 0, 7);
        TextField spectralClassTextField = new TextField();
        spectralClassTextField.setText(record.getSpectralClass());
        spectralClassTextField.setText(" the spectral class as in O, A, etc.");
        gridPane.add(spectralClassTextField, 1, 7);

        gridPane.add(new Label("Temperature"), 0, 8);
        TextField tempTextField = new TextField();
        tempTextField.setText(Double.toString(record.getTemperature()));
        tempTextField.setPromptText("the surface temperature of the star");
        gridPane.add(tempTextField, 1, 8);

        gridPane.add(new Label("Coordinates"), 0, 9);
        GridPane coordGrid = new GridPane();
        double[] coords = record.getActualCoordinates();
        gridPane.add(coordGrid, 1, 9);
        TextField xTextField = new TextField();
        xTextField.setText(Double.toString(coords[0]));
        xTextField.setPromptText("X coordinate");
        coordGrid.add(xTextField, 0, 1);
        TextField yTextField = new TextField();
        yTextField.setText(Double.toString(coords[1]));
        yTextField.setPromptText("Y coordinate");
        coordGrid.add(yTextField, 1, 1);
        TextField zTextField = new TextField();
        zTextField.setText(Double.toString(coords[2]));
        zTextField.setPromptText("Z coordinate");
        coordGrid.add(zTextField, 2, 1);

        gridPane.add(new Label("Notes"), 0, 10);
        TextArea notesArea = new TextArea();
        notesArea.setText(record.getNotes());
        notesArea.setPromptText("Enter a description or general notes on this star");
        gridPane.add(notesArea, 1, 10, 1, 3);

        return gridPane;
    }

    private void changeClicked(ActionEvent actionEvent) {
        StarDisplayRecord starDisplayRecord = getData();
        setResult(starDisplayRecord);
    }

    private StarDisplayRecord getData() {
        StarDisplayRecord record = new StarDisplayRecord();


        return record;
    }

    private void dismissClicked(ActionEvent actionEvent) {
        setResult(null);
    }

}
