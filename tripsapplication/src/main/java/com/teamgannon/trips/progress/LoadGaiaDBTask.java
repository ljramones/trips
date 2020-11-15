package com.teamgannon.trips.progress;

import com.teamgannon.trips.dialogs.dataset.Dataset;
import com.teamgannon.trips.dialogs.dataset.LoadUpdater;
import com.teamgannon.trips.dialogs.dataset.TaskComplete;
import com.teamgannon.trips.file.csvin.RBCsvFile;
import com.teamgannon.trips.service.DataImportService;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoadGaiaDBTask extends Task<Integer> implements LoadUpdater {

    private int totalCount = 0;
    private TaskComplete taskComplete;
    private DataImportService dataImportService;
    private Dataset dataset;


    public LoadGaiaDBTask(TaskComplete taskComplete, DataImportService dataImportService, Dataset dataset) {
        this.taskComplete = taskComplete;

        this.dataImportService = dataImportService;
        this.dataset = dataset;
    }

    @Override
    protected Integer call() throws Exception {
        updateMessage("start load of DB . . .");

        boolean success = dataImportService.processFileType(this, dataset);

        return totalCount;
    }

    public Integer getFinalCount() {
        return totalCount;
    }

    public RBCsvFile getFile() {
        return null;
    }

    public void start() {

    }

    /**
     * pass runningg task update message to update porcess
     * @param message the message
     */
    @Override
    public void updateLoad(String message) {
        updateMessage(message);
    }

    @Override
    public void loadComplete(boolean status, Dataset dataset, String errorMessage) {
        taskComplete.complete(status, dataset, errorMessage);
    }
}
