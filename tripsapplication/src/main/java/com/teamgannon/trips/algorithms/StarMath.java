package com.teamgannon.trips.algorithms;

import com.teamgannon.trips.DistanceRange;

import static java.lang.Math.abs;
import static java.lang.Math.signum;

public class StarMath {

    /**
     * parsecs to light years for conversion
     */
    public final static double ParSecToLightYear = 3.26;

    /**
     * the right ascension and declination of the galactic center
     */
    private static final String galacticCenterRA = "17h 45.66m"; // hours minutes
    private static final String galacticCenterDec = "-28 56.3";  // degrees minutes

    /***
     * the right ascension and declination of the galactic north pole
     */
    private static final String galacticNorthPoleRA = "12h 51.4m"; // hours minutes
    private static final String galacticNorthPoleDec = "+27 07.7"; // degrees minutes

    /**
     * get the distance between two xyz locations
     *
     * @param a first location
     * @param b second location
     * @return the distance between these points
     */
    public static double getDistance(double[] a, double[] b) {

        double xPart = a[0] - b[0];
        double yPart = a[1] - b[1];
        double zPart = a[2] - b[2];
        return Math.sqrt(xPart * xPart + yPart * yPart + zPart * zPart
        );
    }

    public static boolean inSphere(double[] a, double[] b, double distance) throws Exception {
        double calcDistance = getDistance(a, b);
        return !(calcDistance >= distance);
    }

    /**
     * get position from right ascension, declination and distance
     *
     * @param rightAscension the right ascension
     * @param declination    the declination
     * @param distance       the distance in light years
     * @return the xyz coordinates
     */
    public static double[] getPosition(double rightAscension, double declination, double distance) {
        double[] coordinates = new double[3];

        // x
        coordinates[0] = distance * Math.cos(rightAscension) * Math.cos(declination);
        // y
        coordinates[1] = distance * Math.sin(rightAscension) * Math.cos(declination);
        // z
        coordinates[2] = distance * Math.sin(rightAscension);

        return coordinates;
    }

    /**
     * get the position based on used right ascension in hms and declination based on dms with distance
     *
     * @param raHours    right ascension in hours
     * @param raMinutes  right ascension in minutes
     * @param raSeconds  right ascension in seconds
     * @param decDegrees declination in degrees
     * @param decMinutes declination in minutes
     * @param decSecnds  declination in  seconds
     * @param distance   the distance in ly
     * @return the x,y,z coordinates
     */
    public static double[] getPosition(double raHours, double raMinutes, double raSeconds,
                                       double decDegrees, double decMinutes, double decSecnds,
                                       double distance) {

        double rightAscension = (raHours * 15) + (raMinutes * .25) + (raSeconds * 0.004166);
        double declination = (abs(decDegrees) + (decMinutes / 60) + (decSecnds / 3600)) * signum(decDegrees);
        return getPosition(rightAscension, declination, distance);
    }

    /**
     * convert epoch 1950 to galactic coordinates
     *
     * @param ep1950coor coordinates in epoch 1950 coordinates
     * @return galactic coordinates
     */
    public static double[] epoch1950ToGalacticCoordinates(double[] ep1950coor) {
        double[] coordinates = new double[3];

        coordinates[0] = -(0.0672 * ep1950coor[0]) - (0.8727 * ep1950coor[1]) - (0.4835 * ep1950coor[2]);
        coordinates[1] = (0.4927 * ep1950coor[0]) - (0.4504 * ep1950coor[1]) + (0.7445 * ep1950coor[2]);
        coordinates[2] = -(0.8676 * ep1950coor[0]) - (0.1884 * ep1950coor[1]) + (0.4602 * ep1950coor[2]);

        return coordinates;

    }

    /**
     * convert epoch 2000 to galactic coordinates
     *
     * @param ep2000coor coordinates in epoch 2000 format
     * @return galactic coordinates
     */
    public static double[] epoch2000ToGalacticCoordinates(double[] ep2000coor) {
        double[] coordinates = new double[3];

        coordinates[0] = -(0.0550 * ep2000coor[0]) - (0.8732 * ep2000coor[1]) - (0.4839 * ep2000coor[2]);
        coordinates[1] = (0.4940 * ep2000coor[0]) - (0.4449 * ep2000coor[1]) + (0.7470 * ep2000coor[2]);
        coordinates[2] = -(0.8677 * ep2000coor[0]) - (0.1979 * ep2000coor[1]) + (0.4560 * ep2000coor[2]);

        return coordinates;
    }

    /**
     * convert galactic coordinates to equatorial (solar) coordinates
     *
     * @param galCoor the galactic coordinates
     * @return the equatorial (solar) coordinates
     */
    public static double[] galacticToEquatorialCoordinates(double[] galCoor) {
        double[] coordinates = new double[3];

        coordinates[0] = galCoor[0] - 27058;
        coordinates[1] = galCoor[1];
        coordinates[2] = galCoor[2] + 48.9;

        return coordinates;
    }

    /**
     * converts parallax to light years
     *
     * @param parallax the parallax
     * @return the distance in lightyears
     */
    public static double distanceFromParallaxSecs(double parallax) throws Exception {
        if (parallax < 0) {
            throw new Exception("parallax cannot be negative");
        }
        return ParSecToLightYear / parallax;
    }

    public static DistanceRange distanceFromParallax(double parallax, double sigma) throws Exception {
        double base = distanceFromParallaxSecs(parallax);
        double variation = distanceFromParallaxSecs(sigma);
        DistanceRange range = new DistanceRange();
        range.setLower(base - variation);
        range.setUpper(base + variation);
        return range;
    }

    public static double distanceFromParallaxMilliSecs(double parallax) throws Exception {
        if (parallax < 0) {
            throw new Exception("parallax cannot be negative");
        }
        return ParSecToLightYear * 1000 / parallax;
    }

    /**
     * calculate distance from a parallax
     *
     * @param parallax the parallax
     * @param sigma    the +/- from the base
     * @return the distance range
     * @throws Exception if the parallax is bad
     */
    public static DistanceRange distanceFromParallaxMilliSecs(double parallax, double sigma) throws Exception {
        double base = distanceFromParallaxMilliSecs(parallax);
        double variation = distanceFromParallaxMilliSecs(sigma);
        DistanceRange range = new DistanceRange();
        range.setLower(base - variation);
        range.setUpper(base + variation);
        return range;
    }


    /**
     * convert parsecs to light years
     *
     * @param parsec the value in parsec
     * @return the amount of light years
     */
    public static double parsecToLy(double parsec) {
        return parsec * ParSecToLightYear;
    }

}
