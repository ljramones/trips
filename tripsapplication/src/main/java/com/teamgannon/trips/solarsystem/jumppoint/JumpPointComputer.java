package com.teamgannon.trips.solarsystem.jumppoint;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.model.PlanetDescription;
import com.teamgannon.trips.model.SolarSystemDescription;
import javafx.geometry.Point3D;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Deterministic jump-point position computer.
 *
 * <p>v2 Phase E.1 §3 — given a star and its system geometry, returns the position of the star's
 * jump point in AU coordinates (relative to the star), or {@link Optional#empty()} if the star
 * is jump-inaccessible (every candidate position over 100 iterations falls within some mass
 * body's Hill-sphere exclusion zone).
 *
 * <p>Per-star deterministic: the result is keyed off the star's id via
 * {@code Random(starId.hashCode() ^ iteration)}, so the same (star, system) input always
 * produces the same output. This means jump-point positions survive JVM restarts and
 * activation-listener replays without drifting.
 *
 * <p>Worldbuilding rationale: jump points represent a fictional future-discovered
 * gravitational physics. The deterministic-seed approach is honest to the "we don't know the
 * rule" framing — the position isn't derived from real-physics analysis (which would give
 * readers an unintended causal explanation) but is *consistent* across observations, the way
 * a real natural phenomenon would be.
 *
 * <p>This class is a pure function: no persistence, no event handling, no side effects beyond
 * the {@link Random} sequence. Step 6's {@code JumpPointActivationListener} composes
 * persistence + the activation hook around it.
 *
 * <h2>Unit conventions</h2>
 * <ul>
 *   <li>Star mass: solar masses ({@link StarDisplayRecord#getMass()})</li>
 *   <li>Planet mass: Earth masses ({@link PlanetDescription#getMass()})</li>
 *   <li>Semi-major axis: AU ({@link PlanetDescription#getSemiMajorAxis()})</li>
 *   <li>Returned position: AU (Cartesian, star-centered)</li>
 * </ul>
 * Internal Hill-sphere math normalises both masses to solar units; the public
 * {@link #hillSphereRadius(double, double, double)} helper is unit-agnostic as long as
 * caller-supplied masses share a unit.
 */
@Component
public class JumpPointComputer {

    /** Maximum number of rejection-sampling iterations before giving up (per §3.1). */
    static final int MAX_ITERATIONS = 100;

    /** Inner edge of the outer-system band as a fraction of the outer boundary (per §3.1). */
    static final double INNER_BAND_FRACTION = 0.75;

    /** Outer edge of the outer-system band as a fraction of the outer boundary (= 1.0). */
    static final double OUTER_BAND_FRACTION = 1.0;

    /** 1 solar mass in Earth masses (used for {@link PlanetDescription#getMass()} normalisation). */
    static final double EARTH_MASSES_PER_SOLAR_MASS = 332_946.0;

    /**
     * HYG-fallback constant for the outer-system boundary formula
     * {@code outerBoundary_AU = HYG_OUTER_BOUNDARY_CONSTANT_AU * pow(stellarMassSolar, 1/3)}.
     * Calibrated so a 1-solar-mass star yields ~40 AU (matching Sol's Pluto-orbit anchor).
     */
    static final double HYG_OUTER_BOUNDARY_CONSTANT_AU = 40.0;

    /**
     * Computes the deterministic jump-point position for {@code star} within {@code system}.
     *
     * @param star   the star to compute for (its {@code recordId} seeds the deterministic
     *               sequence; its {@code mass} is used for the HYG fallback boundary)
     * @param system the system description (planet list drives the outer-system boundary and
     *               the Hill-sphere exclusion zones)
     * @return {@code Optional.of(position_in_AU)} if a valid jump point exists, or
     *         {@code Optional.empty()} after the 100-iteration cap exhausts
     */
    public Optional<Point3D> compute(StarDisplayRecord star, SolarSystemDescription system) {
        double outerBoundary = computeOuterSystemBoundary(star, system.getPlanetDescriptionList());
        double innerRadius = outerBoundary * INNER_BAND_FRACTION;
        double outerRadius = outerBoundary * OUTER_BAND_FRACTION;
        long baseSeed = star.getRecordId().hashCode();
        for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
            Random rng = new Random(baseSeed ^ iteration);
            Point3D candidate = sampleCandidate(rng, innerRadius, outerRadius);
            if (!isExcluded(candidate, star, system.getPlanetDescriptionList())) {
                return Optional.of(candidate);
            }
        }
        return Optional.empty();
    }

    // ------------------------------------------------------------- outer boundary

    /**
     * v2 Phase E.1 §3.1 — outer-system boundary in AU.
     *
     * <p>If {@code planets} contains entries, returns the maximum semi-major axis. Otherwise
     * (HYG-database star with no planet inventory) returns
     * {@code HYG_OUTER_BOUNDARY_CONSTANT_AU * pow(stellarMassSolar, 1/3)}; calibrated so Sol →
     * 40 AU, red dwarfs → ~18–27 AU, O-class → ~137 AU.
     */
    static double computeOuterSystemBoundary(StarDisplayRecord star, List<PlanetDescription> planets) {
        if (planets != null && !planets.isEmpty()) {
            double maxSma = 0.0;
            for (PlanetDescription p : planets) {
                if (p.getSemiMajorAxis() > maxSma) {
                    maxSma = p.getSemiMajorAxis();
                }
            }
            if (maxSma > 0) {
                return maxSma;
            }
            // Planets present but all have zero sma — fall through to stellar-mass fallback.
        }
        double stellarMassSolar = Math.max(star.getMass(), 0.0);
        if (stellarMassSolar <= 0.0) {
            // Defensive: treat unknown stellar mass as 1 solar mass.
            stellarMassSolar = 1.0;
        }
        return HYG_OUTER_BOUNDARY_CONSTANT_AU * Math.cbrt(stellarMassSolar);
    }

    // ------------------------------------------------------------- candidate sampling

    /**
     * v2 Phase E.1 §3.1 — uniform sample on the spherical shell {@code [innerRadius, outerRadius]}.
     * Uses the standard area-preserving spherical sampling (acos for polar to compensate for the
     * latitude-band area distortion).
     */
    static Point3D sampleCandidate(Random rng, double innerRadius, double outerRadius) {
        double radius = innerRadius + rng.nextDouble() * (outerRadius - innerRadius);
        double polar = Math.acos(2.0 * rng.nextDouble() - 1.0);
        double azimuthal = rng.nextDouble() * 2.0 * Math.PI;
        double x = radius * Math.sin(polar) * Math.cos(azimuthal);
        double y = radius * Math.sin(polar) * Math.sin(azimuthal);
        double z = radius * Math.cos(polar);
        return new Point3D(x, y, z);
    }

    // ------------------------------------------------------------- Hill-sphere exclusion

    /**
     * v2 Phase E.1 §3.2 — Hill sphere radius. Unit-agnostic for the mass arguments as long as
     * both masses are in the same unit; the result is in the same unit as {@code sma}.
     *
     * <pre>
     *     hillRadius = sma * cuberoot(mass / (3 * parentMass))
     * </pre>
     */
    public static double hillSphereRadius(double mass, double sma, double parentMass) {
        if (parentMass <= 0 || mass < 0) {
            return 0.0;
        }
        return sma * Math.cbrt(mass / (3.0 * parentMass));
    }

    /**
     * Returns {@code true} if the candidate falls within any mass body's Hill-sphere exclusion
     * zone. Implemented as a toroidal radial check: a candidate at distance {@code r} from the
     * star is rejected if {@code |r - planet.sma| < hillRadius(planet)} for any planet — i.e.
     * the candidate is within the planet's Hill sphere at some point on its orbit. This is a
     * conservative (slightly over-rejecting) check that matches the §3.2 worldbuilding intent
     * of "no jump points in mass-dominated regions".
     */
    static boolean isExcluded(Point3D candidate, StarDisplayRecord star, List<PlanetDescription> planets) {
        if (planets == null || planets.isEmpty()) {
            return false;
        }
        double starMassSolar = Math.max(star.getMass(), 0.0);
        if (starMassSolar <= 0.0) {
            starMassSolar = 1.0;
        }
        double candidateRadius = candidate.magnitude();
        for (PlanetDescription p : planets) {
            double planetMassSolar = p.getMass() / EARTH_MASSES_PER_SOLAR_MASS;
            double hillRadius = hillSphereRadius(planetMassSolar, p.getSemiMajorAxis(), starMassSolar);
            if (hillRadius <= 0) {
                continue;
            }
            if (Math.abs(candidateRadius - p.getSemiMajorAxis()) < hillRadius) {
                return true;
            }
        }
        return false;
    }
}
