package com.teamgannon.trips.service;

import com.teamgannon.trips.jpa.model.Nebula;
import com.teamgannon.trips.jpa.model.NebulaType;
import com.teamgannon.trips.particlefields.ColorGradientMode;
import com.teamgannon.trips.particlefields.InterstellarRingAdapter;
import com.teamgannon.trips.particlefields.RingConfiguration;
import com.teamgannon.trips.particlefields.RingType;
import com.teamgannon.trips.starplotting.NebulaLODManager;
import com.teamgannon.trips.starplotting.NebulaLODManager.LODLevel;
import javafx.scene.paint.Color;
import lombok.extern.slf4j.Slf4j;

/**
 * Converts Nebula JPA entities to RingConfiguration for rendering.
 * <p>
 * Handles:
 * - Coordinate conversion (light-years to screen units via adapter)
 * - Particle count calculation with LOD scaling via NebulaLODManager
 * - Color parsing from hex strings
 * - Type-specific parameter mapping
 * - LOD-based feature reduction (glow, noise octaves)
 */
@Slf4j
public class NebulaConfigConverter {

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
        return toRingConfiguration(nebula, adapter, cameraDistance, 1.0);
    }

    /**
     * Convert a Nebula entity to a RingConfiguration for rendering with zoom-aware LOD.
     *
     * @param nebula         the nebula entity
     * @param adapter        the interstellar ring adapter for coordinate conversion
     * @param cameraDistance distance from camera to nebula center (for LOD)
     * @param zoomLevel      current zoom level (1.0 = normal)
     * @return RingConfiguration ready for RingFieldRenderer, or null if nebula should be culled
     */
    public static RingConfiguration toRingConfiguration(Nebula nebula,
                                                         InterstellarRingAdapter adapter,
                                                         double cameraDistance,
                                                         double zoomLevel) {
        // Get LOD manager and calculate LOD level
        NebulaLODManager lodManager = NebulaLODManager.getInstance();
        LODLevel lodLevel = lodManager.calculateLOD(nebula.getId(), cameraDistance, zoomLevel);

        // Check if nebula should be culled
        if (!lodManager.shouldRender(lodLevel)) {
            log.debug("Nebula '{}' culled at distance {:.1f}ly", nebula.getName(), cameraDistance);
            return null;
        }

        // Check if we should use billboard (not yet implemented, fall back to minimal particles)
        boolean useBillboard = lodManager.useBillboard(lodLevel);
        if (useBillboard) {
            log.debug("Nebula '{}' using billboard at distance {:.1f}ly (billboard not yet implemented)",
                    nebula.getName(), cameraDistance);
            // TODO: Implement billboard rendering
            // For now, use minimal particles
        }

        // Convert coordinates to screen units
        double screenInner = adapter.toVisualUnits(nebula.getInnerRadius());
        double screenOuter = adapter.toVisualUnits(nebula.getOuterRadius());
        double screenThickness = adapter.toVisualUnits(nebula.getOuterRadius() * 0.8);

        // Calculate particle count with LOD
        int baseCount = nebula.calculateBaseParticleCount();
        int numElements = lodManager.calculateParticleCount(baseCount, lodLevel);
        numElements = Math.max(MIN_PARTICLES, Math.min(MAX_PARTICLES, numElements));

        // Calculate noise octaves with LOD
        int noiseOctaves = lodManager.calculateNoiseOctaves(nebula.getNoiseOctaves(), lodLevel);

        // LOD-adjusted noise strength (disable at LOW and below)
        double noiseStrength = lodLevel.isNoiseEnabled() ? nebula.getNoiseStrength() : 0.0;

        // LOD-adjusted glow
        boolean glowEnabled = lodLevel.isGlowEnabled() && nebula.isEnableGlow();
        double glowIntensity = glowEnabled ? nebula.getGlowIntensity() : 0.0;

        // Calculate particle sizes based on nebula size
        double nebulaSize = screenOuter - screenInner;
        double minSize = Math.max(0.3, nebulaSize * 0.008);
        double maxSize = Math.max(1.0, nebulaSize * 0.025);

        // Parse colors
        Color primaryColor = parseColor(nebula.getPrimaryColor(), nebula.getType().getDefaultPrimaryColor());
        Color secondaryColor = parseColor(nebula.getSecondaryColor(), nebula.getType().getDefaultSecondaryColor());
        Color tertiaryColor = parseColor(nebula.getTertiaryColor(),
                primaryColor.interpolate(secondaryColor, 0.5).toString());

        // Parse color gradient mode
        ColorGradientMode gradientMode = ColorGradientMode.fromString(nebula.getColorGradientMode());

        log.debug("Nebula '{}' LOD {}: particles={}, octaves={}, glow={}, noise={}",
                nebula.getName(), lodLevel, numElements, noiseOctaves, glowEnabled, noiseStrength > 0);

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
                .tertiaryColor(tertiaryColor)
                .colorGradientMode(gradientMode)
                .name(nebula.getName())
                // Nebula-specific parameters
                .radialPower(nebula.getRadialPower())
                .noiseStrength(noiseStrength)
                .noiseOctaves(noiseOctaves)
                .noisePersistence(nebula.getNoisePersistence())
                .noiseLacunarity(nebula.getNoiseLacunarity())
                .filamentAnisotropy(nebula.getFilamentAnisotropy())
                .seed(nebula.getSeed())
                // Glow parameters (LOD-adjusted)
                .glowEnabled(glowEnabled)
                .glowIntensity(glowIntensity)
                .build();
    }

    /**
     * Get the current LOD level for a nebula.
     *
     * @param nebulaId the nebula ID
     * @return the current LOD level, or null if not tracked
     */
    public static LODLevel getCurrentLODLevel(String nebulaId) {
        return NebulaLODManager.getInstance().getCurrentLevel(nebulaId);
    }

    /**
     * Calculate particle count based on density, volume, user override, and LOD.
     *
     * @deprecated Use toRingConfiguration with LOD manager instead.
     * This method is kept for backward compatibility but uses the new LOD system.
     */
    @Deprecated
    public static int calculateNumElements(Nebula nebula, double cameraDistance) {
        return calculateNumElements(nebula, cameraDistance, 1.0);
    }

    /**
     * Calculate particle count based on density, volume, user override, and LOD.
     *
     * @param nebula         the nebula
     * @param cameraDistance distance to camera
     * @param zoomLevel      current zoom level
     * @return particle count adjusted for LOD
     */
    public static int calculateNumElements(Nebula nebula, double cameraDistance, double zoomLevel) {
        int baseCount;

        // Check for user override first
        if (nebula.getNumElementsOverride() != null) {
            baseCount = nebula.getNumElementsOverride();
        } else {
            // Calculate from density and volume
            baseCount = nebula.calculateBaseParticleCount();
        }

        // Use LOD manager for consistent LOD calculation
        NebulaLODManager lodManager = NebulaLODManager.getInstance();
        LODLevel level = lodManager.calculateLOD(nebula.getId(), cameraDistance, zoomLevel);
        int finalCount = lodManager.calculateParticleCount(baseCount, level);

        // Clamp to reasonable bounds
        finalCount = Math.max(MIN_PARTICLES, Math.min(MAX_PARTICLES, finalCount));

        log.debug("Nebula '{}': baseCount={}, LOD={}, finalCount={}",
                nebula.getName(), baseCount, level, finalCount);

        return finalCount;
    }

    /**
     * Calculate LOD factor based on camera distance.
     * Returns 1.0 for close nebulae, decreasing for distant ones.
     *
     * @deprecated Use NebulaLODManager.calculateLOD() instead for full LOD support.
     */
    @Deprecated
    public static double calculateLodFactor(double distanceLy) {
        // Map the old simple factor to the new LOD system
        LODLevel level = NebulaLODManager.getInstance().calculateLOD("_legacy_", distanceLy, 1.0);
        return level.getParticleFactor();
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
