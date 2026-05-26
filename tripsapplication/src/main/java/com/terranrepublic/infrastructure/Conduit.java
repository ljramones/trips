package com.terranrepublic.infrastructure;

import java.time.Instant;

/**
 * A physical transit corridor between two transport nodes, such as a tether or maintained gate corridor.
 */
public record Conduit(
        String id,
        String name,
        String source,
        String faction,
        boolean concealed,
        String description,
        String fromNodeId,
        String toNodeId,
        double lengthKm,
        Instant createdAt,
        Instant modifiedAt
) implements SpaceInfrastructure {

    public Conduit {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Conduit id must be provided");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Conduit name must be provided");
        }
        if (fromNodeId == null || fromNodeId.isBlank() || toNodeId == null || toNodeId.isBlank()) {
            throw new IllegalArgumentException("Conduit endpoints must be provided");
        }
        if (lengthKm < 0) {
            throw new IllegalArgumentException("Conduit length must not be negative");
        }
        source = source == null ? "" : source;
        faction = faction == null || faction.isBlank() ? "Unknown" : faction;
        description = description == null ? "" : description;
        Instant now = Instant.now();
        createdAt = createdAt == null ? now : createdAt;
        modifiedAt = modifiedAt == null ? createdAt : modifiedAt;
    }

    @Override
    public InfrastructureKind kind() {
        return InfrastructureKind.CONDUIT;
    }
}
