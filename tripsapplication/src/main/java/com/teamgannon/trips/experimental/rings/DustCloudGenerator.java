package com.teamgannon.trips.experimental.rings;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generator for dust clouds / nebulae.
 *
 * Characteristics:
 * - Three-dimensional distribution (spherical/ellipsoidal, not a flat ring)
 * - Very diffuse
 * - Slow, turbulent motion rather than organized orbital motion
 * - Very small particles (dust/gas)
 * - Colorful (emission/reflection nebulae) or dark
 * - No clear orbital plane
 */
public class DustCloudGenerator implements RingElementGenerator {

    @Override
    public List<RingElement> generate(RingConfiguration config, Random random) {
        List<RingElement> elements = new ArrayList<>(config.numElements());

        double radialRange = config.outerRadius() - config.innerRadius();
        double sizeRange = config.maxSize() - config.minSize();

        for (int i = 0; i < config.numElements(); i++) {
            // 3D spherical/ellipsoidal distribution
            // Use spherical coordinates with Gaussian falloff
            double r = config.innerRadius() + Math.abs(random.nextGaussian() * 0.4) * radialRange;
            r = Math.min(r, config.outerRadius());

            // Random direction in 3D (uniform on sphere)
            double theta = random.nextDouble() * 2 * Math.PI;  // azimuth
            double phi = Math.acos(2 * random.nextDouble() - 1); // polar (uniform on sphere)

            // Convert to pseudo-orbital elements
            // For a dust cloud, we treat inclination as the polar angle
            double inclination = phi - Math.PI / 2; // Range: -PI/2 to PI/2
            double longitudeOfAscendingNode = theta;

            // Very low eccentricity - particles drift slowly
            double eccentricity = random.nextDouble() * 0.02;

            // No preferred argument of periapsis
            double argumentOfPeriapsis = random.nextDouble() * 2 * Math.PI;
            double initialAngle = random.nextDouble() * 2 * Math.PI;

            // Very slow, almost random motion (turbulent, not orbital)
            double angularSpeed = config.baseAngularSpeed() * 0.1 * (0.5 + random.nextDouble());
            // Random direction of motion
            if (random.nextBoolean()) {
                angularSpeed = -angularSpeed;
            }

            // 3D height offset (for additional vertical spread)
            double heightOffset = random.nextGaussian() * config.thickness();

            // Very small dust particles
            double size = config.minSize() + random.nextDouble() * sizeRange * 0.5;

            // Color with variation - nebulae can be colorful
            double colorFactor = random.nextDouble();
            Color color = interpolateColor(config.primaryColor(), config.secondaryColor(), colorFactor);
            // Add some opacity variation for nebula effect
            color = Color.color(color.getRed(), color.getGreen(), color.getBlue(),
                    0.5 + random.nextDouble() * 0.5);

            elements.add(new RingElement(
                    r, eccentricity, inclination,
                    argumentOfPeriapsis, longitudeOfAscendingNode,
                    initialAngle, angularSpeed, size, heightOffset, color
            ));
        }

        return elements;
    }

    @Override
    public RingType getRingType() {
        return RingType.DUST_CLOUD;
    }

    @Override
    public String getDescription() {
        return "Dust cloud generator - 3D diffuse distribution, slow turbulent motion";
    }

    private Color interpolateColor(Color c1, Color c2, double t) {
        return Color.color(
                c1.getRed() + (c2.getRed() - c1.getRed()) * t,
                c1.getGreen() + (c2.getGreen() - c1.getGreen()) * t,
                c1.getBlue() + (c2.getBlue() - c1.getBlue()) * t
        );
    }
}
