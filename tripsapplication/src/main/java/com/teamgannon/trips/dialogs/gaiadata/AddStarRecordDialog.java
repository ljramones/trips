package com.teamgannon.trips.dialogs.gaiadata;

import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.service.DatabaseManagementService;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class AddStarRecordDialog extends Dialog<Boolean> {

    /**
     * the database service
     */
    private final DatabaseManagementService service;

    /**
     * the star record
     */
    private final StarRecord starRecord;

    /**
     * the vertical box
     */
    private VBox vBox;

    /**
     * the grid pane
     */
    private GridPane gridPane;

    /**
     * the star object
     */
    private StarObject starObject = new StarObject();

    /**
     * constructor
     *
     * @param service    the database service
     * @param starRecord the star record
     */
    public AddStarRecordDialog(DatabaseManagementService service, StarRecord starRecord) {
        this.service = service;
        this.starRecord = starRecord;

        initializeDialog();
        initializeUIComponents();

        HBox hBox1 = new HBox();
        hBox1.setAlignment(Pos.CENTER);
        vBox.getChildren().add(new Separator());
        vBox.getChildren().add(hBox1);

        Button nextButton = new Button("Save");
        nextButton.setOnAction(this::saveStarClicked);

        Button dismissButton = new Button("Dismiss");
        dismissButton.setOnAction(this::dismissClicked);
        dismissButton.setOnAction(this::dismissClicked);

        hBox1.getChildren().addAll(nextButton, dismissButton);
    }

    private void initializeDialog() {
        this.setTitle("Add Star Record");
        this.setHeaderText("Add a new star record");
        this.setResizable(true);

        this.setHeight(500);
        this.setWidth(500);

        vBox = new VBox();
        gridPane = new GridPane();
        vBox.getChildren().add(gridPane);
        this.getDialogPane().setContent(vBox);

        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        stage.setOnCloseRequest(this::close);
    }


    private void initializeUIComponents() {

        gridPane.setHgap(20);

        // left pane
        GridPane leftPane = new GridPane();
        leftPane.setHgap(10);
        leftPane.setVgap(10);
        gridPane.add(leftPane, 0, 0);

        Label nameLabel = new Label("Name");
        TextField nameField = new TextField();
        nameField.setText(starRecord.getObjName());
        leftPane.add(nameLabel, 0, 0);
        leftPane.add(nameField, 1, 0);

        Label raLabel = new Label("RA");
        leftPane.add(raLabel, 0, 1);
        TextField raField = new TextField();
        raField.setText(starRecord.getRAdeg() + "");
        leftPane.add(raField, 1, 1);

        Label decLabel = new Label("Dec");
        leftPane.add(decLabel, 0, 2);
        TextField decField = new TextField();
        decField.setText(starRecord.getDEdeg() + "");
        leftPane.add(decField, 1, 2);

        Label distanceLabel = new Label("Distance");
        leftPane.add(distanceLabel, 0, 3);
        TextField distanceField = new TextField();
        distanceField.setText(starRecord.getDistance() + "");
        leftPane.add(distanceField, 1, 3);

        Label spectralTypeLabel = new Label("Spectral Type");
        leftPane.add(spectralTypeLabel, 0, 4);
        TextField spectralTypeField = new TextField();
        spectralTypeField.setText(starRecord.getSpType());
        leftPane.add(spectralTypeField, 1, 4);

        Label xLabel = new Label("X");
        leftPane.add(xLabel, 0, 5);
        TextField xField = new TextField();
        xField.setText(starRecord.getCoordinates()[0] + "");
        leftPane.add(xField, 1, 5);

        Label yLabel = new Label("Y");
        leftPane.add(yLabel, 0, 6);
        TextField yField = new TextField();
        yField.setText(starRecord.getCoordinates()[1] + "");
        leftPane.add(yField, 1, 6);

        Label zLabel = new Label("Z");
        leftPane.add(zLabel, 0, 7);
        TextField zField = new TextField();
        zField.setText(starRecord.getCoordinates()[2] + "");
        leftPane.add(zField, 1, 7);

        Label numExoplanetsLabel = new Label("Number of Exoplanets");
        leftPane.add(numExoplanetsLabel, 0, 8);
        TextField numExoplanetsField = new TextField();
        numExoplanetsField.setText(starRecord.getNexopl() + "");
        leftPane.add(numExoplanetsField, 1, 8);

        // right pane
        GridPane rightPane = new GridPane();
        rightPane.setHgap(10);
        rightPane.setVgap(10);
        gridPane.add(rightPane, 1, 0);

        Label sysLabel = new Label("System Name");
        rightPane.add(sysLabel, 0, 0);
        TextField systemNameField = new TextField();
        systemNameField.setText(starRecord.getSystemName());
        rightPane.add(systemNameField, 1, 0);

        Label epochLabel = new Label("Epoch");
        rightPane.add(epochLabel, 0, 1);
        TextField epochField = new TextField();
        epochField.setText(starRecord.getEpoch() + "");
        rightPane.add(epochField, 1, 1);

        Label pmraLabel = new Label("pmra");
        rightPane.add(pmraLabel, 0, 2);
        TextField pmraField = new TextField();
        pmraField.setText(starRecord.getPmRA() + "");
        rightPane.add(pmraField, 1, 2);

        Label pmdecLabel = new Label("pmdec");
        rightPane.add(pmdecLabel, 0, 3);
        TextField pmdecField = new TextField();
        pmdecField.setText(starRecord.getPmDE() + "");
        rightPane.add(pmdecField, 1, 3);

        Label rvLabel = new Label("Radial Velocity");
        rightPane.add(rvLabel, 0, 4);
        TextField rvField = new TextField();
        rvField.setText(starRecord.getRV() + "");
        rightPane.add(rvField, 1, 4);

        Label simbadLabel = new Label("Simbad Id");
        rightPane.add(simbadLabel, 0, 5);
        TextField simbadField = new TextField();
        simbadField.setText(starRecord.getSIMBAD());
        rightPane.add(simbadField, 1, 5);

        Label commonNameLabel = new Label("Common Name");
        rightPane.add(commonNameLabel, 0, 6);
        TextField commonNameField = new TextField();
        commonNameField.setText(starRecord.getCommon());
        rightPane.add(commonNameField, 1, 6);

        Label glieseLabel = new Label("Gliese Id");
        rightPane.add(glieseLabel, 0, 7);
        TextField glieseField = new TextField();
        glieseField.setText(starRecord.getGJ());
        rightPane.add(glieseField, 1, 7);

        Label hdLabel = new Label("HD Id");
        rightPane.add(hdLabel, 0, 8);
        TextField hdField = new TextField();
        hdField.setText(starRecord.getHD());
        rightPane.add(hdField, 1, 8);

        Label hipLabel = new Label("Hip Id");
        rightPane.add(hipLabel, 0, 9);
        TextField hipField = new TextField();
        hipField.setText(starRecord.getHIP());
        rightPane.add(hipField, 1, 9);

        Label notesLabel = new Label("Notes");
        rightPane.add(notesLabel, 0, 10);
        TextField notesField = new TextField();
        notesField.setText(starRecord.getCom());
        rightPane.add(notesField, 1, 10);
    }

    private void dismissClicked(ActionEvent actionEvent) {
        setResult(false);
    }

    private void saveStarClicked(ActionEvent actionEvent) {
        if (starObject != null) {
            service.updateStar(starObject);
        }
        setResult(true);
    }


    private void close(WindowEvent windowEvent) {
        setResult(false);
    }


}
