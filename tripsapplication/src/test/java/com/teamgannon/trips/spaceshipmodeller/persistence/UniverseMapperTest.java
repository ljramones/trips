package com.teamgannon.trips.spaceshipmodeller.persistence;

import com.terranrepublic.assets.Universe;
import com.terranrepublic.assets.UniverseLifecycle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * v2 Phase F.1 §4.2 — round-trip coverage for {@link UniverseMapper}.
 *
 * <p>Simpler than {@link GateNetworkMapperTest} because {@link Universe} carries no provenance
 * (Universe IS the provenance scope). Field-by-field round-trip with parameterized lifecycle
 * coverage + null-preservation + entity-default checks.
 */
class UniverseMapperTest {

    private final UniverseMapper mapper = new UniverseMapper();

    private static Universe legacyOfTheAldenata() {
        return new Universe(
                "catalog-universe-legacy-of-the-aldenata",
                "Legacy of the Aldenata",
                "John Ringo's Posleen War setting. Includes Troy, SAPL elements, SheVa Gun, "
                        + "Posleen ship designs, and the fictional Posleen interstellar drive.",
                "John Ringo",
                "1.0",
                UniverseLifecycle.AVAILABLE,
                false);
    }

    // ----------------------------------------------------------------- full round-trip

    @Test
    @DisplayName("all 7 fields round-trip through toEntity → toDomain")
    void allFieldsRoundTrip() {
        Universe src = legacyOfTheAldenata();
        Universe back = mapper.toDomain(mapper.toEntity(src));

        assertEquals(src.id(), back.id());
        assertEquals(src.name(), back.name());
        assertEquals(src.description(), back.description());
        assertEquals(src.sourceAuthor(), back.sourceAuthor());
        assertEquals(src.version(), back.version());
        assertEquals(src.lifecycle(), back.lifecycle());
        assertEquals(src.active(), back.active());
    }

    @Test
    @DisplayName("double round-trip is stable")
    void doubleRoundTripStable() {
        Universe src = legacyOfTheAldenata();
        Universe once = mapper.toDomain(mapper.toEntity(src));
        Universe twice = mapper.toDomain(mapper.toEntity(once));
        assertEquals(once, twice);
    }

    // ----------------------------------------------------------------- parameterized

    @ParameterizedTest
    @EnumSource(UniverseLifecycle.class)
    @DisplayName("every UniverseLifecycle round-trips")
    void everyLifecycleRoundTrips(UniverseLifecycle lifecycle) {
        Universe src = new Universe("id", "name", "desc", "author", "1.0", lifecycle, false);
        Universe back = mapper.toDomain(mapper.toEntity(src));
        assertEquals(lifecycle, back.lifecycle());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("both active states round-trip")
    void bothActiveStatesRoundTrip(boolean active) {
        Universe src = new Universe("id", "name", "desc", "author", "1.0",
                UniverseLifecycle.AVAILABLE, active);
        Universe back = mapper.toDomain(mapper.toEntity(src));
        assertEquals(active, back.active());
    }

    // ----------------------------------------------------------------- empty-string preservation

    @Test
    @DisplayName("empty-string description preserved across round-trip (not normalised to null)")
    void emptyDescriptionPreserved() {
        Universe src = new Universe("id", "name", "", "author", "1.0", UniverseLifecycle.AVAILABLE, false);
        Universe back = mapper.toDomain(mapper.toEntity(src));
        assertEquals("", back.description());
    }

    @Test
    @DisplayName("empty-string sourceAuthor preserved across round-trip")
    void emptySourceAuthorPreserved() {
        Universe src = new Universe("id", "name", "desc", "", "1.0", UniverseLifecycle.AVAILABLE, false);
        Universe back = mapper.toDomain(mapper.toEntity(src));
        assertEquals("", back.sourceAuthor());
    }

    // ----------------------------------------------------------------- entity defaults

    @Test
    @DisplayName("no-arg entity constructor populates NOT NULL defaults matching V15 column defaults")
    void noArgEntityDefaults() {
        UniverseEntity named = new UniverseEntity("Test Universe");
        assertEquals(UniverseLifecycle.AVAILABLE, named.getLifecycle());
        assertEquals("1.0", named.getVersion());
        assertEquals("", named.getDescription());
        assertEquals("", named.getSourceAuthor());
        assertFalse(named.isActive(), "active defaults to false (matches V15 column default)");
        assertNotNull(named.getId());
        assertNotNull(named.getCreatedAt());
        assertNotNull(named.getModifiedAt());
        assertEquals("Test Universe", named.getName());
    }

    @Test
    @DisplayName("toEntity sets createdAt and modifiedAt (timestamps are mapper-owned, not domain-owned)")
    void mapperSetsTimestamps() {
        Universe src = legacyOfTheAldenata();
        UniverseEntity entity = mapper.toEntity(src);
        assertNotNull(entity.getCreatedAt());
        assertNotNull(entity.getModifiedAt());
    }

    // ----------------------------------------------------------------- defensive null handling

    @Test
    @DisplayName("entity with null description maps to empty-string domain description")
    void entityNullDescriptionMapsToEmpty() {
        UniverseEntity entity = new UniverseEntity("Test");
        entity.setDescription(null);
        Universe domain = mapper.toDomain(entity);
        assertEquals("", domain.description());
    }

    @Test
    @DisplayName("entity with null sourceAuthor maps to empty-string domain sourceAuthor")
    void entityNullSourceAuthorMapsToEmpty() {
        UniverseEntity entity = new UniverseEntity("Test");
        entity.setSourceAuthor(null);
        Universe domain = mapper.toDomain(entity);
        assertEquals("", domain.sourceAuthor());
    }

    @Test
    @DisplayName("entity with null lifecycle maps to AVAILABLE domain lifecycle")
    void entityNullLifecycleMapsToAvailable() {
        UniverseEntity entity = new UniverseEntity("Test");
        entity.setLifecycle(null);
        Universe domain = mapper.toDomain(entity);
        assertEquals(UniverseLifecycle.AVAILABLE, domain.lifecycle());
    }

    @Test
    @DisplayName("entity with null version maps to \"1.0\" domain version")
    void entityNullVersionMapsToOnePointZero() {
        UniverseEntity entity = new UniverseEntity("Test");
        entity.setVersion(null);
        Universe domain = mapper.toDomain(entity);
        assertEquals("1.0", domain.version());
    }

    @Test
    @DisplayName("active flag round-trips even from non-default startup state")
    void activeFlagStableFromNonDefault() {
        Universe activeSrc = new Universe("id", "n", "d", "a", "1.0", UniverseLifecycle.AVAILABLE, true);
        UniverseEntity entity = mapper.toEntity(activeSrc);
        assertTrue(entity.isActive());
        Universe back = mapper.toDomain(entity);
        assertTrue(back.active());
    }
}
