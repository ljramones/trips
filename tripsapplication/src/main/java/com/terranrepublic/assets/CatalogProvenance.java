package com.terranrepublic.assets;

/**
 * Catalog metadata composite for a station: where it comes from and what its real-world or
 * in-universe documentary status is.
 *
 * <p>Composes three concepts into one record so they can move together and so future cross-
 * subtype consolidation (sharing this with {@link SpaceshipDesign}) is straightforward:
 * <ul>
 *   <li>{@link SourceType} — REAL / PROPOSED / SCIENCE_FICTION / UNKNOWN (relocated to
 *       {@code com.terranrepublic.assets} in v2 Phase D.6 step 1.5).</li>
 *   <li>{@code sourceUniverse} — free-text universe label
 *       (e.g. "Real / Proposed", "Troy Rising", "The Expanse").</li>
 *   <li>{@code sourceWork} — optional title of the specific work
 *       (e.g. "Babylon 5", "Legacy of the Aldenata"); may be {@code null}.</li>
 *   <li>{@link CatalogOperationalStatus} — HISTORIC / ACTIVE / PLANNED / CANCELLED / FICTIONAL /
 *       UNKNOWN.</li>
 * </ul>
 *
 * <p>Compact-constructor invariants:
 * <ol>
 *   <li>{@code sourceType} is non-null; defaults to {@link SourceType#UNKNOWN} if null.</li>
 *   <li>{@code sourceUniverse} is non-null; defaults to {@code ""} (empty string, not null) if
 *       null.</li>
 *   <li>{@code sourceWork} <em>may</em> be null — it's genuinely optional.</li>
 *   <li>{@code status} is non-null; defaults to {@link CatalogOperationalStatus#UNKNOWN}.</li>
 * </ol>
 *
 * @param sourceType     classification of the source (real / proposed / fictional / unknown)
 * @param sourceUniverse free-text universe label; never null, may be empty string
 * @param sourceWork     optional title of the specific work; may be {@code null}
 * @param status         catalog lifecycle status
 */
public record CatalogProvenance(
        SourceType sourceType,
        String sourceUniverse,
        String sourceWork,
        CatalogOperationalStatus status
) {

    public CatalogProvenance {
        if (sourceType == null) {
            sourceType = SourceType.UNKNOWN;
        }
        if (sourceUniverse == null) {
            sourceUniverse = "";
        }
        // sourceWork intentionally NOT defaulted — null is the documented "no specific work" value.
        if (status == null) {
            status = CatalogOperationalStatus.UNKNOWN;
        }
    }

    /**
     * Convenience factory for the all-defaults instance:
     * {@code (SourceType.UNKNOWN, "", null, CatalogOperationalStatus.UNKNOWN)}. Used as the
     * default value when {@link StationDesign}'s compact constructor must synthesize a
     * provenance for a legacy call site.
     *
     * @return a provenance with every documented default applied
     */
    public static CatalogProvenance unknown() {
        return new CatalogProvenance(SourceType.UNKNOWN, "", null, CatalogOperationalStatus.UNKNOWN);
    }
}
