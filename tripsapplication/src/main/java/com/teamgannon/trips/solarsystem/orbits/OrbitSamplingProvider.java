package com.teamgannon.trips.solarsystem.orbits;

/**
 * Orekit seam: provides orbit sampling and position data without renderer dependencies.
 */
public interface OrbitSamplingProvider {

    /**
     * Sample an orbit in the local orbital plane (XZ) with the focus at the origin.
     *
     * @param semiMajorAxisAU semi-major axis in AU
     * @param eccentricity    orbital eccentricity
     * @param segments        number of segments for sampling
     * @return array of points in AU, sized segments + 1
     */
    double[][] sampleEllipsePlanePointsAu(double semiMajorAxisAU, double eccentricity, int segments);

    /**
     * Calculate an inertial-frame position for a given true anomaly.
     *
     * @param semiMajorAxisAU             semi-major axis in AU
     * @param eccentricity                orbital eccentricity
     * @param inclinationDeg              inclination in degrees
     * @param longitudeOfAscendingNodeDeg longitude of ascending node in degrees
     * @param argumentOfPeriapsisDeg      argument of periapsis in degrees
     * @param trueAnomalyDeg              true anomaly in degrees
     * @return AU position {x, y, z}
     */
    double[] calculatePositionAu(double semiMajorAxisAU,
                                 double eccentricity,
                                 double inclinationDeg,
                                 double longitudeOfAscendingNodeDeg,
                                 double argumentOfPeriapsisDeg,
                                 double trueAnomalyDeg);
}
