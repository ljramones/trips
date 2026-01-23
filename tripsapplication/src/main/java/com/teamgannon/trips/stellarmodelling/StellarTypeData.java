package com.teamgannon.trips.stellarmodelling;

/**
 * Immutable data record holding all configuration values for a stellar classification.
 * This replaces the repetitive createXClass() methods with a data-driven approach.
 */
public record StellarTypeData(
        StellarType stellarType,
        StarColor starColor,
        String chromaticityKey,
        String color,
        String chromacity,
        int lowerTemperature,
        int upperTemperature,
        double lowerMass,
        double upperMass,
        double lowerRadius,
        double upperRadius,
        double lowerLuminosity,
        double upperLuminosity,
        HydrogenLines hydrogenLines,
        double sequenceFraction
) {

    /**
     * Builder for creating StellarTypeData instances with a fluent API.
     */
    public static class Builder {
        private StellarType stellarType;
        private StarColor starColor;
        private String chromaticityKey;
        private String color;
        private String chromacity;
        private int lowerTemperature;
        private int upperTemperature;
        private double lowerMass;
        private double upperMass;
        private double lowerRadius;
        private double upperRadius;
        private double lowerLuminosity;
        private double upperLuminosity;
        private HydrogenLines hydrogenLines;
        private double sequenceFraction;

        public Builder stellarType(StellarType stellarType) {
            this.stellarType = stellarType;
            return this;
        }

        public Builder starColor(StarColor starColor) {
            this.starColor = starColor;
            return this;
        }

        public Builder chromaticityKey(String chromaticityKey) {
            this.chromaticityKey = chromaticityKey;
            return this;
        }

        public Builder color(String color) {
            this.color = color;
            return this;
        }

        public Builder chromacity(String chromacity) {
            this.chromacity = chromacity;
            return this;
        }

        public Builder temperatureRange(int lower, int upper) {
            this.lowerTemperature = lower;
            this.upperTemperature = upper;
            return this;
        }

        public Builder massRange(double lower, double upper) {
            this.lowerMass = lower;
            this.upperMass = upper;
            return this;
        }

        public Builder radiusRange(double lower, double upper) {
            this.lowerRadius = lower;
            this.upperRadius = upper;
            return this;
        }

        public Builder luminosityRange(double lower, double upper) {
            this.lowerLuminosity = lower;
            this.upperLuminosity = upper;
            return this;
        }

        public Builder hydrogenLines(HydrogenLines lines) {
            this.hydrogenLines = lines;
            return this;
        }

        public Builder sequenceFraction(double fraction) {
            this.sequenceFraction = fraction;
            return this;
        }

        public StellarTypeData build() {
            return new StellarTypeData(
                    stellarType, starColor, chromaticityKey, color, chromacity,
                    lowerTemperature, upperTemperature,
                    lowerMass, upperMass,
                    lowerRadius, upperRadius,
                    lowerLuminosity, upperLuminosity,
                    hydrogenLines, sequenceFraction
            );
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
