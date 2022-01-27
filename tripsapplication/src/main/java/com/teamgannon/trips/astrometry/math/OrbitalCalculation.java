package com.teamgannon.trips.astrometry.math;

import lombok.extern.slf4j.Slf4j;

import static com.teamgannon.trips.propulsion.Constants.G;
import static com.teamgannon.trips.propulsion.Constants.pi;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

/**
 * To mathematically describe an orbit one must define six quantities, called orbital elements. They are
 * <p>
 * -  Semi-Major Axis, a
 * -  Eccentricity, e
 * -  Inclination, i
 * -  Argument of Periapsis,
 * -  Time of Periapsis Passage, T
 * -  Longitude of Ascending Node,
 * <p>
 * An orbiting satellite follows an oval shaped path known as an ellipse with the body being orbited, called the primary,
 * located at one of two points called foci. An ellipse is defined to be a curve with the following property: for
 * each point on an ellipse, the sum of its distances from two fixed points, called foci, is constant
 * The longest and shortest lines that can be drawn through the center of an ellipse are called the major axis and
 * minor axis, respectively. The semi-major axis is one-half of the major axis and represents a satellite's mean distance
 * from its primary. Eccentricity is the distance between the foci divided by the length of the major axis and is a
 * number between zero and one. An eccentricity of zero indicates a circle.
 * <p>
 * Inclination is the angular distance between a satellite's orbital plane and the equator of its primary (or the
 * ecliptic plane in the case of heliocentric, or sun centered, orbits). An inclination of zero degrees indicates
 * an orbit about the primary's equator in the same direction as the primary's rotation, a direction called prograde
 * (or direct). An inclination of 90 degrees indicates a polar orbit. An inclination of 180 degrees indicates a
 * retrograde equatorial orbit. A retrograde orbit is one in which a satellite moves in a direction opposite to the
 * rotation of its primary.
 * <p>
 * Periapsis is the point in an orbit closest to the primary. The opposite of periapsis, the farthest point in an orbit,
 * is called apoapsis. Periapsis and apoapsis are usually modified to apply to the body being orbited, such as
 * perihelion and aphelion for the Sun, perigee and apogee for Earth, perijove and apojove for Jupiter, perilune and
 * apolune for the Moon, etc. The argument of periapsis is the angular distance between the ascending node and the
 * point of periapsis (see Figure 4.3). The time of periapsis passage is the time in which a satellite moves through
 * its point of periapsis.
 * <p>
 * Nodes are the points where an orbit crosses a plane, such as a satellite crossing the Earth's equatorial plane.
 * If the satellite crosses the plane going from south to north, the node is the ascending node; if moving from north
 * to south, it is the descending node. The longitude of the ascending node is the node's celestial longitude.
 * Celestial longitude is analogous to longitude on Earth and is measured in degrees counter-clockwise from zero with
 * zero longitude being in the direction of the vernal equinox.
 * <p>
 * <p>
 * In general, three observations of an object in orbit are required to calculate the six orbital elements.
 * Two other quantities often used to describe orbits are period and true anomaly. Period, P, is the length of
 * time required for a satellite to complete one orbit. True anomaly, , is the angular distance of a point in
 * an orbit past the point of periapsis, measured in degrees.
 * <p>
 * Types Of Orbits
 * <p>
 * For a spacecraft to achieve Earth orbit, it must be launched to an elevation above the Earth's atmosphere and
 * accelerated to orbital velocity. The most energy efficient orbit, that is one that requires the least amount
 * of propellant, is a direct low inclination orbit. To achieve such an orbit, a spacecraft is launched in an
 * eastward direction from a site near the Earth's equator. The advantage being that the rotational speed of the
 * Earth contributes to the spacecraft's final orbital speed. At the United States' launch site in Cape Canaveral
 * (28.5 degrees north latitude) a due east launch results in a "free ride" of 1,471 km/h (914 mph). Launching
 * a spacecraft in a direction other than east, or from a site far from the equator, results in an orbit of
 * higher inclination. High inclination orbits are less able to take advantage of the initial speed provided
 * by the Earth's rotation, thus the launch vehicle must provide a greater part, or all, of the energy required
 * to attain orbital velocity. Although high inclination orbits are less energy efficient, they do have advantages
 * over equatorial orbits for certain applications. Below we describe several types of orbits and the advantages
 * of each:
 * <p>
 * Geosynchronous orbits (GEO) are circular orbits around the Earth having a period of 24 hours. A geosynchronous
 * orbit with an inclination of zero degrees is called a geostationary orbit. A spacecraft in a geostationary orbit
 * appears to hang motionless above one position on the Earth's equator. For this reason, they are ideal for some
 * types of communication and meteorological satellites. A spacecraft in an inclined geosynchronous orbit will
 * appear to follow a regular figure-8 pattern in the sky once every orbit. To attain geosynchronous orbit,
 * a spacecraft is first launched into an elliptical orbit with an apogee of 35,786 km (22,236 miles) called
 * a geosynchronous transfer orbit (GTO). The orbit is then circularized by firing the spacecraft's engine at
 * apogee.
 * <p>
 * Polar orbits (PO) are orbits with an inclination of 90 degrees. Polar orbits are useful for satellites that
 * carry out mapping and/or surveillance operations because as the planet rotates the spacecraft has access
 * to virtually every point on the planet's surface.
 * <p>
 * Walking orbits: An orbiting satellite is subjected to a great many gravitational influences. First, planets
 * are not perfectly spherical and they have slightly uneven mass distribution. These fluctuations have an effect
 * on a spacecraft's trajectory. Also, the sun, moon, and planets contribute a gravitational influence on
 * an orbiting satellite. With proper planning it is possible to design an orbit which takes advantage of these
 * influences to induce a precession in the satellite's orbital plane. The resulting orbit is called a walking orbit,
 * or precessing orbit.
 * <p>
 * Sun synchronous orbits (SSO) are walking orbits whose orbital plane precesses with the same period as the planet's
 * solar orbit period. In such an orbit, a satellite crosses periapsis at about the same local time every orbit.
 * This is useful if a satellite is carrying instruments which depend on a certain angle of solar illumination on
 * the planet's surface. In order to maintain an exact synchronous timing, it may be necessary to conduct occasional
 * propulsive maneuvers to adjust the orbit.
 * <p>
 * Molniya orbits are highly eccentric Earth orbits with periods of approximately 12 hours (2 revolutions per day).
 * The orbital inclination is chosen so the rate of change of perigee is zero, thus both apogee and perigee can be
 * maintained over fixed latitudes. This condition occurs at inclinations of 63.4 degrees and 116.6 degrees. For
 * these orbits the argument of perigee is typically placed in the southern hemisphere, so the satellite remains
 * above the northern hemisphere near apogee for approximately 11 hours per orbit. This orientation can provide
 * good ground coverage at high northern latitudes.
 * <p>
 * Hohmann transfer orbits are interplanetary trajectories whose advantage is that they consume the least possible
 * amount of propellant. A Hohmann transfer orbit to an outer planet, such as Mars, is achieved by launching a
 * spacecraft and accelerating it in the direction of Earth's revolution around the sun until it breaks free of
 * the Earth's gravity and reaches a velocity which places it in a sun orbit with an aphelion equal to the orbit
 * of the outer planet. Upon reaching its destination, the spacecraft must decelerate so that the planet's
 * gravity can capture it into a planetary orbit.
 * <p>
 * To send a spacecraft to an inner planet, such as Venus, the spacecraft is launched and accelerated in the
 * direction opposite of Earth's revolution around the sun (i.e. decelerated) until it achieves a sun orbit with
 * a perihelion equal to the orbit of the inner planet. It should be noted that the spacecraft continues to move
 * in the same direction as Earth, only more slowly.
 * <p>
 * To reach a planet requires that the spacecraft be inserted into an interplanetary trajectory at the correct
 * time so that the spacecraft arrives at the planet's orbit when the planet will be at the point where the
 * spacecraft will intercept it. This task is comparable to a quarterback "leading" his receiver so that the
 * football and receiver arrive at the same point at the same time. The interval of time in which a spacecraft
 * must be launched in order to complete its mission is called a launch window.
 */
@Slf4j
public class OrbitalCalculation {


    /////////////////////////////////////////////////////////

    /**
     * find the GM
     *
     * @param mass the mass in kg
     * @return the GM
     */
    public double gm(double mass) {
        return G * mass;
    }

    //////////////////  Circular Orbits ///////////////////

    /**
     * find the velocity of a circular orbit around a planetary mass at a radius r
     * <p>
     * Calculate the velocity of an artificial satellite orbiting the Earth in a
     * circular orbit at an altitude of 200 km above the Earth's surface.
     * <p>
     * From Basics Constants,
     * <p>
     * Radius of Earth = 6,378.14 km
     * GM of Earth = 3.986005×1014 m3/s2
     * <p>
     * Given:  r = (6,378.14 + 200) × 1,000 = 6,578,140 m
     * <p>
     * Equation (4.6),
     * <p>
     * v = SQRT[ GM / r ]
     * v = SQRT[ 3.986005×1014 / 6,578,140 ]
     * v = 7,784 m/s
     *
     * @param radius the radius from center of mass
     * @param mass   the mass
     * @return the velocity
     */
    public double findVelocityInCircularOrbit(double radius, double mass) {
        return sqrt(gm(mass) / radius);
    }

    /**
     * calculate the rotational period for a circular orbit
     * <p>
     * Calculate the period of revolution for the satellite in problem 4.1.
     * <p>
     * Given:  r = 6,578,140 m
     * <p>
     * Equation (4.9),
     * <p>
     * P2 = 4 × 2 × r3 / GM
     * <p>
     * P = SQRT[ 4 × 2 × r3 / GM ]
     * P = SQRT[ 4 × 2 × 6,578,1403 / 3.986005×1014 ]
     * P = 5,310 s
     *
     * @param radius the radius from centre of mass
     * @param mass   the mass of the centre
     * @return rotational period
     */
    public double calcRotationalPeriodForCircularOrbit(double radius, double mass) {
        return sqrt(4 * pi * pi * pow(radius, 3) / gm(mass));
    }

    /**
     * find the centripetal acceleration of a circular orbit
     *
     * @param v the velocity
     * @param r the radius
     * @return centripetal acceleration
     */
    public double calcCentripetalAccelerationCircOrbit(double v, double r) {
        return v * v / r;
    }


    /**
     * calculate the Centripetal Force for a circular orbit
     * <p>
     * if the secondary mass is not negligible, the add it to the primary mass
     *
     * @param mass the mass of the primary
     * @param v    the velocity
     * @param r    the radius
     * @return Centripetal Force
     */
    public double calcCentripetalForceCircOrbit(double mass, double v, double r) {
        return mass * v * v / r;
    }

    ////////////////// Elliptical Orbits  ///////////////////

    /**
     * calculate the period of rotation from the semi major axis with mass m
     *
     * @param semiMajor the semi major axis
     * @param mass      the mass
     * @return the rotational period
     */
    public double calcRotationalPeriodElipticalOrbit(double semiMajor, double mass) {
        return sqrt(4 * pi * pi * pow(semiMajor, 3) / gm(mass));
    }

    /**
     * get the eccentricity of the orbit
     *
     * @param mass               the mass
     * @param perihelionRadius   the radius at perihelion
     * @param perihelionVelocity the velocity at perihelion
     * @return eccentricity of the orbit
     */
    public double getEccentricity(double mass, double perihelionRadius, double perihelionVelocity) {
        return perihelionRadius * perihelionVelocity * perihelionVelocity / gm(mass) - 1;
    }


    /////////////////////////////////////////////////////////

    /**
     * find the perihelion distance
     *
     * @param a the semi-major axis (or mean distance)
     * @param e the eccentricity
     * @return the perihelion distance
     */
    public double findPerihelionDistance(double a, double e) {
        return a * (1 - e);
    }

    /**
     * find the apihelion distance
     *
     * @param a the semi-major axis (or mean distance)
     * @param e the eccentricity
     * @return the perihelion distance
     */
    public double findAphelionDistance(double a, double e) {
        return a * (1 + e);
    }

    /**
     * find the orbital period in days
     *
     * @param a the semi-major axis (or mean distance)
     * @param m the mass of the planet in solar masses (0 for
     *          comets and asteroids)
     * @return the orbital period in days
     */
    public double findOrbitalPeriod(double a, double m) {
        return 365.256898326 * pow(a, 1.5 / sqrt(1 + m));
    }

    /**
     * find the daily motions of degrees per day
     *
     * @param p the orbital period
     * @return the daily motion
     */
    public double findDailyMotion(double p) {
        return 360 / p;
    }

    /**
     * @param timeAtPerihelion Time at perihelion
     * @param julianDay        Some epoch as a day count, e.g. Julian Day Number. The Time
     *                         at Perihelion, T, should then be expressed as the same day count.
     * @return the time since perihelion
     */
    public double timeSincePerihelion(double timeAtPerihelion, double julianDay) {
        return julianDay - timeAtPerihelion;
    }

    /**
     * find the mean anomaly
     * Mean Anomaly is 0 at perihelion and 180 degrees at aphelion
     *
     * @param timeSincePerihelion the time since perihelion
     * @param orbitalPeriod       the orbital period
     * @return the mean anomaly
     */
    public double findMeanAnomaly(double timeSincePerihelion, double orbitalPeriod) {
        return timeSincePerihelion * 360 / orbitalPeriod;
    }

    public double meanLongitude(double meanAnomaly, double angleFromAscendingNode, double longitudeOfAscendingNode) {
        return meanAnomaly + angleFromAscendingNode + longitudeOfAscendingNode;
    }


}
