package com.teamgannon.trips.service.importservices;

import com.teamgannon.trips.dialogs.dataset.Dataset;
import com.teamgannon.trips.dialogs.dataset.FileProcessResult;
import com.teamgannon.trips.dialogs.dataset.TaskComplete;
import com.teamgannon.trips.listener.DataSetChangeListener;
import com.teamgannon.trips.listener.StatusUpdaterListener;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.importservices.tasks.ChvLoadTask;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import lombok.extern.slf4j.Slf4j;

import static javafx.concurrent.Worker.State.RUNNING;

@Slf4j
public class CHVDataImportService extends Service<FileProcessResult> implements ImportTaskControl {

    private final DatabaseManagementService databaseManagementService;
    private Dataset dataset;
    private StatusUpdaterListener statusUpdaterListener;
    private DataSetChangeListener dataSetChangeListener;
    private TaskComplete taskComplete;
    private Label progressText;
    private ProgressBar loadProgressBar;


    public CHVDataImportService(DatabaseManagementService databaseManagementService) {
        this.databaseManagementService = databaseManagementService;
    }

    @Override
    protected Task<FileProcessResult> createTask() {
        return new ChvLoadTask(dataset, databaseManagementService);
    }

    public boolean processDataSet(Dataset dataset,
                                  StatusUpdaterListener statusUpdaterListener,
                                  DataSetChangeListener dataSetChangeListener,
                                  TaskComplete taskComplete,
                                  Label progressText,
                                  ProgressBar loadProgressBar,
                                  Button cancelLoad) {
        this.dataset = dataset;
        this.statusUpdaterListener = statusUpdaterListener;
        this.dataSetChangeListener = dataSetChangeListener;
        this.taskComplete = taskComplete;
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
        statusUpdaterListener.updateStatus(String.format("new Dataset loaded -> %s", dataset.getName()));
        unsetProgressControls();
        FileProcessResult fileProcessResult = this.getValue();
        taskComplete.complete(true, dataset, fileProcessResult, "loaded");
        dataSetChangeListener.addDataSet(fileProcessResult.getDataSetDescriptor());
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
        statusUpdaterListener.updateStatus("dataset laod was cancelled for " + dataset.getName());
        unsetProgressControls();
        FileProcessResult fileProcessResult = this.getValue();
        taskComplete.complete(false, dataset, fileProcessResult, "dataset load cancelled");
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
    public String whoAmI() {
        return "CHV importer service";
    }

    @Override
    public Dataset getCurrentDataSet() {
        return dataset;
    }
}
