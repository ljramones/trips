package com.teamgannon.trips.service;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.ExoPlanet;
import com.teamgannon.trips.jpa.model.SolarSystem;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.jpa.repository.ExoPlanetRepository;
import com.teamgannon.trips.jpa.repository.SolarSystemRepository;
import com.teamgannon.trips.jpa.repository.StarObjectRepository;
import com.teamgannon.trips.planetarymodelling.PlanetDescription;
import com.teamgannon.trips.planetarymodelling.SolarSystemDescription;
import com.teamgannon.trips.solarsysmodelling.accrete.Planet;
import com.teamgannon.trips.solarsysmodelling.habitable.HabitableZoneCalculator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    public SolarSystemService(SolarSystemRepository solarSystemRepository,
                              ExoPlanetRepository exoPlanetRepository,
                              StarObjectRepository starObjectRepository) {
        this.solarSystemRepository = solarSystemRepository;
        this.exoPlanetRepository = exoPlanetRepository;
        this.starObjectRepository = starObjectRepository;
    }

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

        // Try to find existing SolarSystem entity
        String starId = starDisplayRecord.getRecordId();
        Optional<SolarSystem> existingSystem = findSolarSystemForStar(starId);

        if (existingSystem.isPresent()) {
            SolarSystem solarSystem = existingSystem.get();
            description.setSolarSystem(solarSystem);

            // Load planets from the solar system
            List<ExoPlanet> exoPlanets = exoPlanetRepository.findBySolarSystemId(solarSystem.getId());
            description.setPlanetDescriptionList(convertToPlanetDescriptions(exoPlanets));

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

            log.info("Loaded existing solar system '{}' with {} planets",
                    solarSystem.getSystemName(), exoPlanets.size());
        } else {
            // No SolarSystem entity exists - try to find planets by star name matching
            List<ExoPlanet> exoPlanets = findExoplanetsByStarMatch(starDisplayRecord);
            description.setPlanetDescriptionList(convertToPlanetDescriptions(exoPlanets));

            // Calculate habitable zone from star properties
            calculateHabitableZone(description, starDisplayRecord);

            log.info("No solar system entity for '{}', found {} planets by name match",
                    starDisplayRecord.getStarName(), exoPlanets.size());
        }

        return description;
    }

    /**
     * Find the SolarSystem entity for a given star ID.
     * Checks both primaryStarId and looks for stars with matching solarSystemId.
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
     * Used when no SolarSystem entity exists yet.
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
                    results.addAll(exoPlanetRepository.findByRaAndDecNear(so.getRa(), so.getDeclination()));
                }
            }
        }

        return results;
    }

    private boolean containsPlanet(List<ExoPlanet> list, ExoPlanet planet) {
        return list.stream().anyMatch(p -> p.getId().equals(planet.getId()));
    }

    /**
     * Load companion stars for multi-star systems
     */
    private void loadCompanionStars(SolarSystemDescription description,
                                    SolarSystem solarSystem,
                                    String primaryStarId) {
        // Find all stars in this system except the primary
        List<StarObject> allStars = starObjectRepository.findBySolarSystemId(solarSystem.getId());
        for (StarObject star : allStars) {
            if (!star.getId().equals(primaryStarId)) {
                description.getCompanionStars().add(StarDisplayRecord.fromStarObject(star));
            }
        }
    }

    /**
     * Calculate habitable zone boundaries based on star luminosity
     */
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

    /**
     * Convert ExoPlanet entities to PlanetDescription display objects
     */
    private List<PlanetDescription> convertToPlanetDescriptions(List<ExoPlanet> exoPlanets) {
        List<PlanetDescription> descriptions = new ArrayList<>();
        for (ExoPlanet exoPlanet : exoPlanets) {
            descriptions.add(convertToPlanetDescription(exoPlanet));
        }
        return descriptions;
    }

    /**
     * Convert a single ExoPlanet to PlanetDescription
     */
    private PlanetDescription convertToPlanetDescription(ExoPlanet exoPlanet) {
        PlanetDescription desc = new PlanetDescription();
        desc.setId(exoPlanet.getId());
        desc.setName(exoPlanet.getName());
        desc.setBelongstoStar(exoPlanet.getStarName());

        // Mass (convert from Jupiter masses to Earth masses if needed)
        if (exoPlanet.getMass() != null) {
            desc.setMass(exoPlanet.getMass());
        }

        // Radius (in Earth radii)
        if (exoPlanet.getRadius() != null) {
            desc.setRadius(exoPlanet.getRadius());
        }

        // Orbital parameters
        if (exoPlanet.getSemiMajorAxis() != null) {
            desc.setSemiMajorAxis(exoPlanet.getSemiMajorAxis());
        }
        if (exoPlanet.getEccentricity() != null) {
            desc.setEccentricity(exoPlanet.getEccentricity());
        }
        if (exoPlanet.getInclination() != null) {
            desc.setInclination(exoPlanet.getInclination());
        }
        if (exoPlanet.getOrbitalPeriod() != null) {
            desc.setOrbitalPeriod(exoPlanet.getOrbitalPeriod());
        }
        if (exoPlanet.getOmega() != null) {
            desc.setArgumentOfPeriapsis(exoPlanet.getOmega());
        }
        if (exoPlanet.getTperi() != null) {
            desc.setTimeOfPeriapsisPassage(exoPlanet.getTperi());
        }

        // Temperature
        if (exoPlanet.getTempCalculated() != null) {
            desc.setEquilibriumTemperature(exoPlanet.getTempCalculated());
        } else if (exoPlanet.getTempMeasured() != null) {
            desc.setEquilibriumTemperature(exoPlanet.getTempMeasured());
        }

        // Surface gravity
        if (exoPlanet.getLogG() != null) {
            desc.setSurfaceGravity(exoPlanet.getLogG());
        }

        // Moon properties
        desc.setMoon(Boolean.TRUE.equals(exoPlanet.getIsMoon()));
        desc.setParentPlanetId(exoPlanet.getParentPlanetId());

        return desc;
    }

    // ==================== CRUD Operations ====================

    /**
     * Create a new solar system for a star
     */
    @Transactional
    public SolarSystem createSolarSystem(StarObject primaryStar) {
        // Check if one already exists
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
     * Parse luminosity from String to double
     */
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

    /**
     * Create a solar system from a StarDisplayRecord
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
     * Add a star to an existing solar system (for binary/trinary systems)
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
     * Link an exoplanet to a solar system and host star
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

    /**
     * Find or create a solar system for a star
     */
    @Transactional
    public SolarSystem findOrCreateSolarSystem(StarObject star) {
        Optional<SolarSystem> existing = findSolarSystemForStar(star.getId());
        if (existing.isPresent()) {
            return existing.get();
        }
        return createSolarSystem(star);
    }

    /**
     * Get a solar system by ID
     */
    public Optional<SolarSystem> findById(String id) {
        return solarSystemRepository.findById(id);
    }

    /**
     * Get a solar system by name
     */
    public Optional<SolarSystem> findByName(String name) {
        return solarSystemRepository.findBySystemName(name);
    }

    /**
     * Get all solar systems in a dataset
     */
    public List<SolarSystem> findByDataSet(String dataSetName) {
        return solarSystemRepository.findByDataSetName(dataSetName);
    }

    /**
     * Get all solar systems with planets
     */
    public List<SolarSystem> findSystemsWithPlanets() {
        return solarSystemRepository.findSystemsWithPlanets();
    }

    /**
     * Get all solar systems with habitable zone planets
     */
    public List<SolarSystem> findHabitableSystems() {
        return solarSystemRepository.findByHasHabitableZonePlanetsTrue();
    }

    /**
     * Save a solar system
     */
    @Transactional
    public SolarSystem save(SolarSystem solarSystem) {
        return solarSystemRepository.save(solarSystem);
    }

    /**
     * Delete a solar system
     */
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

    // ==================== ExoPlanet CRUD Operations ====================

    /**
     * Find an ExoPlanet by its name
     *
     * @param name the planet name
     * @return the ExoPlanet entity or null if not found
     */
    public ExoPlanet findExoPlanetByName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        return exoPlanetRepository.findByName(name);
    }

    /**
     * Find an ExoPlanet by its ID
     *
     * @param id the planet ID
     * @return the ExoPlanet entity or null if not found
     */
    public ExoPlanet findExoPlanetById(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        return exoPlanetRepository.findById(id).orElse(null);
    }

    /**
     * Update an existing ExoPlanet
     *
     * @param planet the planet with updated values
     * @return the saved planet entity
     */
    @Transactional
    public ExoPlanet updateExoPlanet(ExoPlanet planet) {
        if (planet == null || planet.getId() == null) {
            log.warn("Cannot update null planet or planet without ID");
            return null;
        }

        ExoPlanet saved = exoPlanetRepository.save(planet);
        log.info("Updated ExoPlanet: {} (id={})", saved.getName(), saved.getId());

        // Update solar system's habitable zone planet status if needed
        if (planet.getSolarSystemId() != null && planet.getSemiMajorAxis() != null) {
            solarSystemRepository.findById(planet.getSolarSystemId()).ifPresent(ss -> {
                updateHabitableZonePlanetStatus(ss);
            });
        }

        return saved;
    }

    /**
     * Delete an ExoPlanet by ID
     *
     * @param planetId the planet ID to delete
     */
    @Transactional
    public void deleteExoPlanet(String planetId) {
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
     * Add a new ExoPlanet (or moon) to the database.
     *
     * @param planet the planet to add
     * @return the saved planet
     */
    @Transactional
    public ExoPlanet addExoPlanet(ExoPlanet planet) {
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

        return saved;
    }

    /**
     * Update the hasHabitableZonePlanets flag for a solar system
     */
    private void updateHabitableZonePlanetStatus(SolarSystem solarSystem) {
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

    // ==================== Generated Planet Persistence ====================

    /**
     * Save generated planets from ACCRETE simulation to the database.
     * Creates or updates the SolarSystem entity and converts all ACCRETE Planet
     * objects to ExoPlanet JPA entities.
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

        // Find or create the solar system for this star
        SolarSystem solarSystem = findOrCreateSolarSystem(sourceStar);

        // Delete any existing generated planets for this system
        // (in case user regenerates for the same star)
        List<ExoPlanet> existingPlanets = exoPlanetRepository.findBySolarSystemId(solarSystem.getId());
        for (ExoPlanet existing : existingPlanets) {
            // Only delete planets that were generated (detected as "Simulated")
            if ("Simulated".equals(existing.getDetectionType())) {
                exoPlanetRepository.delete(existing);
            }
        }

        // Convert and save each generated planet and its moons
        int savedCount = 0;
        int moonCount = 0;
        int planetIndex = 1;
        for (Planet planet : planets) {
            // Save the planet
            ExoPlanet exoPlanet = convertAccretePlanetToExoPlanet(
                    planet,
                    sourceStar,
                    solarSystem.getId(),
                    planetIndex++,
                    null,  // no parent planet (this is a planet, not a moon)
                    false  // not a moon
            );
            exoPlanetRepository.save(exoPlanet);
            savedCount++;

            log.debug("Saved generated planet: {} (SMA={} AU, Mass={} Earth masses)",
                    exoPlanet.getName(),
                    exoPlanet.getSemiMajorAxis(),
                    exoPlanet.getMass());

            // Save moons for this planet
            List<Planet> moons = planet.getMoons();
            if (moons != null && !moons.isEmpty()) {
                int moonIndex = 1;
                for (Planet moon : moons) {
                    ExoPlanet exoMoon = convertAccretePlanetToExoPlanet(
                            moon,
                            sourceStar,
                            solarSystem.getId(),
                            moonIndex++,
                            exoPlanet.getId(),  // parent planet ID
                            true  // is a moon
                    );
                    // Override the name for moons (e.g., "Proxima Centauri b I" for first moon of planet b)
                    exoMoon.setName(exoPlanet.getName() + " " + toRomanNumeral(moonIndex - 1));
                    exoPlanetRepository.save(exoMoon);
                    moonCount++;

                    log.debug("Saved generated moon: {} (parent: {}, SMA={} AU)",
                            exoMoon.getName(),
                            exoPlanet.getName(),
                            exoMoon.getSemiMajorAxis());
                }
            }
        }

        // Update solar system planet count (excluding moons) and habitable zone status
        solarSystem.setPlanetCount(savedCount);
        updateHabitableZonePlanetStatus(solarSystem);
        solarSystemRepository.save(solarSystem);

        log.info("Saved {} generated planets and {} moons for star '{}' (system ID: {})",
                savedCount, moonCount, sourceStar.getDisplayName(), solarSystem.getId());

        return savedCount + moonCount;
    }

    /**
     * Convert an integer to Roman numerals for moon naming.
     */
    private String toRomanNumeral(int number) {
        if (number <= 0 || number > 20) {
            return String.valueOf(number);
        }
        String[] romanNumerals = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X",
                "XI", "XII", "XIII", "XIV", "XV", "XVI", "XVII", "XVIII", "XIX", "XX"};
        return romanNumerals[number - 1];
    }

    /**
     * Convert an ACCRETE Planet object to an ExoPlanet JPA entity.
     * Populates all available fields including extended properties for climate,
     * atmosphere, and habitability.
     *
     * @param planet         the ACCRETE planet or moon
     * @param hostStar       the host star
     * @param solarSystemId  the solar system ID
     * @param index          the planet/moon index (for naming)
     * @param parentPlanetId the parent planet ID (null for planets, set for moons)
     * @param isMoon         whether this is a moon
     * @return the ExoPlanet entity
     */
    private ExoPlanet convertAccretePlanetToExoPlanet(Planet planet,
                                                      StarObject hostStar,
                                                      String solarSystemId,
                                                      int index,
                                                      String parentPlanetId,
                                                      boolean isMoon) {
        ExoPlanet exoPlanet = new ExoPlanet();
        exoPlanet.setId(UUID.randomUUID().toString());

        // Generate planet name using standard convention (star name + letter)
        // For moons, the name will be overridden by the caller
        String planetLetter = getPlanetLetter(index);
        String starName = hostStar.getDisplayName();
        exoPlanet.setName(starName + " " + planetLetter);

        // Link to solar system and host star
        exoPlanet.setSolarSystemId(solarSystemId);
        exoPlanet.setHostStarId(hostStar.getId());
        exoPlanet.setStarName(starName);

        // Set moon-specific fields
        exoPlanet.setParentPlanetId(parentPlanetId);
        exoPlanet.setIsMoon(isMoon);

        // Mark as simulated
        exoPlanet.setPlanetStatus("Simulated");
        exoPlanet.setDetectionType("Simulated");

        // --- Basic Orbital Parameters ---
        exoPlanet.setSemiMajorAxis(planet.getSma());
        exoPlanet.setEccentricity(planet.getEccentricity());
        exoPlanet.setInclination(planet.getInclination());
        // Note: ACCRETE doesn't generate omega or longitude of ascending node,
        // so these remain null/default for simulated planets

        // Orbital period: convert from seconds to days
        double orbitalPeriodDays = planet.getOrbitalPeriod() / (24.0 * 3600.0);
        exoPlanet.setOrbitalPeriod(orbitalPeriodDays);

        // --- Basic Physical Properties ---
        // Mass: convert from solar masses to Earth masses
        exoPlanet.setMass(planet.massInEarthMasses());

        // Radius: convert from km to Earth radii (Earth radius ~6371 km)
        exoPlanet.setRadius(planet.getRadius() / 6371.0);

        // --- Planet Type and Classification ---
        exoPlanet.setPlanetType(planet.planetType());
        exoPlanet.setOrbitalZone(planet.getOrbitalZone());

        // --- Habitability Flags ---
        exoPlanet.setHabitable(planet.isHabitable());
        exoPlanet.setEarthlike(planet.isEarthlike());
        exoPlanet.setGasGiant(planet.isGasGiant());
        exoPlanet.setHabitableJovian(planet.isHabitableJovian());
        exoPlanet.setHabitableMoon(planet.isHabitableMoon());
        exoPlanet.setGreenhouseEffect(planet.isGreenhouseEffect());
        exoPlanet.setTidallyLocked(planet.isResonantPeriod());

        // --- Extended Physical Properties ---
        exoPlanet.setDensity(planet.getDensity());
        exoPlanet.setCoreRadius(planet.getCoreRadius());
        exoPlanet.setAxialTilt(planet.getAxialTilt());

        // Day length: convert from seconds to hours
        if (planet.getDayLength() > 0 && planet.getDayLength() < Double.MAX_VALUE) {
            exoPlanet.setDayLength(planet.getDayLength() / 3600.0);
        }

        // Surface gravity (in Earth gravities)
        if (planet.getSurfaceGravity() > 0 && planet.getSurfaceGravity() < Double.MAX_VALUE) {
            exoPlanet.setSurfaceGravity(planet.getSurfaceGravity());
            // Also set log g for catalog compatibility
            double surfaceGravityCmS2 = planet.getSurfaceGravity() * 980.665;
            exoPlanet.setLogG(Math.log10(surfaceGravityCmS2));
        }

        exoPlanet.setSurfaceAcceleration(planet.getSurfaceAcceleration());
        exoPlanet.setEscapeVelocity(planet.getEscapeVelocity());

        // --- Climate Properties ---
        exoPlanet.setHydrosphere(planet.getHydrosphere());
        exoPlanet.setCloudCover(planet.getCloudCover());
        exoPlanet.setIceCover(planet.getIceCover());
        exoPlanet.setAlbedo(planet.getAlbedo());

        if (planet.getSurfacePressure() > 0 && planet.getSurfacePressure() < Double.MAX_VALUE) {
            exoPlanet.setSurfacePressure(planet.getSurfacePressure());
        }

        if (planet.getVolatileGasInventory() > 0 && planet.getVolatileGasInventory() < Double.MAX_VALUE) {
            exoPlanet.setVolatileGasInventory(planet.getVolatileGasInventory());
        }

        // --- Temperature Properties ---
        if (planet.getSurfaceTemperature() > 0 && planet.getSurfaceTemperature() < Double.MAX_VALUE) {
            exoPlanet.setSurfaceTemperature(planet.getSurfaceTemperature());
            exoPlanet.setTempCalculated(planet.getSurfaceTemperature());
        } else if (planet.getEstimatedTemperature() > 0) {
            exoPlanet.setTempCalculated(planet.getEstimatedTemperature());
        }

        if (planet.getHighTemperature() > 0 && planet.getHighTemperature() < Double.MAX_VALUE) {
            exoPlanet.setHighTemperature(planet.getHighTemperature());
        }
        if (planet.getLowTemperature() > 0 && planet.getLowTemperature() < Double.MAX_VALUE) {
            exoPlanet.setLowTemperature(planet.getLowTemperature());
        }
        if (planet.getMaxTemperature() > 0 && planet.getMaxTemperature() < Double.MAX_VALUE) {
            exoPlanet.setMaxTemperature(planet.getMaxTemperature());
        }
        if (planet.getMinTemperature() > 0 && planet.getMinTemperature() < Double.MAX_VALUE) {
            exoPlanet.setMinTemperature(planet.getMinTemperature());
        }
        if (planet.getBoilingPoint() > 0 && planet.getBoilingPoint() < Double.MAX_VALUE) {
            exoPlanet.setBoilingPoint(planet.getBoilingPoint());
        }
        if (planet.getExosphericTemperature() > 0 && planet.getExosphericTemperature() < Double.MAX_VALUE) {
            exoPlanet.setExosphericTemperature(planet.getExosphericTemperature());
        }
        exoPlanet.setGreenhouseRise(planet.getGreenhouseRise());

        // --- Atmospheric Properties ---
        if (planet.getMinimumMolecularWeight() > 0 && planet.getMinimumMolecularWeight() < Double.MAX_VALUE) {
            exoPlanet.setMinimumMolecularWeight(planet.getMinimumMolecularWeight());
        }

        exoPlanet.setAtmosphereType(planet.atmosphereType());

        // Convert atmosphere composition to string format
        if (planet.getAtmosphere() != null && !planet.getAtmosphere().isEmpty()) {
            StringBuilder atmoComp = new StringBuilder();
            for (var chem : planet.getAtmosphere()) {
                if (atmoComp.length() > 0) {
                    atmoComp.append(";");
                }
                atmoComp.append(chem.getChem().getSymbol())
                        .append(":")
                        .append(String.format("%.2f", chem.getSurfacePressure()));
            }
            exoPlanet.setAtmosphereComposition(atmoComp.toString());
        }

        // --- Host Star Properties ---
        exoPlanet.setRa(hostStar.getRa());
        exoPlanet.setDec(hostStar.getDeclination());
        exoPlanet.setStarDistance(hostStar.getDistance());
        exoPlanet.setStarSpType(hostStar.getSpectralClass());

        double starMass = hostStar.getMass();
        if (starMass > 0) {
            exoPlanet.setStarMass(starMass);
        }

        double starRadius = hostStar.getRadius();
        if (starRadius > 0) {
            exoPlanet.setStarRadius(starRadius);
        }

        return exoPlanet;
    }

    /**
     * Get the planet letter designation (b, c, d, ..., z, aa, ab, ...).
     * Uses lowercase letters starting from 'b' (as is convention for exoplanets,
     * where 'a' is reserved for the star).
     *
     * @param planetIndex the 1-based planet index
     * @return the letter designation
     */
    private String getPlanetLetter(int planetIndex) {
        if (planetIndex <= 25) {
            // b through z (index 1 = 'b', index 25 = 'z')
            return String.valueOf((char) ('a' + planetIndex));
        } else {
            // For more than 25 planets, use aa, ab, ac, etc.
            int first = (planetIndex - 26) / 26;
            int second = (planetIndex - 26) % 26;
            return String.valueOf((char) ('a' + first)) + (char) ('a' + second);
        }
    }

}
