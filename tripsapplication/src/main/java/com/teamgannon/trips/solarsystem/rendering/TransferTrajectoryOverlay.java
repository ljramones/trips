package com.teamgannon.trips.solarsystem.rendering;

import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;

import java.util.ArrayList;
import java.util.List;

/**
 * Draws a Hohmann-style dashed transfer arc between two circular orbits on the
 * ecliptic (XZ) plane, in the renderer's screen coordinates.
 * <p>
 * Extracted from {@code SolarSystemRenderer} in Phase 4.1.4 of the
 * codebase-review remediation. Owns its own {@link Group} (the overlay), and
 * borrows the {@link ScaleManager} from the renderer for radial scaling so
 * the arc aligns with planet orbits.
 * <p>
 * Caller responsibility:
 * <ul>
 *   <li>Add {@link #getOverlayGroup()} to the scene graph (the renderer adds
 *       it lazily inside {@link #draw}).</li>
 *   <li>Invoke {@link #clear()} when the underlying system changes.</li>
 * </ul>
 */
public final class TransferTrajectoryOverlay {

    private final ScaleManager scaleManager;
    private final Group overlayGroup = new Group();

    /** Cylinder radius for each dashed segment, in screen units. */
    private static final double SEGMENT_RADIUS = 0.6;

    /** Number of half-orbit samples; 64 is enough for a visually smooth arc. */
    private static final int SAMPLES = 64;

    public TransferTrajectoryOverlay(ScaleManager scaleManager) {
        this.scaleManager = scaleManager;
    }

    public Group getOverlayGroup() {
        return overlayGroup;
    }

    /**
     * Draw a half-ellipse Hohmann arc between two circular orbits.
     * Replaces any previously-drawn trajectory.
     *
     * @param r1Au origin orbital radius (AU)
     * @param r2Au destination orbital radius (AU)
     * @param color arc colour
     */
    public void draw(double r1Au, double r2Au, Color color) {
        clear();
        if (r1Au <= 0 || r2Au <= 0) {
            return;
        }
        double a = (r1Au + r2Au) / 2.0;
        double e = Math.abs(r2Au - r1Au) / (r1Au + r2Au);
        List<double[]> screenPoints = new ArrayList<>(SAMPLES + 1);
        for (int i = 0; i <= SAMPLES; i++) {
            double nu = Math.PI * i / SAMPLES;                       // true anomaly 0..180°
            double r = a * (1 - e * e) / (1 + e * Math.cos(nu));     // AU
            screenPoints.add(scaleManager.auVectorToScreen(r * Math.cos(nu), 0, r * Math.sin(nu)));
        }
        PhongMaterial material = new PhongMaterial(color);
        for (int i = 0; i < screenPoints.size() - 1; i++) {
            // Dashed pattern: 6 segments on, 4 off
            if (i % 10 < 6) {
                overlayGroup.getChildren().add(
                        segment(screenPoints.get(i), screenPoints.get(i + 1), SEGMENT_RADIUS, material));
            }
        }
    }

    /** Remove any drawn trajectory but leave the overlay group attached. */
    public void clear() {
        overlayGroup.getChildren().clear();
    }

    private static Cylinder segment(double[] p0, double[] p1, double radius, PhongMaterial material) {
        double dx = p1[0] - p0[0];
        double dy = p1[1] - p0[1];
        double dz = p1[2] - p0[2];
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        Cylinder cylinder = new Cylinder(radius, length);
        cylinder.setMaterial(material);
        cylinder.setTranslateX((p0[0] + p1[0]) / 2);
        cylinder.setTranslateY((p0[1] + p1[1]) / 2);
        cylinder.setTranslateZ((p0[2] + p1[2]) / 2);
        if (length > 1e-9) {
            // Rotate the Y-axis-aligned default cylinder to track the segment direction.
            Point3D dir = new Point3D(dx, dy, dz).normalize();
            Point3D axis = new Point3D(0, 1, 0).crossProduct(dir);
            if (axis.magnitude() > 1e-9) {
                double angle = Math.toDegrees(Math.acos(new Point3D(0, 1, 0).dotProduct(dir)));
                cylinder.getTransforms().add(new Rotate(angle, axis));
            }
        }
        return cylinder;
    }
}
