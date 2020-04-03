package com.teamgannon.trips.utility;

import lombok.Data;

/**
 * Spherical coordiates
 * <p>
 * Created by larrymitchell on 2017-03-04.
 */
@Data
public class SphericalCoordinate {

    /**
     * the right ascension
     */
    private String rightAscension;

    /**
     * the stellar declinaiton
     */
    private String declination;

    /**
     * the distance in light years
     */
    private double distance;

}
