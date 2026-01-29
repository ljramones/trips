package com.teamgannon.trips.particlefields;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generator for planetary rings (Saturn-like).
 *
 * Characteristics:
 * - Extremely thin vertical distribution
 * - Nearly circular orbits (very low eccentricity)
 * - Very low orbital inclination
 * - Dense particle distribution
 * - Fast Keplerian rotation (inner particles faster than outer)
 * - Icy/rocky composition (whites, grays, subtle browns)
 */
public class PlanetaryRingGenerator implements RingElementGenerator {

    @Override
    public List<RingElement> generate(RingConfiguration config, Random random) {
        List<RingElement> elements = new ArrayList<>(config.numElements());

        double radialRange = config.outerRadius() - config.innerRadius();
        double sizeRange = config.maxSize() - config.minSize();

        for (int i = 0; i < config.numElements(); i++) {
            // Radial distribution - slightly favor middle of ring
            double radialFactor = random.nextGaussian() * 0.3 + 0.5;
            radialFactor = Math.max(0, Math.min(1, radialFactor));
            double semiMajorAxis = config.innerRadius() + radialFactor * radialRange;

            // Very low eccentricity for planetary rings
            double eccentricity = random.nextDouble() * config.maxEccentricity() * 0.1;

            // Very low inclination - rings are flat
            double maxIncRad = Math.toRadians(config.maxInclinationDeg());
            double inclination = (random.nextGaussian() * 0.3) * maxIncRad;

            // Random orbital angles
            double argumentOfPeriapsis = random.nextDouble() * 2 * Math.PI;
            double longitudeOfAscendingNode = random.nextDouble() * 2 * Math.PI;
            double initialAngle = random.nextDouble() * 2 * Math.PI;

            // Keplerian speed: inner particles orbit faster
            double speedFactor = Math.sqrt(config.innerRadius() / semiMajorAxis);
            double angularSpeed = config.baseAngularSpeed() * speedFactor;
            // Small random variation
            angularSpeed *= (0.98 + random.nextDouble() * 0.04);

            // Very thin vertical offset
            double heightOffset = (random.nextGaussian() * 0.5) * config.thickness();

            // Particle size - planetary rings have smaller particles
            double size = config.minSize() + random.nextDouble() * sizeRange;

            // Color variation - icy whites to subtle browns
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
        return RingType.PLANETARY_RING;
    }

    @Override
    public String getDescription() {
        return "Planetary ring generator - thin, dense, fast-rotating icy/rocky particles";
    }

    private Color interpolateColor(Color c1, Color c2, double t) {
        return Color.color(
                c1.getRed() + (c2.getRed() - c1.getRed()) * t,
                c1.getGreen() + (c2.getGreen() - c1.getGreen()) * t,
                c1.getBlue() + (c2.getBlue() - c1.getBlue()) * t
        );
    }
}
