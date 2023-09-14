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

import static com.teamgannon.trips.dialogs.gaiadata.StarObjectUtils.*;

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

        // header fields
        Label headerFileLabel = new Label("File");
        leftPane.add(headerFileLabel, 1, 0);
        Label headerDBLabel = new Label("Database");
        leftPane.add(headerDBLabel, 3, 0);

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
        leftPane.add(nameLabel, 0, 1);
        leftPane.add(nameField, 1, 1);
        leftPane.add(nameCopyButton, 2, 1);
        leftPane.add(nameTransferField, 3, 1);

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
        leftPane.add(raLabel, 0, 2);
        leftPane.add(raField, 1, 2);
        leftPane.add(raCopyButton, 2, 2);
        leftPane.add(raTransferField, 3, 2);

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
        leftPane.add(decLabel, 0, 3);
        leftPane.add(decField, 1, 3);
        leftPane.add(decCopyButton, 2, 3);
        leftPane.add(decTransferField, 3, 3);

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
        leftPane.add(distanceLabel, 0, 4);
        leftPane.add(distanceField, 1, 4);
        leftPane.add(distanceCopyButton, 2, 4);
        leftPane.add(distanceTransferField, 3, 4);

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
        leftPane.add(spectralTypeLabel, 0, 5);
        leftPane.add(spectralTypeField, 1, 5);
        leftPane.add(spectralTypeCopyButton, 2, 5);
        leftPane.add(spectralTypeTransferField, 3, 5);

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
        leftPane.add(xLabel, 0, 6);
        leftPane.add(xField, 1, 6);
        leftPane.add(xCopyButton, 2, 6);
        leftPane.add(xTransferField, 3, 6);

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
        leftPane.add(yLabel, 0, 7);
        leftPane.add(yField, 1, 7);
        leftPane.add(yCopyButton, 2, 7);
        leftPane.add(yTransferField, 3, 7);

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
        leftPane.add(zLabel, 0, 8);
        leftPane.add(zField, 1, 8);
        leftPane.add(zCopyButton, 2, 8);
        leftPane.add(zTransferField, 3, 8);

        // number of exoplanets
        Label numExoplanetsLabel = new Label("Number of Exoplanets");
        TextField numExoplanetsField = new TextField();
        numExoplanetsField.setText(starRecord.getNexopl() + "");
        Button numExoplanetsCopyButton = new Button("-->");
        TextField numExoplanetsTransferField = new TextField();
        numExoplanetsTransferField.setText(starObject.getNumExoplanets() + "");
        numExoplanetsCopyButton.setOnAction(actionEvent -> {
            numExoplanetsTransferField.setText(starRecord.getNexopl() + "");
        });
        leftPane.add(numExoplanetsLabel, 0, 9);
        leftPane.add(numExoplanetsField, 1, 9);
        leftPane.add(numExoplanetsCopyButton, 2, 9);
        leftPane.add(numExoplanetsTransferField, 3, 9);

        // right pane
        GridPane rightPane = new GridPane();
        rightPane.setHgap(10);
        rightPane.setVgap(10);
        gridPane.add(rightPane, 1, 0);

        // header fields
        Label headerFileLabel1 = new Label("File");
        rightPane.add(headerFileLabel1, 1, 0);
        Label headerDBLabel1 = new Label("Database");
        rightPane.add(headerDBLabel1, 3, 0);

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
        rightPane.add(sysLabel, 0, 1);
        rightPane.add(systemNameField, 1, 1);
        rightPane.add(sysCopyButton, 2, 1);
        rightPane.add(sysTransferField, 3, 1);

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
        rightPane.add(epochLabel, 0, 2);
        rightPane.add(epochField, 1, 2);
        rightPane.add(epochCopyButton, 2, 2);
        rightPane.add(epochTransferField, 3, 2);

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
        rightPane.add(pmraLabel, 0, 3);
        rightPane.add(pmraField, 1, 3);
        rightPane.add(pmraCopyButton, 2, 3);
        rightPane.add(pmraTransferField, 3, 3);

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
        rightPane.add(pmdecLabel, 0, 4);
        rightPane.add(pmdecField, 1, 4);
        rightPane.add(pmdecCopyButton, 2, 4);
        rightPane.add(pmdecTransferField, 3, 4);

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
        rightPane.add(rvLabel, 0, 5);
        rightPane.add(rvField, 1, 5);
        rightPane.add(rvCopyButton, 2, 5);
        rightPane.add(rvTransferField, 3, 5);

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
        rightPane.add(simbadLabel, 0, 6);
        rightPane.add(simbadField, 1, 6);
        rightPane.add(simbadCopyButton, 2, 6);
        rightPane.add(simbadTransferField, 3, 6);

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
        rightPane.add(commonNameLabel, 0, 7);
        rightPane.add(commonNameField, 1, 7);
        rightPane.add(commonNameCopyButton, 2, 7);
        rightPane.add(commonNameTransferField, 3, 7);

        // Gliese Id
        Label glieseLabel = new Label("Gliese Id");
        TextField glieseField = new TextField();
        glieseField.setText(starRecord.getGJ());
        Button glieseCopyButton = new Button("-->");
        TextField glieseTransferField = new TextField();
        glieseTransferField.setText(getGlieseId(starObject.getCatalogIdList()));
        glieseCopyButton.setOnAction(actionEvent -> {
            glieseTransferField.setText(starRecord.getGJ());
        });
        rightPane.add(glieseLabel, 0, 8);
        rightPane.add(glieseField, 1, 8);
        rightPane.add(glieseCopyButton, 2, 8);
        rightPane.add(glieseTransferField, 3, 8);

        // HD Id
        Label hdLabel = new Label("HD Id");
        TextField hdField = new TextField();
        hdField.setText(starRecord.getHD());
        Button hdCopyButton = new Button("-->");
        TextField hdTransferField = new TextField();
        hdTransferField.setText(getHDId(starObject.getCatalogIdList()));
        hdCopyButton.setOnAction(actionEvent -> {
            hdTransferField.setText(starRecord.getHD());
        });
        rightPane.add(hdLabel, 0, 9);
        rightPane.add(hdField, 1, 9);
        rightPane.add(hdCopyButton, 2, 9);
        rightPane.add(hdTransferField, 3, 9);

        // Hip Id
        Label hipLabel = new Label("Hip Id");
        TextField hipField = new TextField();
        hipField.setText(starRecord.getHIP());
        Button hipCopyButton = new Button("-->");
        TextField hipTransferField = new TextField();
        hipTransferField.setText(getHipId(starObject.getCatalogIdList()));
        hipCopyButton.setOnAction(actionEvent -> {
            hipTransferField.setText(starRecord.getHIP());
        });
        rightPane.add(hipLabel, 0, 10);
        rightPane.add(hipField, 1, 10);
        rightPane.add(hipCopyButton, 2, 10);
        rightPane.add(hipTransferField, 3, 10);

        // Notes
        Label notesLabel = new Label("Notes");
        TextArea notesField = new TextArea();
        notesField.setWrapText(true);
        notesField.setPrefRowCount(2);
        notesField.setText(starRecord.getCom());
        Button notesCopyButton = new Button("-->");
        TextArea notesTransferField = new TextArea();
        notesTransferField.setPrefRowCount(6);
        notesTransferField.setWrapText(true);
        notesTransferField.setText(starObject.getNotes());
        notesCopyButton.setOnAction(actionEvent -> {
            notesTransferField.setText(starRecord.getCom() + " " + notesField.getText());
        });
        rightPane.add(notesLabel, 0, 11);
        rightPane.add(notesField, 1, 11, 3, 2);
        rightPane.add(notesCopyButton, 1, 13);
        rightPane.add(notesTransferField, 1, 14, 3, 6);
    }

    private void dismissClicked(ActionEvent actionEvent) {
        setResult(false);
    }

    private void updateStarClicked(ActionEvent actionEvent) {
        if (starObject != null) {
            // replace the various catalog entries with updates
            String replaceSubstringStartingWith = replaceOrAddSubstringStartingWith(starObject.getRawCatalogIdList(), "GJ", starRecord.getGJ());
            replaceSubstringStartingWith = replaceOrAddSubstringStartingWith(replaceSubstringStartingWith, "HD", starRecord.getHD());
            replaceSubstringStartingWith = replaceOrAddSubstringStartingWith(replaceSubstringStartingWith, "HIP", starRecord.getHIP());
            starObject.setCatalogIdList(replaceSubstringStartingWith);
            starObject.setNotes(starObject.getNotes() + ", " + starRecord.getCom());
            log.info("update: catalog id List: {}", replaceSubstringStartingWith);
            service.updateStar(starObject);
        }
        setResult(true);
    }


    private void close(WindowEvent windowEvent) {
        setResult(false);
    }


}
