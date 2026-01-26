package com.teamgannon.trips.controller.menubar;

import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.controller.MainSplitPaneManager;
import com.teamgannon.trips.dialogs.query.AdvResultsSet;
import com.teamgannon.trips.dialogs.query.AdvancedQueryDialog;
import com.teamgannon.trips.dialogs.query.QueryDialog;
import com.teamgannon.trips.events.PlotStarsEvent;
import com.teamgannon.trips.events.UIStateChangeEvent;
import com.teamgannon.trips.controller.UIElement;
import com.teamgannon.trips.graphics.PlotManager;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.graphics.panes.InterstellarSpacePane;
import com.teamgannon.trips.routing.RouteManager;
import com.teamgannon.trips.routing.automation.RouteFinderDataset;
import com.teamgannon.trips.routing.automation.RouteFinderInView;
import com.teamgannon.trips.routing.dialogs.ContextManualRoutingDialog;
import com.teamgannon.trips.routing.model.RoutingType;
import com.teamgannon.trips.routing.sidepanel.RoutingPanel;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.DatasetService;
import com.teamgannon.trips.service.StarService;
import com.teamgannon.trips.service.graphsearch.LargeGraphSearchService;
import com.teamgannon.trips.service.measure.StarMeasurementService;
import com.teamgannon.trips.starplotting.StarPlotManager;
import com.teamgannon.trips.transits.*;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxWeaver;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.teamgannon.trips.support.AlertFactory.*;

/**
 * Controller for the Tools menu.
 * Handles routing, transit/link calculations, and advanced search operations.
 */
@Slf4j
@Component
public class ToolsMenuController {

    private final TripsContext tripsContext;
    private final InterstellarSpacePane interstellarSpacePane;
    private final RouteFinderInView routeFinderInView;
    private final LargeGraphSearchService largeGraphSearchService;
    private final DatabaseManagementService databaseManagementService;
    private final DatasetService datasetService;
    private final StarService starService;
    private final StarMeasurementService starMeasurementService;
    private final TransitCalculationService transitCalculationService;
    private final ApplicationEventPublisher eventPublisher;
    private final RoutingPanel routingPanel;
    private final MainSplitPaneManager mainSplitPaneManager;
    private final FxWeaver fxWeaver;

    /**
     * The query dialog instance - created lazily and reused.
     */
    private QueryDialog queryDialog;

    public ToolsMenuController(TripsContext tripsContext,
                               InterstellarSpacePane interstellarSpacePane,
                               RouteFinderInView routeFinderInView,
                               LargeGraphSearchService largeGraphSearchService,
                               DatabaseManagementService databaseManagementService,
                               DatasetService datasetService,
                               StarService starService,
                               StarMeasurementService starMeasurementService,
                               TransitCalculationService transitCalculationService,
                               ApplicationEventPublisher eventPublisher,
                               RoutingPanel routingPanel,
                               MainSplitPaneManager mainSplitPaneManager,
                               FxWeaver fxWeaver) {
        this.tripsContext = tripsContext;
        this.interstellarSpacePane = interstellarSpacePane;
        this.routeFinderInView = routeFinderInView;
        this.largeGraphSearchService = largeGraphSearchService;
        this.databaseManagementService = databaseManagementService;
        this.datasetService = datasetService;
        this.starService = starService;
        this.starMeasurementService = starMeasurementService;
        this.transitCalculationService = transitCalculationService;
        this.eventPublisher = eventPublisher;
        this.routingPanel = routingPanel;
        this.mainSplitPaneManager = mainSplitPaneManager;
        this.fxWeaver = fxWeaver;
    }

    /**
     * Opens route finder for currently displayed stars.
     */
    public void routeFinderInView(ActionEvent actionEvent) {
        try {
            if (interstellarSpacePane.getCurrentStarsInView().size() > 2) {
                routeFinderInView.startRouteLocation(tripsContext.getSearchContext().getDataSetDescriptor());
            } else {
                showErrorAlert("Route Finder", "You need to have more than 2 stars on a plot to use.");
            }
        } catch (Exception e) {
            log.error("Error opening route finder in view", e);
            showErrorAlert("Route Finder", "Failed to open route finder: " + e.getMessage());
        }
    }

    /**
     * Opens route finder for entire dataset.
     */
    public void routeFinderDataset(ActionEvent actionEvent) {
        try {
            RouteFinderDataset routeFinderDataset = new RouteFinderDataset(interstellarSpacePane, largeGraphSearchService);
            routeFinderDataset.startRouteLocation(
                    tripsContext.getSearchContext().getDataSetDescriptor(),
                    databaseManagementService,
                    starService,
                    starMeasurementService,
                    eventPublisher
            );
        } catch (Exception e) {
            log.error("Error opening route finder for dataset", e);
            showErrorAlert("Route Finder", "Failed to open route finder: " + e.getMessage());
        }
    }

    /**
     * Enables manual route creation by clicking on stars.
     */
    public void clickRoutes(ActionEvent actionEvent) {
        try {
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
        } catch (Exception e) {
            log.error("Error setting up manual routing", e);
            showErrorAlert("Manual Routing", "Failed to set up routing: " + e.getMessage());
        }
    }

    /**
     * Opens dialog to edit or delete routes.
     */
    public void editDeleteRoutes(ActionEvent actionEvent) {
        showWarningMessage("Edit/Delete Routes", "This function isn't implemented yet");
    }

    /**
     * Clears all routes from the display.
     */
    public void clearRoutes(ActionEvent actionEvent) {
        try {
            interstellarSpacePane.clearRoutes();
            datasetService.clearRoutesFromCurrent(tripsContext.getSearchContext().getDataSetDescriptor());
            routingPanel.clearData();
        } catch (Exception e) {
            log.error("Error clearing routes", e);
            showErrorAlert("Clear Routes", "Failed to clear routes: " + e.getMessage());
        }
    }

    /**
     * Opens transit finder dialog to calculate links between stars.
     */
    public void transitFinder(ActionEvent actionEvent) {
        try {
            FindTransitsBetweenStarsDialog findTransitsBetweenStarsDialog
                    = new FindTransitsBetweenStarsDialog(datasetService,
                    tripsContext.getDataSetDescriptor().getTransitDefinitions());
            Optional<TransitDefinitions> optionalTransitDefinitions = findTransitsBetweenStarsDialog.showAndWait();
            if (optionalTransitDefinitions.isPresent()) {
                TransitDefinitions transitDefinitions = optionalTransitDefinitions.get();
                if (transitDefinitions.isSelected()) {
                    runTransitCalculationWithProgress(transitDefinitions);
                }
            }
        } catch (Exception e) {
            log.error("Error opening transit finder", e);
            showErrorAlert("Transit Finder", "Failed to open dialog: " + e.getMessage());
        }
    }

    /**
     * Run transit calculation with a progress dialog.
     * For small star counts, this completes quickly.
     * For large star counts, shows progress and allows cancellation.
     */
    private void runTransitCalculationWithProgress(TransitDefinitions transitDefinitions) {
        List<StarDisplayRecord> starsInView = interstellarSpacePane.getCurrentStarsInView();

        if (starsInView.isEmpty()) {
            showErrorAlert("Transit Finder", "No stars in view to calculate transits");
            return;
        }

        // For very small datasets, just run synchronously (fast enough)
        if (starsInView.size() <= 50) {
            interstellarSpacePane.findTransits(transitDefinitions);
            mainSplitPaneManager.getTransitFilterPane().setFilter(transitDefinitions, interstellarSpacePane.getTransitManager());
            return;
        }

        // For larger datasets, show progress dialog
        TransitManager transitManager = interstellarSpacePane.getTransitManager();
        ITransitDistanceCalculator calculator = transitManager.getCalculatorFactory().getCalculator(starsInView.size());

        TransitCalculationDialog calculationDialog = new TransitCalculationDialog(
                transitDefinitions,
                starsInView,
                calculator,
                eventPublisher,
                transitCalculationService
        );

        Optional<TransitCalculationResult> resultOptional = calculationDialog.showAndWait();
        if (resultOptional.isPresent()) {
            TransitCalculationResult result = resultOptional.get();
            if (result.isSuccess()) {
                transitManager.applyCalculatedTransits(result);
                mainSplitPaneManager.getTransitFilterPane().setFilter(transitDefinitions, transitManager);
                log.info("Transit calculation complete: {} routes found in {} ms",
                        result.getTotalRoutes(), result.getCalculationTimeMs());
            } else if (result.isCancelled()) {
                log.info("Transit calculation was cancelled");
            } else {
                showErrorAlert("Transit Calculation", "Calculation failed: " + result.getErrorMessage());
            }
        }
    }

    /**
     * Clears all transit links from the display.
     */
    public void clearTransits(ActionEvent actionEvent) {
        try {
            interstellarSpacePane.clearTransits();
            eventPublisher.publishEvent(new UIStateChangeEvent(this, UIElement.TRANSITS, false));
            eventPublisher.publishEvent(new UIStateChangeEvent(this, UIElement.TRANSIT_LENGTHS, false));
            mainSplitPaneManager.getTransitFilterPane().clear();
        } catch (Exception e) {
            log.error("Error clearing transits", e);
            showErrorAlert("Clear Transits", "Failed to clear transits: " + e.getMessage());
        }
    }

    /**
     * Opens the query dialog (same as Edit menu).
     */
    public void runQuery(ActionEvent actionEvent) {
        try {
            if (tripsContext.getSearchContext().getDatasetMap().isEmpty()) {
                log.error("There aren't any datasets so don't show");
                showErrorAlert("Search Query", "There aren't any datasets to search on.\nPlease import one first");
            } else {
                // Create the dialog lazily on first use
                if (queryDialog == null) {
                    queryDialog = new QueryDialog(tripsContext.getSearchContext(), eventPublisher);
                    queryDialog.initModality(Modality.NONE);
                }
                queryDialog.refreshDataSets();
                queryDialog.show();
            }
        } catch (Exception e) {
            log.error("Error running query", e);
            showErrorAlert("Query Error", "Failed to run query: " + e.getMessage());
        }
    }

    /**
     * Opens the advanced search dialog.
     */
    public void advancedSearch(ActionEvent actionEvent) {
        try {
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
                                eventPublisher.publishEvent(new PlotStarsEvent(
                                        this,
                                        advResultsSet.getStarsFound(),
                                        advResultsSet.getDataSetDescriptor(),
                                        "Advanced Query Results"));
                            }
                        } else {
                            showInfoMessage("Advanced Query", "No stars were found to match query");
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error in advanced search", e);
            showErrorAlert("Advanced Search", "Failed to search: " + e.getMessage());
        }
    }

    /**
     * Opens the Data Workbench window.
     */
    public void openDataWorkbench(ActionEvent actionEvent) {
        try {
            Parent root = fxWeaver.loadView(com.teamgannon.trips.workbench.DataWorkbenchController.class);
            Stage stage = new Stage();
            stage.setTitle("TRIPS Data Workbench");
            stage.initModality(Modality.NONE);
            stage.setScene(new Scene(root, 900, 650));
            stage.show();
        } catch (Exception e) {
            log.error("Error opening Data Workbench", e);
            showErrorAlert("Data Workbench", "Failed to open workbench: " + e.getMessage());
        }
    }
}
