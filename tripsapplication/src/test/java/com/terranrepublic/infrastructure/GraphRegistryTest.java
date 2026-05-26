package com.terranrepublic.infrastructure;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraphRegistryTest {

    @Test
    void validatesThreeNodeGraphAndConduitEndpoints() {
        Instant now = Instant.now();
        TransportNode sol = node("node-sol", "Sol Gate", List.of("node-alpha"), now);
        TransportNode alpha = node("node-alpha", "Alpha Gate", List.of("node-sol", "node-barnard"), now);
        TransportNode barnard = node("node-barnard", "Barnard Gate", List.of("node-alpha"), now);
        Conduit corridor = new Conduit(
                "conduit-1",
                "Alpha-Barnard Corridor",
                "Test",
                "Terran",
                false,
                "Maintained ring corridor",
                "node-alpha",
                "node-barnard",
                5_900_000,
                now,
                now);

        GraphRegistry registry = GraphRegistry.of(List.of(sol, alpha, barnard), List.of(corridor));

        assertEquals(List.of(), registry.validate());
        assertDoesNotThrow(registry::requireValid);
        assertEquals(InfrastructureKind.TRANSPORT_NODE, sol.kind());
        assertEquals(InfrastructureKind.CONDUIT, corridor.kind());
        assertFalse(sol.concealed());
    }

    @Test
    void validationReportsDanglingEdgesAndConduits() {
        Instant now = Instant.now();
        TransportNode sol = node("node-sol", "Sol Gate", List.of("node-missing"), now);
        Conduit badConduit = new Conduit(
                "conduit-bad",
                "Broken Corridor",
                "Test",
                "Terran",
                false,
                "Invalid endpoint",
                "node-sol",
                "node-nowhere",
                10,
                now,
                now);

        GraphRegistry registry = GraphRegistry.of(List.of(sol), List.of(badConduit));

        List<String> errors = registry.validate();
        assertEquals(2, errors.size());
        assertTrue(errors.stream().anyMatch(error -> error.contains("node-missing")));
        assertTrue(errors.stream().anyMatch(error -> error.contains("node-nowhere")));
        assertThrows(IllegalStateException.class, registry::requireValid);
    }

    private static TransportNode node(String id, String name, List<String> connectedIds, Instant now) {
        return new TransportNode(
                id,
                name,
                "Test",
                "Terran",
                false,
                "Test node",
                NodeType.RING_GATE,
                0,
                0,
                0,
                connectedIds,
                1_000,
                true,
                0,
                now,
                now);
    }
}
