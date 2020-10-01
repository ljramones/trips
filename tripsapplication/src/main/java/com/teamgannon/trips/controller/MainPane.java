package com.teamgannon.trips.controller;

import com.teamgannon.trips.algorithms.Universe;
import com.teamgannon.trips.config.application.ApplicationPreferences;
import com.teamgannon.trips.config.application.Localization;
import com.teamgannon.trips.config.application.StarDisplayPreferences;
import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.dataset.model.DataSetDescriptorCellFactory;
import com.teamgannon.trips.dialogs.AboutDialog;
import com.teamgannon.trips.dialogs.dataset.DataSetManagerDialog;
import com.teamgannon.trips.dialogs.preferences.ViewPreferencesDialog;
import com.teamgannon.trips.dialogs.query.QueryDialog;
import com.teamgannon.trips.dialogs.search.FindStarInViewDialog;
import com.teamgannon.trips.dialogs.search.FindTransitsBetweenStarsDialog;
import com.teamgannon.trips.dialogs.search.model.DistanceRoutes;
import com.teamgannon.trips.dialogs.search.model.FindResults;
import com.teamgannon.trips.graphics.AstrographicPlotter;
import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.graphics.panes.InterstellarSpacePane;
import com.teamgannon.trips.graphics.panes.SolarSystemSpacePane;
import com.teamgannon.trips.jpa.model.*;
import com.teamgannon.trips.listener.*;
import com.teamgannon.trips.report.distance.DistanceReport;
import com.teamgannon.trips.report.distance.DistanceReportDialog;
import com.teamgannon.trips.report.distance.DistanceReportSelection;
import com.teamgannon.trips.report.distance.SelectStarForDistanceReportDialog;
import com.teamgannon.trips.routing.Route;
import com.teamgannon.trips.routing.RouteFinder;
import com.teamgannon.trips.routing.RoutingPanel;
import com.teamgannon.trips.screenobjects.ObjectViewPane;
import com.teamgannon.trips.screenobjects.StarPropertiesPane;
import com.teamgannon.trips.search.AstroSearchQuery;
import com.teamgannon.trips.search.SearchContext;
import com.teamgannon.trips.service.DataExportService;
import com.teamgannon.trips.service.DataImportService;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.SimulatorService;
import com.teamgannon.trips.support.AlertFactory;
import com.teamgannon.trips.tableviews.DataSetTable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
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
        ListUpdaterListener,
        StellarPropertiesDisplayerListener,
        StellarDataUpdaterListener,
        ListSelectorActionsListener,
        PreferencesUpdaterListener,
        ContextSelectorListener,
        RouteUpdaterListener,
        RedrawListener,
        ReportGenerator,
        DatabaseListener,
        DataSetChangeListener,
        StatusUpdaterListener {

    private FxWeaver fxWeaver;
    /**
     * database management spring component service
     */
    private final DatabaseManagementService databaseManagementService;

    /**
     * the TRIPS application context
     */
    private final ApplicationContext appContext;


    /**
     * star plotter component
     */
    private final AstrographicPlotter astrographicPlotter;

    /**
     * the TRIPS context component
     */
    private final TripsContext tripsContext;

    /**
     * the current search context to display from
     */
    private final SearchContext searchContext;

    /**
     * the toggle button
     */
    public ToggleButton toggleRoutesBtn;

    public CheckMenuItem togglePolitiesMenuitem;

    public ToggleButton togglePolityBtn;

    public CheckMenuItem toggleTransitsMenuitem;
    public ToggleButton toggleTransitsBtn;
    public Label routingStatus;

    public CheckMenuItem toggleTransitLengthsMenuitem;
    public AnchorPane anchorPane1;
    public AnchorPane anchorPane2;
    public VBox vbox1;

    /**
     * the query dialog
     */
    private QueryDialog queryDialog;

    /**
     * the localization of the application
     */
    private final Localization localization;
    private final DataImportService dataImportService;
    private final DataExportService dataExportService;

    /**
     * list of routes
     */
    private List<Route> routeList;

    /**
     * dataset lists
     */
    private final ListView<DataSetDescriptor> dataSetsListView = new ListView<>();


    ///////////////////////////////////////

    private final double width;
    private final double height;
    private final double depth;
    private final double spacing;


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

    public CheckMenuItem toggleSidePaneMenuitem;
    public CheckMenuItem toggleRoutesMenuitem;
    public CheckMenuItem toggleGridMenuitem;
    public CheckMenuItem toggleLabelsMenuitem;
    public CheckMenuItem toggleExtensionsMenuitem;
    public CheckMenuItem toggleStarMenuitem;
    public CheckMenuItem toggleScaleMenuitem;
    public CheckMenuItem toggleToolBarMenuitem;
    public CheckMenuItem toggleStatusBarMenuitem;
    public CheckBox animationCheckbox;

    private ObjectViewPane objectViewPane;

    @FXML
    public TitledPane stellarObjectPane;

    public StarPropertiesPane starPropertiesPane;

    @FXML
    public TitledPane routingPane;

    private RoutingPanel routingPanel;

    public ToggleButton toggleStarBtn;
    public ToggleButton toggleGridBtn;
    public ToggleButton toggleStemBtn;

    ////////////////////////////////
    public ToggleButton toggleScaleBtn;
    public ToggleButton toggleZoomInBtn;
    public ToggleButton toggleZoomOutBtn;
    public ToggleButton toggleLabelsBtn;

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
    private SimulatorService simulatorService;

    // state settings for control positions
    private boolean polities = true;
    private boolean gridOn = true;
    private boolean extensionsOn = true;
    private boolean labelsOn = true;
    private boolean starsOn = true;
    private boolean scaleOn = true;
    private boolean routesOn = true;
    private boolean transitsOn = true;
    private boolean transitsLengthsOn = true;

    /////// data objects ///////////
    private boolean sidePaneOn = false;
    private boolean toolBarOn = true;
    private boolean statusBarOn = true;

    private Stage stage;

    ///////////////////////////////////////////////////////

    public MainPane(FxWeaver fxWeaver,
                    DatabaseManagementService databaseManagementService,
                    ApplicationContext appContext,
                    AstrographicPlotter astrographicPlotter,
                    TripsContext tripsContext,
                    Localization localization) {

        this.fxWeaver = fxWeaver;
        this.databaseManagementService = databaseManagementService;
        this.appContext = appContext;
        this.astrographicPlotter = astrographicPlotter;
        this.tripsContext = tripsContext;
        this.searchContext = tripsContext.getSearchContext();
        this.localization = localization;
        this.dataImportService = new DataImportService(
                databaseManagementService, this, this);
        this.dataExportService = new DataExportService(
                databaseManagementService, this);

        this.width = 1100;
        this.height = 700;
        this.depth = 700;
        this.spacing = 20;

    }


    public void setStage(Stage stage) {
        this.stage = stage;

        ChangeListener<Number> stageSizeListener = (observable, oldValue, newValue) -> {
            resizeTrips(stage.getHeight(), stage.getWidth());
        };

        stage.widthProperty().addListener(stageSizeListener);
        stage.heightProperty().addListener(stageSizeListener);
    }

    private void resizeTrips(double height, double width) {
        log.info("Height: " + height + " Width: " + width);
        interstellarSpacePane.resize(width, height);
        this.vbox1.setPrefWidth(width);
        this.menuBar.setPrefWidth(width);
        this.toolBar.setPrefWidth(width);
        this.statusBar.setPrefWidth(width);
        this.mainSplitPane.setPrefWidth(width);
        this.mainSplitPane.setPrefHeight(height - 112);
        this.settingsPane.setPrefHeight(height - 112);
        this.settingsPane.setPrefWidth(260);
        this.leftDisplayPane.setPrefWidth(width);
        this.leftDisplayPane.setPrefHeight(height);
    }

    @FXML
    public void initialize() {
        log.info("initialize view");

        this.mainPanel.setPrefHeight(Universe.boxHeight + 20);
        this.mainPanel.setPrefWidth(Universe.boxWidth + 20);

        this.menuBar.setPrefHeight(29.0);
        this.menuBar.setPrefWidth(Universe.boxWidth + 20);

        this.statusBar.setPrefHeight(39.0);
        this.statusBar.setPrefWidth(Universe.boxWidth + 20);

        this.mainSplitPane.setPrefHeight(588.0);
        this.mainSplitPane.setPrefWidth(Universe.boxWidth);

        this.anchorPane1.setMinHeight(0.0);
        this.anchorPane1.setMinWidth(0.0);
        this.anchorPane1.setPrefHeight(554.0);
        this.anchorPane1.setPrefWidth(785.0);

        this.leftDisplayPane.setMinHeight(Universe.boxHeight);
        this.leftDisplayPane.setMinWidth(Universe.boxWidth+100);
        this.leftDisplayPane.setPrefHeight(Universe.boxHeight);
        this.leftDisplayPane.setPrefWidth(Universe.boxWidth);

        this.settingsPane.setPrefHeight(588.0);
        this.settingsPane.setPrefWidth(260.0);

        this.anchorPane2.setMinWidth(0.0);
        this.anchorPane2.setMinHeight(0.0);

        this.datasetsPane.setPrefHeight(200.0);
        this.datasetsPane.setPrefWidth(200.0);

        this.objectsViewPane.setMinHeight(200.0);
        this.objectsViewPane.setMinWidth(200.0);

        this.stellarObjectPane.setMinHeight(200.0);
        this.stellarObjectPane.setMinWidth(200.0);

        this.routingPane.setMinHeight(200);
        this.routingPane.setMinWidth(200);

        this.toolBar.setPrefHeight(40.0);
        this.toolBar.setPrefWidth(Universe.boxWidth + 20);

        this.vbox1.setPrefHeight(39.0);
        this.vbox1.setPrefWidth(Universe.boxWidth + 20);

        setSliderControl();
        setStatusPanel();

        // get colors from DB
        getGraphColorsFromDB();

        // left display
        createLeftDisplay();

        // right display
        createRightDisplay();

        // create the list of objects in view
        setupStellarObjectListView();

        // create a data set pane for the database files present
        setupDataSetView();

        // by default side panel should be off
        toggleSidePane(false);

        // load database preset values
        loadDBPresets();

        queryDialog = new QueryDialog(searchContext, tripsContext.getDataSetContext(), this, this);
        queryDialog.initModality(Modality.NONE);

    }

    private void loadDBPresets() {
        // get graph enables from DB
        getGraphEnablesFromDB();
        // get Star definitions from DB
        getStarDefinitionsFromDB();
        // get civilizations/polities
        getCivilizationsFromDB();
    }

    private void getCivilizationsFromDB() {
        CivilizationDisplayPreferences civilizationDisplayPreferences = databaseManagementService.getCivilizationDisplayPreferences();
        tripsContext.getAppViewPreferences().setCivilizationDisplayPreferences(civilizationDisplayPreferences);
        interstellarSpacePane.setCivilizationPreferences(civilizationDisplayPreferences);
    }

    private void getGraphColorsFromDB() {
        ColorPalette colorPalette = databaseManagementService.getGraphColorsFromDB();
        tripsContext.getAppViewPreferences().setColorPallete(colorPalette);
    }

    public void getGraphEnablesFromDB() {
        GraphEnablesPersist graphEnablesPersist = databaseManagementService.getGraphEnablesFromDB();
        tripsContext.getAppViewPreferences().setGraphEnablesPersist(graphEnablesPersist);

        updateToggles(graphEnablesPersist);

        polities = graphEnablesPersist.isDisplayPolities();
        gridOn = graphEnablesPersist.isDisplayGrid();
        extensionsOn = graphEnablesPersist.isDisplayStems();
        labelsOn = graphEnablesPersist.isDisplayLabels();
        scaleOn = graphEnablesPersist.isDisplayLegend();
        routesOn = graphEnablesPersist.isDisplayRoutes();

        // set defaults
        interstellarSpacePane.setGraphPresets(graphEnablesPersist);
    }

    /**
     * get the star definitions from the db
     */
    private void getStarDefinitionsFromDB() {
        List<StarDetailsPersist> starDetailsPersistList = databaseManagementService.getStarDetails();
        StarDisplayPreferences starDisplayPreferences = new StarDisplayPreferences();
        starDisplayPreferences.setStars(starDetailsPersistList);
        tripsContext.getAppViewPreferences().setStarDisplayPreferences(starDisplayPreferences);
        interstellarSpacePane.setStellarPreferences(starDisplayPreferences);
    }

    /**
     * update the graph toggles
     *
     * @param graphEnablesPersist the graph
     */
    private void updateToggles(GraphEnablesPersist graphEnablesPersist) {
        if (graphEnablesPersist.isDisplayGrid()) {
            interstellarSpacePane.toggleGrid(graphEnablesPersist.isDisplayGrid());
            toggleGridBtn.setSelected(graphEnablesPersist.isDisplayGrid());
            toggleGridMenuitem.setSelected(graphEnablesPersist.isDisplayGrid());
        }

        if (graphEnablesPersist.isDisplayPolities()) {
            interstellarSpacePane.togglePolities(graphEnablesPersist.isDisplayPolities());
            togglePolityBtn.setSelected(graphEnablesPersist.isDisplayPolities());
            togglePolitiesMenuitem.setSelected(graphEnablesPersist.isDisplayPolities());
        }

        if (graphEnablesPersist.isDisplayLabels()) {
            interstellarSpacePane.toggleLabels(graphEnablesPersist.isDisplayLabels());
            toggleLabelsBtn.setSelected(graphEnablesPersist.isDisplayLabels());
            toggleLabelsMenuitem.setSelected(graphEnablesPersist.isDisplayLabels());
        }

        if (graphEnablesPersist.isDisplayStems()) {
            interstellarSpacePane.toggleExtensions(graphEnablesPersist.isDisplayStems());
            toggleStemBtn.setSelected(graphEnablesPersist.isDisplayStems());
            toggleExtensionsMenuitem.setSelected(graphEnablesPersist.isDisplayStems());
        }

        if (graphEnablesPersist.isDisplayLegend()) {
            interstellarSpacePane.toggleScale(graphEnablesPersist.isDisplayLegend());
            toggleScaleBtn.setSelected(graphEnablesPersist.isDisplayLegend());
            toggleScaleMenuitem.setSelected(graphEnablesPersist.isDisplayLegend());
        }

        if (graphEnablesPersist.isDisplayRoutes()) {
            interstellarSpacePane.toggleRoutes(graphEnablesPersist.isDisplayRoutes());
            toggleRoutesBtn.setSelected(graphEnablesPersist.isDisplayRoutes());
            toggleRoutesMenuitem.setSelected(graphEnablesPersist.isDisplayRoutes());
        }

        toggleToolBarMenuitem.setSelected(toolBarOn);
        toggleStatusBarMenuitem.setSelected(statusBarOn);
    }

    private void setupDataSetView() {

        SearchContext searchContext = tripsContext.getSearchContext();
        datasetsPane.setContent(dataSetsListView);

        dataSetsListView.setPrefHeight(10);
        dataSetsListView.setCellFactory(new DataSetDescriptorCellFactory(this, this));
        dataSetsListView.getSelectionModel().selectedItemProperty().addListener(this::datasetDescriptorChanged);

        loadDatasets(searchContext);
        log.info("Application up and running");
    }

    private void loadDatasets(SearchContext searchContext) {
        // load viable datasets into search context
        List<DataSetDescriptor> dataSets = loadDataSetView();
        if (dataSets.size() > 0) {
            searchContext.addDataSets(dataSets);
        }
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
    }


    private List<DataSetDescriptor> loadDataSetView() {

        List<DataSetDescriptor> dataSetDescriptorList = databaseManagementService.getDataSets();

        for (DataSetDescriptor descriptor : dataSetDescriptorList) {
            if (descriptor.getRoutesStr() != null) {
                routeList = descriptor.getRoutes();
                log.info("routes");
            }
        }

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
        interstellarSpacePane = new InterstellarSpacePane(
                Universe.boxWidth, Universe.boxHeight, depth,
                spacing,
                tripsContext,
                this, this, this,
                this, this, this, this);

        leftDisplayPane.getChildren().add(interstellarSpacePane);

        // put the interstellar space on top and the solar system to the back
        interstellarSpacePane.toFront();

        // set the interstellar pane to be the drawing surface
        astrographicPlotter.setInterstellarPane(interstellarSpacePane);

        // setup simulator
        simulatorService = new SimulatorService(interstellarSpacePane, width, height, depth, colorPalette);

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
        createStellarPane();
        createRoutingPane();
    }

    /**
     * create stellar pane
     */
    private void createStellarPane() {
        stellarObjectPane.setPrefHeight(800);
        starPropertiesPane = new StarPropertiesPane();
        ScrollPane scrollPane = new ScrollPane(starPropertiesPane);
        stellarObjectPane.setContent(scrollPane);
    }

    /**
     * create the routing pane
     */
    private void createRoutingPane() {
        routingPanel = new RoutingPanel();
        routingPane.setContent(routingPanel);
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

    /**
     * run the query
     *
     * @param actionEvent the event
     */
    public void runQuery(ActionEvent actionEvent) {
        queryDialog.show();
    }

    public void simulate(ActionEvent actionEvent) {
        simulatorService.simulate();
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

    public void togglePolities(ActionEvent actionEvent) {
        this.polities = !polities;
        tripsContext.getAppViewPreferences().getGraphEnablesPersist().setDisplayPolities(polities);
        interstellarSpacePane.togglePolities(polities);
        togglePolitiesMenuitem.setSelected(polities);
        togglePolityBtn.setSelected(polities);
    }

    public void toggleGrid(ActionEvent actionEvent) {
        this.gridOn = !gridOn;
        tripsContext.getAppViewPreferences().getGraphEnablesPersist().setDisplayGrid(gridOn);
        interstellarSpacePane.toggleGrid(gridOn);
        toggleGridMenuitem.setSelected(gridOn);
        toggleGridBtn.setSelected(gridOn);
    }

    public void toggleGridExtensions(ActionEvent actionEvent) {
        this.extensionsOn = !extensionsOn;
        tripsContext.getAppViewPreferences().getGraphEnablesPersist().setDisplayStems(extensionsOn);
        interstellarSpacePane.toggleExtensions(extensionsOn);
        toggleGridMenuitem.setSelected(extensionsOn);
        toggleGridBtn.setSelected(extensionsOn);
    }


    public void toggleLabels(ActionEvent actionEvent) {
        this.labelsOn = !labelsOn;
        tripsContext.getAppViewPreferences().getGraphEnablesPersist().setDisplayLabels(labelsOn);
        interstellarSpacePane.toggleLabels(labelsOn);
        toggleLabelsMenuitem.setSelected(labelsOn);
        toggleLabelsBtn.setSelected(labelsOn);
    }

    public void toggleStars(ActionEvent actionEvent) {
        this.starsOn = !starsOn;
        interstellarSpacePane.toggleStars(starsOn);
        toggleStarMenuitem.setSelected(starsOn);
        toggleStarBtn.setSelected(starsOn);
    }

    public void toggleScale(ActionEvent actionEvent) {
        this.scaleOn = !scaleOn;
        tripsContext.getAppViewPreferences().getGraphEnablesPersist().setDisplayLegend(scaleOn);
        interstellarSpacePane.toggleScale(scaleOn);
        toggleScaleMenuitem.setSelected(scaleOn);
        toggleScaleBtn.setSelected(scaleOn);
    }

    public void toggleRoutes(ActionEvent actionEvent) {
        this.routesOn = !routesOn;
        tripsContext.getAppViewPreferences().getGraphEnablesPersist().setDisplayRoutes(routesOn);
        interstellarSpacePane.toggleRoutes(routesOn);
        toggleRoutesMenuitem.setSelected(routesOn);
        toggleRoutesBtn.setSelected(routesOn);
    }

    public void toggleTransitAction(ActionEvent actionEvent) {
        this.transitsOn = !transitsOn;
        toggleTransit(transitsOn);
    }

    public void toggleTransitLengths(ActionEvent actionEvent) {
        this.transitsLengthsOn = !transitsLengthsOn;
        interstellarSpacePane.toggleTransitLengths(transitsLengthsOn);
        if (transitsLengthsOn) {
            this.transitsOn = true;
            toggleTransit(true);
        }
    }

    private void toggleTransit(boolean transitWish) {
        toggleTransitsMenuitem.setSelected(transitWish);
        toggleTransitsBtn.setSelected(transitWish);
        interstellarSpacePane.toggleTransits(transitWish);
    }

    public void toggleSidePane(ActionEvent actionEvent) {
        sidePaneOn = !sidePaneOn;
        toggleSidePane(sidePaneOn);
        toggleGridBtn.setSelected(sidePaneOn);
        toggleSidePaneMenuitem.setSelected(sidePaneOn);
    }


    public void toggleSidePane(boolean sidePanelOn) {
        if (sidePanelOn) {
            mainSplitPane.setDividerPositions(0.76);
        } else {
            mainSplitPane.setDividerPositions(1.0);
        }
    }

    public void toggleToolbar(ActionEvent actionEvent) {
        toolBarOn = !toolBarOn;
        toolBar.setVisible(!toolBar.isVisible());
        toggleToolBarMenuitem.setSelected(toolBarOn);
    }

    public void toggleStatusBar(ActionEvent actionEvent) {
        statusBarOn = !statusBarOn;
        statusBar.setVisible(!statusBar.isVisible());
        toggleStatusBarMenuitem.setSelected(statusBarOn);
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


    public void resetView(ActionEvent actionEvent) {
        interstellarSpacePane.setInitialView();
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    ////////// importer and exporters  //////////////////

    public void viewEditStarData(ActionEvent actionEvent) {
        showTableData();
    }

    ///////////// Reports /////////

    public void distanceReport(ActionEvent actionEvent) {
        List<StarDisplayRecord> starsInView = interstellarSpacePane.getCurrentStarsInView();
        if (starsInView.size() > 0) {
            SelectStarForDistanceReportDialog selectDialog = new SelectStarForDistanceReportDialog(starsInView);
            Optional<DistanceReportSelection> optionalStarDisplayRecord = selectDialog.showAndWait();
            if (optionalStarDisplayRecord.isPresent()) {
                DistanceReportSelection reportSelection = optionalStarDisplayRecord.get();
                if (reportSelection.isSelected()) {
                    generateDistanceReport(reportSelection.getRecord());
                }
            }
        } else {
            showWarningMessage("No Visible Stars", "There are no visible stars in the plot. Please plot some first");
        }
    }

    public void routeFinder(ActionEvent actionEvent) {
        RouteFinder routeFinder = new RouteFinder(interstellarSpacePane);
        if (interstellarSpacePane.getCurrentStarsInView().size() > 2) {
            routeFinder.startRouteLocation();
        } else {
            showErrorAlert("Route Finder", "You need to have more than 2 stars on a plot to use.");
        }
    }

    public void routeFinderTree(ActionEvent actionEvent) {

    }

    /////////  About /////////////

    public void aboutTrips(ActionEvent actionEvent) {
        AboutDialog aboutDialog = new AboutDialog(localization);
        aboutDialog.showAndWait();
    }

    public void howToSupport(ActionEvent actionEvent) {
        showWarningMessage("Get Support", "Not currently supported");
    }

    public void checkUpdate(ActionEvent actionEvent) {
        showWarningMessage("Check for Update", "Not currently supported");
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

        DataSetDescriptor descriptor = searchQuery.getDescriptor();

        routingPanel.setContext(descriptor);

        // do a search and cause the plot to show it
        List<AstrographicObject> astrographicObjects = getAstrographicObjectsOnQuery();

        if (!astrographicObjects.isEmpty()) {
            if (showPlot) {
                astrographicPlotter.drawAstrographicData(descriptor,
                        astrographicObjects,
                        searchQuery.getCenterCoordinates(),
                        tripsContext.getAppViewPreferences().getColorPallete(),
                        tripsContext.getAppViewPreferences().getStarDisplayPreferences(),
                        tripsContext.getAppViewPreferences().getCivilizationDisplayPreferences()
                );
            }
            if (showTable) {
                showList(astrographicObjects);
            }
            updateStatus("Dataset loaded is: " + descriptor.getDataSetName());
            setContextDataSet(descriptor);

        } else {
            showErrorAlert("Astrographic data view error", "No Astrographic data was loaded ");
        }
    }

    /**
     * show the data
     *
     * @param dataSetDescriptor the dataset descriptor
     * @param showPlot          show the graphical plot
     * @param showTable         show the table
     */
    @Override
    public void showNewStellarData(DataSetDescriptor dataSetDescriptor, boolean showPlot, boolean showTable) {
        setContextDataSet(dataSetDescriptor);
        showNewStellarData(showPlot, showTable);
    }

    /**
     * display plot or data based on default query
     *
     * @param showPlot  show the graphical plot
     * @param showTable show the table
     */
    @Override
    public void showNewStellarData(boolean showPlot, boolean showTable) {
        AstroSearchQuery searchQuery = tripsContext.getSearchContext().getAstroSearchQuery();

        routingPanel.setContext(searchQuery.getDescriptor());

        // do a search and cause the plot to show it
        List<AstrographicObject> astrographicObjects = getAstrographicObjectsOnQuery();

        if (!astrographicObjects.isEmpty()) {
            if (showPlot) {
                astrographicPlotter.drawAstrographicData(searchQuery.getDescriptor(),
                        astrographicObjects,
                        searchQuery.getCenterCoordinates(),
                        tripsContext.getAppViewPreferences().getColorPallete(),
                        tripsContext.getAppViewPreferences().getStarDisplayPreferences(),
                        tripsContext.getAppViewPreferences().getCivilizationDisplayPreferences()
                );
            }
            if (showTable) {
                showList(astrographicObjects);
            }
            updateStatus("Dataset loaded is: " + searchQuery.getDescriptor().getDataSetName());

            // highlight the data set used
            setContextDataSet(searchQuery.getDescriptor());

        } else {
            showErrorAlert("Astrographic data view error", "No Astrographic data was loaded ");
        }
    }

    @Override
    public void addDataSet(DataSetDescriptor dataSetDescriptor) {
        searchContext.addDataSet(dataSetDescriptor);
        addDataSetToList(new ArrayList<>(searchContext.getDatasetMap().values()), true);
        queryDialog.updateDataContext(dataSetDescriptor);
        updateStatus("Dataset: " + dataSetDescriptor.getDataSetName() + " loaded");
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
            updateStatus("Dataset: " + dataSetDescriptor.getDataSetName() + " removed");
        }
    }

    @Override
    public void setContextDataSet(DataSetDescriptor descriptor) {
        tripsContext.getDataSetContext().setDescriptor(descriptor);
        tripsContext.getDataSetContext().setValidDescriptor(true);
        tripsContext.getSearchContext().getAstroSearchQuery().setDescriptor(descriptor);
        dataSetsListView.getSelectionModel().select(descriptor);
        queryDialog.setDataSetContext(descriptor);
        interstellarSpacePane.setDataSetContext(descriptor);

        updateStatus(descriptor.getDataSetName() + " is the active context");
    }

    private void showList(List<AstrographicObject> astrographicObjects) {
        new DataSetTable(databaseManagementService, astrographicObjects);
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
        List<DataSetDescriptor> datasets = databaseManagementService.getDataSets();
        if (datasets.size() == 0) {
            showErrorAlert("Plot Stars", "No datasets loaded, please load one");
            return;
        }

        if (tripsContext.getDataSetContext().isValidDescriptor()) {
            DataSetDescriptor dataSetDescriptor = tripsContext.getDataSetContext().getDescriptor();
            drawStars(dataSetDescriptor);
            routingPanel.setContext(dataSetDescriptor);
        } else {

            List<String> dialogData = datasets.stream().map(DataSetDescriptor::getDataSetName).collect(Collectors.toList());

            ChoiceDialog<String> dialog = new ChoiceDialog<>(dialogData.get(0), dialogData);
            dialog.setTitle("Choice Data set to display");
            dialog.setHeaderText("Select your choice - (Default display is 20 light years from Earth, use Show Stars filter to change)");

            Optional<String> result = dialog.showAndWait();

            if (result.isPresent()) {
                String selected = result.get();

                DataSetDescriptor dataSetDescriptor = findFromDataSet(selected, datasets);
                if (dataSetDescriptor == null) {
                    log.error("How the hell did this happen");
                    return;
                }

                // update the routing table in the side panel
                routingPanel.setContext(dataSetDescriptor);

                drawStars(dataSetDescriptor);
                setContextDataSet(dataSetDescriptor);
            }
        }
    }

    private void drawStars(DataSetDescriptor dataSetDescriptor) {
        List<AstrographicObject> astrographicObjects = databaseManagementService.getFromDatasetWithinLimit(
                dataSetDescriptor,
                searchContext.getAstroSearchQuery().getDistanceFromCenterStar());
        log.info("DB Query returns {} stars", astrographicObjects.size());

        if (!astrographicObjects.isEmpty()) {
            AstroSearchQuery astroSearchQuery = searchContext.getAstroSearchQuery();
            astroSearchQuery.zeroCenter();
            astrographicPlotter.drawAstrographicData(
                    tripsContext.getSearchContext().getAstroSearchQuery().getDescriptor(),
                    astrographicObjects,
                    astroSearchQuery.getCenterCoordinates(),
                    tripsContext.getAppViewPreferences().getColorPallete(),
                    tripsContext.getAppViewPreferences().getStarDisplayPreferences(),
                    tripsContext.getAppViewPreferences().getCivilizationDisplayPreferences()
            );
            updateStatus("Dataset plotted is: " + dataSetDescriptor.getDataSetName());
        } else {
            showErrorAlert("Astrographic data view error", "No Astrographic data was loaded ");
        }
    }

    /**
     * show the data in a spreadsheet
     */
    private void showTableData() {

        if (tripsContext.getDataSetContext().isValidDescriptor()) {
            List<AstrographicObject> astrographicObjects = getAstrographicObjectsOnQuery();
            new DataSetTable(databaseManagementService, astrographicObjects);
        } else {
            List<DataSetDescriptor> datasets = databaseManagementService.getDataSets();
            if (datasets.size() == 0) {
                showErrorAlert("Plot Stars", "No datasets loaded, please load one");
                return;
            }

            List<String> dialogData = datasets.stream().map(DataSetDescriptor::getDataSetName).collect(Collectors.toList());

            ChoiceDialog<String> dialog = new ChoiceDialog<>(dialogData.get(0), dialogData);
            dialog.setTitle("Choice Data set to display");
            dialog.setHeaderText("Select your choice - (Default display is 20 light years from Earth, use Show Stars filter to change)");

            Optional<String> result = dialog.showAndWait();

            if (result.isPresent()) {
                String selected = result.get();

                DataSetDescriptor dataSetDescriptor = findFromDataSet(selected, datasets);
                if (dataSetDescriptor == null) {
                    log.error("How the hell did this happen");
                    return;
                }
                List<AstrographicObject> astrographicObjects = getAstrographicObjectsOnQuery();
                new DataSetTable(databaseManagementService, astrographicObjects);
                updateStatus("Dataset table loaded is: " + dataSetDescriptor.getDataSetName());

                // set current context
                setContextDataSet(dataSetDescriptor);
            }
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
        updateStatus("Selected Interstellar space");
    }

    /**
     * select the solar space
     *
     * @param starDisplayRecord the properties of the selected object
     */
    @Override
    public void selectSolarSystemSpace(StarDisplayRecord starDisplayRecord) {
        log.info("Showing a solar system");
        solarSystemSpacePane.setSystemToDisplay(starDisplayRecord);
        solarSystemSpacePane.render();
        solarSystemSpacePane.toFront();
        updateStatus("Selected Solarsystem space");
    }

    @Override
    public void updateList(StarDisplayRecord starDisplayRecord) {
        objectViewPane.add(starDisplayRecord);
    }

    @Override
    public void clearList() {
        objectViewPane.clear();
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
    public void highlightStar(UUID starId) {
        interstellarSpacePane.highlightStar(starId);
    }

    /**
     * generate the distance report
     *
     * @param starDescriptor the star to generate the report about
     */
    @Override
    public void generateDistanceReport(StarDisplayRecord starDescriptor) {
        log.info("generate the distance report");

        DistanceReport report = new DistanceReport(starDescriptor);
        List<StarDisplayRecord> starsInView = interstellarSpacePane.getCurrentStarsInView();
        for (StarDisplayRecord record : starsInView) {
            report.findDistance(record);
        }
        // generate the report
        report.generateReport();
        DistanceReportDialog reportDialog = new DistanceReportDialog(report);
        Optional<DistanceReport> reportOptional = reportDialog.showAndWait();
        if (reportOptional.isPresent()) {
            DistanceReport distanceReport = reportOptional.get();
            if (distanceReport.isSaveSelected()) {
                String reportToSave = report.getGeneratedReport();
                storeFile(reportToSave);
            }
        }
        log.info("report complete");
    }

    /**
     * store a report file
     *
     * @param report the report as a string
     */
    private void storeFile(String report) {
        log.debug("Store the report format file");
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enter Report file to save");
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("TXT Files", "txt");
        fileChooser.setSelectedExtensionFilter(filter);
        File file = fileChooser.showSaveDialog(getStage());
        if (file != null) {
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                writer.write(report);
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
    public void routingStatus(boolean statusFlag) {
        if (statusFlag) {
            routingStatus.setTextFill(Color.RED);
            routingStatus.setText("Active");
        } else {
            routingStatus.setTextFill(Color.SEAGREEN);
            routingStatus.setText("Inactive");
        }
    }

    @Override
    public void newRoute(DataSetDescriptor dataSetDescriptor, RouteDescriptor routeDescriptor) {
        log.info("new route");
        databaseManagementService.addRouteToDataSet(dataSetDescriptor, routeDescriptor);
        routingPanel.setContext(dataSetDescriptor);
        routingStatus(false);
    }


    @Override
    public void updateRoute(RouteDescriptor routeDescriptor) {
        log.info("update route");

    }

    @Override
    public void deleteRoute(RouteDescriptor routeDescriptor) {
        log.info("delete route");

        // delete from database @todo
    }

    ////////////////////////////////

    @Override
    public void displayStellarProperties(AstrographicObject astrographicObject) {
        if (astrographicObject != null) {
            starPropertiesPane.setStar(astrographicObject);
            propertiesAccordion.setExpandedPane(stellarObjectPane);
        }
    }

    /**
     * sets up list view for stellar objects
     */
    private void setupStellarObjectListView() {

        objectViewPane = new ObjectViewPane(
                this,
                this,
                this,
                this,
                this);

        // setup model to display in case we turn on
        objectsViewPane.setContent(objectViewPane);

    }

    /////////////////////////  Shutdown   /////////////////////////

    private void shutdown() {
        log.debug("Exit selection");
        Optional<ButtonType> result = AlertFactory.showConfirmationAlert(
                "Exit Application",
                "Exit Application?",
                "Are you sure you want to leave?");

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


    public void loadDataSetManager(ActionEvent actionEvent) {

        DataSetManagerDialog dialog = new DataSetManagerDialog(
                this,
                tripsContext.getDataSetContext(),
                databaseManagementService,
                dataImportService,
                localization,
                dataExportService);

        // we throw away the result after returning
        dialog.showAndWait();
    }

    ///////////////////////////////////////////////
    @Override
    public void updateGraphColors(ColorPalette colorPalette) {

        tripsContext.getAppViewPreferences().setColorPallete(colorPalette);

        // colors changes so update db
        databaseManagementService.updateColors(colorPalette);
        interstellarSpacePane.changeColors(colorPalette);
        log.debug("UPDATE COLORS!!!");
    }


    @Override
    public void changesGraphEnables(GraphEnablesPersist graphEnablesPersist) {
        tripsContext.getAppViewPreferences().setGraphEnablesPersist(graphEnablesPersist);

        updateToggles(graphEnablesPersist);

        databaseManagementService.updateGraphEnables(graphEnablesPersist);
        astrographicPlotter.changeGraphEnables(graphEnablesPersist);
        log.debug("UPDATE GRAPH ENABLES!!!");
    }

    @Override
    public void changeStarPreferences(StarDisplayPreferences starDisplayPreferences) {
        databaseManagementService.updateStarPreferences(starDisplayPreferences);
    }

    @Override
    public void changePolitiesPreferences(CivilizationDisplayPreferences civilizationDisplayPreferences) {
        databaseManagementService.updateCivilizationDisplayPreferences(civilizationDisplayPreferences);
    }

    @Override
    public void updateNotesForStar(UUID recordId, String notes) {
        databaseManagementService.updateNotesOnStar(recordId, notes);
    }

    @Override
    public AstrographicObject getStar(UUID starId) {
        return databaseManagementService.getStar(starId);
    }

    @Override
    public void removeStar(UUID recordId) {
        databaseManagementService.removeStar(recordId);
    }

    public void runAnimation(ActionEvent actionEvent) {
        interstellarSpacePane.toggleAnimation();
    }

    public void findInView(ActionEvent actionEvent) {
        List<StarDisplayRecord> starsInView = interstellarSpacePane.getCurrentStarsInView();
        FindStarInViewDialog findStarInViewDialog = new FindStarInViewDialog(starsInView);
        Optional<FindResults> optional = findStarInViewDialog.showAndWait();
        if (optional.isPresent()) {
            FindResults findResults = optional.get();
            if (findResults.isSelected()) {

                log.info("Value chose = {}", findResults.getRecord());
                interstellarSpacePane.highlightStar(findResults.getRecord().getRecordId());
            }
        }
    }


    public void transitFinder(ActionEvent actionEvent) {

        FindTransitsBetweenStarsDialog findTransitsBetweenStarsDialog = new FindTransitsBetweenStarsDialog();
        Optional<DistanceRoutes> optionalDistanceRoutes = findTransitsBetweenStarsDialog.showAndWait();
        if (optionalDistanceRoutes.isPresent()) {
            DistanceRoutes distanceRoutes = optionalDistanceRoutes.get();
            if (distanceRoutes.isSelected()) {
                interstellarSpacePane.findTransits(distanceRoutes);
            }
        }
    }

    @Override
    public void updateStatus(String message) {
        databaseStatus.setText(message);
    }


    public void clearTransits(ActionEvent actionEvent) {
        interstellarSpacePane.clearTransits();
        toggleTransitsBtn.setSelected(false);
        toggleTransitsMenuitem.setSelected(false);
        toggleTransitLengthsMenuitem.setSelected(false);
    }

    public void loadMuliple(ActionEvent actionEvent) {
        dataImportService.loadDatabase();
        // load datasets into the system
        SearchContext searchContext = tripsContext.getSearchContext();
        loadDatasets(searchContext);
    }

    public void exportDatabase(ActionEvent actionEvent) {
        dataExportService.exportDB();
    }

}
