package com.teamgannon.trips.worldbuilding;

import com.teamgannon.trips.spaceshipmodeller.service.UniverseDesignerService;
import com.terranrepublic.assets.Cataloged;
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
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * v2 Phase F.1 §5.3 — coverage for {@link UniverseFilteringService}'s three responsibilities:
 * isVisible (per-entry), filter (bulk), subscribe/notify broker.
 *
 * <p>Mocked {@link UniverseDesignerService} so the test exercises the filter logic without
 * spinning up Spring + JPA. The broker tests boot the JavaFX toolkit because the subscription
 * dispatch path uses {@code FxThread.runOnFxThread}.
 */
@ExtendWith(MockitoExtension.class)
class UniverseFilteringServiceTest {

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

    private UniverseFilteringService filteringService;

    @BeforeEach
    void setUp() {
        filteringService = new UniverseFilteringService(universeService);
    }

    private static Universe universe(String id, boolean active) {
        return new Universe(id, "Test", "", "", "1.0", UniverseLifecycle.AVAILABLE, active);
    }

    /** Minimal Cataloged for filter testing — only needs to return a universeId. */
    private static Cataloged catalogEntry(String name, String universeId) {
        return new Cataloged() {
            @Override public String id() { return name; }
            @Override public String name() { return name; }
            @Override public String source() { return ""; }
            @Override public String faction() { return ""; }
            @Override public boolean concealed() { return false; }
            @Override public String description() { return ""; }
            @Override public String universeId() { return universeId; }
        };
    }

    // ============================================================
    // isVisible — per-entry
    // ============================================================

    @Test
    @DisplayName("isVisible returns true for null universeId (canonical/real per R1.9 + R5.6)")
    void isVisibleTrueForNullUniverseId() {
        Cataloged real = catalogEntry("ISS", null);
        assertTrue(filteringService.isVisible(real),
                "Real-data entries (universe_id=NULL) must always be visible");
    }

    @Test
    @DisplayName("isVisible returns true for entry referencing an active universe")
    void isVisibleTrueForActiveUniverse() {
        when(universeService.findById("u-active"))
                .thenReturn(Optional.of(universe("u-active", true)));
        assertTrue(filteringService.isVisible(catalogEntry("Troy", "u-active")));
    }

    @Test
    @DisplayName("isVisible returns false for entry referencing an inactive universe")
    void isVisibleFalseForInactiveUniverse() {
        when(universeService.findById("u-inactive"))
                .thenReturn(Optional.of(universe("u-inactive", false)));
        assertFalse(filteringService.isVisible(catalogEntry("Hkh'Rkh Ship", "u-inactive")),
                "Universe-scoped entries from inactive universes must be hidden");
    }

    @Test
    @DisplayName("isVisible returns false for entry referencing unknown universe (defensive)")
    void isVisibleFalseForUnknownUniverse() {
        when(universeService.findById("u-phantom")).thenReturn(Optional.empty());
        assertFalse(filteringService.isVisible(catalogEntry("Orphan", "u-phantom")),
                "Dangling universe_id references must default to hidden, not visible");
    }

    // ============================================================
    // filter — bulk
    // ============================================================

    @Test
    @DisplayName("filter passes through real-data entries when no universes are active")
    void filterPassesRealDataWhenNoneActive() {
        when(universeService.findAllActive()).thenReturn(List.of());
        List<Cataloged> input = List.of(
                catalogEntry("ISS", null),
                catalogEntry("Tiangong", null),
                catalogEntry("Hkh'Rkh", "u-caine-riordan"));
        List<Cataloged> visible = filteringService.filter(input);
        assertEquals(2, visible.size());
        assertEquals("ISS", visible.get(0).name());
        assertEquals("Tiangong", visible.get(1).name());
    }

    @Test
    @DisplayName("filter passes through real + active-universe entries when one universe active")
    void filterPassesMixedWhenOneActive() {
        when(universeService.findAllActive())
                .thenReturn(List.of(universe("u-legacy", true)));
        List<Cataloged> input = List.of(
                catalogEntry("ISS", null),
                catalogEntry("Troy", "u-legacy"),
                catalogEntry("Hkh'Rkh", "u-caine-riordan"));
        List<Cataloged> visible = filteringService.filter(input);
        assertEquals(2, visible.size());
        assertEquals("ISS", visible.get(0).name());
        assertEquals("Troy", visible.get(1).name());
    }

    // ============================================================
    // getActiveUniverseNamesById — F.2 §6.1
    // ============================================================

    @Test
    @DisplayName("getActiveUniverseNamesById returns empty map when no universes active")
    void activeNamesEmptyWhenNoneActive() {
        when(universeService.findAllActive()).thenReturn(List.of());
        assertTrue(filteringService.getActiveUniverseNamesById().isEmpty());
    }

    @Test
    @DisplayName("getActiveUniverseNamesById keys by id with name as value")
    void activeNamesKeyedById() {
        Universe trek = new Universe("u-trek", "Star Trek", "", "", "1.0", UniverseLifecycle.AVAILABLE, true);
        Universe cotp = new Universe("u-cotp", "Children of the Pattern", "", "", "1.0", UniverseLifecycle.AVAILABLE, true);
        when(universeService.findAllActive()).thenReturn(List.of(trek, cotp));

        java.util.Map<String, String> result = filteringService.getActiveUniverseNamesById();

        assertEquals(2, result.size());
        assertEquals("Star Trek", result.get("u-trek"));
        assertEquals("Children of the Pattern", result.get("u-cotp"));
    }

    @Test
    @DisplayName("getActiveUniverseNamesById skips inactive universes (findAllActive contract)")
    void activeNamesSkipsInactive() {
        // findAllActive is contractually filtered upstream; the service trusts that filter.
        // This test documents that contract — only the active ones come through.
        when(universeService.findAllActive())
                .thenReturn(List.of(universe("u-active", true)));
        // override the name for clarity
        Universe withName = new Universe("u-active", "ActiveOne", "", "", "1.0", UniverseLifecycle.AVAILABLE, true);
        when(universeService.findAllActive()).thenReturn(List.of(withName));

        java.util.Map<String, String> result = filteringService.getActiveUniverseNamesById();
        assertEquals(1, result.size());
        assertEquals("ActiveOne", result.get("u-active"));
    }

    @Test
    @DisplayName("filter preserves source order within the visible subset")
    void filterPreservesOrder() {
        when(universeService.findAllActive())
                .thenReturn(List.of(universe("u-a", true), universe("u-b", true)));
        List<Cataloged> input = List.of(
                catalogEntry("entry-1", "u-a"),
                catalogEntry("entry-2", null),
                catalogEntry("entry-3", "u-b"),
                catalogEntry("entry-4", "u-inactive"),
                catalogEntry("entry-5", null));
        List<Cataloged> visible = filteringService.filter(input);
        assertEquals(4, visible.size());
        assertEquals("entry-1", visible.get(0).name());
        assertEquals("entry-2", visible.get(1).name());
        assertEquals("entry-3", visible.get(2).name());
        assertEquals("entry-5", visible.get(3).name());
    }

    @Test
    @DisplayName("filter on empty input returns empty list")
    void filterEmptyInputReturnsEmpty() {
        when(universeService.findAllActive()).thenReturn(List.of());
        assertEquals(List.of(), filteringService.filter(List.of()));
    }

    @Test
    @DisplayName("filter on all-real input returns the input unchanged in content")
    void filterAllRealReturnsAll() {
        when(universeService.findAllActive()).thenReturn(List.of());
        List<Cataloged> input = List.of(
                catalogEntry("ISS", null),
                catalogEntry("Tiangong", null),
                catalogEntry("Mir", null));
        List<Cataloged> visible = filteringService.filter(input);
        assertEquals(3, visible.size());
        for (int i = 0; i < input.size(); i++) {
            assertSame(input.get(i), visible.get(i), "real entries pass through identity-preserved");
        }
    }

    // ============================================================
    // subscribe / notify broker
    // ============================================================

    @Test
    @DisplayName("subscribed callback fires when UniverseActivationChangedEvent arrives")
    void subscriberFiresOnEvent() throws Exception {
        Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger fired = new AtomicInteger();
        filteringService.subscribeToFilterChanges(() -> {
            fired.incrementAndGet();
            latch.countDown();
        });
        Universe u = universe("u-test", true);
        filteringService.onUniverseActivationChanged(new UniverseActivationChangedEvent(u, true));
        assertTrue(latch.await(2, TimeUnit.SECONDS), "callback must fire within 2s");
        assertEquals(1, fired.get());
    }

    @Test
    @DisplayName("multiple subscribers all fire on a single event")
    void multipleSubscribersAllFire() throws Exception {
        Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");
        CountDownLatch latch = new CountDownLatch(3);
        filteringService.subscribeToFilterChanges(latch::countDown);
        filteringService.subscribeToFilterChanges(latch::countDown);
        filteringService.subscribeToFilterChanges(latch::countDown);
        Universe u = universe("u-test", false);
        filteringService.onUniverseActivationChanged(new UniverseActivationChangedEvent(u, false));
        assertTrue(latch.await(2, TimeUnit.SECONDS), "all 3 callbacks must fire within 2s");
    }

    @Test
    @DisplayName("unsubscribe stops the callback from firing on subsequent events")
    void unsubscribeStopsCallback() throws Exception {
        Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");
        AtomicInteger fired = new AtomicInteger();
        Runnable unsubscribe = filteringService.subscribeToFilterChanges(fired::incrementAndGet);

        // First event: subscriber fires
        Universe u = universe("u-test", true);
        UniverseActivationChangedEvent event1 = new UniverseActivationChangedEvent(u, true);
        filteringService.onUniverseActivationChanged(event1);
        // Drain pending FX runLater
        CountDownLatch flush1 = new CountDownLatch(1);
        Platform.runLater(flush1::countDown);
        assertTrue(flush1.await(2, TimeUnit.SECONDS));
        assertEquals(1, fired.get());

        // Unsubscribe
        unsubscribe.run();

        // Second event: subscriber should NOT fire
        Universe u2 = universe("u-test", false);
        filteringService.onUniverseActivationChanged(new UniverseActivationChangedEvent(u2, false));
        CountDownLatch flush2 = new CountDownLatch(1);
        Platform.runLater(flush2::countDown);
        assertTrue(flush2.await(2, TimeUnit.SECONDS));
        assertEquals(1, fired.get(), "post-unsubscribe event must not trigger the callback");
    }

    @Test
    @DisplayName("exception in one subscriber doesn't prevent others from firing")
    void exceptionInSubscriberDoesntBlockOthers() throws Exception {
        Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");
        CountDownLatch successLatch = new CountDownLatch(2);
        filteringService.subscribeToFilterChanges(() -> { throw new RuntimeException("kaboom"); });
        filteringService.subscribeToFilterChanges(successLatch::countDown);
        filteringService.subscribeToFilterChanges(successLatch::countDown);
        Universe u = universe("u-test", true);
        filteringService.onUniverseActivationChanged(new UniverseActivationChangedEvent(u, true));
        assertTrue(successLatch.await(2, TimeUnit.SECONDS),
                "the 2 working subscribers must still fire despite the 1st throwing");
    }
}
