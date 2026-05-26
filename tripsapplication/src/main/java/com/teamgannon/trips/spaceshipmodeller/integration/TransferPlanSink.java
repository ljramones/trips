package com.teamgannon.trips.spaceshipmodeller.integration;

import com.terranrepublic.assets.SpaceshipDesign;

/**
 * Callback for "Create Full Transfer Plan": receives the computed {@link TransferPlan} plus the context
 * needed to persist it (the ship, an optional solar-system id, and the central star mass).
 * <p>
 * Lets {@code TransferPreviewDialog} stay decoupled from persistence — different callers supply different
 * behaviour (save and open the planner; or just save and refresh in place).
 */
@FunctionalInterface
public interface TransferPlanSink {

    /**
     * @param plan                 the computed plan
     * @param ship                 the ship the plan is for
     * @param solarSystemId        id of the solar system the plan was created in (may be {@code null})
     * @param centralStarMassSolar central star mass used, in solar masses
     */
    void accept(TransferPlan plan, SpaceshipDesign ship, String solarSystemId, double centralStarMassSolar);
}
