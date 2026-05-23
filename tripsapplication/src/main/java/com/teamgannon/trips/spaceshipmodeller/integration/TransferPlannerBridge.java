package com.teamgannon.trips.spaceshipmodeller.integration;

import com.teamgannon.trips.spaceshipmodeller.core.SpaceshipDesign;

/**
 * Seam between the Spaceship Modeller and TRIPS mission/transfer planning.
 * <p>
 * Today this produces a first-order {@link TransferEstimate} (Hohmann Δv, propellant, burn/transfer time)
 * that the UI presents in a preview dialog. When a full Transfer Planner exists, this interface is the
 * place to delegate to it; the {@link #canPlan} precondition already encodes who is eligible.
 */
public interface TransferPlannerBridge {

    /**
     * @param ship the design to check
     * @return {@code true} if a transfer can be planned for this ship: it must be valid and not
     * reaction-mass-free (sails have no rocket-equation delta-V to plan with)
     */
    boolean canPlan(SpaceshipDesign ship);

    /**
     * Estimates a Hohmann transfer between two circular orbits for the given ship.
     *
     * @param originAxisAu          origin orbital radius, in AU
     * @param destinationAxisAu     destination orbital radius, in AU
     * @param centralStarMassSolar  central star mass, in solar masses
     * @param ship                  the ship attempting the transfer
     * @return the transfer estimate
     */
    TransferEstimate estimateTransfer(double originAxisAu, double destinationAxisAu,
                                      double centralStarMassSolar, SpaceshipDesign ship);

    /**
     * Creates a full transfer plan (departure/arrival burns, propellant, timing) for the ship between two
     * bodies. This is the structured artefact a fuller mission planner would consume or refine.
     *
     * @param origin               origin body
     * @param destination          destination body
     * @param centralStarMassSolar central star mass, in solar masses
     * @param ship                 the ship
     * @param type                 the transfer type
     * @return the plan
     */
    TransferPlan createTransferPlan(TransferBody origin, TransferBody destination,
                                    double centralStarMassSolar, SpaceshipDesign ship, TransferType type);
}
