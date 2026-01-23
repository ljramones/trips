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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    private final StarTableColumnFactory columnFactory;
    private final StarTableCsvExporter csvExporter;

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
        this.columnFactory = new StarTableColumnFactory();
        this.csvExporter = new StarTableCsvExporter(starService, query, dataSetName);

        setupTable();
        setupToolbar();
        setupPagination();
        setupLayout();

        // Load initial data
        loadTotalCount();
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

        // Configure columns using factory
        columnFactory.configureColumns(tableView);

        // Setup server-side sorting
        columnFactory.setupServerSideSorting(
                tableView,
                currentSortColumn,
                currentSortAscending,
                () -> isLoading,
                (columnId, ascending) -> {
                    currentSortColumn = columnId;
                    currentSortAscending = ascending;
                    pagination.setCurrentPageIndex(0);
                    loadPage(0);
                }
        );
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

    private void setupToolbar() {
        StarTableToolbar toolbar = new StarTableToolbar(
                columnConfig,
                columnFactory,
                tableView,
                pagination,
                this::addNewStar,
                this::exportCurrentPageCsv,
                this::exportAllCsv
        );
        setTop(toolbar);
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
            loadTotalCount();
        }
    }

    private void exportCurrentPageCsv() {
        Window window = getScene() != null ? getScene().getWindow() : null;
        csvExporter.exportCurrentPage(
                tableView.getItems(),
                pagination.getCurrentPageIndex(),
                window,
                statusLabel::setText
        );
    }

    private void exportAllCsv() {
        Window window = getScene() != null ? getScene().getWindow() : null;
        csvExporter.exportAll(totalCount, window, statusLabel::setText);
    }

    /**
     * Refresh the current page data.
     */
    public void refresh() {
        loadPage(pagination.getCurrentPageIndex());
    }

    /**
     * Get the current total count of stars.
     *
     * @return total count
     */
    public long getTotalCount() {
        return totalCount;
    }
}
