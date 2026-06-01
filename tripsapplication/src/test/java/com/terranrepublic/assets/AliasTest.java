package com.terranrepublic.assets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Phase F.2 §4.1 — pins {@link Alias}'s contract: compact-constructor invariants (5 required
 * fields + 3 with defaults), Cataloged-interface overrides (name returns aliasText, source +
 * faction return empty, concealed returns false), and the convenience constructor for fresh
 * aliases.
 */
class AliasTest {

    private static Alias sample() {
        return new Alias(
                "catalog-alias-test",
                "catalog-universe-star-trek",
                AliasTargetKind.STAR,
                "star-40-eridani-a",
                "Vulcan",
                "Home system of the Vulcan species in Star Trek canon.",
                Instant.parse("2026-06-01T00:00:00Z"),
                Instant.parse("2026-06-01T00:00:00Z"));
    }

    // ============================================================
    // Compact-constructor invariants — required fields
    // ============================================================

    @Test
    @DisplayName("null id throws IllegalArgumentException")
    void nullIdRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Alias(null, "u", AliasTargetKind.STAR, "t", "name", "", null, null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t"})
    @DisplayName("blank id throws IllegalArgumentException")
    void blankIdRejected(String blank) {
        assertThrows(IllegalArgumentException.class,
                () -> new Alias(blank, "u", AliasTargetKind.STAR, "t", "name", "", null, null));
    }

    @Test
    @DisplayName("null universeId throws IllegalArgumentException — aliases are universe-intrinsic")
    void nullUniverseIdRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Alias("id", null, AliasTargetKind.STAR, "t", "name", "", null, null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    @DisplayName("blank universeId throws IllegalArgumentException")
    void blankUniverseIdRejected(String blank) {
        assertThrows(IllegalArgumentException.class,
                () -> new Alias("id", blank, AliasTargetKind.STAR, "t", "name", "", null, null));
    }

    @Test
    @DisplayName("null targetKind throws IllegalArgumentException")
    void nullTargetKindRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Alias("id", "u", null, "t", "name", "", null, null));
    }

    @Test
    @DisplayName("null targetId throws IllegalArgumentException")
    void nullTargetIdRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Alias("id", "u", AliasTargetKind.STAR, null, "name", "", null, null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    @DisplayName("blank targetId throws IllegalArgumentException")
    void blankTargetIdRejected(String blank) {
        assertThrows(IllegalArgumentException.class,
                () -> new Alias("id", "u", AliasTargetKind.STAR, blank, "name", "", null, null));
    }

    @Test
    @DisplayName("null aliasText throws IllegalArgumentException")
    void nullAliasTextRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Alias("id", "u", AliasTargetKind.STAR, "t", null, "", null, null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    @DisplayName("blank aliasText throws IllegalArgumentException")
    void blankAliasTextRejected(String blank) {
        assertThrows(IllegalArgumentException.class,
                () -> new Alias("id", "u", AliasTargetKind.STAR, "t", blank, "", null, null));
    }

    // ============================================================
    // Compact-constructor defaults
    // ============================================================

    @Test
    @DisplayName("null description defaults to empty string")
    void nullDescriptionDefaultsToEmpty() {
        Alias a = new Alias("id", "u", AliasTargetKind.STAR, "t", "name", null, null, null);
        assertEquals("", a.description());
    }

    @Test
    @DisplayName("null createdAt defaults to a current-ish Instant")
    void nullCreatedAtDefaultsToNow() {
        Instant before = Instant.now().minusSeconds(1);
        Alias a = new Alias("id", "u", AliasTargetKind.STAR, "t", "name", "", null, null);
        Instant after = Instant.now().plusSeconds(1);
        assertNotNull(a.createdAt());
        assertTrue(!a.createdAt().isBefore(before) && !a.createdAt().isAfter(after),
                "createdAt should fall within [now-1s, now+1s]; was " + a.createdAt());
    }

    @Test
    @DisplayName("null modifiedAt defaults to createdAt")
    void nullModifiedAtDefaultsToCreatedAt() {
        Alias a = new Alias("id", "u", AliasTargetKind.STAR, "t", "name", "", null, null);
        assertSame(a.createdAt(), a.modifiedAt());
    }

    // ============================================================
    // Convenience constructor
    // ============================================================

    @Test
    @DisplayName("5-arg convenience constructor generates a catalog-alias- UUID id")
    void convenienceConstructorGeneratesUuidId() {
        Alias a = new Alias("u", AliasTargetKind.STAR, "t", "name", "desc");
        assertTrue(a.id().startsWith("catalog-alias-"),
                "convenience id must start with catalog-alias- per §14 naming convention; was: " + a.id());
        assertEquals("u", a.universeId());
        assertEquals(AliasTargetKind.STAR, a.targetKind());
        assertEquals("t", a.targetId());
        assertEquals("name", a.aliasText());
        assertEquals("desc", a.description());
        assertNotNull(a.createdAt());
    }

    @Test
    @DisplayName("convenience constructor produces unique ids per call")
    void convenienceConstructorIdsAreUnique() {
        Alias a = new Alias("u", AliasTargetKind.STAR, "t", "name", "");
        Alias b = new Alias("u", AliasTargetKind.STAR, "t", "name", "");
        // UUIDs collision-resistant; two calls in the same millisecond must produce distinct ids.
        assertFalse(a.id().equals(b.id()),
                "UUID-based ids must be unique across construction; got duplicates: " + a.id());
    }

    // ============================================================
    // Cataloged interface overrides
    // ============================================================

    @Test
    @DisplayName("name() returns aliasText (Cataloged contract)")
    void nameReturnsAliasText() {
        Alias a = sample();
        assertEquals("Vulcan", a.name());
        assertEquals(a.aliasText(), a.name());
    }

    @Test
    @DisplayName("source() returns empty string — aliases don't have upstream attribution")
    void sourceReturnsEmpty() {
        assertEquals("", sample().source());
    }

    @Test
    @DisplayName("faction() returns empty string — aliases are orthogonal to factions")
    void factionReturnsEmpty() {
        assertEquals("", sample().faction());
    }

    @Test
    @DisplayName("concealed() returns false — aliases follow universe activation, not concealment")
    void concealedAlwaysFalse() {
        assertFalse(sample().concealed());
    }

    @Test
    @DisplayName("universeId() auto-generated from record component returns the constructor arg")
    void universeIdReturnsComponentValue() {
        Alias a = sample();
        assertEquals("catalog-universe-star-trek", a.universeId());
    }

    @Test
    @DisplayName("Alias IS a Cataloged (catalog uniformity)")
    void isCataloged() {
        assertTrue(sample() instanceof Cataloged);
    }

    // ============================================================
    // Sealed-hierarchy non-membership (compile-time guarantee)
    // ============================================================

    @Test
    @DisplayName("SpaceAsset.class.getPermittedSubclasses() does NOT include Alias (reflection check)")
    void notInSpaceAssetPermits() {
        Class<?>[] permitted = SpaceAsset.class.getPermittedSubclasses();
        for (Class<?> c : permitted) {
            assertFalse(c.equals(Alias.class),
                    "SpaceAsset must not permit Alias — Alias is a Cataloged-but-not-SpaceAsset entity");
        }
    }

    // ============================================================
    // Parameterized
    // ============================================================

    @ParameterizedTest
    @EnumSource(AliasTargetKind.class)
    @DisplayName("every AliasTargetKind value is acceptable in the constructor")
    void everyTargetKindAccepted(AliasTargetKind kind) {
        Alias a = new Alias("id", "u", kind, "t", "name", "", null, null);
        assertEquals(kind, a.targetKind());
    }
}
