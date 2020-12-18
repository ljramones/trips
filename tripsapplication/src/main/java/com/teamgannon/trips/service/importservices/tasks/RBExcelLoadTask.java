package com.teamgannon.trips.service.importservices.tasks;

import com.teamgannon.trips.dialogs.dataset.Dataset;
import com.teamgannon.trips.dialogs.dataset.FileProcessResult;
import com.teamgannon.trips.file.excel.rb.RBExcelFile;
import com.teamgannon.trips.file.excel.rb.RBExcelReader;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.service.DatabaseManagementService;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.File;

@Slf4j
public class RBExcelLoadTask extends Task<FileProcessResult> implements ProgressUpdater {

    private final Dataset dataset;
    private final DatabaseManagementService databaseManagementService;

    private final @NotNull RBExcelReader RBExcelReader;


    public RBExcelLoadTask(Dataset dataset, DatabaseManagementService databaseManagementService) {

        this.dataset = dataset;
        this.databaseManagementService = databaseManagementService;

        this.RBExcelReader = new RBExcelReader();

    }

    @Override
    protected @NotNull FileProcessResult call() throws Exception {
        FileProcessResult processResult = new FileProcessResult();

        File file = new File(dataset.getFileSelected());

        // load RB excel file
        RBExcelFile excelFile = RBExcelReader.loadFile(this, file);
        try {
            DataSetDescriptor dataSetDescriptor = databaseManagementService.loadRBStarSet(excelFile);
            String data = String.format("%s records loaded from dataset %s, Use plot to see data.",
                    dataSetDescriptor.getAstrographicDataList().size(),
                    dataSetDescriptor.getDataSetName());
            log.info("Load RB Excel Format {}", data);
            processResult.setSuccess(true);

        } catch (Exception e) {
            log.error("Duplicate Dataset, This dataset was already loaded in the system ");
            processResult.setSuccess(false);
        }

        return processResult;
    }

    @Override
    public void updateLoadInfo(String message) {
        updateMessage(message);
    }
}
