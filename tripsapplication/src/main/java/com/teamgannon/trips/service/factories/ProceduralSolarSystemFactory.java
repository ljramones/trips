package com.teamgannon.trips.service.factories;

import com.teamgannon.trips.jpa.model.ExoPlanet;
import com.teamgannon.trips.jpa.model.SolarSystem;
import com.teamgannon.trips.jpa.model.SolarSystemFeature;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.jpa.repository.ExoPlanetRepository;
import com.teamgannon.trips.jpa.repository.SolarSystemFeatureRepository;
import com.teamgannon.trips.service.GeneratedSystemPersister;
import com.teamgannon.trips.service.SolarSystemService;
import com.teamgannon.trips.solarsystem.modelling.accrete.StarSystem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Catch-all {@link SolarSystemFactory} that builds a system for any star
 * via the ACCRETE procedural simulation. Wraps the existing
 * {@code new StarSystem(star, ...).getPlanets()} + {@link
 * GeneratedSystemPersister#saveSystem} flow so it can participate in the
 * unified factory lifecycle introduced in Issue 18.
 * <p>
 * Ordered last ({@code @Order(Ordered.LOWEST_PRECEDENCE)}) so any
 * star-specific factory (Sol, and any future curated systems) wins before
 * the procedural fallback is consulted.
 * <p>
 * Idempotency: the existing
 * {@link GeneratedSystemPersister#savePlanets} call already deletes prior
 * {@code "Simulated"} rows for the system before re-saving, so re-running
 * the factory regenerates with a fresh seed. This class additionally
 * checks for {@code "Direct"}-typed planets (hand-curated or imported) and
 * skips the procedural run when those are present — we don't want the
 * procedural pipeline trampling Sol if someone passes the Sol star to it
 * directly.
 */
@Slf4j
@Component
@Order(org.springframework.core.Ordered.LOWEST_PRECEDENCE)
public class ProceduralSolarSystemFactory implements SolarSystemFactory {

    public static final String FACTORY_NAME = "procedural-accrete";

    private final SolarSystemService solarSystemService;
    private final GeneratedSystemPersister generatedSystemPersister;
    private final ExoPlanetRepository exoPlanetRepository;
    private final SolarSystemFeatureRepository featureRepository;

    public ProceduralSolarSystemFactory(SolarSystemService solarSystemService,
                                        GeneratedSystemPersister generatedSystemPersister,
                                        ExoPlanetRepository exoPlanetRepository,
                                        SolarSystemFeatureRepository featureRepository) {
        this.solarSystemService = solarSystemService;
        this.generatedSystemPersister = generatedSystemPersister;
        this.exoPlanetRepository = exoPlanetRepository;
        this.featureRepository = featureRepository;
    }

    @Override
    public String name() {
        return FACTORY_NAME;
    }

    /** Fallback factory — claims every non-null star. */
    @Override
    public boolean appliesTo(StarObject star) {
        return star != null;
    }

    @Override
    @Transactional
    public SolarSystemFactoryResult generate(StarObject star) {
        if (star == null) {
            return SolarSystemFactoryResult.alreadyExisted(FACTORY_NAME, null);
        }

        SolarSystem solarSystem = solarSystemService.findOrCreateSolarSystem(star);
        if (hasHandCuratedPlanets(solarSystem)) {
            log.info("Star '{}' already has hand-curated (Direct) planets; refusing to overwrite with procedural simulation",
                    star.getDisplayName());
            return SolarSystemFactoryResult.alreadyExisted(FACTORY_NAME, star);
        }

        // The StarSystem constructor runs the full simulation (distribution +
        // accretion + checks + migration + environment + post-accretion).
        StarSystem starSystem = new StarSystem(star, true, false, false);
        int bodies = generatedSystemPersister.saveSystem(
                star, starSystem, solarSystemService::findOrCreateSolarSystem);

        int features = countFeatures(solarSystem.getId());
        int planetsAndMoons = bodies;
        int planets = countPlanets(solarSystem.getId());
        int moons = planetsAndMoons - planets;
        if (moons < 0) {
            // Defensive: planet count > body count shouldn't happen, but if it
            // does, clamp moons to zero rather than reporting nonsense.
            moons = 0;
        }

        log.info("Procedural factory generated system for '{}': {} planet(s), {} moon(s), {} feature(s)",
                star.getDisplayName(), planets, moons, features);
        return new SolarSystemFactoryResult(FACTORY_NAME, star, planets, moons, features, false);
    }

    private boolean hasHandCuratedPlanets(SolarSystem solarSystem) {
        List<ExoPlanet> planets = exoPlanetRepository.findBySolarSystemId(solarSystem.getId());
        if (planets == null) return false;
        return planets.stream()
                .anyMatch(p -> "Direct".equalsIgnoreCase(p.getDetectionType()));
    }

    private int countPlanets(String solarSystemId) {
        return (int) exoPlanetRepository.findBySolarSystemId(solarSystemId).stream()
                .filter(p -> !Boolean.TRUE.equals(p.getIsMoon()))
                .count();
    }

    private int countFeatures(String solarSystemId) {
        List<SolarSystemFeature> features = featureRepository.findBySolarSystemId(solarSystemId);
        return features == null ? 0 : features.size();
    }
}
