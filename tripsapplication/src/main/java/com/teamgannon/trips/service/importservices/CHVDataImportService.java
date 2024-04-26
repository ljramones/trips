package com.teamgannon.trips.service.importservices;

import com.teamgannon.trips.dialogs.dataset.model.Dataset;
import com.teamgannon.trips.dialogs.dataset.model.FileProcessResult;
import com.teamgannon.trips.dialogs.dataset.model.ImportTaskComplete;
import com.teamgannon.trips.dialogs.dataset.model.LoadUpdateListener;
import com.teamgannon.trips.events.StatusUpdateEvent;
import com.teamgannon.trips.listener.DataSetChangeListener;
import com.teamgannon.trips.service.BulkLoadService;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.importservices.tasks.ChvLoadTask;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;

import static javafx.concurrent.Worker.State.RUNNING;

@Slf4j
public class CHVDataImportService extends Service<FileProcessResult> implements ImportTaskControl {

    private final DatabaseManagementService databaseManagementService;
    private final BulkLoadService bulkLoadService;
    private Dataset dataset;
    private ApplicationEventPublisher eventPublisher;
    private DataSetChangeListener dataSetChangeListener;
    private ImportTaskComplete importTaskComplete;
    private Label progressText;
    private ProgressBar loadProgressBar;
    private LoadUpdateListener loadUpdateListener;


    public CHVDataImportService(DatabaseManagementService databaseManagementService,
                                BulkLoadService bulkLoadService) {
        this.databaseManagementService = databaseManagementService;

        this.bulkLoadService = bulkLoadService;
    }

    @Override
    protected @NotNull Task<FileProcessResult> createTask() {
        return new ChvLoadTask(dataset, databaseManagementService, bulkLoadService);
    }

    public boolean processDataSet(Dataset dataset,
                                  ApplicationEventPublisher eventPublisher,
                                  DataSetChangeListener dataSetChangeListener,
                                  ImportTaskComplete importTaskComplete,
                                  @NotNull Label progressText,
                                  @NotNull ProgressBar loadProgressBar,
                                  @NotNull Button cancelLoad,
                                  LoadUpdateListener loadUpdateListener) {
        this.dataset = dataset;
        this.eventPublisher = eventPublisher;
        this.dataSetChangeListener = dataSetChangeListener;
        this.importTaskComplete = importTaskComplete;
        this.progressText = progressText;
        this.loadProgressBar = loadProgressBar;
        this.loadUpdateListener = loadUpdateListener;

        progressText.textProperty().bind(this.messageProperty());
        loadProgressBar.progressProperty().bind(this.progressProperty());
        cancelLoad.disableProperty().bind(this.stateProperty().isNotEqualTo(RUNNING));

        return true;
    }


    @Override
    protected void succeeded() {
        log.info("dataset loaded");
        eventPublisher.publishEvent(new StatusUpdateEvent(this, String.format("new Dataset loaded -> %s", dataset.getName())));
        unsetProgressControls();
        FileProcessResult fileProcessResult = this.getValue();
        importTaskComplete.complete(true, dataset, fileProcessResult, "loaded");
        dataSetChangeListener.addDataSet(fileProcessResult.getDataSetDescriptor());
        // set context to newly loaded dataset
        dataSetChangeListener.setContextDataSet(fileProcessResult.getDataSetDescriptor());
        loadUpdateListener.update(fileProcessResult.getDataSetDescriptor());
    }

    @Override
    protected void failed() {
        log.error("dataset load failed due to: " + getException().getMessage());
        eventPublisher.publishEvent(new StatusUpdateEvent(this, "dataset load failed due to: " + getException().getMessage()));
        unsetProgressControls();
        FileProcessResult fileProcessResult = this.getValue();
        importTaskComplete.complete(false, dataset, fileProcessResult, "dataset load failed due to: " + getException().getMessage());
    }

    @Override
    protected void cancelled() {
        log.warn("dataset load cancelled");
        eventPublisher.publishEvent(new StatusUpdateEvent(this, "dataset laod was cancelled for " + dataset.getName()));
        unsetProgressControls();
        FileProcessResult fileProcessResult = this.getValue();
        importTaskComplete.complete(false, dataset, fileProcessResult, "dataset load cancelled");
    }

    private void unsetProgressControls() {
        progressText.textProperty().unbind();
        loadProgressBar.progressProperty().unbind();
    }

    @Override
    public boolean cancelImport() {
        return this.cancel();
    }

    @Override
    public @NotNull String whoAmI() {
        return "CHV importer service";
    }

    @Override
    public Dataset getCurrentDataSet() {
        return dataset;
    }
}
