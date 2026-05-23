package com.teamgannon.trips.spaceshipmodeller.persistence;

import com.teamgannon.trips.spaceshipmodeller.core.ShipClass;
import com.teamgannon.trips.spaceshipmodeller.propulsion.DriveType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link SpaceshipEntity}.
 * <p>
 * Filtering by {@link ShipClass} and {@link DriveType} works directly against the {@code STRING}-mapped
 * enum columns. Filtering by propulsion {@link com.teamgannon.trips.spaceshipmodeller.propulsion.Category}
 * is derived from the drive and handled in the service layer.
 */
public interface SpaceshipRepository extends JpaRepository<SpaceshipEntity, String> {

    Optional<SpaceshipEntity> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    List<SpaceshipEntity> findByShipClass(ShipClass shipClass);

    List<SpaceshipEntity> findByDriveType(DriveType driveType);

    List<SpaceshipEntity> findByNameContainingIgnoreCaseOrderByNameAsc(String fragment);
}
