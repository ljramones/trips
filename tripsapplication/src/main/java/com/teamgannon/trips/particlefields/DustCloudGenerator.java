package com.teamgannon.trips.particlefields;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generator for dust clouds / nebulae.
 * <p>
 * Characteristics:
 * - Three-dimensional distribution (spherical/ellipsoidal, not a flat ring)
 * - Configurable density falloff (dense core vs shell-like)
 * - Noise-based filamentary structure for realistic appearance
 * - Core-biased color and opacity gradients
 * - Slow, turbulent motion rather than organized orbital motion
 * - Very small particles (dust/gas)
 * - Colorful (emission/reflection nebulae) or dark
 * - No clear orbital plane
 * <p>
 * Uses configuration parameters:
 * - radialPower: controls density falloff (0.3 = dense core, 0.7 = shell-like)
 * - noiseStrength: controls filamentary structure (0.0 - 1.0)
 * - noiseOctaves: controls detail level (2-4 typical)
 * - seed: for reproducible generation
 */
public class DustCloudGenerator implements RingElementGenerator {

    @Override
    public List<RingElement> generate(RingConfiguration config, Random random) {
        List<RingElement> elements = new ArrayList<>(config.numElements());

        double radialRange = config.outerRadius() - config.innerRadius();
        double sizeRange = config.maxSize() - config.minSize();

        // Create noise generator with configurable parameters from config
        NoiseGenerator noise = new NoiseGenerator(
                config.seed(),
                config.noisePersistence(),
                config.noiseLacunarity()
        );

        // Noise scale based on nebula size (larger nebulae need coarser noise)
        double noiseScale = 0.8 / Math.max(1, radialRange * 0.01);

        for (int i = 0; i < config.numElements(); i++) {
            // Generate particle with enhanced algorithms
            RingElement element = generateParticle(
                    config, random, noise,
                    radialRange, sizeRange, noiseScale
            );
            if (element != null) {
                elements.add(element);
            }
        }

        return elements;
    }

    /**
     * Generate a single particle with all enhancements.
     */
    private RingElement generateParticle(RingConfiguration config, Random random,
                                          NoiseGenerator noise,
                                          double radialRange, double sizeRange,
                                          double noiseScale) {
        // === RADIAL DISTRIBUTION ===
        // Use power-law for configurable density falloff
        double u = random.nextDouble();
        double r = config.innerRadius() + radialRange * Math.pow(u, config.radialPower());

        // Clamp to valid range
        r = Math.max(config.innerRadius(), Math.min(config.outerRadius(), r));

        // === ANGULAR DISTRIBUTION ===
        // Random direction in 3D (uniform on sphere surface)
        double theta = random.nextDouble() * 2 * Math.PI;  // azimuth [0, 2π]
        double phi = Math.acos(2 * random.nextDouble() - 1); // polar (uniform on sphere)

        // Convert spherical to Cartesian for noise sampling
        double px = r * Math.sin(phi) * Math.cos(theta);
        double py = r * Math.sin(phi) * Math.sin(theta);
        double pz = r * Math.cos(phi);

        // === NOISE DISPLACEMENT (Enhanced Multi-Scale Filamentary Structure) ===
        if (config.noiseStrength() > 0) {
            // Get anisotropy from config (defaults to [1.0, 0.7, 0.4] for horizontal streaks)
            double[] anisotropy = config.filamentAnisotropy();
            if (anisotropy == null || anisotropy.length != 3) {
                anisotropy = new double[]{1.0, 0.7, 0.4};
            }

            // Use multi-scale displacement for more realistic filaments
            // Combines coarse (large-scale) and fine (small-scale) displacement
            double[] displacement = noise.multiScaleFilamentDisplacement(
                    px * noiseScale,
                    py * noiseScale,
                    pz * noiseScale,
                    config.noiseOctaves(),
                    radialRange * 0.15 * config.noiseStrength(),
                    0.7,  // Coarse weight (large scale structures)
                    0.3,  // Fine weight (small scale detail)
                    anisotropy
            );

            px += displacement[0];
            py += displacement[1];
            pz += displacement[2];

            // Recalculate r after displacement (for color/opacity calculations)
            r = Math.sqrt(px * px + py * py + pz * pz);
            r = Math.max(config.innerRadius(), Math.min(config.outerRadius() * 1.1, r));
        }

        // Convert back to spherical-ish coordinates for the orbital model
        // (We use the orbital model for slow turbulent motion)
        double newTheta = Math.atan2(py, px);
        double newR = Math.sqrt(px * px + py * py);
        double newPhi = Math.atan2(newR, pz);

        // Convert to pseudo-orbital elements
        double inclination = newPhi - Math.PI / 2; // Range: -PI/2 to PI/2
        double longitudeOfAscendingNode = newTheta;

        // Very low eccentricity - particles drift slowly
        double eccentricity = random.nextDouble() * 0.02;

        // No preferred argument of periapsis
        double argumentOfPeriapsis = random.nextDouble() * 2 * Math.PI;
        double initialAngle = random.nextDouble() * 2 * Math.PI;

        // === MOTION ===
        // Very slow, almost random motion (turbulent, not orbital)
        double angularSpeed = config.baseAngularSpeed() * 0.1 * (0.5 + random.nextDouble());
        // Random direction of motion
        if (random.nextBoolean()) {
            angularSpeed = -angularSpeed;
        }

        // 3D height offset (for additional vertical spread)
        double heightOffset = random.nextGaussian() * config.thickness() * 0.3;

        // === SIZE ===
        // Very small dust particles, slightly larger near core
        double coreFactor = 1.0 - (r - config.innerRadius()) / radialRange;
        coreFactor = Math.max(0, Math.min(1, coreFactor));
        double size = config.minSize() + random.nextDouble() * sizeRange * (0.3 + 0.2 * coreFactor);

        // === COLOR & OPACITY ===
        Color color = calculateColor(config, random, noise, px, py, pz, r, radialRange, coreFactor);

        return new RingElement(
                r, eccentricity, inclination,
                argumentOfPeriapsis, longitudeOfAscendingNode,
                initialAngle, angularSpeed, size, heightOffset, color
        );
    }

    /**
     * Calculate particle color with gradient mode support.
     */
    private Color calculateColor(RingConfiguration config, Random random,
                                   NoiseGenerator noise,
                                   double px, double py, double pz,
                                   double r, double radialRange,
                                   double coreFactor) {
        // Calculate color factor based on gradient mode
        double colorFactor = calculateColorFactor(config, random, noise, px, py, pz, r, radialRange, coreFactor);

        Color base = interpolateColor(config.primaryColor(), config.secondaryColor(), colorFactor);

        // === CORE-BIASED BRIGHTNESS ===
        // Brighter/more saturated near core
        double brightnessBoost = 1.0 + coreFactor * 0.4;
        double saturationBoost = 1.0 + coreFactor * 0.3;

        base = base.deriveColor(
                0,                    // hue shift
                saturationBoost,      // saturation multiplier
                brightnessBoost,      // brightness multiplier
                1.0                   // opacity (set separately)
        );

        // === NOISE-BASED COLOR VARIATION ===
        if (config.noiseStrength() > 0) {
            double colorNoise = noise.layeredNoise(px * 0.5, py * 0.5, pz * 0.5, 2);
            // Subtle hue shift based on noise
            double hueShift = colorNoise * 10 * config.noiseStrength();
            base = base.deriveColor(hueShift, 1.0, 1.0, 1.0);
        }

        // === OPACITY ===
        // Higher opacity near core, more transparent at edges
        // Plus some random variation for natural look
        double baseOpacity = 0.3 + coreFactor * 0.4;
        double opacityVariation = random.nextDouble() * 0.3;
        double opacity = Math.min(1.0, baseOpacity + opacityVariation);

        // Apply noise-based opacity variation
        if (config.noiseStrength() > 0) {
            double opacityNoise = (noise.layeredNoise(px * 0.3, py * 0.3, pz * 0.3, 2) + 1) * 0.5;
            opacity *= (0.7 + opacityNoise * 0.3);
        }

        return Color.color(
                base.getRed(),
                base.getGreen(),
                base.getBlue(),
                Math.max(0.1, Math.min(1.0, opacity))
        );
    }

    @Override
    public RingType getRingType() {
        return RingType.DUST_CLOUD;
    }

    @Override
    public String getDescription() {
        return "Enhanced dust cloud generator - 3D diffuse distribution with " +
               "configurable density falloff, noise-based filaments, and core-biased gradients";
    }

    /**
     * Calculate color factor based on gradient mode.
     * Returns a value 0.0-1.0 that maps to the color palette.
     */
    private double calculateColorFactor(RingConfiguration config, Random random,
                                         NoiseGenerator noise,
                                         double px, double py, double pz,
                                         double r, double radialRange,
                                         double coreFactor) {
        ColorGradientMode mode = config.colorGradientMode() != null ?
                config.colorGradientMode() : ColorGradientMode.LINEAR;

        switch (mode) {
            case LINEAR:
                // Original behavior: random with core bias
                double colorFactor = random.nextDouble();
                return colorFactor * (1.0 - coreFactor * 0.3);

            case RADIAL:
                // Pure radial: 0 at core, 1 at edge
                return 1.0 - coreFactor;

            case NOISE_BASED:
                // Color varies with noise value
                double noiseValue = noise.layeredNoise(px * 0.5, py * 0.5, pz * 0.5, 2);
                return (noiseValue + 1.0) * 0.5;  // Map [-1,1] to [0,1]

            case TEMPERATURE:
                // Radial but inverted: hot core (0), cool edge (1)
                return 1.0 - coreFactor;

            case MULTI_ZONE:
                // Radial mapping to three zones
                return 1.0 - coreFactor;

            default:
                return random.nextDouble();
        }
    }

    /**
     * Linear color interpolation.
     */
    private Color interpolateColor(Color c1, Color c2, double t) {
        t = Math.max(0, Math.min(1, t));
        return Color.color(
                c1.getRed() + (c2.getRed() - c1.getRed()) * t,
                c1.getGreen() + (c2.getGreen() - c1.getGreen()) * t,
                c1.getBlue() + (c2.getBlue() - c1.getBlue()) * t
        );
    }
}
