package com.teamgannon.trips.spaceshipmodeller.service;

import com.teamgannon.trips.spaceshipmodeller.persistence.UniverseEntity;
import com.teamgannon.trips.spaceshipmodeller.persistence.UniverseMapper;
import com.teamgannon.trips.spaceshipmodeller.persistence.UniverseRepository;
import com.terranrepublic.assets.Catalog;
import com.terranrepublic.assets.Cataloged;
import com.terranrepublic.assets.Universe;
import com.terranrepublic.assets.UniverseLifecycle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Application service for managing the catalog of {@link Universe} entities.
 *
 * <p>v2 Phase F.1 §4.2 — first service in the F-series Worldbuilding pipeline. Mirrors
 * {@link GateNetworkDesignerService}'s find/save/delete/sync shape; F.1 Step 5 extends with
 * {@code activate} / {@code deactivate} + event publication, and F.1 Step 6 adds
 * {@code UniverseFilteringService} that consumes this service's reads.
 *
 * <p>The {@link #syncCatalogEntries} contract is the D.8 per-entry-existsById sync, kept here
 * for catalog-pipeline symmetry. It is vacuous in F.1 — the 15 universes ship via the V15
 * Flyway migration's INSERT statements, not via {@code Catalog.all()} constants. Future F.x
 * phases may add canonical Universe constants to Catalog (e.g. a built-in "Sandbox" universe)
 * and this sync activates without further code change.
 */
@Slf4j
@Service
public class UniverseDesignerService {

    private final UniverseRepository repository;
    private final UniverseMapper mapper;

    public UniverseDesignerService(UniverseRepository repository, UniverseMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    // ---------------------------------------------------------------- reads

    /** @return every saved universe, as domain objects */
    public List<Universe> findAll() {
        return repository.findAll().stream().map(mapper::toDomain).toList();
    }

    /** @return universes whose {@code active} flag is currently {@code true} */
    public List<Universe> findAllActive() {
        return repository.findByActive(true).stream().map(mapper::toDomain).toList();
    }

    /** @return the number of universes in the catalog */
    public long count() {
        return repository.count();
    }

    /**
     * @param id universe id
     * @return the universe, if present
     */
    public Optional<Universe> findById(String id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    /**
     * @param name universe name (case-insensitive)
     * @return the universe, if present
     */
    public Optional<Universe> findByName(String name) {
        return repository.findByNameIgnoreCase(name).map(mapper::toDomain);
    }

    /**
     * @param name universe name (case-insensitive)
     * @return {@code true} if a universe with this name already exists
     */
    public boolean existsByName(String name) {
        return repository.existsByNameIgnoreCase(name);
    }

    /**
     * @param lifecycle lifecycle state to filter by
     * @return universes in the given lifecycle
     */
    public List<Universe> findByLifecycle(UniverseLifecycle lifecycle) {
        return repository.findByLifecycle(lifecycle).stream().map(mapper::toDomain).toList();
    }

    // --------------------------------------------------------------- writes

    /**
     * Creates or updates a universe (upsert by id).
     *
     * @param universe the universe to persist
     * @return the persisted universe, round-tripped through the database
     */
    @Transactional
    public Universe save(Universe universe) {
        UniverseEntity saved = repository.save(mapper.toEntity(universe));
        log.info("Saved universe '{}' (lifecycle={}, active={})",
                saved.getName(), saved.getLifecycle(), saved.isActive());
        return mapper.toDomain(saved);
    }

    /**
     * Deletes a universe by id. No-op if the id is unknown. The V15 schema's
     * {@code ON DELETE SET NULL} on each catalog table's {@code universe_id} FK ensures
     * universe-scoped entries become orphaned (treated as canonical) rather than cascade-deleted;
     * this preserves user data even when the universe row is removed.
     *
     * @param id universe id
     */
    @Transactional
    public void deleteById(String id) {
        repository.findById(id).ifPresent(entity -> {
            repository.delete(entity);
            log.info("Deleted universe '{}'", entity.getName());
        });
    }

    /**
     * v2 Phase D.8 §3.1 sync-by-id seed, applied here for catalog-pipeline symmetry.
     *
     * <p>For each {@link Universe} in {@link Catalog#all()}, insert into JPA if and only if no
     * row with that id exists. Does NOT update existing rows (preserves user edits). Does NOT
     * delete orphan rows (preserves user-created universes).
     *
     * <p>Vacuous in F.1 — Catalog ships zero canonical Universe constants. The 15 universes
     * ship via the V15 Flyway migration's INSERT statements. Future F.x may add Catalog
     * constants for built-in universes (e.g. a "Sandbox" universe), at which point this sync
     * activates without further code change.
     *
     * @return the number of new universes inserted (zero today; zero on idempotent re-runs)
     */
    @Transactional
    public int syncCatalogEntries() {
        List<Universe> catalogUniverses = Catalog.all().stream()
                .filter(Universe.class::isInstance)
                .map(Universe.class::cast)
                .toList();
        int inserted = 0;
        for (Universe universe : catalogUniverses) {
            if (!repository.existsById(universe.id())) {
                repository.save(mapper.toEntity(universe));
                inserted++;
            }
        }
        if (inserted > 0) {
            log.info("Synced {} new universe(s) from Catalog into the UNIVERSE table", inserted);
        }
        return inserted;
    }

    // --------------------------------------------------- registry-facing

    /**
     * Visible for future cross-bucket registry surfaces: returns {@link #findAll()} typed as
     * {@link Cataloged}. Returns {@code List<Cataloged>} (not {@code List<SpaceAsset>}) because
     * {@code Universe} isn't a {@code SpaceAsset} — it sits outside the sealed hierarchies just
     * like {@link com.terranrepublic.assets.GateNetwork}.
     *
     * @return {@link #findAll()} typed as {@link Cataloged}
     */
    public List<Cataloged> findAllAsCataloged() {
        return findAll().stream().map(u -> (Cataloged) u).toList();
    }
}
