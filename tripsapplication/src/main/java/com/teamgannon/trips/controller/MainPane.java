package com.teamgannon.trips.controller;

import com.teamgannon.trips.algorithms.Universe;
import com.teamgannon.trips.config.application.Localization;
import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.config.application.model.ApplicationPreferences;
import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.config.application.model.StarDisplayPreferences;
import com.teamgannon.trips.controller.shared.SharedUIFunctions;
import com.teamgannon.trips.controller.shared.SharedUIState;
import com.teamgannon.trips.controller.statusbar.StatusBarController;
import com.teamgannon.trips.dialogs.AboutDialog;
import com.teamgannon.trips.dialogs.dataset.DataSetManagerDialog;
import com.teamgannon.trips.dialogs.dataset.SelectActiveDatasetDialog;
import com.teamgannon.trips.dialogs.gaiadata.Load10ParsecStarsDialog;
import com.teamgannon.trips.dialogs.gaiadata.Load10ParsecStarsResults;
import com.teamgannon.trips.dialogs.inventory.InventoryReport;
import com.teamgannon.trips.dialogs.preferences.ViewPreferencesDialog;
import com.teamgannon.trips.dialogs.query.AdvResultsSet;
import com.teamgannon.trips.dialogs.query.AdvancedQueryDialog;
import com.teamgannon.trips.dialogs.query.QueryDialog;
import com.teamgannon.trips.dialogs.search.*;
import com.teamgannon.trips.dialogs.search.model.*;
import com.teamgannon.trips.dialogs.sesame.SesameNameResolverDialog;
import com.teamgannon.trips.dialogs.startup.EachTimeStartDialog;
import com.teamgannon.trips.dialogs.startup.FirstStartDialog;
import com.teamgannon.trips.dialogs.utility.EquatorialToGalacticCoordsDialog;
import com.teamgannon.trips.dialogs.utility.FindDistanceDialog;
import com.teamgannon.trips.dialogs.utility.RADecToXYZDialog;
import com.teamgannon.trips.events.*;
import com.teamgannon.trips.graphics.PlotManager;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.graphics.panes.InterstellarSpacePane;
import com.teamgannon.trips.javafxsupport.FxThread;
import com.teamgannon.trips.jpa.model.*;
import com.teamgannon.trips.measure.OshiMeasure;
import com.teamgannon.trips.report.ReportManager;
import com.teamgannon.trips.report.distance.DistanceReportSelection;
import com.teamgannon.trips.report.distance.SelectStarForDistanceReportDialog;
import com.teamgannon.trips.report.route.RouteReportDialog;
import com.teamgannon.trips.routing.RouteManager;
import com.teamgannon.trips.routing.automation.RouteFinderDataset;
import com.teamgannon.trips.routing.automation.RouteFinderInView;
import com.teamgannon.trips.routing.dialogs.ContextManualRoutingDialog;
import com.teamgannon.trips.routing.model.RoutingType;
import com.teamgannon.trips.routing.sidepanel.RoutingPanel;
import com.teamgannon.trips.screenobjects.ObjectViewPane;
import com.teamgannon.trips.screenobjects.StarEditDialog;
import com.teamgannon.trips.screenobjects.StarEditStatus;
import com.teamgannon.trips.screenobjects.StarPropertiesPane;
import com.teamgannon.trips.search.SearchContext;
import com.teamgannon.trips.service.*;
import com.teamgannon.trips.service.graphsearch.LargeGraphSearchService;
import com.teamgannon.trips.service.measure.StarMeasurementService;
import com.teamgannon.trips.starplotting.StarPlotManager;
import com.teamgannon.trips.transits.FindTransitsBetweenStarsDialog;
import com.teamgannon.trips.transits.TransitDefinitions;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxWeaver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.teamgannon.trips.support.AlertFactory.*;


@Slf4j
@Component
public class MainPane  {

    private final SharedUIState sharedUIState;

    @FXML
    private StatusBarController statusBarController;

    public final static double SCREEN_PROPORTION = 0.60;

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

    /**
     * star plotter component
     */
    private PlotManager plotManager;

    /**
     * the main 3D interstellar space visualization pane
     */
    private final InterstellarSpacePane interstellarSpacePane;

    private final Localization localization;
    private final ApplicationEventPublisher eventPublisher;
    private final DataExportService dataExportService;

    ////// injected properties
    public Pane mainPanel;
    public MenuBar menuBar;

    public MenuItem showRoutesMenuitem;
    public MenuItem openDatasetMenuItem;
    public MenuItem saveMenuItem;

    public MenuItem importDataSetMenuItem;
    public MenuItem quitMenuItem;
    public Menu scriptingMenu;


    ////  local assets
    public StackPane displayPane;
    private final BooleanProperty busy = new SimpleBooleanProperty(false);
    private final StringProperty busyMessage = new SimpleStringProperty("");
    private final BooleanProperty cancelAvailable = new SimpleBooleanProperty(false);
    private final ObservableList<String> busyDetails = FXCollections.observableArrayList();
    private final Map<String, Runnable> cancelActions = new LinkedHashMap<>();
    private int busyCount = 0;
    private StackPane busyOverlay;
    private Label busyLabel;
    double sceneWidth = Universe.boxWidth;
    double sceneHeight = Universe.boxHeight;
    private final RoutingPanel routingPanel;
    private final SharedUIFunctions sharedUIFunctions;
    private final StarPropertiesPane starPropertiesPane;
    private final ObjectViewPane objectViewPane;

    /**
     * the query dialog
     */
    private QueryDialog queryDialog;

    private Stage primaryStage;
    private final OshiMeasure oshiMeasure;
    private final StarMeasurementService starMeasurementService;
    private final LargeGraphSearchService largeGraphSearchService;
    private final RouteFinderInView routeFinderInView;
    private final TripsContext tripsContext;



    private final DatasetService datasetService;
    private final SystemPreferencesService systemPreferencesService;
    private final TransitService transitService;
    private final StarService starService;
    private final BulkLoadService bulkLoadService;
    private final DataImportService dataImportService;

    // state settings for control positions

    /////// data objects ///////////
    private boolean sidePaneOn = false;

    private double originalHeight = Universe.boxHeight;
    private double originalWidth = Universe.boxWidth;

    private final HostServices hostServices;

    private final SliderControlManager sliderControlManager;

    private final MainSplitPaneManager mainSplitPaneManager;

    /**
     * constructor
     *
     * @param fxWeaver     the FX Weaver for integrating Spring boot and JavaFX
     * @param tripsContext our trips context
     */
    public MainPane(FxWeaver fxWeaver,
                    OshiMeasure oshiMeasure,
                    StarMeasurementService starMeasurementService,
                    LargeGraphSearchService largeGraphSearchService,
                    RouteFinderInView routeFinderInView,
                    TripsContext tripsContext,
                    ApplicationContext appContext,
                    DatabaseManagementService databaseManagementService,
                    DatasetService datasetService,
                    SystemPreferencesService systemPreferencesService,
                    TransitService transitService,
                    StarService starService,
                    BulkLoadService bulkLoadService,
                    DataImportService dataImportService,
                    Localization localization,
                    ApplicationEventPublisher eventPublisher,
                    RoutingPanel routingPanel,
                    ObjectViewPane objectViewPane,
                    StatusBarController statusBarController,
                    SliderControlManager sliderControlManager,
                    SharedUIFunctions sharedUIFunctions,
                    StarPropertiesPane starPropertiesPane,
                    SharedUIState sharedUIState,
                    InterstellarSpacePane interstellarSpacePane,
                    MainSplitPaneManager mainSplitPaneManager) {

        hostServices = fxWeaver.getBean(HostServices.class);
        this.oshiMeasure = oshiMeasure;
        this.starMeasurementService = starMeasurementService;
        this.largeGraphSearchService = largeGraphSearchService;
        this.routeFinderInView = routeFinderInView;

        this.tripsContext = tripsContext;
        this.datasetService = datasetService;
        this.systemPreferencesService = systemPreferencesService;
        this.transitService = transitService;
        this.starService = starService;
        this.bulkLoadService = bulkLoadService;
        this.dataImportService = dataImportService;

        this.searchContext = tripsContext.getSearchContext();
        this.appContext = appContext;

        this.databaseManagementService = databaseManagementService;
        this.localization = localization;
        this.eventPublisher = eventPublisher;
        this.routingPanel = routingPanel;
        this.objectViewPane = objectViewPane;
        this.statusBarController = statusBarController;
        this.sliderControlManager = sliderControlManager;
        this.sharedUIFunctions = sharedUIFunctions;
        this.starPropertiesPane = starPropertiesPane;
        this.interstellarSpacePane = interstellarSpacePane;
        this.mainSplitPaneManager = mainSplitPaneManager;

        this.dataExportService = new DataExportService(databaseManagementService, starService, eventPublisher);
        this.sharedUIState = sharedUIState;
    }



    @FXML
    public void initialize() {
        log.info("initialize view");

        setMnemonics();

        this.plotManager = new PlotManager(tripsContext, starService, eventPublisher);
        this.plotManager.setInterstellarPane(interstellarSpacePane);

        setDefaultSizesForUI();

        // get colors from DB
        getGraphColorsFromDB();

        // by default side panel should be off
        toggleSidePane(false);

        // load database preset values
        loadDBPresets();

        showBeginningAlert();
    }


    public void setStage(@NotNull Stage primaryStage, double sceneWidth, double sceneHeight, double controlPaneOffset) {
        this.primaryStage = primaryStage;
        this.sceneWidth = sceneWidth;
        this.sceneHeight = sceneHeight;

        addResizeListeners();

        primaryStage.setOnCloseRequest(event -> {
            log.info("Close request detected");
            event.consume();
            initiateShutdown(0);
        });

        interstellarSpacePane.setControlPaneOffset(controlPaneOffset);

        ChangeListener<Number> stageSizeListener = (observable, oldValue, newValue) -> resizeTrips(primaryStage.getHeight(), primaryStage.getWidth());

        primaryStage.widthProperty().addListener(stageSizeListener);
        primaryStage.heightProperty().addListener(stageSizeListener);

        queryDialog = new QueryDialog(searchContext, eventPublisher);
        queryDialog.initModality(Modality.NONE);

        try {
            File imageFileIcon = new File(localization.getProgramdata() + "tripsicon.png");
            FileInputStream fis = new FileInputStream(imageFileIcon);
            Image applicationIcon = new Image(fis);
            if (applicationIcon != null) {
                primaryStage.getIcons().add(applicationIcon);
            } else {
                log.error("Application Icon was not found!");
            }
        } catch (Exception e) {
            log.error("Caught exception: " + e.getMessage());
        }

        createAWTTray();
    }

    private void addResizeListeners() {
        if (primaryStage != null) {
            primaryStage.widthProperty().addListener((obs, oldVal, newVal) -> handleResize());
            primaryStage.heightProperty().addListener((obs, oldVal, newVal) -> handleResize());
        }
    }

    private void handleResize() {
        sharedUIFunctions.updateSidePaneOnResize();
    }

    private void setMnemonics() {
        openDatasetMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
        importDataSetMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.I, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
        quitMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));
    }

    /////////////////////////////  CREATE ASSETS  ////////////////////////////

    private void getGraphColorsFromDB() {
        ColorPalette colorPalette = systemPreferencesService.getGraphColorsFromDB();
        tripsContext.getAppViewPreferences().setColorPallete(colorPalette);
    }

    private void setDefaultSizesForUI() {
        this.mainPanel.setPrefWidth(Universe.boxWidth + 20);
        this.menuBar.setPrefWidth(Universe.boxWidth + 20);

        // Initialize the split pane via MainSplitPaneManager
        mainSplitPaneManager.initialize(sliderControlManager, plotManager);

        // Add mainSplitPane to displayPane
        this.displayPane.getChildren().add(mainSplitPaneManager.getMainSplitPane());
        setupBusyOverlay();
    }

    private void setupBusyOverlay() {
        ProgressIndicator indicator = new ProgressIndicator();
        indicator.setMaxSize(80, 80);

        busyLabel = new Label();
        busyLabel.textProperty().bind(busyMessage);
        busyLabel.setTextFill(Color.WHITE);

        ListView<String> detailsList = new ListView<>(busyDetails);
        detailsList.setMaxHeight(120);
        detailsList.setFocusTraversable(false);

        TitledPane detailsPane = new TitledPane("Details", detailsList);
        detailsPane.setExpanded(false);
        detailsPane.setMaxWidth(360);

        Button cancelButton = new Button("Cancel");
        cancelButton.visibleProperty().bind(cancelAvailable);
        cancelButton.managedProperty().bind(cancelAvailable);
        cancelButton.setOnAction(event -> cancelBusyTasks());

        VBox content = new VBox(10, indicator, busyLabel, detailsPane, cancelButton);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(400);

        busyOverlay = new StackPane(content);
        busyOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.35);");
        busyOverlay.setPickOnBounds(true);
        busyOverlay.visibleProperty().bind(busy);
        busyOverlay.managedProperty().bind(busy);

        displayPane.getChildren().add(busyOverlay);
    }

    private void cancelBusyTasks() {
        if (cancelActions.isEmpty()) {
            return;
        }
        busyMessage.set("Cancelling...");
        for (Runnable cancelAction : new ArrayList<>(cancelActions.values())) {
            cancelAction.run();
        }
    }

    public void toggleSidePane(boolean sidePanelOn) {
        this.sidePaneOn = sidePanelOn;
        sharedUIState.setSidePaneOn(sidePanelOn);
        sharedUIFunctions.applySidePaneState(sidePanelOn);
    }

    /**
     * resize trips
     *
     * @param height the height to change to
     * @param width  the width to change to
     */
    private void resizeTrips(double height, double width) {

        if (Double.isNaN(height)) {
            height = Universe.boxHeight - 10;
        } else {
            height -= 10;
        }
        log.trace("Height: " + height + " Width: " + width);

        interstellarSpacePane.resize(width, height);

        if (statusBarController != null && statusBarController.getStatusBar() != null) {
            statusBarController.getStatusBar().setPrefWidth(width);
        } else {
            log.warn("StatusBar or its controller is null during resize operation");
        }

        SplitPane mainSplitPane = mainSplitPaneManager.getMainSplitPane();
        VBox settingsPane = mainSplitPaneManager.getSettingsPane();
        StackPane leftDisplayPane = mainSplitPaneManager.getLeftDisplayPane();

        settingsPane.setPrefHeight(height - 112);
        settingsPane.setPrefWidth(260);

        leftDisplayPane.setPrefWidth(width);
        leftDisplayPane.setPrefHeight(height);

        mainSplitPane.setPrefWidth(width);
        mainSplitPane.setPrefHeight(height - 112);
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

        Platform.runLater(interstellarSpacePane::updateLabels);

    }

    /**
     * this function adjusts the slider position so that it changes based on screen
     * resize and prevents anyone from moving it with their mouse
     *
     * @param spPosition the current divider position
     */
    private void adjustSliderForSidePanel(double spPosition) {
        SplitPane mainSplitPane = mainSplitPaneManager.getMainSplitPane();
        if (!sidePaneOn) {
            // this takes into consideration that the side-pane is closed, and we
            // ensure it stays that way
            mainSplitPane.setDividerPosition(0, 1);
        } else {
            if (spPosition > .95) {
                // if the divider is all the way over, then make sure it stays there
                mainSplitPane.setDividerPosition(0, 1);
            } else {
                double currentWidth = this.mainPanel.getWidth();
                double exposedSettingsWidth = (1 - spPosition) * currentWidth;
                if (exposedSettingsWidth > SIDE_PANEL_SIZE || exposedSettingsWidth < SIDE_PANEL_SIZE) {
                    double adjustedWidthRatio = SIDE_PANEL_SIZE / currentWidth;
                    mainSplitPane.setDividerPosition(0, 1 - adjustedWidthRatio);
                }
            }
        }
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
                    systemPreferencesService.saveTripsPrefs(tripsPrefs);
                } else {
                    log.info("selected is false");
                }
            }
        }
    }


    private void createAWTTray() {
        TrayIcon trayIcon;
        if (SystemTray.isSupported()) {
            try {
                // get the SystemTray instance
                SystemTray tray = SystemTray.getSystemTray();
                // load an image
                File imageFileIcon = new File(localization.getProgramdata() + "tripsicon.png");
                java.awt.Image image = Toolkit.getDefaultToolkit().getImage(imageFileIcon.getAbsolutePath());
                // create an action listener to listen for default action executed on the tray icon
                ActionListener listener = e -> log.info("action performed");
                // create a popup menu
                PopupMenu popup = new PopupMenu();
                // create menu item for the default action
                java.awt.MenuItem defaultItem = new java.awt.MenuItem("Menu example");
                defaultItem.addActionListener(listener);
                popup.add(defaultItem);
                /// ... add other items
                // construct a TrayIcon
                trayIcon = new TrayIcon(image, "Tray Demo", popup);
                // set the TrayIcon properties
                trayIcon.addActionListener(listener);

                // add the tray image
                tray.add(trayIcon);
            } catch (Exception e) {
                log.error("failed to add tray icon:" + e.getMessage());
            }
            // ...
        } else {
            // disable tray option in your application or
            // perform other actions
            log.error("TrayIcon not supported");

        }
    }




    //////////////////////////  DATABASE STUFF  /////////

    private void getTripsPrefsFromDB() {
        TripsPrefs tripsPrefs = systemPreferencesService.getTripsPrefs();
        tripsContext.setTripsPrefs(tripsPrefs);
        String datasetName = tripsPrefs.getDatasetName();
        if (datasetName != null) {
            if (!datasetName.isEmpty()) {
                DataSetDescriptor descriptor = datasetService.getDatasetFromName(tripsPrefs.getDatasetName());
                eventPublisher.publishEvent(new SetContextDataSetEvent(this, descriptor));
                plotStars(null);
            }
        }
    }

    private void getTransitPrefs() {
        TransitSettings transitSettings = transitService.getTransitSettings();
        tripsContext.setTransitSettings(transitSettings);
    }

    private void getCivilizationsFromDB() {
        CivilizationDisplayPreferences civilizationDisplayPreferences = systemPreferencesService.getCivilizationDisplayPreferences();
        tripsContext.getAppViewPreferences().setCivilizationDisplayPreferences(civilizationDisplayPreferences);
        tripsContext.getCurrentPlot().setCivilizationDisplayPreferences(civilizationDisplayPreferences);
    }

    public void getGraphEnablesFromDB() {
        GraphEnablesPersist graphEnablesPersist = systemPreferencesService.getGraphEnablesFromDB();
        tripsContext.getAppViewPreferences().setGraphEnablesPersist(graphEnablesPersist);

        updateToggles(graphEnablesPersist);

        // set defaults
        interstellarSpacePane.setGraphPresets(graphEnablesPersist);
    }

    /**
     * get the star definitions from the db
     */
    private void getStarDefinitionsFromDB() {
        List<StarDetailsPersist> starDetailsPersistList = systemPreferencesService.getStarDetails();
        StarDisplayPreferences starDisplayPreferences = new StarDisplayPreferences();
        starDisplayPreferences.setStars(starDetailsPersistList);
        tripsContext.getAppViewPreferences().setStarDisplayPreferences(starDisplayPreferences);
        tripsContext.getCurrentPlot().setStarDisplayPreferences(starDisplayPreferences);
    }

    /////////////////////////////  UI triggers  /////////////////////


    public void plotStars(ActionEvent actionEvent) {

        if (tripsContext.getSearchContext().getDataSetDescriptor() != null) {
            plotManager.showPlot(searchContext);
        } else {
            showErrorAlert("Plot stars", "there isn't a dataset selected to plot. Please select one.");
        }
    }

    public void clearRoutes(ActionEvent actionEvent) {
        interstellarSpacePane.clearRoutes();
        datasetService.clearRoutesFromCurrent(searchContext.getDataSetDescriptor());
        routingPanel.clearData();
    }


    public void quit(ActionEvent actionEvent) {
        shutdown();
    }

    public void showApplicationPreferences(ActionEvent actionEvent) {
        ApplicationPreferences applicationPreferences = tripsContext.getAppPreferences();
        ViewPreferencesDialog viewPreferencesDialog = new ViewPreferencesDialog(tripsContext, applicationPreferences, eventPublisher);
        viewPreferencesDialog.showAndWait();
    }

    public void loadDataSetManager(ActionEvent actionEvent) {

        DataSetManagerDialog dialog = new DataSetManagerDialog(
                databaseManagementService,
                datasetService,
                eventPublisher,
                dataImportService,
                localization,
                dataExportService);

        // we throw away the result after returning
        dialog.showAndWait();
    }


    public void selectActiveDataset(ActionEvent actionEvent) {
        if (tripsContext.getDataSetContext().getDescriptor() != null) {
            SelectActiveDatasetDialog dialog = new SelectActiveDatasetDialog(
                    eventPublisher,
                    tripsContext.getDataSetContext(),
                    databaseManagementService,
                    datasetService);
            // we throw away the result after returning
            dialog.showAndWait();

        } else {
            showErrorAlert("Select a Dataset", "There are no datasets to select.");
        }
    }


    public void transitFinder(ActionEvent actionEvent) {
        FindTransitsBetweenStarsDialog findTransitsBetweenStarsDialog
                = new FindTransitsBetweenStarsDialog(datasetService,
                tripsContext.getDataSetDescriptor().getTransitDefinitions());
        Optional<TransitDefinitions> optionalTransitDefinitions = findTransitsBetweenStarsDialog.showAndWait();
        if (optionalTransitDefinitions.isPresent()) {
            TransitDefinitions transitDefinitions = optionalTransitDefinitions.get();
            if (transitDefinitions.isSelected()) {
                interstellarSpacePane.findTransits(transitDefinitions);
                mainSplitPaneManager.getTransitFilterPane().setFilter(transitDefinitions, interstellarSpacePane.getTransitManager());
            }
        }
    }

    public void clearTransits(ActionEvent actionEvent) {
        interstellarSpacePane.clearTransits();
        eventPublisher.publishEvent(new UIStateChangeEvent(this, UIElement.TRANSITS, false));
        eventPublisher.publishEvent(new UIStateChangeEvent(this, UIElement.TRANSIT_LENGTHS, false));
        mainSplitPaneManager.getTransitFilterPane().clear();
    }

    public void routeFinderInView(ActionEvent actionEvent) {
        if (interstellarSpacePane.getCurrentStarsInView().size() > 2) {
            routeFinderInView.startRouteLocation(searchContext.getDataSetDescriptor());
        } else {
            showErrorAlert("Route Finder", "You need to have more than 2 stars on a plot to use.");
        }
    }

    public void routeFinderDataset(ActionEvent actionEvent) {
        RouteFinderDataset routeFinderDataset = new RouteFinderDataset(interstellarSpacePane, largeGraphSearchService);
        routeFinderDataset.startRouteLocation(
                searchContext.getDataSetDescriptor(),
                databaseManagementService,
                starService,
                starMeasurementService,
                eventPublisher
        );

    }

    public void findInView(ActionEvent actionEvent) {
        List<StarDisplayRecord> starsInView = interstellarSpacePane.getCurrentStarsInView();
        FindStarInViewDialog findStarInViewDialog = new FindStarInViewDialog(starsInView);
        findStarInViewDialog.setOnShown(event -> {

            //Values from screen
            int screenMaxX = (int) Screen.getPrimary().getVisualBounds().getMaxX();
            int screenMaxY = (int) Screen.getPrimary().getVisualBounds().getMaxY();

            //Values from stage
            int width = (int) primaryStage.getWidth();
            int height = (int) primaryStage.getHeight();
            int stageMaxX = (int) primaryStage.getX();
            int stageMaxY = (int) primaryStage.getY();

            //Maximal values your stage
            int paneMaxX = screenMaxX - width;
            int paneMaxY = screenMaxY - height;

            //Check if the position of your stage is not out of screen
            if (stageMaxX > paneMaxX || stageMaxY > paneMaxY) {
                // Set the stage where ever you want
                // future
            }
        });
        Optional<FindResults> optional = findStarInViewDialog.showAndWait();
        if (optional.isPresent()) {
            FindResults findResults = optional.get();
            if (findResults.isSelected()) {

                log.info("Value chose = {}", findResults.getRecord());

                String recordId = findResults.getRecord().getRecordId();
                eventPublisher.publishEvent(new HighlightStarEvent(this, recordId));
                StarObject starObject = starService.getStar(recordId);
                eventPublisher.publishEvent(new DisplayStarEvent(this, starObject));
//                displayStellarProperties(starObject);
            }
        }
    }

    public void runQuery(ActionEvent actionEvent) {
        if (tripsContext.getSearchContext().getDatasetMap().isEmpty()) {
            log.error("There aren't any datasets so don't show");
            showErrorAlert("Search Query", "There aren't any datasets to search on.\nPlease import one first");
        } else {
            queryDialog.setOnShown(event -> {

                //Values from screen
                int screenMaxX = (int) Screen.getPrimary().getVisualBounds().getMaxX();
                int screenMaxY = (int) Screen.getPrimary().getVisualBounds().getMaxY();

                //Values from stage
                int width = (int) primaryStage.getWidth();
                int height = (int) primaryStage.getHeight();
                int stageMaxX = (int) primaryStage.getX();
                int stageMaxY = (int) primaryStage.getY();

                //Maximal values your stage
                int paneMaxX = screenMaxX - width;
                int paneMaxY = screenMaxY - height;

                //Check if the position of your stage is not out of screen
                if (stageMaxX > paneMaxX || stageMaxY > paneMaxY) {
                    // Set the stage where ever you want
                    // future
                }
            });
            queryDialog.show();
        }
    }

    public void distanceReport(ActionEvent actionEvent) {
        List<StarDisplayRecord> starsInView = interstellarSpacePane.getCurrentStarsInView();
        if (!starsInView.isEmpty()) {
            SelectStarForDistanceReportDialog selectDialog = new SelectStarForDistanceReportDialog(starsInView);
            Optional<DistanceReportSelection> optionalStarDisplayRecord = selectDialog.showAndWait();
            if (optionalStarDisplayRecord.isPresent()) {
                DistanceReportSelection reportSelection = optionalStarDisplayRecord.get();
                if (reportSelection.isSelected()) {
                    eventPublisher.publishEvent(new DistanceReportEvent(this, reportSelection.getRecord()));
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

    public void runAnimation(ActionEvent actionEvent) {
        interstellarSpacePane.toggleAnimation();
    }

    public void simulate(ActionEvent actionEvent) {
        Optional<ButtonType> btnType = showConfirmationAlert("Simulate", "Simulate", "This function generates an array of “simulated” stars to learn the program and test functions without having to load a dataset. If you have loaded a dataset you probably don’t want to do this.");
        if (btnType.isPresent()) {
            if (btnType.get().equals(ButtonType.OK)) {
                interstellarSpacePane.simulateStars(40);
                eventPublisher.publishEvent(new StatusUpdateEvent(this, "simulating 40 stars"));
            }
        }
    }


    /////////////////////////////  LISTENERS  ///////////////////////////////



    public void clearList() {
        eventPublisher.publishEvent(new ClearListEvent(this));
    }



    @EventListener
    public void onGraphEnablesPersistEvent(GraphEnablesPersistEvent event) {
        updateToggles(event.getGraphEnablesPersist());
    }

    /**
     * update the graph toggles
     *
     * @param graphEnablesPersist the graph
     */
    private void updateToggles(@NotNull GraphEnablesPersist graphEnablesPersist) {
        eventPublisher.publishEvent(new UIStateChangeEvent(this, UIElement.GRID, graphEnablesPersist.isDisplayGrid()));
        eventPublisher.publishEvent(new UIStateChangeEvent(this, UIElement.POLITIES, graphEnablesPersist.isDisplayPolities()));
        eventPublisher.publishEvent(new UIStateChangeEvent(this, UIElement.EXTENSIONS, graphEnablesPersist.isDisplayStems()));
        eventPublisher.publishEvent(new UIStateChangeEvent(this, UIElement.SCALE, graphEnablesPersist.isDisplayLegend()));
        eventPublisher.publishEvent(new UIStateChangeEvent(this, UIElement.ROUTES, graphEnablesPersist.isDisplayRoutes()));
        eventPublisher.publishEvent(new UIStateChangeEvent(this, UIElement.TOOLBAR, graphEnablesPersist.isDisplayRoutes()));
        eventPublisher.publishEvent(new UIStateChangeEvent(this, UIElement.STATUS_BAR, graphEnablesPersist.isDisplayRoutes()));
    }


    /////////////////////////  Shutdown   /////////////////////////

    private void shutdown() {
        log.debug("Exit selection");
        Optional<ButtonType> result = showConfirmationAlert(
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
        int exitCode = SpringApplication.exit(appContext, () -> returnCode);
        // now exit the application
        Platform.exit();
        System.exit(exitCode);
    }

    public void findInDataset(ActionEvent actionEvent) {
        List<String> datasetNames = searchContext.getDataSetNames();
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
                ShowStarMatchesDialog showStarMatchesDialog = new ShowStarMatchesDialog(databaseManagementService, starService, starObjects);
                showStarMatchesDialog.showAndWait();
            }
        }
    }

    public void advancedSearch(ActionEvent actionEvent) {
        if (tripsContext.getSearchContext().getDatasetMap().isEmpty()) {
            log.error("There are no datasets in this database to search on");
            showErrorAlert("Query Stars", "There aren't any datasets to search on.\nPlease import one first");
        } else {
            AdvancedQueryDialog advancedQueryDialog = new AdvancedQueryDialog(databaseManagementService, starService, tripsContext);
            Optional<AdvResultsSet> optional = advancedQueryDialog.showAndWait();
            if (optional.isPresent()) {
                AdvResultsSet advResultsSet = optional.get();
                if (!advResultsSet.isDismissed()) {
                    if (advResultsSet.isResultsFound()) {
                        if (advResultsSet.isViewStars()) {
                            mainSplitPaneManager.showList(advResultsSet.getStarsFound());
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
        File file = saveFileChooser.showSaveDialog(primaryStage);
        if (file != null) {
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
            } catch (IOException e) {
                log.error("unable to save the snapshot file:{}", e.getMessage());
            }
        }
    }

    public void saveDataset(ActionEvent actionEvent) {
        log.info("Save requested");
    }

    public void editStar(ActionEvent actionEvent) {
        List<StarDisplayRecord> starsInView = interstellarSpacePane.getCurrentStarsInView();
        FindStarInViewDialog findStarInViewDialog = new FindStarInViewDialog(starsInView);
        Optional<FindResults> optional = findStarInViewDialog.showAndWait();
        if (optional.isPresent()) {
            FindResults findResults = optional.get();
            if (findResults.isSelected()) {
                StarDisplayRecord record = findResults.getRecord();
                StarObject starObject = starService.getStar(record.getRecordId());
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
    }

    public void showRoutes(ActionEvent actionEvent) {
        toggleSidePane(true);
        mainSplitPaneManager.getPropertiesAccordion().setExpandedPane(mainSplitPaneManager.getRoutingPane());
    }

    public void editDeleteRoutes(ActionEvent actionEvent) {
        showWarningMessage("Edit/Delete Routes", "THis function isn't implmented yet");
    }

    public void clickRoutes(ActionEvent actionEvent) {
        RouteManager routeManager = interstellarSpacePane.getRouteManager();
        ContextManualRoutingDialog manualRoutingDialog = new ContextManualRoutingDialog(
                routeManager,
                tripsContext.getDataSetDescriptor(),
                interstellarSpacePane.getCurrentStarsInView()
        );
        manualRoutingDialog.initModality(Modality.NONE);
        manualRoutingDialog.show();

        StarPlotManager starPlotManager = interstellarSpacePane.getStarPlotManager();
        starPlotManager.setManualRouting(manualRoutingDialog);

        // set the state for the routing so that clicks on stars don't invoke the context menu
        routeManager.setManualRoutingActive(true);
        routeManager.setRoutingType(RoutingType.MANUAL);
    }

    public void routeListReport(ActionEvent actionEvent) {
        List<DataSetDescriptor> dataSetDescriptorList = datasetService.getDataSets();
        RouteReportDialog dialog = new RouteReportDialog(tripsContext.getDataSetDescriptor(), dataSetDescriptorList);
        dialog.showAndWait();
    }

    public void starPropertyReport(ActionEvent actionEvent) {
        showWarningMessage("Star Property Report", "This function hasn't been implemented.");
    }

    public void getInventory(ActionEvent actionEvent) {
        String physicalInventory = oshiMeasure.getComputerInventory();
        InventoryReport inventoryReport = new InventoryReport(physicalInventory);
        ReportManager reportManager = new ReportManager();
        reportManager.generateComputerInventoryReport(primaryStage, inventoryReport);
    }

    /**
     * find by catalog id
     *
     * @param actionEvent an event we don't use.
     */
    public void findByCatalogId(ActionEvent actionEvent) {
        log.info("find a star by catalog id");
        List<String> datasetNames = searchContext.getDataSetNames();
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
                ShowStarMatchesDialog showStarMatchesDialog = new ShowStarMatchesDialog(databaseManagementService, starService, starObjects);
                showStarMatchesDialog.showAndWait();
            }
        }
    }

    public void findByCommonName(ActionEvent actionEvent) {
        log.info("find a star by common name");
        List<String> datasetNames = searchContext.getDataSetNames();
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
                ShowStarMatchesDialog showStarMatchesDialog = new ShowStarMatchesDialog(databaseManagementService, starService, starObjects);
                showStarMatchesDialog.showAndWait();
            }
        }
    }

    public void findByConstellation(ActionEvent actionEvent) {
        FindAllByConstellationDialog dialog = new FindAllByConstellationDialog(tripsContext);
        Optional<ConstellationSelected> optional = dialog.showAndWait();
        if (optional.isPresent()) {
            ConstellationSelected selected = optional.get();
            if (selected.isSelected()) {
                List<StarObject> starObjectList = starService.findStarsByConstellation(
                        tripsContext.getDataSetDescriptor().getDataSetName(),
                        selected.getConstellation());
                ShowStarMatchesDialog showStarMatchesDialog = new ShowStarMatchesDialog(databaseManagementService, starService, starObjectList);
                showStarMatchesDialog.showAndWait();
            }
        }
    }

    public void findDistance(ActionEvent actionEvent) {
        List<String> datasetNames = searchContext.getDataSetNames();
        if (datasetNames.isEmpty()) {
            showErrorAlert("Find stars", "No datasets in database, please load first");
            return;
        }
        FindDistanceDialog dialog = new FindDistanceDialog(datasetNames, tripsContext.getSearchContext().getDataSetDescriptor(), databaseManagementService, starService);
        dialog.showAndWait();
    }

    public void findXYZ(ActionEvent actionEvent) {
        RADecToXYZDialog dialog = new RADecToXYZDialog();
        dialog.showAndWait();
    }

    public void findGalacticCoords(ActionEvent actionEvent) {
        EquatorialToGalacticCoordsDialog dialog = new EquatorialToGalacticCoordsDialog();
        dialog.showAndWait();
    }

    public void findInSesame(ActionEvent actionEvent) {
        SesameNameResolverDialog dialog = new SesameNameResolverDialog();
        Optional<List<String>> resultOpt = dialog.showAndWait();
    }

    /**
     * find stars related to the selected star
     *
     * @param actionEvent an event we don't use.
     */
    public void findRelatedStars(ActionEvent actionEvent) {
        List<String> datasetNames = searchContext.getDataSetNames();
        if (datasetNames.isEmpty()) {
            showErrorAlert("Find stars", "No datasets in database, please load first");
            return;
        }
        FindRelatedStarsbyDistance findRelatedStarsbyDistanceDialog = new FindRelatedStarsbyDistance(databaseManagementService, starService, datasetNames, tripsContext.getSearchContext().getDataSetDescriptor());
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
                File selectedFile = fileChooser.showSaveDialog(primaryStage);

                if (selectedFile != null) {
                    try (FileWriter writer = new FileWriter(selectedFile)) {
                        writer.write(report);
                    } catch (IOException ex) {
                        System.err.println("An error occurred while saving the file: " + ex.getMessage());
                    }
                }

            }
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
     * find stars related to the selected star
     *
     * @param actionEvent an event we don't use.
     */
    public void Load10ParsecStars(ActionEvent actionEvent) {
        log.info("Load 10 parsec stars");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a File containing the 10 parsec stars volume of space");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Data Files", "*.dat"));

        File selectedFile = fileChooser.showOpenDialog(primaryStage);

        if (selectedFile != null) {
            List<DataSetDescriptor> dataSetDescriptorList = datasetService.getDataSets();
            if (!dataSetDescriptorList.isEmpty()) {
                Load10ParsecStarsDialog dialog = new Load10ParsecStarsDialog(selectedFile, databaseManagementService, starService, datasetService);
                Optional<Load10ParsecStarsResults> optional = dialog.showAndWait();
                if (optional.isPresent()) {
                    Load10ParsecStarsResults results = optional.get();
                    if (results.isStarsLoaded()) {
                        log.info("stars loaded");
                    }
                }
            } else {
                showErrorAlert("Load 10 parsec stars", "There are no datasets in this database!");
            }
        }


    }

    public void FindCatalogId(ActionEvent actionEvent) {
        log.info("Find catalog id");
        List<String> datasetNames = searchContext.getDataSetNames();
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
    }

    ////////////////////////////////////////////////////


    public void displayStellarProperties(@Nullable StarObject starObject) {
        if (starObject != null) {
            toggleSidePane(true);
            mainSplitPaneManager.getPropertiesAccordion().setExpandedPane(mainSplitPaneManager.getStellarObjectPane());
            if (!sidePaneOn) {
                toggleSidePane(true);
            }
        }
    }


    @EventListener
    public void onDistanceReportEvent(DistanceReportEvent event) {
        Platform.runLater(() -> {
            StarDisplayRecord starDescriptor = event.getStarDisplayRecord();
            ReportManager reportManager = new ReportManager();
            reportManager.generateDistanceReport(primaryStage, starDescriptor, interstellarSpacePane.getCurrentStarsInView());
        });
    }

    @EventListener
    public void onDisplayStarEvent(DisplayStarEvent event) {
        Platform.runLater(() -> {
            log.info("MAIN PANE ::: Received display star event, star is:{}", event.getStarObject().getDisplayName());
            displayStellarProperties(event.getStarObject());
        });
    }

    @EventListener
    public void onBusyStateEvent(BusyStateEvent event) {
        FxThread.runOnFxThread(() -> {
            if (event.isBusy()) {
                busyCount++;
                if (event.getTaskId() != null && event.getCancelAction() != null) {
                    cancelActions.put(event.getTaskId(), event.getCancelAction());
                }
                if (event.getMessage() != null) {
                    busyMessage.set(event.getMessage());
                }
            } else {
                if (event.getTaskId() != null) {
                    cancelActions.remove(event.getTaskId());
                }
                busyCount = Math.max(0, busyCount - 1);
                if (busyCount == 0) {
                    busyMessage.set("");
                }
            }
            cancelAvailable.set(!cancelActions.isEmpty());
            busy.set(busyCount > 0);
        });
    }

    @EventListener
    public void onStatusUpdateEvent(StatusUpdateEvent event) {
        FxThread.runOnFxThread(() -> {
            busyDetails.add(0, event.getStatus());
            while (busyDetails.size() > 6) {
                busyDetails.remove(busyDetails.size() - 1);
            }
        });
    }

}
