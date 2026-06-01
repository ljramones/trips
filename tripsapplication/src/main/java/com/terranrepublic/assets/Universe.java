package com.terranrepublic.assets;

import java.time.Instant;

/**
 * A worldbuilding universe — a bounded collection of fiction-specific content.
 *
 * <p>v2 Phase F.1 §4.1 — the foundational entity of the Worldbuilding Platform. Every catalog
 * entry (ship, station, weapon, megastructure, transport node, gate network) optionally
 * references a Universe via its {@code universe_id} FK; entries with no reference are canonical
 * / real-data and always visible. Entries scoped to a Universe are visible only when that
 * Universe's {@link #active} flag is {@code true}.
 *
 * <p>Like {@link GateNetwork}, {@code Universe} sits outside the {@link SpaceAsset} /
 * {@link SpaceInfrastructure} sealed hierarchies. It implements {@link Cataloged} for catalog
 * uniformity but does not extend either sealed branch — a universe is a worldbuilding scope, not
 * a physical thing.
 *
 * <p>The {@link #active} flag persists across application restarts (R5.2) and updates fire a
 * {@code UniverseActivationChangedEvent} (added in F.1 Step 5). The flag lives on the entity
 * itself rather than in a separate activation table — TRIPS is single-user, so a per-user
 * activation table would be wasted complexity (per design doc §5.1).
 *
 * <p>Universes do NOT carry {@link CatalogProvenance}: a universe IS the provenance scope, so
 * attributing it to another universe would be circular. The {@link #source()} accessor returns
 * empty string per the Cataloged contract.
 *
 * @param id            stable catalog id, by convention {@code "catalog-universe-<slug>"}
 * @param name          the canonical title (e.g. "Legacy of the Aldenata", "Star Trek")
 * @param description   free-form descriptive prose; never null, may be empty
 * @param sourceAuthor  the in-world author/creator (e.g. "John Ringo", "Larry Mitchell"); never
 *                      null, may be empty; surfaces through {@link #faction()} per the Cataloged
 *                      contract
 * @param version       semantic version of the universe definition (defaults to "1.0")
 * @param lifecycle     {@code AVAILABLE} / {@code DEPRECATED}
 * @param active        whether this universe is currently active (visible) in the user's view
 */
public record Universe(
        String id,
        String name,
        String description,
        String sourceAuthor,
        String version,
        UniverseLifecycle lifecycle,
        boolean active
) implements Cataloged {

    public Universe {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Universe id is required");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Universe name is required");
        }
        lifecycle = lifecycle == null ? UniverseLifecycle.AVAILABLE : lifecycle;
        description = description == null ? "" : description;
        sourceAuthor = sourceAuthor == null ? "" : sourceAuthor;
        version = version == null || version.isBlank() ? "1.0" : version;
    }

    /**
     * Convenience constructor for creating a new available, inactive universe with minimal
     * metadata. Used by the V15 migration's INSERT statements and the future Create-Universe
     * dialog.
     */
    public Universe(String id, String name, String description, String sourceAuthor) {
        this(id, name, description, sourceAuthor, "1.0", UniverseLifecycle.AVAILABLE, false);
    }

    // --------------------------------------------------- Cataloged overrides

    /**
     * Cataloged-interface source accessor. Returns empty string — a universe IS the source, with
     * no upstream attribution. Distinct from {@link GateNetwork#source()} which reads from
     * provenance because GateNetwork has provenance and Universe does not.
     */
    @Override
    public String source() {
        return "";
    }

    /**
     * Cataloged-interface faction accessor. Returns {@link #sourceAuthor} — the universe's
     * author is the closest analogue to a "faction" at the universe-level meta-data tier. (The
     * faction concept proper, for content within a universe, lands in F.3 as a dedicated entity.)
     */
    @Override
    public String faction() {
        return sourceAuthor;
    }

    /**
     * Cataloged-interface concealed accessor. Always {@code false} — universes are inherently
     * visible (the activation flag controls visibility, not concealment).
     */
    @Override
    public boolean concealed() {
        return false;
    }

    /**
     * Returns a copy of this universe with the {@link #active} flag set to the given value.
     * Records are immutable; the activation service uses this to derive a new domain record
     * after flipping the entity's column.
     */
    public Universe withActive(boolean newActive) {
        return new Universe(id, name, description, sourceAuthor, version, lifecycle, newActive);
    }
}
