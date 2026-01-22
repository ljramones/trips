package com.teamgannon.trips.routing;

import com.teamgannon.trips.routing.model.RouteCacheKey;
import com.teamgannon.trips.routing.model.RouteFindingOptions;
import com.teamgannon.trips.routing.model.RouteFindingResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * In-memory cache for route finding results.
 * <p>
 * This cache stores successful route finding results keyed by algorithmic parameters
 * (origin, destination, distance bounds, number of paths, exclusions). Display-only
 * parameters like color and line width do not affect cache key identity.
 * <p>
 * <b>Features:</b>
 * <ul>
 *   <li>Thread-safe with read/write lock</li>
 *   <li>LRU eviction when max size is reached</li>
 *   <li>Manual invalidation on dataset changes</li>
 *   <li>Statistics tracking (hits, misses)</li>
 * </ul>
 * <p>
 * <b>Cache Invalidation:</b>
 * <p>
 * The cache should be cleared when:
 * <ul>
 *   <li>Dataset changes (new data loaded)</li>
 *   <li>Star data is modified</li>
 *   <li>User explicitly requests cache clear</li>
 * </ul>
 */
@Slf4j
@Component
public class RouteCache {

    /**
     * Default maximum number of cached routes.
     */
    private static final int DEFAULT_MAX_SIZE = 50;

    /**
     * The cache map with LRU eviction.
     */
    private final Map<RouteCacheKey, RouteFindingResult> cache;

    /**
     * Lock for thread-safe access.
     */
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Statistics: cache hits.
     */
    private long hits = 0;

    /**
     * Statistics: cache misses.
     */
    private long misses = 0;

    /**
     * Creates a new RouteCache with default max size.
     */
    public RouteCache() {
        this(DEFAULT_MAX_SIZE);
    }

    /**
     * Creates a new RouteCache with specified max size.
     *
     * @param maxSize maximum number of entries
     */
    public RouteCache(int maxSize) {
        // LinkedHashMap with access-order for LRU behavior
        this.cache = new LinkedHashMap<>(maxSize, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<RouteCacheKey, RouteFindingResult> eldest) {
                boolean shouldRemove = size() > maxSize;
                if (shouldRemove) {
                    log.debug("Evicting LRU cache entry: {}", eldest.getKey());
                }
                return shouldRemove;
            }
        };
        log.info("Route cache initialized with max size: {}", maxSize);
    }

    /**
     * Looks up a cached result for the given options.
     *
     * @param options the route finding options
     * @return the cached result if present
     */
    public Optional<RouteFindingResult> get(RouteFindingOptions options) {
        RouteCacheKey key = RouteCacheKey.fromOptions(options);
        return get(key);
    }

    /**
     * Looks up a cached result for the given key.
     *
     * @param key the cache key
     * @return the cached result if present
     */
    public Optional<RouteFindingResult> get(RouteCacheKey key) {
        lock.readLock().lock();
        try {
            RouteFindingResult result = cache.get(key);
            if (result != null) {
                hits++;
                log.debug("Cache hit for: {}", key);
                return Optional.of(result);
            } else {
                misses++;
                log.debug("Cache miss for: {}", key);
                return Optional.empty();
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Stores a result in the cache.
     * <p>
     * Only successful results are cached.
     *
     * @param options the route finding options
     * @param result  the route finding result
     */
    public void put(RouteFindingOptions options, RouteFindingResult result) {
        if (!result.isSuccess()) {
            log.debug("Not caching failed result for: {}", options.getOriginStarName());
            return;
        }

        RouteCacheKey key = RouteCacheKey.fromOptions(options);
        put(key, result);
    }

    /**
     * Stores a result in the cache.
     *
     * @param key    the cache key
     * @param result the route finding result
     */
    public void put(RouteCacheKey key, RouteFindingResult result) {
        lock.writeLock().lock();
        try {
            cache.put(key, result);
            log.debug("Cached result for: {}", key);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Clears all cached entries.
     * <p>
     * Should be called when:
     * <ul>
     *   <li>Dataset changes</li>
     *   <li>Star data is modified</li>
     *   <li>User explicitly requests cache clear</li>
     * </ul>
     */
    public void clear() {
        lock.writeLock().lock();
        try {
            int previousSize = cache.size();
            cache.clear();
            log.info("Route cache cleared ({} entries removed)", previousSize);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Gets the current cache size.
     *
     * @return number of cached entries
     */
    public int size() {
        lock.readLock().lock();
        try {
            return cache.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Gets cache statistics.
     *
     * @return cache statistics string
     */
    public String getStatistics() {
        lock.readLock().lock();
        try {
            long total = hits + misses;
            double hitRate = total > 0 ? (100.0 * hits / total) : 0.0;
            return String.format("RouteCache[size=%d, hits=%d, misses=%d, hitRate=%.1f%%]",
                    cache.size(), hits, misses, hitRate);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Resets cache statistics (but keeps cached data).
     */
    public void resetStatistics() {
        lock.writeLock().lock();
        try {
            hits = 0;
            misses = 0;
            log.debug("Cache statistics reset");
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Gets the number of cache hits.
     *
     * @return cache hits
     */
    public long getHits() {
        lock.readLock().lock();
        try {
            return hits;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Gets the number of cache misses.
     *
     * @return cache misses
     */
    public long getMisses() {
        lock.readLock().lock();
        try {
            return misses;
        } finally {
            lock.readLock().unlock();
        }
    }
}
