package com.teamgannon.trips.spaceshipmodeller.persistence;

import com.terranrepublic.assets.CatalogOperationalStatus;
import com.terranrepublic.assets.CatalogProvenance;
import com.terranrepublic.assets.GateNetwork;
import com.terranrepublic.assets.GateNetworkLifecycle;
import com.terranrepublic.assets.SourceType;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Converts between the immutable domain {@link GateNetwork} record and the mutable JPA
 * {@link GateNetworkEntity}.
 *
 * <p>v2 Phase E.1 §5 — simpler than {@link MegastructureDesignMapper} because GateNetwork has
 * no collection fields (no JSON LOB serialization needed). Field-by-field mapping, with
 * provenance decomposed into four columns on persist and recomposed on load (the D.6 pattern).
 */
@Component
public class GateNetworkMapper {

    /**
     * Converts a domain network into a persistable entity, preserving identity and timestamps.
     *
     * @param network the domain network
     * @return a new entity ready to be saved
     */
    public GateNetworkEntity toEntity(GateNetwork network) {
        GateNetworkEntity entity = new GateNetworkEntity();
        entity.setId(network.id());
        entity.setName(network.name());
        entity.setBuilderPolity(network.builderPolity());
        entity.setLifecycle(network.lifecycle());
        entity.setTransponderName(network.transponderName());
        entity.setDescription(network.description());
        entity.setNotes(network.notes());
        entity.setCategory(network.category());

        CatalogProvenance provenance = network.provenance();
        entity.setProvenanceSourceType(provenance.sourceType());
        entity.setProvenanceSourceUniverse(provenance.sourceUniverse());
        entity.setProvenanceSourceWork(provenance.sourceWork());
        entity.setProvenanceStatus(provenance.status());

        entity.setCreatedAt(network.createdAt());
        entity.setModifiedAt(network.modifiedAt());
        // v2 Phase F.1 §4.4 — universe scope.
        entity.setUniverseId(network.universeId());
        return entity;
    }

    /**
     * Converts a persisted entity back into an immutable domain network.
     *
     * @param entity the persisted entity
     * @return the reconstructed domain network
     */
    public GateNetwork toDomain(GateNetworkEntity entity) {
        GateNetworkLifecycle lifecycle = entity.getLifecycle() == null
                ? GateNetworkLifecycle.ACTIVE
                : entity.getLifecycle();
        CatalogProvenance provenance = new CatalogProvenance(
                entity.getProvenanceSourceType() == null ? SourceType.UNKNOWN : entity.getProvenanceSourceType(),
                entity.getProvenanceSourceUniverse() == null ? "" : entity.getProvenanceSourceUniverse(),
                entity.getProvenanceSourceWork(),
                entity.getProvenanceStatus() == null ? CatalogOperationalStatus.UNKNOWN : entity.getProvenanceStatus());
        return new GateNetwork(
                entity.getId(),
                entity.getName(),
                entity.getBuilderPolity(),
                lifecycle,
                entity.getTransponderName(),
                entity.getDescription(),
                entity.getNotes(),
                entity.getCategory(),
                provenance,
                entity.getCreatedAt() != null ? entity.getCreatedAt() : Instant.now(),
                entity.getModifiedAt() != null ? entity.getModifiedAt() : entity.getCreatedAt(),
                entity.getUniverseId());
    }
}
