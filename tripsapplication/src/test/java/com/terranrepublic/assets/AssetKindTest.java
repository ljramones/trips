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
 * Pins {@link AssetKind}'s exact value set, declaration order, and ordinal stability
 * per the v2 Phase D.7 design (Step 2 — AssetKind extension for the Megastructure subtype).
 */
class AssetKindTest {

    @Test
    @DisplayName("AssetKind has exactly 4 values (v2 Phase D.7 extension)")
    void fourValues() {
        assertEquals(4, AssetKind.values().length);
    }

    @Test
    @DisplayName("the four documented values are present and exactly spelled")
    void exactValues() {
        Set<String> actual = Arrays.stream(AssetKind.values())
                .map(Enum::name)
                .collect(Collectors.toSet());
        assertEquals(Set.of("SHIP", "STATION", "WEAPON_INSTALLATION", "MEGASTRUCTURE"),
                actual);
    }

    @Test
    @DisplayName("declaration order matches design (SHIP, STATION, WEAPON_INSTALLATION, MEGASTRUCTURE)")
    void declarationOrder() {
        List<String> actualOrder = Arrays.stream(AssetKind.values())
                .map(Enum::name)
                .collect(Collectors.toList());
        assertEquals(List.of("SHIP", "STATION", "WEAPON_INSTALLATION", "MEGASTRUCTURE"),
                actualOrder);
    }

    @Test
    @DisplayName("every value is reachable via Enum.valueOf")
    void allValuesReachableViaValueOf() {
        for (AssetKind k : AssetKind.values()) {
            assertNotNull(AssetKind.valueOf(k.name()));
        }
    }

    @Test
    @DisplayName("the original three pre-Phase-D.7 values keep their original ordinals")
    void originalThreeValuesPreserved() {
        assertEquals(0, AssetKind.SHIP.ordinal());
        assertEquals(1, AssetKind.STATION.ordinal());
        assertEquals(2, AssetKind.WEAPON_INSTALLATION.ordinal());
    }

    @Test
    @DisplayName("MEGASTRUCTURE lands at ordinal 3 (appended, not interleaved)")
    void megastructureAppendedAtOrdinal3() {
        assertEquals(3, AssetKind.MEGASTRUCTURE.ordinal());
    }
}
