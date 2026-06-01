# Phase F.2 — Aliases

**Status**: design, pre-implementation
**Date**: 2026-06-01
**Parent**: [worldbuilding-platform-requirements.md](worldbuilding-platform-requirements.md) — R7.2
**Predecessors**: Phase F.1 (Universe entity + activation + filtering chokepoint)
**Successors**: F.3 (Factions full structure), F.4 (Eras), F.5–F.10

---

## §0 — Scope and orientation

### What F.2 delivers

F.2 introduces **Alias** as a first-class universe-scoped entity. An Alias is a worldbuilding overlay — a fictional name attached to a real astronomical target (a HYG star or an ExoPlanet catalog row) within the context of a specific Universe. When the Universe is active, the alias surfaces in the renderer (tooltip) and the star info panel (Fictional Info tab). When the Universe is deactivated, the alias is hidden.

Five concrete deliverables:

1. **Alias entity** in the `com.terranrepublic.assets` package, mirroring the GateNetwork/Universe pipeline pattern (record + JPA entity + mapper + repository + service).
2. **V18 migration** creating the `alias` table with FK to `universe(id)` (NOT NULL, ON DELETE CASCADE) plus polymorphic target reference.
3. **Renderer tooltip integration** — stars and exoplanets in the 3D plot show their universe-scoped aliases on hover when the relevant Universe is active.
4. **Fictional Info tab integration** — a new Aliases section at the top of the existing Fictional Info tab, above the legacy worldbuilding fields.
5. **Create Alias UI** — a new dialog under Worldbuilding → Aliases for browsing existing aliases and creating new ones.

### What F.2 explicitly does NOT deliver

- **Modifications to the existing `StarObject.aliasList`** (Set<String> ElementCollection from earlier development). That field serves a different purpose: catalog identifier variants from Simbad/Bayer/HIP/etc. catalogs. It's universe-agnostic and stays unchanged. F.2's Aliases are a parallel, universe-scoped system.
- **Modifications to `StarWorldBuilding` fields** (polity, worldType, fuelType, techType, etc.). These per-star scalar worldbuilding fields predate F.1. They stay as legacy fields until F.3 (Factions) migrates polity, F.7 (Tech) migrates techType, etc. F.2 sits the new Aliases section alongside them in the Fictional Info tab without touching them.
- **Modifications to `ExoPlanet.alternateNames` or `ExoPlanet.polity` etc.** Same reasoning as above.
- **Search by alias** (e.g., "search for Vulcan" finds 40 Eridani A when Trek is active). Search integration deferred to a later phase.
- **Catalog-reference contextual display** (e.g., a station orbiting "Vulcan" showing the universe-scoped name in the catalog browser). Deferred.

### What F.2 is the foundation for

The Alias entity establishes the per-(universe, target) data model pattern that F.3/F.4/F.6/F.7 will follow. Each subsequent phase introduces a similar universe-scoped overlay on stars or exoplanets:

- F.3 Faction-assigns stars (per-(universe, star) faction reference)
- F.4 Era-tags stars and exoplanets (per-(universe, target) era membership)
- F.6 Population/colonization records (per-(universe, exoplanet) population data)
- F.7 Tech-level overrides (per-(universe, target) tech constraints)

F.2 ships the architectural template. The Aliases section in the Fictional Info tab becomes the first of several universe-scoped sections that future F.x phases will add alongside it.

---

## §1 — Architectural decisions

### §1.1 — Two parallel alias systems by design (Framing A)

The existing `StarObject.aliasList: Set<String>` ElementCollection serves the **catalog identifier variants** purpose — Simbad's "NAME Proxima Centauri", Bayer's "α Centauri", HIP's "HIP 70890", and so on. These are real-world astronomical naming conventions, universe-agnostic, populated by catalog import (Simbad ingestion, ChView import, etc.). The screenshot labels like "NAME Proxima Ce..." come from this list.

F.2's Alias entity serves the **worldbuilding overlay** purpose — Star Trek's "Vulcan" attached to 40 Eridani A, Larry's Children of the Pattern attaching "Akane's homeworld" to Tau Ceti, and so on. These are fictional names, universe-scoped, populated by user editing through the new Create Alias UI.

The two systems coexist and don't interact. The existing aliasList continues to behave as it does today. The new Alias entity adds a separate query path. The renderer's universe-scoped tooltip (new) is distinct from the renderer's label rendering (which uses aliasList today and continues to do so).

This is the same shape as F.1's handling of `CatalogProvenance.sourceUniverse` — preserve existing data structures with their existing semantics; add new universe-scoped entities for the new semantics; don't try to unify the two.

### §1.2 — Polymorphic target reference

An Alias targets either a Star (StarObject row in HYG-imported data) or an ExoPlanet (catalog row, either REAL provenance or PROMOTED-from-ACRETE). The target_kind discriminator + target_id polymorphic reference captures this:

```java
public record Alias(
    String id,                   // catalog-alias-<uuid>
    String universeId,           // NOT NULL FK to universe
    AliasTargetKind targetKind,  // STAR | EXOPLANET
    String targetId,             // FK to STAR_OBJ.id or EXOPLANET.id, per kind
    String aliasText,            // the fictional name
    String description,          // optional worldbuilding context
    Instant createdAt,
    Instant modifiedAt
) {}
```

Two columns or a polymorphic reference would each work. The two-column approach (`targetKind` + `targetId`) is the chosen pattern because:
- It mirrors `SolarSystemFeature.catalogReferenceKind` + `catalogReferenceId` from Phase E.1, where the same polymorphic-discriminator pattern landed
- JPA has clean support: no @MappedSuperclass complexity, no @JoinColumn polymorphism — just two scalar columns
- The CatalogedKind enum's pattern (5 values for catalog discrimination) is the precedent; AliasTargetKind is a smaller version (2 values, room to grow)

### §1.3 — Independent aliases per target

A universe can alias the star without aliasing the planet (or vice versa). The Star Trek universe might have:
- Alias A: Star "40 Eridani A" (StarObject id) → "Vulcan" (the system, colloquially)
- Alias B: ExoPlanet "40 Eridani A b" → "Vulcan" (the planet, precisely)

These are two separate Alias rows. The user creates each independently via the Create Alias UI. The data model treats them as unrelated (no automatic coordination between star and exoplanet aliases — even if they happen to share the alias text).

This avoids forced coordination that would constrain worldbuilding. A user might want to alias just the star ("the system is called Junction in Caine Riordan") without committing to a specific planet alias.

### §1.4 — ON DELETE CASCADE for universe relationship

Unlike F.1's catalog entries (which use `ON DELETE SET NULL` because they can exist canonically without a universe), F.2's aliases require a universe — an alias without a universe is semantically meaningless. The FK constraint is NOT NULL, ON DELETE CASCADE.

This means deleting a Universe deletes all its aliases. The user is warned of this in the universe deletion confirmation (per F.1's pattern). Aliases are universe-intrinsic data, not catalog-shared data.

### §1.5 — Multiple aliases per (universe, target) — disallowed

For F.2, **one alias per (universe, target) pair is enforced** via unique constraint on `(universe_id, target_kind, target_id)`. The Star Trek universe can't have both "Vulcan" and "T'Khut" as separate Alias rows for the same star — that would be confusing in the UI ("which alias represents the star?").

If a user wants multiple names for the same star within the same universe, they put them in the `aliasText` field as separated text ("Vulcan / T'Khut"). Display logic doesn't parse them; it's a single alias string.

This constraint is a deliberate simplification. F.2 doesn't try to model "primary vs secondary alias" or "formal vs colloquial." If that becomes a worldbuilding need later, the data model can be extended.

### §1.6 — Universe scope is required, not implicit

When creating an Alias, the user must explicitly select a Universe. F.2 doesn't infer "the currently-active universe" because that's ambiguous when multiple universes are active. The Create Alias dialog has a Universe dropdown that the user picks from.

Default behavior: if exactly one universe is currently active, the dropdown defaults to it (convenience). Otherwise the user picks.

---

## §2 — Glossary

Inherits the worldbuilding-platform-requirements doc's §2.

**Alias** (F.2): A universe-scoped fictional name attached to a real astronomical target. Distinct from the legacy `StarObject.aliasList` Set<String> which holds catalog identifier variants.

**Alias target**: The StarObject or ExoPlanet that an Alias points at. The target's identity is unaffected by the Alias's existence; the alias is purely an overlay.

**AliasTargetKind**: Enum discriminator with two values: STAR (the targetId is a StarObject id) or EXOPLANET (the targetId is an ExoPlanet id).

**Catalog identifier vs fictional name**: Catalog identifiers (Simbad NAME, Bayer designations, HIP numbers) live in `StarObject.aliasList` and are universe-agnostic. Fictional names (Vulcan, Trantor) live in the F.2 Alias table and are universe-scoped.

---

## §3 — Pre-design audit findings

### §3.1 — StarObject reach

`StarObject.aliasList` is a `Set<String>` ElementCollection. Read paths to verify in Step 1: the star label rendering code (StarPlotManager or similar), the Overview tab, possibly the Edit Star dialog. None of these are touched by F.2 — the existing system stays as-is.

`StarObject.worldBuilding` embedded record carries `polity`, `worldType`, `fuelType`, `portType`, `populationType`, `techType`, `productType`, `milSpaceType`, `milPlanType`, plus `other` and `anomaly` booleans. All universe-agnostic per-star scalars. F.2 doesn't touch any of these. They continue to populate from the Edit Star dialog and ChView import as today.

### §3.2 — ExoPlanet reach

`ExoPlanet.alternateNames` is a free-text String field. Universe-agnostic. F.2 doesn't touch it.

`ExoPlanet` has extensive sci-fi fields (population, techLevel, colonized, colonizationYear, polity, strategicImportance, primaryResource). All universe-agnostic. F.2 doesn't touch them.

### §3.3 — Universe FK reach

F.2 introduces a new table with NOT NULL FK to universe(id). The universe table from F.1 already exists; F.2 just adds a new referencing table.

### §3.4 — Star info panel structure

The Side Pane's "Stellar Object Properties" expansible section has three tabs:
- Overview — canonical scientific data (name, common name, constellation, spectral class, distance, mass, metallicity, age)
- Fictional Info — `StarWorldBuilding` fields (polity, worldType, fuelType, techType, etc.)
- Other Info — deeper astronomical data (Simbad ID, galactic coords, RA/Dec, magnitudes, etc.)

F.2 extends the Fictional Info tab with a new Aliases section at the top, above the existing fields. Step 1 verification of the implementation will inventory the FXML for the Fictional Info tab and confirm whether the section addition is a clean append or requires layout restructure.

### §3.5 — Renderer tooltip behavior

The current renderer (3D star plot) shows labels for stars but the tooltip-on-hover behavior is implementation-specific. Step 1 verification will inventory:
- Is there an existing tooltip mechanism on star nodes? If so, where is it set?
- How does the renderer access universe-active state? (Probably via UniverseFilteringService from F.1.)
- Does the renderer have a per-frame refresh that would let it dynamically include aliases based on activation, or does it construct tooltips once at render time?

If a tooltip mechanism exists, F.2 extends it. If not, F.2 introduces one for the universe-aliased case.

---

## §4 — Data model

### §4.1 — Alias record

```java
package com.terranrepublic.assets;

public record Alias(
        String id,                       // catalog-alias-<slug or uuid>
        String universeId,               // NOT NULL FK to universe
        AliasTargetKind targetKind,      // STAR or EXOPLANET
        String targetId,                 // StarObject.id or ExoPlanet.id per kind
        String aliasText,                // the fictional name; "Vulcan", "Trantor"
        String description,              // optional context; max 1000 chars
        Instant createdAt,
        Instant modifiedAt
) implements Cataloged {
    public Alias {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("id required");
        if (universeId == null || universeId.isBlank()) throw new IllegalArgumentException("universeId required");
        if (targetKind == null) throw new IllegalArgumentException("targetKind required");
        if (targetId == null || targetId.isBlank()) throw new IllegalArgumentException("targetId required");
        if (aliasText == null || aliasText.isBlank()) throw new IllegalArgumentException("aliasText required");
        if (description == null) description = "";
        if (createdAt == null) createdAt = Instant.now();
        if (modifiedAt == null) modifiedAt = createdAt;
    }

    @Override public String universeId() { return universeId; }
    @Override public String faction() { return ""; }      // aliases don't have factions
    @Override public boolean concealed() { return false; } // aliases are always visible to their universe
    @Override public String source() { return ""; }
}
```

The record implements `Cataloged` to gain the universe-scoping default behavior. It overrides `universeId()` (returns the required value, never null for an Alias). Other Cataloged interface methods get reasonable defaults — aliases don't have factions, aren't concealed, and don't have a source field (their "source" is the universe they belong to).

### §4.2 — AliasTargetKind enum

```java
package com.terranrepublic.assets;

public enum AliasTargetKind {
    STAR,      // targetId references StarObject.id
    EXOPLANET  // targetId references ExoPlanet.id
}
```

Two values. Future expansion possible (moons, asteroid belts, etc.) but not in F.2.

### §4.3 — AliasEntity JPA entity

```java
@Entity(name = "ALIAS")
@Table(indexes = {
    @Index(name = "idx_alias_universe", columnList = "universe_id"),
    @Index(name = "idx_alias_target", columnList = "target_kind, target_id"),
    @Index(name = "idx_alias_universe_target", columnList = "universe_id, target_kind, target_id", unique = true)
})
@DynamicUpdate
public class AliasEntity {
    @Id
    private String id;
    
    @Column(nullable = false)
    private String universeId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AliasTargetKind targetKind;
    
    @Column(nullable = false)
    private String targetId;
    
    @Column(nullable = false)
    private String aliasText;
    
    @Column(length = 1000)
    private String description;
    
    private Instant createdAt;
    private Instant modifiedAt;
}
```

Note: the unique composite index on `(universe_id, target_kind, target_id)` enforces §1.5 (one alias per (universe, target) pair).

### §4.4 — V18 migration

File: `V18__alias_table.sql`

```sql
CREATE TABLE alias (
    id VARCHAR(64) PRIMARY KEY,
    universe_id VARCHAR(64) NOT NULL,
    target_kind VARCHAR(32) NOT NULL,
    target_id VARCHAR(64) NOT NULL,
    alias_text VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    created_at TIMESTAMP,
    modified_at TIMESTAMP,
    CONSTRAINT fk_alias_universe FOREIGN KEY (universe_id) 
        REFERENCES universe(id) ON DELETE CASCADE,
    CONSTRAINT uk_alias_universe_target 
        UNIQUE (universe_id, target_kind, target_id)
);

CREATE INDEX idx_alias_universe ON alias (universe_id);
CREATE INDEX idx_alias_target ON alias (target_kind, target_id);
```

No data seeded. Empty table at F.2 ship. Users create aliases via the Create Alias UI.

### §4.5 — AliasMapper, AliasRepository, AliasService

Standard pipeline mirroring UniverseDesignerService from F.1:

- `AliasMapper` — bidirectional, no JSON LOB
- `AliasRepository extends JpaRepository<AliasEntity, String>` with finders:
  - `List<AliasEntity> findByUniverseId(String universeId)`
  - `List<AliasEntity> findByTargetKindAndTargetId(AliasTargetKind kind, String targetId)`
  - `List<AliasEntity> findByUniverseIdAndTargetKindAndTargetId(String universeId, AliasTargetKind kind, String targetId)`
  - `Optional<AliasEntity> findByUniverseIdAndTargetKindAndTargetIdAndAliasText(...)` (uniqueness check on create)
- `AliasDesignerService` — find/save/delete + bulk lookup by target (for tooltip/panel display)

The key lookup method for display:

```java
public List<Alias> findAliasesForTarget(AliasTargetKind kind, String targetId, Set<String> activeUniverseIds) {
    // Returns aliases for the target whose universeId is in activeUniverseIds
}
```

This is the call the renderer and Fictional Info tab make to populate their display.

---

## §5 — UniverseFilteringService integration

F.1's UniverseFilteringService becomes the gateway for "which universes are active?" The AliasDesignerService consumes this:

```java
public List<Alias> findActiveAliasesForTarget(AliasTargetKind kind, String targetId) {
    Set<String> activeIds = universeFilteringService.getActiveUniverseIds();
    return findAliasesForTarget(kind, targetId, activeIds);
}
```

The renderer and Fictional Info tab call `findActiveAliasesForTarget` — they don't need to manage universe state themselves; the filtering service handles it.

When UniverseActivationChangedEvent fires (F.1 Step 5), the renderer and Fictional Info tab refresh via the broker pattern from F.1 Step 6. The Aliases for the currently-selected star/exoplanet may change as universes activate/deactivate.

---

## §6 — Rendering integration

### §6.1 — Renderer tooltip on stars

When the user hovers over a star in the 3D plot, a tooltip appears showing:

1. The star's display name (already shown today)
2. (NEW) If the star has aliases from currently-active universes, the aliases are listed below, one per line, with universe attribution

Example tooltip:
```
40 Eridani A
  Vulcan (Star Trek)
  Forty Eri Prime (Children of the Pattern)
```

Implementation: Step 1 verifies the existing tooltip mechanism. If tooltips are constructed at render time, F.2 needs to either (a) reconstruct on universe activation change, or (b) build dynamic tooltips that query at hover time. (b) is simpler if the renderer supports it.

### §6.2 — Renderer tooltip on exoplanets

Exoplanets are shown in the per-system view (when entering a solar system). Same tooltip pattern as stars. The targetKind for exoplanet aliases is EXOPLANET; the universe-scoped tooltip layer queries by targetId.

### §6.3 — Renderer labels — unchanged

The existing label rendering uses `StarObject.aliasList` (catalog names, "NAME Proxima Centauri" etc.). This continues unchanged. F.2's universe-scoped aliases appear in tooltips only, not in the label rendering. The labels stay as the catalog-identifier surface; tooltips become the worldbuilding-overlay surface.

---

## §7 — Fictional Info tab integration

### §7.1 — Aliases section layout

The Fictional Info tab is currently a flat list of fields (Polity, World type, Fuel type, etc., all showing "NA" when unset). F.2 adds an Aliases section at the top:

```
[Fictional Info tab]

Aliases (from active universes):
  • Vulcan (Star Trek)
  • Forty Eri Prime (Children of the Pattern)

────────────────────────

[Existing fields, unchanged]
Polity:           NA
World type:       NA
Fuel type:        NA
... (etc.)
```

When no universes are active OR no aliases exist for this star in active universes, the Aliases section shows "(no aliases — activate a universe to see worldbuilding names)" or similar placeholder. The visual separator between Aliases section and existing fields preserves the distinction between universe-scoped (top) and per-star scalar (bottom) data.

### §7.2 — Refresh on UniverseActivationChangedEvent

When the user toggles a universe, the Fictional Info tab refreshes its Aliases section to reflect the new active set. The existing fields below the separator don't refresh (they're per-star scalars, unaffected by universe state).

### §7.3 — Exoplanet display analogous

If exoplanets have an analogous info panel/tab in the system view, the same Aliases section appears there with exoplanet-scoped aliases. Step 1 verifies whether such a panel exists.

---

## §8 — Create Alias UI

### §8.1 — Worldbuilding menu integration

The Worldbuilding menu gains a new item: **Aliases...** (Alt+A mnemonic), placed below the Universes... item:

```
Worldbuilding (_W)
├── Universes...           ← F.1
├── Aliases...             ← F.2 NEW
├── (separator)
├── Ships...
├── Stations...
├── Weapons...
├── Megastructures...
└── Gate Networks...
```

### §8.2 — AliasesDialog (modeless Stage)

Per F.1's Universes dialog pattern. BorderPane with:
- Top: filter row (Universe dropdown defaulting to "All active universes", target kind filter)
- Center: TableView listing aliases (columns: Universe, Target kind, Target name, Alias text, Description excerpt)
- Bottom: Create..., Edit..., Delete..., Close buttons

The TableView filters by active universes by default. The Universe dropdown lets the user narrow further or include inactive universes.

### §8.3 — Create Alias sub-dialog

Modal dialog opened from the Aliases dialog's "Create..." button. Fields:

- **Universe** dropdown (required) — defaults to single active universe if exactly one is active; otherwise user picks. Lists all universes (active and inactive).
- **Target kind** radio buttons — Star vs Exoplanet
- **Target picker** (target-kind-dependent):
  - For Star: text-search autocomplete over StarObject by displayName + commonName + aliasList. Limits to current dataset's stars.
  - For Exoplanet: text-search autocomplete over ExoPlanet by name + alternateNames. May filter to exoplanets within the current dataset's stars.
- **Alias text** (required) — the fictional name, max 255 chars
- **Description** (optional) — context, max 1000 chars
- Save / Cancel buttons

Validation:
- All required fields present
- (universe, target) combination doesn't already have an alias (uniqueness check, via repository)
- Friendly error if duplicate

### §8.4 — Target picker UI

The target picker is the trickiest UI element. Step 1 verifies existing star-search machinery in TRIPS. Possibilities:
- TRIPS likely has existing star search (the workbench? a search box?)
- F.2 may be able to reuse the existing search component
- If not, F.2 builds a simple autocomplete: type 3+ characters, show top 20 matches by displayName + commonName + aliasList match

For exoplanets: smaller catalog (likely thousands, not millions). Simple list+filter is probably sufficient.

---

## §9 — Acceptance gate

F.2's acceptance gate is the §5 invariants from the worldbuilding-platform-requirements doc, with R5.7 now substantive (was vacuous in F.1):

### §9.1 — R5.5: Universes don't leak

Activate only Universe A. Create an Alias in Universe A pointing at Star X. Create an Alias in Universe B pointing at Star X. Verify that with only A active, only A's alias appears in tooltips and the Fictional Info tab. B's alias is hidden.

### §9.2 — R5.6: Real data persists

Activate any combination of universes. Verify the star's `displayName`, `commonName`, `aliasList`, `worldBuilding` fields, etc. all remain visible and unaffected. Aliases are an overlay, never a replacement.

### §9.3 — R5.7: Aliases scoped only when universe active

Create an Alias in Universe A. Activate Universe A → alias appears in tooltip and Fictional Info tab. Deactivate A → alias disappears. The R5.7 invariant is now exercised by real Alias rows for the first time.

### §9.4 — Bonus: ON DELETE CASCADE integrity

Create aliases in Universe A. Delete Universe A. Verify all of A's aliases are deleted (no orphans). Verify no other universes' aliases are affected.

### §9.5 — Uniqueness constraint

Attempt to create two aliases for the same (universe, target) pair. Verify the second create fails with a clear error message.

---

## §10 — Step breakdown

F.2 ships in **7 steps**, mirroring F.1's structure. Each step ends with a test pass and ratification gate.

| Step | Subject | Net new tests (est.) |
|---|---|---|
| 1 | Verification + audit (renderer tooltip mechanism, star search machinery, Fictional Info tab FXML, ExoPlanet info panel if exists) | 0 |
| 2 | Alias record + AliasTargetKind enum + Cataloged implementation + entity + mapper + repository + service | ~30 |
| 3 | V18 migration | ~5 |
| 4 | Renderer tooltip integration for stars + exoplanets | ~15 |
| 5 | Fictional Info tab Aliases section + UniverseActivationChangedEvent refresh | ~15 |
| 6 | Worldbuilding → Aliases dialog + Create Alias sub-dialog + target picker | ~25 |
| 7 | Close-out: §9 invariant tests + plan doc rollup + retroactive design doc | ~10 |

Total est. ~100 net new tests across 7 steps.

Step boundaries match where user-visible behavior changes:
- After Step 2-3: the entity + table exist; nothing visible yet
- After Step 4: tooltips show aliases (when seeded via test fixture)
- After Step 5: Fictional Info tab shows aliases
- After Step 6: users can create aliases through the UI
- After Step 7: §9 invariants verified; F.2 ships

---

## §11 — Out of scope

- **Search by alias.** Typing "Vulcan" in a search bar finding 40 Eridani A. Deferred.
- **Inline editing of aliases from the star info panel.** Users navigate to Worldbuilding → Aliases to edit. The Fictional Info tab is display-only for aliases.
- **Bulk alias import** (CSV of aliases for a universe). Deferred to F.x universe-import work (R8.9).
- **Alias categorization** (formal vs colloquial, official vs popular). Single aliasText string; multi-naming is the user's choice within the string.
- **Multiple aliases per (universe, target)**. Enforced uniqueness; not in scope to relax.
- **Migration of existing `StarObject.aliasList` strings**. They stay as catalog identifiers; not converted to F.2 Aliases.
- **Migration of existing `StarWorldBuilding` fields** (polity, etc.). Stay as legacy fields; F.3+ migrate them in their respective phases.
- **Cross-universe alias coordination**. Two universes independently defining "Vulcan" for different stars is fine; F.2 doesn't try to detect or warn.

---

## §12 — Success criteria

F.2 is complete when:

1. The user can open Worldbuilding → Aliases and see a list of all aliases (or empty list at first launch).
2. The user can create a new Alias by picking a Universe, choosing Star or Exoplanet, finding the target via autocomplete, entering alias text, and saving.
3. The created alias appears in the star/exoplanet's tooltip when its Universe is active.
4. The created alias appears in the Fictional Info tab's new Aliases section when the star is selected and its Universe is active.
5. Toggling the alias's Universe causes the alias to appear/disappear in both the tooltip and the Fictional Info tab.
6. Multiple universes' aliases for the same star are shown simultaneously when both are active.
7. Deleting a Universe removes all its aliases (cascade) without affecting other universes' aliases.
8. Attempting to create a duplicate (universe, target) alias produces a clear error.
9. The existing `StarObject.aliasList` (catalog names) continues to render in star labels unchanged.
10. The existing `StarWorldBuilding` fields continue to populate the Fictional Info tab's legacy section unchanged.
11. F.1's §7 invariants from UniverseFilteringInvariantsTest stay green throughout.

---

## §13 — Forward links

| Phase | Adds | F.2 dependency |
|---|---|---|
| F.3 | Faction entity refactor | Will use the per-(universe, star) data model pattern F.2 establishes |
| F.4 | Era entity | Same pattern; eras tag stars/exoplanets per universe |
| F.5 | Visual presentation rules | Per-universe styling overrides for aliased stars |
| F.6 | Population rules | Per-(universe, exoplanet) population data |
| F.7 | Tech constraints | Per-(universe, target) tech-level overrides |

The Alias entity's `(universeId, targetKind, targetId)` shape is the template subsequent phases follow.

---

## §14 — Naming conventions

- **Alias ids**: `catalog-alias-<uuid>` (UUID-based; aliases are user-created and don't have semantic slugs)
- **AliasTargetKind values**: `STAR`, `EXOPLANET` (uppercase, matching CatalogedKind convention from F.1)
- **Event class**: No new event class — UniverseActivationChangedEvent is sufficient (aliases follow universe state)
- **Service class**: `AliasDesignerService` in `com.teamgannon.trips.spaceshipmodeller.service` (mirrors UniverseDesignerService location)
- **Test classes**: `AliasTest`, `AliasTargetKindTest`, `AliasEntityTest`, `AliasMapperTest`, `AliasDesignerServiceTest`, `AliasesDialogTest`, `AliasFictionalInfoIntegrationTest`, `AliasRendererTooltipTest`

---

*End of Phase F.2 design doc. Awaiting Larry's ratification before Step 1 (verification + audit) begins.*
