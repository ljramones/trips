package com.teamgannon.trips.controller;

import com.teamgannon.trips.algorithms.Universe;
import com.teamgannon.trips.config.application.Localization;
import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.config.application.model.StarDisplayPreferences;
import com.teamgannon.trips.controller.shared.SharedUIFunctions;
import com.teamgannon.trips.controller.shared.SharedUIState;
import com.teamgannon.trips.controller.statusbar.StatusBarController;
import com.teamgannon.trips.dialogs.gaiadata.Load10ParsecStarsDialog;
import com.teamgannon.trips.dialogs.gaiadata.Load10ParsecStarsResults;
import com.teamgannon.trips.dialogs.query.QueryDialog;
import com.teamgannon.trips.dialogs.startup.EachTimeStartDialog;
import com.teamgannon.trips.dialogs.startup.FirstStartDialog;
import com.teamgannon.trips.events.*;
import com.teamgannon.trips.graphics.PlotManager;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.graphics.panes.InterstellarSpacePane;
import com.teamgannon.trips.javafxsupport.FxThread;
import com.teamgannon.trips.jpa.model.*;
import com.teamgannon.trips.report.ReportManager;
import com.teamgannon.trips.search.SearchContext;
import com.teamgannon.trips.service.*;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
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

import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
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

    ////// injected properties
    public Pane mainPanel;
    public MenuBar menuBar;

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
    private final SharedUIFunctions sharedUIFunctions;

    /**
     * the query dialog
     */
    private QueryDialog queryDialog;

    private Stage primaryStage;
    private final TripsContext tripsContext;

    private final DatasetService datasetService;
    private final SystemPreferencesService systemPreferencesService;
    private final TransitService transitService;
    private final StarService starService;

    // state settings for control positions

    /////// data objects ///////////
    private boolean sidePaneOn = false;

    private double originalHeight = Universe.boxHeight;
    private double originalWidth = Universe.boxWidth;

    private final FxWeaver fxWeaver;

    private final SliderControlManager sliderControlManager;

    private final MainSplitPaneManager mainSplitPaneManager;

    /**
     * Constructor for MainPane.
     *
     * @param fxWeaver                   the FX Weaver for integrating Spring boot and JavaFX
     * @param tripsContext               our trips context
     * @param appContext                 the Spring application context
     * @param databaseManagementService  database management service
     * @param datasetService             dataset service
     * @param systemPreferencesService   system preferences service
     * @param transitService             transit service
     * @param starService                star service
     * @param localization               localization settings
     * @param eventPublisher             event publisher
     * @param statusBarController        status bar controller
     * @param sliderControlManager       slider control manager
     * @param sharedUIFunctions          shared UI functions
     * @param sharedUIState              shared UI state
     * @param interstellarSpacePane      the 3D visualization pane
     * @param mainSplitPaneManager       main split pane manager
     */
    public MainPane(FxWeaver fxWeaver,
                    TripsContext tripsContext,
                    ApplicationContext appContext,
                    DatabaseManagementService databaseManagementService,
                    DatasetService datasetService,
                    SystemPreferencesService systemPreferencesService,
                    TransitService transitService,
                    StarService starService,
                    Localization localization,
                    ApplicationEventPublisher eventPublisher,
                    StatusBarController statusBarController,
                    SliderControlManager sliderControlManager,
                    SharedUIFunctions sharedUIFunctions,
                    SharedUIState sharedUIState,
                    InterstellarSpacePane interstellarSpacePane,
                    MainSplitPaneManager mainSplitPaneManager) {

        this.fxWeaver = fxWeaver;
        this.tripsContext = tripsContext;
        this.searchContext = tripsContext.getSearchContext();
        this.appContext = appContext;
        this.databaseManagementService = databaseManagementService;
        this.datasetService = datasetService;
        this.systemPreferencesService = systemPreferencesService;
        this.transitService = transitService;
        this.starService = starService;
        this.localization = localization;
        this.eventPublisher = eventPublisher;
        this.statusBarController = statusBarController;
        this.sliderControlManager = sliderControlManager;
        this.sharedUIFunctions = sharedUIFunctions;
        this.sharedUIState = sharedUIState;
        this.interstellarSpacePane = interstellarSpacePane;
        this.mainSplitPaneManager = mainSplitPaneManager;
    }



    @FXML
    public void initialize() {
        log.info("initialize view");

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

    public void openDataWorkbench(ActionEvent actionEvent) {
        Parent root = fxWeaver.loadView(com.teamgannon.trips.workbench.DataWorkbenchController.class);
        Stage stage = new Stage();
        stage.setTitle("TRIPS Data Workbench");
        stage.initModality(Modality.NONE);
        if (primaryStage != null) {
            stage.initOwner(primaryStage);
        }
        stage.setScene(new Scene(root, 900, 650));
        stage.show();
    }

    @EventListener
    public void onOpenWorkbenchEvent(com.teamgannon.trips.events.OpenWorkbenchEvent event) {
        openDataWorkbench(null);
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

    /////////////////////////////  CREATE ASSETS  ////////////////////////////

    private void getGraphColorsFromDB() {
        ColorPalette colorPalette = systemPreferencesService.getGraphColorsFromDB();
        tripsContext.getAppViewPreferences().setColorPalette(colorPalette);
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

        FxThread.runOnFxThread(interstellarSpacePane::updateLabels);

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
        if (!tripsPrefs.isSkipStartupDialog()) {
            EachTimeStartDialog eachTimeStartDialog = new EachTimeStartDialog();
            Optional<Boolean> optStart = eachTimeStartDialog.showAndWait();
            if (optStart.isPresent()) {
                boolean skipNextTime = optStart.get();
                if (skipNextTime) {
                    log.info("User selected to skip startup dialog in future");
                    tripsPrefs.setSkipStartupDialog(true);
                    systemPreferencesService.saveTripsPrefs(tripsPrefs);
                } else {
                    log.info("Startup dialog will continue to show");
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

    public void rotate(ActionEvent actionEvent) {
        RotationDialog rotationDialog = new RotationDialog(interstellarSpacePane);
        rotationDialog.initModality(Modality.NONE);
        rotationDialog.show();
    }

    /**
     * Load 10 parsec stars from a data file.
     *
     * @param actionEvent the action event (unused)
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
        FxThread.runOnFxThread(() -> {
            StarDisplayRecord starDescriptor = event.getStarDisplayRecord();
            ReportManager reportManager = new ReportManager();
            reportManager.generateDistanceReport(primaryStage, starDescriptor, interstellarSpacePane.getCurrentStarsInView());
        });
    }

    @EventListener
    public void onDisplayStarEvent(DisplayStarEvent event) {
        FxThread.runOnFxThread(() -> {
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
