package com.teamgannon.trips.algorithms;

import lombok.Data;

/**
 * handles the scaling for the universe in a box
 * <p>
 * Created by larrymitchell on 2017-01-31.
 */
@Data
public class Universe {

    public final static double boxWidth = 1080;   //780

    public final static double boxHeight = 680;

    public final static double boxDepth = 700;

    public double solCentreX = 0;

    public double solCentreY = 0;

    public double solCentreZ = 0;

}
