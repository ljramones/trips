package com.teamgannon.trips.dialogs.solarsystem;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;

/**
 * Renders climate-zone latitude rings (tropic + arctic boundaries) as a ring
 * of small spheres hovering just above the planet surface.
 * <p>
 * Extracted from {@code ProceduralPlanetViewerDialog} in Phase 4.2 of the
 * codebase-review remediation. Pure scene-graph builder — caller decides
 * whether to invoke based on whether climate data is available.
 *
 * <h2>Threading</h2>
 * All methods must be called on the JavaFX Application thread.
 */
public final class PlanetClimateZoneOverlay {

    /** Tropic latitude bands (°). */
    private static final double TROPIC_LATITUDE_DEG = 30.0;

    /** Arctic / Antarctic latitude bands (°). */
    private static final double POLAR_LATITUDE_DEG = 60.0;

    /** Tropic ring tint (warm orange). */
    private static final Color TROPIC_COLOR = Color.rgb(255, 200, 100, 0.4);

    /** Polar ring tint (cool blue). */
    private static final Color POLAR_COLOR = Color.rgb(100, 200, 255, 0.5);

    /** Radial offset (multiplier) from the planet surface — keeps the ring above the mesh. */
    private static final double RING_RADIUS_OFFSET = 1.008;

    /** Number of dots per ring; 72 gives a smooth visual at typical zoom. */
    private static final int RING_SEGMENTS = 72;

    /** Radius of each dot (screen units). */
    private static final double DOT_RADIUS = 0.008;

    private PlanetClimateZoneOverlay() {
    }

    /**
     * Add the four climate rings (±30°, ±60°) to {@code planetGroup}.
     *
     * @param planetGroup target scene-graph group (must be non-null)
     * @param planetScale planet's display-scale radius
     */
    public static void render(Group planetGroup, double planetScale) {
        addLatitudeRing(planetGroup, planetScale, TROPIC_LATITUDE_DEG, TROPIC_COLOR);
        addLatitudeRing(planetGroup, planetScale, -TROPIC_LATITUDE_DEG, TROPIC_COLOR);
        addLatitudeRing(planetGroup, planetScale, POLAR_LATITUDE_DEG, POLAR_COLOR);
        addLatitudeRing(planetGroup, planetScale, -POLAR_LATITUDE_DEG, POLAR_COLOR);
    }

    /** Build a single ring of dots at the given latitude. */
    private static void addLatitudeRing(Group planetGroup, double planetScale,
                                        double latitudeDegrees, Color color) {
        double latRad = Math.toRadians(latitudeDegrees);
        double ringRadius = planetScale * Math.cos(latRad) * RING_RADIUS_OFFSET;
        double y = planetScale * Math.sin(latRad) * RING_RADIUS_OFFSET;

        for (int i = 0; i < RING_SEGMENTS; i++) {
            double angle = 2.0 * Math.PI * i / RING_SEGMENTS;
            Sphere dot = new Sphere(DOT_RADIUS);
            dot.setMaterial(new PhongMaterial(color));
            dot.setTranslateX(ringRadius * Math.cos(angle));
            dot.setTranslateY(y);
            dot.setTranslateZ(ringRadius * Math.sin(angle));
            planetGroup.getChildren().add(dot);
        }
    }
}
