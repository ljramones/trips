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

Flyway migrations:
- **V6** — create `STATION_DESIGN` table (matches `StationEntity` fields)
- **V7** — create `WEAPON_INSTALLATION` table
- **V8** — create `TRANSPORT_NODE` table

Each migration is additive. No data drops. No changes to `SPACESHIP_DESIGN` or any other existing table.

---

## 5. Phased rollout

Each phase is a self-contained commit set with a full regression run.

### Phase A — `ConstructRegistry` + station persistence
1. Define `ConstructRegistry` interface + `DefaultConstructRegistry` `@Component`.
2. Implement `StationEntity` + `StationDesignMapper` + `StationRepository` + `StationDesignerService`.
3. Flyway V6 migration. Seed-on-empty from `Catalog.TROY` at startup.
4. Smoke test: `StationDesignerService.findAll()` returns Troy after first launch.

### Phase B — weapon installation + transport node persistence
1. `WeaponInstallationEntity` / mapper / repository / service. Flyway V7. Seed `SAPL` + `SHEVA_GUN`.
2. `TransportNodeEntity` / mapper / repository / service. Flyway V8. No seed (no canonical gates yet).

### Phase C — Installations Designer panel (read-only first)
1. New `InstallationDesignerPanel` showing all non-SHIP `Cataloged` via `ConstructRegistry`.
2. Add `Design → Installations Designer…` menu entry (after product-owner naming confirmation).
3. No edit functionality yet; just list + detail pane.
4. Reuse the tabbed layout from `SpaceshipDesignerPanel` (universe tabs, two-row filter/action layout, multi-row FlowPane of universe ToggleButtons).

### Phase D — Edit dialogs for the three non-spacecraft subtypes
1. `StationEditorDialog`, `WeaponInstallationEditorDialog`, `TransportNodeEditorDialog`.
2. Wire "New…" subtype picker in the Installations Designer.
3. Edit + delete actions wired to the services.

### Phase E — Route-finder integration (jump-gate fast paths)
1. `JumpGateNetworkService` reads `TransportNode`s where `NodeType ∈ {RING_GATE, JUMP_POINT, WORMHOLE_MOUTH, PORTAL}` and `connectedNodeIds` is non-empty.
2. Add gate-traversal edges to the route graph weighted by `traversalTimeTicks` (or instantaneous when `instantaneousTransit`).
3. Route display labels gate segments distinct from warp transits.

### Phase F — Polish + visualisation
1. Jump gates render in the interstellar view at `(positionX, positionY, positionZ)` with a distinct glyph.
2. Linked gates show their partner connection as a faint line in the 3D scene.
3. Optional: shared `ConstructDesignerPanel<C>` base class extracted from the two panels — only if the duplication is visibly hurting.

---

## 6. Open questions for the product owner

Validated against the inventory; questions whose answers are already in the codebase are dropped.

| # | Question | Default if no answer |
|---|---|---|
| Q1 | Add `MINING_STATION` to the existing `StationType` enum, or model asteroid mining via the existing `OUTPOST` / `DEPOT` values? | Add `MINING_STATION` — it's a recognisable distinct concept and the enum has room. |
| Q2 | Name confirmation: "Installations Designer" for the non-spacecraft panel? Alternatives: "Stations & Megastructures," "Megaconstructs Designer." | Go with "Installations Designer." |
| Q3 | Do we need a `ConduitEntity` in Phase B, or is conduit editing deferable to Phase E or later? | Defer. Nothing in Phase A–D depends on conduits being editable. |
| Q4 | Should the `Catalog`'s `POSLEEN_*` spaceships be migrated into the persisted `SpaceshipEntity` table on startup (like the spaceship modeller's template library) or remain in-memory constants? | Migrate. Single canonical source. |
| Q5 | The mapper drops `concealed` and `operationalState` on round-trip for `SpaceshipDesign` (Inventory §4.4). Fix during Phase A as part of the cleanup, or leave for a separate ticket? | Fix during Phase A — small, isolated, and the new entities should not inherit the same bug. |

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
