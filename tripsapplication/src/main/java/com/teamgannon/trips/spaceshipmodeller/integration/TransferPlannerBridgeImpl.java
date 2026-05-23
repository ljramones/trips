package com.teamgannon.trips.spaceshipmodeller.integration;

import com.teamgannon.trips.spaceshipmodeller.core.SpaceshipDesign;
import com.teamgannon.trips.spaceshipmodeller.rules.ValidationEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.teamgannon.trips.support.AlertFactory.showInfoMessage;

/**
 * Placeholder {@link TransferPlannerBridge}.
 * <p>
 * It validates that a ship is plannable and presents a summary of the inputs a transfer planner would
 * consume (drive, delta-V, gross mass). When the real Transfer Planner lands, only {@link #planTransfer}
 * needs to change; {@link #canPlan} already encodes the precondition.
 */
@Slf4j
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
    public void planTransfer(SpaceshipDesign ship) {
        if (ship == null) {
            return;
        }
        if (!canPlan(ship)) {
            showInfoMessage("Plan Mission Transfer",
                    "\"" + ship.name() + "\" cannot be used for transfer planning yet: it must be a valid, "
                            + "reaction-mass-carrying design.");
            return;
        }
        double deltaV = ship.estimateDeltaVKmps();
        String summary = """
                Transfer planning for "%s" — coming soon.

                The planner will use:
                  drive:     %s
                  delta-V:   %.0f km/s
                  gross mass: %.0f t""".formatted(
                ship.name(), ship.driveType().name(), deltaV, ship.grossMassTons());
        log.info("Transfer plan requested for '{}' (drive={}, dV={} km/s)",
                ship.name(), ship.driveType().name(), deltaV);
        showInfoMessage("Plan Mission Transfer", summary);
    }
}
