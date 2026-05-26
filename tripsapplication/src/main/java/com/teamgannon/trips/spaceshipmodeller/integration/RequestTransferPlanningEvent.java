package com.teamgannon.trips.spaceshipmodeller.integration;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * Request to open the Transfer Planning dialog for a given solar system context.
 * <p>
 * Phase 3.3 of the codebase-review remediation decoupled the
 * {@code SolarSystemSpacePane} from the Spaceship Modeller module by promoting
 * direct service injection (8-arg constructor) into this event. The pane
 * publishes a {@code RequestTransferPlanningEvent} carrying just the context
 * the dialog needs (bodies, origin preference, central-star mass, system id);
 * the spaceshipmodeller's {@link com.teamgannon.trips.spaceshipmodeller.ui.TransferPlanningCoordinator}
 * listens, looks up the ship catalog, and opens the dialog.
 */
@Getter
public class RequestTransferPlanningEvent extends ApplicationEvent {

    /** Persistent id of the solar system (null for transient / preview). */
    private final String solarSystemId;

    /** Available orbiting bodies to plan between (must be non-empty). */
    private final List<TransferBody> bodies;

    /** Starting point — typically the body the user right-clicked. May be null. */
    private final TransferBody preferredOrigin;

    /** Central body mass in solar masses (Sun = 1.0); 1.0 if unknown. */
    private final double centralStarMassSolar;

    public RequestTransferPlanningEvent(Object source,
                                        String solarSystemId,
                                        List<TransferBody> bodies,
                                        TransferBody preferredOrigin,
                                        double centralStarMassSolar) {
        super(source);
        this.solarSystemId = solarSystemId;
        this.bodies = bodies;
        this.preferredOrigin = preferredOrigin;
        this.centralStarMassSolar = centralStarMassSolar;
    }
}
