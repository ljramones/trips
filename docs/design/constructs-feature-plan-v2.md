# Constructs Feature — Plan v2

**Status**: design, ready for implementation
**Owner**: Larry Mitchell (intent), implementing-agent (build)
**Date**: 2026-05-28
**Supersedes**: [`constructs-feature-plan.md`](./constructs-feature-plan.md) (v1)
**Grounded in**: [`constructs-existing-hierarchies.md`](./constructs-existing-hierarchies.md) (inventory)

This plan reconciles the original Constructs feature intent against the two existing sealed hierarchies (`SpaceAsset`, `SpaceInfrastructure`) that v1 didn't survey. Citations in this document are to inventory-note section numbers (§N) rather than file paths.

---

## 1. Reconciliation log

Walking the v1-vs-existing overlap table row by row. Each verdict is grounded in the inventory note.

| v1 entity | Existing entity | Verdict | Rationale |
|---|---|---|---|
| `Construct` (sealed interface) | `SpaceAsset` (sealed) + `SpaceInfrastructure` (sealed) | **already covered, dual** | Inventory §1.1 — both existing hierarchies already extend `Cataloged`, which *is* the umbrella identity contract v1's `Construct` was trying to be. v2 keeps both hierarchies; "Construct" becomes a UI-only label, not a new type. |
| `ConstructIdentity` (record) | `Cataloged` (interface) | **rename, no new type** | Inventory §1.1 — `Cataloged` already carries `id, name, source, faction, concealed, description`. The extra fields v1 wanted (`designation, category, era, createdAt, mobility`) already live on the SpaceAsset interface or subtype records. No new identity type required. |
| `Spaceship` (record) | `SpaceshipDesign` (record) | **already covered, keep name** | Inventory §1.4 — same data, same role. v2 does not rename it; "Spaceship" is the UI label, `SpaceshipDesign` is the class name. |
| `Starbase` / `BattleMoon` / `MiningStation` | `StationDesign` + `StationType` enum | **extension via enum** | Inventory §1.5, §1.8. `StationType` already enumerates `BATTLESTATION, GATE_FORT, ORBITAL_CITADEL, SHIPYARD, HABITAT, CYLINDER, GENERATION_SHIP, OUTPOST, PIRATE_BASE, DEPOT`. Battle moon → `BATTLESTATION`. Asteroid mining station → `OUTPOST` or add `MINING_STATION` to the enum if the product owner wants the distinction (see open question Q1). |
| `PlanetaryDefenceInstallation` (record) | `WeaponInstallation` + `Emplacement.GROUND_FIXED` + `InstallationType.DEFENCE_BATTERY` | **already covered** | Inventory §1.6, §1.8. The exact combination (`InstallationType.DEFENCE_BATTERY`, `Emplacement.GROUND_FIXED`) is what a planetary-defence installation is. No new record. |
| `JumpGate` (record) | `TransportNode` + `NodeType.{RING_GATE, JUMP_POINT, WORMHOLE_MOUTH, PORTAL}` + `connectedNodeIds` | **already covered** | Inventory §2.3, §2.5. The model is complete. What's missing is a UI for editing them and a route-finder integration. No new record. |
| `Mobility{FIXED, MOBILE_LIMITED, MOBILE}` enum | `Mobility{FIXED, STATIONKEEPING, MANEUVERABLE}` enum | **already exists** | Inventory §1.8. Existing wins; v2 does not redefine. |
| `category` discriminator (single enum) | `AssetKind{SHIP, STATION, WEAPON_INSTALLATION}` + `InfrastructureKind{TRANSPORT_NODE, CONDUIT}` | **two existing discriminators, keep both** | Inventory §1.3, §2.2. v2 keeps both at the domain level; the UI projects them onto a single user-facing dropdown if needed (see §3). |
| Single-table `construct` with `payload_json` CLOB | `SpaceshipEntity` (flat) + `SpaceshipDesignMapper` + JSON-in-LOB *collection fields only* | **wrong shape, switch to per-subtype flat entities** | Inventory §5.1–5.2. The existing pattern serialises *collections* to LOBs, not whole entities. v2 follows the existing flat-entity pattern (one entity per subtype that needs persistence). See §4 below for the Issue-46 analysis. |
| `OtherConstruct` (catch-all record) | (no equivalent) | **dropped** | The existing sealed hierarchies don't permit a "custom-fields-bag" subtype; nothing in this design needs one. If a future need arises, add a real subtype rather than a `Map<String,String> customFields`. |

**Headline**: v2's net new types count drops from v1's eight subtypes + identity record + discriminator enum + table + migration → **zero new sealed-hierarchy types, zero new identity records, zero new enums, four new JPA entities (for the three currently-unpersisted SpaceAsset subtypes and `TransportNode`), one new UI panel**.

---

## 2. Architecture decision (reconciliation prompt §3)

### v2 keeps `SpaceAsset` and `SpaceInfrastructure` as parallel sealed hierarchies. "Construct" is a UI-only label.

Three independent reasons make collapsing them into a single sealed `Construct` wrong:

1. **Different accessor contracts.** `SpaceAsset` carries `armaments`, `dryMassTons`, `operationalState`. `SpaceInfrastructure` carries `connectedNodeIds`, position fields, throughput. Collapsing means every method becomes optional on the common interface (forcing `Optional<T>` returns or nullable accessors), or the common interface shrinks to just `Cataloged` — at which point the collapse adds nothing.
2. **Different downstream consumers.** Inventory §3.6 — `EconomyRegistry.assetsById` is `Map<String, SpaceAsset>`. `Stockpile.ownerAssetId` and `IndustrialOperation.hostAssetId` are FKs to `SpaceAsset.id` *only*. Conflating them with infrastructure nodes would require the registry / tick engine to filter on every read. Splitting by capability (asset vs infrastructure) is exactly what those FK types are for.
3. **The common identity is already there.** Both hierarchies extend `Cataloged`. The thing v1 was trying to introduce already exists. The umbrella concept lives at the `Cataloged` level; the sealed `Construct` introduction was unnecessary.

The **UI** sees one concept ("Construct"). The **domain** keeps two sealed hierarchies. The two UI panels query both via a small aggregation seam.

### The aggregation seam: `ConstructRegistry` (read-only)

```java
public interface ConstructRegistry {
    /** All Cataloged objects across both hierarchies, by id. */
    Map<String, Cataloged> allById();
    /** SpaceAsset subset, by AssetKind. */
    Map<AssetKind, List<SpaceAsset>> assetsByKind();
    /** SpaceInfrastructure subset, by InfrastructureKind. */
    Map<InfrastructureKind, List<SpaceInfrastructure>> infrastructureByKind();
}
```

Implemented by a Spring `@Component` that reads from the four entity repositories (see §4). No new sealed type; no parallel hierarchy. The UI panel asks the registry "give me everything that isn't a SHIP" for the Installations Designer, "give me everything that *is* a SHIP" for the Spaceship Designer.

---

## 3. UI

### Two parallel panels (kept from v1, well-grounded)

| Panel | Shows | Hides |
|---|---|---|
| **Spaceship Designer** (existing, `SpaceshipDesignerPanel`) | `SpaceAsset` rows where `kind() == SHIP` | everything else |
| **Installations Designer** (new, `InstallationDesignerPanel`) | `SpaceAsset` rows where `kind() != SHIP` *plus* all `SpaceInfrastructure` rows | spaceships |

Both panels share enough that a base class is worth extracting once both exist; that's Phase D below, not Phase A. Don't pre-build the base.

### Menu

- `Design → Spaceship Modeller…` (existing — kept)
- `Design → Installations Designer…` (new — name pending product-owner confirmation; see §6 Q3)

The global "Transfer Planner..." menu item was already removed (commit `ec4d6663`) because it's only meaningful in a Solar System context. The same rule applies to anything Constructs adds to the menu bar.

### Per-subtype edit dialogs

One dialog class per concrete subtype:

| Subtype | Dialog | Status |
|---|---|---|
| `SpaceshipDesign` | `SpaceshipEditorDialog` | exists |
| `StationDesign` | `StationEditorDialog` | new |
| `WeaponInstallation` | `WeaponInstallationEditorDialog` | new |
| `TransportNode` | `TransportNodeEditorDialog` | new |
| `Conduit` | (deferred; not in Phase A–D scope) | — |

Installations Designer's "New…" button opens a subtype picker, then routes to the right dialog.

---

## 4. Persistence (reconciliation prompt §5 — the LazyInitializationException analysis)

### v2 follows the existing flat-entity-per-subtype pattern, not v1's `payload_json` CLOB

Inventory §5.1–5.2 establishes the precedent: `SpaceshipEntity` is a flat JPA entity with `@Lob`-annotated *collection* fields (`carriedCraftJson`, `armamentsJson`) that round-trip through Jackson in the mapper. v2 mirrors this pattern for the three currently-unpersisted SpaceAsset subtypes plus `TransportNode`:

| New entity | Mirrors | Collection LOBs |
|---|---|---|
| `StationEntity` | `StationDesign` (Inventory §1.5) | `carriedCraftJson`, `armamentsJson` |
| `WeaponInstallationEntity` | `WeaponInstallation` (Inventory §1.6) | `armamentsJson` |
| `TransportNodeEntity` | `TransportNode` (Inventory §2.3) | `connectedNodeIdsJson` |
| (`ConduitEntity` deferred to Phase E) | `Conduit` (Inventory §2.4) | — (no collection fields) |

Each entity has its own table (`STATION_DESIGN`, `WEAPON_INSTALLATION`, `TRANSPORT_NODE`), its own repository, its own mapper. No discriminator column; no shared table.

### Issue 46 analysis: every CLOB read path

The Phase 7.8 LAZY-fetch revert (Issue 46 / `trips-full-codebase-review-2026.md`) found ~30 cross-tx readers of `@Lob` fields that blew up `LazyInitializationException`. Any new entity with a LOB must enumerate its read paths.

For each new entity, the LOBs are *collection* fields populated only inside the mapper's `toDomain`. The mapper runs at service-layer load time, inside the transactional boundary. The resulting *domain record* is what travels to the UI; the UI never touches the entity directly.

| Read path | Inside tx? | Cache? | Verdict |
|---|---|---|---|
| `StationDesignerService.findAll()` → `mapper.toDomain(entity)` → list of `StationDesign` | yes (service is `@Transactional`) | L2 covers re-reads | **safe** |
| `StationDesignerService.findById(id)` → `mapper.toDomain(entity)` → `StationDesign` | yes | L2 covers re-reads | **safe** |
| `InstallationDesignerPanel.reload()` → `service.findAll()` | calls service, never touches entity | n/a | **safe** |
| `StationEditorDialog` shown for editing | takes `StationDesign` (domain record), never touches entity | n/a | **safe** |
| `InstallationDesignerPanel.applyFilters()` → operates on in-memory `List<StationDesign>` | n/a | n/a | **safe** |
| JSON export (`SpaceshipJsonService`-equivalent) | runs at service layer, inside tx | n/a | **safe** |

The pattern is identical to `SpaceshipEntity` / `SpaceshipDesignMapper`, which the codebase has been running without `LazyInitializationException` since the Issue 46 revert. Following the same pattern carries the same safety property.

### Why not a single `construct` table with `payload_json`?

v1's proposed shape would have introduced a whole-entity JSON CLOB. That has two problems:
1. **New persistence shape, no precedent.** The repo's existing pattern is flat columns + per-collection-field JSON LOBs. A whole-entity JSON column is novel; Issue 46's analysis doesn't carry over.
2. **Queryability loss.** With per-subtype tables, the JPA query `findByStationTypeAndMobility(...)` works natively. With `payload_json`, every subtype-specific filter becomes a service-layer post-filter on a full table scan.

The flat-entity pattern is more boilerplate but correctly applies the existing safety analysis.

### Migration plan

Inventory §1.9: `StationDesign`s and `WeaponInstallation`s are only in `Catalog.all()` today. The migration is one-shot: on app startup, if the new tables are empty, seed them from the catalog constants (mirrors the existing `seedTemplatesIfEmpty()` pattern in `SpaceshipDesignerPanel`).

Flyway migrations (post Phase A0):
- ~~**V6**~~ — **taken by Phase A0**. `V6__spaceship_entity_concealed_and_operational_state.sql` adds the two columns that close the round-trip-loss bug documented in Inventory §4.4. v2 Phase A's new entity migrations therefore shift one slot down.
- **V7** — create `STATION_DESIGN` table (matches `StationEntity` fields)
- **V8** — create `WEAPON_INSTALLATION` table
- **V9** — create `TRANSPORT_NODE` table

Each migration is additive. No data drops. No changes to `SPACESHIP_DESIGN` after V6.

---

## 5. Phased rollout

Each phase is a self-contained commit set with a full regression run.

### Phase A — `ConstructRegistry` wire-up + station persistence ✅ DONE (2026-05-28)
1. ~~Define `ConstructRegistry` interface + `DefaultConstructRegistry` `@Component`.~~ **Landed in Phase A0** as a ships-only skeleton.
2. ✅ Implemented `StationEntity` + `StationDesignMapper` + `StationRepository` + `StationDesignerService` (mirrors the SpaceshipEntity pattern; collection LOBs, not whole-entity payload_json).
3. ✅ Flyway **V7** `V7__station_design_table.sql`. Idempotent `CREATE TABLE IF NOT EXISTS` + index `IF NOT EXISTS` lines. Seed-on-empty wired via `StationCatalogSeeder` listening for `ApplicationReadyEvent`.
4. ✅ Comprehensive mapper round-trip: 33 cases including every `StationType`, every `Mobility`, every `OperationalState`, every `TechLevel`, plus the non-default cases (concealed, allegiance ≠ faction, `auxiliaryDrive` on non-fixed stations). Phase A0 lesson honoured: no field is silently dropped.
5. ✅ `DefaultConstructRegistry.assetsByKind(STATION)` flipped from empty to a read through `StationDesignerService`; `allById()` now spans ships + stations. WEAPON_INSTALLATION and every `infrastructureByKind` bucket stays empty until Phase B.

### Phase B — weapon installation + transport node persistence ✅ DONE (2026-05-28)
1. ✅ `WeaponInstallationEntity` / mapper / repository / service / seeder. Flyway **V8** (`weapon_installation` table, idempotent `CREATE TABLE IF NOT EXISTS` + indexes). `SAPL` + `SHEVA_GUN` seed on `ApplicationReadyEvent` via `WeaponInstallationCatalogSeeder`. `DefaultConstructRegistry.assetsByKind(WEAPON_INSTALLATION)` reads through the service.
2. ✅ `TransportNodeEntity` / mapper / repository / service (no seeder by design — `TransportNodeService.seedFromCatalogIfEmpty()` exists for pattern symmetry but no Spring component triggers it; `Catalog` has no transport-node entries today). Flyway **V9** (`transport_node` table, JSON LOB for `connectedNodeIds`; no FK to `transport_node.id` — in-memory `GraphRegistry` keeps dangling-id validation). `DefaultConstructRegistry.infrastructureByKind(TRANSPORT_NODE)` reads through the service. `CONDUIT` bucket stays empty per §6.1 Q3 deferral.

### Phase C — Installations Designer panel (read-only) ✅ DONE (2026-05-28)
1. ✅ `InstallationDesignerPanel` (`com.teamgannon.trips.construct.ui`) — BorderPane with filter strip, table, details split. Reads through `ConstructRegistry` for stations + weapon installations + transport nodes (ships handled by Spaceship Modeller; conduits deferred per §6.1 Q3). All registry reads run on background `Task`; results applied via `applyConstructs` on the FX thread (Issue 11 discipline; `FxThread.assertFxThread()` guards on every FX-thread method).
2. ✅ `Design → _Installations Designer…` menu item with mnemonic `I` (Issue 36 convention). Reuses the lazy single-Stage pattern of `openSpaceshipModeller`. `panel.loadAsync()` fires after the Stage is shown so a slow registry read can't delay the window appearing.
3. ✅ Per-subtype details templates via Java pattern matching: `StationDesign` shows mobility / allegiance / dimensions / crew / carrier-capable / armaments+carried-craft counts; `WeaponInstallation` shows installation type / emplacement / mobile / mass / footprint / crew / armament count; `TransportNode` shows node type / position / throughput / instantaneous-or-traversal / connected-node count. All field labels live in `construct.properties` (scoped per-feature bundle).
4. ✅ Filters: Kind → Subtype (subtype combo repopulates by Kind), Faction, Category, plus case-insensitive name search. All filter changes operate on the in-memory list — no extra registry reads. Tooltips on every focusable control (Issue 35); a11y annotations on every focusable control (Issue 49). `trips-*` CSS classes only, no inline `setStyle` (Issue 50). Last-column flex resize policy (Issue 34).

### Phase D — Edit dialogs for the three non-spacecraft subtypes ✅ DONE (2026-05-28)
1. ✅ `StationEditorDialog`, `WeaponInstallationEditorDialog`, `TransportNodeEditorDialog` — three independent classes (no shared base; v2 §5 Phase F's extraction stays deferred per the prompt). Each mirrors `SpaceshipEditorDialog`'s structure: programmatic sections, inline validation list, OK disabled while invalid, canonical-record-constructor `buildDraft()`, tooltips on every field, accessibility annotations on every focusable control, `trips-*` CSS classes only.
2. ✅ `New…` → `ChoiceDialog<String>` subtype picker (Station / Weapon Installation / Transport Node) → routes to the matching editor dialog.
3. ✅ Edit + Delete actions enabled iff a row is selected; Delete confirms via `Alert.AlertType.CONFIRMATION`. Saves run on a background `Task` (the panel calls `service.save(…)` off the FX thread); `setOnSucceeded` refreshes the panel via the existing `loadAsync()` — same FX-thread discipline as Phase C.
4. ✅ UI invariants enforced at the dialog level: `Mobility.FIXED` disables + clears the station auxiliary-drive combo; `instantaneousTransit=true` disables + zeros the transport traversal-time field. Both mirror the domain compact-constructor invariants.
5. ✅ Panel constructor now takes the three subtype services in addition to the registry; `DesignMenuController` wires them in. Registry remains read-only (no interface change).

### Phase D.5 — Universe tab strip + real space stations seed ✅ DONE (2026-05-28)
A small additive phase wedged between D and E so the panel has a multi-universe demo surface.
1. ✅ `InstallationDesignerPanel` grew a universe tab strip (FlowPane of ToggleButtons, sticky single-select) above the existing filter row. Tabs are data-driven from the distinct `source` values across loaded constructs; order is "All" → "Real / Proposed" (pinned second when present) → remaining sources alphabetical. Tab selection composes with the existing Kind/Subtype/Faction/Category/search filters.
2. ✅ Eight real / proposed Earth space stations added to `Catalog` under `source = "Real / Proposed"`: ISS, Tiangong, Mir, Skylab, Salyut 1, Salyut 7, Lunar Gateway, Axiom Station. All `Mobility.STATIONKEEPING`, `techLevel = CONTEMPORARY`, `category = "Crewed orbital station"`. Numeric facts web-verified at population (NASA / Wikipedia / NSSDC / SpaceNews / SpacePolicyOnline) and cited in each constant's javadoc; INFERRED values flagged in the description string per the TROY convention. `OperationalState`: OPERATIONAL for ISS + Tiangong; SALVAGED for Mir, Skylab, Salyut 1, Salyut 7; UNDER_CONSTRUCTION for Lunar Gateway (with the March 2026 program-suspension caveat in the description) and Axiom Station. `Catalog.all()` extended; `StationDesignerService.seedFromCatalogIfEmpty()` and `DefaultConstructRegistry.assetsByKind(STATION)` adapt without code change because they iterate `Catalog.all()` dynamically.
3. ✅ Tests: 3 new panel tests for tab strip ordering / selection / Kind-filter composition; parameterised `StationDesignMapperTest.realStationRoundTrips` covers the 8 new entries (data-driven from `Catalog.all()`).

### Phase D.6 — Function + provenance axes ✅ DONE (2026-05-28)
Adds the functional and provenance axes to `StationDesign` per the v2 design doc and `space-assets-functional-taxonomy-v2.md`. Eight sub-steps spanning enum/record additions, JPA schema widening, editor + panel UI surfaces, catalog repopulation, and audit invariants.

1. ✅ **Step 1.5** — Mechanical relocation of `SourceType` from `com.teamgannon.trips.spaceshipmodeller.core` to `com.terranrepublic.assets` (12 import sites). Sets up `CatalogProvenance` to live in the assets package with no cross-package dependency on the spaceship modeller.
2. ✅ **Step 2** — Three new types in `com.terranrepublic.assets`: `StationFunction` enum (**30 values across six functional groups** — Military, Governance + civilian core, Industrial, Transit + infrastructure, Specialized, Catch-alls), `CatalogOperationalStatus` enum (6 values: HISTORIC / ACTIVE / PLANNED / CANCELLED / FICTIONAL / UNKNOWN — distinct axis from the physical `OperationalState`), `CatalogProvenance` record (composite of `sourceType` + `sourceUniverse` + nullable `sourceWork` + `status`, with compact-constructor defaults and a static `unknown()` factory). 20 new tests pin the value sets and the default semantics.
3. ✅ **Step 3** — Three new fields on `StationDesign`: `primaryFunction`, `secondaryFunctions` (defensively-copied immutable `Set`), `provenance`. The legacy `source` field is **dropped** per Concern A; `source()` becomes an interface override reading from `provenance.sourceUniverse()`. Canonical constructor is now 29-arg; the compatibility chain `23 → 26 → 27-compat → canonical-29` preserves every pre-D.6 call site (the 27-compat shim wraps the dropped `source` string into a default-shape `CatalogProvenance`). Compact-constructor invariants enforce defaults + the "secondary cannot contain primary" rule. 41 new tests including the parameterised 30-value `everyStationFunctionCanBePrimary`.
4. ✅ **Step 4** — Flyway **V10** adds six new columns to `STATION_DESIGN` (`primary_function`, `secondary_functions_json` CLOB, `provenance_source_type`, `provenance_source_universe`, `provenance_source_work`, `provenance_status`) with idempotent `ADD COLUMN IF NOT EXISTS` guards. One-shot `UPDATE` backfills `provenance_source_universe` from the legacy `source` column so existing rows don't lose their universe label. `StationEntity` gains six matching fields with `@PrePersist` defaults. `StationDesignMapper` bidirectional round-trip for the three new logical fields; `toDomain` switches from the 27-compat shim to the canonical 29-arg constructor. `FlywayBaselineSmokeTest` green — schema and entity stay in lockstep. 50 new mapper round-trip tests including parameterised coverage over every `StationFunction` (30), `CatalogOperationalStatus` (6), `SourceType` (4), plus the four worked-example provenance shapes (Troy / ISS / Mir / Lunar Gateway).
5. ✅ **Step 5** — `StationEditorDialog` gains a Catalog section (after Basic Information) and a Function section (between Mobility and Carried Craft). Catalog section: `ComboBox<SourceType>` with `t.label()` converter, single Universe `TextField` replacing the dropped Source field per Concern C, optional source-work field, `ComboBox<CatalogOperationalStatus>`. Function section: primary combo + multi-select secondary list with `MULTIPLE` selection mode and a soft 3-cap hint Label using `trips-text-italic-warn`. UI invariant: changing primary silently removes that value from the secondary selection. `buildDraft` switches to canonical-29 with explicit `primaryFunction` / `secondaryFunctions` / `provenance`; empty source-work field surfaces as `null` on the composite per the design's "null is no-specific-work" semantic. `InstallationDesignerPanel.stationSection` adds five new detail rows (primary function, secondary functions, source universe, source work [conditional], catalog status). 35 new tests including the parameterised 30-value `everyStationFunctionAsPrimary`.
6. ✅ **Step 6** — Catalog repopulation: all nine `StationDesign` entries switched from the compatibility-shim path to the canonical 29-arg constructor with explicit worked-example values per §5 — TROY: `DEFENSIVE / {MILITARY_COMMAND} / (SCIENCE_FICTION, "Troy Rising", "Troy Rising", FICTIONAL)`; ISS + TIANGONG: `RESEARCH / {} / (REAL, "Real / Proposed", null, ACTIVE)`; MIR + SKYLAB + SALYUT_1 + SALYUT_7: `RESEARCH / {} / (REAL, "Real / Proposed", null, HISTORIC)`; LUNAR_GATEWAY: `RESEARCH / {LOGISTICS_DEPOT} / (REAL, "Real / Proposed", null, PLANNED)`; AXIOM_STATION: `RESEARCH / {COMMERCIAL} / (REAL, "Real / Proposed", null, PLANNED)`. Zero test changes — every test relying on `Catalog.all()` reads dynamically.
7. ✅ **Step 7** — `ComboBox<StationFunction>` filter added to the Installations Designer filter strip between Kind and Subtype, with "All" sentinel + every `StationFunction` enum constant in declaration order. Predicate in `applyFilters`: when a specific function is selected, non-station rows drop out unconditionally (the "matches the function" semantic — Weapon Installations / Spaceships / TransportNodes have no function axis); for station rows, match if the selected function equals `primaryFunction` or is contained in `secondaryFunctions`. Composes with the universe tab strip + every existing filter. 8 new tests covering RESEARCH-narrows-to-8, DEFENSIVE-narrows-to-Troy, MILITARY_COMMAND-matches-via-secondary, universe-tab + function composition, "All" no-op, and the global non-station-drop.
8. ✅ **Step 8** — `CatalogAuditTest` pins 15 invariants: every catalog station has a non-UNKNOWN `primaryFunction`; secondaries never contain the primary (defense-in-depth over the compact-constructor check); `sourceUniverse` is non-blank; REAL provenance carries a real-line status (HISTORIC / ACTIVE / PLANNED / CANCELLED), SCIENCE_FICTION provenance carries FICTIONAL status; MULTI_ROLE primary requires a ≥20-char description (vacuous today, meaningful for future seeds); plus pinned-fact assertions on Troy / ISS / Mir / Lunar Gateway; and the accidental-default substring guard on `sourceUniverse`. Global iteration tests collect all violators into one failure message so a single run surfaces every offender.

### Phase D.7 — Megastructure subtype ✅ DONE (2026-05-29)
Adds `Megastructure` as a fourth sealed subtype of `SpaceAsset`, parallel to `SpaceshipDesign` / `StationDesign` / `WeaponInstallation`. Scale-class, self-contained-setting catalog assets — hollowed asteroids (Troy), purpose-built war machines (Death Star), disguised moons (Dahak), found enigmas (Rama), engineered worlds (Ringworld). Migrates Troy from `StationDesign` to `Megastructure` per the honest categorization (Troy is a 23 km hollowed asteroid with a self-contained interior setting, not a station-scale gate fort). Eight sub-steps + one preamble.

1. ✅ **Step 1.6 (preamble)** — `Mobility` enum extended 3 → 6 values (`FIXED, STATIONKEEPING, MANEUVERABLE, MOBILE_LIMITED, MOBILE, MOBILE_AUTONOMOUS`) per Divergence A resolution. The axis is shared across all `SpaceAsset` subtypes; megastructure-scale objects need finer-grained mobility distinctions (Troy: MOBILE_LIMITED with ORION pulses; Death Star: MOBILE; Dahak: MOBILE_AUTONOMOUS). New `MobilityTest` pins the 6-value set, declaration order, and ordinal stability for the original three. 5 new tests, suite at 3,297.
2. ✅ **Step 2** — `AssetKind` extended 3 → 4 values, appending `MEGASTRUCTURE` at ordinal 3 (per Divergence B resolution). One exhaustive switch site updated (`DefaultConstructRegistry.assetsByKind`) with a placeholder `case MEGASTRUCTURE -> List.of()` for Step 7 to wire. Three new enums land in `com.terranrepublic.assets`: `MegastructureArchetype` (6 values from §2), `MegastructureOriginType` (5 values), `InteriorGravityType` (6 values). 18 new tests pin value counts, declaration order, exact spellings, valueOf reachability. Suite at 3,315.
3. ✅ **Step 3** — `Megastructure` record with **30 fields** (design's stated 27 + 3 SpaceAsset bookkeeping fields surfaced by Gap 1: `designation`, `createdAt`, `modifiedAt`; per Resolution (a)). `auxiliaryDrive: DriveType` added per Divergence D so Troy can preserve its ORION drive characterization. 10 throwing-or-defaulting invariants from design §3.2 plus 3 pure defaults. `kind()` returns `AssetKind.MEGASTRUCTURE` per Divergence C. `source()` reads from `provenance.sourceUniverse()` per the D.6 Concern A pattern. `dryMassTons()` overrides as a derivation (`dryMassMegatons × 10⁶`) per Gap 2 Resolution (e) — preserves the design's scale-honest field while satisfying the `SpaceAsset` interface. `SpaceAsset` permits clause + `@JsonSubTypes` extended per Gap 3. Sealed-permits ripple caught in `CatalogTest`'s exhaustive switch (3rd call site beyond the Step 2 enum-switch inventory). 90 new tests including parameterized coverage of 7 enum axes. Suite at 3,405.
4. ✅ **Step 4** — Flyway **V11** creates the `megastructure` table with 32 columns covering the 30 record fields (timestamps as TIMESTAMP WITH TIME ZONE, collection JSON LOBs, provenance flattened into 4 columns, NOT NULL DEFAULTs matching record/entity defaults). 4 indexes (`name ASC`, `archetype`, `origin_type`, `provenance_source_universe`) per design §5.3. `MegastructureEntity` carries `@Cache(usage = READ_WRITE)` per design §5.1 — mirrors the `SolarSystem` L2-cache pattern (design said "mirroring StationDesign" but StationEntity has no L2 cache; SolarSystem does). `MegastructureDesignMapper` bidirectional round-trip with JSON LOB serialisation for `secondaryFunctions` and `armaments`. TechLevel null coerced to UNKNOWN at the mapper boundary. `FlywayBaselineSmokeTest` green — entity↔schema in lockstep. 106 new tests including parameterized coverage over 8 axes (added DriveType beyond the design's 6-axis suggestion). Suite at 3,511.
5. ✅ **Step 5** — `MegastructureEditorDialog` with 10 sections in design §8.1 order: Basic Information / Catalog / Archetype & Origin / Structural / Mobility / Function / Interior / Operational / Armaments / Validation. 30-arg `buildDraft()` using canonical constructor. `applyDefaults` produces documented defaults. UI invariants: primary→secondary auto-removal (matches D.6 pattern), soft 3-cap hint on secondary set > 3, `auxiliaryDrive` "None" sentinel via leading-null item + StringConverter (replaces the StationDesign-specific FIXED-disables-aux-drive wiring — Resolution A's "the rule stays on StationDesign only"). `hasInteriorSetting` does NOT gate interior population/gravity visibility per design §8.1 UX call. Test seams for every control. 16 tooltips + accessibility annotations. `InstallationDesignerPanel.renderDetailsForSelection` switch gains `case Megastructure m -> megastructureSection(m)`; new `megastructureSection` renders 20+ field rows. `formatSecondaryFunctions` generalized from `StationDesign`-specific to `Set<StationFunction>`-shaped (one helper, two callers). 58 new tests with 4 parameterized axes (archetype / originType / interiorGravity / station-function-as-primary). Suite at 3,569.
6. ✅ **Step 6** — Catalog repopulation: `Catalog.TROY` migrates from `StationDesign` to `Megastructure` with the worked-example values per design §7 + Divergence D resolutions: `CONVERTED_ASTEROID` archetype, `BUILT_BY_KNOWN` origin, `MOBILE_LIMITED` mobility + `DriveType.ORION` aux drive (preserves the ORION canon), 23.0 km dimension, 2.0×10⁶ MT mass (preserves CANON two-trillion-ton figure: 2.0e12 tons / 10⁶ = 2.0e6 MT), 150 km³ internal volume (mirrors prior `pressurizedVolumeM3 = 1.5e11`), 50k interior population, `NATURAL_MASS` gravity, `DEFENSIVE` primary + `{MILITARY_COMMAND, SHIPBUILDING}` secondaries, `(SCIENCE_FICTION, "Troy Rising", "Troy Rising", FICTIONAL)` provenance. `Catalog.all()` count unchanged at 13 (8 StationDesign + 2 WeaponInstallation + 2 SpaceshipDesign + **1 Megastructure**). 9 candidate-breakage tests resolved with explicit doc (CatalogAuditTest audits 7-11 cast to Megastructure; `StationDesignMapperTest.troyRoundTrips` removed — premise invalidated, real-station round-trip coverage continues via `realStations()` parameterized; `StationDesignerServiceTest.troy()` + `StationEditorDialogTest.troy()` helpers replaced with cached hand-built synthetic StationDesigns; `InstallationDesignerPanelTest` updated for the new bucket layout; `InstallationDesignerPanel`'s function filter extended to include Megastructure as a natural ripple). Suite at 3,568.
7. ✅ **Step 7** — `DefaultConstructRegistry.assetsByKind(MEGASTRUCTURE)` placeholder swapped for the real loader: `Catalog.all().stream().filter(Megastructure.class::isInstance)...`. Differs from the SHIP / STATION / WEAPON_INSTALLATION buckets (which go through JPA-backed `*DesignerService.findAllAsAssets()`) — there's no `MegastructureRepository` + `MegastructureDesignerService` pairing yet, and the Step 5 editor's `buildDraft()` has no persistence pathway, so the catalog is the only source. Javadoc on `loadMegastructures()` flags this explicitly as the hook for future service-backed migration. `allById()` also includes the megastructure bucket. 1 new test pinning Troy-in-Megastructure-bucket; `allByIdSpansAllPersistedKinds` extended. Suite at 3,569.
8. ✅ **Step 8** — `CatalogAuditTest` extended with **10 Megastructure-side global invariants** (audits 16-25): non-UNKNOWN primaryFunction; secondaries never contain primary; non-blank sourceUniverse; REAL provenance carries real-line status; SCIENCE_FICTION provenance carries FICTIONAL; MULTI_ROLE primary requires ≥20-char description (vacuous today); forbidden-default substring guard; non-UNKNOWN archetype (archetype is the primary categorization key); BUILT_BY_KNOWN requires non-blank builderPolity; FOUND_INTACT/FOUND_DAMAGED requires non-null discoveryYear. Plus **8 Troy-specific pinned-fact assertions** (audits 26-33): archetype=CONVERTED_ASTEROID, originType=BUILT_BY_KNOWN, builderPolity populated, hasInteriorSetting=true, dimensionsKm>0, mobility=MOBILE_LIMITED, auxiliaryDrive=ORION, interiorGravity=NATURAL_MASS. Audit 11 extended from MILITARY_COMMAND only to also include SHIPBUILDING per the §7 worked example. Global iteration tests collect all violators into one failure message.

### Phase D.8 — Catalog Sync + Megastructure UI Wiring ✅ DONE (2026-05-29)
Closes two regressions surfaced at the close of Phase D.7:
- **D.5 silent regression**: `seedFromCatalogIfEmpty()` short-circuited on `count() > 0`, so every Catalog change after first launch (D.5's 8 real stations, D.6's function/provenance backfills, D.7's TROY migration) was silently swallowed for any existing user database. The test suite was green because every panel / registry test mocked the JPA-backed read path back to `Catalog.all()`-derived data — the "test the running-app shape, not the mock shape" gap.
- **D.7 incomplete wiring**: the Megastructure subtype shipped with entity + mapper + V11 schema + editor dialog, but no `MegastructureRepository` / `*DesignerService` / `*CatalogSeeder`, no panel-side `loadFromRegistry()` call to `MEGASTRUCTURE`, no `kindFilter` / `onNew` / `onEdit` entries, and no production caller for `MegastructureEditorDialog`. The dialog was reachable only from tests.

D.8 fixes both with a sync-by-id contract across all four `SpaceAsset` subtypes, closes the Megastructure persistence pipeline with the missing service + repository + seeder, wires the panel touchpoints + editor save path, and introduces a `@DataJpaTest`-shaped integration test that exercises the running-app code path against a real H2 + real Flyway + real services — the "no service-level mocking" binding rule that future phases inherit.

1. ✅ **Step 1** — Verification report against the design doc's §8 list. Zero divergences across the eight assumptions (seeder counts, existing `"real-station-*"` slugs, the five `UUID.randomUUID()` constants, registry + panel constructor signatures, properties bundle keys, test fixture id-string usage, Flyway high-water mark). Three useful informational observations flagged for later steps. Read-only.
2. ✅ **Step 1.5** — Stable id assignment (preamble). 13 string-literal substitutions in `Catalog.java`: 5 `UUID.randomUUID().toString()` → `"catalog-<slug>"` (TROY, SAPL, SHEVA_GUN, two Posleen ships) + 8 `"real-station-*"` → `"catalog-*"` (D.5's real stations). Unused `import java.util.UUID;` removed. Zero test changes — verification Item 7's "no hardcoded id strings" prediction was empirically confirmed. Suite stable at 3,587.
3. ✅ **Step 2** — V12 cleanup + rename migration. `V12__catalog_id_stabilization.sql` ships three logical blocks: three `DELETE` statements with the belt-and-braces predicate (`id NOT LIKE 'catalog-%' AND id NOT LIKE 'real-station-%' AND name = 'X'`) removing pre-D.8 legacy random-UUID rows, and 8 `UPDATE` statements renaming the D.5 `"real-station-*"` ids to `"catalog-*"`. Schema-stable (zero DDL), idempotent (re-runs match nothing), forward-only. FlywayBaselineSmokeTest stays green. Suite stable at 3,587.
4. ✅ **Step 3** — Megastructure persistence pipeline. `MegastructureRepository extends JpaRepository<MegastructureEntity, String>` with six finders mirroring `StationRepository`. `MegastructureDesignerService` ships with the new `syncCatalogEntries()` from day one (no `seedFromCatalogIfEmpty` predecessor to rename) — per-entry `existsById` short-circuit + insert. `MegastructureCatalogSeeder` `@EventListener(ApplicationReadyEvent.class)` wires it. 12 unit tests with `@MockitoExtension` cover findAll / save / deleteById round-trips + sync contract (empty / idempotent / partial-fill). Suite at 3,599.
5. ✅ **Step 4** — Sync-by-id rename + reimplementation across the three pre-existing services. `StationDesignerService` / `WeaponInstallationDesignerService` / `TransportNodeService`'s `seedFromCatalogIfEmpty()` → `syncCatalogEntries()`; the `count() > 0` short-circuit is replaced with the per-entry `existsById` predicate matching Step 3's Megastructure implementation. Both `StationCatalogSeeder` and `WeaponInstallationCatalogSeeder` switched to the new name + log line. Five test files updated (three service tests rewriting `seedWhen*` → `sync*`; two seeder tests renaming method references). `StationDesignerServiceTest` gains a new `syncInsertsOnlyMissingEntries` test that exercises the partial-fill scenario the seed-on-empty contract couldn't express. Suite at 3,600. The grep verification confirmed exactly one intentional residual hit for `seedFromCatalogIfEmpty` — a javadoc cross-reference explaining what was replaced.
6. ✅ **Step 5** — `TransportNodeCatalogSeeder` created from scratch (closes the Phase B-era asymmetry where `TransportNodeService.seedFromCatalogIfEmpty` was defined but unwired). `@Component` + `@EventListener(ApplicationReadyEvent.class)` mirroring the three sibling seeders. 2 unit tests mirroring `StationCatalogSeederTest`. All four subtypes now have full pipeline parity: Repository + DesignerService + CatalogSeeder + tests. Suite at 3,602.
7. ✅ **Step 6** — UI wiring. `DefaultConstructRegistry` constructor 5 → 6 args (adds `MegastructureDesignerService`); `loadMegastructures()` swapped from D.7 Step 7's Catalog-direct filter to `megastructureDesignerService.findAllAsAssets()`. `InstallationDesignerPanel` constructor 4 → 5 args. Five panel touchpoints wired: `loadFromRegistry` (+ MEGASTRUCTURE bucket), `kindFilter` (+ 5th entry), `onNew` picker (+ 4th label), `onEdit` switch (+ Megastructure case), `onDelete` switch (+ Megastructure case), new `openMegastructureEditor` + `persistMegastructure` methods (mirror Station pattern). `rebuildSubtypeFilter` switch (+ `KIND_MEGASTRUCTURE` case → `MegastructureArchetype.values()`). `ConstructRow` extended in five switch arms (`getKind` / `getDesignation` / `getSubtype` / `getOperationalState` / `getCategory`) to handle Megastructure — the row was falling through to default branches pre-Step-6, surfacing as a real bug caught by the test-first discipline. `selectedKind()` extended (also caught by a test — the kindFilter narrowing didn't work pre-Step-6 because `selectedKind` fell through to ALL for the Megastructure label). `DesignMenuController` and `construct.properties` updated. Two test fixtures updated for the constructor changes. 5 new panel tests covering Megastructure UI pathway. **MegastructureEditorDialog is now reachable from production code** (was dead at D.7 close-out). Suite at 3,607.
8. ✅ **Step 7** — `CatalogSyncIntegrationTest` ships the 11-method test class per the design doc's §6.2/§6.3 spec. `@DataJpaTest` boot harness with `@AutoConfigureTestDatabase(replace = ANY)`, `spring.flyway.enabled=true` + `spring.jpa.hibernate.ddl-auto=validate`, `@Import` pulling in the four real `*DesignerService` classes + four mappers + the real `DefaultConstructRegistry`. **Zero service-level mocking** per §6.3's binding rule — only repository direct access for setup-convenience pre-seeds + `JdbcTemplate` for in-test V12 simulation. Coverage: S1 (4 seed-from-empty tests, one per subtype) + S2 (the day-one regression catch test, explicitly named `upgradePathFromLegacyTroyResolvesToCatalogTroy` in code) + S3 (2 multi-launch idempotency tests) + S4 (2 user-preservation tests covering user-edited catalog rows and user-created non-catalog rows) + 2 panel load-path tests verifying `registry.assetsByKind(MEGASTRUCTURE)` returns Troy at `catalog-troy` and that the four-bucket aggregate returns the full catalog. Suite at 3,618.

### Phase E — Route-finder integration (jump-gate fast paths)
1. `JumpGateNetworkService` reads `TransportNode`s where `NodeType ∈ {RING_GATE, JUMP_POINT, WORMHOLE_MOUTH, PORTAL}` and `connectedNodeIds` is non-empty.
2. Add gate-traversal edges to the route graph weighted by `traversalTimeTicks` (or instantaneous when `instantaneousTransit`).
3. Route display labels gate segments distinct from warp transits.

### Phase F — Polish + visualisation
1. Jump gates render in the interstellar view at `(positionX, positionY, positionZ)` with a distinct glyph.
2. Linked gates show their partner connection as a faint line in the 3D scene.
3. Optional: shared `ConstructDesignerPanel<C>` base class extracted from the two panels — only if the duplication is visibly hurting.

---

## 6. Decisions pinned in Phase A0

Two questions from earlier drafts have been resolved before v2 Phase A begins. They are recorded here rather than in §6.1's open list so the implementing agent does not re-decide them.

### Q4 (resolved): `Catalog` remains as canonical seed data

The `com.terranrepublic.assets.Catalog` class — which today holds in-memory constants for `TROY`, `SAPL`, `SHEVA_GUN`, `POSLEEN_COMMAND_DODECAHEDRON`, and `POSLEEN_BATTLE_DODECAHEDRON` — stays in place. v2 Phase A's seed-on-empty startup step for the new `StationDesign` and `WeaponInstallation` tables reads from `Catalog.all()`. The constants are not deleted; the class is not relocated.

Rationale: `Catalog` has a documented dual role (Inventory §1.9 cross-references it as the asset hierarchy's canonical seed *and* as the test fixture for asset / economy / sim tests). Deleting the constants would force a fixture-builder rewrite across three test layers with no functional gain. The reconciliation explicitly chose data-duplication-via-known-source-of-truth over data-drift-via-two-independent-seeds.

The class-level javadoc on `Catalog.java` records this decision so future maintainers don't accidentally migrate it.

### Q5 (resolved + implemented): `SpaceshipDesignMapper` round-trip-loss bug

Fixed in Phase A0 commit (see V6 migration). `concealed` and `operationalState` now round-trip correctly through `SpaceshipDesignMapper` and persist as two new columns on the `SPACESHIP_DESIGN` table. v2 Phase A's new entities (StationEntity, WeaponInstallationEntity, TransportNodeEntity) inherit the cleaned-up pattern, not the bug.

---

## 6.1 Open questions for the product owner

Three remaining. Validated against the inventory; questions whose answers are already in the codebase are dropped from the list.

| # | Question | Default if no answer |
|---|---|---|
| Q1 | Add `MINING_STATION` to the existing `StationType` enum, or model asteroid mining via the existing `OUTPOST` / `DEPOT` values? | Add `MINING_STATION` — it's a recognisable distinct concept and the enum has room. |
| Q2 | Name confirmation: "Installations Designer" for the non-spacecraft panel? Alternatives: "Stations & Megastructures," "Megaconstructs Designer." | Go with "Installations Designer." |
| Q3 | Do we need a `ConduitEntity` in Phase B, or is conduit editing deferable to Phase E or later? | Defer. Nothing in Phase A–D depends on conduits being editable. |

**Not in the question list (decided by existing types):**

- ~~Mobility taxonomy~~ — `assets.Mobility{FIXED, STATIONKEEPING, MANEUVERABLE}` already exists. Reuse.
- ~~`category` discriminator shape~~ — `AssetKind` + `InfrastructureKind` already exist. Reuse.
- ~~`ConstructIdentity` shared fields~~ — `Cataloged` already exists. Reuse.

---

## 7. Done definition

This feature is "complete" when:

- `StationDesign`, `WeaponInstallation`, and `TransportNode` are persisted via JPA in the existing flat-entity pattern.
- The Installations Designer panel exists and shows all three persisted non-spacecraft Construct kinds plus any `SpaceshipDesign` `kind() != SHIP` (none exist today; the panel just won't show ships).
- Jump-gate-paired routing is offered as an alternative to warp transit when total travel time is shorter via gates.
- Existing tests still pass; new tests cover each new mapper round-trip and the `JumpGateNetworkService` edge-construction.
- No new sealed hierarchy was introduced. No `payload_json` whole-entity CLOB was introduced.

---

## 8. What v1 got wrong

For honesty and so a future reader can recognise the failure mode:

1. **It didn't survey before designing.** The v1 author saw `SpaceshipDesign` lived under `com.terranrepublic.assets` but didn't trace `SpaceAsset` (the sealed interface above it), `Cataloged` (the identity seam already shared with infrastructure), `StationDesign`, `WeaponInstallation`, `TransportNode`, `NodeType`, or the `EconomyRegistry` FK dependencies on `SpaceAsset`. Every overlap row in §1 above was either already-covered or rename-only, but the v1 plan framed all of them as new types.
2. **It introduced a parallel sealed hierarchy when an equivalent one already existed.** Exactly the fragmentation failure mode the 2026 codebase-review remediation (Issues 13, 17) was prophylaxis against. The reviewer caught it because the reviewer had read the existing packages.
3. **It proposed a novel persistence shape (`payload_json` whole-entity CLOB) without checking against the Issue 46 / `LazyInitializationException` constraint** that the recent Phase 7.8 work established as the load-bearing safety property for any LOB-based persistence design.
4. **It mis-framed `SpaceshipEntity` as a candidate "third duplication"** when in fact `SpaceshipEntity` is the existing immutable-domain / mutable-JPA seam for `SpaceshipDesign`, *and v1 was itself proposing the third*.
5. **It re-asked decisions already made.** `Mobility` granularity was an open question in v1, but `assets.Mobility{FIXED, STATIONKEEPING, MANEUVERABLE}` already exists and has shipped behaviour. Same for the `category` discriminator.

The takeaway, for any future feature plan: **the inventory note exists because designs that skip the survey produce parallel hierarchies. Make the survey artefact-mandatory before the design artefact.**
