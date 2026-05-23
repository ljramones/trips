package com.teamgannon.trips.spaceshipmodeller.ui;

import com.teamgannon.trips.spaceshipmodeller.core.SpaceshipDesign;
import com.teamgannon.trips.spaceshipmodeller.integration.TransferPlan;
import com.teamgannon.trips.spaceshipmodeller.integration.TransferPlannerBridge;
import com.teamgannon.trips.spaceshipmodeller.planner.SavedTransferPlan;
import com.teamgannon.trips.spaceshipmodeller.planner.ShowTransferTrajectoryEvent;
import com.teamgannon.trips.spaceshipmodeller.planner.TransferPlanService;
import com.teamgannon.trips.spaceshipmodeller.service.SpaceshipService;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Opens and reuses the {@link TransferPlannerPanel} window, and saves freshly computed plans into it.
 * <p>
 * This is the single seam other features use to "create a full transfer plan and open the planner": the
 * Spaceship Modeller and the Solar System view pass {@link #createAndOpen} as a {@code TransferPlanSink}.
 * A single {@link Stage}/panel instance is reused; closing it lets the next call recreate it.
 */
@Slf4j
@Component
public class TransferPlannerLauncher {

    private final TransferPlanService planService;
    private final SpaceshipService spaceshipService;
    private final TransferPlannerBridge bridge;
    private final ApplicationEventPublisher eventPublisher;

    private Stage stage;
    private TransferPlannerPanel panel;

    public TransferPlannerLauncher(TransferPlanService planService,
                                   SpaceshipService spaceshipService,
                                   TransferPlannerBridge bridge,
                                   ApplicationEventPublisher eventPublisher) {
        this.planService = planService;
        this.spaceshipService = spaceshipService;
        this.bridge = bridge;
        this.eventPublisher = eventPublisher;
    }

    /** Opens (or re-focuses) the Transfer Planner window. */
    public void open() {
        open(null);
    }

    /**
     * Opens the Transfer Planner window and selects the given plan.
     *
     * @param selectPlanId plan id to select, or {@code null}
     */
    public void open(String selectPlanId) {
        try {
            if (stage == null || !stage.isShowing()) {
                panel = new TransferPlannerPanel(planService, spaceshipService, bridge, eventPublisher);
                stage = new Stage();
                stage.setTitle(SpaceshipModellerLabels.get("planner.title", "Transfer Planner"));
                stage.initModality(Modality.NONE);
                stage.setScene(new Scene(panel, 920, 600));
                stage.setOnCloseRequest(e -> {
                    stage = null;
                    panel = null;
                });
                stage.show();
            } else {
                stage.toFront();
            }
            if (panel != null) {
                panel.refreshAndSelect(selectPlanId);
            }
        } catch (Exception e) {
            log.error("Error opening Transfer Planner", e);
        }
    }

    /**
     * Saves a computed plan and opens the planner focused on it. Signature matches
     * {@code TransferPlanSink}.
     *
     * @param plan                 the computed plan
     * @param ship                 the ship the plan is for
     * @param solarSystemId        solar system id (may be {@code null})
     * @param centralStarMassSolar central star mass used
     */
    public void createAndOpen(TransferPlan plan, SpaceshipDesign ship,
                              String solarSystemId, double centralStarMassSolar) {
        SavedTransferPlan saved = planService.saveComputed(
                plan, ship == null ? null : ship.id(), solarSystemId, centralStarMassSolar);
        eventPublisher.publishEvent(new ShowTransferTrajectoryEvent(
                this, saved.solarSystemId(), saved.originAu(), saved.destinationAu()));
        open(saved.id());
    }
}
