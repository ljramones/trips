package com.teamgannon.trips.spaceshipmodeller.persistence;

import com.terranrepublic.assets.MegastructureArchetype;
import com.terranrepublic.assets.MegastructureOriginType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link MegastructureEntity}.
 * <p>
 * Mirrors the shape of {@link StationRepository}. Filtering by
 * {@link MegastructureArchetype} and {@link MegastructureOriginType} works directly against
 * the {@code STRING}-mapped enum columns; the indexes on those columns are declared on
 * {@link MegastructureEntity} so plan-level filtering stays cheap.
 */
public interface MegastructureRepository extends JpaRepository<MegastructureEntity, String> {

    Optional<MegastructureEntity> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    List<MegastructureEntity> findByArchetype(MegastructureArchetype archetype);

    List<MegastructureEntity> findByOriginType(MegastructureOriginType originType);

    List<MegastructureEntity> findByNameContainingIgnoreCaseOrderByNameAsc(String fragment);
}
