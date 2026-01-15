package com.teamgannon.trips.solarsystem.nightsky;

public final class AltAzResult {

    private final StarCatalogEntry star;
    private final double altitudeDeg;
    private final double azimuthDeg;

    public AltAzResult(StarCatalogEntry star, double altitudeDeg, double azimuthDeg) {
        this.star = star;
        this.altitudeDeg = altitudeDeg;
        this.azimuthDeg = azimuthDeg;
    }

    public StarCatalogEntry getStar() {
        return star;
    }

    public double getAltitudeDeg() {
        return altitudeDeg;
    }

    public double getAzimuthDeg() {
        return azimuthDeg;
    }
}
