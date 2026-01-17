package com.teamgannon.trips.planetarymodelling.procedural.service;

import com.teamgannon.trips.planetarymodelling.procedural.*;
import com.teamgannon.trips.planetarymodelling.procedural.PlanetGenerator.GeneratedPlanet;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for planet generation with caching.
 * Extend this class and annotate with @Service for Spring integration.
 */
public class TectonicService {

    // Cache keyed by full PlanetConfig (record has proper equals/hashCode)
    private final Map<PlanetConfig, GeneratedPlanet> cache = new ConcurrentHashMap<>();

    public GeneratedPlanet generateFromAccrete(long seed, double radiusKm, double massEarths, double waterFraction) {
        PlanetConfig config = PlanetConfig.builder()
            .seed(seed)
            .fromAccreteRadius(radiusKm)
            .plateCount(calculatePlateCount(massEarths))
            .waterFraction(waterFraction)
            .build();

        return generate(config);
    }

    public GeneratedPlanet generate(PlanetConfig config) {
        return cache.computeIfAbsent(config, PlanetGenerator::generate);
    }

    public GeneratedPlanet regenerate(PlanetConfig config) {
        GeneratedPlanet planet = PlanetGenerator.generate(config);
        cache.put(config, planet);
        return planet;
    }

    public GeneratedPlanet getCached(PlanetConfig config) {
        return cache.get(config);
    }

    public void evict(PlanetConfig config) {
        cache.remove(config);
    }

    public void clearCache() {
        cache.clear();
    }

    private int calculatePlateCount(double massEarths) {
        if (massEarths < 0.5) return 7;
        if (massEarths < 1.0) return 10;
        if (massEarths < 2.0) return 14;
        if (massEarths < 5.0) return 18;
        return 21;
    }

    public record AccreteInput(
        long seed,
        double radiusKm,
        double massEarths,
        double waterFraction,
        double surfaceTempK,
        double surfacePressureAtm
    ) {
        public GeneratedPlanet generate(TectonicService service) {
            return service.generateFromAccrete(seed, radiusKm, massEarths, waterFraction);
        }
    }

    public record PlanetSummary(
        long seed,
        int polyCount,
        int plateCount,
        double waterFraction,
        double landFraction,
        int[] heightDistribution
    ) {
        public static PlanetSummary from(GeneratedPlanet planet) {
            int[] heights = planet.heights();
            int[] distribution = new int[9];

            int waterCount = 0;
            for (int h : heights) {
                distribution[h + 4]++;
                if (h < 0) waterCount++;
            }

            double water = (double) waterCount / heights.length;

            return new PlanetSummary(
                planet.config().seed(),
                heights.length,  // Use actual count, not config preset
                planet.config().plateCount(),
                water,
                1.0 - water,
                distribution
            );
        }
    }
}
