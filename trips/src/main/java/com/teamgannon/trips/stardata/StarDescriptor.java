package com.teamgannon.trips.stardata;

import lombok.Data;

/**
 * Used to describe a specific star
 *
 * Created by larrymitchell on 2017-02-19.
 */
@Data
public class StarDescriptor {

    /**
     * MK stellar type
     */
    private StellarType stellarType;

    /**
     *
     */
    private LuminosityClass luminosityClass;

    private SecchiClassification secchiClassification;

    private SpectralPecularities spectralPecularities;

    /**
     * stellar hydrogen lines
     */
    private HydrogenLines lines;

    /**
     * specific stellar temperature
     */
    private double temperature;

    /**
     * specific stellar color
     */
    private String color;

    /**
     * specific stellar chromacity
     */
    private String chromacity;


    /**
     * specific stellar mass
     */
    private double mass;

    /**
     * specific stellar radius
     */
    private double radius;

    /**
     * specific stellar luminosity
     */
    private double luminosity;



}
