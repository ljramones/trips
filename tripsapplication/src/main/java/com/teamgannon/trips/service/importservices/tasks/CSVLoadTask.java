package com.teamgannon.trips.service.importservices.tasks;

import com.teamgannon.trips.dialogs.dataset.Dataset;
import com.teamgannon.trips.dialogs.dataset.FileProcessResult;
import com.teamgannon.trips.file.csvin.RegCSVFile;
import com.teamgannon.trips.file.csvin.RegularCsvReader;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.service.DatabaseManagementService;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
public class CSVLoadTask extends Task<FileProcessResult> implements ProgressUpdater {

    private final Dataset dataSet;
    private final DatabaseManagementService databaseManagementService;

    private final RegularCsvReader regularCsvReader;

    public CSVLoadTask(Dataset dataSet, DatabaseManagementService databaseManagementService) {
        this.dataSet = dataSet;
        this.databaseManagementService = databaseManagementService;

        regularCsvReader = new RegularCsvReader(databaseManagementService);
    }

    @Override
    protected FileProcessResult call() throws Exception {
        FileProcessResult result = processCSVFile(dataSet);
        if (result.isSuccess()) {
            log.info("New dataset {} added", dataSet.getName());
        } else {
            log.error("load csv" + result.getMessage());
        }

        return result;
    }


    public FileProcessResult processCSVFile(Dataset dataset) {
        FileProcessResult processResult = new FileProcessResult();

        File file = new File(dataset.getFileSelected());
        // read records
        RegCSVFile regCSVFile = regularCsvReader.loadFile(this, file, dataset);

        try {
            updateMessage(" File load complete, about to save records in database ");
            DataSetDescriptor dataSetDescriptor = databaseManagementService.loadCSVFile(regCSVFile);
            String data = String.format(" %s records loaded from dataset %s, Use plot to see data.",
                    dataSetDescriptor.getNumberStars(),
                    dataSetDescriptor.getDataSetName());
            processResult.setMessage(data);
            processResult.setSuccess(true);
            processResult.setDataSetDescriptor(dataSetDescriptor);
            updateLoadInfo(String.format(" %s records loaded from dataset %s",
                    dataSetDescriptor.getNumberStars(),
                    dataSetDescriptor.getDataSetName()));
        } catch (Exception e) {
            processResult.setSuccess(false);
            processResult.setMessage("This dataset was already loaded in the system ");
        }

        return processResult;
    }

    @Override
    public void updateLoadInfo(String message) {
        updateMessage(message);
    }
}
