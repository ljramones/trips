package com.teamgannon.trips.dialogs.nebula.catalog;

import com.teamgannon.trips.jpa.model.Nebula;
import com.teamgannon.trips.jpa.model.NebulaType;
import lombok.Builder;
import lombok.Data;

/**
 * Represents a nebula entry from an astronomical catalog.
 * <p>
 * Stores catalog metadata and provides conversion to Nebula entity
 * with proper coordinate transformation from equatorial (RA/Dec) to
 * Cartesian (X/Y/Z) coordinates centered on Sol.
 */
@Data
@Builder
public class NebulaCatalogEntry {

    /**
     * Primary catalog ID (e.g., "M42", "NGC 7000")
     */
    private String catalogId;

    /**
     * Common name (e.g., "Orion Nebula", "North America Nebula")
     */
    private String commonName;

    /**
     * Alternate catalog IDs (e.g., NGC number for Messier objects)
     */
    private String alternateCatalogId;

    /**
     * Right Ascension in degrees (0-360)
     */
    private double raDegrees;

    /**
     * Declination in degrees (-90 to +90)
     */
    private double decDegrees;

    /**
     * Distance from Sol in light-years
     */
    private double distanceLy;

    /**
     * Angular diameter in arcminutes
     */
    private double angularSizeArcmin;

    /**
     * Nebula type
     */
    private NebulaType type;

    /**
     * Source catalog name (e.g., "Messier", "NGC", "Caldwell")
     */
    private String sourceCatalog;

    /**
     * Brief description or notes
     */
    private String description;

    /**
     * Constellation the nebula is located in
     */
    private String constellation;

    /**
     * Convert Right Ascension from hours:minutes:seconds to degrees.
     *
     * @param hours   hours (0-23)
     * @param minutes minutes (0-59)
     * @param seconds seconds (0-59.99)
     * @return RA in degrees (0-360)
     */
    public static double raToDegreesFromHMS(int hours, int minutes, double seconds) {
        double totalHours = hours + minutes / 60.0 + seconds / 3600.0;
        return totalHours * 15.0; // 360 degrees / 24 hours = 15 degrees per hour
    }

    /**
     * Convert Declination from degrees:arcminutes:arcseconds to decimal degrees.
     *
     * @param degrees    degrees (-90 to +90)
     * @param arcminutes arcminutes (0-59)
     * @param arcseconds arcseconds (0-59.99)
     * @return Dec in decimal degrees
     */
    public static double decToDegreesFromDMS(int degrees, int arcminutes, double arcseconds) {
        double sign = degrees >= 0 ? 1.0 : -1.0;
        return sign * (Math.abs(degrees) + arcminutes / 60.0 + arcseconds / 3600.0);
    }

    /**
     * Calculate linear diameter in light-years from angular size and distance.
     *
     * @param angularSizeArcmin angular diameter in arcminutes
     * @param distanceLy        distance in light-years
     * @return linear diameter in light-years
     */
    public static double angularToLinearSize(double angularSizeArcmin, double distanceLy) {
        // Convert arcminutes to radians
        double angularRadians = Math.toRadians(angularSizeArcmin / 60.0);
        // Linear size = 2 * distance * tan(angle/2)
        // For small angles: size ≈ distance * angle (in radians)
        return distanceLy * angularRadians;
    }

    /**
     * Get the X coordinate (light-years from Sol).
     * Uses standard equatorial to Cartesian conversion.
     *
     * @return X coordinate in light-years
     */
    public double getX() {
        double raRad = Math.toRadians(raDegrees);
        double decRad = Math.toRadians(decDegrees);
        return distanceLy * Math.cos(decRad) * Math.cos(raRad);
    }

    /**
     * Get the Y coordinate (light-years from Sol).
     *
     * @return Y coordinate in light-years
     */
    public double getY() {
        double raRad = Math.toRadians(raDegrees);
        double decRad = Math.toRadians(decDegrees);
        return distanceLy * Math.cos(decRad) * Math.sin(raRad);
    }

    /**
     * Get the Z coordinate (light-years from Sol).
     *
     * @return Z coordinate in light-years
     */
    public double getZ() {
        double decRad = Math.toRadians(decDegrees);
        return distanceLy * Math.sin(decRad);
    }

    /**
     * Get the linear radius in light-years.
     *
     * @return outer radius in light-years
     */
    public double getRadiusLy() {
        return angularToLinearSize(angularSizeArcmin, distanceLy) / 2.0;
    }

    /**
     * Get a display name combining catalog ID and common name.
     *
     * @return display name
     */
    public String getDisplayName() {
        if (commonName != null && !commonName.isEmpty()) {
            return catalogId + " - " + commonName;
        }
        return catalogId;
    }

    /**
     * Convert this catalog entry to a Nebula entity for persistence.
     *
     * @param datasetName the dataset to add the nebula to
     * @return Nebula entity ready for persistence
     */
    public Nebula toNebula(String datasetName) {
        double radiusLy = getRadiusLy();
        // Ensure minimum radius for very distant/small nebulae
        if (radiusLy < 0.5) {
            radiusLy = 0.5;
        }

        String name = commonName != null && !commonName.isEmpty() ? commonName : catalogId;

        Nebula nebula = new Nebula(
                name,
                type,
                datasetName,
                getX(),
                getY(),
                getZ(),
                radiusLy
        );

        // Apply type defaults for procedural parameters
        nebula.applyTypeDefaults();

        // Set catalog metadata
        nebula.setCatalogId(catalogId);
        nebula.setSourceCatalog(sourceCatalog);

        // Build description/notes
        StringBuilder notes = new StringBuilder();
        if (alternateCatalogId != null && !alternateCatalogId.isEmpty()) {
            notes.append("Also known as: ").append(alternateCatalogId).append("\n");
        }
        if (constellation != null && !constellation.isEmpty()) {
            notes.append("Constellation: ").append(constellation).append("\n");
        }
        if (description != null && !description.isEmpty()) {
            notes.append(description);
        }
        nebula.setNotes(notes.toString().trim());

        return nebula;
    }

}
