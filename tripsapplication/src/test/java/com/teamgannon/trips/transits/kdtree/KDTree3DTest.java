package com.teamgannon.trips.transits.kdtree;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for KDTree3D spatial indexing.
 */
class KDTree3DTest {

    private static final double EPS = 1e-9;

    // =========================================================================
    // Construction Tests
    // =========================================================================

    @Nested
    @DisplayName("Construction Tests")
    class ConstructionTests {

        @Test
        @DisplayName("Empty tree has size 0")
        void emptyTreeHasSizeZero() {
            KDTree3D<String> tree = new KDTree3D<>(List.of());

            assertEquals(0, tree.size());
            assertTrue(tree.isEmpty());
        }

        @Test
        @DisplayName("Single point tree has size 1")
        void singlePointTreeHasSizeOne() {
            KDTree3D<String> tree = new KDTree3D<>(List.of(
                    new KDPoint<>(0, 0, 0, "origin")
            ));

            assertEquals(1, tree.size());
            assertFalse(tree.isEmpty());
        }

        @Test
        @DisplayName("Tree with multiple points has correct size")
        void multiplePointsCorrectSize() {
            List<KDPoint<String>> points = List.of(
                    new KDPoint<>(0, 0, 0, "a"),
                    new KDPoint<>(1, 1, 1, "b"),
                    new KDPoint<>(2, 2, 2, "c"),
                    new KDPoint<>(-1, -1, -1, "d")
            );

            KDTree3D<String> tree = new KDTree3D<>(points);

            assertEquals(4, tree.size());
        }

        @Test
        @DisplayName("Tree handles large point count")
        void handlesLargePointCount() {
            List<KDPoint<Integer>> points = new ArrayList<>();
            for (int i = 0; i < 10000; i++) {
                points.add(new KDPoint<>(i * 0.1, i * 0.2, i * 0.3, i));
            }

            KDTree3D<Integer> tree = new KDTree3D<>(points);

            assertEquals(10000, tree.size());
        }

        @Test
        @DisplayName("Tree handles duplicate coordinates")
        void handlesDuplicateCoordinates() {
            List<KDPoint<String>> points = List.of(
                    new KDPoint<>(1, 1, 1, "first"),
                    new KDPoint<>(1, 1, 1, "second"),
                    new KDPoint<>(1, 1, 1, "third")
            );

            KDTree3D<String> tree = new KDTree3D<>(points);

            assertEquals(3, tree.size());
        }
    }

    // =========================================================================
    // Range Search Tests
    // =========================================================================

    @Nested
    @DisplayName("Range Search Tests")
    class RangeSearchTests {

        private KDTree3D<String> tree;

        @BeforeEach
        void setUp() {
            List<KDPoint<String>> points = List.of(
                    new KDPoint<>(0, 0, 0, "origin"),
                    new KDPoint<>(1, 0, 0, "x1"),
                    new KDPoint<>(0, 1, 0, "y1"),
                    new KDPoint<>(0, 0, 1, "z1"),
                    new KDPoint<>(5, 5, 5, "far"),
                    new KDPoint<>(-1, -1, -1, "negative")
            );
            tree = new KDTree3D<>(points);
        }

        @Test
        @DisplayName("Range search finds points within radius")
        void findsPointsWithinRadius() {
            List<KDPoint<String>> results = tree.rangeSearch(0, 0, 0, 1.5);

            assertEquals(4, results.size());
            Set<String> names = new HashSet<>();
            for (KDPoint<String> p : results) {
                names.add(p.data());
            }
            assertTrue(names.contains("origin"));
            assertTrue(names.contains("x1"));
            assertTrue(names.contains("y1"));
            assertTrue(names.contains("z1"));
        }

        @Test
        @DisplayName("Range search excludes points outside radius")
        void excludesPointsOutsideRadius() {
            List<KDPoint<String>> results = tree.rangeSearch(0, 0, 0, 0.5);

            assertEquals(1, results.size());
            assertEquals("origin", results.get(0).data());
        }

        @Test
        @DisplayName("Range search finds all points with large radius")
        void findsAllWithLargeRadius() {
            List<KDPoint<String>> results = tree.rangeSearch(0, 0, 0, 100);

            assertEquals(6, results.size());
        }

        @Test
        @DisplayName("Range search returns empty for no matches")
        void returnsEmptyForNoMatches() {
            List<KDPoint<String>> results = tree.rangeSearch(100, 100, 100, 1);

            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("Range search on empty tree returns empty list")
        void emptyTreeReturnsEmpty() {
            KDTree3D<String> emptyTree = new KDTree3D<>(List.of());

            List<KDPoint<String>> results = emptyTree.rangeSearch(0, 0, 0, 10);

            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("Range search with zero radius finds only exact match")
        void zeroRadiusFindsExactMatch() {
            List<KDPoint<String>> results = tree.rangeSearch(0, 0, 0, 0);

            assertEquals(1, results.size());
            assertEquals("origin", results.get(0).data());
        }

        @Test
        @DisplayName("Range search with array coordinates works")
        void arrayCoordinatesWork() {
            double[] query = {0, 0, 0};
            List<KDPoint<String>> results = tree.rangeSearch(query, 1.5);

            assertEquals(4, results.size());
        }
    }

    // =========================================================================
    // Nearest Neighbor Tests
    // =========================================================================

    @Nested
    @DisplayName("Nearest Neighbor Tests")
    class NearestNeighborTests {

        @Test
        @DisplayName("Finds nearest neighbor correctly")
        void findsNearestNeighbor() {
            List<KDPoint<String>> points = List.of(
                    new KDPoint<>(0, 0, 0, "origin"),
                    new KDPoint<>(10, 10, 10, "far")
            );
            KDTree3D<String> tree = new KDTree3D<>(points);

            KDPoint<String> nearest = tree.nearestNeighbor(new double[]{1, 1, 1});

            assertNotNull(nearest);
            assertEquals("origin", nearest.data());
        }

        @Test
        @DisplayName("Nearest neighbor on empty tree returns null")
        void emptyTreeReturnsNull() {
            KDTree3D<String> tree = new KDTree3D<>(List.of());

            KDPoint<String> nearest = tree.nearestNeighbor(new double[]{0, 0, 0});

            assertNull(nearest);
        }

        @Test
        @DisplayName("Exact match returns that point")
        void exactMatchReturnsPoint() {
            List<KDPoint<String>> points = List.of(
                    new KDPoint<>(5, 5, 5, "target"),
                    new KDPoint<>(0, 0, 0, "origin")
            );
            KDTree3D<String> tree = new KDTree3D<>(points);

            KDPoint<String> nearest = tree.nearestNeighbor(new double[]{5, 5, 5});

            assertNotNull(nearest);
            assertEquals("target", nearest.data());
        }
    }

    // =========================================================================
    // Correctness Verification Tests
    // =========================================================================

    @Nested
    @DisplayName("Correctness Verification")
    class CorrectnessTests {

        @Test
        @DisplayName("Range search matches brute force for random data")
        void matchesBruteForce() {
            // Generate random points
            Random random = new Random(42);
            List<KDPoint<Integer>> points = new ArrayList<>();
            for (int i = 0; i < 500; i++) {
                double x = random.nextDouble() * 100 - 50;
                double y = random.nextDouble() * 100 - 50;
                double z = random.nextDouble() * 100 - 50;
                points.add(new KDPoint<>(x, y, z, i));
            }

            KDTree3D<Integer> tree = new KDTree3D<>(points);

            // Test multiple random queries
            for (int q = 0; q < 20; q++) {
                double qx = random.nextDouble() * 100 - 50;
                double qy = random.nextDouble() * 100 - 50;
                double qz = random.nextDouble() * 100 - 50;
                double radius = random.nextDouble() * 20;

                // KD-Tree result
                List<KDPoint<Integer>> treeResult = tree.rangeSearch(qx, qy, qz, radius);

                // Brute-force result
                Set<Integer> bruteForce = new HashSet<>();
                for (KDPoint<Integer> p : points) {
                    double dist = Math.sqrt(
                            Math.pow(p.x() - qx, 2) +
                                    Math.pow(p.y() - qy, 2) +
                                    Math.pow(p.z() - qz, 2)
                    );
                    if (dist <= radius) {
                        bruteForce.add(p.data());
                    }
                }

                // Compare
                Set<Integer> treeResultSet = new HashSet<>();
                for (KDPoint<Integer> p : treeResult) {
                    treeResultSet.add(p.data());
                }

                assertEquals(bruteForce, treeResultSet,
                        "Query (%.2f, %.2f, %.2f) r=%.2f".formatted(qx, qy, qz, radius));
            }
        }

        @Test
        @DisplayName("All returned points are within radius")
        void allResultsWithinRadius() {
            Random random = new Random(123);
            List<KDPoint<Integer>> points = new ArrayList<>();
            for (int i = 0; i < 1000; i++) {
                points.add(new KDPoint<>(
                        random.nextDouble() * 200 - 100,
                        random.nextDouble() * 200 - 100,
                        random.nextDouble() * 200 - 100,
                        i
                ));
            }

            KDTree3D<Integer> tree = new KDTree3D<>(points);
            double[] query = {0, 0, 0};
            double radius = 25;

            List<KDPoint<Integer>> results = tree.rangeSearch(query, radius);

            for (KDPoint<Integer> result : results) {
                double dist = result.distanceTo(query);
                assertTrue(dist <= radius + EPS,
                        "Point at distance %.4f exceeds radius %.4f".formatted(dist, radius));
            }
        }
    }

    // =========================================================================
    // Performance Characteristics Tests
    // =========================================================================

    @Nested
    @DisplayName("Performance Characteristics")
    class PerformanceTests {

        @Test
        @DisplayName("KD-Tree is faster than brute force for large datasets")
        void fasterThanBruteForce() {
            // Generate large random dataset
            List<KDPoint<Integer>> points = new ArrayList<>();
            for (int i = 0; i < 5000; i++) {
                points.add(new KDPoint<>(
                        ThreadLocalRandom.current().nextDouble() * 1000,
                        ThreadLocalRandom.current().nextDouble() * 1000,
                        ThreadLocalRandom.current().nextDouble() * 1000,
                        i
                ));
            }

            // Build tree (includes construction time)
            long startTree = System.nanoTime();
            KDTree3D<Integer> tree = new KDTree3D<>(points);

            // Run 100 queries
            int queryCount = 100;
            for (int i = 0; i < queryCount; i++) {
                tree.rangeSearch(
                        ThreadLocalRandom.current().nextDouble() * 1000,
                        ThreadLocalRandom.current().nextDouble() * 1000,
                        ThreadLocalRandom.current().nextDouble() * 1000,
                        50
                );
            }
            long treeTime = System.nanoTime() - startTree;

            // Brute force comparison
            long startBrute = System.nanoTime();
            for (int i = 0; i < queryCount; i++) {
                double qx = ThreadLocalRandom.current().nextDouble() * 1000;
                double qy = ThreadLocalRandom.current().nextDouble() * 1000;
                double qz = ThreadLocalRandom.current().nextDouble() * 1000;
                double radius = 50;

                for (KDPoint<Integer> p : points) {
                    double dist = Math.sqrt(
                            Math.pow(p.x() - qx, 2) +
                                    Math.pow(p.y() - qy, 2) +
                                    Math.pow(p.z() - qz, 2)
                    );
                    if (dist <= radius) {
                        // Would add to results
                    }
                }
            }
            long bruteTime = System.nanoTime() - startBrute;

            // KD-Tree should be significantly faster
            assertTrue(treeTime < bruteTime,
                    "KD-Tree (%d ms) should be faster than brute force (%d ms)".formatted(
                            treeTime / 1_000_000, bruteTime / 1_000_000));
        }
    }

    // =========================================================================
    // Edge Cases
    // =========================================================================

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Handles negative coordinates")
        void handlesNegativeCoordinates() {
            List<KDPoint<String>> points = List.of(
                    new KDPoint<>(-10, -20, -30, "negative"),
                    new KDPoint<>(10, 20, 30, "positive")
            );
            KDTree3D<String> tree = new KDTree3D<>(points);

            List<KDPoint<String>> results = tree.rangeSearch(-10, -20, -30, 1);

            assertEquals(1, results.size());
            assertEquals("negative", results.get(0).data());
        }

        @Test
        @DisplayName("Handles very small coordinates")
        void handlesSmallCoordinates() {
            List<KDPoint<String>> points = List.of(
                    new KDPoint<>(1e-10, 1e-10, 1e-10, "tiny"),
                    new KDPoint<>(0, 0, 0, "origin")
            );
            KDTree3D<String> tree = new KDTree3D<>(points);

            List<KDPoint<String>> results = tree.rangeSearch(0, 0, 0, 1e-9);

            assertEquals(2, results.size());
        }

        @Test
        @DisplayName("Handles very large coordinates")
        void handlesLargeCoordinates() {
            List<KDPoint<String>> points = List.of(
                    new KDPoint<>(1e10, 1e10, 1e10, "huge"),
                    new KDPoint<>(1e10 + 1, 1e10, 1e10, "nearHuge")
            );
            KDTree3D<String> tree = new KDTree3D<>(points);

            List<KDPoint<String>> results = tree.rangeSearch(1e10, 1e10, 1e10, 2);

            assertEquals(2, results.size());
        }

        @Test
        @DisplayName("Invalid query dimensions throws exception")
        void invalidDimensionsThrows() {
            KDTree3D<String> tree = new KDTree3D<>(List.of(
                    new KDPoint<>(0, 0, 0, "origin")
            ));

            assertThrows(IllegalArgumentException.class, () ->
                    tree.rangeSearch(new double[]{0, 0}, 1) // Only 2D
            );
        }
    }

    // =========================================================================
    // KDPoint Tests
    // =========================================================================

    @Nested
    @DisplayName("KDPoint Tests")
    class KDPointTests {

        @Test
        @DisplayName("Convenience constructor works")
        void convenienceConstructor() {
            KDPoint<String> point = new KDPoint<>(1, 2, 3, "test");

            assertEquals(1, point.x(), EPS);
            assertEquals(2, point.y(), EPS);
            assertEquals(3, point.z(), EPS);
            assertEquals("test", point.data());
        }

        @Test
        @DisplayName("distanceTo calculates correctly")
        void distanceToCalculatesCorrectly() {
            KDPoint<String> p1 = new KDPoint<>(0, 0, 0, "a");
            KDPoint<String> p2 = new KDPoint<>(3, 4, 0, "b");

            assertEquals(5.0, p1.distanceTo(p2), EPS);
        }

        @Test
        @DisplayName("distanceTo with array works")
        void distanceToArray() {
            KDPoint<String> p = new KDPoint<>(0, 0, 0, "origin");

            assertEquals(5.0, p.distanceTo(new double[]{3, 4, 0}), EPS);
        }

        @Test
        @DisplayName("toString is informative")
        void toStringIsInformative() {
            KDPoint<String> p = new KDPoint<>(1.5, 2.5, 3.5, "data");

            String str = p.toString();

            assertTrue(str.contains("1.5"));
            assertTrue(str.contains("2.5"));
            assertTrue(str.contains("3.5"));
            assertTrue(str.contains("data"));
        }
    }
}
