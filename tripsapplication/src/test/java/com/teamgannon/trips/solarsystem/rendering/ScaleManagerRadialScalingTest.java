package com.teamgannon.trips.solarsystem.rendering;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression guard for the "non-linear scaling must be radial, not per-axis"
 * rule documented in CLAUDE.md and Issue 30.
 * <p>
 * If anyone reintroduces the broken pattern of calling {@code auToScreen(x)},
 * {@code auToScreen(y)}, {@code auToScreen(z)} independently when log scaling
 * is enabled, geometry distorts: a circle becomes an ellipse, orbit paths
 * stop aligning with calculated positions, etc.
 * <p>
 * The fix is {@link ScaleManager#auVectorToScreen(double, double, double)},
 * which scales the <i>radial distance</i> and applies the same factor to
 * every axis. These tests pin that contract.
 */
@DisplayName("ScaleManager radial scaling regression")
class ScaleManagerRadialScalingTest {

    @Test
    @DisplayName("under log scaling, two points at the same radial distance scale to the same screen distance")
    void logScaleIsRadiallyIsometric() {
        ScaleManager sm = new ScaleManager();
        sm.setUseLogScale(true);

        // Three points on a sphere of radius 5 AU, in different orientations.
        double[] p1 = sm.auVectorToScreen(5.0, 0.0, 0.0);
        double[] p2 = sm.auVectorToScreen(0.0, 5.0, 0.0);
        double[] p3 = sm.auVectorToScreen(3.0, 4.0, 0.0);  // also r = 5

        double r1 = magnitude(p1);
        double r2 = magnitude(p2);
        double r3 = magnitude(p3);

        // All three must map to the same screen-space radial distance.
        assertEquals(r1, r2, 1e-9, "axis-aligned points at the same radius must scale identically");
        assertEquals(r1, r3, 1e-9, "off-axis points at the same radius must scale identically");
    }

    @Test
    @DisplayName("radial-scaling preserves direction (unit vector unchanged)")
    void radialScalingPreservesDirection() {
        ScaleManager sm = new ScaleManager();
        sm.setUseLogScale(true);

        double[] inputDir = normalize(2.0, 3.0, 4.0);
        double[] screen = sm.auVectorToScreen(2.0, 3.0, 4.0);
        double[] outputDir = normalize(screen[0], screen[1], screen[2]);

        assertEquals(inputDir[0], outputDir[0], 1e-9, "x direction component must be preserved");
        assertEquals(inputDir[1], outputDir[1], 1e-9, "y direction component must be preserved");
        assertEquals(inputDir[2], outputDir[2], 1e-9, "z direction component must be preserved");
    }

    @Test
    @DisplayName("linear scaling is per-axis equivalent to radial scaling (same factor everywhere)")
    void linearScalingMatchesRadial() {
        ScaleManager sm = new ScaleManager();
        sm.setUseLogScale(false);

        double[] p = sm.auVectorToScreen(2.0, 3.0, 4.0);

        // Under linear scaling, the radial helper should equal per-axis: every
        // coordinate scaled by the same baseScale*zoom factor. Verify ratio.
        assertEquals(p[0] / 2.0, p[1] / 3.0, 1e-9);
        assertEquals(p[1] / 3.0, p[2] / 4.0, 1e-9);
    }

    @Test
    @DisplayName("auVectorToScreen at the origin returns the origin")
    void originMapsToOrigin() {
        ScaleManager sm = new ScaleManager();
        sm.setUseLogScale(true);

        double[] p = sm.auVectorToScreen(0.0, 0.0, 0.0);
        assertEquals(0.0, p[0]);
        assertEquals(0.0, p[1]);
        assertEquals(0.0, p[2]);
    }

    @Test
    @DisplayName("naive per-axis log scaling distorts shape (sanity: confirms the bug we're guarding against)")
    void perAxisLogScalingIsNotIsometric() {
        // If a future refactor "simplifies" by doing auToScreen(x), auToScreen(y),
        // auToScreen(z) independently, here's what you'd see at points on the
        // same radial sphere — different screen-space radii. This test pins
        // that observation so the rationale isn't lost.
        ScaleManager sm = new ScaleManager();
        sm.setUseLogScale(true);

        double r1 = magnitude(sm.auToScreen(5.0), sm.auToScreen(0.0), sm.auToScreen(0.0));
        double r2 = magnitude(sm.auToScreen(3.0), sm.auToScreen(4.0), sm.auToScreen(0.0));
        // (3, 4, 0) is on the same r=5 sphere as (5, 0, 0), but naive per-axis
        // scaling distorts: r2 != r1 here, which is precisely the bug.
        assertTrue(Math.abs(r1 - r2) > 1.0,
                "naive per-axis log scaling DOES distort shape — confirms the broken pattern; "
                        + "callers must use auVectorToScreen for 3D coordinates");
    }

    private static double magnitude(double x, double y, double z) {
        return Math.sqrt(x * x + y * y + z * z);
    }

    private static double magnitude(double[] v) {
        return magnitude(v[0], v[1], v[2]);
    }

    private static double[] normalize(double x, double y, double z) {
        double m = magnitude(x, y, z);
        return new double[]{x / m, y / m, z / m};
    }
}
