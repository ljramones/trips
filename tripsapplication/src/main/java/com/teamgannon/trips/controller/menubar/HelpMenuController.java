package com.teamgannon.trips.controller.menubar;

import com.teamgannon.trips.config.application.Localization;
import com.teamgannon.trips.dialogs.AboutDialog;
import com.teamgannon.trips.dialogs.inventory.InventoryReport;
import com.teamgannon.trips.measure.OshiMeasure;
import com.teamgannon.trips.report.ReportManager;
import javafx.application.HostServices;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxWeaver;
import org.springframework.stereotype.Component;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;
import static com.teamgannon.trips.support.AlertFactory.showWarningMessage;

/**
 * Controller for the Help menu.
 * Handles about dialog, support links, update checks, and system inventory.
 */
@Slf4j
@Component
public class HelpMenuController {

    private final Localization localization;
    private final HostServices hostServices;
    private final OshiMeasure oshiMeasure;

    public HelpMenuController(FxWeaver fxWeaver,
                              Localization localization,
                              OshiMeasure oshiMeasure) {
        this.localization = localization;
        this.hostServices = fxWeaver.getBean(HostServices.class);
        this.oshiMeasure = oshiMeasure;
    }

    /**
     * Shows the About TRIPS dialog.
     */
    public void aboutTrips(ActionEvent actionEvent) {
        try {
            AboutDialog aboutDialog = new AboutDialog(localization);
            aboutDialog.showAndWait();
        } catch (Exception e) {
            log.error("Error showing about dialog", e);
            showErrorAlert("About TRIPS", "Failed to show dialog: " + e.getMessage());
        }
    }

    /**
     * Opens the TRIPS wiki page for support documentation.
     */
    public void howToSupport(ActionEvent actionEvent) {
        try {
            hostServices.showDocument("https://github.com/ljramones/trips/wiki");
        } catch (Exception e) {
            log.error("Error opening support wiki", e);
            showErrorAlert("Support", "Failed to open support page: " + e.getMessage());
        }
    }

    /**
     * Checks for application updates.
     */
    public void checkUpdate(ActionEvent actionEvent) {
        showWarningMessage("Check for Update", "Not currently supported");
    }

    /**
     * Generates and displays a computer inventory report.
     */
    public void getInventory(ActionEvent actionEvent) {
        try {
            String physicalInventory = oshiMeasure.getComputerInventory();
            InventoryReport inventoryReport = new InventoryReport(physicalInventory);
            ReportManager reportManager = new ReportManager();

            // Get the stage from the event source if available
            Stage stage = getStageFromEvent(actionEvent);
            reportManager.generateComputerInventoryReport(stage, inventoryReport);
        } catch (Exception e) {
            log.error("Error generating inventory report", e);
            showErrorAlert("Inventory Report", "Failed to generate report: " + e.getMessage());
        }
    }

    /**
     * Attempts to get the Stage from the action event source.
     */
    private Stage getStageFromEvent(ActionEvent actionEvent) {
        try {
            if (actionEvent != null && actionEvent.getSource() instanceof javafx.scene.Node) {
                javafx.scene.Node source = (javafx.scene.Node) actionEvent.getSource();
                return (Stage) source.getScene().getWindow();
            }
        } catch (Exception e) {
            log.debug("Could not get stage from event, using null", e);
        }
        return null;
    }
}
