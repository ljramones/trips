package com.teamgannon.trips.astrogation.util;

public class CoordinatesTransform {

    public static double[] convertRaDecToXyz(double raDegrees, double decDegrees, double distance) {
        // Convert RA and Dec to radians
        double raRadians = convertDegreesToRadians(raDegrees);
        double decRadians = convertDegreesToRadians(decDegrees);

        // Calculate the Cartesian coordinates
        double x = distance * Math.cos(decRadians) * Math.cos(raRadians);
        double y = distance * Math.cos(decRadians) * Math.sin(raRadians);
        double z = distance * Math.sin(decRadians);

        return new double[]{x, y, z};
    }

    private static double convertDegreesToRadians(double degrees) {
        return degrees * Math.PI / 180;
    }

    /**
     * convert equatorial coordinates to galactic coordinates
     *
     * @param raDegrees  ra in degrees
     * @param decDegrees dec in degrees
     * @return galactic coordinates
     */
    public static double[] convertEquatorialToGalactic(double raDegrees, double decDegrees) {
        double lNGP = Math.toRadians(122.93191857); // Galactic longitude of North Galactic Pole (degrees)
        double bNGP = Math.toRadians(27.12835439); // Galactic latitude of North Galactic Pole (degrees)

        double raRadians = Math.toRadians(raDegrees);
        double decRadians = Math.toRadians(decDegrees);
        double a = raRadians - lNGP;

        double sinB = Math.sin(decRadians) * Math.cos(bNGP) + Math.cos(decRadians) * Math.sin(a) * Math.sin(bNGP) + Math.cos(decRadians) * Math.cos(a) * Math.cos(bNGP);
        double b = Math.asin(sinB);

        double cosLcosB = Math.cos(decRadians) * Math.cos(a);
        double sinLcosB = Math.sin(decRadians) * Math.sin(bNGP) - Math.cos(decRadians) * Math.sin(a) * Math.cos(bNGP) + Math.cos(decRadians) * Math.cos(a) * Math.sin(bNGP);

        double l = Math.atan2(sinLcosB, cosLcosB) + lNGP;

        // Normalize the longitude to [0, 2 * PI]
        if (l < 0) {
            l += 2 * Math.PI;
        }

        return new double[]{Math.toDegrees(l), Math.toDegrees(b)};
    }

    /**
     * convert galactic coordinates to equatorial coordinates
     *
     * @param lDegrees galactic longitude in degrees
     * @param bDegrees galactic latitude in degrees
     * @return equatorial coordinates
     */
    public static double[] convertGalacticToEquatorial(double lDegrees, double bDegrees) {
        double lNGP = Math.toRadians(122.93191857); // Galactic longitude of North Galactic Pole (degrees)
        double bNGP = Math.toRadians(27.12835439); // Galactic latitude of North Galactic Pole (degrees)

        double lRadians = Math.toRadians(lDegrees);
        double bRadians = Math.toRadians(bDegrees);

        double sinDec = Math.sin(bRadians) * Math.cos(bNGP) - Math.cos(bRadians) * Math.sin(lRadians) * Math.sin(bNGP) + Math.cos(bRadians) * Math.cos(lRadians) * Math.sin(bNGP);
        double dec = Math.asin(sinDec);

        double cosAcosDec = Math.cos(bRadians) * Math.cos(lRadians);
        double sinAcosDec = Math.sin(bRadians) * Math.sin(bNGP) + Math.cos(bRadians) * Math.sin(lRadians) * Math.cos(bNGP) - Math.cos(bRadians) * Math.cos(lRadians) * Math.sin(bNGP);

        double a = Math.atan2(sinAcosDec, cosAcosDec);
        double ra = a + lNGP;

        // Normalize the right ascension to [0, 2 * PI]
        if (ra < 0) {
            ra += 2 * Math.PI;
        }

        return new double[]{Math.toDegrees(ra), Math.toDegrees(dec)};
    }

    public static double[] convertGalacticToXyz(double lDegrees, double bDegrees, double distance) {
        // Convert longitude and latitude to radians
        double lRadians = Math.toRadians(lDegrees);
        double bRadians = Math.toRadians(bDegrees);

        // Calculate the Cartesian coordinates
        double x = distance * Math.cos(bRadians) * Math.cos(lRadians);
        double y = distance * Math.cos(bRadians) * Math.sin(lRadians);
        double z = distance * Math.sin(bRadians);

        return new double[]{x, y, z};
    }

    /**
     * convert xyz to galactic coordinates
     *
     * @param x x
     * @param y y
     * @param z z
     * @return galactic coordinates
     */
    public static double[] convertXyzToGalactic(double x, double y, double z) {
        // Calculate the galactic longitude
        double lRadians = Math.atan2(y, x);

        // Calculate the galactic latitude
        double bRadians = Math.atan2(z, Math.sqrt(x * x + y * y));

        // Convert to degrees
        double lDegrees = Math.toDegrees(lRadians);
        double bDegrees = Math.toDegrees(bRadians);

        // Normalize the longitude to [0, 360]
        if (lDegrees < 0) {
            lDegrees += 360;
        }

        return new double[]{lDegrees, bDegrees};
    }

    /**
     * convert  xyz to equatorial coordinates
     *
     * @param x x
     * @param y y
     * @param z z
     * @return equatorial coordinates
     */
    public static double[] convertXyzToEquatorial(double x, double y, double z) {
        double raRadians = Math.atan2(y, x);
        double decRadians = Math.atan2(z, Math.sqrt(x * x + y * y));

        double raDegrees = Math.toDegrees(raRadians);
        double decDegrees = Math.toDegrees(decRadians);

        // Normalize the right ascension to [0, 360]
        if (raDegrees < 0) {
            raDegrees += 360;
        }

        return new double[]{raDegrees, decDegrees};
    }

/**
     * convert equatorial coordinates to xyz
     *
     * @param raDegrees  ra in degrees
     * @param decDegrees dec in degrees
     * @param distance   distance
     * @return xyz
     */
    public static double[] convertEquatorialToXyz(double raDegrees, double decDegrees, double distance) {
        double raRadians = Math.toRadians(raDegrees);
        double decRadians = Math.toRadians(decDegrees);

        double x = distance * Math.cos(decRadians) * Math.cos(raRadians);
        double y = distance * Math.cos(decRadians) * Math.sin(raRadians);
        double z = distance * Math.sin(decRadians);

        return new double[] {x, y, z};
    }



}
