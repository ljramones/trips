package com.teamgannon.trips.routing;

import com.teamgannon.trips.dialogs.search.model.DistanceRoutes;
import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.graphics.panes.InterstellarSpacePane;
import com.teamgannon.trips.service.StarMeasurementService;
import com.teamgannon.trips.service.model.TransitRoute;
import javafx.scene.paint.Color;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

@Slf4j
public class RouteFinder {


    private final InterstellarSpacePane interstellarSpacePane;

    public RouteFinder(InterstellarSpacePane interstellarSpacePane) {
        this.interstellarSpacePane = interstellarSpacePane;
    }


    public void startRouteLocation() {
        RouteFinderDialog routeFinderDialog = new RouteFinderDialog();
        Optional<RouteFindingOptions> routeFindingOptionsOptional = routeFinderDialog.showAndWait();
        if (routeFindingOptionsOptional.isPresent()) {
            RouteFindingOptions routeFindingOptions = routeFindingOptionsOptional.get();
            if (routeFindingOptions.isSelected()) {
                try {
                    log.info("find route between stars");
                    String origin = routeFindingOptions.getOriginStar();
                    String destination = routeFindingOptions.getDestinationStar();
                    List<StarDisplayRecord> starsInView = interstellarSpacePane.getCurrentStarsInView();
                    RouteBuilderHelper routeBuilderHelper = new RouteBuilderHelper(starsInView);
                    if (!routeBuilderHelper.has(origin)) {
                        showErrorAlert("Route Finder", "The start star is not in the display");
                        return;
                    }

                    if (!routeBuilderHelper.has(destination)) {
                        showErrorAlert("Route Finder", "The destination star is not in the display");
                        return;
                    }

                    StarMeasurementService starMeasurementService = new StarMeasurementService();
                    DistanceRoutes distanceRoutes = DistanceRoutes
                            .builder()
                            .upperDistance(routeFindingOptions.getUpperBound())
                            .lowerDistance((routeFindingOptions.getLowerBound()))
                            .build();
                    List<TransitRoute> transitRoutes = starMeasurementService.calculateDistances(distanceRoutes, starsInView);
                    log.info("transits calculated");

                    RouteGraph routeGraph = new RouteGraph(transitRoutes);

                    if (routeGraph.isConnected(origin, destination)) {
                        log.info("Source and destination stars have a path");
//                        Set<String> connectStars = routeGraph.getConnectedTo(origin);
//                        log.info("Connected set is{}", connectStars);
//                        String shortestPath = routeGraph.findShortestPath(origin, destination);
//                        System.out.println(shortestPath);

                        List<String> kShortestPaths = routeGraph.findKShortestPaths(
                                origin, destination, routeFindingOptions.getNumberPaths() + 1);
                        kShortestPaths.forEach(System.out::println);

                        List<RouteDescriptor> routeList = new ArrayList<>();
                        List<String> pathToPlot = new ArrayList<>(kShortestPaths);
                        int i = 1;
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
                        }

                        plot(routeList);

                        kShortestPaths.forEach(System.out::println);

                    } else {
                        log.error("Source and destination stars do not have a path");
                        showErrorAlert("Route Finder form A to B", "Unable to find a ");
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

    private void plot(List<RouteDescriptor> routeList) {
        interstellarSpacePane.plotRouteDesciptors(routeList);
    }

}
