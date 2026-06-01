package com.teamgannon.trips.worldbuilding;

import com.teamgannon.trips.spaceshipmodeller.service.UniverseDesignerService;
import com.terranrepublic.assets.Universe;
import com.terranrepublic.assets.UniverseLifecycle;
import javafx.application.Platform;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * v2 Phase F.1 Step 7 — coverage for {@link UniversesDialog}.
 *
 * <p>Construction populates the table from {@link UniverseDesignerService#findAll()}; checkbox
 * toggles call activate/deactivate; the broker subscription is established at construction time
 * and torn down by {@link UniversesDialog#dispose()}. Tests use the
 * {@link Platform#startup} pattern established in E.1 Step 9 dialog tests and the JumpPoint
 * dialog test, plus Mockito for the service.
 *
 * <p>The dialog is mocked-service-driven; live UI interactions (clicking the actual checkbox)
 * are exercised via the {@link UniversesDialog#toggleActivationForTest} seam that drives the
 * same code path the checkbox listener would.
 */
@ExtendWith(MockitoExtension.class)
class UniversesDialogTest {

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
    private UniverseDesignerService universeService;

    /**
     * Real {@link UniverseFilteringService} (not mocked) so the subscribe/unsubscribe semantics
     * are exercised end-to-end. Constructed once per test with the mocked UniverseDesignerService
     * inside it.
     */
    private UniverseFilteringService filteringService;

    @BeforeEach
    void setUp() {
        Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");
        filteringService = new UniverseFilteringService(universeService);
    }

    private static Universe universe(String id, String name, String version, String author,
                                     String description, UniverseLifecycle lifecycle, boolean active) {
        return new Universe(id, name, description, author, version, lifecycle, active);
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
    // Construction + rendering
    // ============================================================

    @Test
    @DisplayName("construction populates table with one row per universe from findAll()")
    void constructionPopulatesTable() throws Exception {
        when(universeService.findAll()).thenReturn(List.of(
                universe("u-a", "Universe A", "1.0", "Author A", "desc A", UniverseLifecycle.AVAILABLE, false),
                universe("u-b", "Universe B", "1.0", "Author B", "desc B", UniverseLifecycle.AVAILABLE, true)));
        runOnFx(() -> {
            UniversesDialog dialog = new UniversesDialog(universeService, filteringService);
            assertEquals(2, dialog.rowsForTest().size());
        });
    }

    @Test
    @DisplayName("table sorts active universes first, then alphabetically by name")
    void tableSortsActiveFirstThenAlpha() throws Exception {
        when(universeService.findAll()).thenReturn(List.of(
                universe("u-z", "Zebra", "1.0", "", "", UniverseLifecycle.AVAILABLE, false),
                universe("u-a", "Apple", "1.0", "", "", UniverseLifecycle.AVAILABLE, false),
                universe("u-m", "Mango", "1.0", "", "", UniverseLifecycle.AVAILABLE, true)));
        runOnFx(() -> {
            UniversesDialog dialog = new UniversesDialog(universeService, filteringService);
            // Expected order: Mango (active) first; then Apple, Zebra alphabetical.
            assertEquals("Mango", dialog.rowsForTest().get(0).universe.name());
            assertEquals("Apple", dialog.rowsForTest().get(1).universe.name());
            assertEquals("Zebra", dialog.rowsForTest().get(2).universe.name());
        });
    }

    @Test
    @DisplayName("empty universe list yields empty table without error")
    void emptyUniverseListYieldsEmptyTable() throws Exception {
        when(universeService.findAll()).thenReturn(List.of());
        runOnFx(() -> {
            UniversesDialog dialog = new UniversesDialog(universeService, filteringService);
            assertEquals(0, dialog.rowsForTest().size());
        });
    }

    // ============================================================
    // Activation
    // ============================================================

    @Test
    @DisplayName("toggleActivationForTest(id, true) calls universeService.activate(id)")
    void toggleTrueCallsActivate() throws Exception {
        Universe inactive = universe("u-test", "Test", "1.0", "", "", UniverseLifecycle.AVAILABLE, false);
        when(universeService.findAll()).thenReturn(List.of(inactive));
        when(universeService.activate("u-test")).thenReturn(inactive.withActive(true));
        runOnFx(() -> {
            UniversesDialog dialog = new UniversesDialog(universeService, filteringService);
            dialog.toggleActivationForTest("u-test", true);
        });
        verify(universeService).activate("u-test");
        verify(universeService, never()).deactivate(any());
    }

    @Test
    @DisplayName("toggleActivationForTest(id, false) calls universeService.deactivate(id)")
    void toggleFalseCallsDeactivate() throws Exception {
        Universe active = universe("u-test", "Test", "1.0", "", "", UniverseLifecycle.AVAILABLE, true);
        when(universeService.findAll()).thenReturn(List.of(active));
        when(universeService.deactivate("u-test")).thenReturn(active.withActive(false));
        runOnFx(() -> {
            UniversesDialog dialog = new UniversesDialog(universeService, filteringService);
            dialog.toggleActivationForTest("u-test", false);
        });
        verify(universeService).deactivate("u-test");
        verify(universeService, never()).activate(any());
    }

    @Test
    @DisplayName("activation failure rolls back checkbox UI by reloading from the service")
    void activationFailureRollsBackUI() throws Exception {
        Universe inactive = universe("u-test", "Test", "1.0", "", "", UniverseLifecycle.AVAILABLE, false);
        when(universeService.findAll()).thenReturn(List.of(inactive));
        when(universeService.activate("u-test")).thenThrow(new RuntimeException("DB unreachable"));
        runOnFx(() -> {
            UniversesDialog dialog = new UniversesDialog(universeService, filteringService);
            dialog.toggleActivationForTest("u-test", true);
            // After the failure, the dialog reloads from the service to restore the visible
            // state. The universe is still inactive in the (mocked) service so the row reflects
            // that — no exception leaked to the user.
            assertFalse(dialog.rowsForTest().get(0).universe.active());
        });
        verify(universeService).activate("u-test");
        // Initial reload + post-failure reload = 2 findAll() calls
        verify(universeService, org.mockito.Mockito.times(2)).findAll();
    }

    // ============================================================
    // Broker integration
    // ============================================================

    @Test
    @DisplayName("external activation change triggers dialog reload via the broker")
    void externalActivationTriggersReload() throws Exception {
        when(universeService.findAll()).thenReturn(List.of(
                universe("u-test", "Test", "1.0", "", "", UniverseLifecycle.AVAILABLE, false)));
        runOnFx(() -> {
            UniversesDialog dialog = new UniversesDialog(universeService, filteringService);
            // Initial load happened in constructor.
            // Simulate an external activation (e.g. another dialog instance, scripted activation):
            Universe active = universe("u-other", "Other", "1.0", "", "", UniverseLifecycle.AVAILABLE, true);
            filteringService.onUniverseActivationChanged(
                    new UniverseActivationChangedEvent(active, true));
        });
        // Drain FX queue so the broker's runLater dispatch completes.
        CountDownLatch flush = new CountDownLatch(1);
        Platform.runLater(flush::countDown);
        assertTrue(flush.await(2, TimeUnit.SECONDS));
        // Two findAll calls: constructor reload + external-event reload.
        verify(universeService, org.mockito.Mockito.atLeast(2)).findAll();
    }

    @Test
    @DisplayName("dispose() unsubscribes from the broker — subsequent events don't trigger reload")
    void disposeUnsubscribesFromBroker() throws Exception {
        when(universeService.findAll()).thenReturn(List.of(
                universe("u-test", "Test", "1.0", "", "", UniverseLifecycle.AVAILABLE, false)));
        AtomicReference<UniversesDialog> ref = new AtomicReference<>();
        runOnFx(() -> ref.set(new UniversesDialog(universeService, filteringService)));
        // Reset the counter we care about — only count findAll calls AFTER dispose.
        org.mockito.Mockito.clearInvocations(universeService);

        runOnFx(() -> ref.get().dispose());

        // Fire an event post-dispose; the unsubscribed dialog should NOT reload.
        Universe other = universe("u-other", "Other", "1.0", "", "", UniverseLifecycle.AVAILABLE, true);
        filteringService.onUniverseActivationChanged(
                new UniverseActivationChangedEvent(other, true));
        // Drain FX queue
        CountDownLatch flush = new CountDownLatch(1);
        Platform.runLater(flush::countDown);
        assertTrue(flush.await(2, TimeUnit.SECONDS));

        verify(universeService, never()).findAll();
    }

    @Test
    @DisplayName("self-induced activation does NOT trigger redundant reload (ignoreNextBrokerCallback)")
    void selfInducedActivationSkipsReload() throws Exception {
        Universe inactive = universe("u-test", "Test", "1.0", "", "", UniverseLifecycle.AVAILABLE, false);
        when(universeService.findAll()).thenReturn(List.of(inactive));
        when(universeService.activate("u-test"))
                .thenAnswer(inv -> {
                    Universe activated = inactive.withActive(true);
                    // Simulate what the real service does: publish the event via the broker.
                    filteringService.onUniverseActivationChanged(
                            new UniverseActivationChangedEvent(activated, true));
                    return activated;
                });
        runOnFx(() -> {
            UniversesDialog dialog = new UniversesDialog(universeService, filteringService);
            org.mockito.Mockito.clearInvocations(universeService);
            dialog.toggleActivationForTest("u-test", true);
        });
        // Drain FX queue so any runLater-dispatched callback runs.
        CountDownLatch flush = new CountDownLatch(1);
        Platform.runLater(flush::countDown);
        assertTrue(flush.await(2, TimeUnit.SECONDS));
        // The service.activate() was called once. The broker callback fired (event published),
        // but the dialog's ignoreNextBrokerCallback flag short-circuited the reload —
        // so findAll() should NOT have been invoked again after the toggle.
        verify(universeService, never()).findAll();
        verify(universeService).activate("u-test");
    }
}
