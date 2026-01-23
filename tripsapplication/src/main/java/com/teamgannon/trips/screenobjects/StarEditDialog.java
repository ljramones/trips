package com.teamgannon.trips.screenobjects;

import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.utility.SesameResolver;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;


/**
 * Dialog for editing star attributes.
 * Uses StarEditFormBinder for data binding and StarEditComboConfig for combo box configuration.
 */
@Slf4j
public class StarEditDialog extends Dialog<StarEditStatus> {

    private final @NotNull StarObject record;
    private final StarEditFormBinder formBinder;

    // Overview Info
    @FXML private Label recordIdLabel;
    @FXML private Label dataSetLabel;
    @FXML private TextField starNameTextField;
    @FXML private TextField commonNameTextField;
    @FXML private TextField constellationNameTextField;
    @FXML private TextField spectralClassTextField;
    @FXML private TextField distanceNameTextField;
    @FXML private TextField metallicityTextfield;
    @FXML private TextField ageTextfield;
    @FXML private TextField xTextField;
    @FXML private TextField yTextField;
    @FXML private TextField zTextField;
    @FXML private TextArea notesArea;

    // Secondary/Scientific Info
    @FXML private TextField simbadIdTextField;
    @FXML private TextField galacticCoorLatTextField;
    @FXML private TextField galacticCoorLongTextField;
    @FXML private TextField radiusTextField;
    @FXML private TextField massTextField;
    @FXML private TextField luminosityTextField;
    @FXML private TextField tempTextField;
    @FXML private TextField raLabel;
    @FXML private TextField decLabel;
    @FXML private TextField pmraLabel;
    @FXML private TextField pmdecLabel;
    @FXML private TextField parallaxLabel;
    @FXML private TextField radialVelocityLabel;
    @FXML private TextField bprpLabel;
    @FXML private TextField bpgLabel;
    @FXML private TextField grpLabel;
    @FXML private TextField maguTextField;
    @FXML private TextField magbTextField;
    @FXML private TextField magvTextField;
    @FXML private TextField magrTextField;
    @FXML private TextField magiTextField;
    @FXML private TextField gaiaIdTextField;
    @FXML private TextArea aliasTextArea;

    // Fictional Info
    @FXML private TextField polityTextField;
    @FXML private TextField worldTypeTextField;
    @FXML private TextField fuelTypeTextField;
    @FXML private TextField techTypeTextField;
    @FXML private TextField portTypeTextField;
    @FXML private TextField popTypeTextField;
    @FXML private TextField prodField;
    @FXML private TextField milspaceTextField;
    @FXML private TextField milplanTextField;
    @FXML private CheckBox anomalyCheckbox;
    @FXML private CheckBox otherCheckbox;

    // Combo boxes
    @FXML private ComboBox<String> politiesComboBox;
    @FXML private ComboBox<String> worldComboBox;
    @FXML private ComboBox<String> fuelComboBox;
    @FXML private ComboBox<String> techComboBox;
    @FXML private ComboBox<String> portComboBox;
    @FXML private ComboBox<String> populationComboBox;
    @FXML private ComboBox<String> productComboBox;
    @FXML private ComboBox<String> milSpaceComboBox;
    @FXML private ComboBox<String> milPlanComboBox;

    // User fields
    @FXML private TextField misc1TextField;
    @FXML private TextField misc2TextField;
    @FXML private TextField misc3TextField;
    @FXML private TextField misc4TextField;
    @FXML private TextField misc5TextField;
    @FXML private TextField miscNum1TextField;
    @FXML private TextField miscNum2TextField;
    @FXML private TextField miscNum3TextField;
    @FXML private TextField miscNum4TextField;
    @FXML private TextField miscNum5TextField;

    @FXML private CheckBox forceLabel;
    @FXML private Button updateAliasBtn;
    @FXML private Button resetBtn;
    @FXML private Button addBtn;

    public StarEditDialog(@NotNull StarObject record) {
        this.record = record;
        this.formBinder = new StarEditFormBinder(record);

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
        // Register fields with the form binder
        formBinder.setOverviewFields(starNameTextField, commonNameTextField, constellationNameTextField,
                spectralClassTextField, distanceNameTextField, metallicityTextfield,
                ageTextfield, xTextField, yTextField, zTextField, notesArea);

        formBinder.setSecondaryFields(simbadIdTextField, galacticCoorLatTextField, galacticCoorLongTextField,
                radiusTextField, massTextField, luminosityTextField, tempTextField,
                raLabel, decLabel, pmraLabel, pmdecLabel, parallaxLabel,
                radialVelocityLabel, bprpLabel, bpgLabel, grpLabel,
                maguTextField, magbTextField, magvTextField, magrTextField, magiTextField,
                gaiaIdTextField, aliasTextArea);

        formBinder.setFictionalFields(polityTextField, worldTypeTextField, fuelTypeTextField,
                techTypeTextField, portTypeTextField, popTypeTextField,
                prodField, milspaceTextField, milplanTextField);

        formBinder.setUserFields(misc1TextField, misc2TextField, misc3TextField,
                misc4TextField, misc5TextField,
                miscNum1TextField, miscNum2TextField, miscNum3TextField,
                miscNum4TextField, miscNum5TextField, forceLabel);

        // Initialize tabs
        formBinder.initializeOverviewTab(recordIdLabel, dataSetLabel);
        initializeFictionalTab();
        formBinder.initializeSecondaryTab();
        formBinder.initializeUserTab();

        // Set up button handlers
        updateAliasBtn.setOnAction(this::updateAliasList);
        resetBtn.setOnAction(this::cancelClicked);
        addBtn.setOnAction(this::changeClicked);

        // Set up checkbox handlers
        anomalyCheckbox.setOnAction(event -> record.setAnomaly(anomalyCheckbox.isSelected()));
        otherCheckbox.setOnAction(event -> record.setOther(otherCheckbox.isSelected()));
    }

    private void close(DialogEvent event) {
        StarEditStatus editStatus = new StarEditStatus();
        editStatus.setChanged(false);
        setResult(editStatus);
    }

    private void initializeFictionalTab() {
        // Initialize text fields from record
        formBinder.initializeFictionalTab();

        // Set up checkboxes
        anomalyCheckbox.setSelected(record.isAnomaly());
        otherCheckbox.setSelected(record.isOther());

        // Configure all combo boxes
        StarEditComboConfig.setupAllCombos(
                politiesComboBox, polityTextField, record.getPolity(),
                worldComboBox, worldTypeTextField, record.getWorldType(),
                fuelComboBox, fuelTypeTextField, record.getFuelType(),
                techComboBox, techTypeTextField, record.getTechType(),
                portComboBox, portTypeTextField, record.getPortType(),
                populationComboBox, popTypeTextField, record.getPopulationType(),
                productComboBox, prodField, record.getProductType(),
                milSpaceComboBox, milspaceTextField, record.getMilSpaceType(),
                milPlanComboBox, milplanTextField, record.getMilPlanType()
        );
    }

    private void updateAliasList(ActionEvent actionEvent) {
        SesameResolver resolver = new SesameResolver();
        List<String> aliasList = resolver.findAliases(record.getDisplayName());
        aliasTextArea.setText(String.join(", ", aliasList));

        // Safely access aliasList - it may be lazy loaded and not initialized
        if (!Hibernate.isInitialized(record.getAliasList())) {
            record.setAliasList(new HashSet<>());
        }
        record.getAliasList().addAll(aliasList);
        log.info("record updated");
    }

    private void changeClicked(ActionEvent actionEvent) {
        try {
            formBinder.collectAllData();

            // Validate critical stellar parameters for solar system generation
            String validationWarning = validateStellarParameters();
            if (validationWarning != null) {
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
