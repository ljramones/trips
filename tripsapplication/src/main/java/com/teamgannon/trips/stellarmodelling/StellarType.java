package com.teamgannon.trips.stellarmodelling;

/**
 * The Harvard Spectral Classification spectral classificaiton type
 * <p>
 * Created by larrymitchell on 2017-02-18.
 */
public enum StellarType {

    /**
     * O-type stars are very hot and extremely luminous, with most of their radiated output in the ultraviolet range.
     * These are the rarest of all main-sequence stars. About 1 in 3,000,000 (0.00003%) of the main-sequence stars in
     * the solar neighborhood are O-type stars.[nb 5][9] Some of the most massive stars lie within this spectral class.
     * O-type stars frequently have complicated surroundings that make measurement of their spectra difficult.
     * <p>
     * O-type spectra used to be defined by the ratio of the strength of the He II λ4541 relative to that of
     * He I λ4471, where λ is the wavelength, measured in ångströms. Spectral type O7 was defined to be the point
     * at which the two intensities are equal, with the He I line weakening towards earlier types. Type O3 was,
     * by definition, the point at which said line disappears altogether, although it can be seen very faintly
     * with modern technology. Due to this, the modern definition uses the ratio of the nitrogen line N IV λ4058
     * to N III λλ4634-40-42.
     * <p>
     * O-type stars have dominant lines of absorption and sometimes emission for He II lines, prominent
     * ionized (Si IV, O III, N III, and C III) and neutral helium lines, strengthening from O5 to O9, and
     * prominent hydrogen Balmer lines, although not as strong as in later types. Because they are so massive,
     * O-type stars have very hot cores and burn through their hydrogen fuel very quickly, so they are the first
     * stars to leave the main sequence.
     */
    O("O"),

    /**
     * B-type stars are very luminous and blue. Their spectra have neutral helium, which are most prominent at the
     * B2 subclass, and moderate hydrogen lines. As O- and B-type stars are so energetic, they only live for a
     * relatively short time. Thus, due to the low probability of kinematic interaction during their lifetime,
     * they are unable to stray far from the area in which they formed, apart from runaway stars.
     * <p>
     * The transition from class O to class B was originally defined to be the point at which the He II λ4541
     * disappears. However, with today's better equipment, the line is still apparent in the early B-type stars.
     * Today for main-sequence stars, the B-class is instead defined by the intensity of the He I violet spectrum,
     * with the maximum intensity corresponding to class B2. For supergiants, lines of silicon are used instead;
     * the Si IV λ4089 and Si III λ4552 lines are indicative of early B. At mid B, the intensity of the latter
     * relative to that of Si II λλ4128-30 is the defining characteristic, while for late B, it is the intensity
     * of Mg II λ4481 relative to that of He I λ4471.
     * <p>
     * These stars tend to be found in their originating OB associations, which are associated with giant
     * molecular clouds. The Orion OB1 association occupies a large portion of a spiral arm of the Milky Way and
     * contains many of the brighter stars of the constellation Orion. About 1 in 800 (0.125%) of the main-sequence
     * stars in the solar neighborhood are B-type main-sequence objects.
     * <p>
     * Massive yet non-supergiant entities known as "Be stars" are main-sequence stars that notably have, or had
     * at some time, one or more Balmer lines in emission, with the hydrogen-related electromagnetic radiation
     * series projected out by the stars being of particular interest. Be stars are generally thought to feature
     * unusually strong stellar winds, high surface temperatures, and significant attrition of stellar mass as
     * the objects rotate at a curiously rapid rate.[57] Objects known as "B(e)" or "B[e]" stars possess distinctive
     * neutral or low ionisation emission lines that are considered to have 'forbidden mechanisms', undergoing
     * processes not normally allowed under current understandings of quantum mechanics.
     */
    B("B"),

    /**
     * A-type stars are among the more common naked eye stars, and are white or bluish-white. They have strong
     * hydrogen lines, at a maximum by A0, and also lines of ionized metals (Fe II, Mg II, Si II) at a maximum
     * at A5. The presence of Ca II lines is notably strengthening by this point. About 1 in 160 (0.625%) of
     * the main-sequence stars in the solar neighborhood are A-type stars
     */
    A("A"),

    /**
     * F-type stars have strengthening H and K lines of Ca II. Neutral metals (Fe I, Cr I) beginning to gain on
     * ionized metal lines by late F. Their spectra are characterized by the weaker hydrogen lines and ionized
     * metals. Their color is white. About 1 in 33 (3.03%) of the main-sequence stars in the solar neighborhood
     * are F-type stars.
     */
    F("F"),

    /**
     * G-type stars, including the Sun[11] have prominent H and K lines of Ca II, which are most pronounced at G2.
     * They have even weaker hydrogen lines than F, but along with the ionized metals, they have neutral metals.
     * There is a prominent spike in the G band of CH molecules. Class G main-sequence stars make up about 7.5%,
     * nearly one in thirteen, of the main-sequence stars in the solar neighborhood.
     * <p>
     * G is host to the "Yellow Evolutionary Void".[59] Supergiant stars often swing between O or B (blue) and K
     * or M (red). While they do this, they do not stay for long in the yellow supergiant G classification as this
     * is an extremely unstable place for a supergiant to be.
     */
    G("G"),

    /**
     * K-type stars are orangish stars that are slightly cooler than the Sun. They make up about 12%, nearly one
     * in eight, of the main-sequence stars in the solar neighborhood.[nb 5][9] There are also giant K-type stars,
     * which range from hypergiants like RW Cephei, to giants and supergiants, such as Arcturus, whereas orange
     * dwarfs, like Alpha Centauri B, are main-sequence stars.
     * <p>
     * They have extremely weak hydrogen lines, if they are present at all, and mostly neutral metals
     * (Mn I, Fe I, Si I). By late K, molecular bands of titanium oxide become present. There is a suggestion that
     * K Spectrum stars may potentially increase the chances of life developing on orbiting planets that are
     * within the habitable zone
     */
    K("K"),

    /**
     * Class M stars are by far the most common. About 76% of the main-sequence stars in the solar neighborhood
     * are class M stars.[nb 5][nb 6][9] However, class M main-sequence stars (red dwarfs) have such low
     * luminosities that none are bright enough to be seen with the unaided eye, unless under exceptional
     * conditions. The brightest known M-class main-sequence star is M0V Lacaille 8760, with magnitude 6.6
     * (the limiting magnitude for typical naked-eye visibility under good conditions is typically quoted as 6.5),
     * and it is extremely unlikely that any brighter examples will be found.
     * <p>
     * Although most class M stars are red dwarfs, most giants and some supergiants such as VY Canis Majoris,
     * Antares and Betelgeuse are also class M. Furthermore, the hotter brown dwarfs are late class M. This is
     * usually in the range of M6.5 to M9.5. The spectrum of a class M star contains lines from oxide molecules,
     * especially TiO, in the visible and all neutral metals, but absorption lines of hydrogen are usually absent.
     * TiO bands can be strong in class M stars, usually dominating their visible spectrum by about M5.
     * vanadium(II) oxide bands become present by late M.
     */
    M("M"),

    /**
     * Dwarf M star, abbreviated dM, was also used, but sometimes it also included stars of spectral type K.
     */
    dM("dM"),

    /**
     * Class W or WR represents the Wolf–Rayet stars, notable for spectra lacking hydrogen lines. Instead their
     * spectra are dominated by broad emission lines of highly ionized helium, nitrogen, carbon and sometimes
     * oxygen. They are thought to mostly be dying supergiants with their hydrogen layers blown away by stellar
     * winds, thereby directly exposing their hot helium shells. Class W is further divided into subclasses
     * according to the relative strength of nitrogen and carbon emission lines in their spectra
     * (and outer layers).
     */
    WN("WN"),

    WO("WO"),

    WC("WC"),

    /**
     * Class L dwarfs get their designation because they are cooler than M stars and L is the remaining letter
     * alphabetically closest to M. Some of these objects have masses large enough to support hydrogen fusion
     * and are therefore stars, but most are of substellar mass and are therefore brown dwarfs. They are a very
     * dark red in color and brightest in infrared. Their atmosphere is cool enough to allow metal hydrides
     * and alkali metals to be prominent in their spectra.
     * <p>
     * Due to low surface gravity in giant stars, TiO- and VO-bearing condensates never form. Thus, L-type stars
     * larger than dwarfs can never form in an isolated environment. It may be possible for these L-type
     * supergiants to form through stellar collisions, however. An example of which is V838 Monocerotis while in
     * the height of its luminous red nova eruption.
     */
    L("L"),

    /**
     * Class T dwarfs are cool brown dwarfs with surface temperatures between approximately 550 and 1,300 K
     * (277 and 1,027 °C; 530 and 1,880 °F). Their emission peaks in the infrared. Methane is prominent in
     * their spectra.
     * <p>
     * Classes T and L could be more common than all the other classes combined if recent research is accurate.
     * Study of the number of proplyds (protoplanetary discs, clumps of gas in nebulae from which stars and
     * planetary systems are formed) indicates that the number of stars in the galaxy should be several orders
     * of magnitude higher than what was previously speculated. It is theorized that these proplyds are in a race
     * with each other. The first one to form will become a protostar, which are very violent objects and will
     * disrupt other proplyds in the vicinity, stripping them of their gas. The victim proplyds will then
     * probably go on to become main-sequence stars or brown dwarfs of the L and T classes, which are quite
     * invisible to us. Because brown dwarfs can live so long, these smaller bodies accumulate over time.
     */
    T("T"),

    /**
     * Brown dwarfs of spectral class Y are cooler than those of spectral class T and have qualitatively different
     * spectra from them. A total of 17 objects have been placed in class Y as of August 2013. Although such
     * dwarfs have been modelled and detected within forty light years by the Wide-field Infrared
     * Survey Explorer (WISE) there is no well-defined spectral sequence yet with prototypes. Nevertheless,
     * several objects have been assigned spectral classes Y0, Y1, and Y2. The spectra of these objects
     * display absorption around 1.55 micrometers. Delorme et al. has suggested that this feature is due to
     * absorption from ammonia and that this should be taken as indicating the T–Y transition, making these
     * objects of type Y0. In fact, this ammonia-absorption feature is the main criterion that has been adopted
     * to define this class. However, this feature is difficult to distinguish from absorption by water
     * and methane, and other authors have stated that the assignment of class Y0 is premature.
     * <p>
     * The brown dwarf with the latest assigned spectral type, WISE 1828+2650, is a >Y2 dwarf with an effective
     * temperature originally estimated around 300 K, the temperature of the human body.[74][75][82] Parallax
     * measurements have, however, since shown that its luminosity is inconsistent with it being colder than ~400 K.
     * The coolest Y dwarf currently known is WISE 0855-0714 with an approximate temperature of 250 K.
     * <p>
     * The mass range for Y dwarfs is 9–25 Jupiter masses, but for young objects might reach below one Jupiter mass,
     * which means that Y class objects straddle the 13 Jupiter mass deuterium-fusion limit that marks the division
     * between brown dwarfs and planets.
     */
    Y("Y"),

    /**
     * Originally classified as R and N stars, these are also known as 'carbon stars'. These are red giants, near
     * the end of their lives, in which there is an excess of carbon in the atmosphere. The old R and N classes
     * ran parallel to the normal classification system from roughly mid G to late M. These have more recently
     * been remapped into a unified carbon classifier C with N0 starting at roughly C6. Another subset of cool
     * carbon stars are the J-type stars, which are characterized by the strong presence of molecules of 13CN
     * in addition to those of 12CN.[84] A few main-sequence carbon stars are known, but the overwhelming majority
     * of known carbon stars are giants or supergiants. There are several subclasses:
     * <p>
     * C-R: Formerly a class on its own representing the carbon star equivalent of late G to early K-type stars.
     * C-N: Formerly a class on its own representing the carbon star equivalent of late K to M stars.
     * C-J: A subtype of cool C stars with a high content of 13C.
     * C-H: Population II analogues of the C-R stars.
     * C-Hd: Hydrogen-deficient carbon stars, similar to late G supergiants with CH and C2 bands added.
     * <p>
     * C-R the old Harvard class R reborn: are still visible at the blue end of the spectrum, strong isotopic bands,
     * no enhanced Ba line	medium disc pop I	0	red giants?	5100-2800	S Cam	~25
     */
    CR("C-R"),

    /**
     * C-N:	the old Harvard class N reborn: heavy diffuse blue absorption, sometimes invisible in blue,
     * s-process elements enhanced over solar abundance, weak isotopic bands	thin disc pop I	-2.2
     * AGB	3100-2600	R Lep	~90
     */
    CB("C-N"),

    // non-classical carbob stars
    /**
     * C-J:	very strong isotopic bands of C2 and CN	unknown	unknown	unknown	3900-2800	Y CVn	~20
     */
    CJ("C-J"),

    /**
     * C-H:	very strong CH absorption	halo pop II	-1.8	bright giants, mass transfer (all C-H:s are
     * binary )	5000-4100	V Ari, TT CVn	~20
     */
    CH("C-H"),

    /**
     * C-Hd:	hydrogen lines and CH bands weak or absent	thin disc pop I	-3.5	unknown	?	HD 137613	~7
     */
    CHd("C-Hd"),

    /**
     * Class-S stars form a continuum between class-M stars and carbon stars. Those most similar to class-M stars
     * have strong ZrO absorption bands analogous to the TiO bands of class-M stars, whereas those most similar
     * to carbon stars have strong sodium D lines and weak C2 bands. Class-S stars have excess amounts of
     * zirconium and other elements produced by the s-process, and have more similar carbon and oxygen abundances
     * than class-M or carbon stars. Like carbon stars, nearly all known class-S stars are asymptotic-giant-branch stars.
     * <p>
     * The spectral type is formed by the letter S and a number between zero and ten. This number corresponds to
     * the temperature of the star and approximately follows the temperature scale used for class-M giants. The
     * most common types are S3 to S5, and S10 has only been used for the star χ Cygni when at an extreme minimum.
     * <p>
     * The basic classification is usually followed by an abundance indication, following one of several schemes:
     * S2,5; S2/5; S2 Zr4 Ti2; or S2*5. A number following a comma is a scale between 1 and 9 based on the ratio
     * of ZrO and TiO. A number following a slash is a more recent but less common scheme designed to represent
     * the ratio of carbon to oxygen on a scale of 1 to 10, where a 0 would be an MS star. Intensities of
     * zirconium and titanium may be indicated explicitly. Also occasionally seen is a number following an
     * asterisk, which represents the strength of the ZrO bands on a scale from 1 to 5.
     */
    S("S"),

    /**
     * A Q-Star, also known as a grey hole, is a hypothetical type of a compact, heavy neutron star with
     * an exotic state of matter. Such a star can be smaller than the progenitor star's Schwarzschild radius
     * and have a gravitational pull so strong that some, but not all light, cannot escape.[citation needed]
     * The Q stands for a conserved particle number. A Q-Star may be mistaken for a stellar black hole.
     */
    Q("Q"),

    /**
     * Planetary nebula
     * gas shell ejected by giant star prior to collapse to white dwarf
     */
    P("P"),

    /**
     * Zirconium Giant stars
     * <p>
     * These are related to late M giants that show absorption bands of zirconium oxide (ZrO) in their spectra
     * and appear to form a bridge between M giant stars and carbon stars in the sequence
     * <p>
     * M → MS → S → SC → C
     * <p>
     * where the C/O ratio between carbon and oxygen shifts across the sequence from < 0.95 in S1 to 1.0 in
     * SC1 and >1.1 in SC10 stars.
     */
    Z("Z"),

    /**
     * generic white dwarf
     */
    D("D"),
    /**
     * The class D (for Degenerate) is the modern classification used for white dwarfs—low-mass stars that are
     * no longer undergoing nuclear fusion and have shrunk to planetary size, slowly cooling down.
     * Class D is further divided into spectral types
     * DA, DB, DC, DO, DQ, DX, and DZ.
     * The letters are not related to the letters used in the classification of other stars,
     * but instead indicate the composition of the white dwarf's visible outer layer or atmosphere.
     *
     * DA – a hydrogen-rich atmosphere or outer layer, indicated by strong Balmer hydrogen spectral lines.
     */
    DA("DA"),

    /**
     * DB – a helium-rich atmosphere, indicated by neutral helium, He I, spectral lines.
     */
    DB("DB"),

    /**
     * DC – no strong spectral lines indicating one of the above categories.
     */
    DC("DC"),

    /**
     * DO – a helium-rich atmosphere, indicated by ionized helium, He II, spectral lines.
     */
    DO("DO"),

    /**
     * DQ – a carbon-rich atmosphere, indicated by atomic or molecular carbon lines.
     */
    DQ("DQ"),

    /**
     * DX – spectral lines are insufficiently clear to classify into one of the above categories.
     */
    DX("DX"),

    /**
     * DZ – a metal-rich atmosphere, indicated by metal spectral lines (a merger of the obsolete white dwarf
     * spectral types, DG, DK, and DM).
     */
    DZ("DZ");

    private final String className;

    StellarType(String className) {
        this.className = className;
    }

    public String getValue() {
        return className;
    }

}
