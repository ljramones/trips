package com.teamgannon.trips.service;

import com.teamgannon.trips.jpa.model.SolarSystemFeature;
import com.teamgannon.trips.planetarymodelling.FeatureDescription;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts SolarSystemFeature JPA entities to FeatureDescription display objects.
 * Used for rendering features in the solar system visualization.
 */
public class FeatureDescriptionConverter {

    private FeatureDescriptionConverter() {
        // Utility class
    }

    /**
     * Convert a list of SolarSystemFeature entities to FeatureDescription display objects.
     *
     * @param features the list of SolarSystemFeature entities
     * @return list of FeatureDescription objects for rendering
     */
    public static List<FeatureDescription> convert(List<SolarSystemFeature> features) {
        List<FeatureDescription> descriptions = new ArrayList<>();
        for (SolarSystemFeature feature : features) {
            descriptions.add(convert(feature));
        }
        return descriptions;
    }

    /**
     * Convert a single SolarSystemFeature to FeatureDescription.
     *
     * @param feature the SolarSystemFeature entity
     * @return the FeatureDescription for rendering
     */
    public static FeatureDescription convert(SolarSystemFeature feature) {
        FeatureDescription desc = new FeatureDescription();

        desc.setId(feature.getId());
        desc.setName(feature.getName());

        // Type classification
        desc.setFeatureType(feature.getFeatureType());
        desc.setFeatureCategory(feature.getFeatureCategory());

        // Belt/disk spatial properties
        if (feature.getInnerRadiusAU() != null) {
            desc.setInnerRadiusAU(feature.getInnerRadiusAU());
        }
        if (feature.getOuterRadiusAU() != null) {
            desc.setOuterRadiusAU(feature.getOuterRadiusAU());
        }
        if (feature.getThickness() != null) {
            desc.setThickness(feature.getThickness());
        }
        if (feature.getInclinationDeg() != null) {
            desc.setInclinationDeg(feature.getInclinationDeg());
        }
        if (feature.getEccentricity() != null) {
            desc.setEccentricity(feature.getEccentricity());
        }

        // Point feature spatial properties
        if (feature.getOrbitalRadiusAU() != null) {
            desc.setOrbitalRadiusAU(feature.getOrbitalRadiusAU());
        }
        if (feature.getOrbitalAngleDeg() != null) {
            desc.setOrbitalAngleDeg(feature.getOrbitalAngleDeg());
        }
        if (feature.getOrbitalHeightAU() != null) {
            desc.setOrbitalHeightAU(feature.getOrbitalHeightAU());
        }
        desc.setAssociatedPlanetId(feature.getAssociatedPlanetId());
        desc.setLagrangePoint(feature.getLagrangePoint());

        // Visual properties
        if (feature.getParticleCount() != null) {
            desc.setParticleCount(feature.getParticleCount());
        }
        if (feature.getMinParticleSize() != null) {
            desc.setMinParticleSize(feature.getMinParticleSize());
        }
        if (feature.getMaxParticleSize() != null) {
            desc.setMaxParticleSize(feature.getMaxParticleSize());
        }
        desc.setPrimaryColor(feature.getPrimaryColor());
        desc.setSecondaryColor(feature.getSecondaryColor());
        if (feature.getOpacity() != null) {
            desc.setOpacity(feature.getOpacity());
        }
        desc.setAnimated(Boolean.TRUE.equals(feature.getAnimated()));
        if (feature.getAnimationSpeed() != null) {
            desc.setAnimationSpeed(feature.getAnimationSpeed());
        }

        // Sci-fi properties
        desc.setControllingPolity(feature.getControllingPolity());
        if (feature.getPopulation() != null) {
            desc.setPopulation(feature.getPopulation());
        }
        desc.setPurpose(feature.getPurpose());
        if (feature.getTechLevel() != null) {
            desc.setTechLevel(feature.getTechLevel());
        }
        if (feature.getYearEstablished() != null) {
            desc.setYearEstablished(feature.getYearEstablished());
        }
        desc.setStatus(feature.getStatus());
        if (feature.getStrategicImportance() != null) {
            desc.setStrategicImportance(feature.getStrategicImportance());
        }
        if (feature.getDefensiveRating() != null) {
            desc.setDefensiveRating(feature.getDefensiveRating());
        }
        desc.setProductionCapacity(feature.getProductionCapacity());
        desc.setTransitDestinations(feature.getTransitDestinations());
        desc.setNotes(feature.getNotes());

        // Hazard properties
        desc.setNavigationHazard(Boolean.TRUE.equals(feature.getNavigationHazard()));
        desc.setHazardType(feature.getHazardType());
        if (feature.getHazardSeverity() != null) {
            desc.setHazardSeverity(feature.getHazardSeverity());
        }

        return desc;
    }
}
