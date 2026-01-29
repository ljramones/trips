package com.teamgannon.trips.experimental.rings;

import javafx.scene.paint.Color;

/**
 * Configuration parameters for a ring/particle field system.
 * This is the base structure used by all ring types.
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
        String name
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

        public RingConfiguration build() {
            return new RingConfiguration(
                    type, innerRadius, outerRadius, numElements,
                    minSize, maxSize, thickness, maxInclinationDeg,
                    maxEccentricity, baseAngularSpeed, centralBodyRadius,
                    primaryColor, secondaryColor, name
            );
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
