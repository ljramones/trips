package com.teamgannon.trips.dialogs.solarsystem;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;

/**
 * Renders the procedural planet's pole marker — a red north-pole sphere and a
 * blue south-pole sphere mounted just above the planet's surface so the spin
 * axis is visible at a glance.
 * <p>
 * Extracted from {@code ProceduralPlanetViewerDialog} in Phase 4.2 of the
 * codebase-review remediation. Owns its own {@link Group}, which the dialog
 * attaches to {@code planetGroup}. Re-rendering replaces any prior markers.
 *
 * <h2>Threading</h2>
 * All methods must be called on the JavaFX Application thread.
 */
public final class PlanetPoleMarker {

    /** Sphere radius (in screen units) for each pole marker. */
    private static final double MARKER_RADIUS = 0.02;

    /** Marker height relative to {@code planetScale} — just above the surface. */
    private static final double MARKER_HEIGHT_FACTOR = 1.05;

    private final Group planetGroup;
    private final double planetScale;

    private Group markerGroup;
    private boolean visible = true;

    public PlanetPoleMarker(Group planetGroup, double planetScale) {
        this.planetGroup = planetGroup;
        this.planetScale = planetScale;
    }

    public boolean isVisible() {
        return visible;
    }

    /**
     * Toggle pole-marker visibility. If currently absent and {@code visible} is
     * true, the caller should also invoke {@link #render()} to (re)build it.
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
        if (markerGroup != null) {
            markerGroup.setVisible(visible);
        }
    }

    /** Build (or rebuild) the marker group and attach it to {@code planetGroup}. */
    public void render() {
        if (markerGroup != null) {
            planetGroup.getChildren().remove(markerGroup);
        }
        markerGroup = new Group();
        double markerDistance = planetScale * MARKER_HEIGHT_FACTOR;

        Sphere north = new Sphere(MARKER_RADIUS);
        north.setMaterial(new PhongMaterial(Color.rgb(255, 80, 80)));
        north.setTranslateY(markerDistance);

        Sphere south = new Sphere(MARKER_RADIUS);
        south.setMaterial(new PhongMaterial(Color.rgb(80, 80, 255)));
        south.setTranslateY(-markerDistance);

        markerGroup.getChildren().addAll(north, south);
        markerGroup.setVisible(visible);
        planetGroup.getChildren().add(markerGroup);
    }
}
