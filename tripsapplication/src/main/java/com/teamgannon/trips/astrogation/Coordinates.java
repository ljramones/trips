package com.teamgannon.trips.astrogation;


import lombok.extern.slf4j.Slf4j;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.TimeStampedPVCoordinates;

@Slf4j
public class Coordinates {


    // Constants for the North Galactic Pole and the Galactic Center in degrees
    private static final double ALPHA_NGP = 192.85948;
    private static final double DELTA_NGP = 27.12825;
    private static final double ALPHA_GC = 266.40510;
    private static final double THETA = 122.93192;


    private static final double[][] TRANSFORMATION_MATRIX = {
            {-0.0548755604, -0.8734370902, -0.4838350155},
            {0.4941094279, -0.4448296298, 0.7469822445},
            {-0.8676661490, -0.1980763734, 0.4559837762}
    };

    private static final double[][] INVERSE_TRANSFORMATION_MATRIX = {
            {-0.0548755604, 0.4941094279, -0.8676661490},
            {-0.8734370902, -0.4448296298, -0.1980763734},
            {-0.4838350155, 0.7469822445, 0.4559837762}
    };

    /**
     * convert to equatorial coordinates
     *
     * @param RA                   the right ascension
     * @param Dec                  the declination
     * @param distanceInLightYears the distance in light years
     * @return the coordinates x,y,z
     */
    public static double[] calculateEquatorialCoordinates(double RA, double Dec, double distanceInLightYears) {
        // Convert RA and Dec to radians if they are in degrees
        double RA_rad = Math.toRadians(RA);
        double Dec_rad = Math.toRadians(Dec);

        // Calculate x, y, and z coordinates using distance in light years
        double x = distanceInLightYears * Math.cos(Dec_rad) * Math.cos(RA_rad);
        double y = distanceInLightYears * Math.cos(Dec_rad) * Math.sin(RA_rad);
        double z = distanceInLightYears * Math.sin(Dec_rad);

        return new double[]{x, y, z};
    }

    /**
     * calculate equatorial coordinates
     *
     * @param raH      hours
     * @param raM      minutes
     * @param raS      seconds
     * @param decDeg   degrees
     * @param decM     minutes
     * @param decS     seconds
     * @param distance distance in light years
     * @return the coordinates x,y,z
     */
    public static double[] calculateEquatorialCoordinates(int raH, int raM, double raS, int decDeg, int decM, double decS, double distance) {
        double raInDegrees = 15.0 * (raH + raM / 60.0 + raS / 3600.0);
        double decInDegrees;
        if (decDeg < 0) {
            decInDegrees = decDeg - decM / 60.0 - decS / 3600.0;
        } else {
            decInDegrees = decDeg + decM / 60.0 + decS / 3600.0;
        }

        double ra = Math.toRadians(raInDegrees);
        double dec = Math.toRadians(decInDegrees);

        double x = distance * Math.cos(dec) * Math.cos(ra);
        double y = distance * Math.cos(dec) * Math.sin(ra);
        double z = distance * Math.sin(dec);
        return new double[]{x, y, z};
    }

    public static double[] equatorialToGalactic(double ra, double dec) {
        // Convert input angles from degrees to radians
        ra = Math.toRadians(ra);
        dec = Math.toRadians(dec);

        double sinB = Math.sin(Math.toRadians(DELTA_NGP)) * Math.sin(dec) +
                Math.cos(Math.toRadians(DELTA_NGP)) * Math.cos(dec) *
                        Math.cos(ra - Math.toRadians(ALPHA_NGP));
        double b = Math.asin(sinB);

        double cosBLsinLTheta = Math.cos(dec) * Math.sin(ra - Math.toRadians(ALPHA_NGP));
        double cosBLcosLTheta = Math.sin(dec) - sinB * Math.sin(Math.toRadians(DELTA_NGP));

        double l = THETA + Math.toDegrees(Math.atan2(cosBLsinLTheta, cosBLcosLTheta));

        // Adjust l to be in the range [0°, 360°]
        if (l < 0) {
            l += 360;
        }

        return new double[]{l, Math.toDegrees(b)};
    }

    /**
     * convert to galactic coordinates from equatorial
     *
     * @param ra       the right ascension
     * @param dec      the declination
     * @param distance the distance
     * @return the coordinates x,y,z
     */
    public static double[] equatorialToGalactic(double ra, double dec, double distance) {
        // Convert RA and Dec to Cartesian
        double x = distance * Math.cos(dec) * Math.cos(ra);
        double y = distance * Math.cos(dec) * Math.sin(ra);
        double z = distance * Math.sin(dec);

        // Multiply by transformation matrix
        double xGal = TRANSFORMATION_MATRIX[0][0] * x + TRANSFORMATION_MATRIX[0][1] * y + TRANSFORMATION_MATRIX[0][2] * z;
        double yGal = TRANSFORMATION_MATRIX[1][0] * x + TRANSFORMATION_MATRIX[1][1] * y + TRANSFORMATION_MATRIX[1][2] * z;
        double zGal = TRANSFORMATION_MATRIX[2][0] * x + TRANSFORMATION_MATRIX[2][1] * y + TRANSFORMATION_MATRIX[2][2] * z;

        // Convert to spherical coordinates
        double l = Math.atan2(yGal, xGal);
        double b = Math.asin(zGal / distance);
        double r = Math.sqrt(xGal * xGal + yGal * yGal + zGal * zGal);

        // Convert l from radians to degrees and ensure it's in the range [0, 360]
        l = Math.toDegrees(l);
        if (l < 0) {
            l += 360;
        }

        return new double[]{l, Math.toDegrees(b), r};
    }

    /**
     * convert to equatorial coordinates from galactic
     *
     * @param l        the longitude
     * @param b        the latitude
     * @param distance the distance
     * @return the coordinates x,y,z
     */

    public static double[] galacticToEquatorial(double l, double b, double distance) {
        // Convert l and b to Cartesian
        l = Math.toRadians(l);
        b = Math.toRadians(b);
        double x = distance * Math.cos(b) * Math.cos(l);
        double y = distance * Math.cos(b) * Math.sin(l);
        double z = distance * Math.sin(b);

        // Multiply by inverse transformation matrix
        double xEqu = INVERSE_TRANSFORMATION_MATRIX[0][0] * x + INVERSE_TRANSFORMATION_MATRIX[0][1] * y + INVERSE_TRANSFORMATION_MATRIX[0][2] * z;
        double yEqu = INVERSE_TRANSFORMATION_MATRIX[1][0] * x + INVERSE_TRANSFORMATION_MATRIX[1][1] * y + INVERSE_TRANSFORMATION_MATRIX[1][2] * z;
        double zEqu = INVERSE_TRANSFORMATION_MATRIX[2][0] * x + INVERSE_TRANSFORMATION_MATRIX[2][1] * y + INVERSE_TRANSFORMATION_MATRIX[2][2] * z;

        // Convert to spherical coordinates
        double ra = Math.atan2(yEqu, xEqu);
        double dec = Math.asin(zEqu / distance);
        double r = Math.sqrt(xEqu * xEqu + yEqu * yEqu + zEqu * zEqu);

        // Convert ra from radians to degrees and ensure it's in the range [0, 360]
        ra = Math.toDegrees(ra);
        if (ra < 0) {
            ra += 360;
        }

        return new double[]{ra, Math.toDegrees(dec), r};
    }

    /**
     * convert right ascension to degrees
     *
     * @param hours   hours
     * @param minutes minutes
     * @param seconds seconds
     */
    public static double raToDegrees(int hours, int minutes, double seconds) {
        return 15.0 * (hours + (minutes / 60.0) + (seconds / 3600.0));
    }

    /**
     * convert declination to degrees
     *
     * @param degrees    degrees
     * @param arcminutes arcminutes
     * @param arcseconds arcseconds
     */
    public static double decToDegrees(int degrees, int arcminutes, double arcseconds) {
        if (degrees < 0) {
            return degrees - (arcminutes / 60.0) - (arcseconds / 3600.0);
        }
        return degrees + (arcminutes / 60.0) + (arcseconds / 3600.0);
    }

    public static double parsecToLightYears(double parsecs) {
        return parsecs * 3.262;
    }

    // Function to convert degrees to radians
    public static double degreesToRadians(double degrees) {
        return Math.toRadians(degrees);
    }

    // Function to convert distance from light-years to parsecs
    public static double lightYearsToParsecs(double lightYears) {
        return lightYears / 3.262;
    }


}
