package com.teamgannon.trips.routing;

import com.teamgannon.trips.config.application.StarDisplayPreferences;
import com.teamgannon.trips.dialogs.search.model.DistanceRoutes;
import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.graphics.panes.InterstellarSpacePane;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.StarMeasurementService;
import com.teamgannon.trips.service.model.TransitRoute;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

@Slf4j
public class RouteFinderDataset {

    /**
     * used to plot the routes found
     */
    private final InterstellarSpacePane interstellarSpacePane;

    public RouteFinderDataset(InterstellarSpacePane interstellarSpacePane) {
        this.interstellarSpacePane = interstellarSpacePane;
    }

    /**
     * start the location of routes
     */
    public void startRouteLocation(DataSetDescriptor currentDataset,
                                   DatabaseManagementService databaseManagementService,
                                   @NotNull StarDisplayPreferences starDisplayPreferences) {

        RouteFinderDialogInDataSet routeFinderDialogInView = new RouteFinderDialogInDataSet(
                currentDataset.getDataSetName(),
                databaseManagementService);
        Stage theStage = (Stage) routeFinderDialogInView.getDialogPane().getScene().getWindow();
        theStage.setAlwaysOnTop(true);
        theStage.toFront();

        Optional<RouteFindingOptions> routeFindingOptionsOptional = routeFinderDialogInView.showAndWait();
        if (routeFindingOptionsOptional.isPresent()) {
            log.info("selected");

            RouteFindingOptions routeFindingOptions = routeFindingOptionsOptional.get();

            String origin = routeFindingOptions.getOriginStar();
            String destination = routeFindingOptions.getDestinationStar();

            // if we actually selected the option to route then do it
            if (routeFindingOptions.isSelected()) {

                List<StarObject> starObjects = databaseManagementService.getFromDatasetWithinLimit(currentDataset, routeFindingOptions.getMaxDistance());

                Map<String, StarObject> astroMap = starObjects.stream().collect(Collectors.toMap(StarObject::getDisplayName, astrographicObject -> astrographicObject, (a, b) -> b));

                List<StarDisplayRecord> starsInView = starObjects.stream().map(
                        astrographicObject -> StarDisplayRecord.fromAstrographicObject(astrographicObject, starDisplayPreferences))
                        .filter(Objects::nonNull).collect(Collectors.toList());

                RouteBuilderHelper routeBuilderHelper = new RouteBuilderHelper(starsInView);

                // calculate the transits based on upper and lower bounds
                StarMeasurementService starMeasurementService = new StarMeasurementService();
                DistanceRoutes distanceRoutes = DistanceRoutes
                        .builder()
                        .upperDistance(routeFindingOptions.getUpperBound())
                        .lowerDistance((routeFindingOptions.getLowerBound()))
                        .build();
                List<TransitRoute> transitRoutes = starMeasurementService.calculateDistances(distanceRoutes, starsInView);

                log.info("transits calculated");
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


                    DisplayAutoRoutesDialog displayAutoRoutesDialog = new DisplayAutoRoutesDialog(theStage, possibleRoutes);
                    Stage stage = (Stage) displayAutoRoutesDialog.getDialogPane().getScene().getWindow();
                    stage.setAlwaysOnTop(true);
                    stage.toFront();
                    Optional<List<RoutingMetric>> optionalRoutingMetrics = displayAutoRoutesDialog.showAndWait();
                    if (optionalRoutingMetrics.isPresent()) {
                        List<RoutingMetric> selectedRoutingMetrics = optionalRoutingMetrics.get();
                        if (selectedRoutingMetrics.size() > 0) {
                            log.info("plotting selected routes:{}", selectedRoutingMetrics);
                            List<StarObject> plotList = gatherStars(possibleRoutes, astroMap);

                            // plot the stars and routes found
                            plot(selectedRoutingMetrics, plotList);

                        }
                    }

                } else {
                    log.error("Source and destination stars do not have a path");
                    showErrorAlert("Route Finder form A to B",
                            "Unable to find a route between source and destination based on supplied parameters.");
                }

            }
        }

    }

    private List<StarObject> gatherStars(PossibleRoutes possibleRoutes, Map<String, StarObject> astroMap) {
        List<StarObject> starList = new ArrayList<>();
        possibleRoutes
                .getRoutes()
                .stream()
                .map(RoutingMetric::getRouteDescriptor)
                .map(RouteDescriptor::getNameList)
                .forEach(nameList -> nameList.stream()
                        .map(astroMap::get)
                        .forEach(starList::add));
        return starList;
    }

    /**
     * plot the routes found
     *
     * @param routeList the routes to plot
     * @param plotList  the list of stars to plot
     */
    private void plot(List<RoutingMetric> routeList, List<StarObject> plotList) {
//        interstellarSpacePane.plotRouteDescriptors(routeList);
        log.info(routeList.toString());
        log.info("# of stars= " + plotList.size());
    }


}
