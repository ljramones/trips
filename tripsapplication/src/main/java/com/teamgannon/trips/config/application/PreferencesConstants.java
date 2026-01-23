package com.teamgannon.trips.config.application;

/**
 * Centralized constants for preferences system to avoid magic strings.
 */
public final class PreferencesConstants {

    private PreferencesConstants() {
        // Prevent instantiation
    }

    /**
     * Primary key ID for the singleton TripsPrefs entity.
     */
    public static final String MAIN_PREFS_ID = "main";

    /**
     * Storage tag for the singleton CivilizationDisplayPreferences entity.
     */
    public static final String CIVILIZATION_STORAGE_TAG = "Main";

    /**
     * Default number of visible star labels.
     */
    public static final int DEFAULT_VISIBLE_LABELS = 30;

    /**
     * Default grid line width.
     */
    public static final double DEFAULT_GRID_LINE_WIDTH = 0.5;

    /**
     * Default stem line width.
     */
    public static final double DEFAULT_STEM_LINE_WIDTH = 0.5;

    /**
     * Default route line width.
     */
    public static final double DEFAULT_ROUTE_LINE_WIDTH = 2.0;
}
