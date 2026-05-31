# Phase E.1 — In-System Feature Foundation

**Status**: as-built design (committed retroactively at close-out)
**Date**: 2026-05-30
**Scope**: Establish the in-system feature foundation that subsequent Phase E sub-phases build on. Introduces the `TransitMode` taxonomy, extends `DriveType` to carry transit-mode sets, adds the `GateNetwork` entity with full Repository/Service/Seeder pipeline, extends `SolarSystemFeature` with `parentBodyId` + `catalogReferenceId` + `catalogReferenceKind` + `networkId` columns plus a `JUMP_POINT` featureType, implements deterministic per-star jump-point computation with Hill-sphere clean-zone rejection, wires the Spring-event-based activation hook, and extends the renderer to draw jump points.

This document is the as-built record after all 8 implementation steps landed and were ratified. Corrections to the original pre-implementation design (where the codebase turned out to differ from what the design predicted) are reflected throughout; the Step 1 verification list in §10 captures the original gaps.

---

## §1 Why this exists

Phase E is the interstellar routing physics arc, broken down as:

- **E.1** (this phase): Structural foundation — the data model exists, the activation hook fires, jump points appear in systems on first entry.
- **E.2**: Catalog-reference population — place existing catalog entries (Troy, ISS, SAPL elements, etc.) as `SolarSystemFeature` instances in specific systems. Populate canonical wormhole mouths and gate-network instances.
- **E.3**: Routing integration — bridge `SolarSystemFeature` instances to the existing `RouteFindingService`. Drive-type-aware route computation. Range gating, wormhole edges, gate-network edges, jump-point edges.
- **E.4**: Optional ultimate transfer planner — full in-system + interstellar + in-system composition.

E.1 establishes the structural foundation. It doesn't populate canonical instances (E.2), doesn't change routing (E.3), and doesn't compose routes (E.4). Each subsequent phase builds on E.1's structure.

What E.1 does NOT do (out of scope, documented to bound the work):
- Populate canonical wormhole mouths (E.2)
- Place Troy, ISS, SAPL elements, etc. as SolarSystemFeature instances (E.2)
- Modify `RouteFindingService` or any routing code (E.3)
- Add UI for editing GateNetwork entities (deferred)
- Add UI for editing per-feature catalogReferenceId / networkId (deferred)
- Implement transponder discovery mechanics (gameplay/narrative, not E.1)
- Add new DriveType values (E.2 catalog work)

---

## §2 Scope — five deliverable groups

1. **Transit-mode taxonomy and DriveType extension.** New `TransitMode` enum with 5 values. `DriveType` gains a `Set<TransitMode>` field. All 25 existing values populated per the locked §4.3 mapping.

2. **GateNetwork entity and pipeline.** New JPA entity. New Repository, DesignerService (with `syncCatalogEntries` D.8 contract), CatalogSeeder. Lives outside the sealed `SpaceAsset`/`SpaceInfrastructure` hierarchies as its own top-level worldbuilding concept. `SpaceshipDesign` extended to carry `Set<String> defaultAccessibleNetworkIds`.

3. **SolarSystemFeature extension.** Four new columns: `parentBodyId` (per-body parent reference; resolves §10 Divergence C), `catalogReferenceId`, `catalogReferenceKind` (typed as the new `CatalogedKind` per §10 Divergence B), `networkId`. New featureType constant value: `JUMP_POINT`. V14 migration.

4. **Jump-point computation.** New `JumpPointComputer` class implementing the deterministic-seed algorithm with Hill-sphere clean-zone rejection. Per-star: each star in a system gets its own jump point (or absence-of-feature for jump-inaccessible stars).

5. **Activation hook + renderer extension.** New `SolarSystemActivatedEvent`. New `JumpPointActivationListener` component. Hook insertion in `SolarSystemSpacePane.render()`. `SolarSystemRenderer` extended to draw `JUMP_POINT` features (MEDIUMPURPLE sphere, 1.5× baseSize, with glow).

---

## §3 Jump-point computation

### §3.1 Algorithm

Per-star deterministic computation with rejection sampling against mass-body Hill spheres.

For each star in an activated system:

1. **Compute the outer-system band.** Outer boundary = the outermost notable mass body's orbital distance (or, for HYG-database stars without detailed planetary inventory, a stellar-mass-derived estimate via `40 × cbrt(stellarMassSolar)` AU). 75% × outer-boundary = inner radius of band. 100% × outer-boundary = outer radius.

2. **Seed the random generator from the star's id.** `long seed = star.getId().hashCode()`. Deterministic across JVM restarts.

3. **For iteration 0 to 99:**
   a. Perturb the seed: `Random rng = new Random(seed ^ iteration)`.
   b. Sample a candidate position uniformly on the spherical shell:
      - `radius = innerRadius + rng.nextDouble() * (outerRadius - innerRadius)`
      - `polar = acos(2 * rng.nextDouble() - 1)` (uniform on sphere)
      - `azimuthal = rng.nextDouble() * 2 * π`
      - `candidate = Vec3.fromSpherical(radius, polar, azimuthal)`
   c. Check the candidate against the system's geometry. If the candidate falls inside any mass body's exclusion zone (§3.2), reject and continue.
   d. If the candidate is clear, return `Optional.of(candidate)`.

4. **If all 100 iterations are rejected**, return `Optional.empty()`. The star is jump-inaccessible. No JUMP_POINT feature is created. Absence-of-feature represents the state.

### §3.2 Exclusion zones — toroidal Hill-sphere check

A candidate position is rejected if it falls within any mass body's Hill sphere.

For each mass body in the system geometry:
- Compute the Hill sphere radius via `hillSphereRadius(planetMassSolar, sma, starMassSolar)`.
- The candidate is rejected if `|candidateRadius - planet.sma| < hillRadius` — the toroidal Hill-sphere check.

This treats the planet's Hill region averaged over an orbit as a torus around the orbit. Implementation-wise this is conservative (slightly over-rejecting positions a planet may not actually swing through at the candidate's azimuthal coordinate), but it matches the worldbuilding intent of "jump points unstable in mass-dominated regions" without requiring orbital-phase tracking.

Worldbuilding rationale: jump points are unstable in local mass-dominated regions; placing a jump point inside a planet's Hill sphere would cause the planet to undergo quakes and eventual breakup. The fictional gravitational physics avoids these regions.

### §3.3 Worked example: Sol

Star: Sol (stellar mass = 1.0 solar masses).

Outer-system band: HYG fallback → 40.0 AU outer boundary. Band is 30.0–40.0 AU.

Iteration 0 with `seed = Sol.id.hashCode()`: some deterministic position in the band.

Sol's planet inventory (when populated from exoplanet records or built in-memory): no notable bodies in the 30–40 AU band (Pluto-ish bodies have tiny Hill spheres). First iteration likely passes.

Result: Sol gets a single JUMP_POINT feature at some deterministic position in the 30–40 AU band, **same coordinates every JVM restart** (the deterministic-seed contract).

For a binary like Alpha Centauri: each star (A, B, plus Proxima as a distant tertiary in the runtime model) gets its own jump point, computed from its own id and its own outer-system band.

### §3.4 Persistence

Each jump-point result becomes a `SolarSystemFeature` row with:
- `solarSystemId` = the activated system's id
- `parentBodyId` = the star's id (per-star scope; resolves Divergence C by adding the `parent_body_id` column in V14)
- `featureType` = `"JUMP_POINT"`
- `featureCategory` = `"NATURAL"`
- `orbitalRadiusAU` / `orbitalAngleDeg` / `orbitalHeightAU` = the computed Cartesian position projected into the entity's existing spherical-ish coordinate columns (radius in xy plane, angle as azimuth in degrees, height = z)

The feature is persisted once on first activation. Subsequent activations of the same system find the existing feature (per `findByParentBodyIdAndFeatureType` query, added in Step 4) and skip the computation. If the feature is deleted, the next activation regenerates it at the same coordinates because the seed is deterministic.

---

## §4 Transit modes and DriveType extension

### §4.1 The five transit modes

`TransitMode` enum in `com.terranrepublic.assets`:

```java
public enum TransitMode {
    SUBLIGHT,        // Continuous in-system propulsion (chemical, fusion, ORION).
    JUMP_POINT,      // Discrete star-to-star FTL using natural jump points as focal points.
    WORMHOLE,        // Discrete transit through paired wormhole mouths.
    JUMP_GATE,       // Discrete network-membership-gated transit via constructed gates.
    WARP             // Continuous FTL travel through space (warp / hyperspace).
}
```

Five values. Discrete-vs-continuous axis splits the modes.

### §4.2 DriveType extension

**Correction from the pre-implementation design**: the original §4.2 said "DriveType is currently a simple enum". Reality: `DriveType` was already a substantial typed enum carrying `Category category` + `DriveSpecs specs` constructor data, with 25 values. Step 1 verification surfaced this; the work scope adjusted accordingly.

Implementation: the existing constructor signature `(Category, DriveSpecs)` extended to `(Category, DriveSpecs, Set<TransitMode>)`. Each value declaration appends `, Set.of(TransitMode.X)`. New public accessor `transitModes()`.

### §4.3 Locked 25-value mapping

After canon analysis (using the `sciFiReferences` javadoc strings as evidence) plus Larry's confirmation of the 2 genuinely-ambiguous cases:

| Mapping | Count | DriveType values |
|---|---|---|
| `{SUBLIGHT}` | 22 | `CHEMICAL_BIPROPELLANT`, `SOLID_ROCKET`, `ION_GRIDDED`, `HALL_EFFECT`, `VASIMR`, `NUCLEAR_THERMAL`, `NUCLEAR_ELECTRIC`, `GAS_CORE_NUCLEAR`, `ORION_PULSE`, `ORION`, `FUSION_TORCH`, `FUSION_PULSE`, `EPSTEIN_DRIVE`, `TERRAN_FUSION_DRIVE`, `HKHRKH_THRUST`, `ANTIMATTER_BEAM_CORE`, `LASER_SAIL`, `SOLAR_SAIL`, `BUSSARD_RAMJET`, `KTORAN_ADVANCED`, `POSLEEN_NORMAL_SPACE`, `SPIN_DRIVE` |
| `{JUMP_GATE}` | 1 | `GRTUL_GATE` — single-mode per javadoc "no onboard thrust"; deprecation tracked for E.2 alongside catalog migration to GateNetwork-based modeling |
| `{WARP}` | 1 | `GALACTIC_HYPER` — continuous strategic FTL; no SUBLIGHT companion (ships using GALACTIC_HYPER need a separate sublight drive — currently unmodeled multi-drive territory) |
| `{}` (empty) | 1 | `NONE` — structural absence of a drive |

**Total: 25 values.** `DriveTypeTest.partitionMatches25` enforces this partition exhaustively as a regression guard.

### §4.4 Scope fence

Phase E.1's Step 2 populates transit modes on the existing 25 values only. New FTL DriveType additions (SHIFT_DRIVE for Caine Riordan canon, POSLEEN_INTERSTELLAR distinct from POSLEEN_NORMAL_SPACE, ALDENATA_WORMHOLE_CAPABLE, etc.) are deferred to Phase E.2 alongside their associated ship designs and gate-network catalog entries.

---

## §5 GateNetwork entity and pipeline

### §5.1 GateNetwork record

`GateNetwork` is a new top-level worldbuilding concept. It's **not** a `SpaceAsset` (not a ship, station, weapon, or megastructure) and **not** a `SpaceInfrastructure` (not a specific TransportNode or Conduit — it's a *grouping* of TransportNode instances forming a connected network).

Lives outside the sealed hierarchies as its own top-level catalog entity. **First persisted catalog entity outside the sealed hierarchies.**

Fields (11 record components):
- `id`, `name`, `builderPolity`, `lifecycle`, `transponderName`, `description`, `notes`, `category`, `provenance`, `createdAt`, `modifiedAt`

Implements `Cataloged` for catalog uniformity with the following overrides (per §10 Divergence G resolution):
- `source()` → `provenance.sourceUniverse()` (D.6 Concern A pattern)
- `faction()` → `builderPolity` (no separate field; the builder is the faction)
- `concealed()` → `false` constant (networks aren't modeled as concealable)

### §5.2 GateNetworkLifecycle enum

Three values, distinction preserved permanently:

```java
public enum GateNetworkLifecycle {
    ACTIVE,         // Originally active, never derelict.
    DERELICT,       // Currently non-functional.
    REACTIVATED     // Was derelict; has been restored via transponder discovery.
}
```

### §5.3 Pipeline

Full sealed-hierarchy-style pipeline:
- `GateNetworkRepository` extends `JpaRepository<GateNetworkEntity, String>` with 4 query methods.
- `GateNetworkDesignerService` with `findAll`, `findById`, `save`, `delete`, `count`, `findByLifecycle`, `syncCatalogEntries` (D.8 contract), `findAllAsCataloged()` — returns `List<Cataloged>` (not `List<SpaceAsset>` since GateNetwork isn't a SpaceAsset).
- `GateNetworkCatalogSeeder` `@Component` + `@EventListener(ApplicationReadyEvent.class)`.
- `GateNetworkEntity` + `GateNetworkMapper` (simpler than `MegastructureDesignMapper` — no JSON LOBs since GateNetwork has no collection fields).

### §5.4 SpaceshipDesign extension

`SpaceshipDesign` gains a 20th record component: `Set<String> defaultAccessibleNetworkIds`.

Semantics: the set of GateNetwork ids that ships of this design have transponder access to by default. Per-instance overrides are future work. Compact-constructor defaults null to `Set.of()`; defensive copy via `Set.copyOf`.

A new 19-arg compatibility constructor matching the pre-E.1 canonical signature delegates to the 20-arg canonical with `Set.of()` for the new field. The 3 pre-existing compat constructors (16/17/18 args) auto-route through it. **Zero call-site ripple** across 11 existing callers.

Persisted via the existing `SpaceshipDesignMapper` with a new `defaultAccessibleNetworkIdsJson` CLOB column on `SpaceshipEntity`.

### §5.5 Catalog seeding

Phase E.1 populates **zero canonical GateNetwork constants**. The pipeline exists; no data flows through it yet. Phase E.2 populates canonical gate networks (Aldenata, Posleen, etc.) into the Catalog.

---

## §6 SolarSystemFeature extension

### §6.1 New columns

`SolarSystemFeature` gains four new nullable columns (added in V14):

- `parentBodyId` (String, nullable) — per-body parent reference. For per-star features (JUMP_POINT), this is the star's id. Resolves §10 Divergence C.
- `catalogReferenceId` (String, nullable) — catalog id of the canonical entry this feature represents.
- `catalogReferenceKind` (`CatalogedKind` enum, nullable) — discriminator for `catalogReferenceId`.
- `networkId` (String, nullable) — for JUMP_GATE features only, the GateNetwork id.

### §6.2 Polymorphic dispatch via CatalogedKind

**Correction from the pre-implementation design**: §6.2's polymorphic dispatch listed `TRANSPORT_NODE` as a case but `AssetKind` doesn't include it. Resolved per §10 Divergence B(β): introduced a new `CatalogedKind` enum with 5 values (`SHIP, STATION, WEAPON_INSTALLATION, MEGASTRUCTURE, TRANSPORT_NODE`), distinct from `AssetKind` which stays at 4 values focused on the SpaceAsset hierarchy.

Parallel-ordinal-stability: the four shared values keep matching ordinals between `AssetKind` and `CatalogedKind`. `TRANSPORT_NODE` is appended at ordinal 4. Pinned by `CatalogedKindTest.parallelOrdinalsMatchAssetKind`.

Resolving a `catalogReferenceId` requires the `catalogReferenceKind` to dispatch to the right repository — pattern lands in Phase E.2 when consumers need it.

`GateNetwork` is NOT in `CatalogedKind` — gate networks are referenced via `networkId` on JUMP_GATE features (separate field, separate dispatch path).

### §6.3 JUMP_POINT featureType

**Correction from the pre-implementation design**: §6.3 said "FeatureType enum gains a new value". Reality: `FeatureType` is a **String-constants inner class** of `SolarSystemFeature`, not an enum. The change is still one line — `public static final String JUMP_POINT = "JUMP_POINT";` — but the "enum" framing was inaccurate.

`isPointType()` updated to include JUMP_POINT so the renderer dispatches via `renderPointFeature` for it. Same update on `FeatureDescription.isPointType()` (the runtime DTO).

### §6.4 V14 migration

`V14__feature_catalog_ref_and_jump_point_and_default_networks.sql` adds (with `IF NOT EXISTS`):
- 4 `ALTER TABLE solar_system_feature ADD COLUMN` (parent_body_id, catalog_reference_id, catalog_reference_kind, network_id)
- 3 indexes (idx_solar_system_feature_parent_body, idx_solar_system_feature_catalog_reference [composite], idx_solar_system_feature_network)
- 1 `ALTER TABLE spaceship_design ADD COLUMN default_accessible_network_ids_json CLOB`

V13 (separate migration shipped with Step 3) created the `gate_network` table.

---

## §7 Activation hook

### §7.1 The event

```java
package com.teamgannon.trips.solarsystem;
public record SolarSystemActivatedEvent(SolarSystemDescription system) {}
```

### §7.2 The publish point

`SolarSystemSpacePane.render()` publishes the event immediately after `this.currentSystem = solarSystemDescription` at line 394, wrapped in try/catch for graceful failure. Matches the existing event-publish patterns in the same class (two pre-existing `eventPublisher.publishEvent(...)` calls).

### §7.3 The listener

`JumpPointActivationListener` `@Component` + `@EventListener` + `@Transactional`. Per-event: iterates primary + companion stars; per-star: existsById short-circuit via `findByParentBodyIdAndFeatureType`, then computes via `JumpPointComputer`, then persists. Graceful failure throughout.

Multi-star feature naming: single-star systems → "Jump Point"; multi-star → "{starName} Jump Point" for disambiguation.

### §7.4 Threading

The event is published synchronously on the JavaFX Application Thread. The listener does JPA writes only (no scene-graph mutation), so it doesn't need `FxThread.runOnFxThread`. JPA writes on the FX thread are acceptable at E.1's 1-3-stars-per-system scale; revisit for E.2's catalog-reference population work if it grows.

---

## §8 Renderer extension

### §8.1 JUMP_POINT visual treatment

`SolarSystemRenderer` extended with two dispatch arms for the new featureType:

| Property | JUMP_POINT | JUMP_GATE (for comparison) |
|---|---|---|
| Color | `Color.MEDIUMPURPLE` | `Color.CYAN` |
| Size multiplier | 1.5× | 2.0× |
| Glow | Yes (extended `isArtificial()` check to also include JUMP_POINT — naturally-occurring gravitational anomaly with energy signature) | Yes |

Test-seam refactor: the dispatch logic extracted to package-private statics `featureSizeMultiplier(String)` and `defaultFeatureColor(String)` so tests can verify dispatch without constructing a full `SolarSystemRenderer` (which needs ScaleManager + many other dependencies).

### §8.2 First-activation visibility trade-off

**Known limitation**: on first activation of a system, the JUMP_POINT feature is persisted by the listener but not visible — the `SolarSystemDescription.features` list was built by `SolarSystemService.populateFromExistingSystem` *before* the listener ran. Second and subsequent activations show the jump point correctly.

Acceptable trade-off for E.1's "structural foundation" scope. If first-activation visibility becomes required, the clean fix is to pre-fetch + sync within `SolarSystemService.getSolarSystem(...)` rather than after `render()` — but that's a phase-E.2-or-later concern.

---

## §9 Rejected alternatives (preserved verbatim from pre-implementation design)

- **§9.1** Physics-based jump-point computation (L1, geodesic search) — rejected because jump points represent fictional future-discovered physics; real-physics derivation gives unintended causal explanation.
- **§9.2** Single-network gate model — rejected; doesn't accommodate Aldenata vs. Posleen vs. future-human networks.
- **§9.3** Per-ship transponder access (no design-level inheritance) — rejected; design-level default + future per-instance override is cleaner.
- **§9.4** Eager precomputation of jump points for all 2.5M HYG stars — rejected; app-launch latency. Lazy-on-activation is correct.
- **§9.5** No clean-zone constraint — rejected; physics inconsistency with planets inside Hill spheres.
- **§9.6** Two-state lifecycle (ACTIVE/DERELICT only) — rejected; REACTIVATED preserves worldbuilding flavor.
- **§9.7** GateNetwork as sealed-hierarchy member — rejected; it's a grouping concept, not a concrete physical thing.
- **§9.8** DB-level FK constraint for catalogReferenceId — rejected; JPA can't FK across multiple tables; application-level integrity via polymorphic dispatch is the standard pattern.
- **§9.9** Asynchronous activation listener — rejected; deterministic-seed work is microseconds; synchronous is correct at E.1 scale.

---

## §10 Agent verification list (Step 1) — original record + outcomes

Reproduced verbatim with verification outcomes inline. Five items confirmed as designed; nine divergences flagged and resolved during implementation.

1. **DriveType enum's current shape.** ⚠ **Divergence A** — Already a typed enum carrying `Category category` + `DriveSpecs specs` constructor data, with 25 values (not "simple enum" as designed). Mapping scope adjusted: 25 values × thoughtful TransitMode-set assignment, not 1-2 values as initially implied. Resolved by canon-evidence-driven mapping with Larry confirming the 2 genuinely-ambiguous cases.

2. **Existing SolarSystemFeature columns.** ⚠ **Divergence C** — Confirmed 35+ existing fields. No `parentBodyId` column (the design assumed one existed). V14 adds it alongside the 3 other new columns.

3. **FeatureType enum's current values.** ⚠ **Divergence D** — `FeatureType` is a String-constants inner class, not an enum. 15 existing constants; JUMP_POINT added as a 16th constant string.

4. **SolarSystemFeatureRepository's query methods.** ⚠ **Divergence F** — `findByParentBodyIdAndFeatureType` doesn't exist; naturally resolved by Divergence C's column addition + new finder.

5. **AssetKind enum's current values.** ⚠ **Divergence B** — Confirmed 4 values (SHIP, STATION, WEAPON_INSTALLATION, MEGASTRUCTURE); no TRANSPORT_NODE. Resolved by creating new `CatalogedKind` enum (option β) with 5 values.

6. **SolarSystemSpacePane.render() shape.** ✅ Confirmed. Already `@Component`, already injects `ApplicationEventPublisher`, already publishes 2 other events. Line 394 convergence point confirmed.

7. **SpaceshipDesign record's current field count.** ✅ Confirmed 19 fields; addition is clean.

8. **Existing GateNetwork references.** ✅ Zero hits; confirmed new concept.

9. **Cataloged interface contract.** ⚠ **Divergence G** — Requires source/faction/concealed fields GateNetwork doesn't list. Resolved by mapping `faction() → builderPolity` and `concealed() → false`.

10. **Flyway high-water mark.** ✅ Confirmed V12 → V13/V14 are next available.

11. **JavaFX threading discipline.** ✅ Confirmed; FxThread helper available; existing `@EventListener` patterns in SolarSystemSpacePane use it for scene-graph mutations.

12. **Renderer's feature dispatch.** ✅ Confirmed; clean dispatch points at lines 810, 851, 924, 946 (only 924 + 946 actually needed touching for JUMP_POINT since it's a point feature, not a belt).

13. **Outer-system boundary computation.** ⚠ **Divergence H** — Prior art exists in `accrete` package (`SimStar.outermostPlanet()`), but tied to procedural-generation modelling layer. Resolved by reimplementing as self-contained util in `JumpPointComputer` (avoids cross-package coupling).

14. **Hill-sphere computation.** ⚠ **Divergence H** — Prior art at `Planet.java:329`. Resolved by reimplementing as self-contained `static double hillSphereRadius(mass, sma, parentMass)` in `JumpPointComputer`. Math is short; verified against known values (Earth ≈ 0.01 AU, Jupiter ≈ 0.35 AU).

---

## §11 Step structure — as built

| Step | Scope | Tests added |
|---|---|---|
| 1 | Read-only §10 verification report; 5 confirmed, 9 divergences flagged | 0 |
| 2 | `TransitMode` enum (5 values) + `DriveType` extension with locked 25-value mapping | +11 |
| 3 | GateNetwork pipeline: enum + record + entity + mapper + repo + service + seeder + V13 migration | +58 |
| 4 | V14 migration + 4 new SolarSystemFeature columns + JUMP_POINT constant + CatalogedKind enum + SpaceshipDesign.defaultAccessibleNetworkIds | +21 |
| 5 | `JumpPointComputer` with deterministic-seed algorithm + Hill-sphere + outer-boundary | +26 |
| 6 | `SolarSystemActivatedEvent` + `JumpPointActivationListener` + `SolarSystemSpacePane.render()` integration | +16 |
| 7 | `SolarSystemRenderer` extended for JUMP_POINT visualization (MEDIUMPURPLE, 1.5× size, glow) | +15 |
| 8 | Plan-doc entry + this retroactive design doc | 0 |

**Net test delta: +147 tests** (3,618 → 3,765).

---

## §12 Done definition — achieved

All 14 §10 verification items checked. 5 confirmed, 9 divergences resolved. `TransitMode` enum + `DriveType` extension live with full 25-value coverage. `GateNetwork` pipeline mirrors the sealed-hierarchy pattern. `SolarSystemFeature` gains 4 columns + JUMP_POINT featureType. `JumpPointComputer` implements the §3 algorithm with 26-test coverage including pathological-iteration-cap exhaustion. `SolarSystemActivatedEvent` published from `render()`; `JumpPointActivationListener` persists JUMP_POINT features per-star with graceful failure. Renderer draws JUMP_POINT features with MEDIUMPURPLE color, 1.5× size, glow. All existing tests pass without modification. `FlywayBaselineSmokeTest` confirms entity↔schema alignment after V13 and V14. `CatalogSyncIntegrationTest` from D.8 continues to pass.

---

## §13 After E.1 — Phase E.2 unblocked

The next phase is **E.2 — catalog-reference population**. With E.1's structure in place, E.2 does:

1. Populate canonical wormhole mouths (Aldenata wormhole network paired mouths, any other canonical wormholes).
2. Add canonical `GateNetwork` entries to the Catalog (Aldenata Civilian Network, Aldenata Military Network, Posleen network if applicable).
3. Place Troy as a Megastructure-typed `SolarSystemFeature` in Sol (`catalogReferenceId = "catalog-troy"`, `catalogReferenceKind = MEGASTRUCTURE`).
4. Place ISS as a Station-typed `SolarSystemFeature` in Sol (Earth-orbit).
5. Place SAPL elements as `WeaponInstallation`-typed `SolarSystemFeature`s in Sol (multi-element distributed across the defense perimeter).
6. Add new FTL drive types deferred from E.1 §4.4 (SHIFT_DRIVE, POSLEEN_INTERSTELLAR, ALDENATA_WORMHOLE_CAPABLE, etc.) with appropriate transit-mode sets.
7. Add the GRTUL_GATE deprecation path (legacy drive value retired in favour of explicit GateNetwork-based access modeling).

E.2 is data-population + new-drive work. E.3 (routing integration) builds on E.1's structure + E.2's populated data.

E.2 prerequisites from E.1 — all met:
- Catalog reaches the running app (D.8 sync-by-id contract) ✅
- `Megastructure` is a fully-wired catalog kind (D.7) ✅
- `GateNetwork` pipeline exists and is ready for canonical data ✅
- `SolarSystemFeature.catalogReferenceId` + `catalogReferenceKind` columns exist (Step 4) ✅
- `CatalogedKind` discriminator covers all 5 referenceable catalog kinds ✅
- `JUMP_POINT` features auto-populate on system activation (Steps 5–7) ✅

---

## Appendix — quick failure-mode review

- *Does the deterministic seed survive JVM restart?* Yes — `recordId.hashCode()` is stable per JVM-version (and is in practice stable across HotSpot versions for String hash code).
- *What if a star has no recordId?* Listener short-circuits (returns false from `ensureJumpPointForStar`). No feature created.
- *What if the JPA write fails?* Caught by the listener's outer try/catch, logged. User can still navigate the system; re-activation retries with the same coordinates.
- *What if the system has no planets and no stellar mass?* HYG fallback uses defaulted 1 solar mass → 40 AU outer boundary. Always produces a position.
- *What if every iteration is rejected?* Returns `Optional.empty()`. Listener treats as "star is jump-inaccessible"; no feature created; no error.
- *What if I add a new DriveType?* `DriveTypeTest.partitionMatches25` fails until the transit-mode set is declared. Regression guard.
- *What if I rename a FeatureType constant?* `SolarSystemFeatureExtensionTest.jumpPointConstantExistsWithExactValue` fails. Regression guard for the JUMP_POINT String value specifically.
- *What if the renderer's dispatch loses a feature type?* `SolarSystemRendererJumpPointTest` covers JUMP_POINT specifically; existing feature-type rendering is implicit via the existing test corpus.
