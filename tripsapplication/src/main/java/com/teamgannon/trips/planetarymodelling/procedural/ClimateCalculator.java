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
            Vector3D center = polygons.get(i).center();
            Vector3D equatorial = new Vector3D(center.getX(), 0, center.getZ());
            double latitude = Vector3D.angle(center, equatorial);

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
        Vector3D center = polygon.center();
        Vector3D equatorial = new Vector3D(center.getX(), 0, center.getZ());
        double latitude = Vector3D.angle(center, equatorial);
        return Math.toDegrees(latitude) * (center.getY() >= 0 ? 1 : -1);
    }
}
