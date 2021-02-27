package com.teamgannon.trips.stellarmodelling;


import static java.lang.Math.log10;
import static java.lang.Math.pow;

public class StarUtils {

    public final static double zeroPointLuminosity = 3.0128E28;

    /**
     * in Watts
     */
    public final static double sunLuminosity = 3.828E26;

    /**
     * sun mass in kg
     */
    public final static double sunMass = 1.989e30;

    /**
     * sun's density iin g/cm3
     */
    public final static double sunDensity = 1.41;

    /**
     * sun's age in years
     */
    public final static double sunAge = 4.603E9;

    /**
     * in kilometers
     */
    public final static double sunRadius = 695700;

    /**
     * in Kevin degrees
     */
    public final static double sunTemp = 5778;

    /**
     * the Absolute Magnitude of the sun
     */
    public final static double sunAbsMag = 4.83;

    /**
     * the sun's metallicity
     */
    public final static double sunMetallicity = 0.0122;

    /**
     * one astronomical unit which is distance from sun to earth
     */
    public final static double AU = 149.6E6;

    /**
     * determine the stellar luminosity
     *
     * @param radius the star radius
     * @param temp   the star surface temp
     * @return the luminosity
     */
    public static double stellarLuminosity(double radius, double temp) {
        return sunLuminosity * (radius / sunRadius) * pow(temp / sunTemp, 4);
    }

    /**
     * find the absolute magnitude
     *
     * @param luminosity the star luminosity
     * @return the absolute magnitude
     */
    public static double absoluteMagnitude(double luminosity) {
        return 2.5 * log10(luminosity / zeroPointLuminosity);
    }

    /**
     * find the apparent magnitude
     *
     * @param absMag   the absolute magnitude
     * @param distance the distance
     * @return the apparent magnitude
     */
    public static double apparentMagnitude(double absMag, double distance) {
        return absMag - 5 + 5 * log10(distance);
    }

    /**
     * find the radius relative to the Sun
     *
     * @param radius the radius to compare
     * @return the relative value
     */
    public static double relativeRadius(double radius) {
        return radius / sunRadius;
    }

    /**
     * find the mass relative to the sun
     *
     * @param mass the mass
     * @return the relative value
     */
    public static double relativeMass(double mass) {
        return mass / sunMass;
    }

    /**
     * find the luminosity relative to the sun
     *
     * @param luminosity the luminosity
     * @return the relative value
     */
    public static double relativeLuminosity(double luminosity) {
        return luminosity / sunLuminosity;
    }

    /**
     * find the age relative to the sun
     *
     * @param density the density
     * @return the relative value
     */
    public static double relativeDensity(double density) {
        return density / sunDensity;
    }

    /**
     * find the age relative to the sun
     *
     * @param age the age
     * @return the relative value
     */
    public static double relativeAge(double age) {
        return age / sunAge;
    }

    /**
     * find the metallicity relative to the sun
     *
     * @param metallicity the metallicity
     * @return the relative value
     */
    public static double relativeMetallicity(double metallicity) {
        return metallicity / sunMetallicity;
    }

    /**
     * find the absolute magnitude relative to the sun
     *
     * @param absMagnitude the absolute magnitude
     * @return the relative value
     */
    public static double relativeAbsMagnitude(double absMagnitude) {
        return absMagnitude / sunAbsMag;
    }

}
