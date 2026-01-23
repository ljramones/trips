package com.teamgannon.trips.solarsystem.rendering;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;
import lombok.extern.slf4j.Slf4j;

/**
 * Renders grid and zone elements for solar system visualization.
 * <p>
 * Handles:
 * <ul>
 *   <li>Scale grid circles at standard AU intervals</li>
 *   <li>Habitable zone as a translucent ring</li>
 *   <li>Ecliptic reference plane with grid lines</li>
 * </ul>
 */
@Slf4j
public class GridAndZoneRenderer {

    private final ScaleManager scaleManager;

    public GridAndZoneRenderer(ScaleManager scaleManager) {
        this.scaleManager = scaleManager;
    }

    /**
     * Render scale grid circles at standard AU intervals.
     *
     * @param scaleGridGroup the group to add grid elements to
     */
    public void renderScaleGrid(Group scaleGridGroup) {
        double[] gridValues = scaleManager.getScaleGridAuValues();

        PhongMaterial gridMaterial = new PhongMaterial();
        gridMaterial.setDiffuseColor(Color.rgb(80, 80, 80, 0.5));

        for (double au : gridValues) {
            Group circle = createCircle(scaleManager.auToScreen(au), gridMaterial);
            scaleGridGroup.getChildren().add(circle);
        }
    }

    /**
     * Render the habitable zone as a translucent ring.
     *
     * @param habitableZoneGroup the group to add zone elements to
     * @param innerAU           inner edge in AU
     * @param outerAU           outer edge in AU
     */
    public void renderHabitableZone(Group habitableZoneGroup, double innerAU, double outerAU) {
        if (innerAU <= 0 || outerAU <= 0 || outerAU <= innerAU) {
            return;
        }

        double innerRadius = scaleManager.auToScreen(innerAU);
        double outerRadius = scaleManager.auToScreen(outerAU);

        // Create ring using cylinders as a simple approximation
        Group hzGroup = new Group();
        PhongMaterial hzMaterial = new PhongMaterial();
        hzMaterial.setDiffuseColor(Color.rgb(0, 255, 0, 0.15));

        int segments = 72;
        for (int i = 0; i < segments; i++) {
            double angle1 = 2 * Math.PI * i / segments;
            double angle2 = 2 * Math.PI * (i + 1) / segments;

            // Inner edge in XZ plane (Y is up)
            double x1Inner = innerRadius * Math.cos(angle1);
            double z1Inner = innerRadius * Math.sin(angle1);
            double x2Inner = innerRadius * Math.cos(angle2);
            double z2Inner = innerRadius * Math.sin(angle2);

            // Outer edge in XZ plane
            double x1Outer = outerRadius * Math.cos(angle1);
            double z1Outer = outerRadius * Math.sin(angle1);
            double x2Outer = outerRadius * Math.cos(angle2);
            double z2Outer = outerRadius * Math.sin(angle2);

            // Create thin cylinders for inner and outer edges
            Cylinder innerSeg = createRingSegmentXZ(x1Inner, z1Inner, x2Inner, z2Inner, 0.3, hzMaterial);
            Cylinder outerSeg = createRingSegmentXZ(x1Outer, z1Outer, x2Outer, z2Outer, 0.3, hzMaterial);

            hzGroup.getChildren().addAll(innerSeg, outerSeg);
        }

        // Add radial connectors every 30 degrees
        for (int i = 0; i < 12; i++) {
            double angle = 2 * Math.PI * i / 12;
            double xInner = innerRadius * Math.cos(angle);
            double zInner = innerRadius * Math.sin(angle);
            double xOuter = outerRadius * Math.cos(angle);
            double zOuter = outerRadius * Math.sin(angle);

            Cylinder radial = createRingSegmentXZ(xInner, zInner, xOuter, zOuter, 0.2, hzMaterial);
            hzGroup.getChildren().add(radial);
        }

        habitableZoneGroup.getChildren().add(hzGroup);

        log.info("Rendered habitable zone: {} - {} AU", innerAU, outerAU);
    }

    /**
     * Render the ecliptic reference plane with grid lines.
     *
     * @param eclipticPlaneGroup the group to add ecliptic elements to
     * @param show              whether to show the ecliptic plane
     */
    public void renderEclipticReference(Group eclipticPlaneGroup, boolean show) {
        eclipticPlaneGroup.getChildren().clear();
        if (!show) {
            return;
        }

        PhongMaterial gridMaterial = new PhongMaterial();
        gridMaterial.setDiffuseColor(Color.rgb(60, 60, 60, 0.25));
        gridMaterial.setSpecularColor(Color.rgb(80, 80, 80, 0.2));

        double[] gridValues = scaleManager.getScaleGridAuValues();
        for (double au : gridValues) {
            Group circle = createCircle(scaleManager.auToScreen(au), gridMaterial);
            eclipticPlaneGroup.getChildren().add(circle);
        }

        double maxRadius = scaleManager.auToScreen(scaleManager.getMaxOrbitalDistanceAU());
        double gridStep = maxRadius / 6;
        int steps = 12;
        for (int i = -steps; i <= steps; i++) {
            double offset = i * gridStep;
            Cylinder lineX = createRingSegmentXZ(-maxRadius, offset, maxRadius, offset, 0.15, gridMaterial);
            Cylinder lineZ = createRingSegmentXZ(offset, -maxRadius, offset, maxRadius, 0.15, gridMaterial);
            eclipticPlaneGroup.getChildren().addAll(lineX, lineZ);
        }

        eclipticPlaneGroup.setVisible(show);
    }

    /**
     * Create a circle in the XZ plane (Y is up).
     */
    public Group createCircle(double radius, PhongMaterial material) {
        Group circleGroup = new Group();
        int segments = 72;

        for (int i = 0; i < segments; i++) {
            double angle1 = 2 * Math.PI * i / segments;
            double angle2 = 2 * Math.PI * (i + 1) / segments;

            double x1 = radius * Math.cos(angle1);
            double z1 = radius * Math.sin(angle1);
            double x2 = radius * Math.cos(angle2);
            double z2 = radius * Math.sin(angle2);

            Cylinder segment = createRingSegmentXZ(x1, z1, x2, z2, 0.2, material);
            circleGroup.getChildren().add(segment);
        }

        return circleGroup;
    }

    /**
     * Create a cylinder segment between two points in the XZ plane (Y is up).
     */
    public Cylinder createRingSegmentXZ(double x1, double z1, double x2, double z2,
                                         double radius, PhongMaterial material) {
        double midX = (x1 + x2) / 2;
        double midZ = (z1 + z2) / 2;

        double dx = x2 - x1;
        double dz = z2 - z1;
        double length = Math.sqrt(dx * dx + dz * dz);

        Cylinder cylinder = new Cylinder(radius, length);
        cylinder.setMaterial(material);

        cylinder.setTranslateX(midX);
        cylinder.setTranslateY(0);  // In XZ plane
        cylinder.setTranslateZ(midZ);

        // Rotate to align with segment direction in XZ plane
        // Default cylinder is along Y axis, we need to lay it flat and rotate
        double angle = Math.toDegrees(Math.atan2(dx, dz));

        // First lay the cylinder flat (rotate 90° around X to put it in XZ plane)
        cylinder.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
        // Then rotate around Y to align with the segment direction
        cylinder.getTransforms().add(new Rotate(-angle, Rotate.Y_AXIS));

        return cylinder;
    }
}
