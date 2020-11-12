package com.teamgannon.trips.dialogs.dataset;

import com.teamgannon.trips.config.application.DataSetContext;
import com.teamgannon.trips.config.application.Localization;
import com.teamgannon.trips.dataset.AddDataSetDialog;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.listener.DataSetChangeListener;
import com.teamgannon.trips.service.DataExportService;
import com.teamgannon.trips.service.DataImportService;
import com.teamgannon.trips.service.DatabaseManagementService;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

import static com.teamgannon.trips.support.AlertFactory.showConfirmationAlert;
import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

@Slf4j
public class DataSetManagerDialog extends Dialog<Integer> {

    private final Font font = Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 13);

    private final Stage stage;
    private final DataSetChangeListener dataSetChangeListener;
    private final DataSetContext dataSetContext;

    private final ProgressBar loadProgress = new ProgressBar();
    private final Label progressText = new Label("    waiting for file selection");

    private Task<?> task;

    /**
     * the database management service used to manage datasets and databases
     */
    private final DatabaseManagementService databaseManagementService;

    private final DataImportService dataImportService;
    private final Localization localization;
    private final DataExportService dataExportService;

    private final ComboBox<DataSetDescriptor> descriptorComboBox = new ComboBox<>();

    private final TableView<DataSetDescriptor> tableView = new TableView<>();

    private final Button deleteButton = new Button("Delete");
    private final Button exportButton = new Button("Export");

    private DataSetDescriptor selectedDataset;

    public DataSetManagerDialog(Stage stage,
                                DataSetChangeListener dataSetChangeListener,
                                DataSetContext dataSetContext,
                                DatabaseManagementService databaseManagementService,
                                DataImportService dataImportService,
                                Localization localization,
                                DataExportService dataExportService) {
        this.stage = stage;

        this.dataSetChangeListener = dataSetChangeListener;
        this.dataSetContext = dataSetContext;
        this.databaseManagementService = databaseManagementService;
        this.dataImportService = dataImportService;
        this.localization = localization;
        this.dataExportService = dataExportService;

        this.setTitle("Dataset Management Dialog");
        this.setWidth(700);
        this.setHeight(500);

        VBox vBox = new VBox();
        this.getDialogPane().setContent(vBox);

        createTable(vBox);

        createSelectedDatasetContext(vBox);

        createButtonPanel(vBox);

        updateTable();

        vBox.getChildren().add(new Separator());
        HBox hBox6 = new HBox();
        hBox6.setAlignment(Pos.CENTER);
        Label progressLabel = new Label("Data file loading progress:  ");
        hBox6.getChildren().add(progressLabel);
        hBox6.getChildren().add(loadProgress);
        hBox6.getChildren().add(progressText);
        vBox.getChildren().add(hBox6);

        // set the dialog as a utility
        stage.setOnCloseRequest(this::close);
    }

    private void createSelectedDatasetContext(VBox vBox) {
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        Label contextSettingLabel = new Label("Active Dataset");
        contextSettingLabel.setFont(font);
        hBox.getChildren().addAll(contextSettingLabel, new Separator(), descriptorComboBox);
        vBox.getChildren().add(hBox);
        vBox.getChildren().addAll(new Separator());
        if (dataSetContext.isValidDescriptor()) {
            descriptorComboBox.setValue(dataSetContext.getDescriptor());
        }
        descriptorComboBox.setCellFactory(new ComboBoxDatasetCellFactory());

        // THIS IS NEEDED because providing a custom cell factory is NOT enough. You also need a specific set button cell
        descriptorComboBox.setButtonCell(new ListCell<>() {

            @Override
            protected void updateItem(DataSetDescriptor item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    setText(item.getDataSetName());
                } else {
                    setText(null);
                }
            }
        });

        descriptorComboBox.setOnAction(e -> dataSetChangeListener.setContextDataSet(descriptorComboBox.getValue()));
    }

    /**
     * close the dialog from stage x button
     *
     * @param we the windows event
     */
    private void close(WindowEvent we) {
        setResult(1);
    }


    /**
     * close triggered by button event
     *
     * @param actionEvent the action event
     */
    private void close(ActionEvent actionEvent) {
        setResult(1);
    }


    private void createButtonPanel(VBox vBox) {
        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setSpacing(5);
        vBox.getChildren().add(buttonBox);

        Button addDatasetButton = new Button("Add Dataset");
        addDatasetButton.setOnAction(this::addDataSet);
        buttonBox.getChildren().add(addDatasetButton);

        buttonBox.getChildren().add(deleteButton);
        deleteButton.setOnAction(this::delete);
        deleteButton.setDisable(true);

        buttonBox.getChildren().add(exportButton);
        exportButton.setOnAction(this::exportDB);
        exportButton.setDisable(true);

        Button closeDialogButton = new Button("Close");
        buttonBox.getChildren().add(closeDialogButton);
        closeDialogButton.setOnAction(this::close);

    }

    private void createTable(VBox vBox) {

        tableView.setPrefWidth(650);

        TableColumn<DataSetDescriptor, String> nameColumn = new TableColumn<>("Dataset Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("dataSetName"));
        tableView.getColumns().add(nameColumn);

        TableColumn<DataSetDescriptor, String> typeColumn = new TableColumn<>("Type");
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("datasetType"));
        tableView.getColumns().add(typeColumn);

        TableColumn<DataSetDescriptor, String> starsColumn = new TableColumn<>("# of stars");
        starsColumn.setCellValueFactory(new PropertyValueFactory<>("numberStars"));
        tableView.getColumns().add(starsColumn);

        TableColumn<DataSetDescriptor, String> distanceColumn = new TableColumn<>("Range in ly");
        distanceColumn.setCellValueFactory(new PropertyValueFactory<>("distanceRange"));
        tableView.getColumns().add(distanceColumn);

        TableColumn<DataSetDescriptor, String> notesColumn = new TableColumn<>("Notes");
        notesColumn.setCellValueFactory(new PropertyValueFactory<>("fileNotes"));
        tableView.getColumns().add(notesColumn);

        // set the default
        tableView.setPlaceholder(new Label("No rows to display"));
        TableView.TableViewSelectionModel<DataSetDescriptor> selectionModel = tableView.getSelectionModel();

        // set selection mode to only 1 row
        selectionModel.setSelectionMode(SelectionMode.SINGLE);

        ObservableList<DataSetDescriptor> selectedItems = selectionModel.getSelectedItems();
        selectedItems.addListener(this::onChanged);

        vBox.getChildren().add(tableView);
    }

    private void onChanged(ListChangeListener.Change<? extends DataSetDescriptor> change) {
        ObservableList<DataSetDescriptor> selectedItems = (ObservableList<DataSetDescriptor>) change.getList();
        if (selectedItems.size() != 0) {
            selectedDataset = selectedItems.get(0);
            deleteButton.setDisable(false);
            exportButton.setDisable(false);
        }
    }

    public void updateTable() {
        // clear the table
        tableView.getItems().clear();

        // get descriptors
        List<DataSetDescriptor> descriptors = databaseManagementService.getDataSets();

        // fill in table
        descriptors.forEach(descriptor -> {
            tableView.getItems().add(descriptor);
            descriptorComboBox.getItems().add(descriptor);
        });
    }

    private void delete(ActionEvent actionEvent) {
        if (selectedDataset != null) {
            this.dataSetChangeListener.removeDataSet(selectedDataset);
        } else {
            showErrorAlert("Delete Dataset", "You need to select a dataset first");
        }
        selectedDataset = null;
        updateTable();

    }


    private void exportDB(ActionEvent actionEvent) {
        Optional<ButtonType> result = showConfirmationAlert("Main Pane", "", "You want to export this dataset");
        if ((result.isPresent()) && (result.get() == ButtonType.OK)) {
            if (selectedDataset != null) {
                log.debug("Export the selected dataset as a CSV file");
                ExportDialog exportDialog = new ExportDialog(selectedDataset);
                Optional<ExportOptions> exportOptional = exportDialog.showAndWait();
                if (exportOptional.isPresent()) {
                    ExportOptions exportOptions = exportOptional.get();
                    if (exportOptions.isDoExport()) {
                        dataExportService.exportDataset(exportOptions);
                    }
                }
            } else {
                showErrorAlert("Export Dataset", "You need to select a dataset first");
            }
        }

    }

    private void addDataSet(ActionEvent actionEvent) {

        AddDataSetDialog dialog = new AddDataSetDialog(stage, localization, databaseManagementService);
        Optional<Dataset> optionalDataSet = dialog.showAndWait();

        if (optionalDataSet.isPresent()) {
            Dataset dataset = optionalDataSet.get();

            // we test if the name is null
            // a null means it was cancelled and so we skip processing
            if (dataset.getName() == null) {
                progressText.setText("  bad file, could not load");
                return;
            }
            progressText.setText("  starting load of " + dataset.getName() + " file");
            if (dataImportService.processFileType(dataset)) {
                progressText.setText("  " + dataset.getName() + " is loaded, updating local tables");
                updateTable();
            }
        }
        log.info("loaded data set dialog");
    }

    public void set(Task<?> task) {
        this.task = task;
        progressText.textProperty().bind(task.messageProperty());
        loadProgress.progressProperty().bind(task.progressProperty());
    }

    public void startLoad() {
        progressText.setText("starting load of file");
        task.stateProperty().addListener((observableValue, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                log.info("File load successfully completed");
            } else if (newState == Worker.State.FAILED) {
                log.error("failed to load data");
            }
        });
    }

}