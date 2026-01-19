package com.teamgannon.trips.planetarymodelling.procedural;

import com.teamgannon.trips.solarsysmodelling.accrete.Planet;
import org.jzy3d.plot3d.primitives.Composite;
import org.jzy3d.plot3d.primitives.Shape;

import java.util.List;

/**
 * Facade for complete planet generation pipeline.
 */
public class PlanetGenerator {

    private final PlanetConfig config;

    /**
     * Creates a new PlanetGenerator with the given configuration.
     *
     * @param config The planet configuration (must not be null)
     * @throws IllegalArgumentException if config is null
     */
    public PlanetGenerator(PlanetConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("PlanetConfig cannot be null");
        }
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
        BoundaryDetector.BoundaryAnalysis boundaryAnalysis,
        ErosionCalculator.ErosionResult erosionResult,
        AdjacencyGraph adjacency
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

        /**
         * Returns the river paths from erosion calculation.
         * Each river is a list of polygon indices from source to mouth.
         */
        public List<List<Integer>> rivers() {
            return erosionResult != null ? erosionResult.rivers() : List.of();
        }

        /**
         * Returns the rainfall distribution from erosion calculation.
         * Values indicate relative precipitation amounts per polygon.
         */
        public double[] rainfall() {
            return erosionResult != null ? erosionResult.rainfall() : new double[0];
        }

        /**
         * Returns the high-precision heights from erosion calculation.
         * These provide finer gradations than integer heights for smooth rendering.
         */
        public double[] preciseHeights() {
            return erosionResult != null ? erosionResult.preciseHeights() : new double[0];
        }

        /**
         * Returns array indicating which rivers end frozen (in polar zones).
         * True = frozen terminus, False = flows into ocean.
         */
        public boolean[] frozenRiverTerminus() {
            return erosionResult != null ? erosionResult.frozenRiverTerminus() : null;
        }

        /**
         * Checks if a specific river ends frozen.
         */
        public boolean isRiverFrozen(int riverIndex) {
            return erosionResult != null && erosionResult.isRiverFrozen(riverIndex);
        }

        /**
         * Creates a shape using smooth color gradients based on precise heights.
         * Provides more natural-looking terrain than integer-stepped colors.
         */
        public Shape createSmoothShape() {
            return new PlanetRenderer(polygons, heights, preciseHeights(), config.radius() / 6371.0)
                .createSmoothShape();
        }

        /**
         * Creates a smooth shape at specified scale.
         */
        public Shape createSmoothShape(double scale) {
            return new PlanetRenderer(polygons, heights, preciseHeights(), scale)
                .createSmoothShape();
        }

        /**
         * Creates a composite shape with planet surface and rivers.
         * Rivers are rendered as blue line strips from mountain sources to ocean mouths.
         * Frozen rivers (ending in polar zones) are rendered with ice-blue gradient.
         */
        public Composite createShapeWithRivers() {
            return new PlanetRenderer(polygons, heights, config.radius() / 6371.0)
                .createShapeWithRivers(rivers(), frozenRiverTerminus());
        }

        /**
         * Creates a composite shape with planet surface and rivers at specified scale.
         */
        public Composite createShapeWithRivers(double scale) {
            return new PlanetRenderer(polygons, heights, scale)
                .createShapeWithRivers(rivers(), frozenRiverTerminus());
        }

        /**
         * Creates a shape containing only the rivers (without planet surface).
         * Useful for layering rivers over existing planet renders.
         * Includes frozen river visualization.
         */
        public Composite createRiversOnly() {
            return new PlanetRenderer(polygons, heights, config.radius() / 6371.0)
                .createRiverShape(rivers(), frozenRiverTerminus());
        }

        /**
         * Creates a shape containing only the rivers at specified scale.
         */
        public Composite createRiversOnly(double scale) {
            return new PlanetRenderer(polygons, heights, scale)
                .createRiverShape(rivers(), frozenRiverTerminus());
        }

        /**
         * Creates a rainfall heat-map visualization.
         * Colors range from brown (dry) through yellow/green to blue (wet).
         * Useful for debugging erosion and understanding climate patterns.
         */
        public Shape createRainfallHeatMap() {
            return new PlanetRenderer(polygons, heights, config.radius() / 6371.0)
                .createRainfallHeatMap(rainfall());
        }

        /**
         * Creates a rainfall heat-map at specified scale.
         */
        public Shape createRainfallHeatMap(double scale) {
            return new PlanetRenderer(polygons, heights, scale)
                .createRainfallHeatMap(rainfall());
        }

        /**
         * Creates a blended terrain/rainfall visualization.
         * @param blendFactor 0.0 = terrain only, 1.0 = rainfall only, 0.5 = equal blend
         */
        public Shape createTerrainWithRainfallOverlay(double blendFactor) {
            return new PlanetRenderer(polygons, heights, config.radius() / 6371.0)
                .createTerrainWithRainfallOverlay(rainfall(), blendFactor);
        }

        /**
         * Creates a blended terrain/rainfall visualization at specified scale.
         */
        public Shape createTerrainWithRainfallOverlay(double scale, double blendFactor) {
            return new PlanetRenderer(polygons, heights, scale)
                .createTerrainWithRainfallOverlay(rainfall(), blendFactor);
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

        // Apply erosion pass (runs after climate since rainfall depends on climate zones)
        // Pass plate data for divergent boundary moisture boost
        ErosionCalculator.ErosionResult erosionResult = ErosionCalculator.calculate(
            heights, polygons, adjacency, climates, config, plateAssignment, boundaryAnalysis);

        // Use eroded heights for final output
        int[] finalHeights = erosionResult.erodedHeights();

        return new GeneratedPlanet(config, polygons, finalHeights, climates,
            plateAssignment, boundaryAnalysis, erosionResult, adjacency);
    }

    /**
     * Generates a procedural planet with the given configuration.
     *
     * @param config The planet configuration (must not be null)
     * @return Generated planet with terrain data
     * @throws IllegalArgumentException if config is null
     */
    public static GeneratedPlanet generate(PlanetConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("PlanetConfig cannot be null");
        }
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
     * @param accretePlanet The planet from Accrete simulation (must not be null, must have positive radius)
     * @param seed          Seed for procedural generation (use planet's orbital hash or similar)
     * @return PlanetConfig with physically-derived tectonic parameters
     * @throws IllegalArgumentException if accretePlanet is null or has invalid properties
     */
    public static PlanetConfig createBiasedConfig(Planet accretePlanet, long seed) {
        validateAccretePlanet(accretePlanet);

        TectonicBias bias = TectonicBias.fromAccretePlanet(accretePlanet);

        // Clamp hydrosphere to valid range [0, 100]
        double hydrosphere = Math.max(0.0, Math.min(100.0, accretePlanet.getHydrosphere()));

        PlanetConfig base = PlanetConfig.builder()
            .seed(seed)
            .fromAccreteRadius(accretePlanet.getRadius())
            .waterFraction(hydrosphere / 100.0)
            .build();

        return bias.applyTo(base, seed);
    }

    /**
     * Generates a procedural planet directly from an Accrete planet.
     * Combines createBiasedConfig and generate into a single call.
     *
     * @param accretePlanet The planet from Accrete simulation (must not be null)
     * @param seed          Seed for procedural generation
     * @return Generated planet with terrain based on physical parameters
     * @throws IllegalArgumentException if accretePlanet is null or invalid
     */
    public static GeneratedPlanet generateFromAccrete(Planet accretePlanet, long seed) {
        validateAccretePlanet(accretePlanet);
        PlanetConfig config = createBiasedConfig(accretePlanet, seed);
        return generate(config);
    }

    /**
     * Validates an Accrete planet has the required properties for terrain generation.
     *
     * @param planet The planet to validate
     * @throws IllegalArgumentException if the planet is null or has invalid properties
     */
    private static void validateAccretePlanet(Planet planet) {
        if (planet == null) {
            throw new IllegalArgumentException("Accrete planet cannot be null");
        }
        if (planet.getRadius() <= 0) {
            throw new IllegalArgumentException(
                "Planet radius must be positive, got: " + planet.getRadius() + " km");
        }
        if (planet.isGasGiant()) {
            // Gas giants have no solid surface, but we still allow generation
            // (returns minimal terrain for visualization purposes)
        }
    }
}
