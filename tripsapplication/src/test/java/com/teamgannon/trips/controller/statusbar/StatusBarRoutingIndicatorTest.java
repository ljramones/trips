package com.teamgannon.trips.controller.statusbar;

import com.teamgannon.trips.events.RoutingStatusEvent;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Status bar rationalization Step 2 — D2 cleanup coverage for the Routing indicator.
 *
 * <p>{@link RoutingStatusEvent} is now listened on {@link StatusBarController} directly (Step 2
 * D2 cleanup; previously the listener lived in {@code RouteEventHandler} and bridged to the
 * controller's {@link StatusBarController#routingStatus(boolean)} method). The direct mutation
 * API is preserved for {@code RouteEventHandler.onNewRouteEvent}'s post-save callback that
 * needs synchronous state-setting.
 */
class StatusBarRoutingIndicatorTest {

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
    private Label routingStatus;

    @BeforeEach
    void setUp() throws Exception {
        Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");
        controller = new StatusBarController();
        routingStatus = new Label("Inactive");
        setField("routingStatus", routingStatus);
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
    // Event listener (Step 2 D2 cleanup)
    // ============================================================

    @Test
    @DisplayName("RoutingStatusEvent(true) listener sets label to 'Active' + red")
    void eventActiveSetsRedActive() throws Exception {
        runOnFx(() -> controller.onRoutingStatusEvent(new RoutingStatusEvent(this, true)));
        assertEquals("Active", routingStatus.getText());
        assertEquals(Color.RED, routingStatus.getTextFill());
    }

    @Test
    @DisplayName("RoutingStatusEvent(false) listener sets label to 'Inactive' + seagreen")
    void eventInactiveSetsGreenInactive() throws Exception {
        // First flip to active so we can verify deactivation transitions correctly.
        runOnFx(() -> controller.onRoutingStatusEvent(new RoutingStatusEvent(this, true)));
        runOnFx(() -> controller.onRoutingStatusEvent(new RoutingStatusEvent(this, false)));
        assertEquals("Inactive", routingStatus.getText());
        assertEquals(Color.SEAGREEN, routingStatus.getTextFill());
    }

    // ============================================================
    // Direct mutation API (preserved for RouteEventHandler.onNewRouteEvent's post-save callback)
    // ============================================================

    @Test
    @DisplayName("routingStatus(boolean) direct API still works — preserved for post-save callbacks")
    void directMutationApiPreserved() throws Exception {
        runOnFx(() -> controller.routingStatus(true));
        assertEquals("Active", routingStatus.getText());
        runOnFx(() -> controller.routingStatus(false));
        assertEquals("Inactive", routingStatus.getText());
    }

    // ============================================================
    // Event listener + direct API drive the same internal method
    // ============================================================

    @Test
    @DisplayName("event-driven and direct-call paths converge on the same UI state")
    void eventAndDirectAreEquivalent() throws Exception {
        // Path A: event listener
        runOnFx(() -> controller.onRoutingStatusEvent(new RoutingStatusEvent(this, true)));
        String afterEventText = routingStatus.getText();
        Color afterEventColor = (Color) routingStatus.getTextFill();

        // Reset
        runOnFx(() -> controller.onRoutingStatusEvent(new RoutingStatusEvent(this, false)));

        // Path B: direct API
        runOnFx(() -> controller.routingStatus(true));
        String afterDirectText = routingStatus.getText();
        Color afterDirectColor = (Color) routingStatus.getTextFill();

        assertEquals(afterEventText, afterDirectText, "both paths must produce identical text");
        assertEquals(afterEventColor, afterDirectColor, "both paths must produce identical color");
    }

    // ============================================================
    // Null-label safety
    // ============================================================

    @Test
    @DisplayName("routingStatus(boolean) is a no-op when routingStatus Label is null")
    void noopWhenLabelNull() throws Exception {
        setField("routingStatus", null);
        // No NPE — short-circuits.
        controller.routingStatus(true);
        controller.routingStatus(false);
    }
}
