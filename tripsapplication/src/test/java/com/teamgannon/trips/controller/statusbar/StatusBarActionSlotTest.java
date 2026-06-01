package com.teamgannon.trips.controller.statusbar;

import com.teamgannon.trips.events.StatusUpdateEvent;
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
 * Status bar rationalization Step 2 — coverage for the action slot listener.
 *
 * <p>The action slot displays whatever {@link StatusUpdateEvent} carries — past-tense actions,
 * progress text, or error feedback per the §6.1 descriptive framing. The slot is blank at
 * startup; updates land via the {@code @EventListener onStatusUpdateEvent} on
 * {@link StatusBarController}. Reflection-set fields seam mirrors the F.1 Step 8 pattern.
 */
class StatusBarActionSlotTest {

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
    // Listener wiring
    // ============================================================

    @Test
    @DisplayName("StatusUpdateEvent listener writes the message text to the action slot")
    void statusUpdateEventWritesMessage() throws Exception {
        runOnFx(() -> controller.onStatusUpdateEvent(new StatusUpdateEvent(this, "Dataset loaded")));
        assertEquals("Dataset loaded", actionSlot.getText());
    }

    @Test
    @DisplayName("subsequent StatusUpdateEvents replace the previous message")
    void subsequentEventsReplaceText() throws Exception {
        runOnFx(() -> {
            controller.onStatusUpdateEvent(new StatusUpdateEvent(this, "first"));
            controller.onStatusUpdateEvent(new StatusUpdateEvent(this, "second"));
            controller.onStatusUpdateEvent(new StatusUpdateEvent(this, "third"));
        });
        assertEquals("third", actionSlot.getText());
    }

    @Test
    @DisplayName("non-empty message sets a tooltip with the full text (for truncation discovery)")
    void nonEmptyMessageSetsTooltip() throws Exception {
        runOnFx(() -> controller.onStatusUpdateEvent(new StatusUpdateEvent(this,
                "Loaded HYG-30ly dataset (12,847 stars)")));
        assertNotNull(actionSlot.getTooltip());
        assertEquals("Loaded HYG-30ly dataset (12,847 stars)", actionSlot.getTooltip().getText());
    }

    @Test
    @DisplayName("setActionSlot with null clears the tooltip")
    void nullMessageClearsTooltip() throws Exception {
        runOnFx(() -> {
            controller.onStatusUpdateEvent(new StatusUpdateEvent(this, "initial"));
            controller.setActionSlot(null);
        });
        assertEquals("", actionSlot.getText());
        assertNull(actionSlot.getTooltip());
    }

    @Test
    @DisplayName("setActionSlot is a no-op when actionSlot Label is null (FXML not loaded)")
    void noopWhenLabelNull() throws Exception {
        setField("actionSlot", null);
        runOnFx(() -> controller.setActionSlot("test"));
        // No NPE; method short-circuits.
    }

    // ============================================================
    // Startup state
    // ============================================================

    @Test
    @DisplayName("action slot is blank initially (no setActionSlot called means no text)")
    void blankAtStartup() {
        // Constructor didn't call setActionSlot, so the label's text stays as initialized ("").
        // (FXML default would set this to "" via the rewrite; we mirror that in setUp.)
        assertEquals("", actionSlot.getText());
        assertNull(actionSlot.getTooltip());
    }
}
