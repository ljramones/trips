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

package org.fxyz3d.geometry;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Point3D class including new per-particle attribute fields.
 */
public class Point3DTest {

    // ==================== Constructor Tests ====================

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Basic float constructor sets x, y, z")
        void testBasicFloatConstructor() {
            Point3D p = new Point3D(1.0f, 2.0f, 3.0f);

            assertThat(p.x, is(1.0f));
            assertThat(p.y, is(2.0f));
            assertThat(p.z, is(3.0f));
            // Defaults
            assertThat(p.f, is(0.0f));
            assertThat(p.colorIndex, is(0));
            assertThat(p.scale, is(1.0f));
            assertThat(p.opacity, is(1.0f));
        }

        @Test
        @DisplayName("Double constructor converts to float")
        void testDoubleConstructor() {
            Point3D p = new Point3D(1.5, 2.5, 3.5);

            assertThat(p.x, is(1.5f));
            assertThat(p.y, is(2.5f));
            assertThat(p.z, is(3.5f));
        }

        @Test
        @DisplayName("Constructor with function value")
        void testConstructorWithF() {
            Point3D p = new Point3D(1.0f, 2.0f, 3.0f, 5.0f);

            assertThat(p.x, is(1.0f));
            assertThat(p.y, is(2.0f));
            assertThat(p.z, is(3.0f));
            assertThat(p.f, is(5.0f));
        }

        @Test
        @DisplayName("Constructor with colorIndex")
        void testConstructorWithColorIndex() {
            Point3D p = new Point3D(1.0f, 2.0f, 3.0f, 128);

            assertThat(p.x, is(1.0f));
            assertThat(p.y, is(2.0f));
            assertThat(p.z, is(3.0f));
            assertThat(p.colorIndex, is(128));
        }

        @Test
        @DisplayName("Constructor with f and colorIndex")
        void testConstructorWithFAndColorIndex() {
            Point3D p = new Point3D(1.0f, 2.0f, 3.0f, 5.0f, 200);

            assertThat(p.f, is(5.0f));
            assertThat(p.colorIndex, is(200));
        }

        @Test
        @DisplayName("Constructor with colorIndex and scale")
        void testConstructorWithColorIndexAndScale() {
            Point3D p = new Point3D(1.0f, 2.0f, 3.0f, 100, 2.5f);

            assertThat(p.colorIndex, is(100));
            assertThat(p.scale, is(2.5f));
        }

        @Test
        @DisplayName("Constructor with f, colorIndex, and scale")
        void testConstructorWithFColorIndexAndScale() {
            Point3D p = new Point3D(1.0f, 2.0f, 3.0f, 5.0f, 150, 0.5f);

            assertThat(p.f, is(5.0f));
            assertThat(p.colorIndex, is(150));
            assertThat(p.scale, is(0.5f));
        }

        @Test
        @DisplayName("Constructor with colorIndex, scale, and opacity")
        void testConstructorWithColorIndexScaleAndOpacity() {
            Point3D p = new Point3D(1.0f, 2.0f, 3.0f, 100, 2.0f, 0.75f);

            assertThat(p.colorIndex, is(100));
            assertThat(p.scale, is(2.0f));
            assertThat(p.opacity, is(0.75f));
        }

        @Test
        @DisplayName("Full constructor with all attributes")
        void testFullConstructor() {
            Point3D p = new Point3D(1.0f, 2.0f, 3.0f, 5.0f, 200, 1.5f, 0.5f);

            assertThat(p.x, is(1.0f));
            assertThat(p.y, is(2.0f));
            assertThat(p.z, is(3.0f));
            assertThat(p.f, is(5.0f));
            assertThat(p.colorIndex, is(200));
            assertThat(p.scale, is(1.5f));
            assertThat(p.opacity, is(0.5f));
        }
    }

    // ==================== Validation/Clamping Tests ====================

    @Nested
    @DisplayName("Value Clamping Tests")
    class ClampingTests {

        @Test
        @DisplayName("colorIndex clamped to 0-255 range")
        void testColorIndexClamping() {
            Point3D pNegative = new Point3D(0f, 0f, 0f, -10);
            Point3D pOverMax = new Point3D(0f, 0f, 0f, 300);
            Point3D pValid = new Point3D(0f, 0f, 0f, 128);

            assertThat(pNegative.colorIndex, is(0));
            assertThat(pOverMax.colorIndex, is(255));
            assertThat(pValid.colorIndex, is(128));
        }

        @Test
        @DisplayName("scale defaults to 1.0 for invalid values")
        void testScaleValidation() {
            Point3D pZero = new Point3D(0f, 0f, 0f, 0, 0f);
            Point3D pNegative = new Point3D(0f, 0f, 0f, 0, -1.0f);
            Point3D pValid = new Point3D(0f, 0f, 0f, 0, 2.0f);

            assertThat(pZero.scale, is(1.0f));
            assertThat(pNegative.scale, is(1.0f));
            assertThat(pValid.scale, is(2.0f));
        }

        @Test
        @DisplayName("opacity clamped to 0.0-1.0 range")
        void testOpacityClamping() {
            Point3D pNegative = new Point3D(0f, 0f, 0f, 0, 1.0f, -0.5f);
            Point3D pOverMax = new Point3D(0f, 0f, 0f, 0, 1.0f, 1.5f);
            Point3D pValid = new Point3D(0f, 0f, 0f, 0, 1.0f, 0.7f);

            assertThat(pNegative.opacity, is(0.0f));
            assertThat(pOverMax.opacity, is(1.0f));
            assertThat(pValid.opacity, is(0.7f));
        }
    }

    // ==================== Equals/HashCode Tests ====================

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("equals returns true for identical points")
        void testEqualsIdentical() {
            Point3D p1 = new Point3D(1.0f, 2.0f, 3.0f, 5.0f, 100, 1.5f, 0.8f);
            Point3D p2 = new Point3D(1.0f, 2.0f, 3.0f, 5.0f, 100, 1.5f, 0.8f);

            assertThat(p1.equals(p2), is(true));
            assertThat(p1.hashCode(), is(p2.hashCode()));
        }

        @Test
        @DisplayName("equals returns false for different x")
        void testEqualsDifferentX() {
            Point3D p1 = new Point3D(1.0f, 2.0f, 3.0f);
            Point3D p2 = new Point3D(1.1f, 2.0f, 3.0f);

            assertThat(p1.equals(p2), is(false));
        }

        @Test
        @DisplayName("equals returns false for different colorIndex")
        void testEqualsDifferentColorIndex() {
            Point3D p1 = new Point3D(1.0f, 2.0f, 3.0f, 100);
            Point3D p2 = new Point3D(1.0f, 2.0f, 3.0f, 101);

            assertThat(p1.equals(p2), is(false));
        }

        @Test
        @DisplayName("equals returns false for different scale")
        void testEqualsDifferentScale() {
            Point3D p1 = new Point3D(1.0f, 2.0f, 3.0f, 100, 1.0f);
            Point3D p2 = new Point3D(1.0f, 2.0f, 3.0f, 100, 2.0f);

            assertThat(p1.equals(p2), is(false));
        }

        @Test
        @DisplayName("equals returns false for different opacity")
        void testEqualsDifferentOpacity() {
            Point3D p1 = new Point3D(1.0f, 2.0f, 3.0f, 100, 1.0f, 0.5f);
            Point3D p2 = new Point3D(1.0f, 2.0f, 3.0f, 100, 1.0f, 0.6f);

            assertThat(p1.equals(p2), is(false));
        }

        @Test
        @DisplayName("equals returns false for null")
        void testEqualsNull() {
            Point3D p = new Point3D(1.0f, 2.0f, 3.0f);

            assertThat(p.equals(null), is(false));
        }

        @Test
        @DisplayName("equals returns false for different class")
        void testEqualsDifferentClass() {
            Point3D p = new Point3D(1.0f, 2.0f, 3.0f);

            assertThat(p.equals("not a point"), is(false));
        }
    }

    // ==================== toString Tests ====================

    @Nested
    @DisplayName("toString Tests")
    class ToStringTests {

        @Test
        @DisplayName("toString includes all attributes")
        void testToStringIncludesAllAttributes() {
            Point3D p = new Point3D(1.0f, 2.0f, 3.0f, 100, 1.5f, 0.8f);

            String str = p.toString();

            assertThat(str, containsString("x=1.0"));
            assertThat(str, containsString("y=2.0"));
            assertThat(str, containsString("z=3.0"));
            assertThat(str, containsString("colorIndex=100"));
            assertThat(str, containsString("scale=1.5"));
            assertThat(str, containsString("opacity=0.8"));
        }
    }

    // ==================== Vector Operations Tests ====================

    @Nested
    @DisplayName("Vector Operations Tests")
    class VectorOperationsTests {

        @Test
        @DisplayName("add returns new point with summed coordinates")
        void testAdd() {
            Point3D p1 = new Point3D(1.0f, 2.0f, 3.0f);
            Point3D p2 = new Point3D(4.0f, 5.0f, 6.0f);

            Point3D result = p1.add(p2);

            assertThat(result.x, is(5.0f));
            assertThat(result.y, is(7.0f));
            assertThat(result.z, is(9.0f));
        }

        @Test
        @DisplayName("substract returns new point with difference")
        void testSubstract() {
            Point3D p1 = new Point3D(5.0f, 7.0f, 9.0f);
            Point3D p2 = new Point3D(1.0f, 2.0f, 3.0f);

            Point3D result = p1.substract(p2);

            assertThat(result.x, is(4.0f));
            assertThat(result.y, is(5.0f));
            assertThat(result.z, is(6.0f));
        }

        @Test
        @DisplayName("multiply scales all coordinates")
        void testMultiply() {
            Point3D p = new Point3D(2.0f, 3.0f, 4.0f);

            Point3D result = p.multiply(2.0f);

            assertThat(result.x, is(4.0f));
            assertThat(result.y, is(6.0f));
            assertThat(result.z, is(8.0f));
        }

        @Test
        @DisplayName("magnitude calculates correct length")
        void testMagnitude() {
            Point3D p = new Point3D(3.0f, 4.0f, 0.0f);

            assertThat(p.magnitude(), is(5.0f));
        }

        @Test
        @DisplayName("normalize returns unit vector")
        void testNormalize() {
            Point3D p = new Point3D(3.0f, 4.0f, 0.0f);

            Point3D normalized = p.normalize();

            assertThat((double) normalized.x, closeTo(0.6, 0.0001));
            assertThat((double) normalized.y, closeTo(0.8, 0.0001));
            assertThat(normalized.z, is(0.0f));
            assertThat((double) normalized.magnitude(), closeTo(1.0, 0.0001));
        }

        @Test
        @DisplayName("normalize handles zero vector")
        void testNormalizeZeroVector() {
            Point3D p = new Point3D(0.0f, 0.0f, 0.0f);

            Point3D normalized = p.normalize();

            assertThat(normalized.x, is(0.0f));
            assertThat(normalized.y, is(0.0f));
            assertThat(normalized.z, is(0.0f));
        }

        @Test
        @DisplayName("dotProduct calculates correctly")
        void testDotProduct() {
            Point3D p1 = new Point3D(1.0f, 2.0f, 3.0f);
            Point3D p2 = new Point3D(4.0f, 5.0f, 6.0f);

            float result = p1.dotProduct(p2);

            // 1*4 + 2*5 + 3*6 = 4 + 10 + 18 = 32
            assertThat(result, is(32.0f));
        }

        @Test
        @DisplayName("crossProduct calculates correctly")
        void testCrossProduct() {
            Point3D p1 = new Point3D(1.0f, 0.0f, 0.0f);
            Point3D p2 = new Point3D(0.0f, 1.0f, 0.0f);

            Point3D result = p1.crossProduct(p2);

            // i x j = k
            assertThat(result.x, is(0.0f));
            assertThat(result.y, is(0.0f));
            assertThat(result.z, is(1.0f));
        }
    }

    // ==================== Coordinate Conversion Tests ====================

    @Nested
    @DisplayName("Coordinate Conversion Tests")
    class ConversionTests {

        @Test
        @DisplayName("getCoordinates returns stream of coordinates")
        void testGetCoordinates() {
            Point3D p = new Point3D(1.0f, 2.0f, 3.0f);

            double[] coords = p.getCoordinates().toArray();

            assertThat(coords.length, is(3));
            assertThat(coords[0], closeTo(1.0, 0.0001));
            assertThat(coords[1], closeTo(2.0, 0.0001));
            assertThat(coords[2], closeTo(3.0, 0.0001));
        }

        @Test
        @DisplayName("getCoordinates with factor scales coordinates")
        void testGetCoordinatesWithFactor() {
            Point3D p = new Point3D(1.0f, 2.0f, 3.0f);

            double[] coords = p.getCoordinates(2.0f).toArray();

            assertThat(coords[0], closeTo(2.0, 0.0001));
            assertThat(coords[1], closeTo(4.0, 0.0001));
            assertThat(coords[2], closeTo(6.0, 0.0001));
        }

        @Test
        @DisplayName("convertFromJavaFXPoint3D creates Point3D from JavaFX point")
        void testConvertFromJavaFX() {
            javafx.geometry.Point3D fxPoint = new javafx.geometry.Point3D(1.5, 2.5, 3.5);

            Point3D p = Point3D.convertFromJavaFXPoint3D(fxPoint);

            assertThat(p.x, is(1.5f));
            assertThat(p.y, is(2.5f));
            assertThat(p.z, is(3.5f));
        }

        @Test
        @DisplayName("convertToJavaFXPoint3D creates JavaFX point from Point3D")
        void testConvertToJavaFX() {
            Point3D p = new Point3D(1.5f, 2.5f, 3.5f);

            javafx.geometry.Point3D fxPoint = Point3D.convertToJavaFXPoint3D(p);

            assertThat(fxPoint.getX(), closeTo(1.5, 0.0001));
            assertThat(fxPoint.getY(), closeTo(2.5, 0.0001));
            assertThat(fxPoint.getZ(), closeTo(3.5, 0.0001));
        }

        @Test
        @DisplayName("toCSV formats coordinates correctly")
        void testToCSV() {
            Point3D p = new Point3D(1.0f, 2.0f, 3.0f, 5.0f);

            String csv = p.toCSV();

            assertThat(csv, is("1.0;2.0;3.0;5.0"));
        }
    }
}
