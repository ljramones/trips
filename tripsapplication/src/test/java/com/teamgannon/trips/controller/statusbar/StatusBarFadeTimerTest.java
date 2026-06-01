package com.teamgannon.trips.controller.statusbar;

import com.teamgannon.trips.events.StatusUpdateEvent;
import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.control.Label;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Status bar rationalization Step 2 — coverage for the 5-minute fade timer on the action slot.
 *
 * <p>The fade timer is a {@link PauseTransition} created lazily on the first
 * {@link StatusUpdateEvent}. Each subsequent event cancels-and-restarts the timer; on
 * completion (or via direct {@code clearActionSlot()} call) the slot reverts to blank.
 *
 * <p>Tests don't wait 5 real minutes — they (a) verify the timer object exists after first
 * event, (b) verify subsequent events restart rather than accumulate, and (c) invoke the
 * timer's completion path via the package-private {@code clearActionSlot()} seam.
 */
class StatusBarFadeTimerTest {

    private static boolean javaFxInitialized = false;

    @BeforeAll
    static void initJavaFx() {
        try {
            Platform.startup(() -> {});
            javaFxInitialized = true;
        } catch (IllegalStateException e) {
            javaFxInitialized = true;
        } catch (Exception e) {
            System.out.println("JavaFX not available: " + e.getMessage());
            javaFxInitialized = false;
        }
    }

    private StatusBarController controller;
    private Label actionSlot;

    @BeforeEach
    void setUp() throws Exception {
        Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");
        controller = new StatusBarController();
        actionSlot = new Label("");
        setField("actionSlot", actionSlot);
    }

    private void setField(String name, Object value) throws Exception {
        Field f = StatusBarController.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(controller, value);
    }

    private PauseTransition timerField() throws Exception {
        Field f = StatusBarController.class.getDeclaredField("actionFadeTimer");
        f.setAccessible(true);
        return (PauseTransition) f.get(controller);
    }

    private void runOnFx(Runnable r) throws Exception {
        if (Platform.isFxApplicationThread()) {
            r.run();
            return;
        }
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> err = new AtomicReference<>();
        Platform.runLater(() -> {
            try { r.run(); }
            catch (Throwable t) { err.set(t); }
            finally { latch.countDown(); }
        });
        assertTrue(latch.await(5, TimeUnit.SECONDS), "FX action timed out");
        if (err.get() != null) {
            if (err.get() instanceof RuntimeException re) throw re;
            throw new RuntimeException(err.get());
        }
    }

    // ============================================================
    // Timer lifecycle
    // ============================================================

    @Test
    @DisplayName("first StatusUpdateEvent creates the fade timer (was null before)")
    void firstEventCreatesTimer() throws Exception {
        assertNull(timerField(), "timer is null before any event");
        runOnFx(() -> controller.onStatusUpdateEvent(new StatusUpdateEvent(this, "test")));
        assertNotNull(timerField(), "timer must exist after first event");
    }

    @Test
    @DisplayName("subsequent events reuse the same timer instance (cancel-and-restart, not accumulate)")
    void subsequentEventsReuseTimerInstance() throws Exception {
        runOnFx(() -> controller.onStatusUpdateEvent(new StatusUpdateEvent(this, "first")));
        PauseTransition firstTimer = timerField();
        runOnFx(() -> controller.onStatusUpdateEvent(new StatusUpdateEvent(this, "second")));
        runOnFx(() -> controller.onStatusUpdateEvent(new StatusUpdateEvent(this, "third")));
        PauseTransition lastTimer = timerField();
        assertEquals(firstTimer, lastTimer,
                "timer instance must be reused across events; accumulating new timers per event would leak");
    }

    @Test
    @DisplayName("fade duration is 5 minutes (locked constant)")
    void fadeDurationIsFiveMinutes() throws Exception {
        runOnFx(() -> controller.onStatusUpdateEvent(new StatusUpdateEvent(this, "test")));
        PauseTransition timer = timerField();
        assertEquals(5.0, timer.getDuration().toMinutes(), 1e-9,
                "fade timer duration must be 5 minutes per the §6.3 spec");
    }

    @Test
    @DisplayName("timer is RUNNING after an event (not stopped or paused)")
    void timerIsRunningAfterEvent() throws Exception {
        runOnFx(() -> controller.onStatusUpdateEvent(new StatusUpdateEvent(this, "test")));
        PauseTransition timer = timerField();
        assertEquals(Animation.Status.RUNNING, timer.getStatus());
    }

    // ============================================================
    // Slot clearing (timer completion path)
    // ============================================================

    @Test
    @DisplayName("clearActionSlot() blanks the label + drops the tooltip")
    void clearActionSlotBlanksLabel() throws Exception {
        runOnFx(() -> {
            controller.onStatusUpdateEvent(new StatusUpdateEvent(this, "message"));
            assertEquals("message", actionSlot.getText()); // sanity
            controller.clearActionSlot();
        });
        assertEquals("", actionSlot.getText());
        assertNull(actionSlot.getTooltip());
    }

    @Test
    @DisplayName("timer's onFinished handler calls clearActionSlot — verifies wiring not just method")
    void timerOnFinishedClearsTheSlot() throws Exception {
        runOnFx(() -> controller.onStatusUpdateEvent(new StatusUpdateEvent(this, "message")));
        PauseTransition timer = timerField();
        // Manually invoke the completion handler (we don't wait 5 min). This is the seam that
        // proves "when the timer fires, the slot clears" — same control flow as the natural
        // completion path.
        runOnFx(() -> timer.getOnFinished().handle(null));
        assertEquals("", actionSlot.getText());
        assertNull(actionSlot.getTooltip());
    }

    @Test
    @DisplayName("clearActionSlot() is a no-op when actionSlot Label is null (test harness path)")
    void clearActionSlotNoopWhenLabelNull() throws Exception {
        setField("actionSlot", null);
        // No NPE — short-circuits.
        controller.clearActionSlot();
    }
}
