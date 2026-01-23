package com.teamgannon.trips.stellarmodelling;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Factory for creating stellar classifications.
 * <p>
 * Uses {@link StellarTypeRegistry} for stellar type data definitions.
 * <p>
 * Created by larrymitchell on 2017-02-19.
 */
public class StellarFactory {

    /**
     * The factory singleton.
     */
    private static StellarFactory factory;

    /**
     * Stellar classifications indexed by type string.
     */
    private static Map<String, StellarClassification> stellarClassificationMap = new HashMap<>();

    /**
     * Set of all stellar class type strings.
     */
    private static Set<String> stellarClasses = new HashSet<>();

    public StellarFactory() {
        createStellarTypes();
    }

    /**
     * Get the factory singleton.
     *
     * @return the factory
     */
    public static StellarFactory getFactory() {
        stellarClassificationMap = new HashMap<>();
        stellarClasses = new HashSet<>();
        factory = new StellarFactory();
        return factory;
    }

    /**
     * Create a stellar classification from a StellarTypeData record.
     *
     * @param data the stellar type data
     * @return the stellar classification
     */
    public static StellarClassification createFromData(StellarTypeData data) {
        StellarClassification classification = new StellarClassification();
        classification.setStellarType(data.stellarType());
        classification.setStarColor(data.starColor());
        classification.setStellarChromaticity(StellarTypeRegistry.getChromaticityColor(data.chromaticityKey()));
        classification.setColor(data.color());
        classification.setChromacity(data.chromacity());
        classification.setLowerTemperature(data.lowerTemperature());
        classification.setUpperTemperature(data.upperTemperature());
        classification.setLowerMass(data.lowerMass());
        classification.setUpperMass(data.upperMass());
        classification.setLowerRadius(data.lowerRadius());
        classification.setUpperRadius(data.upperRadius());
        classification.setLowerLuminosity(data.lowerLuminosity());
        classification.setUpperLuminosity(data.upperLuminosity());
        classification.setLines(data.hydrogenLines());
        classification.setSequenceFraction(data.sequenceFraction());
        return classification;
    }

    /**
     * Create all stellar types from the registry.
     */
    public static void createStellarTypes() {
        for (StellarTypeData data : StellarTypeRegistry.getAllData()) {
            StellarClassification classification = createFromData(data);
            String typeKey = data.stellarType().toString();
            stellarClassificationMap.put(typeKey, classification);
            stellarClasses.add(typeKey);
        }

        // Special case: Q class uses different key in map
        StellarTypeData qData = StellarTypeRegistry.get(StellarType.Q.toString());
        if (qData != null) {
            StellarClassification qClass = createFromData(qData);
            stellarClassificationMap.put(StellarType.Q.toString(), qClass);
            stellarClasses.add(StellarType.Q.toString());
        }
    }

    /**
     * Get the stellar classification for a given type.
     *
     * @param stellarClass the stellar class string
     * @return the stellar classification, or null if not found
     */
    public StellarClassification getStellarClass(String stellarClass) {
        return stellarClassificationMap.get(stellarClass);
    }

    /**
     * Check if a stellar class exists.
     *
     * @param stellarClass the stellar class string
     * @return true if the class exists
     */
    public boolean classes(String stellarClass) {
        return stellarClasses.contains(stellarClass);
    }

    // =========================================================================
    // Legacy Static Factory Methods (for backwards compatibility)
    // =========================================================================

    /**
     * Create O class stellar classification.
     * @return the O class classification
     */
    public static StellarClassification createOClass() {
        return createFromData(StellarTypeRegistry.O_CLASS);
    }

    /**
     * Create B class stellar classification.
     * @return the B class classification
     */
    public static StellarClassification createBClass() {
        return createFromData(StellarTypeRegistry.B_CLASS);
    }

    /**
     * Create A class stellar classification.
     * @return the A class classification
     */
    public static StellarClassification createAClass() {
        return createFromData(StellarTypeRegistry.A_CLASS);
    }

    /**
     * Create F class stellar classification.
     * @return the F class classification
     */
    public static StellarClassification createFClass() {
        return createFromData(StellarTypeRegistry.F_CLASS);
    }

    /**
     * Create G class stellar classification.
     * @return the G class classification
     */
    public static StellarClassification createGClass() {
        return createFromData(StellarTypeRegistry.G_CLASS);
    }

    /**
     * Create K class stellar classification.
     * @return the K class classification
     */
    public static StellarClassification createKClass() {
        return createFromData(StellarTypeRegistry.K_CLASS);
    }

    /**
     * Create M class stellar classification.
     * @return the M class classification
     */
    public static StellarClassification createMClass() {
        return createFromData(StellarTypeRegistry.M_CLASS);
    }
}
