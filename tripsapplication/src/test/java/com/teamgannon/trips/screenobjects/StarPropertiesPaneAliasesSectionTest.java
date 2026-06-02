package com.teamgannon.trips.screenobjects;

import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.service.StarService;
import com.teamgannon.trips.spaceshipmodeller.service.AliasDesignerService;
import com.teamgannon.trips.spaceshipmodeller.service.AliasDesignerService.AliasDisplay;
import com.teamgannon.trips.spaceshipmodeller.service.UniverseDesignerService;
import com.teamgannon.trips.worldbuilding.UniverseFilteringService;
import com.terranrepublic.assets.AliasTargetKind;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.scene.control.Label;
import net.rgielen.fxweaver.core.FxWeaver;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Phase F.2 §6.2 — covers the StarPropertiesPane Aliases section: FXML field is populated by
 * {@code setStar()}; broker subscription wired in constructor; refresh on
 * {@code UniverseActivationChangedEvent} updates the label for the currently-displayed star;
 * placeholder text shown when no active universes have aliased the star.
 *
 * <p>Uses the Platform.startup pattern + UniversesDialog-style FxApplicationThread runner
 * because StarPropertiesPane extends VBox and loads FXML at construction.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StarPropertiesPaneAliasesSectionTest {

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

    @Mock private StarService starService;
    @Mock private FxWeaver fxWeaver;
    @Mock private HostServices hostServices;
    @Mock private AliasDesignerService aliasService;
    @Mock private UniverseDesignerService universeService;

    private UniverseFilteringService filteringService;

    @BeforeEach
    void setUp() {
        Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");
        when(fxWeaver.getBean(HostServices.class)).thenReturn(hostServices);
        // Real filtering service so subscribeToFilterChanges semantics are exercised end-to-end.
        filteringService = new UniverseFilteringService(universeService);
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
        if (!latch.await(10, TimeUnit.SECONDS)) {
            throw new IllegalStateException("FX runnable did not complete in 10s");
        }
        if (err.get() != null) {
            if (err.get() instanceof RuntimeException re) throw re;
            throw new RuntimeException(err.get());
        }
    }

    private static StarObject star(String id, String name) {
        StarObject s = new StarObject();
        s.setId(id);
        s.setDisplayName(name);
        return s;
    }

    private static Label aliasesLabel(StarPropertiesPane pane) throws Exception {
        Field f = StarPropertiesPane.class.getDeclaredField("aliasesContentLabel");
        f.setAccessible(true);
        return (Label) f.get(pane);
    }

    @Test
    @DisplayName("setStar with no aliases: placeholder shown in aliases label")
    void setStarNoAliasesShowsPlaceholder() throws Exception {
        when(aliasService.findActiveAliasesForTooltip(any(), any())).thenReturn(List.of());
        runOnFx(() -> {
            StarPropertiesPane pane = new StarPropertiesPane(starService, fxWeaver, aliasService, filteringService);
            String uuid = "11111111-1111-1111-1111-111111111111";
            pane.setStar(star(uuid, "Sol"));
            try {
                assertEquals(StarPropertiesPane.EMPTY_ALIASES_PLACEHOLDER, aliasesLabel(pane).getText());
            } catch (Exception ex) { throw new RuntimeException(ex); }
            pane.disposeAliasSubscription();
        });
    }

    @Test
    @DisplayName("setStar with one alias: label shows '• alias (universe)'")
    void setStarOneAliasShowsBulletLine() throws Exception {
        when(aliasService.findActiveAliasesForTooltip(any(), any()))
                .thenReturn(List.of(new AliasDisplay("Vulcan", "Star Trek")));
        runOnFx(() -> {
            StarPropertiesPane pane = new StarPropertiesPane(starService, fxWeaver, aliasService, filteringService);
            String uuid = "22222222-2222-2222-2222-222222222222";
            pane.setStar(star(uuid, "40 Eri A"));
            try {
                assertEquals("• Vulcan (Star Trek)", aliasesLabel(pane).getText());
            } catch (Exception ex) { throw new RuntimeException(ex); }
            pane.disposeAliasSubscription();
        });
    }

    @Test
    @DisplayName("setStar with multiple aliases: label shows multi-line bullet list")
    void setStarMultipleAliasesShowsMultiLineList() throws Exception {
        when(aliasService.findActiveAliasesForTooltip(any(), any()))
                .thenReturn(List.of(
                        new AliasDisplay("Vulcan", "Star Trek"),
                        new AliasDisplay("Forty Eri Prime", "Children of the Pattern")));
        runOnFx(() -> {
            StarPropertiesPane pane = new StarPropertiesPane(starService, fxWeaver, aliasService, filteringService);
            String uuid = "33333333-3333-3333-3333-333333333333";
            pane.setStar(star(uuid, "40 Eri A"));
            try {
                assertEquals("• Vulcan (Star Trek)\n• Forty Eri Prime (Children of the Pattern)",
                        aliasesLabel(pane).getText());
            } catch (Exception ex) { throw new RuntimeException(ex); }
            pane.disposeAliasSubscription();
        });
    }

    @Test
    @DisplayName("setStar routes STAR kind + StarObject.id (UUID toString) — not name")
    void setStarRoutesStarKindWithUuidId() throws Exception {
        when(aliasService.findActiveAliasesForTooltip(any(), any())).thenReturn(List.of());
        runOnFx(() -> {
            StarPropertiesPane pane = new StarPropertiesPane(starService, fxWeaver, aliasService, filteringService);
            String uuid = "44444444-4444-4444-4444-444444444444";
            pane.setStar(star(uuid, "DisplayOnly"));
            pane.disposeAliasSubscription();
        });
        verify(aliasService).findActiveAliasesForTooltip(AliasTargetKind.STAR, "44444444-4444-4444-4444-444444444444");
        verify(aliasService, never()).findActiveAliasesForTooltip(AliasTargetKind.EXOPLANET, "44444444-4444-4444-4444-444444444444");
        verify(aliasService, never()).findActiveAliasesForTooltip(AliasTargetKind.STAR, "DisplayOnly");
    }

    @Test
    @DisplayName("null aliasService at construction: placeholder shown; no NPE")
    void nullAliasServiceShowsPlaceholder() throws Exception {
        runOnFx(() -> {
            StarPropertiesPane pane = new StarPropertiesPane(starService, fxWeaver, null, filteringService);
            String uuid = "55555555-5555-5555-5555-555555555555";
            pane.setStar(star(uuid, "Sol"));
            try {
                assertEquals(StarPropertiesPane.EMPTY_ALIASES_PLACEHOLDER, aliasesLabel(pane).getText());
            } catch (Exception ex) { throw new RuntimeException(ex); }
            pane.disposeAliasSubscription();
        });
    }

    @Test
    @DisplayName("null filteringService at construction: pane still works; no NPE on broker path")
    void nullFilteringServiceTolerated() throws Exception {
        when(aliasService.findActiveAliasesForTooltip(any(), any())).thenReturn(List.of());
        runOnFx(() -> {
            StarPropertiesPane pane = new StarPropertiesPane(starService, fxWeaver, aliasService, null);
            String uuid = "66666666-6666-6666-6666-666666666666";
            pane.setStar(star(uuid, "Sol"));
            // Dispose is safe even with no subscription
            pane.disposeAliasSubscription();
        });
    }

    @Test
    @DisplayName("clearData empties the aliases label to placeholder")
    void clearDataResetsAliasesLabel() throws Exception {
        when(aliasService.findActiveAliasesForTooltip(any(), any()))
                .thenReturn(List.of(new AliasDisplay("Vulcan", "Star Trek")));
        runOnFx(() -> {
            StarPropertiesPane pane = new StarPropertiesPane(starService, fxWeaver, aliasService, filteringService);
            String uuid = "77777777-7777-7777-7777-777777777777";
            pane.setStar(star(uuid, "40 Eri A"));
            try {
                assertEquals("• Vulcan (Star Trek)", aliasesLabel(pane).getText());
            } catch (Exception ex) { throw new RuntimeException(ex); }
            pane.clearData();
            try {
                assertEquals(StarPropertiesPane.EMPTY_ALIASES_PLACEHOLDER, aliasesLabel(pane).getText());
            } catch (Exception ex) { throw new RuntimeException(ex); }
            pane.disposeAliasSubscription();
        });
    }

    @Test
    @DisplayName("broker subscription wired: refresh fires when filter callbacks invoked")
    void brokerSubscriptionRefreshesOnActivationChange() throws Exception {
        // First call returns one alias; second call returns two — simulating an activation
        // change that brings a new universe's alias into view.
        when(aliasService.findActiveAliasesForTooltip(any(), any()))
                .thenReturn(List.of(new AliasDisplay("Vulcan", "Star Trek")))
                .thenReturn(List.of(
                        new AliasDisplay("Vulcan", "Star Trek"),
                        new AliasDisplay("Forty Eri Prime", "Children of the Pattern")));

        StarPropertiesPane[] paneHolder = new StarPropertiesPane[1];
        runOnFx(() -> {
            StarPropertiesPane pane = new StarPropertiesPane(starService, fxWeaver, aliasService, filteringService);
            String uuid = "88888888-8888-8888-8888-888888888888";
            pane.setStar(star(uuid, "40 Eri A"));
            try {
                assertEquals("• Vulcan (Star Trek)", aliasesLabel(pane).getText());
            } catch (Exception ex) { throw new RuntimeException(ex); }
            paneHolder[0] = pane;
        });
        // Trigger the broker — same path UniverseActivationChangedEvent would take.
        com.terranrepublic.assets.Universe trek = new com.terranrepublic.assets.Universe(
                "u-trek", "Star Trek", "", "", "1.0",
                com.terranrepublic.assets.UniverseLifecycle.AVAILABLE, true);
        filteringService.onUniverseActivationChanged(
                new com.teamgannon.trips.worldbuilding.UniverseActivationChangedEvent(trek, true));
        // Broker dispatches via FxThread.runOnFxThread — wait for that FX-thread work to drain.
        runOnFx(() -> {
            try {
                assertEquals("• Vulcan (Star Trek)\n• Forty Eri Prime (Children of the Pattern)",
                        aliasesLabel(paneHolder[0]).getText());
            } catch (Exception ex) { throw new RuntimeException(ex); }
            paneHolder[0].disposeAliasSubscription();
        });
    }

    @Test
    @DisplayName("disposeAliasSubscription unsubscribes the broker callback")
    void disposeUnsubscribes() throws Exception {
        when(aliasService.findActiveAliasesForTooltip(any(), any())).thenReturn(List.of());
        StarPropertiesPane[] paneHolder = new StarPropertiesPane[1];
        runOnFx(() -> {
            StarPropertiesPane pane = new StarPropertiesPane(starService, fxWeaver, aliasService, filteringService);
            String uuid = "99999999-9999-9999-9999-999999999999";
            pane.setStar(star(uuid, "Sol"));
            paneHolder[0] = pane;
            pane.disposeAliasSubscription();
        });
        // Inspect filteringService refresh callback list via reflection — must be empty.
        Field f = UniverseFilteringService.class.getDeclaredField("refreshCallbacks");
        f.setAccessible(true);
        java.util.List<?> callbacks = (java.util.List<?>) f.get(filteringService);
        assertTrue(callbacks.isEmpty(),
                "after dispose, the pane's callback should be removed; got size " + callbacks.size());
        assertNotNull(paneHolder[0]);
    }
}
