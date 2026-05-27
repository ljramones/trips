package com.teamgannon.trips.dialogs.solarsystem;

import com.teamgannon.trips.planetary.modelling.procedural.PlanetGenerator.GeneratedPlanet;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;

import java.util.List;

import com.teamgannon.trips.planetary.modelling.procedural.Polygon;

/**
 * Renders a procedural planet's river network: each river is a chain of
 * polygon-to-polygon flow segments drawn as small cylinders along the
 * surface, optionally tinted by flow accumulation so trunk rivers read
 * wider/darker than tributaries.
 * <p>
 * Extracted from {@code ProceduralPlanetViewerDialog} in Phase 4.2 of the
 * codebase-review remediation (Issue 18). Pure scene-graph builder — the
 * dialog still decides when to render (the "Show rivers" toggle).
 *
 * <h2>Threading</h2>
 * Must be called on the JavaFX Application thread.
 */
public final class RiverNetworkRenderer {

    private static final double MIN_SEGMENT_RADIUS = 0.002;
    private static final double MAX_SEGMENT_RADIUS = 0.008;
    private static final double SURFACE_OFFSET = 1.003;
    private static final double HEIGHT_DISPLACEMENT = 0.025;

    /** Frozen river palette — light blue at source fading to icy white. */
    private static final Color FROZEN_SOURCE = Color.rgb(135, 206, 250);
    private static final Color FROZEN_TERMINUS = Color.rgb(224, 255, 255);

    /** Flowing river palette — light blue at source fading to deep navy at mouth. */
    private static final Color FLOWING_SOURCE = Color.rgb(100, 180, 255);
    private static final Color FLOWING_MOUTH = Color.rgb(0, 80, 160);

    private final GeneratedPlanet planet;
    private final Group planetGroup;
    private final double planetScale;
    private final boolean useFlowAccumulation;

    public RiverNetworkRenderer(GeneratedPlanet planet,
                                Group planetGroup,
                                double planetScale,
                                boolean useFlowAccumulation) {
        this.planet = planet;
        this.planetGroup = planetGroup;
        this.planetScale = planetScale;
        this.useFlowAccumulation = useFlowAccumulation;
    }

    /** Add every river segment to the planet group. No-op if the planet has no rivers. */
    public void render() {
        List<List<Integer>> rivers = planet.rivers();
        if (rivers == null || rivers.isEmpty()) return;

        List<Polygon> polygons = planet.polygons();
        boolean[] frozenTerminus = planet.frozenRiverTerminus();
        int[] heights = planet.heights();
        double[] flowAccumulation = useFlowAccumulation ? planet.flowAccumulation() : null;

        for (int riverIdx = 0; riverIdx < rivers.size(); riverIdx++) {
            List<Integer> river = rivers.get(riverIdx);
            if (river.size() < 2) continue;

            boolean isFrozen = frozenTerminus != null
                    && riverIdx < frozenTerminus.length
                    && frozenTerminus[riverIdx];

            double[] flowValues = calculateFlowValues(river, flowAccumulation);
            double maxFlow = 0.0;
            for (double v : flowValues) if (v > maxFlow) maxFlow = v;
            if (maxFlow <= 0) maxFlow = 1.0;

            for (int i = 0; i < river.size() - 1; i++) {
                int polyIdx1 = river.get(i);
                int polyIdx2 = river.get(i + 1);
                if (polyIdx1 < 0 || polyIdx1 >= polygons.size()
                        || polyIdx2 < 0 || polyIdx2 >= polygons.size()) {
                    continue;
                }

                double flowRatio = flowValues[i + 1] / maxFlow;
                double avgHeight = (heights[polyIdx1] + heights[polyIdx2]) / 2.0;
                double displacement = 1.0 + avgHeight * HEIGHT_DISPLACEMENT;

                Point3D p1 = toPoint3D(polygons.get(polyIdx1).center().normalize()
                        .scalarMultiply(planetScale * displacement * SURFACE_OFFSET));
                Point3D p2 = toPoint3D(polygons.get(polyIdx2).center().normalize()
                        .scalarMultiply(planetScale * displacement * SURFACE_OFFSET));

                planetGroup.getChildren().add(buildSegment(p1, p2, flowRatio, isFrozen));
            }
        }
    }

    private double[] calculateFlowValues(List<Integer> river, double[] flowAccumulation) {
        double[] flow = new double[river.size()];
        if (flowAccumulation != null && flowAccumulation.length > 0) {
            for (int i = 0; i < river.size(); i++) {
                int polyIdx = river.get(i);
                flow[i] = polyIdx >= 0 && polyIdx < flowAccumulation.length
                        ? flowAccumulation[polyIdx]
                        : 0.0;
            }
            return flow;
        }
        // Fallback when flow accumulation isn't available: accumulate a small
        // baseline per segment plus a rainfall-driven contribution from the
        // polygon each segment crosses.
        double[] rainfall = planet.rainfall();
        double cumulative = 0.0;
        double baseFlowPerSegment = 0.5;
        for (int i = 0; i < river.size(); i++) {
            int polyIdx = river.get(i);
            double contribution = baseFlowPerSegment;
            if (rainfall != null && polyIdx >= 0 && polyIdx < rainfall.length) {
                contribution += rainfall[polyIdx] * 0.5;
            }
            cumulative += contribution;
            flow[i] = cumulative;
        }
        return flow;
    }

    private Cylinder buildSegment(Point3D start, Point3D end, double flowRatio, boolean frozen) {
        Point3D midpoint = start.midpoint(end);
        double length = start.distance(end);
        double radius = MIN_SEGMENT_RADIUS + Math.sqrt(flowRatio) * (MAX_SEGMENT_RADIUS - MIN_SEGMENT_RADIUS);

        Cylinder cylinder = new Cylinder(radius, length);
        Color color = frozen
                ? FROZEN_SOURCE.interpolate(FROZEN_TERMINUS, flowRatio)
                : FLOWING_SOURCE.interpolate(FLOWING_MOUTH, flowRatio);
        PhongMaterial material = new PhongMaterial(color);
        material.setSpecularColor(Color.WHITE.deriveColor(0, 1, 0.5, 1));
        material.setSpecularPower(25.0);
        cylinder.setMaterial(material);

        cylinder.setTranslateX(midpoint.getX());
        cylinder.setTranslateY(midpoint.getY());
        cylinder.setTranslateZ(midpoint.getZ());

        Point3D direction = end.subtract(start).normalize();
        Point3D yAxis = new Point3D(0, 1, 0);
        Point3D rotationAxis = yAxis.crossProduct(direction);
        double rotationAngle = Math.acos(Math.max(-1, Math.min(1, yAxis.dotProduct(direction))));
        if (rotationAxis.magnitude() > 0.0001) {
            cylinder.getTransforms().add(new Rotate(Math.toDegrees(rotationAngle), rotationAxis));
        }
        return cylinder;
    }

    private static Point3D toPoint3D(Vector3D v) {
        return new Point3D(v.getX(), v.getY(), v.getZ());
    }
}
