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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Phase F.2 §6.4 — coverage for {@link AliasEditorDialog}: create + edit paths, default
 * Universe selection when one is active, validation errors, save invocation with correct
 * fields, duplicate-error path, edit-mode field locking.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AliasEditorDialogTest {

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

    @BeforeEach
    void setUp() {
        Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");
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

    private AliasEditorDialog newCreate() {
        return new AliasEditorDialog(aliasService, universeService, starRepository, exoPlanetRepository, null);
    }

    private AliasEditorDialog newEdit(Alias existing) {
        return new AliasEditorDialog(aliasService, universeService, starRepository, exoPlanetRepository, existing);
    }

    // ============================================================
    // Create path
    // ============================================================

    @Test
    @DisplayName("create mode: title is 'Create Alias'; STAR radio selected by default")
    void createModeDefaults() throws Exception {
        when(universeService.findAll()).thenReturn(List.of(universe("u-trek", "Star Trek", true)));
        when(universeService.findAllActive()).thenReturn(List.of(universe("u-trek", "Star Trek", true)));
        when(starRepository.findAll()).thenReturn(List.of(starObj("s1", "Sol")));
        runOnFx(() -> {
            AliasEditorDialog dialog = newCreate();
            assertEquals("Create Alias", dialog.getTitle());
            assertTrue(dialog.starRadioForTest().isSelected());
            assertFalse(dialog.exoplanetRadioForTest().isSelected());
        });
    }

    @Test
    @DisplayName("create mode: single active universe pre-fills Universe dropdown")
    void createSingleActiveUniversePreFills() throws Exception {
        Universe trek = universe("u-trek", "Star Trek", true);
        when(universeService.findAll()).thenReturn(List.of(trek));
        when(universeService.findAllActive()).thenReturn(List.of(trek));
        when(starRepository.findAll()).thenReturn(List.of());
        runOnFx(() -> {
            AliasEditorDialog dialog = newCreate();
            assertEquals("Star Trek", dialog.universeComboForTest().getValue());
        });
    }

    @Test
    @DisplayName("create mode: multiple active universes → no default selection")
    void createMultipleActiveUniversesNoDefault() throws Exception {
        Universe trek = universe("u-trek", "Star Trek", true);
        Universe cotp = universe("u-cotp", "Children of the Pattern", true);
        when(universeService.findAll()).thenReturn(List.of(trek, cotp));
        when(universeService.findAllActive()).thenReturn(List.of(trek, cotp));
        when(starRepository.findAll()).thenReturn(List.of());
        runOnFx(() -> {
            AliasEditorDialog dialog = newCreate();
            assertNull(dialog.universeComboForTest().getValue(),
                    "no default when multiple universes active");
        });
    }

    @Test
    @DisplayName("attemptSave: missing Universe → error displayed, save NOT called")
    void saveMissingUniverseShowsError() throws Exception {
        Universe trek = universe("u-trek", "Star Trek", true);
        Universe cotp = universe("u-cotp", "Children of the Pattern", true);
        when(universeService.findAll()).thenReturn(List.of(trek, cotp));
        when(universeService.findAllActive()).thenReturn(List.of(trek, cotp));
        when(starRepository.findAll()).thenReturn(List.of(starObj("s1", "Sol")));
        runOnFx(() -> {
            AliasEditorDialog dialog = newCreate();
            // Universe combo has no default in this scenario.
            dialog.aliasTextFieldForTest().setText("Vulcan");
            dialog.targetComboForTest().setValue("Sol");
            boolean ok = dialog.attemptSave();
            assertFalse(ok);
            assertTrue(dialog.errorLabelForTest().getText().contains("Universe"),
                    "error should mention Universe: " + dialog.errorLabelForTest().getText());
            verify(aliasService, never()).save(any());
        });
    }

    @Test
    @DisplayName("attemptSave: missing target → error displayed, save NOT called")
    void saveMissingTargetShowsError() throws Exception {
        Universe trek = universe("u-trek", "Star Trek", true);
        when(universeService.findAll()).thenReturn(List.of(trek));
        when(universeService.findAllActive()).thenReturn(List.of(trek));
        when(starRepository.findAll()).thenReturn(List.of(starObj("s1", "Sol")));
        runOnFx(() -> {
            AliasEditorDialog dialog = newCreate();
            dialog.aliasTextFieldForTest().setText("Vulcan");
            // target left blank
            boolean ok = dialog.attemptSave();
            assertFalse(ok);
            assertTrue(dialog.errorLabelForTest().getText().toLowerCase().contains("target"),
                    "error should mention target: " + dialog.errorLabelForTest().getText());
            verify(aliasService, never()).save(any());
        });
    }

    @Test
    @DisplayName("attemptSave: missing alias text → error displayed")
    void saveMissingAliasTextShowsError() throws Exception {
        Universe trek = universe("u-trek", "Star Trek", true);
        when(universeService.findAll()).thenReturn(List.of(trek));
        when(universeService.findAllActive()).thenReturn(List.of(trek));
        when(starRepository.findAll()).thenReturn(List.of(starObj("s1", "Sol")));
        runOnFx(() -> {
            AliasEditorDialog dialog = newCreate();
            dialog.targetComboForTest().setValue("Sol");
            // aliasText left blank
            boolean ok = dialog.attemptSave();
            assertFalse(ok);
            assertTrue(dialog.errorLabelForTest().getText().toLowerCase().contains("alias text"),
                    "error: " + dialog.errorLabelForTest().getText());
        });
    }

    @Test
    @DisplayName("attemptSave: valid input → save called with correct Alias; result populated")
    void saveValidInputCallsService() throws Exception {
        Universe trek = universe("u-trek", "Star Trek", true);
        when(universeService.findAll()).thenReturn(List.of(trek));
        when(universeService.findAllActive()).thenReturn(List.of(trek));
        when(starRepository.findAll()).thenReturn(List.of(starObj("s1", "Sol")));
        when(aliasService.save(any())).thenAnswer(inv -> inv.getArgument(0));

        runOnFx(() -> {
            AliasEditorDialog dialog = newCreate();
            dialog.targetComboForTest().setValue("Sol");
            dialog.aliasTextFieldForTest().setText("Vulcan");
            dialog.descriptionAreaForTest().setText("home of the Vulcans");

            boolean ok = dialog.attemptSave();
            assertTrue(ok);

            ArgumentCaptor<Alias> captor = ArgumentCaptor.forClass(Alias.class);
            verify(aliasService).save(captor.capture());
            Alias saved = captor.getValue();
            assertEquals("u-trek", saved.universeId());
            assertEquals(AliasTargetKind.STAR, saved.targetKind());
            assertEquals("s1", saved.targetId());
            assertEquals("Vulcan", saved.aliasText());
            assertEquals("home of the Vulcans", saved.description());
            assertNotNull(dialog.savedResultForTest());
        });
    }

    @Test
    @DisplayName("attemptSave: service throws IllegalStateException (duplicate) → error shown, dialog stays open")
    void saveDuplicateShowsError() throws Exception {
        Universe trek = universe("u-trek", "Star Trek", true);
        when(universeService.findAll()).thenReturn(List.of(trek));
        when(universeService.findAllActive()).thenReturn(List.of(trek));
        when(starRepository.findAll()).thenReturn(List.of(starObj("s1", "Sol")));
        when(aliasService.save(any()))
                .thenThrow(new IllegalStateException(
                        "An alias already exists for universe 'u-trek' + target STAR/s1 "
                                + "(existing alias: 'OtherName', id=catalog-alias-existing)."));

        runOnFx(() -> {
            AliasEditorDialog dialog = newCreate();
            dialog.targetComboForTest().setValue("Sol");
            dialog.aliasTextFieldForTest().setText("Vulcan");
            boolean ok = dialog.attemptSave();
            assertFalse(ok);
            assertTrue(dialog.errorLabelForTest().getText().contains("already exists"),
                    "should surface duplicate message: " + dialog.errorLabelForTest().getText());
            assertNull(dialog.savedResultForTest());
        });
    }

    // ============================================================
    // Edit path
    // ============================================================

    @Test
    @DisplayName("edit mode: title is 'Edit Alias'; Universe + Target + Kind are locked")
    void editModeLocksFields() throws Exception {
        Universe trek = universe("u-trek", "Star Trek", true);
        when(universeService.findAll()).thenReturn(List.of(trek));
        when(universeService.findById("u-trek")).thenReturn(Optional.of(trek));
        when(starRepository.findAll()).thenReturn(List.of(starObj("s1", "Sol")));
        when(starRepository.findById("s1")).thenReturn(Optional.of(starObj("s1", "Sol")));

        Alias existing = new Alias("catalog-alias-existing", "u-trek", AliasTargetKind.STAR, "s1",
                "OldName", "Old desc",
                Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-01-01T00:00:00Z"));

        runOnFx(() -> {
            AliasEditorDialog dialog = newEdit(existing);
            assertEquals("Edit Alias", dialog.getTitle());
            // Universe, target, kind: locked
            assertTrue(dialog.universeComboForTest().isDisabled(), "universe locked");
            assertTrue(dialog.targetComboForTest().isDisabled(), "target locked");
            assertTrue(dialog.starRadioForTest().isDisabled(), "star radio locked");
            assertTrue(dialog.exoplanetRadioForTest().isDisabled(), "exoplanet radio locked");
            // Pre-populated
            assertEquals("Star Trek", dialog.universeComboForTest().getValue());
            assertEquals("Sol", dialog.targetComboForTest().getValue());
            assertEquals("OldName", dialog.aliasTextFieldForTest().getText());
            assertEquals("Old desc", dialog.descriptionAreaForTest().getText());
        });
    }

    @Test
    @DisplayName("edit save: preserves id, universe, target; modifies aliasText + description")
    void editSavePreservesIdentity() throws Exception {
        Universe trek = universe("u-trek", "Star Trek", true);
        when(universeService.findAll()).thenReturn(List.of(trek));
        when(universeService.findById("u-trek")).thenReturn(Optional.of(trek));
        when(starRepository.findAll()).thenReturn(List.of(starObj("s1", "Sol")));
        when(starRepository.findById("s1")).thenReturn(Optional.of(starObj("s1", "Sol")));
        when(aliasService.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Alias existing = new Alias("catalog-alias-existing", "u-trek", AliasTargetKind.STAR, "s1",
                "OldName", "Old desc",
                Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-01-01T00:00:00Z"));

        runOnFx(() -> {
            AliasEditorDialog dialog = newEdit(existing);
            dialog.aliasTextFieldForTest().setText("NewName");
            dialog.descriptionAreaForTest().setText("New desc");
            boolean ok = dialog.attemptSave();
            assertTrue(ok);
            ArgumentCaptor<Alias> captor = ArgumentCaptor.forClass(Alias.class);
            verify(aliasService).save(captor.capture());
            Alias saved = captor.getValue();
            assertEquals("catalog-alias-existing", saved.id(), "id preserved on edit");
            assertEquals("u-trek", saved.universeId(), "universe preserved");
            assertEquals(AliasTargetKind.STAR, saved.targetKind(), "kind preserved");
            assertEquals("s1", saved.targetId(), "target preserved");
            assertEquals("NewName", saved.aliasText(), "alias text updated");
            assertEquals("New desc", saved.description(), "description updated");
        });
    }
}
