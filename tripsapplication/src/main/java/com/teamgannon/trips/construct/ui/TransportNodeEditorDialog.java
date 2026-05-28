package com.teamgannon.trips.construct.ui;

import com.teamgannon.trips.utility.DialogUtils;
import com.terranrepublic.infrastructure.NodeType;
import com.terranrepublic.infrastructure.TransportNode;
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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.teamgannon.trips.construct.ui.ConstructLabels.get;

/**
 * Modal editor for a {@link TransportNode}. Smallest of the three Phase D editors — no
 * designation, operational state, icon path, or era controls because those fields don't exist on
 * the record.
 *
 * <p>UI invariant: {@code instantaneousTransit=true} disables and clears the
 * {@code traversalTimeTicks} field, mirroring the domain compact-constructor behaviour that
 * zeroes traversal time when instantaneous transit is selected.
 *
 * <p>The connected-node-ids editor is a small {@code ListView<String>} with an add/remove form.
 * The dialog does not validate that the entered ids resolve to real transport nodes — that
 * concern lives in the in-memory {@code GraphRegistry}, which v2 Phase B left in place. Prompt
 * pinned this scope boundary.
 */
public class TransportNodeEditorDialog extends Dialog<TransportNode> {

    private final TransportNode existing;

    // identity
    private final TextField nameField = new TextField();
    private final TextField sourceField = new TextField();
    private final TextField factionField = new TextField();
    private final CheckBox concealedCheck = new CheckBox();
    private final TextArea descriptionArea = new TextArea();

    // classification
    private final ComboBox<NodeType> typeCombo = new ComboBox<>();

    // position
    private final TextField positionXField = new TextField();
    private final TextField positionYField = new TextField();
    private final TextField positionZField = new TextField();

    // transit
    private final TextField throughputField = new TextField();
    private final CheckBox instantaneousTransitCheck = new CheckBox();
    private final TextField traversalTimeField = new TextField();

    // connections
    private final ObservableList<String> connectedNodeIds = FXCollections.observableArrayList();
    private final ListView<String> connectionsList = new ListView<>(connectedNodeIds);
    private final TextField newConnectionField = new TextField();

    // validation
    private final Label statusLabel = new Label();
    private final ListView<String> messagesList = new ListView<>();
    private Button okButton;

    public TransportNodeEditorDialog() {
        this(null);
    }

    public TransportNodeEditorDialog(TransportNode existing) {
        this.existing = existing;
        setTitle(get(existing == null ? "editor.transport.title.new" : "editor.transport.title.edit"));
        getDialogPane().getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
        okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);

        typeCombo.getItems().setAll(NodeType.values());

        buildContent();
        DialogUtils.applyTheme(this);
        configureAccessibility();
        wireInstantaneousInvariant();
        wireValidation();

        if (existing != null) {
            populateFrom(existing);
        } else {
            applyDefaults();
        }
        revalidate();

        setResultConverter(buttonType -> buttonType == ButtonType.OK ? buildDraft() : null);
    }

    private void buildContent() {
        VBox root = new VBox(12);
        root.setPadding(new Insets(14));
        root.getChildren().addAll(
                section(get("editor.transport.section.basic"), basicGrid()),
                section(get("editor.transport.section.classification"), classificationGrid()),
                section(get("editor.transport.section.position"), positionGrid()),
                section(get("editor.transport.section.transit"), transitGrid()),
                section(get("editor.transport.section.connections"), connectionsSection()),
                section(get("editor.transport.section.validation"), validationSection()));

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.setPrefViewportHeight(560);
        getDialogPane().setContent(scroll);
        getDialogPane().setPrefWidth(680);
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
        addRow(g, r++, get("editor.field.source"), sourceField);
        addRow(g, r++, get("editor.field.faction"), factionField);
        addRow(g, r++, get("editor.field.concealed"), concealedCheck);
        addRow(g, r++, get("editor.field.description"), descriptionArea);
        return g;
    }

    private GridPane classificationGrid() {
        GridPane g = grid();
        addRow(g, 0, get("editor.transport.field.type"), typeCombo);
        return g;
    }

    private GridPane positionGrid() {
        GridPane g = grid();
        int r = 0;
        addRow(g, r++, get("editor.transport.field.positionX"), positionXField);
        addRow(g, r++, get("editor.transport.field.positionY"), positionYField);
        addRow(g, r++, get("editor.transport.field.positionZ"), positionZField);
        return g;
    }

    private GridPane transitGrid() {
        GridPane g = grid();
        int r = 0;
        addRow(g, r++, get("editor.transport.field.throughput"), throughputField);
        addRow(g, r++, get("editor.transport.field.instantaneousTransit"), instantaneousTransitCheck);
        addRow(g, r++, get("editor.transport.field.traversalTime"), traversalTimeField);
        return g;
    }

    private VBox connectionsSection() {
        connectionsList.setPrefHeight(120);
        connectionsList.setPlaceholder(new Label(""));
        newConnectionField.setPromptText(get("editor.transport.field.connectedNodeId"));

        Button addBtn = new Button(get("editor.button.add"));
        addBtn.setOnAction(e -> addConnection());
        Button removeBtn = new Button(get("editor.button.remove"));
        removeBtn.setOnAction(e -> {
            String sel = connectionsList.getSelectionModel().getSelectedItem();
            if (sel != null) {
                connectedNodeIds.remove(sel);
            }
        });

        HBox addForm = new HBox(6, newConnectionField, addBtn, removeBtn);
        HBox.setHgrow(newConnectionField, Priority.ALWAYS);
        return new VBox(6, connectionsList, addForm);
    }

    private void addConnection() {
        String id = trim(newConnectionField.getText());
        if (id.isBlank() || connectedNodeIds.contains(id)) {
            return;
        }
        connectedNodeIds.add(id);
        newConnectionField.clear();
    }

    private VBox validationSection() {
        statusLabel.getStyleClass().add("trips-bold");
        messagesList.setPrefHeight(80);
        return new VBox(6, statusLabel, messagesList);
    }

    private void configureAccessibility() {
        getDialogPane().setAccessibleText(existing == null
                ? get("editor.transport.title.new")
                : get("editor.transport.title.edit"));

        annotate(nameField, get("editor.field.name"), "Required node name.");
        annotate(sourceField, get("editor.field.source"), "Free text source or universe label.");
        annotate(factionField, get("editor.field.faction"), "Owning faction or polity.");
        annotate(concealedCheck, get("editor.field.concealed"), get("editor.tooltip.concealed"));
        concealedCheck.setTooltip(new Tooltip(get("editor.tooltip.concealed")));
        annotate(descriptionArea, get("editor.field.description"), "Free-form notes.");

        annotate(typeCombo, get("editor.transport.field.type"),
                "Physical or exotic transit-node class.");
        typeCombo.setTooltip(new Tooltip(get("editor.transport.field.type")));

        annotate(positionXField, get("editor.transport.field.positionX"), "X coordinate.");
        annotate(positionYField, get("editor.transport.field.positionY"), "Y coordinate.");
        annotate(positionZField, get("editor.transport.field.positionZ"), "Z coordinate.");

        annotate(throughputField, get("editor.transport.field.throughput"),
                "Throughput in tons per tick.");
        annotate(instantaneousTransitCheck, get("editor.transport.field.instantaneousTransit"),
                get("editor.transport.tooltip.instantaneousTransit"));
        instantaneousTransitCheck.setTooltip(new Tooltip(get("editor.transport.tooltip.instantaneousTransit")));
        annotate(traversalTimeField, get("editor.transport.field.traversalTime"),
                "Ignored when instantaneous transit is checked.");

        annotate(connectionsList, "Connected nodes",
                get("editor.transport.tooltip.connectedNodeIds"));
        connectionsList.setTooltip(new Tooltip(get("editor.transport.tooltip.connectedNodeIds")));
        annotate(newConnectionField, get("editor.transport.field.connectedNodeId"),
                "Enter a partner node id and click Add.");
    }

    private void wireInstantaneousInvariant() {
        instantaneousTransitCheck.selectedProperty().addListener((o, oldVal, newVal) -> {
            applyInstantaneousInvariant();
            revalidate();
        });
        applyInstantaneousInvariant();
    }

    private void applyInstantaneousInvariant() {
        boolean instantaneous = instantaneousTransitCheck.isSelected();
        if (instantaneous) {
            traversalTimeField.setText("0");
            traversalTimeField.setDisable(true);
        } else {
            traversalTimeField.setDisable(false);
        }
    }

    private void wireValidation() {
        nameField.textProperty().addListener((o, a, b) -> revalidate());
        typeCombo.valueProperty().addListener((o, a, b) -> revalidate());
        throughputField.textProperty().addListener((o, a, b) -> revalidate());
        traversalTimeField.textProperty().addListener((o, a, b) -> revalidate());
    }

    private void revalidate() {
        List<String> errors = new ArrayList<>();
        if (nameField.getText() == null || nameField.getText().isBlank()) {
            errors.add(get("validation.nameRequired"));
        }
        if (typeCombo.getValue() == null) {
            errors.add(get("validation.nodeTypeRequired"));
        }
        if (parse(throughputField, 0) < 0 || parse(traversalTimeField, 0) < 0) {
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

    private void populateFrom(TransportNode t) {
        nameField.setText(t.name());
        sourceField.setText(t.source());
        factionField.setText(t.faction());
        concealedCheck.setSelected(t.concealed());
        descriptionArea.setText(t.description());

        typeCombo.setValue(t.type());

        positionXField.setText(Double.toString(t.positionX()));
        positionYField.setText(Double.toString(t.positionY()));
        positionZField.setText(Double.toString(t.positionZ()));

        throughputField.setText(Double.toString(t.throughputTonsPerTick()));
        instantaneousTransitCheck.setSelected(t.instantaneousTransit());
        traversalTimeField.setText(Double.toString(t.traversalTimeTicks()));
        applyInstantaneousInvariant();

        connectedNodeIds.setAll(t.connectedNodeIds());
    }

    private void applyDefaults() {
        typeCombo.setValue(NodeType.RELAY);
        positionXField.setText("0");
        positionYField.setText("0");
        positionZField.setText("0");
        throughputField.setText("0");
        traversalTimeField.setText("0");
        instantaneousTransitCheck.setSelected(false);
        applyInstantaneousInvariant();
    }

    private TransportNode buildDraft() {
        String id = existing != null ? existing.id() : UUID.randomUUID().toString();
        Instant createdAt = existing != null ? existing.createdAt() : Instant.now();
        Instant now = Instant.now();
        return new TransportNode(
                id,
                trim(nameField.getText()),
                trim(sourceField.getText()),
                trim(factionField.getText()),
                concealedCheck.isSelected(),
                trim(descriptionArea.getText()),
                typeCombo.getValue(),
                parse(positionXField, 0),
                parse(positionYField, 0),
                parse(positionZField, 0),
                List.copyOf(connectedNodeIds),
                parse(throughputField, 0),
                instantaneousTransitCheck.isSelected(),
                parse(traversalTimeField, 0),
                createdAt,
                now);
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

    // ---------------------------------------------------------------- test seam

    CheckBox instantaneousTransitCheckForTesting() {
        return instantaneousTransitCheck;
    }

    TextField traversalTimeFieldForTesting() {
        return traversalTimeField;
    }

    TextField newConnectionFieldForTesting() {
        return newConnectionField;
    }

    ListView<String> connectionsListForTesting() {
        return connectionsList;
    }

    Button okButtonForTesting() {
        return okButton;
    }

    /** Public hook for the test to invoke addConnection without simulating a click. */
    void addConnectionForTesting() {
        addConnection();
    }
}
