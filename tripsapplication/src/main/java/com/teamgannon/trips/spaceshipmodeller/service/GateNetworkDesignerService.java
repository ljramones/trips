package com.teamgannon.trips.spaceshipmodeller.service;

import com.teamgannon.trips.spaceshipmodeller.persistence.GateNetworkEntity;
import com.teamgannon.trips.spaceshipmodeller.persistence.GateNetworkMapper;
import com.teamgannon.trips.spaceshipmodeller.persistence.GateNetworkRepository;
import com.terranrepublic.assets.Catalog;
import com.terranrepublic.assets.Cataloged;
import com.terranrepublic.assets.GateNetwork;
import com.terranrepublic.assets.GateNetworkLifecycle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Application service for managing the catalogue of {@link GateNetwork}s.
 *
 * <p>v2 Phase E.1 §5 — first catalog-pipeline service for an entity outside the
 * {@code SpaceAsset} / {@code SpaceInfrastructure} sealed hierarchies. Mirrors the shape of
 * the four sealed-hierarchy services (StationDesigner, WeaponInstallationDesigner,
 * TransportNode, MegastructureDesigner) but returns {@link Cataloged} (not {@code SpaceAsset})
 * from its registry-facing accessor since {@code GateNetwork} isn't a member of either sealed
 * hierarchy.
 *
 * <p>The {@code syncCatalogEntries} contract is the D.8 per-entry-existsById sync — vacuous
 * today (Catalog has no canonical GateNetwork constants), but the pipeline is in place for
 * E.2's catalog-data work to populate.
 */
@Slf4j
@Service
public class GateNetworkDesignerService {

    private final GateNetworkRepository repository;
    private final GateNetworkMapper mapper;

    public GateNetworkDesignerService(GateNetworkRepository repository, GateNetworkMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    // ---------------------------------------------------------------- reads

    /** @return every saved gate network, as domain objects */
    public List<GateNetwork> findAll() {
        return repository.findAll().stream().map(mapper::toDomain).toList();
    }

    /** @return the number of gate networks in the catalog */
    public long count() {
        return repository.count();
    }

    /**
     * @param id gate-network id
     * @return the network, if present
     */
    public Optional<GateNetwork> findById(String id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    /**
     * @param name gate-network name (case-insensitive)
     * @return the network, if present
     */
    public Optional<GateNetwork> findByName(String name) {
        return repository.findByNameIgnoreCase(name).map(mapper::toDomain);
    }

    /**
     * @param name gate-network name (case-insensitive)
     * @return {@code true} if a network with this name already exists
     */
    public boolean existsByName(String name) {
        return repository.existsByNameIgnoreCase(name);
    }

    /**
     * @param lifecycle lifecycle state to filter by
     * @return networks in the given lifecycle
     */
    public List<GateNetwork> findByLifecycle(GateNetworkLifecycle lifecycle) {
        return repository.findByLifecycle(lifecycle).stream().map(mapper::toDomain).toList();
    }

    // --------------------------------------------------------------- writes

    /**
     * Creates or updates a gate network (upsert by id).
     *
     * @param network the network to persist
     * @return the persisted network, round-tripped through the database
     */
    @Transactional
    public GateNetwork save(GateNetwork network) {
        GateNetworkEntity saved = repository.save(mapper.toEntity(network));
        log.info("Saved gate network '{}' (lifecycle={}, builderPolity={})",
                saved.getName(), saved.getLifecycle(), saved.getBuilderPolity());
        return mapper.toDomain(saved);
    }

    /**
     * Deletes a gate network by id. No-op if the id is unknown.
     *
     * @param id gate-network id
     */
    @Transactional
    public void deleteById(String id) {
        repository.findById(id).ifPresent(entity -> {
            repository.delete(entity);
            log.info("Deleted gate network '{}'", entity.getName());
        });
    }

    /**
     * v2 Phase D.8 §3.1 sync-by-id seed.
     *
     * <p>For each {@link GateNetwork} in {@link Catalog#all()}, insert into JPA if and only if no
     * row with that id exists. Does NOT update existing rows (preserves user edits). Does NOT
     * delete orphan rows (preserves user-created entries).
     *
     * <p>Vacuous today — Catalog ships zero canonical {@code GateNetwork} constants in Phase E.1.
     * Phase E.2 populates Aldenata + Posleen + other canonical networks.
     *
     * @return the number of new gate networks inserted (zero today; zero on idempotent re-runs)
     */
    @Transactional
    public int syncCatalogEntries() {
        List<GateNetwork> catalogNetworks = Catalog.all().stream()
                .filter(GateNetwork.class::isInstance)
                .map(GateNetwork.class::cast)
                .toList();
        int inserted = 0;
        for (GateNetwork network : catalogNetworks) {
            if (!repository.existsById(network.id())) {
                repository.save(mapper.toEntity(network));
                inserted++;
            }
        }
        if (inserted > 0) {
            log.info("Synced {} new gate network(s) from Catalog into the GATE_NETWORK table", inserted);
        }
        return inserted;
    }

    // --------------------------------------------------- registry-facing

    /**
     * Visible for future cross-bucket registry surfaces: returns {@link #findAll()} typed as
     * {@link Cataloged}. Returns {@code List<Cataloged>} (not {@code List<SpaceAsset>}) because
     * {@code GateNetwork} isn't a {@code SpaceAsset} — it sits outside the sealed hierarchies.
     *
     * @return {@link #findAll()} typed as {@link Cataloged}
     */
    public List<Cataloged> findAllAsCataloged() {
        return findAll().stream().map(n -> (Cataloged) n).toList();
    }
}
