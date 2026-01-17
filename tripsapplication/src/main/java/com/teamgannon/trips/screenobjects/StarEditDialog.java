package com.teamgannon.trips.screenobjects;

import com.teamgannon.trips.jpa.model.CivilizationDisplayPreferences;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.utility.SesameResolver;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogEvent;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;


@Slf4j
public class StarEditDialog extends Dialog<StarEditStatus> {

    private final @NotNull StarObject record;

    // Overview Info
    @FXML
    private Label recordIdLabel;
    @FXML
    private Label dataSetLabel;

    @FXML
    private TextField starNameTextField;
    @FXML
    private TextField commonNameTextField;
    @FXML
    private TextField constellationNameTextField;
    @FXML
    private TextField spectralClassTextField;
    @FXML
    private TextField distanceNameTextField;
    @FXML
    private TextField metallicityTextfield;
    @FXML
    private TextField ageTextfield;
    @FXML
    private TextField xTextField;
    @FXML
    private TextField yTextField;
    @FXML
    private TextField zTextField;
    @FXML
    private TextArea notesArea;

    ////////// Other Info

    @FXML
    private TextField simbadIdTextField;
    @FXML
    private TextField galacticCoorLatTextField;
    @FXML
    private TextField galacticCoorLongTextField;
    @FXML
    private TextField radiusTextField;
    @FXML
    private TextField massTextField;
    @FXML
    private TextField luminosityTextField;
    @FXML
    private TextField tempTextField;
    @FXML
    private TextField raLabel;
    @FXML
    private TextField decLabel;
    @FXML
    private TextField pmraLabel;
    @FXML
    private TextField pmdecLabel;
    @FXML
    private TextField parallaxLabel;
    @FXML
    private TextField radialVelocityLabel;
    @FXML
    private TextField bprpLabel;
    @FXML
    private TextField bpgLabel;
    @FXML
    private TextField grpLabel;

    @FXML
    private TextField maguTextField;
    @FXML
    private TextField magbTextField;
    @FXML
    private TextField magvTextField;
    @FXML
    private TextField magrTextField;
    @FXML
    private TextField magiTextField;

    @FXML
    private TextField gaiaIdTextField;

    @FXML
    private TextArea aliasTextArea;

    ///////// fictional Info
    @FXML
    private TextField polityTextField;
    @FXML
    private TextField worldTypeTextField;
    @FXML
    private TextField fuelTypeTextField;
    @FXML
    private TextField techTypeTextField;
    @FXML
    private TextField portTypeTextField;
    @FXML
    private TextField popTypeTextField;
    @FXML
    private TextField prodField;
    @FXML
    private TextField milspaceTextField;
    @FXML
    private TextField milplanTextField;

    @FXML
    private CheckBox anomalyCheckbox;
    @FXML
    private CheckBox otherCheckbox;

    //////////////// User Specific
    @FXML
    private TextField misc1TextField;
    @FXML
    private TextField misc2TextField;
    @FXML
    private TextField misc3TextField;
    @FXML
    private TextField misc4TextField;
    @FXML
    private TextField misc5TextField;

    @FXML
    private TextField miscNum1TextField;
    @FXML
    private TextField miscNum2TextField;
    @FXML
    private TextField miscNum3TextField;
    @FXML
    private TextField miscNum4TextField;
    @FXML
    private TextField miscNum5TextField;

    @FXML
    private ComboBox<String> politiesComboBox;
    @FXML
    private ComboBox<String> worldComboBox;
    @FXML
    private ComboBox<String> fuelComboBox;
    @FXML
    private ComboBox<String> techComboBox;
    @FXML
    private ComboBox<String> portComboBox;
    @FXML
    private ComboBox<String> populationComboBox;
    @FXML
    private ComboBox<String> productComboBox;
    @FXML
    private ComboBox<String> milSpaceComboBox;
    @FXML
    private ComboBox<String> milPlanComboBox;

    @FXML
    private CheckBox forceLabel;

    @FXML
    private Button updateAliasBtn;
    @FXML
    private Button resetBtn;
    @FXML
    private Button addBtn;


    ////////////////

    public StarEditDialog(@NotNull StarObject record) {
        this.record = record;

        FXMLLoader loader = new FXMLLoader(getClass().getResource("StarEditDialog.fxml"));
        loader.setController(this);
        Parent content;
        try {
            content = loader.load();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load StarEditDialog.fxml", ex);
        }
        getDialogPane().setContent(content);
        getDialogPane().getButtonTypes().clear();
        this.setTitle("Change attributes for " + record.getDisplayName());
        setOnCloseRequest(this::close);
    }

    @FXML
    private void initialize() {
        initializeOverviewTab();
        initializeFictionalTab();
        initializeSecondaryTab();
        initializeUserTab();
        updateAliasBtn.setOnAction(this::updateAliasList);
        resetBtn.setOnAction(this::cancelClicked);
        addBtn.setOnAction(this::changeClicked);
        anomalyCheckbox.setOnAction(event -> record.setAnomaly(anomalyCheckbox.isSelected()));
        otherCheckbox.setOnAction(event -> record.setOther(otherCheckbox.isSelected()));
        forceLabel.setSelected(record.isForceLabelToBeShown());
    }

    private void close(DialogEvent event) {
        StarEditStatus editStatus = new StarEditStatus();
        editStatus.setChanged(false);
        setResult(editStatus);
    }

    private void initializeOverviewTab() {
        recordIdLabel.setText(record.getId().toString());
        dataSetLabel.setText(record.getDataSetName());

        starNameTextField.setText(record.getDisplayName());
        commonNameTextField.setText(record.getCommonName());
        constellationNameTextField.setText(record.getConstellationName());

        spectralClassTextField.setText(record.getSpectralClass());
        spectralClassTextField.setPromptText(" the spectral class as in O, A, etc.");

        distanceNameTextField.setText(Double.toString(record.getDistance()));
        distanceNameTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkDistance();
            }
        });
        distanceNameTextField.setPromptText("the distance from Sol in ly, press enter");

        metallicityTextfield.setText(Double.toString(record.getMetallicity()));
        metallicityTextfield.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkMetallicity();
            }
        });
        metallicityTextfield.setPromptText("the metallicity, press enter");

        ageTextfield.setText(Double.toString(record.getAge()));
        ageTextfield.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkAge();
            }
        });
        ageTextfield.setPromptText("the age, press enter");

        xTextField.setText(Double.toString(record.getX()));
        xTextField.setPromptText("X coordinate, press enter");
        xTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkX();
            }
        });
        yTextField.setText(Double.toString(record.getY()));
        yTextField.setPromptText("Y coordinate, press enter");
        yTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkY();
            }
        });
        zTextField.setText(Double.toString(record.getZ()));
        zTextField.setPromptText("Z coordinate, press enter");
        zTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkZ();
            }
        });

        notesArea.setText(record.getNotes());
        notesArea.setPromptText("Enter a description or general notes on this star");
    }

    private void initializeFictionalTab() {
        fillCombos();

        polityTextField.setText(record.getPolity());

        worldTypeTextField.setText(record.getWorldType());

        fuelTypeTextField.setText(record.getFuelType());

        techTypeTextField.setText(record.getTechType());

        portTypeTextField.setText(record.getPortType());

        popTypeTextField.setText(record.getPopulationType());

        prodField.setText(record.getProductType());

        milspaceTextField.setText(record.getMilSpaceType());

        milplanTextField.setText(record.getMilPlanType());
        anomalyCheckbox.setSelected(record.isAnomaly());
        otherCheckbox.setSelected(record.isOther());

        selectCombo(politiesComboBox, record.getPolity());
        selectCombo(worldComboBox, record.getWorldType());
        selectCombo(fuelComboBox, record.getFuelType());
        selectCombo(techComboBox, record.getTechType());
        selectCombo(portComboBox, record.getPortType());
        selectCombo(populationComboBox, record.getPopulationType());
        selectCombo(productComboBox, record.getProductType());
        selectCombo(milSpaceComboBox, record.getMilSpaceType());
        selectCombo(milPlanComboBox, record.getMilPlanType());
    }

    private void fillCombos() {

        // polities
        politiesComboBox.getItems().addAll(
                CivilizationDisplayPreferences.TERRAN,
                CivilizationDisplayPreferences.DORNANI,
                CivilizationDisplayPreferences.KTOR,
                CivilizationDisplayPreferences.ARAKUR,
                CivilizationDisplayPreferences.HKHRKH,
                CivilizationDisplayPreferences.SLAASRIITHI,
                CivilizationDisplayPreferences.OTHER1,
                CivilizationDisplayPreferences.OTHER2,
                CivilizationDisplayPreferences.OTHER3,
                CivilizationDisplayPreferences.OTHER4,
                "NA");
        politiesComboBox.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> polityTextField.setText(newValue));
        politiesComboBox.getSelectionModel().select("NA");

        // fuel type
        fuelComboBox.getItems().addAll("H2", "Antimatter", "Gas Giant", "Water World", "NA");
        fuelComboBox.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> fuelTypeTextField.setText(newValue));
        fuelComboBox.getSelectionModel().select("NA");

        // world type
        worldComboBox.getItems().addAll("Green", "Grey", "Brown", "NA");
        worldComboBox.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> worldTypeTextField.setText(newValue));
        worldComboBox.getSelectionModel().select("NA");

        // world type
        portComboBox.getItems().addAll("A", "B", "C", "D", "E", "NA");
        portComboBox.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> portTypeTextField.setText(newValue));
        portComboBox.getSelectionModel().select("NA");

        // military space side type
        milSpaceComboBox.getItems().addAll("A", "B", "C", "D", "E", "NA");
        milSpaceComboBox.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> milspaceTextField.setText(newValue));
        milSpaceComboBox.getSelectionModel().select("NA");

        // military planet side type
        milPlanComboBox.getItems().addAll("A", "B", "C", "D", "E", "NA");
        milPlanComboBox.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> milplanTextField.setText(newValue));
        milPlanComboBox.getSelectionModel().select("NA");

        // military planet side type
        techComboBox.getItems().addAll("1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "NA");
        techComboBox.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> techTypeTextField.setText(newValue));
        techComboBox.getSelectionModel().select("NA");

        // military planet side type
        populationComboBox.getItems().addAll("1s", "10s", "100s", "1000s", "10\u2074", "10\u2075", "10\u2076", "10\u2077", "10\u2078", "10\u2079", "NA");
        populationComboBox.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> popTypeTextField.setText(newValue));
        populationComboBox.getSelectionModel().select("NA");

        // world type
        productComboBox.getItems().addAll("Agricultural", "Industry", "Services", "Raw Materials", "Bio", "Fossil Fuel", "Finished Goods", "Hi Tech", "Unique", "Energy (Nuke, AM or Fuel Cells \nat high export levels", "NA");
        productComboBox.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> prodField.setText(newValue));
        productComboBox.getSelectionModel().select("NA");
    }

    private void selectCombo(ComboBox<String> comboBox, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        if (comboBox.getItems().contains(value)) {
            comboBox.getSelectionModel().select(value);
        }
    }

    private void initializeSecondaryTab() {
        simbadIdTextField.setText(record.getSimbadId());
        simbadIdTextField.setPromptText("the Simbad Id");

        galacticCoorLatTextField.setText(Double.toString(record.getGalacticLat()));
        galacticCoorLatTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkGalacticLat();
            }
        });
        galacticCoorLatTextField.setPromptText("the galactic latitude");

        galacticCoorLongTextField.setText(Double.toString(record.getGalacticLong()));
        galacticCoorLongTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkGalacticLong();
            }
        });
        galacticCoorLongTextField.setPromptText("the galactic longitude");

        radiusTextField.setText(Double.toString(record.getRadius()));
        radiusTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkRadius();
            }
        });
        radiusTextField.setPromptText("the radius in Sol units");

        massTextField.setText(Double.toString(record.getMass()));
        massTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkMass();
            }
        });
        massTextField.setPromptText("the mass in Sol units");

        luminosityTextField.setText(record.getLuminosity() != null ? record.getLuminosity() : "");
        luminosityTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkLuminosity();
            }
        });
        luminosityTextField.setPromptText("luminosity value or class (e.g., 1.0 or V)");

        tempTextField.setText(Double.toString(record.getTemperature()));
        tempTextField.setPromptText("temperature, press enter");
        tempTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkTemp();
            }
        });
        tempTextField.setPromptText("the surface temperature of the star");

        // RA
        raLabel.setText(Double.toString(record.getRa()));
        raLabel.setPromptText("right ascension, press enter");
        raLabel.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkRa();
            }
        });

        decLabel.setText(Double.toString(record.getDeclination()));
        decLabel.setPromptText("declination, press enter");
        decLabel.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkDeclination();
            }
        });

        pmraLabel.setText(Double.toString(record.getPmra()));
        pmraLabel.setPromptText("PMRA, press enter");
        pmraLabel.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkPmra();
            }
        });

        pmdecLabel.setText(Double.toString(record.getPmdec()));
        pmdecLabel.setPromptText("PMDEC, press enter");
        pmdecLabel.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkPmdec();
            }
        });

        parallaxLabel.setText(Double.toString(record.getParallax()));
        parallaxLabel.setPromptText("parallax, press enter");
        parallaxLabel.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkParallax();
            }
        });

        radialVelocityLabel.setText(Double.toString(record.getRadialVelocity()));
        radialVelocityLabel.setPromptText("radial velocity, press enter");
        radialVelocityLabel.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkRadialVel();
            }
        });

        bprpLabel.setText(Double.toString(record.getBprp()));
        bprpLabel.setPromptText("bprp, press enter");
        bprpLabel.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkBprp();
            }
        });

        bpgLabel.setText(Double.toString(record.getBpg()));
        bpgLabel.setPromptText("bpg, press enter");
        bpgLabel.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkBpg();
            }
        });

        grpLabel.setText(Double.toString(record.getGrp()));
        grpLabel.setPromptText("grp, press enter");
        grpLabel.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkGrp();
            }
        });

        maguTextField.setText(Double.toString(record.getMagu()));
        maguTextField.setPromptText("magu, press enter");
        maguTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkMagu();
            }
        });

        magbTextField.setText(Double.toString(record.getMagb()));
        magbTextField.setPromptText("magb, press enter");
        magbTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkMagb();
            }
        });

        magvTextField.setText(Double.toString(record.getMagv()));
        magvTextField.setPromptText("magv, press enter");
        magvTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkMagv();
            }
        });

        magrTextField.setText(Double.toString(record.getMagr()));
        magrTextField.setPromptText("magr, press enter");
        magrTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkMagr();
            }
        });

        magiTextField.setText(Double.toString(record.getMagi()));
        magiTextField.setPromptText("magi, press enter");
        magiTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkMagi();
            }
        });

        gaiaIdTextField.setText(record.getGaiaDR2CatId());

        if (!record.getAliasList().isEmpty()) {
            aliasTextArea.setText(String.join(", ", record.getAliasList()));
        }
    }

    private void updateAliasList(ActionEvent actionEvent) {
        SesameResolver resolver = new SesameResolver();
        List<String> aliasList = resolver.findAliases(record.getDisplayName());
        aliasTextArea.setText(String.join(", ", aliasList));
        record.getAliasList().addAll(aliasList);
        log.info("record updated");
    }

    private void checkMagi() {
        try {
            double value = Double.parseDouble(magiTextField.getText());
            record.setMagi(value);
        } catch (NumberFormatException nfe) {
            showErrorAlert("Edit Star Record", magiTextField.getText() + " is an invalid floating point number");
        }
    }

    private void checkMagr() {
        try {
            double value = Double.parseDouble(magrTextField.getText());
            record.setMagr(value);
        } catch (NumberFormatException nfe) {
            showErrorAlert("Edit Star Record", magrTextField.getText() + " is an invalid floating point number");
        }
    }

    private void checkMagv() {
        try {
            double value = Double.parseDouble(magvTextField.getText());
            record.setMagv(value);
        } catch (NumberFormatException nfe) {
            showErrorAlert("Edit Star Record", magvTextField.getText() + " is an invalid floating point number");
        }
    }

    private void checkMagb() {
        try {
            double value = Double.parseDouble(magbTextField.getText());
            record.setMagb(value);
        } catch (NumberFormatException nfe) {
            showErrorAlert("Edit Star Record", magbTextField.getText() + " is an invalid floating point number");
        }
    }

    private void checkMagu() {
        try {
            double value = Double.parseDouble(maguTextField.getText());
            record.setMagu(value);
        } catch (NumberFormatException nfe) {
            showErrorAlert("Edit Star Record", maguTextField.getText() + " is an invalid floating point number");
        }
    }

    private void checkGalacticLong() {
        try {
            double galLong = Double.parseDouble(galacticCoorLongTextField.getText());
            record.setGalacticLong(galLong);
        } catch (NumberFormatException nfe) {
            showErrorAlert("Edit Star Record", galacticCoorLongTextField.getText() + " is an invalid floating point number");
        }
    }

    private void checkGalacticLat() {
        try {
            double galLat = Double.parseDouble(galacticCoorLatTextField.getText());
            record.setGalacticLat(galLat);
        } catch (NumberFormatException nfe) {
            showErrorAlert("Edit Star Record", galacticCoorLatTextField.getText() + " is an invalid floating point number");
        }
    }

    private void initializeUserTab() {
        misc1TextField.setText(record.getMiscText1());

        misc2TextField.setText(record.getMiscText2());

        misc3TextField.setText(record.getMiscText3());

        misc4TextField.setText(record.getMiscText4());

        misc5TextField.setText(record.getMiscText5());

        miscNum1TextField.setText(Double.toString(record.getMiscNum1()));
        miscNum1TextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkMiscNum1();
            }
        });

        miscNum2TextField.setText(Double.toString(record.getMiscNum2()));
        miscNum2TextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkMiscNum2();
            }
        });

        miscNum3TextField.setText(Double.toString(record.getMiscNum3()));
        miscNum3TextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkMiscNum3();
            }
        });

        miscNum4TextField.setText(Double.toString(record.getMiscNum4()));
        miscNum4TextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkMiscNum4();
            }
        });

        miscNum5TextField.setText(Double.toString(record.getMiscNum5()));
        miscNum5TextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkMiscNum5();
            }
        });
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
        record.setMilPlanType(milplanTextField.getText());
    }

    private void checkAge() {
        try {
            double age = Double.parseDouble(ageTextfield.getText());
            record.setAge(age);
        } catch (NumberFormatException nfe) {
            showErrorAlert("Edit Star Record", ageTextfield.getText() + " is an invalid floating point number");
        }
    }

    private void checkMetallicity() {
        try {
            double metallicity = Double.parseDouble(metallicityTextfield.getText());
            record.setMetallicity(metallicity);
        } catch (NumberFormatException nfe) {
            showErrorAlert("Edit Star Record", metallicityTextfield.getText() + " is an invalid floating point number");
        }
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
            record.setMiscNum3(miscNum3);
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


    private void checkRadius() {
        try {
            double radius = Double.parseDouble(radiusTextField.getText());
            record.setRadius(radius);
        } catch (NumberFormatException nfe) {
            showErrorAlert("Edit Star Record", radiusTextField.getText() + " is an invalid floating point number");
        }
    }

    private void checkMass() {
        try {
            double mass = Double.parseDouble(massTextField.getText());
            record.setMass(mass);
        } catch (NumberFormatException nfe) {
            showErrorAlert("Edit Star Record", massTextField.getText() + " is an invalid floating point number");
        }
    }

    private void checkLuminosity() {
        // Luminosity can be a numeric value (e.g., "1.0") or luminosity class (e.g., "V")
        record.setLuminosity(luminosityTextField.getText());
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
        record.setConstellationName(constellationNameTextField.getText());
        record.setSpectralClass(spectralClassTextField.getText());
        record.setNotes(notesArea.getText());

        double radius = Double.parseDouble(radiusTextField.getText());
        record.setRadius(radius);

        double mass = Double.parseDouble(massTextField.getText());
        record.setMass(mass);

        record.setLuminosity(luminosityTextField.getText());

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
        record.setMilPlanType(milplanTextField.getText());

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

        record.setSimbadId(simbadIdTextField.getText());
        record.setCommonName(commonNameTextField.getText());

        double metallicity = Double.parseDouble(metallicityTextfield.getText());
        record.setMetallicity(metallicity);

        double age = Double.parseDouble(ageTextfield.getText());
        record.setAge(age);

        double galacticLat = Double.parseDouble(galacticCoorLatTextField.getText());
        record.setGalacticLat(galacticLat);

        double galacticLong = Double.parseDouble(galacticCoorLongTextField.getText());
        record.setGalacticLong(galacticLong);

        double magu = Double.parseDouble(maguTextField.getText());
        record.setMagu(magu);

        double magb = Double.parseDouble(magbTextField.getText());
        record.setMagb(magb);

        double magv = Double.parseDouble(magvTextField.getText());
        record.setMagv(magv);

        double magr = Double.parseDouble(magrTextField.getText());
        record.setMagr(magr);

        double magi = Double.parseDouble(magiTextField.getText());
        record.setMagi(magi);

        record.setMiscText1(misc1TextField.getText());
        record.setMiscText2(misc2TextField.getText());
        record.setMiscText3(misc3TextField.getText());
        record.setMiscText4(misc4TextField.getText());
        record.setMiscText5(misc5TextField.getText());

        record.setGaiaDR2CatId(gaiaIdTextField.getText());

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

        record.setForceLabelToBeShown(forceLabel.isSelected());

    }

    private void changeClicked(ActionEvent actionEvent) {
        try {
            getData();

            // Validate critical stellar parameters for solar system generation
            String validationWarning = validateStellarParameters();
            if (validationWarning != null) {
                // Show warning but allow save - user may want to fix later
                showWarningAlert("Incomplete Stellar Data", validationWarning);
            }

            StarEditStatus starEditStatus = new StarEditStatus();
            starEditStatus.setRecord(record);
            starEditStatus.setChanged(true);
            setResult(starEditStatus);
        } catch (Exception e) {
            showErrorAlert("enter star data", "invalid floating point number entered");
        }
    }

    /**
     * Validates that critical stellar parameters are present for solar system generation.
     * Returns a warning message if any are missing/zero, or null if all are valid.
     */
    private String validateStellarParameters() {
        StringBuilder issues = new StringBuilder();

        if (record.getMass() <= 0) {
            issues.append("• Mass is missing or zero\n");
        }
        if (record.getRadius() <= 0) {
            issues.append("• Radius is missing or zero\n");
        }
        if (record.getTemperature() <= 0) {
            issues.append("• Temperature is missing or zero\n");
        }

        // Check luminosity - it's a String that should parse to a positive number
        String lumStr = record.getLuminosity();
        if (lumStr == null || lumStr.isBlank()) {
            issues.append("• Luminosity is missing\n");
        } else {
            try {
                double lum = Double.parseDouble(lumStr.trim());
                if (lum <= 0) {
                    issues.append("• Luminosity is zero or negative\n");
                }
            } catch (NumberFormatException e) {
                // It might be a luminosity class (e.g., "V") which is fine
            }
        }

        if (issues.length() > 0) {
            return "The following stellar parameters are missing or invalid:\n\n" +
                   issues +
                   "\nSolar system generation may use default values (Sun-like) for missing data.";
        }
        return null;
    }

    private void showWarningAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void cancelClicked(ActionEvent actionEvent) {
        StarEditStatus starEditStatus = new StarEditStatus();
        starEditStatus.setChanged(false);
        setResult(starEditStatus);
    }

}
