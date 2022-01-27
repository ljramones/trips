package com.teamgannon.trips.planetarymodelling.planetgen.planet.layer;


import com.teamgannon.trips.planetarymodelling.planetgen.math.Color;
import com.teamgannon.trips.planetarymodelling.planetgen.math.MathUtil;
import com.teamgannon.trips.planetarymodelling.planetgen.planet.Planet;
import com.teamgannon.trips.planetarymodelling.planetgen.planet.PlanetGenerationContext;
import com.teamgannon.trips.planetarymodelling.util.Units;

public class SnowLayer implements Layer {

    private static final Color SNOW_SPECULAR_COLOR = Color.rgb(0.6, 0.6, 0.6);

    private final Color snowColor;

    private double temperatureOptimum = Units.celsiusToKelvin(-20);
    private double temperatureDeviation = 20;

    private double maxSnowHeight = 2.0; // m

    public SnowLayer(Color snowColor) {
        this.snowColor = snowColor;
    }

    @Override
    public void calculatePlanetPoint(PlanetPoint planetPoint, Planet planet, double latitude, double longitude, PlanetGenerationContext context) {
        if (!planetPoint.isWater) {
            double snow = 1.0 - MathUtil.smoothstep(temperatureOptimum, temperatureOptimum + temperatureDeviation, planetPoint.temperature);

            planetPoint.snowHeight = snow * maxSnowHeight;
            planetPoint.height += planetPoint.snowHeight;
            planetPoint.color = planetPoint.color.interpolate(snowColor, snow * 0.9);
            planetPoint.specularColor = planetPoint.specularColor.interpolate(SNOW_SPECULAR_COLOR, snow * 0.9);
        }
    }
}
