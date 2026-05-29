package com.terranrepublic.assets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Pins {@link Mobility}'s exact value set and declaration order per the v2 Phase D.7
 * design (Step 1.6 — preamble to the Megastructure subtype work).
 */
class MobilityTest {

    @Test
    @DisplayName("Mobility has exactly 6 values (v2 Phase D.7 extension)")
    void sixValues() {
        assertEquals(6, Mobility.values().length);
    }

    @Test
    @DisplayName("the six documented values are present and exactly spelled")
    void exactValues() {
        Set<String> actual = Arrays.stream(Mobility.values())
                .map(Enum::name)
                .collect(Collectors.toSet());
        assertEquals(Set.of(
                "FIXED",
                "STATIONKEEPING",
                "MANEUVERABLE",
                "MOBILE_LIMITED",
                "MOBILE",
                "MOBILE_AUTONOMOUS"),
                actual);
    }

    @Test
    @DisplayName("declaration order is ascending mobility (user-facing combobox order)")
    void declarationOrder() {
        List<String> actualOrder = Arrays.stream(Mobility.values())
                .map(Enum::name)
                .collect(Collectors.toList());
        assertEquals(List.of(
                "FIXED",
                "STATIONKEEPING",
                "MANEUVERABLE",
                "MOBILE_LIMITED",
                "MOBILE",
                "MOBILE_AUTONOMOUS"),
                actualOrder);
    }

    @Test
    @DisplayName("every value is reachable via Enum.valueOf")
    void allValuesReachableViaValueOf() {
        for (Mobility m : Mobility.values()) {
            assertNotNull(Mobility.valueOf(m.name()));
        }
    }

    @Test
    @DisplayName("the original three pre-Phase-D.7 values are still present at their original ordinals")
    void originalThreeValuesPreserved() {
        assertEquals(0, Mobility.FIXED.ordinal());
        assertEquals(1, Mobility.STATIONKEEPING.ordinal());
        assertEquals(2, Mobility.MANEUVERABLE.ordinal());
    }
}
