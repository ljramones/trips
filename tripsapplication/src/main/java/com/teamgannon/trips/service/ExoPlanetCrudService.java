package com.teamgannon.trips.service;

import com.teamgannon.trips.jpa.model.ExoPlanet;
import com.teamgannon.trips.jpa.model.SolarSystem;
import com.teamgannon.trips.jpa.repository.ExoPlanetRepository;
import com.teamgannon.trips.jpa.repository.SolarSystemRepository;
import com.teamgannon.trips.planetarymodelling.procedural.PlanetConfig;
import com.teamgannon.trips.planetarymodelling.procedural.PlanetGenerator;
import com.teamgannon.trips.planetarymodelling.procedural.ProceduralPlanetPersistenceHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for ExoPlanet CRUD operations.
 * Handles creation, retrieval, update, and deletion of exoplanet entities,
 * including maintaining consistency with parent solar system metadata.
 */
@Slf4j
@Service
public class ExoPlanetCrudService {

    private final ExoPlanetRepository exoPlanetRepository;
    private final SolarSystemRepository solarSystemRepository;

    public ExoPlanetCrudService(ExoPlanetRepository exoPlanetRepository,
                                SolarSystemRepository solarSystemRepository) {
        this.exoPlanetRepository = exoPlanetRepository;
        this.solarSystemRepository = solarSystemRepository;
    }

    /**
     * Find an ExoPlanet by its name.
     *
     * @param name the planet name
     * @return the ExoPlanet entity or null if not found
     */
    public ExoPlanet findByName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        return exoPlanetRepository.findByName(name);
    }

    /**
     * Find an ExoPlanet by its ID.
     *
     * @param id the planet ID
     * @return the ExoPlanet entity or null if not found
     */
    public ExoPlanet findById(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        return exoPlanetRepository.findById(id).orElse(null);
    }

    /**
     * Find all ExoPlanets in a solar system.
     *
     * @param solarSystemId the solar system ID
     * @return list of ExoPlanet entities
     */
    public List<ExoPlanet> findBySolarSystemId(String solarSystemId) {
        return exoPlanetRepository.findBySolarSystemId(solarSystemId);
    }

    /**
     * Find all planets (not moons) in a solar system.
     *
     * @param solarSystemId the solar system ID
     * @return list of ExoPlanet entities that are planets
     */
    public List<ExoPlanet> findPlanetsBySolarSystemId(String solarSystemId) {
        return exoPlanetRepository.findPlanetsBySolarSystemId(solarSystemId);
    }

    /**
     * Find all moons of a parent planet.
     *
     * @param parentPlanetId the parent planet ID
     * @return list of ExoPlanet entities that are moons
     */
    public List<ExoPlanet> findMoonsByParentPlanetId(String parentPlanetId) {
        return exoPlanetRepository.findByParentPlanetId(parentPlanetId);
    }

    /**
     * Find exoplanets by star name match.
     *
     * @param starName the star name
     * @return list of matching ExoPlanet entities
     */
    public List<ExoPlanet> findByStarName(String starName) {
        return exoPlanetRepository.findByStarName(starName);
    }

    /**
     * Find exoplanets near given RA/Dec coordinates.
     *
     * @param ra  the right ascension
     * @param dec the declination
     * @return list of nearby ExoPlanet entities
     */
    public List<ExoPlanet> findByRaDecNear(double ra, double dec) {
        return exoPlanetRepository.findByRaDecNear(ra, dec);
    }

    /**
     * Add a new ExoPlanet (or moon) to the database.
     *
     * @param planet the planet to add
     * @return the saved planet
     */
    @Transactional
    public ExoPlanet add(ExoPlanet planet) {
        if (planet == null) {
            log.warn("Cannot add null planet");
            return null;
        }

        // Ensure ID is set
        if (planet.getId() == null || planet.getId().isBlank()) {
            planet.setId(UUID.randomUUID().toString());
        }

        ExoPlanet saved = exoPlanetRepository.save(planet);
        log.info("Added new {}: {} (id={}, SMA={} AU)",
                Boolean.TRUE.equals(planet.getIsMoon()) ? "moon" : "planet",
                saved.getName(),
                saved.getId(),
                saved.getSemiMajorAxis());

        // Update solar system planet count
        updateSolarSystemAfterAdd(planet);

        return saved;
    }

    /**
     * Update an existing ExoPlanet.
     *
     * @param planet the planet with updated values
     * @return the saved planet entity
     */
    @Transactional
    public ExoPlanet update(ExoPlanet planet) {
        if (planet == null || planet.getId() == null) {
            log.warn("Cannot update null planet or planet without ID");
            return null;
        }

        ExoPlanet saved = exoPlanetRepository.save(planet);
        log.info("Updated ExoPlanet: {} (id={})", saved.getName(), saved.getId());

        // Update solar system's habitable zone planet status if needed
        if (planet.getSolarSystemId() != null && planet.getSemiMajorAxis() != null) {
            solarSystemRepository.findById(planet.getSolarSystemId()).ifPresent(this::updateHabitableZonePlanetStatus);
        }

        return saved;
    }

    /**
     * Delete an ExoPlanet by ID.
     *
     * @param planetId the planet ID to delete
     */
    @Transactional
    public void delete(String planetId) {
        if (planetId == null || planetId.isBlank()) {
            log.warn("Cannot delete planet with null/empty ID");
            return;
        }

        Optional<ExoPlanet> planet = exoPlanetRepository.findById(planetId);
        if (planet.isEmpty()) {
            log.warn("Planet not found for deletion: {}", planetId);
            return;
        }

        String solarSystemId = planet.get().getSolarSystemId();
        String planetName = planet.get().getName();

        exoPlanetRepository.deleteById(planetId);
        log.info("Deleted ExoPlanet: {} (id={})", planetName, planetId);

        // Update solar system planet count
        if (solarSystemId != null) {
            solarSystemRepository.findById(solarSystemId).ifPresent(ss -> {
                long count = exoPlanetRepository.countBySolarSystemId(solarSystemId);
                ss.setPlanetCount((int) count);
                updateHabitableZonePlanetStatus(ss);
                solarSystemRepository.save(ss);
            });
        }
    }

    /**
     * Regenerate procedural terrain using stored snapshot/overrides and persist a new preview.
     *
     * @param exoPlanet the planet to regenerate
     * @return Generated planet or null if regeneration fails
     */
    @Transactional
    public PlanetGenerator.GeneratedPlanet regenerateProceduralPlanet(ExoPlanet exoPlanet) {
        if (exoPlanet == null) {
            log.warn("Cannot regenerate procedural planet for null ExoPlanet");
            return null;
        }

        PlanetConfig config = ProceduralPlanetPersistenceHelper.buildConfigFromSnapshots(exoPlanet);
        if (config == null) {
            log.warn("Could not build procedural config for {}", exoPlanet.getName());
            return null;
        }

        PlanetGenerator.GeneratedPlanet generated = PlanetGenerator.generate(config);

        ProceduralPlanetPersistenceHelper.populateProceduralMetadata(
                exoPlanet, config, config.seed(), generated, "REGENERATED");
        update(exoPlanet);

        return generated;
    }

    /**
     * Count exoplanets in a solar system.
     *
     * @param solarSystemId the solar system ID
     * @return the count
     */
    public long countBySolarSystemId(String solarSystemId) {
        return exoPlanetRepository.countBySolarSystemId(solarSystemId);
    }

    /**
     * Save an exoplanet (internal use).
     */
    @Transactional
    public ExoPlanet save(ExoPlanet planet) {
        return exoPlanetRepository.save(planet);
    }

    /**
     * Delete an exoplanet entity (internal use).
     */
    @Transactional
    public void deleteEntity(ExoPlanet planet) {
        exoPlanetRepository.delete(planet);
    }

    // ==================== Helper Methods ====================

    /**
     * Update solar system metadata after adding a planet.
     */
    private void updateSolarSystemAfterAdd(ExoPlanet planet) {
        String solarSystemId = planet.getSolarSystemId();
        if (solarSystemId != null) {
            solarSystemRepository.findById(solarSystemId).ifPresent(ss -> {
                // Count only planets, not moons
                long planetCount = exoPlanetRepository.findPlanetsBySolarSystemId(solarSystemId).size();
                ss.setPlanetCount((int) planetCount);
                updateHabitableZonePlanetStatus(ss);
                solarSystemRepository.save(ss);
            });
        }
    }

    /**
     * Update the hasHabitableZonePlanets flag for a solar system.
     */
    void updateHabitableZonePlanetStatus(SolarSystem solarSystem) {
        if (solarSystem.getHabitableZoneInnerAU() == null || solarSystem.getHabitableZoneOuterAU() == null) {
            return;
        }

        List<ExoPlanet> planets = exoPlanetRepository.findBySolarSystemId(solarSystem.getId());
        boolean hasHZPlanets = planets.stream()
                .filter(p -> p.getSemiMajorAxis() != null)
                .anyMatch(p -> p.getSemiMajorAxis() >= solarSystem.getHabitableZoneInnerAU()
                        && p.getSemiMajorAxis() <= solarSystem.getHabitableZoneOuterAU());

        solarSystem.setHasHabitableZonePlanets(hasHZPlanets);
        solarSystemRepository.save(solarSystem);
    }
}
