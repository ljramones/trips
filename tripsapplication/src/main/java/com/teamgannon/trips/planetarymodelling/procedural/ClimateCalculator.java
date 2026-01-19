package com.teamgannon.trips.planetarymodelling.procedural;

import org.hipparchus.geometry.euclidean.threed.Vector3D;

import java.util.List;

/**
 * Assigns climate zones based on latitude and selected climate model.
 * Supports multiple climate patterns for different planetary conditions.
 */
public class ClimateCalculator {

    /**
     * Climate zone categories.
     */
    public enum ClimateZone { TROPICAL, TEMPERATE, POLAR }

    /**
     * Available climate models for planetary atmosphere simulation.
     *
     * <ul>
     *   <li>SIMPLE_LATITUDE: Basic 30°/60° latitude bands (Earth-like default)</li>
     *   <li>HADLEY_CELLS: Realistic with subtropical high pressure (desert) zones</li>
     *   <li>ICE_WORLD: Mostly frozen with narrow equatorial temperate band</li>
     *   <li>TROPICAL_WORLD: Hot planet with extended tropical zone and small polar caps</li>
     *   <li>TIDALLY_LOCKED: Day/night sides based on longitude (subsolar point = tropical)</li>
     * </ul>
     */
    public enum ClimateModel {
        /** Simple latitude bands: 0-30° tropical, 30-60° temperate, 60°+ polar */
        SIMPLE_LATITUDE,
        /** Hadley cells: includes subtropical deserts at ~25-35° */
        HADLEY_CELLS,
        /** Ice world: polar dominates, narrow temperate at equator, no tropical */
        ICE_WORLD,
        /** Hot tropical world: extended tropical zone (0-45°), small polar caps */
        TROPICAL_WORLD,
        /** Tidally locked: fixed day/night sides based on assumed subsolar point */
        TIDALLY_LOCKED,
        /** Seasonal insolation with axial tilt and averaged sunlight */
        SEASONAL
    }

    // Standard latitude limits (radians)
    private static final double TROPICAL_LIMIT = Math.toRadians(30);
    private static final double TEMPERATE_LIMIT = Math.toRadians(60);

    // Hadley cell model limits
    private static final double HADLEY_ITCZ_LIMIT = Math.toRadians(10);       // Equatorial rain belt
    private static final double HADLEY_SUBTROPICAL_LOW = Math.toRadians(25);  // Subtropical high start
    private static final double HADLEY_SUBTROPICAL_HIGH = Math.toRadians(35); // Subtropical high end
    private static final double HADLEY_TEMPERATE_LIMIT = Math.toRadians(60);

    // Ice world limits - narrow temperate band, extensive polar caps
    private static final double ICE_TEMPERATE_LIMIT = Math.toRadians(15);
    private static final double ICE_POLAR_START = Math.toRadians(30);

    // Tropical world limits
    private static final double HOT_TROPICAL_LIMIT = Math.toRadians(45);
    private static final double HOT_TEMPERATE_LIMIT = Math.toRadians(75);

    /**
     * Epsilon for floating-point comparisons. Used to safely check for
     * zero-length vectors without exact equality on doubles.
     */
    private static final double EPSILON = 1e-10;

    private final List<Polygon> polygons;
    private final ClimateModel model;
    private final double axialTiltRadians;
    private final double seasonalOffsetRadians;
    private final int seasonalSamples;

    /**
     * Create calculator with default SIMPLE_LATITUDE model.
     */
    public ClimateCalculator(List<Polygon> polygons) {
        this(polygons, ClimateModel.SIMPLE_LATITUDE, 0.0, 0.0, 12);
    }

    /**
     * Create calculator with specified climate model.
     */
    public ClimateCalculator(List<Polygon> polygons, ClimateModel model) {
        this(polygons, model, 0.0, 0.0, 12);
    }

    /**
     * Create calculator with specified climate model and seasonal parameters.
     */
    public ClimateCalculator(List<Polygon> polygons, ClimateModel model,
            double axialTiltDegrees, double seasonalOffsetDegrees, int seasonalSamples) {
        this.polygons = polygons;
        this.model = model != null ? model : ClimateModel.SIMPLE_LATITUDE;
        this.axialTiltRadians = Math.toRadians(axialTiltDegrees);
        this.seasonalOffsetRadians = Math.toRadians(seasonalOffsetDegrees);
        this.seasonalSamples = Math.max(4, Math.min(48, seasonalSamples));
    }

    /**
     * Calculate climate zones for all polygons using the configured model.
     */
    public ClimateZone[] calculate() {
        ClimateZone[] zones = new ClimateZone[polygons.size()];

        for (int i = 0; i < polygons.size(); i++) {
            Vector3D center = polygons.get(i).center();
            zones[i] = calculateZoneForModel(center);
        }

        return zones;
    }

    /**
     * Calculate climate zone based on the selected model.
     */
    private ClimateZone calculateZoneForModel(Vector3D center) {
        return switch (model) {
            case SIMPLE_LATITUDE -> calculateSimpleLatitude(center);
            case HADLEY_CELLS -> calculateHadleyCells(center);
            case ICE_WORLD -> calculateIceWorld(center);
            case TROPICAL_WORLD -> calculateTropicalWorld(center);
            case TIDALLY_LOCKED -> calculateTidallyLocked(center);
            case SEASONAL -> calculateSeasonalInsolation(center);
        };
    }

    /**
     * Simple latitude-based model (default Earth-like).
     * 0-30° = Tropical, 30-60° = Temperate, 60°+ = Polar
     */
    private ClimateZone calculateSimpleLatitude(Vector3D center) {
        double latitude = Math.abs(getLatitudeRadians(center));

        if (latitude <= TROPICAL_LIMIT) {
            return ClimateZone.TROPICAL;
        } else if (latitude <= TEMPERATE_LIMIT) {
            return ClimateZone.TEMPERATE;
        } else {
            return ClimateZone.POLAR;
        }
    }

    /**
     * Hadley cell model with realistic atmospheric circulation.
     * Features: equatorial rain belt, subtropical deserts, westerly temperate zones.
     *
     * Zones:
     * - 0-10° (ITCZ): Tropical rain belt
     * - 10-25°: Tropical (transitional)
     * - 25-35°: Temperate (subtropical high pressure, often desert-like in reality)
     * - 35-60°: Temperate (westerlies belt)
     * - 60°+: Polar
     */
    private ClimateZone calculateHadleyCells(Vector3D center) {
        double latitude = Math.abs(getLatitudeRadians(center));

        if (latitude <= HADLEY_ITCZ_LIMIT) {
            // Equatorial convergence zone - very wet tropical
            return ClimateZone.TROPICAL;
        } else if (latitude <= HADLEY_SUBTROPICAL_LOW) {
            // Transitional tropical
            return ClimateZone.TROPICAL;
        } else if (latitude <= HADLEY_SUBTROPICAL_HIGH) {
            // Subtropical high - mapped to temperate (represents dry zones)
            return ClimateZone.TEMPERATE;
        } else if (latitude <= HADLEY_TEMPERATE_LIMIT) {
            // Westerlies belt - temperate
            return ClimateZone.TEMPERATE;
        } else {
            return ClimateZone.POLAR;
        }
    }

    /**
     * Ice world model for cold planets (far from star, low greenhouse effect).
     * Polar zones dominate; narrow temperate band near equator; no true tropical.
     */
    private ClimateZone calculateIceWorld(Vector3D center) {
        double latitude = Math.abs(getLatitudeRadians(center));

        if (latitude <= ICE_TEMPERATE_LIMIT) {
            // Narrow "warm" belt at equator - still just temperate, no tropical
            return ClimateZone.TEMPERATE;
        } else if (latitude <= ICE_POLAR_START) {
            // Transitional zone
            return ClimateZone.TEMPERATE;
        } else {
            // Everything beyond 40° is polar ice
            return ClimateZone.POLAR;
        }
    }

    /**
     * Tropical world model for hot planets (close to star, high greenhouse).
     * Extended tropical zone with small polar caps.
     */
    private ClimateZone calculateTropicalWorld(Vector3D center) {
        double latitude = Math.abs(getLatitudeRadians(center));

        if (latitude <= HOT_TROPICAL_LIMIT) {
            // Extended tropical zone up to 45°
            return ClimateZone.TROPICAL;
        } else if (latitude <= HOT_TEMPERATE_LIMIT) {
            // Narrow temperate band
            return ClimateZone.TEMPERATE;
        } else {
            // Small polar caps beyond 75°
            return ClimateZone.POLAR;
        }
    }

    /**
     * Tidally locked model for planets orbiting close to their star.
     * One side permanently faces the star (hot), other side is dark (cold).
     * Uses longitude as proxy for day/night position (X coordinate).
     *
     * Subsolar point assumed at X=+1, antisolar at X=-1.
     */
    private ClimateZone calculateTidallyLocked(Vector3D center) {
        Vector3D normalized = center.normalize();

        // Dot product with "sun direction" (+X) gives how much this point faces the star
        // +1 = directly facing star, -1 = opposite side, 0 = terminator
        double starFacing = normalized.getX();

        if (starFacing > 0.5) {
            // Day side - tropical (hot)
            return ClimateZone.TROPICAL;
        } else if (starFacing > -0.3) {
            // Terminator region - temperate
            return ClimateZone.TEMPERATE;
        } else {
            // Night side - polar (frozen)
            return ClimateZone.POLAR;
        }
    }

    /**
     * Seasonal insolation model with axial tilt.
     * Uses average insolation over the year to assign climate zones.
     */
    private ClimateZone calculateSeasonalInsolation(Vector3D center) {
        double latitude = getLatitudeRadians(center);
        double avgInsolation = averageInsolation(latitude);

        if (avgInsolation >= 0.65) {
            return ClimateZone.TROPICAL;
        } else if (avgInsolation >= 0.3) {
            return ClimateZone.TEMPERATE;
        } else {
            return ClimateZone.POLAR;
        }
    }

    private double averageInsolation(double latitude) {
        double sum = 0.0;
        double step = (2.0 * Math.PI) / seasonalSamples;
        for (int i = 0; i < seasonalSamples; i++) {
            double phase = seasonalOffsetRadians + i * step;
            double subsolarLat = axialTiltRadians * Math.sin(phase);
            double cosZenith = Math.sin(latitude) * Math.sin(subsolarLat)
                + Math.cos(latitude) * Math.cos(subsolarLat);
            sum += Math.max(0.0, cosZenith);
        }
        return sum / seasonalSamples;
    }

    public static double getLatitudeDegrees(Polygon polygon) {
        return Math.toDegrees(getLatitudeRadians(polygon.center()));
    }

    private static double getLatitudeRadians(Vector3D center) {
        double norm = center.getNorm();
        // Use epsilon comparison for floating-point zero check
        if (norm < EPSILON) {
            return 0.0;
        }
        double clamped = Math.max(-1.0, Math.min(1.0, center.getY() / norm));
        return Math.asin(clamped);
    }
}
