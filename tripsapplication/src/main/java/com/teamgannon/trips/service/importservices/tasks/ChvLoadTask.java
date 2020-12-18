package com.teamgannon.trips.service.importservices.tasks;

import com.teamgannon.trips.dialogs.dataset.Dataset;
import com.teamgannon.trips.dialogs.dataset.FileProcessResult;
import com.teamgannon.trips.file.chview.ChviewReader;
import com.teamgannon.trips.file.chview.model.ChViewFile;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.service.DatabaseManagementService;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.File;

@Slf4j
public class ChvLoadTask extends Task<FileProcessResult> implements ProgressUpdater {

    private final Dataset dataset;
    private final DatabaseManagementService databaseManagementService;

    private final @NotNull ChviewReader chviewReader;

    public ChvLoadTask(Dataset dataset, DatabaseManagementService databaseManagementService) {
        this.dataset = dataset;
        this.databaseManagementService = databaseManagementService;

        this.chviewReader = new ChviewReader();
    }

    @Override
    protected @NotNull FileProcessResult call() throws Exception {
        FileProcessResult processResult = new FileProcessResult();

        File file = new File(dataset.getFileSelected());

        // load chview file
        ChViewFile chViewFile = chviewReader.loadFile(this, file);
        if (chViewFile == null) {
            FileProcessResult result = new FileProcessResult();
            result.setDataSetDescriptor(null);
            result.setSuccess(false);
            result.setMessage("Failed to parse file");
            return result;
        }
        try {
            updateMessage("File load complete, about to save records in database");
            DataSetDescriptor dataSetDescriptor = databaseManagementService.loadCHFile(this, dataset, chViewFile);
            String data = String.format("%s records loaded from dataset %s, Use plot to see data.",
                    dataSetDescriptor.getNumberStars(),
                    dataSetDescriptor.getDataSetName());
            processResult.setMessage(data);
            processResult.setSuccess(true);
            processResult.setDataSetDescriptor(dataSetDescriptor);
            updateProgress(dataSetDescriptor.getNumberStars(), dataSetDescriptor.getNumberStars());
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
