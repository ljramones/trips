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
     * Maximum number of stars for route finding.
     * <p>
     * With KD-tree based graph building (O(n log n)), we can handle much larger
     * datasets than the previous O(n²) brute-force approach. The limiting factor
     * is now Yen's K-shortest paths algorithm rather than edge discovery.
     */
    public static final int GRAPH_THRESHOLD = 10000;

    /**
     * Threshold for switching from brute-force to KD-Tree algorithm.
     * Below this count, the O(n²) algorithm is faster due to lower overhead.
     * Above this count, KD-Tree O(n log n) provides significant speedup.
     */
    public static final int KDTREE_THRESHOLD = 100;

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

    // =========================================================================
    // Label Display Constants
    // =========================================================================

    /**
     * Padding from viewport edges for label visibility clipping.
     * Labels closer than this to the edge are hidden.
     */
    public static final double LABEL_CLIPPING_PADDING = 20.0;

    /**
     * Margin for clamping labels to prevent them from going off-screen.
     */
    public static final double LABEL_EDGE_MARGIN = 5.0;

    /**
     * Corner radius for label background.
     */
    public static final double LABEL_CORNER_RADIUS = 5.0;

    // =========================================================================
    // 3D Graphics Constants
    // =========================================================================

    /**
     * Radius for route endpoint marker spheres.
     */
    public static final double ROUTE_POINT_SPHERE_RADIUS = 1.0;

    /**
     * Threshold for determining if text should be light or dark based on background.
     * If the sum of RGB components (0-255 each) is less than this, use light text.
     * Value of 384 = 128 * 3, meaning if average component < 128, background is "dark".
     */
    public static final int DARK_BACKGROUND_THRESHOLD = 384;

    /**
     * Maximum value for a single RGB color component.
     */
    public static final double RGB_MAX_VALUE = 255.0;

    // =========================================================================
    // Routing Panel UI Constants
    // =========================================================================

    /**
     * Preferred height for the routing table view.
     */
    public static final int ROUTING_TABLE_PREFERRED_HEIGHT = 800;

    /**
     * Column width for the "show route" checkbox column.
     */
    public static final int SHOW_ROUTE_COL_WIDTH = 40;

    /**
     * Column width for the route status column.
     */
    public static final int STATUS_COL_WIDTH = 80;

    /**
     * Column width for the color column.
     */
    public static final int COLOR_COL_WIDTH = 65;

    /**
     * Column width for the route name column.
     */
    public static final int ROUTE_NAME_COL_WIDTH = 300;

    // =========================================================================
    // Dialog Layout Constants
    // =========================================================================

    /**
     * Minimum width for progress dialogs.
     */
    public static final int PROGRESS_DIALOG_MIN_WIDTH = 600;

    /**
     * Minimum width for progress bars.
     */
    public static final int PROGRESS_BAR_MIN_WIDTH = 500;

    /**
     * Preferred width for route selection tables.
     */
    public static final int ROUTE_TABLE_WIDTH = 750;

    /**
     * Standard spacing between buttons in dialogs.
     */
    public static final int BUTTON_SPACING = 5;

    /**
     * Standard padding for grid panes (all sides).
     */
    public static final int GRID_PADDING = 10;

    // =========================================================================
    // Default Input Values
    // =========================================================================

    /**
     * Default lower bound for transit distance in dialogs.
     */
    public static final String DEFAULT_LOWER_BOUND_TEXT = "3";

    /**
     * Default upper bound for transit distance in dialogs.
     */
    public static final String DEFAULT_UPPER_BOUND_TEXT = "8";

    /**
     * Default line width text for dialogs.
     */
    public static final String DEFAULT_LINE_WIDTH_TEXT = "0.5";

    /**
     * Default number of paths text for dialogs.
     */
    public static final String DEFAULT_NUM_PATHS_TEXT = "3";

    /**
     * Maximum transit distance used for validation.
     */
    public static final double MAX_VALIDATION_DISTANCE = 20.0;

    // =========================================================================
    // Route Label Formatting
    // =========================================================================

    /**
     * Prefix text for the first segment label.
     */
    public static final String FIRST_SEGMENT_PREFIX = " Start -> ";

    /**
     * Suffix text for labels (space for padding).
     */
    public static final String LABEL_SUFFIX = " ";
}
