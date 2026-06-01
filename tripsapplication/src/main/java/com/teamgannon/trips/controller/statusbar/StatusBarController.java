package com.teamgannon.trips.controller.statusbar;

import com.teamgannon.trips.algorithms.Universe;
import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.config.application.model.DataSetContext;
import com.teamgannon.trips.events.RoutingStatusEvent;
import com.teamgannon.trips.events.SetContextDataSetEvent;
import com.teamgannon.trips.events.StatusUpdateEvent;
import com.teamgannon.trips.javafxsupport.FxThread;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.spaceshipmodeller.service.UniverseDesignerService;
import com.teamgannon.trips.worldbuilding.UniverseActivationChangedEvent;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Status bar controller — rationalized 4-slot layout (status bar rationalization task,
 * post-Step-1-(γ)-pullback design). See docs/design/status-bar-rationalization.md.
 *
 * <p>The four slots, left to right:
 * <ol>
 *   <li><b>Action slot</b> ({@code actionSlot}): transient text from {@link StatusUpdateEvent}.
 *       Reserved ~280px width; ellipsis on overflow; tooltip shows full text. Blank at startup.
 *       5-minute {@link PauseTransition} fade timer clears the slot when no new event arrives;
 *       each new event cancels-and-restarts the timer.</li>
 *   <li><b>Dataset indicator</b> ({@code datasetStatus}): persistent. Updated by
 *       {@link SetContextDataSetEvent}. Initial state populated by Step 4's
 *       {@link ApplicationReadyEvent} listener (querying {@code TripsContext.getDataSetContext()}).</li>
 *   <li><b>Routing indicator</b> ({@code routingStatus}): persistent. Updated by direct
 *       {@code @EventListener RoutingStatusEvent} (Step 2 D2 cleanup — listener moved here
 *       from RouteEventHandler's bridge method). The {@link #routingStatus(boolean)} direct
 *       mutation API is preserved for callers that need synchronous state-setting (e.g. the
 *       post-save callback in RouteEventHandler.onNewRouteEvent).</li>
 *   <li><b>Worldbuilding indicator</b> ({@code universeStatus}): persistent. F.1 Step 8 wiring
 *       (UniverseActivationChangedEvent + ApplicationReadyEvent) preserved unchanged.</li>
 * </ol>
 *
 * <p>Spring delivers events synchronously on the publisher thread; every listener wraps its
 * scene-graph mutations in {@link FxThread#runOnFxThread(Runnable)}.
 */
@Slf4j
@Component
public class StatusBarController {

    /** v2 status bar rationalization — action slot fade duration. */
    static final Duration ACTION_FADE_DURATION = Duration.minutes(5);

    @Getter
    @FXML
    private HBox statusBar;

    /**
     * The action slot — transient text from {@link StatusUpdateEvent} publishers. Reserved
     * width prevents persistent indicators from shifting when messages arrive.
     */
    @FXML
    private Label actionSlot;

    /** Bold "Dataset:" prefix label. */
    @FXML
    private Label datasetStatusLabel;

    /** Dataset value — name of currently loaded dataset, or "(none selected)". */
    @FXML
    private Label datasetStatus;

    /** Bold "Routing:" prefix label. */
    @FXML
    private Label routingStatusLabel;

    /** Routing value — "Active" or "Inactive". */
    @FXML
    private Label routingStatus;

    /** Bold "Worldbuilding:" prefix label (F.1 Step 8). */
    @FXML
    private Label universeStatusLabel;

    /** Worldbuilding value — "Real only" or "Real + N universe(s) active" (F.1 Step 8). */
    @FXML
    private Label universeStatus;

    /**
     * Injected for the universe-activation indicator (F.1 Step 8). Field-injected (not
     * constructor-injected) because StatusBarController is FXML-instantiated by FxWeaver and
     * FXML constructors are no-arg. {@code required=false} for test-harness null safety.
     */
    @Autowired(required = false)
    private UniverseDesignerService universeDesignerService;

    /**
     * Injected for the Dataset indicator's boot-time initial state (Step 4). Same field-
     * injection rationale as {@link #universeDesignerService}. Provides access to
     * {@link DataSetContext#getDescriptor()} for the persisted "which dataset is loaded"
     * answer on app startup.
     */
    @Autowired(required = false)
    private TripsContext tripsContext;

    /**
     * Action slot fade timer. Created lazily on the first {@link StatusUpdateEvent} so headless
     * test environments don't pay the JavaFX-animation cost. Each new event cancels-and-restarts
     * the timer; on completion the slot clears.
     */
    private PauseTransition actionFadeTimer;

    @FXML
    public void initialize() {
        setupStatusbar();
    }

    private void setupStatusbar() {
        statusBar.setPrefWidth(Universe.boxWidth + 20);

        if (actionSlot != null) {
            actionSlot.setTextFill(Color.BLUE);
        }
        if (datasetStatusLabel != null) {
            datasetStatusLabel.setTextFill(Color.BLACK);
        }
        if (datasetStatus != null) {
            datasetStatus.setTextFill(Color.DARKSLATEGRAY);
        }
        if (routingStatusLabel != null) {
            routingStatusLabel.setTextFill(Color.BLACK);
        }
        if (routingStatus != null) {
            routingStatus.setTextFill(Color.SEAGREEN);
        }
        if (universeStatusLabel != null) {
            universeStatusLabel.setTextFill(Color.BLACK);
        }
        if (universeStatus != null) {
            universeStatus.setTextFill(Color.DARKSLATEBLUE);
        }
    }

    // ============================================================
    // Action slot
    // ============================================================

    /**
     * Update the action slot's text. Cancels any in-flight fade timer and starts a fresh
     * 5-minute timer; on expiration the slot clears. Tooltip is set to the full text so
     * users can hover to read truncated messages.
     *
     * <p>Visible for test seams; production callers use the {@link StatusUpdateEvent} path.
     */
    void setActionSlot(String message) {
        if (actionSlot == null) {
            return; // FXML not loaded (test harness)
        }
        actionSlot.setText(message == null ? "" : message);
        if (message != null && !message.isBlank()) {
            actionSlot.setTooltip(new Tooltip(message));
        } else {
            actionSlot.setTooltip(null);
        }
        restartActionFadeTimer();
    }

    /**
     * Start or restart the 5-minute fade timer for the action slot. Each new
     * {@link StatusUpdateEvent} cancels-and-restarts; on completion the slot clears.
     */
    private void restartActionFadeTimer() {
        if (actionFadeTimer != null) {
            actionFadeTimer.stop();
        } else {
            actionFadeTimer = new PauseTransition(ACTION_FADE_DURATION);
            actionFadeTimer.setOnFinished(e -> clearActionSlot());
        }
        actionFadeTimer.playFromStart();
    }

    /**
     * Clear the action slot — called by the fade timer's onFinished. Visible for tests.
     */
    void clearActionSlot() {
        if (actionSlot != null) {
            actionSlot.setText("");
            actionSlot.setTooltip(null);
        }
    }

    @EventListener
    public void onStatusUpdateEvent(StatusUpdateEvent event) {
        FxThread.runOnFxThread(() -> setActionSlot(event.getStatus()));
    }

    // ============================================================
    // Dataset indicator (Step 2 — SetContextDataSetEvent listener only;
    // Step 4 adds the ApplicationReadyEvent initial-state listener)
    // ============================================================

    /**
     * Refresh the Dataset indicator from a freshly-set dataset descriptor. Visible for tests.
     */
    void refreshDatasetStatus(DataSetDescriptor descriptor) {
        if (datasetStatus == null) {
            return;
        }
        if (descriptor == null) {
            datasetStatus.setText("(none selected)");
            datasetStatus.setTooltip(new Tooltip("No dataset currently selected"));
            return;
        }
        String name = descriptor.getDataSetName();
        datasetStatus.setText(name == null || name.isBlank() ? "(unnamed)" : name);
        String tooltipText = buildDatasetTooltip(descriptor);
        datasetStatus.setTooltip(new Tooltip(tooltipText));
    }

    private String buildDatasetTooltip(DataSetDescriptor descriptor) {
        StringBuilder sb = new StringBuilder();
        sb.append("Dataset: ").append(descriptor.getDataSetName());
        // DataSetDescriptor field names vary; using available getters defensively.
        try {
            long count = descriptor.getNumberStars();
            if (count > 0) {
                sb.append("\nStars: ").append(String.format("%,d", count));
            }
        } catch (Exception ignored) {
            // Method may not exist on every descriptor; ignore.
        }
        return sb.toString();
    }

    @EventListener
    public void onSetContextDataSetEvent(SetContextDataSetEvent event) {
        FxThread.runOnFxThread(() -> refreshDatasetStatus(event.getDescriptor()));
    }

    /**
     * Step 4 — read the boot-time Dataset state from TripsContext. Defensive null-traversal of
     * the two-step path ({@code TripsContext.getDataSetContext().getDescriptor()}); any null
     * along the chain resolves to "(none selected)" via {@link #refreshDatasetStatus(DataSetDescriptor)}.
     */
    void refreshDatasetStatusFromContext() {
        if (tripsContext == null) {
            refreshDatasetStatus(null);
            return;
        }
        DataSetContext ctx = tripsContext.getDataSetContext();
        DataSetDescriptor descriptor = ctx == null ? null : ctx.getDescriptor();
        refreshDatasetStatus(descriptor);
    }

    // ============================================================
    // Routing indicator (Step 2 D2 cleanup — listener moved here from
    // RouteEventHandler.onRoutingStatusEvent bridge)
    // ============================================================

    /**
     * Direct routing-status mutation API. Preserved for callers that need synchronous
     * state-setting (specifically RouteEventHandler.onNewRouteEvent's post-save callback;
     * the event-driven path is preferred for all other callers).
     */
    public void routingStatus(boolean statusFlag) {
        if (routingStatus == null) {
            return;
        }
        if (statusFlag) {
            routingStatus.setTextFill(Color.RED);
            routingStatus.setText("Active");
        } else {
            routingStatus.setTextFill(Color.SEAGREEN);
            routingStatus.setText("Inactive");
        }
    }

    /**
     * v2 status bar rationalization D2 cleanup — moved from RouteEventHandler's bridge method.
     * Direct listener on the controller is the canonical pattern (matches the
     * UniverseActivationChangedEvent listener in this class).
     */
    @EventListener
    public void onRoutingStatusEvent(RoutingStatusEvent event) {
        FxThread.runOnFxThread(() -> routingStatus(event.isStatusFlag()));
    }

    /**
     * Step 4 — boot-time Routing state. Routing has no persisted state today (transient
     * session toggle managed by {@code CurrentManualRoute}); on app start the default is
     * always inactive. This helper exists for uniformity with the Dataset + Worldbuilding
     * indicators' boot-time read pattern; if routing-state-persistence is added later, this
     * is the place to read it.
     */
    void refreshRoutingStatusFromContext() {
        routingStatus(false);
    }

    // ============================================================
    // Worldbuilding indicator (F.1 Step 8 — preserved unchanged)
    // ============================================================

    /**
     * v2 status bar rationalization Step 4 — uniform boot-time initial-state refresh for all
     * three persistent indicators. F.1 Step 8 wired this for the Worldbuilding indicator only;
     * Step 4 extends it to Dataset + Routing so persisted state (and defaults) surface
     * uniformly at app launch rather than waiting for the first user-driven event.
     *
     * <p>Order of refresh is the on-screen order (Dataset → Routing → Worldbuilding) so any
     * exception in one indicator's refresh still leaves earlier-rendered indicators in their
     * correct state. Exceptions are NOT swallowed here — Spring's @EventListener will log them
     * and continue; partial-failure is preferable to silent skip.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady(ApplicationReadyEvent event) {
        FxThread.runOnFxThread(() -> {
            refreshDatasetStatusFromContext();
            refreshRoutingStatusFromContext();
            refreshUniverseStatus();
        });
    }

    @EventListener
    public void onUniverseActivationChanged(UniverseActivationChangedEvent event) {
        FxThread.runOnFxThread(this::refreshUniverseStatus);
    }

    void refreshUniverseStatus() {
        if (universeStatus == null) {
            return;
        }
        if (universeDesignerService == null) {
            universeStatus.setText("Real only");
            return;
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
