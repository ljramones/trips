package com.teamgannon.trips.solarsystem.nightsky;

public final class EquatorialCoordinates {

    private final double raHours;
    private final double decDeg;

    public EquatorialCoordinates(double raHours, double decDeg) {
        this.raHours = raHours;
        this.decDeg = decDeg;
    }

    public double getRaHours() {
        return raHours;
    }

    public double getDecDeg() {
        return decDeg;
    }

    public double getRaRadians() {
        return Math.toRadians(raHours * 15.0);
    }

    public double getDecRadians() {
        return Math.toRadians(decDeg);
    }
}
