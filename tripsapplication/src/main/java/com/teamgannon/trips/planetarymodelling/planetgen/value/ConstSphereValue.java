package com.teamgannon.trips.planetarymodelling.planetgen.value;

public class ConstSphereValue implements SphereValue {

    private double value;

    public ConstSphereValue(double value) {
        this.value = value;
    }

    @Override
    public double sphereValue(double latitude, double longitude, double radius, double accuracy) {
        return value;
    }

}
