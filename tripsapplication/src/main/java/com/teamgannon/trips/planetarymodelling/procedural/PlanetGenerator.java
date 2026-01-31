package com.teamgannon.trips.planetarymodelling.procedural;

import com.teamgannon.trips.planetarymodelling.procedural.biome.BiomeClassifier;
import com.teamgannon.trips.planetarymodelling.procedural.biome.BiomeType;
import com.teamgannon.trips.planetarymodelling.procedural.impact.CraterCalculator;
import com.teamgannon.trips.planetarymodelling.procedural.impact.ImpactResult;
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

        /**
         * Classifies each polygon into a biome type based on elevation, climate, and rainfall.
         * The classification is computed on demand (not cached).
         *
         * @return Array of BiomeType for each polygon
         */
        public BiomeType[] biomes() {
            // Build adjacency array format expected by BiomeClassifier
            int[][] adjacencies = new int[polygons.size()][];
            for (int i = 0; i < polygons.size(); i++) {
                adjacencies[i] = adjacency.neighbors(i);
            }

            return BiomeClassifier.classify(heights, climates, erosionResult, adjacencies);
        }

        /**
         * Returns the biome distribution as a map of biome type to count.
         */
        public java.util.Map<BiomeType, Integer> biomeDistribution() {
            return BiomeClassifier.getDistribution(biomes());
        }

        /**
         * Returns the land biome distribution as percentages.
         */
        public java.util.Map<BiomeType, Double> landBiomePercentages() {
            return BiomeClassifier.getLandDistribution(biomes());
        }
    }

    public GeneratedPlanet generate() {
        GenerationProgressListener.Phase currentPhase = GenerationProgressListener.Phase.MESH_GENERATION;
        try {
            validateConfigForGeneration(config);

            // Phase 1: Mesh generation
            currentPhase = GenerationProgressListener.Phase.MESH_GENERATION;
            listener.onPhaseStarted(currentPhase,
                "Creating icosahedral mesh with " + config.polyCount() + " polygons");
            IcosahedralMesh mesh = new IcosahedralMesh(config);
            List<Polygon> polygons = mesh.generate();
            listener.onProgressUpdate(currentPhase, 1.0);
            listener.onPhaseCompleted(currentPhase);

            // Phase 2: Adjacency graph
            currentPhase = GenerationProgressListener.Phase.ADJACENCY_GRAPH;
            listener.onPhaseStarted(currentPhase,
                "Building adjacency relationships");
            AdjacencyGraph adjacency = new AdjacencyGraph(polygons);
            listener.onProgressUpdate(currentPhase, 1.0);
            listener.onPhaseCompleted(currentPhase);

            // Phase 3: Plate assignment
            currentPhase = GenerationProgressListener.Phase.PLATE_ASSIGNMENT;
            listener.onPhaseStarted(currentPhase,
                "Assigning " + config.plateCount() + " tectonic plates");
            PlateAssigner plateAssigner = new PlateAssigner(config, adjacency);
            PlateAssigner.PlateAssignment plateAssignment = plateAssigner.assign();
            validatePlateAssignment(polygons.size(), config.plateCount(), plateAssignment);
            listener.onProgressUpdate(currentPhase, 1.0);
            listener.onPhaseCompleted(currentPhase);

            // Phase 4: Boundary detection
            currentPhase = GenerationProgressListener.Phase.BOUNDARY_DETECTION;
            listener.onPhaseStarted(currentPhase,
                "Detecting plate boundaries and interactions");
            BoundaryDetector boundaryDetector = new BoundaryDetector(config, plateAssignment);
            BoundaryDetector.BoundaryAnalysis boundaryAnalysis = boundaryDetector.analyze();
            listener.onProgressUpdate(currentPhase, 1.0);
            listener.onPhaseCompleted(currentPhase);

            // Phase 5: Elevation calculation
            currentPhase = GenerationProgressListener.Phase.ELEVATION_CALCULATION;
            listener.onPhaseStarted(currentPhase,
                "Calculating terrain elevations");
            ElevationCalculator elevationCalc = new ElevationCalculator(
                config, adjacency, plateAssignment, boundaryAnalysis);
            ElevationCalculator.ElevationResult elevationResult = elevationCalc.calculateResult();
            int[] heights = elevationResult.heights();
            double[] baseHeights = elevationResult.continuousHeights();
            listener.onProgressUpdate(currentPhase, 1.0);
            listener.onPhaseCompleted(currentPhase);

            // Phase 5.5: Impact features (craters and volcanoes)
            if (config.craterDensity() > 0 || (config.enableVolcanos() && config.volcanoDensity() > 0)) {
                currentPhase = GenerationProgressListener.Phase.IMPACT_FEATURES;
                listener.onPhaseStarted(currentPhase,
                    "Placing impact craters and volcanic features");
                CraterCalculator craterCalc = new CraterCalculator(
                    config, polygons, adjacency, baseHeights, plateAssignment, boundaryAnalysis);
                ImpactResult impactResult = craterCalc.calculate();
                // Update baseHeights with impact modifications (already done in place)
                // Update integer heights to match
                for (int i = 0; i < heights.length; i++) {
                    heights[i] = (int) Math.round(Math.max(-4, Math.min(4, baseHeights[i])));
                }
                listener.onProgressUpdate(currentPhase, 1.0);
                listener.onPhaseCompleted(currentPhase);
            }

            // Phase 6: Climate calculation
            currentPhase = GenerationProgressListener.Phase.CLIMATE_CALCULATION;
            listener.onPhaseStarted(currentPhase,
                "Assigning climate zones using " + config.climateModel() + " model");
            ClimateCalculator climateCalc = new ClimateCalculator(
                polygons, config.climateModel(),
                config.axialTiltDegrees(), config.seasonalOffsetDegrees(), config.seasonalSamples());
            ClimateCalculator.ClimateZone[] climates = climateCalc.calculate();
            validateClimates(polygons.size(), climates);
            listener.onProgressUpdate(currentPhase, 1.0);
            listener.onPhaseCompleted(currentPhase);

            // Phase 7: Erosion calculation
            currentPhase = GenerationProgressListener.Phase.EROSION_CALCULATION;
            listener.onPhaseStarted(currentPhase,
                "Simulating erosion with " + config.erosionIterations() + " iterations");
            // Apply erosion pass (runs after climate since rainfall depends on climate zones)
            // Pass plate data for divergent boundary moisture boost
            ErosionCalculator.ErosionResult erosionResult = config.useContinuousHeights()
                ? ErosionCalculator.calculate(
                    baseHeights, polygons, adjacency, climates, config, plateAssignment, boundaryAnalysis)
                : ErosionCalculator.calculate(
                    heights, polygons, adjacency, climates, config, plateAssignment, boundaryAnalysis);
            listener.onProgressUpdate(currentPhase, 1.0);
            listener.onPhaseCompleted(currentPhase);

            // Use eroded heights for final output
            int[] finalHeights = erosionResult.erodedHeights();
            validateHeights(finalHeights);

            GeneratedPlanet planet = new GeneratedPlanet(config, polygons, finalHeights, baseHeights, climates,
                plateAssignment, boundaryAnalysis, erosionResult, adjacency);

            listener.onGenerationCompleted();
            return planet;

        } catch (Exception e) {
            listener.onGenerationError(currentPhase, e);
            throw e;
        }
    }

    private static void validateConfigForGeneration(PlanetConfig config) {
        if (config.plateCount() > config.polyCount() / 10) {
            throw new IllegalArgumentException(
                "Too many plates for mesh size: " + config.plateCount()
                    + " plates for " + config.polyCount() + " polygons");
        }
    }

    private static void validatePlateAssignment(
            int polygonCount, int plateCount, PlateAssigner.PlateAssignment assignment) {
        int[] plateIndex = assignment.plateIndex();
        if (plateIndex.length != polygonCount) {
            throw new IllegalStateException(
                "Plate assignment size mismatch: expected " + polygonCount
                    + " got " + plateIndex.length);
        }
        for (int idx : plateIndex) {
            if (idx < 0 || idx >= plateCount) {
                throw new IllegalStateException("Invalid plate index: " + idx);
            }
        }
        int total = assignment.plates().stream().mapToInt(List::size).sum();
        if (total != polygonCount) {
            throw new IllegalStateException(
                "Plate assignment incomplete: " + total + " of " + polygonCount);
        }
    }

    private static void validateHeights(int[] heights) {
        for (int height : heights) {
            if (height < -4 || height > 4) {
                throw new IllegalStateException("Height out of range: " + height);
            }
        }
    }

    private static void validateClimates(int polygonCount, ClimateCalculator.ClimateZone[] climates) {
        if (climates.length != polygonCount) {
            throw new IllegalStateException(
                "Climate array size mismatch: expected " + polygonCount
                    + " got " + climates.length);
        }
        for (ClimateCalculator.ClimateZone zone : climates) {
            if (zone == null) {
                throw new IllegalStateException("Climate zone missing");
            }
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
