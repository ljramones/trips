package com.teamgannon.trips.routing.automation;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.routing.model.SparseStarRecord;
import com.teamgannon.trips.routing.model.SparseTransit;
import com.teamgannon.trips.support.LogExecutionTime;
import com.teamgannon.trips.transits.TransitRoute;
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
     * the ctor
     */
    public RouteGraph() {

        routingGraph = new SimpleWeightedGraph<>(DefaultEdge.class);
    }

    public void calculateGraphForTransit(List<TransitRoute> transitRoutes) {
        for (TransitRoute transitRoute : transitRoutes) {
            StarDisplayRecord source = transitRoute.getSource();
            StarDisplayRecord destination = transitRoute.getTarget();
            routingGraph.addVertex(source.getStarName());
            routingGraph.addVertex(destination.getStarName());
            DefaultEdge e1 = routingGraph.addEdge(source.getStarName(), destination.getStarName());
            routingGraph.setEdgeWeight(e1, transitRoute.getDistance());
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

    @LogExecutionTime
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
