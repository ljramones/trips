package com.teamgannon.trips.planetarymodelling.planetgen.planet.layer;


import com.teamgannon.trips.planetarymodelling.planetgen.planet.Planet;
import com.teamgannon.trips.planetarymodelling.planetgen.planet.PlanetGenerationContext;
import com.teamgannon.trips.planetarymodelling.planetgen.value.SphereValue;

public class AtmosphericPressureLayer implements Layer {

    private final SphereValue valueFunction;

    public AtmosphericPressureLayer(SphereValue valueFunction) {
        this.valueFunction = valueFunction;
    }

    @Override
    public void calculatePlanetPoint(PlanetPoint planetPoint, Planet planet, double latitude, double longitude, PlanetGenerationContext context) {
        double pressure = valueFunction.sphereValue(latitude, longitude, context);
        //pressure = MathUtil.smoothstep(0, 1, Math.abs(pressure)) * Math.signum(pressure);
        planetPoint.atmospherePressure = 1.0 + pressure * 0.05;
    }

}
