package com.teamgannon.trips.spaceshipmodeller.persistence;

import com.terranrepublic.assets.GateNetworkLifecycle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link GateNetworkEntity}.
 *
 * <p>v2 Phase E.1 §5 — query methods support the future browser surfaces (find by name for
 * editor dialogs, filter by lifecycle for "show only active networks", filter by builder polity
 * for faction-grouped browsing). The indexes on name / lifecycle / builderPolity /
 * provenanceSourceUniverse are declared on {@link GateNetworkEntity} so plan-level filtering
 * stays cheap.
 */
public interface GateNetworkRepository extends JpaRepository<GateNetworkEntity, String> {

    Optional<GateNetworkEntity> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    List<GateNetworkEntity> findByLifecycle(GateNetworkLifecycle lifecycle);

    List<GateNetworkEntity> findByBuilderPolityIgnoreCase(String builderPolity);
}
