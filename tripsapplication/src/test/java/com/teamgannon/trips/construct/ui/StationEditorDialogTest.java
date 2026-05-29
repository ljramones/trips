package com.teamgannon.trips.construct.ui;

import com.teamgannon.trips.spaceshipmodeller.propulsion.DriveType;
import com.terranrepublic.assets.Armament;
import com.terranrepublic.assets.CatalogOperationalStatus;
import com.terranrepublic.assets.CatalogProvenance;
import com.terranrepublic.assets.Mobility;
import com.terranrepublic.assets.OperationalState;
import com.terranrepublic.assets.SourceType;
import com.terranrepublic.assets.StationDesign;
import com.terranrepublic.assets.StationFunction;
import com.terranrepublic.assets.StationType;
import com.terranrepublic.assets.TechLevel;
import com.terranrepublic.assets.WeaponType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import javafx.application.Platform;
import javafx.scene.control.ButtonType;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Headless tests for {@link StationEditorDialog}. Mirrors the pattern from
 * {@code SpaceshipEditorDialogFieldsTest}: lightweight {@link Platform#startup} bootstrap,
 * dialog constructed on the FX thread inside a {@link CountDownLatch} latch, OK result-converter
 * invoked directly to assert the produced draft without showing the dialog.
 */
class StationEditorDialogTest {

    private static boolean javaFxInitialized = false;

    @BeforeAll
    static void initJavaFx() {
        try {
            Platform.startup(() -> {
            });
            javaFxInitialized = true;
        } catch (IllegalStateException alreadyStarted) {
            javaFxInitialized = true;
        } catch (Exception e) {
            javaFxInitialized = false;
        }
    }

    private void onFx(Runnable r) throws InterruptedException {
        Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                r.run();
            } catch (Throwable t) {
                error.set(t);
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        if (error.get() != null) {
            throw new AssertionError(error.get());
        }
    }

    /**
     * v2 Phase D.7 Step 6 — Troy migrated to {@link com.terranrepublic.assets.Megastructure}.
     * The dialog tests need a non-trivial StationDesign fixture; this is a hand-built Troy-shaped
     * value preserving the original {@code MANEUVERABLE} + {@code ORION} mobility shape that
     * the {@code editDialogReadsExisting} assertions rely on.
     * <p>
     * Cached in a static field so successive calls return the SAME instance — tests like
     * {@code editDialogReadsExisting} compare {@code troy().id()} across calls.
     */
    private static final StationDesign TROY_FIXTURE = buildTroyFixture();

    private static StationDesign troy() {
        return TROY_FIXTURE;
    }

    private static StationDesign buildTroyFixture() {
        Instant now = Instant.parse("2024-01-01T00:00:00Z");
        return new StationDesign(
                UUID.randomUUID().toString(),
                "Troy",
                "TR-T",
                StationType.GATE_FORT,
                "Test Faction",
                false,
                "Test Faction",
                "Synthetic Troy-shaped test fixture (Phase D.7 Step 6).",
                9_000,
                7_000,
                2.0e12,
                2_000,
                150_000,
                120_000,
                1.5e11,
                Mobility.MANEUVERABLE,
                DriveType.ORION,
                List.of(),
                List.of(new Armament("SAPL primary", WeaponType.SOLAR_PUMPED_LASER,
                        1, 1.0e6, 1.0e7, "main", null)),
                5.0e7,
                true,
                TechLevel.ADVANCED,
                "gate fortification",
                OperationalState.OPERATIONAL,
                now,
                now,
                StationFunction.DEFENSIVE,
                java.util.Set.of(StationFunction.MILITARY_COMMAND),
                new CatalogProvenance(SourceType.SCIENCE_FICTION, "Troy Rising", null,
                        CatalogOperationalStatus.FICTIONAL));
    }

    @Test
    @DisplayName("new-dialog defaults: OUTPOST / FIXED / OPERATIONAL / aux drive disabled")
    void newDialogDefaults() throws InterruptedException {
        AtomicReference<StationEditorDialog> ref = new AtomicReference<>();
        onFx(() -> ref.set(new StationEditorDialog()));

        StationEditorDialog dialog = ref.get();
        assertEquals(Mobility.FIXED, dialog.mobilityComboForTesting().getValue());
        assertTrue(dialog.auxiliaryDriveComboForTesting().isDisabled(),
                "auxiliary drive must be disabled for FIXED mobility");
        assertNull(dialog.auxiliaryDriveComboForTesting().getValue(),
                "auxiliary drive must be null for FIXED mobility");
    }

    @Test
    @DisplayName("edit-dialog reads Troy: stationType + mobility + auxiliaryDrive + allegiance all round-trip")
    void editDialogReadsExisting() throws InterruptedException {
        AtomicReference<StationDesign> draft = new AtomicReference<>();
        onFx(() -> {
            StationEditorDialog dialog = new StationEditorDialog(troy());
            // OK should be enabled because Troy is a valid station.
            assertFalse(dialog.okButtonForTesting().isDisable(), "loading Troy must not produce validation errors");
            draft.set(dialog.getResultConverter().call(ButtonType.OK));
        });

        StationDesign result = draft.get();
        assertNotNull(result);
        assertEquals(troy().id(), result.id(), "edit preserves the original id");
        assertEquals(troy().name(), result.name());
        assertEquals(troy().stationType(), result.stationType());
        assertEquals(troy().mobility(), result.mobility());
        assertEquals(troy().auxiliaryDrive(), result.auxiliaryDrive());
        assertEquals(troy().allegiance(), result.allegiance());
        assertEquals(troy().concealed(), result.concealed());
        assertEquals(troy().operationalState(), result.operationalState());
        assertEquals(troy().carriedCraft(), result.carriedCraft());
        assertEquals(troy().armaments(), result.armaments());
        // v2 Phase D.6 — three new fields persist through the dialog. Pre-Step-6, Troy carries
        // the 27-compat default values: UNKNOWN primary, empty secondary set, and a provenance
        // composite with the universe label wrapped under UNKNOWN/null/UNKNOWN. Step 6 will
        // replace these with the worked-example values.
        assertEquals(troy().primaryFunction(), result.primaryFunction());
        assertEquals(troy().secondaryFunctions(), result.secondaryFunctions());
        assertEquals(troy().provenance(), result.provenance());
    }

    @Test
    @DisplayName("mutate-and-save: change name / concealed / allegiance / operational state — id preserved")
    void mutateThenSaveProducesUpdatedDesign() throws InterruptedException {
        AtomicReference<StationDesign> draft = new AtomicReference<>();
        onFx(() -> {
            StationEditorDialog dialog = new StationEditorDialog(troy());
            // Trigger mutations via the publicly exposed controls. We use the test seam combos
            // for mobility / aux drive and reach the other controls through node lookups.
            dialog.mobilityComboForTesting().setValue(Mobility.MANEUVERABLE);
            dialog.auxiliaryDriveComboForTesting().setValue(DriveType.ORION);

            javafx.scene.control.TextField name = lookupTextFieldByAccessibleText(dialog,
                    ConstructLabels.get("editor.field.name"));
            javafx.scene.control.TextField allegiance = lookupTextFieldByAccessibleText(dialog,
                    ConstructLabels.get("editor.field.allegiance"));
            javafx.scene.control.CheckBox concealed = lookupCheckBoxByAccessibleText(dialog,
                    ConstructLabels.get("editor.field.concealed"));
            javafx.scene.control.ComboBox<?> opState = lookupComboBoxByAccessibleText(dialog,
                    ConstructLabels.get("editor.field.operationalState"));

            name.setText("Troy Reborn");
            allegiance.setText("Free Worlds");
            concealed.setSelected(true);
            @SuppressWarnings("unchecked")
            javafx.scene.control.ComboBox<OperationalState> os =
                    (javafx.scene.control.ComboBox<OperationalState>) opState;
            os.setValue(OperationalState.DAMAGED);

            draft.set(dialog.getResultConverter().call(ButtonType.OK));
        });

        StationDesign result = draft.get();
        assertNotNull(result);
        assertEquals(troy().id(), result.id());
        assertEquals("Troy Reborn", result.name());
        assertEquals("Free Worlds", result.allegiance());
        assertTrue(result.concealed());
        assertEquals(OperationalState.DAMAGED, result.operationalState());
        assertEquals(Mobility.MANEUVERABLE, result.mobility());
        assertEquals(DriveType.ORION, result.auxiliaryDrive());
    }

    @Test
    @DisplayName("switching mobility to FIXED clears + disables auxiliary drive")
    void mobilityFixedDisablesAuxiliaryDrive() throws InterruptedException {
        AtomicReference<StationEditorDialog> ref = new AtomicReference<>();
        onFx(() -> {
            StationEditorDialog dialog = new StationEditorDialog(troy());
            dialog.mobilityComboForTesting().setValue(Mobility.MANEUVERABLE);
            dialog.auxiliaryDriveComboForTesting().setValue(DriveType.ORION);
            assertFalse(dialog.auxiliaryDriveComboForTesting().isDisabled());

            dialog.mobilityComboForTesting().setValue(Mobility.FIXED);
            ref.set(dialog);
        });

        StationEditorDialog dialog = ref.get();
        assertTrue(dialog.auxiliaryDriveComboForTesting().isDisabled(),
                "switching to FIXED must disable the auxiliary drive combo");
        assertNull(dialog.auxiliaryDriveComboForTesting().getValue(),
                "switching to FIXED must clear the auxiliary drive value");
    }

    @ParameterizedTest
    @EnumSource(StationType.class)
    @DisplayName("every StationType round-trips through the editor draft")
    void everyStationTypeRoundTrips(StationType type) throws InterruptedException {
        AtomicReference<StationDesign> draft = new AtomicReference<>();
        onFx(() -> {
            StationEditorDialog dialog = new StationEditorDialog();
            // Need a non-blank name + positive dry mass for the OK converter to run; the new
            // dialog applies a positive dry-mass default so we only have to set the name.
            javafx.scene.control.TextField nameField = lookupTextFieldByAccessibleText(dialog,
                    ConstructLabels.get("editor.field.name"));
            nameField.setText("Coverage Station");
            @SuppressWarnings("unchecked")
            javafx.scene.control.ComboBox<StationType> stCombo =
                    (javafx.scene.control.ComboBox<StationType>) lookupComboBoxByAccessibleText(dialog,
                            ConstructLabels.get("editor.station.field.stationType"));
            stCombo.setValue(type);
            draft.set(dialog.getResultConverter().call(ButtonType.OK));
        });

        StationDesign result = draft.get();
        assertNotNull(result);
        assertEquals(type, result.stationType());
    }

    // ---------------------------------------------------------------- node lookup helpers

    private static javafx.scene.control.TextField lookupTextFieldByAccessibleText(
            StationEditorDialog dialog, String accessibleText) {
        return (javafx.scene.control.TextField) lookupNode(dialog, accessibleText, javafx.scene.control.TextField.class);
    }

    private static javafx.scene.control.CheckBox lookupCheckBoxByAccessibleText(
            StationEditorDialog dialog, String accessibleText) {
        return (javafx.scene.control.CheckBox) lookupNode(dialog, accessibleText, javafx.scene.control.CheckBox.class);
    }

    private static javafx.scene.control.ComboBox<?> lookupComboBoxByAccessibleText(
            StationEditorDialog dialog, String accessibleText) {
        return (javafx.scene.control.ComboBox<?>) lookupNode(dialog, accessibleText, javafx.scene.control.ComboBox.class);
    }

    private static javafx.scene.Node lookupNode(StationEditorDialog dialog, String accessibleText,
                                                Class<?> type) {
        return walk(dialog.getDialogPane(), node ->
                type.isInstance(node) && accessibleText.equals(node.getAccessibleText()));
    }

    private static javafx.scene.Node walk(javafx.scene.Node node, java.util.function.Predicate<javafx.scene.Node> p) {
        if (node == null) {
            return null;
        }
        if (p.test(node)) {
            return node;
        }
        if (node instanceof javafx.scene.control.DialogPane dp) {
            javafx.scene.Node found = walk(dp.getContent(), p);
            if (found != null) {
                return found;
            }
        }
        if (node instanceof javafx.scene.control.ScrollPane sp) {
            javafx.scene.Node found = walk(sp.getContent(), p);
            if (found != null) {
                return found;
            }
        }
        if (node instanceof javafx.scene.Parent parent) {
            for (javafx.scene.Node child : parent.getChildrenUnmodifiable()) {
                javafx.scene.Node found = walk(child, p);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    // ==================================================================
    // v2 Phase D.6 — Function + Catalog section tests (Step 5)
    // ==================================================================

    @Test
    @DisplayName("applyDefaults: primaryFunction=UNKNOWN, secondaryFunctions empty, provenance=CatalogProvenance.unknown()")
    void applyDefaultsProducesDocumentedDefaults() throws InterruptedException {
        AtomicReference<StationDesign> draft = new AtomicReference<>();
        onFx(() -> {
            StationEditorDialog dialog = new StationEditorDialog();
            javafx.scene.control.TextField name = lookupTextFieldByAccessibleText(dialog,
                    ConstructLabels.get("editor.field.name"));
            name.setText("Defaults Test");
            draft.set(dialog.getResultConverter().call(ButtonType.OK));
        });
        StationDesign d = draft.get();
        assertNotNull(d);
        assertEquals(StationFunction.UNKNOWN, d.primaryFunction(),
                "applyDefaults sets primaryFunction to UNKNOWN");
        assertEquals(Set.of(), d.secondaryFunctions(),
                "applyDefaults leaves secondaryFunctions empty");
        assertEquals(CatalogProvenance.unknown(), d.provenance(),
                "applyDefaults sets provenance to CatalogProvenance.unknown()");
    }

    @Test
    @DisplayName("changing primaryFunction silently removes that value from the secondary selection")
    void primaryChangeRemovesFromSecondary() throws InterruptedException {
        AtomicReference<StationEditorDialog> ref = new AtomicReference<>();
        onFx(() -> {
            StationEditorDialog dialog = new StationEditorDialog();
            // Pre-select RESEARCH and COMMERCIAL in the secondary list.
            int rIdx = dialog.secondaryFunctionsListForTesting().getItems().indexOf(StationFunction.RESEARCH);
            int cIdx = dialog.secondaryFunctionsListForTesting().getItems().indexOf(StationFunction.COMMERCIAL);
            dialog.secondaryFunctionsListForTesting().getSelectionModel().selectIndices(rIdx, cIdx);
            // Now set primary to RESEARCH — the invariant must remove it from secondaries.
            dialog.primaryFunctionComboForTesting().setValue(StationFunction.RESEARCH);
            ref.set(dialog);
        });

        StationEditorDialog dialog = ref.get();
        assertFalse(dialog.secondaryFunctionsListForTesting().getSelectionModel().getSelectedItems()
                        .contains(StationFunction.RESEARCH),
                "primary RESEARCH must be removed from the secondary selection");
        assertTrue(dialog.secondaryFunctionsListForTesting().getSelectionModel().getSelectedItems()
                        .contains(StationFunction.COMMERCIAL),
                "non-collision secondary COMMERCIAL must remain selected");
    }

    @Test
    @DisplayName("soft 3-cap hint label is visible when secondary set has 4+ values, hidden otherwise")
    void softThreeCapHintAppearsWhenSecondarySetExceedsThree() throws InterruptedException {
        AtomicReference<StationEditorDialog> ref = new AtomicReference<>();
        onFx(() -> {
            StationEditorDialog dialog = new StationEditorDialog();
            // Empty start → hint must be hidden.
            assertFalse(dialog.secondaryHintLabelForTesting().isVisible(),
                    "empty secondary set: hint hidden");

            // Select 3 values → still hidden.
            javafx.scene.control.ListView<StationFunction> list = dialog.secondaryFunctionsListForTesting();
            list.getSelectionModel().selectIndices(
                    list.getItems().indexOf(StationFunction.RESEARCH),
                    list.getItems().indexOf(StationFunction.COMMERCIAL),
                    list.getItems().indexOf(StationFunction.RESIDENTIAL));
            assertFalse(dialog.secondaryHintLabelForTesting().isVisible(),
                    "3-value secondary set: hint hidden (3 is the cap, > 3 triggers)");

            // Select 4th → hint visible.
            list.getSelectionModel().select(list.getItems().indexOf(StationFunction.DIPLOMATIC));
            ref.set(dialog);
        });

        assertTrue(ref.get().secondaryHintLabelForTesting().isVisible(),
                "4-value secondary set: soft 3-cap hint must appear");
        assertTrue(ref.get().secondaryHintLabelForTesting().isManaged(),
                "hint Label must be managed when visible so it claims layout space");
    }

    @Test
    @DisplayName("Catalog section: source type / universe / source work / status round-trip through provenance composite")
    void catalogSectionRoundTrip() throws InterruptedException {
        AtomicReference<StationDesign> draft = new AtomicReference<>();
        onFx(() -> {
            StationEditorDialog dialog = new StationEditorDialog();
            lookupTextFieldByAccessibleText(dialog, ConstructLabels.get("editor.field.name"))
                    .setText("Catalog Round-Trip Test");

            dialog.sourceTypeComboForTesting().setValue(SourceType.SCIENCE_FICTION);
            dialog.sourceUniverseFieldForTesting().setText("Babylon 5");
            dialog.sourceWorkFieldForTesting().setText("Babylon 5");
            dialog.catalogStatusComboForTesting().setValue(CatalogOperationalStatus.FICTIONAL);

            draft.set(dialog.getResultConverter().call(ButtonType.OK));
        });

        StationDesign d = draft.get();
        assertNotNull(d);
        assertEquals(SourceType.SCIENCE_FICTION, d.provenance().sourceType());
        assertEquals("Babylon 5", d.provenance().sourceUniverse());
        assertEquals("Babylon 5", d.provenance().sourceWork());
        assertEquals(CatalogOperationalStatus.FICTIONAL, d.provenance().status());
    }

    @Test
    @DisplayName("empty source-work field surfaces as null on the provenance composite (genuine optionality)")
    void emptySourceWorkFieldYieldsNullOnProvenance() throws InterruptedException {
        AtomicReference<StationDesign> draft = new AtomicReference<>();
        onFx(() -> {
            StationEditorDialog dialog = new StationEditorDialog();
            lookupTextFieldByAccessibleText(dialog, ConstructLabels.get("editor.field.name"))
                    .setText("Null Source Work Test");

            dialog.sourceTypeComboForTesting().setValue(SourceType.REAL);
            dialog.sourceUniverseFieldForTesting().setText("Real / Proposed");
            dialog.sourceWorkFieldForTesting().setText("   ");  // blank — should null out
            dialog.catalogStatusComboForTesting().setValue(CatalogOperationalStatus.ACTIVE);

            draft.set(dialog.getResultConverter().call(ButtonType.OK));
        });

        assertNull(draft.get().provenance().sourceWork(),
                "blank source-work field must surface as null on the provenance, not empty string");
    }

    @ParameterizedTest
    @EnumSource(StationFunction.class)
    @DisplayName("every StationFunction round-trips through the dialog as primaryFunction")
    void everyStationFunctionAsPrimary(StationFunction f) throws InterruptedException {
        AtomicReference<StationDesign> draft = new AtomicReference<>();
        onFx(() -> {
            StationEditorDialog dialog = new StationEditorDialog();
            lookupTextFieldByAccessibleText(dialog, ConstructLabels.get("editor.field.name"))
                    .setText("Function-" + f.name());
            dialog.primaryFunctionComboForTesting().setValue(f);
            draft.set(dialog.getResultConverter().call(ButtonType.OK));
        });
        assertNotNull(draft.get());
        assertEquals(f, draft.get().primaryFunction());
    }
}
