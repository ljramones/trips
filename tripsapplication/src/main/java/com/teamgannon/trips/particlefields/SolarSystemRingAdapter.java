package com.teamgannon.trips.particlefields;

import com.teamgannon.trips.solarsystem.rendering.ScaleManager;
import javafx.scene.paint.Color;
import lombok.Getter;

/**
 * Adapts ring configurations for use in the solar system view.
 * Converts between Astronomical Units (AU) and screen coordinates
 * using the existing {@link ScaleManager}.
 *
 * <p>Example usage:
 * <pre>{@code
 * ScaleManager scaleManager = new ScaleManager(30.0); // max 30 AU
 * SolarSystemRingAdapter adapter = new SolarSystemRingAdapter(scaleManager);
 *
 * // Create Saturn's rings (inner: 1.2 Saturn radii, outer: 2.3 Saturn radii)
 * // Saturn is at ~9.5 AU, ring extends from ~0.07 AU to ~0.14 AU from Saturn
 * RingConfiguration saturnRings = adapter.createPlanetaryRing(
 *     0.07,  // inner radius in AU
 *     0.14,  // outer radius in AU
 *     "Saturn's Rings"
 * );
 *
 * RingFieldRenderer renderer = new RingFieldRenderer(saturnRings, new Random());
 * }</pre>
 */
public class SolarSystemRingAdapter implements RingScaleAdapter {

    @Getter
    private final ScaleManager scaleManager;

    /**
     * Creates an adapter using the given scale manager.
     *
     * @param scaleManager the scale manager for AU to screen conversion
     */
    public SolarSystemRingAdapter(ScaleManager scaleManager) {
        this.scaleManager = scaleManager;
    }

    @Override
    public double toVisualUnits(double au) {
        return scaleManager.auToScreen(au);
    }

    @Override
    public double fromVisualUnits(double visualValue) {
        return scaleManager.screenToAu(visualValue);
    }

    @Override
    public RingConfiguration adaptConfiguration(RingConfiguration config) {
        // Scale all distance-based parameters from AU to screen units
        return RingConfiguration.builder()
                .type(config.type())
                .innerRadius(toVisualUnits(config.innerRadius()))
                .outerRadius(toVisualUnits(config.outerRadius()))
                .numElements(config.numElements())
                .minSize(scaleParticleSize(config.minSize(), config))
                .maxSize(scaleParticleSize(config.maxSize(), config))
                .thickness(toVisualUnits(config.thickness()))
                .maxInclinationDeg(config.maxInclinationDeg())
                .maxEccentricity(config.maxEccentricity())
                .baseAngularSpeed(config.baseAngularSpeed())
                .centralBodyRadius(toVisualUnits(config.centralBodyRadius()))
                .primaryColor(config.primaryColor())
                .secondaryColor(config.secondaryColor())
                .name(config.name())
                .build();
    }

    @Override
    public RingConfiguration createAdaptedConfiguration(
            String presetName,
            double innerRadiusAU,
            double outerRadiusAU
    ) {
        // Get the preset as a template
        RingConfiguration template = RingFieldFactory.getPreset(presetName);

        // Calculate scale factor based on the preset's intended size vs actual size
        double presetRange = template.outerRadius() - template.innerRadius();
        double actualRange = outerRadiusAU - innerRadiusAU;
        double relativeScale = actualRange / presetRange;

        // Create configuration in AU units
        RingConfiguration auConfig = RingConfiguration.builder()
                .type(template.type())
                .innerRadius(innerRadiusAU)
                .outerRadius(outerRadiusAU)
                .numElements(template.numElements())
                .minSize(template.minSize() * relativeScale)
                .maxSize(template.maxSize() * relativeScale)
                .thickness(template.thickness() * relativeScale)
                .maxInclinationDeg(template.maxInclinationDeg())
                .maxEccentricity(template.maxEccentricity())
                .baseAngularSpeed(template.baseAngularSpeed())
                .centralBodyRadius(template.centralBodyRadius() * relativeScale)
                .primaryColor(template.primaryColor())
                .secondaryColor(template.secondaryColor())
                .name(template.name())
                .build();

        // Adapt to screen units
        return adaptConfiguration(auConfig);
    }

    /**
     * Creates a planetary ring configuration for a gas giant.
     *
     * @param innerRadiusAU inner ring radius in AU (from planet center)
     * @param outerRadiusAU outer ring radius in AU (from planet center)
     * @param name display name for the ring
     * @return adapted configuration in screen units
     */
    public RingConfiguration createPlanetaryRing(
            double innerRadiusAU,
            double outerRadiusAU,
            String name
    ) {
        double screenInner = toVisualUnits(innerRadiusAU);
        double screenOuter = toVisualUnits(outerRadiusAU);
        double ringWidth = screenOuter - screenInner;

        // Scale particle count based on ring area
        int numElements = Math.max(2000, (int) (ringWidth * 200));
        numElements = Math.min(numElements, 15000);

        // Particle sizes relative to ring width
        double minSize = Math.max(0.1, ringWidth * 0.005);
        double maxSize = Math.max(0.3, ringWidth * 0.02);

        return RingConfiguration.builder()
                .type(RingType.PLANETARY_RING)
                .innerRadius(screenInner)
                .outerRadius(screenOuter)
                .numElements(numElements)
                .minSize(minSize)
                .maxSize(maxSize)
                .thickness(ringWidth * 0.01)  // Very thin
                .maxInclinationDeg(0.5)
                .maxEccentricity(0.01)
                .baseAngularSpeed(0.004)
                .centralBodyRadius(screenInner * 0.5)  // Estimate planet size
                .primaryColor(Color.rgb(230, 220, 200))
                .secondaryColor(Color.rgb(180, 170, 160))
                .name(name)
                .build();
    }

    /**
     * Creates an asteroid belt configuration.
     *
     * @param innerRadiusAU inner belt radius in AU
     * @param outerRadiusAU outer belt radius in AU
     * @param name display name for the belt
     * @return adapted configuration in screen units
     */
    public RingConfiguration createAsteroidBelt(
            double innerRadiusAU,
            double outerRadiusAU,
            String name
    ) {
        double screenInner = toVisualUnits(innerRadiusAU);
        double screenOuter = toVisualUnits(outerRadiusAU);
        double beltWidth = screenOuter - screenInner;

        // Asteroid belts are sparse, fewer elements
        int numElements = Math.max(1000, (int) (beltWidth * 50));
        numElements = Math.min(numElements, 8000);

        // Larger particles for asteroids
        double minSize = Math.max(0.3, beltWidth * 0.01);
        double maxSize = Math.max(1.0, beltWidth * 0.05);

        return RingConfiguration.builder()
                .type(RingType.ASTEROID_BELT)
                .innerRadius(screenInner)
                .outerRadius(screenOuter)
                .numElements(numElements)
                .minSize(minSize)
                .maxSize(maxSize)
                .thickness(beltWidth * 0.15)  // Thick vertical distribution
                .maxInclinationDeg(15.0)
                .maxEccentricity(0.08)
                .baseAngularSpeed(0.002)
                .centralBodyRadius(toVisualUnits(0.005))  // Small star
                .primaryColor(Color.rgb(140, 130, 120))
                .secondaryColor(Color.rgb(100, 90, 80))
                .name(name)
                .build();
    }

    /**
     * Creates a debris disk configuration (e.g., protoplanetary disk).
     *
     * @param innerRadiusAU inner disk radius in AU
     * @param outerRadiusAU outer disk radius in AU
     * @param name display name for the disk
     * @return adapted configuration in screen units
     */
    public RingConfiguration createDebrisDisk(
            double innerRadiusAU,
            double outerRadiusAU,
            String name
    ) {
        double screenInner = toVisualUnits(innerRadiusAU);
        double screenOuter = toVisualUnits(outerRadiusAU);
        double diskWidth = screenOuter - screenInner;

        int numElements = Math.max(3000, (int) (diskWidth * 100));
        numElements = Math.min(numElements, 12000);

        double minSize = Math.max(0.15, diskWidth * 0.003);
        double maxSize = Math.max(0.6, diskWidth * 0.015);

        return RingConfiguration.builder()
                .type(RingType.DEBRIS_DISK)
                .innerRadius(screenInner)
                .outerRadius(screenOuter)
                .numElements(numElements)
                .minSize(minSize)
                .maxSize(maxSize)
                .thickness(diskWidth * 0.08)
                .maxInclinationDeg(8.0)
                .maxEccentricity(0.05)
                .baseAngularSpeed(0.003)
                .centralBodyRadius(toVisualUnits(0.01))
                .primaryColor(Color.rgb(200, 180, 150))
                .secondaryColor(Color.rgb(180, 140, 100))
                .name(name)
                .build();
    }

    @Override
    public String getUnitName() {
        return "AU";
    }

    @Override
    public double getScaleFactor() {
        return scaleManager.getBaseScale() * scaleManager.getZoomLevel();
    }

    /**
     * Scales particle size appropriately for the ring dimensions.
     */
    private double scaleParticleSize(double originalSize, RingConfiguration config) {
        // Scale particle size proportionally, but with limits
        double scaleFactor = toVisualUnits(1.0);  // Screen units per AU
        double scaledSize = originalSize * scaleFactor * 0.01;  // Particles much smaller than orbital scale

        // Clamp to reasonable display sizes
        return Math.max(0.1, Math.min(5.0, scaledSize));
    }
}
