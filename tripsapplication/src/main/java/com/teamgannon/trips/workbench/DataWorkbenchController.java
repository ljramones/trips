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
            "numExoplanets"
    );

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

    private final ApplicationEventPublisher eventPublisher;

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
    private static final long GAIA_TAP_MAX_WAIT_MS = 20 * 60 * 1000;
    private static final long GAIA_TAP_MAX_DELAY_MS = 60 * 1000;
    private static final long GAIA_TAP_INITIAL_DELAY_MS = 1000;
    private volatile boolean tapCancelRequested = false;
    private volatile String tapJobUrl;
    private volatile long tapStartMillis = 0L;
    private volatile String tapLabel = "TAP";

    public DataWorkbenchController(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @FXML
    public void initialize() {
        log.info("Data Workbench: initialize");
        updateStatus("Data Workbench ready");
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
                "SIMBAD TAP (TOP 1000)"
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
        WorkbenchSourceType type = WorkbenchSourceType.fromLabel(selection);
        if (type == WorkbenchSourceType.LOCAL_CSV) {
            addLocalCsvSource();
        } else if (type == WorkbenchSourceType.URL_CSV) {
            addUrlCsvSource();
        } else if (type == WorkbenchSourceType.GAIA_TAP) {
            addGaiaTapSource();
        } else if (type == WorkbenchSourceType.SIMBAD_TAP) {
            addSimbadTapSource();
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
        } else {
            showError("Download", "Only URL or TAP sources can be downloaded.");
        }
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
        mappingStatusLabel.setText("Mappings: " + mappings.size()
                + " (auto-added " + (added + gaiaAdded + simbadAdded) + ")");
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

    private String[] splitCsvLine(String line) {
        return line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
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

    private List<MappingRow> defaultGaiaMappings() {
        Map<String, String> sourceByNormalized = new HashMap<>();
        for (String field : sourceFields) {
            sourceByNormalized.put(normalizeFieldName(field), field);
        }
        List<MappingRow> rows = new ArrayList<>();
        addMappingIfPresent(rows, sourceByNormalized, "source_id", "catalogIdList");
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
