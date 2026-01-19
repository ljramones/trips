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
    List<Double> distortionValues
) {

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
        private int plateCount = 14;
        private double radius = 6371.0;
        private double waterFraction = 0.66;
        private double oceanicPlateRatio = 0.65;
        private double heightScaleMultiplier = 1.0;
        private double riftDepthMultiplier = 1.0;
        private double hotspotProbability = 0.12;
        private boolean enableActiveTectonics = true;
        // Erosion defaults
        private int erosionIterations = 5;
        private double rainfallScale = 1.0;
        private boolean enableRivers = true;
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


        public PlanetConfig build() {
            if (distortionProgressThresholds.size() != distortionValues.size()) {
                throw new IllegalArgumentException("Distortion thresholds and values must have the same size.");
            }
            return new PlanetConfig(
                seed, size.n, size.polyCount, plateCount, radius, waterFraction,
                oceanicPlateRatio, heightScaleMultiplier, riftDepthMultiplier,
                hotspotProbability, enableActiveTectonics,
                erosionIterations, rainfallScale, enableRivers,
                maxMountainPercentage, mountainReductionChance, minFarmablePercentage,
                farmableCreationChance, maxHillPercentage, hillReductionChance,
                maxLowlandPercentage, lowlandIncreaseChance,
                distortionProgressThresholds, distortionValues
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
            .maxMountainPercentage(maxMountainPercentage)
            .mountainReductionChance(mountainReductionChance)
            .minFarmablePercentage(minFarmablePercentage)
            .farmableCreationChance(farmableCreationChance)
            .maxHillPercentage(maxHillPercentage)
            .hillReductionChance(hillReductionChance)
            .maxLowlandPercentage(maxLowlandPercentage)
            .lowlandIncreaseChance(lowlandIncreaseChance)
            .distortionProgressThresholds(distortionProgressThresholds)
            .distortionValues(distortionValues);
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
