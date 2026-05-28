package com.teamgannon.trips.construct.ui;

import com.teamgannon.trips.spaceshipmodeller.core.CarriedCraft;
import com.teamgannon.trips.spaceshipmodeller.core.ShipClass;
import com.teamgannon.trips.spaceshipmodeller.propulsion.DriveType;
import com.teamgannon.trips.utility.DialogUtils;
import com.terranrepublic.assets.Armament;
import com.terranrepublic.assets.Mobility;
import com.terranrepublic.assets.OperationalState;
import com.terranrepublic.assets.StationDesign;
import com.terranrepublic.assets.StationType;
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
 * Modal editor for a {@link StationDesign}. Mirrors {@code SpaceshipEditorDialog}'s structure:
 * sections built programmatically, controls wired with tooltips + accessibility annotations,
 * validation surfaced via a messages list, OK button disabled while errors are present, and the
 * result converter returns the assembled draft on OK.
 *
 * <p>Domain invariant enforced by the dialog UI: {@code auxiliaryDrive} is only valid when
 * {@code mobility != FIXED}. Switching mobility to FIXED disables the auxiliary-drive combo and
 * clears its value, matching {@link StationDesign}'s compact-constructor invariant.
 */
public class StationEditorDialog extends Dialog<StationDesign> {

    private final StationDesign existing;

    // identity
    private final TextField nameField = new TextField();
    private final TextField designationField = new TextField();
    private final ComboBox<StationType> stationTypeCombo = new ComboBox<>();
    private final TextField sourceField = new TextField();
    private final TextField factionField = new TextField();
    private final TextField allegianceField = new TextField();
    private final CheckBox concealedCheck = new CheckBox();
    private final ComboBox<OperationalState> operationalStateCombo = new ComboBox<>();
    private final TextArea descriptionArea = new TextArea();

    // physical
    private final TextField overallSpanField = new TextField();
    private final TextField interiorSpanField = new TextField();
    private final TextField dryMassField = new TextField();
    private final TextField armourThicknessField = new TextField();
    private final TextField pressurizedVolumeField = new TextField();
    private final TextField hangarVolumeField = new TextField();
    private final CheckBox carrierCapableCheck = new CheckBox();

    // crew
    private final TextField crewCapacityField = new TextField();
    private final TextField crewComplementField = new TextField();

    // mobility
    private final ComboBox<Mobility> mobilityCombo = new ComboBox<>();
    private final ComboBox<DriveType> auxiliaryDriveCombo = new ComboBox<>();

    // carried craft
    private final ObservableList<CarriedCraft> carried = FXCollections.observableArrayList();
    private final TableView<CarriedCraft> carriedTable = new TableView<>(carried);
    private final TextField craftNameField = new TextField();
    private final ComboBox<ShipClass> craftClassCombo = new ComboBox<>();
    private final TextField craftCountField = new TextField("1");
    private final TextField craftMassField = new TextField("0");
    private final TextField craftRoleField = new TextField();

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

    // tech
    private final ComboBox<TechLevel> techLevelCombo = new ComboBox<>();
    private final TextField categoryField = new TextField();

    // validation
    private final Label statusLabel = new Label();
    private final ListView<String> messagesList = new ListView<>();
    private Button okButton;

    public StationEditorDialog() {
        this(null);
    }

    public StationEditorDialog(StationDesign existing) {
        this.existing = existing;
        setTitle(get(existing == null ? "editor.station.title.new" : "editor.station.title.edit"));
        getDialogPane().getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
        okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);

        stationTypeCombo.getItems().setAll(StationType.values());
        mobilityCombo.getItems().setAll(Mobility.values());
        auxiliaryDriveCombo.getItems().setAll(DriveType.values());
        operationalStateCombo.getItems().setAll(OperationalState.values());
        operationalStateCombo.setConverter(operationalStateConverter());
        techLevelCombo.getItems().setAll(TechLevel.values());
        craftClassCombo.getItems().setAll(ShipClass.values());
        armTypeCombo.getItems().setAll(WeaponType.values());

        buildContent();
        DialogUtils.applyTheme(this);
        configureAccessibility();
        wireMobilityInvariant();
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
                section(get("editor.station.section.basic"), basicGrid()),
                section(get("editor.station.section.physical"), physicalGrid()),
                section(get("editor.station.section.crew"), crewGrid()),
                section(get("editor.station.section.mobility"), mobilityGrid()),
                section(get("editor.station.section.carried"), carriedSection()),
                section(get("editor.station.section.armaments"), armamentSection()),
                section(get("editor.station.section.tech"), techGrid()),
                section(get("editor.station.section.validation"), validationSection()));

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
        descriptionArea.setPrefRowCount(3);
        int r = 0;
        addRow(g, r++, get("editor.field.name"), nameField);
        addRow(g, r++, get("editor.field.designation"), designationField);
        addRow(g, r++, get("editor.station.field.stationType"), stationTypeCombo);
        addRow(g, r++, get("editor.field.source"), sourceField);
        addRow(g, r++, get("editor.field.faction"), factionField);
        addRow(g, r++, get("editor.field.allegiance"), allegianceField);
        addRow(g, r++, get("editor.field.concealed"), concealedCheck);
        addRow(g, r++, get("editor.field.operationalState"), operationalStateCombo);
        addRow(g, r++, get("editor.field.description"), descriptionArea);
        return g;
    }

    private GridPane physicalGrid() {
        GridPane g = grid();
        int r = 0;
        addRow(g, r++, get("editor.station.field.overallSpan"), overallSpanField);
        addRow(g, r++, get("editor.station.field.interiorSpan"), interiorSpanField);
        addRow(g, r++, get("editor.station.field.dryMass"), dryMassField);
        addRow(g, r++, get("editor.station.field.armourThickness"), armourThicknessField);
        addRow(g, r++, get("editor.station.field.pressurizedVolume"), pressurizedVolumeField);
        addRow(g, r++, get("editor.station.field.hangarVolume"), hangarVolumeField);
        addRow(g, r++, get("editor.station.field.carrierCapable"), carrierCapableCheck);
        return g;
    }

    private GridPane crewGrid() {
        GridPane g = grid();
        int r = 0;
        addRow(g, r++, get("editor.station.field.crewCapacity"), crewCapacityField);
        addRow(g, r++, get("editor.station.field.crewComplement"), crewComplementField);
        return g;
    }

    private GridPane mobilityGrid() {
        GridPane g = grid();
        int r = 0;
        addRow(g, r++, get("editor.station.field.mobility"), mobilityCombo);
        addRow(g, r++, get("editor.station.field.auxiliaryDrive"), auxiliaryDriveCombo);
        return g;
    }

    private GridPane techGrid() {
        GridPane g = grid();
        int r = 0;
        addRow(g, r++, get("editor.station.field.techLevel"), techLevelCombo);
        addRow(g, r++, get("editor.station.field.category"), categoryField);
        return g;
    }

    @SuppressWarnings("unchecked")
    private VBox carriedSection() {
        TableColumn<CarriedCraft, String> nameCol = new TableColumn<>(get("editor.carried.column.name"));
        nameCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().name()));
        nameCol.setPrefWidth(150);
        TableColumn<CarriedCraft, ShipClass> classCol = new TableColumn<>(get("editor.carried.column.class"));
        classCol.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().shipClass()));
        TableColumn<CarriedCraft, Number> countCol = new TableColumn<>(get("editor.carried.column.count"));
        countCol.setCellValueFactory(cd -> new SimpleIntegerProperty(cd.getValue().count()));
        TableColumn<CarriedCraft, String> roleCol = new TableColumn<>(get("editor.carried.column.role"));
        roleCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().role()));
        roleCol.setPrefWidth(150);
        carriedTable.getColumns().setAll(List.of(nameCol, classCol, countCol, roleCol));
        carriedTable.setPrefHeight(120);
        carriedTable.setPlaceholder(new Label(get("editor.carried.placeholder")));
        carriedTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        craftNameField.setPromptText(get("editor.carried.column.name"));
        craftRoleField.setPromptText(get("editor.carried.column.role"));
        craftCountField.setPrefWidth(60);
        craftMassField.setPrefWidth(80);

        Button addBtn = new Button(get("editor.button.add"));
        addBtn.setOnAction(e -> addCarried());
        Button removeBtn = new Button(get("editor.button.remove"));
        removeBtn.setOnAction(e -> {
            CarriedCraft sel = carriedTable.getSelectionModel().getSelectedItem();
            if (sel != null) {
                carried.remove(sel);
                revalidate();
            }
        });

        HBox addForm = new HBox(6, craftNameField, craftClassCombo,
                new Label("x"), craftCountField,
                new Label("t"), craftMassField,
                craftRoleField, addBtn, removeBtn);
        HBox.setHgrow(craftNameField, Priority.ALWAYS);
        return new VBox(6, carriedTable, addForm);
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
                ? get("editor.station.title.new")
                : get("editor.station.title.edit"));
        annotate(okButton, get("editor.button.ok"), "Save this station when validation passes.");
        Button cancel = (Button) getDialogPane().lookupButton(ButtonType.CANCEL);
        annotate(cancel, get("editor.button.cancel"), "Discard changes and close.");

        annotate(nameField, get("editor.field.name"), "Required display name.");
        annotate(designationField, get("editor.field.designation"), "Optional hull or fleet designation.");
        annotate(stationTypeCombo, get("editor.station.field.stationType"),
                get("editor.station.tooltip.stationType"));
        stationTypeCombo.setTooltip(new Tooltip(get("editor.station.tooltip.stationType")));
        annotate(sourceField, get("editor.field.source"), "Free text source or universe label.");
        annotate(factionField, get("editor.field.faction"), "Owning faction or polity.");
        annotate(allegianceField, get("editor.field.allegiance"), get("editor.tooltip.allegiance"));
        allegianceField.setTooltip(new Tooltip(get("editor.tooltip.allegiance")));
        annotate(concealedCheck, get("editor.field.concealed"), get("editor.tooltip.concealed"));
        concealedCheck.setTooltip(new Tooltip(get("editor.tooltip.concealed")));
        annotate(operationalStateCombo, get("editor.field.operationalState"),
                get("editor.tooltip.operationalState"));
        operationalStateCombo.setTooltip(new Tooltip(get("editor.tooltip.operationalState")));
        annotate(descriptionArea, get("editor.field.description"), "Free-form notes.");

        annotate(overallSpanField, get("editor.station.field.overallSpan"), "Overall length in metres.");
        annotate(interiorSpanField, get("editor.station.field.interiorSpan"), "Interior cavity span in metres.");
        annotate(dryMassField, get("editor.station.field.dryMass"), "Dry mass in tonnes.");
        annotate(armourThicknessField, get("editor.station.field.armourThickness"), "Armour thickness in metres.");
        annotate(pressurizedVolumeField, get("editor.station.field.pressurizedVolume"),
                "Pressurized volume in cubic metres.");
        annotate(hangarVolumeField, get("editor.station.field.hangarVolume"), "Hangar volume in cubic metres.");
        annotate(carrierCapableCheck, get("editor.station.field.carrierCapable"),
                "Whether this station carries embarked craft.");

        annotate(crewCapacityField, get("editor.station.field.crewCapacity"), "Maximum crew aboard.");
        annotate(crewComplementField, get("editor.station.field.crewComplement"), "Typical crew complement.");

        annotate(mobilityCombo, get("editor.station.field.mobility"), get("editor.station.tooltip.mobility"));
        mobilityCombo.setTooltip(new Tooltip(get("editor.station.tooltip.mobility")));
        annotate(auxiliaryDriveCombo, get("editor.station.field.auxiliaryDrive"),
                get("editor.station.tooltip.auxiliaryDrive"));
        auxiliaryDriveCombo.setTooltip(new Tooltip(get("editor.station.tooltip.auxiliaryDrive")));

        annotate(techLevelCombo, get("editor.station.field.techLevel"), "Coarse technology band.");
        annotate(categoryField, get("editor.station.field.category"), "Free-form designer category label.");
    }

    private void wireMobilityInvariant() {
        mobilityCombo.valueProperty().addListener((o, oldVal, newVal) -> {
            applyMobilityInvariant();
            revalidate();
        });
        applyMobilityInvariant();
    }

    private void applyMobilityInvariant() {
        boolean isFixed = mobilityCombo.getValue() == Mobility.FIXED || mobilityCombo.getValue() == null;
        if (isFixed) {
            auxiliaryDriveCombo.setValue(null);
            auxiliaryDriveCombo.setDisable(true);
        } else {
            auxiliaryDriveCombo.setDisable(false);
        }
    }

    private void wireValidation() {
        nameField.textProperty().addListener((o, a, b) -> revalidate());
        stationTypeCombo.valueProperty().addListener((o, a, b) -> revalidate());
        mobilityCombo.valueProperty().addListener((o, a, b) -> revalidate());
        dryMassField.textProperty().addListener((o, a, b) -> revalidate());
        carried.addListener((javafx.collections.ListChangeListener<CarriedCraft>) c -> revalidate());
        armaments.addListener((javafx.collections.ListChangeListener<Armament>) c -> revalidate());
    }

    private void revalidate() {
        List<String> errors = new ArrayList<>();
        if (nameField.getText() == null || nameField.getText().isBlank()) {
            errors.add(get("validation.nameRequired"));
        }
        if (stationTypeCombo.getValue() == null) {
            errors.add(get("validation.stationTypeRequired"));
        }
        if (mobilityCombo.getValue() == null) {
            errors.add(get("validation.mobilityRequired"));
        }
        double dryMass = parse(dryMassField, -1);
        if (dryMass <= 0) {
            errors.add(get("validation.dryMassPositive"));
        }
        if (parse(overallSpanField, 0) < 0 || parse(interiorSpanField, 0) < 0
                || parse(armourThicknessField, 0) < 0 || parse(pressurizedVolumeField, 0) < 0
                || parse(hangarVolumeField, 0) < 0
                || (int) parse(crewCapacityField, 0) < 0 || (int) parse(crewComplementField, 0) < 0) {
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

    private void populateFrom(StationDesign d) {
        nameField.setText(d.name());
        designationField.setText(d.designation());
        stationTypeCombo.setValue(d.stationType());
        sourceField.setText(d.source());
        factionField.setText(d.faction());
        allegianceField.setText(d.allegiance());
        concealedCheck.setSelected(d.concealed());
        operationalStateCombo.setValue(d.operationalState() == null
                ? OperationalState.OPERATIONAL : d.operationalState());
        descriptionArea.setText(d.description());

        overallSpanField.setText(Double.toString(d.overallSpanMeters()));
        interiorSpanField.setText(Double.toString(d.interiorSpanMeters()));
        dryMassField.setText(Double.toString(d.dryMassTons()));
        armourThicknessField.setText(Double.toString(d.armourThicknessMeters()));
        pressurizedVolumeField.setText(Double.toString(d.pressurizedVolumeM3()));
        hangarVolumeField.setText(Double.toString(d.hangarVolumeM3()));
        carrierCapableCheck.setSelected(d.carrierCapable());

        crewCapacityField.setText(Integer.toString(d.crewCapacity()));
        crewComplementField.setText(Integer.toString(d.crewComplement()));

        mobilityCombo.setValue(d.mobility());
        auxiliaryDriveCombo.setValue(d.auxiliaryDrive());
        applyMobilityInvariant();

        carried.setAll(d.carriedCraft());
        armaments.setAll(d.armaments());

        techLevelCombo.setValue(d.techLevel());
        categoryField.setText(d.category());
    }

    private void applyDefaults() {
        nameField.setText("");
        stationTypeCombo.setValue(StationType.OUTPOST);
        mobilityCombo.setValue(Mobility.FIXED);
        operationalStateCombo.setValue(OperationalState.OPERATIONAL);
        techLevelCombo.setValue(TechLevel.UNKNOWN);
        dryMassField.setText("100");
        overallSpanField.setText("0");
        interiorSpanField.setText("0");
        armourThicknessField.setText("0");
        pressurizedVolumeField.setText("0");
        hangarVolumeField.setText("0");
        crewCapacityField.setText("0");
        crewComplementField.setText("0");
        applyMobilityInvariant();
    }

    private StationDesign buildDraft() {
        String id = existing != null ? existing.id() : UUID.randomUUID().toString();
        Instant createdAt = existing != null ? existing.createdAt() : Instant.now();
        Instant now = Instant.now();
        return new StationDesign(
                id,
                trim(nameField.getText()),
                trim(designationField.getText()),
                stationTypeCombo.getValue(),
                trim(sourceField.getText()),
                trim(factionField.getText()),
                concealedCheck.isSelected(),
                trim(allegianceField.getText()),
                trim(descriptionArea.getText()),
                parse(overallSpanField, 0),
                parse(interiorSpanField, 0),
                parse(dryMassField, 0),
                parse(armourThicknessField, 0),
                (int) parse(crewCapacityField, 0),
                (int) parse(crewComplementField, 0),
                parse(pressurizedVolumeField, 0),
                mobilityCombo.getValue(),
                mobilityCombo.getValue() == Mobility.FIXED ? null : auxiliaryDriveCombo.getValue(),
                List.copyOf(carried),
                List.copyOf(armaments),
                parse(hangarVolumeField, 0),
                carrierCapableCheck.isSelected(),
                techLevelCombo.getValue() == null ? TechLevel.UNKNOWN : techLevelCombo.getValue(),
                trim(categoryField.getText()),
                operationalStateCombo.getValue() == null
                        ? OperationalState.OPERATIONAL : operationalStateCombo.getValue(),
                createdAt,
                now);
    }

    // -------------------------------------------------------------------------------- carried craft + armament adders

    private void addCarried() {
        String name = trim(craftNameField.getText());
        ShipClass cls = craftClassCombo.getValue();
        if (name.isBlank() || cls == null) {
            return;
        }
        int count = (int) Math.max(1, parse(craftCountField, 1));
        double mass = Math.max(0, parse(craftMassField, 0));
        carried.add(new CarriedCraft(name, cls, count, mass, trim(craftRoleField.getText())));
        craftNameField.clear();
        craftRoleField.clear();
        craftCountField.setText("1");
        craftMassField.setText("0");
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

    // -------------------------------------------------------------------------------- test seam

    /** Test access to the auxiliary drive combo so headless tests can assert the mobility invariant. */
    ComboBox<DriveType> auxiliaryDriveComboForTesting() {
        return auxiliaryDriveCombo;
    }

    ComboBox<Mobility> mobilityComboForTesting() {
        return mobilityCombo;
    }

    Button okButtonForTesting() {
        return okButton;
    }
}
