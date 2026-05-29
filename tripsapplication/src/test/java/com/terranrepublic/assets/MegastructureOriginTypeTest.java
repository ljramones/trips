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
 * Pins {@link MegastructureOriginType}'s exact value set and declaration order per
 * the v2 Phase D.7 design §4.2.
 */
class MegastructureOriginTypeTest {

    @Test
    @DisplayName("MegastructureOriginType has exactly 5 values (v2 Phase D.7 §4.2)")
    void fiveValues() {
        assertEquals(5, MegastructureOriginType.values().length);
    }

    @Test
    @DisplayName("the five documented values are present and exactly spelled")
    void exactValues() {
        Set<String> actual = Arrays.stream(MegastructureOriginType.values())
                .map(Enum::name)
                .collect(Collectors.toSet());
        assertEquals(Set.of(
                "BUILT_BY_KNOWN",
                "BUILT_BY_UNKNOWN",
                "FOUND_INTACT",
                "FOUND_DAMAGED",
                "UNKNOWN"),
                actual);
    }

    @Test
    @DisplayName("declaration order matches design §4.2 (BUILT_BY_KNOWN, BUILT_BY_UNKNOWN, FOUND_INTACT, FOUND_DAMAGED, UNKNOWN)")
    void declarationOrder() {
        List<String> actualOrder = Arrays.stream(MegastructureOriginType.values())
                .map(Enum::name)
                .collect(Collectors.toList());
        assertEquals(List.of(
                "BUILT_BY_KNOWN",
                "BUILT_BY_UNKNOWN",
                "FOUND_INTACT",
                "FOUND_DAMAGED",
                "UNKNOWN"),
                actualOrder);
    }

    @Test
    @DisplayName("every value is reachable via Enum.valueOf")
    void allValuesReachableViaValueOf() {
        for (MegastructureOriginType o : MegastructureOriginType.values()) {
            assertNotNull(MegastructureOriginType.valueOf(o.name()));
        }
    }
}
