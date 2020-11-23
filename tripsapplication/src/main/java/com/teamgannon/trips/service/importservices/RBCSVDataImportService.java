package com.teamgannon.trips.service.importservices;


import com.teamgannon.trips.dialogs.dataset.Dataset;
import com.teamgannon.trips.dialogs.dataset.FileProcessResult;
import com.teamgannon.trips.dialogs.dataset.TaskComplete;
import com.teamgannon.trips.listener.DataSetChangeListener;
import com.teamgannon.trips.listener.StatusUpdaterListener;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.importservices.tasks.RBCsvLoadTask;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class RBCSVDataImportService extends Service<FileProcessResult> implements ImportTaskControl {

    private DatabaseManagementService databaseManagementService;

    private StatusUpdaterListener statusUpdaterListener;
    private DataSetChangeListener dataSetChangeListener;
    private TaskComplete taskComplete;
    private Label progressText;

    private Dataset dataSet;
    private Dataset dataset;


    public RBCSVDataImportService(DatabaseManagementService databaseManagementService,
                                  StatusUpdaterListener statusUpdaterListener,
                                  DataSetChangeListener dataSetChangeListener,
                                  TaskComplete taskComplete,
                                  Label progressText) {

        this.databaseManagementService = databaseManagementService;
        this.statusUpdaterListener = statusUpdaterListener;
        this.dataSetChangeListener = dataSetChangeListener;

        this.taskComplete = taskComplete;
        this.progressText = progressText;
    }

    public RBCSVDataImportService(DatabaseManagementService databaseManagementService) {

        this.databaseManagementService = databaseManagementService;
    }

    public void setDataSet(Dataset dataSet) {
        this.dataSet = dataSet;
    }

    @Override
    protected Task<FileProcessResult> createTask() {
        return new RBCsvLoadTask(
                databaseManagementService,
                dataSet,
                statusUpdaterListener,
                dataSetChangeListener,
                taskComplete,
                progressText
        );
    }

    @Override
    protected void succeeded() {
        log.info("dataset loaded");
        FileProcessResult fileProcessResult = this.getValue();
        taskComplete.complete(true, dataset, fileProcessResult, "loaded");    }

    @Override
    protected void failed() {
        log.error("dataset load failed due to: " + getException().getMessage());
        FileProcessResult fileProcessResult = this.getValue();
        taskComplete.complete(false, dataset, fileProcessResult,"dataset load failed due to: " + getException().getMessage());
    }

    @Override
    protected void cancelled() {
        log.warn("dataset load cancelled");
        FileProcessResult fileProcessResult = this.getValue();
        taskComplete.complete(false, dataset, fileProcessResult,"dataset load cancelled");
    }

    public boolean processDataSet(Dataset dataset, StatusUpdaterListener statusUpdaterListener,
                                  DataSetChangeListener dataSetChangeListener,
                                  TaskComplete taskComplete, Label progressText) {
        this.dataset = dataset;
        this.taskComplete = taskComplete;
        this.progressText = progressText;
        this.reset();
        this.restart();
        return false;
    }

    @Override
    public boolean cancelImport() {
        return this.cancel();
    }

    @Override
    public String whoAmI() {
        return "RB CSV importer";
    }

    @Override
    public Dataset getCurrentDataSet() {
        return dataSet;
    }
}
