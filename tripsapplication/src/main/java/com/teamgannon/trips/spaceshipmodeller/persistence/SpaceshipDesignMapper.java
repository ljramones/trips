package com.teamgannon.trips.spaceshipmodeller.persistence;

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.teamgannon.trips.spaceshipmodeller.core.CarriedCraft;
import com.teamgannon.trips.spaceshipmodeller.core.MassBudget;
import com.terranrepublic.assets.Armament;
import com.terranrepublic.assets.OperationalState;
import com.terranrepublic.assets.SpaceshipDesign;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * Converts between the immutable domain {@link SpaceshipDesign} record and the mutable JPA
 * {@link SpaceshipEntity}.
 * <p>
 * Collection fields are serialised to JSON for storage and parsed back on read, mirroring how other
 * TRIPS entities persist collections. The mapper owns a private
 * {@link ObjectMapper} so it does not depend on the application's shared, possibly differently-configured
 * instance.
 */
@Slf4j
@Component
public class SpaceshipDesignMapper {

    private static final TypeReference<List<CarriedCraft>> CARRIED_CRAFT_LIST = new TypeReference<>() {
    };
    private static final TypeReference<List<Armament>> ARMAMENT_LIST = new TypeReference<>() {
    };
    private static final TypeReference<Set<String>> NETWORK_ID_SET = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Converts a domain design into a persistable entity, preserving the design's id and creation time.
     *
     * @param design the domain design
     * @return a new entity ready to be saved
     */
    public SpaceshipEntity toEntity(SpaceshipDesign design) {
        SpaceshipEntity entity = new SpaceshipEntity();
        entity.setId(design.id());
        entity.setName(design.name());
        entity.setDesignation(design.designation());
        entity.setShipClass(design.shipClass());
        entity.setDriveType(design.driveType());

        MassBudget mass = design.massBudget();
        entity.setStructureMassTons(mass.structureMassTons());
        entity.setEngineMassTons(mass.engineMassTons());
        entity.setPropellantMassTons(mass.propellantMassTons());
        entity.setPayloadMassTons(mass.payloadMassTons());
        entity.setCrewMassTons(mass.crewMassTons());
        entity.setRadiatorMassTons(mass.radiatorMassTons());

        entity.setCrewComplement(design.crewComplement());
        entity.setLengthMeters(design.lengthMeters());
        entity.setCarriedCraftJson(writeCarriedCraft(design.carriedCraft()));
        entity.setArmamentsJson(writeArmaments(design.armaments()));
        entity.setIconPath(design.iconPath());
        entity.setDescription(design.description());
        entity.setSourceType(design.sourceType());
        entity.setSourceUniverse(design.sourceUniverse());
        entity.setFaction(design.faction());
        entity.setEra(design.era());
        // Phase A0 (Constructs): close the round-trip-loss bug documented in
        // constructs-existing-hierarchies.md §4.4. Before V6 these two fields
        // were silently dropped on persist and the compact constructor
        // reconstituted operationalState as OPERATIONAL on read.
        entity.setConcealed(design.concealed());
        entity.setOperationalState(design.operationalState());
        entity.setCreatedAt(design.createdAt());
        // v2 Phase E.1 §5.4 — round-trip the GateNetwork transponder access set.
        entity.setDefaultAccessibleNetworkIdsJson(writeNetworkIds(design.defaultAccessibleNetworkIds()));
        return entity;
    }

    /**
     * Converts a persisted entity back into an immutable domain design.
     *
     * @param entity the persisted entity
     * @return the reconstructed domain design
     */
    public SpaceshipDesign toDomain(SpaceshipEntity entity) {
        MassBudget mass = new MassBudget(
                entity.getStructureMassTons(),
                entity.getEngineMassTons(),
                entity.getPropellantMassTons(),
                entity.getPayloadMassTons(),
                entity.getCrewMassTons(),
                entity.getRadiatorMassTons());

        OperationalState state = entity.getOperationalState();
        if (state == null) {
            // Defensive: rows materialised before V6 ran could surface null.
            // The compact constructor would also default to OPERATIONAL but
            // we prefer to be explicit at the persistence boundary.
            state = OperationalState.OPERATIONAL;
        }
        return new SpaceshipDesign(
                entity.getId(),
                entity.getName(),
                entity.getDesignation(),
                entity.getShipClass(),
                entity.getDriveType(),
                mass,
                entity.getCrewComplement(),
                entity.getLengthMeters(),
                readCarriedCraft(entity.getCarriedCraftJson()),
                readArmaments(entity.getArmamentsJson()),
                entity.getIconPath(),
                entity.getDescription(),
                entity.getSourceType(),
                entity.getSourceUniverse(),
                entity.getFaction(),
                entity.isConcealed(),
                state,
                entity.getEra(),
                entity.getCreatedAt() != null ? entity.getCreatedAt() : Instant.now(),
                readNetworkIds(entity.getDefaultAccessibleNetworkIdsJson()));
    }

    private String writeCarriedCraft(List<CarriedCraft> carriedCraft) {
        if (carriedCraft == null || carriedCraft.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(carriedCraft);
        } catch (JacksonException e) {
            log.error("Failed to serialise carried craft; storing none: {}", e.getMessage());
            return null;
        }
    }

    private List<CarriedCraft> readCarriedCraft(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, CARRIED_CRAFT_LIST);
        } catch (JacksonException e) {
            log.error("Failed to deserialise carried craft; returning none: {}", e.getMessage());
            return List.of();
        }
    }

    private String writeArmaments(List<Armament> armaments) {
        if (armaments == null || armaments.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(armaments);
        } catch (JacksonException e) {
            log.error("Failed to serialise armaments; storing none: {}", e.getMessage());
            return null;
        }
    }

    private List<Armament> readArmaments(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, ARMAMENT_LIST);
        } catch (JacksonException e) {
            log.error("Failed to deserialise armaments; returning none: {}", e.getMessage());
            return List.of();
        }
    }

    /** v2 Phase E.1 §5.4 — serialise the network-id set; empty/null persists as null. */
    private String writeNetworkIds(Set<String> networkIds) {
        if (networkIds == null || networkIds.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(networkIds);
        } catch (JacksonException e) {
            log.error("Failed to serialise default-accessible network ids; storing none: {}", e.getMessage());
            return null;
        }
    }

    /** v2 Phase E.1 §5.4 — deserialise the network-id set; null/blank reads back as empty set. */
    private Set<String> readNetworkIds(String json) {
        if (json == null || json.isBlank()) {
            return Set.of();
        }
        try {
            return objectMapper.readValue(json, NETWORK_ID_SET);
        } catch (JacksonException e) {
            log.error("Failed to deserialise default-accessible network ids; returning none: {}", e.getMessage());
            return Set.of();
        }
    }
}
