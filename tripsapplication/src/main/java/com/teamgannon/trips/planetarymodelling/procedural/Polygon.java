package com.teamgannon.trips.planetarymodelling.procedural;

import org.hipparchus.geometry.euclidean.threed.Vector3D;
import java.util.List;

/**
 * A polygon on the Goldberg polyhedron (hexagon or pentagon).
 */
public record Polygon(
    Vector3D center,
    List<Vector3D> vertices
) {
    public int vertexCount() {
        return vertices.size();
    }

    public boolean isPentagon() {
        return vertices.size() == 5;
    }

    public boolean isHexagon() {
        return vertices.size() == 6;
    }
}
