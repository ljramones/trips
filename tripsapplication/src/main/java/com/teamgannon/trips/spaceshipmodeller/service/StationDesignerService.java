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
 * write methods {@code @Transactional}, and a {@code seedFromCatalogIfEmpty} idempotent seed that
 * mirrors the Spaceship Designer panel's {@code seedTemplatesIfEmpty} pattern.
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
     * Seeds the station table from {@link Catalog#all()}, filtered to {@link StationDesign}
     * instances, if and only if the table is empty.
     * <p>
     * Pinned by v2 §6 decision Q4: {@code Catalog} remains the canonical in-memory seed; this
     * method is the side that mirrors it into JPA. Mirrors the {@code seedTemplatesIfEmpty}
     * pattern from {@code SpaceshipDesignerPanel}: idempotent — re-runs after the first seed are
     * no-ops, never re-inserts or overwrites custom rows.
     *
     * @return the number of stations seeded (zero if the table was already populated)
     */
    @Transactional
    public int seedFromCatalogIfEmpty() {
        if (count() > 0) {
            return 0;
        }
        List<StationDesign> stations = Catalog.all().stream()
                .filter(StationDesign.class::isInstance)
                .map(StationDesign.class::cast)
                .toList();
        for (StationDesign design : stations) {
            repository.save(mapper.toEntity(design));
        }
        log.info("Seeded {} station(s) from Catalog into an empty STATION_DESIGN table", stations.size());
        return stations.size();
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
