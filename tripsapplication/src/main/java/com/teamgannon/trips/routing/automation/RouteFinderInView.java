package com.teamgannon.trips.routing.automation;

import com.teamgannon.trips.dialogs.search.model.DistanceRoutes;
import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.graphics.panes.InterstellarSpacePane;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.routing.dialogs.DisplayAutoRoutesDialog;
import com.teamgannon.trips.routing.dialogs.RouteFinderDialogInView;
import com.teamgannon.trips.routing.model.PossibleRoutes;
import com.teamgannon.trips.routing.model.RouteFindingOptions;
import com.teamgannon.trips.routing.model.RoutingMetric;
import com.teamgannon.trips.service.measure.StarMeasurementService;
import com.teamgannon.trips.transits.TransitRoute;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

@Slf4j
@Component
public class RouteFinderInView {

    private static final int GRAPH_THRESHOLD = 1500;

    /**
     * used to plot the routes found
     */
    private final InterstellarSpacePane interstellarSpacePane;
    private final StarMeasurementService starMeasurementService;

    /**
     * the constructor
     *
     * @param interstellarSpacePane the graphics pane to plot
     */
    public RouteFinderInView(InterstellarSpacePane interstellarSpacePane,
                             StarMeasurementService starMeasurementService) {
        this.interstellarSpacePane = interstellarSpacePane;
        this.starMeasurementService = starMeasurementService;
    }

    /**
     * start the location of routes
     *
     * @param currentDataSet the current dataset
     */
    public void startRouteLocation(DataSetDescriptor currentDataSet) {
        RouteFinderDialogInView routeFinderDialogInView = new RouteFinderDialogInView(interstellarSpacePane.getCurrentStarsInView());
        Stage theStage = (Stage) routeFinderDialogInView.getDialogPane().getScene().getWindow();
        theStage.setAlwaysOnTop(true);
        theStage.toFront();

        // get the route location parameters from the dialog
        processRouteRequest(currentDataSet, theStage, routeFinderDialogInView);
    }

    public void processRouteRequest(DataSetDescriptor currentDataSet,
                                    Stage theStage,
                                    @NotNull RouteFinderDialogInView routeFinderDialogInView) {

        Optional<RouteFindingOptions> routeFindingOptionsOptional = routeFinderDialogInView.showAndWait();
        if (routeFindingOptionsOptional.isPresent()) {
            RouteFindingOptions routeFindingOptions = routeFindingOptionsOptional.get();

            // if we actually selected the option to route then do it
            if (routeFindingOptions.isSelected()) {
                findRoute(currentDataSet, theStage, routeFinderDialogInView, routeFindingOptions);
            }
        }
    }

    public void findRoute(DataSetDescriptor currentDataSet,
                          Stage theStage,
                          @NotNull RouteFinderDialogInView routeFinderDialogInView,
                          RouteFindingOptions routeFindingOptions) {
        try {
            log.info("find route between stars");

            // setup our initials
            String origin = routeFindingOptions.getOriginStarName();
            String destination = routeFindingOptions.getDestinationStarName();
            List<StarDisplayRecord> starsInView = interstellarSpacePane.getCurrentStarsInView();
            List<StarDisplayRecord> prunedStars = prune(starsInView, routeFindingOptions);

            if (prunedStars.size() > GRAPH_THRESHOLD) {
                showErrorAlert("Route Finder", "There are too many stars to plan a route");
            }

            RouteBuilderHelper routeBuilderHelper = new RouteBuilderHelper(prunedStars);

            // check if the start star is present
            if (!routeBuilderHelper.has(origin)) {
                showErrorAlert("Route Finder", "The start star is not in route");
            }

            // check if the destination star is present
            if (!routeBuilderHelper.has(destination)) {
                showErrorAlert("Route Finder", "The destination star is not in route");
            }

            // calculate the transits based on upper and lower bounds
            DistanceRoutes distanceRoutes = DistanceRoutes
                    .builder()
                    .upperDistance(routeFindingOptions.getUpperBound())
                    .lowerDistance((routeFindingOptions.getLowerBound()))
                    .build();
            List<TransitRoute> transitRoutes = starMeasurementService.calculateDistances(distanceRoutes, prunedStars);
            log.info("transits calculated");

            // create a graph based on the transits available
            RouteGraph routeGraph = new RouteGraph();
            routeGraph.calculateGraphForTransit(transitRoutes);
            try {
                // check if the origin star and destination star are connected to each other
                if (routeGraph.isConnected(origin, destination)) {
                    determineRoutesAndPlotOne(currentDataSet, theStage, routeFindingOptions, origin, destination, routeBuilderHelper, routeGraph);
                } else {
                    log.error("Source and destination stars do not have a path");
                    showErrorAlert("Route Finder from A to B",
                            "Unable to find a route between source and destination based on supplied parameters.");
                    processRouteRequest(currentDataSet, theStage, routeFinderDialogInView);
                }
            } catch (Exception e) {
                showErrorAlert("Route Finder from A to B",
                        "Unable to find a route between source and destination based on supplied parameters.");
                processRouteRequest(currentDataSet, theStage, routeFinderDialogInView);
            }
        } catch (Exception e) {
            log.error("failed to find routes:", e);
            processRouteRequest(currentDataSet, theStage, routeFinderDialogInView);
        }
    }

    /**
     * pick a route and plot one
     *
     * @param currentDataSet
     * @param theStage
     * @param routeFindingOptions
     * @param origin
     * @param destination
     * @param routeBuilderHelper
     * @param routeGraph
     */
    private void determineRoutesAndPlotOne(DataSetDescriptor currentDataSet,
                                           Stage theStage,
                                           RouteFindingOptions routeFindingOptions,
                                           String origin,
                                           String destination,
                                           RouteBuilderHelper routeBuilderHelper,
                                           RouteGraph routeGraph) {

        log.info("Source and destination stars have a path");

        // find the k shortest paths. We add one because the first is null
        List<String> kShortestPaths = routeGraph.findKShortestPaths(
                origin, destination, routeFindingOptions.getNumberPaths() + 1
        );

        PossibleRoutes possibleRoutes = new PossibleRoutes();
        possibleRoutes.setDesiredPath(String.format("Route %s to %s", origin, destination));

        List<String> pathToPlot = new ArrayList<>(kShortestPaths);
        int i = 1;
        // for each of our paths create a route
        for (String path : pathToPlot) {
            if (path.contains("null")) {
                // this is a dead path
                continue;
            }

            Color color = routeFindingOptions.getColor();
            if (i > 1) {
                color = Color.color(Math.random(), Math.random(), Math.random());
            }

            RouteDescriptor route = routeBuilderHelper.buildPath(
                    origin, destination, Integer.toString(i++),
                    color, routeFindingOptions.getLineWidth(), path);

            route.setDescriptor(currentDataSet);

            RoutingMetric routingMetric = RoutingMetric
                    .builder()
                    .totalLength(route.getTotalLength())
                    .routeDescriptor(route)
                    .path(path)
                    .rank(i - 1)
                    .numberOfSegments(route.getRouteCoordinates().size())
                    .build();
            possibleRoutes.getRoutes().add(routingMetric);
        }

        DisplayAutoRoutesDialog displayAutoRoutesDialog = new DisplayAutoRoutesDialog(possibleRoutes);
        Stage stage = (Stage) displayAutoRoutesDialog.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        stage.toFront();
        Optional<List<RoutingMetric>> optionalRoutingMetrics = displayAutoRoutesDialog.showAndWait();
        if (optionalRoutingMetrics.isPresent()) {
            List<RoutingMetric> selectedRoutingMetrics = optionalRoutingMetrics.get();
            if (selectedRoutingMetrics.size() > 0) {
                log.info("plotting selected routes:{}", selectedRoutingMetrics);
                // plot the routes found
                plot(currentDataSet, selectedRoutingMetrics);
            }
        }
    }

    private List<StarDisplayRecord> prune(List<StarDisplayRecord> starsInView, RouteFindingOptions routeFindingOptions) {
        List<StarDisplayRecord> prunedStars = new ArrayList<>();
        for (StarDisplayRecord starDisplayRecord : starsInView) {
            if (routeFindingOptions.getStarExclusions().contains(starDisplayRecord.getSpectralClass().substring(0, 1))) {
                continue;
            }
            if (routeFindingOptions.getPolityExclusions().contains(starDisplayRecord.getPolity())) {
                continue;
            }
            prunedStars.add(starDisplayRecord);
        }
        return prunedStars;
    }


    /**
     * plot the routes found
     *
     * @param currentDataSet the data descriptor
     * @param routeList      the routes to plot
     */
    private void plot(DataSetDescriptor currentDataSet, List<RoutingMetric> routeList) {
        interstellarSpacePane.plotRouteDescriptors(currentDataSet, routeList);
    }
}
