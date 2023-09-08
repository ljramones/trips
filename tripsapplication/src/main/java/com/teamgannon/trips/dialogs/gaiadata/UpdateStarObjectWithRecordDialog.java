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
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class UpdateStarObjectWithRecordDialog extends Dialog<Boolean> {


    private final DatabaseManagementService service;
    private final StarRecord starRecord;
    private final StarObject starObject;
    private VBox vBox;
    private GridPane gridPane;

    public UpdateStarObjectWithRecordDialog(DatabaseManagementService service, StarRecord starRecord, StarObject starObject) {
        this.service = service;
        this.starRecord = starRecord;
        this.starObject = starObject;

        initializeDialog();
        initializeUIComponents();

        HBox hBox1 = new HBox();
        hBox1.setAlignment(Pos.CENTER);
        vBox.getChildren().add(new Separator());
        vBox.getChildren().add(hBox1);

        Button nextButton = new Button("Save");
        nextButton.setOnAction(this::updateStarClicked);

        Button dismissButton = new Button("Dismiss");
        dismissButton.setOnAction(this::dismissClicked);
        dismissButton.setOnAction(this::dismissClicked);

        hBox1.getChildren().addAll(nextButton, dismissButton);
    }

    private void initializeDialog() {
        this.setTitle("Update Star");
        this.setHeaderText("Update an existing star record");
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

        // name fields
        Label nameLabel = new Label("Name");
        TextField nameField = new TextField();
        nameField.setText(starRecord.getObjName());
        Button nameCopyButton = new Button("-->");
        TextField nameTransferField = new TextField();
        nameTransferField.setText(starObject.getDisplayName());
        nameCopyButton.setOnAction(actionEvent -> {
            nameTransferField.setText(starRecord.getObjName());
        });
        leftPane.add(nameLabel, 0, 0);
        leftPane.add(nameField, 1, 0);
        leftPane.add(nameCopyButton, 2, 0);
        leftPane.add(nameTransferField, 3, 0);

        // ra fields
        Label raLabel = new Label("RA");
        TextField raField = new TextField();
        Button raCopyButton = new Button("-->");
        TextField raTransferField = new TextField();
        raTransferField.setText(starObject.getRa() + "");
        raCopyButton.setOnAction(actionEvent -> {
            raTransferField.setText(starRecord.getRAdeg() + "");
        });
        raField.setText(starRecord.getRAdeg() + "");
        leftPane.add(raLabel, 0, 1);
        leftPane.add(raField, 1, 1);
        leftPane.add(raCopyButton, 2, 1);
        leftPane.add(raTransferField, 3, 1);

        // dec fields
        Label decLabel = new Label("Dec");
        TextField decField = new TextField();
        decField.setText(starRecord.getDEdeg() + "");
        Button decCopyButton = new Button("-->");
        TextField decTransferField = new TextField();
        decTransferField.setText(starObject.getDeclination() + "");
        decCopyButton.setOnAction(actionEvent -> {
            decTransferField.setText(starRecord.getDEdeg() + "");
        });
        leftPane.add(decLabel, 0, 2);
        leftPane.add(decField, 1, 2);
        leftPane.add(decCopyButton, 2, 2);
        leftPane.add(decTransferField, 3, 2);

        // distance fields
        Label distanceLabel = new Label("Distance");
        TextField distanceField = new TextField();
        distanceField.setText(starRecord.getDistance() + "");
        Button distanceCopyButton = new Button("-->");
        TextField distanceTransferField = new TextField();
        distanceTransferField.setText(starObject.getDistance() + "");
        distanceCopyButton.setOnAction(actionEvent -> {
            distanceTransferField.setText(starRecord.getDistance() + "");
        });
        leftPane.add(distanceLabel, 0, 3);
        leftPane.add(distanceField, 1, 3);
        leftPane.add(distanceCopyButton, 2, 3);
        leftPane.add(distanceTransferField, 3, 3);

        // Spectral type
        Label spectralTypeLabel = new Label("Spectral Type");
        TextField spectralTypeField = new TextField();
        spectralTypeField.setText(starRecord.getSpType());
        Button spectralTypeCopyButton = new Button("-->");
        TextField spectralTypeTransferField = new TextField();
        spectralTypeTransferField.setText(starObject.getSpectralClass());
        spectralTypeCopyButton.setOnAction(actionEvent -> {
            spectralTypeTransferField.setText(starRecord.getSpType());
        });
        leftPane.add(spectralTypeLabel, 0, 4);
        leftPane.add(spectralTypeField, 1, 4);
        leftPane.add(spectralTypeCopyButton, 2, 4);
        leftPane.add(spectralTypeTransferField, 3, 4);

        // coordinates - X
        Label xLabel = new Label("X");
        TextField xField = new TextField();
        xField.setText(starRecord.getCoordinates()[0] + "");
        Button xCopyButton = new Button("-->");
        TextField xTransferField = new TextField();
        xTransferField.setText(starObject.getX() + "");
        xCopyButton.setOnAction(actionEvent -> {
            xTransferField.setText(starRecord.getCoordinates()[0] + "");
        });
        leftPane.add(xLabel, 0, 5);
        leftPane.add(xField, 1, 5);
        leftPane.add(xCopyButton, 2, 5);
        leftPane.add(xTransferField, 3, 5);

        // coordinates - Y
        Label yLabel = new Label("Y");
        TextField yField = new TextField();
        yField.setText(starRecord.getCoordinates()[1] + "");
        Button yCopyButton = new Button("-->");
        TextField yTransferField = new TextField();
        yTransferField.setText(starObject.getY() + "");
        yCopyButton.setOnAction(actionEvent -> {
            yTransferField.setText(starRecord.getCoordinates()[1] + "");
        });
        leftPane.add(yLabel, 0, 6);
        leftPane.add(yField, 1, 6);
        leftPane.add(yCopyButton, 2, 6);
        leftPane.add(yTransferField, 3, 6);

        // coordinates - Z
        Label zLabel = new Label("Z");
        TextField zField = new TextField();
        zField.setText(starRecord.getCoordinates()[2] + "");
        Button zCopyButton = new Button("-->");
        TextField zTransferField = new TextField();
        zTransferField.setText(starObject.getZ() + "");
        zCopyButton.setOnAction(actionEvent -> {
            zTransferField.setText(starRecord.getCoordinates()[2] + "");
        });
        leftPane.add(zLabel, 0, 7);
        leftPane.add(zField, 1, 7);
        leftPane.add(zCopyButton, 2, 7);
        leftPane.add(zTransferField, 3, 7);

        // number of exoplanets
        Label numExoplanetsLabel = new Label("Number of Exoplanets");
        TextField numExoplanetsField = new TextField();
        numExoplanetsField.setText(starRecord.getNexopl() + "");
        Button numExoplanetsCopyButton = new Button("-->");
        TextField numExoplanetsTransferField = new TextField();
        numExoplanetsTransferField.setText(starObject.getNumExoplanets() + "");
        leftPane.add(numExoplanetsLabel, 0, 8);
        leftPane.add(numExoplanetsField, 1, 8);
        leftPane.add(numExoplanetsCopyButton, 2, 8);
        leftPane.add(numExoplanetsTransferField, 3, 8);

        // right pane
        GridPane rightPane = new GridPane();
        rightPane.setHgap(10);
        rightPane.setVgap(10);
        gridPane.add(rightPane, 1, 0);

        // system name
        Label sysLabel = new Label("System Name");
        TextField systemNameField = new TextField();
        systemNameField.setText(starRecord.getSystemName());
        Button sysCopyButton = new Button("-->");
        TextField sysTransferField = new TextField();
        sysTransferField.setText(starObject.getSystemName());
        sysCopyButton.setOnAction(actionEvent -> {
            sysTransferField.setText(starRecord.getSystemName());
        });
        rightPane.add(sysLabel, 0, 0);
        rightPane.add(systemNameField, 1, 0);
        rightPane.add(sysCopyButton, 2, 0);
        rightPane.add(sysTransferField, 3, 0);

        // Epoch
        Label epochLabel = new Label("Epoch");
        TextField epochField = new TextField();
        epochField.setText(starRecord.getEpoch() + "");
        Button epochCopyButton = new Button("-->");
        TextField epochTransferField = new TextField();
        epochTransferField.setText(starObject.getEpoch());
        epochCopyButton.setOnAction(actionEvent -> {
            epochTransferField.setText(starRecord.getEpoch() + "");
        });
        rightPane.add(epochLabel, 0, 1);
        rightPane.add(epochField, 1, 1);
        rightPane.add(epochCopyButton, 2, 1);
        rightPane.add(epochTransferField, 3, 1);

        // pmra
        Label pmraLabel = new Label("pmra");
        TextField pmraField = new TextField();
        pmraField.setText(starRecord.getPmRA() + "");
        Button pmraCopyButton = new Button("-->");
        TextField pmraTransferField = new TextField();
        pmraTransferField.setText(starObject.getPmra() + "");
        pmraCopyButton.setOnAction(actionEvent -> {
            pmraTransferField.setText(starRecord.getPmRA() + "");
        });
        rightPane.add(pmraLabel, 0, 2);
        rightPane.add(pmraField, 1, 2);
        rightPane.add(pmraCopyButton, 2, 2);
        rightPane.add(pmraTransferField, 3, 2);

        // pmdec
        Label pmdecLabel = new Label("pmdec");
        TextField pmdecField = new TextField();
        pmdecField.setText(starRecord.getPmDE() + "");
        Button pmdecCopyButton = new Button("-->");
        TextField pmdecTransferField = new TextField();
        pmdecTransferField.setText(starObject.getPmdec() + "");
        pmdecCopyButton.setOnAction(actionEvent -> {
            pmdecTransferField.setText(starRecord.getPmDE() + "");
        });
        rightPane.add(pmdecLabel, 0, 3);
        rightPane.add(pmdecField, 1, 3);
        rightPane.add(pmdecCopyButton, 2, 3);
        rightPane.add(pmdecTransferField, 3, 3);

        // radial velocity
        Label rvLabel = new Label("Radial Velocity");
        TextField rvField = new TextField();
        rvField.setText(starRecord.getRV() + "");
        Button rvCopyButton = new Button("-->");
        TextField rvTransferField = new TextField();
        rvTransferField.setText(starObject.getRadialVelocity() + "");
        rvCopyButton.setOnAction(actionEvent -> {
            rvTransferField.setText(starRecord.getRV() + "");
        });
        rightPane.add(rvLabel, 0, 4);
        rightPane.add(rvField, 1, 4);
        rightPane.add(rvCopyButton, 2, 4);
        rightPane.add(rvTransferField, 3, 4);

        // Simbad Id
        Label simbadLabel = new Label("Simbad Id");
        TextField simbadField = new TextField();
        simbadField.setText(starRecord.getSIMBAD());
        Button simbadCopyButton = new Button("-->");
        TextField simbadTransferField = new TextField();
        simbadTransferField.setText(starObject.getSimbadId());
        simbadCopyButton.setOnAction(actionEvent -> {
            simbadTransferField.setText(starRecord.getSIMBAD());
        });
        rightPane.add(simbadLabel, 0, 5);
        rightPane.add(simbadField, 1, 5);
        rightPane.add(simbadCopyButton, 2, 5);
        rightPane.add(simbadTransferField, 3, 5);

        // common name
        Label commonNameLabel = new Label("Common Name");
        TextField commonNameField = new TextField();
        commonNameField.setText(starRecord.getCommon());
        Button commonNameCopyButton = new Button("-->");
        TextField commonNameTransferField = new TextField();
        commonNameTransferField.setText(starObject.getCommonName());
        commonNameCopyButton.setOnAction(actionEvent -> {
            commonNameTransferField.setText(starRecord.getCommon());
        });
        rightPane.add(commonNameLabel, 0, 6);
        rightPane.add(commonNameField, 1, 6);
        rightPane.add(commonNameCopyButton, 2, 6);
        rightPane.add(commonNameTransferField, 3, 6);

        // Gliese Id
        Label glieseLabel = new Label("Gliese Id");
        TextField glieseField = new TextField();
        glieseField.setText(starRecord.getGJ());
        Button glieseCopyButton = new Button("-->");
        TextField glieseTransferField = new TextField();
        glieseTransferField.setText(extractGlieseField(starObject.getCatalogIdList()));
        glieseCopyButton.setOnAction(actionEvent -> {
            glieseTransferField.setText(starRecord.getGJ());
        });
        rightPane.add(glieseLabel, 0, 7);
        rightPane.add(glieseField, 1, 7);
        rightPane.add(glieseCopyButton, 2, 7);
        rightPane.add(glieseTransferField, 3, 7);

        // HD Id
        Label hdLabel = new Label("HD Id");
        TextField hdField = new TextField();
        hdField.setText(starRecord.getHD());
        Button hdCopyButton = new Button("-->");
        TextField hdTransferField = new TextField();
        hdTransferField.setText(extractHDField(starObject.getCatalogIdList()));
        hdCopyButton.setOnAction(actionEvent -> {
            hdTransferField.setText(starRecord.getHD());
        });
        rightPane.add(hdLabel, 0, 8);
        rightPane.add(hdField, 1, 8);
        rightPane.add(hdCopyButton, 2, 8);
        rightPane.add(hdTransferField, 3, 8);

        // Hip Id
        Label hipLabel = new Label("Hip Id");
        TextField hipField = new TextField();
        hipField.setText(starRecord.getHIP());
        Button hipCopyButton = new Button("-->");
        TextField hipTransferField = new TextField();
        hipTransferField.setText(extractHipField(starObject.getCatalogIdList()));
        hipCopyButton.setOnAction(actionEvent -> {
            hipTransferField.setText(starRecord.getHIP());
        });
        rightPane.add(hipLabel, 0, 9);
        rightPane.add(hipField, 1, 9);
        rightPane.add(hipCopyButton, 2, 9);
        rightPane.add(hipTransferField, 3, 9);

        // Notes
        Label notesLabel = new Label("Notes");
        TextField notesField = new TextField();
        notesField.setText(starRecord.getCom());
        Button notesCopyButton = new Button("-->");
        TextField notesTransferField = new TextField();
        notesTransferField.setText(starObject.getNotes());
        notesCopyButton.setOnAction(actionEvent -> {
            notesTransferField.setText(starRecord.getCom() + " " + notesField.getText());
        });
        rightPane.add(notesLabel, 0, 10);
        rightPane.add(notesField, 1, 10);
    }

    private String extractHipField(List<String> catalogIdList) {
        log.info("catalog id list: {}", catalogIdList);
        return "";
    }

    private String extractHDField(List<String> catalogIdList) {
        log.info("catalog id list: {}", catalogIdList);
        return "";
    }

    private String extractGlieseField(List<String> catalogIdList) {
        return null;
    }

    private void dismissClicked(ActionEvent actionEvent) {
        setResult(false);
    }

    private void updateStarClicked(ActionEvent actionEvent) {
        if (starObject != null) {
            service.updateStar(starObject);
        }
        setResult(true);
    }


    private void close(WindowEvent windowEvent) {
        setResult(false);
    }

}
