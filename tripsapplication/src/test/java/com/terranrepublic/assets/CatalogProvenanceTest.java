package com.terranrepublic.assets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Pins {@link CatalogProvenance}'s compact-constructor invariants and the {@link
 * CatalogProvenance#unknown()} factory shape per the v2 Phase D.6 design §4.2.
 */
class CatalogProvenanceTest {

    @Test
    @DisplayName("null sourceType defaults to SourceType.UNKNOWN")
    void nullSourceTypeDefaultsToUnknown() {
        CatalogProvenance p = new CatalogProvenance(null, "Some Universe", "Some Work",
                CatalogOperationalStatus.FICTIONAL);
        assertSame(SourceType.UNKNOWN, p.sourceType());
    }

    @Test
    @DisplayName("null sourceUniverse defaults to the empty string (not null)")
    void nullSourceUniverseDefaultsToEmptyString() {
        CatalogProvenance p = new CatalogProvenance(SourceType.REAL, null, null,
                CatalogOperationalStatus.ACTIVE);
        assertNotNull(p.sourceUniverse(), "sourceUniverse must never be null after construction");
        assertEquals("", p.sourceUniverse());
    }

    @Test
    @DisplayName("null sourceWork is preserved (the field is genuinely optional)")
    void nullSourceWorkPreserved() {
        CatalogProvenance p = new CatalogProvenance(SourceType.SCIENCE_FICTION, "Babylon 5", null,
                CatalogOperationalStatus.FICTIONAL);
        assertNull(p.sourceWork(),
                "sourceWork null is the documented \"no specific work\" value; must be preserved");
    }

    @Test
    @DisplayName("null status defaults to CatalogOperationalStatus.UNKNOWN")
    void nullStatusDefaultsToUnknown() {
        CatalogProvenance p = new CatalogProvenance(SourceType.REAL, "Real / Proposed", null, null);
        assertSame(CatalogOperationalStatus.UNKNOWN, p.status());
    }

    @Test
    @DisplayName("CatalogProvenance.unknown() returns the all-defaults shape")
    void unknownFactoryReturnsAllDefaults() {
        CatalogProvenance p = CatalogProvenance.unknown();
        assertSame(SourceType.UNKNOWN, p.sourceType());
        assertEquals("", p.sourceUniverse());
        assertNull(p.sourceWork());
        assertSame(CatalogOperationalStatus.UNKNOWN, p.status());
    }

    @Test
    @DisplayName("non-default sourceWork round-trips intact (no silent rewrite by compact constructor)")
    void nonDefaultSourceWorkRoundTrips() {
        CatalogProvenance p = new CatalogProvenance(SourceType.SCIENCE_FICTION, "Babylon 5",
                "Babylon 5", CatalogOperationalStatus.FICTIONAL);
        assertEquals("Babylon 5", p.sourceWork(),
                "non-null sourceWork must not be silently rewritten by the compact constructor");
    }
}
