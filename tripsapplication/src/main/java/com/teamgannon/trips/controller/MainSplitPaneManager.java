package com.teamgannon.trips.controller;

import com.teamgannon.trips.algorithms.Universe;
import com.teamgannon.trips.config.application.Localization;
import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.config.application.model.DataSetContext;
import com.teamgannon.trips.controller.shared.SharedUIFunctions;
import com.teamgannon.trips.controller.shared.SharedUIState;
import com.teamgannon.trips.controller.statusbar.StatusBarController;
import com.teamgannon.trips.dataset.model.DataSetDescriptorCellFactory;
import com.teamgannon.trips.dialogs.ExportQueryDialog;
import com.teamgannon.trips.dialogs.query.QueryDialog;
import com.teamgannon.trips.events.*;
import com.teamgannon.trips.graphics.PlotManager;
import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.graphics.panes.GalacticSpacePlane;
import com.teamgannon.trips.graphics.panes.InterstellarSpacePane;
import com.teamgannon.trips.graphics.panes.SolarSystemSpacePane;
import com.teamgannon.trips.javafxsupport.BackgroundTaskRunner;
import com.teamgannon.trips.javafxsupport.FxThread;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.listener.*;
import com.teamgannon.trips.routing.model.Route;
import com.teamgannon.trips.routing.sidepanel.RoutingPanel;
import com.teamgannon.trips.screenobjects.ObjectViewPane;
import com.teamgannon.trips.screenobjects.StarPropertiesPane;
import com.teamgannon.trips.search.AstroSearchQuery;
import com.teamgannon.trips.search.SearchContext;
import com.teamgannon.trips.service.DataExportService;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.DatasetService;
import com.teamgannon.trips.service.StarService;
import com.teamgannon.trips.tableviews.DataSetTable;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.stream.Collectors;

import static com.teamgannon.trips.support.AlertFactory.showConfirmationAlert;
import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

@Slf4j
@Component
public class MainSplitPaneManager {

    public final static double SCREEN_PROPORTION = 0.60;
    public final static double SIDE_PANEL_SIZE = 350;

    private final SharedUIFunctions sharedUIFunctions;
    private final ApplicationEventPublisher eventPublisher;
    private final StatusBarController statusBarController;
    private final TripsContext tripsContext;
    private final SearchContext searchContext;
    private PlotManager plotManager;
    private final SharedUIState sharedUIState;
    private final DataExportService dataExportService;
    private final Localization localization;
    private final DatasetService datasetService;


    private final StarPropertiesPane starPropertiesPane;
    private final RoutingPanel routingPanel;

    @Getter
    private TitledPane stellarObjectPane;
    @Getter
    private TitledPane objectsViewPane;
    @Getter
    private TitledPane transitPane;
    @Getter
    private TitledPane routingPane;
    private TitledPane datasetsPane;
    @Getter
    private Accordion propertiesAccordion;
    @Getter
    private TransitFilterPane transitFilterPane;

    /**
     * list of routes
     */
    private List<Route> routeList;

    @Getter
    private SplitPane mainSplitPane;
    private BorderPane leftBorderPane;
    @Getter
    private StackPane leftDisplayPane;
    private BorderPane rightBorderPane;
    @Getter
    private VBox settingsPane;

    private final ObjectViewPane objectViewPane;

    private final DatabaseManagementService databaseManagementService;
    /**
     * galactic space
     */
    private final GalacticSpacePlane galacticSpacePlane;

    /**
     * interstellar space
     */
    private final InterstellarSpacePane interstellarSpacePane;
    private final StarService starService;

    /**
     * the query dialog
     */
    private QueryDialog queryDialog;

    /**
     * dataset lists
     */
    private final ListView<DataSetDescriptor> dataSetsListView = new ListView<>();

    /**
     * solar system panes for showing the details of various solar systems
     */
    private final SolarSystemSpacePane solarSystemSpacePane;

    private SliderControlManager sliderControlManager;

    @Autowired
    public MainSplitPaneManager(SharedUIFunctions sharedUIFunctions,
                                ApplicationEventPublisher eventPublisher,
                                StatusBarController statusBarController,
                                TripsContext tripsContext,
                                SharedUIState sharedUIState,
                                StarPropertiesPane starPropertiesPane,
                                RoutingPanel routingPanel,
                                Localization localization,
                                ObjectViewPane objectViewPane,
                                DatabaseManagementService databaseManagementService,
                                DatasetService datasetService,
                                GalacticSpacePlane galacticSpacePlane,
                                SolarSystemSpacePane solarSystemSpacePane,
                                InterstellarSpacePane interstellarSpacePane,
                                StarService starService) {
        this.sharedUIFunctions = sharedUIFunctions;
        this.eventPublisher = eventPublisher;
        this.statusBarController = statusBarController;
        this.tripsContext = tripsContext;
        this.searchContext = tripsContext.getSearchContext();
        this.sharedUIState = sharedUIState;
        this.starPropertiesPane = starPropertiesPane;
        this.routingPanel = routingPanel;
        this.localization = localization;
        this.objectViewPane = objectViewPane;
        this.databaseManagementService = databaseManagementService;
        this.datasetService = datasetService;
        this.galacticSpacePlane = galacticSpacePlane;
        this.solarSystemSpacePane = solarSystemSpacePane;
        this.interstellarSpacePane = interstellarSpacePane;
        this.starService = starService;

        this.dataExportService = new DataExportService(databaseManagementService, starService, eventPublisher);

    }

    public void initialize(SliderControlManager sliderControlManager, PlotManager plotManager) {
        this.sliderControlManager = sliderControlManager;
        this.plotManager = plotManager;

        this.mainSplitPane = new SplitPane();
        this.mainSplitPane.setDividerPositions(1.0);
        this.mainSplitPane.setPrefWidth(Universe.boxWidth);


        // Create left and right panes
        createLeftDisplay();
        createRightDisplay();

        // create a data set pane for the database files present
        setupDataSetView();

        // create the list of objects in view
        setupStellarObjectListView();

        // Initialize the SliderControlManager
        sliderControlManager.initialize(mainSplitPane);

        // Set up the slider control
        setSliderControl(sliderControlManager);

        // Initialize SharedUIFunctions
        sharedUIFunctions.initialize(plotManager, mainSplitPane, sliderControlManager);
    }

    private void createLeftDisplay() {
        leftBorderPane = new BorderPane();
        leftBorderPane.setMinWidth(0);

        leftBorderPane.setPrefWidth(Universe.boxWidth * 0.6);

        mainSplitPane.getItems().add(leftBorderPane);

        leftDisplayPane = new StackPane();
        leftDisplayPane.setMinWidth(Universe.boxWidth + 100);
        leftDisplayPane.setPrefWidth(Universe.boxWidth);

        leftBorderPane.setLeft(leftDisplayPane);

        // create the solar system
        createSolarSystemSpace();

        // create the interstellar space
        createInterstellarSpace(tripsContext.getAppViewPreferences().getColorPallete());

        // create galactic space
        createGalacticSpace(tripsContext.getAppViewPreferences().getColorPallete());
    }

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
        ScrollPane scrollPane = new ScrollPane(starPropertiesPane);
        stellarObjectPane.setContent(scrollPane);
        propertiesAccordion.getPanes().add(stellarObjectPane);

        transitPane = new TitledPane();
        transitPane.setText("Link Control");
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
        routingPane.setContent(routingPanel);
        propertiesAccordion.getPanes().add(routingPane);
    }

    private void setSliderControl(SliderControlManager sliderControlManager) {
        DoubleProperty splitPaneDividerPosition = mainSplitPane.getDividers().get(0).positionProperty();
        splitPaneDividerPosition.addListener(sliderControlManager.getSliderChangeListener());
    }

    /**
     * Toggle the side panel visibility
     *
     * @param sidePanelOn true to show, false to hide
     */
    public void toggleSidePane(boolean sidePanelOn) {
        if (sidePanelOn) {
            mainSplitPane.setDividerPositions(SCREEN_PROPORTION);
            interstellarSpacePane.shiftDisplayLeft(true);
        } else {
            mainSplitPane.setDividerPositions(1.0);
            interstellarSpacePane.shiftDisplayLeft(false);
        }
    }

    /**
     * create the galactic plane
     *
     * @param colorPalette the Galactic Space plane
     */
    private void createGalacticSpace(ColorPalette colorPalette) {
        leftDisplayPane.getChildren().add(galacticSpacePlane);
        // force it back
        galacticSpacePlane.toBack();
    }

    /**
     * create an interstellar space drawing area
     *
     * @param colorPalette the colors to use in drawing
     */
    private void createInterstellarSpace(ColorPalette colorPalette) {

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
        leftDisplayPane.getChildren().add(solarSystemSpacePane);
        solarSystemSpacePane.toBack();
    }

    /**
     * sets up list view for stellar objects
     */
    private void setupStellarObjectListView() {

        ScrollPane scrollPane = new ScrollPane();

        scrollPane.setContent(objectViewPane);

        // setup model to display in case we turn on
        objectsViewPane.setContent(scrollPane);
    }

    private void setupDataSetView() {

        SearchContext searchContext = tripsContext.getSearchContext();
        datasetsPane.setContent(dataSetsListView);

        dataSetsListView.setPrefHeight(10);
        dataSetsListView.setCellFactory(new DataSetDescriptorCellFactory(eventPublisher));
        dataSetsListView.getSelectionModel().selectedItemProperty().addListener(this::datasetDescriptorChanged);

        loadDatasets(searchContext);
        log.info("Application up and running");
    }

    private void loadDatasets(@NotNull SearchContext searchContext) {
        // load viable datasets into search context
        List<DataSetDescriptor> dataSets = loadDataSetView();
        if (!dataSets.isEmpty()) {
            searchContext.addDataSets(dataSets);
        }
    }

    private @NotNull List<DataSetDescriptor> loadDataSetView() {

        List<DataSetDescriptor> dataSetDescriptorList = datasetService.getDataSets();

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

    public void datasetDescriptorChanged(ObservableValue<? extends DataSetDescriptor> ov, @Nullable DataSetDescriptor oldValue, @Nullable DataSetDescriptor newValue) {
        String oldText = oldValue == null ? "null" : oldValue.toString();
        String newText = newValue == null ? "null" : newValue.toString();
    }


    public void showList(@NotNull List<StarObject> starObjects) {
        if (!starObjects.isEmpty()) {
            new DataSetTable(databaseManagementService, starService, starObjects);
        } else {
            showErrorAlert("Display Data table", "no data to show");
        }
    }

    public void addDataSetToList(@NotNull List<DataSetDescriptor> list, boolean clear) {
        if (clear) {
            dataSetsListView.getItems().clear();
        }
        list.forEach(descriptor -> dataSetsListView.getItems().add(descriptor));
        log.debug("update complete");
    }

    /**
     * show the data in a spreadsheet
     */
    private void showTableData() {

        if (tripsContext.getDataSetContext().isValidDescriptor()) {
            List<StarObject> starObjects = getAstrographicObjectsOnQuery();
            if (!starObjects.isEmpty()) {
                new DataSetTable(databaseManagementService, starService, starObjects);
            } else {
                showErrorAlert("Show Data Table", "no data found");
            }
        } else {
            List<DataSetDescriptor> datasets = datasetService.getDataSets();
            if (datasets.isEmpty()) {
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
                    log.error("Dataset Descriptor is null. How the hell did this happen");
                    return;
                }
                List<StarObject> starObjects = getAstrographicObjectsOnQuery();
                if (!starObjects.isEmpty()) {
                    new DataSetTable(databaseManagementService, starService, starObjects);
                    eventPublisher.publishEvent(new StatusUpdateEvent(this, "Dataset table loaded is: " + dataSetDescriptor.getDataSetName()));
                } else {
                    showErrorAlert("Show Data Table", "No data to show");
                }

                // set current context
                eventPublisher.publishEvent(new SetContextDataSetEvent(this, dataSetDescriptor));
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


    /////////////////////////

    public List<StarObject> getAstrographicObjectsOnQuery() {
        return starService.getAstrographicObjectsOnQuery(searchContext);
    }

    public void updateStar(StarObject starObject) {
        starService.updateStar(starObject);
    }

    public void updateNotesForStar(String recordId, String notes) {
        starService.updateNotesOnStar(recordId, notes);
    }

    public StarObject getStar(String starId) {
        return starService.getStar(starId);
    }

    public void removeStar(StarObject starObject) {
        starService.removeStar(starObject);
    }

    public void removeStar(String recordId) {
        starService.removeStar(recordId);
    }

    @EventListener
    public void onRecenterStarEvent(RecenterStarEvent event) {
        StarDisplayRecord starId = event.getStarDisplayRecord();
        log.info("recenter plot at {}", starId);
        AstroSearchQuery query = searchContext.getAstroSearchQuery();
        query.setCenterRanging(starId, query.getUpperDistanceLimit());
        log.info("New Center Range: {}", query.getCenterRangingCube());
        FxThread.runOnFxThread(() -> showNewStellarData(query, true, false));
    }


    public void showNewStellarData(boolean showPlot, boolean showTable) {

    }

    /**
     * show the data
     *
     * @param dataSetDescriptor the dataset descriptor
     * @param showPlot          show the graphical plot
     * @param showTable         show the table
     */
    public void showNewStellarData(@NotNull DataSetDescriptor dataSetDescriptor, boolean showPlot, boolean showTable) {
        log.info("showing new stellar data: {}, showPlot: {}, showTable: {}", dataSetDescriptor.getDataSetName(), showPlot, showTable);
        eventPublisher.publishEvent(new SetContextDataSetEvent(this, dataSetDescriptor));
        showNewStellarData(showPlot, showTable);
    }

    public void doExport(AstroSearchQuery newQuery) {
        ExportQueryDialog exportQueryDialog = new ExportQueryDialog(searchContext, databaseManagementService,
                dataExportService, localization, eventPublisher);
        exportQueryDialog.showAndWait();

    }

    /**
     * redisplay data based on the selected filter criteria
     *
     * @param searchQuery the search query to use
     * @param showPlot    show a graphical plot
     * @param showTable   show a table
     */
    public void showNewStellarData(@NotNull AstroSearchQuery searchQuery, boolean showPlot, boolean showTable) {

        log.info(searchQuery.toString());
        searchContext.setAstroSearchQuery(searchQuery);

        DataSetDescriptor descriptor = searchQuery.getDataSetContext().getDescriptor();
        if (descriptor != null) {
            eventPublisher.publishEvent(new SetContextDataSetEvent(this, descriptor));

            if (showPlot || showTable) {
                // get the distance range
                double displayRadius = searchQuery.getUpperDistanceLimit();

                eventPublisher.publishEvent(new StatusUpdateEvent(this,
                        "Loading dataset " + descriptor.getDataSetName() + "..."));
                String taskId = createTaskId("load-dataset");

                BackgroundTaskRunner.TaskHandle taskHandle = BackgroundTaskRunner.runCancelable(
                        "trips-show-stellar-data",
                        this::getAstrographicObjectsOnQuery,
                        starObjects -> FxThread.runOnFxThread(() -> {
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

                            routingPanel.setContext(descriptor, plotManager.getRouteVisibility());
                        }
                        if (showTable) {
                            showList(starObjects);
                        }
                        eventPublisher.publishEvent(new StatusUpdateEvent(this, "Dataset loaded is: " + descriptor.getDataSetName()));
                    } else {
                        showErrorAlert("Astrographic data view error", "No Astrographic data was loaded ");
                        eventPublisher.publishEvent(new StatusUpdateEvent(this,
                                "No data returned for " + descriptor.getDataSetName()));
                    }
                        }),
                        exception -> FxThread.runOnFxThread(() -> {
                            if (isCancellation(exception)) {
                                eventPublisher.publishEvent(new StatusUpdateEvent(this,
                                        "Load cancelled for " + descriptor.getDataSetName()));
                                return;
                            }
                            String message = exception == null ? "Unknown error loading astrographic data." : exception.getMessage();
                            showErrorAlert("Astrographic data view error", message);
                            eventPublisher.publishEvent(new StatusUpdateEvent(this,
                                    "Failed to load dataset " + descriptor.getDataSetName()));
                        }),
                        () -> eventPublisher.publishEvent(new BusyStateEvent(this, taskId, false, null, null)));
                eventPublisher.publishEvent(new BusyStateEvent(this,
                        taskId,
                        true,
                        "Loading dataset " + descriptor.getDataSetName() + "...",
                        taskHandle::cancel));
            }
        } else {
            FxThread.runOnFxThread(() ->
                    showErrorAlert("Show query data", "Data descriptor is null which shouldn't happen.\nPlease select a dataset."));
        }
    }

    @EventListener
    public void onShowStellarDataEvent(ShowStellarDataEvent event) {
        Platform.runLater(() -> {
            if (event.hasSearchQuery()) {
                showNewStellarData(event.getSearchQuery(), event.isShowPlot(), event.isShowTable());
            } else if (event.hasDataSetDescriptor()) {
                showNewStellarData(event.getDataSetDescriptor(), event.isShowPlot(), event.isShowTable());
            } else {
                showNewStellarData(event.isShowPlot(), event.isShowTable());
            }
        });
    }

    @EventListener
    public void onExportQueryEvent(ExportQueryEvent event) {
        Platform.runLater(() -> doExport(event.getSearchQuery()));
    }

    @EventListener
    public void onRoutingStatusEvent(RoutingStatusEvent event) {
        Platform.runLater(() -> statusBarController.routingStatus(event.isStatusFlag()));
    }

    @EventListener
    public void onNewRouteEvent(NewRouteEvent event) {
        eventPublisher.publishEvent(new StatusUpdateEvent(this, "Saving new route..."));
        String taskId = createTaskId("add-route");
        BackgroundTaskRunner.TaskHandle taskHandle = BackgroundTaskRunner.runCancelable(
                "trips-add-route",
                () -> {
                    DataSetDescriptor dataSetDescriptor = event.getDataSetDescriptor();
                    RouteDescriptor routeDescriptor = event.getRouteDescriptor();
                    datasetService.addRouteToDataSet(dataSetDescriptor, routeDescriptor);
                    return null;
                },
                result -> FxThread.runOnFxThread(() -> {
                    log.info("new route");
                    DataSetDescriptor dataSetDescriptor = event.getDataSetDescriptor();
                    routingPanel.setContext(dataSetDescriptor, plotManager.getRouteVisibility());
                    statusBarController.routingStatus(false);
                    eventPublisher.publishEvent(new StatusUpdateEvent(this, "Route saved."));
                }),
                exception -> FxThread.runOnFxThread(() -> {
                    if (isCancellation(exception)) {
                        eventPublisher.publishEvent(new StatusUpdateEvent(this, "Route save cancelled."));
                        return;
                    }
                    String message = exception == null ? "Failed to add route." : exception.getMessage();
                    showErrorAlert("Route Update Error", message);
                    eventPublisher.publishEvent(new StatusUpdateEvent(this, "Failed to save route."));
                }),
                () -> eventPublisher.publishEvent(new BusyStateEvent(this, taskId, false, null, null)));
        eventPublisher.publishEvent(new BusyStateEvent(this, taskId, true, "Saving route...", taskHandle::cancel));
    }

    @EventListener
    public void onUpdateRouteEvent(UpdateRouteEvent event) {
        eventPublisher.publishEvent(new StatusUpdateEvent(this, "Updating route..."));
        String taskId = createTaskId("update-route");
        BackgroundTaskRunner.TaskHandle taskHandle = BackgroundTaskRunner.runCancelable(
                "trips-update-route",
                () -> {
                    RouteDescriptor routeDescriptor = event.getRouteDescriptor();
                    String datasetName = searchContext.getDataSetDescriptor().getDataSetName();
                    return datasetService.updateRoute(datasetName, routeDescriptor);
                },
                descriptor -> FxThread.runOnFxThread(() -> {
                    log.info("update route");
                    searchContext.getAstroSearchQuery().setDescriptor(descriptor);
                    routingPanel.setContext(descriptor, plotManager.getRouteVisibility());
                    interstellarSpacePane.redrawRoutes(descriptor.getRoutes());
                    eventPublisher.publishEvent(new StatusUpdateEvent(this, "Route updated."));
                }),
                exception -> FxThread.runOnFxThread(() -> {
                    if (isCancellation(exception)) {
                        eventPublisher.publishEvent(new StatusUpdateEvent(this, "Route update cancelled."));
                        return;
                    }
                    String message = exception == null ? "Failed to update route." : exception.getMessage();
                    showErrorAlert("Route Update Error", message);
                    eventPublisher.publishEvent(new StatusUpdateEvent(this, "Failed to update route."));
                }),
                () -> eventPublisher.publishEvent(new BusyStateEvent(this, taskId, false, null, null)));
        eventPublisher.publishEvent(new BusyStateEvent(this, taskId, true, "Updating route...", taskHandle::cancel));
    }

    @EventListener
    public void onDisplayRouteEvent(DisplayRouteEvent event) {
        Platform.runLater(() -> interstellarSpacePane.displayRoute(event.getRouteDescriptor(), event.isVisible()));
    }

    @EventListener
    public void onDeleteRouteEvent(DeleteRouteEvent event) {
        eventPublisher.publishEvent(new StatusUpdateEvent(this, "Deleting route..."));
        String taskId = createTaskId("delete-route");
        BackgroundTaskRunner.TaskHandle taskHandle = BackgroundTaskRunner.runCancelable(
                "trips-delete-route",
                () -> {
                    RouteDescriptor routeDescriptor = event.getRouteDescriptor();
                    DataSetDescriptor descriptor = searchContext.getDataSetDescriptor();
                    return datasetService.deleteRoute(descriptor.getDataSetName(), routeDescriptor);
                },
                descriptor -> FxThread.runOnFxThread(() -> {
                    log.info("delete route");
                    RouteDescriptor routeDescriptor = event.getRouteDescriptor();
                    searchContext.getAstroSearchQuery().setDescriptor(descriptor);
                    // clear the route from the plot
                    tripsContext.getCurrentPlot().removeRoute(routeDescriptor);
                    routingPanel.setContext(descriptor, plotManager.getRouteVisibility());
                    interstellarSpacePane.redrawRoutes(descriptor.getRoutes());
                    eventPublisher.publishEvent(new StatusUpdateEvent(this, "Route deleted."));
                }),
                exception -> FxThread.runOnFxThread(() -> {
                    if (isCancellation(exception)) {
                        eventPublisher.publishEvent(new StatusUpdateEvent(this, "Route delete cancelled."));
                        return;
                    }
                    String message = exception == null ? "Failed to delete route." : exception.getMessage();
                    showErrorAlert("Route Update Error", message);
                    eventPublisher.publishEvent(new StatusUpdateEvent(this, "Failed to delete route."));
                }),
                () -> eventPublisher.publishEvent(new BusyStateEvent(this, taskId, false, null, null)));
        eventPublisher.publishEvent(new BusyStateEvent(this, taskId, true, "Deleting route...", taskHandle::cancel));
    }

    @EventListener
    public void onAddDataSetEvent(AddDataSetEvent event) {
        DataSetDescriptor dataSetDescriptor = event.getDataSetDescriptor();

        // add dataset to trips context
        tripsContext.addDataSet(dataSetDescriptor);

        FxThread.runOnFxThread(() -> {
            // add to side-panel
            addDataSetToList(new ArrayList<>(searchContext.getDatasetMap().values()), true);

            // update the query dialog
            if (queryDialog != null) {
                queryDialog.updateDataContext(dataSetDescriptor);
            }
            eventPublisher.publishEvent(new StatusUpdateEvent(this, "Dataset: " + dataSetDescriptor.getDataSetName() + " loaded"));
        });
    }

    @EventListener
    public void onRemoveDataSetEvent(RemoveDataSetEvent event) {
        DataSetDescriptor dataSetDescriptor = event.getDataSetDescriptor();
        FxThread.runOnFxThread(() -> {
            Optional<ButtonType> buttonType = showConfirmationAlert("Remove Dataset",
                    "Remove",
                    "Are you sure you want to remove: " + dataSetDescriptor.getDataSetName());

            if ((buttonType.isPresent()) && (buttonType.get() == ButtonType.OK)) {
                eventPublisher.publishEvent(new StatusUpdateEvent(this,
                        "Removing dataset " + dataSetDescriptor.getDataSetName() + "..."));
                String taskId = createTaskId("remove-dataset");
                BackgroundTaskRunner.TaskHandle taskHandle = BackgroundTaskRunner.runCancelable(
                        "trips-remove-dataset",
                        () -> {
                            tripsContext.removeDataSet(dataSetDescriptor);
                            return null;
                        },
                        result -> FxThread.runOnFxThread(() -> {
                            // update the query dialog
                            if (queryDialog != null) {
                                queryDialog.removeDataset(dataSetDescriptor);
                            }

                            // redisplay the datasets
                            addDataSetToList(new ArrayList<>(searchContext.getDatasetMap().values()), true);
                            eventPublisher.publishEvent(new StatusUpdateEvent(this, "Dataset: " + dataSetDescriptor.getDataSetName() + " removed"));
                        }),
                        exception -> FxThread.runOnFxThread(() -> {
                            if (isCancellation(exception)) {
                                eventPublisher.publishEvent(new StatusUpdateEvent(this,
                                        "Dataset removal cancelled for " + dataSetDescriptor.getDataSetName()));
                                return;
                            }
                            String message = exception == null ? "Failed to remove dataset." : exception.getMessage();
                            showErrorAlert("Remove Dataset Error", message);
                            eventPublisher.publishEvent(new StatusUpdateEvent(this,
                                    "Failed to remove dataset " + dataSetDescriptor.getDataSetName()));
                        }),
                        () -> eventPublisher.publishEvent(new BusyStateEvent(this, taskId, false, null, null)));
                eventPublisher.publishEvent(new BusyStateEvent(this,
                        taskId,
                        true,
                        "Removing dataset " + dataSetDescriptor.getDataSetName() + "...",
                        taskHandle::cancel));
            }
        });
    }

    private String createTaskId(String base) {
        return base + "-" + System.nanoTime();
    }

    private boolean isCancellation(Throwable exception) {
        return exception instanceof CancellationException;
    }

    @EventListener
    public void onSetContextDataSetEvent(SetContextDataSetEvent event) {
        DataSetDescriptor descriptor = event.getDescriptor();

        FxThread.runOnFxThread(() -> {
            // clear all the current data
            clearAll();

            // update the trips context and write through to the database
            tripsContext.setDataSetContext(new DataSetContext(descriptor));

            // update the side panel
            dataSetsListView.getSelectionModel().select(descriptor);

            if (queryDialog != null) {
                queryDialog.setDataSetContext(descriptor);
            }

            eventPublisher.publishEvent(new StatusUpdateEvent(this, ("You are looking at the stars in " + descriptor.getDataSetName() + " dataset.  ")));
        });
    }


    public void clearAll() {
        clearData();
        clearList();
        clearInterstellar();
    }


    public void clearData() {
        eventPublisher.publishEvent(new ClearDataEvent(this));
    }


    public void clearList() {
        eventPublisher.publishEvent(new ClearListEvent(this));
    }


    private void clearInterstellar() {
        interstellarSpacePane.clearAll();
    }


    @EventListener
    public void onContextSelectorEvent(ContextSelectorEvent event) {
        Platform.runLater(() -> {
            switch (event.getContextSelectionType()) {
                case INTERSTELLAR -> {
                    log.info("Showing interstellar Space");
                    interstellarSpacePane.toFront();
                    eventPublisher.publishEvent(new StatusUpdateEvent(this, "Selected Interstellar space"));
                }
                case SOLARSYSTEM -> {
                    log.info("Showing a solar system");
                    solarSystemSpacePane.reset();
                    solarSystemSpacePane.setSystemToDisplay(event.getStarDisplayRecord());
                    solarSystemSpacePane.toFront();
                    eventPublisher.publishEvent(new StatusUpdateEvent(this, "Selected Solarsystem space: " + event.getStarDisplayRecord().getStarName()));
                }
                default -> log.error("Unexpected value: {}", event.getContextSelectionType());
            }
        });
    }


}
