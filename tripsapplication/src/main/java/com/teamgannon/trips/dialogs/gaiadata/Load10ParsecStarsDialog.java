package com.teamgannon.trips.dialogs.gaiadata;

import com.teamgannon.trips.dialogs.dataset.ComboBoxDatasetCellFactory;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.DatasetService;
import com.teamgannon.trips.service.StarService;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.Optional;

@Slf4j
public class Load10ParsecStarsDialog extends Dialog<Load10ParsecStarsResults> {

    private final File selectedFile;
    private final DatabaseManagementService databaseManagementService;
    private final StarService starService;
    private final DatasetService datasetService;

    private final ComboBox<DataSetDescriptor> descriptorComboBox = new ComboBox<>();
    private VBox vBox;
    private GridPane gridPane;

    private boolean starsLoaded = false;

    private int recordCount = 0;

    private final Load10PcCSVFile load10PcCSVFile = new Load10PcCSVFile();

    private final ComboBox<String> starSelector = new ComboBox<>();

    private final Font font = Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 13);
    private TextField starNameInDB;
    private TextField starDBRa;
    private TextField starDBDec;
    private TextField starDBDistance;
    private TextField starNameLoaded;
    private TextField starLoadedRa;
    private TextField starLoadedDec;
    private TextField starLoadedDistance;

    private TextField xInDB;
    private TextField yInDB;
    private TextField zInDB;

    private TextField xLoaded;
    private TextField yLoaded;
    private TextField zLoaded;


    private final TextArea textArea = new TextArea();

    private StarRecord currentStarRecord;
    private List<StarRecord> records;
    private List<StarObject> starObjectList;
    private Button loadStarButton;
    private Button addStarButton;


    private Label currentCounterLabel = new Label("0");

    private Label totalCounterLabel = new Label("?");

    /**
     * constructor
     *
     * @param selectedFile              the file to load
     * @param databaseManagementService the database service
     * @param datasetService            the dataset service
     */
    public Load10ParsecStarsDialog(File selectedFile,
                                   DatabaseManagementService databaseManagementService,
                                   StarService starService,
                                   DatasetService datasetService) {

        this.selectedFile = selectedFile;
        this.databaseManagementService = databaseManagementService;
        this.starService = starService;
        this.datasetService = datasetService;

        initializeDialog();
        initializeUIComponents();
    }

    /**
     * setup the dialog
     */
    private void initializeDialog() {
        this.setTitle("Load Stars from the 10 Parsec Volume of Space");
        this.setHeight(500);
        this.setWidth(500);

        vBox = new VBox();
        gridPane = new GridPane();
        vBox.getChildren().add(gridPane);
        this.getDialogPane().setContent(vBox);

        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        stage.setOnCloseRequest(this::close);
    }

    /**
     * set up the UI components
     */
    private void initializeUIComponents() {
        // Initialize UI components like descriptorComboBox, starSelector, etc.

        setupUIDataSetSelector();

        setupRecordComparisons();

        setupUIStatus();

        setupUIControlButtons();

        setupUIProcessingLog();
    }

    /**
     * set up the record comparisons
     */
    private void setupRecordComparisons() {
        setupHeaderDefinitions();

        starSelector.setOnAction(this::starSelected);
        starSelector.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(@Nullable String item, boolean empty) {
                super.updateItem(item, empty);
                setText(item);
            }
        });
        vBox.getChildren().add(starSelector);

        SetUUIDBRecord();

        // Star loaded from 10pc
        setupUILoadedRecord();
    }

    /**
     * set up the header definitions
     */
    private void setupHeaderDefinitions() {
        // Star database entry
        Label nameLabel = new Label("Name ");
        nameLabel.setFont(font);
        gridPane.add(nameLabel, 1, 1);
        Label raLabel = new Label("RA ");
        raLabel.setFont(font);
        gridPane.add(raLabel, 2, 1);
        Label decLabel = new Label("Dec ");
        decLabel.setFont(font);
        gridPane.add(decLabel, 3, 1);
        Label distanceLabel = new Label("Distance ");
        distanceLabel.setFont(font);
        gridPane.add(distanceLabel, 4, 1);
        Label xLabel = new Label("X ");
        xLabel.setFont(font);
        gridPane.add(xLabel, 5, 1);
        Label yLabel = new Label("Y ");
        yLabel.setFont(font);
        gridPane.add(yLabel, 6, 1);
        Label zLabel = new Label("Z ");
        zLabel.setFont(font);
        gridPane.add(zLabel, 7, 1);
    }

    /**
     * set up the dataset selector
     */
    private void setupUIDataSetSelector() {
        Label dataSetLabel = new Label("Select Dataset");
        dataSetLabel.setFont(font);
        gridPane.add(dataSetLabel, 0, 0);
        descriptorComboBox.getItems().addAll(datasetService.getDescriptors());
        descriptorComboBox.getSelectionModel().selectFirst();
        gridPane.add(descriptorComboBox, 1, 0);

        descriptorComboBox.setCellFactory(new ComboBoxDatasetCellFactory());

        // THIS IS NEEDED because providing a custom cell factory is NOT enough. You also need a specific set button cell
        descriptorComboBox.setButtonCell(new ListCell<>() {

            @Override
            protected void updateItem(@Nullable DataSetDescriptor item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    setText(item.getDataSetName());
                } else {
                    setText(null);
                }
            }
        });
    }

    /**
     * set up the processing log
     */
    private void setupUIProcessingLog() {
        vBox.getChildren().add(new Separator());
        textArea.setWrapText(true);
        textArea.setPrefRowCount(40);
        textArea.setPrefColumnCount(80);
        vBox.getChildren().add(textArea);
    }

    /**
     * set up the control buttons
     */
    private void setupUIControlButtons() {
        // first Button box
        HBox hBox1 = new HBox();
        hBox1.setAlignment(Pos.CENTER);
        vBox.getChildren().addAll(hBox1);
        Button nextButton = new Button("Begin/Next");
        nextButton.setOnAction(this::nextStarClicked);
        loadStarButton = new Button("Update Star in DB");
        loadStarButton.setDisable(true);
        loadStarButton.setOnAction(this::replaceStarClicked);
        addStarButton = new Button("Add Star in DB");
        addStarButton.setDisable(true);
        addStarButton.setOnAction(this::addStarClicked);

        hBox1.getChildren().addAll(nextButton, loadStarButton, addStarButton);

        // add separator between button boxes
        vBox.getChildren().add(new Separator());

        // fill button box
        HBox hBox2 = new HBox();
        hBox2.setAlignment(Pos.CENTER);
        vBox.getChildren().addAll(hBox2);

        Button cancelDataSetButton = new Button("Dismiss");
        cancelDataSetButton.setOnAction(this::close);
        hBox2.getChildren().add(cancelDataSetButton);
    }

    private void setupUIStatus() {
        gridPane.add(new Separator(), 0, 4, 8, 1);
        Label statusLabel = new Label("Current Record");
        statusLabel.setFont(font);
        gridPane.add(statusLabel, 0, 5);
        currentCounterLabel.setFont(font);
        gridPane.add(currentCounterLabel, 1, 5);

        // counters
        Label totalLabel = new Label("Total Records");
        totalLabel.setFont(font);
        gridPane.add(totalLabel, 3, 5);
        totalCounterLabel.setFont(font);
        gridPane.add(totalCounterLabel, 4, 5);
    }

    private void setupUILoadedRecord() {
        Label starLoadedLabel = new Label("Star from 10pc: ");
        starLoadedLabel.setFont(font);
        gridPane.add(starLoadedLabel, 0, 3);
        starNameLoaded = new TextField();
        gridPane.add(starNameLoaded, 1, 3);
        starLoadedRa = new TextField();
        gridPane.add(starLoadedRa, 2, 3);
        starLoadedDec = new TextField();
        gridPane.add(starLoadedDec, 3, 3);
        starLoadedDistance = new TextField();
        gridPane.add(starLoadedDistance, 4, 3);
        xLoaded = new TextField();
        gridPane.add(xLoaded, 5, 3);
        yLoaded = new TextField();
        gridPane.add(yLoaded, 6, 3);
        zLoaded = new TextField();
        gridPane.add(zLoaded, 7, 3);
    }

    private void SetUUIDBRecord() {
        Label starInDBLabel = new Label("Star in DB: ");
        starInDBLabel.setFont(font);
        gridPane.add(starInDBLabel, 0, 2);
        starNameInDB = new TextField();
        gridPane.add(starNameInDB, 1, 2);
        starDBRa = new TextField();
        gridPane.add(starDBRa, 2, 2);
        starDBDec = new TextField();
        gridPane.add(starDBDec, 3, 2);
        starDBDistance = new TextField();
        gridPane.add(starDBDistance, 4, 2);
        xInDB = new TextField();
        gridPane.add(xInDB, 5, 2);
        yInDB = new TextField();
        gridPane.add(yInDB, 6, 2);
        zInDB = new TextField();
        gridPane.add(zInDB, 7, 2);
    }

    private void starSelected(ActionEvent actionEvent) {
        Optional.ofNullable(starSelector.getSelectionModel().getSelectedItem())
                .flatMap(selectedStar -> starObjectList.stream()
                        .filter(star -> selectedStar.equals(star.getDisplayName()))
                        .findFirst())
                .ifPresent(this::updateUIWithStarObject);
    }

    private void updateUIWithStarObject(StarObject starObject) {
        starNameInDB.setText(starObject.getDisplayName());
        starDBRa.setText(Double.toString(starObject.getRa()));
        starDBDec.setText(Double.toString(starObject.getDeclination()));
        starDBDistance.setText(Double.toString(starObject.getDistance()));
        xInDB.setText(Double.toString(starObject.getX()));
        yInDB.setText(Double.toString(starObject.getY()));
        zInDB.setText(Double.toString(starObject.getZ()));
    }

    /**
     * Handle the star type record
     *
     * @param record the record to handle
     */
    private void handleStarType(StarRecord record) {
        starObjectList = getStarObjectsMatchingName(record.getSystemName(), record.getObjName());

        if (!starObjectList.isEmpty()) {
            loadStarButton.setDisable(false);
            updateUIWithRecord(record);
        } else {
            addStarButton.setDisable(false);
            logAndDisplayError(String.format("Record %d named %s is not in the database\n", recordCount, record.getObjName()));
            updateUIForMissingStar(record);
            currentStarRecord = record;
        }
    }

    /**
     * Update the UI with the record
     *
     * @param record the record to update
     */
    private void updateUIWithRecord(StarRecord record) {
        // Populate the ComboBox for star selection
        starSelector.getItems().clear();
        starSelector.getItems().addAll(starObjectList.stream().map(StarObject::getDisplayName).toList());
        starSelector.getSelectionModel().selectFirst();

        // Update the current file record
        currentStarRecord = record;
        log.info("Current Star Record, at next ==" + currentStarRecord.getObjName());


        // Update the UI fields with the star details from the loaded record
        starNameLoaded.setText(record.getObjName());
        starLoadedRa.setText(Double.toString(record.getRAdeg()));
        starLoadedDec.setText(Double.toString(record.getDEdeg()));
        starLoadedDistance.setText(Double.toString(record.getDistance()));
        xLoaded.setText(Double.toString(record.getCoordinates()[0]));
        yLoaded.setText(Double.toString(record.getCoordinates()[1]));
        zLoaded.setText(Double.toString(record.getCoordinates()[2]));

        // Increment the record count for the next iteration
        recordCount++;
    }

    /**
     * Update the UI for a missing star
     *
     * @param record the record to update
     */
    private void updateUIForMissingStar(StarRecord record) {
        // Update the UI fields to show that the star is missing in the database
        starNameInDB.setText("Not found in DB");
        starDBRa.setText("---");
        starDBDec.setText("---");
        starDBDistance.setText("---");
        xInDB.setText("---");
        yInDB.setText("---");
        zInDB.setText("---");

        // Update the UI fields with the loaded star details
        starNameLoaded.setText(record.getObjName());
        starLoadedRa.setText(Double.toString(record.getRAdeg()));
        starLoadedDec.setText(Double.toString(record.getDEdeg()));
        starLoadedDistance.setText(Double.toString(record.getDistance()));
        xLoaded.setText(Double.toString(record.getCoordinates()[0]));
        yLoaded.setText(Double.toString(record.getCoordinates()[1]));
        zLoaded.setText(Double.toString(record.getCoordinates()[2]));

        // Increment the record count for the next iteration
        recordCount++;
    }


    private void logAndDisplayError(String error) {
        textArea.appendText(error);
        log.error(error);
    }

    private void clearStarFields() {
        starNameInDB.clear();
        starDBRa.clear();
        starDBDec.clear();
        starDBDistance.clear();
        starNameLoaded.clear();
        starLoadedRa.clear();
        starLoadedDec.clear();
        starLoadedDistance.clear();
        xInDB.clear();
        yInDB.clear();
        zInDB.clear();
        xLoaded.clear();
        yLoaded.clear();
        zLoaded.clear();
    }

    private void nextStarClicked(ActionEvent actionEvent) {
        addStarButton.setDisable(true);
        loadStarButton.setDisable(true);
        if (!starsLoaded) {
            records = load10PcCSVFile.loadFile(selectedFile);
            totalCounterLabel.setText(Integer.toString(records.size()));
            starsLoaded = true;
        }

        for (; recordCount < records.size(); recordCount++) {
            currentCounterLabel.setText(Integer.toString(recordCount));
            StarRecord record = records.get(recordCount);
            clearStarFields();

            if (isStarType(record.getObjType())) {
                handleStarType(record);
                return;
            } else {
                currentStarRecord = null;
                logAndDisplayError(String.format("Record %d named %s is not a star, it is a %s\n", recordCount, record.getObjName(), record.getObjType()));
            }
        }
    }

    private List<StarObject> getStarObjectsMatchingName(String systemName, String objName) {
        String dataSetName = descriptorComboBox.getSelectionModel().getSelectedItem().getDataSetName();
        return starService.findStarWithName(dataSetName, objName);
    }

    /**
     * Check if the object type corresponds to a star type
     *
     * @param objType the object type
     * @return true if it is a star type
     */
    public static boolean isStarType(String objType) {
        // Normalize the input to handle case variations
        String normalizedObjType = objType.trim().toUpperCase();

        // Check if the object type corresponds to a star type
        return "*".equals(normalizedObjType) ||
                "LM".equals(normalizedObjType) ||
                "BD".equals(normalizedObjType) ||
                "WD".equals(normalizedObjType);
    }

    private void addStarClicked(ActionEvent actionEvent) {
        if (currentStarRecord == null) {
            log.error("No star record selected");
            return;
        } else {
            log.info("Current Star Record, before add ==" + currentStarRecord.getObjName());
        }
        Dialog<Boolean> dialog = new AddStarRecordDialog(databaseManagementService, starService, descriptorComboBox.getSelectionModel().getSelectedItem(), currentStarRecord);
        Optional<Boolean> result = dialog.showAndWait();
        result.ifPresent(this::handleAddStarResult);
    }

    private void handleAddStarResult(Boolean aBoolean) {
        log.info("Add star result: " + aBoolean);
        addStarButton.setDisable(true);
    }

    private void replaceStarClicked(ActionEvent actionEvent) {
        // the starObject is probably not correct
        Dialog<Boolean> dialog = new UpdateStarObjectWithRecordDialog(databaseManagementService,starService, descriptorComboBox.getSelectionModel().getSelectedItem(), currentStarRecord, starObjectList.get(0));
        Optional<Boolean> result = dialog.showAndWait();
        result.ifPresent(this::handleUpdateStarResult);
    }

    private void handleUpdateStarResult(Boolean aBoolean) {
        log.info("Update star result: " + aBoolean);
        loadStarButton.setDisable(true);
    }

    private void close(ActionEvent actionEvent) {
        Load10ParsecStarsResults findResults = Load10ParsecStarsResults.builder()
                .starsLoaded(false)
                .build();
        setResult(findResults);
    }

    private void close(WindowEvent windowEvent) {
        Load10ParsecStarsResults findResults = Load10ParsecStarsResults.builder()
                .starsLoaded(false)
                .build();
        setResult(findResults);
    }
}
