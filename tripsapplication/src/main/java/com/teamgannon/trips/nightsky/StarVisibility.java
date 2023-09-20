package com.teamgannon.trips.nightsky;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StarVisibility {

    public static boolean isStarVisible(double ra, double declination, double lst, double latitude) {
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

        return altitude > 0;
    }

    public static void main(String[] args) {
        double ra = 5.5;  // Sample RA in hours
        double declination = 23.44;  // Sample declination in degrees
        double lst = 6.5;  // Sample LST in hours
        double latitude = 40.7128;  // Sample latitude for New York City

        boolean visible = isStarVisible(ra, declination, lst, latitude);
        System.out.println("Star is visible: " + visible);
    }
}
