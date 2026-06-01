package com.terranrepublic.assets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * v2 Phase F.1 Step 4 — universe-tagging audit invariants over {@link Catalog#all()}.
 *
 * <p>Distinct from {@link CatalogAuditTest} (which covers Phase D.6's primaryFunction +
 * provenance invariants) and from {@code V17MigrationAuditTest} (which covers the
 * post-Flyway DB-level state). This test is the in-memory contract: the 5 fiction-canon
 * Catalog constants must carry their universe affiliation, and real entries must NOT.
 *
 * <p>Without this audit, the V17 migration's UPDATE statements would correctly tag
 * pre-existing DB rows but the Catalog seeder (which runs post-Flyway) would insert
 * fresh-install rows with {@code universe_id = null}, silently dropping universe scoping
 * for canonical fiction content. The cost of getting this wrong is invisible at install
 * time (everything looks like real data) — hence the audit.
 */
class CatalogUniverseAuditTest {

    private static final String LEGACY = "catalog-universe-legacy-of-the-aldenata";

    // ============================================================
    // Per-entry assertions (specific known-fiction entries)
    // ============================================================

    @Test
    @DisplayName("Catalog.TROY is scoped to Legacy of the Aldenata")
    void troyHasLegacyUniverseId() {
        assertEquals(LEGACY, Catalog.TROY.universeId(),
                "TROY (Troy Rising megastructure) must be tagged with Legacy of the Aldenata");
    }

    @Test
    @DisplayName("Catalog.SAPL is scoped to Legacy of the Aldenata")
    void saplHasLegacyUniverseId() {
        assertEquals(LEGACY, Catalog.SAPL.universeId(),
                "SAPL (Solar Array Pumped Laser) must be tagged with Legacy of the Aldenata");
    }

    @Test
    @DisplayName("Catalog.SHEVA_GUN is scoped to Legacy of the Aldenata")
    void shevaGunHasLegacyUniverseId() {
        assertEquals(LEGACY, Catalog.SHEVA_GUN.universeId(),
                "SheVa Gun must be tagged with Legacy of the Aldenata");
    }

    @Test
    @DisplayName("Catalog.POSLEEN_COMMAND_DODECAHEDRON is scoped to Legacy of the Aldenata")
    void posleenCommandHasLegacyUniverseId() {
        assertEquals(LEGACY, Catalog.POSLEEN_COMMAND_DODECAHEDRON.universeId());
    }

    @Test
    @DisplayName("Catalog.POSLEEN_BATTLE_DODECAHEDRON is scoped to Legacy of the Aldenata")
    void posleenBattleHasLegacyUniverseId() {
        assertEquals(LEGACY, Catalog.POSLEEN_BATTLE_DODECAHEDRON.universeId());
    }

    // ============================================================
    // Global assertions
    // ============================================================

    @Test
    @DisplayName("every catalog entry with sourceType=REAL has universeId() == null")
    void realEntriesHaveNullUniverseId() {
        List<String> offenders = new ArrayList<>();
        for (Cataloged c : Catalog.all()) {
            SourceType srcType = sourceTypeOf(c);
            if (srcType == SourceType.REAL && c.universeId() != null) {
                offenders.add(c.name() + " (" + c.getClass().getSimpleName()
                        + ") has universeId=" + c.universeId()
                        + " — real entries must be universe-scope-NULL (R1.9/R1.10)");
            }
        }
        if (!offenders.isEmpty()) {
            fail("Real entries must have universeId() == null:\n  - " + String.join("\n  - ", offenders));
        }
    }

    @Test
    @DisplayName("Legacy of the Aldenata is the only universe scope present in Catalog today")
    void onlyLegacyUniverseInCatalog() {
        // F.1 ships 5 fiction-canon constants (TROY, SAPL, SHEVA_GUN, 2 Posleen). All Posleen-
        // War content. No other universe-scoped Catalog constants exist today; F.2+ may add.
        Set<String> universes = new java.util.HashSet<>();
        for (Cataloged c : Catalog.all()) {
            if (c.universeId() != null) {
                universes.add(c.universeId());
            }
        }
        assertEquals(Set.of(LEGACY), universes,
                "F.1 ships only Legacy-of-the-Aldenata-scoped fiction content in Catalog; "
                        + "found: " + universes);
    }

    @Test
    @DisplayName("exactly 5 fiction-canon entries (TROY + SAPL + SHEVA_GUN + 2 Posleen) carry a universeId")
    void exactly5FictionCanonEntries() {
        long fictionCount = Catalog.all().stream()
                .filter(c -> c.universeId() != null)
                .count();
        assertEquals(5L, fictionCount,
                "F.1 ships 5 fiction-canon Catalog constants (Troy + SAPL + SheVa Gun + 2 Posleen "
                        + "Dodecahedra). Catalog.all() has " + fictionCount
                        + " universe-scoped entries — count drift indicates a Catalog change that "
                        + "needs Step 4 attention.");
    }

    @Test
    @DisplayName("every fiction-canon entry's universeId references a valid catalog-universe- slug")
    void fictionUniverseIdsFollowSlugConvention() {
        List<String> offenders = new ArrayList<>();
        for (Cataloged c : Catalog.all()) {
            String universeId = c.universeId();
            if (universeId != null && !universeId.startsWith("catalog-universe-")) {
                offenders.add(c.name() + " has universeId='" + universeId
                        + "' — must start with 'catalog-universe-' (§12 naming convention)");
            }
        }
        if (!offenders.isEmpty()) {
            fail("Universe ids must follow the catalog-universe-<slug> convention:\n  - "
                    + String.join("\n  - ", offenders));
        }
    }

    // ============================================================
    // Helpers
    // ============================================================

    /**
     * Extract the SourceType from any Cataloged subtype. Different subtypes expose provenance
     * differently — SpaceshipDesign has a top-level {@code sourceType()} accessor; others wrap
     * it in {@link CatalogProvenance}. This helper unifies the read.
     */
    private static SourceType sourceTypeOf(Cataloged c) {
        if (c instanceof SpaceshipDesign s) return s.sourceType();
        if (c instanceof StationDesign s) return s.provenance().sourceType();
        if (c instanceof WeaponInstallation w) {
            // WeaponInstallation didn't get the D.6 provenance refactor; it has only a `source`
            // String. Treat "Real / Proposed" or "Real" as REAL; everything else as non-REAL.
            String src = w.source();
            return ("Real / Proposed".equals(src) || "Real".equals(src))
                    ? SourceType.REAL : SourceType.SCIENCE_FICTION;
        }
        if (c instanceof Megastructure m) return m.provenance().sourceType();
        if (c instanceof GateNetwork g) return g.provenance().sourceType();
        // Other Cataloged subtypes (Universe, Conduit, TransportNode) aren't in Catalog.all().
        return SourceType.UNKNOWN;
    }
}
