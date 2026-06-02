# Worldbuilding Data Model Normalization

**Status**: design, pre-implementation (revised 2026-06-02 with major reframing per pre-Step-1 review)
**Date**: 2026-06-02
**Type**: Discrete task (architectural cleanup, not capability phase)
**Predecessors**: F.1 (Universe entity), F.2 (Alias entity)
**Successors**: F.3 Factions (resumes after this ships, with revised design accounting for the cleaner architecture)

---

## §0 — Scope and orientation

### Purpose

The current data model embeds universe-agnostic worldbuilding fields in entities that are conceptually astronomical: `StarObject.worldBuilding` carries polity, worldType, fuelType, techType etc. as per-star scalars; `ExoPlanet` carries population, techLevel, colonized, polity, etc. These fields predate the F.1 worldbuilding-platform work. They create three problems:

1. **Architectural mismatch.** Universe-agnostic fictional fields embedded in real-data entities make StarObject's schema incoherent — it's neither pure astronomical data nor properly universe-scoped worldbuilding data.

2. **Dual-system framing tax on every F.x phase.** F.1, F.2, F.3 all carry "legacy field stays; new universe-scoped entity adds alongside" framing because the existing fields can't be cleanly attributed to universes. Each phase pays a small ongoing cost.

3. **Blocks universe portability.** Once the worldbuilding data is fully universe-scoped in dedicated tables, an entire universe can be exported (Universe row + all Aliases + Factions + future entities, all transitively referencing universe_id) as a portable unit. The legacy fields can't participate because they aren't universe-attributed.

This task **separates astronomical and worldbuilding data at the model level** with a deliberate reframe:

> Every affected feature is a worldbuilding feature wrongly modeled as universe-agnostic astronomical metadata. This task removes all of them. F.3 reintroduces each in its proper universe-scoped form. The interim period (between this task and F.3 ship) is feature-degraded by design — that's the price of the cleanup, and it's a forcing function for F.3 to focus on reintroducing user-visible value.

No triage like "delete vs no-op vs keep" — every affected feature is deleted here; F.3 reintroduces with proper architecture.

Three categories of field/feature, three different fates:

| Category | Disposition | Examples |
|---|---|---|
| **Worldbuilding wrongly modeled** | Delete entirely | `worldBuilding.polity`, ExoPlanet `population`, polity-coloring button, polity route exclusion |
| **Astronomical workflow / user observations** | Keep | `StarObject.notes`, `ExoPlanet.notes`, catalog identifier `aliasList` |
| **Data lineage / provenance** | Keep | `StarObject.source`, `ExoPlanet.publication`, `ExoPlanet.detectionType` |

### What this task delivers

1. **Field removal** from `StarWorldBuilding` embedded record (deleted entirely; 11 fields removed)
2. **Field removal** from ExoPlanet's sci-fi section (7 fields removed: population, techLevel, colonized, colonizationYear, polity, strategicImportance, primaryResource)
3. **Field removal** of `StarObject.customData1-10` per the (D) ratification — if these fields exist (Step 1 audit verifies; one earlier audit mentioned them; the initial StarObject.java read did not surface them)
4. **Feature deletion sweep** — all 14 worldbuilding-wrongly-modeled features delete (full inventory in §4)
5. **V19 migration** dropping the corresponding columns from STAR_OBJ and EXOPLANET tables (and customData columns if they exist)
6. **UI cleanup** — "Fictional Info" tab → "Worldbuilding" tab; legacy field row group removed; gray-out behavior with actionable overlay message when no universes active; Aliases section (F.2) preserved
7. **Edit Star dialog cleanup** — worldbuilding-field editing UI removed
8. **PlanetPropertiesDialog cleanup** — worldbuilding-field editing UI removed
9. **"Star Polities" toolbar button deletion** — coloring by a deleted field is meaningless; F.3 ships fresh control
10. **ChView import path adjustment** — `StarObject.fromChvRecord` no longer sets polity from group numbers (F.3 reintroduces via FactionAssignment auto-seeding)

### What this task does NOT deliver

- **Universe load/export capability.** This task positions us for it by ensuring worldbuilding data is fully self-contained in universe-scoped tables, but the actual import/export work is future scope.
- **Replacement UI for the deleted worldbuilding fields.** F.3+ phases introduce universe-scoped equivalents (FactionAssignment replaces polity-as-scalar; F.6 replaces population; F.7 replaces techLevel; etc.). In the interim, the Worldbuilding tab is sparse and the deleted features stay deleted until F.3 reintroduces them.
- **Migration of existing legacy field values.** Drop and forget. The values are lost. ChView-imported polity values were the most populated; their loss is a known regression and the forcing function for F.3.
- **ExoPlanet atmospheric/physical properties.** These (atmosphereType, atmosphereComposition, hydrosphere, surfaceTemperature, etc.) stay on ExoPlanet because they're physical properties, not worldbuilding.
- **CSV worldbuilding columns in astronomical exports.** Per (C), polities and other worldbuilding fields are universe-scoped and need their own export mechanism (future work tied to universe load/export). The astronomical CSV export loses the polity column. No interim compatibility shim.
- **F.3 work.** F.3 resumes against the cleaner architecture after this ships; F.3's design doc gets revised for the new baseline (per §6.4 below).

### Why a discrete task, not F.x

This doesn't introduce worldbuilding capability — it normalizes existing data structure. Same shape as the menu rename, status bar rationalization, and other discrete tasks landed across the codebase. Naming it as a discrete task signals "preparatory cleanup, not feature work."

---

## §1 — Architectural decisions

### §1.1 — Reframe: every affected feature is worldbuilding wrongly modeled

The pre-Step-1 review surfaced a load-bearing reality: there are no current users, and the legacy entanglement was a wrong architectural turn. Every affected feature (polity coloring, polity route exclusion, polity context menus, polity scoring, polity CSV exports) is conceptually a worldbuilding feature that was wrongly shipped as universe-agnostic astronomical metadata. This task removes all of them; F.3 reintroduces each in its proper FactionAssignment-based universe-scoped form.

The interim period is feature-degraded by design. This is a forcing function for F.3 to focus on reintroducing user-visible value rather than scope-drifting into novel capabilities.

No "keep as no-op" or "delete vs migrate vs degrade" triage. Every affected feature is deleted here.

### §1.2 — Drop legacy fields entirely (no migration)

Existing values in `StarObject.worldBuilding.*`, `ExoPlanet.{population,techLevel,colonized,colonizationYear,polity,strategicImportance,primaryResource}`, and `StarObject.customData1-10` (if present) are dropped without migration.

Rationale:
- No current users; data loss has no real impact
- Most values are "NA" defaults (the fields exist but were never populated)
- Populated values are predominantly from ChView imports setting polity to Caine Riordan factions
- Those values lack universe attribution; reconstructing universe attribution would require heuristic and could be wrong
- The cleaner path is to drop and let F.3+ create properly attributed FactionAssignments going forward
- Universe load/export (future work) will let users import canonical universe data sets reproducing what the legacy field provided in a properly attributed way

### §1.3 — StarObject and ExoPlanet covered symmetrically

Both entities carry universe-agnostic fictional fields; both get the same treatment. The discrete task does StarObject's StarWorldBuilding cleanup AND ExoPlanet's sci-fi field cleanup in one migration (V19). Symmetric scope avoids inconsistency.

### §1.4 — Astronomical/workflow/provenance fields stay

The triage principle: notes is "general user observations on this real entity." Provenance fields are "data lineage." Worldbuilding fields are "fictional metadata that only makes sense within a universe." Three categories; only the third deletes.

**Fields that stay** (Step 1 verifies each exists and reach is purely astronomical/workflow/provenance):

| Field | Category | Reason it stays |
|---|---|---|
| `StarObject.notes` | Workflow | User-workflow general observations on real stars ("interesting parallax, follow up on Gaia DR4") |
| `ExoPlanet.notes` | Workflow | User-workflow general observations on real exoplanets |
| `StarObject.source` | Provenance | Import provenance (CHView etc.) |
| `ExoPlanet.publication` | Provenance | Scientific provenance (source paper) |
| `ExoPlanet.detectionType` | Provenance | Scientific provenance (detection method) |
| `ExoPlanet.massDetectionType` | Provenance | Scientific provenance |
| `ExoPlanet.radiusDetectionType` | Provenance | Scientific provenance |
| `StarObject.aliasList` | Catalog | Catalog identifier variants (universe-agnostic by design — Simbad/Bayer/HIP names — distinct from F.2's universe-scoped `Alias` entity) |
| `ExoPlanet.alternateNames` | Catalog | Catalog identifier variants |
| ExoPlanet atmospheric/physical fields | Astronomical | Real-data physical properties (atmosphereType, atmosphereComposition, hydrosphere, cloudCover, iceCover, albedo, surfacePressure, surfaceTemperature, density, coreRadius, axialTilt, dayLength, surfaceGravity, escapeVelocity) |
| ExoPlanet orbital + identity fields | Astronomical | hostStarId, parentPlanetId, isMoon, orbital parameters, mass/radius/temperature, procedural-generation metadata |
| `StarObject.realStar` | Astronomical | Distinguishes real catalog stars from procedurally-generated ones; not worldbuilding |

### §1.5 — "Star Polities" toolbar button deletion (was: F.3 refactor)

The button currently colors stars by the legacy `polity` field. After this task, polity is gone. The button has no data source.

Options were: delete, keep no-op, refactor. The reframe per §1.1 dictates deletion. F.3 ships a fresh faction-coloring control with proper FactionAssignment-based data, not a refactor of the deleted button.

**Scope shift from F.3 doc:** F.3's pre-revision design (decision #3 path α) had the Star Polities button refactored. That decision is superseded: F.3's revised design has the button deleted by this task; F.3 introduces a fresh control.

### §1.6 — ChView import polity-setting removal (deletion, not retention as comment)

`StarObject.fromChvRecord` currently sets polity from `chViewRecord.getGroupNumber()` via `CivilizationDisplayPreferences` constants. These lines are removed entirely.

The `CivilizationDisplayPreferences` constants themselves stay (F.3 may use the color constants for seeded factions' displayColor values). The group-number-to-polity mapping logic is removed — F.3 reintroduces via auto-seeded FactionAssignments in the Caine Riordan universe (the work the original mapping was trying to do, now done properly).

Regression: ChView imports no longer carry polity information until F.3 reintroduces. This is the forcing function for F.3 to ship FactionAssignment auto-seeding.

### §1.7 — Worldbuilding tab gray-out with actionable overlay

The renamed Worldbuilding tab shows only universe-scoped content. When no universes are active:
- Aliases section shows its existing "no aliases — activate a universe to see worldbuilding names" placeholder (F.2 behavior preserved)
- Tab itself is grayed out (semi-transparent) **with an actionable overlay message:**

> No worldbuilding universe active. Open Worldbuilding → Universes... to activate a universe.

When universes are active:
- Tab renders normally
- Sections (Aliases, future Factions, etc.) populate per their content
- Overlay disappears

The overlay's actionable message addresses the fresh-install UX edge case where a user opens a star info panel with no universes active and the grayed-out tab is opaque about what to do.

### §1.8 — Cataloged interface unchanged

This task doesn't touch the Cataloged interface. StarObject and ExoPlanet are not Cataloged implementations (they're astronomical entities, not worldbuilding entries). The interface contract stays as-is.

---

## §2 — Glossary

**Astronomical data**: Real-world or physically-grounded data about stars and exoplanets that doesn't change based on worldbuilding context. HYG catalog fields, Simbad/Bayer/HIP identifiers, spectral classification, orbital parameters, atmospheric composition (whether from real measurements or ACRETE-generated), etc.

**Workflow data**: Universal user-workflow surfaces with value regardless of worldbuilding context. Free-text notes ("interesting parallax measurement"), user annotations on real entities.

**Provenance data**: Data lineage — where this record came from. Import source ("CHView"), scientific publication, detection method.

**Worldbuilding data**: Fictional or interpretive data about astronomical objects that exists within a worldbuilding context (a Universe). Aliases, faction assignments, era memberships, population data, tech levels, etc.

**Legacy worldbuilding fields**: The pre-F.1 universe-agnostic fictional fields embedded in StarObject and ExoPlanet, deleted by this task.

**Worldbuilding tab**: The renamed "Fictional Info" tab in the star info panel; surface for universe-scoped worldbuilding content.

**Forcing function**: An interim feature degradation that drives subsequent work to focus on a specific user-visible outcome. Here, deleting polity-using features without immediate replacement forces F.3 to reintroduce them with proper architecture rather than scope-drifting.

---

## §3 — Pre-design audit findings (Step 1 verification produces concrete inventory)

### §3.1 — StarWorldBuilding reach (from F.2 + F.3 Step 1 audits)

`StarObject.worldBuilding` is set by:
- `StarObject.init()` — defaults to NA
- `StarObject.fromChvRecord()` — sets polity from group numbers (deleted by this task)
- Edit Star dialog (Step 1 verifies controller location)
- Possibly catalog import services (CHV/CSV — Step 1 verifies)

`StarObject.worldBuilding` is read by 50+ callsites per F.3 Step 1 audit §16.2:
- `StarPropertiesPane.java` Fictional Info tab — displays values (entire row group deleted)
- `StarRenderer.java` tooltip — `record.getPolity()` (Polity line removed from tooltip)
- "Star Polities" toolbar button — colors stars by polity (button + handler deleted)
- Edit Star dialog — populates editable fields (editing UI deleted)
- `RouteFindingService.getPolityExclusions()` — routing exclusion (feature deleted; F.3 reintroduces)
- `PolitySelectionPanel` — search filter (panel deleted; F.3 reintroduces as FactionSelectionPanel)
- `StarContextMenuHandler` — polity-based context menu items (items deleted; F.3 reintroduces)
- `DisplayScoreCalculator` — polity weight in scoring (weight removed; F.3 reintroduces)
- `StarTableExportService`, `CSVDataSetDataExportTask`, `StarCsvFormatter` — CSV polity column (column removed)
- `StarDisplayRecord.polity` — UI display model field (field removed)
- `SolarSystem` polity propagation (removed)
- `AstrographicObjectFactory` ChView import (group-to-polity switch removed)

Step 1 verification produces the complete callsite inventory + confirms reframe applies to each (no surprises where a polity reference is genuinely astronomical).

### §3.2 — ExoPlanet sci-fi field reach

Step 1 verification needs to inventory:
- Read paths for population, techLevel, colonized, colonizationYear, polity, strategicImportance, primaryResource
- Write paths (Edit Planet dialog? import services?)
- Display surfaces (`PlanetPropertiesDialog` from F.2 Step 1's E.1 work — has `polityField` at line 131)
- Whether any of these fields are referenced by tests that need updating
- Whether `ExoPlanet.alternateNames` or other catalog-identifier fields exist (those stay per §1.4)

### §3.3 — Fictional Info → Worldbuilding tab reach

Per F.2 Step 1 audit + F.3 audit §16.5:
- `StarPropertiesPane.fxml` lines 60-121 hold the tab's GridPane
- Rows 1-13 hold the legacy worldbuilding field labels (deleted by this task)
- Rows 14-16 hold F.2's Aliases section (Separator + header + content) — preserved
- Tab `text` attribute changes from "Fictional Info" to "Worldbuilding"

`StarPropertiesPane.java` controller cleanup:
- Remove @FXML fields and `setText(...)` calls for the deleted legacy field labels
- Add gray-out behavior via opacity/visibility binding on `UniverseFilteringService.getActiveUniverseIds().isEmpty()`
- Add overlay Label with actionable message (§1.7)
- Reuse F.2's broker subscription (already wired for Aliases refresh) for gray-out toggle

### §3.4 — Edit Star dialog reach

Step 1 verification locates the Edit Star dialog's controller and FXML. The cleanup:
- Remove FXML editing widgets for the 11 deleted worldbuilding fields
- Remove @FXML fields, getters, save logic
- Dialog stays for astronomical editing (name, common name, spectral class, possibly catalog identifiers, notes if present)

### §3.5 — Star Polities toolbar button reach (deletion confirmed)

Per F.3 audit §16.3, the button is at `toolbar.fxml` lines 26-34 with `fx:id="togglePolityBtn"`. Handler chain: `ToolbarController.togglePolities()` → `SharedUIFunctions.togglePolities()` → `InterstellarSpacePane.togglePolities()` → publishes `UIStateChangeEvent(POLITIES, ...)`.

This task removes:
- The button from toolbar FXML
- The on-click handler from `ToolbarController` and `SharedUIFunctions`
- The `InterstellarSpacePane.togglePolities()` flag and any "color by polity" rendering logic in StarRenderer
- The `UIStateChangeEvent(POLITIES, ...)` event publishing (event type itself may be reused; the polity-specific publication path is removed)

`CivilizationDisplayPreferences` class stays — F.3 may reuse color constants.

### §3.6 — PolityObjectFactory reach

Per pre-Step-1 review's smaller items: `PolityObjectFactory` creates 3D mesh objects for the polity rendering. Post-deletion of the button + rendering logic, this factory has no caller.

**Disposition: Delete.** F.3 introduces a simpler color-application path (per the F.3 §6 renderer integration plan: faction `displayColor` applied directly to star sphere material rather than via mesh-object overlay). The mesh-shape capability dies with the factory; F.3's coloring is hex-string + Color object only.

### §3.7 — ChView import path reach (deletion confirmed)

`StarObject.fromChvRecord` group-number switch at lines 71-76 of `AstrographicObjectFactory.java`:
```java
case 1 -> starObject.setPolity(CivilizationDisplayPreferences.ARAKUR);
case 2 -> starObject.setPolity(CivilizationDisplayPreferences.HKHRKH);
case 4 -> starObject.setPolity(CivilizationDisplayPreferences.KTOR);
case 8 -> starObject.setPolity(CivilizationDisplayPreferences.TERRAN);
```

Deleted entirely. The chViewRecord's getGroupNumber() is still available for future use (F.3 polish phase can read it and auto-create FactionAssignments in Caine Riordan universe based on these mappings).

Step 1 verification audits CHV and CSV import services for other paths setting fictional fields.

### §3.8 — Test reach

Step 1 produces the concrete test impact projection. Categories:
- Tests verifying StarWorldBuilding initialization (e.g., that polity defaults to "NA") — DELETED
- Tests verifying ChView import sets polity — DELETED
- Tests verifying the Fictional Info tab displays the legacy fields — DELETED
- Tests for the Edit Star dialog's worldbuilding-field editing — DELETED
- Tests for the Star Polities button behavior — DELETED
- Tests for `RouteFindingService.getPolityExclusions` — DELETED
- Tests for `PolitySelectionPanel` — DELETED
- Tests for polity context menu items — DELETED
- Tests for polity in `DisplayScoreCalculator` — DELETED
- Tests for CSV polity column — MODIFIED (column removed from assertions) or DELETED
- New tests for Worldbuilding tab gray-out behavior — ADDED (5-10)

Realistic projection per pre-Step-1 review: 80-150 tests affected (mix of deleted, modified, ~5-10 added for gray-out). Net suite delta likely -50 to -100 tests (suite shrinks). Suite shrinkage is information: it tells us we're correctly removing coverage for features that no longer exist.

Step 1 produces the actual count.

### §3.9 — Universe load/export readiness verification

After this task, the worldbuilding data set is fully self-contained in universe-scoped tables. Step 1 verification confirms no remaining worldbuilding references exist on StarObject or ExoPlanet (the only "leakage" between universe-scoped tables and astronomical tables is via target_id FK in FactionAssignment, Alias, and future entities — all of which point AT astronomical entities, never the reverse).

Not an implementation deliverable — just verification that the architecture supports future load/export work.

### §3.10 — StarObject.customData1-10 disposition

The pre-Step-1 review flagged these as possibly existing per an earlier audit, but the actual StarObject.java read didn't surface them. Step 1 verification:
- Confirms whether customData1-10 fields exist on StarObject (or on `StarWorldBuilding`)
- If they exist: delete per (D) ratification — they're user-extensible fields with no enforced semantics, fitting "worldbuilding wrongly modeled" framing
- If they don't exist: documents that and moves on; no V19 column drops needed for them

---

## §4 — Feature inventory (the 14 affected items)

The complete inventory of features deleted by this task. Step 1 verification confirms reach for each and triages any genuine astronomical-not-worldbuilding surprises (none expected).

| # | Feature | Current location (per F.3 audit + this design) | F.3 reintroduction |
|---|---|---|---|
| 1 | `StarWorldBuilding` embedded record (11 fields) | `StarObject.worldBuilding` + `StarWorldBuilding` class | F.3 FactionAssignment (polity) + future F.x for other fields |
| 2 | ExoPlanet sci-fi fields (7 fields) | `ExoPlanet.{population,techLevel,colonized,colonizationYear,polity,strategicImportance,primaryResource}` | Future F.x phases (F.6 Population, F.7 Tech, etc.) |
| 3 | `StarObject.customData1-10` (if exists) | `StarObject` or `StarWorldBuilding` (Step 1 verifies) | Not planned for reintroduction (user-extensible fields fit Worldbuilding tab better as F.x custom-data feature if ever needed) |
| 4 | `RouteFindingService.getPolityExclusions` | `com.teamgannon.trips.routing.RouteFindingService` | F.3 reintroduces as FactionExclusions on routing |
| 5 | `PolitySelectionPanel` | `com.teamgannon.trips.selection.PolitySelectionPanel` (Step 1 confirms path) | F.3 reintroduces as `FactionSelectionPanel` |
| 6 | `StarContextMenuHandler` polity items | `com.teamgannon.trips.starplotting.StarContextMenuHandler` | F.3 reintroduces with faction context menu items |
| 7 | `DisplayScoreCalculator` polity weight | `com.teamgannon.trips.scoring.DisplayScoreCalculator` (Step 1 confirms) | Scoring loses one component; F.3 reintroduces faction weight |
| 8 | CSV polity column in astronomical exports | `StarTableExportService`, `CSVDataSetDataExportTask`, `StarCsvFormatter` | Worldbuilding exports become separate mechanism (future universe load/export) |
| 9 | `StarDisplayRecord.polity` field | `com.teamgannon.trips.graphics.entities.StarDisplayRecord` | F.3 may add factionId / factionName fields |
| 10 | F.2 renderer tooltip "Polity:" line | `AliasTooltipFormatter.formatStarTooltip` (F.2 Step 3) | F.3 reintroduces as "Polity: {faction} ({universe})" line |
| 11 | `SolarSystem` polity propagation | `com.teamgannon.trips.solarsystem.SolarSystem` (Step 1 confirms) | F.3 reintroduces as faction propagation |
| 12 | `PolityObjectFactory` | `com.teamgannon.trips.starplotting.PolityObjectFactory` | F.3 uses simpler color application (no factory) |
| 13 | ChView import polity-setting (`fromChvRecord`) | `AstrographicObjectFactory.create` lines 71-76 | F.3 polish reintroduces via FactionAssignment auto-seeding |
| 14 | "Star Polities" toolbar button | `toolbar.fxml` + `ToolbarController.togglePolities` + downstream | F.3 ships fresh faction-coloring control |

Step 1 verifies each, confirms reframe applies (flag anything genuinely astronomical), and produces detailed callsite inventory per feature.

---

## §5 — Data model changes

### §5.1 — StarWorldBuilding deletion

The `com.teamgannon.trips.jpa.model.StarWorldBuilding` class is deleted. The `@Embedded private StarWorldBuilding worldBuilding` field on StarObject is removed along with its `@AttributeOverrides`.

StarObject's compatibility accessor methods (`getPolity()`, `setPolity()`, `getWorldType()`, etc. — 11 getter/setter pairs at lines 526-557 per F.3 audit) are removed.

### §5.2 — ExoPlanet field deletion

The following fields are removed from `ExoPlanet`:
- `Long population`
- `Integer techLevel`
- `Boolean colonized`
- `Integer colonizationYear`
- `String polity`
- `Integer strategicImportance`
- `String primaryResource`

Lombok-generated getters and setters are removed automatically.

### §5.3 — StarObject.customData1-10 deletion (if exists)

If Step 1 confirms these fields exist on StarObject or StarWorldBuilding, they are deleted. V19 includes their column drops.

If they don't exist, this sub-deliverable is a no-op.

### §5.4 — V19 migration

```sql
-- Drop worldbuilding columns from STAR_OBJ
ALTER TABLE STAR_OBJ DROP COLUMN polity;
ALTER TABLE STAR_OBJ DROP COLUMN worldType;
ALTER TABLE STAR_OBJ DROP COLUMN fuelType;
ALTER TABLE STAR_OBJ DROP COLUMN portType;
ALTER TABLE STAR_OBJ DROP COLUMN populationType;
ALTER TABLE STAR_OBJ DROP COLUMN techType;
ALTER TABLE STAR_OBJ DROP COLUMN productType;
ALTER TABLE STAR_OBJ DROP COLUMN milSpaceType;
ALTER TABLE STAR_OBJ DROP COLUMN milPlanType;
ALTER TABLE STAR_OBJ DROP COLUMN other;
ALTER TABLE STAR_OBJ DROP COLUMN anomaly;
-- And customData1-10 if Step 1 confirms presence:
-- ALTER TABLE STAR_OBJ DROP COLUMN customData1;
-- ... through customData10

-- Drop worldbuilding columns from EXOPLANET
ALTER TABLE EXOPLANET DROP COLUMN population;
ALTER TABLE EXOPLANET DROP COLUMN techLevel;
ALTER TABLE EXOPLANET DROP COLUMN colonized;
ALTER TABLE EXOPLANET DROP COLUMN colonizationYear;
ALTER TABLE EXOPLANET DROP COLUMN polity;
ALTER TABLE EXOPLANET DROP COLUMN strategicImportance;
ALTER TABLE EXOPLANET DROP COLUMN primaryResource;
```

Forward-only and destructive. Step 1 verification confirms exact column names match what Flyway has actually applied (JPA field names should map cleanly, but column-name overrides could exist).

### §5.5 — Cataloged interface

Unchanged by this task. F.3 will add the `factionId()` default; that's F.3 territory.

---

## §6 — UI changes

### §6.1 — Worldbuilding tab rename + gray-out

`StarPropertiesPane.fxml`:
- The `<Tab text="Fictional Info">` element's text attribute changes to "Worldbuilding"
- The tab's content GridPane removes the 13 legacy-field rows; preserves the F.2 Aliases section rows
- A new top-level container handles gray-out behavior (likely an `<StackPane>` wrapping the existing content plus an overlay `<Label>` shown when no universes are active)
- Overlay message: "No worldbuilding universe active. Open Worldbuilding → Universes... to activate a universe."

`StarPropertiesPane.java`:
- Remove @FXML fields for the 11 deleted legacy field labels
- Remove the `populate...()` methods that set those labels
- Add gray-out logic: bind tab content opacity (or visibility of an overlay) to `universeFilteringService.getActiveUniverseIds().isEmpty()`
- The existing F.2 broker subscription extends to trigger gray-out refresh on universe activation/deactivation

### §6.2 — Edit Star dialog cleanup

Step 1 verification locates the Edit Star dialog's controller and FXML. The cleanup:
- Remove FXML elements editing polity, worldType, fuelType, etc.
- Remove @FXML fields, getters, save logic for those elements
- Dialog stays for astronomical editing (name, common name, spectral class, possibly catalog identifiers, notes)

If most of the dialog's content was the worldbuilding fields, the dialog becomes much shorter post-cleanup. That's fine.

### §6.3 — "Star Polities" toolbar button deletion

Per §3.5: remove button from `toolbar.fxml`, remove handler from `ToolbarController` + `SharedUIFunctions` + `InterstellarSpacePane`, remove "color by polity" rendering logic from `StarRenderer`, remove the rendering flag if separate property.

`CivilizationDisplayPreferences` class stays.

### §6.4 — PlanetPropertiesDialog cleanup

Per F.2 Step 1 finding: `PlanetPropertiesDialog.java` is an edit dialog with `polityField` (line 131). Step 1 verification extends to find which of the seven deleted ExoPlanet fields are surfaced. Each surface gets removed:
- @FXML fields removed
- FXML elements removed
- Save logic adjusted
- Dialog becomes astronomical/physical editing only (orbital parameters, atmospheric data, etc.)

### §6.5 — Feature-deletion sweep (the 14 items)

Per §4 inventory. Step 3 of implementation handles the bulk:
- Delete `RouteFindingService.getPolityExclusions` + callers
- Delete `PolitySelectionPanel` class + any references in FXML/controllers
- Delete polity context menu items in `StarContextMenuHandler`
- Remove polity weight from `DisplayScoreCalculator`
- Delete polity columns from CSV export services
- Delete `StarDisplayRecord.polity` field
- Remove "Polity:" line from `AliasTooltipFormatter.formatStarTooltip`
- Remove polity propagation in `SolarSystem`
- Delete `PolityObjectFactory` class
- Remove ChView polity-setting in `AstrographicObjectFactory`

Each deletion verified by Step 1's callsite inventory.

---

## §7 — Migration strategy

### §7.1 — Forward-only, destructive

V19 drops columns. Data in those columns is lost. There is no rollback path; once V19 applies, the data is gone.

### §7.2 — No staged migration

The columns and fields are deleted in one step. We don't preserve them as deprecated or read-only intermediates. The dual-system framing pattern (legacy field stays; new entity adds alongside) is the thing we're getting rid of.

### §7.3 — Test data updates

Tests that reference the deleted fields are updated or deleted per §3.8 categories. Step 1 produces the concrete count.

### §7.4 — Coordination with F.3 — design doc revision

F.3 design doc (currently on master at `ec62fa5e` + Step 1 audit at `82b32365`) was written against the legacy field presence. After this task ships, F.3's design needs revision before F.3 Step 2 implementation resumes:

| F.3 reference | Pre-revision content | Post-revision content |
|---|---|---|
| F.3 §3.2 (StarObject.polity reach) | "F.3 doesn't touch the field; legacy reads continue" | "Field no longer exists; F.3 introduces FactionAssignment as the universe-scoped equivalent" |
| F.3 §3.3 + decision #3 (Star Polities button path α refactor) | "Refactor toolbar button to faction-coloring master switch" | "Button deleted by normalization task; F.3 introduces fresh faction-coloring control" |
| F.3 §1.6 (CatalogProvenance migration) | "Map sourceUniverse → factionId via V21 data migration" | "V21 adds factionId columns + seeds Factions from CivilizationDisplayPreferences constants; no data migration (polity field gone, sourceUniverse values are universe-named not faction-named)" |
| F.3 audit R1 ratification | "V21 scope shrunk; seed Caine Riordan factions from CivilizationDisplayPreferences" | "Reaffirmed — `CivilizationDisplayPreferences` stays per §1.6; F.3 V21 is column-addition + curated seed only" |
| F.3 audit R2 ratification | "Fallback option (i): legacy polity-field coloring" | "N/A — no legacy polity field to fall back to. New 'no faction data' fallback = neutral gray with FactionAssignment placeholder text" |
| F.3 audit R3 (Star Polities button refactor scope) | "Step 2 first task: refactor button" | "Button already deleted; F.3 ships fresh control as Step 4 deliverable" |

F.3's design doc revision happens as part of F.3 resumption (γ-style doc revision before implementation), not in this task.

The interim feature degradation between this task ship and F.3 ship is the forcing function: F.3 must reintroduce the 14 features in proper universe-scoped form before users have functional polity-related capability.

---

## §8 — Step breakdown

This task ships in **5 implementation steps + Step 1 verification + close-out = 7 numbered steps**. Larger than the menu rename or status bar work; smaller than F.x phases.

| Step | Subject | Net new/changed tests (est.) |
|---|---|---|
| 1 | Verification + audit (complete callsite inventory per affected feature, reframe-applicability check, customData1-10 disposition, provenance field confirmation, test impact projection) | 0 |
| 2 | Data layer cleanup — delete StarWorldBuilding, delete ExoPlanet fictional fields, delete customData1-10 if present, V19 migration, compat constructors absorb removals | ~20-40 tests modified |
| 3 | Feature-deletion sweep — the 14 items from §4 inventory; largest step by reach | ~40-80 tests deleted, ~10-20 modified |
| 4 | UI cleanup — Worldbuilding tab rename + gray-out overlay with actionable message, Edit Star dialog cleanup, PlanetPropertiesDialog cleanup | ~5-10 new tests for gray-out; ~10-15 modified for dialog cleanup |
| 5 | Close-out — §9 acceptance gate verification, plan doc rollup, retroactive design doc | ~5 new tests |

Total: ~80-150 tests affected (mix of deleted, modified, ~10-15 new for gray-out). Net suite delta likely -50 to -100 tests (shrinkage as worldbuilding-feature coverage retires).

Realistic shipping time per pre-Step-1 review: Step 1 verification 60-90 min, implementation 4-6 hours, close-out 30-45 min. Total ~5-7 hours shipping.

---

## §9 — Acceptance gate

This task is complete when:

1. StarObject has no `worldBuilding` field; the `StarWorldBuilding` class doesn't exist
2. ExoPlanet has none of the seven deleted worldbuilding fields
3. StarObject.customData1-10 are gone (if they existed)
4. V19 has applied; database has no corresponding columns
5. The 14 affected features from §4 inventory are deleted (no lingering callsites)
6. Fields per §1.4 ("Fields that stay") are still present and functional (notes, source, publication, detectionType, etc.)
7. The star info panel has a "Worldbuilding" tab (not "Fictional Info") with only the F.2 Aliases section visible (legacy fields gone)
8. When no universes are active, the Worldbuilding tab is grayed out with actionable overlay message
9. When universes are active, the Worldbuilding tab renders normally with Aliases content
10. Edit Star dialog has no worldbuilding-field editing UI
11. PlanetPropertiesDialog has no worldbuilding-field editing UI
12. Star Polities toolbar button is gone
13. `PolityObjectFactory` class is gone
14. ChView imports no longer set polity values on imported StarObjects
15. F.1's `UniverseFilteringInvariantsTest` stays green (4,116-test baseline preserved minus deletions)
16. F.2's `AliasFilteringInvariantsTest` stays green
17. Test suite passes (modulo deletions per Step 1 projection — sanity check that the right tests were retired)

---

## §10 — Out of scope

- **Replacement UI for any deleted worldbuilding capability.** F.3+ phases introduce universe-scoped equivalents.
- **Migration of existing legacy field values.** Drop and forget.
- **Universe load/export implementation.** Future capability; this task only positions for it.
- **Cataloged interface changes.** F.3 territory.
- **F.3 design doc revision.** Happens as part of F.3 resumption.
- **`CivilizationDisplayPreferences` class deletion.** Stays; F.3 may reuse color constants.
- **Removal of `realStar` boolean from StarObject.** Stays — astronomical data distinguishing real catalog stars from procedurally-generated ones, not worldbuilding.
- **Notes field on StarObject or ExoPlanet.** Stays — workflow surface, universe-agnostic by intent.
- **Provenance fields** (source, publication, detectionType, etc.). Stay — data lineage, not worldbuilding.
- **Catalog identifier `aliasList` / `alternateNames`.** Stay — universe-agnostic by design (Simbad/Bayer/HIP names), distinct from F.2's universe-scoped Alias entity.

---

## §11 — Naming conventions

- **Task name**: "Worldbuilding data model normalization" (consistent with similar discrete tasks)
- **Migration**: V19 (next available; V18 was F.2's alias table)
- **No new entities**: just deletions and renames
- **Tab name change**: "Fictional Info" → "Worldbuilding"

---

## §12 — Forward links

After this task ships:
- **F.3 Factions** resumes with γ-style design doc revision (§7.4 above) before implementation. F.3 reintroduces the 14 deleted features in proper universe-scoped form; the interim feature degradation is the forcing function.
- **F.4-F.10** each compose against the now-coherent architecture; no dual-system framing tax going forward
- **Universe load/export** becomes feasible as a discrete task whenever it's prioritized

---

*End of design doc. Awaiting Larry's ratification before Step 1 (verification + audit) begins. The reframe in §1.1 is the load-bearing change vs the original design — every affected feature is wrongly-modeled worldbuilding to be deleted here and reintroduced by F.3.*
