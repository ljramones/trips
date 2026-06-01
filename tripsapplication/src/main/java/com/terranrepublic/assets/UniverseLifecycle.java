package com.terranrepublic.assets;

/**
 * Lifecycle state of a {@link Universe}.
 *
 * <p>v2 Phase F.1 §4.1 — two values capturing whether a universe is in regular use or marked for
 * removal. Distinct from {@link GateNetworkLifecycle}'s three-value scheme: a universe doesn't
 * have a "derelict" middle state — it's either available for activation or it's been deprecated
 * (probably because the user is winding down support for that fictional setting or it was
 * imported by mistake).
 *
 * <p>Deletion is a separate concern from deprecation: DEPRECATED preserves the row + its content
 * for reactivation/recovery; explicit delete (via the eventual Universe editor) removes the row
 * and ON DELETE SET NULL orphans the catalog entries that pointed at it.
 */
public enum UniverseLifecycle {
    /**
     * Normal state. The universe is selectable in the activation UI and ready for use. Default
     * for new {@link Universe} records.
     */
    AVAILABLE,

    /**
     * Marked for removal. The universe row still exists (and its content remains tagged), but it
     * should not be activated by default — the UI may dim it or hide it from the activation
     * surface. A subsequent delete operation (F.x) removes the row entirely; DEPRECATED is the
     * pre-removal hold state.
     */
    DEPRECATED
}
