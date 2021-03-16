package com.teamgannon.trips.stellarmodelling;

/**
 * The star color
 * <p>
 * Created by larrymitchell on 2017-02-19.
 */
public enum StarColor {

    /**
     * blue
     */
    O("157,180,254"),

    /**
     * blue white
     */
    B("187,204,255"),

    /**
     * white
     */
    A("255,255,255"),

    /**
     * yellow white
     */
    F("255,255,237"),

    /**
     * yellow
     */
    G("255,255,2"),

    /**
     * orange
     */
    K("255,152,51"),

    /**
     * red
     */
    M("255,0,0"),

    /**
     * Wolf-Rayet star N type
     */
    WN("187,204,255"),

    /**
     * Wolf-Rayet star C type
     */
    WC("187,204,255"),

    /**
     * Wolf-Rayet star O type
     */
    WO("187,204,255"),

    /**
     * S type star
     */
    S("187,204,255"),

    /**
     * C type star
     */
    C("255,0,0"),

    /**
     * degenerate star - white dwarfs
     */
    D("255,255,255"),
    DA("255,255,255"),
    DB("255,255,255"),
    DO("255,255,255"),
    DQ("255,255,255"),
    DZ("255,255,255"),
    DC("255,255,255"),
    DX("255,255,255"),

    Unknown("255,0,0"),

    /*
     * Add spectral classes for sub-stellar objects and 
     * "dead" stars that have cooled below visible.  
     * These classes where added to the MK spectral classes 
     * L objects have temperatures down to about 1300K 
     * T objects have temperatures down to about 500K 
     *    The coolest objects currently known are classified as T9 
     * Y objects are cooler than 500K. 
     *    less than 20 Y objects Y0 to Y2 have been classified. 
     *
     * The fact that it's LTY is annoying.  If it was LYT you get an improved mneumonic
     * "Oh be a fine girl kiss me long you temptress"  but LTY has resulted in the 
     * much less woderful 
     * Oh be a fine girl kiss me LighTlY.   (ick) 
     *     Rick Boatright 2017-03-10 
     */

    /**
     * Near IR
     */
    L("200,0,0"),

    /**
     * Mid IR
     */
    T("150,0,0"),

    /**
     * Far IR
     */
    Y("100,0,0");


    private final String color;

    StarColor(String color) {
        this.color = color;
    }

    public String color() {
        return color;
    }


}
