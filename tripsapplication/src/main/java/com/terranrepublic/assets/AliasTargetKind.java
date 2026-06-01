package com.terranrepublic.assets;

/**
 * Discriminator for {@link Alias#targetId()}: which catalog table the polymorphic reference
 * points at. Two values today; future expansion (moons, asteroid belts, etc.) deferred per
 * Phase F.2 §4.2.
 *
 * <p>Mirrors the {@code CatalogedKind} pattern from F.1 + the {@code SolarSystemFeature
 * .catalogReferenceKind} pattern from E.1 Step 4 — discriminator + scalar id is the established
 * polymorphic-reference shape in the codebase, in preference to {@code @MappedSuperclass}
 * complexity or JPA polymorphic joins.
 */
public enum AliasTargetKind {
    /** {@code targetId} references {@code StarObject.id} in the HYG-imported catalog. */
    STAR,
    /** {@code targetId} references {@code ExoPlanet.id} (either REAL provenance or PROMOTED-from-ACRETE). */
    EXOPLANET
}
