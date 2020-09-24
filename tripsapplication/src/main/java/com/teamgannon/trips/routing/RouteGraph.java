package com.teamgannon.trips.routing;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.service.model.TransitRoute;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.dot.DOTExporter;

import java.io.StringWriter;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@Slf4j
public class RouteGraph {

    private List<TransitRoute> transitRoutes;

    private Graph<String, DefaultEdge> routingGraph;

    private ConnectivityInspector<String, DefaultEdge> connectivityInspector;

    DijkstraShortestPath<String, DefaultEdge> dijkstraAlg;

    public RouteGraph(List<TransitRoute> transitRoutes) {
        this.transitRoutes = transitRoutes;

        routingGraph = new SimpleGraph<>(DefaultEdge.class);

        for (TransitRoute transitRoute : transitRoutes) {
            StarDisplayRecord source = transitRoute.getSource();
            StarDisplayRecord destination = transitRoute.getTarget();
            routingGraph.addVertex(source.getStarName());
            routingGraph.addVertex(destination.getStarName());
            routingGraph.addEdge(source.getStarName(), destination.getStarName());
        }

        // setup a connectivity inspector
        connectivityInspector = new ConnectivityInspector<>(routingGraph);

        // determine shortest paths
        dijkstraAlg = new DijkstraShortestPath<>(routingGraph);

    }

    public void exportGraphViz() {
        DOTExporter<String, DefaultEdge> exporter =
                new DOTExporter<>();

        exporter.setVertexAttributeProvider((v) -> {
            Map<String, Attribute> map = new LinkedHashMap<>();
            map.put("label", DefaultAttribute.createAttribute(v.toString()));
            return map;
        });
        Writer writer = new StringWriter();
        exporter.exportGraph(routingGraph, writer);
        System.out.println(writer.toString());
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

    public String findShortestPath(String origin, String destination) {
        ShortestPathAlgorithm.SingleSourcePaths<String, DefaultEdge> originPaths = dijkstraAlg.getPaths(origin);
        return originPaths.getPath(destination).toString();
    }

}
