package com.teamgannon.trips.solarsystem;

import com.teamgannon.trips.model.SolarSystemDescription;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Trivial record-shape tests for {@link SolarSystemActivatedEvent} per v2 Phase E.1 §7.1.
 * Records get equals/hashCode/accessor for free; these tests pin the contract anyway so a
 * future refactor that breaks the accessor name or the record's single-component shape is
 * caught.
 */
class SolarSystemActivatedEventTest {

    @Test
    @DisplayName("system() accessor returns the constructor argument")
    void systemAccessorReturnsConstructorArg() {
        SolarSystemDescription src = new SolarSystemDescription();
        src.setSolarSystemId("test-system-id");
        SolarSystemActivatedEvent event = new SolarSystemActivatedEvent(src);
        assertSame(src, event.system());
    }

    @Test
    @DisplayName("null system is allowed (the publishing site guards against null upstream)")
    void nullSystemAllowed() {
        SolarSystemActivatedEvent event = new SolarSystemActivatedEvent(null);
        assertNull(event.system());
    }

    @Test
    @DisplayName("record equality: same system reference → equal events")
    void equalityForSameSystemReference() {
        SolarSystemDescription src = new SolarSystemDescription();
        src.setSolarSystemId("equality-test");
        SolarSystemActivatedEvent a = new SolarSystemActivatedEvent(src);
        SolarSystemActivatedEvent b = new SolarSystemActivatedEvent(src);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("record equality: different system references → distinct events")
    void inequalityForDifferentSystemReferences() {
        SolarSystemDescription srcA = new SolarSystemDescription();
        srcA.setSolarSystemId("a");
        SolarSystemDescription srcB = new SolarSystemDescription();
        srcB.setSolarSystemId("b");
        SolarSystemActivatedEvent a = new SolarSystemActivatedEvent(srcA);
        SolarSystemActivatedEvent b = new SolarSystemActivatedEvent(srcB);
        assertNotEquals(a, b);
    }
}
