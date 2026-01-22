package com.teamgannon.trips.routing.automation;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.routing.model.SparseStarRecord;
import com.teamgannon.trips.routing.model.SparseTransit;
import com.teamgannon.trips.transits.TransitRoute;
import com.teamgannon.trips.transits.kdtree.KDTreeGraphBuilder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.alg.shortestpath.YenKShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Slf4j
public class RouteGraph {

    /**
     * the graph constructed
     */
    private final @NotNull Graph<String, DefaultEdge> routingGraph;

    /**
     * our connectivity graph
     */
    private ConnectivityInspector<String, DefaultEdge> connectivityInspector;

    /**
     * our Yen k shortest paths
     */
    private YenKShortestPath<String, DefaultEdge> kShortedPaths;


    /**
     * Creates an empty RouteGraph.
     */
    public RouteGraph() {
        routingGraph = new SimpleWeightedGraph<>(DefaultEdge.class);
    }

    /**
     * Creates a RouteGraph from a pre-built graph.
     * <p>
     * Use this constructor when the graph has been built externally,
     * e.g., by {@link com.teamgannon.trips.transits.kdtree.KDTreeGraphBuilder}.
     *
     * @param preBuiltGraph the pre-built weighted graph
     */
    public RouteGraph(@NotNull Graph<String, DefaultEdge> preBuiltGraph) {
        this.routingGraph = preBuiltGraph;
        calculateGraphPaths();
    }

    // =========================================================================
    // Static Factory Methods - KD-Tree Based Construction
    // =========================================================================

    /**
     * Creates a RouteGraph using KD-Tree spatial indexing for efficient edge discovery.
     * <p>
     * This is O(n log n) compared to O(n²) brute-force transit calculation,
     * providing significant speedup for large star datasets.
     *
     * @param stars      list of stars to include in the graph
     * @param lowerBound minimum edge distance (exclusive)
     * @param upperBound maximum edge distance (inclusive)
     * @return a fully constructed RouteGraph ready for pathfinding
     */
    public static @NotNull RouteGraph buildWithKDTree(
            @NotNull List<StarDisplayRecord> stars,
            double lowerBound,
            double upperBound) {

        KDTreeGraphBuilder builder = new KDTreeGraphBuilder();
        Graph<String, DefaultEdge> graph = builder.buildGraph(stars, lowerBound, upperBound);
        return new RouteGraph(graph);
    }

    /**
     * Creates a RouteGraph from SparseStarRecords using KD-Tree spatial indexing.
     *
     * @param stars      list of sparse star records
     * @param lowerBound minimum edge distance (exclusive)
     * @param upperBound maximum edge distance (inclusive)
     * @return a fully constructed RouteGraph ready for pathfinding
     */
    public static @NotNull RouteGraph buildWithKDTreeFromSparse(
            @NotNull List<SparseStarRecord> stars,
            double lowerBound,
            double upperBound) {

        KDTreeGraphBuilder builder = new KDTreeGraphBuilder();
        Graph<String, DefaultEdge> graph = builder.buildGraphFromSparse(stars, lowerBound, upperBound);
        return new RouteGraph(graph);
    }

    // =========================================================================
    // Instance Methods - Transit-Based Construction (Legacy)
    // =========================================================================

    public void calculateGraphForTransit(List<TransitRoute> transitRoutes) {
        for (TransitRoute transitRoute : transitRoutes) {
            StarDisplayRecord source = transitRoute.getSource();
            StarDisplayRecord destination = transitRoute.getTarget();

            // Skip self-loops
            if (source.getStarName().equals(destination.getStarName())) {
                log.debug("Skipping self-loop transit for star: {}", source.getStarName());
                continue;
            }

            routingGraph.addVertex(source.getStarName());
            routingGraph.addVertex(destination.getStarName());
            DefaultEdge e1 = routingGraph.addEdge(source.getStarName(), destination.getStarName());

            // addEdge returns null if edge already exists (duplicate transit)
            if (e1 != null) {
                routingGraph.setEdgeWeight(e1, transitRoute.getDistance());
            } else {
                log.debug("Duplicate edge skipped: {} -> {}", source.getStarName(), destination.getStarName());
            }
        }

        // setup a connectivity inspector
        calculateGraphPaths();
    }

    public Double findEdges(String from, String to) {
        DefaultEdge edge = routingGraph.getEdge(from.trim(), to);
        if (edge != null) {
            return routingGraph.getEdgeWeight(edge);
        } else {
            return null;
        }
    }


    public boolean calculateGraphForSparseTransits(List<SparseTransit> sparseTransitList) {

        for (SparseTransit transitRoute : sparseTransitList) {
            try {
                SparseStarRecord source = transitRoute.getSource();
                SparseStarRecord destination = transitRoute.getTarget();
                if (source.getStarName().equals(destination.getStarName())) {
                    log.info("hmmm, this will form a loop - bad -> skip");
                    continue;
                }
                routingGraph.addVertex(source.getStarName());
                routingGraph.addVertex(destination.getStarName());
                DefaultEdge e1 = routingGraph.addEdge(source.getStarName(), destination.getStarName());
                if (e1 != null) {
                    routingGraph.setEdgeWeight(e1, transitRoute.getDistance());
                } else {
                    log.error("edge is null!?");
                }
            } catch (Exception e) {
                log.error("caught error on creating graph: " + e.getMessage());
                return false;
            }
        }
        calculateGraphPaths();
        return true;

    }

    private void calculateGraphPaths() {
        // setup a connectivity inspector
        connectivityInspector = new ConnectivityInspector<>(routingGraph);

        // determine k shortest paths
        kShortedPaths = new YenKShortestPath<>(routingGraph);
    }


    /**
     * tells us whether a path exists between the origin star and destination star
     *
     * @param originStar      the star we start at
     * @param destinationStar the star we want to go to
     * @return true if a path exists, false otherwise
     */
    public boolean isConnected(String originStar, String destinationStar) {
        return connectivityInspector.pathExists(originStar, destinationStar);
    }

    /**
     * find the k shortest paths
     *
     * @param source      the start
     * @param destination the destination
     * @param kPaths      the number of paths to find
     * @return the lsit of discovered paths
     */
    public List<String> findKShortestPaths(String source, String destination, int kPaths) {
        List<GraphPath<String, DefaultEdge>> yenKShort = kShortedPaths.getPaths(source, destination, kPaths);
        return yenKShort.stream().map(Object::toString).collect(Collectors.toList());
    }

}
