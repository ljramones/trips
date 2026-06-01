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
 * Pins {@link UniverseLifecycle}'s exact value set and declaration order per v2 Phase F.1 §4.1.
 */
class UniverseLifecycleTest {

    @Test
    @DisplayName("UniverseLifecycle has exactly 2 values (v2 Phase F.1 §4.1)")
    void twoValues() {
        assertEquals(2, UniverseLifecycle.values().length);
    }

    @Test
    @DisplayName("the two documented values are present and exactly spelled")
    void exactValues() {
        Set<String> actual = Arrays.stream(UniverseLifecycle.values())
                .map(Enum::name)
                .collect(Collectors.toSet());
        assertEquals(Set.of("AVAILABLE", "DEPRECATED"), actual);
    }

    @Test
    @DisplayName("declaration order: AVAILABLE, DEPRECATED")
    void declarationOrder() {
        List<String> actualOrder = Arrays.stream(UniverseLifecycle.values())
                .map(Enum::name)
                .collect(Collectors.toList());
        assertEquals(List.of("AVAILABLE", "DEPRECATED"), actualOrder);
    }

    @Test
    @DisplayName("every value is reachable via Enum.valueOf")
    void allValuesReachableViaValueOf() {
        for (UniverseLifecycle l : UniverseLifecycle.values()) {
            assertNotNull(UniverseLifecycle.valueOf(l.name()));
        }
    }
}
