package com.teamgannon.trips.controller.statusbar;

import com.teamgannon.trips.spaceshipmodeller.service.UniverseDesignerService;
import com.terranrepublic.assets.Universe;
import com.terranrepublic.assets.UniverseLifecycle;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * v2 Phase F.1 Step 8 — coverage for {@link StatusBarController}'s universe activation
 * indicator. The text format must be:
 * <ul>
 *   <li>"Real only" when no universes active</li>
 *   <li>"Real + N universe(s) active" when N >= 1</li>
 * </ul>
 * Tooltip lists the active universe names.
 *
 * <p>Tests use reflection to set the {@code universeStatus} + {@code universeDesignerService}
 * fields directly because the controller is FXML-instantiated; instantiating it via the FXMLLoader
 * harness would require the full FX-stage scaffolding. Reflection is the pragmatic test seam.
 */
@ExtendWith(MockitoExtension.class)
class StatusBarUniverseIndicatorTest {

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

    @Mock
    private UniverseDesignerService universeDesignerService;

    private StatusBarController controller;
    private Label universeStatusLabel;
    private Label universeStatusValueLabel;

    @BeforeEach
    void setUp() throws Exception {
        Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");
        controller = new StatusBarController();
        universeStatusLabel = new Label(" Worldbuilding: ");
        universeStatusValueLabel = new Label("Real only");
        setField("universeStatusLabel", universeStatusLabel);
        setField("universeStatus", universeStatusValueLabel);
        setField("universeDesignerService", universeDesignerService);
    }

    private void setField(String name, Object value) throws Exception {
        Field f = StatusBarController.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(controller, value);
    }

    private static Universe universe(String id, String name, boolean active) {
        return new Universe(id, name, "", "", "1.0", UniverseLifecycle.AVAILABLE, active);
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
    // Indicator text format
    // ============================================================

    @Test
    @DisplayName("indicator shows 'Real only' when no universes active")
    void indicatorRealOnlyWhenNoneActive() throws Exception {
        when(universeDesignerService.findAllActive()).thenReturn(List.of());
        runOnFx(controller::refreshUniverseStatus);
        assertEquals("Real only", universeStatusValueLabel.getText());
    }

    @Test
    @DisplayName("indicator shows 'Real + 1 universe(s) active' when one universe active")
    void indicatorRealPlusOneWhenOneActive() throws Exception {
        when(universeDesignerService.findAllActive())
                .thenReturn(List.of(universe("u-test", "Test Universe", true)));
        runOnFx(controller::refreshUniverseStatus);
        assertEquals("Real + 1 universe(s) active", universeStatusValueLabel.getText());
    }

    @Test
    @DisplayName("indicator shows 'Real + 3 universe(s) active' when three universes active")
    void indicatorRealPlusThreeWhenThreeActive() throws Exception {
        when(universeDesignerService.findAllActive()).thenReturn(List.of(
                universe("u-1", "Alpha", true),
                universe("u-2", "Beta", true),
                universe("u-3", "Gamma", true)));
        runOnFx(controller::refreshUniverseStatus);
        assertEquals("Real + 3 universe(s) active", universeStatusValueLabel.getText());
    }

    // ============================================================
    // Tooltip
    // ============================================================

    @Test
    @DisplayName("tooltip lists active universe names alphabetically")
    void tooltipListsActiveUniverseNames() throws Exception {
        when(universeDesignerService.findAllActive()).thenReturn(List.of(
                universe("u-z", "Zebra", true),
                universe("u-a", "Apple", true),
                universe("u-m", "Mango", true)));
        runOnFx(controller::refreshUniverseStatus);
        assertNotNull(universeStatusValueLabel.getTooltip(),
                "tooltip must be set when universes are active");
        String tooltipText = universeStatusValueLabel.getTooltip().getText();
        // Should be alphabetical: Apple, Mango, Zebra
        int appleIdx = tooltipText.indexOf("Apple");
        int mangoIdx = tooltipText.indexOf("Mango");
        int zebraIdx = tooltipText.indexOf("Zebra");
        assertTrue(appleIdx > -1 && mangoIdx > -1 && zebraIdx > -1,
                "all three universe names must appear in tooltip: " + tooltipText);
        assertTrue(appleIdx < mangoIdx && mangoIdx < zebraIdx,
                "tooltip must list universes alphabetically: " + tooltipText);
    }

    @Test
    @DisplayName("tooltip explains real-only mode when no universes active")
    void tooltipExplainsRealOnly() throws Exception {
        when(universeDesignerService.findAllActive()).thenReturn(List.of());
        runOnFx(controller::refreshUniverseStatus);
        assertNotNull(universeStatusValueLabel.getTooltip());
        String tooltipText = universeStatusValueLabel.getTooltip().getText();
        assertTrue(tooltipText.contains("No fictional universes active"),
                "tooltip must surface the real-only contract: " + tooltipText);
    }

    // ============================================================
    // Defensive null-handling
    // ============================================================

    @Test
    @DisplayName("refreshUniverseStatus is a no-op when service is null (test harness scenario)")
    void refreshNoopWhenServiceNull() throws Exception {
        setField("universeDesignerService", null);
        runOnFx(controller::refreshUniverseStatus);
        // Label text falls back to "Real only" defensively; no exception.
        assertEquals("Real only", universeStatusValueLabel.getText());
    }

    @Test
    @DisplayName("refreshUniverseStatus is a no-op when universeStatus label is null (FXML not loaded)")
    void refreshNoopWhenLabelNull() throws Exception {
        setField("universeStatus", null);
        // No NPE; method short-circuits.
        runOnFx(controller::refreshUniverseStatus);
    }
}
