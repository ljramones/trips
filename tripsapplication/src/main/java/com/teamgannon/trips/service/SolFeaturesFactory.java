package com.teamgannon.trips.service;

import com.teamgannon.trips.jpa.model.SolarSystem;
import com.teamgannon.trips.jpa.model.SolarSystemFeature;
import com.teamgannon.trips.jpa.repository.SolarSystemFeatureRepository;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Creates the named system-level features of Sol: the Main Asteroid Belt
 * (2.1-3.3 AU) and the Kuiper Belt (30-50 AU).
 * <p>
 * Extracted from {@code SolPlanetsInitializer} in Phase 4.6 of the
 * codebase-review remediation. Static helper; the caller passes in the
 * SolarSystem entity and the repository to persist into.
 *
 * <h2>Idempotency</h2>
 * If the system already has any persisted feature rows, this is a no-op —
 * matches the prior in-line behaviour and avoids double-creating belts on
 * repeat startups.
 */
@Slf4j
public final class SolFeaturesFactory {

    private SolFeaturesFactory() {
    }

    /**
     * Create the canonical Sol-system features (Main Asteroid Belt, Kuiper Belt)
     * if none exist yet. Persists each feature via {@code featureRepository}.
     */
    public static void createAll(SolarSystem solarSystem, SolarSystemFeatureRepository featureRepository) {
        log.info("Creating Sol's asteroid belts...");

        List<SolarSystemFeature> existing = featureRepository.findBySolarSystemId(solarSystem.getId());
        if (existing != null && !existing.isEmpty()) {
            log.info("Sol system already has {} features", existing.size());
            return;
        }

        featureRepository.save(buildMainAsteroidBelt(solarSystem.getId()));
        log.info("Created Main Asteroid Belt: 2.1-3.3 AU");

        featureRepository.save(buildKuiperBelt(solarSystem.getId()));
        log.info("Created Kuiper Belt: 30-50 AU");
    }

    /** Main Asteroid Belt between Mars and Jupiter (2.1-3.3 AU). */
    private static SolarSystemFeature buildMainAsteroidBelt(String solarSystemId) {
        SolarSystemFeature belt = new SolarSystemFeature(
                "Main Asteroid Belt",
                SolarSystemFeature.FeatureType.ASTEROID_BELT,
                SolarSystemFeature.FeatureCategory.NATURAL);
        belt.setSolarSystemId(solarSystemId);
        belt.setInnerRadiusAU(2.1);
        belt.setOuterRadiusAU(3.3);
        belt.setThickness(0.15);            // thick vertical distribution
        belt.setInclinationDeg(10.0);       // typical asteroid inclination
        belt.setEccentricity(0.08);
        belt.setParticleCount(5000);
        belt.setMinParticleSize(0.5);
        belt.setMaxParticleSize(2.0);
        belt.setPrimaryColor("#8C8278");    // rocky gray
        belt.setSecondaryColor("#645A50");  // brown-gray
        belt.setOpacity(0.8);
        belt.setAnimated(true);
        belt.setAnimationSpeed(1.0);
        belt.setNavigationHazard(false);    // not really hazardous despite sci-fi depictions
        belt.setNotes("The asteroid belt between Mars and Jupiter, containing millions of rocky bodies. "
                + "Despite popular depictions, asteroids are widely spaced (average ~1 million km apart).");
        return belt;
    }

    /** Kuiper Belt beyond Neptune (30-50 AU). */
    private static SolarSystemFeature buildKuiperBelt(String solarSystemId) {
        SolarSystemFeature belt = new SolarSystemFeature(
                "Kuiper Belt",
                SolarSystemFeature.FeatureType.KUIPER_BELT,
                SolarSystemFeature.FeatureCategory.NATURAL);
        belt.setSolarSystemId(solarSystemId);
        belt.setInnerRadiusAU(30.0);
        belt.setOuterRadiusAU(50.0);
        belt.setThickness(0.2);             // thicker than asteroid belt
        belt.setInclinationDeg(15.0);
        belt.setEccentricity(0.1);
        belt.setParticleCount(3000);
        belt.setMinParticleSize(0.8);
        belt.setMaxParticleSize(3.0);
        belt.setPrimaryColor("#B4BEC8");    // icy blue-gray
        belt.setSecondaryColor("#8C8C96");
        belt.setOpacity(0.6);
        belt.setAnimated(true);
        belt.setAnimationSpeed(0.5);        // slower due to greater distance
        belt.setNavigationHazard(false);
        belt.setNotes("A region of icy bodies beyond Neptune, including dwarf planets like Pluto, Eris, and Makemake. "
                + "Contains an estimated 100,000+ objects larger than 100 km.");
        return belt;
    }
}
