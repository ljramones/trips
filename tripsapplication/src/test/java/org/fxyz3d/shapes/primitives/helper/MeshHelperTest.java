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

package org.fxyz3d.shapes.primitives.helper;

import javafx.scene.shape.TriangleMesh;
import org.fxyz3d.geometry.Point3D;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Tests for MeshHelper class including per-particle color and scale support.
 */
public class MeshHelperTest {

    private MeshHelper baseHelper;
    private MeshHelper templateHelper;

    /**
     * Creates a simple triangle mesh for testing.
     */
    private TriangleMesh createSimpleTriangle() {
        TriangleMesh mesh = new TriangleMesh();
        // 3 vertices forming a triangle
        mesh.getPoints().addAll(
                0f, 0f, 0f,     // vertex 0
                1f, 0f, 0f,     // vertex 1
                0.5f, 1f, 0f    // vertex 2
        );
        mesh.getTexCoords().addAll(
                0f, 0f,
                1f, 0f,
                0.5f, 1f
        );
        mesh.getFaces().addAll(
                0, 0, 1, 1, 2, 2  // one triangle
        );
        mesh.getFaceSmoothingGroups().addAll(1);
        return mesh;
    }

    @BeforeEach
    void setUp() {
        baseHelper = new MeshHelper(createSimpleTriangle());
        templateHelper = new MeshHelper(createSimpleTriangle());
    }

    // ==================== Basic MeshHelper Tests ====================

    @Nested
    @DisplayName("Basic MeshHelper Tests")
    class BasicTests {

        @Test
        @DisplayName("Constructor from TriangleMesh extracts data correctly")
        void testConstructorFromTriangleMesh() {
            TriangleMesh mesh = createSimpleTriangle();
            MeshHelper helper = new MeshHelper(mesh);

            assertThat(helper.getPoints().length, is(9));  // 3 vertices * 3 coords
            assertThat(helper.getTexCoords().length, is(6));  // 3 tex coords * 2
            assertThat(helper.getFaces().length, is(6));  // 1 face * 6 indices
            assertThat(helper.getFaceSmoothingGroups().length, is(1));
        }

        @Test
        @DisplayName("addMesh combines two meshes")
        void testAddMesh() {
            int originalPointCount = baseHelper.getPoints().length;
            int originalFaceCount = baseHelper.getFaces().length;

            baseHelper.addMesh(templateHelper);

            assertThat(baseHelper.getPoints().length, is(originalPointCount * 2));
            assertThat(baseHelper.getFaces().length, is(originalFaceCount * 2));
        }
    }

    // ==================== Per-Particle Scale Tests ====================

    @Nested
    @DisplayName("Per-Particle Scale Tests")
    class ScaleTests {

        @Test
        @DisplayName("addMeshWithScale applies scale to each particle")
        void testAddMeshWithScaleAppliesScale() {
            List<Point3D> positions = Arrays.asList(
                    new Point3D(10f, 0f, 0f, 0, 2.0f),  // double size
                    new Point3D(20f, 0f, 0f, 0, 0.5f)   // half size
            );

            baseHelper.addMeshWithScale(templateHelper, positions);

            // Original has 3 vertices, we added 2 particles = 3 + 6 = 9 vertices = 27 floats
            assertThat(baseHelper.getPoints().length, is(27));
        }

        @Test
        @DisplayName("addMeshWithScale positions particles correctly")
        void testAddMeshWithScalePositions() {
            List<Point3D> positions = Arrays.asList(
                    new Point3D(100f, 200f, 300f, 0, 1.0f)  // scale 1.0
            );

            baseHelper.addMeshWithScale(templateHelper, positions);

            float[] points = baseHelper.getPoints();
            // First vertex of second particle should be at (100, 200, 300)
            // (since template's first vertex is at origin and scale is 1.0)
            assertThat(points[9], is(100f));   // x of second particle's first vertex
            assertThat(points[10], is(200f));  // y
            assertThat(points[11], is(300f));  // z
        }

        @Test
        @DisplayName("addMeshWithScale scales vertices around origin before translating")
        void testAddMeshWithScaleScalesAroundOrigin() {
            List<Point3D> positions = Arrays.asList(
                    new Point3D(0f, 0f, 0f, 0, 2.0f)  // double size at origin
            );

            baseHelper.addMeshWithScale(templateHelper, positions);

            float[] points = baseHelper.getPoints();
            // Template vertex 1 is at (1, 0, 0), scaled by 2 = (2, 0, 0)
            assertThat(points[12], is(2f));  // x of second particle's vertex 1
            assertThat(points[13], is(0f));  // y
            assertThat(points[14], is(0f));  // z
        }

        @Test
        @DisplayName("addMeshWithScale uses default scale for invalid values")
        void testAddMeshWithScaleDefaultsInvalidScale() {
            List<Point3D> positions = Arrays.asList(
                    new Point3D(0f, 0f, 0f, 0, 0f),    // invalid scale 0
                    new Point3D(10f, 0f, 0f, 0, -1f)   // invalid scale negative
            );

            // Should not throw, uses default scale of 1.0
            baseHelper.addMeshWithScale(templateHelper, positions);

            // Both particles should use scale 1.0
            float[] points = baseHelper.getPoints();
            // Second particle (index 1 in positions), vertex 1 at x=1, with scale 1, offset by 10
            // = 1*1 + 10 = 11
            assertThat(points[21], is(11f));
        }
    }

    // ==================== Per-Particle Color Tests ====================

    @Nested
    @DisplayName("Per-Particle Color Tests")
    class ColorTests {

        @Test
        @DisplayName("addMeshWithColorIndex creates one texCoord per particle")
        void testAddMeshWithColorIndexTexCoords() {
            List<Point3D> positions = Arrays.asList(
                    new Point3D(10f, 0f, 0f, 64),   // colorIndex 64
                    new Point3D(20f, 0f, 0f, 192)   // colorIndex 192
            );

            baseHelper.addMeshWithColorIndex(templateHelper, positions);

            // Original has 3 texCoords (6 floats)
            // Added 2 particles, each needs 1 texCoord (2 floats each)
            // Total: 6 + 4 = 10 floats
            assertThat(baseHelper.getTexCoords().length, is(10));
        }

        @Test
        @DisplayName("addMeshWithColorIndex maps colorIndex to U coordinate")
        void testAddMeshWithColorIndexMapsU() {
            List<Point3D> positions = Arrays.asList(
                    new Point3D(0f, 0f, 0f, 128)  // colorIndex 128 -> u = 128/255 ≈ 0.502
            );

            baseHelper.addMeshWithColorIndex(templateHelper, positions);

            float[] texCoords = baseHelper.getTexCoords();
            // New texCoord should be at index 3 (after original 3 texCoords)
            float u = texCoords[6];  // first float of 4th texCoord entry
            assertThat((double) u, closeTo(128.0 / 255.0, 0.001));
        }

        @Test
        @DisplayName("addMeshWithColorIndex sets V to 0.5 by default")
        void testAddMeshWithColorIndexDefaultV() {
            List<Point3D> positions = Arrays.asList(
                    new Point3D(0f, 0f, 0f, 100)
            );

            baseHelper.addMeshWithColorIndex(templateHelper, positions);

            float[] texCoords = baseHelper.getTexCoords();
            float v = texCoords[7];  // second float of 4th texCoord entry
            assertThat(v, is(0.5f));
        }

        @Test
        @DisplayName("addMeshWithColorIndex with opacity uses opacity for V")
        void testAddMeshWithColorIndexWithOpacity() {
            Point3D p = new Point3D(0f, 0f, 0f, 100, 1.0f, 0.75f);  // opacity 0.75
            List<Point3D> positions = Arrays.asList(p);

            baseHelper.addMeshWithColorIndex(templateHelper, positions, true);

            float[] texCoords = baseHelper.getTexCoords();
            float v = texCoords[7];  // second float of 4th texCoord entry
            assertThat(v, is(0.75f));
        }

        @Test
        @DisplayName("addMeshWithColorIndex applies scale to vertices")
        void testAddMeshWithColorIndexAppliesScale() {
            List<Point3D> positions = Arrays.asList(
                    new Point3D(0f, 0f, 0f, 100, 3.0f)  // scale 3.0
            );

            baseHelper.addMeshWithColorIndex(templateHelper, positions);

            float[] points = baseHelper.getPoints();
            // Template vertex 1 is at (1, 0, 0), scaled by 3 = (3, 0, 0)
            assertThat(points[12], is(3f));
        }

        @Test
        @DisplayName("addMeshWithColorIndex updates face texCoord indices")
        void testAddMeshWithColorIndexFaceIndices() {
            List<Point3D> positions = Arrays.asList(
                    new Point3D(0f, 0f, 0f, 50)
            );

            baseHelper.addMeshWithColorIndex(templateHelper, positions);

            int[] faces = baseHelper.getFaces();
            // Second face (indices 6-11) should have texCoord index 3 for all vertices
            // Face format: v0, t0, v1, t1, v2, t2
            assertThat(faces[7], is(3));   // t0
            assertThat(faces[9], is(3));   // t1
            assertThat(faces[11], is(3));  // t2
        }
    }

    // ==================== Edge Cases ====================

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("addMeshWithScale handles empty list")
        void testAddMeshWithScaleEmptyList() {
            int originalPointCount = baseHelper.getPoints().length;

            baseHelper.addMeshWithScale(templateHelper, Arrays.asList());

            assertThat(baseHelper.getPoints().length, is(originalPointCount));
        }

        @Test
        @DisplayName("addMeshWithColorIndex handles empty list")
        void testAddMeshWithColorIndexEmptyList() {
            int originalPointCount = baseHelper.getPoints().length;

            baseHelper.addMeshWithColorIndex(templateHelper, Arrays.asList());

            assertThat(baseHelper.getPoints().length, is(originalPointCount));
        }

        @Test
        @DisplayName("addMeshWithColorIndex handles colorIndex at boundaries")
        void testAddMeshWithColorIndexBoundaries() {
            List<Point3D> positions = Arrays.asList(
                    new Point3D(0f, 0f, 0f, 0),    // min colorIndex
                    new Point3D(10f, 0f, 0f, 255)  // max colorIndex
            );

            baseHelper.addMeshWithColorIndex(templateHelper, positions);

            float[] texCoords = baseHelper.getTexCoords();
            assertThat(texCoords[6], is(0f));       // u for colorIndex 0
            assertThat(texCoords[8], is(1f));       // u for colorIndex 255
        }
    }
}
