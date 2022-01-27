package com.teamgannon.trips.planetarymodelling.planetgen.planet.layer;


import com.teamgannon.trips.planetarymodelling.planetgen.planet.Planet;
import com.teamgannon.trips.planetarymodelling.planetgen.planet.PlanetGenerationContext;

public interface Layer {

    void calculatePlanetPoint(PlanetPoint planetPoint, Planet planet, double latitude, double longitude, PlanetGenerationContext context);
}
