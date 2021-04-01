package com.teamgannon.trips.file.excel.normal;

import com.monitorjbl.xlsx.StreamingReader;
import com.teamgannon.trips.dialogs.dataset.Dataset;
import com.teamgannon.trips.file.csvin.model.AstroCSVStar;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.importservices.tasks.ProgressUpdater;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class ExcelReader {

    // to load on demand
    private DatabaseManagementService databaseManagementService;


    /**
     * load the excel file
     *
     * @param loadUpdater               the load updater
     * @param dataSet                   the dataset to load
     * @param file                      the excel file
     * @param databaseManagementService the database management service
     */
    public @NotNull ExcelFile loadFile(ProgressUpdater loadUpdater, Dataset dataSet, @NotNull File file, DatabaseManagementService databaseManagementService) {

        this.databaseManagementService = databaseManagementService;

        ExcelFile excelFile = new ExcelFile();
        excelFile.setFileName(file.getAbsolutePath());
        excelFile.setAuthor(dataSet.getAuthor());
        return useStreamingAPI(loadUpdater, dataSet, file, excelFile);
    }

    private ExcelFile useStreamingAPI(ProgressUpdater loadUpdater, Dataset dataSet, File file, ExcelFile excelFile) {

        try (
                InputStream is = new FileInputStream(file);
                Workbook workbook = StreamingReader.builder()
                        .rowCacheSize(100) // number of rows to keep in memory (defaults to 10)
                        .bufferSize(4096) // buffer size to use when reading InputStream to file (defaults to 1024)
                        .open(is)) // InputStream or File for XLSX file (required)
        {

            Set<StarObject> starSet = new HashSet<>();

            // first row is the headers
            boolean firstRow = true;

            long totalCount = 0;
            int rejectCount = 0;
            double maxDistance = 0.0;

            DataSetDescriptor descriptor = createDescriptor(dataSet);
            excelFile.setDescriptor(descriptor);

            Sheet dataSheet = null;
            for (Sheet sheet : workbook) {
                if (sheet != null) {
                    dataSheet = sheet;
                }
            }
            if (dataSheet == null) {
                return excelFile;
            }

            int i = 1;
            for (Row row : dataSheet) {
                if (row == null) {
                    if (!starSet.isEmpty()) {
                        databaseManagementService.starBulkSave(starSet);
                        loadUpdater.updateTaskInfo(totalCount + " stars loaded");
                        totalCount += starSet.size();
                        starSet.clear();
                    }
                    break;
                }

                // skip first row always
                if (firstRow) {
                    firstRow = false;
                    continue;
                }

                // build a star
                AstroCSVStar star = extractStar1(dataSet.getName(), row);
                if (star.getDisplayName().isEmpty()) {
                    rejectCount++;
                    continue;
                }

                StarObject starObject = star.toStarObject();
                if (starObject != null) {
                    starSet.add(star.toStarObject());
                } else {
                    rejectCount++;
                    continue;
                }

                if (starObject.getDistance() > maxDistance) {
                    maxDistance = starObject.getDistance();
                }

                if ((i % 2000) == 0) {
                    log.info("about to save 1000 stars");
                    totalCount += starSet.size();
                    databaseManagementService.starBulkSave(starSet);
                    starSet.clear();
                    log.info(totalCount + " stars loaded so far");
                    loadUpdater.updateTaskInfo(totalCount + " stars loaded so far");
                }

                log.debug("Parsed {} stars", starSet.size());
                i++;
            }

            if (!starSet.isEmpty()) {
                databaseManagementService.starBulkSave(starSet);
                loadUpdater.updateTaskInfo(totalCount + " stars loaded");
                totalCount += starSet.size();
            }

            excelFile.getDescriptor().setNumberStars(totalCount);
            excelFile.getDescriptor().setDistanceRange(maxDistance);

            return excelFile;
        } catch (IOException e) {
            log.error("failed to read file:" + e.getMessage());
        }
        return excelFile;
    }

    private AstroCSVStar extractStar1(String name, Row row) {
        List<String> fieldList = new ArrayList<>();
        for (Cell c : row) {
            fieldList.add(c.getStringCellValue().trim());
        }
        int i = 2;
        AstroCSVStar star = AstroCSVStar.builder().build();
        // fill
        star.setDatasetName(name);
        star.setDisplayName(getValue(fieldList, i++, ""));
        star.setCommonName(getValue(fieldList, i++, ""));
        star.setSimbadId(getValue(fieldList, i++, ""));
        star.setGaiaId(getValue(fieldList, i++, ""));
        star.setConstellationName(getValue(fieldList, i++, ""));
        star.setMass(getValue(fieldList, i++, "0.0"));
        star.setAge(getValue(fieldList, i++, "0.0"));
        star.setMetallicity(getValue(fieldList, i++, "0.0"));
        star.setSource(getValue(fieldList, i++, ""));
        star.setCatalogIdList(getValue(fieldList, i++, ""));

        star.setX(getValue(fieldList, i++, "0.0"));
        star.setY(getValue(fieldList, i++, "0.0"));
        star.setZ(getValue(fieldList, i++, "0.0"));

        star.setRadius(getValue(fieldList, i++, "0.0"));
        star.setRa(getValue(fieldList, i++, "0.0"));
        star.setPmra(getValue(fieldList, i++, "0.0"));
        star.setDeclination(getValue(fieldList, i++, "0.0"));
        star.setPmdec(getValue(fieldList, i++, "0.0"));

        star.setParallax(getValue(fieldList, i++, "0.0"));
        star.setDistance(getValue(fieldList, i++, "0.0"));
        star.setRadialVelocity(getValue(fieldList, i++, "0.0"));
        star.setSpectralClass(getValue(fieldList, i++, ""));
        star.setOrthoSpectralClass(getValue(fieldList, i++, ""));

        star.setTemperature(getValue(fieldList, i++, "0.0"));
        star.setRealStar(getValue(fieldList, i++, "0.0"));

        star.setBprp(getValue(fieldList, i++, "0.0"));
        star.setBpg(getValue(fieldList, i++, "0.0"));
        star.setGrp(getValue(fieldList, i++, "0.0"));

        star.setLuminosity(getValue(fieldList, i++, ""));

        star.setMagu(getValue(fieldList, i++, "0.0"));
        star.setMagb(getValue(fieldList, i++, "0.0"));
        star.setMagv(getValue(fieldList, i++, "0.0"));
        star.setMagr(getValue(fieldList, i++, "0.0"));
        star.setMagi(getValue(fieldList, i++, "0.0"));

        star.setOther(getValue(fieldList, i++, "false"));
        star.setAnomaly(getValue(fieldList, i++, "false"));

        star.setPolity(getValue(fieldList, i++, "NA"));
        star.setWorldType(getValue(fieldList, i++, "NA"));
        star.setFuelType(getValue(fieldList, i++, "NA"));
        star.setPortType(getValue(fieldList, i++, "NA"));
        star.setPopulationType(getValue(fieldList, i++, "NA"));
        star.setTechType(getValue(fieldList, i++, "NA"));
        star.setProductType(getValue(fieldList, i++, "NA"));
        star.setMilSpaceType(getValue(fieldList, i++, "NA"));
        star.setMilPlanType(getValue(fieldList, i++, "NA"));

        star.setMiscText1(getValue(fieldList, i++, "none"));
        star.setMiscText2(getValue(fieldList, i++, "none"));
        star.setMiscText3(getValue(fieldList, i++, "none"));
        star.setMiscText4(getValue(fieldList, i++, "none"));
        star.setMiscText5(getValue(fieldList, i++, "none"));

        star.setMiscNum1(parseDouble(getValue(fieldList, i++, "0.0")));
        star.setMiscNum2(parseDouble(getValue(fieldList, i++, "0.0")));
        star.setMiscNum3(parseDouble(getValue(fieldList, i++, "0.0")));
        star.setMiscNum4(parseDouble(getValue(fieldList, i++, "0.0")));

        double miscNum5 = parseDouble(getValue(fieldList, i++, "0.0"));
        star.setMiscNum5(miscNum5);
        String notes = getValue(fieldList, i++, "none");
        star.setNotes(notes);

        star.setGalacticLattitude(getValue(fieldList, i++, "0.0"));
        star.setGalacticLongitude(getValue(fieldList, i, "0.0"));
        return star;
    }

    public String getValue(List<String> fields, int i, String optional) {
        if (i >= fields.size()) {
            return optional;
        } else {
            String value = fields.get(i).trim();
            if (value.isEmpty()) {
                return optional;
            } else {
                return value;
            }
        }
    }

    private DataSetDescriptor createDescriptor(Dataset dataSet) {
        DataSetDescriptor descriptor = new DataSetDescriptor();
        descriptor.setDataSetName(dataSet.getName());
        descriptor.setFileCreator(dataSet.getAuthor());
        descriptor.setFileNotes(dataSet.getNotes());
        descriptor.setFilePath(dataSet.getFileSelected());
        descriptor.setDatasetType(dataSet.getDataType().getDataFormatEnum().getValue());
        return descriptor;
    }

    private double parseDouble(String val) {
        try {
            return Double.parseDouble(val);
        } catch (Exception e) {
            return 0;
        }
    }

}
