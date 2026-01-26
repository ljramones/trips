package com.teamgannon.trips.controller.menubar;

import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.config.application.model.ApplicationPreferences;
import com.teamgannon.trips.dialogs.preferences.ViewPreferencesDialog;
import com.teamgannon.trips.dialogs.query.QueryDialog;
import com.teamgannon.trips.dialogs.search.*;
import com.teamgannon.trips.dialogs.search.model.*;
import com.teamgannon.trips.events.DisplayStarEvent;
import com.teamgannon.trips.events.HighlightStarEvent;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.graphics.panes.InterstellarSpacePane;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.screenobjects.StarEditDialog;
import com.teamgannon.trips.screenobjects.StarEditStatus;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.StarService;
import javafx.event.ActionEvent;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

/**
 * Controller for the Edit menu.
 * Handles star search, selection, editing, and preferences.
 */
@Slf4j
@Component
public class EditMenuController {

    private final TripsContext tripsContext;
    private final InterstellarSpacePane interstellarSpacePane;
    private final DatabaseManagementService databaseManagementService;
    private final StarService starService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * The query dialog instance - created lazily and reused.
     */
    private QueryDialog queryDialog;

    public EditMenuController(TripsContext tripsContext,
                              InterstellarSpacePane interstellarSpacePane,
                              DatabaseManagementService databaseManagementService,
                              StarService starService,
                              ApplicationEventPublisher eventPublisher) {
        this.tripsContext = tripsContext;
        this.interstellarSpacePane = interstellarSpacePane;
        this.databaseManagementService = databaseManagementService;
        this.starService = starService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Opens the query dialog to select stars from the current dataset.
     */
    public void runQuery(ActionEvent actionEvent) {
        try {
            if (tripsContext.getSearchContext().getDatasetMap().isEmpty()) {
                log.error("There aren't any datasets so don't show");
                showErrorAlert("Search Query", "There aren't any datasets to search on.\nPlease import one first");
            } else {
                // Create the dialog lazily on first use
                if (queryDialog == null) {
                    queryDialog = new QueryDialog(tripsContext.getSearchContext(), eventPublisher);
                    queryDialog.initModality(Modality.NONE);
                }
                queryDialog.refreshDataSets();
                queryDialog.show();
            }
        } catch (Exception e) {
            log.error("Error running query", e);
            showErrorAlert("Query Error", "Failed to run query: " + e.getMessage());
        }
    }

    /**
     * Opens dialog to find a star by name from currently displayed stars.
     */
    public void findInView(ActionEvent actionEvent) {
        try {
            List<StarDisplayRecord> starsInView = interstellarSpacePane.getCurrentStarsInView();
            FindStarInViewDialog findStarInViewDialog = new FindStarInViewDialog(starsInView);
            Optional<FindResults> optional = findStarInViewDialog.showAndWait();
            if (optional.isPresent()) {
                FindResults findResults = optional.get();
                if (findResults.isSelected()) {
                    log.info("Value chose = {}", findResults.getRecord());
                    String recordId = findResults.getRecord().getRecordId();
                    eventPublisher.publishEvent(new HighlightStarEvent(this, recordId));
                    StarObject starObject = starService.getStar(recordId);
                    eventPublisher.publishEvent(new DisplayStarEvent(this, starObject));
                }
            }
        } catch (Exception e) {
            log.error("Error finding star in view", e);
            showErrorAlert("Find in View", "Failed to find star: " + e.getMessage());
        }
    }

    /**
     * Opens dialog to find a star by catalog ID.
     */
    public void findByCatalogId(ActionEvent actionEvent) {
        try {
            log.info("find a star by catalog id");
            List<String> datasetNames = tripsContext.getSearchContext().getDataSetNames();
            if (datasetNames.isEmpty()) {
                showErrorAlert("Find stars", "No datasets in database, please load first");
                return;
            }
            FindStarByCatalogIdDialog dialog = new FindStarByCatalogIdDialog(
                    datasetNames,
                    tripsContext.getSearchContext().getDataSetDescriptor());
            Optional<StarSearchResults> resultsOptional = dialog.showAndWait();
            if (resultsOptional.isPresent()) {
                StarSearchResults results = resultsOptional.get();
                if (results.isStarsFound()) {
                    log.info("found");
                    String datasetName = results.getDataSetName();
                    String catalogId = results.getNameToSearch();
                    log.info("name to search: {}", results.getNameToSearch());
                    List<StarObject> starObjects = starService.findStarsWithCatalogId(datasetName, catalogId);
                    log.info("number of stars found ={}", starObjects.size());
                    ShowStarMatchesDialog showStarMatchesDialog = new ShowStarMatchesDialog(
                            databaseManagementService,
                            starService,
                            starObjects,
                            eventPublisher,
                            tripsContext.getSearchContext().getDatasetMap().get(datasetName),
                            "Catalog ID: " + catalogId);
                    showStarMatchesDialog.showAndWait();
                }
            }
        } catch (Exception e) {
            log.error("Error finding by catalog ID", e);
            showErrorAlert("Find by Catalog ID", "Failed to search: " + e.getMessage());
        }
    }

    /**
     * Opens dialog to find all stars by constellation.
     */
    public void findByConstellation(ActionEvent actionEvent) {
        try {
            FindAllByConstellationDialog dialog = new FindAllByConstellationDialog(tripsContext);
            Optional<ConstellationSelected> optional = dialog.showAndWait();
            if (optional.isPresent()) {
                ConstellationSelected selected = optional.get();
                if (selected.isSelected()) {
                    List<StarObject> starObjectList = starService.findStarsByConstellation(
                            tripsContext.getDataSetDescriptor().getDataSetName(),
                            selected.getConstellation());
                    ShowStarMatchesDialog showStarMatchesDialog = new ShowStarMatchesDialog(
                            databaseManagementService,
                            starService,
                            starObjectList,
                            eventPublisher,
                            tripsContext.getDataSetDescriptor(),
                            "Constellation: " + selected.getConstellation());
                    showStarMatchesDialog.showAndWait();
                }
            }
        } catch (Exception e) {
            log.error("Error finding by constellation", e);
            showErrorAlert("Find by Constellation", "Failed to search: " + e.getMessage());
        }
    }

    /**
     * Opens dialog to find a star by common name.
     */
    public void findByCommonName(ActionEvent actionEvent) {
        try {
            log.info("find a star by common name");
            List<String> datasetNames = tripsContext.getSearchContext().getDataSetNames();
            if (datasetNames.isEmpty()) {
                showErrorAlert("Find stars", "No datasets in database, please load first");
                return;
            }
            FindStarByCommonNameDialog dialog = new FindStarByCommonNameDialog(
                    datasetNames,
                    tripsContext.getSearchContext().getDataSetDescriptor());
            Optional<StarSearchResults> resultsOptional = dialog.showAndWait();
            if (resultsOptional.isPresent()) {
                StarSearchResults results = resultsOptional.get();
                if (results.isStarsFound()) {
                    log.info("found");
                    String datasetName = results.getDataSetName();
                    String commonName = results.getNameToSearch();
                    log.info("name to search: {}", results.getNameToSearch());
                    List<StarObject> starObjects = starService.findStarsByCommonName(datasetName, commonName);
                    log.info("number of stars found ={}", starObjects.size());
                    ShowStarMatchesDialog showStarMatchesDialog = new ShowStarMatchesDialog(
                            databaseManagementService,
                            starService,
                            starObjects,
                            eventPublisher,
                            tripsContext.getSearchContext().getDatasetMap().get(datasetName),
                            "Common Name: " + commonName);
                    showStarMatchesDialog.showAndWait();
                }
            }
        } catch (Exception e) {
            log.error("Error finding by common name", e);
            showErrorAlert("Find by Common Name", "Failed to search: " + e.getMessage());
        }
    }

    /**
     * Opens dialog to find a star by name from entire dataset.
     */
    public void findInDataset(ActionEvent actionEvent) {
        try {
            List<String> datasetNames = tripsContext.getSearchContext().getDataSetNames();
            if (datasetNames.isEmpty()) {
                showErrorAlert("Find stars", "No datasets in database, please load first");
                return;
            }
            FindStarsWithNameMatchDialog findStarsWithNameMatchDialog = new FindStarsWithNameMatchDialog(datasetNames, tripsContext.getSearchContext().getDataSetDescriptor());
            Optional<StarSearchResults> optional = findStarsWithNameMatchDialog.showAndWait();
            if (optional.isPresent()) {
                StarSearchResults starSearchResults = optional.get();
                if (starSearchResults.isStarsFound()) {
                    String datasetName = starSearchResults.getDataSetName();
                    String starName = starSearchResults.getNameToSearch();
                    log.info("name to search: {}", starSearchResults.getNameToSearch());
                    List<StarObject> starObjects = starService.findStarsWithName(datasetName, starName);
                    log.info("number of stars found ={}", starObjects.size());
                    ShowStarMatchesDialog showStarMatchesDialog = new ShowStarMatchesDialog(
                            databaseManagementService,
                            starService,
                            starObjects,
                            eventPublisher,
                            tripsContext.getSearchContext().getDatasetMap().get(datasetName),
                            "Name: " + starName);
                    showStarMatchesDialog.showAndWait();
                }
            }
        } catch (Exception e) {
            log.error("Error finding in dataset", e);
            showErrorAlert("Find in Dataset", "Failed to search: " + e.getMessage());
        }
    }

    /**
     * Opens dialog to find stars related to a selected star within a distance.
     */
    public void findRelatedStars(ActionEvent actionEvent) {
        try {
            List<String> datasetNames = tripsContext.getSearchContext().getDataSetNames();
            if (datasetNames.isEmpty()) {
                showErrorAlert("Find stars", "No datasets in database, please load first");
                return;
            }
            FindRelatedStarsbyDistance findRelatedStarsbyDistanceDialog = new FindRelatedStarsbyDistance(
                    databaseManagementService, starService, datasetNames, tripsContext.getSearchContext().getDataSetDescriptor());
            Optional<MultipleStarSearchResults> optional = findRelatedStarsbyDistanceDialog.showAndWait();
            if (optional.isPresent()) {
                MultipleStarSearchResults starSearchResults = optional.get();
                if (starSearchResults.isStarsFound()) {
                    List<StarDistances> starDistancesList = starSearchResults.getStarObjects();
                    log.info("name to search: {}", starSearchResults.getNameToSearch());
                    log.info("number of stars found ={}", starDistancesList.size());

                    if (starDistancesList.size() < 2) {
                        showErrorAlert("Find related stars", "No stars found");
                        return;
                    }

                    // generate report
                    String report = createRelatedStarsDistanceReport(starDistancesList);

                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setTitle("Save Generated Distances of Related Stars to File");
                    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));

                    Stage stage = getStageFromEvent(actionEvent);
                    File selectedFile = fileChooser.showSaveDialog(stage);

                    if (selectedFile != null) {
                        try (FileWriter writer = new FileWriter(selectedFile)) {
                            writer.write(report);
                        } catch (IOException ex) {
                            log.error("An error occurred while saving the file: " + ex.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error finding related stars", e);
            showErrorAlert("Find Related Stars", "Failed to search: " + e.getMessage());
        }
    }

    private String createRelatedStarsDistanceReport(List<StarDistances> starDistancesList) {
        StringBuilder string = new StringBuilder();
        for (StarDistances starDistance : starDistancesList) {
            StarObject star = starDistance.getStarObject();
            string.append(String.format("Name = %s, " +
                            "spectral class = %s, " +
                            "temperature = %5.1f, " +
                            "mass = %5.1f," +
                            "distance from Sol =%5.1f, " +
                            "declination = %4.1f, " +
                            "ra = %4.1f, " +
                            "Simbad id = %s, " +
                            "coordinates = (%5.1f, %5.1f, %5.1f), " +
                            "distance = %3.1f ly\n",
                    star.getDisplayName(),
                    star.getSpectralClass(),
                    star.getTemperature(),
                    star.getMass(),
                    star.getDistance(),
                    star.getDeclination(),
                    star.getRa(),
                    star.getSimbadId(),
                    star.getX(),
                    star.getY(),
                    star.getZ(),
                    starDistance.getDistance()));
        }
        return string.toString();
    }

    /**
     * Opens dialog to edit a selected star's data.
     */
    public void editStar(ActionEvent actionEvent) {
        try {
            List<StarDisplayRecord> starsInView = interstellarSpacePane.getCurrentStarsInView();
            FindStarInViewDialog findStarInViewDialog = new FindStarInViewDialog(starsInView);
            Optional<FindResults> optional = findStarInViewDialog.showAndWait();
            if (optional.isPresent()) {
                FindResults findResults = optional.get();
                if (findResults.isSelected()) {
                    StarDisplayRecord record = findResults.getRecord();
                    if (record == null) {
                        log.warn("Edit star requested but no record was selected.");
                        showErrorAlert("Edit Star", "No star was selected to edit.");
                        return;
                    }
                    if (record.getRecordId() == null || record.getRecordId().isEmpty()) {
                        log.warn("Edit star requested but recordId was empty for {}", record.getStarName());
                        showErrorAlert("Edit Star", "Selected star has no record id.");
                        return;
                    }
                    StarObject starObject = starService.getStar(record.getRecordId());
                    if (starObject == null) {
                        log.warn("Edit star requested but star record {} was not found.", record.getRecordId());
                        showErrorAlert("Edit Star", "Selected star could not be loaded.");
                        return;
                    }
                    StarEditDialog starEditDialog = new StarEditDialog(starObject);

                    Optional<StarEditStatus> statusOptional = starEditDialog.showAndWait();
                    if (statusOptional.isPresent()) {
                        StarEditStatus starEditStatus = statusOptional.get();
                        if (starEditStatus.isChanged()) {
                            // update the database
                            starService.updateStar(starEditStatus.getRecord());
                        }
                    }
                } else {
                    log.info("cancel request to edit star");
                }
            }
        } catch (Exception e) {
            log.error("Error editing star", e);
            showErrorAlert("Edit Star", "Failed to edit star: " + e.getMessage());
        }
    }

    /**
     * Opens the application preferences dialog.
     */
    public void showApplicationPreferences(ActionEvent actionEvent) {
        try {
            ApplicationPreferences applicationPreferences = tripsContext.getAppPreferences();
            ViewPreferencesDialog viewPreferencesDialog = new ViewPreferencesDialog(tripsContext, applicationPreferences, eventPublisher);
            viewPreferencesDialog.showAndWait();
        } catch (Exception e) {
            log.error("Error showing preferences", e);
            showErrorAlert("Preferences", "Failed to open preferences: " + e.getMessage());
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
