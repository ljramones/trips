package com.teamgannon.trips.particlefields;

import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.Setter;

/**
 * Adapts ring configurations for use in the interstellar view.
 * Converts between light-years and screen coordinates.
 *
 * <p>In the interstellar view, coordinates are typically in light-years
 * with the origin at Sol. This adapter allows nebulae and other large-scale
 * structures to be rendered using the ring system.
 *
 * <p>Example usage:
 * <pre>{@code
 * InterstellarRingAdapter adapter = new InterstellarRingAdapter(50.0); // 50 screen units per ly
 *
 * // Create a nebula at position (10, 5, -3) light-years
 * RingConfiguration nebula = adapter.createEmissionNebula(
 *     2.0,   // inner radius in light-years
 *     15.0,  // outer radius in light-years
 *     "Orion Nebula"
 * );
 *
 * RingFieldRenderer renderer = new RingFieldRenderer(nebula, new Random());
 * renderer.setPosition(
 *     adapter.toVisualUnits(10),
 *     adapter.toVisualUnits(5),
 *     adapter.toVisualUnits(-3)
 * );
 * }</pre>
 */
public class InterstellarRingAdapter implements RingScaleAdapter {

    /**
     * Screen units per light-year at zoom level 1.0
     */
    @Getter
    @Setter
    private double baseScale;

    /**
     * Current zoom level
     */
    @Getter
    @Setter
    private double zoomLevel = 1.0;

    /**
     * Creates an adapter with the given scale.
     *
     * @param baseScale screen units per light-year
     */
    public InterstellarRingAdapter(double baseScale) {
        this.baseScale = baseScale;
    }

    /**
     * Creates an adapter calibrated to fit a given display area.
     *
     * @param maxDistanceLy maximum distance to display in light-years
     * @param displayRadius desired screen radius for that distance
     * @return configured adapter
     */
    public static InterstellarRingAdapter forDisplayArea(double maxDistanceLy, double displayRadius) {
        double scale = displayRadius / maxDistanceLy;
        return new InterstellarRingAdapter(scale);
    }

    @Override
    public double toVisualUnits(double lightYears) {
        return lightYears * baseScale * zoomLevel;
    }

    @Override
    public double fromVisualUnits(double visualValue) {
        return visualValue / (baseScale * zoomLevel);
    }

    @Override
    public RingConfiguration adaptConfiguration(RingConfiguration config) {
        return RingConfiguration.builder()
                .type(config.type())
                .innerRadius(toVisualUnits(config.innerRadius()))
                .outerRadius(toVisualUnits(config.outerRadius()))
                .numElements(config.numElements())
                .minSize(scaleParticleSize(config.minSize()))
                .maxSize(scaleParticleSize(config.maxSize()))
                .thickness(toVisualUnits(config.thickness()))
                .maxInclinationDeg(config.maxInclinationDeg())
                .maxEccentricity(config.maxEccentricity())
                .baseAngularSpeed(config.baseAngularSpeed() * 0.1)  // Slow down for interstellar scale
                .centralBodyRadius(toVisualUnits(config.centralBodyRadius()))
                .primaryColor(config.primaryColor())
                .secondaryColor(config.secondaryColor())
                .name(config.name())
                .build();
    }

    @Override
    public RingConfiguration createAdaptedConfiguration(
            String presetName,
            double innerRadiusLy,
            double outerRadiusLy
    ) {
        RingConfiguration template = RingFieldFactory.getPreset(presetName);

        double presetRange = template.outerRadius() - template.innerRadius();
        double actualRange = outerRadiusLy - innerRadiusLy;
        double relativeScale = actualRange / presetRange;

        RingConfiguration lyConfig = RingConfiguration.builder()
                .type(template.type())
                .innerRadius(innerRadiusLy)
                .outerRadius(outerRadiusLy)
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

        return adaptConfiguration(lyConfig);
    }

    /**
     * Creates an emission nebula configuration (glowing gas cloud).
     *
     * @param innerRadiusLy inner radius in light-years (can be 0 for filled)
     * @param outerRadiusLy outer radius in light-years
     * @param name display name
     * @return adapted configuration in screen units
     */
    public RingConfiguration createEmissionNebula(
            double innerRadiusLy,
            double outerRadiusLy,
            String name
    ) {
        double screenInner = toVisualUnits(innerRadiusLy);
        double screenOuter = toVisualUnits(outerRadiusLy);
        double nebulaSize = screenOuter - screenInner;

        // Nebulae need lots of particles for the diffuse look
        int numElements = Math.max(5000, (int) (nebulaSize * 100));
        numElements = Math.min(numElements, 15000);

        double minSize = Math.max(0.3, nebulaSize * 0.01);
        double maxSize = Math.max(1.0, nebulaSize * 0.03);

        return RingConfiguration.builder()
                .type(RingType.DUST_CLOUD)
                .innerRadius(screenInner)
                .outerRadius(screenOuter)
                .numElements(numElements)
                .minSize(minSize)
                .maxSize(maxSize)
                .thickness(nebulaSize * 0.8)  // 3D distribution
                .maxInclinationDeg(90.0)       // Full sphere
                .maxEccentricity(0.02)
                .baseAngularSpeed(0.0002)      // Very slow motion
                .centralBodyRadius(nebulaSize * 0.05)
                .primaryColor(Color.rgb(255, 100, 150))   // H-alpha pink
                .secondaryColor(Color.rgb(100, 200, 255)) // OIII blue-green
                .name(name)
                .build();
    }

    /**
     * Creates a dark nebula configuration (obscuring dust cloud).
     *
     * @param innerRadiusLy inner radius in light-years
     * @param outerRadiusLy outer radius in light-years
     * @param name display name
     * @return adapted configuration in screen units
     */
    public RingConfiguration createDarkNebula(
            double innerRadiusLy,
            double outerRadiusLy,
            String name
    ) {
        double screenInner = toVisualUnits(innerRadiusLy);
        double screenOuter = toVisualUnits(outerRadiusLy);
        double nebulaSize = screenOuter - screenInner;

        int numElements = Math.max(3000, (int) (nebulaSize * 60));
        numElements = Math.min(numElements, 10000);

        double minSize = Math.max(0.5, nebulaSize * 0.015);
        double maxSize = Math.max(1.5, nebulaSize * 0.04);

        return RingConfiguration.builder()
                .type(RingType.DUST_CLOUD)
                .innerRadius(screenInner)
                .outerRadius(screenOuter)
                .numElements(numElements)
                .minSize(minSize)
                .maxSize(maxSize)
                .thickness(nebulaSize * 0.6)
                .maxInclinationDeg(90.0)
                .maxEccentricity(0.02)
                .baseAngularSpeed(0.0001)
                .centralBodyRadius(nebulaSize * 0.03)
                .primaryColor(Color.rgb(40, 35, 30))
                .secondaryColor(Color.rgb(20, 18, 15))
                .name(name)
                .build();
    }

    /**
     * Creates a reflection nebula configuration (dust reflecting starlight).
     *
     * @param innerRadiusLy inner radius in light-years
     * @param outerRadiusLy outer radius in light-years
     * @param starColor color of the illuminating star
     * @param name display name
     * @return adapted configuration in screen units
     */
    public RingConfiguration createReflectionNebula(
            double innerRadiusLy,
            double outerRadiusLy,
            Color starColor,
            String name
    ) {
        double screenInner = toVisualUnits(innerRadiusLy);
        double screenOuter = toVisualUnits(outerRadiusLy);
        double nebulaSize = screenOuter - screenInner;

        int numElements = Math.max(4000, (int) (nebulaSize * 80));
        numElements = Math.min(numElements, 12000);

        double minSize = Math.max(0.2, nebulaSize * 0.008);
        double maxSize = Math.max(0.8, nebulaSize * 0.025);

        // Reflection nebulae appear blue due to scattering
        Color blueShifted = Color.color(
                Math.max(0, starColor.getRed() * 0.6),
                Math.max(0, starColor.getGreen() * 0.7),
                Math.min(1, starColor.getBlue() * 1.2 + 0.3)
        );

        return RingConfiguration.builder()
                .type(RingType.DUST_CLOUD)
                .innerRadius(screenInner)
                .outerRadius(screenOuter)
                .numElements(numElements)
                .minSize(minSize)
                .maxSize(maxSize)
                .thickness(nebulaSize * 0.5)
                .maxInclinationDeg(90.0)
                .maxEccentricity(0.02)
                .baseAngularSpeed(0.00015)
                .centralBodyRadius(nebulaSize * 0.04)
                .primaryColor(blueShifted)
                .secondaryColor(blueShifted.darker())
                .name(name)
                .build();
    }

    /**
     * Creates a planetary nebula configuration (expanding shell from dying star).
     *
     * @param innerRadiusLy inner radius (hollow center)
     * @param outerRadiusLy outer radius of the shell
     * @param name display name
     * @return adapted configuration in screen units
     */
    public RingConfiguration createPlanetaryNebula(
            double innerRadiusLy,
            double outerRadiusLy,
            String name
    ) {
        double screenInner = toVisualUnits(innerRadiusLy);
        double screenOuter = toVisualUnits(outerRadiusLy);
        double shellThickness = screenOuter - screenInner;

        // Planetary nebulae are relatively small but detailed
        int numElements = Math.max(6000, (int) (shellThickness * 500));
        numElements = Math.min(numElements, 15000);

        double minSize = Math.max(0.15, shellThickness * 0.02);
        double maxSize = Math.max(0.5, shellThickness * 0.06);

        return RingConfiguration.builder()
                .type(RingType.DUST_CLOUD)
                .innerRadius(screenInner)
                .outerRadius(screenOuter)
                .numElements(numElements)
                .minSize(minSize)
                .maxSize(maxSize)
                .thickness(shellThickness * 1.2)  // Spherical shell
                .maxInclinationDeg(90.0)
                .maxEccentricity(0.01)
                .baseAngularSpeed(0.0003)  // Expanding motion
                .centralBodyRadius(screenInner * 0.3)  // White dwarf remnant
                .primaryColor(Color.rgb(100, 255, 200))  // Ionized oxygen green
                .secondaryColor(Color.rgb(200, 100, 255)) // Ionized nitrogen purple
                .name(name)
                .build();
    }

    @Override
    public String getUnitName() {
        return "Light-years";
    }

    @Override
    public double getScaleFactor() {
        return baseScale * zoomLevel;
    }

    /**
     * Scales particle size for interstellar distances.
     */
    private double scaleParticleSize(double originalSize) {
        double scaledSize = originalSize * baseScale * 0.1;
        return Math.max(0.2, Math.min(8.0, scaledSize));
    }
}
