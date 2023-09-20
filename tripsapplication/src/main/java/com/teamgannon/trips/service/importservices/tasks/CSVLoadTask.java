package com.teamgannon.trips.service.importservices.tasks;

import com.teamgannon.trips.dialogs.dataset.model.Dataset;
import com.teamgannon.trips.dialogs.dataset.model.FileProcessResult;
import com.teamgannon.trips.file.csvin.RegCSVFile;
import com.teamgannon.trips.file.csvin.RegularStarCatalogCsvReader;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.service.DatabaseManagementService;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.File;

@Slf4j
public class CSVLoadTask extends Task<FileProcessResult> implements ProgressUpdater {

    private final Dataset dataSet;
    private final DatabaseManagementService databaseManagementService;

    private final RegularStarCatalogCsvReader regularStarCatalogCsvReader;

    public CSVLoadTask(DatabaseManagementService databaseManagementService, Dataset loadDataset) {
        this.databaseManagementService = databaseManagementService;
        this.dataSet = loadDataset;
        this.regularStarCatalogCsvReader = new RegularStarCatalogCsvReader(databaseManagementService);
    }

    @Override
    protected @NotNull FileProcessResult call() throws Exception {
        FileProcessResult result = processCSVFile(dataSet);
        if (result.isSuccess()) {
            log.info("New dataset {} added", dataSet.getName());
        } else {
            log.error("load csv" + result.getMessage());
        }

        return result;
    }


    public FileProcessResult processCSVFile(@NotNull Dataset dataset) {
        log.info("beginning processing of dataset={}", dataset);
        FileProcessResult processResult = new FileProcessResult();

        File file = new File(dataset.getFileSelected());
        // read records
        RegCSVFile regCSVFile = regularStarCatalogCsvReader.loadFile(this, file, dataset);
        log.info("finished processing of dataset");
        try {
            if (regCSVFile.isReadSuccess()) {
                updateMessage(" File load complete, about to save records in database ");
                DataSetDescriptor dataSetDescriptor = databaseManagementService.loadCSVFile(regCSVFile);
                String data = String.format(" %s records loaded from dataset %s, Use plot to see data.",
                        dataSetDescriptor.getNumberStars(),
                        dataSetDescriptor.getDataSetName());
                processResult.setMessage(data);
                processResult.setSuccess(true);
                processResult.setDataSetDescriptor(dataSetDescriptor);
                updateTaskInfo(String.format(" %s records loaded from dataset %s",
                        dataSetDescriptor.getNumberStars(),
                        dataSetDescriptor.getDataSetName()));
            } else {
                processResult.setMessage(regCSVFile.getProcessMessage());
                processResult.setSuccess(false);
            }
        } catch (Exception e) {
            processResult.setSuccess(false);
            processResult.setMessage("Failed to load the dataset, see log ");
        }

        return processResult;
    }

    @Override
    public void updateTaskInfo(String message) {
        updateMessage(message + "  ");
    }


}
