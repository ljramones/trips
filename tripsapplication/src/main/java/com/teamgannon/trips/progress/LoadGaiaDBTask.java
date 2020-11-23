package com.teamgannon.trips.progress;

import com.teamgannon.trips.dialogs.dataset.Dataset;
import com.teamgannon.trips.dialogs.dataset.LoadUpdater;
import com.teamgannon.trips.dialogs.dataset.TaskComplete;
import com.teamgannon.trips.file.csvin.RBCsvFile;
import com.teamgannon.trips.service.DataImportServiceOld;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoadGaiaDBTask extends Task<Integer> implements LoadUpdater {

    private int totalCount = 0;
    private TaskComplete taskComplete;
    private DataImportServiceOld dataImportServiceOld;
    private Dataset dataset;


    public LoadGaiaDBTask(TaskComplete taskComplete, DataImportServiceOld dataImportServiceOld, Dataset dataset) {
        this.taskComplete = taskComplete;

        this.dataImportServiceOld = dataImportServiceOld;
        this.dataset = dataset;
    }

    @Override
    protected Integer call() throws Exception {
        updateMessage("start load of DB . . .");

        boolean success = dataImportServiceOld.processFileType( dataset);

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

}
