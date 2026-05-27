package com.teamgannon.trips.planetary.modelling.procedural;

import lombok.extern.slf4j.Slf4j;
import org.hipparchus.geometry.euclidean.threed.Vector3D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Vertex deduplication + height averaging helpers extracted from
 * {@link JavaFxPlanetMeshConverter} in Phase 4.5 of the codebase-review
 * remediation (Issue 18).
 * <p>
 * The icosahedral planet mesh has many polygons sharing the same vertex
 * (pentagon/hexagon corners). To smooth terrain across polygon boundaries
 * we need the same {@code Vector3D} position to map to a single vertex
 * index — {@link #buildVertexData} deduplicates by quantized-position hash
 * (4-decimal grid), and the {@code computeAveragedHeights*} variants then
 * pool heights from every polygon touching each unique vertex.
 * <p>
 * All methods are pure data-crunching; no JavaFX or scene-graph
 * dependencies. Safe to call from any thread.
 */
@Slf4j
public final class MeshVertexAveraging {

    private static final boolean DEBUG_LOGGING = false;

    private MeshVertexAveraging() {
    }

    /**
     * Container for vertex indexing data — unique vertex positions, the
     * (polygon, local-vertex) → global-vertex-index lookup, and the inverse
     * (global-vertex-index → list of polygon indices touching that vertex).
     */
    public record VertexData(
            List<Vector3D> uniqueVertices,
            int[][] polygonVertexIndices,
            List<List<Integer>> vertexToPolygons) {
    }

    /**
     * Build a global vertex list and vertex-to-polygon map. Uses a
     * quantized-position hash (0.0001 unit-sphere resolution) for
     * O(1) lookup — far cheaper than string concatenation in the hot path.
     */
    public static VertexData buildVertexData(List<Polygon> polygons) {
        List<Vector3D> uniqueVertices = new ArrayList<>();
        List<List<Integer>> vertexToPolygons = new ArrayList<>();
        int[][] polygonVertexIndices = new int[polygons.size()][];

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
                    polygonVertexIndices[polyIdx][i] = existingIdx;
                    vertexToPolygons.get(existingIdx).add(polyIdx);
                } else {
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
            int totalShared = 0;
            int maxShared = 0;
            for (List<Integer> polys : vertexToPolygons) {
                if (polys.size() > 1) totalShared++;
                if (polys.size() > maxShared) maxShared = polys.size();
            }
            log.debug("[VertexData] Polygons: {}, Unique vertices: {}, Shared vertices: {}, Max polys/vertex: {}",
                    polygons.size(), uniqueVertices.size(), totalShared, maxShared);
        }

        return new VertexData(uniqueVertices, polygonVertexIndices, vertexToPolygons);
    }

    /**
     * Quantize a vertex position to a 64-bit hash key for spatial lookup.
     * Grid resolution is 0.0001 on the unit sphere (~0.01% of radius).
     * Each coordinate fits in 21 bits for range [-1048576, 1048575]
     * (±104.8 on unit sphere).
     */
    public static long quantizePositionHash(Vector3D v) {
        int qx = (int) Math.round(v.getX() * 10000);
        int qy = (int) Math.round(v.getY() * 10000);
        int qz = (int) Math.round(v.getZ() * 10000);
        return ((long) (qx + 1048576) << 42) | ((long) (qy + 1048576) << 21) | (qz + 1048576);
    }

    /**
     * Compute averaged heights for all unique vertices by pooling integer
     * heights from every polygon that touches each vertex.
     */
    public static double[] computeAveragedHeights(VertexData vertexData, int[] heights) {
        double[] averaged = new double[vertexData.uniqueVertices.size()];
        int smoothedCount = 0;

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

            if (polys.size() > 1 && maxH != minH) {
                smoothedCount++;
            }
        }

        if (DEBUG_LOGGING) {
            log.debug("[AveragedHeights] Total vertices: {}, Smoothed (different neighbors): {}",
                    averaged.length, smoothedCount);
        }

        return averaged;
    }

    /**
     * Compute averaged heights for polygon centers based on their neighbors.
     * Uses the adjacency graph to pool neighbour-polygon heights into each
     * polygon's center height.
     */
    public static double[] computeAveragedCenterHeights(
            List<Polygon> polygons, int[] heights, AdjacencyGraph adjacency) {

        double[] averaged = new double[polygons.size()];

        for (int polyIdx = 0; polyIdx < polygons.size(); polyIdx++) {
            int[] neighbors = adjacency.neighbors(polyIdx);
            double sum = 0;
            for (int neighborIdx : neighbors) {
                sum += heights[neighborIdx];
            }
            averaged[polyIdx] = sum / neighbors.length;
        }

        return averaged;
    }

    /**
     * Compute averaged heights for edge vertices using precise (double)
     * heights. Provides finer gradations than the integer variant.
     */
    public static double[] computeAveragedHeightsPrecise(VertexData vertexData, double[] preciseHeights) {
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

            if (polys.size() > 1 && (maxH - minH) > 0.1) {
                smoothedCount++;
            }
        }

        if (DEBUG_LOGGING) {
            log.debug("[AveragedHeightsPrecise] Total vertices: {}, Smoothed (different neighbors): {}",
                    averaged.length, smoothedCount);
        }

        return averaged;
    }

    /**
     * Compute averaged heights for polygon centers using precise (double)
     * heights.
     */
    public static double[] computeAveragedCenterHeightsPrecise(
            List<Polygon> polygons, double[] preciseHeights, AdjacencyGraph adjacency) {

        double[] averaged = new double[polygons.size()];

        for (int polyIdx = 0; polyIdx < polygons.size(); polyIdx++) {
            int[] neighbors = adjacency.neighbors(polyIdx);
            double sum = 0;
            for (int neighborIdx : neighbors) {
                sum += preciseHeights[neighborIdx];
            }
            averaged[polyIdx] = sum / neighbors.length;
        }

        return averaged;
    }

    /** Mean of an integer height array. Returns {@code 0} for a null or empty array. */
    public static int calculateAverageHeight(int[] heights) {
        if (heights == null || heights.length == 0) {
            return 0;
        }
        long sum = 0;
        for (int h : heights) {
            sum += h;
        }
        return (int) (sum / heights.length);
    }

    /** Convert a {@code List<Float>} to a primitive {@code float[]}. */
    public static float[] toFloatArray(List<Float> list) {
        float[] array = new float[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    /** Convert a {@code List<Integer>} to a primitive {@code int[]}. */
    public static int[] toIntArray(List<Integer> list) {
        int[] array = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }
}
