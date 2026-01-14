package com.teamgannon.trips.nightsky.service;

import com.teamgannon.trips.nightsky.model.NightSkyRequest;
import com.teamgannon.trips.nightsky.model.NightSkyResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Performance optimization through caching of sky computations.
 * Uses ConcurrentHashMap with TTL-based expiration.
 */
@Slf4j
@Service
public class NightSkyCacheService {

    private static final Duration DEFAULT_TTL = Duration.ofMinutes(5);
    private static final int MAX_CACHE_SIZE = 100;

    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();

    /**
     * Get cached result if available and not expired.
     */
    public NightSkyResult get(NightSkyRequest request) {
        String key = computeKey(request);
        CacheEntry entry = cache.get(key);

        if (entry == null) {
            return null;
        }

        if (entry.isExpired()) {
            cache.remove(key);
            return null;
        }

        log.debug("Cache hit for key: {}", key);
        NightSkyResult result = entry.getResult();

        // Mark as from cache
        return NightSkyResult.builder()
                .stars(result.getStars())
                .hostStar(result.getHostStar())
                .totalStarsQueried(result.getTotalStarsQueried())
                .visibleCount(result.getVisibleCount())
                .computeTime(result.getComputeTime())
                .request(result.getRequest())
                .computedAt(result.getComputedAt())
                .fromCache(true)
                .build();
    }

    /**
     * Store result in cache.
     */
    public void put(NightSkyRequest request, NightSkyResult result) {
        // Evict oldest entries if cache is full
        if (cache.size() >= MAX_CACHE_SIZE) {
            evictOldest();
        }

        String key = computeKey(request);
        cache.put(key, new CacheEntry(result, DEFAULT_TTL));
        log.debug("Cached result for key: {}", key);
    }

    /**
     * Invalidate cache entry for a planet.
     */
    public void invalidate(String planetId) {
        cache.entrySet().removeIf(entry ->
                entry.getKey().startsWith(planetId));
        log.debug("Invalidated cache for planet: {}", planetId);
    }

    /**
     * Clear all cached entries.
     */
    public void clear() {
        cache.clear();
        log.info("Cache cleared");
    }

    /**
     * Get cache statistics.
     */
    public String getStats() {
        long valid = cache.values().stream()
                .filter(e -> !e.isExpired())
                .count();
        return String.format("Cache: %d entries (%d valid)", cache.size(), valid);
    }

    private String computeKey(NightSkyRequest request) {
        // Key based on planet, time (rounded to minute), and observer position
        long timeMinutes = request.getInstantUtc().toEpochMilli() / 60000;
        return String.format("%s_%d_%.2f_%.2f_%.0f_%s",
                request.getPlanetId(),
                timeMinutes,
                request.getLatRad(),
                request.getLonRad(),
                request.getRadiusLy(),
                request.getLod().name());
    }

    private void evictOldest() {
        // Remove expired entries first
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());

        // If still too full, remove oldest
        if (cache.size() >= MAX_CACHE_SIZE) {
            cache.entrySet().stream()
                    .min((a, b) -> a.getValue().getCreatedAt()
                            .compareTo(b.getValue().getCreatedAt()))
                    .ifPresent(oldest -> cache.remove(oldest.getKey()));
        }
    }

    private static class CacheEntry {
        private final NightSkyResult result;
        private final Instant expiresAt;
        private final Instant createdAt;

        CacheEntry(NightSkyResult result, Duration ttl) {
            this.result = result;
            this.createdAt = Instant.now();
            this.expiresAt = createdAt.plus(ttl);
        }

        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }

        NightSkyResult getResult() {
            return result;
        }

        Instant getCreatedAt() {
            return createdAt;
        }
    }
}
