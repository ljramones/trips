package com.teamgannon.trips.routing;

import com.teamgannon.trips.dialogs.search.model.DistanceRoutes;
import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.graphics.panes.InterstellarSpacePane;
import com.teamgannon.trips.service.StarMeasurementService;
import com.teamgannon.trips.service.model.TransitRoute;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

@Slf4j
public class RouteFinder {

    private Stage stage;
    /**
     * used to plot the routes found
     */
    private final InterstellarSpacePane interstellarSpacePane;

    /**
     * the constructor
     *
     * @param interstellarSpacePane the graphics pane to plot
     */
    public RouteFinder(Stage stage,
                       InterstellarSpacePane interstellarSpacePane) {
        this.stage = stage;
        this.interstellarSpacePane = interstellarSpacePane;
    }

    /**
     * start the location of routes
     */
    public void startRouteLocation() {
        RouteFinderDialog routeFinderDialog = new RouteFinderDialog();

        // get the route location parameters from the dialog
        Optional<RouteFindingOptions> routeFindingOptionsOptional = routeFinderDialog.showAndWait();
        if (routeFindingOptionsOptional.isPresent()) {
            RouteFindingOptions routeFindingOptions = routeFindingOptionsOptional.get();

            // if we actually selected the option to route then do it
            if (routeFindingOptions.isSelected()) {
                try {
                    log.info("find route between stars");

                    // setup our initials
                    String origin = routeFindingOptions.getOriginStar();
                    String destination = routeFindingOptions.getDestinationStar();
                    List<StarDisplayRecord> starsInView = interstellarSpacePane.getCurrentStarsInView();
                    RouteBuilderHelper routeBuilderHelper = new RouteBuilderHelper(starsInView);

                    // check if the start star is present
                    if (!routeBuilderHelper.has(origin)) {
                        showErrorAlert("Route Finder", "The start star is not in the display");
                        return;
                    }

                    // check if the destination star is present
                    if (!routeBuilderHelper.has(destination)) {
                        showErrorAlert("Route Finder", "The destination star is not in the display");
                        return;
                    }

                    // calculate the transits based on upper and lower bounds
                    StarMeasurementService starMeasurementService = new StarMeasurementService();
                    DistanceRoutes distanceRoutes = DistanceRoutes
                            .builder()
                            .upperDistance(routeFindingOptions.getUpperBound())
                            .lowerDistance((routeFindingOptions.getLowerBound()))
                            .build();
                    List<TransitRoute> transitRoutes = starMeasurementService.calculateDistances(distanceRoutes, starsInView);
                    log.info("transits calculated");

                    // create a graph based on the transits available
                    RouteGraph routeGraph = new RouteGraph(transitRoutes);

                    // check if the origin star and destination star are connected to each other
                    if (routeGraph.isConnected(origin, destination)) {
                        log.info("Source and destination stars have a path");

                        // find the k shortest paths. We add one because the first is null
                        List<String> kShortestPaths = routeGraph.findKShortestPaths(
                                origin, destination, routeFindingOptions.getNumberPaths() + 1);
                        kShortestPaths.forEach(System.out::println);

                        PossibleRoutes possibleRoutes = new PossibleRoutes();
                        possibleRoutes.setDesiredPath(String.format("Route %s to %s", origin, destination));

                        List<RouteDescriptor> routeList = new ArrayList<>();
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

                            routeList.add(route);

                            RoutingMetric routingMetric = RoutingMetric
                                    .builder()
                                    .totalLength(route.getTotalLength())
                                    .routeDescriptor(route)
                                    .path(path)
                                    .rank(i - 1)
                                    .numberOfSegments(route.getLineSegments().size())
                                    .build();
                            possibleRoutes.getRoutes().add(routingMetric);
                        }


                        DisplayAutoRoutesDialog displayAutoRoutesDialog = new DisplayAutoRoutesDialog(stage, possibleRoutes);
                        Optional<List<RoutingMetric>> optionalRoutingMetrics = displayAutoRoutesDialog.showAndWait();
                        if (optionalRoutingMetrics.isPresent()) {
                            List<RoutingMetric> selectedRoutingMetrics = optionalRoutingMetrics.get();
                            if (selectedRoutingMetrics.size() > 0) {
                                log.info("plotting selected routes:{}", selectedRoutingMetrics);
                                // plot the routes found
                                plot(selectedRoutingMetrics);

                            }
                        }

                    } else {
                        log.error("Source and destination stars do not have a path");
                        showErrorAlert("Route Finder form A to B",
                                "Unable to find a route between source and destination based on supplied parameters.");
                    }

//                    routeGraph.exportGraphViz();
                    log.info("export complete");
                } catch (Exception e) {
                    log.error("failed to find routes:", e);
                    e.printStackTrace();
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
        interstellarSpacePane.plotRouteDesciptors(routeList);
    }
}
