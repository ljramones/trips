package com.teamgannon.trips.worldbuilding;

import com.terranrepublic.assets.Universe;
import com.terranrepublic.assets.UniverseLifecycle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * v2 Phase F.1 §5.2 — record-shape tests for {@link UniverseActivationChangedEvent}.
 *
 * <p>Records get equals/hashCode/accessor for free; these tests pin the contract anyway so a
 * future refactor that breaks the accessor names or the (universe, nowActive) shape is caught.
 * Also exercises the compact-constructor invariant that {@code nowActive} must match
 * {@code universe.active()} — the event payload is the post-toggle state, not a pre/post pair.
 */
class UniverseActivationChangedEventTest {

    private static Universe activeFixture() {
        return new Universe("catalog-universe-test", "Test Universe", "desc", "author",
                "1.0", UniverseLifecycle.AVAILABLE, true);
    }

    private static Universe inactiveFixture() {
        return new Universe("catalog-universe-test", "Test Universe", "desc", "author",
                "1.0", UniverseLifecycle.AVAILABLE, false);
    }

    // ============================================================
    // Accessor + record shape
    // ============================================================

    @Test
    @DisplayName("universe() accessor returns the constructor argument")
    void universeAccessorReturnsConstructorArg() {
        Universe src = activeFixture();
        UniverseActivationChangedEvent event = new UniverseActivationChangedEvent(src, true);
        assertSame(src, event.universe());
    }

    @Test
    @DisplayName("nowActive() accessor returns the constructor argument")
    void nowActiveAccessorReturnsConstructorArg() {
        UniverseActivationChangedEvent active = new UniverseActivationChangedEvent(activeFixture(), true);
        UniverseActivationChangedEvent inactive = new UniverseActivationChangedEvent(inactiveFixture(), false);
        assertTrue(active.nowActive());
        assertFalse(inactive.nowActive());
    }

    // ============================================================
    // Compact-constructor invariants
    // ============================================================

    @Test
    @DisplayName("null universe is rejected at construction time")
    void nullUniverseRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new UniverseActivationChangedEvent(null, true));
    }

    @Test
    @DisplayName("nowActive=true with universe.active()=false is rejected (event is post-toggle state)")
    void nowActiveTrueWithInactiveUniverseRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new UniverseActivationChangedEvent(inactiveFixture(), true),
                "the event's nowActive must match the universe's current active() — both reflect "
                        + "the post-toggle state");
    }

    @Test
    @DisplayName("nowActive=false with universe.active()=true is rejected")
    void nowActiveFalseWithActiveUniverseRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new UniverseActivationChangedEvent(activeFixture(), false));
    }

    // ============================================================
    // Equality / identity
    // ============================================================

    @Test
    @DisplayName("two events with the same universe + nowActive are equal")
    void equalityForMatchingFields() {
        Universe src = activeFixture();
        UniverseActivationChangedEvent a = new UniverseActivationChangedEvent(src, true);
        UniverseActivationChangedEvent b = new UniverseActivationChangedEvent(src, true);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("events with different universe identities are distinct")
    void inequalityForDifferentUniverses() {
        Universe a = new Universe("u-a", "A", "", "", "1.0", UniverseLifecycle.AVAILABLE, true);
        Universe b = new Universe("u-b", "B", "", "", "1.0", UniverseLifecycle.AVAILABLE, true);
        UniverseActivationChangedEvent ea = new UniverseActivationChangedEvent(a, true);
        UniverseActivationChangedEvent eb = new UniverseActivationChangedEvent(b, true);
        assertNotEquals(ea, eb);
    }
}
