package com.teamgannon.trips.service.importservices;

import com.teamgannon.trips.dialogs.dataset.Dataset;
import com.teamgannon.trips.dialogs.dataset.FileProcessResult;
import com.teamgannon.trips.dialogs.dataset.TaskComplete;
import com.teamgannon.trips.listener.DataSetChangeListener;
import com.teamgannon.trips.listener.StatusUpdaterListener;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.importservices.tasks.RBExcelLoadTask;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RBExcelDataImportService extends Service<FileProcessResult> implements ImportTaskControl {

    private DatabaseManagementService databaseManagementService;
    private Dataset dataset;
    private TaskComplete taskComplete;
    private Label progressText;

    public RBExcelDataImportService(DatabaseManagementService databaseManagementService) {
        this.databaseManagementService = databaseManagementService;
    }


    @Override
    protected void succeeded() {
        log.info("dataset loaded");

    }

    @Override
    protected void failed() {
        log.error("dataset load failed due to: " + getException().getMessage());
    }

    @Override
    protected void cancelled() {
        log.warn("dataset load cancelled");
    }

    @Override
    protected Task<FileProcessResult> createTask() {
        return new RBExcelLoadTask(dataset, databaseManagementService, progressText);
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
        return "RB Excel importer";
    }

    @Override
    public Dataset getCurrentDataSet() {
        return dataset;
    }
}
