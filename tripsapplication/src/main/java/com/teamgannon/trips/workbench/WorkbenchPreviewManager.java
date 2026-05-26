package com.teamgannon.trips.workbench;

import com.teamgannon.trips.workbench.service.WorkbenchCsvService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Manages the Data Workbench's preview table: opens a CSV source, counts rows,
 * rebuilds the table columns from the active mappings, drives the pagination
 * control, and lazily loads a page on demand.
 * <p>
 * Extracted from {@code DataWorkbenchController} in Phase 4.4 of the
 * codebase-review remediation. Owns the runtime preview state (source path,
 * header index, target-to-source map, total-row count, the rendered rows);
 * borrows the {@link TableView}, {@link Pagination}, and the active mappings
 * list from the controller. Errors and status updates are routed back to the
 * controller via {@link Consumer} callbacks so this class doesn't need to know
 * about {@code Alert}s, validation logs, or status labels.
 *
 * <h2>Threading</h2>
 * All public methods must be called on the JavaFX Application thread.
 */
public final class WorkbenchPreviewManager {

    private static final int DEFAULT_PAGE_SIZE = 100;

    // --- Borrowed UI / model references (owned by the controller) ---
    private final TableView<Map<String, String>> previewTable;
    private final Pagination previewPagination;
    private final ObservableList<MappingRow> mappings;
    private final WorkbenchCsvService csvService;

    // --- Callbacks back to the controller ---
    /** Called as {@code (title, message)} on errors. */
    private final BiConsumer<String, String> errorReporter;
    /** Called with the status bar message. */
    private final Consumer<String> statusReporter;
    /** Called for each validation-log line. */
    private final Consumer<String> validationLogReporter;

    // --- Owned state ---
    private final ObservableList<Map<String, String>> previewRows = FXCollections.observableArrayList();
    private final int pageSize;

    private Path previewSourcePath;
    private Map<String, Integer> previewHeaderIndex = new HashMap<>();
    private Map<String, String> previewTargetToSource = new HashMap<>();
    private int previewTotalRows = 0;

    public WorkbenchPreviewManager(TableView<Map<String, String>> previewTable,
                                   Pagination previewPagination,
                                   ObservableList<MappingRow> mappings,
                                   WorkbenchCsvService csvService,
                                   BiConsumer<String, String> errorReporter,
                                   Consumer<String> statusReporter,
                                   Consumer<String> validationLogReporter) {
        this(previewTable, previewPagination, mappings, csvService,
                errorReporter, statusReporter, validationLogReporter, DEFAULT_PAGE_SIZE);
    }

    public WorkbenchPreviewManager(TableView<Map<String, String>> previewTable,
                                   Pagination previewPagination,
                                   ObservableList<MappingRow> mappings,
                                   WorkbenchCsvService csvService,
                                   BiConsumer<String, String> errorReporter,
                                   Consumer<String> statusReporter,
                                   Consumer<String> validationLogReporter,
                                   int pageSize) {
        this.previewTable = previewTable;
        this.previewPagination = previewPagination;
        this.mappings = mappings;
        this.csvService = csvService;
        this.errorReporter = errorReporter;
        this.statusReporter = statusReporter;
        this.validationLogReporter = validationLogReporter;
        this.pageSize = pageSize;
    }

    /** Wire pagination-page-change to {@link #loadPreviewPage(int)}. Call once during controller {@code initialize()}. */
    public void install() {
        if (previewTable != null) {
            previewTable.setPlaceholder(new Label("No data loaded yet."));
        }
        if (previewPagination != null) {
            previewPagination.setPageCount(1);
            previewPagination.currentPageIndexProperty().addListener((obs, oldVal, newVal) -> {
                if (previewSourcePath != null) {
                    loadPreviewPage(newVal.intValue());
                }
            });
        }
    }

    /** True if the preview table has at least one row loaded. Used to gate "Validate Preview" etc. */
    public boolean hasRows() {
        return !previewRows.isEmpty();
    }

    /** Direct read access to the currently-rendered rows (used by validators / exporters). */
    public ObservableList<Map<String, String>> getPreviewRows() {
        return previewRows;
    }

    /** Open the given CSV source, build the preview, and load page 1. */
    public void loadPreviewFromPath(Path path) {
        previewSourcePath = path;
        try (BufferedReader reader = Files.newBufferedReader(previewSourcePath, StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (header == null) {
                errorReporter.accept("Preview", "Source file is empty.");
                return;
            }
            previewHeaderIndex = csvService.buildHeaderIndex(header);
            previewTargetToSource = csvService.buildTargetToSourceMap(mappings);
            previewTotalRows = countRows(previewSourcePath);
            previewRows.clear();
            updatePagination();
            loadPreviewPage(0);
            rebuildPreviewTable();
            validationLogReporter.accept("Loaded " + previewRows.size() + " preview rows.");
            statusReporter.accept("Preview page 1 / " + (previewPagination == null ? 1 : previewPagination.getPageCount()));
        } catch (IOException e) {
            errorReporter.accept("Preview", "Unable to read source CSV: " + e.getMessage());
        }
    }

    /** Rebuild the table columns from the active mappings and re-bind the rows. Safe to call repeatedly. */
    public void rebuildPreviewTable() {
        if (previewTable == null) {
            return;
        }
        previewTable.getColumns().clear();
        List<String> previewColumns = new ArrayList<>();
        for (MappingRow mapping : mappings) {
            if (!previewColumns.contains(mapping.getTargetField())) {
                previewColumns.add(mapping.getTargetField());
            }
        }
        for (String columnName : previewColumns) {
            TableColumn<Map<String, String>, String> column = new TableColumn<>(columnName);
            column.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getOrDefault(columnName, "")));
            column.setPrefWidth(140);
            previewTable.getColumns().add(column);
        }
        previewTable.setItems(previewRows);
    }

    // --- internal ---

    private int countRows(Path path) throws IOException {
        int count = 0;
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            while (reader.readLine() != null) {
                count++;
            }
        }
        return Math.max(0, count - 1);   // exclude the header row
    }

    private void updatePagination() {
        if (previewPagination == null) {
            return;
        }
        int pageCount = Math.max(1, (int) Math.ceil((double) previewTotalRows / pageSize));
        previewPagination.setPageCount(pageCount);
        previewPagination.setCurrentPageIndex(0);
    }

    private void loadPreviewPage(int pageIndex) {
        if (previewSourcePath == null) {
            return;
        }
        previewRows.clear();
        int startIndex = pageIndex * pageSize;
        int endIndex = startIndex + pageSize;
        try (BufferedReader reader = Files.newBufferedReader(previewSourcePath, StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (header == null) {
                return;
            }
            String line;
            int rowIndex = 0;
            while ((line = reader.readLine()) != null) {
                if (rowIndex >= startIndex && rowIndex < endIndex) {
                    String[] values = csvService.splitCsvLine(line);
                    previewRows.add(csvService.mapRow(values, previewHeaderIndex, previewTargetToSource));
                }
                rowIndex++;
                if (rowIndex >= endIndex) {
                    break;
                }
            }
        } catch (IOException e) {
            errorReporter.accept("Preview", "Unable to load preview page: " + e.getMessage());
        }
        rebuildPreviewTable();
        if (previewPagination != null) {
            statusReporter.accept("Preview page " + (pageIndex + 1) + " / " + previewPagination.getPageCount());
        }
    }
}
