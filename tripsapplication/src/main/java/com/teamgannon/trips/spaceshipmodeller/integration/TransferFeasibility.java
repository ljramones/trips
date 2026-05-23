package com.teamgannon.trips.spaceshipmodeller.integration;

/**
 * Evaluates the three-level {@link Feasibility} of a transfer from delta-V and propellant margins, with
 * small tolerances so an exact tie (e.g. a minimum-time burn that uses the whole propellant load) reads as
 * {@code MARGINAL} rather than {@code INSUFFICIENT}.
 * <p>
 * Rules:
 * <ul>
 *   <li>Delta-V: margin &gt; 0.5 km/s &rarr; FEASIBLE; 0 &le; margin &le; 0.5 &rarr; MARGINAL;
 *       margin &lt; 0 &rarr; INSUFFICIENT.</li>
 *   <li>Propellant: required &lt; available &rarr; FEASIBLE; required &asymp; available (within ~1%)
 *       &rarr; MARGINAL; required &gt; available &rarr; INSUFFICIENT.</li>
 * </ul>
 * Transfers needing no delta-V (sails, wormholes) and non-derivable propellant (reactionless) never fail on
 * that axis. The overall result is the worse of the two.
 */
public final class TransferFeasibility {

    private static final double DV_MARGINAL_KMPS = 0.5;
    private static final double EPS = 1e-6;

    private TransferFeasibility() {
    }

    /**
     * @param requiredDvKmps    delta-V the transfer needs (km/s)
     * @param shipDvKmps        the ship's delta-V budget (km/s; {@code NaN} if reactionless)
     * @param requiredPropTons  propellant the transfer needs (tonnes; {@code NaN} if not derivable)
     * @param availablePropTons propellant the ship carries (tonnes)
     * @return the worse of the delta-V and propellant statuses
     */
    public static Feasibility evaluate(double requiredDvKmps, double shipDvKmps,
                                       double requiredPropTons, double availablePropTons) {
        return worst(deltaVStatus(requiredDvKmps, shipDvKmps),
                propellantStatus(requiredPropTons, availablePropTons));
    }

    /** @return the delta-V feasibility level. */
    public static Feasibility deltaVStatus(double requiredDvKmps, double shipDvKmps) {
        if (requiredDvKmps <= EPS) {
            return Feasibility.FEASIBLE; // no delta-V needed (sails, wormholes, ...)
        }
        if (Double.isNaN(shipDvKmps)) {
            return Feasibility.INSUFFICIENT; // reactionless but delta-V is required
        }
        double margin = shipDvKmps - requiredDvKmps;
        if (margin < -EPS) {
            return Feasibility.INSUFFICIENT;
        }
        return margin <= DV_MARGINAL_KMPS ? Feasibility.MARGINAL : Feasibility.FEASIBLE;
    }

    /** @return the propellant feasibility level. */
    public static Feasibility propellantStatus(double requiredPropTons, double availablePropTons) {
        if (Double.isNaN(requiredPropTons) || requiredPropTons <= EPS) {
            return Feasibility.FEASIBLE; // no (or non-derivable) propellant requirement
        }
        double tolerance = Math.max(EPS, availablePropTons * 1e-6);
        if (requiredPropTons > availablePropTons + tolerance) {
            return Feasibility.INSUFFICIENT;
        }
        // within ~1% of the tank (incl. an exact tie) = marginal "uses essentially all propellant"
        return requiredPropTons >= availablePropTons * 0.99 ? Feasibility.MARGINAL : Feasibility.FEASIBLE;
    }

    private static Feasibility worst(Feasibility a, Feasibility b) {
        return a.ordinal() >= b.ordinal() ? a : b;
    }
}
