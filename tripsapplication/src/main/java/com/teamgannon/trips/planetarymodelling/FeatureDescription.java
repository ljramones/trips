package com.teamgannon.trips.planetarymodelling;

import lombok.Data;

/**
 * Description of a solar system feature for rendering purposes.
 * Used to pass feature data from the persistence layer to the renderer.
 */
@Data
public class FeatureDescription {

    private String id;
    private String name;

    // Type classification
    private String featureType;
    private String featureCategory;

    // Belt/disk spatial properties
    private double innerRadiusAU;
    private double outerRadiusAU;
    private double thickness;
    private double inclinationDeg;
    private double eccentricity;

    // Point feature spatial properties
    private double orbitalRadiusAU;
    private double orbitalAngleDeg;
    private double orbitalHeightAU;
    private String associatedPlanetId;
    private String lagrangePoint;

    // Visual properties
    private int particleCount;
    private double minParticleSize;
    private double maxParticleSize;
    private String primaryColor;
    private String secondaryColor;
    private double opacity;
    private boolean animated;
    private double animationSpeed;

    // Sci-fi properties
    private String controllingPolity;
    private long population;
    private String purpose;
    private int techLevel;
    private int yearEstablished;
    private String status;
    private int strategicImportance;
    private int defensiveRating;
    private String productionCapacity;
    private String transitDestinations;
    private String notes;

    // Hazard properties
    private boolean navigationHazard;
    private String hazardType;
    private int hazardSeverity;

    /**
     * Check if this is a belt/disk type feature (rendered with particles)
     */
    public boolean isBeltType() {
        return featureType != null && (
                featureType.equals("ASTEROID_BELT") ||
                featureType.equals("KUIPER_BELT") ||
                featureType.equals("DEBRIS_DISK") ||
                featureType.equals("OORT_CLOUD") ||
                featureType.equals("ZODIACAL_DUST") ||
                featureType.equals("DYSON_SWARM") ||
                featureType.equals("DEFENSE_PERIMETER") ||
                featureType.equals("SENSOR_NETWORK")
        );
    }

    /**
     * Check if this is a point/localized feature
     */
    public boolean isPointType() {
        return featureType != null && (
                featureType.equals("ORBITAL_HABITAT") ||
                featureType.equals("JUMP_GATE") ||
                featureType.equals("SHIPYARD") ||
                featureType.equals("RESEARCH_STATION") ||
                featureType.equals("MINING_OPERATION") ||
                featureType.equals("TROJAN_CLUSTER")
        );
    }

    /**
     * Check if this is a natural feature
     */
    public boolean isNatural() {
        return "NATURAL".equals(featureCategory);
    }

    /**
     * Check if this is an artificial feature
     */
    public boolean isArtificial() {
        return "ARTIFICIAL".equals(featureCategory);
    }
}
