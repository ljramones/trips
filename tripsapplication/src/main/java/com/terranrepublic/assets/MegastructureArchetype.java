package com.terranrepublic.assets;

/**
 * The primary categorization key for a {@link com.terranrepublic.assets.Megastructure}.
 *
 * <p>The taxonomy runs along two axes — <em>origin</em> (built vs. found) and
 * <em>mode</em> (weapon vs. wonder/habitat) — yielding five canonical archetypes.
 * Most fictional megastructures land cleanly in one archetype; genuine boundary
 * cases (Janus moving from disguised-moon to big-dumb-object as its nature is
 * revealed; the Bowl of Heaven as both engineered world and mobile under power)
 * pick the primary archetype with the secondary character captured by the
 * Megastructure record's other fields (origin type, mobility, function).
 *
 * <p>v2 Phase D.7 §2 — archetype is analogous to {@code StationType} for stations:
 * the primary structural-kind key for the catalog entry.
 */
public enum MegastructureArchetype {
    /**
     * An object disguised as a natural body; secretly a machine.
     * <p>Examples: Dahak (<em>Empire from the Ashes</em>), Janus (<em>Pushing Ice</em>).
     */
    DISGUISED_MOON,

    /**
     * A built war machine at scale-class size.
     * <p>Examples: Death Star, Iserlohn (<em>Legend of the Galactic Heroes</em>),
     * Centerpoint, Mycroft (<em>Honorverse</em>), Star Forge, Halo MAC platforms,
     * Phalanx (Warhammer 40K).
     */
    PURPOSE_BUILT_FORT,

    /**
     * A rock body melted, hollowed, or otherwise repurposed into a fortress or installation.
     * <p>Examples: Troy (<em>Troy Rising</em>), Gundam asteroid forts (Solomon, A Baoa Qu,
     * Axis), Hektor variants.
     */
    CONVERTED_ASTEROID,

    /**
     * A found alien enigma whose purpose is unknown or unclear.
     * <p>Examples: Rama, Thistledown (<em>Eon</em>), Gateway / Heechee artifacts,
     * the Great Ship (<em>Marrow</em>), the 2001 monoliths at scale.
     */
    BIG_DUMB_OBJECT,

    /**
     * A habitat-scale or world-scale engineered structure: the object <em>is</em> the world.
     * <p>Examples: Ringworld, Orbitsville, Bowl of Heaven, Culture Orbitals,
     * Culture Shellworlds (<em>Matter</em>), Xeelee Ring.
     */
    ENGINEERED_WORLD,

    /**
     * Archetype not yet determined. Reserved for genuine mysteries: precursor objects
     * discovered with no understood archetype. Catalog seeds should not use this value;
     * it exists for parser/import paths that lack archetype information at load time.
     */
    UNKNOWN
}
