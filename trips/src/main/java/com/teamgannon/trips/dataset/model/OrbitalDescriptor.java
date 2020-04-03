package com.teamgannon.trips.dataset.model;

import lombok.Data;

/**
 * Describes the orbital parameters for an object
 * <p>
 * Created by larrymitchell on 2017-03-02.
 */
@Data
public class OrbitalDescriptor {

    /**
     * period in days
     */
    private int orbitalPeriod;

    /**
     * the size in A.U.
     */
    private double semiMajorAxis;

    /**
     * the orbital eccentricity 0 to 1
     */
    private double eccentricity;

    /**
     * Minimum distance to object being orbited in A.U.
     */
    private double periapsis;

    /**
     * Maximum distance to object being orbited in A.U.
     */
    private double apoapsis;


}
