package com.teamgannon.trips.solarsysmodelling.utils;

import static java.lang.Math.*;

/**
 * orbital mechanics equations
 */
public class OrbitalDynamicsUtils implements OrbitalDynamics {

    /**
     * to find what speed an object needs to gain in order to leave the surface of any celestial body,
     * opposing its gravity.
     *
     * @param mass   the mass
     * @param radius the radius
     * @return the escape velocity
     */
    public static double escapeVelocity(double mass, double radius) {
        return sqrt(2 * G * mass / radius);
    }

    /**
     * The first cosmic velocity is the velocity that an object needs to orbit the celestial body. For example,
     * all satellites need to have this velocity in order not to fall back to the surface of Earth. It is equal
     * to the escape velocity divided by the square root of 2. The full formula looks like this:
     *
     * @param mass   mass of the body
     * @param radius radius
     * @return the first cosmic velocity
     */
    public static double firstCosmicVelocity(double mass, double radius) {
        return sqrt(G * mass / radius);
    }

    /**
     * Satellite around central body (say like earth or a planet), where the orbiting object
     * is quite small in comparison
     *
     * @param centralBodyDensity density of the body at rest (that you orbit) in g/cm^3
     * @return the period in hours
     */
    public static double lowOrbitalPeriod(double centralBodyDensity) {
        return sqrt(3 * PI / (G * centralBodyDensity));
    }

    /**
     * calculate the orbital period of a system made of two objects of similar size orbiting each other.
     *
     * @param firstMass     the mass of the first object
     * @param secondMass    the mass of the second object
     * @param semiMajorAxis the semomajor axis between them
     * @return the orbital period
     */
    public static double orbitalPeriod(double firstMass, double secondMass, double semiMajorAxis) {
        return 2 * PI * sqrt(pow(semiMajorAxis, 3) / (G * (firstMass + secondMass)));
    }

    /**
     * calculate the eccentricity of the orbit - e
     * The orbital eccentricity is a parameter that characterizes the shape of the orbit. The higher is its value,
     * the more flattened ellipse becomes. It is linked to the other two important parameters: semi-major axis
     * and semi-minor axis
     * circular: e = 0
     * elliptical: 0 < e < 1
     * parabolic: e = 1
     * hyperbolic: e>1
     *
     * @param semiMajorAxis length of the semi major axis
     * @param semiMinorAxis length of the semi minor axis
     * @return the eccentricity
     */
    public static double eccentricity(double semiMajorAxis, double semiMinorAxis) {
        return sqrt((1 - (semiMinorAxis * semiMinorAxis) / (semiMajorAxis * semiMajorAxis)));
    }


    ///////////////////////////////////////

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

    /**
     * find the total energy
     *
     * @param mass          the mass
     * @param semiMajorAxis the semi major axis
     * @return the energy
     */
    public static double totalEnergy(double mass, double semiMajorAxis) {
        return -k(mass) / (2 * semiMajorAxis);
    }

    /**
     * find the distance from the central body
     *
     * @param semiLatusRectum the semi latus rectum
     * @param eccentricity    the eccentricity
     * @param angle           the current angle
     * @return the current distance
     */
    public static double distanceFromCentralBody(double semiLatusRectum, double eccentricity, double angle) {
        return semiLatusRectum / (1 + eccentricity * cos(angle));
    }

    /**
     * the current velocity -v
     * v2 = [k/p](1+e2+2 e cos(θ))
     *
     * @param semiMajorAxis the semi major axis
     * @param mass          the mass
     * @param eccentricity  the eccentricity
     * @return the current velocity
     */
    public static double velocity(double mass, double semiMajorAxis, double eccentricity, double angle) {
        return sqrt((k(mass) / semiLatusRectum(semiMajorAxis, eccentricity)) * (1 + eccentricity * eccentricity + 2 * eccentricity * cos(angle)));
    }

    /**
     * the angle of velocity
     * tan(φ) = [e sin(θ)/(1 + e cos(θ))]
     *
     * @param angle        the angle
     * @param eccentricity the eccentricity
     * @return the angle
     */
    public static double angleOfVelocity(double angle, double eccentricity) {
        return atan(eccentricity * sin(angle) / (1 + eccentricity * cos(angle)));
    }

    /**
     * the periapsis velocity - vq
     * vq = [(k/a)(1+e)/(1-e)]1/2
     *
     * @return the velocity
     */
    public static double periapsisVelocity(double mass, double semiMajorAxis, double eccentricity) {
        return sqrt((k(mass) / semiMajorAxis) * (1 + eccentricity) / (1 - eccentricity));
    }

    /**
     * orbital period
     * P = 2π[a3/k]1/2
     *
     * @param semiMajorAxis the semi major axis
     * @param mass          the mass
     * @return the orbital period
     */
    public static double orbitPeriod(double semiMajorAxis, double mass) {
        return 2 * PI * sqrt(pow(semiMajorAxis, 3 / k(mass)));
    }

    /**
     * eccentric anomaly
     * [e + cos(θ)]
     * cos(E) = -------------------
     * [1 + e cos(θ)
     *
     * @param angle        the angle
     * @param eccentricity the eccentricity
     * @return the eccentric anomaly
     */
    public static double eccentricAnomaly(double angle, double eccentricity) {
        return acos((eccentricity + cos(angle)) / (1 + eccentricity * cos(angle)));
    }

    /**
     * mean anomaly
     * M = E - e sin(E)
     *
     * @param angle        the angle
     * @param eccentricity the eccentricity
     * @return mean anomaly
     */
    public static double meanAnomaly(double angle, double eccentricity) {
        double ea = eccentricAnomaly(angle, eccentricity);
        return ea - eccentricity * sin(ea);
    }

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

    /**
     * determine the orbit type based on the eccentricity
     * these are expressed as conical sections
     *
     * @param eccentricity the eccentricity
     * @return the type
     */
    public OrbitalTypeEnum whatTypeOfOrbit(double eccentricity) {
        if (eccentricity == 0) {
            return OrbitalTypeEnum.Circular;
        }
        if (eccentricity > 0 && eccentricity < 1) {
            return OrbitalTypeEnum.Elliptical;
        }
        if (eccentricity == 1) {
            return OrbitalTypeEnum.Parabolic;
        }
        return OrbitalTypeEnum.Hyperbolic;
    }

}
