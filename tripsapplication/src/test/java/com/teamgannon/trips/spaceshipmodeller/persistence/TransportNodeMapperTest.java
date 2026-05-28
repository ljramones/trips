package com.teamgannon.trips.spaceshipmodeller.persistence;

import com.terranrepublic.infrastructure.NodeType;
import com.terranrepublic.infrastructure.TransportNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Comprehensive round-trip coverage for {@link TransportNodeMapper}.
 *
 * <p>Same standard as the SpaceAsset-side mapper tests: parameterised over every
 * {@link NodeType} constant (eight) plus the non-default cases — {@code concealed=true},
 * {@code instantaneousTransit=true} (which the compact constructor zeroes
 * {@code traversalTimeTicks} for), and connected-node-ids populated / empty / null-LOB.
 */
class TransportNodeMapperTest {

    private final TransportNodeMapper mapper = new TransportNodeMapper();

    private static TransportNode with(NodeType type,
                                      boolean concealed,
                                      boolean instantaneousTransit,
                                      List<String> connectedNodeIds) {
        Instant now = Instant.parse("2025-05-01T08:00:00Z");
        return new TransportNode(
                UUID.randomUUID().toString(),
                "Coverage-" + type.name(),
                "Coverage Source",
                "Coverage Faction",
                concealed,
                "Coverage description",
                type,
                10.0,
                20.0,
                30.0,
                connectedNodeIds,
                100.0,
                instantaneousTransit,
                instantaneousTransit ? 0 : 50.0,
                now,
                now);
    }

    // ------------------------------------------------------------------
    // All-fields round-trip
    // ------------------------------------------------------------------

    @Test
    @DisplayName("a fully populated transport node round-trips every field")
    void allFieldsRoundTrip() {
        TransportNode original = with(NodeType.RING_GATE, true, false,
                List.of("node-a", "node-b", "node-c"));
        TransportNode back = mapper.toDomain(mapper.toEntity(original));
        assertEquals(original, back);
        assertEquals(NodeType.RING_GATE, back.type());
        assertTrue(back.concealed());
        assertEquals(List.of("node-a", "node-b", "node-c"), back.connectedNodeIds());
    }

    // ------------------------------------------------------------------
    // connectedNodeIds LOB column
    // ------------------------------------------------------------------

    @Test
    @DisplayName("connectedNodeIds are serialised into the JSON LOB column")
    void connectedNodeIdsSerialisedToLob() {
        TransportNode node = with(NodeType.JUMP_POINT, false, false, List.of("partner-1"));
        TransportNodeEntity entity = mapper.toEntity(node);
        assertNotNull(entity.getConnectedNodeIdsJson());
        assertTrue(entity.getConnectedNodeIdsJson().contains("partner-1"));
    }

    @Test
    @DisplayName("empty connectedNodeIds round-trip to an empty list (not null)")
    void emptyConnectedNodeIdsRoundTrip() {
        TransportNode node = with(NodeType.RELAY, false, false, List.of());
        TransportNode back = mapper.toDomain(mapper.toEntity(node));
        assertTrue(back.connectedNodeIds().isEmpty());
    }

    @Test
    @DisplayName("null connectedNodeIdsJson on the entity surfaces as empty list (legacy-row safety)")
    void nullLobSurfacesEmptyList() {
        TransportNodeEntity entity = mapper.toEntity(with(NodeType.BEACON, false, false, List.of("x")));
        entity.setConnectedNodeIdsJson(null);
        TransportNode back = mapper.toDomain(entity);
        assertTrue(back.connectedNodeIds().isEmpty(), "null LOB must not NPE");
    }

    // ------------------------------------------------------------------
    // Non-default cases (Phase A0 round-trip-loss-class coverage)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("concealed=true survives the round-trip")
    void concealedRoundTrips() {
        TransportNode node = with(NodeType.WORMHOLE_MOUTH, true, false, List.of());
        TransportNode back = mapper.toDomain(mapper.toEntity(node));
        assertTrue(back.concealed());
    }

    @Test
    @DisplayName("instantaneousTransit=true survives the round-trip (and zeros traversalTimeTicks)")
    void instantaneousTransitRoundTrips() {
        TransportNode node = with(NodeType.PORTAL, false, true, List.of());
        TransportNode back = mapper.toDomain(mapper.toEntity(node));
        assertTrue(back.instantaneousTransit());
        assertEquals(0.0, back.traversalTimeTicks(),
                "compact constructor zeroes traversalTimeTicks when instantaneousTransit is true");
    }

    @Test
    @DisplayName("instantaneousTransit=false preserves traversalTimeTicks")
    void nonInstantaneousTransitPreservesTime() {
        TransportNode node = with(NodeType.NAV_HAZARD, false, false, List.of());
        TransportNode back = mapper.toDomain(mapper.toEntity(node));
        assertFalse(back.instantaneousTransit());
        assertEquals(50.0, back.traversalTimeTicks());
    }

    // ------------------------------------------------------------------
    // Enum constant coverage
    // ------------------------------------------------------------------

    @ParameterizedTest
    @EnumSource(NodeType.class)
    @DisplayName("every NodeType constant round-trips through the mapper")
    void everyNodeTypeRoundTrips(NodeType type) {
        TransportNode node = with(type, false, false, List.of());
        TransportNode back = mapper.toDomain(mapper.toEntity(node));
        assertEquals(type, back.type());
    }
}
