package com.teamgannon.trips.spaceshipmodeller.service;

import com.teamgannon.trips.spaceshipmodeller.persistence.MegastructureDesignMapper;
import com.teamgannon.trips.spaceshipmodeller.persistence.MegastructureEntity;
import com.teamgannon.trips.spaceshipmodeller.persistence.MegastructureRepository;
import com.terranrepublic.assets.Catalog;
import com.terranrepublic.assets.Megastructure;
import com.terranrepublic.assets.MegastructureArchetype;
import com.terranrepublic.assets.MegastructureOriginType;
import com.terranrepublic.assets.SpaceAsset;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Application service for managing the catalogue of {@link Megastructure}s.
 * <p>
 * Mirrors the shape of {@link StationDesignerService}: constructor injection, read methods
 * untransacted, write methods {@code @Transactional}, and a {@code syncCatalogEntries}
 * insert-only sync that runs on every {@code ApplicationReadyEvent} via
 * {@link MegastructureCatalogSeeder}.
 * <p>
 * v2 Phase D.8 §3.1 — the sync-by-id contract: on every app launch, ensure every Catalog
 * Megastructure exists in JPA. Insert what's missing; leave existing rows untouched.
 * Idempotent, never overwrites user edits, never deletes orphan rows. See the design doc
 * at {@code docs/design/constructs-d8-catalog-sync-and-megastructure-wiring.md}.
 */
@Slf4j
@Service
public class MegastructureDesignerService {

    private final MegastructureRepository repository;
    private final MegastructureDesignMapper mapper;

    public MegastructureDesignerService(MegastructureRepository repository,
                                        MegastructureDesignMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    // ---------------------------------------------------------------- reads

    /** @return every saved megastructure, as domain objects */
    public List<Megastructure> findAll() {
        return repository.findAll().stream().map(mapper::toDomain).toList();
    }

    /** @return the number of megastructures in the library */
    public long count() {
        return repository.count();
    }

    /**
     * @param id megastructure id
     * @return the megastructure, if present
     */
    public Optional<Megastructure> findById(String id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    /**
     * @param name megastructure name (case-insensitive)
     * @return the megastructure, if present
     */
    public Optional<Megastructure> findByName(String name) {
        return repository.findByNameIgnoreCase(name).map(mapper::toDomain);
    }

    /**
     * @param name megastructure name (case-insensitive)
     * @return {@code true} if a megastructure with this name already exists
     */
    public boolean existsByName(String name) {
        return repository.existsByNameIgnoreCase(name);
    }

    /**
     * @param archetype archetype to filter by
     * @return megastructures of the given archetype
     */
    public List<Megastructure> findByArchetype(MegastructureArchetype archetype) {
        return repository.findByArchetype(archetype).stream().map(mapper::toDomain).toList();
    }

    /**
     * @param originType origin type to filter by
     * @return megastructures with the given origin type
     */
    public List<Megastructure> findByOriginType(MegastructureOriginType originType) {
        return repository.findByOriginType(originType).stream().map(mapper::toDomain).toList();
    }

    // --------------------------------------------------------------- writes

    /**
     * Creates or updates a megastructure (upsert by id).
     *
     * @param design the megastructure to persist
     * @return the persisted megastructure, round-tripped through the database
     */
    @Transactional
    public Megastructure save(Megastructure design) {
        MegastructureEntity saved = repository.save(mapper.toEntity(design));
        log.info("Saved megastructure '{}' (archetype={}, originType={})",
                saved.getName(), saved.getArchetype(), saved.getOriginType());
        return mapper.toDomain(saved);
    }

    /**
     * Deletes a megastructure by id. No-op if the id is unknown.
     *
     * @param id megastructure id
     */
    @Transactional
    public void deleteById(String id) {
        repository.findById(id).ifPresent(entity -> {
            repository.delete(entity);
            log.info("Deleted megastructure '{}'", entity.getName());
        });
    }

    /**
     * v2 Phase D.8 §3.1 — sync-by-id seed.
     * <p>
     * For each {@link Megastructure} in {@link Catalog#all()}, insert into JPA if and only if
     * no row with that id exists. Does NOT update existing rows (preserves user edits). Does
     * NOT delete orphan rows (preserves user-created entries).
     * <p>
     * Idempotent: a second call within the same JVM returns 0 (every catalog id is present
     * after the first call). Mirrors the pattern used by the other three {@code *DesignerService}
     * classes after D.8 Step 4's rename.
     *
     * @return the number of new megastructures inserted (zero on idempotent re-runs)
     */
    @Transactional
    public int syncCatalogEntries() {
        List<Megastructure> catalogMegastructures = Catalog.all().stream()
                .filter(Megastructure.class::isInstance)
                .map(Megastructure.class::cast)
                .toList();
        int inserted = 0;
        for (Megastructure design : catalogMegastructures) {
            if (!repository.existsById(design.id())) {
                repository.save(mapper.toEntity(design));
                inserted++;
            }
        }
        if (inserted > 0) {
            log.info("Synced {} new megastructure(s) from Catalog into the MEGASTRUCTURE table", inserted);
        }
        return inserted;
    }

    // --------------------------------------------------- internal helpers

    /**
     * Visible for the registry: the constructs-feature {@code DefaultConstructRegistry} reads
     * megastructures through {@link #findAll()} and exposes them as {@link SpaceAsset}s.
     *
     * @return {@link #findAll()} typed as {@link SpaceAsset}
     */
    public List<SpaceAsset> findAllAsAssets() {
        return findAll().stream().map(d -> (SpaceAsset) d).toList();
    }
}
