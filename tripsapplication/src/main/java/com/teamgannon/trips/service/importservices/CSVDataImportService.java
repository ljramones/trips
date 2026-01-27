package com.teamgannon.trips.service.importservices;

import com.teamgannon.trips.dialogs.dataset.model.Dataset;
import com.teamgannon.trips.dialogs.dataset.model.FileProcessResult;
import com.teamgannon.trips.dialogs.dataset.model.ImportTaskComplete;
import com.teamgannon.trips.events.AddDataSetEvent;
import com.teamgannon.trips.events.DataSetLoadEvent;
import com.teamgannon.trips.events.SetContextDataSetEvent;
import com.teamgannon.trips.events.StatusUpdateEvent;
import com.teamgannon.trips.service.BulkLoadService;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.StarService;
import com.teamgannon.trips.service.importservices.tasks.CSVLoadTask;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import static javafx.concurrent.Worker.State.RUNNING;

@Slf4j
@Component
public class CSVDataImportService extends Service<FileProcessResult> implements ImportTaskControl {


    private final DatabaseManagementService databaseManagementService;
    private final StarService starService;
    private final BulkLoadService bulkLoadService;
    private final ApplicationEventPublisher eventPublisher;
    private Dataset dataset;
    private ImportTaskComplete importTaskComplete;
    private Label progressText;
    private ProgressBar loadProgressBar;
    private Button cancelLoad;

    public CSVDataImportService(DatabaseManagementService databaseManagementService,
                                StarService starService,
                                BulkLoadService bulkLoadService,
                                ApplicationEventPublisher eventPublisher) {
        this.databaseManagementService = databaseManagementService;
        this.starService = starService;
        this.bulkLoadService = bulkLoadService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    protected @NotNull Task<FileProcessResult> createTask() {
        log.info("calling csv import task");
        return new CSVLoadTask(databaseManagementService, starService, bulkLoadService, dataset);
    }

    public boolean processDataSet(Dataset dataset,
                                  ImportTaskComplete importTaskComplete,
                                  @NotNull Label progressText,
                                  @NotNull ProgressBar loadProgressBar,
                                  @NotNull Button cancelLoad) {
        this.dataset = dataset;
        this.importTaskComplete = importTaskComplete;
        this.progressText = progressText;
        this.loadProgressBar = loadProgressBar;
        this.cancelLoad = cancelLoad;

        progressText.textProperty().bind(this.messageProperty());
        loadProgressBar.progressProperty().bind(this.progressProperty());
        cancelLoad.disableProperty().bind(this.stateProperty().isNotEqualTo(RUNNING));

        return true;
    }

    @Override
    protected void succeeded() {
        log.info("dataset loaded");
        String datasetName = dataset != null ? dataset.getName() : "unknown dataset";
        String message = "new Dataset loaded -> %s".formatted(datasetName);
        eventPublisher.publishEvent(new StatusUpdateEvent(this, message));
        unsetProgressControls();
        FileProcessResult fileProcessResult = this.getValue();
        if (fileProcessResult == null) {
            log.error("fileProcessResult is null after successful import.");
            if (importTaskComplete != null) {
                importTaskComplete.complete(false, dataset, null, "dataset load failed: no result returned");
            }
            return;
        }
        if (importTaskComplete != null) {
            importTaskComplete.complete(true, dataset, fileProcessResult, "loaded");
        }
        eventPublisher.publishEvent(new AddDataSetEvent(this, fileProcessResult.getDataSetDescriptor()));
        // set context to newly loaded dataset
        eventPublisher.publishEvent(new SetContextDataSetEvent(this, fileProcessResult.getDataSetDescriptor()));
        eventPublisher.publishEvent(new DataSetLoadEvent(this, fileProcessResult.getDataSetDescriptor()));
    }

    @Override
    protected void failed() {
        Throwable exception = getException();
        log.error("dataset load failed", exception);
        String message = exception != null ? exception.getMessage() : "unknown error";
        eventPublisher.publishEvent(new StatusUpdateEvent(this, "dataset load failed due to: " + message));
        unsetProgressControls();
        FileProcessResult fileProcessResult = this.getValue();
        if (importTaskComplete != null) {
            importTaskComplete.complete(false, dataset, fileProcessResult, "dataset load failed due to: " + message);
        }
    }

    @Override
    protected void cancelled() {
        log.warn("dataset load cancelled");
        String datasetName = dataset != null ? dataset.getName() : "unknown dataset";
        eventPublisher.publishEvent(new StatusUpdateEvent(this, "dataset load was cancelled for " + datasetName));
        unsetProgressControls();
        FileProcessResult fileProcessResult = this.getValue();
        if (importTaskComplete != null) {
            importTaskComplete.complete(false, dataset, fileProcessResult, "dataset load cancelled");
        }
    }

    private void unsetProgressControls() {
        if (progressText != null) {
            progressText.textProperty().unbind();
        }
        if (loadProgressBar != null) {
            loadProgressBar.progressProperty().unbind();
            loadProgressBar.setProgress(1);
        }
        if (cancelLoad != null) {
            cancelLoad.disableProperty().unbind();
        }
    }

    @Override
    public boolean cancelImport() {
        return this.cancel();
    }

    @Override
    public @NotNull String whoAmI() {
        return "CSV importer";
    }

    @Override
    public Dataset getCurrentDataSet() {
        return dataset;
    }

}
