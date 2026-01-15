package com.teamgannon.trips.solarsystem.rendering;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import lombok.extern.slf4j.Slf4j;

/**
 * Creates 3D visualizations of orbital paths.
 * Renders elliptical orbits with proper 3D orientation based on Keplerian elements.
 */
@Slf4j
public class OrbitVisualizer {

    /**
     * Number of segments to use when drawing orbital ellipse
     */
    private static final int ORBIT_SEGMENTS = 120;

    /**
     * Radius of the tube used to draw orbit path
     */
    private static final double ORBIT_TUBE_RADIUS = 0.5;

    private final ScaleManager scaleManager;

    public OrbitVisualizer(ScaleManager scaleManager) {
        this.scaleManager = scaleManager;
    }

    /**
     * Create a 3D orbital path visualization.
     *
     * @param semiMajorAxisAU              semi-major axis in AU
     * @param eccentricity                 orbital eccentricity (0 = circle, 0-1 = ellipse)
     * @param inclinationDeg               orbital inclination in degrees
     * @param longitudeOfAscendingNodeDeg  longitude of ascending node (Ω) in degrees
     * @param argumentOfPeriapsisDeg       argument of periapsis (ω) in degrees
     * @param orbitColor                   color for the orbit path
     * @return Group containing the orbital path
     */
    public Group createOrbitPath(double semiMajorAxisAU,
                                  double eccentricity,
                                  double inclinationDeg,
                                  double longitudeOfAscendingNodeDeg,
                                  double argumentOfPeriapsisDeg,
                                  Color orbitColor) {

        Group orbitGroup = new Group();

        // Calculate semi-minor axis: b = a * sqrt(1 - e²)
        double semiMinorAxisAU = semiMajorAxisAU * Math.sqrt(1 - eccentricity * eccentricity);

        double a = semiMajorAxisAU;
        double b = semiMinorAxisAU;

        // Calculate focus offset (center of ellipse is offset from focus where star is)
        double focusOffset = semiMajorAxisAU * eccentricity;

        // Create the ellipse as a series of connected cylinders (tube segments)
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(orbitColor);
        material.setSpecularColor(orbitColor.brighter());

        double[] prevPoint = null;
        double[] firstPoint = null;

        for (int i = 0; i <= ORBIT_SEGMENTS; i++) {
            double angle = 2 * Math.PI * i / ORBIT_SEGMENTS;

            // Parametric ellipse in the XZ plane (Y is up, XZ is the orbital plane)
            // This gives a proper top-down view of the solar system
            double x = a * Math.cos(angle);
            double y = 0;  // Orbital plane is XZ, Y is perpendicular (up)
            double z = b * Math.sin(angle);

            // Shift so the focus (star) is at origin
            x -= focusOffset;

            double[] point = toScreen(x, y, z);

            if (i == 0) {
                firstPoint = point;
            }

            if (prevPoint != null) {
                // Create cylinder segment between previous point and current point
                Cylinder segment = createSegment(prevPoint, point, ORBIT_TUBE_RADIUS, material);
                orbitGroup.getChildren().add(segment);
            }

            prevPoint = point;
        }

        // Close the loop by connecting last point to first
        if (prevPoint != null && firstPoint != null) {
            Cylinder segment = createSegment(prevPoint, firstPoint, ORBIT_TUBE_RADIUS, material);
            orbitGroup.getChildren().add(segment);
        }

        // Apply orbital rotations to match calculateOrbitalPosition():
        // JavaFX applies transforms first-to-last, so order must be:
        // 1. Argument of periapsis (ω) - rotation around Y axis (perpendicular to XZ plane)
        // 2. Inclination (i) - tilt the orbital plane around X axis
        // 3. Longitude of ascending node (Ω) - rotation around Y axis in reference plane
        // This gives R_LAN(R_inc(R_argPeri(P))) matching the position calculation.

        Rotate rotateArgPeri = new Rotate(argumentOfPeriapsisDeg, Rotate.Y_AXIS);
        Rotate rotateInclination = new Rotate(inclinationDeg, Rotate.X_AXIS);
        Rotate rotateLAN = new Rotate(longitudeOfAscendingNodeDeg, Rotate.Y_AXIS);

        orbitGroup.getTransforms().addAll(rotateArgPeri, rotateInclination, rotateLAN);

        return orbitGroup;
    }

    /**
     * Create a simple circular orbit path (for low eccentricity orbits)
     *
     * @param radiusAU       orbital radius in AU
     * @param inclinationDeg inclination in degrees
     * @param orbitColor     color for the orbit
     * @return Group containing the circular orbit path
     */
    public Group createCircularOrbitPath(double radiusAU, double inclinationDeg, Color orbitColor) {
        return createOrbitPath(radiusAU, 0.0, inclinationDeg, 0.0, 0.0, orbitColor);
    }

    /**
     * Calculate the position on an orbit at a given true anomaly.
     *
     * @param semiMajorAxisAU              semi-major axis in AU
     * @param eccentricity                 orbital eccentricity
     * @param inclinationDeg               orbital inclination in degrees
     * @param longitudeOfAscendingNodeDeg  longitude of ascending node in degrees
     * @param argumentOfPeriapsisDeg       argument of periapsis in degrees
     * @param trueAnomalyDeg               true anomaly (position on orbit) in degrees
     * @return double[3] array of {x, y, z} in screen coordinates
     */
    public double[] calculateOrbitalPosition(double semiMajorAxisAU,
                                              double eccentricity,
                                              double inclinationDeg,
                                              double longitudeOfAscendingNodeDeg,
                                              double argumentOfPeriapsisDeg,
                                              double trueAnomalyDeg) {

        double trueAnomalyRad = Math.toRadians(trueAnomalyDeg);
        double inclinationRad = Math.toRadians(inclinationDeg);
        double lanRad = Math.toRadians(longitudeOfAscendingNodeDeg);
        double argPeriRad = Math.toRadians(argumentOfPeriapsisDeg);

        // Calculate radius at this true anomaly
        // r = a(1 - e²) / (1 + e*cos(ν))
        double r = semiMajorAxisAU * (1 - eccentricity * eccentricity)
                / (1 + eccentricity * Math.cos(trueAnomalyRad));

        // Position in orbital plane (XZ plane, with Y as "up")
        // x and z are in the orbital plane, y is perpendicular
        double xOrbital = r * Math.cos(trueAnomalyRad);
        double zOrbital = r * Math.sin(trueAnomalyRad);

        // Transform to 3D space using rotation matrices
        // For XZ orbital plane: R_y(Ω) * R_x(i) * R_y(ω)

        double cosLAN = Math.cos(lanRad);
        double sinLAN = Math.sin(lanRad);
        double cosInc = Math.cos(inclinationRad);
        double sinInc = Math.sin(inclinationRad);
        double cosArg = Math.cos(argPeriRad);
        double sinArg = Math.sin(argPeriRad);

        // Apply argument of periapsis rotation (around Y axis in orbital plane)
        double x1 = xOrbital * cosArg + zOrbital * sinArg;
        double y1 = 0;
        double z1 = -xOrbital * sinArg + zOrbital * cosArg;

        // Apply inclination rotation (tilt orbital plane around X axis)
        double x2 = x1;
        double y2 = y1 * cosInc - z1 * sinInc;
        double z2 = y1 * sinInc + z1 * cosInc;

        // Apply longitude of ascending node rotation (around Y axis)
        double x3 = x2 * cosLAN + z2 * sinLAN;
        double y3 = y2;
        double z3 = -x2 * sinLAN + z2 * cosLAN;

        return toScreen(x3, y3, z3);
    }

    private double[] toScreen(double xAu, double yAu, double zAu) {
        double r = Math.sqrt(xAu * xAu + yAu * yAu + zAu * zAu);
        if (r == 0) {
            return new double[]{0, 0, 0};
        }
        double scaledR = scaleManager.auToScreen(r);
        double factor = scaledR / r;
        return new double[]{xAu * factor, yAu * factor, zAu * factor};
    }

    /**
     * Create a cylinder segment between two points
     */
    private Cylinder createSegment(double[] start, double[] end, double radius, PhongMaterial material) {
        // Calculate midpoint
        double midX = (start[0] + end[0]) / 2;
        double midY = (start[1] + end[1]) / 2;
        double midZ = (start[2] + end[2]) / 2;

        // Calculate length
        double dx = end[0] - start[0];
        double dy = end[1] - start[1];
        double dz = end[2] - start[2];
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);

        // Create cylinder (default orientation is along Y axis)
        Cylinder cylinder = new Cylinder(radius, length);
        cylinder.setMaterial(material);

        // Position at midpoint
        cylinder.setTranslateX(midX);
        cylinder.setTranslateY(midY);
        cylinder.setTranslateZ(midZ);

        // Rotate to align with the segment direction
        // Default cylinder is along Y axis, we need to rotate to point from start to end
        if (length > 0) {
            double[] direction = {dx / length, dy / length, dz / length};

            // Calculate rotation to align Y axis with direction vector
            // Using axis-angle representation
            double[] yAxis = {0, 1, 0};

            // Cross product gives rotation axis
            double axisX = yAxis[1] * direction[2] - yAxis[2] * direction[1];
            double axisY = yAxis[2] * direction[0] - yAxis[0] * direction[2];
            double axisZ = yAxis[0] * direction[1] - yAxis[1] * direction[0];

            double axisLength = Math.sqrt(axisX * axisX + axisY * axisY + axisZ * axisZ);

            if (axisLength > 0.0001) {
                // Dot product gives angle
                double dot = yAxis[0] * direction[0] + yAxis[1] * direction[1] + yAxis[2] * direction[2];
                double angle = Math.toDegrees(Math.acos(Math.max(-1, Math.min(1, dot))));

                Rotate rotate = new Rotate(angle, axisX / axisLength, axisY / axisLength, axisZ / axisLength);
                cylinder.getTransforms().add(rotate);
            }
        }

        return cylinder;
    }

    /**
     * Create a marker sphere at a position (for debugging or highlighting points)
     */
    public Sphere createPositionMarker(double[] screenPosition, double radius, Color color) {
        Sphere marker = new Sphere(radius);
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(color);
        marker.setMaterial(material);
        marker.setTranslateX(screenPosition[0]);
        marker.setTranslateY(screenPosition[1]);
        marker.setTranslateZ(screenPosition[2]);
        return marker;
    }

}
