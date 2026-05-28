package com.teamgannon.trips.spaceshipmodeller.service;

import com.teamgannon.trips.spaceshipmodeller.persistence.WeaponInstallationEntity;
import com.teamgannon.trips.spaceshipmodeller.persistence.WeaponInstallationMapper;
import com.teamgannon.trips.spaceshipmodeller.persistence.WeaponInstallationRepository;
import com.terranrepublic.assets.Catalog;
import com.terranrepublic.assets.Emplacement;
import com.terranrepublic.assets.InstallationType;
import com.terranrepublic.assets.SpaceAsset;
import com.terranrepublic.assets.WeaponInstallation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Application service for managing the catalogue of {@link WeaponInstallation}s.
 * <p>
 * Mirrors {@link StationDesignerService}: returns and accepts immutable domain objects, the JPA
 * {@link WeaponInstallationEntity} never leaves this layer. Constructor-injected, read methods
 * untransacted, write methods {@code @Transactional}.
 */
@Slf4j
@Service
public class WeaponInstallationDesignerService {

    private final WeaponInstallationRepository repository;
    private final WeaponInstallationMapper mapper;

    public WeaponInstallationDesignerService(WeaponInstallationRepository repository,
                                             WeaponInstallationMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    // ---------------------------------------------------------------- reads

    public List<WeaponInstallation> findAll() {
        return repository.findAll().stream().map(mapper::toDomain).toList();
    }

    public long count() {
        return repository.count();
    }

    public Optional<WeaponInstallation> findById(String id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    public Optional<WeaponInstallation> findByName(String name) {
        return repository.findByNameIgnoreCase(name).map(mapper::toDomain);
    }

    public boolean existsByName(String name) {
        return repository.existsByNameIgnoreCase(name);
    }

    public List<WeaponInstallation> findByInstallationType(InstallationType type) {
        return repository.findByInstallationType(type).stream().map(mapper::toDomain).toList();
    }

    public List<WeaponInstallation> findByEmplacement(Emplacement emplacement) {
        return repository.findByEmplacement(emplacement).stream().map(mapper::toDomain).toList();
    }

    // --------------------------------------------------------------- writes

    @Transactional
    public WeaponInstallation save(WeaponInstallation design) {
        WeaponInstallationEntity saved = repository.save(mapper.toEntity(design));
        log.info("Saved weapon installation '{}' (type={}, emplacement={})",
                saved.getName(), saved.getInstallationType(), saved.getEmplacement());
        return mapper.toDomain(saved);
    }

    @Transactional
    public void deleteById(String id) {
        repository.findById(id).ifPresent(entity -> {
            repository.delete(entity);
            log.info("Deleted weapon installation '{}'", entity.getName());
        });
    }

    /**
     * Seeds the weapon-installation table from {@link Catalog#all()}, filtered to
     * {@link WeaponInstallation} instances, if and only if the table is empty.
     * <p>
     * Same idempotency contract as {@link StationDesignerService#seedFromCatalogIfEmpty()}: a
     * non-empty table is left alone.
     *
     * @return the number of weapon installations seeded (zero if the table was already populated)
     */
    @Transactional
    public int seedFromCatalogIfEmpty() {
        if (count() > 0) {
            return 0;
        }
        List<WeaponInstallation> installations = Catalog.all().stream()
                .filter(WeaponInstallation.class::isInstance)
                .map(WeaponInstallation.class::cast)
                .toList();
        for (WeaponInstallation design : installations) {
            repository.save(mapper.toEntity(design));
        }
        log.info("Seeded {} weapon installation(s) from Catalog into an empty WEAPON_INSTALLATION table",
                installations.size());
        return installations.size();
    }

    /** @return {@link #findAll()} typed as {@link SpaceAsset} for the construct registry */
    public List<SpaceAsset> findAllAsAssets() {
        return findAll().stream().map(d -> (SpaceAsset) d).toList();
    }
}
