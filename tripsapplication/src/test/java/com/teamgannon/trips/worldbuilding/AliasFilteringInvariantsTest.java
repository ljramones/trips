package com.teamgannon.trips.worldbuilding;

import com.teamgannon.trips.spaceshipmodeller.persistence.AliasMapper;
import com.teamgannon.trips.spaceshipmodeller.persistence.AliasRepository;
import com.teamgannon.trips.spaceshipmodeller.persistence.MegastructureDesignMapper;
import com.teamgannon.trips.spaceshipmodeller.persistence.StationDesignMapper;
import com.teamgannon.trips.spaceshipmodeller.persistence.UniverseEntity;
import com.teamgannon.trips.spaceshipmodeller.persistence.UniverseMapper;
import com.teamgannon.trips.spaceshipmodeller.persistence.UniverseRepository;
import com.teamgannon.trips.spaceshipmodeller.persistence.WeaponInstallationMapper;
import com.teamgannon.trips.spaceshipmodeller.service.AliasDesignerService;
import com.teamgannon.trips.spaceshipmodeller.service.MegastructureDesignerService;
import com.teamgannon.trips.spaceshipmodeller.service.StationDesignerService;
import com.teamgannon.trips.spaceshipmodeller.service.UniverseDesignerService;
import com.teamgannon.trips.spaceshipmodeller.service.WeaponInstallationDesignerService;
import com.terranrepublic.assets.Alias;
import com.terranrepublic.assets.AliasTargetKind;
import com.terranrepublic.assets.Cataloged;
import com.terranrepublic.assets.Universe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * v2 Phase F.2 §9 acceptance gate — the load-bearing invariants for the Aliases feature,
 * exercised against the real Flyway-seeded (through V18) + Catalog-seeded DB state.
 *
 * <p>Mirrors {@link UniverseFilteringInvariantsTest}'s harness (boot @DataJpaTest with Flyway
 * through V18, run Catalog seeders, sweep activation combinations) and adds F.2-specific
 * invariants:
 *
 * <ul>
 *   <li><b>R5.5</b>: Universes don't leak aliases into each other. Create aliases in A + B;
 *       activate only A; verify only A's aliases are visible via
 *       {@link AliasDesignerService#findActiveAliasesForTarget}.</li>
 *   <li><b>R5.6</b>: Real catalog data unaffected by alias activity. Catalog table counts
 *       (megastructures, weapons, stations) remain constant regardless of alias creates.</li>
 *   <li><b>R5.7</b>: Aliases hidden when their universe is inactive. <b>First substantive
 *       test of R5.7</b> — F.1's was vacuous because no universe-scoped entries existed at
 *       F.1 time; F.2's aliases are the first content category that's never canonical/real.</li>
 *   <li><b>FK ON DELETE CASCADE</b>: Deleting a Universe row deletes all its aliases. Distinct
 *       from F.1's V16 ON DELETE SET NULL (catalog entries can exist without a universe;
 *       aliases cannot).</li>
 *   <li><b>Uniqueness constraint</b>: V18's {@code uk_alias_universe_target} unique constraint
 *       rejects duplicate (universe, target) pairs; the two-layer pattern's service-level
 *       pre-check surfaces a friendly {@link IllegalStateException}.</li>
 * </ul>
 *
 * <p>F.2 ships no alias seed data (V18 creates the table empty); the test creates aliases
 * programmatically against the F.1-seeded universe rows.
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
        AliasDesignerService.class, AliasMapper.class,
        MegastructureDesignerService.class, MegastructureDesignMapper.class,
        WeaponInstallationDesignerService.class, WeaponInstallationMapper.class,
        StationDesignerService.class, StationDesignMapper.class
})
class AliasFilteringInvariantsTest {

    private static final String LEGACY = "catalog-universe-legacy-of-the-aldenata";
    private static final String CAINE_RIORDAN = "catalog-universe-caine-riordan";
    private static final String STAR_TREK = "catalog-universe-star-trek";

    @Autowired private UniverseDesignerService universeService;
    @Autowired private UniverseFilteringService filteringService;
    @Autowired private AliasDesignerService aliasService;
    @Autowired private AliasRepository aliasRepository;
    @Autowired private UniverseRepository universeRepository;
    @Autowired private MegastructureDesignerService megastructureService;
    @Autowired private WeaponInstallationDesignerService weaponService;
    @Autowired private StationDesignerService stationService;

    @BeforeEach
    void seedCatalog() {
        megastructureService.syncCatalogEntries();
        weaponService.syncCatalogEntries();
        stationService.syncCatalogEntries();
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

    /** Helper: catalog entries currently in the DB (excluding aliases). */
    private List<Cataloged> realCatalogEntries() {
        List<Cataloged> all = new ArrayList<>();
        all.addAll(megastructureService.findAll());
        all.addAll(weaponService.findAll());
        all.addAll(stationService.findAll());
        return all;
    }

    private Alias newAlias(String universeId, AliasTargetKind kind, String targetId, String text) {
        return new Alias(universeId, kind, targetId, text, "");
    }

    // ============================================================
    // R5.5 — Universes don't leak aliases
    // ============================================================

    @Test
    @DisplayName("R5.5 — only Legacy active: aliases in Caine Riordan + Star Trek are NOT visible")
    void r5_5_aliasesDoNotLeak_LegacyOnly() {
        String starId = "star-r5-5-shared";
        aliasService.save(newAlias(LEGACY, AliasTargetKind.STAR, starId, "Legacy-name"));
        aliasService.save(newAlias(CAINE_RIORDAN, AliasTargetKind.STAR, starId, "Caine-name"));
        aliasService.save(newAlias(STAR_TREK, AliasTargetKind.STAR, starId, "Trek-name"));

        setActivationState(Set.of(LEGACY));
        List<Alias> visible = aliasService.findActiveAliasesForTarget(AliasTargetKind.STAR, starId);

        assertEquals(1, visible.size(), "only Legacy alias should be visible");
        assertEquals("Legacy-name", visible.get(0).aliasText());
        assertEquals(LEGACY, visible.get(0).universeId());
    }

    @Test
    @DisplayName("R5.5 — only Caine Riordan active: Legacy + Star Trek aliases NOT visible")
    void r5_5_aliasesDoNotLeak_CaineOnly() {
        String starId = "star-r5-5-shared-2";
        aliasService.save(newAlias(LEGACY, AliasTargetKind.STAR, starId, "Legacy-name"));
        aliasService.save(newAlias(CAINE_RIORDAN, AliasTargetKind.STAR, starId, "Caine-name"));

        setActivationState(Set.of(CAINE_RIORDAN));
        List<Alias> visible = aliasService.findActiveAliasesForTarget(AliasTargetKind.STAR, starId);

        assertEquals(1, visible.size());
        assertEquals(CAINE_RIORDAN, visible.get(0).universeId());
        for (Alias a : visible) {
            assertFalse(LEGACY.equals(a.universeId()),
                    "Legacy alias leaked into Caine Riordan-only view: " + a.aliasText());
        }
    }

    @Test
    @DisplayName("R5.5 — multi-universe active: each universe's aliases visible; no cross-contamination")
    void r5_5_multiUniverseActive() {
        String starId = "star-r5-5-multi";
        aliasService.save(newAlias(LEGACY, AliasTargetKind.STAR, starId, "Legacy-name"));
        aliasService.save(newAlias(CAINE_RIORDAN, AliasTargetKind.STAR, starId, "Caine-name"));
        aliasService.save(newAlias(STAR_TREK, AliasTargetKind.STAR, starId, "Trek-name"));

        setActivationState(Set.of(LEGACY, CAINE_RIORDAN));
        List<Alias> visible = aliasService.findActiveAliasesForTarget(AliasTargetKind.STAR, starId);

        assertEquals(2, visible.size(), "Legacy + Caine Riordan aliases both visible");
        assertTrue(visible.stream().anyMatch(a -> "Legacy-name".equals(a.aliasText())));
        assertTrue(visible.stream().anyMatch(a -> "Caine-name".equals(a.aliasText())));
        assertFalse(visible.stream().anyMatch(a -> "Trek-name".equals(a.aliasText())),
                "Star Trek alias must not appear");
    }

    // ============================================================
    // R5.6 — Real catalog data unaffected by alias activity
    // ============================================================

    @Test
    @DisplayName("R5.6 — creating aliases doesn't change real catalog (megastructures/weapons/stations) counts")
    void r5_6_realCatalogUnaffected() {
        long beforeMegas = megastructureService.findAll().size();
        long beforeWeapons = weaponService.findAll().size();
        long beforeStations = stationService.findAll().size();

        // Create aliases in multiple universes — should not touch catalog tables
        aliasService.save(newAlias(LEGACY, AliasTargetKind.STAR, "star-r5-6-a", "AliasA"));
        aliasService.save(newAlias(CAINE_RIORDAN, AliasTargetKind.STAR, "star-r5-6-b", "AliasB"));
        aliasService.save(newAlias(STAR_TREK, AliasTargetKind.EXOPLANET, "ep-r5-6-c", "AliasC"));

        assertEquals(beforeMegas, megastructureService.findAll().size(),
                "R5.6: megastructure count changed by alias creates");
        assertEquals(beforeWeapons, weaponService.findAll().size(),
                "R5.6: weapon count changed by alias creates");
        assertEquals(beforeStations, stationService.findAll().size(),
                "R5.6: station count changed by alias creates");
    }

    @Test
    @DisplayName("R5.6 — filtering aliases doesn't affect real catalog visibility (universe_id=null entries unaffected)")
    void r5_6_realCatalogVisibilityStableAcrossAliasActivity() {
        long expectedRealEntries = realCatalogEntries().stream()
                .filter(c -> c.universeId() == null)
                .count();

        aliasService.save(newAlias(LEGACY, AliasTargetKind.STAR, "star-r5-6-d", "AliasD"));

        List<Set<String>> activationCombinations = List.of(
                Set.<String>of(),
                Set.of(LEGACY),
                Set.of(CAINE_RIORDAN),
                Set.of(LEGACY, CAINE_RIORDAN, STAR_TREK));
        for (Set<String> active : activationCombinations) {
            setActivationState(active);
            long actualRealEntries = filteringService.filter(realCatalogEntries()).stream()
                    .filter(c -> c.universeId() == null)
                    .count();
            assertEquals(expectedRealEntries, actualRealEntries,
                    "R5.6: real catalog count changed when active set = " + active);
        }
    }

    // ============================================================
    // R5.7 — Aliases hidden when universe inactive (substantive for the first time)
    // ============================================================

    @Test
    @DisplayName("R5.7 — alias appears when universe active, disappears when universe inactive")
    void r5_7_aliasFollowsUniverseActivation() {
        String starId = "star-r5-7-toggle";
        aliasService.save(newAlias(LEGACY, AliasTargetKind.STAR, starId, "Legacy-alias-toggle"));

        // Activate Legacy → alias visible
        setActivationState(Set.of(LEGACY));
        List<Alias> visibleActive = aliasService.findActiveAliasesForTarget(AliasTargetKind.STAR, starId);
        assertEquals(1, visibleActive.size());
        assertEquals("Legacy-alias-toggle", visibleActive.get(0).aliasText());

        // Deactivate Legacy → alias hidden
        setActivationState(Set.of());
        List<Alias> visibleInactive = aliasService.findActiveAliasesForTarget(AliasTargetKind.STAR, starId);
        assertEquals(0, visibleInactive.size(),
                "R5.7: alias visible despite owning universe being inactive");

        // Re-activate Legacy → alias visible again (idempotent)
        setActivationState(Set.of(LEGACY));
        List<Alias> visibleReactive = aliasService.findActiveAliasesForTarget(AliasTargetKind.STAR, starId);
        assertEquals(1, visibleReactive.size());
    }

    @Test
    @DisplayName("R5.7 — empty active set: no aliases visible regardless of how many rows exist")
    void r5_7_emptyActiveSetHidesAll() {
        aliasService.save(newAlias(LEGACY, AliasTargetKind.STAR, "star-r5-7-a", "A"));
        aliasService.save(newAlias(CAINE_RIORDAN, AliasTargetKind.STAR, "star-r5-7-b", "B"));
        aliasService.save(newAlias(STAR_TREK, AliasTargetKind.EXOPLANET, "ep-r5-7-c", "C"));

        setActivationState(Set.of());
        // Every target should return empty list
        assertEquals(0, aliasService.findActiveAliasesForTarget(AliasTargetKind.STAR, "star-r5-7-a").size());
        assertEquals(0, aliasService.findActiveAliasesForTarget(AliasTargetKind.STAR, "star-r5-7-b").size());
        assertEquals(0, aliasService.findActiveAliasesForTarget(AliasTargetKind.EXOPLANET, "ep-r5-7-c").size());
    }

    // ============================================================
    // FK ON DELETE CASCADE — F.2 V18 contract (distinct from F.1's SET NULL)
    // ============================================================

    @Test
    @DisplayName("FK CASCADE — deleting a Universe row deletes all its aliases (V18 contract)")
    void onDeleteCascadeRemovesAliases() {
        // Create a transient test universe so we can delete it without affecting V17's 13 ships.
        // ID must fit V15's VARCHAR(64) column, so use a short suffix instead of full UUID.
        UniverseEntity testUniverse = new UniverseEntity("Test Cascade");
        testUniverse.setId("catalog-universe-cascade-" + UUID.randomUUID().toString().substring(0, 8));
        universeRepository.saveAndFlush(testUniverse);
        String testUniverseId = testUniverse.getId();

        aliasService.save(newAlias(testUniverseId, AliasTargetKind.STAR, "star-cascade-1", "Cascade-A"));
        aliasService.save(newAlias(testUniverseId, AliasTargetKind.STAR, "star-cascade-2", "Cascade-B"));
        // Also create an alias in Legacy that should SURVIVE the cascade
        aliasService.save(newAlias(LEGACY, AliasTargetKind.STAR, "star-cascade-3", "Survives"));

        assertEquals(2, aliasService.findByUniverseId(testUniverseId).size(),
                "before delete: 2 test-universe aliases");

        // Delete the test universe via repository — triggers ON DELETE CASCADE on alias.universe_id
        universeRepository.deleteById(testUniverseId);
        universeRepository.flush();

        assertEquals(0, aliasService.findByUniverseId(testUniverseId).size(),
                "FK CASCADE: aliases for deleted universe must be gone");
        // Other universes' aliases must survive
        List<Alias> legacyAliases = aliasService.findByUniverseId(LEGACY);
        assertTrue(legacyAliases.stream().anyMatch(a -> "Survives".equals(a.aliasText())),
                "Legacy alias must survive the cascade of an unrelated universe deletion");
    }

    // ============================================================
    // Uniqueness constraint — V18 uk_alias_universe_target
    // ============================================================

    @Test
    @DisplayName("Uniqueness — duplicate (universe, target) save throws IllegalStateException with friendly message")
    void uniquenessRejectsDuplicate() {
        String starId = "star-uniqueness-test";
        aliasService.save(newAlias(LEGACY, AliasTargetKind.STAR, starId, "First"));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> aliasService.save(newAlias(LEGACY, AliasTargetKind.STAR, starId, "Second")));

        assertTrue(ex.getMessage().contains("already exists"),
                "friendly message must surface: " + ex.getMessage());
        assertTrue(ex.getMessage().contains("First"),
                "existing alias text must appear in error: " + ex.getMessage());
    }

    @Test
    @DisplayName("Uniqueness — same alias text allowed across different (universe, target) pairs")
    void uniquenessAllowsSameTextAcrossDifferentPairs() {
        // "Vulcan" alias in Legacy targeting star A, Caine Riordan targeting star A: allowed
        aliasService.save(newAlias(LEGACY, AliasTargetKind.STAR, "star-u-1", "Vulcan"));
        aliasService.save(newAlias(CAINE_RIORDAN, AliasTargetKind.STAR, "star-u-1", "Vulcan"));
        // Same text targeting different star within same universe: allowed
        aliasService.save(newAlias(LEGACY, AliasTargetKind.STAR, "star-u-2", "Vulcan"));
        // Verify all three exist
        assertEquals(2, aliasRepository.findByTargetKindAndTargetId(AliasTargetKind.STAR, "star-u-1").size());
        assertEquals(1, aliasRepository.findByTargetKindAndTargetId(AliasTargetKind.STAR, "star-u-2").size());
    }

    // ============================================================
    // Naming convention audit
    // ============================================================

    @Test
    @DisplayName("§14 — saved aliases all carry catalog-alias- id prefix")
    void aliasIdPrefixConvention() {
        Alias saved = aliasService.save(newAlias(LEGACY, AliasTargetKind.STAR, "star-naming", "Test"));
        assertTrue(saved.id().startsWith("catalog-alias-"),
                "Alias id must follow §14 convention: " + saved.id());
    }
}
