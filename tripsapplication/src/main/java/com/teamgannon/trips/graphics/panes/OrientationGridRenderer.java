package com.teamgannon.trips.graphics.panes;

import com.teamgannon.trips.planetary.PlanetaryContext;
import com.teamgannon.trips.planetary.rendering.PlanetarySkyRenderer;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import lombok.extern.slf4j.Slf4j;

/**
 * Renders orientation grid overlay for planetary sky visualization.
 * <p>
 * Handles:
 * <ul>
 *   <li>Altitude rings (horizon, 30°, 60°)</li>
 *   <li>Cardinal direction spokes (N, E, S, W)</li>
 *   <li>Cardinal and altitude labels</li>
 * </ul>
 */
@Slf4j
public class OrientationGridRenderer {

    private final Canvas orientationCanvas;
    private final Group world;
    private final PlanetarySkyRenderer skyRenderer;
    private final java.util.function.Supplier<PlanetaryContext> contextSupplier;

    public OrientationGridRenderer(Canvas orientationCanvas,
                                   Group world,
                                   PlanetarySkyRenderer skyRenderer,
                                   java.util.function.Supplier<PlanetaryContext> contextSupplier) {
        this.orientationCanvas = orientationCanvas;
        this.world = world;
        this.skyRenderer = skyRenderer;
        this.contextSupplier = contextSupplier;
    }

    /**
     * Redraw the orientation grid overlay.
     */
    public void redraw() {
        GraphicsContext gc = orientationCanvas.getGraphicsContext2D();
        double width = orientationCanvas.getWidth();
        double height = orientationCanvas.getHeight();
        gc.clearRect(0, 0, width, height);

        PlanetaryContext currentContext = contextSupplier.get();
        boolean showGrid = currentContext != null && currentContext.isShowOrientationGrid();
        orientationCanvas.setVisible(showGrid);
        if (!showGrid || width <= 0 || height <= 0) {
            return;
        }

        // Styling per spec: subtle violet-blue grid
        Color horizonColor = Color.rgb(140, 130, 255, 0.30);   // Brighter for horizon
        Color altitudeColor = Color.rgb(130, 120, 255, 0.20);  // Subtle for altitude rings
        Color spokeColor = Color.rgb(130, 120, 255, 0.18);     // Very subtle for spokes
        Color labelColor = Color.rgb(170, 160, 255, 0.65);     // More visible for labels

        double radius = skyRenderer.getSkyDomeRadius() * 0.995;

        // Draw altitude rings: horizon (0°), 30°, 60°
        gc.setLineWidth(2.0);  // Thicker horizon line
        drawAltitudeRing(gc, radius, 0, horizonColor);

        gc.setLineWidth(1.0);  // Standard width for other rings
        drawAltitudeRing(gc, radius, 30, altitudeColor);
        drawAltitudeRing(gc, radius, 60, altitudeColor);

        // Draw cardinal direction spokes (N, E, S, W)
        gc.setLineWidth(1.0);
        for (int az = 0; az < 360; az += 90) {
            drawAzimuthSpoke(gc, radius, az, 0, 80, spokeColor);
        }

        // Draw cardinal labels at the horizon
        gc.setFill(labelColor);
        gc.setFont(Font.font("Verdana", 14));
        drawCardinalLabel(gc, radius, 0, "N");
        drawCardinalLabel(gc, radius, 90, "E");
        drawCardinalLabel(gc, radius, 180, "S");
        drawCardinalLabel(gc, radius, 270, "W");

        // Draw altitude labels along the North spoke
        gc.setFont(Font.font("Verdana", 10));
        Color altLabelColor = Color.rgb(150, 140, 255, 0.50);
        gc.setFill(altLabelColor);
        drawAltitudeLabel(gc, radius, 0, 30, "30°");
        drawAltitudeLabel(gc, radius, 0, 60, "60°");
    }

    private void drawAltitudeLabel(GraphicsContext gc, double radius, double azimuthDeg,
                                   double altitudeDeg, String label) {
        Point2D point = projectToOverlay(radius, azimuthDeg, altitudeDeg);
        if (point == null) {
            return;
        }
        // Offset slightly to the right of the spoke
        gc.fillText(label, point.getX() + 8, point.getY() + 4);
    }

    private void drawAltitudeRing(GraphicsContext gc, double radius, double altitudeDeg, Color color) {
        gc.setStroke(color);
        double step = 3.0;  // Smaller step for smoother curves
        Point2D prev = null;

        for (double az = 0; az <= 360.0 + step; az += step) {
            double azNorm = az % 360.0;
            Point2D next = projectToOverlay(radius, azNorm, altitudeDeg);

            if (prev != null && next != null) {
                // Check for wraparound artifacts (large jumps across screen)
                double dx = next.getX() - prev.getX();
                double dy = next.getY() - prev.getY();
                double dist = Math.sqrt(dx * dx + dy * dy);

                // Skip if segment jumps across more than 1/4 of screen (behind camera wrap)
                double maxDist = Math.max(orientationCanvas.getWidth(), orientationCanvas.getHeight()) * 0.4;
                if (dist < maxDist) {
                    gc.strokeLine(prev.getX(), prev.getY(), next.getX(), next.getY());
                }
            }

            prev = next;
        }
    }

    private void drawAzimuthSpoke(GraphicsContext gc, double radius, double azimuthDeg,
                                  double altStart, double altEnd, Color color) {
        gc.setStroke(color);
        double step = 3.0;  // Smaller step for smoother lines
        Point2D prev = null;

        for (double alt = Math.max(0, altStart); alt <= altEnd; alt += step) {
            Point2D next = projectToOverlay(radius, azimuthDeg, alt);

            if (prev != null && next != null) {
                // Check for large jumps (shouldn't happen for spokes, but defensive)
                double dx = next.getX() - prev.getX();
                double dy = next.getY() - prev.getY();
                double dist = Math.sqrt(dx * dx + dy * dy);

                double maxDist = Math.max(orientationCanvas.getWidth(), orientationCanvas.getHeight()) * 0.3;
                if (dist < maxDist) {
                    gc.strokeLine(prev.getX(), prev.getY(), next.getX(), next.getY());
                }
            }

            prev = next;
        }
    }

    private void drawCardinalLabel(GraphicsContext gc, double radius, double azimuthDeg, String label) {
        // Project the label position at horizon (alt=0) plus a small offset above
        Point2D horizonPoint = projectToOverlay(radius, azimuthDeg, 0);
        Point2D abovePoint = projectToOverlay(radius, azimuthDeg, 5);

        if (horizonPoint == null) {
            return;
        }

        // Position label slightly above the horizon point
        double x = horizonPoint.getX();
        double y = horizonPoint.getY();

        // If we have a point above horizon, use it to offset in the right direction
        if (abovePoint != null) {
            double dx = abovePoint.getX() - horizonPoint.getX();
            double dy = abovePoint.getY() - horizonPoint.getY();
            double len = Math.sqrt(dx * dx + dy * dy);
            if (len > 1) {
                // Offset 15 pixels in the "up" direction relative to view
                x += (dx / len) * 15;
                y += (dy / len) * 15;
            }
        } else {
            // Fallback: offset upward in screen coords
            y -= 15;
        }

        // Center the label horizontally on the computed position
        double labelWidth = gc.getFont().getSize() * label.length() * 0.6;
        gc.fillText(label, x - labelWidth / 2, y + 5);
    }

    private Point2D projectToOverlay(double radius, double azimuthDeg, double altitudeDeg) {
        if (altitudeDeg < 0) {
            return null;  // Below horizon - don't draw
        }

        // Get 3D position in world coordinates (same coordinate system as stars)
        double[] pos = skyRenderer.toSkyPoint(radius, azimuthDeg, altitudeDeg);
        Point3D worldPoint = new Point3D(pos[0], pos[1], pos[2]);

        // Project to scene coordinates (localToScene with rootScene=true does perspective projection)
        Point3D scenePoint = world.localToScene(worldPoint, true);

        // Check for invalid projection
        if (Double.isNaN(scenePoint.getX()) || Double.isNaN(scenePoint.getY())) {
            return null;
        }

        // Convert scene coordinates to canvas local coordinates
        Point2D canvasPoint = orientationCanvas.sceneToLocal(scenePoint.getX(), scenePoint.getY());
        if (canvasPoint == null) {
            return null;
        }

        // Get canvas dimensions
        double width = orientationCanvas.getWidth();
        double height = orientationCanvas.getHeight();

        double x = canvasPoint.getX();
        double y = canvasPoint.getY();

        // Check if point is within visible bounds (with margin for labels)
        if (x < -50 || x > width + 50 || y < -50 || y > height + 50) {
            return null;
        }

        return new Point2D(x, y);
    }
}
