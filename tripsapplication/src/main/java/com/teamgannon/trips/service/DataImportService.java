package com.teamgannon.trips.service;

import com.teamgannon.trips.dialogs.dataset.Dataset;
import com.teamgannon.trips.dialogs.dataset.LoadUpdateListener;
import com.teamgannon.trips.dialogs.dataset.TaskComplete;
import com.teamgannon.trips.listener.DataSetChangeListener;
import com.teamgannon.trips.listener.StatusUpdaterListener;
import com.teamgannon.trips.service.importservices.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class DataImportService {

    private final @NotNull CHVDataImportService chvDataImportService;
    private final @NotNull JsonDataImportService jsonDataImportService;
    private final @NotNull CSVDataImportService csvDataImportService;
    private final @NotNull ExcelDataImportService excelDataImportService;

    private final AtomicBoolean currentlyRunning = new AtomicBoolean(false);

    private @Nullable ImportTaskControl runningImportService;


    public DataImportService(DatabaseManagementService databaseManagementService) {

        // importer services are pre-created
        chvDataImportService = new CHVDataImportService(databaseManagementService);
        jsonDataImportService = new JsonDataImportService(databaseManagementService);
        csvDataImportService = new CSVDataImportService(databaseManagementService);
        excelDataImportService = new ExcelDataImportService(databaseManagementService);
    }

    public ImportResult processFile(@NotNull Dataset dataset,
                                    StatusUpdaterListener statusUpdaterListener,
                                    DataSetChangeListener dataSetChangeListener,
                                    TaskComplete taskComplete,
                                    @NotNull Label progressText,
                                    @NotNull ProgressBar loadProgressBar,
                                    @NotNull Button cancelLoad,
                                    LoadUpdateListener loadUpdateListener) {

        if (currentlyRunning.get()) {
            if (runningImportService != null) {
                String currentDataSet = runningImportService.getCurrentDataSet().getName();
                log.error("There is a current import happening, please wait for {} to finish", currentDataSet);
                return ImportResult
                        .builder()
                        .success(false)
                        .message(String.format("There is a current import happening, please wait for %s to finish", currentDataSet))
                        .build();
            }
            currentlyRunning.set(false);
            return ImportResult
                    .builder()
                    .success(false)
                    .message("There is no current running import service")
                    .build();
        }

        switch (dataset.getDataType().getSuffix()) {

            case "chv" -> {
                currentlyRunning.set(true);
                runningImportService = chvDataImportService;
                boolean queued = chvDataImportService.processDataSet(
                        dataset, statusUpdaterListener, dataSetChangeListener,
                        taskComplete, progressText, loadProgressBar, cancelLoad, loadUpdateListener);
                if (!queued) {
                    log.error("failed to start import process");
                    currentlyRunning.set(false);
                    runningImportService = null;
                    chvDataImportService.reset();
                    return ImportResult.builder().success(false).message(String.format("failed to start the import for %s", dataset.getName())).build();
                }
                // start the work
                chvDataImportService.reset();
                chvDataImportService.restart();
            }

            case "trips.csv"-> {
                currentlyRunning.set(true);
                runningImportService = csvDataImportService;
                boolean queued = csvDataImportService.processDataSet(
                        dataset, statusUpdaterListener, dataSetChangeListener,
                        taskComplete, progressText, loadProgressBar, cancelLoad, loadUpdateListener);
                if (!queued) {
                    log.error("failed to start import process");
                    currentlyRunning.set(false);
                    runningImportService = null;
                    csvDataImportService.reset();
                    return ImportResult.builder().success(false).message(String.format("failed to start the import for %s", dataset.getName())).build();
                }
                // start the work
                csvDataImportService.reset();
                csvDataImportService.restart();
            }

            case "trips.xlsx" -> {
                currentlyRunning.set(true);
                runningImportService = excelDataImportService;
                boolean queued = excelDataImportService.processDataSet(
                        dataset, statusUpdaterListener, dataSetChangeListener,
                        taskComplete, progressText, loadProgressBar, cancelLoad, loadUpdateListener);
                if (!queued) {
                    log.error("failed to start import process");
                    currentlyRunning.set(false);
                    runningImportService = null;
                    excelDataImportService.reset();
                    return ImportResult.builder().success(false).message(String.format("failed to start the import for %s", dataset.getName())).build();
                }
                // start the work
                excelDataImportService.reset();
                excelDataImportService.restart();
            }

            case "json" -> {
                currentlyRunning.set(true);
                runningImportService = jsonDataImportService;
                boolean queued = jsonDataImportService.processDataSet(
                        dataset, statusUpdaterListener, dataSetChangeListener,
                        taskComplete, progressText, loadProgressBar, cancelLoad, loadUpdateListener);
                if (!queued) {
                    log.error("failed to start import process");
                    currentlyRunning.set(false);
                    runningImportService = null;
                    jsonDataImportService.reset();
                    return ImportResult.builder().success(false).message(String.format("failed to start the import for %s", dataset.getName())).build();
                }
                // start the work
                jsonDataImportService.reset();
                jsonDataImportService.restart();
            }

            default -> {
                log.error("Unsupported or unknown input format:{}", dataset.getDataType().getSuffix());
                return ImportResult.builder().success(false).message(String.format("Unsupported or unknown input format:%s", dataset.getDataType().getSuffix())).build();
            }
        }

        return ImportResult.builder().success(true).message(String.format("import process started for %s", dataset.getName())).build();
    }

    public void cancelCurrent() {
        if (currentlyRunning.get()) {
            if (runningImportService != null) {
                runningImportService.cancelImport();
                log.warn("import of current import was cancelled");
            }
        }
    }


    public void complete(boolean status, Dataset dataset, String errorMessage) {
        currentlyRunning.set(false);
        runningImportService = null;
    }

    public void loadDatabase() {

    }

}
