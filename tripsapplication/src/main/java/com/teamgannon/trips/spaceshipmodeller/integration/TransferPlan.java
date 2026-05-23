package com.teamgannon.trips.spaceshipmodeller.integration;

import java.util.List;

/**
 * A concrete transfer plan: an ordered list of {@link ManeuverNode} burns plus the totals a mission
 * planner cares about.
 * <p>
 * Produced by {@link TransferCalculator#plan}. This is the structured artefact the UI presents and that a
 * fuller mission planner could consume/refine.
 *
 * @param shipName             the ship the plan was computed for
 * @param type                 the transfer type
 * @param origin               origin body
 * @param destination          destination body
 * @param nodes                the burns, in execution order
 * @param totalDeltaVKmps      sum of all burn delta-Vs, in km/s
 * @param totalPropellantTons  total propellant consumed, in tonnes ({@code NaN} if not derivable)
 * @param transferTimeDays     coast time between burns, in days
 * @param shipDeltaVKmps       the ship's available delta-V, in km/s ({@code NaN} if reactionless)
 * @param feasible             {@code true} if the ship's delta-V budget covers the total
 * @param propellantSufficient {@code true} if the ship carries enough propellant for the burns
 */
public record TransferPlan(
        String shipName,
        TransferType type,
        TransferBody origin,
        TransferBody destination,
        List<ManeuverNode> nodes,
        double totalDeltaVKmps,
        double totalPropellantTons,
        double transferTimeDays,
        double shipDeltaVKmps,
        boolean feasible,
        boolean propellantSufficient
) {

    public TransferPlan {
        nodes = nodes == null ? List.of() : List.copyOf(nodes);
    }
}
