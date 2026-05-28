# Constructs Feature — Reconciliation Pre-Work

**Status**: pre-work for the implementing agent; must run *before* any code from the feature plan
**Owner**: implementing agent
**Date authored**: 2026-05-28

The companion document, [`constructs-feature-plan.md`](./constructs-feature-plan.md), proposes a new `Construct` sealed hierarchy. **That plan was authored without first surveying two existing hierarchies in this repo that cover most of the same concept space.** Before you touch any code, your job is to reconcile the plan with what's already there.

This is the standing reconciliation order. Do not skip it.

---

## 1. What you must inventory first

Read every file in these three packages and understand what each one models. Don't skim:

- `tripsapplication/src/main/java/com/terranrepublic/assets/` — `SpaceAsset` sealed interface + `Cataloged` seam + `SpaceshipDesign`, `StationDesign`, `WeaponInstallation`, plus the enums (`AssetKind`, `StationType`, `Emplacement`, `Mobility`, `OperationalState`, `TechLevel`, `WeaponType`, `InstallationType`) and the populated `Catalog`.
- `tripsapplication/src/main/java/com/terranrepublic/infrastructure/` — `SpaceInfrastructure` hierarchy with `TransportNode` (whose `NodeType` enum already enumerates `RING_GATE`, `JUMP_POINT`, `WORMHOLE_MOUTH`, `PORTAL`, `BEANSTALK_ANCHOR`, `RELAY`, `BEACON`, `NAV_HAZARD`) and `Conduit`, plus the `GraphRegistry`.
- `tripsapplication/src/main/java/com/terranrepublic/economy/` and `.../sim/` — what's downstream of the asset/infrastructure types (`ResourceDeposit` join, `IndustrialOperation`, `Stockpile`, `SupplyRoute`, `TickEngine`, `WorldState`). You need to know what would break if a sealed-hierarchy rename happens.

Also confirm the Jackson polymorphism wiring on `SpaceAsset` (`@JsonTypeInfo(property = "kind")`) and how it's currently persisted.

When you've finished reading, write a short inventory note in `docs/design/constructs-existing-hierarchies.md` (one new file) summarising what each existing type already does. That note is the artifact that proves you did the survey.

---

## 2. The structural conflict you must resolve

The feature plan and the existing code overlap as follows. Verify each row against the current source tree, then decide whether each row's plan entity is a **rename**, an **extension**, or a **genuine new type** of the existing entity:

| Feature-plan entity | Existing entity | First-glance verdict |
|---|---|---|
| `Construct` sealed interface | `SpaceAsset` sealed interface (assets) + `SpaceInfrastructure` sealed interface (infra) | **rename / consolidate** — the plan's `Construct` is `SpaceAsset` ∪ `SpaceInfrastructure` |
| `Spaceship` record | `SpaceshipDesign` record | **rename** — same concept, different name |
| `Starbase` / `BattleMoon` / `MiningStation` | `StationDesign` record (with `StationType` enum) | **extension** of `StationDesign` via its enum, not new types |
| `PlanetaryDefenceInstallation` | `WeaponInstallation` record (with `Emplacement.GROUND_FIXED`) | **rename / minor extension** — same record, possibly extend `Emplacement` |
| `JumpGate` record | `TransportNode(type=RING_GATE)` already exists, with partner links via `connectedNodeIds` | **extension** — jump gates are already modelled; what's missing is the UI for them, not the data |
| `ConstructIdentity` | `Cataloged` interface | **rename / extension** — `Cataloged` is the existing identity seam |
| `Mobility` enum (FIXED / MOBILE_LIMITED / MOBILE) | `Mobility` enum already exists in `assets` | **already there** |
| `category` discriminator | `AssetKind` discriminator on `SpaceAsset` + `InfrastructureKind` on `SpaceInfrastructure` | **already there** (two of them — see §3) |
| `construct` table with discriminator + JSON | (existing JPA + Jackson `@JsonTypeInfo` setup) | **already wired** — check what the actual persistence shape is |

You do not get to skip a row. If you find a row whose verdict above is wrong, write down why in the inventory note and propose the correct verdict.

---

## 3. The split between `SpaceAsset` and `SpaceInfrastructure`

`SpaceAsset` covers things that are *units* (ships, stations, weapon emplacements) — they're owned, they have a `dryMassTons`, an `OperationalState`, `Armament`s.

`SpaceInfrastructure` covers things that are *network* (transport nodes, conduits) — they have `connectedNodeIds`, `throughputTonsPerTick`, position in a graph.

Some of the Constructs the original feature plan proposes belong to one and some to the other. Jump gates are infrastructure (already modelled as `TransportNode`). Starbases are assets (already modelled as `StationDesign`).

Your reconciled design must decide:

- **a.** Keep the split: `SpaceAsset` and `SpaceInfrastructure` stay as two parallel sealed hierarchies; the Installations Designer is a UI over *both* of them, presented as a unified concept to the user; the type system stays honest about the distinction. **(Recommended.)**
- **b.** Collapse them into one `Construct` hierarchy. Then justify in the design why doubling-up the `kind()` discriminator and the persistence shape was worth it.

If you pick (a), the original plan's `Construct` term becomes the *UI* term — the user sees "Constructs" but the code keeps both `SpaceAsset` and `SpaceInfrastructure`. The Designer panel queries both via a `ConstructRegistry` or `ConstructService` aggregating call.

---

## 4. The naming reconciliation

The Spaceship Designer panel today lives in `com.teamgannon.trips.spaceshipmodeller.*` and persists a `SpaceshipEntity` that is **its own thing** — separate from the catalog `SpaceshipDesign` record in `com.terranrepublic.assets`. There may be a duplication there too. Survey:

- Is `com.teamgannon.trips.spaceshipmodeller.entity.SpaceshipEntity` the *same data* as `com.terranrepublic.assets.SpaceshipDesign`? Check the mapper.
- If yes, that's a third duplication this reconciliation needs to fold. The Spaceship Designer either (a) starts reading and writing through the unified `SpaceAsset` model, or (b) the existing entity stays but the catalog uses it.
- If no — they model different things and the duplication is intentional — say so explicitly in your inventory note.

Either way: do not introduce a *third* parallel model. Reconciliation means the count goes down, not up.

---

## 5. The Issue 46 / LazyInitializationException constraint

The feature plan proposes single-table persistence with `payload_json` as a CLOB. The repo's Phase 7.8 work (Issue 46 in `trips-full-codebase-review-2026.md`) found that `@Basic(fetch = LAZY)` on `@Lob` fields blew up `LazyInitializationException` on ~30 cross-tx readers and the L2 cache (Issue 53, EhCache 3.10 via JCache) was the chosen alternative.

If your reconciled design uses a CLOB `payload_json` column, you must:

- enumerate every UI / export / dialog / renderer path that will deserialize that JSON
- confirm those paths are inside a transactional boundary OR that the L2 cache covers them
- write the analysis into the design before any JPA code is touched

If you can't satisfy that constraint, switch to one of:
- **per-subtype tables** (joined inheritance) — each subtype has its own columns
- **separate `construct_payload` table** with explicit fetch in service-layer methods

This is non-negotiable. The L2 cache work in Issue 53 is documented in `trips-full-codebase-review-2026.md`; read it.

---

## 6. The transfer planner menu lesson

The feature plan does not mention this, but it's relevant context: the global "Transfer Planner..." menu item was just removed because it was only useful in a Solar System context. The same rule applies to anything you add to the menu bar for Constructs. The two Designer panels are fine as menu items (they're self-contained and don't require a system context). Per-subtype "design and immediately deploy" actions are not — those belong in-context where the deployment target lives.

---

## 7. Your deliverable: a revised plan

When you finish the inventory and the reconciliation, produce a new file at `docs/design/constructs-feature-plan-v2.md`. It supersedes the v1 plan. It must:

1. Open with a "**Reconciliation log**" section listing every row from §2 above, your verdict per row, and the rationale.
2. Either rename + extend the existing hierarchies (Option (a) from the original plan's §10 — the recommended path), or explicitly justify a parallel hierarchy.
3. Address the CLOB / LazyInitializationException constraint from §5 above.
4. Keep the parts of v1 that were good: two parallel UIs, phased rollout, jump-gate-first because of routing leverage, terminology ("Construct" as the UI umbrella, "Installations" for the non-spacecraft designer pending product owner confirmation).
5. Drop the parts that conflicted with existing code.
6. Re-validate the open questions section against what the existing types already decide for you (e.g. `Mobility` is already defined; don't ask the product owner to re-decide its granularity unless the existing one is wrong).
7. End with a "What v1 got wrong" section, one or two sentences per failure, so a future reader can see the failure mode. Don't sandbag it; just be honest about what the v1 agent didn't survey.

Once v2 is in the tree, mark v1 with a banner at the top:

> **SUPERSEDED**: see `constructs-feature-plan-v2.md`. This document did not account for `com.terranrepublic.assets.SpaceAsset` and `com.terranrepublic.infrastructure.SpaceInfrastructure` and proposed a parallel hierarchy. Retained for history.

---

## 8. What to do if the v1 plan turns out to be correct after all

It's possible — though I think unlikely — that after the inventory you conclude `Construct` *should* be a parallel hierarchy. If so, your v2 plan must contain an explicit "Why a parallel hierarchy is correct here" section answering at minimum:

- Why doesn't extending `SpaceAsset` work?
- Why doesn't extending `SpaceInfrastructure` work?
- How will the two hierarchies stay in sync over time?
- What's the migration / deprecation timeline for the existing types?

If you can't answer those four, you're picking the wrong option.

---

## 9. Hand-off note for the product owner

This pre-work doesn't change anything user-visible. It's strictly an architectural reconciliation step before any feature code lands. When v2 is written and reviewed, the product owner should re-confirm the naming decisions ("Installations" for the non-spacecraft UI, Caine Riordan pin in the Spaceship Designer tab strip, etc.) before Phase A of v2 starts.

The product owner should also see the inventory note from §1 so they're informed which existing types are about to be unified vs renamed vs left alone. That note is the artifact that proves the v2 plan was grounded in the real codebase.
