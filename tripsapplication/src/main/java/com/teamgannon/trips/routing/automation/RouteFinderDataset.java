package com.teamgannon.trips.routing.automation;

import com.teamgannon.trips.graphics.panes.InterstellarSpacePane;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.routing.dialogs.DisplayAutoRoutesDialog;
import com.teamgannon.trips.routing.dialogs.RouteFinderDialogInDataSet;
import com.teamgannon.trips.routing.dialogs.RouteLoadingInfoDialog;
import com.teamgannon.trips.routing.model.PossibleRoutes;
import com.teamgannon.trips.routing.model.RouteFindingOptions;
import com.teamgannon.trips.routing.model.RoutingMetric;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.StarService;
import com.teamgannon.trips.service.graphsearch.GraphRouteResult;
import com.teamgannon.trips.service.graphsearch.LargeGraphSearchService;
import com.teamgannon.trips.service.measure.StarMeasurementService;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

@Slf4j
public class RouteFinderDataset {

    /**
     * used to plot the routes found
     */
    private final InterstellarSpacePane interstellarSpacePane;
    private final LargeGraphSearchService largeGraphSearchService;
    private DataSetDescriptor currentDataset;
    private ApplicationEventPublisher eventPublisher;

    public RouteFinderDataset(InterstellarSpacePane interstellarSpacePane,
                              LargeGraphSearchService largeGraphSearchService) {
        this.interstellarSpacePane = interstellarSpacePane;
        this.largeGraphSearchService = largeGraphSearchService;
    }

    /**
     * start the location of routes
     */
    public void startRouteLocation(DataSetDescriptor currentDataset,
                                   DatabaseManagementService databaseManagementService,
                                   StarService starService,
                                   StarMeasurementService starMeasurementService,
                                   ApplicationEventPublisher eventPublisher) {

        this.currentDataset = currentDataset;
        this.eventPublisher = eventPublisher;

        RouteFinderDialogInDataSet routeFinderDialogInView = new RouteFinderDialogInDataSet(
                currentDataset.getDataSetName(),
                starMeasurementService,
                databaseManagementService,
                starService);

        Stage theStage = (Stage) routeFinderDialogInView.getDialogPane().getScene().getWindow();
        theStage.setAlwaysOnTop(true);
        theStage.toFront();

        Optional<RouteFindingOptions> routeFindingOptionsOptional = routeFinderDialogInView.showAndWait();
        if (routeFindingOptionsOptional.isPresent()) {
            log.info("selected");

            RouteFindingOptions routeFindingOptions = routeFindingOptionsOptional.get();

            // if we actually selected the option to route then do it
            if (routeFindingOptions.isSelected()) {

                RouteLoadingInfoDialog routeLoadingInfoDialog = new RouteLoadingInfoDialog(
                        currentDataset, databaseManagementService,
                        starService,
                        largeGraphSearchService,
                        eventPublisher,
                        routeFindingOptions);
                Optional<GraphRouteResult> routeResultOptional = routeLoadingInfoDialog.showAndWait();

                if (routeResultOptional.isPresent()) {
                    GraphRouteResult graphRouteResult = routeResultOptional.get();
                    log.info("Route found is {}", graphRouteResult.getMessage());
                    if (graphRouteResult.isSearchCancelled()) {
                        return;
                    }
                    if (graphRouteResult.isRouteFound()) {
                        PossibleRoutes possibleRoutes = graphRouteResult.getPossibleRoutes();
                        // Create non-modal dialog with preview and accept callbacks
                        DisplayAutoRoutesDialog dialog = new DisplayAutoRoutesDialog(
                                possibleRoutes,
                                // Preview callback: plot routes without closing dialog
                                selectedRoutes -> {
                                    log.info("Previewing {} routes", selectedRoutes.size());
                                    plot(selectedRoutes);
                                },
                                // Accept callback: final plot when user clicks Accept
                                selectedRoutes -> {
                                    if (!selectedRoutes.isEmpty()) {
                                        log.info("Accepted {} routes", selectedRoutes.size());
                                        plot(selectedRoutes);
                                    }
                                }
                        );
                        // Show non-modal dialog (user can interact with map)
                        dialog.show();
                    }
                }
            }
        }
    }

    /**
     * plot the routes found
     *
     * @param routeList the routes to plot
     */
    private void plot(List<RoutingMetric> routeList) {
        log.info(routeList.toString());
        interstellarSpacePane.plotRouteDescriptors(currentDataset, routeList);
    }

}
