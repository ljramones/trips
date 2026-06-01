package com.teamgannon.trips.spaceshipmodeller.persistence;

import com.terranrepublic.assets.Armament;
import com.terranrepublic.assets.CatalogOperationalStatus;
import com.terranrepublic.assets.CatalogProvenance;
import com.terranrepublic.assets.InteriorGravityType;
import com.terranrepublic.assets.Megastructure;
import com.terranrepublic.assets.MegastructureArchetype;
import com.terranrepublic.assets.MegastructureOriginType;
import com.terranrepublic.assets.Mobility;
import com.terranrepublic.assets.OperationalState;
import com.terranrepublic.assets.SourceType;
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
 * Converts between the immutable domain {@link Megastructure} record and the mutable JPA
 * {@link MegastructureEntity}.
 * <p>
 * Collection fields are serialised to JSON for storage and parsed back on read, mirroring
 * the {@link StationDesignMapper} pattern. The mapper owns a private {@link ObjectMapper}
 * so it does not depend on the application's shared, possibly differently-configured
 * instance.
 * <p>
 * Every field on {@link Megastructure} round-trips through this mapper; the comprehensive
 * test coverage in {@code MegastructureDesignMapperTest} is the regression guard against
 * round-trip-loss.
 */
@Slf4j
@Component
public class MegastructureDesignMapper {

    private static final TypeReference<Set<StationFunction>> STATION_FUNCTION_SET = new TypeReference<>() {
    };
    private static final TypeReference<List<Armament>> ARMAMENT_LIST = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Converts a domain design into a persistable entity, preserving identity and timestamps.
     *
     * @param design the domain design
     * @return a new entity ready to be saved
     */
    public MegastructureEntity toEntity(Megastructure design) {
        MegastructureEntity entity = new MegastructureEntity();
        entity.setId(design.id());
        entity.setName(design.name());
        entity.setDesignation(design.designation());
        entity.setDescription(design.description());
        entity.setCategory(design.category());
        entity.setNotes(design.notes());

        entity.setArchetype(design.archetype());
        entity.setDimensionsKm(design.dimensionsKm());
        entity.setDryMassMegatons(design.dryMassMegatons());
        entity.setInternalVolumeKm3(design.internalVolumeKm3());

        entity.setMobility(design.mobility());
        entity.setAuxiliaryDrive(design.auxiliaryDrive());

        entity.setOriginType(design.originType());
        entity.setBuilderPolity(design.builderPolity());
        entity.setDiscoveryYear(design.discoveryYear());
        entity.setConstructionYear(design.constructionYear());

        entity.setPrimaryFunction(design.primaryFunction());
        entity.setSecondaryFunctionsJson(writeSecondaryFunctions(design.secondaryFunctions()));

        entity.setHasInteriorSetting(design.hasInteriorSetting());
        entity.setInteriorPopulation(design.interiorPopulation());
        entity.setInteriorGravity(design.interiorGravity());

        entity.setOperationalState(design.operationalState());
        entity.setConcealed(design.concealed());

        entity.setArmamentsJson(writeArmaments(design.armaments()));

        CatalogProvenance provenance = design.provenance();
        entity.setProvenanceSourceType(provenance.sourceType());
        entity.setProvenanceSourceUniverse(provenance.sourceUniverse());
        entity.setProvenanceSourceWork(provenance.sourceWork());
        entity.setProvenanceStatus(provenance.status());

        entity.setFaction(design.faction());
        entity.setAllegiance(design.allegiance());
        // techLevel: domain record permits null; coerce here for the NOT NULL column.
        entity.setTechLevel(design.techLevel() == null ? TechLevel.UNKNOWN : design.techLevel());

        entity.setCreatedAt(design.createdAt());
        entity.setModifiedAt(design.modifiedAt());
        // v2 Phase F.1 §4.4 — universe scope.
        entity.setUniverseId(design.universeId());
        return entity;
    }

    /**
     * Converts a persisted entity back into an immutable domain design.
     *
     * @param entity the persisted entity
     * @return the reconstructed domain design
     */
    public Megastructure toDomain(MegastructureEntity entity) {
        MegastructureArchetype archetype = entity.getArchetype() == null
                ? MegastructureArchetype.UNKNOWN
                : entity.getArchetype();
        Mobility mobility = entity.getMobility() == null ? Mobility.STATIONKEEPING : entity.getMobility();
        MegastructureOriginType originType = entity.getOriginType() == null
                ? MegastructureOriginType.UNKNOWN
                : entity.getOriginType();
        StationFunction primaryFunction = entity.getPrimaryFunction() == null
                ? StationFunction.UNKNOWN
                : entity.getPrimaryFunction();
        Set<StationFunction> secondaryFunctions = readSecondaryFunctions(entity.getSecondaryFunctionsJson());
        InteriorGravityType interiorGravity = entity.getInteriorGravity() == null
                ? InteriorGravityType.UNKNOWN
                : entity.getInteriorGravity();
        OperationalState operationalState = entity.getOperationalState() == null
                ? OperationalState.OPERATIONAL
                : entity.getOperationalState();
        TechLevel techLevel = entity.getTechLevel() == null ? TechLevel.UNKNOWN : entity.getTechLevel();
        CatalogProvenance provenance = new CatalogProvenance(
                entity.getProvenanceSourceType() == null ? SourceType.UNKNOWN : entity.getProvenanceSourceType(),
                entity.getProvenanceSourceUniverse() == null ? "" : entity.getProvenanceSourceUniverse(),
                entity.getProvenanceSourceWork(),
                entity.getProvenanceStatus() == null ? CatalogOperationalStatus.UNKNOWN : entity.getProvenanceStatus());
        return new Megastructure(
                entity.getId(),
                entity.getName(),
                entity.getDesignation(),
                entity.getDescription(),
                entity.getCategory(),
                entity.getNotes(),
                archetype,
                entity.getDimensionsKm(),
                entity.getDryMassMegatons(),
                entity.getInternalVolumeKm3(),
                mobility,
                entity.getAuxiliaryDrive(),
                originType,
                entity.getBuilderPolity(),
                entity.getDiscoveryYear(),
                entity.getConstructionYear(),
                primaryFunction,
                secondaryFunctions,
                entity.isHasInteriorSetting(),
                entity.getInteriorPopulation(),
                interiorGravity,
                operationalState,
                entity.isConcealed(),
                readArmaments(entity.getArmamentsJson()),
                provenance,
                entity.getFaction(),
                entity.getAllegiance(),
                techLevel,
                entity.getCreatedAt() != null ? entity.getCreatedAt() : Instant.now(),
                entity.getModifiedAt() != null ? entity.getModifiedAt() : entity.getCreatedAt(),
                entity.getUniverseId());
    }

    private String writeSecondaryFunctions(Set<StationFunction> functions) {
        if (functions == null || functions.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(functions);
        } catch (JacksonException e) {
            log.error("Failed to serialise megastructure secondary functions; storing none: {}", e.getMessage());
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
            log.error("Failed to deserialise megastructure secondary functions; returning none: {}", e.getMessage());
            return Set.of();
        }
    }

    private String writeArmaments(List<Armament> armaments) {
        if (armaments == null || armaments.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(armaments);
        } catch (JacksonException e) {
            log.error("Failed to serialise megastructure armaments; storing none: {}", e.getMessage());
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
            log.error("Failed to deserialise megastructure armaments; returning none: {}", e.getMessage());
            return List.of();
        }
    }
}
