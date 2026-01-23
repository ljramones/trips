package com.teamgannon.trips.service;

import com.teamgannon.trips.jpa.model.ExoPlanet;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.solarsysmodelling.accrete.Planet;

import java.util.UUID;

/**
 * Converts ACCRETE simulation Planet objects to ExoPlanet JPA entities.
 * Handles all the unit conversions and field mappings needed to persist
 * simulated planet data.
 */
public class AccretePlanetConverter {

    // Earth radius in km for unit conversion
    private static final double EARTH_RADIUS_KM = 6371.0;

    // Seconds per day for orbital period conversion
    private static final double SECONDS_PER_DAY = 24.0 * 3600.0;

    // Seconds per hour for day length conversion
    private static final double SECONDS_PER_HOUR = 3600.0;

    // Earth surface gravity in cm/s² for log g calculation
    private static final double EARTH_SURFACE_GRAVITY_CMS2 = 980.665;

    private AccretePlanetConverter() {
        // Utility class
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
     * @return the ExoPlanet entity (not yet persisted)
     */
    public static ExoPlanet convert(Planet planet,
                                    StarObject hostStar,
                                    String solarSystemId,
                                    int index,
                                    String parentPlanetId,
                                    boolean isMoon) {
        ExoPlanet exoPlanet = new ExoPlanet();
        exoPlanet.setId(UUID.randomUUID().toString());

        // Generate planet name using standard convention (star name + letter)
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

        // Populate orbital parameters
        populateOrbitalParameters(exoPlanet, planet);

        // Populate physical properties
        populatePhysicalProperties(exoPlanet, planet);

        // Populate climate properties
        populateClimateProperties(exoPlanet, planet);

        // Populate temperature properties
        populateTemperatureProperties(exoPlanet, planet);

        // Populate atmospheric properties
        populateAtmosphericProperties(exoPlanet, planet);

        // Populate host star properties
        populateHostStarProperties(exoPlanet, hostStar);

        return exoPlanet;
    }

    private static void populateOrbitalParameters(ExoPlanet exoPlanet, Planet planet) {
        exoPlanet.setSemiMajorAxis(planet.getSma());
        exoPlanet.setEccentricity(planet.getEccentricity());
        exoPlanet.setInclination(planet.getInclination());

        // Orbital period: convert from seconds to days
        double orbitalPeriodDays = planet.getOrbitalPeriod() / SECONDS_PER_DAY;
        exoPlanet.setOrbitalPeriod(orbitalPeriodDays);
    }

    private static void populatePhysicalProperties(ExoPlanet exoPlanet, Planet planet) {
        // Mass: convert from solar masses to Earth masses
        exoPlanet.setMass(planet.massInEarthMasses());

        // Radius: convert from km to Earth radii
        exoPlanet.setRadius(planet.getRadius() / EARTH_RADIUS_KM);

        // Planet type and classification
        exoPlanet.setPlanetType(planet.planetType());
        exoPlanet.setOrbitalZone(planet.getOrbitalZone());

        // Habitability flags
        exoPlanet.setHabitable(planet.isHabitable());
        exoPlanet.setEarthlike(planet.isEarthlike());
        exoPlanet.setGasGiant(planet.isGasGiant());
        exoPlanet.setHabitableJovian(planet.isHabitableJovian());
        exoPlanet.setHabitableMoon(planet.isHabitableMoon());
        exoPlanet.setGreenhouseEffect(planet.isGreenhouseEffect());
        exoPlanet.setTidallyLocked(planet.isResonantPeriod());

        // Extended physical properties
        exoPlanet.setDensity(planet.getDensity());
        exoPlanet.setCoreRadius(planet.getCoreRadius());
        exoPlanet.setAxialTilt(planet.getAxialTilt());

        // Day length: convert from seconds to hours
        if (isValidValue(planet.getDayLength())) {
            exoPlanet.setDayLength(planet.getDayLength() / SECONDS_PER_HOUR);
        }

        // Surface gravity (in Earth gravities)
        if (isValidValue(planet.getSurfaceGravity())) {
            exoPlanet.setSurfaceGravity(planet.getSurfaceGravity());
            // Also set log g for catalog compatibility
            double surfaceGravityCmS2 = planet.getSurfaceGravity() * EARTH_SURFACE_GRAVITY_CMS2;
            exoPlanet.setLogG(Math.log10(surfaceGravityCmS2));
        }

        exoPlanet.setSurfaceAcceleration(planet.getSurfaceAcceleration());
        exoPlanet.setEscapeVelocity(planet.getEscapeVelocity());
    }

    private static void populateClimateProperties(ExoPlanet exoPlanet, Planet planet) {
        exoPlanet.setHydrosphere(planet.getHydrosphere());
        exoPlanet.setCloudCover(planet.getCloudCover());
        exoPlanet.setIceCover(planet.getIceCover());
        exoPlanet.setAlbedo(planet.getAlbedo());

        if (isValidValue(planet.getSurfacePressure())) {
            exoPlanet.setSurfacePressure(planet.getSurfacePressure());
        }

        if (isValidValue(planet.getVolatileGasInventory())) {
            exoPlanet.setVolatileGasInventory(planet.getVolatileGasInventory());
        }
    }

    private static void populateTemperatureProperties(ExoPlanet exoPlanet, Planet planet) {
        if (isValidValue(planet.getSurfaceTemperature())) {
            exoPlanet.setSurfaceTemperature(planet.getSurfaceTemperature());
            exoPlanet.setTempCalculated(planet.getSurfaceTemperature());
        } else if (planet.getEstimatedTemperature() > 0) {
            exoPlanet.setTempCalculated(planet.getEstimatedTemperature());
        }

        if (isValidValue(planet.getHighTemperature())) {
            exoPlanet.setHighTemperature(planet.getHighTemperature());
        }
        if (isValidValue(planet.getLowTemperature())) {
            exoPlanet.setLowTemperature(planet.getLowTemperature());
        }
        if (isValidValue(planet.getMaxTemperature())) {
            exoPlanet.setMaxTemperature(planet.getMaxTemperature());
        }
        if (isValidValue(planet.getMinTemperature())) {
            exoPlanet.setMinTemperature(planet.getMinTemperature());
        }
        if (isValidValue(planet.getBoilingPoint())) {
            exoPlanet.setBoilingPoint(planet.getBoilingPoint());
        }
        if (isValidValue(planet.getExosphericTemperature())) {
            exoPlanet.setExosphericTemperature(planet.getExosphericTemperature());
        }
        exoPlanet.setGreenhouseRise(planet.getGreenhouseRise());
    }

    private static void populateAtmosphericProperties(ExoPlanet exoPlanet, Planet planet) {
        if (isValidValue(planet.getMinimumMolecularWeight())) {
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
    }

    private static void populateHostStarProperties(ExoPlanet exoPlanet, StarObject hostStar) {
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
    }

    /**
     * Check if a value is valid (positive and not MAX_VALUE).
     */
    private static boolean isValidValue(double value) {
        return value > 0 && value < Double.MAX_VALUE;
    }

    /**
     * Get the planet letter designation (b, c, d, ..., z, aa, ab, ...).
     * Uses lowercase letters starting from 'b' (as is convention for exoplanets,
     * where 'a' is reserved for the star).
     *
     * @param planetIndex the 1-based planet index
     * @return the letter designation
     */
    public static String getPlanetLetter(int planetIndex) {
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

    /**
     * Convert an integer to Roman numerals for moon naming.
     *
     * @param number the number to convert (1-20)
     * @return the Roman numeral string
     */
    public static String toRomanNumeral(int number) {
        if (number <= 0 || number > 20) {
            return String.valueOf(number);
        }
        String[] romanNumerals = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X",
                "XI", "XII", "XIII", "XIV", "XV", "XVI", "XVII", "XVIII", "XIX", "XX"};
        return romanNumerals[number - 1];
    }
}
