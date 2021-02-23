package com.teamgannon.trips.service.export;

import com.teamgannon.trips.dialogs.dataset.FileProcessResult;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.importservices.tasks.ProgressUpdater;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

/**
 * I leave this in case we want it for the future but in reality, writing out as xlsx or xls is generally a bad idea.
 * I may use this streams form in the future.
 */
@Slf4j
public class ExcelExportTask extends Task<FileProcessResult> implements ProgressUpdater {

    private final DataSetDescriptor dataSet;
    private final File fileToExport;
    private final DatabaseManagementService databaseManagementService;

    private int totalRowsWritten = 0;


    public ExcelExportTask(DataSetDescriptor dataSet, File fileToExport, DatabaseManagementService databaseManagementService) {
        this.dataSet = dataSet;
        this.fileToExport = fileToExport;
        this.databaseManagementService = databaseManagementService;
    }


    @Override
    protected FileProcessResult call() throws Exception {
        FileProcessResult processResult = new FileProcessResult();

        int requestSize = 1000;

        try {
            // create an excel file from the file
            FileInputStream inputStream = new FileInputStream(fileToExport.getAbsoluteFile());
            XSSFWorkbook wb_template = new XSSFWorkbook(inputStream);
            inputStream.close();

            // define a streaming worksheet
            SXSSFWorkbook wb = new SXSSFWorkbook(wb_template);
            wb.setCompressTempFiles(true);

            // setup the read ratio at 110 pages
            SXSSFSheet sh = wb.getSheetAt(0);
            sh.setRandomAccessWindowSize(100);// keep 100 rows in memory, exceeding rows will be flushed to disk

            // read the first page and get how many pages there are
            Page<StarObject> starObjectPage = databaseManagementService.getFromDatasetByPage(dataSet, 0, requestSize);
            int totalPages = starObjectPage.getTotalPages();
            int totalElements = starObjectPage.getNumberOfElements();

            // write this initial page to the file
            writeToExcelFile(starObjectPage, sh);
            updateTaskInfo("1000 written to file," + totalRowsWritten + " so far");

            // for the remaining pages parse and write each to a file
            log.info("Total pages={}, total elements={}", totalPages, totalElements);
            for (int pageNumber = 0; pageNumber < totalPages - 1; pageNumber++) {
                starObjectPage = databaseManagementService.getFromDatasetByPage(dataSet, pageNumber, requestSize);
                writeToExcelFile(starObjectPage, sh);
                updateTaskInfo("1000 written to file," + totalRowsWritten + " so far");
            }

            // report the final result
            String msg = String.format("dataset %s exported with %d stars", fileToExport.getAbsoluteFile(), totalRowsWritten);
            processResult.setMessage(msg);
            updateTaskInfo(msg);
        } catch (Exception e) {
            String msg = String.format("failed to load %s into system", fileToExport.getAbsoluteFile());
            log.error(msg);
            processResult.setSuccess(false);
            processResult.setMessage(msg);
            updateTaskInfo(msg);
        }
        return processResult;
    }

    /**
     * write a page to the target file
     *
     * @param starObjectPage the page retrieved
     * @param sh             the sheet to write to
     */
    private void writeToExcelFile(Page<StarObject> starObjectPage, SXSSFSheet sh) {
        List<StarObject> starObjectList = starObjectPage.getContent();
        for (StarObject starObject : starObjectList) {
            writeLineToExcelFile(starObject, sh);
        }
    }

    /**
     * write a line to the file
     *
     * @param starObject the star object to write
     * @param sh         the sheet to use
     */
    private void writeLineToExcelFile(StarObject starObject, SXSSFSheet sh) {
        Row row = sh.createRow(totalRowsWritten);
        for (int cellnum = 0; cellnum < 10; cellnum++) {
            Cell cell = row.createCell(cellnum);
            String address = new CellReference(cell).formatAsString();
            cell.setCellValue(address);
        }
        totalRowsWritten++;
    }


    /**
     * update the load info to the UI
     *
     * @param message the message to write
     */
    @Override
    public void updateTaskInfo(String message) {
        updateMessage(message);
    }
}
