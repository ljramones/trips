package com.teamgannon.trips.graphics.panes;

import com.teamgannon.trips.config.application.ScreenSize;
import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.model.SolarSystemDescription;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.SolarSystemService;
import com.teamgannon.trips.solarsystem.SolarSystemContextMenuFactory;
import com.teamgannon.trips.test.TestFXBase;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.testfx.util.WaitForAsyncUtils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Smoke coverage for the Phase 2.1 async solar-system load path. The test keeps
 * the service call blocked long enough to fail if {@code setSystemToDisplay}
 * ever goes back to doing repository-backed work on the JavaFX thread.
 */
class SolarSystemSpacePaneAsyncLoadTest extends TestFXBase {

    private SolarSystemSpacePane pane;
    private SolarSystemService solarSystemService;

    @Override
    public void start(Stage stage) {
        TripsContext tripsContext = mock(TripsContext.class);
        when(tripsContext.getScreenSize()).thenReturn(ScreenSize.builder()
                .sceneWidth(800)
                .sceneHeight(600)
                .depth(1000)
                .spacing(10)
                .build());

        solarSystemService = mock(SolarSystemService.class);
        pane = new SolarSystemSpacePane(
                tripsContext,
                mock(ApplicationEventPublisher.class),
                mock(DatabaseManagementService.class),
                solarSystemService,
                new SolarSystemContextMenuFactory(),
                null);  // F.2: aliasService — null disables alias tooltip lines (test context)

        stage.setScene(new Scene(pane, 800, 600));
        stage.show();
    }

    @Test
    @DisplayName("setSystemToDisplay returns before the repository-backed solar-system load completes")
    void setSystemToDisplayLoadsSolarSystemOffFxThread() throws Exception {
        StarDisplayRecord star = star("Async Sol");
        SolarSystemDescription renderedSystem = solarSystem(star);
        CountDownLatch serviceEntered = new CountDownLatch(1);
        CountDownLatch allowServiceToReturn = new CountDownLatch(1);

        when(solarSystemService.getSolarSystem(star)).thenAnswer(invocation -> {
            serviceEntered.countDown();
            allowServiceToReturn.await(2, TimeUnit.SECONDS);
            return renderedSystem;
        });

        long startedNanos = System.nanoTime();
        interact(() -> pane.setSystemToDisplay(star));
        long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedNanos);

        assertTrue(elapsedMillis < 500,
                "setSystemToDisplay should return quickly instead of blocking the FX thread");
        assertTrue(serviceEntered.await(1, TimeUnit.SECONDS),
                "background load task should call SolarSystemService");
        assertNull(pane.getCurrentSystem(),
                "system should not render until the background load succeeds");

        allowServiceToReturn.countDown();

        WaitForAsyncUtils.waitFor(2, TimeUnit.SECONDS, () -> pane.getCurrentSystem() == renderedSystem);
        assertSame(renderedSystem, pane.getCurrentSystem());
        verify(solarSystemService).getSolarSystem(star);
    }

    private static StarDisplayRecord star(String name) {
        StarDisplayRecord star = new StarDisplayRecord();
        star.setStarName(name);
        star.setSpectralClass("G2V");
        star.setMass(1.0);
        star.setRadius(1.0);
        star.setTemperature(5778);
        return star;
    }

    private static SolarSystemDescription solarSystem(StarDisplayRecord star) {
        SolarSystemDescription description = new SolarSystemDescription();
        description.setSolarSystemId("async-sol");
        description.setStarDisplayRecord(star);
        description.setHabitableZoneInnerAU(0.95);
        description.setHabitableZoneOuterAU(1.37);
        return description;
    }
}
