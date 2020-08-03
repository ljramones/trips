package com.teamgannon.trips.dialogs;

import com.teamgannon.trips.dialogs.support.FileProcessResult;
import com.teamgannon.trips.file.chview.ChviewReader;
import com.teamgannon.trips.file.chview.model.ChViewFile;
import com.teamgannon.trips.file.excel.ExcelReader;
import com.teamgannon.trips.file.excel.model.RBExcelFile;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.search.StellarDataUpdater;
import com.teamgannon.trips.service.DatabaseManagementService;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.List;
import java.util.Optional;

import static com.teamgannon.trips.support.AlertFactory.*;


@Slf4j
public class DataSetManagerDialog extends Dialog<Integer> {

    private StellarDataUpdater dataUpdater;
    /**
     * the database management service used to manage datasets and databases
     */
    private final DatabaseManagementService databaseManagementService;
    private final ChviewReader chviewReader;
    private final ExcelReader excelReader;

    private final TableView<DataSetDescriptor> tableView = new TableView<>();

    private final Button deleteButton = new Button("Delete");
    private final Button exportButton = new Button("Export");

    private DataSetDescriptor selectedDataset;

    public DataSetManagerDialog(StellarDataUpdater dataUpdater,
                                DatabaseManagementService databaseManagementService,
                                ChviewReader chviewReader,
                                ExcelReader excelReader) {
        this.dataUpdater = dataUpdater;

        this.databaseManagementService = databaseManagementService;
        this.chviewReader = chviewReader;
        this.excelReader = excelReader;

        this.setTitle("Dataset Management Dialog");
        this.setWidth(700);
        this.setHeight(400);

        VBox vBox = new VBox();
        this.getDialogPane().setContent(vBox);

        createTable(vBox);

        createButtonPanel(vBox);
        updateTable();

        // set the dialog as a utility
        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        stage.setOnCloseRequest(this::close);

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

        TableColumn<DataSetDescriptor, String> typeColumn = new TableColumn<>("Type");
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("datasetType"));

        TableColumn<DataSetDescriptor, String> starsColumn = new TableColumn<>("# of stars");
        starsColumn.setCellValueFactory(new PropertyValueFactory<>("numberStars"));

        TableColumn<DataSetDescriptor, String> notesColumn = new TableColumn<>("Notes");
        notesColumn.setCellValueFactory(new PropertyValueFactory<>("fileNotes"));

        tableView.getColumns().add(nameColumn);
        tableView.getColumns().add(typeColumn);
        tableView.getColumns().add(starsColumn);
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
        List<DataSetDescriptor> descriptors = databaseManagementService.getDataSetIds();

        // fill in table
        for (DataSetDescriptor descriptor : descriptors) {
            tableView.getItems().add(descriptor);
        }
    }

    private void delete(ActionEvent actionEvent) {
        Optional<ButtonType> result = showConfirmationAlert("Main Pane", "", "You want to delete these datasets?\nThis action cannot be undone");
        if ((result.isPresent()) && (result.get() == ButtonType.OK)) {
            if (selectedDataset != null) {
                DataSetDescriptor dataSetDescriptor = databaseManagementService.getDatasetFromName(selectedDataset.getDataSetName());
                databaseManagementService.removeDataSet(dataSetDescriptor);
                this.dataUpdater.removeDataSet(dataSetDescriptor);
            } else {
                showErrorAlert("Delete Dataset", "You need to select a dataset first");
            }
            selectedDataset = null;
            updateTable();
        }
    }


    private void exportDB(ActionEvent actionEvent) {
        Optional<ButtonType> result = showConfirmationAlert("Main Pane", "", "You want to export this dataset");
        if ((result.isPresent()) && (result.get() == ButtonType.OK)) {
            if (selectedDataset != null) {
                log.debug("Export the selected dataset as a CSV file");
                final FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Select Database to export as a CSV file");
                File file = fileChooser.showSaveDialog(null);
                if (file != null) {
                    exportDB(selectedDataset, file);
                } else {
                    log.warn("file export cancelled");
                }
            } else {
                showErrorAlert("Export Dataset", "You need to select a dataset first");
            }
        }

    }

    private void addDataSet(ActionEvent actionEvent) {

        AddDataSetDialog dialog = new AddDataSetDialog();
        Optional<Dataset> optionalDataSet = dialog.showAndWait();

        if (optionalDataSet.isPresent()) {
            Dataset dataset = optionalDataSet.get();

            // we test if the name is null
            // a null means it was cancelled and so we skip processing
            if (dataset.getName() == null) {
                return;
            }

            FileProcessResult result;
            switch (dataset.getDataType().getSuffix()) {
                case "chv":
                    // this is a CH View import format
                    result = processCHViewFile(dataset);
                    if (result.isSuccess()) {
                        DataSetDescriptor dataSetDescriptor = result.getDataSetDescriptor();
                        this.dataUpdater.addDataSet(dataSetDescriptor);
                        updateTable();
                    } else {
                        showErrorAlert("load CH View file", result.getMessage());
                    }
                    break;
                case "xlsv":
                    // this is Excel format that follows a specification from the Rick Boatwright format
                    result = processRBExcelFile(dataset);
                    if (result.isSuccess()) {
                        DataSetDescriptor dataSetDescriptor = result.getDataSetDescriptor();
                        updateTable();
                    } else {
                        showErrorAlert("load Excel file", result.getMessage());
                    }
                    break;
                case "csv":
                    // this is a database import
                    result = processCSVFile(dataset);
                    if (result.isSuccess()) {
                        DataSetDescriptor dataSetDescriptor = result.getDataSetDescriptor();
                        updateTable();
                    } else {
                        showErrorAlert("load csv", result.getMessage());
                    }
                    break;
                case "simbad":
                    // this is Simbad database import format
                    result = processSimbadFile(dataset);
                    if (result.isSuccess()) {
                        DataSetDescriptor dataSetDescriptor = result.getDataSetDescriptor();

                        updateTable();
                    } else {
                        showErrorAlert("load simbad", result.getMessage());
                    }
            }
            log.info("New dataset {} added", dataset.getName());
        }
        log.info("loaded data set dialog");
    }

    private FileProcessResult processSimbadFile(Dataset dataset) {
        FileProcessResult processResult = new FileProcessResult();
        processResult.setSuccess(true);

        return processResult;
    }

    private FileProcessResult processCSVFile(Dataset dataset) {
        FileProcessResult processResult = new FileProcessResult();
        processResult.setSuccess(true);

        return processResult;
    }

    private FileProcessResult processRBExcelFile(Dataset dataset) {
        FileProcessResult processResult = new FileProcessResult();

        File file = new File(dataset.getFileSelected());

        // load RB excel file
        RBExcelFile excelFile = excelReader.loadFile(file);
        try {
            databaseManagementService.loadRBStarSet(excelFile);
            showInfoMessage(
                    "Load RB Excel Format",
                    "xx records loaded from dataset yy, Use plot to see data");
            processResult.setSuccess(true);

        } catch (Exception e) {
            showErrorAlert("Duplicate Dataset", "This dataset was already loaded in the system ");
            processResult.setSuccess(false);
        }

        return processResult;
    }



    private FileProcessResult processCHViewFile(Dataset dataset) {
        FileProcessResult processResult = new FileProcessResult();

        File file = new File(dataset.getFileSelected());

        // load chview file
        ChViewFile chViewFile = chviewReader.loadFile(file);
        try {
            DataSetDescriptor dataSetDescriptor = databaseManagementService.loadCHFile(dataset, chViewFile);
            String data = String.format("%s records loaded from dataset %s, Use plot to see data.",
                    dataSetDescriptor.getAstrographicDataList().size(),
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

    //////// Database import and export methods

    /**
     * call the database import method
     *
     * @param file the file to load
     */
    private void importDatabase(File file) {
        databaseManagementService.importDatabase(file);
        log.info("File selection is:" + file.getAbsolutePath());
        Optional<ButtonType> result = showConfirmationAlert("MainPane", "", "Database loaded:" + file.getName());
        if ((result.isPresent()) && (result.get() == ButtonType.OK)) {
            log.info("import database");
        }
    }


    /**
     * export the database as a CSV file
     *
     * @param dataSetDescriptor the descriptor
     * @param file the file to export
     */
    private void exportDB(DataSetDescriptor dataSetDescriptor, File file) {
        databaseManagementService.exportDatabase(dataSetDescriptor,file);
        log.info("File selection is:" + file.getAbsolutePath());
    }

    /**
     * export as a JSON file
     */
    public void exportJSON() {
        log.debug("Export as a CSV format file");
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select JSON file to import");
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("JSON Files", "json");
        fileChooser.setSelectedExtensionFilter(filter);
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            log.info("export");
//            chviewReader.exportJson(file, chViewFile);
        } else {
            log.warn("file selection cancelled");
        }
    }

}