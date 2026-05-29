# Phase D.8 — Catalog sync-by-id + Megastructure persistence pipeline

**Status**: design, pre-implementation
**Date**: 2026-05-29
**Scope**: Replace the seed-on-empty contract across all four `SpaceAsset` subtypes with a sync-by-id contract that survives the upgrade path; close the Megastructure persistence pipeline left dangling at D.7 close-out (repository + service + seeder + four panel-side wiring touchpoints + editor save path); add the end-to-end test class that would have caught D.5's silent regression on the day it landed.

---

## 1. Why this exists

The diagnostic at the close of Phase D.7 surfaced a category of failure the test suite has been silently ignoring since at least 2026-05-28.

**The user's running UI showed three constructs** — Troy (rendering as `StationDesign` + `GATE_FORT`), SAPL, SheVa Gun — and a universe tab strip carrying only "All / Aldenata / Troy Rising". Neither D.5's eight real space stations nor D.7's migrated `Megastructure` Troy reached the panel. The test suite at 3,587 was green.

The root cause splits into two independent buckets.

**Bucket A — D.5 silent regression.** `StationDesignerService.seedFromCatalogIfEmpty()` runs once per app launch via `StationCatalogSeeder` (an `@EventListener(ApplicationReadyEvent.class)`). Its first action is `if (count() > 0) return 0;` — the table is touched **only** when it is empty. The user's database was seeded by a pre-D.5 build with `Catalog.all()`-StationDesigns of size 1 (TROY alone). Every subsequent Catalog change since — D.5's eight real stations, D.6's function/provenance axes, D.7's TROY migration to Megastructure — has been silently swallowed because the seed-on-empty guard short-circuits before any of those deltas matter. The same shape exists on `WeaponInstallationDesignerService.seedFromCatalogIfEmpty()` and `TransportNodeService.seedFromCatalogIfEmpty()`. D.5 has been broken in production for one day for one user; the moment any other user picks up the latest builds, it will look broken for them too.

**Bucket B — D.7 incomplete wiring.** Phase D.7 added the `Megastructure` record (Step 3), the V11 migration + entity + mapper (Step 4), the editor dialog (Step 5), the Catalog seed entry (Step 6), the registry bucket via `Catalog`-direct filter (Step 7), and the audit invariants (Step 8). It did **not** add:

- `MegastructureRepository` (no JPA persistence interface);
- `MegastructureDesignerService` (no service layer; no save / delete / count / `findAllAsAssets`);
- `MegastructureCatalogSeeder` (no `@EventListener` bean — even an empty `megastructure` table never receives Troy);
- A panel-side load path: `InstallationDesignerPanel.loadFromRegistry()` calls `registry.assetsByKind(AssetKind.STATION/WEAPON_INSTALLATION)` + `infrastructureByKind(TRANSPORT_NODE)` — `MEGASTRUCTURE` is not in the list;
- A kind-filter combo entry: `kindFilter.getItems().setAll(ALL, KIND_STATION, KIND_WEAPON, KIND_TRANSPORT)` is hardcoded to four strings, no Megastructure;
- A new-construct picker entry: `onNew()` builds `new ChoiceDialog<>(stationLabel, List.of(stationLabel, weaponLabel, transportLabel))` — three entries;
- An edit dispatcher entry: `onEdit()`'s `switch (construct) { ... }` has three cases plus a defensive `default -> {}`;
- A caller for `MegastructureEditorDialog` itself — `grep` confirms it is unreferenced outside test code.

**Bucket C — the test-shape gap that masked both.** Every existing panel / registry test mocks the registry to return `Catalog.all()`-derived data directly. `InstallationDesignerPanelTest.registryWithCatalogSeed()` short-circuits the JPA layer entirely. `DefaultConstructRegistryTest` mocks `stationDesignerService.findAllAsAssets()` with `catalogStations()`. `FlywayBaselineSmokeTest` validates schema↔entity drift but not seed-data freshness. **No test exercises the path "JPA table contains a stale subset of Catalog at boot time, the app launches, the registry reads through the real services, the panel's load path runs end-to-end."** That test would have caught the D.5 regression the day it landed.

Phase D.8 closes all three buckets.

---

## 2. Scope — the four work items

1. **Sync-by-id seeding contract** (replaces seed-on-empty across all four `SpaceAsset` subtypes — Station, WeaponInstallation, TransportNode, Megastructure). On every app launch, ensure every `Catalog` entry of the relevant subtype exists in JPA; insert what's missing; leave existing rows untouched.

2. **Megastructure persistence pipeline.** `MegastructureRepository`, `MegastructureDesignerService`, `MegastructureCatalogSeeder` — mirrors the existing triple for Station / WeaponInstallation / TransportNode. `DefaultConstructRegistry.loadMegastructures()` switches from the Step 7 `Catalog`-direct filter to `megastructureService.findAllAsAssets()`.

3. **UI wiring.** `InstallationDesignerPanel.loadFromRegistry()` adds the `MEGASTRUCTURE` bucket call. `kindFilter`, `onNew()` picker, `onEdit()` switch, and the `rebuildSubtypeFilter()` switch all extend to a fourth `KIND_MEGASTRUCTURE` case. New `openMegastructureEditor` + `persistMegastructure` methods mirror `openStationEditor` + `persistStation` exactly. New properties keys for `kind.MEGASTRUCTURE` + the megastructure subtype labels.

4. **End-to-end test that would have caught D.5's regression.** A `@DataJpaTest`-shaped class that boots Spring + JPA, runs Flyway V1…V11, runs the seeders, then exercises the `DefaultConstructRegistry` and the panel's load path against the actual JPA-backed services. No service mocking. Asserts that the full Catalog reaches the panel.

The Phase D.8 work is **not**:

- Out of scope: redesigning the `Catalog` constant pattern wholesale, introducing a `catalog_version` table, or anything that breaks the existing `Catalog.all()` contract beyond the id-stability requirement in §3.
- Out of scope: backwards-data-migration logic that *transforms* stale rows in pre-existing tables to match new Catalog values. Sync-by-id is **insert-only**; an existing TROY row with `STATION_TYPE = GATE_FORT` stays as-is. Updating already-persisted rows to reflect catalog changes is its own design question and is left for a possible D.9.
- Out of scope: any change to the audit-test invariants. D.7's CatalogAuditTest is correct; this phase is purely persistence + wiring.

---

## 3. The sync-by-id seeding contract

### 3.1 Method shape and rename

The existing `seedFromCatalogIfEmpty()` method on each service is renamed and reimplemented:

```java
// Before — D.5 era
public int seedFromCatalogIfEmpty() {
    if (count() > 0) return 0;
    // ... insert all catalog entries ...
}

// After — D.8
public int syncCatalogEntries() {
    int inserted = 0;
    for (StationDesign design : catalogStations()) {
        if (!repository.existsById(design.id())) {
            repository.save(mapper.toEntity(design));
            inserted++;
        }
    }
    return inserted;
}
```

**The rename is non-negotiable.** Keeping the old method name with the new semantics is a trap for any future reader. `seedFromCatalogIfEmpty` describes a behavior contract (only seeds an empty table) that no longer holds. The new name `syncCatalogEntries` describes the new contract (idempotent, insert-only, runs every launch).

The same rename applies to `WeaponInstallationDesignerService` and `TransportNodeService`. A new `MegastructureDesignerService.syncCatalogEntries()` ships in the new shape from day one (no `seedFromCatalogIfEmpty` predecessor to remove).

Each `*CatalogSeeder` bean's `@EventListener` method also gets its log line updated to describe the new contract — "Synced N catalog entr(y/ies) into …" rather than "Seeded N … from Catalog into an empty … table".

### 3.2 The id-stability sub-requirement — universal `"catalog-<slug>"` naming

The sync-by-id algorithm depends on **Catalog constants having stable ids across JVM launches**. The current `Catalog` does this inconsistently:

- The eight Phase D.5 real stations use deterministic string ids: `"real-station-iss"`, `"real-station-tiangong"`, etc.
- TROY (now `Megastructure`), SAPL, SHEVA_GUN, POSLEEN_COMMAND_DODECAHEDRON, POSLEEN_BATTLE_DODECAHEDRON all use `UUID.randomUUID().toString()` evaluated at class-load time. Every JVM start produces a fresh UUID.

If TROY's id changes each launch, sync-by-id sees a Catalog entry whose id is absent from JPA, inserts a fresh row, and the old TROY row remains alongside it. The table grows by one Megastructure each launch.

**Phase D.8 converts every `Catalog` constant to a single naming convention: `"catalog-<lowercase-kebab-slug>"`.** This applies to both the five UUID-random constants AND to the eight D.5 real-stations (which currently use `"real-station-*"`). Single convention everywhere is worth the small renaming cost.

**The full mapping** (13 constants, every catalog entry):

| Catalog constant | Subtype | Old id | New id |
|---|---|---|---|
| `TROY` | Megastructure | `UUID.randomUUID()` | `"catalog-troy"` |
| `SAPL` | WeaponInstallation | `UUID.randomUUID()` | `"catalog-sapl"` |
| `SHEVA_GUN` | WeaponInstallation | `UUID.randomUUID()` | `"catalog-sheva-gun"` |
| `POSLEEN_COMMAND_DODECAHEDRON` | SpaceshipDesign | `UUID.randomUUID()` | `"catalog-posleen-command-dodecahedron"` |
| `POSLEEN_BATTLE_DODECAHEDRON` | SpaceshipDesign | `UUID.randomUUID()` | `"catalog-posleen-battle-dodecahedron"` |
| `ISS` | StationDesign | `"real-station-iss"` | `"catalog-iss"` |
| `TIANGONG` | StationDesign | `"real-station-tiangong"` | `"catalog-tiangong"` |
| `MIR` | StationDesign | `"real-station-mir"` | `"catalog-mir"` |
| `SKYLAB` | StationDesign | `"real-station-skylab"` | `"catalog-skylab"` |
| `SALYUT_1` | StationDesign | `"real-station-salyut-1"` | `"catalog-salyut-1"` |
| `SALYUT_7` | StationDesign | `"real-station-salyut-7"` | `"catalog-salyut-7"` |
| `LUNAR_GATEWAY` | StationDesign | `"real-station-lunar-gateway"` | `"catalog-lunar-gateway"` |
| `AXIOM_STATION` | StationDesign | `"real-station-axiom"` | `"catalog-axiom"` |

(The audit in §8 item 3 should confirm the exact existing `"real-station-*"` slugs and feed corrections back into this table before implementation.)

### 3.2.1 V12 cleanup-and-rename migration

Phase D.8 ships a one-shot Flyway migration **V12** that handles both transformations: deletes legacy random-UUID rows whose canonical name matches a Catalog entry (so existing users don't end up with duplicate-Troy / duplicate-SAPL after sync), and renames the eight `"real-station-*"` ids to `"catalog-*"` so the convention is uniform.

The full SQL:

```sql
-- V12__catalog_id_stabilization.sql
-- Phase D.8 cleanup: remove legacy random-UUID rows whose name matches a Catalog entry,
-- then rename the eight D.5 real-station ids to the universal "catalog-*" convention.
-- Idempotent: re-running this migration is safe (subsequent runs find nothing to do).

-- ---- Stations: delete legacy random-UUID Troy ---------------------------
-- Belt-and-braces predicate: match ON canonical name AND on the absence of
-- the new stable-id pattern. This deletes only genuine legacy rows; a user
-- who renamed their Troy to something else (a user edit) keeps their row.
DELETE FROM station_design
 WHERE id NOT LIKE 'catalog-%'
   AND id NOT LIKE 'real-station-%'
   AND name = 'Troy';

-- ---- WeaponInstallations: delete legacy random-UUID SAPL + SheVa Gun ----
DELETE FROM weapon_installation
 WHERE id NOT LIKE 'catalog-%'
   AND name IN ('SAPL', 'SheVa Gun');

-- ---- Spaceships: delete legacy random-UUID Posleen ships ----------------
DELETE FROM spaceship_design
 WHERE id NOT LIKE 'catalog-%'
   AND name IN ('Posleen Command Dodecahedron', 'Posleen Battle Dodecahedron');

-- ---- D.5 real-station rename: "real-station-X" -> "catalog-X" -----------
UPDATE station_design SET id = 'catalog-iss'           WHERE id = 'real-station-iss';
UPDATE station_design SET id = 'catalog-tiangong'      WHERE id = 'real-station-tiangong';
UPDATE station_design SET id = 'catalog-mir'           WHERE id = 'real-station-mir';
UPDATE station_design SET id = 'catalog-skylab'        WHERE id = 'real-station-skylab';
UPDATE station_design SET id = 'catalog-salyut-1'      WHERE id = 'real-station-salyut-1';
UPDATE station_design SET id = 'catalog-salyut-7'      WHERE id = 'real-station-salyut-7';
UPDATE station_design SET id = 'catalog-lunar-gateway' WHERE id = 'real-station-lunar-gateway';
UPDATE station_design SET id = 'catalog-axiom'         WHERE id = 'real-station-axiom';
```

The exact `"real-station-*"` source slugs need confirmation by the §8 audit. The SQL above assumes the most-likely pattern; flag any mismatch before the migration ships.

**Why the belt-and-braces predicate matters:** `id NOT LIKE 'catalog-%' AND id NOT LIKE 'real-station-%' AND name = 'Troy'`. A user who edited their Troy's `description` doesn't lose data — the row's id is still the legacy random UUID, the name is still 'Troy', and the predicate matches, so the row is deleted. **But** if the user edited Troy's *name* (say, renamed it to "Troy II" in the editor), the predicate falls through and the row survives. Result: one user-renamed row + one new `catalog-troy` row coexist. That's the correct trade-off: a redundant row is recoverable; a wrongful deletion of edited data isn't.

After V12 runs, the V11 `megastructure` table is empty (V11 created it; the V12 cleanup has nothing to do there), and the `ApplicationReadyEvent` seeder fires immediately after, inserting `catalog-troy` via `syncCatalogEntries`. End state: one TROY row in `megastructure` with `id = 'catalog-troy'`; zero TROY rows in `station_design`.

### 3.3 What sync does NOT do

- **Does not update existing rows.** If a row's `id` is present in JPA, the row is left alone. User-edited values, custom catalog overrides, partial D.6 backfills — all preserved. This is the foot-gun guard: an upgrade should never silently overwrite the user's local edits.
- **Does not delete orphan rows.** A JPA row whose `id` is no longer in `Catalog.all()` stays. This preserves user-created entries (constructed via the editor dialog, saved to JPA, never present in `Catalog`). **Architecturally accepted limitation:** the insert-only contract means a catalog *removal* (a constant deleted from `Catalog.java`) does not propagate to running installs — the row lives on forever in pre-existing JPA tables. This is the correct trade-off for D.8 (preserves user data), but is **deferred future work** if catalog deprecation becomes a real need. A possible D.9 (or later) could introduce a `catalog_deprecation` table + tombstone-marker pattern; not in D.8 scope.
- **Does not run inside a single transaction across all subtypes.** Each subtype's sync runs in its own `@Transactional`; a failure in one doesn't block the others. Mirrors the existing `*CatalogSeeder` independence.

### 3.4 Idempotency contract

Running `syncCatalogEntries()` twice in a row on the same JVM must return the same result on the first call and `0` on the second. Asserted by an explicit test on each service.

### 3.5 The log line

Each service logs at `INFO` after each sync invocation:

- If `inserted == 0`: nothing (silent; no log spam on every launch).
- If `inserted > 0`: `log.info("Synced {} new catalog entry/ies into the {} table", inserted, TABLE_NAME);`

The "Phase B seed" / "Phase D.5 seed" labels from existing log lines disappear with the rename.

---

## 4. The Megastructure persistence pipeline

### 4.1 `MegastructureRepository`

New JPA repository, mirrors `StationRepository`:

```java
@Repository
public interface MegastructureRepository extends JpaRepository<MegastructureEntity, String> {
    boolean existsByNameIgnoreCase(String name);
    // any other finders matched against StationRepository's existing shape
}
```

Lives at `com.teamgannon.trips.spaceshipmodeller.persistence.MegastructureRepository`. Same package as `StationRepository`, `WeaponInstallationRepository`, etc.

### 4.2 `MegastructureDesignerService`

New `@Service`, mirrors `StationDesignerService` field-for-field where the patterns transfer cleanly:

```java
@Service
@Transactional(readOnly = true)
public class MegastructureDesignerService {

    private final MegastructureRepository repository;
    private final MegastructureDesignMapper mapper;

    @Autowired
    public MegastructureDesignerService(MegastructureRepository repository,
                                        MegastructureDesignMapper mapper) { ... }

    public List<Megastructure> findAll() { ... }
    public List<SpaceAsset> findAllAsAssets() { ... }
    public Optional<Megastructure> findById(String id) { ... }
    public boolean existsByName(String name) { ... }
    public long count() { ... }

    @Transactional
    public Megastructure save(Megastructure design) { ... }

    @Transactional
    public void deleteById(String id) { ... }

    @Transactional
    public int syncCatalogEntries() { ... }   // §3 contract
}
```

The `save`, `deleteById`, and `syncCatalogEntries` methods are `@Transactional` for writes; the read methods inherit the class-level `readOnly = true`.

`findAllAsAssets()` returns `List<SpaceAsset>` — the same shape used by `DefaultConstructRegistry.loadMegastructures()` after the §4.4 swap.

### 4.3 `MegastructureCatalogSeeder`

New `@Component` mirroring `StationCatalogSeeder`:

```java
@Component
@Slf4j
public class MegastructureCatalogSeeder {
    private final MegastructureDesignerService service;

    public MegastructureCatalogSeeder(MegastructureDesignerService service) { ... }

    @EventListener(ApplicationReadyEvent.class)
    public void syncOnApplicationReady() {
        try {
            int inserted = service.syncCatalogEntries();
            if (inserted > 0) {
                log.info("Synced {} new megastructure(s) from Catalog into the MEGASTRUCTURE table",
                        inserted);
            }
        } catch (Exception e) {
            log.error("Megastructure catalog sync failed; table may be incomplete until the next launch", e);
        }
    }
}
```

Spring auto-discovers via component-scan. Fires once per launch.

### 4.4 `DefaultConstructRegistry.loadMegastructures()` — swap the source

Current Step 7 implementation:

```java
private List<SpaceAsset> loadMegastructures() {
    return Catalog.all().stream()
            .filter(Megastructure.class::isInstance)
            .map(a -> (SpaceAsset) a)
            .toList();
}
```

New implementation:

```java
private List<SpaceAsset> loadMegastructures() {
    return megastructureDesignerService.findAllAsAssets();
}
```

The Step 7 javadoc that flagged the "Catalog-direct because no repository exists yet — hook for future migration" goes away. The constructor signature on `DefaultConstructRegistry` grows by one parameter: `MegastructureDesignerService megastructureDesignerService`. Spring constructor-injects.

`allById()` continues to include `loadMegastructures()` in its concatenation — that part already works.

### 4.5 Migration ordering: Flyway runs before the seeders

Confirmed correct by inspection of the existing pattern. Flyway runs at Spring Boot's auto-configuration time (well before `ApplicationReadyEvent`). V11 will create the `megastructure` table; the seeder fires after, sees an empty table, and inserts Troy. Mirrors the V7 → `StationCatalogSeeder` ordering.

---

## 5. The UI wiring

Five touchpoints on `InstallationDesignerPanel.java`.

### 5.1 `loadFromRegistry()` — load the MEGASTRUCTURE bucket

Currently (line 626):

```java
private List<Cataloged> loadFromRegistry() {
    // ...
    List<Cataloged> all = new ArrayList<>();
    all.addAll(registry.assetsByKind(AssetKind.STATION));
    all.addAll(registry.assetsByKind(AssetKind.WEAPON_INSTALLATION));
    all.addAll(registry.infrastructureByKind(InfrastructureKind.TRANSPORT_NODE));
    return all;
}
```

Adds one line:

```java
all.addAll(registry.assetsByKind(AssetKind.MEGASTRUCTURE));
```

Position: between WEAPON_INSTALLATION and TRANSPORT_NODE (the natural reading order from "asset subtypes" to "infrastructure subtypes").

### 5.2 `kindFilter` — add the fourth combo entry

Currently (line 166):

```java
kindFilter.getItems().setAll(ALL,
    kindLabel(KIND_STATION),
    kindLabel(KIND_WEAPON),
    kindLabel(KIND_TRANSPORT));
```

Adds a fourth label between WEAPON and TRANSPORT (alphabetical-ish order in the existing list):

```java
kindFilter.getItems().setAll(ALL,
    kindLabel(KIND_STATION),
    kindLabel(KIND_WEAPON),
    kindLabel(KIND_MEGASTRUCTURE),
    kindLabel(KIND_TRANSPORT));
```

Requires a new constant: `private static final String KIND_MEGASTRUCTURE = "MEGASTRUCTURE";` co-located with the existing `KIND_STATION` / `KIND_WEAPON` / `KIND_TRANSPORT` near line 97-99.

`rebuildSubtypeFilter()` (the switch at line 382) also gains a case:

```java
case KIND_MEGASTRUCTURE -> {
    Arrays.stream(MegastructureArchetype.values()).map(Enum::name).forEach(subtypeFilter.getItems()::add);
    subtypeFilter.setDisable(false);
}
```

### 5.3 `onNew()` picker — add the fourth choice

Currently (line 458):

```java
ChoiceDialog<String> picker = new ChoiceDialog<>(stationLabel,
        List.of(stationLabel, weaponLabel, transportLabel));
```

Adds the megastructure label:

```java
String megaLabel = get("kind." + NEW_MEGASTRUCTURE, "Megastructure");
ChoiceDialog<String> picker = new ChoiceDialog<>(stationLabel,
        List.of(stationLabel, weaponLabel, megaLabel, transportLabel));
```

Plus the corresponding dispatch arm:

```java
} else if (label.equals(megaLabel)) {
    openMegastructureEditor(null);
}
```

### 5.4 `onEdit()` switch — add the fourth case

Currently (line 482):

```java
switch (construct) {
    case StationDesign s -> openStationEditor(s);
    case WeaponInstallation w -> openWeaponEditor(w);
    case TransportNode t -> openTransportEditor(t);
    default -> { /* defensive */ }
}
```

Adds:

```java
case Megastructure m -> openMegastructureEditor(m);
```

### 5.5 `openMegastructureEditor` + `persistMegastructure` — new methods

Mirror `openStationEditor` + `persistStation` exactly:

```java
private void openMegastructureEditor(Megastructure existing) {
    FxThread.assertFxThread();
    MegastructureEditorDialog dialog = new MegastructureEditorDialog(existing);
    Optional<Megastructure> result = dialog.showAndWait();
    result.ifPresent(this::persistMegastructure);
}

private void persistMegastructure(Megastructure draft) {
    // Run the JPA save off the FX thread (matches persistStation's shape):
    Task<Megastructure> task = new Task<>() {
        @Override protected Megastructure call() {
            return megastructureService.save(draft);
        }
    };
    task.setOnSucceeded(e -> {
        statusLabel.setText(get("status.saved"));
        loadAsync();
    });
    task.setOnFailed(e -> {
        log.error("Failed to save megastructure", task.getException());
        statusLabel.setText(get("status.saveFailed"));
    });
    new Thread(task, "InstallationDesignerPanel-saveMegastructure").start();
}
```

This adds a constructor-injection dependency on `MegastructureDesignerService`. The panel's constructor signature grows by one parameter.

### 5.6 Properties bundle additions

`construct.properties` needs:

- `kind.MEGASTRUCTURE=Megastructure`
- Any subtype-filter label keys following the existing `kind.<X>` pattern.

The editor dialog's existing `editor.megastructure.*` keys (from D.7 Step 5) cover its own surface.

### 5.7 What the user will see after Phase D.8 lands

- **Kind filter combo**: `All / Station / Weapon Installation / Megastructure / Transport Node`.
- **New Construct picker**: four options, including Megastructure.
- **Edit on a Megastructure row**: opens `MegastructureEditorDialog`.
- **Panel rows after first launch**: Troy as a Megastructure (one row), plus the 8 real stations, plus SAPL + SheVa Gun. The universe tab strip will show `All / Real / Proposed / Aldenata / Troy Rising`.
- **On launch with a fresh DB**: all four subtype tables seed cleanly.
- **On launch with a stale DB**: missing entries get inserted; existing rows survive untouched.

---

## 6. The end-to-end test that catches this class of regression

### 6.1 Test class and shape

New test class `CatalogSyncIntegrationTest` at `com.teamgannon.trips.construct.CatalogSyncIntegrationTest`. Uses `@DataJpaTest` + manual seeder invocation, or a narrower `@SpringBootTest` slice that includes the construct-registry + four services + four seeders + Flyway.

**Critical contract:** no service mocking. The test exercises the real `DefaultConstructRegistry` constructed from the real services constructed against the real (in-memory H2) JPA repository, after the real seeders have run against a real freshly-Flyway-migrated schema.

```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=validate",
    "spring.flyway.enabled=true",
    "spring.flyway.baseline-on-migrate=true",
    "spring.flyway.baseline-version=1",
    "spring.flyway.locations=classpath:db/migration"
})
@Import({StationDesignerService.class, WeaponInstallationDesignerService.class,
         TransportNodeService.class, MegastructureDesignerService.class,
         StationDesignMapper.class, WeaponInstallationDesignMapper.class,
         TransportNodeMapper.class, MegastructureDesignMapper.class,
         SpaceshipDesignMapper.class, DefaultConstructRegistry.class})
class CatalogSyncIntegrationTest { ... }
```

### 6.2 The four critical scenarios

Four scenarios MUST be covered by name. Each captures a distinct failure mode the existing test suite has not exercised.

**Scenario S1 — empty database → seed → all 13 entries appear** (seed-from-empty). Fresh in-memory H2, Flyway V1…V12 runs, all four seeders fire, assert `DefaultConstructRegistry.allById()` returns the full catalog (1 Megastructure + 8 StationDesigns + 2 WeaponInstallations + 2 SpaceshipDesigns from the SHIP path if exercised + 0 TransportNodes = 13 from this slice or 11 if SHIP is not in the test scope). Every catalog id present.

**Scenario S2 — pre-seeded database with stale data → upgrade path** (THE D.5-day-one test). Setup: simulate the user's actual production DB by inserting a single legacy random-UUID Troy row directly into `station_design` before any seeder runs (mirrors what a pre-D.5 build would have left). Run Flyway V12 (the cleanup migration), then run all four seeders, then assert: (a) the legacy `station_design` Troy row is gone (V12 deleted it); (b) the `megastructure` table has Troy at `id = 'catalog-troy'` (the seeder inserted it); (c) the 8 D.5 stations are present at `'catalog-*'` ids; (d) SAPL and SheVa are present at `'catalog-sapl'` and `'catalog-sheva-gun'`. **This is the scenario that would have caught D.5's silent regression on the day it landed.**

**Scenario S3 — multi-launch idempotency**. Boot the application twice (or simulate by running `syncCatalogEntries()` twice in the same JVM after Flyway). After the second sync, assert: every table's row count is unchanged from the first sync; every seeder returns `0` inserted on the second invocation.

**Scenario S4 — user-edit preservation**. Insert a Catalog station (e.g. ISS) into JPA at the new stable id `'catalog-iss'` with a deliberately modified field — say `description = 'user-edited description'`. Run `syncCatalogEntries()`. Assert: the existsById short-circuit fires, no save happens, the row's `description` still reads `'user-edited description'`. Then for completeness, run sync a second time; same outcome.

### 6.3 Test methods (concrete list)

Each as its own `@Test` so failures pinpoint which contract regressed. Mapped to the §6.2 scenarios:

| # | Method name | Scenario |
|---|---|---|
| 1 | `stationSyncSeedsFromEmpty` | S1 |
| 2 | `weaponInstallationSyncSeedsFromEmpty` | S1 |
| 3 | `transportNodeSyncSeedsFromEmpty` | S1 |
| 4 | `megastructureSyncSeedsFromEmpty` | S1 |
| 5 | `registryReturnsFullCatalogPostSync` | S1 (aggregate) |
| 6 | `registryMegastructureBucketContainsTroyAtCatalogTroyId` | S1 (specific) |
| 7 | `panelLoadFromRegistryReturnsFullCatalog` | S1 (panel slice) |
| 8 | `upgradePathFromLegacyTroyResolvesToCatalogTroy` | **S2** — THE day-one regression catch |
| 9 | `syncIsIdempotentAcrossSecondBoot` | S3 |
| 10 | `syncPreservesUserEditedRow` | S4 |
| 11 | `syncDoesNotDeleteOrphanRows` | S4 (sibling — covers user-created rows) |

The S2 test (`upgradePathFromLegacyTroyResolvesToCatalogTroy`) is **the regression guard** for the failure mode that D.5 silently introduced and shipped for one day. Future Catalog-shape edits that don't propagate through the seeders or the registry trip this test.

### 6.4 What the test is allowed to do; what it is not

**Allowed:**
- Boot Spring against an in-memory H2.
- Run Flyway.
- Construct real services + the real registry from injected beans.
- Insert / delete rows directly via the repository in setup or assertion code.

**Not allowed:**
- Mock `assetsByKind`, `findAllAsAssets`, or any of the per-service read methods. Once the test mocks the JPA-backed read, it has reverted to the shape that masked D.5.
- Skip the seeder invocation. The seeder *must* be exercised — that's the contract under test.

---

## 7. Rejected alternatives

### 7.1 Option (b) — upsert / overwrite

Sync-by-id rejected the "always overwrite from Catalog" variant. Rationale:

- A user who edits ISS's `description` in the dialog and saves expects the edit to persist across launches. An overwrite sync would silently wipe it.
- The Catalog is the **canonical seed**, not the **authoritative source-of-truth at runtime**. After seed, JPA owns the data.
- The behavior of "overwrite" varies depending on whether the editor saved the user's edit before or after the next sync. Order-dependence is a foot-gun.

Insert-only sync (the chosen path) is conservative and reversible: nothing the sync does can destroy user state.

### 7.2 Option (c) — Catalog-only, no JPA persistence for Megastructure

Rejected. Step 7 chose Catalog-direct for Megastructure as a temporary expedient, with a javadoc-flagged "to be reconsidered". The downsides:

- The Step 5 editor's `buildDraft()` produces a fresh `Megastructure` with a fresh id. If there's no JPA persistence, that draft has no save path; the user loses their work on close.
- Cross-subtype consistency: SHIP / STATION / WEAPON_INSTALLATION are JPA-backed. Megastructure being the lone Catalog-direct outlier is an architectural anomaly that will rot in code review and produce surprise behavior in future phases (Phase E onwards, where `Megastructure` participates in route-finding etc.).

Closing the persistence pipeline (the chosen path) restores the four-subtype symmetry.

### 7.3 Option (d) — keep `seedFromCatalogIfEmpty` semantics but layer a separate "catalog version check" on top

Considered. Shape: each service stores a `last_seeded_catalog_version` row; on launch, compare to the current Catalog version and re-seed if higher. Rejected because:

- It adds a new mutable schema table (`catalog_version`) just to encode the version check.
- The "what's a Catalog version" question doesn't have a clean answer — Catalog has no version field, and adding one is its own design debate.
- It still doesn't handle the user-edit case (versions would either trigger overwrite, or never overwrite, and the simple sync-by-id avoids the question entirely).

Sync-by-id solves the same problem in less code and with cleaner semantics.

### 7.4 Option (e) — sync at panel load time instead of `ApplicationReadyEvent`

Considered. Shape: drop the `@EventListener` seeders entirely, have `InstallationDesignerPanel.loadAsync()` call `syncCatalogEntries()` before reading from the registry. Rejected because:

- The sync writes to the DB. Calling it from a user-facing UI event ties UI latency to disk I/O.
- The panel isn't the only consumer; the `SpaceshipDesignerPanel` and route-finding services would also need their own sync triggers, fragmenting the contract.
- `@EventListener(ApplicationReadyEvent)` is the established pattern; deviating without strong reason is noise.

Application-ready time is the right phase boundary.

---

## 8. Notes for the coding agent verifying this design against the codebase

Before writing the implementation, verify:

1. **The existing `*CatalogSeeder` beans — confirmed at design time.** A grep of `tripsapplication/src/main/java` for `*CatalogSeeder*` files returned exactly two: `StationCatalogSeeder` and `WeaponInstallationCatalogSeeder`. **No `TransportNodeCatalogSeeder` exists** — the `TransportNodeService.seedFromCatalogIfEmpty()` method is defined but unwired (its javadoc explicitly states "kept for pattern"). **Phase D.8 creates `TransportNodeCatalogSeeder` from scratch** in `com.teamgannon.trips.spaceshipmodeller.service`, mirroring `StationCatalogSeeder` exactly. The four subtypes (Station, WeaponInstallation, TransportNode, Megastructure) all end up with parity: a `*CatalogSeeder` bean + an `@EventListener(ApplicationReadyEvent.class)` method + a delegating call to `service.syncCatalogEntries()`. The Step 1 agent should re-confirm this seeder-count audit and flag any drift.

2. **The four `*DesignerService.seedFromCatalogIfEmpty()` methods.** Confirm Station, WeaponInstallation, TransportNode all carry the method. Report on any divergence from the canonical seed-on-empty shape. (Megastructure doesn't have one yet — Phase D.8 ships its `syncCatalogEntries()` from day one.)

3. **`Catalog` constant id stability.** Audit every `Catalog.X` constant. Categorize: which use `UUID.randomUUID().toString()` (random per JVM), which use deterministic string literals, which use anything else. The §3.2 mapping table assumed five UUID-random constants; the audit should confirm or correct.

4. **Existing JPA rows in test-DB fixtures.** Are there any test files that pre-populate JPA rows with specific ids? If so, those ids may collide with the new `"catalog-*"` ids and need updating in lockstep.

5. **`MegastructureRepository` does NOT already exist** under any name. Confirm.

6. **`DefaultConstructRegistry` constructor signature.** Today it takes `SpaceshipRepository, SpaceshipDesignMapper, StationDesignerService, WeaponInstallationDesignerService, TransportNodeService` (5 args). The D.8 work bumps to 6 args by adding `MegastructureDesignerService`. Confirm the existing list and verify all test fixtures that construct `DefaultConstructRegistry` directly (likely `DefaultConstructRegistryTest`) will need updating.

7. **`InstallationDesignerPanel` constructor signature.** Today's constructor at line 139ish takes `(ConstructRegistry registry, StationDesignerService stationService, WeaponInstallationDesignerService weaponInstallationService, TransportNodeService transportService)` — 4 args. The D.8 work adds `MegastructureDesignerService megastructureService` — bump to 5 args. Confirm and inventory the test fixtures that construct the panel.

8. **Properties bundle.** Confirm the existing `kind.STATION`, `kind.WEAPON_INSTALLATION`, `kind.TRANSPORT_NODE` keys in `construct.properties`. Add `kind.MEGASTRUCTURE` in lockstep with the new constant.

9. **`Catalog.all()` filter performance.** The sync inserts via per-row `existsById` checks. For 13 entries today this is fine; if the catalog grew to thousands, the per-row check would matter. Don't optimize yet — note the constraint.

10. **The existing `FlywayBaselineSmokeTest`.** Confirm it stays green after Phase D.8. It validates entity↔schema; nothing about D.8's repository / service additions should break it.

If any of these assumptions are wrong, flag clearly before starting implementation.

---

## 9. Done definition

- Step 1 verification report posted, divergences flagged if any.
- All four `*DesignerService.seedFromCatalogIfEmpty()` methods renamed to `syncCatalogEntries()` with the §3 contract.
- All `*CatalogSeeder` `@EventListener` methods call the renamed `syncCatalogEntries()`. Log lines updated.
- `MegastructureRepository`, `MegastructureDesignerService`, `MegastructureCatalogSeeder` exist and are auto-wired.
- `DefaultConstructRegistry.loadMegastructures()` reads through `megastructureDesignerService.findAllAsAssets()`. Step 7's Catalog-direct javadoc replaced.
- `InstallationDesignerPanel`: `loadFromRegistry` loads MEGASTRUCTURE; `kindFilter` has 5 entries; `onNew` picker has 4 entries; `onEdit` switch has 4 cases; `openMegastructureEditor` + `persistMegastructure` exist; subtype filter handles MEGASTRUCTURE; properties bundle has the needed keys.
- Every Catalog constant carries a stable string id (`"catalog-<slug>"` or `"real-station-<slug>"` etc.). Zero `UUID.randomUUID()` calls remain in `Catalog`.
- `CatalogSyncIntegrationTest` exists at `com.teamgannon.trips.construct.CatalogSyncIntegrationTest` with the §6.2 test methods and the §6.3 no-mocking constraint.
- All existing tests pass after the updates documented in §8 verification.
- `constructs-feature-plan-v2.md` gains a "Phase D.8 — Catalog sync-by-id + Megastructure persistence pipeline ✅ DONE" subsection between D.7 and Phase E.

Expected test-count delta: ~10–15 new tests from `CatalogSyncIntegrationTest`, plus updates to existing seeder tests for the rename. Net positive.

---

## 10. After D.8 — what this unblocks

Phase D.8 is a load-bearing prerequisite for Phase E.1 (jump-point computation + multi-network gates + catalog-reference link on `SolarSystemFeature`). Three concrete enabling effects:

- **The catalog actually reaches the running app.** Today, Phase E.1's design assumes "place a Catalog construct into a solar system via `SolarSystemFeature`" — but the catalog hasn't been reaching the running registry for either D.5 stations or D.7's Troy. After D.8, the registry is the honest source for "what catalog entries exist", and E.1's placement model can reference them by id with confidence that the id-keyed lookup will resolve.
- **Megastructure is reachable as a catalog kind.** E.1's `catalogReferenceKind` discriminator needs to cover all four `SpaceAsset` subtypes; D.8 makes Megastructure a fully-wired subtype, so the E.1 discriminator can treat it symmetrically (a Troy placed into Sol's `SolarSystemFeature` resolves the same way as a Death Star — both go through `MegastructureDesignerService.findById(...)`).
- **The end-to-end test class is in place.** E.1's tests can extend `CatalogSyncIntegrationTest` (or a sibling) without re-establishing the Spring + Flyway + seeder boot harness; the JPA-backed read path is already validated.

Phase E.1 also picks up the deferred-but-architecturally-noted orphan-deletion concern from §3.3: when the in-system feature model lands, an asset placed in a system that's later removed from the Catalog needs a defined behavior (orphan reference → fall back to display-only? prevent deletion of in-use Catalog entries?). D.8 doesn't answer this; it just makes the question well-formed.

---

## Appendix — quick failure-mode review

A reader of this design doc should be able to answer:

- *Does sync-by-id ever overwrite a user's edit?* No (§3.3).
- *Does sync-by-id ever delete a user's custom row?* No (§3.3).
- *What happens if Catalog adds an entry and the user has an existing row with the same id?* Sync skips it (existsById is true); the existing row stays.
- *What happens if Catalog removes an entry the user has in JPA?* Sync ignores; the row stays (orphan, but not deleted).
- *What happens if a Catalog constant's id changes between releases?* The sync sees the new id as missing, inserts a fresh row. The old row stays under its old id. This is the failure mode §3.2 is designed to prevent (stable ids forever after D.8).
- *What happens if the user constructs a `Megastructure` in the editor with no `id`?* The compact ctor + entity layer generate one (current behavior). It saves with that id. Sync ignores it on future launches because the id isn't in Catalog.
- *Does the end-to-end test exercise the seeder path?* Yes (§6.3, explicit "not allowed to mock").
