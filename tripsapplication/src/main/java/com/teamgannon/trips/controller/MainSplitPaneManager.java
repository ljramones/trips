package com.teamgannon.trips.controller;

import com.teamgannon.trips.algorithms.Universe;
import com.teamgannon.trips.config.application.Localization;
import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.controller.shared.SharedUIFunctions;
import com.teamgannon.trips.controller.splitpane.*;
import com.teamgannon.trips.dialogs.ExportQueryDialog;
import com.teamgannon.trips.dialogs.query.QueryDialog;
import com.teamgannon.trips.events.*;
import com.teamgannon.trips.graphics.PlotManager;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.graphics.panes.InterstellarSpacePane;
import com.teamgannon.trips.javafxsupport.BackgroundTaskRunner;
import com.teamgannon.trips.javafxsupport.FxThread;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.routing.sidepanel.RoutingPanel;
import com.teamgannon.trips.search.AstroSearchQuery;
import com.teamgannon.trips.service.DataExportService;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.DatasetService;
import com.teamgannon.trips.service.StarService;
import com.teamgannon.trips.tableviews.StarTableDialog;
import javafx.beans.property.DoubleProperty;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxWeaver;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

@Slf4j
@Component
public class MainSplitPaneManager {

    public final static double SCREEN_PROPORTION = 0.60;
    public final static double SIDE_PANEL_SIZE = 350;

    private final SharedUIFunctions sharedUIFunctions;
    private final ApplicationEventPublisher eventPublisher;
    private final TripsContext tripsContext;
    private PlotManager plotManager;
    private final DataExportService dataExportService;
    private final Localization localization;
    private final DatasetService datasetService;
    private final RightPanelCoordinator rightPanelCoordinator;
    private final SearchContextCoordinator searchContextCoordinator;
    private final FxWeaver fxWeaver;

    // Event handlers
    private final RouteEventHandler routeEventHandler;
    private final DataSetEventHandler dataSetEventHandler;
    private final ViewContextHandler viewContextHandler;

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

    @Getter
    private SplitPane mainSplitPane;
    private BorderPane leftBorderPane;
    @Getter
    private StackPane leftDisplayPane;
    private BorderPane rightBorderPane;
    @Getter
    private VBox settingsPane;

    private final DatabaseManagementService databaseManagementService;
    /**
     * interstellar space
     */
    private final InterstellarSpacePane interstellarSpacePane;
    private final StarService starService;
    private SplitPaneView splitPaneView;
    private RightPanelController rightPanelController;
    private LeftDisplayController leftDisplayController;

    /**
     * the query dialog
     */
    private QueryDialog queryDialog;

    private SliderControlManager sliderControlManager;

    @Autowired
    public MainSplitPaneManager(SharedUIFunctions sharedUIFunctions,
                                ApplicationEventPublisher eventPublisher,
                                TripsContext tripsContext,
                                RoutingPanel routingPanel,
                                Localization localization,
                                DatabaseManagementService databaseManagementService,
                                DatasetService datasetService,
                                DataExportService dataExportService,
                                InterstellarSpacePane interstellarSpacePane,
                                StarService starService,
                                FxWeaver fxWeaver,
                                RightPanelCoordinator rightPanelCoordinator,
                                SearchContextCoordinator searchContextCoordinator,
                                RouteEventHandler routeEventHandler,
                                DataSetEventHandler dataSetEventHandler,
                                ViewContextHandler viewContextHandler) {
        this.sharedUIFunctions = sharedUIFunctions;
        this.eventPublisher = eventPublisher;
        this.tripsContext = tripsContext;
        this.routingPanel = routingPanel;
        this.localization = localization;
        this.databaseManagementService = databaseManagementService;
        this.datasetService = datasetService;
        this.dataExportService = dataExportService;
        this.rightPanelCoordinator = rightPanelCoordinator;
        this.searchContextCoordinator = searchContextCoordinator;
        this.interstellarSpacePane = interstellarSpacePane;
        this.starService = starService;
        this.fxWeaver = fxWeaver;
        this.routeEventHandler = routeEventHandler;
        this.dataSetEventHandler = dataSetEventHandler;
        this.viewContextHandler = viewContextHandler;
    }

    public void initialize(SliderControlManager sliderControlManager, PlotManager plotManager) {
        this.sliderControlManager = sliderControlManager;
        this.plotManager = plotManager;

        fxWeaver.loadView(com.teamgannon.trips.controller.splitpane.SplitPaneController.class);
        this.splitPaneView = fxWeaver.getBean(com.teamgannon.trips.controller.splitpane.SplitPaneController.class);
        this.rightPanelController = splitPaneView.getRightPanel();
        this.leftDisplayController = splitPaneView.getLeftDisplay();
        this.mainSplitPane = splitPaneView.getMainSplitPane();
        this.leftBorderPane = splitPaneView.getLeftBorderPane();
        this.leftDisplayPane = splitPaneView.getLeftDisplayPane();
        this.rightBorderPane = splitPaneView.getRightBorderPane();
        this.settingsPane = splitPaneView.getSettingsPane();
        this.propertiesAccordion = splitPaneView.getPropertiesAccordion();

        this.mainSplitPane.setDividerPositions(1.0);
        this.mainSplitPane.setPrefWidth(Universe.boxWidth);


        // Create left and right panes
        leftDisplayController.build(plotManager);
        createRightDisplay();
        rightPanelCoordinator.initialize();

        connectSolarSystemReferenceControls();

        // Initialize the SliderControlManager
        sliderControlManager.initialize(mainSplitPane);

        // Set up the slider control
        setSliderControl(sliderControlManager);

        // Initialize SharedUIFunctions
        sharedUIFunctions.initialize(plotManager, mainSplitPane, sliderControlManager);

        // Initialize event handlers
        routeEventHandler.initialize(plotManager);
        dataSetEventHandler.initialize(rightPanelController);
        viewContextHandler.initialize(leftDisplayController);
    }

    private void connectSolarSystemReferenceControls() {
        if (rightPanelController == null || leftDisplayController == null) {
            return;
        }
        var solarSystemSidePane = rightPanelController.getSolarSystemSidePane();
        if (solarSystemSidePane == null) {
            return;
        }
        var referencePane = solarSystemSidePane.getReferenceCueControlPane();
        if (referencePane == null) {
            return;
        }
        var solarSystemSpacePane = leftDisplayController.getSolarSystemSpacePane();
        if (solarSystemSpacePane == null) {
            return;
        }

        referencePane.getShowEclipticPlaneCheckbox().selectedProperty().addListener((obs, oldVal, newVal) -> {
            solarSystemSpacePane.toggleEclipticPlane(Boolean.TRUE.equals(newVal));
        });

        referencePane.getShowOrbitNodesCheckbox().selectedProperty().addListener((obs, oldVal, newVal) -> {
            solarSystemSpacePane.toggleOrbitNodes(Boolean.TRUE.equals(newVal));
        });
    }

    private void createRightDisplay() {
        datasetsPane = rightPanelController.getDatasetsPane();
        objectsViewPane = rightPanelController.getObjectsViewPane();
        stellarObjectPane = rightPanelController.getStellarObjectPane();
        transitPane = rightPanelController.getTransitPane();
        routingPane = rightPanelController.getRoutingPane();
        transitFilterPane = rightPanelController.getTransitFilterPane();
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
     * Show the star table dialog with server-side pagination.
     *
     * @param query the search query to use
     */
    public void showTable(@NotNull AstroSearchQuery query) {
        if (query.getDataSetContext() == null || query.getDataSetContext().getDescriptor() == null) {
            showErrorAlert("Display Data table", "No dataset context available");
            return;
        }
        StarTableDialog dialog = new StarTableDialog(starService, query);
        dialog.show();
    }

    /**
     * Legacy method for backward compatibility.
     * Shows the star table if there are stars in the list.
     *
     * @param starObjects the list of star objects (used only to check if non-empty)
     * @deprecated Use showTable(AstroSearchQuery) instead for server-side pagination
     */
    @Deprecated
    public void showList(@NotNull List<StarObject> starObjects) {
        if (!starObjects.isEmpty()) {
            AstroSearchQuery query = searchContextCoordinator.getAstroSearchQuery();
            if (query != null && query.getDataSetContext() != null) {
                showTable(query);
            } else {
                showErrorAlert("Display Data table", "No search context available");
            }
        } else {
            showErrorAlert("Display Data table", "no data to show");
        }
    }

    /**
     * show the data in a spreadsheet
     */
    private void showTableData() {

        if (tripsContext.getDataSetContext().isValidDescriptor()) {
            AstroSearchQuery query = searchContextCoordinator.getAstroSearchQuery();
            if (query != null) {
                showTable(query);
            } else {
                showErrorAlert("Show Data Table", "no search query available");
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

                // Set context first
                eventPublisher.publishEvent(new SetContextDataSetEvent(this, dataSetDescriptor));

                // Now show table with query
                AstroSearchQuery query = searchContextCoordinator.getAstroSearchQuery();
                if (query != null) {
                    showTable(query);
                    eventPublisher.publishEvent(new StatusUpdateEvent(this, "Dataset table loaded is: " + dataSetDescriptor.getDataSetName()));
                } else {
                    showErrorAlert("Show Data Table", "No data to show");
                }
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
        return searchContextCoordinator.getAstrographicObjectsOnQuery();
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
        try {
            StarDisplayRecord starId = event.getStarDisplayRecord();
            log.info("recenter plot at {}", starId);
            AstroSearchQuery query = searchContextCoordinator.getAstroSearchQuery();
            searchContextCoordinator.recenter(starId, query.getUpperDistanceLimit());
            log.info("New Center Range: {}", query.getCenterRangingCube());
            FxThread.runOnFxThread(() -> showNewStellarData(query, true, false));
        } catch (Exception e) {
            log.error("Error handling recenter star event", e);
            FxThread.runOnFxThread(() -> {
                showErrorAlert("Recenter Error", "Failed to recenter on star: " + e.getMessage());
                eventPublisher.publishEvent(new StatusUpdateEvent(this, "Recenter failed"));
            });
        }
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
        ExportQueryDialog exportQueryDialog = new ExportQueryDialog(searchContextCoordinator.getSearchContext(),
                databaseManagementService,
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
        searchContextCoordinator.setAstroSearchQuery(searchQuery);

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
                        int sampleSize = Math.min(50, starObjects.size());
                        for (int i = 0; i < sampleSize; i++) {
                            StarObject starObject = starObjects.get(i);
                            log.info("SearchPane sample {}: name={}, distance={}, x={}, y={}, z={}",
                                    i + 1,
                                    starObject.getDisplayName(),
                                    starObject.getDistance(),
                                    starObject.getX(),
                                    starObject.getY(),
                                    starObject.getZ());
                        }
                        if (showPlot) {
                            plotManager.drawAstrographicData(descriptor,
                                    starObjects,
                                    displayRadius,
                                    searchQuery.getCenterCoordinates(),
                                    tripsContext.getAppViewPreferences().getColorPalette(),
                                    tripsContext.getAppViewPreferences().getStarDisplayPreferences(),
                                    tripsContext.getAppViewPreferences().getCivilizationDisplayPreferences()
                            );

                            routingPanel.setContext(descriptor, plotManager.getRouteVisibility());
                        }
                        if (showTable) {
                            // Use server-side paginated table instead of loading all into memory
                            showTable(searchQuery);
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
        FxThread.runOnFxThread(() -> {
            try {
                if (event.hasSearchQuery()) {
                    showNewStellarData(event.getSearchQuery(), event.isShowPlot(), event.isShowTable());
                } else if (event.hasDataSetDescriptor()) {
                    showNewStellarData(event.getDataSetDescriptor(), event.isShowPlot(), event.isShowTable());
                } else {
                    showNewStellarData(event.isShowPlot(), event.isShowTable());
                }
            } catch (Exception e) {
                log.error("Error handling show stellar data event", e);
                showErrorAlert("Display Error", "Failed to display stellar data: " + e.getMessage());
                eventPublisher.publishEvent(new StatusUpdateEvent(this, "Display failed"));
            }
        });
    }

    @EventListener
    public void onExportQueryEvent(ExportQueryEvent event) {
        FxThread.runOnFxThread(() -> {
            try {
                doExport(event.getSearchQuery());
            } catch (Exception e) {
                log.error("Error handling export query event", e);
                showErrorAlert("Export Error", "Failed to export data: " + e.getMessage());
                eventPublisher.publishEvent(new StatusUpdateEvent(this, "Export failed"));
            }
        });
    }

    /**
     * Handle PlotStarsEvent to plot a specific list of stars.
     * This is triggered when the user clicks "Plot" in a table dialog.
     */
    @EventListener
    public void onPlotStarsEvent(PlotStarsEvent event) {
        FxThread.runOnFxThread(() -> {
            try {
                List<StarObject> starObjects = event.getStarObjects();
                DataSetDescriptor descriptor = event.getDataSetDescriptor();

                if (starObjects == null || starObjects.isEmpty()) {
                    showErrorAlert("Plot Stars", "No stars to plot");
                    return;
                }
                if (descriptor == null) {
                    showErrorAlert("Plot Stars", "No dataset descriptor available");
                    return;
                }

                // Get preferences from context
                var colorPalette = tripsContext.getAppViewPreferences().getColorPalette();
                var starDisplayPreferences = tripsContext.getAppViewPreferences().getStarDisplayPreferences();
                var civilizationDisplayPreferences = tripsContext.getAppViewPreferences().getCivilizationDisplayPreferences();

                // Calculate center coordinates from the star list
                double[] centerCoordinates = calculateCenterCoordinates(starObjects);

                // Calculate display radius to encompass all stars
                double displayRadius = calculateDisplayRadius(starObjects, centerCoordinates);

                // Update search context for consistency
                searchContextCoordinator.setDescriptor(descriptor);

                // Plot the stars
                plotManager.drawAstrographicData(
                        descriptor,
                        starObjects,
                        displayRadius,
                        centerCoordinates,
                        colorPalette,
                        starDisplayPreferences,
                        civilizationDisplayPreferences
                );

                // Update routing panel
                routingPanel.setContext(descriptor, plotManager.getRouteVisibility());

                // Status message
                String statusMsg = event.getDescription() != null
                        ? String.format("Plotted %d stars (%s)", starObjects.size(), event.getDescription())
                        : "Plotted %d stars".formatted(starObjects.size());
                eventPublisher.publishEvent(new StatusUpdateEvent(this, statusMsg));

                log.info("Plotted {} stars from PlotStarsEvent", starObjects.size());

            } catch (Exception e) {
                log.error("Error handling plot stars event", e);
                showErrorAlert("Plot Stars Error", "Failed to plot stars: " + e.getMessage());
                eventPublisher.publishEvent(new StatusUpdateEvent(this, "Plot failed"));
            }
        });
    }

    /**
     * Calculate the center coordinates from a list of stars.
     */
    private double[] calculateCenterCoordinates(List<StarObject> starObjects) {
        if (starObjects.isEmpty()) {
            return new double[]{0, 0, 0};
        }

        double sumX = 0, sumY = 0, sumZ = 0;
        for (StarObject star : starObjects) {
            sumX += star.getX();
            sumY += star.getY();
            sumZ += star.getZ();
        }
        int count = starObjects.size();
        return new double[]{sumX / count, sumY / count, sumZ / count};
    }

    /**
     * Calculate a display radius that encompasses all stars.
     */
    private double calculateDisplayRadius(List<StarObject> starObjects, double[] center) {
        if (starObjects.isEmpty()) {
            return 20.0; // Default radius
        }

        double maxDistance = 0;
        for (StarObject star : starObjects) {
            double dx = star.getX() - center[0];
            double dy = star.getY() - center[1];
            double dz = star.getZ() - center[2];
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            maxDistance = Math.max(maxDistance, distance);
        }

        // Add some padding
        return Math.max(maxDistance * 1.2, 10.0);
    }

    private String createTaskId(String base) {
        return base + "-" + System.nanoTime();
    }

    private boolean isCancellation(Throwable exception) {
        return exception instanceof java.util.concurrent.CancellationException;
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

    /**
     * Sets the query dialog reference for event handlers.
     *
     * @param queryDialog the query dialog
     */
    public void setQueryDialog(QueryDialog queryDialog) {
        this.queryDialog = queryDialog;
        dataSetEventHandler.setQueryDialog(queryDialog);
    }
}
