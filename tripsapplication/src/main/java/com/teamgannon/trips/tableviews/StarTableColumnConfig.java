package com.teamgannon.trips.tableviews;

import org.springframework.data.domain.Sort;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Configuration class for star table columns.
 * Maps UI column IDs to entity field names for server-side sorting.
 */
public class StarTableColumnConfig {

    /**
     * Map column ID to entity field name for sorting.
     * LinkedHashMap preserves insertion order for consistent column display.
     */
    private static final Map<String, String> COLUMN_TO_FIELD = new LinkedHashMap<>();

    static {
        COLUMN_TO_FIELD.put("displayName", "displayName");
        COLUMN_TO_FIELD.put("distanceToEarth", "distance");
        COLUMN_TO_FIELD.put("spectra", "spectralClass");
        COLUMN_TO_FIELD.put("radius", "radius");
        COLUMN_TO_FIELD.put("mass", "mass");
        COLUMN_TO_FIELD.put("luminosity", "luminosity");
        COLUMN_TO_FIELD.put("ra", "ra");
        COLUMN_TO_FIELD.put("declination", "declination");
        COLUMN_TO_FIELD.put("parallax", "parallax");
        COLUMN_TO_FIELD.put("xCoord", "x");
        COLUMN_TO_FIELD.put("yCoord", "y");
        COLUMN_TO_FIELD.put("zCoord", "z");
        COLUMN_TO_FIELD.put("real", "realStar");
        COLUMN_TO_FIELD.put("commonName", "commonName");
        COLUMN_TO_FIELD.put("constellationName", "constellationName");
        COLUMN_TO_FIELD.put("polity", "polity");
        COLUMN_TO_FIELD.put("temperature", "temperature");
    }

    /**
     * Display names for columns (for UI labels)
     */
    private static final Map<String, String> COLUMN_DISPLAY_NAMES = new LinkedHashMap<>();

    static {
        COLUMN_DISPLAY_NAMES.put("displayName", "Display Name");
        COLUMN_DISPLAY_NAMES.put("distanceToEarth", "Distance (LY)");
        COLUMN_DISPLAY_NAMES.put("spectra", "Spectra");
        COLUMN_DISPLAY_NAMES.put("radius", "Radius");
        COLUMN_DISPLAY_NAMES.put("mass", "Mass (M\u2609)");
        COLUMN_DISPLAY_NAMES.put("luminosity", "Luminosity");
        COLUMN_DISPLAY_NAMES.put("ra", "RA");
        COLUMN_DISPLAY_NAMES.put("declination", "Declination");
        COLUMN_DISPLAY_NAMES.put("parallax", "Parallax");
        COLUMN_DISPLAY_NAMES.put("xCoord", "X");
        COLUMN_DISPLAY_NAMES.put("yCoord", "Y");
        COLUMN_DISPLAY_NAMES.put("zCoord", "Z");
        COLUMN_DISPLAY_NAMES.put("real", "Real");
        COLUMN_DISPLAY_NAMES.put("comment", "Comment");
        COLUMN_DISPLAY_NAMES.put("commonName", "Common Name");
        COLUMN_DISPLAY_NAMES.put("constellationName", "Constellation");
        COLUMN_DISPLAY_NAMES.put("polity", "Polity");
        COLUMN_DISPLAY_NAMES.put("temperature", "Temperature");
    }

    /**
     * Set of currently visible columns
     */
    private final Set<String> visibleColumns;

    /**
     * Create a new column config with default visible columns
     */
    public StarTableColumnConfig() {
        this.visibleColumns = new LinkedHashSet<>(getDefaultVisibleColumns());
    }

    /**
     * Get the default set of visible columns
     *
     * @return set of column IDs that are visible by default
     */
    public static Set<String> getDefaultVisibleColumns() {
        Set<String> defaults = new LinkedHashSet<>();
        defaults.add("displayName");
        defaults.add("distanceToEarth");
        defaults.add("spectra");
        defaults.add("radius");
        defaults.add("mass");
        defaults.add("luminosity");
        defaults.add("ra");
        defaults.add("declination");
        defaults.add("parallax");
        defaults.add("xCoord");
        defaults.add("yCoord");
        defaults.add("zCoord");
        defaults.add("real");
        defaults.add("comment");
        return defaults;
    }

    /**
     * Get all available column IDs
     *
     * @return set of all column IDs
     */
    public static Set<String> getAllColumnIds() {
        Set<String> all = new LinkedHashSet<>(COLUMN_TO_FIELD.keySet());
        all.add("comment"); // comment is not sortable but is a column
        return all;
    }

    /**
     * Get the entity field name for a given column ID
     *
     * @param columnId the column ID
     * @return the entity field name, or null if not sortable
     */
    public static String getEntityField(String columnId) {
        return COLUMN_TO_FIELD.get(columnId);
    }

    /**
     * Get the display name for a column
     *
     * @param columnId the column ID
     * @return the display name
     */
    public static String getDisplayName(String columnId) {
        return COLUMN_DISPLAY_NAMES.getOrDefault(columnId, columnId);
    }

    /**
     * Check if a column is sortable
     *
     * @param columnId the column ID
     * @return true if the column can be sorted server-side
     */
    public static boolean isSortable(String columnId) {
        return COLUMN_TO_FIELD.containsKey(columnId);
    }

    /**
     * Convert a UI column sort to Spring Data Sort
     *
     * @param columnId  the column ID
     * @param ascending true for ascending, false for descending
     * @return Spring Data Sort object
     */
    public static Sort toSpringSort(String columnId, boolean ascending) {
        String entityField = COLUMN_TO_FIELD.get(columnId);
        if (entityField == null) {
            // Default to displayName if column not found
            entityField = "displayName";
        }
        return ascending ? Sort.by(entityField).ascending() : Sort.by(entityField).descending();
    }

    /**
     * Get currently visible columns
     *
     * @return set of visible column IDs
     */
    public Set<String> getVisibleColumns() {
        return new LinkedHashSet<>(visibleColumns);
    }

    /**
     * Set column visibility
     *
     * @param columnId the column ID
     * @param visible  true to show, false to hide
     */
    public void setColumnVisible(String columnId, boolean visible) {
        if (visible) {
            visibleColumns.add(columnId);
        } else {
            visibleColumns.remove(columnId);
        }
    }

    /**
     * Check if a column is visible
     *
     * @param columnId the column ID
     * @return true if visible
     */
    public boolean isColumnVisible(String columnId) {
        return visibleColumns.contains(columnId);
    }

    /**
     * Reset to default column visibility
     */
    public void resetToDefaults() {
        visibleColumns.clear();
        visibleColumns.addAll(getDefaultVisibleColumns());
    }

    /**
     * Create a config with default settings
     *
     * @return new StarTableColumnConfig with defaults
     */
    public static StarTableColumnConfig defaults() {
        return new StarTableColumnConfig();
    }
}
