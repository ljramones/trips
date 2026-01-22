package com.teamgannon.trips.starplotting;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Sphere;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

/**
 * Manages Level-of-Detail (LOD) for star rendering.
 * <p>
 * This class determines the appropriate detail level for rendering stars
 * based on distance from center, star magnitude, and zoom level. Using
 * simpler geometry for distant or dim stars improves rendering performance
 * with large star counts.
 * <p>
 * LOD Levels:
 * <ul>
 *   <li>HIGH - Complex mesh (special stars like central star, or very bright nearby stars)</li>
 *   <li>MEDIUM - Standard sphere with full material (default for most visible stars)</li>
 *   <li>LOW - Simple small sphere (for distant or dim stars)</li>
 *   <li>MINIMAL - Very small sphere, reduced divisions (for very distant stars)</li>
 * </ul>
 */
@Slf4j
public class StarLODManager {

    /**
     * Level-of-detail enumeration for star rendering.
     */
    public enum LODLevel {
        /** Complex mesh with full detail - for central/special stars */
        HIGH,
        /** Standard sphere with full material - default for most stars */
        MEDIUM,
        /** Simple sphere with reduced divisions - for distant stars */
        LOW,
        /** Minimal sphere - for very distant or dim stars */
        MINIMAL
    }

    // =========================================================================
    // Configuration Constants
    // =========================================================================

    /**
     * Distance threshold for HIGH to MEDIUM transition (in screen units).
     * Stars closer than this to the center may use HIGH detail.
     */
    private static final double HIGH_DETAIL_DISTANCE = 50.0;

    /**
     * Distance threshold for MEDIUM to LOW transition (in screen units).
     */
    private static final double MEDIUM_DETAIL_DISTANCE = 200.0;

    /**
     * Distance threshold for LOW to MINIMAL transition (in screen units).
     */
    private static final double LOW_DETAIL_DISTANCE = 400.0;

    /**
     * Magnitude threshold for HIGH detail (brighter stars).
     * Stars brighter than this may use higher detail.
     */
    private static final double HIGH_DETAIL_MAGNITUDE = 2.0;

    /**
     * Magnitude threshold for reduced detail (dimmer stars).
     * Stars dimmer than this use lower detail.
     */
    private static final double LOW_DETAIL_MAGNITUDE = 6.0;

    /**
     * Sphere divisions for MEDIUM detail level.
     */
    private static final int MEDIUM_SPHERE_DIVISIONS = 32;

    /**
     * Sphere divisions for LOW detail level.
     */
    private static final int LOW_SPHERE_DIVISIONS = 16;

    /**
     * Sphere divisions for MINIMAL detail level.
     */
    private static final int MINIMAL_SPHERE_DIVISIONS = 8;

    /**
     * Minimum radius for MINIMAL detail stars.
     */
    private static final double MINIMAL_STAR_RADIUS = 0.5;

    /**
     * Radius multiplier for LOW detail stars.
     */
    private static final double LOW_DETAIL_RADIUS_MULTIPLIER = 0.8;

    // =========================================================================
    // Configuration State
    // =========================================================================

    /**
     * Whether LOD is enabled. When disabled, all stars use MEDIUM detail.
     */
    @Getter
    @Setter
    private boolean lodEnabled = true;

    /**
     * Current zoom level (affects LOD thresholds).
     */
    @Getter
    @Setter
    private double zoomLevel = 1.0;

    /**
     * Center coordinates for distance calculations.
     */
    private double[] centerCoordinates = {0, 0, 0};

    // =========================================================================
    // Statistics (for debugging/tuning)
    // =========================================================================

    @Getter
    private int highDetailCount = 0;
    @Getter
    private int mediumDetailCount = 0;
    @Getter
    private int lowDetailCount = 0;
    @Getter
    private int minimalDetailCount = 0;

    // =========================================================================
    // Configuration Methods
    // =========================================================================

    /**
     * Set the center coordinates for distance calculations.
     *
     * @param x center X coordinate
     * @param y center Y coordinate
     * @param z center Z coordinate
     */
    public void setCenterCoordinates(double x, double y, double z) {
        this.centerCoordinates = new double[]{x, y, z};
    }

    /**
     * Reset LOD statistics counters.
     */
    public void resetStatistics() {
        highDetailCount = 0;
        mediumDetailCount = 0;
        lowDetailCount = 0;
        minimalDetailCount = 0;
    }

    /**
     * Log current LOD statistics with triangle counts and efficiency metrics.
     */
    public void logStatistics() {
        int total = highDetailCount + mediumDetailCount + lowDetailCount + minimalDetailCount;
        if (total == 0) {
            log.info("LOD Statistics: No stars rendered");
            return;
        }

        // Triangle counts per LOD level (triangles ≈ 2 * divisions²)
        // HIGH uses default (64), MEDIUM=32, LOW=16, MINIMAL=8
        long highTriangles = highDetailCount * 8192L;      // 2 * 64 * 64
        long mediumTriangles = mediumDetailCount * 2048L;  // 2 * 32 * 32
        long lowTriangles = lowDetailCount * 512L;         // 2 * 16 * 16
        long minimalTriangles = minimalDetailCount * 128L; // 2 * 8 * 8
        long actualTriangles = highTriangles + mediumTriangles + lowTriangles + minimalTriangles;
        long maxTriangles = total * 8192L; // If all were HIGH

        double efficiency = 100.0 * (1.0 - (double) actualTriangles / maxTriangles);

        log.info("╔══════════════════════════════════════════════════════════════╗");
        log.info("║                    LOD RENDERING STATISTICS                   ║");
        log.info("╠══════════════════════════════════════════════════════════════╣");
        log.info("║ Level    │ Count │ Percent │ Triangles                       ║");
        log.info("║ HIGH     │ {:>5} │ {:>6.1f}% │ {:>12,}                       ║",
                highDetailCount, (highDetailCount * 100.0) / total, highTriangles);
        log.info("║ MEDIUM   │ {:>5} │ {:>6.1f}% │ {:>12,}                       ║",
                mediumDetailCount, (mediumDetailCount * 100.0) / total, mediumTriangles);
        log.info("║ LOW      │ {:>5} │ {:>6.1f}% │ {:>12,}                       ║",
                lowDetailCount, (lowDetailCount * 100.0) / total, lowTriangles);
        log.info("║ MINIMAL  │ {:>5} │ {:>6.1f}% │ {:>12,}                       ║",
                minimalDetailCount, (minimalDetailCount * 100.0) / total, minimalTriangles);
        log.info("╠══════════════════════════════════════════════════════════════╣");
        log.info("║ TOTAL STARS: {:,}                                            ║", total);
        log.info("║ ACTUAL TRIANGULAR TRIANGLES: {:,}                              ║", actualTriangles);
        log.info("║ WITHOUT LOD WOULD BE: {:,}                                   ║", maxTriangles);
        log.info("║ EFFICIENCY GAIN: {:.1f}% triangle reduction                   ║", efficiency);
        log.info("╚══════════════════════════════════════════════════════════════╝");
    }

    // =========================================================================
    // LOD Determination
    // =========================================================================

    /**
     * Determine the appropriate LOD level for a star.
     * <p>
     * Note: LOD uses screen coordinates because thresholds are calibrated for
     * visual rendering. The cached distance in StarDisplayRecord is in light-years
     * (for KD-tree consistency) and is NOT used here.
     *
     * @param record the star display record
     * @param isCenter whether this is the center star
     * @return the appropriate LOD level
     */
    public LODLevel determineLODLevel(@NotNull StarDisplayRecord record, boolean isCenter) {
        if (!lodEnabled) {
            return LODLevel.MEDIUM;
        }

        // Center star always gets HIGH detail
        if (isCenter) {
            return LODLevel.HIGH;
        }

        // Calculate distance in screen coordinates (LOD thresholds are screen-calibrated)
        Point3D coords = record.getCoordinates();
        double distance = calculateDistance(coords.getX(), coords.getY(), coords.getZ());

        // Adjust distance thresholds based on zoom level
        double adjustedHighThreshold = HIGH_DETAIL_DISTANCE / zoomLevel;
        double adjustedMediumThreshold = MEDIUM_DETAIL_DISTANCE / zoomLevel;
        double adjustedLowThreshold = LOW_DETAIL_DISTANCE / zoomLevel;

        // Get magnitude (use absolute magnitude if available, otherwise estimate)
        double magnitude = estimateMagnitude(record);

        // Determine LOD based on distance and magnitude
        if (distance < adjustedHighThreshold && magnitude < HIGH_DETAIL_MAGNITUDE) {
            return LODLevel.HIGH;
        } else if (distance < adjustedMediumThreshold || magnitude < LOW_DETAIL_MAGNITUDE) {
            return LODLevel.MEDIUM;
        } else if (distance < adjustedLowThreshold) {
            return LODLevel.LOW;
        } else {
            return LODLevel.MINIMAL;
        }
    }

    /**
     * Calculate distance from center coordinates.
     */
    private double calculateDistance(double x, double y, double z) {
        double dx = x - centerCoordinates[0];
        double dy = y - centerCoordinates[1];
        double dz = z - centerCoordinates[2];
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * Estimate magnitude from star record.
     * Uses actual magnitude if available, otherwise estimates from luminosity or radius.
     */
    private double estimateMagnitude(@NotNull StarDisplayRecord record) {
        // If we have a display score, use it as a proxy for importance
        double displayScore = record.getDisplayScore();
        if (displayScore > 0) {
            // Higher display score = more important = "brighter"
            // Convert to approximate magnitude scale (inverted)
            return 10.0 - displayScore;
        }

        // Fall back to using radius as brightness indicator
        // Larger radius = brighter display
        double radius = record.getRadius();
        if (radius > 3.0) {
            return 1.0; // Very bright
        } else if (radius > 2.0) {
            return 3.0; // Bright
        } else if (radius > 1.0) {
            return 5.0; // Medium
        } else {
            return 7.0; // Dim
        }
    }

    // =========================================================================
    // Star Creation with LOD
    // =========================================================================

    /**
     * Create a star node with the appropriate LOD level.
     *
     * @param record the star display record
     * @param baseRadius the base radius from star preferences
     * @param material the material to apply
     * @param lodLevel the LOD level to use
     * @return the created star node
     */
    public Node createStarWithLOD(@NotNull StarDisplayRecord record,
                                   double baseRadius,
                                   @NotNull PhongMaterial material,
                                   @NotNull LODLevel lodLevel) {
        // Update statistics
        switch (lodLevel) {
            case HIGH -> highDetailCount++;
            case MEDIUM -> mediumDetailCount++;
            case LOW -> lowDetailCount++;
            case MINIMAL -> minimalDetailCount++;
        }

        return switch (lodLevel) {
            case HIGH -> createHighDetailStar(baseRadius, material);
            case MEDIUM -> createMediumDetailStar(baseRadius, material);
            case LOW -> createLowDetailStar(baseRadius, material);
            case MINIMAL -> createMinimalDetailStar(baseRadius, material);
        };
    }

    /**
     * Create a high-detail star (standard sphere with full divisions).
     * Note: For truly high detail (mesh stars), use createCentralStar() in StarPlotManager.
     */
    private Node createHighDetailStar(double radius, PhongMaterial material) {
        Sphere sphere = new Sphere(radius);
        sphere.setMaterial(material);
        return sphere;
    }

    /**
     * Create a medium-detail star (sphere with moderate divisions).
     */
    private Node createMediumDetailStar(double radius, PhongMaterial material) {
        Sphere sphere = new Sphere(radius, MEDIUM_SPHERE_DIVISIONS);
        sphere.setMaterial(material);
        return sphere;
    }

    /**
     * Create a low-detail star (sphere with reduced divisions and size).
     */
    private Node createLowDetailStar(double radius, PhongMaterial material) {
        double adjustedRadius = radius * LOW_DETAIL_RADIUS_MULTIPLIER;
        Sphere sphere = new Sphere(adjustedRadius, LOW_SPHERE_DIVISIONS);
        sphere.setMaterial(material);
        return sphere;
    }

    /**
     * Create a minimal-detail star (small sphere with minimal divisions).
     */
    private Node createMinimalDetailStar(double radius, PhongMaterial material) {
        double adjustedRadius = Math.max(radius * LOW_DETAIL_RADIUS_MULTIPLIER, MINIMAL_STAR_RADIUS);
        Sphere sphere = new Sphere(adjustedRadius, MINIMAL_SPHERE_DIVISIONS);
        sphere.setMaterial(material);
        return sphere;
    }

    // =========================================================================
    // Utility Methods
    // =========================================================================

    /**
     * Get total star count from statistics.
     */
    public int getTotalStarCount() {
        return highDetailCount + mediumDetailCount + lowDetailCount + minimalDetailCount;
    }

    /**
     * Get percentage of stars at each LOD level.
     */
    public String getStatisticsPercentages() {
        int total = getTotalStarCount();
        if (total == 0) {
            return "No stars rendered";
        }

        return String.format("HIGH: %.1f%%, MEDIUM: %.1f%%, LOW: %.1f%%, MINIMAL: %.1f%%",
                (highDetailCount * 100.0) / total,
                (mediumDetailCount * 100.0) / total,
                (lowDetailCount * 100.0) / total,
                (minimalDetailCount * 100.0) / total);
    }
}
