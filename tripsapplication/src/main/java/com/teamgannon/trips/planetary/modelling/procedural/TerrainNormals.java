package com.teamgannon.trips.planetary.modelling.procedural;

import org.hipparchus.geometry.euclidean.threed.Vector3D;

import java.util.List;

/**
 * Pure-math helper for computing terrain-aware vertex normals on a
 * procedural planet mesh. Extracted from {@link JavaFxPlanetMeshConverter}
 * in Phase 4.5 of the codebase-review remediation (Issue 18).
 * <p>
 * The icosahedral mesh uses fan triangulation from a polygon's center to
 * its edge vertices; without per-triangle face normals every vertex would
 * fall back to the radial (sphere-outward) normal, which gives smooth
 * spherical shading that flattens out actual terrain relief. This class
 * computes per-vertex normals by accumulating face normals from every
 * triangle that touches the vertex, then renormalising.
 */
public final class TerrainNormals {

    private TerrainNormals() {
    }

    /**
     * Compute terrain-aware normals for a polygon's vertices.
     * <p>
     * For each triangle in the fan (center → edge[i] → edge[i+1]):
     * <ul>
     *   <li>Calculates two edge vectors</li>
     *   <li>Cross-product gives the face normal</li>
     *   <li>Flips it if it ends up pointing inward (dot-product with the
     *       radial direction is negative)</li>
     *   <li>Accumulates that face normal onto each of the triangle's three
     *       vertices</li>
     * </ul>
     * After the loop the accumulated vectors are renormalised. Degenerate
     * (zero-length) cases fall back to the corresponding radial normal.
     *
     * @param center      Polygon centre vertex position
     * @param edgeVerts   Edge vertex positions (size == {@code vertexCount})
     * @param vertexCount Number of edge vertices (5 for pentagon, 6 for hexagon)
     * @return Array of normals: {@code [centerNormal, edge0Normal, edge1Normal, …]}
     */
    public static Vector3D[] computeTerrainNormals(Vector3D center, List<Vector3D> edgeVerts, int vertexCount) {
        Vector3D[] accumulated = new Vector3D[vertexCount + 1];
        for (int i = 0; i < accumulated.length; i++) {
            accumulated[i] = Vector3D.ZERO;
        }

        for (int i = 0; i < vertexCount; i++) {
            int nextI = (i + 1) % vertexCount;

            Vector3D v0 = center;
            Vector3D v1 = edgeVerts.get(i);
            Vector3D v2 = edgeVerts.get(nextI);

            Vector3D edge1 = v1.subtract(v0);
            Vector3D edge2 = v2.subtract(v0);

            Vector3D faceNormal = edge1.crossProduct(edge2);

            Vector3D radialDir = v0.normalize();
            if (faceNormal.dotProduct(radialDir) < 0) {
                faceNormal = faceNormal.negate();
            }

            accumulated[0] = accumulated[0].add(faceNormal);
            accumulated[i + 1] = accumulated[i + 1].add(faceNormal);
            accumulated[nextI + 1] = accumulated[nextI + 1].add(faceNormal);
        }

        Vector3D[] normals = new Vector3D[accumulated.length];
        for (int i = 0; i < accumulated.length; i++) {
            double len = accumulated[i].getNorm();
            if (len > 1e-10) {
                normals[i] = accumulated[i].scalarMultiply(1.0 / len);
            } else {
                normals[i] = (i == 0) ? center.normalize() : edgeVerts.get(i - 1).normalize();
            }
        }

        return normals;
    }
}
