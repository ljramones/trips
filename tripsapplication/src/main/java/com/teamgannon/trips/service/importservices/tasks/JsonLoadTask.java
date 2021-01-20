package com.teamgannon.trips.service.importservices.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamgannon.trips.dialogs.dataset.Dataset;
import com.teamgannon.trips.dialogs.dataset.FileProcessResult;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.export.model.JsonExportObj;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Paths;

@Slf4j
public class JsonLoadTask extends Task<FileProcessResult> implements ProgressUpdater {

    private final Dataset dataset;
    private final DatabaseManagementService databaseManagementService;

    public JsonLoadTask(Dataset dataset, DatabaseManagementService databaseManagementService) {
        this.dataset = dataset;
        this.databaseManagementService = databaseManagementService;
    }

    @Override
    protected @Nullable FileProcessResult call() throws Exception {
        FileProcessResult result = processJsonFile(dataset);
        if (result.isSuccess()) {
            log.info("New dataset {} added", dataset.getName());
        } else {
            log.error("load csv" + result.getMessage());
        }

        return result;
    }

    private FileProcessResult processJsonFile(Dataset dataset) {
        FileProcessResult processResult = new FileProcessResult();

        try {
            ObjectMapper obj = new ObjectMapper();
            JsonExportObj jsonExportObj = obj.readValue(Paths.get(dataset.getFileSelected()).toFile(), JsonExportObj.class);
            updateFromDataset(dataset, jsonExportObj);
            databaseManagementService.loadJsonFileSingleDS(this, jsonExportObj);
            processResult.setDataSetDescriptor(jsonExportObj.getDescriptor().toDataSetDescriptor());
            processResult.setMessage("dataset loaded "+ dataset.getName());
            processResult.setSuccess(true);
        } catch (Exception e) {
            log.error("Failed to read file for {}", dataset.getName());
            processResult.setSuccess(false);
            processResult.setMessage("This dataset was already loaded in the system ");
        }


        return processResult;
    }

    private void updateFromDataset(Dataset dataset, JsonExportObj jsonExportObj) {
        jsonExportObj.getDescriptor().setDataSetName(dataset.getName());
        jsonExportObj.getDescriptor().setFileCreator(dataset.getAuthor());
        jsonExportObj.getStarObjectList().forEach(astrographicObject -> astrographicObject.setDataSetName(dataset.getName()));
    }


    @Override
    public void updateLoadInfo(String message) {
        updateMessage(message);
    }
}
