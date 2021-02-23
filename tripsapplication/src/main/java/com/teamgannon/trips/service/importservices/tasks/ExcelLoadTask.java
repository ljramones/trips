package com.teamgannon.trips.service.importservices.tasks;

import com.teamgannon.trips.dialogs.dataset.Dataset;
import com.teamgannon.trips.dialogs.dataset.FileProcessResult;
import com.teamgannon.trips.file.excel.normal.ExcelFile;
import com.teamgannon.trips.file.excel.normal.ExcelReader;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.service.DatabaseManagementService;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.File;

@Slf4j
public class ExcelLoadTask extends Task<FileProcessResult> implements ProgressUpdater {


    private final Dataset dataSet;
    private final DatabaseManagementService databaseManagementService;

    private final ExcelReader excelReader;

    public ExcelLoadTask(Dataset dataSet, DatabaseManagementService databaseManagementService) {
        this.dataSet = dataSet;
        this.databaseManagementService = databaseManagementService;

        this.excelReader = new ExcelReader();
    }


    @Override
    protected @NotNull FileProcessResult call() throws Exception {
        FileProcessResult processResult = new FileProcessResult();

        File file = new File(dataSet.getFileSelected());

        // load excel file
        ExcelFile excelFile = excelReader.loadFile(this, dataSet, file, databaseManagementService);
        try {

            DataSetDescriptor descriptor = excelFile.getDescriptor();
            databaseManagementService.saveExcelDataSetDescriptor(this, excelFile);
            processResult.setDataSetDescriptor(descriptor);
            processResult.setSuccess(true);
            String msg = String.format("dataset %s loaded with %d stars", descriptor.getDataSetName(), descriptor.getNumberStars());
            processResult.setMessage(msg);
            updateTaskInfo(msg);

        } catch (Exception e) {
            String msg = String.format("failed to load %s into system", dataSet.getName());
            log.error(msg);
            processResult.setSuccess(false);
            processResult.setMessage(msg);
            updateTaskInfo(msg);
        }

        return processResult;
    }


    @Override
    public void updateTaskInfo(String message) {
        updateMessage(message + "  ");
    }

}
