package com.teamgannon.trips.utility;


import static java.lang.Math.*;

/**
 * Used to calculate between coordinate systems
 * <p>
 * Created by larrymitchell on 2017-03-04.
 */
public class CoordinateCalculator {


    public static CartesianCoordinate convertToCartesian(SphericalCoordinate sphericalCoordinate) {
        double a = convertRAtoDegrees(sphericalCoordinate.getRightAscension());
        double b = convertDeclinationToB(sphericalCoordinate.getDeclination());
        double distance = sphericalCoordinate.getDistance();

        CartesianCoordinate cartesianCoordinate = new CartesianCoordinate();
        cartesianCoordinate.setX(xCalculate(a, b, distance));
        cartesianCoordinate.setY(yCalculate(a, b, distance));
        cartesianCoordinate.setZ(zCalculate(a, b, distance));

        return cartesianCoordinate;
    }

    public static double calculateDistance(CartesianCoordinate from, CartesianCoordinate to) {
        double xdiff = to.getX() - from.getX();
        double ydiff = to.getY() - from.getY();
        double zdiff = to.getZ() - from.getZ();
        return sqrt(
                xdiff * xdiff + ydiff * ydiff + zdiff * zdiff
        );
    }

    /**
     * calculate the x cartesian coordinate
     * <p>
     * X = (C * cos(B)) * cos(A)
     *
     * @param a        the ascension isn degrees
     * @param b        the declination in degrees
     * @param distance the distance in light years
     * @return the x cartesian coordinate
     */
    public static double xCalculate(double a, double b, double distance) {
        return distance * cos(b) * cos(a);
    }

    /**
     * calculate the y cartesian coordinate
     * <p>
     * Y = (C * cos(B)) * sin(A)
     *
     * @param a        the ascension isn degrees
     * @param b        the declination in degrees
     * @param distance the distance in light years
     * @return the x cartesian coordinate
     */
    public static double yCalculate(double a, double b, double distance) {
        return distance * cos(b) * sin(a);
    }

    /**
     * calculate the z cartesian coordinate
     * <p>
     * Z = C * sin(B)
     *
     * @param a        the ascension isn degrees
     * @param b        the declination in degrees
     * @param distance the distance in light years
     * @return the x cartesian coordinate
     */
    public static double zCalculate(double a, double b, double distance) {
        return distance * sin(b);
    }

    /**
     * convert the Right Ascension to degrees
     * <p>
     * 05h 14m 32.27210s
     *
     * @param rightAscension the right ascension
     * @return degree representation
     */
    public static double convertRAtoDegrees(String rightAscension) {
        int hours = Integer.parseInt(rightAscension.substring(0, 1));
        int minutes = Integer.parseInt(rightAscension.substring(2, 3));
        int seconds = Integer.parseInt(rightAscension.substring(4, 5));

        return (hours * 15) + (minutes * 0.25) + (seconds * 0.004166);
    }

    /**
     * convert declination to B
     * <p>
     * −08° 12′ 14.78″
     *
     * @param declination the declination as a string
     * @return as a B value
     */
    private static double convertDeclinationToB(String declination) {
        String[] parts = declination.split("\\s+");
        // B = ( ABS(Dec_degrees) + (Dec_minutes / 60) + (Dec_seconds / 3600)) * SIGN(Dec_Degrees)
        double degrees = Double.parseDouble(parts[0]);
        double minutes = Double.parseDouble(parts[1]);
        double seconds = Double.parseDouble(parts[2]);
        return (abs(degrees) + (minutes / 60) + (seconds / 3600) * signum(degrees));
    }

}
