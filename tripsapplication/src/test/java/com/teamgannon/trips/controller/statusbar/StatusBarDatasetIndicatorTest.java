package com.teamgannon.trips.controller.statusbar;

import com.teamgannon.trips.events.SetContextDataSetEvent;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Status bar rationalization Step 2 — coverage for the Dataset indicator.
 *
 * <p>The Dataset indicator's label text + tooltip update on
 * {@link SetContextDataSetEvent}. Step 4 will add the {@code @EventListener(ApplicationReadyEvent.class)}
 * initial-state listener that reads from {@code TripsContext.getDataSetContext().getDescriptor()};
 * Step 2 ships just the event-listener path.
 */
class StatusBarDatasetIndicatorTest {

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
    private Label datasetStatus;

    @BeforeEach
    void setUp() throws Exception {
        Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");
        controller = new StatusBarController();
        datasetStatus = new Label("(none selected)");
        setField("datasetStatus", datasetStatus);
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

    private static DataSetDescriptor descriptor(String name, long starCount) {
        DataSetDescriptor d = new DataSetDescriptor();
        d.setDataSetName(name);
        d.setNumberStars(starCount);
        return d;
    }

    // ============================================================
    // Listener wiring
    // ============================================================

    @Test
    @DisplayName("SetContextDataSetEvent listener updates label text with the dataset name")
    void eventUpdatesLabelText() throws Exception {
        DataSetDescriptor d = descriptor("HYG-30ly", 12_847);
        runOnFx(() -> controller.onSetContextDataSetEvent(new SetContextDataSetEvent(this, d)));
        assertEquals("HYG-30ly", datasetStatus.getText());
    }

    @Test
    @DisplayName("SetContextDataSetEvent listener sets tooltip with name + star count")
    void eventSetsTooltipWithDetails() throws Exception {
        DataSetDescriptor d = descriptor("HYG-30ly", 12_847);
        runOnFx(() -> controller.onSetContextDataSetEvent(new SetContextDataSetEvent(this, d)));
        assertNotNull(datasetStatus.getTooltip());
        String tooltipText = datasetStatus.getTooltip().getText();
        assertTrue(tooltipText.contains("HYG-30ly"), "tooltip must include dataset name: " + tooltipText);
        assertTrue(tooltipText.contains("12,847"), "tooltip must include formatted star count: " + tooltipText);
    }

    @Test
    @DisplayName("descriptor with null name renders as '(unnamed)'")
    void nullDescriptorNameRendersUnnamed() throws Exception {
        DataSetDescriptor d = descriptor(null, 100);
        runOnFx(() -> controller.refreshDatasetStatus(d));
        assertEquals("(unnamed)", datasetStatus.getText());
    }

    @Test
    @DisplayName("descriptor with blank name renders as '(unnamed)'")
    void blankDescriptorNameRendersUnnamed() throws Exception {
        DataSetDescriptor d = descriptor("   ", 100);
        runOnFx(() -> controller.refreshDatasetStatus(d));
        assertEquals("(unnamed)", datasetStatus.getText());
    }

    // ============================================================
    // Null descriptor (no dataset selected)
    // ============================================================

    @Test
    @DisplayName("refreshDatasetStatus(null) shows '(none selected)' + appropriate tooltip")
    void nullDescriptorShowsNoneSelected() throws Exception {
        runOnFx(() -> controller.refreshDatasetStatus(null));
        assertEquals("(none selected)", datasetStatus.getText());
        assertNotNull(datasetStatus.getTooltip());
        assertTrue(datasetStatus.getTooltip().getText().contains("No dataset"));
    }

    // ============================================================
    // Multiple events
    // ============================================================

    @Test
    @DisplayName("subsequent events replace label + tooltip with new descriptor's details")
    void subsequentEventsReplace() throws Exception {
        runOnFx(() -> {
            controller.refreshDatasetStatus(descriptor("first-dataset", 1000));
            controller.refreshDatasetStatus(descriptor("second-dataset", 2000));
            controller.refreshDatasetStatus(descriptor("third-dataset", 3000));
        });
        assertEquals("third-dataset", datasetStatus.getText());
        assertTrue(datasetStatus.getTooltip().getText().contains("third-dataset"));
        assertTrue(datasetStatus.getTooltip().getText().contains("3,000"));
    }

    // ============================================================
    // Null-label safety
    // ============================================================

    @Test
    @DisplayName("refreshDatasetStatus is a no-op when datasetStatus Label is null")
    void noopWhenLabelNull() throws Exception {
        setField("datasetStatus", null);
        // No NPE — short-circuits.
        runOnFx(() -> controller.refreshDatasetStatus(descriptor("test", 100)));
    }
}
