package com.teamgannon.trips.service.importservices.tasks;

import com.teamgannon.trips.dialogs.dataset.Dataset;
import com.teamgannon.trips.dialogs.dataset.FileProcessResult;
import com.teamgannon.trips.service.DatabaseManagementService;
import javafx.concurrent.Task;
import javafx.scene.control.Label;

public class JsonLoadTask extends Task<FileProcessResult> {

    private final Dataset dataset;
    private final DatabaseManagementService databaseManagementService;
    private final Label progressText;

    public JsonLoadTask(Dataset dataset, DatabaseManagementService databaseManagementService, Label progressText) {
        this.dataset = dataset;
        this.databaseManagementService = databaseManagementService;
        this.progressText = progressText;
    }

    @Override
    protected FileProcessResult call() throws Exception {
        // bind message property to gui message indicator
        progressText.textProperty().bind(this.messageProperty());

        return null;
    }


}
