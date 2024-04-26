package com.teamgannon.trips.service.export;

import com.teamgannon.trips.dialogs.dataset.model.Dataset;
import com.teamgannon.trips.dialogs.dataset.model.FileProcessResult;
import com.teamgannon.trips.dialogs.dataset.model.ImportTaskComplete;
import com.teamgannon.trips.dialogs.dataset.model.LoadUpdateListener;
import com.teamgannon.trips.listener.DataSetChangeListener;
import com.teamgannon.trips.service.DatabaseManagementService;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;

import static javafx.concurrent.Worker.State.RUNNING;

/**
 * currently not used, but it might be a technique I want to use later and that is why this code is here.
 * it was complicated to figure out so that is why I didn't want to lose it.
 */
public class ExportExportService extends Service<FileProcessResult> {

    private final DatabaseManagementService databaseManagementService;

    private Dataset dataset;
    private DataSetChangeListener dataSetChangeListener;
    private ImportTaskComplete importTaskComplete;
    private Label progressText;
    private ProgressBar loadProgressBar;
    private LoadUpdateListener loadUpdateListener;

    public ExportExportService(DatabaseManagementService databaseManagementService) {
        this.databaseManagementService = databaseManagementService;
    }

    public boolean processDataSet(Dataset dataset,
                                  ApplicationEventPublisher eventPublisher,
                                  DataSetChangeListener dataSetChangeListener,
                                  ImportTaskComplete importTaskComplete,
                                  @NotNull Label progressText,
                                  @NotNull ProgressBar loadProgressBar,
                                  @NotNull Button cancelLoad, LoadUpdateListener loadUpdateListener) {
        this.dataset = dataset;
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
    protected Task<FileProcessResult> createTask() {
        return null;
    }

    @Override
    protected void succeeded() {

    }

    @Override
    protected void failed() {

    }

    @Override
    protected void cancelled() {

    }

}
