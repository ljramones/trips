package com.teamgannon.trips.nightsky;

import lombok.extern.slf4j.Slf4j;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Slf4j
public class SiderealTime {

    public static double calculateLST(ZonedDateTime dateTime, double longitude) {
        double gst = calculateGST(dateTime);
        double lst = gst + longitude / 15.0;
        return (lst + 24) % 24; // Ensure LST is between 0 and 24
    }

    private static double calculateGST(ZonedDateTime dateTime) {
        // This is a simplified formula for GST.
        // In reality, more accurate methods involve longer series expansions.
        long jd = toJulianDate(dateTime);
        double T = (jd - 2451545.0) / 36525.0;
        double gst = 280.46061837 + 360.98564736629 * (jd - 2451545) + T * T * (0.000387933 - T / 38710000);
        return (gst / 15.0) % 24; // Convert to hours and ensure it's between 0 and 24
    }

    private static long toJulianDate(ZonedDateTime dateTime) {
        ZonedDateTime utcDateTime = dateTime.withZoneSameInstant(ZoneOffset.UTC);
        int year = utcDateTime.getYear();
        int month = utcDateTime.getMonthValue();
        int day = utcDateTime.getDayOfMonth();
        if (month <= 2) {
            year--;
            month += 12;
        }
        long A = year / 100;
        long B = 2 - A + A / 4;
        return (long) ((long) (365.25 * (year + 4716)) + (long) (30.6001 * (month + 1)) + day + B - 1524.5);
    }

    public static void main(String[] args) {
        ZonedDateTime dateTime = ZonedDateTime.now(ZoneOffset.UTC);
        double longitude = -74.0060; // New York City
        double lst = calculateLST(dateTime, longitude);
        System.out.println("Local Sidereal Time (LST) in hours: " + lst);
    }
}
