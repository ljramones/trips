package com.terranrepublic.assets;

/**
 * How gravity is provided inside a {@link com.terranrepublic.assets.Megastructure}'s interior.
 *
 * <p>Stations don't model this dimension — they're too small for gravity to be a
 * design-significant axis. Megastructures routinely span the range from "intact
 * planetary mass" (Dahak, Troy — a hollowed moon or asteroid retains its
 * gravitational well) to "rotating cylinder" (Rama, O'Neill cylinders at
 * megastructure scale) to "advanced-tech artificial field" (Culture Orbitals at
 * 1g via centripetal acceleration around the central star, or Death Star via
 * unspecified field generation).
 *
 * <p>v2 Phase D.7 §3.1 — only meaningful when {@code hasInteriorSetting} is true.
 */
public enum InteriorGravityType {
    /**
     * The natural mass of the body provides interior gravity.
     * <p>Examples: Dahak (hollowed Sol-system-mass moon), Troy (23 km nickel-iron
     * asteroid retains a small but non-zero gravitational pull), Centerpoint.
     */
    NATURAL_MASS,

    /**
     * Spin / centripetal acceleration provides interior gravity.
     * <p>Examples: Rama (50 km cylinder rotating to provide ~1g at the inner hull),
     * O'Neill cylinders, Culture Orbitals (a ring spinning around the star).
     */
    SPIN,

    /**
     * Engineered artificial gravity (sufficiently advanced technology, mechanism
     * unspecified).
     * <p>Examples: Death Star, most space-opera capital structures.
     */
    ARTIFICIAL_FIELD,

    /**
     * A mixture of multiple sources — natural mass plus spin, artificial field
     * augmenting spin, etc.
     * <p>Examples: Bowl of Heaven (spin for habitat layer, gravitational anchoring
     * to its star); Shellworlds (multi-layer hybrid).
     */
    MIXED,

    /**
     * No gravity in the interior — micro-g or free-fall throughout.
     * <p>Examples: an unspun cylindrical hab; a derelict object with no spin and
     * insufficient mass.
     */
    NONE,

    /**
     * Interior gravity mechanism not determined or not described in the source
     * material.
     */
    UNKNOWN
}
