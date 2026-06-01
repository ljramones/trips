package com.teamgannon.trips.spaceshipmodeller.persistence;

import com.terranrepublic.assets.Universe;
import com.terranrepublic.assets.UniverseLifecycle;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Converts between the immutable domain {@link Universe} record and the mutable JPA
 * {@link UniverseEntity}.
 *
 * <p>v2 Phase F.1 §4.2 — simpler than {@link GateNetworkMapper} because Universe carries no
 * provenance object (Universe IS the provenance scope; see Universe javadoc). Direct
 * field-by-field mapping with no nested struct unpacking.
 */
@Component
public class UniverseMapper {

    /**
     * Converts a domain universe into a persistable entity.
     *
     * @param universe the domain universe
     * @return a new entity ready to be saved
     */
    public UniverseEntity toEntity(Universe universe) {
        UniverseEntity entity = new UniverseEntity();
        entity.setId(universe.id());
        entity.setName(universe.name());
        entity.setDescription(universe.description());
        entity.setSourceAuthor(universe.sourceAuthor());
        entity.setVersion(universe.version());
        entity.setLifecycle(universe.lifecycle());
        entity.setActive(universe.active());
        Instant now = Instant.now();
        entity.setCreatedAt(now);
        entity.setModifiedAt(now);
        return entity;
    }

    /**
     * Converts a persisted entity back into an immutable domain universe.
     *
     * @param entity the persisted entity
     * @return the reconstructed domain universe
     */
    public Universe toDomain(UniverseEntity entity) {
        UniverseLifecycle lifecycle = entity.getLifecycle() == null
                ? UniverseLifecycle.AVAILABLE
                : entity.getLifecycle();
        return new Universe(
                entity.getId(),
                entity.getName(),
                entity.getDescription() == null ? "" : entity.getDescription(),
                entity.getSourceAuthor() == null ? "" : entity.getSourceAuthor(),
                entity.getVersion() == null || entity.getVersion().isBlank() ? "1.0" : entity.getVersion(),
                lifecycle,
                entity.isActive()
        );
    }
}
