package com.teamgannon.trips.nightsky.model;

import com.teamgannon.trips.jpa.model.StarObject;

/**
 * Result of a star visibility calculation, containing position in horizon coordinates.
 */
public final class VisibleStarResult {

    private final StarObject star;
    private final double altitudeDeg;
    private final double azimuthDeg;
    private final double magnitude;
    private final double distanceLy;

    public VisibleStarResult(StarObject star,
                             double altitudeDeg,
                             double azimuthDeg,
                             double magnitude,
                             double distanceLy) {
        this.star = star;
        this.altitudeDeg = altitudeDeg;
        this.azimuthDeg = azimuthDeg;
        this.magnitude = magnitude;
        this.distanceLy = distanceLy;
    }

    public StarObject getStar() {
        return star;
    }

    public double getAltitudeDeg() {
        return altitudeDeg;
    }

    public double getAzimuthDeg() {
        return azimuthDeg;
    }

    public double getMagnitude() {
        return magnitude;
    }

    public double getDistanceLy() {
        return distanceLy;
    }
}
