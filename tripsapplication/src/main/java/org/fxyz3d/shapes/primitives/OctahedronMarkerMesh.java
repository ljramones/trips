/**
 * OctahedronMarkerMesh.java
 *
 * Copyright (c) 2013-2019, F(X)yz
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of F(X)yz, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL F(X)yz BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.fxyz3d.shapes.primitives;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.DepthTest;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.TriangleMesh;
import org.fxyz3d.geometry.Point3D;

/**
 * A regular octahedron mesh for use as a low-polygon sphere approximation.
 * <p>
 * A regular octahedron has 6 vertices and 8 triangular faces, making it
 * much more efficient than a sphere mesh while still appearing roughly spherical.
 * <p>
 * This is useful for particle systems where you need many 3D markers but
 * want to minimize polygon count.
 * <p>
 * Vertex count comparison:
 * <ul>
 *   <li>Octahedron: 6 vertices, 8 faces</li>
 *   <li>Tetrahedron: 4 vertices, 4 faces</li>
 *   <li>Low-detail sphere (6 segments): ~38 vertices, ~72 faces</li>
 *   <li>Medium-detail sphere (12 segments): ~146 vertices, ~288 faces</li>
 * </ul>
 *
 * @author TRIPS Project
 */
public class OctahedronMarkerMesh extends TexturedMesh {

    private static final double DEFAULT_RADIUS = 1.0;
    private static final Point3D DEFAULT_CENTER = new Point3D(0f, 0f, 0f);

    public OctahedronMarkerMesh() {
        this(DEFAULT_RADIUS, null);
    }

    public OctahedronMarkerMesh(double radius) {
        this(radius, null);
    }

    public OctahedronMarkerMesh(double radius, Point3D center) {
        setRadius(radius);
        setCenter(center);

        updateMesh();
        setCullFace(CullFace.BACK);
        setDrawMode(DrawMode.FILL);
        setDepthTest(DepthTest.ENABLE);
    }

    private final DoubleProperty radius = new SimpleDoubleProperty(DEFAULT_RADIUS) {
        @Override
        protected void invalidated() {
            if (mesh != null) {
                updateMesh();
            }
        }
    };

    public double getRadius() {
        return radius.get();
    }

    public final void setRadius(double value) {
        radius.set(value);
    }

    public DoubleProperty radiusProperty() {
        return radius;
    }

    private final ObjectProperty<Point3D> center = new SimpleObjectProperty<>(DEFAULT_CENTER) {
        @Override
        protected void invalidated() {
            if (mesh != null) {
                updateMesh();
            }
        }
    };

    public Point3D getCenter() {
        return center.get();
    }

    public final void setCenter(Point3D value) {
        center.set(value);
    }

    public ObjectProperty<Point3D> centerProperty() {
        return center;
    }

    @Override
    protected final void updateMesh() {
        setMesh(null);
        mesh = createOctahedron((float) getRadius());
        setMesh(mesh);
    }

    private TriangleMesh createOctahedron(float radius) {
        TriangleMesh mesh = new TriangleMesh();

        float cx = center.get() != null ? center.get().x : 0f;
        float cy = center.get() != null ? center.get().y : 0f;
        float cz = center.get() != null ? center.get().z : 0f;

        // Regular octahedron: 6 vertices at distance 'radius' from center
        // Vertices on each axis: +X, -X, +Y, -Y, +Z, -Z
        mesh.getPoints().addAll(
                cx + radius, cy, cz,           // 0: +X
                cx - radius, cy, cz,           // 1: -X
                cx, cy + radius, cz,           // 2: +Y
                cx, cy - radius, cz,           // 3: -Y
                cx, cy, cz + radius,           // 4: +Z
                cx, cy, cz - radius            // 5: -Z
        );

        // Simple texture coordinate (all faces same color)
        mesh.getTexCoords().addAll(0.5f, 0.5f);

        // 8 triangular faces (counter-clockwise winding when viewed from outside)
        // Top 4 faces (above XZ plane, toward +Y)
        mesh.getFaces().addAll(
                0, 0, 4, 0, 2, 0,  // +X, +Z, +Y
                4, 0, 1, 0, 2, 0,  // +Z, -X, +Y
                1, 0, 5, 0, 2, 0,  // -X, -Z, +Y
                5, 0, 0, 0, 2, 0   // -Z, +X, +Y
        );

        // Bottom 4 faces (below XZ plane, toward -Y)
        mesh.getFaces().addAll(
                4, 0, 0, 0, 3, 0,  // +Z, +X, -Y
                1, 0, 4, 0, 3, 0,  // -X, +Z, -Y
                5, 0, 1, 0, 3, 0,  // -Z, -X, -Y
                0, 0, 5, 0, 3, 0   // +X, -Z, -Y
        );

        // Smoothing groups (each face in its own group for faceted look)
        mesh.getFaceSmoothingGroups().addAll(1, 2, 4, 8, 16, 32, 64, 128);

        return mesh;
    }
}
