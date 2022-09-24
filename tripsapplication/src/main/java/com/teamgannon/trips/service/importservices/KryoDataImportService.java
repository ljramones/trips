package com.teamgannon.trips.service.importservices;

import com.teamgannon.trips.dialogs.dataset.model.Dataset;
import com.teamgannon.trips.dialogs.dataset.model.FileProcessResult;
import com.teamgannon.trips.dialogs.dataset.model.ImportTaskComplete;
import com.teamgannon.trips.dialogs.dataset.model.LoadUpdateListener;
import com.teamgannon.trips.listener.DataSetChangeListener;
import com.teamgannon.trips.listener.StatusUpdaterListener;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.importservices.tasks.CSVLoadTask;
import com.teamgannon.trips.service.importservices.tasks.KryoLoadTask;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import static javafx.concurrent.Worker.State.RUNNING;

@Slf4j
public class KryoDataImportService extends Service<FileProcessResult> implements ImportTaskControl {

    private final DatabaseManagementService databaseManagementService;
    private Dataset dataset;
    private StatusUpdaterListener statusUpdaterListener;
    private DataSetChangeListener dataSetChangeListener;
    private ImportTaskComplete importTaskComplete;
    private Label progressText;
    private ProgressBar loadProgressBar;
    private LoadUpdateListener loadUpdateListener;

    public KryoDataImportService(DatabaseManagementService databaseManagementService) {
        this.databaseManagementService = databaseManagementService;
    }


    public boolean processDataSet(Dataset dataset,
                                  StatusUpdaterListener statusUpdaterListener,
                                  DataSetChangeListener dataSetChangeListener,
                                  ImportTaskComplete importTaskComplete,
                                  @NotNull Label progressText,
                                  @NotNull ProgressBar loadProgressBar,
                                  @NotNull Button cancelLoad, LoadUpdateListener loadUpdateListener) {
        this.dataset = dataset;
        this.statusUpdaterListener = statusUpdaterListener;
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
    public boolean cancelImport() {
        return this.cancel();
    }

    @Override
    public String whoAmI() {
        return "Compact importer";
    }


    @Override
    public Dataset getCurrentDataSet() {
        return dataset;
    }

    @Override
    protected Task<FileProcessResult> createTask() {
        log.info("calling csv import task");
        return new KryoLoadTask(databaseManagementService, dataset);
    }

    @Override
    protected void succeeded() {
        log.info("dataset loaded");
        String message = String.format("new Dataset loaded -> %s", dataset.getName());
        statusUpdaterListener.updateStatus(message);
        unsetProgressControls();
        FileProcessResult fileProcessResult = this.getValue();
        if (fileProcessResult == null) {
            log.error("why is fileProcessResult null.");
        }
        importTaskComplete.complete(true, dataset, fileProcessResult, "loaded");
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
        importTaskComplete.complete(false, dataset, fileProcessResult, "dataset load failed due to: " + getException().getMessage());
    }

    @Override
    protected void cancelled() {
        log.warn("dataset load cancelled");
        statusUpdaterListener.updateStatus("dataset load was cancelled for " + dataset.getName());
        unsetProgressControls();
        FileProcessResult fileProcessResult = this.getValue();
        importTaskComplete.complete(false, dataset, fileProcessResult, "dataset load cancelled");
    }

    private void unsetProgressControls() {
        progressText.textProperty().unbind();
        loadProgressBar.progressProperty().unbind();
        loadProgressBar.setProgress(1);
    }
}
