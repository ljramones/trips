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
import com.teamgannon.trips.dialogs.dataset.ExportOptions;
import com.teamgannon.trips.dialogs.preferences.ViewPreferencesDialog;
import com.teamgannon.trips.dialogs.query.AdvResultsSet;
import com.teamgannon.trips.dialogs.query.AdvancedQueryDialog;
import com.teamgannon.trips.dialogs.query.QueryDialog;
import com.teamgannon.trips.dialogs.search.FindStarInViewDialog;
import com.teamgannon.trips.dialogs.search.FindStarsWithNameMatchDialog;
import com.teamgannon.trips.dialogs.search.FindTransitsBetweenStarsDialog;
import com.teamgannon.trips.dialogs.search.ShowStarMatchesDialog;
import com.teamgannon.trips.dialogs.search.model.DistanceRoutes;
import com.teamgannon.trips.dialogs.search.model.FindResults;
import com.teamgannon.trips.dialogs.search.model.StarSearchResults;
import com.teamgannon.trips.dialogs.startup.EachTimeStartDialog;
import com.teamgannon.trips.dialogs.startup.FirstStartDialog;
import com.teamgannon.trips.graphics.AstrographicPlotter;
import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.graphics.panes.InterstellarSpacePane;
import com.teamgannon.trips.graphics.panes.SolarSystemSpacePane;
import com.teamgannon.trips.jpa.model.*;
import com.teamgannon.trips.listener.*;
import com.teamgannon.trips.report.distance.DistanceReportSelection;
import com.teamgannon.trips.report.distance.SelectStarForDistanceReportDialog;
import com.teamgannon.trips.routing.Route;
import com.teamgannon.trips.routing.RouteFinderInView;
import com.teamgannon.trips.routing.RouteFinderOffline;
import com.teamgannon.trips.routing.RoutingPanel;
import com.teamgannon.trips.screenobjects.ObjectViewPane;
import com.teamgannon.trips.screenobjects.StarPropertiesPane;
import com.teamgannon.trips.search.AstroSearchQuery;
import com.teamgannon.trips.search.SearchContext;
import com.teamgannon.trips.service.DataExportService;
import com.teamgannon.trips.service.DataImportService;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.model.ExportFileType;
import com.teamgannon.trips.support.AlertFactory;
import com.teamgannon.trips.tableviews.DataSetTable;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxWeaver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.zondicons.Zondicons;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static com.teamgannon.trips.support.AlertFactory.*;

@Slf4j
@Component
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

    ////// injected properties
    public Pane mainPanel;

    public MenuBar menuBar;
    public CheckMenuItem togglePolitiesMenuitem;
    public CheckMenuItem toggleGridMenuitem;
    public CheckMenuItem toggleLabelsMenuitem;
    public CheckMenuItem toggleExtensionsMenuitem;
    public CheckMenuItem toggleStarMenuitem;
    public CheckMenuItem toggleScaleMenuitem;
    public CheckMenuItem toggleSidePaneMenuitem;
    public CheckMenuItem toggleToolBarMenuitem;
    public CheckMenuItem toggleStatusBarMenuitem;
    public CheckMenuItem toggleTransitsMenuitem;
    public CheckMenuItem toggleTransitLengthsMenuitem;
    public CheckMenuItem toggleRoutesMenuitem;

    public ToolBar toolBar;
    public ToggleButton togglePolityBtn;
    public ToggleButton toggleStarBtn;
    public ToggleButton toggleGridBtn;
    public ToggleButton toggleLabelsBtn;
    public ToggleButton toggleStemBtn;
    public ToggleButton toggleScaleBtn;
    public ToggleButton toggleRoutesBtn;
    public ToggleButton toggleTransitsBtn;
    public ToggleButton toggleSettings;
    public ToggleButton toggleZoomInBtn;
    public ToggleButton toggleZoomOutBtn;
    public CheckBox animationCheckbox;
    public SplitPane mainSplitPane;
    public HBox statusBar;

    public Label databaseStatus;
    public Label routingStatus;
    public Button plotButton;

    ////  local assets

    private TitledPane datasetsPane;
    private RoutingPanel routingPanel;
    private StarPropertiesPane starPropertiesPane;
    public Accordion propertiesAccordion;
    public TitledPane stellarObjectPane;
    private ObjectViewPane objectViewPane;
    private VBox settingsPane;
    private StackPane leftDisplayPane;
    public BorderPane leftBorderPane;
    public BorderPane rightBorderPane;
    public TitledPane objectsViewPane;
    public TitledPane routingPane;


    /**
     * the query dialog
     */
    private QueryDialog queryDialog;

    /**
     * the current search context to display from
     */
    private final SearchContext searchContext;


    private Stage stage;
    private FxWeaver fxWeaver;
    private TripsContext tripsContext;

    /**
     * the TRIPS application context
     */
    private final ApplicationContext appContext;

    double sceneWidth = Universe.boxWidth;
    double sceneHeight = Universe.boxHeight;
    double depth = Universe.boxDepth;
    double spacing = 20;

    /**
     * interstellar space
     */
    private InterstellarSpacePane interstellarSpacePane;

    /**
     * solar system panes for showing the details of various solar systems
     */
    private SolarSystemSpacePane solarSystemSpacePane;

    /**
     * database management spring component service
     */
    private final DatabaseManagementService databaseManagementService;

    /**
     * star plotter component
     */
    private final AstrographicPlotter astrographicPlotter;
    private final Localization localization;

    private DataImportService dataImportService;
    private final @NotNull DataExportService dataExportService;

    /**
     * dataset lists
     */
    private final ListView<DataSetDescriptor> dataSetsListView = new ListView<>();

    /**
     * list of routes
     */
    private List<Route> routeList;

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

    private double originalHeight = Universe.boxHeight;
    private double originalWidth = Universe.boxWidth;


    /**
     * constructor
     *
     * @param fxWeaver     the FX Weaver for integrating Spring boot and JavaFX
     * @param tripsContext our trips context
     */
    public MainPane(FxWeaver fxWeaver,
                    @NotNull TripsContext tripsContext,
                    ApplicationContext appContext,
                    DatabaseManagementService databaseManagementService,
                    AstrographicPlotter astrographicPlotter,
                    Localization localization
    ) {

        this.fxWeaver = fxWeaver;

        this.tripsContext = tripsContext;
        this.searchContext = tripsContext.getSearchContext();
        this.appContext = appContext;

        this.databaseManagementService = databaseManagementService;
        this.astrographicPlotter = astrographicPlotter;
        this.localization = localization;

        this.dataExportService = new DataExportService(databaseManagementService, this);

    }

    @FXML
    public void initialize() {
        log.info("initialize view");

        setButtons();

        setDefaultSizesForUI();

        setStatusPanel();

        // get colors from DB
        getGraphColorsFromDB();

        // left display
        createLeftDisplay();

        // right display
        createRightDisplay();

        // set the sliders
        setSliderControl();

        // create the list of objects in view
        setupStellarObjectListView();

        // create a data set pane for the database files present
        setupDataSetView();

        // by default side panel should be off
        toggleSidePane(false);

        setupStatusbar();

        // load database preset values
        loadDBPresets();

        showBeginningAlert();

    }


    /////////////////////////////  CREATE ASSETS  ////////////////////////////

    private void setButtons() {

        final Image toggleRoutesBtnGraphic = new Image("/images/buttons/tb_routes.gif");
        final ImageView toggleRoutesBtnImage = new ImageView(toggleRoutesBtnGraphic);
        toggleRoutesBtn.setGraphic(toggleRoutesBtnImage);
        toggleRoutesBtn.setTooltip(new Tooltip("Toggle routes"));

        FontIcon fontIconZoomIn = new FontIcon(Zondicons.ZOOM_IN);
        toggleZoomInBtn.setGraphic(fontIconZoomIn);
        toggleZoomInBtn.setTooltip(new Tooltip("Zoom in"));

        FontIcon fontIconZoomOut = new FontIcon(Zondicons.ZOOM_OUT);
        toggleZoomOutBtn.setGraphic(fontIconZoomOut);
        toggleZoomOutBtn.setTooltip(new Tooltip("Zoom out"));

        final Image toggleGridBtnGraphic = new Image("/images/buttons/tb_grid.gif");
        final ImageView toggleGridBtnImage = new ImageView(toggleGridBtnGraphic);
        toggleGridBtn.setGraphic(toggleGridBtnImage);
        toggleGridBtn.setTooltip(new Tooltip("Toggle grid"));

        plotButton.setDisable(true);
        toolBar.setTooltip(new Tooltip("Select Dataset to enable plot"));
    }

    private void setDefaultSizesForUI() {
        this.mainPanel.setPrefWidth(Universe.boxWidth + 20);

        this.menuBar.setPrefWidth(Universe.boxWidth + 20);
        this.toolBar.setPrefWidth(Universe.boxWidth + 20);

        this.mainSplitPane.setPrefWidth(Universe.boxWidth);
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

    private void getGraphColorsFromDB() {
        ColorPalette colorPalette = databaseManagementService.getGraphColorsFromDB();
        tripsContext.getAppViewPreferences().setColorPallete(colorPalette);
    }

    /**
     * create the left part of the display
     */
    private void createLeftDisplay() {

        leftBorderPane = new BorderPane();
        leftBorderPane.setMinWidth(0);

        this.leftBorderPane.setPrefWidth(785.0);

        mainSplitPane.getItems().add(leftBorderPane);

        leftDisplayPane = new StackPane();
        this.leftDisplayPane.setMinWidth(Universe.boxWidth + 100);
        this.leftDisplayPane.setPrefWidth(Universe.boxWidth);

        leftBorderPane.setLeft(leftDisplayPane);

        // create the solar system
        createSolarSystemSpace();

        // create the interstellar space
        createInterstellarSpace(tripsContext.getAppViewPreferences().getColorPallete());
    }

    /**
     * create the right portion of the display
     */
    private void createRightDisplay() {

        rightBorderPane = new BorderPane();
        mainSplitPane.getItems().add(rightBorderPane);

        rightBorderPane.setMinWidth(0);
        settingsPane = new VBox();
        settingsPane.setPrefHeight(588.0);
        settingsPane.setPrefWidth(260.0);

        rightBorderPane.setRight(settingsPane);

        propertiesAccordion = new Accordion();
        settingsPane.getChildren().add(propertiesAccordion);

        // datasets pane
        datasetsPane = new TitledPane();
        datasetsPane.setText("DataSets Available");
        datasetsPane.setMinWidth(200);
        datasetsPane.setMinHeight(200);
        datasetsPane.setMaxHeight(500);
        propertiesAccordion.getPanes().add(datasetsPane);

        // objects in pane
        objectsViewPane = new TitledPane();
        objectsViewPane.setText("Objects in View");
        objectsViewPane.setMinWidth(200);
        objectsViewPane.setMinHeight(200);
        objectsViewPane.setMaxHeight(460);
        propertiesAccordion.getPanes().add(objectsViewPane);

        // stellar pane
        stellarObjectPane = new TitledPane();
        stellarObjectPane.setText("Stellar Object Properties");
        stellarObjectPane.setPrefHeight(500);
        stellarObjectPane.setMaxHeight(520);
        starPropertiesPane = new StarPropertiesPane();
        ScrollPane scrollPane = new ScrollPane(starPropertiesPane);
        stellarObjectPane.setContent(scrollPane);
        propertiesAccordion.getPanes().add(stellarObjectPane);

        // routing pane
        routingPane = new TitledPane();
        routingPane.setText("Star Routing");
        routingPane.setMinWidth(200);
        routingPane.setMinHeight(400);
        routingPane.setMaxHeight(400);
        routingPanel = new RoutingPanel();
        routingPane.setContent(routingPanel);
        propertiesAccordion.getPanes().add(routingPane);
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

    private void setupDataSetView() {

        SearchContext searchContext = tripsContext.getSearchContext();
        datasetsPane.setContent(dataSetsListView);

        dataSetsListView.setPrefHeight(10);
        dataSetsListView.setCellFactory(new DataSetDescriptorCellFactory(this, this));
        dataSetsListView.getSelectionModel().selectedItemProperty().addListener(this::datasetDescriptorChanged);

        loadDatasets(searchContext);
        log.info("Application up and running");
    }

    public void toggleSidePane(boolean sidePanelOn) {
        if (sidePanelOn) {
            mainSplitPane.setDividerPositions(0.76);
        } else {
            mainSplitPane.setDividerPositions(1.0);
        }
    }

    private void setupStatusbar() {
        Font labelFont = Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 13);
        Font statusFont = Font.font("Verdana", FontWeight.LIGHT, FontPosture.REGULAR, 13);

        this.statusBar.setPrefWidth(Universe.boxWidth + 20);
        this.statusBar.setAlignment(Pos.BASELINE_LEFT);

        GridPane gridPane = new GridPane();
        Label databaseCommentLabel = new Label("Plot Status: ");
        databaseCommentLabel.setFont(labelFont);
        databaseCommentLabel.setTextFill(Color.BLACK);
        gridPane.add(databaseCommentLabel, 0, 0);

        databaseStatus = new Label("Waiting for a dataset to be selected");
        databaseStatus.setFont(statusFont);
        databaseStatus.setTextFill(Color.BLUE);
        gridPane.add(databaseStatus, 1, 0);

        // put a unique divider
        gridPane.add(new Label("\u25AE\u25C4\u25BA\u25AE"), 2, 0);

        Label routingStatusLabel = new Label("Routing State: ");
        routingStatusLabel.setFont(labelFont);
        routingStatusLabel.setTextFill(Color.BLACK);
        gridPane.add(routingStatusLabel, 3, 0);

        routingStatus = new Label("Inactive");
        routingStatus.setFont(statusFont);
        routingStatus.setTextFill(Color.SEAGREEN);
        gridPane.add(routingStatus, 4, 0);

        this.statusBar.getChildren().add(gridPane);

    }

    private void loadDBPresets() {
        // get graph enables from DB
        getGraphEnablesFromDB();
        // get Star definitions from DB
        getStarDefinitionsFromDB();
        // get civilizations/polities
        getCivilizationsFromDB();
        // get trips preferences
        getTripsPrefsFromDB();
    }

    private void showBeginningAlert() {
        if (tripsContext.getSearchContext().getDatasetMap().isEmpty()) {
            FirstStartDialog firstStartDialog = new FirstStartDialog();
            firstStartDialog.showAndWait();
        }

        TripsPrefs tripsPrefs = tripsContext.getTripsPrefs();
        if (!tripsPrefs.isShowWelcomeDataReq()) {
            EachTimeStartDialog eachTimeStartDialog = new EachTimeStartDialog();
            Optional<Boolean> optStart = eachTimeStartDialog.showAndWait();
            if (optStart.isPresent()) {
                boolean onStart = optStart.get();
                if (onStart) {
                    log.info("selected is true");
                    tripsPrefs.setShowWelcomeDataReq(true);
                    databaseManagementService.saveTripsPrefs(tripsPrefs);
                } else {
                    log.info("selected is false");
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////

    public void setStage(@NotNull Stage stage, double sceneWidth, double sceneHeight, double controlPaneOffset) {
        this.stage = stage;
        this.sceneWidth = sceneWidth;
        this.sceneHeight = sceneHeight;

        interstellarSpacePane.setControlPaneOffset(controlPaneOffset);

        ChangeListener<Number> stageSizeListener = (observable, oldValue, newValue) -> {
            resizeTrips(stage.getHeight(), stage.getWidth());
        };

        stage.widthProperty().addListener(stageSizeListener);
        stage.heightProperty().addListener(stageSizeListener);

        queryDialog = new QueryDialog(searchContext, tripsContext.getDataSetContext(), this, this);
        queryDialog.initModality(Modality.NONE);

        this.dataImportService = new DataImportService(databaseManagementService);
    }


    private void resizeTrips(double height, double width) {

        if (Double.isNaN(height)) {
            height = Universe.boxHeight - 10;
        } else {
            height -= 10;
        }
        log.info("Height: " + height + " Width: " + width);

        interstellarSpacePane.resize(width, height);

        this.menuBar.setPrefWidth(width);
        this.toolBar.setPrefWidth(width);
        this.statusBar.setPrefWidth(width);

        this.settingsPane.setPrefHeight(height - 112);
        this.settingsPane.setPrefWidth(260);

        this.leftDisplayPane.setPrefWidth(width);
        this.leftDisplayPane.setPrefHeight(height);

        this.mainSplitPane.setPrefWidth(width);
        this.mainSplitPane.setPrefHeight(height - 112);
        // control split-pane divider so it doesn't lag while resizing
        double spPosition = mainSplitPane.getDividers().get(0).getPosition();
        if (spPosition > .95) {
            // if the divider is all the way over then make sure it stays there
            mainSplitPane.setDividerPosition(0, 1);
        } else {
            // now make sure that the divider does not go past the settings panel position
            double currentWidth = this.mainPanel.getWidth();
            double exposedSettingsWidth = (1 - spPosition) * currentWidth;
            log.info("currentWidth={}, exposedSetting={}", currentWidth, exposedSettingsWidth);
            if (exposedSettingsWidth > 262 || exposedSettingsWidth < 258) {
                double adjustedWidthRatio = 260 / currentWidth;
                mainSplitPane.setDividerPosition(0, 1 - adjustedWidthRatio);
            }
        }

        Double originalDims = Math.sqrt(originalHeight * originalWidth);
        Double newDims = Math.sqrt(height * width);

        if (originalDims < newDims) {
            interstellarSpacePane.zoomIn(2);
        } else {
            interstellarSpacePane.zoomOut(2);
        }

        originalHeight = height;
        originalWidth = width;

        Platform.runLater(() -> {
            interstellarSpacePane.updateLabels();
        });


    }


    //////////////////////////  DATABASE STUFF  /////////


    private void getTripsPrefsFromDB() {
        TripsPrefs tripsPrefs = databaseManagementService.getTripsPrefs();
        tripsContext.setTripsPrefs(tripsPrefs);
    }

    private void getCivilizationsFromDB() {
        CivilizationDisplayPreferences civilizationDisplayPreferences = databaseManagementService.getCivilizationDisplayPreferences();
        tripsContext.getAppViewPreferences().setCivilizationDisplayPreferences(civilizationDisplayPreferences);
        interstellarSpacePane.setCivilizationPreferences(civilizationDisplayPreferences);
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


    private void loadDatasets(@NotNull SearchContext searchContext) {
        // load viable datasets into search context
        List<DataSetDescriptor> dataSets = loadDataSetView();
        if (dataSets.size() > 0) {
            searchContext.addDataSets(dataSets);
        }
    }


    private @NotNull List<DataSetDescriptor> loadDataSetView() {

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

    public void addDataSetToList(@NotNull List<DataSetDescriptor> list, boolean clear) {
        if (clear) {
            dataSetsListView.getItems().clear();
        }
        list.forEach(descriptor -> dataSetsListView.getItems().add(descriptor));
        log.debug("update complete");
    }

    public void datasetDescriptorChanged(ObservableValue<? extends DataSetDescriptor> ov, @Nullable DataSetDescriptor oldValue, @Nullable DataSetDescriptor newValue) {
        String oldText = oldValue == null ? "null" : oldValue.toString();
        String newText = newValue == null ? "null" : newValue.toString();
    }


    /**
     * create a interstellar space drawing area
     *
     * @param colorPalette the colors to use in drawing
     */
    private void createInterstellarSpace(ColorPalette colorPalette) {

        // create main graphics display pane
        interstellarSpacePane = new InterstellarSpacePane(sceneWidth,
                sceneHeight,
                depth,
                spacing,
                tripsContext,
                this,
                this,
                this,
                this,
                this,
                this,
                this
        );

        leftDisplayPane.getChildren().add(interstellarSpacePane);

        // put the interstellar space on top and the solar system to the back
        interstellarSpacePane.toFront();

        // set the interstellar pane to be the drawing surface
        astrographicPlotter.setInterstellarPane(interstellarSpacePane);

    }

    /**
     * create the solar space drawing area
     */
    private void createSolarSystemSpace() {
        solarSystemSpacePane = new SolarSystemSpacePane(leftDisplayPane.getMaxWidth(), leftDisplayPane.getMaxHeight(), depth);
        solarSystemSpacePane.setContextUpdater(this);
        leftDisplayPane.getChildren().add(solarSystemSpacePane);
    }


    /////////////////////////////  UI triggers  /////////////////////


    public void plotStars(ActionEvent actionEvent) {
        showPlot();
    }

    public void viewEditStarData(ActionEvent actionEvent) {
        showTableData();
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

    public void showApplicationPreferences(ActionEvent actionEvent) {
        ApplicationPreferences applicationPreferences = tripsContext.getAppPreferences();
        ViewPreferencesDialog viewPreferencesDialog = new ViewPreferencesDialog(this, tripsContext, applicationPreferences);
        viewPreferencesDialog.showAndWait();
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

        toggleScale(null);
    }

    public void toggleLabels(ActionEvent actionEvent) {
        this.labelsOn = !labelsOn;
        tripsContext.getAppViewPreferences().getGraphEnablesPersist().setDisplayLabels(labelsOn);
        interstellarSpacePane.toggleLabels(labelsOn);
        toggleLabelsMenuitem.setSelected(labelsOn);
        toggleLabelsBtn.setSelected(labelsOn);
    }

    public void toggleGridExtensions(ActionEvent actionEvent) {
        this.extensionsOn = !extensionsOn;
        tripsContext.getAppViewPreferences().getGraphEnablesPersist().setDisplayStems(extensionsOn);
        interstellarSpacePane.toggleExtensions(extensionsOn);
        toggleGridMenuitem.setSelected(extensionsOn);
        toggleGridBtn.setSelected(extensionsOn);
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

    public void toggleSidePane(ActionEvent actionEvent) {
        sidePaneOn = !sidePaneOn;
        toggleSidePane(sidePaneOn);
        toggleGridBtn.setSelected(sidePaneOn);
        toggleSidePaneMenuitem.setSelected(sidePaneOn);
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

    public void loadDataSetManager(ActionEvent actionEvent) {

        DataSetManagerDialog dialog = new DataSetManagerDialog(
                this,
                tripsContext.getDataSetContext(),
                databaseManagementService,
                this,
                dataImportService,
                localization,
                dataExportService);

        // we throw away the result after returning
        dialog.showAndWait();
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


    public void clearTransits(ActionEvent actionEvent) {
        interstellarSpacePane.clearTransits();
        toggleTransitsBtn.setSelected(false);
        toggleTransitsMenuitem.setSelected(false);
        toggleTransitLengthsMenuitem.setSelected(false);
    }

    public void routeFinderInView(ActionEvent actionEvent) {
        RouteFinderInView routeFinderInView = new RouteFinderInView(interstellarSpacePane);
        if (interstellarSpacePane.getCurrentStarsInView().size() > 2) {
            routeFinderInView.startRouteLocation(searchContext.getAstroSearchQuery().getDescriptor().getDataSetName(), databaseManagementService);
        } else {
            showErrorAlert("Route Finder", "You need to have more than 2 stars on a plot to use.");
        }
    }

    public void routeFinderOffline(ActionEvent actionEvent) {
        RouteFinderOffline routeFinderOffline = new RouteFinderOffline(interstellarSpacePane);
        routeFinderOffline.startRouteLocation(
                searchContext.getAstroSearchQuery().getDescriptor(),
                databaseManagementService,
                tripsContext.getAppViewPreferences().getStarDisplayPreferences()
        );

    }

    public void routeFinderTree(ActionEvent actionEvent) {

    }

    public void toggleRoutes(ActionEvent actionEvent) {
        this.routesOn = !routesOn;
        tripsContext.getAppViewPreferences().getGraphEnablesPersist().setDisplayRoutes(routesOn);
        interstellarSpacePane.toggleRoutes(routesOn);
        toggleRoutesMenuitem.setSelected(routesOn);
        toggleRoutesBtn.setSelected(routesOn);
    }

    public void findInView(ActionEvent actionEvent) {
        List<StarDisplayRecord> starsInView = interstellarSpacePane.getCurrentStarsInView();
        FindStarInViewDialog findStarInViewDialog = new FindStarInViewDialog(starsInView);
        findStarInViewDialog.setOnShown(new EventHandler<DialogEvent>() {
            @Override
            public void handle(DialogEvent event) {

                //Values from screen
                int screenMaxX = (int) Screen.getPrimary().getVisualBounds().getMaxX();
                int screenMaxY = (int) Screen.getPrimary().getVisualBounds().getMaxY();

                //Values from stage
                int width = (int) stage.getWidth();
                int height = (int) stage.getHeight();
                int stageMaxX = (int) stage.getX();
                int stageMaxY = (int) stage.getY();

                //Maximal values your stage
                int paneMaxX = screenMaxX - width;
                int paneMaxY = screenMaxY - height;

                //Check if the position of your stage is not out of screen
                if (stageMaxX > paneMaxX || stageMaxY > paneMaxY) {
                    // Set stage where ever you want
                    // future
                }
            }
        });
        Optional<FindResults> optional = findStarInViewDialog.showAndWait();
        if (optional.isPresent()) {
            FindResults findResults = optional.get();
            if (findResults.isSelected()) {

                log.info("Value chose = {}", findResults.getRecord());
                interstellarSpacePane.highlightStar(findResults.getRecord().getRecordId());
            }
        }
    }

    public void runQuery(ActionEvent actionEvent) {
        queryDialog.setOnShown(new EventHandler<DialogEvent>() {
            @Override
            public void handle(DialogEvent event) {

                //Values from screen
                int screenMaxX = (int) Screen.getPrimary().getVisualBounds().getMaxX();
                int screenMaxY = (int) Screen.getPrimary().getVisualBounds().getMaxY();

                //Values from stage
                int width = (int) stage.getWidth();
                int height = (int) stage.getHeight();
                int stageMaxX = (int) stage.getX();
                int stageMaxY = (int) stage.getY();

                //Maximal values your stage
                int paneMaxX = screenMaxX - width;
                int paneMaxY = screenMaxY - height;

                //Check if the position of your stage is not out of screen
                if (stageMaxX > paneMaxX || stageMaxY > paneMaxY) {
                    // Set stage where ever you want
                    // future
                }
            }
        });
        queryDialog.show();
    }

    public void distanceReport(ActionEvent actionEvent) {
        List<StarDisplayRecord> starsInView = interstellarSpacePane.getCurrentStarsInView();
        if (starsInView.size() > 0) {
            SelectStarForDistanceReportDialog selectDialog = new SelectStarForDistanceReportDialog(stage, starsInView);
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

    public void runAnimation(ActionEvent actionEvent) {
        interstellarSpacePane.toggleAnimation();
    }

    public void simulate(ActionEvent actionEvent) {
        interstellarSpacePane.simulateStars(40);
        updateStatus("simulating 40 stars");
    }


    /////////////////////////////  LISTENERS  ///////////////////////////////

    @Override
    public void selectInterstellarSpace(Map<String, String> objectProperties) {
        log.info("Showing interstellar Space");
        interstellarSpacePane.toFront();
        updateStatus("Selected Interstellar space");
    }

    @Override
    public void selectSolarSystemSpace(@NotNull StarDisplayRecord starDisplayRecord) {
        log.info("Showing a solar system");
        solarSystemSpacePane.setSystemToDisplay(starDisplayRecord);
        solarSystemSpacePane.render();
        solarSystemSpacePane.toFront();
        updateStatus("Selected Solarsystem space: " + starDisplayRecord.getStarName());
    }

    @Override
    public List<AstrographicObject> getAstrographicObjectsOnQuery() {
        return databaseManagementService.getAstrographicObjectsOnQuery(searchContext);
    }

    @Override
    public void updateStar(@NotNull AstrographicObject astrographicObject) {
        databaseManagementService.updateStar(astrographicObject);
    }

    @Override
    public void updateNotesForStar(@NotNull UUID recordId, String notes) {
        databaseManagementService.updateNotesOnStar(recordId, notes);
    }

    @Override
    public AstrographicObject getStar(@NotNull UUID starId) {
        return databaseManagementService.getStar(starId);
    }

    @Override
    public void removeStar(@NotNull AstrographicObject astrographicObject) {
        databaseManagementService.removeStar(astrographicObject);
    }

    @Override
    public void removeStar(@NotNull UUID recordId) {
        databaseManagementService.removeStar(recordId);
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
    public void recenter(@NotNull StarDisplayRecord starId) {
        log.info("recenter plot at {}", starId);
        AstroSearchQuery query = searchContext.getAstroSearchQuery();
        query.setCenterRanging(starId, query.getUpperDistanceLimit());
        log.info("New Center Range: {}", query.getCenterRangingCube());
        showNewStellarData(query, true, false);
    }

    @Override
    public void highlightStar(UUID starId) {
        interstellarSpacePane.highlightStar(starId);
    }

    @Override
    public void generateDistanceReport(StarDisplayRecord starDescriptor) {
        List<StarDisplayRecord> starsInView = interstellarSpacePane.getCurrentStarsInView();
        if (starsInView.size() > 0) {
            SelectStarForDistanceReportDialog selectDialog = new SelectStarForDistanceReportDialog(stage, starsInView);
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
    public void newRoute(@NotNull DataSetDescriptor dataSetDescriptor, @NotNull RouteDescriptor routeDescriptor) {
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

    @Override
    public void displayStellarProperties(@Nullable AstrographicObject astrographicObject) {
        if (astrographicObject != null) {
            starPropertiesPane.setStar(astrographicObject);
            propertiesAccordion.setExpandedPane(stellarObjectPane);
        }
    }

    @Override
    public void updateStatus(String message) {
        databaseStatus.setText(message);
    }

    /**
     * redisplay data based on the selected filter criteria
     *
     * @param searchQuery the search query to use
     * @param showPlot    show a graphical plot
     * @param showTable   show a table
     */
    @Override
    public void showNewStellarData(@NotNull AstroSearchQuery searchQuery, boolean showPlot, boolean showTable) {
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
    public void showNewStellarData(@NotNull DataSetDescriptor dataSetDescriptor, boolean showPlot, boolean showTable) {
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
    public void doExport(AstroSearchQuery newQuery) {
        DataSetDescriptor descriptor = newQuery.getDescriptor();
        List<AstrographicObject> astrographicObjects = getAstrographicObjectsOnQuery();
        if (astrographicObjects.isEmpty()) {
            showErrorAlert("Astrographic data view error", "No Astrographic data was loaded ");
            return;
        }
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select export file to import");
        File filesFolder = new File(localization.getFileDirectory());
        if (!filesFolder.exists()) {
            boolean created = filesFolder.mkdirs();
            if (!created) {
                log.error("data files folder did not exist, but attempt to create directories failed");
                showErrorAlert("Add Dataset ", "files folder did not exist, but attempt to create directories failed");
            }
        }
        fileChooser.setInitialDirectory(filesFolder);
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            DataSetDescriptor modDescriptor = databaseManagementService.recheckDescriptor(descriptor, astrographicObjects);
            ExportOptions exportOptions =
                    ExportOptions
                            .builder()
                            .doExport(true)
                            .dataset(modDescriptor)
                            .fileName(file.getAbsolutePath())
                            .exportFormat(ExportFileType.EXCEL)
                            .build();
            dataExportService.exportDatasetOnQuery(exportOptions, astrographicObjects);
        } else {
            log.warn("file save cancelled");
        }
    }

    private void showList(@NotNull List<AstrographicObject> astrographicObjects) {
        if (astrographicObjects.size() > 0) {
            new DataSetTable(databaseManagementService, astrographicObjects);
        } else {
            showErrorAlert("Display Data table", "no data to show");
        }
    }

    @Override
    public void addDataSet(@NotNull DataSetDescriptor dataSetDescriptor) {
        searchContext.addDataSet(dataSetDescriptor);
        addDataSetToList(new ArrayList<>(searchContext.getDatasetMap().values()), true);
        queryDialog.updateDataContext(dataSetDescriptor);
        updateStatus("Dataset: " + dataSetDescriptor.getDataSetName() + " loaded");
    }

    @Override
    public void removeDataSet(@NotNull DataSetDescriptor dataSetDescriptor) {
        Optional<ButtonType> buttonType = showConfirmationAlert("Remove Dataset",
                "Remove",
                "Are you sure you want to remove: " + dataSetDescriptor.getDataSetName());

        if ((buttonType.isPresent()) && (buttonType.get() == ButtonType.OK)) {
            searchContext.removeDataSet(dataSetDescriptor);

            // remove from database
            databaseManagementService.removeDataSet(dataSetDescriptor);

            // redisplay the datasets
            addDataSetToList(new ArrayList<>(searchContext.getDatasetMap().values()), true);
            updateStatus("Dataset: " + dataSetDescriptor.getDataSetName() + " removed");
        }
    }

    @Override
    public void setContextDataSet(@NotNull DataSetDescriptor descriptor) {
        tripsContext.getDataSetContext().setDescriptor(descriptor);
        tripsContext.getDataSetContext().setValidDescriptor(true);
        tripsContext.getSearchContext().getAstroSearchQuery().setDescriptor(descriptor);
        tripsContext.getSearchContext().setCurrentDataSet(descriptor.getDataSetName());
        dataSetsListView.getSelectionModel().select(descriptor);
        queryDialog.setDataSetContext(descriptor);
        interstellarSpacePane.setDataSetContext(descriptor);

        plotButton.setDisable(false);
        toolBar.setTooltip(new Tooltip(null));

        updateStatus("You are looking at the stars in " + descriptor.getDataSetName() + " dataset.  ");
    }

    @Override
    public void updateGraphColors(@NotNull ColorPalette colorPalette) {
        tripsContext.getAppViewPreferences().setColorPallete(colorPalette);

        // colors changes so update db
        databaseManagementService.updateColors(colorPalette);
        interstellarSpacePane.changeColors(colorPalette);
        log.debug("UPDATE COLORS!!!");
    }

    @Override
    public void changesGraphEnables(@NotNull GraphEnablesPersist graphEnablesPersist) {
        tripsContext.getAppViewPreferences().setGraphEnablesPersist(graphEnablesPersist);

        updateToggles(graphEnablesPersist);

        databaseManagementService.updateGraphEnables(graphEnablesPersist);
        astrographicPlotter.changeGraphEnables(graphEnablesPersist);
        log.debug("UPDATE GRAPH ENABLES!!!");
    }

    /**
     * update the graph toggles
     *
     * @param graphEnablesPersist the graph
     */
    private void updateToggles(@NotNull GraphEnablesPersist graphEnablesPersist) {
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

    @Override
    public void changeStarPreferences(@NotNull StarDisplayPreferences starDisplayPreferences) {
        databaseManagementService.updateStarPreferences(starDisplayPreferences);
    }

    @Override
    public void changePolitiesPreferences(@NotNull CivilizationDisplayPreferences civilizationDisplayPreferences) {
        databaseManagementService.updateCivilizationDisplayPreferences(civilizationDisplayPreferences);
    }


    ///////////////////////

    /////////////////////  DISPLAY DATA   ///////////////////////////

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

    private void drawStars(@NotNull DataSetDescriptor dataSetDescriptor) {
        List<AstrographicObject> astrographicObjects = databaseManagementService.getFromDatasetWithinLimit(
                dataSetDescriptor,
                searchContext.getAstroSearchQuery().getUpperDistanceLimit());
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
            if (astrographicObjects.size() > 0) {
                new DataSetTable(databaseManagementService, astrographicObjects);
            } else {
                showErrorAlert("Show Data Table", "no data found");
            }
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
                if (astrographicObjects.size() > 0) {
                    new DataSetTable(databaseManagementService, astrographicObjects);
                    updateStatus("Dataset table loaded is: " + dataSetDescriptor.getDataSetName());
                } else {
                    showErrorAlert("Show Data Table", "No data to show");
                }

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
    private DataSetDescriptor findFromDataSet(String selected, @NotNull List<DataSetDescriptor> datasets) {
        return datasets.stream().filter(dataSetDescriptor -> dataSetDescriptor.getDataSetName().equals(selected)).findFirst().orElse(null);
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
        if (queryDialog != null) {
            queryDialog.close();
        }
        // close the spring context which invokes all the bean destroy methods
        SpringApplication.exit(appContext, () -> returnCode);
        // now exit the application
        System.exit(returnCode);
    }

    public void findInDatabase(ActionEvent actionEvent) {
        List<String> datasetNames = searchContext.getDataSetNames();
        if (datasetNames.isEmpty()) {
            showErrorAlert("Find stars", "No datasets in database, please load first");
            return;
        }
        FindStarsWithNameMatchDialog findStarsWithNameMatchDialog = new FindStarsWithNameMatchDialog(datasetNames);
        Optional<StarSearchResults> optional = findStarsWithNameMatchDialog.showAndWait();
        if (optional.isPresent()) {
            StarSearchResults starSearchResults = optional.get();
            if (starSearchResults.isStarsFound()) {
                String datasetName = starSearchResults.getDataSetName();
                String starName = starSearchResults.getNameToSearch();
                log.info("name to search: {}", starSearchResults.getNameToSearch());
                List<AstrographicObject> astrographicObjects = databaseManagementService.findStarsWithName(datasetName, starName);
                log.info("number of stars found ={}", astrographicObjects.size());
                ShowStarMatchesDialog showStarMatchesDialog = new ShowStarMatchesDialog(databaseManagementService, astrographicObjects);
                showStarMatchesDialog.showAndWait();
            }
        }
    }


    public void copyDatabase(ActionEvent actionEvent) {
        showInfoMessage("Copy Database", "not ready yet, coming soon");
    }

    public void advancedSearch(ActionEvent actionEvent) {
        AdvancedQueryDialog advancedQueryDialog = new AdvancedQueryDialog(databaseManagementService, searchContext.getDatasetMap());
        Optional<AdvResultsSet> optional = advancedQueryDialog.showAndWait();
        if (optional.isPresent()) {
            AdvResultsSet advResultsSet = optional.get();
            if (!advResultsSet.isDismissed()) {
                if (advResultsSet.isResultsFound()) {
                    showList(advResultsSet.getStarsFound());
                    astrographicPlotter.drawAstrographicData(advResultsSet.getDataSetDescriptor(),
                            advResultsSet.getStarsFound(),
                            searchContext.getAstroSearchQuery().getCenterCoordinates(),
                            tripsContext.getAppViewPreferences().getColorPallete(),
                            tripsContext.getAppViewPreferences().getStarDisplayPreferences(),
                            tripsContext.getAppViewPreferences().getCivilizationDisplayPreferences()
                    );
                } else {
                    showInfoMessage("Advanced Query", "No stars were found to match query");
                }
            }
        }

    }


}
