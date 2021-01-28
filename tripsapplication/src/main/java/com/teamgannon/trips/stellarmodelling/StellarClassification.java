package com.teamgannon.trips.stellarmodelling;

import lombok.Data;

/**
 * the stellar classification
 * <p>
 * Created by larrymitchell on 2017-02-18.
 */
@Data
public class StellarClassification {

    private StellarType stellarType;

    private StarColor starColor;

    private StellarChromaticity stellarChromaticity;

    private LuminosityClass luminosityClass;

    private SecchiClassification secchiClassification;

    private SpectralPecularities spectralPecularities;

    private double upperTemperature;

    private double lowerTemperature;

    private String color;

    private String chromacity;

    private double upperMass;

    private double lowerMass;

    private double upperRadius;

    private double lowerRadius;

    private double upperLuminosity;

    private double lowerLuminosity;

    private HydrogenLines lines;

    private double sequenceFraction;

    /**
     * get the average radius
     *
     * @return the the average radius
     */
    public double getAverageRadius() {
        return (upperRadius + lowerRadius) / 2;
    }
}
