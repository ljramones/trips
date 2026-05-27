package com.teamgannon.trips.dialogs.solarsystem;

import com.teamgannon.trips.planetary.modelling.procedural.JavaFxPlanetMeshConverter.TerrainType;

/**
 * Maps a planet's physical context (declared type, surface temperature, water /
 * ice fractions, density, orbital distance) to one of the renderer's
 * {@link TerrainType}s.
 * <p>
 * Extracted from {@code ProceduralPlanetViewerDialog} in Phase 4.2 of the
 * codebase-review remediation. Pure functions, no JavaFX dependencies — safe
 * to call from any thread.
 */
public final class PlanetTerrainClassifier {

    /** Frost-line distance (AU) — beyond this volatiles freeze readily. */
    private static final double FROST_LINE_AU = 2.7;

    /** Threshold (K) below which any water locks up as ice. */
    private static final double DEEP_FREEZE_K = 150.0;

    /** Threshold (K) below which cold-world classifiers kick in. */
    private static final double COLD_K = 200.0;

    /** Liquid-water threshold (K). */
    private static final double FREEZING_K = 273.0;

    /** Density (g/cm³) below which a body is presumed ice-rich. */
    private static final double LOW_DENSITY = 2.5;

    /** Even lower density: almost certainly ice-dominated (Enceladus 1.61, Pluto 1.85). */
    private static final double VERY_LOW_DENSITY = 2.0;

    private PlanetTerrainClassifier() {
    }

    /**
     * Inputs to {@link #classify}. All fields nullable except primitives —
     * {@code waterFraction} and {@code iceCoverFraction} are required (0..1
     * floats), {@code surfaceTemperatureK} is required, the rest are
     * "best-effort" hints.
     *
     * @param planetType           free-form planet type string (e.g. "Gas Giant", "Ice", "Terrestrial"); nullable
     * @param surfaceTemperatureK  surface temperature in K
     * @param waterFraction        liquid-water surface fraction (0..1)
     * @param iceCoverFraction     ice-cover fraction (0..1)
     * @param densityGcm3          mean density in g/cm³; nullable
     * @param semiMajorAxisAU      orbital semi-major axis in AU; nullable
     */
    public record Inputs(String planetType,
                         double surfaceTemperatureK,
                         double waterFraction,
                         double iceCoverFraction,
                         Double densityGcm3,
                         Double semiMajorAxisAU) {
    }

    /**
     * Classify a planet's terrain by walking a priority list of indicators.
     * <p>
     * Priorities (first match wins):
     * <ol>
     *   <li>declared planet type ⇒ JOVIAN / ICE_GIANT / ICE</li>
     *   <li>{@link #isIcyWorld(Inputs)} ⇒ ICE</li>
     *   <li>no water + no ice ⇒ DRY</li>
     *   <li>fallback ⇒ WET</li>
     * </ol>
     */
    public static TerrainType classify(Inputs in) {
        if (in.planetType() != null) {
            String typeLower = in.planetType().toLowerCase();
            if (typeLower.contains("gas giant") || typeLower.contains("jovian")) {
                return TerrainType.JOVIAN;
            }
            if (typeLower.contains("ice giant")) {
                return TerrainType.ICE_GIANT;
            }
            if (typeLower.contains("ice") && !typeLower.contains("giant")) {
                return TerrainType.ICE;
            }
        }
        if (isIcyWorld(in)) {
            return TerrainType.ICE;
        }
        if (in.waterFraction() < 0.05 && in.iceCoverFraction() < 0.1) {
            return TerrainType.DRY;
        }
        return TerrainType.WET;
    }

    /**
     * Determine if the body is icy via multiple physical indicators:
     * <ol>
     *   <li>explicit ice cover &gt; 30%;</li>
     *   <li>low density (&lt; {@value #LOW_DENSITY}) + cold (&lt; {@value #COLD_K} K);</li>
     *   <li>very low density (&lt; {@value #VERY_LOW_DENSITY}) — Enceladus / Pluto territory;</li>
     *   <li>beyond the frost line + cold + any volatiles;</li>
     *   <li>deep-freeze cold (&lt; {@value #DEEP_FREEZE_K} K) + any volatiles;</li>
     *   <li>cold (&lt; {@value #FREEZING_K} K) + significant volatiles.</li>
     * </ol>
     */
    public static boolean isIcyWorld(Inputs in) {
        if (in.iceCoverFraction() > 0.3) {
            return true;
        }
        if (in.densityGcm3() != null && in.densityGcm3() < LOW_DENSITY && in.surfaceTemperatureK() < COLD_K) {
            return true;
        }
        if (in.densityGcm3() != null && in.densityGcm3() < VERY_LOW_DENSITY) {
            return true;
        }
        if (in.semiMajorAxisAU() != null && in.semiMajorAxisAU() > FROST_LINE_AU
                && in.surfaceTemperatureK() < COLD_K
                && (in.waterFraction() > 0.01 || in.iceCoverFraction() > 0.05)) {
            return true;
        }
        if (in.surfaceTemperatureK() < DEEP_FREEZE_K
                && (in.waterFraction() > 0.01 || in.iceCoverFraction() > 0.05)) {
            return true;
        }
        return in.surfaceTemperatureK() < FREEZING_K
                && (in.waterFraction() > 0.05 || in.iceCoverFraction() > 0.1);
    }
}
