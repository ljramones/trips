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

}
