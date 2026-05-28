package com.teamgannon.trips.service.performance;

import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.jpa.model.SolarSystem;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

/**
 * Manual release-prep probe for Hibernate second-level cache effectiveness.
 * <p>
 * This intentionally is not a CI check: it measures the current local database
 * and hardware by evicting one entity from L2, timing one cold read, then
 * timing repeated warm reads through fresh EntityManagers so the first-level
 * persistence context cannot hide the result.
 */
@Slf4j
@Service
public class HibernateCacheBenchmarkService {

    private static final int DEFAULT_WARM_READS = 100;

    private final EntityManagerFactory entityManagerFactory;

    public HibernateCacheBenchmarkService(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    public @NotNull CacheBenchmarkResult benchmarkDataSetDescriptor(@NotNull String datasetName) {
        return benchmarkDataSetDescriptor(datasetName, DEFAULT_WARM_READS);
    }

    public @NotNull CacheBenchmarkResult benchmarkDataSetDescriptor(@NotNull String datasetName, int warmReads) {
        return benchmarkEntity(DataSetDescriptor.class, "DataSetDescriptor", datasetName, warmReads);
    }

    public @NotNull CacheBenchmarkResult benchmarkSolarSystem(@NotNull String solarSystemId, int warmReads) {
        return benchmarkEntity(SolarSystem.class, "SolarSystem", solarSystemId, warmReads);
    }

    private @NotNull <T> CacheBenchmarkResult benchmarkEntity(@NotNull Class<T> entityClass,
                                                             @NotNull String label,
                                                             @NotNull Object id,
                                                             int warmReads) {
        int reads = Math.max(1, warmReads);
        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        Statistics statistics = sessionFactory.getStatistics();
        boolean statisticsWereEnabled = statistics.isStatisticsEnabled();
        statistics.setStatisticsEnabled(true);
        statistics.clear();
        sessionFactory.getCache().evictEntityData(entityClass, id);

        long coldStart = System.nanoTime();
        Object cold = load(entityClass, id);
        long coldReadNanos = System.nanoTime() - coldStart;
        if (cold == null) {
            statistics.setStatisticsEnabled(statisticsWereEnabled);
            throw new IllegalArgumentException("%s not found for id '%s'".formatted(label, id));
        }

        long warmReadTotalNanos = 0;
        for (int i = 0; i < reads; i++) {
            long warmStart = System.nanoTime();
            Object warm = load(entityClass, id);
            warmReadTotalNanos += System.nanoTime() - warmStart;
            if (warm == null) {
                statistics.setStatisticsEnabled(statisticsWereEnabled);
                throw new IllegalStateException("%s disappeared during cache benchmark: %s".formatted(label, id));
            }
        }

        CacheBenchmarkResult result = new CacheBenchmarkResult(
                label,
                id.toString(),
                reads,
                coldReadNanos,
                warmReadTotalNanos,
                statistics.getSecondLevelCacheHitCount(),
                statistics.getSecondLevelCacheMissCount(),
                statistics.getSecondLevelCachePutCount());
        log.info("{}", result.summary());
        statistics.setStatisticsEnabled(statisticsWereEnabled);
        return result;
    }

    private <T> T load(Class<T> entityClass, Object id) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            return entityManager.find(entityClass, id);
        } finally {
            entityManager.close();
        }
    }

    public record CacheBenchmarkResult(String label,
                                       String id,
                                       int warmReads,
                                       long coldReadNanos,
                                       long warmReadTotalNanos,
                                       long secondLevelCacheHits,
                                       long secondLevelCacheMisses,
                                       long secondLevelCachePuts) {

        public double coldReadMillis() {
            return coldReadNanos / 1_000_000.0;
        }

        public double averageWarmReadMillis() {
            return (warmReadTotalNanos / (double) warmReads) / 1_000_000.0;
        }

        public double hitRatePercent() {
            long total = secondLevelCacheHits + secondLevelCacheMisses;
            return total == 0 ? 0.0 : (secondLevelCacheHits * 100.0) / total;
        }

        public @NotNull String summary() {
            return "%s cache benchmark id=%s: cold=%.3f ms, warm avg=%.3f ms over %d reads, L2 hits=%d, misses=%d, puts=%d, hitRate=%.1f%%"
                    .formatted(label, id, coldReadMillis(), averageWarmReadMillis(), warmReads,
                            secondLevelCacheHits, secondLevelCacheMisses, secondLevelCachePuts, hitRatePercent());
        }
    }
}
