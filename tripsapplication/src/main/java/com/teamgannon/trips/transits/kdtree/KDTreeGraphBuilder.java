package com.teamgannon.trips.transits.kdtree;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.measure.TrackExecutionTime;
import com.teamgannon.trips.routing.model.SparseStarRecord;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Builds JGraphT weighted graphs using KD-Tree spatial indexing for efficient edge discovery.
 * <p>
 * This replaces O(n²) brute-force edge calculation with O(n log n) KD-Tree queries,
 * providing significant speedup for large star datasets.
 * <p>
 * Complexity:
 * <ul>
 *   <li>Tree construction: O(n log n)</li>
 *   <li>Edge discovery: O(n log n) sequential, O(n log n / p) parallel</li>
 *   <li>Total: O(n log n) vs O(n²) brute-force</li>
 * </ul>
 * <p>
 * Usage:
 * <pre>{@code
 * KDTreeGraphBuilder builder = new KDTreeGraphBuilder();
 * Graph<String, DefaultEdge> graph = builder.buildGraph(stars, 0.5, 8.0);
 * // Use with YenKShortestPath or ConnectivityInspector
 * }</pre>
 *
 * @see KDTree3D
 * @see com.teamgannon.trips.routing.automation.RouteGraph
 */
@Slf4j
public class KDTreeGraphBuilder {

    /**
     * Minimum star count to use parallel processing.
     * Below this threshold, sequential processing is faster due to overhead.
     */
    private static final int PARALLEL_THRESHOLD = 500;
    private static final int PARALLEL_EDGE_BATCH_SIZE = 256;

    private final boolean enableParallel;

    /**
     * Creates a builder with parallel processing enabled.
     */
    public KDTreeGraphBuilder() {
        this(true);
    }

    /**
     * Creates a builder with configurable parallel processing.
     *
     * @param enableParallel true to enable parallel processing for large datasets
     */
    public KDTreeGraphBuilder(boolean enableParallel) {
        this.enableParallel = enableParallel;
    }

    // =========================================================================
    // Public API - StarDisplayRecord
    // =========================================================================

    /**
     * Builds a weighted graph from StarDisplayRecord list.
     *
     * @param stars      the stars to include in the graph
     * @param lowerBound minimum edge distance (exclusive)
     * @param upperBound maximum edge distance (inclusive)
     * @return weighted graph with star names as vertices and distances as edge weights
     */
    @TrackExecutionTime
    public @NotNull Graph<String, DefaultEdge> buildGraph(
            @NotNull List<StarDisplayRecord> stars,
            double lowerBound,
            double upperBound) {

        return buildGraphGeneric(
                stars,
                StarDisplayRecord::getStarName,
                StarDisplayRecord::getActualCoordinates,
                lowerBound,
                upperBound
        );
    }

    // =========================================================================
    // Public API - SparseStarRecord
    // =========================================================================

    /**
     * Builds a weighted graph from SparseStarRecord list.
     *
     * @param stars      the stars to include in the graph
     * @param lowerBound minimum edge distance (exclusive)
     * @param upperBound maximum edge distance (inclusive)
     * @return weighted graph with star names as vertices and distances as edge weights
     */
    @TrackExecutionTime
    public @NotNull Graph<String, DefaultEdge> buildGraphFromSparse(
            @NotNull List<SparseStarRecord> stars,
            double lowerBound,
            double upperBound) {

        return buildGraphGeneric(
                stars,
                SparseStarRecord::getStarName,
                SparseStarRecord::getActualCoordinates,
                lowerBound,
                upperBound
        );
    }

    // =========================================================================
    // Generic Implementation
    // =========================================================================

    /**
     * Generic graph builder that works with any star record type.
     *
     * @param stars            the star records
     * @param nameExtractor    function to extract star name
     * @param coordsExtractor  function to extract 3D coordinates
     * @param lowerBound       minimum edge distance (exclusive)
     * @param upperBound       maximum edge distance (inclusive)
     * @param <T>              the star record type
     * @return weighted graph
     */
    public <T> @NotNull Graph<String, DefaultEdge> buildGraphGeneric(
            @NotNull List<T> stars,
            @NotNull Function<T, String> nameExtractor,
            @NotNull Function<T, double[]> coordsExtractor,
            double lowerBound,
            double upperBound) {

        if (stars.isEmpty()) {
            log.debug("Empty star list, returning empty graph");
            return new SimpleWeightedGraph<>(DefaultEdge.class);
        }

        log.debug("Building graph for {} stars (range: {}-{} ly)",
                stars.size(), lowerBound, upperBound);

        // Build KD-Tree - O(n log n)
        long startBuild = System.nanoTime();
        KDTree3D<T> tree = buildTree(stars, coordsExtractor);
        long buildTime = System.nanoTime() - startBuild;
        log.debug("KD-Tree built in {:.2f} ms", buildTime / 1_000_000.0);

        // Build graph with edges - O(n log n)
        long startGraph = System.nanoTime();
        Graph<String, DefaultEdge> graph;
        if (enableParallel && stars.size() >= PARALLEL_THRESHOLD) {
            graph = buildGraphParallel(tree, stars, nameExtractor, coordsExtractor, lowerBound, upperBound);
        } else {
            graph = buildGraphSequential(tree, stars, nameExtractor, coordsExtractor, lowerBound, upperBound);
        }
        long graphTime = System.nanoTime() - startGraph;

        log.debug("Graph built in {:.2f} ms - {} vertices, {} edges",
                graphTime / 1_000_000.0,
                graph.vertexSet().size(),
                graph.edgeSet().size());

        return graph;
    }

    // =========================================================================
    // Private Implementation
    // =========================================================================

    private <T> @NotNull KDTree3D<T> buildTree(
            @NotNull List<T> stars,
            @NotNull Function<T, double[]> coordsExtractor) {

        List<KDPoint<T>> points = stars.stream()
                .map(star -> new KDPoint<>(coordsExtractor.apply(star), star))
                .collect(Collectors.toList());
        return new KDTree3D<>(points);
    }

    private <T> @NotNull Graph<String, DefaultEdge> buildGraphSequential(
            @NotNull KDTree3D<T> tree,
            @NotNull List<T> stars,
            @NotNull Function<T, String> nameExtractor,
            @NotNull Function<T, double[]> coordsExtractor,
            double lowerBound,
            double upperBound) {

        Graph<String, DefaultEdge> graph = new SimpleWeightedGraph<>(DefaultEdge.class);
        Map<T, Integer> indexByStar = buildIdentityIndex(stars);

        // Add all vertices first
        for (T star : stars) {
            graph.addVertex(nameExtractor.apply(star));
        }

        // Find and add edges
        for (int sourceIndex = 0; sourceIndex < stars.size(); sourceIndex++) {
            for (EdgeData edge : findEdgesForSource(
                    tree, stars, sourceIndex, indexByStar, nameExtractor, coordsExtractor, lowerBound, upperBound)) {
                addEdge(graph, edge.source, edge.target, edge.distance);
            }
        }

        return graph;
    }

    private <T> @NotNull Graph<String, DefaultEdge> buildGraphParallel(
            @NotNull KDTree3D<T> tree,
            @NotNull List<T> stars,
            @NotNull Function<T, String> nameExtractor,
            @NotNull Function<T, double[]> coordsExtractor,
            double lowerBound,
            double upperBound) {

        Graph<String, DefaultEdge> graph = new SimpleWeightedGraph<>(DefaultEdge.class);
        Map<T, Integer> indexByStar = buildIdentityIndex(stars);

        // Add all vertices first (sequential - graph not thread-safe for vertex addition)
        for (T star : stars) {
            graph.addVertex(nameExtractor.apply(star));
        }

        // Discover edges in bounded parallel batches, then flush sequentially because graph mutation is not thread-safe.
        for (int start = 0; start < stars.size(); start += PARALLEL_EDGE_BATCH_SIZE) {
            int batchStart = start;
            int batchEnd = Math.min(batchStart + PARALLEL_EDGE_BATCH_SIZE, stars.size());
            List<EdgeData> batchEdges = IntStream.range(batchStart, batchEnd)
                    .parallel()
                    .mapToObj(sourceIndex -> findEdgesForSource(
                            tree, stars, sourceIndex, indexByStar, nameExtractor, coordsExtractor, lowerBound, upperBound))
                    .flatMap(List::stream)
                    .collect(Collectors.toList());

            for (EdgeData edge : batchEdges) {
                addEdge(graph, edge.source, edge.target, edge.distance);
            }
        }

        return graph;
    }

    private <T> @NotNull Map<T, Integer> buildIdentityIndex(@NotNull List<T> stars) {
        Map<T, Integer> indexByStar = new IdentityHashMap<>(stars.size());
        for (int i = 0; i < stars.size(); i++) {
            indexByStar.put(stars.get(i), i);
        }
        return indexByStar;
    }

    private <T> @NotNull List<EdgeData> findEdgesForSource(
            @NotNull KDTree3D<T> tree,
            @NotNull List<T> stars,
            int sourceIndex,
            @NotNull Map<T, Integer> indexByStar,
            @NotNull Function<T, String> nameExtractor,
            @NotNull Function<T, double[]> coordsExtractor,
            double lowerBound,
            double upperBound) {

        T source = stars.get(sourceIndex);
        String sourceName = nameExtractor.apply(source);
        double[] sourceCoords = coordsExtractor.apply(source);
        List<KDPoint<T>> neighbors = tree.rangeSearch(sourceCoords, upperBound);
        List<EdgeData> edges = new ArrayList<>();

        for (KDPoint<T> neighbor : neighbors) {
            T target = neighbor.data();
            Integer targetIndex = indexByStar.get(target);
            if (targetIndex == null || targetIndex <= sourceIndex) {
                continue;
            }

            double distance = neighbor.distanceTo(sourceCoords);
            if (distance > lowerBound) {
                edges.add(new EdgeData(sourceName, nameExtractor.apply(target), distance));
            }
        }

        return edges;
    }

    private void addEdge(@NotNull Graph<String, DefaultEdge> graph,
                         @NotNull String source,
                         @NotNull String target,
                         double distance) {
        DefaultEdge edge = graph.addEdge(source, target);
        if (edge != null) {
            graph.setEdgeWeight(edge, distance);
        }
        // edge == null means duplicate, which we skip silently
    }

    /**
     * Internal record for collecting edges during parallel processing.
     */
    private record EdgeData(String source, String target, double distance) {}
}
