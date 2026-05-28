package com.teamgannon.trips.spaceshipmodeller.persistence;

import com.terranrepublic.assets.Emplacement;
import com.terranrepublic.assets.InstallationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link WeaponInstallationEntity}.
 * <p>
 * Filter methods are aligned with the {@code WEAPON_INSTALLATION} table's indexes (declared on
 * {@link WeaponInstallationEntity}) so per-type and per-emplacement queries stay cheap.
 */
public interface WeaponInstallationRepository extends JpaRepository<WeaponInstallationEntity, String> {

    Optional<WeaponInstallationEntity> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    List<WeaponInstallationEntity> findByInstallationType(InstallationType installationType);

    List<WeaponInstallationEntity> findByEmplacement(Emplacement emplacement);

    List<WeaponInstallationEntity> findByFactionIgnoreCase(String faction);

    List<WeaponInstallationEntity> findByNameContainingIgnoreCaseOrderByNameAsc(String fragment);
}
