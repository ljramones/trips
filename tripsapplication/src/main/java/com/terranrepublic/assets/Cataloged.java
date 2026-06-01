package com.terranrepublic.assets;

/**
 * Shared identity and provenance contract for anything listed in a Terran Republic catalog.
 */
public interface Cataloged {

    String id();

    String name();

    String source();

    String faction();

    boolean concealed();

    String description();

    /**
     * Worldbuilding universe affiliation. {@code null} (the default) means this entry is
     * canonical / real-data and visible regardless of which fictional universes the user has
     * activated. A non-null value is the id of a {@link Universe} row (by convention,
     * {@code "catalog-universe-<slug>"}); the entry is visible only when that universe is active.
     *
     * <p>v2 Phase F.1 §4.4 — the universal scope-carrier for the Worldbuilding Platform. Per-
     * subtype overrides on each persisted Cataloged implementation (SpaceshipDesign,
     * StationDesign, WeaponInstallation, Megastructure, GateNetwork, TransportNode) surface the
     * value from a {@code universeId} record component. Subtypes without a backing field
     * (notably {@link Universe} itself + {@code Conduit}) inherit the {@code null} default —
     * universes don't have a parent universe, and Conduit has no JPA persistence and so no
     * activation semantics in F.1.
     *
     * <p>The return type is nullable {@code String} (not {@code Optional<String>}) to keep the
     * Cataloged interface uniformly non-Optional and to align with the JPA column shape
     * (nullable VARCHAR). Callers preferring Optional handling can wrap via
     * {@code Optional.ofNullable(cataloged.universeId())}.
     *
     * @return universe id this entry is scoped to, or {@code null} for canonical/real entries
     */
    default String universeId() {
        return null;
    }
}
