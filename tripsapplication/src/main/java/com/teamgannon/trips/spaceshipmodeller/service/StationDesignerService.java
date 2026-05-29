package com.teamgannon.trips.spaceshipmodeller.service;

import com.teamgannon.trips.spaceshipmodeller.persistence.StationDesignMapper;
import com.teamgannon.trips.spaceshipmodeller.persistence.StationEntity;
import com.teamgannon.trips.spaceshipmodeller.persistence.StationRepository;
import com.terranrepublic.assets.Catalog;
import com.terranrepublic.assets.Mobility;
import com.terranrepublic.assets.SpaceAsset;
import com.terranrepublic.assets.StationDesign;
import com.terranrepublic.assets.StationType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Application service for managing the catalogue of {@link StationDesign}s.
 * <p>
 * Single Spring-managed entry point for CRUD over {@link StationDesign}s, returning and accepting
 * immutable domain objects — the JPA {@link StationEntity} never leaves this layer. This is the
 * Phase 7.7 (Issue 23) lesson from the side-trip review and is non-negotiable: entity escape
 * regresses that work.
 * <p>
 * Conventions match {@code SpaceshipService}: constructor injection, read methods untransacted,
 * write methods {@code @Transactional}, and a {@code syncCatalogEntries} insert-only sync
 * (v2 Phase D.8 §3.1) that runs on every {@code ApplicationReadyEvent} via
 * {@link StationCatalogSeeder}.
 */
@Slf4j
@Service
public class StationDesignerService {

    private final StationRepository repository;
    private final StationDesignMapper mapper;

    public StationDesignerService(StationRepository repository, StationDesignMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    // ---------------------------------------------------------------- reads

    /** @return every saved station, as domain objects */
    public List<StationDesign> findAll() {
        return repository.findAll().stream().map(mapper::toDomain).toList();
    }

    /** @return the number of stations in the library */
    public long count() {
        return repository.count();
    }

    /**
     * @param id station id
     * @return the station, if present
     */
    public Optional<StationDesign> findById(String id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    /**
     * @param name station name (case-insensitive)
     * @return the station, if present
     */
    public Optional<StationDesign> findByName(String name) {
        return repository.findByNameIgnoreCase(name).map(mapper::toDomain);
    }

    /**
     * @param name station name (case-insensitive)
     * @return {@code true} if a station with this name already exists
     */
    public boolean existsByName(String name) {
        return repository.existsByNameIgnoreCase(name);
    }

    /**
     * @param stationType type to filter by
     * @return stations of the given type
     */
    public List<StationDesign> findByStationType(StationType stationType) {
        return repository.findByStationType(stationType).stream().map(mapper::toDomain).toList();
    }

    /**
     * @param mobility mobility class to filter by
     * @return stations with the given mobility
     */
    public List<StationDesign> findByMobility(Mobility mobility) {
        return repository.findByMobility(mobility).stream().map(mapper::toDomain).toList();
    }

    // --------------------------------------------------------------- writes

    /**
     * Creates or updates a station (upsert by id).
     *
     * @param design the station to persist
     * @return the persisted station, round-tripped through the database
     */
    @Transactional
    public StationDesign save(StationDesign design) {
        StationEntity saved = repository.save(mapper.toEntity(design));
        log.info("Saved station '{}' (type={}, mobility={})",
                saved.getName(), saved.getStationType(), saved.getMobility());
        return mapper.toDomain(saved);
    }

    /**
     * Deletes a station by id. No-op if the id is unknown.
     *
     * @param id station id
     */
    @Transactional
    public void deleteById(String id) {
        repository.findById(id).ifPresent(entity -> {
            repository.delete(entity);
            log.info("Deleted station '{}'", entity.getName());
        });
    }

    /**
     * v2 Phase D.8 §3.1 — sync-by-id seed.
     * <p>
     * For each {@link StationDesign} in {@link Catalog#all()}, insert into JPA if and only if no
     * row with that id exists. Does NOT update existing rows (preserves user edits). Does NOT
     * delete orphan rows (preserves user-created entries).
     * <p>
     * Replaces the pre-D.8 {@code seedFromCatalogIfEmpty} contract, which short-circuited on
     * {@code count() > 0} and silently swallowed every Catalog change after first launch — the
     * regression that made D.5's 8 real stations invisible to existing users. The per-entry
     * {@code existsById} check makes sync idempotent at the row level instead of at the
     * table level.
     *
     * @return the number of new stations inserted (zero on idempotent re-runs)
     */
    @Transactional
    public int syncCatalogEntries() {
        List<StationDesign> catalogStations = Catalog.all().stream()
                .filter(StationDesign.class::isInstance)
                .map(StationDesign.class::cast)
                .toList();
        int inserted = 0;
        for (StationDesign design : catalogStations) {
            if (!repository.existsById(design.id())) {
                repository.save(mapper.toEntity(design));
                inserted++;
            }
        }
        if (inserted > 0) {
            log.info("Synced {} new station(s) from Catalog into the STATION_DESIGN table", inserted);
        }
        return inserted;
    }

    // --------------------------------------------------- internal helpers

    /**
     * Visible for the registry: the constructs-feature {@code DefaultConstructRegistry} reads
     * stations through {@link #findAll()} and exposes them as {@link SpaceAsset}s. This method is
     * here for symmetry with {@code SpaceshipService}; production code calls {@link #findAll()}.
     *
     * @return {@link #findAll()} typed as {@link SpaceAsset}
     */
    public List<SpaceAsset> findAllAsAssets() {
        return findAll().stream().map(d -> (SpaceAsset) d).toList();
    }
}
