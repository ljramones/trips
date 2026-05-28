package com.teamgannon.trips.workbench;

import com.teamgannon.trips.service.DatasetService;
import com.teamgannon.trips.workbench.model.ExoplanetMatchRow;
import com.teamgannon.trips.workbench.model.ExoplanetPreviewRow;
import com.teamgannon.trips.workbench.service.WorkbenchExoplanetImportService;
import com.teamgannon.trips.workbench.service.WorkbenchExoplanetImportService.ExoplanetCsvRow;
import com.teamgannon.trips.workbench.service.WorkbenchExoplanetImportService.ExoplanetImportResult;
import com.teamgannon.trips.workbench.service.WorkbenchExoplanetImportService.ExoplanetMatch;
import com.teamgannon.trips.workbench.service.WorkbenchExoplanetImportService.ExoplanetMatchResult;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Owns the Data Workbench "Exoplanets" tab: state (parsed exoplanets,
 * match result, observable preview + match row lists), the four button
 * handlers (load CSV / match / import / clear), and the four
 * tab-specific status helpers (status / file-status / match-stats / log).
 * <p>
 * Extracted from {@link DataWorkbenchController} in Phase 4.4 of the
 * codebase-review remediation (Issue 18). The original Phase 4.4 status
 * called out a "context-object pattern" as the gating step; this class
 * realises it: the controller keeps its {@code @FXML} fields (JavaFX
 * reflection requires that), bundles them into a {@link Bindings} record,
 * passes the bundle here once via {@link #bind}, and forwards each
 * {@code @FXML} on-action method to a one-line delegate. The tab class
 * owns everything else.
 *
 * <h2>Threading</h2>
 * Status helpers are FX-thread safe (auto-wrap with {@code Platform.runLater}).
 * Public action methods must be called on the FX Application thread.
 */
@Slf4j
public class WorkbenchExoplanetTab {

    /**
     * @FXML control bundle. The controller fills this in after JavaFX has
     * resolved its {@code @FXML} fields, then hands it to {@link #bind}.
     */
    public record Bindings(
            TableView<ExoplanetPreviewRow> previewTable,
            TableView<ExoplanetMatchRow> matchTable,
            Label fileStatusLabel,
            Label matchStatsLabel,
            CheckBox skipDuplicatesCheckbox,
            ProgressBar progressBar,
            TextArea logArea,
            TableColumn<ExoplanetPreviewRow, String> nameCol,
            TableColumn<ExoplanetPreviewRow, String> starNameCol,
            TableColumn<ExoplanetPreviewRow, String> smaCol,
            TableColumn<ExoplanetPreviewRow, String> massCol,
            TableColumn<ExoplanetPreviewRow, String> radiusCol,
            TableColumn<ExoplanetPreviewRow, String> periodCol,
            TableColumn<ExoplanetPreviewRow, String> statusCol,
            TableColumn<ExoplanetMatchRow, Boolean> matchSelectCol,
            TableColumn<ExoplanetMatchRow, String> matchExoNameCol,
            TableColumn<ExoplanetMatchRow, String> matchCsvStarCol,
            TableColumn<ExoplanetMatchRow, String> matchMatchedStarCol,
            TableColumn<ExoplanetMatchRow, String> matchTypeCol,
            TableColumn<ExoplanetMatchRow, String> matchConfidenceCol) {
    }

    private final WorkbenchExoplanetImportService importService;
    private final DatasetService datasetService;
    private final WorkbenchSourceActions sourceActions;
    private final BiConsumer<String, String> showError;
    private final Supplier<Window> windowSupplier;

    private Bindings bindings;

    private final ObservableList<ExoplanetPreviewRow> previewRows = FXCollections.observableArrayList();
    private final ObservableList<ExoplanetMatchRow> matchRows = FXCollections.observableArrayList();
    private List<ExoplanetCsvRow> parsedExoplanets = new ArrayList<>();
    private ExoplanetMatchResult matchResult;

    public WorkbenchExoplanetTab(WorkbenchExoplanetImportService importService,
                                 DatasetService datasetService,
                                 WorkbenchSourceActions sourceActions,
                                 BiConsumer<String, String> showError,
                                 Supplier<Window> windowSupplier) {
        this.importService = importService;
        this.datasetService = datasetService;
        this.sourceActions = sourceActions;
        this.showError = showError;
        this.windowSupplier = windowSupplier;
    }

    /**
     * Wire up the FXML controls and install the cell value factories.
     * Must be called once, on the FX thread, after JavaFX has populated
     * the controller's {@code @FXML} fields.
     */
    public void bind(Bindings bindings) {
        this.bindings = bindings;
        installCellFactories();

        if (bindings.previewTable != null) {
            bindings.previewTable.setItems(previewRows);
            bindings.previewTable.setPlaceholder(new Label("Load an exoplanet.eu CSV file to see data here."));
        }
        if (bindings.matchTable != null) {
            bindings.matchTable.setItems(matchRows);
            bindings.matchTable.setEditable(true);
            bindings.matchTable.setPlaceholder(new Label("Run 'Match to Stars' to see matching results."));
        }
        if (bindings.logArea != null) {
            bindings.logArea.setEditable(false);
        }
    }

    private void installCellFactories() {
        bindIfNotNull(bindings.nameCol, new PropertyValueFactory<>("name"));
        bindIfNotNull(bindings.starNameCol, new PropertyValueFactory<>("starName"));
        bindIfNotNull(bindings.smaCol, new PropertyValueFactory<>("semiMajorAxis"));
        bindIfNotNull(bindings.massCol, new PropertyValueFactory<>("mass"));
        bindIfNotNull(bindings.radiusCol, new PropertyValueFactory<>("radius"));
        bindIfNotNull(bindings.periodCol, new PropertyValueFactory<>("orbitalPeriod"));
        bindIfNotNull(bindings.statusCol, new PropertyValueFactory<>("planetStatus"));

        if (bindings.matchSelectCol != null) {
            bindings.matchSelectCol.setCellValueFactory(cd -> cd.getValue().selectedProperty());
            bindings.matchSelectCol.setCellFactory(CheckBoxTableCell.forTableColumn(bindings.matchSelectCol));
        }
        bindIfNotNull(bindings.matchExoNameCol, new PropertyValueFactory<>("exoplanetName"));
        bindIfNotNull(bindings.matchCsvStarCol, new PropertyValueFactory<>("csvStarName"));
        bindIfNotNull(bindings.matchMatchedStarCol, new PropertyValueFactory<>("matchedStarName"));
        bindIfNotNull(bindings.matchTypeCol, new PropertyValueFactory<>("matchType"));
        bindIfNotNull(bindings.matchConfidenceCol, new PropertyValueFactory<>("confidence"));
    }

    private static <S, T> void bindIfNotNull(TableColumn<S, T> col,
                                             PropertyValueFactory<S, T> factory) {
        if (col != null) {
            col.setCellValueFactory(factory);
        }
    }

    // ==================== Public action handlers ====================

    public void onLoadCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Exoplanet Catalog CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));
        sourceActions.applyInitialDirectory(fileChooser);

        File file = fileChooser.showOpenDialog(windowSupplier.get());
        if (file == null) {
            return;
        }

        updateStatus("Loading " + file.getName() + "...");
        setProgressVisible(true);

        Task<List<ExoplanetCsvRow>> task = new Task<>() {
            @Override
            protected List<ExoplanetCsvRow> call() throws Exception {
                return importService.parseCsvFile(file.toPath(), WorkbenchExoplanetTab.this::updateStatus);
            }
        };
        task.setOnSucceeded(event -> {
            parsedExoplanets = task.getValue();
            previewRows.setAll(importService.toPreviewRows(parsedExoplanets));
            updateFileStatus("Loaded " + parsedExoplanets.size() + " exoplanets from " + file.getName());
            setProgressVisible(false);
            appendLog("Loaded " + parsedExoplanets.size() + " exoplanets from " + file.getName());

            matchRows.clear();
            matchResult = null;
            updateMatchStats("");
        });
        task.setOnFailed(event -> {
            showError.accept("Load Exoplanets", String.valueOf(task.getException().getMessage()));
            setProgressVisible(false);
        });
        startDaemon(task);
    }

    public void onMatch() {
        if (parsedExoplanets == null || parsedExoplanets.isEmpty()) {
            showError.accept("Match Exoplanets", "Load an exoplanet CSV file first.");
            return;
        }

        List<String> datasetNames = datasetService.getDescriptors().stream()
                .map(descriptor -> descriptor.getDataSetName())
                .sorted()
                .collect(Collectors.toList());

        if (datasetNames.isEmpty()) {
            showError.accept("Match Exoplanets", "No datasets available. Load a star dataset first.");
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(datasetNames.get(0), datasetNames);
        dialog.setTitle("Match Exoplanets to Stars");
        dialog.setHeaderText("Select the dataset to match against");
        dialog.setContentText("Dataset:");
        Optional<String> selection = dialog.showAndWait();
        if (selection.isEmpty()) {
            return;
        }

        String dataSetName = selection.get();
        updateStatus("Matching exoplanets to stars in " + dataSetName + "...");
        setProgressVisible(true);

        Task<ExoplanetMatchResult> task = new Task<>() {
            @Override
            protected ExoplanetMatchResult call() {
                return importService.matchExoplanetsToStars(parsedExoplanets, dataSetName,
                        WorkbenchExoplanetTab.this::updateStatus);
            }
        };
        task.setOnSucceeded(event -> {
            matchResult = task.getValue();
            matchRows.setAll(importService.toMatchRows(matchResult));

            String stats = String.format("Matched: %d exact, %d fuzzy, %d RA/Dec, %d unmatched",
                    matchResult.getExactMatches(), matchResult.getFuzzyMatches(),
                    matchResult.getRaDecMatches(), matchResult.getUnmatched());
            updateMatchStats(stats);
            appendLog("Matching complete: " + stats);
            setProgressVisible(false);
        });
        task.setOnFailed(event -> {
            showError.accept("Match Exoplanets", String.valueOf(task.getException().getMessage()));
            setProgressVisible(false);
        });
        startDaemon(task);
    }

    public void onImport() {
        if (matchResult == null || matchResult.getMatches().isEmpty()) {
            showError.accept("Import Exoplanets", "Run 'Match to Stars' first.");
            return;
        }

        List<ExoplanetMatch> selectedMatches = new ArrayList<>();
        for (int i = 0; i < matchRows.size(); i++) {
            ExoplanetMatchRow row = matchRows.get(i);
            if (row.isSelected() && row.hasMatch()) {
                selectedMatches.add(matchResult.getMatches().get(i));
            }
        }
        if (selectedMatches.isEmpty()) {
            showError.accept("Import Exoplanets", "No matched exoplanets selected for import.");
            return;
        }

        boolean skipDuplicates = bindings.skipDuplicatesCheckbox != null
                && bindings.skipDuplicatesCheckbox.isSelected();

        updateStatus("Importing " + selectedMatches.size() + " exoplanets...");
        setProgressVisible(true);

        Task<ExoplanetImportResult> task = new Task<>() {
            @Override
            protected ExoplanetImportResult call() {
                return importService.importMatchedExoplanets(selectedMatches, skipDuplicates,
                        WorkbenchExoplanetTab.this::updateStatus);
            }
        };
        task.setOnSucceeded(event -> {
            ExoplanetImportResult result = task.getValue();
            String summary = String.format("Import complete: %d imported, %d skipped, %d solar systems created",
                    result.getImported(), result.getSkipped(), result.getSolarSystemsCreated());
            updateStatus(summary);
            appendLog(summary);

            if (!result.getErrors().isEmpty()) {
                appendLog("Errors (" + result.getErrors().size() + "):");
                for (String error : result.getErrors().subList(0, Math.min(10, result.getErrors().size()))) {
                    appendLog("  " + error);
                }
                if (result.getErrors().size() > 10) {
                    appendLog("  ... and " + (result.getErrors().size() - 10) + " more errors");
                }
            }
            setProgressVisible(false);
        });
        task.setOnFailed(event -> {
            showError.accept("Import Exoplanets", String.valueOf(task.getException().getMessage()));
            setProgressVisible(false);
        });
        startDaemon(task);
    }

    public void onClear() {
        parsedExoplanets.clear();
        previewRows.clear();
        matchRows.clear();
        matchResult = null;
        updateFileStatus("No file loaded");
        updateMatchStats("");
        if (bindings.logArea != null) {
            bindings.logArea.clear();
        }
        updateStatus("Cleared exoplanet data");
    }

    // ==================== Status helpers (FX-thread safe) ====================

    private void updateStatus(String message) {
        // Treated as a log line — the file status label stays put for
        // the "loaded X from Y" message instead.
        runOnFxThread(() -> appendLog(message));
    }

    private void updateFileStatus(String message) {
        runOnFxThread(() -> {
            if (bindings.fileStatusLabel != null) {
                bindings.fileStatusLabel.setText(message);
            }
        });
    }

    private void updateMatchStats(String message) {
        runOnFxThread(() -> {
            if (bindings.matchStatsLabel != null) {
                bindings.matchStatsLabel.setText(message);
            }
        });
    }

    private void appendLog(String message) {
        runOnFxThread(() -> {
            if (bindings.logArea != null) {
                bindings.logArea.appendText(message + System.lineSeparator());
            }
        });
    }

    private void setProgressVisible(boolean visible) {
        runOnFxThread(() -> {
            if (bindings.progressBar != null) {
                bindings.progressBar.setVisible(visible);
            }
        });
    }

    private static void runOnFxThread(Runnable r) {
        if (Platform.isFxApplicationThread()) {
            r.run();
        } else {
            Platform.runLater(r);
        }
    }

    private static void startDaemon(Task<?> task) {
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
}
