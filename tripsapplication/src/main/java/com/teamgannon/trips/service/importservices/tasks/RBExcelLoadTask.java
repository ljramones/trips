package com.teamgannon.trips.service.importservices.tasks;

import com.teamgannon.trips.dialogs.dataset.Dataset;
import com.teamgannon.trips.dialogs.dataset.FileProcessResult;
import com.teamgannon.trips.file.excel.ExcelReader;
import com.teamgannon.trips.file.excel.RBExcelFile;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.service.DatabaseManagementService;
import javafx.concurrent.Task;
import javafx.scene.control.Label;

import java.io.File;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;
import static com.teamgannon.trips.support.AlertFactory.showInfoMessage;

public class RBExcelLoadTask extends Task<FileProcessResult> implements ProgressUpdater {

    private final Dataset dataset;
    private final DatabaseManagementService databaseManagementService;

    private final ExcelReader excelReader;


    public RBExcelLoadTask(Dataset dataset, DatabaseManagementService databaseManagementService) {

        this.dataset = dataset;
        this.databaseManagementService = databaseManagementService;

        this.excelReader = new ExcelReader();

    }

    @Override
    protected FileProcessResult call() throws Exception {
        FileProcessResult processResult = new FileProcessResult();

        File file = new File(dataset.getFileSelected());

        // load RB excel file
        RBExcelFile excelFile = excelReader.loadFile(this,file);
        try {
            DataSetDescriptor dataSetDescriptor = databaseManagementService.loadRBStarSet(excelFile);
            String data = String.format("%s records loaded from dataset %s, Use plot to see data.",
                    dataSetDescriptor.getAstrographicDataList().size(),
                    dataSetDescriptor.getDataSetName());
            showInfoMessage("Load RB Excel Format", data);
            processResult.setSuccess(true);

        } catch (Exception e) {
            showErrorAlert("Duplicate Dataset", "This dataset was already loaded in the system ");
            processResult.setSuccess(false);
        }

        return processResult;
    }

    @Override
    public void updateLoadInfo(String message) {
        updateMessage(message);
    }
}
