package com.teamgannon.trips.planetary.modelling.procedural;

import com.teamgannon.trips.planetary.modelling.procedural.JavaFxPlanetMeshConverter.TerrainType;
import javafx.scene.paint.Color;

/**
 * Colour lookup tables and interpolators for procedural-planet rendering.
 * <p>
 * Extracted from {@code JavaFxPlanetMeshConverter} in Phase 4.5 of the
 * codebase-review remediation. Pure functions over the existing
 * {@link TerrainType} enum — no mesh logic, no JavaFX scene-graph state.
 *
 * <h2>Palettes</h2>
 * Each palette has 9 colour stops representing elevation {@code -4..+4}
 * (or cloud-band layer for gas giants). The fall-back for an unrecognised
 * {@code TerrainType} is the {@link TerrainType#WET WET} palette.
 *
 * <h2>Rainfall</h2>
 * The rainfall palette is independent of terrain type and uses 8 stops from
 * brown (dry) through green to dark blue (wet). Bucket indices outside
 * {@code [0, 7]} are clamped.
 */
public final class PlanetColorPalette {

    /** Height colour mapping for WET planets — liquid water; negative elevations are oceans. */
    private static final Color[] HEIGHT_COLORS = {
            Color.rgb(0, 0, 102),     // -4: deep ocean
            Color.rgb(0, 0, 128),     // -3: ocean
            Color.rgb(51, 77, 230),   // -2: shallow ocean
            Color.rgb(102, 153, 255), // -1: coastal
            Color.rgb(204, 204, 153), //  0: lowlands
            Color.rgb(166, 204, 102), //  1: plains
            Color.rgb(166, 153, 102), //  2: hills
            Color.rgb(102, 51, 0),    //  3: mountains
            Color.rgb(51, 0, 0)       //  4: high mountains
    };

    /** Height colour mapping for DRY planets — no water; canyons / basins / dust. */
    private static final Color[] DRY_TERRAIN_COLORS = {
            Color.rgb(89, 60, 31),    // -4: deep canyon (dark brown)
            Color.rgb(120, 85, 50),   // -3: canyon floor
            Color.rgb(150, 110, 70),  // -2: shallow basin (tan)
            Color.rgb(180, 145, 100), // -1: low depression
            Color.rgb(204, 180, 140), //  0: lowlands (sandy)
            Color.rgb(190, 170, 120), //  1: plains (dusty tan)
            Color.rgb(160, 140, 100), //  2: hills
            Color.rgb(110, 80, 50),   //  3: mountains
            Color.rgb(70, 50, 30)     //  4: high mountains
    };

    /** Height colour mapping for ICE planets — Europa / Enceladus / Pluto-like. */
    private static final Color[] ICE_TERRAIN_COLORS = {
            Color.rgb(140, 160, 180), // -4: deep crevasse
            Color.rgb(160, 180, 200), // -3: ice canyon
            Color.rgb(180, 200, 220), // -2: ice basin
            Color.rgb(200, 215, 230), // -1: low ice plain
            Color.rgb(220, 230, 240), //  0: ice flats
            Color.rgb(235, 240, 250), //  1: ice plains
            Color.rgb(245, 248, 255), //  2: ice ridges
            Color.rgb(255, 255, 255), //  3: ice mountains
            Color.rgb(240, 245, 255)  //  4: high ice peaks
    };

    /** Cloud-band colours for JOVIAN gas giants (Jupiter / Saturn). "Heights" are cloud layers. */
    private static final Color[] JOVIAN_COLORS = {
            Color.rgb(120, 80, 60),   // -4: deep brown belt
            Color.rgb(150, 100, 70),  // -3: brown belt
            Color.rgb(180, 130, 90),  // -2: tan belt
            Color.rgb(200, 160, 120), // -1: light tan zone
            Color.rgb(230, 200, 170), //  0: cream zone
            Color.rgb(245, 230, 200), //  1: pale cream zone
            Color.rgb(255, 245, 220), //  2: white zone
            Color.rgb(220, 150, 100), //  3: orange storm band
            Color.rgb(180, 100, 60)   //  4: deep orange / Great-Red-Spot
    };

    /** Cloud-band colours for ICE_GIANT planets (Neptune / Uranus). */
    private static final Color[] ICE_GIANT_COLORS = {
            Color.rgb(20, 50, 100),   // -4: deep blue
            Color.rgb(30, 70, 130),   // -3: dark blue
            Color.rgb(50, 100, 160),  // -2: medium blue
            Color.rgb(70, 130, 190),  // -1: blue
            Color.rgb(100, 160, 210), //  0: light blue
            Color.rgb(130, 190, 220), //  1: pale blue
            Color.rgb(160, 210, 230), //  2: cyan-white
            Color.rgb(200, 230, 240), //  3: white cloud
            Color.rgb(180, 220, 235)  //  4: bright cloud band
    };

    /** Rainfall heatmap palette: brown (dry) → khaki → green → cyan → dark blue (wet). */
    private static final Color[] RAINFALL_COLORS = {
            Color.rgb(139, 90, 43),   // 0: dry (brown/tan)
            Color.rgb(189, 183, 107), // 1: low (khaki)
            Color.rgb(154, 205, 50),  // 2: medium-low (yellow-green)
            Color.rgb(60, 179, 113),  // 3: medium (sea green)
            Color.rgb(32, 178, 170),  // 4: medium-high (light sea green)
            Color.rgb(0, 139, 139),   // 5: high (dark cyan)
            Color.rgb(0, 100, 180),   // 6: very high (blue)
            Color.rgb(0, 0, 139)      // 7: extreme (dark blue)
    };

    private PlanetColorPalette() {
    }

    /** Pick the colour-table for a given terrain type (WET as fallback). */
    private static Color[] paletteFor(TerrainType terrainType) {
        return switch (terrainType) {
            case DRY -> DRY_TERRAIN_COLORS;
            case ICE -> ICE_TERRAIN_COLORS;
            case JOVIAN -> JOVIAN_COLORS;
            case ICE_GIANT -> ICE_GIANT_COLORS;
            default -> HEIGHT_COLORS;
        };
    }

    /** WET-terrain colour for an integer height in {@code [-4, +4]} (clamped). */
    public static Color getColorForHeight(int height) {
        return getColorForHeight(height, TerrainType.WET);
    }

    /** Colour for an integer height in {@code [-4, +4]} (clamped) on the given terrain. */
    public static Color getColorForHeight(int height, TerrainType terrainType) {
        Color[] palette = paletteFor(terrainType);
        int index = Math.max(0, Math.min(palette.length - 1, height + 4));
        return palette[index];
    }

    /** WET-terrain smoothly interpolated colour for a precise height (typically {@code [-4, +4]}). */
    public static Color getColorForPreciseHeight(double height) {
        return getColorForPreciseHeight(height, TerrainType.WET);
    }

    /** Smoothly interpolated colour for a precise height on the given terrain. */
    public static Color getColorForPreciseHeight(double height, TerrainType terrainType) {
        Color[] palette = paletteFor(terrainType);

        // Map height from [-4, 4] to [0, 8] and clamp.
        double normalized = Math.max(0, Math.min(8, height + 4.0));

        int lowerIndex = Math.max(0, Math.min(palette.length - 1, (int) Math.floor(normalized)));
        int upperIndex = Math.max(0, Math.min(palette.length - 1, lowerIndex + 1));
        double fraction = normalized - Math.floor(normalized);

        return palette[lowerIndex].interpolate(palette[upperIndex], fraction);
    }

    /** Rainfall heatmap colour for a bucket in {@code [0, 7]} (clamped). */
    public static Color getColorForRainfall(int bucket) {
        int index = Math.max(0, Math.min(RAINFALL_COLORS.length - 1, bucket));
        return RAINFALL_COLORS[index];
    }

    /** Smoothly interpolated rainfall colour for a normalised value in {@code [0, 1]}. */
    public static Color getColorForNormalizedRainfall(double normalizedRainfall) {
        double scaled = Math.max(0, Math.min(1, normalizedRainfall)) * (RAINFALL_COLORS.length - 1);
        int lowerIdx = Math.max(0, Math.min(RAINFALL_COLORS.length - 1, (int) Math.floor(scaled)));
        int upperIdx = Math.max(0, Math.min(RAINFALL_COLORS.length - 1, lowerIdx + 1));
        double fraction = scaled - Math.floor(scaled);
        return RAINFALL_COLORS[lowerIdx].interpolate(RAINFALL_COLORS[upperIdx], fraction);
    }

    /** Total number of rainfall buckets — exposed for callers (e.g. legends) that need to iterate. */
    public static int rainfallBucketCount() {
        return RAINFALL_COLORS.length;
    }
}
