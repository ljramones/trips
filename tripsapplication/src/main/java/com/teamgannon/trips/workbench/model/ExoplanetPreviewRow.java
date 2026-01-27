package com.teamgannon.trips.workbench.model;

import lombok.Data;

/**
 * Display model for showing loaded exoplanets in preview table.
 */
@Data
public class ExoplanetPreviewRow {

    private String name;
    private String planetStatus;
    private String starName;
    private String semiMajorAxis;
    private String mass;
    private String radius;
    private String orbitalPeriod;
    private String eccentricity;
    private String inclination;
    private String ra;
    private String dec;
    private String starDistance;
    private String spectralType;

    /**
     * Format a double value for display, handling nulls.
     */
    private static String formatDouble(Double value, String format) {
        if (value == null || value.isNaN()) {
            return "";
        }
        return format.formatted(value);
    }

    /**
     * Format a double value for display with default format.
     */
    private static String formatDouble(Double value) {
        return formatDouble(value, "%.4f");
    }
}
