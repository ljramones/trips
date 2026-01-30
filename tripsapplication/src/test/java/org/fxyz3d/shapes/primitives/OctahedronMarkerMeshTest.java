/*
 * F(X)yz
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

import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.TriangleMesh;
import org.fxyz3d.geometry.Point3D;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Tests for OctahedronMarkerMesh - a low-polygon sphere approximation.
 */
public class OctahedronMarkerMeshTest {

    private OctahedronMarkerMesh mesh;

    @BeforeEach
    void setUp() {
        mesh = new OctahedronMarkerMesh();
    }

    // ==================== Construction Tests ====================

    @Nested
    @DisplayName("Construction Tests")
    class ConstructionTests {

        @Test
        @DisplayName("Default constructor creates mesh with radius 1.0")
        void testDefaultConstructor() {
            OctahedronMarkerMesh m = new OctahedronMarkerMesh();

            assertThat(m.getRadius(), is(1.0));
            // Center may be null for default, but centerProperty is always non-null
            assertThat(m.centerProperty(), is(notNullValue()));
            assertThat(m.getMesh(), is(notNullValue()));
        }

        @Test
        @DisplayName("Constructor with radius sets radius")
        void testConstructorWithRadius() {
            OctahedronMarkerMesh m = new OctahedronMarkerMesh(2.5);

            assertThat(m.getRadius(), is(2.5));
        }

        @Test
        @DisplayName("Constructor with radius and center sets both")
        void testConstructorWithRadiusAndCenter() {
            Point3D center = new Point3D(10f, 20f, 30f);
            OctahedronMarkerMesh m = new OctahedronMarkerMesh(3.0, center);

            assertThat(m.getRadius(), is(3.0));
            assertThat(m.getCenter(), is(center));
        }

        @Test
        @DisplayName("Default CullFace is BACK")
        void testDefaultCullFace() {
            assertThat(mesh.getCullFace(), is(CullFace.BACK));
        }

        @Test
        @DisplayName("Default DrawMode is FILL")
        void testDefaultDrawMode() {
            assertThat(mesh.getDrawMode(), is(DrawMode.FILL));
        }
    }

    // ==================== Geometry Tests ====================

    @Nested
    @DisplayName("Geometry Tests")
    class GeometryTests {

        @Test
        @DisplayName("Octahedron has 6 vertices")
        void testVertexCount() {
            TriangleMesh triangleMesh = (TriangleMesh) mesh.getMesh();

            // 6 vertices * 3 coordinates = 18 floats
            assertThat(triangleMesh.getPoints().size(), is(18));
        }

        @Test
        @DisplayName("Octahedron has 8 faces")
        void testFaceCount() {
            TriangleMesh triangleMesh = (TriangleMesh) mesh.getMesh();

            // 8 faces * 6 indices per face (v0,t0,v1,t1,v2,t2) = 48
            assertThat(triangleMesh.getFaces().size(), is(48));
        }

        @Test
        @DisplayName("Octahedron has 8 smoothing groups")
        void testSmoothingGroups() {
            TriangleMesh triangleMesh = (TriangleMesh) mesh.getMesh();

            // 8 faces, each in its own smoothing group
            assertThat(triangleMesh.getFaceSmoothingGroups().size(), is(8));
        }

        @Test
        @DisplayName("Vertices are at correct positions for unit octahedron")
        void testVertexPositions() {
            OctahedronMarkerMesh m = new OctahedronMarkerMesh(1.0, null);
            TriangleMesh triangleMesh = (TriangleMesh) m.getMesh();
            float[] points = new float[18];
            triangleMesh.getPoints().toArray(points);

            // Vertex 0: +X axis (1, 0, 0)
            assertThat(points[0], is(1.0f));
            assertThat(points[1], is(0.0f));
            assertThat(points[2], is(0.0f));

            // Vertex 1: -X axis (-1, 0, 0)
            assertThat(points[3], is(-1.0f));
            assertThat(points[4], is(0.0f));
            assertThat(points[5], is(0.0f));

            // Vertex 2: +Y axis (0, 1, 0)
            assertThat(points[6], is(0.0f));
            assertThat(points[7], is(1.0f));
            assertThat(points[8], is(0.0f));

            // Vertex 3: -Y axis (0, -1, 0)
            assertThat(points[9], is(0.0f));
            assertThat(points[10], is(-1.0f));
            assertThat(points[11], is(0.0f));

            // Vertex 4: +Z axis (0, 0, 1)
            assertThat(points[12], is(0.0f));
            assertThat(points[13], is(0.0f));
            assertThat(points[14], is(1.0f));

            // Vertex 5: -Z axis (0, 0, -1)
            assertThat(points[15], is(0.0f));
            assertThat(points[16], is(0.0f));
            assertThat(points[17], is(-1.0f));
        }

        @Test
        @DisplayName("Radius scales vertex positions")
        void testRadiusScaling() {
            OctahedronMarkerMesh m = new OctahedronMarkerMesh(5.0, null);
            TriangleMesh triangleMesh = (TriangleMesh) m.getMesh();
            float[] points = new float[18];
            triangleMesh.getPoints().toArray(points);

            // Vertex 0: +X axis should be at (5, 0, 0)
            assertThat(points[0], is(5.0f));
        }

        @Test
        @DisplayName("Center offsets vertex positions")
        void testCenterOffset() {
            Point3D center = new Point3D(100f, 200f, 300f);
            OctahedronMarkerMesh m = new OctahedronMarkerMesh(1.0, center);
            TriangleMesh triangleMesh = (TriangleMesh) m.getMesh();
            float[] points = new float[18];
            triangleMesh.getPoints().toArray(points);

            // Vertex 0: +X axis should be at (101, 200, 300)
            assertThat(points[0], is(101.0f));
            assertThat(points[1], is(200.0f));
            assertThat(points[2], is(300.0f));
        }
    }

    // ==================== Property Change Tests ====================

    @Nested
    @DisplayName("Property Change Tests")
    class PropertyChangeTests {

        @Test
        @DisplayName("setRadius updates mesh")
        void testSetRadiusUpdatesMesh() {
            Mesh oldMesh = mesh.getMesh();

            mesh.setRadius(3.0);

            assertThat(mesh.getMesh(), is(not(oldMesh)));
            assertThat(mesh.getRadius(), is(3.0));
        }

        @Test
        @DisplayName("setCenter updates mesh")
        void testSetCenterUpdatesMesh() {
            Mesh oldMesh = mesh.getMesh();

            mesh.setCenter(new Point3D(5f, 5f, 5f));

            assertThat(mesh.getMesh(), is(not(oldMesh)));
        }

        @Test
        @DisplayName("radiusProperty is bindable")
        void testRadiusPropertyBindable() {
            assertThat(mesh.radiusProperty(), is(notNullValue()));
            assertThat(mesh.radiusProperty().get(), is(1.0));
        }

        @Test
        @DisplayName("centerProperty is bindable")
        void testCenterPropertyBindable() {
            assertThat(mesh.centerProperty(), is(notNullValue()));
        }
    }
}
