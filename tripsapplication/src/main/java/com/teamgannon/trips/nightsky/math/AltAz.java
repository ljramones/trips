package com.teamgannon.trips.nightsky.math;

/**
 * Altitude-azimuth coordinates representing a position in the sky.
 */
public final class AltAz {

    private final double altitudeDeg;
    private final double azimuthDeg;

    public AltAz(double altitudeDeg, double azimuthDeg) {
        this.altitudeDeg = altitudeDeg;
        this.azimuthDeg = azimuthDeg;
    }

    public double getAltitudeDeg() {
        return altitudeDeg;
    }

    public double getAzimuthDeg() {
        return azimuthDeg;
    }
}
