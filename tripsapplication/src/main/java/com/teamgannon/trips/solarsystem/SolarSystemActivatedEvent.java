package com.teamgannon.trips.solarsystem;

import com.teamgannon.trips.model.SolarSystemDescription;

/**
 * Published by {@link com.teamgannon.trips.graphics.panes.SolarSystemSpacePane} immediately after
 * a system has been set as the currently-rendered system. Downstream listeners can use this hook
 * to perform per-activation work (e.g. computing + persisting jump-point features, the first
 * consumer added in v2 Phase E.1 §7).
 *
 * <p>The event fires synchronously on the publishing thread. Listeners that mutate the JavaFX
 * scene graph must therefore wrap their body in {@code FxThread.runOnFxThread(...)} per the
 * existing convention in {@code SolarSystemSpacePane}. Listeners that only touch persistence
 * (no scene-graph mutation) can run inline — the v2 Phase E.1 jump-point activation listener
 * is in this category.
 *
 * <p>By the time this event fires, the {@link SolarSystemDescription} is fully populated with
 * planets (via {@code SolarSystemService.getSolarSystem}). Listeners that need planet data for
 * their work — e.g. jump-point computation's Hill-sphere exclusion zones — can rely on
 * {@code system.getPlanetDescriptionList()} being authoritative.
 *
 * @param system the system that has just been activated for rendering; never null
 */
public record SolarSystemActivatedEvent(SolarSystemDescription system) {
}
