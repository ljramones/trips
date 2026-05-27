package com.teamgannon.trips.controller;

import com.teamgannon.trips.jpa.model.StarObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the pure-function helpers on {@link PlotStarsCoordinator}.
 * <p>
 * The {@code handle(PlotStarsEvent, PlotManager)} method itself mutates the
 * scene graph via PlotManager and can't be cleanly tested without TestFX —
 * but the geometry helpers {@link PlotStarsCoordinator#calculateCenterCoordinates}
 * and {@link PlotStarsCoordinator#calculateDisplayRadius} are pure and were
 * specifically extracted in Phase 7.14 to be unit-testable. Coverage for the
 * full handle() path is part of the broader Phase 7.9 dialog-smoke-test sweep.
 */
@DisplayName("PlotStarsCoordinator: pure geometry helpers")
class PlotStarsCoordinatorTest {

    private static StarObject star(double x, double y, double z) {
        StarObject s = new StarObject();
        s.setX(x);
        s.setY(y);
        s.setZ(z);
        return s;
    }

    @Nested
    @DisplayName("calculateCenterCoordinates")
    class CenterCoordinatesTests {

        @Test
        @DisplayName("empty list returns origin {0,0,0}")
        void emptyListReturnsOrigin() {
            double[] center = PlotStarsCoordinator.calculateCenterCoordinates(List.of());
            assertArrayEquals(new double[]{0, 0, 0}, center, 1e-12);
        }

        @Test
        @DisplayName("single star returns its own coordinates")
        void singleStarReturnsItsPosition() {
            double[] center = PlotStarsCoordinator.calculateCenterCoordinates(
                    List.of(star(3.0, 4.0, 5.0)));
            assertArrayEquals(new double[]{3.0, 4.0, 5.0}, center, 1e-12);
        }

        @Test
        @DisplayName("two stars on opposite sides of origin return the midpoint")
        void symmetricStarsReturnMidpoint() {
            double[] center = PlotStarsCoordinator.calculateCenterCoordinates(List.of(
                    star(5.0, 0.0, 0.0),
                    star(-5.0, 0.0, 0.0)
            ));
            assertArrayEquals(new double[]{0, 0, 0}, center, 1e-12);
        }

        @Test
        @DisplayName("centroid is the unweighted arithmetic mean of coordinates")
        void centroidIsArithmeticMean() {
            double[] center = PlotStarsCoordinator.calculateCenterCoordinates(List.of(
                    star(1.0, 2.0, 3.0),
                    star(4.0, 5.0, 6.0),
                    star(7.0, 8.0, 9.0)
            ));
            // means: (1+4+7)/3 = 4, (2+5+8)/3 = 5, (3+6+9)/3 = 6
            assertArrayEquals(new double[]{4.0, 5.0, 6.0}, center, 1e-12);
        }

        @Test
        @DisplayName("100 random stars centred at known point — centroid converges to it")
        void manyStarsCenteredAtKnownPoint() {
            java.util.Random rng = new java.util.Random(42);
            List<StarObject> stars = new ArrayList<>();
            double cx = 10.0, cy = -20.0, cz = 5.0;
            for (int i = 0; i < 100; i++) {
                stars.add(star(
                        cx + rng.nextGaussian() * 2.0,
                        cy + rng.nextGaussian() * 2.0,
                        cz + rng.nextGaussian() * 2.0));
            }
            double[] center = PlotStarsCoordinator.calculateCenterCoordinates(stars);
            // 100 Gaussian samples around (cx,cy,cz) should give centroid within ~0.3
            assertEquals(cx, center[0], 0.5);
            assertEquals(cy, center[1], 0.5);
            assertEquals(cz, center[2], 0.5);
        }
    }

    @Nested
    @DisplayName("calculateDisplayRadius")
    class DisplayRadiusTests {

        @Test
        @DisplayName("empty list returns default 20.0 ly")
        void emptyListReturnsDefault() {
            double radius = PlotStarsCoordinator.calculateDisplayRadius(List.of(), new double[]{0, 0, 0});
            assertEquals(20.0, radius, 1e-12);
        }

        @Test
        @DisplayName("single star at the center clamps to the 10 ly minimum")
        void singleStarAtCenterIsClampedToMin() {
            double radius = PlotStarsCoordinator.calculateDisplayRadius(
                    List.of(star(0, 0, 0)), new double[]{0, 0, 0});
            assertEquals(10.0, radius, 1e-12);
        }

        @Test
        @DisplayName("padding factor 1.2 is applied to the maximum distance")
        void radiusIncludes20PercentPadding() {
            // Star at 100 ly from center → radius = 100 * 1.2 = 120 ly
            double radius = PlotStarsCoordinator.calculateDisplayRadius(
                    List.of(star(100, 0, 0)), new double[]{0, 0, 0});
            assertEquals(120.0, radius, 1e-12);
        }

        @Test
        @DisplayName("uses the farthest star — not the average")
        void radiusUsesMaxDistance() {
            // 5 stars at the center + 1 far away: radius is set by the far one.
            List<StarObject> stars = List.of(
                    star(0, 0, 0),
                    star(1, 0, 0),
                    star(0, 1, 0),
                    star(0, 0, 1),
                    star(2, 2, 2),
                    star(50, 0, 0)   // farthest
            );
            double radius = PlotStarsCoordinator.calculateDisplayRadius(stars, new double[]{0, 0, 0});
            assertEquals(50 * 1.2, radius, 1e-12);
        }

        @Test
        @DisplayName("radius honours the supplied centre, not the origin")
        void respectsExplicitCenter() {
            // Star at (10,0,0), centre at (5,0,0) → max distance 5, radius max(5*1.2, 10) = 10
            double radius = PlotStarsCoordinator.calculateDisplayRadius(
                    List.of(star(10, 0, 0)), new double[]{5, 0, 0});
            assertEquals(10.0, radius, 1e-12);
        }

        @Test
        @DisplayName("3D distance — diagonal star spans all three axes")
        void handlesDiagonalDistance() {
            // (3, 4, 12) is 13 ly from origin (3-4-12-13 Pythagorean quadruple)
            double radius = PlotStarsCoordinator.calculateDisplayRadius(
                    List.of(star(3, 4, 12)), new double[]{0, 0, 0});
            assertEquals(13 * 1.2, radius, 1e-9);
        }
    }

    @Nested
    @DisplayName("static-helper contract")
    class StaticContract {

        @Test
        @DisplayName("calculateCenterCoordinates is callable without a coordinator instance")
        void centerHelperIsStatic() {
            // Compile-time check: if this calls anything other than the static helper,
            // it won't compile. Runtime check just exercises that path.
            double[] center = PlotStarsCoordinator.calculateCenterCoordinates(
                    List.of(star(1, 1, 1)));
            assertTrue(center.length == 3);
        }
    }
}
