package com.teamgannon.trips.service;

import com.teamgannon.trips.jpa.model.AstrographicObject;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.listener.StatusUpdaterListener;
import javafx.application.Platform;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.teamgannon.trips.support.AlertFactory.*;

@Slf4j
@Service
public class DataImportService {

    private final DatabaseManagementService databaseManagementService;
    private final StatusUpdaterListener updaterListener;

    public DataImportService(DatabaseManagementService databaseManagementService,
                             StatusUpdaterListener updaterListener) {
        this.databaseManagementService = databaseManagementService;
        this.updaterListener = updaterListener;
    }

    public void loadMultipleDatasets() {
        Optional<ButtonType> result = showConfirmationAlert(
                "Data Import Service", "Database load",
                "Do you want to export this database? It will take a while, and if any of " +
                        "these datasets already exist then they will not be loaded.");
        if ((result.isPresent()) && (result.get() == ButtonType.OK)) {

            final FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select excel file comtaining datasets");
            fileChooser.setInitialDirectory(new File("."));
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Trips Excel Files", "*.trips.xlsx")
            );
            File file = fileChooser.showOpenDialog(null);
            if (file != null) {
                loadDBFile(file);
            } else {
                log.warn("file import cancelled");
                showInfoMessage("Import database", "Cancelled request to import");
            }
        }
    }

    private void loadDBFile(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            // create a work book
            XSSFWorkbook myWorkBook = new XSSFWorkbook(fis);
            List<DataSetDescriptor> dataSetDescriptorList = getDatasets(myWorkBook);
            if (dataSetDescriptorList.size() > 0) {
                for (DataSetDescriptor descriptor : dataSetDescriptorList) {
                    XSSFSheet mySheet = myWorkBook.getSheet(descriptor.getDataSetName());
                    if (mySheet!=null) {
                        extractDataset(mySheet);
                    }
                }

                myWorkBook.close();
                fis.close();
                updateStatus("Export of database complete");

                showInfoMessage("Database Export", file.getAbsolutePath() + " was imported");
            } else {
                showErrorAlert(
                        "Import Databse from an Excel file",
                        "There are no dataset definitions in this file. Failed to Import");
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.error("caught error opening the file:{}", e.getMessage());
            showErrorAlert(
                    "Import Database from an Excel file",
                    "Database failed to be imported:" + e.getMessage());
        }

    }

    private List<DataSetDescriptor> getDatasets(XSSFWorkbook myWorkBook) {
        try {
            List<DataSetDescriptor> dataSetDescriptorList = new ArrayList<>();
            XSSFSheet mySheet = myWorkBook.getSheet("Database");
            for (Row currentRow : mySheet) {
                DataSetDescriptor descriptor = new DataSetDescriptor();
                int column = 0;
                String datasetname = readCell(currentRow, column++);
                if (datasetname.equals("Dataset name")) {
                    continue;
                }
                descriptor.setDataSetName(datasetname);
                String fileCreator = readCell(currentRow, column++);
                descriptor.setFileCreator(fileCreator);
                String originalDate = readCell(currentRow, column++);
                descriptor.setFileOriginalDate(Long.parseLong(originalDate));
                String fileNotes = readCell(currentRow, column++);
                descriptor.setFileNotes(fileNotes);
                String datasetType = readCell(currentRow, column++);
                descriptor.setDatasetType(datasetType);
                String numberStars = readCell(currentRow, column++);
                descriptor.setNumberStars(Long.parseLong(numberStars));
                String distanceRange = readCell(currentRow, column++);
                descriptor.setDistanceRange(Double.parseDouble(distanceRange));
                String routeStr = readCell(currentRow, column);
                if (routeStr.isBlank()) {
                    descriptor.setRoutesStr(null);
                }else {
                    descriptor.setRoutesStr(routeStr);
                }

                // save dataset
                databaseManagementService.createDataSet(descriptor);
                dataSetDescriptorList.add(descriptor);
            }
            return dataSetDescriptorList;
        } catch (Exception e) {
            log.error("Was unable to read the datasets");
            return new ArrayList<>();
        }
    }

    private String readCell(Row currentRow, int column) {
        Cell cell = currentRow.getCell(column);
        return cell.getStringCellValue();
    }

    private void extractDataset(XSSFSheet mySheet) {
        updateStatus(String.format("starting import of %s dataset", mySheet.getSheetName()));
        List<AstrographicObject> astrographicObjectList = new ArrayList<>();
        for (Row row : mySheet) {
            AstrographicObject astrographicObject = loadRow(row);
            if (astrographicObject != null) {
                astrographicObjectList.add(astrographicObject);
            }
        }
        databaseManagementService.addStars(astrographicObjectList);
    }

    private AstrographicObject loadRow(Row row) {
        try {
            AstrographicObject object = new AstrographicObject();
            int column = 0;

            String uuid = readCell(row, column++);
            object.setId(UUID.fromString(uuid));

            String datasetName = readCell(row, column++);
            object.setDataSetName(datasetName);

            String displayName = readCell(row, column++);
            object.setDisplayName(displayName);

            String constellationName = readCell(row, column++);
            object.setConstellationName(constellationName);

            String mass = readCell(row, column++);
            object.setMass(Double.parseDouble(mass));

            String actualMass = readCell(row, column++);
            object.setActualMass(Double.parseDouble(actualMass));

            String source = readCell(row, column++);
            object.setSource(source);

            String catalogIdList = readCell(row, column++);
            List<String> catList = new ArrayList<>();
            catList.add(catalogIdList);
            object.setCatalogIdList(catList);

            String X = readCell(row, column++);
            object.setX(Double.parseDouble(X));

            String Y = readCell(row, column++);
            object.setY(Double.parseDouble(Y));

            String Z = readCell(row, column++);
            object.setZ(Double.parseDouble(Z));

            String radius = readCell(row, column++);
            object.setRadius(Double.parseDouble(radius));

            String ra = readCell(row, column++);
            object.setRa(Double.parseDouble(ra));

            String pmra = readCell(row, column++);
            object.setPmra(Double.parseDouble(pmra));

            String declination = readCell(row, column++);
            object.setDeclination(Double.parseDouble(declination));

            String pmdec = readCell(row, column++);
            object.setPmdec(Double.parseDouble(pmdec));

            String dec_deg = readCell(row, column++);
            object.setDec_deg(Double.parseDouble(dec_deg));

            String rs_deg = readCell(row, column++);
            object.setRs_cdeg(Double.parseDouble(rs_deg));

            String parallax = readCell(row, column++);
            object.setParallax(Double.parseDouble(parallax));

            String distance = readCell(row, column++);
            object.setDistance(Double.parseDouble(distance));

            String radialVelocity = readCell(row, column++);
            object.setRadialVelocity(Double.parseDouble(radialVelocity));

            String spectralClass = readCell(row, column++);
            object.setSpectralClass(spectralClass);

            String orthoSpectralClass = readCell(row, column++);
            object.setOrthoSpectralClass(orthoSpectralClass);

            String temperature = readCell(row, column++);
            object.setTemperature(Double.parseDouble(temperature));

            String realStar = readCell(row, column++);
            object.setRealStar(Boolean.parseBoolean(realStar));

            String bprp = readCell(row, column++);
            object.setBprp(Double.parseDouble(bprp));

            String bpg = readCell(row, column++);
            object.setBpg(Double.parseDouble(bpg));

            String grp = readCell(row, column++);
            object.setGrp(Double.parseDouble(grp));

            String luminosity = readCell(row, column++);
            object.setLuminosity(luminosity);

            String magu = readCell(row, column++);
            object.setMagu(Double.parseDouble(magu));

            String magb = readCell(row, column++);
            object.setMagb(Double.parseDouble(magb));

            String magv = readCell(row, column++);
            object.setMagr(Double.parseDouble(magv));

            String magr = readCell(row, column++);
            object.setRadius(Double.parseDouble(magr));

            String magi = readCell(row, column++);
            object.setMagi(Double.parseDouble(magi));

            String other = readCell(row, column++);
            object.setOther(Boolean.parseBoolean(other));

            String anomaly = readCell(row, column++);
            object.setAnomaly(Boolean.parseBoolean(anomaly));

            String polity = readCell(row, column++);
            object.setPolity(polity);

            String worldType = readCell(row, column++);
            object.setWorldType(worldType);

            String fuelType = readCell(row, column++);
            object.setFuelType(fuelType);

            String portType = readCell(row, column++);
            object.setPortType(portType);

            String populationType = readCell(row, column++);
            object.setPopulationType(populationType);

            String techType = readCell(row, column++);
            object.setTechType(techType);

            String productType = readCell(row, column++);
            object.setProductType(productType);

            String milSpaceType = readCell(row, column++);
            object.setMilSpaceType(milSpaceType);

            String milPlanType = readCell(row, column++);
            object.setMilPlanType(milPlanType);

            String miscText1 = readCell(row, column++);
            object.setMiscText1(miscText1);

            String miscText2 = readCell(row, column++);
            object.setMiscText2(miscText2);

            String miscText3 = readCell(row, column++);
            object.setMiscText3(miscText3);

            String miscText4 = readCell(row, column++);
            object.setMiscText4(miscText4);

            String miscText5 = readCell(row, column++);
            object.setMiscText5(miscText5);

            String miscNum1 = readCell(row, column++);
            object.setMiscNum1(Double.parseDouble(miscNum1));

            String miscNum2 = readCell(row, column++);
            object.setMiscNum2(Double.parseDouble(miscNum2));

            String miscNum3 = readCell(row, column++);
            object.setMiscNum3(Double.parseDouble(miscNum3));

            String miscNum4 = readCell(row, column++);
            object.setMiscNum4(Double.parseDouble(miscNum4));

            String miscNum5 = readCell(row, column++);
            object.setMiscNum5(Double.parseDouble(miscNum5));

            String notes = readCell(row, column);
            object.setNotes(notes);


            return object;
        } catch (Exception e) {
            log.error("rejected row");
            return null;
        }
    }


    private void updateStatus(String status) {
        new Thread(() -> Platform.runLater(() -> {
            log.info(status);
            updaterListener.updateStatus(status);
        })).start();
    }

}
