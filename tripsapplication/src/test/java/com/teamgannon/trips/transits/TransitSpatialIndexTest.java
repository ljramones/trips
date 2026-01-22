package com.teamgannon.trips.transits;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import javafx.geometry.Point3D;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for TransitSpatialIndex.
 */
class TransitSpatialIndexTest {

    private TransitSpatialIndex index;

    @BeforeEach
    void setUp() {
        index = new TransitSpatialIndex();
    }

    // =========================================================================
    // IndexedTransit Tests
    // =========================================================================

    @Nested
    @DisplayName("IndexedTransit Tests")
    class IndexedTransitTests {

        @Test
        @DisplayName("Create calculates midpoint correctly")
        void createCalculatesMidpointCorrectly() {
            TransitRoute route = createTestTransitRoute("Star A", "Star B",
                    new Point3D(0, 0, 0), new Point3D(10, 0, 0), 10.0);

            IndexedTransit transit = IndexedTransit.create("band1", route);

            assertEquals(5.0, transit.midpoint().getX(), 0.001);
            assertEquals(0.0, transit.midpoint().getY(), 0.001);
            assertEquals(0.0, transit.midpoint().getZ(), 0.001);
        }

        @Test
        @DisplayName("Create calculates bounding radius correctly")
        void createCalculatesBoundingRadiusCorrectly() {
            TransitRoute route = createTestTransitRoute("Star A", "Star B",
                    new Point3D(0, 0, 0), new Point3D(10, 0, 0), 10.0);

            IndexedTransit transit = IndexedTransit.create("band1", route);

            assertEquals(5.0, transit.boundingRadius(), 0.001);
        }

        @Test
        @DisplayName("GetKey is bidirectional")
        void getKeyIsBidirectional() {
            TransitRoute route1 = createTestTransitRoute("Alpha", "Beta",
                    new Point3D(0, 0, 0), new Point3D(10, 0, 0), 10.0);
            TransitRoute route2 = createTestTransitRoute("Beta", "Alpha",
                    new Point3D(10, 0, 0), new Point3D(0, 0, 0), 10.0);

            IndexedTransit transit1 = IndexedTransit.create("band1", route1);
            IndexedTransit transit2 = IndexedTransit.create("band1", route2);

            assertEquals(transit1.getKey(), transit2.getKey());
        }

        @Test
        @DisplayName("IntersectsBoundingSphere returns true for overlapping spheres")
        void intersectsBoundingSphereReturnsTrueForOverlapping() {
            TransitRoute route = createTestTransitRoute("Star A", "Star B",
                    new Point3D(0, 0, 0), new Point3D(10, 0, 0), 10.0);
            IndexedTransit transit = IndexedTransit.create("band1", route);

            assertTrue(transit.intersectsBoundingSphere(3, 0, 0, 5));
        }

        @Test
        @DisplayName("IntersectsBoundingSphere returns false for non-overlapping spheres")
        void intersectsBoundingSphereReturnsFalseForNonOverlapping() {
            TransitRoute route = createTestTransitRoute("Star A", "Star B",
                    new Point3D(0, 0, 0), new Point3D(10, 0, 0), 10.0);
            IndexedTransit transit = IndexedTransit.create("band1", route);

            assertFalse(transit.intersectsBoundingSphere(100, 0, 0, 5));
        }
    }

    // =========================================================================
    // Index Building Tests
    // =========================================================================

    @Nested
    @DisplayName("Index Building Tests")
    class IndexBuildingTests {

        @Test
        @DisplayName("Empty index reports zero transits")
        void emptyIndexReportsZeroTransits() {
            assertEquals(0, index.getTotalTransits());
            assertTrue(index.isEmpty());
        }

        @Test
        @DisplayName("AddTransits indexes transits")
        void addTransitsIndexesTransits() {
            List<TransitRoute> routes = List.of(
                    createTestTransitRoute("A", "B", new Point3D(0, 0, 0), new Point3D(10, 0, 0), 10.0),
                    createTestTransitRoute("B", "C", new Point3D(10, 0, 0), new Point3D(20, 0, 0), 10.0)
            );

            index.addTransits("band1", routes);

            assertEquals(2, index.getTotalTransits());
            assertEquals(1, index.getBandCount());
        }

        @Test
        @DisplayName("Multiple bands are tracked separately")
        void multipleBandsAreTrackedSeparately() {
            List<TransitRoute> band1Routes = List.of(
                    createTestTransitRoute("A", "B", new Point3D(0, 0, 0), new Point3D(10, 0, 0), 10.0)
            );
            List<TransitRoute> band2Routes = List.of(
                    createTestTransitRoute("C", "D", new Point3D(20, 0, 0), new Point3D(30, 0, 0), 10.0),
                    createTestTransitRoute("D", "E", new Point3D(30, 0, 0), new Point3D(40, 0, 0), 10.0)
            );

            index.addTransits("band1", band1Routes);
            index.addTransits("band2", band2Routes);

            assertEquals(3, index.getTotalTransits());
            assertEquals(2, index.getBandCount());
            assertTrue(index.hasBand("band1"));
            assertTrue(index.hasBand("band2"));
        }

        @Test
        @DisplayName("RemoveBand removes transits")
        void removeBandRemovesTransits() {
            List<TransitRoute> routes = List.of(
                    createTestTransitRoute("A", "B", new Point3D(0, 0, 0), new Point3D(10, 0, 0), 10.0)
            );
            index.addTransits("band1", routes);

            index.removeBand("band1");

            assertEquals(0, index.getTotalTransits());
            assertFalse(index.hasBand("band1"));
        }

        @Test
        @DisplayName("Clear removes all transits")
        void clearRemovesAllTransits() {
            index.addTransits("band1", List.of(
                    createTestTransitRoute("A", "B", new Point3D(0, 0, 0), new Point3D(10, 0, 0), 10.0)
            ));
            index.addTransits("band2", List.of(
                    createTestTransitRoute("C", "D", new Point3D(20, 0, 0), new Point3D(30, 0, 0), 10.0)
            ));

            index.clear();

            assertTrue(index.isEmpty());
            assertEquals(0, index.getBandCount());
        }
    }

    // =========================================================================
    // Spatial Query Tests
    // =========================================================================

    @Nested
    @DisplayName("Spatial Query Tests")
    class SpatialQueryTests {

        @Test
        @DisplayName("FindTransitsWithinRadius returns transits in range")
        void findTransitsWithinRadiusReturnsTransitsInRange() {
            index.addTransits("band1", List.of(
                    createTestTransitRoute("A", "B", new Point3D(0, 0, 0), new Point3D(10, 0, 0), 10.0),
                    createTestTransitRoute("C", "D", new Point3D(100, 0, 0), new Point3D(110, 0, 0), 10.0)
            ));

            // Query near the first transit midpoint (5, 0, 0)
            List<IndexedTransit> results = index.findTransitsWithinRadius(5, 0, 0, 20);

            assertEquals(1, results.size());
            assertEquals("A", results.get(0).sourceName());
        }

        @Test
        @DisplayName("FindTransitsWithinRadius returns empty for distant query")
        void findTransitsWithinRadiusReturnsEmptyForDistantQuery() {
            index.addTransits("band1", List.of(
                    createTestTransitRoute("A", "B", new Point3D(0, 0, 0), new Point3D(10, 0, 0), 10.0)
            ));

            List<IndexedTransit> results = index.findTransitsWithinRadius(1000, 1000, 1000, 10);

            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("FindTransitsForBandWithinRadius filters by band")
        void findTransitsForBandWithinRadiusFiltersByBand() {
            index.addTransits("band1", List.of(
                    createTestTransitRoute("A", "B", new Point3D(0, 0, 0), new Point3D(10, 0, 0), 10.0)
            ));
            index.addTransits("band2", List.of(
                    createTestTransitRoute("C", "D", new Point3D(5, 5, 0), new Point3D(15, 5, 0), 10.0)
            ));

            List<IndexedTransit> results = index.findTransitsForBandWithinRadius("band1", 5, 0, 0, 50);

            assertEquals(1, results.size());
            assertEquals("band1", results.get(0).bandId());
        }

        @Test
        @DisplayName("GetTransitsForBand returns all transits for band")
        void getTransitsForBandReturnsAllTransitsForBand() {
            index.addTransits("band1", List.of(
                    createTestTransitRoute("A", "B", new Point3D(0, 0, 0), new Point3D(10, 0, 0), 10.0),
                    createTestTransitRoute("B", "C", new Point3D(10, 0, 0), new Point3D(20, 0, 0), 10.0)
            ));

            List<IndexedTransit> transits = index.getTransitsForBand("band1");

            assertEquals(2, transits.size());
        }

        @Test
        @DisplayName("FindNearestTransit returns closest transit")
        void findNearestTransitReturnsClosestTransit() {
            index.addTransits("band1", List.of(
                    createTestTransitRoute("A", "B", new Point3D(0, 0, 0), new Point3D(10, 0, 0), 10.0),
                    createTestTransitRoute("C", "D", new Point3D(100, 0, 0), new Point3D(110, 0, 0), 10.0)
            ));

            IndexedTransit nearest = index.findNearestTransit(4, 0, 0);

            assertNotNull(nearest);
            assertEquals("A", nearest.sourceName());
        }

        @Test
        @DisplayName("GetVisibleBandIds returns bands with visible transits")
        void getVisibleBandIdsReturnsBandsWithVisibleTransits() {
            index.addTransits("band1", List.of(
                    createTestTransitRoute("A", "B", new Point3D(0, 0, 0), new Point3D(10, 0, 0), 10.0)
            ));
            index.addTransits("band2", List.of(
                    createTestTransitRoute("C", "D", new Point3D(1000, 0, 0), new Point3D(1010, 0, 0), 10.0)
            ));

            Set<String> visibleIds = index.getVisibleBandIds(5, 0, 0, 50);

            assertTrue(visibleIds.contains("band1"));
            assertFalse(visibleIds.contains("band2"));
        }
    }

    // =========================================================================
    // Statistics Tests
    // =========================================================================

    @Nested
    @DisplayName("Statistics Tests")
    class StatisticsTests {

        @Test
        @DisplayName("Statistics are tracked correctly")
        void statisticsAreTrackedCorrectly() {
            index.addTransits("band1", List.of(
                    createTestTransitRoute("A", "B", new Point3D(0, 0, 0), new Point3D(10, 0, 0), 10.0)
            ));

            index.findTransitsWithinRadius(5, 0, 0, 10);
            index.findTransitsWithinRadius(5, 0, 0, 10);

            assertEquals(2, index.getQueryCount());
            assertTrue(index.getTransitsChecked() > 0);
        }

        @Test
        @DisplayName("ResetStatistics clears counters")
        void resetStatisticsClearsCounters() {
            index.addTransits("band1", List.of(
                    createTestTransitRoute("A", "B", new Point3D(0, 0, 0), new Point3D(10, 0, 0), 10.0)
            ));
            index.findTransitsWithinRadius(5, 0, 0, 10);

            index.resetStatistics();

            assertEquals(0, index.getQueryCount());
        }

        @Test
        @DisplayName("GetStatistics returns formatted string")
        void getStatisticsReturnsFormattedString() {
            index.addTransits("band1", List.of(
                    createTestTransitRoute("A", "B", new Point3D(0, 0, 0), new Point3D(10, 0, 0), 10.0)
            ));

            String stats = index.getStatistics();

            assertTrue(stats.contains("TransitSpatialIndex"));
            assertTrue(stats.contains("bands=1"));
            assertTrue(stats.contains("transits=1"));
        }

        @Test
        @DisplayName("GetTransitCountForBand returns correct count")
        void getTransitCountForBandReturnsCorrectCount() {
            index.addTransits("band1", List.of(
                    createTestTransitRoute("A", "B", new Point3D(0, 0, 0), new Point3D(10, 0, 0), 10.0),
                    createTestTransitRoute("B", "C", new Point3D(10, 0, 0), new Point3D(20, 0, 0), 10.0)
            ));
            index.addTransits("band2", List.of(
                    createTestTransitRoute("D", "E", new Point3D(30, 0, 0), new Point3D(40, 0, 0), 10.0)
            ));

            assertEquals(2, index.getTransitCountForBand("band1"));
            assertEquals(1, index.getTransitCountForBand("band2"));
            assertEquals(0, index.getTransitCountForBand("nonexistent"));
        }
    }

    // =========================================================================
    // Edge Case Tests
    // =========================================================================

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Query on empty index returns empty list")
        void queryOnEmptyIndexReturnsEmptyList() {
            List<IndexedTransit> results = index.findTransitsWithinRadius(0, 0, 0, 100);

            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("FindNearestTransit on empty index returns null")
        void findNearestTransitOnEmptyIndexReturnsNull() {
            IndexedTransit nearest = index.findNearestTransit(0, 0, 0);

            assertNull(nearest);
        }

        @Test
        @DisplayName("Transit with null endpoints is skipped")
        void transitWithNullEndpointsIsSkipped() {
            TransitRoute badRoute = mock(TransitRoute.class);
            when(badRoute.getSource()).thenReturn(null);

            index.addTransits("band1", List.of(badRoute));

            assertEquals(0, index.getTotalTransits());
        }
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================

    private TransitRoute createTestTransitRoute(String sourceName, String targetName,
                                                 Point3D sourcePoint, Point3D targetPoint,
                                                 double distance) {
        StarDisplayRecord source = mock(StarDisplayRecord.class);
        when(source.getStarName()).thenReturn(sourceName);
        when(source.getCoordinates()).thenReturn(sourcePoint);

        StarDisplayRecord target = mock(StarDisplayRecord.class);
        when(target.getStarName()).thenReturn(targetName);
        when(target.getCoordinates()).thenReturn(targetPoint);

        return TransitRoute.builder()
                .source(source)
                .target(target)
                .distance(distance)
                .color(Color.WHITE)
                .lineWeight(1.0)
                .good(true)
                .build();
    }
}
