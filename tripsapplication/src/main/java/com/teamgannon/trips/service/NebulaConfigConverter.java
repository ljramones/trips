package com.teamgannon.trips.service;

import com.teamgannon.trips.jpa.model.Nebula;
import com.teamgannon.trips.jpa.model.NebulaType;
import com.teamgannon.trips.particlefields.InterstellarRingAdapter;
import com.teamgannon.trips.particlefields.RingConfiguration;
import com.teamgannon.trips.particlefields.RingType;
import javafx.scene.paint.Color;
import lombok.extern.slf4j.Slf4j;

/**
 * Converts Nebula JPA entities to RingConfiguration for rendering.
 * <p>
 * Handles:
 * - Coordinate conversion (light-years to screen units via adapter)
 * - Particle count calculation with LOD scaling
 * - Color parsing from hex strings
 * - Type-specific parameter mapping
 */
@Slf4j
public class NebulaConfigConverter {

    // LOD distance thresholds (light-years)
    private static final double LOD_FULL_DISTANCE = 10.0;
    private static final double LOD_MEDIUM_DISTANCE = 50.0;
    private static final double LOD_LOW_DISTANCE = 100.0;

    // Particle count bounds
    private static final int MIN_PARTICLES = 1000;
    private static final int MAX_PARTICLES = 100_000;

    private NebulaConfigConverter() {
        // Utility class
    }

    /**
     * Convert a Nebula entity to a RingConfiguration for rendering.
     *
     * @param nebula         the nebula entity
     * @param adapter        the interstellar ring adapter for coordinate conversion
     * @param cameraDistance distance from camera to nebula center (for LOD)
     * @return RingConfiguration ready for RingFieldRenderer
     */
    public static RingConfiguration toRingConfiguration(Nebula nebula,
                                                         InterstellarRingAdapter adapter,
                                                         double cameraDistance) {
        // Convert coordinates to screen units
        double screenInner = adapter.toVisualUnits(nebula.getInnerRadius());
        double screenOuter = adapter.toVisualUnits(nebula.getOuterRadius());
        double screenThickness = adapter.toVisualUnits(nebula.getOuterRadius() * 0.8);

        // Calculate particle count with LOD
        int numElements = calculateNumElements(nebula, cameraDistance);

        // Calculate particle sizes based on nebula size
        double nebulaSize = screenOuter - screenInner;
        double minSize = Math.max(0.3, nebulaSize * 0.008);
        double maxSize = Math.max(1.0, nebulaSize * 0.025);

        // Parse colors
        Color primaryColor = parseColor(nebula.getPrimaryColor(), nebula.getType().getDefaultPrimaryColor());
        Color secondaryColor = parseColor(nebula.getSecondaryColor(), nebula.getType().getDefaultSecondaryColor());

        // Build configuration
        return RingConfiguration.builder()
                .type(RingType.DUST_CLOUD)
                .innerRadius(screenInner)
                .outerRadius(screenOuter)
                .numElements(numElements)
                .minSize(minSize)
                .maxSize(maxSize)
                .thickness(screenThickness)
                .maxInclinationDeg(90.0)  // Full 3D distribution
                .maxEccentricity(0.02)
                .baseAngularSpeed(nebula.isEnableAnimation() ? nebula.getBaseAngularSpeed() : 0.0)
                .centralBodyRadius(screenInner * 0.1)
                .primaryColor(primaryColor)
                .secondaryColor(secondaryColor)
                .name(nebula.getName())
                // Nebula-specific parameters
                .radialPower(nebula.getRadialPower())
                .noiseStrength(nebula.getNoiseStrength())
                .noiseOctaves(nebula.getNoiseOctaves())
                .seed(nebula.getSeed())
                .build();
    }

    /**
     * Calculate particle count based on density, volume, user override, and LOD.
     */
    public static int calculateNumElements(Nebula nebula, double cameraDistance) {
        int baseCount;

        // Check for user override first
        if (nebula.getNumElementsOverride() != null) {
            baseCount = nebula.getNumElementsOverride();
        } else {
            // Calculate from density and volume
            baseCount = nebula.calculateBaseParticleCount();
        }

        // Apply LOD scaling based on distance
        double lodFactor = calculateLodFactor(cameraDistance);
        int finalCount = (int) (baseCount * lodFactor);

        // Clamp to reasonable bounds
        finalCount = Math.max(MIN_PARTICLES, Math.min(MAX_PARTICLES, finalCount));

        log.debug("Nebula '{}': baseCount={}, lodFactor={:.2f}, finalCount={}",
                nebula.getName(), baseCount, lodFactor, finalCount);

        return finalCount;
    }

    /**
     * Calculate LOD factor based on camera distance.
     * Returns 1.0 for close nebulae, decreasing for distant ones.
     */
    public static double calculateLodFactor(double distanceLy) {
        if (distanceLy < LOD_FULL_DISTANCE) {
            return 1.0;
        } else if (distanceLy < LOD_MEDIUM_DISTANCE) {
            // Linear interpolation from 1.0 to 0.5
            double t = (distanceLy - LOD_FULL_DISTANCE) / (LOD_MEDIUM_DISTANCE - LOD_FULL_DISTANCE);
            return 1.0 - t * 0.5;
        } else if (distanceLy < LOD_LOW_DISTANCE) {
            // Linear interpolation from 0.5 to 0.2
            double t = (distanceLy - LOD_MEDIUM_DISTANCE) / (LOD_LOW_DISTANCE - LOD_MEDIUM_DISTANCE);
            return 0.5 - t * 0.3;
        } else {
            return 0.1;  // Minimum for very distant nebulae
        }
    }

    /**
     * Parse a hex color string, falling back to default if invalid.
     */
    public static Color parseColor(String hexColor, String defaultHex) {
        if (hexColor == null || hexColor.isBlank()) {
            hexColor = defaultHex;
        }

        try {
            return Color.web(hexColor);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid color '{}', using default '{}'", hexColor, defaultHex);
            try {
                return Color.web(defaultHex);
            } catch (IllegalArgumentException e2) {
                return Color.LIGHTGRAY;
            }
        }
    }

    /**
     * Calculate the screen position for a nebula.
     *
     * @param nebula  the nebula entity
     * @param adapter the interstellar ring adapter
     * @return array [screenX, screenY, screenZ]
     */
    public static double[] toScreenPosition(Nebula nebula, InterstellarRingAdapter adapter) {
        return new double[]{
                adapter.toVisualUnits(nebula.getCenterX()),
                adapter.toVisualUnits(nebula.getCenterY()),
                adapter.toVisualUnits(nebula.getCenterZ())
        };
    }

    /**
     * Calculate the distance from the plot center to a nebula.
     *
     * @param nebula      the nebula
     * @param plotCenterX plot center X in light-years
     * @param plotCenterY plot center Y in light-years
     * @param plotCenterZ plot center Z in light-years
     * @return distance in light-years
     */
    public static double calculateDistance(Nebula nebula,
                                            double plotCenterX,
                                            double plotCenterY,
                                            double plotCenterZ) {
        return nebula.distanceTo(plotCenterX, plotCenterY, plotCenterZ);
    }

    /**
     * Create a default Nebula entity from a NebulaType for quick testing.
     */
    public static Nebula createDefaultNebula(String name, NebulaType type, String datasetName,
                                              double x, double y, double z, double radius) {
        Nebula nebula = new Nebula(name, type, datasetName, x, y, z, radius);
        nebula.applyTypeDefaults();
        return nebula;
    }
}
