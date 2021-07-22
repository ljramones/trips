package com.teamgannon.trips.controller;

import com.teamgannon.trips.algorithms.Universe;
import com.teamgannon.trips.config.application.*;
import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.dataset.model.DataSetDescriptorCellFactory;
import com.teamgannon.trips.dialogs.AboutDialog;
import com.teamgannon.trips.dialogs.ExportQueryDialog;
import com.teamgannon.trips.dialogs.dataset.DataSetManagerDialog;
import com.teamgannon.trips.dialogs.dataset.SelectActiveDatasetDialog;
import com.teamgannon.trips.dialogs.preferences.ViewPreferencesDialog;
import com.teamgannon.trips.dialogs.query.AdvResultsSet;
import com.teamgannon.trips.dialogs.query.AdvancedQueryDialog;
import com.teamgannon.trips.dialogs.query.QueryDialog;
import com.teamgannon.trips.dialogs.search.FindStarInViewDialog;
import com.teamgannon.trips.dialogs.search.FindStarsWithNameMatchDialog;
import com.teamgannon.trips.dialogs.search.ShowStarMatchesDialog;
import com.teamgannon.trips.dialogs.search.model.FindResults;
import com.teamgannon.trips.dialogs.search.model.StarSearchResults;
import com.teamgannon.trips.dialogs.startup.EachTimeStartDialog;
import com.teamgannon.trips.dialogs.startup.FirstStartDialog;
import com.teamgannon.trips.graphics.PlotManager;
import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.graphics.panes.InterstellarSpacePane;
import com.teamgannon.trips.graphics.panes.SolarSystemSpacePane;
import com.teamgannon.trips.jpa.model.*;
import com.teamgannon.trips.listener.*;
import com.teamgannon.trips.report.ReportManager;
import com.teamgannon.trips.report.distance.DistanceReportSelection;
import com.teamgannon.trips.report.distance.SelectStarForDistanceReportDialog;
import com.teamgannon.trips.report.route.RouteReportDialog;
import com.teamgannon.trips.routing.*;
import com.teamgannon.trips.routing.dialogs.ContextManualRoutingDialog;
import com.teamgannon.trips.routing.sidepanel.RoutingPanel;
import com.teamgannon.trips.screenobjects.ObjectViewPane;
import com.teamgannon.trips.screenobjects.StarEditDialog;
import com.teamgannon.trips.screenobjects.StarEditStatus;
import com.teamgannon.trips.screenobjects.StarPropertiesPane;
import com.teamgannon.trips.search.AstroSearchQuery;
import com.teamgannon.trips.search.SearchContext;
import com.teamgannon.trips.service.DataExportService;
import com.teamgannon.trips.service.DataImportService;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.starplotting.StarPlotManager;
import com.teamgannon.trips.support.AlertFactory;
import com.teamgannon.trips.tableviews.DataSetTable;
import com.teamgannon.trips.transits.FindTransitsBetweenStarsDialog;
import com.teamgannon.trips.transits.TransitDefinitions;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
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
import org.springframework.util.ResourceUtils;

import javax.imageio.ImageIO;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
        StatusUpdaterListener,
        RoutingPanelListener {

    private final static double SCREEN_PROPORTION = 0.60;

    public final static double SIDE_PANEL_SIZE = 350;

    /**
     * the current search context to display from
     */
    private final SearchContext searchContext;

    /**
     * the TRIPS application context
     */
    private final ApplicationContext appContext;

    /**
     * database management spring component service
     */
    private final DatabaseManagementService databaseManagementService;
    public CheckMenuItem toggleRouteLengthsMenuitem;
    public MenuItem showRoutesMenuitem;
    public MenuItem openDatasetMenuItem;
    public MenuItem saveMenuItem;
    public MenuItem saveAsMenuItem;
    public MenuItem exportDataSetMenuItem;
    public MenuItem importDataSetMenuItem;
    public MenuItem quitMenuItem;

    /**
     * star plotter component
     */
    private PlotManager plotManager;

    private final Localization localization;
    private final DataExportService dataExportService;

    /**
     * dataset lists
     */
    private final ListView<DataSetDescriptor> dataSetsListView = new ListView<>();

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

    ////  local assets
    public SplitPane mainSplitPane;
    public HBox statusBar;
    public Label databaseStatus;
    public Label routingStatus;
    public Button plotButton;
    public VBox displayPane;
    public Accordion propertiesAccordion;
    public TitledPane stellarObjectPane;
    public BorderPane leftBorderPane;
    public BorderPane rightBorderPane;
    public TitledPane objectsViewPane;
    public TitledPane transitPane;
    public TitledPane routingPane;
    double sceneWidth = Universe.boxWidth;
    double sceneHeight = Universe.boxHeight;
    double depth = Universe.boxDepth;
    double spacing = 20;
    private TitledPane datasetsPane;
    private RoutingPanel routingPanel;
    private TransitFilterPane transitFilterPane;
    private StarPropertiesPane starPropertiesPane;
    private ObjectViewPane objectViewPane;
    private VBox settingsPane;
    private StackPane leftDisplayPane;

    /**
     * the query dialog
     */
    private QueryDialog queryDialog;

    private Stage stage;
    private final FxWeaver fxWeaver;
    private final TripsContext tripsContext;
    /**
     * interstellar space
     */
    private InterstellarSpacePane interstellarSpacePane;
    /**
     * solar system panes for showing the details of various solar systems
     */
    private SolarSystemSpacePane solarSystemSpacePane;
    private DataImportService dataImportService;
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
    private boolean routesLengthsOn = true;
    private boolean transitsOn = true;
    private boolean transitsLengthsOn = true;
    private boolean helpModeOn = true;

    /////// data objects ///////////
    private boolean sidePaneOn = false;
    private boolean toolBarOn = true;
    private boolean statusBarOn = true;

    private double originalHeight = Universe.boxHeight;
    private double originalWidth = Universe.boxWidth;

    private final HostServices hostServices;

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
                    Localization localization
    ) {

        this.fxWeaver = fxWeaver;
        hostServices = fxWeaver.getBean(HostServices.class);

        this.tripsContext = tripsContext;
        this.searchContext = tripsContext.getSearchContext();
        this.appContext = appContext;

        this.databaseManagementService = databaseManagementService;
        this.localization = localization;

        this.dataExportService = new DataExportService(databaseManagementService, this);

    }

    @FXML
    public void initialize() {
        log.info("initialize view");

        setMnemonics();

        this.plotManager = new PlotManager(tripsContext, databaseManagementService,
                this, this, this);

        setButtons();

        setDefaultSizesForUI();

        setButtonFonts();

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

        loadConstellationFile();

    }

    private void setMnemonics() {
        openDatasetMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
        saveMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        saveAsMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
        exportDataSetMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
        importDataSetMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.I, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
        quitMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));
    }

    /**
     * load the constellation file
     */
    private void loadConstellationFile() {
        try {
            File constellationFile = ResourceUtils.getFile("classpath:files/constellation.csv");
            BufferedReader reader = new BufferedReader(new FileReader(constellationFile));

            boolean readComplete = false;
            String line = reader.readLine();
            do {
                String[] parts = line.split(",");
                Constellation constellation = Constellation
                        .builder()
                        .name(parts[0])
                        .iauAbbr(parts[1])
                        .nasaAbbr(parts[2])
                        .brightestStar(parts[4])
                        .build();

                tripsContext.getConstellationSet().getConstellationList().add(constellation);

                line = reader.readLine();
                if (line == null) {
                    readComplete = true;
                }
            } while (!readComplete); // the moment readComplete turns true, we stop

            // setup
            tripsContext.getConstellationSet().setup();

            log.info("loaded constellation file");

        } catch (IOException e) {
            log.error("cannot load constellation file:" + e.getMessage());
        }
    }

    private void setButtonFonts() {
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

        this.mainSplitPane = new SplitPane();
        this.mainSplitPane.setDividerPositions(1.0);
        this.mainSplitPane.setPrefWidth(Universe.boxWidth);

        this.displayPane.getChildren().add(mainSplitPane);
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

        this.leftBorderPane.setPrefWidth(Universe.boxWidth * 0.6);

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
        settingsPane.setPrefWidth(SIDE_PANEL_SIZE);

        rightBorderPane.setRight(settingsPane);

        propertiesAccordion = new Accordion();
        settingsPane.getChildren().add(propertiesAccordion);

        // datasets pane
        datasetsPane = new TitledPane();
        datasetsPane.setText("DataSets Available");
        datasetsPane.setMinWidth(SIDE_PANEL_SIZE);
        datasetsPane.setMinHeight(200);
        datasetsPane.setMaxHeight(500);
        propertiesAccordion.getPanes().add(datasetsPane);

        // objects in pane
        objectsViewPane = new TitledPane();
        objectsViewPane.setText("Objects in View");
        objectsViewPane.setMinWidth(SIDE_PANEL_SIZE);
        objectsViewPane.setMinHeight(200);
        objectsViewPane.setMaxHeight(460);
        propertiesAccordion.getPanes().add(objectsViewPane);

        // stellar pane
        stellarObjectPane = new TitledPane();
        stellarObjectPane.setText("Stellar Object Properties");
        stellarObjectPane.setPrefWidth(SIDE_PANEL_SIZE);
        stellarObjectPane.setPrefHeight(500);
        stellarObjectPane.setMaxHeight(520);
        starPropertiesPane = new StarPropertiesPane(databaseManagementService, hostServices);
        ScrollPane scrollPane = new ScrollPane(starPropertiesPane);
        stellarObjectPane.setContent(scrollPane);
        propertiesAccordion.getPanes().add(stellarObjectPane);

        transitPane = new TitledPane();
        transitPane.setText("Transit Control");
        transitPane.setPrefWidth(SIDE_PANEL_SIZE);
        transitPane.setPrefHeight(500);
        transitPane.setMaxHeight(520);
        transitFilterPane = new TransitFilterPane();
        ScrollPane scrollPane2 = new ScrollPane(transitFilterPane);
        transitPane.setContent(scrollPane2);
        propertiesAccordion.getPanes().add(transitPane);

        // routing pane
        routingPane = new TitledPane();
        routingPane.setText("Star Routing");
        routingPane.setMinWidth(SIDE_PANEL_SIZE);
        routingPane.setMinHeight(400);
        routingPane.setMaxHeight(400);
        routingPanel = new RoutingPanel(this);
        routingPane.setContent(routingPanel);
        propertiesAccordion.getPanes().add(routingPane);
    }

    /**
     * set the slider controls
     */
    private void setSliderControl() {
        DoubleProperty splitPaneDividerPosition = mainSplitPane.getDividers().get(0).positionProperty();
        splitPaneDividerPosition.addListener((obs, oldPos, newPos) -> {
                    // this listener is invoked each time the slider is changed
                    // we look at our confiuration and see if its allowed and them change it if the
                    // operation is not
                    double pos = newPos.doubleValue();
                    toggleSettings.setSelected(pos < 0.95);
                    adjustSliderForSidePanel(pos);
                }
        );
    }

    /**
     * sets up list view for stellar objects
     */
    private void setupStellarObjectListView() {

        ScrollPane scrollPane = new ScrollPane();

        objectViewPane = new ObjectViewPane(
                this,
                this,
                this,
                this,
                this);

        scrollPane.setContent(objectViewPane);

        // setup model to display in case we turn on
        objectsViewPane.setContent(scrollPane);

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
            mainSplitPane.setDividerPositions(SCREEN_PROPORTION);
            interstellarSpacePane.shiftDisplayLeft(true);
        } else {
            mainSplitPane.setDividerPositions(1.0);
            interstellarSpacePane.shiftDisplayLeft(false);
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

        // get transit prefs
        getTransitPrefs();
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

        // now make sure that the divider does not go past the settings panel position
        adjustSliderForSidePanel(spPosition);

        double originalDims = Math.sqrt(originalHeight * originalWidth);
        double newDims = Math.sqrt(height * width);

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

    /**
     * this function adjusts the slider position so that it changes based on screen
     * resize and prevents anyone from moving it with their mouse
     *
     * @param spPosition the current divider position
     */
    private void adjustSliderForSidePanel(double spPosition) {
        if (!sidePaneOn) {
            // this takes into consideration that the side-pane is closed and we
            // ensure it stays that way
            mainSplitPane.setDividerPosition(0, 1);
        } else {
            if (spPosition > .95) {
                // if the divider is all the way over then make sure it stays there
                mainSplitPane.setDividerPosition(0, 1);
            } else {
                double currentWidth = this.mainPanel.getWidth();
                double exposedSettingsWidth = (1 - spPosition) * currentWidth;
//                log.info("currentWidth={}, exposedSetting={}", currentWidth, exposedSettingsWidth);
                if (exposedSettingsWidth > SIDE_PANEL_SIZE || exposedSettingsWidth < SIDE_PANEL_SIZE) {
                    double adjustedWidthRatio = SIDE_PANEL_SIZE / currentWidth;
                    mainSplitPane.setDividerPosition(0, 1 - adjustedWidthRatio);
                }
            }
        }
    }


    //////////////////////////  DATABASE STUFF  /////////


    private void getTripsPrefsFromDB() {
        TripsPrefs tripsPrefs = databaseManagementService.getTripsPrefs();
        tripsContext.setTripsPrefs(tripsPrefs);
        String datasetName = tripsPrefs.getDatasetName();
        if (datasetName != null) {
            if (!datasetName.isEmpty()) {
                DataSetDescriptor descriptor = databaseManagementService.getDatasetFromName(tripsPrefs.getDatasetName());
                this.setContextDataSet(descriptor);
                plotStars(null);
            }
        }
    }

    private void getTransitPrefs() {
        TransitSettings transitSettings = databaseManagementService.getTransitSettings();
        tripsContext.setTransitSettings(transitSettings);
    }

    private void getCivilizationsFromDB() {
        CivilizationDisplayPreferences civilizationDisplayPreferences = databaseManagementService.getCivilizationDisplayPreferences();
        tripsContext.getAppViewPreferences().setCivilizationDisplayPreferences(civilizationDisplayPreferences);
        tripsContext.getCurrentPlot().setCivilizationDisplayPreferences(civilizationDisplayPreferences);
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
        tripsContext.getCurrentPlot().setStarDisplayPreferences(starDisplayPreferences);
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
        plotManager.setInterstellarPane(interstellarSpacePane);

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
//        clearData();
        plotManager.showPlot(searchContext);
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
        databaseManagementService.clearRoutesFromCurrent(searchContext.getAstroSearchQuery().getDescriptor());
        routingPanel.clearData();
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
        if (this.starsOn) {
            this.polities = !polities;
            tripsContext.getAppViewPreferences().getGraphEnablesPersist().setDisplayPolities(polities);
            interstellarSpacePane.togglePolities(polities);
            togglePolitiesMenuitem.setSelected(polities);
            togglePolityBtn.setSelected(polities);
        }
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
        if (starsOn) {
            polities = false;
            interstellarSpacePane.togglePolities(polities);
        }
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

    public void toggleHelpMode(ActionEvent actionEvent) {
        helpModeOn = !helpModeOn;
        statusBar.setVisible(!statusBar.isVisible());
//        toggleStatusBarMenuitem.setSelected(statusBarOn);
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


    public void selectActiveDataset(ActionEvent actionEvent) {
        SelectActiveDatasetDialog dialog = new SelectActiveDatasetDialog(
                this,
                tripsContext.getDataSetContext(),
                databaseManagementService);
        // we throw away the result after returning
        dialog.showAndWait();
    }


    public void exportDatabase(ActionEvent actionEvent) {
        dataExportService.exportDB();
    }

    public void transitFinder(ActionEvent actionEvent) {
        FindTransitsBetweenStarsDialog findTransitsBetweenStarsDialog = new FindTransitsBetweenStarsDialog(databaseManagementService, tripsContext.getDataSetContext().getDescriptor().getTransitDefinitions());
        Optional<TransitDefinitions> optionalTransitDefinitions = findTransitsBetweenStarsDialog.showAndWait();
        if (optionalTransitDefinitions.isPresent()) {
            TransitDefinitions transitDefinitions = optionalTransitDefinitions.get();
            if (transitDefinitions.isSelected()) {
                interstellarSpacePane.findTransits(transitDefinitions);
                transitFilterPane.setFilter(transitDefinitions, interstellarSpacePane.getTransitManager());
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
            routeFinderInView.startRouteLocation(searchContext.getAstroSearchQuery().getDescriptor());
        } else {
            showErrorAlert("Route Finder", "You need to have more than 2 stars on a plot to use.");
        }
    }

    public void routeFinderDataset(ActionEvent actionEvent) {
        RouteFinderDataset routeFinderDataset = new RouteFinderDataset(interstellarSpacePane);
        routeFinderDataset.startRouteLocation(
                searchContext.getAstroSearchQuery().getDescriptor(),
                databaseManagementService,
                this
        );

    }

    public void toggleRoutes(ActionEvent actionEvent) {
        this.routesOn = !routesOn;
        tripsContext.getAppViewPreferences().getGraphEnablesPersist().setDisplayRoutes(routesOn);
        interstellarSpacePane.toggleRoutes(routesOn);
        toggleRoutesMenuitem.setSelected(routesOn);
        toggleRoutesBtn.setSelected(routesOn);
    }

    public void toggleRouteLengths(ActionEvent actionEvent) {
        this.routesLengthsOn = !routesLengthsOn;
        tripsContext.getAppViewPreferences().getGraphEnablesPersist().setDisplayRoutes(routesLengthsOn);
        interstellarSpacePane.toggleRouteLengths(routesLengthsOn);
        toggleRouteLengthsMenuitem.setSelected(routesLengthsOn);
    }

    public void findInView(ActionEvent actionEvent) {
        List<StarDisplayRecord> starsInView = interstellarSpacePane.getCurrentStarsInView();
        FindStarInViewDialog findStarInViewDialog = new FindStarInViewDialog(starsInView);
        findStarInViewDialog.setOnShown(event -> {

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
        });
        Optional<FindResults> optional = findStarInViewDialog.showAndWait();
        if (optional.isPresent()) {
            FindResults findResults = optional.get();
            if (findResults.isSelected()) {

                log.info("Value chose = {}", findResults.getRecord());

                UUID recordId = findResults.getRecord().getRecordId();
                interstellarSpacePane.highlightStar(recordId);
                StarObject starObject = databaseManagementService.getStar(recordId);
                displayStellarProperties(starObject);
            }
        }
    }

    public void runQuery(ActionEvent actionEvent) {
        queryDialog.setOnShown(event -> {

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
        });
        queryDialog.show();
    }

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

    public void aboutTrips(ActionEvent actionEvent) {
        AboutDialog aboutDialog = new AboutDialog(localization);
        aboutDialog.showAndWait();
    }

    public void howToSupport(ActionEvent actionEvent) {
        hostServices.showDocument("https://github.com/ljramones/trips/wiki");
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
        if (tripsContext.isShowWarningOnZoom()) {
            ShowZoomWarning showZoomWarning = new ShowZoomWarning();
            Optional<Boolean> optionalBoolean = showZoomWarning.showAndWait();
            if (optionalBoolean.isPresent()) {
                Boolean dontShowAgain = optionalBoolean.get();
                if (dontShowAgain) {
                    tripsContext.setShowWarningOnZoom(false);
                }
            }
        }
        interstellarSpacePane.zoomIn();
    }

    /**
     * zoom out on the plot by a standard amount
     *
     * @param actionEvent the specific action event
     */
    public void zoomOut(ActionEvent actionEvent) {
        if (tripsContext.isShowWarningOnZoom()) {
            ShowZoomWarning showZoomWarning = new ShowZoomWarning();
            Optional<Boolean> optionalBoolean = showZoomWarning.showAndWait();
            if (optionalBoolean.isPresent()) {
                Boolean dontShowAgain = optionalBoolean.get();
                if (dontShowAgain) {
                    tripsContext.setShowWarningOnZoom(false);
                }
            }
        }
        interstellarSpacePane.zoomOut();
    }

    public void runAnimation(ActionEvent actionEvent) {
        interstellarSpacePane.toggleAnimation();
    }

    public void simulate(ActionEvent actionEvent) {
        Optional<ButtonType> btnType = showConfirmationAlert("Simulate", "Simulate", "This function generates an array of “simulated” stars to learn the program and test functions without having to load a dataset. If you have loaded a dataset you probably don’t want to do this.");
        if (btnType.isPresent()) {
            if (btnType.get().equals(ButtonType.OK)) {
                interstellarSpacePane.simulateStars(40);
                updateStatus("simulating 40 stars");
            }
        }
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
    public List<StarObject> getAstrographicObjectsOnQuery() {
        return databaseManagementService.getAstrographicObjectsOnQuery(searchContext);
    }

    @Override
    public void updateStar(@NotNull StarObject starObject) {
        databaseManagementService.updateStar(starObject);
    }

    @Override
    public void updateNotesForStar(@NotNull UUID recordId, String notes) {
        databaseManagementService.updateNotesOnStar(recordId, notes);
    }

    @Override
    public StarObject getStar(@NotNull UUID starId) {
        return databaseManagementService.getStar(starId);
    }

    @Override
    public void removeStar(@NotNull StarObject starObject) {
        databaseManagementService.removeStar(starObject);
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
        ReportManager reportManager = new ReportManager();
        reportManager.generateDistanceReport(stage, starDescriptor, interstellarSpacePane.getCurrentStarsInView());
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
        routingPanel.setContext(dataSetDescriptor, plotManager.willRoutesShow(dataSetDescriptor));
        routingStatus(false);
    }

    @Override
    public void updateRoute(RouteDescriptor routeDescriptor) {
        log.info("update route");
        String datasetName = searchContext.getAstroSearchQuery().getDescriptor().getDataSetName();
        DataSetDescriptor descriptor = databaseManagementService.updateRoute(datasetName, routeDescriptor);
        searchContext.getAstroSearchQuery().setDescriptor(descriptor);
        routingPanel.setContext(descriptor, plotManager.willRoutesShow(descriptor));
        interstellarSpacePane.redrawRoutes(descriptor.getRoutes());
    }

    @Override
    public void displayRoute(RouteDescriptor routeDescriptor, boolean state) {
        interstellarSpacePane.displayRoute(routeDescriptor, state);
    }

    @Override
    public void deleteRoute(RouteDescriptor routeDescriptor) {
        log.info("delete route");
        DataSetDescriptor descriptor = searchContext.getAstroSearchQuery().getDescriptor();
        descriptor = databaseManagementService.deleteRoute(descriptor.getDataSetName(), routeDescriptor);
        searchContext.getAstroSearchQuery().setDescriptor(descriptor);
        routingPanel.setContext(descriptor, plotManager.willRoutesShow(descriptor));
        interstellarSpacePane.redrawRoutes(descriptor.getRoutes());
    }

    @Override
    public void displayStellarProperties(@Nullable StarObject starObject) {
        if (starObject != null) {
            toggleSidePane(true);
            starPropertiesPane.setStar(starObject);
            propertiesAccordion.setExpandedPane(stellarObjectPane);
            if (!sidePaneOn) {
                toggleSidePane(null);
            }
        }
    }

    @Override
    public void clearData() {
        starPropertiesPane.clearData();
        routingPanel.clearData();
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
        setContextDataSet(descriptor);

        if (showPlot || showTable) {
            // get the distance range
            double displayRadius = searchQuery.getUpperDistanceLimit();

            // do a search and cause the plot to show it
            List<StarObject> starObjects = getAstrographicObjectsOnQuery();

            if (!starObjects.isEmpty()) {
                if (showPlot) {
                    plotManager.drawAstrographicData(descriptor,
                            starObjects,
                            displayRadius,
                            searchQuery.getCenterCoordinates(),
                            tripsContext.getAppViewPreferences().getColorPallete(),
                            tripsContext.getAppViewPreferences().getStarDisplayPreferences(),
                            tripsContext.getAppViewPreferences().getCivilizationDisplayPreferences()
                    );

                    //
                    routingPanel.setContext(descriptor, plotManager.willRoutesShow(descriptor));
                }
                if (showTable) {
                    showList(starObjects);
                }
                updateStatus("Dataset loaded is: " + descriptor.getDataSetName());

            } else {
                showErrorAlert("Astrographic data view error", "No Astrographic data was loaded ");
            }
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

        // do a search and cause the plot to show it
        List<StarObject> starObjects = getAstrographicObjectsOnQuery();

        if (!starObjects.isEmpty()) {
            // get the distance range
            double displayRadius = searchQuery.getUpperDistanceLimit();

            if (showPlot) {
                plotManager.drawAstrographicData(searchQuery.getDescriptor(),
                        starObjects,
                        displayRadius,
                        searchQuery.getCenterCoordinates(),
                        tripsContext.getAppViewPreferences().getColorPallete(),
                        tripsContext.getAppViewPreferences().getStarDisplayPreferences(),
                        tripsContext.getAppViewPreferences().getCivilizationDisplayPreferences()
                );
                DataSetDescriptor descriptor = searchQuery.getDescriptor();
                routingPanel.setContext(descriptor, plotManager.willRoutesShow(descriptor));
            }
            if (showTable) {
                showList(starObjects);
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
        ExportQueryDialog exportQueryDialog = new ExportQueryDialog(searchContext, databaseManagementService,
                dataExportService, this, localization);
        exportQueryDialog.showAndWait();
    }

    private void showList(@NotNull List<StarObject> starObjects) {
        if (starObjects.size() > 0) {
            new DataSetTable(databaseManagementService, starObjects);
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

        // clear all the current data
        clearAll();

        tripsContext.getDataSetContext().setDescriptor(descriptor);
        tripsContext.getDataSetContext().setValidDescriptor(true);
        tripsContext.getSearchContext().getAstroSearchQuery().setDescriptor(descriptor);
        tripsContext.getSearchContext().setCurrentDataSet(descriptor.getDataSetName());
        interstellarSpacePane.setDataSetContext(descriptor);
        dataSetsListView.getSelectionModel().select(descriptor);
        if (queryDialog != null) {
            queryDialog.setDataSetContext(descriptor);
        }

        plotButton.setDisable(false);
        toolBar.setTooltip(new Tooltip(null));

        updatePersistentDataSet(descriptor);

        updateStatus("You are looking at the stars in " + descriptor.getDataSetName() + " dataset.  ");
    }

    private void updatePersistentDataSet(DataSetDescriptor descriptor) {
        databaseManagementService.updateDataSet(descriptor);
    }

    public void clearAll() {
        clearData();
        clearList();
        clearInterstellar();
    }

    private void clearInterstellar() {
        interstellarSpacePane.clearAll();
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
        plotManager.changeGraphEnables(graphEnablesPersist);
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

    @Override
    public void changeUserControlsPreferences(UserControls userControls) {
        //  @todo
        log.info("changed user controls");
        interstellarSpacePane.changeUserControls(userControls);
    }


    ///////////////////////

    /////////////////////  DISPLAY DATA   ///////////////////////////


    /**
     * show the data in a spreadsheet
     */
    private void showTableData() {

        if (tripsContext.getDataSetContext().isValidDescriptor()) {
            List<StarObject> starObjects = getAstrographicObjectsOnQuery();
            if (starObjects.size() > 0) {
                new DataSetTable(databaseManagementService, starObjects);
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
            dialog.setTitle("Choose data set to display");
            dialog.setHeaderText("Select your choice - (Default display is 20 light years from Earth, use Show Stars filter to change)");

            Optional<String> result = dialog.showAndWait();

            if (result.isPresent()) {
                String selected = result.get();

                DataSetDescriptor dataSetDescriptor = findFromDataSet(selected, datasets);
                if (dataSetDescriptor == null) {
                    log.error("How the hell did this happen");
                    return;
                }
                List<StarObject> starObjects = getAstrographicObjectsOnQuery();
                if (starObjects.size() > 0) {
                    new DataSetTable(databaseManagementService, starObjects);
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

    public void findInDataset(ActionEvent actionEvent) {
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
                List<StarObject> starObjects = databaseManagementService.findStarsWithName(datasetName, starName);
                log.info("number of stars found ={}", starObjects.size());
                ShowStarMatchesDialog showStarMatchesDialog = new ShowStarMatchesDialog(databaseManagementService, starObjects);
                showStarMatchesDialog.showAndWait();
            }
        }
    }


    public void copyDatabase(ActionEvent actionEvent) {
        showInfoMessage("Copy Database", "not ready yet, coming soon");
    }

    public void advancedSearch(ActionEvent actionEvent) {
        AdvancedQueryDialog advancedQueryDialog = new AdvancedQueryDialog(databaseManagementService, tripsContext.getDataSetContext(), searchContext.getDatasetMap());
        Optional<AdvResultsSet> optional = advancedQueryDialog.showAndWait();
        if (optional.isPresent()) {
            AdvResultsSet advResultsSet = optional.get();
            if (!advResultsSet.isDismissed()) {
                if (advResultsSet.isResultsFound()) {
                    if (advResultsSet.isViewStars()) {
                        showList(advResultsSet.getStarsFound());
                    }
                    if (advResultsSet.isPlotStars()) {
                        // get the distance range - in this case, pick a default @todo check this
                        double displayRadius = searchContext.getAstroSearchQuery().getUpperDistanceLimit();

                        plotManager.drawAstrographicData(advResultsSet.getDataSetDescriptor(),
                                advResultsSet.getStarsFound(),
                                displayRadius,
                                searchContext.getAstroSearchQuery().getCenterCoordinates(),
                                tripsContext.getAppViewPreferences().getColorPallete(),
                                tripsContext.getAppViewPreferences().getStarDisplayPreferences(),
                                tripsContext.getAppViewPreferences().getCivilizationDisplayPreferences()
                        );
                    }
                } else {
                    showInfoMessage("Advanced Query", "No stars were found to match query");
                }
            }
        }

    }

    @Override
    public void updateRoutingPanel(DataSetDescriptor dataSetDescriptor) {
        routingPanel.setContext(dataSetDescriptor, plotManager.willRoutesShow(dataSetDescriptor));
    }

    public void rotate(ActionEvent actionEvent) {
        RotationDialog rotationDialog = new RotationDialog(interstellarSpacePane);
        rotationDialog.initModality(Modality.NONE);
        rotationDialog.show();
    }

    public void onSnapShot(ActionEvent actionEvent) {
        WritableImage image = interstellarSpacePane.snapshot(new SnapshotParameters(), null);

        FileChooser saveFileChooser = new FileChooser();
        saveFileChooser.setTitle("Save Plot Snapshot");
        FileChooser.ExtensionFilter extFilter =
                new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png");
        saveFileChooser.getExtensionFilters().add(extFilter);
        saveFileChooser.setInitialDirectory(new File("."));
        File file = saveFileChooser.showSaveDialog(stage);
        if (file != null) {
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
            } catch (IOException e) {
                log.error("unable to save the snapshot file:" + e.getMessage());
            }
        }
    }

    public void saveDataset(ActionEvent actionEvent) {
        log.info("Save requested");
    }

    public void saveAsDataset(ActionEvent actionEvent) {
        showWarningMessage("Save As", "This function is not yet implemented");
    }

    public void exportDataset(ActionEvent actionEvent) {
        showWarningMessage("Export dataset", "This function is not yet implemented");
    }

    public void editStar(ActionEvent actionEvent) {
        List<StarDisplayRecord> starsInView = interstellarSpacePane.getCurrentStarsInView();
        FindStarInViewDialog findStarInViewDialog = new FindStarInViewDialog(starsInView);
        Optional<FindResults> optional = findStarInViewDialog.showAndWait();
        if (optional.isPresent()) {
            FindResults findResults = optional.get();
            StarDisplayRecord record = findResults.getRecord();
            StarObject starObject = databaseManagementService.getStar(record.getRecordId());
            StarEditDialog starEditDialog = new StarEditDialog(starObject);

            Optional<StarEditStatus> statusOptional = starEditDialog.showAndWait();
            if (statusOptional.isPresent()) {
                StarEditStatus starEditStatus = statusOptional.get();
                if (starEditStatus.isChanged()) {
                    // update the database
                    databaseManagementService.updateStar(starEditStatus.getRecord());
                }
            }

        }
    }


    public void showRoutes(ActionEvent actionEvent) {
        toggleSidePane(null);
        propertiesAccordion.setExpandedPane(routingPane);
    }

    public void editDeleteRoutes(ActionEvent actionEvent) {
        showWarningMessage("Edit/Delete Routes", "THis function isn't implmented yet");
    }

    public void clickRoutes(ActionEvent actionEvent) {
        RouteManager routeManager = interstellarSpacePane.getRouteManager();
        ContextManualRoutingDialog manualRoutingDialog = new ContextManualRoutingDialog(
                routeManager,
                tripsContext.getDataSetContext().getDescriptor(),
                interstellarSpacePane.getCurrentStarsInView()
        );
        manualRoutingDialog.initModality(Modality.NONE);
        manualRoutingDialog.show();

        StarPlotManager starPlotManager = interstellarSpacePane.getStarPlotManager();
        starPlotManager.setManualRouting(manualRoutingDialog);

        // set the state for the routing so that clicks on stars don't invoke the context menu
        routeManager.setRoutingActive(true);
        routeManager.setRoutingType(RoutingType.MANUAL);
    }

    public void routeListReport(ActionEvent actionEvent) {
        List<DataSetDescriptor> dataSetDescriptorList = databaseManagementService.getDataSets();
        RouteReportDialog dialog = new RouteReportDialog(tripsContext.getDataSetContext(), dataSetDescriptorList);
        dialog.showAndWait();
    }

    public void starPropertyReport(ActionEvent actionEvent) {
        showWarningMessage("Star Propery Report", "This function hasn't been implemented.");
    }
}
