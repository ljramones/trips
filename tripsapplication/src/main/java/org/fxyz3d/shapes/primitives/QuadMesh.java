/**
 * QuadMesh.java
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
 * A simple quad (two triangles forming a square) mesh, useful for billboard-style markers.
 * <p>
 * The quad lies in the XY plane by default, facing the positive Z direction.
 * It can be positioned at any Point3D location.
 * <p>
 * This is the most lightweight marker type, with only 4 vertices and 2 triangles.
 * Ideal for particle systems where you need many simple, flat markers.
 *
 * @author TRIPS Project
 */
public class QuadMesh extends TexturedMesh {

    private static final double DEFAULT_SIZE = 1.0;
    private static final Point3D DEFAULT_CENTER = new Point3D(0f, 0f, 0f);

    public QuadMesh() {
        this(DEFAULT_SIZE, null);
    }

    public QuadMesh(double size) {
        this(size, null);
    }

    public QuadMesh(double size, Point3D center) {
        setSize(size);
        setCenter(center);

        updateMesh();
        setCullFace(CullFace.NONE); // Visible from both sides
        setDrawMode(DrawMode.FILL);
        setDepthTest(DepthTest.ENABLE);
    }

    private final DoubleProperty size = new SimpleDoubleProperty(DEFAULT_SIZE) {
        @Override
        protected void invalidated() {
            if (mesh != null) {
                updateMesh();
            }
        }
    };

    public double getSize() {
        return size.get();
    }

    public final void setSize(double value) {
        size.set(value);
    }

    public DoubleProperty sizeProperty() {
        return size;
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
        mesh = createQuad((float) getSize());
        setMesh(mesh);
    }

    private TriangleMesh createQuad(float size) {
        TriangleMesh mesh = new TriangleMesh();

        float halfSize = size / 2f;
        float cx = center.get() != null ? center.get().x : 0f;
        float cy = center.get() != null ? center.get().y : 0f;
        float cz = center.get() != null ? center.get().z : 0f;

        // 4 vertices forming a quad in XY plane
        mesh.getPoints().addAll(
                cx - halfSize, cy - halfSize, cz,  // 0: bottom-left
                cx + halfSize, cy - halfSize, cz,  // 1: bottom-right
                cx + halfSize, cy + halfSize, cz,  // 2: top-right
                cx - halfSize, cy + halfSize, cz   // 3: top-left
        );

        // Texture coordinates
        mesh.getTexCoords().addAll(
                0f, 1f,  // 0: bottom-left
                1f, 1f,  // 1: bottom-right
                1f, 0f,  // 2: top-right
                0f, 0f   // 3: top-left
        );

        // 2 triangles forming the quad (counter-clockwise winding)
        // Each face entry: vertex1, texCoord1, vertex2, texCoord2, vertex3, texCoord3
        mesh.getFaces().addAll(
                0, 0, 1, 1, 2, 2,  // Triangle 1: bottom-left, bottom-right, top-right
                0, 0, 2, 2, 3, 3   // Triangle 2: bottom-left, top-right, top-left
        );

        // Both faces in same smoothing group
        mesh.getFaceSmoothingGroups().addAll(1, 1);

        return mesh;
    }
}
