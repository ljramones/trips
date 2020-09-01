package com.teamgannon.trips.screenobjects;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
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
        gridPane.add(polityTextField, 1, 1);

        gridPane.add(new Label("world type"), 0, 2);
        TextField worldTypeTextField = new TextField();
        gridPane.add(worldTypeTextField, 1, 2);

        gridPane.add(new Label("fuel type"), 0, 3);
        TextField fuelTypeTextField = new TextField();
        gridPane.add(fuelTypeTextField, 1, 3);

        gridPane.add(new Label("tech type"), 0, 4);
        TextField techTypeTextField = new TextField();
        gridPane.add(techTypeTextField, 1, 4);

        gridPane.add(new Label("port type"), 0, 5);
        TextField portTypeTextField = new TextField();
        gridPane.add(portTypeTextField, 1, 5);

        gridPane.add(new Label("population type"), 0, 6);
        TextField popTypeTextField = new TextField();
        gridPane.add(popTypeTextField, 1, 6);

        gridPane.add(new Label("product type"), 0, 7);
        TextField prodField = new TextField();
        gridPane.add(prodField, 1, 7);

        gridPane.add(new Label("milspace type"), 0, 8);
        TextField milspaceTextField = new TextField();
        gridPane.add(milspaceTextField, 1, 8);

        gridPane.add(new Label("milplan type"), 0, 9);
        TextField milplanTextField = new TextField();
        gridPane.add(milplanTextField, 1, 9);

        gridPane.add(new Label("anomaly"), 0, 10);
        CheckBox anomalyTextField = new CheckBox();
        gridPane.add(anomalyTextField, 1, 10);

        gridPane.add(new Label("other"), 0, 11);
        CheckBox otherTextField = new CheckBox();
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
        raLabel.setPromptText("right ascension");
        gridPane.add(raLabel, 1, 1);

        gridPane.add(new Label("pmra"), 0, 2);
        TextField pmraLabel = new TextField();
        gridPane.add(pmraLabel, 1, 2);

        gridPane.add(new Label("dec"), 0, 3);
        TextField decLabel = new TextField();
        decLabel.setPromptText("declination");
        gridPane.add(decLabel, 1, 3);

        gridPane.add(new Label("pmdec"), 0, 4);
        TextField pmdecLabel = new TextField();
        gridPane.add(pmdecLabel, 1, 4);

        gridPane.add(new Label("dec_deg"), 0, 5);
        TextField decdegLabel = new TextField();
        gridPane.add(decdegLabel, 1, 5);

        gridPane.add(new Label("rs-cdeg"), 0, 6);
        TextField rsLabel = new TextField();
        gridPane.add(rsLabel, 1, 6);

        gridPane.add(new Label("Parallax"), 0, 7);
        TextField parallaxLabel = new TextField();
        gridPane.add(parallaxLabel, 1, 7);

        gridPane.add(new Label("Radial velocity"), 0, 8);
        TextField radialVelocityLabel = new TextField();
        gridPane.add(radialVelocityLabel, 1, 8);

        gridPane.add(new Label("bprp"), 0, 9);
        TextField bprpLabel = new TextField();
        gridPane.add(bprpLabel, 1, 9);

        gridPane.add(new Label("bpg"), 0, 10);
        TextField bpgLabel = new TextField();
        gridPane.add(bpgLabel, 1, 10);

        gridPane.add(new Label("grp"), 0, 11);
        TextField grpLabel = new TextField();
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
        gridPane.add(recordIdLabel, 1, 1);

        gridPane.add(new Label("Data set name"), 0, 2);
        Label dataSetLabel = new Label();
        gridPane.add(dataSetLabel, 1, 2);

        gridPane.add(new Label("Star name"), 0, 3);
        TextField starNameTextField = new TextField();
        gridPane.add(starNameTextField, 1, 3);

        gridPane.add(new Label("Color"), 0, 4);
        ColorPicker starColorPicker = new ColorPicker();
        gridPane.add(starColorPicker, 1, 4);

        gridPane.add(new Label("Radius"), 0, 5);
        TextField radiusTextField = new TextField();
        radiusTextField.setPromptText("the radius in Sol units");
        gridPane.add(radiusTextField, 1, 5);

        gridPane.add(new Label("Distance"), 0, 6);
        TextField distanceNameTextField = new TextField();
        distanceNameTextField.setPromptText("the distance from Sol in ly");
        gridPane.add(distanceNameTextField, 1, 6);

        gridPane.add(new Label("Spectral class"), 0, 7);
        TextField spectralClassTextField = new TextField();
        spectralClassTextField.setText(" the spectral class as in O, A, etc.");
        gridPane.add(spectralClassTextField, 1, 7);

        gridPane.add(new Label("Temperature"), 0, 8);
        TextField tempTextField = new TextField();
        tempTextField.setPromptText("the surface temperature of the star");
        gridPane.add(tempTextField, 1, 8);

        gridPane.add(new Label("Coordinates"), 0, 9);
        GridPane coordGrid = new GridPane();
        gridPane.add(coordGrid, 1, 9);
        TextField xTextField = new TextField();
        xTextField.setPromptText("X coordinate");
        coordGrid.add(xTextField, 0, 1);
        TextField yTextField = new TextField();
        yTextField.setPromptText("Y coordinate");
        coordGrid.add(yTextField, 1, 1);
        TextField zTextField = new TextField();
        zTextField.setPromptText("Z coordinate");
        coordGrid.add(zTextField, 2, 1);

        gridPane.add(new Label("Notes"), 0, 10);
        TextArea notesArea = new TextArea();
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
