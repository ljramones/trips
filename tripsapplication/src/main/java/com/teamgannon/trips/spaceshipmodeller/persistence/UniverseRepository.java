package com.teamgannon.trips.spaceshipmodeller.persistence;

import com.terranrepublic.assets.UniverseLifecycle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link UniverseEntity}.
 *
 * <p>v2 Phase F.1 §4.2 — finders support the activation UI (findByActive for "what's currently
 * on"), the catalog audit invariants (findAll), and per-name lookup for editor flows.
 * Indexes on name / lifecycle / active are declared on {@link UniverseEntity} so the activation
 * filter stays cheap regardless of universe count.
 */
public interface UniverseRepository extends JpaRepository<UniverseEntity, String> {

    Optional<UniverseEntity> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    List<UniverseEntity> findByLifecycle(UniverseLifecycle lifecycle);

    List<UniverseEntity> findByActive(boolean active);
}
