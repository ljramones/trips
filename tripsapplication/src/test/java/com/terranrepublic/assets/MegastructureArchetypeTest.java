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
 * Pins {@link MegastructureArchetype}'s exact value set and declaration order per
 * the v2 Phase D.7 design §2.
 */
class MegastructureArchetypeTest {

    @Test
    @DisplayName("MegastructureArchetype has exactly 6 values (v2 Phase D.7 §2)")
    void sixValues() {
        assertEquals(6, MegastructureArchetype.values().length);
    }

    @Test
    @DisplayName("the six documented values are present and exactly spelled")
    void exactValues() {
        Set<String> actual = Arrays.stream(MegastructureArchetype.values())
                .map(Enum::name)
                .collect(Collectors.toSet());
        assertEquals(Set.of(
                "DISGUISED_MOON",
                "PURPOSE_BUILT_FORT",
                "CONVERTED_ASTEROID",
                "BIG_DUMB_OBJECT",
                "ENGINEERED_WORLD",
                "UNKNOWN"),
                actual);
    }

    @Test
    @DisplayName("declaration order matches design §2 (DISGUISED_MOON, PURPOSE_BUILT_FORT, CONVERTED_ASTEROID, BIG_DUMB_OBJECT, ENGINEERED_WORLD, UNKNOWN)")
    void declarationOrder() {
        List<String> actualOrder = Arrays.stream(MegastructureArchetype.values())
                .map(Enum::name)
                .collect(Collectors.toList());
        assertEquals(List.of(
                "DISGUISED_MOON",
                "PURPOSE_BUILT_FORT",
                "CONVERTED_ASTEROID",
                "BIG_DUMB_OBJECT",
                "ENGINEERED_WORLD",
                "UNKNOWN"),
                actualOrder);
    }

    @Test
    @DisplayName("every value is reachable via Enum.valueOf")
    void allValuesReachableViaValueOf() {
        for (MegastructureArchetype a : MegastructureArchetype.values()) {
            assertNotNull(MegastructureArchetype.valueOf(a.name()));
        }
    }
}
