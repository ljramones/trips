package com.terranrepublic.assets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins {@link CatalogOperationalStatus}'s exact value set per the v2 Phase D.6 design §4.3.
 */
class CatalogOperationalStatusTest {

    @Test
    @DisplayName("CatalogOperationalStatus has exactly 6 values (v2 design §4.3)")
    void sixValues() {
        assertEquals(6, CatalogOperationalStatus.values().length);
    }

    @Test
    @DisplayName("the six documented values are present and exactly spelled")
    void exactValues() {
        Set<String> actual = Arrays.stream(CatalogOperationalStatus.values())
                .map(Enum::name)
                .collect(Collectors.toSet());
        assertEquals(Set.of("HISTORIC", "ACTIVE", "PLANNED", "CANCELLED", "FICTIONAL", "UNKNOWN"),
                actual);
    }

    @Test
    @DisplayName("CatalogOperationalStatus is distinct from OperationalState (no name collisions)")
    void notACollisionWithOperationalState() {
        Set<String> catalogStatus = Arrays.stream(CatalogOperationalStatus.values())
                .map(Enum::name).collect(Collectors.toSet());
        Set<String> physicalState = Arrays.stream(OperationalState.values())
                .map(Enum::name).collect(Collectors.toSet());
        catalogStatus.retainAll(physicalState);
        // The two enums share zero value names. (OperationalState has no UNKNOWN — confirmed
        // from the enum: OPERATIONAL/DAMAGED/DERELICT/WRECK/UNDER_CONSTRUCTION/SALVAGED.) Two
        // axes, two value sets — no accidental string-match between a status and a state.
        assertTrue(catalogStatus.isEmpty(),
                "CatalogOperationalStatus and OperationalState must share no value names; "
                        + "intersection was: " + catalogStatus);
    }

    @Test
    @DisplayName("every value is reachable via Enum.valueOf")
    void allValuesReachableViaValueOf() {
        for (CatalogOperationalStatus s : CatalogOperationalStatus.values()) {
            assertNotNull(CatalogOperationalStatus.valueOf(s.name()));
        }
    }
}
