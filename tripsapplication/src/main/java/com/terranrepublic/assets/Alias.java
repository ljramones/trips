package com.terranrepublic.assets;

import java.time.Instant;

/**
 * A worldbuilding overlay — a fictional name attached to a real astronomical target
 * ({@link AliasTargetKind#STAR StarObject} or {@link AliasTargetKind#EXOPLANET ExoPlanet})
 * within the context of a specific {@link Universe}.
 *
 * <p>Phase F.2 §4.1 — third {@code Cataloged}-but-not-{@code SpaceAsset} persisted entity after
 * GateNetwork (E.1) and Universe (F.1). Unlike catalog entries (where {@code universeId} is
 * nullable to mark canonical/real data), an Alias's {@code universeId} is <strong>required</strong>
 * — an alias without a universe is semantically meaningless. The FK constraint is NOT NULL,
 * ON DELETE CASCADE.
 *
 * <p>Distinct from the legacy {@code StarObject.aliasList} (Set&lt;String&gt; ElementCollection)
 * which holds catalog identifier variants (Simbad NAME, Bayer, HIP). That field is universe-
 * agnostic, populated by catalog import, and untouched by F.2. The Alias entity adds a separate
 * universe-scoped query path; the two systems coexist.
 *
 * <p>The {@code Cataloged} interface contract is satisfied as follows:
 * <ul>
 *   <li>{@link #id()}, {@link #description()}, {@link #universeId()} — auto-generated from
 *       record components</li>
 *   <li>{@link #name()} — returns {@link #aliasText} (the user-facing fictional name)</li>
 *   <li>{@link #source()}, {@link #faction()} — return empty string (aliases don't have
 *       upstream attribution or factions; they're universe-intrinsic)</li>
 *   <li>{@link #concealed()} — returns {@code false} (aliases follow universe activation, not
 *       per-alias concealment)</li>
 * </ul>
 *
 * @param id           catalog-alias-&lt;uuid&gt; (UUID-based; aliases are user-created and
 *                     don't have semantic slugs)
 * @param universeId   non-null FK to {@link Universe#id()}
 * @param targetKind   {@link AliasTargetKind#STAR} or {@link AliasTargetKind#EXOPLANET}
 * @param targetId     {@code StarObject.id} or {@code ExoPlanet.id} per kind
 * @param aliasText    the fictional name; e.g. "Vulcan", "Trantor"
 * @param description  optional worldbuilding context; max 1000 chars; defaults to ""
 * @param createdAt    creation timestamp
 * @param modifiedAt   last modification timestamp
 */
public record Alias(
        String id,
        String universeId,
        AliasTargetKind targetKind,
        String targetId,
        String aliasText,
        String description,
        Instant createdAt,
        Instant modifiedAt
) implements Cataloged {

    public Alias {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Alias id is required");
        }
        if (universeId == null || universeId.isBlank()) {
            throw new IllegalArgumentException("Alias universeId is required (aliases are universe-intrinsic)");
        }
        if (targetKind == null) {
            throw new IllegalArgumentException("Alias targetKind is required");
        }
        if (targetId == null || targetId.isBlank()) {
            throw new IllegalArgumentException("Alias targetId is required");
        }
        if (aliasText == null || aliasText.isBlank()) {
            throw new IllegalArgumentException("Alias aliasText is required");
        }
        description = description == null ? "" : description;
        Instant now = Instant.now();
        createdAt = createdAt == null ? now : createdAt;
        modifiedAt = modifiedAt == null ? createdAt : modifiedAt;
    }

    /**
     * Convenience constructor for fresh aliases — generates a UUID-based id and sets
     * timestamps to now.
     */
    public Alias(String universeId, AliasTargetKind targetKind, String targetId,
                 String aliasText, String description) {
        this("catalog-alias-" + java.util.UUID.randomUUID(),
                universeId, targetKind, targetId, aliasText, description,
                null, null);
    }

    // --------------------------------------------------- Cataloged overrides

    /**
     * Cataloged contract — the alias's user-facing name is its alias text. (The record's
     * {@code aliasText()} accessor remains the domain-specific reader; {@link #name()} is the
     * cross-cutting Cataloged surface.)
     */
    @Override
    public String name() {
        return aliasText;
    }

    /**
     * Cataloged contract — aliases don't have an upstream "source"; their source IS the
     * universe they belong to. Returns empty string. (The universe relationship is exposed via
     * {@link #universeId()} which auto-generates from the record component.)
     */
    @Override
    public String source() {
        return "";
    }

    /**
     * Cataloged contract — aliases don't have factions. Returns empty string. (Factions are a
     * universe-internal concept landing in F.3; alias-vs-faction is orthogonal.)
     */
    @Override
    public String faction() {
        return "";
    }

    /**
     * Cataloged contract — aliases are never concealed. Visibility is controlled by universe
     * activation state, not per-alias concealment. Always {@code false}.
     */
    @Override
    public boolean concealed() {
        return false;
    }
}
