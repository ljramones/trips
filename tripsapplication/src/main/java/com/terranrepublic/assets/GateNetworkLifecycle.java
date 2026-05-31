package com.terranrepublic.assets;

/**
 * Lifecycle state of a {@link GateNetwork}.
 *
 * <p>v2 Phase E.1 §5.2 — three values capturing whether the network is currently in use,
 * defunct, or rediscovered. The {@code ACTIVE} / {@code REACTIVATED} distinction is preserved
 * permanently rather than collapsed: a network that was lost and recovered carries different
 * worldbuilding flavor than one that has always been live, and the distinction may matter for
 * future gameplay/narrative work (e.g. reactivated-network discovery quests).
 */
public enum GateNetworkLifecycle {
    /**
     * Originally active, never derelict. The network has continuously functioned since its
     * construction. Default for new {@link GateNetwork} records.
     */
    ACTIVE,

    /**
     * Currently non-functional. The network exists structurally — its gates are persisted as
     * {@code SolarSystemFeature}s — but transit through it is unavailable until reactivation.
     */
    DERELICT,

    /**
     * Was derelict at some point; has been restored to operational status via transponder
     * discovery or similar in-world mechanism. Distinct from {@link #ACTIVE} so the recovered
     * provenance survives in the data model.
     */
    REACTIVATED
}
