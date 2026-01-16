package com.teamgannon.trips.nightsky.math;

/**
 * Immutable rotation model for a planet.
 * Longitude is positive east.
 */
public final class PlanetRotationModel {

    private final double obliquityDeg;
    private final double rotationPeriodSeconds;
    private final double primeMeridianAtEpochDeg;

    /**
     * @param obliquityDeg              axial tilt in degrees
     * @param rotationPeriodSeconds     rotation period in seconds
     * @param primeMeridianAtEpochDeg   prime meridian longitude at epoch (deg, east-positive)
     */
    public PlanetRotationModel(double obliquityDeg,
                               double rotationPeriodSeconds,
                               double primeMeridianAtEpochDeg) {
        this.obliquityDeg = obliquityDeg;
        this.rotationPeriodSeconds = rotationPeriodSeconds;
        this.primeMeridianAtEpochDeg = primeMeridianAtEpochDeg;
    }

    public double getObliquityDeg() {
        return obliquityDeg;
    }

    public double getRotationPeriodSeconds() {
        return rotationPeriodSeconds;
    }

    public double getPrimeMeridianAtEpochDeg() {
        return primeMeridianAtEpochDeg;
    }
}
