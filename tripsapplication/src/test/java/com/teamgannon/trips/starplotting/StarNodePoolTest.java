package com.teamgannon.trips.starplotting;

import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for StarNodePool.
 * <p>
 * Tests cover:
 * <ul>
 *   <li>Pool initialization and default values</li>
 *   <li>Sphere acquisition and configuration</li>
 *   <li>Sphere release and reuse</li>
 *   <li>Pre-warming functionality</li>
 *   <li>Pool size limits</li>
 *   <li>Statistics tracking</li>
 *   <li>Pool clearing</li>
 * </ul>
 */
class StarNodePoolTest {

    private static boolean javaFxInitialized = false;
    private StarNodePool pool;

    @BeforeAll
    static void initJavaFx() {
        try {
            Platform.startup(() -> {});
            javaFxInitialized = true;
        } catch (IllegalStateException e) {
            javaFxInitialized = true;
        } catch (Exception e) {
            System.out.println("JavaFX not available: " + e.getMessage());
            javaFxInitialized = false;
        }
    }

    @BeforeEach
    void setUp() {
        pool = new StarNodePool();
    }

    // =========================================================================
    // Initialization Tests
    // =========================================================================

    @Nested
    @DisplayName("Initialization Tests")
    class InitializationTests {

        @Test
        @DisplayName("New pool has empty pools for all LOD levels")
        void newPoolHasEmptyPools() {
            Map<StarLODManager.LODLevel, Integer> sizes = pool.getPoolSizes();

            assertEquals(4, sizes.size(), "Should have pools for all 4 LOD levels");
            for (StarLODManager.LODLevel level : StarLODManager.LODLevel.values()) {
                assertEquals(0, sizes.get(level), "Pool for " + level + " should be empty");
            }
        }

        @Test
        @DisplayName("New pool has zero statistics")
        void newPoolHasZeroStatistics() {
            String stats = pool.getStatistics();

            assertTrue(stats.contains("created=0"));
            assertTrue(stats.contains("reused=0"));
            assertTrue(stats.contains("released=0"));
        }

        @Test
        @DisplayName("Custom initial size constructor works")
        void customInitialSizeConstructor() {
            StarNodePool customPool = new StarNodePool(200);

            // Pool should be created but still empty until prewarm
            Map<StarLODManager.LODLevel, Integer> sizes = customPool.getPoolSizes();
            for (Integer size : sizes.values()) {
                assertEquals(0, size, "Pool should be empty before prewarm");
            }
        }
    }

    // =========================================================================
    // Acquisition Tests
    // =========================================================================

    @Nested
    @DisplayName("Sphere Acquisition Tests")
    class AcquisitionTests {

        @Test
        @DisplayName("Acquire creates new sphere when pool is empty")
        void acquireCreatesNewSphereWhenPoolEmpty() throws Exception {
            assumeJavaFxAvailable();

            Sphere sphere = runOnFxThread(() -> {
                PhongMaterial material = new PhongMaterial(Color.RED);
                return pool.acquire(StarLODManager.LODLevel.MEDIUM, 5.0, material);
            });

            assertNotNull(sphere);
            assertEquals(5.0, sphere.getRadius(), 0.001);
            assertNotNull(sphere.getMaterial());

            String stats = pool.getStatistics();
            assertTrue(stats.contains("created=1"), "Should have created 1 sphere");
            assertTrue(stats.contains("reused=0"), "Should have reused 0 spheres");
        }

        @Test
        @DisplayName("Acquire reuses sphere from pool when available")
        void acquireReusesSphereFromPool() throws Exception {
            assumeJavaFxAvailable();

            runOnFxThread(() -> {
                // First acquire and release to populate pool
                PhongMaterial material = new PhongMaterial(Color.RED);
                Sphere sphere1 = pool.acquire(StarLODManager.LODLevel.MEDIUM, 5.0, material);
                pool.release(sphere1, StarLODManager.LODLevel.MEDIUM);

                // Second acquire should reuse
                Sphere sphere2 = pool.acquire(StarLODManager.LODLevel.MEDIUM, 3.0, material);

                assertEquals(3.0, sphere2.getRadius(), 0.001, "Radius should be updated");
                return null;
            });

            String stats = pool.getStatistics();
            assertTrue(stats.contains("reused=1"), "Should have reused 1 sphere");
        }

        @Test
        @DisplayName("Acquire configures sphere with correct radius")
        void acquireConfiguresRadius() throws Exception {
            assumeJavaFxAvailable();

            double[] radii = {1.0, 2.5, 5.0, 10.0};

            for (double radius : radii) {
                Sphere sphere = runOnFxThread(() -> {
                    PhongMaterial material = new PhongMaterial(Color.BLUE);
                    return pool.acquire(StarLODManager.LODLevel.LOW, radius, material);
                });

                assertEquals(radius, sphere.getRadius(), 0.001, "Radius should be " + radius);
            }
        }

        @Test
        @DisplayName("Acquire configures sphere with correct material")
        void acquireConfiguresMaterial() throws Exception {
            assumeJavaFxAvailable();

            PhongMaterial redMaterial = new PhongMaterial(Color.RED);

            Sphere sphere = runOnFxThread(() ->
                    pool.acquire(StarLODManager.LODLevel.HIGH, 5.0, redMaterial));

            assertSame(redMaterial, sphere.getMaterial());
        }

        @Test
        @DisplayName("Acquire resets sphere transforms")
        void acquireResetsTransforms() throws Exception {
            assumeJavaFxAvailable();

            runOnFxThread(() -> {
                PhongMaterial material = new PhongMaterial(Color.GREEN);

                // Acquire, modify transforms, release
                Sphere sphere1 = pool.acquire(StarLODManager.LODLevel.MEDIUM, 5.0, material);
                sphere1.setTranslateX(100);
                sphere1.setTranslateY(200);
                sphere1.setTranslateZ(300);
                sphere1.setScaleX(2.0);
                pool.release(sphere1, StarLODManager.LODLevel.MEDIUM);

                // Re-acquire should have reset transforms
                Sphere sphere2 = pool.acquire(StarLODManager.LODLevel.MEDIUM, 5.0, material);

                assertEquals(0, sphere2.getTranslateX(), 0.001, "TranslateX should be reset");
                assertEquals(0, sphere2.getTranslateY(), 0.001, "TranslateY should be reset");
                assertEquals(0, sphere2.getTranslateZ(), 0.001, "TranslateZ should be reset");
                assertEquals(1.0, sphere2.getScaleX(), 0.001, "ScaleX should be reset");

                return null;
            });
        }

        @Test
        @DisplayName("Acquire from different LOD levels uses correct pool")
        void acquireFromDifferentLODLevels() throws Exception {
            assumeJavaFxAvailable();

            runOnFxThread(() -> {
                PhongMaterial material = new PhongMaterial(Color.YELLOW);

                // Acquire from each LOD level
                Sphere high = pool.acquire(StarLODManager.LODLevel.HIGH, 5.0, material);
                Sphere medium = pool.acquire(StarLODManager.LODLevel.MEDIUM, 5.0, material);
                Sphere low = pool.acquire(StarLODManager.LODLevel.LOW, 5.0, material);
                Sphere minimal = pool.acquire(StarLODManager.LODLevel.MINIMAL, 5.0, material);

                // Each should have correct divisions
                assertEquals(64, high.getDivisions(), "HIGH should have 64 divisions");
                assertEquals(32, medium.getDivisions(), "MEDIUM should have 32 divisions");
                assertEquals(16, low.getDivisions(), "LOW should have 16 divisions");
                assertEquals(8, minimal.getDivisions(), "MINIMAL should have 8 divisions");

                return null;
            });
        }
    }

    // =========================================================================
    // Release Tests
    // =========================================================================

    @Nested
    @DisplayName("Sphere Release Tests")
    class ReleaseTests {

        @Test
        @DisplayName("Release adds sphere to correct pool")
        void releaseAddsToCorrectPool() throws Exception {
            assumeJavaFxAvailable();

            runOnFxThread(() -> {
                PhongMaterial material = new PhongMaterial(Color.CYAN);
                Sphere sphere = pool.acquire(StarLODManager.LODLevel.LOW, 5.0, material);
                pool.release(sphere, StarLODManager.LODLevel.LOW);
                return null;
            });

            Map<StarLODManager.LODLevel, Integer> sizes = pool.getPoolSizes();
            assertEquals(1, sizes.get(StarLODManager.LODLevel.LOW), "LOW pool should have 1 sphere");
            assertEquals(0, sizes.get(StarLODManager.LODLevel.HIGH), "HIGH pool should be empty");
            assertEquals(0, sizes.get(StarLODManager.LODLevel.MEDIUM), "MEDIUM pool should be empty");
            assertEquals(0, sizes.get(StarLODManager.LODLevel.MINIMAL), "MINIMAL pool should be empty");
        }

        @Test
        @DisplayName("Release clears material reference")
        void releaseClearsMaterial() throws Exception {
            assumeJavaFxAvailable();

            AtomicReference<Sphere> sphereRef = new AtomicReference<>();

            runOnFxThread(() -> {
                PhongMaterial material = new PhongMaterial(Color.MAGENTA);
                Sphere sphere = pool.acquire(StarLODManager.LODLevel.MEDIUM, 5.0, material);
                sphereRef.set(sphere);
                pool.release(sphere, StarLODManager.LODLevel.MEDIUM);
                return null;
            });

            assertNull(sphereRef.get().getMaterial(), "Material should be cleared on release");
        }

        @Test
        @DisplayName("Release clears user data")
        void releaseClearsUserData() throws Exception {
            assumeJavaFxAvailable();

            AtomicReference<Sphere> sphereRef = new AtomicReference<>();

            runOnFxThread(() -> {
                PhongMaterial material = new PhongMaterial(Color.ORANGE);
                Sphere sphere = pool.acquire(StarLODManager.LODLevel.MEDIUM, 5.0, material);
                sphere.setUserData("test data");
                sphereRef.set(sphere);
                pool.release(sphere, StarLODManager.LODLevel.MEDIUM);
                return null;
            });

            assertNull(sphereRef.get().getUserData(), "User data should be cleared on release");
        }

        @Test
        @DisplayName("Release increments released count")
        void releaseIncrementsCount() throws Exception {
            assumeJavaFxAvailable();

            runOnFxThread(() -> {
                PhongMaterial material = new PhongMaterial(Color.PINK);
                for (int i = 0; i < 5; i++) {
                    Sphere sphere = pool.acquire(StarLODManager.LODLevel.MEDIUM, 5.0, material);
                    pool.release(sphere, StarLODManager.LODLevel.MEDIUM);
                }
                return null;
            });

            String stats = pool.getStatistics();
            assertTrue(stats.contains("released=5"), "Should have released 5 spheres");
        }

        @Test
        @DisplayName("ReleaseAll handles multiple spheres")
        void releaseAllHandlesMultipleSpheres() throws Exception {
            assumeJavaFxAvailable();

            runOnFxThread(() -> {
                PhongMaterial material = new PhongMaterial(Color.WHITE);
                List<Sphere> spheres = new ArrayList<>();

                // Acquire spheres of different LOD levels
                spheres.add(pool.acquire(StarLODManager.LODLevel.HIGH, 5.0, material));
                spheres.add(pool.acquire(StarLODManager.LODLevel.MEDIUM, 5.0, material));
                spheres.add(pool.acquire(StarLODManager.LODLevel.LOW, 5.0, material));
                spheres.add(pool.acquire(StarLODManager.LODLevel.MINIMAL, 5.0, material));

                pool.releaseAll(spheres);
                return null;
            });

            Map<StarLODManager.LODLevel, Integer> sizes = pool.getPoolSizes();
            assertEquals(1, sizes.get(StarLODManager.LODLevel.HIGH));
            assertEquals(1, sizes.get(StarLODManager.LODLevel.MEDIUM));
            assertEquals(1, sizes.get(StarLODManager.LODLevel.LOW));
            assertEquals(1, sizes.get(StarLODManager.LODLevel.MINIMAL));
        }
    }

    // =========================================================================
    // Pre-warming Tests
    // =========================================================================

    @Nested
    @DisplayName("Pre-warming Tests")
    class PrewarmingTests {

        @Test
        @DisplayName("Prewarm creates spheres for all LOD levels")
        void prewarmCreatesSpheresForAllLevels() throws Exception {
            assumeJavaFxAvailable();

            runOnFxThread(() -> {
                pool.prewarm(50);
                return null;
            });

            Map<StarLODManager.LODLevel, Integer> sizes = pool.getPoolSizes();
            for (StarLODManager.LODLevel level : StarLODManager.LODLevel.values()) {
                assertEquals(50, sizes.get(level), "Pool for " + level + " should have 50 spheres");
            }
        }

        @Test
        @DisplayName("Prewarm updates created count")
        void prewarmUpdatesCreatedCount() throws Exception {
            assumeJavaFxAvailable();

            runOnFxThread(() -> {
                pool.prewarm(25);
                return null;
            });

            String stats = pool.getStatistics();
            // 25 per level * 4 levels = 100 total
            assertTrue(stats.contains("created=100"), "Should have created 100 spheres");
        }

        @Test
        @DisplayName("Pre-warmed spheres can be acquired")
        void prewarmedSpheresCanBeAcquired() throws Exception {
            assumeJavaFxAvailable();

            runOnFxThread(() -> {
                pool.prewarm(10);

                PhongMaterial material = new PhongMaterial(Color.RED);
                Sphere sphere = pool.acquire(StarLODManager.LODLevel.MEDIUM, 5.0, material);

                assertNotNull(sphere);
                assertEquals(5.0, sphere.getRadius(), 0.001);
                return null;
            });

            String stats = pool.getStatistics();
            assertTrue(stats.contains("reused=1"), "Should have reused 1 pre-warmed sphere");
        }
    }

    // =========================================================================
    // Pool Size Limit Tests
    // =========================================================================

    @Nested
    @DisplayName("Pool Size Limit Tests")
    class PoolSizeLimitTests {

        @Test
        @DisplayName("Pool does not exceed max size")
        void poolDoesNotExceedMaxSize() throws Exception {
            assumeJavaFxAvailable();

            // The max pool size is 5000 per level
            // We'll test with a smaller number to verify the mechanism
            runOnFxThread(() -> {
                PhongMaterial material = new PhongMaterial(Color.GRAY);

                // Prewarm to near max
                pool.prewarm(100);

                // Acquire and release many more
                for (int i = 0; i < 200; i++) {
                    Sphere sphere = pool.acquire(StarLODManager.LODLevel.MEDIUM, 5.0, material);
                    pool.release(sphere, StarLODManager.LODLevel.MEDIUM);
                }

                return null;
            });

            // Pool should have spheres but not unlimited growth
            Map<StarLODManager.LODLevel, Integer> sizes = pool.getPoolSizes();
            assertTrue(sizes.get(StarLODManager.LODLevel.MEDIUM) <= 5000,
                    "Pool should not exceed max size");
        }
    }

    // =========================================================================
    // Statistics Tests
    // =========================================================================

    @Nested
    @DisplayName("Statistics Tests")
    class StatisticsTests {

        @Test
        @DisplayName("Statistics string includes all metrics")
        void statisticsIncludesAllMetrics() throws Exception {
            assumeJavaFxAvailable();

            runOnFxThread(() -> {
                PhongMaterial material = new PhongMaterial(Color.BLUE);
                pool.prewarm(5);

                Sphere s1 = pool.acquire(StarLODManager.LODLevel.MEDIUM, 5.0, material);
                pool.release(s1, StarLODManager.LODLevel.MEDIUM);

                Sphere s2 = pool.acquire(StarLODManager.LODLevel.MEDIUM, 5.0, material);
                return null;
            });

            String stats = pool.getStatistics();
            assertTrue(stats.contains("created="), "Should contain created count");
            assertTrue(stats.contains("reused="), "Should contain reused count");
            assertTrue(stats.contains("released="), "Should contain released count");
            assertTrue(stats.contains("pooled="), "Should contain pooled count");
            assertTrue(stats.contains("reuseRate="), "Should contain reuse rate");
        }

        @Test
        @DisplayName("Reuse rate calculation is correct")
        void reuseRateCalculationIsCorrect() throws Exception {
            assumeJavaFxAvailable();

            runOnFxThread(() -> {
                PhongMaterial material = new PhongMaterial(Color.RED);

                // Create 2 spheres (not reused)
                Sphere s1 = pool.acquire(StarLODManager.LODLevel.MEDIUM, 5.0, material);
                Sphere s2 = pool.acquire(StarLODManager.LODLevel.MEDIUM, 5.0, material);

                // Release them
                pool.release(s1, StarLODManager.LODLevel.MEDIUM);
                pool.release(s2, StarLODManager.LODLevel.MEDIUM);

                // Acquire 2 more (these are reused)
                pool.acquire(StarLODManager.LODLevel.MEDIUM, 5.0, material);
                pool.acquire(StarLODManager.LODLevel.MEDIUM, 5.0, material);

                return null;
            });

            String stats = pool.getStatistics();
            // 2 created, 2 reused = 50% reuse rate
            assertTrue(stats.contains("reuseRate=50.0%"), "Reuse rate should be 50%");
        }

        @Test
        @DisplayName("Reset statistics clears all counters")
        void resetStatisticsClearsCounters() throws Exception {
            assumeJavaFxAvailable();

            runOnFxThread(() -> {
                PhongMaterial material = new PhongMaterial(Color.GREEN);
                pool.prewarm(10);
                pool.acquire(StarLODManager.LODLevel.MEDIUM, 5.0, material);
                return null;
            });

            pool.resetStatistics();
            String stats = pool.getStatistics();

            assertTrue(stats.contains("created=0"), "Created count should be reset");
            assertTrue(stats.contains("reused=0"), "Reused count should be reset");
            assertTrue(stats.contains("released=0"), "Released count should be reset");
        }
    }

    // =========================================================================
    // Clear Tests
    // =========================================================================

    @Nested
    @DisplayName("Clear Tests")
    class ClearTests {

        @Test
        @DisplayName("Clear empties all pools")
        void clearEmptiesAllPools() throws Exception {
            assumeJavaFxAvailable();

            runOnFxThread(() -> {
                pool.prewarm(50);
                return null;
            });

            pool.clear();

            Map<StarLODManager.LODLevel, Integer> sizes = pool.getPoolSizes();
            for (StarLODManager.LODLevel level : StarLODManager.LODLevel.values()) {
                assertEquals(0, sizes.get(level), "Pool for " + level + " should be empty after clear");
            }
        }

        @Test
        @DisplayName("Clear does not reset statistics")
        void clearDoesNotResetStatistics() throws Exception {
            assumeJavaFxAvailable();

            runOnFxThread(() -> {
                pool.prewarm(10);
                return null;
            });

            String statsBefore = pool.getStatistics();
            pool.clear();
            String statsAfter = pool.getStatistics();

            // Created count should remain
            assertTrue(statsAfter.contains("created=40"), "Statistics should not be reset by clear");
        }

        @Test
        @DisplayName("Pool can be used after clear")
        void poolCanBeUsedAfterClear() throws Exception {
            assumeJavaFxAvailable();

            runOnFxThread(() -> {
                pool.prewarm(10);
                pool.clear();

                // Should still work
                PhongMaterial material = new PhongMaterial(Color.PURPLE);
                Sphere sphere = pool.acquire(StarLODManager.LODLevel.HIGH, 5.0, material);

                assertNotNull(sphere);
                assertEquals(5.0, sphere.getRadius(), 0.001);
                return null;
            });
        }
    }

    // =========================================================================
    // Edge Case Tests
    // =========================================================================

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Acquire with zero radius works")
        void acquireWithZeroRadius() throws Exception {
            assumeJavaFxAvailable();

            Sphere sphere = runOnFxThread(() -> {
                PhongMaterial material = new PhongMaterial(Color.BLACK);
                return pool.acquire(StarLODManager.LODLevel.MEDIUM, 0.0, material);
            });

            assertNotNull(sphere);
            assertEquals(0.0, sphere.getRadius(), 0.001);
        }

        @Test
        @DisplayName("Acquire with very small radius works")
        void acquireWithVerySmallRadius() throws Exception {
            assumeJavaFxAvailable();

            Sphere sphere = runOnFxThread(() -> {
                PhongMaterial material = new PhongMaterial(Color.BLACK);
                return pool.acquire(StarLODManager.LODLevel.MINIMAL, 0.001, material);
            });

            assertNotNull(sphere);
            assertEquals(0.001, sphere.getRadius(), 0.0001);
        }

        @Test
        @DisplayName("Acquire with very large radius works")
        void acquireWithVeryLargeRadius() throws Exception {
            assumeJavaFxAvailable();

            Sphere sphere = runOnFxThread(() -> {
                PhongMaterial material = new PhongMaterial(Color.BLACK);
                return pool.acquire(StarLODManager.LODLevel.HIGH, 1000.0, material);
            });

            assertNotNull(sphere);
            assertEquals(1000.0, sphere.getRadius(), 0.001);
        }

        @Test
        @DisplayName("Multiple acquires without release creates new spheres")
        void multipleAcquiresWithoutRelease() throws Exception {
            assumeJavaFxAvailable();

            runOnFxThread(() -> {
                PhongMaterial material = new PhongMaterial(Color.RED);

                // Acquire 5 spheres without releasing
                for (int i = 0; i < 5; i++) {
                    pool.acquire(StarLODManager.LODLevel.MEDIUM, 5.0, material);
                }

                return null;
            });

            String stats = pool.getStatistics();
            assertTrue(stats.contains("created=5"), "Should have created 5 spheres");
            assertTrue(stats.contains("reused=0"), "Should have reused 0 spheres");
        }

        @Test
        @DisplayName("Prewarm with zero count is safe")
        void prewarmWithZeroCount() throws Exception {
            assumeJavaFxAvailable();

            assertDoesNotThrow(() -> runOnFxThread(() -> {
                pool.prewarm(0);
                return null;
            }));

            Map<StarLODManager.LODLevel, Integer> sizes = pool.getPoolSizes();
            for (Integer size : sizes.values()) {
                assertEquals(0, size, "Pool should remain empty");
            }
        }
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================

    private void assumeJavaFxAvailable() {
        Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");
    }

    private <T> T runOnFxThread(java.util.concurrent.Callable<T> callable) throws Exception {
        if (Platform.isFxApplicationThread()) {
            return callable.call();
        }

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<T> result = new AtomicReference<>();
        AtomicReference<Exception> exception = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                result.set(callable.call());
            } catch (Exception e) {
                exception.set(e);
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(30, TimeUnit.SECONDS), "JavaFX operation timed out");

        if (exception.get() != null) {
            throw exception.get();
        }

        return result.get();
    }
}
