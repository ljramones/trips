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
    private final GenerationProgressListener listener;

    /**
     * Creates a new PlanetGenerator with the given configuration.
     *
     * @param config The planet configuration (must not be null)
     * @throws IllegalArgumentException if config is null
     */
    public PlanetGenerator(PlanetConfig config) {
        this(config, GenerationProgressListener.NO_OP);
    }

    /**
     * Creates a new PlanetGenerator with the given configuration and progress listener.
     *
     * @param config   The planet configuration (must not be null)
     * @param listener Progress listener for tracking generation (null uses NO_OP)
     * @throws IllegalArgumentException if config is null
     */
    public PlanetGenerator(PlanetConfig config, GenerationProgressListener listener) {
        if (config == null) {
            throw new IllegalArgumentException("PlanetConfig cannot be null");
        }
        this.config = config;
        this.listener = listener != null ? listener : GenerationProgressListener.NO_OP;
    }

    /**
     * Result of procedural planet generation.
     * Named GeneratedPlanet to avoid collision with Accrete's Planet class.
     */
    public record GeneratedPlanet(
        PlanetConfig config,
        List<Polygon> polygons,
        int[] heights,
        double[] baseHeights,
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
         * Returns flow accumulation values used for river sizing.
         */
        public double[] flowAccumulation() {
            return erosionResult != null ? erosionResult.flowAccumulation() : new double[0];
        }

        /**
         * Returns mask of polygons that are part of filled lake basins.
         */
        public boolean[] lakeMask() {
            return erosionResult != null ? erosionResult.lakeMask() : null;
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
        try {
            // Phase 1: Mesh generation
            listener.onPhaseStarted(GenerationProgressListener.Phase.MESH_GENERATION,
                "Creating icosahedral mesh with " + config.polyCount() + " polygons");
            IcosahedralMesh mesh = new IcosahedralMesh(config);
            List<Polygon> polygons = mesh.generate();
            listener.onProgressUpdate(GenerationProgressListener.Phase.MESH_GENERATION, 1.0);
            listener.onPhaseCompleted(GenerationProgressListener.Phase.MESH_GENERATION);

            // Phase 2: Adjacency graph
            listener.onPhaseStarted(GenerationProgressListener.Phase.ADJACENCY_GRAPH,
                "Building adjacency relationships");
            AdjacencyGraph adjacency = new AdjacencyGraph(polygons);
            listener.onProgressUpdate(GenerationProgressListener.Phase.ADJACENCY_GRAPH, 1.0);
            listener.onPhaseCompleted(GenerationProgressListener.Phase.ADJACENCY_GRAPH);

            // Phase 3: Plate assignment
            listener.onPhaseStarted(GenerationProgressListener.Phase.PLATE_ASSIGNMENT,
                "Assigning " + config.plateCount() + " tectonic plates");
            PlateAssigner plateAssigner = new PlateAssigner(config, adjacency);
            PlateAssigner.PlateAssignment plateAssignment = plateAssigner.assign();
            listener.onProgressUpdate(GenerationProgressListener.Phase.PLATE_ASSIGNMENT, 1.0);
            listener.onPhaseCompleted(GenerationProgressListener.Phase.PLATE_ASSIGNMENT);

            // Phase 4: Boundary detection
            listener.onPhaseStarted(GenerationProgressListener.Phase.BOUNDARY_DETECTION,
                "Detecting plate boundaries and interactions");
            BoundaryDetector boundaryDetector = new BoundaryDetector(config, plateAssignment);
            BoundaryDetector.BoundaryAnalysis boundaryAnalysis = boundaryDetector.analyze();
            listener.onProgressUpdate(GenerationProgressListener.Phase.BOUNDARY_DETECTION, 1.0);
            listener.onPhaseCompleted(GenerationProgressListener.Phase.BOUNDARY_DETECTION);

            // Phase 5: Elevation calculation
            listener.onPhaseStarted(GenerationProgressListener.Phase.ELEVATION_CALCULATION,
                "Calculating terrain elevations");
            ElevationCalculator elevationCalc = new ElevationCalculator(
                config, adjacency, plateAssignment, boundaryAnalysis);
            ElevationCalculator.ElevationResult elevationResult = elevationCalc.calculateResult();
            int[] heights = elevationResult.heights();
            double[] baseHeights = elevationResult.continuousHeights();
            listener.onProgressUpdate(GenerationProgressListener.Phase.ELEVATION_CALCULATION, 1.0);
            listener.onPhaseCompleted(GenerationProgressListener.Phase.ELEVATION_CALCULATION);

            // Phase 6: Climate calculation
            listener.onPhaseStarted(GenerationProgressListener.Phase.CLIMATE_CALCULATION,
                "Assigning climate zones using " + config.climateModel() + " model");
            ClimateCalculator climateCalc = new ClimateCalculator(polygons, config.climateModel());
            ClimateCalculator.ClimateZone[] climates = climateCalc.calculate();
            listener.onProgressUpdate(GenerationProgressListener.Phase.CLIMATE_CALCULATION, 1.0);
            listener.onPhaseCompleted(GenerationProgressListener.Phase.CLIMATE_CALCULATION);

            // Phase 7: Erosion calculation
            listener.onPhaseStarted(GenerationProgressListener.Phase.EROSION_CALCULATION,
                "Simulating erosion with " + config.erosionIterations() + " iterations");
            // Apply erosion pass (runs after climate since rainfall depends on climate zones)
            // Pass plate data for divergent boundary moisture boost
            ErosionCalculator.ErosionResult erosionResult = config.useContinuousHeights()
                ? ErosionCalculator.calculate(
                    baseHeights, polygons, adjacency, climates, config, plateAssignment, boundaryAnalysis)
                : ErosionCalculator.calculate(
                    heights, polygons, adjacency, climates, config, plateAssignment, boundaryAnalysis);
            listener.onProgressUpdate(GenerationProgressListener.Phase.EROSION_CALCULATION, 1.0);
            listener.onPhaseCompleted(GenerationProgressListener.Phase.EROSION_CALCULATION);

            // Use eroded heights for final output
            int[] finalHeights = erosionResult.erodedHeights();

            GeneratedPlanet planet = new GeneratedPlanet(config, polygons, finalHeights, baseHeights, climates,
                plateAssignment, boundaryAnalysis, erosionResult, adjacency);

            listener.onGenerationCompleted();
            return planet;

        } catch (Exception e) {
            listener.onGenerationError(GenerationProgressListener.Phase.MESH_GENERATION, e);
            throw e;
        }
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

    /**
     * Generates a procedural planet with the given configuration and progress listener.
     *
     * @param config   The planet configuration (must not be null)
     * @param listener Progress listener for tracking generation (null uses NO_OP)
     * @return Generated planet with terrain data
     * @throws IllegalArgumentException if config is null
     */
    public static GeneratedPlanet generate(PlanetConfig config, GenerationProgressListener listener) {
        if (config == null) {
            throw new IllegalArgumentException("PlanetConfig cannot be null");
        }
        return new PlanetGenerator(config, listener).generate();
    }

    public static GeneratedPlanet generateDefault() {
        return generate(PlanetConfig.builder().build());
    }

    /**
     * Generates a procedural planet with default configuration and progress listener.
     *
     * @param listener Progress listener for tracking generation (null uses NO_OP)
     * @return Generated planet with terrain data
     */
    public static GeneratedPlanet generateDefault(GenerationProgressListener listener) {
        return generate(PlanetConfig.builder().build(), listener);
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
