package com.teamgannon.trips.stardata;

/**
 * THe spectral pecularities
 * <p>
 * Additional nomenclature, in the form of lower-case letters, can follow the spectral type to indicate peculiar
 * features of the spectrum.
 * <p>
 * Created by larrymitchell on 2017-02-18.
 */
public enum SpectralPecularities {

    /**
     * uncertain spectral value
     */
    uncertainSpectralValue(":"),

    /**
     * Undescribed spectral peculiarities exist
     */
    undescribedSpectralPecularities("..."),

    /**
     * Special peculiarity
     */
    specialPecularity("!"),

    /**
     * Composite spectrum
     */
    compositeSpectrum("comp"),

    /**
     * Emission lines present
     */
    emissionLinesPresent("e"),

    /**
     * Forbidden emission lines have been observed in extremely low-density gases and plasmas, either in
     * outer space or in the extreme upper atmosphere of the Earth. In space environments, densities
     * may be only a few atoms per cubic centimetre, making atomic collisions unlikely. Under such conditions,
     * once an atom or molecule has been excited for any reason into a meta-stable state, then it is almost
     * certain to decay by emitting a forbidden-line photon. Since meta-stable states are rather common,
     * forbidden transitions account for a significant percentage of the photons emitted by the ultra-low density
     * gas in space. Forbidden transitions in highly charged ions resulting in the emission of visible,
     * vacuum-ultraviolet, soft x-ray and x-ray photons are routinely observed in certain laboratory devices
     * such as electron beam ion traps [8] and ion storage rings, where in both cases residual gas densities
     * are sufficiently low for forbidden line emission to occur before atoms are collisionally de-excited.
     * Using laser spectroscopy techniques, forbidden transitions are used to stabilize atomic clocks and
     * quantum clocks that have the highest accuracies currently available.
     */
    forbiddenEmissionLinesPresent("..."),

    /**
     * "Reversed" center of emission lines weaker than edges
     */
    reversedCenter("er"),

    /**
     * Emission lines with P Cygni profile
     * <p>
     * P Cygni gives its name to a type of spectroscopic feature called a P Cygni profile, where the presence of
     * both absorption and emission in the profile of the same spectral line indicates the existence of a gaseous
     * envelope expanding away from the star. The emission line arises from a dense stellar wind near to the star,
     * while the blueshifted absorption lobe is created where the radiation passes through circumstellar material
     * rapidly expanding in the direction of the observer. These profiles are useful in the study of stellar winds
     * in many types of stars. They are often cited as an indicator of a luminous blue variable star, although they
     * also occur in other types of star
     */
    eq("eq"),

    /**
     * N III and He II emission
     */
    f("f"),

    /**
     * N IV λ4058Å is stronger than the N III λ4634Å, λ4640Å, & λ4642Å lines
     */
    fstar("f*"),

    /**
     * Si IV λ4089Å & λ4116Å are emission in addition to the N III line
     */
    fplus("f+"),

    /**
     * N III emission, absence or weak absorption of He II
     */
    fbrackets("(f)"),

    /**
     * https://arxiv.org/abs/0907.1033
     */
    fbracketsplus("(f+)"),

    /**
     * Displays strong He II absorption accompanied by weak N III emissions
     */
    fdoublebrackets("((f))"),

    /**
     * https://arxiv.org/abs/0907.1033
     */
    fdoublebracketsstar("((f*))"),

    /**
     * WR stars with emission lines due to hydrogen.
     */
    h("h"),

    /**
     * WR stars with hydrogen emissions seen on both absorption and emission.
     */
    ha("ha"),

    /**
     * Weak He lines
     */
    hewk("HE wk"),

    /**
     * Spectra with interstellar absorption features
     */
    k("k"),

    /**
     * Enhanced metal features
     */
    m("m"),

    /**
     * Broad ("nebulous") absorption due to spinning
     */
    n("n"),

    /**
     * Very broad absorption features
     */
    nn("nn"),

    /**
     * A nebula's spectrum mixed in
     */
    neb("neb"),

    /**
     * Unspecified peculiarity, peculiar star
     * <p>
     * In astrophysics, chemically peculiar stars are stars with distinctly unusual metal abundances, at least
     * in their surface layers.
     */
    p("p"),

    /**
     * Peculiar spectrum, similar to the spectra of novae
     */
    pq("pq"),

    /**
     * Red & blue shifts line present
     */
    q("q"),

    /**
     * Narrowly "sharp" absorption lines
     */
    s("s"),

    /**
     * Very narrow lines
     */
    ss("ss"),

    /**
     * Shell star features
     * <p>
     * A shell star is a star having a spectrum that exhibits features indicating a circumstellar disc of gas
     * surrounding the star at the equator. They may exhibit irregular variations in their luminosity due to
     * the outflow of matter. The shell stars are fast rotators, giving a partial explanation on the mechanism,
     * but shell stars are still considered enigmatic. Shell stars belong to spectral types O7.5 to F5, but
     * their spectra are characterized by enormously widened absorption lines due to fast rotation and the disc
     * that contributes also to other spectral peculiarities. Rotation velocities are about 200–250 km/s, not
     * far from the point when the rotational acceleration would disrupt the star.
     */
    sh("sh"),

    /**
     * Variable spectral feature (sometimes abbreviated to "v")
     */
    var("var"),

    /**
     * Weak lines (also "w" & "wk")
     */
    wl("wl");


    private String spectralPecularities;

    SpectralPecularities(String spectralPecularities) {
        this.spectralPecularities = spectralPecularities;
    }

    public String luminosity() {
        return spectralPecularities;
    }

}
