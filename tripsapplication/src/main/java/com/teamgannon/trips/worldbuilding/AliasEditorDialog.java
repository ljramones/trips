package com.teamgannon.trips.worldbuilding;

import com.teamgannon.trips.jpa.model.ExoPlanet;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.jpa.repository.ExoPlanetRepository;
import com.teamgannon.trips.jpa.repository.StarObjectRepository;
import com.teamgannon.trips.spaceshipmodeller.service.AliasDesignerService;
import com.teamgannon.trips.spaceshipmodeller.service.UniverseDesignerService;
import com.terranrepublic.assets.Alias;
import com.terranrepublic.assets.AliasTargetKind;
import com.terranrepublic.assets.Universe;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import lombok.extern.slf4j.Slf4j;
import org.controlsfx.control.textfield.TextFields;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Modal dialog for creating or editing an {@link Alias}. Single dialog class handles both
 * paths — the existing-alias parameter switches between create-mode (null) and edit-mode
 * (pre-populated, target locked).
 *
 * <p>Phase F.2 §6.4 — invoked from {@link AliasesDialog}'s Create / Edit buttons.
 *
 * <h2>Field semantics</h2>
 * <ul>
 *   <li><b>Universe</b> — required; defaults to the lone active universe when exactly one is
 *       active (most common case); otherwise no default. Locked on edit (changing a saved
 *       alias's universe would mean creating a different alias).</li>
 *   <li><b>Target kind</b> — Star / Exoplanet radio buttons. Locked on edit.</li>
 *   <li><b>Target</b> — autocomplete ComboBox driven by ControlsFX TextFields. For STAR kind,
 *       the items list is {@link StarObjectRepository#findAll()} mapped to display names; for
 *       EXOPLANET, {@link ExoPlanetRepository#findAll()} mapped to names. Locked on edit.</li>
 *   <li><b>Alias text</b> — required, max 255 chars per V18's column constraint.</li>
 *   <li><b>Description</b> — optional, max 1000 chars per V18.</li>
 * </ul>
 *
 * <p>Save flows through {@link AliasDesignerService#save(Alias)}, which enforces the two-layer
 * uniqueness contract from F.2 Step 2. On {@link IllegalStateException} (duplicate
 * (universe, target) pair), the friendly message is surfaced via an Alert and the dialog
 * stays open for the user to edit before retrying.
 */
@Slf4j
public class AliasEditorDialog extends Dialog<Alias> {

    private final AliasDesignerService aliasService;
    private final UniverseDesignerService universeService;
    private final StarObjectRepository starRepository;
    private final ExoPlanetRepository exoPlanetRepository;
    @Nullable
    private final Alias existing;

    private final ComboBox<String> universeCombo = new ComboBox<>();
    private final ToggleGroup kindGroup = new ToggleGroup();
    private final RadioButton starRadio = new RadioButton("Star");
    private final RadioButton exoplanetRadio = new RadioButton("Exoplanet");
    private final ComboBox<String> targetCombo = new ComboBox<>();
    private final TextField aliasTextField = new TextField();
    private final TextArea descriptionArea = new TextArea();
    private final Label errorLabel = new Label();

    /** universe-name → universe-id; built once on construction. */
    private final Map<String, String> universeNameToId = new HashMap<>();
    /** star-display-name → star-id; rebuilt on kind switch. */
    private final Map<String, String> starNameToId = new HashMap<>();
    /** exoplanet-name → exoplanet-id; rebuilt on kind switch. */
    private final Map<String, String> exoplanetNameToId = new HashMap<>();

    public AliasEditorDialog(AliasDesignerService aliasService,
                             UniverseDesignerService universeService,
                             StarObjectRepository starRepository,
                             ExoPlanetRepository exoPlanetRepository,
                             @Nullable Alias existing) {
        this.aliasService = aliasService;
        this.universeService = universeService;
        this.starRepository = starRepository;
        this.exoPlanetRepository = exoPlanetRepository;
        this.existing = existing;

        setTitle(existing == null ? "Create Alias" : "Edit Alias");
        setHeaderText(existing == null
                ? "Attach a fictional name to a real astronomical target."
                : "Modify this alias's text or description.");

        getDialogPane().setContent(buildContent());
        getDialogPane().getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
        // Wire the OK button to our save logic with error display rather than letting the dialog
        // close on the default OK action — we need to keep the dialog open on validation errors.
        getDialogPane().lookupButton(ButtonType.OK).addEventFilter(
                javafx.event.ActionEvent.ACTION, evt -> {
                    if (!attemptSave()) {
                        evt.consume();
                    }
                });

        setResultConverter(button -> button == ButtonType.OK ? savedResult : null);

        populateUniverses();
        if (existing == null) {
            // Create mode — start with STAR selected by default.
            starRadio.setSelected(true);
            populateTargetsForKind(AliasTargetKind.STAR);
        } else {
            applyExistingForEdit(existing);
        }

        kindGroup.selectedToggleProperty().addListener((obs, prev, sel) -> {
            if (existing != null) return; // edit-mode: locked
            populateTargetsForKind(currentKind());
        });
    }

    private GridPane buildContent() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));

        grid.add(new Label("Universe:"), 0, 0);
        universeCombo.setEditable(false);
        universeCombo.setPrefWidth(280);
        grid.add(universeCombo, 1, 0);

        grid.add(new Label("Target kind:"), 0, 1);
        starRadio.setToggleGroup(kindGroup);
        exoplanetRadio.setToggleGroup(kindGroup);
        HBox kindBox = new HBox(12, starRadio, exoplanetRadio);
        grid.add(kindBox, 1, 1);

        grid.add(new Label("Target:"), 0, 2);
        targetCombo.setEditable(true);
        targetCombo.setPromptText("start typing to filter");
        targetCombo.setPrefWidth(280);
        grid.add(targetCombo, 1, 2);

        grid.add(new Label("Alias text:"), 0, 3);
        aliasTextField.setPromptText("e.g. Vulcan");
        aliasTextField.setTextFormatter(new TextFormatter<>(
                change -> change.getControlNewText().length() <= 255 ? change : null));
        grid.add(aliasTextField, 1, 3);

        grid.add(new Label("Description:"), 0, 4);
        descriptionArea.setPrefRowCount(3);
        descriptionArea.setPromptText("Optional worldbuilding context (max 1000 chars)");
        descriptionArea.setTextFormatter(new TextFormatter<>(
                change -> change.getControlNewText().length() <= 1000 ? change : null));
        grid.add(descriptionArea, 1, 4);

        errorLabel.setStyle("-fx-text-fill: red;");
        errorLabel.setWrapText(true);
        errorLabel.setMaxWidth(380);
        grid.add(errorLabel, 1, 5);

        return grid;
    }

    private void populateUniverses() {
        universeNameToId.clear();
        List<Universe> all = universeService.findAll().stream()
                .sorted(Comparator.comparing(Universe::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
        for (Universe u : all) {
            universeCombo.getItems().add(u.name());
            universeNameToId.put(u.name(), u.id());
        }
        // Default for create mode: if exactly one universe is active, prefill.
        if (existing == null) {
            List<Universe> active = universeService.findAllActive();
            if (active.size() == 1) {
                universeCombo.setValue(active.get(0).name());
            }
        }
    }

    private void populateTargetsForKind(AliasTargetKind kind) {
        targetCombo.getItems().clear();
        targetCombo.setValue(null);
        targetCombo.getEditor().clear();
        if (kind == AliasTargetKind.STAR) {
            starNameToId.clear();
            starRepository.findAll().forEach(s -> {
                String displayName = s.getDisplayName();
                if (displayName != null && !displayName.isBlank() && s.getId() != null) {
                    targetCombo.getItems().add(displayName);
                    starNameToId.put(displayName, s.getId());
                }
            });
        } else {
            exoplanetNameToId.clear();
            exoPlanetRepository.findAll().forEach(ep -> {
                String name = ep.getName();
                if (name != null && !name.isBlank() && ep.getId() != null) {
                    targetCombo.getItems().add(name);
                    exoplanetNameToId.put(name, ep.getId());
                }
            });
        }
        TextFields.bindAutoCompletion(targetCombo.getEditor(), targetCombo.getItems());
    }

    private void applyExistingForEdit(Alias alias) {
        // Universe: pre-set + lock.
        String universeName = universeService.findById(alias.universeId())
                .map(Universe::name)
                .orElse("(unknown)");
        if (!universeCombo.getItems().contains(universeName)) {
            universeCombo.getItems().add(universeName);
            universeNameToId.put(universeName, alias.universeId());
        }
        universeCombo.setValue(universeName);
        universeCombo.setDisable(true);

        // Kind: pre-select + lock.
        if (alias.targetKind() == AliasTargetKind.STAR) {
            starRadio.setSelected(true);
        } else {
            exoplanetRadio.setSelected(true);
        }
        starRadio.setDisable(true);
        exoplanetRadio.setDisable(true);

        // Target: load candidates so the user sees the resolved name, then pre-set + lock.
        populateTargetsForKind(alias.targetKind());
        String resolvedTargetName = resolveTargetName(alias.targetKind(), alias.targetId());
        if (!targetCombo.getItems().contains(resolvedTargetName)) {
            targetCombo.getItems().add(resolvedTargetName);
            if (alias.targetKind() == AliasTargetKind.STAR) {
                starNameToId.put(resolvedTargetName, alias.targetId());
            } else {
                exoplanetNameToId.put(resolvedTargetName, alias.targetId());
            }
        }
        targetCombo.setValue(resolvedTargetName);
        targetCombo.setDisable(true);

        // Editable fields: pre-fill.
        aliasTextField.setText(alias.aliasText());
        descriptionArea.setText(alias.description());
    }

    private String resolveTargetName(AliasTargetKind kind, String targetId) {
        if (kind == AliasTargetKind.STAR) {
            return starRepository.findById(targetId)
                    .map(StarObject::getDisplayName)
                    .orElse(targetId);
        }
        return exoPlanetRepository.findById(targetId)
                .map(ExoPlanet::getName)
                .orElse(targetId);
    }

    private AliasTargetKind currentKind() {
        return starRadio.isSelected() ? AliasTargetKind.STAR : AliasTargetKind.EXOPLANET;
    }

    /** Result accumulator — set by attemptSave() so the resultConverter can return it. */
    @Nullable
    private Alias savedResult = null;

    /**
     * Validates input + invokes {@link AliasDesignerService#save}. Returns {@code true} when
     * the save succeeded (dialog should close); {@code false} when validation failed or save
     * threw (dialog should stay open).
     */
    boolean attemptSave() {
        errorLabel.setText("");
        String universeName = universeCombo.getValue();
        String universeId = universeName == null ? null : universeNameToId.get(universeName);
        if (universeId == null || universeId.isBlank()) {
            errorLabel.setText("Universe is required.");
            return false;
        }
        AliasTargetKind kind = currentKind();
        String targetName = targetCombo.getValue();
        if (targetName == null || targetName.isBlank()) {
            errorLabel.setText("Target is required.");
            return false;
        }
        String targetId = kind == AliasTargetKind.STAR
                ? starNameToId.get(targetName)
                : exoplanetNameToId.get(targetName);
        if (targetId == null) {
            errorLabel.setText("Pick a target from the list (autocomplete suggestions).");
            return false;
        }
        String aliasText = aliasTextField.getText();
        if (aliasText == null || aliasText.isBlank()) {
            errorLabel.setText("Alias text is required.");
            return false;
        }
        String description = descriptionArea.getText() == null ? "" : descriptionArea.getText();

        Alias toSave;
        if (existing == null) {
            toSave = new Alias(universeId, kind, targetId, aliasText, description);
        } else {
            toSave = new Alias(
                    existing.id(), existing.universeId(), existing.targetKind(), existing.targetId(),
                    aliasText, description, existing.createdAt(), java.time.Instant.now());
        }
        try {
            savedResult = aliasService.save(toSave);
            return true;
        } catch (IllegalStateException duplicate) {
            errorLabel.setText(duplicate.getMessage());
            return false;
        } catch (Exception ex) {
            log.error("Failed to save alias", ex);
            errorLabel.setText("Save failed: " + ex.getMessage());
            return false;
        }
    }

    // ============================================================
    // Test seams
    // ============================================================

    ComboBox<String> universeComboForTest() { return universeCombo; }
    RadioButton starRadioForTest() { return starRadio; }
    RadioButton exoplanetRadioForTest() { return exoplanetRadio; }
    ComboBox<String> targetComboForTest() { return targetCombo; }
    TextField aliasTextFieldForTest() { return aliasTextField; }
    TextArea descriptionAreaForTest() { return descriptionArea; }
    Label errorLabelForTest() { return errorLabel; }
    @Nullable Alias savedResultForTest() { return savedResult; }
}
