package com.teamgannon.trips.controller.statusbar;

import com.teamgannon.trips.algorithms.Universe;
import com.teamgannon.trips.events.StatusUpdateEvent;
import com.teamgannon.trips.javafxsupport.FxThread;
import com.teamgannon.trips.spaceshipmodeller.service.UniverseDesignerService;
import com.teamgannon.trips.worldbuilding.UniverseActivationChangedEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class StatusBarController {

    @Getter
    @FXML
    private HBox statusBar;

    @FXML
    private Label databaseCommentLabel;

    @FXML
    private Label databaseStatus;

    @FXML
    private Label routingStatusLabel;

    @FXML
    private Label routingStatus;

    /** v2 Phase F.1 Step 8 — fixed-text "Worldbuilding:" label. */
    @FXML
    private Label universeStatusLabel;

    /** v2 Phase F.1 Step 8 — dynamic text "Real only" / "Real + N universe(s) active". */
    @FXML
    private Label universeStatus;

    /**
     * Injected for the universe-activation indicator. Field-injected (not constructor-injected)
     * because StatusBarController is FXML-instantiated by FxWeaver and FXML constructors are no-
     * arg. v2 Phase F.1 Step 8 — nullable safety in {@link #refreshUniverseStatus} guards the
     * test-harness scenarios where the service isn't wired.
     */
    @Autowired(required = false)
    private UniverseDesignerService universeDesignerService;

    @FXML
    public void initialize() {
        setupStatusbar();
    }

    private void setupStatusbar() {
        statusBar.setPrefWidth(Universe.boxWidth + 20);

        databaseCommentLabel.setTextFill(Color.BLACK);
        databaseStatus.setTextFill(Color.BLUE);
        routingStatusLabel.setTextFill(Color.BLACK);
        routingStatus.setTextFill(Color.SEAGREEN);
        if (universeStatusLabel != null) {
            universeStatusLabel.setTextFill(Color.BLACK);
        }
        if (universeStatus != null) {
            universeStatus.setTextFill(Color.DARKSLATEBLUE);
        }
    }

    public void setStatus(String newStatus) {
        databaseStatus.setText(newStatus);
    }

    public void routingStatus(boolean statusFlag) {
        if (statusFlag) {
            routingStatus.setTextFill(Color.RED);
            routingStatus.setText("Active");
        } else {
            routingStatus.setTextFill(Color.SEAGREEN);
            routingStatus.setText("Inactive");
        }
    }

    @EventListener
    public void onStatusUpdateEvent(StatusUpdateEvent event) {
        FxThread.runOnFxThread(() -> {
            setStatus(event.getStatus());
        });
    }

    /**
     * v2 Phase F.1 Step 8 — initialise the universe indicator at application startup. Runs on
     * {@link ApplicationReadyEvent} (after Flyway + seeders) so the DB query against the
     * universe table is safe. If the user previously activated universes and restarted, the
     * indicator reflects that activation state immediately rather than waiting for the next
     * user-driven toggle.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady(ApplicationReadyEvent event) {
        FxThread.runOnFxThread(this::refreshUniverseStatus);
    }

    /**
     * v2 Phase F.1 Step 8 — refresh the universe indicator when any universe activates/deactivates.
     * The event payload IS the post-toggle state, but the indicator shows the
     * <em>aggregate</em> state across all universes, so we re-query the service rather than
     * read just this event's universe.
     */
    @EventListener
    public void onUniverseActivationChanged(UniverseActivationChangedEvent event) {
        FxThread.runOnFxThread(this::refreshUniverseStatus);
    }

    /**
     * Re-renders the universe indicator text + tooltip from the current set of active universes.
     * Safe to call from anywhere; nullable-safe if either the service or the labels are missing
     * (test harness scenarios).
     */
    void refreshUniverseStatus() {
        if (universeStatus == null) {
            return; // labels not yet initialised (FXML hasn't loaded)
        }
        if (universeDesignerService == null) {
            universeStatus.setText("Real only");
            return; // service not wired (test harness)
        }
        List<com.terranrepublic.assets.Universe> active = universeDesignerService.findAllActive();
        if (active.isEmpty()) {
            universeStatus.setText("Real only");
            universeStatus.setTooltip(new Tooltip("No fictional universes active. Real-data catalog "
                    + "entries are visible by default (R1.8)."));
        } else {
            universeStatus.setText("Real + " + active.size() + " universe(s) active");
            String names = active.stream()
                    .map(com.terranrepublic.assets.Universe::name)
                    .sorted()
                    .collect(Collectors.joining("\n  • ", "Active universes:\n  • ", ""));
            universeStatus.setTooltip(new Tooltip(names));
        }
    }
}
