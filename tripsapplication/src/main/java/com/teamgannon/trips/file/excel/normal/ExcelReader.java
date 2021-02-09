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
                        loadUpdater.updateLoadInfo(totalCount + " stars loaded");
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

                StarObject starObject = star.toAstrographicObject();
                if (starObject != null) {
                    starSet.add(star.toAstrographicObject());
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
                    loadUpdater.updateLoadInfo(totalCount + " stars loaded so far");
                }

                log.debug("Parsed {} stars", starSet.size());
                i++;
            }

            if (!starSet.isEmpty()) {
                databaseManagementService.starBulkSave(starSet);
                loadUpdater.updateLoadInfo(totalCount + " stars loaded");
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
        star.setDisplayName(getValue(fieldList, i++));
        star.setCommonName(getValue(fieldList, i++));
        star.setSimbadId(getValue(fieldList, i++));
        star.setGaiaId(getValue(fieldList, i++));
        star.setConstellationName(getValue(fieldList, i++));
        star.setMass(getValue(fieldList, i++));
        star.setAge(getValue(fieldList, i++));
        star.setMetallicity(getValue(fieldList, i++));
        star.setSource(getValue(fieldList, i++));
        star.setCatalogIdList(getValue(fieldList, i++));

        star.setX(getValue(fieldList, i++));
        star.setY(getValue(fieldList, i++));
        star.setZ(getValue(fieldList, i++));

        star.setRadius(getValue(fieldList, i++));
        star.setRa(getValue(fieldList, i++));
        star.setPmra(getValue(fieldList, i++));
        star.setDeclination(getValue(fieldList, i++));
        star.setPmdec(getValue(fieldList, i++));

        star.setParallax(getValue(fieldList, i++));
        star.setDistance(getValue(fieldList, i++));
        star.setRadialVelocity(getValue(fieldList, i++));
        star.setSpectralClass(getValue(fieldList, i++));
        star.setOrthoSpectralClass(getValue(fieldList, i++));

        star.setTemperature(getValue(fieldList, i++));
        star.setRealStar(getValue(fieldList, i++));

        star.setBprp(getValue(fieldList, i++));
        star.setBpg(getValue(fieldList, i++));
        star.setGrp(getValue(fieldList, i++));

        star.setLuminosity(getValue(fieldList, i++));

        star.setMagu(getValue(fieldList, i++));
        star.setMagb(getValue(fieldList, i++));
        star.setMagv(getValue(fieldList, i++));
        star.setMagr(getValue(fieldList, i++));
        star.setMagi(getValue(fieldList, i++));

        star.setOther(getValue(fieldList, i++));
        star.setAnomaly(getValue(fieldList, i++));

        star.setPolity(getValue(fieldList, i++));
        star.setWorldType(getValue(fieldList, i++));
        star.setFuelType(getValue(fieldList, i++));
        star.setPortType(getValue(fieldList, i++));
        star.setPopulationType(getValue(fieldList, i++));
        star.setTechType(getValue(fieldList, i++));
        star.setProductType(getValue(fieldList, i++));
        star.setMilSpaceType(getValue(fieldList, i++));
        star.setMilPlanType(getValue(fieldList, i++));

        star.setMiscText1(getValue(fieldList, i++));
        star.setMiscText2(getValue(fieldList, i++));
        star.setMiscText3(getValue(fieldList, i++));
        star.setMiscText4(getValue(fieldList, i++));
        star.setMiscText5(getValue(fieldList, i++));

        star.setMiscNum1(parseDouble(getValue(fieldList, i++)));
        star.setMiscNum2(parseDouble(getValue(fieldList, i++)));
        star.setMiscNum3(parseDouble(getValue(fieldList, i++)));
        star.setMiscNum4(parseDouble(getValue(fieldList, i++)));

        double miscNum5 = parseDouble(getValue(fieldList, i++));
        star.setMiscNum5(miscNum5);
        String notes = getValue(fieldList, i++);
        star.setNotes(notes);

        star.setGalacticLattitude(getValue(fieldList, i++));
        star.setGalacticLongitude(getValue(fieldList, i));
        return star;
    }

    public String getValue(List<String> fields, int i) {
        return i >= fields.size() ? "" : fields.get(i).trim();
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
