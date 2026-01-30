package com.teamgannon.trips.particlefields;

import javafx.scene.paint.Color;

/**
 * Configuration parameters for a ring/particle field system.
 * This is the base structure used by all ring types including nebulae.
 * <p>
 * The nebula-specific parameters (radialPower, noiseStrength, noiseOctaves, seed)
 * are primarily used by DustCloudGenerator but are available for all types.
 */
public record RingConfiguration(
        /** The type of ring system */
        RingType type,

        /** Inner radius of the ring (visual units) */
        double innerRadius,

        /** Outer radius of the ring (visual units) */
        double outerRadius,

        /** Number of particles/elements in the ring */
        int numElements,

        /** Minimum particle size */
        double minSize,

        /** Maximum particle size */
        double maxSize,

        /** Vertical thickness/spread of the ring (visual units) */
        double thickness,

        /** Maximum orbital inclination in degrees (0 = perfectly flat) */
        double maxInclinationDeg,

        /** Maximum orbital eccentricity (0 = circular, <1 = elliptical) */
        double maxEccentricity,

        /** Base angular speed multiplier (affects rotation rate) */
        double baseAngularSpeed,

        /** Radius of the central body */
        double centralBodyRadius,

        /** Primary color for particles */
        Color primaryColor,

        /** Secondary color for particles (for gradients/variation) */
        Color secondaryColor,

        /** Display name for the window title */
        String name,

        // ==================== Nebula-Specific Parameters ====================

        /**
         * Radial power for density falloff (used by DustCloudGenerator).
         * < 0.5 = dense core with gradual falloff (e.g., 0.3 for emission nebulae)
         * = 0.5 = uniform distribution
         * > 0.5 = shell-like, hollow center (e.g., 0.7 for planetary nebulae)
         */
        double radialPower,

        /**
         * Noise strength for filamentary structure (0.0 - 1.0).
         * 0.0 = no noise (smooth distribution)
         * 0.3-0.5 = moderate filaments (typical nebulae)
         * 0.6+ = highly turbulent (supernova remnants)
         */
        double noiseStrength,

        /**
         * Number of noise octaves for detail level (1-6).
         * More octaves = finer detail but more computation.
         * 2-3 = typical, 4+ = high detail
         */
        int noiseOctaves,

        /**
         * Random seed for reproducible procedural generation.
         * Same seed with same parameters = identical particle distribution.
         */
        long seed
) {
    /**
     * Builder for creating RingConfiguration instances with defaults.
     */
    public static class Builder {
        private RingType type = RingType.ASTEROID_BELT;
        private double innerRadius = 50;
        private double outerRadius = 100;
        private int numElements = 5000;
        private double minSize = 0.5;
        private double maxSize = 2.0;
        private double thickness = 5.0;
        private double maxInclinationDeg = 10.0;
        private double maxEccentricity = 0.05;
        private double baseAngularSpeed = 0.002;
        private double centralBodyRadius = 8.0;
        private Color primaryColor = Color.LIGHTGRAY;
        private Color secondaryColor = Color.DARKGRAY;
        private String name = "Ring Field";

        // Nebula-specific defaults
        private double radialPower = 0.5;      // Uniform distribution by default
        private double noiseStrength = 0.0;    // No noise by default
        private int noiseOctaves = 3;          // Moderate detail
        private long seed = System.currentTimeMillis();

        public Builder type(RingType type) {
            this.type = type;
            return this;
        }

        public Builder innerRadius(double innerRadius) {
            this.innerRadius = innerRadius;
            return this;
        }

        public Builder outerRadius(double outerRadius) {
            this.outerRadius = outerRadius;
            return this;
        }

        public Builder numElements(int numElements) {
            this.numElements = numElements;
            return this;
        }

        public Builder minSize(double minSize) {
            this.minSize = minSize;
            return this;
        }

        public Builder maxSize(double maxSize) {
            this.maxSize = maxSize;
            return this;
        }

        public Builder thickness(double thickness) {
            this.thickness = thickness;
            return this;
        }

        public Builder maxInclinationDeg(double maxInclinationDeg) {
            this.maxInclinationDeg = maxInclinationDeg;
            return this;
        }

        public Builder maxEccentricity(double maxEccentricity) {
            this.maxEccentricity = maxEccentricity;
            return this;
        }

        public Builder baseAngularSpeed(double baseAngularSpeed) {
            this.baseAngularSpeed = baseAngularSpeed;
            return this;
        }

        public Builder centralBodyRadius(double centralBodyRadius) {
            this.centralBodyRadius = centralBodyRadius;
            return this;
        }

        public Builder primaryColor(Color primaryColor) {
            this.primaryColor = primaryColor;
            return this;
        }

        public Builder secondaryColor(Color secondaryColor) {
            this.secondaryColor = secondaryColor;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        // Nebula-specific builder methods

        /**
         * Set the radial power for density falloff.
         * @param radialPower 0.0-1.0, where < 0.5 = dense core, > 0.5 = shell-like
         */
        public Builder radialPower(double radialPower) {
            this.radialPower = radialPower;
            return this;
        }

        /**
         * Set the noise strength for filamentary structure.
         * @param noiseStrength 0.0-1.0
         */
        public Builder noiseStrength(double noiseStrength) {
            this.noiseStrength = noiseStrength;
            return this;
        }

        /**
         * Set the number of noise octaves.
         * @param noiseOctaves 1-6 typically
         */
        public Builder noiseOctaves(int noiseOctaves) {
            this.noiseOctaves = noiseOctaves;
            return this;
        }

        /**
         * Set the random seed for reproducible generation.
         * @param seed any long value
         */
        public Builder seed(long seed) {
            this.seed = seed;
            return this;
        }

        public RingConfiguration build() {
            return new RingConfiguration(
                    type, innerRadius, outerRadius, numElements,
                    minSize, maxSize, thickness, maxInclinationDeg,
                    maxEccentricity, baseAngularSpeed, centralBodyRadius,
                    primaryColor, secondaryColor, name,
                    radialPower, noiseStrength, noiseOctaves, seed
            );
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns a copy of this configuration with a new seed.
     * Useful for creating variations of the same nebula.
     */
    public RingConfiguration withSeed(long newSeed) {
        return new RingConfiguration(
                type, innerRadius, outerRadius, numElements,
                minSize, maxSize, thickness, maxInclinationDeg,
                maxEccentricity, baseAngularSpeed, centralBodyRadius,
                primaryColor, secondaryColor, name,
                radialPower, noiseStrength, noiseOctaves, newSeed
        );
    }

    /**
     * Returns a copy with adjusted particle count (for LOD).
     */
    public RingConfiguration withNumElements(int newNumElements) {
        return new RingConfiguration(
                type, innerRadius, outerRadius, newNumElements,
                minSize, maxSize, thickness, maxInclinationDeg,
                maxEccentricity, baseAngularSpeed, centralBodyRadius,
                primaryColor, secondaryColor, name,
                radialPower, noiseStrength, noiseOctaves, seed
        );
    }
}
