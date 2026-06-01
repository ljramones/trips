package com.teamgannon.trips.spaceshipmodeller.persistence;

import com.terranrepublic.infrastructure.NodeType;
import com.terranrepublic.infrastructure.TransportNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;

/**
 * Converts between the immutable domain {@link TransportNode} record and the mutable JPA
 * {@link TransportNodeEntity}.
 * <p>
 * Mirrors the SpaceAsset-side mappers ({@link StationDesignMapper},
 * {@link WeaponInstallationMapper}). The single collection field on {@link TransportNode} —
 * {@code connectedNodeIds} (a {@code List<String>}) — is serialised to a JSON LOB the same way
 * complex collections are handled elsewhere; storing the list as JSON is symmetrical with the
 * other mappers and keeps the entity flat.
 */
@Slf4j
@Component
public class TransportNodeMapper {

    private static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper = new ObjectMapper();

    public TransportNodeEntity toEntity(TransportNode node) {
        TransportNodeEntity entity = new TransportNodeEntity();
        entity.setId(node.id());
        entity.setName(node.name());
        entity.setSource(node.source());
        entity.setFaction(node.faction());
        entity.setConcealed(node.concealed());
        entity.setDescription(node.description());
        entity.setType(node.type());
        entity.setPositionX(node.positionX());
        entity.setPositionY(node.positionY());
        entity.setPositionZ(node.positionZ());
        entity.setConnectedNodeIdsJson(writeIds(node.connectedNodeIds()));
        entity.setThroughputTonsPerTick(node.throughputTonsPerTick());
        entity.setInstantaneousTransit(node.instantaneousTransit());
        entity.setTraversalTimeTicks(node.traversalTimeTicks());
        entity.setCreatedAt(node.createdAt());
        entity.setModifiedAt(node.modifiedAt());
        // v2 Phase F.1 §4.4 — universe scope.
        entity.setUniverseId(node.universeId());
        return entity;
    }

    public TransportNode toDomain(TransportNodeEntity entity) {
        NodeType type = entity.getType() == null ? NodeType.RELAY : entity.getType();
        return new TransportNode(
                entity.getId(),
                entity.getName(),
                entity.getSource(),
                entity.getFaction(),
                entity.isConcealed(),
                entity.getDescription(),
                type,
                entity.getPositionX(),
                entity.getPositionY(),
                entity.getPositionZ(),
                readIds(entity.getConnectedNodeIdsJson()),
                entity.getThroughputTonsPerTick(),
                entity.isInstantaneousTransit(),
                entity.getTraversalTimeTicks(),
                entity.getCreatedAt() != null ? entity.getCreatedAt() : Instant.now(),
                entity.getModifiedAt() != null ? entity.getModifiedAt() : entity.getCreatedAt(),
                entity.getUniverseId());
    }

    private String writeIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(ids);
        } catch (JacksonException e) {
            log.error("Failed to serialise transport-node connectedNodeIds; storing none: {}", e.getMessage());
            return null;
        }
    }

    private List<String> readIds(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, STRING_LIST);
        } catch (JacksonException e) {
            log.error("Failed to deserialise transport-node connectedNodeIds; returning none: {}", e.getMessage());
            return List.of();
        }
    }
}
