package com.teamgannon.trips.planetarymodelling.procedural;

import org.hipparchus.geometry.euclidean.threed.Vector3D;
import java.util.List;

/**
 * Immutable configuration for procedural planet generation.
 * Stores seed + params for reproducible reload/re-gen.
 */
public record PlanetConfig(
    long seed,
    int n,                        // subdivision level (11-32)
    int polyCount,                // derived from n
    int plateCount,               // tectonic plates (7-21)
    double radius,                // planetary radius from Accrete (km)
    double waterFraction,         // 0.0-1.0, default ~0.66
    double oceanicPlateRatio,     // 0.0-1.0, proportion of plates that start as oceanic
    double heightScaleMultiplier, // multiplier for mountain/rift heights (default 1.0)
    double riftDepthMultiplier,   // multiplier for divergent boundary depths
    double hotspotProbability,    // 0.0-1.0, chance of volcanic hotspots
    boolean enableActiveTectonics, // whether full plate tectonics is active (vs stagnant lid)

    // Erosion parameters
    int erosionIterations,        // 0-10, number of sediment flow iterations (default 5)
    double rainfallScale,         // 0.0-2.0, multiplier for rainfall amounts (default 1.0)
    boolean enableRivers,         // whether to carve river valleys (default true)

    // Erosion thresholds (configurable for different planet types)
    double rainfallThreshold,     // 0.0-1.0, min rainfall for erosion (default 0.3 = semi-arid)
    double riverSourceThreshold,  // 0.0-1.0, min rainfall for river sources (default 0.7 = humid)
    double riverSourceElevationMin, // 0.0-1.0, min elevation for river sources (default 0.5 = hills+)
    double erosionCap,            // max erosion per iteration (default 0.3 height units)
    double depositionFactor,      // fraction of eroded sediment deposited (default 0.5 = 50%)
    double riverCarveDepth,       // max river valley depth at source (default 0.3 height units)

    // Terrain distribution parameters
    double maxMountainPercentage,
    double mountainReductionChance,
    double minFarmablePercentage,
    double farmableCreationChance,
    double maxHillPercentage,
    double hillReductionChance,
    double maxLowlandPercentage,
    double lowlandIncreaseChance,

    // Plate assigner parameters
    List<Double> distortionProgressThresholds,
    List<Double> distortionValues,

    // Boundary effect parameters - controls terrain generation at plate boundaries
    // These define how much area is affected (percent), height change (delta), and irregularity (distortion)
    // for each type of plate interaction. See ElevationCalculator for usage.
    BoundaryEffectConfig boundaryEffects,

    // Climate model selection for atmospheric/temperature simulation
    ClimateCalculator.ClimateModel climateModel
) {

    /**
     * Configuration for terrain effects at plate boundaries.
     * Each boundary type (convergent, divergent, transform) produces different terrain
     * based on the plate types involved (oceanic vs continental).
     *
     * <p>Parameter meanings:
     * <ul>
     *   <li>percent: Fraction of plate area affected (0.0-1.0)</li>
     *   <li>delta: Height change direction (+1 = uplift, -1 = subsidence)</li>
     *   <li>distortion: Irregularity of affected area (0.0-1.0, higher = more irregular)</li>
     * </ul>
     *
     * <p>Physical basis:
     * <ul>
     *   <li>Convergent oceanic-oceanic: Island arcs form as one plate subducts</li>
     *   <li>Convergent oceanic-continental: Mountains form inland, trench at coast</li>
     *   <li>Convergent continental-continental: High mountain ranges (Himalayas)</li>
     *   <li>Divergent continental: Rift valleys (East African Rift)</li>
     *   <li>Transform: Minor uplift/depression along fault line</li>
     * </ul>
     */
    public record BoundaryEffectConfig(
        // Convergent boundary effects
        EffectParams convergentOceanicOceanic,      // Island arc formation
        EffectParams convergentOceanicContinental,  // Subduction with coastal mountains
        EffectParams convergentContinentalOceanic,  // Reverse subduction
        EffectParams convergentContinentalContinental, // Collision mountains

        // Divergent boundary effects
        EffectParams divergentOceanicContinental,   // Passive margin
        EffectParams divergentContinentalOceanic,   // Rift initiation
        EffectParams divergentContinentalContinental, // Continental rift valley

        // Transform boundary effects
        EffectParams transformOceanicContinental,   // Coastal fault uplift
        EffectParams transformContinentalOceanic,   // Coastal fault depression
        EffectParams transformContinentalContinental, // Strike-slip basin

        // Probability for random variations
        double continentalCollisionExtraChance      // Chance of extra uplift at cont-cont collision
    ) {
        /**
         * Parameters for a single boundary effect application.
         * Multiple effects can be applied for the same boundary type (layered terrain).
         */
        public record EffectParams(
            double[] percents,      // Area percentages for each layer
            int[] deltas,           // Height change for each layer (+1/-1)
            double[] distortions    // Irregularity for each layer
        ) {
            public int layerCount() {
                return percents.length;
            }
        }

        /**
         * Creates default boundary effect configuration matching original algorithm.
         * These values were empirically tuned for realistic terrain generation.
         */
        public static BoundaryEffectConfig defaults() {
            return new BoundaryEffectConfig(
                // Convergent oceanic-oceanic: Small island arc (15% area, low distortion)
                new EffectParams(
                    new double[]{0.15},
                    new int[]{1},
                    new double[]{0.10}
                ),
                // Convergent oceanic-continental: Coastal mountains + foothills + trench
                new EffectParams(
                    new double[]{0.35, 0.15, 0.10},
                    new int[]{1, 1, 1},
                    new double[]{0.40, 0.25, 0.25}
                ),
                // Convergent continental-oceanic: Mountains + coastal hills
                new EffectParams(
                    new double[]{0.25, 0.15},
                    new int[]{1, 1},
                    new double[]{0.35, 0.10}
                ),
                // Convergent continental-continental: Major mountain range
                new EffectParams(
                    new double[]{0.25, 0.10},
                    new int[]{1, 1},
                    new double[]{0.35, 0.10}
                ),
                // Divergent oceanic-continental: Passive margin uplift
                new EffectParams(
                    new double[]{0.15},
                    new int[]{1},
                    new double[]{0.10}
                ),
                // Divergent continental-oceanic: Rift initiation
                new EffectParams(
                    new double[]{0.35, 0.25},
                    new int[]{-1, -1},
                    new double[]{0.35, 0.10}
                ),
                // Divergent continental-continental: Full rift valley (East Africa style)
                new EffectParams(
                    new double[]{0.60, 0.45, 0.35, 0.25, 0.15},
                    new int[]{-1, -1, -1, -1, -1},
                    new double[]{0.55, 0.40, 0.20, 0.10, 0.10}
                ),
                // Transform oceanic-continental: Coastal fault uplift
                new EffectParams(
                    new double[]{0.35, 0.25},
                    new int[]{1, 1},
                    new double[]{0.40, 0.25}
                ),
                // Transform continental-oceanic: Coastal fault depression
                new EffectParams(
                    new double[]{0.35},
                    new int[]{-1},
                    new double[]{0.25}
                ),
                // Transform continental-continental: Minor pull-apart basin (rare)
                new EffectParams(
                    new double[]{0.20, 0.15},
                    new int[]{-1, -1},
                    new double[]{0.25, 0.15}
                ),
                // 50% chance of extra uplift at continental collision
                0.50
            );
        }
    }

    /** Size presets matching original GDScript */
    public enum Size {
        DUEL(11, 1212),
        TINY(15, 2252),
        SMALL(19, 3612),
        STANDARD(21, 4412),
        LARGE(24, 5762),
        HUGE(26, 6762),
        COLOSSAL(32, 10242);

        public final int n;
        public final int polyCount;

        Size(int n, int polyCount) {
            this.n = n;
            this.polyCount = polyCount;
        }
    }

    /** Computed radius of Goldberg polyhedron (unit sphere scaling) */
    public double goldbergRadius() {
        double phi = (1 + Math.sqrt(5)) / 2;
        Vector3D v1 = new Vector3D(0.5, 0, phi / 2);
        Vector3D v2 = new Vector3D(
            -1.0 / (2.0 * n),
            Math.sin(0.5 * Math.acos(-Math.sqrt(5) / 3)) / (2 * Math.sqrt(3) * n),
            -Math.cos(0.5 * Math.acos(-Math.sqrt(5) / 3)) / (2 * Math.sqrt(3) * n)
        );
        Vector3D v3 = new Vector3D(
            -1.0 / (2.0 * n),
            -Math.sin(0.5 * Math.acos(-Math.sqrt(5) / 3)) / (2 * Math.sqrt(3) * n),
            -Math.cos(0.5 * Math.acos(-Math.sqrt(5) / 3)) / (2 * Math.sqrt(3) * n)
        );
        double angle = Vector3D.angle(v1.add(v2), v1.add(v3));
        return Math.sqrt(1.0 / (2.0 * (1.0 - Math.cos(angle))));
    }

    /** Derive sub-seed for specific generation phase */
    public long subSeed(int phase) {
        return seed ^ (phase * 0x9E3779B97F4A7C15L);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private long seed = System.nanoTime();
        private Size size = Size.STANDARD;
        // Default 14 plates matches Earth's major plate count (7 major + 7 minor).
        private int plateCount = 14;
        // Default Earth radius in km.
        private double radius = 6371.0;
        // Default 66% water matches Earth's ocean coverage.
        private double waterFraction = 0.66;
        // 65% oceanic plates: Earth's oceanic crust is ~60-70% of surface by area.
        // Higher values create more ocean-dominated worlds (waterworlds).
        // Lower values create more continental land mass.
        private double oceanicPlateRatio = 0.65;
        // Height/depth multiplier for terrain extremes (1.0 = Earth-like).
        private double heightScaleMultiplier = 1.0;
        private double riftDepthMultiplier = 1.0;
        // 12% hotspot probability per plate: Earth has ~40-50 active hotspots
        // across 15 plates, roughly 3 per plate, but not all plates have one.
        // 12% gives ~1-2 hotspots per planet, creating volcanic island chains.
        private double hotspotProbability = 0.12;
        private boolean enableActiveTectonics = true;
        // Erosion defaults
        private int erosionIterations = 5;
        private double rainfallScale = 1.0;
        private boolean enableRivers = true;
        // Erosion threshold defaults (Earth-like values)
        private double rainfallThreshold = 0.3;       // Semi-arid minimum
        private double riverSourceThreshold = 0.7;    // Humid conditions for rivers
        private double riverSourceElevationMin = 0.5; // Hills or higher
        private double erosionCap = 0.3;              // Max erosion per pass
        private double depositionFactor = 0.5;        // 50% sediment deposited
        private double riverCarveDepth = 0.3;         // Max valley depth at source
        // Terrain distribution defaults
        private double maxMountainPercentage = 0.05;
        private double mountainReductionChance = 0.65;
        private double minFarmablePercentage = 0.15;
        private double farmableCreationChance = 0.75;
        private double maxHillPercentage = 0.14;
        private double hillReductionChance = 0.35;
        private double maxLowlandPercentage = 0.08;
        private double lowlandIncreaseChance = 0.35;
        // Plate assigner defaults
        private List<Double> distortionProgressThresholds = List.of(0.25, 0.50, 1.00);
        private List<Double> distortionValues = List.of(0.1, 0.2, 0.7);
        // Boundary effect defaults
        private BoundaryEffectConfig boundaryEffects = BoundaryEffectConfig.defaults();
        // Climate model defaults to simple latitude-based zones (Earth-like)
        private ClimateCalculator.ClimateModel climateModel = ClimateCalculator.ClimateModel.SIMPLE_LATITUDE;


        public Builder seed(long seed) { this.seed = seed; return this; }
        public Builder size(Size size) { this.size = size; return this; }
        public Builder plateCount(int count) { this.plateCount = Math.max(7, Math.min(21, count)); return this; }
        public Builder radius(double radius) { this.radius = radius; return this; }
        public Builder waterFraction(double fraction) { this.waterFraction = Math.max(0.0, Math.min(1.0, fraction)); return this; }

        public Builder oceanicPlateRatio(double ratio) {
            this.oceanicPlateRatio = Math.max(0.0, Math.min(1.0, ratio));
            return this;
        }

        public Builder heightScaleMultiplier(double multiplier) {
            this.heightScaleMultiplier = Math.max(0.5, multiplier);
            return this;
        }

        public Builder riftDepthMultiplier(double multiplier) {
            this.riftDepthMultiplier = Math.max(0.5, multiplier);
            return this;
        }

        public Builder hotspotProbability(double probability) {
            this.hotspotProbability = Math.max(0.0, Math.min(1.0, probability));
            return this;
        }

        public Builder enableActiveTectonics(boolean enabled) {
            this.enableActiveTectonics = enabled;
            return this;
        }

        public Builder erosionIterations(int iterations) {
            this.erosionIterations = Math.max(0, Math.min(10, iterations));
            return this;
        }

        public Builder rainfallScale(double scale) {
            this.rainfallScale = Math.max(0.0, Math.min(2.0, scale));
            return this;
        }

        public Builder enableRivers(boolean enabled) {
            this.enableRivers = enabled;
            return this;
        }

        public Builder rainfallThreshold(double threshold) {
            this.rainfallThreshold = Math.max(0.0, Math.min(1.0, threshold));
            return this;
        }

        public Builder riverSourceThreshold(double threshold) {
            this.riverSourceThreshold = Math.max(0.0, Math.min(1.0, threshold));
            return this;
        }

        public Builder riverSourceElevationMin(double min) {
            this.riverSourceElevationMin = Math.max(0.0, Math.min(1.0, min));
            return this;
        }

        public Builder erosionCap(double cap) {
            this.erosionCap = Math.max(0.0, Math.min(1.0, cap));
            return this;
        }

        public Builder depositionFactor(double factor) {
            this.depositionFactor = Math.max(0.0, Math.min(1.0, factor));
            return this;
        }

        public Builder riverCarveDepth(double depth) {
            this.riverCarveDepth = Math.max(0.0, Math.min(1.0, depth));
            return this;
        }

        public Builder fromAccreteRadius(double radiusKm) {
            this.radius = radiusKm;
            if (radiusKm < 3000) this.size = Size.SMALL;
            else if (radiusKm < 6000) this.size = Size.STANDARD;
            else if (radiusKm < 10000) this.size = Size.LARGE;
            else this.size = Size.HUGE;
            return this;
        }

        public Builder maxMountainPercentage(double val) { this.maxMountainPercentage = val; return this; }
        public Builder mountainReductionChance(double val) { this.mountainReductionChance = val; return this; }
        public Builder minFarmablePercentage(double val) { this.minFarmablePercentage = val; return this; }
        public Builder farmableCreationChance(double val) { this.farmableCreationChance = val; return this; }
        public Builder maxHillPercentage(double val) { this.maxHillPercentage = val; return this; }
        public Builder hillReductionChance(double val) { this.hillReductionChance = val; return this; }
        public Builder maxLowlandPercentage(double val) { this.maxLowlandPercentage = val; return this; }
        public Builder lowlandIncreaseChance(double val) { this.lowlandIncreaseChance = val; return this; }
        public Builder distortionProgressThresholds(List<Double> thresholds) { this.distortionProgressThresholds = thresholds; return this; }
        public Builder distortionValues(List<Double> values) { this.distortionValues = values; return this; }
        public Builder boundaryEffects(BoundaryEffectConfig effects) { this.boundaryEffects = effects; return this; }

        /**
         * Sets the climate model for atmospheric simulation.
         * @param model Climate model to use (default: SIMPLE_LATITUDE)
         */
        public Builder climateModel(ClimateCalculator.ClimateModel model) {
            this.climateModel = model != null ? model : ClimateCalculator.ClimateModel.SIMPLE_LATITUDE;
            return this;
        }


        public PlanetConfig build() {
            if (distortionProgressThresholds.size() != distortionValues.size()) {
                throw new IllegalArgumentException("Distortion thresholds and values must have the same size.");
            }
            return new PlanetConfig(
                seed, size.n, size.polyCount, plateCount, radius, waterFraction,
                oceanicPlateRatio, heightScaleMultiplier, riftDepthMultiplier,
                hotspotProbability, enableActiveTectonics,
                erosionIterations, rainfallScale, enableRivers,
                rainfallThreshold, riverSourceThreshold, riverSourceElevationMin,
                erosionCap, depositionFactor, riverCarveDepth,
                maxMountainPercentage, mountainReductionChance, minFarmablePercentage,
                farmableCreationChance, maxHillPercentage, hillReductionChance,
                maxLowlandPercentage, lowlandIncreaseChance,
                distortionProgressThresholds, distortionValues,
                boundaryEffects, climateModel
            );
        }
    }

    /**
     * Creates a new Builder pre-populated with this config's values.
     * Useful for creating modified copies of an existing config.
     */
    public Builder toBuilder() {
        return PlanetConfig.builder()
            .seed(seed)
            .size(sizeFromN(n))
            .plateCount(plateCount)
            .radius(radius)
            .waterFraction(waterFraction)
            .oceanicPlateRatio(oceanicPlateRatio)
            .heightScaleMultiplier(heightScaleMultiplier)
            .riftDepthMultiplier(riftDepthMultiplier)
            .hotspotProbability(hotspotProbability)
            .enableActiveTectonics(enableActiveTectonics)
            .erosionIterations(erosionIterations)
            .rainfallScale(rainfallScale)
            .enableRivers(enableRivers)
            .rainfallThreshold(rainfallThreshold)
            .riverSourceThreshold(riverSourceThreshold)
            .riverSourceElevationMin(riverSourceElevationMin)
            .erosionCap(erosionCap)
            .depositionFactor(depositionFactor)
            .riverCarveDepth(riverCarveDepth)
            .maxMountainPercentage(maxMountainPercentage)
            .mountainReductionChance(mountainReductionChance)
            .minFarmablePercentage(minFarmablePercentage)
            .farmableCreationChance(farmableCreationChance)
            .maxHillPercentage(maxHillPercentage)
            .hillReductionChance(hillReductionChance)
            .maxLowlandPercentage(maxLowlandPercentage)
            .lowlandIncreaseChance(lowlandIncreaseChance)
            .distortionProgressThresholds(distortionProgressThresholds)
            .distortionValues(distortionValues)
            .boundaryEffects(boundaryEffects)
            .climateModel(climateModel);
    }

    /**
     * Derives Size enum from subdivision level n.
     */
    private static Size sizeFromN(int n) {
        for (Size s : Size.values()) {
            if (s.n == n) return s;
        }
        // Default to closest match
        if (n <= 13) return Size.DUEL;
        if (n <= 17) return Size.TINY;
        if (n <= 20) return Size.SMALL;
        if (n <= 22) return Size.STANDARD;
        if (n <= 25) return Size.LARGE;
        if (n <= 29) return Size.HUGE;
        return Size.COLOSSAL;
    }
}
