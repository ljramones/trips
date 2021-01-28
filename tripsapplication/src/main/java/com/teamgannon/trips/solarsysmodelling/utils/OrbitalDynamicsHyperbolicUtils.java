package com.teamgannon.trips.solarsysmodelling.utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OrbitalDynamicsHyperbolicUtils implements OrbitalDynamics {

    /**
     * periapsis distance - q
     *
     * @param semiMajorAxis length of the semi major axis
     * @param eccentricity  eccentricity
     * @return distance
     */
    public static double periapsisDistance(double semiMajorAxis, double eccentricity) {
        return semiMajorAxis * (1 - eccentricity);
    }

    /**
     * Semi-latus rectum - p
     * The semi-latus rectum is the distance from the focal point to the curve measured perpendicular to the
     * eccentricity vector
     *
     * @param semiMajorAxis length of the semi major axis
     * @param eccentricity  eccentricity
     * @return distance
     */
    public static double semiLatusRectum(double semiMajorAxis, double eccentricity) {
        return semiMajorAxis * (1 - eccentricity * eccentricity);
    }

    public static double totalEnergy(double semiMajorAxis, OrbitalTypeEnum orbitalType) {
        return 0;
    }

    public static double distanceFromCentralBody(double semiLatusRectum, double eccentricity) {
        return 0;
    }

    public static double velocity(double semiLatusRectum, double eccentricity) {
        return 0;
    }

    public static double angleOfVelocity(double angle, double eccentricity) {
        return 0;
    }

    public static double orbitPeriod(double semiLatusRectum, double mass) {
        return 0;
    }

    public static double eccentricAnomaly(double angle) {
        return 0;
    }

    public static double meanAnomaly(double totalEnergy, double eccentricity) {
        return 0;
    }

    ////

    public static double timeFromPeriapsis(double meanAnomaly, double orbitPeriod, double mass) {
        return 0;
    }

    //////////

    /**
     * orbital mechanics constant - k
     *
     * @param mass the object mass
     * @return the k constant
     */
    public static double k(double mass) {
        return mass * G;
    }

}
