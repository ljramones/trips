package com.terranrepublic.infrastructure;

import java.time.Instant;
import java.util.List;

/**
 * A graph vertex for modeled space-transport infrastructure.
 */
public record TransportNode(
        String id,
        String name,
        String source,
        String faction,
        boolean concealed,
        String description,
        NodeType type,
        double positionX,
        double positionY,
        double positionZ,
        List<String> connectedNodeIds,
        double throughputTonsPerTick,
        boolean instantaneousTransit,
        double traversalTimeTicks,
        Instant createdAt,
        Instant modifiedAt
) implements SpaceInfrastructure {

    public TransportNode {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("TransportNode id must be provided");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("TransportNode name must be provided");
        }
        if (type == null) {
            throw new IllegalArgumentException("TransportNode type must be provided");
        }
        if (throughputTonsPerTick < 0 || traversalTimeTicks < 0) {
            throw new IllegalArgumentException("TransportNode throughput and traversal time must not be negative");
        }
        source = source == null ? "" : source;
        faction = faction == null || faction.isBlank() ? "Unknown" : faction;
        description = description == null ? "" : description;
        connectedNodeIds = connectedNodeIds == null ? List.of() : List.copyOf(connectedNodeIds);
        traversalTimeTicks = instantaneousTransit ? 0 : traversalTimeTicks;
        Instant now = Instant.now();
        createdAt = createdAt == null ? now : createdAt;
        modifiedAt = modifiedAt == null ? createdAt : modifiedAt;
    }

    @Override
    public InfrastructureKind kind() {
        return InfrastructureKind.TRANSPORT_NODE;
    }
}
