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
     * v2 Phase D.8 §3.1 — sync-by-id seed.
     * <p>
     * For each {@link WeaponInstallation} in {@link Catalog#all()}, insert into JPA if and only if
     * no row with that id exists. Does NOT update existing rows (preserves user edits). Does NOT
     * delete orphan rows (preserves user-created entries).
     * <p>
     * Same contract as {@link StationDesignerService#syncCatalogEntries()}.
     *
     * @return the number of new weapon installations inserted (zero on idempotent re-runs)
     */
    @Transactional
    public int syncCatalogEntries() {
        List<WeaponInstallation> catalogInstallations = Catalog.all().stream()
                .filter(WeaponInstallation.class::isInstance)
                .map(WeaponInstallation.class::cast)
                .toList();
        int inserted = 0;
        for (WeaponInstallation design : catalogInstallations) {
            if (!repository.existsById(design.id())) {
                repository.save(mapper.toEntity(design));
                inserted++;
            }
        }
        if (inserted > 0) {
            log.info("Synced {} new weapon installation(s) from Catalog into the WEAPON_INSTALLATION table",
                    inserted);
        }
        return inserted;
    }

    /** @return {@link #findAll()} typed as {@link SpaceAsset} for the construct registry */
    public List<SpaceAsset> findAllAsAssets() {
        return findAll().stream().map(d -> (SpaceAsset) d).toList();
    }
}
