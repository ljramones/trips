package com.terranrepublic.assets;

/**
 * The kind of transit a propulsion drive supports.
 *
 * <p>v2 Phase E.1 §4.1 — five canonical transit modes covering the discrete-vs-continuous and
 * sublight-vs-FTL axes that the route-finding integration (Phase E.3) needs to reason about.
 * Drives ({@link com.teamgannon.trips.spaceshipmodeller.propulsion.DriveType}) carry a
 * {@code Set<TransitMode>} declaring which transit kinds they support.
 *
 * <p>Each value represents a distinct transit physics:
 * <ul>
 *   <li>Continuous: {@link #SUBLIGHT}, {@link #WARP}</li>
 *   <li>Discrete: {@link #JUMP_POINT}, {@link #WORMHOLE}, {@link #JUMP_GATE}</li>
 * </ul>
 *
 * <p>The discrete/continuous distinction matters at the routing layer: continuous-mode travel
 * traces a single trajectory; discrete-mode travel composes star-pair hops through fixed
 * waypoints.
 */
public enum TransitMode {
    /**
     * Continuous in-system propulsion. Chemical, electric, nuclear-thermal, fusion, antimatter,
     * sail, ramjet, exotic-but-non-FTL drives. The dominant mode for the 22 of 25 catalogued
     * drives that aren't FTL-capable.
     */
    SUBLIGHT,

    /**
     * Discrete star-to-star FTL using natural jump points as focal points. Per-star deterministic
     * jump-point computation lives in Phase E.1's {@code JumpPointComputer}; ships with this
     * transit mode can use the jump points feature subsystem creates.
     */
    JUMP_POINT,

    /**
     * Discrete transit through paired wormhole mouths. Modeled as catalog-reference
     * {@code SolarSystemFeature}s; canonical mouth pairs populated in Phase E.2.
     */
    WORMHOLE,

    /**
     * Discrete network-membership-gated transit via constructed jump gates. Ships must have
     * transponder access to a {@code GateNetwork} to use its gates. Constructed gates are
     * {@code TRANSPORT_NODE} features carrying a {@code networkId}.
     */
    JUMP_GATE,

    /**
     * Continuous FTL travel through space (warp / hyperspace / Alcubierre-style). Strategic
     * interstellar transit without discrete focal points; routes trace a continuous path between
     * source and destination.
     */
    WARP
}
