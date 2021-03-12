package com.teamgannon.trips.stellarmodelling;

import com.teamgannon.trips.solarsysmodelling.accrete.SimStar;
import javafx.scene.paint.Color;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

@Slf4j
@Data
public class StarModel {

    private StellarFactory factory = StellarFactory.getFactory();

    private StellarType stellarClass;

    private double mass;

    private double radius;

    private double luminosity;

    private double absoluteMagnitude;

    private double temperature;

    private String luminosityClass;

    private String spectralPecularities;

    /**
     * Chromaticity can vary significantly within a class; for example, the Sun (a G2 star) is white,
     * while a G9 star is yellow.
     */
    private Color chromaticity;

    /**
     * the set of hydrogen lines
     */
    private HydrogenLines hydrogenLines;

    /**
     * the percentage fraction that this class is of the total population of stars
     */
    private double percentageFractionOfWhole;


    public void setStarClass(String spectralClass) {
        if (spectralClass.isEmpty()) {
            log.error("spectral class cannot be null");
            return;
        }
        if (spectralClass.equals("0")) {
            log.error("used zero instead of O, changed to an O");
            spectralClass = "O";
        }
        if (spectralClass.length() == 1) {
            StellarClassification stellarClassification = factory.getStellarClass(spectralClass);
            if (stellarClassification == null) {
                log.error("spectral classification cannot be null");
                return;
            }
            stellarClass = stellarClassification.getStellarType();
            if (stellarClass == null) {
                log.info("bad stellar class:{}", spectralClass);
            }
        } else {
            Pattern spectralSplit = Pattern.compile("^[A-Z]+");
            String[] split = spectralSplit.split(spectralClass);
            String sClass;
            if (split.length == 0) {
                sClass = spectralClass;
            } else {
                int remainingLength = spectralClass.length() - split[1].length();
                if (remainingLength <= 0) {
                    log.error("hmmm");
                }
                sClass = spectralClass.substring(0, remainingLength);
            }
            StellarClassification stellarClassification = factory.getStellarClass(sClass);
            if (stellarClassification != null) {
                if (split.length == 2) {
                    try {
                        stellarClass = stellarClassification.getStellarType();
                        chromaticity = stellarClassification.getStellarChromaticity();
                        hydrogenLines = stellarClassification.getLines();
                        percentageFractionOfWhole = stellarClassification.getSequenceFraction();

                        double multiplier = Double.parseDouble(split[1]);
                        mass = calcMean(stellarClassification.getUpperMass(), stellarClassification.getLowerMass(), multiplier / 10);
                        radius = calcMean(stellarClassification.getUpperRadius(), stellarClassification.getLowerRadius(), multiplier / 10);
                        luminosity = calcMean(stellarClassification.getUpperLuminosity(), stellarClassification.getLowerLuminosity(), multiplier / 10);
                        temperature = calcMean(stellarClassification.getUpperTemperature(), stellarClassification.getLowerTemperature(), multiplier / 10);
                        absoluteMagnitude = StarUtils.absoluteMagnitude(luminosity);
//                        log.info("mass={}, radius = {}, luminosity={}, temperature={}", mass, radius, luminosity, temperature);
                    } catch (NullPointerException ignored) {
                    }
                } else {
                    stellarClass = stellarClassification.getStellarType();
                    chromaticity = stellarClassification.getStellarChromaticity();
                    hydrogenLines = stellarClassification.getLines();
                    percentageFractionOfWhole = stellarClassification.getSequenceFraction();
                    double multiplier = 5;
                    mass = calcMean(stellarClassification.getUpperMass(), stellarClassification.getLowerMass(), multiplier / 10);
                    radius = calcMean(stellarClassification.getUpperRadius(), stellarClassification.getLowerRadius(), multiplier / 10);
                    luminosity = calcMean(stellarClassification.getUpperLuminosity(), stellarClassification.getLowerLuminosity(), multiplier / 10);
                    temperature = calcMean(stellarClassification.getUpperTemperature(), stellarClassification.getLowerTemperature(), multiplier / 10);
                    absoluteMagnitude = StarUtils.absoluteMagnitude(luminosity);
//                    log.info("mass={}, radius = {}, luminosity={}, temperature={}", mass, radius, luminosity, temperature);
                }
            } else {
                log.error("unable to find a stellar classification for {}", spectralClass);
            }
        }
    }

    public void setMassInFullUnits(double mass) {

    }

    public double calcMean(double lower, double upper, double percentage) {
        return (upper - lower) * percentage + lower;
    }

    public SimStar toSimStar() {
        return new SimStar(this.mass, this.luminosity, this.radius, this.temperature, absoluteMagnitude);
    }


    public static void main(String[] args) {
        //G8IV
        StarModel starModel = new StarModel();
        starModel.setStarClass("G8IV");
        log.info(starModel.toString());
    }

}
