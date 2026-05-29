package com.teamgannon.trips.spaceshipmodeller.service;

import com.teamgannon.trips.spaceshipmodeller.persistence.TransportNodeEntity;
import com.teamgannon.trips.spaceshipmodeller.persistence.TransportNodeMapper;
import com.teamgannon.trips.spaceshipmodeller.persistence.TransportNodeRepository;
import com.terranrepublic.infrastructure.NodeType;
import com.terranrepublic.infrastructure.SpaceInfrastructure;
import com.terranrepublic.infrastructure.TransportNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Application service for managing persisted {@link TransportNode}s.
 * <p>
 * Mirrors {@link StationDesignerService} / {@link WeaponInstallationDesignerService}. v2 Phase D.8
 * Step 5 adds {@link TransportNodeCatalogSeeder} so all four {@code *DesignerService} classes
 * (Station / WeaponInstallation / TransportNode / Megastructure) have parity at the seeder layer.
 * The {@link #syncCatalogEntries()} method filters {@code Catalog.all()} for transport nodes
 * (currently none, so the sync returns zero), so a future catalogue addition auto-seeds without
 * code change.
 * <p>
 * The class is named {@code TransportNodeService} (not {@code TransportNodeDesignerService})
 * because infrastructure entries are not "designs" in the same sense ships and stations are.
 * Prompt-pinned distinction.
 */
@Slf4j
@Service
public class TransportNodeService {

    private final TransportNodeRepository repository;
    private final TransportNodeMapper mapper;

    public TransportNodeService(TransportNodeRepository repository, TransportNodeMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    // ---------------------------------------------------------------- reads

    public List<TransportNode> findAll() {
        return repository.findAll().stream().map(mapper::toDomain).toList();
    }

    public long count() {
        return repository.count();
    }

    public Optional<TransportNode> findById(String id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    public Optional<TransportNode> findByName(String name) {
        return repository.findByNameIgnoreCase(name).map(mapper::toDomain);
    }

    public boolean existsByName(String name) {
        return repository.existsByNameIgnoreCase(name);
    }

    public List<TransportNode> findByType(NodeType type) {
        return repository.findByType(type).stream().map(mapper::toDomain).toList();
    }

    // --------------------------------------------------------------- writes

    @Transactional
    public TransportNode save(TransportNode node) {
        TransportNodeEntity saved = repository.save(mapper.toEntity(node));
        log.info("Saved transport node '{}' (type={})", saved.getName(), saved.getType());
        return mapper.toDomain(saved);
    }

    @Transactional
    public void deleteById(String id) {
        repository.findById(id).ifPresent(entity -> {
            repository.delete(entity);
            log.info("Deleted transport node '{}'", entity.getName());
        });
    }

    /**
     * v2 Phase D.8 §3.1 — sync-by-id seed.
     * <p>
     * For each {@link TransportNode} in {@link com.terranrepublic.assets.Catalog#all()}, insert
     * into JPA if and only if no row with that id exists. Does NOT update existing rows
     * (preserves user edits). Does NOT delete orphan rows (preserves user-created entries).
     * <p>
     * {@code Catalog.all()} carries no canonical transport-node entries today, so the loop is
     * vacuous and the method returns zero. When a future Catalog adds transport-node constants,
     * sync activates without further code change. Same contract as
     * {@link StationDesignerService#syncCatalogEntries()}.
     *
     * @return the number of new transport nodes inserted (zero today; zero on idempotent re-runs)
     */
    @Transactional
    public int syncCatalogEntries() {
        List<TransportNode> catalogNodes = com.terranrepublic.assets.Catalog.all().stream()
                .filter(TransportNode.class::isInstance)
                .map(TransportNode.class::cast)
                .toList();
        int inserted = 0;
        for (TransportNode design : catalogNodes) {
            if (!repository.existsById(design.id())) {
                repository.save(mapper.toEntity(design));
                inserted++;
            }
        }
        if (inserted > 0) {
            log.info("Synced {} new transport node(s) from Catalog into the TRANSPORT_NODE table", inserted);
        }
        return inserted;
    }

    /** @return {@link #findAll()} typed as {@link SpaceInfrastructure} for the construct registry */
    public List<SpaceInfrastructure> findAllAsInfrastructure() {
        return findAll().stream().map(n -> (SpaceInfrastructure) n).toList();
    }
}
