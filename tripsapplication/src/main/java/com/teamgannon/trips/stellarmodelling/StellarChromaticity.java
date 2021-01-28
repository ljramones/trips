package com.teamgannon.trips.stellarmodelling;

/**
 * The Stellar Chromaticity
 * <p>
 * Created by larrymitchell on 2017-02-19.
 */
public enum StellarChromaticity {

    /**
     * blue
     */
    O("157,180,254"),

    /**
     * deep blue white
     */
    B("170,191,255"),

    /**
     * blue white
     */
    A("202,216,255"),

    /**
     * white
     */
    F("255,255,255"),

    /**
     * yellowish white
     */
    G("255,244,232"),

    /**
     * pale yellowish orange
     */
    K("255,222,180"),

    /**
     * light orange red
     */
    M("255,189,111");


    private final String chromaticity;

    StellarChromaticity(String chromaticity) {
        this.chromaticity = chromaticity;
    }

    public String chromaticity() {
        return chromaticity;
    }


}
