package com.teamgannon.trips.experimental.rings;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generator for accretion disks (around compact objects like black holes, neutron stars).
 *
 * Characteristics:
 * - Thin disk structure
 * - Very fast rotation, especially near center
 * - Density increases toward center
 * - Temperature gradient (hot inner edge, cooler outer)
 * - Nearly circular orbits (viscosity circularizes)
 * - Very low inclination (disk is flat)
 * - Color gradient from blue-white (hot) to red (cooler)
 */
public class AccretionDiskGenerator implements RingElementGenerator {

    @Override
    public List<RingElement> generate(RingConfiguration config, Random random) {
        List<RingElement> elements = new ArrayList<>(config.numElements());

        double radialRange = config.outerRadius() - config.innerRadius();
        double sizeRange = config.maxSize() - config.minSize();

        for (int i = 0; i < config.numElements(); i++) {
            // Radial distribution - denser toward center (power law)
            double u = random.nextDouble();
            double radialFactor = Math.pow(u, 0.5); // Square root gives 1/r density profile
            double semiMajorAxis = config.innerRadius() + radialFactor * radialRange;

            // Very low eccentricity - viscosity circularizes orbits
            double eccentricity = random.nextDouble() * config.maxEccentricity() * 0.05;

            // Very flat disk
            double maxIncRad = Math.toRadians(config.maxInclinationDeg());
            double inclination = random.nextGaussian() * 0.1 * maxIncRad;

            // Random orbital angles
            double argumentOfPeriapsis = random.nextDouble() * 2 * Math.PI;
            double longitudeOfAscendingNode = random.nextDouble() * 2 * Math.PI;
            double initialAngle = random.nextDouble() * 2 * Math.PI;

            // Very fast Keplerian speed, especially near center
            double speedFactor = Math.sqrt(config.innerRadius() / semiMajorAxis);
            // Accretion disks rotate faster than normal Keplerian due to pressure
            double angularSpeed = config.baseAngularSpeed() * speedFactor * 2.0;
            angularSpeed *= (0.95 + random.nextDouble() * 0.1);

            // Very thin vertical distribution
            double heightOffset = random.nextGaussian() * 0.2 * config.thickness();
            // Disk flares slightly at outer edge
            double flare = (semiMajorAxis - config.innerRadius()) / radialRange;
            heightOffset *= (1 + flare * 0.5);

            // Smaller particles (gas/plasma)
            double size = config.minSize() + random.nextDouble() * sizeRange * 0.6;

            // Temperature-based color gradient
            // Inner = hot (blue-white), outer = cooler (red-orange)
            double temperatureFactor = 1.0 - radialFactor; // 1 at inner, 0 at outer
            Color color = getTemperatureColor(temperatureFactor, config.primaryColor(), config.secondaryColor());

            elements.add(new RingElement(
                    semiMajorAxis, eccentricity, inclination,
                    argumentOfPeriapsis, longitudeOfAscendingNode,
                    initialAngle, angularSpeed, size, heightOffset, color
            ));
        }

        return elements;
    }

    @Override
    public RingType getRingType() {
        return RingType.ACCRETION_DISK;
    }

    @Override
    public String getDescription() {
        return "Accretion disk generator - thin, fast, temperature gradient toward center";
    }

    /**
     * Returns a color based on temperature (black body approximation).
     * High temperature = blue-white, low temperature = red-orange.
     */
    private Color getTemperatureColor(double temperatureFactor, Color hotColor, Color coolColor) {
        // Interpolate with bias toward hot colors at high temperature
        double t = Math.pow(temperatureFactor, 0.7);

        // Blend between cool and hot colors
        double r = coolColor.getRed() + (hotColor.getRed() - coolColor.getRed()) * t;
        double g = coolColor.getGreen() + (hotColor.getGreen() - coolColor.getGreen()) * t;
        double b = coolColor.getBlue() + (hotColor.getBlue() - coolColor.getBlue()) * t;

        // Boost brightness for hot regions
        double brightness = 0.7 + 0.3 * t;
        r = Math.min(1.0, r * brightness);
        g = Math.min(1.0, g * brightness);
        b = Math.min(1.0, b * brightness);

        return Color.color(r, g, b);
    }
}
