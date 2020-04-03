package com.teamgannon.trips.stardata;

/**
 * The Morgan-Keenan Luminosity Class star luminosity class
 * <p>
 * Created by larrymitchell on 2017-02-18.
 */
public enum LuminosityClass {

    /**
     * 0 or Ia+ (hypergiants or extremely luminous supergiants). Example: Cygnus OB2#12 (B3-4Ia+)
     */
    Iaplus("0/Ia+, Hypergiants"),

    /**
     * Ia (luminous supergiants). Example: Eta Canis Majoris (B5Ia)
     */
    Ia("Ia, Supergiants"),

    /**
     * Iab (intermediate luminous supergiants).  Example: Gamma Cygni (F8Iab)
     */
    Iab("Iab, Supergiants"),

    /**
     * Ib (less luminous supergiants). Example: Zeta Persei (B1Ib)
     */
    Ib("Ib, Supergiants"),

    /**
     * II bright giants. Example: Beta Leporis (G0II
     */
    II("II, Bright Giants"),

    /**
     * III normal giants. Example: Arcturus (K0III)
     */
    III("III, Giants"),

    /**
     * IV subgiants. Example: Gamma Cassiopeiae (B0.5IVpe)
     */
    IV("IV, Subgiants"),

    /**
     * V main-sequence stars (dwarfs). Example: Achernar (B6Vep)
     */
    V("V, dwarfs, main sequence"),

    /**
     * sd (prefix) subdwarfs. Example: HD 149382 (sdB5)
     */
    VI("VI, sd, subdwarfs"),

    /**
     * white dwarfs
     */
    VII("VII, white dwarfs"),

    /**
     * D (prefix) white dwarfs.[nb 3] Example: van Maanen 2 (DZ8)
     */
    D("D"),

    /**
     * we don't have a classification for this
     */
    UNKNOWN("unknown"),

    /**
     * we did not set this
     */
    NOT_SET("not set");


    private String luminosity;

    LuminosityClass(String luminosity) {
        this.luminosity = luminosity;
    }

    public String luminosity() {
        return luminosity;
    }
}
