package com.teamgannon.trips.spaceshipmodeller.integration;

import com.teamgannon.trips.spaceshipmodeller.core.SpaceshipDesign;
import com.teamgannon.trips.spaceshipmodeller.rules.ValidationEngine;
import org.springframework.stereotype.Component;

/**
 * Default {@link TransferPlannerBridge}.
 * <p>
 * {@link #canPlan} gates eligibility (valid and not reactionless); {@link #estimateTransfer} delegates to the
 * pure {@link TransferCalculator}. No UI here — the preview is rendered by {@code TransferPreviewDialog}.
 * When a real planner lands, {@code estimateTransfer} can hand off to it.
 */
@Component
public class TransferPlannerBridgeImpl implements TransferPlannerBridge {

    private final ValidationEngine validationEngine = new ValidationEngine();

    @Override
    public boolean canPlan(SpaceshipDesign ship) {
        if (ship == null) {
            return false;
        }
        return validationEngine.validate(ship).isValid() && !ship.driveSpecs().reactionless();
    }

    @Override
    public TransferEstimate estimateTransfer(double originAxisAu, double destinationAxisAu,
                                             double centralStarMassSolar, SpaceshipDesign ship) {
        return TransferCalculator.estimate(originAxisAu, destinationAxisAu, centralStarMassSolar, ship);
    }

    @Override
    public TransferPlan createTransferPlan(TransferBody origin, TransferBody destination,
                                           double centralStarMassSolar, SpaceshipDesign ship,
                                           TransferType type) {
        return TransferCalculator.plan(origin, destination, centralStarMassSolar, ship, type);
    }
}
