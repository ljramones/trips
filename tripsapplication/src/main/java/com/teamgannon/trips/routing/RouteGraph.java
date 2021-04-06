package com.teamgannon.trips.routing;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.transits.TransitRoute;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.alg.shortestpath.YenKShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.dot.DOTExporter;

import java.io.StringWriter;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Slf4j
public class RouteGraph {

    /**
     * our discovered transits
     */
    private final @NotNull List<TransitRoute> transitRoutes;

    /**
     * the graph constructed
     */
    private final @NotNull Graph<String, DefaultEdge> routingGraph;

    /**
     * our connectivity graph
     */
    private final @NotNull ConnectivityInspector<String, DefaultEdge> connectivityInspector;

    /**
     * our shortest path graph
     */
    private final @NotNull DijkstraShortestPath<String, DefaultEdge> dijkstraAlg;

    /**
     * our Yen k shortest paths
     */
    private final @NotNull YenKShortestPath<String, DefaultEdge> kShortedPaths;


    /**
     * the ctor
     *
     * @param transitRoutes the transits to map to a graph
     */
    public RouteGraph(@NotNull List<TransitRoute> transitRoutes) {
        this.transitRoutes = transitRoutes;

        routingGraph = new SimpleWeightedGraph<>(DefaultEdge.class);

        for (TransitRoute transitRoute : transitRoutes) {
            StarDisplayRecord source = transitRoute.getSource();
            StarDisplayRecord destination = transitRoute.getTarget();
            routingGraph.addVertex(source.getStarName());
            routingGraph.addVertex(destination.getStarName());
            DefaultEdge e1 = routingGraph.addEdge(source.getStarName(), destination.getStarName());
            routingGraph.setEdgeWeight(e1, transitRoute.getDistance());
        }

        // setup a connectivity inspector
        connectivityInspector = new ConnectivityInspector<>(routingGraph);

        // determine shortest paths
        dijkstraAlg = new DijkstraShortestPath<>(routingGraph);

        kShortedPaths = new YenKShortestPath<>(routingGraph);

    }

    /**
     * export our grah in GraphViz format for plotting
     */
    public void exportGraphViz() {
        DOTExporter<String, DefaultEdge> exporter =
                new DOTExporter<>();

        exporter.setVertexAttributeProvider((v) -> {
            Map<String, Attribute> map = new LinkedHashMap<>();
            map.put("label", DefaultAttribute.createAttribute(v));
            return map;
        });
        Writer writer = new StringWriter();
        exporter.exportGraph(routingGraph, writer);
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
     * get the connected set from the origin point
     *
     * @param vertexToLook the vertex to find connectivity
     * @return the set of connections
     */
    public Set<String> getConnectedTo(String vertexToLook) {
        return connectivityInspector.connectedSetOf(vertexToLook);
    }

    /**
     * find the shortest path
     *
     * @param origin      the star
     * @param destination the destination
     * @return the path found
     */
    public String findShortestPath(@NotNull String origin, String destination) {
        ShortestPathAlgorithm.SingleSourcePaths<String, DefaultEdge> originPaths = dijkstraAlg.getPaths(origin);
        return originPaths.getPath(destination).toString();
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
