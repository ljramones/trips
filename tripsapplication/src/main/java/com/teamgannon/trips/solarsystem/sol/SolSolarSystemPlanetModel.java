package com.teamgannon.trips.solarsystem.sol;

import com.opencsv.bean.CsvBindByPosition;
import lombok.Data;

@Data
public class SolSolarSystemPlanetModel {

    /**
     * common name
     */
    @CsvBindByPosition(position = 0)
    private String name;

    /**
     *  This is the mass of the planet in septillion (1 followed by 24 zeros) kilograms or sextillion (1 followed
     *  by 21 zeros) tons. Strictly speaking tons are measures of weight, not mass, but are used here to represent
     *  the mass of one ton of material under Earth gravity.
     *
     *  10^24 kg
     */
    @CsvBindByPosition(position = 1)
    private double mass;

    /**
     * The diameter of the planet at the equator, the distance through the center of the planet from one point on the
     * equator to the opposite side, in kilometers.
     *
     * km
     */
    @CsvBindByPosition(position = 2)
    private double diameter;

    /**
     * The average density (mass divided by volume) of the whole planet (not including the atmosphere for the
     * terrestrial planets) in kilograms per cubic meter or pounds per cubic foot. Strictly speaking pounds are
     * measures of weight, not mass, but are used here to represent the mass of one pound of material under Earth
     * gravity.
     *
     * kg/m3
     */
    @CsvBindByPosition(position = 3)
    private double density;

    /**
     * The gravitational acceleration on the surface at the equator in meters per second squared or feet per second
     * squared, including the effects of rotation. For the gas giant planets the gravity is given at the 1 bar
     * pressure level in the atmosphere. The gravity on Earth is designated as 1 "G", so the Earth ratio fact sheets
     * gives the gravity of the other planets in G's.
     *
     * m/s2
     */
    @CsvBindByPosition(position = 4)
    private double gravity;

    /**
     * Initial velocity, in kilometers per second or miles per second, needed at the surface (at the 1 bar
     * pressure level for the gas giants) to escape the body's gravitational pull, ignoring atmospheric drag.
     *
     * km/s
     */
    @CsvBindByPosition(position = 5)
    private double escapeVelocity;

    /**
     * This is the time it takes for the planet to complete one rotation relative to the fixed background stars
     * (not relative to the Sun) in hours. Negative numbers indicate retrograde (backwards relative to the Earth)
     * rotation.
     *
     * hours
     */
    @CsvBindByPosition(position = 6)
    private double rotationalPeriod;

    /**
     * The average time in hours for the Sun to move from the noon position in the sky at a point on the equator
     * back to the same position.
     *
     * hours
     */
    @CsvBindByPosition(position = 7)
    private double lengthOfDay;

    /**
     * This is the average distance from the planet to the Sun in millions of kilometers or millions of miles,
     * also known as the semi-major axis. All planets have orbits which are elliptical, not perfectly circular,
     * so there is a point in the orbit at which the planet is closest to the Sun, the perihelion, and a point
     * furthest from the Sun, the aphelion. The average distance from the Sun is midway between these two values.
     * The average distance from the Earth to the Sun is defined as 1 Astronomical Unit (AU), so the ratio table
     * gives this distance in AU.
     * For the Moon, the average distance from the Earth is given.
     *
     * 10^6 km
     */
    @CsvBindByPosition(position = 8)
    private double distanceFromStar;

    /**
     * The closest and furthest points in a planet's orbit about the Sun, see "Distance from Sun" above.
     * For the Moon, the closest and furthest points to Earth are given, known as the "Perigee" and "Apogee"
     * respectively.
     *
     * 10^6 km
     */
    @CsvBindByPosition(position = 9)
    private double perihelion;

    /**
     * The closest and furthest points in a planet's orbit about the Sun, see "Distance from Sun" above.
     * For the Moon, the closest and furthest points to Earth are given, known as the "Perigee" and "Apogee"
     * respectively.
     *
     * 10^6 km
     */
    @CsvBindByPosition(position = 10)
    private double aphelion;

    /**
     * This is the time in Earth days for a planet to orbit the Sun from one vernal equinox to the next. Also known
     * as the tropical orbit period, this is equal to a year on Earth.
     * * For the Moon, the sidereal orbit period, the time to orbit once relative to the fixed background stars,
     * is given. The time from full Moon to full Moon, or synodic period, is 29.53 days. For Pluto, the tropical
     * orbit period is not well known, the sidereal orbit period is used.
     */
    @CsvBindByPosition(position = 11)
    private double orbitalPeriod;

    /**
     * The average velocity or speed of the planet as it orbits the Sun, in kilometers per second or miles per second.
     * For the Moon, the average velocity around the Earth is given.
     *
     * days
     */
    @CsvBindByPosition(position = 12)
    private double orbitalVelocity;

    /**
     * The angle in degrees at which a planets orbit around the Sun is tilted relative to the ecliptic plane. The
     * ecliptic plane is defined as the plane containing the Earth's orbit, so the Earth's inclination is 0.
     *
     * degrees
     */
    @CsvBindByPosition(position = 13)
    private double orbitalInclination;

    /**
     * This is a measure of how far a planet's orbit about the Sun (or the Moon's orbit about the Earth) is from
     * being circular. The larger the eccentricity, the more elongated is the orbit, an eccentricity of 0 means
     * the orbit is a perfect circle. There are no units for eccentricity.
     *
     */
    @CsvBindByPosition(position = 14)
    private double orbitalEccentricity;

    /**
     * The angle in degrees the axis of a planet (the imaginary line running through the center of the planet from the
     * north to south poles) is tilted relative to a line perpendicular to the planet's orbit around the Sun,
     * north pole defined by right hand rule.
     * *Venus rotates in a retrograde direction, opposite the other planets, so the tilt is almost 180 degrees,
     * it is considered to be spinning with its "top", or north pole pointing "downward" (southward). Uranus
     * rotates almost on its side relative to the orbit, Pluto is pointing slightly "down". The ratios with Earth
     * refer to the axis without reference to north or south.
     *
     * degrees
     */
    @CsvBindByPosition(position = 15)
    private double obliquityToOrbit;

    /**
     * This is the average temperature over the whole planet's surface (or for the gas giants at the one bar level)
     * in degrees C (Celsius or Centigrade) or degrees F (Fahrenheit). For Mercury and the Moon, for example,
     * this is an average over the sunlit (very hot) and dark (very cold) hemispheres and so is not
     * representative of any given region on the planet, and most of the surface is quite different from this
     * average value. As with the Earth, there will tend to be variations in temperature from the equator to the
     * poles, from the day to night sides, and seasonal changes on most of the planets.
     *
     * degrees C
     */
    @CsvBindByPosition(position = 16)
    private double meanTemperature;

    /**
     * This is the atmospheric pressure (the weight of the atmosphere per unit area) at the surface of the planet in
     * bars or atmospheres.
     * The surfaces of Jupiter, Saturn, Uranus, and Neptune are deep in the atmosphere and the location and
     * pressures are not known.
     *
     * bars
     */
    @CsvBindByPosition(position = 17)
    private double surfacePressure;

    /**
     * This gives the number of IAU officially confirmed moons orbiting the planet. New moons are still being discovered.
     */
    @CsvBindByPosition(position = 18)
    private int numberOfMoons;

    /**
     * This tells whether a planet has a set of rings around it, Saturn being the most obvious example.
     */
    @CsvBindByPosition(position = 19)
    private boolean ringSystem;

    /**
     * This tells whether the planet has a measurable large-scale magnetic field. Mars and the Moon have
     * localized regional magnetic fields but no global field.
     */
    @CsvBindByPosition(position = 20)
    private boolean globalMagneticField;

}
