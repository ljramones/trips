package com.teamgannon.trips.routing;

import com.teamgannon.trips.dialogs.search.model.DistanceRoutes;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.graphics.panes.InterstellarSpacePane;
import com.teamgannon.trips.service.StarMeasurementService;
import com.teamgannon.trips.service.model.TransitRoute;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.Set;

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
                log.info("find route between stars");
                String origin = routeFindingOptions.getOriginStar();
                String destination = routeFindingOptions.getDestinationStar();
                List<StarDisplayRecord> starsInView = interstellarSpacePane.getCurrentStarsInView();

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
                    Set<String> connectStars = routeGraph.getConnectedTo(origin);
                    log.info("Connected set is{}", connectStars);
                } else {
                    log.error("Source and destination stars do not have a path");
                }

                String shortestPath = routeGraph.findShortestPath(origin, destination);
                System.out.println(shortestPath);

                routeGraph.exportGraphViz();
                log.info("export complete");


            }
        }

    }


}
