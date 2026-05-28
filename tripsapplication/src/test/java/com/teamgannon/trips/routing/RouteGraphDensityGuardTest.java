package com.teamgannon.trips.routing;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.routing.model.RouteFindingOptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RouteGraphDensityGuardTest {

    @Test
    @DisplayName("sparse route settings pass")
    void sparseRouteSettingsPass() {
        List<StarDisplayRecord> stars = lineOfStars(200, 10.0);
        RouteFindingOptions options = RouteFindingOptions.builder()
                .lowerBound(0.0)
                .upperBound(1.0)
                .build();

        assertTrue(RouteGraphDensityGuard.validate(stars, options).isEmpty());
    }

    @Test
    @DisplayName("dense route settings fail before graph construction")
    void denseRouteSettingsFail() {
        List<StarDisplayRecord> stars = lineOfStars(2_000, 0.001);
        RouteFindingOptions options = RouteFindingOptions.builder()
                .lowerBound(0.0)
                .upperBound(10.0)
                .build();

        String error = RouteGraphDensityGuard.validate(stars, options).orElseThrow();

        assertTrue(error.contains("estimated"));
        assertTrue(error.contains("graph edges"));
        assertTrue(RouteGraphDensityGuard.estimate(stars, 0.0, 10.0).estimatedEdges()
                > RoutingConstants.MAX_ROUTE_GRAPH_ESTIMATED_EDGES);
    }

    @Test
    @DisplayName("estimate is bounded by complete graph edge count")
    void estimateIsBoundedByCompleteGraphEdgeCount() {
        List<StarDisplayRecord> stars = lineOfStars(200, 0.001);

        RouteGraphDensityGuard.GraphDensityEstimate estimate =
                RouteGraphDensityGuard.estimate(stars, 0.0, 10.0);

        assertEquals(19_900, estimate.estimatedEdges());
        assertEquals(199.0, estimate.averageDegree(), 0.0001);
    }

    private static List<StarDisplayRecord> lineOfStars(int count, double spacing) {
        List<StarDisplayRecord> stars = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            StarDisplayRecord star = new StarDisplayRecord();
            star.setStarName("Star-" + i);
            star.setRecordId("star-" + i);
            star.setActualCoordinates(new double[]{i * spacing, 0.0, 0.0});
            stars.add(star);
        }
        return stars;
    }
}
