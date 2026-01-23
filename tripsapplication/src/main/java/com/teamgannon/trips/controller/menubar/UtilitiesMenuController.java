package com.teamgannon.trips.controller.menubar;

import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.dialogs.utility.EquatorialToGalacticCoordsDialog;
import com.teamgannon.trips.dialogs.utility.FindDistanceDialog;
import com.teamgannon.trips.dialogs.utility.RADecToXYZDialog;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.StarService;
import javafx.event.ActionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

/**
 * Controller for the Utilities menu.
 * Handles coordinate conversion and distance calculation utilities.
 */
@Slf4j
@Component
public class UtilitiesMenuController {

    private final TripsContext tripsContext;
    private final DatabaseManagementService databaseManagementService;
    private final StarService starService;

    public UtilitiesMenuController(TripsContext tripsContext,
                                   DatabaseManagementService databaseManagementService,
                                   StarService starService) {
        this.tripsContext = tripsContext;
        this.databaseManagementService = databaseManagementService;
        this.starService = starService;
    }

    /**
     * Opens the distance calculation dialog.
     */
    public void findDistance(ActionEvent actionEvent) {
        try {
            List<String> datasetNames = tripsContext.getSearchContext().getDataSetNames();
            if (datasetNames.isEmpty()) {
                showErrorAlert("Find Distance", "No datasets in database, please load first");
                return;
            }
            FindDistanceDialog dialog = new FindDistanceDialog(
                    datasetNames,
                    tripsContext.getSearchContext().getDataSetDescriptor(),
                    databaseManagementService,
                    starService);
            dialog.showAndWait();
        } catch (Exception e) {
            log.error("Error opening find distance dialog", e);
            showErrorAlert("Find Distance Error", "Failed to open dialog: " + e.getMessage());
        }
    }

    /**
     * Opens the RA/Dec to XYZ coordinate conversion dialog.
     */
    public void findXYZ(ActionEvent actionEvent) {
        try {
            RADecToXYZDialog dialog = new RADecToXYZDialog();
            dialog.showAndWait();
        } catch (Exception e) {
            log.error("Error opening RA/Dec to XYZ dialog", e);
            showErrorAlert("Coordinate Conversion Error", "Failed to open dialog: " + e.getMessage());
        }
    }

    /**
     * Opens the Equatorial to Galactic coordinate conversion dialog.
     */
    public void findGalacticCoords(ActionEvent actionEvent) {
        try {
            EquatorialToGalacticCoordsDialog dialog = new EquatorialToGalacticCoordsDialog();
            dialog.showAndWait();
        } catch (Exception e) {
            log.error("Error opening galactic coordinates dialog", e);
            showErrorAlert("Coordinate Conversion Error", "Failed to open dialog: " + e.getMessage());
        }
    }
}
