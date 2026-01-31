package com.teamgannon.trips.starplotting;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Manages Level-of-Detail (LOD) for nebula rendering.
 * <p>
 * Provides multi-strategy LOD that changes rendering approach based on distance,
 * not just particle count. Includes hysteresis to prevent flickering during
 * zoom/pan operations.
 * <p>
 * LOD Levels:
 * <ul>
 *   <li>FULL: < 10 ly - 100% particles, all features, glow</li>
 *   <li>HIGH: 10-30 ly - 50% particles, all features, glow</li>
 *   <li>MEDIUM: 30-60 ly - 25% particles, no glow, reduced octaves</li>
 *   <li>LOW: 60-100 ly - 10% particles, no glow, no noise</li>
 *   <li>MINIMAL: 100-200 ly - Billboard sprite (not yet implemented)</li>
 *   <li>CULLED: > 200 ly - Not rendered</li>
 * </ul>
 */
@Slf4j
public class NebulaLODManager {

    /**
     * LOD level enumeration with associated properties.
     */
    public enum LODLevel {
        FULL(10.0, 1.0, true, true, 4, "Full detail"),
        HIGH(30.0, 0.5, true, true, 3, "High detail"),
        MEDIUM(60.0, 0.25, false, true, 2, "Medium detail"),
        LOW(100.0, 0.1, false, false, 0, "Low detail"),
        MINIMAL(200.0, 0.0, false, false, 0, "Billboard"),
        CULLED(Double.MAX_VALUE, 0.0, false, false, 0, "Not rendered");

        @Getter
        private final double maxDistance;
        @Getter
        private final double particleFactor;
        @Getter
        private final boolean glowEnabled;
        @Getter
        private final boolean noiseEnabled;
        @Getter
        private final int maxNoiseOctaves;
        @Getter
        private final String description;

        LODLevel(double maxDistance, double particleFactor, boolean glowEnabled,
                 boolean noiseEnabled, int maxNoiseOctaves, String description) {
            this.maxDistance = maxDistance;
            this.particleFactor = particleFactor;
            this.glowEnabled = glowEnabled;
            this.noiseEnabled = noiseEnabled;
            this.maxNoiseOctaves = maxNoiseOctaves;
            this.description = description;
        }
    }

    // Hysteresis factors to prevent flickering during zoom
    private static final double UPGRADE_FACTOR = 0.8;   // Upgrade at 80% of threshold
    private static final double DOWNGRADE_FACTOR = 1.2; // Downgrade at 120% of threshold

    // Minimum zoom level (prevents extreme LOD reduction when zoomed in close)
    private static final double MIN_ZOOM_FOR_LOD = 0.5;

    /**
     * Current LOD level cache per nebula ID
     */
    private final java.util.Map<String, LODLevel> currentLevels = new java.util.HashMap<>();

    /**
     * Singleton instance
     */
    private static NebulaLODManager instance;

    /**
     * Get the singleton instance.
     */
    public static synchronized NebulaLODManager getInstance() {
        if (instance == null) {
            instance = new NebulaLODManager();
        }
        return instance;
    }

    private NebulaLODManager() {
        // Private constructor for singleton
    }

    /**
     * Calculate the appropriate LOD level for a nebula.
     *
     * @param nebulaId    unique identifier for the nebula
     * @param distanceLy  distance from camera to nebula center (light-years)
     * @param zoomLevel   current zoom level (1.0 = normal)
     * @return the appropriate LOD level
     */
    public LODLevel calculateLOD(String nebulaId, double distanceLy, double zoomLevel) {
        // Adjust distance based on zoom level
        // When zoomed in (zoomLevel > 1), effective distance is smaller
        double effectiveZoom = Math.max(MIN_ZOOM_FOR_LOD, zoomLevel);
        double adjustedDistance = distanceLy / effectiveZoom;

        // Get current level for hysteresis
        LODLevel currentLevel = currentLevels.get(nebulaId);

        // Determine new level
        LODLevel newLevel = determineLevelWithHysteresis(adjustedDistance, currentLevel);

        // Cache the new level
        currentLevels.put(nebulaId, newLevel);

        if (currentLevel != null && currentLevel != newLevel) {
            log.debug("LOD transition for {}: {} -> {} (dist={:.1f}ly, zoom={:.2f}, adjusted={:.1f})",
                    nebulaId, currentLevel, newLevel, distanceLy, zoomLevel, adjustedDistance);
        }

        return newLevel;
    }

    /**
     * Determine LOD level with hysteresis to prevent flickering.
     */
    private LODLevel determineLevelWithHysteresis(double adjustedDistance, LODLevel currentLevel) {
        // If no current level, use strict thresholds
        if (currentLevel == null) {
            return determineLevelStrict(adjustedDistance);
        }

        // Check if we should upgrade (move to higher detail)
        LODLevel[] levels = LODLevel.values();
        int currentIndex = currentLevel.ordinal();

        // Can we upgrade? (lower index = higher detail)
        if (currentIndex > 0) {
            LODLevel higherLevel = levels[currentIndex - 1];
            double upgradeThreshold = higherLevel.maxDistance * DOWNGRADE_FACTOR;
            if (adjustedDistance <= upgradeThreshold) {
                return higherLevel;
            }
        }

        // Should we downgrade? (higher index = lower detail)
        if (currentIndex < levels.length - 1) {
            double downgradeThreshold = currentLevel.maxDistance * UPGRADE_FACTOR;
            if (adjustedDistance > currentLevel.maxDistance) {
                return levels[currentIndex + 1];
            }
        }

        // Stay at current level
        return currentLevel;
    }

    /**
     * Determine LOD level using strict thresholds (no hysteresis).
     */
    private LODLevel determineLevelStrict(double adjustedDistance) {
        for (LODLevel level : LODLevel.values()) {
            if (adjustedDistance <= level.maxDistance) {
                return level;
            }
        }
        return LODLevel.CULLED;
    }

    /**
     * Calculate the particle count for a given LOD level.
     *
     * @param baseCount the base particle count at full LOD
     * @param level     the LOD level
     * @return the adjusted particle count
     */
    public int calculateParticleCount(int baseCount, LODLevel level) {
        if (level == LODLevel.CULLED || level == LODLevel.MINIMAL) {
            return 0;
        }
        int count = (int) (baseCount * level.particleFactor);
        return Math.max(1000, count);  // Minimum 1000 particles
    }

    /**
     * Get the noise octaves for a given LOD level.
     *
     * @param requestedOctaves the originally requested octave count
     * @param level            the LOD level
     * @return the adjusted octave count
     */
    public int calculateNoiseOctaves(int requestedOctaves, LODLevel level) {
        if (!level.noiseEnabled) {
            return 0;
        }
        return Math.min(requestedOctaves, level.maxNoiseOctaves);
    }

    /**
     * Check if a nebula should be rendered at this LOD level.
     */
    public boolean shouldRender(LODLevel level) {
        return level != LODLevel.CULLED;
    }

    /**
     * Check if a nebula should use billboard rendering (sprite) instead of particles.
     */
    public boolean useBillboard(LODLevel level) {
        return level == LODLevel.MINIMAL;
    }

    /**
     * Clear the LOD cache for a specific nebula.
     */
    public void clearCache(String nebulaId) {
        currentLevels.remove(nebulaId);
    }

    /**
     * Clear all LOD caches.
     */
    public void clearAllCaches() {
        currentLevels.clear();
    }

    /**
     * Get the current cached LOD level for a nebula.
     */
    public LODLevel getCurrentLevel(String nebulaId) {
        return currentLevels.get(nebulaId);
    }

    /**
     * Get summary of current LOD states for debugging.
     */
    public String getLODSummary() {
        if (currentLevels.isEmpty()) {
            return "No nebulae tracked";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("LOD States:\n");
        for (var entry : currentLevels.entrySet()) {
            sb.append(String.format("  %s: %s\n", entry.getKey(), entry.getValue()));
        }
        return sb.toString();
    }
}
