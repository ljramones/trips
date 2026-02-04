package com.teamgannon.trips.benchmark;

import com.cognitivedynamics.noisegen.FastNoiseLite;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

/**
 * JMH benchmarks for FastNoiseLite noise generation.
 *
 * Run with:
 *   ./mvnw-java25.sh test -Pbenchmark -Dbenchmark.class=FastNoiseLiteBenchmark
 *
 * Or run all benchmarks:
 *   ./mvnw-java25.sh test -Pbenchmark
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class FastNoiseLiteBenchmark {

    private FastNoiseLite perlinNoise;
    private FastNoiseLite simplexNoise;
    private FastNoiseLite cellularNoise;
    private FastNoiseLite fbmNoise;
    private FastNoiseLite warpedNoise;

    // Grid parameters
    private static final int GRID_SIZE = 128;
    private static final float SCALE = 0.01f;

    @Setup(Level.Trial)
    public void setup() {
        // Basic Perlin noise
        perlinNoise = new FastNoiseLite(12345);
        perlinNoise.SetNoiseType(FastNoiseLite.NoiseType.Perlin);
        perlinNoise.SetFrequency(SCALE);

        // OpenSimplex2 noise
        simplexNoise = new FastNoiseLite(12345);
        simplexNoise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
        simplexNoise.SetFrequency(SCALE);

        // Cellular/Voronoi noise
        cellularNoise = new FastNoiseLite(12345);
        cellularNoise.SetNoiseType(FastNoiseLite.NoiseType.Cellular);
        cellularNoise.SetCellularDistanceFunction(FastNoiseLite.CellularDistanceFunction.EuclideanSq);
        cellularNoise.SetCellularReturnType(FastNoiseLite.CellularReturnType.Distance);
        cellularNoise.SetFrequency(SCALE);

        // FBm (Fractal Brownian Motion) - most common terrain generation
        fbmNoise = new FastNoiseLite(12345);
        fbmNoise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
        fbmNoise.SetFractalType(FastNoiseLite.FractalType.FBm);
        fbmNoise.SetFractalOctaves(6);
        fbmNoise.SetFractalLacunarity(2.0f);
        fbmNoise.SetFractalGain(0.5f);
        fbmNoise.SetFrequency(SCALE);

        // Domain warped noise - more complex terrain
        warpedNoise = new FastNoiseLite(12345);
        warpedNoise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
        warpedNoise.SetFractalType(FastNoiseLite.FractalType.FBm);
        warpedNoise.SetFractalOctaves(4);
        warpedNoise.SetDomainWarpType(FastNoiseLite.DomainWarpType.OpenSimplex2);
        warpedNoise.SetDomainWarpAmp(50.0f);
        warpedNoise.SetFrequency(SCALE);
    }

    // ============== Single Point Benchmarks ==============

    @Benchmark
    public float perlin2D() {
        return perlinNoise.GetNoise(100.5f, 200.5f);
    }

    @Benchmark
    public float perlin3D() {
        return perlinNoise.GetNoise(100.5f, 200.5f, 300.5f);
    }

    @Benchmark
    public float simplex2D() {
        return simplexNoise.GetNoise(100.5f, 200.5f);
    }

    @Benchmark
    public float simplex3D() {
        return simplexNoise.GetNoise(100.5f, 200.5f, 300.5f);
    }

    @Benchmark
    public float cellular2D() {
        return cellularNoise.GetNoise(100.5f, 200.5f);
    }

    @Benchmark
    public float cellular3D() {
        return cellularNoise.GetNoise(100.5f, 200.5f, 300.5f);
    }

    @Benchmark
    public float fbm2D() {
        return fbmNoise.GetNoise(100.5f, 200.5f);
    }

    @Benchmark
    public float fbm3D() {
        return fbmNoise.GetNoise(100.5f, 200.5f, 300.5f);
    }

    // ============== Grid Benchmarks (Realistic Use Case) ==============

    @Benchmark
    public void perlinGrid2D(Blackhole bh) {
        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                bh.consume(perlinNoise.GetNoise(x, y));
            }
        }
    }

    @Benchmark
    public void simplexGrid2D(Blackhole bh) {
        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                bh.consume(simplexNoise.GetNoise(x, y));
            }
        }
    }

    @Benchmark
    public void cellularGrid2D(Blackhole bh) {
        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                bh.consume(cellularNoise.GetNoise(x, y));
            }
        }
    }

    @Benchmark
    public void fbmGrid2D(Blackhole bh) {
        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                bh.consume(fbmNoise.GetNoise(x, y));
            }
        }
    }

    @Benchmark
    public void fbmGrid3D(Blackhole bh) {
        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                bh.consume(fbmNoise.GetNoise(x, y, 0));
            }
        }
    }

    // ============== Domain Warp Benchmarks ==============

    @Benchmark
    public void domainWarp2D(Blackhole bh) {
        float x = 100.5f;
        float y = 200.5f;
        // Manual domain warp
        float warpX = warpedNoise.GetNoise(x, y) * 50.0f;
        float warpY = warpedNoise.GetNoise(x + 1000, y + 1000) * 50.0f;
        bh.consume(warpedNoise.GetNoise(x + warpX, y + warpY));
    }

    @Benchmark
    public void domainWarpGrid2D(Blackhole bh) {
        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                float warpX = warpedNoise.GetNoise(x, y) * 50.0f;
                float warpY = warpedNoise.GetNoise(x + 1000, y + 1000) * 50.0f;
                bh.consume(warpedNoise.GetNoise(x + warpX, y + warpY));
            }
        }
    }
}
