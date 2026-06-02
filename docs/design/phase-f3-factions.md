# Phase F.3 — Factions

**Status**: design, pre-implementation (revised 2026-06-02 with 4 ratified architectural decisions)
**Date**: 2026-06-02
**Parent**: [worldbuilding-platform-requirements.md](worldbuilding-platform-requirements.md) — R7.3
**Predecessors**: F.1 (Universe entity + activation + filtering chokepoint), F.2 (Alias + AliasTargetKind polymorphic targets + per-(universe, target) data model template)
**Successors**: F.4 (Eras), F.5 (Visual rules), F.6 (Population rules), F.7 (Tech constraints), F.8 (Events), F.9 (Economics), F.10 (Threats)

---

## §0 — Scope and orientation

### What F.3 delivers

F.3 introduces **Faction** as a universe-scoped first-class entity and **FactionAssignment** as the per-(universe, star) assignment record. Factions belong to universes (Federation belongs to Star Trek; Hkh'Rkh belongs to Caine Riordan). FactionAssignments attach factions to specific stars within their universe ("40 Eridani A is Federation territory in Star Trek").

Contested control is modeled within a single FactionAssignment: the primary faction owns the system; the contestedBy set lists other factions claiming or holding pieces of it (the "Romulan outpost on a Federation planet" case is captured as Federation primary + Romulan contesting).

Six concrete deliverables:

1. **Faction entity** in `com.terranrepublic.assets`, mirroring the Alias pipeline pattern from F.2.
2. **FactionAssignment entity** with STAR + EXOPLANET target kinds in the enum (UI creates STAR assignments only in F.3 per §1.2; EXOPLANET path dormant but data-model-supported, see ratified decision #4).
3. **V19/V20 migrations** creating both tables with appropriate FK constraints. (Entity ships with its migration per the F.2-pinned discipline.)
4. **CRUD UI** for factions and assignments — Worldbuilding → Factions... dialog (browse + create + edit + delete factions) and Worldbuilding → Faction Assignments... dialog (browse + create + edit + delete assignments). Inline assignment from the star info panel as a usability convenience.
5. **Unified faction coloring control** — refactor the legacy "Star Polities" toolbar button into a global faction-coloring master switch; per-view side-pane toggles become per-view participation flags within faction-coloring mode (ratified decision #3, path α). Off by default; renderer continues to use spectral classification when off.
6. **CatalogProvenance.sourceUniverse path-(ii) migration:** add a `factionId` field to CatalogProvenance referencing the new Faction entity; legacy sourceUniverse free-text stays as-is. Catalog entries whose sourceUniverse values are faction-named ("Hkh'Rkh", "Ktoran", etc.) gain proper Faction references; entries whose values are universe-named keep their existing universe linkage from F.1.

### What F.3 explicitly does NOT deliver

- **Modifications to StarObject.worldBuilding.polity** legacy scalar field. Stays as-is per the dual-system framing established in F.2 (parallel systems: legacy fields untouched; new entity-based system added alongside).
- **Modifications to ExoPlanet.polity** legacy scalar field. Same reasoning.
- **EXOPLANET-target UI flows.** F.3's FactionAssignmentTargetKind enum supports STAR + EXOPLANET (ratified decision #4 — ship both values to avoid future enum/entity/migration churn), but the Step 6/7 dialogs UI is STAR-only in F.3. EXOPLANET-target UI lands in a future phase without entity changes.
- **Search by faction** (e.g., "show all stars controlled by Hkh'Rkh"). Deferred.
- **Faction hierarchy / sub-factions** (e.g., "The Federation contains United Earth, Vulcan, Andoria as sub-factions"). F.3 has flat factions; hierarchical relationships deferred.
- **Time-based faction control** (which faction owned a star in 2200 vs 2400). F.4 Eras territory.
- **Faction relationship modeling** (allied / hostile / neutral between factions). Deferred.
- **ChView import auto-seeding FactionAssignments.** ChView import continues to set StarObject.polity from group numbers as today. Future polish could auto-seed Caine Riordan FactionAssignments based on those values; not in F.3 scope.
- **Migration of StarObject.polity legacy values to FactionAssignments.** "Hkh'Rkh" on a star without universe attribution is ambiguous (which universe is it in?); the legacy field stays as the legacy display.

### What F.3 is the foundation for

The Faction + FactionAssignment pattern establishes the **assignment entity model** for F.x phases. Where F.2 established the **single per-(universe, target) overlay** model (alias is text attached to target), F.3 establishes the **categorical assignment** model (target is assigned to a category, with category membership being the worldbuilding concept).

Subsequent phases follow the assignment pattern:

- F.4 Eras: EraAssignment(universe, target, eraId) — target is assigned to one or more eras
- F.6 Population: PopulationData attached per-(universe, target) — population numbers, demographic categories, colony status
- F.7 Tech: TechAssignment(universe, target, techLevel) — target is assigned to a tech level
- F.8 Events: events attached per-(universe, target) with time component (F.4 era integration)

Each phase introduces a new entity following F.3's shape: universe-scoped categorical entity (Faction, Era, etc.) + assignment entity that attaches it to targets per universe.

---

## §0.5 — Ratified architectural decisions (2026-06-02 pre-implementation)

Four architectural concerns surfaced during F.2 close-out review were ratified before Step 1 verification:

### Decision 1 — Cataloged interface gains separate `factionId()` accessor

The legacy `Cataloged.faction()` method returns a free-text descriptor (backwards compat with F.1's Universe.faction()=sourceAuthor and F.2's Alias.faction()=""). F.3 adds a separate `default String factionId() { return null; }` to the interface for entity-reference semantics. See §4.6 (no longer "Step 2 design call" — settled here).

### Decision 2 — Multi-universe tiebreaker via explicit `lastActivatedAt` on Universe

UniverseEntity gains a `lastActivatedAt: Instant` column. UniverseDesignerService.activate() updates it to Instant.now() on each activation. The tiebreaker in §1.5 / §6.4 / §7 uses this column — most-recently-activated wins. Decouples from modifiedAt (which updates on description edits too). See §1.5 + §4.7 for column addition details.

### Decision 3 — Star Polities toolbar button refactored to master switch (path α)

The legacy "Star Polities" toolbar button is refactored to mean "faction coloring globally on/off." Per-view side-pane checkboxes become per-view participation flags within faction-coloring mode. Single coherent control hierarchy; no competing color systems. See §1.5 + §3.3 + §6.

### Decision 4 — FactionAssignmentTargetKind ships STAR + EXOPLANET; UI is STAR-only

The enum carries both values from F.3 ship, the entity + migration accommodate both, but the F.3 UI dialogs create STAR-only. EXOPLANET path is dormant data-model-supported; future phase adds the EXOPLANET UI without entity/enum/migration churn. See §1.2 + §4.3.

These four decisions are baseline for Step 1 verification. The remaining smaller items (§4.4 storage choice, displayColor regex, mnemonic letters, Step 4 test estimate, V21 mapping table) are listed in §13 as Step 1 deliverables.

---

## §1 — Architectural decisions

### §1.1 — Faction as universe-scoped first-class entity

A Faction belongs to exactly one Universe. Cross-universe faction reuse (e.g., "Federation" appearing in both Star Trek and Star Trek: Discovery sub-universes) requires creating separate Faction entities per universe. The data model doesn't support shared factions; this is a deliberate simplification.

If cross-universe sharing becomes needed (e.g., for fan-fiction universes that build on canonical universes), future phases can introduce a Faction-clone or reference mechanism. F.3 ships the simple model.

```java
public record Faction(
    String id,                  // catalog-faction-<uuid>
    String universeId,          // NOT NULL FK to universe
    String name,                // unique within universe
    String displayColor,        // hex color string, validated ^#[0-9A-Fa-f]{6}$
    String description,         // optional
    Instant createdAt,
    Instant modifiedAt
) implements Cataloged {
    // unique on (universeId, name) enforced via DB constraint
    // displayColor regex validated in compact constructor (smaller item #2)
}
```

The `displayColor` is the faction's identifying color in the renderer when faction-coloring is enabled. Stored as hex string `#RRGGBB`; the compact constructor enforces the regex `^#[0-9A-Fa-f]{6}$` so copy-paste of non-hex strings ("blue", "rgb(0,100,200)") fails fast at record construction time rather than producing runtime renderer crashes. Default at creation time is `#808080` (neutral gray); users edit via JavaFX ColorPicker in the editor dialog to match worldbuilding canon (Federation blue, Klingon red, etc.).

### §1.2 — FactionAssignment per-(universe, star), STAR + EXOPLANET kinds in data model, STAR-only UI in F.3

A FactionAssignment carries:
- universeId (NOT NULL)
- targetKind (STAR or EXOPLANET per ratified decision #4)
- targetId (FK to StarObject for STAR; FK to ExoPlanet for EXOPLANET)
- primaryFactionId (NOT NULL FK to Faction)
- contestedByFactionIds (comma-separated String column for F.3 scale; see §4.4)
- description (optional context — "Romulan outpost on planet b creates contested status")

F.3's UI creates STAR-only assignments. EXOPLANET-target UI is deferred to a future phase. The data model accommodates both from V20 so future expansion is additive, not schema-modifying.

Unique constraint on `(universe_id, target_kind, target_id)` — one FactionAssignment per (kind, target) per universe. To change a star's faction, the user edits the existing assignment.

The primary/contestedBy asymmetry is a usability choice, not an ontological claim. The user picks one faction as primary (typically the official or dominant controller); contestedBy lists other claimants. Both are valid as data; the asymmetry just makes the renderer's job simpler (primary color, plus contested indicator).

### §1.3 — Contested control as Option B (single assignment with contestedBy set)

The Q5 decision picks Option B over Option A (multiple FactionAssignments per target). Reasons:

- Preserves uniqueness constraint pattern from F.2 (one per (universe, target))
- The renderer logic stays simple: primary color is the visual base; contested status adds a badge/icon
- contestedByFactionIds is metadata that's easy to query and display in tooltips and the info panel
- Migrations are simpler (single row per assignment, not multiple)
- Future enrichment is additive (a `claimStrength` field could be added per contestedBy faction; description field can carry text-form claim notes)

The trade-off: F.3 doesn't capture "three factions equally contest this system, no primary." If primary + contestedBy doesn't fit a worldbuilding case, the user picks one as primary; the asymmetry is a UX choice.

### §1.4 — Visual indicator for contested status: badge/icon (Q5a option 2)

The renderer's faction-coloring mode shows the primary faction's displayColor on the star sphere. Contested status is indicated by a small badge or icon overlaid on or adjacent to the sphere. Tooltip surfaces the full list (primary faction name + each contesting faction name).

Why badge/icon over stripes or border:
- Stripes are visually busy with the existing star plot (lines, labels, grid)
- Border on a small sphere doesn't read clearly at typical zoom levels
- Badge is a clear discrete element that doesn't compete with the sphere's color
- Tooltip provides the full information for users who want detail

The exact badge shape is a Step 4 implementation decision. A small triangle, exclamation mark, or asterisk overlay are all plausible.

### §1.5 — Unified faction-coloring control hierarchy (ratified decision #3, path α)

The legacy "Star Polities" toolbar button is refactored to mean **"enable faction coloring globally."** Per-view side-pane checkboxes become per-view **participation flags** within faction-coloring mode.

Single coherent control hierarchy:

| Toolbar button | View checkbox | Active universes? | Renderer behavior |
|---|---|---|---|
| OFF | (any) | (any) | Spectral classification (legacy default) |
| ON | OFF | (any) | Spectral classification (view opted out) |
| ON | ON | ≥1 with FactionAssignment for star | Primary-faction color; contested badge if applicable |
| ON | ON | None / no assignment | Step-1 audit determines fallback: (i) legacy polity-field coloring (backwards compat) or (ii) neutral gray ("no faction data"). Recommend (i) for now — preserves the user's existing polity-based color expectations when no F.3 universes are in play |

Multi-universe tiebreaker (when multiple active universes each have a FactionAssignment for the same star) uses `Universe.lastActivatedAt` per ratified decision #2 — most-recently-activated wins for the sphere color; tooltip lists all.

The interstellar view side pane gains a "Display & Controls" section (mirroring the solar system view's naming) with the participation checkbox. The solar system view's Display & Controls section gains the participation checkbox alongside its existing controls. Default off on both.

### §1.6 — CatalogProvenance.sourceUniverse path (ii) migration

CatalogProvenance currently has `sourceUniverse: String`. The F.1 Step 1 audit found ~10 files of reach with 59 distinct values across multiple semantic categories (universes, factions, eras, real entities).

F.1 handled the universe-named values. F.3 handles the faction-named values via additive change: a new `factionId: String` field on CatalogProvenance referencing the Faction entity. Catalog entries whose sourceUniverse is faction-named get their factionId populated by V21 data migration; sourceUniverse stays as legacy descriptor.

The migration is bounded by **Step 1 audit producing the explicit mapping table** (smaller item #5) — every sourceUniverse value that maps to a seeded faction is documented before V21 SQL is written. Catalog entries with sourceUniverse values like "Hkh'Rkh", "Ktoran", "Arakur", "Terran", etc. get mapped to the seeded Caine Riordan factions. Entries whose sourceUniverse doesn't match a known faction get factionId = null and stay as legacy descriptors only.

Future F.x phases can clean up the remaining sourceUniverse values (era-named, real-entity-named) similarly.

### §1.7 — Inline assignment from star info panel

For usability, the star info panel's Fictional Info tab gains a "Faction" section that shows:
- Current primary faction (if any) for the star in active universes
- Contested factions (if any)
- An "Edit Assignment..." button that opens the FactionAssignment editor for this specific star

This complements the standalone Worldbuilding → Faction Assignments... dialog. The standalone dialog is for browsing all assignments and bulk operations. The inline panel button is for the common "I'm looking at this star and want to assign or change its faction" flow.

---

## §2 — Glossary

Inherits from worldbuilding-platform-requirements.md §2.

**Faction**: A universe-scoped named entity representing a political, military, or organizational group. Stars are assigned to factions via FactionAssignment. Federation, Klingon Empire, Hkh'Rkh Consortium are all factions in their respective universes.

**FactionAssignment**: A per-(universe, target) record assigning a target to a primary faction with optional contesting factions. F.3 ships STAR + EXOPLANET kinds in the enum; F.3 UI handles STAR only.

**Contested system**: A star whose FactionAssignment has at least one contestedByFactionId. Visually indicated by a badge in the renderer when faction coloring is on.

**Faction coloring mode**: Master switch on the toolbar (refactored from legacy "Star Polities" button) that enables faction-based rendering across views. Per-view side-pane checkboxes are participation flags within this mode.

**Inherited polity**: For elements within a star system (planets, moons, stations), their polity is implicitly the star's primary faction. F.3 doesn't model overrides at the planet level via FactionAssignment UI; if a planet has a distinct polity, the star itself is marked contested. (EXOPLANET-kind assignments are data-model-supported per decision #4 but no UI in F.3.)

**lastActivatedAt**: Timestamp column on Universe entity (added by F.3) that records the most recent activation transition. Used as the multi-universe tiebreaker for renderer color and Fictional Info tab primary-display.

---

## §3 — Pre-design audit findings (Step 1 verification will refine)

### §3.1 — CatalogProvenance reach

Per F.1 Step 1 audit, CatalogProvenance.sourceUniverse has ~10 files of read/write reach. F.3 adds a new factionId field; Step 1 verifies that the existing CatalogProvenance constructor and serialization paths can accommodate the addition.

Compatibility constructor preserves call sites (per F.1's established discipline). Existing code reads factionId as null until V21 data migration populates it.

**Step 1 deliverable**: explicit mapping table from existing sourceUniverse values to F.3-seeded factions (smaller item #5).

### §3.2 — StarObject.worldBuilding.polity reach

Per F.2 Step 1 audit, polity is set by ChView import via fromChvRecord. Read paths: Edit Star dialog, Overview tab display, possibly the legacy "Star Polities" toolbar button for color-coding.

F.3 doesn't touch the field directly. The legacy "Star Polities" button is refactored per §3.3.

### §3.3 — "Star Polities" toolbar button (path α refactor)

Per ratified decision #3 (path α), the legacy button is refactored to mean "enable faction coloring globally." It becomes the master switch in the unified control hierarchy (§1.5).

**Step 1 verification**:
- Confirm the button's current behavior (what does it color today — StarObject.polity field?)
- Confirm what its OFF state does (return to spectral?)
- Identify the controller/handler so Step 4 can extend it
- For the "toolbar ON + no active universes" edge case: does the legacy polity-field coloring path still exist and work? If yes, decision #3 recommendation (i) is clean. If no, fall back to (ii).

### §3.4 — Interstellar view side pane structure

Current sections per screenshot: DataSets Available, Objects in View, Planetary Systems, Stellar Object Properties, Link Control, Star Routing.

F.3 adds: Display & Controls section (mirroring solar system view's naming) with faction-coloring participation checkbox.

Step 1 verifies whether section addition is clean or whether the existing pane uses a fixed accordion structure that needs explicit configuration.

### §3.5 — Solar system view side pane structure

Current sections per screenshot: System Overview, Planets & Moons, Selected Object, Reference Cues, Display & Controls.

F.3 adds: faction-coloring participation checkbox to the existing Display & Controls section. No new section needed.

Step 1 verifies the controller for Display & Controls allows additive checkboxes.

### §3.6 — Star info panel Fictional Info tab

Per F.2 work, the Fictional Info tab now has:
- Aliases section (F.2, at top after row 13)
- Legacy fields (Polity, World type, Fuel type, etc., rows 1-13)

F.3 inserts a new section between Aliases and Legacy fields (or appended after Aliases): "Faction (in active universes)" section with primary/contested display and Edit Assignment button.

Step 1 verifies the FXML supports another row group insertion (append-friendly per F.2's pattern; should be straightforward).

### §3.7 — Renderer integration

The interstellar view's StarRenderer handles per-star sphere coloring. F.3 needs StarRenderer to consult a "faction coloring on/off" flag and a faction-color-lookup service when on.

The solar system view's BodyRenderer (per F.2's Step 3 finding) handles planet/moon/star coloring in the system view. F.3 needs analogous integration.

Step 1 verifies the existing renderers can accept the additional service injection and flag without architectural surgery. (F.2's pattern was constructor injection for StarRenderer, setter injection for BodyRenderer matching its existing setContextMenuHandler pattern — F.3 follows the same.)

### §3.8 — Universe entity shape audit

**Step 1 verification** (new per ratified decision #2): does `Universe` / `UniverseEntity` carry a `lastActivatedAt`-equivalent field today? If not (likely), design the addition:
- New column on UniverseEntity (timestamp; nullable for legacy rows)
- Record component on Universe with backward-compat constructor
- UniverseDesignerService.activate() updates it on each call
- Migration: small V19 prequel or fold into V19 / V20 depending on ordering — Step 2 decides

### §3.9 — Menu mnemonic inventory

**Step 1 verification** (new per smaller item #3): grep existing menu FXML files for all `_X` mnemonics across all menus. Pick safe letters for "Factions..." and "Faction Assignments..." that don't collide with file-menu / edit-menu / etc. Alt+F is likely taken by File menu.

### §3.10 — ChView import polity-setting code

**Step 1 verification** (out-of-scope confirmation): identify where ChView import sets StarObject.polity. Document for the design doc's §0 "what F.3 does not deliver" list. No changes here, just confirmation that the code path remains untouched.

---

## §4 — Data model

### §4.1 — Faction record

```java
package com.terranrepublic.assets;

public record Faction(
    String id,
    String universeId,
    String name,
    String displayColor,
    String description,
    Instant createdAt,
    Instant modifiedAt
) implements Cataloged {
    private static final java.util.regex.Pattern HEX_COLOR =
            java.util.regex.Pattern.compile("^#[0-9A-Fa-f]{6}$");

    public Faction {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("id required");
        if (universeId == null || universeId.isBlank()) throw new IllegalArgumentException("universeId required");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name required");
        if (displayColor == null || displayColor.isBlank()) {
            displayColor = "#808080";  // neutral gray default
        } else if (!HEX_COLOR.matcher(displayColor).matches()) {
            throw new IllegalArgumentException("displayColor must be hex #RRGGBB: " + displayColor);
        }
        if (description == null) description = "";
        if (createdAt == null) createdAt = Instant.now();
        if (modifiedAt == null) modifiedAt = createdAt;
    }

    // Cataloged contract:
    @Override public String name() { return name; }
    @Override public String source() { return ""; }
    @Override public String faction() { return name; }       // entity IS the faction
    @Override public String factionId() { return id; }       // ratified decision #1
    @Override public boolean concealed() { return false; }
}
```

### §4.2 — FactionAssignment record

```java
package com.terranrepublic.assets;

public record FactionAssignment(
    String id,
    String universeId,
    FactionAssignmentTargetKind targetKind,
    String targetId,
    String primaryFactionId,
    java.util.Set<String> contestedByFactionIds,
    String description,
    Instant createdAt,
    Instant modifiedAt
) implements Cataloged {
    public FactionAssignment {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("id required");
        if (universeId == null || universeId.isBlank()) throw new IllegalArgumentException("universeId required");
        if (targetKind == null) throw new IllegalArgumentException("targetKind required");
        if (targetId == null || targetId.isBlank()) throw new IllegalArgumentException("targetId required");
        if (primaryFactionId == null || primaryFactionId.isBlank()) throw new IllegalArgumentException("primaryFactionId required");
        if (contestedByFactionIds == null) contestedByFactionIds = java.util.Set.of();
        if (description == null) description = "";
        if (createdAt == null) createdAt = Instant.now();
        if (modifiedAt == null) modifiedAt = createdAt;
    }

    public boolean isContested() { return !contestedByFactionIds.isEmpty(); }

    // Cataloged contract:
    @Override public String name() { return primaryFactionId; }   // assignment's "name" is the primary it carries
    @Override public String source() { return ""; }
    @Override public String faction() { return ""; }              // ratified decision #1: assignment isn't itself a faction
    @Override public String factionId() { return primaryFactionId; }  // ratified decision #1
    @Override public boolean concealed() { return false; }
}
```

### §4.3 — FactionAssignmentTargetKind enum (ratified decision #4)

```java
package com.terranrepublic.assets;

public enum FactionAssignmentTargetKind {
    STAR,
    EXOPLANET   // data-model-supported in F.3; UI is STAR-only
}
```

Two values from F.3 ship. The F.3 dialogs only create/edit STAR assignments, but the entity, the V20 schema, the mapper, the repository, and the service all accept either value without modification. Future EXOPLANET-target UI work is additive (a new dialog or a target-kind switch in FactionAssignmentEditorDialog) — no entity/enum/migration churn required.

### §4.4 — JPA entities

**FactionEntity:**

```java
@Entity(name = "FACTION")
@Table(
    indexes = {
        @Index(name = "idx_faction_universe", columnList = "universe_id"),
        @Index(name = "idx_faction_universe_name", columnList = "universe_id, name", unique = true)
    }
)
```

**FactionAssignmentEntity:**

```java
@Entity(name = "FACTION_ASSIGNMENT")
@Table(
    indexes = {
        @Index(name = "idx_factionassign_universe", columnList = "universe_id"),
        @Index(name = "idx_factionassign_target", columnList = "target_kind, target_id"),
        @Index(name = "idx_factionassign_universe_target",
               columnList = "universe_id, target_kind, target_id", unique = true),
        @Index(name = "idx_factionassign_primary", columnList = "primary_faction_id")
    }
)
```

**contestedByFactionIds storage (smaller item #1):** comma-separated `String` column (`VARCHAR(2000)` to fit ~30+ faction ids with overhead). For F.3 scale (typical contested = 0-3 factions per assignment), a join table via `@ElementCollection` is overkill — the per-row payload fits in the main table comfortably, and the renderer's per-star query stays single-row-fetch. If a future phase needs richer per-contesting-faction metadata (claim strength, claim type), it can promote to `@ElementCollection` or a dedicated entity.

Mapper splits/joins the column via `String.split(",")` / `String.join(",", set)`. Empty set serializes as empty string; mapper handles round-trip.

### §4.5 — V19, V20, V21 migrations

**V19** creates faction table. FK to universe(id) with ON DELETE CASCADE (factions belong to universes; deleting universe deletes its factions). Also adds `last_activated_at TIMESTAMP NULL` column to universe table (ratified decision #2) — backfills nulls for existing rows, populated going forward by UniverseDesignerService.activate().

Note: bundling the lastActivatedAt column into V19 (rather than a separate V19-prequel migration) keeps F.3's migration count to 3 (V19/V20/V21). Step 2 confirms ordering; if Faction needs to reference lastActivatedAt before V19 finishes (it doesn't), split.

**V20** creates faction_assignment table. FK to universe(id) with ON DELETE CASCADE. FK to faction(id) for primary_faction_id with ON DELETE RESTRICT (deleting a faction with active assignments should fail loudly rather than orphan assignments; user must delete or reassign first). contested_by_faction_ids stored as VARCHAR(2000) (comma-separated per §4.4 storage choice).

**V21** data migration:
- Seed factions for the two first-class universes via the Step-1-produced mapping table. Caine Riordan gets Hkh'Rkh, Ktoran, Arakur, Terran, and any other faction names found in existing CatalogProvenance.sourceUniverse values. Legacy of the Aldenata gets Posleen, and any other Legacy-faction names found.
- Add factionId column to catalog_provenance (or wherever CatalogProvenance is persisted; Step 1 verifies the column location across the 6 catalog tables — gate_network, megastructure, spaceship_design, station_design, transport_node, weapon_installation).
- UPDATE catalog entries' factionId where sourceUniverse matches a seeded faction's name.
- Leave sourceUniverse unchanged (legacy descriptor preserved).

### §4.6 — Cataloged interface adjustment (ratified decision #1)

Per ratified decision #1, the Cataloged interface gets a new default method:

```java
public interface Cataloged {
    // ... existing methods ...

    /**
     * v2 Phase F.3 — faction-entity reference. Distinct from {@link #faction()} which returns
     * free-text descriptor. Implementations referring to a Faction entity return its id; all
     * others return null. Default null preserves backward compatibility.
     */
    default String factionId() {
        return null;
    }
}
```

Existing implementations (Universe, Alias, GateNetwork, SpaceshipDesign, etc.) inherit the default null. New F.3 entities override:
- `Faction.factionId()` returns its own id (the entity IS a faction)
- `FactionAssignment.factionId()` returns primaryFactionId (the assignment references a faction)

Old `faction()` semantics preserved across the board:
- Universe.faction() = sourceAuthor (free-text)
- Alias.faction() = "" (aliases have no faction)
- Faction.faction() = name (free-text descriptor matching the entity's name)
- FactionAssignment.faction() = "" (assignment isn't itself a faction; consumers wanting resolved name look up via factionId)

This split keeps the interface contract clean: `faction()` is the human-readable descriptor (or empty when not applicable); `factionId()` is the entity reference (or null when no entity).

### §4.7 — Universe entity `lastActivatedAt` column (ratified decision #2)

UniverseEntity gains:

```java
@Column(name = "last_activated_at", nullable = true)
private Instant lastActivatedAt;
```

`Universe` record gains:

```java
public record Universe(
    // ... existing fields ...
    Instant lastActivatedAt   // nullable; null = never activated
) implements Cataloged {
    // backward-compat constructor that doesn't take lastActivatedAt:
    public Universe(String id, String name, ...) {
        this(id, name, ..., null);
    }
}
```

The record gets a new component + a backward-compat constructor. UniverseMapper round-trips the field (entity ↔ record).

UniverseDesignerService.activate(String id) updates `lastActivatedAt = Instant.now()` on each call (regardless of whether the universe was previously active — the timestamp tracks the most recent activation transition; even re-activating an already-active universe updates it).

UniverseDesignerService.deactivate(String id) leaves lastActivatedAt unchanged (the timestamp tracks "most recent activation"; deactivation doesn't reset it).

Multi-universe tiebreaker queries: `findActiveUniverses().stream().max(Comparator.comparing(Universe::lastActivatedAt, Comparator.nullsFirst(Comparator.naturalOrder())))`. Null-handling: never-activated universes (e.g., freshly imported) lose the tiebreaker; intentional — they're new arrivals.

---

## §5 — UniverseFilteringService integration

F.1's UniverseFilteringService provides "which universes are active?" F.3's FactionDesignerService and FactionAssignmentDesignerService consume this:

```java
public List<Faction> findActiveFactionsForUniverse(String universeId);
public List<FactionAssignment> findActiveAssignmentsForTarget(FactionAssignmentTargetKind kind, String targetId);
// Returns assignments whose universeId is in UniverseFilteringService.getActiveUniverseIds()
```

The renderer (when faction-coloring master switch is on AND view participation checkbox is on) calls `findActiveAssignmentsForTarget(STAR, starId)` per star to determine coloring. The tooltip / Fictional Info tab use the same call.

Broker subscription pattern from F.1 Step 6 applies — when UniverseActivationChangedEvent fires, the renderer (if coloring is on) refreshes; the Fictional Info tab refreshes its Faction section; dialogs refresh their lists.

UniverseFilteringService.getActiveUniverseIds() already exists per F.1; F.2 added getActiveUniverseNamesById(). F.3 may add `getMostRecentlyActivatedUniverseId()` (smaller helper) — uses lastActivatedAt per §4.7. Step 2 decides whether to add to the service or compute inline in consumers.

---

## §6 — Rendering integration

### §6.1 — Interstellar view: faction-colored stars (unified control hierarchy per §1.5)

When the toolbar master switch is ON **and** the interstellar Display & Controls "Color stars by faction" checkbox is ON:
- For each star with a FactionAssignment in an active universe, the sphere is colored by primaryFaction.displayColor.
- Multi-universe tiebreaker: most-recently-activated universe's assignment wins for sphere color (uses Universe.lastActivatedAt per §4.7).
- For stars without an assignment in any active universe: Step-1-determined fallback — recommend (i) legacy polity-field coloring (preserves user expectations when no F.3 data is in play); (ii) neutral gray if (i) is no longer cleanly available.
- Contested status (FactionAssignment.isContested()) adds a small badge to the star sphere.
- Spectral classification coloring is suppressed.

When toolbar OFF, or view checkbox OFF: spectral classification coloring (existing behavior preserved).

### §6.2 — Solar system view: faction-colored elements

When the toolbar master switch is ON **and** the solar system Display & Controls "Color elements by faction" checkbox is ON:
- The host star is colored per §6.1.
- Planets inherit the star's primary faction color (per §1.5; per-planet overrides via EXOPLANET-target UI not in F.3, though data-model-supported).
- Contested badge appears on the host star sphere.

When toolbar OFF, or view checkbox OFF: normal coloring (existing behavior preserved).

### §6.3 — Tooltip integration

Per F.2's pattern, the tooltip can carry faction information when relevant. When faction coloring is active for the view:

```
40 Eridani A
  Polity: Federation (Star Trek)
  Contested by: Romulan
  Vulcan (Star Trek)  ← if alias also exists
```

Multi-universe: tooltip lists all assignments (one line per universe). The sphere color picks the lastActivatedAt winner; the tooltip surfaces the full picture.

When faction coloring is off: tooltip retains current behavior (star name + polity + aliases).

Step 3 / Step 4 implementation refines.

### §6.4 — Multi-universe coloring tiebreaker (per ratified decision #2)

When multiple active universes have FactionAssignments for the same star:
- Sphere color: most-recently-activated universe's primary faction (via Universe.lastActivatedAt; "most recent" = max nullsFirst — null = never-activated, loses tie).
- Tooltip lists all (primary + universe attribution per assignment).
- Contested badge: shown if any active universe's assignment is contested.

Step 4 refines if the lastActivatedAt-based ordering produces unexpected UX edge cases (e.g., re-activating an already-active universe via the Universes dialog should bring it to the top — confirm this matches user intuition).

---

## §7 — Fictional Info tab integration

The Fictional Info tab structure post-F.3:

```
[Aliases section (F.2)]
  • Vulcan (Star Trek)
  ────────────────────────
[Faction section (F.3 NEW)]
  Primary: Federation (Star Trek)
  Contested by: Romulan, Klingon
  [Edit Assignment...]
  ────────────────────────
[Legacy fields]
  Polity: NA
  World type: NA
  ... etc.
```

The Faction section displays the assignment in the most-recently-activated universe (per §6.4 tiebreaker via Universe.lastActivatedAt). The "Edit Assignment..." button opens the FactionAssignmentEditorDialog for this star, pre-populated with the current assignment in the most-recently-activated universe.

If no FactionAssignment exists for this star in any active universe, the section shows: "No faction assignment in active universes." with a "Create Assignment..." button.

If multiple active universes have distinct assignments, the section shows the lastActivatedAt-winner with a note: "(2 more in other universes; click Edit to switch)." Clicking Edit opens the editor with the current winner pre-selected; the dialog lets the user switch which universe's assignment to view/edit.

Step 5 implementation refines.

---

## §8 — CRUD UI

### §8.1 — Worldbuilding menu (mnemonics TBD per Step 1 audit, smaller item #3)

Two new menu items. Final mnemonic letters chosen by Step 1 verification after grepping the existing menu FXML inventory:

```
Worldbuilding (_W)
├── Universes...           ← F.1 (Alt+U)
├── Aliases...             ← F.2 (Alt+A)
├── Factions...            ← F.3 NEW (mnemonic TBD)
├── Faction Assignments... ← F.3 NEW (mnemonic TBD)
├── (separator)
├── Ships...
... (etc.)
```

Step 1 deliverable: full menu mnemonic inventory + recommended letters for these two items.

### §8.2 — FactionsDialog (modeless Stage)

Mirrors F.2's AliasesDialog pattern:
- BorderPane with filter row (Universe dropdown, name search)
- TableView columns: Universe, Faction name, Color swatch, Description excerpt
- Create..., Edit..., Delete..., Close buttons
- Broker subscription for refresh on universe activation change
- Color swatch is a small colored rectangle showing the displayColor

### §8.3 — FactionEditorDialog (modal Dialog)

Mirrors F.2's AliasEditorDialog (single dialog for create + edit). Fields:
- Universe dropdown (required)
- Name field (required, unique within universe)
- Display color picker (JavaFX ColorPicker; produces clean #RRGGBB hex)
- Description (optional, multiline)
- Save / Cancel buttons with two-layer uniqueness UX (DB constraint + service pre-check)

### §8.4 — FactionAssignmentsDialog (modeless Stage)

Similar shape to FactionsDialog but for assignments:
- BorderPane with filter row (Universe dropdown, target name search, target-kind filter — even though UI creates STAR-only, the dialog browses both kinds since EXOPLANET assignments may arrive from future imports / future UI)
- TableView columns: Universe, Target kind, Target name, Primary faction, Contested by, Description excerpt
- Create..., Edit..., Delete..., Close buttons
- Broker subscription

### §8.5 — FactionAssignmentEditorDialog (modal Dialog)

Single dialog for create + edit. F.3 UI creates STAR-only:
- Universe dropdown (required, defaults to single active if exactly one — per F.2 Step 5's pattern)
- Target picker (STAR-only; reuses ControlsFX autocomplete pattern from F.2 Step 5; populated from StarObjectRepository.findAll() mapped by displayName)
- Primary faction dropdown (filtered to factions in selected universe via FactionDesignerService.findByUniverseId)
- Contested by: multi-select listing factions in selected universe (excluding primary)
- Description (optional, multiline)
- Save / Cancel buttons with uniqueness UX

The dialog can be opened in two modes:
- From Worldbuilding → Faction Assignments... → Create / Edit
- From Star info panel Fictional Info tab → Edit Assignment / Create Assignment (target locked to current star, target picker disabled)

### §8.6 — Inline "Edit Assignment..." from star info panel

Opens FactionAssignmentEditorDialog pre-populated with the current star's assignment (if any), with the target picker locked to the current star (setDisable(true)).

---

## §9 — Acceptance gate

F.3's §9 invariant tests mirror F.1 / F.2 patterns. @DataJpaTest with Flyway through V21 + Catalog seeders running.

### §9.1 — R5.5: Universes don't leak

Activate only Universe A. Create Faction and FactionAssignment in A. Verify B's factions and assignments are not visible. Vice versa.

### §9.2 — R5.6: Real catalog unaffected

Across all universe-activation combinations, verify star displayName, polity field, etc. remain unchanged. F.3 adds no overlays on real fields.

### §9.3 — R5.7: Faction display scoped to active universes

Create FactionAssignment in A. Activate A → faction visible in tooltip + Fictional Info tab + (if coloring on) sphere color. Deactivate A → faction hidden.

### §9.4 — ON DELETE CASCADE (universe → factions, universe → assignments)

Create factions + assignments in A. Delete Universe A. Verify both cascade.

### §9.5 — ON DELETE RESTRICT (faction → assignments)

Create FactionAssignment referencing Faction X. Attempt to delete Faction X. Verify deletion fails with clear error message (user must reassign or delete assignments first).

### §9.6 — Uniqueness constraints

- Two factions with same name in same universe → rejected
- Two FactionAssignments for same (universe, targetKind, targetId) → rejected

### §9.7 — Contested status

FactionAssignment with non-empty contestedByFactionIds → isContested() returns true. Tooltip and Fictional Info section display contested list. (Renderer visual verification deferred to manual UI check.)

### §9.8 — Faction coloring control hierarchy

- Toolbar OFF → renderer uses spectral classification regardless of view checkbox state or active universes.
- Toolbar ON + view checkbox OFF → spectral classification.
- Toolbar ON + view checkbox ON + active universe with FactionAssignment → primary faction color.
- Toolbar ON + view checkbox ON + no active universe → Step-1-determined fallback (legacy polity-field or neutral gray).

### §9.9 — Multi-universe tiebreaker via lastActivatedAt

Create FactionAssignments for the same star in three universes. Activate them in known order (A then B then C). Verify the sphere color (or Fictional Info Faction section primary) reflects C's assignment. Re-activate A → A becomes most-recent → renderer / panel switch to A's assignment.

### §9.10 — `factionId()` interface accessor

Verify the Cataloged.factionId() default returns null for all non-F.3 implementations (Universe, Alias, GateNetwork, etc.). Verify Faction.factionId() returns its own id. Verify FactionAssignment.factionId() returns primaryFactionId. Verify the legacy faction() returns the documented free-text for each implementation.

---

## §10 — Step breakdown

F.3 ships in **8 implementation steps + Step 1 verification = 9 numbered steps**. Larger than F.2 (which was 6 steps); F.3 has two entities, three migrations (including the universe `lastActivatedAt` column), two CRUD dialogs, two renderers to integrate, the side-pane toggles, and the toolbar refactor.

| Step | Subject | Net new tests (est.) |
|---|---|---|
| 1 | Verification + audit (existing renderer state, side pane FXML, CatalogProvenance reach, Star Polities button behavior, Universe.lastActivatedAt verification, menu mnemonic inventory, sourceUniverse → faction mapping table) | 0 |
| 2 | Faction entity pipeline + V19 migration (including Universe.lastActivatedAt column addition) — per F.2-pinned discipline: entity ships with migration | ~30 |
| 3 | FactionAssignment entity pipeline + V20 migration + V21 data migration for CatalogProvenance.sourceUniverse → factionId | ~40 |
| 4 | Renderer integration — interstellar view StarRenderer + solar system view BodyRenderer faction coloring + contested badge + toolbar master switch refactor + multi-universe tiebreaker | ~30-35 (revised per F.2-close-out review smaller item #4) |
| 5 | Fictional Info tab Faction section + side pane Display & Controls participation toggles (interstellar view side pane gains new Display & Controls section) | ~20 |
| 6 | FactionsDialog + FactionEditorDialog | ~25 |
| 7 | FactionAssignmentsDialog + FactionAssignmentEditorDialog + inline Edit Assignment from Fictional Info tab | ~25 |
| 8 | Close-out + §9 invariant tests (10 invariants per §9.1-§9.10) + retroactive design doc note + plan doc rollup | ~15 |

Total est. **~185-190 net new tests** across 8 implementation steps + verification.

This is larger than F.2 (~136 tests). The growth comes from: the second entity (Faction + FactionAssignment vs F.2's single Alias), the renderer integration work (color logic, contested badge, multi-universe tiebreaker, toolbar refactor), and the additional §9.8 / §9.9 / §9.10 invariants (control hierarchy + lastActivatedAt + factionId interface).

---

## §11 — Out of scope

Per §0:
- StarObject.worldBuilding.polity migration (stays as legacy scalar)
- ExoPlanet.polity migration (stays as legacy scalar)
- EXOPLANET-target UI flows (data-model-supported per ratified decision #4; UI deferred)
- Search by faction
- Faction hierarchies / sub-factions
- Time-based faction control (F.4 Eras territory)
- Faction relationship modeling (allied/hostile/neutral)
- ChView import auto-seeding FactionAssignments
- Migration of remaining sourceUniverse non-faction values (era-named, real-entity-named) — future F.x

The "Star Polities" toolbar button is **refactored** (not coexisting) per ratified decision #3 path α — listed in §1.5 / §3.3 as a positive deliverable, no longer in the deferred list.

---

## §12 — Success criteria

F.3 is complete when:

1. Users can create Factions via Worldbuilding → Factions... menu and the dialog with display color picker.
2. Users can create FactionAssignments via Worldbuilding → Faction Assignments... menu or inline from star info panel (STAR targets only in F.3).
3. The star info panel's Fictional Info tab shows the Faction section with primary/contested display for the selected star in active universes; multi-universe tiebreaker via Universe.lastActivatedAt.
4. The toolbar master switch + per-view side-pane participation flags form a single coherent control hierarchy per §1.5.
5. With master switch + view checkbox ON and active universe with FactionAssignment, stars in the 3D plot are colored by primary faction.
6. Contested systems show a badge in the renderer when faction coloring is on.
7. Tooltips show faction information when faction coloring is on (with universe attribution; multi-universe lists all).
8. CatalogProvenance.factionId is populated for catalog entries whose sourceUniverse value matched a seeded faction (per Step 1's mapping table).
9. Deleting a faction with active assignments fails with clear error (ON DELETE RESTRICT).
10. Deleting a universe cascades to its factions and assignments.
11. UniverseDesignerService.activate() updates Universe.lastActivatedAt on every call; lastActivatedAt drives the multi-universe tiebreaker.
12. Cataloged.factionId() default returns null; Faction.factionId() and FactionAssignment.factionId() override per §4.6.
13. F.1 and F.2 invariants stay green throughout (UniverseFilteringInvariantsTest, AliasFilteringInvariantsTest).

---

## §13 — Step 1 verification deliverables (refined per pre-implementation review)

Step 1 produces a single audit report covering:

1. **CatalogProvenance reach + sourceUniverse value inventory + explicit faction-mapping table.** Lists every distinct sourceUniverse value across all 6 catalog tables; marks which are faction-named (mapping target) vs universe-named vs other. The mapping table is V21's input.
2. **StarObject.worldBuilding.polity read paths.** Documents who reads the field; confirms F.3 leaves it untouched.
3. **"Star Polities" toolbar button current behavior.** What does it color today? What's its handler location? Confirms path α refactor scope (Step 4). For the "toolbar ON + no active universes" edge case: documents whether legacy polity-field coloring works cleanly; recommends (i) or (ii) for §1.5.
4. **Side pane FXML structure (both views).**
   - Interstellar view: confirms whether adding a new "Display & Controls" section is clean (existing accordion / VBox / etc.).
   - Solar system view: confirms the existing Display & Controls section is append-friendly.
5. **Star info panel Fictional Info tab FXML.** Confirms insertion point for new Faction section (between Aliases and legacy fields, or appended after Aliases).
6. **Renderer integration points.**
   - StarRenderer (interstellar): per-star coloring pipeline today; how to inject FactionAssignmentDesignerService + toolbar/checkbox flags.
   - BodyRenderer (solar system): analogous.
7. **Menu mnemonic inventory.** Greps `_X` across all menu FXML files in `controller/menubar/`. Recommends safe mnemonic letters for "Factions..." and "Faction Assignments...".
8. **Universe entity current shape.** Confirms whether `lastActivatedAt` or analogous exists today (almost certainly does not). Designs the addition per §4.7 (entity column + record component + backward-compat constructor + UniverseDesignerService.activate() update + mapper round-trip).
9. **ChView import polity-setting code.** Confirms location; documents for §0 "out of scope" verification. No changes required, just confirmation.
10. **CatalogProvenance persistence layout.** Confirms which catalog tables carry CatalogProvenance fields and whether factionId can be added as a single new column per table (likely yes; F.1 V16 added universe_id per table similarly).
11. **F.1/F.2 invariants baseline.** Re-runs UniverseFilteringInvariantsTest + AliasFilteringInvariantsTest; confirms green before F.3 work begins. (Just smoke; the suite should be 4,116 from F.2 close-out.)

Step 1 produces a single markdown report appended to this design doc as §15 (or a separate audit file referenced from §15). Doc revisions for any divergences ratified before Step 2 begins, matching F.1/F.2 discipline.

---

## §14 — Forward links

| Phase | Adds | F.3 dependency |
|---|---|---|
| F.4 | Era entity + EraAssignment | Uses F.3's assignment pattern; eras tag stars per universe, plus add time component |
| F.5 | Visual rules | May extend F.3's faction-coloring with rule-based styling overrides |
| F.6 | Population rules | Per-(universe, target) population data; assignment pattern reuse |
| F.7 | Tech constraints | Per-(universe, target) tech levels; assignment pattern reuse |
| F.8 | Events | Time-bound assignments combining F.3 + F.4 |
| F.9 | Economics | Resource production tied to factions + populations |
| F.10 | Threats | Per-faction threat profiles, conflict modeling |

F.3 is the second template-establishing phase (F.2 was the first). F.4 onward compose against both.

---

## §15 — Naming conventions

- **Faction IDs**: `catalog-faction-<uuid>`
- **FactionAssignment IDs**: `catalog-factionassign-<uuid>`
- **FactionAssignmentTargetKind values**: `STAR`, `EXOPLANET` (both ship in F.3; UI is STAR-only)
- **Service classes**: `FactionDesignerService`, `FactionAssignmentDesignerService` in `com.teamgannon.trips.spaceshipmodeller.service`
- **Test classes**: FactionTest, FactionAssignmentTest, FactionEntityTest, FactionAssignmentEntityTest, FactionMapperTest, FactionAssignmentMapperTest, FactionDesignerServiceTest, FactionAssignmentDesignerServiceTest, FactionsDialogTest, FactionEditorDialogTest, FactionAssignmentsDialogTest, FactionAssignmentEditorDialogTest, FactionRendererIntegrationTest, UniverseLastActivatedAtTest (new), CatalogedFactionIdInterfaceTest (new), FactionFilteringInvariantsTest

---

*End of Phase F.3 design doc. Awaiting Larry's ratification before Step 1 (verification + audit) begins. The four §0.5 ratified decisions + smaller items in §13 are baseline scope.*
