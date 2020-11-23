package com.teamgannon.trips.service.importservices.tasks;

import com.teamgannon.trips.dialogs.dataset.Dataset;
import com.teamgannon.trips.dialogs.dataset.FileProcessResult;
import com.teamgannon.trips.dialogs.dataset.LoadUpdater;
import com.teamgannon.trips.dialogs.dataset.TaskComplete;
import com.teamgannon.trips.file.csvin.RBCsvFile;
import com.teamgannon.trips.file.csvin.RBCsvReader;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.listener.DataSetChangeListener;
import com.teamgannon.trips.listener.StatusUpdaterListener;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.importservices.ImportTaskControl;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;
import static com.teamgannon.trips.support.AlertFactory.showInfoMessage;

@Slf4j
public class RBCsvLoadTask extends Task<FileProcessResult> implements LoadUpdater {

    /**
     * the database management service
     */
    private final DatabaseManagementService databaseManagementService;

    private final RBCsvReader rbCsvReader;
    private StatusUpdaterListener statusUpdaterListener;
    private DataSetChangeListener dataSetChangeListener;
    private final TaskComplete taskComplete;
    private final Label progressText;


    /**
     * the dataset to load
     */
    private final Dataset dataSet;

    public RBCsvLoadTask(DatabaseManagementService databaseManagementService,
                         Dataset dataSet,
                         StatusUpdaterListener statusUpdaterListener,
                         DataSetChangeListener dataSetChangeListener,
                         TaskComplete taskComplete,
                         Label progressText) {

        this.databaseManagementService = databaseManagementService;
        this.dataSet = dataSet;

        // define readers
        this.rbCsvReader = new RBCsvReader(databaseManagementService);
        this.statusUpdaterListener = statusUpdaterListener;
        this.dataSetChangeListener = dataSetChangeListener;
        this.taskComplete = taskComplete;
        this.progressText = progressText;
    }

    @Override
    protected FileProcessResult call() throws Exception {
        // bind message property to gui message indicator
        progressText.textProperty().bind(this.messageProperty());

        FileProcessResult result = processRBCSVFile(this, dataSet);
        if (result.isSuccess()) {
            this.dataSetChangeListener.addDataSet(result.getDataSetDescriptor());
            this.statusUpdaterListener.updateStatus("CSV database: " + result.getDataSetDescriptor().getDataSetName() + " is loaded");
            log.info("New dataset {} added", dataSet.getName());
        } else {
            showErrorAlert("load csv", result.getMessage());
        }

        return null;
    }

    public FileProcessResult processRBCSVFile(LoadUpdater loadUpdater, Dataset dataset) {
        FileProcessResult processResult = new FileProcessResult();

        File file = new File(dataset.getFileSelected());
        RBCsvFile rbCsvFile = rbCsvReader.loadFile(loadUpdater, file, dataset);
        try {
            DataSetDescriptor dataSetDescriptor = databaseManagementService.loadRBCSVStarSet(rbCsvFile);
            String data = String.format("%s records loaded from dataset %s, Use plot to see data.",
                    dataSetDescriptor.getNumberStars(),
                    dataSetDescriptor.getDataSetName());
            showInfoMessage("Load CSV Format", data);
            processResult.setSuccess(true);
            processResult.setDataSetDescriptor(dataSetDescriptor);
        } catch (Exception e) {
            showErrorAlert("Duplicate Dataset", "This dataset was already loaded in the system ");
            processResult.setSuccess(false);
        }

        return processResult;
    }

    @Override
    public void updateLoad(String message) {
        updateMessage(message);
    }


}
