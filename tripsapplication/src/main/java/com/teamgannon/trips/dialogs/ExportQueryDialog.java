package com.teamgannon.trips.dialogs;

import com.teamgannon.trips.config.application.Localization;
import com.teamgannon.trips.dialogs.dataset.ExportOptions;
import com.teamgannon.trips.dialogs.dataset.ExportTaskComplete;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.listener.StatusUpdaterListener;
import com.teamgannon.trips.search.SearchContext;
import com.teamgannon.trips.service.DataExportService;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.export.ExportResult;
import com.teamgannon.trips.service.export.ExportResults;
import com.teamgannon.trips.service.model.ExportFileType;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

@Slf4j
public class ExportQueryDialog extends Dialog<Boolean> implements ExportTaskComplete {

    private final ChoiceBox<String> exportChoice = new ChoiceBox<>();

    private final TextField fileNameTextField = new TextField();
    private final SearchContext searchContext;
    private final DatabaseManagementService databaseManagementService;
    private final DataExportService dataExportService;
    private StatusUpdaterListener statusUpdaterListener;
    private Localization localization;

    private File fileToStore;


    private final HBox exportLoadingPanel = new HBox();
    private final ProgressBar exportProgressBar = new ProgressBar();
    private final Label exportProgressText = new Label("    waiting for file selection");
    private final Button cancelExport = new Button("Cancel Export");


    /**
     * constructor
     *
     * @param searchContext             the saerch context to do the export on
     * @param databaseManagementService the database management service
     * @param dataExportService         the data export service
     * @param statusUpdaterListener     for reporting status
     * @param localization              for localization
     */
    public ExportQueryDialog(SearchContext searchContext,
                             DatabaseManagementService databaseManagementService,
                             DataExportService dataExportService,
                             StatusUpdaterListener statusUpdaterListener,
                             Localization localization) {

        this.searchContext = searchContext;
        this.databaseManagementService = databaseManagementService;
        this.dataExportService = dataExportService;
        this.statusUpdaterListener = statusUpdaterListener;
        this.localization = localization;

        this.setTitle("Export Options");
        this.setWidth(600);

        exportChoice.getItems().add(ExportFileType.CSV.toString());

        exportChoice.setValue(ExportFileType.CSV.toString());

        VBox vBox = new VBox();

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setVgap(5);
        gridPane.setHgap(5);
        gridPane.setPrefWidth(450);

        Label exportTypeLabel = new Label("Export Type");
        Font font = Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 13);
        exportTypeLabel.setFont(font);
        gridPane.add(exportTypeLabel, 0, 1);
        gridPane.add(exportChoice, 1, 1);

        gridPane.add(new Label("FileName"), 0, 2);
        fileNameTextField.setPrefWidth(300);
        gridPane.add(fileNameTextField, 1, 2);

        Button fileDialogButton = new Button("Pick file and location");
        fileDialogButton.setFont(font);
        gridPane.add(fileDialogButton, 0, 2);
        fileDialogButton.setOnAction(event -> {
            showDialog();
        });

        vBox.getChildren().add(gridPane);

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);

        Button doExportButton = new Button("Export");
        doExportButton.setOnAction(this::exportClicked);
        hBox.getChildren().add(doExportButton);

        Button cancelButton = new Button("  Cancel");
        cancelButton.setOnAction(this::close);
        hBox.getChildren().add(cancelButton);
        vBox.getChildren().add(hBox);

        createExportProgress(vBox);

        this.getDialogPane().setContent(vBox);

        // set the dialog as a utility
        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        stage.setOnCloseRequest(this::close);
    }


    /**
     * show the export progress
     *
     * @param vBox the container
     */
    protected void createExportProgress(VBox vBox) {
        vBox.getChildren().add(new Separator());
        exportLoadingPanel.setAlignment(Pos.CENTER);
        Label progressLabel = new Label("Data file export progress:  ");
        exportLoadingPanel.getChildren().add(progressLabel);
        exportLoadingPanel.getChildren().add(exportProgressBar);
        exportLoadingPanel.getChildren().add(exportProgressText);
        exportLoadingPanel.getChildren().add(cancelExport);
        cancelExport.setOnAction(this::cancelTaskExport);
        exportLoadingPanel.setVisible(true);
        vBox.getChildren().add(exportLoadingPanel);
    }

    /**
     * to cancel the export
     *
     * @param actionEvent the event
     */
    private void cancelTaskExport(ActionEvent actionEvent) {
        log.info("Export was canceled");
        dataExportService.cancelCurrent();
    }


    private void showDialog() {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Database to export as a CSV file");
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            fileToStore = file;
            fileNameTextField.setText(file.getAbsolutePath());
        } else {
            log.warn("file export cancelled");
            setResult(false);
        }
    }

    private void close(WindowEvent windowEvent) {
        setResult(false);
    }

    private void close(ActionEvent actionEvent) {
        setResult(false);
    }

    private void exportClicked(ActionEvent actionEvent) {

        if (!databaseManagementService.starsAvailableForQuery(searchContext)) {
            showErrorAlert(
                    "Astrographic data view error",
                    "No Astrographic data is available for export.");
            return;
        }

        if (fileToStore != null) {
            ExportOptions exportOptions = ExportOptions
                    .builder()
                    .fileName(fileToStore.getAbsolutePath())
                    .exportFormat(ExportFileType.CSV)
                    .dataset(searchContext.getAstroSearchQuery().getDescriptor())
                    .doExport(true)
                    .build();

            ExportResult success = dataExportService.exportDatasetOnQuery(exportOptions, searchContext,
                    statusUpdaterListener, this, exportProgressText,
                    exportProgressBar, cancelExport);
            if (!success.isSuccess()) {
                dataExportService.complete(false, exportOptions.getDataset(), "");
                setResult(false);
            }
        } else {
            log.warn("selected file was null");
        }
    }


    @Override
    public void complete(boolean status, DataSetDescriptor dataSetDescriptor, ExportResults fileProcessResult, String errorMessage) {
        if (status) {
            setResult(true);
        } else {
            showErrorAlert("Add Dataset",
                    "failed to load dataset: " + dataSetDescriptor.getDataSetName() + ", because of " + errorMessage);
            setResult(false);
        }
        dataExportService.complete(status, dataSetDescriptor, errorMessage);

    }

}
