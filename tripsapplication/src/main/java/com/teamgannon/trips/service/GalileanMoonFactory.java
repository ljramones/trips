package com.teamgannon.trips.service;

import com.teamgannon.trips.jpa.model.ExoPlanet;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Factory for creating the four Galilean moons of Jupiter with accurate real-world data.
 * The Galilean moons are Io, Europa, Ganymede, and Callisto.
 */
@Slf4j
public class GalileanMoonFactory {

    /**
     * Galilean moon orbital and physical data.
     * All values use standard astronomical units:
     * - Semi-major axis in AU (converted from km: divide by 149,597,870.7)
     * - Radius in Earth radii (Earth radius = 6371 km)
     * - Mass in Earth masses (Earth mass = 5.972e24 kg)
     * - Orbital period in days
     * - Eccentricity (dimensionless)
     */
    private static final MoonData[] GALILEAN_MOONS = {
            // Io: 421,700 km from Jupiter
            new MoonData("Io", 0.002819, 0.286, 0.015, 1.769, 0.0041),
            // Europa: 670,900 km from Jupiter
            new MoonData("Europa", 0.004485, 0.245, 0.008, 3.551, 0.0094),
            // Ganymede: 1,070,400 km from Jupiter (largest moon in solar system)
            new MoonData("Ganymede", 0.007155, 0.413, 0.025, 7.155, 0.0011),
            // Callisto: 1,882,700 km from Jupiter
            new MoonData("Callisto", 0.012585, 0.378, 0.018, 16.689, 0.0074)
    };

    private GalileanMoonFactory() {
        // Utility class
    }

    /**
     * Create ExoPlanet entities for the Galilean moons.
     * Filters out any moons that already exist based on name.
     *
     * @param jupiterPlanet  the Jupiter ExoPlanet entity
     * @param existingNames  list of existing moon names (lowercase) to skip
     * @return list of new ExoPlanet entities (not yet persisted)
     */
    public static List<ExoPlanet> createMoons(ExoPlanet jupiterPlanet, List<String> existingNames) {
        if (jupiterPlanet == null) {
            log.warn("Cannot create Galilean moons - Jupiter planet is null");
            return List.of();
        }

        List<ExoPlanet> moons = new ArrayList<>();
        String jupiterId = jupiterPlanet.getId();
        String solarSystemId = jupiterPlanet.getSolarSystemId();

        for (MoonData data : GALILEAN_MOONS) {
            if (existingNames.contains(data.name.toLowerCase())) {
                log.debug("Moon {} already exists for Jupiter", data.name);
                continue;
            }

            ExoPlanet moon = createMoon(data, jupiterId, solarSystemId,
                    jupiterPlanet.getHostStarId(), jupiterPlanet.getStarName());
            moons.add(moon);

            log.info("Created Galilean moon: {} (SMA={} AU, radius={} Earth radii)",
                    data.name, data.semiMajorAxisAU, data.radiusEarthRadii);
        }

        return moons;
    }

    /**
     * Create a single moon ExoPlanet entity from moon data.
     */
    private static ExoPlanet createMoon(MoonData data, String parentPlanetId,
                                        String solarSystemId, String hostStarId,
                                        String starName) {
        ExoPlanet moon = new ExoPlanet();
        moon.setId(UUID.randomUUID().toString());
        moon.setName(data.name);
        moon.setSolarSystemId(solarSystemId);
        moon.setHostStarId(hostStarId);
        moon.setParentPlanetId(parentPlanetId);
        moon.setIsMoon(true);

        moon.setSemiMajorAxis(data.semiMajorAxisAU);
        moon.setRadius(data.radiusEarthRadii);
        moon.setMass(data.massEarthMasses);
        moon.setOrbitalPeriod(data.orbitalPeriodDays);
        moon.setEccentricity(data.eccentricity);

        // Galilean moons have very low inclinations (nearly coplanar with Jupiter's equator)
        moon.setInclination(0.0);
        moon.setOmega(0.0);  // Argument of periapsis

        moon.setStarName(starName);
        moon.setPlanetStatus("Confirmed");
        moon.setDetectionType("Known");

        return moon;
    }

    /**
     * Get the names of all Galilean moons.
     *
     * @return array of moon names
     */
    public static String[] getMoonNames() {
        String[] names = new String[GALILEAN_MOONS.length];
        for (int i = 0; i < GALILEAN_MOONS.length; i++) {
            names[i] = GALILEAN_MOONS[i].name;
        }
        return names;
    }

    /**
     * Internal data class for moon parameters.
     */
    private record MoonData(
            String name,
            double semiMajorAxisAU,
            double radiusEarthRadii,
            double massEarthMasses,
            double orbitalPeriodDays,
            double eccentricity
    ) {}
}
