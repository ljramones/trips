package com.teamgannon.trips.routing;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

/**
 * Constants used throughout the routing package.
 * <p>
 * Centralizes magic numbers and default values to:
 * <ul>
 *   <li>Make configuration changes easier</li>
 *   <li>Improve code readability</li>
 *   <li>Ensure consistency across dialogs</li>
 * </ul>
 */
public final class RoutingConstants {

    private RoutingConstants() {
        // Prevent instantiation
    }

    // =========================================================================
    // Graph Algorithm Constants
    // =========================================================================

    /**
     * Maximum number of stars for route finding before performance degrades.
     * Beyond this threshold, route finding may become slow or memory-intensive.
     */
    public static final int GRAPH_THRESHOLD = 1500;

    /**
     * Default number of alternative paths to find using Yen's K-shortest path algorithm.
     */
    public static final int DEFAULT_NUMBER_PATHS = 3;

    /**
     * Maximum number of paths that can be requested.
     */
    public static final int MAX_NUMBER_PATHS = 10;

    // =========================================================================
    // Route Display Constants
    // =========================================================================

    /**
     * Default line width for route visualization.
     */
    public static final double DEFAULT_LINE_WIDTH = 0.5;

    /**
     * Minimum allowed line width.
     */
    public static final double MIN_LINE_WIDTH = 0.1;

    /**
     * Maximum allowed line width.
     */
    public static final double MAX_LINE_WIDTH = 5.0;

    /**
     * Default route color when none is specified.
     */
    public static final Color DEFAULT_ROUTE_COLOR = Color.LIGHTCORAL;

    // =========================================================================
    // Distance Bounds Constants
    // =========================================================================

    /**
     * Default upper bound for transit distance (light years).
     */
    public static final double DEFAULT_UPPER_DISTANCE = 8.0;

    /**
     * Default lower bound for transit distance (light years).
     */
    public static final double DEFAULT_LOWER_DISTANCE = 0.0;

    /**
     * Maximum allowed transit distance (light years).
     */
    public static final double MAX_TRANSIT_DISTANCE = 100.0;

    // =========================================================================
    // UI Font Constants
    // =========================================================================

    /**
     * Default font family for routing dialogs.
     */
    public static final String FONT_FAMILY = "Verdana";

    /**
     * Default font size for dialog labels.
     */
    public static final int DEFAULT_FONT_SIZE = 13;

    /**
     * Font size for performance/info dialogs.
     */
    public static final int INFO_FONT_SIZE = 12;

    /**
     * Creates the standard bold font for routing dialog labels.
     *
     * @return the standard dialog font
     */
    public static Font createDialogFont() {
        return Font.font(FONT_FAMILY, FontWeight.BOLD, FontPosture.REGULAR, DEFAULT_FONT_SIZE);
    }

    /**
     * Creates the italic font for info/performance dialogs.
     *
     * @return the info dialog font
     */
    public static Font createInfoFont() {
        return Font.font(FONT_FAMILY, FontWeight.BOLD, FontPosture.ITALIC, INFO_FONT_SIZE);
    }

    // =========================================================================
    // UI Layout Constants
    // =========================================================================

    /**
     * Standard spacing between grid elements in dialogs.
     */
    public static final int GRID_HGAP = 10;

    /**
     * Standard vertical gap between grid rows in dialogs.
     */
    public static final int GRID_VGAP = 10;

    /**
     * Standard width for checkbox controls.
     */
    public static final int CHECKBOX_WIDTH = 100;

    /**
     * Standard padding for dialog content.
     */
    public static final int DIALOG_PADDING = 20;

    // =========================================================================
    // Validation Messages
    // =========================================================================

    /**
     * Error message when line width is not a valid number.
     */
    public static final String INVALID_LINE_WIDTH_MSG = "Line width must be a number between %.1f and %.1f";

    /**
     * Error message when upper bound is less than lower bound.
     */
    public static final String INVALID_BOUNDS_MSG = "Upper bound must be greater than lower bound";

    /**
     * Error message when origin equals destination.
     */
    public static final String SAME_STAR_MSG = "Origin and destination cannot be the same star";

    // =========================================================================
    // Route Naming
    // =========================================================================

    /**
     * Default route name prefix.
     */
    public static final String DEFAULT_ROUTE_NAME_PREFIX = "Route";

    /**
     * Format string for auto-generated route names.
     * Arguments: source, destination, path number
     */
    public static final String ROUTE_NAME_FORMAT = "Route %s to %s, path %s";
}
