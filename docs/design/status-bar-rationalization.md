# Status Bar Rationalization

**Status**: design, pre-implementation
**Date**: 2026-06-01
**Predecessor**: F.1 Step 8 (added `universeStatus` to `StatusBarController`)
**Successor**: implementation conversation (single conversation; estimated 4–6 steps)

---

## §1 — Purpose

The status bar today carries three indicators (Plot Status, Routing State, Worldbuilding) plus an ad-hoc text channel that publishers shove arbitrary messages into. The F.1 Step 7 screenshot surfaced the practical problem: at typical window widths the text truncates ("Real + 1 universe(s) act…"), and the channel mixes two semantically distinct concerns — *what state am I in?* and *what just happened?*

The rationalization gives each concern its own slot:

- **Action message** (transient): "what just happened?" — past-tense events surfaced briefly, then they fade.
- **Three persistent indicators**: "what state am I in?" — dataset, routing, worldbuilding. Each reflects current persisted state; survives restarts.

The user's mental model becomes consistent: the left slot is ephemeral; the right slots are durable.

---

## §2 — Current state

Step 1 audit:

| Indicator | Today's backing | Today's update path |
|---|---|---|
| Plot Status | `databaseStatus` Label | `@EventListener StatusUpdateEvent` — 11+ publishers (PythonScriptEngine, MainSplitPaneManager dataset/recenter/display/export ops) push ad-hoc text |
| Routing State | `routingStatus` Label | Direct `routingStatus(boolean)` call from `RouteEventHandler`; `RoutingStatusEvent` exists but is not consumed by StatusBarController |
| Worldbuilding | `universeStatus` Label | `@EventListener UniverseActivationChangedEvent` + `@EventListener ApplicationReadyEvent` (F.1 Step 8) |

Two observations:

1. **The Plot Status label is already serving as the action message slot.** Publishers use `StatusUpdateEvent` to surface ephemeral messages ("Dataset loaded is: HYG-30ly", "Recenter failed", "Running script: foo.py"). The label *name* suggests persistent state, but the *usage* is transient.

2. **There's no Dataset indicator today.** The user has no persistent display of "which dataset is currently selected" — only the most-recent StatusUpdateEvent text, which gets overwritten by the next event.

The rationalization splits these concerns cleanly: Plot Status (today's label) becomes the action message slot; a new Dataset indicator joins the persistent group.

---

## §3 — Four-category state surface

Left to right:

| Position | Slot | Lifetime | Source of truth |
|---|---|---|---|
| 1 (leftmost) | Action message | Transient (5-min fade or replaced) | `ActionMessageEvent` publishers |
| 2 | Dataset | Persistent (survives restart) | `DataSetContext.activeDescriptor` |
| 3 | Routing | Persistent (survives restart) | Route service / persisted routing state |
| 4 | Worldbuilding | Persistent (survives restart) | `UniverseDesignerService.findAllActive()` |

Separator icon (the existing `⥮◄►⥮` glyph or similar) between each pair.

**Boot behavior**: action message slot is **blank** at app launch. The splash page handles boot-time progress messaging; the status bar's action slot is for in-session user-initiated events. The three persistent indicators populate from their backing services on `ApplicationReadyEvent` — the F.1 pattern, now applied uniformly.

---

## §4 — Layout specification

ASCII sketch:

```
| [action message slot ~280px]  ⥮◄►⥮  Dataset: <name>  ⥮◄►⥮  Routing: <state>  ⥮◄►⥮  Worldbuilding: <state>  |
```

- **Action slot**: reserved width ~280 px (room for ~35 characters), text-left-aligned, ellipsis on overflow. Empty at startup (no placeholder text, no zero-width — the reserved space stays so the persistent indicators don't shift when an action message arrives).
- **Persistent indicators**: each is a `Label name:` + `Label value` pair, bold label + non-bold value, with the existing F.1 styling. Tooltip on the value.
- **Separators**: `⥮◄►⥮` glyph between slots. Already used in the existing status bar.

The fixed-width action slot is the rationalization's key UX commitment: persistent indicators don't shift left or right when actions fire. The user's eye learns the position of each indicator and stays there.

---

## §5 — Persistent indicator specifications

Each persistent indicator:
- **Format**: `Category: <compact value>` — label is bold, value is non-bold; standard Verdana 13.
- **Tooltip**: longer-form explanation. Hover-discoverable.
- **Initial state**: populated on `@EventListener(ApplicationReadyEvent.class)` from the backing service.
- **Update**: `@EventListener` on the relevant domain event; FX-thread wrapped per the existing pattern.

### §5.1 — Dataset

- **Value**: `<name>` if a dataset is active, "(none selected)" otherwise. Example: "Dataset: HYG-30ly".
- **Tooltip**: star count, last-loaded timestamp, distance range. Example: "30 ly bubble around Sol; 12,847 stars; loaded 2026-06-01 14:23".
- **Backing event**: new `DataSetContextChangedEvent` (or whatever the existing context-change carrier is — Step 1 verification of the implementation conversation will name it; `SetContextDataSetEvent` exists today and is a likely candidate). Listener refreshes the value + tooltip from `DataSetContext`.

### §5.2 — Routing

- **Value**: "Inactive" or "Active". Existing color coding (green / red) preserved.
- **Tooltip**: explains the behavioral implication. "Routing inactive: clicking stars selects them. Routing active: clicking stars adds them to the current route." (Exact phrasing TBD during implementation.)
- **Backing event**: today's direct `RouteEventHandler` → `statusBarController.routingStatus(boolean)` call is refactored to publish a `RoutingStatusEvent` (the event class already exists; just isn't routed through StatusBarController today). Listener replaces the direct method call.

### §5.3 — Worldbuilding

- **Value**: "Real only" (default) or "Real + N universe(s) active".
- **Tooltip**: lists active universe names alphabetically, plus the R1.8 contract reminder when in real-only mode.
- **Backing event**: `UniverseActivationChangedEvent` (F.1 Step 8). Already implemented; just renamed/repositioned in the layout.

---

## §6 — Action message specifications

### §6.1 — Triggers

User-initiated state changes that are worth surfacing as a fleeting "you just did this" confirmation:

| Trigger | Example message |
|---|---|
| Dataset switch | "Loaded 30 ly dataset (12,847 stars)" |
| Dataset unload | "Unloaded HYG-30ly" |
| Universe activated | "Activated Legacy of the Aldenata" |
| Universe deactivated | "Deactivated Caine Riordan" |
| Routing toggled on | "Routing activated" |
| Routing toggled off | "Routing deactivated" |
| Major data operation | "Imported 142 stars from CSV" |
| Operation failed | "Recenter failed — see log" |

**Not** triggers (no action message):

- View changes (rotate, pan, zoom)
- Selection changes (click a star, click a planet)
- Catalog editing (rename a station, edit a ship's mass)
- Application configuration (preferences edits, theme changes)
- Background work that isn't user-initiated

### §6.2 — Format

- **Past-tense verb leading the message.** "Activated …", "Loaded …", "Imported …", "Failed …".
- **Compact**: target ~35 characters; the action slot reserves room for slightly more with ellipsis on overflow. Tooltip shows the full text on hover when truncated.
- **No timestamps in the message itself.** The slot is "what just happened?" — timestamp is implicit (now-ish).

### §6.3 — Persistence

- **Display duration**: until replaced by a new action OR 5 minutes elapse, whichever comes first.
- **Replacement is immediate**: a new ActionMessageEvent overwrites the current message without animation. Rapid actions (toggle universe twice quickly) produce two message replacements; each is briefly visible.
- **Fade**: after 5 minutes with no new action, the slot reverts to blank (no fade animation; instantaneous clear is fine).

### §6.4 — Event class

**`ActionMessageEvent`** in a new package or `com.teamgannon.trips.events`:

```java
public record ActionMessageEvent(Object source, String message) {
    public ActionMessageEvent {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("ActionMessageEvent message must not be blank");
        }
    }
}
```

Single field: the formatted message text. No category, no severity, no icon. Future enrichment lands as a new event subtype when an actual need surfaces (F.1's narrow-payload-ages-better lesson).

**Source parameter** follows Spring's `ApplicationEvent` convention even though the record-based event doesn't extend `ApplicationEvent`. Useful for log tracing.

### §6.5 — Relationship to existing `StatusUpdateEvent`

Step 1 audit: `StatusUpdateEvent` is published by ~11 sites today, all pushing transient text into the Plot Status label. The semantics already match action-message semantics: ephemeral, replaced-on-next, ad-hoc text. **Two options:**

(i) **Rename `StatusUpdateEvent` → `ActionMessageEvent`.** Touches 11 publish sites + the listener. Clean but invasive; existing tests + log messages reference the old name.

(ii) **Introduce `ActionMessageEvent` as the new canonical; keep `StatusUpdateEvent` as a deprecated alias.** StatusBarController's listener handles both; new publishers use ActionMessageEvent. Existing publishers can migrate opportunistically.

**Recommendation: (ii).** Migration is bounded; the new event has clean record-based shape; deprecation is the established pattern for non-urgent renames. The implementation conversation can decide whether to migrate the 11 sites in one pass or leave them for later.

---

## §7 — Implementation notes

### §7.1 — StatusBarController changes

- New FXML label: `actionMessage` (~280 px reserved width, left-aligned, ellipsis on overflow).
- Existing `databaseStatus` label and the `"Plot Status:"` literal are removed (their semantics absorbed into action message + new Dataset indicator).
- New FXML label pair: `datasetStatusLabel` + `datasetStatus`.
- Existing `routingStatusLabel` + `routingStatus` preserved (just repositioned).
- Existing `universeStatusLabel` + `universeStatus` preserved.
- New `@EventListener ActionMessageEvent` handler. FX-thread wrapped.
- New `@EventListener` for dataset-context-change (TBD which existing event, decided during Step 1 of the implementation conversation).
- Existing direct `routingStatus(boolean)` method kept for backward compat; also refactored to listen to `RoutingStatusEvent` (preferred path going forward).
- New 5-minute fade timer for action message: JavaFX `PauseTransition` with `setOnFinished` clearing the label. Each new ActionMessageEvent cancels-and-restarts the timer.

### §7.2 — Publish-site migration

Action messages must be added at each trigger site:

- `UniverseDesignerService.activate(id)` + `.deactivate(id)` — publish `ActionMessageEvent("Activated <name>")` / `("Deactivated <name>")` alongside the existing `UniverseActivationChangedEvent`.
- `MainSplitPaneManager` dataset operations — replace `StatusUpdateEvent` with `ActionMessageEvent` (or keep StatusUpdateEvent and let the controller bridge).
- `RouteEventHandler` — publish `ActionMessageEvent("Routing activated/deactivated")` alongside the routing state change.
- `PythonScriptEngine` script-start / script-complete — likely keep on StatusUpdateEvent path (deprecated alias path) since they're not in F.1's scope.

### §7.3 — Test list

| Test | What it pins |
|---|---|
| `ActionMessageEventTest` | Record-shape: non-null/non-blank invariant, source preserved, equality |
| `StatusBarActionMessageTest` | Listener wires ActionMessageEvent to `actionMessage` label; replacement on subsequent events; FX-thread safety |
| `StatusBarFadeTimerTest` | 5-min fade clears the slot; new event cancels-and-restarts; multiple rapid events extend the timer correctly |
| `StatusBarDatasetIndicatorTest` | ApplicationReadyEvent populates initial state; context-change event refreshes value + tooltip |
| `StatusBarRoutingIndicatorTest` | RoutingStatusEvent listener updates label + color; ApplicationReadyEvent picks up persisted routing state |
| `StatusBarUniverseIndicatorTest` | Existing F.1 Step 8 tests preserved unchanged |
| `StatusBarLayoutTest` | All four slots present + ordered correctly + action slot reserved width is preserved when empty |

Estimated ~25-30 net new tests.

### §7.4 — FXML changes

Whole replacement of `StatusBar.fxml`'s GridPane is cleaner than incremental edits. The current GridPane has 8 columns (worldbuilding work added 3 to the existing 5); the rationalization wants 7 columns (action + separator + 3 indicator-pairs + 2 inter-pair separators). Specifics decided during implementation.

---

## §8 — Out of scope

- **Renaming all 11 existing StatusUpdateEvent publishers** — migration is opt-in; deprecation marker is enough for now.
- **Animated transitions** for action message replacement or fade — instantaneous is fine; animation is later polish.
- **Severity coloring** (info / warning / error) on the action slot — future ActionMessageEvent subtype if needed; current scope is text only.
- **Click-to-dismiss** on the action message — could be added later; not required for F.1 alignment.
- **Persistent action message history** (a dropdown of recent messages) — out of scope; current slot shows only the most recent.
- **Editing the Dataset indicator** — read-only display; switching datasets happens elsewhere in the app.

---

## §9 — Success criteria

The rationalization is "complete" when:

1. The status bar shows four slots in order: action message, Dataset, Routing, Worldbuilding.
2. At app startup, the action slot is blank; the three persistent indicators reflect current persisted state.
3. Toggling a universe in the Worldbuilding > Universes dialog produces an `ActionMessageEvent` ("Activated <name>" / "Deactivated <name>") that appears in the action slot AND the Worldbuilding indicator updates simultaneously.
4. Loading a dataset produces an `ActionMessageEvent` AND the Dataset indicator updates.
5. Toggling routing produces an `ActionMessageEvent` AND the Routing indicator updates.
6. Rapid actions (toggle a universe twice quickly) produce two visible message replacements.
7. After 5 minutes of no new action, the slot reverts to blank.
8. Persistent indicators do not shift when actions arrive; the action slot's reserved width is preserved.
9. Each persistent indicator's tooltip displays a longer-form explanation.
10. F.1's `UniverseFilteringInvariantsTest` and other existing status-bar-touching tests stay green throughout.

These are testable observations. The implementation conversation lands them in 4–6 steps.

---

*End of design doc. Awaiting Larry's ratification before the implementation conversation begins.*
