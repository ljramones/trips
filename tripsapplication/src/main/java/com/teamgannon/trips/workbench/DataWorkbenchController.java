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
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Dialog;
import javafx.application.Platform;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import com.teamgannon.trips.events.StatusUpdateEvent;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.service.DatasetService;
import com.teamgannon.trips.service.StarService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@Slf4j
public class DataWorkbenchController {

    private static final List<String> CSV_HEADER_COLUMNS = Arrays.asList(
            "id",
            "dataSetName",
            "displayName",
            "commonName",
            "System Name",
            "Epoch",
            "constellationName",
            "mass",
            "notes",
            "source",
            "catalogIdList",
            "simbadId",
            "Gaia DR2",
            "radius",
            "ra",
            "declination",
            "pmra",
            "pmdec",
            "distance",
            "radialVelocity",
            "spectralClass",
            "temperature",
            "realStar",
            "bprp",
            "bpg",
            "grp",
            "luminosity",
            "magu",
            "magb",
            "magv",
            "magr",
            "magi",
            "other",
            "anomaly",
            "polity",
            "worldType",
            "fuelType",
            "portType",
            "populationType",
            "techType",
            "productType",
            "milSpaceType",
            "milPlanType",
            "age",
            "metallicity",
            "miscText1",
            "miscText2",
            "miscText3",
            "miscText4",
            "miscText5",
            "miscNum1",
            "miscNum2",
            "miscNum3",
            "miscNum4",
            "miscNum5",
            "numExoplanets",
            "absmag",
            "Gaia DR3",
            "x",
            "y",
            "z"
    );

    private static final String HYG_SOURCE_NAME = "HYG-MERGED-2m";
    private static final DateTimeFormatter HYG_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("ddMMyyyyHHmmss");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("(\\d+)");

    private static final List<String> REQUIRED_FIELDS = List.of(
            "displayName",
            "spectralClass",
            "ra",
            "declination",
            "distance",
            "realStar"
    );

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

    private final ApplicationEventPublisher eventPublisher;
    private final StarService starService;
    private final DatasetService datasetService;

    private final ObservableList<WorkbenchSource> sources = FXCollections.observableArrayList();
    private final ObservableList<String> sourceFields = FXCollections.observableArrayList();
    private final ObservableList<String> targetFields = FXCollections.observableArrayList(CSV_HEADER_COLUMNS);
    private final ObservableList<MappingRow> mappings = FXCollections.observableArrayList();
    private final ObservableList<Map<String, String>> previewRows = FXCollections.observableArrayList();
    private Path cacheDir;
    private Path previewSourcePath;
    private Map<String, Integer> previewHeaderIndex = new HashMap<>();
    private Map<String, String> previewTargetToSource = new HashMap<>();
    private int previewTotalRows = 0;
    private final int previewPageSize = 100;
    private static final String GAIA_TAP_BASE_URL = "https://gea.esac.esa.int/tap-server/tap";
    private static final String SIMBAD_TAP_BASE_URL = "https://simbad.cds.unistra.fr/simbad/sim-tap";
    private static final String VIZIER_TAP_BASE_URL = "https://tapvizier.cds.unistra.fr/TAPVizieR/tap";
    private static final long GAIA_TAP_MAX_WAIT_MS = 20 * 60 * 1000;
    private static final long GAIA_TAP_MAX_DELAY_MS = 60 * 1000;
    private static final long GAIA_TAP_INITIAL_DELAY_MS = 1000;
    private volatile boolean tapCancelRequested = false;
    private volatile String tapJobUrl;
    private volatile long tapStartMillis = 0L;
    private volatile String tapLabel = "TAP";

    public DataWorkbenchController(ApplicationEventPublisher eventPublisher,
                                   StarService starService,
                                   DatasetService datasetService) {
        this.eventPublisher = eventPublisher;
        this.starService = starService;
        this.datasetService = datasetService;
    }

    @FXML
    public void initialize() {
        log.info("Data Workbench: initialize");
        updateStatus("Data Workbench ready");
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
                    } else if (REQUIRED_FIELDS.contains(item)) {
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
            List<String> messages = validateRow(row, rowIndex);
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
        ensureLocalCsvSource(selected, path -> validateFullFile(path, selected.getName()));
    }

    @FXML
    private void onExportCsv() {
        WorkbenchSource selected = sourceListView.getSelectionModel().getSelectedItem();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export TRIPS CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));
        applyInitialDirectory(fileChooser);
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
                String headerLine = String.join(",", CSV_HEADER_COLUMNS);
                Files.writeString(file.toPath(), headerLine + System.lineSeparator(), StandardCharsets.UTF_8);
                updateStatus("Template exported: " + file.getName());
                return;
            }
            if (selected.getType() == WorkbenchSourceType.LOCAL_CSV) {
                exportMappedCsvFromSource(Path.of(selected.getLocation()), file.toPath(), file.getName());
                return;
            }
            ensureLocalCsvSource(selected, path -> exportMappedCsvFromSource(path, file.toPath(), file.getName()));
        } catch (IOException e) {
            showError("Export failed", "Unable to write CSV: " + e.getMessage());
        }
    }

    @FXML
    private void onAddSource() {
        log.info("Data Workbench: add source clicked");
        List<String> choices = List.of(
                WorkbenchSourceType.LOCAL_CSV.getLabel(),
                WorkbenchSourceType.URL_CSV.getLabel(),
                WorkbenchSourceType.GAIA_TAP.getLabel(),
                "Gaia TAP (TOP 200)",
                "Gaia TAP (TOP 1000)",
                "Gaia TAP (TOP 5000)",
                WorkbenchSourceType.SIMBAD_TAP.getLabel(),
                "SIMBAD TAP (TOP 200)",
                "SIMBAD TAP (TOP 1000)",
                WorkbenchSourceType.VIZIER_TAP.getLabel(),
                "VizieR TAP (Hipparcos)",
                "VizieR TAP (Tycho-2)",
                "VizieR TAP (RAVE DR5)",
                "VizieR TAP (LAMOST DR5)",
                "VizieR TAP (Gaia RUWE subset)",
                "VizieR TAP (Gliese/CNS)",
                "VizieR TAP (RECONS)",
                "VizieR TAP (Lookup tables)"
        );
        ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(0), choices);
        dialog.setTitle("Add Source");
        dialog.setHeaderText("Select a source type");
        dialog.setContentText("Source type:");
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }
        String selection = result.get();
        if ("Gaia TAP (TOP 200)".equals(selection)) {
            addGaiaTapSource(defaultGaiaQuery(200), "Gaia_DR3_TOP_200.csv");
            return;
        }
        if ("Gaia TAP (TOP 1000)".equals(selection)) {
            addGaiaTapSource(defaultGaiaQuery(1000), "Gaia_DR3_TOP_1000.csv");
            return;
        }
        if ("Gaia TAP (TOP 5000)".equals(selection)) {
            addGaiaTapSource(defaultGaiaQuery(5000), "Gaia_DR3_TOP_5000.csv");
            return;
        }
        if ("SIMBAD TAP (TOP 200)".equals(selection)) {
            addSimbadTapSource(defaultSimbadQuery(200), "SIMBAD_TOP_200.csv");
            return;
        }
        if ("SIMBAD TAP (TOP 1000)".equals(selection)) {
            addSimbadTapSource(defaultSimbadQuery(1000), "SIMBAD_TOP_1000.csv");
            return;
        }
        if ("VizieR TAP (Hipparcos)".equals(selection)) {
            addVizierTapSource(defaultVizierHipparcosQuery(1000), "VIZIER_HIP2_TOP_1000.csv");
            return;
        }
        if ("VizieR TAP (Tycho-2)".equals(selection)) {
            addVizierTapSource(defaultVizierTycho2Query(1000), "VIZIER_TYC2_TOP_1000.csv");
            return;
        }
        if ("VizieR TAP (RAVE DR5)".equals(selection)) {
            addVizierTapSource(defaultVizierRaveQuery(1000), "VIZIER_RAVE_DR5_TOP_1000.csv");
            return;
        }
        if ("VizieR TAP (LAMOST DR5)".equals(selection)) {
            addVizierTapSource(defaultVizierLamostQuery(1000), "VIZIER_LAMOST_DR5_TOP_1000.csv");
            return;
        }
        if ("VizieR TAP (Gaia RUWE subset)".equals(selection)) {
            addVizierPresetWithPrompt("Gaia RUWE subset", "VIZIER_GAIA_RUWE_TOP_1000.csv");
            return;
        }
        if ("VizieR TAP (Gliese/CNS)".equals(selection)) {
            addVizierPresetWithPrompt("Gliese/CNS", "VIZIER_GLIESE_CNS_TOP_1000.csv");
            return;
        }
        if ("VizieR TAP (RECONS)".equals(selection)) {
            addVizierTapSource(defaultVizierReconsQuery(1000), "VIZIER_RECONS_TOP_1000.csv");
            return;
        }
        if ("VizieR TAP (Lookup tables)".equals(selection)) {
            addVizierTableLookupSource();
            return;
        }
        WorkbenchSourceType type = WorkbenchSourceType.fromLabel(selection);
        if (type == WorkbenchSourceType.LOCAL_CSV) {
            addLocalCsvSource();
        } else if (type == WorkbenchSourceType.URL_CSV) {
            addUrlCsvSource();
        } else if (type == WorkbenchSourceType.GAIA_TAP) {
            addGaiaTapSource();
        } else if (type == WorkbenchSourceType.SIMBAD_TAP) {
            addSimbadTapSource();
        } else if (type == WorkbenchSourceType.VIZIER_TAP) {
            addVizierTapSource();
        }
    }

    @FXML
    private void onRefreshSources() {
        sourceStatusLabel.setText("Sources: " + sources.size());
    }

    @FXML
    private void onRemoveSource() {
        WorkbenchSource selected = sourceListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            sources.remove(selected);
            sourceStatusLabel.setText("Removed source: " + selected.getName());
        }
    }

    @FXML
    private void onDownloadSource() {
        log.info("Data Workbench: download clicked");
        updateStatus("Download clicked.");
        WorkbenchSource selected = sourceListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Download", "Select a source to download.");
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save downloaded CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));
        applyInitialDirectory(fileChooser);
        String fileName = ensureCsvFileName(selected.getName());
        fileChooser.setInitialFileName(fileName);
        File file;
        if (cacheDefaultCheckbox != null && cacheDefaultCheckbox.isSelected() && cacheDir != null) {
            file = cacheDir.resolve(fileName).toFile();
        } else {
            file = fileChooser.showSaveDialog(workbenchTabs.getScene() != null
                    ? workbenchTabs.getScene().getWindow()
                    : null);
        }
        if (file == null) {
            return;
        }
        if (selected.getType() == WorkbenchSourceType.URL_CSV) {
            updateStatus("Downloading " + selected.getName() + "...");
            downloadToFile(selected.getLocation(), file.toPath());
        } else if (selected.getType() == WorkbenchSourceType.GAIA_TAP) {
            updateStatus("Submitting Gaia TAP job...");
            downloadGaiaTapToFile(selected.getLocation(), file.toPath(), null);
        } else if (selected.getType() == WorkbenchSourceType.SIMBAD_TAP) {
            updateStatus("Submitting SIMBAD TAP job...");
            downloadSimbadTapToFile(selected.getLocation(), file.toPath(), null);
        } else if (selected.getType() == WorkbenchSourceType.VIZIER_TAP) {
            updateStatus("Submitting VizieR TAP job...");
            downloadVizierTapToFile(selected.getLocation(), file.toPath(), null);
        } else {
            showError("Download", "Only URL or TAP sources can be downloaded.");
        }
    }

    @FXML
    private void onConvertHygCsv() {
        log.info("Data Workbench: convert HYG CSV clicked");
        FileChooser inputChooser = new FileChooser();
        inputChooser.setTitle("Select HYG CSV File");
        inputChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));
        applyInitialDirectory(inputChooser);
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
        applyInitialDirectory(outputChooser);
        outputChooser.setInitialFileName(datasetName + ".csv");
        File outputFile = outputChooser.showSaveDialog(workbenchTabs.getScene() != null
                ? workbenchTabs.getScene().getWindow()
                : null);
        if (outputFile == null) {
            return;
        }

        updateStatus("Converting HYG TSV to TRIPS CSV...");
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                convertHygCsvToTripsCsv(inputFile.toPath(), outputFile.toPath(), datasetName,
                        count -> updateStatus("HYG converter: " + count + " rows processed"));
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            updateStatus("Converted: " + outputFile.getName());
            addLocalSourceIfMissing(outputFile.toPath());
        });
        task.setOnFailed(event -> showError("HYG Converter", String.valueOf(task.getException().getMessage())));
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void onEnrichDistances() {
        FileChooser baseChooser = new FileChooser();
        baseChooser.setTitle("Select TRIPS CSV to enrich");
        baseChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));
        applyInitialDirectory(baseChooser);
        File baseFile = baseChooser.showOpenDialog(workbenchTabs.getScene() != null
                ? workbenchTabs.getScene().getWindow()
                : null);
        if (baseFile == null) {
            return;
        }

        FileChooser gaiaChooser = new FileChooser();
        gaiaChooser.setTitle("Select Gaia DR3 CSV (optional)");
        gaiaChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));
        applyInitialDirectory(gaiaChooser);
        File gaiaFile = gaiaChooser.showOpenDialog(workbenchTabs.getScene() != null
                ? workbenchTabs.getScene().getWindow()
                : null);

        FileChooser hipChooser = new FileChooser();
        hipChooser.setTitle("Select Hipparcos CSV (optional)");
        hipChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));
        applyInitialDirectory(hipChooser);
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
        applyInitialDirectory(outputChooser);
        outputChooser.setInitialFileName(baseFile.getName().replace(".csv", "") + "-enriched.csv");
        File outputFile = outputChooser.showSaveDialog(workbenchTabs.getScene() != null
                ? workbenchTabs.getScene().getWindow()
                : null);
        if (outputFile == null) {
            return;
        }

        updateStatus("Enriching distances...");
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                enrichDistances(baseFile.toPath(),
                        gaiaFile != null ? gaiaFile.toPath() : null,
                        hipFile != null ? hipFile.toPath() : null,
                        outputFile.toPath(),
                        count -> updateStatus("Enriching: " + count + " rows processed"));
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            updateStatus("Enriched CSV saved: " + outputFile.getName());
            addLocalSourceIfMissing(outputFile.toPath());
        });
        task.setOnFailed(event -> showError("Enrich Distances", String.valueOf(task.getException().getMessage())));
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
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                enrichMissingDistancesLive(dataSetName, finalBatchSize, finalBackoffMs);
                return null;
            }
        };
        task.setOnSucceeded(event -> updateStatus("Live TAP enrichment complete."));
        task.setOnFailed(event -> showError("Enrich Distances", String.valueOf(task.getException().getMessage())));
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void onCancelTap() {
        tapCancelRequested = true;
        String jobUrl = tapJobUrl;
        if (jobUrl == null || jobUrl.isBlank()) {
            updateStatus("No TAP job to cancel.");
            return;
        }
        updateStatus("Cancelling TAP job...");
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                HttpClient client = HttpClient.newHttpClient();
                String body = "PHASE=ABORT";
                HttpRequest request = HttpRequest.newBuilder(URI.create(jobUrl + "/phase"))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build();
                client.send(request, HttpResponse.BodyHandlers.discarding());
                return null;
            }
        };
        task.setOnSucceeded(event -> updateStatus("TAP job cancelled."));
        task.setOnFailed(event -> showError("Cancel TAP", String.valueOf(task.getException().getMessage())));
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
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
        ensureLocalCsvSource(selected, this::loadSourceFieldsFromPath);
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
        applyInitialDirectory(fileChooser);
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
        applyInitialDirectory(fileChooser);
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
        List<MappingRow> gaiaMappings = defaultGaiaMappings();
        int gaiaAdded = 0;
        for (MappingRow mapping : gaiaMappings) {
            if (mappedTargets.contains(mapping.getTargetField())) {
                continue;
            }
            mappings.add(mapping);
            mappedTargets.add(mapping.getTargetField());
            gaiaAdded++;
        }
        List<MappingRow> simbadMappings = defaultSimbadMappings();
        int simbadAdded = 0;
        for (MappingRow mapping : simbadMappings) {
            if (mappedTargets.contains(mapping.getTargetField())) {
                continue;
            }
            mappings.add(mapping);
            mappedTargets.add(mapping.getTargetField());
            simbadAdded++;
        }
        List<MappingRow> vizierMappings = defaultVizierMappings();
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
        ensureLocalCsvSource(selected, this::loadPreviewFromPath);
    }

    private void addLocalCsvSource() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select CSV Source");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));
        applyInitialDirectory(fileChooser);
        File file = fileChooser.showOpenDialog(workbenchTabs.getScene() != null
                ? workbenchTabs.getScene().getWindow()
                : null);
        if (file == null) {
            return;
        }
        WorkbenchSource source = WorkbenchSource.localCsv(file.getName(), file.getAbsolutePath());
        sources.add(source);
        sourceStatusLabel.setText("Added source: " + source.getName());
    }

    private void addUrlCsvSource() {
        TextInputDialog dialog = new TextInputDialog("https://");
        dialog.setTitle("Add CSV URL");
        dialog.setHeaderText("Enter a URL to a CSV source");
        dialog.setContentText("URL:");
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }
        String url = result.get().trim();
        if (!url.startsWith("http")) {
            showError("Add Source", "URL must start with http or https.");
            return;
        }
        WorkbenchSource source = WorkbenchSource.urlCsv(url);
        sources.add(source);
        sourceStatusLabel.setText("Added source: " + source.getName());
    }

    private void addGaiaTapSource() {
        Dialog<GaiaQuerySpec> dialog = new Dialog<>();
        dialog.setTitle("Add Gaia TAP Source");
        dialog.setHeaderText("Define a Gaia TAP query");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField nameField = new TextField("Gaia DR3 query");
        TextArea queryArea = new TextArea(defaultGaiaQuery(2000));
        queryArea.setPrefColumnCount(60);
        queryArea.setPrefRowCount(8);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(8);
        gridPane.setVgap(8);
        gridPane.add(new Label("Name"), 0, 0);
        gridPane.add(nameField, 1, 0);
        gridPane.add(new Label("ADQL"), 0, 1);
        gridPane.add(queryArea, 1, 1);

        dialog.getDialogPane().setContent(gridPane);
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return new GaiaQuerySpec(nameField.getText().trim(), queryArea.getText().trim());
            }
            return null;
        });

        Optional<GaiaQuerySpec> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }
        GaiaQuerySpec spec = result.get();
        if (spec.adql.isEmpty()) {
            showError("Add Gaia TAP Source", "ADQL query cannot be empty.");
            return;
        }
        String name = spec.name.isEmpty() ? "Gaia DR3 query" : spec.name;
        WorkbenchSource source = WorkbenchSource.gaiaTap(name, spec.adql);
        sources.add(source);
        sourceStatusLabel.setText("Added source: " + source.getName());
    }

    private void addGaiaTapSource(String adql, String fileName) {
        WorkbenchSource source = WorkbenchSource.gaiaTap(fileName, adql);
        sources.add(source);
        sourceStatusLabel.setText("Added source: " + source.getName());
    }

    private void addSimbadTapSource() {
        Dialog<GaiaQuerySpec> dialog = new Dialog<>();
        dialog.setTitle("Add SIMBAD TAP Source");
        dialog.setHeaderText("Define a SIMBAD TAP query");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField nameField = new TextField("SIMBAD query");
        TextArea queryArea = new TextArea(defaultSimbadQuery(1000));
        queryArea.setPrefColumnCount(60);
        queryArea.setPrefRowCount(8);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(8);
        gridPane.setVgap(8);
        gridPane.add(new Label("Name"), 0, 0);
        gridPane.add(nameField, 1, 0);
        gridPane.add(new Label("ADQL"), 0, 1);
        gridPane.add(queryArea, 1, 1);

        dialog.getDialogPane().setContent(gridPane);
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return new GaiaQuerySpec(nameField.getText().trim(), queryArea.getText().trim());
            }
            return null;
        });

        Optional<GaiaQuerySpec> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }
        GaiaQuerySpec spec = result.get();
        if (spec.adql.isEmpty()) {
            showError("Add SIMBAD TAP Source", "ADQL query cannot be empty.");
            return;
        }
        String name = spec.name.isEmpty() ? "SIMBAD query" : spec.name;
        WorkbenchSource source = WorkbenchSource.simbadTap(name, spec.adql);
        sources.add(source);
        sourceStatusLabel.setText("Added source: " + source.getName());
    }

    private void addSimbadTapSource(String adql, String fileName) {
        WorkbenchSource source = WorkbenchSource.simbadTap(fileName, adql);
        sources.add(source);
        sourceStatusLabel.setText("Added source: " + source.getName());
    }

    private void addVizierTapSource() {
        Dialog<GaiaQuerySpec> dialog = new Dialog<>();
        dialog.setTitle("Add VizieR TAP Source");
        dialog.setHeaderText("Define a VizieR TAP query");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField nameField = new TextField("VizieR query");
        TextArea queryArea = new TextArea(defaultVizierHipparcosQuery(1000));
        queryArea.setPrefColumnCount(60);
        queryArea.setPrefRowCount(8);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(8);
        gridPane.setVgap(8);
        gridPane.add(new Label("Name"), 0, 0);
        gridPane.add(nameField, 1, 0);
        gridPane.add(new Label("ADQL"), 0, 1);
        gridPane.add(queryArea, 1, 1);

        dialog.getDialogPane().setContent(gridPane);
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return new GaiaQuerySpec(nameField.getText().trim(), queryArea.getText().trim());
            }
            return null;
        });

        Optional<GaiaQuerySpec> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }
        GaiaQuerySpec spec = result.get();
        if (spec.adql.isEmpty()) {
            showError("Add VizieR TAP Source", "ADQL query cannot be empty.");
            return;
        }
        String name = spec.name.isEmpty() ? "VizieR query" : spec.name;
        WorkbenchSource source = WorkbenchSource.vizierTap(name, spec.adql);
        sources.add(source);
        sourceStatusLabel.setText("Added source: " + source.getName());
    }

    private void addVizierTapSource(String adql, String fileName) {
        WorkbenchSource source = WorkbenchSource.vizierTap(fileName, adql);
        sources.add(source);
        sourceStatusLabel.setText("Added source: " + source.getName());
    }

    private void addVizierTableLookupSource() {
        TextInputDialog dialog = new TextInputDialog("lamost");
        dialog.setTitle("VizieR Table Lookup");
        dialog.setHeaderText("Search TAP_SCHEMA.tables for a catalog");
        dialog.setContentText("Keyword:");
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }
        String keyword = result.get().trim();
        if (keyword.isEmpty()) {
            showError("VizieR Table Lookup", "Keyword cannot be empty.");
            return;
        }
        String adql = vizierTableLookupQuery(keyword, 200);
        String fileName = "VIZIER_TABLES_" + keyword.replaceAll("\\s+", "_") + ".csv";
        addVizierTapSource(adql, fileName);
    }

    private void addVizierPresetWithPrompt(String label, String fileName) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("VizieR Catalog");
        dialog.setHeaderText("Enter the VizieR table ID for " + label);
        dialog.setContentText("Table ID (example: V/164/dr5):");
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }
        String tableId = sanitizeVizierTableId(result.get());
        if (tableId.isEmpty()) {
            showError("VizieR Catalog", "Table ID cannot be empty.");
            return;
        }
        String adql = "SELECT TOP 1000 * FROM \"" + tableId + "\"";
        addVizierTapSource(adql, fileName);
    }

    private String buildHygDatasetName() {
        return "HYG-MERGED-2M-TRIPS-" + LocalDateTime.now().format(HYG_TIMESTAMP_FORMAT);
    }

    private void convertHygCsvToTripsCsv(Path inputPath,
                                         Path outputPath,
                                         String datasetName,
                                         Consumer<Long> progressConsumer) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(inputPath, StandardCharsets.UTF_8);
             BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {

            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IOException("Input file is empty.");
            }
            boolean tabDelimited = isTabDelimited(headerLine);
            String[] headers = splitHygLine(headerLine, tabDelimited);
            Map<String, Integer> headerIndex = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                headerIndex.put(unquote(headers[i]).trim(), i);
            }

            Map<String, Integer> csvIndex = buildCsvIndex();
            writer.write(String.join(",", CSV_HEADER_COLUMNS));
            writer.newLine();

            long count = 0;
            writeSolRow(writer, csvIndex, datasetName);
            count++;
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = splitHygLine(line, tabDelimited);

                String hygId = getTsvField(headerIndex, values, "id");
                String tyc = getTsvField(headerIndex, values, "tyc");
                String gaia = getTsvField(headerIndex, values, "gaia");
                String hip = getTsvField(headerIndex, values, "hip");
                String hd = getTsvField(headerIndex, values, "hd");
                String hr = getTsvField(headerIndex, values, "hr");
                String gl = getTsvField(headerIndex, values, "gl");
                String bayer = getTsvField(headerIndex, values, "bayer");
                String flam = getTsvField(headerIndex, values, "flam");
                String con = getTsvField(headerIndex, values, "con");
                String proper = getTsvField(headerIndex, values, "proper");

                String ra = getTsvField(headerIndex, values, "ra");
                String dec = getTsvField(headerIndex, values, "dec");
                String dist = getTsvField(headerIndex, values, "dist");
                String x0 = getTsvField(headerIndex, values, "x0");
                String y0 = getTsvField(headerIndex, values, "y0");
                String z0 = getTsvField(headerIndex, values, "z0");
                String pmRa = getTsvField(headerIndex, values, "pm_ra");
                String pmDec = getTsvField(headerIndex, values, "pm_dec");
                String rv = getTsvField(headerIndex, values, "rv");
                String spect = getTsvField(headerIndex, values, "spect");
                String colorIndex = getTsvField(headerIndex, values, "ci");
                String mag = getTsvField(headerIndex, values, "mag");
                String absMag = getTsvField(headerIndex, values, "absmag");
                String vx = getTsvField(headerIndex, values, "vx");
                String vy = getTsvField(headerIndex, values, "vy");
                String vz = getTsvField(headerIndex, values, "vz");
                String resolvedDistance = resolveDistance(dist, x0, y0, z0);
                Coordinates hygCoordinates = resolveCoordinates(x0, y0, z0, ra, dec, resolvedDistance);

                String displayName = chooseDisplayName(proper, bayer, flam, con, gl, hd, hip, hr, tyc, gaia, hygId);
                if (isSolRow(displayName, proper)) {
                    continue;
                }
                String catalogIdList = buildHygCatalogIdList(hygId, tyc, gaia, hip, hd, hr, gl);

                String[] row = new String[CSV_HEADER_COLUMNS.size()];
                Arrays.fill(row, "");
                setCsvValue(row, csvIndex, "dataSetName", datasetName);
                setCsvValue(row, csvIndex, "displayName", displayName);
                setCsvValue(row, csvIndex, "commonName", proper);
                setCsvValue(row, csvIndex, "Epoch", "J2000");
                setCsvValue(row, csvIndex, "constellationName", con);
                setCsvValue(row, csvIndex, "source", HYG_SOURCE_NAME);
                setCsvValue(row, csvIndex, "catalogIdList", catalogIdList);
                setCsvValue(row, csvIndex, "Gaia DR3", gaia);
                setCsvValue(row, csvIndex, "ra", ra);
                setCsvValue(row, csvIndex, "declination", dec);
                setCsvValue(row, csvIndex, "x", hygCoordinates.x);
                setCsvValue(row, csvIndex, "y", hygCoordinates.y);
                setCsvValue(row, csvIndex, "z", hygCoordinates.z);
                setCsvValue(row, csvIndex, "pmra", pmRa);
                setCsvValue(row, csvIndex, "pmdec", pmDec);
                setCsvValue(row, csvIndex, "distance", resolvedDistance);
                setCsvValue(row, csvIndex, "radialVelocity", rv);
                setCsvValue(row, csvIndex, "spectralClass", spect);
                setCsvValue(row, csvIndex, "bprp", colorIndex);
                setCsvValue(row, csvIndex, "magv", mag);
                setCsvValue(row, csvIndex, "absmag", absMag);
                setCsvValue(row, csvIndex, "miscText2", "vx");
                setCsvValue(row, csvIndex, "miscText3", "vy");
                setCsvValue(row, csvIndex, "miscText4", "vz");
                setCsvValue(row, csvIndex, "miscNum1", vx);
                setCsvValue(row, csvIndex, "miscNum2", vy);
                setCsvValue(row, csvIndex, "miscNum3", vz);
                setCsvValue(row, csvIndex, "realStar", "true");
                setCsvValue(row, csvIndex, "other", "false");
                setCsvValue(row, csvIndex, "anomaly", "false");
                setCsvValue(row, csvIndex, "polity", "NA");
                setCsvValue(row, csvIndex, "worldType", "NA");
                setCsvValue(row, csvIndex, "fuelType", "NA");
                setCsvValue(row, csvIndex, "portType", "NA");
                setCsvValue(row, csvIndex, "populationType", "NA");
                setCsvValue(row, csvIndex, "techType", "NA");
                setCsvValue(row, csvIndex, "productType", "NA");
                setCsvValue(row, csvIndex, "milSpaceType", "NA");
                setCsvValue(row, csvIndex, "milPlanType", "NA");

                List<String> rowValues = new ArrayList<>(row.length);
                for (String value : row) {
                    rowValues.add(escapeCsv(value));
                }
                writer.write(String.join(",", rowValues));
                writer.newLine();

                count++;
                if (count % 50000 == 0) {
                    if (progressConsumer != null) {
                        progressConsumer.accept(count);
                    }
                    log.info("HYG converter: {} rows written", count);
                }
            }

            log.info("HYG converter complete. rows={}", count);
            if (progressConsumer != null) {
                progressConsumer.accept(count);
            }
        }
    }

    private Map<String, Integer> buildCsvIndex() {
        Map<String, Integer> index = new HashMap<>();
        for (int i = 0; i < CSV_HEADER_COLUMNS.size(); i++) {
            index.put(CSV_HEADER_COLUMNS.get(i), i);
        }
        return index;
    }

    private void setCsvValue(String[] row, Map<String, Integer> index, String column, String value) {
        Integer idx = index.get(column);
        if (idx != null) {
            row[idx] = value == null ? "" : value;
        }
    }

    private boolean isTabDelimited(String headerLine) {
        return headerLine.contains("\t") && !headerLine.contains(",");
    }

    private String[] splitHygLine(String line, boolean tabDelimited) {
        if (tabDelimited) {
            return line.split("\t", -1);
        }
        return splitCsvLine(line);
    }

    private String resolveDistance(String dist, String x0, String y0, String z0) {
        String normalized = dist == null ? "" : dist.trim();
        if (!normalized.isEmpty()) {
            double value = parseDoubleSafe(normalized);
            if (value > 0) {
                return normalized;
            }
        }
        double x = parseDoubleSafe(x0);
        double y = parseDoubleSafe(y0);
        double z = parseDoubleSafe(z0);
        double computed = Math.sqrt((x * x) + (y * y) + (z * z));
        if (computed > 0) {
            return Double.toString(computed);
        }
        return "0.0";
    }

    private double parseDoubleSafe(String value) {
        if (value == null) {
            return 0.0;
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(normalized);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private Coordinates resolveCoordinates(String x0, String y0, String z0,
                                           String ra, String dec, String dist) {
        double x = parseDoubleSafe(x0);
        double y = parseDoubleSafe(y0);
        double z = parseDoubleSafe(z0);
        if (x != 0.0 || y != 0.0 || z != 0.0) {
            return Coordinates.from(x, y, z);
        }
        double distance = parseDoubleSafe(dist);
        if (distance <= 0.0) {
            return Coordinates.from(0.0, 0.0, 0.0);
        }
        double raDeg = parseDoubleSafe(ra);
        double decDeg = parseDoubleSafe(dec);
        double raRad = Math.toRadians(raDeg);
        double decRad = Math.toRadians(decDeg);
        double cosDec = Math.cos(decRad);
        double calcX = distance * cosDec * Math.cos(raRad);
        double calcY = distance * cosDec * Math.sin(raRad);
        double calcZ = distance * Math.sin(decRad);
        return Coordinates.from(calcX, calcY, calcZ);
    }

    private static class Coordinates {
        private final String x;
        private final String y;
        private final String z;

        private Coordinates(String x, String y, String z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        private static Coordinates from(double x, double y, double z) {
            return new Coordinates(Double.toString(x), Double.toString(y), Double.toString(z));
        }
    }

    private void writeSolRow(BufferedWriter writer,
                             Map<String, Integer> csvIndex,
                             String datasetName) throws IOException {
        String[] row = new String[CSV_HEADER_COLUMNS.size()];
        Arrays.fill(row, "");
        setCsvValue(row, csvIndex, "dataSetName", datasetName);
        setCsvValue(row, csvIndex, "displayName", "Sol");
        setCsvValue(row, csvIndex, "commonName", "Sol");
        setCsvValue(row, csvIndex, "Epoch", "J2000");
        setCsvValue(row, csvIndex, "mass", "1.99E+30");
        setCsvValue(row, csvIndex, "notes", "none");
        setCsvValue(row, csvIndex, "source", HYG_SOURCE_NAME);
        setCsvValue(row, csvIndex, "catalogIdList", "HYG 0");
        setCsvValue(row, csvIndex, "radius", "695700.0");
        setCsvValue(row, csvIndex, "ra", "0.0");
        setCsvValue(row, csvIndex, "declination", "0.0");
        setCsvValue(row, csvIndex, "distance", "0.0");
        setCsvValue(row, csvIndex, "spectralClass", "G2V");
        setCsvValue(row, csvIndex, "temperature", "5772.0");
        setCsvValue(row, csvIndex, "realStar", "true");
        setCsvValue(row, csvIndex, "other", "false");
        setCsvValue(row, csvIndex, "anomaly", "false");
        setCsvValue(row, csvIndex, "polity", "NA");
        setCsvValue(row, csvIndex, "worldType", "NA");
        setCsvValue(row, csvIndex, "fuelType", "NA");
        setCsvValue(row, csvIndex, "portType", "NA");
        setCsvValue(row, csvIndex, "populationType", "NA");
        setCsvValue(row, csvIndex, "techType", "NA");
        setCsvValue(row, csvIndex, "productType", "NA");
        setCsvValue(row, csvIndex, "milSpaceType", "NA");
        setCsvValue(row, csvIndex, "milPlanType", "NA");

        List<String> rowValues = new ArrayList<>(row.length);
        for (String value : row) {
            rowValues.add(escapeCsv(value));
        }
        writer.write(String.join(",", rowValues));
        writer.newLine();
    }

    private boolean isSolRow(String displayName, String proper) {
        if (displayName != null && displayName.trim().equalsIgnoreCase("Sol")) {
            return true;
        }
        return proper != null && proper.trim().equalsIgnoreCase("Sol");
    }

    private String getTsvField(Map<String, Integer> index, String[] values, String name) {
        Integer pos = index.get(name);
        if (pos == null || pos < 0 || pos >= values.length) {
            return "";
        }
        return unquote(values[pos]).trim();
    }

    private String[] splitCsvLine(String line) {
        if (line == null) {
            return new String[0];
        }
        return line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
    }

    private String chooseDisplayName(String proper,
                                     String bayer,
                                     String flam,
                                     String con,
                                     String gl,
                                     String hd,
                                     String hip,
                                     String hr,
                                     String tyc,
                                     String gaia,
                                     String hygId) {
        return firstNonEmpty(
                proper,
                formatWithConstellation(bayer, con),
                formatWithConstellation(flam, con),
                formatCatalogId("GJ", gl),
                formatCatalogId("HD", hd),
                formatCatalogId("HIP", hip),
                formatCatalogId("HR", hr),
                formatCatalogId("TYC", tyc),
                formatCatalogId("Gaia", gaia),
                formatCatalogId("HYG", hygId)
        );
    }

    private String formatWithConstellation(String value, String con) {
        if (value == null || value.isBlank()) {
            return "";
        }
        if (con == null || con.isBlank()) {
            return value.trim();
        }
        return value.trim() + " " + con.trim();
    }

    private String formatCatalogId(String prefix, String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return prefix + " " + value.trim();
    }

    private String firstNonEmpty(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "";
    }

    private String buildHygCatalogIdList(String hygId,
                                         String tyc,
                                         String gaia,
                                         String hip,
                                         String hd,
                                         String hr,
                                         String gl) {
        List<String> entries = new ArrayList<>();
        if (hygId != null && !hygId.isBlank()) {
            entries.add("HYG " + hygId.trim());
        }
        if (tyc != null && !tyc.isBlank()) {
            entries.add("TYC " + tyc.trim());
        }
        if (gaia != null && !gaia.isBlank()) {
            entries.add("Gaia DR3 " + gaia.trim());
        }
        if (hip != null && !hip.isBlank()) {
            entries.add("HIP " + hip.trim());
        }
        if (hd != null && !hd.isBlank()) {
            entries.add("HD " + hd.trim());
        }
        if (hr != null && !hr.isBlank()) {
            entries.add("HR " + hr.trim());
        }
        if (gl != null && !gl.isBlank()) {
            entries.add("GJ " + gl.trim());
        }
        return String.join("|", entries);
    }

    private void downloadToFile(String url, Path outputPath) {
        downloadToFile(url, outputPath, null);
    }

    private void downloadToFile(String url, Path outputPath, Runnable onSuccess) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
                HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(outputPath));
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    throw new IOException("HTTP " + response.statusCode());
                }
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            updateStatus("Downloaded: " + outputPath.getFileName());
            addLocalSourceIfMissing(outputPath);
            if (onSuccess != null) {
                onSuccess.run();
            }
        });
        task.setOnFailed(event -> showError("Download failed", String.valueOf(task.getException().getMessage())));
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void downloadGaiaTapToFile(String adql, Path outputPath, Runnable onSuccess) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                tapCancelRequested = false;
                tapLabel = "Gaia TAP";
                HttpClient client = HttpClient.newHttpClient();
                String body = "REQUEST=doQuery&LANG=ADQL&FORMAT=csv&PHASE=RUN&QUERY="
                        + URLEncoder.encode(adql, StandardCharsets.UTF_8);
                HttpRequest request = HttpRequest.newBuilder(URI.create(GAIA_TAP_BASE_URL + "/async"))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build();
                HttpResponse<Void> submitResponse = client.send(request, HttpResponse.BodyHandlers.discarding());
                log.info("Gaia TAP submit status: {}", submitResponse.statusCode());
                Optional<String> locationHeader = submitResponse.headers().firstValue("location");
                if (locationHeader.isEmpty()) {
                    throw new IOException("Gaia TAP submission failed. HTTP " + submitResponse.statusCode());
                }
                String jobUrl = locationHeader.get();
                log.info("Gaia TAP job URL: {}", jobUrl);
                tapJobUrl = jobUrl;
                tapStartMillis = System.currentTimeMillis();
                updateStatus("Gaia TAP job submitted.");
                startTapJobIfPending(client, jobUrl);
                waitForTapCompletion(client, jobUrl);
                updateStatus("Gaia TAP completed. Downloading results...");
                log.info("Gaia TAP downloading results to {}", outputPath);
                HttpRequest resultRequest = HttpRequest.newBuilder(URI.create(jobUrl + "/results/result"))
                        .GET()
                        .build();
                HttpResponse<Path> resultResponse = client.send(resultRequest, HttpResponse.BodyHandlers.ofFile(outputPath));
                log.info("Gaia TAP result status: {}", resultResponse.statusCode());
                if (resultResponse.statusCode() < 200 || resultResponse.statusCode() >= 300) {
                    throw new IOException("Gaia TAP download failed. HTTP " + resultResponse.statusCode());
                }
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            updateStatus("Downloaded: " + outputPath.getFileName());
            addLocalSourceIfMissing(outputPath);
            if (onSuccess != null) {
                onSuccess.run();
            }
            tapJobUrl = null;
            tapStartMillis = 0L;
        });
        task.setOnFailed(event -> {
            tapJobUrl = null;
            tapStartMillis = 0L;
            showError("Gaia TAP failed", String.valueOf(task.getException().getMessage()));
        });
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void downloadSimbadTapToFile(String adql, Path outputPath, Runnable onSuccess) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                tapCancelRequested = false;
                tapLabel = "SIMBAD TAP";
                HttpClient client = HttpClient.newHttpClient();
                String body = "REQUEST=doQuery&LANG=ADQL&FORMAT=csv&PHASE=RUN&QUERY="
                        + URLEncoder.encode(adql, StandardCharsets.UTF_8);
                HttpRequest request = HttpRequest.newBuilder(URI.create(SIMBAD_TAP_BASE_URL + "/async"))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build();
                HttpResponse<Void> submitResponse = client.send(request, HttpResponse.BodyHandlers.discarding());
                log.info("SIMBAD TAP submit status: {}", submitResponse.statusCode());
                Optional<String> locationHeader = submitResponse.headers().firstValue("location");
                if (locationHeader.isEmpty()) {
                    throw new IOException("SIMBAD TAP submission failed. HTTP " + submitResponse.statusCode());
                }
                String jobUrl = locationHeader.get();
                log.info("SIMBAD TAP job URL: {}", jobUrl);
                tapJobUrl = jobUrl;
                tapStartMillis = System.currentTimeMillis();
                updateStatus("SIMBAD TAP job submitted.");
                startTapJobIfPending(client, jobUrl);
                waitForTapCompletion(client, jobUrl);
                updateStatus("SIMBAD TAP completed. Downloading results...");
                log.info("SIMBAD TAP downloading results to {}", outputPath);
                HttpRequest resultRequest = HttpRequest.newBuilder(URI.create(jobUrl + "/results/result"))
                        .GET()
                        .build();
                HttpResponse<Path> resultResponse = client.send(resultRequest, HttpResponse.BodyHandlers.ofFile(outputPath));
                log.info("SIMBAD TAP result status: {}", resultResponse.statusCode());
                if (resultResponse.statusCode() < 200 || resultResponse.statusCode() >= 300) {
                    throw new IOException("SIMBAD TAP download failed. HTTP " + resultResponse.statusCode());
                }
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            updateStatus("Downloaded: " + outputPath.getFileName());
            addLocalSourceIfMissing(outputPath);
            if (onSuccess != null) {
                onSuccess.run();
            }
            tapJobUrl = null;
            tapStartMillis = 0L;
        });
        task.setOnFailed(event -> {
            tapJobUrl = null;
            tapStartMillis = 0L;
            showError("SIMBAD TAP failed", String.valueOf(task.getException().getMessage()));
        });
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void downloadVizierTapToFile(String adql, Path outputPath, Runnable onSuccess) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                tapCancelRequested = false;
                tapLabel = "VizieR TAP";
                log.info("VizieR TAP ADQL: {}", adql);
                HttpClient client = HttpClient.newHttpClient();
                String body = "REQUEST=doQuery&LANG=ADQL&FORMAT=csv&PHASE=RUN&QUERY="
                        + URLEncoder.encode(adql, StandardCharsets.UTF_8);
                HttpRequest request = HttpRequest.newBuilder(URI.create(VIZIER_TAP_BASE_URL + "/async"))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build();
                HttpResponse<Void> submitResponse = client.send(request, HttpResponse.BodyHandlers.discarding());
                log.info("VizieR TAP submit status: {}", submitResponse.statusCode());
                Optional<String> locationHeader = submitResponse.headers().firstValue("location");
                if (locationHeader.isEmpty()) {
                    throw new IOException("VizieR TAP submission failed. HTTP " + submitResponse.statusCode());
                }
                String jobUrl = locationHeader.get();
                log.info("VizieR TAP job URL: {}", jobUrl);
                tapJobUrl = jobUrl;
                tapStartMillis = System.currentTimeMillis();
                updateStatus("VizieR TAP job submitted.");
                startTapJobIfPending(client, jobUrl);
                waitForTapCompletion(client, jobUrl);
                updateStatus("VizieR TAP completed. Downloading results...");
                log.info("VizieR TAP downloading results to {}", outputPath);
                HttpRequest resultRequest = HttpRequest.newBuilder(URI.create(jobUrl + "/results/result"))
                        .GET()
                        .build();
                HttpResponse<Path> resultResponse = client.send(resultRequest, HttpResponse.BodyHandlers.ofFile(outputPath));
                log.info("VizieR TAP result status: {}", resultResponse.statusCode());
                if (resultResponse.statusCode() < 200 || resultResponse.statusCode() >= 300) {
                    throw new IOException("VizieR TAP download failed. HTTP " + resultResponse.statusCode());
                }
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            updateStatus("Downloaded: " + outputPath.getFileName());
            addLocalSourceIfMissing(outputPath);
            if (onSuccess != null) {
                onSuccess.run();
            }
            tapJobUrl = null;
            tapStartMillis = 0L;
        });
        task.setOnFailed(event -> {
            tapJobUrl = null;
            tapStartMillis = 0L;
            showError("VizieR TAP failed", String.valueOf(task.getException().getMessage()));
        });
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void waitForTapCompletion(HttpClient client, String jobUrl) throws IOException, InterruptedException {
        long delayMs = GAIA_TAP_INITIAL_DELAY_MS;
        long maxDelayMs = GAIA_TAP_MAX_DELAY_MS;
        long maxWaitMs = GAIA_TAP_MAX_WAIT_MS;
        long waitedMs = 0;
        while (waitedMs < maxWaitMs) {
            if (tapCancelRequested) {
                throw new IOException("TAP job cancelled.");
            }
            HttpRequest phaseRequest = HttpRequest.newBuilder(URI.create(jobUrl + "/phase"))
                    .GET()
                    .build();
            HttpResponse<String> phaseResponse = client.send(phaseRequest, HttpResponse.BodyHandlers.ofString());
            String phase = phaseResponse.body().trim();
            log.info("TAP phase status: {} body: {}", phaseResponse.statusCode(), phase);
            updateStatus(tapLabel + " phase: " + phase + " (" + formatElapsed(tapStartMillis) + ")");
            if ("COMPLETED".equalsIgnoreCase(phase)) {
                return;
            }
            if ("ERROR".equalsIgnoreCase(phase) || "ABORTED".equalsIgnoreCase(phase)) {
                logTapError(client, jobUrl);
                throw new IOException("TAP job " + phase);
            }
            Thread.sleep(delayMs);
            waitedMs += delayMs;
            delayMs = Math.min(delayMs * 2, maxDelayMs);
        }
        throw new IOException("Gaia TAP job timed out.");
    }

    private void startTapJobIfPending(HttpClient client, String jobUrl) throws IOException, InterruptedException {
        HttpRequest phaseRequest = HttpRequest.newBuilder(URI.create(jobUrl + "/phase"))
                .GET()
                .build();
        HttpResponse<String> phaseResponse = client.send(phaseRequest, HttpResponse.BodyHandlers.ofString());
        String phase = phaseResponse.body().trim();
        if (!"PENDING".equalsIgnoreCase(phase)) {
            return;
        }
        log.info("Gaia TAP phase is PENDING, sending PHASE=RUN");
        HttpRequest runRequest = HttpRequest.newBuilder(URI.create(jobUrl + "/phase"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("PHASE=RUN"))
                .build();
        client.send(runRequest, HttpResponse.BodyHandlers.discarding());
    }

    private void logTapError(HttpClient client, String jobUrl) {
        try {
            HttpRequest errorRequest = HttpRequest.newBuilder(URI.create(jobUrl + "/error"))
                    .GET()
                    .build();
            HttpResponse<String> errorResponse = client.send(errorRequest, HttpResponse.BodyHandlers.ofString());
            if (errorResponse.statusCode() == 303) {
                Optional<String> location = errorResponse.headers().firstValue("location");
                if (location.isPresent()) {
                    HttpRequest redirectRequest = HttpRequest.newBuilder(URI.create(location.get()))
                            .GET()
                            .build();
                    HttpResponse<String> redirectResponse = client.send(redirectRequest, HttpResponse.BodyHandlers.ofString());
                    log.info("TAP error redirect status: {} body: {}", redirectResponse.statusCode(), redirectResponse.body());
                    return;
                }
            }
            log.info("TAP error status: {} body: {}", errorResponse.statusCode(), errorResponse.body());
        } catch (IOException | InterruptedException e) {
            log.warn("Failed to read TAP error response: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
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
    }

    private void loadSourceFieldsFromPath(Path path) {
        if (!Files.exists(path)) {
            showError("Load Fields", "File not found: " + path);
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String header = Optional.ofNullable(reader.readLine()).orElse("");
            sourceFields.setAll(Arrays.asList(splitCsvLine(header)));
            mappingStatusLabel.setText("Loaded " + sourceFields.size() + " source fields.");
        } catch (IOException e) {
            showError("Load Fields", "Unable to read source header: " + e.getMessage());
        }
    }

    private void enrichDistances(Path baseCsv,
                                 Path gaiaCsv,
                                 Path hipCsv,
                                 Path outputCsv,
                                 Consumer<Long> progressConsumer) throws IOException {
        Map<String, Double> gaiaParallax = gaiaCsv != null
                ? loadParallaxMap(gaiaCsv, List.of("source_id", "gaia_source_id", "gaia_dr3", "gaia"), List.of("parallax"))
                : Map.of();
        Map<String, Double> hipParallax = hipCsv != null
                ? loadParallaxMap(hipCsv, List.of("HIP", "hip", "hip_id"), List.of("Plx", "parallax", "plx"))
                : Map.of();

        try (BufferedReader reader = Files.newBufferedReader(baseCsv, StandardCharsets.UTF_8);
             BufferedWriter writer = Files.newBufferedWriter(outputCsv, StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (header == null) {
                throw new IOException("Base CSV is empty.");
            }
            String[] headerFields = splitCsvLine(header);
            Map<String, Integer> headerIndex = buildHeaderIndex(header);
            writer.write(String.join(",", headerFields));
            writer.newLine();

            long count = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = splitCsvLine(line);
                values = linePad(values, headerFields.length);

                int distanceIdx = headerIndex.getOrDefault("distance", -1);
                int raIdx = headerIndex.getOrDefault("ra", -1);
                int decIdx = headerIndex.getOrDefault("declination", -1);
                int xIdx = headerIndex.getOrDefault("x", -1);
                int yIdx = headerIndex.getOrDefault("y", -1);
                int zIdx = headerIndex.getOrDefault("z", -1);
                int sourceIdx = headerIndex.getOrDefault("source", -1);
                int notesIdx = headerIndex.getOrDefault("notes", -1);
                int catalogIdx = headerIndex.getOrDefault("catalogIdList", -1);
                int gaiaIdx = headerIndex.getOrDefault("Gaia DR3", -1);

                double distance = distanceIdx >= 0 ? parseDoubleSafe(values[distanceIdx]) : 0.0;
                double x = xIdx >= 0 ? parseDoubleSafe(values[xIdx]) : 0.0;
                double y = yIdx >= 0 ? parseDoubleSafe(values[yIdx]) : 0.0;
                double z = zIdx >= 0 ? parseDoubleSafe(values[zIdx]) : 0.0;

                if (distance <= 0.0 && (x == 0.0 && y == 0.0 && z == 0.0)) {
                    String gaiaId = gaiaIdx >= 0 ? extractNumericId(values[gaiaIdx]) : "";
                    String hipId = catalogIdx >= 0 ? extractHipId(values[catalogIdx]) : "";
                    Double parallax = null;
                    String sourceTag = null;
                    String notesTag = null;
                    if (!gaiaId.isEmpty()) {
                        parallax = gaiaParallax.get(gaiaId);
                        if (parallax != null && parallax > 0) {
                            sourceTag = "Gaia DR3 parallax";
                            notesTag = "distance from Gaia DR3 parallax";
                        }
                    }
                    if ((parallax == null || parallax <= 0) && !hipId.isEmpty()) {
                        parallax = hipParallax.get(hipId);
                        if (parallax != null && parallax > 0) {
                            sourceTag = "HIP parallax";
                            notesTag = "distance from HIP parallax";
                        }
                    }
                    if (parallax != null && parallax > 0 && distanceIdx >= 0 && raIdx >= 0 && decIdx >= 0) {
                        distance = calculateDistanceFromParallax(parallax);
                        values[distanceIdx] = Double.toString(distance);
                        double raDeg = parseDoubleSafe(values[raIdx]);
                        double decDeg = parseDoubleSafe(values[decIdx]);
                        double[] coords = calculateCoordinatesFromRaDec(raDeg, decDeg, distance);
                        if (xIdx >= 0) {
                            values[xIdx] = Double.toString(coords[0]);
                        }
                        if (yIdx >= 0) {
                            values[yIdx] = Double.toString(coords[1]);
                        }
                        if (zIdx >= 0) {
                            values[zIdx] = Double.toString(coords[2]);
                        }
                        if (sourceIdx >= 0 && sourceTag != null) {
                            values[sourceIdx] = appendToken(values[sourceIdx], sourceTag, "|");
                        }
                        if (notesIdx >= 0 && notesTag != null) {
                            values[notesIdx] = appendToken(values[notesIdx], notesTag, "; ");
                        }
                    }
                }

                List<String> rowValues = new ArrayList<>(values.length);
                for (String value : values) {
                    rowValues.add(escapeCsv(value));
                }
                writer.write(String.join(",", rowValues));
                writer.newLine();
                count++;
                if (count % 50000 == 0 && progressConsumer != null) {
                    progressConsumer.accept(count);
                }
            }
        }
    }

    private void enrichMissingDistancesLive(String dataSetName, int batchSize, long delayMs)
            throws IOException, InterruptedException {
        int pageSize = Math.max(batchSize * 4, 200);
        int pageIndex = 0;
        long updated = 0;
        long processed = 0;
        while (true) {
            Page<StarObject> page = starService.findMissingDistanceWithIds(dataSetName, PageRequest.of(pageIndex, pageSize));
            if (!page.hasContent()) {
                break;
            }
            List<StarObject> candidates = page.getContent();
            List<StarObject> updatedStars = new ArrayList<>();
            Set<String> updatedIds = new HashSet<>();
            int pageNumber = pageIndex + 1;
            updateStatus("Live TAP enrichment: page " + pageNumber + ", candidates " + candidates.size());

            Map<String, List<StarObject>> gaiaMap = new HashMap<>();
            Map<String, List<StarObject>> hipMap = new HashMap<>();
            Map<String, List<StarObject>> simbadMap = new HashMap<>();
            for (StarObject star : candidates) {
                if (star.getDistance() > 0) {
                    continue;
                }
                String gaiaId = extractNumericId(star.getGaiaDR3CatId());
                if (!gaiaId.isEmpty()) {
                    gaiaMap.computeIfAbsent(gaiaId, key -> new ArrayList<>()).add(star);
                    continue;
                }
                String hipId = star.getHipCatId();
                if (hipId == null || hipId.isBlank()) {
                    hipId = extractHipId(star.getRawCatalogIdList());
                }
                hipId = extractNumericId(hipId);
                if (!hipId.isEmpty()) {
                    hipMap.computeIfAbsent(hipId, key -> new ArrayList<>()).add(star);
                }
            }

            List<String> gaiaIds = new ArrayList<>(gaiaMap.keySet());
            List<String> hipIds = new ArrayList<>(hipMap.keySet());
            int gaiaBatches = (gaiaIds.size() + batchSize - 1) / batchSize;
            int hipBatches = (hipIds.size() + batchSize - 1) / batchSize;

            for (int i = 0; i < gaiaIds.size(); i += batchSize) {
                int batchNumber = (i / batchSize) + 1;
                List<String> batch = gaiaIds.subList(i, Math.min(i + batchSize, gaiaIds.size()));
                updateStatus("Live TAP: Gaia batch " + batchNumber + "/" + gaiaBatches + " (ids " + batch.size() + ")");
                Map<String, Double> parallaxById = fetchGaiaParallax(batch);
                int updatedBefore = updatedStars.size();
                for (Map.Entry<String, Double> entry : parallaxById.entrySet()) {
                    List<StarObject> stars = gaiaMap.get(entry.getKey());
                    if (stars != null) {
                        for (StarObject star : stars) {
                            if (applyParallaxEnrichment(star, entry.getValue(), "Gaia DR3 parallax",
                                    "distance from Gaia DR3 parallax") && updatedIds.add(star.getId())) {
                                updatedStars.add(star);
                            }
                        }
                    }
                }
                int updatedInBatch = updatedStars.size() - updatedBefore;
                log.info("Gaia TAP batch {}/{}: ids={}, matches={}, updated={}",
                        batchNumber, gaiaBatches, batch.size(), parallaxById.size(), updatedInBatch);
                if (delayMs > 0) {
                    Thread.sleep(delayMs);
                }
            }

            for (int i = 0; i < hipIds.size(); i += batchSize) {
                int batchNumber = (i / batchSize) + 1;
                List<String> batch = hipIds.subList(i, Math.min(i + batchSize, hipIds.size()));
                updateStatus("Live TAP: HIP batch " + batchNumber + "/" + hipBatches + " (ids " + batch.size() + ")");
                Map<String, Double> parallaxById = fetchHipParallax(batch);
                int updatedBefore = updatedStars.size();
                for (Map.Entry<String, Double> entry : parallaxById.entrySet()) {
                    List<StarObject> stars = hipMap.get(entry.getKey());
                    if (stars != null) {
                        for (StarObject star : stars) {
                            if (applyParallaxEnrichment(star, entry.getValue(), "HIP parallax",
                                    "distance from HIP parallax") && updatedIds.add(star.getId())) {
                                updatedStars.add(star);
                            }
                        }
                    }
                }
                int updatedInBatch = updatedStars.size() - updatedBefore;
                log.info("HIP TAP batch {}/{}: ids={}, matches={}, updated={}",
                        batchNumber, hipBatches, batch.size(), parallaxById.size(), updatedInBatch);
                if (delayMs > 0) {
                    Thread.sleep(delayMs);
                }
            }

            for (StarObject star : candidates) {
                if (star.getDistance() > 0 || updatedIds.contains(star.getId())) {
                    continue;
                }
                String name = getPreferredSimbadName(star);
                if (!name.isEmpty()) {
                    String key = normalizeSimbadKey(name);
                    simbadMap.computeIfAbsent(key, value -> new ArrayList<>()).add(star);
                }
            }

            List<String> simbadNames = new ArrayList<>(simbadMap.keySet());
            int simbadBatches = (simbadNames.size() + batchSize - 1) / batchSize;
            if (!simbadNames.isEmpty()) {
                updateStatus("Live TAP: SIMBAD batches " + simbadBatches + " (names " + simbadNames.size() + ")");
            }
            for (int i = 0; i < simbadNames.size(); i += batchSize) {
                int batchNumber = (i / batchSize) + 1;
                List<String> batch = simbadNames.subList(i, Math.min(i + batchSize, simbadNames.size()));
                updateStatus("Live TAP: SIMBAD batch " + batchNumber + "/" + simbadBatches + " (names " + batch.size() + ")");
                log.info("SIMBAD TAP batch {}/{} name sample: {}",
                        batchNumber, simbadBatches, batch.subList(0, Math.min(10, batch.size())));
                Map<String, Double> parallaxByName = fetchSimbadParallax(batch);
                int updatedBefore = updatedStars.size();
                int positiveParallax = 0;
                List<String> parallaxSamples = new ArrayList<>();
                for (Map.Entry<String, Double> entry : parallaxByName.entrySet()) {
                    Double parallax = entry.getValue();
                    if (parallax != null && parallax > 0) {
                        positiveParallax++;
                        if (parallaxSamples.size() < 5) {
                            parallaxSamples.add(entry.getKey() + "=" + parallax);
                        }
                    }
                    List<StarObject> stars = simbadMap.get(normalizeSimbadKey(entry.getKey()));
                    if (stars != null) {
                        for (StarObject star : stars) {
                            if (applyParallaxEnrichment(star, parallax, "SIMBAD parallax",
                                    "distance from SIMBAD parallax") && updatedIds.add(star.getId())) {
                                updatedStars.add(star);
                            }
                        }
                    }
                }
                int updatedInBatch = updatedStars.size() - updatedBefore;
                log.info("SIMBAD TAP batch {}/{}: names={}, matches={}, updated={}",
                        batchNumber, simbadBatches, batch.size(), parallaxByName.size(), updatedInBatch);
                log.info("SIMBAD TAP batch {}/{}: positive parallaxes={}, samples={}",
                        batchNumber, simbadBatches, positiveParallax, parallaxSamples);
                if (delayMs > 0) {
                    Thread.sleep(delayMs);
                }
            }

            if (!updatedStars.isEmpty()) {
                starService.updateStars(updatedStars);
                updated += updatedStars.size();
                updateStatus("Live TAP enrichment: updated " + updated + " stars");
                log.info("Live TAP enrichment: page {} saved {}, total updated {}",
                        pageNumber, updatedStars.size(), updated);
            }
            processed += candidates.size();
            if (processed % 1000 == 0 || updatedStars.size() > 0) {
                long remaining = starService.countMissingDistance(dataSetName);
                log.info("Live TAP enrichment: processed {}, remaining missing distance {}",
                        processed, remaining);
                updateStatus("Live TAP enrichment: updated " + updated
                        + " stars, remaining missing distance " + remaining);
            }
            pageIndex++;
            if (!page.hasNext()) {
                break;
            }
        }
    }

    private Map<String, Double> fetchGaiaParallax(List<String> gaiaIds) throws IOException, InterruptedException {
        if (gaiaIds.isEmpty()) {
            return Map.of();
        }
        String idList = String.join(",", gaiaIds);
        String adql = "SELECT source_id, parallax FROM gaiadr3.gaia_source WHERE source_id IN (" + idList + ")";
        String csv = submitTapSyncCsv(GAIA_TAP_BASE_URL, adql, "Gaia TAP");
        return parseParallaxCsv(csv, "source_id", "parallax");
    }

    private Map<String, Double> fetchHipParallax(List<String> hipIds) throws IOException, InterruptedException {
        if (hipIds.isEmpty()) {
            return Map.of();
        }
        String idList = String.join(",", hipIds);
        String adql = "SELECT HIP, Plx FROM \"I/239/hip_main\" WHERE HIP IN (" + idList + ")";
        String csv = submitTapSyncCsv(VIZIER_TAP_BASE_URL, adql, "VizieR TAP");
        return parseParallaxCsv(csv, "HIP", "Plx");
    }

    private Map<String, Double> fetchSimbadParallax(List<String> simbadNames) throws IOException, InterruptedException {
        if (simbadNames.isEmpty()) {
            return Map.of();
        }
        String idList = simbadNames.stream()
                .map(this::escapeAdqlString)
                .map(name -> "'" + name + "'")
                .collect(Collectors.joining(","));
        String adql = "SELECT i.id AS id, b.plx_value "
                + "FROM ident i JOIN basic b ON i.oidref = b.oid "
                + "WHERE i.id IN (" + idList + ")";
        String csv = submitTapSyncCsv(SIMBAD_TAP_BASE_URL, adql, "SIMBAD TAP");
        logSimbadCsvSample(csv);
        return parseParallaxCsvRawId(csv, "id", "plx_value");
    }

    private void logSimbadCsvSample(String csv) {
        if (csv == null || csv.isBlank()) {
            log.info("SIMBAD TAP CSV sample: <empty>");
            return;
        }
        String[] lines = csv.split("\\r?\\n");
        if (lines.length > 0) {
            log.info("SIMBAD TAP CSV header: {}", lines[0]);
        }
        int printed = 0;
        List<String> sample = new ArrayList<>();
        for (int i = 1; i < lines.length && printed < 5; i++) {
            String line = lines[i].trim();
            if (!line.isEmpty()) {
                sample.add(line);
                printed++;
            }
        }
        log.info("SIMBAD TAP CSV sample rows: {}", sample);
    }

    private String submitTapSyncCsv(String baseUrl, String adql, String label) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        String body = "REQUEST=doQuery&LANG=ADQL&FORMAT=csv&QUERY="
                + URLEncoder.encode(adql, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/sync"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        log.info("{} sync status: {}", label, response.statusCode());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            String bodyPreview = response.body();
            if (bodyPreview != null && bodyPreview.length() > 400) {
                bodyPreview = bodyPreview.substring(0, 400) + "...";
            }
            log.error("{} sync error body: {}", label, bodyPreview);
            throw new IOException(label + " sync failed. HTTP " + response.statusCode());
        }
        return response.body();
    }

    private Map<String, Double> parseParallaxCsv(String csv, String idHeader, String parallaxHeader) {
        Map<String, Double> map = new HashMap<>();
        if (csv == null || csv.isBlank()) {
            return map;
        }
        String[] lines = csv.split("\\r?\\n");
        if (lines.length == 0) {
            return map;
        }
        String[] header = splitCsvLine(lines[0]);
        Map<String, Integer> headerIndex = new HashMap<>();
        for (int i = 0; i < header.length; i++) {
            headerIndex.put(header[i].trim(), i);
        }
        int idIdx = findHeaderIndex(headerIndex, List.of(idHeader));
        int parallaxIdx = findHeaderIndex(headerIndex, List.of(parallaxHeader));
        if (idIdx < 0 || parallaxIdx < 0) {
            return map;
        }
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            if (line.isBlank()) {
                continue;
            }
            String[] values = splitCsvLine(line);
            if (idIdx >= values.length || parallaxIdx >= values.length) {
                continue;
            }
            String id = extractNumericId(values[idIdx]);
            double parallax = parseDoubleSafe(values[parallaxIdx]);
            if (!id.isEmpty() && parallax > 0) {
                map.putIfAbsent(id, parallax);
            }
        }
        return map;
    }

    private Map<String, Double> parseParallaxCsvRawId(String csv, String idHeader, String parallaxHeader) {
        Map<String, Double> map = new HashMap<>();
        if (csv == null || csv.isBlank()) {
            return map;
        }
        String[] lines = csv.split("\\r?\\n");
        if (lines.length == 0) {
            return map;
        }
        String[] header = splitCsvLine(lines[0]);
        Map<String, Integer> headerIndex = new HashMap<>();
        for (int i = 0; i < header.length; i++) {
            headerIndex.put(unquote(header[i]).trim(), i);
        }
        int idIdx = findHeaderIndex(headerIndex, List.of(idHeader));
        int parallaxIdx = findHeaderIndex(headerIndex, List.of(parallaxHeader));
        if (idIdx < 0 || parallaxIdx < 0) {
            return map;
        }
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            if (line.isBlank()) {
                continue;
            }
            String[] values = splitCsvLine(line);
            if (idIdx >= values.length || parallaxIdx >= values.length) {
                continue;
            }
            String id = normalizeSimbadKey(values[idIdx]);
            double parallax = parseDoubleSafe(values[parallaxIdx]);
            if (!id.isEmpty() && parallax > 0) {
                map.putIfAbsent(id, parallax);
            }
        }
        return map;
    }

    private boolean applyParallaxEnrichment(StarObject star,
                                            double parallaxMas,
                                            String sourceToken,
                                            String notesToken) {
        if (parallaxMas <= 0 || star.getDistance() > 0) {
            return false;
        }
        double distance = calculateDistanceFromParallax(parallaxMas);
        if (distance <= 0) {
            return false;
        }
        star.setParallax(parallaxMas);
        star.setDistance(distance);
        double[] coords = calculateCoordinatesFromRaDec(star.getRa(), star.getDeclination(), distance);
        star.setX(coords[0]);
        star.setY(coords[1]);
        star.setZ(coords[2]);
        star.setSource(appendToken(star.getSource(), sourceToken, "|"));
        star.setNotes(appendToken(star.getNotes(), notesToken, "; "));
        return true;
    }

    private String getPreferredSimbadName(StarObject star) {
        String name = star.getCommonName();
        if (name == null || name.isBlank() || "NA".equalsIgnoreCase(name.trim()) || isNumericToken(name)) {
            name = star.getDisplayName();
        }
        if (name == null || name.isBlank() || isNumericToken(name)) {
            String catalogId = extractSimbadCatalogId(star.getRawCatalogIdList());
            if (catalogId != null && !catalogId.isBlank()) {
                name = catalogId;
            }
        }
        if (name == null) {
            return "";
        }
        return name.trim();
    }

    private boolean isNumericToken(String value) {
        if (value == null) {
            return false;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return false;
        }
        for (int i = 0; i < trimmed.length(); i++) {
            if (!Character.isDigit(trimmed.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private String extractSimbadCatalogId(String catalogIdList) {
        if (catalogIdList == null || catalogIdList.isBlank()) {
            return "";
        }
        String[] tokens = catalogIdList.split("\\|");
        for (String token : tokens) {
            String trimmed = token.trim();
            if (trimmed.startsWith("TYC ")) {
                return trimmed;
            }
        }
        for (String token : tokens) {
            String trimmed = token.trim();
            if (trimmed.startsWith("HD ") || trimmed.startsWith("HIP ") || trimmed.startsWith("HR ")
                    || trimmed.startsWith("BD ") || trimmed.startsWith("GJ ") || trimmed.startsWith("GL ")
                    || trimmed.startsWith("LHS ") || trimmed.startsWith("2MASS ")) {
                return trimmed;
            }
        }
        return "";
    }

    private String normalizeSimbadKey(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = unquote(value).trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        return trimmed.replaceAll("\\s+", " ");
    }

    private String escapeAdqlString(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("'", "''");
    }

    private Map<String, Double> loadParallaxMap(Path csvPath,
                                                List<String> idHeaders,
                                                List<String> parallaxHeaders) throws IOException {
        Map<String, Double> map = new HashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (header == null) {
                return map;
            }
            String[] headerFields = splitCsvLine(header);
            Map<String, Integer> headerIndex = new HashMap<>();
            for (int i = 0; i < headerFields.length; i++) {
                headerIndex.put(headerFields[i].trim(), i);
            }
            int idIdx = findHeaderIndex(headerIndex, idHeaders);
            int parallaxIdx = findHeaderIndex(headerIndex, parallaxHeaders);
            if (idIdx < 0 || parallaxIdx < 0) {
                return map;
            }
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = splitCsvLine(line);
                if (idIdx >= values.length || parallaxIdx >= values.length) {
                    continue;
                }
                String id = extractNumericId(values[idIdx]);
                if (id.isEmpty()) {
                    continue;
                }
                double parallax = parseDoubleSafe(values[parallaxIdx]);
                if (parallax > 0) {
                    map.putIfAbsent(id, parallax);
                }
            }
        }
        return map;
    }

    private int findHeaderIndex(Map<String, Integer> headerIndex, List<String> candidates) {
        for (String candidate : candidates) {
            for (Map.Entry<String, Integer> entry : headerIndex.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(candidate)) {
                    return entry.getValue();
                }
            }
        }
        return -1;
    }

    private String extractNumericId(String value) {
        if (value == null) {
            return "";
        }
        Matcher matcher = DIGIT_PATTERN.matcher(value);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    private String extractHipId(String catalogIdList) {
        if (catalogIdList == null) {
            return "";
        }
        String[] tokens = catalogIdList.split("\\|");
        for (String token : tokens) {
            String trimmed = token.trim();
            if (trimmed.toUpperCase().startsWith("HIP")) {
                return extractNumericId(trimmed);
            }
        }
        return "";
    }

    private double calculateDistanceFromParallax(double parallaxMas) {
        if (parallaxMas <= 0) {
            return 0.0;
        }
        double distanceParsecs = 1000.0 / parallaxMas;
        return distanceParsecs * 3.26156;
    }

    private double[] calculateCoordinatesFromRaDec(double raDeg, double decDeg, double distance) {
        double raRad = Math.toRadians(raDeg);
        double decRad = Math.toRadians(decDeg);
        double cosDec = Math.cos(decRad);
        double x = distance * cosDec * Math.cos(raRad);
        double y = distance * cosDec * Math.sin(raRad);
        double z = distance * Math.sin(decRad);
        return new double[]{x, y, z};
    }

    private String appendToken(String current, String token, String separator) {
        String base = current == null ? "" : current.trim();
        if (base.isEmpty()) {
            return token;
        }
        if (base.contains(token)) {
            return base;
        }
        return base + separator + token;
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
            previewHeaderIndex = buildHeaderIndex(header);
            previewTargetToSource = buildTargetToSourceMap();
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
            ExportResult result = exportMappedCsv(sourcePath, outputPath);
            updateStatus("Exported " + result.rowCount + " rows to " + outputName);
            writeValidationLog(outputPath, result.validationMessages);
        } catch (IOException e) {
            showError("Export failed", "Unable to write CSV: " + e.getMessage());
        }
    }

    private void validateFullFile(Path sourcePath, String sourceName) {
        try {
            ExportResult result = validateMappedCsv(sourcePath);
            for (String message : result.validationMessages) {
                appendValidationMessage(message);
            }
            updateStatus("Validated " + result.rowCount + " rows from " + sourceName);
        } catch (IOException e) {
            showError("Validate Full File", "Unable to validate CSV: " + e.getMessage());
        }
    }

    private ExportResult exportMappedCsv(Path sourcePath, Path outputPath) throws IOException {
        if (!Files.exists(sourcePath)) {
            return new ExportResult(0, List.of("Source file not found."));
        }
        try (BufferedReader reader = Files.newBufferedReader(sourcePath, StandardCharsets.UTF_8);
             BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (header == null) {
                return new ExportResult(0, List.of("Source file is empty."));
            }
            Map<String, Integer> headerIndex = buildHeaderIndex(header);
            Map<String, String> targetToSource = buildTargetToSourceMap();
            List<String> validationMessages = new ArrayList<>();
            writer.write(String.join(",", CSV_HEADER_COLUMNS));
            writer.newLine();

            int rowCount = 0;
            String line;
            int rowIndex = 1;
            while ((line = reader.readLine()) != null) {
                String[] values = splitCsvLine(line);
                Map<String, String> mappedRow = mapRow(values, headerIndex, targetToSource);
                List<String> rowValues = new ArrayList<>();
                for (String column : CSV_HEADER_COLUMNS) {
                    rowValues.add(escapeCsv(mappedRow.getOrDefault(column, "")));
                }
                writer.write(String.join(",", rowValues));
                writer.newLine();
                rowCount++;
                validationMessages.addAll(validateRow(mappedRow, rowIndex));
                rowIndex++;
            }
            if (validationMessages.isEmpty()) {
                validationMessages.add("Validation complete. Errors: 0");
            } else {
                validationMessages.add(0, "Validation complete. Errors: " + validationMessages.size());
            }
            return new ExportResult(rowCount, validationMessages);
        }
    }

    private ExportResult validateMappedCsv(Path sourcePath) throws IOException {
        if (!Files.exists(sourcePath)) {
            return new ExportResult(0, List.of("Source file not found."));
        }
        try (BufferedReader reader = Files.newBufferedReader(sourcePath, StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (header == null) {
                return new ExportResult(0, List.of("Source file is empty."));
            }
            Map<String, Integer> headerIndex = buildHeaderIndex(header);
            Map<String, String> targetToSource = buildTargetToSourceMap();
            List<String> validationMessages = new ArrayList<>();
            int rowCount = 0;
            String line;
            int rowIndex = 1;
            while ((line = reader.readLine()) != null) {
                String[] values = splitCsvLine(line);
                Map<String, String> mappedRow = mapRow(values, headerIndex, targetToSource);
                validationMessages.addAll(validateRow(mappedRow, rowIndex));
                rowCount++;
                rowIndex++;
            }
            if (validationMessages.isEmpty()) {
                validationMessages.add("Validation complete. Errors: 0");
            } else {
                validationMessages.add(0, "Validation complete. Errors: " + validationMessages.size());
            }
            return new ExportResult(rowCount, validationMessages);
        }
    }

    private Map<String, Integer> buildHeaderIndex(String headerLine) {
        String[] headerFields = splitCsvLine(headerLine);
        Map<String, Integer> headerIndex = new HashMap<>();
        for (int i = 0; i < headerFields.length; i++) {
            headerIndex.put(headerFields[i].trim(), i);
        }
        return headerIndex;
    }

    private Map<String, String> buildTargetToSourceMap() {
        Map<String, String> targetToSource = new HashMap<>();
        for (MappingRow mapping : mappings) {
            targetToSource.put(mapping.getTargetField(), mapping.getSourceField());
        }
        return targetToSource;
    }

    private Map<String, String> mapRow(String[] values,
                                       Map<String, Integer> headerIndex,
                                       Map<String, String> targetToSource) {
        Map<String, String> mappedRow = new HashMap<>();
        for (Map.Entry<String, String> entry : targetToSource.entrySet()) {
            Integer index = headerIndex.get(entry.getValue());
            if (index != null && index < values.length) {
                mappedRow.put(entry.getKey(), unquote(values[index]));
            }
        }
        return mappedRow;
    }

    private void writeValidationLog(Path outputPath, List<String> messages) throws IOException {
        Path logPath = Path.of(outputPath.toString() + ".log");
        Files.writeString(logPath, messages.stream().collect(Collectors.joining(System.lineSeparator()))
                + System.lineSeparator(), StandardCharsets.UTF_8);
        appendValidationMessage("Validation summary written: " + logPath.getFileName());
    }

    private String unquote(String value) {
        String trimmed = value.trim();
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() >= 2) {
            String inner = trimmed.substring(1, trimmed.length() - 1);
            return inner.replace("\"\"", "\"");
        }
        return trimmed;
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.contains(",") || trimmed.contains("\"") || trimmed.contains("\n")) {
            String escaped = trimmed.replace("\"", "\"\"");
            return "\"" + escaped + "\"";
        }
        return trimmed;
    }

    private String[] linePad(String[] values, int length) {
        if (values.length >= length) {
            return values;
        }
        return Arrays.copyOf(values, length);
    }

    private List<String> validateRow(Map<String, String> row, int rowIndex) {
        List<String> messages = new ArrayList<>();
        for (String req : REQUIRED_FIELDS) {
            String value = row.getOrDefault(req, "").trim();
            if (value.isEmpty()) {
                messages.add("Row " + rowIndex + ": missing required field " + req);
            }
        }
        validateRange(row, rowIndex, "ra", 0.0, 360.0, messages);
        validateRange(row, rowIndex, "declination", -90.0, 90.0, messages);
        validateMin(row, rowIndex, "distance", 0.0, messages);
        validateBoolean(row, rowIndex, "realStar", messages);
        String spectralClass = row.getOrDefault("spectralClass", "").trim();
        if (!spectralClass.isEmpty()) {
            char type = Character.toUpperCase(spectralClass.charAt(0));
            if ("OBAFGKMLTY".indexOf(type) == -1) {
                messages.add("Row " + rowIndex + ": invalid spectralClass '" + spectralClass + "'");
            }
        }
        return messages;
    }

    private void validateRange(Map<String, String> row,
                               int rowIndex,
                               String field,
                               double min,
                               double max,
                               List<String> messages) {
        String value = row.getOrDefault(field, "").trim();
        if (value.isEmpty()) {
            return;
        }
        try {
            double parsed = Double.parseDouble(value);
            if (parsed < min || parsed > max) {
                messages.add("Row " + rowIndex + ": " + field + " out of range (" + min + " to " + max + ")");
            }
        } catch (NumberFormatException e) {
            messages.add("Row " + rowIndex + ": " + field + " is not numeric");
        }
    }

    private void validateMin(Map<String, String> row,
                             int rowIndex,
                             String field,
                             double min,
                             List<String> messages) {
        String value = row.getOrDefault(field, "").trim();
        if (value.isEmpty()) {
            return;
        }
        try {
            double parsed = Double.parseDouble(value);
            if (parsed <= min) {
                messages.add("Row " + rowIndex + ": " + field + " must be > " + min);
            }
        } catch (NumberFormatException e) {
            messages.add("Row " + rowIndex + ": " + field + " is not numeric");
        }
    }

    private void validateBoolean(Map<String, String> row,
                                 int rowIndex,
                                 String field,
                                 List<String> messages) {
        String value = row.getOrDefault(field, "").trim().toLowerCase();
        if (value.isEmpty()) {
            return;
        }
        if (!value.equals("true") && !value.equals("false")) {
            messages.add("Row " + rowIndex + ": " + field + " must be true/false");
        }
    }

    private static class ExportResult {
        private final int rowCount;
        private final List<String> validationMessages;

        private ExportResult(int rowCount, List<String> validationMessages) {
            this.rowCount = rowCount;
            this.validationMessages = validationMessages;
        }
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

    private void applyInitialDirectory(FileChooser fileChooser) {
        if (cacheDir != null && Files.exists(cacheDir)) {
            fileChooser.setInitialDirectory(cacheDir.toFile());
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
                    String[] values = splitCsvLine(line);
                    previewRows.add(mapRow(values, previewHeaderIndex, previewTargetToSource));
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
                String[] values = splitCsvLine(line);
                if (values.length >= 2) {
                    mappings.add(new MappingRow(unquote(values[0]), unquote(values[1])));
                }
            }
        }
    }

    private void writeMappingToPath(Path path) throws IOException {
        StringBuilder output = new StringBuilder();
        output.append("sourceField,targetField").append(System.lineSeparator());
        for (MappingRow mapping : mappings) {
            output.append(escapeCsv(mapping.getSourceField()))
                    .append(",")
                    .append(escapeCsv(mapping.getTargetField()))
                    .append(System.lineSeparator());
        }
        Files.writeString(path, output.toString(), StandardCharsets.UTF_8);
    }

    private void ensureLocalCsvSource(WorkbenchSource source, Consumer<Path> onReady) {
        if (source.getType() == WorkbenchSourceType.LOCAL_CSV) {
            onReady.accept(Path.of(source.getLocation()));
            return;
        }
        if (source.getType() == WorkbenchSourceType.GAIA_TAP) {
            ensureCacheDir();
            String fileName = ensureCsvFileName(source.getName());
            Path outputPath = cacheDir.resolve(fileName);
            if (Files.exists(outputPath)) {
                addLocalSourceIfMissing(outputPath);
                onReady.accept(outputPath);
                return;
            }
            updateStatus("Downloading " + source.getName() + "...");
            downloadGaiaTapToFile(source.getLocation(), outputPath, () -> onReady.accept(outputPath));
            return;
        }
        if (source.getType() == WorkbenchSourceType.SIMBAD_TAP) {
            ensureCacheDir();
            String fileName = ensureCsvFileName(source.getName());
            Path outputPath = cacheDir.resolve(fileName);
            if (Files.exists(outputPath)) {
                addLocalSourceIfMissing(outputPath);
                onReady.accept(outputPath);
                return;
            }
            updateStatus("Downloading " + source.getName() + "...");
            downloadSimbadTapToFile(source.getLocation(), outputPath, () -> onReady.accept(outputPath));
            return;
        }
        if (source.getType() == WorkbenchSourceType.VIZIER_TAP) {
            ensureCacheDir();
            String fileName = ensureCsvFileName(source.getName());
            Path outputPath = cacheDir.resolve(fileName);
            if (Files.exists(outputPath)) {
                addLocalSourceIfMissing(outputPath);
                onReady.accept(outputPath);
                return;
            }
            updateStatus("Downloading " + source.getName() + "...");
            downloadVizierTapToFile(source.getLocation(), outputPath, () -> onReady.accept(outputPath));
            return;
        }
        ensureCacheDir();
        Path outputPath = cacheDir.resolve(ensureCsvFileName(source.getName()));
        if (Files.exists(outputPath)) {
            addLocalSourceIfMissing(outputPath);
            onReady.accept(outputPath);
            return;
        }
        updateStatus("Downloading " + source.getName() + "...");
        downloadToFile(source.getLocation(), outputPath, () -> onReady.accept(outputPath));
    }

    private void addLocalSourceIfMissing(Path outputPath) {
        Optional<WorkbenchSource> existing = sources.stream()
                .filter(source -> source.getType() == WorkbenchSourceType.LOCAL_CSV)
                .filter(source -> outputPath.toString().equals(source.getLocation()))
                .findFirst();
        WorkbenchSource source = existing.orElseGet(() -> {
            WorkbenchSource created = WorkbenchSource.localCsv(outputPath.getFileName().toString(), outputPath.toString());
            sources.add(created);
            return created;
        });
        sourceListView.getSelectionModel().select(source);
        sourceStatusLabel.setText("Added source: " + source.getName());
    }

    private String normalizeFieldName(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    private String formatElapsed(long startMillis) {
        if (startMillis <= 0L) {
            return "0s";
        }
        long elapsedSeconds = Math.max(0L, (System.currentTimeMillis() - startMillis) / 1000L);
        long minutes = elapsedSeconds / 60;
        long seconds = elapsedSeconds % 60;
        if (minutes > 0) {
            return minutes + "m " + seconds + "s";
        }
        return seconds + "s";
    }

    private String ensureCsvFileName(String name) {
        String safeName = name == null || name.isBlank() ? "gaia-query.csv" : name.trim();
        safeName = safeName.replaceAll("[\\\\/]+", "_");
        safeName = safeName.replaceAll("\\s+", "_");
        if (!safeName.toLowerCase().endsWith(".csv")) {
            safeName = safeName + ".csv";
        }
        return safeName;
    }

    private String defaultGaiaQuery(int limit) {
        return """
                SELECT TOP %d
                  source_id,
                  ra,
                  dec,
                  parallax,
                  pmra,
                  pmdec,
                  radial_velocity,
                  phot_g_mean_mag,
                  phot_bp_mean_mag,
                  phot_rp_mean_mag,
                  bp_rp
                FROM gaiadr3.gaia_source
                WHERE parallax > 33.3
                  AND phot_g_mean_mag < 15
                """.formatted(limit);
    }

    private String defaultSimbadQuery(int limit) {
        return """
                SELECT TOP %d
                  b.main_id,
                  b.ra,
                  b.dec,
                  b.pmra,
                  b.pmdec,
                  b.plx_value,
                  b.rvz_radvel,
                  b.sp_type,
                  b.otype,
                  MIN(i.id) AS alt_id
                FROM basic b
                LEFT OUTER JOIN ident i ON i.oidref = b.oid
                WHERE b.ra IS NOT NULL
                  AND b.dec IS NOT NULL
                  AND b.plx_value IS NOT NULL
                  AND b.pmra IS NOT NULL
                  AND b.pmdec IS NOT NULL
                  AND b.sp_type IS NOT NULL
                GROUP BY
                  b.main_id,
                  b.ra,
                  b.dec,
                  b.pmra,
                  b.pmdec,
                  b.plx_value,
                  b.rvz_radvel,
                  b.sp_type,
                  b.otype
                """.formatted(limit);
    }

    private String defaultVizierHipparcosQuery(int limit) {
        return """
                SELECT TOP %d *
                FROM "I/311/hip2"
                """.formatted(limit);
    }

    private String defaultVizierTycho2Query(int limit) {
        return """
                SELECT TOP %d *
                FROM "I/259/tyc2"
                """.formatted(limit);
    }

    private String defaultVizierRaveQuery(int limit) {
        return """
                SELECT TOP %d *
                FROM "III/279/rave_dr5"
                """.formatted(limit);
    }

    private String defaultVizierLamostQuery(int limit) {
        return """
                SELECT TOP %d *
                FROM "V/164/dr5"
                """.formatted(limit);
    }

    private String defaultVizierReconsQuery(int limit) {
        return """
                SELECT TOP %d *
                FROM "J/AJ/160/215/table2"
                """.formatted(limit);
    }

    private String vizierTableLookupQuery(String keyword, int limit) {
        String escaped = keyword.replace("'", "''");
        return """
                SELECT TOP %d
                  table_name,
                  description
                FROM TAP_SCHEMA.tables
                WHERE ivo_nocasematch(table_name, '%%%s%%') = 1
                   OR ivo_nocasematch(description, '%%%s%%') = 1
                """.formatted(limit, escaped, escaped);
    }

    private String sanitizeVizierTableId(String input) {
        String trimmed = input.trim();
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() > 1) {
            trimmed = trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed.trim();
    }

    private List<MappingRow> defaultGaiaMappings() {
        Map<String, String> sourceByNormalized = new HashMap<>();
        for (String field : sourceFields) {
            sourceByNormalized.put(normalizeFieldName(field), field);
        }
        List<MappingRow> rows = new ArrayList<>();
        addMappingIfPresent(rows, sourceByNormalized, "source_id", "catalogIdList");
        addMappingIfPresent(rows, sourceByNormalized, "source_id", "Gaia DR3");
        addMappingIfPresent(rows, sourceByNormalized, "ra", "ra");
        addMappingIfPresent(rows, sourceByNormalized, "dec", "declination");
        addMappingIfPresent(rows, sourceByNormalized, "pmra", "pmra");
        addMappingIfPresent(rows, sourceByNormalized, "pmdec", "pmdec");
        addMappingIfPresent(rows, sourceByNormalized, "radial_velocity", "radialVelocity");
        addMappingIfPresent(rows, sourceByNormalized, "phot_g_mean_mag", "magr");
        addMappingIfPresent(rows, sourceByNormalized, "phot_bp_mean_mag", "magb");
        addMappingIfPresent(rows, sourceByNormalized, "phot_rp_mean_mag", "magr");
        addMappingIfPresent(rows, sourceByNormalized, "bp_rp", "bprp");
        return rows;
    }

    private List<MappingRow> defaultSimbadMappings() {
        Map<String, String> sourceByNormalized = new HashMap<>();
        for (String field : sourceFields) {
            sourceByNormalized.put(normalizeFieldName(field), field);
        }
        List<MappingRow> rows = new ArrayList<>();
        addMappingIfPresent(rows, sourceByNormalized, "main_id", "displayName");
        addMappingIfPresent(rows, sourceByNormalized, "alt_id", "catalogIdList");
        addMappingIfPresent(rows, sourceByNormalized, "ra", "ra");
        addMappingIfPresent(rows, sourceByNormalized, "dec", "declination");
        addMappingIfPresent(rows, sourceByNormalized, "pmra", "pmra");
        addMappingIfPresent(rows, sourceByNormalized, "pmdec", "pmdec");
        addMappingIfPresent(rows, sourceByNormalized, "rvz_radvel", "radialVelocity");
        addMappingIfPresent(rows, sourceByNormalized, "sp_type", "spectralClass");
        return rows;
    }

    private List<MappingRow> defaultVizierMappings() {
        Map<String, String> sourceByNormalized = new HashMap<>();
        for (String field : sourceFields) {
            sourceByNormalized.put(normalizeFieldName(field), field);
        }
        List<MappingRow> rows = new ArrayList<>();
        addMappingIfPresent(rows, sourceByNormalized, "raicrs", "ra");
        addMappingIfPresent(rows, sourceByNormalized, "deicrs", "declination");
        addMappingIfPresent(rows, sourceByNormalized, "ramdeg", "ra");
        addMappingIfPresent(rows, sourceByNormalized, "demdeg", "declination");
        addMappingIfPresent(rows, sourceByNormalized, "pmra", "pmra");
        addMappingIfPresent(rows, sourceByNormalized, "pmde", "pmdec");
        addMappingIfPresent(rows, sourceByNormalized, "plx", "distance");
        addMappingIfPresent(rows, sourceByNormalized, "vmag", "magv");
        addMappingIfPresent(rows, sourceByNormalized, "vtmag", "magv");
        addMappingIfPresent(rows, sourceByNormalized, "btmag", "magb");
        addMappingIfPresent(rows, sourceByNormalized, "hip", "catalogIdList");
        addMappingIfPresent(rows, sourceByNormalized, "tyc", "catalogIdList");
        return rows;
    }

    private void addMappingIfPresent(List<MappingRow> rows,
                                     Map<String, String> sourceByNormalized,
                                     String sourceField,
                                     String targetField) {
        String source = sourceByNormalized.get(normalizeFieldName(sourceField));
        if (source != null) {
            rows.add(new MappingRow(source, targetField));
        }
    }

    private static class GaiaQuerySpec {
        private final String name;
        private final String adql;

        private GaiaQuerySpec(String name, String adql) {
            this.name = name;
            this.adql = adql;
        }
    }
}
