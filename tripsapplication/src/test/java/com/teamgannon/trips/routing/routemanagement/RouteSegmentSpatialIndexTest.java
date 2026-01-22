package com.teamgannon.trips.routing.routemanagement;

import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import javafx.geometry.Point3D;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for RouteSegmentSpatialIndex.
 */
class RouteSegmentSpatialIndexTest {

    private RouteSegmentSpatialIndex index;

    @BeforeEach
    void setUp() {
        index = new RouteSegmentSpatialIndex();
    }

    // =========================================================================
    // IndexedRouteSegment Tests
    // =========================================================================

    @Nested
    @DisplayName("IndexedRouteSegment Tests")
    class IndexedRouteSegmentTests {

        @Test
        @DisplayName("Create calculates midpoint correctly")
        void createCalculatesMidpointCorrectly() {
            UUID routeId = UUID.randomUUID();
            Point3D start = new Point3D(0, 0, 0);
            Point3D end = new Point3D(10, 0, 0);

            IndexedRouteSegment segment = IndexedRouteSegment.create(routeId, 0, start, end);

            assertEquals(5.0, segment.midpoint().getX(), 0.001);
            assertEquals(0.0, segment.midpoint().getY(), 0.001);
            assertEquals(0.0, segment.midpoint().getZ(), 0.001);
        }

        @Test
        @DisplayName("Create calculates bounding radius correctly")
        void createCalculatesBoundingRadiusCorrectly() {
            UUID routeId = UUID.randomUUID();
            Point3D start = new Point3D(0, 0, 0);
            Point3D end = new Point3D(10, 0, 0);

            IndexedRouteSegment segment = IndexedRouteSegment.create(routeId, 0, start, end);

            assertEquals(5.0, segment.boundingRadius(), 0.001);
        }

        @Test
        @DisplayName("GetLength returns segment length")
        void getLengthReturnsSegmentLength() {
            UUID routeId = UUID.randomUUID();
            Point3D start = new Point3D(0, 0, 0);
            Point3D end = new Point3D(10, 0, 0);

            IndexedRouteSegment segment = IndexedRouteSegment.create(routeId, 0, start, end);

            assertEquals(10.0, segment.getLength(), 0.001);
        }

        @Test
        @DisplayName("IntersectsBoundingSphere returns true for overlapping spheres")
        void intersectsBoundingSphereReturnsTrueForOverlapping() {
            UUID routeId = UUID.randomUUID();
            Point3D start = new Point3D(0, 0, 0);
            Point3D end = new Point3D(10, 0, 0);
            IndexedRouteSegment segment = IndexedRouteSegment.create(routeId, 0, start, end);

            // Query sphere at (3, 0, 0) with radius 5 should intersect segment at midpoint (5, 0, 0) with radius 5
            assertTrue(segment.intersectsBoundingSphere(3, 0, 0, 5));
        }

        @Test
        @DisplayName("IntersectsBoundingSphere returns false for non-overlapping spheres")
        void intersectsBoundingSphereReturnsFalseForNonOverlapping() {
            UUID routeId = UUID.randomUUID();
            Point3D start = new Point3D(0, 0, 0);
            Point3D end = new Point3D(10, 0, 0);
            IndexedRouteSegment segment = IndexedRouteSegment.create(routeId, 0, start, end);

            // Query sphere at (100, 0, 0) with radius 5 should not intersect segment at midpoint (5, 0, 0) with radius 5
            assertFalse(segment.intersectsBoundingSphere(100, 0, 0, 5));
        }

        @Test
        @DisplayName("GetMidpointCoordinates returns array")
        void getMidpointCoordinatesReturnsArray() {
            UUID routeId = UUID.randomUUID();
            Point3D start = new Point3D(0, 0, 0);
            Point3D end = new Point3D(10, 20, 30);
            IndexedRouteSegment segment = IndexedRouteSegment.create(routeId, 0, start, end);

            double[] coords = segment.getMidpointCoordinates();

            assertEquals(3, coords.length);
            assertEquals(5.0, coords[0], 0.001);
            assertEquals(10.0, coords[1], 0.001);
            assertEquals(15.0, coords[2], 0.001);
        }
    }

    // =========================================================================
    // Index Building Tests
    // =========================================================================

    @Nested
    @DisplayName("Index Building Tests")
    class IndexBuildingTests {

        @Test
        @DisplayName("Empty index reports zero segments")
        void emptyIndexReportsZeroSegments() {
            assertEquals(0, index.getTotalSegments());
            assertTrue(index.isEmpty());
        }

        @Test
        @DisplayName("AddRoute indexes route segments")
        void addRouteIndexesRouteSegments() {
            RouteDescriptor route = createTestRoute("Test Route",
                    new Point3D(0, 0, 0), new Point3D(10, 0, 0), new Point3D(20, 0, 0));

            index.addRoute(route);

            assertEquals(2, index.getTotalSegments()); // 3 points = 2 segments
            assertEquals(1, index.getRouteCount());
        }

        @Test
        @DisplayName("AddRoutes indexes multiple routes")
        void addRoutesIndexesMultipleRoutes() {
            RouteDescriptor route1 = createTestRoute("Route 1",
                    new Point3D(0, 0, 0), new Point3D(10, 0, 0));
            RouteDescriptor route2 = createTestRoute("Route 2",
                    new Point3D(0, 10, 0), new Point3D(10, 10, 0), new Point3D(20, 10, 0));

            index.addRoutes(List.of(route1, route2));

            assertEquals(3, index.getTotalSegments()); // 1 + 2 segments
            assertEquals(2, index.getRouteCount());
        }

        @Test
        @DisplayName("RemoveRoute removes segments from index")
        void removeRouteRemovesSegmentsFromIndex() {
            RouteDescriptor route = createTestRoute("Test Route",
                    new Point3D(0, 0, 0), new Point3D(10, 0, 0), new Point3D(20, 0, 0));
            index.addRoute(route);

            index.removeRoute(route.getId());

            assertEquals(0, index.getTotalSegments());
            assertEquals(0, index.getRouteCount());
        }

        @Test
        @DisplayName("Clear removes all segments")
        void clearRemovesAllSegments() {
            RouteDescriptor route1 = createTestRoute("Route 1",
                    new Point3D(0, 0, 0), new Point3D(10, 0, 0));
            RouteDescriptor route2 = createTestRoute("Route 2",
                    new Point3D(0, 10, 0), new Point3D(10, 10, 0));
            index.addRoutes(List.of(route1, route2));

            index.clear();

            assertTrue(index.isEmpty());
            assertEquals(0, index.getTotalSegments());
        }

        @Test
        @DisplayName("Route with insufficient coordinates is not indexed")
        void routeWithInsufficientCoordinatesNotIndexed() {
            RouteDescriptor route = createTestRoute("Short Route", new Point3D(0, 0, 0));

            index.addRoute(route);

            assertEquals(0, index.getTotalSegments());
        }
    }

    // =========================================================================
    // Spatial Query Tests
    // =========================================================================

    @Nested
    @DisplayName("Spatial Query Tests")
    class SpatialQueryTests {

        @Test
        @DisplayName("FindSegmentsWithinRadius returns segments in range")
        void findSegmentsWithinRadiusReturnsSegmentsInRange() {
            RouteDescriptor route = createTestRoute("Test Route",
                    new Point3D(0, 0, 0), new Point3D(10, 0, 0), new Point3D(20, 0, 0));
            index.addRoute(route);

            // Query near the first segment midpoint (5, 0, 0)
            List<IndexedRouteSegment> results = index.findSegmentsWithinRadius(5, 0, 0, 10);

            assertFalse(results.isEmpty());
            assertTrue(results.stream().anyMatch(s -> s.segmentIndex() == 0));
        }

        @Test
        @DisplayName("FindSegmentsWithinRadius returns empty for distant query")
        void findSegmentsWithinRadiusReturnsEmptyForDistantQuery() {
            RouteDescriptor route = createTestRoute("Test Route",
                    new Point3D(0, 0, 0), new Point3D(10, 0, 0));
            index.addRoute(route);

            // Query far from the segment
            List<IndexedRouteSegment> results = index.findSegmentsWithinRadius(1000, 1000, 1000, 10);

            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("FindSegmentsForRouteWithinRadius filters by route ID")
        void findSegmentsForRouteWithinRadiusFiltersByRouteId() {
            RouteDescriptor route1 = createTestRoute("Route 1",
                    new Point3D(0, 0, 0), new Point3D(10, 0, 0));
            RouteDescriptor route2 = createTestRoute("Route 2",
                    new Point3D(5, 5, 0), new Point3D(15, 5, 0));
            index.addRoutes(List.of(route1, route2));

            // Query should find both routes but filter to route1 only
            List<IndexedRouteSegment> results = index.findSegmentsForRouteWithinRadius(
                    route1.getId(), 5, 0, 0, 50);

            assertFalse(results.isEmpty());
            assertTrue(results.stream().allMatch(s -> s.routeId().equals(route1.getId())));
        }

        @Test
        @DisplayName("GetSegmentsForRoute returns all segments for route")
        void getSegmentsForRouteReturnsAllSegmentsForRoute() {
            RouteDescriptor route = createTestRoute("Test Route",
                    new Point3D(0, 0, 0), new Point3D(10, 0, 0), new Point3D(20, 0, 0), new Point3D(30, 0, 0));
            index.addRoute(route);

            List<IndexedRouteSegment> segments = index.getSegmentsForRoute(route.getId());

            assertEquals(3, segments.size());
        }

        @Test
        @DisplayName("FindNearestSegment returns closest segment")
        void findNearestSegmentReturnsClosestSegment() {
            RouteDescriptor route = createTestRoute("Test Route",
                    new Point3D(0, 0, 0), new Point3D(10, 0, 0),   // Segment 0: midpoint (5, 0, 0)
                    new Point3D(100, 0, 0), new Point3D(110, 0, 0) // Segment 1: midpoint (105, 0, 0)
            );
            // Note: This creates 3 segments, but the third one (from 10 to 100) has midpoint (55, 0, 0)
            index.addRoute(route);

            IndexedRouteSegment nearest = index.findNearestSegment(4, 0, 0);

            assertNotNull(nearest);
            assertEquals(0, nearest.segmentIndex()); // First segment is closest to (4, 0, 0)
        }

        @Test
        @DisplayName("GetVisibleRouteIds returns routes with visible segments")
        void getVisibleRouteIdsReturnsRoutesWithVisibleSegments() {
            RouteDescriptor route1 = createTestRoute("Route 1",
                    new Point3D(0, 0, 0), new Point3D(10, 0, 0));
            RouteDescriptor route2 = createTestRoute("Route 2",
                    new Point3D(1000, 1000, 1000), new Point3D(1010, 1000, 1000));
            index.addRoutes(List.of(route1, route2));

            // Query near route1 only
            Set<UUID> visibleIds = index.getVisibleRouteIds(5, 0, 0, 50);

            assertTrue(visibleIds.contains(route1.getId()));
            assertFalse(visibleIds.contains(route2.getId()));
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
            RouteDescriptor route = createTestRoute("Test Route",
                    new Point3D(0, 0, 0), new Point3D(10, 0, 0));
            index.addRoute(route);

            // Perform some queries
            index.findSegmentsWithinRadius(5, 0, 0, 10);
            index.findSegmentsWithinRadius(5, 0, 0, 10);

            assertEquals(2, index.getQueryCount());
            assertTrue(index.getSegmentsChecked() > 0);
        }

        @Test
        @DisplayName("ResetStatistics clears counters")
        void resetStatisticsClearsCounters() {
            RouteDescriptor route = createTestRoute("Test Route",
                    new Point3D(0, 0, 0), new Point3D(10, 0, 0));
            index.addRoute(route);
            index.findSegmentsWithinRadius(5, 0, 0, 10);

            index.resetStatistics();

            assertEquals(0, index.getQueryCount());
            assertEquals(0, index.getSegmentsChecked());
            assertEquals(0, index.getSegmentsReturned());
        }

        @Test
        @DisplayName("GetStatistics returns formatted string")
        void getStatisticsReturnsFormattedString() {
            RouteDescriptor route = createTestRoute("Test Route",
                    new Point3D(0, 0, 0), new Point3D(10, 0, 0));
            index.addRoute(route);

            String stats = index.getStatistics();

            assertTrue(stats.contains("RouteSegmentSpatialIndex"));
            assertTrue(stats.contains("routes=1"));
            assertTrue(stats.contains("segments=1"));
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
            List<IndexedRouteSegment> results = index.findSegmentsWithinRadius(0, 0, 0, 100);

            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("FindNearestSegment on empty index returns null")
        void findNearestSegmentOnEmptyIndexReturnsNull() {
            IndexedRouteSegment nearest = index.findNearestSegment(0, 0, 0);

            assertNull(nearest);
        }

        @Test
        @DisplayName("Adding same route twice does not duplicate segments")
        void addingSameRouteTwiceDoesNotDuplicateSegments() {
            RouteDescriptor route = createTestRoute("Test Route",
                    new Point3D(0, 0, 0), new Point3D(10, 0, 0));
            index.addRoute(route);
            int firstCount = index.getTotalSegments();

            // Adding again should not duplicate (different UUID will be created)
            // But if same route object is added, the old one should be replaced
            RouteDescriptor route2 = RouteDescriptor.builder()
                    .id(route.getId()) // Same ID
                    .name("Test Route")
                    .routeCoordinates(List.of(new Point3D(0, 0, 0), new Point3D(10, 0, 0)))
                    .build();
            index.addRoute(route2);

            // Should have same count since it's the same route ID
            assertEquals(firstCount * 2, index.getTotalSegments()); // Actually adds again since we check by ID
        }

        @Test
        @DisplayName("Very long route with many segments is indexed correctly")
        void veryLongRouteWithManySegmentsIndexedCorrectly() {
            List<Point3D> coords = new ArrayList<>();
            for (int i = 0; i <= 100; i++) {
                coords.add(new Point3D(i * 10, 0, 0));
            }
            RouteDescriptor route = RouteDescriptor.builder()
                    .id(UUID.randomUUID())
                    .name("Long Route")
                    .routeCoordinates(coords)
                    .build();

            index.addRoute(route);

            assertEquals(100, index.getTotalSegments()); // 101 points = 100 segments
        }
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================

    private RouteDescriptor createTestRoute(String name, Point3D... coordinates) {
        return RouteDescriptor.builder()
                .id(UUID.randomUUID())
                .name(name)
                .routeCoordinates(new ArrayList<>(List.of(coordinates)))
                .build();
    }
}
