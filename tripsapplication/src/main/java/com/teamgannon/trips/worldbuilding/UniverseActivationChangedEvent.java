package com.teamgannon.trips.worldbuilding;

import com.terranrepublic.assets.Universe;

/**
 * Spring application event published when a {@link Universe}'s activation state changes via
 * {@code UniverseDesignerService.activate(String)} or {@code deactivate(String)}.
 *
 * <p>v2 Phase F.1 §5.2 — the runtime signal that connects the data layer (Steps 2-4: Universe
 * entity + universe_id FK + V17 migration) to consumer layers (Step 6: UniverseFilteringService;
 * Step 8: status bar indicator; future F.x renderer overlays).
 *
 * <p>Always-publish semantics: every call to {@code activate(id)} or {@code deactivate(id)}
 * publishes an event, regardless of whether the activation state actually changed. This
 * preserves the "service publishes when called" contract and lets listeners decide whether to
 * deduplicate (e.g. cache invalidation on every event vs. only on state transitions). The
 * service-side cost of a redundant publish is negligible; the consumer-side cost of a missed
 * event would be a stale UI.
 *
 * <p>Threading: Spring delivers events synchronously on the publishing thread.
 * {@code UniverseDesignerService.activate/deactivate} run on the FX thread (called from the
 * Worldbuilding menu in Step 7), so listeners receive on the FX thread. No
 * {@code FxThread.runOnFxThread} wrap required in F.1 listeners. The defensive wrap pattern
 * (per E.1 Step 6's discipline) only becomes mandatory when off-FX-thread publishers appear.
 *
 * <p>Event payload semantics: {@link #universe} is the <strong>post-toggle</strong> Universe
 * record — its {@link Universe#active()} reflects the new state, matching {@link #nowActive}.
 * Listeners can use either; they're equivalent. The {@code nowActive} parameter is convenience
 * for listeners that only care about the new state without unpacking the full record.
 *
 * @param universe   the universe whose activation state changed (post-toggle state)
 * @param nowActive  the new activation state (equal to {@code universe.active()})
 */
public record UniverseActivationChangedEvent(Universe universe, boolean nowActive) {

    public UniverseActivationChangedEvent {
        if (universe == null) {
            throw new IllegalArgumentException("UniverseActivationChangedEvent universe must not be null");
        }
        if (universe.active() != nowActive) {
            throw new IllegalArgumentException(
                    "nowActive (" + nowActive + ") must match universe.active() ("
                            + universe.active() + ") -- the event payload is the post-toggle state");
        }
    }
}
