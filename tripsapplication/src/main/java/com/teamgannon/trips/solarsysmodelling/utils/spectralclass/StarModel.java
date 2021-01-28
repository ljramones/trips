package com.teamgannon.trips.solarsysmodelling.utils.spectralclass;

import com.teamgannon.trips.stellarmodelling.StellarType;
import javafx.scene.paint.Color;
import lombok.Data;

import static com.teamgannon.trips.stellarmodelling.StellarFactory.*;

@Data
public class StarModel {

    private StellarType stellarClass;

    private StellarLimitsRange effectiveTempRange;

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
     * the range of mass values for this class
     */
    private StellarLimitsRange massRange;

    /**
     * the range of radius values for this class
     */
    private StellarLimitsRange radiusRange;

    /**
     * the range of luminosity values ofr this class
     */
    private StellarLimitsRange luminostyRange;

    /**
     * the set of hydrogen lines
     */
    private HydrogenLines hydrogenLines;

    /**
     * the percentage fraction that this class is of the total population of stars
     */
    private double percentageFractionOfWhole;

    public void setupStarRanges() {
        createOClass();
        createBClass();
        createAClass();
        createFClass();
        createGClass();
        createKClass();
        createMClass();

    }

}
