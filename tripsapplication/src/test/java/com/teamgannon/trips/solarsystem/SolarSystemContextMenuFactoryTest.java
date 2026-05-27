package com.teamgannon.trips.solarsystem;

import com.teamgannon.trips.planetary.modelling.PlanetDescription;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Tests for the "Plan Transfer from Here..." wiring added to {@link SolarSystemContextMenuFactory}.
 * Requires the JavaFX toolkit; skips gracefully if it is unavailable.
 */
class SolarSystemContextMenuFactoryTest {

    private static final String PLAN_TRANSFER = "Plan Transfer from Here...";
    private static boolean fxAvailable = false;

    @BeforeAll
    static void initFx() {
        try {
            Platform.startup(() -> {
            });
            fxAvailable = true;
        } catch (IllegalStateException alreadyStarted) {
            fxAvailable = true;
        } catch (Throwable t) {
            fxAvailable = false;
        }
    }

    private final SolarSystemContextMenuFactory factory = new SolarSystemContextMenuFactory();

    @Test
    @DisplayName("planet menu offers Plan Transfer and fires the callback with the clicked body")
    void planetMenuHasPlanTransferItem() throws InterruptedException {
        assumeTrue(fxAvailable, "JavaFX toolkit not available");

        PlanetDescription planet = new PlanetDescription();
        planet.setName("Terra");
        planet.setSemiMajorAxis(1.0);

        AtomicReference<PlanetDescription> captured = new AtomicReference<>();
        ContextMenu menu = buildMenu(planet, captured::set);

        MenuItem item = findItem(menu);
        assertNotNull(item, "planet menu should contain a Plan Transfer item");
        assertFalse(item.isDisable());

        item.getOnAction().handle(new ActionEvent());
        assertEquals("Terra", captured.get().getName());
    }

    @Test
    @DisplayName("with no callback, the Plan Transfer item is disabled")
    void planTransferDisabledWithoutCallback() throws InterruptedException {
        assumeTrue(fxAvailable, "JavaFX toolkit not available");

        PlanetDescription planet = new PlanetDescription();
        planet.setName("Terra");

        ContextMenu menu = buildMenu(planet, null);
        MenuItem item = findItem(menu);
        assertNotNull(item);
        assertTrue(item.isDisable());
    }

    private ContextMenu buildMenu(PlanetDescription planet,
                                  java.util.function.Consumer<PlanetDescription> onPlanTransfer)
            throws InterruptedException {
        AtomicReference<ContextMenu> ref = new AtomicReference<>();
        runOnFx(() -> ref.set(factory.createPlanetContextMenu(
                planet, null, List.of(),
                r -> {
                }, r -> {
                }, r -> {
                }, r -> {
                },
                onPlanTransfer)));
        return ref.get();
    }

    private static MenuItem findItem(ContextMenu menu) {
        return menu.getItems().stream()
                .filter(mi -> PLAN_TRANSFER.equals(mi.getText()))
                .findFirst().orElse(null);
    }

    private static void runOnFx(Runnable r) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                r.run();
            } finally {
                latch.countDown();
            }
        });
        if (!latch.await(5, TimeUnit.SECONDS)) {
            throw new IllegalStateException("FX task timed out");
        }
    }
}
