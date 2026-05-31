package com.teamgannon.trips.solarsystem.jumppoint;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.model.PlanetDescription;
import com.teamgannon.trips.model.SolarSystemDescription;
import javafx.geometry.Point3D;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * v2 Phase E.1 Step 5 — tests the {@link JumpPointComputer} algorithmic contract.
 *
 * <p>Pure-function tests: no Spring context, no JPA, no event listeners. The computer takes
 * a star + a system geometry and returns {@code Optional<Point3D>}. Tests cover:
 * <ul>
 *   <li>Deterministic per-star (same input → same output across calls)</li>
 *   <li>Different stars → different positions</li>
 *   <li>Multi-star systems compute distinct positions per star</li>
 *   <li>Rejection sampling against Hill-sphere exclusion zones</li>
 *   <li>Iteration cap exhaustion → {@code Optional.empty()}</li>
 *   <li>Outer-system boundary computation (planet-derived vs HYG-fallback)</li>
 *   <li>Hill-sphere math against known values (Earth, Jupiter)</li>
 *   <li>Position-in-band invariant</li>
 *   <li>Spherical sampling smoke test</li>
 * </ul>
 */
class JumpPointComputerTest {

    private final JumpPointComputer computer = new JumpPointComputer();

    // --------------------------------------------------------------- helpers

    private static StarDisplayRecord starOf(String id, double massSolar) {
        StarDisplayRecord s = new StarDisplayRecord();
        s.setRecordId(id);
        s.setMass(massSolar);
        s.setStarName("test-" + id);
        return s;
    }

    private static PlanetDescription planetOf(double massEarth, double smaAU) {
        PlanetDescription p = new PlanetDescription();
        p.setMass(massEarth);
        p.setSemiMajorAxis(smaAU);
        return p;
    }

    private static SolarSystemDescription systemOf(StarDisplayRecord primary, PlanetDescription... planets) {
        SolarSystemDescription system = new SolarSystemDescription();
        system.setStarDisplayRecord(primary);
        List<PlanetDescription> planetList = new ArrayList<>();
        for (PlanetDescription p : planets) {
            planetList.add(p);
        }
        system.setPlanetDescriptionList(planetList);
        return system;
    }

    // ============================================================
    // Deterministic per-star
    // ============================================================

    @Test
    @DisplayName("same star + same system → same Vec3 every time")
    void deterministicPerStar() {
        StarDisplayRecord sol = starOf("star-sol", 1.0);
        SolarSystemDescription system = systemOf(sol);  // HYG-fallback: ~40 AU boundary
        Optional<Point3D> first = computer.compute(sol, system);
        Optional<Point3D> second = computer.compute(sol, system);
        assertTrue(first.isPresent());
        assertEquals(first.get(), second.get(),
                "deterministic-seed contract: same star id + same system geometry → exact-same Vec3");
    }

    @Test
    @DisplayName("different star ids → different positions (different seed paths)")
    void differentStarsDifferentPositions() {
        StarDisplayRecord starA = starOf("star-a", 1.0);
        StarDisplayRecord starB = starOf("star-b", 1.0);
        SolarSystemDescription systemA = systemOf(starA);
        SolarSystemDescription systemB = systemOf(starB);
        Optional<Point3D> posA = computer.compute(starA, systemA);
        Optional<Point3D> posB = computer.compute(starB, systemB);
        assertTrue(posA.isPresent());
        assertTrue(posB.isPresent());
        assertNotEquals(posA.get(), posB.get(),
                "different star ids must seed different sequences → different positions");
    }

    @Test
    @DisplayName("multi-star system: each star gets its own jump point (different Vec3s)")
    void multiStarScalingProducesDistinctPositions() {
        // Trinary system: each star computed independently from its own id.
        StarDisplayRecord starA = starOf("trinary-a", 1.2);
        StarDisplayRecord starB = starOf("trinary-b", 0.9);
        StarDisplayRecord starC = starOf("trinary-c", 0.3);
        SolarSystemDescription systemA = systemOf(starA);
        SolarSystemDescription systemB = systemOf(starB);
        SolarSystemDescription systemC = systemOf(starC);
        Optional<Point3D> posA = computer.compute(starA, systemA);
        Optional<Point3D> posB = computer.compute(starB, systemB);
        Optional<Point3D> posC = computer.compute(starC, systemC);
        assertTrue(posA.isPresent());
        assertTrue(posB.isPresent());
        assertTrue(posC.isPresent());
        // Three distinct positions.
        assertNotEquals(posA.get(), posB.get());
        assertNotEquals(posA.get(), posC.get());
        assertNotEquals(posB.get(), posC.get());
    }

    // ============================================================
    // Rejection sampling
    // ============================================================

    @Test
    @DisplayName("rejection sampling: candidate inside a planet's Hill sphere is rejected; computer finds clear position")
    void rejectionSamplingFindsClearPosition() {
        // Sol-like star with one massive planet (Jupiter-class) in the outer band.
        // Its Hill sphere is ~0.35 AU radially. Most of the outer band stays clear; the
        // computer should find a position outside the Hill region quickly.
        StarDisplayRecord sol = starOf("star-with-jupiter", 1.0);
        PlanetDescription jupiter = planetOf(318.0, 35.0);  // Jupiter in the outer band
        SolarSystemDescription system = systemOf(sol, jupiter);
        Optional<Point3D> result = computer.compute(sol, system);
        assertTrue(result.isPresent(),
                "computer must find a clear position with one Hill-sphere obstacle");
        // Verify the result is not within the Jupiter Hill region (radial check).
        double r = result.get().magnitude();
        double hillR = JumpPointComputer.hillSphereRadius(
                318.0 / JumpPointComputer.EARTH_MASSES_PER_SOLAR_MASS, 35.0, 1.0);
        assertTrue(Math.abs(r - 35.0) >= hillR,
                "returned position must be outside Jupiter's Hill region; |r - 35.0|="
                        + Math.abs(r - 35.0) + " vs hillR=" + hillR);
    }

    @Test
    @DisplayName("iteration cap exhaustion: when every candidate falls inside a Hill sphere, returns Optional.empty()")
    void iterationCapExhaustionReturnsEmpty() {
        // Construct a pathological system: a star with an obscenely massive planet whose
        // Hill sphere consumes the entire outer band. Every candidate position will be
        // rejected; after 100 iterations the computer gives up.
        StarDisplayRecord star = starOf("star-jump-inaccessible", 1.0);
        // Outer boundary will be derived from the planet sma (35 AU). Make the planet so
        // massive that its Hill radius exceeds the entire band-width (35 - 26.25 = 8.75 AU).
        // Hill radius = 35 * cbrt(massSolar / 3). For hillR ≥ 20, need massSolar ≥ 3 * 0.187 ≈ 0.56.
        // In earth masses: 0.56 * 332946 ≈ 186,000 Earth masses (about 0.6 solar mass — a tiny stellar
        // companion masquerading as a planet, for test purposes).
        PlanetDescription planet = planetOf(200_000.0, 35.0);
        SolarSystemDescription system = systemOf(star, planet);
        Optional<Point3D> result = computer.compute(star, system);
        assertTrue(result.isEmpty(),
                "100-iteration cap must exhaust when no clear position exists; star is jump-inaccessible");
    }

    // ============================================================
    // Outer-system boundary
    // ============================================================

    @Test
    @DisplayName("outer boundary = max(planet sma) when planets present")
    void outerBoundaryFromPlanets() {
        StarDisplayRecord star = starOf("star-with-planets", 1.0);
        PlanetDescription p1 = planetOf(1.0, 10.0);
        PlanetDescription p2 = planetOf(2.0, 30.0);
        PlanetDescription p3 = planetOf(0.5, 40.0);
        double boundary = JumpPointComputer.computeOuterSystemBoundary(
                star, List.of(p1, p2, p3));
        assertEquals(40.0, boundary, 1e-9,
                "outer boundary must be the max semi-major axis across planet inventory");
    }

    @Test
    @DisplayName("HYG-fallback boundary: 40 AU for 1 solar mass")
    void hygFallbackBoundarySol() {
        StarDisplayRecord sol = starOf("sol", 1.0);
        double boundary = JumpPointComputer.computeOuterSystemBoundary(sol, List.of());
        assertEquals(40.0, boundary, 1e-9,
                "HYG-fallback for 1 solar mass must yield 40 AU (matches Pluto by construction)");
    }

    @Test
    @DisplayName("HYG-fallback boundary: ~50.4 AU for 2 solar masses (Sirius-class)")
    void hygFallbackBoundarySirius() {
        StarDisplayRecord sirius = starOf("sirius", 2.0);
        double boundary = JumpPointComputer.computeOuterSystemBoundary(sirius, List.of());
        assertEquals(40.0 * Math.cbrt(2.0), boundary, 1e-9);
        // Reality check: should be ~50.4 AU
        assertTrue(boundary > 50.0 && boundary < 51.0,
                "Sirius-class fallback should be ~50.4 AU; got " + boundary);
    }

    @Test
    @DisplayName("HYG-fallback boundary: ~26.8 AU for 0.3 solar masses (red-dwarf-class)")
    void hygFallbackBoundaryRedDwarf() {
        StarDisplayRecord redDwarf = starOf("red-dwarf", 0.3);
        double boundary = JumpPointComputer.computeOuterSystemBoundary(redDwarf, List.of());
        assertEquals(40.0 * Math.cbrt(0.3), boundary, 1e-9);
        assertTrue(boundary > 26.0 && boundary < 28.0,
                "red-dwarf-class fallback should be ~26.8 AU; got " + boundary);
    }

    @Test
    @DisplayName("HYG-fallback boundary: ~137 AU for 40 solar masses (O-class)")
    void hygFallbackBoundaryOClass() {
        StarDisplayRecord oClass = starOf("o-class", 40.0);
        double boundary = JumpPointComputer.computeOuterSystemBoundary(oClass, List.of());
        assertTrue(boundary > 135.0 && boundary < 140.0,
                "O-class fallback should be ~137 AU; got " + boundary);
    }

    @Test
    @DisplayName("zero stellar mass falls back to 1 solar mass (defensive)")
    void zeroStellarMassDefensiveFallback() {
        StarDisplayRecord defaulted = starOf("defaulted", 0.0);
        double boundary = JumpPointComputer.computeOuterSystemBoundary(defaulted, List.of());
        assertEquals(40.0, boundary, 1e-9,
                "zero stellar mass must defensively default to 1 solar mass → 40 AU");
    }

    // ============================================================
    // Hill-sphere math (against known astronomical values)
    // ============================================================

    @Test
    @DisplayName("Hill sphere: Earth (1 Earth mass, 1 AU, 1 solar mass) ≈ 0.01 AU")
    void hillSphereEarth() {
        // 1 Earth mass in solar masses
        double earthMassSolar = 1.0 / JumpPointComputer.EARTH_MASSES_PER_SOLAR_MASS;
        double hillR = JumpPointComputer.hillSphereRadius(earthMassSolar, 1.0, 1.0);
        // Expected ~0.01 AU per the design spec; tolerance 5% for the textbook value
        assertTrue(hillR > 0.009 && hillR < 0.011,
                "Earth Hill sphere should be ~0.01 AU; got " + hillR);
    }

    @Test
    @DisplayName("Hill sphere: Jupiter (318 Earth masses, 5.2 AU, 1 solar mass) ≈ 0.35 AU")
    void hillSphereJupiter() {
        double jupiterMassSolar = 318.0 / JumpPointComputer.EARTH_MASSES_PER_SOLAR_MASS;
        double hillR = JumpPointComputer.hillSphereRadius(jupiterMassSolar, 5.2, 1.0);
        assertTrue(hillR > 0.34 && hillR < 0.36,
                "Jupiter Hill sphere should be ~0.35 AU; got " + hillR);
    }

    @Test
    @DisplayName("Hill sphere: kg-unit inputs work (formula is unit-agnostic)")
    void hillSphereKgUnits() {
        // Per design spec — works with any consistent mass unit.
        double earthKg = 5.97e24;
        double sunKg = 1.989e30;
        double hillR = JumpPointComputer.hillSphereRadius(earthKg, 1.0, sunKg);
        assertTrue(hillR > 0.009 && hillR < 0.011,
                "Earth Hill sphere from kg inputs should also be ~0.01 AU; got " + hillR);
    }

    @Test
    @DisplayName("Hill sphere: zero parentMass returns 0 (defensive)")
    void hillSphereDefensive() {
        assertEquals(0.0, JumpPointComputer.hillSphereRadius(1.0, 1.0, 0.0));
        assertEquals(0.0, JumpPointComputer.hillSphereRadius(-1.0, 1.0, 1.0));
    }

    // ============================================================
    // Position-in-band invariant
    // ============================================================

    @ParameterizedTest
    @ValueSource(doubles = {0.1, 0.5, 1.0, 2.0, 5.0, 10.0, 40.0})
    @DisplayName("position-in-band invariant: every returned Vec3 has magnitude in [innerRadius, outerRadius]")
    void positionInBand(double stellarMassSolar) {
        StarDisplayRecord star = starOf("star-" + stellarMassSolar, stellarMassSolar);
        SolarSystemDescription system = systemOf(star);  // HYG fallback
        Optional<Point3D> result = computer.compute(star, system);
        assertTrue(result.isPresent());
        double r = result.get().magnitude();
        double boundary = JumpPointComputer.computeOuterSystemBoundary(star, List.of());
        double inner = boundary * JumpPointComputer.INNER_BAND_FRACTION;
        double outer = boundary * JumpPointComputer.OUTER_BAND_FRACTION;
        assertTrue(r >= inner && r <= outer,
                "result magnitude " + r + " must be in [" + inner + ", " + outer + "] "
                        + "for stellar mass " + stellarMassSolar);
    }

    // ============================================================
    // Spherical sampling smoke test
    // ============================================================

    @Test
    @DisplayName("spherical sampling: polar covers [0, π] and azimuthal covers [0, 2π] across many samples")
    void sphericalSamplingCoversFullSphere() {
        double minPolar = Double.MAX_VALUE;
        double maxPolar = 0.0;
        double minAzimuthal = Double.MAX_VALUE;
        double maxAzimuthal = 0.0;
        // Sample 1000 candidates from many seeds; verify polar and azimuthal coverage.
        Random outer = new Random(42);
        for (int i = 0; i < 1000; i++) {
            Random rng = new Random(outer.nextLong());
            Point3D candidate = JumpPointComputer.sampleCandidate(rng, 30.0, 40.0);
            // Recover polar/azimuthal from the cartesian point (only for the sampling check).
            double r = candidate.magnitude();
            double polar = Math.acos(candidate.getZ() / r);
            double azimuthal = Math.atan2(candidate.getY(), candidate.getX());
            if (azimuthal < 0) azimuthal += 2 * Math.PI;
            minPolar = Math.min(minPolar, polar);
            maxPolar = Math.max(maxPolar, polar);
            minAzimuthal = Math.min(minAzimuthal, azimuthal);
            maxAzimuthal = Math.max(maxAzimuthal, azimuthal);
        }
        assertTrue(minPolar < 0.1, "min polar should be near 0; got " + minPolar);
        assertTrue(maxPolar > Math.PI - 0.1, "max polar should be near π; got " + maxPolar);
        assertTrue(minAzimuthal < 0.1, "min azimuthal should be near 0; got " + minAzimuthal);
        assertTrue(maxAzimuthal > 2 * Math.PI - 0.1,
                "max azimuthal should be near 2π; got " + maxAzimuthal);
    }

    @Test
    @DisplayName("spherical sampling: radii fall within the [innerRadius, outerRadius] band")
    void sphericalSamplingRadiusInBand() {
        double inner = 30.0;
        double outer = 40.0;
        for (int i = 0; i < 100; i++) {
            Random rng = new Random(i);
            Point3D candidate = JumpPointComputer.sampleCandidate(rng, inner, outer);
            double r = candidate.magnitude();
            assertTrue(r >= inner - 1e-9 && r <= outer + 1e-9,
                    "sample " + i + " radius " + r + " must be in [" + inner + ", " + outer + "]");
        }
    }

    // ============================================================
    // Edge cases
    // ============================================================

    @Test
    @DisplayName("empty planet list uses HYG fallback (no rejection sampling needed)")
    void emptyPlanetListFirstIterationWins() {
        StarDisplayRecord star = starOf("star-empty", 1.0);
        SolarSystemDescription system = systemOf(star);
        Optional<Point3D> result = computer.compute(star, system);
        assertTrue(result.isPresent(),
                "with no planets, no Hill-sphere exclusion → first iteration always succeeds");
    }

    @Test
    @DisplayName("planets present but all sma = 0: falls back to stellar-mass boundary")
    void zeroSmaPlanetsFallBackToStellarMass() {
        StarDisplayRecord star = starOf("star-bad-data", 1.0);
        PlanetDescription degenerate = planetOf(1.0, 0.0);
        double boundary = JumpPointComputer.computeOuterSystemBoundary(star, List.of(degenerate));
        assertEquals(40.0, boundary, 1e-9,
                "zero-sma 'planets' should not stuff the boundary at 0; fall back to stellar mass");
    }
}
