package com.teamgannon.trips.spaceshipmodeller.persistence;

import com.teamgannon.trips.spaceshipmodeller.core.CarriedCraft;
import com.terranrepublic.assets.Armament;
import com.terranrepublic.assets.CatalogOperationalStatus;
import com.terranrepublic.assets.CatalogProvenance;
import com.terranrepublic.assets.Mobility;
import com.terranrepublic.assets.OperationalState;
import com.terranrepublic.assets.SourceType;
import com.terranrepublic.assets.StationDesign;
import com.terranrepublic.assets.StationFunction;
import com.terranrepublic.assets.TechLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * Converts between the immutable domain {@link StationDesign} record and the mutable JPA
 * {@link StationEntity}.
 * <p>
 * Collection fields are serialised to JSON for storage and parsed back on read, mirroring how
 * {@link SpaceshipDesignMapper} persists collections. The mapper owns a private
 * {@link ObjectMapper} so it does not depend on the application's shared, possibly
 * differently-configured instance.
 * <p>
 * Every field on {@link StationDesign} round-trips through this mapper; the comprehensive test
 * coverage in {@code StationDesignMapperTest} is the regression guard against the round-trip-loss
 * pattern that produced Phase A0's V6 migration.
 */
@Slf4j
@Component
public class StationDesignMapper {

    private static final TypeReference<List<CarriedCraft>> CARRIED_CRAFT_LIST = new TypeReference<>() {
    };
    private static final TypeReference<List<Armament>> ARMAMENT_LIST = new TypeReference<>() {
    };
    private static final TypeReference<Set<StationFunction>> STATION_FUNCTION_SET = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Converts a domain design into a persistable entity, preserving identity and timestamps.
     *
     * @param design the domain design
     * @return a new entity ready to be saved
     */
    public StationEntity toEntity(StationDesign design) {
        StationEntity entity = new StationEntity();
        entity.setId(design.id());
        entity.setName(design.name());
        entity.setDesignation(design.designation());
        entity.setStationType(design.stationType());
        entity.setSource(design.source());
        entity.setFaction(design.faction());
        entity.setConcealed(design.concealed());
        entity.setAllegiance(design.allegiance());
        entity.setDescription(design.description());
        entity.setOverallSpanMeters(design.overallSpanMeters());
        entity.setInteriorSpanMeters(design.interiorSpanMeters());
        entity.setDryMassTons(design.dryMassTons());
        entity.setArmourThicknessMeters(design.armourThicknessMeters());
        entity.setCrewCapacity(design.crewCapacity());
        entity.setCrewComplement(design.crewComplement());
        entity.setPressurizedVolumeM3(design.pressurizedVolumeM3());
        entity.setMobility(design.mobility());
        // Domain invariant: auxiliaryDrive is only valid for non-fixed stations.
        // The compact constructor on StationDesign enforces it on the way in,
        // so this branch is defensive only.
        entity.setAuxiliaryDrive(design.mobility() == Mobility.FIXED ? null : design.auxiliaryDrive());
        entity.setCarriedCraftJson(writeCarriedCraft(design.carriedCraft()));
        entity.setArmamentsJson(writeArmaments(design.armaments()));
        entity.setHangarVolumeM3(design.hangarVolumeM3());
        entity.setCarrierCapable(design.carrierCapable());
        entity.setTechLevel(design.techLevel());
        entity.setCategory(design.category());
        entity.setOperationalState(design.operationalState());
        entity.setCreatedAt(design.createdAt());
        entity.setModifiedAt(design.modifiedAt());
        // ----- v2 Phase D.6 — function + provenance axes -----
        entity.setPrimaryFunction(design.primaryFunction());
        entity.setSecondaryFunctionsJson(writeSecondaryFunctions(design.secondaryFunctions()));
        CatalogProvenance provenance = design.provenance();
        entity.setProvenanceSourceType(provenance.sourceType());
        entity.setProvenanceSourceUniverse(provenance.sourceUniverse());
        entity.setProvenanceSourceWork(provenance.sourceWork());
        entity.setProvenanceStatus(provenance.status());
        return entity;
    }

    /**
     * Converts a persisted entity back into an immutable domain design.
     *
     * @param entity the persisted entity
     * @return the reconstructed domain design
     */
    public StationDesign toDomain(StationEntity entity) {
        Mobility mobility = entity.getMobility() == null ? Mobility.FIXED : entity.getMobility();
        TechLevel techLevel = entity.getTechLevel() == null ? TechLevel.UNKNOWN : entity.getTechLevel();
        OperationalState operationalState = entity.getOperationalState() == null
                ? OperationalState.OPERATIONAL
                : entity.getOperationalState();
        // ----- v2 Phase D.6 — function + provenance axes -----
        StationFunction primaryFunction = entity.getPrimaryFunction() == null
                ? StationFunction.UNKNOWN
                : entity.getPrimaryFunction();
        Set<StationFunction> secondaryFunctions = readSecondaryFunctions(entity.getSecondaryFunctionsJson());
        CatalogProvenance provenance = new CatalogProvenance(
                entity.getProvenanceSourceType() == null ? SourceType.UNKNOWN : entity.getProvenanceSourceType(),
                entity.getProvenanceSourceUniverse() == null ? "" : entity.getProvenanceSourceUniverse(),
                entity.getProvenanceSourceWork(),
                entity.getProvenanceStatus() == null ? CatalogOperationalStatus.UNKNOWN : entity.getProvenanceStatus());
        return new StationDesign(
                entity.getId(),
                entity.getName(),
                entity.getDesignation(),
                entity.getStationType(),
                entity.getFaction(),
                entity.isConcealed(),
                entity.getAllegiance(),
                entity.getDescription(),
                entity.getOverallSpanMeters(),
                entity.getInteriorSpanMeters(),
                entity.getDryMassTons(),
                entity.getArmourThicknessMeters(),
                entity.getCrewCapacity(),
                entity.getCrewComplement(),
                entity.getPressurizedVolumeM3(),
                mobility,
                mobility == Mobility.FIXED ? null : entity.getAuxiliaryDrive(),
                readCarriedCraft(entity.getCarriedCraftJson()),
                readArmaments(entity.getArmamentsJson()),
                entity.getHangarVolumeM3(),
                entity.isCarrierCapable(),
                techLevel,
                entity.getCategory(),
                operationalState,
                entity.getCreatedAt() != null ? entity.getCreatedAt() : Instant.now(),
                entity.getModifiedAt() != null ? entity.getModifiedAt() : entity.getCreatedAt(),
                primaryFunction,
                secondaryFunctions,
                provenance);
    }

    private String writeSecondaryFunctions(Set<StationFunction> functions) {
        if (functions == null || functions.isEmpty()) {
            // Empty set persists as null (matches the existing carriedCraftJson / armamentsJson
            // null-safe convention). The null-safe read returns Set.of() either way.
            return null;
        }
        try {
            return objectMapper.writeValueAsString(functions);
        } catch (JacksonException e) {
            log.error("Failed to serialise station secondary functions; storing none: {}", e.getMessage());
            return null;
        }
    }

    private Set<StationFunction> readSecondaryFunctions(String json) {
        if (json == null || json.isBlank()) {
            return Set.of();
        }
        try {
            return objectMapper.readValue(json, STATION_FUNCTION_SET);
        } catch (JacksonException e) {
            log.error("Failed to deserialise station secondary functions; returning none: {}", e.getMessage());
            return Set.of();
        }
    }

    private String writeCarriedCraft(List<CarriedCraft> carriedCraft) {
        if (carriedCraft == null || carriedCraft.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(carriedCraft);
        } catch (JacksonException e) {
            log.error("Failed to serialise station carried craft; storing none: {}", e.getMessage());
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
            log.error("Failed to deserialise station carried craft; returning none: {}", e.getMessage());
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
            log.error("Failed to serialise station armaments; storing none: {}", e.getMessage());
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
            log.error("Failed to deserialise station armaments; returning none: {}", e.getMessage());
            return List.of();
        }
    }
}
