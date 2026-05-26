package com.teamgannon.trips.stellarmodelling;

import lombok.extern.slf4j.Slf4j;

/**
 * Normalises a stellar mass to solar-mass units (Sun = 1.0), regardless of
 * whether the source provided kg or M☉.
 * <p>
 * Real stars max out around ~150 M☉, so any value above {@link #KG_THRESHOLD}
 * must have been authored in kilograms. The heuristic matches what
 * {@code TransferCalculator.toSolarMasses} used to do at the UI boundary
 * before Phase 1.1.
 * <p>
 * History: CSV/ChView imports historically wrote raw kg into
 * {@code StarObject.mass} (see {@code 30ly.trips.csv} where the Sun appears as
 * {@code 1.99E30}) despite the column being documented as solar masses.
 * Phase 1.1 of the codebase-review remediation made solar masses canonical:
 * the V2 Flyway migration converts legacy rows, and this normaliser is
 * applied at every CSV/ChView/external ingestion boundary so kg can no longer
 * enter the database.
 * <p>
 * Once all known import paths have been audited and a deprecation cycle has
 * passed, this helper can be inlined to identity.
 */
@Slf4j
public final class StarMassNormalizer {

    /** One solar mass in kilograms. */
    public static final double SOLAR_MASS_KG = 1.989e30;

    /** Threshold above which an input is assumed to be in kg, not solar masses. */
    public static final double KG_THRESHOLD = 1000.0;

    private StarMassNormalizer() {
    }

    /**
     * Normalise a raw mass to solar masses.
     *
     * @param rawMass input mass — may be solar masses, kg, or 0
     * @return mass in solar masses; 0 if the input was 0 or negative
     */
    public static double toSolarMasses(double rawMass) {
        if (rawMass <= 0) {
            return 0.0;
        }
        if (rawMass > KG_THRESHOLD) {
            double normalised = rawMass / SOLAR_MASS_KG;
            log.warn("Mass {} appears to be in kg; normalising to {} M_sun", rawMass, normalised);
            return normalised;
        }
        return rawMass;
    }
}
