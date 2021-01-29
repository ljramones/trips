package com.teamgannon.trips.stellarmodelling;

import com.teamgannon.trips.solarsysmodelling.utils.spectralclass.HydrogenLines;
import javafx.scene.paint.Color;
import lombok.Data;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
public class StarModel {

    public static Map<String, String> startypes = Stream.of(new String[][]{
            {"sd", "Subdwarf"},
            {"d", "Dwarf"},
            {"sg", "Subgiant"},
            {"g", "Giant"},
            {"c", "Supergiant"}
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    private StellarFactory factory = StellarFactory.getFactory();

    private String starType;

    private StellarType stellarClass;

    private double mass;

    private double radius;

    private double luminosity;

    private double temperature;

    private String luminosityClass;

    private String spectralPecularities;

    /**
     * This is the relative color of the star if Vega, generally considered a bluish star,
     * is used as a standard for "white"
     */
    private Color vegaRelativeChomaticity;

    /**
     * Chromaticity can vary significantly within a class; for example, the Sun (a G2 star) is white,
     * while a G9 star is yellow.
     */
    private Color chomaticity;

    /**
     * the set of hydrogen lines
     */
    private HydrogenLines hydrogenLines;

    /**
     * the percentage fraction that this class is of the total population of stars
     */
    private double percentageFractionOfWhole;


    public void setStarType(String code) {
        starType = startypes.get(code);
    }

    public void setStarClass(String spectralClass) {
        Pattern spectralSplit = Pattern.compile("[A-Z]+");
        String[] split = spectralSplit.split(spectralClass);
        String sClass = spectralClass.substring(0, spectralClass.length() - split[1].length());
        StellarClassification stellarClassification = factory.getStellarClass(sClass);
        if (split.length == 2) {
            try {
                double multiplier = Double.parseDouble(split[1]);
                mass = (stellarClassification.getUpperMass() - stellarClassification.getLowerMass()) * multiplier + stellarClassification.getLowerMass();
                radius = (stellarClassification.getUpperRadius() - stellarClassification.getLowerRadius()) * multiplier + stellarClassification.getLowerRadius();
                luminosity = (stellarClassification.getUpperLuminosity() - stellarClassification.getLowerLuminosity()) * multiplier + stellarClassification.getLowerLuminosity();
            } catch (NullPointerException ignored) {
            }
        }
    }

}
