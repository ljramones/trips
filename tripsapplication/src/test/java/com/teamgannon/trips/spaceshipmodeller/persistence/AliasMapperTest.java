package com.teamgannon.trips.spaceshipmodeller.persistence;

import com.terranrepublic.assets.Alias;
import com.terranrepublic.assets.AliasTargetKind;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Phase F.2 §4.5 — round-trip coverage for {@link AliasMapper}.
 *
 * <p>Simpler than GateNetworkMapperTest because Alias has no provenance object. Field-by-field
 * round-trip with parameterized {@link AliasTargetKind} coverage.
 */
class AliasMapperTest {

    private final AliasMapper mapper = new AliasMapper();

    private static Alias vulcanFixture() {
        return new Alias(
                "catalog-alias-vulcan-system",
                "catalog-universe-star-trek",
                AliasTargetKind.STAR,
                "star-40-eridani-a",
                "Vulcan",
                "Home system of the Vulcan species in Star Trek canon.",
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-06-01T00:00:00Z"));
    }

    @Test
    @DisplayName("all 8 fields round-trip through toEntity → toDomain")
    void allFieldsRoundTrip() {
        Alias src = vulcanFixture();
        Alias back = mapper.toDomain(mapper.toEntity(src));

        assertEquals(src.id(), back.id());
        assertEquals(src.universeId(), back.universeId());
        assertEquals(src.targetKind(), back.targetKind());
        assertEquals(src.targetId(), back.targetId());
        assertEquals(src.aliasText(), back.aliasText());
        assertEquals(src.description(), back.description());
        assertEquals(src.createdAt(), back.createdAt());
        assertEquals(src.modifiedAt(), back.modifiedAt());
    }

    @Test
    @DisplayName("double round-trip is stable")
    void doubleRoundTripStable() {
        Alias src = vulcanFixture();
        Alias once = mapper.toDomain(mapper.toEntity(src));
        Alias twice = mapper.toDomain(mapper.toEntity(once));
        assertEquals(once, twice);
    }

    @ParameterizedTest
    @EnumSource(AliasTargetKind.class)
    @DisplayName("every AliasTargetKind round-trips")
    void everyTargetKindRoundTrips(AliasTargetKind kind) {
        Alias src = new Alias("id", "u", kind, "t", "name", "desc",
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z"));
        Alias back = mapper.toDomain(mapper.toEntity(src));
        assertEquals(kind, back.targetKind());
    }

    @Test
    @DisplayName("empty description preserved across round-trip (not normalised to null)")
    void emptyDescriptionPreserved() {
        Alias src = new Alias("id", "u", AliasTargetKind.STAR, "t", "name", "",
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z"));
        Alias back = mapper.toDomain(mapper.toEntity(src));
        assertEquals("", back.description());
    }

    // ============================================================
    // Entity defaults
    // ============================================================

    @Test
    @DisplayName("4-arg entity constructor populates UUID id + NOT NULL defaults")
    void fourArgEntityDefaults() {
        AliasEntity entity = new AliasEntity("u", AliasTargetKind.EXOPLANET, "ep-1", "Akane's homeworld");
        assertNotNull(entity.getId());
        assertEquals("catalog-alias-", entity.getId().substring(0, 14));
        assertEquals("u", entity.getUniverseId());
        assertEquals(AliasTargetKind.EXOPLANET, entity.getTargetKind());
        assertEquals("ep-1", entity.getTargetId());
        assertEquals("Akane's homeworld", entity.getAliasText());
        assertEquals("", entity.getDescription());
        assertNotNull(entity.getCreatedAt());
        assertNotNull(entity.getModifiedAt());
    }

    @Test
    @DisplayName("entity with null description maps to empty-string domain description (defensive)")
    void entityNullDescriptionMapsToEmpty() {
        AliasEntity entity = new AliasEntity("u", AliasTargetKind.STAR, "t", "name");
        entity.setDescription(null);
        Alias domain = mapper.toDomain(entity);
        assertEquals("", domain.description());
    }

    @Test
    @DisplayName("entity with null targetKind maps to STAR domain targetKind (defensive)")
    void entityNullTargetKindMapsToStar() {
        AliasEntity entity = new AliasEntity("u", AliasTargetKind.STAR, "t", "name");
        entity.setTargetKind(null);
        Alias domain = mapper.toDomain(entity);
        assertEquals(AliasTargetKind.STAR, domain.targetKind());
    }
}
