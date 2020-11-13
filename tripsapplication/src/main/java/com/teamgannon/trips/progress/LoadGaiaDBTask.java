package com.teamgannon.trips.progress;

import com.teamgannon.trips.dialogs.dataset.Dataset;
import com.teamgannon.trips.file.csvin.RBCsvFile;
import com.teamgannon.trips.service.DataImportService;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoadGaiaDBTask extends Task<Integer> {

    private int totalCount = 0;
    private DataImportService dataImportService;
    private Dataset dataset;


    public LoadGaiaDBTask(DataImportService dataImportService, Dataset dataset) {

        this.dataImportService = dataImportService;
        this.dataset = dataset;
    }

    @Override
    protected Integer call() throws Exception {
        updateMessage("start load of DB . . .");

        boolean success = dataImportService.processFileType(dataset);

        return totalCount;
    }

    public Integer getFinalCount() {
        return totalCount;
    }

    public RBCsvFile getFile() {
        return null;
    }
}
