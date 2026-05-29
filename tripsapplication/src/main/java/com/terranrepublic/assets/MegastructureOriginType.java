package com.terranrepublic.assets;

/**
 * Where a {@link com.terranrepublic.assets.Megastructure} came from — built vs. found,
 * and (if built) by whom.
 *
 * <p>The origin axis is independent of the {@link MegastructureArchetype}: a
 * PURPOSE_BUILT_FORT may be BUILT_BY_KNOWN (Death Star — Galactic Empire) or
 * BUILT_BY_UNKNOWN (a precursor fortress); a BIG_DUMB_OBJECT is typically
 * FOUND_INTACT or FOUND_DAMAGED.
 *
 * <p>v2 Phase D.7 §3.1 — drives which auxiliary fields are meaningful on the
 * record: {@code builderPolity} matters for BUILT_BY_KNOWN; {@code discoveryYear}
 * matters for FOUND_*; {@code constructionYear} matters for BUILT_* when known.
 */
public enum MegastructureOriginType {
    /**
     * Built by a known polity or civilization, in a known era.
     * <p>Examples: Death Star (Galactic Empire), Troy (Solar Confederation),
     * Centerpoint (built but builders forgotten — borderline; if records survive,
     * still BUILT_BY_KNOWN).
     */
    BUILT_BY_KNOWN,

    /**
     * Built by an unknown civilization, possibly ancient or extinct.
     * <p>Examples: Forerunner installations (<em>Halo</em>), Engineer artifacts
     * (<em>Prometheus</em>), the Citadel (<em>Mass Effect</em>) — when the in-universe
     * builders are unknown to the discoverers.
     */
    BUILT_BY_UNKNOWN,

    /**
     * Discovered already operational, in working order.
     * <p>Examples: Rama (arrives functional), Thistledown (active when entered),
     * an intact precursor object encountered.
     */
    FOUND_INTACT,

    /**
     * Discovered in derelict, damaged, or partial state.
     * <p>Examples: most precursor wrecks; ancient megastructures that have suffered
     * combat damage or aeons of decay.
     */
    FOUND_DAMAGED,

    /**
     * Origin not yet determined. Reserved for catalog entries where the source
     * material is ambiguous or the in-universe history is unrevealed.
     */
    UNKNOWN
}
