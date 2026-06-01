package com.teamgannon.trips.controller.statusbar;

import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.config.application.model.DataSetContext;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.spaceshipmodeller.service.UniverseDesignerService;
import com.terranrepublic.assets.Universe;
import com.terranrepublic.assets.UniverseLifecycle;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Status bar rationalization Step 4 — coverage for the uniform
 * {@code @EventListener(ApplicationReadyEvent.class)} initial-state refresh for all three
 * persistent indicators.
 *
 * <p>F.1 Step 8 wired this for the Worldbuilding indicator only. Step 4 extends to Dataset +
 * Routing so persisted state surfaces on boot rather than only on the first user-driven event.
 */
@ExtendWith(MockitoExtension.class)
class StatusBarApplicationReadyTest {

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
    private TripsContext tripsContext;
    @Mock
    private UniverseDesignerService universeDesignerService;
    @Mock
    private ApplicationReadyEvent applicationReadyEvent;

    private StatusBarController controller;
    private Label actionSlot;
    private Label datasetStatus;
    private Label routingStatus;
    private Label universeStatus;

    @BeforeEach
    void setUp() throws Exception {
        Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");
        controller = new StatusBarController();
        actionSlot = new Label("");
        datasetStatus = new Label("(none selected)");
        routingStatus = new Label("Inactive");
        universeStatus = new Label("Real only");
        setField("actionSlot", actionSlot);
        setField("datasetStatus", datasetStatus);
        setField("routingStatus", routingStatus);
        setField("universeStatus", universeStatus);
        setField("tripsContext", tripsContext);
        setField("universeDesignerService", universeDesignerService);
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
    // Dataset boot-time read
    // ============================================================

    @Test
    @DisplayName("ApplicationReadyEvent populates Dataset from TripsContext.getDataSetContext().getDescriptor()")
    void datasetPopulatesFromTripsContext() throws Exception {
        DataSetDescriptor descriptor = new DataSetDescriptor();
        descriptor.setDataSetName("HYG-30ly");
        descriptor.setNumberStars(12_847L);

        DataSetContext datasetCtx = new DataSetContext(descriptor);
        when(tripsContext.getDataSetContext()).thenReturn(datasetCtx);
        lenient().when(universeDesignerService.findAllActive()).thenReturn(List.of());

        runOnFx(() -> controller.onApplicationReady(applicationReadyEvent));

        assertEquals("HYG-30ly", datasetStatus.getText());
    }

    @Test
    @DisplayName("null TripsContext on ApplicationReady resolves to '(none selected)' — test-harness safety")
    void nullTripsContextSafe() throws Exception {
        setField("tripsContext", null);
        lenient().when(universeDesignerService.findAllActive()).thenReturn(List.of());

        runOnFx(() -> controller.onApplicationReady(applicationReadyEvent));

        assertEquals("(none selected)", datasetStatus.getText());
    }

    @Test
    @DisplayName("TripsContext with null DataSetContext resolves to '(none selected)'")
    void nullDataSetContextSafe() throws Exception {
        when(tripsContext.getDataSetContext()).thenReturn(null);
        lenient().when(universeDesignerService.findAllActive()).thenReturn(List.of());

        runOnFx(() -> controller.onApplicationReady(applicationReadyEvent));

        assertEquals("(none selected)", datasetStatus.getText());
    }

    @Test
    @DisplayName("TripsContext with DataSetContext but null descriptor resolves to '(none selected)'")
    void nullDescriptorSafe() throws Exception {
        DataSetContext datasetCtx = new DataSetContext(null);
        // The DataSetContext constructor sets descriptor + validDescriptor=true; manually
        // null it back out to simulate a context that's never had a real descriptor.
        Field f = DataSetContext.class.getDeclaredField("descriptor");
        f.setAccessible(true);
        f.set(datasetCtx, null);

        when(tripsContext.getDataSetContext()).thenReturn(datasetCtx);
        lenient().when(universeDesignerService.findAllActive()).thenReturn(List.of());

        runOnFx(() -> controller.onApplicationReady(applicationReadyEvent));

        assertEquals("(none selected)", datasetStatus.getText());
    }

    // ============================================================
    // Routing boot-time default
    // ============================================================

    @Test
    @DisplayName("ApplicationReadyEvent sets Routing to Inactive (no persisted state today)")
    void routingDefaultsInactive() throws Exception {
        // Pre-state: simulate routing was somehow flipped active in the in-memory label.
        runOnFx(() -> controller.routingStatus(true));
        assertEquals("Active", routingStatus.getText()); // sanity

        DataSetContext datasetCtx = new DataSetContext(null);
        when(tripsContext.getDataSetContext()).thenReturn(datasetCtx);
        lenient().when(universeDesignerService.findAllActive()).thenReturn(List.of());

        runOnFx(() -> controller.onApplicationReady(applicationReadyEvent));

        assertEquals("Inactive", routingStatus.getText(),
                "Routing must reset to Inactive on ApplicationReadyEvent (no persisted state)");
        assertEquals(Color.SEAGREEN, routingStatus.getTextFill());
    }

    // ============================================================
    // Worldbuilding boot-time refresh (F.1 Step 8 wiring still works)
    // ============================================================

    @Test
    @DisplayName("ApplicationReadyEvent refreshes Worldbuilding indicator from UniverseDesignerService (F.1 regression)")
    void universeRefreshes() throws Exception {
        DataSetContext datasetCtx = new DataSetContext(null);
        when(tripsContext.getDataSetContext()).thenReturn(datasetCtx);
        when(universeDesignerService.findAllActive()).thenReturn(List.of(
                new Universe("u-1", "Test Universe", "", "", "1.0", UniverseLifecycle.AVAILABLE, true)));

        runOnFx(() -> controller.onApplicationReady(applicationReadyEvent));

        assertEquals("Real + 1 universe(s) active", universeStatus.getText(),
                "F.1 Step 8's Worldbuilding indicator wiring must still run on ApplicationReadyEvent");
    }

    // ============================================================
    // All three indicators refreshed in a single ApplicationReadyEvent
    // ============================================================

    @Test
    @DisplayName("a single ApplicationReadyEvent refreshes all three persistent indicators")
    void singleEventRefreshesAllThree() throws Exception {
        DataSetDescriptor descriptor = new DataSetDescriptor();
        descriptor.setDataSetName("UNIQUE-DATASET-NAME");
        DataSetContext datasetCtx = new DataSetContext(descriptor);
        when(tripsContext.getDataSetContext()).thenReturn(datasetCtx);
        when(universeDesignerService.findAllActive()).thenReturn(List.of(
                new Universe("u-1", "Some Universe", "", "", "1.0", UniverseLifecycle.AVAILABLE, true)));

        // Pre-state to verify all three get touched (not just one):
        runOnFx(() -> controller.routingStatus(true));

        runOnFx(() -> controller.onApplicationReady(applicationReadyEvent));

        assertEquals("UNIQUE-DATASET-NAME", datasetStatus.getText(),
                "Dataset indicator must be refreshed");
        assertEquals("Inactive", routingStatus.getText(),
                "Routing indicator must be refreshed to default");
        assertEquals("Real + 1 universe(s) active", universeStatus.getText(),
                "Worldbuilding indicator must be refreshed");
    }
}
