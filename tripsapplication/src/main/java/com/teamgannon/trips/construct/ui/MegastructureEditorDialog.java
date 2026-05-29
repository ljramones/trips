package com.teamgannon.trips.construct.ui;

import com.teamgannon.trips.spaceshipmodeller.propulsion.DriveType;
import com.teamgannon.trips.utility.DialogUtils;
import com.terranrepublic.assets.Armament;
import com.terranrepublic.assets.CatalogOperationalStatus;
import com.terranrepublic.assets.CatalogProvenance;
import com.terranrepublic.assets.InteriorGravityType;
import com.terranrepublic.assets.Megastructure;
import com.terranrepublic.assets.MegastructureArchetype;
import com.terranrepublic.assets.MegastructureOriginType;
import com.terranrepublic.assets.Mobility;
import com.terranrepublic.assets.OperationalState;
import com.terranrepublic.assets.SourceType;
import com.terranrepublic.assets.StationFunction;
import com.terranrepublic.assets.TechLevel;
import com.terranrepublic.assets.WeaponType;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.util.StringConverter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.teamgannon.trips.construct.ui.ConstructLabels.get;

/**
 * Modal editor for a {@link Megastructure}. Mirrors the {@link StationEditorDialog} pattern from
 * v2 Phase D.6 with additions for the megastructure-specific axes (archetype, origin, interior,
 * megaton-scale mass).
 *
 * <p>UI invariants:
 * <ul>
 *   <li>Setting {@code primaryFunction} to a value currently in the secondary selection silently
 *       removes it (matches the record's compact-constructor invariant);</li>
 *   <li>The soft 3-cap hint appears when more than three secondary functions are selected;</li>
 *   <li>The {@code auxiliaryDrive} combo includes a "None" sentinel mapping to {@code null}.
 *       Unlike {@link StationEditorDialog}, FIXED mobility does NOT disable the aux-drive combo
 *       on a megastructure (Resolution A from D.7's gap analysis: the StationDesign-specific
 *       FIXED-forbids-auxiliaryDrive invariant is intentionally not carried across);</li>
 *   <li>{@code hasInteriorSetting} is informational only — it does NOT gate the visibility of
 *       interior population / gravity fields (D.7 §8 UX call).</li>
 * </ul>
 */
public class MegastructureEditorDialog extends Dialog<Megastructure> {

    private final Megastructure existing;

    // identity
    private final TextField nameField = new TextField();
    private final TextField designationField = new TextField();
    private final TextArea descriptionArea = new TextArea();
    private final TextField categoryField = new TextField();
    private final TextArea notesArea = new TextArea();
    private final TextField factionField = new TextField();
    private final TextField allegianceField = new TextField();
    private final ComboBox<TechLevel> techLevelCombo = new ComboBox<>();

    // catalog (D.6 axis)
    private final ComboBox<SourceType> sourceTypeCombo = new ComboBox<>();
    private final TextField sourceUniverseField = new TextField();
    private final TextField sourceWorkField = new TextField();
    private final ComboBox<CatalogOperationalStatus> catalogStatusCombo = new ComboBox<>();

    // archetype & origin
    private final ComboBox<MegastructureArchetype> archetypeCombo = new ComboBox<>();
    private final ComboBox<MegastructureOriginType> originTypeCombo = new ComboBox<>();
    private final TextField builderPolityField = new TextField();
    private final TextField constructionYearField = new TextField();
    private final TextField discoveryYearField = new TextField();

    // structural (megaton scale)
    private final TextField dimensionsKmField = new TextField();
    private final TextField dryMassMegatonsField = new TextField();
    private final TextField internalVolumeKm3Field = new TextField();

    // mobility
    private final ComboBox<Mobility> mobilityCombo = new ComboBox<>();
    private final ComboBox<DriveType> auxiliaryDriveCombo = new ComboBox<>();

    // function (D.6 axis)
    private final ComboBox<StationFunction> primaryFunctionCombo = new ComboBox<>();
    private final ListView<StationFunction> secondaryFunctionsList = new ListView<>(
            FXCollections.observableArrayList(StationFunction.values()));
    private final Label secondaryHintLabel = new Label();

    // interior
    private final CheckBox hasInteriorSettingCheck = new CheckBox();
    private final TextField interiorPopulationField = new TextField();
    private final ComboBox<InteriorGravityType> interiorGravityCombo = new ComboBox<>();

    // operational
    private final ComboBox<OperationalState> operationalStateCombo = new ComboBox<>();
    private final CheckBox concealedCheck = new CheckBox();

    // armaments (reuse the StationEditorDialog mini-editor shape)
    private final ObservableList<Armament> armaments = FXCollections.observableArrayList();
    private final TableView<Armament> armamentTable = new TableView<>(armaments);
    private final TextField armNameField = new TextField();
    private final ComboBox<WeaponType> armTypeCombo = new ComboBox<>();
    private final TextField armQuantityField = new TextField("1");
    private final TextField armYieldField = new TextField("0");
    private final TextField armRangeField = new TextField("0");
    private final TextField armRoleField = new TextField();
    private final TextField armNotesField = new TextField();

    // validation
    private final Label statusLabel = new Label();
    private final ListView<String> messagesList = new ListView<>();
    private Button okButton;

    public MegastructureEditorDialog() {
        this(null);
    }

    public MegastructureEditorDialog(Megastructure existing) {
        this.existing = existing;
        setTitle(get(existing == null
                ? "editor.megastructure.title.new"
                : "editor.megastructure.title.edit"));
        getDialogPane().getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
        okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);

        archetypeCombo.getItems().setAll(MegastructureArchetype.values());
        originTypeCombo.getItems().setAll(MegastructureOriginType.values());
        interiorGravityCombo.getItems().setAll(InteriorGravityType.values());
        mobilityCombo.getItems().setAll(Mobility.values());

        // auxiliaryDrive combo: "None" sentinel + every DriveType value. Per the Step 5 spec,
        // null is selectable as the "None" item; getValue() returns null when selected. Mirrors
        // the StationEditorDialog combo shape but adds a leading null sentinel and a converter.
        auxiliaryDriveCombo.getItems().add(null);
        auxiliaryDriveCombo.getItems().addAll(DriveType.values());
        auxiliaryDriveCombo.setConverter(auxiliaryDriveConverter());

        operationalStateCombo.getItems().setAll(OperationalState.values());
        operationalStateCombo.setConverter(operationalStateConverter());
        techLevelCombo.getItems().setAll(TechLevel.values());
        armTypeCombo.getItems().setAll(WeaponType.values());

        sourceTypeCombo.getItems().setAll(SourceType.values());
        sourceTypeCombo.setConverter(sourceTypeConverter());
        catalogStatusCombo.getItems().setAll(CatalogOperationalStatus.values());
        primaryFunctionCombo.getItems().setAll(StationFunction.values());
        secondaryFunctionsList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        secondaryFunctionsList.setPrefHeight(140);

        buildContent();
        DialogUtils.applyTheme(this);
        configureAccessibility();
        wirePrimarySecondaryInvariant();
        wireValidation();

        if (existing != null) {
            populateFrom(existing);
        } else {
            applyDefaults();
        }
        revalidate();

        setResultConverter(buttonType -> buttonType == ButtonType.OK ? buildDraft() : null);
    }

    private static StringConverter<OperationalState> operationalStateConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(OperationalState s) {
                return s == null ? "" : get("operationalState." + s.name(), s.name());
            }

            @Override
            public OperationalState fromString(String s) {
                return null;
            }
        };
    }

    private static StringConverter<SourceType> sourceTypeConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(SourceType t) {
                return t == null ? "" : t.label();
            }

            @Override
            public SourceType fromString(String s) {
                return null;
            }
        };
    }

    private static StringConverter<DriveType> auxiliaryDriveConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(DriveType d) {
                return d == null ? get("editor.megastructure.field.auxiliaryDrive.none") : d.name();
            }

            @Override
            public DriveType fromString(String s) {
                return null;
            }
        };
    }

    private void buildContent() {
        VBox root = new VBox(12);
        root.setPadding(new Insets(14));
        root.getChildren().addAll(
                section(get("editor.megastructure.section.basic"), basicGrid()),
                section(get("editor.megastructure.section.catalog"), catalogGrid()),
                section(get("editor.megastructure.section.archetype"), archetypeGrid()),
                section(get("editor.megastructure.section.structural"), structuralGrid()),
                section(get("editor.megastructure.section.mobility"), mobilityGrid()),
                section(get("editor.megastructure.section.function"), functionSection()),
                section(get("editor.megastructure.section.interior"), interiorGrid()),
                section(get("editor.megastructure.section.operational"), operationalGrid()),
                section(get("editor.megastructure.section.armaments"), armamentSection()),
                section(get("editor.megastructure.section.validation"), validationSection()));

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.setPrefViewportHeight(640);
        getDialogPane().setContent(scroll);
        getDialogPane().setPrefWidth(720);
    }

    private VBox section(String title, Node body) {
        Label header = new Label(title);
        header.setFont(Font.font(header.getFont().getFamily(), 14));
        header.getStyleClass().add("trips-bold");
        VBox box = new VBox(6, header, body);
        box.setPadding(new Insets(4, 0, 4, 0));
        return box;
    }

    private GridPane basicGrid() {
        GridPane g = grid();
        descriptionArea.setPrefRowCount(4);
        notesArea.setPrefRowCount(2);
        int r = 0;
        addRow(g, r++, get("editor.field.name"), nameField);
        addRow(g, r++, get("editor.field.designation"), designationField);
        addRow(g, r++, get("editor.field.description"), descriptionArea);
        addRow(g, r++, get("editor.megastructure.field.category"), categoryField);
        addRow(g, r++, get("editor.megastructure.field.notes"), notesArea);
        addRow(g, r++, get("editor.field.faction"), factionField);
        addRow(g, r++, get("editor.field.allegiance"), allegianceField);
        addRow(g, r++, get("editor.megastructure.field.techLevel"), techLevelCombo);
        return g;
    }

    private GridPane catalogGrid() {
        GridPane g = grid();
        int r = 0;
        addRow(g, r++, get("editor.station.field.sourceType"), sourceTypeCombo);
        addRow(g, r++, get("editor.station.field.universe"), sourceUniverseField);
        addRow(g, r++, get("editor.station.field.sourceWork"), sourceWorkField);
        addRow(g, r++, get("editor.station.field.catalogStatus"), catalogStatusCombo);
        return g;
    }

    private GridPane archetypeGrid() {
        GridPane g = grid();
        int r = 0;
        addRow(g, r++, get("editor.megastructure.field.archetype"), archetypeCombo);
        addRow(g, r++, get("editor.megastructure.field.originType"), originTypeCombo);
        addRow(g, r++, get("editor.megastructure.field.builderPolity"), builderPolityField);
        addRow(g, r++, get("editor.megastructure.field.constructionYear"), constructionYearField);
        addRow(g, r++, get("editor.megastructure.field.discoveryYear"), discoveryYearField);
        return g;
    }

    private GridPane structuralGrid() {
        GridPane g = grid();
        int r = 0;
        addRow(g, r++, get("editor.megastructure.field.dimensionsKm"), dimensionsKmField);
        addRow(g, r++, get("editor.megastructure.field.dryMassMegatons"), dryMassMegatonsField);
        addRow(g, r++, get("editor.megastructure.field.internalVolumeKm3"), internalVolumeKm3Field);
        return g;
    }

    private GridPane mobilityGrid() {
        GridPane g = grid();
        int r = 0;
        addRow(g, r++, get("editor.megastructure.field.mobility"), mobilityCombo);
        addRow(g, r++, get("editor.megastructure.field.auxiliaryDrive"), auxiliaryDriveCombo);
        return g;
    }

    private VBox functionSection() {
        GridPane g = grid();
        int r = 0;
        addRow(g, r++, get("editor.megastructure.field.primaryFunction"), primaryFunctionCombo);
        addRow(g, r++, get("editor.megastructure.field.secondaryFunctions"), secondaryFunctionsList);

        secondaryHintLabel.setText(get("editor.megastructure.hint.secondaryCap"));
        secondaryHintLabel.getStyleClass().add("trips-text-italic-warn");
        secondaryHintLabel.setWrapText(true);
        secondaryHintLabel.setVisible(false);
        secondaryHintLabel.setManaged(false);

        return new VBox(6, g, secondaryHintLabel);
    }

    private GridPane interiorGrid() {
        GridPane g = grid();
        int r = 0;
        addRow(g, r++, get("editor.megastructure.field.hasInteriorSetting"), hasInteriorSettingCheck);
        addRow(g, r++, get("editor.megastructure.field.interiorPopulation"), interiorPopulationField);
        addRow(g, r++, get("editor.megastructure.field.interiorGravity"), interiorGravityCombo);
        return g;
    }

    private GridPane operationalGrid() {
        GridPane g = grid();
        int r = 0;
        addRow(g, r++, get("editor.megastructure.field.operationalState"), operationalStateCombo);
        addRow(g, r++, get("editor.megastructure.field.concealed"), concealedCheck);
        return g;
    }

    @SuppressWarnings("unchecked")
    private VBox armamentSection() {
        TableColumn<Armament, String> nameCol = new TableColumn<>(get("editor.armament.column.name"));
        nameCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().name()));
        nameCol.setPrefWidth(150);
        TableColumn<Armament, WeaponType> typeCol = new TableColumn<>(get("editor.armament.column.type"));
        typeCol.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().type()));
        TableColumn<Armament, Number> qtyCol = new TableColumn<>(get("editor.armament.column.quantity"));
        qtyCol.setCellValueFactory(cd -> new SimpleIntegerProperty(cd.getValue().quantity()));
        TableColumn<Armament, String> roleCol = new TableColumn<>(get("editor.armament.column.role"));
        roleCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().role()));
        roleCol.setPrefWidth(120);
        armamentTable.getColumns().setAll(List.of(nameCol, typeCol, qtyCol, roleCol));
        armamentTable.setPrefHeight(120);
        armamentTable.setPlaceholder(new Label(get("editor.armament.placeholder")));
        armamentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        armNameField.setPromptText(get("editor.armament.column.name"));
        armRoleField.setPromptText(get("editor.armament.column.role"));
        armNotesField.setPromptText(get("editor.armament.column.notes"));
        armQuantityField.setPrefWidth(60);
        armYieldField.setPrefWidth(80);
        armRangeField.setPrefWidth(80);

        Button addBtn = new Button(get("editor.button.add"));
        addBtn.setOnAction(e -> addArmament());
        Button removeBtn = new Button(get("editor.button.remove"));
        removeBtn.setOnAction(e -> {
            Armament sel = armamentTable.getSelectionModel().getSelectedItem();
            if (sel != null) {
                armaments.remove(sel);
                revalidate();
            }
        });

        HBox addForm = new HBox(6, armNameField, armTypeCombo,
                new Label("x"), armQuantityField,
                new Label("MW"), armYieldField,
                new Label("km"), armRangeField,
                armRoleField, armNotesField, addBtn, removeBtn);
        HBox.setHgrow(armNameField, Priority.ALWAYS);
        return new VBox(6, armamentTable, addForm);
    }

    private VBox validationSection() {
        statusLabel.getStyleClass().add("trips-bold");
        messagesList.setPrefHeight(80);
        return new VBox(6, statusLabel, messagesList);
    }

    private void configureAccessibility() {
        getDialogPane().setAccessibleText(existing == null
                ? get("editor.megastructure.title.new")
                : get("editor.megastructure.title.edit"));
        annotate(okButton, get("editor.button.ok"), "Save this megastructure when validation passes.");
        Button cancel = (Button) getDialogPane().lookupButton(ButtonType.CANCEL);
        annotate(cancel, get("editor.button.cancel"), "Discard changes and close.");

        annotate(nameField, get("editor.field.name"), "Required display name.");
        annotate(designationField, get("editor.field.designation"), "Optional designation (e.g. DS-1, TR-01).");
        annotate(descriptionArea, get("editor.field.description"),
                "Long-form prose describing the megastructure, including interior detail when applicable.");
        annotate(categoryField, get("editor.megastructure.field.category"), "Free-form designer category label.");
        annotate(notesArea, get("editor.megastructure.field.notes"),
                "Curator notes — assumption flags, inference reminders, source citations.");
        annotate(factionField, get("editor.field.faction"), "Owning faction or polity at time of design.");
        annotate(allegianceField, get("editor.field.allegiance"), get("editor.tooltip.allegiance"));
        allegianceField.setTooltip(new Tooltip(get("editor.tooltip.allegiance")));
        annotate(techLevelCombo, get("editor.megastructure.field.techLevel"), "Coarse technology band.");

        annotate(sourceTypeCombo, get("editor.station.field.sourceType"),
                get("editor.station.tooltip.sourceType"));
        sourceTypeCombo.setTooltip(new Tooltip(get("editor.station.tooltip.sourceType")));
        annotate(sourceUniverseField, get("editor.station.field.universe"),
                get("editor.station.tooltip.universe"));
        sourceUniverseField.setTooltip(new Tooltip(get("editor.station.tooltip.universe")));
        annotate(sourceWorkField, get("editor.station.field.sourceWork"),
                get("editor.station.tooltip.sourceWork"));
        sourceWorkField.setTooltip(new Tooltip(get("editor.station.tooltip.sourceWork")));
        annotate(catalogStatusCombo, get("editor.station.field.catalogStatus"),
                get("editor.station.tooltip.catalogStatus"));
        catalogStatusCombo.setTooltip(new Tooltip(get("editor.station.tooltip.catalogStatus")));

        annotate(archetypeCombo, get("editor.megastructure.field.archetype"),
                get("editor.megastructure.tooltip.archetype"));
        archetypeCombo.setTooltip(new Tooltip(get("editor.megastructure.tooltip.archetype")));
        annotate(originTypeCombo, get("editor.megastructure.field.originType"),
                get("editor.megastructure.tooltip.originType"));
        originTypeCombo.setTooltip(new Tooltip(get("editor.megastructure.tooltip.originType")));
        annotate(builderPolityField, get("editor.megastructure.field.builderPolity"),
                get("editor.megastructure.tooltip.builderPolity"));
        builderPolityField.setTooltip(new Tooltip(get("editor.megastructure.tooltip.builderPolity")));
        annotate(constructionYearField, get("editor.megastructure.field.constructionYear"),
                get("editor.megastructure.tooltip.constructionYear"));
        constructionYearField.setTooltip(new Tooltip(get("editor.megastructure.tooltip.constructionYear")));
        annotate(discoveryYearField, get("editor.megastructure.field.discoveryYear"),
                get("editor.megastructure.tooltip.discoveryYear"));
        discoveryYearField.setTooltip(new Tooltip(get("editor.megastructure.tooltip.discoveryYear")));

        annotate(dimensionsKmField, get("editor.megastructure.field.dimensionsKm"),
                get("editor.megastructure.tooltip.dimensionsKm"));
        dimensionsKmField.setTooltip(new Tooltip(get("editor.megastructure.tooltip.dimensionsKm")));
        annotate(dryMassMegatonsField, get("editor.megastructure.field.dryMassMegatons"),
                get("editor.megastructure.tooltip.dryMassMegatons"));
        dryMassMegatonsField.setTooltip(new Tooltip(get("editor.megastructure.tooltip.dryMassMegatons")));
        annotate(internalVolumeKm3Field, get("editor.megastructure.field.internalVolumeKm3"),
                get("editor.megastructure.tooltip.internalVolumeKm3"));
        internalVolumeKm3Field.setTooltip(new Tooltip(get("editor.megastructure.tooltip.internalVolumeKm3")));

        annotate(mobilityCombo, get("editor.megastructure.field.mobility"),
                get("editor.megastructure.tooltip.mobility"));
        mobilityCombo.setTooltip(new Tooltip(get("editor.megastructure.tooltip.mobility")));
        annotate(auxiliaryDriveCombo, get("editor.megastructure.field.auxiliaryDrive"),
                get("editor.megastructure.tooltip.auxiliaryDrive"));
        auxiliaryDriveCombo.setTooltip(new Tooltip(get("editor.megastructure.tooltip.auxiliaryDrive")));

        annotate(primaryFunctionCombo, get("editor.megastructure.field.primaryFunction"),
                get("editor.megastructure.tooltip.primaryFunction"));
        primaryFunctionCombo.setTooltip(new Tooltip(get("editor.megastructure.tooltip.primaryFunction")));
        annotate(secondaryFunctionsList, get("editor.megastructure.field.secondaryFunctions"),
                get("editor.megastructure.tooltip.secondaryFunctions"));
        secondaryFunctionsList.setTooltip(new Tooltip(get("editor.megastructure.tooltip.secondaryFunctions")));

        annotate(hasInteriorSettingCheck, get("editor.megastructure.field.hasInteriorSetting"),
                get("editor.megastructure.tooltip.hasInteriorSetting"));
        hasInteriorSettingCheck.setTooltip(new Tooltip(get("editor.megastructure.tooltip.hasInteriorSetting")));
        annotate(interiorPopulationField, get("editor.megastructure.field.interiorPopulation"),
                get("editor.megastructure.tooltip.interiorPopulation"));
        interiorPopulationField.setTooltip(new Tooltip(get("editor.megastructure.tooltip.interiorPopulation")));
        annotate(interiorGravityCombo, get("editor.megastructure.field.interiorGravity"),
                get("editor.megastructure.tooltip.interiorGravity"));
        interiorGravityCombo.setTooltip(new Tooltip(get("editor.megastructure.tooltip.interiorGravity")));

        annotate(operationalStateCombo, get("editor.megastructure.field.operationalState"),
                get("editor.tooltip.operationalState"));
        operationalStateCombo.setTooltip(new Tooltip(get("editor.tooltip.operationalState")));
        annotate(concealedCheck, get("editor.megastructure.field.concealed"),
                get("editor.tooltip.concealed"));
        concealedCheck.setTooltip(new Tooltip(get("editor.tooltip.concealed")));
    }

    /**
     * UI invariant: when {@code primaryFunctionCombo} changes to a value that is currently
     * selected in {@code secondaryFunctionsList}, silently remove it from the selection.
     */
    private void wirePrimarySecondaryInvariant() {
        primaryFunctionCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                int idx = secondaryFunctionsList.getItems().indexOf(newVal);
                if (idx >= 0 && secondaryFunctionsList.getSelectionModel().isSelected(idx)) {
                    secondaryFunctionsList.getSelectionModel().clearSelection(idx);
                }
            }
            updateSecondaryHint();
            revalidate();
        });
        secondaryFunctionsList.getSelectionModel().getSelectedItems().addListener(
                (javafx.collections.ListChangeListener<StationFunction>) c -> {
                    updateSecondaryHint();
                    revalidate();
                });
        updateSecondaryHint();
    }

    private void updateSecondaryHint() {
        int size = secondaryFunctionsList.getSelectionModel().getSelectedItems().size();
        boolean show = size > 3;
        secondaryHintLabel.setVisible(show);
        secondaryHintLabel.setManaged(show);
    }

    private void wireValidation() {
        nameField.textProperty().addListener((o, a, b) -> revalidate());
        archetypeCombo.valueProperty().addListener((o, a, b) -> revalidate());
        originTypeCombo.valueProperty().addListener((o, a, b) -> revalidate());
        mobilityCombo.valueProperty().addListener((o, a, b) -> revalidate());
        dimensionsKmField.textProperty().addListener((o, a, b) -> revalidate());
        dryMassMegatonsField.textProperty().addListener((o, a, b) -> revalidate());
        armaments.addListener((javafx.collections.ListChangeListener<Armament>) c -> revalidate());
    }

    private void revalidate() {
        List<String> errors = new ArrayList<>();
        if (nameField.getText() == null || nameField.getText().isBlank()) {
            errors.add(get("validation.nameRequired"));
        }
        if (archetypeCombo.getValue() == null) {
            errors.add(get("validation.archetypeRequired"));
        }
        if (originTypeCombo.getValue() == null) {
            errors.add(get("validation.originTypeRequired"));
        }
        if (mobilityCombo.getValue() == null) {
            errors.add(get("validation.mobilityRequired"));
        }
        if (parse(dimensionsKmField, -1) <= 0) {
            errors.add(get("validation.dimensionsPositive"));
        }
        if (parse(dryMassMegatonsField, -1) <= 0) {
            errors.add(get("validation.dryMassMegatonsPositive"));
        }
        if (parse(internalVolumeKm3Field, 0) < 0
                || parseLong(interiorPopulationField, 0) < 0) {
            errors.add(get("validation.numericNonNegative"));
        }

        messagesList.getItems().setAll(errors);
        if (errors.isEmpty()) {
            statusLabel.setText(get("editor.status.valid"));
            statusLabel.getStyleClass().removeAll("trips-text-insufficient");
            statusLabel.getStyleClass().add("trips-text-feasible");
        } else {
            statusLabel.setText(get("editor.status.invalid"));
            statusLabel.getStyleClass().removeAll("trips-text-feasible");
            statusLabel.getStyleClass().add("trips-text-insufficient");
        }
        if (okButton != null) {
            okButton.setDisable(!errors.isEmpty());
        }
    }

    private void populateFrom(Megastructure m) {
        nameField.setText(m.name());
        designationField.setText(m.designation());
        descriptionArea.setText(m.description() == null ? "" : m.description());
        categoryField.setText(m.category() == null ? "" : m.category());
        notesArea.setText(m.notes() == null ? "" : m.notes());
        factionField.setText(m.faction() == null ? "" : m.faction());
        allegianceField.setText(m.allegiance() == null ? "" : m.allegiance());
        techLevelCombo.setValue(m.techLevel());

        CatalogProvenance provenance = m.provenance();
        sourceTypeCombo.setValue(provenance.sourceType());
        sourceUniverseField.setText(provenance.sourceUniverse());
        sourceWorkField.setText(provenance.sourceWork() == null ? "" : provenance.sourceWork());
        catalogStatusCombo.setValue(provenance.status());

        archetypeCombo.setValue(m.archetype());
        originTypeCombo.setValue(m.originType());
        builderPolityField.setText(m.builderPolity() == null ? "" : m.builderPolity());
        constructionYearField.setText(m.constructionYear() == null ? "" : Integer.toString(m.constructionYear()));
        discoveryYearField.setText(m.discoveryYear() == null ? "" : Integer.toString(m.discoveryYear()));

        dimensionsKmField.setText(Double.toString(m.dimensionsKm()));
        dryMassMegatonsField.setText(Double.toString(m.dryMassMegatons()));
        internalVolumeKm3Field.setText(Double.toString(m.internalVolumeKm3()));

        mobilityCombo.setValue(m.mobility());
        auxiliaryDriveCombo.setValue(m.auxiliaryDrive());

        primaryFunctionCombo.setValue(m.primaryFunction());
        secondaryFunctionsList.getSelectionModel().clearSelection();
        for (StationFunction f : m.secondaryFunctions()) {
            int idx = secondaryFunctionsList.getItems().indexOf(f);
            if (idx >= 0) {
                secondaryFunctionsList.getSelectionModel().select(idx);
            }
        }
        updateSecondaryHint();

        hasInteriorSettingCheck.setSelected(m.hasInteriorSetting());
        interiorPopulationField.setText(Long.toString(m.interiorPopulation()));
        interiorGravityCombo.setValue(m.interiorGravity());

        operationalStateCombo.setValue(m.operationalState());
        concealedCheck.setSelected(m.concealed());

        armaments.setAll(m.armaments());
    }

    private void applyDefaults() {
        nameField.setText("");
        archetypeCombo.setValue(MegastructureArchetype.UNKNOWN);
        originTypeCombo.setValue(MegastructureOriginType.UNKNOWN);
        mobilityCombo.setValue(Mobility.STATIONKEEPING);
        auxiliaryDriveCombo.setValue(null);
        operationalStateCombo.setValue(OperationalState.OPERATIONAL);
        techLevelCombo.setValue(TechLevel.UNKNOWN);
        interiorGravityCombo.setValue(InteriorGravityType.UNKNOWN);
        dimensionsKmField.setText("1");
        dryMassMegatonsField.setText("1");
        internalVolumeKm3Field.setText("0");
        interiorPopulationField.setText("0");
        hasInteriorSettingCheck.setSelected(false);
        concealedCheck.setSelected(false);

        sourceTypeCombo.setValue(SourceType.UNKNOWN);
        sourceUniverseField.setText("");
        sourceWorkField.setText("");
        catalogStatusCombo.setValue(CatalogOperationalStatus.UNKNOWN);

        primaryFunctionCombo.setValue(StationFunction.UNKNOWN);
        secondaryFunctionsList.getSelectionModel().clearSelection();
        updateSecondaryHint();
    }

    private Megastructure buildDraft() {
        String id = existing != null ? existing.id() : UUID.randomUUID().toString();
        Instant createdAt = existing != null ? existing.createdAt() : Instant.now();
        Instant now = Instant.now();

        String rawWork = sourceWorkField.getText();
        String sourceWork = rawWork == null ? "" : rawWork.trim();
        if (sourceWork.isEmpty()) {
            sourceWork = null;
        }
        CatalogProvenance provenance = new CatalogProvenance(
                sourceTypeCombo.getValue() == null ? SourceType.UNKNOWN : sourceTypeCombo.getValue(),
                trim(sourceUniverseField.getText()),
                sourceWork,
                catalogStatusCombo.getValue() == null
                        ? CatalogOperationalStatus.UNKNOWN
                        : catalogStatusCombo.getValue());

        StationFunction primaryFunction = primaryFunctionCombo.getValue() == null
                ? StationFunction.UNKNOWN
                : primaryFunctionCombo.getValue();
        Set<StationFunction> secondaryFunctions = Set.copyOf(
                new HashSet<>(secondaryFunctionsList.getSelectionModel().getSelectedItems()));

        Integer discoveryYear = parseIntegerOrNull(discoveryYearField.getText());
        Integer constructionYear = parseIntegerOrNull(constructionYearField.getText());
        String builderPolity = trim(builderPolityField.getText());
        if (builderPolity.isEmpty()) {
            builderPolity = null;
        }
        String designation = trim(designationField.getText());
        String description = trim(descriptionArea.getText());
        String category = trim(categoryField.getText());
        if (category.isEmpty()) {
            category = null;
        }
        String notes = trim(notesArea.getText());
        if (notes.isEmpty()) {
            notes = null;
        }
        String faction = trim(factionField.getText());
        if (faction.isEmpty()) {
            faction = null;
        }
        String allegiance = trim(allegianceField.getText());
        if (allegiance.isEmpty()) {
            allegiance = null;
        }

        return new Megastructure(
                id,
                trim(nameField.getText()),
                designation,
                description.isEmpty() ? null : description,
                category,
                notes,
                archetypeCombo.getValue(),
                parse(dimensionsKmField, 0),
                parse(dryMassMegatonsField, 0),
                parse(internalVolumeKm3Field, 0),
                mobilityCombo.getValue() == null ? Mobility.STATIONKEEPING : mobilityCombo.getValue(),
                auxiliaryDriveCombo.getValue(),
                originTypeCombo.getValue(),
                builderPolity,
                discoveryYear,
                constructionYear,
                primaryFunction,
                secondaryFunctions,
                hasInteriorSettingCheck.isSelected(),
                parseLong(interiorPopulationField, 0),
                interiorGravityCombo.getValue() == null
                        ? InteriorGravityType.UNKNOWN
                        : interiorGravityCombo.getValue(),
                operationalStateCombo.getValue() == null
                        ? OperationalState.OPERATIONAL
                        : operationalStateCombo.getValue(),
                concealedCheck.isSelected(),
                List.copyOf(armaments),
                provenance,
                faction,
                allegiance,
                techLevelCombo.getValue() == null ? TechLevel.UNKNOWN : techLevelCombo.getValue(),
                createdAt,
                now);
    }

    private void addArmament() {
        String name = trim(armNameField.getText());
        WeaponType type = armTypeCombo.getValue();
        if (name.isBlank() || type == null) {
            return;
        }
        int qty = (int) Math.max(0, parse(armQuantityField, 1));
        double power = Math.max(0, parse(armYieldField, 0));
        double range = Math.max(0, parse(armRangeField, 0));
        armaments.add(new Armament(name, type, qty, power, range,
                trim(armRoleField.getText()), trim(armNotesField.getText())));
        armNameField.clear();
        armRoleField.clear();
        armNotesField.clear();
        armQuantityField.setText("1");
        armYieldField.setText("0");
        armRangeField.setText("0");
    }

    // -------------------------------------------------------------------------------- helpers

    private static void annotate(Node n, String accessibleText, String accessibleHelp) {
        if (n == null) {
            return;
        }
        n.setAccessibleText(accessibleText);
        n.setAccessibleHelp(accessibleHelp);
    }

    private static GridPane grid() {
        GridPane g = new GridPane();
        g.setHgap(8);
        g.setVgap(6);
        return g;
    }

    private static void addRow(GridPane g, int row, String label, Node field) {
        Label l = new Label(label);
        l.getStyleClass().add("trips-text-form-label");
        g.add(l, 0, row);
        g.add(field, 1, row);
        if (field instanceof javafx.scene.layout.Region region) {
            region.setMaxWidth(Double.MAX_VALUE);
        }
        GridPane.setHgrow(field, Priority.ALWAYS);
        ButtonBar.setButtonUniformSize(field, false);
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    private static double parse(TextField f, double fallback) {
        try {
            String t = f.getText();
            return (t == null || t.isBlank()) ? fallback : Double.parseDouble(t.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static long parseLong(TextField f, long fallback) {
        try {
            String t = f.getText();
            return (t == null || t.isBlank()) ? fallback : Long.parseLong(t.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static Integer parseIntegerOrNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        if (t.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(t);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // -------------------------------------------------------------------------------- test seams

    ComboBox<MegastructureArchetype> archetypeComboForTesting() {
        return archetypeCombo;
    }

    ComboBox<MegastructureOriginType> originTypeComboForTesting() {
        return originTypeCombo;
    }

    ComboBox<InteriorGravityType> interiorGravityComboForTesting() {
        return interiorGravityCombo;
    }

    ComboBox<Mobility> mobilityComboForTesting() {
        return mobilityCombo;
    }

    ComboBox<DriveType> auxiliaryDriveComboForTesting() {
        return auxiliaryDriveCombo;
    }

    ComboBox<StationFunction> primaryFunctionComboForTesting() {
        return primaryFunctionCombo;
    }

    ListView<StationFunction> secondaryFunctionsListForTesting() {
        return secondaryFunctionsList;
    }

    Label secondaryHintLabelForTesting() {
        return secondaryHintLabel;
    }

    ComboBox<SourceType> sourceTypeComboForTesting() {
        return sourceTypeCombo;
    }

    TextField sourceUniverseFieldForTesting() {
        return sourceUniverseField;
    }

    TextField sourceWorkFieldForTesting() {
        return sourceWorkField;
    }

    ComboBox<CatalogOperationalStatus> catalogStatusComboForTesting() {
        return catalogStatusCombo;
    }

    TextField builderPolityFieldForTesting() {
        return builderPolityField;
    }

    TextField constructionYearFieldForTesting() {
        return constructionYearField;
    }

    TextField discoveryYearFieldForTesting() {
        return discoveryYearField;
    }

    TextField dimensionsKmFieldForTesting() {
        return dimensionsKmField;
    }

    TextField dryMassMegatonsFieldForTesting() {
        return dryMassMegatonsField;
    }

    TextField internalVolumeKm3FieldForTesting() {
        return internalVolumeKm3Field;
    }

    CheckBox hasInteriorSettingCheckForTesting() {
        return hasInteriorSettingCheck;
    }

    TextField interiorPopulationFieldForTesting() {
        return interiorPopulationField;
    }

    Button okButtonForTesting() {
        return okButton;
    }

    ObservableList<Armament> armamentsForTesting() {
        return armaments;
    }
}
