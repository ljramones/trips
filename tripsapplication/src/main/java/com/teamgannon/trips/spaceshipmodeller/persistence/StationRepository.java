package com.teamgannon.trips.spaceshipmodeller.persistence;

import com.terranrepublic.assets.Mobility;
import com.terranrepublic.assets.StationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link StationEntity}.
 * <p>
 * Filtering by {@link StationType} and {@link Mobility} works directly against the
 * {@code STRING}-mapped enum columns; the indexes on those columns are declared on
 * {@link StationEntity} so plan-level filtering stays cheap.
 * <p>
 * Subtype-specific query methods beyond these are intentionally absent — the
 * Installations Designer panel in v2 Phase C will add what its UI conventions need.
 */
public interface StationRepository extends JpaRepository<StationEntity, String> {

    Optional<StationEntity> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    List<StationEntity> findByStationType(StationType stationType);

    List<StationEntity> findByMobility(Mobility mobility);

    List<StationEntity> findByFactionIgnoreCase(String faction);

    List<StationEntity> findByNameContainingIgnoreCaseOrderByNameAsc(String fragment);
}
