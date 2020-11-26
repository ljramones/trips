package com.teamgannon.trips.service.importservices.tasks;

import com.teamgannon.trips.dialogs.dataset.Dataset;
import com.teamgannon.trips.dialogs.dataset.FileProcessResult;
import com.teamgannon.trips.service.DatabaseManagementService;
import javafx.concurrent.Task;
import javafx.scene.control.Label;

public class JsonLoadTask extends Task<FileProcessResult> implements ProgressUpdater{

    private final Dataset dataset;
    private final DatabaseManagementService databaseManagementService;

    public JsonLoadTask(Dataset dataset, DatabaseManagementService databaseManagementService) {
        this.dataset = dataset;
        this.databaseManagementService = databaseManagementService;
    }

    @Override
    protected FileProcessResult call() throws Exception {


        return null;
    }


    @Override
    public void updateLoadInfo(String message) {
        updateMessage(message);
    }
}
