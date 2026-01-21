package com.teamgannon.trips.solarsystem.rendering;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import lombok.extern.slf4j.Slf4j;

import com.teamgannon.trips.solarsystem.orbits.OrbitSamplingProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Creates 3D visualizations of orbital paths.
 * Renders elliptical orbits with proper 3D orientation based on Keplerian elements.
 */
@Slf4j
public class OrbitVisualizer {

    public static final String ORBIT_BASE_MESH_KEY = "orbitBaseMesh";
    public static final String ORBIT_HIGHLIGHT_MESH_KEY = "orbitHighlightMesh";

    private static final int[] LOD_SEGMENTS = {128, 256, 512, 1024};
    private static final double[] LOD_THRESHOLDS = {0.7, 1.4, 2.6};
    private static final double LOD_HYSTERESIS = 0.15;

    private static final int DASH_ON_SEGMENTS = 6;
    private static final int DASH_OFF_SEGMENTS = 4;

    private static final double ORBIT_RIBBON_HALF_WIDTH = 0.6;
    private static final double ORBIT_HIGHLIGHT_SCALE = 1.6;
    private static final double ORBIT_PLANE_EPSILON = 0.35;
    private static final double MIN_TANGENT_LENGTH = 1e-6;

    private final ScaleManager scaleManager;
    private final OrbitSamplingProvider orbitSamplingProvider;
    private final Map<OrbitKey, OrbitLodState> lodStates = new HashMap<>();
    private final Map<OrbitMeshKey, OrbitMeshPair> meshCache = new HashMap<>();

    public OrbitVisualizer(ScaleManager scaleManager, OrbitSamplingProvider orbitSamplingProvider) {
        this.scaleManager = scaleManager;
        this.orbitSamplingProvider = orbitSamplingProvider;
    }

    /**
     * Create a 3D orbital path visualization.
     *
     * @param semiMajorAxisAU              semi-major axis in AU
     * @param eccentricity                 orbital eccentricity (0 = circle, 0-1 = ellipse)
     * @param inclinationDeg               orbital inclination in degrees
     * @param longitudeOfAscendingNodeDeg  longitude of ascending node (Ω) in degrees
     * @param argumentOfPeriapsisDeg       argument of periapsis (ω) in degrees
     * @param orbitColor                   color for the orbit path
     * @return Group containing the orbital path
     */
    public Group createOrbitPath(double semiMajorAxisAU,
                                  double eccentricity,
                                  double inclinationDeg,
                                  double longitudeOfAscendingNodeDeg,
                                  double argumentOfPeriapsisDeg,
                                  Color orbitColor) {

        OrbitKey orbitKey = new OrbitKey(semiMajorAxisAU, eccentricity, inclinationDeg,
                longitudeOfAscendingNodeDeg, argumentOfPeriapsisDeg);
        int segments = selectSegmentCount(orbitKey);
        OrbitMeshKey meshKey = new OrbitMeshKey(semiMajorAxisAU, eccentricity, segments,
                scaleManager.getBaseScale(), scaleManager.getZoomLevel(), scaleManager.isUseLogScale());
        OrbitMeshPair meshPair = meshCache.computeIfAbsent(meshKey,
                key -> buildOrbitMeshPair(semiMajorAxisAU, eccentricity, segments));

        PhongMaterial baseMaterial = createOrbitMaterial(orbitColor, false);
        PhongMaterial highlightMaterial = createOrbitMaterial(orbitColor, true);

        MeshView baseMesh = createOrbitMeshView(meshPair.baseMesh(), baseMaterial);
        MeshView highlightMesh = createOrbitMeshView(meshPair.highlightMesh(), highlightMaterial);
        highlightMesh.setVisible(false);

        Group orbitGroup = new Group(baseMesh, highlightMesh);
        orbitGroup.getProperties().put(ORBIT_BASE_MESH_KEY, baseMesh);
        orbitGroup.getProperties().put(ORBIT_HIGHLIGHT_MESH_KEY, highlightMesh);

        // Offset slightly along the local orbit normal to reduce z-fighting with the ecliptic plane.
        orbitGroup.getTransforms().add(new Translate(0, ORBIT_PLANE_EPSILON, 0));

        // Apply orbital rotations to match the sampling provider's position calculation:
        // JavaFX applies transforms first-to-last, so order must be:
        // 1. Argument of periapsis (ω) - rotation around Y axis (perpendicular to XZ plane)
        // 2. Inclination (i) - tilt the orbital plane around X axis
        // 3. Longitude of ascending node (Ω) - rotation around Y axis in reference plane
        // This gives R_LAN(R_inc(R_argPeri(P))) matching the position calculation.
        Rotate rotateArgPeri = new Rotate(argumentOfPeriapsisDeg, Rotate.Y_AXIS);
        Rotate rotateInclination = new Rotate(inclinationDeg, Rotate.X_AXIS);
        Rotate rotateLAN = new Rotate(longitudeOfAscendingNodeDeg, Rotate.Y_AXIS);

        orbitGroup.getTransforms().addAll(rotateArgPeri, rotateInclination, rotateLAN);

        return orbitGroup;
    }

    /**
     * Create a simple circular orbit path (for low eccentricity orbits)
     *
     * @param radiusAU       orbital radius in AU
     * @param inclinationDeg inclination in degrees
     * @param orbitColor     color for the orbit
     * @return Group containing the circular orbit path
     */
    public Group createCircularOrbitPath(double radiusAU, double inclinationDeg, Color orbitColor) {
        return createOrbitPath(radiusAU, 0.0, inclinationDeg, 0.0, 0.0, orbitColor);
    }

    /**
     * Create a solid (non-dashed) orbit path. Used for moon orbits where dashing
     * can look fragmented at small scales.
     *
     * @param semiMajorAxisAU              semi-major axis in AU
     * @param eccentricity                 orbital eccentricity
     * @param inclinationDeg               orbital inclination in degrees
     * @param longitudeOfAscendingNodeDeg  longitude of ascending node in degrees
     * @param argumentOfPeriapsisDeg       argument of periapsis in degrees
     * @param orbitColor                   color for the orbit path
     * @return Group containing the solid orbital path
     */
    public Group createSolidOrbitPath(double semiMajorAxisAU,
                                       double eccentricity,
                                       double inclinationDeg,
                                       double longitudeOfAscendingNodeDeg,
                                       double argumentOfPeriapsisDeg,
                                       Color orbitColor) {

        OrbitKey orbitKey = new OrbitKey(semiMajorAxisAU, eccentricity, inclinationDeg,
                longitudeOfAscendingNodeDeg, argumentOfPeriapsisDeg);
        int segments = selectSegmentCount(orbitKey);

        // Build solid mesh (no dashing)
        double[][] points = buildOrbitSamplePoints(semiMajorAxisAU, eccentricity, segments);
        TriangleMesh baseMesh = buildSolidRibbonMesh(points, ORBIT_RIBBON_HALF_WIDTH * 0.7);
        TriangleMesh highlightMesh = buildSolidRibbonMesh(points, ORBIT_RIBBON_HALF_WIDTH * 0.7 * ORBIT_HIGHLIGHT_SCALE);

        PhongMaterial baseMaterial = createOrbitMaterial(orbitColor, false);
        PhongMaterial highlightMaterial = createOrbitMaterial(orbitColor, true);

        MeshView baseMeshView = createOrbitMeshView(baseMesh, baseMaterial);
        MeshView highlightMeshView = createOrbitMeshView(highlightMesh, highlightMaterial);
        highlightMeshView.setVisible(false);

        Group orbitGroup = new Group(baseMeshView, highlightMeshView);
        orbitGroup.getProperties().put(ORBIT_BASE_MESH_KEY, baseMeshView);
        orbitGroup.getProperties().put(ORBIT_HIGHLIGHT_MESH_KEY, highlightMeshView);

        orbitGroup.getTransforms().add(new Translate(0, ORBIT_PLANE_EPSILON, 0));

        Rotate rotateArgPeri = new Rotate(argumentOfPeriapsisDeg, Rotate.Y_AXIS);
        Rotate rotateInclination = new Rotate(inclinationDeg, Rotate.X_AXIS);
        Rotate rotateLAN = new Rotate(longitudeOfAscendingNodeDeg, Rotate.Y_AXIS);

        orbitGroup.getTransforms().addAll(rotateArgPeri, rotateInclination, rotateLAN);

        return orbitGroup;
    }

    /**
     * Build a solid (non-dashed) ribbon mesh for smooth orbit appearance.
     */
    private TriangleMesh buildSolidRibbonMesh(double[][] points, double halfWidth) {
        List<Float> meshPoints = new ArrayList<>();
        List<Integer> meshFaces = new ArrayList<>();
        int segmentCount = points.length - 1;

        for (int i = 0; i < segmentCount; i++) {
            double[] p0 = points[i];
            double[] p1 = points[i + 1];

            double dx = p1[0] - p0[0];
            double dy = p1[1] - p0[1];
            double dz = p1[2] - p0[2];
            double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (length < MIN_TANGENT_LENGTH) {
                continue;
            }

            double sideX = dz / length;
            double sideY = 0;
            double sideZ = -dx / length;

            int baseIndex = meshPoints.size() / 3;
            addPoint(meshPoints, p0[0] + sideX * halfWidth, p0[1] + sideY * halfWidth, p0[2] + sideZ * halfWidth);
            addPoint(meshPoints, p0[0] - sideX * halfWidth, p0[1] - sideY * halfWidth, p0[2] - sideZ * halfWidth);
            addPoint(meshPoints, p1[0] + sideX * halfWidth, p1[1] + sideY * halfWidth, p1[2] + sideZ * halfWidth);
            addPoint(meshPoints, p1[0] - sideX * halfWidth, p1[1] - sideY * halfWidth, p1[2] - sideZ * halfWidth);

            addQuadFaces(meshFaces, baseIndex);
        }

        TriangleMesh mesh = new TriangleMesh();
        mesh.getPoints().setAll(toFloatArray(meshPoints));
        mesh.getTexCoords().addAll(0, 0);
        mesh.getFaces().setAll(toIntArray(meshFaces));
        return mesh;
    }

    private int selectSegmentCount(OrbitKey key) {
        OrbitLodState state = lodStates.get(key);
        if (state == null) {
            int level = levelForZoom(scaleManager.getZoomLevel());
            state = new OrbitLodState(level);
            lodStates.put(key, state);
            return LOD_SEGMENTS[level];
        }

        int level = state.lodIndex;
        double zoom = scaleManager.getZoomLevel();

        if (level < LOD_SEGMENTS.length - 1) {
            double upper = LOD_THRESHOLDS[level];
            if (zoom > upper + LOD_HYSTERESIS) {
                level++;
            }
        }

        if (level > 0) {
            double lower = LOD_THRESHOLDS[level - 1];
            if (zoom < lower - LOD_HYSTERESIS) {
                level--;
            }
        }

        state.lodIndex = level;
        return LOD_SEGMENTS[level];
    }

    private int levelForZoom(double zoom) {
        for (int i = 0; i < LOD_THRESHOLDS.length; i++) {
            if (zoom < LOD_THRESHOLDS[i]) {
                return i;
            }
        }
        return LOD_SEGMENTS.length - 1;
    }

    private OrbitMeshPair buildOrbitMeshPair(double semiMajorAxisAU, double eccentricity, int segments) {
        double[][] points = buildOrbitSamplePoints(semiMajorAxisAU, eccentricity, segments);
        TriangleMesh baseMesh = buildRibbonMesh(points, ORBIT_RIBBON_HALF_WIDTH);
        TriangleMesh highlightMesh = buildRibbonMesh(points, ORBIT_RIBBON_HALF_WIDTH * ORBIT_HIGHLIGHT_SCALE);
        return new OrbitMeshPair(baseMesh, highlightMesh);
    }

    private double[][] buildOrbitSamplePoints(double semiMajorAxisAU, double eccentricity, int segments) {
        double[][] orbitPlanePoints = orbitSamplingProvider.sampleEllipsePlanePointsAu(
                semiMajorAxisAU, eccentricity, segments);
        double[][] screenPoints = new double[orbitPlanePoints.length][3];
        for (int i = 0; i < orbitPlanePoints.length; i++) {
            double[] point = orbitPlanePoints[i];
            screenPoints[i] = scaleManager.auVectorToScreen(point[0], point[1], point[2]);
        }
        return screenPoints;
    }

    private TriangleMesh buildRibbonMesh(double[][] points, double halfWidth) {
        List<Float> meshPoints = new ArrayList<>();
        List<Integer> meshFaces = new ArrayList<>();
        int segmentCount = points.length - 1;

        for (int i = 0; i < segmentCount; i++) {
            if (!isDashSegment(i)) {
                continue;
            }
            double[] p0 = points[i];
            double[] p1 = points[i + 1];

            double dx = p1[0] - p0[0];
            double dy = p1[1] - p0[1];
            double dz = p1[2] - p0[2];
            double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (length < MIN_TANGENT_LENGTH) {
                continue;
            }

            // In the local orbit plane, the normal is Y. Side vector is perpendicular in-plane.
            double sideX = dz / length;
            double sideY = 0;
            double sideZ = -dx / length;

            int baseIndex = meshPoints.size() / 3;
            addPoint(meshPoints, p0[0] + sideX * halfWidth, p0[1] + sideY * halfWidth, p0[2] + sideZ * halfWidth);
            addPoint(meshPoints, p0[0] - sideX * halfWidth, p0[1] - sideY * halfWidth, p0[2] - sideZ * halfWidth);
            addPoint(meshPoints, p1[0] + sideX * halfWidth, p1[1] + sideY * halfWidth, p1[2] + sideZ * halfWidth);
            addPoint(meshPoints, p1[0] - sideX * halfWidth, p1[1] - sideY * halfWidth, p1[2] - sideZ * halfWidth);

            addQuadFaces(meshFaces, baseIndex);
        }

        TriangleMesh mesh = new TriangleMesh();
        mesh.getPoints().setAll(toFloatArray(meshPoints));
        mesh.getTexCoords().addAll(0, 0);
        mesh.getFaces().setAll(toIntArray(meshFaces));
        return mesh;
    }

    private boolean isDashSegment(int index) {
        int patternLength = DASH_ON_SEGMENTS + DASH_OFF_SEGMENTS;
        return index % patternLength < DASH_ON_SEGMENTS;
    }

    private MeshView createOrbitMeshView(TriangleMesh mesh, PhongMaterial material) {
        MeshView meshView = new MeshView(mesh);
        meshView.setMaterial(material);
        meshView.setCullFace(CullFace.NONE);
        meshView.setDrawMode(DrawMode.FILL);
        return meshView;
    }

    private PhongMaterial createOrbitMaterial(Color baseColor, boolean highlight) {
        PhongMaterial material = new PhongMaterial();
        if (highlight) {
            material.setDiffuseColor(baseColor.brighter().brighter());
            material.setSpecularColor(baseColor.brighter());
        } else {
            material.setDiffuseColor(baseColor);
            material.setSpecularColor(baseColor.brighter());
        }
        return material;
    }

    private void addPoint(List<Float> points, double x, double y, double z) {
        points.add((float) x);
        points.add((float) y);
        points.add((float) z);
    }

    private void addQuadFaces(List<Integer> faces, int baseIndex) {
        // Triangle 1: 0-2-1
        faces.add(baseIndex);
        faces.add(0);
        faces.add(baseIndex + 2);
        faces.add(0);
        faces.add(baseIndex + 1);
        faces.add(0);

        // Triangle 2: 1-2-3
        faces.add(baseIndex + 1);
        faces.add(0);
        faces.add(baseIndex + 2);
        faces.add(0);
        faces.add(baseIndex + 3);
        faces.add(0);
    }

    private float[] toFloatArray(List<Float> values) {
        float[] array = new float[values.size()];
        for (int i = 0; i < values.size(); i++) {
            array[i] = values.get(i);
        }
        return array;
    }

    private int[] toIntArray(List<Integer> values) {
        int[] array = new int[values.size()];
        for (int i = 0; i < values.size(); i++) {
            array[i] = values.get(i);
        }
        return array;
    }

    /**
     * Create a marker sphere at a position (for debugging or highlighting points)
     */
    public Sphere createPositionMarker(double[] screenPosition, double radius, Color color) {
        Sphere marker = new Sphere(radius);
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(color);
        marker.setMaterial(material);
        marker.setTranslateX(screenPosition[0]);
        marker.setTranslateY(screenPosition[1]);
        marker.setTranslateZ(screenPosition[2]);
        return marker;
    }

    private record OrbitMeshPair(TriangleMesh baseMesh, TriangleMesh highlightMesh) {
    }

    private static class OrbitLodState {
        private int lodIndex;

        private OrbitLodState(int lodIndex) {
            this.lodIndex = lodIndex;
        }
    }

    private static class OrbitKey {
        private final long a;
        private final long e;
        private final long inclination;
        private final long lan;
        private final long argPeriapsis;

        private OrbitKey(double semiMajorAxisAU, double eccentricity, double inclinationDeg,
                         double longitudeOfAscendingNodeDeg, double argumentOfPeriapsisDeg) {
            this.a = quantize(semiMajorAxisAU, 1_000_000);
            this.e = quantize(eccentricity, 1_000_000);
            this.inclination = quantize(inclinationDeg, 1_000_000);
            this.lan = quantize(longitudeOfAscendingNodeDeg, 1_000_000);
            this.argPeriapsis = quantize(argumentOfPeriapsisDeg, 1_000_000);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            OrbitKey orbitKey = (OrbitKey) o;
            return a == orbitKey.a
                    && e == orbitKey.e
                    && inclination == orbitKey.inclination
                    && lan == orbitKey.lan
                    && argPeriapsis == orbitKey.argPeriapsis;
        }

        @Override
        public int hashCode() {
            return Objects.hash(a, e, inclination, lan, argPeriapsis);
        }
    }

    private static class OrbitMeshKey {
        private final long a;
        private final long e;
        private final int segments;
        private final long baseScale;
        private final long zoomLevel;
        private final boolean logScale;

        private OrbitMeshKey(double semiMajorAxisAU, double eccentricity, int segments,
                             double baseScale, double zoomLevel, boolean logScale) {
            this.a = quantize(semiMajorAxisAU, 1_000_000);
            this.e = quantize(eccentricity, 1_000_000);
            this.segments = segments;
            this.baseScale = quantize(baseScale, 10_000);
            this.zoomLevel = quantize(zoomLevel, 10_000);
            this.logScale = logScale;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            OrbitMeshKey that = (OrbitMeshKey) o;
            return a == that.a
                    && e == that.e
                    && segments == that.segments
                    && baseScale == that.baseScale
                    && zoomLevel == that.zoomLevel
                    && logScale == that.logScale;
        }

        @Override
        public int hashCode() {
            return Objects.hash(a, e, segments, baseScale, zoomLevel, logScale);
        }
    }

    private static long quantize(double value, double scale) {
        return Math.round(value * scale);
    }
}
