package com.teamgannon.trips.dialogs.dataset;

import com.teamgannon.trips.config.application.DataSetContext;
import com.teamgannon.trips.config.application.Localization;
import com.teamgannon.trips.dataset.AddDataSetDialog;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.listener.DataSetChangeListener;
import com.teamgannon.trips.listener.StatusUpdaterListener;
import com.teamgannon.trips.service.DataExportService;
import com.teamgannon.trips.service.DataImportService;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.export.ExportResult;
import com.teamgannon.trips.service.export.ExportResults;
import com.teamgannon.trips.service.importservices.ImportResult;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

import static com.teamgannon.trips.support.AlertFactory.showConfirmationAlert;
import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

@Slf4j
public class DataSetManagerDialog extends Dialog<Integer> implements ImportTaskComplete, ExportTaskComplete, LoadUpdateListener {


    private final Font font = Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 13);

    private final DataSetChangeListener dataSetChangeListener;

    private final HBox importLoadingPanel = new HBox();
    private final ProgressBar importProgressBar = new ProgressBar();
    private final Label importProgressText = new Label("    waiting for file selection");
    private final Button cancelImport = new Button("Cancel Import");

    private final HBox exportLoadingPanel = new HBox();
    private final ProgressBar exportProgressBar = new ProgressBar();
    private final Label exportProgressText = new Label("    waiting for file selection");
    private final Button cancelExport = new Button("Cancel Export");


    /**
     * the database management service used to manage datasets and databases
     */
    private final DatabaseManagementService databaseManagementService;

    private final StatusUpdaterListener statusUpdaterListener;
    private final DataImportService dataImportService;
    private final Localization localization;
    private final DataExportService dataExportService;

    private final TableView<DataSetDescriptor> tableView = new TableView<>();

    private final Button deleteButton = new Button("Delete");
    private final Button exportButton = new Button("Export");


    private @Nullable DataSetDescriptor selectedDataset;

    public DataSetManagerDialog(DataSetChangeListener dataSetChangeListener,
                                DataSetContext dataSetContext,
                                DatabaseManagementService databaseManagementService,
                                StatusUpdaterListener statusUpdaterListener,
                                DataImportService dataImportService,
                                Localization localization,
                                DataExportService dataExportService) {

        this.dataSetChangeListener = dataSetChangeListener;
        this.databaseManagementService = databaseManagementService;
        this.statusUpdaterListener = statusUpdaterListener;
        this.dataImportService = dataImportService;
        this.localization = localization;
        this.dataExportService = dataExportService;

        this.setTitle("Dataset Management Dialog");
        this.setWidth(700);
        this.setHeight(500);

        VBox vBox = new VBox();
        this.getDialogPane().setContent(vBox);

        createTable(vBox);

        createButtonPanel(vBox);

        updateTable();

        createImportProgress(vBox);
        createExportProgress(vBox);

        // set the dialog as a utility
        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        stage.setOnCloseRequest(this::close);
    }

    private void createImportProgress(@NotNull VBox vBox) {
        vBox.getChildren().add(new Separator());
        importLoadingPanel.setAlignment(Pos.CENTER);
        Label progressLabel = new Label("Data import loading progress:  ");
        importLoadingPanel.getChildren().add(progressLabel);
        importLoadingPanel.getChildren().add(importProgressBar);
        importLoadingPanel.getChildren().add(importProgressText);
        importLoadingPanel.getChildren().add(cancelImport);
        cancelImport.setOnAction(this::cancelTaskImport);
        importLoadingPanel.setVisible(false);
        vBox.getChildren().add(importLoadingPanel);
    }

    protected void createExportProgress(VBox vBox) {
        vBox.getChildren().add(new Separator());
        exportLoadingPanel.setAlignment(Pos.CENTER);
        Label progressLabel = new Label("Data file export progress:  ");
        exportLoadingPanel.getChildren().add(progressLabel);
        exportLoadingPanel.getChildren().add(exportProgressBar);
        exportLoadingPanel.getChildren().add(exportProgressText);
        exportLoadingPanel.getChildren().add(cancelExport);
        cancelExport.setOnAction(this::cancelTaskExport);
        exportLoadingPanel.setVisible(false);
        vBox.getChildren().add(exportLoadingPanel);
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


    private void createButtonPanel(@NotNull VBox vBox) {
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

    private void createTable(@NotNull VBox vBox) {

        HBox hBox = new HBox();
        Label titleLabel = new Label("Manage Datasets");
        titleLabel.setFont(font);
        hBox.getChildren().add(titleLabel);
        hBox.setAlignment(Pos.CENTER);
        vBox.getChildren().add(hBox);
        vBox.getChildren().add(new Separator());

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

    private void onChanged(ListChangeListener.@NotNull Change<? extends DataSetDescriptor> change) {
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

                        exportProgressText.setText("  starting export of " + exportOptions.getDataset().getDataSetName() + " dataset");
                        exportLoadingPanel.setVisible(true);
                        exportProgressBar.setProgress(0);

                        ExportResult success = dataExportService.exportDataset(
                                exportOptions,
                                statusUpdaterListener,
                                this,
                                exportProgressText,
                                exportProgressBar,
                                cancelExport);

                        if (!success.isSuccess()) {
                            showErrorAlert("export Dataset", success.getMessage());
                        }

                    }
                }
            } else {
                showErrorAlert("Export Dataset", "You need to select a dataset first");
            }
        }

    }

    private void addDataSet(ActionEvent actionEvent) {

        AddDataSetDialog dialog = new AddDataSetDialog(localization, databaseManagementService);
        Optional<Dataset> optionalDataSet = dialog.showAndWait();

        if (optionalDataSet.isPresent()) {
            Dataset dataset = optionalDataSet.get();

            // we test if the name is null
            // a null means it was cancelled and so we skip processing
            if (dataset.getName() == null) {
                importProgressText.setText("  bad file, could not load");
                return;
            }
            importProgressText.setText("  starting load of " + dataset.getName() + " file");

            importLoadingPanel.setVisible(true);
            importProgressBar.setProgress(0);

            ImportResult success = dataImportService.processFile(
                    dataset,
                    statusUpdaterListener,
                    dataSetChangeListener,
                    this,
                    importProgressText,
                    importProgressBar,
                    cancelImport,
                    this);

            if (!success.isSuccess()) {
                showErrorAlert("add Dataset", success.getMessage());
            }

        }
        log.info("loaded data set dialog");
    }

    public void complete(boolean status, @NotNull Dataset dataset, FileProcessResult fileProcessResult, String errorMessage) {
        if (status) {
            updateTable();
        } else {
            showErrorAlert("Add Dataset",
                    "failed to load dataset: " + dataset.getName() + ", because of " + errorMessage);
        }
        dataImportService.complete(status, dataset, errorMessage);
    }

    private void cancelTaskImport(ActionEvent event) {
        log.info("Import was cancelled");
        dataImportService.cancelCurrent();
    }

    private void cancelTaskExport(ActionEvent actionEvent) {
        log.info("Export was cancelled");
        dataExportService.cancelCurrent();
    }

    @Override
    public void update(DataSetDescriptor descriptor) {
    }

    @Override
    public void complete(boolean status, DataSetDescriptor dataSetDescriptor, ExportResults fileProcessResult, String errorMessage) {
        if (!status) {
            showErrorAlert("Add Dataset",
                    "failed to load dataset: " + dataSetDescriptor.getDataSetName() + ", because of " + errorMessage);
        }
        dataExportService.complete(status, dataSetDescriptor, errorMessage);
    }

}