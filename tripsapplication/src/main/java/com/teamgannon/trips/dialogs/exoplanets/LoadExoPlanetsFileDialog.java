package com.teamgannon.trips.dialogs.exoplanets;

import com.teamgannon.trips.dialogs.dataset.ComboBoxDatasetCellFactory;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.jpa.model.ExoPlanet;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.exoplanet.ExoPlanetService;
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
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

public class LoadExoPlanetsFileDialog extends Dialog<Boolean> {

    private final ExoPlanetService exoPlanetService;
    /**
     * the database management service
     */
    private final DatabaseManagementService databaseManagementService;

    private final File selectedFile;
    private VBox vBox;
    private GridPane gridPane;
    private Button loadPlanetButton;
    private TextArea textArea = new TextArea();

    private final ComboBox<DataSetDescriptor> descriptorComboBox = new ComboBox<>();

    private final LoadExoPlanetsCSVFile loadExoPlanetsCSVFile = new LoadExoPlanetsCSVFile();

    private final Font font = Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 13);

    private Label currentCounterLabel = new Label("0");

    private Label totalCounterLabel = new Label("?");

    private TextField planetNameLoaded;
    private TextField starNameLoaded;
    private TextField starLoadedRa;
    private TextField starLoadedDec;
    private TextField starLoadedDistance;
    private boolean planetsLoaded = false;

    private List<ExoPlanet> records;
    private int recordCount = 0;

    /**
     * constructor
     *
     * @param databaseManagementService the database management service
     */
    public LoadExoPlanetsFileDialog(File selectedFile, ExoPlanetService exoPlanetService, DatabaseManagementService databaseManagementService) {
        this.selectedFile = selectedFile;
        this.exoPlanetService = exoPlanetService;

        this.databaseManagementService = databaseManagementService;

        initializeDialog();
        initializeUIComponents();
    }

    /**
     * set up the dialog
     */
    private void initializeDialog() {
        this.setTitle("Load ExoPlanets File");
        this.setHeight(500);
        this.setWidth(500);
        this.setResizable(true);

        vBox = new VBox();
        gridPane = new GridPane();
        vBox.getChildren().add(gridPane);
        this.getDialogPane().setContent(vBox);

        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        stage.setOnCloseRequest(this::close);
    }


    private void initializeUIComponents() {

        setupUIDataSetSelector();

        setupRecordComparisons();

        // this tells us how many items we've processed
        setupUIStatus();

        // set uup the button to control entry
        setupUIControlButtons();

        // this text area gives us a visual log of what's going on
        setupUIProcessingLog();
    }

    /**
     * set up the record comparisons
     */
    private void setupRecordComparisons() {
        setupHeaderDefinitions();

        // Star loaded from 10pc
        setupUILoadedRecord();
    }

    private void setupHeaderDefinitions() {
        // Star database entry
        Label nameLabel = new Label("Planet Name ");
        nameLabel.setFont(font);
        gridPane.add(nameLabel, 1, 1);
        Label starName = new Label("Star Name ");
        starName.setFont(font);
        gridPane.add(starName, 2, 1);
        Label raLabel = new Label("Star RA ");
        raLabel.setFont(font);
        gridPane.add(raLabel, 3, 1);
        Label decLabel = new Label("Star Dec ");
        decLabel.setFont(font);
        gridPane.add(decLabel, 4, 1);
        Label distanceLabel = new Label("Star Distance ");
        distanceLabel.setFont(font);
        gridPane.add(distanceLabel, 5, 1);
    }


    private void setupUILoadedRecord() {
        Label starLoadedLabel = new Label("Planet from Exoplanets.csv: ");
        starLoadedLabel.setFont(font);
        gridPane.add(starLoadedLabel, 0, 2);
        planetNameLoaded = new TextField();
        gridPane.add(planetNameLoaded, 1, 2);
        starNameLoaded = new TextField();
        gridPane.add(starNameLoaded, 2, 2);
        starLoadedRa = new TextField();
        gridPane.add(starLoadedRa, 3, 2);
        starLoadedDec = new TextField();
        gridPane.add(starLoadedDec, 4, 2);
        starLoadedDistance = new TextField();
        gridPane.add(starLoadedDistance, 5, 2);
    }

    private void setupUIStatus() {
        gridPane.add(new Separator(), 0, 4, 5, 1);
        Label statusLabel = new Label("Current Record: ");
        statusLabel.setFont(font);
        gridPane.add(statusLabel, 0, 5);
        currentCounterLabel.setFont(font);
        gridPane.add(currentCounterLabel, 1, 5);

        // counters
        Label totalLabel = new Label("Total Records: ");
        totalLabel.setFont(font);
        gridPane.add(totalLabel, 3, 5);
        totalCounterLabel.setFont(font);
        gridPane.add(totalCounterLabel, 4, 5);
    }

    /**
     * set up the dataset selector
     */
    private void setupUIDataSetSelector() {
        Label dataSetLabel = new Label("Select Dataset");
        dataSetLabel.setFont(font);
        gridPane.add(dataSetLabel, 0, 0);
        descriptorComboBox.getItems().addAll(databaseManagementService.getDescriptors());
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

    private void setupUIControlButtons() {
        // first Button box
        HBox hBox1 = new HBox();
        hBox1.setAlignment(Pos.CENTER);
        vBox.getChildren().addAll(hBox1);
        Button nextButton = new Button("Begin/Next");
        nextButton.setOnAction(this::nextPlanetClicked);
        loadPlanetButton = new Button("Add Planet in DB");
        loadPlanetButton.setDisable(true);
        loadPlanetButton.setOnAction(this::loadPlanetButton);

        hBox1.getChildren().addAll(nextButton, loadPlanetButton);

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

    private void loadPlanetButton(ActionEvent actionEvent) {
        vBox.getChildren().add(new Separator());
        textArea.setWrapText(true);
        textArea.setPrefRowCount(40);
        textArea.setPrefColumnCount(80);
        vBox.getChildren().add(textArea);

        // add the planet to the database
        DataSetDescriptor descriptor = descriptorComboBox.getSelectionModel().getSelectedItem();

    }

    private void close(ActionEvent actionEvent) {
        this.setResult(false);
    }

    private void nextPlanetClicked(ActionEvent actionEvent) {
        loadPlanetButton.setDisable(true);
        if (!planetsLoaded) {
            records = loadExoPlanetsCSVFile.loadFile(selectedFile);
            totalCounterLabel.setText(Integer.toString(records.size()));
            planetsLoaded = true;
        }

        for (; recordCount < records.size(); recordCount++) {
            ExoPlanet record = records.get(recordCount);
            if (!exoPlanetService.existsByName(record.getName())) {
                textArea.appendText("Planet " + record.getName() + " doesn't exist creating\n");
                String datasetName = descriptorComboBox.getSelectionModel().getSelectedItem().getDataSetName();
                List<StarObject> starObjects = databaseManagementService.findStarWithName(datasetName, record.getStarName());
                if (!starObjects.isEmpty()) {
                    // there are already stars with this name
                    textArea.appendText("Star " + record.getStarName() + " exists\n");



                } else {
                    textArea.appendText("Star " + record.getStarName() + " doesn't exist creating\n");
                    // create the star
                    StarObject starObject = createStarObject(record);
                }
                continue;
            }


            currentCounterLabel.setText(Integer.toString(recordCount));
//

            clearStarFields();
            handlePlanetType(record);

        }
    }

    private StarObject createStarObject(ExoPlanet record) {
        StarObject starObject = new StarObject();
        starObject.setDisplayName(record.getStarName());

        return starObject;
    }

    private void clearStarFields() {
        planetNameLoaded.setText("");
        starNameLoaded.setText("");
        starLoadedRa.setText("");
        starLoadedDec.setText("");
        starLoadedDistance.setText("");
    }

    private void handlePlanetType(ExoPlanet record) {

    }

    private void setupUIProcessingLog() {
        vBox.getChildren().add(new Separator());
        textArea.setWrapText(true);
        textArea.setPrefRowCount(40);
        textArea.setPrefColumnCount(80);
        vBox.getChildren().add(textArea);
    }


    private void close(WindowEvent windowEvent) {
        this.setResult(false);
    }


}
