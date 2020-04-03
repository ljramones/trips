package com.teamgannon.trips.dead;

import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.controls.ApplicationPreferencesPane;
import com.teamgannon.trips.dialog.ProgressDialog;
import com.teamgannon.trips.elasticsearch.model.AstrographicObject;
import com.teamgannon.trips.elasticsearch.model.DataSetDescriptor;
import com.teamgannon.trips.elasticsearch.repository.AstrographicObjectRepository;
import com.teamgannon.trips.file.chview.ChviewReader;
import com.teamgannon.trips.file.chview.model.ChViewFile;
import com.teamgannon.trips.file.excel.ExcelReader;
import com.teamgannon.trips.file.excel.model.RBExcelFile;
import com.teamgannon.trips.graphics.AstrographicPlotter;
import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.graphics.operators.ContextSelector;
import com.teamgannon.trips.graphics.operators.ListUpdater;
import com.teamgannon.trips.graphics.operators.StellarPropertiesDisplayer;
import com.teamgannon.trips.graphics.panes.InterstellarSpacePane;
import com.teamgannon.trips.graphics.panes.SolarSystemSpacePane;
import com.teamgannon.trips.javafxspringbootsupport.FXMLController;
import com.teamgannon.trips.model.StarBase;
import com.teamgannon.trips.search.*;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.tableviews.DataSetTable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Point3D;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.util.*;
import java.util.stream.IntStream;

@Slf4j
@FXMLController
public class ComplexPresenter
        implements ListUpdater,
        StellarPropertiesDisplayer,
        StellarDataUpdater,
        ContextSelector {


    ///////////////////// Spring components /////////

    private DatabaseManagementService databaseManagementService;

    private ApplicationContext appContext;

    private ChviewReader chviewReader;

    private ExcelReader excelReader;

    private AstrographicPlotter astrographicPlotter;

    private StarBase starBase;

    private SearchContext searchContext;

    private TripsContext tripsContext;

    private AstrographicObjectRepository astrographicObjectRepository;


    //////////////  java fx fields  //////////////

    public VBox settingsPane;

    public VBox mainPanel;

    public MenuBar mainMenu;

    public ToolBar toolBar;

    public HBox statusBar;

    public GridPane propertiesPane;

    public GridPane tripsPropertiesPane;

    public Label databaseStatus;

    public Accordion propertiesAccordion;

    public StackPane leftDisplayPane;

    public SplitPane mainSplitPane;

    public ToggleButton toggleSettings;

    //////////// accordion items

    public TitledPane viewPreferencesPane;

    public TitledPane stellarObjectPane;

    public TitledPane objectsViewPane;

    public TitledPane searchPane;

    public TitledPane routingPane;

    public ScrollPane queryScrollPane;

    /**
     * the main graphics panes for showing interstellar space
     */
    private InterstellarSpacePane interstellarSpacePane;

    private boolean gridOn = true;
    private boolean extensionsOn = true;
    private boolean starsOn = true;
    private boolean scaleOn = true;
    private boolean routesOn = true;

    /**
     * the lsit of star display recored that are available for this graph
     */
    private List<StarDisplayRecord> recordList = new ArrayList<>();

    /**
     * list of routes
     */
    private List<RouteDescriptor> routeList = new ArrayList<>();

    /**
     * the set of colors that we use to randomize
     */
    private Color[] colors = new Color[10];

    /**
     * solar system panes for showing the details of various solar systems
     */
    protected SolarSystemSpacePane solarSystemSpacePane;

    /**
     * temporary data for chview data testing
     */
    private ChViewFile chViewFile;


    /////// data objects ///////////

    /**
     * backing array for listable stellar objects - the M for the MVC of Listview
     */
    private List<Map<String, String>> objectsInView = new ArrayList<>();

    /**
     * observable list that provides the C for the MVC of the ListView
     */
    private ObservableList<Map<String, String>> stellarObjectList = FXCollections.observableArrayList(objectsInView);

    /**
     * the ListView UI control for displaying lists - the V for the MVC of Listview
     */
    private ListView<Map<String, String>> stellarObjectsListView = new ListView<>(stellarObjectList);


    //////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * The main constructor for the screen
     *
     * @param databaseManagementService    the database management service
     * @param appContext                   the Spring application
     * @param chviewReader                 the reader for CH View data
     * @param excelReader                  the reader for RB Excel format files
     * @param astrographicPlotter          the plotter for star data
     * @param starBase                     the local in memory db
     * @param tripsContext                 the trips context
     * @param astrographicObjectRepository the backing store for star data
     */
    public ComplexPresenter(
            DatabaseManagementService databaseManagementService,
            ApplicationContext appContext,
            ChviewReader chviewReader,
            ExcelReader excelReader,
            AstrographicPlotter astrographicPlotter,
            StarBase starBase,
            TripsContext tripsContext,
            AstrographicObjectRepository astrographicObjectRepository
    ) {
        this.databaseManagementService = databaseManagementService;
        this.appContext = appContext;
        this.chviewReader = chviewReader;
        this.excelReader = excelReader;
        this.astrographicPlotter = astrographicPlotter;
        this.starBase = starBase;
        this.tripsContext = tripsContext;
        this.searchContext = tripsContext.getSearchContext();
        this.astrographicObjectRepository = astrographicObjectRepository;
    }

    /**
     * initialize the view controller
     */
    public void initialize() {
        log.info("\n\n\n--------- initializing main controller    ---------");
        showStatus("No database loaded");

        settingsPane.setMinWidth(0);

        DoubleProperty splitPaneDividerPosition = mainSplitPane.getDividers().get(0).positionProperty();
        splitPaneDividerPosition.addListener((obs, oldPos, newPos) -> toggleSettings.setSelected(newPos.doubleValue() < 0.95));

        viewPreferencesPane.setContent(new ApplicationPreferencesPane(tripsContext));

        queryScrollPane.setContent(new SearchPane(searchContext.getAstroSearchQuery(), this));

        // setup the object in view
        setupStellarObjectListView();

        // setup graphics panes for display
        setupInterstellarPane();

        // setup solar system panes
        setupSolarSystemPane();

        // put the interstellar space on top and the solarsystem to the back
        interstellarSpacePane.toFront();

        // set the interstellar pane to be the drawing surface
        astrographicPlotter.setInterstellarPane(interstellarSpacePane);

    }


    ////////////////  display toggles  //////////////////////
    public void toggleStars(ActionEvent event) {
        interstellarSpacePane.toggleStars(starsOn);
    }

    public void toggleExtensions(ActionEvent event) {
        interstellarSpacePane.toggleExtensions(extensionsOn);
    }

    public void toggleRoutes(ActionEvent event) {
        interstellarSpacePane.toggleRoutes(routesOn);
    }

    /**
     * toggle the grid display
     *
     * @param actionEvent the action event which we do not use
     */
    public void toggleGrid(ActionEvent actionEvent) {
        interstellarSpacePane.toggleGrid(gridOn);
    }

    /**
     * toggle the scale display
     *
     * @param actionEvent the action event which we do not use
     */
    public void toggleScale(ActionEvent actionEvent) {
        interstellarSpacePane.toggleScale(scaleOn);
    }


    /**
     * toggle the setting side panel
     *
     * @param actionEvent the event
     */
    public void toggleSidePanel(ActionEvent actionEvent) {
        if (toggleSettings.isSelected()) {
            mainSplitPane.setDividerPositions(0.8);
        } else {
            mainSplitPane.setDividerPositions(1.0);
        }
    }


    public void toggleToolbar(ActionEvent event) {
        if (toolBar.isVisible()) {
            toolBar.setVisible(false);
        } else {
            toolBar.setVisible(true);
        }
    }

    public void toggleStatusBar(ActionEvent event) {
        if (statusBar.isVisible()) {
            statusBar.setVisible(false);
        } else{
            statusBar.setVisible(true);
        }
    }


    /////////////////////////////////////////////

    /**
     * show the about
     */
    public void aboutMenuItem() {
        log.info("Showing about");
        Alert alert = new Alert(AlertType.INFORMATION);
        // @TODO need to write this
    }

    /**
     * drop database event endpoint
     *
     * @param actionEvent the incoming action
     */
    public void dropDatabase(ActionEvent actionEvent) {
        log.debug("Drop Database");
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Drop Database");
        alert.setHeaderText("Drop Database?");
        alert.setContentText("Warning, this will clear the existing database and cannot be undone!");
        Optional<ButtonType> result = alert.showAndWait();

        if ((result.isPresent()) && (result.get() == ButtonType.OK)) {

            ProgressDialog progressDialog = new ProgressDialog("Dropping Database");

            // In real life this task would do something useful and return
            // some meaningful result:
            Task<Void> task = new Task<Void>() {
                @Override
                public Void call() throws InterruptedException {
                    for (int i = 0; i < 10; i++) {
                        updateProgress(i, 10);
                        Thread.sleep(200);
                    }
                    updateProgress(10, 10);
                    return null;
                }
            };

            // binds progress of progress bars to progress of task:
            progressDialog.activateProgressBar(task);

            // in real life this method would get the result of the task
            // and update the UI based on its value:
            task.setOnSucceeded(event -> {
                progressDialog.getDialogStage().close();
                showStatus("database was dropped");
            });

            progressDialog.getDialogStage().show();

            Thread thread = new Thread(task);
            thread.start();
            boolean dropStarted = true;
            databaseManagementService.dropDatabase();
            dropStarted = false;
            showStatus("database was dropped");
        }
    }

    /**
     * import a CSV file into the database
     *
     * @param actionEvent the incoming event
     */
    public void importDatabase(ActionEvent actionEvent) {
        log.debug("This is will import a csv file");
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Import Database");
        alert.setHeaderText("Import Database?");
        alert.setContentText("Warning, this will overwrite the existing database and cannot be undone!");
        Optional<ButtonType> result = alert.showAndWait();

        if ((result.isPresent()) && (result.get() == ButtonType.OK)) {
            final FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Database to import");
            File file = fileChooser.showOpenDialog(getStage());
            if (file != null) {
                importDatabase(file);
            } else {
                log.warn("file selection cancelled");
            }
        } else {
            log.warn("file selection cancelled");
        }
    }

    /**
     * export the database as a CSV file
     *
     * @param actionEvent the incoming event
     */
    public void exportDatabase(ActionEvent actionEvent) {
        log.debug("Export the existing database as a CSV file");
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Database to export as a CSV file");
        File file = fileChooser.showSaveDialog(getStage());
        if (file != null) {
            exportDB(file);
        } else {
            log.warn("file selection cancelled");
        }
    }


    /**
     * event handler for the menu shutdown selection
     *
     * @param actionEvent the incoming action event
     */
    public void shutdown(ActionEvent actionEvent) {
        log.debug("Exit selection");
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Exit Application");
        alert.setHeaderText("Exit Application?");
        alert.setContentText("Are you sure you want to leave?");
        Optional<ButtonType> result = alert.showAndWait();

        if ((result.isPresent()) && (result.get() == ButtonType.OK)) {
            initiateShutdown(0);
        }
    }


    // ---------------- ListUpdater interface -------------------------//

    /**
     * update the list
     *
     * @param listItem the list item
     */
    @Override
    public void updateList(Map<String, String> listItem) {
        stellarObjectList.add(listItem);
        log.info(listItem.get("name"));
    }

    /**
     * clear the entire list
     */
    @Override
    public void clearList() {
        stellarObjectList.clear();
    }

    // ------------------ StellarPropertiesDisplayer interface --------------//

    @Override
    public void displayStellarProperties(Map<String, String> stellarObjectSelected) {
        displayProperties(stellarObjectSelected);
    }


    /**
     * select a interstellar system space
     *
     * @param objectProperties the properties of the selected object
     */
    @Override
    public void selectInterstellarSpace(Map<String, String> objectProperties) {
        log.info("Showing interstellar Space");
        interstellarSpacePane.toFront();
    }

    /**
     * select a solar system
     *
     * @param objectProperties the properties of the selected object
     */
    @Override
    public void selectSolarSystemSpace(Map<String, String> objectProperties) {
        log.info("Showing a solar system");
        solarSystemSpacePane.setSystemToDisplay(objectProperties);
        solarSystemSpacePane.render();
        solarSystemSpacePane.toFront();
    }

    ////////   event handlers ////////////////////

    /**
     * simulate a set of 40 sets (for testing purposes only)
     *
     * @param actionEvent the action event which we do not use
     */
    public void simulate(ActionEvent actionEvent) {
        simulate();
    }

    /////////// to be done yet

    public void undo(ActionEvent actionEvent) {
        showUnsupportedMessage();
    }

    public void redo(ActionEvent actionEvent) {
        showUnsupportedMessage();
    }

    public void copy(ActionEvent actionEvent) {
        showUnsupportedMessage();
    }

    public void cut(ActionEvent actionEvent) {
        showUnsupportedMessage();
    }

    public void paste(ActionEvent actionEvent) {
        showUnsupportedMessage();
    }

    public void selectAll(ActionEvent actionEvent) {
        showUnsupportedMessage();
    }

    public void unSelectAll(ActionEvent actionEvent) {
        showUnsupportedMessage();
    }

    public void addStar(ActionEvent actionEvent) {
        showUnsupportedMessage();
    }


    public void showLinks(ActionEvent actionEvent) {
        showUnsupportedMessage();
    }

    public void showDistance(ActionEvent actionEvent) {
        showUnsupportedMessage();
    }

    public void showScope(ActionEvent actionEvent) {
        showUnsupportedMessage();
    }

    public void showRoutes(ActionEvent actionEvent) {
        showUnsupportedMessage();
    }

    public void viewObject(ActionEvent actionEvent) {
        showUnsupportedMessage();
    }

    public void editObject(ActionEvent actionEvent) {
        showUnsupportedMessage();
    }

    public void deleteObject(ActionEvent actionEvent) {
        showUnsupportedMessage();
    }

    public void showToolbar(ActionEvent actionEvent) {
        showUnsupportedMessage();
    }

    public void showSlideBar(ActionEvent actionEvent) {
        showUnsupportedMessage();
    }

    public void showStatusBar(ActionEvent actionEvent) {
        showUnsupportedMessage();
    }

    public void showPreferences(ActionEvent actionEvent) {
        showUnsupportedMessage();
    }

    public void invertColors(ActionEvent actionEvent) {
        showUnsupportedMessage();
    }

    public void goTo(ActionEvent actionEvent) {
        showUnsupportedMessage();
    }

    public void centre(ActionEvent actionEvent) {
        showUnsupportedMessage();
    }

    public void reset(ActionEvent actionEvent) {
        showUnsupportedMessage();
    }

    public void hideSelected(ActionEvent actionEvent) {
        showUnsupportedMessage();
    }

    public void showAll(ActionEvent actionEvent) {
        showUnsupportedMessage();
    }

    public void showOnlySelected(ActionEvent actionEvent) {
        showUnsupportedMessage();
    }

    public void support(ActionEvent actionEvent) {
        showUnsupportedMessage();
    }


    public void distanceReport(ActionEvent actionEvent) {
        showUnsupportedMessage();
    }

    public void densityReport(ActionEvent actionEvent) {
        showUnsupportedMessage();
    }

    public void cubicDensityReport(ActionEvent actionEvent) {
        showUnsupportedMessage();
    }

    public void clustersReport(ActionEvent actionEvent) {
        showUnsupportedMessage();
    }

    public void routeFinderReport(ActionEvent actionEvent) {
        showUnsupportedMessage();
    }

    public void crossroadsReport(ActionEvent actionEvent) {
        showUnsupportedMessage();
    }

    public void civilizationsReport(ActionEvent actionEvent) {
        showUnsupportedMessage();
    }

    public void checkForUpdate(ActionEvent actionEvent) {
        showUnsupportedMessage();
    }


    // ------------------ File Loaders and Exporters ----------------------------- //

    /**
     * load a CH View binary file
     *
     * @param actionEvent the associated action event that triggred this
     */
    public void importCHV(ActionEvent actionEvent) {
        log.debug("Import a CHV format file");
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select CHV file to import");
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("CHV Files", "chv");
        fileChooser.setSelectedExtensionFilter(filter);
        File file = fileChooser.showOpenDialog(getStage());
        if (file != null) {
            // load chview file
            chViewFile = chviewReader.loadFile(file);
            try {
                databaseManagementService.loadCHFile(chViewFile);
            } catch (Exception e) {
                loadErrorAlert();
            }
        } else {
            log.warn("file selection cancelled");
        }
    }


    public void importRBExcel(ActionEvent actionEvent) {
        log.debug("Import a RB Excel format file");
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Excel file to import");
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("XLSX Files", "xlsx");
        fileChooser.setSelectedExtensionFilter(filter);
        File file = fileChooser.showOpenDialog(getStage());
        if (file != null) {
            // load RB excel file
            RBExcelFile excelFile = excelReader.loadFile(file);
            try {
                databaseManagementService.loadRBStarSet(excelFile);
            } catch (Exception e) {
                loadErrorAlert();
            }
        } else {
            log.warn("file selection cancelled");
        }
    }

    private void loadErrorAlert() {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Duplicate Dataset");
        String s = "This dataset was already loaded in the system ";
        alert.setContentText(s);
        alert.showAndWait();
        log.error("This dataset was already loaded in the system");
    }

    public void exportJSON(ActionEvent actionEvent) {
        log.debug("Export as a CNV format file");
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select JSON file to import");
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("JSON Files", "json");
        fileChooser.setSelectedExtensionFilter(filter);
        File file = fileChooser.showSaveDialog(getStage());
        if (file != null) {
            chviewReader.exportJson(file, chViewFile);
        } else {
            log.warn("file selection cancelled");
        }
    }


    public void importSimbad(ActionEvent actionEvent) {
        showUnsupportedMessage();
//        log.debug("Import a Simbad CSV format file");
//        final FileChooser fileChooser = new FileChooser();
//        fileChooser.setTitle("Select CSV file to import");
//        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("CHV Files", "chv");
//        fileChooser.setSelectedExtensionFilter(filter);
//        File file = fileChooser.showOpenDialog(getStage());
//        if (file != null) {
//            // load simbad file
//            chViewFile = chviewReader.loadFile(file);
//
//        } else {
//            log.warn("file selection cancelled");
//        }
    }

    public void exportSimbad(ActionEvent actionEvent) {
        showUnsupportedMessage();
    }

    public void importHipparcos3(ActionEvent actionEvent) {
        showUnsupportedMessage();
//        log.debug("Import a Hipparcos 3 CSV format file");
//        final FileChooser fileChooser = new FileChooser();
//        fileChooser.setTitle("Select CHV file to import");
//        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("CHV Files", "chv");
//        fileChooser.setSelectedExtensionFilter(filter);
//        File file = fileChooser.showOpenDialog(getStage());
//        if (file != null) {
//            // loaf chview file
//            chViewFile = chviewReader.loadFile(file);
//        } else {
//            log.warn("file selection cancelled");
//        }
    }

    public void exportHipparcos3(ActionEvent actionEvent) {
        showUnsupportedMessage();
    }


    public void showOnFilters(ActionEvent actionEvent) {

        showSearchPane();

        PlotQueryFilterWindow codedDialog = new PlotQueryFilterWindow(searchContext.getAstroSearchQuery());
        Optional<AstroSearchQuery> result = codedDialog.showAndWait();
        result.ifPresent(this::showNewStellarData);

    }

    public void showNewStellarData(AstroSearchQuery searchQuery) {
        log.info(searchQuery.toString());
        searchContext.setAstroSearchQuery(searchQuery);
        // do a search and cause the plot to show it

        // ******************************************************

        List<AstrographicObject> astrographicObjects = astrographicObjectRepository.findBySearchQuery(searchQuery);
        log.info("New DB Query returns {} stars", astrographicObjects.size());

        if (!astrographicObjects.isEmpty()) {
            astrographicPlotter.drawAstrographicData(astrographicObjects, searchQuery.getCenterCoordinates());
        } else {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Astrographic data view error");
            String s = "No Astrographic data was loaded ";
            alert.setContentText(s);
            alert.showAndWait();
        }
        showSearchPane();
    }


    public void showPlot(ActionEvent actionEvent) {
        List<DataSetDescriptor> datasets = databaseManagementService.getDataSetIds();

        List<String> dialogData = new ArrayList<>();
        for (DataSetDescriptor dataSetDescriptor : datasets) {
            dialogData.add(dataSetDescriptor.getDataSetName());
        }

        ChoiceDialog dialog = new ChoiceDialog(dialogData.get(0), dialogData);
        dialog.setTitle("Choice Data set to display");
        dialog.setHeaderText("Select your choice - (Default display is 15 light years from Earth, use Show Stars filter to change)");

        Optional<String> result = dialog.showAndWait();
        String selected = "cancelled.";

        if (result.isPresent()) {
            selected = result.get();

            DataSetDescriptor dataSetDescriptor = findFromDataSet(selected, datasets);
            if (dataSetDescriptor == null) {
                log.error("How the hell did this happen");
                return;
            }

            List<AstrographicObject> astrographicObjects = databaseManagementService.getFromDatasetWithinLimit(dataSetDescriptor, searchContext.getAstroSearchQuery().getDistanceFromCenterStar());
            log.info("DB Query returns {} stars", astrographicObjects.size());

            if (!astrographicObjects.isEmpty()) {
                astrographicPlotter.drawAstrographicData(astrographicObjects, searchContext.getAstroSearchQuery().getCenterCoordinates());
            } else {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Astrographic data view error");
                String s = "No Astrographic data was loaded ";
                alert.setContentText(s);
                alert.showAndWait();
            }
        }
    }


    /**
     * show a table with CHV data
     *
     * @param actionEvent the action event
     */
    public void showTableData(ActionEvent actionEvent) {
        List<DataSetDescriptor> datasets = databaseManagementService.getDataSetIds();

        List<String> dialogData = new ArrayList<>();
        for (DataSetDescriptor dataSetDescriptor : datasets) {
            dialogData.add(dataSetDescriptor.getDataSetName());
        }

        ChoiceDialog dialog = new ChoiceDialog(dialogData.get(0), dialogData);
        dialog.setTitle("Choice Data set to display");
        dialog.setHeaderText("Select your choice");

        Optional<String> result = dialog.showAndWait();
        String selected = "cancelled.";

        if (result.isPresent()) {
            selected = result.get();

            DataSetDescriptor dataSetDescriptor = findFromDataSet(selected, datasets);
            if (dataSetDescriptor == null) {
                log.error("How the hell did this happen");
                return;
            }

            List<AstrographicObject> astrographicObjects = databaseManagementService.getFromDatasetWithinLimit(dataSetDescriptor, 15.0);
            if (!astrographicObjects.isEmpty()) {
                new DataSetTable(getStage(), astrographicObjects);
            } else {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Astrographic data view error");
                String s = "No Astrographic data was loaded ";
                alert.setContentText(s);
                alert.showAndWait();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    private void showSearchPane() {

//        queryScrollPane.setContent(new SearchPane(searchContext.getAstroSearchQuery()));
        searchPane.setText("Search Pane");
        searchPane.setExpanded(true);
        searchPane.setAnimated(false);
        searchPane.setDisable(false);

//        propertiesAccordion.getPanes().add(searchPane);
        propertiesAccordion.setExpandedPane(searchPane);
        log.info("show search pane");
    }

    private void removeSearchPane() {
        propertiesAccordion.getPanes().remove(searchPane);
        log.info("remove search pane");
    }

    private DataSetDescriptor findFromDataSet(String selected, List<DataSetDescriptor> datasets) {
        for (DataSetDescriptor dataSetDescriptor : datasets) {
            if (dataSetDescriptor.getDataSetName().equals(selected)) {
                return dataSetDescriptor;
            }
        }
        return null;
    }


    // -----------------  helpers ------------ //


    /**
     * call the database import method
     *
     * @param file the file to load
     */
    private void importDatabase(File file) {
        databaseManagementService.importDatabase(file);
        log.info("File selection is:" + file.getAbsolutePath());
        showStatus("Database loaded:" + file.getName());

    }


    /**
     * export the database as a CSV file
     *
     * @param file the file to export
     */
    private void exportDB(File file) {
        databaseManagementService.exportDatabase(file);
        log.info("File selection is:" + file.getAbsolutePath());
    }

    /**
     * get the parent window for this application
     *
     * @return the primary primaryStage
     */
    private Stage getStage() {
        return (Stage) mainPanel.getScene().getWindow();
    }

    /**
     * shutdown the application
     *
     * @param returnCode shoudl be a return code of zero meaning success
     */
    private void initiateShutdown(int returnCode) {
        // close the spring context which invokes all the bean destroy methods
        SpringApplication.exit(appContext, () -> returnCode);
        // now exit the application
        System.exit(returnCode);
    }

    /**
     * show a status message
     *
     * @param message the status message
     */
    private void showStatus(String message) {
        databaseStatus.setText(message);
    }


    /**
     * Displays the properties of the astronomical object of name given as a parameter.
     *
     * @param properties Astronomical object unique name.
     */
    private void displayProperties(Map<String, String> properties) {
        String objectId = properties.get("name");
        String record = properties.get("recordNumber");
        UUID recordNumber = UUID.fromString(record);
        Map<String, String> characteristics = starBase.getRecordFields(recordNumber);
        propertiesPane.getChildren().clear();
        int i = 0;
        for (String key : characteristics.keySet()) {
            Label keyLabel = new Label(key + ":");
            keyLabel.setWrapText(true);
            keyLabel.setTextAlignment(TextAlignment.JUSTIFY);
            Label charLabel = new Label(characteristics.get(key));
            charLabel.setWrapText(true);
            charLabel.setTextAlignment(TextAlignment.JUSTIFY);
            charLabel.setPrefWidth(200);
            propertiesPane.addRow(i++, keyLabel, charLabel);
        }

        stellarObjectPane.setDisable(false);
        propertiesAccordion.setExpandedPane(stellarObjectPane);
    }


    /**
     * sets up list view for stellar objects
     */
    private void setupStellarObjectListView() {

        // initially greyed
//        objectsViewPane.setDisable(true);

        // setup model to display in case we turn on
        objectsViewPane.setContent(stellarObjectsListView);

        stellarObjectsListView.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue<? extends Map<String, String>> ov, Map<String, String> old_val, Map<String, String> newSelection) -> {
                    displayProperties(newSelection);
                    log.info("Object Selected is:" + newSelection);
                });

        stellarObjectsListView.setCellFactory(new Callback<ListView<Map<String, String>>, ListCell<Map<String, String>>>() {

            @Override
            public ListCell<Map<String, String>> call(ListView<Map<String, String>> p) {

                objectsViewPane.setDisable(false);
                searchPane.setDisable(false);

                return new ListCell<Map<String, String>>() {

                    @Override
                    protected void updateItem(Map<String, String> objectProperties, boolean bln) {
                        super.updateItem(objectProperties, bln);
                        if (objectProperties != null) {
                            setText(objectProperties.get("name"));
                        }
                    }

                };
            }
        });
    }


    /**
     * sets up a graphical plane to show a solar system of a specified star or multiple star system
     */
    private void setupSolarSystemPane() {
        solarSystemSpacePane = new SolarSystemSpacePane(leftDisplayPane.getMaxWidth(), leftDisplayPane.getMaxHeight());
        solarSystemSpacePane.setContextUpdater(this);
        leftDisplayPane.getChildren().add(solarSystemSpacePane);
    }

    /**
     * setup a graphical panes to display interstellar space
     */
    private void setupInterstellarPane() {
        interstellarSpacePane = new InterstellarSpacePane((int) leftDisplayPane.getMaxWidth(), (int) leftDisplayPane.getMaxHeight(), 700, 20);
        interstellarSpacePane.setListUpdater(this);
        interstellarSpacePane.setStellarObjectDisplayer(this);
        interstellarSpacePane.setContextUpdater(this);

        leftDisplayPane.getChildren().add(interstellarSpacePane);
    }

    private void showUnsupportedMessage() {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Work in progress");
        String s = "not supported yet ";
        alert.setContentText(s);
        alert.showAndWait();
    }

    /////////////////////


    /**
     * simulate stars
     */
    @FXML
    private void simulate() {

        initializeColors();

        log.info("clearing stars");
        recordList.clear();

        log.info("drawing 25 random stars, plus earth");
        recordList.add(createSolNode());
        IntStream.range(0, 25).forEach(i -> recordList.add(createStarNode()));

        interstellarSpacePane.drawStar(recordList, "Sol");
    }


    /**
     * create an earth node
     *
     * @return the earth node
     */
    private StarDisplayRecord createSolNode() {
        return StarDisplayRecord.builder()
                .starName("Sol")
                .starColor(Color.YELLOW)
                .radius(2)
                .recordId(UUID.randomUUID())
                .coordinates(new Point3D(0, 0, 0))
                .build();

    }

    private StarDisplayRecord createStarNode() {
        double starSize = 10 * Math.random() + 1;

        Random random = new Random();
        double width = interstellarSpacePane.getWidth();
        double height = interstellarSpacePane.getHeight();
        double depth = interstellarSpacePane.getDepth();

        double x = width / 2.0 * random.nextDouble()
                * (random.nextBoolean() ? 1 : -1);
        // we flip the y axis since it points down
        double y = -height / 2.0 * random.nextDouble()
                * (random.nextBoolean() ? 1 : -1);
        double z = depth / 2.0 * random.nextDouble()
                * (random.nextBoolean() ? 1 : -1);

        Color color = chooseRandomColor();
        String name = generateRandomLabel();

        return StarDisplayRecord.builder()
                .starName(name)
                .starColor(color)
                .radius(starSize)
                .recordId(UUID.randomUUID())
                .coordinates(new Point3D(x, y, z))
                .build();
    }


    /**
     * generate a random color
     *
     * @return the color
     */
    private Color chooseRandomColor() {
        int index = (int) (10 * Math.random());
        return colors[index];
    }


    /**
     * generate a random label
     *
     * @return the label
     */
    private String generateRandomLabel() {
        int i = (int) (100 * Math.random());
        return "star-" + i;
    }


    /**
     * initialize the colors that we will select form
     */
    private void initializeColors() {
        colors[0] = Color.ALICEBLUE;
        colors[1] = Color.CHARTREUSE;
        colors[2] = Color.RED;
        colors[3] = Color.YELLOW;
        colors[4] = Color.YELLOWGREEN;
        colors[5] = Color.GREEN;
        colors[6] = Color.PEACHPUFF;
        colors[7] = Color.GOLD;
        colors[8] = Color.DARKMAGENTA;
        colors[9] = Color.OLIVE;
    }

    private RouteDescriptor createRoute() {
        RouteDescriptor route
                = RouteDescriptor.builder()
                .name("TestRoute")
                .color(Color.LIGHTCORAL)
                .maxLength(10)
                .startStar("Sol")
                .build();

        List<Point3D> lineSegments = new ArrayList<>();
        lineSegments.add(new Point3D(0, 0, 0));
        lineSegments.add(new Point3D(10, 10, 10));
        lineSegments.add(new Point3D(5, -2, 10));
        lineSegments.add(new Point3D(15, 2, 15));
        lineSegments.add(new Point3D(20, 8, 15));
        lineSegments.add(new Point3D(25, 10, 20));
        route.setLineSegments(lineSegments);

        return route;
    }


}
