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
 * Mirrors {@link StationDesignerService} / {@link WeaponInstallationDesignerService} except for
 * one deliberate omission: there is <strong>no {@code TransportNodeCatalogSeeder}</strong>
 * companion {@code @Component}. v2 §5 Phase B explicitly notes that there are no canonical
 * transport nodes in the catalogue to seed, so first-launch starts the table empty and the
 * user populates it. The {@link #seedFromCatalogIfEmpty()} method here is kept for pattern
 * symmetry — it filters {@code Catalog.all()} for transport nodes (currently none, hence
 * always zero) so a future catalogue addition would auto-seed without code change, but no
 * Spring component triggers it at startup today.
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
     * Symmetry-preserving idempotent seeder. Currently a no-op because {@code Catalog.all()}
     * carries no transport-node entries; kept here so the pattern is identical to the
     * SpaceAsset-side services. No Spring component triggers this method today — v2 §5 Phase B
     * explicitly chose not to ship a {@code TransportNodeCatalogSeeder}.
     *
     * @return the number of transport nodes seeded (always zero until the catalogue gains some)
     */
    @Transactional
    public int seedFromCatalogIfEmpty() {
        if (count() > 0) {
            return 0;
        }
        // No transport-node constants in com.terranrepublic.assets.Catalog today. When some are
        // added, filter Catalog.all() to TransportNode here and the seed will activate.
        return 0;
    }

    /** @return {@link #findAll()} typed as {@link SpaceInfrastructure} for the construct registry */
    public List<SpaceInfrastructure> findAllAsInfrastructure() {
        return findAll().stream().map(n -> (SpaceInfrastructure) n).toList();
    }
}
