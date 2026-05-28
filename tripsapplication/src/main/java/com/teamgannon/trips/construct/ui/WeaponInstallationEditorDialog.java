package com.teamgannon.trips.construct.ui;

import com.teamgannon.trips.utility.DialogUtils;
import com.terranrepublic.assets.Armament;
import com.terranrepublic.assets.Emplacement;
import com.terranrepublic.assets.InstallationType;
import com.terranrepublic.assets.OperationalState;
import com.terranrepublic.assets.TechLevel;
import com.terranrepublic.assets.WeaponInstallation;
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
import java.util.List;
import java.util.UUID;

import static com.teamgannon.trips.construct.ui.ConstructLabels.get;

/**
 * Modal editor for a {@link WeaponInstallation}. Independent class — no shared base with
 * {@link StationEditorDialog} per the Phase D scope (premature-abstraction guard). Mirrors the
 * same internal patterns: programmatic sections, validation list, OK disabled while invalid,
 * canonical-record-constructor draft.
 */
public class WeaponInstallationEditorDialog extends Dialog<WeaponInstallation> {

    private final WeaponInstallation existing;

    // identity
    private final TextField nameField = new TextField();
    private final TextField designationField = new TextField();
    private final TextField sourceField = new TextField();
    private final TextField factionField = new TextField();
    private final CheckBox concealedCheck = new CheckBox();
    private final ComboBox<OperationalState> operationalStateCombo = new ComboBox<>();
    private final TextArea descriptionArea = new TextArea();

    // classification
    private final ComboBox<InstallationType> installationTypeCombo = new ComboBox<>();
    private final ComboBox<Emplacement> emplacementCombo = new ComboBox<>();
    private final CheckBox mobileCheck = new CheckBox();
    private final ComboBox<TechLevel> techLevelCombo = new ComboBox<>();
    private final TextField categoryField = new TextField();

    // physical
    private final TextField dryMassField = new TextField();
    private final TextField footprintSpanField = new TextField();

    // crew
    private final TextField crewComplementField = new TextField();

    // armaments
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

    public WeaponInstallationEditorDialog() {
        this(null);
    }

    public WeaponInstallationEditorDialog(WeaponInstallation existing) {
        this.existing = existing;
        setTitle(get(existing == null ? "editor.weapon.title.new" : "editor.weapon.title.edit"));
        getDialogPane().getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
        okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);

        installationTypeCombo.getItems().setAll(InstallationType.values());
        emplacementCombo.getItems().setAll(Emplacement.values());
        operationalStateCombo.getItems().setAll(OperationalState.values());
        operationalStateCombo.setConverter(operationalStateConverter());
        techLevelCombo.getItems().setAll(TechLevel.values());
        armTypeCombo.getItems().setAll(WeaponType.values());

        buildContent();
        DialogUtils.applyTheme(this);
        configureAccessibility();
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

    private void buildContent() {
        VBox root = new VBox(12);
        root.setPadding(new Insets(14));
        root.getChildren().addAll(
                section(get("editor.weapon.section.basic"), basicGrid()),
                section(get("editor.weapon.section.classification"), classificationGrid()),
                section(get("editor.weapon.section.physical"), physicalGrid()),
                section(get("editor.weapon.section.crew"), crewGrid()),
                section(get("editor.weapon.section.armaments"), armamentSection()),
                section(get("editor.weapon.section.validation"), validationSection()));

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.setPrefViewportHeight(620);
        getDialogPane().setContent(scroll);
        getDialogPane().setPrefWidth(700);
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
        descriptionArea.setPrefRowCount(3);
        int r = 0;
        addRow(g, r++, get("editor.field.name"), nameField);
        addRow(g, r++, get("editor.field.designation"), designationField);
        addRow(g, r++, get("editor.field.source"), sourceField);
        addRow(g, r++, get("editor.field.faction"), factionField);
        addRow(g, r++, get("editor.field.concealed"), concealedCheck);
        addRow(g, r++, get("editor.field.operationalState"), operationalStateCombo);
        addRow(g, r++, get("editor.field.description"), descriptionArea);
        return g;
    }

    private GridPane classificationGrid() {
        GridPane g = grid();
        int r = 0;
        addRow(g, r++, get("editor.weapon.field.installationType"), installationTypeCombo);
        addRow(g, r++, get("editor.weapon.field.emplacement"), emplacementCombo);
        addRow(g, r++, get("editor.weapon.field.mobile"), mobileCheck);
        addRow(g, r++, get("editor.weapon.field.techLevel"), techLevelCombo);
        addRow(g, r++, get("editor.weapon.field.category"), categoryField);
        return g;
    }

    private GridPane physicalGrid() {
        GridPane g = grid();
        int r = 0;
        addRow(g, r++, get("editor.weapon.field.dryMass"), dryMassField);
        addRow(g, r++, get("editor.weapon.field.footprintSpan"), footprintSpanField);
        return g;
    }

    private GridPane crewGrid() {
        GridPane g = grid();
        int r = 0;
        addRow(g, r++, get("editor.weapon.field.crewComplement"), crewComplementField);
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
                ? get("editor.weapon.title.new")
                : get("editor.weapon.title.edit"));

        annotate(nameField, get("editor.field.name"), "Required display name.");
        annotate(designationField, get("editor.field.designation"), "Optional designation.");
        annotate(sourceField, get("editor.field.source"), "Free text source or universe label.");
        annotate(factionField, get("editor.field.faction"), "Owning faction or polity.");
        annotate(concealedCheck, get("editor.field.concealed"), get("editor.tooltip.concealed"));
        concealedCheck.setTooltip(new Tooltip(get("editor.tooltip.concealed")));
        annotate(operationalStateCombo, get("editor.field.operationalState"),
                get("editor.tooltip.operationalState"));
        operationalStateCombo.setTooltip(new Tooltip(get("editor.tooltip.operationalState")));
        annotate(descriptionArea, get("editor.field.description"), "Free-form notes.");

        annotate(installationTypeCombo, get("editor.weapon.field.installationType"),
                "Installation class (beam array, super cannon, defence battery, missile field).");
        installationTypeCombo.setTooltip(new Tooltip(get("editor.weapon.field.installationType")));
        annotate(emplacementCombo, get("editor.weapon.field.emplacement"),
                "Where and how the installation is mounted.");
        emplacementCombo.setTooltip(new Tooltip(get("editor.weapon.field.emplacement")));
        annotate(mobileCheck, get("editor.weapon.field.mobile"), get("editor.weapon.tooltip.mobile"));
        mobileCheck.setTooltip(new Tooltip(get("editor.weapon.tooltip.mobile")));
        annotate(techLevelCombo, get("editor.weapon.field.techLevel"), "Coarse technology band.");
        annotate(categoryField, get("editor.weapon.field.category"), "Free-form designer category label.");

        annotate(dryMassField, get("editor.weapon.field.dryMass"), "Dry mass in tonnes.");
        annotate(footprintSpanField, get("editor.weapon.field.footprintSpan"),
                "Footprint span in metres.");
        annotate(crewComplementField, get("editor.weapon.field.crewComplement"),
                "Typical crew complement.");
    }

    private void wireValidation() {
        nameField.textProperty().addListener((o, a, b) -> revalidate());
        installationTypeCombo.valueProperty().addListener((o, a, b) -> revalidate());
        emplacementCombo.valueProperty().addListener((o, a, b) -> revalidate());
        dryMassField.textProperty().addListener((o, a, b) -> revalidate());
        armaments.addListener((javafx.collections.ListChangeListener<Armament>) c -> revalidate());
    }

    private void revalidate() {
        List<String> errors = new ArrayList<>();
        if (nameField.getText() == null || nameField.getText().isBlank()) {
            errors.add(get("validation.nameRequired"));
        }
        if (installationTypeCombo.getValue() == null) {
            errors.add(get("validation.installationTypeRequired"));
        }
        if (emplacementCombo.getValue() == null) {
            errors.add(get("validation.emplacementRequired"));
        }
        double dryMass = parse(dryMassField, -1);
        if (dryMass <= 0) {
            errors.add(get("validation.dryMassPositive"));
        }
        if (parse(footprintSpanField, 0) < 0 || (int) parse(crewComplementField, 0) < 0) {
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

    private void populateFrom(WeaponInstallation w) {
        nameField.setText(w.name());
        designationField.setText(w.designation());
        sourceField.setText(w.source());
        factionField.setText(w.faction());
        concealedCheck.setSelected(w.concealed());
        operationalStateCombo.setValue(w.operationalState() == null
                ? OperationalState.OPERATIONAL : w.operationalState());
        descriptionArea.setText(w.description());

        installationTypeCombo.setValue(w.installationType());
        emplacementCombo.setValue(w.emplacement());
        mobileCheck.setSelected(w.mobile());
        techLevelCombo.setValue(w.techLevel());
        categoryField.setText(w.category());

        dryMassField.setText(Double.toString(w.dryMassTons()));
        footprintSpanField.setText(Double.toString(w.footprintSpanMeters()));
        crewComplementField.setText(Integer.toString(w.crewComplement()));

        armaments.setAll(w.armaments());
    }

    private void applyDefaults() {
        installationTypeCombo.setValue(InstallationType.DEFENCE_BATTERY);
        emplacementCombo.setValue(Emplacement.GROUND_FIXED);
        operationalStateCombo.setValue(OperationalState.OPERATIONAL);
        techLevelCombo.setValue(TechLevel.UNKNOWN);
        dryMassField.setText("100");
        footprintSpanField.setText("0");
        crewComplementField.setText("0");
    }

    private WeaponInstallation buildDraft() {
        String id = existing != null ? existing.id() : UUID.randomUUID().toString();
        Instant createdAt = existing != null ? existing.createdAt() : Instant.now();
        Instant now = Instant.now();
        return new WeaponInstallation(
                id,
                trim(nameField.getText()),
                trim(designationField.getText()),
                installationTypeCombo.getValue(),
                emplacementCombo.getValue(),
                trim(sourceField.getText()),
                trim(factionField.getText()),
                concealedCheck.isSelected(),
                trim(descriptionArea.getText()),
                parse(dryMassField, 0),
                parse(footprintSpanField, 0),
                mobileCheck.isSelected(),
                (int) parse(crewComplementField, 0),
                List.copyOf(armaments),
                techLevelCombo.getValue() == null ? TechLevel.UNKNOWN : techLevelCombo.getValue(),
                trim(categoryField.getText()),
                operationalStateCombo.getValue() == null
                        ? OperationalState.OPERATIONAL : operationalStateCombo.getValue(),
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

    Button okButtonForTesting() {
        return okButton;
    }
}
