package com.teamgannon.trips.stardata;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The stellar factory
 * <p>
 * Created by larrymitchell on 2017-02-19.
 */
public class StellarFactory {

    /**
     * the factory
     */
    private static final StellarFactory factory = new StellarFactory();
    /**
     * the stellar map based on key = stellar type and the created stellar classification
     */
    private static final Map<String, StellarClassification> stellarClassificationMap = new HashMap<>();
    /**
     * the set of stelaar classes
     */
    private static final Set<String> stellarClasses = new HashSet<>();

    /**
     * get the factory
     *
     * @return the factory
     */
    public static StellarFactory getFactory() {
        return factory;
    }

    /**
     * create an O class generic star
     * <p>
     * O-type stars are very hot and extremely luminous, with most of their radiated output in the ultraviolet
     * range. These are the rarest of all main-sequence stars. About 1 in 3,000,000 (0.00003%) of the
     * main-sequence stars in the solar neighborhood are O-type stars.[nb 5][9] Some of the most massive stars
     * lie within this spectral class. O-type stars frequently have complicated surroundings that make measurement
     * of their spectra difficult.
     * <p>
     * O-type spectra used to be defined by the ratio of the strength of the He II λ4541 relative to that of
     * He I λ4471, where λ is the wavelength, measured in ångströms. Spectral type O7 was defined to be the
     * point at which the two intensities are equal, with the He I line weakening towards earlier types.
     * Type O3 was, by definition, the point at which said line disappears altogether, although it can be seen
     * very faintly with modern technology. Due to this, the modern definition uses the ratio of the nitrogen
     * line N IV λ4058 to N III λλ4634-40-42.
     * <p>
     * O-type stars have dominant lines of absorption and sometimes emission for He II lines, prominent ionized
     * (Si IV, O III, N III, and C III) and neutral helium lines, strengthening from O5 to O9, and prominent hydrogen
     * Balmer lines, although not as strong as in later types. Because they are so massive, O-type stars have very
     * hot cores and burn through their hydrogen fuel very quickly, so they are the first stars to leave the main
     * sequence.
     * <p>
     * When the MKK classification scheme was first described in 1943, the only subtypes of class O used were O5
     * to O9.5. The MKK scheme was extended to O9.7 in 1971 and O4 in 1978, and new classification schemes
     * that add types O2, O3 and O3.5 have subsequently been introduced.
     * Spectral standards:
     * <p>
     * O7V: S Monocerotis
     * O9V: 10 Lacertae
     *
     * @return the star general description
     */
    public static StellarClassification createOClass() {
        StellarClassification oClass = new StellarClassification();
        oClass.setStellarType(StellarType.O);
        oClass.setChromacity("blue");
        oClass.setStarColor(StarColor.O);
        oClass.setStellarChromaticity(StellarChromaticity.O);
        oClass.setColor("blue");
        oClass.setLines(HydrogenLines.WEAK);
        oClass.setLowerTemperature(30000);
        oClass.setUpperTemperature(70000);
        oClass.setUpperLuminosity(10000000.0);
        oClass.setLowerLuminosity(30000.0);
        oClass.setUpperMass(400);
        oClass.setLowerMass(16.0);
        oClass.setUpperRadius(45.0);
        oClass.setLowerRadius(6.6);
        oClass.setSequenceFraction(0.00003);
        return oClass;
    }

    /**
     * create an B class generic star
     * <p>
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
     * These stars tend to be found in their originating OB associations, which are associated with giant molecular
     * clouds. The Orion OB1 association occupies a large portion of a spiral arm of the Milky Way and contains
     * many of the brighter stars of the constellation Orion. About 1 in 800 (0.125%) of the main-sequence stars
     * in the solar neighborhood are B-type main-sequence objects.
     * <p>
     * Massive yet non-supergiant entities known as "Be stars" are main-sequence stars that notably have, or had
     * at some time, one or more Balmer lines in emission, with the hydrogen-related electromagnetic radiation
     * series projected out by the stars being of particular interest. Be stars are generally thought to feature
     * unusually strong stellar winds, high surface temperatures, and significant attrition of stellar mass as the
     * objects rotate at a curiously rapid rate. Objects known as "B(e)" or "B[e]" stars possess distinctive
     * neutral or low ionisation emission lines that are considered to have 'forbidden mechanisms', undergoing
     * processes not normally allowed under current understandings of quantum mechanics.
     * <p>
     * Spectral standards:
     * <p>
     * B0V: Upsilon Orionis
     * B0Ia: Alnilam
     * B2Ia: Chi2 Orionis
     * B2Ib: 9 Cephei
     * B3V: Eta Ursae Majoris
     * B3V: Eta Aurigae
     * B3Ia: Omicron2 Canis Majoris
     * B5Ia: Eta Canis Majoris
     * B8Ia: Rigel
     *
     * @return the star general description
     */
    public static StellarClassification createBClass() {
        StellarClassification bClass = new StellarClassification();
        bClass.setStellarType(StellarType.B);
        bClass.setStarColor(StarColor.B);
        bClass.setStellarChromaticity(StellarChromaticity.B);
        bClass.setColor("blue white");
        bClass.setChromacity("deep blue white");
        bClass.setLines(HydrogenLines.MEDIUM);
        bClass.setUpperTemperature(30000);
        bClass.setLowerTemperature(10000);
        bClass.setUpperLuminosity(30000);
        bClass.setLowerLuminosity(25);
        bClass.setUpperMass(16);
        bClass.setLowerMass(2.1);
        bClass.setUpperRadius(6.6);
        bClass.setLowerLuminosity(1.8);
        bClass.setSequenceFraction(.13);
        return bClass;
    }

    /**
     * create an A class generic star
     * <p>
     * A-type stars are among the more common naked eye stars, and are white or bluish-white. They have strong
     * hydrogen lines, at a maximum by A0, and also lines of ionized metals (Fe II, Mg II, Si II) at a maximum
     * at A5. The presence of Ca II lines is notably strengthening by this point. About 1 in 160 (0.625%) of the
     * main-sequence stars in the solar neighborhood are A-type stars.
     * <p>
     * Spectral standards:
     * A0Van: Gamma Ursae Majoris
     * A0Va: Vega
     * A0Ib: Eta Leonis
     * A0Ia: HD 21389
     * A2Ia: Deneb
     * A3Va: Fomalhaut
     *
     * @return the star general description
     */
    public static StellarClassification createAClass() {
        StellarClassification aClass = new StellarClassification();
        aClass.setStellarType(StellarType.A);

        aClass.setStarColor(StarColor.A);
        aClass.setStellarChromaticity(StellarChromaticity.A);

        aClass.setUpperTemperature(10000);
        aClass.setLowerTemperature(7500);

        aClass.setColor("white");
        aClass.setChromacity("blue white");

        aClass.setUpperMass(2.1);
        aClass.setLowerMass(1.4);

        aClass.setUpperRadius(1.8);
        aClass.setLowerRadius(1.4);

        aClass.setUpperLuminosity(25);
        aClass.setLowerLuminosity(5);

        aClass.setLines(HydrogenLines.STRONG);
        aClass.setSequenceFraction(.6);
        return aClass;
    }

    /**
     * create an F class generic star
     * <p>
     * F-type stars have strengthening H and K lines of Ca II. Neutral metals (Fe I, Cr I) beginning to gain on
     * ionized metal lines by late F. Their spectra are characterized by the weaker hydrogen lines and ionized
     * metals. Their color is white. About 1 in 33 (3.03%) of the main-sequence stars in the solar neighborhood
     * are F-type stars.
     * <p>
     * Spectral standards:
     * F0IIIa: Zeta Leonis
     * F0Ib: Alpha Leporis
     * F2V: 78 Ursae Majoris
     *
     * @return the star general description
     */
    public static StellarClassification createFClass() {
        StellarClassification fClass = new StellarClassification();
        fClass.setStellarType(StellarType.F);

        fClass.setStarColor(StarColor.F);
        fClass.setStellarChromaticity(StellarChromaticity.F);

        fClass.setUpperTemperature(7500);
        fClass.setLowerTemperature(6000);

        fClass.setColor("yellow white");
        fClass.setChromacity("white");

        fClass.setUpperMass(1.4);
        fClass.setLowerMass(1.04);

        fClass.setUpperRadius(1.4);
        fClass.setLowerRadius(1.15);

        fClass.setUpperLuminosity(5);
        fClass.setLowerLuminosity(1.5);

        fClass.setLines(HydrogenLines.MEDIUM);
        fClass.setSequenceFraction(3);
        return fClass;
    }

    /**
     * create an G class generic star
     * <p>
     * G-type stars, including the Sun[11] have prominent H and K lines of Ca II, which are most pronounced at G2.
     * They have even weaker hydrogen lines than F, but along with the ionized metals, they have neutral metals.
     * There is a prominent spike in the G band of CH molecules. Class G main-sequence stars make up about 7.5%,
     * nearly one in thirteen, of the main-sequence stars in the solar neighborhood.
     * <p>
     * G is host to the "Yellow Evolutionary Void".[59] Supergiant stars often swing between O or B (blue) and K or
     * M (red). While they do this, they do not stay for long in the yellow supergiant G classification as this is
     * an extremely unstable place for a supergiant to be.
     * <p>
     * Spectral standards:
     * G0V: Beta Canum Venaticorum
     * G0IV: Eta Boötis
     * G0Ib: Beta Aquarii
     * G2V: Sun
     * G5V: Kappa Ceti
     * G5IV: Mu Herculis
     * G5Ib: 9 Pegasi
     * G8V: 61 Ursae Majoris
     * G8IV: Beta Aquilae
     * G8IIIa: Kappa Geminorum
     * G8IIIab: Epsilon Virginis
     * G8Ib: Epsilon Geminorum
     *
     * @return the star general description
     */
    public static StellarClassification createGClass() {

        StellarClassification gClass = new StellarClassification();
        gClass.setStellarType(StellarType.G);

        gClass.setStarColor(StarColor.G);
        gClass.setStellarChromaticity(StellarChromaticity.G);

        gClass.setUpperTemperature(6000);
        gClass.setLowerTemperature(5200);

        gClass.setColor("yellow");
        gClass.setChromacity("yellowish white");

        gClass.setUpperMass(1.04);
        gClass.setLowerMass(0.8);

        gClass.setUpperRadius(1.15);
        gClass.setLowerRadius(0.96);

        gClass.setUpperLuminosity(1.5);
        gClass.setLowerLuminosity(0.6);

        gClass.setLines(HydrogenLines.WEAK);
        gClass.setSequenceFraction(7.6);

        return gClass;
    }

    /**
     * create an K class generic star
     * <p>
     * K-type stars are orangish stars that are slightly cooler than the Sun. They make up about 12%, nearly one
     * in eight, of the main-sequence stars in the solar neighborhood.[nb 5][9] There are also giant K-type stars,
     * which range from hypergiants like RW Cephei, to giants and supergiants, such as Arcturus, whereas orange
     * dwarfs, like Alpha Centauri B, are main-sequence stars.
     * <p>
     * They have extremely weak hydrogen lines, if they are present at all, and mostly neutral metals
     * (Mn I, Fe I, Si I). By late K, molecular bands of titanium oxide become present. There is a suggestion
     * that K Spectrum stars may potentially increase the chances of life developing on orbiting planets that
     * are within the habitable zone.
     * <p>
     * Spectral standards:
     * K0V: Sigma Draconis
     * K0III: Pollux
     * K0III: Epsilon Cygni
     * K2V: Epsilon Eridani
     * K2III: Kappa Ophiuchi
     * K3III: Rho Boötis
     * K5V: 61 Cygni A
     * K5III: Gamma Draconis
     *
     * @return the star general description
     */
    public static StellarClassification createKClass() {
        StellarClassification kClass = new StellarClassification();
        kClass.setStellarType(StellarType.K);

        kClass.setStarColor(StarColor.K);
        kClass.setStellarChromaticity(StellarChromaticity.K);

        kClass.setUpperTemperature(5200);
        kClass.setLowerTemperature(3700);

        kClass.setColor("orange");
        kClass.setChromacity("pale yellow orange");

        kClass.setUpperMass(0.8);
        kClass.setLowerMass(0.45);

        kClass.setUpperRadius(0.96);
        kClass.setLowerRadius(0.7);

        kClass.setUpperLuminosity(0.6);
        kClass.setLowerLuminosity(0.06);

        kClass.setLines(HydrogenLines.VERY_WEAK);
        kClass.setSequenceFraction(12.1);

        return kClass;
    }


    /**
     * create an M class generic star
     * <p>
     * Class M stars are by far the most common. About 76% of the main-sequence stars in the solar neighborhood
     * are class M stars. However, class M main-sequence stars (red dwarfs) have such low luminosities
     * that none are bright enough to be seen with the unaided eye, unless under exceptional conditions. The
     * brightest known M-class main-sequence star is M0V Lacaille 8760, with magnitude 6.6 (the limiting magnitude
     * for typical naked-eye visibility under good conditions is typically quoted as 6.5), and it is extremely
     * unlikely that any brighter examples will be found.
     * <p>
     * Although most class M stars are red dwarfs, most giants and some supergiants such as VY Canis Majoris,
     * Antares and Betelgeuse are also class M. Furthermore, the hotter brown dwarfs are late class M. This is
     * usually in the range of M6.5 to M9.5. The spectrum of a class M star contains lines from oxide molecules,
     * especially TiO, in the visible and all neutral metals, but absorption lines of hydrogen are usually absent.
     * TiO bands can be strong in class M stars, usually dominating their visible spectrum by about M5.
     * vanadium(II) oxide bands become present by late M.
     * <p>
     * Spectral standards:
     * M0IIIa: Beta Andromedae
     * M2III: Chi Pegasi
     * M1-M2Ia-Iab: Betelgeuse
     * M2Ia: Mu Cephei
     *
     * @return the star general description
     */
    public static StellarClassification createMClass() {
        StellarClassification mClass = new StellarClassification();
        mClass.setStellarType(StellarType.M);

        mClass.setStarColor(StarColor.M);
        mClass.setStellarChromaticity(StellarChromaticity.M);

        mClass.setUpperTemperature(3700);
        mClass.setLowerTemperature(2400);

        mClass.setColor("red");
        mClass.setChromacity("light orange red");

        mClass.setUpperMass(0.45);
        mClass.setLowerMass(0.08);

        mClass.setUpperRadius(0.7);
        mClass.setLowerRadius(0.1);

        mClass.setUpperLuminosity(0.08);
        mClass.setLowerLuminosity(0.001);

        mClass.setLines(HydrogenLines.VERY_WEAK);
        mClass.setSequenceFraction(76.45);

        return mClass;
    }

    private static StellarClassification createQClass() {
        StellarClassification qClass = new StellarClassification();
        qClass.setStellarType(StellarType.Q);

        qClass.setStarColor(StarColor.F);
        qClass.setStellarChromaticity(StellarChromaticity.F);

        qClass.setUpperTemperature(7500);
        qClass.setLowerTemperature(6000);

        qClass.setColor("yellow white");
        qClass.setChromacity("white");

        qClass.setUpperMass(1.4);
        qClass.setLowerMass(1.04);

        qClass.setUpperRadius(1.4);
        qClass.setLowerRadius(1.15);

        qClass.setUpperLuminosity(5);
        qClass.setLowerLuminosity(1.5);

        qClass.setLines(HydrogenLines.MEDIUM);
        qClass.setSequenceFraction(3);
        return qClass;
    }

    /**
     * create the stellar types
     */
    public static void createStellarTypes() {

        StellarClassification oClass = createOClass();
        stellarClassificationMap.put(oClass.getStellarType().toString(), oClass);
        stellarClasses.add(StellarType.O.toString());

        StellarClassification bClass = createBClass();
        stellarClassificationMap.put(bClass.getStellarType().toString(), bClass);
        stellarClasses.add(StellarType.B.toString());

        StellarClassification aClass = createAClass();
        stellarClassificationMap.put(aClass.getStellarType().toString(), aClass);
        stellarClasses.add(StellarType.A.toString());

        StellarClassification fClass = createFClass();
        stellarClassificationMap.put(fClass.getStellarType().toString(), fClass);
        stellarClasses.add(StellarType.F.toString());

        StellarClassification gClass = createGClass();
        stellarClassificationMap.put(gClass.getStellarType().toString(), gClass);
        stellarClasses.add(StellarType.G.toString());

        StellarClassification kClass = createKClass();
        stellarClassificationMap.put(kClass.getStellarType().toString(), kClass);
        stellarClasses.add(StellarType.K.toString());

        StellarClassification mClass = createMClass();
        stellarClassificationMap.put(mClass.getStellarType().toString(), mClass);
        stellarClasses.add(StellarType.M.toString());

        StellarClassification qClass = createQClass();
        stellarClassificationMap.put(qClass.getStellarType().toString(), qClass);
        stellarClasses.add(StellarType.Q.toString());

    }


    /**
     * get the stellar class based on the type
     *
     * @param stellarClass the string form of the stellar class
     * @return the stellar classification
     */
    public StellarClassification getStellarClass(String stellarClass) {
        return stellarClassificationMap.get(stellarClass);
    }

    /**
     * whether the class exists
     *
     * @param stellarClass the stellar calss
     * @return true is it is there
     */
    public boolean classes(String stellarClass) {
        return stellarClasses.contains(stellarClass);
    }

}
