package com.teamgannon.trips.tableviews;

import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.screenobjects.StarEditDialog;
import com.teamgannon.trips.screenobjects.StarEditStatus;
import com.teamgannon.trips.search.AstroSearchQuery;
import com.teamgannon.trips.service.StarService;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Callback;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

import static com.teamgannon.trips.support.AlertFactory.showConfirmationAlert;
import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

/**
 * Reusable TableView component with server-side pagination for displaying star data.
 * Uses Spring Data pagination to only load one page at a time, dramatically reducing memory usage.
 */
@Slf4j
public class StarTablePane extends BorderPane {

    private static final int PAGE_SIZE = 50;

    private final StarService starService;
    private final AstroSearchQuery query;
    private final String dataSetName;

    private final TableView<StarEditRecord> tableView = new TableView<>();
    private final Pagination pagination = new Pagination();
    private final Label statusLabel = new Label();
    private final StarTableColumnConfig columnConfig;

    // Columns - visible by default
    private final TableColumn<StarEditRecord, String> displayNameCol = new TableColumn<>("Display Name");
    private final TableColumn<StarEditRecord, Double> distanceToEarthCol = new TableColumn<>("Distance (LY)");
    private final TableColumn<StarEditRecord, String> spectraCol = new TableColumn<>("Spectra");
    private final TableColumn<StarEditRecord, Double> radiusCol = new TableColumn<>("Radius");
    private final TableColumn<StarEditRecord, Double> massCol = new TableColumn<>("Mass (M\u2609)");
    private final TableColumn<StarEditRecord, String> luminosityCol = new TableColumn<>("Luminosity");
    private final TableColumn<StarEditRecord, Double> raCol = new TableColumn<>("RA");
    private final TableColumn<StarEditRecord, Double> decCol = new TableColumn<>("Declination");
    private final TableColumn<StarEditRecord, Double> paraCol = new TableColumn<>("Parallax");
    private final TableColumn<StarEditRecord, Double> xCoordCol = new TableColumn<>("X");
    private final TableColumn<StarEditRecord, Double> yCoordCol = new TableColumn<>("Y");
    private final TableColumn<StarEditRecord, Double> zCoordCol = new TableColumn<>("Z");
    private final TableColumn<StarEditRecord, String> realCol = new TableColumn<>("Real");
    private final TableColumn<StarEditRecord, String> commentCol = new TableColumn<>("Comment");

    // Columns - hidden by default (can be shown via Columns menu)
    private final TableColumn<StarEditRecord, String> commonNameCol = new TableColumn<>("Common Name");
    private final TableColumn<StarEditRecord, String> constellationNameCol = new TableColumn<>("Constellation");
    private final TableColumn<StarEditRecord, String> polityCol = new TableColumn<>("Polity");
    private final TableColumn<StarEditRecord, Double> temperatureCol = new TableColumn<>("Temperature");

    private final Map<String, TableColumn<StarEditRecord, ?>> columnMap = new LinkedHashMap<>();

    // Current sort state
    private String currentSortColumn = "displayName";
    private boolean currentSortAscending = true;

    // Total count (cached)
    private long totalCount = 0;

    // Loading state
    private boolean isLoading = false;

    /**
     * Create a new StarTablePane with server-side pagination.
     *
     * @param starService the star service for database access
     * @param query       the search query to use
     */
    public StarTablePane(StarService starService, AstroSearchQuery query) {
        this.starService = starService;
        this.query = query;
        this.dataSetName = query.getDataSetContext().getDescriptor().getDataSetName();
        this.columnConfig = StarTableColumnConfig.defaults();

        initializeColumns();
        setupTable();
        setupToolbar();
        setupPagination();
        setupLayout();

        // Load initial data
        loadTotalCount();
    }

    private void initializeColumns() {
        columnMap.put("displayName", displayNameCol);
        columnMap.put("distanceToEarth", distanceToEarthCol);
        columnMap.put("spectra", spectraCol);
        columnMap.put("radius", radiusCol);
        columnMap.put("mass", massCol);
        columnMap.put("luminosity", luminosityCol);
        columnMap.put("ra", raCol);
        columnMap.put("declination", decCol);
        columnMap.put("parallax", paraCol);
        columnMap.put("xCoord", xCoordCol);
        columnMap.put("yCoord", yCoordCol);
        columnMap.put("zCoord", zCoordCol);
        columnMap.put("real", realCol);
        columnMap.put("comment", commentCol);
        // Additional columns (hidden by default)
        columnMap.put("commonName", commonNameCol);
        columnMap.put("constellationName", constellationNameCol);
        columnMap.put("polity", polityCol);
        columnMap.put("temperature", temperatureCol);
    }

    private void setupTable() {
        tableView.setPlaceholder(new Label("No stars to display"));

        // Selection model
        TableView.TableViewSelectionModel<StarEditRecord> selectionModel = tableView.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);
        ObservableList<StarEditRecord> selectedItems = selectionModel.getSelectedItems();
        selectedItems.addListener((ListChangeListener<StarEditRecord>) change ->
                log.debug("Selection changed: {}", change.getList()));

        // Context menu
        setupContextMenu();

        // Setup columns
        setupTableColumns();
    }

    private void setupContextMenu() {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem editItem = new MenuItem("Edit selected");
        editItem.setOnAction(event -> editSelectedStar());

        MenuItem deleteItem = new MenuItem("Delete selected");
        deleteItem.setOnAction(event -> deleteSelectedStar());

        contextMenu.getItems().addAll(editItem, deleteItem);
        tableView.setContextMenu(contextMenu);
    }

    private void setupTableColumns() {
        // Decimal formatters for cleaner display
        DecimalFormat distanceFormat = new DecimalFormat("#,##0.00");
        DecimalFormat coordFormat = new DecimalFormat("0.000");
        DecimalFormat raDecFormat = new DecimalFormat("0.0000");
        DecimalFormat generalFormat = new DecimalFormat("0.00");

        // Display Name - text column
        displayNameCol.setMinWidth(120);
        displayNameCol.setPrefWidth(140);
        displayNameCol.setCellValueFactory(new PropertyValueFactory<>("displayName"));
        displayNameCol.setSortable(true);

        // Distance - formatted decimal
        distanceToEarthCol.setMinWidth(100);
        distanceToEarthCol.setPrefWidth(110);
        distanceToEarthCol.setCellValueFactory(new PropertyValueFactory<>("distanceToEarth"));
        distanceToEarthCol.setCellFactory(createFormattedDoubleCellFactory(distanceFormat));
        distanceToEarthCol.setSortable(true);

        // Spectra - text column
        spectraCol.setMinWidth(60);
        spectraCol.setPrefWidth(70);
        spectraCol.setCellValueFactory(new PropertyValueFactory<>("spectra"));
        spectraCol.setSortable(true);

        // Radius - formatted decimal
        radiusCol.setMinWidth(70);
        radiusCol.setPrefWidth(80);
        radiusCol.setCellValueFactory(new PropertyValueFactory<>("radius"));
        radiusCol.setCellFactory(createFormattedDoubleCellFactory(generalFormat));
        radiusCol.setSortable(true);

        // Mass - formatted decimal
        massCol.setMinWidth(80);
        massCol.setPrefWidth(90);
        massCol.setCellValueFactory(new PropertyValueFactory<>("mass"));
        massCol.setCellFactory(createFormattedDoubleCellFactory(generalFormat));
        massCol.setSortable(true);

        // Luminosity - text column (already stored as formatted string)
        luminosityCol.setMinWidth(80);
        luminosityCol.setPrefWidth(90);
        luminosityCol.setCellValueFactory(new PropertyValueFactory<>("luminosity"));
        luminosityCol.setSortable(true);

        // RA - formatted decimal with more precision
        raCol.setMinWidth(90);
        raCol.setPrefWidth(100);
        raCol.setCellValueFactory(new PropertyValueFactory<>("ra"));
        raCol.setCellFactory(createFormattedDoubleCellFactory(raDecFormat));
        raCol.setSortable(true);

        // Declination - formatted decimal with more precision
        decCol.setMinWidth(90);
        decCol.setPrefWidth(100);
        decCol.setCellValueFactory(new PropertyValueFactory<>("declination"));
        decCol.setCellFactory(createFormattedDoubleCellFactory(raDecFormat));
        decCol.setSortable(true);

        // Parallax - formatted decimal
        paraCol.setMinWidth(80);
        paraCol.setPrefWidth(90);
        paraCol.setCellValueFactory(new PropertyValueFactory<>("parallax"));
        paraCol.setCellFactory(createFormattedDoubleCellFactory(generalFormat));
        paraCol.setSortable(true);

        // X coordinate - formatted decimal
        xCoordCol.setMinWidth(80);
        xCoordCol.setPrefWidth(90);
        xCoordCol.setCellValueFactory(new PropertyValueFactory<>("xCoord"));
        xCoordCol.setCellFactory(createFormattedDoubleCellFactory(coordFormat));
        xCoordCol.setSortable(true);

        // Y coordinate - formatted decimal
        yCoordCol.setMinWidth(80);
        yCoordCol.setPrefWidth(90);
        yCoordCol.setCellValueFactory(new PropertyValueFactory<>("yCoord"));
        yCoordCol.setCellFactory(createFormattedDoubleCellFactory(coordFormat));
        yCoordCol.setSortable(true);

        // Z coordinate - formatted decimal
        zCoordCol.setMinWidth(80);
        zCoordCol.setPrefWidth(90);
        zCoordCol.setCellValueFactory(new PropertyValueFactory<>("zCoord"));
        zCoordCol.setCellFactory(createFormattedDoubleCellFactory(coordFormat));
        zCoordCol.setSortable(true);

        // Real - boolean as text
        realCol.setMinWidth(50);
        realCol.setPrefWidth(60);
        realCol.setCellValueFactory(new PropertyValueFactory<>("real"));
        realCol.setSortable(true);

        // Comment - text column
        commentCol.setMinWidth(150);
        commentCol.setPrefWidth(200);
        commentCol.setCellValueFactory(new PropertyValueFactory<>("comment"));
        commentCol.setSortable(false);

        // --- Additional columns (hidden by default) ---

        // Common Name - text column
        commonNameCol.setMinWidth(100);
        commonNameCol.setPrefWidth(120);
        commonNameCol.setCellValueFactory(new PropertyValueFactory<>("commonName"));
        commonNameCol.setSortable(true);
        commonNameCol.setVisible(false);

        // Constellation - text column
        constellationNameCol.setMinWidth(100);
        constellationNameCol.setPrefWidth(120);
        constellationNameCol.setCellValueFactory(new PropertyValueFactory<>("constellationName"));
        constellationNameCol.setSortable(true);
        constellationNameCol.setVisible(false);

        // Polity - text column
        polityCol.setMinWidth(80);
        polityCol.setPrefWidth(100);
        polityCol.setCellValueFactory(new PropertyValueFactory<>("polity"));
        polityCol.setSortable(true);
        polityCol.setVisible(false);

        // Temperature - formatted decimal
        temperatureCol.setMinWidth(90);
        temperatureCol.setPrefWidth(100);
        temperatureCol.setCellValueFactory(new PropertyValueFactory<>("temperature"));
        temperatureCol.setCellFactory(createFormattedDoubleCellFactory(generalFormat));
        temperatureCol.setSortable(true);
        temperatureCol.setVisible(false);

        // Add all columns to table (including hidden ones)
        tableView.getColumns().addAll(
                displayNameCol, distanceToEarthCol, spectraCol,
                radiusCol, massCol, luminosityCol, raCol, decCol, paraCol,
                xCoordCol, yCoordCol, zCoordCol, realCol, commentCol,
                // Hidden by default
                commonNameCol, constellationNameCol, polityCol, temperatureCol
        );

        // Handle server-side sorting
        tableView.setSortPolicy(tv -> {
            if (isLoading) {
                return false;
            }
            ObservableList<TableColumn<StarEditRecord, ?>> sortOrder = tv.getSortOrder();
            if (!sortOrder.isEmpty()) {
                TableColumn<StarEditRecord, ?> sortColumn = sortOrder.get(0);
                String columnId = getColumnId(sortColumn);
                if (columnId != null && StarTableColumnConfig.isSortable(columnId)) {
                    boolean ascending = sortColumn.getSortType() == TableColumn.SortType.ASCENDING;
                    if (!columnId.equals(currentSortColumn) || ascending != currentSortAscending) {
                        currentSortColumn = columnId;
                        currentSortAscending = ascending;
                        // Reload from page 0 with new sort
                        pagination.setCurrentPageIndex(0);
                        loadPage(0);
                    }
                }
            }
            return true;
        });
    }

    /**
     * Create a cell factory that formats Double values using the given DecimalFormat.
     */
    private Callback<TableColumn<StarEditRecord, Double>, TableCell<StarEditRecord, Double>> createFormattedDoubleCellFactory(DecimalFormat format) {
        return column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(format.format(item));
                }
            }
        };
    }

    private String getColumnId(TableColumn<StarEditRecord, ?> column) {
        for (Map.Entry<String, TableColumn<StarEditRecord, ?>> entry : columnMap.entrySet()) {
            if (entry.getValue() == column) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void setupToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setPadding(new Insets(5));
        toolbar.setAlignment(Pos.CENTER_LEFT);

        Button addButton = new Button("Add Star");
        addButton.setOnAction(e -> addNewStar());

        Button exportPageButton = new Button("Export Page CSV");
        exportPageButton.setOnAction(e -> exportCurrentPageCsv());

        Button exportAllButton = new Button("Export All CSV");
        exportAllButton.setOnAction(e -> exportAllCsv());

        MenuButton columnsMenu = createColumnsMenu();

        // Jump to page
        Label jumpLabel = new Label("Go to page:");
        TextField pageField = new TextField();
        pageField.setPrefWidth(60);
        pageField.setOnAction(e -> {
            try {
                int pageNum = Integer.parseInt(pageField.getText()) - 1;
                if (pageNum >= 0 && pageNum < pagination.getPageCount()) {
                    pagination.setCurrentPageIndex(pageNum);
                }
            } catch (NumberFormatException ex) {
                // Ignore invalid input
            }
            pageField.clear();
        });

        // Spacer
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        toolbar.getChildren().addAll(
                addButton,
                new Separator(),
                exportPageButton,
                exportAllButton,
                new Separator(),
                columnsMenu,
                spacer,
                jumpLabel,
                pageField
        );

        setTop(toolbar);
    }

    private MenuButton createColumnsMenu() {
        MenuButton menuButton = new MenuButton("Columns...");

        for (String columnId : StarTableColumnConfig.getAllColumnIds()) {
            CheckMenuItem item = new CheckMenuItem(StarTableColumnConfig.getDisplayName(columnId));
            TableColumn<StarEditRecord, ?> column = columnMap.get(columnId);

            // Set initial checkbox state based on column visibility
            if (column != null) {
                item.setSelected(column.isVisible());
            } else {
                item.setSelected(columnConfig.isColumnVisible(columnId));
            }

            if (column != null) {
                item.setOnAction(e -> {
                    columnConfig.setColumnVisible(columnId, item.isSelected());
                    column.setVisible(item.isSelected());
                    // Force table to recalculate layout
                    tableView.refresh();
                });
            }
            menuButton.getItems().add(item);
        }

        menuButton.getItems().add(new SeparatorMenuItem());

        MenuItem resetItem = new MenuItem("Reset to Defaults");
        resetItem.setOnAction(e -> {
            columnConfig.resetToDefaults();
            for (MenuItem mi : menuButton.getItems()) {
                if (mi instanceof CheckMenuItem cmi) {
                    String text = cmi.getText();
                    for (String colId : StarTableColumnConfig.getAllColumnIds()) {
                        if (StarTableColumnConfig.getDisplayName(colId).equals(text)) {
                            TableColumn<StarEditRecord, ?> col = columnMap.get(colId);
                            boolean shouldBeVisible = columnConfig.isColumnVisible(colId);
                            cmi.setSelected(shouldBeVisible);
                            if (col != null) {
                                col.setVisible(shouldBeVisible);
                            }
                            break;
                        }
                    }
                }
            }
            tableView.refresh();
        });
        menuButton.getItems().add(resetItem);

        return menuButton;
    }

    private void setupPagination() {
        pagination.setPageCount(1);
        pagination.setCurrentPageIndex(0);
        pagination.setMaxPageIndicatorCount(10);

        pagination.currentPageIndexProperty().addListener((obs, oldVal, newVal) -> {
            if (!isLoading && newVal != null) {
                loadPage(newVal.intValue());
            }
        });
    }

    private void setupLayout() {
        VBox centerBox = new VBox(5);
        centerBox.setPadding(new Insets(5));
        VBox.setVgrow(tableView, Priority.ALWAYS);
        centerBox.getChildren().addAll(tableView, pagination);

        setCenter(centerBox);

        HBox statusBar = new HBox(10);
        statusBar.setPadding(new Insets(5));
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusBar.getChildren().add(statusLabel);
        setBottom(statusBar);
    }

    private void loadTotalCount() {
        isLoading = true;
        statusLabel.setText("Loading...");

        // Run count query in background
        Thread countThread = new Thread(() -> {
            try {
                long count = starService.countBySearchQuery(query);
                Platform.runLater(() -> {
                    totalCount = count;
                    int pageCount = Math.max(1, (int) Math.ceil((double) count / PAGE_SIZE));
                    pagination.setPageCount(pageCount);
                    loadPage(0);
                });
            } catch (Exception e) {
                log.error("Error loading count", e);
                Platform.runLater(() -> {
                    isLoading = false;
                    statusLabel.setText("Error loading data: " + e.getMessage());
                });
            }
        });
        countThread.setDaemon(true);
        countThread.start();
    }

    private void loadPage(int pageIndex) {
        isLoading = true;
        statusLabel.setText("Loading page " + (pageIndex + 1) + "...");
        tableView.getItems().clear();

        Thread loadThread = new Thread(() -> {
            try {
                Sort sort = StarTableColumnConfig.toSpringSort(currentSortColumn, currentSortAscending);
                Pageable pageable = PageRequest.of(pageIndex, PAGE_SIZE, sort);
                Page<StarObject> page = starService.getStarPaged(query, pageable);

                List<StarEditRecord> records = page.getContent().stream()
                        .filter(obj -> obj.getDisplayName() != null && !obj.getDisplayName().equalsIgnoreCase("name"))
                        .map(StarEditRecord::fromAstrographicObject)
                        .toList();

                Platform.runLater(() -> {
                    tableView.getItems().setAll(records);
                    updateStatus(pageIndex, page.getTotalElements());
                    isLoading = false;
                });
            } catch (Exception e) {
                log.error("Error loading page", e);
                Platform.runLater(() -> {
                    isLoading = false;
                    statusLabel.setText("Error loading page: " + e.getMessage());
                });
            }
        });
        loadThread.setDaemon(true);
        loadThread.start();
    }

    private void updateStatus(int pageIndex, long totalElements) {
        int start = pageIndex * PAGE_SIZE + 1;
        int end = Math.min((pageIndex + 1) * PAGE_SIZE, (int) totalElements);
        statusLabel.setText(String.format("Showing %d - %d of %d stars | Dataset: %s",
                start, end, totalElements, dataSetName));
    }

    private void editSelectedStar() {
        StarEditRecord selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showErrorAlert("Edit Star", "No star selected.");
            return;
        }

        StarObject starObject = starService.getStar(selected.getId());
        if (starObject == null) {
            showErrorAlert("Edit Star", "Star not found in database.");
            return;
        }

        StarEditDialog dialog = new StarEditDialog(starObject);
        Optional<StarEditStatus> result = dialog.showAndWait();
        if (result.isPresent() && result.get().isChanged()) {
            starService.updateStar(result.get().getRecord());
            // Reload current page
            loadPage(pagination.getCurrentPageIndex());
        }
    }

    private void deleteSelectedStar() {
        StarEditRecord selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showErrorAlert("Delete Star", "No star selected.");
            return;
        }

        Optional<ButtonType> confirm = showConfirmationAlert(
                "Delete Star",
                "Confirm Delete",
                "Are you sure you want to delete: " + selected.getDisplayName() + "?"
        );

        if (confirm.isPresent() && confirm.get() == ButtonType.OK) {
            starService.removeStar(selected.getId());
            tableView.getItems().remove(selected);
            totalCount--;
            int pageCount = Math.max(1, (int) Math.ceil((double) totalCount / PAGE_SIZE));
            pagination.setPageCount(pageCount);
            updateStatus(pagination.getCurrentPageIndex(), totalCount);
        }
    }

    private void addNewStar() {
        StarObject starObject = new StarObject();
        starObject.setId(UUID.randomUUID().toString());
        starObject.setDataSetName(dataSetName);

        StarEditDialog dialog = new StarEditDialog(starObject);
        Optional<StarEditStatus> result = dialog.showAndWait();
        if (result.isPresent() && result.get().isChanged()) {
            starService.addStar(result.get().getRecord());
            // Reload to reflect new star
            loadTotalCount();
        }
    }

    private void exportCurrentPageCsv() {
        if (tableView.getItems().isEmpty()) {
            showErrorAlert("Export CSV", "No rows on this page to export.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Page to CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName(dataSetName + "-page-" + (pagination.getCurrentPageIndex() + 1) + ".csv");

        Window window = getScene() != null ? getScene().getWindow() : null;
        File file = fileChooser.showSaveDialog(window);
        if (file == null) {
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writeHeader(writer);
            for (StarEditRecord record : tableView.getItems()) {
                writeLine(writer, record);
            }
            statusLabel.setText("Exported " + tableView.getItems().size() + " records to " + file.getName());
        } catch (IOException e) {
            log.error("Failed to export CSV", e);
            showErrorAlert("Export CSV", "Failed to export: " + e.getMessage());
        }
    }

    private void exportAllCsv() {
        if (totalCount == 0) {
            showErrorAlert("Export CSV", "No stars to export.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export All Stars to CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName(dataSetName + "-all.csv");

        Window window = getScene() != null ? getScene().getWindow() : null;
        File file = fileChooser.showSaveDialog(window);
        if (file == null) {
            return;
        }

        // Create and start export service
        StarTableExportService exportService = new StarTableExportService(starService, query, file);

        // Progress dialog
        Alert progressAlert = new Alert(Alert.AlertType.INFORMATION);
        progressAlert.setTitle("Exporting...");
        progressAlert.setHeaderText("Exporting all stars to CSV");
        progressAlert.getButtonTypes().setAll(ButtonType.CANCEL);

        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(300);
        Label progressLabel = new Label("Starting export...");

        VBox content = new VBox(10);
        content.getChildren().addAll(progressLabel, progressBar);
        progressAlert.getDialogPane().setContent(content);

        progressBar.progressProperty().bind(exportService.progressProperty());
        exportService.messageProperty().addListener((obs, old, msg) ->
                Platform.runLater(() -> progressLabel.setText(msg)));

        exportService.setOnSucceeded(e -> {
            progressAlert.close();
            Long count = exportService.getValue();
            statusLabel.setText("Exported " + count + " records to " + file.getName());
        });

        exportService.setOnFailed(e -> {
            progressAlert.close();
            Throwable ex = exportService.getException();
            log.error("Export failed", ex);
            showErrorAlert("Export Failed", "Export failed: " + (ex != null ? ex.getMessage() : "Unknown error"));
        });

        exportService.setOnCancelled(e -> {
            progressAlert.close();
            statusLabel.setText("Export cancelled.");
        });

        progressAlert.setOnCloseRequest(e -> {
            if (exportService.isRunning()) {
                exportService.cancel();
            }
        });

        exportService.start();
        progressAlert.show();
    }

    private void writeHeader(BufferedWriter writer) throws IOException {
        writer.write(String.join(",",
                "Display Name",
                "Distance (LY)",
                "Spectra",
                "Radius",
                "Mass",
                "Luminosity",
                "RA",
                "Declination",
                "Parallax",
                "X",
                "Y",
                "Z",
                "Real",
                "Comment"));
        writer.newLine();
    }

    private void writeLine(BufferedWriter writer, StarEditRecord record) throws IOException {
        writer.write(String.join(",",
                csvCell(record.getDisplayName()),
                csvCell(record.getDistanceToEarth()),
                csvCell(record.getSpectra()),
                csvCell(record.getRadius()),
                csvCell(record.getMass()),
                csvCell(record.getLuminosity()),
                csvCell(record.getRa()),
                csvCell(record.getDeclination()),
                csvCell(record.getParallax()),
                csvCell(record.getXCoord()),
                csvCell(record.getYCoord()),
                csvCell(record.getZCoord()),
                csvCell(record.isReal()),
                csvCell(record.getComment())));
        writer.newLine();
    }

    private String csvCell(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

    private String csvCell(Double value) {
        return value == null ? "" : value.toString();
    }

    private String csvCell(Boolean value) {
        return value == null ? "" : value.toString();
    }

    /**
     * Refresh the current page data
     */
    public void refresh() {
        loadPage(pagination.getCurrentPageIndex());
    }

    /**
     * Get the current total count of stars
     *
     * @return total count
     */
    public long getTotalCount() {
        return totalCount;
    }
}
