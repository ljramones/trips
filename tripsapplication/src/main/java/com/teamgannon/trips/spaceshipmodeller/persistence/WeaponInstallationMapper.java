package com.teamgannon.trips.spaceshipmodeller.persistence;

import com.terranrepublic.assets.Armament;
import com.terranrepublic.assets.OperationalState;
import com.terranrepublic.assets.TechLevel;
import com.terranrepublic.assets.WeaponInstallation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;

/**
 * Converts between the immutable domain {@link WeaponInstallation} record and the mutable JPA
 * {@link WeaponInstallationEntity}.
 * <p>
 * Mirrors {@link StationDesignMapper}: every field on {@link WeaponInstallation} round-trips
 * through this mapper. The {@code armaments} collection field is serialised to a JSON LOB on
 * the way to the database and parsed back on the way out, the same pattern used by
 * {@link SpaceshipDesignMapper} and {@link StationDesignMapper}.
 */
@Slf4j
@Component
public class WeaponInstallationMapper {

    private static final TypeReference<List<Armament>> ARMAMENT_LIST = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper = new ObjectMapper();

    public WeaponInstallationEntity toEntity(WeaponInstallation design) {
        WeaponInstallationEntity entity = new WeaponInstallationEntity();
        entity.setId(design.id());
        entity.setName(design.name());
        entity.setDesignation(design.designation());
        entity.setInstallationType(design.installationType());
        entity.setEmplacement(design.emplacement());
        entity.setSource(design.source());
        entity.setFaction(design.faction());
        entity.setConcealed(design.concealed());
        entity.setDescription(design.description());
        entity.setDryMassTons(design.dryMassTons());
        entity.setFootprintSpanMeters(design.footprintSpanMeters());
        entity.setMobile(design.mobile());
        entity.setCrewComplement(design.crewComplement());
        entity.setArmamentsJson(writeArmaments(design.armaments()));
        entity.setTechLevel(design.techLevel());
        entity.setCategory(design.category());
        entity.setOperationalState(design.operationalState());
        entity.setCreatedAt(design.createdAt());
        entity.setModifiedAt(design.modifiedAt());
        return entity;
    }

    public WeaponInstallation toDomain(WeaponInstallationEntity entity) {
        TechLevel techLevel = entity.getTechLevel() == null ? TechLevel.UNKNOWN : entity.getTechLevel();
        OperationalState operationalState = entity.getOperationalState() == null
                ? OperationalState.OPERATIONAL
                : entity.getOperationalState();
        return new WeaponInstallation(
                entity.getId(),
                entity.getName(),
                entity.getDesignation(),
                entity.getInstallationType(),
                entity.getEmplacement(),
                entity.getSource(),
                entity.getFaction(),
                entity.isConcealed(),
                entity.getDescription(),
                entity.getDryMassTons(),
                entity.getFootprintSpanMeters(),
                entity.isMobile(),
                entity.getCrewComplement(),
                readArmaments(entity.getArmamentsJson()),
                techLevel,
                entity.getCategory(),
                operationalState,
                entity.getCreatedAt() != null ? entity.getCreatedAt() : Instant.now(),
                entity.getModifiedAt() != null ? entity.getModifiedAt() : entity.getCreatedAt());
    }

    private String writeArmaments(List<Armament> armaments) {
        if (armaments == null || armaments.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(armaments);
        } catch (JacksonException e) {
            log.error("Failed to serialise weapon-installation armaments; storing none: {}", e.getMessage());
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
            log.error("Failed to deserialise weapon-installation armaments; returning none: {}", e.getMessage());
            return List.of();
        }
    }
}
