package com.teamgannon.trips.stellarmodelling;

import javafx.scene.paint.Color;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Registry of all stellar type configurations.
 * Contains data definitions for all stellar classifications in a data-driven format.
 * <p>
 * This replaces the repetitive createXClass() methods in the original StellarFactory
 * with declarative data definitions.
 */
public class StellarTypeRegistry {

    /**
     * Chromaticity map: stellar type key -> RGB values as "r,g,b" string.
     */
    private static final Map<String, String> CHROMATICITY_MAP = Stream.of(new String[][]{
            {"O", "157,180,254"},
            {"B", "170,191,255"},
            {"A", "202,216,255"},
            {"F", "255,255,255"},
            {"G", "255,244,232"},
            {"K", "255,222,180"},
            {"M", "157,180,254"},
            {"C", "157,180,254"},
            {"Unknown", "157,180,254"},
            {"WN", "170,191,255"},
            {"WO", "170,191,255"},
            {"WC", "170,191,255"},
            {"L", "157,180,254"},
            {"Y", "157,180,254"},
            {"S", "170,191,255"},
            {"T", "157,180,254"},
            {"D", "202,216,255"},
            {"DA", "202,216,255"},
            {"DB", "202,216,255"},
            {"DO", "202,216,255"},
            {"DQ", "202,216,255"},
            {"DZ", "202,216,255"},
            {"DC", "202,216,255"},
            {"DX", "202,216,255"},
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    // =========================================================================
    // Main Sequence Stars (O, B, A, F, G, K, M)
    // =========================================================================

    /**
     * O-type: Very hot and extremely luminous blue stars.
     * About 1 in 3,000,000 of main-sequence stars.
     */
    public static final StellarTypeData O_CLASS = StellarTypeData.builder()
            .stellarType(StellarType.O)
            .starColor(StarColor.O)
            .chromaticityKey("O")
            .color("blue")
            .chromacity("blue")
            .temperatureRange(30000, 70000)
            .massRange(16.0, 400)
            .radiusRange(6.6, 45.0)
            .luminosityRange(30000.0, 10000000.0)
            .hydrogenLines(HydrogenLines.WEAK)
            .sequenceFraction(0.00003)
            .build();

    /**
     * B-type: Very luminous blue-white stars.
     * About 0.125% of main-sequence stars.
     */
    public static final StellarTypeData B_CLASS = StellarTypeData.builder()
            .stellarType(StellarType.B)
            .starColor(StarColor.B)
            .chromaticityKey("B")
            .color("blue white")
            .chromacity("deep blue white")
            .temperatureRange(10000, 30000)
            .massRange(2.1, 16)
            .radiusRange(1.8, 6.6)
            .luminosityRange(25, 30000)
            .hydrogenLines(HydrogenLines.MEDIUM)
            .sequenceFraction(0.13)
            .build();

    /**
     * A-type: White or bluish-white stars with strong hydrogen lines.
     * About 0.625% of main-sequence stars.
     */
    public static final StellarTypeData A_CLASS = StellarTypeData.builder()
            .stellarType(StellarType.A)
            .starColor(StarColor.A)
            .chromaticityKey("A")
            .color("white")
            .chromacity("blue white")
            .temperatureRange(7500, 10000)
            .massRange(1.4, 2.1)
            .radiusRange(1.4, 1.8)
            .luminosityRange(5, 25)
            .hydrogenLines(HydrogenLines.STRONG)
            .sequenceFraction(0.6)
            .build();

    /**
     * F-type: White stars with weaker hydrogen lines.
     * About 3.03% of main-sequence stars.
     */
    public static final StellarTypeData F_CLASS = StellarTypeData.builder()
            .stellarType(StellarType.F)
            .starColor(StarColor.F)
            .chromaticityKey("F")
            .color("yellow white")
            .chromacity("white")
            .temperatureRange(6000, 7500)
            .massRange(1.04, 1.4)
            .radiusRange(1.15, 1.4)
            .luminosityRange(1.5, 5)
            .hydrogenLines(HydrogenLines.MEDIUM)
            .sequenceFraction(3)
            .build();

    /**
     * G-type: Yellow stars like the Sun.
     * About 7.5% of main-sequence stars.
     */
    public static final StellarTypeData G_CLASS = StellarTypeData.builder()
            .stellarType(StellarType.G)
            .starColor(StarColor.G)
            .chromaticityKey("G")
            .color("yellow")
            .chromacity("yellowish white")
            .temperatureRange(5200, 6000)
            .massRange(0.8, 1.04)
            .radiusRange(0.96, 1.15)
            .luminosityRange(0.6, 1.5)
            .hydrogenLines(HydrogenLines.WEAK)
            .sequenceFraction(7.6)
            .build();

    /**
     * K-type: Orange stars, slightly cooler than the Sun.
     * About 12% of main-sequence stars.
     */
    public static final StellarTypeData K_CLASS = StellarTypeData.builder()
            .stellarType(StellarType.K)
            .starColor(StarColor.K)
            .chromaticityKey("K")
            .color("orange")
            .chromacity("pale yellow orange")
            .temperatureRange(3700, 5200)
            .massRange(0.45, 0.8)
            .radiusRange(0.7, 0.96)
            .luminosityRange(0.06, 0.6)
            .hydrogenLines(HydrogenLines.VERY_WEAK)
            .sequenceFraction(12.1)
            .build();

    /**
     * M-type: Red dwarf stars, most common type.
     * About 76% of main-sequence stars.
     */
    public static final StellarTypeData M_CLASS = StellarTypeData.builder()
            .stellarType(StellarType.M)
            .starColor(StarColor.M)
            .chromaticityKey("M")
            .color("red")
            .chromacity("light orange red")
            .temperatureRange(2400, 3700)
            .massRange(0.08, 0.45)
            .radiusRange(0.1, 0.7)
            .luminosityRange(0.001, 0.08)
            .hydrogenLines(HydrogenLines.VERY_WEAK)
            .sequenceFraction(76.45)
            .build();

    // =========================================================================
    // Brown Dwarf Stars (L, T, Y)
    // =========================================================================

    /**
     * L-type: Cool brown dwarf stars.
     */
    public static final StellarTypeData L_CLASS = StellarTypeData.builder()
            .stellarType(StellarType.L)
            .starColor(StarColor.L)
            .chromaticityKey("L")
            .color("red")
            .chromacity("red")
            .temperatureRange(1300, 2400)
            .massRange(0.070, 0.085)
            .radiusRange(0.088, 0.110)
            .luminosityRange(0.00006, 0.00031)
            .hydrogenLines(HydrogenLines.VERY_WEAK)
            .sequenceFraction(14.45)
            .build();

    /**
     * T-type: Methane brown dwarfs.
     */
    public static final StellarTypeData T_CLASS = StellarTypeData.builder()
            .stellarType(StellarType.T)
            .starColor(StarColor.T)
            .chromaticityKey("T")
            .color("magenta")
            .chromacity("magenta")
            .temperatureRange(700, 1300)
            .massRange(0.070, 0.085)
            .radiusRange(0.088, 0.110)
            .luminosityRange(0.00006, 0.00031)
            .hydrogenLines(HydrogenLines.VERY_WEAK)
            .sequenceFraction(12.45)
            .build();

    /**
     * Y-type: Coolest brown dwarfs.
     */
    public static final StellarTypeData Y_CLASS = StellarTypeData.builder()
            .stellarType(StellarType.Y)
            .starColor(StarColor.Y)
            .chromaticityKey("Y")
            .color("infared")
            .chromacity("infrared")
            .temperatureRange(500, 700)
            .massRange(0.040, 0.065)
            .radiusRange(0.050, 0.080)
            .luminosityRange(0.00001, 0.00006)
            .hydrogenLines(HydrogenLines.VERY_WEAK)
            .sequenceFraction(10.45)
            .build();

    // =========================================================================
    // White Dwarf Stars (D, DA, DB, DO, DQ, DZ, DC, DX)
    // =========================================================================

    /**
     * D-type: Generic white dwarf.
     */
    public static final StellarTypeData D_CLASS = StellarTypeData.builder()
            .stellarType(StellarType.D)
            .starColor(StarColor.D)
            .chromaticityKey("D")
            .color("white")
            .chromacity("white")
            .temperatureRange(6000, 50000)
            .massRange(0.08, 0.45)
            .radiusRange(0.1, 0.7)
            .luminosityRange(0.001, 0.08)
            .hydrogenLines(HydrogenLines.STRONG)
            .sequenceFraction(29)
            .build();

    /**
     * DA-type: White dwarf with hydrogen atmosphere.
     */
    public static final StellarTypeData DA_CLASS = StellarTypeData.builder()
            .stellarType(StellarType.DA)
            .starColor(StarColor.DA)
            .chromaticityKey("DA")
            .color("white")
            .chromacity("white")
            .temperatureRange(6000, 50000)
            .massRange(0.08, 0.45)
            .radiusRange(0.1, 0.7)
            .luminosityRange(0.001, 0.08)
            .hydrogenLines(HydrogenLines.STRONG)
            .sequenceFraction(29)
            .build();

    /**
     * DB-type: White dwarf with helium atmosphere.
     */
    public static final StellarTypeData DB_CLASS = StellarTypeData.builder()
            .stellarType(StellarType.DB)
            .starColor(StarColor.DB)
            .chromaticityKey("DB")
            .color("white")
            .chromacity("white")
            .temperatureRange(6000, 50000)
            .massRange(0.08, 0.45)
            .radiusRange(0.1, 0.7)
            .luminosityRange(0.001, 0.08)
            .hydrogenLines(HydrogenLines.STRONG)
            .sequenceFraction(5.1)
            .build();

    /**
     * DO-type: White dwarf with ionized helium.
     */
    public static final StellarTypeData DO_CLASS = StellarTypeData.builder()
            .stellarType(StellarType.DO)
            .starColor(StarColor.DO)
            .chromaticityKey("DO")
            .color("white")
            .chromacity("white")
            .temperatureRange(6000, 50000)
            .massRange(0.08, 0.45)
            .radiusRange(0.1, 0.7)
            .luminosityRange(0.001, 0.08)
            .hydrogenLines(HydrogenLines.MEDIUM)
            .sequenceFraction(76.45)
            .build();

    /**
     * DQ-type: White dwarf with carbon atmosphere.
     */
    public static final StellarTypeData DQ_CLASS = StellarTypeData.builder()
            .stellarType(StellarType.DQ)
            .starColor(StarColor.DQ)
            .chromaticityKey("DQ")
            .color("white")
            .chromacity("white")
            .temperatureRange(12000, 18000)
            .massRange(0.08, 0.45)
            .radiusRange(0.1, 0.7)
            .luminosityRange(0.001, 0.08)
            .hydrogenLines(HydrogenLines.WEAK)
            .sequenceFraction(0.02)
            .build();

    /**
     * DZ-type: White dwarf with metal-rich atmosphere.
     */
    public static final StellarTypeData DZ_CLASS = StellarTypeData.builder()
            .stellarType(StellarType.DZ)
            .starColor(StarColor.DZ)
            .chromaticityKey("DZ")
            .color("white")
            .chromacity("white")
            .temperatureRange(6000, 50000)
            .massRange(0.08, 0.45)
            .radiusRange(0.1, 0.7)
            .luminosityRange(0.001, 0.08)
            .hydrogenLines(HydrogenLines.WEAK)
            .sequenceFraction(76.45)
            .build();

    /**
     * DC-type: White dwarf with continuous spectrum.
     */
    public static final StellarTypeData DC_CLASS = StellarTypeData.builder()
            .stellarType(StellarType.DC)
            .starColor(StarColor.DC)
            .chromaticityKey("DC")
            .color("white")
            .chromacity("white")
            .temperatureRange(6000, 50000)
            .massRange(0.08, 0.45)
            .radiusRange(0.1, 0.7)
            .luminosityRange(0.001, 0.08)
            .hydrogenLines(HydrogenLines.MEDIUM)
            .sequenceFraction(3.8)
            .build();

    /**
     * DX-type: White dwarf with unclassified spectrum.
     */
    public static final StellarTypeData DX_CLASS = StellarTypeData.builder()
            .stellarType(StellarType.DX)
            .starColor(StarColor.DX)
            .chromaticityKey("DX")
            .color("white")
            .chromacity("white")
            .temperatureRange(6000, 50000)
            .massRange(0.08, 0.45)
            .radiusRange(0.1, 0.7)
            .luminosityRange(0.001, 0.08)
            .hydrogenLines(HydrogenLines.VERY_WEAK)
            .sequenceFraction(76.45)
            .build();

    // =========================================================================
    // Wolf-Rayet Stars (WN, WC, WO)
    // =========================================================================

    /**
     * WN-type: Wolf-Rayet star with nitrogen emission.
     */
    public static final StellarTypeData WN_CLASS = StellarTypeData.builder()
            .stellarType(StellarType.WN)
            .starColor(StarColor.WN)
            .chromaticityKey("WN")
            .color("blue")
            .chromacity("blue")
            .temperatureRange(35000, 141000)
            .massRange(16, 200)
            .radiusRange(2.3, 25)
            .luminosityRange(160000, 5000000)
            .hydrogenLines(HydrogenLines.STRONG)
            .sequenceFraction(0.1)
            .build();

    /**
     * WC-type: Wolf-Rayet star with carbon emission.
     */
    public static final StellarTypeData WC_CLASS = StellarTypeData.builder()
            .stellarType(StellarType.WC)
            .starColor(StarColor.WC)
            .chromaticityKey("WC")
            .color("blue")
            .chromacity("blue")
            .temperatureRange(44000, 117000)
            .massRange(11, 22)
            .radiusRange(0.9, 8.7)
            .luminosityRange(158000, 501000)
            .hydrogenLines(HydrogenLines.STRONG)
            .sequenceFraction(0.1)
            .build();

    /**
     * WO-type: Wolf-Rayet star with oxygen emission.
     */
    public static final StellarTypeData WO_CLASS = StellarTypeData.builder()
            .stellarType(StellarType.WO)
            .starColor(StarColor.WO)
            .chromaticityKey("WO")
            .color("blue")
            .chromacity("blue")
            .temperatureRange(44000, 200000)
            .massRange(11, 22)
            .radiusRange(0.9, 8.7)
            .luminosityRange(158000, 630000)
            .hydrogenLines(HydrogenLines.STRONG)
            .sequenceFraction(0.1)
            .build();

    // =========================================================================
    // Carbon and S-type Stars
    // =========================================================================

    /**
     * C-type: Carbon stars (red giants).
     */
    public static final StellarTypeData C_CLASS = StellarTypeData.builder()
            .stellarType(StellarType.C)
            .starColor(StarColor.C)
            .chromaticityKey("C")
            .color("red")
            .chromacity("red")
            .temperatureRange(3150, 4000)
            .massRange(1.5, 5)
            .radiusRange(2.3, 25)
            .luminosityRange(5, 10000)
            .hydrogenLines(HydrogenLines.STRONG)
            .sequenceFraction(0.3)
            .build();

    /**
     * S-type: Cool giant with zirconium oxide bands.
     */
    public static final StellarTypeData S_CLASS = StellarTypeData.builder()
            .stellarType(StellarType.S)
            .starColor(StarColor.S)
            .chromaticityKey("S")
            .color("blue")
            .chromacity("blue")
            .temperatureRange(3150, 4000)
            .massRange(1.5, 5)
            .radiusRange(2.3, 25)
            .luminosityRange(5, 10000)
            .hydrogenLines(HydrogenLines.STRONG)
            .sequenceFraction(0.3)
            .build();

    // =========================================================================
    // Special Types
    // =========================================================================

    /**
     * Q-type: Novae (temporary classification).
     */
    public static final StellarTypeData Q_CLASS = StellarTypeData.builder()
            .stellarType(StellarType.Y)  // Note: original code used Y for Q
            .starColor(StarColor.F)
            .chromaticityKey("F")
            .color("yellow white")
            .chromacity("white")
            .temperatureRange(6000, 7500)
            .massRange(1.04, 1.4)
            .radiusRange(1.15, 1.4)
            .luminosityRange(1.5, 5)
            .hydrogenLines(HydrogenLines.MEDIUM)
            .sequenceFraction(3)
            .build();

    /**
     * Unknown-type: Unclassified stars.
     */
    public static final StellarTypeData UNKNOWN_CLASS = StellarTypeData.builder()
            .stellarType(StellarType.Unk)
            .starColor(StarColor.Unknown)
            .chromaticityKey("Unknown")
            .color("red")
            .chromacity("red")
            .temperatureRange(2400, 3700)
            .massRange(0.08, 0.45)
            .radiusRange(0.1, 0.7)
            .luminosityRange(0.001, 0.08)
            .hydrogenLines(HydrogenLines.VERY_WEAK)
            .sequenceFraction(76.45)
            .build();

    // =========================================================================
    // Registry Access
    // =========================================================================

    /**
     * All stellar type data definitions, indexed by stellar type string.
     */
    private static final Map<String, StellarTypeData> REGISTRY = new HashMap<>();

    static {
        // Main sequence
        register(O_CLASS);
        register(B_CLASS);
        register(A_CLASS);
        register(F_CLASS);
        register(G_CLASS);
        register(K_CLASS);
        register(M_CLASS);

        // Brown dwarfs
        register(L_CLASS);
        register(T_CLASS);
        register(Y_CLASS);

        // White dwarfs
        register(D_CLASS);
        register(DA_CLASS);
        register(DB_CLASS);
        register(DO_CLASS);
        register(DQ_CLASS);
        register(DZ_CLASS);
        register(DC_CLASS);
        register(DX_CLASS);

        // Wolf-Rayet
        register(WN_CLASS);
        register(WC_CLASS);
        register(WO_CLASS);

        // Carbon and S-type
        register(C_CLASS);
        register(S_CLASS);

        // Special
        REGISTRY.put(StellarType.Q.toString(), Q_CLASS);
        register(UNKNOWN_CLASS);
    }

    private static void register(StellarTypeData data) {
        REGISTRY.put(data.stellarType().toString(), data);
    }

    /**
     * Get the stellar type data for a given stellar type string.
     *
     * @param stellarType the stellar type string (e.g., "O", "G", "DA")
     * @return the stellar type data, or null if not found
     */
    public static StellarTypeData get(String stellarType) {
        return REGISTRY.get(stellarType);
    }

    /**
     * Get all registered stellar types.
     *
     * @return unmodifiable set of all stellar type strings
     */
    public static Set<String> getAllTypes() {
        return Collections.unmodifiableSet(REGISTRY.keySet());
    }

    /**
     * Get all stellar type data entries.
     *
     * @return unmodifiable collection of all stellar type data
     */
    public static Collection<StellarTypeData> getAllData() {
        return Collections.unmodifiableCollection(REGISTRY.values());
    }

    /**
     * Check if a stellar type is registered.
     *
     * @param stellarType the stellar type string
     * @return true if registered
     */
    public static boolean contains(String stellarType) {
        return REGISTRY.containsKey(stellarType);
    }

    /**
     * Get the chromaticity map.
     *
     * @return unmodifiable chromaticity map
     */
    public static Map<String, String> getChromaticityMap() {
        return Collections.unmodifiableMap(CHROMATICITY_MAP);
    }

    /**
     * Parse an RGB string to a JavaFX Color.
     *
     * @param rgbString the RGB string in format "r,g,b"
     * @return the Color, or MEDIUMVIOLETRED if parsing fails
     */
    public static Color parseColor(String rgbString) {
        String[] parts = rgbString.split(",");
        try {
            int red = Integer.parseInt(parts[0]);
            int green = Integer.parseInt(parts[1]);
            int blue = Integer.parseInt(parts[2]);
            return Color.rgb(red, green, blue);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            return Color.MEDIUMVIOLETRED;
        }
    }

    /**
     * Get the chromaticity color for a stellar type.
     *
     * @param stellarType the stellar type string
     * @return the chromaticity color
     */
    public static Color getChromaticityColor(String stellarType) {
        String rgb = CHROMATICITY_MAP.get(stellarType);
        if (rgb == null) {
            rgb = CHROMATICITY_MAP.get("Unknown");
        }
        return parseColor(rgb);
    }
}
