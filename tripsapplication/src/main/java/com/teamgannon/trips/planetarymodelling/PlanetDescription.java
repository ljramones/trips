package com.teamgannon.trips.planetarymodelling;

import com.teamgannon.trips.solarsysmodelling.accrete.PlanetTypeEnum;
import lombok.Data;

@Data
public class PlanetDescription {

    private String id;

    private String name;

    /**
     * comma separated list if more than one
     */
    private String belongstoStar;

    /**
     * orginal was in earth masses
     */
    private double mass;

    /**
     * was in earth radius units
     */
    private double radius;

    /**
     * in AU
     */
    private double semiMajorAxis;

    private double eccentricity;

    private double inclination;

    private double argumentOfPeriapsis;

    private double timeOfPeriapsisPassage;

    private double longitudeOfAscendingNode;

    private PlanetTypeEnum planetTypeEnum;

    /**
     * in days
     */
    private double orbitalPeriod;

    /**
     * in Kelvin
     */
    private double equilibriumTemperature;

    /**
     * in log10(cm/s**2)
     */
    private double surfaceGravity;

    /**
     * Whether this is a moon (orbits a planet rather than a star)
     */
    private boolean isMoon;

    /**
     * If this is a moon, the ID of the parent planet
     */
    private String parentPlanetId;

    // --- Ring System Properties ---

    /**
     * Whether this planet has a ring system
     */
    private boolean hasRings;

    /**
     * Type of ring system: "SATURN", "URANUS", "NEPTUNE", or "CUSTOM"
     */
    private String ringType;

    /**
     * Inner radius of ring system in AU (from planet center)
     */
    private double ringInnerRadiusAU;

    /**
     * Outer radius of ring system in AU (from planet center)
     */
    private double ringOuterRadiusAU;

    /**
     * Thickness of ring system as a ratio (0.01 = very thin, 0.1 = moderate)
     */
    private double ringThickness;

    /**
     * Inclination of ring plane relative to planet's equator in degrees
     */
    private double ringInclination;

    /**
     * Primary color of the ring system as hex string (e.g., "#E6DCC8")
     */
    private String ringPrimaryColor;

    /**
     * Secondary color of the ring system as hex string
     */
    private String ringSecondaryColor;

}
