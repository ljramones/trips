package com.terranrepublic.assets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * v2 Phase F.1 §4.1 — pins {@link Universe}'s contract: compact-constructor invariants,
 * compact-constructor defaults, the {@link Cataloged} interface overrides
 * (source/faction/concealed), the deliberate non-membership in the sealed
 * {@link SpaceAsset} hierarchy, and the {@code withActive} immutability helper.
 */
class UniverseTest {

    private static Universe sample() {
        return new Universe(
                "catalog-universe-test",
                "Test Universe",
                "A test universe used by unit tests.",
                "Larry Mitchell",
                "1.0",
                UniverseLifecycle.AVAILABLE,
                false);
    }

    // ----------------------------------------------------- invariants

    @Test
    @DisplayName("null id throws IllegalArgumentException")
    void nullIdRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Universe(null, "Name", "", "", "1.0", UniverseLifecycle.AVAILABLE, false));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t"})
    @DisplayName("blank id throws IllegalArgumentException")
    void blankIdRejected(String blank) {
        assertThrows(IllegalArgumentException.class,
                () -> new Universe(blank, "Name", "", "", "1.0", UniverseLifecycle.AVAILABLE, false));
    }

    @Test
    @DisplayName("null name throws IllegalArgumentException")
    void nullNameRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Universe("id", null, "", "", "1.0", UniverseLifecycle.AVAILABLE, false));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    @DisplayName("blank name throws IllegalArgumentException")
    void blankNameRejected(String blank) {
        assertThrows(IllegalArgumentException.class,
                () -> new Universe("id", blank, "", "", "1.0", UniverseLifecycle.AVAILABLE, false));
    }

    // ----------------------------------------------------- defaults

    @Test
    @DisplayName("lifecycle defaults to AVAILABLE when null")
    void lifecycleDefaultsAvailable() {
        Universe u = new Universe("id", "name", "", "", "1.0", null, false);
        assertEquals(UniverseLifecycle.AVAILABLE, u.lifecycle());
    }

    @Test
    @DisplayName("description defaults to \"\" (empty string, not null)")
    void descriptionDefaultsEmpty() {
        Universe u = new Universe("id", "name", null, "", "1.0", UniverseLifecycle.AVAILABLE, false);
        assertEquals("", u.description());
    }

    @Test
    @DisplayName("sourceAuthor defaults to \"\" (empty string, not null)")
    void sourceAuthorDefaultsEmpty() {
        Universe u = new Universe("id", "name", "", null, "1.0", UniverseLifecycle.AVAILABLE, false);
        assertEquals("", u.sourceAuthor());
    }

    @Test
    @DisplayName("null version defaults to \"1.0\"")
    void nullVersionDefaultsToOnePointZero() {
        Universe u = new Universe("id", "name", "", "", null, UniverseLifecycle.AVAILABLE, false);
        assertEquals("1.0", u.version());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  "})
    @DisplayName("blank version defaults to \"1.0\"")
    void blankVersionDefaultsToOnePointZero(String blank) {
        Universe u = new Universe("id", "name", "", "", blank, UniverseLifecycle.AVAILABLE, false);
        assertEquals("1.0", u.version());
    }

    // ----------------------------------------------------- convenience constructor

    @Test
    @DisplayName("4-arg convenience constructor produces an AVAILABLE, inactive, version-1.0 universe")
    void fourArgConvenienceConstructor() {
        Universe u = new Universe("id", "Name", "desc", "author");
        assertEquals("1.0", u.version());
        assertEquals(UniverseLifecycle.AVAILABLE, u.lifecycle());
        assertFalse(u.active());
        assertEquals("id", u.id());
        assertEquals("Name", u.name());
        assertEquals("desc", u.description());
        assertEquals("author", u.sourceAuthor());
    }

    // ----------------------------------------------------- Cataloged overrides

    @Test
    @DisplayName("source() returns empty string — Universe IS the source, no upstream attribution")
    void sourceReturnsEmpty() {
        assertEquals("", sample().source());
    }

    @Test
    @DisplayName("faction() returns sourceAuthor")
    void factionReturnsSourceAuthor() {
        Universe u = sample();
        assertEquals("Larry Mitchell", u.faction());
        assertEquals(u.sourceAuthor(), u.faction());
    }

    @Test
    @DisplayName("concealed() always returns false (universes use the active flag for visibility, not concealment)")
    void concealedAlwaysFalse() {
        assertFalse(sample().concealed());
        // Even an inactive universe is not "concealed" — the visibility distinction is orthogonal.
        Universe inactive = new Universe("id", "Name", "", "", "1.0", UniverseLifecycle.AVAILABLE, false);
        assertFalse(inactive.concealed());
        Universe deprecated = new Universe("id", "Name", "", "", "1.0", UniverseLifecycle.DEPRECATED, false);
        assertFalse(deprecated.concealed());
    }

    // ----------------------------------------------------- sealed-hierarchy non-membership

    /*
     * Note: "Universe is NOT a SpaceAsset" is enforced at COMPILE TIME by the Java type system.
     * Writing `assertFalse(u instanceof SpaceAsset)` here would fail to compile because SpaceAsset
     * is sealed and does not permit Universe. The compile-time guarantee is stronger than any
     * runtime assertion. The reflection-level check below confirms the same.
     */

    @Test
    @DisplayName("Universe IS a Cataloged (catalog uniformity preserved)")
    void isCataloged() {
        assertTrue(sample() instanceof Cataloged,
                "Universe must implement Cataloged for catalog uniformity");
    }

    @Test
    @DisplayName("SpaceAsset.class.getPermittedSubclasses() does NOT include Universe (reflection-level check)")
    void notInSpaceAssetPermits() {
        Class<?>[] permitted = SpaceAsset.class.getPermittedSubclasses();
        for (Class<?> c : permitted) {
            assertFalse(c.equals(Universe.class),
                    "SpaceAsset must not permit Universe — sealed-hierarchy non-membership is the design");
        }
    }

    // ----------------------------------------------------- withActive helper

    @Test
    @DisplayName("withActive(true) returns a new Universe with active = true")
    void withActiveTrueProducesActive() {
        Universe inactive = sample();
        assertFalse(inactive.active());
        Universe activated = inactive.withActive(true);
        assertTrue(activated.active());
        assertEquals(inactive.id(), activated.id(), "id preserved");
        assertEquals(inactive.name(), activated.name(), "name preserved");
        assertEquals(inactive.lifecycle(), activated.lifecycle(), "lifecycle preserved");
        assertNotSame(inactive, activated, "withActive returns a new instance, not mutates in place");
    }

    @Test
    @DisplayName("withActive(false) returns a new Universe with active = false")
    void withActiveFalseProducesInactive() {
        Universe active = sample().withActive(true);
        Universe deactivated = active.withActive(false);
        assertFalse(deactivated.active());
        assertNotEquals(active, deactivated);
    }

    @Test
    @DisplayName("withActive preserves all other fields exactly")
    void withActivePreservesAllOtherFields() {
        Universe original = new Universe(
                "catalog-universe-cotp", "Children of the Pattern", "Larry's series",
                "Larry Mitchell", "2.1", UniverseLifecycle.DEPRECATED, false);
        Universe toggled = original.withActive(true);
        assertEquals(original.id(), toggled.id());
        assertEquals(original.name(), toggled.name());
        assertEquals(original.description(), toggled.description());
        assertEquals(original.sourceAuthor(), toggled.sourceAuthor());
        assertEquals(original.version(), toggled.version());
        assertEquals(original.lifecycle(), toggled.lifecycle());
        assertTrue(toggled.active());
    }

    // ----------------------------------------------------- parameterized

    @ParameterizedTest
    @EnumSource(UniverseLifecycle.class)
    @DisplayName("every UniverseLifecycle value is acceptable in the constructor")
    void everyLifecycleAccepted(UniverseLifecycle lifecycle) {
        Universe u = new Universe("id", "name", "", "", "1.0", lifecycle, false);
        assertEquals(lifecycle, u.lifecycle());
    }
}
