package com.teamgannon.trips.service.export;

import com.teamgannon.trips.dialogs.dataset.ExportOptions;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.listener.StatusUpdaterListener;
import com.teamgannon.trips.service.DatabaseManagementService;
import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;
import static com.teamgannon.trips.support.AlertFactory.showInfoMessage;

@Slf4j
public class ExcelExporter {

    private final DatabaseManagementService databaseManagementService;
    private final StatusUpdaterListener updaterListener;

    public ExcelExporter(DatabaseManagementService databaseManagementService,
                         StatusUpdaterListener updaterListener) {
        this.databaseManagementService = databaseManagementService;
        this.updaterListener = updaterListener;
    }


    public void exportEntireDB(String fileName) {
        try {

            File myFile = new File(fileName + ".tripsdb.xlsx");
            updateStatus("Starting database export to: " + myFile.getAbsolutePath());

            if (!myFile.createNewFile()) {
                showErrorAlert(
                        "Export Entire Database as an Excel file",
                        "Unable to create file: " + fileName);
                updateStatus("Export failed, could not create: " + myFile.getAbsolutePath());
            }

            // create a work book
            XSSFWorkbook myWorkBook = new XSSFWorkbook();

            List<DataSetDescriptor> dataSetDescriptorList = databaseManagementService.getDataSets();
            createDataSetDescriptorsPage(myWorkBook, dataSetDescriptorList);

            for (DataSetDescriptor descriptor : dataSetDescriptorList) {
                createDataSetSheet(myWorkBook, descriptor);
            }

            // now write it out
            FileOutputStream os = new FileOutputStream(myFile);
            myWorkBook.write(os);
            showInfoMessage("Database Export", "Entire DB was exported to " + myFile.getAbsolutePath());
            updateStatus("export complete, please see: " + myFile.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
            log.error("caught error opening the file:{}", e.getMessage());
            showErrorAlert(
                    "Export Dataset as an Excel file",
                    "Database failed to exported:" + e.getMessage());
        }
    }


    public void exportAsExcel(@NotNull ExportOptions export, @NotNull List<StarObject> starObjects) {

        try {

            File myFile = new File(export.getFileName() + ".trips.xlsx");
            if (!myFile.createNewFile()) {
                showErrorAlert(
                        "Export Dataset as an Excel file",
                        "Unable to create file: " + export.getFileName());
            }

            // create a work book
            XSSFWorkbook myWorkBook = new XSSFWorkbook();

            writeDataDescriptor(export, myWorkBook);
            writeStarData(export, starObjects, myWorkBook);

            FileOutputStream os = new FileOutputStream(myFile);
            myWorkBook.write(os);

            // close the work book
            myWorkBook.close();

            showInfoMessage("Database Export", export.getDataset().getDataSetName()
                    + " was exported to " + export.getFileName() + ".trips.xlsx");

        } catch (Exception e) {
            e.printStackTrace();
            log.error("caught error opening the file:{}", e.getMessage());
            showErrorAlert(
                    "Export Dataset as an Excel file",
                    export.getDataset().getDataSetName() +
                            "failed to exported:" + e.getMessage());
        }
    }

    private void writeDataDescriptor(@NotNull ExportOptions export, @NotNull XSSFWorkbook myWorkBook) {
        XSSFSheet mySheet = myWorkBook.createSheet("descriptor");
        writeDescriptorHeaders(mySheet);
        writeDescriptorData(export.getDataset(),mySheet);

    }

    private void writeDescriptorData(@NotNull DataSetDescriptor dataset, @NotNull XSSFSheet mySheet) {
        int column = 0;
        Row row = mySheet.createRow(1);
        storeCell(row, column++, dataset.getDataSetName());
        storeCell(row, column++, dataset.getFilePath());
        storeCell(row, column++, dataset.getFileCreator());
        storeCell(row, column++, dataset.getFileOriginalDate());
        storeCell(row, column++, dataset.getFileNotes());
        storeCell(row, column++, dataset.getDatasetType());
        storeCell(row, column++, dataset.getNumberStars());
        storeCell(row, column++, dataset.getDistanceRange());
        storeCell(row, column++, dataset.getNumberRoutes());
        storeCell(row, column++, dataset.getThemeStr());
        storeCell(row, column++, dataset.getAstroDataString());
        storeCell(row, column++, dataset.getRoutesStr());
        storeCell(row, column++, dataset.getCustomDataDefsStr());
        storeCell(row, column, dataset.getCustomDataValuesStr());

    }

    private void writeDescriptorHeaders(@NotNull XSSFSheet mySheet) {
        int column = 0;
        Row row = mySheet.createRow(0);
        storeCell(row, column++, "dataSetName");
        storeCell(row, column++, "filePath");
        storeCell(row, column++, "fileCreator");
        storeCell(row, column++, "fileOriginalDate");
        storeCell(row, column++, "fileNotes");
        storeCell(row, column++, "datasetType");
        storeCell(row, column++, "numberStars");
        storeCell(row, column++, "distanceRange");
        storeCell(row, column++, "numberRoutes");
        storeCell(row, column++, "themeStr");
        storeCell(row, column++, "astrographicDataList");
        storeCell(row, column++, "routesStr");
        storeCell(row, column++, "customDataDefsStr");
        storeCell(row, column, "customDataValuesStr");
    }

    private void writeStarData(ExportOptions export, @NotNull List<StarObject> starObjects, @NotNull XSSFWorkbook myWorkBook) {
        // create a work sheet with the dataset name on it
        XSSFSheet mySheet = myWorkBook.createSheet("data");

        writeStarDataHeaders(mySheet);

        int rowCount = 1;
        for (StarObject starObject : starObjects) {
            Row row = mySheet.createRow(rowCount++);
            saveRow(row, starObject);
        }
    }


    private void createDescriptorRow(@NotNull XSSFSheet mySheet, int i, @NotNull DataSetDescriptor descriptor) {
        int column = 0;
        Row row = mySheet.createRow(i);
        storeCell(row, column++, descriptor.getDataSetName());
        storeCell(row, column++, descriptor.getFileCreator());
        storeCell(row, column++, descriptor.getFileOriginalDate());
        storeCell(row, column++, descriptor.getFileNotes());
        storeCell(row, column++, descriptor.getDatasetType());
        storeCell(row, column++, descriptor.getNumberStars());
        storeCell(row, column++, descriptor.getDistanceRange());
        storeCell(row, column, descriptor.getRoutesStr());

    }


    private void createDataSetDescriptorsPage(@NotNull XSSFWorkbook myWorkBook, @NotNull List<DataSetDescriptor> dataSetDescriptorList) {
        XSSFSheet mySheet = myWorkBook.createSheet("Database");
        createDescriptorHeaderRow(mySheet);
        int row = 1;
        for (DataSetDescriptor descriptor : dataSetDescriptorList) {
            createDescriptorRow(mySheet, row++, descriptor);
        }

    }


    private void createDescriptorHeaderRow(@NotNull XSSFSheet mySheet) {
        int column = 0;
        Row row = mySheet.createRow(0);
        storeCell(row, column++, "Dataset name");
        storeCell(row, column++, "File creator");
        storeCell(row, column++, "Original date");
        storeCell(row, column++, "file notes");
        storeCell(row, column++, "datasetType");
        storeCell(row, column++, "Number of stars");
        storeCell(row, column++, "Distance Range");
        storeCell(row, column++, "Number of Routes");
        storeCell(row, column, "Routes as a string");
    }


    private void createDataSetSheet(@NotNull XSSFWorkbook myWorkBook, @NotNull DataSetDescriptor descriptor) {
        // create a work sheet with the dataset name on it
        updateStatus(String.format("starting export of %s", descriptor.getDataSetName()));
        XSSFSheet mySheet = myWorkBook.createSheet(descriptor.getDataSetName());
        writeStarDataHeaders(mySheet);
        int rowCount = 1;
        List<StarObject> starObjects = databaseManagementService.getFromDataset(descriptor);
        for (StarObject starObject : starObjects) {
            Row row = mySheet.createRow(rowCount++);
            saveRow(row, starObject);
        }
        updateStatus(String.format("Export of %s complete, moving to next", descriptor.getDataSetName()));
    }


    private void writeStarDataHeaders(@NotNull XSSFSheet mySheet) {
        int column = 0;
        Row row = mySheet.createRow(0);
        storeCell(row, column++, "id");
        storeCell(row, column++, "dataSetName");
        storeCell(row, column++, "displayName");
        storeCell(row, column++, "constellationName");
        storeCell(row, column++, "mass");
        storeCell(row, column++, "actualMass");
        storeCell(row, column++, "source");
        storeCell(row, column++, "catalogIdList");
        storeCell(row, column++, "X");
        storeCell(row, column++, "Y");
        storeCell(row, column++, "Z");
        storeCell(row, column++, "radius");
        storeCell(row, column++, "ra");
        storeCell(row, column++, "pmra");
        storeCell(row, column++, "declination");
        storeCell(row, column++, "pmdec");
        storeCell(row, column++, "dec_deg");
        storeCell(row, column++, "rs_cdeg");
        storeCell(row, column++, "parallax");
        storeCell(row, column++, "distance");
        storeCell(row, column++, "radialVelocity");
        storeCell(row, column++, "spectralClass");
        storeCell(row, column++, "orthoSpectralClass");
        storeCell(row, column++, "temperature");
        storeCell(row, column++, "realStar");
        storeCell(row, column++, "bprp");
        storeCell(row, column++, "bpg");
        storeCell(row, column++, "grp");
        storeCell(row, column++, "luminosity");
        storeCell(row, column++, "magu");
        storeCell(row, column++, "magb");
        storeCell(row, column++, "magv");
        storeCell(row, column++, "magr");
        storeCell(row, column++, "magi");
        storeCell(row, column++, "other");
        storeCell(row, column++, "anomaly");
        storeCell(row, column++, "polity");
        storeCell(row, column++, "worldType");
        storeCell(row, column++, "fuelType");
        storeCell(row, column++, "portType");
        storeCell(row, column++, "populationType");
        storeCell(row, column++, "techType");
        storeCell(row, column++, "productType");
        storeCell(row, column++, "milSpaceType");
        storeCell(row, column++, "milPlanType");
        storeCell(row, column++, "miscText1");
        storeCell(row, column++, "miscText2");
        storeCell(row, column++, "miscText3");
        storeCell(row, column++, "miscText4");
        storeCell(row, column++, "miscText5");
        storeCell(row, column++, "miscNum1");
        storeCell(row, column++, "miscNum2");
        storeCell(row, column++, "miscNum3");
        storeCell(row, column++, "miscNum4");
        storeCell(row, column++, "miscNum5");
        storeCell(row, column, "notes");
    }


    private void saveRow(@NotNull Row row, @NotNull StarObject starObject) {
        int column = 0;

        storeCell(row, column++, starObject.getId().toString());
        storeCell(row, column++, starObject.getDataSetName());
        storeCell(row, column++, starObject.getDisplayName());
        storeCell(row, column++, starObject.getConstellationName());
        storeCell(row, column++, starObject.getMass());
        storeCell(row, column++, starObject.getActualMass());
        storeCell(row, column++, starObject.getSource());
        storeCell(row, column++, starObject.getCatalogIdList());
        storeCell(row, column++, starObject.getX());
        storeCell(row, column++, starObject.getY());
        storeCell(row, column++, starObject.getZ());
        storeCell(row, column++, starObject.getRadius());
        storeCell(row, column++, starObject.getRa());
        storeCell(row, column++, starObject.getPmra());
        storeCell(row, column++, starObject.getDeclination());
        storeCell(row, column++, starObject.getPmdec());
        storeCell(row, column++, starObject.getDec_deg());
        storeCell(row, column++, starObject.getRs_cdeg());
        storeCell(row, column++, starObject.getParallax());
        storeCell(row, column++, starObject.getDistance());
        storeCell(row, column++, starObject.getRadialVelocity());
        storeCell(row, column++, starObject.getSpectralClass());
        storeCell(row, column++, starObject.getOrthoSpectralClass());
        storeCell(row, column++, starObject.getTemperature());
        storeCell(row, column++, starObject.isRealStar());
        storeCell(row, column++, starObject.getBprp());
        storeCell(row, column++, starObject.getBpg());
        storeCell(row, column++, starObject.getGrp());
        storeCell(row, column++, starObject.getLuminosity());
        storeCell(row, column++, starObject.getMagu());
        storeCell(row, column++, starObject.getMagb());
        storeCell(row, column++, starObject.getMagv());
        storeCell(row, column++, starObject.getMagr());
        storeCell(row, column++, starObject.getMagi());
        storeCell(row, column++, starObject.isOther());
        storeCell(row, column++, starObject.isAnomaly());
        storeCell(row, column++, starObject.getPolity());
        storeCell(row, column++, starObject.getWorldType());
        storeCell(row, column++, starObject.getFuelType());
        storeCell(row, column++, starObject.getPortType());
        storeCell(row, column++, starObject.getPopulationType());
        storeCell(row, column++, starObject.getTechType());
        storeCell(row, column++, starObject.getProductType());
        storeCell(row, column++, starObject.getMilSpaceType());
        storeCell(row, column++, starObject.getMilPlanType());
        storeCell(row, column++, starObject.getMiscText1());
        storeCell(row, column++, starObject.getMiscText2());
        storeCell(row, column++, starObject.getMiscText3());
        storeCell(row, column++, starObject.getMiscText4());
        storeCell(row, column++, starObject.getMiscText5());
        storeCell(row, column++, starObject.getMiscNum1());
        storeCell(row, column++, starObject.getMiscNum2());
        storeCell(row, column++, starObject.getMiscNum3());
        storeCell(row, column++, starObject.getMiscNum4());
        storeCell(row, column++, starObject.getMiscNum5());
        storeCell(row, column, starObject.getNotes());

    }

    private void storeCell(@NotNull Row row, int column, long intValue) {
        Cell cell = row.createCell(column);
        cell.setCellValue(Long.toString(intValue));
    }

    private void storeCell(@NotNull Row row, int column, int intValue) {
        Cell cell = row.createCell(column);
        cell.setCellValue(Integer.toString(intValue));
    }

    private void storeCell(@NotNull Row row, int column, boolean booleanValue) {
        Cell cell = row.createCell(column);
        cell.setCellValue(Boolean.toString(booleanValue));
    }

    private void storeCell(@NotNull Row row, int column, String stringValue) {
        Cell cell = row.createCell(column);
        cell.setCellValue(stringValue);
    }

    private void storeCell(@NotNull Row row, int column, double doubleValue) {
        Cell cell = row.createCell(column);
        cell.setCellValue(Double.toString(doubleValue));
    }

    private void storeCell(@NotNull Row row, int column, List<String> list) {
        Cell cell = row.createCell(column);
        cell.setCellValue(StringUtils.join(list, ' '));
    }

    private void updateStatus(String status) {
        new Thread(() -> Platform.runLater(() -> {
            log.info(status);
            updaterListener.updateStatus(status);
        })).start();
    }
}
