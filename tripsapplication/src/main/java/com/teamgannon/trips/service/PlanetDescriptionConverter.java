package com.teamgannon.trips.service;

import com.teamgannon.trips.jpa.model.ExoPlanet;
import com.teamgannon.trips.planetarymodelling.PlanetDescription;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts ExoPlanet JPA entities to PlanetDescription display objects.
 * Used for rendering planets in the solar system visualization.
 */
public class PlanetDescriptionConverter {

    private PlanetDescriptionConverter() {
        // Utility class
    }

    /**
     * Convert a list of ExoPlanet entities to PlanetDescription display objects.
     *
     * @param exoPlanets the list of ExoPlanet entities
     * @return list of PlanetDescription objects for rendering
     */
    public static List<PlanetDescription> convert(List<ExoPlanet> exoPlanets) {
        List<PlanetDescription> descriptions = new ArrayList<>();
        for (ExoPlanet exoPlanet : exoPlanets) {
            descriptions.add(convert(exoPlanet));
        }
        return descriptions;
    }

    /**
     * Convert a single ExoPlanet to PlanetDescription.
     *
     * @param exoPlanet the ExoPlanet entity
     * @return the PlanetDescription for rendering
     */
    public static PlanetDescription convert(ExoPlanet exoPlanet) {
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
}
