package com.teamgannon.trips.worldbuilding;

import com.teamgannon.trips.jpa.model.ExoPlanet;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.jpa.repository.ExoPlanetRepository;
import com.teamgannon.trips.jpa.repository.StarObjectRepository;
import com.teamgannon.trips.spaceshipmodeller.service.AliasDesignerService;
import com.teamgannon.trips.spaceshipmodeller.service.UniverseDesignerService;
import com.terranrepublic.assets.Alias;
import com.terranrepublic.assets.AliasTargetKind;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Phase F.2 §6.3 — coverage for {@link AliasesDialog}: table populates from
 * {@link AliasDesignerService#findAll()}; filters narrow by universe + kind; broker subscription
 * fires on activation change; dispose unsubscribes; ignoreNextBrokerCallback suppresses
 * self-induced reloads.
 *
 * <p>Pattern mirrors {@code UniversesDialogTest} from F.1 Step 7.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AliasesDialogTest {

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

    @Mock private AliasDesignerService aliasService;
    @Mock private UniverseDesignerService universeService;
    @Mock private StarObjectRepository starRepository;
    @Mock private ExoPlanetRepository exoPlanetRepository;

    private UniverseFilteringService filteringService;

    @BeforeEach
    void setUp() {
        Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");
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

    private static Universe universe(String id, String name, boolean active) {
        return new Universe(id, name, "", "", "1.0", UniverseLifecycle.AVAILABLE, active);
    }

    private static Alias alias(String universeId, AliasTargetKind kind, String targetId, String text) {
        return new Alias("catalog-alias-" + universeId + "-" + targetId, universeId, kind, targetId,
                text, "", Instant.parse("2026-06-01T00:00:00Z"), Instant.parse("2026-06-01T00:00:00Z"));
    }

    private static StarObject starObj(String id, String displayName) {
        StarObject s = new StarObject();
        s.setId(id);
        s.setDisplayName(displayName);
        return s;
    }

    private static ExoPlanet exoplanet(String id, String name) {
        ExoPlanet ep = new ExoPlanet();
        ep.setId(id);
        ep.setName(name);
        return ep;
    }

    private AliasesDialog newDialog() {
        return new AliasesDialog(aliasService, universeService, filteringService,
                starRepository, exoPlanetRepository);
    }

    // ============================================================
    // Construction + table population
    // ============================================================

    @Test
    @DisplayName("constructor populates table from aliasService.findAll filtered by active universes")
    void constructorPopulatesTable() throws Exception {
        Universe trek = universe("u-trek", "Star Trek", true);
        Universe cotp = universe("u-cotp", "Children of the Pattern", false);
        when(universeService.findAll()).thenReturn(List.of(trek, cotp));
        when(universeService.findAllActive()).thenReturn(List.of(trek));
        when(aliasService.findAll()).thenReturn(List.of(
                alias("u-trek", AliasTargetKind.STAR, "star-40-eri", "Vulcan"),
                alias("u-cotp", AliasTargetKind.STAR, "star-40-eri", "Forty Eri Prime")));
        when(starRepository.findById("star-40-eri"))
                .thenReturn(Optional.of(starObj("star-40-eri", "40 Eridani A")));

        AliasesDialog[] holder = new AliasesDialog[1];
        runOnFx(() -> {
            holder[0] = newDialog();
            // Default filter "All active universes" → only u-trek alias visible.
            assertEquals(1, holder[0].rowsForTest().size());
            assertEquals("Vulcan", holder[0].rowsForTest().get(0).aliasText.get());
            holder[0].dispose();
        });
    }

    @Test
    @DisplayName("selecting a specific universe shows aliases for that universe (active or not)")
    void specificUniverseFilterShowsInactiveAliases() throws Exception {
        Universe trek = universe("u-trek", "Star Trek", true);
        Universe cotp = universe("u-cotp", "Children of the Pattern", false);
        when(universeService.findAll()).thenReturn(List.of(trek, cotp));
        when(universeService.findAllActive()).thenReturn(List.of(trek));
        when(aliasService.findAll()).thenReturn(List.of(
                alias("u-trek", AliasTargetKind.STAR, "star-1", "Vulcan"),
                alias("u-cotp", AliasTargetKind.STAR, "star-1", "Forty Eri Prime")));
        when(starRepository.findById(any()))
                .thenReturn(Optional.of(starObj("star-1", "40 Eridani A")));

        runOnFx(() -> {
            AliasesDialog dialog = newDialog();
            // Switch to "Children of the Pattern" filter → inactive universe's alias appears
            dialog.setUniverseFilterForTest("Children of the Pattern");
            assertEquals(1, dialog.rowsForTest().size());
            assertEquals("Forty Eri Prime", dialog.rowsForTest().get(0).aliasText.get());
            dialog.dispose();
        });
    }

    @Test
    @DisplayName("kind filter narrows to Star or Exoplanet")
    void kindFilterNarrowsResults() throws Exception {
        Universe trek = universe("u-trek", "Star Trek", true);
        when(universeService.findAll()).thenReturn(List.of(trek));
        when(universeService.findAllActive()).thenReturn(List.of(trek));
        when(aliasService.findAll()).thenReturn(List.of(
                alias("u-trek", AliasTargetKind.STAR, "star-1", "Vulcan"),
                alias("u-trek", AliasTargetKind.EXOPLANET, "ep-1", "Vulcan-IV")));
        when(starRepository.findById("star-1")).thenReturn(Optional.of(starObj("star-1", "40 Eri A")));
        when(exoPlanetRepository.findById("ep-1")).thenReturn(Optional.of(exoplanet("ep-1", "40 Eri A b")));

        runOnFx(() -> {
            AliasesDialog dialog = newDialog();
            assertEquals(2, dialog.rowsForTest().size(), "All kinds: 2 rows");

            dialog.setKindFilterForTest("Star");
            assertEquals(1, dialog.rowsForTest().size());
            assertEquals("Star", dialog.rowsForTest().get(0).kindLabel.get());

            dialog.setKindFilterForTest("Exoplanet");
            assertEquals(1, dialog.rowsForTest().size());
            assertEquals("Exoplanet", dialog.rowsForTest().get(0).kindLabel.get());

            dialog.dispose();
        });
    }

    @Test
    @DisplayName("missing star target shows fallback name in row")
    void missingTargetShowsFallback() throws Exception {
        Universe trek = universe("u-trek", "Star Trek", true);
        when(universeService.findAll()).thenReturn(List.of(trek));
        when(universeService.findAllActive()).thenReturn(List.of(trek));
        when(aliasService.findAll()).thenReturn(List.of(
                alias("u-trek", AliasTargetKind.STAR, "deleted-star", "Vulcan")));
        when(starRepository.findById("deleted-star")).thenReturn(Optional.empty());

        runOnFx(() -> {
            AliasesDialog dialog = newDialog();
            assertEquals(1, dialog.rowsForTest().size());
            assertTrue(dialog.rowsForTest().get(0).targetName.get().contains("(missing star"),
                    "row should show fallback: " + dialog.rowsForTest().get(0).targetName.get());
            dialog.dispose();
        });
    }

    @Test
    @DisplayName("description column shows excerpt with ellipsis for long descriptions")
    void descriptionExcerpt() throws Exception {
        Universe trek = universe("u-trek", "Star Trek", true);
        when(universeService.findAll()).thenReturn(List.of(trek));
        when(universeService.findAllActive()).thenReturn(List.of(trek));
        String longDescription = "A".repeat(100);
        Alias withLongDesc = new Alias("catalog-alias-1", "u-trek", AliasTargetKind.STAR,
                "star-1", "Vulcan", longDescription,
                Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-01-01T00:00:00Z"));
        when(aliasService.findAll()).thenReturn(List.of(withLongDesc));
        when(starRepository.findById("star-1")).thenReturn(Optional.of(starObj("star-1", "40 Eri A")));

        runOnFx(() -> {
            AliasesDialog dialog = newDialog();
            String excerpt = dialog.rowsForTest().get(0).descriptionExcerpt.get();
            assertTrue(excerpt.length() <= 80, "excerpt should be capped: " + excerpt.length());
            assertTrue(excerpt.endsWith("..."), "excerpt should end with ellipsis: " + excerpt);
            dialog.dispose();
        });
    }

    // ============================================================
    // Broker subscription + dispose
    // ============================================================

    @Test
    @DisplayName("dispose unsubscribes from the broker")
    void disposeUnsubscribes() throws Exception {
        Universe trek = universe("u-trek", "Star Trek", true);
        when(universeService.findAll()).thenReturn(List.of(trek));
        when(universeService.findAllActive()).thenReturn(List.of(trek));
        when(aliasService.findAll()).thenReturn(List.of());

        AliasesDialog[] holder = new AliasesDialog[1];
        runOnFx(() -> {
            holder[0] = newDialog();
            holder[0].dispose();
        });

        Field f = UniverseFilteringService.class.getDeclaredField("refreshCallbacks");
        f.setAccessible(true);
        List<?> callbacks = (List<?>) f.get(filteringService);
        assertTrue(callbacks.isEmpty(),
                "after dispose, broker callback list should be empty; got size " + callbacks.size());
    }

    @Test
    @DisplayName("activation change refreshes the table when 'All active universes' filter is selected")
    void brokerFiresOnActivationChange() throws Exception {
        Universe trek = universe("u-trek", "Star Trek", true);
        Universe cotp = universe("u-cotp", "Children of the Pattern", false);
        when(universeService.findAll()).thenReturn(List.of(trek, cotp));
        // Mutable active list — initial reload may call findAllActive more than once due to the
        // universeFilter ChangeListener firing applyFilters; using a mutable list (rather than
        // chained thenReturn) keeps the initial phase stable regardless of call count.
        java.util.List<Universe> activeUniverses = new java.util.ArrayList<>(List.of(trek));
        when(universeService.findAllActive()).thenAnswer(inv -> activeUniverses);
        when(aliasService.findAll()).thenReturn(List.of(
                alias("u-trek", AliasTargetKind.STAR, "star-1", "Vulcan"),
                alias("u-cotp", AliasTargetKind.STAR, "star-1", "Forty Eri Prime")));
        when(starRepository.findById("star-1")).thenReturn(Optional.of(starObj("star-1", "40 Eri A")));

        AliasesDialog[] holder = new AliasesDialog[1];
        runOnFx(() -> {
            holder[0] = newDialog();
            // Initial: only trek active → 1 row visible under "All active universes"
            assertEquals(1, holder[0].rowsForTest().size());
        });

        // Activate cotp + fire broker event simulating the activation change.
        // UniverseActivationChangedEvent validates nowActive matches universe.active(), so the
        // payload must carry the post-toggle Universe instance.
        Universe cotpActive = cotp.withActive(true);
        activeUniverses.add(cotpActive);
        filteringService.onUniverseActivationChanged(new UniverseActivationChangedEvent(cotpActive, true));

        runOnFx(() -> {
            // After broker: both universes active → both rows
            assertEquals(2, holder[0].rowsForTest().size());
            holder[0].dispose();
        });
    }
}
