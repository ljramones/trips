package com.teamgannon.trips.construct.ui;

import com.teamgannon.trips.spaceshipmodeller.propulsion.DriveType;
import com.terranrepublic.assets.Armament;
import com.terranrepublic.assets.CatalogOperationalStatus;
import com.terranrepublic.assets.CatalogProvenance;
import com.terranrepublic.assets.InteriorGravityType;
import com.terranrepublic.assets.Megastructure;
import com.terranrepublic.assets.MegastructureArchetype;
import com.terranrepublic.assets.MegastructureOriginType;
import com.terranrepublic.assets.Mobility;
import com.terranrepublic.assets.OperationalState;
import com.terranrepublic.assets.SourceType;
import com.terranrepublic.assets.StationFunction;
import com.terranrepublic.assets.TechLevel;
import com.terranrepublic.assets.WeaponType;
import javafx.application.Platform;
import javafx.scene.control.ButtonType;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Instant;
import java.util.List;
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
 * Headless tests for {@link MegastructureEditorDialog}. Mirrors the {@link StationEditorDialogTest}
 * pattern: lightweight {@link Platform#startup} bootstrap, dialog constructed on the FX thread
 * inside a latch, OK result-converter invoked directly.
 */
class MegastructureEditorDialogTest {

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

    // -------------------------------------------------------- Troy-shaped fixture

    private static Megastructure troyShaped() {
        return new Megastructure(
                "troy-1",
                "Troy",
                "TR-01",
                "Hollowed nickel-iron asteroid (originally Hektor), Earth's primary defensive fortress.",
                "Solar System defense fortress",
                "INFERRED: precise armament count not given in source; crew complement approximate.",
                MegastructureArchetype.CONVERTED_ASTEROID,
                23.0,
                1.0e9,
                4500.0,
                Mobility.MOBILE_LIMITED,
                DriveType.ORION,
                MegastructureOriginType.BUILT_BY_KNOWN,
                "Solar Confederation / Saturn Photon Project",
                null,
                2014,
                StationFunction.DEFENSIVE,
                Set.of(StationFunction.MILITARY_COMMAND, StationFunction.SHIPBUILDING),
                true,
                50000L,
                InteriorGravityType.NATURAL_MASS,
                OperationalState.OPERATIONAL,
                false,
                List.of(new Armament("SAPL primary", WeaponType.SOLAR_PUMPED_LASER, 1, 1.0e6, 1.0e7, "main", null)),
                new CatalogProvenance(SourceType.SCIENCE_FICTION, "Troy Rising", "Troy Rising",
                        CatalogOperationalStatus.FICTIONAL),
                "Solar Confederation",
                "Solar Confederation",
                TechLevel.ADVANCED,
                Instant.parse("2014-01-01T00:00:00Z"),
                Instant.parse("2014-01-01T00:00:00Z"));
    }

    // -------------------------------------------------------- defaults

    @Test
    @DisplayName("new-dialog applyDefaults: UNKNOWN/UNKNOWN/STATIONKEEPING/null-aux-drive/UNKNOWN-status")
    void newDialogDefaults() throws InterruptedException {
        AtomicReference<MegastructureEditorDialog> ref = new AtomicReference<>();
        onFx(() -> ref.set(new MegastructureEditorDialog()));

        MegastructureEditorDialog d = ref.get();
        assertEquals(MegastructureArchetype.UNKNOWN, d.archetypeComboForTesting().getValue());
        assertEquals(MegastructureOriginType.UNKNOWN, d.originTypeComboForTesting().getValue());
        assertEquals(Mobility.STATIONKEEPING, d.mobilityComboForTesting().getValue());
        assertEquals(InteriorGravityType.UNKNOWN, d.interiorGravityComboForTesting().getValue());
        assertEquals(StationFunction.UNKNOWN, d.primaryFunctionComboForTesting().getValue());
        assertEquals(SourceType.UNKNOWN, d.sourceTypeComboForTesting().getValue());
        assertEquals(CatalogOperationalStatus.UNKNOWN, d.catalogStatusComboForTesting().getValue());
        assertNull(d.auxiliaryDriveComboForTesting().getValue(),
                "auxiliary drive must default to null (None)");
    }

    @Test
    @DisplayName("applyDefaults: produces documented defaults from buildDraft")
    void applyDefaultsProducesDocumentedDefaults() throws InterruptedException {
        AtomicReference<Megastructure> draft = new AtomicReference<>();
        onFx(() -> {
            MegastructureEditorDialog d = new MegastructureEditorDialog();
            javafx.scene.control.TextField name = lookupTextFieldByAccessibleText(d,
                    ConstructLabels.get("editor.field.name"));
            name.setText("Defaults Test");
            draft.set(d.getResultConverter().call(ButtonType.OK));
        });
        Megastructure m = draft.get();
        assertNotNull(m);
        assertEquals(MegastructureArchetype.UNKNOWN, m.archetype());
        assertEquals(MegastructureOriginType.UNKNOWN, m.originType());
        assertEquals(Mobility.STATIONKEEPING, m.mobility());
        assertNull(m.auxiliaryDrive());
        assertEquals(StationFunction.UNKNOWN, m.primaryFunction());
        assertEquals(Set.of(), m.secondaryFunctions());
        assertEquals(InteriorGravityType.UNKNOWN, m.interiorGravity());
        assertEquals(OperationalState.OPERATIONAL, m.operationalState());
        assertEquals(CatalogProvenance.unknown(), m.provenance());
        assertEquals(List.of(), m.armaments());
    }

    // -------------------------------------------------------- Troy round-trip

    @Test
    @DisplayName("Troy-shaped fixture round-trips through the dialog with all 30 fields preserved")
    void troyShapedRoundTrips() throws InterruptedException {
        AtomicReference<Megastructure> draft = new AtomicReference<>();
        onFx(() -> {
            MegastructureEditorDialog d = new MegastructureEditorDialog(troyShaped());
            assertFalse(d.okButtonForTesting().isDisable(),
                    "Troy-shaped fixture must pass validation");
            draft.set(d.getResultConverter().call(ButtonType.OK));
        });

        Megastructure m = draft.get();
        Megastructure src = troyShaped();
        assertNotNull(m);
        assertEquals(src.id(), m.id(), "edit preserves the original id");
        assertEquals(src.name(), m.name());
        assertEquals(src.designation(), m.designation());
        assertEquals(src.description(), m.description());
        assertEquals(src.category(), m.category());
        assertEquals(src.notes(), m.notes());
        assertEquals(src.archetype(), m.archetype());
        assertEquals(src.dimensionsKm(), m.dimensionsKm());
        assertEquals(src.dryMassMegatons(), m.dryMassMegatons());
        assertEquals(src.internalVolumeKm3(), m.internalVolumeKm3());
        assertEquals(src.mobility(), m.mobility());
        assertEquals(src.auxiliaryDrive(), m.auxiliaryDrive());
        assertEquals(src.originType(), m.originType());
        assertEquals(src.builderPolity(), m.builderPolity());
        assertEquals(src.discoveryYear(), m.discoveryYear());
        assertEquals(src.constructionYear(), m.constructionYear());
        assertEquals(src.primaryFunction(), m.primaryFunction());
        assertEquals(src.secondaryFunctions(), m.secondaryFunctions());
        assertEquals(src.hasInteriorSetting(), m.hasInteriorSetting());
        assertEquals(src.interiorPopulation(), m.interiorPopulation());
        assertEquals(src.interiorGravity(), m.interiorGravity());
        assertEquals(src.operationalState(), m.operationalState());
        assertEquals(src.concealed(), m.concealed());
        assertEquals(src.armaments(), m.armaments());
        assertEquals(src.provenance(), m.provenance());
        assertEquals(src.faction(), m.faction());
        assertEquals(src.allegiance(), m.allegiance());
        assertEquals(src.techLevel(), m.techLevel());
    }

    // -------------------------------------------------------- primary/secondary invariant

    @Test
    @DisplayName("changing primaryFunction silently removes that value from the secondary selection")
    void primaryChangeRemovesFromSecondary() throws InterruptedException {
        AtomicReference<MegastructureEditorDialog> ref = new AtomicReference<>();
        onFx(() -> {
            MegastructureEditorDialog d = new MegastructureEditorDialog();
            int rIdx = d.secondaryFunctionsListForTesting().getItems().indexOf(StationFunction.RESEARCH);
            int cIdx = d.secondaryFunctionsListForTesting().getItems().indexOf(StationFunction.COMMERCIAL);
            d.secondaryFunctionsListForTesting().getSelectionModel().selectIndices(rIdx, cIdx);
            d.primaryFunctionComboForTesting().setValue(StationFunction.RESEARCH);
            ref.set(d);
        });

        MegastructureEditorDialog d = ref.get();
        assertFalse(d.secondaryFunctionsListForTesting().getSelectionModel().getSelectedItems()
                        .contains(StationFunction.RESEARCH),
                "primary RESEARCH must be removed from the secondary selection");
        assertTrue(d.secondaryFunctionsListForTesting().getSelectionModel().getSelectedItems()
                        .contains(StationFunction.COMMERCIAL),
                "non-collision secondary COMMERCIAL must remain selected");
    }

    @Test
    @DisplayName("soft 3-cap hint label is visible when secondary set has 4+ values, hidden otherwise")
    void softThreeCapHintAppearsWhenSecondarySetExceedsThree() throws InterruptedException {
        AtomicReference<MegastructureEditorDialog> ref = new AtomicReference<>();
        onFx(() -> {
            MegastructureEditorDialog d = new MegastructureEditorDialog();
            assertFalse(d.secondaryHintLabelForTesting().isVisible(),
                    "empty secondary set: hint hidden");

            javafx.scene.control.ListView<StationFunction> list = d.secondaryFunctionsListForTesting();
            list.getSelectionModel().selectIndices(
                    list.getItems().indexOf(StationFunction.RESEARCH),
                    list.getItems().indexOf(StationFunction.COMMERCIAL),
                    list.getItems().indexOf(StationFunction.RESIDENTIAL));
            assertFalse(d.secondaryHintLabelForTesting().isVisible(),
                    "3-value secondary set: hint hidden");

            list.getSelectionModel().select(list.getItems().indexOf(StationFunction.DIPLOMATIC));
            ref.set(d);
        });

        assertTrue(ref.get().secondaryHintLabelForTesting().isVisible(),
                "4-value secondary set: soft 3-cap hint must appear");
        assertTrue(ref.get().secondaryHintLabelForTesting().isManaged(),
                "hint Label must be managed when visible so it claims layout space");
    }

    // -------------------------------------------------------- auxiliaryDrive null preservation

    @Test
    @DisplayName("null auxiliaryDrive in → null auxiliaryDrive out (None sentinel)")
    void auxiliaryDriveNullPreservation() throws InterruptedException {
        Megastructure src = new Megastructure(
                "n-1", "No-Drive Megastructure", "", "", null, null,
                MegastructureArchetype.ENGINEERED_WORLD,
                100.0, 1.0e15, 1.0e9,
                Mobility.STATIONKEEPING, null,  // null aux drive
                MegastructureOriginType.BUILT_BY_KNOWN, "Builder", null, 3000,
                StationFunction.RESIDENTIAL, Set.of(),
                true, 1_000_000_000L, InteriorGravityType.SPIN,
                OperationalState.OPERATIONAL, false, List.of(),
                CatalogProvenance.unknown(), "Faction", "Faction", TechLevel.ADVANCED,
                Instant.parse("2025-01-01T00:00:00Z"), Instant.parse("2025-01-01T00:00:00Z"));

        AtomicReference<Megastructure> draft = new AtomicReference<>();
        onFx(() -> {
            MegastructureEditorDialog d = new MegastructureEditorDialog(src);
            assertNull(d.auxiliaryDriveComboForTesting().getValue(),
                    "populateFrom must read null aux drive as null");
            draft.set(d.getResultConverter().call(ButtonType.OK));
        });
        assertNull(draft.get().auxiliaryDrive(),
                "buildDraft must produce null aux drive when None is selected");
    }

    @Test
    @DisplayName("auxiliaryDrive combo includes a 'None' (null) item as the leading sentinel")
    void auxiliaryDriveCombosIncludesNullSentinel() throws InterruptedException {
        AtomicReference<MegastructureEditorDialog> ref = new AtomicReference<>();
        onFx(() -> ref.set(new MegastructureEditorDialog()));
        assertTrue(ref.get().auxiliaryDriveComboForTesting().getItems().contains(null),
                "items must include a null sentinel for the None selection");
        assertEquals(0, ref.get().auxiliaryDriveComboForTesting().getItems().indexOf(null),
                "the null sentinel must lead the items list");
    }

    // -------------------------------------------------------- year nullability

    @Test
    @DisplayName("blank discoveryYear / constructionYear fields surface as null Integers")
    void blankYearsSurfaceAsNull() throws InterruptedException {
        AtomicReference<Megastructure> draft = new AtomicReference<>();
        onFx(() -> {
            MegastructureEditorDialog d = new MegastructureEditorDialog();
            lookupTextFieldByAccessibleText(d, ConstructLabels.get("editor.field.name"))
                    .setText("Year Test");
            d.archetypeComboForTesting().setValue(MegastructureArchetype.BIG_DUMB_OBJECT);
            d.originTypeComboForTesting().setValue(MegastructureOriginType.FOUND_INTACT);
            d.discoveryYearFieldForTesting().setText("");
            d.constructionYearFieldForTesting().setText("");
            draft.set(d.getResultConverter().call(ButtonType.OK));
        });
        assertNull(draft.get().discoveryYear());
        assertNull(draft.get().constructionYear());
    }

    @Test
    @DisplayName("non-blank discoveryYear / constructionYear fields surface as parsed Integers")
    void nonBlankYearsParsedAsIntegers() throws InterruptedException {
        AtomicReference<Megastructure> draft = new AtomicReference<>();
        onFx(() -> {
            MegastructureEditorDialog d = new MegastructureEditorDialog();
            lookupTextFieldByAccessibleText(d, ConstructLabels.get("editor.field.name"))
                    .setText("Year Test");
            d.archetypeComboForTesting().setValue(MegastructureArchetype.PURPOSE_BUILT_FORT);
            d.originTypeComboForTesting().setValue(MegastructureOriginType.BUILT_BY_KNOWN);
            d.discoveryYearFieldForTesting().setText("2245");
            d.constructionYearFieldForTesting().setText("2050");
            draft.set(d.getResultConverter().call(ButtonType.OK));
        });
        assertEquals(Integer.valueOf(2245), draft.get().discoveryYear());
        assertEquals(Integer.valueOf(2050), draft.get().constructionYear());
    }

    // -------------------------------------------------------- catalog round-trip

    @Test
    @DisplayName("Catalog section round-trips through the provenance composite")
    void catalogSectionRoundTrip() throws InterruptedException {
        AtomicReference<Megastructure> draft = new AtomicReference<>();
        onFx(() -> {
            MegastructureEditorDialog d = new MegastructureEditorDialog();
            lookupTextFieldByAccessibleText(d, ConstructLabels.get("editor.field.name"))
                    .setText("Catalog Round-Trip");

            d.archetypeComboForTesting().setValue(MegastructureArchetype.PURPOSE_BUILT_FORT);
            d.originTypeComboForTesting().setValue(MegastructureOriginType.BUILT_BY_KNOWN);
            d.sourceTypeComboForTesting().setValue(SourceType.SCIENCE_FICTION);
            d.sourceUniverseFieldForTesting().setText("Star Wars");
            d.sourceWorkFieldForTesting().setText("A New Hope");
            d.catalogStatusComboForTesting().setValue(CatalogOperationalStatus.FICTIONAL);
            draft.set(d.getResultConverter().call(ButtonType.OK));
        });

        Megastructure m = draft.get();
        assertNotNull(m);
        assertEquals(SourceType.SCIENCE_FICTION, m.provenance().sourceType());
        assertEquals("Star Wars", m.provenance().sourceUniverse());
        assertEquals("A New Hope", m.provenance().sourceWork());
        assertEquals(CatalogOperationalStatus.FICTIONAL, m.provenance().status());
    }

    @Test
    @DisplayName("blank source-work field surfaces as null on the provenance composite")
    void emptySourceWorkYieldsNull() throws InterruptedException {
        AtomicReference<Megastructure> draft = new AtomicReference<>();
        onFx(() -> {
            MegastructureEditorDialog d = new MegastructureEditorDialog();
            lookupTextFieldByAccessibleText(d, ConstructLabels.get("editor.field.name"))
                    .setText("Null Work");
            d.archetypeComboForTesting().setValue(MegastructureArchetype.CONVERTED_ASTEROID);
            d.originTypeComboForTesting().setValue(MegastructureOriginType.BUILT_BY_KNOWN);
            d.sourceWorkFieldForTesting().setText("   ");
            draft.set(d.getResultConverter().call(ButtonType.OK));
        });
        assertNull(draft.get().provenance().sourceWork());
    }

    // -------------------------------------------------------- parameterized: archetype / origin / interior / function

    @ParameterizedTest
    @EnumSource(MegastructureArchetype.class)
    @DisplayName("every MegastructureArchetype round-trips through the dialog")
    void everyArchetypeRoundTrips(MegastructureArchetype archetype) throws InterruptedException {
        AtomicReference<Megastructure> draft = new AtomicReference<>();
        onFx(() -> {
            MegastructureEditorDialog d = new MegastructureEditorDialog();
            lookupTextFieldByAccessibleText(d, ConstructLabels.get("editor.field.name"))
                    .setText("Archetype-" + archetype.name());
            d.archetypeComboForTesting().setValue(archetype);
            d.originTypeComboForTesting().setValue(MegastructureOriginType.UNKNOWN);
            draft.set(d.getResultConverter().call(ButtonType.OK));
        });
        assertNotNull(draft.get());
        assertEquals(archetype, draft.get().archetype());
    }

    @ParameterizedTest
    @EnumSource(MegastructureOriginType.class)
    @DisplayName("every MegastructureOriginType round-trips through the dialog")
    void everyOriginTypeRoundTrips(MegastructureOriginType origin) throws InterruptedException {
        AtomicReference<Megastructure> draft = new AtomicReference<>();
        onFx(() -> {
            MegastructureEditorDialog d = new MegastructureEditorDialog();
            lookupTextFieldByAccessibleText(d, ConstructLabels.get("editor.field.name"))
                    .setText("Origin-" + origin.name());
            d.archetypeComboForTesting().setValue(MegastructureArchetype.UNKNOWN);
            d.originTypeComboForTesting().setValue(origin);
            draft.set(d.getResultConverter().call(ButtonType.OK));
        });
        assertNotNull(draft.get());
        assertEquals(origin, draft.get().originType());
    }

    @ParameterizedTest
    @EnumSource(InteriorGravityType.class)
    @DisplayName("every InteriorGravityType round-trips through the dialog")
    void everyInteriorGravityRoundTrips(InteriorGravityType g) throws InterruptedException {
        AtomicReference<Megastructure> draft = new AtomicReference<>();
        onFx(() -> {
            MegastructureEditorDialog d = new MegastructureEditorDialog();
            lookupTextFieldByAccessibleText(d, ConstructLabels.get("editor.field.name"))
                    .setText("Gravity-" + g.name());
            d.archetypeComboForTesting().setValue(MegastructureArchetype.UNKNOWN);
            d.originTypeComboForTesting().setValue(MegastructureOriginType.UNKNOWN);
            d.interiorGravityComboForTesting().setValue(g);
            draft.set(d.getResultConverter().call(ButtonType.OK));
        });
        assertNotNull(draft.get());
        assertEquals(g, draft.get().interiorGravity());
    }

    @ParameterizedTest
    @EnumSource(StationFunction.class)
    @DisplayName("every StationFunction round-trips as primaryFunction")
    void everyStationFunctionAsPrimary(StationFunction f) throws InterruptedException {
        AtomicReference<Megastructure> draft = new AtomicReference<>();
        onFx(() -> {
            MegastructureEditorDialog d = new MegastructureEditorDialog();
            lookupTextFieldByAccessibleText(d, ConstructLabels.get("editor.field.name"))
                    .setText("Function-" + f.name());
            d.archetypeComboForTesting().setValue(MegastructureArchetype.UNKNOWN);
            d.originTypeComboForTesting().setValue(MegastructureOriginType.UNKNOWN);
            d.primaryFunctionComboForTesting().setValue(f);
            draft.set(d.getResultConverter().call(ButtonType.OK));
        });
        assertNotNull(draft.get());
        assertEquals(f, draft.get().primaryFunction());
    }

    // -------------------------------------------------------- node lookup helpers

    private static javafx.scene.control.TextField lookupTextFieldByAccessibleText(
            MegastructureEditorDialog dialog, String accessibleText) {
        return (javafx.scene.control.TextField) walk(dialog.getDialogPane(),
                node -> javafx.scene.control.TextField.class.isInstance(node)
                        && accessibleText.equals(node.getAccessibleText()));
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
}
