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
 * Tests for QuadMesh - a simple billboard-style quad marker.
 */
public class QuadMeshTest {

    private QuadMesh mesh;

    @BeforeEach
    void setUp() {
        mesh = new QuadMesh();
    }

    // ==================== Construction Tests ====================

    @Nested
    @DisplayName("Construction Tests")
    class ConstructionTests {

        @Test
        @DisplayName("Default constructor creates mesh with size 1.0")
        void testDefaultConstructor() {
            QuadMesh m = new QuadMesh();

            assertThat(m.getSize(), is(1.0));
            // Center may be null for default, but centerProperty is always non-null
            assertThat(m.centerProperty(), is(notNullValue()));
            assertThat(m.getMesh(), is(notNullValue()));
        }

        @Test
        @DisplayName("Constructor with size sets size")
        void testConstructorWithSize() {
            QuadMesh m = new QuadMesh(2.5);

            assertThat(m.getSize(), is(2.5));
        }

        @Test
        @DisplayName("Constructor with size and center sets both")
        void testConstructorWithSizeAndCenter() {
            Point3D center = new Point3D(10f, 20f, 30f);
            QuadMesh m = new QuadMesh(3.0, center);

            assertThat(m.getSize(), is(3.0));
            assertThat(m.getCenter(), is(center));
        }

        @Test
        @DisplayName("Default CullFace is NONE for billboard visibility")
        void testDefaultCullFace() {
            assertThat(mesh.getCullFace(), is(CullFace.NONE));
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
        @DisplayName("Quad has 4 vertices")
        void testVertexCount() {
            TriangleMesh triangleMesh = (TriangleMesh) mesh.getMesh();

            // 4 vertices * 3 coordinates = 12 floats
            assertThat(triangleMesh.getPoints().size(), is(12));
        }

        @Test
        @DisplayName("Quad has 2 faces (triangles)")
        void testFaceCount() {
            TriangleMesh triangleMesh = (TriangleMesh) mesh.getMesh();

            // 2 faces * 6 indices per face (v0,t0,v1,t1,v2,t2) = 12
            assertThat(triangleMesh.getFaces().size(), is(12));
        }

        @Test
        @DisplayName("Quad has 4 texture coordinates")
        void testTexCoordCount() {
            TriangleMesh triangleMesh = (TriangleMesh) mesh.getMesh();

            // 4 texCoords * 2 floats = 8
            assertThat(triangleMesh.getTexCoords().size(), is(8));
        }

        @Test
        @DisplayName("Quad has 2 smoothing groups")
        void testSmoothingGroups() {
            TriangleMesh triangleMesh = (TriangleMesh) mesh.getMesh();

            // 2 faces, both in same smoothing group
            assertThat(triangleMesh.getFaceSmoothingGroups().size(), is(2));
        }

        @Test
        @DisplayName("Vertices form square in XY plane")
        void testVertexPositions() {
            QuadMesh m = new QuadMesh(2.0, null);  // size 2, so half = 1
            TriangleMesh triangleMesh = (TriangleMesh) m.getMesh();
            float[] points = new float[12];
            triangleMesh.getPoints().toArray(points);

            // Vertex 0: bottom-left (-1, -1, 0)
            assertThat(points[0], is(-1.0f));
            assertThat(points[1], is(-1.0f));
            assertThat(points[2], is(0.0f));

            // Vertex 1: bottom-right (1, -1, 0)
            assertThat(points[3], is(1.0f));
            assertThat(points[4], is(-1.0f));
            assertThat(points[5], is(0.0f));

            // Vertex 2: top-right (1, 1, 0)
            assertThat(points[6], is(1.0f));
            assertThat(points[7], is(1.0f));
            assertThat(points[8], is(0.0f));

            // Vertex 3: top-left (-1, 1, 0)
            assertThat(points[9], is(-1.0f));
            assertThat(points[10], is(1.0f));
            assertThat(points[11], is(0.0f));
        }

        @Test
        @DisplayName("All vertices have Z=0 (flat in XY plane)")
        void testVerticesInXYPlane() {
            TriangleMesh triangleMesh = (TriangleMesh) mesh.getMesh();
            float[] points = new float[12];
            triangleMesh.getPoints().toArray(points);

            // Check Z coordinate of all vertices
            assertThat(points[2], is(0.0f));   // vertex 0 z
            assertThat(points[5], is(0.0f));   // vertex 1 z
            assertThat(points[8], is(0.0f));   // vertex 2 z
            assertThat(points[11], is(0.0f));  // vertex 3 z
        }

        @Test
        @DisplayName("Size scales vertex positions")
        void testSizeScaling() {
            QuadMesh m = new QuadMesh(10.0, null);  // size 10, half = 5
            TriangleMesh triangleMesh = (TriangleMesh) m.getMesh();
            float[] points = new float[12];
            triangleMesh.getPoints().toArray(points);

            // Vertex 0: bottom-left should be at (-5, -5, 0)
            assertThat(points[0], is(-5.0f));
            assertThat(points[1], is(-5.0f));
        }

        @Test
        @DisplayName("Center offsets vertex positions")
        void testCenterOffset() {
            Point3D center = new Point3D(100f, 200f, 300f);
            QuadMesh m = new QuadMesh(2.0, center);  // half = 1
            TriangleMesh triangleMesh = (TriangleMesh) m.getMesh();
            float[] points = new float[12];
            triangleMesh.getPoints().toArray(points);

            // Vertex 0: bottom-left should be at (99, 199, 300)
            assertThat(points[0], is(99.0f));
            assertThat(points[1], is(199.0f));
            assertThat(points[2], is(300.0f));
        }

        @Test
        @DisplayName("Texture coordinates cover full UV range")
        void testTextureCoordinates() {
            TriangleMesh triangleMesh = (TriangleMesh) mesh.getMesh();
            float[] texCoords = new float[8];
            triangleMesh.getTexCoords().toArray(texCoords);

            // Should have corners at (0,0), (1,0), (0,1), (1,1)
            // Bottom-left: (0, 1)
            assertThat(texCoords[0], is(0.0f));
            assertThat(texCoords[1], is(1.0f));

            // Bottom-right: (1, 1)
            assertThat(texCoords[2], is(1.0f));
            assertThat(texCoords[3], is(1.0f));

            // Top-right: (1, 0)
            assertThat(texCoords[4], is(1.0f));
            assertThat(texCoords[5], is(0.0f));

            // Top-left: (0, 0)
            assertThat(texCoords[6], is(0.0f));
            assertThat(texCoords[7], is(0.0f));
        }
    }

    // ==================== Property Change Tests ====================

    @Nested
    @DisplayName("Property Change Tests")
    class PropertyChangeTests {

        @Test
        @DisplayName("setSize updates mesh")
        void testSetSizeUpdatesMesh() {
            Mesh oldMesh = mesh.getMesh();

            mesh.setSize(3.0);

            assertThat(mesh.getMesh(), is(not(oldMesh)));
            assertThat(mesh.getSize(), is(3.0));
        }

        @Test
        @DisplayName("setCenter updates mesh")
        void testSetCenterUpdatesMesh() {
            Mesh oldMesh = mesh.getMesh();

            mesh.setCenter(new Point3D(5f, 5f, 5f));

            assertThat(mesh.getMesh(), is(not(oldMesh)));
        }

        @Test
        @DisplayName("sizeProperty is bindable")
        void testSizePropertyBindable() {
            assertThat(mesh.sizeProperty(), is(notNullValue()));
            assertThat(mesh.sizeProperty().get(), is(1.0));
        }

        @Test
        @DisplayName("centerProperty is bindable")
        void testCenterPropertyBindable() {
            assertThat(mesh.centerProperty(), is(notNullValue()));
        }
    }

    // ==================== Billboard Use Case Tests ====================

    @Nested
    @DisplayName("Billboard Use Case Tests")
    class BillboardTests {

        @Test
        @DisplayName("Quad is visible from both sides due to CullFace.NONE")
        void testVisibleFromBothSides() {
            // CullFace.NONE means both front and back faces are rendered
            assertThat(mesh.getCullFace(), is(CullFace.NONE));
        }

        @Test
        @DisplayName("Quad faces +Z direction (normal is +Z)")
        void testFacesPositiveZ() {
            // With counter-clockwise winding in XY plane, normal points +Z
            // This is verified by the vertex ordering in the faces array
            TriangleMesh triangleMesh = (TriangleMesh) mesh.getMesh();
            int[] faces = new int[12];
            triangleMesh.getFaces().toArray(faces);

            // First triangle: 0, 1, 2 (counter-clockwise when viewed from +Z)
            // v0=0 (bottom-left), v1=1 (bottom-right), v2=2 (top-right)
            assertThat(faces[0], is(0));  // v0
            assertThat(faces[2], is(1));  // v1
            assertThat(faces[4], is(2));  // v2
        }
    }
}
