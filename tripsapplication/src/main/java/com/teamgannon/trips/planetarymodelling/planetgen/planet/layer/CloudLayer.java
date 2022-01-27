package com.teamgannon.trips.planetarymodelling.planetgen.planet.layer;


import com.teamgannon.trips.planetarymodelling.planetgen.math.MathUtil;
import com.teamgannon.trips.planetarymodelling.planetgen.planet.Planet;
import com.teamgannon.trips.planetarymodelling.planetgen.planet.PlanetGenerationContext;
import com.teamgannon.trips.planetarymodelling.planetgen.value.SphereValue;

public class CloudLayer implements Layer {

    private final SphereValue cloudValueFunction;
    private final SphereValue highPressureValueFunction;

    public CloudLayer(SphereValue cloudValueFunction, SphereValue highPressureValueFunction) {
        this.cloudValueFunction = cloudValueFunction;
        this.highPressureValueFunction = highPressureValueFunction;
    }

    @Override
    public void calculatePlanetPoint(PlanetPoint planetPoint, Planet planet, double latitude, double longitude, PlanetGenerationContext context) {
        double cloud = cloudValueFunction.sphereValue(latitude, longitude, 1.0, 0.0001);

        double oceanPart = planet.planetData.getOceanPart();
        double minEdge = 0.95 - oceanPart * 0.5;
        double maxEdge = 1.0 - oceanPart * 0.1;

        if (planetPoint.atmospherePressure > 1.0) {
            double pressureLevel = (planetPoint.atmospherePressure - 1.0) / 0.05;
            cloud = MathUtil.mix(cloud, cloud * highPressureValueFunction.sphereValue(latitude, longitude, 1.0, 0.0001), pressureLevel);
        }

        cloud = MathUtil.smoothstep(minEdge, maxEdge, cloud);
        cloud = Math.sqrt(cloud);

        double cloudMinHeight = planet.planetData.atmosphereHeight * 0.5 * MathUtil.exaggerate(planetPoint.atmospherePressure, 2);
        if (cloudMinHeight < planetPoint.height) {
            double heightCorrection = Math.min(1.0, (planetPoint.height - cloudMinHeight) / (planet.planetData.atmosphereHeight * 0.5));
            cloud *= 1.0 - heightCorrection;
            cloudMinHeight = planetPoint.height;
        }

        double cloudThickness = cloud * 200 * MathUtil.exaggerate(planetPoint.atmospherePressure, 10); // TODO cloud thickness

        planetPoint.cloud = cloud;
        planetPoint.cloudMinHeight = cloudMinHeight;
        planetPoint.cloudMaxHeight = cloudMinHeight + cloudThickness;
    }

}
