package com.teamgannon.trips.solarsystem.rendering;

import com.teamgannon.trips.planetary.modelling.PlanetDescription;
import com.teamgannon.trips.planetary.modelling.SolarSystemDescription;

import java.util.List;

/**
 * Pure-function geometry helpers used by the solar-system renderer when
 * positioning bodies in the scene graph: outer / inner orbital bounds,
 * collision-aware planet phase angles, and max-radius lookup.
 * <p>
 * Extracted from {@code SolarSystemRenderer} in Phase 4.1.3 of the
 * codebase-review remediation. Stateless and FX-free — safe to call from any
 * thread.
 */
public final class SystemGeometryHelper {

    private SystemGeometryHelper() {
    }

    /**
     * Maximum semi-major axis among the non-moon planets, also bounded below
     * by the habitable zone's outer edge so the renderer's scale calculation
     * never crops the habitable-zone ring out of view.
     */
    public static double maxOrbitalDistance(SolarSystemDescription description) {
        double max = 0;
        for (PlanetDescription planet : description.getPlanetDescriptionList()) {
            if (planet.isMoon()) {
                continue;
            }
            if (planet.getSemiMajorAxis() > max) {
                max = planet.getSemiMajorAxis();
            }
        }
        if (description.getHabitableZoneOuterAU() > max) {
            max = description.getHabitableZoneOuterAU();
        }
        return max;
    }

    /**
     * Minimum (positive) semi-major axis among non-moon planets, or 0 if there
     * are none. Used by the renderer to drive auto-log-scale and inner-grid
     * spacing.
     */
    public static double minOrbitalDistance(SolarSystemDescription description) {
        double min = Double.MAX_VALUE;
        for (PlanetDescription planet : description.getPlanetDescriptionList()) {
            if (planet.isMoon()) {
                continue;
            }
            double sma = planet.getSemiMajorAxis();
            if (sma > 0 && sma < min) {
                min = sma;
            }
        }
        return min == Double.MAX_VALUE ? 0 : min;
    }

    /**
     * Compute phase angles for planets so that orbits which are visually close
     * (within 15% of each other in semi-major axis) end up on opposite sides
     * of the system, preventing planet-icon overlap.
     *
     * @param planets list of planets, sorted by semi-major axis ascending
     * @return array of true-anomaly angles in degrees, same length as input
     */
    public static double[] planetPhaseAngles(List<PlanetDescription> planets) {
        int n = planets.size();
        if (n == 0) return new double[0];

        double[] angles = new double[n];
        double baseSpread = 360.0 / Math.max(n, 1);

        for (int i = 0; i < n; i++) {
            double baseAngle = i * baseSpread;
            if (i > 0) {
                double prevSma = planets.get(i - 1).getSemiMajorAxis();
                double currSma = planets.get(i).getSemiMajorAxis();
                // If orbits are within 15% of each other, flip 180° relative
                // to the prior planet so they don't visually overlap.
                if (prevSma > 0 && Math.abs(currSma - prevSma) / prevSma < 0.15) {
                    baseAngle = angles[i - 1] + 180;
                }
            }
            angles[i] = baseAngle % 360;
        }
        return angles;
    }

    /**
     * Largest planet radius in the list, or 1.0 if all are zero / negative.
     * Used for relative-size scaling so that the biggest planet anchors the
     * proportional sphere math.
     */
    public static double maxPlanetRadius(List<PlanetDescription> planets) {
        double max = 0;
        for (PlanetDescription planet : planets) {
            if (planet.getRadius() > max) {
                max = planet.getRadius();
            }
        }
        return max > 0 ? max : 1.0;
    }
}
