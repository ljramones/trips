package com.teamgannon.trips.solarsystem.rendering;

import com.teamgannon.trips.model.PlanetDescription;
import javafx.scene.paint.Color;

/**
 * Color constants and small helpers used by the solar-system renderer.
 * <p>
 * Extracted from {@code SolarSystemRenderer} in Phase 4.1.2 of the
 * codebase-review remediation. Pure functions only — no state, no scene-graph
 * dependencies — so any collaborator in {@code solarsystem.rendering} can use
 * these without coupling back to the renderer.
 */
public final class SolarSystemColors {

    /** Default orbit colours by index — cycled through for unique planet orbits. */
    public static final Color[] ORBIT_COLORS = {
            Color.rgb(100, 149, 237, 0.7),  // Cornflower blue
            Color.rgb(144, 238, 144, 0.7),  // Light green
            Color.rgb(255, 182, 193, 0.7),  // Light pink
            Color.rgb(255, 218, 185, 0.7),  // Peach
            Color.rgb(221, 160, 221, 0.7),  // Plum
            Color.rgb(176, 224, 230, 0.7),  // Powder blue
            Color.rgb(240, 230, 140, 0.7),  // Khaki
            Color.rgb(152, 251, 152, 0.7),  // Pale green
    };

    /** Planet colours used as defaults by {@link #planetColor(PlanetDescription)}. */
    public static final Color HOT_PLANET_COLOR = Color.rgb(255, 100, 50);
    public static final Color TEMPERATE_PLANET_COLOR = Color.rgb(100, 180, 100);
    public static final Color COLD_PLANET_COLOR = Color.rgb(150, 200, 255);
    public static final Color GAS_GIANT_COLOR = Color.rgb(230, 180, 120);

    /** Moon colour — silver / grey to distinguish from planets. */
    public static final Color MOON_COLOR = Color.rgb(192, 192, 200);

    /**
     * Moon-orbit colour — single consistent silver / grey for every moon orbit
     * so they're clearly distinguishable from planet orbits.
     */
    public static final Color MOON_ORBIT_COLOR = Color.rgb(180, 180, 200, 0.8);

    private SolarSystemColors() {
    }

    /**
     * Map a stellar spectral class (O/B/A/F/G/K/M) to a representative display colour.
     * Falls back to yellow for unrecognised or empty inputs.
     */
    public static Color starColor(String spectralClass) {
        if (spectralClass == null || spectralClass.isEmpty()) {
            return Color.YELLOW;
        }
        return switch (spectralClass.charAt(0)) {
            case 'O' -> Color.rgb(155, 176, 255);
            case 'B' -> Color.rgb(170, 191, 255);
            case 'A' -> Color.rgb(202, 215, 255);
            case 'F' -> Color.rgb(248, 247, 255);
            case 'G' -> Color.rgb(255, 244, 234);
            case 'K' -> Color.rgb(255, 210, 161);
            case 'M' -> Color.rgb(255, 180, 120);
            default -> Color.YELLOW;
        };
    }

    /**
     * Pick a display colour for a planet:
     * <ul>
     *   <li>Moons get {@link #MOON_COLOR}.</li>
     *   <li>If equilibrium temperature is known, classify by temperature
     *       (hot &gt; 500 K, temperate &gt; 200 K, cold otherwise).</li>
     *   <li>Otherwise fall back to mass (&gt; 50 Earth masses ⇒ gas giant).</li>
     * </ul>
     */
    public static Color planetColor(PlanetDescription planet) {
        if (planet.isMoon()) {
            return MOON_COLOR;
        }
        double temp = planet.getEquilibriumTemperature();
        if (temp > 0) {
            if (temp > 500) return HOT_PLANET_COLOR;
            if (temp > 200) return TEMPERATE_PLANET_COLOR;
            return COLD_PLANET_COLOR;
        }
        // Fallback: mass-based (gas giants are heavier)
        if (planet.getMass() > 50) {
            return GAS_GIANT_COLOR;
        }
        return TEMPERATE_PLANET_COLOR;
    }
}
