package com.teamgannon.trips.spaceshipmodeller.ui;

import com.teamgannon.trips.spaceshipmodeller.builder.SpaceshipBuilder;
import com.teamgannon.trips.spaceshipmodeller.core.CarriedCraft;
import com.teamgannon.trips.spaceshipmodeller.core.ShipClass;
import com.teamgannon.trips.spaceshipmodeller.core.SourceType;
import com.teamgannon.trips.spaceshipmodeller.integration.TransferPlannerBridge;
import com.teamgannon.trips.spaceshipmodeller.propulsion.DriveType;
import com.teamgannon.trips.spaceshipmodeller.rules.ValidationEngine;
import com.teamgannon.trips.spaceshipmodeller.rules.ValidationMessage;
import com.teamgannon.trips.spaceshipmodeller.rules.ValidationResult;
import com.teamgannon.trips.utility.DialogUtils;
import com.terranrepublic.assets.OperationalState;
import com.terranrepublic.assets.SpaceshipDesign;
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

import java.util.List;

import static com.teamgannon.trips.spaceshipmodeller.ui.SpaceshipModellerLabels.get;
import static com.teamgannon.trips.spaceshipmodeller.ui.SpaceshipModellerLabels.getDouble;
import static com.teamgannon.trips.spaceshipmodeller.ui.SpaceshipModellerLabels.getInt;

/**
 * Modal create/edit dialog for a {@link SpaceshipDesign}.
 * <p>
 * Built programmatically (matching the TRIPS dialog convention of extending {@link Dialog} and returning a
 * result via the result converter). Fields are grouped into sections — basic info, propulsion, mass budget,
 * carried craft and a live validation panel. The {@code OK} button is disabled whenever the current draft
 * has validation errors, so an invalid design can never be saved; warnings are allowed.
 * <p>
 * When editing, the original {@code id} and creation timestamp are preserved; assembly otherwise goes
 * through {@link SpaceshipBuilder}.
 */
public class SpaceshipEditorDialog extends Dialog<SpaceshipDesign> {

    private final SpaceshipDesign existing;
    private final List<SpaceshipDesign> templates;
    private final TransferPlannerBridge transferPlannerBridge;
    private final ValidationEngine validationEngine = new ValidationEngine();

    private final ComboBox<SpaceshipDesign> templateCombo = new ComboBox<>();

    // basic
    private final TextField nameField = new TextField();
    private final TextField designationField = new TextField();
    private final ComboBox<ShipClass> classCombo = new ComboBox<>();
    private final ComboBox<DriveType> driveCombo = new ComboBox<>();
    private final TextField crewField = new TextField();
    private final TextField lengthField = new TextField();
    private final TextField iconField = new TextField();
    private final TextArea descriptionArea = new TextArea();
    private final ComboBox<SourceType> sourceTypeCombo = new ComboBox<>();
    private final TextField universeField = new TextField();
    private final TextField factionField = new TextField();
    private final TextField eraField = new TextField();
    private final CheckBox concealedCheck = new CheckBox();
    private final ComboBox<OperationalState> operationalStateCombo = new ComboBox<>();

    // mass budget
    private final TextField structureField = new TextField();
    private final TextField engineField = new TextField();
    private final TextField propellantField = new TextField();
    private final TextField payloadField = new TextField();
    private final TextField crewMassField = new TextField();
    private final TextField radiatorField = new TextField();

    // carried craft
    private final ObservableList<CarriedCraft> carried = FXCollections.observableArrayList();
    private final TableView<CarriedCraft> carriedTable = new TableView<>(carried);
    private final TextField craftNameField = new TextField();
    private final ComboBox<ShipClass> craftClassCombo = new ComboBox<>();
    private final TextField craftCountField = new TextField("1");
    private final TextField craftMassField = new TextField("0");
    private final TextField craftRoleField = new TextField();

    // validation panel
    private final Label statusLabel = new Label();
    private final Label deltaVBadge = new Label();
    private final ListView<String> messagesList = new ListView<>();
    private final Button planButton = new Button(get("button.planTransfer"));

    private Button okButton;

    /**
     * @param existing the design to edit, or {@code null} to create a new one
     */
    public SpaceshipEditorDialog(SpaceshipDesign existing) {
        this(existing, List.of(), null);
    }

    /**
     * @param existing              the design to edit, or {@code null} to create a new one
     * @param templates             templates offered in the "New from Template" picker (new designs only)
     * @param transferPlannerBridge bridge for the "Plan Mission Transfer" button (may be {@code null})
     */
    public SpaceshipEditorDialog(SpaceshipDesign existing,
                                 List<SpaceshipDesign> templates,
                                 TransferPlannerBridge transferPlannerBridge) {
        this.existing = existing;
        this.templates = templates == null ? List.of() : templates;
        this.transferPlannerBridge = transferPlannerBridge;

        setTitle(existing == null ? get("editor.title.new") : get("editor.title.edit"));
        getDialogPane().getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
        okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);

        classCombo.getItems().setAll(ShipClass.values());
        driveCombo.getItems().setAll(DriveType.values());
        craftClassCombo.getItems().setAll(ShipClass.values());
        sourceTypeCombo.getItems().setAll(SourceType.values());
        sourceTypeCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(SourceType t) {
                return t == null ? "" : sourceTypeLabel(t);
            }

            @Override
            public SourceType fromString(String s) {
                return null;
            }
        });

        operationalStateCombo.getItems().setAll(OperationalState.values());
        operationalStateCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(OperationalState s) {
                return s == null ? "" : operationalStateLabel(s);
            }

            @Override
            public OperationalState fromString(String s) {
                return null;
            }
        });
        concealedCheck.setTooltip(new Tooltip(get("editor.tooltip.concealed")));
        operationalStateCombo.setTooltip(new Tooltip(get("editor.tooltip.operationalState")));

        buildContent();
        DialogUtils.applyTheme(this);
        configureAccessibility();
        if (existing != null) {
            populateFrom(existing);
        } else {
            applyDefaults();
        }
        wireValidation();
        revalidate();

        setResultConverter(buttonType -> buttonType == ButtonType.OK ? buildDraft() : null);
    }

    private void buildContent() {
        VBox root = new VBox(12);
        root.setPadding(new Insets(14));
        if (existing == null && !templates.isEmpty()) {
            root.getChildren().add(section(get("editor.section.template"), templateRow()));
        }
        root.getChildren().addAll(
                section(get("editor.section.basic"), basicGrid()),
                section(get("editor.section.drive"), driveGrid()),
                section(get("editor.section.mass"), massGrid()),
                section(get("editor.section.carried"), carriedSection()),
                section(get("editor.section.validation"), validationSection()));

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.setPrefViewportHeight(640);
        getDialogPane().setContent(scroll);
        getDialogPane().setPrefWidth(680);
    }

    private VBox section(String title, Node body) {
        Label header = new Label(title);
        header.setFont(Font.font(header.getFont().getFamily(), 14));
        header.getStyleClass().add("trips-bold"); // Issue 50 / Bucket A
        VBox box = new VBox(6, header, body);
        box.setPadding(new Insets(4, 0, 4, 0));
        return box;
    }

    private GridPane basicGrid() {
        GridPane g = grid();
        descriptionArea.setPrefRowCount(3);
        addRow(g, 0, get("editor.field.name"), nameField);
        addRow(g, 1, get("editor.field.designation"), designationField);
        addRow(g, 2, get("editor.field.class"), classCombo);
        addRow(g, 3, get("editor.field.crew"), crewField);
        addRow(g, 4, get("editor.field.length"), lengthField);
        addRow(g, 5, get("editor.field.icon"), iconField);
        addRow(g, 6, get("editor.field.sourceType"), sourceTypeCombo);
        addRow(g, 7, get("editor.field.sourceUniverse"), universeField);
        addRow(g, 8, get("editor.field.faction"), factionField);
        addRow(g, 9, get("editor.field.era"), eraField);
        addRow(g, 10, get("editor.field.concealed"), concealedCheck);
        addRow(g, 11, get("editor.field.operationalState"), operationalStateCombo);
        addRow(g, 12, get("editor.field.description"), descriptionArea);
        return g;
    }

    private GridPane driveGrid() {
        GridPane g = grid();
        addRow(g, 0, get("editor.field.drive"), driveCombo);
        return g;
    }

    private GridPane massGrid() {
        GridPane g = grid();
        addRow(g, 0, get("editor.mass.structure"), structureField);
        addRow(g, 1, get("editor.mass.engine"), engineField);
        addRow(g, 2, get("editor.mass.propellant"), propellantField);
        addRow(g, 3, get("editor.mass.payload"), payloadField);
        addRow(g, 4, get("editor.mass.crew"), crewMassField);
        addRow(g, 5, get("editor.mass.radiator"), radiatorField);
        return g;
    }

    private VBox carriedSection() {
        TableColumn<CarriedCraft, String> nameCol = new TableColumn<>(get("column.name"));
        nameCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().name()));
        nameCol.setPrefWidth(150);
        TableColumn<CarriedCraft, ShipClass> classCol = new TableColumn<>(get("column.class"));
        classCol.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().shipClass()));
        TableColumn<CarriedCraft, Number> countCol = new TableColumn<>("Count");
        countCol.setCellValueFactory(cd -> new SimpleIntegerProperty(cd.getValue().count()));
        TableColumn<CarriedCraft, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().role()));
        roleCol.setPrefWidth(150);
        carriedTable.getColumns().setAll(java.util.List.of(nameCol, classCol, countCol, roleCol));
        carriedTable.setPrefHeight(120);
        carriedTable.setPlaceholder(new Label(get("editor.carriedTable.placeholder", "No carried craft")));
        // Issue 34: last column (Role) flexes to fill leftover space on resize.
        carriedTable.setColumnResizePolicy(javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        craftNameField.setPromptText(get("column.name"));
        craftRoleField.setPromptText("Role");
        craftCountField.setPrefWidth(60);
        craftMassField.setPrefWidth(80);

        Button addBtn = new Button(get("editor.carried.add"));
        addBtn.setOnAction(e -> addCarried());
        annotate(addBtn, get("editor.carried.add"), "Add the carried craft row to this design.");
        Button removeBtn = new Button(get("editor.carried.remove"));
        removeBtn.setOnAction(e -> {
            CarriedCraft sel = carriedTable.getSelectionModel().getSelectedItem();
            if (sel != null) {
                carried.remove(sel);
                revalidate();
            }
        });
        annotate(removeBtn, get("editor.carried.remove"), "Remove the selected carried craft row.");

        HBox addForm = new HBox(6,
                craftNameField, craftClassCombo,
                new Label("x"), craftCountField,
                new Label("t"), craftMassField,
                craftRoleField, addBtn, removeBtn);
        HBox.setHgrow(craftNameField, Priority.ALWAYS);

        return new VBox(6, carriedTable, addForm);
    }

    private VBox validationSection() {
        statusLabel.getStyleClass().add("trips-bold"); // Issue 50 / Bucket A
        deltaVBadge.getStyleClass().add("trips-text-form-label");
        messagesList.setPrefHeight(120);
        messagesList.setPlaceholder(new Label(get("editor.messages.empty", "No issues")));

        planButton.setOnAction(e -> {
            if (transferPlannerBridge != null) {
                SpaceshipDesign draft = buildDraft();
                if (draft != null) {
                    new TransferPreviewDialog(draft, transferPlannerBridge).showAndWait();
                }
            }
        });
        planButton.setVisible(transferPlannerBridge != null);
        planButton.setManaged(transferPlannerBridge != null);

        HBox statusRow = new HBox(12, statusLabel, deltaVBadge);
        return new VBox(6, statusRow, new HBox(8, planButton), messagesList);
    }

    private HBox templateRow() {
        templateCombo.getItems().setAll(templates);
        templateCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(SpaceshipDesign d) {
                return d == null ? "" : d.name();
            }

            @Override
            public SpaceshipDesign fromString(String s) {
                return null;
            }
        });
        templateCombo.setPromptText(get("editor.template.prompt"));
        templateCombo.valueProperty().addListener((o, a, b) -> {
            if (b != null) {
                populateFrom(b);
                revalidate();
            }
        });
        return new HBox(8, new Label(get("editor.template.label")), templateCombo);
    }

    private void configureAccessibility() {
        getDialogPane().setAccessibleText(existing == null ? "New spaceship design dialog" : "Edit spaceship design dialog");
        getDialogPane().setAccessibleHelp("Edit identity, drive, mass budget, carried craft, and validation details for a spaceship design.");

        annotate(okButton, "OK", "Save this spaceship design when all validation errors are resolved.");
        Button cancelButton = (Button) getDialogPane().lookupButton(ButtonType.CANCEL);
        annotate(cancelButton, "Cancel", "Close this dialog without saving changes.");
        annotate(templateCombo, get("editor.template.label"), "Choose a template to prefill a new spaceship design.");

        annotate(nameField, get("editor.field.name"), "Required spaceship name.");
        annotate(designationField, get("editor.field.designation"), "Optional hull or fleet designation.");
        annotate(classCombo, get("editor.field.class"), "Select the ship class.");
        annotate(crewField, get("editor.field.crew"), "Crew complement count.");
        annotate(lengthField, get("editor.field.length"), "Ship length in meters.");
        annotate(iconField, get("editor.field.icon"), "Optional icon path.");
        annotate(sourceTypeCombo, get("editor.field.sourceType"), "Select whether the design is real, proposed, fictional, or unknown.");
        annotate(universeField, get("editor.field.sourceUniverse"), "Source universe or setting for this design.");
        annotate(factionField, get("editor.field.faction"), "Faction, polity, or operator for this design.");
        annotate(eraField, get("editor.field.era"), "Era, year, or period for this design.");
        annotate(concealedCheck, get("editor.field.concealed"), get("editor.tooltip.concealed"));
        annotate(operationalStateCombo, get("editor.field.operationalState"), get("editor.tooltip.operationalState"));
        annotate(descriptionArea, get("editor.field.description"), "Free-form design notes.");

        annotate(driveCombo, get("editor.field.drive"), "Select the primary drive type.");
        annotate(structureField, get("editor.mass.structure"), "Structure mass in tons.");
        annotate(engineField, get("editor.mass.engine"), "Engine mass in tons.");
        annotate(propellantField, get("editor.mass.propellant"), "Propellant mass in tons.");
        annotate(payloadField, get("editor.mass.payload"), "Payload mass in tons.");
        annotate(crewMassField, get("editor.mass.crew"), "Crew and life-support mass in tons.");
        annotate(radiatorField, get("editor.mass.radiator"), "Radiator mass in tons.");

        annotate(carriedTable, "Carried craft table", "Carried craft already assigned to this design.");
        annotate(craftNameField, get("column.name"), "Name of the carried craft to add.");
        annotate(craftClassCombo, get("column.class"), "Class of the carried craft to add.");
        annotate(craftCountField, "Carried craft count", "Number of carried craft of this type.");
        annotate(craftMassField, "Carried craft mass", "Mass of one carried craft in tons.");
        annotate(craftRoleField, "Carried craft role", "Role or mission for the carried craft.");

        annotate(statusLabel, "Validation status", "Current validation result for this design.");
        annotate(deltaVBadge, "Delta V summary", "Estimated delta V and special capability summary.");
        annotate(planButton, get("button.planTransfer"), "Open transfer planning for the current design when available.");
        annotate(messagesList, "Validation messages", "Warnings and blocking validation messages for this design.");
    }

    private static void annotate(Node node, String text, String help) {
        if (node == null) {
            return;
        }
        node.setAccessibleText(text);
        node.setAccessibleHelp(help);
    }

    private void addCarried() {
        String name = craftNameField.getText() == null ? "" : craftNameField.getText().trim();
        ShipClass cls = craftClassCombo.getValue();
        if (name.isBlank() || cls == null) {
            return;
        }
        int count = (int) Math.max(1, parse(craftCountField, 1));
        double mass = Math.max(0, parse(craftMassField, 0));
        String role = craftRoleField.getText() == null ? "" : craftRoleField.getText().trim();
        carried.add(new CarriedCraft(name, cls, count, mass, role));
        craftNameField.clear();
        craftRoleField.clear();
        craftCountField.setText("1");
        craftMassField.setText("0");
        revalidate();
    }

    private void wireValidation() {
        nameField.textProperty().addListener((o, a, b) -> revalidate());
        classCombo.valueProperty().addListener((o, a, b) -> revalidate());
        driveCombo.valueProperty().addListener((o, a, b) -> revalidate());
        crewField.textProperty().addListener((o, a, b) -> revalidate());
        for (TextField f : new TextField[]{structureField, engineField, propellantField,
                payloadField, crewMassField, radiatorField}) {
            f.textProperty().addListener((o, a, b) -> revalidate());
        }
        carried.addListener((javafx.collections.ListChangeListener<CarriedCraft>) c -> revalidate());
    }

    private void revalidate() {
        SpaceshipDesign draft = buildDraft();
        if (draft == null) {
            statusLabel.setText(get("status.incomplete"));
            statusLabel.setTextFill(javafx.scene.paint.Color.GRAY);
            deltaVBadge.setText("");
            messagesList.getItems().clear();
            planButton.setDisable(true);
            if (okButton != null) {
                okButton.setDisable(true);
            }
            return;
        }
        ValidationResult result = validationEngine.validate(draft);
        messagesList.getItems().setAll(
                result.messages().stream().map(ValidationMessage::toString).toList());
        deltaVBadge.setText(badgeText(draft));
        planButton.setDisable(transferPlannerBridge == null || !transferPlannerBridge.canPlan(draft));

        if (!result.isValid()) {
            statusLabel.setText(get("status.invalid"));
            statusLabel.setTextFill(javafx.scene.paint.Color.web("#c0392b")); // red
        } else if (result.hasWarnings()) {
            statusLabel.setText(get("status.warnings"));
            statusLabel.setTextFill(javafx.scene.paint.Color.web("#d68910")); // amber
        } else {
            statusLabel.setText(get("status.valid"));
            statusLabel.setTextFill(javafx.scene.paint.Color.web("#1e8449")); // green
        }
        if (okButton != null) {
            okButton.setDisable(!result.isValid());
        }
    }

    /**
     * Assembles the current field values into a design, or returns {@code null} if the required identity
     * fields (name, class, drive) are not yet filled in.
     */
    private SpaceshipDesign buildDraft() {
        String name = nameField.getText() == null ? "" : nameField.getText().trim();
        ShipClass shipClass = classCombo.getValue();
        DriveType driveType = driveCombo.getValue();
        if (name.isBlank() || shipClass == null || driveType == null) {
            return null;
        }

        SpaceshipBuilder builder = SpaceshipBuilder.create(name)
                .designation(text(designationField))
                .shipClass(shipClass)
                .driveType(driveType)
                .structureTons(parse(structureField, 0))
                .engineTons(parse(engineField, 0))
                .propellantTons(parse(propellantField, 0))
                .payloadTons(parse(payloadField, 0))
                .crewTons(parse(crewMassField, 0))
                .radiatorTons(parse(radiatorField, 0))
                .crew((int) parse(crewField, 0))
                .lengthMeters(parse(lengthField, 0))
                .icon(text(iconField))
                .description(text(descriptionArea))
                .sourceType(sourceTypeCombo.getValue() == null ? SourceType.UNKNOWN : sourceTypeCombo.getValue())
                .sourceUniverse(text(universeField))
                .faction(text(factionField))
                .era(text(eraField));
        for (CarriedCraft c : carried) {
            builder.carry(c);
        }
        SpaceshipDesign built = builder.build();

        // The SpaceshipBuilder does not (yet) carry concealed / operationalState — see
        // commit-message gap note — so we apply both via the canonical 19-arg
        // SpaceshipDesign constructor here. The edit path also uses the canonical
        // constructor so it preserves both fields (and the existing id + createdAt).
        boolean concealed = concealedCheck.isSelected();
        OperationalState operationalState = operationalStateCombo.getValue() == null
                ? OperationalState.OPERATIONAL
                : operationalStateCombo.getValue();

        String id = existing != null ? existing.id() : built.id();
        java.time.Instant createdAt = existing != null ? existing.createdAt() : built.createdAt();

        return new SpaceshipDesign(
                id,
                built.name(),
                built.designation(),
                built.shipClass(),
                built.driveType(),
                built.massBudget(),
                built.crewComplement(),
                built.lengthMeters(),
                built.carriedCraft(),
                built.armaments(),
                built.iconPath(),
                built.description(),
                built.sourceType(),
                built.sourceUniverse(),
                built.faction(),
                concealed,
                operationalState,
                built.era(),
                createdAt);
    }

    private void populateFrom(SpaceshipDesign d) {
        nameField.setText(d.name());
        designationField.setText(d.designation());
        classCombo.setValue(d.shipClass());
        driveCombo.setValue(d.driveType());
        crewField.setText(Integer.toString(d.crewComplement()));
        lengthField.setText(Double.toString(d.lengthMeters()));
        iconField.setText(d.iconPath());
        sourceTypeCombo.setValue(d.sourceType());
        universeField.setText(d.sourceUniverse());
        factionField.setText(d.faction());
        eraField.setText(d.era());
        concealedCheck.setSelected(d.concealed());
        operationalStateCombo.setValue(d.operationalState() == null ? OperationalState.OPERATIONAL : d.operationalState());
        descriptionArea.setText(d.description());
        structureField.setText(Double.toString(d.massBudget().structureMassTons()));
        engineField.setText(Double.toString(d.massBudget().engineMassTons()));
        propellantField.setText(Double.toString(d.massBudget().propellantMassTons()));
        payloadField.setText(Double.toString(d.massBudget().payloadMassTons()));
        crewMassField.setText(Double.toString(d.massBudget().crewMassTons()));
        radiatorField.setText(Double.toString(d.massBudget().radiatorMassTons()));
        carried.setAll(d.carriedCraft());
    }

    private void applyDefaults() {
        nameField.setText(get("default.name", "New Vessel"));
        classCombo.setValue(ShipClass.FRIGATE);
        driveCombo.setValue(DriveType.CHEMICAL_BIPROPELLANT);
        sourceTypeCombo.setValue(SourceType.UNKNOWN);
        universeField.setPromptText(get("editor.universe.prompt", "e.g. The Expanse, Caine Riordan"));
        factionField.setPromptText(get("editor.faction.prompt", "e.g. Terran Republic, NASA / JPL"));
        eraField.setPromptText(get("editor.era.prompt", "e.g. 2045, Post-Contact"));
        concealedCheck.setSelected(false);
        operationalStateCombo.setValue(OperationalState.OPERATIONAL);
        crewField.setText(Integer.toString(getInt("default.crew", 1)));
        lengthField.setText(Double.toString(getDouble("default.length", 50)));
        structureField.setText(Double.toString(getDouble("default.mass.structure", 100)));
        engineField.setText(Double.toString(getDouble("default.mass.engine", 50)));
        propellantField.setText(Double.toString(getDouble("default.mass.propellant", 200)));
        payloadField.setText(Double.toString(getDouble("default.mass.payload", 20)));
        crewMassField.setText(Double.toString(getDouble("default.mass.crew", 10)));
        radiatorField.setText(Double.toString(getDouble("default.mass.radiator", 0)));
    }

    // -------------------------------------------------------------- helpers

    private static String sourceTypeLabel(SourceType t) {
        return switch (t) {
            case REAL -> get("source.real", t.label());
            case PROPOSED -> get("source.proposed", t.label());
            case SCIENCE_FICTION -> get("source.scienceFiction", t.label());
            case UNKNOWN -> get("source.unknown", t.label());
        };
    }

    private static String operationalStateLabel(OperationalState s) {
        return get("operationalState." + s.name(), s.name());
    }

    private static String badgeText(SpaceshipDesign d) {
        double dv = d.estimateDeltaVKmps();
        String dvText = Double.isNaN(dv) ? "Δv: n/a (reactionless)" : "Δv: %.0f km/s".formatted(dv);
        String mother = d.isMothership() ? "  •  Mothership" : "";
        String land = d.isSuitableForLanding() ? "  •  Can land" : "";
        return dvText + mother + land;
    }

    private static GridPane grid() {
        GridPane g = new GridPane();
        g.setHgap(8);
        g.setVgap(6);
        return g;
    }

    private static void addRow(GridPane g, int row, String label, Node field) {
        Label l = new Label(label);
        g.add(l, 0, row);
        g.add(field, 1, row);
        if (field instanceof javafx.scene.layout.Region region) {
            region.setMaxWidth(Double.MAX_VALUE);
        }
        GridPane.setHgrow(field, Priority.ALWAYS);
        ButtonBar.setButtonUniformSize(field, false);
    }

    private static String text(TextField f) {
        return f.getText() == null ? "" : f.getText().trim();
    }

    private static String text(TextArea f) {
        return f.getText() == null ? "" : f.getText().trim();
    }

    private static double parse(TextField f, double fallback) {
        try {
            String t = f.getText();
            return (t == null || t.isBlank()) ? fallback : Double.parseDouble(t.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
