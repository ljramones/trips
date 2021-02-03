package com.teamgannon.trips.screenobjects;

import com.teamgannon.trips.jpa.model.StarObject;
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
import org.jetbrains.annotations.NotNull;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;


@Slf4j
public class StarEditDialog extends Dialog<StarEditStatus> {

    private final @NotNull StarObject record;

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
    private final TextArea notesArea = new TextArea();

    //////////

    private final TextField raLabel = new TextField();
    private final TextField pmraLabel = new TextField();
    private final TextField decLabel = new TextField();
    private final TextField pmdecLabel = new TextField();
    private final TextField decdegLabel = new TextField();
    private final TextField rsLabel = new TextField();
    private final TextField parallaxLabel = new TextField();
    private final TextField radialVelocityLabel = new TextField();
    private final TextField bprpLabel = new TextField();
    private final TextField bpgLabel = new TextField();
    private final TextField grpLabel = new TextField();

    /////////
    private final TextField polityTextField = new TextField();
    private final TextField worldTypeTextField = new TextField();
    private final TextField fuelTypeTextField = new TextField();
    private final TextField techTypeTextField = new TextField();
    private final TextField portTypeTextField = new TextField();
    private final TextField popTypeTextField = new TextField();
    private final TextField prodField = new TextField();
    private final TextField milspaceTextField = new TextField();
    private final TextField milplanTextField = new TextField();

    private final CheckBox anomalyCheckbox = new CheckBox();
    private final CheckBox otherCheckbox = new CheckBox();

    ////////////////
    private final TextField misc1TextField = new TextField();
    private final TextField misc2TextField = new TextField();
    private final TextField misc3TextField = new TextField();
    private final TextField misc4TextField = new TextField();
    private final TextField misc5TextField = new TextField();

    private final TextField miscNum1TextField = new TextField();
    private final TextField miscNum2TextField = new TextField();
    private final TextField miscNum3TextField = new TextField();
    private final TextField miscNum4TextField = new TextField();
    private final TextField miscNum5TextField = new TextField();


    ////////////////

    public StarEditDialog(@NotNull StarObject record) {
        this.record = record;

        VBox vBox = new VBox();

        TabPane tabPane = new TabPane();

        Tab overviewTab = new Tab("Overview");
        overviewTab.setContent(createOverviewTab());
        tabPane.getTabs().add(overviewTab);

        Tab secondaryTab = new Tab("Secondary");
        secondaryTab.setContent(createSecondaryTab());
        tabPane.getTabs().add(secondaryTab);

        Tab fictionalTab = new Tab("Fictional");
        fictionalTab.setContent(createFictionalTab());
        tabPane.getTabs().add(fictionalTab);

        Tab userTab = new Tab("User Special");
        userTab.setContent(createUserTab());
        tabPane.getTabs().add(userTab);

        vBox.getChildren().add(tabPane);

        // setup button boxes
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        Button resetBtn = new Button("Cancel");
        resetBtn.setOnAction(this::cancelClicked);
        hBox.getChildren().add(resetBtn);
        Button addBtn = new Button("Change");
        addBtn.setOnAction(this::changeClicked);
        hBox.getChildren().add(addBtn);
        vBox.getChildren().add(hBox);

        this.setTitle("Change attributes for " + record.getDisplayName());
        this.getDialogPane().setContent(vBox);
    }

    private @NotNull Pane createUserTab() {
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setVgap(5);
        gridPane.setHgap(5);

        GridPane innerGridPane1 = new GridPane();
        innerGridPane1.setPadding(new Insets(10, 10, 10, 10));
        innerGridPane1.setVgap(5);
        innerGridPane1.setHgap(5);
        gridPane.add(innerGridPane1, 0, 1);

        innerGridPane1.add(new Label("misc1"), 0, 1);
        misc1TextField.setText(record.getMiscText1());
        innerGridPane1.add(misc1TextField, 1, 1);

        innerGridPane1.add(new Label("misc2"), 0, 2);
        misc2TextField.setText(record.getMiscText2());
        innerGridPane1.add(misc2TextField, 1, 2);

        innerGridPane1.add(new Label("misc3"), 0, 3);
        misc3TextField.setText(record.getMiscText3());
        innerGridPane1.add(misc3TextField, 1, 3);

        innerGridPane1.add(new Label("misc4"), 0, 4);
        misc4TextField.setText(record.getMiscText4());
        innerGridPane1.add(misc4TextField, 1, 4);

        innerGridPane1.add(new Label("misc5"), 0, 5);
        misc5TextField.setText(record.getMiscText5());
        innerGridPane1.add(misc5TextField, 1, 5);

        GridPane innerGridPane2 = new GridPane();
        innerGridPane2.setPadding(new Insets(10, 10, 10, 10));
        innerGridPane2.setVgap(5);
        innerGridPane2.setHgap(5);
        gridPane.add(innerGridPane2, 1, 1);

        innerGridPane2.add(new Label("miscNum1"), 0, 1);
        miscNum1TextField.setText(Double.toString(record.getMiscNum2()));
        miscNum1TextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkMiscNum1();
            }
        });
        innerGridPane2.add(miscNum1TextField, 1, 1);

        innerGridPane2.add(new Label("miscNum2"), 0, 2);
        miscNum2TextField.setText(Double.toString(record.getMiscNum2()));
        miscNum2TextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkMiscNum2();
            }
        });
        innerGridPane2.add(miscNum2TextField, 1, 2);

        innerGridPane2.add(new Label("miscNum3"), 0, 3);
        miscNum3TextField.setText(Double.toString(record.getMiscNum3()));
        miscNum3TextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkMiscNum3();
            }
        });
        innerGridPane2.add(miscNum3TextField, 1, 3);

        innerGridPane2.add(new Label("miscNum4"), 0, 4);
        miscNum4TextField.setText(Double.toString(record.getMiscNum4()));
        miscNum4TextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkMiscNum4();
            }
        });
        innerGridPane2.add(miscNum4TextField, 1, 4);

        innerGridPane2.add(new Label("miscNum5"), 0, 5);
        miscNum5TextField.setText(Double.toString(record.getMiscNum5()));
        miscNum5TextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkMiscNum5();
            }
        });
        innerGridPane2.add(miscNum5TextField, 1, 5);

        return gridPane;
    }

    private @NotNull Pane createFictionalTab() {
        // setup grid structure
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setVgap(5);
        gridPane.setHgap(5);

        gridPane.add(new Label("polity"), 0, 1);
        polityTextField.setText(record.getPolity());
        gridPane.add(polityTextField, 1, 1);

        gridPane.add(new Label("world type"), 0, 2);
        worldTypeTextField.setText(record.getWorldType());
        gridPane.add(worldTypeTextField, 1, 2);

        gridPane.add(new Label("fuel type"), 0, 3);
        fuelTypeTextField.setText(record.getFuelType());
        gridPane.add(fuelTypeTextField, 1, 3);

        gridPane.add(new Label("tech type"), 0, 4);
        techTypeTextField.setText(record.getTechType());
        gridPane.add(techTypeTextField, 1, 4);

        gridPane.add(new Label("port type"), 0, 5);
        portTypeTextField.setText(record.getPortType());
        gridPane.add(portTypeTextField, 1, 5);

        gridPane.add(new Label("population type"), 0, 6);
        popTypeTextField.setText(record.getPopulationType());
        gridPane.add(popTypeTextField, 1, 6);

        gridPane.add(new Label("product type"), 0, 7);
        prodField.setText(record.getProductType());
        gridPane.add(prodField, 1, 7);

        gridPane.add(new Label("milspace type"), 0, 8);
        milspaceTextField.setText(record.getMilSpaceType());
        gridPane.add(milspaceTextField, 1, 8);

        gridPane.add(new Label("milplan type"), 0, 9);
        milplanTextField.setText(record.getMilPlanType());
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

    private @NotNull Pane createSecondaryTab() {

        // setup grid structure
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setVgap(5);
        gridPane.setHgap(5);

        // items for right grid
        gridPane.add(new Label("ra"), 0, 1);
        raLabel.setText(Double.toString(record.getRa()));
        raLabel.setPromptText("right ascension, press enter");
        raLabel.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkRa();
            }
        });
        gridPane.add(raLabel, 1, 1);

        gridPane.add(new Label("pmra"), 0, 2);
        pmraLabel.setText(Double.toString(record.getPmra()));
        pmraLabel.setPromptText("PMRA, press enter");
        pmraLabel.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkPmra();
            }
        });
        gridPane.add(pmraLabel, 1, 2);

        gridPane.add(new Label("declination"), 0, 3);
        decLabel.setText(Double.toString(record.getDeclination()));
        decLabel.setPromptText("declination, press enter");
        decLabel.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkDeclination();
            }
        });
        gridPane.add(decLabel, 1, 3);

        gridPane.add(new Label("pmdec"), 0, 4);
        pmdecLabel.setText(Double.toString(record.getPmdec()));
        pmdecLabel.setPromptText("PMDEC, press enter");
        pmdecLabel.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkPmdec();
            }
        });
        gridPane.add(pmdecLabel, 1, 4);

        gridPane.add(new Label("Parallax"), 0, 7);
        parallaxLabel.setText(Double.toString(record.getParallax()));
        parallaxLabel.setPromptText("parallax, press enter");
        parallaxLabel.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkParallax();
            }
        });
        gridPane.add(parallaxLabel, 1, 7);

        gridPane.add(new Label("Radial velocity"), 0, 8);
        radialVelocityLabel.setText(Double.toString(record.getRadialVelocity()));
        radialVelocityLabel.setPromptText("radial velocity, press enter");
        radialVelocityLabel.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkRadialVel();
            }
        });
        gridPane.add(radialVelocityLabel, 1, 8);

        gridPane.add(new Label("bprp"), 0, 9);
        bprpLabel.setText(Double.toString(record.getBprp()));
        bprpLabel.setPromptText("bprp, press enter");
        bprpLabel.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkBprp();
            }
        });
        gridPane.add(bprpLabel, 1, 9);

        gridPane.add(new Label("bpg"), 0, 10);
        bpgLabel.setText(Double.toString(record.getBpg()));
        bpgLabel.setPromptText("bpg, press enter");
        bpgLabel.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkBpg();
            }
        });
        gridPane.add(bpgLabel, 1, 10);

        gridPane.add(new Label("grp"), 0, 11);
        grpLabel.setText(Double.toString(record.getGrp()));
        grpLabel.setPromptText("grp, press enter");
        grpLabel.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkGrp();
            }
        });
        gridPane.add(grpLabel, 1, 11);

        return gridPane;

    }

    private void checkMiscNum1() {
        try {
            double miscNum1 = Double.parseDouble(miscNum1TextField.getText());
            record.setMiscNum1(miscNum1);
        } catch (NumberFormatException nfe) {
            showErrorAlert("Edit Star Record", miscNum1TextField.getText() + " is an invalid floating point number");
        }
    }

    private void checkMiscNum2() {
        try {
            double miscNum2 = Double.parseDouble(miscNum2TextField.getText());
            record.setMiscNum2(miscNum2);
        } catch (NumberFormatException nfe) {
            showErrorAlert("Edit Star Record", miscNum2TextField.getText() + " is an invalid floating point number");
        }
    }

    private void checkMiscNum3() {
        try {
            double miscNum3 = Double.parseDouble(miscNum3TextField.getText());
            record.setMiscNum4(miscNum3);
        } catch (NumberFormatException nfe) {
            showErrorAlert("Edit Star Record", miscNum3TextField.getText() + " is an invalid floating point number");
        }
    }

    private void checkMiscNum4() {
        try {
            double miscNum4 = Double.parseDouble(miscNum4TextField.getText());
            record.setMiscNum4(miscNum4);
        } catch (NumberFormatException nfe) {
            showErrorAlert("Edit Star Record", miscNum4TextField.getText() + " is an invalid floating point number");
        }
    }

    private void checkMiscNum5() {
        try {
            double miscNum5 = Double.parseDouble(miscNum5TextField.getText());
            record.setMiscNum5(miscNum5);
        } catch (NumberFormatException nfe) {
            showErrorAlert("Edit Star Record", miscNum5TextField.getText() + " is an invalid floating point number");
        }
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

    private @NotNull Pane createOverviewTab() {

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
        gridPane.add(starNameTextField, 1, 3);

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
        distanceNameTextField.setPromptText("the distance from Sol in ly, press enter");
        gridPane.add(distanceNameTextField, 1, 6);

        gridPane.add(new Label("Spectral class"), 0, 7);
        spectralClassTextField.setText(record.getSpectralClass());
        spectralClassTextField.setPromptText(" the spectral class as in O, A, etc.");
        gridPane.add(spectralClassTextField, 1, 7);

        gridPane.add(new Label("Temperature"), 0, 8);
        tempTextField.setText(Double.toString(record.getTemperature()));
        tempTextField.setPromptText("temperature, press enter");
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
        xTextField.setPromptText("X coordinate, press enter");
        xTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkX();
            }
        });
        coordGrid.add(xTextField, 0, 1);
        yTextField.setText(Double.toString(record.getY()));
        yTextField.setPromptText("Y coordinate, press enter");
        yTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkY();
            }
        });
        coordGrid.add(yTextField, 1, 1);
        zTextField.setText(Double.toString(record.getZ()));
        zTextField.setPromptText("Z coordinate, press enter");
        zTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkZ();
            }
        });
        coordGrid.add(zTextField, 2, 1);

        gridPane.add(new Label("Notes"), 0, 10);
        notesArea.setText(record.getNotes());
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

    private void getData() {

        record.setDisplayName(starNameTextField.getText());
        record.setSpectralClass(spectralClassTextField.getText());
        record.setNotes(notesArea.getText());

        double radius = Double.parseDouble(radiusTextField.getText());
        record.setRadius(radius);

        double distance = Double.parseDouble(distanceNameTextField.getText());
        record.setDistance(distance);

        double temp = Double.parseDouble(tempTextField.getText());
        record.setTemperature(temp);

        double x = Double.parseDouble(xTextField.getText());
        record.setX(x);

        double y = Double.parseDouble(yTextField.getText());
        record.setY(y);

        double z = Double.parseDouble(zTextField.getText());
        record.setZ(z);

        record.setPolity(polityTextField.getText());
        record.setWorldType(worldTypeTextField.getText());
        record.setFuelType(fuelTypeTextField.getText());
        record.setTechType(techTypeTextField.getText());
        record.setPortType(portTypeTextField.getText());
        record.setPopulationType(popTypeTextField.getText());
        record.setProductType(prodField.getText());
        record.setMilSpaceType(milspaceTextField.getText());
        record.setMilPlanType(milspaceTextField.getText());

        double ra = Double.parseDouble(raLabel.getText());
        record.setRa(ra);

        double pmra = Double.parseDouble(pmraLabel.getText());
        record.setPmra(pmra);

        double dec = Double.parseDouble(decLabel.getText());
        record.setDeclination(dec);

        double pmdec = Double.parseDouble(pmdecLabel.getText());
        record.setPmdec(pmdec);

        double parallax = Double.parseDouble(parallaxLabel.getText());
        record.setParallax(parallax);

        double radialVelocity = Double.parseDouble(radialVelocityLabel.getText());
        record.setRadialVelocity(radialVelocity);

        double bprp = Double.parseDouble(bprpLabel.getText());
        record.setBprp(bprp);

        double bpg = Double.parseDouble(bpgLabel.getText());
        record.setBpg(bpg);

        double grp = Double.parseDouble(grpLabel.getText());
        record.setGrp(grp);

        record.setMiscText1(misc1TextField.getText());
        record.setMiscText2(misc2TextField.getText());
        record.setMiscText3(misc3TextField.getText());
        record.setMiscText4(misc4TextField.getText());
        record.setMiscText5(misc5TextField.getText());

        double miscNum1 = Double.parseDouble(miscNum1TextField.getText());
        record.setMiscNum1(miscNum1);

        double miscNum2 = Double.parseDouble(miscNum2TextField.getText());
        record.setMiscNum2(miscNum2);

        double miscNum3 = Double.parseDouble(miscNum3TextField.getText());
        record.setMiscNum3(miscNum3);

        double miscNum4 = Double.parseDouble(miscNum4TextField.getText());
        record.setMiscNum4(miscNum4);

        double miscNum5 = Double.parseDouble(miscNum5TextField.getText());
        record.setMiscNum5(miscNum5);


    }

    private void changeClicked(ActionEvent actionEvent) {
        try {
            getData();
            StarEditStatus starEditStatus = new StarEditStatus();
            starEditStatus.setRecord(record);
            starEditStatus.setChanged(true);
            setResult(starEditStatus);
        } catch (Exception e) {
            showErrorAlert("enter star data", "invalid floating point number entered");
        }
    }

    private void cancelClicked(ActionEvent actionEvent) {
        StarEditStatus starEditStatus = new StarEditStatus();
        starEditStatus.setChanged(false);
        setResult(starEditStatus);
    }

}
