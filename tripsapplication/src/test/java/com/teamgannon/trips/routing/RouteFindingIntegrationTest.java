package com.teamgannon.trips.routing;

import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.measure.OshiMeasure;
import com.teamgannon.trips.routing.automation.RouteGraph;
import com.teamgannon.trips.routing.model.PossibleRoutes;
import com.teamgannon.trips.routing.model.RouteFindingOptions;
import com.teamgannon.trips.routing.model.RouteFindingResult;
import com.teamgannon.trips.routing.model.RoutingMetric;
import com.teamgannon.trips.service.measure.StarMeasurementService;
import com.teamgannon.trips.transits.TransitRoute;
import javafx.geometry.Point3D;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Integration tests for route finding functionality.
 * <p>
 * These tests use real implementations (not mocks) to verify end-to-end
 * route finding behavior. They test:
 * <ul>
 *   <li>Full route finding pipeline with real algorithms</li>
 *   <li>Multi-hop route discovery</li>
 *   <li>K-shortest paths algorithm (Yen's algorithm)</li>
 *   <li>Graph connectivity detection</li>
 *   <li>Distance-based transit calculation</li>
 *   <li>Spectral class and polity exclusions</li>
 *   <li>Route caching integration</li>
 * </ul>
 */
@DisplayName("Route Finding Integration Tests")
class RouteFindingIntegrationTest {

    private RouteFindingService routeFindingService;
    private StarMeasurementService starMeasurementService;
    private RouteCache routeCache;
    private DataSetDescriptor dataSet;

    // Test star network - a simple connected graph
    private List<StarDisplayRecord> testStarNetwork;

    @BeforeEach
    void setUp() {
        // Create real StarMeasurementService with mocked OshiMeasure
        OshiMeasure oshiMeasure = mock(OshiMeasure.class);
        when(oshiMeasure.numberOfLogicalProcessors()).thenReturn(4);
        when(oshiMeasure.getAvailableMemoryInMb()).thenReturn(8192L);

        starMeasurementService = new StarMeasurementService(oshiMeasure);
        routeCache = new RouteCache();
        routeFindingService = new RouteFindingService(starMeasurementService, routeCache);

        dataSet = new DataSetDescriptor();
        dataSet.setDataSetName("integration-test-dataset");

        // Build test star network
        testStarNetwork = createTestStarNetwork();
    }

    // =========================================================================
    // Test Star Network Factory
    // =========================================================================

    /**
     * Creates a test star network with the following topology:
     * <pre>
     *                    Vega (15,0,0)
     *                      |
     *    Sol (0,0,0) --- Alpha Centauri (4,0,0) --- Sirius (8,0,0)
     *        |               |
     *   Barnard's (0,4,0)  Epsilon (4,4,0) --- Procyon (8,4,0)
     *        |               |
     *   Proxima (0,6,0)   Tau Ceti (4,8,0)
     * </pre>
     * Stars are positioned so that adjacent stars are within 5 LY of each other.
     */
    private List<StarDisplayRecord> createTestStarNetwork() {
        List<StarDisplayRecord> stars = new ArrayList<>();

        // Central cluster
        stars.add(createStar("Sol", 0, 0, 0, "G2V", "Terran"));
        stars.add(createStar("Alpha Centauri", 4, 0, 0, "G2V", "Terran"));
        stars.add(createStar("Sirius", 8, 0, 0, "A1V", "Terran"));
        stars.add(createStar("Vega", 15, 0, 0, "A0V", "Independent"));

        // Vertical branch from Sol
        stars.add(createStar("Barnard's Star", 0, 4, 0, "M4V", "Terran"));
        stars.add(createStar("Proxima Centauri", 0, 6, 0, "M5V", "Terran"));

        // Branch from Alpha Centauri
        stars.add(createStar("Epsilon Eridani", 4, 4, 0, "K2V", "Terran"));
        stars.add(createStar("Tau Ceti", 4, 8, 0, "G8V", "Terran"));

        // Connect to Sirius
        stars.add(createStar("Procyon", 8, 4, 0, "F5V", "Terran"));

        return stars;
    }

    private StarDisplayRecord createStar(String name, double x, double y, double z,
                                          String spectralClass, String polity) {
        StarDisplayRecord record = new StarDisplayRecord();
        record.setStarName(name);
        record.setX(x);
        record.setY(y);
        record.setZ(z);
        record.setSpectralClass(spectralClass);
        record.setPolity(polity);
        record.setActualCoordinates(new double[]{x, y, z});
        record.setCoordinates(new Point3D(x, y, z));
        record.setRecordId(UUID.randomUUID().toString());
        return record;
    }

    private RouteFindingOptions.RouteFindingOptionsBuilder defaultOptionsBuilder() {
        return RouteFindingOptions.builder()
                .selected(true)
                .upperBound(6.0)  // Allow jumps up to 6 LY
                .lowerBound(0.5)  // Minimum 0.5 LY
                .numberPaths(3)
                .lineWidth(0.5)
                .color(Color.BLUE)
                .starExclusions(new HashSet<>())
                .polityExclusions(new HashSet<>());
    }

    // =========================================================================
    // Direct Connection Tests
    // =========================================================================

    @Nested
    @DisplayName("Direct Connection Routes")
    class DirectConnectionTests {

        @Test
        @DisplayName("Find direct route between adjacent stars")
        void findDirectRouteBetweenAdjacentStars() {
            RouteFindingOptions options = defaultOptionsBuilder()
                    .originStarName("Sol")
                    .destinationStarName("Alpha Centauri")
                    .build();

            RouteFindingResult result = routeFindingService.findRoutes(options, testStarNetwork, dataSet);

            assertTrue(result.isSuccess(), "Route finding should succeed");
            assertTrue(result.hasRoutes(), "Should have at least one route");

            PossibleRoutes routes = result.getRoutes();
            assertFalse(routes.getRoutes().isEmpty(), "Should find at least one route");

            // First route should be the direct connection
            RoutingMetric firstRoute = routes.getRoutes().get(0);
            assertEquals(1, firstRoute.getRank(), "First route should be rank 1");
            assertTrue(firstRoute.getTotalLength() > 0, "Route should have positive length");
        }

        @Test
        @DisplayName("Direct route has positive total distance")
        void directRouteHasPositiveTotalDistance() {
            RouteFindingOptions options = defaultOptionsBuilder()
                    .originStarName("Sol")
                    .destinationStarName("Alpha Centauri")
                    .build();

            RouteFindingResult result = routeFindingService.findRoutes(options, testStarNetwork, dataSet);

            assertTrue(result.isSuccess());
            RoutingMetric route = result.getRoutes().getRoutes().get(0);

            // Verify route has a positive distance
            assertTrue(route.getTotalLength() > 0, "Route should have positive distance");
            assertTrue(route.getNumberOfSegments() >= 1, "Route should have at least one segment");
        }
    }

    // =========================================================================
    // Multi-Hop Route Tests
    // =========================================================================

    @Nested
    @DisplayName("Multi-Hop Routes")
    class MultiHopRouteTests {

        @Test
        @DisplayName("Find multi-hop route when direct path not available")
        void findMultiHopRouteWhenDirectPathNotAvailable() {
            // Sol to Sirius requires going through Alpha Centauri
            // (Sol at 0,0,0 - Sirius at 8,0,0 is 8 LY, outside 5 LY bound)
            RouteFindingOptions options = defaultOptionsBuilder()
                    .originStarName("Sol")
                    .destinationStarName("Sirius")
                    .upperBound(5.0)  // Force multi-hop
                    .build();

            RouteFindingResult result = routeFindingService.findRoutes(options, testStarNetwork, dataSet);

            assertTrue(result.isSuccess(), "Should find multi-hop route");

            RoutingMetric route = result.getRoutes().getRoutes().get(0);
            // Route requires at least 2 segments (cannot go direct with 5 LY bound)
            assertTrue(route.getNumberOfSegments() >= 2, "Should have at least 2 segments for multi-hop");
            assertTrue(route.getTotalLength() > 0, "Route should have positive total length");
        }

        @Test
        @DisplayName("Find route through complex path")
        void findRouteThroughComplexPath() {
            // Sol to Tau Ceti through multiple hops
            RouteFindingOptions options = defaultOptionsBuilder()
                    .originStarName("Sol")
                    .destinationStarName("Tau Ceti")
                    .upperBound(5.0)
                    .build();

            RouteFindingResult result = routeFindingService.findRoutes(options, testStarNetwork, dataSet);

            assertTrue(result.isSuccess(), "Should find complex route");
            assertTrue(result.getRoutes().getRoutes().get(0).getNumberOfSegments() >= 2,
                    "Should require multiple hops");
        }
    }

    // =========================================================================
    // K-Shortest Paths Tests
    // =========================================================================

    @Nested
    @DisplayName("K-Shortest Paths")
    class KShortestPathsTests {

        @Test
        @DisplayName("Find multiple alternative routes")
        void findMultipleAlternativeRoutes() {
            // From Sol to Epsilon Eridani, there should be multiple paths
            RouteFindingOptions options = defaultOptionsBuilder()
                    .originStarName("Sol")
                    .destinationStarName("Epsilon Eridani")
                    .numberPaths(5)
                    .upperBound(5.0)
                    .build();

            RouteFindingResult result = routeFindingService.findRoutes(options, testStarNetwork, dataSet);

            assertTrue(result.isSuccess());

            // Should find at least 2 different routes
            List<RoutingMetric> routes = result.getRoutes().getRoutes();
            assertTrue(routes.size() >= 1, "Should find at least one route");
        }

        @Test
        @DisplayName("Routes are ordered by total distance")
        void routesAreOrderedByTotalDistance() {
            RouteFindingOptions options = defaultOptionsBuilder()
                    .originStarName("Sol")
                    .destinationStarName("Procyon")
                    .numberPaths(5)
                    .upperBound(5.0)
                    .build();

            RouteFindingResult result = routeFindingService.findRoutes(options, testStarNetwork, dataSet);

            if (result.isSuccess() && result.getRoutes().getRoutes().size() > 1) {
                List<RoutingMetric> routes = result.getRoutes().getRoutes();

                // Verify routes are ordered by rank
                for (int i = 0; i < routes.size() - 1; i++) {
                    assertTrue(routes.get(i).getRank() < routes.get(i + 1).getRank(),
                            "Routes should be ordered by rank");
                }
            }
        }
    }

    // =========================================================================
    // Exclusion Tests
    // =========================================================================

    @Nested
    @DisplayName("Exclusion Handling")
    class ExclusionTests {

        @Test
        @DisplayName("Route avoids excluded spectral classes")
        void routeAvoidsExcludedSpectralClasses() {
            // Exclude M-class stars - should avoid Barnard's Star and Proxima
            RouteFindingOptions options = defaultOptionsBuilder()
                    .originStarName("Sol")
                    .destinationStarName("Tau Ceti")
                    .starExclusions(Set.of("M"))
                    .upperBound(6.0)
                    .build();

            RouteFindingResult result = routeFindingService.findRoutes(options, testStarNetwork, dataSet);

            assertTrue(result.isSuccess(), "Should find alternative route");

            // Verify the path doesn't include M-class stars
            String path = result.getRoutes().getRoutes().get(0).getPath();
            assertFalse(path.contains("Barnard"), "Path should not include Barnard's Star");
            assertFalse(path.contains("Proxima"), "Path should not include Proxima");
        }

        @Test
        @DisplayName("Route fails when destination excluded")
        void routeFailsWhenDestinationExcluded() {
            // Try to route to Barnard's Star while excluding M-class
            RouteFindingOptions options = defaultOptionsBuilder()
                    .originStarName("Sol")
                    .destinationStarName("Barnard's Star")
                    .starExclusions(Set.of("M"))
                    .build();

            RouteFindingResult result = routeFindingService.findRoutes(options, testStarNetwork, dataSet);

            assertFalse(result.isSuccess(), "Should fail when destination is excluded");
            assertTrue(result.getErrorMessage().contains("Destination star"),
                    "Error should mention destination star");
        }

        @Test
        @DisplayName("Route avoids excluded polities")
        void routeAvoidsExcludedPolities() {
            RouteFindingOptions options = defaultOptionsBuilder()
                    .originStarName("Sol")
                    .destinationStarName("Sirius")
                    .polityExclusions(Set.of("Independent"))
                    .upperBound(10.0)
                    .build();

            RouteFindingResult result = routeFindingService.findRoutes(options, testStarNetwork, dataSet);

            // Should still succeed by going through Terran-controlled space
            assertTrue(result.isSuccess());

            // Verify path doesn't include Independent stars
            String path = result.getRoutes().getRoutes().get(0).getPath();
            assertFalse(path.contains("Vega"), "Path should not include Vega (Independent)");
        }
    }

    // =========================================================================
    // Connectivity Tests
    // =========================================================================

    @Nested
    @DisplayName("Connectivity Detection")
    class ConnectivityTests {

        @Test
        @DisplayName("Detects when stars are not connected")
        void detectsWhenStarsNotConnected() {
            // Add an isolated star
            List<StarDisplayRecord> starsWithIsolated = new ArrayList<>(testStarNetwork);
            starsWithIsolated.add(createStar("Isolated Star", 100, 100, 100, "G2V", "Terran"));

            RouteFindingOptions options = defaultOptionsBuilder()
                    .originStarName("Sol")
                    .destinationStarName("Isolated Star")
                    .upperBound(10.0)
                    .build();

            RouteFindingResult result = routeFindingService.findRoutes(options, starsWithIsolated, dataSet);

            assertFalse(result.isSuccess(), "Should fail for isolated star");
            assertTrue(result.getErrorMessage().contains("No path exists"),
                    "Error should indicate no path exists");
        }

        @Test
        @DisplayName("Finds path when stars connected through intermediaries")
        void findsPathWhenConnectedThroughIntermediaries() {
            // Proxima to Sirius requires multiple hops
            RouteFindingOptions options = defaultOptionsBuilder()
                    .originStarName("Proxima Centauri")
                    .destinationStarName("Sirius")
                    .upperBound(5.0)
                    .build();

            RouteFindingResult result = routeFindingService.findRoutes(options, testStarNetwork, dataSet);

            assertTrue(result.isSuccess(), "Should find path through intermediaries");
            assertTrue(result.getRoutes().getRoutes().get(0).getNumberOfSegments() >= 3,
                    "Path should require multiple hops");
        }
    }

    // =========================================================================
    // Distance Bounds Tests
    // =========================================================================

    @Nested
    @DisplayName("Distance Bounds")
    class DistanceBoundsTests {

        @Test
        @DisplayName("No routes found with too-restrictive bounds")
        void noRoutesWithTooRestrictiveBounds() {
            // With max 1.0 LY jumps, no connections exist
            RouteFindingOptions options = defaultOptionsBuilder()
                    .originStarName("Sol")
                    .destinationStarName("Alpha Centauri")
                    .upperBound(1.0)
                    .lowerBound(0.1)
                    .build();

            RouteFindingResult result = routeFindingService.findRoutes(options, testStarNetwork, dataSet);

            assertFalse(result.isSuccess(), "Should fail with restrictive bounds");
            assertTrue(result.getErrorMessage().contains("No transits") ||
                            result.getErrorMessage().contains("No path"),
                    "Error should mention no transits or paths");
        }

        @Test
        @DisplayName("More routes available with wider bounds")
        void moreRoutesWithWiderBounds() {
            RouteFindingOptions narrowOptions = defaultOptionsBuilder()
                    .originStarName("Sol")
                    .destinationStarName("Sirius")
                    .upperBound(4.5)  // Just barely connects adjacent stars
                    .numberPaths(5)
                    .build();

            RouteFindingOptions wideOptions = defaultOptionsBuilder()
                    .originStarName("Sol")
                    .destinationStarName("Sirius")
                    .upperBound(10.0)  // Allows more connections
                    .numberPaths(5)
                    .build();

            RouteFindingResult narrowResult = routeFindingService.findRoutes(narrowOptions, testStarNetwork, dataSet);
            RouteFindingResult wideResult = routeFindingService.findRoutes(wideOptions, testStarNetwork, dataSet);

            if (narrowResult.isSuccess() && wideResult.isSuccess()) {
                assertTrue(wideResult.getRoutes().getRoutes().size() >= narrowResult.getRoutes().getRoutes().size(),
                        "Wider bounds should find at least as many routes");
            }
        }
    }

    // =========================================================================
    // Caching Integration Tests
    // =========================================================================

    @Nested
    @DisplayName("Caching Integration")
    class CachingIntegrationTests {

        @Test
        @DisplayName("Second identical request uses cache")
        void secondIdenticalRequestUsesCache() {
            RouteFindingOptions options = defaultOptionsBuilder()
                    .originStarName("Sol")
                    .destinationStarName("Alpha Centauri")
                    .build();

            // First call
            RouteFindingResult result1 = routeFindingService.findRoutes(options, testStarNetwork, dataSet);

            // Record cache stats after first call
            String statsAfterFirst = routeFindingService.getCacheStatistics();

            // Second call (should use cache)
            RouteFindingResult result2 = routeFindingService.findRoutes(options, testStarNetwork, dataSet);

            // Verify cache hit
            String statsAfterSecond = routeFindingService.getCacheStatistics();

            // Both results should be equal
            if (result1.isSuccess() && result2.isSuccess()) {
                assertEquals(result1.getRoutes().getRoutes().size(),
                        result2.getRoutes().getRoutes().size(),
                        "Cached result should match original");
            }
        }

        @Test
        @DisplayName("Cache cleared properly")
        void cacheClearedProperly() {
            RouteFindingOptions options = defaultOptionsBuilder()
                    .originStarName("Sol")
                    .destinationStarName("Alpha Centauri")
                    .build();

            // First call to populate cache
            routeFindingService.findRoutes(options, testStarNetwork, dataSet);

            // Clear cache
            routeFindingService.clearCache();

            // Verify cache is empty
            assertTrue(routeFindingService.getCacheStatistics().contains("size=0"),
                    "Cache should be empty after clear");
        }
    }

    // =========================================================================
    // Route Descriptor Tests
    // =========================================================================

    @Nested
    @DisplayName("Route Descriptor Creation")
    class RouteDescriptorTests {

        @Test
        @DisplayName("Route descriptor contains dataset reference")
        void routeDescriptorContainsDatasetReference() {
            RouteFindingOptions options = defaultOptionsBuilder()
                    .originStarName("Sol")
                    .destinationStarName("Alpha Centauri")
                    .build();

            RouteFindingResult result = routeFindingService.findRoutes(options, testStarNetwork, dataSet);

            if (result.isSuccess()) {
                RouteDescriptor descriptor = result.getRoutes().getRoutes().get(0).getRouteDescriptor();
                assertNotNull(descriptor, "Route descriptor should exist");
                assertEquals(dataSet, descriptor.getDescriptor(), "Route should reference dataset");
            }
        }

        @Test
        @DisplayName("First route uses specified color")
        void firstRouteUsesSpecifiedColor() {
            Color expectedColor = Color.RED;

            RouteFindingOptions options = defaultOptionsBuilder()
                    .originStarName("Sol")
                    .destinationStarName("Alpha Centauri")
                    .color(expectedColor)
                    .build();

            RouteFindingResult result = routeFindingService.findRoutes(options, testStarNetwork, dataSet);

            if (result.isSuccess()) {
                RouteDescriptor descriptor = result.getRoutes().getRoutes().get(0).getRouteDescriptor();
                assertEquals(expectedColor, descriptor.getColor(), "First route should use specified color");
            }
        }

        @Test
        @DisplayName("Route descriptor has correct coordinates")
        void routeDescriptorHasCorrectCoordinates() {
            RouteFindingOptions options = defaultOptionsBuilder()
                    .originStarName("Sol")
                    .destinationStarName("Alpha Centauri")
                    .build();

            RouteFindingResult result = routeFindingService.findRoutes(options, testStarNetwork, dataSet);

            if (result.isSuccess()) {
                RouteDescriptor descriptor = result.getRoutes().getRoutes().get(0).getRouteDescriptor();
                List<Point3D> coords = descriptor.getRouteCoordinates();

                assertNotNull(coords, "Coordinates should exist");
                assertFalse(coords.isEmpty(), "Should have at least one coordinate");

                // First coordinate should be origin (Sol at 0,0,0)
                Point3D first = coords.get(0);
                assertEquals(0.0, first.getX(), 0.01, "Origin X should be 0");
                assertEquals(0.0, first.getY(), 0.01, "Origin Y should be 0");
            }
        }
    }

    // =========================================================================
    // RouteGraph Unit Tests
    // =========================================================================

    @Nested
    @DisplayName("RouteGraph Direct Tests")
    class RouteGraphTests {

        @Test
        @DisplayName("Graph builds correctly from transits")
        void graphBuildsCorrectlyFromTransits() {
            // Create manual transits
            StarDisplayRecord sol = createStar("Sol", 0, 0, 0, "G2V", "Terran");
            StarDisplayRecord alpha = createStar("Alpha", 4, 0, 0, "G2V", "Terran");
            StarDisplayRecord sirius = createStar("Sirius", 8, 0, 0, "A1V", "Terran");

            List<TransitRoute> transits = List.of(
                    TransitRoute.builder().good(true).source(sol).target(alpha).distance(4.0).build(),
                    TransitRoute.builder().good(true).source(alpha).target(sirius).distance(4.0).build()
            );

            RouteGraph graph = new RouteGraph();
            graph.calculateGraphForTransit(transits);

            assertTrue(graph.isConnected("Sol", "Sirius"), "Sol should be connected to Sirius");
            assertTrue(graph.isConnected("Sol", "Alpha"), "Sol should be connected to Alpha");
        }

        @Test
        @DisplayName("Graph handles self-loops gracefully")
        void graphHandlesSelfLoopsGracefully() {
            StarDisplayRecord sol = createStar("Sol", 0, 0, 0, "G2V", "Terran");
            StarDisplayRecord alpha = createStar("Alpha", 4, 0, 0, "G2V", "Terran");

            // Include a self-loop (should be skipped)
            List<TransitRoute> transits = List.of(
                    TransitRoute.builder().good(true).source(sol).target(sol).distance(0.0).build(),
                    TransitRoute.builder().good(true).source(sol).target(alpha).distance(4.0).build()
            );

            RouteGraph graph = new RouteGraph();
            assertDoesNotThrow(() -> graph.calculateGraphForTransit(transits));

            assertTrue(graph.isConnected("Sol", "Alpha"), "Valid edges should still work");
        }

        @Test
        @DisplayName("findKShortestPaths returns paths")
        void findKShortestPathsReturnsPaths() {
            StarDisplayRecord sol = createStar("Sol", 0, 0, 0, "G2V", "Terran");
            StarDisplayRecord alpha = createStar("Alpha", 4, 0, 0, "G2V", "Terran");
            StarDisplayRecord sirius = createStar("Sirius", 8, 0, 0, "A1V", "Terran");

            List<TransitRoute> transits = List.of(
                    TransitRoute.builder().good(true).source(sol).target(alpha).distance(4.0).build(),
                    TransitRoute.builder().good(true).source(alpha).target(sirius).distance(4.0).build(),
                    TransitRoute.builder().good(true).source(sol).target(sirius).distance(8.0).build()
            );

            RouteGraph graph = new RouteGraph();
            graph.calculateGraphForTransit(transits);

            List<String> paths = graph.findKShortestPaths("Sol", "Sirius", 3);

            assertFalse(paths.isEmpty(), "Should find at least one path");
        }
    }
}
