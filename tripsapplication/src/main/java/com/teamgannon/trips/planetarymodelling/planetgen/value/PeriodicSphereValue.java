package com.teamgannon.trips.planetarymodelling.planetgen.value;


import com.teamgannon.trips.planetarymodelling.planetgen.math.MathUtil;
import com.teamgannon.trips.planetarymodelling.planetgen.planet.Planet;

public class PeriodicSphereValue implements SphereValue {

    private final SphereValue valueFunction;

    public PeriodicSphereValue(SphereValue valueFunction) {
        this.valueFunction = valueFunction;
    }

    @Override
    public double sphereValue(double latitude, double longitude, double radius, double accuracy) {
        double value1 = valueFunction.sphereValue(latitude, longitude, radius, accuracy);
        double value2 = valueFunction.sphereValue(latitude, longitude - Planet.RANGE_LONGITUDE, radius, accuracy);
        double longitudeWeight = (longitude - Planet.MIN_LONGITUDE) / Planet.RANGE_LONGITUDE;
        return MathUtil.mix(value1, value2, longitudeWeight);
    }

}
