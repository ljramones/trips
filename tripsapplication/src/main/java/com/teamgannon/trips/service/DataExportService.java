package com.teamgannon.trips.service;

import com.teamgannon.trips.dialogs.dataset.model.ExportOptions;
import com.teamgannon.trips.dialogs.dataset.model.ExportTaskComplete;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.listener.StatusUpdaterListener;
import com.teamgannon.trips.measure.TrackExecutionTime;
import com.teamgannon.trips.search.SearchContext;
import com.teamgannon.trips.service.export.*;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.FileChooser;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.teamgannon.trips.support.AlertFactory.showConfirmationAlert;
import static com.teamgannon.trips.support.AlertFactory.showInfoMessage;

/**
 * Used to import and export data sets
 * <p>
 * Created by larrymitchell on 2017-01-22.
 */
@Slf4j
public class DataExportService {

    private final DatabaseManagementService databaseManagementService;

    private final StarService starService;
    private final @NotNull CSVQueryExporterService csvQueryExporterService;
    private final @NotNull CSVDataSetDataExportService csvDataSetDataExportService;

    private final AtomicBoolean currentlyRunning = new AtomicBoolean(false);

    private @Nullable ExportTaskControl runningExportService;


    public DataExportService(DatabaseManagementService databaseManagementService,
                             StarService starService,
                             StatusUpdaterListener statusUpdaterListener) {

        this.databaseManagementService = databaseManagementService;
        this.starService = starService;

        csvQueryExporterService = new CSVQueryExporterService(statusUpdaterListener);
        csvDataSetDataExportService = new CSVDataSetDataExportService(statusUpdaterListener);
    }

    @TrackExecutionTime
    public void exportDB() {
        Optional<ButtonType> result = showConfirmationAlert(
                "Data Export Service", "Entire Database",
                "Do you want to export this database? It will take a while.");
        if ((result.isPresent()) && (result.get() == ButtonType.OK)) {
            final FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export entire database to export as a Excel file");
            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                log.info("\n\n\n\nThis will be replaced by the compact export option!!!\n\n\n\n");
            } else {
                log.warn("file export cancelled");
                showInfoMessage("Database export", "Export cancelled");
            }
        }
    }

    @TrackExecutionTime
    public ExportResult exportDataset(@NotNull ExportOptions exportOptions,
                                      StatusUpdaterListener statusUpdaterListener,
                                      ExportTaskComplete importTaskCompleteListener,
                                      Label progressText,
                                      ProgressBar exportProgressBar,
                                      Button cancelExport) {
        if (currentlyRunning.get()) {
            if (runningExportService != null) {
                log.error("There is a current import happening, please wait for {} to finish", runningExportService.whoAmI());
                return ExportResult
                        .builder()
                        .success(false)
                        .message(String.format("There is a current import happening, please wait for %s to finish", runningExportService.whoAmI()))
                        .build();

            }
        }
        // we keep the switch in case we want to add more varieties of export
        switch (exportOptions.getExportFormat()) {

            case CSV -> {
                currentlyRunning.set(true);
                runningExportService = csvDataSetDataExportService;
                boolean queued = csvDataSetDataExportService.exportAsCSV(
                        exportOptions, databaseManagementService,
                        starService,
                        statusUpdaterListener, importTaskCompleteListener, progressText,
                        exportProgressBar, cancelExport);
                if (!queued) {
                    log.error("failed to start import process");
                    currentlyRunning.set(false);
                    runningExportService = null;
                    csvDataSetDataExportService.reset();
                    return ExportResult
                            .builder()
                            .success(false)
                            .message(String.format("failed to start the export for %s", exportOptions.getDataset().getDataSetName()))
                            .build();

                }
                csvDataSetDataExportService.reset();
                csvDataSetDataExportService.restart();
                return ExportResult.builder().success(true).build();
            }

        }

        return ExportResult.builder().build();

    }


    /**
     * export a queried dataset based on options
     *
     * @param exportOptions the options
     * @param searchContext the search context to export
     */
    @TrackExecutionTime
    public ExportResult exportDatasetOnQuery(ExportOptions exportOptions,
                                             SearchContext searchContext,
                                             StatusUpdaterListener statusUpdaterListener,
                                             ExportTaskComplete importTaskCompleteListener,
                                             Label progressText,
                                             ProgressBar exportProgressBar,
                                             Button cancelExport) {

        if (currentlyRunning.get()) {
            if (runningExportService != null) {
                log.error("There is a current import happening, please wait for {} to finish", runningExportService.whoAmI());
                return ExportResult
                        .builder()
                        .success(false)
                        .message(String.format("There is a current import happening, please wait for %s to finish", runningExportService.whoAmI()))
                        .build();

            }
        }
        switch (exportOptions.getExportFormat()) {

            case CSV -> {
                currentlyRunning.set(true);
                runningExportService = csvQueryExporterService;
                boolean queued = csvQueryExporterService.exportAsCSV(exportOptions, searchContext,
                        databaseManagementService,
                        starService,
                        statusUpdaterListener,
                        importTaskCompleteListener,
                        progressText, exportProgressBar, cancelExport);
                if (!queued) {
                    log.error("failed to start import process");
                    currentlyRunning.set(false);
                    runningExportService = null;
                    csvQueryExporterService.reset();
                    return ExportResult
                            .builder()
                            .success(false)
                            .message(String.format("failed to start the export for %s", exportOptions.getDataset().getDataSetName()))
                            .build();

                }
                csvQueryExporterService.reset();
                csvQueryExporterService.restart();
                return ExportResult.builder().success(true).build();
            }
//            case JSON -> jsonExporter.exportAsJson(exportOptions, starObjects);
        }
        return ExportResult.builder().build();
    }

    public void cancelCurrent() {
        if (currentlyRunning.get()) {
            if (runningExportService != null) {
                boolean result = runningExportService.cancelExport();
                log.warn("import of current export was cancelled: {}", result);
            }
        }
    }

    public void complete(boolean status, DataSetDescriptor dataset, String errorMessage) {
        currentlyRunning.set(false);
        runningExportService = null;
    }

}
