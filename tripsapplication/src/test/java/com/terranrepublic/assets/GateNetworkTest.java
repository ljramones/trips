package com.terranrepublic.assets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * v2 Phase E.1 §5 — pins {@link GateNetwork}'s contract: compact-constructor defaults, the
 * {@link Cataloged} interface overrides (source/faction/concealed), and the deliberate
 * non-membership in the sealed {@link SpaceAsset} hierarchy.
 */
class GateNetworkTest {

    private static GateNetwork sample() {
        return new GateNetwork(
                "catalog-network-test",
                "Test Network",
                "Test Polity",
                GateNetworkLifecycle.ACTIVE,
                "TEST-XPDR",
                "A test gate network.",
                "INFERRED: synthetic test fixture.",
                "test-category",
                new CatalogProvenance(SourceType.SCIENCE_FICTION, "Test Universe", "Test Work",
                        CatalogOperationalStatus.FICTIONAL),
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-06-01T00:00:00Z"));
    }

    // ------------------------------------------------------------ defaults

    @Test
    @DisplayName("lifecycle defaults to ACTIVE when null")
    void lifecycleDefaultsActive() {
        GateNetwork n = new GateNetwork("id", "n", "p", null, "t", "d", null, null, null, null, null);
        assertEquals(GateNetworkLifecycle.ACTIVE, n.lifecycle());
    }

    @Test
    @DisplayName("provenance defaults to CatalogProvenance.unknown() when null")
    void provenanceDefaultsUnknown() {
        GateNetwork n = new GateNetwork("id", "n", "p", null, "t", "d", null, null, null, null, null);
        assertEquals(CatalogProvenance.unknown(), n.provenance());
    }

    @Test
    @DisplayName("createdAt defaults to a current-ish Instant when null")
    void createdAtDefaultsToNow() {
        Instant before = Instant.now().minus(Duration.ofSeconds(1));
        GateNetwork n = new GateNetwork("id", "n", "p", null, "t", "d", null, null, null, null, null);
        Instant after = Instant.now().plus(Duration.ofSeconds(1));
        assertNotNull(n.createdAt());
        assertTrue(!n.createdAt().isBefore(before) && !n.createdAt().isAfter(after),
                "createdAt should fall within [now-1s, now+1s]; was " + n.createdAt());
    }

    @Test
    @DisplayName("modifiedAt defaults to createdAt when null")
    void modifiedAtDefaultsToCreatedAt() {
        GateNetwork n = new GateNetwork("id", "n", "p", null, "t", "d", null, null, null, null, null);
        assertSame(n.createdAt(), n.modifiedAt());
    }

    // ------------------------------------------------------------ Cataloged overrides

    @Test
    @DisplayName("source() reads from provenance.sourceUniverse() (D.6 Concern A pattern)")
    void sourceReadsFromProvenance() {
        GateNetwork n = sample();
        assertEquals("Test Universe", n.source());
        assertEquals(n.provenance().sourceUniverse(), n.source());
    }

    @Test
    @DisplayName("faction() returns builderPolity (E.1 §G gap-resolution: no separate field)")
    void factionReturnsBuilderPolity() {
        GateNetwork n = sample();
        assertEquals("Test Polity", n.faction());
        assertEquals(n.builderPolity(), n.faction());
    }

    @Test
    @DisplayName("concealed() always returns false (E.1 §G gap-resolution: not modeled)")
    void concealedAlwaysFalse() {
        assertFalse(sample().concealed());
        // Even with all-null fields the constant return holds.
        GateNetwork minimal = new GateNetwork("id", "n", "p", null, "t", "d", null, null, null, null, null);
        assertFalse(minimal.concealed());
    }

    // ------------------------------------------------------------ sealed-hierarchy non-membership

    /*
     * Note: "GateNetwork is NOT a SpaceAsset" is enforced at COMPILE TIME by the Java type
     * system. Writing `assertFalse(n instanceof SpaceAsset)` here would fail to compile
     * ("GateNetwork cannot be converted to SpaceAsset") because SpaceAsset is sealed and does
     * not permit GateNetwork. The compile-time guarantee is stronger than any runtime assertion
     * we could write. The runtime check below confirms the same via reflection on the
     * permitted-subclasses list.
     */

    @Test
    @DisplayName("GateNetwork IS a Cataloged (catalog uniformity preserved)")
    void isCataloged() {
        GateNetwork n = sample();
        assertTrue(n instanceof Cataloged,
                "GateNetwork must implement Cataloged for catalog uniformity");
    }

    @Test
    @DisplayName("SpaceAsset.class.getPermittedSubclasses() does NOT include GateNetwork (reflection-level check)")
    void notInSpaceAssetPermits() {
        Class<?>[] permitted = SpaceAsset.class.getPermittedSubclasses();
        for (Class<?> c : permitted) {
            assertFalse(c.equals(GateNetwork.class),
                    "SpaceAsset must not permit GateNetwork — sealed-hierarchy non-membership is the design");
        }
    }

    // ------------------------------------------------------------ parameterized

    @ParameterizedTest
    @EnumSource(GateNetworkLifecycle.class)
    @DisplayName("every GateNetworkLifecycle value can be set")
    void everyLifecycleAccepted(GateNetworkLifecycle lifecycle) {
        GateNetwork b = sample();
        GateNetwork n = new GateNetwork(
                b.id(), b.name(), b.builderPolity(), lifecycle, b.transponderName(),
                b.description(), b.notes(), b.category(), b.provenance(), b.createdAt(), b.modifiedAt());
        assertEquals(lifecycle, n.lifecycle());
    }

    @ParameterizedTest
    @EnumSource(CatalogOperationalStatus.class)
    @DisplayName("every CatalogOperationalStatus flows through provenance")
    void everyCatalogStatusAccepted(CatalogOperationalStatus status) {
        CatalogProvenance prov = new CatalogProvenance(SourceType.SCIENCE_FICTION, "X", null, status);
        GateNetwork n = new GateNetwork("id", "n", "p", null, "t", "d", null, null, prov, null, null);
        assertEquals(status, n.provenance().status());
    }

    @ParameterizedTest
    @EnumSource(SourceType.class)
    @DisplayName("every SourceType flows through provenance")
    void everySourceTypeAccepted(SourceType srcType) {
        CatalogProvenance prov = new CatalogProvenance(srcType, "Origin", null, CatalogOperationalStatus.UNKNOWN);
        GateNetwork n = new GateNetwork("id", "n", "p", null, "t", "d", null, null, prov, null, null);
        assertEquals(srcType, n.provenance().sourceType());
    }
}
