package com.teamgannon.trips.file.excel.normal;

import com.teamgannon.trips.dialogs.dataset.Dataset;
import com.teamgannon.trips.file.csvin.model.AstroCSVStar;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.service.importservices.tasks.ProgressUpdater;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ExcelReader {

    // Create a DataFormatter to format and get each cell's value as String
    private final DataFormatter dataFormatter = new DataFormatter();

    public static void main(String[] args) {
        ExcelReader excelReader = new ExcelReader();
        ProgressUpdater progressUpdater = new ProgressUpdater() {
            @Override
            public void updateLoadInfo(String message) {
                log.info(message);
            }
        };

        Dataset dataSet = new Dataset();
        dataSet.setName("MyName");

        ExcelFile excelFile = excelReader.loadFile(progressUpdater, dataSet, new File("/Users/larrymitchell/tripsnew/trips/files/larryx.trips.xlsx"));
        log.info("done");

    }

    /**
     * load the excel file
     *
     * @param loadUpdater the load updater
     * @param dataSet
     * @param file        the excel file
     */
    public @NotNull ExcelFile loadFile(ProgressUpdater loadUpdater, Dataset dataSet, @NotNull File file) {

        ExcelFile excelFile = new ExcelFile();
        excelFile.setFileName(file.getAbsolutePath());
        excelFile.setAuthor("anonymous");

        // Creating a Workbook from an Excel file (.xls or .xlsx)
        Workbook workbook = null;
        try {

            workbook = WorkbookFactory.create(file);

            // Retrieving the number of sheets in the Workbook
            log.info("Workbook has " + workbook.getNumberOfSheets() + " Sheets : ");

            DataSetDescriptor descriptor = extractDataDescriptor(dataSet, workbook);
            excelFile.setDescriptor(descriptor);
            loadUpdater.updateLoadInfo("descriptor parsed");

            Sheet dataSheet = workbook.getSheet("data");
            if (dataSheet == null) {
                dataSheet = workbook.getSheet("Sheet1");
            }

            List<StarObject> starObjectList = parseSheet(dataSet.getName(), dataSheet);

            descriptor.setNumberStars((long) starObjectList.size());

            excelFile.setStarObjects(starObjectList);
            loadUpdater.updateLoadInfo("star data parsed");

            // close the workbook
            workbook.close();

            return excelFile;

        } catch (IOException e) {
            log.error("Failed to read {} because of ", file.getName(), e);
            return excelFile;
        }

    }

    private DataSetDescriptor extractDataDescriptor(Dataset dataSet, Workbook workbook) {
        DataSetDescriptor descriptor = new DataSetDescriptor();
        Sheet descriptorSheet = workbook.getSheet("descriptor");
        if (descriptorSheet != null) {
            Row descriptorRow = descriptorSheet.getRow(1);

            descriptor.setDataSetName(dataSet.getName());
            descriptor.setFilePath(getCell(descriptorRow, 1));
            descriptor.setFileCreator(getCell(descriptorRow, 2));
            descriptor.setFileOriginalDate(parseLong(getCell(descriptorRow, 3)));
            descriptor.setFileNotes(getCell(descriptorRow, 4));
            descriptor.setDatasetType(getCell(descriptorRow, 5));
            descriptor.setNumberStars(parseLong(getCell(descriptorRow, 6)));
            descriptor.setDistanceRange(parseDouble(getCell(descriptorRow, 7)));
            descriptor.setNumberRoutes(parseInt(getCell(descriptorRow, 8)));
            descriptor.setThemeStr(getCell(descriptorRow, 9));
            descriptor.setRoutesStr(getCell(descriptorRow, 11));
            descriptor.setCustomDataDefsStr(getCell(descriptorRow, 12));
            descriptor.setCustomDataValuesStr(getCell(descriptorRow, 13));
        } else {
            descriptor.setDataSetName(dataSet.getName());
            descriptor.setFileCreator(dataSet.getAuthor());
            descriptor.setFileNotes(dataSet.getNotes());
            descriptor.setFilePath(dataSet.getFileSelected());
            descriptor.setDatasetType(dataSet.getDataType().toString());
        }

        return descriptor;
    }

    /**
     * parse a workbook sheet
     *
     * @param datasetName the new datasetname
     * @param sheet       the sheet to parse
     * @return the parsed stars
     */
    private List<StarObject> parseSheet(@NotNull String datasetName, @NotNull Sheet sheet) {
        log.info("=> " + sheet.getSheetName());

        List<StarObject> starList = new ArrayList<>();

        // first row is the headers
        boolean firstRow = true;

        for (Row row : sheet) {
            if (firstRow) {
                firstRow = false;
                continue;
            }
            AstroCSVStar star = AstroCSVStar.builder().build();

            // note that this format is very specific and if the fields are not in this order then
            // parsing will return crap
            int cell = 2;

            star.setDatasetName(datasetName);
            star.setDisplayName(getCell(row, cell++));
            star.setConstellationName(getCell(row, cell++));
            star.setMass(getCell(row, cell++));
            star.setActualMass(getCell(row, cell++));
            star.setSource(getCell(row, cell++));
            star.setCatalogIdList(getCell(row, cell++));

            star.setX(getCell(row, cell++));
            star.setY(getCell(row, cell++));
            star.setZ(getCell(row, cell++));

            star.setRadius(getCell(row, cell++));
            star.setRa(getCell(row, cell++));
            star.setPmra(getCell(row, cell++));
            star.setDeclination(getCell(row, cell++));
            star.setPmdec(getCell(row, cell++));
            star.setDec_deg(getCell(row, cell++));
            star.setRs_cdeg(getCell(row, cell++));

            star.setParallax(getCell(row, cell++));
            star.setDistance(getCell(row, cell++));
            star.setRadialVelocity(getCell(row, cell++));
            star.setSpectralClass(getCell(row, cell++));
            star.setOrthoSpectralClass(getCell(row, cell++));

            star.setTemperature(getCell(row, cell++));
            star.setRealStar(getCell(row, cell++));

            star.setBprp(getCell(row, cell++));
            star.setBpg(getCell(row, cell++));
            star.setGrp(getCell(row, cell++));

            star.setLuminosity(getCell(row, cell++));

            star.setMagu(getCell(row, cell++));
            star.setMagb(getCell(row, cell++));
            star.setMagv(getCell(row, cell++));
            star.setMagr(getCell(row, cell++));
            star.setMagi(getCell(row, cell++));

            star.setOther(getCell(row, cell++));
            star.setAnomaly(getCell(row, cell++));

            star.setPolity(getCell(row, cell++));
            star.setWorldType(getCell(row, cell++));
            star.setFuelType(getCell(row, cell++));
            star.setPortType(getCell(row, cell++));
            star.setPopulationType(getCell(row, cell++));
            star.setTechType(getCell(row, cell++));
            star.setProductType(getCell(row, cell++));
            star.setMilSpaceType(getCell(row, cell++));
            star.setMilPlanType(getCell(row, cell++));

            star.setMiscText1(getCell(row, cell++));
            star.setMiscText2(getCell(row, cell++));
            star.setMiscText3(getCell(row, cell++));
            star.setMiscText4(getCell(row, cell++));
            star.setMiscText5(getCell(row, cell++));

            star.setMiscNum1(parseDouble(getCell(row, cell++)));
            star.setMiscNum2(parseDouble(getCell(row, cell++)));
            star.setMiscNum3(parseDouble(getCell(row, cell++)));
            star.setMiscNum4(parseDouble(getCell(row, cell++)));

            double miscNum5 = parseDouble(getCell(row, cell++));
            star.setMiscNum5(miscNum5);

            String notes = getCell(row, cell);
            star.setNotes(notes);
//            System.out.println(star);

            StarObject starObject = star.toAstrographicObject();
            if (starObject != null) {
                starList.add(star.toAstrographicObject());
            }
        }

        log.debug("Parsed {} stars", starList.size());
        return starList;

    }

    private String getCell(@NotNull Row row, int cellNumber) {
        return dataFormatter.formatCellValue(row.getCell(cellNumber));
    }

    private long parseLong(String val) {
        try {
            return Long.parseLong(val);
        } catch (Exception e) {
            return 0L;
        }
    }

    private int parseInt(String val) {
        try {
            return Integer.parseInt(val);
        } catch (Exception e) {
            return 0;
        }
    }

    private double parseDouble(String val) {
        try {
            return Double.parseDouble(val);
        } catch (Exception e) {
            return 0;
        }
    }

}
