package com.teamgannon.trips.solarsysmodelling.utils;


import static java.lang.Math.log10;
import static java.lang.Math.pow;

public class StarUtils {

    public final static double zeroPointLuminosity = 3.0128E28;

    /**
     * in Watts
     */
    public final static double sunLuminosity = 3.828E26;

    /**
     * in kilometers
     */
    public final static double sunRadius = 695700;

    /**
     * in Kevin degrees
     */
    public final static double sunTemp = 5778;

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


}
