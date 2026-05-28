package com.teamgannon.trips.routing;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.routing.model.RouteFindingOptions;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

import static com.teamgannon.trips.routing.RoutingConstants.KDTREE_THRESHOLD;
import static com.teamgannon.trips.routing.RoutingConstants.MAX_ROUTE_GRAPH_ESTIMATED_EDGES;
import static com.teamgannon.trips.routing.RoutingConstants.ROUTE_GRAPH_DENSITY_SAMPLE_SIZE;

/**
 * Preflight guard for route settings that would create impractically dense
 * graphs. It estimates edge count before the KD-tree graph builder allocates
 * JGraphT edges.
 */
public final class RouteGraphDensityGuard {

    private RouteGraphDensityGuard() {
    }

    public static @NotNull Optional<String> validate(@NotNull List<StarDisplayRecord> stars,
                                                      @NotNull RouteFindingOptions options) {
        if (stars.size() <= KDTREE_THRESHOLD) {
            return Optional.empty();
        }

        GraphDensityEstimate estimate = estimate(stars, options.getLowerBound(), options.getUpperBound());
        if (estimate.estimatedEdges() <= MAX_ROUTE_GRAPH_ESTIMATED_EDGES) {
            return Optional.empty();
        }

        return Optional.of(
                """
                Route settings would create an estimated %,d graph edges across %,d stars \
                (average degree %.1f). Tighten the upper bound, raise the lower bound, \
                or exclude more stars before route finding. Guardrail maximum is %,d estimated edges.\
                """.formatted(
                        estimate.estimatedEdges(),
                        estimate.starCount(),
                        estimate.averageDegree(),
                        MAX_ROUTE_GRAPH_ESTIMATED_EDGES));
    }

    public static @NotNull GraphDensityEstimate estimate(@NotNull List<StarDisplayRecord> stars,
                                                          double lowerBound,
                                                          double upperBound) {
        int starCount = stars.size();
        if (starCount < 2 || upperBound <= lowerBound) {
            return new GraphDensityEstimate(starCount, 0, 0.0, 0);
        }

        int sampleCount = Math.min(starCount, ROUTE_GRAPH_DENSITY_SAMPLE_SIZE);
        long sampledDirectedEdges = 0;
        for (int sampleIndex = 0; sampleIndex < sampleCount; sampleIndex++) {
            int sourceIndex = evenlySpacedIndex(sampleIndex, sampleCount, starCount);
            StarDisplayRecord source = stars.get(sourceIndex);
            for (int targetIndex = 0; targetIndex < starCount; targetIndex++) {
                if (targetIndex == sourceIndex) {
                    continue;
                }
                if (isWithinBounds(source, stars.get(targetIndex), lowerBound, upperBound)) {
                    sampledDirectedEdges++;
                }
            }
        }

        double averageDegree = sampledDirectedEdges / (double) sampleCount;
        long estimatedEdges = Math.round((averageDegree * starCount) / 2.0);
        long maxPossibleEdges = ((long) starCount * (starCount - 1)) / 2L;
        if (estimatedEdges > maxPossibleEdges) {
            estimatedEdges = maxPossibleEdges;
        }

        return new GraphDensityEstimate(starCount, sampleCount, averageDegree, estimatedEdges);
    }

    private static int evenlySpacedIndex(int sampleIndex, int sampleCount, int starCount) {
        if (sampleCount <= 1) {
            return 0;
        }
        return (int) Math.round(sampleIndex * ((starCount - 1) / (double) (sampleCount - 1)));
    }

    private static boolean isWithinBounds(StarDisplayRecord source,
                                          StarDisplayRecord target,
                                          double lowerBound,
                                          double upperBound) {
        double[] sourceCoords = source.getActualCoordinates();
        double[] targetCoords = target.getActualCoordinates();
        if (!hasUsableCoordinates(sourceCoords) || !hasUsableCoordinates(targetCoords)) {
            return false;
        }

        double dx = sourceCoords[0] - targetCoords[0];
        double dy = sourceCoords[1] - targetCoords[1];
        double dz = sourceCoords[2] - targetCoords[2];
        double distanceSquared = dx * dx + dy * dy + dz * dz;
        return distanceSquared > lowerBound * lowerBound
                && distanceSquared <= upperBound * upperBound;
    }

    private static boolean hasUsableCoordinates(double[] coordinates) {
        return coordinates != null
                && coordinates.length >= 3
                && Double.isFinite(coordinates[0])
                && Double.isFinite(coordinates[1])
                && Double.isFinite(coordinates[2]);
    }

    public record GraphDensityEstimate(
            int starCount,
            int sampleCount,
            double averageDegree,
            long estimatedEdges) {
    }
}
