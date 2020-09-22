package com.teamgannon.trips.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamgannon.trips.dialogs.dataset.ExportOptions;
import com.teamgannon.trips.jpa.model.AstrographicObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;
import static com.teamgannon.trips.support.AlertFactory.showInfoMessage;

/**
 * Used to import and export data sets
 * <p>
 * Created by larrymitchell on 2017-01-22.
 */
@Slf4j
@Service
public class DataExportService {

    private DatabaseManagementService databaseManagementService;

    public DataExportService(DatabaseManagementService databaseManagementService) {
        this.databaseManagementService = databaseManagementService;
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
            Writer writer = Files.newBufferedWriter(Paths.get(export.getFileName() + ".json"));

            String jsonStr = Obj.writeValueAsString(astrographicObjects);
            writer.write(jsonStr);

            writer.flush();
            writer.close();
            showInfoMessage("Database Export", export.getDataset().getDataSetName()
                    + " was export to " + export.getFileName() + ".json");

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

            File myFile = new File(export.getFileName() + ".xlsx");
            if (!myFile.createNewFile()) {
                showErrorAlert(
                        "Export Dataset as JSON file",
                        "Unable to create file: " + export.getFileName());
            }
            FileInputStream fis = new FileInputStream(myFile);

            // create a work book
            XSSFWorkbook myWorkBook = new XSSFWorkbook();

            // create a work sheet with the dataset name on it
            XSSFSheet mySheet = myWorkBook.createSheet(export.getDataset().getDataSetName());

            int rowCount = 0;
            for (AstrographicObject astrographicObject : astrographicObjects) {
                Row row = mySheet.createRow(++rowCount);
                saveRow(row, astrographicObject);
            }

            FileOutputStream os = new FileOutputStream(myFile);
            myWorkBook.write(os);

            // close the work book
            myWorkBook.close();

            showInfoMessage("Database Export", export.getDataset().getDataSetName()
                    + " was exported to " + export.getFileName() + ".xlsx");


        } catch (Exception e) {
            e.printStackTrace();
            log.error("caught error opening the file:{}", e.getMessage());
            showErrorAlert(
                    "Export Dataset as JSON file",
                    export.getDataset().getDataSetName() +
                            "failed to exported:" + e.getMessage());
        }
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
            Writer writer = Files.newBufferedWriter(Paths.get(export.getFileName() + ".csv"));

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
                    + " was exported to " + export.getFileName() + ".csv");

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
