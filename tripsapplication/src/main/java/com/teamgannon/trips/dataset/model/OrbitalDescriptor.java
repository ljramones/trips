package com.teamgannon.trips.dataset.model;

import lombok.Data;
import org.springframework.lang.Nullable;

import javax.persistence.Embeddable;

/**
 * Describes the orbital parameters for an object
 * <p>
 * Created by larrymitchell on 2017-03-02.
 */

@Data
@Embeddable
public class OrbitalDescriptor {

    /**
     * period in days
     */
    private Integer orbitalPeriod;

    /**
     * the size in A.U.
     */
    private Double semiMajorAxis;

    /**
     * the orbital eccentricity 0 to 1
     */
    private Double eccentricity;

    /**
     * Minimum distance to object being orbited in A.U.
     */
    private Double periapsis;

    /**
     * Maximum distance to object being orbited in A.U.
     */
    @Nullable
    private Double apoapsis;


}
