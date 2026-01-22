package com.teamgannon.trips.transits.kdtree;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.transits.TransitConstants;
import com.teamgannon.trips.transits.TransitRangeDef;
import com.teamgannon.trips.transits.TransitRoute;
import javafx.geometry.Point3D;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for KDTreeTransitCalculator.
 */
class KDTreeTransitCalculatorTest {

    private KDTreeTransitCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new KDTreeTransitCalculator(false); // Disable parallel for deterministic tests
    }

    // =========================================================================
    // Basic Calculation Tests
    // =========================================================================

    @Nested
    @DisplayName("Basic Calculation Tests")
    class BasicCalculationTests {

        @Test
        @DisplayName("Empty star list returns empty routes")
        void emptyStarListReturnsEmpty() {
            TransitRangeDef rangeDef = createRangeDef(0, 10);

            List<TransitRoute> routes = calculator.calculateDistances(rangeDef, List.of());

            assertTrue(routes.isEmpty());
        }

        @Test
        @DisplayName("Single star returns no routes")
        void singleStarReturnsNoRoutes() {
            TransitRangeDef rangeDef = createRangeDef(0, 10);
            List<StarDisplayRecord> stars = List.of(createStar("Sol", 0, 0, 0));

            List<TransitRoute> routes = calculator.calculateDistances(rangeDef, stars);

            assertTrue(routes.isEmpty());
        }

        @Test
        @DisplayName("Two nearby stars create one route")
        void twoNearbyStarsCreateOneRoute() {
            TransitRangeDef rangeDef = createRangeDef(0, 10);
            List<StarDisplayRecord> stars = List.of(
                    createStar("Sol", 0, 0, 0),
                    createStar("AlphaCentauri", 4.37, 0, 0)
            );

            List<TransitRoute> routes = calculator.calculateDistances(rangeDef, stars);

            assertEquals(1, routes.size());
            assertTrue(routes.get(0).isGood());
            assertEquals(4.37, routes.get(0).getDistance(), 0.01);
        }

        @Test
        @DisplayName("Two distant stars create no routes when out of range")
        void distantStarsNoRoutes() {
            TransitRangeDef rangeDef = createRangeDef(0, 5);
            List<StarDisplayRecord> stars = List.of(
                    createStar("Sol", 0, 0, 0),
                    createStar("Distant", 10, 0, 0)
            );

            List<TransitRoute> routes = calculator.calculateDistances(rangeDef, stars);

            assertTrue(routes.isEmpty());
        }

        @Test
        @DisplayName("Routes respect lower range bound")
        void routesRespectLowerBound() {
            TransitRangeDef rangeDef = createRangeDef(5, 10);
            List<StarDisplayRecord> stars = List.of(
                    createStar("Sol", 0, 0, 0),
                    createStar("Close", 3, 0, 0),   // Too close
                    createStar("InRange", 7, 0, 0)  // In range
            );

            List<TransitRoute> routes = calculator.calculateDistances(rangeDef, stars);

            assertEquals(1, routes.size());
            assertEquals(7.0, routes.get(0).getDistance(), 0.01);
        }
    }

    // =========================================================================
    // Route Properties Tests
    // =========================================================================

    @Nested
    @DisplayName("Route Properties Tests")
    class RoutePropertiesTests {

        @Test
        @DisplayName("Routes have correct source and target")
        void routesHaveCorrectSourceAndTarget() {
            TransitRangeDef rangeDef = createRangeDef(0, 10);
            StarDisplayRecord sol = createStar("Sol", 0, 0, 0);
            StarDisplayRecord alpha = createStar("AlphaCentauri", 4.37, 0, 0);

            List<TransitRoute> routes = calculator.calculateDistances(rangeDef, List.of(sol, alpha));

            assertEquals(1, routes.size());
            TransitRoute route = routes.get(0);

            // Route could be in either direction
            Set<String> starNames = Set.of(
                    route.getSource().getStarName(),
                    route.getTarget().getStarName()
            );
            assertTrue(starNames.contains("Sol"));
            assertTrue(starNames.contains("AlphaCentauri"));
        }

        @Test
        @DisplayName("Routes have correct color from range definition")
        void routesHaveCorrectColor() {
            TransitRangeDef rangeDef = createRangeDef(0, 10);
            rangeDef.setBandColor(Color.CYAN);

            List<StarDisplayRecord> stars = List.of(
                    createStar("Sol", 0, 0, 0),
                    createStar("Near", 5, 0, 0)
            );

            List<TransitRoute> routes = calculator.calculateDistances(rangeDef, stars);

            assertEquals(1, routes.size());
            assertEquals(Color.CYAN, routes.get(0).getColor());
        }

        @Test
        @DisplayName("Routes have correct line weight from range definition")
        void routesHaveCorrectLineWeight() {
            TransitRangeDef rangeDef = createRangeDef(0, 10);
            rangeDef.setLineWidth(2.5);

            List<StarDisplayRecord> stars = List.of(
                    createStar("Sol", 0, 0, 0),
                    createStar("Near", 5, 0, 0)
            );

            List<TransitRoute> routes = calculator.calculateDistances(rangeDef, stars);

            assertEquals(1, routes.size());
            assertEquals(2.5, routes.get(0).getLineWeight(), 0.001);
        }
    }

    // =========================================================================
    // No Duplicate Routes Tests
    // =========================================================================

    @Nested
    @DisplayName("No Duplicate Routes Tests")
    class NoDuplicateTests {

        @Test
        @DisplayName("No duplicate A-B and B-A routes")
        void noDuplicateRoutes() {
            TransitRangeDef rangeDef = createRangeDef(0, 10);
            List<StarDisplayRecord> stars = List.of(
                    createStar("A", 0, 0, 0),
                    createStar("B", 5, 0, 0)
            );

            List<TransitRoute> routes = calculator.calculateDistances(rangeDef, stars);

            assertEquals(1, routes.size());
        }

        @Test
        @DisplayName("Three stars create three unique routes")
        void threeStarsThreeRoutes() {
            TransitRangeDef rangeDef = createRangeDef(0, 20);
            List<StarDisplayRecord> stars = List.of(
                    createStar("A", 0, 0, 0),
                    createStar("B", 5, 0, 0),
                    createStar("C", 0, 5, 0)
            );

            List<TransitRoute> routes = calculator.calculateDistances(rangeDef, stars);

            // A-B, A-C, B-C = 3 routes
            assertEquals(3, routes.size());
        }

        @Test
        @DisplayName("No self-loops (star to itself)")
        void noSelfLoops() {
            TransitRangeDef rangeDef = createRangeDef(0, 10);
            List<StarDisplayRecord> stars = List.of(
                    createStar("Sol", 0, 0, 0)
            );

            List<TransitRoute> routes = calculator.calculateDistances(rangeDef, stars);

            assertTrue(routes.isEmpty());
        }
    }

    // =========================================================================
    // 3D Distance Calculation Tests
    // =========================================================================

    @Nested
    @DisplayName("3D Distance Calculation Tests")
    class DistanceCalculationTests {

        @Test
        @DisplayName("Calculates correct 3D distance")
        void correctThreeDDistance() {
            TransitRangeDef rangeDef = createRangeDef(0, 10);
            List<StarDisplayRecord> stars = List.of(
                    createStar("Origin", 0, 0, 0),
                    createStar("Diagonal", 3, 4, 0)  // Distance = 5
            );

            List<TransitRoute> routes = calculator.calculateDistances(rangeDef, stars);

            assertEquals(1, routes.size());
            assertEquals(5.0, routes.get(0).getDistance(), 0.001);
        }

        @Test
        @DisplayName("Calculates correct 3D distance with Z component")
        void correctThreeDDistanceWithZ() {
            TransitRangeDef rangeDef = createRangeDef(0, 10);
            List<StarDisplayRecord> stars = List.of(
                    createStar("Origin", 0, 0, 0),
                    createStar("3D", 2, 2, 1)  // Distance = 3
            );

            List<TransitRoute> routes = calculator.calculateDistances(rangeDef, stars);

            assertEquals(1, routes.size());
            assertEquals(3.0, routes.get(0).getDistance(), 0.001);
        }

        @Test
        @DisplayName("Handles negative coordinates")
        void handlesNegativeCoordinates() {
            TransitRangeDef rangeDef = createRangeDef(0, 15);
            List<StarDisplayRecord> stars = List.of(
                    createStar("Pos", 5, 5, 5),
                    createStar("Neg", -5, -5, -5)  // Distance = sqrt(300) ≈ 17.32
            );

            List<TransitRoute> routes = calculator.calculateDistances(rangeDef, stars);

            // Distance is ~17.32, which exceeds range of 15
            assertTrue(routes.isEmpty());
        }
    }

    // =========================================================================
    // Larger Dataset Tests
    // =========================================================================

    @Nested
    @DisplayName("Larger Dataset Tests")
    class LargerDatasetTests {

        @Test
        @DisplayName("Handles 100 stars correctly")
        void handles100Stars() {
            TransitRangeDef rangeDef = createRangeDef(0, 5);

            // Create 100 stars in a grid
            List<StarDisplayRecord> stars = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    stars.add(createStar("Star_" + i + "_" + j, i * 2, j * 2, 0));
                }
            }

            List<TransitRoute> routes = calculator.calculateDistances(rangeDef, stars);

            // Each internal star (8x8) has 4 neighbors at distance 2
            // Edge stars have fewer neighbors
            // The grid spacing of 2 means neighbors are at distance 2 (adjacent)
            // and sqrt(8) ≈ 2.83 (diagonal), all within range 5
            assertTrue(routes.size() > 100);
        }

        @Test
        @DisplayName("Results match brute force for random data")
        void matchesBruteForce() {
            TransitRangeDef rangeDef = createRangeDef(0, 5);

            // Generate random stars
            Random random = new Random(42);
            List<StarDisplayRecord> stars = new ArrayList<>();
            for (int i = 0; i < 50; i++) {
                stars.add(createStar("Star" + i,
                        random.nextDouble() * 20,
                        random.nextDouble() * 20,
                        random.nextDouble() * 20
                ));
            }

            List<TransitRoute> routes = calculator.calculateDistances(rangeDef, stars);

            // Verify with brute force
            Set<String> routeKeys = new HashSet<>();
            for (TransitRoute r : routes) {
                routeKeys.add(pairKey(r.getSource().getStarName(), r.getTarget().getStarName()));
            }

            Set<String> bruteForceKeys = new HashSet<>();
            for (int i = 0; i < stars.size(); i++) {
                for (int j = i + 1; j < stars.size(); j++) {
                    StarDisplayRecord a = stars.get(i);
                    StarDisplayRecord b = stars.get(j);
                    double dist = distance(a, b);
                    if (dist > rangeDef.getLowerRange() && dist <= rangeDef.getUpperRange()) {
                        bruteForceKeys.add(pairKey(a.getStarName(), b.getStarName()));
                    }
                }
            }

            assertEquals(bruteForceKeys, routeKeys);
        }

        private String pairKey(String a, String b) {
            return a.compareTo(b) < 0 ? a + "|" + b : b + "|" + a;
        }

        private double distance(StarDisplayRecord a, StarDisplayRecord b) {
            double[] ac = a.getActualCoordinates();
            double[] bc = b.getActualCoordinates();
            return Math.sqrt(
                    Math.pow(ac[0] - bc[0], 2) +
                            Math.pow(ac[1] - bc[1], 2) +
                            Math.pow(ac[2] - bc[2], 2)
            );
        }
    }

    // =========================================================================
    // Parallel Processing Tests
    // =========================================================================

    @Nested
    @DisplayName("Parallel Processing Tests")
    class ParallelTests {

        @Test
        @DisplayName("Parallel calculator produces correct results")
        void parallelProducesCorrectResults() {
            KDTreeTransitCalculator parallel = new KDTreeTransitCalculator(true);

            TransitRangeDef rangeDef = createRangeDef(0, 10);

            // Create enough stars to trigger parallel processing
            List<StarDisplayRecord> stars = new ArrayList<>();
            Random random = new Random(123);
            for (int i = 0; i < 600; i++) {
                stars.add(createStar("Star" + i,
                        random.nextDouble() * 100,
                        random.nextDouble() * 100,
                        random.nextDouble() * 100
                ));
            }

            List<TransitRoute> parRoutes = parallel.calculateDistances(rangeDef, stars);

            // Verify all routes are valid and within range
            for (TransitRoute route : parRoutes) {
                assertTrue(route.isGood());
                assertTrue(route.getDistance() > rangeDef.getLowerRange());
                assertTrue(route.getDistance() <= rangeDef.getUpperRange());
                assertNotNull(route.getSource());
                assertNotNull(route.getTarget());
                assertNotEquals(route.getSource(), route.getTarget());
            }

            // Verify no duplicate routes
            Set<String> routeKeys = new HashSet<>();
            for (TransitRoute route : parRoutes) {
                String key = normalizeRouteKey(route);
                assertFalse(routeKeys.contains(key),
                        "Duplicate route found: " + key);
                routeKeys.add(key);
            }

            // Verify we found a reasonable number of routes
            assertTrue(parRoutes.size() > 0, "Should find at least some routes");
        }

        @Test
        @DisplayName("Parallel calculator handles large dataset")
        void parallelHandlesLargeDataset() {
            KDTreeTransitCalculator parallel = new KDTreeTransitCalculator(true);

            TransitRangeDef rangeDef = createRangeDef(0, 5);

            // Create 1000 stars
            List<StarDisplayRecord> stars = new ArrayList<>();
            Random random = new Random(456);
            for (int i = 0; i < 1000; i++) {
                stars.add(createStar("Star" + i,
                        random.nextDouble() * 50,
                        random.nextDouble() * 50,
                        random.nextDouble() * 50
                ));
            }

            long startTime = System.nanoTime();
            List<TransitRoute> routes = parallel.calculateDistances(rangeDef, stars);
            long duration = System.nanoTime() - startTime;

            // Should complete in reasonable time (< 5 seconds)
            assertTrue(duration < 5_000_000_000L,
                    "Parallel calculation took too long: " + (duration / 1_000_000) + " ms");

            // Should produce some routes
            assertTrue(routes.size() > 0);
        }

        private String normalizeRouteKey(TransitRoute route) {
            String a = route.getSource().getStarName();
            String b = route.getTarget().getStarName();
            return a.compareTo(b) < 0 ? a + "|" + b : b + "|" + a;
        }
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================

    private TransitRangeDef createRangeDef(double lower, double upper) {
        TransitRangeDef def = new TransitRangeDef();
        def.setBandId(UUID.randomUUID());
        def.setBandName("Test Band");
        def.setEnabled(true);
        def.setLowerRange(lower);
        def.setUpperRange(upper);
        def.setLineWidth(TransitConstants.DEFAULT_LINE_WIDTH);
        def.setBandColor(Color.WHITE);
        return def;
    }

    private StarDisplayRecord createStar(String name, double x, double y, double z) {
        StarDisplayRecord record = new StarDisplayRecord();
        record.setStarName(name);
        record.setRecordId("id-" + name);
        record.setActualCoordinates(new double[]{x, y, z});
        record.setCoordinates(new Point3D(x, y, z));
        return record;
    }
}
