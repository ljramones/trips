package com.teamgannon.trips.controller.menubar;

import com.teamgannon.trips.config.application.Localization;
import com.teamgannon.trips.controller.shared.SharedUIFunctions;
import com.teamgannon.trips.controller.shared.SharedUIState;
import com.teamgannon.trips.dialogs.dataset.DataSetManagerDialog;
import com.teamgannon.trips.service.DataExportService;
import com.teamgannon.trips.service.DataImportService;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.DatasetService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MenuBarController {

    private final SharedUIState sharedUIState;
    private final SharedUIFunctions sharedUIFunctions;
    private final DatabaseManagementService databaseManagementService;
    private final DatasetService datasetService;
    private final ApplicationEventPublisher eventPublisher;
    private final DataImportService dataImportService;
    private final Localization localization;

    @FXML
    public MenuBar menuBar;

    @FXML
    public MenuItem importDataSetMenuItem;
    @FXML
    public MenuItem openDatasetMenuItem;
    @FXML
    public MenuItem saveMenuItem;
    @FXML
    public MenuItem quitMenuItem;
    @FXML
    public CheckMenuItem toggleGridMenuitem;
    @FXML
    public CheckMenuItem toggleExtensionsMenuitem;
    @FXML
    public CheckMenuItem toggleLabelsMenuitem;
    @FXML
    public CheckMenuItem toggleScaleMenuitem;
    @FXML
    public CheckMenuItem togglePolitiesMenuitem;
    @FXML
    public CheckMenuItem toggleStarMenuitem;
    @FXML
    public CheckMenuItem toggleTransitsMenuitem;
    @FXML
    public CheckMenuItem toggleTransitLengthsMenuitem;
    @FXML
    public CheckMenuItem toggleRoutesMenuitem;
    @FXML
    public CheckMenuItem toggleRouteLengthsMenuitem;
    @FXML
    public CheckMenuItem toggleSidePaneMenuitem;
    @FXML
    public CheckMenuItem toggleToolBarMenuitem;
    @FXML
    public CheckMenuItem toggleStatusBarMenuitem;
    @FXML
    public MenuItem showRoutesMenuitem;
    private DataExportService dataExportService;

    public MenuBarController(SharedUIState sharedUIState,
                             SharedUIFunctions sharedUIFunctions,
                             DatabaseManagementService databaseManagementService,
                             DatasetService datasetService,
                             ApplicationEventPublisher eventPublisher,
                             DataImportService dataImportService,
                             Localization localization) {
        this.sharedUIState = sharedUIState;
        this.sharedUIFunctions = sharedUIFunctions;


        this.databaseManagementService = databaseManagementService;
        this.datasetService = datasetService;
        this.eventPublisher = eventPublisher;
        this.dataImportService = dataImportService;
        this.localization = localization;
    }

    public void setup(DataExportService dataExportService) {

        this.dataExportService = dataExportService;
    }


    public void loadDataSetManager(ActionEvent actionEvent) {

    }

    public void selectActiveDataset(ActionEvent actionEvent) {

    }

    public void saveDataset(ActionEvent actionEvent) {

    }

    public void quit(ActionEvent actionEvent) {

    }

    public void runQuery(ActionEvent actionEvent) {

    }

    public void findInView(ActionEvent actionEvent) {

    }

    public void findByCatalogId(ActionEvent actionEvent) {

    }

    public void findByConstellation(ActionEvent actionEvent) {

    }

    public void findByCommonName(ActionEvent actionEvent) {

    }

    public void findInDataset(ActionEvent actionEvent) {

    }

    public void findRelatedStars(ActionEvent actionEvent) {

    }

    public void editStar(ActionEvent actionEvent) {

    }

    public void showApplicationPreferences(ActionEvent actionEvent) {

    }

    public void plotStars(ActionEvent actionEvent) {

    }

    public void toggleGrid(ActionEvent actionEvent) {

    }

    public void toggleGridExtensions(ActionEvent actionEvent) {

    }

    public void toggleLabels(ActionEvent actionEvent) {

    }

    public void toggleScale(ActionEvent actionEvent) {

    }

    public void togglePolities(ActionEvent actionEvent) {

    }

    public void toggleStars(ActionEvent actionEvent) {

    }

    public void toggleTransitAction(ActionEvent actionEvent) {

    }

    public void toggleTransitLengths(ActionEvent actionEvent) {

    }

    public void toggleRoutes(ActionEvent actionEvent) {

    }

    public void toggleRouteLengths(ActionEvent actionEvent) {

    }

    public void toggleSidePane(ActionEvent actionEvent) {

    }

    public void toggleToolbar(ActionEvent actionEvent) {

    }

    public void toggleStatusBar(ActionEvent actionEvent) {

    }

    public void showRoutes(ActionEvent actionEvent) {

    }

    public void onSnapShot(ActionEvent actionEvent) {

    }

    public void FindCatalogId(ActionEvent actionEvent) {

    }

    public void routeFinderInView(ActionEvent actionEvent) {

    }

    public void routeFinderDataset(ActionEvent actionEvent) {

    }

    public void clickRoutes(ActionEvent actionEvent) {

    }

    public void editDeleteRoutes(ActionEvent actionEvent) {

    }

    public void clearRoutes(ActionEvent actionEvent) {

    }

    public void transitFinder(ActionEvent actionEvent) {

    }

    public void clearTransits(ActionEvent actionEvent) {

    }

    public void advancedSearch(ActionEvent actionEvent) {

    }

    public void distanceReport(ActionEvent actionEvent) {

    }

    public void routeListReport(ActionEvent actionEvent) {

    }

    public void starPropertyReport(ActionEvent actionEvent) {

    }

    public void findDistance(ActionEvent actionEvent) {

    }

    public void findXYZ(ActionEvent actionEvent) {

    }

    public void findGalacticCoords(ActionEvent actionEvent) {

    }

    public void findInSesame(ActionEvent actionEvent) {

    }

    public void aboutTrips(ActionEvent actionEvent) {

    }

    public void howToSupport(ActionEvent actionEvent) {

    }

    public void checkUpdate(ActionEvent actionEvent) {

    }

    public void getInventory(ActionEvent actionEvent) {

    }
}
