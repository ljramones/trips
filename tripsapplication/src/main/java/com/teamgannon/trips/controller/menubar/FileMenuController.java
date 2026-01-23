package com.teamgannon.trips.controller.menubar;

import com.teamgannon.trips.config.application.Localization;
import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.dialogs.dataset.DataSetManagerDialog;
import com.teamgannon.trips.dialogs.dataset.SelectActiveDatasetDialog;
import com.teamgannon.trips.service.DataExportService;
import com.teamgannon.trips.service.DataImportService;
import com.teamgannon.trips.service.DatasetService;
import com.teamgannon.trips.service.DatabaseManagementService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.teamgannon.trips.support.AlertFactory.showConfirmationAlert;
import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

/**
 * Controller for the File menu.
 * Handles dataset management, save, and application exit.
 */
@Slf4j
@Component
public class FileMenuController {

    @FXML
    public MenuItem importDataSetMenuItem;
    @FXML
    public MenuItem openDatasetMenuItem;
    @FXML
    public MenuItem saveMenuItem;
    @FXML
    public MenuItem quitMenuItem;

    private final TripsContext tripsContext;
    private final ApplicationContext appContext;
    private final ApplicationEventPublisher eventPublisher;
    private final DatabaseManagementService databaseManagementService;
    private final DatasetService datasetService;
    private final DataImportService dataImportService;
    private final DataExportService dataExportService;
    private final Localization localization;

    public FileMenuController(TripsContext tripsContext,
                              ApplicationContext appContext,
                              ApplicationEventPublisher eventPublisher,
                              DatabaseManagementService databaseManagementService,
                              DatasetService datasetService,
                              DataImportService dataImportService,
                              DataExportService dataExportService,
                              Localization localization) {
        this.tripsContext = tripsContext;
        this.appContext = appContext;
        this.eventPublisher = eventPublisher;
        this.databaseManagementService = databaseManagementService;
        this.datasetService = datasetService;
        this.dataImportService = dataImportService;
        this.dataExportService = dataExportService;
        this.localization = localization;
    }

    @FXML
    public void initialize() {
        setMnemonics();
    }

    private void setMnemonics() {
        if (openDatasetMenuItem != null) {
            openDatasetMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
        }
        if (importDataSetMenuItem != null) {
            importDataSetMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.I, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
        }
        if (quitMenuItem != null) {
            quitMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));
        }
    }

    /**
     * Opens the dataset manager dialog for importing/loading/managing datasets.
     */
    public void loadDataSetManager(ActionEvent actionEvent) {
        try {
            DataSetManagerDialog dialog = new DataSetManagerDialog(
                    databaseManagementService,
                    datasetService,
                    eventPublisher,
                    dataImportService,
                    localization,
                    dataExportService);
            dialog.showAndWait();
        } catch (Exception e) {
            log.error("Error opening dataset manager", e);
            showErrorAlert("Dataset Manager", "Failed to open dialog: " + e.getMessage());
        }
    }

    /**
     * Opens dialog to select and activate a dataset.
     */
    public void selectActiveDataset(ActionEvent actionEvent) {
        try {
            if (tripsContext.getDataSetContext().getDescriptor() != null) {
                SelectActiveDatasetDialog dialog = new SelectActiveDatasetDialog(
                        eventPublisher,
                        tripsContext.getDataSetContext(),
                        databaseManagementService,
                        datasetService);
                dialog.showAndWait();
            } else {
                showErrorAlert("Select a Dataset", "There are no datasets to select.");
            }
        } catch (Exception e) {
            log.error("Error selecting active dataset", e);
            showErrorAlert("Select Dataset", "Failed to open dialog: " + e.getMessage());
        }
    }

    /**
     * Saves the current dataset.
     */
    public void saveDataset(ActionEvent actionEvent) {
        log.info("Save requested");
        // Not yet implemented
    }

    /**
     * Exits the application after confirmation.
     */
    public void quit(ActionEvent actionEvent) {
        log.debug("Exit selection");
        Optional<ButtonType> result = showConfirmationAlert(
                "Exit Application",
                "Exit Application?",
                "Are you sure you want to leave?");

        if ((result.isPresent()) && (result.get() == ButtonType.OK)) {
            initiateShutdown(0);
        }
    }

    /**
     * Shutdown the application.
     *
     * @param returnCode should be a return code of zero meaning success
     */
    private void initiateShutdown(int returnCode) {
        // close the spring context which invokes all the bean destroy methods
        int exitCode = SpringApplication.exit(appContext, () -> returnCode);
        // now exit the application
        Platform.exit();
        System.exit(exitCode);
    }
}
