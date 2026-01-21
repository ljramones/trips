package com.teamgannon.trips.starplotting;

import com.teamgannon.trips.algorithms.Universe;
import com.teamgannon.trips.jpa.model.StarObject;
import javafx.geometry.Point3D;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Manages coordinate scaling for interstellar space visualization.
 * Converts between light-years (astronomical units) and screen coordinates.
 * <p>
 * This class consolidates all scaling logic that was previously spread across
 * AstrographicTransformer, ScalingParameters, and Universe classes.
 * <p>
 * Coordinate System:
 * - Light-years: Database stores star positions in light-years with Sol at (0,0,0)
 * - Screen units: JavaFX 3D coordinates where the view fits within Universe.boxHeight
 * <p>
 * The transformation is: screenCoord = (lyCoord - centerCoord) * scalingFactor * zoomLevel
 */
@Slf4j
public class InterstellarScaleManager {

    // ==================== Screen Dimension Constants ====================

    /**
     * Screen box width in pixels (from Universe class)
     */
    public static final double SCREEN_WIDTH = Universe.boxWidth;

    /**
     * Screen box height in pixels - used as the primary scaling reference
     */
    public static final double SCREEN_HEIGHT = Universe.boxHeight;

    /**
     * Screen box depth in pixels
     */
    public static final double SCREEN_DEPTH = Universe.boxDepth;

    // ==================== Star Display Constants ====================

    /**
     * Minimum star display radius in screen units
     */
    private static final double MIN_STAR_RADIUS = 1.0;

    /**
     * Maximum star display radius in screen units
     */
    private static final double MAX_STAR_RADIUS = 7.0;

    /**
     * Default star radius when no magnitude/luminosity data available
     */
    private static final double DEFAULT_STAR_RADIUS = 3.0;

    /**
     * Base multiplier for star size (legacy GRAPHICS_FUDGE_FACTOR)
     */
    @Getter
    @Setter
    private double starSizeMultiplier = 1.5;

    // ==================== Scaling State ====================

    /**
     * Center coordinates in light-years (the origin point for the view)
     */
    @Getter
    private double[] centerCoordinatesLY = {0, 0, 0};

    /**
     * Base scaling factor: screen units per light-year at zoom level 1.0
     * Calculated as: SCREEN_HEIGHT / maxCoordinateRange
     */
    @Getter
    private double baseScalingFactor = 1.0;

    /**
     * Current zoom level (1.0 = default view)
     */
    @Getter
    @Setter
    private double zoomLevel = 1.0;

    /**
     * Minimum zoom level (zoomed out)
     */
    @Getter
    @Setter
    private double minZoom = 0.1;

    /**
     * Maximum zoom level (zoomed in)
     */
    @Getter
    @Setter
    private double maxZoom = 10.0;

    // ==================== Data Range State ====================

    @Getter
    private double minX, maxX;
    @Getter
    private double minY, maxY;
    @Getter
    private double minZ, maxZ;

    /**
     * Grid line spacing in light-years
     */
    @Getter
    @Setter
    private double gridSpacingLY = 5.0;

    // ==================== Constructors ====================

    /**
     * Create a scale manager with default settings.
     */
    public InterstellarScaleManager() {
    }

    /**
     * Create a scale manager with a specific grid spacing.
     *
     * @param gridSpacingLY grid line spacing in light-years
     */
    public InterstellarScaleManager(double gridSpacingLY) {
        this.gridSpacingLY = gridSpacingLY;
    }

    // ==================== Configuration Methods ====================

    /**
     * Set the center point for the view in light-years.
     * All coordinates will be transformed relative to this point.
     *
     * @param x center X in light-years
     * @param y center Y in light-years
     * @param z center Z in light-years
     */
    public void setCenterCoordinates(double x, double y, double z) {
        this.centerCoordinatesLY = new double[]{x, y, z};
    }

    /**
     * Set the center point from a star's coordinates.
     *
     * @param centerStar the star to center on
     */
    public void setCenterCoordinates(@NotNull StarObject centerStar) {
        double[] coords = centerStar.getCoordinates();
        setCenterCoordinates(coords[0], coords[1], coords[2]);
    }

    /**
     * Reset center to Sol (origin).
     */
    public void resetCenterToOrigin() {
        setCenterCoordinates(0, 0, 0);
    }

    /**
     * Calculate scaling parameters from a list of stars.
     * This determines the min/max ranges and calculates the appropriate
     * scaling factor to fit all stars within the screen bounds.
     *
     * @param stars             list of stars to analyze
     * @param centerCoordinates the center point in light-years
     */
    public void calculateScalingFromStars(@NotNull List<StarObject> stars, double[] centerCoordinates) {
        if (stars.isEmpty()) {
            log.warn("Cannot calculate scaling from empty star list");
            return;
        }

        this.centerCoordinatesLY = centerCoordinates;

        // Reset ranges
        minX = Double.MAX_VALUE;
        maxX = -Double.MAX_VALUE;
        minY = Double.MAX_VALUE;
        maxY = -Double.MAX_VALUE;
        minZ = Double.MAX_VALUE;
        maxZ = -Double.MAX_VALUE;

        // Find min/max for each axis
        for (StarObject star : stars) {
            double[] coords = star.getCoordinates();

            if (coords[0] < minX) minX = coords[0];
            if (coords[0] > maxX) maxX = coords[0];

            if (coords[1] < minY) minY = coords[1];
            if (coords[1] > maxY) maxY = coords[1];

            if (coords[2] < minZ) minZ = coords[2];
            if (coords[2] > maxZ) maxZ = coords[2];
        }

        // Calculate base scaling factor based on largest range
        double xRange = maxX - minX;
        double yRange = maxY - minY;
        double zRange = maxZ - minZ;
        double maxRange = Math.max(Math.max(xRange, yRange), zRange);

        if (maxRange > 0) {
            baseScalingFactor = SCREEN_HEIGHT / maxRange;
        } else {
            baseScalingFactor = 1.0;
            log.warn("Max coordinate range is zero, using default scaling factor");
        }

        log.info("Calculated scaling: center=[{},{},{}], ranges=[{},{},{}], factor={}",
                String.format("%.2f", centerCoordinates[0]),
                String.format("%.2f", centerCoordinates[1]),
                String.format("%.2f", centerCoordinates[2]),
                String.format("%.2f", xRange),
                String.format("%.2f", yRange),
                String.format("%.2f", zRange),
                String.format("%.4f", baseScalingFactor));
    }

    // ==================== Coordinate Transformation Methods ====================

    /**
     * Get the effective scaling factor (base * zoom).
     *
     * @return current effective scaling factor
     */
    public double getEffectiveScalingFactor() {
        return baseScalingFactor * zoomLevel;
    }

    /**
     * Convert light-year coordinates to screen coordinates.
     *
     * @param lyX X coordinate in light-years
     * @param lyY Y coordinate in light-years
     * @param lyZ Z coordinate in light-years
     * @return array of screen coordinates [x, y, z]
     */
    public double[] lyToScreen(double lyX, double lyY, double lyZ) {
        double factor = getEffectiveScalingFactor();
        return new double[]{
                (lyX - centerCoordinatesLY[0]) * factor,
                (lyY - centerCoordinatesLY[1]) * factor,
                (lyZ - centerCoordinatesLY[2]) * factor
        };
    }

    /**
     * Convert light-year coordinates to a JavaFX Point3D in screen space.
     *
     * @param lyX X coordinate in light-years
     * @param lyY Y coordinate in light-years
     * @param lyZ Z coordinate in light-years
     * @return Point3D in screen coordinates
     */
    public Point3D lyToScreenPoint(double lyX, double lyY, double lyZ) {
        double[] screen = lyToScreen(lyX, lyY, lyZ);
        return new Point3D(screen[0], screen[1], screen[2]);
    }

    /**
     * Convert light-year coordinate array to screen coordinates.
     *
     * @param lyCoords array of [x, y, z] in light-years
     * @return array of screen coordinates [x, y, z]
     */
    public double[] lyToScreen(double[] lyCoords) {
        if (lyCoords == null || lyCoords.length < 3) {
            return new double[]{0, 0, 0};
        }
        return lyToScreen(lyCoords[0], lyCoords[1], lyCoords[2]);
    }

    /**
     * Convert screen coordinates back to light-years.
     *
     * @param screenX X coordinate in screen units
     * @param screenY Y coordinate in screen units
     * @param screenZ Z coordinate in screen units
     * @return array of light-year coordinates [x, y, z]
     */
    public double[] screenToLY(double screenX, double screenY, double screenZ) {
        double factor = getEffectiveScalingFactor();
        if (factor == 0) {
            return new double[]{0, 0, 0};
        }
        return new double[]{
                (screenX / factor) + centerCoordinatesLY[0],
                (screenY / factor) + centerCoordinatesLY[1],
                (screenZ / factor) + centerCoordinatesLY[2]
        };
    }

    /**
     * Convert a distance in light-years to screen units.
     *
     * @param distanceLY distance in light-years
     * @return distance in screen units
     */
    public double lyDistanceToScreen(double distanceLY) {
        return distanceLY * getEffectiveScalingFactor();
    }

    /**
     * Convert a distance in screen units to light-years.
     *
     * @param screenDistance distance in screen units
     * @return distance in light-years
     */
    public double screenDistanceToLY(double screenDistance) {
        double factor = getEffectiveScalingFactor();
        if (factor == 0) {
            return 0;
        }
        return screenDistance / factor;
    }

    // ==================== Star Size Calculation ====================

    /**
     * Calculate the display radius for a star based on its absolute magnitude.
     * Brighter stars (lower magnitude) appear larger.
     * <p>
     * The formula maps magnitude to a visual size:
     * - Magnitude -1 (very bright) → larger radius
     * - Magnitude +10 (dim) → smaller radius
     *
     * @param absoluteMagnitude the star's absolute magnitude (lower = brighter)
     * @return display radius in screen units
     */
    public double calculateStarRadius(double absoluteMagnitude) {
        // Handle invalid/missing magnitude
        if (Double.isNaN(absoluteMagnitude) || absoluteMagnitude == 0) {
            return DEFAULT_STAR_RADIUS * starSizeMultiplier;
        }

        // Map magnitude to radius
        // Typical range: -1 (bright) to +15 (dim)
        // We want: bright → large, dim → small
        // Using a simple linear mapping with clamping

        // Normalize: magnitude -1 → 1.0, magnitude +15 → 0.0
        double normalized = 1.0 - ((absoluteMagnitude + 1) / 16.0);
        normalized = Math.max(0, Math.min(1, normalized));

        // Map to radius range
        double radius = MIN_STAR_RADIUS + normalized * (MAX_STAR_RADIUS - MIN_STAR_RADIUS);

        return radius * starSizeMultiplier;
    }

    /**
     * Calculate the display radius for a star based on its luminosity.
     * More luminous stars appear larger.
     *
     * @param luminositySolar luminosity in solar luminosities
     * @return display radius in screen units
     */
    public double calculateStarRadiusFromLuminosity(double luminositySolar) {
        if (luminositySolar <= 0 || Double.isNaN(luminositySolar)) {
            return DEFAULT_STAR_RADIUS * starSizeMultiplier;
        }

        // Use log scale for luminosity (can vary by many orders of magnitude)
        // Sun (1 solar) → medium size
        // log10(1) = 0 → normalized 0.5
        // log10(0.01) = -2 → normalized ~0.25
        // log10(100) = 2 → normalized ~0.75

        double logLum = Math.log10(luminositySolar);
        // Typical range: -2 to +4
        double normalized = (logLum + 2) / 6.0;
        normalized = Math.max(0, Math.min(1, normalized));

        double radius = MIN_STAR_RADIUS + normalized * (MAX_STAR_RADIUS - MIN_STAR_RADIUS);

        return radius * starSizeMultiplier;
    }

    /**
     * Calculate the display radius for a star using available data.
     * Prefers luminosity if available, falls back to magnitude, then default.
     *
     * @param luminositySolar   luminosity in solar luminosities (0 if unknown)
     * @param absoluteMagnitude absolute magnitude (NaN if unknown)
     * @return display radius in screen units
     */
    public double calculateStarRadius(double luminositySolar, double absoluteMagnitude) {
        if (luminositySolar > 0 && !Double.isNaN(luminositySolar)) {
            return calculateStarRadiusFromLuminosity(luminositySolar);
        } else if (!Double.isNaN(absoluteMagnitude) && absoluteMagnitude != 0) {
            return calculateStarRadius(absoluteMagnitude);
        } else {
            return DEFAULT_STAR_RADIUS * starSizeMultiplier;
        }
    }

    // ==================== Grid and Scale Information ====================

    /**
     * Get the screen distance for grid lines spaced at gridSpacingLY light-years.
     *
     * @return grid spacing in screen units
     */
    public double getGridSpacingScreen() {
        return lyDistanceToScreen(gridSpacingLY);
    }

    /**
     * Get appropriate light-year values for drawing scale reference lines.
     * Returns distances that create visually useful grid lines.
     *
     * @return array of light-year distances for scale lines
     */
    public double[] getScaleGridLYValues() {
        double maxRange = Math.max(Math.max(getXRange(), getYRange()), getZRange());

        if (maxRange <= 10) {
            return new double[]{1, 2, 5, 10};
        } else if (maxRange <= 25) {
            return new double[]{5, 10, 15, 20, 25};
        } else if (maxRange <= 50) {
            return new double[]{10, 20, 30, 40, 50};
        } else if (maxRange <= 100) {
            return new double[]{20, 40, 60, 80, 100};
        } else {
            return new double[]{50, 100, 150, 200};
        }
    }

    /**
     * Get the X coordinate range in light-years.
     *
     * @return X range (maxX - minX)
     */
    public double getXRange() {
        return maxX - minX;
    }

    /**
     * Get the Y coordinate range in light-years.
     *
     * @return Y range (maxY - minY)
     */
    public double getYRange() {
        return maxY - minY;
    }

    /**
     * Get the Z coordinate range in light-years.
     *
     * @return Z range (maxZ - minZ)
     */
    public double getZRange() {
        return maxZ - minZ;
    }

    // ==================== Zoom Methods ====================

    /**
     * Apply a zoom factor to the current zoom level.
     *
     * @param factor zoom factor (greater than 1 zooms in, less than 1 zooms out)
     */
    public void zoom(double factor) {
        double newZoom = zoomLevel * factor;
        zoomLevel = Math.max(minZoom, Math.min(maxZoom, newZoom));
    }

    /**
     * Reset zoom to default level (1.0).
     */
    public void resetZoom() {
        zoomLevel = 1.0;
    }

    // ==================== Utility Methods ====================

    /**
     * Check if a screen coordinate is within the visible bounds.
     *
     * @param screenX X coordinate in screen units
     * @param screenY Y coordinate in screen units
     * @param screenZ Z coordinate in screen units
     * @return true if the point is within the screen bounds
     */
    public boolean isWithinScreenBounds(double screenX, double screenY, double screenZ) {
        return Math.abs(screenX) <= SCREEN_WIDTH / 2 &&
                Math.abs(screenY) <= SCREEN_HEIGHT / 2 &&
                Math.abs(screenZ) <= SCREEN_DEPTH / 2;
    }

    /**
     * Calculate the distance between two points in light-years.
     *
     * @param ly1 first point [x, y, z] in light-years
     * @param ly2 second point [x, y, z] in light-years
     * @return distance in light-years
     */
    public static double distanceLY(double[] ly1, double[] ly2) {
        double dx = ly2[0] - ly1[0];
        double dy = ly2[1] - ly1[1];
        double dz = ly2[2] - ly1[2];
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * Get a human-readable description of the current scale.
     *
     * @return description string
     */
    public String getScaleDescription() {
        double lyPerScreenUnit = 1.0 / getEffectiveScalingFactor();
        return String.format("1 screen unit = %.3f light-years (zoom: %.1fx)",
                lyPerScreenUnit, zoomLevel);
    }

    @Override
    public String toString() {
        return String.format("InterstellarScaleManager[center=(%.2f,%.2f,%.2f), " +
                        "factor=%.4f, zoom=%.2f, ranges=(%.2f,%.2f,%.2f)]",
                centerCoordinatesLY[0], centerCoordinatesLY[1], centerCoordinatesLY[2],
                baseScalingFactor, zoomLevel,
                getXRange(), getYRange(), getZRange());
    }
}
