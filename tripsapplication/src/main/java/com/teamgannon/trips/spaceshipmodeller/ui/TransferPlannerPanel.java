package com.teamgannon.trips.spaceshipmodeller.ui;

import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import com.terranrepublic.assets.SpaceshipDesign;
import com.teamgannon.trips.spaceshipmodeller.integration.Feasibility;
import com.teamgannon.trips.spaceshipmodeller.integration.ManeuverNode;
import com.teamgannon.trips.spaceshipmodeller.integration.TransferPlannerBridge;
import com.teamgannon.trips.spaceshipmodeller.planner.SavedTransferPlan;
import com.teamgannon.trips.spaceshipmodeller.planner.ShowTransferTrajectoryEvent;
import com.teamgannon.trips.spaceshipmodeller.planner.TransferPlanService;
import com.teamgannon.trips.spaceshipmodeller.service.SpaceshipService;
import org.springframework.context.ApplicationEventPublisher;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Separator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static com.teamgannon.trips.spaceshipmodeller.ui.SpaceshipModellerLabels.get;
import static com.teamgannon.trips.support.AlertFactory.showConfirmationAlert;
import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;
import static com.teamgannon.trips.support.AlertFactory.showInfoMessage;

/**
 * Main panel for the Transfer Planner: a split pane listing saved {@link SavedTransferPlan}s on the left and
 * the selected plan's maneuver nodes/details on the right. A toolbar offers New (reuses
 * {@link TransferPreviewDialog}), Delete, and Export to JSON.
 * <p>
 * A plain {@link BorderPane} constructed with the Spring-managed services, matching {@code
 * SpaceshipDesignerPanel}.
 */
@Slf4j
public class TransferPlannerPanel extends BorderPane {

    private final TransferPlanService planService;
    private final SpaceshipService spaceshipService;
    private final TransferPlannerBridge bridge;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper jsonMapper = buildJsonMapper();

    private final TableView<TransferPlanRow> table = new TableView<>();
    private final TableView<ManeuverNode> nodeTable = new TableView<>();

    private final Label detailRoute = new Label();
    private final Label detailShip = new Label();
    private final Label detailType = new Label();
    private final Label detailDeltaV = new Label();
    private final Label detailPropellant = new Label();
    private final Label detailDuration = new Label();
    private final Label detailStatus = new Label();

    private final Button deleteButton = new Button(get("button.delete", "Delete"));
    private final Button exportButton = new Button(get("button.exportJson", "Export to JSON..."));

    public TransferPlannerPanel(TransferPlanService planService,
                                SpaceshipService spaceshipService,
                                TransferPlannerBridge bridge,
                                ApplicationEventPublisher eventPublisher) {
        this.planService = planService;
        this.spaceshipService = spaceshipService;
        this.bridge = bridge;
        this.eventPublisher = eventPublisher;
        setPadding(new Insets(10));
        setTop(buildHeader());
        setCenter(buildCenter());
        reload();
    }

    private VBox buildHeader() {
        Label title = new Label(get("planner.title", "Transfer Planner"));
        title.setFont(Font.font(title.getFont().getFamily(), 18));
        title.setStyle("-fx-font-weight: bold;");

        Button newButton = new Button(get("planner.new", "New Transfer Plan..."));
        newButton.setOnAction(e -> onNew());
        deleteButton.setOnAction(e -> onDelete());
        exportButton.setOnAction(e -> onExportJson());
        Button refresh = new Button(get("button.refresh", "Refresh"));
        refresh.setOnAction(e -> reload());

        HBox toolbar = new HBox(8, newButton, deleteButton, exportButton, new Separator(), refresh);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(8, 0, 8, 0));
        return new VBox(2, title, toolbar);
    }

    private SplitPane buildCenter() {
        configureTable();
        configureNodeTable();

        Label detailsHeader = new Label(get("planner.details", "Plan Details"));
        detailsHeader.setStyle("-fx-font-weight: bold;");
        detailStatus.setStyle("-fx-font-weight: bold;");

        VBox details = new VBox(6, detailsHeader, detailShip, detailRoute, detailType,
                detailDeltaV, detailPropellant, detailDuration, detailStatus, new Separator(), nodeTable);
        details.setPadding(new Insets(0, 0, 0, 10));
        VBox.setVgrow(nodeTable, Priority.ALWAYS);

        SplitPane split = new SplitPane(table, details);
        split.setDividerPositions(0.5);
        showDetails(null);
        return split;
    }

    private void configureTable() {
        table.setPlaceholder(new Label(get("planner.empty", "No transfer plans yet")));
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        table.getColumns().setAll(List.of(
                col(get("planner.col.route", "Origin → Destination"), "route", 180),
                col(get("planner.col.ship", "Ship"), "ship", 130),
                deltaVColumn(get("planner.col.deltaV", "Total Δv"), 100),
                col(get("planner.col.status", "Status"), "status", 140)));
        table.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> {
            SavedTransferPlan plan = b == null ? null : b.getPlan();
            showDetails(plan);
            if (plan != null) {
                eventPublisher.publishEvent(new ShowTransferTrajectoryEvent(
                        this, plan.solarSystemId(), plan.originAu(), plan.destinationAu()));
            }
        });
    }

    private static TableColumn<TransferPlanRow, String> col(String header, String property, int width) {
        TableColumn<TransferPlanRow, String> c = new TableColumn<>(header);
        c.setCellValueFactory(new PropertyValueFactory<>(property));
        c.setPrefWidth(width);
        return c;
    }

    /** Δv column bound to the raw number so it sorts numerically, rendered with the "km/s" suffix. */
    private static TableColumn<TransferPlanRow, Number> deltaVColumn(String header, int width) {
        TableColumn<TransferPlanRow, Number> c = new TableColumn<>(header);
        c.setCellValueFactory(new PropertyValueFactory<>("deltaVValue"));
        c.setPrefWidth(width);
        c.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Number value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : "%.2f km/s".formatted(value.doubleValue()));
            }
        });
        return c;
    }

    private void configureNodeTable() {
        nodeTable.setPlaceholder(new Label("No maneuvers"));
        TableColumn<ManeuverNode, String> nameCol = new TableColumn<>("Maneuver");
        nameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().name()));
        nameCol.setPrefWidth(150);
        TableColumn<ManeuverNode, String> dvCol = new TableColumn<>("Δv");
        dvCol.setCellValueFactory(c -> new SimpleStringProperty("%.2f km/s".formatted(c.getValue().deltaVKmps())));
        TableColumn<ManeuverNode, String> timeCol = new TableColumn<>("T+ (days)");
        timeCol.setCellValueFactory(c -> new SimpleStringProperty("%.0f".formatted(c.getValue().timeFromStartDays())));
        TableColumn<ManeuverNode, String> propCol = new TableColumn<>("Propellant");
        propCol.setCellValueFactory(c -> new SimpleStringProperty(
                Double.isNaN(c.getValue().propellantTons()) ? "n/a" : "%.0f t".formatted(c.getValue().propellantTons())));
        TableColumn<ManeuverNode, String> massCol = new TableColumn<>("Mass after");
        massCol.setCellValueFactory(c -> new SimpleStringProperty(
                Double.isNaN(c.getValue().massAfterTons()) ? "n/a" : "%.0f t".formatted(c.getValue().massAfterTons())));
        nodeTable.getColumns().setAll(List.of(nameCol, dvCol, timeCol, propCol, massCol));
        nodeTable.setPrefHeight(160);
    }

    // -------------------------------------------------------------- actions

    private void reload() {
        try {
            List<TransferPlanRow> rows = planService.findAll().stream().map(TransferPlanRow::new).toList();
            table.setItems(FXCollections.observableArrayList(rows));
        } catch (Exception e) {
            log.error("Failed to load transfer plans", e);
            showErrorAlert(get("planner.title", "Transfer Planner"), "Failed to load plans: " + e.getMessage());
        }
    }

    /**
     * Refresh the list and select the plan with the given id (used after a new plan is created).
     *
     * @param planId id to select, or {@code null}
     */
    public void refreshAndSelect(String planId) {
        reload();
        if (planId != null) {
            for (TransferPlanRow row : table.getItems()) {
                if (planId.equals(row.getPlan().id())) {
                    table.getSelectionModel().select(row);
                    break;
                }
            }
        }
    }

    private void onNew() {
        List<SpaceshipDesign> ships = spaceshipService.findAll();
        if (ships.isEmpty()) {
            showInfoMessage(get("planner.new", "New Transfer Plan..."),
                    "There are no ships in the library yet. Create one in the Spaceship Modeller first.");
            return;
        }
        new TransferPreviewDialog(bridge, ships, List.of(), null, 1.0)
                .onCreate((plan, ship, sysId, mass) -> {
                    SavedTransferPlan saved = planService.saveComputed(plan, ship.id(), sysId, mass);
                    refreshAndSelect(saved.id());
                }, null)
                .showAndWait();
    }

    private void onDelete() {
        SavedTransferPlan selected = selectedPlan();
        if (selected == null) {
            return;
        }
        Optional<ButtonType> confirm = showConfirmationAlert(
                get("planner.title", "Transfer Planner"),
                "Delete this transfer plan?",
                selected.route() + "  (" + selected.shipName() + ")");
        if (confirm.isPresent() && confirm.get() == ButtonType.OK) {
            planService.delete(selected.id());
            reload();
        }
    }

    private void onExportJson() {
        SavedTransferPlan selected = selectedPlan();
        if (selected == null) {
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle(get("planner.export.title", "Export Transfer Plan to JSON"));
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(get("json.filter", "JSON files"), "*.json"));
        chooser.setInitialFileName("transfer-plan.json");
        File file = chooser.showSaveDialog(getScene() == null ? null : getScene().getWindow());
        if (file == null) {
            return;
        }
        try {
            Files.writeString(file.toPath(), jsonMapper.writeValueAsString(selected));
            showInfoMessage(get("button.exportJson", "Export to JSON..."), "Exported to " + file.getName());
        } catch (IOException e) {
            log.error("Failed to export transfer plan", e);
            showErrorAlert(get("button.exportJson", "Export to JSON..."), "Failed to export: " + e.getMessage());
        }
    }

    private SavedTransferPlan selectedPlan() {
        TransferPlanRow row = table.getSelectionModel().getSelectedItem();
        return row == null ? null : row.getPlan();
    }

    private void showDetails(SavedTransferPlan plan) {
        boolean has = plan != null;
        deleteButton.setDisable(!has);
        exportButton.setDisable(!has);
        if (!has) {
            detailRoute.setText("");
            detailShip.setText("");
            detailType.setText("");
            detailDeltaV.setText("");
            detailPropellant.setText("");
            detailDuration.setText("");
            detailStatus.setText(get("planner.selectHint", "Select a plan to view its maneuvers."));
            detailStatus.setTextFill(javafx.scene.paint.Color.GRAY);
            nodeTable.setItems(FXCollections.observableArrayList());
            return;
        }
        detailShip.setText("Ship: " + plan.shipName());
        detailRoute.setText("Route: " + plan.route());
        detailType.setText("Type: " + plan.transferType().label());
        detailDeltaV.setText("Total Δv: %.2f km/s".formatted(plan.totalDeltaVKmps()));
        detailPropellant.setText("Total propellant: " + (Double.isNaN(plan.totalPropellantTons())
                ? "n/a" : "%.0f t".formatted(plan.totalPropellantTons())));
        detailDuration.setText("Mission duration: %.0f days".formatted(plan.transferTimeDays()));
        Feasibility f = plan.feasibility();
        detailStatus.setText("Status: " + f.label());
        detailStatus.setTextFill(switch (f) {
            case FEASIBLE -> javafx.scene.paint.Color.web("#1e8449");
            case MARGINAL -> javafx.scene.paint.Color.web("#d68910");
            case INSUFFICIENT -> javafx.scene.paint.Color.web("#c0392b");
        });
        nodeTable.setItems(FXCollections.observableArrayList(plan.nodes()));
    }

    private static ObjectMapper buildJsonMapper() {
        SimpleModule m = new SimpleModule();
        m.addSerializer(Instant.class, new ValueSerializer<>() {
            @Override
            public void serialize(Instant value, JsonGenerator gen, SerializationContext context) {
                gen.writeString(value.toString());
            }
        });
        m.addDeserializer(Instant.class, new ValueDeserializer<>() {
            @Override
            public Instant deserialize(JsonParser p, DeserializationContext context) {
                return Instant.parse(p.getValueAsString());
            }
        });
        return JsonMapper.builder().addModule(m).enable(SerializationFeature.INDENT_OUTPUT).build();
    }
}
