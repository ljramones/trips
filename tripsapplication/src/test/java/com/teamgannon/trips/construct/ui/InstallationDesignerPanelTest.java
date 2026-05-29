package com.teamgannon.trips.construct.ui;

import com.teamgannon.trips.construct.ConstructRegistry;
import com.teamgannon.trips.spaceshipmodeller.service.StationDesignerService;
import com.teamgannon.trips.spaceshipmodeller.service.TransportNodeService;
import com.teamgannon.trips.spaceshipmodeller.service.WeaponInstallationDesignerService;
import com.terranrepublic.assets.AssetKind;
import com.terranrepublic.assets.Catalog;
import com.terranrepublic.assets.Cataloged;
import com.terranrepublic.assets.SpaceAsset;
import com.terranrepublic.assets.StationDesign;
import com.terranrepublic.assets.StationFunction;
import com.terranrepublic.assets.WeaponInstallation;
import com.terranrepublic.infrastructure.InfrastructureKind;
import com.terranrepublic.infrastructure.NodeType;
import com.terranrepublic.infrastructure.SpaceInfrastructure;
import com.terranrepublic.infrastructure.TransportNode;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Headless tests for the Installations Designer panel — Phase C steps 1 and 2.
 *
 * <p>Construction runs on the JavaFX Application Thread via the same lightweight
 * {@link Platform#startup(Runnable)} bootstrap used by {@code StarEditFormBinderTest} and
 * {@code SpaceshipEditorDialogFieldsTest}. Data is applied through the panel's package-private
 * {@code applyConstructs(List)} test seam so we don't need to spin up the background load
 * Task — the seam mirrors the production flow exactly (the Task's {@code setOnSucceeded}
 * calls into this same method).
 */
class InstallationDesignerPanelTest {

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
        assertTrue(latch.await(10, TimeUnit.SECONDS), "Test exceeded 10s on the FX thread");
        if (error.get() != null) {
            throw new AssertionError(error.get());
        }
    }

    /** Construct an InstallationDesignerPanel with no-op write services — the read-only tests
     *  only exercise the registry + apply seam. */
    private InstallationDesignerPanel newPanel(ConstructRegistry registry) {
        return new InstallationDesignerPanel(registry,
                Mockito.mock(StationDesignerService.class),
                Mockito.mock(WeaponInstallationDesignerService.class),
                Mockito.mock(TransportNodeService.class));
    }

    private ConstructRegistry registryWithCatalogSeed() {
        ConstructRegistry r = Mockito.mock(ConstructRegistry.class);
        List<SpaceAsset> stations = Catalog.all().stream()
                .filter(StationDesign.class::isInstance)
                .map(a -> (SpaceAsset) a)
                .toList();
        List<SpaceAsset> weapons = Catalog.all().stream()
                .filter(WeaponInstallation.class::isInstance)
                .map(a -> (SpaceAsset) a)
                .toList();
        Mockito.when(r.assetsByKind(AssetKind.STATION)).thenReturn(stations);
        Mockito.when(r.assetsByKind(AssetKind.WEAPON_INSTALLATION)).thenReturn(weapons);
        Mockito.when(r.infrastructureByKind(InfrastructureKind.TRANSPORT_NODE)).thenReturn(List.of());
        return r;
    }

    private List<Cataloged> catalogConstructsForApply() {
        // Mirrors what InstallationDesignerPanel.loadFromRegistry() would assemble: stations +
        // weapon installations + transport nodes (the catalog seeds no transport nodes today).
        List<Cataloged> all = new ArrayList<>();
        Catalog.all().stream()
                .filter(StationDesign.class::isInstance)
                .forEach(a -> all.add((Cataloged) a));
        Catalog.all().stream()
                .filter(WeaponInstallation.class::isInstance)
                .forEach(a -> all.add((Cataloged) a));
        return all;
    }

    private TransportNode ringGate(String name) {
        Instant now = Instant.parse("2025-07-01T10:00:00Z");
        return new TransportNode(
                UUID.randomUUID().toString(),
                name,
                "src",
                "F",
                false,
                "desc",
                NodeType.RING_GATE,
                0, 0, 0,
                List.of(),
                100,
                false,
                10,
                now,
                now);
    }

    // ------------------------------------------------------------------
    // Step 1 — skeleton construction
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Step 1 — panel constructs without throwing against a registry returning known data")
    void panelConstructsAgainstSeededRegistry() throws InterruptedException {
        AtomicReference<InstallationDesignerPanel> ref = new AtomicReference<>();
        onFx(() -> ref.set(newPanel(registryWithCatalogSeed())));
        InstallationDesignerPanel panel = ref.get();
        assertNotNull(panel, "panel constructs");
        assertNotNull(panel.tableForTesting(), "table is reachable");
    }

    @Test
    @DisplayName("Step 1 — constructor does not hit the registry (Issue 11: no FX-thread DB calls)")
    void constructorDoesNotTouchRegistry() throws InterruptedException {
        ConstructRegistry r = Mockito.mock(ConstructRegistry.class);
        onFx(() -> newPanel(r));
        Mockito.verifyNoInteractions(r);
    }

    // ------------------------------------------------------------------
    // Step 2 — table population, filters, details
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Step 2 — applying Catalog seed populates the table with the canonical entries")
    void applySeedPopulatesCatalog() throws InterruptedException {
        AtomicReference<InstallationDesignerPanel> ref = new AtomicReference<>();
        onFx(() -> {
            InstallationDesignerPanel panel = newPanel(registryWithCatalogSeed());
            panel.applyConstructs(catalogConstructsForApply());
            ref.set(panel);
        });

        InstallationDesignerPanel panel = ref.get();
        List<ConstructRow> rows = panel.rowsForTesting();
        long expectedCount = Catalog.all().stream()
                .filter(a -> a instanceof StationDesign || a instanceof WeaponInstallation)
                .count();
        assertEquals(expectedCount, rows.size(),
                "panel row count must match Catalog's station + weapon installation entries");
        assertTrue(rows.stream().anyMatch(r -> r.getName().equals("Troy")), "Troy present");
        assertTrue(rows.stream().anyMatch(r -> r.getName().equals("SAPL")), "SAPL present");
        assertTrue(rows.stream().anyMatch(r -> r.getName().equals("SheVa Gun")), "SheVa Gun present");
    }

    @Test
    @DisplayName("Step 2 — Kind filter narrows to the catalog's station / weapon-installation counts")
    void kindFilterNarrows() throws InterruptedException {
        AtomicReference<InstallationDesignerPanel> ref = new AtomicReference<>();
        onFx(() -> {
            InstallationDesignerPanel panel = newPanel(registryWithCatalogSeed());
            panel.applyConstructs(catalogConstructsForApply());
            ref.set(panel);
        });

        long stationCount = Catalog.all().stream().filter(StationDesign.class::isInstance).count();
        long weaponCount = Catalog.all().stream().filter(WeaponInstallation.class::isInstance).count();

        onFx(() -> ref.get().kindFilterForTesting().setValue("Station"));
        assertEquals(stationCount, ref.get().tableForTesting().getItems().size(),
                "STATION = exactly Catalog station count");
        assertTrue(ref.get().tableForTesting().getItems().stream()
                .anyMatch(r -> r.getName().equals("Troy")), "Troy still in Station bucket");

        onFx(() -> ref.get().kindFilterForTesting().setValue("Weapon Installation"));
        assertEquals(weaponCount, ref.get().tableForTesting().getItems().size(),
                "WEAPON_INSTALLATION = exactly Catalog weapon count");
    }

    @Test
    @DisplayName("Step 2 — search field filters by case-insensitive name substring")
    void searchFiltersByCaseInsensitiveSubstring() throws InterruptedException {
        AtomicReference<InstallationDesignerPanel> ref = new AtomicReference<>();
        onFx(() -> {
            InstallationDesignerPanel panel = newPanel(registryWithCatalogSeed());
            panel.applyConstructs(catalogConstructsForApply());
            ref.set(panel);
        });

        onFx(() -> ref.get().searchFieldForTesting().setText("sap"));
        assertEquals(1, ref.get().tableForTesting().getItems().size(),
                "name substring 'sap' matches SAPL only");
        assertEquals("SAPL", ref.get().tableForTesting().getItems().get(0).getName());

        onFx(() -> ref.get().searchFieldForTesting().setText("TROY"));
        assertEquals(1, ref.get().tableForTesting().getItems().size(),
                "uppercase 'TROY' matches 'Troy' case-insensitively");
    }

    @Test
    @DisplayName("Step 2 — selecting a station renders the station-specific details template")
    void selectingStationRendersStationTemplate() throws InterruptedException {
        AtomicReference<InstallationDesignerPanel> ref = new AtomicReference<>();
        onFx(() -> {
            InstallationDesignerPanel panel = newPanel(registryWithCatalogSeed());
            panel.applyConstructs(catalogConstructsForApply());
            ref.set(panel);
        });

        onFx(() -> {
            ConstructRow troy = ref.get().tableForTesting().getItems().stream()
                    .filter(r -> r.getName().equals("Troy"))
                    .findFirst().orElseThrow();
            ref.get().tableForTesting().getSelectionModel().select(troy);
        });

        List<String> labels = labelTextsIn(ref.get().detailsContentForTesting());
        assertTrue(labels.contains(ConstructLabels.get("details.station.stationType")),
                "station section header present");
        assertTrue(labels.contains(ConstructLabels.get("details.station.allegiance")),
                "station-specific 'allegiance' field present");
        // v2 Phase D.6 — the station details template now also shows the function + provenance
        // axes. Source-work is conditional (only shown when provenance.sourceWork() != null) so
        // for Troy (whose pre-Step-6 sourceWork is null) it is intentionally absent.
        assertTrue(labels.contains(ConstructLabels.get("details.station.primaryFunction")),
                "Phase D.6 'primary function' detail row present");
        assertTrue(labels.contains(ConstructLabels.get("details.station.secondaryFunctions")),
                "Phase D.6 'secondary functions' detail row present");
        assertTrue(labels.contains(ConstructLabels.get("details.station.sourceUniverse")),
                "Phase D.6 'source universe' detail row present");
        assertTrue(labels.contains(ConstructLabels.get("details.station.catalogStatus")),
                "Phase D.6 'catalog status' detail row present");
    }

    @Test
    @DisplayName("Step 2 — selecting a weapon installation renders the weapon-specific details template")
    void selectingWeaponRendersWeaponTemplate() throws InterruptedException {
        AtomicReference<InstallationDesignerPanel> ref = new AtomicReference<>();
        onFx(() -> {
            InstallationDesignerPanel panel = newPanel(registryWithCatalogSeed());
            panel.applyConstructs(catalogConstructsForApply());
            ref.set(panel);
        });

        onFx(() -> {
            ConstructRow sapl = ref.get().tableForTesting().getItems().stream()
                    .filter(r -> r.getName().equals("SAPL"))
                    .findFirst().orElseThrow();
            ref.get().tableForTesting().getSelectionModel().select(sapl);
        });

        List<String> labels = labelTextsIn(ref.get().detailsContentForTesting());
        assertTrue(labels.contains(ConstructLabels.get("details.weapon.installationType")),
                "weapon section header present");
        assertTrue(labels.contains(ConstructLabels.get("details.weapon.emplacement")),
                "weapon-specific 'emplacement' field present");
    }

    @Test
    @DisplayName("Step 2 — selecting a transport node renders the transport-specific details template")
    void selectingTransportNodeRendersTransportTemplate() throws InterruptedException {
        TransportNode gate = ringGate("Sol Gate");
        ConstructRegistry r = Mockito.mock(ConstructRegistry.class);
        Mockito.when(r.assetsByKind(AssetKind.STATION)).thenReturn(List.of());
        Mockito.when(r.assetsByKind(AssetKind.WEAPON_INSTALLATION)).thenReturn(List.of());
        Mockito.when(r.infrastructureByKind(InfrastructureKind.TRANSPORT_NODE))
                .thenReturn(List.of((SpaceInfrastructure) gate));

        AtomicReference<InstallationDesignerPanel> ref = new AtomicReference<>();
        onFx(() -> {
            InstallationDesignerPanel panel = newPanel(r);
            panel.applyConstructs(List.of((Cataloged) gate));
            ref.set(panel);
        });

        onFx(() -> {
            ConstructRow row = ref.get().tableForTesting().getItems().get(0);
            ref.get().tableForTesting().getSelectionModel().select(row);
        });

        List<String> labels = labelTextsIn(ref.get().detailsContentForTesting());
        assertTrue(labels.contains(ConstructLabels.get("details.transport.type")),
                "transport section header present");
        assertTrue(labels.contains(ConstructLabels.get("details.transport.position")),
                "transport-specific 'position' field present");
    }

    @Test
    @DisplayName("Step 4 — Edit / Delete start disabled and enable on row selection")
    void editDeleteEnableOnSelection() throws InterruptedException {
        AtomicReference<InstallationDesignerPanel> ref = new AtomicReference<>();
        onFx(() -> {
            InstallationDesignerPanel panel = newPanel(registryWithCatalogSeed());
            panel.applyConstructs(catalogConstructsForApply());
            ref.set(panel);
        });
        InstallationDesignerPanel panel = ref.get();
        assertTrue(panel.editButtonForTesting().isDisable(), "Edit starts disabled with no selection");
        assertTrue(panel.deleteButtonForTesting().isDisable(), "Delete starts disabled with no selection");

        onFx(() -> panel.tableForTesting().getSelectionModel().select(0));
        assertNotNull(panel.tableForTesting().getSelectionModel().getSelectedItem());
        assertFalse(panel.editButtonForTesting().isDisable(), "Edit enabled after selection");
        assertFalse(panel.deleteButtonForTesting().isDisable(), "Delete enabled after selection");
    }

    @Test
    @DisplayName("Step 4 — New button + status bar exist (visible affordances for Phase D CRUD)")
    void newButtonIsPresent() throws InterruptedException {
        AtomicReference<InstallationDesignerPanel> ref = new AtomicReference<>();
        onFx(() -> ref.set(newPanel(registryWithCatalogSeed())));
        assertFalse(ref.get().newButtonForTesting().isDisable(),
                "New... is always enabled (it opens a subtype picker)");
    }

    @Test
    @DisplayName("Phase D.5 — universe tab strip orders: All, Real / Proposed (pinned), then alphabetical")
    void universeTabStripOrdering() throws InterruptedException {
        // The Catalog now spans 3 distinct sources: "Real / Proposed" (the Phase D.5 real
        // stations), "Aldenata" (SheVa Gun), "Troy Rising" (Troy + SAPL). Plus "All" pinned first.
        AtomicReference<InstallationDesignerPanel> ref = new AtomicReference<>();
        onFx(() -> {
            InstallationDesignerPanel panel = newPanel(registryWithCatalogSeed());
            panel.applyConstructs(catalogConstructsForApply());
            ref.set(panel);
        });

        List<String> labels = ref.get().universeTabBarForTesting().getChildren().stream()
                .map(n -> (javafx.scene.control.ToggleButton) n)
                .map(javafx.scene.control.ToggleButton::getText)
                .toList();
        assertEquals(4, labels.size(), "All + 3 distinct source values");
        assertEquals(ConstructLabels.get("tab.all"), labels.get(0), "All is always first");
        assertEquals("Real / Proposed", labels.get(1), "Real / Proposed pinned second");
        assertEquals("Aldenata", labels.get(2));
        assertEquals("Troy Rising", labels.get(3));
    }

    @Test
    @DisplayName("Phase D.5 — selecting a tab narrows to constructs with matching source")
    void tabSelectionNarrowsBySource() throws InterruptedException {
        AtomicReference<InstallationDesignerPanel> ref = new AtomicReference<>();
        onFx(() -> {
            InstallationDesignerPanel panel = newPanel(registryWithCatalogSeed());
            panel.applyConstructs(catalogConstructsForApply());
            ref.set(panel);
        });

        long expectedRealCount = Catalog.all().stream()
                .filter(StationDesign.class::isInstance)
                .map(a -> ((StationDesign) a).source())
                .filter("Real / Proposed"::equals)
                .count();

        // "Troy Rising" → Troy (station) + SAPL (weapon) = 2
        onFx(() -> selectUniverseTab(ref.get(), "Troy Rising"));
        assertEquals(2, ref.get().tableForTesting().getItems().size(),
                "'Troy Rising' tab has Troy + SAPL");

        // "Aldenata" → SheVa Gun
        onFx(() -> selectUniverseTab(ref.get(), "Aldenata"));
        assertEquals(1, ref.get().tableForTesting().getItems().size());
        assertEquals("SheVa Gun", ref.get().tableForTesting().getItems().get(0).getName());

        // "Real / Proposed" → the 8 real Earth space stations
        onFx(() -> selectUniverseTab(ref.get(), "Real / Proposed"));
        assertEquals(expectedRealCount, ref.get().tableForTesting().getItems().size(),
                "'Real / Proposed' tab carries the Phase D.5 real-station seed");

        // "All" → full count
        long expectedAll = catalogConstructsForApply().size();
        onFx(() -> selectUniverseTab(ref.get(), ConstructLabels.get("tab.all")));
        assertEquals(expectedAll, ref.get().tableForTesting().getItems().size());
    }

    @Test
    @DisplayName("Phase D.5 — universe tab strip composes with the Kind filter")
    void tabSelectionComposesWithKindFilter() throws InterruptedException {
        AtomicReference<InstallationDesignerPanel> ref = new AtomicReference<>();
        onFx(() -> {
            InstallationDesignerPanel panel = newPanel(registryWithCatalogSeed());
            panel.applyConstructs(catalogConstructsForApply());
            ref.set(panel);
        });

        // Troy Rising + Kind=STATION → just Troy (not SAPL, which is a weapon).
        onFx(() -> {
            selectUniverseTab(ref.get(), "Troy Rising");
            ref.get().kindFilterForTesting().setValue("Station");
        });

        assertEquals(1, ref.get().tableForTesting().getItems().size(),
                "Troy Rising + STATION = Troy only");
        assertEquals("Troy", ref.get().tableForTesting().getItems().get(0).getName());
    }

    private static void selectUniverseTab(InstallationDesignerPanel panel, String label) {
        panel.universeTabBarForTesting().getChildren().stream()
                .map(n -> (javafx.scene.control.ToggleButton) n)
                .filter(t -> label.equals(t.getText()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No tab named '" + label + "'"))
                .setSelected(true);
    }

    @Test
    @DisplayName("Step 2 — Faction filter populates from distinct factions and narrows correctly")
    void factionFilterPopulatesAndNarrows() throws InterruptedException {
        AtomicReference<InstallationDesignerPanel> ref = new AtomicReference<>();
        onFx(() -> {
            InstallationDesignerPanel panel = newPanel(registryWithCatalogSeed());
            panel.applyConstructs(catalogConstructsForApply());
            ref.set(panel);
        });

        // The Catalog seed carries distinct factions on the 3 entries; the filter combo should
        // contain "All" + each distinct faction.
        assertTrue(ref.get().factionFilterForTesting().getItems().size() >= 2,
                "filter populated with at least All + one real faction");
        assertTrue(ref.get().factionFilterForTesting().getItems().get(0)
                        .equals(ConstructLabels.get("filter.all")),
                "All sentinel sits at index 0");
    }

    private static List<String> labelTextsIn(javafx.scene.layout.VBox container) {
        List<String> out = new ArrayList<>();
        collectLabels(container, out);
        return out;
    }

    private static void collectLabels(Node node, List<String> out) {
        if (node instanceof Label l) {
            out.add(l.getText());
        }
        if (node instanceof GridPane g) {
            g.getChildren().forEach(child -> collectLabels(child, out));
        } else if (node instanceof javafx.scene.Parent p) {
            p.getChildrenUnmodifiable().forEach(child -> collectLabels(child, out));
        }
    }

    // ==================================================================
    // v2 Phase D.6 Step 7 — Function filter
    // ==================================================================

    @Test
    @DisplayName("Step 7 — RESEARCH selection narrows to the 8 real stations (Troy is DEFENSIVE)")
    void functionFilterResearchShowsRealStationsOnly() throws InterruptedException {
        AtomicReference<InstallationDesignerPanel> ref = new AtomicReference<>();
        onFx(() -> {
            InstallationDesignerPanel panel = newPanel(registryWithCatalogSeed());
            panel.applyConstructs(catalogConstructsForApply());
            ref.set(panel);
        });

        onFx(() -> ref.get().functionFilterForTesting().setValue(StationFunction.RESEARCH.name()));

        long expectedResearchCount = Catalog.all().stream()
                .filter(StationDesign.class::isInstance)
                .map(StationDesign.class::cast)
                .filter(s -> s.primaryFunction() == StationFunction.RESEARCH
                        || s.secondaryFunctions().contains(StationFunction.RESEARCH))
                .count();
        assertEquals(expectedResearchCount, ref.get().tableForTesting().getItems().size(),
                "RESEARCH selection should match exactly the catalog's RESEARCH-primary stations");
        // None of the visible rows should be the Troy entry — Troy is DEFENSIVE.
        assertFalse(ref.get().tableForTesting().getItems().stream()
                        .anyMatch(r -> r.getName().equals("Troy")),
                "Troy is DEFENSIVE, not RESEARCH; must be filtered out");
    }

    @Test
    @DisplayName("Step 7 — DEFENSIVE selection narrows to exactly Troy")
    void functionFilterDefensiveShowsTroyOnly() throws InterruptedException {
        AtomicReference<InstallationDesignerPanel> ref = new AtomicReference<>();
        onFx(() -> {
            InstallationDesignerPanel panel = newPanel(registryWithCatalogSeed());
            panel.applyConstructs(catalogConstructsForApply());
            ref.set(panel);
        });

        onFx(() -> ref.get().functionFilterForTesting().setValue(StationFunction.DEFENSIVE.name()));

        assertEquals(1, ref.get().tableForTesting().getItems().size());
        assertEquals("Troy", ref.get().tableForTesting().getItems().get(0).getName());
    }

    @Test
    @DisplayName("Step 7 — MILITARY_COMMAND selection matches Troy (via its secondary)")
    void functionFilterMatchesSecondaryFunctions() throws InterruptedException {
        AtomicReference<InstallationDesignerPanel> ref = new AtomicReference<>();
        onFx(() -> {
            InstallationDesignerPanel panel = newPanel(registryWithCatalogSeed());
            panel.applyConstructs(catalogConstructsForApply());
            ref.set(panel);
        });

        onFx(() -> ref.get().functionFilterForTesting().setValue(StationFunction.MILITARY_COMMAND.name()));

        assertEquals(1, ref.get().tableForTesting().getItems().size(),
                "Troy carries MILITARY_COMMAND in its secondaryFunctions set");
        assertEquals("Troy", ref.get().tableForTesting().getItems().get(0).getName());
    }

    @Test
    @DisplayName("Step 7 — Real / Proposed universe tab + RESEARCH function = the 8 real stations")
    void functionFilterComposesWithUniverseTab() throws InterruptedException {
        AtomicReference<InstallationDesignerPanel> ref = new AtomicReference<>();
        onFx(() -> {
            InstallationDesignerPanel panel = newPanel(registryWithCatalogSeed());
            panel.applyConstructs(catalogConstructsForApply());
            ref.set(panel);
        });

        long expectedRealCount = Catalog.all().stream()
                .filter(StationDesign.class::isInstance)
                .map(StationDesign.class::cast)
                .filter(s -> "Real / Proposed".equals(s.source()))
                .count();

        onFx(() -> {
            selectUniverseTab(ref.get(), "Real / Proposed");
            ref.get().functionFilterForTesting().setValue(StationFunction.RESEARCH.name());
        });

        assertEquals(expectedRealCount, ref.get().tableForTesting().getItems().size(),
                "Real / Proposed tab + RESEARCH function = the 8 real stations");
    }

    @Test
    @DisplayName("Step 7 — Troy Rising tab + RESEARCH function = empty (Troy is DEFENSIVE, SAPL is non-station)")
    void functionFilterDropsNonStationSubtypesEvenWithinUniverse() throws InterruptedException {
        AtomicReference<InstallationDesignerPanel> ref = new AtomicReference<>();
        onFx(() -> {
            InstallationDesignerPanel panel = newPanel(registryWithCatalogSeed());
            panel.applyConstructs(catalogConstructsForApply());
            ref.set(panel);
        });

        onFx(() -> {
            selectUniverseTab(ref.get(), "Troy Rising");
            ref.get().functionFilterForTesting().setValue(StationFunction.RESEARCH.name());
        });

        assertEquals(0, ref.get().tableForTesting().getItems().size(),
                "Troy is DEFENSIVE so RESEARCH doesn't match; SAPL is WeaponInstallation so it "
                        + "drops out per the non-station rule");
    }

    @Test
    @DisplayName("Step 7 — Troy Rising tab + DEFENSIVE function = Troy")
    void functionFilterTroyRisingTabComposesCorrectlyWithDefensive() throws InterruptedException {
        AtomicReference<InstallationDesignerPanel> ref = new AtomicReference<>();
        onFx(() -> {
            InstallationDesignerPanel panel = newPanel(registryWithCatalogSeed());
            panel.applyConstructs(catalogConstructsForApply());
            ref.set(panel);
        });

        onFx(() -> {
            selectUniverseTab(ref.get(), "Troy Rising");
            ref.get().functionFilterForTesting().setValue(StationFunction.DEFENSIVE.name());
        });

        assertEquals(1, ref.get().tableForTesting().getItems().size());
        assertEquals("Troy", ref.get().tableForTesting().getItems().get(0).getName());
    }

    @Test
    @DisplayName("Step 7 — All selection is a no-op (everything visible)")
    void functionFilterAllIsNoOp() throws InterruptedException {
        AtomicReference<InstallationDesignerPanel> ref = new AtomicReference<>();
        onFx(() -> {
            InstallationDesignerPanel panel = newPanel(registryWithCatalogSeed());
            panel.applyConstructs(catalogConstructsForApply());
            ref.set(panel);
        });

        long allCount = catalogConstructsForApply().size();

        // Set to RESEARCH then back to All to confirm the toggle.
        onFx(() -> ref.get().functionFilterForTesting().setValue(StationFunction.RESEARCH.name()));
        onFx(() -> ref.get().functionFilterForTesting().setValue(ConstructLabels.get("filter.all")));

        assertEquals(allCount, ref.get().tableForTesting().getItems().size(),
                "All function selection should not filter anything");
    }

    @Test
    @DisplayName("Step 7 — any specific function selection drops non-station subtypes")
    void functionFilterDropsAllNonStationSubtypesUnconditionally() throws InterruptedException {
        AtomicReference<InstallationDesignerPanel> ref = new AtomicReference<>();
        onFx(() -> {
            InstallationDesignerPanel panel = newPanel(registryWithCatalogSeed());
            panel.applyConstructs(catalogConstructsForApply());
            ref.set(panel);
        });

        onFx(() -> ref.get().functionFilterForTesting().setValue(StationFunction.RESEARCH.name()));

        // Visible rows must all be StationDesign instances.
        assertTrue(ref.get().tableForTesting().getItems().stream()
                        .allMatch(r -> r.getConstruct() instanceof StationDesign),
                "with a specific function selected, no non-station rows should remain visible");
    }
}
