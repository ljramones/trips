package com.teamgannon.trips.planetarymodelling.planetgen.value;


import com.teamgannon.trips.planetarymodelling.planetgen.planet.PlanetGenerationContext;

public interface PolarValue {
    double polarValue(double angle, double radius, double accuracy);

    default double polarValue(double angle, double radius, PlanetGenerationContext context) {
        return polarValue(angle, radius, context.accuracy);
    }
}
