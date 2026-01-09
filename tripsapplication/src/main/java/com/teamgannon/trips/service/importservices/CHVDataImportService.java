package com.teamgannon.trips.service.importservices;

import com.teamgannon.trips.dialogs.dataset.model.Dataset;
import com.teamgannon.trips.dialogs.dataset.model.FileProcessResult;
import com.teamgannon.trips.dialogs.dataset.model.ImportTaskComplete;
import com.teamgannon.trips.events.AddDataSetEvent;
import com.teamgannon.trips.events.DataSetLoadEvent;
import com.teamgannon.trips.events.SetContextDataSetEvent;
import com.teamgannon.trips.events.StatusUpdateEvent;
import com.teamgannon.trips.service.BulkLoadService;
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
@org.springframework.stereotype.Service
public class CHVDataImportService extends Service<FileProcessResult> implements ImportTaskControl {

    private final ApplicationEventPublisher eventPublisher;
    private final BulkLoadService bulkLoadService;
    private Dataset dataset;
    private ImportTaskComplete importTaskComplete;
    private Label progressText;
    private ProgressBar loadProgressBar;


    public CHVDataImportService(ApplicationEventPublisher eventPublisher,
                                BulkLoadService bulkLoadService) {
        this.eventPublisher = eventPublisher;
        this.bulkLoadService = bulkLoadService;
    }

    @Override
    protected @NotNull Task<FileProcessResult> createTask() {
        return new ChvLoadTask(dataset, bulkLoadService);
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
        eventPublisher.publishEvent(new AddDataSetEvent(this, fileProcessResult.getDataSetDescriptor()));
        // set context to newly loaded dataset
        eventPublisher.publishEvent(new SetContextDataSetEvent(this, fileProcessResult.getDataSetDescriptor()));
        eventPublisher.publishEvent(new DataSetLoadEvent(this, fileProcessResult.getDataSetDescriptor()));
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
