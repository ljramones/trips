package com.teamgannon.trips.benchmark;

import com.teamgannon.trips.routing.model.SparseStarRecord;
import com.teamgannon.trips.transits.kdtree.KDTreeGraphBuilder;
import org.jgrapht.Graph;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.alg.shortestpath.YenKShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * JMH benchmarks for routing and graph algorithms.
 *
 * Run with:
 *   ./mvnw-java25.sh test -Pbenchmark -Dbenchmark.class=RoutingBenchmark
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 3, time = 1)
@Fork(1)
public class RoutingBenchmark {

    private List<SparseStarRecord> stars1000;
    private List<SparseStarRecord> stars5000;
    private List<SparseStarRecord> stars10000;

    private Graph<String, DefaultWeightedEdge> graph1000;
    private Graph<String, DefaultWeightedEdge> graph5000;
    private Graph<String, DefaultWeightedEdge> graph10000;

    private static final double MAX_JUMP_DISTANCE = 10.0; // light years
    private Random random = new Random(12345);

    @Setup(Level.Trial)
    public void setup() {
        // Generate synthetic star fields
        stars1000 = generateStarField(1000, 50.0);  // 50 ly radius
        stars5000 = generateStarField(5000, 100.0); // 100 ly radius
        stars10000 = generateStarField(10000, 150.0); // 150 ly radius

        // Pre-build graphs for pathfinding benchmarks
        graph1000 = buildGraph(stars1000, MAX_JUMP_DISTANCE);
        graph5000 = buildGraph(stars5000, MAX_JUMP_DISTANCE);
        graph10000 = buildGraph(stars10000, MAX_JUMP_DISTANCE);
    }

    private List<SparseStarRecord> generateStarField(int count, double radius) {
        List<SparseStarRecord> stars = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            SparseStarRecord star = new SparseStarRecord();
            star.setRecordId("STAR-" + i);
            star.setStarName("Star " + i);

            // Random position in spherical volume
            double r = radius * Math.cbrt(random.nextDouble());
            double theta = random.nextDouble() * 2 * Math.PI;
            double phi = Math.acos(2 * random.nextDouble() - 1);

            star.setActualCoordinates(new double[]{
                r * Math.sin(phi) * Math.cos(theta),
                r * Math.sin(phi) * Math.sin(theta),
                r * Math.cos(phi)
            });
            stars.add(star);
        }
        return stars;
    }

    private Graph<String, DefaultWeightedEdge> buildGraph(
            List<SparseStarRecord> stars, double maxDistance) {
        Graph<String, DefaultWeightedEdge> graph =
            new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

        // Add vertices
        for (SparseStarRecord star : stars) {
            graph.addVertex(star.getRecordId());
        }

        // Add edges for stars within jump distance (O(n^2) naive approach)
        for (int i = 0; i < stars.size(); i++) {
            double[] pos1 = stars.get(i).getActualCoordinates();
            for (int j = i + 1; j < stars.size(); j++) {
                double[] pos2 = stars.get(j).getActualCoordinates();
                double dist = distance(pos1, pos2);
                if (dist <= maxDistance) {
                    DefaultWeightedEdge edge = graph.addEdge(
                        stars.get(i).getRecordId(),
                        stars.get(j).getRecordId()
                    );
                    if (edge != null) {
                        graph.setEdgeWeight(edge, dist);
                    }
                }
            }
        }
        return graph;
    }

    private double distance(double[] a, double[] b) {
        double dx = a[0] - b[0];
        double dy = a[1] - b[1];
        double dz = a[2] - b[2];
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    // ============== Graph Building Benchmarks (Naive O(n²)) ==============

    @Benchmark
    public Graph<String, DefaultWeightedEdge> buildGraphNaive1000() {
        return buildGraph(stars1000, MAX_JUMP_DISTANCE);
    }

    @Benchmark
    public Graph<String, DefaultWeightedEdge> buildGraphNaive5000() {
        return buildGraph(stars5000, MAX_JUMP_DISTANCE);
    }

    // ============== Graph Building Benchmarks (KD-Tree O(n log n)) ==============

    private KDTreeGraphBuilder kdTreeBuilder = new KDTreeGraphBuilder();

    @Benchmark
    public Graph<String, DefaultEdge> buildGraphKDTree1000() {
        return kdTreeBuilder.buildGraphFromSparse(stars1000, 0.0, MAX_JUMP_DISTANCE);
    }

    @Benchmark
    public Graph<String, DefaultEdge> buildGraphKDTree5000() {
        return kdTreeBuilder.buildGraphFromSparse(stars5000, 0.0, MAX_JUMP_DISTANCE);
    }

    @Benchmark
    public Graph<String, DefaultEdge> buildGraphKDTree10000() {
        return kdTreeBuilder.buildGraphFromSparse(stars10000, 0.0, MAX_JUMP_DISTANCE);
    }

    // ============== Transit Calculation Benchmarks (Naive O(n²)) ==============

    @Benchmark
    public int countTransits1000(Blackhole bh) {
        int count = 0;
        for (int i = 0; i < stars1000.size(); i++) {
            double[] pos1 = stars1000.get(i).getActualCoordinates();
            for (int j = i + 1; j < stars1000.size(); j++) {
                double[] pos2 = stars1000.get(j).getActualCoordinates();
                if (distance(pos1, pos2) <= MAX_JUMP_DISTANCE) {
                    count++;
                }
            }
        }
        return count;
    }

    @Benchmark
    public int countTransits5000(Blackhole bh) {
        int count = 0;
        for (int i = 0; i < stars5000.size(); i++) {
            double[] pos1 = stars5000.get(i).getActualCoordinates();
            for (int j = i + 1; j < stars5000.size(); j++) {
                double[] pos2 = stars5000.get(j).getActualCoordinates();
                if (distance(pos1, pos2) <= MAX_JUMP_DISTANCE) {
                    count++;
                }
            }
        }
        return count;
    }

    // ============== Pathfinding Benchmarks ==============

    @Benchmark
    public void dijkstra1000(Blackhole bh) {
        DijkstraShortestPath<String, DefaultWeightedEdge> dijkstra =
            new DijkstraShortestPath<>(graph1000);
        // Find path from first to last star (if connected)
        var path = dijkstra.getPath("STAR-0", "STAR-999");
        bh.consume(path);
    }

    @Benchmark
    public void dijkstra5000(Blackhole bh) {
        DijkstraShortestPath<String, DefaultWeightedEdge> dijkstra =
            new DijkstraShortestPath<>(graph5000);
        var path = dijkstra.getPath("STAR-0", "STAR-4999");
        bh.consume(path);
    }

    @Benchmark
    public void yenKShortest1000_k3(Blackhole bh) {
        YenKShortestPath<String, DefaultWeightedEdge> yen =
            new YenKShortestPath<>(graph1000);
        var paths = yen.getPaths("STAR-0", "STAR-999", 3);
        bh.consume(paths);
    }

    @Benchmark
    public void yenKShortest1000_k5(Blackhole bh) {
        YenKShortestPath<String, DefaultWeightedEdge> yen =
            new YenKShortestPath<>(graph1000);
        var paths = yen.getPaths("STAR-0", "STAR-999", 5);
        bh.consume(paths);
    }

    // ============== Distance Calculation Benchmarks ==============

    @Benchmark
    public double distanceCalculation(Blackhole bh) {
        double sum = 0;
        for (int i = 0; i < 10000; i++) {
            double[] a = {random.nextDouble() * 100, random.nextDouble() * 100, random.nextDouble() * 100};
            double[] b = {random.nextDouble() * 100, random.nextDouble() * 100, random.nextDouble() * 100};
            sum += distance(a, b);
        }
        return sum;
    }
}
