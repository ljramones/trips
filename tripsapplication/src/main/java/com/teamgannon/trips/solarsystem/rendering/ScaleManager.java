package com.teamgannon.trips.solarsystem.rendering;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Manages coordinate scaling for solar system visualization.
 * Converts between astronomical units (AU) and screen coordinates.
 */
@Slf4j
@Getter
@Setter
public class ScaleManager {

    /**
     * Astronomical unit in meters
     */
    public static final double AU_IN_METERS = 149_597_870_700.0;

    /**
     * Screen units per AU at zoom level 1.0
     */
    private double baseScale = 50.0;

    /**
     * Current zoom level (1.0 = default)
     */
    private double zoomLevel = 1.0;

    /**
     * Minimum zoom level
     */
    private double minZoom = 0.1;

    /**
     * Maximum zoom level
     */
    private double maxZoom = 100.0;

    /**
     * Whether to use logarithmic scaling for distances
     */
    private boolean useLogScale = false;

    /**
     * Whether to use true relative planet sizes (vs clamped for visibility)
     */
    private boolean useRelativeScale = false;

    /**
     * Maximum orbital distance in AU (used for scaling)
     */
    private double maxOrbitalDistanceAU = 30.0;

    /**
     * Minimum planet display radius in screen units
     */
    private double minPlanetRadius = 2.0;

    /**
     * Maximum planet display radius in screen units
     */
    private double maxPlanetRadius = 10.0;

    /**
     * Star display radius in screen units
     */
    private double starRadius = 20.0;

    public ScaleManager() {
    }

    public ScaleManager(double maxOrbitalDistanceAU) {
        this.maxOrbitalDistanceAU = maxOrbitalDistanceAU;
        // Adjust base scale so the max orbit fits reasonably on screen
        // Aim for max orbit to be about 400 screen units from center
        if (maxOrbitalDistanceAU > 0) {
            this.baseScale = 400.0 / maxOrbitalDistanceAU;
        }
    }

    /**
     * Convert AU to screen coordinates
     *
     * @param au distance in astronomical units
     * @return distance in screen units
     */
    public double auToScreen(double au) {
        if (useLogScale && au > 0) {
            // Log scale: compress large distances
            // Use log10(1 + au) to handle au < 1 nicely
            return Math.log10(1 + au * 10) * baseScale * zoomLevel * 30;
        }
        return au * baseScale * zoomLevel;
    }

    /**
     * Convert an AU vector to screen coordinates.
     *
     * @param xAu x in AU
     * @param yAu y in AU
     * @param zAu z in AU
     * @return screen-space vector {x, y, z}
     */
    public double[] auVectorToScreen(double xAu, double yAu, double zAu) {
        double r = Math.sqrt(xAu * xAu + yAu * yAu + zAu * zAu);
        if (r == 0) {
            return new double[]{0, 0, 0};
        }
        double scaledR = auToScreen(r);
        double factor = scaledR / r;
        return new double[]{xAu * factor, yAu * factor, zAu * factor};
    }

    /**
     * Convert screen coordinates to AU
     *
     * @param screen distance in screen units
     * @return distance in astronomical units
     */
    public double screenToAu(double screen) {
        if (useLogScale) {
            // Inverse of log scale
            double logValue = screen / (baseScale * zoomLevel * 30);
            return (Math.pow(10, logValue) - 1) / 10;
        }
        return screen / (baseScale * zoomLevel);
    }

    /**
     * Convert AU to meters (for Orekit)
     *
     * @param au distance in astronomical units
     * @return distance in meters
     */
    public static double auToMeters(double au) {
        return au * AU_IN_METERS;
    }

    /**
     * Convert meters to AU
     *
     * @param meters distance in meters
     * @return distance in astronomical units
     */
    public static double metersToAu(double meters) {
        return meters / AU_IN_METERS;
    }

    /**
     * Calculate planet display radius based on actual radius.
     * Scales planet sizes relative to each other while keeping them visible.
     *
     * @param planetRadiusEarthRadii planet radius in Earth radii
     * @param maxRadiusInSystem      largest planet radius in the system (Earth radii)
     * @return display radius in screen units
     */
    public double calculatePlanetDisplayRadius(double planetRadiusEarthRadii, double maxRadiusInSystem) {
        if (maxRadiusInSystem <= 0 || planetRadiusEarthRadii <= 0) {
            return minPlanetRadius;
        }

        // Normalize to 0-1 range
        double normalized = planetRadiusEarthRadii / maxRadiusInSystem;

        if (useRelativeScale) {
            // True relative sizing - linear scaling preserving actual ratios
            // Jupiter really is ~11x Earth's radius, so show that difference
            // But cap at 40% of star size so planets don't look as big as stars
            double maxAllowed = starRadius * 0.4;
            return minPlanetRadius + normalized * (maxAllowed - minPlanetRadius);
        }

        // Clamped mode (default) - use square root to compress the range
        // Jupiter won't be 11x Earth visually, making small planets more visible
        double compressed = Math.sqrt(normalized);

        return minPlanetRadius + compressed * (maxPlanetRadius - minPlanetRadius);
    }

    /**
     * Calculate moon display radius based on parent planet's display size.
     * Uses actual physical radius ratios to ensure accurate relative sizing.
     *
     * Real moon/planet ratios (by diameter):
     * - Ganymede/Jupiter: 5268/142984 = 0.0368 (1/27)
     * - Callisto/Jupiter: 4821/142984 = 0.0337 (1/30)
     * - Io/Jupiter:       3643/142984 = 0.0255 (1/39)
     * - Europa/Jupiter:   3122/142984 = 0.0218 (1/46)
     *
     * @param moonRadiusEarthRadii   moon's physical radius in Earth radii
     * @param parentRadiusEarthRadii parent planet's physical radius in Earth radii
     * @param parentDisplayRadius    parent planet's display radius in screen units
     * @return display radius for the moon in screen units
     */
    public double calculateMoonDisplayRadius(double moonRadiusEarthRadii,
                                              double parentRadiusEarthRadii,
                                              double parentDisplayRadius) {
        if (moonRadiusEarthRadii <= 0 || parentRadiusEarthRadii <= 0 || parentDisplayRadius <= 0) {
            // Fallback: use a tiny fraction of parent size
            return parentDisplayRadius > 0 ? parentDisplayRadius * 0.03 : 0.2;
        }

        // Calculate the actual physical ratio - this is the scientifically accurate ratio
        double physicalRatio = moonRadiusEarthRadii / parentRadiusEarthRadii;

        // Apply the same ratio to display size - moons will be tiny relative to parent
        double moonDisplayRadius = parentDisplayRadius * physicalRatio;

        // Very small minimum just to keep moons from becoming invisible dots
        // At 0.15 screen units, moons will still render as visible pixels
        // but won't be artificially inflated beyond their true relative size
        double absoluteMinimum = 0.15;

        return Math.max(absoluteMinimum, moonDisplayRadius);
    }

    /**
     * Calculate star display radius based on stellar radius.
     *
     * @param stellarRadiusSolarRadii star radius in solar radii
     * @return display radius in screen units
     */
    public double calculateStarDisplayRadius(double stellarRadiusSolarRadii) {
        if (stellarRadiusSolarRadii <= 0) {
            return starRadius;
        }
        // Compress using log scale for giant stars
        if (stellarRadiusSolarRadii > 2) {
            return starRadius + Math.log10(stellarRadiusSolarRadii) * 10;
        }
        return starRadius * stellarRadiusSolarRadii;
    }

    /**
     * Zoom in by a factor
     *
     * @param factor zoom factor (> 1 zooms in, < 1 zooms out)
     */
    public void zoom(double factor) {
        zoomLevel = Math.max(minZoom, Math.min(maxZoom, zoomLevel * factor));
    }

    /**
     * Reset zoom to default
     */
    public void resetZoom() {
        zoomLevel = 1.0;
    }

    /**
     * Get AU values for scale grid circles
     *
     * @return array of AU values for drawing reference circles
     */
    public double[] getScaleGridAuValues() {
        if (maxOrbitalDistanceAU <= 1) {
            return new double[]{0.1, 0.2, 0.5, 1.0};
        } else if (maxOrbitalDistanceAU <= 5) {
            return new double[]{0.5, 1.0, 2.0, 3.0, 5.0};
        } else if (maxOrbitalDistanceAU <= 10) {
            return new double[]{1.0, 2.0, 5.0, 10.0};
        } else if (maxOrbitalDistanceAU <= 50) {
            return new double[]{1.0, 5.0, 10.0, 20.0, 30.0, 50.0};
        } else {
            return new double[]{1.0, 10.0, 30.0, 50.0, 100.0};
        }
    }

}
