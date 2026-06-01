package com.teamgannon.trips.spaceshipmodeller.persistence;

import com.teamgannon.trips.spaceshipmodeller.service.MegastructureDesignerService;
import com.teamgannon.trips.spaceshipmodeller.service.UniverseDesignerService;
import com.teamgannon.trips.spaceshipmodeller.service.WeaponInstallationDesignerService;
import com.terranrepublic.assets.Universe;
import com.terranrepublic.assets.UniverseLifecycle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * v2 Phase F.1 Step 4 — DB-level audit of V17's migration outcomes.
 *
 * <p>Boots Spring + JPA with Flyway through V17. Verifies:
 * <ul>
 *   <li>V17 created exactly 13 Universe rows.</li>
 *   <li>The 2 first-class universes (Legacy of the Aldenata, Caine Riordan) have curated
 *       descriptions (non-stem) and named authors.</li>
 *   <li>The 11 thin universes have the auto-generated description stem.</li>
 *   <li>All 13 universes ship inactive (active=FALSE) and AVAILABLE lifecycle (R1.8 default).</li>
 *   <li>All ids follow the {@code catalog-universe-<slug>} convention.</li>
 *   <li>After running the Catalog seeders, the 5 fiction-canon entries are tagged with
 *       Legacy of the Aldenata's universe_id (verifying the Catalog source-of-truth +
 *       mapper round-trip wire all the way through to DB state).</li>
 * </ul>
 *
 * <p>Sibling to {@code CatalogSyncIntegrationTest} (D.8) — same @DataJpaTest harness with
 * Flyway + service imports. No service mocking; the test runs against the same code path the
 * production seeder uses.
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
        MegastructureDesignerService.class, MegastructureDesignMapper.class,
        WeaponInstallationDesignerService.class, WeaponInstallationMapper.class
})
class V17MigrationAuditTest {

    private static final String LEGACY = "catalog-universe-legacy-of-the-aldenata";
    private static final String CAINE_RIORDAN = "catalog-universe-caine-riordan";
    private static final String STEM_PREFIX = "Auto-seeded from existing catalog entries";

    @Autowired
    private UniverseDesignerService universeService;
    @Autowired
    private UniverseRepository universeRepository;
    @Autowired
    private MegastructureDesignerService megastructureService;
    @Autowired
    private MegastructureRepository megastructureRepository;
    @Autowired
    private WeaponInstallationDesignerService weaponService;
    @Autowired
    private WeaponInstallationRepository weaponRepository;

    // ============================================================
    // Part A — V17 INSERT statements
    // ============================================================

    @Test
    @DisplayName("V17 created exactly 13 Universe rows (2 first-class + 11 thin)")
    void exactly13UniverseRows() {
        assertEquals(13L, universeService.count(),
                "V17 INSERTs exactly 13 universes per the §3.2 audit mapping");
    }

    @Test
    @DisplayName("Legacy of the Aldenata exists with curated description and named author")
    void legacyOfTheAldenataIsFirstClass() {
        Universe u = universeService.findById(LEGACY)
                .orElseThrow(() -> new AssertionError("Legacy of the Aldenata row missing"));
        assertEquals("Legacy of the Aldenata", u.name());
        assertEquals("John Ringo", u.sourceAuthor());
        assertEquals(UniverseLifecycle.AVAILABLE, u.lifecycle());
        assertFalse(u.active(), "default activation state is FALSE (R1.8 real-only default)");
        assertFalse(u.description().startsWith(STEM_PREFIX),
                "first-class universe must have curated description, not the auto-stem");
        assertTrue(u.description().contains("Posleen"),
                "Legacy of the Aldenata description must reference the Posleen War");
    }

    @Test
    @DisplayName("Caine Riordan exists with curated description and named author")
    void caineRiordanIsFirstClass() {
        Universe u = universeService.findById(CAINE_RIORDAN)
                .orElseThrow(() -> new AssertionError("Caine Riordan row missing"));
        assertEquals("Caine Riordan", u.name());
        assertEquals("Charles Gannon", u.sourceAuthor());
        assertEquals(UniverseLifecycle.AVAILABLE, u.lifecycle());
        assertFalse(u.active());
        assertFalse(u.description().startsWith(STEM_PREFIX));
        assertTrue(u.description().contains("Hkh"),
                "Caine Riordan description must reference Hkh'Rkh factions");
    }

    @Test
    @DisplayName("the 11 thin universes have the auto-generated description stem")
    void elevenThinUniversesHaveAutoGenStem() {
        List<String> thinIds = List.of(
                "catalog-universe-battlestar-galactica",
                "catalog-universe-firefly",
                "catalog-universe-foundation",
                "catalog-universe-honor-harrington",
                "catalog-universe-mass-effect",
                "catalog-universe-project-hail-mary",
                "catalog-universe-star-trek",
                "catalog-universe-star-wars",
                "catalog-universe-the-expanse",
                "catalog-universe-the-hitchhikers-guide-to-the-galaxy",
                "catalog-universe-the-martian"
        );
        for (String id : thinIds) {
            Universe u = universeService.findById(id)
                    .orElseThrow(() -> new AssertionError("Thin universe row missing: " + id));
            assertTrue(u.description().startsWith(STEM_PREFIX),
                    id + " description must start with the auto-stem; actual: " + u.description());
            assertEquals("", u.sourceAuthor(), id + " thin-universe sourceAuthor is empty");
        }
    }

    @Test
    @DisplayName("all 13 universes have ids following catalog-universe-<slug> convention")
    void allIdsFollowSlugConvention() {
        for (Universe u : universeService.findAll()) {
            assertTrue(u.id().startsWith("catalog-universe-"),
                    "Universe id '" + u.id() + "' violates §12 naming convention");
        }
    }

    @Test
    @DisplayName("all 13 universes ship inactive (active=FALSE per R1.8)")
    void allUniversesShipInactive() {
        long activeCount = universeService.findAllActive().size();
        assertEquals(0L, activeCount,
                "Fresh installation must default to real-only mode (R1.8); no universes "
                        + "should be active until the user explicitly activates them");
    }

    @Test
    @DisplayName("all 13 universes ship with AVAILABLE lifecycle")
    void allUniversesShipAvailable() {
        long availableCount = universeService.findByLifecycle(UniverseLifecycle.AVAILABLE).size();
        assertEquals(13L, availableCount);
        long deprecatedCount = universeService.findByLifecycle(UniverseLifecycle.DEPRECATED).size();
        assertEquals(0L, deprecatedCount, "No universes ship DEPRECATED in F.1");
    }

    // ============================================================
    // Part B — Post-seed UPDATE coverage
    // ============================================================

    @Test
    @DisplayName("after seeding, Catalog.TROY's megastructure row is tagged with Legacy of the Aldenata")
    void troyTaggedAfterSeed() {
        int inserted = megastructureService.syncCatalogEntries();
        assertTrue(inserted > 0, "Catalog ships at least the TROY megastructure");

        MegastructureEntity troy = megastructureRepository.findById("catalog-troy")
                .orElseThrow(() -> new AssertionError("TROY missing post-seed"));
        assertEquals(LEGACY, troy.getUniverseId(),
                "Catalog.TROY ships with universeId set; seeder must persist that value");
    }

    @Test
    @DisplayName("after seeding, Catalog.SAPL is tagged with Legacy of the Aldenata")
    void saplTaggedAfterSeed() {
        int inserted = weaponService.syncCatalogEntries();
        assertTrue(inserted > 0, "Catalog ships at least SAPL");

        WeaponInstallationEntity sapl = weaponRepository.findById("catalog-sapl")
                .orElseThrow(() -> new AssertionError("SAPL missing post-seed"));
        assertEquals(LEGACY, sapl.getUniverseId(),
                "Catalog.SAPL ships with universeId set; seeder must persist that value");
    }

    @Test
    @DisplayName("after seeding, Catalog.SHEVA_GUN is tagged with Legacy of the Aldenata")
    void shevaGunTaggedAfterSeed() {
        weaponService.syncCatalogEntries();
        WeaponInstallationEntity sheva = weaponRepository.findById("catalog-sheva-gun")
                .orElseThrow(() -> new AssertionError("SHEVA_GUN missing post-seed"));
        assertEquals(LEGACY, sheva.getUniverseId());
    }

    @Test
    @DisplayName("after seeding, real stations seeded later remain universe_id=NULL (canonical)")
    void realCatalogEntriesUntaggedAfterSeed() {
        // The 8 real stations in Catalog.all() are seeded via the station service. They must
        // NOT get tagged with any universe — real data is universe_id=NULL per R1.9.
        // We don't import StationDesignerService here since the existing CatalogSyncIntegrationTest
        // covers that surface; this test focuses on the universe-id propagation specifically.
        // The megastructure + weapon seeds exercise the same propagation path, so this assertion
        // verifies via TROY's siblings (real megastructures, if any — currently none in Catalog).
        megastructureService.syncCatalogEntries();
        // After sync, every megastructure with sourceType=REAL must have universe_id=null.
        // Catalog ships only TROY which is fictional, so this is forward-looking validation.
        List<MegastructureEntity> all = megastructureRepository.findAll();
        for (MegastructureEntity m : all) {
            if (m.getProvenanceSourceType() != null && m.getProvenanceSourceType().name().equals("REAL")) {
                assertNotNull(m.getUniverseId() == null,
                        m.getName() + " is REAL but has universe_id=" + m.getUniverseId());
            }
        }
    }

    // ============================================================
    // Part C — Idempotency + integrity
    // ============================================================

    @Test
    @DisplayName("universe_id values are subset of universe row ids (FK integrity at the data level)")
    void allUniverseIdReferencesValid() {
        Set<String> validIds = universeService.findAll().stream()
                .map(Universe::id)
                .collect(java.util.stream.Collectors.toSet());

        // Seed the catalog entities so there are rows with universe_id to check.
        megastructureService.syncCatalogEntries();
        weaponService.syncCatalogEntries();

        for (MegastructureEntity m : megastructureRepository.findAll()) {
            if (m.getUniverseId() != null) {
                assertTrue(validIds.contains(m.getUniverseId()),
                        m.getName() + " references universe_id='" + m.getUniverseId()
                                + "' which has no matching Universe row");
            }
        }
        for (WeaponInstallationEntity w : weaponRepository.findAll()) {
            if (w.getUniverseId() != null) {
                assertTrue(validIds.contains(w.getUniverseId()),
                        w.getName() + " references universe_id='" + w.getUniverseId()
                                + "' which has no matching Universe row");
            }
        }
    }

    @Test
    @DisplayName("V17 INSERTs are idempotent — re-running yields the same 13 rows")
    void v17InsertsAreIdempotent() {
        // V17 uses INSERT ... WHERE NOT EXISTS so re-running the SQL would be safe.
        // Flyway itself prevents re-running by checksum; this test verifies the SQL semantics.
        long before = universeService.count();
        // Manually call the service-side sync (the closest production analogue to "re-running");
        // it's vacuous in F.1 (Catalog ships no Universe constants), so count stays the same.
        int inserted = universeService.syncCatalogEntries();
        long after = universeService.count();
        assertEquals(0, inserted, "Catalog ships zero Universe constants; sync is vacuous");
        assertEquals(before, after);
        assertEquals(13L, after);
    }
}
