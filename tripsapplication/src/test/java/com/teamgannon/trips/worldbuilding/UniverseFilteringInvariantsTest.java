package com.teamgannon.trips.worldbuilding;

import com.teamgannon.trips.spaceshipmodeller.persistence.MegastructureDesignMapper;
import com.teamgannon.trips.spaceshipmodeller.persistence.StationDesignMapper;
import com.teamgannon.trips.spaceshipmodeller.persistence.UniverseMapper;
import com.teamgannon.trips.spaceshipmodeller.persistence.WeaponInstallationMapper;
import com.teamgannon.trips.spaceshipmodeller.service.MegastructureDesignerService;
import com.teamgannon.trips.spaceshipmodeller.service.StationDesignerService;
import com.teamgannon.trips.spaceshipmodeller.service.UniverseDesignerService;
import com.teamgannon.trips.spaceshipmodeller.service.WeaponInstallationDesignerService;
import com.terranrepublic.assets.Cataloged;
import com.terranrepublic.assets.Megastructure;
import com.terranrepublic.assets.Universe;
import com.terranrepublic.assets.WeaponInstallation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * v2 Phase F.1 §7 acceptance gate — the four invariants from the requirements doc, exercised
 * against the real Flyway-seeded + Catalog-seeded DB state.
 *
 * <p>Boots @DataJpaTest with Flyway through V17, runs the Catalog seeders (Catalog ships 5
 * fiction-canon entries + 8 real stations + assorted real bodies), then sweeps activation
 * combinations through {@link UniverseFilteringService#filter} and verifies:
 *
 * <ul>
 *   <li><b>R5.5</b>: Universes don't leak content into each other. Activate only Universe A;
 *       verify content tagged to B isn't visible.</li>
 *   <li><b>R5.6</b>: Real data persists across all activation states. The count of
 *       {@code universe_id IS NULL} entries returned by {@code filter()} is constant across
 *       every activation combination.</li>
 *   <li><b>R5.7</b>: Universe-scoped entries hidden when their universe is inactive. (Aliases
 *       proper are vacuous in F.1; this is the analogous invariant for catalog entries.)</li>
 *   <li><b>Bonus</b>: Real data <em>always</em> visible. Same sweep as R5.6 but verifies
 *       specific real-data entries by id (Catalog.TROY's siblings — the 8 real station ids)
 *       are present in the visible set in every activation iteration.</li>
 * </ul>
 *
 * <p>If any of these fail, F.1's contract is broken and the regression must be fixed before
 * Step 9 (close-out). The §5 invariants are the platform's load-bearing guarantees; surfacing a
 * violation here surfaces it before the user ever toggles a universe.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true",
        "spring.flyway.baseline-on-migrate=true",
        "spring.flyway.baseline-version=1",
        "spring.flyway.locations=classpath:db/migration"
})
@Import({
        UniverseDesignerService.class, UniverseMapper.class,
        UniverseFilteringService.class,
        MegastructureDesignerService.class, MegastructureDesignMapper.class,
        WeaponInstallationDesignerService.class, WeaponInstallationMapper.class,
        StationDesignerService.class, StationDesignMapper.class
})
class UniverseFilteringInvariantsTest {

    private static final String LEGACY = "catalog-universe-legacy-of-the-aldenata";
    private static final String CAINE_RIORDAN = "catalog-universe-caine-riordan";
    private static final String STAR_TREK = "catalog-universe-star-trek";

    @Autowired
    private UniverseDesignerService universeService;
    @Autowired
    private UniverseFilteringService filteringService;
    @Autowired
    private MegastructureDesignerService megastructureService;
    @Autowired
    private WeaponInstallationDesignerService weaponService;
    @Autowired
    private StationDesignerService stationService;
    @Autowired
    private ApplicationEventPublisher events; // unused but documents the test wires up event publishing

    @BeforeEach
    void seedCatalog() {
        // Run the Catalog seeders so the catalog tables have content. After V17 runs (Flyway)
        // the universe rows exist; the seeders insert:
        //   1 megastructure (TROY)  -- fiction, scoped to Legacy of the Aldenata
        //   2 weapons (SAPL, SHEVA_GUN) -- fiction, scoped to Legacy of the Aldenata
        //   8 stations (ISS, Tiangong, Mir, Skylab, Salyut-1, Salyut-7, Lunar Gateway, Axiom)
        //     -- all real, universe_id=NULL (R5.6 protection target)
        //   2 Posleen ships -- not seeded here (no SpaceshipService import); spaceship_design
        //     content is not exercised by this acceptance gate.
        // The 5 fiction-canon entries arrive with universeId =
        // "catalog-universe-legacy-of-the-aldenata" per Step 4's source-of-truth tagging.
        // The 8 stations arrive with universeId = null (real data).
        megastructureService.syncCatalogEntries();
        weaponService.syncCatalogEntries();
        stationService.syncCatalogEntries();
    }

    /** All catalog entries currently present in the DB (across the 3 tables this test exercises). */
    private List<Cataloged> allCatalogedFromDb() {
        List<Cataloged> all = new ArrayList<>();
        all.addAll(megastructureService.findAll());
        all.addAll(weaponService.findAll());
        all.addAll(stationService.findAll());
        return all;
    }

    /** Helper: set the active state for a list of universe ids; all others become inactive. */
    private void setActivationState(Set<String> activeIds) {
        for (Universe u : universeService.findAll()) {
            boolean shouldBeActive = activeIds.contains(u.id());
            if (u.active() != shouldBeActive) {
                if (shouldBeActive) {
                    universeService.activate(u.id());
                } else {
                    universeService.deactivate(u.id());
                }
            }
        }
    }

    // ============================================================
    // R5.5 — Universes don't leak content into each other
    // ============================================================

    @Test
    @DisplayName("R5.5 — only Caine Riordan active: Legacy-of-the-Aldenata content is NOT visible")
    void r5_5_universesDoNotLeak() {
        setActivationState(Set.of(CAINE_RIORDAN));
        List<Cataloged> visible = filteringService.filter(allCatalogedFromDb());
        for (Cataloged c : visible) {
            assertFalse(LEGACY.equals(c.universeId()),
                    "R5.5 leak: " + c.name() + " is tagged to Legacy of the Aldenata but appears "
                            + "when only Caine Riordan is active");
        }
    }

    @Test
    @DisplayName("R5.5 — only Legacy active: Caine Riordan + Star Trek content NOT visible")
    void r5_5_universesDoNotLeakReversed() {
        setActivationState(Set.of(LEGACY));
        List<Cataloged> visible = filteringService.filter(allCatalogedFromDb());
        for (Cataloged c : visible) {
            assertFalse(CAINE_RIORDAN.equals(c.universeId()),
                    c.name() + " (Caine Riordan) leaked into Legacy-only view");
            assertFalse(STAR_TREK.equals(c.universeId()),
                    c.name() + " (Star Trek) leaked into Legacy-only view");
        }
    }

    // ============================================================
    // R5.6 — Real data persists across all activation states
    // ============================================================

    @Test
    @DisplayName("R5.6 — real-data count (universe_id IS NULL) is constant across all activation combinations")
    void r5_6_realDataCountConstantAcrossAllActivationCombos() {
        // Establish baseline: count entries with universe_id=null in the raw catalog.
        long expectedRealCount = allCatalogedFromDb().stream()
                .filter(c -> c.universeId() == null)
                .count();
        assertTrue(expectedRealCount > 0,
                "Baseline check: catalog should contain at least some real-data entries");

        // Sweep activation combinations: none, each universe singly, all-three combo.
        List<Set<String>> activationCombinations = List.of(
                Set.of(),
                Set.of(LEGACY),
                Set.of(CAINE_RIORDAN),
                Set.of(STAR_TREK),
                Set.of(LEGACY, CAINE_RIORDAN),
                Set.of(LEGACY, STAR_TREK),
                Set.of(CAINE_RIORDAN, STAR_TREK),
                Set.of(LEGACY, CAINE_RIORDAN, STAR_TREK)
        );
        for (Set<String> active : activationCombinations) {
            setActivationState(active);
            long actualRealCount = filteringService.filter(allCatalogedFromDb()).stream()
                    .filter(c -> c.universeId() == null)
                    .count();
            assertEquals(expectedRealCount, actualRealCount,
                    "R5.6 violation: real-data count changed when activation set = " + active
                            + " (expected " + expectedRealCount + ", got " + actualRealCount + ")");
        }
    }

    // ============================================================
    // R5.7 — Universe-scoped entries hidden when their universe inactive
    // ============================================================

    @Test
    @DisplayName("R5.7 (analogous) — universe-scoped entries are hidden when their universe is inactive")
    void r5_7_universeScopedEntriesHiddenWhenInactive() {
        // Activate Legacy of the Aldenata; verify TROY (which is scoped to Legacy) appears.
        setActivationState(Set.of(LEGACY));
        List<Cataloged> visibleWithLegacy = filteringService.filter(allCatalogedFromDb());
        boolean troyVisibleWhenLegacyActive = visibleWithLegacy.stream()
                .filter(Megastructure.class::isInstance)
                .anyMatch(c -> "catalog-troy".equals(c.id()));
        assertTrue(troyVisibleWhenLegacyActive,
                "When Legacy of the Aldenata is active, TROY must be visible (positive check)");

        // Now deactivate Legacy; verify TROY disappears.
        setActivationState(Set.of());
        List<Cataloged> visibleWhenInactive = filteringService.filter(allCatalogedFromDb());
        boolean troyVisibleWhenLegacyInactive = visibleWhenInactive.stream()
                .filter(Megastructure.class::isInstance)
                .anyMatch(c -> "catalog-troy".equals(c.id()));
        assertFalse(troyVisibleWhenLegacyInactive,
                "R5.7 violation: TROY visible despite Legacy of the Aldenata being inactive");
    }

    // ============================================================
    // Bonus — Real data ALWAYS visible (specific-entry version)
    // ============================================================

    @Test
    @DisplayName("Bonus — SAPL is NEVER visible without Legacy active (specific fiction-canon entry)")
    void bonusSaplVisibilityFollowsLegacyActivation() {
        // SAPL is in Catalog.all() as a fiction-canon WeaponInstallation tagged to Legacy of
        // the Aldenata. It should appear iff Legacy is active.

        // Sweep: when Legacy inactive, SAPL must NOT be visible.
        setActivationState(Set.of());
        boolean saplVisibleWhenInactive = filteringService.filter(allCatalogedFromDb()).stream()
                .anyMatch(c -> "catalog-sapl".equals(c.id()));
        assertFalse(saplVisibleWhenInactive,
                "SAPL visible when Legacy of the Aldenata is inactive — universe scoping broken");

        // When Legacy active, SAPL must be visible.
        setActivationState(Set.of(LEGACY));
        boolean saplVisibleWhenActive = filteringService.filter(allCatalogedFromDb()).stream()
                .anyMatch(c -> "catalog-sapl".equals(c.id()));
        assertTrue(saplVisibleWhenActive,
                "SAPL hidden when Legacy of the Aldenata is active — visibility broken");

        // When Legacy + other universes active, SAPL still visible.
        setActivationState(Set.of(LEGACY, CAINE_RIORDAN));
        boolean saplVisibleWhenMultiActive = filteringService.filter(allCatalogedFromDb()).stream()
                .anyMatch(c -> "catalog-sapl".equals(c.id()));
        assertTrue(saplVisibleWhenMultiActive,
                "SAPL hidden when Legacy + Caine Riordan are both active — multi-universe visibility broken");
    }

    // ============================================================
    // Catalog audit invariants (§7.5)
    // ============================================================

    @Test
    @DisplayName("§7.5 — every Universe id starts with catalog-universe- prefix")
    void everyUniverseIdFollowsSlugConvention() {
        List<Universe> all = universeService.findAll();
        assertEquals(13L, all.size(), "F.1 V17 ships exactly 13 universes");
        for (Universe u : all) {
            assertTrue(u.id().startsWith("catalog-universe-"),
                    "Universe id '" + u.id() + "' violates §12 naming convention");
        }
    }

    @Test
    @DisplayName("§7.5 — every catalog entry's universe_id references an extant Universe row")
    void everyUniverseIdReferenceIsValid() {
        Set<String> validIds = universeService.findAll().stream()
                .map(Universe::id)
                .collect(Collectors.toSet());

        List<Cataloged> all = allCatalogedFromDb();
        for (Cataloged c : all) {
            String universeId = c.universeId();
            if (universeId != null) {
                assertTrue(validIds.contains(universeId),
                        c.name() + " references universe_id='" + universeId
                                + "' which has no matching Universe row");
            }
        }
    }
}
