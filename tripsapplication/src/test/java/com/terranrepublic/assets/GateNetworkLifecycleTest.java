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
 * Pins {@link GateNetworkLifecycle}'s exact value set and declaration order per v2 Phase E.1 §5.2.
 */
class GateNetworkLifecycleTest {

    @Test
    @DisplayName("GateNetworkLifecycle has exactly 3 values (v2 Phase E.1 §5.2)")
    void threeValues() {
        assertEquals(3, GateNetworkLifecycle.values().length);
    }

    @Test
    @DisplayName("the three documented values are present and exactly spelled")
    void exactValues() {
        Set<String> actual = Arrays.stream(GateNetworkLifecycle.values())
                .map(Enum::name)
                .collect(Collectors.toSet());
        assertEquals(Set.of("ACTIVE", "DERELICT", "REACTIVATED"), actual);
    }

    @Test
    @DisplayName("declaration order: ACTIVE, DERELICT, REACTIVATED")
    void declarationOrder() {
        List<String> actualOrder = Arrays.stream(GateNetworkLifecycle.values())
                .map(Enum::name)
                .collect(Collectors.toList());
        assertEquals(List.of("ACTIVE", "DERELICT", "REACTIVATED"), actualOrder);
    }

    @Test
    @DisplayName("every value is reachable via Enum.valueOf")
    void allValuesReachableViaValueOf() {
        for (GateNetworkLifecycle l : GateNetworkLifecycle.values()) {
            assertNotNull(GateNetworkLifecycle.valueOf(l.name()));
        }
    }
}
