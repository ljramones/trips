package com.teamgannon.trips.jpa.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for StarObject coordinate getters and the fix for side-effect-free getters.
 */
@DisplayName("StarObject Coordinate Methods")
class StarObjectCoordinateTest {

    @Nested
    @DisplayName("Coordinate Getters Without Side Effects")
    class GetterNoSideEffectsTests {

        @Test
        @DisplayName("getX() should return raw value without computing coordinates")
        void getXShouldNotComputeCoordinates() {
            StarObject star = new StarObject();
            star.setX(0.0);
            star.setY(0.0);
            star.setZ(0.0);
            star.setRa(100.0);
            star.setDeclination(45.0);
            star.setDistance(10.0);

            // Before fix, this would have computed coordinates
            double x = star.getX();

            // X should still be 0 - getter should not have side effects
            assertEquals(0.0, x);
            assertEquals(0.0, star.getY());
            assertEquals(0.0, star.getZ());
        }

        @Test
        @DisplayName("getY() should return raw value without computing coordinates")
        void getYShouldNotComputeCoordinates() {
            StarObject star = new StarObject();
            star.setX(0.0);
            star.setY(0.0);
            star.setZ(0.0);
            star.setRa(100.0);
            star.setDeclination(45.0);
            star.setDistance(10.0);

            double y = star.getY();

            assertEquals(0.0, y);
        }

        @Test
        @DisplayName("getZ() should return raw value without computing coordinates")
        void getZShouldNotComputeCoordinates() {
            StarObject star = new StarObject();
            star.setX(0.0);
            star.setY(0.0);
            star.setZ(0.0);
            star.setRa(100.0);
            star.setDeclination(45.0);
            star.setDistance(10.0);

            double z = star.getZ();

            assertEquals(0.0, z);
        }

        @Test
        @DisplayName("getCoordinates() should return raw values without computing")
        void getCoordinatesShouldNotCompute() {
            StarObject star = new StarObject();
            star.setX(0.0);
            star.setY(0.0);
            star.setZ(0.0);
            star.setRa(100.0);
            star.setDeclination(45.0);
            star.setDistance(10.0);

            double[] coords = star.getCoordinates();

            assertEquals(0.0, coords[0]);
            assertEquals(0.0, coords[1]);
            assertEquals(0.0, coords[2]);
        }

        @Test
        @DisplayName("getters should return set values correctly")
        void gettersShouldReturnSetValues() {
            StarObject star = new StarObject();
            star.setX(1.5);
            star.setY(2.5);
            star.setZ(3.5);

            assertEquals(1.5, star.getX());
            assertEquals(2.5, star.getY());
            assertEquals(3.5, star.getZ());

            double[] coords = star.getCoordinates();
            assertEquals(1.5, coords[0]);
            assertEquals(2.5, coords[1]);
            assertEquals(3.5, coords[2]);
        }
    }

    @Nested
    @DisplayName("computeCoordinatesIfNeeded()")
    class ComputeCoordinatesTests {

        @Test
        @DisplayName("should compute coordinates when all are zero and distance > 0")
        void shouldComputeWhenZeroAndHasDistance() {
            StarObject star = new StarObject();
            star.setX(0.0);
            star.setY(0.0);
            star.setZ(0.0);
            star.setRa(0.0);  // RA = 0
            star.setDeclination(0.0);  // Dec = 0
            star.setDistance(10.0);

            star.computeCoordinatesIfNeeded();

            // With RA=0, Dec=0, Distance=10, coordinates should be computed
            // The exact values depend on the coordinate calculation implementation
            // But they should no longer all be zero
            double[] coords = star.getCoordinates();

            // At RA=0, Dec=0, the star should be on the positive X axis
            // (approximately, depending on the coordinate system used)
            assertTrue(coords[0] != 0.0 || coords[1] != 0.0 || coords[2] != 0.0,
                    "Coordinates should be computed when distance > 0");
        }

        @Test
        @DisplayName("should not compute coordinates when distance is zero")
        void shouldNotComputeWhenDistanceIsZero() {
            StarObject star = new StarObject();
            star.setX(0.0);
            star.setY(0.0);
            star.setZ(0.0);
            star.setRa(100.0);
            star.setDeclination(45.0);
            star.setDistance(0.0);

            star.computeCoordinatesIfNeeded();

            // Should remain zero since distance is 0
            assertEquals(0.0, star.getX());
            assertEquals(0.0, star.getY());
            assertEquals(0.0, star.getZ());
        }

        @Test
        @DisplayName("should not overwrite existing coordinates")
        void shouldNotOverwriteExistingCoordinates() {
            StarObject star = new StarObject();
            star.setX(5.0);
            star.setY(6.0);
            star.setZ(7.0);
            star.setRa(100.0);
            star.setDeclination(45.0);
            star.setDistance(10.0);

            star.computeCoordinatesIfNeeded();

            // Should preserve existing values since not all zero
            assertEquals(5.0, star.getX());
            assertEquals(6.0, star.getY());
            assertEquals(7.0, star.getZ());
        }

        @Test
        @DisplayName("should compute when only one coordinate is zero")
        void shouldNotComputeWhenOnlyOneIsZero() {
            StarObject star = new StarObject();
            star.setX(5.0);
            star.setY(0.0);
            star.setZ(7.0);
            star.setRa(100.0);
            star.setDeclination(45.0);
            star.setDistance(10.0);

            star.computeCoordinatesIfNeeded();

            // Should preserve existing values (not all three are zero)
            assertEquals(5.0, star.getX());
            assertEquals(0.0, star.getY());
            assertEquals(7.0, star.getZ());
        }
    }

    @Nested
    @DisplayName("setCoordinates()")
    class SetCoordinatesTests {

        @Test
        @DisplayName("should set all coordinates from array")
        void shouldSetAllCoordinates() {
            StarObject star = new StarObject();

            star.setCoordinates(new double[]{1.1, 2.2, 3.3});

            assertEquals(1.1, star.getX());
            assertEquals(2.2, star.getY());
            assertEquals(3.3, star.getZ());
        }

        @Test
        @DisplayName("should overwrite existing coordinates")
        void shouldOverwriteExisting() {
            StarObject star = new StarObject();
            star.setX(10.0);
            star.setY(20.0);
            star.setZ(30.0);

            star.setCoordinates(new double[]{1.0, 2.0, 3.0});

            assertEquals(1.0, star.getX());
            assertEquals(2.0, star.getY());
            assertEquals(3.0, star.getZ());
        }
    }

    @Nested
    @DisplayName("Coordinate Array Independence")
    class ArrayIndependenceTests {

        @Test
        @DisplayName("getCoordinates() should return new array each time")
        void getCoordinatesShouldReturnNewArray() {
            StarObject star = new StarObject();
            star.setX(1.0);
            star.setY(2.0);
            star.setZ(3.0);

            double[] coords1 = star.getCoordinates();
            double[] coords2 = star.getCoordinates();

            assertNotSame(coords1, coords2);
            assertArrayEquals(coords1, coords2);
        }

        @Test
        @DisplayName("modifying returned array should not affect star")
        void modifyingReturnedArrayShouldNotAffectStar() {
            StarObject star = new StarObject();
            star.setX(1.0);
            star.setY(2.0);
            star.setZ(3.0);

            double[] coords = star.getCoordinates();
            coords[0] = 999.0;
            coords[1] = 999.0;
            coords[2] = 999.0;

            // Star's coordinates should be unchanged
            assertEquals(1.0, star.getX());
            assertEquals(2.0, star.getY());
            assertEquals(3.0, star.getZ());
        }
    }
}
