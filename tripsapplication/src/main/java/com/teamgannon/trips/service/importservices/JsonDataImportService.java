package com.teamgannon.trips.service.importservices;

import com.teamgannon.trips.dialogs.dataset.Dataset;
import com.teamgannon.trips.dialogs.dataset.FileProcessResult;
import com.teamgannon.trips.dialogs.dataset.TaskComplete;
import com.teamgannon.trips.listener.DataSetChangeListener;
import com.teamgannon.trips.listener.StatusUpdaterListener;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.importservices.tasks.JsonLoadTask;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonDataImportService extends Service<FileProcessResult> implements ImportTaskControl {


    private final DatabaseManagementService databaseManagementService;
    private Dataset dataset;
    private TaskComplete taskComplete;
    private Label progressText;

    public JsonDataImportService(DatabaseManagementService databaseManagementService) {
        this.databaseManagementService = databaseManagementService;
    }


    @Override
    protected void succeeded() {
        log.info("dataset loaded");
        FileProcessResult fileProcessResult = this.getValue();
        taskComplete.complete(true, dataset, fileProcessResult, "loaded");
    }

    @Override
    protected void failed() {
        log.error("dataset load failed due to: " + getException().getMessage());
        FileProcessResult fileProcessResult = this.getValue();
        taskComplete.complete(false, dataset, fileProcessResult, "dataset load failed due to: " + getException().getMessage());
    }

    @Override
    protected void cancelled() {
        log.warn("dataset load cancelled");
        FileProcessResult fileProcessResult = this.getValue();
        taskComplete.complete(false, dataset, fileProcessResult,"dataset load cancelled");
    }


    @Override
    protected Task<FileProcessResult> createTask() {
        return new JsonLoadTask(dataset, databaseManagementService, progressText);
    }

    public boolean processDataSet(Dataset dataset, StatusUpdaterListener statusUpdaterListener,
                                  DataSetChangeListener dataSetChangeListener,
                                  TaskComplete taskComplete, Label progressText) {
        this.dataset = dataset;
        this.taskComplete = taskComplete;
        this.progressText = progressText;
        this.reset();
        this.restart();
        return true;
    }

    @Override
    public boolean cancelImport() {
        return this.cancel();
    }

    @Override
    public String whoAmI() {
        return "JSON importer";
    }

    @Override
    public Dataset getCurrentDataSet() {
        return dataset;
    }
}
