package com.teamgannon.trips.planetarymodelling.procedural;

import com.teamgannon.trips.solarsysmodelling.accrete.Planet;
import org.jzy3d.plot3d.primitives.Shape;

import java.util.List;

/**
 * Facade for complete planet generation pipeline.
 */
public class PlanetGenerator {

    private final PlanetConfig config;

    public PlanetGenerator(PlanetConfig config) {
        this.config = config;
    }

    /**
     * Result of procedural planet generation.
     * Named GeneratedPlanet to avoid collision with Accrete's Planet class.
     */
    public record GeneratedPlanet(
        PlanetConfig config,
        List<Polygon> polygons,
        int[] heights,
        ClimateCalculator.ClimateZone[] climates,
        PlateAssigner.PlateAssignment plateAssignment,
        BoundaryDetector.BoundaryAnalysis boundaryAnalysis
    ) {
        public Shape createShape() {
            return new PlanetRenderer(polygons, heights, config.radius() / 6371.0).createShape();
        }

        public Shape createShape(double scale) {
            return new PlanetRenderer(polygons, heights, scale).createShape();
        }

        public Shape createWireframe() {
            return new PlanetRenderer(polygons, heights).createWireframeShape();
        }
    }

    public GeneratedPlanet generate() {
        IcosahedralMesh mesh = new IcosahedralMesh(config);
        List<Polygon> polygons = mesh.generate();

        AdjacencyGraph adjacency = new AdjacencyGraph(polygons);

        PlateAssigner plateAssigner = new PlateAssigner(config, adjacency);
        PlateAssigner.PlateAssignment plateAssignment = plateAssigner.assign();

        BoundaryDetector boundaryDetector = new BoundaryDetector(config, plateAssignment);
        BoundaryDetector.BoundaryAnalysis boundaryAnalysis = boundaryDetector.analyze();

        ElevationCalculator elevationCalc = new ElevationCalculator(
            config, adjacency, plateAssignment, boundaryAnalysis);
        int[] heights = elevationCalc.calculate();

        ClimateCalculator climateCalc = new ClimateCalculator(polygons);
        ClimateCalculator.ClimateZone[] climates = climateCalc.calculate();

        return new GeneratedPlanet(config, polygons, heights, climates, plateAssignment, boundaryAnalysis);
    }

    public static GeneratedPlanet generate(PlanetConfig config) {
        return new PlanetGenerator(config).generate();
    }

    public static GeneratedPlanet generateDefault() {
        return generate(PlanetConfig.builder().build());
    }

    /**
     * Creates a PlanetConfig with tectonic bias derived from an Accrete planet.
     * This is the primary integration point between Accrete simulation and
     * procedural terrain generation.
     *
     * @param accretePlanet The planet from Accrete simulation
     * @param seed          Seed for procedural generation (use planet's orbital hash or similar)
     * @return PlanetConfig with physically-derived tectonic parameters
     */
    public static PlanetConfig createBiasedConfig(Planet accretePlanet, long seed) {
        TectonicBias bias = TectonicBias.fromAccretePlanet(accretePlanet);

        PlanetConfig base = PlanetConfig.builder()
            .seed(seed)
            .fromAccreteRadius(accretePlanet.getRadius())
            .waterFraction(accretePlanet.getHydrosphere() / 100.0)
            .build();

        return bias.applyTo(base, seed);
    }

    /**
     * Generates a procedural planet directly from an Accrete planet.
     * Combines createBiasedConfig and generate into a single call.
     *
     * @param accretePlanet The planet from Accrete simulation
     * @param seed          Seed for procedural generation
     * @return Generated planet with terrain based on physical parameters
     */
    public static GeneratedPlanet generateFromAccrete(Planet accretePlanet, long seed) {
        PlanetConfig config = createBiasedConfig(accretePlanet, seed);
        return generate(config);
    }
}
