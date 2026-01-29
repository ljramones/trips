package com.teamgannon.trips.particlefields;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generator for asteroid belts (Main belt-like).
 *
 * Characteristics:
 * - Thick vertical distribution
 * - Moderate eccentricity (elliptical orbits)
 * - Significant orbital inclination variation
 * - Sparse distribution (large gaps between bodies)
 * - Large rocky bodies
 * - Slower rotation than planetary rings
 * - Rocky composition (grays, browns, dark colors)
 */
public class AsteroidBeltGenerator implements RingElementGenerator {

    @Override
    public List<RingElement> generate(RingConfiguration config, Random random) {
        List<RingElement> elements = new ArrayList<>(config.numElements());

        double radialRange = config.outerRadius() - config.innerRadius();
        double sizeRange = config.maxSize() - config.minSize();

        for (int i = 0; i < config.numElements(); i++) {
            // Radial distribution - more uniform than planetary rings
            double semiMajorAxis = config.innerRadius() + random.nextDouble() * radialRange;

            // Moderate eccentricity - elliptical orbits
            double eccentricity = random.nextDouble() * config.maxEccentricity();

            // Significant inclination variation
            double maxIncRad = Math.toRadians(config.maxInclinationDeg());
            double inclination = (random.nextDouble() - 0.5) * 2 * maxIncRad;

            // Random orbital angles
            double argumentOfPeriapsis = random.nextDouble() * 2 * Math.PI;
            double longitudeOfAscendingNode = random.nextDouble() * 2 * Math.PI;
            double initialAngle = random.nextDouble() * 2 * Math.PI;

            // Keplerian speed with more variation
            double speedFactor = Math.sqrt(config.innerRadius() / semiMajorAxis);
            double angularSpeed = config.baseAngularSpeed() * speedFactor;
            // More random variation than planetary rings
            angularSpeed *= (0.8 + random.nextDouble() * 0.4);

            // Thick vertical distribution
            double heightOffset = (random.nextDouble() - 0.5) * config.thickness();

            // Larger particles with more size variation
            double size = config.minSize() + random.nextDouble() * sizeRange;
            // Bias toward medium sizes (fewer very large or very small)
            if (random.nextDouble() < 0.7) {
                size = config.minSize() + (0.3 + random.nextDouble() * 0.4) * sizeRange;
            }

            // Color variation - rocky grays and browns
            Color color = interpolateColor(config.primaryColor(), config.secondaryColor(),
                    random.nextDouble());

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
        return RingType.ASTEROID_BELT;
    }

    @Override
    public String getDescription() {
        return "Asteroid belt generator - thick, sparse, eccentric rocky bodies";
    }

    private Color interpolateColor(Color c1, Color c2, double t) {
        return Color.color(
                c1.getRed() + (c2.getRed() - c1.getRed()) * t,
                c1.getGreen() + (c2.getGreen() - c1.getGreen()) * t,
                c1.getBlue() + (c2.getBlue() - c1.getBlue()) * t
        );
    }
}
