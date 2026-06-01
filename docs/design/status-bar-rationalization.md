# Status Bar Rationalization

**Status**: design, pre-implementation (revised post-Step-1 audit)
**Date**: 2026-06-01 (initial); 2026-06-01 (revised after Step 1 verification)
**Predecessor**: F.1 Step 8 (added `universeStatus` to `StatusBarController`)
**Successor**: implementation conversation (single conversation; 4 implementation steps after Step 1)

**Revision note (post-Step-1)**: Step 1 verification revealed `StatusUpdateEvent` has 70 publish sites across 19 unique classes (not ~11 as initially estimated), spanning three semantic categories (~30% past-tense actions, ~50% in-progress text, ~20% error feedback). The original design's introduction of a new `ActionMessageEvent` was solving a problem that doesn't actually exist — the slot's problems are presentation (truncation, no fade, mixed semantics in one label) and a missing Dataset indicator, neither of which require a new event class. The doc has been revised to drop the ActionMessageEvent introduction; StatusUpdateEvent stays as the canonical event for the action slot. §6.4 is preserved as a "considered + rejected" historical record. See §6.4 + §6.5 for the rationale.

---

## §1 — Purpose

The status bar today carries three indicators (Plot Status, Routing State, Worldbuilding) plus an ad-hoc text channel that publishers shove arbitrary messages into. The F.1 Step 7 screenshot surfaced the practical problem: at typical window widths the text truncates ("Real + 1 universe(s) act…"), and the channel mixes two semantically distinct concerns — *what state am I in?* and *what just happened?*

The rationalization gives each concern its own slot:

- **Action message** (transient): "what just happened?" — past-tense events surfaced briefly, then they fade.
- **Three persistent indicators**: "what state am I in?" — dataset, routing, worldbuilding. Each reflects current persisted state; survives restarts.

The user's mental model becomes consistent: the left slot is ephemeral; the right slots are durable.

---

## §2 — Current state

Step 1 audit (post-verification):

| Indicator | Today's backing | Today's update path |
|---|---|---|
| Plot Status | `databaseStatus` Label | `@EventListener StatusUpdateEvent` — **70 publish sites across 19 unique source classes**, spanning three semantic categories: ~30% past-tense actions ("Dataset loaded"), ~50% in-progress text ("Running script: foo.py"), ~20% error feedback ("Recenter failed") |
| Routing State | `routingStatus` Label | `@EventListener RoutingStatusEvent` in `RouteEventHandler` → bridges to `statusBarController.routingStatus(boolean)`. The event class exists + drives the indicator; the indirection through RouteEventHandler is the only smell. |
| Worldbuilding | `universeStatus` Label | `@EventListener UniverseActivationChangedEvent` + `@EventListener ApplicationReadyEvent` (F.1 Step 8) — direct on StatusBarController |

Three observations:

1. **The Plot Status label serves three semantic categories, not one.** The original design treated the slot as an action message channel; the audit reveals it actually carries past-tense actions, in-progress text, AND error feedback in the same label. The rationalization makes the slot work better as a *mixed-semantic* channel (reserved width, 5-min fade, blank at boot) rather than narrowing it to actions only.

2. **There's no Dataset indicator today.** The user has no persistent display of "which dataset is currently selected" — only the most-recent StatusUpdateEvent text, which gets overwritten by the next event. Adding a proper Dataset indicator is the largest user-visible improvement.

3. **Routing's event-driven path already works.** The doc initially assumed routing updated via direct method call from disparate sources; Step 1 confirmed `RoutingStatusEvent` exists and `RouteEventHandler` bridges it to the controller method. The cleanup is moving the listener from RouteEventHandler into StatusBarController directly (architectural consistency with how `UniverseActivationChangedEvent` is wired), not introducing event-driven semantics from scratch.

The rationalization, post-Step-1, is purely listener-side UI work:
- Reserved-width action slot (no shifting on message arrival)
- 5-minute fade (stale messages clear)
- Blank at startup (splash page owns boot messaging)
- New Dataset indicator (state surface completed)
- Routing listener moved to controller-direct (D2 cleanup)
- All three persistent indicators populate from `ApplicationReadyEvent` (uniform F.1 pattern)

No new event class. No publisher-site migration. The 70 StatusUpdateEvent publishers stay where they are; their messages continue to land in the action slot, now with proper presentation discipline.

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

## §6 — Action slot specifications

### §6.1 — What appears in the slot (descriptive, not prescriptive)

The slot displays whatever `StatusUpdateEvent` carries. Step 1's audit characterized the 70 publish-site usage as falling into three semantic categories:

| Category | Examples | Approx. share |
|---|---|---|
| Past-tense actions | "Dataset loaded is: HYG-30ly", "Activated Legacy of the Aldenata" (when published) | ~30% |
| In-progress text | "Running script: foo.py", "Saving new route…", "Computing transits…" | ~50% |
| Error feedback | "Recenter failed", "Display failed", "View switch failed" | ~20% |

The rationalization does not enforce a particular category. Publishers continue to write whatever message text best describes the moment. The slot's job is to display the most recent message with reasonable lifecycle (replaced or fades after 5 min).

**Future polish**: a follow-up task could add severity-aware styling (color the slot red for error feedback, blue for in-progress, green for completed actions). That requires either parsing the message text heuristically or adding category tagging to StatusUpdateEvent. Both are out of scope here; the slot stays semantically mixed.

### §6.2 — Format

- **Publisher-determined.** Each publisher writes the text it wants displayed. The original design's "past-tense verb leading" expectation only applies to the ~30% of publishers that are publishing actions; the other 70% follow their own conventions (in-progress text is typically gerund or present-tense; errors are typically `<thing> failed`).
- **Compact**: text longer than the slot width gets truncated with ellipsis; tooltip on the slot shows the full text on hover.
- **No timestamps in the message itself.** Implicit (now-ish).

### §6.3 — Persistence

- **Display duration**: until replaced by a new message OR 5 minutes elapse, whichever comes first.
- **Replacement is immediate**: a new StatusUpdateEvent overwrites the current message without animation. Rapid events produce rapid replacements; each briefly visible.
- **Fade**: after 5 minutes with no new message, the slot reverts to blank (no fade animation; instantaneous clear).
- **Boot state**: blank at startup. The splash page handles boot-time progress messaging; the status bar's action slot is for in-session events.

### §6.4 — Event class (considered + rejected)

The original design draft proposed introducing a new `ActionMessageEvent` record (single `String message` field, narrow payload) to give publishers a typed way to surface "user-initiated action just completed" messages. **Step 1 audit caused this to be rejected.** The reasoning, preserved here as a historical record for future readers:

The original hypothesis was that ~11 publishers were pushing past-tense actions through `StatusUpdateEvent` and a new typed event would let the slot become a dedicated "action channel" with the legacy event channel handling other concerns.

Step 1 falsified the hypothesis. The actual publisher count is 70 across 19 source classes, spanning three semantic categories. Introducing `ActionMessageEvent` would have:

- Required deciding which of the 70 sites to migrate (the ~30% that are genuinely "actions") and which to leave on legacy
- Left the legacy `StatusUpdateEvent` carrying the other 70% indefinitely (no realistic migration path for in-progress + error categories)
- Created two coexisting event channels both writing to the same slot
- Solved no concrete user-visible problem (the slot's actual problems are width, fade, and the missing Dataset indicator)

The (γ) pullback recognises that the slot's semantic mixing is a real but separable concern from the rationalization's user-visible improvements. Adding a new event class doesn't fix width, doesn't fix the missing Dataset indicator, doesn't fix the absent fade behavior. The right intervention is listener-side UI work, not a new event class.

Future severity-aware styling (if pursued) would either (a) parse message text heuristically or (b) add a category enum to StatusUpdateEvent's record. Either approach is more compatible with the 70-publisher reality than introducing a new event class that handles a minority of cases.

### §6.5 — Relationship to existing `StatusUpdateEvent`

`StatusUpdateEvent` IS the action slot's canonical event. No new event class is introduced. No publishers migrate. The slot's `@EventListener StatusUpdateEvent` continues to write into `databaseStatus` (renamed to the new action slot label during the StatusBarController refactor).

The improvements over today are all listener-side:
- Reserved-width slot prevents persistent indicators shifting on message arrival
- 5-minute `PauseTransition` fade clears stale messages
- Blank-at-startup avoids splash-page conflict
- Tooltip on overflow handles truncation discovery

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

**None.** Under the (γ) pullback, no publishers migrate. All 70 `StatusUpdateEvent` publish sites stay where they are, publishing what they publish today. The action slot listens to `StatusUpdateEvent` directly.

The original design draft contemplated 4 publisher migrations (UniverseDesignerService, MainSplitPaneManager, RouteEventHandler, PythonScriptEngine). All four sites continue using their current event channel:

- `UniverseDesignerService.activate()/.deactivate()` — publishes only `UniverseActivationChangedEvent` (drives the Worldbuilding indicator). The action slot reflects universe toggles via the indirect path: the dialog or other UI publishes a StatusUpdateEvent if it wants the toggle surfaced as an action message. F.1's UniversesDialog can opt into this in a small follow-up if desired, but is not required for this rationalization.
- `MainSplitPaneManager` — continues publishing StatusUpdateEvent for dataset/recenter/display/export ops.
- `RouteEventHandler` — continues publishing RoutingStatusEvent (drives the Routing indicator). The action slot doesn't get a "Routing activated" message unless someone publishes a StatusUpdateEvent for it; not required.
- `PythonScriptEngine` — continues publishing StatusUpdateEvent for script lifecycle.

### §7.3 — Test list

| Test | What it pins |
|---|---|
| `StatusBarActionSlotTest` | Listener wires `StatusUpdateEvent` to the action slot label; replacement on subsequent events; FX-thread safety; truncation triggers tooltip |
| `StatusBarFadeTimerTest` | 5-min `PauseTransition` clears the slot; new event cancels-and-restarts the timer; multiple rapid events behave correctly |
| `StatusBarDatasetIndicatorTest` | `ApplicationReadyEvent` populates initial state from `TripsContext.getDataSetContext().getDescriptor()`; `SetContextDataSetEvent` refreshes value + tooltip on context change |
| `StatusBarRoutingIndicatorTest` | Direct `@EventListener RoutingStatusEvent` (post-D2 cleanup) updates label + color; `ApplicationReadyEvent` picks up persisted routing state from the route service |
| `StatusBarUniverseIndicatorTest` | Existing F.1 Step 8 tests preserved unchanged (verifies the new layout doesn't regress the F.1 universe indicator) |
| `StatusBarLayoutTest` | All four slots present + ordered correctly + action slot reserved width is preserved when blank (no shifting) |

Estimated ~25 net new tests (down from the original 25–30 estimate because `ActionMessageEventTest` + publisher-side migration tests are no longer needed).

### §7.4 — FXML changes

Whole replacement of `StatusBar.fxml`'s GridPane is cleaner than incremental edits. The current GridPane has 8 columns (worldbuilding work added 3 to the existing 5); the rationalization wants 7 columns (action + separator + 3 indicator-pairs + 2 inter-pair separators). Specifics decided during implementation.

---

## §8 — Out of scope

- **Migrating any of the 70 `StatusUpdateEvent` publishers** — they stay where they are. No new event class is introduced.
- **Semantic differentiation of progress vs action vs error messages in the slot.** The slot continues to serve mixed semantics from existing StatusUpdateEvent publishers. Future UX work may add severity-aware styling (color-coding by category, separate slots, prioritization), but is not in scope here. That work needs its own design conversation.
- **Animated transitions** for slot replacement or fade — instantaneous is fine; animation is later polish.
- **Severity coloring** (info / warning / error) on the action slot — see §6.4 + §6.1; either heuristic text parsing or category enum on StatusUpdateEvent; out of scope for this rationalization.
- **Click-to-dismiss** on the action slot — not required.
- **Persistent action message history** (a dropdown of recent messages) — out of scope; current slot shows only the most recent.
- **Editing the Dataset indicator** — read-only display; switching datasets happens elsewhere in the app.
- **Modifying the splash page's boot messaging** — splash page is the boot-progress surface; this task only touches the in-session status bar.

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

These are testable observations. The implementation conversation lands them in 4 implementation steps after Step 1's verification audit (which has already landed; see commit `56661e6d`).

---

## §10 — Step structure (post-Step-1 revision)

| Step | Subject | Est. tests |
|---|---|---|
| 1 | Read-only verification audit (DONE — see Step 1 findings report) | 0 |
| 2 | `StatusBarController` refactor: action slot replaces Plot Status label; reserved width; 5-min `PauseTransition` fade timer; new Dataset indicator; Routing listener moved from RouteEventHandler bridge to controller-direct `@EventListener RoutingStatusEvent` (D2 cleanup) | ~12 |
| 3 | `StatusBar.fxml` whole-file rewrite for 4-slot layout (action + Dataset + Routing + Worldbuilding); column structure updated; existing widths/fonts preserved where unchanged | ~3 |
| 4 | Uniform `@EventListener(ApplicationReadyEvent.class)` initial-state for all three persistent indicators (Dataset, Routing, Worldbuilding); ensures persisted state surfaces on app boot rather than only on the next user-driven event | ~8 |
| 5 | Close-out: retroactive design doc note (this section is part of it; Step 5 also captures any divergences surfaced during Steps 2-4) | 0 |

**Step count revision**: 4 implementation steps under the (γ) pullback, down from the design's original 4–6 estimate. Smaller scope because no new event class + no publisher-site migration.

---

*End of design doc body. (γ) pullback revision ratified by Larry post-Step-1 (2026-06-01). Implementation ratified through Steps 2/4 and closed out in §11 below.*

---

## §11 — Close-out (Step 5 retroactive)

Implementation shipped 2026-06-01. Four commits on `master` between the design doc landing and this close-out:

| Commit | Subject |
|---|---|
| `56661e6d` | Design doc initial (pre-Step-1) |
| `c9b6f324` | Design doc revision — (γ) pullback per Step 1 audit findings |
| `68259ad2` | Step 2/3 combined — controller refactor + FXML rewrite + Dataset indicator + D2 cleanup |
| `77282a3e` | Step 4 — uniform `@EventListener(ApplicationReadyEvent.class)` initial state |

### §11.1 — (γ)-revised design held

The (γ) pullback decision (no new `ActionMessageEvent` class; `StatusUpdateEvent` stays canonical for the action slot) held through implementation without further substantive divergences. The Step 1 audit caught the architectural error (70 publishers across 19 classes, not ~11) before it cost any implementation cycles; revising the design pre-Step-2 was the right discipline. Same pattern as F.1's pre-Step-2 transport_node revision.

### §11.2 — Step 2/3 combination decision

The design doc's §10 listed Step 2 (controller refactor) and Step 3 (FXML rewrite) as separate steps. Per ratification preference, they were combined into a single commit (`68259ad2`):
- Cleaner single-step deliverable — the user-visible 4-slot layout lands all at once
- Screenshot verification at end of Step 2 exercises the full layout end-to-end (rather than Step 2 alone showing old FXML + new controller, then Step 3 alone showing new FXML + already-verified controller)
- Diff is larger but cognitively coherent (FXML change + controller change are co-dependent; readers wanting to understand the layout change look at both together)

### §11.3 — Defensive null-traversal pattern (canonical for FxWeaver controllers)

The Dataset indicator's `refreshDatasetStatusFromContext()` walks two nullable steps (`tripsContext.getDataSetContext().getDescriptor()`) defensively, with any null along the chain resolving to "(none selected)" via the existing `refreshDatasetStatus(null)` path. This is the canonical pattern for FxWeaver-instantiated controllers that field-inject Spring services with `@Autowired(required = false)`:

1. The injected service itself may be null in test-harness scenarios (no Spring context).
2. The service's return value may be null (no current context / context not yet initialized).
3. The nested chain may have intermediate nulls (DataSetContext may exist but its descriptor is null).

Each step gets an explicit guard. The fallback (null → "(none selected)" via the existing `refreshDatasetStatus` method) is the same code path the SetContextDataSetEvent listener takes when handed a null descriptor — no duplication.

**Pin this pattern for future FxWeaver-instantiated controllers**: field injection with `required = false` + defensive null traversal at every step + delegate to the same code path the event-listener uses. F.1 Step 8 introduced it for `UniverseDesignerService`; Step 4 here applies it to `TripsContext`. Future status-bar / panel work follows the same shape.

### §11.4 — Minor finding: int-to-Long test fix

While writing `StatusBarApplicationReadyTest`, the compile errored on `descriptor.setNumberStars(12_847)` — `DataSetDescriptor.setNumberStars` takes a boxed `Long`, not an int. Java's autoboxing doesn't widen `int` to `Long` (only to `Integer`); the call needed an `L` suffix (`12_847L`). One-character fix; mentioned here because it's the kind of test-fixture friction that's worth pinning so the next test against `DataSetDescriptor` doesn't re-hit it.

### §11.5 — Test count summary

Net status bar tests:

| Step | Tests | File(s) |
|---|---|---|
| 1 | 0 | Read-only verification (no test artifact) |
| 2 | 25 | StatusBarActionSlotTest (6) + StatusBarFadeTimerTest (7) + StatusBarDatasetIndicatorTest (7) + StatusBarRoutingIndicatorTest (5) |
| 4 | 7 | StatusBarApplicationReadyTest (7) |
| 5 | 0 | Doc-only |
| **Total** | **32** | |

Suite went from 3,948 (F.1 close) → 3,980 (Step 4 close). +32 tests = +0.81% to the suite for the four user-visible improvements (Dataset indicator added, action slot reserved width, action slot 5-min fade, blank at startup, uniform boot-time state restoration across three indicators).

### §11.6 — Architecture pins (status-bar-specific)

After this rationalization, the status bar's architectural shape is:

- **Four-slot layout**: action message + Dataset + Routing + Worldbuilding
- **Reserved-width action slot** (280 px) prevents persistent indicators from shifting
- **5-min `PauseTransition` fade** on the action slot; cancel-and-restart on each new `StatusUpdateEvent`
- **Three persistent indicators populate from `@EventListener(ApplicationReadyEvent.class)` at boot** (the F.1 Step 8 pattern, now uniform)
- **Event-driven updates** for all four slots:
  - Action slot ← `StatusUpdateEvent` (70 existing publishers; unchanged)
  - Dataset ← `SetContextDataSetEvent` + boot-time read of `TripsContext.getDataSetContext().getDescriptor()`
  - Routing ← `RoutingStatusEvent` (D2 cleanup — listener now on controller directly, not bridged via RouteEventHandler) + direct `routingStatus(boolean)` API preserved for synchronous callbacks
  - Worldbuilding ← `UniverseActivationChangedEvent` + boot-time read of `UniverseDesignerService.findAllActive()` (F.1 Step 8 unchanged)
- **FxThread.runOnFxThread wrap** on every listener — defensive even when Step 5 events are FX-thread-synchronous today

### §11.7 — Out of scope (still)

Per the design doc §8, deferred to future tasks:

- Semantic differentiation of progress vs action vs error messages in the action slot (severity-aware styling)
- Animated transitions for slot replacement or fade
- Click-to-dismiss on the action slot
- Persistent action message history (dropdown of recent messages)
- Editing the Dataset indicator inline

None of these block this rationalization's shipped value.

---

*End of close-out. Status bar rationalization ships. Local commits `56661e6d`, `c9b6f324`, `68259ad2`, `77282a3e` pending push to origin/master.*
