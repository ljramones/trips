package com.teamgannon.trips.starplotting;

import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumMap;
import java.util.Map;

/**
 * Object pool for star node spheres.
 * <p>
 * Reuses JavaFX Sphere objects to reduce garbage collection pressure and
 * improve rendering performance. Since Sphere divisions are set at construction
 * and cannot be changed, separate pools are maintained for each LOD level.
 * <p>
 * <b>Usage:</b>
 * <pre>
 * // Acquire a sphere from the pool
 * Sphere star = pool.acquire(LODLevel.MEDIUM, radius, material);
 *
 * // When done (e.g., clearing the plot)
 * pool.release(star, LODLevel.MEDIUM);
 * </pre>
 * <p>
 * <b>Performance Benefits:</b>
 * <ul>
 *   <li>Eliminates repeated Sphere allocation for recurring renders</li>
 *   <li>Reduces GC pressure during plot transitions</li>
 *   <li>Pre-warming can ensure smooth first render</li>
 * </ul>
 */
@Slf4j
public class StarNodePool {

    /**
     * Default initial pool size per LOD level.
     */
    private static final int DEFAULT_INITIAL_SIZE = 100;

    /**
     * Maximum pool size per LOD level to prevent unbounded growth.
     */
    private static final int MAX_POOL_SIZE = 5000;

    /**
     * Sphere divisions matching StarLODManager constants.
     */
    private static final int HIGH_DIVISIONS = 64;
    private static final int MEDIUM_DIVISIONS = 32;
    private static final int LOW_DIVISIONS = 16;
    private static final int MINIMAL_DIVISIONS = 8;

    /**
     * Pools organized by LOD level.
     */
    private final Map<StarLODManager.LODLevel, Deque<Sphere>> pools;

    /**
     * Statistics: total spheres created (not from pool).
     */
    private int totalCreated = 0;

    /**
     * Statistics: total acquires from pool (reused).
     */
    private int totalReused = 0;

    /**
     * Statistics: total releases back to pool.
     */
    private int totalReleased = 0;

    /**
     * Creates a new StarNodePool with default initial size.
     */
    public StarNodePool() {
        this(DEFAULT_INITIAL_SIZE);
    }

    /**
     * Creates a new StarNodePool with specified initial size per LOD level.
     *
     * @param initialSizePerLevel initial number of spheres to pre-allocate per LOD level
     */
    public StarNodePool(int initialSizePerLevel) {
        pools = new EnumMap<>(StarLODManager.LODLevel.class);

        for (StarLODManager.LODLevel level : StarLODManager.LODLevel.values()) {
            pools.put(level, new ArrayDeque<>(initialSizePerLevel));
        }

        log.info("StarNodePool created with initial capacity {} per LOD level", initialSizePerLevel);
    }

    /**
     * Pre-warms the pool by creating spheres ahead of time.
     * <p>
     * Call this during initialization to avoid allocation during first render.
     *
     * @param countPerLevel number of spheres to pre-create per LOD level
     */
    public void prewarm(int countPerLevel) {
        long startTime = System.currentTimeMillis();

        for (StarLODManager.LODLevel level : StarLODManager.LODLevel.values()) {
            Deque<Sphere> pool = pools.get(level);
            int divisions = getDivisionsForLevel(level);

            for (int i = 0; i < countPerLevel; i++) {
                pool.push(createSphere(divisions));
                totalCreated++;
            }
        }

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("StarNodePool pre-warmed with {} spheres per level ({} total) in {}ms",
                countPerLevel, countPerLevel * 4, elapsed);
    }

    /**
     * Acquires a sphere from the pool, creating one if necessary.
     * <p>
     * The sphere is configured with the specified radius and material.
     *
     * @param level    the LOD level (determines sphere divisions)
     * @param radius   the sphere radius
     * @param material the material to apply
     * @return a configured sphere ready for use
     */
    public @NotNull Sphere acquire(@NotNull StarLODManager.LODLevel level,
                                    double radius,
                                    @NotNull PhongMaterial material) {
        Deque<Sphere> pool = pools.get(level);
        Sphere sphere;

        if (pool.isEmpty()) {
            // Pool exhausted - create new sphere
            sphere = createSphere(getDivisionsForLevel(level));
            totalCreated++;
        } else {
            // Reuse from pool
            sphere = pool.pop();
            totalReused++;
        }

        // Configure the sphere
        sphere.setRadius(radius);
        sphere.setMaterial(material);

        // Reset transforms (in case previously used)
        sphere.setTranslateX(0);
        sphere.setTranslateY(0);
        sphere.setTranslateZ(0);
        sphere.setScaleX(1);
        sphere.setScaleY(1);
        sphere.setScaleZ(1);
        sphere.setRotate(0);
        sphere.setVisible(true);

        return sphere;
    }

    /**
     * Releases a sphere back to the pool for reuse.
     * <p>
     * The sphere should be removed from the scene graph before releasing.
     *
     * @param sphere the sphere to release
     * @param level  the LOD level of the sphere
     */
    public void release(@NotNull Sphere sphere, @NotNull StarLODManager.LODLevel level) {
        Deque<Sphere> pool = pools.get(level);

        // Don't grow pool beyond max size
        if (pool.size() < MAX_POOL_SIZE) {
            // Clear references to help GC
            sphere.setMaterial(null);
            sphere.setUserData(null);

            pool.push(sphere);
            totalReleased++;
        }
        // If pool is full, let the sphere be garbage collected
    }

    /**
     * Releases all spheres in a collection back to the pool.
     * <p>
     * Automatically determines LOD level based on sphere divisions.
     *
     * @param spheres the spheres to release
     */
    public void releaseAll(@NotNull Iterable<Sphere> spheres) {
        for (Sphere sphere : spheres) {
            StarLODManager.LODLevel level = getLevelForDivisions(sphere.getDivisions());
            release(sphere, level);
        }
    }

    /**
     * Clears all pools, releasing resources.
     */
    public void clear() {
        for (Deque<Sphere> pool : pools.values()) {
            pool.clear();
        }
        log.debug("StarNodePool cleared");
    }

    /**
     * Gets the current size of each pool.
     *
     * @return map of LOD level to pool size
     */
    public @NotNull Map<StarLODManager.LODLevel, Integer> getPoolSizes() {
        Map<StarLODManager.LODLevel, Integer> sizes = new EnumMap<>(StarLODManager.LODLevel.class);
        for (Map.Entry<StarLODManager.LODLevel, Deque<Sphere>> entry : pools.entrySet()) {
            sizes.put(entry.getKey(), entry.getValue().size());
        }
        return sizes;
    }

    /**
     * Gets pool statistics.
     *
     * @return statistics string
     */
    public @NotNull String getStatistics() {
        int totalPooled = pools.values().stream().mapToInt(Deque::size).sum();
        double reuseRate = (totalCreated + totalReused) > 0
                ? (100.0 * totalReused / (totalCreated + totalReused))
                : 0.0;

        return 
                "StarNodePool[created=%d, reused=%d, released=%d, pooled=%d, reuseRate=%.1f%%]".formatted(
                totalCreated, totalReused, totalReleased, totalPooled, reuseRate);
    }

    /**
     * Logs detailed pool statistics.
     */
    public void logStatistics() {
        int totalPooled = pools.values().stream().mapToInt(Deque::size).sum();
        double reuseRate = (totalCreated + totalReused) > 0
                ? (100.0 * totalReused / (totalCreated + totalReused))
                : 0.0;

        log.info("StarNodePool Statistics:");
        log.info("  Created (new): {}", totalCreated);
        log.info("  Reused (from pool): {}", totalReused);
        log.info("  Released (to pool): {}", totalReleased);
        log.info("  Currently pooled: {}", totalPooled);
        log.info("  Reuse rate: {:.1f}%", reuseRate);

        for (StarLODManager.LODLevel level : StarLODManager.LODLevel.values()) {
            log.info("    {}: {} available", level, pools.get(level).size());
        }
    }

    /**
     * Resets statistics counters.
     */
    public void resetStatistics() {
        totalCreated = 0;
        totalReused = 0;
        totalReleased = 0;
    }

    // =========================================================================
    // Private Helpers
    // =========================================================================

    private Sphere createSphere(int divisions) {
        return new Sphere(1.0, divisions); // Default radius, will be set on acquire
    }

    private int getDivisionsForLevel(StarLODManager.LODLevel level) {
        return switch (level) {
            case HIGH -> HIGH_DIVISIONS;
            case MEDIUM -> MEDIUM_DIVISIONS;
            case LOW -> LOW_DIVISIONS;
            case MINIMAL -> MINIMAL_DIVISIONS;
        };
    }

    private StarLODManager.LODLevel getLevelForDivisions(int divisions) {
        if (divisions >= HIGH_DIVISIONS) return StarLODManager.LODLevel.HIGH;
        if (divisions >= MEDIUM_DIVISIONS) return StarLODManager.LODLevel.MEDIUM;
        if (divisions >= LOW_DIVISIONS) return StarLODManager.LODLevel.LOW;
        return StarLODManager.LODLevel.MINIMAL;
    }
}
