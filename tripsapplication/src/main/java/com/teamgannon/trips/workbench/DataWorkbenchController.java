package com.teamgannon.trips.workbench;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Pagination;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ProgressBar;
import javafx.application.Platform;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import com.teamgannon.trips.events.StatusUpdateEvent;
import com.teamgannon.trips.service.DatasetService;
import com.teamgannon.trips.service.StarService;
import com.teamgannon.trips.workbench.model.ExoplanetMatchRow;
import com.teamgannon.trips.workbench.model.ExoplanetPreviewRow;
import com.teamgannon.trips.workbench.model.WorkbenchCsvSchema;
import com.teamgannon.trips.workbench.service.WorkbenchCsvService;
import com.teamgannon.trips.workbench.service.WorkbenchEnrichmentService;
import com.teamgannon.trips.workbench.service.WorkbenchExoplanetImportService;
import com.teamgannon.trips.workbench.service.WorkbenchExoplanetImportService.ExoplanetCsvRow;
import com.teamgannon.trips.workbench.service.WorkbenchExoplanetImportService.ExoplanetMatch;
import com.teamgannon.trips.workbench.service.WorkbenchExoplanetImportService.ExoplanetMatchResult;
import com.teamgannon.trips.workbench.service.WorkbenchExoplanetImportService.ExoplanetImportResult;
import com.teamgannon.trips.workbench.service.WorkbenchMappingDefaults;
import com.teamgannon.trips.workbench.service.WorkbenchTapService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component
@Slf4j
public class DataWorkbenchController {

    private static final DateTimeFormatter HYG_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("ddMMyyyyHHmmss");

    @FXML
    private TabPane workbenchTabs;

    @FXML
    private ListView<WorkbenchSource> sourceListView;

    @FXML
    private ListView<String> sourceFieldsListView;

    @FXML
    private ListView<String> targetFieldsListView;

    @FXML
    private TableView<MappingRow> mappingTable;

    @FXML
    private TableView<Map<String, String>> previewTable;

    @FXML
    private Pagination previewPagination;

    @FXML
    private TextArea validationLog;

    @FXML
    private Label sourceStatusLabel;

    @FXML
    private Label mappingStatusLabel;

    @FXML
    private Label exportStatusLabel;

    @FXML
    private TextField cacheDirField;

    @FXML
    private CheckBox cacheDefaultCheckbox;

    @FXML
    private TextField liveTapBatchField;

    @FXML
    private TextField liveTapBackoffField;

    @FXML
    private ProgressBar enrichmentProgressBar;

    // Exoplanet tab FXML fields
    @FXML
    private TableView<ExoplanetPreviewRow> exoplanetPreviewTable;

    @FXML
    private TableView<ExoplanetMatchRow> exoplanetMatchTable;

    @FXML
    private Label exoplanetFileStatusLabel;

    @FXML
    private Label exoplanetMatchStatsLabel;

    @FXML
    private CheckBox skipDuplicatesCheckbox;

    @FXML
    private ProgressBar exoplanetProgressBar;

    @FXML
    private TextArea exoplanetLogArea;

    @FXML
    private TableColumn<ExoplanetPreviewRow, String> exoNameCol;

    @FXML
    private TableColumn<ExoplanetPreviewRow, String> exoStarNameCol;

    @FXML
    private TableColumn<ExoplanetPreviewRow, String> exoSmaCol;

    @FXML
    private TableColumn<ExoplanetPreviewRow, String> exoMassCol;

    @FXML
    private TableColumn<ExoplanetPreviewRow, String> exoRadiusCol;

    @FXML
    private TableColumn<ExoplanetPreviewRow, String> exoPeriodCol;

    @FXML
    private TableColumn<ExoplanetPreviewRow, String> exoStatusCol;

    @FXML
    private TableColumn<ExoplanetMatchRow, Boolean> matchSelectCol;

    @FXML
    private TableColumn<ExoplanetMatchRow, String> matchExoNameCol;

    @FXML
    private TableColumn<ExoplanetMatchRow, String> matchCsvStarCol;

    @FXML
    private TableColumn<ExoplanetMatchRow, String> matchMatchedStarCol;

    @FXML
    private TableColumn<ExoplanetMatchRow, String> matchTypeCol;

    @FXML
    private TableColumn<ExoplanetMatchRow, String> matchConfidenceCol;

    private final ApplicationEventPublisher eventPublisher;
    private final StarService starService;
    private final DatasetService datasetService;
    private final WorkbenchEnrichmentService enrichmentService;
    private final WorkbenchCsvService csvService;
    private final WorkbenchTapService tapService;
    private final WorkbenchExoplanetImportService exoplanetImportService;

    private final ObservableList<WorkbenchSource> sources = FXCollections.observableArrayList();
    private final ObservableList<String> sourceFields = FXCollections.observableArrayList();
    private final ObservableList<String> targetFields = FXCollections.observableArrayList(WorkbenchCsvSchema.CSV_HEADER_COLUMNS);
    private final ObservableList<MappingRow> mappings = FXCollections.observableArrayList();
    private final ObservableList<Map<String, String>> previewRows = FXCollections.observableArrayList();
    private Path cacheDir;
    private Path previewSourcePath;
    private Map<String, Integer> previewHeaderIndex = new HashMap<>();
    private Map<String, String> previewTargetToSource = new HashMap<>();
    private int previewTotalRows = 0;
    private final int previewPageSize = 100;
    private WorkbenchSourceActions sourceActions;

    // Exoplanet import state
    private final ObservableList<ExoplanetPreviewRow> exoplanetPreviewRows = FXCollections.observableArrayList();
    private final ObservableList<ExoplanetMatchRow> exoplanetMatchRows = FXCollections.observableArrayList();
    private List<ExoplanetCsvRow> parsedExoplanets = new ArrayList<>();
    private ExoplanetMatchResult exoplanetMatchResult;
    public DataWorkbenchController(ApplicationEventPublisher eventPublisher,
                                   StarService starService,
                                   DatasetService datasetService,
                                   WorkbenchEnrichmentService enrichmentService,
                                   WorkbenchCsvService csvService,
                                   WorkbenchTapService tapService,
                                   WorkbenchExoplanetImportService exoplanetImportService) {
        this.eventPublisher = eventPublisher;
        this.starService = starService;
        this.datasetService = datasetService;
        this.enrichmentService = enrichmentService;
        this.csvService = csvService;
        this.tapService = tapService;
        this.exoplanetImportService = exoplanetImportService;
    }

    @FXML
    public void initialize() {
        log.info("Data Workbench: initialize");
        // Hide progress bar initially
        if (enrichmentProgressBar != null) {
            enrichmentProgressBar.setVisible(false);
        }
        updateStatus("Data Workbench ready");
        sourceActions = new WorkbenchSourceActions(
                sources,
                sourceListView,
                sourceStatusLabel,
                workbenchTabs,
                cacheDefaultCheckbox,
                () -> cacheDir,
                tapService,
                this::updateStatus,
                this::showError);
        if (liveTapBatchField != null) {
            liveTapBatchField.setText("50");
        }
        if (liveTapBackoffField != null) {
            liveTapBackoffField.setText("1000");
        }
        if (sourceListView != null) {
            sourceListView.setItems(sources);
            sourceListView.setCellFactory(listView -> new ListCell<>() {
                @Override
                protected void updateItem(WorkbenchSource item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getName() + " (" + item.getType().getLabel() + ")");
                    }
                }
            });
        }
        if (sourceFieldsListView != null) {
            sourceFieldsListView.setItems(sourceFields);
        }
        if (targetFieldsListView != null) {
            targetFieldsListView.setItems(targetFields);
            targetFieldsListView.setCellFactory(listView -> new ListCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else if (WorkbenchCsvSchema.REQUIRED_FIELDS.contains(item)) {
                        setText(item + " *");
                    } else {
                        setText(item);
                    }
                }
            });
        }
        if (mappingTable != null) {
            mappingTable.setItems(mappings);
            mappingTable.setPlaceholder(new Label("No mappings defined."));
            TableColumn<MappingRow, String> sourceColumn = new TableColumn<>("Source Field");
            sourceColumn.setCellValueFactory(new PropertyValueFactory<>("sourceField"));
            sourceColumn.setPrefWidth(220);
            TableColumn<MappingRow, String> targetColumn = new TableColumn<>("Target Field");
            targetColumn.setCellValueFactory(new PropertyValueFactory<>("targetField"));
            targetColumn.setPrefWidth(220);
            mappingTable.getColumns().setAll(sourceColumn, targetColumn);
        }
        if (previewTable != null) {
            previewTable.setPlaceholder(new Label("No data loaded yet."));
        }
        if (validationLog != null) {
            validationLog.setEditable(false);
        }
        if (previewPagination != null) {
            previewPagination.setPageCount(1);
            previewPagination.currentPageIndexProperty().addListener((obs, oldVal, newVal) -> {
                if (previewSourcePath != null) {
                    loadPreviewPage(newVal.intValue());
                }
            });
        }
        initializeCacheDir();
        loadLastMappingIfAvailable();
        initializeExoplanetTab();
    }

    private void initializeExoplanetTab() {
        // Setup preview table columns
        if (exoNameCol != null) {
            exoNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        }
        if (exoStarNameCol != null) {
            exoStarNameCol.setCellValueFactory(new PropertyValueFactory<>("starName"));
        }
        if (exoSmaCol != null) {
            exoSmaCol.setCellValueFactory(new PropertyValueFactory<>("semiMajorAxis"));
        }
        if (exoMassCol != null) {
            exoMassCol.setCellValueFactory(new PropertyValueFactory<>("mass"));
        }
        if (exoRadiusCol != null) {
            exoRadiusCol.setCellValueFactory(new PropertyValueFactory<>("radius"));
        }
        if (exoPeriodCol != null) {
            exoPeriodCol.setCellValueFactory(new PropertyValueFactory<>("orbitalPeriod"));
        }
        if (exoStatusCol != null) {
            exoStatusCol.setCellValueFactory(new PropertyValueFactory<>("planetStatus"));
        }

        if (exoplanetPreviewTable != null) {
            exoplanetPreviewTable.setItems(exoplanetPreviewRows);
            exoplanetPreviewTable.setPlaceholder(new Label("Load an exoplanet.eu CSV file to see data here."));
        }

        // Setup match table columns
        if (matchSelectCol != null) {
            matchSelectCol.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
            matchSelectCol.setCellFactory(CheckBoxTableCell.forTableColumn(matchSelectCol));
        }
        if (matchExoNameCol != null) {
            matchExoNameCol.setCellValueFactory(new PropertyValueFactory<>("exoplanetName"));
        }
        if (matchCsvStarCol != null) {
            matchCsvStarCol.setCellValueFactory(new PropertyValueFactory<>("csvStarName"));
        }
        if (matchMatchedStarCol != null) {
            matchMatchedStarCol.setCellValueFactory(new PropertyValueFactory<>("matchedStarName"));
        }
        if (matchTypeCol != null) {
            matchTypeCol.setCellValueFactory(new PropertyValueFactory<>("matchType"));
        }
        if (matchConfidenceCol != null) {
            matchConfidenceCol.setCellValueFactory(new PropertyValueFactory<>("confidence"));
        }

        if (exoplanetMatchTable != null) {
            exoplanetMatchTable.setItems(exoplanetMatchRows);
            exoplanetMatchTable.setEditable(true);
            exoplanetMatchTable.setPlaceholder(new Label("Run 'Match to Stars' to see matching results."));
        }

        if (exoplanetLogArea != null) {
            exoplanetLogArea.setEditable(false);
        }
    }

    @FXML
    private void onValidate() {
        validationLog.clear();
        if (previewRows.isEmpty()) {
            appendValidationMessage("No preview data loaded.");
            return;
        }
        int rowIndex = 1;
        int errorCount = 0;
        for (Map<String, String> row : previewRows) {
            List<String> messages = csvService.validateRow(row, rowIndex);
            for (String message : messages) {
                appendValidationMessage(message);
                errorCount++;
            }
            rowIndex++;
        }
        String summary = "Validation complete. Errors: " + errorCount;
        appendValidationMessage(summary);
        updateStatus(summary);
    }

    @FXML
    private void onValidateFullFile() {
        WorkbenchSource selected = sourceListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Validate Full File", "Select a CSV source to validate.");
            return;
        }
        if (mappings.isEmpty()) {
            showError("Validate Full File", "Add at least one mapping before validating.");
            return;
        }
        validationLog.clear();
        if (selected.getType() == WorkbenchSourceType.LOCAL_CSV) {
            validateFullFile(Path.of(selected.getLocation()), selected.getName());
            return;
        }
        sourceActions.ensureLocalCsvSource(selected, path -> validateFullFile(path, selected.getName()));
    }

    @FXML
    private void onExportCsv() {
        WorkbenchSource selected = sourceListView.getSelectionModel().getSelectedItem();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export TRIPS CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));
        sourceActions.applyInitialDirectory(fileChooser);
        File file;
        if (cacheDefaultCheckbox != null && cacheDefaultCheckbox.isSelected() && cacheDir != null) {
            String name = selected != null ? selected.getName() : "trips-export.csv";
            file = cacheDir.resolve("export-" + name).toFile();
        } else {
            file = fileChooser.showSaveDialog(workbenchTabs.getScene() != null
                    ? workbenchTabs.getScene().getWindow()
                    : null);
        }
        if (file == null) {
            return;
        }
        try {
            if (selected == null || mappings.isEmpty()) {
                String headerLine = String.join(",", WorkbenchCsvSchema.CSV_HEADER_COLUMNS);
                Files.writeString(file.toPath(), headerLine + System.lineSeparator(), StandardCharsets.UTF_8);
                updateStatus("Template exported: " + file.getName());
                return;
            }
            if (selected.getType() == WorkbenchSourceType.LOCAL_CSV) {
                exportMappedCsvFromSource(Path.of(selected.getLocation()), file.toPath(), file.getName());
                return;
            }
            sourceActions.ensureLocalCsvSource(selected, path -> exportMappedCsvFromSource(path, file.toPath(), file.getName()));
        } catch (IOException e) {
            showError("Export failed", "Unable to write CSV: " + e.getMessage());
        }
    }

    @FXML
    private void onAddSource() {
        log.info("Data Workbench: add source clicked");
        sourceActions.onAddSource();
    }

    @FXML
    private void onRefreshSources() {
        sourceActions.onRefreshSources();
    }

    @FXML
    private void onRemoveSource() {
        sourceActions.onRemoveSource();
    }

    @FXML
    private void onDownloadSource() {
        log.info("Data Workbench: download clicked");
        sourceActions.onDownloadSource();
    }

    @FXML
    private void onConvertHygCsv() {
        log.info("Data Workbench: convert HYG CSV clicked");
        FileChooser inputChooser = new FileChooser();
        inputChooser.setTitle("Select HYG CSV File");
        inputChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));
        sourceActions.applyInitialDirectory(inputChooser);
        File inputFile = inputChooser.showOpenDialog(workbenchTabs.getScene() != null
                ? workbenchTabs.getScene().getWindow()
                : null);
        if (inputFile == null) {
            return;
        }

        String datasetName = buildHygDatasetName();
        FileChooser outputChooser = new FileChooser();
        outputChooser.setTitle("Save TRIPS CSV");
        outputChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));
        sourceActions.applyInitialDirectory(outputChooser);
        outputChooser.setInitialFileName(datasetName + ".csv");
        File outputFile = outputChooser.showSaveDialog(workbenchTabs.getScene() != null
                ? workbenchTabs.getScene().getWindow()
                : null);
        if (outputFile == null) {
            return;
        }

        updateStatus("Converting HYG TSV to TRIPS CSV...");
        showProgress();
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                csvService.convertHygCsvToTripsCsv(inputFile.toPath(), outputFile.toPath(), datasetName,
                        count -> updateStatus("HYG converter: " + count + " rows processed"));
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            hideProgress();
            updateStatus("Converted: " + outputFile.getName());
            sourceActions.addLocalSourceIfMissing(outputFile.toPath());
        });
        task.setOnFailed(event -> {
            hideProgress();
            showError("HYG Converter", String.valueOf(task.getException().getMessage()));
        });
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void onEnrichDistances() {
        FileChooser baseChooser = new FileChooser();
        baseChooser.setTitle("Select TRIPS CSV to enrich");
        baseChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));
        sourceActions.applyInitialDirectory(baseChooser);
        File baseFile = baseChooser.showOpenDialog(workbenchTabs.getScene() != null
                ? workbenchTabs.getScene().getWindow()
                : null);
        if (baseFile == null) {
            return;
        }

        FileChooser gaiaChooser = new FileChooser();
        gaiaChooser.setTitle("Select Gaia DR3 CSV (optional)");
        gaiaChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));
        sourceActions.applyInitialDirectory(gaiaChooser);
        File gaiaFile = gaiaChooser.showOpenDialog(workbenchTabs.getScene() != null
                ? workbenchTabs.getScene().getWindow()
                : null);

        FileChooser hipChooser = new FileChooser();
        hipChooser.setTitle("Select Hipparcos CSV (optional)");
        hipChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));
        sourceActions.applyInitialDirectory(hipChooser);
        File hipFile = hipChooser.showOpenDialog(workbenchTabs.getScene() != null
                ? workbenchTabs.getScene().getWindow()
                : null);

        if (gaiaFile == null && hipFile == null) {
            showError("Enrich Distances", "Select at least a Gaia or Hipparcos CSV file.");
            return;
        }

        FileChooser outputChooser = new FileChooser();
        outputChooser.setTitle("Save enriched TRIPS CSV");
        outputChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));
        sourceActions.applyInitialDirectory(outputChooser);
        outputChooser.setInitialFileName(baseFile.getName().replace(".csv", "") + "-enriched.csv");
        File outputFile = outputChooser.showSaveDialog(workbenchTabs.getScene() != null
                ? workbenchTabs.getScene().getWindow()
                : null);
        if (outputFile == null) {
            return;
        }

        updateStatus("Enriching distances...");
        showProgress();
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                csvService.enrichDistances(baseFile.toPath(),
                        gaiaFile != null ? gaiaFile.toPath() : null,
                        hipFile != null ? hipFile.toPath() : null,
                        outputFile.toPath(),
                        count -> updateStatus("Enriching: " + count + " rows processed"));
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            hideProgress();
            updateStatus("Enriched CSV saved: " + outputFile.getName());
            sourceActions.addLocalSourceIfMissing(outputFile.toPath());
        });
        task.setOnFailed(event -> {
            hideProgress();
            showError("Enrich Distances", String.valueOf(task.getException().getMessage()));
        });
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void onEnrichMissingDistancesLive() {
        int batchSize = 50;
        int backoffMs = 1000;
        if (liveTapBatchField != null && !liveTapBatchField.getText().isBlank()) {
            Integer parsed = parseIntStrict(liveTapBatchField.getText(), "Live TAP batch size");
            if (parsed == null || parsed <= 0) {
                showError("Enrich Distances", "Live TAP batch size must be a positive integer.");
                return;
            }
            batchSize = parsed;
        }
        if (liveTapBackoffField != null && !liveTapBackoffField.getText().isBlank()) {
            Integer parsed = parseIntStrict(liveTapBackoffField.getText(), "Live TAP backoff");
            if (parsed == null || parsed < 0) {
                showError("Enrich Distances", "Live TAP backoff must be 0 or greater.");
                return;
            }
            backoffMs = parsed;
        }
        List<String> datasetNames = datasetService.getDescriptors().stream()
                .map(descriptor -> descriptor.getDataSetName())
                .sorted()
                .collect(Collectors.toList());
        if (datasetNames.isEmpty()) {
            showError("Enrich Distances", "No datasets available.");
            return;
        }
        ChoiceDialog<String> dialog = new ChoiceDialog<>(datasetNames.get(0), datasetNames);
        dialog.setTitle("Enrich Missing Distances");
        dialog.setHeaderText("Select dataset to enrich");
        dialog.setContentText("Dataset:");
        Optional<String> selection = dialog.showAndWait();
        if (selection.isEmpty()) {
            return;
        }

        String dataSetName = selection.get();
        int finalBatchSize = batchSize;
        int finalBackoffMs = backoffMs;
        updateStatus("Enriching missing distances (live TAP)...");
        showProgress();
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                enrichmentService.enrichMissingDistancesLive(dataSetName, finalBatchSize, finalBackoffMs, DataWorkbenchController.this::updateStatus);
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            hideProgress();
            updateStatus("Live TAP enrichment complete.");
        });
        task.setOnFailed(event -> {
            hideProgress();
            showError("Enrich Distances", String.valueOf(task.getException().getMessage()));
        });
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void onPhotometricEstimation() {
        List<String> datasetNames = datasetService.getDescriptors().stream()
                .map(descriptor -> descriptor.getDataSetName())
                .sorted()
                .collect(Collectors.toList());
        if (datasetNames.isEmpty()) {
            showError("Photometric Estimation", "No datasets available.");
            return;
        }
        ChoiceDialog<String> dialog = new ChoiceDialog<>(datasetNames.get(0), datasetNames);
        dialog.setTitle("Photometric Distance Estimation");
        dialog.setHeaderText("Estimate distances for orphan stars using magnitude/color.\nThis runs AFTER TAP enrichment for stars that couldn't be matched.");
        dialog.setContentText("Dataset:");
        Optional<String> selection = dialog.showAndWait();
        if (selection.isEmpty()) {
            return;
        }

        String dataSetName = selection.get();
        updateStatus("Estimating distances photometrically...");
        showProgress();
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                enrichmentService.enrichOrphanDistancesPhotometric(dataSetName, DataWorkbenchController.this::updateStatus);
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            hideProgress();
            updateStatus("Photometric estimation complete.");
        });
        task.setOnFailed(event -> {
            hideProgress();
            showError("Photometric Estimation", String.valueOf(task.getException().getMessage()));
        });
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void onEnrichMasses() {
        int batchSize = 50;
        int backoffMs = 1000;
        if (liveTapBatchField != null && !liveTapBatchField.getText().isBlank()) {
            Integer parsed = parseIntStrict(liveTapBatchField.getText(), "Live TAP batch size");
            if (parsed == null || parsed <= 0) {
                showError("Mass Enrichment", "Batch size must be a positive integer.");
                return;
            }
            batchSize = parsed;
        }
        if (liveTapBackoffField != null && !liveTapBackoffField.getText().isBlank()) {
            Integer parsed = parseIntStrict(liveTapBackoffField.getText(), "Live TAP backoff");
            if (parsed == null || parsed < 0) {
                showError("Mass Enrichment", "Backoff must be 0 or greater.");
                return;
            }
            backoffMs = parsed;
        }

        List<String> datasetNames = datasetService.getDescriptors().stream()
                .map(descriptor -> descriptor.getDataSetName())
                .sorted()
                .collect(Collectors.toList());
        if (datasetNames.isEmpty()) {
            showError("Mass Enrichment", "No datasets available.");
            return;
        }
        ChoiceDialog<String> dialog = new ChoiceDialog<>(datasetNames.get(0), datasetNames);
        dialog.setTitle("Enrich Stellar Parameters from Gaia DR3");
        dialog.setHeaderText("Look up stellar parameters from Gaia DR3 astrophysical_parameters.\nFetches: mass, radius, luminosity, temperature, metallicity\nOnly fills in values that are currently missing.");
        dialog.setContentText("Dataset:");
        Optional<String> selection = dialog.showAndWait();
        if (selection.isEmpty()) {
            return;
        }

        String dataSetName = selection.get();
        int finalBatchSize = batchSize;
        int finalBackoffMs = backoffMs;
        updateStatus("Enriching stellar parameters from Gaia DR3...");
        showProgress();
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                enrichmentService.enrichMissingMassesFromGaia(dataSetName, finalBatchSize, finalBackoffMs,
                        DataWorkbenchController.this::updateStatus);
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            hideProgress();
            updateStatus("Stellar parameters enrichment complete.");
        });
        task.setOnFailed(event -> {
            hideProgress();
            showError("Stellar Parameters Enrichment", String.valueOf(task.getException().getMessage()));
        });
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void onEstimateMassPhotometric() {
        List<String> datasetNames = datasetService.getDescriptors().stream()
                .map(descriptor -> descriptor.getDataSetName())
                .sorted()
                .collect(Collectors.toList());
        if (datasetNames.isEmpty()) {
            showError("Mass Estimation", "No datasets available.");
            return;
        }
        ChoiceDialog<String> dialog = new ChoiceDialog<>(datasetNames.get(0), datasetNames);
        dialog.setTitle("Estimate Mass (Photometric)");
        dialog.setHeaderText("Estimate stellar mass from luminosity using the mass-luminosity relation.\nRequires distance and apparent magnitude data.\nAlso estimates radius and luminosity.");
        dialog.setContentText("Dataset:");
        Optional<String> selection = dialog.showAndWait();
        if (selection.isEmpty()) {
            return;
        }

        String dataSetName = selection.get();
        updateStatus("Estimating masses photometrically...");
        showProgress();
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                enrichmentService.enrichMassPhotometric(dataSetName, DataWorkbenchController.this::updateStatus);
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            hideProgress();
            updateStatus("Photometric mass estimation complete.");
        });
        task.setOnFailed(event -> {
            hideProgress();
            showError("Mass Estimation", String.valueOf(task.getException().getMessage()));
        });
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void onCancelTap() {
        tapService.cancelCurrentJob(this::updateStatus, message -> showError("Cancel TAP", message));
    }

    @FXML
    private void onLoadSourceFields() {
        WorkbenchSource selected = sourceListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Load Fields", "Select a source to inspect.");
            return;
        }
        if (selected.getType() == WorkbenchSourceType.LOCAL_CSV) {
            loadSourceFieldsFromPath(Path.of(selected.getLocation()));
            return;
        }
        sourceActions.ensureLocalCsvSource(selected, this::loadSourceFieldsFromPath);
    }

    @FXML
    private void onAddMapping() {
        String sourceField = sourceFieldsListView.getSelectionModel().getSelectedItem();
        String targetField = targetFieldsListView.getSelectionModel().getSelectedItem();
        if (sourceField == null || targetField == null) {
            showError("Add Mapping", "Select a source field and a target field.");
            return;
        }
        mappings.add(new MappingRow(sourceField, targetField));
        mappingStatusLabel.setText("Mappings: " + mappings.size());
        saveLastMapping();
    }

    @FXML
    private void onRemoveMapping() {
        MappingRow selected = mappingTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            mappings.remove(selected);
            mappingStatusLabel.setText("Mappings: " + mappings.size());
            saveLastMapping();
        }
    }

    @FXML
    private void onSaveMapping() {
        if (mappings.isEmpty()) {
            showError("Save Mapping", "No mappings to save.");
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Mapping");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Mapping files (*.map.csv)", "*.map.csv"));
        sourceActions.applyInitialDirectory(fileChooser);
        File file = fileChooser.showSaveDialog(workbenchTabs.getScene() != null
                ? workbenchTabs.getScene().getWindow()
                : null);
        if (file == null) {
            return;
        }
        try {
            writeMappingToPath(file.toPath());
            mappingStatusLabel.setText("Mapping saved: " + file.getName());
            saveLastMapping();
        } catch (IOException e) {
            showError("Save Mapping", "Unable to save mapping: " + e.getMessage());
        }
    }

    @FXML
    private void onLoadMapping() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Mapping");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Mapping files (*.map.csv)", "*.map.csv"));
        sourceActions.applyInitialDirectory(fileChooser);
        File file = fileChooser.showOpenDialog(workbenchTabs.getScene() != null
                ? workbenchTabs.getScene().getWindow()
                : null);
        if (file == null) {
            return;
        }
        try {
            loadMappingFromPath(file.toPath());
            mappingStatusLabel.setText("Mappings: " + mappings.size());
            saveLastMapping();
        } catch (IOException e) {
            showError("Load Mapping", "Unable to load mapping: " + e.getMessage());
        }
    }

    @FXML
    private void onAutoMap() {
        if (sourceFields.isEmpty()) {
            showError("Auto Map", "Load source fields before auto-mapping.");
            return;
        }
        Map<String, String> sourceByLower = new HashMap<>();
        Map<String, String> sourceByNormalized = new HashMap<>();
        for (String field : sourceFields) {
            String trimmed = field.trim();
            sourceByLower.putIfAbsent(trimmed.toLowerCase(), trimmed);
            sourceByNormalized.putIfAbsent(normalizeFieldName(trimmed), trimmed);
        }
        Set<String> mappedTargets = mappings.stream()
                .map(MappingRow::getTargetField)
                .collect(Collectors.toCollection(HashSet::new));
        int added = 0;
        for (String target : targetFields) {
            if (mappedTargets.contains(target)) {
                continue;
            }
            String lowerMatch = sourceByLower.get(target.toLowerCase());
            String normalizedMatch = sourceByNormalized.get(normalizeFieldName(target));
            String match = lowerMatch != null ? lowerMatch : normalizedMatch;
            if (match != null) {
                mappings.add(new MappingRow(match, target));
                mappedTargets.add(target);
                added++;
            }
        }
        List<MappingRow> gaiaMappings = WorkbenchMappingDefaults.defaultGaiaMappings(sourceFields);
        int gaiaAdded = 0;
        for (MappingRow mapping : gaiaMappings) {
            if (mappedTargets.contains(mapping.getTargetField())) {
                continue;
            }
            mappings.add(mapping);
            mappedTargets.add(mapping.getTargetField());
            gaiaAdded++;
        }
        List<MappingRow> simbadMappings = WorkbenchMappingDefaults.defaultSimbadMappings(sourceFields);
        int simbadAdded = 0;
        for (MappingRow mapping : simbadMappings) {
            if (mappedTargets.contains(mapping.getTargetField())) {
                continue;
            }
            mappings.add(mapping);
            mappedTargets.add(mapping.getTargetField());
            simbadAdded++;
        }
        List<MappingRow> vizierMappings = WorkbenchMappingDefaults.defaultVizierMappings(sourceFields);
        int vizierAdded = 0;
        for (MappingRow mapping : vizierMappings) {
            if (mappedTargets.contains(mapping.getTargetField())) {
                continue;
            }
            mappings.add(mapping);
            mappedTargets.add(mapping.getTargetField());
            vizierAdded++;
        }
        mappingStatusLabel.setText("Mappings: " + mappings.size()
                + " (auto-added " + (added + gaiaAdded + simbadAdded + vizierAdded) + ")");
        saveLastMapping();
    }

    @FXML
    private void onChooseCacheDir() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Cache Directory");
        if (cacheDir != null && Files.exists(cacheDir)) {
            chooser.setInitialDirectory(cacheDir.toFile());
        }
        File folder = chooser.showDialog(workbenchTabs.getScene() != null
                ? workbenchTabs.getScene().getWindow()
                : null);
        if (folder == null) {
            return;
        }
        cacheDir = folder.toPath();
        if (cacheDirField != null) {
            cacheDirField.setText(cacheDir.toString());
        }
        ensureCacheDir();
    }

    @FXML
    private void onLoadPreview() {
        WorkbenchSource selected = sourceListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Preview", "Select a CSV source to preview.");
            return;
        }
        if (mappings.isEmpty()) {
            showError("Preview", "Add at least one mapping before preview.");
            return;
        }
        if (selected.getType() == WorkbenchSourceType.LOCAL_CSV) {
            loadPreviewFromPath(Path.of(selected.getLocation()));
            return;
        }
        sourceActions.ensureLocalCsvSource(selected, this::loadPreviewFromPath);
    }

    private String buildHygDatasetName() {
        return "HYG-MERGED-2M-TRIPS-" + LocalDateTime.now().format(HYG_TIMESTAMP_FORMAT);
    }

    private void convertHygCsvToTripsCsv(Path inputPath,
                                         Path outputPath,
                                         String datasetName,
                                         Consumer<Long> progressConsumer) throws IOException {
        csvService.convertHygCsvToTripsCsv(inputPath, outputPath, datasetName, progressConsumer);
    }

    private void appendValidationMessage(String message) {
        if (validationLog != null) {
            validationLog.appendText(message + System.lineSeparator());
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle(title);
        alert.showAndWait();
    }

    private void rebuildPreviewTable() {
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
            column.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getOrDefault(columnName, "")));
            column.setPrefWidth(140);
            previewTable.getColumns().add(column);
        }
        previewTable.setItems(previewRows);
    }

    private void updateStatus(String message) {
        Runnable updateUi = () -> {
            if (exportStatusLabel != null) {
                exportStatusLabel.setText(message);
            }
            if (sourceStatusLabel != null) {
                sourceStatusLabel.setText(message);
            }
        };
        if (Platform.isFxApplicationThread()) {
            updateUi.run();
        } else {
            Platform.runLater(updateUi);
        }
        if (eventPublisher != null) {
            eventPublisher.publishEvent(new StatusUpdateEvent(this, message));
        }
        log.info("Status: {}", message);
    }

    private void showProgress() {
        Runnable updateUi = () -> {
            if (enrichmentProgressBar != null) {
                enrichmentProgressBar.setVisible(true);
                enrichmentProgressBar.setProgress(-1); // Indeterminate
            }
        };
        if (Platform.isFxApplicationThread()) {
            updateUi.run();
        } else {
            Platform.runLater(updateUi);
        }
    }

    private void hideProgress() {
        Runnable updateUi = () -> {
            if (enrichmentProgressBar != null) {
                enrichmentProgressBar.setVisible(false);
            }
        };
        if (Platform.isFxApplicationThread()) {
            updateUi.run();
        } else {
            Platform.runLater(updateUi);
        }
    }

    private void loadSourceFieldsFromPath(Path path) {
        if (!Files.exists(path)) {
            showError("Load Fields", "File not found: " + path);
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String header = Optional.ofNullable(reader.readLine()).orElse("");
            sourceFields.setAll(Arrays.asList(csvService.splitCsvLine(header)));
            mappingStatusLabel.setText("Loaded " + sourceFields.size() + " source fields.");
        } catch (IOException e) {
            showError("Load Fields", "Unable to read source header: " + e.getMessage());
        }
    }

    private Integer parseIntStrict(String value, String label) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            showError("Enrich Distances", label + " must be a valid integer.");
            return null;
        }
    }

    private void loadPreviewFromPath(Path path) {
        previewSourcePath = path;
        try (BufferedReader reader = Files.newBufferedReader(previewSourcePath, StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (header == null) {
                showError("Preview", "Source file is empty.");
                return;
            }
            previewHeaderIndex = csvService.buildHeaderIndex(header);
            previewTargetToSource = csvService.buildTargetToSourceMap(mappings);
            previewTotalRows = countRows(previewSourcePath);
            previewRows.clear();
            updatePagination();
            loadPreviewPage(0);
            rebuildPreviewTable();
            appendValidationMessage("Loaded " + previewRows.size() + " preview rows.");
            updateStatus("Preview page 1 / " + previewPagination.getPageCount());
        } catch (IOException e) {
            showError("Preview", "Unable to read source CSV: " + e.getMessage());
        }
    }

    private void exportMappedCsvFromSource(Path sourcePath, Path outputPath, String outputName) {
        try {
            WorkbenchCsvService.ExportResult result = csvService.exportMappedCsv(
                    sourcePath, outputPath, WorkbenchCsvSchema.CSV_HEADER_COLUMNS, mappings);
            updateStatus("Exported " + result.getRowCount() + " rows to " + outputName);
            writeValidationLog(outputPath, result.getValidationMessages());
        } catch (IOException e) {
            showError("Export failed", "Unable to write CSV: " + e.getMessage());
        }
    }

    private void validateFullFile(Path sourcePath, String sourceName) {
        try {
            WorkbenchCsvService.ExportResult result = csvService.validateMappedCsv(sourcePath, mappings);
            for (String message : result.getValidationMessages()) {
                appendValidationMessage(message);
            }
            updateStatus("Validated " + result.getRowCount() + " rows from " + sourceName);
        } catch (IOException e) {
            showError("Validate Full File", "Unable to validate CSV: " + e.getMessage());
        }
    }

    private void writeValidationLog(Path outputPath, List<String> messages) throws IOException {
        Path logPath = Path.of(outputPath.toString() + ".log");
        Files.writeString(logPath, messages.stream().collect(Collectors.joining(System.lineSeparator()))
                + System.lineSeparator(), StandardCharsets.UTF_8);
        appendValidationMessage("Validation summary written: " + logPath.getFileName());
    }

    private void initializeCacheDir() {
        cacheDir = Path.of(System.getProperty("user.home", "."), "trips-workbench-cache");
        if (cacheDirField != null) {
            cacheDirField.setText(cacheDir.toString());
        }
        if (cacheDefaultCheckbox != null) {
            cacheDefaultCheckbox.setSelected(true);
        }
        ensureCacheDir();
    }

    private void ensureCacheDir() {
        try {
            Files.createDirectories(cacheDir);
        } catch (IOException e) {
            showError("Cache Directory", "Unable to create cache directory: " + e.getMessage());
        }
    }

    private int countRows(Path path) throws IOException {
        int count = 0;
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            while (reader.readLine() != null) {
                count++;
            }
        }
        return Math.max(0, count - 1);
    }

    private void updatePagination() {
        if (previewPagination == null) {
            return;
        }
        int pageCount = Math.max(1, (int) Math.ceil((double) previewTotalRows / previewPageSize));
        previewPagination.setPageCount(pageCount);
        previewPagination.setCurrentPageIndex(0);
    }

    private void loadPreviewPage(int pageIndex) {
        if (previewSourcePath == null) {
            return;
        }
        previewRows.clear();
        int startIndex = pageIndex * previewPageSize;
        int endIndex = startIndex + previewPageSize;
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
            showError("Preview", "Unable to load preview page: " + e.getMessage());
        }
        rebuildPreviewTable();
        if (previewPagination != null) {
            updateStatus("Preview page " + (pageIndex + 1) + " / " + previewPagination.getPageCount());
        }
    }

    private void loadLastMappingIfAvailable() {
        if (cacheDir == null) {
            return;
        }
        Path mappingPath = cacheDir.resolve("last-mapping.map.csv");
        if (!Files.exists(mappingPath)) {
            return;
        }
        try {
            loadMappingFromPath(mappingPath);
            mappingStatusLabel.setText("Mappings: " + mappings.size() + " (last used)");
        } catch (IOException e) {
            showError("Load Mapping", "Unable to load cached mapping: " + e.getMessage());
        }
    }

    private void saveLastMapping() {
        if (cacheDir == null) {
            return;
        }
        Path mappingPath = cacheDir.resolve("last-mapping.map.csv");
        if (mappings.isEmpty()) {
            try {
                Files.deleteIfExists(mappingPath);
            } catch (IOException e) {
                showError("Save Mapping", "Unable to clear cached mapping: " + e.getMessage());
            }
            return;
        }
        try {
            writeMappingToPath(mappingPath);
        } catch (IOException e) {
            showError("Save Mapping", "Unable to save cached mapping: " + e.getMessage());
        }
    }

    private void loadMappingFromPath(Path path) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (header == null) {
                throw new IOException("Mapping file is empty.");
            }
            mappings.clear();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = csvService.splitCsvLine(line);
                if (values.length >= 2) {
                    mappings.add(new MappingRow(csvService.unquote(values[0]), csvService.unquote(values[1])));
                }
            }
        }
    }

    private void writeMappingToPath(Path path) throws IOException {
        StringBuilder output = new StringBuilder();
        output.append("sourceField,targetField").append(System.lineSeparator());
        for (MappingRow mapping : mappings) {
            output.append(csvService.escapeCsv(mapping.getSourceField()))
                    .append(",")
                    .append(csvService.escapeCsv(mapping.getTargetField()))
                    .append(System.lineSeparator());
        }
        Files.writeString(path, output.toString(), StandardCharsets.UTF_8);
    }

    private String normalizeFieldName(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    // ==================== Exoplanet Tab Handlers ====================

    @FXML
    private void onLoadExoplanetCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Exoplanet Catalog CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));
        sourceActions.applyInitialDirectory(fileChooser);

        File file = fileChooser.showOpenDialog(workbenchTabs.getScene() != null
                ? workbenchTabs.getScene().getWindow()
                : null);

        if (file == null) {
            return;
        }

        updateExoplanetStatus("Loading " + file.getName() + "...");
        if (exoplanetProgressBar != null) {
            exoplanetProgressBar.setVisible(true);
        }

        Task<List<ExoplanetCsvRow>> task = new Task<>() {
            @Override
            protected List<ExoplanetCsvRow> call() throws Exception {
                return exoplanetImportService.parseCsvFile(file.toPath(),
                        DataWorkbenchController.this::updateExoplanetStatus);
            }
        };

        task.setOnSucceeded(event -> {
            parsedExoplanets = task.getValue();
            exoplanetPreviewRows.setAll(exoplanetImportService.toPreviewRows(parsedExoplanets));
            updateExoplanetFileStatus("Loaded " + parsedExoplanets.size() + " exoplanets from " + file.getName());
            if (exoplanetProgressBar != null) {
                exoplanetProgressBar.setVisible(false);
            }
            appendExoplanetLog("Loaded " + parsedExoplanets.size() + " exoplanets from " + file.getName());

            // Clear previous match results
            exoplanetMatchRows.clear();
            exoplanetMatchResult = null;
            updateExoplanetMatchStats("");
        });

        task.setOnFailed(event -> {
            showError("Load Exoplanets", String.valueOf(task.getException().getMessage()));
            if (exoplanetProgressBar != null) {
                exoplanetProgressBar.setVisible(false);
            }
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void onMatchExoplanets() {
        if (parsedExoplanets == null || parsedExoplanets.isEmpty()) {
            showError("Match Exoplanets", "Load an exoplanet CSV file first.");
            return;
        }

        // Get available datasets and let user choose
        List<String> datasetNames = datasetService.getDescriptors().stream()
                .map(descriptor -> descriptor.getDataSetName())
                .sorted()
                .collect(Collectors.toList());

        if (datasetNames.isEmpty()) {
            showError("Match Exoplanets", "No datasets available. Load a star dataset first.");
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
        updateExoplanetStatus("Matching exoplanets to stars in " + dataSetName + "...");
        if (exoplanetProgressBar != null) {
            exoplanetProgressBar.setVisible(true);
        }

        Task<ExoplanetMatchResult> task = new Task<>() {
            @Override
            protected ExoplanetMatchResult call() throws Exception {
                return exoplanetImportService.matchExoplanetsToStars(
                        parsedExoplanets,
                        dataSetName,
                        DataWorkbenchController.this::updateExoplanetStatus);
            }
        };

        task.setOnSucceeded(event -> {
            exoplanetMatchResult = task.getValue();
            exoplanetMatchRows.setAll(exoplanetImportService.toMatchRows(exoplanetMatchResult));

            String stats = String.format("Matched: %d exact, %d fuzzy, %d RA/Dec, %d unmatched",
                    exoplanetMatchResult.getExactMatches(),
                    exoplanetMatchResult.getFuzzyMatches(),
                    exoplanetMatchResult.getRaDecMatches(),
                    exoplanetMatchResult.getUnmatched());
            updateExoplanetMatchStats(stats);
            appendExoplanetLog("Matching complete: " + stats);

            if (exoplanetProgressBar != null) {
                exoplanetProgressBar.setVisible(false);
            }
        });

        task.setOnFailed(event -> {
            showError("Match Exoplanets", String.valueOf(task.getException().getMessage()));
            if (exoplanetProgressBar != null) {
                exoplanetProgressBar.setVisible(false);
            }
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void onImportExoplanets() {
        if (exoplanetMatchResult == null || exoplanetMatchResult.getMatches().isEmpty()) {
            showError("Import Exoplanets", "Run 'Match to Stars' first.");
            return;
        }

        // Filter to only selected rows that have matches
        List<ExoplanetMatch> selectedMatches = new ArrayList<>();
        for (int i = 0; i < exoplanetMatchRows.size(); i++) {
            ExoplanetMatchRow row = exoplanetMatchRows.get(i);
            if (row.isSelected() && row.hasMatch()) {
                selectedMatches.add(exoplanetMatchResult.getMatches().get(i));
            }
        }

        if (selectedMatches.isEmpty()) {
            showError("Import Exoplanets", "No matched exoplanets selected for import.");
            return;
        }

        boolean skipDuplicates = skipDuplicatesCheckbox != null && skipDuplicatesCheckbox.isSelected();

        updateExoplanetStatus("Importing " + selectedMatches.size() + " exoplanets...");
        if (exoplanetProgressBar != null) {
            exoplanetProgressBar.setVisible(true);
        }

        Task<ExoplanetImportResult> task = new Task<>() {
            @Override
            protected ExoplanetImportResult call() throws Exception {
                return exoplanetImportService.importMatchedExoplanets(
                        selectedMatches,
                        skipDuplicates,
                        DataWorkbenchController.this::updateExoplanetStatus);
            }
        };

        task.setOnSucceeded(event -> {
            ExoplanetImportResult result = task.getValue();
            String summary = String.format("Import complete: %d imported, %d skipped, %d solar systems created",
                    result.getImported(), result.getSkipped(), result.getSolarSystemsCreated());
            updateExoplanetStatus(summary);
            appendExoplanetLog(summary);

            if (!result.getErrors().isEmpty()) {
                appendExoplanetLog("Errors (" + result.getErrors().size() + "):");
                for (String error : result.getErrors().subList(0, Math.min(10, result.getErrors().size()))) {
                    appendExoplanetLog("  " + error);
                }
                if (result.getErrors().size() > 10) {
                    appendExoplanetLog("  ... and " + (result.getErrors().size() - 10) + " more errors");
                }
            }

            if (exoplanetProgressBar != null) {
                exoplanetProgressBar.setVisible(false);
            }
        });

        task.setOnFailed(event -> {
            showError("Import Exoplanets", String.valueOf(task.getException().getMessage()));
            if (exoplanetProgressBar != null) {
                exoplanetProgressBar.setVisible(false);
            }
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void onClearExoplanetData() {
        parsedExoplanets.clear();
        exoplanetPreviewRows.clear();
        exoplanetMatchRows.clear();
        exoplanetMatchResult = null;
        updateExoplanetFileStatus("No file loaded");
        updateExoplanetMatchStats("");
        if (exoplanetLogArea != null) {
            exoplanetLogArea.clear();
        }
        updateExoplanetStatus("Cleared exoplanet data");
    }

    private void updateExoplanetStatus(String message) {
        Runnable updateUi = () -> {
            if (exoplanetFileStatusLabel != null) {
                // Don't update file status label with general status
            }
            appendExoplanetLog(message);
        };
        if (Platform.isFxApplicationThread()) {
            updateUi.run();
        } else {
            Platform.runLater(updateUi);
        }
    }

    private void updateExoplanetFileStatus(String message) {
        Runnable updateUi = () -> {
            if (exoplanetFileStatusLabel != null) {
                exoplanetFileStatusLabel.setText(message);
            }
        };
        if (Platform.isFxApplicationThread()) {
            updateUi.run();
        } else {
            Platform.runLater(updateUi);
        }
    }

    private void updateExoplanetMatchStats(String message) {
        Runnable updateUi = () -> {
            if (exoplanetMatchStatsLabel != null) {
                exoplanetMatchStatsLabel.setText(message);
            }
        };
        if (Platform.isFxApplicationThread()) {
            updateUi.run();
        } else {
            Platform.runLater(updateUi);
        }
    }

    private void appendExoplanetLog(String message) {
        Runnable updateUi = () -> {
            if (exoplanetLogArea != null) {
                exoplanetLogArea.appendText(message + System.lineSeparator());
            }
        };
        if (Platform.isFxApplicationThread()) {
            updateUi.run();
        } else {
            Platform.runLater(updateUi);
        }
    }
}
