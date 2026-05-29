package com.teamgannon.trips.construct;

import com.teamgannon.trips.spaceshipmodeller.persistence.MegastructureDesignMapper;
import com.teamgannon.trips.spaceshipmodeller.persistence.MegastructureEntity;
import com.teamgannon.trips.spaceshipmodeller.persistence.MegastructureRepository;
import com.teamgannon.trips.spaceshipmodeller.persistence.SpaceshipDesignMapper;
import com.teamgannon.trips.spaceshipmodeller.persistence.StationDesignMapper;
import com.teamgannon.trips.spaceshipmodeller.persistence.StationEntity;
import com.teamgannon.trips.spaceshipmodeller.persistence.StationRepository;
import com.teamgannon.trips.spaceshipmodeller.persistence.TransportNodeMapper;
import com.teamgannon.trips.spaceshipmodeller.persistence.WeaponInstallationMapper;
import com.teamgannon.trips.spaceshipmodeller.service.MegastructureDesignerService;
import com.teamgannon.trips.spaceshipmodeller.service.StationDesignerService;
import com.teamgannon.trips.spaceshipmodeller.service.TransportNodeService;
import com.teamgannon.trips.spaceshipmodeller.service.WeaponInstallationDesignerService;
import com.terranrepublic.assets.AssetKind;
import com.terranrepublic.assets.Catalog;
import com.terranrepublic.assets.Cataloged;
import com.terranrepublic.assets.Megastructure;
import com.terranrepublic.assets.SpaceAsset;
import com.terranrepublic.assets.StationDesign;
import com.terranrepublic.assets.StationType;
import com.terranrepublic.assets.WeaponInstallation;
import com.terranrepublic.infrastructure.InfrastructureKind;
import com.terranrepublic.infrastructure.SpaceInfrastructure;
import com.terranrepublic.infrastructure.TransportNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * v2 Phase D.8 Step 7 — end-to-end integration test that exercises the catalog-sync pipeline
 * against a real Spring + JPA boot harness.
 *
 * <h2>Design contract (§6.3)</h2>
 * <p>
 * No service mocking. The test boots Spring with Flyway V1…V12 applied, instantiates real
 * {@code *DesignerService} classes + the real {@link DefaultConstructRegistry}, and exercises the
 * sync-by-id contract through the actual code path the running application uses. Repository
 * direct access is permitted for pre-seed setup convenience (e.g. inserting a legacy random-UUID
 * row to simulate a stale DB); service / registry calls must reach the real implementations.
 *
 * <h2>Scenarios covered</h2>
 * <ul>
 *   <li><b>S1 — Seed-from-empty</b>: fresh DB, sync runs, all catalog entries appear (4 tests,
 *       one per subtype).</li>
 *   <li><b>S2 — Upgrade-path</b>: pre-seeded with legacy random-UUID Troy row; after V12 cleanup
 *       + sync, the legacy row is gone and {@code catalog-troy} lives in the megastructure table.
 *       <em>This is the test that would have caught D.5's regression on day one</em> (§6.3).</li>
 *   <li><b>S3 — Multi-launch idempotency</b>: sync called twice, second call inserts zero rows
 *       (2 tests).</li>
 *   <li><b>S4 — User-edit preservation</b>: pre-existing rows are left untouched by sync
 *       (2 tests; one for user-edited catalog row, one for user-created non-catalog row).</li>
 *   <li><b>Panel load-path verification</b>: the four-bucket aggregate the panel reads through
 *       {@link DefaultConstructRegistry} returns the full catalog after sync (2 tests).</li>
 * </ul>
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
        StationDesignerService.class, StationDesignMapper.class,
        WeaponInstallationDesignerService.class, WeaponInstallationMapper.class,
        TransportNodeService.class, TransportNodeMapper.class,
        MegastructureDesignerService.class, MegastructureDesignMapper.class,
        SpaceshipDesignMapper.class,
        DefaultConstructRegistry.class
})
class CatalogSyncIntegrationTest {

    @Autowired
    private StationDesignerService stationService;
    @Autowired
    private WeaponInstallationDesignerService weaponService;
    @Autowired
    private TransportNodeService transportService;
    @Autowired
    private MegastructureDesignerService megastructureService;
    @Autowired
    private DefaultConstructRegistry registry;

    // Repositories — for setup convenience only (pre-seeding rows).
    @Autowired
    private StationRepository stationRepository;
    @Autowired
    private MegastructureRepository megastructureRepository;

    // JdbcTemplate — for simulating V12's DELETE in the S2 upgrade-path scenario.
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private long catalogStationCount() {
        return Catalog.all().stream().filter(StationDesign.class::isInstance).count();
    }

    private long catalogWeaponCount() {
        return Catalog.all().stream().filter(WeaponInstallation.class::isInstance).count();
    }

    private long catalogMegastructureCount() {
        return Catalog.all().stream().filter(Megastructure.class::isInstance).count();
    }

    private Set<String> catalogStationIds() {
        Set<String> ids = new HashSet<>();
        Catalog.all().stream()
                .filter(StationDesign.class::isInstance)
                .map(StationDesign.class::cast)
                .forEach(s -> ids.add(s.id()));
        return ids;
    }

    /** Mirrors the panel's {@code loadFromRegistry()} four-bucket aggregate. */
    private List<Cataloged> panelLoadPath() {
        List<Cataloged> all = new ArrayList<>();
        all.addAll(registry.assetsByKind(AssetKind.STATION));
        all.addAll(registry.assetsByKind(AssetKind.WEAPON_INSTALLATION));
        all.addAll(registry.assetsByKind(AssetKind.MEGASTRUCTURE));
        all.addAll(registry.infrastructureByKind(InfrastructureKind.TRANSPORT_NODE));
        return all;
    }

    // ==================================================================
    // S1 — Seed-from-empty (4 tests)
    // ==================================================================

    @Test
    @DisplayName("S1 — empty station table seeds all 8 catalog stations after sync")
    void emptyStationTableSeedsAllCatalogStations() {
        assertEquals(0, stationService.count(), "fresh DB starts empty");
        int inserted = stationService.syncCatalogEntries();
        assertEquals(catalogStationCount(), inserted, "sync inserts every catalog station");
        assertEquals(catalogStationCount(), stationService.count(), "table populated to full catalog");
        // Every catalog id is present after sync.
        for (String id : catalogStationIds()) {
            assertTrue(stationRepository.existsById(id),
                    "catalog station id '" + id + "' must be present after sync");
        }
    }

    @Test
    @DisplayName("S1 — empty weapon installation table seeds all catalog weapons after sync")
    void emptyWeaponTableSeedsAllCatalogWeapons() {
        assertEquals(0, weaponService.count(), "fresh DB starts empty");
        int inserted = weaponService.syncCatalogEntries();
        assertEquals(catalogWeaponCount(), inserted);
        assertEquals(catalogWeaponCount(), weaponService.count());
    }

    @Test
    @DisplayName("S1 — empty transport node table syncs zero entries (catalog has no transport nodes)")
    void emptyTransportNodeTableSeedsZeroEntries() {
        assertEquals(0, transportService.count(), "fresh DB starts empty");
        int inserted = transportService.syncCatalogEntries();
        assertEquals(0, inserted, "Catalog has no canonical transport-node entries today");
        assertEquals(0, transportService.count(), "table remains empty");
    }

    @Test
    @DisplayName("S1 — empty megastructure table seeds catalog-troy after sync")
    void emptyMegastructureTableSeedsCatalogTroy() {
        assertEquals(0, megastructureService.count(), "fresh DB starts empty");
        int inserted = megastructureService.syncCatalogEntries();
        assertEquals(catalogMegastructureCount(), inserted);
        assertEquals(1, megastructureService.count(), "Catalog ships Troy as the sole Megastructure");
        Optional<Megastructure> troy = megastructureService.findById("catalog-troy");
        assertTrue(troy.isPresent(), "Troy must be present at id 'catalog-troy'");
        assertEquals("Troy", troy.get().name());
    }

    // ==================================================================
    // S2 — Upgrade-path (the day-one regression catch test)
    // ==================================================================

    @Test
    @DisplayName("S2 — UPGRADE PATH: legacy random-UUID Troy row removed by V12; catalog-troy seeded into megastructure")
    void upgradePathFromLegacyTroyResolvesToCatalogTroy() {
        // ----- Pre-seed: simulate a pre-D.5 build's DB state ----------------
        // A legacy Troy row in station_design with a random-UUID id (matching what the
        // pre-D.8 seedFromCatalogIfEmpty would have produced from the pre-D.5 catalog where
        // Troy was a StationDesign). Repository.save is used here for setup convenience per
        // §6.3's allowance; the assertion paths still go through real services.
        StationEntity legacyTroy = new StationEntity("Troy");
        legacyTroy.setId("legacy-troy-uuid-12345");
        legacyTroy.setStationType(StationType.GATE_FORT);
        stationRepository.save(legacyTroy);
        assertEquals(1, stationRepository.count(), "legacy Troy row seeded");

        // ----- Simulate V12's DELETE running -------------------------------
        // Flyway V12 runs once during context init, before this test method sees the DB.
        // To test the cleanup effect on a row we inserted AFTER context init, we manually
        // invoke V12's DELETE statement via JdbcTemplate. The SQL is verbatim from V12.
        jdbcTemplate.update("""
                DELETE FROM station_design
                 WHERE id NOT LIKE 'catalog-%'
                   AND id NOT LIKE 'real-station-%'
                   AND name = 'Troy'
                """);

        // ----- Now run the sync, mirroring what the seeders do on boot -----
        stationService.syncCatalogEntries();
        weaponService.syncCatalogEntries();
        megastructureService.syncCatalogEntries();

        // ----- Assertions: the user's running app post-upgrade -------------

        // (a) Legacy station_design Troy row is gone.
        assertFalse(stationRepository.existsById("legacy-troy-uuid-12345"),
                "V12 cleanup must remove the legacy random-UUID Troy row from station_design");

        // (b) catalog-troy is present in the megastructure table.
        Optional<Megastructure> troy = megastructureService.findById("catalog-troy");
        assertTrue(troy.isPresent(),
                "catalog-troy must be present in the megastructure table after the upgrade");
        assertEquals("Troy", troy.get().name());

        // (c) All 8 D.5 stations present at catalog-* ids.
        for (String id : catalogStationIds()) {
            assertTrue(stationRepository.existsById(id),
                    "D.5 station '" + id + "' must be present after sync");
            assertTrue(id.startsWith("catalog-"),
                    "every D.5 station id must follow the catalog-* convention; was: " + id);
        }

        // (d) SAPL + SheVa Gun at catalog-sapl and catalog-sheva-gun.
        Optional<WeaponInstallation> sapl = weaponService.findById("catalog-sapl");
        Optional<WeaponInstallation> sheva = weaponService.findById("catalog-sheva-gun");
        assertTrue(sapl.isPresent(), "SAPL must be present at id 'catalog-sapl'");
        assertTrue(sheva.isPresent(), "SheVa Gun must be present at id 'catalog-sheva-gun'");
        assertEquals("SAPL", sapl.get().name());
        assertEquals("SheVa Gun", sheva.get().name());
    }

    // ==================================================================
    // S3 — Multi-launch idempotency (2 tests)
    // ==================================================================

    @Test
    @DisplayName("S3 — second sync call inserts zero rows (idempotent)")
    void multipleSyncCallsAreIdempotent() {
        // First sync: populates everything.
        int firstStations = stationService.syncCatalogEntries();
        int firstWeapons = weaponService.syncCatalogEntries();
        int firstMegas = megastructureService.syncCatalogEntries();
        int firstTransports = transportService.syncCatalogEntries();
        assertEquals(catalogStationCount(), firstStations);
        assertEquals(catalogWeaponCount(), firstWeapons);
        assertEquals(catalogMegastructureCount(), firstMegas);
        assertEquals(0, firstTransports);

        // Second sync: every catalog id already present → zero inserts.
        assertEquals(0, stationService.syncCatalogEntries(), "idempotent station sync");
        assertEquals(0, weaponService.syncCatalogEntries(), "idempotent weapon sync");
        assertEquals(0, megastructureService.syncCatalogEntries(), "idempotent megastructure sync");
        assertEquals(0, transportService.syncCatalogEntries(), "idempotent transport sync");

        // Counts unchanged after the second sync.
        assertEquals(catalogStationCount(), stationService.count());
        assertEquals(catalogWeaponCount(), weaponService.count());
        assertEquals(catalogMegastructureCount(), megastructureService.count());
    }

    @Test
    @DisplayName("S3 — registry state consistent across redundant sync calls")
    void registryStateConsistentAcrossSyncCalls() {
        // First sync + capture registry state.
        stationService.syncCatalogEntries();
        weaponService.syncCatalogEntries();
        megastructureService.syncCatalogEntries();
        List<SpaceAsset> stationsBefore = registry.assetsByKind(AssetKind.STATION);
        List<SpaceAsset> weaponsBefore = registry.assetsByKind(AssetKind.WEAPON_INSTALLATION);
        List<SpaceAsset> megasBefore = registry.assetsByKind(AssetKind.MEGASTRUCTURE);

        // Redundant sync.
        stationService.syncCatalogEntries();
        weaponService.syncCatalogEntries();
        megastructureService.syncCatalogEntries();

        // Registry sees identical counts (id sets unchanged).
        assertEquals(stationsBefore.size(), registry.assetsByKind(AssetKind.STATION).size());
        assertEquals(weaponsBefore.size(), registry.assetsByKind(AssetKind.WEAPON_INSTALLATION).size());
        assertEquals(megasBefore.size(), registry.assetsByKind(AssetKind.MEGASTRUCTURE).size());
    }

    // ==================================================================
    // S4 — User-edit preservation (2 tests)
    // ==================================================================

    @Test
    @DisplayName("S4 — user-edited catalog row is preserved across sync (existsById short-circuits)")
    void userEditedRowPreservedAcrossSync() {
        // Pre-seed catalog-iss with a deliberately modified description (simulates a user
        // edit in production). The sync must NOT overwrite.
        StationDesign iss = (StationDesign) Catalog.ISS;
        StationDesignMapper mapper = new StationDesignMapper();
        StationEntity entity = mapper.toEntity(iss);
        entity.setDescription("user-edited description — must survive the next sync");
        stationRepository.save(entity);

        // Run sync.
        int inserted = stationService.syncCatalogEntries();

        // Sync inserts every catalog station EXCEPT ISS (already present).
        assertEquals(catalogStationCount() - 1, inserted,
                "sync must skip the pre-existing ISS row and insert the rest");

        // The user-edited description is still there.
        Optional<StationDesign> back = stationService.findById("catalog-iss");
        assertTrue(back.isPresent());
        assertEquals("user-edited description — must survive the next sync",
                back.get().description(),
                "user edit must survive sync — insert-only contract per §3.3");
    }

    @Test
    @DisplayName("S4 — user-created non-catalog row preserved across sync (no orphan deletion)")
    void userCreatedNonCatalogRowPreservedAcrossSync() {
        // Pre-seed a station with an id not matching the catalog-* pattern — simulates a
        // user-created station saved through the editor that has no catalog twin.
        StationEntity custom = new StationEntity("My Custom Station");
        custom.setId("my-custom-station-id");
        custom.setStationType(StationType.OUTPOST);
        stationRepository.save(custom);

        // Run sync.
        stationService.syncCatalogEntries();

        // The custom station is still there.
        assertTrue(stationRepository.existsById("my-custom-station-id"),
                "user-created non-catalog row must survive sync — insert-only contract preserves orphans");
        Optional<StationDesign> back = stationService.findById("my-custom-station-id");
        assertTrue(back.isPresent());
        assertEquals("My Custom Station", back.get().name());
    }

    // ==================================================================
    // Panel load-path verification (2 tests)
    // ==================================================================

    @Test
    @DisplayName("Panel load-path returns all catalog entries after sync (S1 panel slice)")
    void panelLoadPathReturnsAllCatalogEntries() {
        // Sync every subtype, then exercise the same four-bucket aggregate the panel calls.
        stationService.syncCatalogEntries();
        weaponService.syncCatalogEntries();
        megastructureService.syncCatalogEntries();
        transportService.syncCatalogEntries();

        List<Cataloged> all = panelLoadPath();

        long expectedCount = catalogStationCount() + catalogWeaponCount() + catalogMegastructureCount();
        // + 0 transport nodes (Catalog ships none today).
        assertEquals(expectedCount, all.size(),
                "panel load-path must return the full catalog after sync");
        assertEquals(catalogStationCount(),
                all.stream().filter(StationDesign.class::isInstance).count());
        assertEquals(catalogWeaponCount(),
                all.stream().filter(WeaponInstallation.class::isInstance).count());
        assertEquals(catalogMegastructureCount(),
                all.stream().filter(Megastructure.class::isInstance).count());
        assertEquals(0, all.stream().filter(TransportNode.class::isInstance).count());
    }

    @Test
    @DisplayName("Panel load-path returns Troy via the MEGASTRUCTURE bucket — the D.7-close-out symptom is gone")
    void panelLoadPathReturnsMegastructureBucket() {
        // This test names the D.7-close-out symptom directly: at that point, the panel
        // showed "Loaded 3 construct(s)" with no Megastructure entries because
        // (a) the seeder didn't exist and (b) the panel never called assetsByKind(MEGASTRUCTURE).
        // After D.8 Step 6's wiring + this test's sync, Troy must reach the panel.
        megastructureService.syncCatalogEntries();

        List<SpaceAsset> megas = registry.assetsByKind(AssetKind.MEGASTRUCTURE);

        assertEquals(1, megas.size(),
                "registry.assetsByKind(MEGASTRUCTURE) must return Troy after sync");
        assertEquals("Troy", megas.get(0).name());
        assertEquals(AssetKind.MEGASTRUCTURE, megas.get(0).kind());
        assertEquals("catalog-troy", megas.get(0).id());
    }
}
