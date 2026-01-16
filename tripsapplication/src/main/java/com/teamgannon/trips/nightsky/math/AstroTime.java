package com.teamgannon.trips.nightsky.math;

import java.time.Instant;

/**
 * Utility class for astronomical time calculations.
 */
public final class AstroTime {

    private static final double JD_UNIX_EPOCH = 2440587.5;

    private AstroTime() {
    }

    public static double julianDate(Instant t) {
        double millis = t.toEpochMilli();
        return JD_UNIX_EPOCH + millis / 86_400_000.0;
    }

    public static double gmstRadians(double julianDate) {
        double t = (julianDate - 2451545.0) / 36525.0;
        double gmstDeg = 280.46061837
                + 360.98564736629 * (julianDate - 2451545.0)
                + 0.000387933 * t * t
                - (t * t * t) / 38710000.0;
        return normalizeRadians(Math.toRadians(gmstDeg));
    }

    public static double lstRadians(double julianDate, double longitudeDeg) {
        double lst = gmstRadians(julianDate) + Math.toRadians(longitudeDeg);
        return normalizeRadians(lst);
    }

    public static double normalizeRadians(double angle) {
        double twoPi = Math.PI * 2.0;
        double result = angle % twoPi;
        return result < 0 ? result + twoPi : result;
    }
}
