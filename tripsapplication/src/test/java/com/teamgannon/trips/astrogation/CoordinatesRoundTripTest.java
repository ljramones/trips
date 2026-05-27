package com.teamgannon.trips.astrogation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Round-trip + orthogonality tests for the galactic-coordinate transforms
 * documented in {@link Coordinates} (Issue 44 — the constants previously
 * carried no source citation and no regression guard).
 * <p>
 * The IAU 1958 galactic coordinate system uses an orthonormal rotation
 * matrix, so {@code EQUATORIAL_TO_GALACTIC_J2000.T == GALACTIC_TO_EQUATORIAL_J2000}
 * and applying both in sequence to any point must return the original.
 */
@DisplayName("Coordinates: galactic ↔ equatorial round-trip")
class CoordinatesRoundTripTest {

    @Test
    @DisplayName("equatorial → galactic → equatorial round-trips within 1e-6 deg")
    void equatorialToGalacticRoundTrip() {
        // RA, Dec, distance in light years. Three reference points: the
        // Galactic Centre (Sgr A*), Polaris-ish, and an off-axis star.
        double[][] inputs = {
                {266.40510, -28.93617, 26000},   // Sgr A*
                {37.95456, 89.26411, 433},        // Polaris
                {83.633, 22.0145, 6500},          // M1 (Crab Nebula)
        };

        for (double[] in : inputs) {
            double[] gal = Coordinates.equatorialToGalactic(
                    Math.toRadians(in[0]), Math.toRadians(in[1]), in[2]);
            // equatorialToGalactic(ra, dec, distance) returns {l_deg, b_deg, r}.
            // Feed back into galacticToEquatorial.
            double[] equ = Coordinates.galacticToEquatorial(gal[0], gal[1], gal[2]);

            // Convert returned RA/Dec back to radians for comparison with input.
            double raOut = Math.toRadians(equ[0]);
            double decOut = Math.toRadians(equ[1]);
            double raIn = Math.toRadians(in[0]);
            double decIn = Math.toRadians(in[1]);

            assertEquals(raIn, raOut, 1e-6,
                    "RA round-trip failed for input " + in[0]);
            assertEquals(decIn, decOut, 1e-6,
                    "Dec round-trip failed for input " + in[1]);
            // Distance is in light years and can be large (e.g. 26000 ly to
            // the Galactic Centre), so use a relative tolerance.
            assertEquals(in[2], equ[2], in[2] * 1e-9,
                    "Distance round-trip failed for input " + in[2]);
        }
    }

    @Test
    @DisplayName("forward matrix is orthonormal (rows are unit vectors)")
    void forwardMatrixIsOrthonormal() {
        double[][] m = Coordinates.EQUATORIAL_TO_GALACTIC_J2000;
        for (int i = 0; i < 3; i++) {
            double norm = m[i][0] * m[i][0] + m[i][1] * m[i][1] + m[i][2] * m[i][2];
            assertEquals(1.0, norm, 1e-9, "Row " + i + " must be a unit vector");
        }
    }

    @Test
    @DisplayName("inverse matrix is the transpose of the forward matrix")
    void inverseIsTranspose() {
        double[][] fwd = Coordinates.EQUATORIAL_TO_GALACTIC_J2000;
        double[][] inv = Coordinates.GALACTIC_TO_EQUATORIAL_J2000;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                assertEquals(fwd[i][j], inv[j][i], 1e-12,
                        "inv[" + j + "][" + i + "] must equal fwd[" + i + "][" + j + "]");
            }
        }
    }

    @Test
    @DisplayName("RA hms → degrees conversion matches direct computation")
    void raToDegreesIsLinear() {
        // 12h 30m 30s = 12.508333h * 15 = 187.625°
        assertEquals(187.625, Coordinates.raToDegrees(12, 30, 30.0), 1e-9);
        assertEquals(0.0, Coordinates.raToDegrees(0, 0, 0.0), 1e-9);
        assertEquals(360.0, Coordinates.raToDegrees(24, 0, 0.0), 1e-9);
    }

    @Test
    @DisplayName("declination dms → degrees handles negative declinations correctly")
    void decToDegreesHandlesNegative() {
        // Sgr A* is at Dec -29°00'28.1" ≈ -29.007806°
        assertEquals(-29.007806, Coordinates.decToDegrees(-29, 0, 28.1), 1e-4);
        // Polaris ~89°15'50.79"
        assertEquals(89.264108, Coordinates.decToDegrees(89, 15, 50.79), 1e-4);
    }

    @Test
    @DisplayName("parsec ↔ light-year conversion round-trips")
    void parsecLightYearRoundTrip() {
        for (double pc : new double[]{1.0, 10.0, 100.0, 1234.5}) {
            double ly = Coordinates.parsecToLightYears(pc);
            double back = Coordinates.lightYearsToParsecs(ly);
            assertEquals(pc, back, 1e-9, "parsec round-trip for " + pc + " pc");
        }
    }
}
