package com.teamgannon.trips.nightsky.math;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Local horizon basis expressed in inertial coordinates.
 */
public final class HorizonBasis {

    private final Vector3D eastUnit;
    private final Vector3D northUnit;
    private final Vector3D upUnit;

    public HorizonBasis(Vector3D eastUnit, Vector3D northUnit, Vector3D upUnit) {
        this.eastUnit = eastUnit;
        this.northUnit = northUnit;
        this.upUnit = upUnit;
    }

    public Vector3D getEastUnit() {
        return eastUnit;
    }

    public Vector3D getNorthUnit() {
        return northUnit;
    }

    public Vector3D getUpUnit() {
        return upUnit;
    }
}
