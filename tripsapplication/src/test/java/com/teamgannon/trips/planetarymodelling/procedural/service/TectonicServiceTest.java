package com.teamgannon.trips.planetarymodelling.procedural.service;

import com.teamgannon.trips.planetarymodelling.procedural.PlanetConfig;
import com.teamgannon.trips.planetarymodelling.procedural.PlanetGenerator.GeneratedPlanet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.data.Percentage.withPercentage;

class TectonicServiceTest {

    private TectonicService service;

    @BeforeEach
    void setUp() {
        service = new TectonicService();
    }

    @Test
    @DisplayName("generate() returns non-null planet")
    void generateReturnsNonNull() {
        var config = PlanetConfig.builder()
            .seed(12345L)
            .size(PlanetConfig.Size.DUEL)
            .build();

        GeneratedPlanet planet = service.generate(config);

        assertThat(planet).isNotNull();
    }

    @Test
    @DisplayName("generate() caches results")
    void generateCachesResults() {
        var config = PlanetConfig.builder()
            .seed(12345L)
            .size(PlanetConfig.Size.DUEL)
            .build();

        GeneratedPlanet planet1 = service.generate(config);
        GeneratedPlanet planet2 = service.generate(config);

        assertThat(planet1).isSameAs(planet2);
    }

    @Test
    @DisplayName("Different configs get different cache entries")
    void differentConfigsDifferentCache() {
        var config1 = PlanetConfig.builder()
            .seed(12345L)
            .size(PlanetConfig.Size.DUEL)
            .build();
        var config2 = PlanetConfig.builder()
            .seed(12345L)
            .size(PlanetConfig.Size.TINY)  // Different size, same seed
            .build();

        GeneratedPlanet planet1 = service.generate(config1);
        GeneratedPlanet planet2 = service.generate(config2);

        assertThat(planet1).isNotSameAs(planet2);
        assertThat(planet1.polygons().size()).isNotEqualTo(planet2.polygons().size());
    }

    @Test
    @DisplayName("regenerate() bypasses cache")
    void regenerateBypassesCache() {
        var config = PlanetConfig.builder()
            .seed(12345L)
            .size(PlanetConfig.Size.DUEL)
            .build();

        GeneratedPlanet planet1 = service.generate(config);
        GeneratedPlanet planet2 = service.regenerate(config);

        // regenerate creates a new instance even with same config
        assertThat(planet1).isNotSameAs(planet2);
        // But with same seed, heights should be equal
        assertThat(planet1.heights()).isEqualTo(planet2.heights());
    }

    @Test
    @DisplayName("getCached() returns null when not cached")
    void getCachedReturnsNullWhenNotCached() {
        var config = PlanetConfig.builder()
            .seed(99999L)
            .size(PlanetConfig.Size.DUEL)
            .build();

        assertThat(service.getCached(config)).isNull();
    }

    @Test
    @DisplayName("getCached() returns cached planet")
    void getCachedReturnsCachedPlanet() {
        var config = PlanetConfig.builder()
            .seed(12345L)
            .size(PlanetConfig.Size.DUEL)
            .build();

        GeneratedPlanet generated = service.generate(config);
        GeneratedPlanet cached = service.getCached(config);

        assertThat(cached).isSameAs(generated);
    }

    @Test
    @DisplayName("evict() removes from cache")
    void evictRemovesFromCache() {
        var config = PlanetConfig.builder()
            .seed(12345L)
            .size(PlanetConfig.Size.DUEL)
            .build();

        service.generate(config);
        assertThat(service.getCached(config)).isNotNull();

        service.evict(config);
        assertThat(service.getCached(config)).isNull();
    }

    @Test
    @DisplayName("clearCache() removes all entries")
    void clearCacheRemovesAll() {
        var config1 = PlanetConfig.builder().seed(111L).size(PlanetConfig.Size.DUEL).build();
        var config2 = PlanetConfig.builder().seed(222L).size(PlanetConfig.Size.DUEL).build();

        service.generate(config1);
        service.generate(config2);

        service.clearCache();

        assertThat(service.getCached(config1)).isNull();
        assertThat(service.getCached(config2)).isNull();
    }

    @Test
    @DisplayName("generateFromAccrete() creates planet with correct parameters")
    void generateFromAccrete() {
        GeneratedPlanet planet = service.generateFromAccrete(42L, 6371.0, 1.0, 0.66);

        assertThat(planet).isNotNull();
        assertThat(planet.config().seed()).isEqualTo(42L);
        assertThat(planet.config().radius()).isEqualTo(6371.0);
    }

    @Test
    @DisplayName("generateFromAccrete() selects appropriate size based on radius")
    void generateFromAccreteSelectsSize() {
        // Small planet (radius < 3000 km)
        GeneratedPlanet small = service.generateFromAccrete(1L, 2000.0, 0.3, 0.5);
        assertThat(small.config().n()).isEqualTo(PlanetConfig.Size.SMALL.n);

        service.clearCache();

        // Large planet (radius 6000-10000 km)
        GeneratedPlanet large = service.generateFromAccrete(2L, 8000.0, 3.0, 0.7);
        assertThat(large.config().n()).isEqualTo(PlanetConfig.Size.LARGE.n);
    }

    @Test
    @DisplayName("generateFromAccrete() calculates plate count from mass")
    void generateFromAccreteCalculatesPlateCount() {
        // Low mass planet (< 0.5 Earth masses) -> 7 plates
        GeneratedPlanet lowMass = service.generateFromAccrete(1L, 3000.0, 0.3, 0.5);
        assertThat(lowMass.config().plateCount()).isEqualTo(7);

        service.clearCache();

        // High mass planet (> 5 Earth masses) -> 21 plates
        GeneratedPlanet highMass = service.generateFromAccrete(2L, 15000.0, 8.0, 0.5);
        assertThat(highMass.config().plateCount()).isEqualTo(21);
    }

    @Test
    @DisplayName("AccreteInput.generate() calls service correctly")
    void accreteInputGenerate() {
        var input = new TectonicService.AccreteInput(
            12345L, 6371.0, 1.0, 0.66, 288.0, 1.0
        );

        GeneratedPlanet planet = input.generate(service);

        assertThat(planet).isNotNull();
        assertThat(planet.config().seed()).isEqualTo(12345L);
    }

    @Test
    @DisplayName("PlanetSummary.from() calculates correct statistics")
    void planetSummaryFrom() {
        var config = PlanetConfig.builder()
            .seed(42L)
            .size(PlanetConfig.Size.DUEL)
            .plateCount(10)
            .waterFraction(0.66)
            .build();

        GeneratedPlanet planet = service.generate(config);
        var summary = TectonicService.PlanetSummary.from(planet);

        assertThat(summary.seed()).isEqualTo(42L);
        assertThat(summary.polyCount()).isEqualTo(planet.heights().length);
        assertThat(summary.plateCount()).isEqualTo(10);
        assertThat(summary.waterFraction() + summary.landFraction()).isEqualTo(1.0);
        assertThat(summary.heightDistribution()).hasSize(9);
    }

    @Test
    @DisplayName("PlanetSummary height distribution sums to polyCount")
    void planetSummaryHeightDistributionSums() {
        var config = PlanetConfig.builder()
            .seed(42L)
            .size(PlanetConfig.Size.DUEL)
            .build();

        GeneratedPlanet planet = service.generate(config);
        var summary = TectonicService.PlanetSummary.from(planet);

        int sum = 0;
        for (int count : summary.heightDistribution()) {
            sum += count;
        }

        assertThat(sum).isEqualTo(summary.polyCount());
    }

    @Test
    @DisplayName("PlanetSummary water fraction matches height distribution")
    void planetSummaryWaterFractionMatchesDistribution() {
        var config = PlanetConfig.builder()
            .seed(42L)
            .size(PlanetConfig.Size.DUEL)
            .build();

        GeneratedPlanet planet = service.generate(config);
        var summary = TectonicService.PlanetSummary.from(planet);

        // Water is heights -4 to -1 (indices 0-3 in distribution)
        int waterCount = summary.heightDistribution()[0]
            + summary.heightDistribution()[1]
            + summary.heightDistribution()[2]
            + summary.heightDistribution()[3];

        double expectedWater = (double) waterCount / summary.polyCount();

        assertThat(summary.waterFraction())
            .isCloseTo(expectedWater, withPercentage(0.1));
    }

    @Test
    @DisplayName("Service is thread-safe for concurrent generation")
    void threadSafeConcurrentGeneration() throws InterruptedException {
        int threadCount = 4;
        Thread[] threads = new Thread[threadCount];
        GeneratedPlanet[] results = new GeneratedPlanet[threadCount];

        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            var config = PlanetConfig.builder()
                .seed(idx * 1000L)
                .size(PlanetConfig.Size.DUEL)
                .build();

            threads[i] = new Thread(() -> {
                results[idx] = service.generate(config);
            });
        }

        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();

        for (GeneratedPlanet p : results) {
            assertThat(p).isNotNull();
        }
    }
}
