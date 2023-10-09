package com.teamgannon.trips.routing.automation;

import com.teamgannon.trips.graphics.panes.InterstellarSpacePane;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.listener.StatusUpdaterListener;
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
                                   StatusUpdaterListener statusUpdaterListener) {

        this.currentDataset = currentDataset;

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
                        statusUpdaterListener, routeFindingOptions);
                Optional<GraphRouteResult> routeResultOptional = routeLoadingInfoDialog.showAndWait();

                if (routeResultOptional.isPresent()) {
                    GraphRouteResult graphRouteResult = routeResultOptional.get();
                    log.info("Route found is {}", graphRouteResult.getMessage());
                    if (graphRouteResult.isSearchCancelled()) {
                        return;
                    }
                    if (graphRouteResult.isRouteFound()) {
                        PossibleRoutes possibleRoutes = graphRouteResult.getPossibleRoutes();
                        DisplayAutoRoutesDialog displayAutoRoutesDialog = new DisplayAutoRoutesDialog(possibleRoutes);
                        Optional<List<RoutingMetric>> optionalRoutingMetrics = displayAutoRoutesDialog.showAndWait();
                        if (optionalRoutingMetrics.isPresent()) {
                            List<RoutingMetric> selectedRoutingMetrics = optionalRoutingMetrics.get();
                            if (selectedRoutingMetrics.size() > 0) {
                                log.info("plotting selected routes:{}", selectedRoutingMetrics);
                                // plot the stars and routes found
                                plot(selectedRoutingMetrics);
                            }
                        }
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
