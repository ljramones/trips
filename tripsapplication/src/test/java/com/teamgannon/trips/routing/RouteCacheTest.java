package com.teamgannon.trips.routing;

import com.teamgannon.trips.routing.model.RouteCacheKey;
import com.teamgannon.trips.routing.model.RouteFindingOptions;
import com.teamgannon.trips.routing.model.RouteFindingResult;
import com.teamgannon.trips.routing.model.PossibleRoutes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RouteCache.
 * <p>
 * Tests cover:
 * <ul>
 *   <li>Basic get/put operations</li>
 *   <li>Cache misses and hits</li>
 *   <li>LRU eviction</li>
 *   <li>Statistics tracking</li>
 *   <li>Cache clearing</li>
 *   <li>Thread safety</li>
 * </ul>
 */
@DisplayName("RouteCache")
class RouteCacheTest {

    private RouteCache cache;

    @BeforeEach
    void setUp() {
        cache = new RouteCache();
    }

    // =========================================================================
    // Test Helpers
    // =========================================================================

    private RouteFindingOptions createOptions(String origin, String destination) {
        return RouteFindingOptions.builder()
                .originStarName(origin)
                .destinationStarName(destination)
                .upperBound(8.0)
                .lowerBound(3.0)
                .numberPaths(3)
                .starExclusions(new HashSet<>())
                .polityExclusions(new HashSet<>())
                .build();
    }

    private RouteFindingResult createSuccessResult() {
        PossibleRoutes routes = new PossibleRoutes();
        routes.setDesiredPath("Test Route");
        return RouteFindingResult.success(routes);
    }

    private RouteFindingResult createFailureResult() {
        return RouteFindingResult.failure("Test failure");
    }

    // =========================================================================
    // Basic Operations Tests
    // =========================================================================

    @Nested
    @DisplayName("Basic Operations")
    class BasicOperationsTests {

        @Test
        @DisplayName("Get returns empty for missing key")
        void getReturnsEmptyForMissingKey() {
            RouteFindingOptions options = createOptions("Sol", "Alpha Centauri");

            Optional<RouteFindingResult> result = cache.get(options);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Get returns value after put")
        void getReturnsValueAfterPut() {
            RouteFindingOptions options = createOptions("Sol", "Alpha Centauri");
            RouteFindingResult expectedResult = createSuccessResult();

            cache.put(options, expectedResult);
            Optional<RouteFindingResult> result = cache.get(options);

            assertTrue(result.isPresent());
            assertEquals(expectedResult, result.get());
        }

        @Test
        @DisplayName("Failed results are not cached")
        void failedResultsAreNotCached() {
            RouteFindingOptions options = createOptions("Sol", "Alpha Centauri");
            RouteFindingResult failureResult = createFailureResult();

            cache.put(options, failureResult);
            Optional<RouteFindingResult> result = cache.get(options);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Size increases after put")
        void sizeIncreasesAfterPut() {
            assertEquals(0, cache.size());

            cache.put(createOptions("Sol", "Alpha Centauri"), createSuccessResult());
            assertEquals(1, cache.size());

            cache.put(createOptions("Sol", "Sirius"), createSuccessResult());
            assertEquals(2, cache.size());
        }

        @Test
        @DisplayName("Same key overwrites previous value")
        void sameKeyOverwritesPreviousValue() {
            RouteFindingOptions options = createOptions("Sol", "Alpha Centauri");

            PossibleRoutes routes1 = new PossibleRoutes();
            routes1.setDesiredPath("First Route");
            cache.put(options, RouteFindingResult.success(routes1));

            PossibleRoutes routes2 = new PossibleRoutes();
            routes2.setDesiredPath("Second Route");
            cache.put(options, RouteFindingResult.success(routes2));

            assertEquals(1, cache.size());
            Optional<RouteFindingResult> result = cache.get(options);
            assertTrue(result.isPresent());
            assertEquals("Second Route", result.get().getRoutes().getDesiredPath());
        }
    }

    // =========================================================================
    // Cache Key Tests
    // =========================================================================

    @Nested
    @DisplayName("Cache Key Behavior")
    class CacheKeyTests {

        @Test
        @DisplayName("Get with RouteCacheKey works")
        void getWithRouteCacheKeyWorks() {
            RouteFindingOptions options = createOptions("Sol", "Alpha Centauri");
            RouteCacheKey key = RouteCacheKey.fromOptions(options);
            RouteFindingResult expectedResult = createSuccessResult();

            cache.put(key, expectedResult);
            Optional<RouteFindingResult> result = cache.get(key);

            assertTrue(result.isPresent());
        }

        @Test
        @DisplayName("Equivalent options produce same cache key")
        void equivalentOptionsProduceSameCacheKey() {
            RouteFindingOptions options1 = createOptions("Sol", "Alpha Centauri");
            RouteFindingOptions options2 = createOptions("Sol", "Alpha Centauri");
            RouteFindingResult expectedResult = createSuccessResult();

            cache.put(options1, expectedResult);
            Optional<RouteFindingResult> result = cache.get(options2);

            assertTrue(result.isPresent());
        }
    }

    // =========================================================================
    // LRU Eviction Tests
    // =========================================================================

    @Nested
    @DisplayName("LRU Eviction")
    class LruEvictionTests {

        @Test
        @DisplayName("Cache evicts oldest entries when full")
        void cacheEvictsOldestEntriesWhenFull() {
            // Create cache with small size
            RouteCache smallCache = new RouteCache(3);

            // Fill cache
            smallCache.put(createOptions("A", "B"), createSuccessResult());
            smallCache.put(createOptions("C", "D"), createSuccessResult());
            smallCache.put(createOptions("E", "F"), createSuccessResult());

            assertEquals(3, smallCache.size());

            // Add one more - should evict oldest (A -> B)
            smallCache.put(createOptions("G", "H"), createSuccessResult());

            assertEquals(3, smallCache.size());
            assertTrue(smallCache.get(createOptions("A", "B")).isEmpty()); // Evicted
            assertTrue(smallCache.get(createOptions("G", "H")).isPresent()); // Present
        }

        @Test
        @DisplayName("Accessing entry prevents eviction")
        void accessingEntryPreventsEviction() {
            RouteCache smallCache = new RouteCache(3);

            // Fill cache
            smallCache.put(createOptions("A", "B"), createSuccessResult());
            smallCache.put(createOptions("C", "D"), createSuccessResult());
            smallCache.put(createOptions("E", "F"), createSuccessResult());

            // Access oldest entry to make it most recently used
            smallCache.get(createOptions("A", "B"));

            // Add one more - should evict C -> D (now oldest)
            smallCache.put(createOptions("G", "H"), createSuccessResult());

            assertTrue(smallCache.get(createOptions("A", "B")).isPresent()); // Still present
            assertTrue(smallCache.get(createOptions("C", "D")).isEmpty()); // Evicted
        }
    }

    // =========================================================================
    // Statistics Tests
    // =========================================================================

    @Nested
    @DisplayName("Statistics")
    class StatisticsTests {

        @Test
        @DisplayName("Hits are tracked")
        void hitsAreTracked() {
            RouteFindingOptions options = createOptions("Sol", "Alpha Centauri");
            cache.put(options, createSuccessResult());

            cache.get(options);
            cache.get(options);
            cache.get(options);

            assertEquals(3, cache.getHits());
        }

        @Test
        @DisplayName("Misses are tracked")
        void missesAreTracked() {
            cache.get(createOptions("A", "B"));
            cache.get(createOptions("C", "D"));

            assertEquals(2, cache.getMisses());
        }

        @Test
        @DisplayName("Statistics string is formatted correctly")
        void statisticsStringIsFormattedCorrectly() {
            RouteFindingOptions options = createOptions("Sol", "Alpha Centauri");
            cache.put(options, createSuccessResult());

            cache.get(options); // Hit
            cache.get(options); // Hit
            cache.get(createOptions("Missing", "Star")); // Miss

            String stats = cache.getStatistics();

            assertTrue(stats.contains("size=1"));
            assertTrue(stats.contains("hits=2"));
            assertTrue(stats.contains("misses=1"));
            assertTrue(stats.contains("hitRate=66")); // 66.7%
        }

        @Test
        @DisplayName("Statistics can be reset")
        void statisticsCanBeReset() {
            RouteFindingOptions options = createOptions("Sol", "Alpha Centauri");
            cache.put(options, createSuccessResult());
            cache.get(options);
            cache.get(createOptions("Missing", "Star"));

            cache.resetStatistics();

            assertEquals(0, cache.getHits());
            assertEquals(0, cache.getMisses());
            assertEquals(1, cache.size()); // Data still present
        }
    }

    // =========================================================================
    // Clear Tests
    // =========================================================================

    @Nested
    @DisplayName("Clear")
    class ClearTests {

        @Test
        @DisplayName("Clear removes all entries")
        void clearRemovesAllEntries() {
            cache.put(createOptions("A", "B"), createSuccessResult());
            cache.put(createOptions("C", "D"), createSuccessResult());
            cache.put(createOptions("E", "F"), createSuccessResult());

            cache.clear();

            assertEquals(0, cache.size());
            assertTrue(cache.get(createOptions("A", "B")).isEmpty());
        }

        @Test
        @DisplayName("Clear can be called on empty cache")
        void clearCanBeCalledOnEmptyCache() {
            assertDoesNotThrow(() -> cache.clear());
            assertEquals(0, cache.size());
        }
    }

    // =========================================================================
    // Thread Safety Tests
    // =========================================================================

    @Nested
    @DisplayName("Thread Safety")
    class ThreadSafetyTests {

        @Test
        @DisplayName("Concurrent puts do not lose data")
        void concurrentPutsDoNotLoseData() throws InterruptedException {
            int threadCount = 10;
            int operationsPerThread = 100;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            AtomicInteger successCount = new AtomicInteger(0);

            for (int t = 0; t < threadCount; t++) {
                final int threadId = t;
                executor.submit(() -> {
                    try {
                        for (int i = 0; i < operationsPerThread; i++) {
                            String key = "Thread" + threadId + "-" + i;
                            cache.put(createOptions(key, "Destination"), createSuccessResult());
                            successCount.incrementAndGet();
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            assertTrue(latch.await(10, TimeUnit.SECONDS));
            executor.shutdown();

            // All puts should succeed (though some may be evicted due to LRU)
            assertEquals(threadCount * operationsPerThread, successCount.get());
            assertTrue(cache.size() <= 200); // Default max size
        }

        @Test
        @DisplayName("Concurrent reads and writes are safe")
        void concurrentReadsAndWritesAreSafe() throws InterruptedException {
            int threadCount = 10;
            int operationsPerThread = 100;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            AtomicInteger exceptionCount = new AtomicInteger(0);

            // Pre-populate some entries
            for (int i = 0; i < 10; i++) {
                cache.put(createOptions("Key" + i, "Dest"), createSuccessResult());
            }

            for (int t = 0; t < threadCount; t++) {
                final int threadId = t;
                executor.submit(() -> {
                    try {
                        for (int i = 0; i < operationsPerThread; i++) {
                            if (i % 2 == 0) {
                                // Read
                                cache.get(createOptions("Key" + (i % 10), "Dest"));
                            } else {
                                // Write
                                cache.put(createOptions("Thread" + threadId + "-" + i, "Dest"), createSuccessResult());
                            }
                        }
                    } catch (Exception e) {
                        exceptionCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            assertTrue(latch.await(10, TimeUnit.SECONDS));
            executor.shutdown();

            assertEquals(0, exceptionCount.get(), "No exceptions should occur during concurrent access");
        }
    }

    // =========================================================================
    // Edge Cases
    // =========================================================================

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Null origin star name is handled")
        void nullOriginStarNameIsHandled() {
            RouteFindingOptions options = RouteFindingOptions.builder()
                    .originStarName(null)
                    .destinationStarName("Alpha Centauri")
                    .upperBound(8.0)
                    .lowerBound(3.0)
                    .numberPaths(3)
                    .build();

            assertDoesNotThrow(() -> cache.put(options, createSuccessResult()));
            assertDoesNotThrow(() -> cache.get(options));
        }

        @Test
        @DisplayName("Empty exclusion sets are handled")
        void emptyExclusionSetsAreHandled() {
            RouteFindingOptions options = RouteFindingOptions.builder()
                    .originStarName("Sol")
                    .destinationStarName("Alpha Centauri")
                    .upperBound(8.0)
                    .lowerBound(3.0)
                    .numberPaths(3)
                    .starExclusions(new HashSet<>())
                    .polityExclusions(new HashSet<>())
                    .build();

            assertDoesNotThrow(() -> cache.put(options, createSuccessResult()));
            assertTrue(cache.get(options).isPresent());
        }

        @Test
        @DisplayName("Cache with size 1 works correctly")
        void cacheWithSizeOneWorksCorrectly() {
            RouteCache tinyCache = new RouteCache(1);

            tinyCache.put(createOptions("A", "B"), createSuccessResult());
            assertTrue(tinyCache.get(createOptions("A", "B")).isPresent());

            tinyCache.put(createOptions("C", "D"), createSuccessResult());
            assertTrue(tinyCache.get(createOptions("A", "B")).isEmpty()); // Evicted
            assertTrue(tinyCache.get(createOptions("C", "D")).isPresent());
        }
    }
}
