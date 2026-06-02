package com.teamgannon.trips.screenobjects;

import com.teamgannon.trips.utility.SesameResolver;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;


/**
 * Dialog for editing star attributes.
 * <p>
 * As of Issue 23 / Phase 7-follow-on the dialog takes a
 * {@link StarEditViewModel} rather than a live {@link com.teamgannon.trips.jpa.model.StarObject}
 * entity. The caller is responsible for loading the entity, converting
 * via {@link StarEditMapper#toViewModel} inside a transactional service,
 * showing the dialog, and on save converting back via
 * {@link StarEditMapper#applyToEntity} + persisting.
 */
@Slf4j
public class StarEditDialog extends Dialog<StarEditStatus> {

    private final @NotNull StarEditViewModel vm;
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

    // Fictional Info worldbuilding widgets removed by the Worldbuilding Data Model
    // Normalization task. The whole "Fictional Info" tab came out of the FXML, and
    // the corresponding view-model fields, form-binder bindings, and combo-box
    // config helper all went with it.

    @FXML private CheckBox forceLabel;
    @FXML private Button updateAliasBtn;
    @FXML private Button resetBtn;
    @FXML private Button addBtn;

    public StarEditDialog(@NotNull StarEditViewModel vm) {
        this.vm = vm;
        this.formBinder = new StarEditFormBinder(vm);

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
        this.setTitle("Change attributes for " + vm.getDisplayName());
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

        formBinder.setUserFields(forceLabel);

        // Initialize tabs (the Fictional Info tab came out with the worldbuilding cleanup)
        formBinder.initializeOverviewTab(recordIdLabel, dataSetLabel);
        formBinder.initializeSecondaryTab();
        formBinder.initializeUserTab();

        // Set up button handlers
        updateAliasBtn.setOnAction(this::updateAliasList);
        resetBtn.setOnAction(this::cancelClicked);
        addBtn.setOnAction(this::changeClicked);
    }

    private void close(DialogEvent event) {
        StarEditStatus editStatus = new StarEditStatus();
        editStatus.setChanged(false);
        setResult(editStatus);
    }

    private void updateAliasList(ActionEvent actionEvent) {
        SesameResolver resolver = new SesameResolver();
        List<String> aliasList = resolver.findAliases(vm.getDisplayName());
        aliasTextArea.setText(String.join(", ", aliasList));

        // The view-model's aliases are a plain ArrayList already materialised
        // by StarEditMapper.toViewModel — no LIE risk, just mutate it.
        vm.getAliases().addAll(aliasList);
        log.info("view-model updated with {} new alias(es)", aliasList.size());
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
            starEditStatus.setViewModel(vm);
            starEditStatus.setChanged(true);
            setResult(starEditStatus);
        } catch (StarFieldValidationException e) {
            showErrorAlert("enter star data", e.getMessage());
        }
    }

    /**
     * Validates that critical stellar parameters are present for solar system generation.
     * Returns a warning message if any are missing/zero, or null if all are valid.
     */
    private String validateStellarParameters() {
        StringBuilder issues = new StringBuilder();

        if (vm.getMass() <= 0) {
            issues.append("• Mass is missing or zero\n");
        }
        if (vm.getRadius() <= 0) {
            issues.append("• Radius is missing or zero\n");
        }
        if (vm.getTemperature() <= 0) {
            issues.append("• Temperature is missing or zero\n");
        }

        // Check luminosity - it's a String that should parse to a positive number
        String lumStr = vm.getLuminosity();
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
