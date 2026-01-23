package com.teamgannon.trips.controller.menubar;

import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.dialogs.search.FindByCatalogIdDialog;
import com.teamgannon.trips.dialogs.search.model.StarSearchResults;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.StarService;
import javafx.event.ActionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

/**
 * Controller for the Search menu.
 * Handles star catalog lookups.
 */
@Slf4j
@Component
public class SearchMenuController {

    private final TripsContext tripsContext;
    private final DatabaseManagementService databaseManagementService;
    private final StarService starService;

    public SearchMenuController(TripsContext tripsContext,
                                DatabaseManagementService databaseManagementService,
                                StarService starService) {
        this.tripsContext = tripsContext;
        this.databaseManagementService = databaseManagementService;
        this.starService = starService;
    }

    /**
     * Opens dialog to find a star by its catalog ID.
     */
    public void FindCatalogId(ActionEvent actionEvent) {
        try {
            log.info("Find catalog id");
            List<String> datasetNames = tripsContext.getSearchContext().getDataSetNames();
            if (datasetNames.isEmpty()) {
                showErrorAlert("Find stars", "No datasets in database, please load first");
                return;
            }
            FindByCatalogIdDialog dialog = new FindByCatalogIdDialog(
                    databaseManagementService,
                    starService,
                    datasetNames,
                    tripsContext.getSearchContext().getDataSetDescriptor());
            Optional<StarSearchResults> resultsOptional = dialog.showAndWait();
            if (resultsOptional.isPresent()) {
                StarSearchResults results = resultsOptional.get();
                if (results.isStarsFound()) {
                    log.info("found, star found = {}", results.getStarObject());
                }
            }
        } catch (Exception e) {
            log.error("Error searching for catalog ID", e);
            showErrorAlert("Search Error", "Failed to search: " + e.getMessage());
        }
    }
}
