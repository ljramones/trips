package com.teamgannon.trips.solarsystem.orbits;

/**
 * Orekit seam: current Keplerian orbit math implementation.
 * This is the replaceable integration point for future Orekit-based providers.
 */
public class KeplerOrbitSamplingProvider implements OrbitSamplingProvider {

    /**
     * Coordinate conventions:
     * - +Y is "up" (normal to the reference plane).
     * - The orbital plane is the XZ plane before rotations (Y = 0).
     * - Ellipse is sampled parametrically: x = a cos(t), z = b sin(t), with focus offset on +X.
     * - Rotation order matches the renderer: R_y(ω) then R_x(i) then R_y(Ω).
     *   This keeps orbit sampling and point positions consistent with visual transforms.
     */
    @Override
    public double[][] sampleEllipsePlanePointsAu(double semiMajorAxisAU, double eccentricity, int segments) {
        double semiMinorAxisAU = semiMajorAxisAU * Math.sqrt(1 - eccentricity * eccentricity);
        double focusOffset = semiMajorAxisAU * eccentricity;

        double[][] points = new double[segments + 1][3];
        for (int i = 0; i <= segments; i++) {
            double angle = 2 * Math.PI * i / segments;
            double x = semiMajorAxisAU * Math.cos(angle) - focusOffset;
            double y = 0;
            double z = semiMinorAxisAU * Math.sin(angle);
            points[i] = new double[]{x, y, z};
        }
        return points;
    }

    @Override
    public double[] calculatePositionAu(double semiMajorAxisAU,
                                        double eccentricity,
                                        double inclinationDeg,
                                        double longitudeOfAscendingNodeDeg,
                                        double argumentOfPeriapsisDeg,
                                        double trueAnomalyDeg) {

        double trueAnomalyRad = Math.toRadians(trueAnomalyDeg);
        double inclinationRad = Math.toRadians(inclinationDeg);
        double lanRad = Math.toRadians(longitudeOfAscendingNodeDeg);
        double argPeriRad = Math.toRadians(argumentOfPeriapsisDeg);

        // r = a(1 - e²) / (1 + e*cos(ν))
        double r = semiMajorAxisAU * (1 - eccentricity * eccentricity)
                / (1 + eccentricity * Math.cos(trueAnomalyRad));

        // Position in orbital plane (XZ plane, with Y as "up")
        double xOrbital = r * Math.cos(trueAnomalyRad);
        double zOrbital = r * Math.sin(trueAnomalyRad);

        // Rotation: R_y(Ω) * R_x(i) * R_y(ω)
        double cosLAN = Math.cos(lanRad);
        double sinLAN = Math.sin(lanRad);
        double cosInc = Math.cos(inclinationRad);
        double sinInc = Math.sin(inclinationRad);
        double cosArg = Math.cos(argPeriRad);
        double sinArg = Math.sin(argPeriRad);

        // Argument of periapsis rotation (around Y axis in orbital plane)
        double x1 = xOrbital * cosArg + zOrbital * sinArg;
        double y1 = 0;
        double z1 = -xOrbital * sinArg + zOrbital * cosArg;

        // Inclination rotation (tilt orbital plane around X axis)
        double x2 = x1;
        double y2 = y1 * cosInc - z1 * sinInc;
        double z2 = y1 * sinInc + z1 * cosInc;

        // Longitude of ascending node rotation (around Y axis)
        double x3 = x2 * cosLAN + z2 * sinLAN;
        double y3 = y2;
        double z3 = -x2 * sinLAN + z2 * cosLAN;

        return new double[]{x3, y3, z3};
    }
}
