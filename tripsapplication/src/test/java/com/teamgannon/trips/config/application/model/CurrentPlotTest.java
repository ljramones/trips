package com.teamgannon.trips.config.application.model;

import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.graphics.entities.RouteVisibility;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import javafx.application.Platform;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.shape.Sphere;
import org.junit.jupiter.api.*;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for CurrentPlot.
 * <p>
 * Tests cover:
 * <ul>
 *   <li>Plot setup and initialization</li>
 *   <li>Star management (add, get, lookup)</li>
 *   <li>Label management</li>
 *   <li>Route management</li>
 *   <li>Spatial index integration</li>
 *   <li>Distance-sorted star retrieval</li>
 *   <li>Plot clearing</li>
 * </ul>
 */
class CurrentPlotTest {

    private static boolean javaFxInitialized = false;
    private CurrentPlot plot;

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
        plot = new CurrentPlot();
    }

    // =========================================================================
    // Initialization Tests
    // =========================================================================

    @Nested
    @DisplayName("Initialization Tests")
    class InitializationTests {

        @Test
        @DisplayName("New plot is not active")
        void newPlotIsNotActive() {
            assertFalse(plot.isPlotActive());
        }

        @Test
        @DisplayName("New plot has empty star list")
        void newPlotHasEmptyStarList() {
            assertTrue(plot.getStarDisplayRecordList().isEmpty());
        }

        @Test
        @DisplayName("New plot has empty star lookup")
        void newPlotHasEmptyStarLookup() {
            assertTrue(plot.getStarIds().isEmpty());
        }

        @Test
        @DisplayName("New plot has empty routes")
        void newPlotHasEmptyRoutes() {
            assertTrue(plot.getRoutes().isEmpty());
        }

        @Test
        @DisplayName("New plot has null spatial index")
        void newPlotHasNullSpatialIndex() {
            assertNull(plot.getSpatialIndex());
        }
    }

    // =========================================================================
    // Setup Tests
    // =========================================================================

    @Nested
    @DisplayName("Plot Setup Tests")
    class SetupTests {

        @Test
        @DisplayName("Setup configures all properties")
        void setupConfiguresAllProperties() {
            DataSetDescriptor descriptor = mock(DataSetDescriptor.class);
            double[] center = {1.0, 2.0, 3.0};
            ColorPalette palette = mock(ColorPalette.class);

            plot.setupPlot(descriptor, center, "Sol", palette);

            assertSame(descriptor, plot.getDataSetDescriptor());
            assertArrayEquals(center, plot.getCenterCoordinates());
            assertEquals("Sol", plot.getCenterStar());
            assertSame(palette, plot.getColorPalette());
        }

        @Test
        @DisplayName("Setup with null values is allowed")
        void setupWithNullValuesIsAllowed() {
            assertDoesNotThrow(() -> plot.setupPlot(null, null, null, null));
        }
    }

    // =========================================================================
    // Star Management Tests
    // =========================================================================

    @Nested
    @DisplayName("Star Management Tests")
    class StarManagementTests {

        @Test
        @DisplayName("Add record increases list size")
        void addRecordIncreasesListSize() {
            StarDisplayRecord record = createTestRecord("star-1", 1, 2, 3);

            plot.addRecord(record);

            assertEquals(1, plot.getStarDisplayRecordList().size());
        }

        @Test
        @DisplayName("Add record marks spatial index dirty")
        void addRecordMarksSpatialIndexDirty() {
            // Setup with center coordinates
            plot.setupPlot(null, new double[]{0, 0, 0}, "Sol", null);
            StarDisplayRecord record1 = createTestRecord("star-1", 1, 0, 0);
            plot.addRecord(record1);

            // Force index build
            plot.getSpatialIndex();

            // Add another record
            StarDisplayRecord record2 = createTestRecord("star-2", 2, 0, 0);
            plot.addRecord(record2);

            // Index should be rebuilt on next access (dirty)
            // We can't easily test this without accessing internals,
            // but we can verify the new star is included
            assertEquals(2, plot.getStarDisplayRecordList().size());
        }

        @Test
        @DisplayName("Add star to lookup allows retrieval")
        void addStarToLookupAllowsRetrieval() throws Exception {
            assumeJavaFxAvailable();

            Node starNode = runOnFxThread(() -> new Sphere(5.0));
            String starId = "test-star-id";

            plot.addStar(starId, starNode);

            assertSame(starNode, plot.getStar(starId));
        }

        @Test
        @DisplayName("Get star ids returns all added stars")
        void getStarIdsReturnsAllAddedStars() throws Exception {
            assumeJavaFxAvailable();

            Node star1 = runOnFxThread(() -> new Sphere(5.0));
            Node star2 = runOnFxThread(() -> new Sphere(5.0));
            Node star3 = runOnFxThread(() -> new Sphere(5.0));

            plot.addStar("star-1", star1);
            plot.addStar("star-2", star2);
            plot.addStar("star-3", star3);

            Set<String> ids = plot.getStarIds();
            assertEquals(3, ids.size());
            assertTrue(ids.contains("star-1"));
            assertTrue(ids.contains("star-2"));
            assertTrue(ids.contains("star-3"));
        }

        @Test
        @DisplayName("Get star ids returns defensive copy")
        void getStarIdsReturnsDefensiveCopy() throws Exception {
            assumeJavaFxAvailable();

            Node star = runOnFxThread(() -> new Sphere(5.0));
            plot.addStar("star-1", star);

            Set<String> ids = plot.getStarIds();
            ids.clear();

            assertEquals(1, plot.getStarIds().size());
        }

        @Test
        @DisplayName("Is star visible checks UUID lookup")
        void isStarVisibleChecksUuidLookup() throws Exception {
            assumeJavaFxAvailable();

            UUID starId = UUID.randomUUID();
            Node star = runOnFxThread(() -> new Sphere(5.0));

            plot.addStar(starId.toString(), star);

            assertTrue(plot.isStarVisible(starId));
        }

        @Test
        @DisplayName("Get non-existent star returns null")
        void getNonExistentStarReturnsNull() {
            assertNull(plot.getStar("non-existent"));
        }

        @Test
        @DisplayName("Center star has display label enabled")
        void centerStarHasDisplayLabelEnabled() {
            StarDisplayRecord record = createCenterRecord("Sol");

            plot.addRecord(record);

            assertTrue(record.isDisplayLabel());
        }

        @Test
        @DisplayName("Forced label star has high display score")
        void forcedLabelStarHasHighDisplayScore() {
            StarDisplayRecord record = createForcedLabelRecord("star-1", 10, 0, 0);

            plot.addRecord(record);

            assertEquals(1000, record.getCurrentLabelDisplayScore());
        }
    }

    // =========================================================================
    // Label Management Tests
    // =========================================================================

    @Nested
    @DisplayName("Label Management Tests")
    class LabelManagementTests {

        @Test
        @DisplayName("Map label to star allows retrieval")
        void mapLabelToStarAllowsRetrieval() throws Exception {
            assumeJavaFxAvailable();

            Label label = runOnFxThread(() -> new Label("Test Star"));
            UUID starId = UUID.randomUUID();

            plot.mapLabelToStar(starId.toString(), label);

            assertSame(label, plot.getLabelForStar(starId));
        }

        @Test
        @DisplayName("Map label to star supports UUID overload")
        void mapLabelToStarSupportsUuidOverload() throws Exception {
            assumeJavaFxAvailable();

            Label label = runOnFxThread(() -> new Label("Test Star"));
            UUID starId = UUID.randomUUID();

            plot.mapLabelToStar(starId, label);

            assertSame(label, plot.getLabelForStar(starId.toString()));
        }

        @Test
        @DisplayName("Get label for star supports String overload")
        void getLabelForStarSupportsStringOverload() throws Exception {
            assumeJavaFxAvailable();

            Label label = runOnFxThread(() -> new Label("Test Star"));
            String starId = "star-123";

            plot.mapLabelToStar(starId, label);

            assertSame(label, plot.getLabelForStar(starId));
        }

        @Test
        @DisplayName("Determine visible labels sets correct count")
        void determineVisibleLabelsSetsCorrectCount() {
            // Add stars with different scores
            for (int i = 0; i < 10; i++) {
                StarDisplayRecord record = createTestRecord("star-" + i, i, 0, 0);
                record.setCurrentLabelDisplayScore(i * 10.0);
                plot.addRecord(record);
            }

            plot.determineVisibleLabels(3);

            long labeledCount = plot.getStarDisplayRecordList().stream()
                    .filter(StarDisplayRecord::isDisplayLabel)
                    .count();
            assertEquals(3, labeledCount);
        }

        @Test
        @DisplayName("Determine visible labels clears previous labels")
        void determineVisibleLabelsClearsPreviousLabels() {
            for (int i = 0; i < 5; i++) {
                StarDisplayRecord record = createTestRecord("star-" + i, i, 0, 0);
                record.setCurrentLabelDisplayScore(i * 10.0);
                plot.addRecord(record);
            }

            plot.determineVisibleLabels(4);
            plot.determineVisibleLabels(2);

            long labeledCount = plot.getStarDisplayRecordList().stream()
                    .filter(StarDisplayRecord::isDisplayLabel)
                    .count();
            assertEquals(2, labeledCount);
        }

        @Test
        @DisplayName("Determine visible labels respects max count")
        void determineVisibleLabelsRespectsMaxCount() {
            // Add only 2 stars
            plot.addRecord(createTestRecord("star-1", 1, 0, 0));
            plot.addRecord(createTestRecord("star-2", 2, 0, 0));

            // Request 10 labels
            plot.determineVisibleLabels(10);

            long labeledCount = plot.getStarDisplayRecordList().stream()
                    .filter(StarDisplayRecord::isDisplayLabel)
                    .count();
            assertEquals(2, labeledCount); // Can't have more labels than stars
        }
    }

    // =========================================================================
    // Route Management Tests
    // =========================================================================

    @Nested
    @DisplayName("Route Management Tests")
    class RouteManagementTests {

        @Test
        @DisplayName("Add route allows retrieval")
        void addRouteAllowsRetrieval() {
            UUID routeId = UUID.randomUUID();
            RouteDescriptor route = createTestRoute(routeId);

            plot.addRoute(null, route);

            assertSame(route, plot.getRoute(routeId));
        }

        @Test
        @DisplayName("Add duplicate route does not create duplicates")
        void addDuplicateRouteDoesNotCreateDuplicates() {
            UUID routeId = UUID.randomUUID();
            RouteDescriptor route = createTestRoute(routeId);

            plot.addRoute(null, route);
            plot.addRoute(null, route);

            assertEquals(1, plot.getRoutes().size());
        }

        @Test
        @DisplayName("Remove route removes from list")
        void removeRouteRemovesFromList() {
            UUID routeId = UUID.randomUUID();
            RouteDescriptor route = createTestRoute(routeId);

            plot.addRoute(null, route);
            plot.removeRoute(route);

            assertNull(plot.getRoute(routeId));
            assertTrue(plot.getRoutes().isEmpty());
        }

        @Test
        @DisplayName("Get routes returns all added routes")
        void getRoutesReturnsAllAddedRoutes() {
            RouteDescriptor route1 = createTestRoute(UUID.randomUUID());
            RouteDescriptor route2 = createTestRoute(UUID.randomUUID());
            RouteDescriptor route3 = createTestRoute(UUID.randomUUID());

            plot.addRoute(null, route1);
            plot.addRoute(null, route2);
            plot.addRoute(null, route3);

            List<RouteDescriptor> routes = plot.getRoutes();
            assertEquals(3, routes.size());
        }

        @Test
        @DisplayName("Clear routes removes all routes")
        void clearRoutesRemovesAllRoutes() {
            plot.addRoute(null, createTestRoute(UUID.randomUUID()));
            plot.addRoute(null, createTestRoute(UUID.randomUUID()));

            plot.clearRoutes();

            assertTrue(plot.getRoutes().isEmpty());
        }

        @Test
        @DisplayName("Get visibility map returns route visibilities")
        void getVisibilityMapReturnsRouteVisibilities() {
            UUID routeId = UUID.randomUUID();
            RouteDescriptor route = createTestRoute(routeId);
            route.setVisibility(RouteVisibility.FULL);

            plot.addRoute(null, route);

            Map<UUID, RouteVisibility> visibilityMap = plot.getVisibilityMap();
            assertEquals(RouteVisibility.FULL, visibilityMap.get(routeId));
        }
    }

    // =========================================================================
    // Spatial Index Tests
    // =========================================================================

    @Nested
    @DisplayName("Spatial Index Tests")
    class SpatialIndexTests {

        @Test
        @DisplayName("Get spatial index returns null when no stars")
        void getSpatialIndexReturnsNullWhenNoStars() {
            assertNull(plot.getSpatialIndex());
        }

        @Test
        @DisplayName("Get spatial index builds index when stars exist")
        void getSpatialIndexBuildsIndexWhenStarsExist() {
            plot.setupPlot(null, new double[]{0, 0, 0}, "Sol", null);
            plot.addRecord(createTestRecord("star-1", 5, 0, 0));
            plot.addRecord(createTestRecord("star-2", 10, 0, 0));

            assertNotNull(plot.getSpatialIndex());
        }

        @Test
        @DisplayName("Invalidate spatial index forces rebuild")
        void invalidateSpatialIndexForcesRebuild() {
            plot.setupPlot(null, new double[]{0, 0, 0}, "Sol", null);
            plot.addRecord(createTestRecord("star-1", 5, 0, 0));

            // Build index
            var index1 = plot.getSpatialIndex();

            // Invalidate
            plot.invalidateSpatialIndex();

            // Should rebuild
            var index2 = plot.getSpatialIndex();

            // Both should be valid indexes (we can't easily test they're different)
            assertNotNull(index1);
            assertNotNull(index2);
        }

        @Test
        @DisplayName("Get stars within radius returns filtered results")
        void getStarsWithinRadiusReturnsFilteredResults() {
            plot.setupPlot(null, new double[]{0, 0, 0}, "Sol", null);

            // Add stars at different distances
            plot.addRecord(createTestRecord("close-1", 5, 0, 0));
            plot.addRecord(createTestRecord("close-2", 8, 0, 0));
            plot.addRecord(createTestRecord("far-1", 20, 0, 0));
            plot.addRecord(createTestRecord("far-2", 30, 0, 0));

            List<StarDisplayRecord> withinRadius = plot.getStarsWithinRadius(10);

            assertEquals(2, withinRadius.size());
        }

        @Test
        @DisplayName("Get stars within radius returns all when no center")
        void getStarsWithinRadiusReturnsAllWhenNoCenter() {
            plot.addRecord(createTestRecord("star-1", 5, 0, 0));
            plot.addRecord(createTestRecord("star-2", 100, 0, 0));

            List<StarDisplayRecord> stars = plot.getStarsWithinRadius(10);

            assertEquals(2, stars.size()); // Returns all since center not set
        }

        @Test
        @DisplayName("Get stars in range returns filtered results")
        void getStarsInRangeReturnsFilteredResults() {
            plot.setupPlot(null, new double[]{0, 0, 0}, "Sol", null);

            plot.addRecord(createTestRecord("very-close", 2, 0, 0));
            plot.addRecord(createTestRecord("mid-1", 10, 0, 0));
            plot.addRecord(createTestRecord("mid-2", 15, 0, 0));
            plot.addRecord(createTestRecord("far", 30, 0, 0));

            List<StarDisplayRecord> inRange = plot.getStarsInRange(5, 20);

            assertEquals(2, inRange.size());
        }
    }

    // =========================================================================
    // Distance Sorted Access Tests
    // =========================================================================

    @Nested
    @DisplayName("Distance Sorted Access Tests")
    class DistanceSortedAccessTests {

        @Test
        @DisplayName("Get stars sorted by distance returns nearest first")
        void getStarsSortedByDistanceReturnsNearestFirst() {
            plot.setupPlot(null, new double[]{0, 0, 0}, "Sol", null);

            plot.addRecord(createTestRecord("far", 30, 0, 0));
            plot.addRecord(createTestRecord("close", 5, 0, 0));
            plot.addRecord(createTestRecord("mid", 15, 0, 0));

            List<StarDisplayRecord> sorted = plot.getStarsSortedByDistance();

            assertEquals(3, sorted.size());
            assertEquals("close", sorted.get(0).getStarName());
            assertEquals("mid", sorted.get(1).getStarName());
            assertEquals("far", sorted.get(2).getStarName());
        }

        @Test
        @DisplayName("Get stars sorted by distance caches distance")
        void getStarsSortedByDistanceCachesDistance() {
            plot.setupPlot(null, new double[]{0, 0, 0}, "Sol", null);

            StarDisplayRecord record = createTestRecord("star", 10, 0, 0);
            plot.addRecord(record);

            plot.getStarsSortedByDistance();

            assertTrue(record.hasComputedDistance());
            assertEquals(10.0, record.getDistanceFromPlotCenter(), 0.001);
        }

        @Test
        @DisplayName("Get stars sorted by distance returns unsorted when no center")
        void getStarsSortedByDistanceReturnsUnsortedWhenNoCenter() {
            plot.addRecord(createTestRecord("star-1", 10, 0, 0));
            plot.addRecord(createTestRecord("star-2", 5, 0, 0));

            List<StarDisplayRecord> result = plot.getStarsSortedByDistance();

            assertEquals(2, result.size());
            // Order is undefined when center not set
        }

        @Test
        @DisplayName("Get stars within radius sorted combines filter and sort")
        void getStarsWithinRadiusSortedCombinesFilterAndSort() {
            plot.setupPlot(null, new double[]{0, 0, 0}, "Sol", null);

            plot.addRecord(createTestRecord("close-far", 8, 0, 0));
            plot.addRecord(createTestRecord("close-near", 3, 0, 0));
            plot.addRecord(createTestRecord("outside", 20, 0, 0));

            List<StarDisplayRecord> result = plot.getStarsWithinRadiusSorted(10);

            assertEquals(2, result.size());
            assertEquals("close-near", result.get(0).getStarName());
            assertEquals("close-far", result.get(1).getStarName());
        }
    }

    // =========================================================================
    // Clear Plot Tests
    // =========================================================================

    @Nested
    @DisplayName("Clear Plot Tests")
    class ClearPlotTests {

        @Test
        @DisplayName("Clear plot removes all stars")
        void clearPlotRemovesAllStars() throws Exception {
            assumeJavaFxAvailable();

            plot.addRecord(createTestRecord("star-1", 1, 0, 0));
            plot.addStar("star-1", runOnFxThread(() -> new Sphere(5.0)));

            plot.clearPlot();

            assertTrue(plot.getStarDisplayRecordList().isEmpty());
            assertTrue(plot.getStarIds().isEmpty());
        }

        @Test
        @DisplayName("Clear plot removes all routes")
        void clearPlotRemovesAllRoutes() {
            plot.addRoute(null, createTestRoute(UUID.randomUUID()));

            plot.clearPlot();

            assertTrue(plot.getRoutes().isEmpty());
        }

        @Test
        @DisplayName("Clear plot removes all labels")
        void clearPlotRemovesAllLabels() throws Exception {
            assumeJavaFxAvailable();

            plot.mapLabelToStar("star-1", runOnFxThread(() -> new Label("Test")));

            plot.clearPlot();

            assertTrue(plot.getStarToLabelLookup().isEmpty());
        }

        @Test
        @DisplayName("Clear plot sets inactive")
        void clearPlotSetsInactive() {
            plot.setPlotActive(true);

            plot.clearPlot();

            assertFalse(plot.isPlotActive());
        }

        @Test
        @DisplayName("Clear plot resets center coordinates")
        void clearPlotResetsCenterCoordinates() {
            plot.setupPlot(null, new double[]{10, 20, 30}, "Sol", null);

            plot.clearPlot();

            assertArrayEquals(new double[3], plot.getCenterCoordinates());
        }

        @Test
        @DisplayName("Clear plot invalidates spatial index")
        void clearPlotInvalidatesSpatialIndex() {
            plot.setupPlot(null, new double[]{0, 0, 0}, "Sol", null);
            plot.addRecord(createTestRecord("star", 10, 0, 0));
            plot.getSpatialIndex(); // Build index

            plot.clearPlot();

            assertNull(plot.getSpatialIndex()); // Should be null after clear
        }
    }

    // =========================================================================
    // Edge Case Tests
    // =========================================================================

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Multiple adds with same ID overwrites star")
        void multipleAddsWithSameIdOverwritesStar() throws Exception {
            assumeJavaFxAvailable();

            Node star1 = runOnFxThread(() -> new Sphere(5.0));
            Node star2 = runOnFxThread(() -> new Sphere(10.0));

            plot.addStar("same-id", star1);
            plot.addStar("same-id", star2);

            assertSame(star2, plot.getStar("same-id"));
        }

        @Test
        @DisplayName("Empty center coordinates handled gracefully")
        void emptyCenterCoordinatesHandledGracefully() {
            plot.setupPlot(null, new double[]{}, "Sol", null);

            // Should not throw
            List<StarDisplayRecord> result = plot.getStarsSortedByDistance();
            assertNotNull(result);
        }

        @Test
        @DisplayName("3D distance calculation is correct")
        void threeDDistanceCalculationIsCorrect() {
            plot.setupPlot(null, new double[]{0, 0, 0}, "Sol", null);

            // Star at (3, 4, 0) should be distance 5 from origin
            plot.addRecord(createTestRecord("pythagoras", 3, 4, 0));

            List<StarDisplayRecord> sorted = plot.getStarsSortedByDistance();
            assertEquals(5.0, sorted.get(0).getDistanceFromPlotCenter(), 0.001);
        }
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================

    private StarDisplayRecord createTestRecord(String name, double x, double y, double z) {
        StarDisplayRecord record = mock(StarDisplayRecord.class);
        when(record.getStarName()).thenReturn(name);
        when(record.getRecordId()).thenReturn("id-" + name);
        when(record.getActualCoordinates()).thenReturn(new double[]{x, y, z});
        when(record.getCoordinates()).thenReturn(new Point3D(x * 10, y * 10, z * 10));
        when(record.isCenter()).thenReturn(false);
        when(record.isLabelForced()).thenReturn(false);
        when(record.getDisplayScore()).thenReturn(5.0);
        when(record.getCurrentLabelDisplayScore()).thenReturn(5.0);

        // Allow setting display label
        final boolean[] displayLabel = {false};
        doAnswer(inv -> {
            displayLabel[0] = inv.getArgument(0);
            return null;
        }).when(record).setDisplayLabel(anyBoolean());
        when(record.isDisplayLabel()).thenAnswer(inv -> displayLabel[0]);

        // Allow setting current label display score
        final double[] labelScore = {5.0};
        doAnswer(inv -> {
            labelScore[0] = inv.getArgument(0);
            return null;
        }).when(record).setCurrentLabelDisplayScore(anyDouble());
        when(record.getCurrentLabelDisplayScore()).thenAnswer(inv -> labelScore[0]);

        // Support distance caching
        final double[] cachedDistance = {-1};
        doAnswer(inv -> {
            double cx = inv.getArgument(0);
            double cy = inv.getArgument(1);
            double cz = inv.getArgument(2);
            double dx = x - cx;
            double dy = y - cy;
            double dz = z - cz;
            cachedDistance[0] = Math.sqrt(dx * dx + dy * dy + dz * dz);
            return cachedDistance[0];
        }).when(record).computeAndCacheDistanceFromCenter(anyDouble(), anyDouble(), anyDouble());
        when(record.getDistanceFromPlotCenter()).thenAnswer(inv -> cachedDistance[0]);
        when(record.hasComputedDistance()).thenAnswer(inv -> cachedDistance[0] >= 0);

        return record;
    }

    private StarDisplayRecord createCenterRecord(String name) {
        StarDisplayRecord record = createTestRecord(name, 0, 0, 0);
        when(record.isCenter()).thenReturn(true);
        return record;
    }

    private StarDisplayRecord createForcedLabelRecord(String name, double x, double y, double z) {
        StarDisplayRecord record = createTestRecord(name, x, y, z);
        when(record.isLabelForced()).thenReturn(true);
        return record;
    }

    private RouteDescriptor createTestRoute(UUID id) {
        RouteDescriptor route = mock(RouteDescriptor.class);
        when(route.getId()).thenReturn(id);
        when(route.getVisibility()).thenReturn(RouteVisibility.FULL);
        return route;
    }

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
