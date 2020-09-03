package com.teamgannon.trips.screenobjects;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.AstrographicObject;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;


@Slf4j
public class StarEditDialog extends Dialog<StarEditStatus> {

    private final AstrographicObject record;

    // UI elements
    private final Label recordIdLabel = new Label();
    private final Label dataSetLabel = new Label();
    private final TextField starNameTextField = new TextField();
    private final ColorPicker starColorPicker = new ColorPicker();
    private final TextField radiusTextField = new TextField();
    private final TextField distanceNameTextField = new TextField();
    private final TextField spectralClassTextField = new TextField();
    private final TextField tempTextField = new TextField();

    private final TextField xTextField = new TextField();
    private final TextField yTextField = new TextField();
    private final TextField zTextField = new TextField();

    //////////

    TextField raLabel = new TextField();
    TextField pmraLabel = new TextField();
    TextField decLabel = new TextField();
    TextField pmdecLabel = new TextField();
    TextField decdegLabel = new TextField();
    TextField rsLabel = new TextField();
    TextField parallaxLabel = new TextField();
    TextField radialVelocityLabel = new TextField();
    TextField bprpLabel = new TextField();
    TextField bpgLabel = new TextField();
    TextField grpLabel = new TextField();

    /////////
    TextField polityTextField = new TextField();
    TextField worldTypeTextField = new TextField();
    TextField fuelTypeTextField = new TextField();
    TextField techTypeTextField = new TextField();
    TextField portTypeTextField = new TextField();
    TextField popTypeTextField = new TextField();
    TextField prodField = new TextField();
    TextField milspaceTextField = new TextField();
    TextField milplanTextField = new TextField();

    CheckBox anomalyCheckbox = new CheckBox();
    CheckBox otherCheckbox = new CheckBox();

    ////////////////

    TabPane tabPane;

    public StarEditDialog(AstrographicObject record) {
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

        this.setTitle("Change attributes for " + record.getDisplayName());
        this.getDialogPane().setContent(vBox);
    }

    private Pane createFictionalTab() {
        // setup grid structure
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setVgap(5);
        gridPane.setHgap(5);

        gridPane.add(new Label("polity"), 0, 1);
        polityTextField.setText(record.getPolity());
        polityTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkPolity();
            }
        });
        gridPane.add(polityTextField, 1, 1);

        gridPane.add(new Label("world type"), 0, 2);
        worldTypeTextField.setText(record.getWorldType());
        worldTypeTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkWorldType();
            }
        });
        gridPane.add(worldTypeTextField, 1, 2);

        gridPane.add(new Label("fuel type"), 0, 3);
        fuelTypeTextField.setText(record.getFuelType());
        fuelTypeTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkFuelType();
            }
        });
        gridPane.add(fuelTypeTextField, 1, 3);

        gridPane.add(new Label("tech type"), 0, 4);
        techTypeTextField.setText(record.getTechType());
        techTypeTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkTechType();
            }
        });
        gridPane.add(techTypeTextField, 1, 4);

        gridPane.add(new Label("port type"), 0, 5);
        portTypeTextField.setText(record.getPortType());
        portTypeTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkPortType();
            }
        });
        gridPane.add(portTypeTextField, 1, 5);

        gridPane.add(new Label("population type"), 0, 6);
        popTypeTextField.setText(record.getPopulationType());
        popTypeTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkPopType();
            }
        });
        gridPane.add(popTypeTextField, 1, 6);

        gridPane.add(new Label("product type"), 0, 7);
        prodField.setText(record.getProductType());
        prodField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkProd();
            }
        });
        gridPane.add(prodField, 1, 7);

        gridPane.add(new Label("milspace type"), 0, 8);
        milspaceTextField.setText(record.getMilSpaceType());
        milspaceTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkMilSpace();
            }
        });
        gridPane.add(milspaceTextField, 1, 8);

        gridPane.add(new Label("milplan type"), 0, 9);
        milplanTextField.setText(record.getMilPlanType());
        milplanTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkMilPlan();
            }
        });
        gridPane.add(milplanTextField, 1, 9);

        gridPane.add(new Label("anomaly"), 0, 10);
        anomalyCheckbox.setSelected(record.isAnomaly());
        anomalyCheckbox.setOnAction(event -> record.setAnomaly(anomalyCheckbox.isSelected()));
        gridPane.add(anomalyCheckbox, 1, 10);

        gridPane.add(new Label("other"), 0, 11);
        otherCheckbox.setSelected(record.isOther());
        otherCheckbox.setOnAction(event -> record.setOther(otherCheckbox.isSelected()));
        gridPane.add(otherCheckbox, 1, 11);

        return gridPane;
    }

    private void checkPolity() {
        record.setPolity(polityTextField.getText());
    }

    private void checkWorldType() {
        record.setWorldType(worldTypeTextField.getText());
    }

    private void checkFuelType() {
        record.setFuelType(fuelTypeTextField.getText());
    }

    private void checkTechType() {
        record.setTechType(techTypeTextField.getText());
    }

    private void checkPortType() {
        record.setPortType(portTypeTextField.getText());
    }

    private void checkPopType() {
        record.setPopulationType(popTypeTextField.getText());
    }

    private void checkProd() {
        record.setProductType(prodField.getText());
    }

    private void checkMilSpace() {
        record.setMilSpaceType(milspaceTextField.getText());
    }

    private void checkMilPlan() {
        record.setMilPlanType(milspaceTextField.getText());
    }

    private Pane createSecondaryTab() {

        // setup grid structure
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setVgap(5);
        gridPane.setHgap(5);

        // items for right grid
        gridPane.add(new Label("ra"), 0, 1);
        raLabel.setText(Double.toString(record.getRa()));
        raLabel.setPromptText("right ascension");
        raLabel.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkRa();
            }
        });
        gridPane.add(raLabel, 1, 1);

        gridPane.add(new Label("pmra"), 0, 2);
        pmraLabel.setText(Double.toString(record.getPmra()));
        pmraLabel.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkPmra();
            }
        });
        gridPane.add(pmraLabel, 1, 2);

        gridPane.add(new Label("declination"), 0, 3);
        decLabel.setText(Double.toString(record.getDeclination()));
        decLabel.setPromptText("declination");
        decLabel.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkDeclination();
            }
        });
        gridPane.add(decLabel, 1, 3);

        gridPane.add(new Label("pmdec"), 0, 4);
        pmdecLabel.setText(Double.toString(record.getPmdec()));
        pmdecLabel.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkPmdec();
            }
        });
        gridPane.add(pmdecLabel, 1, 4);

        gridPane.add(new Label("dec_deg"), 0, 5);
        decdegLabel.setText(Double.toString(record.getDec_deg()));
        decdegLabel.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkDecDeg();
            }
        });
        gridPane.add(decdegLabel, 1, 5);

        gridPane.add(new Label("rs_cdeg"), 0, 6);
        rsLabel.setText(Double.toString(record.getRs_cdeg()));
        rsLabel.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkRsCdeg();
            }
        });
        gridPane.add(rsLabel, 1, 6);

        gridPane.add(new Label("Parallax"), 0, 7);
        parallaxLabel.setText(Double.toString(record.getParallax()));
        parallaxLabel.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkParallax();
            }
        });
        gridPane.add(parallaxLabel, 1, 7);

        gridPane.add(new Label("Radial velocity"), 0, 8);
        radialVelocityLabel.setText(Double.toString(record.getRadialVelocity()));
        radialVelocityLabel.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkRadialVel();
            }
        });
        gridPane.add(radialVelocityLabel, 1, 8);

        gridPane.add(new Label("bprp"), 0, 9);
        bprpLabel.setText(Double.toString(record.getBprp()));
        bprpLabel.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkBprp();
            }
        });
        gridPane.add(bprpLabel, 1, 9);

        gridPane.add(new Label("bpg"), 0, 10);
        bpgLabel.setText(Double.toString(record.getBpg()));
        bpgLabel.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkBpg();
            }
        });
        gridPane.add(bpgLabel, 1, 10);

        gridPane.add(new Label("grp"), 0, 11);
        grpLabel.setText(Double.toString(record.getGrp()));
        grpLabel.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkGrp();
            }
        });
        gridPane.add(grpLabel, 1, 11);

        return gridPane;

    }

    private void checkRa() {
        try {
            double ra = Double.parseDouble(raLabel.getText());
            record.setRa(ra);
        } catch (NumberFormatException nfe) {
            showErrorAlert("Edit Star Record", raLabel.getText() + " is an invalid floating point number");
        }
    }

    private void checkPmra() {
        try {
            double pmra = Double.parseDouble(pmraLabel.getText());
            record.setPmra(pmra);
        } catch (NumberFormatException nfe) {
            showErrorAlert("Edit Star Record", pmraLabel.getText() + " is an invalid floating point number");
        }
    }

    private void checkDeclination() {
        try {
            double dec = Double.parseDouble(decLabel.getText());
            record.setDeclination(dec);
        } catch (NumberFormatException nfe) {
            showErrorAlert("Edit Star Record", decLabel.getText() + " is an invalid floating point number");
        }
    }

    private void checkPmdec() {
        try {
            double pmdec = Double.parseDouble(pmdecLabel.getText());
            record.setPmdec(pmdec);
        } catch (NumberFormatException nfe) {
            showErrorAlert("Edit Star Record", pmdecLabel.getText() + " is an invalid floating point number");
        }
    }

    private void checkDecDeg() {
        try {
            double decdeg = Double.parseDouble(decdegLabel.getText());
            record.setRs_cdeg(decdeg);
        } catch (NumberFormatException nfe) {
            showErrorAlert("Edit Star Record", decdegLabel.getText() + " is an invalid floating point number");
        }
    }

    private void checkRsCdeg() {
        try {
            double rs = Double.parseDouble(rsLabel.getText());
            record.setRs_cdeg(rs);
        } catch (NumberFormatException nfe) {
            showErrorAlert("Edit Star Record", rsLabel.getText() + " is an invalid floating point number");
        }
    }

    private void checkParallax() {
        try {
            double parallax = Double.parseDouble(parallaxLabel.getText());
            record.setParallax(parallax);
        } catch (NumberFormatException nfe) {
            showErrorAlert("Edit Star Record", parallaxLabel.getText() + " is an invalid floating point number");
        }
    }

    private void checkRadialVel() {
        try {
            double radialVelocity = Double.parseDouble(radialVelocityLabel.getText());
            record.setRadialVelocity(radialVelocity);
        } catch (NumberFormatException nfe) {
            showErrorAlert("Edit Star Record", radialVelocityLabel.getText() + " is an invalid floating point number");
        }
    }

    private void checkBprp() {
        try {
            double bprp = Double.parseDouble(bprpLabel.getText());
            record.setBprp(bprp);
        } catch (NumberFormatException nfe) {
            showErrorAlert("Edit Star Record", bprpLabel.getText() + " is an invalid floating point number");
        }
    }

    private void checkBpg() {
        try {
            double bpg = Double.parseDouble(bpgLabel.getText());
            record.setBpg(bpg);
        } catch (NumberFormatException nfe) {
            showErrorAlert("Edit Star Record", bpgLabel.getText() + " is an invalid floating point number");
        }
    }

    private void checkGrp() {
        try {
            double grp = Double.parseDouble(grpLabel.getText());
            record.setGrp(grp);
        } catch (NumberFormatException nfe) {
            showErrorAlert("Edit Star Record", grpLabel.getText() + " is an invalid floating point number");
        }
    }

    private Pane createOverviewTab() {

        // setup grid structure
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setVgap(5);
        gridPane.setHgap(5);

        gridPane.add(new Label("Record id"), 0, 1);
        recordIdLabel.setText(record.getId().toString());
        gridPane.add(recordIdLabel, 1, 1);

        gridPane.add(new Label("Data set name"), 0, 2);
        dataSetLabel.setText(record.getDataSetName());
        gridPane.add(dataSetLabel, 1, 2);

        gridPane.add(new Label("Star name"), 0, 3);
        starNameTextField.setText(record.getDisplayName());
        starNameTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                record.setDisplayName(starNameTextField.getText());
            }
        });
        gridPane.add(starNameTextField, 1, 3);

        gridPane.add(new Label("Color"), 0, 4);
        starColorPicker.setValue(record.getStarColor());
        starColorPicker.setOnAction(event -> record.setStarColor(starColorPicker.getValue()));
        gridPane.add(starColorPicker, 1, 4);

        gridPane.add(new Label("Radius"), 0, 5);
        radiusTextField.setText(Double.toString(record.getRadius()));
        radiusTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkRadius();
            }
        });
        radiusTextField.setPromptText("the radius in Sol units");
        gridPane.add(radiusTextField, 1, 5);

        gridPane.add(new Label("Distance"), 0, 6);
        distanceNameTextField.setText(Double.toString(record.getDistance()));
        distanceNameTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkDistance();
            }
        });
        distanceNameTextField.setPromptText("the distance from Sol in ly");
        gridPane.add(distanceNameTextField, 1, 6);

        gridPane.add(new Label("Spectral class"), 0, 7);
        spectralClassTextField.setText(record.getSpectralClass());
        spectralClassTextField.setText(" the spectral class as in O, A, etc.");
        spectralClassTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                record.setSpectralClass(spectralClassTextField.getText());
            }
        });
        gridPane.add(spectralClassTextField, 1, 7);

        gridPane.add(new Label("Temperature"), 0, 8);
        tempTextField.setText(Double.toString(record.getTemperature()));
        tempTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkTemp();
            }
        });
        tempTextField.setPromptText("the surface temperature of the star");
        gridPane.add(tempTextField, 1, 8);

        gridPane.add(new Label("Coordinates"), 0, 9);
        GridPane coordGrid = new GridPane();
        gridPane.add(coordGrid, 1, 9);
        xTextField.setText(Double.toString(record.getX()));
        xTextField.setPromptText("X coordinate");
        xTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkX();
            }
        });
        coordGrid.add(xTextField, 0, 1);
        yTextField.setText(Double.toString(record.getY()));
        yTextField.setPromptText("Y coordinate");
        yTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkY();
            }
        });
        coordGrid.add(yTextField, 1, 1);
        zTextField.setText(Double.toString(record.getZ()));
        zTextField.setPromptText("Z coordinate");
        zTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkZ();
            }
        });
        coordGrid.add(zTextField, 2, 1);

        gridPane.add(new Label("Notes"), 0, 10);
        TextArea notesArea = new TextArea();
        notesArea.setText(record.getNotes());
        notesArea.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                record.setNotes(notesArea.getText());
            }
        });
        notesArea.setPromptText("Enter a description or general notes on this star");
        gridPane.add(notesArea, 1, 10, 1, 3);

        return gridPane;
    }

    private void checkRadius() {
        try {
            double radius = Double.parseDouble(radiusTextField.getText());
            record.setRadius(radius);
        } catch (NumberFormatException nfe) {
            showErrorAlert("Edit Star Record", radiusTextField.getText() + " is an invalid floating point number");
        }
    }

    private void checkDistance() {
        try {
            double distance = Double.parseDouble(distanceNameTextField.getText());
            record.setDistance(distance);
        } catch (NumberFormatException nfe) {
            showErrorAlert("Edit Star Record", distanceNameTextField.getText() + " is an invalid floating point number");
        }
    }

    private void checkTemp() {
        try {
            double temp = Double.parseDouble(tempTextField.getText());
            record.setTemperature(temp);
        } catch (NumberFormatException nfe) {
            showErrorAlert("Edit Star Record", tempTextField.getText() + " is an invalid floating point number");
        }
    }

    private void checkX() {
        try {
            double x = Double.parseDouble(xTextField.getText());
            record.setX(x);
        } catch (NumberFormatException nfe) {
            showErrorAlert("Edit Star Record", xTextField.getText() + " is an invalid floating point number");
        }
    }

    private void checkY() {
        try {
            double y = Double.parseDouble(yTextField.getText());
            record.setY(y);
        } catch (NumberFormatException nfe) {
            showErrorAlert("Edit Star Record", yTextField.getText() + " is an invalid floating point number");
        }
    }

    private void checkZ() {
        try {
            double z = Double.parseDouble(zTextField.getText());
            record.setZ(z);
        } catch (NumberFormatException nfe) {
            showErrorAlert("Edit Star Record", zTextField.getText() + " is an invalid floating point number");
        }
    }

    private void changeClicked(ActionEvent actionEvent) {
        StarEditStatus starEditStatus = new StarEditStatus();
        starEditStatus.setRecord(record);
        starEditStatus.setChanged(true);
        setResult(starEditStatus);
    }

    private void dismissClicked(ActionEvent actionEvent) {
        StarEditStatus starEditStatus = new StarEditStatus();
        starEditStatus.setChanged(false);
        setResult(starEditStatus);
    }

}