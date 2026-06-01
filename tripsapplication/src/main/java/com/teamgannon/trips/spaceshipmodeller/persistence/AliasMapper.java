package com.teamgannon.trips.spaceshipmodeller.persistence;

import com.terranrepublic.assets.Alias;
import com.terranrepublic.assets.AliasTargetKind;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Converts between the immutable domain {@link Alias} record and the mutable JPA
 * {@link AliasEntity}.
 *
 * <p>Phase F.2 §4.5 — simpler than GateNetworkMapper because Alias has no provenance object
 * (aliases are universe-intrinsic; no upstream attribution to decompose). Field-by-field
 * mapping with no JSON LOB serialization.
 */
@Component
public class AliasMapper {

    /**
     * Converts a domain alias into a persistable entity.
     *
     * @param alias the domain alias
     * @return a new entity ready to be saved
     */
    public AliasEntity toEntity(Alias alias) {
        AliasEntity entity = new AliasEntity();
        entity.setId(alias.id());
        entity.setUniverseId(alias.universeId());
        entity.setTargetKind(alias.targetKind());
        entity.setTargetId(alias.targetId());
        entity.setAliasText(alias.aliasText());
        entity.setDescription(alias.description());
        entity.setCreatedAt(alias.createdAt());
        entity.setModifiedAt(alias.modifiedAt());
        return entity;
    }

    /**
     * Converts a persisted entity back into an immutable domain alias.
     *
     * @param entity the persisted entity
     * @return the reconstructed domain alias
     */
    public Alias toDomain(AliasEntity entity) {
        AliasTargetKind kind = entity.getTargetKind() == null
                ? AliasTargetKind.STAR
                : entity.getTargetKind();
        return new Alias(
                entity.getId(),
                entity.getUniverseId(),
                kind,
                entity.getTargetId(),
                entity.getAliasText(),
                entity.getDescription() == null ? "" : entity.getDescription(),
                entity.getCreatedAt() != null ? entity.getCreatedAt() : Instant.now(),
                entity.getModifiedAt() != null ? entity.getModifiedAt() : entity.getCreatedAt()
        );
    }
}
