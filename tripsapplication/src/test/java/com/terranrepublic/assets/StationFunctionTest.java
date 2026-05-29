package com.terranrepublic.assets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins {@link StationFunction}'s exact value set so the v2 Phase D.6 design contract can't
 * silently drift. The design originally claimed 26 values in the prose; the §4.1 tables actually
 * enumerate 30 across six functional groups. The 30-value table is the source of truth.
 */
class StationFunctionTest {

    @Test
    @DisplayName("StationFunction has exactly 30 values (v2 design §4.1)")
    void thirtyValues() {
        assertEquals(30, StationFunction.values().length,
                "v2 Phase D.6 design pins 30 values across six functional groups");
    }

    @Test
    @DisplayName("the seven Military values are present and exactly spelled")
    void militaryValues() {
        assertContainsAll("MILITARY_COMMAND", "WEAPONS_PLATFORM", "DEFENSIVE", "SURVEILLANCE",
                "BORDER_CONTROL", "FLEET_ANCHORAGE", "FLEET_REPAIR");
    }

    @Test
    @DisplayName("the seven Governance + civilian-core values are present and exactly spelled")
    void governanceCivilianCoreValues() {
        assertContainsAll("GOVERNMENT_ADMINISTRATION", "DIPLOMATIC", "RESEARCH", "RESIDENTIAL",
                "COMMERCIAL", "TOURISM", "MEDICAL_QUARANTINE");
    }

    @Test
    @DisplayName("the six Industrial values are present and exactly spelled")
    void industrialValues() {
        assertContainsAll("INDUSTRIAL", "SHIPBUILDING", "MINING_REFINING", "LOGISTICS_DEPOT",
                "AGRICULTURAL_BIOSPHERE", "ENERGY_COLLECTION");
    }

    @Test
    @DisplayName("the three Transit + infrastructure values are present and exactly spelled")
    void transitInfrastructureValues() {
        assertContainsAll("TRANSPORTATION_HUB", "COMMUNICATION_RELAY", "NAVIGATION_BEACON");
    }

    @Test
    @DisplayName("the five Specialized values are present and exactly spelled")
    void specializedValues() {
        assertContainsAll("COLONIZATION", "PENAL", "CULTURAL_EDUCATIONAL", "TERRAFORMING_CONTROL",
                "CONTAINMENT");
    }

    @Test
    @DisplayName("the two catch-all values are present and exactly spelled")
    void catchAllValues() {
        assertContainsAll("MULTI_ROLE", "UNKNOWN");
    }

    @Test
    @DisplayName("DERELICT_RUIN is NOT a StationFunction (v2 removed it; OperationalState.DERELICT covers status)")
    void derelictRuinIsAbsent() {
        Set<String> names = Arrays.stream(StationFunction.values())
                .map(Enum::name)
                .collect(Collectors.toSet());
        assertTrue(!names.contains("DERELICT_RUIN"),
                "DERELICT_RUIN was removed in v2; derelict-ness is a status, not a function");
    }

    @Test
    @DisplayName("documented order: Military first → Catch-alls last with the catch-alls trailing")
    void documentedOrder() {
        // The enum file groups Military → Governance → Industrial → Transit → Specialized →
        // Catch-alls. The catch-all values are the last two declared, in that order.
        StationFunction[] all = StationFunction.values();
        assertEquals(StationFunction.MULTI_ROLE, all[all.length - 2],
                "MULTI_ROLE is the penultimate value in declaration order");
        assertEquals(StationFunction.UNKNOWN, all[all.length - 1],
                "UNKNOWN is the final value in declaration order");

        // Spot-check that MILITARY_COMMAND is the first value (Military group leads).
        assertEquals(StationFunction.MILITARY_COMMAND, all[0],
                "MILITARY_COMMAND is the first value (Military group leads)");
    }

    @Test
    @DisplayName("every value is reachable via Enum.valueOf")
    void allValuesReachableViaValueOf() {
        for (StationFunction f : StationFunction.values()) {
            assertNotNull(StationFunction.valueOf(f.name()));
        }
    }

    private static void assertContainsAll(String... expected) {
        Set<String> actual = Arrays.stream(StationFunction.values()).map(Enum::name).collect(Collectors.toSet());
        List<String> missing = Arrays.stream(expected).filter(e -> !actual.contains(e)).toList();
        assertTrue(missing.isEmpty(), "missing values: " + missing);
    }
}
