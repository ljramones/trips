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
 * Pins {@link CatalogedKind}'s exact value set, declaration order, and the ordinal-parallel
 * relationship with {@link AssetKind} per v2 Phase E.1 Divergence B resolution.
 */
class CatalogedKindTest {

    @Test
    @DisplayName("CatalogedKind has exactly 5 values (v2 Phase E.1 Divergence B)")
    void fiveValues() {
        assertEquals(5, CatalogedKind.values().length);
    }

    @Test
    @DisplayName("the five documented values are present and exactly spelled")
    void exactValues() {
        Set<String> actual = Arrays.stream(CatalogedKind.values())
                .map(Enum::name)
                .collect(Collectors.toSet());
        assertEquals(Set.of("SHIP", "STATION", "WEAPON_INSTALLATION", "MEGASTRUCTURE", "TRANSPORT_NODE"),
                actual);
    }

    @Test
    @DisplayName("declaration order: SHIP, STATION, WEAPON_INSTALLATION, MEGASTRUCTURE, TRANSPORT_NODE")
    void declarationOrder() {
        List<String> actualOrder = Arrays.stream(CatalogedKind.values())
                .map(Enum::name)
                .collect(Collectors.toList());
        assertEquals(List.of("SHIP", "STATION", "WEAPON_INSTALLATION", "MEGASTRUCTURE", "TRANSPORT_NODE"),
                actualOrder);
    }

    @Test
    @DisplayName("every value is reachable via Enum.valueOf")
    void allValuesReachableViaValueOf() {
        for (CatalogedKind k : CatalogedKind.values()) {
            assertNotNull(CatalogedKind.valueOf(k.name()));
        }
    }

    @Test
    @DisplayName("the four SpaceAsset-paralleling values keep their AssetKind-matching ordinals (parallel-ordinal-stability)")
    void parallelOrdinalsMatchAssetKind() {
        // The Divergence B resolution explicitly preserved ordinal parallelism for the four
        // SpaceAsset-derived values so callers holding an AssetKind ordinal can map cleanly.
        // TRANSPORT_NODE is appended at ordinal 4 (no AssetKind equivalent).
        assertEquals(AssetKind.SHIP.ordinal(), CatalogedKind.SHIP.ordinal());
        assertEquals(AssetKind.STATION.ordinal(), CatalogedKind.STATION.ordinal());
        assertEquals(AssetKind.WEAPON_INSTALLATION.ordinal(), CatalogedKind.WEAPON_INSTALLATION.ordinal());
        assertEquals(AssetKind.MEGASTRUCTURE.ordinal(), CatalogedKind.MEGASTRUCTURE.ordinal());
        assertEquals(4, CatalogedKind.TRANSPORT_NODE.ordinal());
    }

    @Test
    @DisplayName("AssetKind stays focused at 4 values — CatalogedKind extension does not bleed into AssetKind")
    void assetKindUnchangedByCatalogedKindAddition() {
        assertEquals(4, AssetKind.values().length,
                "AssetKind must stay focused on the SpaceAsset sealed hierarchy; Divergence B "
                        + "resolution chose option (β) — a new CatalogedKind enum — over option (α) "
                        + "which would have extended AssetKind itself");
    }
}
