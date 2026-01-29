package com.teamgannon.trips.service;

import com.teamgannon.trips.events.SetContextDataSetEvent;
import com.teamgannon.trips.jpa.model.ExoPlanet;
import com.teamgannon.trips.jpa.model.SolarSystem;
import com.teamgannon.trips.jpa.model.SolarSystemFeature;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.jpa.repository.ExoPlanetRepository;
import com.teamgannon.trips.jpa.repository.SolarSystemFeatureRepository;
import com.teamgannon.trips.jpa.repository.SolarSystemRepository;
import com.teamgannon.trips.jpa.repository.StarObjectRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Initializes Sol's solar system with the 8 planets (plus Pluto) at application startup
 * and whenever a new dataset is activated.
 * This ensures that when a user "Enters System" on Sol, they see our familiar planets.
 */
@Slf4j
@Service
public class SolPlanetsInitializer {

    private final StarObjectRepository starObjectRepository;
    private final SolarSystemRepository solarSystemRepository;
    private final ExoPlanetRepository exoPlanetRepository;
    private final SolarSystemFeatureRepository featureRepository;

    // Earth constants for unit conversion
    private static final double EARTH_MASS_KG = 5.97e24;  // kg
    private static final double EARTH_RADIUS_KM = 6378.0;  // km
    private static final double KM_TO_AU = 149597870.7;    // km per AU
    private static final double MILLION_KM_TO_AU = 149.597870700;  // 10^6 km per AU

    public SolPlanetsInitializer(StarObjectRepository starObjectRepository,
                                  SolarSystemRepository solarSystemRepository,
                                  ExoPlanetRepository exoPlanetRepository,
                                  SolarSystemFeatureRepository featureRepository) {
        this.starObjectRepository = starObjectRepository;
        this.solarSystemRepository = solarSystemRepository;
        this.exoPlanetRepository = exoPlanetRepository;
        this.featureRepository = featureRepository;
    }

    @PostConstruct
    @Transactional
    public void initializeSolPlanets() {
        log.info("Checking if Sol's planets need to be initialized...");

        // Find Sol - it's at coordinates (0, 0, 0) or named "Sol"
        StarObject sol = findSol();
        if (sol == null) {
            log.info("Sol not found in any dataset - planets will be created when Sol is loaded");
            return;
        }

        // Check if Sol already has planets
        List<ExoPlanet> existingPlanets = exoPlanetRepository.findByStarName("Sol");
        if (existingPlanets == null || existingPlanets.isEmpty()) {
            existingPlanets = exoPlanetRepository.findByHostStarId(sol.getId());
        }

        if (existingPlanets != null && !existingPlanets.isEmpty()) {
            boolean hasMoons = existingPlanets.stream()
                .anyMatch(planet -> Boolean.TRUE.equals(planet.getIsMoon()));
            if (hasMoons) {
                log.info("Sol already has {} planets/moons initialized", existingPlanets.size());
                return;
            }

            SolarSystem solarSystem = getOrCreateSolSolarSystem(sol);
            java.util.Map<String, ExoPlanet> planetsByName = new java.util.HashMap<>();
            for (ExoPlanet planet : existingPlanets) {
                if (!Boolean.TRUE.equals(planet.getIsMoon())) {
                    planetsByName.put(planet.getName(), planet);
                }
            }
            if (!planetsByName.isEmpty()) {
                createSolMoons(sol, solarSystem, planetsByName);
                log.info("Sol planets existed but moons were missing; moons created");
            }
            return;
        }

        // Get or create Sol's solar system
        SolarSystem solarSystem = getOrCreateSolSolarSystem(sol);

        // Create the planets
        createSolPlanets(sol, solarSystem);

        log.info("Sol's planets have been initialized successfully");
    }

    /**
     * Event listener that triggers Sol planet initialization when a new dataset is activated.
     * This ensures Sol's planets are available for any dataset that contains Sol.
     *
     * @param event the dataset context change event
     */
    @EventListener
    @Transactional
    public void onDatasetContextChange(SetContextDataSetEvent event) {
        if (event.getDescriptor() != null) {
            log.info("Dataset context changed to '{}', checking Sol initialization...",
                    event.getDescriptor().getDataSetName());
            initializeSolPlanetsInternal();
        }
    }

    /**
     * Internal initialization logic, called by both @PostConstruct and event listener.
     */
    private void initializeSolPlanetsInternal() {
        // Find Sol - it's at coordinates (0, 0, 0) or named "Sol"
        StarObject sol = findSol();
        if (sol == null) {
            log.debug("Sol not found in current dataset - no planets to initialize");
            return;
        }

        // Check if Sol already has planets
        List<ExoPlanet> existingPlanets = exoPlanetRepository.findByStarName("Sol");
        if (existingPlanets == null || existingPlanets.isEmpty()) {
            existingPlanets = exoPlanetRepository.findByHostStarId(sol.getId());
        }

        if (existingPlanets != null && !existingPlanets.isEmpty()) {
            boolean hasMoons = existingPlanets.stream()
                .anyMatch(planet -> Boolean.TRUE.equals(planet.getIsMoon()));
            if (hasMoons) {
                log.debug("Sol already has {} planets/moons initialized", existingPlanets.size());
                return;
            }

            SolarSystem solarSystem = getOrCreateSolSolarSystem(sol);
            java.util.Map<String, ExoPlanet> planetsByName = new java.util.HashMap<>();
            for (ExoPlanet planet : existingPlanets) {
                if (!Boolean.TRUE.equals(planet.getIsMoon())) {
                    planetsByName.put(planet.getName(), planet);
                }
            }
            if (!planetsByName.isEmpty()) {
                createSolMoons(sol, solarSystem, planetsByName);
                log.info("Sol planets existed but moons were missing; moons created");
            }
            return;
        }

        // Get or create Sol's solar system
        SolarSystem solarSystem = getOrCreateSolSolarSystem(sol);

        // Create the planets
        createSolPlanets(sol, solarSystem);

        log.info("Sol's planets have been initialized for current dataset");
    }

    /**
     * Find Sol in the database. Sol is at coordinates (0, 0, 0) in the TRIPS coordinate system.
     */
    private StarObject findSol() {
        // Search by common name variations
        List<StarObject> solCandidates = starObjectRepository.findByCommonNameContaining("Sol");
        if (solCandidates != null && !solCandidates.isEmpty()) {
            for (StarObject candidate : solCandidates) {
                // Check if it's at the origin
                if (isAtOrigin(candidate)) {
                    log.info("Found Sol: {} at ({}, {}, {})",
                            candidate.getDisplayName(), candidate.getX(), candidate.getY(), candidate.getZ());
                    return candidate;
                }
            }
            // If we found "Sol" but not at origin, still use it
            return solCandidates.get(0);
        }

        // Try "Sun"
        solCandidates = starObjectRepository.findByCommonNameContaining("Sun");
        if (solCandidates != null && !solCandidates.isEmpty()) {
            for (StarObject candidate : solCandidates) {
                if (isAtOrigin(candidate)) {
                    return candidate;
                }
            }
        }

        return null;
    }

    private boolean isAtOrigin(StarObject star) {
        double tolerance = 0.001;
        // X, Y, Z are primitive doubles - just check if they're near zero
        return Math.abs(star.getX()) < tolerance &&
               Math.abs(star.getY()) < tolerance &&
               Math.abs(star.getZ()) < tolerance;
    }

    private SolarSystem getOrCreateSolSolarSystem(StarObject sol) {
        // Check if Sol already has a solar system
        if (sol.getSolarSystemId() != null) {
            Optional<SolarSystem> existing = solarSystemRepository.findById(sol.getSolarSystemId());
            if (existing.isPresent()) {
                return existing.get();
            }
        }

        // Create a new solar system for Sol
        SolarSystem solarSystem = new SolarSystem();
        solarSystem.setId(UUID.randomUUID().toString());
        solarSystem.setSystemName("Sol System");
        solarSystem.setPrimaryStarId(sol.getId());
        solarSystem.setStarCount(1);
        solarSystem.setPlanetCount(8);  // Will be updated
        solarSystem.setHasHabitableZonePlanets(true);
        // Sol's habitable zone approximately 0.95 AU to 1.37 AU
        solarSystem.setHabitableZoneInnerAU(0.95);
        solarSystem.setHabitableZoneOuterAU(1.37);

        solarSystem = solarSystemRepository.save(solarSystem);

        // Update Sol with the solar system reference
        sol.setSolarSystemId(solarSystem.getId());
        starObjectRepository.save(sol);

        return solarSystem;
    }

    private void createSolPlanets(StarObject sol, SolarSystem solarSystem) {
        log.info("Creating Sol's planets...");

        // Planet data: name, mass (10^24 kg), diameter (km), distance (10^6 km),
        //              orbital period (days), eccentricity, inclination (deg), temp (C),
        //              habitable, planetType, hasRings, ringType
        Object[][] planetData = {
                // Mercury
                {"Mercury", 0.33, 4879.0, 57.9, 88.0, 0.206, 7.0, 167.0, false, "Terrestrial", false, null},
                // Venus
                {"Venus", 4.87, 12104.0, 108.2, 224.7, 0.007, 3.4, 464.0, false, "Terrestrial", false, null},
                // Earth
                {"Earth", 5.97, 12756.0, 149.6, 365.2, 0.017, 0.0, 15.0, true, "Terrestrial", false, null},
                // Mars
                {"Mars", 0.642, 6792.0, 228.0, 687.0, 0.094, 1.8, -65.0, false, "Terrestrial", false, null},
                // Jupiter (faint ring system)
                {"Jupiter", 1898.0, 142984.0, 778.5, 4331.0, 0.049, 1.3, -110.0, false, "Gas Giant", true, "CUSTOM"},
                // Saturn (prominent ring system)
                {"Saturn", 568.0, 120536.0, 1432.0, 10747.0, 0.052, 2.5, -140.0, false, "Gas Giant", true, "SATURN"},
                // Uranus (narrow dark rings)
                {"Uranus", 86.8, 51118.0, 2867.0, 30589.0, 0.047, 0.8, -195.0, false, "Ice Giant", true, "URANUS"},
                // Neptune (faint ring arcs)
                {"Neptune", 102.0, 49528.0, 4515.0, 59800.0, 0.01, 1.8, -200.0, false, "Ice Giant", true, "NEPTUNE"},
                // Pluto (dwarf planet)
                {"Pluto", 0.013, 2376.0, 5906.4, 90560.0, 0.244, 17.2, -225.0, false, "Dwarf Planet", false, null}
        };

        java.util.Map<String, ExoPlanet> createdPlanets = new java.util.HashMap<>();
        for (Object[] data : planetData) {
            ExoPlanet planet = createPlanet(sol, solarSystem, data);
            createdPlanets.put(planet.getName(), planet);
        }

        createSolMoons(sol, solarSystem, createdPlanets);

        // Create asteroid belts and other features
        createSolFeatures(solarSystem);

        // Update solar system planet count
        solarSystem.setPlanetCount(planetData.length);
        solarSystemRepository.save(solarSystem);
    }

    private ExoPlanet createPlanet(StarObject sol, SolarSystem solarSystem, Object[] data) {
        String name = (String) data[0];
        double massE24Kg = (Double) data[1];
        double diameterKm = (Double) data[2];
        double distanceMillionKm = (Double) data[3];
        double orbitalPeriodDays = (Double) data[4];
        double eccentricity = (Double) data[5];
        double inclination = (Double) data[6];
        double tempCelsius = (Double) data[7];
        boolean habitable = (Boolean) data[8];
        String planetType = (String) data[9];
        boolean hasRings = (Boolean) data[10];
        String ringType = (String) data[11];

        ExoPlanet planet = new ExoPlanet();
        planet.setId(UUID.randomUUID().toString());
        planet.setName(name);
        planet.setStarName("Sol");
        planet.setSolarSystemId(solarSystem.getId());
        planet.setHostStarId(sol.getId());
        planet.setIsMoon(false);
        planet.setPlanetStatus("Confirmed");

        // Convert mass to Jupiter masses (Jupiter = 1898 × 10^24 kg)
        double massJupiter = massE24Kg / 1898.0;
        planet.setMass(massJupiter);

        // Convert diameter to Jupiter radii (Jupiter diameter = 142984 km, radius = 71492 km)
        double radiusKm = diameterKm / 2.0;
        double radiusJupiter = radiusKm / 71492.0;
        planet.setRadius(radiusJupiter);

        // Convert distance to AU
        double semiMajorAxisAU = distanceMillionKm / MILLION_KM_TO_AU;
        planet.setSemiMajorAxis(semiMajorAxisAU);

        planet.setOrbitalPeriod(orbitalPeriodDays);
        planet.setEccentricity(eccentricity);
        planet.setInclination(inclination);

        // Temperature (convert Celsius to Kelvin for storage)
        double tempKelvin = tempCelsius + 273.15;
        planet.setTempCalculated(tempKelvin);
        planet.setSurfaceTemperature(tempKelvin);

        // Set habitability
        planet.setHabitable(habitable);
        planet.setEarthlike(name.equals("Earth"));

        // Set planet type
        planet.setPlanetType(planetType);
        planet.setGasGiant(planetType.contains("Giant"));

        // Detection info
        planet.setDetectionType("Direct");
        planet.setDiscovered(null);  // Known since antiquity

        // Orbital elements - assume circular reference frame
        planet.setLongitudeOfAscendingNode(0.0);
        planet.setOmega(0.0);  // Argument of periapsis

        // Ring system properties
        if (hasRings) {
            planet.setHasRings(true);
            planet.setRingType(ringType);
            setRingParameters(planet, name, radiusKm);
        }

        exoPlanetRepository.save(planet);
        log.info("Created planet: {} at {} AU{}", name, "%.2f".formatted(semiMajorAxisAU),
                hasRings ? " (with rings)" : "");
        return planet;
    }

    /**
     * Sets ring parameters for planets with ring systems.
     * Ring radii are in planetary radii, converted to AU for storage.
     */
    private void setRingParameters(ExoPlanet planet, String planetName, double planetRadiusKm) {
        // Convert planetary radius to AU for ring calculations
        double planetRadiusAU = planetRadiusKm / KM_TO_AU;

        switch (planetName) {
            case "Jupiter" -> {
                // Jupiter's faint ring: 1.29 to 1.81 planetary radii (Halo + Main ring)
                planet.setRingInnerRadiusAU(1.29 * planetRadiusAU);
                planet.setRingOuterRadiusAU(1.81 * planetRadiusAU);
                planet.setRingThickness(0.001);  // Very thin
                planet.setRingInclination(0.0);
                planet.setRingPrimaryColor("#4A4A4A");  // Dark gray
                planet.setRingSecondaryColor("#3A3A3A");
            }
            case "Saturn" -> {
                // Saturn's prominent rings: D ring inner (~1.11) to E ring outer (~8.0) radii
                // Main visible rings: 1.24 (C ring) to 2.27 (A ring) radii
                planet.setRingInnerRadiusAU(1.24 * planetRadiusAU);
                planet.setRingOuterRadiusAU(2.27 * planetRadiusAU);
                planet.setRingThickness(0.001);  // Extremely thin
                planet.setRingInclination(26.7);  // Saturn's axial tilt
                planet.setRingPrimaryColor("#E6DCC8");  // Icy tan
                planet.setRingSecondaryColor("#B4AAA0");  // Dusty gray
            }
            case "Uranus" -> {
                // Uranus rings: 1.64 to 2.0 planetary radii
                planet.setRingInnerRadiusAU(1.64 * planetRadiusAU);
                planet.setRingOuterRadiusAU(2.0 * planetRadiusAU);
                planet.setRingThickness(0.0005);
                planet.setRingInclination(97.8);  // Uranus's extreme tilt
                planet.setRingPrimaryColor("#505A5C");  // Dark gray
                planet.setRingSecondaryColor("#32323C");  // Darker
            }
            case "Neptune" -> {
                // Neptune's faint ring arcs: 1.69 to 2.54 planetary radii
                planet.setRingInnerRadiusAU(1.69 * planetRadiusAU);
                planet.setRingOuterRadiusAU(2.54 * planetRadiusAU);
                planet.setRingThickness(0.0005);
                planet.setRingInclination(28.3);  // Neptune's axial tilt
                planet.setRingPrimaryColor("#3C3C4A");  // Very dark blue-gray
                planet.setRingSecondaryColor("#28283C");
            }
        }
    }

    /**
     * Creates natural features for the Sol system: asteroid belt and Kuiper belt.
     */
    private void createSolFeatures(SolarSystem solarSystem) {
        log.info("Creating Sol's asteroid belts...");

        // Check if features already exist
        java.util.List<SolarSystemFeature> existing = featureRepository.findBySolarSystemId(solarSystem.getId());
        if (existing != null && !existing.isEmpty()) {
            log.info("Sol system already has {} features", existing.size());
            return;
        }

        // Main Asteroid Belt: 2.1 to 3.3 AU
        SolarSystemFeature asteroidBelt = new SolarSystemFeature(
                "Main Asteroid Belt",
                SolarSystemFeature.FeatureType.ASTEROID_BELT,
                SolarSystemFeature.FeatureCategory.NATURAL
        );
        asteroidBelt.setSolarSystemId(solarSystem.getId());
        asteroidBelt.setInnerRadiusAU(2.1);
        asteroidBelt.setOuterRadiusAU(3.3);
        asteroidBelt.setThickness(0.15);  // Thick vertical distribution
        asteroidBelt.setInclinationDeg(10.0);  // Typical asteroid inclination
        asteroidBelt.setEccentricity(0.08);
        asteroidBelt.setParticleCount(5000);
        asteroidBelt.setMinParticleSize(0.5);
        asteroidBelt.setMaxParticleSize(2.0);
        asteroidBelt.setPrimaryColor("#8C8278");  // Rocky gray
        asteroidBelt.setSecondaryColor("#645A50");  // Brown-gray
        asteroidBelt.setOpacity(0.8);
        asteroidBelt.setAnimated(true);
        asteroidBelt.setAnimationSpeed(1.0);
        asteroidBelt.setNavigationHazard(false);  // Not really hazardous despite sci-fi depictions
        asteroidBelt.setNotes("The asteroid belt between Mars and Jupiter, containing millions of rocky bodies. " +
                "Despite popular depictions, asteroids are widely spaced (average ~1 million km apart).");
        featureRepository.save(asteroidBelt);
        log.info("Created Main Asteroid Belt: 2.1-3.3 AU");

        // Kuiper Belt: 30 to 50 AU
        SolarSystemFeature kuiperBelt = new SolarSystemFeature(
                "Kuiper Belt",
                SolarSystemFeature.FeatureType.KUIPER_BELT,
                SolarSystemFeature.FeatureCategory.NATURAL
        );
        kuiperBelt.setSolarSystemId(solarSystem.getId());
        kuiperBelt.setInnerRadiusAU(30.0);
        kuiperBelt.setOuterRadiusAU(50.0);
        kuiperBelt.setThickness(0.2);  // Thicker than asteroid belt
        kuiperBelt.setInclinationDeg(15.0);
        kuiperBelt.setEccentricity(0.1);
        kuiperBelt.setParticleCount(3000);
        kuiperBelt.setMinParticleSize(0.8);
        kuiperBelt.setMaxParticleSize(3.0);
        kuiperBelt.setPrimaryColor("#B4BEC8");  // Icy blue-gray
        kuiperBelt.setSecondaryColor("#8C8C96");
        kuiperBelt.setOpacity(0.6);
        kuiperBelt.setAnimated(true);
        kuiperBelt.setAnimationSpeed(0.5);  // Slower due to greater distance
        kuiperBelt.setNavigationHazard(false);
        kuiperBelt.setNotes("A region of icy bodies beyond Neptune, including dwarf planets like Pluto, Eris, and Makemake. " +
                "Contains an estimated 100,000+ objects larger than 100 km.");
        featureRepository.save(kuiperBelt);
        log.info("Created Kuiper Belt: 30-50 AU");
    }

    private void createSolMoons(StarObject sol,
                                SolarSystem solarSystem,
                                java.util.Map<String, ExoPlanet> planetsByName) {
        Object[][] moonData = {
                // Earth
                {"Moon", "Earth", 0.073, 3474.8, 0.3844, 27.32, 0.055, 5.145},
                // Mars
                {"Phobos", "Mars", 0.0000000107, 22.4, 0.009376, 0.319, 0.015, 1.08},
                {"Deimos", "Mars", 0.00000000148, 12.4, 0.023463, 1.263, 0.0002, 1.79},
                // Jupiter (Galilean moons)
                {"Io", "Jupiter", 0.0893, 3643.0, 0.4217, 1.769, 0.0041, 0.04},
                {"Europa", "Jupiter", 0.0480, 3122.0, 0.6711, 3.551, 0.0094, 0.47},
                {"Ganymede", "Jupiter", 0.1480, 5268.0, 1.0704, 7.155, 0.0013, 0.18},
                {"Callisto", "Jupiter", 0.1080, 4821.0, 1.8827, 16.689, 0.0074, 0.28},
                // Saturn
                {"Titan", "Saturn", 0.1345, 5150.0, 1.2219, 15.945, 0.0288, 0.33},
                {"Enceladus", "Saturn", 0.000108, 504.0, 0.2380, 1.370, 0.0047, 0.02},
                // Uranus
                {"Titania", "Uranus", 0.0035, 1578.0, 0.4359, 8.706, 0.0011, 0.08},
                {"Oberon", "Uranus", 0.0030, 1523.0, 0.5835, 13.463, 0.0014, 0.07},
                // Neptune
                {"Triton", "Neptune", 0.0214, 2706.0, 0.3548, 5.877, 0.00002, 156.9},
                // Pluto
                {"Charon", "Pluto", 0.001586, 1212.0, 0.0196, 6.387, 0.0002, 0.0}
        };

        for (Object[] data : moonData) {
            String moonName = (String) data[0];
            String parentName = (String) data[1];
            ExoPlanet parent = planetsByName.get(parentName);
            if (parent == null) {
                log.warn("Skipping moon {}: parent planet {} not found", moonName, parentName);
                continue;
            }
            createMoon(sol, solarSystem, parent, data);
        }
    }

    private void createMoon(StarObject sol,
                            SolarSystem solarSystem,
                            ExoPlanet parent,
                            Object[] data) {
        String name = (String) data[0];
        double massE24Kg = (Double) data[2];
        double diameterKm = (Double) data[3];
        double distanceMillionKm = (Double) data[4];
        double orbitalPeriodDays = (Double) data[5];
        double eccentricity = (Double) data[6];
        double inclination = (Double) data[7];

        ExoPlanet moon = new ExoPlanet();
        moon.setId(UUID.randomUUID().toString());
        moon.setName(name);
        moon.setStarName("Sol");
        moon.setSolarSystemId(solarSystem.getId());
        moon.setHostStarId(sol.getId());
        moon.setParentPlanetId(parent.getId());
        moon.setIsMoon(true);
        moon.setPlanetStatus("Confirmed");

        // Convert mass to Jupiter masses (Jupiter = 1898 × 10^24 kg)
        double massJupiter = massE24Kg / 1898.0;
        moon.setMass(massJupiter);

        // Convert diameter to Jupiter radii (Jupiter diameter = 142984 km, radius = 71492 km)
        double radiusKm = diameterKm / 2.0;
        double radiusJupiter = radiusKm / 71492.0;
        moon.setRadius(radiusJupiter);

        // Moon semi-major axis is relative to the parent planet (store in AU)
        double semiMajorAxisAU = distanceMillionKm / MILLION_KM_TO_AU;
        moon.setSemiMajorAxis(semiMajorAxisAU);
        moon.setOrbitalPeriod(orbitalPeriodDays);
        moon.setEccentricity(eccentricity);
        moon.setInclination(inclination);

        moon.setPlanetType("Moon");
        moon.setGasGiant(false);
        moon.setDetectionType("Direct");

        exoPlanetRepository.save(moon);
        log.info("Created moon: {} for {}", name, parent.getName());
    }
}
