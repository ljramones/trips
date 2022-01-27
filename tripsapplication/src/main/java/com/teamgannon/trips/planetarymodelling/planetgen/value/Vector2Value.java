package com.teamgannon.trips.planetarymodelling.planetgen.value;


import com.teamgannon.trips.planetarymodelling.planetgen.math.Vector2;
import com.teamgannon.trips.planetarymodelling.planetgen.planet.PlanetGenerationContext;

public interface Vector2Value {

    double vector2Value(Vector2 value, double accuracy);

    default double vector2Value(Vector2 value, PlanetGenerationContext context) {
        return vector2Value(value, context.accuracy);
    }
}
