package com.teamgannon.trips.solarsysmodelling.utils;

import static java.lang.Math.*;

public class OrbitalDynamicsCircularUtils implements OrbitalDynamics {

    /**
     * periapsis distance - q
     *
     * @param semiMajorAxis length of the semi major axis
     * @return distance
     */
    public static double q(double semiMajorAxis) {
        return semiMajorAxis;
    }

    /**
     * Semi-latus rectum - p
     * The semi-latus rectum is the distance from the focal point to the curve measured perpendicular to the
     * eccentricity vector
     *
     * @param semiMajorAxis length of the semi major axis
     * @return distance
     */
    public static double p(double semiMajorAxis) {
        return semiMajorAxis;
    }

    /**
     * the total energy
     *
     * @param semiMajorAxis the semo major axis
     * @param mass          the mass
     * @return the energy
     */
    public static double totalEnergy(double semiMajorAxis, double mass) {
        return -k(mass) / (2 * semiMajorAxis);
    }

    /**
     * the distance from the central body
     *
     * @param semiMajorAxis the semimajor axis
     * @return the distance
     */
    public static double distanceFromCentralBody(double semiMajorAxis) {
        return semiMajorAxis;
    }

    /**
     * find the velocty
     *
     * @param semiMajorAxis the semi major axis
     * @param eccentricity  the eccentricity
     * @param mass          the mass
     * @return the velocity
     */
    public static double velocity(double semiMajorAxis, double eccentricity, double mass) {
        return 0;
    }

    /**
     * find the angle of velocity
     *
     * @param angle the angle
     * @return the angle
     */
    public static double angleOfVelocity(double angle) {
        return 0;
    }

    /**
     * periapsis velocity - vq
     *
     * @param semiMajorAxis the semi major axis
     * @param mass          the mass
     * @return the velocity
     */
    public static double periapsisVelocity(double semiMajorAxis, double mass) {
        return sqrt(k(mass) / semiMajorAxis);
    }

    public static double arealVelocity(double semiMajorAxis, double mass) {
        return sqrt(k(mass) * semiMajorAxis);
    }


    public static double orbitPeriod(double semiMajorAxis, double mass) {
        return 2 * PI * sqrt(pow(semiMajorAxis, 3) * semiMajorAxis);
    }

    public static double eccentricAnomaly(double angle) {
        return angle;
    }

    public static double meanAnomaly(double eccentricAnomaly) {
        return eccentricAnomaly;
    }

    /**
     * calculate time form periapsis
     *
     * @param meanAnomaly the mean anomaly
     * @param orbitPeriod the orbital period
     * @return the time
     */
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
