package com.teamgannon.trips.planetarymodelling.procedural;

import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;
import org.hipparchus.geometry.euclidean.threed.Vector3D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts procedural planet icosahedral mesh to JavaFX TriangleMesh.
 * Handles both pentagons (5 vertices) and hexagons (6 vertices) by
 * triangulating from center point using fan triangulation.
 * <p>
 * Supports multiple rendering modes:
 * - Basic: Integer height displacement with faceted terrain
 * - Smooth: Precise height displacement with interpolated colors
 * - Per-height: Separate meshes per height level for distinct coloring
 */
public class JavaFxPlanetMeshConverter {

    // Height displacement scale factor (how much height affects radial distance).
    // Tuned lower to avoid exaggerated mountain relief in JavaFX rendering.
    private static final double HEIGHT_SCALE = 0.02;

    // Edge scale equals center scale for seamless polygon transitions.
    private static final double EDGE_HEIGHT_SCALE = 0.02;

    // Enable debug logging (set to false for production)
    private static final boolean DEBUG_LOGGING = false;

    /**
     * Options for mesh conversion controlling averaging, normals, and grouping.
     *
     * @param useAveraging      Whether to average heights at shared vertices
     * @param useNormals        Whether to generate per-vertex normals for smooth shading
     * @param groupByValue      Whether to create separate meshes per height/rainfall value
     * @param preciseHeights    Optional precise (double) heights for smoother gradations
     * @param adjacency         Optional adjacency graph (required for center averaging)
     */
    public record MeshConversionOptions(
        boolean useAveraging,
        boolean useNormals,
        boolean groupByValue,
        double[] preciseHeights,
        AdjacencyGraph adjacency
    ) {
        /** Default options: no averaging, no normals, single mesh */
        public static MeshConversionOptions defaults() {
            return new MeshConversionOptions(false, false, false, null, null);
        }

        /** Options for smooth terrain with averaged heights and normals */
        public static MeshConversionOptions smooth(AdjacencyGraph adjacency) {
            return new MeshConversionOptions(true, true, false, null, adjacency);
        }

        /** Options for per-height colored meshes with averaging and normals */
        public static MeshConversionOptions byHeight(AdjacencyGraph adjacency) {
            return new MeshConversionOptions(true, true, true, null, adjacency);
        }

        /** Options with precise heights for finer gradations */
        public static MeshConversionOptions withPreciseHeights(double[] heights, AdjacencyGraph adjacency) {
            return new MeshConversionOptions(true, true, true, heights, adjacency);
        }
    }

    // Height color mapping (same as PlanetRenderer)
    private static final Color[] HEIGHT_COLORS = {
        Color.rgb(0, 0, 102),     // -4: deep ocean
        Color.rgb(0, 0, 128),     // -3: ocean
        Color.rgb(51, 77, 230),   // -2: shallow ocean
        Color.rgb(102, 153, 255), // -1: coastal
        Color.rgb(204, 204, 153), //  0: lowlands
        Color.rgb(166, 204, 102), //  1: plains
        Color.rgb(166, 153, 102), //  2: hills
        Color.rgb(102, 51, 0),    //  3: mountains
        Color.rgb(51, 0, 0)       //  4: high mountains
    };

    /**
     * Convert procedural planet mesh to JavaFX TriangleMesh.
     * Uses fan triangulation from polygon center to edges.
     * Edge vertices are displaced by height to create proper terrain relief.
     *
     * @param polygons The icosahedral mesh polygons
     * @param heights  Height values per polygon (for vertex displacement)
     * @param scale    Scale factor for rendering
     * @return JavaFX TriangleMesh ready for MeshView
     */
    public static TriangleMesh convert(List<Polygon> polygons, int[] heights, double scale) {
        TriangleMesh mesh = new TriangleMesh();

        List<Float> points = new ArrayList<>();
        List<Integer> faces = new ArrayList<>();
        List<Float> texCoords = new ArrayList<>();

        // Single texture coordinate pair (we use vertex colors via material)
        texCoords.add(0.5f);
        texCoords.add(0.5f);

        int vertexIndex = 0;

        // For each polygon, create triangles using fan triangulation
        for (int polyIdx = 0; polyIdx < polygons.size(); polyIdx++) {
            Polygon poly = polygons.get(polyIdx);
            int height = heights[polyIdx];

            // Calculate radial displacement from height
            // Center gets full displacement, edges get slightly less for beveled look
            double centerDisplacement = 1.0 + height * HEIGHT_SCALE;
            double edgeDisplacement = 1.0 + height * EDGE_HEIGHT_SCALE;

            // Add center point (displaced by height)
            Vector3D center = poly.center().normalize().scalarMultiply(centerDisplacement * scale);
            points.add((float) center.getX());
            points.add((float) center.getY());
            points.add((float) center.getZ());
            int centerIdx = vertexIndex++;

            // Add edge vertices (also displaced by height for terrain relief)
            List<Vector3D> vertices = poly.vertices();
            int[] edgeIndices = new int[vertices.size()];

            for (int i = 0; i < vertices.size(); i++) {
                Vector3D v = vertices.get(i).normalize().scalarMultiply(edgeDisplacement * scale);
                points.add((float) v.getX());
                points.add((float) v.getY());
                points.add((float) v.getZ());
                edgeIndices[i] = vertexIndex++;
            }

            // Create fan triangles from center to edges
            // For polygon with n vertices, creates n triangles
            for (int i = 0; i < vertices.size(); i++) {
                int nextI = (i + 1) % vertices.size();

                // Face indices: p0, t0, p1, t1, p2, t2
                // All use same texture coord (0)
                faces.add(centerIdx);
                faces.add(0);
                faces.add(edgeIndices[i]);
                faces.add(0);
                faces.add(edgeIndices[nextI]);
                faces.add(0);
            }
        }

        // Convert to arrays
        float[] pointsArray = new float[points.size()];
        for (int i = 0; i < points.size(); i++) {
            pointsArray[i] = points.get(i);
        }

        int[] facesArray = new int[faces.size()];
        for (int i = 0; i < faces.size(); i++) {
            facesArray[i] = faces.get(i);
        }

        float[] texCoordsArray = new float[texCoords.size()];
        for (int i = 0; i < texCoords.size(); i++) {
            texCoordsArray[i] = texCoords.get(i);
        }

        mesh.getPoints().addAll(pointsArray);
        mesh.getTexCoords().addAll(texCoordsArray);
        mesh.getFaces().addAll(facesArray);

        return mesh;
    }

    /**
     * Convert procedural planet mesh to JavaFX TriangleMesh with properly averaged heights.
     * Builds a global vertex index and averages heights from ALL polygons sharing each vertex,
     * eliminating gaps/overlaps at shared edges for smooth terrain transitions.
     *
     * @param polygons  The icosahedral mesh polygons
     * @param heights   Height values per polygon
     * @param adjacency The polygon adjacency graph (unused, kept for API compatibility)
     * @param scale     Scale factor for rendering
     * @return JavaFX TriangleMesh with smooth vertex displacement
     */
    public static TriangleMesh convertWithAveraging(
            List<Polygon> polygons, int[] heights, AdjacencyGraph adjacency, double scale) {

        // Build vertex indexing data
        VertexData vertexData = buildVertexData(polygons);
        double[] averagedHeights = computeAveragedHeights(vertexData, heights);

        TriangleMesh mesh = new TriangleMesh();

        List<Float> points = new ArrayList<>();
        List<Integer> faces = new ArrayList<>();
        List<Float> texCoords = new ArrayList<>();

        texCoords.add(0.5f);
        texCoords.add(0.5f);

        int vertexIndex = 0;

        for (int polyIdx = 0; polyIdx < polygons.size(); polyIdx++) {
            Polygon poly = polygons.get(polyIdx);
            int height = heights[polyIdx];

            // Center uses polygon's own height
            double centerDisplacement = 1.0 + height * HEIGHT_SCALE;

            Vector3D center = poly.center().normalize().scalarMultiply(centerDisplacement * scale);
            points.add((float) center.getX());
            points.add((float) center.getY());
            points.add((float) center.getZ());
            int centerIdx = vertexIndex++;

            List<Vector3D> vertices = poly.vertices();
            int[] polyVertIndices = vertexData.polygonVertexIndices[polyIdx];
            int[] edgeIndices = new int[vertices.size()];

            for (int i = 0; i < vertices.size(); i++) {
                Vector3D vertex = vertices.get(i);

                // Use pre-computed averaged height for this vertex
                int globalVertIdx = polyVertIndices[i];
                double avgHeight = averagedHeights[globalVertIdx];

                double edgeDisplacement = 1.0 + avgHeight * EDGE_HEIGHT_SCALE;
                Vector3D v = vertex.normalize().scalarMultiply(edgeDisplacement * scale);

                points.add((float) v.getX());
                points.add((float) v.getY());
                points.add((float) v.getZ());
                edgeIndices[i] = vertexIndex++;
            }

            // Create fan triangles
            for (int i = 0; i < vertices.size(); i++) {
                int nextI = (i + 1) % vertices.size();
                faces.add(centerIdx);
                faces.add(0);
                faces.add(edgeIndices[i]);
                faces.add(0);
                faces.add(edgeIndices[nextI]);
                faces.add(0);
            }
        }

        float[] pointsArray = toFloatArray(points);
        int[] facesArray = toIntArray(faces);
        float[] texCoordsArray = toFloatArray(texCoords);

        mesh.getPoints().addAll(pointsArray);
        mesh.getTexCoords().addAll(texCoordsArray);
        mesh.getFaces().addAll(facesArray);

        return mesh;
    }

    /**
     * Build a global vertex list and vertex-to-polygon map.
     * Uses a spatial hash grid for efficient vertex deduplication.
     */
    private static VertexData buildVertexData(List<Polygon> polygons) {
        List<Vector3D> uniqueVertices = new ArrayList<>();
        List<List<Integer>> vertexToPolygons = new ArrayList<>();
        int[][] polygonVertexIndices = new int[polygons.size()][];

        // Use a map with quantized position hash as key for O(1) lookup
        // More efficient than string concatenation in hot path
        Map<Long, Integer> positionToIndex = new HashMap<>();

        for (int polyIdx = 0; polyIdx < polygons.size(); polyIdx++) {
            Polygon poly = polygons.get(polyIdx);
            List<Vector3D> verts = poly.vertices();
            polygonVertexIndices[polyIdx] = new int[verts.size()];

            for (int i = 0; i < verts.size(); i++) {
                Vector3D v = verts.get(i);
                long key = quantizePositionHash(v);

                Integer existingIdx = positionToIndex.get(key);
                if (existingIdx != null) {
                    // Found existing vertex at this grid cell
                    polygonVertexIndices[polyIdx][i] = existingIdx;
                    vertexToPolygons.get(existingIdx).add(polyIdx);
                } else {
                    // New unique vertex
                    int newIdx = uniqueVertices.size();
                    uniqueVertices.add(v);
                    positionToIndex.put(key, newIdx);
                    List<Integer> polyList = new ArrayList<>();
                    polyList.add(polyIdx);
                    vertexToPolygons.add(polyList);
                    polygonVertexIndices[polyIdx][i] = newIdx;
                }
            }
        }

        if (DEBUG_LOGGING) {
            // Debug: verify vertex sharing statistics
            int totalShared = 0;
            int maxShared = 0;
            for (List<Integer> polys : vertexToPolygons) {
                if (polys.size() > 1) totalShared++;
                if (polys.size() > maxShared) maxShared = polys.size();
            }
            System.out.println("[VertexData] Polygons: " + polygons.size() +
                ", Unique vertices: " + uniqueVertices.size() +
                ", Shared vertices: " + totalShared +
                ", Max polys/vertex: " + maxShared);
        }

        return new VertexData(uniqueVertices, polygonVertexIndices, vertexToPolygons);
    }

    /**
     * Quantize vertex position to a hash key for spatial lookup.
     * Uses bit-packing instead of string concatenation for efficiency.
     * Grid resolution is 0.0001 on unit sphere (~0.01% of radius).
     *
     * @param v Vertex position
     * @return Hash key combining quantized x, y, z coordinates
     */
    private static long quantizePositionHash(Vector3D v) {
        // Quantize to 4 decimal places (0.0001 resolution)
        // Each coordinate fits in 21 bits for range [-1048576, 1048575] (±104.8 on unit sphere)
        int qx = (int) Math.round(v.getX() * 10000);
        int qy = (int) Math.round(v.getY() * 10000);
        int qz = (int) Math.round(v.getZ() * 10000);
        // Pack into 64-bit long: bits 42-62 for x, 21-41 for y, 0-20 for z
        return ((long) (qx + 1048576) << 42) | ((long) (qy + 1048576) << 21) | (qz + 1048576);
    }

    /**
     * Compute averaged heights for all unique vertices.
     */
    private static double[] computeAveragedHeights(VertexData vertexData, int[] heights) {
        double[] averaged = new double[vertexData.uniqueVertices.size()];

        int smoothedCount = 0; // vertices where averaging changed the value

        for (int vIdx = 0; vIdx < averaged.length; vIdx++) {
            List<Integer> polys = vertexData.vertexToPolygons.get(vIdx);
            double sum = 0;
            int minH = Integer.MAX_VALUE;
            int maxH = Integer.MIN_VALUE;
            for (int polyIdx : polys) {
                int h = heights[polyIdx];
                sum += h;
                if (h < minH) minH = h;
                if (h > maxH) maxH = h;
            }
            averaged[vIdx] = sum / polys.size();

            // Count vertices where averaging made a difference
            if (polys.size() > 1 && maxH != minH) {
                smoothedCount++;
            }
        }

        if (DEBUG_LOGGING) {
            System.out.println("[AveragedHeights] Total vertices: " + averaged.length +
                ", Smoothed (different neighbors): " + smoothedCount);
        }

        return averaged;
    }

    /**
     * Compute averaged heights for polygon centers based on their neighbors.
     */
    private static double[] computeAveragedCenterHeights(
            List<Polygon> polygons, int[] heights, AdjacencyGraph adjacency) {

        double[] averaged = new double[polygons.size()];

        for (int polyIdx = 0; polyIdx < polygons.size(); polyIdx++) {
            int[] neighbors = adjacency.neighbors(polyIdx); // includes self
            double sum = 0;
            for (int neighborIdx : neighbors) {
                sum += heights[neighborIdx];
            }
            averaged[polyIdx] = sum / neighbors.length;
        }

        return averaged;
    }

    /**
     * Compute averaged heights for edge vertices using precise (double) heights.
     * This provides finer gradations than integer heights for smoother terrain.
     */
    private static double[] computeAveragedHeightsPrecise(VertexData vertexData, double[] preciseHeights) {
        double[] averaged = new double[vertexData.uniqueVertices.size()];

        int smoothedCount = 0;

        for (int vIdx = 0; vIdx < averaged.length; vIdx++) {
            List<Integer> polys = vertexData.vertexToPolygons.get(vIdx);
            double sum = 0;
            double minH = Double.MAX_VALUE;
            double maxH = Double.MIN_VALUE;
            for (int polyIdx : polys) {
                double h = preciseHeights[polyIdx];
                sum += h;
                if (h < minH) minH = h;
                if (h > maxH) maxH = h;
            }
            averaged[vIdx] = sum / polys.size();

            // Count vertices where averaging made a difference (threshold: 0.1)
            if (polys.size() > 1 && (maxH - minH) > 0.1) {
                smoothedCount++;
            }
        }

        if (DEBUG_LOGGING) {
            System.out.println("[AveragedHeightsPrecise] Total vertices: " + averaged.length +
                ", Smoothed (different neighbors): " + smoothedCount);
        }

        return averaged;
    }

    /**
     * Compute averaged heights for polygon centers using precise (double) heights.
     */
    private static double[] computeAveragedCenterHeightsPrecise(
            List<Polygon> polygons, double[] preciseHeights, AdjacencyGraph adjacency) {

        double[] averaged = new double[polygons.size()];

        for (int polyIdx = 0; polyIdx < polygons.size(); polyIdx++) {
            int[] neighbors = adjacency.neighbors(polyIdx); // includes self
            double sum = 0;
            for (int neighborIdx : neighbors) {
                sum += preciseHeights[neighborIdx];
            }
            averaged[polyIdx] = sum / neighbors.length;
        }

        return averaged;
    }


    /**
     * Container for vertex indexing data.
     */
    private record VertexData(
        List<Vector3D> uniqueVertices,
        int[][] polygonVertexIndices,  // [polyIdx][localVertIdx] -> globalVertIdx
        List<List<Integer>> vertexToPolygons  // globalVertIdx -> list of polyIdx
    ) {}

    /**
     * Convert List<Float> to float[].
     */
    private static float[] toFloatArray(List<Float> list) {
        float[] array = new float[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    /**
     * Convert List<Integer> to int[].
     */
    private static int[] toIntArray(List<Integer> list) {
        int[] array = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    /**
     * Convert procedural planet mesh to JavaFX TriangleMesh with smooth heights.
     * Uses precise heights for smoother displacement and finer terrain gradations.
     *
     * @param polygons       The icosahedral mesh polygons
     * @param preciseHeights Precise height values per polygon
     * @param scale          Scale factor for rendering
     * @return JavaFX TriangleMesh ready for MeshView
     */
    public static TriangleMesh convertSmooth(List<Polygon> polygons, double[] preciseHeights, double scale) {
        TriangleMesh mesh = new TriangleMesh();

        List<Float> points = new ArrayList<>();
        List<Integer> faces = new ArrayList<>();
        List<Float> texCoords = new ArrayList<>();

        texCoords.add(0.5f);
        texCoords.add(0.5f);

        int vertexIndex = 0;

        for (int polyIdx = 0; polyIdx < polygons.size(); polyIdx++) {
            Polygon poly = polygons.get(polyIdx);
            double height = preciseHeights != null && polyIdx < preciseHeights.length
                ? preciseHeights[polyIdx]
                : 0.0;

            // For smooth mode, center and edge use the same displacement
            // to create more natural continuous terrain
            double centerDisplacement = 1.0 + height * HEIGHT_SCALE;
            double edgeDisplacement = 1.0 + height * EDGE_HEIGHT_SCALE;

            Vector3D center = poly.center().normalize().scalarMultiply(centerDisplacement * scale);
            points.add((float) center.getX());
            points.add((float) center.getY());
            points.add((float) center.getZ());
            int centerIdx = vertexIndex++;

            List<Vector3D> vertices = poly.vertices();
            int[] edgeIndices = new int[vertices.size()];

            for (int i = 0; i < vertices.size(); i++) {
                Vector3D v = vertices.get(i).normalize().scalarMultiply(edgeDisplacement * scale);
                points.add((float) v.getX());
                points.add((float) v.getY());
                points.add((float) v.getZ());
                edgeIndices[i] = vertexIndex++;
            }

            for (int i = 0; i < vertices.size(); i++) {
                int nextI = (i + 1) % vertices.size();
                faces.add(centerIdx);
                faces.add(0);
                faces.add(edgeIndices[i]);
                faces.add(0);
                faces.add(edgeIndices[nextI]);
                faces.add(0);
            }
        }

        float[] pointsArray = new float[points.size()];
        for (int i = 0; i < points.size(); i++) {
            pointsArray[i] = points.get(i);
        }

        int[] facesArray = new int[faces.size()];
        for (int i = 0; i < faces.size(); i++) {
            facesArray[i] = faces.get(i);
        }

        float[] texCoordsArray = new float[texCoords.size()];
        for (int i = 0; i < texCoords.size(); i++) {
            texCoordsArray[i] = texCoords.get(i);
        }

        mesh.getPoints().addAll(pointsArray);
        mesh.getTexCoords().addAll(texCoordsArray);
        mesh.getFaces().addAll(facesArray);

        return mesh;
    }

    // ==================== Unified Conversion API ====================

    /**
     * Unified mesh conversion method supporting all rendering modes.
     * This is the recommended entry point for new code.
     *
     * @param polygons The icosahedral mesh polygons
     * @param heights  Integer height values per polygon
     * @param scale    Scale factor for rendering
     * @param options  Conversion options controlling averaging, normals, and grouping
     * @return Single TriangleMesh (if groupByValue=false) or throws if groupByValue=true
     */
    public static TriangleMesh convertUnified(
            List<Polygon> polygons, int[] heights, double scale, MeshConversionOptions options) {

        if (options.groupByValue()) {
            throw new IllegalArgumentException(
                "Use convertUnifiedByHeight() for grouped meshes (groupByValue=true)");
        }

        if (options.useAveraging()) {
            return convertWithAveragingInternal(polygons, heights, options, scale);
        } else {
            return convert(polygons, heights, scale);
        }
    }

    /**
     * Unified mesh conversion returning separate meshes per height level.
     * Each mesh can be colored independently for distinct terrain visualization.
     *
     * @param polygons The icosahedral mesh polygons
     * @param heights  Integer height values per polygon
     * @param scale    Scale factor for rendering
     * @param options  Conversion options (groupByValue should be true)
     * @return Map of height value to TriangleMesh
     */
    public static Map<Integer, TriangleMesh> convertUnifiedByHeight(
            List<Polygon> polygons, int[] heights, double scale, MeshConversionOptions options) {

        if (options.useAveraging()) {
            return convertByHeightWithAveraging(
                polygons, heights, options.adjacency(), scale, options.preciseHeights());
        } else {
            return convertByHeight(polygons, heights, scale);
        }
    }

    /**
     * Internal method for averaged conversion with options.
     */
    private static TriangleMesh convertWithAveragingInternal(
            List<Polygon> polygons, int[] heights, MeshConversionOptions options, double scale) {

        VertexData vertexData = buildVertexData(polygons);

        double[] averagedHeights;
        if (options.preciseHeights() != null) {
            averagedHeights = computeAveragedHeightsPrecise(vertexData, options.preciseHeights());
        } else {
            averagedHeights = computeAveragedHeights(vertexData, heights);
        }

        TriangleMesh mesh = new TriangleMesh();
        if (options.useNormals()) {
            mesh.setVertexFormat(VertexFormat.POINT_NORMAL_TEXCOORD);
        }

        List<Float> points = new ArrayList<>();
        List<Float> normals = options.useNormals() ? new ArrayList<>() : null;
        List<Integer> faces = new ArrayList<>();

        mesh.getTexCoords().addAll(0.5f, 0.5f);

        int vertexIndex = 0;

        for (int polyIdx = 0; polyIdx < polygons.size(); polyIdx++) {
            Polygon poly = polygons.get(polyIdx);
            List<Vector3D> vertices = poly.vertices();
            int[] polyVertIndices = vertexData.polygonVertexIndices[polyIdx];

            // Center height averaged from edge vertices
            double centerSum = 0.0;
            for (int local = 0; local < polyVertIndices.length; local++) {
                centerSum += averagedHeights[polyVertIndices[local]];
            }
            double centerHeight = centerSum / polyVertIndices.length;
            double centerDisplacement = 1.0 + centerHeight * HEIGHT_SCALE;

            Vector3D center = poly.center().normalize().scalarMultiply(centerDisplacement * scale);
            points.add((float) center.getX());
            points.add((float) center.getY());
            points.add((float) center.getZ());

            if (options.useNormals()) {
                Vector3D centerNormal = center.normalize();
                normals.add((float) centerNormal.getX());
                normals.add((float) centerNormal.getY());
                normals.add((float) centerNormal.getZ());
            }

            int centerIdx = vertexIndex++;
            int[] edgeIndices = new int[vertices.size()];

            for (int i = 0; i < vertices.size(); i++) {
                int globalVertIdx = polyVertIndices[i];
                double avgHeight = averagedHeights[globalVertIdx];
                double edgeDisplacement = 1.0 + avgHeight * EDGE_HEIGHT_SCALE;

                Vector3D v = vertices.get(i).normalize().scalarMultiply(edgeDisplacement * scale);
                points.add((float) v.getX());
                points.add((float) v.getY());
                points.add((float) v.getZ());

                if (options.useNormals()) {
                    Vector3D edgeNormal = v.normalize();
                    normals.add((float) edgeNormal.getX());
                    normals.add((float) edgeNormal.getY());
                    normals.add((float) edgeNormal.getZ());
                }

                edgeIndices[i] = vertexIndex++;
            }

            // Create fan triangles
            addFanTriangles(faces, centerIdx, edgeIndices, vertices.size(), options.useNormals());
        }

        mesh.getPoints().addAll(toFloatArray(points));
        if (options.useNormals() && normals != null) {
            mesh.getNormals().addAll(toFloatArray(normals));
        }
        mesh.getFaces().addAll(toIntArray(faces));

        return mesh;
    }

    /**
     * Creates fan triangles from center vertex to edge vertices.
     *
     * @param faces       List to add face indices to
     * @param centerIdx   Index of center vertex
     * @param edgeIndices Indices of edge vertices
     * @param vertCount   Number of vertices (5 for pentagon, 6 for hexagon)
     * @param useNormals  Whether using POINT_NORMAL_TEXCOORD format
     */
    private static void addFanTriangles(List<Integer> faces, int centerIdx,
            int[] edgeIndices, int vertCount, boolean useNormals) {

        for (int i = 0; i < vertCount; i++) {
            int nextI = (i + 1) % vertCount;

            if (useNormals) {
                // Face format for POINT_NORMAL_TEXCOORD: p0, n0, t0, p1, n1, t1, p2, n2, t2
                faces.add(centerIdx);        // point
                faces.add(centerIdx);        // normal (same as point for radial normals)
                faces.add(0);                // tex coord
                faces.add(edgeIndices[i]);
                faces.add(edgeIndices[i]);
                faces.add(0);
                faces.add(edgeIndices[nextI]);
                faces.add(edgeIndices[nextI]);
                faces.add(0);
            } else {
                // Face format for POINT_TEXCOORD: p0, t0, p1, t1, p2, t2
                faces.add(centerIdx);
                faces.add(0);
                faces.add(edgeIndices[i]);
                faces.add(0);
                faces.add(edgeIndices[nextI]);
                faces.add(0);
            }
        }
    }

    // ==================== Material Creation Methods ====================

    /**
     * Creates PhongMaterial with terrain colors based on heights.
     * Generates a texture map where each polygon gets a color based on its height.
     *
     * @param polygons The icosahedral mesh polygons
     * @param heights  Height values per polygon
     * @return PhongMaterial with height-based diffuse color map
     */
    public static PhongMaterial createTerrainMaterial(List<Polygon> polygons, int[] heights) {
        PhongMaterial material = new PhongMaterial();

        // Create a diffuse texture based on average height
        int avgHeight = calculateAverageHeight(heights);
        Color baseColor = getColorForHeight(avgHeight);

        material.setDiffuseColor(baseColor);
        material.setSpecularColor(Color.WHITE.deriveColor(0, 1, 0.3, 1));
        material.setSpecularPower(8.0);

        return material;
    }

    /**
     * Creates PhongMaterial with a generated height-map texture.
     * Each polygon's color is baked into a 2D texture for more detailed rendering.
     *
     * @param polygons The icosahedral mesh polygons
     * @param heights  Height values per polygon
     * @return PhongMaterial with diffuse texture map
     */
    public static PhongMaterial createTexturedTerrainMaterial(List<Polygon> polygons, int[] heights) {
        PhongMaterial material = new PhongMaterial();

        // Generate a simple gradient texture based on height distribution
        int textureSize = 256;
        WritableImage diffuseMap = new WritableImage(textureSize, textureSize);
        PixelWriter writer = diffuseMap.getPixelWriter();

        // Create a latitude-based gradient representing terrain
        for (int y = 0; y < textureSize; y++) {
            // Map y to latitude (-90 to 90)
            double lat = ((double) y / textureSize - 0.5) * 180.0;

            // Determine base color based on latitude (polar ice, temperate, tropical)
            Color rowColor;
            if (Math.abs(lat) > 70) {
                // Polar - ice
                rowColor = Color.WHITE.interpolate(Color.LIGHTCYAN, 0.7);
            } else if (Math.abs(lat) > 50) {
                // Subpolar - tundra
                rowColor = Color.rgb(180, 190, 170);
            } else if (Math.abs(lat) > 30) {
                // Temperate - forest/plains
                rowColor = Color.rgb(100, 140, 80);
            } else {
                // Tropical - desert/jungle
                rowColor = Color.rgb(160, 150, 100);
            }

            for (int x = 0; x < textureSize; x++) {
                writer.setColor(x, y, rowColor);
            }
        }

        material.setDiffuseMap(diffuseMap);
        material.setSpecularColor(Color.WHITE.deriveColor(0, 1, 0.2, 1));
        material.setSpecularPower(10.0);

        return material;
    }

    /**
     * Creates PhongMaterial using precise heights for smooth color gradients.
     *
     * @param polygons       The icosahedral mesh polygons
     * @param preciseHeights Precise height values per polygon
     * @return PhongMaterial with smooth height-based coloring
     */
    public static PhongMaterial createSmoothTerrainMaterial(List<Polygon> polygons, double[] preciseHeights) {
        PhongMaterial material = new PhongMaterial();

        // Calculate average height for base color
        double avgHeight = 0;
        if (preciseHeights != null && preciseHeights.length > 0) {
            for (double h : preciseHeights) {
                avgHeight += h;
            }
            avgHeight /= preciseHeights.length;
        }

        Color baseColor = getColorForPreciseHeight(avgHeight);
        material.setDiffuseColor(baseColor);
        material.setSpecularColor(Color.WHITE.deriveColor(0, 1, 0.3, 1));
        material.setSpecularPower(8.0);

        return material;
    }

    /**
     * Creates a per-polygon colored mesh using vertex colors.
     * This approach creates separate meshes per height level for distinct coloring.
     * Each height group gets proper terrain displacement.
     *
     * @param polygons The icosahedral mesh polygons
     * @param heights  Height values per polygon
     * @param scale    Scale factor for rendering
     * @return Map of height value to TriangleMesh (for separate coloring)
     */
    public static Map<Integer, TriangleMesh> convertByHeight(List<Polygon> polygons, int[] heights, double scale) {
        Map<Integer, List<Integer>> heightToPolygons = new HashMap<>();

        // Group polygons by height
        for (int i = 0; i < polygons.size(); i++) {
            int height = heights[i];
            heightToPolygons.computeIfAbsent(height, k -> new ArrayList<>()).add(i);
        }

        Map<Integer, TriangleMesh> result = new HashMap<>();

        // Create separate mesh for each height level
        for (Map.Entry<Integer, List<Integer>> entry : heightToPolygons.entrySet()) {
            int height = entry.getKey();
            List<Integer> polyIndices = entry.getValue();

            TriangleMesh mesh = new TriangleMesh();
            List<Float> points = new ArrayList<>();
            List<Integer> faces = new ArrayList<>();

            mesh.getTexCoords().addAll(0.5f, 0.5f);

            int vertexIndex = 0;
            double centerDisplacement = 1.0 + height * HEIGHT_SCALE;
            double edgeDisplacement = 1.0 + height * EDGE_HEIGHT_SCALE;

            for (int polyIdx : polyIndices) {
                Polygon poly = polygons.get(polyIdx);

                Vector3D center = poly.center().normalize().scalarMultiply(centerDisplacement * scale);
                points.add((float) center.getX());
                points.add((float) center.getY());
                points.add((float) center.getZ());
                int centerIdx = vertexIndex++;

                List<Vector3D> vertices = poly.vertices();
                int[] edgeIndices = new int[vertices.size()];

                for (int i = 0; i < vertices.size(); i++) {
                    // Edge vertices also displaced for proper terrain relief
                    Vector3D v = vertices.get(i).normalize().scalarMultiply(edgeDisplacement * scale);
                    points.add((float) v.getX());
                    points.add((float) v.getY());
                    points.add((float) v.getZ());
                    edgeIndices[i] = vertexIndex++;
                }

                for (int i = 0; i < vertices.size(); i++) {
                    int nextI = (i + 1) % vertices.size();
                    faces.add(centerIdx);
                    faces.add(0);
                    faces.add(edgeIndices[i]);
                    faces.add(0);
                    faces.add(edgeIndices[nextI]);
                    faces.add(0);
                }
            }

            float[] pointsArray = new float[points.size()];
            for (int i = 0; i < points.size(); i++) {
                pointsArray[i] = points.get(i);
            }

            int[] facesArray = new int[faces.size()];
            for (int i = 0; i < faces.size(); i++) {
                facesArray[i] = faces.get(i);
            }

            mesh.getPoints().addAll(pointsArray);
            mesh.getFaces().addAll(facesArray);

            result.put(height, mesh);
        }

        return result;
    }

    /**
     * Creates per-height colored meshes with properly averaged vertex heights.
     * Convenience method that delegates to full version without preciseHeights.
     */
    public static Map<Integer, TriangleMesh> convertByHeightWithAveraging(
            List<Polygon> polygons, int[] heights, AdjacencyGraph adjacency, double scale) {
        return convertByHeightWithAveraging(polygons, heights, adjacency, scale, null);
    }

    /**
     * Creates per-height colored meshes with properly averaged vertex heights.
     * Builds a global vertex index and averages heights from ALL polygons sharing each vertex,
     * eliminating gaps at shared edges for smooth terrain transitions.
     * Also averages center heights with neighbors for smoother overall terrain.
     *
     * @param polygons       The icosahedral mesh polygons
     * @param heights        Integer height values per polygon (for color grouping)
     * @param adjacency      The polygon adjacency graph (used for center averaging)
     * @param scale          Scale factor for rendering
     * @param preciseHeights Optional precise (double) heights for finer averaging (null to use int heights)
     * @return Map of height value to TriangleMesh (for separate coloring)
     */
    public static Map<Integer, TriangleMesh> convertByHeightWithAveraging(
            List<Polygon> polygons, int[] heights, AdjacencyGraph adjacency, double scale,
            double[] preciseHeights) {

        // Build vertex indexing data ONCE for all polygons
        VertexData vertexData = buildVertexData(polygons);

        // Use precise heights for averaging if available, otherwise fall back to int heights
        double[] averagedEdgeHeights;
        double[] averagedCenterHeights;

        if (preciseHeights != null && preciseHeights.length == polygons.size()) {
            // Use precise heights for smoother averaging
            averagedEdgeHeights = computeAveragedHeightsPrecise(vertexData, preciseHeights);
            averagedCenterHeights = adjacency != null
                ? computeAveragedCenterHeightsPrecise(polygons, preciseHeights, adjacency)
                : null;
        } else {
            // Fall back to integer heights
            averagedEdgeHeights = computeAveragedHeights(vertexData, heights);
            averagedCenterHeights = adjacency != null
                ? computeAveragedCenterHeights(polygons, heights, adjacency)
                : null;
        }

        Map<Integer, List<Integer>> heightToPolygons = new HashMap<>();

        // Group polygons by height
        for (int i = 0; i < polygons.size(); i++) {
            int height = heights[i];
            heightToPolygons.computeIfAbsent(height, k -> new ArrayList<>()).add(i);
        }

        Map<Integer, TriangleMesh> result = new HashMap<>();

        // Create separate mesh for each height level
        for (Map.Entry<Integer, List<Integer>> entry : heightToPolygons.entrySet()) {
            int height = entry.getKey();
            List<Integer> polyIndices = entry.getValue();

            TriangleMesh mesh = new TriangleMesh();
            // Enable smooth shading with per-vertex normals
            mesh.setVertexFormat(VertexFormat.POINT_NORMAL_TEXCOORD);

            List<Float> points = new ArrayList<>();
            List<Float> normals = new ArrayList<>();
            List<Integer> faces = new ArrayList<>();

            mesh.getTexCoords().addAll(0.5f, 0.5f);

            int vertexIndex = 0;

            for (int polyIdx : polyIndices) {
                Polygon poly = polygons.get(polyIdx);
                List<Vector3D> vertices = poly.vertices();
                int[] polyVertIndices = vertexData.polygonVertexIndices[polyIdx];

                // Center height: average from its own edge vertices for uniform displacement
                // This eliminates center-puffing artifacts by matching center to edge heights
                double centerSum = 0.0;
                for (int local = 0; local < polyVertIndices.length; local++) {
                    int globalV = polyVertIndices[local];
                    centerSum += averagedEdgeHeights[globalV];
                }
                double centerHeight = centerSum / polyVertIndices.length;
                double centerDisplacement = 1.0 + centerHeight * HEIGHT_SCALE;

                Vector3D center = poly.center().normalize().scalarMultiply(centerDisplacement * scale);
                points.add((float) center.getX());
                points.add((float) center.getY());
                points.add((float) center.getZ());

                // Normal = radial direction (normalized position) for smooth spherical shading
                Vector3D centerNormal = center.normalize();
                normals.add((float) centerNormal.getX());
                normals.add((float) centerNormal.getY());
                normals.add((float) centerNormal.getZ());

                int centerIdx = vertexIndex++;

                int[] edgeIndices = new int[vertices.size()];

                for (int i = 0; i < vertices.size(); i++) {
                    Vector3D vertex = vertices.get(i);

                    // Use pre-computed averaged height for this vertex
                    int globalVertIdx = polyVertIndices[i];
                    double avgHeight = averagedEdgeHeights[globalVertIdx];

                    double edgeDisplacement = 1.0 + avgHeight * EDGE_HEIGHT_SCALE;
                    Vector3D v = vertex.normalize().scalarMultiply(edgeDisplacement * scale);

                    points.add((float) v.getX());
                    points.add((float) v.getY());
                    points.add((float) v.getZ());

                    // Radial normal for smooth shading
                    Vector3D edgeNormal = v.normalize();
                    normals.add((float) edgeNormal.getX());
                    normals.add((float) edgeNormal.getY());
                    normals.add((float) edgeNormal.getZ());

                    edgeIndices[i] = vertexIndex++;
                }

                // Face format for POINT_NORMAL_TEXCOORD: p0, n0, t0, p1, n1, t1, p2, n2, t2
                for (int i = 0; i < vertices.size(); i++) {
                    int nextI = (i + 1) % vertices.size();
                    // Vertex 0: center
                    faces.add(centerIdx);        // point index
                    faces.add(centerIdx);        // normal index (same as point for radial normals)
                    faces.add(0);                // tex coord index
                    // Vertex 1: edge[i]
                    faces.add(edgeIndices[i]);
                    faces.add(edgeIndices[i]);
                    faces.add(0);
                    // Vertex 2: edge[nextI]
                    faces.add(edgeIndices[nextI]);
                    faces.add(edgeIndices[nextI]);
                    faces.add(0);
                }
            }

            mesh.getPoints().addAll(toFloatArray(points));
            mesh.getNormals().addAll(toFloatArray(normals));
            mesh.getFaces().addAll(toIntArray(faces));

            result.put(height, mesh);
        }

        return result;
    }

    /**
     * Returns color for a given integer height value.
     * Maps height from [-4, 4] to color gradient.
     *
     * @param height Integer height value
     * @return JavaFX Color for that height
     */
    public static Color getColorForHeight(int height) {
        int index = height + 4;
        index = Math.max(0, Math.min(HEIGHT_COLORS.length - 1, index));
        return HEIGHT_COLORS[index];
    }

    /**
     * Returns a smoothly interpolated color for a precise height value.
     * Blends between adjacent height colors based on fractional part.
     *
     * @param height Precise height value (typically -4 to 4)
     * @return JavaFX Color interpolated for that height
     */
    public static Color getColorForPreciseHeight(double height) {
        // Map height from [-4, 4] to [0, 8]
        double normalized = height + 4.0;
        normalized = Math.max(0, Math.min(8, normalized));

        int lowerIndex = (int) Math.floor(normalized);
        int upperIndex = lowerIndex + 1;
        double fraction = normalized - lowerIndex;

        lowerIndex = Math.max(0, Math.min(HEIGHT_COLORS.length - 1, lowerIndex));
        upperIndex = Math.max(0, Math.min(HEIGHT_COLORS.length - 1, upperIndex));

        Color lowerColor = HEIGHT_COLORS[lowerIndex];
        Color upperColor = HEIGHT_COLORS[upperIndex];

        return lowerColor.interpolate(upperColor, fraction);
    }

    /**
     * Creates a material for a specific height level.
     *
     * @param height The height level
     * @return PhongMaterial with appropriate color
     */
    public static PhongMaterial createMaterialForHeight(int height) {
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(getColorForHeight(height));
        material.setSpecularColor(Color.WHITE.deriveColor(0, 1, 0.2, 1));
        material.setSpecularPower(12.0);
        return material;
    }

    private static int calculateAverageHeight(int[] heights) {
        if (heights == null || heights.length == 0) {
            return 0;
        }
        long sum = 0;
        for (int h : heights) {
            sum += h;
        }
        return (int) (sum / heights.length);
    }

    // ==================== Rainfall Heatmap Support ====================

    // Rainfall color gradient: brown (dry) → yellow → green → cyan → blue (wet)
    private static final Color[] RAINFALL_COLORS = {
        Color.rgb(139, 90, 43),   // Dry: brown/tan
        Color.rgb(189, 183, 107), // Low: khaki
        Color.rgb(154, 205, 50),  // Medium-low: yellow-green
        Color.rgb(60, 179, 113),  // Medium: sea green
        Color.rgb(32, 178, 170),  // Medium-high: light sea green
        Color.rgb(0, 139, 139),   // High: dark cyan
        Color.rgb(0, 100, 180),   // Very high: blue
        Color.rgb(0, 0, 139)      // Extreme: dark blue
    };

    /**
     * Creates a map of rainfall levels to TriangleMesh for heatmap visualization.
     * Groups polygons by rainfall intensity and colors accordingly.
     *
     * @param polygons The icosahedral mesh polygons
     * @param heights  Height values per polygon (for displacement)
     * @param rainfall Rainfall values per polygon
     * @param scale    Scale factor for rendering
     * @return Map of rainfall bucket (0-7) to TriangleMesh
     */
    public static Map<Integer, TriangleMesh> convertByRainfall(
            List<Polygon> polygons, int[] heights, double[] rainfall, double scale) {

        if (rainfall == null || rainfall.length == 0) {
            // No rainfall data, return empty map
            return new HashMap<>();
        }

        // Find rainfall range for normalization
        double minRain = Double.MAX_VALUE;
        double maxRain = Double.MIN_VALUE;
        for (double r : rainfall) {
            if (r < minRain) minRain = r;
            if (r > maxRain) maxRain = r;
        }
        double rainRange = maxRain - minRain;
        if (rainRange < 0.001) rainRange = 1.0; // Avoid division by zero

        // Group polygons by rainfall bucket (0-7)
        Map<Integer, List<Integer>> bucketToPolygons = new HashMap<>();
        for (int i = 0; i < polygons.size(); i++) {
            double normalizedRain = (rainfall[i] - minRain) / rainRange;
            int bucket = (int) Math.min(7, Math.floor(normalizedRain * 8));
            bucketToPolygons.computeIfAbsent(bucket, k -> new ArrayList<>()).add(i);
        }

        Map<Integer, TriangleMesh> result = new HashMap<>();

        // Create mesh for each rainfall bucket
        for (Map.Entry<Integer, List<Integer>> entry : bucketToPolygons.entrySet()) {
            int bucket = entry.getKey();
            List<Integer> polyIndices = entry.getValue();

            TriangleMesh mesh = new TriangleMesh();
            List<Float> points = new ArrayList<>();
            List<Integer> faces = new ArrayList<>();

            mesh.getTexCoords().addAll(0.5f, 0.5f);

            int vertexIndex = 0;

            for (int polyIdx : polyIndices) {
                Polygon poly = polygons.get(polyIdx);
                int height = heights[polyIdx];

                double centerDisplacement = 1.0 + height * HEIGHT_SCALE;
                double edgeDisplacement = 1.0 + height * EDGE_HEIGHT_SCALE;

                Vector3D center = poly.center().normalize().scalarMultiply(centerDisplacement * scale);
                points.add((float) center.getX());
                points.add((float) center.getY());
                points.add((float) center.getZ());
                int centerIdx = vertexIndex++;

                List<Vector3D> vertices = poly.vertices();
                int[] edgeIndices = new int[vertices.size()];

                for (int i = 0; i < vertices.size(); i++) {
                    Vector3D v = vertices.get(i).normalize().scalarMultiply(edgeDisplacement * scale);
                    points.add((float) v.getX());
                    points.add((float) v.getY());
                    points.add((float) v.getZ());
                    edgeIndices[i] = vertexIndex++;
                }

                for (int i = 0; i < vertices.size(); i++) {
                    int nextI = (i + 1) % vertices.size();
                    faces.add(centerIdx);
                    faces.add(0);
                    faces.add(edgeIndices[i]);
                    faces.add(0);
                    faces.add(edgeIndices[nextI]);
                    faces.add(0);
                }
            }

            float[] pointsArray = new float[points.size()];
            for (int i = 0; i < points.size(); i++) {
                pointsArray[i] = points.get(i);
            }

            int[] facesArray = new int[faces.size()];
            for (int i = 0; i < faces.size(); i++) {
                facesArray[i] = faces.get(i);
            }

            mesh.getPoints().addAll(pointsArray);
            mesh.getFaces().addAll(facesArray);

            result.put(bucket, mesh);
        }

        return result;
    }

    /**
     * Creates a material for a rainfall bucket (heatmap coloring).
     *
     * @param bucket Rainfall bucket (0-7, 0=dry, 7=wet)
     * @return PhongMaterial with appropriate rainfall color
     */
    public static PhongMaterial createMaterialForRainfall(int bucket) {
        PhongMaterial material = new PhongMaterial();
        int index = Math.max(0, Math.min(RAINFALL_COLORS.length - 1, bucket));
        material.setDiffuseColor(RAINFALL_COLORS[index]);
        material.setSpecularColor(Color.WHITE.deriveColor(0, 1, 0.15, 1));
        material.setSpecularPower(8.0);
        return material;
    }

    /**
     * Returns the color for a rainfall bucket.
     *
     * @param bucket Rainfall bucket (0-7)
     * @return JavaFX Color for the bucket
     */
    public static Color getColorForRainfall(int bucket) {
        int index = Math.max(0, Math.min(RAINFALL_COLORS.length - 1, bucket));
        return RAINFALL_COLORS[index];
    }

    /**
     * Interpolates a color for a normalized rainfall value.
     *
     * @param normalizedRainfall Rainfall value normalized to 0.0-1.0 range
     * @return Interpolated color from the rainfall gradient
     */
    public static Color getColorForNormalizedRainfall(double normalizedRainfall) {
        double scaled = normalizedRainfall * (RAINFALL_COLORS.length - 1);
        int lowerIdx = (int) Math.floor(scaled);
        int upperIdx = lowerIdx + 1;
        double fraction = scaled - lowerIdx;

        lowerIdx = Math.max(0, Math.min(RAINFALL_COLORS.length - 1, lowerIdx));
        upperIdx = Math.max(0, Math.min(RAINFALL_COLORS.length - 1, upperIdx));

        return RAINFALL_COLORS[lowerIdx].interpolate(RAINFALL_COLORS[upperIdx], fraction);
    }
}
