# Constructs Feature — Design Plan

**Status**: design, not yet implemented
**Owner**: Larry Mitchell (intent), implementing-agent (build)
**Date**: 2026-05-28

This document captures the agreed shape of the upcoming **Constructs** feature so an implementing agent (or future you) can pick it up cleanly. It is the source of truth for terminology and architectural decisions; the implementer should consult this before writing code and update it as decisions evolve.

---

## 1. The problem

Today TRIPS has a `SpaceshipDesign` model + `SpaceshipDesigner` UI. The product owner wants to model a much broader range of engineered things in space:

- Spacecraft (existing — mobile, drive-powered, crewed/uncrewed)
- **Jump gates** — fixed network endpoints; partner-pair links; create routing edges
- **Starbases** — large fixed (or slowly-moving) habitats / military bases
- **Planetary defence installations** — fixed weapon emplacements on planet surface or in orbit
- **Asteroid mining stations** — fixed; attached to an asteroid; extraction profile
- **Battle moons / Death Stars** — very large; fixed *or* mobile; armed
- ... and "many other things" (Dyson swarms, generation ships, space elevators, communication relays, navigation buoys, …)

Spacecraft alone isn't broad enough to cover this. Stuffing it all into `SpaceshipDesign` would dilute the model. A separate, parallel feature is wanted.

---

## 2. Terminology decision

### "Construct" — the umbrella concept (interface / parent type)

Everything modelled by this feature, including spacecraft, is a **Construct**. This is the engineering / domain term.

### Spacecraft — one Construct subtype (existing UI)

The existing `SpaceshipDesigner` panel handles all Construct subtypes that are *primarily mobile and drive-powered*. We keep its existing UI and data path; only its underlying model joins the Construct hierarchy.

### Non-spacecraft Constructs — the new UI

For the new UI we need a better label than "non-spacecraft". Options considered:

| Term | Pros | Cons |
|---|---|---|
| **Installations** | Sci-fi standard; covers fixed + military + civilian; widely used in Mass Effect / Star Trek / Halo | Slightly implies "fixed" — but battle moons / generation ships *can* move |
| **Megastructures** | Captures very large scale (battle moons, Dyson swarms) | Implies *very* large — feels wrong for a single navigation buoy |
| **Stations & Megastructures** | Descriptive | Long; awkward in a menu |
| **Strategic Assets** | Catches everything | Military-game vibe; alienates worldbuilders |
| **Facilities** | Generic | Bland; doesn't read as sci-fi |
| **Platforms** | OK for fixed | Doesn't cover mobile battle moons |

**Recommendation: "Installations"** as the UI term, with the Javadoc clarifying that the type also covers mobile-but-not-primarily-drive-powered Constructs (battle moon, generation ship). The menu reads "Installations Designer..."; the panel is `InstallationDesignerPanel`. *The implementing agent should confirm with the product owner before committing the name — if rejected, "Megastructures" or "Stations" are the next fallbacks.*

---

## 3. Goals & non-goals

### Goals

- A `Construct` sealed hierarchy that unifies Spacecraft + Installation subtypes, with shared provenance / identity / mass fields.
- Persistence path that handles all subtypes uniformly (one table or one-per-subtype — see §5).
- **Two parallel UIs**:
  - existing **Spacecraft Designer** — only shows `Spaceship` Constructs
  - new **Installations Designer** — only shows non-`Spaceship` Constructs
  - both share the tab-strip + filter + search + detail-pane skeleton (lift to a base panel)
- First non-spacecraft subtype shipped end-to-end: **Jump Gate** (highest leverage — also unlocks the routing-alternatives ask).
- Subsequent subtypes can be added one at a time with minimal scaffolding.

### Non-goals (initial scope)

- 3D rendering of installations in the system view — placeholder only for now.
- A "ConstructDesignerPanel" that shows everything in one table — explicitly *two* parallel panels by user request.
- Migration of historical data — there isn't any non-spacecraft data yet.
- Editing battle / combat stats. The first cut is identity + size + provenance + (for jump gates) the partner link.

---

## 4. Domain model

### 4.1 Shared identity

Extract a record carrying the fields every Construct has, regardless of subtype:

```java
public record ConstructIdentity(
        String id,                  // UUID
        String name,                // "Rocinante", "Babylon 5", "Sol-Tau Ceti Gate"
        String designation,         // "MCRN-CR-101", "B5", "JG-001"
        ConstructCategory category, // SPACECRAFT, JUMP_GATE, STARBASE, …
        SourceType sourceType,      // REAL / PROPOSED / SCIENCE_FICTION / UNKNOWN
        String sourceUniverse,
        String faction,
        String era,
        String description,
        String iconPath,
        Instant createdAt,
        Mobility mobility           // FIXED, MOBILE_LIMITED, MOBILE
) {}
```

Notes:
- `category` is the discriminator that drives which UI the Construct shows up in (Spacecraft vs Installations).
- `mobility` lives on Identity so the UI can show / filter on it.
- `SourceType` + `sourceUniverse` + `faction` + `era` already exist; reuse them.

### 4.2 The sealed hierarchy

```java
public sealed interface Construct
        permits Spaceship,
                JumpGate,
                Starbase,
                PlanetaryDefenceInstallation,
                MiningStation,
                BattleMoon,
                OtherConstruct {

    ConstructIdentity identity();
    MassBudget massBudget();       // reuse existing record
    int crewComplement();          // 0 for uncrewed
}
```

Each subtype is a record that adds its own fields:

```java
public record Spaceship(
        ConstructIdentity identity,
        MassBudget massBudget,
        int crewComplement,
        ShipClass shipClass,
        DriveType driveType,
        double lengthMeters,
        List<CarriedCraft> carriedCraft) implements Construct {}

public record JumpGate(
        ConstructIdentity identity,
        MassBudget massBudget,
        int crewComplement,
        String partnerGateId,       // FK to another JumpGate (or null = unpaired)
        double maxThroughputTonnesPerHour,
        double traversalDurationSeconds,
        double minSafeDistanceLy)   // closest a gate must be to a star
        implements Construct {}

public record Starbase(
        ConstructIdentity identity,
        MassBudget massBudget,
        int crewComplement,
        long population,
        double diameterMeters,
        String orbitAroundBodyId)   // FK to a SolarSystem body — null = deep space
        implements Construct {}

// … PlanetaryDefenceInstallation, MiningStation, BattleMoon follow the same shape
```

`OtherConstruct` is a catch-all so users can add a Construct without a dedicated subtype yet:
```java
public record OtherConstruct(
        ConstructIdentity identity,
        MassBudget massBudget,
        int crewComplement,
        Map<String, String> customFields) implements Construct {}
```

### 4.3 The `Spaceship` migration

`SpaceshipDesign` (the existing record under `com.terranrepublic.assets`) becomes `Spaceship implements Construct` and moves into `com.teamgannon.trips.construct.spaceship`. The existing fields slide into the new shape:

| Existing `SpaceshipDesign` field | New home |
|---|---|
| `id, name, designation, sourceType, sourceUniverse, faction, era, iconPath, description, createdAt` | `ConstructIdentity` |
| `shipClass, driveType, lengthMeters, carriedCraft` | `Spaceship` |
| `massBudget, crewComplement` | `Construct` interface |

A type alias / deprecated typedef in the old package smooths the transition for anything still importing `com.terranrepublic.assets.SpaceshipDesign`. The implementing agent should **grep for every importer of that path** before the rename and update them in the same commit.

---

## 5. Persistence

### Approach: single table with discriminator + per-subtype JSON

Rationale: one repository, one query for "all Constructs", easy filtering by category, easy to add new subtypes without schema changes. Trade-off: subtype-specific fields aren't queryable as SQL columns, but we don't need to query on them in the near term.

```sql
CREATE TABLE construct (
    id              VARCHAR(36) PRIMARY KEY,
    category        VARCHAR(40) NOT NULL,    -- discriminator
    name            VARCHAR(255) NOT NULL,
    designation     VARCHAR(64),
    source_type     VARCHAR(40),
    source_universe VARCHAR(255),
    faction         VARCHAR(255),
    era             VARCHAR(255),
    mobility        VARCHAR(20),
    description     CLOB,
    icon_path       VARCHAR(512),
    mass_total_t    DOUBLE,
    crew_complement INTEGER,
    payload_json    CLOB NOT NULL,           -- subtype-specific fields
    created_at      TIMESTAMP NOT NULL
);

CREATE INDEX construct_category_idx ON construct(category);
CREATE INDEX construct_universe_idx ON construct(source_universe);
```

`payload_json` carries the subtype-specific record fields (e.g. `JumpGate.partnerGateId`). A `ConstructMapper` handles the polymorphic deserialise based on `category`.

Flyway migration: **V6** (`V6__construct_table.sql`). The existing `spaceship_design` table either:
- a) stays for one release as a read-only fallback, with a one-shot migration that copies rows into `construct` with `category='SPACECRAFT'` — recommended.
- b) is dropped and only `construct` is used — riskier; do this once the new path has soaked.

### Migration is one-shot, not bidirectional

Spaceship rows move into `construct` on app boot if the source table still has rows and the destination doesn't. Idempotent; safe to re-run.

---

## 6. UI

### Two parallel panels

| Existing | New |
|---|---|
| `SpaceshipDesignerPanel` (Spaceship subtype only) | `InstallationDesignerPanel` (every non-Spaceship subtype) |
| Menu: `Design → Spaceship Modeller…` | Menu: `Design → Installations Designer…` (subject to naming confirmation) |

### Shared base panel

Both panels share enough that a base class is worth extracting:

```java
public abstract class ConstructDesignerPanel<C extends Construct> extends BorderPane {
    // tab strip (universe + Real/Proposed), filter combos, search field,
    // CRUD buttons, table → detail layout
    protected abstract Set<ConstructCategory> allowedCategories();
    protected abstract TableView<ConstructRow> buildTable();
    protected abstract Node buildDetail(C selected);
    protected abstract Dialog<C> buildEditDialog(C existing);
}
```

`SpaceshipDesignerPanel` becomes `extends ConstructDesignerPanel<Spaceship>` with `allowedCategories() = Set.of(SPACECRAFT)`.

`InstallationDesignerPanel` becomes `extends ConstructDesignerPanel<Construct>` with `allowedCategories() = Set.copyOf(EnumSet.complementOf(EnumSet.of(SPACECRAFT)))`.

### Detail / edit dialogs are subtype-specific

A `Spaceship` edit dialog is the existing `SpaceshipEditorDialog`. New dialog per Construct subtype: `JumpGateEditorDialog`, `StarbaseEditorDialog`, …

In the Installations Designer, the "New…" button opens a small picker first ("New Jump Gate / New Starbase / …") then routes to the right subtype-specific dialog.

---

## 7. Routing integration (Jump Gates → routing edges)

Jump gates and the **routing-alternatives** discussion are tied together; first-class jump gate support is the highest-value reason to ship this feature now.

### 7.1 The model

A `JumpGate` Construct has a `partnerGateId`. Two gates pointing at each other form a bidirectional edge. The edge's weight is `traversalDurationSeconds` (not light-years) so a route mixing transit-edges and gate-edges can be compared on time, not distance.

### 7.2 The service

New `JumpGateNetworkService`:
- queries all `JumpGate` Constructs whose `partnerGateId` resolves to another existing `JumpGate`
- exposes `List<JumpGateEdge> edges(String dataSetName)` (each edge = a partner pair)
- caches the result; invalidates on Construct save / delete

### 7.3 The route finder

`RouteFinderInView` + `RouteFinderDataset` gain an optional `JumpGateNetworkService`. When provided:
- the graph adds an additional edge per gate pair, with weight = traversal time (converted to a units-comparable cost)
- a "route via gate" path is preferred when its total time < the multi-hop warp transit time
- the route display labels each segment as transit-edge or gate-edge so the user sees which gates were used

### 7.4 Visualisation

Jump gates render in the interstellar view as a small icon (square / lens / something distinguishable from a star) at their (x, y, z) coordinates. A line between paired gates visually shows the network. Defer until end of the routing integration pass; the data model can ship first.

---

## 8. Suggested phases for the implementing agent

Each phase is a self-contained commit set. Run the full test suite after each.

### Phase A — sealed hierarchy + persistence (no UI changes)
1. Introduce `Construct`, `ConstructIdentity`, `ConstructCategory`, `Mobility` types (no subtypes yet).
2. Migrate `SpaceshipDesign` → `Spaceship implements Construct`. Update every importer. Keep behaviour identical; existing tests must still pass.
3. Flyway V6: `construct` table with discriminator + JSON payload. Copy existing `spaceship_design` rows into `construct` on boot.
4. Repository / Service swap: `ConstructRepository` + `ConstructService`. `SpaceshipService` becomes a thin facade that filters to `SPACECRAFT` category. Old `SpaceshipService.findAll()` etc. keep working from the outside.
5. Smoke-test: existing `SpaceshipDesignerPanel` shows the same designs as before; nothing regresses.

### Phase B — Jump Gate subtype + edit dialog
1. Add `JumpGate` record + `JumpGateEditorDialog`.
2. Add a "JumpGate" template entry to the template library (one or two examples for testing).
3. Wire it into `ConstructService` save / load.

### Phase C — Installations Designer panel (Jump Gate is the only subtype shown initially)
1. Extract shared base `ConstructDesignerPanel<C>`. Refactor `SpaceshipDesignerPanel` to extend it.
2. Build `InstallationDesignerPanel extends ConstructDesignerPanel<Construct>`.
3. Add menu item `Design → Installations Designer…` (after confirming the name).
4. The "New…" button opens a subtype picker, which currently has one entry (Jump Gate).

### Phase D — Jump-gate routing integration
1. `JumpGateNetworkService` reads paired gates.
2. Route finder integration: optional dependency, additional edges weighted by traversal time.
3. Route display labels gate segments.

### Phase E — Additional Construct subtypes
1. Add `Starbase`, `PlanetaryDefenceInstallation`, `MiningStation`, `BattleMoon` records + editor dialogs.
2. Each adds itself to the "New…" picker in the Installations Designer.
3. Add seed templates per subtype (a few canonical examples — Babylon 5, Death Star, a Mass Effect citadel, etc.).

### Phase F — Polish + visualisation
1. Jump gates render in the interstellar view.
2. Linked gates show their partner connection as a faint line.
3. Asteroid mining stations rendered at their attached body in the solar system view.

---

## 9. Open questions for the product owner

1. **Name confirmation for the non-spacecraft UI**: "Installations" preferred — confirm or pick a different label.
2. **Mobility taxonomy**: is `FIXED / MOBILE_LIMITED / MOBILE` the right granularity, or do we need more (e.g. `FIXED, ORBITAL, MOBILE_SLOW, MOBILE_FAST`)?
3. **Jump-gate construction cost** — model as an in-universe stat (build time, material requirements) or out of scope for first cut? Recommend out of scope.
4. **One-shot migration or keep `spaceship_design` table** for one release? Recommend one-shot — the table has no other consumers.
5. **Pictures / models**: do we need icon assets for the Installations Designer table view, or text-only is fine at first? Recommend text-only first; add icons in Phase F.
6. **Initial seed Constructs** for the template library — which battle moons / starbases / jump gates / mining stations are canonical enough to ship as templates?

---

## 10. Things explicitly outside this design

- Combat mechanics. We model what a thing *is*, not how it *fights*.
- Construction project planning ("you start building a Starbase at T=0, it's done at T+5y"). Could be a separate feature on top.
- Economy / trade routes via stations. Same.
- Full 3D models per Construct. Icons + simple primitives only.

---

## 11. Related code paths the implementer should read first

- `tripsapplication/src/main/java/com/teamgannon/trips/spaceshipmodeller/` — existing Spaceship modeller; entire structure including UI, service, repo, templates
- `tripsapplication/src/main/java/com/terranrepublic/assets/SpaceshipDesign.java` — the existing record that becomes `Spaceship`
- `tripsapplication/src/main/java/com/teamgannon/trips/routing/` — route finders that will be extended in Phase D
- `tripsapplication/src/main/resources/com/teamgannon/trips/controller/menubar/DesignMenu.fxml` — menu wiring
- `tripsapplication/src/main/resources/db/migration/` — Flyway migration directory; next migration is V6
- The existing `SolarSystemFactory` + `SolarSystemFactoryRegistry` (under `service/factories/`) — same polymorphic-via-interface pattern that worked well for Sol-vs-procedural. Reuse the shape if it fits naturally.

---

## 12. Done definition

This feature is "complete" when:

- Both Designer panels exist and are independently usable.
- All Construct subtypes from §4.2 can be created, edited, saved, loaded, and listed.
- Jump-gate-paired routing is offered as an alternative to warp transit when total travel time is shorter via gates.
- Persistence is single-table; no `spaceship_design` table remains in production.
- Existing tests still pass; new tests cover the migration, the `ConstructService` polymorphism, and at least one route-finder integration scenario.

When the implementing agent reaches that point, mark this document as **implemented** at the top and add a "Lessons learned" section at the bottom.
