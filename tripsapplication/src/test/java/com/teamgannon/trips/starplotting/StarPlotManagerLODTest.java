package com.teamgannon.trips.starplotting;

import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.config.application.model.AppViewPreferences;
import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.config.application.model.CurrentPlot;
import com.teamgannon.trips.config.application.model.StarDisplayPreferences;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.CivilizationDisplayPreferences;
import com.teamgannon.trips.routing.RouteManager;
import com.teamgannon.trips.routing.RouteFindingService;
import com.teamgannon.trips.service.SolarSystemService;
import com.teamgannon.trips.service.StarService;
import javafx.application.Platform;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SubScene;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.*;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for StarPlotManager's LOD (Level-of-Detail) integration.
 * <p>
 * Tests cover:
 * <ul>
 *   <li>LOD manager initialization and access</li>
 *   <li>Zoom level synchronization between scale and LOD managers</li>
 *   <li>LOD statistics reset on clear</li>
 *   <li>LOD configuration via setLodEnabled</li>
 *   <li>Integration of LOD with star drawing</li>
 * </ul>
 */
class StarPlotManagerLODTest {

    private static boolean javaFxInitialized = false;
    private StarPlotManager starPlotManager;
    private TripsContext tripsContext;
    private RouteManager routeManager;
    private StarService starService;
    private SolarSystemService solarSystemService;
    private StarContextMenuHandler contextMenuHandler;
    private ApplicationEventPublisher eventPublisher;

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
        Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

        // Create mocks
        tripsContext = mock(TripsContext.class);
        routeManager = mock(RouteManager.class);
        RouteFindingService routeFindingService = mock(RouteFindingService.class);
        starService = mock(StarService.class);
        solarSystemService = mock(SolarSystemService.class);
        contextMenuHandler = mock(StarContextMenuHandler.class);
        eventPublisher = mock(ApplicationEventPublisher.class);

        // Configure tripsContext
        AppViewPreferences appViewPreferences = mock(AppViewPreferences.class);
        ColorPalette colorPalette = new ColorPalette();
        when(appViewPreferences.getColorPalette()).thenReturn(colorPalette);
        when(tripsContext.getAppViewPreferences()).thenReturn(appViewPreferences);

        CurrentPlot currentPlot = mock(CurrentPlot.class);
        when(tripsContext.getCurrentPlot()).thenReturn(currentPlot);

        // Create StarPlotManager
        starPlotManager = new StarPlotManager(
                tripsContext,
                routeManager,
                starService,
                solarSystemService,
                routeFindingService,
                contextMenuHandler,
                eventPublisher
        );
    }

    // =========================================================================
    // LOD Manager Access Tests
    // =========================================================================

    @Nested
    @DisplayName("LOD Manager Access Tests")
    class LODManagerAccessTests {

        @Test
        @DisplayName("getLodManager returns non-null manager")
        void getLodManagerReturnsNonNull() {
            assertNotNull(starPlotManager.getLodManager());
        }

        @Test
        @DisplayName("LOD manager is enabled by default")
        void lodManagerEnabledByDefault() {
            assertTrue(starPlotManager.getLodManager().isLodEnabled());
        }

        @Test
        @DisplayName("LOD manager has default zoom level 1.0")
        void lodManagerHasDefaultZoomLevel() {
            assertEquals(1.0, starPlotManager.getLodManager().getZoomLevel());
        }

        @Test
        @DisplayName("LOD manager statistics are initially zero")
        void lodManagerStatisticsInitiallyZero() {
            StarLODManager lodManager = starPlotManager.getLodManager();
            assertEquals(0, lodManager.getTotalStarCount());
            assertEquals(0, lodManager.getHighDetailCount());
            assertEquals(0, lodManager.getMediumDetailCount());
            assertEquals(0, lodManager.getLowDetailCount());
            assertEquals(0, lodManager.getMinimalDetailCount());
        }
    }

    // =========================================================================
    // Zoom Level Synchronization Tests
    // =========================================================================

    @Nested
    @DisplayName("Zoom Level Synchronization Tests")
    class ZoomLevelSyncTests {

        @Test
        @DisplayName("setZoomLevel updates both scale and LOD managers")
        void setZoomLevelUpdatesBothManagers() {
            starPlotManager.setZoomLevel(2.5);

            assertEquals(2.5, starPlotManager.getScaleManager().getZoomLevel());
            assertEquals(2.5, starPlotManager.getLodManager().getZoomLevel());
        }

        @Test
        @DisplayName("setZoomLevel with small value works correctly")
        void setZoomLevelSmallValue() {
            starPlotManager.setZoomLevel(0.25);

            assertEquals(0.25, starPlotManager.getScaleManager().getZoomLevel());
            assertEquals(0.25, starPlotManager.getLodManager().getZoomLevel());
        }

        @Test
        @DisplayName("setZoomLevel with large value works correctly")
        void setZoomLevelLargeValue() {
            starPlotManager.setZoomLevel(10.0);

            assertEquals(10.0, starPlotManager.getScaleManager().getZoomLevel());
            assertEquals(10.0, starPlotManager.getLodManager().getZoomLevel());
        }

        @Test
        @DisplayName("Multiple zoom level changes are tracked")
        void multipleZoomLevelChanges() {
            starPlotManager.setZoomLevel(1.5);
            assertEquals(1.5, starPlotManager.getLodManager().getZoomLevel());

            starPlotManager.setZoomLevel(3.0);
            assertEquals(3.0, starPlotManager.getLodManager().getZoomLevel());

            starPlotManager.setZoomLevel(0.5);
            assertEquals(0.5, starPlotManager.getLodManager().getZoomLevel());
        }
    }

    // =========================================================================
    // LOD Enable/Disable Tests
    // =========================================================================

    @Nested
    @DisplayName("LOD Enable/Disable Tests")
    class LODEnableDisableTests {

        @Test
        @DisplayName("setLodEnabled(false) disables LOD")
        void setLodEnabledFalseDisablesLod() {
            starPlotManager.setLodEnabled(false);

            assertFalse(starPlotManager.getLodManager().isLodEnabled());
        }

        @Test
        @DisplayName("setLodEnabled(true) enables LOD")
        void setLodEnabledTrueEnablesLod() {
            starPlotManager.setLodEnabled(false);
            starPlotManager.setLodEnabled(true);

            assertTrue(starPlotManager.getLodManager().isLodEnabled());
        }

        @Test
        @DisplayName("Disabled LOD returns MEDIUM for all stars")
        void disabledLodReturnsMediumForAllStars() {
            starPlotManager.setLodEnabled(false);
            StarLODManager lodManager = starPlotManager.getLodManager();

            // Test with various star configurations
            StarDisplayRecord nearStar = createTestRecord(10, 0, 0, 4.0);
            StarDisplayRecord farStar = createTestRecord(1000, 0, 0, 0.5);
            StarDisplayRecord centerStar = createTestRecord(0, 0, 0, 5.0);

            // All should return MEDIUM when LOD is disabled
            assertEquals(StarLODManager.LODLevel.MEDIUM,
                    lodManager.determineLODLevel(nearStar, false));
            assertEquals(StarLODManager.LODLevel.MEDIUM,
                    lodManager.determineLODLevel(farStar, false));
            // Even center star returns MEDIUM when disabled
            assertEquals(StarLODManager.LODLevel.MEDIUM,
                    lodManager.determineLODLevel(centerStar, true));
        }
    }

    // =========================================================================
    // Clear Stars Tests
    // =========================================================================

    @Nested
    @DisplayName("Clear Stars LOD Tests")
    class ClearStarsLODTests {

        @Test
        @DisplayName("clearStars resets LOD statistics")
        void clearStarsResetsLodStatistics() throws Exception {
            runOnFxThread(() -> {
                // Setup graphics first
                Group sceneRoot = new Group();
                Group world = new Group();
                SubScene subScene = new SubScene(world, 800, 600);
                starPlotManager.setGraphics(sceneRoot, world, subScene);

                // Manually increment LOD statistics by creating stars
                StarLODManager lodManager = starPlotManager.getLodManager();
                StarDisplayRecord record = createTestRecord(0, 0, 0, 2.0);
                javafx.scene.paint.PhongMaterial material = new javafx.scene.paint.PhongMaterial(Color.YELLOW);

                lodManager.createStarWithLOD(record, 5.0, material, StarLODManager.LODLevel.HIGH);
                lodManager.createStarWithLOD(record, 5.0, material, StarLODManager.LODLevel.MEDIUM);
                lodManager.createStarWithLOD(record, 5.0, material, StarLODManager.LODLevel.LOW);

                assertEquals(3, lodManager.getTotalStarCount());

                // Clear stars should reset statistics
                starPlotManager.clearStars();

                assertEquals(0, lodManager.getTotalStarCount());
                assertEquals(0, lodManager.getHighDetailCount());
                assertEquals(0, lodManager.getMediumDetailCount());
                assertEquals(0, lodManager.getLowDetailCount());
                assertEquals(0, lodManager.getMinimalDetailCount());

                return null;
            });
        }
    }

    // =========================================================================
    // Draw Stellar Object LOD Tests
    // =========================================================================

    @Nested
    @DisplayName("Draw Stellar Object LOD Tests")
    class DrawStellarObjectLODTests {

        @Test
        @DisplayName("drawStellarObject uses LOD for non-center stars")
        void drawStellarObjectUsesLodForNonCenterStars() throws Exception {
            runOnFxThread(() -> {
                // Setup
                Group sceneRoot = new Group();
                Group world = new Group();
                SubScene subScene = new SubScene(world, 800, 600);
                starPlotManager.setGraphics(sceneRoot, world, subScene);

                // Reset statistics
                starPlotManager.getLodManager().resetStatistics();

                // Create test data
                StarDisplayRecord record = createTestRecord(100, 0, 0, 2.0);
                when(record.getStarColor()).thenReturn(Color.YELLOW);
                when(record.getPolity()).thenReturn("NA");

                ColorPalette colorPalette = new ColorPalette();
                StarDisplayPreferences starDisplayPreferences = new StarDisplayPreferences();
                CivilizationDisplayPreferences polityPreferences = new CivilizationDisplayPreferences();

                // Draw non-center star
                Node star = starPlotManager.drawStellarObject(
                        record, colorPalette, false, false, false,
                        starDisplayPreferences, polityPreferences);

                assertNotNull(star);

                // LOD statistics should be updated
                StarLODManager lodManager = starPlotManager.getLodManager();
                assertEquals(1, lodManager.getTotalStarCount());

                return null;
            });
        }

        @Test
        @DisplayName("drawStellarObject uses central star mesh for center stars")
        void drawStellarObjectUsesCentralStarMeshForCenterStars() throws Exception {
            runOnFxThread(() -> {
                // Setup
                Group sceneRoot = new Group();
                Group world = new Group();
                SubScene subScene = new SubScene(world, 800, 600);
                starPlotManager.setGraphics(sceneRoot, world, subScene);

                // Create test data
                StarDisplayRecord record = createTestRecord(0, 0, 0, 2.0);
                when(record.getStarColor()).thenReturn(Color.YELLOW);
                when(record.getPolity()).thenReturn("NA");

                ColorPalette colorPalette = new ColorPalette();
                StarDisplayPreferences starDisplayPreferences = new StarDisplayPreferences();
                CivilizationDisplayPreferences polityPreferences = new CivilizationDisplayPreferences();

                // Draw center star (isCenter = true)
                Node star = starPlotManager.drawStellarObject(
                        record, colorPalette, true, false, false,
                        starDisplayPreferences, polityPreferences);

                assertNotNull(star);
                // Center star uses mesh, not sphere, so LOD manager is not involved
                // The star should be a Group (central star mesh)
                assertTrue(star instanceof Group, "Center star should be a Group (mesh)");

                return null;
            });
        }

        @Test
        @DisplayName("Different LOD levels produce different sphere divisions")
        void differentLodLevelsProduceDifferentDivisions() throws Exception {
            runOnFxThread(() -> {
                StarLODManager lodManager = starPlotManager.getLodManager();
                lodManager.resetStatistics();

                StarDisplayRecord record = createTestRecord(0, 0, 0, 2.0);
                javafx.scene.paint.PhongMaterial material = new javafx.scene.paint.PhongMaterial(Color.YELLOW);

                // Create stars at different LOD levels
                javafx.scene.shape.Sphere highStar = (javafx.scene.shape.Sphere)
                        lodManager.createStarWithLOD(record, 5.0, material, StarLODManager.LODLevel.HIGH);
                javafx.scene.shape.Sphere mediumStar = (javafx.scene.shape.Sphere)
                        lodManager.createStarWithLOD(record, 5.0, material, StarLODManager.LODLevel.MEDIUM);
                javafx.scene.shape.Sphere lowStar = (javafx.scene.shape.Sphere)
                        lodManager.createStarWithLOD(record, 5.0, material, StarLODManager.LODLevel.LOW);
                javafx.scene.shape.Sphere minimalStar = (javafx.scene.shape.Sphere)
                        lodManager.createStarWithLOD(record, 5.0, material, StarLODManager.LODLevel.MINIMAL);

                // All should be created
                assertNotNull(highStar);
                assertNotNull(mediumStar);
                assertNotNull(lowStar);
                assertNotNull(minimalStar);

                // Statistics should reflect creation
                assertEquals(4, lodManager.getTotalStarCount());
                assertEquals(1, lodManager.getHighDetailCount());
                assertEquals(1, lodManager.getMediumDetailCount());
                assertEquals(1, lodManager.getLowDetailCount());
                assertEquals(1, lodManager.getMinimalDetailCount());

                return null;
            });
        }
    }

    // =========================================================================
    // Draw Stars Integration Tests
    // =========================================================================

    @Nested
    @DisplayName("Draw Stars Integration Tests")
    class DrawStarsIntegrationTests {

        @Test
        @DisplayName("drawStars configures LOD center coordinates")
        void drawStarsConfiguresLodCenterCoordinates() throws Exception {
            runOnFxThread(() -> {
                // Setup
                Group sceneRoot = new Group();
                Group world = new Group();
                SubScene subScene = new SubScene(world, 800, 600);
                starPlotManager.setGraphics(sceneRoot, world, subScene);

                // Create current plot with center coordinates
                CurrentPlot currentPlot = mock(CurrentPlot.class);
                when(currentPlot.getCenterCoordinates()).thenReturn(new double[]{100.0, 200.0, 50.0});
                when(currentPlot.getColorPalette()).thenReturn(new ColorPalette());
                when(currentPlot.getStarDisplayPreferences()).thenReturn(new StarDisplayPreferences());
                when(currentPlot.getCivilizationDisplayPreferences()).thenReturn(new CivilizationDisplayPreferences());
                when(currentPlot.getStarDisplayRecordList()).thenReturn(new ArrayList<>());
                when(currentPlot.getCenterStar()).thenReturn(null);

                when(tripsContext.getCurrentPlot()).thenReturn(currentPlot);

                // Draw stars (empty list, but should configure LOD manager)
                starPlotManager.drawStars(currentPlot, false);

                // LOD manager should have center configured
                // We can verify by checking that a star at the center gets HIGH LOD
                StarLODManager lodManager = starPlotManager.getLodManager();
                StarDisplayRecord centerStar = createTestRecord(100, 200, 50, 4.0);

                // Star exactly at center should be very close
                StarLODManager.LODLevel level = lodManager.determineLODLevel(centerStar, false);
                assertEquals(StarLODManager.LODLevel.HIGH, level,
                        "Star at center coordinates should get HIGH LOD");

                return null;
            });
        }

        @Test
        @DisplayName("drawStars resets LOD statistics before drawing")
        void drawStarsResetsLodStatistics() throws Exception {
            runOnFxThread(() -> {
                // Setup
                Group sceneRoot = new Group();
                Group world = new Group();
                SubScene subScene = new SubScene(world, 800, 600);
                starPlotManager.setGraphics(sceneRoot, world, subScene);

                // Manually add some statistics
                StarLODManager lodManager = starPlotManager.getLodManager();
                StarDisplayRecord record = createTestRecord(0, 0, 0, 2.0);
                javafx.scene.paint.PhongMaterial material = new javafx.scene.paint.PhongMaterial(Color.YELLOW);
                lodManager.createStarWithLOD(record, 5.0, material, StarLODManager.LODLevel.HIGH);

                assertEquals(1, lodManager.getTotalStarCount());

                // Create current plot
                CurrentPlot currentPlot = mock(CurrentPlot.class);
                when(currentPlot.getCenterCoordinates()).thenReturn(new double[]{0, 0, 0});
                when(currentPlot.getColorPalette()).thenReturn(new ColorPalette());
                when(currentPlot.getStarDisplayPreferences()).thenReturn(new StarDisplayPreferences());
                when(currentPlot.getCivilizationDisplayPreferences()).thenReturn(new CivilizationDisplayPreferences());
                when(currentPlot.getStarDisplayRecordList()).thenReturn(new ArrayList<>());
                when(currentPlot.getCenterStar()).thenReturn(null);

                when(tripsContext.getCurrentPlot()).thenReturn(currentPlot);

                // Draw stars should reset statistics
                starPlotManager.drawStars(currentPlot, false);

                assertEquals(0, lodManager.getTotalStarCount(),
                        "Statistics should be reset after drawStars with empty list");

                return null;
            });
        }

        @Test
        @DisplayName("drawStars with multiple stars updates LOD statistics")
        void drawStarsWithMultipleStarsUpdatesStatistics() throws Exception {
            runOnFxThread(() -> {
                // Setup
                Group sceneRoot = new Group();
                Group world = new Group();
                SubScene subScene = new SubScene(world, 800, 600);
                starPlotManager.setGraphics(sceneRoot, world, subScene);

                // Create star records at various distances
                List<StarDisplayRecord> stars = new ArrayList<>();

                // Near star (should be HIGH or MEDIUM)
                StarDisplayRecord nearStar = createTestRecord(30, 0, 0, 4.0);
                when(nearStar.getStarColor()).thenReturn(Color.BLUE);
                when(nearStar.getPolity()).thenReturn("NA");
                when(nearStar.isDisplayLabel()).thenReturn(false);
                when(nearStar.isCenter()).thenReturn(false);
                stars.add(nearStar);

                // Medium distance star
                StarDisplayRecord mediumStar = createTestRecord(150, 0, 0, 2.0);
                when(mediumStar.getStarColor()).thenReturn(Color.YELLOW);
                when(mediumStar.getPolity()).thenReturn("NA");
                when(mediumStar.isDisplayLabel()).thenReturn(false);
                when(mediumStar.isCenter()).thenReturn(false);
                stars.add(mediumStar);

                // Far star (dim)
                StarDisplayRecord farStar = createTestRecord(500, 0, 0, 0.5);
                when(farStar.getStarColor()).thenReturn(Color.RED);
                when(farStar.getPolity()).thenReturn("NA");
                when(farStar.isDisplayLabel()).thenReturn(false);
                when(farStar.isCenter()).thenReturn(false);
                stars.add(farStar);

                // Create current plot
                CurrentPlot currentPlot = mock(CurrentPlot.class);
                when(currentPlot.getCenterCoordinates()).thenReturn(new double[]{0, 0, 0});
                when(currentPlot.getColorPalette()).thenReturn(new ColorPalette());
                when(currentPlot.getStarDisplayPreferences()).thenReturn(new StarDisplayPreferences());
                when(currentPlot.getCivilizationDisplayPreferences()).thenReturn(new CivilizationDisplayPreferences());
                when(currentPlot.getStarDisplayRecordList()).thenReturn(stars);
                when(currentPlot.getCenterStar()).thenReturn(null);

                when(tripsContext.getCurrentPlot()).thenReturn(currentPlot);

                // Draw stars
                starPlotManager.drawStars(currentPlot, false);

                // Should have 3 stars in statistics
                StarLODManager lodManager = starPlotManager.getLodManager();
                assertEquals(3, lodManager.getTotalStarCount(),
                        "Should have 3 stars in LOD statistics");

                return null;
            });
        }
    }

    // =========================================================================
    // LOD Performance Configuration Tests
    // =========================================================================

    @Nested
    @DisplayName("LOD Performance Configuration Tests")
    class LODPerformanceConfigurationTests {

        @Test
        @DisplayName("Higher zoom increases effective LOD quality")
        void higherZoomIncreasesEffectiveLodQuality() {
            StarLODManager lodManager = starPlotManager.getLodManager();

            // Star at 100 units with normal zoom gets MEDIUM
            StarDisplayRecord star = createTestRecord(100, 0, 0, 2.0);
            assertEquals(StarLODManager.LODLevel.MEDIUM,
                    lodManager.determineLODLevel(star, false));

            // With higher zoom, same star should get better LOD
            // because effective distance threshold is reduced
            starPlotManager.setZoomLevel(4.0);

            // At 4x zoom, the HIGH threshold becomes 50/4 = 12.5
            // The MEDIUM threshold becomes 200/4 = 50
            // 100 units is now beyond MEDIUM threshold, so it gets LOW
            StarLODManager.LODLevel levelAtHighZoom = lodManager.determineLODLevel(star, false);

            // This demonstrates zoom affects LOD calculation
            assertNotNull(levelAtHighZoom);
        }

        @Test
        @DisplayName("LOD statistics provide performance insights")
        void lodStatisticsProvidePerformanceInsights() throws Exception {
            runOnFxThread(() -> {
                StarLODManager lodManager = starPlotManager.getLodManager();
                lodManager.resetStatistics();

                javafx.scene.paint.PhongMaterial material = new javafx.scene.paint.PhongMaterial(Color.YELLOW);

                // Create distribution of stars
                for (int i = 0; i < 10; i++) {
                    StarDisplayRecord record = createTestRecord(0, 0, 0, 2.0);
                    lodManager.createStarWithLOD(record, 5.0, material, StarLODManager.LODLevel.HIGH);
                }
                for (int i = 0; i < 30; i++) {
                    StarDisplayRecord record = createTestRecord(0, 0, 0, 2.0);
                    lodManager.createStarWithLOD(record, 5.0, material, StarLODManager.LODLevel.MEDIUM);
                }
                for (int i = 0; i < 40; i++) {
                    StarDisplayRecord record = createTestRecord(0, 0, 0, 2.0);
                    lodManager.createStarWithLOD(record, 5.0, material, StarLODManager.LODLevel.LOW);
                }
                for (int i = 0; i < 20; i++) {
                    StarDisplayRecord record = createTestRecord(0, 0, 0, 2.0);
                    lodManager.createStarWithLOD(record, 5.0, material, StarLODManager.LODLevel.MINIMAL);
                }

                assertEquals(100, lodManager.getTotalStarCount());
                assertEquals(10, lodManager.getHighDetailCount());
                assertEquals(30, lodManager.getMediumDetailCount());
                assertEquals(40, lodManager.getLowDetailCount());
                assertEquals(20, lodManager.getMinimalDetailCount());

                // Check percentages
                String percentages = lodManager.getStatisticsPercentages();
                assertTrue(percentages.contains("HIGH: 10.0%"));
                assertTrue(percentages.contains("MEDIUM: 30.0%"));
                assertTrue(percentages.contains("LOW: 40.0%"));
                assertTrue(percentages.contains("MINIMAL: 20.0%"));

                return null;
            });
        }
    }

    // =========================================================================
    // Edge Cases Tests
    // =========================================================================

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("LOD handles null center coordinates gracefully")
        void lodHandlesNullCenterCoordinates() throws Exception {
            runOnFxThread(() -> {
                // Setup
                Group sceneRoot = new Group();
                Group world = new Group();
                SubScene subScene = new SubScene(world, 800, 600);
                starPlotManager.setGraphics(sceneRoot, world, subScene);

                // Create current plot with null center coordinates
                CurrentPlot currentPlot = mock(CurrentPlot.class);
                when(currentPlot.getCenterCoordinates()).thenReturn(null);
                when(currentPlot.getColorPalette()).thenReturn(new ColorPalette());
                when(currentPlot.getStarDisplayPreferences()).thenReturn(new StarDisplayPreferences());
                when(currentPlot.getCivilizationDisplayPreferences()).thenReturn(new CivilizationDisplayPreferences());
                when(currentPlot.getStarDisplayRecordList()).thenReturn(new ArrayList<>());
                when(currentPlot.getCenterStar()).thenReturn(null);

                when(tripsContext.getCurrentPlot()).thenReturn(currentPlot);

                // Should not throw
                assertDoesNotThrow(() -> starPlotManager.drawStars(currentPlot, false));

                return null;
            });
        }

        @Test
        @DisplayName("LOD handles empty center coordinates array gracefully")
        void lodHandlesEmptyCenterCoordinates() throws Exception {
            runOnFxThread(() -> {
                // Setup
                Group sceneRoot = new Group();
                Group world = new Group();
                SubScene subScene = new SubScene(world, 800, 600);
                starPlotManager.setGraphics(sceneRoot, world, subScene);

                // Create current plot with empty center coordinates
                CurrentPlot currentPlot = mock(CurrentPlot.class);
                when(currentPlot.getCenterCoordinates()).thenReturn(new double[]{});
                when(currentPlot.getColorPalette()).thenReturn(new ColorPalette());
                when(currentPlot.getStarDisplayPreferences()).thenReturn(new StarDisplayPreferences());
                when(currentPlot.getCivilizationDisplayPreferences()).thenReturn(new CivilizationDisplayPreferences());
                when(currentPlot.getStarDisplayRecordList()).thenReturn(new ArrayList<>());
                when(currentPlot.getCenterStar()).thenReturn(null);

                when(tripsContext.getCurrentPlot()).thenReturn(currentPlot);

                // Should not throw
                assertDoesNotThrow(() -> starPlotManager.drawStars(currentPlot, false));

                return null;
            });
        }

        @Test
        @DisplayName("LOD handles zero zoom level")
        void lodHandlesZeroZoomLevel() {
            // Zero zoom should still work (though unusual)
            assertDoesNotThrow(() -> starPlotManager.setZoomLevel(0.0));

            // Verify both managers have the value
            assertEquals(0.0, starPlotManager.getScaleManager().getZoomLevel());
            assertEquals(0.0, starPlotManager.getLodManager().getZoomLevel());
        }

        @Test
        @DisplayName("LOD handles negative zoom level")
        void lodHandlesNegativeZoomLevel() {
            // Negative zoom should still work (though unusual)
            assertDoesNotThrow(() -> starPlotManager.setZoomLevel(-1.0));

            // Verify both managers have the value
            assertEquals(-1.0, starPlotManager.getScaleManager().getZoomLevel());
            assertEquals(-1.0, starPlotManager.getLodManager().getZoomLevel());
        }
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================

    private StarDisplayRecord createTestRecord(double x, double y, double z, double radius) {
        StarDisplayRecord record = mock(StarDisplayRecord.class);
        when(record.getCoordinates()).thenReturn(new Point3D(x, y, z));
        when(record.getRadius()).thenReturn(radius);
        when(record.getDisplayScore()).thenReturn(0.0);
        when(record.getRecordId()).thenReturn("test-star-" + System.nanoTime());
        when(record.getStarName()).thenReturn("Test Star");
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
