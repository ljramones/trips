package com.teamgannon.trips.spaceshipmodeller.persistence;

import com.terranrepublic.assets.CatalogOperationalStatus;
import com.terranrepublic.assets.CatalogProvenance;
import com.terranrepublic.assets.GateNetwork;
import com.terranrepublic.assets.GateNetworkLifecycle;
import com.terranrepublic.assets.SourceType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * v2 Phase E.1 §5 — round-trip coverage for {@link GateNetworkMapper}.
 *
 * <p>Simpler than {@link MegastructureDesignMapperTest} because {@link GateNetwork} has no
 * collection fields (no JSON LOB serialization). Field-by-field round-trip, parameterized over
 * lifecycle + catalog status + source type.
 */
class GateNetworkMapperTest {

    private final GateNetworkMapper mapper = new GateNetworkMapper();

    private static GateNetwork aldenataShaped() {
        return new GateNetwork(
                "catalog-network-aldenata-civilian",
                "Aldenata Civilian Network",
                "Aldenata",
                GateNetworkLifecycle.DERELICT,
                "ALDENATA-CIV-XPDR",
                "Civilian-grade gate network constructed by the Aldenata. Long-derelict; the "
                        + "Posleen War scrambled the transponder discovery chain.",
                "INFERRED: transponder name is synthetic; canonical name not in source material.",
                "interstellar gate network",
                new CatalogProvenance(SourceType.SCIENCE_FICTION, "Legacy of the Aldenata",
                        "A Hymn Before Battle", CatalogOperationalStatus.FICTIONAL),
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-06-01T00:00:00Z"));
    }

    // ----------------------------------------------------------------- full round-trip

    @Test
    @DisplayName("all 11 fields round-trip through toEntity → toDomain")
    void allFieldsRoundTrip() {
        GateNetwork src = aldenataShaped();
        GateNetwork back = mapper.toDomain(mapper.toEntity(src));

        assertEquals(src.id(), back.id());
        assertEquals(src.name(), back.name());
        assertEquals(src.builderPolity(), back.builderPolity());
        assertEquals(src.lifecycle(), back.lifecycle());
        assertEquals(src.transponderName(), back.transponderName());
        assertEquals(src.description(), back.description());
        assertEquals(src.notes(), back.notes());
        assertEquals(src.category(), back.category());
        assertEquals(src.provenance(), back.provenance());
        assertEquals(src.createdAt(), back.createdAt());
        assertEquals(src.modifiedAt(), back.modifiedAt());
    }

    // ----------------------------------------------------------------- parameterized

    @ParameterizedTest
    @EnumSource(GateNetworkLifecycle.class)
    @DisplayName("every GateNetworkLifecycle round-trips")
    void everyLifecycleRoundTrips(GateNetworkLifecycle lifecycle) {
        GateNetwork src = new GateNetwork("id", "name", "polity", lifecycle, "xpdr",
                "desc", "notes", "cat",
                new CatalogProvenance(SourceType.SCIENCE_FICTION, "U", null, CatalogOperationalStatus.FICTIONAL),
                Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-01-01T00:00:00Z"));
        GateNetwork back = mapper.toDomain(mapper.toEntity(src));
        assertEquals(lifecycle, back.lifecycle());
    }

    @ParameterizedTest
    @EnumSource(CatalogOperationalStatus.class)
    @DisplayName("every CatalogOperationalStatus round-trips through provenance")
    void everyCatalogStatusRoundTrips(CatalogOperationalStatus status) {
        CatalogProvenance prov = new CatalogProvenance(SourceType.SCIENCE_FICTION, "U", null, status);
        GateNetwork src = new GateNetwork("id", "name", "polity", null, "xpdr",
                "desc", "notes", "cat", prov, null, null);
        GateNetwork back = mapper.toDomain(mapper.toEntity(src));
        assertEquals(status, back.provenance().status());
    }

    @ParameterizedTest
    @EnumSource(SourceType.class)
    @DisplayName("every SourceType round-trips through provenance")
    void everySourceTypeRoundTrips(SourceType srcType) {
        CatalogProvenance prov = new CatalogProvenance(srcType, "Origin", null, CatalogOperationalStatus.UNKNOWN);
        GateNetwork src = new GateNetwork("id", "name", "polity", null, "xpdr",
                "desc", "notes", "cat", prov, null, null);
        GateNetwork back = mapper.toDomain(mapper.toEntity(src));
        assertEquals(srcType, back.provenance().sourceType());
    }

    // ----------------------------------------------------------------- null preservation

    @Test
    @DisplayName("null sourceWork is preserved as null across the round-trip")
    void nullSourceWorkPreserved() {
        CatalogProvenance prov = new CatalogProvenance(SourceType.REAL, "Earth", null, CatalogOperationalStatus.ACTIVE);
        GateNetwork src = new GateNetwork("id", "name", "polity", null, "xpdr",
                "desc", "notes", "cat", prov, null, null);
        GateNetwork back = mapper.toDomain(mapper.toEntity(src));
        assertNull(back.provenance().sourceWork());
    }

    @Test
    @DisplayName("null nullable strings (builderPolity, transponderName, description, notes, category) are preserved")
    void nullNullableStringsPreserved() {
        GateNetwork src = new GateNetwork("id", "name", null, null, null, null, null, null, null, null, null);
        GateNetwork back = mapper.toDomain(mapper.toEntity(src));
        assertNull(back.builderPolity());
        assertNull(back.transponderName());
        assertNull(back.description());
        assertNull(back.notes());
        assertNull(back.category());
    }

    // ----------------------------------------------------------------- entity defaults

    @Test
    @DisplayName("no-arg entity constructor populates NOT NULL defaults matching the V13 column defaults")
    void noArgEntityDefaults() {
        GateNetworkEntity named = new GateNetworkEntity("X");
        assertEquals(GateNetworkLifecycle.ACTIVE, named.getLifecycle());
        assertEquals(SourceType.UNKNOWN, named.getProvenanceSourceType());
        assertEquals("", named.getProvenanceSourceUniverse());
        assertEquals(CatalogOperationalStatus.UNKNOWN, named.getProvenanceStatus());
        assertNotNull(named.getId());
        assertNotNull(named.getCreatedAt());
        assertNotNull(named.getModifiedAt());
        assertEquals("X", named.getName());
    }

    @Test
    @DisplayName("double round-trip is stable")
    void doubleRoundTripStable() {
        GateNetwork src = aldenataShaped();
        GateNetwork once = mapper.toDomain(mapper.toEntity(src));
        GateNetwork twice = mapper.toDomain(mapper.toEntity(once));
        assertEquals(once, twice);
    }
}
