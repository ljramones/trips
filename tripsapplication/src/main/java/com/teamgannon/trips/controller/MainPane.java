package com.teamgannon.trips.controller;

import com.teamgannon.trips.config.application.ApplicationPreferences;
import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.controller.support.DataSetDescriptorCellFactory;
import com.teamgannon.trips.controls.RoutingPanel;
import com.teamgannon.trips.dialogs.DataSetManagerDialog;
import com.teamgannon.trips.dialogs.QueryDialog;
import com.teamgannon.trips.dialogs.ViewPreferencesDialog;
import com.teamgannon.trips.dialogs.preferencespanes.PreferencesUpdater;
import com.teamgannon.trips.file.chview.ChviewReader;
import com.teamgannon.trips.file.chview.model.ChViewFile;
import com.teamgannon.trips.file.csvin.RBCsvReader;
import com.teamgannon.trips.file.excel.ExcelReader;
import com.teamgannon.trips.graphics.AstrographicPlotter;
import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.graphics.operators.*;
import com.teamgannon.trips.graphics.panes.InterstellarSpacePane;
import com.teamgannon.trips.graphics.panes.SolarSystemSpacePane;
import com.teamgannon.trips.jpa.model.AstrographicObject;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.jpa.model.GraphEnablesPersist;
import com.teamgannon.trips.jpa.repository.AstrographicObjectRepository;
import com.teamgannon.trips.search.AstroSearchQuery;
import com.teamgannon.trips.search.SearchContext;
import com.teamgannon.trips.search.StellarDataUpdater;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.Simulator;
import com.teamgannon.trips.starmodel.DistanceReport;
import com.teamgannon.trips.starmodel.DistanceToFrom;
import com.teamgannon.trips.starmodel.StarBase;
import com.teamgannon.trips.support.AlertFactory;
import com.teamgannon.trips.tableviews.DataSetTable;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;
import java.util.stream.Collectors;

import static com.teamgannon.trips.support.AlertFactory.*;

@Slf4j
@Component
@FxmlView("MainPane.fxml")
public class MainPane implements
        ListUpdater,
        StellarPropertiesDisplayer,
        StellarDataUpdater,
        PreferencesUpdater,
        ContextSelector,
        RouteUpdater,
        RedrawListener,
        ReportGenerator {

    /**
     * the ListView UI control for displaying lists - the V for the MVC of Listview
     */
    @FXML
    public MenuBar menuBar;
    @FXML
    public ToolBar toolBar;
    @FXML
    public HBox statusBar;
    @FXML
    public SplitPane mainSplitPane;
    @FXML
    public ToggleButton toggleSettings;
    @FXML
    public Label databaseStatus;
    @FXML
    public StackPane leftDisplayPane;
    @FXML
    public VBox settingsPane;
    @FXML
    public Accordion propertiesAccordion;
    @FXML
    public Pane mainPanel;
    @FXML
    public TitledPane datasetsPane;

    @FXML
    public TitledPane objectsViewPane;
    @FXML
    public TitledPane stellarObjectPane;

    @FXML
    public TitledPane routingPane;
    @FXML
    public GridPane propertiesPane;

    /**
     * used to weave the java fx code with spring boot
     */
    private final FxWeaver fxWeaver;
    /**
     * database management spring component service
     */
    private final DatabaseManagementService databaseManagementService;
    /**
     * the TRIPS application context
     */
    private final ApplicationContext appContext;
    /**
     * the CHView file reader component
     */
    private final ChviewReader chviewReader;
    /**
     * the excel file reader component
     */
    private final ExcelReader excelReader;
    /**
     * the RB csv reader
     */
    private final RBCsvReader rbCsvReader;
    /**
     * star plotter component
     */
    private final AstrographicPlotter astrographicPlotter;
    /**
     * in memory star base component
     */
    private final StarBase starBase;
    /**
     * the TRIPS context component
     */
    private final TripsContext tripsContext;

    /**
     * the current search context to display from
     */
    private final SearchContext searchContext;

    /**
     * list of routes
     */
    private final List<RouteDescriptor> routeList = new ArrayList<>();

    /**
     * backing array for listable stellar objects - the M for the MVC of Listview
     */
    private final List<Map<String, String>> objectsInView = new ArrayList<>();

    /**
     * observable list that provides the C for the MVC of the ListView
     */
    private final ObservableList<Map<String, String>> stellarObjectList = FXCollections.observableArrayList(objectsInView);

    /**
     * the ListView UI control for displaying lists - the V for the MVC of Listview
     */
    private final ListView<Map<String, String>> stellarObjectsListView = new ListView<>(stellarObjectList);

    /**
     * dataset lists
     */
    private final ListView<DataSetDescriptor> dataSetsListView = new ListView<>();

    public ToggleButton toggleStarBtn;
    public ToggleButton toggleGridBtn;
    public ToggleButton toggleStemBtn;
    public ToggleButton toggleScaleBtn;
    public ToggleButton toggleZoomInBtn;
    public ToggleButton toggleZoomOutBtn;
    public ToggleButton toggleLabelsBtn;

    ////////////////////////////////

    /**
     * temporary data for chview data testing
     */
    private ChViewFile chViewFile;

    /**
     * solar system panes for showing the details of various solar systems
     */
    private SolarSystemSpacePane solarSystemSpacePane;
    /**
     * graphics pane to draw stars across interstellar space
     */
    private InterstellarSpacePane interstellarSpacePane;
    /**
     * the simulator
     */
    private Simulator simulator;

    // state settings for control positions
    private boolean gridOn = true;
    private boolean extensionsOn = true;
    private boolean labelsOn = true;
    private boolean starsOn = true;

    /////// data objects ///////////
    private boolean scaleOn = true;
    private boolean routesOn = true;
    private final int width;

    ///////////////////////////////////////
    private final int height;
    private final int depth;
    private final int spacing;

    ///////////////////////////////////////////////////////

    public MainPane(FxWeaver fxWeaver,
                    HostServices hostServices,
                    DatabaseManagementService databaseManagementService,
                    ApplicationContext appContext,
                    ChviewReader chviewReader,
                    ExcelReader excelReader,
                    RBCsvReader rbCsvReader,
                    AstrographicPlotter astrographicPlotter,
                    StarBase starBase,
                    TripsContext tripsContext) {

        this.fxWeaver = fxWeaver;
        this.databaseManagementService = databaseManagementService;
        this.appContext = appContext;
        this.chviewReader = chviewReader;
        this.excelReader = excelReader;
        this.rbCsvReader = rbCsvReader;
        this.astrographicPlotter = astrographicPlotter;
        this.starBase = starBase;
        this.tripsContext = tripsContext;
        this.searchContext = tripsContext.getSearchContext();

        this.width = 1100;
        this.height = 700;
        this.depth = 700;
        this.spacing = 20;

    }

    @FXML
    public void initialize() {
        log.info("initialize view");

        setSliderControl();
        setStatusPanel();

        // left display
        createLeftDisplay();

        // right display
        createRightDisplay();

        // create the list of objects in view
        setupStellarObjectListView();

        // create a data set pane for the database files present
        setupDataSetView();

        // load database preset values
        loadDBPresets();

        // by deafult side panel should be off
        toggleSidePane(false);
    }

    private void loadDBPresets() {

        // get colors from DB
        getGraphColorsFromDB();

        // get graph enables from DB
        getGraphEnablesFromDB();

        // get Star definitions from DB
        getStarDefinitionsFromDB();

    }

    private void getGraphColorsFromDB() {
        ColorPalette colorPalette = databaseManagementService.getGraphColorsFromDB();
        tripsContext.getAppViewPreferences().setColorPallete(colorPalette);
    }


    public void getGraphEnablesFromDB() {
        GraphEnablesPersist graphEnablesPersist = databaseManagementService.getGraphEnablesFromDB();
        tripsContext.getAppViewPreferences().setGraphEnablesPersist(graphEnablesPersist);

        updateToggles(graphEnablesPersist);
        gridOn = graphEnablesPersist.isDisplayGrid();
        extensionsOn = graphEnablesPersist.isDisplayStems();
        labelsOn = graphEnablesPersist.isDisplayLabels();
        scaleOn = graphEnablesPersist.isDisplayLegend();
    }

    private void getStarDefinitionsFromDB() {

    }


    private void updateToggles(GraphEnablesPersist graphEnablesPersist) {
        if (graphEnablesPersist.isDisplayGrid()) {
            interstellarSpacePane.toggleGrid(graphEnablesPersist.isDisplayGrid());
            toggleGridBtn.setSelected(graphEnablesPersist.isDisplayGrid());
        }

        if (graphEnablesPersist.isDisplayLabels()) {
            interstellarSpacePane.toggleLabels(graphEnablesPersist.isDisplayLabels());
            toggleLabelsBtn.setSelected(graphEnablesPersist.isDisplayLabels());
        }

        if (graphEnablesPersist.isDisplayStems()) {
            interstellarSpacePane.toggleExtensions(graphEnablesPersist.isDisplayStems());
            toggleStemBtn.setSelected(graphEnablesPersist.isDisplayStems());
        }

        if (graphEnablesPersist.isDisplayLegend()) {
            interstellarSpacePane.toggleScale(graphEnablesPersist.isDisplayLegend());
            toggleScaleBtn.setSelected(graphEnablesPersist.isDisplayLegend());
        }
    }

    private void setupDataSetView() {

        SearchContext searchContext = tripsContext.getSearchContext();
        datasetsPane.setContent(dataSetsListView);

        dataSetsListView.setPrefHeight(10);
        dataSetsListView.setCellFactory(new DataSetDescriptorCellFactory(this));
        dataSetsListView.getSelectionModel().selectedItemProperty().addListener(this::datasetDescriptorChanged);

        // load viable datasets into search context
        List<DataSetDescriptor> dataSets = loadDataSetView();
        if (dataSets.size() > 0) {
            searchContext.addDataSets(dataSets);
        }
        log.info("Application up and running");
    }

    public void addDataSetToList(List<DataSetDescriptor> list, boolean clear) {
        if (clear) {
            dataSetsListView.getItems().clear();
        }
        list.forEach(descriptor -> dataSetsListView.getItems().add(descriptor));
        log.debug("update complete");
    }

    public void addDataSetToList(DataSetDescriptor descriptor) {
        dataSetsListView.getItems().add(descriptor);
    }

    public void clearDataSetListView() {
        dataSetsListView.getItems().clear();
    }


    public void datasetDescriptorChanged(ObservableValue<? extends DataSetDescriptor> ov, DataSetDescriptor oldValue, DataSetDescriptor newValue) {
        String oldText = oldValue == null ? "null" : oldValue.toString();
        String newText = newValue == null ? "null" : newValue.toString();

        log.info("Change: old = " + oldText + ", new = " + newText + "\n");
    }

    /**
     * select the dataset in use
     *
     * @param index the index to select
     * @todo connect this code
     */
    private void selectDataSet(int index) {
        Platform.runLater(() -> {
            dataSetsListView.scrollTo(index);
            dataSetsListView.getFocusModel().focus(index);
            dataSetsListView.getSelectionModel().select(index);
        });

    }

    private List<DataSetDescriptor> loadDataSetView() {

        List<DataSetDescriptor> dataSetDescriptorList = databaseManagementService.getDataSetIds();
        addDataSetToList(dataSetDescriptorList, true);
        log.info("loaded DBs");
        return dataSetDescriptorList;
    }

    /**
     * create the left part of the display
     */
    private void createLeftDisplay() {

        // create the solar system
        createSolarSystemSpace();

        // create the interstellar space
        createInterstellarSpace(tripsContext.getAppViewPreferences().getColorPallete());
    }

    /**
     * create a interstellar space drawing area
     *
     * @param colorPalette the colors to use in drawing
     */
    private void createInterstellarSpace(ColorPalette colorPalette) {
        // create main graphics display pane
        interstellarSpacePane = new InterstellarSpacePane(1080, 680, depth, spacing, colorPalette);
        leftDisplayPane.getChildren().add(interstellarSpacePane);

        // put the interstellar space on top and the solar system to the back
        interstellarSpacePane.toFront();

        // set the interstellar pane to be the drawing surface
        astrographicPlotter.setInterstellarPane(interstellarSpacePane);

        // setup simulator
        simulator = new Simulator(interstellarSpacePane, width, height, depth, colorPalette);

        // setup event listeners
        interstellarSpacePane.setListUpdater(this);
        interstellarSpacePane.setContextUpdater(this);
        interstellarSpacePane.setStellarObjectDisplayer(this);
        interstellarSpacePane.setRouteUpdater(this);
        interstellarSpacePane.setRedrawListener(this);
        interstellarSpacePane.setReportGenerator(this);
    }

    /**
     * create the solar space drawing area
     */
    private void createSolarSystemSpace() {
        solarSystemSpacePane = new SolarSystemSpacePane(leftDisplayPane.getMaxWidth(), leftDisplayPane.getMaxHeight());
        solarSystemSpacePane.setContextUpdater(this);
        leftDisplayPane.getChildren().add(solarSystemSpacePane);
    }

    /**
     * create the right portion of the display
     */
    private void createRightDisplay() {
        createRoutingPane();
    }

    private void createRoutingPane() {
        routingPane.setContent(new RoutingPanel());
    }

    /**
     * setup the status panel
     */
    private void setStatusPanel() {
        statusBar.setAlignment(Pos.CENTER);
        statusBar.setSpacing(5.0);
        Insets insets1 = new Insets(3.0, 3.0, 3.0, 3.0);
        statusBar.setPadding(insets1);
    }

    /**
     * set the slider controls
     */
    private void setSliderControl() {
        DoubleProperty splitPaneDividerPosition = mainSplitPane.getDividers().get(0).positionProperty();
        splitPaneDividerPosition.addListener((obs, oldPos, newPos)
                -> toggleSettings.setSelected(newPos.doubleValue() < 0.95));
    }

    /**
     * get the parent window for this application
     *
     * @return the primary primaryStage
     */
    private Stage getStage() {
        Scene scene = mainPanel.getScene();
        Window window = scene.getWindow();
        return (Stage) window;
    }

    //////////  menu events

    public void runQuery(ActionEvent actionEvent) {
        QueryDialog queryDialog = new QueryDialog(searchContext, this);
        queryDialog.initModality(Modality.NONE);
        queryDialog.show();
    }

    public void simulate(ActionEvent actionEvent) {
        simulator.simulate();
    }

    public void plotStars(ActionEvent actionEvent) {
        showPlot();
    }

    public void plotRoutes(ActionEvent actionEvent) {
        interstellarSpacePane.plotRoutes(routeList);
    }

    public void clearStars(ActionEvent actionEvent) {
        interstellarSpacePane.clearStars();
    }

    public void clearRoutes(ActionEvent actionEvent) {
        interstellarSpacePane.clearRoutes();
    }

    public void quit(ActionEvent actionEvent) {
        shutdown();
    }

    public void toggleGrid(ActionEvent actionEvent) {
        gridOn = !gridOn;
        tripsContext.getAppViewPreferences().getGraphEnablesPersist().setDisplayGrid(gridOn);
        interstellarSpacePane.toggleGrid(gridOn);
    }

    public void toggleGridExtensions(ActionEvent actionEvent) {
        extensionsOn = !extensionsOn;
        tripsContext.getAppViewPreferences().getGraphEnablesPersist().setDisplayStems(extensionsOn);
        interstellarSpacePane.toggleExtensions(extensionsOn);
    }


    public void toggleLabels(ActionEvent actionEvent) {
        labelsOn = !labelsOn;
        tripsContext.getAppViewPreferences().getGraphEnablesPersist().setDisplayLabels(labelsOn);
        interstellarSpacePane.toggleLabels(labelsOn);
    }

    public void toggleStars(ActionEvent actionEvent) {
        starsOn = !starsOn;
        interstellarSpacePane.toggleStars(starsOn);
    }

    public void toggleScale(ActionEvent actionEvent) {
        scaleOn = !scaleOn;
        tripsContext.getAppViewPreferences().getGraphEnablesPersist().setDisplayLegend(scaleOn);
        interstellarSpacePane.toggleScale(scaleOn);
    }

    public void toggleRoutes(ActionEvent actionEvent) {
        routesOn = !routesOn;
        interstellarSpacePane.toggleRoutes(routesOn);
    }

    public void toggleSidePane(ActionEvent actionEvent) {
        toggleSidePane(toggleSettings.isSelected());
    }


    public void toggleSidePane(boolean sidePanelOn) {
        if (sidePanelOn) {
            mainSplitPane.setDividerPositions(0.76);
        } else {
            mainSplitPane.setDividerPositions(1.0);
        }
    }

    public void toggleToolbar(ActionEvent actionEvent) {
        toolBar.setVisible(!toolBar.isVisible());
    }

    public void toggleStatusBar(ActionEvent actionEvent) {
        statusBar.setVisible(!statusBar.isVisible());
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * show view preferences
     *
     * @param actionEvent the event that triggere this - nor used
     */
    public void showApplicationPreferences(ActionEvent actionEvent) {
        ApplicationPreferences applicationPreferences = tripsContext.getAppPreferences();
        ViewPreferencesDialog viewPreferencesDialog = new ViewPreferencesDialog(this, tripsContext, applicationPreferences);
        viewPreferencesDialog.showAndWait();
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////


    ////////// importer and exporters  //////////////////

    public void viewEditStarData(ActionEvent actionEvent) {
        showTableData();
    }

    ///////////// Reports /////////

    public void distanceReport(ActionEvent actionEvent) {
        showWarningMessage("Work in progress", "not supported yet ");
    }

    public void routeFinder(ActionEvent actionEvent) {
        showWarningMessage("Work in progress", "not supported yet ");
    }

    /////////  About /////////////

    public void aboutTrips(ActionEvent actionEvent) {
        showWarningMessage("info", "aboutTrips");
    }

    public void howToSupport(ActionEvent actionEvent) {
        showWarningMessage("info", "howToSupport");
    }

    public void checkUpdate(ActionEvent actionEvent) {
        showWarningMessage("info", "checkUpdate");
    }

    /////////////// Toolbar events

    /**
     * zoom in on the plot by a standard incidence amount
     *
     * @param actionEvent the specific action event
     */
    public void zoomIn(ActionEvent actionEvent) {
        interstellarSpacePane.zoomIn();
    }

    /**
     * zoom out on the plot by a standard amount
     *
     * @param actionEvent the specific action event
     */
    public void zoomOut(ActionEvent actionEvent) {
        interstellarSpacePane.zoomOut();
    }


    /////////////////////////////////////////////////////////////////////////////////

    /**
     * redisplay data based on the selected filter criteria
     *
     * @param searchQuery the search query to use
     * @param showPlot    show a graphical plot
     * @param showTable   show a table
     */
    @Override
    public void showNewStellarData(AstroSearchQuery searchQuery, boolean showPlot, boolean showTable) {
        log.info(searchQuery.toString());
        searchContext.setAstroSearchQuery(searchQuery);

        // do a search and cause the plot to show it
        List<AstrographicObject> astrographicObjects = getAstrographicObjectsOnQuery();

        if (!astrographicObjects.isEmpty()) {
            if (showPlot) {
                astrographicPlotter.drawAstrographicData(astrographicObjects, searchQuery.getCenterCoordinates(), tripsContext.getAppViewPreferences().getColorPallete());
            }
            if (showTable) {
                showList(astrographicObjects);
            }
            showStatus("Dataset loaded is: " + searchQuery.getDataSetName());

            // highlight the data set used
            selectDataSet(1);

        } else {
            showErrorAlert("Astrographic data view error", "No Astrographic data was loaded ");
        }
    }

    /**
     * display plot or data based on default query
     *
     * @param showPlot  show the graphical plot
     * @param showTable show the table
     * @todo implement this
     */
    @Override
    public void showNewStellarData(boolean showPlot, boolean showTable) {
        AstroSearchQuery searchQuery = tripsContext.getSearchContext().getAstroSearchQuery();

        // do a search and cause the plot to show it
        List<AstrographicObject> astrographicObjects = getAstrographicObjectsOnQuery();

        if (!astrographicObjects.isEmpty()) {
            if (showPlot) {
                astrographicPlotter.drawAstrographicData(astrographicObjects, searchQuery.getCenterCoordinates(), tripsContext.getAppViewPreferences().getColorPallete());
            }
            if (showTable) {
                showList(astrographicObjects);
            }
            showStatus("Dataset loaded is: " + searchQuery.getDataSetName());

            // highlight the data set used
            selectDataSet(1);

        } else {
            showErrorAlert("Astrographic data view error", "No Astrographic data was loaded ");
        }
    }

    @Override
    public void addDataSet(DataSetDescriptor dataSetDescriptor) {
        searchContext.addDataSet(dataSetDescriptor);
        addDataSetToList(new ArrayList<>(searchContext.getDatasetMap().values()), true);
    }

    @Override
    public void removeDataSet(DataSetDescriptor dataSetDescriptor) {
        Optional<ButtonType> buttonType = showConfirmationAlert("Remove Dataset",
                "Remove",
                "Are you sure you want to remove: " + dataSetDescriptor.getDataSetName());

        if ((buttonType.isPresent()) && (buttonType.get() == ButtonType.OK)) {
            searchContext.removeDataSet(dataSetDescriptor);

            //
            databaseManagementService.removeDataSet(dataSetDescriptor);

            // redisplay the datasets
            addDataSetToList(new ArrayList<>(searchContext.getDatasetMap().values()), true);
        }
    }

    private void showList(List<AstrographicObject> astrographicObjects) {
        new DataSetTable(this, astrographicObjects);
    }

    @Override
    public void addStar(AstrographicObject astrographicObject) {
        databaseManagementService.addStar(astrographicObject);
    }

    @Override
    public void updateStar(AstrographicObject astrographicObject) {
        databaseManagementService.updateStar(astrographicObject);
    }

    @Override
    public void removeStar(AstrographicObject astrographicObject) {
        databaseManagementService.removeStar(astrographicObject);
    }

    @Override
    public List<AstrographicObject> getAstrographicObjectsOnQuery() {
        return databaseManagementService.getAstrographicObjectsOnQuery((searchContext));
    }

    //////////////

    /**
     * show a loaded dataset in the plot menu
     */
    private void showPlot() {
        List<DataSetDescriptor> datasets = databaseManagementService.getDataSetIds();
        if (datasets.size() == 0) {
            showErrorAlert("Plot Stars", "No datasets loaded, please load one");
            return;
        }

        List<String> dialogData = datasets.stream().map(DataSetDescriptor::getDataSetName)
                .collect(Collectors.toList());

        ChoiceDialog<String> dialog = new ChoiceDialog<>(dialogData.get(0), dialogData);
        dialog.setTitle("Choice Data set to display");
        dialog.setHeaderText("Select your choice - (Default display is 15 light years from Earth, use Show Stars filter to change)");

        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            String selected = result.get();

            DataSetDescriptor dataSetDescriptor = findFromDataSet(selected, datasets);
            if (dataSetDescriptor == null) {
                log.error("How the hell did this happen");
                return;
            }

            List<AstrographicObject> astrographicObjects = databaseManagementService.getFromDatasetWithinLimit(
                    dataSetDescriptor,
                    searchContext.getAstroSearchQuery().getDistanceFromCenterStar());
            log.info("DB Query returns {} stars", astrographicObjects.size());

            if (!astrographicObjects.isEmpty()) {
                AstroSearchQuery astroSearchQuery = searchContext.getAstroSearchQuery();
                astroSearchQuery.zeroCenter();
                astrographicPlotter.drawAstrographicData(
                        astrographicObjects,
                        astroSearchQuery.getCenterCoordinates(), tripsContext.getAppViewPreferences().getColorPallete());
                String data = String.format("%s records plotted from dataset %s.",
                        dataSetDescriptor.getAstrographicDataList().size(),
                        dataSetDescriptor.getDataSetName());
                showInfoMessage("Load Astrographic Format", data);
                showStatus("Dataset loaded is: " + dataSetDescriptor.getDataSetName());
            } else {
                showErrorAlert("Astronautic data view error", "No Astronautic data was loaded ");
            }
        }
    }

    /**
     * show the data in a spreadsheet
     */
    private void showTableData() {
        List<DataSetDescriptor> datasets = databaseManagementService.getDataSetIds();
        if (datasets.size() == 0) {
            showErrorAlert("Plot Stars", "No datasets loaded, please load one");
            return;
        }

        List<String> dialogData = datasets.stream().map(DataSetDescriptor::getDataSetName)
                .collect(Collectors.toList());

        ChoiceDialog<String> dialog = new ChoiceDialog<>(dialogData.get(0), dialogData);
        dialog.setTitle("Choice Data set to display");
        dialog.setHeaderText("Select your choice - (Default display is 15 light years from Earth, use Show Stars filter to change)");

        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            String selected = result.get();

            DataSetDescriptor dataSetDescriptor = findFromDataSet(selected, datasets);
            if (dataSetDescriptor == null) {
                log.error("How the hell did this happen");
                return;
            }
            List<AstrographicObject> astrographicObjects = getAstrographicObjectsOnQuery();
            new DataSetTable(this, astrographicObjects);
            showStatus("Dataset loaded is: " + dataSetDescriptor.getDataSetName());
        }
    }

    /**
     * find the data selected
     *
     * @param selected the selected data
     * @param datasets the datasets
     * @return the descriptor wanted
     */
    private DataSetDescriptor findFromDataSet(String selected, List<DataSetDescriptor> datasets) {
        return datasets.stream().filter(dataSetDescriptor -> dataSetDescriptor.getDataSetName().equals(selected)).findFirst().orElse(null);
    }

    //////////////////  Interface realization methods

    /**
     * show the interstellar space (bring to the front)
     *
     * @param objectProperties the properties of the selected object
     */
    @Override
    public void selectInterstellarSpace(Map<String, String> objectProperties) {
        log.info("Showing interstellar Space");
        interstellarSpacePane.toFront();
    }

    /**
     * select the solar space
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

    @Override
    public void updateList(Map<String, String> listItem) {
        stellarObjectList.add(listItem);
        log.info(listItem.get("name"));
    }

    @Override
    public void clearList() {
        stellarObjectList.clear();
    }

    @Override
    public void recenter(StarDisplayRecord starId) {
        log.info("recenter plot at {}", starId);
        AstroSearchQuery query = searchContext.getAstroSearchQuery();
        query.setCenterRanging(starId, query.getDistanceFromCenterStar());
        log.info("New Center Range: {}", query.getCenterRangingCube());
        showNewStellarData(query, true, false);
    }

    @Override
    public void generateDistanceReport(StarDisplayRecord starDescriptor) {
        log.info("generate the distance report");
        storeFile(starDescriptor);
        log.info("report complete");
    }


    private void storeFile(StarDisplayRecord starDisplayRecord) {
        log.debug("Store the report format file");
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enter Report file to save");
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("TXT Files", "txt");
        fileChooser.setSelectedExtensionFilter(filter);
        File file = fileChooser.showSaveDialog(getStage());
        if (file != null) {
            // load chview file
            try {
                DistanceReport report = starBase.getDistanceReport(starDisplayRecord);
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                List<DistanceToFrom> distanceToFromList = report.getDistanceList();
                for (DistanceToFrom distanceToFrom : distanceToFromList) {
                    writer.write(distanceToFrom.toString());
                }
                writer.flush();
                writer.close();
            } catch (Exception e) {
                showErrorAlert("Report Generation Error", "Failed to save distance report");
            }
        } else {
            log.warn("file storage cancelled");
        }
    }

    @Override
    public void newRoute(RouteDescriptor routeDescriptor) {
        log.info("new route");

        // store in database @todo
    }

    @Override
    public void updateRoute(RouteDescriptor routeDescriptor) {
        log.info("update route");

        // update in database @todo
    }

    @Override
    public void deleteRoute(RouteDescriptor routeDescriptor) {
        log.info("delete route");

        // delete from database @todo
    }

    ////////////////////////////////

    @Override
    public void displayStellarProperties(Map<String, String> stellarObjectSelected) {
        displayProperties(stellarObjectSelected);
    }

    /**
     * Displays the properties of the astronomical object of name given as a parameter.
     *
     * @param properties Astronomical object unique name.
     */
    private void displayProperties(Map<String, String> properties) {
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

        // setup model to display in case we turn on
        objectsViewPane.setContent(stellarObjectsListView);

        stellarObjectsListView.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue<? extends Map<String, String>> ov, Map<String, String> old_val, Map<String, String> newSelection) -> {
                    displayProperties(newSelection);
                    log.info("Object Selected is:" + newSelection);
                });

        stellarObjectsListView.setCellFactory(new Callback<>() {

            @Override
            public ListCell<Map<String, String>> call(ListView<Map<String, String>> p) {
                objectsViewPane.setDisable(false);
                return new ListCell<>() {
                    @Override
                    protected void updateItem(Map<String, String> objectProperties, boolean bln) {
                        final Tooltip tooltip = new Tooltip();

                        super.updateItem(objectProperties, bln);
                        if (objectProperties != null) {
                            tooltip.setText("Please select a star to show it's properties");
                            setTooltip(tooltip);
                            setText(objectProperties.get("name"));
                        }
                    }
                };
            }
        });
    }

    /////////////////////////  Shutdown   /////////////////////////

    private void shutdown() {
        log.debug("Exit selection");
        Optional<ButtonType> result = AlertFactory.showConfirmationAlert("Exit Application", "Exit Application?", "Are you sure you want to leave?");

        if ((result.isPresent()) && (result.get() == ButtonType.OK)) {
            initiateShutdown(0);
        }
    }

    /**
     * shutdown the application
     *
     * @param returnCode should be a return code of zero meaning success
     */
    private void initiateShutdown(int returnCode) {
        // close the spring context which invokes all the bean destroy methods
        SpringApplication.exit(appContext, () -> returnCode);
        // now exit the application
        System.exit(returnCode);
    }


    /////////////////////////  user feedback  //////////////////////////////

    private void showStatus(String message) {
        databaseStatus.setText(message);
    }

    public void loadDataSetManager(ActionEvent actionEvent) {

        DataSetManagerDialog dialog = new DataSetManagerDialog(
                this,
                databaseManagementService,
                chviewReader,
                excelReader,
                rbCsvReader);

        // we throw away the result after returning
        dialog.showAndWait();
    }

    ///////////////////////////////////////////////
    @Override
    public void updateGraphColors(ColorPalette colorPalette) {

        tripsContext.getAppViewPreferences().setColorPallete(colorPalette);

        // colors changes so update db
        databaseManagementService.updateColors(colorPalette);
        astrographicPlotter.changeColors(colorPalette);

        log.info("UPDATE COLORS!!!");
    }


    @Override
    public void changesGraphEnables(GraphEnablesPersist graphEnablesPersist) {
        tripsContext.getAppViewPreferences().setGraphEnablesPersist(graphEnablesPersist);

        updateToggles(graphEnablesPersist);

        databaseManagementService.updateGraphEnables(graphEnablesPersist);
        astrographicPlotter.changeGraphEnables(graphEnablesPersist);
        log.info("UPDATE GRAPH ENABLES!!!");
    }

    //////////////////

}
