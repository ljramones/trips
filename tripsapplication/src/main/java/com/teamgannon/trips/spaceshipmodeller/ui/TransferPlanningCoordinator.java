package com.teamgannon.trips.spaceshipmodeller.ui;

import com.teamgannon.trips.javafxsupport.FxThread;
import com.teamgannon.trips.spaceshipmodeller.integration.RequestTransferPlanningEvent;
import com.teamgannon.trips.spaceshipmodeller.integration.TransferPlannerBridge;
import com.teamgannon.trips.spaceshipmodeller.service.SpaceshipService;
import com.teamgannon.trips.support.AlertFactory;
import com.terranrepublic.assets.SpaceshipDesign;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Listens for {@link RequestTransferPlanningEvent}s and opens the
 * {@link TransferPreviewDialog}.
 * <p>
 * Phase 3.3 of the codebase-review remediation introduced this coordinator so
 * {@code SolarSystemSpacePane} no longer needs to inject {@link SpaceshipService},
 * {@link TransferPlannerBridge}, or {@link TransferPlannerLauncher}. The pane
 * just publishes an event carrying the solar-system context; everything
 * spaceshipmodeller-specific lives here.
 */
@Slf4j
@Component
public class TransferPlanningCoordinator {

    private final SpaceshipService spaceshipService;
    private final TransferPlannerBridge transferPlannerBridge;
    private final TransferPlannerLauncher transferPlannerLauncher;

    public TransferPlanningCoordinator(SpaceshipService spaceshipService,
                                       TransferPlannerBridge transferPlannerBridge,
                                       TransferPlannerLauncher transferPlannerLauncher) {
        this.spaceshipService = spaceshipService;
        this.transferPlannerBridge = transferPlannerBridge;
        this.transferPlannerLauncher = transferPlannerLauncher;
    }

    @EventListener
    public void onRequestTransferPlanningEvent(RequestTransferPlanningEvent event) {
        // Defensive FX-thread wrap (per CLAUDE.md @EventListener threading contract).
        FxThread.runOnFxThread(() -> openDialog(event));
    }

    private void openDialog(RequestTransferPlanningEvent event) {
        List<SpaceshipDesign> ships = spaceshipService.findAll();
        if (ships.isEmpty()) {
            AlertFactory.showInfoMessage("Plan Transfer",
                    "There are no ships in the library yet. Create one in the Spaceship Modeller "
                            + "(Design menu) first.");
            return;
        }
        if (event.getBodies() == null || event.getBodies().isEmpty()) {
            AlertFactory.showInfoMessage("Plan Transfer",
                    "This system has no planets to plan a transfer between.");
            return;
        }
        new TransferPreviewDialog(
                transferPlannerBridge,
                ships,
                event.getBodies(),
                event.getPreferredOrigin(),
                event.getCentralStarMassSolar())
                .onCreate(transferPlannerLauncher::createAndOpen, event.getSolarSystemId())
                .showAndWait();
    }
}
