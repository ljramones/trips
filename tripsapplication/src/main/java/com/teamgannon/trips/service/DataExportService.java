package com.teamgannon.trips.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamgannon.trips.dialogs.dataset.ExportOptions;
import com.teamgannon.trips.jpa.model.AstrographicObject;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.listener.StatusUpdaterListener;
import javafx.application.Platform;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static com.teamgannon.trips.support.AlertFactory.*;

/**
 * Used to import and export data sets
 * <p>
 * Created by larrymitchell on 2017-01-22.
 */
@Slf4j
public class DataExportService {

    private final DatabaseManagementService databaseManagementService;
    private final StatusUpdaterListener updaterListener;

    public DataExportService(DatabaseManagementService databaseManagementService,
                             StatusUpdaterListener updaterListener) {
        this.databaseManagementService = databaseManagementService;
        this.updaterListener = updaterListener;
    }


    public void exportDB() {
        Optional<ButtonType> result = showConfirmationAlert(
                "Data Export Service", "Entire Database",
                "Do you want to export this database? It will take a while.");
        if ((result.isPresent()) && (result.get() == ButtonType.OK)) {
            final FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export entire database to export as a Excel file");
            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                exportEntireDB(file.getAbsolutePath());
            } else {
                log.warn("file export cancelled");
                showInfoMessage("Database export", "Export cancelled");
            }
        }
    }

    private void exportEntireDB(String fileName) {
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

    private void createDataSetDescriptorsPage(XSSFWorkbook myWorkBook, List<DataSetDescriptor> dataSetDescriptorList) {
        XSSFSheet mySheet = myWorkBook.createSheet("Database");
        createDescriptorHeaderRow(mySheet);
        int row = 1;
        for (DataSetDescriptor descriptor : dataSetDescriptorList) {
            createDescriptorRow(mySheet, row++, descriptor);
        }

    }

    private void createDescriptorHeaderRow(XSSFSheet mySheet) {
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

    private void createDescriptorRow(XSSFSheet mySheet, int i, DataSetDescriptor descriptor) {
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

    private void createDataSetSheet(XSSFWorkbook myWorkBook, DataSetDescriptor descriptor) {
        // create a work sheet with the dataset name on it
        updateStatus(String.format("starting export of %s", descriptor.getDataSetName()));
        XSSFSheet mySheet = myWorkBook.createSheet(descriptor.getDataSetName());
        writeHeaders(mySheet);
        int rowCount = 1;
        List<AstrographicObject> astrographicObjects = databaseManagementService.getFromDataset(descriptor);
        for (AstrographicObject astrographicObject : astrographicObjects) {
            Row row = mySheet.createRow(rowCount++);
            saveRow(row, astrographicObject);
        }
        updateStatus(String.format("Export of %s complete, moving to next", descriptor.getDataSetName()));
    }

    public void exportDataset(ExportOptions exportOptions) {

        List<AstrographicObject> astrographicObjects = databaseManagementService.getFromDataset(exportOptions.getDataset());
        switch (exportOptions.getExportFormat()) {

            case CSV -> {
                exportAsCSV(exportOptions, astrographicObjects);
            }
            case EXCEL -> {
                exportAsExcel(exportOptions, astrographicObjects);
            }
            case JSON -> {
                exportAsJson(exportOptions, astrographicObjects);
            }
        }

    }

    private void exportAsJson(ExportOptions export, List<AstrographicObject> astrographicObjects) {

        ObjectMapper Obj = new ObjectMapper();

        try {
            Writer writer = Files.newBufferedWriter(Paths.get(export.getFileName() + ".trips.json"));

            String jsonStr = Obj.writeValueAsString(astrographicObjects);
            writer.write(jsonStr);

            writer.flush();
            writer.close();
            showInfoMessage("Database Export", export.getDataset().getDataSetName()
                    + " was export to " + export.getFileName() + ".trips.json");

        } catch (Exception e) {
            log.error("caught error opening the file:{}", e.getMessage());
            showErrorAlert(
                    "Export Dataset as JSON file",
                    export.getDataset().getDataSetName() +
                            "failed to export:" + e.getMessage());
        }
    }

    private void exportAsExcel(ExportOptions export, List<AstrographicObject> astrographicObjects) {

        try {

            File myFile = new File(export.getFileName() + ".trips.xlsx");
            if (!myFile.createNewFile()) {
                showErrorAlert(
                        "Export Dataset as an Excel file",
                        "Unable to create file: " + export.getFileName());
            }

            // create a work book
            XSSFWorkbook myWorkBook = new XSSFWorkbook();

            // create a work sheet with the dataset name on it
            XSSFSheet mySheet = myWorkBook.createSheet(export.getDataset().getDataSetName());

            writeHeaders(mySheet);

            int rowCount = 1;
            for (AstrographicObject astrographicObject : astrographicObjects) {
                Row row = mySheet.createRow(rowCount++);
                saveRow(row, astrographicObject);
            }

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

    private void updateStatus(String status) {
        new Thread(() -> Platform.runLater(() -> {
            log.info(status);
            updaterListener.updateStatus(status);
        })).start();
    }

    private void writeHeaders(XSSFSheet mySheet) {
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

    private void saveRow(Row row, AstrographicObject astrographicObject) {
        int column = 0;

        storeCell(row, column++, astrographicObject.getId().toString());
        storeCell(row, column++, astrographicObject.getDataSetName());
        storeCell(row, column++, astrographicObject.getDisplayName());
        storeCell(row, column++, astrographicObject.getConstellationName());
        storeCell(row, column++, astrographicObject.getMass());
        storeCell(row, column++, astrographicObject.getActualMass());
        storeCell(row, column++, astrographicObject.getSource());
        storeCell(row, column++, astrographicObject.getCatalogIdList());
        storeCell(row, column++, astrographicObject.getX());
        storeCell(row, column++, astrographicObject.getY());
        storeCell(row, column++, astrographicObject.getZ());
        storeCell(row, column++, astrographicObject.getRadius());
        storeCell(row, column++, astrographicObject.getRa());
        storeCell(row, column++, astrographicObject.getPmra());
        storeCell(row, column++, astrographicObject.getDeclination());
        storeCell(row, column++, astrographicObject.getPmdec());
        storeCell(row, column++, astrographicObject.getDec_deg());
        storeCell(row, column++, astrographicObject.getRs_cdeg());
        storeCell(row, column++, astrographicObject.getParallax());
        storeCell(row, column++, astrographicObject.getDistance());
        storeCell(row, column++, astrographicObject.getRadialVelocity());
        storeCell(row, column++, astrographicObject.getSpectralClass());
        storeCell(row, column++, astrographicObject.getOrthoSpectralClass());
        storeCell(row, column++, astrographicObject.getTemperature());
        storeCell(row, column++, astrographicObject.isRealStar());
        storeCell(row, column++, astrographicObject.getBprp());
        storeCell(row, column++, astrographicObject.getBpg());
        storeCell(row, column++, astrographicObject.getGrp());
        storeCell(row, column++, astrographicObject.getLuminosity());
        storeCell(row, column++, astrographicObject.getMagu());
        storeCell(row, column++, astrographicObject.getMagb());
        storeCell(row, column++, astrographicObject.getMagv());
        storeCell(row, column++, astrographicObject.getMagr());
        storeCell(row, column++, astrographicObject.getMagi());
        storeCell(row, column++, astrographicObject.isOther());
        storeCell(row, column++, astrographicObject.isAnomaly());
        storeCell(row, column++, astrographicObject.getPolity());
        storeCell(row, column++, astrographicObject.getWorldType());
        storeCell(row, column++, astrographicObject.getFuelType());
        storeCell(row, column++, astrographicObject.getPortType());
        storeCell(row, column++, astrographicObject.getPopulationType());
        storeCell(row, column++, astrographicObject.getTechType());
        storeCell(row, column++, astrographicObject.getProductType());
        storeCell(row, column++, astrographicObject.getMilSpaceType());
        storeCell(row, column++, astrographicObject.getMilPlanType());
        storeCell(row, column++, astrographicObject.getMiscText1());
        storeCell(row, column++, astrographicObject.getMiscText2());
        storeCell(row, column++, astrographicObject.getMiscText3());
        storeCell(row, column++, astrographicObject.getMiscText4());
        storeCell(row, column++, astrographicObject.getMiscText5());
        storeCell(row, column++, astrographicObject.getMiscNum1());
        storeCell(row, column++, astrographicObject.getMiscNum2());
        storeCell(row, column++, astrographicObject.getMiscNum3());
        storeCell(row, column++, astrographicObject.getMiscNum4());
        storeCell(row, column++, astrographicObject.getMiscNum5());
        storeCell(row, column, astrographicObject.getNotes());

    }

    private void storeCell(Row row, int column, long intValue) {
        Cell cell = row.createCell(column);
        cell.setCellValue(Long.toString(intValue));
    }

    private void storeCell(Row row, int column, int intValue) {
        Cell cell = row.createCell(column);
        cell.setCellValue(Integer.toString(intValue));
    }

    private void storeCell(Row row, int column, boolean booleanValue) {
        Cell cell = row.createCell(column);
        cell.setCellValue(Boolean.toString(booleanValue));
    }

    private void storeCell(Row row, int column, String stringValue) {
        Cell cell = row.createCell(column);
        cell.setCellValue(stringValue);
    }

    private void storeCell(Row row, int column, double doubleValue) {
        Cell cell = row.createCell(column);
        cell.setCellValue(Double.toString(doubleValue));
    }

    private void storeCell(Row row, int column, List<String> list) {
        Cell cell = row.createCell(column);
        cell.setCellValue(StringUtils.join(list, ' '));
    }

    private void exportAsCSV(ExportOptions export, List<AstrographicObject> astrographicObjects) {
        try {
            Writer writer = Files.newBufferedWriter(Paths.get(export.getFileName() + ".trips.csv"));

            String headers = getHeaders();
            writer.write(headers);
            int i = 0;
            for (AstrographicObject astrographicObject : astrographicObjects) {
                String csvRecord = convertToCSV(astrographicObject);
                writer.write(csvRecord);
                i++;
            }
            writer.flush();
            writer.close();
            showInfoMessage("Database Export", export.getDataset().getDataSetName()
                    + " was exported to " + export.getFileName() + ".trips.csv");

        } catch (Exception e) {
            log.error("caught error opening the file:{}", e.getMessage());
            showErrorAlert(
                    "Export Dataset as CSV file",
                    export.getDataset().getDataSetName() +
                            "failed to export:" + e.getMessage()
            );
        }
    }


    private String getHeaders() {

        return "id," +
                "dataSetName," +
                "displayName," +
                "constellationName," +
                "mass," +
                "actualMass," +
                "source," +
                "catalogIdList," +
                "X," +
                "Y," +
                "Z," +
                "radius," +
                "ra," +
                "pmra," +
                "declination," +
                "pmdec," +
                "dec_deg," +
                "rs_cdeg," +
                "parallax," +
                "distance," +
                "radialVelocity," +
                "spectralClass," +
                "orthoSpectralClass," +
                "temperature," +
                "realStar," +
                "bprp," +
                "bpg," +
                "grp," +
                "luminosity," +
                "magu," +
                "magb," +
                "magv," +
                "magr," +
                "magi," +
                "other," +
                "anomaly," +
                "polity," +
                "worldType," +
                "fuelType," +
                "portType," +
                "populationType," +
                "techType," +
                "productType," +
                "milSpaceType," +
                "milPlanType," +
                "miscText1," +
                "miscText2," +
                "miscText3," +
                "miscText4," +
                "miscText5," +
                "miscNum1," +
                "miscNum2," +
                "miscNum3," +
                "miscNum4," +
                "miscNum5," +
                "notes" +
                "\n";
    }

    private String convertToCSV(AstrographicObject astrographicObject) {

        return removeCommas(astrographicObject.getId().toString()) + ", " +
                removeCommas(astrographicObject.getDataSetName()) + ", " +
                removeCommas(astrographicObject.getDisplayName()) + ", " +
                removeCommas(astrographicObject.getConstellationName()) + ", " +
                astrographicObject.getMass() + ", " +
                astrographicObject.getActualMass() + ", " +
                removeCommas(astrographicObject.getSource()) + ", " +
                astrographicObject.getCatalogIdList() + ", " +
                astrographicObject.getX() + ", " +
                astrographicObject.getY() + ", " +
                astrographicObject.getY() + ", " +
                astrographicObject.getRadius() + ", " +
                astrographicObject.getRa() + ", " +
                astrographicObject.getPmra() + ", " +
                astrographicObject.getDeclination() + ", " +
                astrographicObject.getPmdec() + ", " +
                astrographicObject.getDec_deg() + ", " +
                astrographicObject.getRs_cdeg() + ", " +
                astrographicObject.getParallax() + ", " +
                astrographicObject.getDistance() + ", " +
                astrographicObject.getRadialVelocity() + ", " +
                astrographicObject.getSpectralClass() + ", " +
                astrographicObject.getOrthoSpectralClass() + ", " +
                astrographicObject.getTemperature() + ", " +
                astrographicObject.isRealStar() + ", " +
                astrographicObject.getBprp() + ", " +
                astrographicObject.getBpg() + ", " +
                astrographicObject.getGrp() + ", " +
                astrographicObject.getLuminosity() + ", " +
                astrographicObject.getMagu() + ", " +
                astrographicObject.getMagb() + ", " +
                astrographicObject.getMagv() + ", " +
                astrographicObject.getMagr() + ", " +
                astrographicObject.getMagi() + ", " +
                astrographicObject.isOther() + ", " +
                astrographicObject.isAnomaly() + ", " +
                astrographicObject.getPolity() + ", " +
                astrographicObject.getWorldType() + ", " +
                astrographicObject.getFuelType() + ", " +
                astrographicObject.getPortType() + ", " +
                astrographicObject.getPopulationType() + ", " +
                astrographicObject.getTechType() + ", " +
                astrographicObject.getProductType() + ", " +
                astrographicObject.getMilSpaceType() + ", " +
                astrographicObject.getMilPlanType() + ", " +
                removeCommas(astrographicObject.getMiscText1()) + ", " +
                removeCommas(astrographicObject.getMiscText2()) + ", " +
                removeCommas(astrographicObject.getMiscText3()) + ", " +
                removeCommas(astrographicObject.getMiscText4()) + ", " +
                removeCommas(astrographicObject.getMiscText5()) + ", " +
                astrographicObject.getMiscNum1() + ", " +
                astrographicObject.getMiscNum2() + ", " +
                astrographicObject.getMiscNum3() + ", " +
                astrographicObject.getMiscNum4() + ", " +
                astrographicObject.getMiscNum5() + ", " +
                removeCommas(astrographicObject.getNotes()) +
                "\n";
    }

    private String removeCommas(String origin) {
        String replaced = origin;
        if (origin != null) {
            replaced = origin.replace(",", " ");
        }
        return replaced;
    }

}
