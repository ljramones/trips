package com.terranrepublic.assets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Phase F.2 §4.2 — pins {@link AliasTargetKind}'s exact value set and declaration order.
 */
class AliasTargetKindTest {

    @Test
    @DisplayName("AliasTargetKind has exactly 2 values (F.2 §4.2)")
    void twoValues() {
        assertEquals(2, AliasTargetKind.values().length);
    }

    @Test
    @DisplayName("the two documented values are present and exactly spelled")
    void exactValues() {
        Set<String> actual = Arrays.stream(AliasTargetKind.values())
                .map(Enum::name)
                .collect(Collectors.toSet());
        assertEquals(Set.of("STAR", "EXOPLANET"), actual);
    }

    @Test
    @DisplayName("declaration order: STAR, EXOPLANET")
    void declarationOrder() {
        List<String> actualOrder = Arrays.stream(AliasTargetKind.values())
                .map(Enum::name)
                .toList();
        assertEquals(List.of("STAR", "EXOPLANET"), actualOrder);
    }

    @ParameterizedTest
    @EnumSource(AliasTargetKind.class)
    @DisplayName("every value is reachable via Enum.valueOf")
    void allValuesReachableViaValueOf(AliasTargetKind kind) {
        assertNotNull(AliasTargetKind.valueOf(kind.name()));
    }
}
