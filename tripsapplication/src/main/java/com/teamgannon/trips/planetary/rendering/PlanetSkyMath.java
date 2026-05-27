package com.teamgannon.trips.planetary.rendering;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.planetarymodelling.PlanetDescription;
import javafx.scene.paint.Color;

/**
 * Pure-function helpers for sibling-planet sky rendering: orbital position
 * propagation, apparent-magnitude estimation, and planet-type colour lookup.
 * <p>
 * Extracted from {@code PlanetarySkyRenderer} in Phase 4.7 of the
 * codebase-review remediation. Stateless static utility — no scene-graph
 * dependencies, safe to call off the FX thread.
 *
 * <h2>Coordinate convention</h2>
 * Positions are returned in <b>light-years</b>, in the same Cartesian frame
 * the rest of the rendering pipeline uses. {@link #AU_TO_LY} performs the
 * unit conversion.
 */
final class PlanetSkyMath {

    /** 1 AU expressed in light-years. */
    static final double AU_TO_LY = 1.0 / 63241.0;

    private PlanetSkyMath() {
    }

    /**
     * Propagate a sibling planet to its current Keplerian position in
     * heliocentric → galactic coordinates (light-years).
     *
     * @param planet    planet whose orbital elements drive the calculation
     * @param hostStar  host star's (x, y, z) in light-years sets the origin
     * @param localTime "local time" in hours (0-24) used as a phase proxy so
     *                  different planets sit at different orbital positions
     * @return [x, y, z] in light-years, in the same frame as the host star
     */
    static double[] calculatePlanetOrbitalPosition(PlanetDescription planet,
                                                   StarDisplayRecord hostStar,
                                                   double localTime) {
        double starX = hostStar.getX();
        double starY = hostStar.getY();
        double starZ = hostStar.getZ();

        double semiMajorAU = planet.getSemiMajorAxis();
        double eccentricity = planet.getEccentricity();
        double inclination = Math.toRadians(planet.getInclination());
        double argPeri = Math.toRadians(planet.getArgumentOfPeriapsis());
        double longAscNode = Math.toRadians(planet.getLongitudeOfAscendingNode());

        double orbitalPeriod = planet.getOrbitalPeriod();
        if (orbitalPeriod <= 0) orbitalPeriod = 365.25;

        // Phase proxy: hours-of-day → fraction-of-orbit, scaled by period ratio.
        double meanAnomaly = (localTime / 24.0) * 2 * Math.PI * (365.25 / orbitalPeriod);

        double eccentricAnomaly = solveKeplerEquation(meanAnomaly, eccentricity);

        double trueAnomaly = 2 * Math.atan2(
                Math.sqrt(1 + eccentricity) * Math.sin(eccentricAnomaly / 2),
                Math.sqrt(1 - eccentricity) * Math.cos(eccentricAnomaly / 2));

        double radius = semiMajorAU * (1 - eccentricity * eccentricity) /
                       (1 + eccentricity * Math.cos(trueAnomaly));

        double xOrbit = radius * Math.cos(trueAnomaly);
        double yOrbit = radius * Math.sin(trueAnomaly);

        // R = R_z(Omega) * R_x(i) * R_z(omega) * [x, y, 0]
        double cosO = Math.cos(longAscNode);
        double sinO = Math.sin(longAscNode);
        double cosI = Math.cos(inclination);
        double sinI = Math.sin(inclination);
        double cosW = Math.cos(argPeri);
        double sinW = Math.sin(argPeri);

        double x = (cosO * cosW - sinO * sinW * cosI) * xOrbit +
                   (-cosO * sinW - sinO * cosW * cosI) * yOrbit;
        double y = (sinO * cosW + cosO * sinW * cosI) * xOrbit +
                   (-sinO * sinW + cosO * cosW * cosI) * yOrbit;
        double z = (sinW * sinI) * xOrbit + (cosW * sinI) * yOrbit;

        return new double[]{
                starX + x * AU_TO_LY,
                starY + y * AU_TO_LY,
                starZ + z * AU_TO_LY
        };
    }

    /** Newton-Raphson on Kepler's equation E - e·sin(E) = M. 10 iterations, 1e-8 tolerance. */
    static double solveKeplerEquation(double meanAnomaly, double eccentricity) {
        double E = meanAnomaly;
        for (int i = 0; i < 10; i++) {
            double dE = (E - eccentricity * Math.sin(E) - meanAnomaly) /
                       (1 - eccentricity * Math.cos(E));
            E -= dE;
            if (Math.abs(dE) < 1e-8) break;
        }
        return E;
    }

    /**
     * Estimate apparent magnitude of a planet at {@code distanceAU} from the
     * observer. Base magnitude is keyed off the planet-type enum (Jupiter-like
     * = very bright, asteroids = dim) with a radius-based fallback. Distance
     * attenuation is the standard 5·log₁₀(d) term.
     */
    static double calculatePlanetApparentMagnitude(PlanetDescription planet, double distanceAU) {
        double baseMag;
        if (planet.getPlanetTypeEnum() != null) {
            baseMag = switch (planet.getPlanetTypeEnum()) {
                case tGasGiant, tSubGasGiant, tSubSubGasGiant -> -2.5;
                case tIce, tWater -> -1.5;
                case tSuperEarth, tTerrestrial -> 0.0;
                case tRock, tMartian, tAsteroids -> 2.0;
                case tVenusian -> -0.5;
                default -> 0.5;
            };
        } else {
            double radius = planet.getRadius();
            if (radius > 5) baseMag = -2.5;
            else if (radius > 2) baseMag = -1.0;
            else if (radius > 0.5) baseMag = 0.5;
            else baseMag = 2.0;
        }

        double distanceFactor = 5.0 * Math.log10(Math.max(0.1, distanceAU));
        return baseMag + distanceFactor;
    }

    /**
     * Pick a representative colour for a planet based on its type enum, with
     * an equilibrium-temperature fallback (hot=orange, cold=pale blue).
     */
    static Color getPlanetColor(PlanetDescription planet) {
        if (planet.getPlanetTypeEnum() != null) {
            return switch (planet.getPlanetTypeEnum()) {
                case tGasGiant -> Color.rgb(255, 200, 150);
                case tSubGasGiant, tSubSubGasGiant -> Color.rgb(230, 190, 140);
                case tIce -> Color.rgb(150, 200, 255);
                case tWater -> Color.rgb(100, 150, 220);
                case tSuperEarth -> Color.rgb(180, 200, 180);
                case tTerrestrial -> Color.rgb(200, 180, 160);
                case tRock, tMartian -> Color.rgb(180, 140, 120);
                case tVenusian -> Color.rgb(255, 230, 180);
                case tAsteroids -> Color.rgb(150, 140, 130);
                default -> Color.rgb(200, 200, 200);
            };
        }

        double temp = planet.getEquilibriumTemperature();
        if (temp > 500) return Color.rgb(255, 180, 120);
        if (temp > 300) return Color.rgb(200, 180, 160);
        if (temp > 200) return Color.rgb(150, 180, 200);
        return Color.rgb(200, 220, 255);
    }

    /**
     * Re-scale a star's catalogued apparent magnitude for a new observer
     * distance: m_new = m_old + 5·log₁₀(d_new / d_old). Returns the original
     * magnitude unchanged when either distance is non-positive.
     */
    static double adjustMagnitude(double originalMag, double distFromSol, double distFromPlanet) {
        if (distFromSol <= 0 || distFromPlanet <= 0) return originalMag;
        return originalMag + 5.0 * Math.log10(distFromPlanet / distFromSol);
    }
}
