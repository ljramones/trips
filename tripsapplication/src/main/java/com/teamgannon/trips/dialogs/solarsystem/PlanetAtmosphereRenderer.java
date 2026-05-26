package com.teamgannon.trips.dialogs.solarsystem;

import com.teamgannon.trips.planetarymodelling.procedural.JavaFxPlanetMeshConverter.TerrainType;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.Sphere;

/**
 * Renders the atmosphere "glow" sphere around the procedural planet.
 * <p>
 * Extracted from {@code ProceduralPlanetViewerDialog} in Phase 4.2 of the
 * codebase-review remediation.
 * <p>
 * Behaviour:
 * <ul>
 *   <li><b>Gas giants</b> (JOVIAN / ICE_GIANT) always carry a thick (8% radius)
 *       atmosphere — tan/orange for Jovian, blue for Neptune-like.</li>
 *   <li>Dry rocky planets (water &lt; 5%) get no atmosphere.</li>
 *   <li>Frozen worlds (T &lt; 200 K) get no atmosphere — Europa / Enceladus / Pluto.</li>
 *   <li>Everyone else gets a 5%-radius atmosphere colour-matched to water
 *       fraction + temperature (Mars-tan, Earth-blue, semi-arid pale-blue,
 *       very-dry dusty).</li>
 * </ul>
 *
 * <h2>Threading</h2>
 * All methods must be called on the JavaFX Application thread.
 */
public final class PlanetAtmosphereRenderer {

    /** Atmosphere shell radius (relative to {@code planetScale}) for gas / ice giants. */
    private static final double GIANT_RADIUS_FACTOR = 1.08;

    /** Atmosphere shell radius (relative to {@code planetScale}) for terrestrial worlds. */
    private static final double TERRESTRIAL_RADIUS_FACTOR = 1.05;

    /** Below this water fraction, terrestrial worlds skip the atmosphere. */
    private static final double DRY_THRESHOLD = 0.05;

    /** Below this temperature (K), worlds are too frozen for atmosphere. */
    private static final double FROZEN_TEMPERATURE_K = 200.0;

    /** Liquid-water threshold (K) — Mars-like cold-but-not-frozen below this. */
    private static final double FREEZING_K = 273.0;

    private final Group world;
    private final double planetScale;

    private Sphere atmosphereSphere;
    private boolean showAtmosphere = true;

    public PlanetAtmosphereRenderer(Group world, double planetScale) {
        this.world = world;
        this.planetScale = planetScale;
    }

    public void setShowAtmosphere(boolean show) {
        this.showAtmosphere = show;
        if (atmosphereSphere != null) {
            atmosphereSphere.setVisible(show);
        }
    }

    public boolean isShowAtmosphere() {
        return showAtmosphere;
    }

    /**
     * (Re)render the atmosphere shell for the given terrain context. Removes
     * the previous shell if any. If {@code showAtmosphere} is false, only the
     * removal happens — call {@link #setShowAtmosphere(boolean)} to flip
     * visibility on an existing shell.
     *
     * @param terrainType         classified terrain (drives shell thickness + colour)
     * @param waterFraction       liquid-water fraction (0..1)
     * @param surfaceTemperatureK surface temperature in K
     */
    public void render(TerrainType terrainType, double waterFraction, double surfaceTemperatureK) {
        if (atmosphereSphere != null) {
            world.getChildren().remove(atmosphereSphere);
            atmosphereSphere = null;
        }
        if (!showAtmosphere) {
            return;
        }

        if (terrainType == TerrainType.JOVIAN || terrainType == TerrainType.ICE_GIANT) {
            atmosphereSphere = buildShell(planetScale * GIANT_RADIUS_FACTOR,
                    terrainType == TerrainType.JOVIAN
                            ? Color.rgb(200, 180, 150, 0.15)   // Jupiter-like tan/orange
                            : Color.rgb(100, 150, 200, 0.18)); // Neptune-like blue
            world.getChildren().add(atmosphereSphere);
            return;
        }

        if (waterFraction < DRY_THRESHOLD) {
            return;
        }
        if (surfaceTemperatureK < FROZEN_TEMPERATURE_K) {
            return;
        }

        Color color;
        if (surfaceTemperatureK < FREEZING_K) {
            color = Color.rgb(200, 180, 160, 0.04);   // Mars-tan (200-273 K)
        } else if (waterFraction > 0.5) {
            color = Color.rgb(100, 150, 255, 0.12);   // Earth-blue (wet)
        } else if (waterFraction > 0.2) {
            color = Color.rgb(135, 180, 255, 0.08);   // semi-arid pale blue
        } else {
            color = Color.rgb(180, 200, 230, 0.05);   // very-dry dusty
        }

        atmosphereSphere = buildShell(planetScale * TERRESTRIAL_RADIUS_FACTOR, color);
        world.getChildren().add(atmosphereSphere);
    }

    private static Sphere buildShell(double radius, Color color) {
        Sphere shell = new Sphere(radius);
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(color);
        material.setSpecularColor(Color.TRANSPARENT);
        shell.setMaterial(material);
        shell.setCullFace(CullFace.NONE);
        return shell;
    }
}
