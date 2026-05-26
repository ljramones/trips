package com.teamgannon.trips.spaceshipmodeller.planner;

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.teamgannon.trips.spaceshipmodeller.integration.ManeuverNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * Converts between {@link SavedTransferPlan} and the JPA {@link TransferPlanEntity}, serialising the
 * maneuver-node list to/from JSON.
 */
@Slf4j
@Component
public class TransferPlanMapper {

    private static final TypeReference<List<ManeuverNode>> NODE_LIST = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper = new ObjectMapper();

    public TransferPlanEntity toEntity(SavedTransferPlan plan) {
        TransferPlanEntity e = new TransferPlanEntity();
        e.setId(plan.id());
        e.setShipId(plan.shipId());
        e.setShipName(plan.shipName());
        e.setSolarSystemId(plan.solarSystemId());
        e.setTransferType(plan.transferType());
        e.setOriginName(plan.originName());
        e.setOriginAu(plan.originAu());
        e.setDestinationName(plan.destinationName());
        e.setDestinationAu(plan.destinationAu());
        e.setCentralMassSolar(plan.centralMassSolar());
        e.setManeuverNodesJson(writeNodes(plan.nodes()));
        e.setTotalDeltaVKmps(plan.totalDeltaVKmps());
        e.setTotalPropellantTons(plan.totalPropellantTons());
        e.setAvailablePropellantTons(plan.availablePropellantTons());
        e.setTransferTimeDays(plan.transferTimeDays());
        e.setShipDeltaVKmps(plan.shipDeltaVKmps());
        e.setFeasible(plan.feasible());
        e.setPropellantSufficient(plan.propellantSufficient());
        e.setStatus(plan.status());
        e.setCreatedAt(plan.createdAt());
        return e;
    }

    public SavedTransferPlan toDomain(TransferPlanEntity e) {
        return new SavedTransferPlan(
                e.getId(), e.getShipId(), e.getShipName(), e.getSolarSystemId(), e.getTransferType(),
                e.getOriginName(), e.getOriginAu(), e.getDestinationName(), e.getDestinationAu(),
                e.getCentralMassSolar(), readNodes(e.getManeuverNodesJson()), e.getTotalDeltaVKmps(),
                e.getTotalPropellantTons(), e.getAvailablePropellantTons(), e.getTransferTimeDays(),
                e.getShipDeltaVKmps(), e.isFeasible(), e.isPropellantSufficient(), e.getStatus(),
                e.getCreatedAt() != null ? e.getCreatedAt() : Instant.now());
    }

    private String writeNodes(List<ManeuverNode> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(nodes);
        } catch (JacksonException ex) {
            log.error("Failed to serialise maneuver nodes: {}", ex.getMessage());
            return null;
        }
    }

    private List<ManeuverNode> readNodes(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, NODE_LIST);
        } catch (JacksonException ex) {
            log.error("Failed to deserialise maneuver nodes: {}", ex.getMessage());
            return List.of();
        }
    }
}
