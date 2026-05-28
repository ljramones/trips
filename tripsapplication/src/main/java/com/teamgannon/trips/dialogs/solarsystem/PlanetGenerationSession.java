package com.teamgannon.trips.dialogs.solarsystem;

import com.teamgannon.trips.planetary.modelling.procedural.AdjacencyGraph;
import com.teamgannon.trips.planetary.modelling.procedural.BoundaryDetector;
import com.teamgannon.trips.planetary.modelling.procedural.ClimateCalculator;
import com.teamgannon.trips.planetary.modelling.procedural.JavaFxPlanetMeshConverter.TerrainType;
import com.teamgannon.trips.planetary.modelling.procedural.PlateAssigner;
import com.teamgannon.trips.planetary.modelling.procedural.PlanetConfig;
import com.teamgannon.trips.planetary.modelling.procedural.PlanetGenerator.GeneratedPlanet;

/**
 * Mutable non-JavaFX state for {@link ProceduralPlanetViewerDialog}.
 * <p>
 * The dialog owns controls and scene graph updates; this session owns the
 * current generated planet snapshot, generation parameters, and physical
 * context used by terrain classification.
 */
final class PlanetGenerationSession {

    private GeneratedPlanet planet;
    private double[] rainfall;
    private double[] preciseHeights;
    private AdjacencyGraph adjacency;
    private PlateAssigner.PlateAssignment plateAssignment;
    private BoundaryDetector.BoundaryAnalysis boundaryAnalysis;

    private double surfaceTemperatureK = 288.0;
    private String planetType;
    private double iceCoverFraction;
    private Double densityGcm3;
    private Double semiMajorAxisAU;

    private long seed;
    private int plateCount;
    private double waterFraction;
    private int erosionIterations;
    private double riverThreshold;
    private double heightScale;
    private boolean useContinuousHeights;
    private double reliefMin;
    private double reliefMax;
    private double axialTilt;
    private double seasonalOffset;
    private PlanetConfig.Size size;
    private ClimateCalculator.ClimateModel climateModel;

    private PlanetGenerationSession(GeneratedPlanet planet) {
        applyPlanet(planet);
        applyConfig(planet.config());
    }

    static PlanetGenerationSession from(GeneratedPlanet planet) {
        return new PlanetGenerationSession(planet);
    }

    void applyPlanet(GeneratedPlanet newPlanet) {
        this.planet = newPlanet;
        this.rainfall = newPlanet.rainfall();
        this.preciseHeights = newPlanet.preciseHeights();
        this.adjacency = newPlanet.adjacency();
        this.plateAssignment = newPlanet.plateAssignment();
        this.boundaryAnalysis = newPlanet.boundaryAnalysis();
    }

    private void applyConfig(PlanetConfig config) {
        this.seed = config != null ? config.seed() : System.nanoTime();
        this.plateCount = config != null ? config.plateCount() : 12;
        this.waterFraction = config != null ? config.waterFraction() : 0.66;
        this.erosionIterations = config != null ? config.erosionIterations() : 5;
        this.riverThreshold = config != null ? config.riverSourceThreshold() : 0.7;
        this.heightScale = config != null ? config.heightScaleMultiplier() : 1.0;
        this.useContinuousHeights = config != null && config.useContinuousHeights();
        this.reliefMin = config != null ? config.continuousReliefMin() : -4.0;
        this.reliefMax = config != null ? config.continuousReliefMax() : 4.0;
        this.axialTilt = config != null ? config.axialTiltDegrees() : 23.5;
        this.seasonalOffset = config != null ? config.seasonalOffsetDegrees() : 0.0;
        this.size = config != null ? deriveSizeFromN(config.n()) : PlanetConfig.Size.STANDARD;
        this.climateModel = config != null
                ? config.climateModel()
                : ClimateCalculator.ClimateModel.SIMPLE_LATITUDE;
    }

    void captureGenerationControls(long seed,
                                   int plateCount,
                                   double waterFraction,
                                   int erosionIterations,
                                   double riverThreshold,
                                   double heightScale,
                                   boolean useContinuousHeights,
                                   double reliefMin,
                                   double reliefMax,
                                   double axialTilt,
                                   double seasonalOffset,
                                   PlanetConfig.Size size,
                                   ClimateCalculator.ClimateModel climateModel) {
        this.seed = seed;
        this.plateCount = plateCount;
        this.waterFraction = waterFraction;
        this.erosionIterations = erosionIterations;
        this.riverThreshold = riverThreshold;
        this.heightScale = heightScale;
        this.useContinuousHeights = useContinuousHeights;
        this.reliefMin = reliefMin;
        this.reliefMax = reliefMax;
        this.axialTilt = axialTilt;
        this.seasonalOffset = seasonalOffset;
        this.size = size;
        this.climateModel = climateModel;
    }

    PlanetConfig buildConfig() {
        return PlanetConfig.builder()
                .seed(seed)
                .size(size)
                .plateCount(plateCount)
                .waterFraction(waterFraction)
                .erosionIterations(erosionIterations)
                .riverSourceThreshold(riverThreshold)
                .heightScaleMultiplier(heightScale)
                .useContinuousHeights(useContinuousHeights)
                .continuousReliefMin(reliefMin)
                .continuousReliefMax(reliefMax)
                .climateModel(climateModel)
                .axialTiltDegrees(axialTilt)
                .seasonalOffsetDegrees(seasonalOffset)
                .build();
    }

    TerrainType determineTerrainType() {
        return PlanetTerrainClassifier.classify(new PlanetTerrainClassifier.Inputs(
                planetType,
                surfaceTemperatureK,
                waterFractionForRendering(),
                iceCoverFraction,
                densityGcm3,
                semiMajorAxisAU));
    }

    double waterFractionForRendering() {
        return planet.config() != null ? planet.config().waterFraction() : waterFraction;
    }

    boolean hasLakes() {
        boolean[] lakeMask = planet.lakeMask();
        if (lakeMask == null) {
            return false;
        }
        for (boolean isLake : lakeMask) {
            if (isLake) {
                return true;
            }
        }
        return false;
    }

    boolean hasFlowAccumulation() {
        double[] accumulation = planet.flowAccumulation();
        return accumulation != null && accumulation.length > 0;
    }

    boolean hasPlateData() {
        return plateAssignment != null && boundaryAnalysis != null;
    }

    boolean hasClimateData() {
        return planet.climates() != null && planet.climates().length > 0;
    }

    boolean hasPreciseHeights() {
        return preciseHeights != null && preciseHeights.length > 0;
    }

    boolean hasRainfall() {
        return rainfall != null && rainfall.length > 0;
    }

    int polygonCount() {
        return planet.polygons().size();
    }

    int riverCount() {
        return planet.rivers() != null ? planet.rivers().size() : 0;
    }

    int plateCountForDisplay() {
        return plateAssignment != null ? plateAssignment.plates().size() : 0;
    }

    GeneratedPlanet planet() {
        return planet;
    }

    double[] rainfall() {
        return rainfall;
    }

    double[] preciseHeights() {
        return preciseHeights;
    }

    AdjacencyGraph adjacency() {
        return adjacency;
    }

    PlateAssigner.PlateAssignment plateAssignment() {
        return plateAssignment;
    }

    BoundaryDetector.BoundaryAnalysis boundaryAnalysis() {
        return boundaryAnalysis;
    }

    long seed() {
        return seed;
    }

    void setSeed(long seed) {
        this.seed = seed;
    }

    int plateCount() {
        return plateCount;
    }

    double waterFraction() {
        return waterFraction;
    }

    void setWaterFraction(double waterFraction) {
        this.waterFraction = waterFraction;
    }

    int erosionIterations() {
        return erosionIterations;
    }

    double riverThreshold() {
        return riverThreshold;
    }

    void setRiverThreshold(double riverThreshold) {
        this.riverThreshold = riverThreshold;
    }

    double heightScale() {
        return heightScale;
    }

    void setHeightScale(double heightScale) {
        this.heightScale = heightScale;
    }

    boolean useContinuousHeights() {
        return useContinuousHeights;
    }

    double reliefMin() {
        return reliefMin;
    }

    double reliefMax() {
        return reliefMax;
    }

    double axialTilt() {
        return axialTilt;
    }

    void setAxialTilt(double axialTilt) {
        this.axialTilt = axialTilt;
    }

    double seasonalOffset() {
        return seasonalOffset;
    }

    void setSeasonalOffset(double seasonalOffset) {
        this.seasonalOffset = seasonalOffset;
    }

    PlanetConfig.Size size() {
        return size;
    }

    ClimateCalculator.ClimateModel climateModel() {
        return climateModel;
    }

    double surfaceTemperatureK() {
        return surfaceTemperatureK;
    }

    void setSurfaceTemperature(double surfaceTemperatureK) {
        this.surfaceTemperatureK = surfaceTemperatureK;
    }

    void setPlanetType(String planetType) {
        this.planetType = planetType;
    }

    void setIceCover(double iceCoverFraction) {
        this.iceCoverFraction = iceCoverFraction;
    }

    void setDensity(double densityGcm3) {
        this.densityGcm3 = densityGcm3;
    }

    void setSemiMajorAxis(double semiMajorAxisAU) {
        this.semiMajorAxisAU = semiMajorAxisAU;
    }

    static PlanetConfig.Size deriveSizeFromN(int n) {
        for (PlanetConfig.Size candidate : PlanetConfig.Size.values()) {
            if (candidate.n == n) {
                return candidate;
            }
        }

        PlanetConfig.Size closest = PlanetConfig.Size.STANDARD;
        int minDiff = Integer.MAX_VALUE;
        for (PlanetConfig.Size candidate : PlanetConfig.Size.values()) {
            int diff = Math.abs(candidate.n - n);
            if (diff < minDiff) {
                minDiff = diff;
                closest = candidate;
            }
        }
        return closest;
    }
}
