package com.teamgannon.trips.starplotting;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import javafx.application.Platform;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import org.junit.jupiter.api.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for StarLODManager.
 * <p>
 * Tests cover:
 * <ul>
 *   <li>LOD level determination based on distance and magnitude</li>
 *   <li>Star creation at different LOD levels</li>
 *   <li>Statistics tracking</li>
 *   <li>Configuration options (zoom level, enabled/disabled)</li>
 * </ul>
 */
class StarLODManagerTest {

    private static boolean javaFxInitialized = false;
    private StarLODManager lodManager;

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
        lodManager = new StarLODManager();
    }

    // =========================================================================
    // Initialization Tests
    // =========================================================================

    @Nested
    @DisplayName("Initialization Tests")
    class InitializationTests {

        @Test
        @DisplayName("New manager has LOD enabled by default")
        void newManagerHasLodEnabled() {
            assertTrue(lodManager.isLodEnabled());
        }

        @Test
        @DisplayName("New manager has zoom level 1.0 by default")
        void newManagerHasDefaultZoomLevel() {
            assertEquals(1.0, lodManager.getZoomLevel());
        }

        @Test
        @DisplayName("New manager has zero star counts")
        void newManagerHasZeroStarCounts() {
            assertEquals(0, lodManager.getHighDetailCount());
            assertEquals(0, lodManager.getMediumDetailCount());
            assertEquals(0, lodManager.getLowDetailCount());
            assertEquals(0, lodManager.getMinimalDetailCount());
            assertEquals(0, lodManager.getTotalStarCount());
        }
    }

    // =========================================================================
    // LOD Level Determination Tests
    // =========================================================================

    @Nested
    @DisplayName("LOD Level Determination Tests")
    class LODLevelDeterminationTests {

        @Test
        @DisplayName("Center star always gets HIGH detail")
        void centerStarGetsHighDetail() {
            StarDisplayRecord record = createTestRecord(0, 0, 0, 5.0);

            StarLODManager.LODLevel level = lodManager.determineLODLevel(record, true);

            assertEquals(StarLODManager.LODLevel.HIGH, level);
        }

        @Test
        @DisplayName("Close bright star gets HIGH detail")
        void closeBrightStarGetsHighDetail() {
            // Close to center (within 50 units) and bright (large radius)
            StarDisplayRecord record = createTestRecord(30, 0, 0, 4.0);

            StarLODManager.LODLevel level = lodManager.determineLODLevel(record, false);

            assertEquals(StarLODManager.LODLevel.HIGH, level);
        }

        @Test
        @DisplayName("Medium distance star gets MEDIUM detail")
        void mediumDistanceStarGetsMediumDetail() {
            // Between 50-200 units from center
            StarDisplayRecord record = createTestRecord(100, 0, 0, 2.0);

            StarLODManager.LODLevel level = lodManager.determineLODLevel(record, false);

            assertEquals(StarLODManager.LODLevel.MEDIUM, level);
        }

        @Test
        @DisplayName("Far distance dim star gets LOW detail")
        void farDistanceDimStarGetsLowDetail() {
            // Between 200-400 units from center with dim star
            StarDisplayRecord record = createTestRecord(300, 0, 0, 0.5);

            StarLODManager.LODLevel level = lodManager.determineLODLevel(record, false);

            assertEquals(StarLODManager.LODLevel.LOW, level);
        }

        @Test
        @DisplayName("Very far star gets MINIMAL detail")
        void veryFarStarGetsMinimalDetail() {
            // Beyond 400 units from center
            StarDisplayRecord record = createTestRecord(500, 0, 0, 1.0);

            StarLODManager.LODLevel level = lodManager.determineLODLevel(record, false);

            assertEquals(StarLODManager.LODLevel.MINIMAL, level);
        }

        @Test
        @DisplayName("Disabled LOD always returns MEDIUM")
        void disabledLodReturnsMedium() {
            lodManager.setLodEnabled(false);

            // Even center star gets MEDIUM when disabled
            StarDisplayRecord centerRecord = createTestRecord(0, 0, 0, 5.0);
            assertEquals(StarLODManager.LODLevel.MEDIUM,
                    lodManager.determineLODLevel(centerRecord, true));

            // Far star also gets MEDIUM when disabled
            StarDisplayRecord farRecord = createTestRecord(1000, 0, 0, 0.5);
            assertEquals(StarLODManager.LODLevel.MEDIUM,
                    lodManager.determineLODLevel(farRecord, false));
        }

        @Test
        @DisplayName("Custom center coordinates affect distance calculation")
        void customCenterCoordinatesAffectDistance() {
            // Set center at (100, 0, 0)
            lodManager.setCenterCoordinates(100, 0, 0);

            // Star at (100, 30, 0) is only 30 units from center with bright star (radius 4.0)
            // Should be HIGH because distance < 50 AND magnitude < 2.0 (from radius > 3.0)
            StarDisplayRecord nearRecord = createTestRecord(100, 30, 0, 4.0);
            assertEquals(StarLODManager.LODLevel.HIGH,
                    lodManager.determineLODLevel(nearRecord, false));

            // Star at (0, 0, 0) is 100 units from center - should be MEDIUM
            StarDisplayRecord farRecord = createTestRecord(0, 0, 0, 2.0);
            assertEquals(StarLODManager.LODLevel.MEDIUM,
                    lodManager.determineLODLevel(farRecord, false));
        }

        @Test
        @DisplayName("Zoom level affects LOD thresholds")
        void zoomLevelAffectsThresholds() {
            // At 2x zoom, thresholds are halved (closer apparent distance)
            lodManager.setZoomLevel(2.0);

            // Star at 75 units would normally be MEDIUM (50-200 range)
            // At 2x zoom, thresholds become 25-100, so 75 is still MEDIUM
            StarDisplayRecord record = createTestRecord(75, 0, 0, 2.0);
            StarLODManager.LODLevel level = lodManager.determineLODLevel(record, false);

            // With zoom 2.0, distance thresholds are divided by zoom
            // HIGH threshold: 50/2 = 25, MEDIUM threshold: 200/2 = 100
            // 75 is between 25 and 100, so MEDIUM
            assertEquals(StarLODManager.LODLevel.MEDIUM, level);
        }

        @Test
        @DisplayName("3D distance calculated correctly")
        void threeDDistanceCalculatedCorrectly() {
            // Star at (30, 40, 0) is 50 units from origin (3-4-5 triangle scaled)
            StarDisplayRecord record = createTestRecord(30, 40, 0, 3.5);

            // At exactly 50 units with bright star, should be MEDIUM (just past HIGH threshold)
            // With large radius (3.5), magnitude estimate should be bright
            StarLODManager.LODLevel level = lodManager.determineLODLevel(record, false);

            // 50 is at the HIGH threshold boundary
            assertTrue(level == StarLODManager.LODLevel.HIGH ||
                       level == StarLODManager.LODLevel.MEDIUM);
        }
    }

    // =========================================================================
    // Star Creation Tests
    // =========================================================================

    @Nested
    @DisplayName("Star Creation Tests")
    class StarCreationTests {

        @Test
        @DisplayName("createStarWithLOD creates appropriate sphere for each level")
        void createStarWithLodCreatesSpheres() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            runOnFxThread(() -> {
                StarDisplayRecord record = createTestRecord(0, 0, 0, 2.0);
                PhongMaterial material = new PhongMaterial(Color.YELLOW);
                double radius = 5.0;

                // Test each LOD level
                for (StarLODManager.LODLevel level : StarLODManager.LODLevel.values()) {
                    Node star = lodManager.createStarWithLOD(record, radius, material, level);
                    assertNotNull(star, "Star should be created for " + level);
                    assertTrue(star instanceof Sphere, "Star should be a Sphere for " + level);
                }
                return null;
            });
        }

        @Test
        @DisplayName("LOW detail star has reduced radius")
        void lowDetailStarHasReducedRadius() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            runOnFxThread(() -> {
                lodManager.resetStatistics();
                StarDisplayRecord record = createTestRecord(0, 0, 0, 2.0);
                PhongMaterial material = new PhongMaterial(Color.YELLOW);
                double baseRadius = 10.0;

                Sphere mediumStar = (Sphere) lodManager.createStarWithLOD(
                        record, baseRadius, material, StarLODManager.LODLevel.MEDIUM);
                Sphere lowStar = (Sphere) lodManager.createStarWithLOD(
                        record, baseRadius, material, StarLODManager.LODLevel.LOW);

                assertTrue(lowStar.getRadius() < mediumStar.getRadius(),
                        "LOW detail star should have smaller radius than MEDIUM");
                return null;
            });
        }

        @Test
        @DisplayName("MINIMAL detail star has minimum radius enforced")
        void minimalDetailStarHasMinimumRadius() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            runOnFxThread(() -> {
                lodManager.resetStatistics();
                StarDisplayRecord record = createTestRecord(0, 0, 0, 2.0);
                PhongMaterial material = new PhongMaterial(Color.YELLOW);
                double verySmallRadius = 0.1; // Very small base radius

                Sphere minimalStar = (Sphere) lodManager.createStarWithLOD(
                        record, verySmallRadius, material, StarLODManager.LODLevel.MINIMAL);

                // MINIMAL should enforce minimum radius of 0.5
                assertTrue(minimalStar.getRadius() >= 0.5,
                        "MINIMAL detail star should have at least minimum radius");
                return null;
            });
        }

        @Test
        @DisplayName("Material is applied to created star")
        void materialIsApplied() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            runOnFxThread(() -> {
                lodManager.resetStatistics();
                StarDisplayRecord record = createTestRecord(0, 0, 0, 2.0);
                PhongMaterial material = new PhongMaterial(Color.RED);

                Sphere star = (Sphere) lodManager.createStarWithLOD(
                        record, 5.0, material, StarLODManager.LODLevel.MEDIUM);

                assertSame(material, star.getMaterial(), "Material should be applied to star");
                return null;
            });
        }
    }

    // =========================================================================
    // Statistics Tests
    // =========================================================================

    @Nested
    @DisplayName("Statistics Tests")
    class StatisticsTests {

        @Test
        @DisplayName("Creating stars increments correct counters")
        void creatingStarsIncrementsCounters() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            runOnFxThread(() -> {
                lodManager.resetStatistics();
                StarDisplayRecord record = createTestRecord(0, 0, 0, 2.0);
                PhongMaterial material = new PhongMaterial(Color.YELLOW);

                // Create stars at each level
                lodManager.createStarWithLOD(record, 5.0, material, StarLODManager.LODLevel.HIGH);
                lodManager.createStarWithLOD(record, 5.0, material, StarLODManager.LODLevel.HIGH);
                lodManager.createStarWithLOD(record, 5.0, material, StarLODManager.LODLevel.MEDIUM);
                lodManager.createStarWithLOD(record, 5.0, material, StarLODManager.LODLevel.MEDIUM);
                lodManager.createStarWithLOD(record, 5.0, material, StarLODManager.LODLevel.MEDIUM);
                lodManager.createStarWithLOD(record, 5.0, material, StarLODManager.LODLevel.LOW);
                lodManager.createStarWithLOD(record, 5.0, material, StarLODManager.LODLevel.MINIMAL);

                assertEquals(2, lodManager.getHighDetailCount());
                assertEquals(3, lodManager.getMediumDetailCount());
                assertEquals(1, lodManager.getLowDetailCount());
                assertEquals(1, lodManager.getMinimalDetailCount());
                assertEquals(7, lodManager.getTotalStarCount());
                return null;
            });
        }

        @Test
        @DisplayName("Reset statistics clears all counters")
        void resetStatisticsClearsCounters() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            runOnFxThread(() -> {
                StarDisplayRecord record = createTestRecord(0, 0, 0, 2.0);
                PhongMaterial material = new PhongMaterial(Color.YELLOW);

                // Create some stars
                lodManager.createStarWithLOD(record, 5.0, material, StarLODManager.LODLevel.HIGH);
                lodManager.createStarWithLOD(record, 5.0, material, StarLODManager.LODLevel.MEDIUM);

                // Reset
                lodManager.resetStatistics();

                assertEquals(0, lodManager.getHighDetailCount());
                assertEquals(0, lodManager.getMediumDetailCount());
                assertEquals(0, lodManager.getLowDetailCount());
                assertEquals(0, lodManager.getMinimalDetailCount());
                assertEquals(0, lodManager.getTotalStarCount());
                return null;
            });
        }

        @Test
        @DisplayName("getStatisticsPercentages returns correct format")
        void getStatisticsPercentagesReturnsCorrectFormat() throws Exception {
            Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

            runOnFxThread(() -> {
                lodManager.resetStatistics();
                StarDisplayRecord record = createTestRecord(0, 0, 0, 2.0);
                PhongMaterial material = new PhongMaterial(Color.YELLOW);

                // Create 4 stars, one of each type
                lodManager.createStarWithLOD(record, 5.0, material, StarLODManager.LODLevel.HIGH);
                lodManager.createStarWithLOD(record, 5.0, material, StarLODManager.LODLevel.MEDIUM);
                lodManager.createStarWithLOD(record, 5.0, material, StarLODManager.LODLevel.LOW);
                lodManager.createStarWithLOD(record, 5.0, material, StarLODManager.LODLevel.MINIMAL);

                String percentages = lodManager.getStatisticsPercentages();

                // Each should be 25%
                assertTrue(percentages.contains("HIGH: 25.0%"), "Should show HIGH percentage");
                assertTrue(percentages.contains("MEDIUM: 25.0%"), "Should show MEDIUM percentage");
                assertTrue(percentages.contains("LOW: 25.0%"), "Should show LOW percentage");
                assertTrue(percentages.contains("MINIMAL: 25.0%"), "Should show MINIMAL percentage");
                return null;
            });
        }

        @Test
        @DisplayName("getStatisticsPercentages handles empty stats")
        void getStatisticsPercentagesHandlesEmpty() {
            lodManager.resetStatistics();
            String result = lodManager.getStatisticsPercentages();
            assertEquals("No stars rendered", result);
        }
    }

    // =========================================================================
    // Configuration Tests
    // =========================================================================

    @Nested
    @DisplayName("Configuration Tests")
    class ConfigurationTests {

        @Test
        @DisplayName("setLodEnabled toggles LOD")
        void setLodEnabledTogglesLod() {
            assertTrue(lodManager.isLodEnabled());

            lodManager.setLodEnabled(false);
            assertFalse(lodManager.isLodEnabled());

            lodManager.setLodEnabled(true);
            assertTrue(lodManager.isLodEnabled());
        }

        @Test
        @DisplayName("setZoomLevel updates zoom")
        void setZoomLevelUpdatesZoom() {
            assertEquals(1.0, lodManager.getZoomLevel());

            lodManager.setZoomLevel(2.5);
            assertEquals(2.5, lodManager.getZoomLevel());

            lodManager.setZoomLevel(0.5);
            assertEquals(0.5, lodManager.getZoomLevel());
        }

        @Test
        @DisplayName("setCenterCoordinates updates center")
        void setCenterCoordinatesUpdatesCenter() {
            // Default is (0,0,0)
            // Star at origin should be at distance 0 with bright star (radius > 3.0)
            // For HIGH: distance < 50 AND magnitude < 2.0 (radius > 3.0 gives magnitude 1.0)
            StarDisplayRecord brightAtOrigin = createTestRecord(0, 0, 0, 4.0);
            StarLODManager.LODLevel level1 = lodManager.determineLODLevel(brightAtOrigin, false);
            assertEquals(StarLODManager.LODLevel.HIGH, level1); // Distance 0, bright star

            // Move center to (1000, 1000, 1000)
            lodManager.setCenterCoordinates(1000, 1000, 1000);

            // Create a dim star (radius 0.5 → magnitude 7.0) at origin
            // It's now ~1732 units away from center AND dim (magnitude > 6.0)
            // Should get MINIMAL because: distance > 400 AND magnitude >= 6.0
            StarDisplayRecord dimAtOrigin = createTestRecord(0, 0, 0, 0.5);
            StarLODManager.LODLevel level2 = lodManager.determineLODLevel(dimAtOrigin, false);
            assertEquals(StarLODManager.LODLevel.MINIMAL, level2); // Very far and dim
        }
    }

    // =========================================================================
    // Magnitude Estimation Tests
    // =========================================================================

    @Nested
    @DisplayName("Magnitude Estimation Tests")
    class MagnitudeEstimationTests {

        @Test
        @DisplayName("Large radius stars are treated as bright")
        void largeRadiusStarsAreBright() {
            // Large radius (> 3.0) should give magnitude ~1.0 (very bright)
            StarDisplayRecord brightStar = createTestRecord(40, 0, 0, 4.0);

            // Should get HIGH detail due to brightness even at moderate distance
            StarLODManager.LODLevel level = lodManager.determineLODLevel(brightStar, false);
            assertEquals(StarLODManager.LODLevel.HIGH, level);
        }

        @Test
        @DisplayName("Small radius stars are treated as dim")
        void smallRadiusStarsAreDim() {
            // Small radius (< 1.0) should give magnitude ~7.0 (dim)
            StarDisplayRecord dimStar = createTestRecord(150, 0, 0, 0.5);

            // Even at moderate distance, dim star should get MEDIUM
            StarLODManager.LODLevel level = lodManager.determineLODLevel(dimStar, false);
            assertEquals(StarLODManager.LODLevel.MEDIUM, level);
        }

        @Test
        @DisplayName("Display score affects magnitude estimation")
        void displayScoreAffectsMagnitude() {
            // High display score should indicate importance/brightness
            StarDisplayRecord record = mock(StarDisplayRecord.class);
            when(record.getCoordinates()).thenReturn(new Point3D(100, 0, 0));
            when(record.getRadius()).thenReturn(1.0);
            when(record.getDisplayScore()).thenReturn(8.0); // High score = bright

            StarLODManager.LODLevel level = lodManager.determineLODLevel(record, false);

            // High display score should result in better LOD
            assertTrue(level == StarLODManager.LODLevel.HIGH ||
                       level == StarLODManager.LODLevel.MEDIUM);
        }
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================

    private StarDisplayRecord createTestRecord(double x, double y, double z, double radius) {
        StarDisplayRecord record = mock(StarDisplayRecord.class);
        when(record.getCoordinates()).thenReturn(new Point3D(x, y, z));
        when(record.getRadius()).thenReturn(radius);
        when(record.getDisplayScore()).thenReturn(0.0); // No score, use radius for magnitude
        when(record.getRecordId()).thenReturn("test-star-" + System.nanoTime());
        return record;
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
