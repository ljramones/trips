package com.teamgannon.trips.dynamics;

public class DynamicsFactory {

    /**
     * Gravitional constant
     */
    public static final double G = 6.6743015e-11;

    /**
     * the value of an astronomical unit in meters.
     */
    public static final double AU = 149597870700.0;

    // planetary masses for major bodies on solar system
    public static final double SUN_MASS = 1.327124400189e20;
    public static final double MERCURY_MASS = 2.20329e13;
    public static final double VENUS_MASS = 3.248599e14;
    public static final double EARTH_MASS = 3.9860044188e14;
    public static final double MOON_MASS = 4.90486959e12;
    public static final double MARS_MASS = 4.2828372e13;
    public static final double JUPITER_MASS = 3.79311879e16;
    public static final double SATURN_MASS = 3.79311879e16;
    public static final double URANUS_MASS = 5.7939399e15;
    public static final double NEPTUNE_MASS = 6.8365299e15;

    // dwarf planets
    public static final double CERES_MASS = 6.26325e10;
    public static final double PLUTO_MASS = 8.719e11;
    public static final double ERIS_MASS = 1.1089e12;
    public static final double MAKEMAKE_MASS = 3.1e21;

    // AU values for solar system
    public static final double MERCURY_AU = 0.3871;
    public static final double VENUS_AU = 0.3871;
    public static final double EARTH_AU = 0.3871;
    public static final double MARS_AU = 0.3871;
    public static final double JUPITER_AU = 0.3871;
    public static final double SATURN_AU = 0.3871;
    public static final double URANUS_AU = 0.3871;
    public static final double NEPTUNE_AU = 0.3871;

    // dwarf planets
    public static final double CERES_AU = 0.3871;
    public static final double PLUTO_AU = 0.3871;
    public static final double HAUMEA_AU = 0.3871;
    public static final double MAKEMAKE_AU = 0.3871;
    public static final double ERIS_AU = 0.3871;


    /**
     * calculate the standard gravitational parameter μ of a celestial body
     *
     * @param mass the mass of the planet
     * @return the mu
     */
    public static double mu(double mass) {
        return G * mass;
    }

}
