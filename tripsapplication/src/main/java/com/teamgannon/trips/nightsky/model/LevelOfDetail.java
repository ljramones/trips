package com.teamgannon.trips.nightsky.model;

/**
 * Level of detail for star queries.
 * Controls the trade-off between accuracy and performance.
 */
public enum LevelOfDetail {
    /** Maximum stars, full precision - for close-up detailed views */
    ULTRA(100000, 10.0f),

    /** High quality - default for normal viewing */
    HIGH(50000, 8.0f),

    /** Medium quality - for performance on large datasets */
    MEDIUM(20000, 6.5f),

    /** Low quality - for quick previews or mobile */
    LOW(5000, 5.0f);

    private final int maxStars;
    private final float magnitudeLimit;

    LevelOfDetail(int maxStars, float magnitudeLimit) {
        this.maxStars = maxStars;
        this.magnitudeLimit = magnitudeLimit;
    }

    public int getMaxStars() {
        return maxStars;
    }

    public float getMagnitudeLimit() {
        return magnitudeLimit;
    }
}
