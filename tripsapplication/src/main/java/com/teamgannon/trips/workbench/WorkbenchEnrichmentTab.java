package com.teamgannon.trips.workbench;

import com.teamgannon.trips.service.DatasetService;
import com.teamgannon.trips.workbench.service.WorkbenchCsvService;
import com.teamgannon.trips.workbench.service.WorkbenchEnrichmentService;
import com.teamgannon.trips.workbench.service.WorkbenchTapService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Owns the Data Workbench enrichment actions: offline CSV distance enrichment,
 * live TAP enrichment, photometric estimation, Gaia stellar-parameter fills,
 * temperature/spectral estimation, and TAP cancellation.
 * <p>
 * The controller keeps the {@code @FXML} fields and forwards button handlers
 * here. This mirrors the context-object pattern used by
 * {@link WorkbenchExoplanetTab} while keeping source/mapping/preview behavior
 * in the controller and existing helpers.
 */
@Slf4j
public class WorkbenchEnrichmentTab {

    public record Bindings(
            TextField liveTapBatchField,
            TextField liveTapBackoffField,
            ProgressBar progressBar) {
    }

    private final DatasetService datasetService;
    private final WorkbenchEnrichmentService enrichmentService;
    private final WorkbenchCsvService csvService;
    private final WorkbenchTapService tapService;
    private final WorkbenchSourceActions sourceActions;
    private final Consumer<String> statusConsumer;
    private final BiConsumer<String, String> showError;
    private final Supplier<Window> windowSupplier;

    private Bindings bindings;

    public WorkbenchEnrichmentTab(DatasetService datasetService,
                                  WorkbenchEnrichmentService enrichmentService,
                                  WorkbenchCsvService csvService,
                                  WorkbenchTapService tapService,
                                  WorkbenchSourceActions sourceActions,
                                  Consumer<String> statusConsumer,
                                  BiConsumer<String, String> showError,
                                  Supplier<Window> windowSupplier) {
        this.datasetService = datasetService;
        this.enrichmentService = enrichmentService;
        this.csvService = csvService;
        this.tapService = tapService;
        this.sourceActions = sourceActions;
        this.statusConsumer = statusConsumer;
        this.showError = showError;
        this.windowSupplier = windowSupplier;
    }

    public void bind(Bindings bindings) {
        this.bindings = bindings;
        if (bindings.progressBar != null) {
            bindings.progressBar.setVisible(false);
        }
        if (bindings.liveTapBatchField != null) {
            bindings.liveTapBatchField.setText("50");
        }
        if (bindings.liveTapBackoffField != null) {
            bindings.liveTapBackoffField.setText("1000");
        }
    }

    public void onEnrichDistances() {
        FileChooser baseChooser = csvChooser("Select TRIPS CSV to enrich");
        File baseFile = baseChooser.showOpenDialog(windowSupplier.get());
        if (baseFile == null) {
            return;
        }

        FileChooser gaiaChooser = csvChooser("Select Gaia DR3 CSV (optional)");
        File gaiaFile = gaiaChooser.showOpenDialog(windowSupplier.get());

        FileChooser hipChooser = csvChooser("Select Hipparcos CSV (optional)");
        File hipFile = hipChooser.showOpenDialog(windowSupplier.get());

        if (gaiaFile == null && hipFile == null) {
            showError.accept("Enrich Distances", "Select at least a Gaia or Hipparcos CSV file.");
            return;
        }

        FileChooser outputChooser = csvChooser("Save enriched TRIPS CSV");
        outputChooser.setInitialFileName(baseFile.getName().replace(".csv", "") + "-enriched.csv");
        File outputFile = outputChooser.showSaveDialog(windowSupplier.get());
        if (outputFile == null) {
            return;
        }

        updateStatus("Enriching distances...");
        setProgressVisible(true);
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
            setProgressVisible(false);
            updateStatus("Enriched CSV saved: " + outputFile.getName());
            sourceActions.addLocalSourceIfMissing(outputFile.toPath());
        });
        task.setOnFailed(event -> {
            setProgressVisible(false);
            showError.accept("Enrich Distances", String.valueOf(task.getException().getMessage()));
        });
        startDaemon(task);
    }

    public void onEnrichMissingDistancesLive() {
        TapSettings settings = readTapSettings("Enrich Distances", "Live TAP batch size", "Live TAP backoff");
        if (settings == null) {
            return;
        }
        Optional<String> selection = chooseDataset(
                "Enrich Distances",
                "Enrich Missing Distances",
                "Select dataset to enrich");
        if (selection.isEmpty()) {
            return;
        }

        String dataSetName = selection.get();
        updateStatus("Enriching missing distances (live TAP)...");
        setProgressVisible(true);
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                enrichmentService.enrichMissingDistancesLive(dataSetName, settings.batchSize(), settings.backoffMs(), WorkbenchEnrichmentTab.this::updateStatus);
                return null;
            }
        };
        runEnrichmentTask(task, "Enrich Distances", "Live TAP enrichment complete.");
    }

    public void onPhotometricEstimation() {
        Optional<String> selection = chooseDataset(
                "Photometric Estimation",
                "Photometric Distance Estimation",
                "Estimate distances for orphan stars using magnitude/color.\nThis runs AFTER TAP enrichment for stars that couldn't be matched.");
        if (selection.isEmpty()) {
            return;
        }

        String dataSetName = selection.get();
        updateStatus("Estimating distances photometrically...");
        setProgressVisible(true);
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                enrichmentService.enrichOrphanDistancesPhotometric(dataSetName, WorkbenchEnrichmentTab.this::updateStatus);
                return null;
            }
        };
        runEnrichmentTask(task, "Photometric Estimation", "Photometric estimation complete.");
    }

    public void onEnrichMasses() {
        TapSettings settings = readTapSettings("Mass Enrichment", "Live TAP batch size", "Live TAP backoff");
        if (settings == null) {
            return;
        }
        Optional<String> selection = chooseDataset(
                "Mass Enrichment",
                "Enrich Stellar Parameters from Gaia DR3",
                "Look up stellar parameters from Gaia DR3 astrophysical_parameters.\nFetches: mass, radius, luminosity, temperature, metallicity\nOnly fills in values that are currently missing.");
        if (selection.isEmpty()) {
            return;
        }

        String dataSetName = selection.get();
        updateStatus("Enriching stellar parameters from Gaia DR3...");
        setProgressVisible(true);
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                enrichmentService.enrichMissingMassesFromGaia(dataSetName, settings.batchSize(), settings.backoffMs(),
                        WorkbenchEnrichmentTab.this::updateStatus);
                return null;
            }
        };
        runEnrichmentTask(task, "Stellar Parameters Enrichment", "Stellar parameters enrichment complete.");
    }

    public void onEstimateMassPhotometric() {
        Optional<String> selection = chooseDataset(
                "Mass Estimation",
                "Estimate Mass (Photometric)",
                "Estimate stellar mass from luminosity using the mass-luminosity relation.\nRequires distance and apparent magnitude data.\nAlso estimates radius and luminosity.");
        if (selection.isEmpty()) {
            return;
        }

        String dataSetName = selection.get();
        updateStatus("Estimating masses photometrically...");
        setProgressVisible(true);
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                enrichmentService.enrichMassPhotometric(dataSetName, WorkbenchEnrichmentTab.this::updateStatus);
                return null;
            }
        };
        runEnrichmentTask(task, "Mass Estimation", "Photometric mass estimation complete.");
    }

    public void onCancelTap() {
        tapService.cancelCurrentJob(this::updateStatus, message -> showError.accept("Cancel TAP", message));
    }

    public void onEstimateTemperature() {
        Optional<String> selection = chooseDataset(
                "Temperature Estimation",
                "Estimate Temperature from BP-RP",
                "Estimate stellar temperature from Gaia BP-RP color.\nRequires BP-RP color data.");
        if (selection.isEmpty()) {
            return;
        }

        String dataSetName = selection.get();
        updateStatus("Estimating temperatures from BP-RP...");
        setProgressVisible(true);
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                enrichmentService.enrichTemperatureFromBprp(dataSetName, WorkbenchEnrichmentTab.this::updateStatus);
                return null;
            }
        };
        runEnrichmentTask(task, "Temperature Estimation", "Temperature estimation complete.");
    }

    public void onEstimateSpectral() {
        Optional<String> selection = chooseDataset(
                "Spectral Estimation",
                "Estimate Spectral Class from BP-RP",
                "Estimate spectral classification from Gaia BP-RP color.\nAssumes main-sequence (luminosity class V).");
        if (selection.isEmpty()) {
            return;
        }

        String dataSetName = selection.get();
        updateStatus("Estimating spectral classes from BP-RP...");
        setProgressVisible(true);
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                enrichmentService.enrichSpectralFromBprp(dataSetName, WorkbenchEnrichmentTab.this::updateStatus);
                return null;
            }
        };
        runEnrichmentTask(task, "Spectral Estimation", "Spectral classification complete.");
    }

    public void onCrossFillTempSpectral() {
        Optional<String> selection = chooseDataset(
                "Cross-Fill",
                "Cross-Fill Temperature & Spectral",
                """
                        Cross-fill missing data:
                        - Estimate temperature from spectral class
                        - Estimate spectral class from temperature\
                        """);
        if (selection.isEmpty()) {
            return;
        }

        String dataSetName = selection.get();
        updateStatus("Cross-filling temperature and spectral...");
        setProgressVisible(true);
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                enrichmentService.crossFillTemperatureFromSpectral(dataSetName, WorkbenchEnrichmentTab.this::updateStatus);
                enrichmentService.crossFillSpectralFromTemperature(dataSetName, WorkbenchEnrichmentTab.this::updateStatus);
                return null;
            }
        };
        runEnrichmentTask(task, "Cross-Fill", "Cross-fill complete.");
    }

    private FileChooser csvChooser(String title) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));
        sourceActions.applyInitialDirectory(chooser);
        return chooser;
    }

    private Optional<String> chooseDataset(String errorTitle, String dialogTitle, String headerText) {
        List<String> datasetNames = datasetService.getDescriptors().stream()
                .map(descriptor -> descriptor.getDataSetName())
                .sorted()
                .collect(Collectors.toList());
        if (datasetNames.isEmpty()) {
            showError.accept(errorTitle, "No datasets available.");
            return Optional.empty();
        }
        ChoiceDialog<String> dialog = new ChoiceDialog<>(datasetNames.get(0), datasetNames);
        dialog.setTitle(dialogTitle);
        dialog.setHeaderText(headerText);
        dialog.setContentText("Dataset:");
        return dialog.showAndWait();
    }

    private TapSettings readTapSettings(String errorTitle, String batchLabel, String backoffLabel) {
        int batchSize = 50;
        int backoffMs = 1000;
        if (bindings.liveTapBatchField != null && !bindings.liveTapBatchField.getText().isBlank()) {
            Integer parsed = parseIntStrict(bindings.liveTapBatchField.getText(), errorTitle, batchLabel);
            if (parsed == null || parsed <= 0) {
                showError.accept(errorTitle, batchLabel + " must be a positive integer.");
                return null;
            }
            batchSize = parsed;
        }
        if (bindings.liveTapBackoffField != null && !bindings.liveTapBackoffField.getText().isBlank()) {
            Integer parsed = parseIntStrict(bindings.liveTapBackoffField.getText(), errorTitle, backoffLabel);
            if (parsed == null || parsed < 0) {
                showError.accept(errorTitle, backoffLabel + " must be 0 or greater.");
                return null;
            }
            backoffMs = parsed;
        }
        return new TapSettings(batchSize, backoffMs);
    }

    private Integer parseIntStrict(String value, String errorTitle, String label) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            showError.accept(errorTitle, label + " must be a valid integer.");
            return null;
        }
    }

    private void runEnrichmentTask(Task<Void> task, String errorTitle, String successMessage) {
        task.setOnSucceeded(event -> {
            setProgressVisible(false);
            updateStatus(successMessage);
        });
        task.setOnFailed(event -> {
            setProgressVisible(false);
            showError.accept(errorTitle, String.valueOf(task.getException().getMessage()));
        });
        startDaemon(task);
    }

    private void updateStatus(String message) {
        statusConsumer.accept(message);
    }

    private void setProgressVisible(boolean visible) {
        runOnFxThread(() -> {
            if (bindings.progressBar != null) {
                bindings.progressBar.setVisible(visible);
                if (visible) {
                    bindings.progressBar.setProgress(-1);
                }
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

    private record TapSettings(int batchSize, int backoffMs) {
    }
}
