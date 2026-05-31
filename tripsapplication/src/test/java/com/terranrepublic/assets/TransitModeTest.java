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
 * Pins {@link TransitMode}'s exact value set and declaration order per v2 Phase E.1 §4.1.
 */
class TransitModeTest {

    @Test
    @DisplayName("TransitMode has exactly 5 values (v2 Phase E.1 §4.1)")
    void fiveValues() {
        assertEquals(5, TransitMode.values().length);
    }

    @Test
    @DisplayName("the five documented values are present and exactly spelled")
    void exactValues() {
        Set<String> actual = Arrays.stream(TransitMode.values())
                .map(Enum::name)
                .collect(Collectors.toSet());
        assertEquals(Set.of("SUBLIGHT", "JUMP_POINT", "WORMHOLE", "JUMP_GATE", "WARP"), actual);
    }

    @Test
    @DisplayName("declaration order: SUBLIGHT, JUMP_POINT, WORMHOLE, JUMP_GATE, WARP")
    void declarationOrder() {
        List<String> actualOrder = Arrays.stream(TransitMode.values())
                .map(Enum::name)
                .collect(Collectors.toList());
        assertEquals(List.of("SUBLIGHT", "JUMP_POINT", "WORMHOLE", "JUMP_GATE", "WARP"), actualOrder);
    }

    @Test
    @DisplayName("every value is reachable via Enum.valueOf")
    void allValuesReachableViaValueOf() {
        for (TransitMode m : TransitMode.values()) {
            assertNotNull(TransitMode.valueOf(m.name()));
        }
    }
}
