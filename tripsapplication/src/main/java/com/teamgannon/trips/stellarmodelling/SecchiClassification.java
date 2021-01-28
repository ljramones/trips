package com.teamgannon.trips.stellarmodelling;

/**
 * During the 1860s and 1870s, pioneering stellar spectroscopist Angelo Secchi created the Secchi classes in order
 * to classify observed spectra. By 1866, he had developed three classes of stellar spectra
 * <p>
 * Created by larrymitchell on 2017-02-18.
 */
public enum SecchiClassification {

    /**
     * Class I: white and blue stars with broad heavy hydrogen lines, such as Vega and Altair. This includes
     * the modern class A and early class F.
     * Class I, Orion subtype: a subtype of class I with narrow lines in place of wide bands, such as Rigel
     * and Bellatrix. In modern terms, this corresponds to early B-type stars
     */
    CLASS_I("I"),

    /**
     * Class II: yellow stars—hydrogen less strong, but evident metallic lines, such as the Sun, Arcturus,
     * and Capella. This includes the modern classes G and K as well as late class F.
     */
    CLASS_II("II"),

    /**
     * Class III: orange to red stars with complex band spectra, such as Betelgeuse and Antares. This corresponds
     * to the modern class M.
     */
    CLASS_III("III"),

    /**
     * Class IV: red stars with significant carbon bands and lines (carbon stars.)
     * In 1877, he added a fifth class
     */
    CLASS_IV("IV"),

    /**
     * Class V: emission-line stars, such as γ Cassiopeiae and Sheliak.
     */
    CLASS_V("V");


    private final String secchiClass;

    SecchiClassification(String secchiClass) {
        this.secchiClass = secchiClass;
    }

    public String luminosity() {
        return secchiClass;
    }

}
