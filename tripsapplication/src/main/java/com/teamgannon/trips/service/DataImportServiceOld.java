package com.teamgannon.trips.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamgannon.trips.dialogs.dataset.Dataset;
import com.teamgannon.trips.dialogs.dataset.FileProcessResult;
import com.teamgannon.trips.dialogs.dataset.LoadUpdater;
import com.teamgannon.trips.file.chview.ChviewReader;
import com.teamgannon.trips.file.chview.model.ChViewFile;
import com.teamgannon.trips.file.csvin.RBCsvFile;
import com.teamgannon.trips.file.csvin.RBCsvReader;
import com.teamgannon.trips.file.excel.rb.RBExcelReader;
import com.teamgannon.trips.file.excel.rb.RBExcelFile;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.listener.DataSetChangeListener;
import com.teamgannon.trips.listener.StatusUpdaterListener;
import javafx.application.Platform;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static com.teamgannon.trips.support.AlertFactory.*;

@Slf4j
public class DataImportServiceOld {

    private Stage stage;
    private final DatabaseManagementService databaseManagementService;
    private final @NotNull RBCsvReader rbCsvReader;
    private final DataSetChangeListener dataSetChangeListener;
    private final @NotNull ChviewReader chviewReader;
    private final StatusUpdaterListener statusUpdaterListener;
    private final @NotNull RBExcelReader RBExcelReader;
    private LoadUpdater loadUpdater;

    /**
     * constructor
     *
     * @param databaseManagementService the database service
     * @param statusUpdaterListener     the updater
     */
    public DataImportServiceOld(Stage stage,
                                DatabaseManagementService databaseManagementService,
                                StatusUpdaterListener statusUpdaterListener,
                                DataSetChangeListener dataSetChangeListener
    ) {
        this.stage = stage;

        this.databaseManagementService = databaseManagementService;
        this.dataSetChangeListener = dataSetChangeListener;
        this.statusUpdaterListener = statusUpdaterListener;

        // define readers
        this.rbCsvReader = new RBCsvReader(databaseManagementService);
        this.chviewReader = new ChviewReader();
        this.RBExcelReader = new RBExcelReader();
    }

    /**
     * process the file provided
     *
     * @param dataset the defined dataset
     */
    public boolean processFileType(@NotNull Dataset dataset) {
        this.loadUpdater = loadUpdater;
        FileProcessResult result;
        // this is a CH View import format
        // this is Excel format that follows a specification from the Rick Boatwright format
        // this is a database import
        // this is Simbad database import format
        switch (dataset.getDataType().getSuffix()) {
            case "chv" -> {
                result = processCHViewFile(dataset);
                if (result.isSuccess()) {
                    this.dataSetChangeListener.addDataSet(result.getDataSetDescriptor());
                    statusUpdaterListener.updateStatus("CHView database: " + result.getDataSetDescriptor().getDataSetName() + " is loaded");
                    log.info("New dataset {} added", dataset.getName());
                    return true;
                } else {
                    showErrorAlert("load CH View file", result.getMessage());
                    return false;
                }
            }
            case "xlsx" -> {
                result = processRBExcelFile(dataset);
                if (result.isSuccess()) {
                    DataSetDescriptor dataSetDescriptor = result.getDataSetDescriptor();
                    statusUpdaterListener.updateStatus("Excel database: " + result.getDataSetDescriptor().getDataSetName() + " is loaded");
                    log.info("New dataset {} added", dataset.getName());
                    return true;
                } else {
                    showErrorAlert("load Excel file", result.getMessage());
                    return false;
                }
            }
            case "csv" -> {
                result = processRBCSVFile(loadUpdater, dataset);
                if (result.isSuccess()) {
                    this.dataSetChangeListener.addDataSet(result.getDataSetDescriptor());
                    statusUpdaterListener.updateStatus("CSV database: " + result.getDataSetDescriptor().getDataSetName() + " is loaded");
                    log.info("New dataset {} added", dataset.getName());
                    return true;
                } else {
                    showErrorAlert("load csv", result.getMessage());
                    return false;
                }
            }
//            case ".csv" -> {
//                result = processCSVFile(dataset);
//                if (result.isSuccess()) {
//                    this.dataSetChangeListener.addDataSet(result.getDataSetDescriptor());
//                    statusUpdaterListener.updateStatus("CSV database: " + result.getDataSetDescriptor().getDataSetName() + " is loaded");
//                    return true;
//                } else {
//                    showErrorAlert("load csv", result.getMessage());
//                    return false;
//                }
//            }
            case "json" -> {
                result = processJSONFile(dataset);
                if (result.isSuccess()) {
                    this.dataSetChangeListener.addDataSet(result.getDataSetDescriptor());
                    statusUpdaterListener.updateStatus("JSON database: " + result.getDataSetDescriptor().getDataSetName() + " is loaded");
                    log.info("New dataset {} added", dataset.getName());
                    return true;
                } else {
                    showErrorAlert("load JSON", result.getMessage());
                    return false;
                }
            }
//            case ".xlsx" -> {
//                result = processExcelFile(dataset);
//                if (result.isSuccess()) {
//                    this.dataSetChangeListener.addDataSet(result.getDataSetDescriptor());
//                    statusUpdaterListener.updateStatus("CSV database: " + result.getDataSetDescriptor().getDataSetName() + " is loaded");
//                    return true;
//                } else {
//                    showErrorAlert("load csv", result.getMessage());
//                    return false;
//                }
//            }
            default -> {
                showErrorAlert("Add Dataset", "Unable to match the correct suffix to: " + dataset.getDataType().getSuffix());
            }
        }
        return false;
    }

    ///////////////////////////////////////////////////////////////////////

    public @NotNull FileProcessResult processCHViewFile(@NotNull Dataset dataset) {
        FileProcessResult processResult = new FileProcessResult();

        File file = new File(dataset.getFileSelected());

        // load chview file
        ChViewFile chViewFile = chviewReader.loadFile(null, file);
        if (chViewFile == null) {
            FileProcessResult result = new FileProcessResult();
            result.setDataSetDescriptor(null);
            result.setSuccess(false);
            result.setMessage("Failed to parse file");
            return result;
        }
        try {
            DataSetDescriptor dataSetDescriptor = databaseManagementService.loadCHFile(null, dataset, chViewFile);
            String data = String.format("%s records loaded from dataset %s, Use plot to see data.",
                    dataSetDescriptor.getNumberStars(),
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
    }

    public @NotNull FileProcessResult processRBCSVFile(LoadUpdater loadUpdater, @NotNull Dataset dataset) {
        FileProcessResult processResult = new FileProcessResult();

        File file = new File(dataset.getFileSelected());
        RBCsvFile rbCsvFile = rbCsvReader.loadFile(null, file, dataset);
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


    public @NotNull FileProcessResult processRBExcelFile(@NotNull Dataset dataset) {
        FileProcessResult processResult = new FileProcessResult();

        File file = new File(dataset.getFileSelected());

        // load RB excel file
        RBExcelFile excelFile = RBExcelReader.loadFile(null,file);
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

    private @NotNull FileProcessResult processCSVFile(@NotNull Dataset dataset) {
        FileProcessResult processResult = new FileProcessResult();

        File file = new File(dataset.getFileSelected());

        return processResult;
    }


    private @NotNull FileProcessResult processJSONFile(Dataset dataset) {
        FileProcessResult processResult = new FileProcessResult();
        try {
            //create ObjectMapper instance
            ObjectMapper objectMapper = new ObjectMapper();

            //read json file and convert to customer object
            Set<StarObject> starObjectList = objectMapper.readValue(new File("customer.json"), new TypeReference<>() {
            });

            // now store
            databaseManagementService.starBulkSave(starObjectList);

        } catch (Exception e) {
            showErrorAlert("Duplicate Dataset", "This dataset was already loaded in the system ");
            processResult.setSuccess(false);
        }

        return processResult;
    }

    private @NotNull FileProcessResult processExcelFile(@NotNull Dataset dataset) {
        FileProcessResult processResult = new FileProcessResult();

        File file = new File(dataset.getFileSelected());

        // load RB excel file
        RBExcelFile excelFile = RBExcelReader.loadFile(null, file);
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


    /////////////////////////////////////////////////

    public void loadCSVDataset(@NotNull File file) {
        try {
            Reader reader = Files.newBufferedReader(Paths.get(file.getAbsolutePath()));


        } catch (Exception e) {
            log.error("caught error opening the file:{}", e.getMessage());
            showErrorAlert(
                    "Import Dataset from CSV file",
                    file.getAbsolutePath() +
                            " failed to import:" + e.getMessage()
            );
        }
    }


    /////////////////////////////////////////////

    /**
     * load the database from an excel file
     */
    public void loadDatabase() {
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


    ///////////////////////////////////

    /**
     * load a database file form an excel document
     *
     * @param file the database file
     */
    private void loadDBFile(@NotNull File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            // create a work book
            XSSFWorkbook myWorkBook = new XSSFWorkbook(fis);
            List<DataSetDescriptor> dataSetDescriptorList = getDatasets(myWorkBook);
            if (dataSetDescriptorList.size() > 0) {
                for (DataSetDescriptor descriptor : dataSetDescriptorList) {
                    XSSFSheet mySheet = myWorkBook.getSheet(descriptor.getDataSetName());
                    if (mySheet != null) {
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


    /**
     * get the dataset definitions from the Database worksheet
     *
     * @param myWorkBook the workbook containing the database
     * @return the set of dataset descriptors
     */
    private @NotNull List<DataSetDescriptor> getDatasets(@NotNull XSSFWorkbook myWorkBook) {
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
                } else {
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

    /**
     * read a cell on a row of a sheet
     *
     * @param currentRow the current row we are processing
     * @param column     the column to read
     * @return the cell as a string
     */
    private String readCell(@NotNull Row currentRow, int column) {
        Cell cell = currentRow.getCell(column);
        return cell.getStringCellValue();
    }

    /**
     * extract a dataset form a sheet
     *
     * @param mySheet the sheet on the excel workbook to extract
     */
    private void extractDataset(@NotNull XSSFSheet mySheet) {
        updateStatus(String.format("starting import of %s dataset", mySheet.getSheetName()));
        List<StarObject> starObjectList = new ArrayList<>();
        for (Row row : mySheet) {
            StarObject starObject = loadRow(row);
            if (starObject != null) {
                starObjectList.add(starObject);
            }
        }
        databaseManagementService.addStars(starObjectList);
    }

    /**
     * load a row of data for a star
     *
     * @param row the excel sheet row
     * @return the star data
     */
    private @Nullable StarObject loadRow(@NotNull Row row) {
        try {
            StarObject object = new StarObject();
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


    ///////////////////////////////////////////////////

    /**
     * update the status on an off UI thread
     *
     * @param status the status to report
     */
    private void updateStatus(String status) {
        new Thread(() -> Platform.runLater(() -> {
            log.info(status);
            statusUpdaterListener.updateStatus(status);
        })).start();
    }

}
