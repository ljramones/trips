package com.teamgannon.trips.service.importservices;

import com.teamgannon.trips.dialogs.dataset.Dataset;
import com.teamgannon.trips.dialogs.dataset.FileProcessResult;
import com.teamgannon.trips.dialogs.dataset.LoadUpdateListener;
import com.teamgannon.trips.dialogs.dataset.TaskComplete;
import com.teamgannon.trips.listener.DataSetChangeListener;
import com.teamgannon.trips.listener.StatusUpdaterListener;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.importservices.tasks.ExcelLoadTask;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import static javafx.concurrent.Worker.State.RUNNING;

@Slf4j
public class ExcelDataImportService extends Service<FileProcessResult> implements ImportTaskControl {

    private final DatabaseManagementService databaseManagementService;
    private Dataset dataset;
    private StatusUpdaterListener statusUpdaterListener;
    private DataSetChangeListener dataSetChangeListener;
    private TaskComplete taskComplete;
    private Label progressText;
    private ProgressBar loadProgressBar;
    private LoadUpdateListener loadUpdateListener;

    public ExcelDataImportService(DatabaseManagementService databaseManagementService) {
        this.databaseManagementService = databaseManagementService;
    }

    public boolean processDataSet(Dataset dataset,
                                  StatusUpdaterListener statusUpdaterListener,
                                  DataSetChangeListener dataSetChangeListener,
                                  TaskComplete taskComplete,
                                  @NotNull Label progressText,
                                  @NotNull ProgressBar loadProgressBar,
                                  @NotNull Button cancelLoad, LoadUpdateListener loadUpdateListener) {
        this.dataset = dataset;
        this.statusUpdaterListener = statusUpdaterListener;
        this.dataSetChangeListener = dataSetChangeListener;
        this.taskComplete = taskComplete;
        this.progressText = progressText;
        this.loadProgressBar = loadProgressBar;
        this.loadUpdateListener = loadUpdateListener;

        progressText.textProperty().bind(this.messageProperty());
        loadProgressBar.progressProperty().bind(this.progressProperty());
        cancelLoad.disableProperty().bind(this.stateProperty().isNotEqualTo(RUNNING));

        return true;
    }

    @Override
    protected @NotNull Task<FileProcessResult> createTask() {
        return new ExcelLoadTask(dataset, databaseManagementService);
    }


    @Override
    protected void succeeded() {
        log.info("dataset loaded");
        String message = String.format("new Dataset loaded -> %s", dataset.getName());
        statusUpdaterListener.updateStatus(message);
        unsetProgressControls();
        FileProcessResult fileProcessResult = this.getValue();
        taskComplete.complete(true, dataset, fileProcessResult, "loaded");
        dataSetChangeListener.addDataSet(fileProcessResult.getDataSetDescriptor());
        // set context to newly loaded dataset
        dataSetChangeListener.setContextDataSet(fileProcessResult.getDataSetDescriptor());
        loadUpdateListener.update(fileProcessResult.getDataSetDescriptor());
    }

    @Override
    protected void failed() {
        log.error("dataset load failed due to: " + getException().getMessage());
        statusUpdaterListener.updateStatus("dataset load failed due to: " + getException().getMessage());
        unsetProgressControls();
        FileProcessResult fileProcessResult = this.getValue();
        taskComplete.complete(false, dataset, fileProcessResult, "dataset load failed due to: " + getException().getMessage());
    }

    @Override
    protected void cancelled() {
        log.warn("dataset load cancelled");
        statusUpdaterListener.updateStatus("dataset load was cancelled for " + dataset.getName());
        unsetProgressControls();
        FileProcessResult fileProcessResult = this.getValue();
        taskComplete.complete(false, dataset, fileProcessResult, "dataset load cancelled");
    }

    private void unsetProgressControls() {
        progressText.textProperty().unbind();
        loadProgressBar.progressProperty().unbind();
        loadProgressBar.setProgress(1);
    }

    @Override
    public boolean cancelImport() {
        return this.cancel();
    }

    @Override
    public @NotNull String whoAmI() {
        return "Excel importer";
    }

    @Override
    public Dataset getCurrentDataSet() {
        return dataset;
    }


}
