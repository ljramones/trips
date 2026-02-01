package com.teamgannon.trips.benchmark;

import com.teamgannon.trips.planetarymodelling.procedural.*;
import org.openjdk.jmh.annotations.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * JMH benchmarks for procedural planet generation pipeline.
 *
 * Run with:
 *   ./mvnw-java25.sh test -Pbenchmark -Dbenchmark.class=ProceduralPlanetBenchmark
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 3, time = 1)
@Fork(1)
public class ProceduralPlanetBenchmark {

    // ============== Mesh Generation Benchmarks ==============

    @Benchmark
    public List<Polygon> meshGenerationDuel() {
        PlanetConfig config = PlanetConfig.builder()
            .seed(12345L)
            .size(PlanetConfig.Size.DUEL)  // 1,212 polygons
            .build();
        return new IcosahedralMesh(config).generate();
    }

    @Benchmark
    public List<Polygon> meshGenerationSmall() {
        PlanetConfig config = PlanetConfig.builder()
            .seed(12345L)
            .size(PlanetConfig.Size.SMALL)  // 4,842 polygons
            .build();
        return new IcosahedralMesh(config).generate();
    }

    @Benchmark
    public List<Polygon> meshGenerationStandard() {
        PlanetConfig config = PlanetConfig.builder()
            .seed(12345L)
            .size(PlanetConfig.Size.STANDARD)  // 4,412 polygons
            .build();
        return new IcosahedralMesh(config).generate();
    }

    // ============== Full Pipeline Benchmarks ==============

    @Benchmark
    public PlanetGenerator.GeneratedPlanet fullPipelineDuel() {
        PlanetConfig config = PlanetConfig.builder()
            .seed(12345L)
            .size(PlanetConfig.Size.DUEL)
            .plateCount(8)
            .waterFraction(0.65)
            .build();
        return PlanetGenerator.generate(config);
    }

    @Benchmark
    public PlanetGenerator.GeneratedPlanet fullPipelineSmall() {
        PlanetConfig config = PlanetConfig.builder()
            .seed(12345L)
            .size(PlanetConfig.Size.SMALL)
            .plateCount(12)
            .waterFraction(0.65)
            .build();
        return PlanetGenerator.generate(config);
    }

    // ============== Component Benchmarks ==============

    private List<Polygon> polygonsDuel;
    private List<Polygon> polygonsSmall;
    private AdjacencyGraph adjacencyDuel;
    private AdjacencyGraph adjacencySmall;
    private PlanetConfig configDuel;
    private PlanetConfig configSmall;

    @Setup(Level.Trial)
    public void setup() {
        configDuel = PlanetConfig.builder()
            .seed(12345L)
            .size(PlanetConfig.Size.DUEL)
            .plateCount(8)
            .build();
        configSmall = PlanetConfig.builder()
            .seed(12345L)
            .size(PlanetConfig.Size.SMALL)
            .plateCount(12)
            .build();

        polygonsDuel = new IcosahedralMesh(configDuel).generate();
        polygonsSmall = new IcosahedralMesh(configSmall).generate();

        adjacencyDuel = new AdjacencyGraph(polygonsDuel);
        adjacencySmall = new AdjacencyGraph(polygonsSmall);
    }

    @Benchmark
    public AdjacencyGraph adjacencyGraphDuel() {
        return new AdjacencyGraph(polygonsDuel);
    }

    @Benchmark
    public AdjacencyGraph adjacencyGraphSmall() {
        return new AdjacencyGraph(polygonsSmall);
    }

    @Benchmark
    public PlateAssigner.PlateAssignment plateAssignmentDuel() {
        return new PlateAssigner(configDuel, adjacencyDuel).assign();
    }

    @Benchmark
    public PlateAssigner.PlateAssignment plateAssignmentSmall() {
        return new PlateAssigner(configSmall, adjacencySmall).assign();
    }

    // ============== Scaling Benchmarks ==============

    @Benchmark
    public int scalingTestDuel() {
        PlanetConfig config = PlanetConfig.builder()
            .seed(12345L)
            .size(PlanetConfig.Size.DUEL)  // 1,212 polygons
            .build();
        List<Polygon> polys = new IcosahedralMesh(config).generate();
        return polys.size();
    }

    @Benchmark
    public int scalingTestTiny() {
        PlanetConfig config = PlanetConfig.builder()
            .seed(12345L)
            .size(PlanetConfig.Size.TINY)  // 2,252 polygons
            .build();
        List<Polygon> polys = new IcosahedralMesh(config).generate();
        return polys.size();
    }

    @Benchmark
    public int scalingTestSmall() {
        PlanetConfig config = PlanetConfig.builder()
            .seed(12345L)
            .size(PlanetConfig.Size.SMALL)  // 3,612 polygons
            .build();
        List<Polygon> polys = new IcosahedralMesh(config).generate();
        return polys.size();
    }

    @Benchmark
    public int scalingTestStandard() {
        PlanetConfig config = PlanetConfig.builder()
            .seed(12345L)
            .size(PlanetConfig.Size.STANDARD)  // 4,412 polygons
            .build();
        List<Polygon> polys = new IcosahedralMesh(config).generate();
        return polys.size();
    }
}
