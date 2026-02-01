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
        log.info("Creating Sol's planets with full properties...");

        // Create each planet with full properties and track them
        java.util.Map<String, ExoPlanet> createdPlanets = new java.util.HashMap<>();

        createdPlanets.put("Mercury", createMercury(sol, solarSystem));
        createdPlanets.put("Venus", createVenus(sol, solarSystem));
        createdPlanets.put("Earth", createEarth(sol, solarSystem));
        createdPlanets.put("Mars", createMars(sol, solarSystem));
        createdPlanets.put("Jupiter", createJupiter(sol, solarSystem));
        createdPlanets.put("Saturn", createSaturn(sol, solarSystem));
        createdPlanets.put("Uranus", createUranus(sol, solarSystem));
        createdPlanets.put("Neptune", createNeptune(sol, solarSystem));
        createdPlanets.put("Pluto", createPluto(sol, solarSystem));

        // Create moons for planets
        createSolMoons(sol, solarSystem, createdPlanets);

        // Create asteroid belts and other features
        createSolFeatures(solarSystem);

        // Update solar system planet count
        solarSystem.setPlanetCount(9);
        solarSystemRepository.save(solarSystem);

        log.info("Created {} planets with full properties", createdPlanets.size());
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

    // ==================== Individual Planet Creation Methods ====================

    private ExoPlanet createMercury(StarObject sol, SolarSystem solarSystem) {
        ExoPlanet planet = createBasePlanet(sol, solarSystem, "Mercury");

        // Orbital parameters
        planet.setSemiMajorAxis(0.387);
        planet.setOrbitalPeriod(87.97);
        planet.setEccentricity(0.206);
        planet.setInclination(7.0);
        planet.setLongitudeOfAscendingNode(48.33);
        planet.setOmega(29.12);

        // Physical properties
        planet.setMass(0.33 / 1898.0);  // Jupiter masses
        planet.setRadius(2439.7 / 71492.0);  // Jupiter radii
        planet.setDensity(5.43);
        planet.setCoreRadius(1800.0);  // Large iron core ~75% of radius
        planet.setAxialTilt(0.034);
        planet.setDayLength(4222.6);  // hours (58.65 Earth days)
        planet.setSurfaceGravity(0.38);
        planet.setSurfaceAcceleration(3.7);
        planet.setEscapeVelocity(4250.0);
        planet.setAlbedo(0.142);
        planet.setGeometricAlbedo(0.142);

        // Temperature (Kelvin)
        planet.setSurfaceTemperature(440.0);  // Mean
        planet.setTempCalculated(440.0);
        planet.setTempMeasured(440.0);
        planet.setHighTemperature(700.0);  // Day side max
        planet.setLowTemperature(100.0);   // Night side min
        planet.setMaxTemperature(700.0);   // Subsolar point
        planet.setMinTemperature(100.0);   // Polar night
        planet.setExosphericTemperature(2000.0);
        planet.setGreenhouseRise(0.0);  // No atmosphere to trap heat

        // Atmosphere (essentially none)
        planet.setAtmosphereType("None");
        planet.setSurfacePressure(0.0);
        planet.setAtmosphereComposition("Na:trace;K:trace;O2:trace");
        planet.setMolecules("Na, K, O2, H2, He");
        planet.setMinimumMolecularWeight(2.0);  // Can't retain anything

        // Climate
        planet.setHydrosphere(0.0);
        planet.setCloudCover(0.0);
        planet.setIceCover(0.0);  // Except polar craters

        // Classification
        planet.setPlanetType("Terrestrial");
        planet.setOrbitalZone(1);
        planet.setGasGiant(false);
        planet.setHabitable(false);
        planet.setEarthlike(false);
        planet.setTidallyLocked(false);  // 3:2 resonance
        planet.setGreenhouseEffect(false);
        planet.setHasRings(false);

        // Host star properties
        setSolStarProperties(planet);

        // Notes
        planet.setNotes("Closest planet to the Sun. Extreme temperature variation between day and night sides. " +
                "3:2 spin-orbit resonance. No moons. Heavily cratered surface similar to the Moon.");

        exoPlanetRepository.save(planet);
        log.info("Created Mercury with full properties");
        return planet;
    }

    private ExoPlanet createVenus(StarObject sol, SolarSystem solarSystem) {
        ExoPlanet planet = createBasePlanet(sol, solarSystem, "Venus");

        // Orbital parameters
        planet.setSemiMajorAxis(0.723);
        planet.setOrbitalPeriod(224.7);
        planet.setEccentricity(0.007);
        planet.setInclination(3.39);
        planet.setLongitudeOfAscendingNode(76.68);
        planet.setOmega(54.85);

        // Physical properties
        planet.setMass(4.87 / 1898.0);
        planet.setRadius(6051.8 / 71492.0);
        planet.setDensity(5.24);
        planet.setCoreRadius(3200.0);  // Estimated iron core
        planet.setAxialTilt(177.4);  // Retrograde rotation
        planet.setDayLength(5832.5);  // hours (243 Earth days, retrograde)
        planet.setSurfaceGravity(0.91);
        planet.setSurfaceAcceleration(8.87);
        planet.setEscapeVelocity(10360.0);
        planet.setAlbedo(0.77);  // Very reflective clouds
        planet.setGeometricAlbedo(0.67);

        // Temperature
        planet.setSurfaceTemperature(737.0);
        planet.setTempCalculated(737.0);
        planet.setTempMeasured(737.0);
        planet.setHighTemperature(737.0);  // Nearly uniform due to atmosphere
        planet.setLowTemperature(737.0);
        planet.setMaxTemperature(737.0);
        planet.setMinTemperature(737.0);
        planet.setExosphericTemperature(350.0);
        planet.setGreenhouseRise(500.0);  // Massive greenhouse effect
        planet.setBoilingPoint(553.0);  // At 92 bar

        // Atmosphere
        planet.setAtmosphereType("Poisonous");
        planet.setSurfacePressure(92000.0);  // 92 bar in millibars
        planet.setAtmosphereComposition("CO2:96.5;N2:3.5;SO2:0.015;Ar:0.007;H2O:0.002;CO:0.002");
        planet.setMolecules("CO2, N2, SO2, H2SO4, CO, HCl, HF");
        planet.setMinimumMolecularWeight(4.0);
        planet.setVolatileGasInventory(100.0);

        // Climate
        planet.setHydrosphere(0.0);
        planet.setCloudCover(1.0);  // 100% cloud cover
        planet.setIceCover(0.0);

        // Classification
        planet.setPlanetType("Venusian");
        planet.setOrbitalZone(1);
        planet.setGasGiant(false);
        planet.setHabitable(false);
        planet.setEarthlike(false);
        planet.setTidallyLocked(false);
        planet.setGreenhouseEffect(true);
        planet.setHasRings(false);

        setSolStarProperties(planet);

        // Notes
        planet.setNotes("Earth's 'sister planet' by size but with runaway greenhouse effect. " +
                "Surface pressure 92x Earth. Sulfuric acid clouds. Retrograde rotation (243 Earth days). " +
                "Hottest surface in solar system despite not being closest to Sun.");

        exoPlanetRepository.save(planet);
        log.info("Created Venus with full properties");
        return planet;
    }

    private ExoPlanet createEarth(StarObject sol, SolarSystem solarSystem) {
        ExoPlanet planet = createBasePlanet(sol, solarSystem, "Earth");

        // Orbital parameters
        planet.setSemiMajorAxis(1.0);
        planet.setOrbitalPeriod(365.256);
        planet.setEccentricity(0.0167);
        planet.setInclination(0.0);  // Reference plane
        planet.setLongitudeOfAscendingNode(0.0);
        planet.setOmega(102.9);

        // Physical properties
        planet.setMass(5.97 / 1898.0);  // Jupiter masses
        planet.setRadius(6371.0 / 71492.0);  // Jupiter radii
        planet.setDensity(5.514);
        planet.setCoreRadius(3485.0);  // Iron core radius in km
        planet.setAxialTilt(23.44);
        planet.setDayLength(23.934);  // Sidereal day in hours
        planet.setSurfaceGravity(1.0);
        planet.setSurfaceAcceleration(9.807);
        planet.setEscapeVelocity(11186.0);
        planet.setAlbedo(0.306);
        planet.setGeometricAlbedo(0.367);

        // Temperature (all in Kelvin)
        planet.setSurfaceTemperature(288.0);  // 15°C global average
        planet.setTempCalculated(255.0);  // Equilibrium temp without atmosphere
        planet.setTempMeasured(288.0);  // Actual measured
        planet.setHighTemperature(313.0);  // 40°C typical hot day
        planet.setLowTemperature(184.0);   // -89°C (Antarctica record)
        planet.setMaxTemperature(329.85);  // 56.7°C Death Valley record
        planet.setMinTemperature(184.0);   // -89.2°C Vostok Station
        planet.setExosphericTemperature(1500.0);
        planet.setGreenhouseRise(33.0);  // Atmosphere adds 33K
        planet.setBoilingPoint(373.15);  // 100°C at 1 atm

        // Atmosphere
        planet.setAtmosphereType("Breathable");
        planet.setSurfacePressure(1013.25);  // 1 atm in millibars
        planet.setAtmosphereComposition("N2:780840;O2:209460;Ar:9340;CO2:420;Ne:18;He:5;CH4:2;Kr:1;H2:0.5");
        planet.setMolecules("N2, O2, H2O, Ar, CO2, Ne, He, CH4, O3, N2O, CO");
        planet.setMinimumMolecularWeight(2.0);
        planet.setVolatileGasInventory(1.0);  // Reference value
        planet.setLogG(2.99);  // log10(surface gravity in cm/s²)

        // Climate
        planet.setHydrosphere(0.71);  // 71% ocean coverage
        planet.setCloudCover(0.67);   // ~67% average cloud cover
        planet.setIceCover(0.03);     // ~3% ice caps and glaciers

        // Classification
        planet.setPlanetType("Terrestrial");
        planet.setOrbitalZone(2);  // Habitable zone
        planet.setGasGiant(false);
        planet.setHabitable(true);
        planet.setEarthlike(true);
        planet.setTidallyLocked(false);
        planet.setGreenhouseEffect(false);  // Natural greenhouse, not runaway
        planet.setHasRings(false);
        planet.setHabitableMoon(false);  // Moon is not habitable (no atmosphere)

        // Host star properties
        setSolStarProperties(planet);

        // Science Fiction properties for Earth
        planet.setPopulation(8000000000L);  // ~8 billion
        planet.setTechLevel(10);  // Modern technological civilization
        planet.setColonized(true);  // Well, we live here
        planet.setColonizationYear(-200000);  // Homo sapiens ~200,000 years ago
        planet.setPolity("Various Nations");
        planet.setStrategicImportance(10);  // Humanity's homeworld
        planet.setPrimaryResource("Diverse");
        planet.setNotes("Third planet from the Sun. Humanity's homeworld. " +
                "Only known planet with confirmed life. 71% ocean coverage. " +
                "Single large natural satellite (Moon). Moderate axial tilt creates seasons.");

        exoPlanetRepository.save(planet);
        log.info("Created Earth with full properties");
        return planet;
    }

    private ExoPlanet createMars(StarObject sol, SolarSystem solarSystem) {
        ExoPlanet planet = createBasePlanet(sol, solarSystem, "Mars");

        // Orbital parameters
        planet.setSemiMajorAxis(1.524);
        planet.setOrbitalPeriod(687.0);
        planet.setEccentricity(0.094);
        planet.setInclination(1.85);
        planet.setLongitudeOfAscendingNode(49.56);
        planet.setOmega(286.5);

        // Physical properties
        planet.setMass(0.642 / 1898.0);
        planet.setRadius(3389.5 / 71492.0);
        planet.setDensity(3.93);
        planet.setCoreRadius(1700.0);
        planet.setAxialTilt(25.19);
        planet.setDayLength(24.62);  // Sol is 24h 37m
        planet.setSurfaceGravity(0.38);
        planet.setSurfaceAcceleration(3.72);
        planet.setEscapeVelocity(5027.0);
        planet.setAlbedo(0.25);
        planet.setGeometricAlbedo(0.17);

        // Temperature
        planet.setSurfaceTemperature(210.0);  // -63°C mean
        planet.setTempCalculated(210.0);
        planet.setTempMeasured(210.0);
        planet.setHighTemperature(293.0);   // 20°C at equator summer
        planet.setLowTemperature(130.0);    // -143°C polar winter
        planet.setMaxTemperature(308.0);    // 35°C recorded
        planet.setMinTemperature(130.0);
        planet.setExosphericTemperature(200.0);
        planet.setGreenhouseRise(5.0);
        planet.setBoilingPoint(268.0);  // At 6 mbar

        // Atmosphere
        planet.setAtmosphereType("Unbreathable");
        planet.setSurfacePressure(6.36);  // 0.6% of Earth
        planet.setAtmosphereComposition("CO2:95.3;N2:2.7;Ar:1.6;O2:0.13;CO:0.07");
        planet.setMolecules("CO2, N2, Ar, O2, CO, H2O, NO");
        planet.setMinimumMolecularWeight(5.0);
        planet.setVolatileGasInventory(0.01);

        // Climate
        planet.setHydrosphere(0.0);  // No liquid water currently
        planet.setCloudCover(0.02);  // Thin clouds occasionally
        planet.setIceCover(0.15);    // Polar caps (CO2 and H2O ice)

        // Classification
        planet.setPlanetType("Martian");
        planet.setOrbitalZone(2);
        planet.setGasGiant(false);
        planet.setHabitable(false);
        planet.setEarthlike(false);
        planet.setTidallyLocked(false);
        planet.setGreenhouseEffect(false);
        planet.setHasRings(false);

        setSolStarProperties(planet);

        // Notes
        planet.setNotes("The Red Planet. Evidence of ancient water features. Olympus Mons is the largest volcano " +
                "in the solar system. Valles Marineris canyon system. Two small moons: Phobos and Deimos. " +
                "Primary target for human colonization.");

        exoPlanetRepository.save(planet);
        log.info("Created Mars with full properties");
        return planet;
    }

    private ExoPlanet createJupiter(StarObject sol, SolarSystem solarSystem) {
        ExoPlanet planet = createBasePlanet(sol, solarSystem, "Jupiter");

        // Orbital parameters
        planet.setSemiMajorAxis(5.203);
        planet.setOrbitalPeriod(4332.59);
        planet.setEccentricity(0.049);
        planet.setInclination(1.31);
        planet.setLongitudeOfAscendingNode(100.46);
        planet.setOmega(273.87);

        // Physical properties
        planet.setMass(1.0);  // Jupiter masses (by definition)
        planet.setRadius(1.0);  // Jupiter radii (by definition)
        planet.setDensity(1.33);
        planet.setAxialTilt(3.13);
        planet.setDayLength(9.93);  // Fastest rotation
        planet.setSurfaceGravity(2.53);
        planet.setSurfaceAcceleration(24.79);
        planet.setEscapeVelocity(59500.0);
        planet.setAlbedo(0.52);

        // Temperature (cloud tops at 1 bar level)
        planet.setSurfaceTemperature(165.0);  // Cloud top ~-108°C
        planet.setTempCalculated(124.0);  // Effective temperature
        planet.setTempMeasured(165.0);
        planet.setHighTemperature(165.0);
        planet.setLowTemperature(110.0);
        planet.setMaxTemperature(165.0);
        planet.setMinTemperature(110.0);
        planet.setExosphericTemperature(1000.0);  // Upper atmosphere

        // Atmosphere
        planet.setAtmosphereType("Gas Giant");
        planet.setAtmosphereComposition("H2:89.8;He:10.2;CH4:0.3;NH3:0.026;HD:0.003");
        planet.setMolecules("H2, He, CH4, NH3, H2O, C2H2, C2H4, C2H6, PH3, GeH4");
        planet.setMinimumMolecularWeight(2.0);

        // Climate
        planet.setCloudCover(1.0);  // 100% cloud bands
        planet.setHydrosphere(0.0);  // No liquid water surface
        planet.setIceCover(0.0);

        // Classification
        planet.setPlanetType("Gas Giant");
        planet.setOrbitalZone(3);
        planet.setGasGiant(true);
        planet.setHabitable(false);
        planet.setEarthlike(false);
        planet.setHabitableJovian(false);  // Not habitable itself; moons have astrobiological interest
        planet.setTidallyLocked(false);
        planet.setHasRings(true);
        planet.setRingType("CUSTOM");
        setRingParameters(planet, "Jupiter", 71492.0);

        setSolStarProperties(planet);

        // Notes
        planet.setNotes("Largest planet in the solar system. Great Red Spot storm active for 400+ years. " +
                "Faint ring system. 95 known moons including the Galilean moons (Io, Europa, Ganymede, Callisto). " +
                "Strong magnetic field and intense radiation belts.");

        exoPlanetRepository.save(planet);
        log.info("Created Jupiter with full properties");
        return planet;
    }

    private ExoPlanet createSaturn(StarObject sol, SolarSystem solarSystem) {
        ExoPlanet planet = createBasePlanet(sol, solarSystem, "Saturn");

        // Orbital parameters
        planet.setSemiMajorAxis(9.537);
        planet.setOrbitalPeriod(10759.22);
        planet.setEccentricity(0.052);
        planet.setInclination(2.49);
        planet.setLongitudeOfAscendingNode(113.64);
        planet.setOmega(339.39);

        // Physical properties
        planet.setMass(568.0 / 1898.0);
        planet.setRadius(58232.0 / 71492.0);
        planet.setDensity(0.69);  // Less dense than water!
        planet.setAxialTilt(26.73);
        planet.setDayLength(10.66);
        planet.setSurfaceGravity(1.07);
        planet.setSurfaceAcceleration(10.44);
        planet.setEscapeVelocity(35500.0);
        planet.setAlbedo(0.47);

        // Temperature (cloud tops at 1 bar level)
        planet.setSurfaceTemperature(134.0);  // Cloud top ~-139°C
        planet.setTempCalculated(95.0);  // Effective temperature
        planet.setTempMeasured(134.0);
        planet.setHighTemperature(134.0);
        planet.setLowTemperature(82.0);
        planet.setMaxTemperature(134.0);
        planet.setMinTemperature(82.0);
        planet.setExosphericTemperature(420.0);

        // Atmosphere
        planet.setAtmosphereType("Gas Giant");
        planet.setAtmosphereComposition("H2:96.3;He:3.25;CH4:0.45;NH3:0.01;HD:0.01");
        planet.setMolecules("H2, He, CH4, NH3, C2H2, C2H4, PH3, C3H8");
        planet.setMinimumMolecularWeight(2.0);

        // Climate
        planet.setCloudCover(1.0);
        planet.setHydrosphere(0.0);
        planet.setIceCover(0.0);

        // Classification
        planet.setPlanetType("Gas Giant");
        planet.setOrbitalZone(3);
        planet.setGasGiant(true);
        planet.setHabitable(false);
        planet.setHabitableJovian(false);  // Not habitable itself; Titan/Enceladus have astrobiological interest
        planet.setHasRings(true);
        planet.setRingType("SATURN");
        setRingParameters(planet, "Saturn", 58232.0);

        setSolStarProperties(planet);

        // Notes
        planet.setNotes("Known for its spectacular ring system, the most extensive in the solar system. " +
                "Less dense than water. 146 known moons including Titan (with dense atmosphere) and " +
                "Enceladus (with subsurface ocean). Hexagonal storm at north pole.");

        exoPlanetRepository.save(planet);
        log.info("Created Saturn with full properties");
        return planet;
    }

    private ExoPlanet createUranus(StarObject sol, SolarSystem solarSystem) {
        ExoPlanet planet = createBasePlanet(sol, solarSystem, "Uranus");

        // Orbital parameters
        planet.setSemiMajorAxis(19.19);
        planet.setOrbitalPeriod(30685.4);
        planet.setEccentricity(0.047);
        planet.setInclination(0.77);
        planet.setLongitudeOfAscendingNode(74.01);
        planet.setOmega(96.54);

        // Physical properties
        planet.setMass(86.8 / 1898.0);
        planet.setRadius(25362.0 / 71492.0);
        planet.setDensity(1.27);
        planet.setAxialTilt(97.77);  // Extreme tilt - rotates on its side
        planet.setDayLength(17.24);  // Retrograde
        planet.setSurfaceGravity(0.89);
        planet.setSurfaceAcceleration(8.69);
        planet.setEscapeVelocity(21300.0);
        planet.setAlbedo(0.51);

        // Temperature (cloud tops)
        planet.setSurfaceTemperature(76.0);  // Cloud top ~-197°C
        planet.setTempCalculated(58.0);  // Effective temperature
        planet.setTempMeasured(76.0);
        planet.setHighTemperature(76.0);
        planet.setLowTemperature(49.0);
        planet.setMaxTemperature(76.0);
        planet.setMinTemperature(49.0);  // Coldest atmosphere in solar system
        planet.setExosphericTemperature(800.0);

        // Atmosphere
        planet.setAtmosphereType("Ice Giant");
        planet.setAtmosphereComposition("H2:82.5;He:15.2;CH4:2.3");
        planet.setMolecules("H2, He, CH4, H2S, NH3, C2H2");
        planet.setMinimumMolecularWeight(2.0);

        // Climate
        planet.setCloudCover(1.0);
        planet.setHydrosphere(0.0);
        planet.setIceCover(0.0);

        // Classification
        planet.setPlanetType("Ice Giant");
        planet.setOrbitalZone(3);
        planet.setGasGiant(true);
        planet.setHabitable(false);
        planet.setHasRings(true);
        planet.setRingType("URANUS");
        setRingParameters(planet, "Uranus", 25362.0);

        setSolStarProperties(planet);

        // Notes
        planet.setNotes("Ice giant with extreme axial tilt (97.8°) - essentially rotates on its side. " +
                "Narrow dark ring system. 28 known moons. Discovered in 1781 by William Herschel. " +
                "Coldest planetary atmosphere in solar system.");

        exoPlanetRepository.save(planet);
        log.info("Created Uranus with full properties");
        return planet;
    }

    private ExoPlanet createNeptune(StarObject sol, SolarSystem solarSystem) {
        ExoPlanet planet = createBasePlanet(sol, solarSystem, "Neptune");

        // Orbital parameters
        planet.setSemiMajorAxis(30.07);
        planet.setOrbitalPeriod(60190.0);
        planet.setEccentricity(0.01);
        planet.setInclination(1.77);
        planet.setLongitudeOfAscendingNode(131.78);
        planet.setOmega(273.19);

        // Physical properties
        planet.setMass(102.0 / 1898.0);
        planet.setRadius(24622.0 / 71492.0);
        planet.setDensity(1.64);
        planet.setAxialTilt(28.32);
        planet.setDayLength(16.11);
        planet.setSurfaceGravity(1.14);
        planet.setSurfaceAcceleration(11.15);
        planet.setEscapeVelocity(23500.0);
        planet.setAlbedo(0.41);

        // Temperature (cloud tops)
        planet.setSurfaceTemperature(72.0);  // Cloud top ~-201°C
        planet.setTempCalculated(47.0);  // Effective temperature
        planet.setTempMeasured(72.0);
        planet.setHighTemperature(72.0);
        planet.setLowTemperature(55.0);
        planet.setMaxTemperature(72.0);
        planet.setMinTemperature(55.0);
        planet.setExosphericTemperature(750.0);

        // Atmosphere
        planet.setAtmosphereType("Ice Giant");
        planet.setAtmosphereComposition("H2:80;He:19;CH4:1.5");
        planet.setMolecules("H2, He, CH4, H2S, C2H2, C2H4, CO");
        planet.setMinimumMolecularWeight(2.0);

        // Climate
        planet.setCloudCover(1.0);
        planet.setHydrosphere(0.0);
        planet.setIceCover(0.0);

        // Classification
        planet.setPlanetType("Ice Giant");
        planet.setOrbitalZone(3);
        planet.setGasGiant(true);
        planet.setHabitable(false);
        planet.setHasRings(true);
        planet.setRingType("NEPTUNE");
        setRingParameters(planet, "Neptune", 24622.0);

        setSolStarProperties(planet);

        // Notes
        planet.setNotes("Outermost planet (since Pluto's reclassification). Strongest winds in solar system. " +
                "Faint ring arcs. 16 known moons including Triton (retrograde, likely captured Kuiper Belt object). " +
                "Discovered 1846 via mathematical prediction.");

        exoPlanetRepository.save(planet);
        log.info("Created Neptune with full properties");
        return planet;
    }

    private ExoPlanet createPluto(StarObject sol, SolarSystem solarSystem) {
        ExoPlanet planet = createBasePlanet(sol, solarSystem, "Pluto");

        // Orbital parameters
        planet.setSemiMajorAxis(39.48);
        planet.setOrbitalPeriod(90560.0);
        planet.setEccentricity(0.244);
        planet.setInclination(17.16);
        planet.setLongitudeOfAscendingNode(110.30);
        planet.setOmega(113.76);

        // Physical properties
        planet.setMass(0.0130 / 1898.0);
        planet.setRadius(1188.3 / 71492.0);
        planet.setDensity(1.86);
        planet.setCoreRadius(850.0);  // Rocky core estimate
        planet.setAxialTilt(122.5);  // Retrograde, extreme tilt
        planet.setDayLength(153.3);  // 6.39 Earth days, retrograde
        planet.setSurfaceGravity(0.063);
        planet.setSurfaceAcceleration(0.62);
        planet.setEscapeVelocity(1210.0);
        planet.setAlbedo(0.52);
        planet.setGeometricAlbedo(0.52);

        // Temperature
        planet.setSurfaceTemperature(44.0);  // ~-229°C average
        planet.setTempCalculated(44.0);
        planet.setTempMeasured(44.0);
        planet.setHighTemperature(55.0);  // At perihelion
        planet.setLowTemperature(33.0);   // At aphelion
        planet.setMaxTemperature(55.0);
        planet.setMinTemperature(33.0);
        planet.setExosphericTemperature(70.0);  // Very cold

        // Atmosphere (tenuous, varies with distance from Sun)
        planet.setAtmosphereType("Tenuous");
        planet.setSurfacePressure(0.001);  // ~1 Pa at closest approach
        planet.setAtmosphereComposition("N2:99;CH4:0.5;CO:0.05");
        planet.setMolecules("N2, CH4, CO");
        planet.setMinimumMolecularWeight(28.0);

        // Climate
        planet.setHydrosphere(0.0);
        planet.setCloudCover(0.0);
        planet.setIceCover(0.98);  // Mostly nitrogen and methane ice

        // Classification
        planet.setPlanetType("Dwarf Planet");
        planet.setOrbitalZone(3);
        planet.setGasGiant(false);
        planet.setHabitable(false);
        planet.setHasRings(false);

        setSolStarProperties(planet);

        // Notes
        planet.setNotes("Dwarf planet in the Kuiper Belt. Reclassified from planet in 2006. " +
                "Binary system with Charon (mutually tidally locked). Heart-shaped nitrogen ice plain (Sputnik Planitia). " +
                "5 known moons. Visited by New Horizons in 2015.");

        exoPlanetRepository.save(planet);
        log.info("Created Pluto with full properties");
        return planet;
    }

    /**
     * Creates a base ExoPlanet with common properties set.
     * For real Solar System planets, procedural metadata is explicitly null.
     */
    private ExoPlanet createBasePlanet(StarObject sol, SolarSystem solarSystem, String name) {
        ExoPlanet planet = new ExoPlanet();
        planet.setId(UUID.randomUUID().toString());
        planet.setName(name);
        planet.setStarName("Sol");
        planet.setSolarSystemId(solarSystem.getId());
        planet.setHostStarId(sol.getId());
        planet.setIsMoon(false);
        planet.setPlanetStatus("Confirmed");
        planet.setDetectionType("Direct");
        planet.setDiscovered(null);  // Known since antiquity

        // Explicitly set procedural metadata to null for real planets
        planet.setProceduralSource(null);
        planet.setProceduralSeed(null);
        planet.setProceduralGeneratorVersion(null);
        planet.setProceduralAccreteSnapshot(null);
        planet.setProceduralOverrides(null);

        return planet;
    }

    /**
     * Sets the host star properties for Sol on a planet.
     */
    private void setSolStarProperties(ExoPlanet planet) {
        planet.setStarSpType("G2V");
        planet.setStarMass(1.0);          // Solar masses
        planet.setStarRadius(1.0);        // Solar radii
        planet.setStarTeff(5778.0);       // Kelvin
        planet.setStarAge(4.6);           // Billion years
        planet.setStarDistance(0.0);      // We're here
        planet.setStarMetallicity(0.0);   // By definition
        planet.setRa(0.0);                // Reference point
        planet.setDec(0.0);
        planet.setMagV(-26.74);           // Apparent magnitude from Earth
    }

    private void createSolMoons(StarObject sol,
                                SolarSystem solarSystem,
                                java.util.Map<String, ExoPlanet> planetsByName) {
        log.info("Creating moons for Sol's planets...");

        // Earth's Moon
        ExoPlanet earthMoon = createBaseMoon(sol, solarSystem, planetsByName.get("Earth"), "Moon");
        earthMoon.setSemiMajorAxis(0.00257);  // 384,400 km in AU
        earthMoon.setOrbitalPeriod(27.32);
        earthMoon.setEccentricity(0.055);
        earthMoon.setInclination(5.145);
        earthMoon.setMass(0.073 / 1898.0);
        earthMoon.setRadius(1737.4 / 71492.0);
        earthMoon.setDensity(3.34);
        earthMoon.setAxialTilt(6.68);
        earthMoon.setDayLength(655.7);  // Synchronous rotation
        earthMoon.setSurfaceGravity(0.166);
        earthMoon.setSurfaceAcceleration(1.62);
        earthMoon.setEscapeVelocity(2380.0);
        earthMoon.setAlbedo(0.136);
        earthMoon.setSurfaceTemperature(250.0);  // Average
        earthMoon.setHighTemperature(400.0);
        earthMoon.setLowTemperature(100.0);
        earthMoon.setAtmosphereType("None");
        earthMoon.setSurfacePressure(0.0);
        earthMoon.setTidallyLocked(true);
        exoPlanetRepository.save(earthMoon);
        log.info("Created Moon (Earth)");

        // Mars moons
        createPhobos(sol, solarSystem, planetsByName.get("Mars"));
        createDeimos(sol, solarSystem, planetsByName.get("Mars"));

        // Jupiter's Galilean moons
        createIo(sol, solarSystem, planetsByName.get("Jupiter"));
        createEuropa(sol, solarSystem, planetsByName.get("Jupiter"));
        createGanymede(sol, solarSystem, planetsByName.get("Jupiter"));
        createCallisto(sol, solarSystem, planetsByName.get("Jupiter"));

        // Saturn's major moons
        createTitan(sol, solarSystem, planetsByName.get("Saturn"));
        createEnceladus(sol, solarSystem, planetsByName.get("Saturn"));

        // Uranus moons
        createTitania(sol, solarSystem, planetsByName.get("Uranus"));
        createOberon(sol, solarSystem, planetsByName.get("Uranus"));

        // Neptune's Triton
        createTriton(sol, solarSystem, planetsByName.get("Neptune"));

        // Pluto's Charon
        createCharon(sol, solarSystem, planetsByName.get("Pluto"));
    }

    private ExoPlanet createBaseMoon(StarObject sol, SolarSystem solarSystem, ExoPlanet parent, String name) {
        ExoPlanet moon = new ExoPlanet();
        moon.setId(UUID.randomUUID().toString());
        moon.setName(name);
        moon.setStarName("Sol");
        moon.setSolarSystemId(solarSystem.getId());
        moon.setHostStarId(sol.getId());
        moon.setParentPlanetId(parent.getId());
        moon.setIsMoon(true);
        moon.setPlanetStatus("Confirmed");
        moon.setPlanetType("Moon");
        moon.setGasGiant(false);
        moon.setDetectionType("Direct");
        moon.setHasRings(false);
        setSolStarProperties(moon);
        return moon;
    }

    private void createPhobos(StarObject sol, SolarSystem solarSystem, ExoPlanet mars) {
        ExoPlanet moon = createBaseMoon(sol, solarSystem, mars, "Phobos");
        moon.setSemiMajorAxis(9376.0 / KM_TO_AU);
        moon.setOrbitalPeriod(0.319);
        moon.setEccentricity(0.015);
        moon.setInclination(1.08);
        moon.setMass(1.07e-8 / 1898.0);
        moon.setRadius(11.2 / 71492.0);
        moon.setDensity(1.87);
        moon.setSurfaceGravity(0.00057);
        moon.setAlbedo(0.071);
        moon.setSurfaceTemperature(233.0);
        moon.setAtmosphereType("None");
        moon.setTidallyLocked(true);
        moon.setNotes("Irregularly shaped, likely a captured asteroid. Gradually spiraling inward.");
        exoPlanetRepository.save(moon);
        log.info("Created Phobos");
    }

    private void createDeimos(StarObject sol, SolarSystem solarSystem, ExoPlanet mars) {
        ExoPlanet moon = createBaseMoon(sol, solarSystem, mars, "Deimos");
        moon.setSemiMajorAxis(23463.0 / KM_TO_AU);
        moon.setOrbitalPeriod(1.263);
        moon.setEccentricity(0.0002);
        moon.setInclination(1.79);
        moon.setMass(1.48e-9 / 1898.0);
        moon.setRadius(6.2 / 71492.0);
        moon.setDensity(1.47);
        moon.setSurfaceGravity(0.00030);
        moon.setAlbedo(0.068);
        moon.setSurfaceTemperature(233.0);
        moon.setAtmosphereType("None");
        moon.setTidallyLocked(true);
        moon.setNotes("Smaller and more distant of Mars's moons. Smooth surface.");
        exoPlanetRepository.save(moon);
        log.info("Created Deimos");
    }

    private void createIo(StarObject sol, SolarSystem solarSystem, ExoPlanet jupiter) {
        ExoPlanet moon = createBaseMoon(sol, solarSystem, jupiter, "Io");
        moon.setSemiMajorAxis(421700.0 / KM_TO_AU);
        moon.setOrbitalPeriod(1.769);
        moon.setEccentricity(0.0041);
        moon.setInclination(0.04);
        moon.setMass(0.0893 / 1898.0);
        moon.setRadius(1821.6 / 71492.0);
        moon.setDensity(3.53);
        moon.setSurfaceGravity(0.183);
        moon.setSurfaceAcceleration(1.796);
        moon.setEscapeVelocity(2558.0);
        moon.setAlbedo(0.63);
        moon.setSurfaceTemperature(130.0);
        moon.setHighTemperature(2000.0);  // Volcanic hotspots
        moon.setLowTemperature(90.0);
        moon.setAtmosphereType("Tenuous");
        moon.setSurfacePressure(0.0000003);  // SO2 atmosphere
        moon.setAtmosphereComposition("SO2:90;SO:5;S:3;O:1;NaCl:1");
        moon.setMolecules("SO2, SO, S, O, NaCl, S2");
        moon.setTidallyLocked(true);
        moon.setNotes("Most volcanically active body in the solar system. Over 400 active volcanoes.");
        exoPlanetRepository.save(moon);
        log.info("Created Io");
    }

    private void createEuropa(StarObject sol, SolarSystem solarSystem, ExoPlanet jupiter) {
        ExoPlanet moon = createBaseMoon(sol, solarSystem, jupiter, "Europa");
        moon.setSemiMajorAxis(671100.0 / KM_TO_AU);
        moon.setOrbitalPeriod(3.551);
        moon.setEccentricity(0.0094);
        moon.setInclination(0.47);
        moon.setMass(0.0480 / 1898.0);
        moon.setRadius(1560.8 / 71492.0);
        moon.setDensity(3.01);
        moon.setSurfaceGravity(0.134);
        moon.setSurfaceAcceleration(1.314);
        moon.setEscapeVelocity(2025.0);
        moon.setAlbedo(0.67);  // Very reflective ice
        moon.setSurfaceTemperature(102.0);
        moon.setHighTemperature(132.0);
        moon.setLowTemperature(50.0);
        moon.setAtmosphereType("Tenuous");
        moon.setSurfacePressure(0.0000001);
        moon.setAtmosphereComposition("O2:100");
        moon.setMolecules("O2, H2O, H2O2");
        moon.setIceCover(1.0);  // 100% ice surface
        moon.setHydrosphere(1.0);  // Subsurface ocean
        moon.setTidallyLocked(true);
        moon.setHabitable(false);  // Not habitable, but has astrobiological potential (subsurface ocean)
        moon.setNotes("Smooth ice surface with subsurface ocean. Prime target for astrobiology.");
        exoPlanetRepository.save(moon);
        log.info("Created Europa");
    }

    private void createGanymede(StarObject sol, SolarSystem solarSystem, ExoPlanet jupiter) {
        ExoPlanet moon = createBaseMoon(sol, solarSystem, jupiter, "Ganymede");
        moon.setSemiMajorAxis(1070400.0 / KM_TO_AU);
        moon.setOrbitalPeriod(7.155);
        moon.setEccentricity(0.0013);
        moon.setInclination(0.18);
        moon.setMass(0.1480 / 1898.0);
        moon.setRadius(2634.1 / 71492.0);
        moon.setDensity(1.94);
        moon.setSurfaceGravity(0.146);
        moon.setSurfaceAcceleration(1.428);
        moon.setEscapeVelocity(2741.0);
        moon.setAlbedo(0.43);
        moon.setSurfaceTemperature(110.0);
        moon.setHighTemperature(152.0);
        moon.setLowTemperature(70.0);
        moon.setAtmosphereType("Tenuous");
        moon.setSurfacePressure(0.0000001);
        moon.setAtmosphereComposition("O2:100");
        moon.setMolecules("O2, O3");
        moon.setTidallyLocked(true);
        moon.setNotes("Largest moon in solar system. Has its own magnetic field and subsurface ocean.");
        exoPlanetRepository.save(moon);
        log.info("Created Ganymede");
    }

    private void createCallisto(StarObject sol, SolarSystem solarSystem, ExoPlanet jupiter) {
        ExoPlanet moon = createBaseMoon(sol, solarSystem, jupiter, "Callisto");
        moon.setSemiMajorAxis(1882700.0 / KM_TO_AU);
        moon.setOrbitalPeriod(16.689);
        moon.setEccentricity(0.0074);
        moon.setInclination(0.28);
        moon.setMass(0.1080 / 1898.0);
        moon.setRadius(2410.3 / 71492.0);
        moon.setDensity(1.83);
        moon.setSurfaceGravity(0.126);
        moon.setSurfaceAcceleration(1.235);
        moon.setEscapeVelocity(2440.0);
        moon.setAlbedo(0.22);
        moon.setSurfaceTemperature(134.0);
        moon.setHighTemperature(165.0);
        moon.setLowTemperature(80.0);
        moon.setAtmosphereType("Tenuous");
        moon.setSurfacePressure(0.0000001);
        moon.setAtmosphereComposition("CO2:100");
        moon.setMolecules("CO2, O2");
        moon.setTidallyLocked(true);
        moon.setNotes("Most heavily cratered object in solar system. Possible subsurface ocean.");
        exoPlanetRepository.save(moon);
        log.info("Created Callisto");
    }

    private void createTitan(StarObject sol, SolarSystem solarSystem, ExoPlanet saturn) {
        ExoPlanet moon = createBaseMoon(sol, solarSystem, saturn, "Titan");
        moon.setSemiMajorAxis(1221870.0 / KM_TO_AU);
        moon.setOrbitalPeriod(15.945);
        moon.setEccentricity(0.0288);
        moon.setInclination(0.33);
        moon.setMass(0.1345 / 1898.0);
        moon.setRadius(2574.7 / 71492.0);
        moon.setDensity(1.88);
        moon.setSurfaceGravity(0.138);
        moon.setSurfaceAcceleration(1.352);
        moon.setEscapeVelocity(2639.0);
        moon.setAlbedo(0.22);
        moon.setSurfaceTemperature(94.0);  // -179°C
        moon.setHighTemperature(94.0);
        moon.setLowTemperature(94.0);  // Very uniform due to thick atmosphere

        // Titan has a thick atmosphere!
        moon.setAtmosphereType("Unbreathable");
        moon.setSurfacePressure(1467.0);  // 1.45 atm, thicker than Earth!
        moon.setAtmosphereComposition("N2:98.4;CH4:1.4;H2:0.2");
        moon.setMolecules("N2, CH4, C2H2, C2H4, C2H6, HCN, C3H8, C4H2");
        moon.setMinimumMolecularWeight(2.0);
        moon.setVolatileGasInventory(10.0);
        moon.setCloudCover(0.7);  // Methane clouds

        moon.setHydrosphere(0.02);  // Methane lakes and seas
        moon.setIceCover(0.0);  // Water ice is bedrock

        moon.setTidallyLocked(true);
        moon.setHabitable(false);  // Not habitable, but has prebiotic chemistry interest
        moon.setNotes("Only moon with a dense atmosphere. Has methane lakes and rivers. Prebiotic chemistry.");
        exoPlanetRepository.save(moon);
        log.info("Created Titan");
    }

    private void createEnceladus(StarObject sol, SolarSystem solarSystem, ExoPlanet saturn) {
        ExoPlanet moon = createBaseMoon(sol, solarSystem, saturn, "Enceladus");
        moon.setSemiMajorAxis(238020.0 / KM_TO_AU);
        moon.setOrbitalPeriod(1.370);
        moon.setEccentricity(0.0047);
        moon.setInclination(0.02);
        moon.setMass(0.000108 / 1898.0);
        moon.setRadius(252.1 / 71492.0);
        moon.setDensity(1.61);
        moon.setSurfaceGravity(0.0113);
        moon.setSurfaceAcceleration(0.111);
        moon.setEscapeVelocity(239.0);
        moon.setAlbedo(0.99);  // Most reflective body in solar system!
        moon.setSurfaceTemperature(75.0);
        moon.setHighTemperature(145.0);  // Near tiger stripes
        moon.setLowTemperature(33.0);
        moon.setAtmosphereType("Tenuous");
        moon.setSurfacePressure(0.0);
        moon.setAtmosphereComposition("H2O:91;N2:4;CO2:3.2;CH4:1.7");
        moon.setMolecules("H2O, N2, CO2, CH4, NH3, H2S, C2H2");
        moon.setIceCover(1.0);
        moon.setHydrosphere(1.0);  // Global subsurface ocean
        moon.setTidallyLocked(true);
        moon.setHabitable(false);  // Not habitable, but has astrobiological potential (hydrothermal vents)
        moon.setNotes("Active water geysers from south pole. Global subsurface ocean with potential habitability.");
        exoPlanetRepository.save(moon);
        log.info("Created Enceladus");
    }

    private void createTitania(StarObject sol, SolarSystem solarSystem, ExoPlanet uranus) {
        ExoPlanet moon = createBaseMoon(sol, solarSystem, uranus, "Titania");
        moon.setSemiMajorAxis(435910.0 / KM_TO_AU);
        moon.setOrbitalPeriod(8.706);
        moon.setEccentricity(0.0011);
        moon.setInclination(0.08);
        moon.setMass(0.0035 / 1898.0);
        moon.setRadius(788.4 / 71492.0);
        moon.setDensity(1.71);
        moon.setSurfaceGravity(0.039);
        moon.setAlbedo(0.35);
        moon.setSurfaceTemperature(70.0);
        moon.setAtmosphereType("None");
        moon.setTidallyLocked(true);
        moon.setNotes("Largest moon of Uranus. Rift valleys suggest past geological activity.");
        exoPlanetRepository.save(moon);
        log.info("Created Titania");
    }

    private void createOberon(StarObject sol, SolarSystem solarSystem, ExoPlanet uranus) {
        ExoPlanet moon = createBaseMoon(sol, solarSystem, uranus, "Oberon");
        moon.setSemiMajorAxis(583520.0 / KM_TO_AU);
        moon.setOrbitalPeriod(13.463);
        moon.setEccentricity(0.0014);
        moon.setInclination(0.07);
        moon.setMass(0.0030 / 1898.0);
        moon.setRadius(761.4 / 71492.0);
        moon.setDensity(1.63);
        moon.setSurfaceGravity(0.035);
        moon.setAlbedo(0.31);
        moon.setSurfaceTemperature(75.0);
        moon.setAtmosphereType("None");
        moon.setTidallyLocked(true);
        moon.setNotes("Second largest moon of Uranus. Heavily cratered ancient surface.");
        exoPlanetRepository.save(moon);
        log.info("Created Oberon");
    }

    private void createTriton(StarObject sol, SolarSystem solarSystem, ExoPlanet neptune) {
        ExoPlanet moon = createBaseMoon(sol, solarSystem, neptune, "Triton");
        moon.setSemiMajorAxis(354800.0 / KM_TO_AU);
        moon.setOrbitalPeriod(5.877);
        moon.setEccentricity(0.00002);
        moon.setInclination(156.9);  // Retrograde orbit!
        moon.setMass(0.0214 / 1898.0);
        moon.setRadius(1353.4 / 71492.0);
        moon.setDensity(2.06);
        moon.setSurfaceGravity(0.0794);
        moon.setSurfaceAcceleration(0.779);
        moon.setEscapeVelocity(1455.0);
        moon.setAlbedo(0.76);
        moon.setSurfaceTemperature(38.0);  // Coldest known surface
        moon.setHighTemperature(38.0);
        moon.setLowTemperature(38.0);
        moon.setAtmosphereType("Tenuous");
        moon.setSurfacePressure(0.014);  // Very thin
        moon.setAtmosphereComposition("N2:99.9;CH4:0.01");
        moon.setMolecules("N2, CH4, CO, CO2");
        moon.setIceCover(0.55);  // Nitrogen ice cap
        moon.setTidallyLocked(true);
        moon.setNotes("Retrograde orbit indicates captured Kuiper Belt object. Active nitrogen geysers.");
        exoPlanetRepository.save(moon);
        log.info("Created Triton");
    }

    private void createCharon(StarObject sol, SolarSystem solarSystem, ExoPlanet pluto) {
        ExoPlanet moon = createBaseMoon(sol, solarSystem, pluto, "Charon");
        moon.setSemiMajorAxis(19591.0 / KM_TO_AU);
        moon.setOrbitalPeriod(6.387);
        moon.setEccentricity(0.0002);
        moon.setInclination(0.0);  // Relative to Pluto's equator
        moon.setMass(0.001586 / 1898.0);
        moon.setRadius(606.0 / 71492.0);
        moon.setDensity(1.70);
        moon.setSurfaceGravity(0.029);
        moon.setSurfaceAcceleration(0.288);
        moon.setEscapeVelocity(580.0);
        moon.setAlbedo(0.37);
        moon.setSurfaceTemperature(53.0);
        moon.setHighTemperature(53.0);
        moon.setLowTemperature(53.0);
        moon.setAtmosphereType("None");
        moon.setIceCover(0.4);  // Water ice visible
        moon.setTidallyLocked(true);
        moon.setNotes("Mutually tidally locked with Pluto (double dwarf planet). Red polar cap of tholins.");
        exoPlanetRepository.save(moon);
        log.info("Created Charon");
    }
}
