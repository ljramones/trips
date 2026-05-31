# Phase F.1 — Worldbuilding Universes Foundation

**Status**: design, pre-implementation
**Date**: 2026-05-31
**Audience**: implementers + reviewers of TRIPS Phase F.1
**Parent**: [worldbuilding-platform-requirements.md](worldbuilding-platform-requirements.md)
**Predecessors**: Phase E.1 (in-system feature foundation), Phase D.6 (catalog provenance), Phase D.8 (catalog sync-by-id)
**Successors**: F.2 (Aliases), F.3 (Factions structure), F.4 (Eras), F.5 (Visual rules), F.6 (Population rules), F.7 (Tech constraints), F.8 (Events), F.9 (Economics), F.10 (Threats)

---

## §0 — Scope and orientation

### What F.1 delivers

F.1 introduces the **Universe** entity as a first-class persisted scope and threads it through the existing catalog tables. After F.1 ships, every catalog entry has a clear answer to "does this belong to a fictional universe, and if so which one?" — and the UI respects that scoping by filtering visibility based on which universes the user has activated.

The four concrete deliverables (per Larry's ratification):

1. **Universe entity** in the `com.terranrepublic.assets` package, mirroring the GateNetwork pipeline pattern (record + JPA entity + mapper + repository + lifecycle enum + designer service + seeder).
2. **`universe_id` nullable FK column** on the six catalog tables: `station_design`, `weapon_installation`, `megastructure`, `spaceship_design`, `transport_node`, `gate_network`. NULL = canonical/real; non-NULL = scoped to the referenced universe.
3. **Activation mechanism**: per-universe `active` boolean + `UniverseActivationChangedEvent` + `UniverseFilteringService` that catalog designer panels and renderers consult before showing universe-scoped content.
4. **Worldbuilding menu integration**: a "Universes" submenu listing available universes with activation toggles. Status bar indicator showing "Real only" or "Real + N universe(s) active".

Plus the V15 Flyway migration that:
- Creates the `universe` table
- Adds the `universe_id` column to each catalog table
- Inserts a row per actual SF universe identified in the existing catalog audit (§3.2)
- UPDATEs existing catalog rows to point their `universe_id` at the appropriate universe row

### What F.1 explicitly does NOT deliver

The §7.2–§7.14 content categories from the requirements doc are deferred:

| Category | Phase | Notes |
|---|---|---|
| §7.2 Aliases | F.2 | Star alias entity + alias label overlay in StarPlotManager |
| §7.6 Factions full structure | F.3 | Faction entity refactor; today `faction` is a free-text String on catalog entries |
| §7.7 Eras full structure | F.4 | Era entity per-universe; era-based visibility filtering |
| §7.8 Visual presentation rules | F.5 | Per-universe color/icon overrides |
| §7.10 Population rules | F.6 | When the universe definition spans `SolarSystemFeature` placements; F.1 doesn't touch SolarSystemFeature scoping |
| §7.11 Tech constraints | F.7 | DriveType per-universe filtering in editors |
| §7.12 Events/Timeline | F.8 | Event entity + timeline view |
| §7.13 Economics/Resources | F.9 | Resource tagging on places |
| §7.14 Threats/Anomalies | F.10 | Threat overlay rendering |

Also deferred from F.1:
- **Create/Import/Export universe UI** (R8.6–R8.12). The Worldbuilding menu in F.1 lists existing universes (the ones the V15 migration created from existing catalog audit) with activation toggles. Authoring new universes through the UI lands in a later F.x (sequencing TBD; could be bundled with F.2).
- **Catalog editor "Universe" field** (R8.13). Editing existing entries' universe affiliation is deferred. The V15 migration is the only way to set `universe_id` in F.1.
- **SolarSystemFeature scoping** (per-universe placements). E.1's `SolarSystemFeature` table has no `universe_id` column in F.1 — feature placements remain universe-agnostic. R7.10 work in F.6 will add that.
- **Real-data lock** (R1.11). F.1 doesn't enforce that the user can't edit canonical real entries; it relies on `universe_id = NULL` as the marker and the catalog audit tests as the integrity check, but the editor UI doesn't yet gate writes by universe affiliation.

### What F.1 is the foundation for

Every later F.x phase consumes the Universe entity. Once F.1 ships:
- F.2 adds `Alias(universe_id, real_place_id, alias_text)` referencing the Universe rows F.1 creates.
- F.3 adds `Faction(universe_id, name, parent_faction_id, ...)` likewise.
- The activation mechanism F.1 ships becomes the central filter the renderer, catalog browsers, and editors all consult.

The "are you scoped to a universe?" question is asked once at the schema level (the FK column) and once at the runtime level (the activation service). Every F.x phase adds new schema/runtime concerns that funnel through these two chokepoints.

---

## §1 — Architectural decisions

Larry asked me to settle two architectural questions before drafting the design. Both have answers grounded in the existing codebase's state, including a significant discovery while auditing.

### §1.1 — Q1: CatalogProvenance vs Universe entity relationship

**Decision: Option (b).** Keep `CatalogProvenance.sourceUniverse` as a free-text String field; add `Universe` as a separate JPA entity; `universe_id` FK on catalog tables is the authoritative scope.

**Reasoning:**

The audit of existing `CatalogProvenance.sourceUniverse` values turned up **59 distinct strings** in the current catalog. The breakdown:

- **~15 actual SF universes**: "Star Trek", "Star Wars", "The Expanse", "Mass Effect", "Battlestar Galactica", "Honor Harrington", "Foundation", "Firefly", "The Martian", "Project Hail Mary", "The Hitchhiker's Guide to the Galaxy", "Caine Riordan"/"Terran Republic", "Lost Soldiers", "Real / Proposed" (real data), and a few others.
- **~25 faction names**: "Hkh'Rkh", "Slaasriithi", "Arat Kur Wholenest", "Dornaani Collective", "Ktoran Dominion" (all Caine Riordan factions); "Colonial Fleet" (BSG); "Rebel Alliance", "Galactic Empire", "Galactic Civil War" (Star Wars); "Starfleet" (Star Trek); "Royal Manticoran Navy" (Honor Harrington); "MCRN", "OPA", "Free Navy", "Tycho / OPA", "Pur'n'Kleen Water Co.", "Cerberus / Systems Alliance" (Expanse + Mass Effect); etc.
- **~15 era names**: "First Contact (2105)", "First Contact era", "Imperial era", "Pre-Epstein era", "Late Seldon era", "Cleon dynasty", "Post-Contact", "Interregnum", "The Mule", "Lost Soldiers Era", "Second Cylon War", "Early Foundation era", etc.
- **~4 real-world entities**: "NASA", "Near future", "United States", "Earth".

The current String field is doing the work of three concepts: universe, faction, era. Repurposing it as a universe identifier (option c) would lose the faction/era information embedded in it. Replacing it with an entity reference (option a) would force a one-shot disambiguation that's actually a multi-phase audit task (F.3 surfaces Faction structure; F.4 surfaces Era structure).

Option (b) cleanly separates concerns:
- **`Universe` entity** answers the question "which fictional universe scopes this entry?" — authoritative for activation/filtering decisions.
- **`CatalogProvenance.sourceUniverse` String** stays as a free-text descriptor surfaced in provenance tooltips and metadata views — informational, not authoritative.
- Entries whose `sourceUniverse` String is a faction or era (e.g., "Hkh'Rkh") get `universe_id` pointing at the parent universe (Caine Riordan) while the String stays "Hkh'Rkh" — and F.3 later extracts the faction structure as a proper Faction entity.

This is forward-compatible: when F.3 ships, the `CatalogProvenance.sourceUniverse` String can be parsed/migrated into `Faction` references where appropriate; until then it stays as the existing free-text descriptor.

**Pre-emptive scope estimate for F.3** (surfaced by the Step 1 audit): the `CatalogProvenance.sourceUniverse` String has wider write/read reach than this design doc originally captured — ~10 files (writers in `SpaceshipEditorDialog`, `SpaceshipDesignDto`, 4 mappers; readers in `SpaceshipRow`, `SpaceshipEntity`, plus the 5 originally-listed paths). Deprecating the String (Option a, rejected here) would touch all ~10 files; this is load-bearing context for F.3's Faction-extraction work when it eventually wants to deprecate or refactor the String field.

### §1.2 — Q2: Example universe naming

**Decision: Multi-universe seed, not a single example.** F.1's V15 migration creates one Universe row per distinct SF universe identified in the catalog audit (~15 rows), not just "Legacy of the Aldenata". Two universes get the formal naming the requirements doc suggested:

- **"Legacy of the Aldenata"** (John Ringo) — Troy, SAPL, SheVa Gun, Posleen ships, fictional Posleen drives.
- **"Caine Riordan"** (Charles Gannon) — Hkh'Rkh, Slaasriithi, Arat Kur, Dornaani, Ktoran, Lost Soldiers ships; Grtul Gates; GRTUL_GATE, GALACTIC_HYPER, KTORAN_ADVANCED, HKHRKH_THRUST drives.

The remaining ~13 universes are seeded from existing catalog entries that reference them ("Star Trek", "Star Wars", "The Expanse", etc.). Each gets a Universe row in V15 so the FK references resolve, but they ship with **no default active state** and **no curated content** beyond what the audit found.

**Reasoning:**

The requirements doc's R2.6 says "at least one example universe demonstrating the platform." It doesn't say "exactly one." Given that the catalog already contains entries from many SF universes, hiding 14 of them and only surfacing Legacy of the Aldenata would create more architectural debt than it removes: the other entries would still exist but with no universe scope, masking the underlying problem.

Better to:
- Acknowledge the actual SF universes already in the catalog (each gets a Universe row).
- Use Legacy of the Aldenata + Caine Riordan as the **first-class examples** with full population and curated metadata.
- Treat the other ~13 as **migrated-but-thin** — present, valid, activatable, but with minimal authored description ("Auto-seeded from existing catalog entries; expand metadata as desired").
- The default-active set in F.1 is **empty** (R1.8: real-only by default).

This also exposes a real worldbuilding question to Larry early: do you actually want all these universes shipped? Or do you want to scrub the catalog and remove entries from universes you don't intend to support? That's a curation question, not an architectural one, and F.1 makes it visible by treating each universe as a discrete entity.

**Naming convention** for the Universe rows: use the universe's canonical title from the existing `sourceUniverse` String values, normalised. The id slugs follow the established `catalog-<slug>` convention (e.g., `catalog-universe-legacy-of-the-aldenata`, `catalog-universe-caine-riordan`, `catalog-universe-star-trek`).

---

## §2 — Glossary

Terms specific to F.1. Inherits the worldbuilding-platform-requirements doc's §2 glossary.

**Activation state**: A per-universe boolean (`active`) stored on the `Universe` JPA entity. Persists across application restarts (R5.2). Updates fire a `UniverseActivationChangedEvent`.

**Cataloged scope**: A catalog entry's universe affiliation. Encoded as `universe_id` FK (NULL = canonical/real; non-NULL = scoped to the referenced universe). Authoritative for visibility filtering.

**UniverseFilteringService**: F.1's central runtime service that answers "should this catalog entry be visible right now?" given the entry's `universe_id` and the current activation state. Used by designer panels, the StarPlotManager (for future universe-scoped overlays in F.2), and the catalog browsers.

**Activation set**: The set of currently-active universes. May be empty (real-only mode), single-element (single-universe mode), or multi-element (multi-universe mode).

**Migrated-but-thin universe**: A universe whose V15 row was auto-seeded from the catalog audit but has minimal authored metadata (description, source/author, version). Distinguished from "first-class example" universes (Legacy of the Aldenata, Caine Riordan) which have curated metadata.

**Real-data universe**: A non-concept. Real data is the absence of universe scoping (`universe_id = NULL`). There is no Universe row named "Real" in F.1.

---

## §3 — Pre-design audit findings

F.1 is the first phase that has to reckon with the catalog's existing fiction-canon content as data needing migration, not just code needing refactoring. This section captures the audit findings that shape the design.

### §3.1 — CatalogProvenance reach

`CatalogProvenance.sourceUniverse` is read from:
- `GateNetwork.faction()` returns `provenance.sourceUniverse()` (per the F.1 predecessor Phase E.1 Divergence G resolution).
- `Megastructure.faction()` returns `provenance.sourceUniverse()`.
- `StationDesign` getters expose `provenance.sourceUniverse()`.
- `SpaceshipDesign.sourceUniverse()` is a top-level accessor on the record.
- `SpaceshipDesignerPanel` uses `d.sourceUniverse()` for the universe filter dropdown + the "meaningful universe" predicate at line 475 + the universe filter match at line 487 + the full-text search at line 504.

Implication: F.1 must not break these read paths. The String stays. F.1 adds a parallel `universe_id` accessor without disturbing `sourceUniverse()`.

### §3.2 — The 59 distinct sourceUniverse values

The catalog audit produced 59 distinct strings. Categorisation (best-effort; some entries are ambiguous):

**Universes (~15)** — these become Universe rows in V15:
| sourceUniverse value | Proposed Universe row | Slug |
|---|---|---|
| "Real / Proposed" + similar | (no Universe; `universe_id = NULL`) | — |
| "Battlestar Galactica" | Battlestar Galactica | `catalog-universe-battlestar-galactica` |
| "Caine Riordan", "Terran Republic", "IRIS / Terran Republic" | Caine Riordan | `catalog-universe-caine-riordan` |
| "Firefly" | Firefly | `catalog-universe-firefly` |
| "Foundation" | Foundation | `catalog-universe-foundation` |
| "Honor Harrington" | Honor Harrington | `catalog-universe-honor-harrington` |
| "Mass Effect" | Mass Effect | `catalog-universe-mass-effect` |
| "Project Hail Mary" | Project Hail Mary | `catalog-universe-project-hail-mary` |
| "Star Trek" | Star Trek | `catalog-universe-star-trek` |
| "Star Wars" | Star Wars | `catalog-universe-star-wars` |
| "The Expanse" | The Expanse | `catalog-universe-the-expanse` |
| "The Hitchhiker's Guide to the Galaxy" | The Hitchhiker's Guide to the Galaxy | `catalog-universe-the-hitchhikers-guide-to-the-galaxy` |
| "The Martian" | The Martian | `catalog-universe-the-martian` |
| "Lost Soldiers", "Lost Soldiers Era" | (subsidiary of Caine Riordan; map to Caine Riordan in F.1) | — |
| (Posleen War; identified by faction values) | Legacy of the Aldenata | `catalog-universe-legacy-of-the-aldenata` |

15 first-class Universe rows. Two get curated metadata in F.1 (Legacy of the Aldenata + Caine Riordan); the other ~13 ship with auto-generated metadata.

**Faction values (~25)** — these stay as `CatalogProvenance.sourceUniverse` Strings; F.3 extracts them later. The migration maps them to a Universe via a deterministic lookup:
| Faction value | Maps to Universe |
|---|---|
| "Hkh'Rkh", "Slaasriithi", "Arat Kur Wholenest", "Dornaani Collective", "Ktoran Dominion", "Roaches", "Gok", "SpinDog", "RockHound" | Caine Riordan |
| "Colonial Fleet" | Battlestar Galactica |
| "Rebel Alliance", "Galactic Empire", "Galactic Civil War" | Star Wars |
| "Starfleet" | Star Trek |
| "Royal Manticoran Navy" | Honor Harrington |
| "MCRN", "OPA", "Free Navy", "Tycho / OPA", "Pur'n'Kleen Water Co." | The Expanse |
| "Cerberus / Systems Alliance" | Mass Effect |
| "Foundation Traders" | Foundation |
| "UN Navy", "United Earth (UEG)", "United Earth" | (ambiguous: real, Caine Riordan, or other; map to NULL conservatively) |

**Era values (~15)** — these stay as Strings; F.4 extracts. Migration maps to Universe via parent-series lookup:
| Era value | Maps to Universe |
|---|---|
| "First Contact (2105)", "First Contact era", "Post-Contact", "Pre-Epstein era" | The Expanse |
| "Imperial era" | Star Wars (or ambiguous — multiple have empires) |
| "Late Seldon era", "Cleon dynasty", "Interregnum", "The Mule", "Early Foundation era" | Foundation |
| "Second Cylon War" | Battlestar Galactica |
| "Lost Soldiers Era" | Caine Riordan |

**Real-world entities (~4)**: "NASA", "Near future", "United States", "Earth" — map to NULL `universe_id` (these are real entries).

**Other** — "Independent" (generic; map to NULL), "Source universe or setting for this design." (this is documentation accidentally captured; map to NULL), "Galactic Government" (Hitchhiker's Guide universe-specific governance term; map to The Hitchhiker's Guide to the Galaxy).

### §3.3 — The 6 catalog tables

`universe_id` FK lands on these tables. Status as of F.1 start:

| Table | Source | Status | Notes |
|---|---|---|---|
| `station_design` | D.5 (real) + D.6 (provenance) | Active | 8 real stations + fictional |
| `weapon_installation` | D.5 + D.6 | Active | Real + Caine Riordan + others |
| `megastructure` | D.7 | Active | Troy (Legacy of Aldenata) + Real (Dyson, etc.) |
| `spaceship_design` | Pre-Constructs v2 | Active | Multi-universe |
| `gate_network` | E.1 / V13 | Active | Currently empty; F.2 may populate |
| `transport_node` | V9 (Constructs v2 Phase B) | Active | Currently empty per V9 header — no canonical transport nodes shipped yet; `universe_id` column added anyway for schema uniformity |

### §3.4 — SolarSystemFeature is NOT a catalog table

Important clarification: `SolarSystemFeature` (per E.1 Step 4 schema) represents per-system placements of features, not catalog entries. It's a placement layer that *references* catalog entries via `catalog_reference_id` + `catalog_reference_kind`. F.1 does NOT add `universe_id` to `SolarSystemFeature` — placement visibility derives from the visibility of the catalog entry the feature references (which has `universe_id` after F.1).

Example: a `SolarSystemFeature(featureType=MEGASTRUCTURE, catalog_reference_id="catalog-troy")` is visible iff the Troy megastructure row is visible iff Troy's `universe_id` points at an active universe. The feature itself doesn't need a `universe_id` column.

R7.10 / F.6 may revisit this when population rules become a thing — at that point, *which* features a universe activation places into a system may be a per-universe data definition. F.1 does not deliver that.

---

## §4 — Data model

### §4.1 — Universe (record + entity + lifecycle)

`com.terranrepublic.assets.Universe` — record, immutable, mirrors the GateNetwork pattern:

```java
public record Universe(
        String id,                       // catalog-universe-<slug>
        String name,                     // "Legacy of the Aldenata"
        String description,
        String sourceAuthor,             // "John Ringo"
        String version,                  // "1.0"
        UniverseLifecycle lifecycle,     // AVAILABLE / DEPRECATED
        boolean active                   // activation state (mutable via service)
) implements Cataloged {
    public Universe {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("id required");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name required");
        if (lifecycle == null) lifecycle = UniverseLifecycle.AVAILABLE;
        if (description == null) description = "";
        if (sourceAuthor == null) sourceAuthor = "";
        if (version == null) version = "1.0";
    }

    @Override public String faction() { return sourceAuthor; }
    @Override public boolean concealed() { return false; }
}
```

`UniverseLifecycle` enum:
- `AVAILABLE` — normal state; can be activated
- `DEPRECATED` — marked for removal; should not be activated by new code but old activations preserved

### §4.2 — Universe JPA entity + mapper + repository

Mirrors `GateNetworkEntity` exactly:

- `UniverseEntity` (`@Entity(name = "UNIVERSE")`, no L2 cache, no JSON LOBs)
- `UniverseMapper` (`@Component`, bidirectional)
- `UniverseRepository` extending `JpaRepository<UniverseEntity, String>` with finders:
  - `Optional<UniverseEntity> findByName(String name)`
  - `boolean existsByName(String name)`
  - `List<UniverseEntity> findByLifecycle(UniverseLifecycle lifecycle)`
  - `List<UniverseEntity> findByActive(boolean active)`

`UniverseDesignerService` exposes:
- `List<Universe> findAll()`
- `List<Universe> findAllActive()`
- `Optional<Universe> findById(String id)`
- `Universe activate(String id)` — sets `active = true`, persists, publishes `UniverseActivationChangedEvent`
- `Universe deactivate(String id)` — sets `active = false`, persists, publishes event
- `List<Cataloged> findAllAsCataloged()` (parallels GateNetworkDesignerService for catalog audit symmetry)

### §4.3 — universe_id FK columns

Added to six existing tables via V15:

| Table | Column | Constraint |
|---|---|---|
| `station_design` | `universe_id VARCHAR(64) NULL` | `FK references universe(id) ON DELETE SET NULL` |
| `weapon_installation` | `universe_id VARCHAR(64) NULL` | same |
| `megastructure` | `universe_id VARCHAR(64) NULL` | same |
| `spaceship_design` | `universe_id VARCHAR(64) NULL` | same |
| `gate_network` | `universe_id VARCHAR(64) NULL` | same |
| `transport_node` | `universe_id VARCHAR(64) NULL` | same |

`transport_node` was added in V9 (Constructs v2 Phase B) and exists as a separate table with its own JPA pipeline (entity, mapper, repository, service, seeder). It's currently empty in production (no canonical content seeded yet per V9 header) but the `universe_id` column belongs here for schema uniformity — when canonical transport nodes eventually ship, they'll need universe scoping consistent with the other catalog tables. The Step 1 verification audit surfaced this; the original design draft incorrectly claimed `transport_node` didn't exist as a separate table. SpaceAsset and SpaceInfrastructure both contribute persisted Cataloged subtypes in F.1; future F.x phases inherit this cross-hierarchy treatment as the default.

`ON DELETE SET NULL`: if a Universe row is deleted (e.g., user removes "Children of the Pattern"), the entries pointing at it revert to canonical/real status rather than cascading-deleting (R2.10 says "deleting a universe loses its content" but in F.1 the safer interpretation is "deleting a universe orphans its content as canonical; the user can then re-tag or delete entries explicitly"). F.6 may revisit when cascade-delete semantics get richer.

### §4.4 — Cataloged interface extension

`Cataloged` interface (the common parent of all catalog entry sealed types) gets a new default method:

```java
default Optional<String> universeId() {
    return Optional.empty();
}
```

Each subtype (SpaceshipDesign, StationDesign, WeaponInstallation, Megastructure, GateNetwork, TransportNode) overrides if it has the field. The new Universe record itself does NOT override (universes are not scoped to other universes). Conduit (the second SpaceInfrastructure permits, record-only with no JPA backing) inherits the default `Optional.empty()` automatically.

This becomes the universal visibility-filter input for UniverseFilteringService.

### §4.5 — V15 migration

File: `tripsapplication/src/main/resources/db/migration/V15__universe_table_and_catalog_universe_id.sql`

Structure:
1. `CREATE TABLE universe (...)` with id PK, name, description, source_author, version, lifecycle, active, created_at, modified_at.
2. `CREATE INDEX universe_name_idx ON universe (name);`
3. `CREATE INDEX universe_lifecycle_idx ON universe (lifecycle);`
4. `CREATE INDEX universe_active_idx ON universe (active);`
5. `ALTER TABLE station_design ADD COLUMN universe_id VARCHAR(64) NULL;`
6. (Same for the other 5 tables: weapon_installation, megastructure, spaceship_design, gate_network, transport_node.)
7. Add FK constraints (idempotent via `IF NOT EXISTS` per repo Flyway convention).
8. `INSERT INTO universe (...)` rows — 15 universe rows. Two get curated description/sourceAuthor:
   ```sql
   INSERT INTO universe (id, name, description, source_author, version, lifecycle, active, created_at, modified_at) VALUES
   ('catalog-universe-legacy-of-the-aldenata', 'Legacy of the Aldenata',
    'John Ringo''s Posleen War setting. Includes Troy, SAPL elements, SheVa Gun, Posleen ship designs, and the fictional Posleen interstellar drive.',
    'John Ringo', '1.0', 'AVAILABLE', FALSE, NOW(), NOW());
   INSERT INTO universe (...) VALUES
   ('catalog-universe-caine-riordan', 'Caine Riordan',
    'Charles Gannon''s Terran Republic / Caine Riordan setting. Includes Hkh''Rkh, Slaasriithi, Arat Kur, Dornaani, Ktoran factions; Grtul Gates; the GALACTIC_HYPER, KTORAN_ADVANCED, HKHRKH_THRUST, and GRTUL_GATE drive types.',
    'Charles Gannon', '1.0', 'AVAILABLE', FALSE, NOW(), NOW());
   ```
   The other 13 universes get a generic description: `'Auto-seeded from existing catalog entries. Expand metadata via Universe editor (Phase F.x).'`
9. `UPDATE station_design SET universe_id = ... WHERE provenance_source_universe IN (...)` — one UPDATE per universe, scoped by the §3.2 mapping table.
10. (Same UPDATEs for `weapon_installation`, `megastructure`, `spaceship_design`, `gate_network`. The `transport_node` table is currently empty, so no UPDATE statements needed for it — just the column addition. Additionally, `transport_node`'s provenance column naming differs: V9 uses a bare `faction` String column rather than the `provenance_source_*` prefix the other tables use. When canonical transport nodes are eventually seeded with universe scoping, the seeder writes `universe_id` directly rather than going through a sourceUniverse-string lookup.)
11. Belt-and-braces verification queries (commented; not executed): "After this migration, every fiction-canon entry should have a non-NULL universe_id matching the §3.2 audit."

The UPDATE statements are the most error-prone part. Two safety measures:

- **Pre-migration audit**: Step 1 of F.1 (verification) produces the actual current set of `sourceUniverse` strings (since the catalog grows between this design doc landing and the migration shipping). The §3.2 table here is a snapshot; the implementation reads from a fresh audit.
- **Idempotency**: Each UPDATE has a `WHERE universe_id IS NULL AND provenance_source_universe = ...` predicate so re-running the migration (or a partial failure + resume) doesn't double-tag.

### §4.6 — What V15 does NOT migrate

- **`CatalogProvenance.sourceUniverse` String values are not touched.** They remain the documentation/free-text descriptor. The new `universe_id` column is purely additive.
- **Real entries' `universe_id` stays NULL.** No UPDATE touches them. Real-data invariant preserved.
- **Entries with ambiguous `sourceUniverse` stay NULL.** "UN Navy", "Independent", documentation accidents — these don't get force-fitted into a universe. They render as canonical/real with their existing free-text descriptor still showing as provenance metadata.

---

## §5 — Activation mechanism

### §5.1 — Where activation state lives

**Decision: as a `boolean active` field on the `Universe` JPA entity itself.** Persists natively via JPA; no separate table; no separate preferences store.

Alternatives considered:
- A `universe_activation_state` table with `user_id + universe_id`. Rejected: TRIPS is single-user, so the user_id dimension is wasted complexity.
- `SystemPreferencesService` key-value (e.g., `universe.active.<id> = true`). Rejected: bypasses the JPA layer that already manages Universe rows; creates state-sync risk.
- A JSON field on Universe storing `{ active: bool, lastActivated: timestamp, ... }`. Rejected: premature; the simple boolean covers F.1 needs.

The simple boolean is the minimum that satisfies R5.1, R5.2, R5.3.

### §5.2 — UniverseActivationChangedEvent

```java
package com.teamgannon.trips.worldbuilding;

public record UniverseActivationChangedEvent(Universe universe, boolean nowActive) {}
```

Published by `UniverseDesignerService.activate()` and `.deactivate()` after the JPA write commits. Listeners react by:
- Refreshing catalog browser views (designer panels filter their item lists by active universes).
- Re-rendering the stellar map / system view (if F.1 introduces any universe-scoped overlay; mostly forward-looking for F.2+).
- Updating the status bar indicator.

Threading: Spring delivers events synchronously on the publisher's thread. `UniverseDesignerService` methods are called from the Worldbuilding menu (FX thread by construction), so listeners can stay simple — no `FxThread.runOnFxThread` wrap required in F.1 listeners. The defensive wrap pattern (per E.1 Step 6) becomes mandatory only when off-FX-thread publishers appear; F.1 has none.

### §5.3 — UniverseFilteringService

```java
package com.teamgannon.trips.worldbuilding;

@Service
public class UniverseFilteringService {
    public boolean isVisible(Cataloged entry) {
        Optional<String> universeId = entry.universeId();
        if (universeId.isEmpty()) return true;  // canonical/real always visible (R1.9)
        return universeDesignerService.findById(universeId.get())
                .map(Universe::active)
                .orElse(false);  // unknown universe = not visible
    }

    public List<T extends Cataloged> filter(List<T> entries) {
        return entries.stream().filter(this::isVisible).toList();
    }
}
```

Designer panels call `filter(catalogService.findAll())` instead of using the raw catalog. UniverseFilteringService becomes the universal chokepoint.

Designer panels that get the filter integration in F.1:
- `SpaceshipDesignerPanel` (already has a per-universe dropdown; refactor to consume UniverseFilteringService for the "visible to user" backing list while keeping the existing dropdown as a sub-filter)
- `StationDesignerPanel`, `WeaponInstallationDesignerPanel`, `MegastructureDesignerPanel` (each gets the same treatment)
- `GateNetworkDesignerService` (new in E.1; integrate filter)

Note: the existing `SpaceshipDesignerPanel` universe dropdown is filtering by `CatalogProvenance.sourceUniverse` String values. F.1 keeps that dropdown working but its semantics shift: the dropdown filters within the **active** universes (the universe-scoped filter is applied first; the dropdown narrows further within what's visible). The user perceives this as natural — they only see universes' content they've activated, then can filter further by sourceUniverse if desired.

### §5.4 — Real-data is always visible

`Cataloged.universeId()` returning `Optional.empty()` (i.e., `universe_id IS NULL`) means the entry is real/canonical. The filter short-circuits to `true` in that case — R1.9 satisfied unconditionally.

---

## §6 — UI: Worldbuilding menu Universes submenu

### §6.1 — Menu structure

Existing Worldbuilding menu structure (per the D.6/D.7/D.8 rename + the Step 9 work) becomes:

```
Worldbuilding (_W)
├── Universes...          ← NEW in F.1
├── (separator)
├── Ships...
├── Stations...
├── Weapons...
├── Megastructures...
└── Gate Networks... (E.1)
```

Clicking "Universes..." opens a new dialog (raw FXMLLoader pattern per CLAUDE.md, since it's transient with per-open state):

```
+--------------------------------------------------------+
| Universes                                              |
+--------------------------------------------------------+
| Available Universes:                                   |
|                                                        |
| [Activate?]  Name                          Version     |
| [    X    ]  Battlestar Galactica           1.0        |
| [    X    ]  Caine Riordan                  1.0        |
| [    ✓    ]  Legacy of the Aldenata         1.0        |
| [    X    ]  Star Trek                      1.0        |
| ...                                                    |
|                                                        |
| (Selected universe details panel)                      |
|   Description: John Ringo's Posleen War setting...     |
|   Author: John Ringo                                   |
|   Entries: 1 megastructure, 4 ships, 2 weapons         |
|                                                        |
| [Close]                                                |
+--------------------------------------------------------+
```

Checkbox toggles activation immediately (R8.4 — no confirmation). Closing the dialog doesn't reset anything; activation state is already persisted.

Create / Import / Export buttons are **omitted in F.1** (deferred per §0).

### §6.2 — Status bar indicator

The existing `StatusBarController` gets a new region showing universe activation state:

- `Real only` — when no universes are active (default)
- `Real + N universe(s) active` — when N >= 1 universes are active
- Hovering shows a tooltip listing the active universe names

This is the R5.4 visual indication. Updated on `UniverseActivationChangedEvent`.

### §6.3 — What's NOT in the F.1 UI

- No editor "Universe" field on catalog editors (R8.13 deferred).
- No badge/icon on universe-scoped entries in the catalog browsers (R2.13 / R7.1.3 deferred — could ship as part of F.5 visual rules or earlier as a small polish).
- No active-universe indicator on individual entries beyond their existing `sourceUniverse` String column.
- No hover/click affordance navigating from a fictional entry to its universe metadata (R8.18 deferred).
- No filtering UI in the catalog browsers beyond what already exists (the SpaceshipDesignerPanel dropdown stays; new universe filtering happens silently via UniverseFilteringService at the data-fetch layer).

These deferred UI affordances are mostly cosmetic. F.1 nails the data model + activation mechanism + minimum UI to make the platform work; subsequent F.x polish the UX.

---

## §7 — Acceptance gate: the four §5 invariants

Larry's brief stated: "The four invariants from §5 (R5.5, R5.6, R5.7 + the bonus 'real data is always visible') become the F.1 design's acceptance gate."

Each invariant maps to a verification step that F.1's tests must satisfy.

### §7.1 — R5.5: Universes shall not leak content into each other

**Test**: Activate only Universe A. Verify catalog designer panels show entries with `universe_id = A.id` and entries with `universe_id IS NULL`, but NOT entries with `universe_id = B.id` for any B ≠ A.

**Test fixture**: V15 migration creates Legacy of the Aldenata (with Troy) and Caine Riordan (with Hkh'Rkh Battle Cruiser). Activate only Caine Riordan. Verify Troy is filtered out; Hkh'Rkh Battle Cruiser is visible.

### §7.2 — R5.6: Real data shall not be lost when a universe is active

**Test**: With Universe A active, verify entries with `universe_id IS NULL` (the 8 D.5 real stations, real megastructures, real ships) remain in the catalog browser views.

**Test fixture**: Activate Legacy of the Aldenata. Verify ISS, Tiangong, Mir, Skylab, Salyut 1, Salyut 7, Lunar Gateway, Axiom Station all remain visible in StationDesignerPanel.

### §7.3 — R5.7: Universe-specific aliases shall be applied only when their universe is active

**Deferred to F.2** when Alias entities exist. F.1 has no aliases to verify. The R5.7 invariant is **vacuously satisfied** in F.1 (there are no aliases to leak), but the invariant becomes substantive in F.2.

For F.1, the analogous invariant is: **universe-scoped catalog entries are hidden when their universe is deactivated**. Same shape as R5.5 from the entry's perspective.

### §7.4 — Bonus invariant: real data is always visible

**Test**: Sweep all activation combinations (no universes active, each single universe active, all universes active). Verify that the count of `universe_id IS NULL` entries returned by `UniverseFilteringService.filter(catalogService.findAll())` is **constant** across all sweeps.

This is the integrity check that catches accidentally treating real data as universe-scoped, or accidentally filtering it out when no universe is active.

### §7.5 — Belt-and-braces: CatalogAuditTest extension

The existing `CatalogAuditTest` (Phase D.6) gets new assertions:
- Every Universe row in the V15 migration has a unique id with the `catalog-universe-` prefix.
- Every `universe_id` value present in any catalog table has a matching Universe row.
- The set of `sourceUniverse` String values appearing in entries with `universe_id IS NULL` (entries the migration left un-tagged) does NOT include any value listed as a "Universe (~15)" entry in §3.2 (which would indicate the migration failed to tag a fiction-canon entry).

---

## §8 — Step breakdown

F.1 ships in **9 steps**, parallel to E.1's structure. Each step ends with a test pass; rate-of-progress checkpoints between steps invite Larry's ratification.

| Step | Subject | Net new tests (est.) |
|---|---|---|
| 1 | Verification + audit of existing sourceUniverse data | 0 (read-only) |
| 2 | Universe entity pipeline (record + lifecycle + entity + mapper + repo + service + seeder) | ~30 |
| 3 | `universe_id` FK + Cataloged interface extension + per-subtype `universeId()` override | ~15 |
| 4 | V15 migration | ~10 (FlywayBaselineSmokeTest + audit-extension tests) |
| 5 | Activation mechanism (UniverseActivationChangedEvent + service active/deactivate) | ~10 |
| 6 | UniverseFilteringService + integration into the 5 designer panels | ~20 |
| 7 | Worldbuilding menu Universes submenu UI | ~8 |
| 8 | Status bar indicator + visibility-filtering verification | ~12 |
| 9 | Plan doc rollup + retroactive design doc | 0 |

Total est. ~108 new tests (Step 1 audit added TransportNode to the universeId() override set, +3 tests for the per-subtype extension; final count may run higher if parameterized tests over UniverseLifecycle and audit invariants expand).

Step boundaries match where the user-visible behaviour changes incrementally:
- After Step 2, the entity exists but nothing references it.
- After Step 3, the FK column exists but the migration hasn't populated it (DB has NULLs everywhere except via direct SQL).
- After Step 4, real and universe-scoped entries are distinguishable in the database.
- After Step 5, the activation state can be toggled via the service API but not yet via UI.
- After Step 6, the catalog designer panels are universe-aware (real data + active-universe data visible).
- After Step 7, the user can toggle universes through the Worldbuilding menu.
- After Step 8, the status bar shows the current activation state and tests validate the §7 invariants.

---

## §9 — Test architecture

### §9.1 — Per-step test scaffolding

- **Step 2**: UniverseTest (record shape, defaults, invariants); UniverseLifecycleTest (3 values); UniverseEntityTest, UniverseMapperTest, UniverseRepositoryTest (each mirrors the GateNetwork test pattern from E.1 Step 3); UniverseDesignerServiceTest; UniverseSeederTest (vacuous — no canonical Universe rows ship from a Spring seeder; they're all in V15 migration).
- **Step 3**: Per-subtype `universeId()` accessor tests (SpaceshipDesignTest, StationDesignTest, WeaponInstallationTest, MegastructureTest, GateNetworkTest each get a small extension); CatalogedInterfaceTest covers the default-method.
- **Step 4**: FlywayBaselineSmokeTest extension (V15 doesn't break baseline); V15MigrationAuditTest covers the 15 universe rows present + the UPDATE statement results.
- **Step 5**: UniverseActivationChangedEventTest; UniverseDesignerServiceActivationTest covers activate/deactivate + event publication.
- **Step 6**: UniverseFilteringServiceTest covers visibility logic; per-panel integration tests verify each designer panel filters correctly.
- **Step 7**: UniversesDialogTest (raw FXMLLoader pattern, FX-thread wrapped per E.1 Step 9 dialog tests).
- **Step 8**: StatusBarControllerTest extension; the four §7 invariant tests (UniverseFilteringInvariantsTest).

### §9.2 — Integration tests

- **CatalogSyncIntegrationTest** (extending Phase D.8's): verify that universe-scoped catalog entries sync correctly under the existing sync-by-id contract; in particular, the universe_id column round-trips correctly through the seeder pipeline.
- **WorldbuildingMenuIntegrationTest**: end-to-end click-through covering open dialog → toggle a universe → verify the designer panel updates → close.

### §9.3 — CatalogAuditTest extensions

Per §7.5, the existing audit test (Phase D.6) grows three new invariant checks. These become regression guards for the universe-tagging discipline.

---

## §10 — Verification (forward-looking)

Items here are claims F.1 will make about the shipped state. The plan-doc rollup in Step 9 fills in actual values.

| Claim | Evidence |
|---|---|
| Universe entity exists in `com.terranrepublic.assets`; mirrors GateNetwork pattern | Step 2 deliverables |
| All 5 catalog tables have `universe_id` FK column | Step 3 + V15 migration |
| V15 migration creates ~15 Universe rows; UPDATEs ~Y existing catalog entries | Step 4 (Y determined by Step 1 audit) |
| `UniverseFilteringService` is the single chokepoint for visibility decisions | Step 6 |
| Worldbuilding → Universes submenu lists universes and toggles activation | Step 7 |
| Status bar shows "Real only" or "Real + N universe(s) active" | Step 8 |
| §7.5 audit test asserts no universe row orphans | Step 8 |
| Real-data invariant holds across all activation combinations | Step 8 (UniverseFilteringInvariantsTest) |

---

## §11 — Forward links

| Phase | Adds | Depends on F.1 |
|---|---|---|
| F.2 | Aliases (R7.2) | Universe entity + activation mechanism for alias visibility |
| F.3 | Faction entity refactor (R7.6.2) | Universe entity to scope Faction rows |
| F.4 | Era entity per-universe (R7.7) | Universe entity |
| F.5 | Visual presentation rules (R7.8) | Universe entity for color/icon overrides |
| F.6 | Population rules + SolarSystemFeature universe scoping (R7.10) | Universe entity + filtering service |
| F.7 | Tech constraints (R7.11) | Universe entity for per-universe tech-level scales |
| F.8 | Events + timeline (R7.12) | Universe entity |
| F.9 | Economics + resources (R7.13) | Universe entity |
| F.10 | Threats + anomalies (R7.14) | Universe entity |

The single common dependency: F.1's Universe entity. Every subsequent F.x phase composes against this foundation.

---

## §12 — Naming conventions

- **Universe ids**: `catalog-universe-<slug>` where slug is lowercase, hyphen-separated, derived from the canonical universe title. Example: `catalog-universe-the-hitchhikers-guide-to-the-galaxy` (no apostrophe in slug; punctuation stripped).
- **Universe names**: human-readable, exact canonical title from the SF universe. "Legacy of the Aldenata", "Caine Riordan", "The Expanse".
- **Event class**: `UniverseActivationChangedEvent` in `com.teamgannon.trips.worldbuilding`.
- **Service class**: `UniverseFilteringService` + `UniverseDesignerService` in the same package.
- **Test class names**: `Universe*Test` for record/entity-level; `UniverseActivation*Test` for activation; `UniverseFiltering*Test` for filtering.

---

## §13 — Lessons (pre-implementation; updated retroactively)

These will be filled in retroactively in Step 9. Predicted lessons based on the design work:

1. **The Cataloged interface is doing more universe work than originally intended.** F.1 adds `universeId()` to the default-method set. F.3 will add `factionId()` similarly. The Cataloged interface is becoming the universal scope-carrier.

2. **Catalog audit data is messy.** The 59-value sourceUniverse audit revealed three concepts (universe, faction, era) baked into one String. This is the cost of not having type-system distinctions in Phase D.6's design; F.3/F.4 will pay it back with extracted entity refactors.

3. **The migration is the design.** V15's UPDATE statements *are* the disambiguation of existing data. Get them right and the whole platform's data is clean; get them wrong and every subsequent phase's data is wrong. Pre-migration audit (Step 1) is the most-important step despite being labeled "verification" rather than "implementation."

4. **Real data needs an absence-marker, not a presence-marker.** `universe_id = NULL` for canonical/real is cleaner than `universe_id = 'catalog-universe-real'` because there's no Universe row to manage activation state on. Real data is intrinsically always-active by virtue of being NULL.

5. **The activation `active` boolean on Universe is the simplest thing that could work.** Resist the temptation to overload Universe with activation history, per-user prefs, or scheduled activation. F.1 ships a boolean; if more is needed, the schema migration is small.

---

*End of Phase F.1 design doc. Awaiting Larry's ratification before Step 1 (audit + verification) begins.*
