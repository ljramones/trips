# TRIPS JMH Microbenchmarks

Performance benchmarks using JMH (Java Microbenchmark Harness).

## Running Benchmarks

### Run All Benchmarks
```bash
./mvnw-java25.sh test -Pbenchmark
```

### Run Specific Benchmark Class
```bash
# FastNoiseLite noise generation
./mvnw-java25.sh test -Pbenchmark -Dbenchmark.class=FastNoiseLiteBenchmark

# Procedural planet generation
./mvnw-java25.sh test -Pbenchmark -Dbenchmark.class=ProceduralPlanetBenchmark

# Routing and graph algorithms
./mvnw-java25.sh test -Pbenchmark -Dbenchmark.class=RoutingBenchmark
```

### Run Specific Benchmark Method
```bash
./mvnw-java25.sh test -Pbenchmark -Dbenchmark.class=".*fbmGrid2D.*"
```

## Benchmark Classes

### FastNoiseLiteBenchmark
Tests noise generation performance:
- Single point evaluation (2D/3D)
- Grid-based generation (realistic terrain use case)
- Various noise types: Perlin, Simplex, Cellular, FBm
- Domain warp performance

### ProceduralPlanetBenchmark
Tests procedural planet pipeline:
- IcosahedralMesh generation at various sizes
- AdjacencyGraph construction
- PlateAssigner performance
- Full pipeline (mesh → plates → elevation → climate → rivers)

### RoutingBenchmark
Tests graph algorithms for star routing:
- Graph building from star positions
- Transit calculation (O(n²) distance checks)
- Dijkstra shortest path
- Yen's K-Shortest paths

## Output

Results are written to `target/jmh-result.json` in JSON format.

Default configuration:
- 1 fork
- 3 warmup iterations
- 5 measurement iterations
- Results show throughput (ops/ms) or average time (ms/op)

## Adding New Benchmarks

1. Create a new class in this package
2. Annotate class with `@State(Scope.Benchmark)`
3. Annotate methods with `@Benchmark`
4. Use `@Setup` for initialization
5. Use `Blackhole.consume()` to prevent dead code elimination

Example:
```java
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class MyBenchmark {

    @Setup(Level.Trial)
    public void setup() {
        // Initialize test data
    }

    @Benchmark
    public void myMethod(Blackhole bh) {
        bh.consume(doWork());
    }
}
```
