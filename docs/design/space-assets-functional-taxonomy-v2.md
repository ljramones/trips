# Space Assets in Science Fiction — Functional Taxonomy Expansion (v2)

**Status**: design, pre-implementation
**Date**: 2026-05-28
**Supersedes**: `space-assets-functional-taxonomy.md` (v1)
**Scope**: `StationDesign` — adding a functional axis, catalog provenance, and operational lifecycle status, sized to cover non-spacecraft, non-planet-based space assets from the wealth of science fiction.

---

## Revision log: what changed from v1

This v2 revision is grounded in two external reviews. Per-change rationale:

| Change | Driver |
|---|---|
| Added `GOVERNMENT_ADMINISTRATION`, `MEDICAL_QUARANTINE`, `AGRICULTURAL_BIOSPHERE`, `ENERGY_COLLECTION`, `CONTAINMENT` to `StationFunction` | Real SF coverage gaps; Dyson swarms, hospital stations, Citadel-as-seat-of-government, closed-ecology habitats, alien-artifact vaults |
| Added `BORDER_CONTROL`, `FLEET_REPAIR`, `FLEET_ANCHORAGE`, `TOURISM` to `StationFunction` | Inspection/customs and fleet-maintenance gaps; tourism is a canonical commercial subcategory in many settings |
| Renamed `RELIGIOUS_CULTURAL` → `CULTURAL_EDUCATIONAL` | Broader and less niche; covers monasteries, universities, archives, schools uniformly |
| Renamed `MIXED_USE` → `MULTI_ROLE` | More domain-natural label |
| **Removed `DERELICT_RUIN` from `StationFunction`** | It's a status, not a function. Existing `OperationalState.DERELICT` handles current condition; `primaryFunction = UNKNOWN` handles "purpose unknown" |
| Added `CatalogProvenance` record (sourceType + universe + work + status) | Universe-tab grouping needs structured metadata, not free-text `source` string equality |
| Added `CatalogOperationalStatus` enum (HISTORIC, ACTIVE, PLANNED, FICTIONAL, CANCELLED) | Lifecycle status for actual stations; `OperationalState` covers physical condition, this covers documentary/historical status |
| Made `primaryFunction` mandatory non-null with `UNKNOWN` as default only for genuine mystery | Prevents lazy nulling-out |
| Made `secondaryFunctions` non-null, defaulting to empty immutable set, with the constraint that it cannot contain `primaryFunction` | Compact-constructor invariants |
| Documented constructor-arity ripple from current 27 fields to ~30 after expansion, with `CatalogProvenance` composition mitigating the spread | Prevents the additive change from silently breaking every existing call site |
| Explicitly noted that `faction` (builder/owner) and `allegiance` (current controller) already exist and should be used consistently | Both reviewers recommended adding "owner vs controller" fields without checking what's there |
| Added catalog audit tests as a first-class deliverable | Bridges this doc to Phase E's tab-strip work |
| Tweaked worked examples: DS9 primary is now `TRANSPORTATION_HUB`, Citadel primary is now `GOVERNMENT_ADMINISTRATION`, Salyut 1 secondary set cleared of `MILITARY_COMMAND` | More accurate per second reviewer |

What was **rejected** from the reviews and why:

- `MATRIOSHKA_NODE`, `STARGATE`, `NICOLL_DYSON_BEAM`, `SELF_REPLICATING_FACTORY` — these mix structural and functional axes; three of them belong in `StationType` or `WeaponInstallation`, and `STARGATE` already exists as `SpaceInfrastructure.TransportNode`. Adding them as functions would re-introduce the fragmentation v2 of the Constructs plan was specifically designed to prevent.
- `BANKING_FINANCIAL`, `ENTERTAINMENT` — specializations of `COMMERCIAL` that don't yet earn their own enum slot. The same logic that keeps `RESEARCH` from splitting into `BIOLOGY`/`PHYSICS`/`ASTRONOMY` applies. Deferred until a real station forces it.
- `XENOBIOLOGY_LAB` — specialization of `RESEARCH`. Same reasoning.
- `isDerelict` boolean flag — duplicates existing `OperationalState.DERELICT`. Two places for one fact, will drift.
- `isNeutralZone`, `isContested`, `isSanctuary`, `strategicImportance` — per-instance political/strategic context, not part of the design record. Future political-state layer if needed.
- `connectedToJumpGate`, `hasMassDriver`, `hasSpaceElevatorAnchor` — should be modeled as connections to other assets, not flags. The sealed-hierarchy architecture handles this naturally.
- `operationalCostPerYear` — undefined units; the tick-engine economy layer already provides where this kind of modeling belongs.
- `primaryFunctionDescription` — duplicates the existing `description` field.
- `ownerFaction` vs `controllingFaction` as new fields — `StationDesign` already has `faction` and `allegiance` covering exactly this distinction. Use what's there.

---

## 1. The problem (unchanged from v1)

The current `StationDesign` model captures *what a station is structurally* — its shape, mass, mobility, armament. The `StationType` enum covers ten structural categories: `BATTLESTATION, GATE_FORT, ORBITAL_CITADEL, SHIPYARD, HABITAT, CYLINDER, GENERATION_SHIP, OUTPOST, PIRATE_BASE, DEPOT`.

What the model cannot express is *what the station is for*. Babylon 5 and the ISS are both structurally `ORBITAL_CITADEL`s — large, multi-module, fixed-location, long-term-crewed. But Babylon 5's purpose is diplomatic; the ISS's purpose is scientific research. Forcing them into the same structural bucket loses the thing that defines them in their respective universes.

This document scopes a single additive change with three coordinated pieces:

1. A new `StationFunction` enum capturing role/purpose.
2. A new `CatalogProvenance` record capturing source universe and lifecycle status (where the station comes from and what its real-world or in-universe historical state is).
3. Two new fields on `StationDesign` — `primaryFunction`, `secondaryFunctions` — plus one field for the provenance composite, with compatibility-constructor defaults.

The change is sized to cover the canonical non-planet-based space assets that appear across science fiction.

---

## 2. Scope: what we are and aren't covering

### In scope

Any built object in space that is not a spacecraft and not planet-based, drawn from the wealth of science fiction. That means:

- Orbital stations of every kind (LEO, geostationary, lunar orbit, gas giant orbit, Lagrange points, stellar orbit, deep space).
- Free-flying mega-structures (ring worlds, Dyson swarms, generation ships, derelict precursor artifacts).
- Station-scale fortifications (the Death Star considered as a fortress, planetary defense forts at orbital altitude, Honor Harrington's system-defense fortresses).
- Infrastructure-class assets that happen to be station-shaped (deep-space communication relays at scale, navigation beacons at choke points, refueling depots at jump points).
- Ancient/derelict precursor objects that are catalogued as stations even though their builders aren't around (Forerunner installations, Prothean ruins).

### Out of scope

- Spaceships. Already covered by `SpaceshipDesign`.
- Planet-surface structures. Cities, ground bases, atmospheric platforms. These aren't space assets.
- Transport infrastructure as nodes (jump gates, wormhole mouths, the Ring network). Already covered by `SpaceInfrastructure.TransportNode`.
- Weapons-only installations (the SAPL itself, SheVa-class super-cannons). Already covered by `WeaponInstallation`. (A station that *contains* a major weapon as a function — Death Star, system-defense fortress — is in scope; the weapon installation is a separate asset, possibly attached.)
- Habitable celestial bodies that have been engineered (terraformed Mars, hollowed-out planetoids treated as worlds). A mining base *on* Ceres is in scope; Ceres-as-a-world is not.

One-line rule: **if you'd call it a "station" in casual SF conversation, it's in scope, regardless of size.** This includes Babylon 5 (city), Citadel (city-state), Halo (continent-sized), and a single navigation buoy.

---

## 3. The three-axis model

The expansion introduces three orthogonal axes describing every catalogued station. Each axis has a clear question it answers.

| Axis | Question | Drives | Where it lives |
|---|---|---|---|
| **Structure** | What is this thing physically? | Mass, span, mobility, armour, hangar volume | `StationType` (existing 10-value enum) |
| **Function** | What is this thing for? | In-universe role, who uses it, why it exists | `StationFunction` (new, 30 values) — `primaryFunction` + `secondaryFunctions` |
| **Provenance** | Where does this come from and what's its catalogue lifecycle? | Universe tab grouping, real-vs-fictional filtering, historical-vs-active filtering | `CatalogProvenance` (new record holding `SourceType`, universe, work, `CatalogOperationalStatus`) |

The three axes are orthogonal. A `CYLINDER` can be `RESIDENTIAL` function (Stanford Torus) or `WEAPONS_PLATFORM` function (Halo ring) — both from a `SCIENCE_FICTION` source under universe "Halo" or "various hard SF". The ISS is structurally `ORBITAL_CITADEL`, functionally `RESEARCH`, provenance `REAL/ACTIVE`. Skylab is structurally `HABITAT`, functionally `RESEARCH`, provenance `REAL/HISTORIC`. The combinations carry meaning.

A fourth, related axis — **physical condition** — already exists via `OperationalState`. That answers "what state is the physical asset in?" (`OPERATIONAL`, `DAMAGED`, `DERELICT`, `WRECK`, `UNDER_CONSTRUCTION`, `SALVAGED`). It does *not* duplicate `CatalogOperationalStatus`, which answers a different question ("what's the catalog/lifecycle status?"). See §4.3 for the distinction.

### Why secondary functions, not just a primary

Real and fictional large stations are routinely multi-functional. Babylon 5 is diplomatic, commercial, and residential. The Citadel is administrative, diplomatic, military, commercial, and residential. Tycho Station is industrial, shipbuilding, and commercial. Forcing them to one role flattens them; forcing them to multiple equal roles loses the *primary* purpose the station is famous for.

The shape: `StationFunction primaryFunction` (the one-line answer to "what is this?") plus `Set<StationFunction> secondaryFunctions` (the supporting roles, typically 0–3). Most catalogued stations will have an empty secondary set; the deeply multi-purpose ones won't.

---

## 4. The proposed enums and record

### 4.1 `StationFunction` — 30 values

Organized by family. The grouping is editorial — the enum itself is flat.

#### Military

| Value | Meaning | Canonical SF examples |
|---|---|---|
| `MILITARY_COMMAND` | HQ, command authority, flag officer's seat | Starbase 1 (Trek), Earth Spacedock, Imperial fleet HQs |
| `WEAPONS_PLATFORM` | Station's primary purpose is firing things | Death Star, Starkiller Base, Ktoran fortresses |
| `DEFENSIVE` | Planetary/system defense, primarily reactive | Honor Harrington system-defense forts, planetary shield generators |
| `SURVEILLANCE` | Listening, early warning, intelligence gathering | Border listening posts, deep-space ELINT |
| `BORDER_CONTROL` | Customs, immigration, inspection, transit checkpoints | Expanse inspection stations, Trek-style border posts |
| `FLEET_ANCHORAGE` | Where the fleet sits between operations | Honor Harrington's repair-and-resupply anchorages |
| `FLEET_REPAIR` | Major fleet maintenance and refit | Utopia Planitia (refit aspect), Starbase 1 yard work |

#### Governance and civilian core

| Value | Meaning | Canonical SF examples |
|---|---|---|
| `GOVERNMENT_ADMINISTRATION` | Seat of civil authority, sector governance, bureaucratic hub | The Citadel (administrative aspect), capital-world orbital seats |
| `DIPLOMATIC` | Neutral ground, embassies, treaty venues | Babylon 5, Deep Space 9 (diplomatic aspect), Citadel (diplomatic aspect) |
| `RESEARCH` | Science, observation, laboratories | ISS, Project Lazarus station, hard-SF deep-space telescopes |
| `RESIDENTIAL` | Primarily where people live | Stanford Torus, Bernal sphere habitats, Expanse belter stations |
| `COMMERCIAL` | Trade hub, markets, business | Tycho's commercial face, Nar Shaddaa, freeport stations |
| `TOURISM` | Resort stations, cruise terminals, leisure facilities | Risa (Trek), Mass Effect's various leisure locales |
| `MEDICAL_QUARANTINE` | Hospitals, plague isolation, biohazard containment, recovery | Hospital stations, plague quarantine platforms, biohazard isolation |

#### Industrial

| Value | Meaning | Canonical SF examples |
|---|---|---|
| `INDUSTRIAL` | Manufacturing, fabrication, finished goods | Tycho's industrial face, Roche Habitat, factory ships in orbit |
| `SHIPBUILDING` | Constructing or repairing ships specifically | Kuat Drive Yards, Utopia Planitia (construction aspect) |
| `MINING_REFINING` | Resource extraction and processing | Belter colonies, Expanse refineries, Bespin's gas mining |
| `LOGISTICS_DEPOT` | Storage, resupply, refueling | Military supply nodes, fuel scoops at gas giants, ice depots |
| `AGRICULTURAL_BIOSPHERE` | Orbital farms, closed-ecology food production | Robinson-style closed loops, generation-ship greenhouses |
| `ENERGY_COLLECTION` | Solar power, Dyson swarm elements, stellar-energy harvest | Solar power satellites, Dyson swarm collectors, beamed-power platforms |

#### Transit and infrastructure

| Value | Meaning | Canonical SF examples |
|---|---|---|
| `TRANSPORTATION_HUB` | Passenger and cargo transit at scale | DS9 (post-wormhole), busy Expanse stations, port-of-call stations |
| `COMMUNICATION_RELAY` | Comms infrastructure, signal repeating | Mass Effect comm buoys at scale, Foundation hyperspace relays |
| `NAVIGATION_BEACON` | Navigation aid, jump-point markers, hazard warning | Hard-SF nav buoys, choke-point markers |

#### Specialized

| Value | Meaning | Canonical SF examples |
|---|---|---|
| `COLONIZATION` | Generation ships, ark function, in-transit colonies | Nauvoo/Behemoth, BSG colony ships, Robinson's Aurora |
| `PENAL` | Prison, detention, exile | Penal asteroids, prison stations |
| `CULTURAL_EDUCATIONAL` | Monasteries, universities, archives, cultural institutions, schools | Streeling University (Foundation), monastery stations, Bene Gesserit orbital schools |
| `TERRAFORMING_CONTROL` | Planetary engineering coordination, orbital mirrors, atmospheric processing | Mars trilogy control stations, soletta-class mirrors |
| `CONTAINMENT` | Holding the dangerous — alien artifacts, sealed anomalies, rogue AI | Alien artifact vaults, precursor containment, sealed anomaly platforms |

#### Catch-alls

| Value | Meaning | When to use |
|---|---|---|
| `MULTI_ROLE` | Genuinely multi-purpose, no clear primary | Last resort; flag in javadoc as needing justification in catalog seeds |
| `UNKNOWN` | Function not yet determined or genuinely mysterious | Default for entries pending categorization; legitimate for unexplored precursor sites |

**Note on `DERELICT_RUIN`**: v1 had this as a function value. v2 removes it. Derelict-ness is a *status*, not a *function*, and the existing `OperationalState.DERELICT` / `WRECK` / `SALVAGED` already cover it. A Halo ring whose original purpose was known but is now abandoned is `primaryFunction = WEAPONS_PLATFORM` + `operationalState = DERELICT`. A Forerunner installation whose purpose is unknown is `primaryFunction = UNKNOWN` + `operationalState = DERELICT`. The combination handles both cases without conflating the axes.

### 4.2 `CatalogProvenance` record — new

```java
public record CatalogProvenance(
    SourceType sourceType,           // Existing enum: REAL, PROPOSED, SCIENCE_FICTION, UNKNOWN
    String sourceUniverse,           // e.g. "Aldenata", "Troy Rising", "Real / Proposed", "The Expanse"
    String sourceWork,               // e.g. "Legacy of the Aldenata", "Babylon 5", nullable
    CatalogOperationalStatus status  // HISTORIC, ACTIVE, PLANNED, FICTIONAL, CANCELLED, UNKNOWN
) {}
```

Reuses the existing `SourceType` enum already used by `SpaceshipDesign` rather than introducing a parallel one. This sets up a future consolidation where both subtypes share `CatalogProvenance` (not in scope for this phase, but the shape is compatible).

The composite avoids spreading four fields directly across `StationDesign` and parallels the existing `MassBudget` composition pattern.

### 4.3 `CatalogOperationalStatus` enum — new

| Value | Meaning |
|---|---|
| `HISTORIC` | Real station that existed, flew, and was retired/destroyed (Skylab, Mir, Salyut series) |
| `ACTIVE` | Real station currently operational (ISS, Tiangong) |
| `PLANNED` | Real station planned or under construction (Lunar Gateway, Axiom) |
| `CANCELLED` | Real station planned but cancelled (Freedom, Skylab B) |
| `FICTIONAL` | In-universe station from any fictional setting |
| `UNKNOWN` | Status not yet determined |

#### How this differs from the existing `OperationalState`

| | `OperationalState` (physical) | `CatalogOperationalStatus` (catalog) |
|---|---|---|
| Question | What state is the physical asset in? | What's the documentary/historical status? |
| Values | OPERATIONAL, DAMAGED, DERELICT, WRECK, UNDER_CONSTRUCTION, SALVAGED | HISTORIC, ACTIVE, PLANNED, CANCELLED, FICTIONAL, UNKNOWN |
| Mir example | `SALVAGED` (deorbited) | `HISTORIC` (was a real station, no longer extant) |
| Babylon 5 example | `OPERATIONAL` (in its setting) | `FICTIONAL` (it's from a TV show) |
| Death Star example | `OPERATIONAL` (in its setting) | `FICTIONAL` |
| Lunar Gateway example | `UNDER_CONSTRUCTION` | `PLANNED` |

Two distinct facts about the same station, neither redundant.

### 4.4 `StationDesign` additions

Three new fields, two of which are functions, one of which is the provenance composite:

```java
public record StationDesign(
    // ... existing 27 fields ...
    StationFunction primaryFunction,           // NEW — non-null, defaults to UNKNOWN
    Set<StationFunction> secondaryFunctions,   // NEW — non-null, defaults to empty
    CatalogProvenance provenance               // NEW — non-null; provenance with default UNKNOWN values is acceptable
) implements SpaceAsset { ... }
```

Compact-constructor invariants:
1. `primaryFunction != null` (default `UNKNOWN` if not supplied)
2. `secondaryFunctions != null` (default empty immutable set)
3. `!secondaryFunctions.contains(primaryFunction)` — secondary set cannot duplicate the primary
4. `provenance != null` (default `new CatalogProvenance(UNKNOWN, "", null, UNKNOWN)`)

The compatibility constructor for old callers fills these with defaults. Existing call sites compile unchanged.

---

## 5. Worked examples

Updated from v1 per the second reviewer's suggestions. Showing the three axes plus the existing operational-state field for select cases.

| Station | `StationType` | `primaryFunction` | `secondaryFunctions` | Provenance |
|---|---|---|---|---|
| **ISS** | `ORBITAL_CITADEL` | `RESEARCH` | `{}` | REAL / "Real / Proposed" / null / ACTIVE |
| **Tiangong** | `ORBITAL_CITADEL` | `RESEARCH` | `{}` | REAL / "Real / Proposed" / null / ACTIVE |
| **Mir** | `ORBITAL_CITADEL` | `RESEARCH` | `{}` | REAL / "Real / Proposed" / null / HISTORIC |
| **Skylab** | `HABITAT` | `RESEARCH` | `{}` | REAL / "Real / Proposed" / null / HISTORIC |
| **Salyut 1** | `HABITAT` | `RESEARCH` | `{}` | REAL / "Real / Proposed" / null / HISTORIC |
| **Salyut Almaz variants** | `HABITAT` | `SURVEILLANCE` | `{RESEARCH}` | REAL / "Real / Proposed" / null / HISTORIC |
| **Lunar Gateway** | `OUTPOST` | `RESEARCH` | `{LOGISTICS_DEPOT}` | REAL / "Real / Proposed" / null / PLANNED |
| **Axiom Station** | `HABITAT` | `RESEARCH` | `{COMMERCIAL}` | REAL / "Real / Proposed" / null / PLANNED |
| **Babylon 5** | `ORBITAL_CITADEL` | `DIPLOMATIC` | `{COMMERCIAL, RESIDENTIAL}` | SCIENCE_FICTION / "Babylon 5" / "Babylon 5" / FICTIONAL |
| **Deep Space 9** | `ORBITAL_CITADEL` | `TRANSPORTATION_HUB` | `{DIPLOMATIC, COMMERCIAL, MILITARY_COMMAND}` | SCIENCE_FICTION / "Star Trek" / "Deep Space Nine" / FICTIONAL |
| **The Citadel** | `ORBITAL_CITADEL` | `GOVERNMENT_ADMINISTRATION` | `{DIPLOMATIC, MILITARY_COMMAND, COMMERCIAL, RESIDENTIAL}` | SCIENCE_FICTION / "Mass Effect" / null / FICTIONAL |
| **Death Star** | `BATTLESTATION` | `WEAPONS_PLATFORM` | `{MILITARY_COMMAND}` | SCIENCE_FICTION / "Star Wars" / null / FICTIONAL |
| **Starkiller Base** | `BATTLESTATION` | `WEAPONS_PLATFORM` | `{MILITARY_COMMAND}` | SCIENCE_FICTION / "Star Wars" / null / FICTIONAL |
| **Tycho Station** | `ORBITAL_CITADEL` | `SHIPBUILDING` | `{INDUSTRIAL, COMMERCIAL, RESIDENTIAL}` | SCIENCE_FICTION / "The Expanse" / null / FICTIONAL |
| **Ceres Station** | `OUTPOST` | `MINING_REFINING` | `{RESIDENTIAL, COMMERCIAL}` | SCIENCE_FICTION / "The Expanse" / null / FICTIONAL |
| **Medina Station** | `CYLINDER` | `TRANSPORTATION_HUB` | `{DIPLOMATIC, COMMERCIAL}` | SCIENCE_FICTION / "The Expanse" / null / FICTIONAL |
| **Nauvoo / Behemoth** | `GENERATION_SHIP` | `COLONIZATION` | `{}` | SCIENCE_FICTION / "The Expanse" / null / FICTIONAL |
| **Kuat Drive Yards** | `SHIPYARD` | `SHIPBUILDING` | `{INDUSTRIAL}` | SCIENCE_FICTION / "Star Wars" / null / FICTIONAL |
| **Halo ring (active)** | `CYLINDER` | `WEAPONS_PLATFORM` | `{RESEARCH}` | SCIENCE_FICTION / "Halo" / null / FICTIONAL |
| **Halo ring (encountered, abandoned)** | `CYLINDER` | `WEAPONS_PLATFORM` | `{}` | (same provenance, `OperationalState.DERELICT`) |
| **Stanford Torus** | `CYLINDER` | `RESIDENTIAL` | `{AGRICULTURAL_BIOSPHERE}` | PROPOSED / "Real / Proposed" / null / PLANNED |
| **O'Neill Cylinder** | `CYLINDER` | `RESIDENTIAL` | `{INDUSTRIAL, AGRICULTURAL_BIOSPHERE}` | PROPOSED / "Real / Proposed" / null / PLANNED |
| **Ringworld (encountered)** | `CYLINDER` | `UNKNOWN` | `{}` | SCIENCE_FICTION / "Niven Known Space" / "Ringworld" / FICTIONAL (`OperationalState.DERELICT`) |
| **Dyson swarm element** | `OUTPOST` | `ENERGY_COLLECTION` | `{}` | SCIENCE_FICTION / various / null / FICTIONAL |
| **Troy** (Troy Rising) | `GATE_FORT` | `DEFENSIVE` | `{MILITARY_COMMAND}` | SCIENCE_FICTION / "Troy Rising" / "Troy Rising" / FICTIONAL |
| **Forerunner installation** | `ORBITAL_CITADEL` | `UNKNOWN` | `{}` | SCIENCE_FICTION / "Halo" / null / FICTIONAL (`OperationalState.DERELICT`) |
| **Vulcan orbital monastery** | `OUTPOST` | `CULTURAL_EDUCATIONAL` | `{}` | SCIENCE_FICTION / "Star Trek" / null / FICTIONAL |
| **Prison asteroid** | `OUTPOST` | `PENAL` | `{}` | (varies by source) |
| **Mars terraforming station** | `OUTPOST` | `TERRAFORMING_CONTROL` | `{RESEARCH}` | SCIENCE_FICTION / "Mars Trilogy" / "Red Mars" / FICTIONAL |
| **Honor Harrington system defense fort** | `BATTLESTATION` | `DEFENSIVE` | `{MILITARY_COMMAND}` | SCIENCE_FICTION / "Honorverse" / null / FICTIONAL |
| **Quarantine station (any setting)** | `OUTPOST` | `MEDICAL_QUARANTINE` | `{}` | (varies) |
| **Alien artifact containment vault** | `OUTPOST` | `CONTAINMENT` | `{RESEARCH}` | (varies; Children of the Pattern would use FICTIONAL / "Children of the Pattern") |

What this shows:
- Most stations land cleanly with one primary and 0–3 secondaries.
- The deeply multi-purpose ones (Citadel) reach four secondaries — but rarely more.
- `UNKNOWN` is reserved for genuine mystery (Ringworld on first encounter, Forerunner installations). It is not a default-from-laziness.
- `MULTI_ROLE` doesn't appear in any worked example. Every station has a discernible primary. The catch-all is reserved for cases we genuinely can't categorize after thought.
- `OperationalState.DERELICT` and `CatalogOperationalStatus.FICTIONAL` are independent — a fictional Halo ring is *both* fictional (per the catalog) *and* derelict (per the physical state in its own narrative).

---

## 6. What this change is NOT

- **Not a replacement of `StationType`.** Both structural and functional axes coexist.
- **Not a new sealed subtype.** No new record type. The change is additive fields on `StationDesign`.
- **Not a schema overhaul.** Per the Phase A0 / Issue 46 discipline, additive change with compatibility constructor, one Flyway migration, every existing call site continues to compile.
- **Not an expansion of `WeaponInstallation` or `TransportNode`.** Those subtypes have their own functional clarity.
- **Not a population pass on all canonical SF stations.** Categorizing Troy + the 8 real stations + the 2 Posleen ships is part of the implementation phase. Populating Babylon 5, Citadel, Tycho, etc. is a separate future data-population phase.
- **Not a UI redesign.** The editor dialog gains controls for the new fields. The details pane shows them. Same Phase D pattern.

---

## 7. Implementation shape (preview)

The implementation prompt will be Phase D.6. Rough outline (the prompt comes separately):

1. **New `StationFunction` enum** with the 30 values from §4.1.
2. **New `CatalogOperationalStatus` enum** with the 6 values from §4.3.
3. **New `CatalogProvenance` record** per §4.2, using the existing `SourceType` enum.
4. **Three new fields on `StationDesign`**: `primaryFunction`, `secondaryFunctions`, `provenance`. Compact-constructor invariants per §4.4. Compatibility constructor handles defaults so every existing caller compiles unchanged.
5. **JPA changes**: columns on `STATION_DESIGN` table for `primary_function` (VARCHAR enum name), `secondary_functions_json` (JSON LOB), `provenance_source_type`, `provenance_source_universe`, `provenance_source_work`, `provenance_status` (four flat columns for the composite — provenance is small and frequently filtered on, flat columns serve better than a single JSON blob). Flyway migration adds the columns idempotently. Migration number is whatever comes next in the V-sequence (likely V10 after V9 = transport_node, but the agent should verify the current high-water mark).
6. **Mapper**: bidirectional round-trip for all three new fields. Parameterized test over every `StationFunction` as primary; parameterized over every `CatalogOperationalStatus`; round-trip for a handful of secondary-set permutations.
7. **Editor dialog**: `StationEditorDialog` gains a "Function" section with `ComboBox<StationFunction>` for primary and a multi-select control (CheckComboBox or equivalent) for secondaries. UI invariant: changing primary clears that value from the secondary set if present. Plus a "Catalog" section with controls for the provenance fields. Tooltips, accessibility annotations, theme.css classes — same Phase D pattern.
8. **Details pane**: `InstallationDesignerPanel` station details template gains rows for primary function, secondary functions, source universe, source work, and catalog status.
9. **Filter**: the panel's filter strip gains a "Function" filter (ComboBox<StationFunction>, including "All"). Composes with the existing Kind / Subtype / Faction / Category filters. Note: this is *in addition to* the universe tab strip from Phase D.5, not a replacement.
10. **Catalog re-population**: Troy, the 8 real stations, and the 2 Posleen ships get their `primaryFunction` and `provenance` filled in deliberately per the §5 worked examples.
11. **Constructor-arity ripple**: every call site that uses the canonical `StationDesign` record constructor gets updated, or routes through the compatibility constructor that fills new fields with defaults. The `StationEditorDialog.buildDraft()` must pass all three new fields. The `StationEditorDialogTest` Troy round-trip must assert all three fields persist.
12. **Catalog audit tests** (bridges this work to Phase E):
    - Every `StationDesign` in `Catalog.all()` has `primaryFunction != UNKNOWN`.
    - No `StationDesign` in `Catalog.all()` has `secondaryFunctions` containing its `primaryFunction`.
    - Every `StationDesign` in `Catalog.all()` has a non-blank `provenance.sourceUniverse()`.
    - Every entry where `provenance.sourceType() == REAL` has `provenance.status() ∈ {HISTORIC, ACTIVE, PLANNED, CANCELLED}`.
    - Every entry where `provenance.sourceType() == SCIENCE_FICTION` has `provenance.status() == FICTIONAL`.
    - Troy's provenance: `sourceUniverse = "Troy Rising"`, `sourceWork = "Troy Rising"`, `sourceType = SCIENCE_FICTION`, `status = FICTIONAL`. (The Aldenata mention in the earlier ChatGPT review was incorrect — Troy is from Ringo's *Troy Rising* series, not the Aldenata / Posleen series. The Posleen Command/Battle Dodecahedrons are the ones with `sourceUniverse = "Aldenata"` and `sourceWork = "Legacy of the Aldenata"`.)
    - No phantom-default-universe rows: there should be no entries where `provenance.sourceUniverse()` is something like "Dynamis" or any other accidental default that doesn't correspond to a real seeded entry.

13. **MULTI_ROLE audit**: a test (or commit-time check) that any `StationDesign` in `Catalog.all()` using `primaryFunction = MULTI_ROLE` carries a non-blank `description` containing the rationale.

Expected test count rise: ~30–40 from parameterized coverage, the new field exercises, and the catalog audit tests.

---

## 8. Open questions before the implementation prompt

To resolve before the next prompt is written.

1. **`MULTI_ROLE` as last-resort**: the design pins it as needing justification in catalog seeds. Confirm: enforce via test (any seed using `MULTI_ROLE` must have a description containing some rationale string), or document-only convention? Recommendation: enforce via test. The cost of the test is small; the cost of `MULTI_ROLE` becoming the lazy bucket is large.
2. **Secondary functions UI cap**: soft three-cap recommendation in v1 stands. Recommendation: no hard validation, but the editor dialog displays a non-blocking hint ("More than three secondary functions usually means the primary is wrong or MULTI_ROLE may be needed").
3. **Niche values retained**: `PENAL`, `TERRAFORMING_CONTROL`, `CONTAINMENT` are all niche. Recommendation: keep all three. Cost of unused enum value is near-zero; cost of needing one that doesn't exist is a schema change.
4. **Provenance composition vs flat fields**: this v2 proposes `CatalogProvenance` as a composite record. The alternative is four flat fields directly on `StationDesign`. Composite reads cleaner, isolates the concept, and sets up future sharing with `SpaceshipDesign`. Recommendation: composite as designed. The JPA mapper flattens it to four columns for query performance.
5. **Any function missed for Caine Riordan / Children of the Pattern**: the 30-value list covers canonical SF and the worked examples include Troy. Recommendation: explicit review by the product owner — are there functional roles in the Riordan setting (Ktoran institutions, Slaasriithi cultural sites, Arat Kur facilities) or in Children of the Pattern (Veyara living-ship adjuncts, RS-dimensional containment, Morvaïn cultural archives) that don't map to anything above? If yes, name them; otherwise CONTAINMENT and CULTURAL_EDUCATIONAL should cover most cases.

---

## 9. Done definition (for the design — not the implementation)

This document is "done" when:

- The three-axis model (structural + functional + provenance) is agreed.
- The 30-value `StationFunction` enum is reviewed and adjusted to taste.
- The 6-value `CatalogOperationalStatus` enum and the `CatalogProvenance` record are agreed.
- The compact-constructor invariants are agreed.
- The five open questions in §8 are resolved.
- The worked examples in §5 are accepted as the verification target.

When all six hold, the implementation prompt is straightforward — same shape as the v2 Phase B mapper expansion combined with Phase A0 Step 5's dialog control additions. Expected scope: larger than Phase A0 Step 5, smaller than Phase D's three-dialog buildout.

---

## 10. Notes for the coding agent verifying this design against the codebase

Before writing the implementation, the agent should verify:

1. **The current `StationDesign` field count.** v1 said 27; this design assumes that. If it's different (e.g. if Phase D.5 added a field), the constructor-arity discussion needs updating.
2. **The current `SourceType` enum values.** This design assumes `REAL, PROPOSED, SCIENCE_FICTION, UNKNOWN` per the inventory. Confirm those are the actual values and that the enum lives where the inventory said.
3. **The current `OperationalState` enum values.** This design assumes `OPERATIONAL, DAMAGED, DERELICT, WRECK, UNDER_CONSTRUCTION, SALVAGED`. Confirm.
4. **`faction` and `allegiance` field semantics on `StationDesign`.** This design assumes `faction = builder/owner`, `allegiance = current controller`. Confirm.
5. **The current Flyway high-water mark.** This design assumes V9 is the latest from Phase B's transport_node migration. Confirm before assigning the new migration number.
6. **Catalog membership.** This design assumes `Catalog.all()` currently contains 5 entries (Troy, SAPL, SheVa, two Posleen ships) — *before* the 8 real stations from Phase D.5 land. If Phase D.5 has already landed by the time the implementation prompt runs, the count is 13 and the worked-examples table in §5 should already match those entries.
7. **The Phase D.5 universe-tab implementation.** This design's §7 step 9 mentions that the function filter is *in addition to* the tab strip. Confirm what Phase D.5 actually built so the implementation doesn't conflict.

If any of these assumptions are wrong, flag them in the agent's response before starting implementation, not silently in the diff.
