package com.teamgannon.trips.nightsky;


import lombok.extern.slf4j.Slf4j;

/**
 * To compute the altitude (\( \alpha \)) and azimuth (\( A \)) of a star given its Right Ascension (RA), declination (\( \delta \)), Local Sidereal Time (LST), and the observer's latitude (\( \phi \)), you can use the following formulas:
 * <p>
 * 1. Compute the Hour Angle (HA) of the star:
 * \[ \text{HA} = \text{LST} - \text{RA} \]
 * If HA is negative, add 24 hours to make it positive.
 * <p>
 * 2. Convert the observer's latitude, the star's declination, and the hour angle from degrees to radians.
 * <p>
 * 3. Compute the altitude:
 * \[ \sin(\alpha) = \sin(\phi) \times \sin(\delta) + \cos(\phi) \times \cos(\delta) \times \cos(\text{HA}) \]
 * The altitude is the arcsin of the result.
 * <p>
 * 4. Compute the azimuth:
 * \[ \sin(A) = -\cos(\delta) \times \sin(\text{HA}) \]
 * \[ \cos(A) = \frac{\cos(\alpha) - \sin(\phi) \times \sin(\delta)}{\cos(\phi) \times \cos(\delta)} \]
 * The azimuth \( A \) is the arctan2 of the result (which handles the quadrant ambiguity).
 * <p>
 * This function computes the altitude and azimuth of a star based on its RA, declination, LST, and the observer's latitude. If the altitude is positive, the star is above the horizon. The azimuth provides the direction of the star from the observer's location.
 */
@Slf4j
public class StarPosition {

    public static double[] computeAltitudeAzimuth(double ra, double declination, double lst, double latitude) {
        double ha = lst - ra;

        // Convert to range [0, 24]
        if (ha < 0) ha += 24;

        // Convert hour angle from hours to degrees
        ha *= 15;

        // Convert angles to radians for trigonometric calculations
        double latRad = Math.toRadians(latitude);
        double decRad = Math.toRadians(declination);
        double haRad = Math.toRadians(ha);

        // Calculate altitude
        double sinAltitude = Math.sin(latRad) * Math.sin(decRad) + Math.cos(latRad) * Math.cos(decRad) * Math.cos(haRad);
        double altitude = Math.toDegrees(Math.asin(sinAltitude));

        // Calculate azimuth
        double sinAzimuth = -Math.cos(decRad) * Math.sin(haRad);
        double cosAzimuth = (Math.sin(altitude) - Math.sin(latRad) * Math.sin(decRad)) / (Math.cos(latRad) * Math.cos(decRad));
        double azimuth = Math.toDegrees(Math.atan2(sinAzimuth, cosAzimuth));
        if (azimuth < 0) azimuth += 360;  // Ensure azimuth is between 0 and 360 degrees

        return new double[]{altitude, azimuth};
    }

    public static void main(String[] args) {
        double ra = 5.5;  // Sample RA in hours
        double declination = 23.44;  // Sample declination in degrees
        double lst = 6.5;  // Sample LST in hours
        double latitude = 40.7128;  // Sample latitude for New York City

        double[] position = computeAltitudeAzimuth(ra, declination, lst, latitude);
        System.out.println("Altitude: " + position[0] + " degrees");
        System.out.println("Azimuth: " + position[1] + " degrees");
    }
}
