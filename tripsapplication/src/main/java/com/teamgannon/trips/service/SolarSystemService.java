package com.teamgannon.trips.service;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.ExoPlanet;
import com.teamgannon.trips.jpa.model.SolarSystem;
import com.teamgannon.trips.jpa.model.SolarSystemFeature;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.jpa.repository.ExoPlanetRepository;
import com.teamgannon.trips.jpa.repository.SolarSystemFeatureRepository;
import com.teamgannon.trips.jpa.repository.SolarSystemRepository;
import com.teamgannon.trips.jpa.repository.StarObjectRepository;
import com.teamgannon.trips.planetarymodelling.FeatureDescription;
import com.teamgannon.trips.planetarymodelling.PlanetDescription;
import com.teamgannon.trips.planetarymodelling.SolarSystemDescription;
import com.teamgannon.trips.planetarymodelling.procedural.PlanetGenerator;
import com.teamgannon.trips.solarsysmodelling.accrete.Planet;
import com.teamgannon.trips.solarsysmodelling.habitable.HabitableZoneCalculator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing solar systems and their contents.
 * Handles the relationship between stars, planets, and other celestial bodies.
 */
@Slf4j
@Service
public class SolarSystemService {

    private final SolarSystemRepository solarSystemRepository;
    private final ExoPlanetRepository exoPlanetRepository;
    private final StarObjectRepository starObjectRepository;
    private final SolarSystemFeatureRepository featureRepository;
    private final ExoPlanetCrudService exoPlanetCrudService;

    public SolarSystemService(SolarSystemRepository solarSystemRepository,
                              ExoPlanetRepository exoPlanetRepository,
                              StarObjectRepository starObjectRepository,
                              SolarSystemFeatureRepository featureRepository,
                              ExoPlanetCrudService exoPlanetCrudService) {
        this.solarSystemRepository = solarSystemRepository;
        this.exoPlanetRepository = exoPlanetRepository;
        this.starObjectRepository = starObjectRepository;
        this.featureRepository = featureRepository;
        this.exoPlanetCrudService = exoPlanetCrudService;
    }

    // ==================== Solar System Retrieval ====================

    /**
     * Get the solar system description for a given star.
     * This is the main entry point when a user selects "Jump into..." on a star.
     *
     * @param starDisplayRecord the star to view
     * @return the solar system description for rendering
     */
    public SolarSystemDescription getSolarSystem(StarDisplayRecord starDisplayRecord) {
        SolarSystemDescription description = new SolarSystemDescription();
        description.setStarDisplayRecord(starDisplayRecord);

        String starId = starDisplayRecord.getRecordId();
        Optional<SolarSystem> existingSystem = findSolarSystemForStar(starId);

        if (existingSystem.isPresent()) {
            populateFromExistingSystem(description, existingSystem.get(), starId);
        } else {
            populateFromStarMatch(description, starDisplayRecord);
        }

        return description;
    }

    private void populateFromExistingSystem(SolarSystemDescription description,
                                            SolarSystem solarSystem,
                                            String starId) {
        description.setSolarSystem(solarSystem);

        // Load planets from the solar system
        List<ExoPlanet> exoPlanets = exoPlanetRepository.findBySolarSystemId(solarSystem.getId());
        description.setPlanetDescriptionList(PlanetDescriptionConverter.convert(exoPlanets));

        // Load features (asteroid belts, stations, gates, etc.)
        List<SolarSystemFeature> features = featureRepository.findBySolarSystemId(solarSystem.getId());
        description.setFeatures(FeatureDescriptionConverter.convert(features));

        // Set habitable zone from stored values
        if (solarSystem.getHabitableZoneInnerAU() != null) {
            description.setHabitableZoneInnerAU(solarSystem.getHabitableZoneInnerAU());
        }
        if (solarSystem.getHabitableZoneOuterAU() != null) {
            description.setHabitableZoneOuterAU(solarSystem.getHabitableZoneOuterAU());
        }

        // Load companion stars for multi-star systems
        if (solarSystem.getStarCount() > 1) {
            loadCompanionStars(description, solarSystem, starId);
        }

        log.info("Loaded existing solar system '{}' with {} planets and {} features",
                solarSystem.getSystemName(), exoPlanets.size(), features.size());
    }

    private void populateFromStarMatch(SolarSystemDescription description,
                                       StarDisplayRecord starDisplayRecord) {
        List<ExoPlanet> exoPlanets = findExoplanetsByStarMatch(starDisplayRecord);
        description.setPlanetDescriptionList(PlanetDescriptionConverter.convert(exoPlanets));

        // Calculate habitable zone from star properties
        calculateHabitableZone(description, starDisplayRecord);

        log.info("No solar system entity for '{}', found {} planets by name match",
                starDisplayRecord.getStarName(), exoPlanets.size());
    }

    /**
     * Find the SolarSystem entity for a given star ID.
     */
    private Optional<SolarSystem> findSolarSystemForStar(String starId) {
        // First check if this star is the primary star of a system
        Optional<SolarSystem> byPrimary = solarSystemRepository.findByPrimaryStarId(starId);
        if (byPrimary.isPresent()) {
            return byPrimary;
        }

        // Check if this star has a solarSystemId set
        Optional<StarObject> star = starObjectRepository.findById(starId);
        if (star.isPresent() && star.get().getSolarSystemId() != null) {
            return solarSystemRepository.findById(star.get().getSolarSystemId());
        }

        return Optional.empty();
    }

    /**
     * Find exoplanets that match a star by name.
     */
    private List<ExoPlanet> findExoplanetsByStarMatch(StarDisplayRecord star) {
        List<ExoPlanet> results = new ArrayList<>();

        // Try matching by display name
        String displayName = star.getStarName();
        if (displayName != null && !displayName.isBlank()) {
            results.addAll(exoPlanetRepository.findByStarName(displayName));
        }

        // If no results, try to look up the full StarObject for more matching options
        if (results.isEmpty() && star.getRecordId() != null) {
            Optional<StarObject> starObject = starObjectRepository.findById(star.getRecordId());
            if (starObject.isPresent()) {
                StarObject so = starObject.get();

                // Try matching by common name
                String commonName = so.getCommonName();
                if (commonName != null && !commonName.isBlank() && !commonName.equals(displayName)) {
                    results.addAll(exoPlanetRepository.findByStarName(commonName));
                }

                // Try matching by RA/Dec proximity
                if (results.isEmpty() && so.getRa() != 0 && so.getDeclination() != 0) {
                    results.addAll(exoPlanetRepository.findByRaDecNear(so.getRa(), so.getDeclination()));
                }
            }
        }

        return results;
    }

    private void loadCompanionStars(SolarSystemDescription description,
                                    SolarSystem solarSystem,
                                    String primaryStarId) {
        List<StarObject> allStars = starObjectRepository.findBySolarSystemId(solarSystem.getId());
        for (StarObject star : allStars) {
            if (!star.getId().equals(primaryStarId)) {
                description.getCompanionStars().add(StarDisplayRecord.fromStarObject(star));
            }
        }
    }

    private void calculateHabitableZone(SolarSystemDescription description,
                                        StarDisplayRecord star) {
        double luminosity = star.getLuminosity();
        if (luminosity > 0) {
            double[] hz = HabitableZoneCalculator.calculate(luminosity);
            description.setHabitableZoneInnerAU(hz[0]);
            description.setHabitableZoneOuterAU(hz[1]);
        } else {
            // Default to Sun-like values if luminosity unknown
            description.setHabitableZoneInnerAU(0.95);
            description.setHabitableZoneOuterAU(1.67);
        }
    }

    // ==================== Solar System CRUD ====================

    /**
     * Create a new solar system for a star.
     */
    @Transactional
    public SolarSystem createSolarSystem(StarObject primaryStar) {
        if (solarSystemRepository.existsByPrimaryStarId(primaryStar.getId())) {
            log.warn("Solar system already exists for star {}", primaryStar.getDisplayName());
            return solarSystemRepository.findByPrimaryStarId(primaryStar.getId()).orElse(null);
        }

        SolarSystem solarSystem = SolarSystem.fromStar(primaryStar);

        // Calculate habitable zone from luminosity
        double luminosity = parseLuminosity(primaryStar.getLuminosity());
        if (luminosity > 0) {
            double[] hz = HabitableZoneCalculator.calculate(luminosity);
            solarSystem.setHabitableZoneInnerAU(hz[0]);
            solarSystem.setHabitableZoneOuterAU(hz[1]);
        }

        // Save and update the star's reference
        solarSystem = solarSystemRepository.save(solarSystem);
        primaryStar.setSolarSystemId(solarSystem.getId());
        starObjectRepository.save(primaryStar);

        log.info("Created solar system '{}' for star '{}'",
                solarSystem.getSystemName(), primaryStar.getDisplayName());

        return solarSystem;
    }

    /**
     * Create a solar system from a StarDisplayRecord.
     */
    @Transactional
    public SolarSystem createSolarSystem(StarDisplayRecord starRecord) {
        Optional<StarObject> star = starObjectRepository.findById(starRecord.getRecordId());
        if (star.isEmpty()) {
            log.error("Cannot create solar system - star not found: {}", starRecord.getRecordId());
            return null;
        }
        return createSolarSystem(star.get());
    }

    /**
     * Find or create a solar system for a star.
     */
    @Transactional
    public SolarSystem findOrCreateSolarSystem(StarObject star) {
        Optional<SolarSystem> existing = findSolarSystemForStar(star.getId());
        return existing.orElseGet(() -> createSolarSystem(star));
    }

    /**
     * Add a star to an existing solar system (for binary/trinary systems).
     */
    @Transactional
    public void addStarToSystem(SolarSystem solarSystem, StarObject star) {
        star.setSolarSystemId(solarSystem.getId());
        starObjectRepository.save(star);

        solarSystem.setStarCount(solarSystem.getStarCount() + 1);
        solarSystemRepository.save(solarSystem);

        log.info("Added star '{}' to solar system '{}'",
                star.getDisplayName(), solarSystem.getSystemName());
    }

    /**
     * Link an exoplanet to a solar system and host star.
     */
    @Transactional
    public void linkPlanetToSystem(ExoPlanet planet, SolarSystem solarSystem, StarObject hostStar) {
        planet.setSolarSystemId(solarSystem.getId());
        planet.setHostStarId(hostStar.getId());
        exoPlanetRepository.save(planet);

        // Update planet count
        long count = exoPlanetRepository.countBySolarSystemId(solarSystem.getId());
        solarSystem.setPlanetCount((int) count);

        // Check if planet is in habitable zone
        if (planet.getSemiMajorAxis() != null && solarSystem.getHabitableZoneInnerAU() != null) {
            double sma = planet.getSemiMajorAxis();
            if (sma >= solarSystem.getHabitableZoneInnerAU() &&
                    sma <= solarSystem.getHabitableZoneOuterAU()) {
                solarSystem.setHasHabitableZonePlanets(true);
            }
        }

        solarSystemRepository.save(solarSystem);

        log.info("Linked planet '{}' to solar system '{}' orbiting '{}'",
                planet.getName(), solarSystem.getSystemName(), hostStar.getDisplayName());
    }

    // ==================== Query Methods ====================

    public Optional<SolarSystem> findById(String id) {
        return solarSystemRepository.findById(id);
    }

    public Optional<SolarSystem> findByName(String name) {
        return solarSystemRepository.findBySystemName(name);
    }

    public List<SolarSystem> findByDataSet(String dataSetName) {
        return solarSystemRepository.findByDataSetName(dataSetName);
    }

    public List<SolarSystem> findSystemsWithPlanets() {
        return solarSystemRepository.findSystemsWithPlanets();
    }

    public List<SolarSystem> findHabitableSystems() {
        return solarSystemRepository.findByHasHabitableZonePlanetsTrue();
    }

    @Transactional
    public SolarSystem save(SolarSystem solarSystem) {
        return solarSystemRepository.save(solarSystem);
    }

    @Transactional
    public void delete(SolarSystem solarSystem) {
        // Clear references from stars
        List<StarObject> stars = starObjectRepository.findBySolarSystemId(solarSystem.getId());
        for (StarObject star : stars) {
            star.setSolarSystemId(null);
            starObjectRepository.save(star);
        }

        // Clear references from planets
        List<ExoPlanet> planets = exoPlanetRepository.findBySolarSystemId(solarSystem.getId());
        for (ExoPlanet planet : planets) {
            planet.setSolarSystemId(null);
            planet.setHostStarId(null);
            exoPlanetRepository.save(planet);
        }

        solarSystemRepository.delete(solarSystem);
        log.info("Deleted solar system '{}'", solarSystem.getSystemName());
    }

    // ==================== ExoPlanet Delegation ====================

    public ExoPlanet findExoPlanetByName(String name) {
        return exoPlanetCrudService.findByName(name);
    }

    public ExoPlanet findExoPlanetById(String id) {
        return exoPlanetCrudService.findById(id);
    }

    @Transactional
    public ExoPlanet updateExoPlanet(ExoPlanet planet) {
        return exoPlanetCrudService.update(planet);
    }

    @Transactional
    public PlanetGenerator.GeneratedPlanet regenerateProceduralPlanet(ExoPlanet exoPlanet) {
        return exoPlanetCrudService.regenerateProceduralPlanet(exoPlanet);
    }

    @Transactional
    public void deleteExoPlanet(String planetId) {
        exoPlanetCrudService.delete(planetId);
    }

    @Transactional
    public ExoPlanet addExoPlanet(ExoPlanet planet) {
        return exoPlanetCrudService.add(planet);
    }

    // ==================== Galilean Moon Helper ====================

    /**
     * Create the four Galilean moons of Jupiter if they don't exist.
     *
     * @param jupiterPlanet the Jupiter ExoPlanet entity
     * @return list of created moons (empty if they already exist)
     */
    @Transactional
    public List<ExoPlanet> createGalileanMoonsIfMissing(ExoPlanet jupiterPlanet) {
        if (jupiterPlanet == null) {
            log.warn("Cannot create Galilean moons - Jupiter planet is null");
            return List.of();
        }

        // Get existing moon names
        List<ExoPlanet> existingMoons = exoPlanetRepository.findByParentPlanetId(jupiterPlanet.getId());
        List<String> existingNames = existingMoons.stream()
                .map(ExoPlanet::getName)
                .map(String::toLowerCase)
                .toList();

        // Create moons using factory
        List<ExoPlanet> moons = GalileanMoonFactory.createMoons(jupiterPlanet, existingNames);

        // Save all created moons
        List<ExoPlanet> savedMoons = new ArrayList<>();
        for (ExoPlanet moon : moons) {
            savedMoons.add(exoPlanetRepository.save(moon));
        }

        if (!savedMoons.isEmpty()) {
            log.info("Created {} Galilean moons for Jupiter", savedMoons.size());
        }

        return savedMoons;
    }

    /**
     * Find a planet named "Jupiter" or similar in the given solar system.
     *
     * @param solarSystemId the solar system ID
     * @return the Jupiter planet, or null if not found
     */
    public ExoPlanet findJupiterInSystem(String solarSystemId) {
        List<ExoPlanet> planets = exoPlanetRepository.findBySolarSystemId(solarSystemId);
        for (ExoPlanet planet : planets) {
            String name = planet.getName();
            if (name != null && (name.toLowerCase().contains("jupiter") ||
                    (name.toLowerCase().endsWith(" e") && planet.getMass() != null && planet.getMass() > 100))) {
                return planet;
            }
        }
        return null;
    }

    // ==================== Generated Planet Persistence ====================

    /**
     * Save generated planets from ACCRETE simulation to the database.
     *
     * @param sourceStar the star for which planets were generated
     * @param planets    the list of generated ACCRETE Planet objects
     * @return the number of planets saved
     */
    @Transactional
    public int saveGeneratedPlanets(StarObject sourceStar, List<Planet> planets) {
        if (sourceStar == null || planets == null || planets.isEmpty()) {
            log.warn("Cannot save generated planets: source star or planets list is null/empty");
            return 0;
        }

        SolarSystem solarSystem = findOrCreateSolarSystem(sourceStar);

        // Delete any existing simulated planets for this system
        List<ExoPlanet> existingPlanets = exoPlanetRepository.findBySolarSystemId(solarSystem.getId());
        for (ExoPlanet existing : existingPlanets) {
            if ("Simulated".equals(existing.getDetectionType())) {
                exoPlanetRepository.delete(existing);
            }
        }

        // Convert and save each generated planet and its moons
        int savedCount = 0;
        int moonCount = 0;
        int planetIndex = 1;

        for (Planet planet : planets) {
            ExoPlanet exoPlanet = AccretePlanetConverter.convert(
                    planet, sourceStar, solarSystem.getId(), planetIndex++, null, false);
            exoPlanetRepository.save(exoPlanet);
            savedCount++;

            log.debug("Saved generated planet: {} (SMA={} AU, Mass={} Earth masses)",
                    exoPlanet.getName(), exoPlanet.getSemiMajorAxis(), exoPlanet.getMass());

            // Save moons for this planet
            List<Planet> moons = planet.getMoons();
            if (moons != null && !moons.isEmpty()) {
                int moonIndex = 1;
                for (Planet moon : moons) {
                    ExoPlanet exoMoon = AccretePlanetConverter.convert(
                            moon, sourceStar, solarSystem.getId(), moonIndex++, exoPlanet.getId(), true);
                    exoMoon.setName(exoPlanet.getName() + " " + AccretePlanetConverter.toRomanNumeral(moonIndex - 1));
                    exoPlanetRepository.save(exoMoon);
                    moonCount++;

                    log.debug("Saved generated moon: {} (parent: {}, SMA={} AU)",
                            exoMoon.getName(), exoPlanet.getName(), exoMoon.getSemiMajorAxis());
                }
            }
        }

        // Update solar system metadata
        solarSystem.setPlanetCount(savedCount);
        exoPlanetCrudService.updateHabitableZonePlanetStatus(solarSystem);
        solarSystemRepository.save(solarSystem);

        log.info("Saved {} generated planets and {} moons for star '{}' (system ID: {})",
                savedCount, moonCount, sourceStar.getDisplayName(), solarSystem.getId());

        return savedCount + moonCount;
    }

    // ==================== Feature CRUD ====================

    /**
     * Add a feature to a solar system.
     *
     * @param feature the feature to add
     * @return the saved feature
     */
    @Transactional
    public SolarSystemFeature addFeature(SolarSystemFeature feature) {
        SolarSystemFeature saved = featureRepository.save(feature);
        log.info("Added {} feature '{}' to solar system {}",
                feature.getFeatureType(), feature.getName(), feature.getSolarSystemId());
        return saved;
    }

    /**
     * Update an existing feature.
     *
     * @param feature the feature to update
     * @return the updated feature
     */
    @Transactional
    public SolarSystemFeature updateFeature(SolarSystemFeature feature) {
        return featureRepository.save(feature);
    }

    /**
     * Delete a feature by ID.
     *
     * @param featureId the feature ID
     */
    @Transactional
    public void deleteFeature(String featureId) {
        featureRepository.deleteById(featureId);
        log.info("Deleted feature {}", featureId);
    }

    /**
     * Find a feature by ID.
     *
     * @param featureId the feature ID
     * @return the feature, or null if not found
     */
    public SolarSystemFeature findFeatureById(String featureId) {
        return featureRepository.findById(featureId).orElse(null);
    }

    /**
     * Find all features for a solar system.
     *
     * @param solarSystemId the solar system ID
     * @return list of features
     */
    public List<SolarSystemFeature> findFeaturesBySolarSystem(String solarSystemId) {
        return featureRepository.findBySolarSystemId(solarSystemId);
    }

    /**
     * Find all belt-type features (asteroid belts, debris disks, etc.) for a solar system.
     *
     * @param solarSystemId the solar system ID
     * @return list of belt features
     */
    public List<SolarSystemFeature> findBeltFeatures(String solarSystemId) {
        return featureRepository.findBeltFeatures(solarSystemId);
    }

    /**
     * Find all point-type features (stations, gates, etc.) for a solar system.
     *
     * @param solarSystemId the solar system ID
     * @return list of point features
     */
    public List<SolarSystemFeature> findPointFeatures(String solarSystemId) {
        return featureRepository.findPointFeatures(solarSystemId);
    }

    /**
     * Find all navigation hazards in a solar system.
     *
     * @param solarSystemId the solar system ID
     * @return list of hazardous features
     */
    public List<SolarSystemFeature> findNavigationHazards(String solarSystemId) {
        return featureRepository.findNavigationHazards(solarSystemId);
    }

    /**
     * Find all jump gates in a solar system.
     *
     * @param solarSystemId the solar system ID
     * @return list of jump gates
     */
    public List<SolarSystemFeature> findJumpGates(String solarSystemId) {
        return featureRepository.findJumpGates(solarSystemId);
    }

    /**
     * Create a standard asteroid belt feature.
     *
     * @param solarSystemId the solar system ID
     * @param name          display name
     * @param innerAU       inner radius in AU
     * @param outerAU       outer radius in AU
     * @return the created feature
     */
    @Transactional
    public SolarSystemFeature createAsteroidBelt(String solarSystemId, String name,
                                                  double innerAU, double outerAU) {
        SolarSystemFeature belt = new SolarSystemFeature(name,
                SolarSystemFeature.FeatureType.ASTEROID_BELT,
                SolarSystemFeature.FeatureCategory.NATURAL);
        belt.setSolarSystemId(solarSystemId);
        belt.setInnerRadiusAU(innerAU);
        belt.setOuterRadiusAU(outerAU);
        belt.setThickness(0.1);  // Default moderate thickness
        belt.setParticleCount(5000);
        belt.setPrimaryColor("#8C8278");
        belt.setSecondaryColor("#645A50");
        belt.setAnimated(true);
        belt.setAnimationSpeed(1.0);
        return featureRepository.save(belt);
    }

    /**
     * Create a jump gate feature.
     *
     * @param solarSystemId      the solar system ID
     * @param name               display name
     * @param orbitalRadiusAU    distance from star
     * @param orbitalAngleDeg    position on orbit
     * @param destinations       comma-separated destination system names
     * @param controllingPolity  faction controlling the gate
     * @return the created feature
     */
    @Transactional
    public SolarSystemFeature createJumpGate(String solarSystemId, String name,
                                              double orbitalRadiusAU, double orbitalAngleDeg,
                                              String destinations, String controllingPolity) {
        SolarSystemFeature gate = new SolarSystemFeature(name,
                SolarSystemFeature.FeatureType.JUMP_GATE,
                SolarSystemFeature.FeatureCategory.ARTIFICIAL);
        gate.setSolarSystemId(solarSystemId);
        gate.setOrbitalRadiusAU(orbitalRadiusAU);
        gate.setOrbitalAngleDeg(orbitalAngleDeg);
        gate.setTransitDestinations(destinations);
        gate.setControllingPolity(controllingPolity);
        gate.setStatus(SolarSystemFeature.FeatureStatus.ACTIVE);
        gate.setPrimaryColor("#00AAFF");
        return featureRepository.save(gate);
    }

    /**
     * Create an orbital habitat feature.
     *
     * @param solarSystemId      the solar system ID
     * @param name               display name
     * @param orbitalRadiusAU    distance from star
     * @param population         habitat population
     * @param controllingPolity  faction controlling the habitat
     * @return the created feature
     */
    @Transactional
    public SolarSystemFeature createOrbitalHabitat(String solarSystemId, String name,
                                                    double orbitalRadiusAU, long population,
                                                    String controllingPolity) {
        SolarSystemFeature habitat = new SolarSystemFeature(name,
                SolarSystemFeature.FeatureType.ORBITAL_HABITAT,
                SolarSystemFeature.FeatureCategory.ARTIFICIAL);
        habitat.setSolarSystemId(solarSystemId);
        habitat.setOrbitalRadiusAU(orbitalRadiusAU);
        habitat.setOrbitalAngleDeg(Math.random() * 360);  // Random position
        habitat.setPopulation(population);
        habitat.setControllingPolity(controllingPolity);
        habitat.setStatus(SolarSystemFeature.FeatureStatus.ACTIVE);
        habitat.setPrimaryColor("#AAFFAA");
        return featureRepository.save(habitat);
    }

    // ==================== Helper Methods ====================

    private double parseLuminosity(String luminosityStr) {
        if (luminosityStr == null || luminosityStr.isBlank()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(luminosityStr.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
