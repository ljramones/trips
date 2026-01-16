package com.teamgannon.trips.nightsky.math;

/**
 * Immutable observer location on a spherical planet.
 * Longitude is positive east.
 */
public final class ObserverLocation {

    private final double latitudeDeg;
    private final double longitudeDeg;

    public ObserverLocation(double latitudeDeg, double longitudeDeg) {
        this.latitudeDeg = latitudeDeg;
        this.longitudeDeg = longitudeDeg;
    }

    public double getLatitudeDeg() {
        return latitudeDeg;
    }

    public double getLongitudeDeg() {
        return longitudeDeg;
    }
}
