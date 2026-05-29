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
 * Pins {@link InteriorGravityType}'s exact value set and declaration order per
 * the v2 Phase D.7 design §4.3.
 */
class InteriorGravityTypeTest {

    @Test
    @DisplayName("InteriorGravityType has exactly 6 values (v2 Phase D.7 §4.3)")
    void sixValues() {
        assertEquals(6, InteriorGravityType.values().length);
    }

    @Test
    @DisplayName("the six documented values are present and exactly spelled")
    void exactValues() {
        Set<String> actual = Arrays.stream(InteriorGravityType.values())
                .map(Enum::name)
                .collect(Collectors.toSet());
        assertEquals(Set.of(
                "NATURAL_MASS",
                "SPIN",
                "ARTIFICIAL_FIELD",
                "MIXED",
                "NONE",
                "UNKNOWN"),
                actual);
    }

    @Test
    @DisplayName("declaration order matches design §4.3 (NATURAL_MASS, SPIN, ARTIFICIAL_FIELD, MIXED, NONE, UNKNOWN)")
    void declarationOrder() {
        List<String> actualOrder = Arrays.stream(InteriorGravityType.values())
                .map(Enum::name)
                .collect(Collectors.toList());
        assertEquals(List.of(
                "NATURAL_MASS",
                "SPIN",
                "ARTIFICIAL_FIELD",
                "MIXED",
                "NONE",
                "UNKNOWN"),
                actualOrder);
    }

    @Test
    @DisplayName("every value is reachable via Enum.valueOf")
    void allValuesReachableViaValueOf() {
        for (InteriorGravityType g : InteriorGravityType.values()) {
            assertNotNull(InteriorGravityType.valueOf(g.name()));
        }
    }
}
