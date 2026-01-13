package com.teamgannon.trips.screenobjects;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import lombok.Data;

/**
 * Record representing a star system with planets for display in the Planetary Systems pane.
 */
@Data
public class PlanetarySystemRecord {

    /**
     * The star display record
     */
    private final StarDisplayRecord starDisplayRecord;

    /**
     * Number of planets (not moons) in this system
     */
    private final long planetCount;

    /**
     * Get the star name
     */
    public String getStarName() {
        return starDisplayRecord != null ? starDisplayRecord.getStarName() : "Unknown";
    }

    /**
     * Get the star's record ID
     */
    public String getRecordId() {
        return starDisplayRecord != null ? starDisplayRecord.getRecordId() : null;
    }
}
