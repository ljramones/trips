package com.teamgannon.trips.planetarymodelling.procedural;

import org.hipparchus.geometry.euclidean.threed.Vector3D;

import java.util.List;

/**
 * Assigns climate zones based on latitude.
 */
public class ClimateCalculator {

    public enum ClimateZone { TROPICAL, TEMPERATE, POLAR }

    private static final double TROPICAL_LIMIT = Math.toRadians(30);
    private static final double TEMPERATE_LIMIT = Math.toRadians(60);

    private final List<Polygon> polygons;

    public ClimateCalculator(List<Polygon> polygons) {
        this.polygons = polygons;
    }

    public ClimateZone[] calculate() {
        ClimateZone[] zones = new ClimateZone[polygons.size()];

        for (int i = 0; i < polygons.size(); i++) {
            double latitude = Math.abs(getLatitudeRadians(polygons.get(i).center()));

            if (latitude <= TROPICAL_LIMIT) {
                zones[i] = ClimateZone.TROPICAL;
            } else if (latitude <= TEMPERATE_LIMIT) {
                zones[i] = ClimateZone.TEMPERATE;
            } else {
                zones[i] = ClimateZone.POLAR;
            }
        }

        return zones;
    }

    public static double getLatitudeDegrees(Polygon polygon) {
        return Math.toDegrees(getLatitudeRadians(polygon.center()));
    }

    private static double getLatitudeRadians(Vector3D center) {
        double norm = center.getNorm();
        if (norm == 0.0) {
            return 0.0;
        }
        double clamped = Math.max(-1.0, Math.min(1.0, center.getY() / norm));
        return Math.asin(clamped);
    }
}
