package com.teamgannon.trips.spaceshipmodeller.integration;

import com.teamgannon.trips.spaceshipmodeller.core.SpaceshipDesign;

/**
 * Seam between the Spaceship Modeller and TRIPS mission/transfer planning.
 * <p>
 * A real implementation will hand the ship's performance envelope (mass, drive Isp/thrust, delta-V budget)
 * to the transfer planner once that feature exists. Until then {@link TransferPlannerBridgeImpl} provides a
 * informative placeholder so the UI wiring is complete and testable.
 */
public interface TransferPlannerBridge {

    /**
     * Opens transfer planning for the given ship (or reports that the planner is not yet available).
     *
     * @param ship the design to plan a transfer for
     */
    void planTransfer(SpaceshipDesign ship);

    /**
     * @param ship the design to check
     * @return {@code true} if a transfer can be planned for this ship: it must be valid and not
     * reaction-mass-free (sails have no rocket-equation delta-V to plan with)
     */
    boolean canPlan(SpaceshipDesign ship);
}
