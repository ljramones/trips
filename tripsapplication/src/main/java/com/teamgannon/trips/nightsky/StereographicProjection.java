package com.teamgannon.trips.nightsky;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StereographicProjection {

    public static double[] project(double altitude, double azimuth) {
        double theta = Math.toRadians(90 - altitude);  // Convert altitude to zenith distance

        double x = (2 * Math.tan(theta / 2) * Math.cos(Math.toRadians(azimuth))) / (1 + Math.sin(theta));
        double y = (2 * Math.tan(theta / 2) * Math.sin(Math.toRadians(azimuth))) / (1 + Math.sin(theta));

        return new double[]{x, y};
    }

    public static void main(String[] args) {
        double altitude = 45;  // Sample altitude in degrees
        double azimuth = 60;   // Sample azimuth in degrees

        double[] position = project(altitude, azimuth);
        System.out.println("Projected x: " + position[0]);
        System.out.println("Projected y: " + position[1]);
    }
}

