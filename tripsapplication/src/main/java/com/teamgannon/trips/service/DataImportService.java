package com.teamgannon.trips.service;

import com.teamgannon.trips.dialogs.dataset.model.Dataset;
import com.teamgannon.trips.dialogs.dataset.model.ImportTaskComplete;
import com.teamgannon.trips.measure.TrackExecutionTime;
import com.teamgannon.trips.service.importservices.CHVDataImportService;
import com.teamgannon.trips.service.importservices.CSVDataImportService;
import com.teamgannon.trips.service.importservices.ImportResult;
import com.teamgannon.trips.service.importservices.ImportTaskControl;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class DataImportService {

    private final @NotNull CHVDataImportService chvDataImportService;
    private final BulkLoadService bulkLoadService;
    private final @NotNull CSVDataImportService csvDataImportService;


    private final AtomicBoolean currentlyRunning = new AtomicBoolean(false);
    private final ApplicationEventPublisher eventPublisher;

    private @Nullable ImportTaskControl runningImportService;


    public DataImportService(BulkLoadService bulkLoadService,
                             CSVDataImportService csvDataImportService,
                             CHVDataImportService chvDataImportService,
                             ApplicationEventPublisher eventPublisher) {
        this.chvDataImportService = chvDataImportService;
        this.eventPublisher = eventPublisher;

        // importer services are pre-created
        this.bulkLoadService = bulkLoadService;
        this.csvDataImportService = csvDataImportService;
    }

    @TrackExecutionTime
    public ImportResult processFile(@NotNull Dataset dataset,
                                    ImportTaskComplete importTaskComplete,
                                    @NotNull Label progressText,
                                    @NotNull ProgressBar importProgressBar,
                                    @NotNull Button cancelLoad) {

        if (currentlyRunning.get()) {
            if (runningImportService != null) {
                String currentDataSet = runningImportService.getCurrentDataSet().getName();
                log.error("There is a current import happening, please wait for {} to finish", currentDataSet);
                return ImportResult
                        .builder()
                        .success(false)
                        .message("There is a current import happening, please wait for %s to finish".formatted(currentDataSet))
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
                        dataset,
                        importTaskComplete,
                        progressText,
                        importProgressBar,
                        cancelLoad);
                if (!queued) {
                    log.error("failed to start import process");
                    currentlyRunning.set(false);
                    runningImportService = null;
                    chvDataImportService.reset();
                    return ImportResult.builder().success(false).message("failed to start the import for %s".formatted(dataset.getName())).build();
                }
                // start the work
                chvDataImportService.reset();
                chvDataImportService.restart();
            }

            case "trips.csv" -> {
                currentlyRunning.set(true);
                runningImportService = csvDataImportService;
                boolean queued = csvDataImportService.processDataSet(
                        dataset,
                        importTaskComplete, progressText, importProgressBar, cancelLoad);
                if (!queued) {
                    log.error("failed to start import process");
                    currentlyRunning.set(false);
                    runningImportService = null;
                    csvDataImportService.reset();
                    return ImportResult.builder().success(false).message("failed to start the import for %s".formatted(dataset.getName())).build();
                }
                // start the work
                csvDataImportService.reset();
                csvDataImportService.restart();
            }

            default -> {
                log.error("Unsupported or unknown input format:{}", dataset.getDataType().getSuffix());
                return ImportResult.builder().success(false).message(String.format("Unsupported or unknown input format:%s", dataset.getDataType().getSuffix())).build();
            }
        }

        return ImportResult.builder().success(true).message("import process started for %s".formatted(dataset.getName())).build();
    }

    public void cancelCurrent() {
        if (currentlyRunning.get()) {
            if (runningImportService != null) {
                boolean result = runningImportService.cancelImport();
                log.warn("import of current import was cancelled: {}", result);
            }
        }
    }


    public void complete(boolean status, Dataset dataset, String errorMessage) {
        currentlyRunning.set(false);
        runningImportService = null;
    }

}
