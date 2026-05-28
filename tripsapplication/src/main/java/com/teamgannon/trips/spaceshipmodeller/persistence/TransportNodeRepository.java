package com.teamgannon.trips.spaceshipmodeller.persistence;

import com.terranrepublic.infrastructure.NodeType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link TransportNodeEntity}.
 * <p>
 * Mirrors {@link StationRepository} / {@link WeaponInstallationRepository}: name lookups and
 * filters by the discriminator column ({@link NodeType}) and {@code faction}. No node-graph
 * traversal methods here; those belong to the in-memory {@code GraphRegistry} until v2's
 * Phase E ships route-finder integration.
 */
public interface TransportNodeRepository extends JpaRepository<TransportNodeEntity, String> {

    Optional<TransportNodeEntity> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    List<TransportNodeEntity> findByType(NodeType type);

    List<TransportNodeEntity> findByFactionIgnoreCase(String faction);

    List<TransportNodeEntity> findByNameContainingIgnoreCaseOrderByNameAsc(String fragment);
}
