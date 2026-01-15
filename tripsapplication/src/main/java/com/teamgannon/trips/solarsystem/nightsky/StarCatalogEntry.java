package com.teamgannon.trips.solarsystem.nightsky;

/**
 * Star catalog entry with RA in hours and Dec in degrees.
 */
public final class StarCatalogEntry {

    private final String name;
    private final double raHours;
    private final double decDeg;
    private final double magnitude;
    private final String constellation;

    public StarCatalogEntry(String name, double raHours, double decDeg, double magnitude, String constellation) {
        this.name = name;
        this.raHours = raHours;
        this.decDeg = decDeg;
        this.magnitude = magnitude;
        this.constellation = constellation;
    }

    public String getName() {
        return name;
    }

    public double getRaHours() {
        return raHours;
    }

    public double getDecDeg() {
        return decDeg;
    }

    public double getMagnitude() {
        return magnitude;
    }

    public String getConstellation() {
        return constellation;
    }
}
