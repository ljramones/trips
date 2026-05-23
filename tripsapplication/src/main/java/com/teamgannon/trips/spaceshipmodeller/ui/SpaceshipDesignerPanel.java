package com.teamgannon.trips.spaceshipmodeller.ui;

import com.teamgannon.trips.spaceshipmodeller.core.ShipClass;
import com.teamgannon.trips.spaceshipmodeller.core.SpaceshipDesign;
import com.teamgannon.trips.spaceshipmodeller.io.SpaceshipJsonService;
import com.teamgannon.trips.spaceshipmodeller.propulsion.Category;
import com.teamgannon.trips.spaceshipmodeller.propulsion.DriveType;
import com.teamgannon.trips.spaceshipmodeller.integration.TransferPlannerBridge;
import com.teamgannon.trips.spaceshipmodeller.rules.ValidationResult;
import com.teamgannon.trips.spaceshipmodeller.service.SpaceshipService;
import com.teamgannon.trips.spaceshipmodeller.templates.SpaceshipTemplateLibrary;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Separator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.teamgannon.trips.spaceshipmodeller.ui.SpaceshipModellerLabels.get;
import static com.teamgannon.trips.support.AlertFactory.showConfirmationAlert;
import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;
import static com.teamgannon.trips.support.AlertFactory.showInfoMessage;

/**
 * Main JavaFX panel for the Spaceship Modeller.
 * <p>
 * A plain {@link BorderPane} (matching TRIPS panels such as {@code StarTablePane}) constructed with the
 * Spring-managed {@link SpaceshipService}. Layout:
 * <ul>
 *   <li>top: title plus filters by class, drive, category and a name search;</li>
 *   <li>centre: a split pane with the saved-designs table on the left and a details/preview pane on the
 *       right;</li>
 *   <li>CRUD buttons (New, Edit, Delete) drive the {@link SpaceshipEditorDialog} and the service.</li>
 * </ul>
 * The right-hand pane reserves a "3D Preview (Coming)" placeholder for the future advanced visual designer.
 * Creating or editing through the dialog persists to the library ("save to library"); a stub
 * "Export to Mission" action marks the future integration point.
 */
@Slf4j
public class SpaceshipDesignerPanel extends BorderPane {

    private static final String ALL = "All";

    private final SpaceshipService spaceshipService;
    private final SpaceshipJsonService jsonService;
    private final SpaceshipTemplateLibrary templateLibrary;
    private final TransferPlannerBridge transferPlannerBridge;

    private final TableView<SpaceshipRow> table = new TableView<>();
    private final ComboBox<String> classFilter = new ComboBox<>();
    private final ComboBox<String> driveFilter = new ComboBox<>();
    private final ComboBox<String> categoryFilter = new ComboBox<>();
    private final TextField searchField = new TextField();

    private List<SpaceshipDesign> allDesigns = List.of();

    // details fields
    private final Label detailName = new Label();
    private final Label detailDesignation = new Label();
    private final Label detailClass = new Label();
    private final Label detailDrive = new Label();
    private final Label detailCategory = new Label();
    private final Label detailMass = new Label();
    private final Label detailMassRatio = new Label();
    private final Label detailDeltaV = new Label();
    private final Label detailCrew = new Label();
    private final Label detailCarried = new Label();
    private final Label detailStatus = new Label();

    private final Button editButton = new Button(get("button.edit"));
    private final Button deleteButton = new Button(get("button.delete"));
    private final Button exportButton = new Button(get("button.export"));
    private final Button exportJsonButton = new Button(get("button.exportJson"));
    private final Button planButton = new Button(get("button.planTransfer"));
    private final Button useInSolarSystemButton = new Button(get("button.useInSolarSystem"));
    private final ListView<String> validationMessages = new ListView<>();

    public SpaceshipDesignerPanel(SpaceshipService spaceshipService,
                                  SpaceshipJsonService jsonService,
                                  SpaceshipTemplateLibrary templateLibrary,
                                  TransferPlannerBridge transferPlannerBridge) {
        this.spaceshipService = spaceshipService;
        this.jsonService = jsonService;
        this.templateLibrary = templateLibrary;
        this.transferPlannerBridge = transferPlannerBridge;
        setPadding(new Insets(10));
        setTop(buildHeader());
        setCenter(buildCenter());
        seedTemplatesIfEmpty();
        reload();
    }

    // ------------------------------------------------------------- building

    private VBox buildHeader() {
        Label title = new Label(get("panel.title"));
        title.setFont(Font.font(title.getFont().getFamily(), 18));
        title.setStyle("-fx-font-weight: bold;");
        Label subtitle = new Label(get("panel.subtitle"));
        subtitle.setStyle("-fx-text-fill: gray;");

        classFilter.getItems().add(ALL);
        Arrays.stream(ShipClass.values()).map(Enum::name).forEach(classFilter.getItems()::add);
        driveFilter.getItems().add(ALL);
        Arrays.stream(DriveType.values()).map(Enum::name).forEach(driveFilter.getItems()::add);
        categoryFilter.getItems().add(ALL);
        Arrays.stream(Category.values()).map(Enum::name).forEach(categoryFilter.getItems()::add);
        classFilter.setValue(ALL);
        driveFilter.setValue(ALL);
        categoryFilter.setValue(ALL);
        classFilter.valueProperty().addListener((o, a, b) -> applyFilters());
        driveFilter.valueProperty().addListener((o, a, b) -> applyFilters());
        categoryFilter.valueProperty().addListener((o, a, b) -> applyFilters());

        searchField.setPromptText(get("filter.search.prompt"));
        searchField.textProperty().addListener((o, a, b) -> applyFilters());

        Button newButton = new Button(get("button.new"));
        newButton.setTooltip(new Tooltip(get("tooltip.new")));
        newButton.setOnAction(e -> onNew());
        Button importButton = new Button(get("button.importJson"));
        importButton.setTooltip(new Tooltip(get("tooltip.importJson")));
        importButton.setOnAction(e -> onImportJson());
        Button templatesButton = new Button(get("button.loadTemplates"));
        templatesButton.setTooltip(new Tooltip(get("tooltip.loadTemplates")));
        templatesButton.setOnAction(e -> onLoadTemplates());
        Button refreshButton = new Button(get("button.refresh"));
        refreshButton.setOnAction(e -> reload());

        HBox filters = new HBox(8,
                new Label(get("filter.shipClass")), classFilter,
                new Label(get("filter.driveType")), driveFilter,
                new Label(get("filter.category")), categoryFilter,
                searchField,
                new Separator(),
                newButton, importButton, templatesButton, refreshButton);
        filters.setAlignment(Pos.CENTER_LEFT);
        filters.setPadding(new Insets(8, 0, 8, 0));
        HBox.setHgrow(searchField, Priority.ALWAYS);

        return new VBox(2, title, subtitle, filters);
    }

    private SplitPane buildCenter() {
        configureTable();

        Button editLocal = editButton;
        editLocal.setTooltip(new Tooltip(get("tooltip.edit")));
        editLocal.setOnAction(e -> onEdit());
        deleteButton.setTooltip(new Tooltip(get("tooltip.delete")));
        deleteButton.setOnAction(e -> onDelete());
        HBox tableButtons = new HBox(8, editButton, deleteButton);
        tableButtons.setPadding(new Insets(8, 0, 0, 0));

        VBox left = new VBox(0, table, tableButtons);
        VBox.setVgrow(table, Priority.ALWAYS);

        SplitPane split = new SplitPane(left, buildDetails());
        split.setDividerPositions(0.58);
        return split;
    }

    @SuppressWarnings("unchecked")
    private void configureTable() {
        table.setPlaceholder(new Label("No spaceship designs yet — click \"" + get("button.new") + "\""));
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        TableColumn<SpaceshipRow, String> nameCol = col(get("column.name"), "name", 150);
        TableColumn<SpaceshipRow, String> desigCol = col(get("column.designation"), "designation", 110);
        TableColumn<SpaceshipRow, String> classCol = col(get("column.class"), "shipClass", 90);
        TableColumn<SpaceshipRow, String> driveCol = col(get("column.drive"), "driveType", 150);
        TableColumn<SpaceshipRow, String> catCol = col(get("column.category"), "category", 120);
        TableColumn<SpaceshipRow, Number> massCol = col(get("column.mass"), "mass", 100);
        TableColumn<SpaceshipRow, Number> crewCol = col(get("column.crew"), "crew", 60);
        TableColumn<SpaceshipRow, String> deltaVCol = col(get("column.deltaV"), "deltaV", 90);
        TableColumn<SpaceshipRow, String> motherCol = col(get("column.mothership"), "mothership", 90);

        table.getColumns().setAll(
                nameCol, desigCol, classCol, driveCol, catCol, massCol, crewCol, deltaVCol, motherCol);
        table.getSelectionModel().selectedItemProperty().addListener(
                (o, a, b) -> showDetails(b == null ? null : b.getDesign()));

        MenuItem editItem = new MenuItem(get("button.edit"));
        editItem.setOnAction(e -> onEdit());
        MenuItem duplicateItem = new MenuItem(get("button.duplicate"));
        duplicateItem.setOnAction(e -> onDuplicate());
        MenuItem exportItem = new MenuItem(get("button.exportJson"));
        exportItem.setOnAction(e -> onExportJson());
        MenuItem deleteItem = new MenuItem(get("button.delete"));
        deleteItem.setOnAction(e -> onDelete());
        table.setContextMenu(new ContextMenu(editItem, duplicateItem, exportItem, deleteItem));
    }

    private static <S, T> TableColumn<S, T> col(String header, String property, int prefWidth) {
        TableColumn<S, T> c = new TableColumn<>(header);
        c.setCellValueFactory(new PropertyValueFactory<>(property));
        c.setPrefWidth(prefWidth);
        return c;
    }

    private VBox buildDetails() {
        Label header = new Label(get("details.title"));
        header.setFont(Font.font(header.getFont().getFamily(), 14));
        header.setStyle("-fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(6);
        int r = 0;
        addDetail(grid, r++, get("column.name"), detailName);
        addDetail(grid, r++, get("column.designation"), detailDesignation);
        addDetail(grid, r++, get("column.class"), detailClass);
        addDetail(grid, r++, get("column.drive"), detailDrive);
        addDetail(grid, r++, get("column.category"), detailCategory);
        addDetail(grid, r++, get("column.mass"), detailMass);
        addDetail(grid, r++, get("details.massRatio"), detailMassRatio);
        addDetail(grid, r++, get("details.deltaV"), detailDeltaV);
        addDetail(grid, r++, get("column.crew"), detailCrew);
        addDetail(grid, r++, get("details.carried"), detailCarried);

        detailStatus.setStyle("-fx-font-weight: bold;");

        planButton.setTooltip(new Tooltip(get("tooltip.planTransfer")));
        planButton.setOnAction(e -> onPlanTransfer());
        useInSolarSystemButton.setTooltip(new Tooltip(get("tooltip.useInSolarSystem")));
        useInSolarSystemButton.setOnAction(e -> onUseInSolarSystem());
        exportJsonButton.setTooltip(new Tooltip(get("tooltip.exportJson")));
        exportJsonButton.setOnAction(e -> onExportJson());
        exportButton.setTooltip(new Tooltip(get("tooltip.export")));
        exportButton.setOnAction(e -> onExport());
        HBox actions = new HBox(8, planButton, useInSolarSystemButton, exportJsonButton, exportButton);
        actions.setPadding(new Insets(6, 0, 6, 0));

        validationMessages.setPrefHeight(120);
        validationMessages.setPlaceholder(new Label("No issues"));
        TitledPane validationPane = new TitledPane(get("details.validationMessages"), validationMessages);
        validationPane.setExpanded(false);

        StackPane preview = new StackPane(new Label(get("details.preview3d")));
        preview.setMinHeight(140);
        preview.setStyle("-fx-border-color: #999; -fx-border-style: dashed; "
                + "-fx-background-color: #f4f4f4; -fx-text-fill: gray;");
        VBox.setVgrow(preview, Priority.ALWAYS);

        VBox box = new VBox(8, header, grid, detailStatus, validationPane, new Separator(), actions, preview);
        box.setPadding(new Insets(0, 0, 0, 10));
        showDetails(null);
        return box;
    }

    private static void addDetail(GridPane grid, int row, String label, Label value) {
        Label l = new Label(label + ":");
        l.setStyle("-fx-text-fill: #555;");
        grid.add(l, 0, row);
        grid.add(value, 1, row);
    }

    // -------------------------------------------------------------- actions

    private void reload() {
        try {
            allDesigns = spaceshipService.findAll();
            applyFilters();
        } catch (Exception e) {
            log.error("Failed to load spaceship designs", e);
            showErrorAlert(get("window.title"), "Failed to load designs: " + e.getMessage());
        }
    }

    private void applyFilters() {
        String cls = classFilter.getValue();
        String drv = driveFilter.getValue();
        String cat = categoryFilter.getValue();
        String q = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();

        List<SpaceshipRow> rows = allDesigns.stream()
                .filter(d -> cls == null || ALL.equals(cls) || d.shipClass().name().equals(cls))
                .filter(d -> drv == null || ALL.equals(drv) || d.driveType().name().equals(drv))
                .filter(d -> cat == null || ALL.equals(cat) || d.driveType().category().name().equals(cat))
                .filter(d -> q.isEmpty()
                        || d.name().toLowerCase().contains(q)
                        || d.designation().toLowerCase().contains(q))
                .map(SpaceshipRow::new)
                .toList();
        table.setItems(FXCollections.observableArrayList(rows));
    }

    private void onNew() {
        SpaceshipEditorDialog dialog =
                new SpaceshipEditorDialog(null, templateLibrary.getAllTemplates(), transferPlannerBridge);
        dialog.showAndWait().ifPresent(this::saveAndReload);
    }

    private void onEdit() {
        SpaceshipDesign selected = selectedDesign();
        if (selected == null) {
            return;
        }
        SpaceshipEditorDialog dialog =
                new SpaceshipEditorDialog(selected, List.of(), transferPlannerBridge);
        dialog.showAndWait().ifPresent(this::saveAndReload);
    }

    private void onDuplicate() {
        SpaceshipDesign selected = selectedDesign();
        if (selected == null) {
            return;
        }
        SpaceshipDesign copy = new SpaceshipDesign(
                UUID.randomUUID().toString(), "Copy of " + selected.name(), selected.designation(),
                selected.shipClass(), selected.driveType(), selected.massBudget(), selected.crewComplement(),
                selected.lengthMeters(), selected.carriedCraft(), selected.iconPath(), selected.description(),
                Instant.now());
        saveAndReload(copy);
    }

    private void onPlanTransfer() {
        SpaceshipDesign selected = selectedDesign();
        if (selected != null) {
            new TransferPreviewDialog(selected, transferPlannerBridge).showAndWait();
        }
    }

    private void onUseInSolarSystem() {
        SpaceshipDesign selected = selectedDesign();
        String name = selected == null ? "the ship" : "\"" + selected.name() + "\"";
        showInfoMessage(get("button.useInSolarSystem"),
                "To plan a transfer for " + name + " in context, open a system in the Solar System view, "
                        + "then right-click a body and choose \"Plan Transfer from Here...\". "
                        + "Every saved ship is available there.");
    }

    private void onLoadTemplates() {
        try {
            int added = spaceshipService.seedTemplates(templateLibrary.getAllTemplates());
            reload();
            showInfoMessage(get("button.loadTemplates"),
                    added == 0 ? "All templates are already in the library." : "Added " + added + " template(s).");
        } catch (Exception e) {
            log.error("Failed to load templates", e);
            showErrorAlert(get("button.loadTemplates"), "Failed to load templates: " + e.getMessage());
        }
    }

    private void seedTemplatesIfEmpty() {
        try {
            if (spaceshipService.count() == 0) {
                spaceshipService.seedTemplates(templateLibrary.getAllTemplates());
            }
        } catch (Exception e) {
            log.error("Failed to seed templates on first launch", e);
        }
    }

    private void onDelete() {
        SpaceshipDesign selected = selectedDesign();
        if (selected == null) {
            return;
        }
        Optional<ButtonType> confirm = showConfirmationAlert(
                get("window.title"),
                "Delete \"" + selected.name() + "\"?",
                "This permanently removes the design from the library.");
        if (confirm.isPresent() && confirm.get() == ButtonType.OK) {
            try {
                spaceshipService.delete(selected.id());
                reload();
            } catch (Exception e) {
                log.error("Failed to delete spaceship design", e);
                showErrorAlert(get("window.title"), "Failed to delete: " + e.getMessage());
            }
        }
    }

    private void onExport() {
        SpaceshipDesign selected = selectedDesign();
        if (selected == null) {
            return;
        }
        showInfoMessage(get("button.export"),
                "Exporting \"" + selected.name() + "\" to a mission is coming soon.");
    }

    private void onExportJson() {
        SpaceshipDesign selected = selectedDesign();
        if (selected == null) {
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle(get("json.export.title"));
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(get("json.filter"), "*.json"));
        chooser.setInitialFileName(sanitizeFileName(selected.name()) + ".json");
        File file = chooser.showSaveDialog(getScene() == null ? null : getScene().getWindow());
        if (file == null) {
            return;
        }
        try {
            jsonService.exportToFile(selected, file);
            showInfoMessage(get("button.exportJson"),
                    "Exported \"" + selected.name() + "\" to " + file.getName());
        } catch (Exception e) {
            log.error("Failed to export design to JSON", e);
            showErrorAlert(get("button.exportJson"), "Failed to export: " + e.getMessage());
        }
    }

    private void onImportJson() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(get("json.import.title"));
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(get("json.filter"), "*.json"));
        File file = chooser.showOpenDialog(getScene() == null ? null : getScene().getWindow());
        if (file == null) {
            return;
        }
        try {
            List<SpaceshipDesign> imported = jsonService.importFromFile(file);
            imported.forEach(spaceshipService::save);
            reload();
            showInfoMessage(get("button.importJson"),
                    "Imported " + imported.size() + " design(s) from " + file.getName());
        } catch (Exception e) {
            log.error("Failed to import designs from JSON", e);
            showErrorAlert(get("button.importJson"), "Failed to import: " + e.getMessage());
        }
    }

    private static String sanitizeFileName(String name) {
        String cleaned = name == null ? "design" : name.trim().replaceAll("[^a-zA-Z0-9._-]+", "_");
        return cleaned.isBlank() ? "design" : cleaned;
    }

    private void saveAndReload(SpaceshipDesign design) {
        try {
            spaceshipService.save(design);
            reload();
        } catch (Exception e) {
            log.error("Failed to save spaceship design", e);
            showErrorAlert(get("window.title"), "Failed to save: " + e.getMessage());
        }
    }

    private SpaceshipDesign selectedDesign() {
        SpaceshipRow row = table.getSelectionModel().getSelectedItem();
        return row == null ? null : row.getDesign();
    }

    private void showDetails(SpaceshipDesign d) {
        boolean has = d != null;
        editButton.setDisable(!has);
        deleteButton.setDisable(!has);
        exportButton.setDisable(!has);
        exportJsonButton.setDisable(!has);

        if (!has) {
            for (Label l : List.of(detailName, detailDesignation, detailClass, detailDrive,
                    detailCategory, detailMass, detailMassRatio, detailDeltaV, detailCrew, detailCarried)) {
                l.setText("");
            }
            planButton.setDisable(true);
            validationMessages.getItems().clear();
            detailStatus.setText(get("details.none"));
            detailStatus.setTextFill(javafx.scene.paint.Color.GRAY);
            return;
        }

        detailName.setText(d.name());
        detailDesignation.setText(d.designation());
        detailClass.setText(d.shipClass().label());
        detailDrive.setText(d.driveType().name());
        detailCategory.setText(d.driveType().category().label());
        detailMass.setText("%.1f t".formatted(d.massBudget().wetMassTons()));
        detailMassRatio.setText("%.2f".formatted(d.massBudget().massRatio()));
        double dv = d.estimateDeltaVKmps();
        detailDeltaV.setText(Double.isNaN(dv) ? "n/a (reactionless)" : "%.0f km/s".formatted(dv));
        detailCrew.setText(Integer.toString(d.crewComplement()));
        detailCarried.setText(carriedSummary(d));

        ValidationResult result = spaceshipService.validate(d);
        validationMessages.getItems().setAll(result.messages().stream().map(Object::toString).toList());
        if (!result.isValid()) {
            detailStatus.setText(get("status.invalid") + " (" + result.errors().size() + ")");
            detailStatus.setTextFill(javafx.scene.paint.Color.web("#c0392b"));
        } else if (result.hasWarnings()) {
            detailStatus.setText(get("status.warnings") + " (" + result.warnings().size() + ")");
            detailStatus.setTextFill(javafx.scene.paint.Color.web("#d68910"));
        } else {
            detailStatus.setText(get("status.valid"));
            detailStatus.setTextFill(javafx.scene.paint.Color.web("#1e8449"));
        }
        planButton.setDisable(!transferPlannerBridge.canPlan(d));
    }

    private static String carriedSummary(SpaceshipDesign d) {
        if (d.carriedCraft().isEmpty()) {
            return "none";
        }
        return d.carriedCraft().stream()
                .map(c -> c.count() + "× " + c.name())
                .collect(java.util.stream.Collectors.joining(" + "));
    }
}
