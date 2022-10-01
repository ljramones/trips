package com.teamgannon.trips.screenobjects;

import com.teamgannon.trips.jpa.model.CivilizationDisplayPreferences;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.utility.SesameResolver;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;


@Slf4j
public class StarEditDialog extends Dialog<StarEditStatus> {

    private final @NotNull StarObject record;

    // Overview Info
    private final Label recordIdLabel = new Label();
    private final Label dataSetLabel = new Label();

    private final TextField starNameTextField = new TextField();
    private final TextField commonNameTextField = new TextField();
    private final TextField constellationNameTextField = new TextField();
    private final TextField spectralClassTextField = new TextField();
    private final TextField distanceNameTextField = new TextField();
    private final TextField metallicityTextfield = new TextField();
    private final TextField ageTextfield = new TextField();
    private final TextField xTextField = new TextField();
    private final TextField yTextField = new TextField();
    private final TextField zTextField = new TextField();
    private final TextArea notesArea = new TextArea();

    ////////// Other Info

    private final TextField simbadIdTextField = new TextField();
    private final TextField galacticCoorLatTextField = new TextField();
    private final TextField galacticCoorLongTextField = new TextField();
    private final TextField radiusTextField = new TextField();
    private final TextField tempTextField = new TextField();
    private final TextField raLabel = new TextField();
    private final TextField decLabel = new TextField();
    private final TextField pmraLabel = new TextField();
    private final TextField pmdecLabel = new TextField();
    private final TextField parallaxLabel = new TextField();
    private final TextField radialVelocityLabel = new TextField();
    private final TextField bprpLabel = new TextField();
    private final TextField bpgLabel = new TextField();
    private final TextField grpLabel = new TextField();

    private final TextField maguTextField = new TextField();
    private final TextField magbTextField = new TextField();
    private final TextField magvTextField = new TextField();
    private final TextField magrTextField = new TextField();
    private final TextField magiTextField = new TextField();

    private final TextField gaiaIdTextField = new TextField();

    private final TextArea aliasTextArea = new TextArea();

    ///////// fictional Info
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

    //////////////// User Specific
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

    private final ComboBox<String> politiesComboBox = new ComboBox<>();
    private final ComboBox<String> worldComboBox = new ComboBox<>();
    private final ComboBox<String> fuelComboBox = new ComboBox<>();
    private final ComboBox<String> techComboBox = new ComboBox<>();
    private final ComboBox<String> portComboBox = new ComboBox<>();
    private final ComboBox<String> populationComboBox = new ComboBox<>();
    private final ComboBox<String> productComboBox = new ComboBox<>();
    private final ComboBox<String> milSpaceComboBox = new ComboBox<>();
    private final ComboBox<String> milPlanComboBox = new ComboBox<>();

    private final CheckBox forceLabel = new CheckBox("Force Label to be seen");


    ////////////////

    public StarEditDialog(@NotNull StarObject record) {
        this.record = record;

        // set the dialog as a utility
        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        stage.setOnCloseRequest(this::close);

        VBox vBox = new VBox();

        TabPane tabPane = new TabPane();

        Tab overviewTab = new Tab("Overview");
        overviewTab.setContent(createOverviewTab());
        tabPane.getTabs().add(overviewTab);

        Tab fictionalTab = new Tab("Fictional Info");
        fictionalTab.setContent(createFictionalTab());
        tabPane.getTabs().add(fictionalTab);

        Tab secondaryTab = new Tab("Other Info");
        secondaryTab.setContent(createSecondaryTab());
        tabPane.getTabs().add(secondaryTab);

        Tab userTab = new Tab("User Special Info");
        userTab.setContent(createUserTab());
        tabPane.getTabs().add(userTab);

        vBox.getChildren().add(tabPane);

        // setup button boxes
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);

        Button resetBtn = new Button("Cancel");
        resetBtn.setOnAction(this::cancelClicked);
        hBox.getChildren().add(resetBtn);

        Button addBtn = new Button("Update");
        addBtn.setOnAction(this::changeClicked);
        hBox.getChildren().add(addBtn);

        vBox.getChildren().add(hBox);

        this.setTitle("Change attributes for " + record.getDisplayName());
        this.getDialogPane().setContent(vBox);
    }

    private void close(WindowEvent windowEvent) {
        StarEditStatus editStatus = new StarEditStatus();
        editStatus.setChanged(false);
        setResult(editStatus);
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

        Label datasetLabel = new Label("Data set name");
        datasetLabel.setMinWidth(80);
        gridPane.add(datasetLabel, 0, 2);
        dataSetLabel.setText(record.getDataSetName());
        gridPane.add(dataSetLabel, 1, 2);

        //////////////////

        // star name
        gridPane.add(new Label("Star name"), 0, 3);
        starNameTextField.setText(record.getDisplayName());
        gridPane.add(starNameTextField, 1, 3);

        // common name
        gridPane.add(new Label("Common name"), 0, 4);
        commonNameTextField.setText(record.getCommonName());
        gridPane.add(commonNameTextField, 1, 4);

        // constellation name
        gridPane.add(new Label("Constellation name"), 0, 5);
        constellationNameTextField.setText(record.getConstellationName());
        gridPane.add(constellationNameTextField, 1, 5);

        // spectral class
        gridPane.add(new Label("Spectral class"), 0, 6);
        spectralClassTextField.setText(record.getSpectralClass());
        spectralClassTextField.setPromptText(" the spectral class as in O, A, etc.");
        gridPane.add(spectralClassTextField, 1, 6);

        // distance
        gridPane.add(new Label("Distance"), 0, 7);
        distanceNameTextField.setText(Double.toString(record.getDistance()));
        distanceNameTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkDistance();
            }
        });
        distanceNameTextField.setPromptText("the distance from Sol in ly, press enter");
        gridPane.add(distanceNameTextField, 1, 7);

        // metallicity
        gridPane.add(new Label("Metallicity"), 0, 8);
        metallicityTextfield.setText(Double.toString(record.getMetallicity()));
        metallicityTextfield.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkMetallicity();
            }
        });
        metallicityTextfield.setPromptText("the metallicity, press enter");
        gridPane.add(metallicityTextfield, 1, 8);

        // age
        gridPane.add(new Label("Age"), 0, 9);
        ageTextfield.setText(Double.toString(record.getDistance()));
        ageTextfield.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkAge();
            }
        });
        ageTextfield.setPromptText("the age, press enter");
        gridPane.add(ageTextfield, 1, 9);

        // coordinates
        gridPane.add(new Label("Coordinates"), 0, 10);
        GridPane coordGrid = new GridPane();
        gridPane.add(coordGrid, 1, 10);
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

        // notes
        gridPane.add(new Label("Notes"), 0, 11);
        notesArea.setText(record.getNotes());
        notesArea.setPromptText("Enter a description or general notes on this star");
        gridPane.add(notesArea, 1, 11, 1, 3);

        return gridPane;
    }

    private @NotNull Pane createFictionalTab() {

        fillCombos();

        // setup grid structure
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setVgap(5);
        gridPane.setHgap(5);

        // polity
        gridPane.add(new Label("Polity"), 0, 1);
        polityTextField.setText(record.getPolity());
        gridPane.add(polityTextField, 1, 1);
        gridPane.add(politiesComboBox, 2, 1);

        // world type
        gridPane.add(new Label("World Type"), 0, 2);
        worldTypeTextField.setText(record.getWorldType());
        gridPane.add(worldTypeTextField, 1, 2);
        gridPane.add(worldComboBox, 2, 2);

        // fuel type
        gridPane.add(new Label("Fuel Type"), 0, 3);
        fuelTypeTextField.setText(record.getFuelType());
        gridPane.add(fuelTypeTextField, 1, 3);
        gridPane.add(fuelComboBox, 2, 3);

        // tech type
        gridPane.add(new Label("Tech Type"), 0, 4);
        techTypeTextField.setText(record.getTechType());
        gridPane.add(techTypeTextField, 1, 4);
        gridPane.add(techComboBox, 2, 4);

        // port type
        gridPane.add(new Label("Port Type"), 0, 5);
        portTypeTextField.setText(record.getPortType());
        gridPane.add(portTypeTextField, 1, 5);
        gridPane.add(portComboBox, 2, 5);

        // population
        gridPane.add(new Label("Population Type"), 0, 6);
        popTypeTextField.setText(record.getPopulationType());
        gridPane.add(popTypeTextField, 1, 6);
        gridPane.add(populationComboBox, 2, 6);

        // product type
        gridPane.add(new Label("Product Type"), 0, 7);
        prodField.setText(record.getProductType());
        gridPane.add(prodField, 1, 7);
        gridPane.add(productComboBox, 2, 7);

        // milspace type
        gridPane.add(new Label("Milspace Type"), 0, 8);
        milspaceTextField.setText(record.getMilSpaceType());
        gridPane.add(milspaceTextField, 1, 8);
        gridPane.add(milSpaceComboBox, 2, 8);

        // milplan type
        gridPane.add(new Label("Milplan Type"), 0, 9);
        milplanTextField.setText(record.getMilPlanType());
        gridPane.add(milplanTextField, 1, 9);
        gridPane.add(milPlanComboBox, 2, 9);

        // anomaly
        gridPane.add(new Label("Anomaly"), 0, 10);
        anomalyCheckbox.setSelected(record.isAnomaly());
        anomalyCheckbox.setOnAction(event -> record.setAnomaly(anomalyCheckbox.isSelected()));
        gridPane.add(anomalyCheckbox, 1, 10);

        // other
        gridPane.add(new Label("Other"), 0, 11);
        otherCheckbox.setSelected(record.isOther());
        otherCheckbox.setOnAction(event -> record.setOther(otherCheckbox.isSelected()));
        gridPane.add(otherCheckbox, 1, 11);

        return gridPane;
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

    private @NotNull Pane createSecondaryTab() {

        // setup grid structure
        GridPane mGridPane = new GridPane();
        mGridPane.setPadding(new Insets(10, 10, 10, 10));
        mGridPane.setVgap(5);
        mGridPane.setHgap(5);

        GridPane leftGrid = new GridPane();
        leftGrid.setPadding(new Insets(10, 10, 10, 10));
        leftGrid.setVgap(5);
        leftGrid.setHgap(5);
        mGridPane.add(leftGrid, 0, 1);

        GridPane rightGrid = new GridPane();
        rightGrid.setPadding(new Insets(10, 10, 10, 10));
        rightGrid.setVgap(5);
        rightGrid.setHgap(5);
        mGridPane.add(rightGrid, 1, 1);

        // simbad id
        leftGrid.add(new Label("Simbad Id"), 0, 2);
        simbadIdTextField.setText(record.getSimbadId());
        simbadIdTextField.setPromptText("the Simbad Id");
        leftGrid.add(simbadIdTextField, 1, 2);

        // galactic lat
        leftGrid.add(new Label("Galactic Lat"), 0, 3);
        galacticCoorLatTextField.setText(Double.toString(record.getGalacticLat()));
        galacticCoorLatTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkGalacticLat();
            }
        });
        galacticCoorLatTextField.setPromptText("the galactic latitude");
        leftGrid.add(galacticCoorLatTextField, 1, 3);

        // galactic long
        leftGrid.add(new Label("Galactic Long"), 0, 4);
        galacticCoorLongTextField.setText(Double.toString(record.getGalacticLong()));
        galacticCoorLongTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkGalacticLong();
            }
        });
        galacticCoorLongTextField.setPromptText("the galactic longitude");
        leftGrid.add(galacticCoorLongTextField, 1, 4);

        // radius
        leftGrid.add(new Label("Radius"), 0, 5);
        radiusTextField.setText(Double.toString(record.getRadius()));
        radiusTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkRadius();
            }
        });
        radiusTextField.setPromptText("the radius in Sol units");
        leftGrid.add(radiusTextField, 1, 5);

        // temperature
        leftGrid.add(new Label("Temperature"), 0, 6);
        tempTextField.setText(Double.toString(record.getTemperature()));
        tempTextField.setPromptText("temperature, press enter");
        tempTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkTemp();
            }
        });
        tempTextField.setPromptText("the surface temperature of the star");
        leftGrid.add(tempTextField, 1, 6);


        // items for right grid
        // RA
        leftGrid.add(new Label("ra"), 0, 7);
        raLabel.setText(Double.toString(record.getRa()));
        raLabel.setPromptText("right ascension, press enter");
        raLabel.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkRa();
            }
        });
        leftGrid.add(raLabel, 1, 7);

        // declination
        leftGrid.add(new Label("Declination"), 0, 8);
        decLabel.setText(Double.toString(record.getDeclination()));
        decLabel.setPromptText("declination, press enter");
        decLabel.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkDeclination();
            }
        });
        leftGrid.add(decLabel, 1, 8);

        // PMRA
        leftGrid.add(new Label("Pmra"), 0, 9);
        pmraLabel.setText(Double.toString(record.getPmra()));
        pmraLabel.setPromptText("PMRA, press enter");
        pmraLabel.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkPmra();
            }
        });
        leftGrid.add(pmraLabel, 1, 9);

        // PMDEC
        leftGrid.add(new Label("pmdec"), 0, 10);
        pmdecLabel.setText(Double.toString(record.getPmdec()));
        pmdecLabel.setPromptText("PMDEC, press enter");
        pmdecLabel.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkPmdec();
            }
        });
        leftGrid.add(pmdecLabel, 1, 10);

        // parallax
        leftGrid.add(new Label("Parallax"), 0, 11);
        parallaxLabel.setText(Double.toString(record.getParallax()));
        parallaxLabel.setPromptText("parallax, press enter");
        parallaxLabel.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkParallax();
            }
        });
        leftGrid.add(parallaxLabel, 1, 11);

        // radial velocity
        leftGrid.add(new Label("Radial velocity"), 0, 12);
        radialVelocityLabel.setText(Double.toString(record.getRadialVelocity()));
        radialVelocityLabel.setPromptText("radial velocity, press enter");
        radialVelocityLabel.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkRadialVel();
            }
        });
        leftGrid.add(radialVelocityLabel, 1, 12);

        Button updateAliasBtn = new Button("Update Alias List");
        updateAliasBtn.setOnAction(this::updateAliasList);
        leftGrid.add(updateAliasBtn, 0, 13, 2, 1);

        ////////////////

        // bprp
        rightGrid.add(new Label("bprp"), 0, 1);
        bprpLabel.setText(Double.toString(record.getBprp()));
        bprpLabel.setPromptText("bprp, press enter");
        bprpLabel.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkBprp();
            }
        });
        rightGrid.add(bprpLabel, 1, 1);

        // brp
        rightGrid.add(new Label("bpg"), 0, 2);
        bpgLabel.setText(Double.toString(record.getBpg()));
        bpgLabel.setPromptText("bpg, press enter");
        bpgLabel.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkBpg();
            }
        });
        rightGrid.add(bpgLabel, 1, 2);

        // grp
        rightGrid.add(new Label("grp"), 0, 3);
        grpLabel.setText(Double.toString(record.getGrp()));
        grpLabel.setPromptText("grp, press enter");
        grpLabel.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkGrp();
            }
        });
        rightGrid.add(grpLabel, 1, 3);

        // magu
        rightGrid.add(new Label("magu"), 0, 4);
        maguTextField.setText(Double.toString(record.getMagu()));
        maguTextField.setPromptText("magu, press enter");
        maguTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkMagu();
            }
        });
        rightGrid.add(maguTextField, 1, 4);

        // magb
        rightGrid.add(new Label("magb"), 0, 5);
        magbTextField.setText(Double.toString(record.getMagb()));
        magbTextField.setPromptText("magb, press enter");
        magbTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkMagb();
            }
        });
        rightGrid.add(magbTextField, 1, 5);

        // magv
        rightGrid.add(new Label("magv"), 0, 6);
        magvTextField.setText(Double.toString(record.getMagv()));
        magvTextField.setPromptText("magv, press enter");
        magvTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkMagv();
            }
        });
        rightGrid.add(magvTextField, 1, 6);

        // magr
        rightGrid.add(new Label("magr"), 0, 7);
        magrTextField.setText(Double.toString(record.getMagr()));
        magrTextField.setPromptText("magr, press enter");
        magrTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkMagr();
            }
        });
        rightGrid.add(magrTextField, 1, 7);

        // magi
        rightGrid.add(new Label("magi"), 0, 8);
        magiTextField.setText(Double.toString(record.getMagi()));
        magiTextField.setPromptText("magi, press enter");
        magiTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                checkMagi();
            }
        });
        rightGrid.add(magiTextField, 1, 8);

        rightGrid.add(new Label("GAIA Id"), 0, 9);
        gaiaIdTextField.setText(record.getGaiaId());
        rightGrid.add(gaiaIdTextField, 1, 9);

        // the alias list for the star
        rightGrid.add(new Label("Alias list"), 0, 10);
        rightGrid.add(aliasTextArea, 1, 10);
        if (!record.getAliasList().isEmpty()) {
            aliasTextArea.setText(String.join(", ", record.getAliasList()));
        }


        return mGridPane;
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

        forceLabel.setSelected(record.isForceLabelToBeShown());
        innerGridPane1.add(forceLabel, 0, 6, 2, 1);

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

    private void checkAge() {
        try {
            double age = Double.parseDouble(ageTextfield.getText());
            record.setMetallicity(age);
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

        record.setGaiaId(gaiaIdTextField.getText());

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
