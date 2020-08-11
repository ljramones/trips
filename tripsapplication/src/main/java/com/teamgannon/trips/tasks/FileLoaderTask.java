package com.teamgannon.trips.tasks;

import com.teamgannon.trips.dialogs.Dataset;
import com.teamgannon.trips.dialogs.support.FileProcessResult;
import com.teamgannon.trips.file.chview.model.ChViewFile;
import com.teamgannon.trips.file.csvin.RBCsvFile;
import com.teamgannon.trips.file.excel.RBExcelFile;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;
import static com.teamgannon.trips.support.AlertFactory.showInfoMessage;

@Slf4j
public class FileLoaderTask extends Task<Long> {

    private Dataset dataset;

    public FileLoaderTask(Dataset dataset) {
        this.dataset = dataset;
    }


    @Override
    protected Long call() throws Exception {
//        FileProcessResult result;
//        // this is a CH View import format
//        // this is Excel format that follows a specification from the Rick Boatwright format
//        // this is a database import
//        // this is Simbad database import format
//        switch (dataset.getDataType().getSuffix()) {
//            case "chv" -> {
//                result = processCHViewFile(dataset);
//                if (result.isSuccess()) {
//                    DataSetDescriptor dataSetDescriptor = result.getDataSetDescriptor();
//                    this.dataUpdater.addDataSet(dataSetDescriptor);
//                    updateTable();
//                } else {
//                    showErrorAlert("load CH View file", result.getMessage());
//                }
//            }
//            case "xlsv" -> {
//                result = processRBExcelFile(dataset);
//                if (result.isSuccess()) {
//                    DataSetDescriptor dataSetDescriptor = result.getDataSetDescriptor();
//                    updateTable();
//                } else {
//                    showErrorAlert("load Excel file", result.getMessage());
//                }
//            }
//            case "csv" -> {
//
//                result = processCSVFile(dataset);
//                if (result.isSuccess()) {
//                    DataSetDescriptor dataSetDescriptor = result.getDataSetDescriptor();
//                    this.dataUpdater.addDataSet(dataSetDescriptor);
//                    updateTable();
//                } else {
//                    showErrorAlert("load csv", result.getMessage());
//                }
//            }
//            case "simbad" -> {
//                result = processSimbadFile(dataset);
//                if (result.isSuccess()) {
//                    DataSetDescriptor dataSetDescriptor = result.getDataSetDescriptor();
//
//                    updateTable();
//                } else {
//                    showErrorAlert("load simbad", result.getMessage());
//                }
//            }
//        }
//        log.info("New dataset {} added", dataset.getName());
        return 0L;
    }
/*
    private FileProcessResult processSimbadFile(Dataset dataset) {
        FileProcessResult processResult = new FileProcessResult();
        processResult.setSuccess(true);

        return processResult;
    }

    private FileProcessResult processCSVFile(Dataset dataset) {
        FileProcessResult processResult = new FileProcessResult();

        File file = new File(dataset.getFileSelected());
        RBCsvFile rbCsvFile = rbCsvReader.loadFile(file, dataset);
        try {
            DataSetDescriptor dataSetDescriptor = databaseManagementService.loadRBCSVStarSet(rbCsvFile);
            String data = String.format("%s records loaded from dataset %s, Use plot to see data.",
                    dataSetDescriptor.getNumberStars(),
                    dataSetDescriptor.getDataSetName());
            showInfoMessage("Load CSV Format", data);
            processResult.setSuccess(true);
            processResult.setDataSetDescriptor(dataSetDescriptor);
        } catch (Exception e) {
            showErrorAlert("Duplicate Dataset", "This dataset was already loaded in the system ");
            processResult.setSuccess(false);
        }

        return processResult;
    }

    private FileProcessResult processRBExcelFile(Dataset dataset) {
        FileProcessResult processResult = new FileProcessResult();

        File file = new File(dataset.getFileSelected());

        // load RB excel file
        RBExcelFile excelFile = excelReader.loadFile(file);
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

    private FileProcessResult processCHViewFile(Dataset dataset) {
        FileProcessResult processResult = new FileProcessResult();

        File file = new File(dataset.getFileSelected());

        // load chview file
        ChViewFile chViewFile = chviewReader.loadFile(file);
        try {
            DataSetDescriptor dataSetDescriptor = databaseManagementService.loadCHFile(dataset, chViewFile);
            String data = String.format("%s records loaded from dataset %s, Use plot to see data.",
                    dataSetDescriptor.getAstrographicDataList().size(),
                    dataSetDescriptor.getDataSetName());
            showInfoMessage("Load CHV Format", data);
            processResult.setSuccess(true);
            processResult.setDataSetDescriptor(dataSetDescriptor);
        } catch (Exception e) {
            processResult.setSuccess(false);
            processResult.setMessage("This dataset was already loaded in the system ");
            showErrorAlert("Duplicate Dataset", "This dataset was already loaded in the system ");
        }
        return processResult;

    }*/
}
