package com.teamgannon.trips.dialogs.solarsystem;

import com.teamgannon.trips.planetary.modelling.procedural.AdjacencyGraph;
import com.teamgannon.trips.planetary.modelling.procedural.BoundaryDetector;
import com.teamgannon.trips.planetary.modelling.procedural.BoundaryDetector.BoundaryType;
import com.teamgannon.trips.planetary.modelling.procedural.PlanetGenerator.GeneratedPlanet;
import com.teamgannon.trips.planetary.modelling.procedural.PlateAssigner;
import com.teamgannon.trips.planetary.modelling.procedural.Polygon;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;
import org.hipparchus.geometry.euclidean.threed.Vector3D;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Renders plate boundaries on a procedural planet as colour-coded cylinders
 * between every pair of polygons that belong to different plates: red for
 * convergent (collisions / mountain-building), teal for divergent (rifts /
 * spreading), gold for transform faults, grey for inactive boundaries.
 * <p>
 * Extracted from {@code ProceduralPlanetViewerDialog} in Phase 4.2 of the
 * codebase-review remediation (Issue 18). Pure scene-graph builder.
 *
 * <h2>Threading</h2>
 * Must be called on the JavaFX Application thread.
 */
public final class PlateBoundaryRenderer {

    private static final double SURFACE_OFFSET = 1.004;
    private static final double HEIGHT_DISPLACEMENT = 0.025;
    private static final double ACTIVE_BOUNDARY_RADIUS = 0.004;
    private static final double INACTIVE_BOUNDARY_RADIUS = 0.003;

    private static final Color CONVERGENT_COLOR = Color.rgb(220, 60, 60);
    private static final Color DIVERGENT_COLOR = Color.rgb(60, 200, 180);
    private static final Color TRANSFORM_COLOR = Color.rgb(220, 180, 60);
    private static final Color INACTIVE_COLOR = Color.rgb(120, 120, 120);

    private final GeneratedPlanet planet;
    private final AdjacencyGraph adjacency;
    private final PlateAssigner.PlateAssignment plateAssignment;
    private final BoundaryDetector.BoundaryAnalysis boundaryAnalysis;
    private final Group planetGroup;
    private final double planetScale;

    public PlateBoundaryRenderer(GeneratedPlanet planet,
                                 AdjacencyGraph adjacency,
                                 PlateAssigner.PlateAssignment plateAssignment,
                                 BoundaryDetector.BoundaryAnalysis boundaryAnalysis,
                                 Group planetGroup,
                                 double planetScale) {
        this.planet = planet;
        this.adjacency = adjacency;
        this.plateAssignment = plateAssignment;
        this.boundaryAnalysis = boundaryAnalysis;
        this.planetGroup = planetGroup;
        this.planetScale = planetScale;
    }

    /**
     * No-op if plate / boundary / adjacency data isn't available (the planet
     * generator can skip it for performance reasons). Otherwise iterates
     * every adjacency pair, deduplicates by edge id, and adds one cylinder
     * per cross-plate boundary.
     */
    public void render() {
        if (plateAssignment == null || boundaryAnalysis == null || adjacency == null) {
            return;
        }

        List<Polygon> polygons = planet.polygons();
        int[] plateIndex = plateAssignment.plateIndex();
        int[] heights = planet.heights();

        Set<String> drawnEdges = new HashSet<>();

        for (int polyIdx = 0; polyIdx < polygons.size(); polyIdx++) {
            int plate1 = plateIndex[polyIdx];
            int[] neighbors = adjacency.neighborsOnly(polyIdx);

            for (int neighborIdx : neighbors) {
                int plate2 = plateIndex[neighborIdx];
                if (plate1 == plate2) continue;

                String edgeKey = Math.min(polyIdx, neighborIdx) + "-" + Math.max(polyIdx, neighborIdx);
                if (!drawnEdges.add(edgeKey)) continue;

                BoundaryDetector.PlatePair pair = new BoundaryDetector.PlatePair(plate1, plate2);
                BoundaryType boundaryType = boundaryAnalysis.boundaries().get(pair);
                if (boundaryType == null) {
                    boundaryType = BoundaryType.INACTIVE;
                }

                double avgHeight = (heights[polyIdx] + heights[neighborIdx]) / 2.0;
                double displacement = 1.0 + avgHeight * HEIGHT_DISPLACEMENT;

                Point3D p1 = toPoint3D(polygons.get(polyIdx).center().normalize()
                        .scalarMultiply(planetScale * displacement * SURFACE_OFFSET));
                Point3D p2 = toPoint3D(polygons.get(neighborIdx).center().normalize()
                        .scalarMultiply(planetScale * displacement * SURFACE_OFFSET));

                planetGroup.getChildren().add(buildSegment(p1, p2, boundaryType));
            }
        }
    }

    private Cylinder buildSegment(Point3D start, Point3D end, BoundaryType type) {
        Point3D midpoint = start.midpoint(end);
        double length = start.distance(end);
        double radius = (type == BoundaryType.CONVERGENT || type == BoundaryType.DIVERGENT)
                ? ACTIVE_BOUNDARY_RADIUS
                : INACTIVE_BOUNDARY_RADIUS;

        Cylinder cylinder = new Cylinder(radius, length);
        Color color = switch (type) {
            case CONVERGENT -> CONVERGENT_COLOR;
            case DIVERGENT -> DIVERGENT_COLOR;
            case TRANSFORM -> TRANSFORM_COLOR;
            case INACTIVE -> INACTIVE_COLOR;
        };
        PhongMaterial material = new PhongMaterial(color);
        material.setSpecularColor(Color.WHITE.deriveColor(0, 1, 0.3, 1));
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
