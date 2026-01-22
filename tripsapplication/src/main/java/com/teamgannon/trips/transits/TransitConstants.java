package com.teamgannon.trips.transits;

/**
 * Shared constants for the transits package.
 */
public final class TransitConstants {

    private TransitConstants() {
        // Prevent instantiation
    }

    // Transit range configuration
    public static final double RANGE_MIN = 0.0;
    public static final double RANGE_MAX = 20.0;
    public static final double RANGE_MAJOR_TICK = 5.0;
    public static final int RANGE_MINOR_TICK_COUNT = 5;

    // Default transit line properties
    public static final double DEFAULT_LINE_WIDTH = 1.0;
    public static final double DEFAULT_BAND_LINE_WIDTH = 0.5;

    // 3D rendering
    public static final double LABEL_ANCHOR_SPHERE_RADIUS = 1.0;

    // Label positioning
    public static final double LABEL_PADDING = 20.0;
    public static final double LABEL_EDGE_MARGIN = 5.0;
}
