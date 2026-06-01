package com.teamgannon.trips.spaceshipmodeller.service;

import com.teamgannon.trips.spaceshipmodeller.persistence.AliasEntity;
import com.teamgannon.trips.spaceshipmodeller.persistence.AliasMapper;
import com.teamgannon.trips.spaceshipmodeller.persistence.AliasRepository;
import com.teamgannon.trips.worldbuilding.UniverseFilteringService;
import com.terranrepublic.assets.Alias;
import com.terranrepublic.assets.AliasTargetKind;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Application service for managing the catalog of {@link Alias} entries.
 *
 * <p>Phase F.2 §4.5 — third entity service in the F-series after Universe (F.1) and Gate
 * Network (E.1). Mirrors their shape (find/save/delete + bulk lookup) with one F.2-specific
 * addition: {@link #findActiveAliasesForTarget(AliasTargetKind, String)} consults
 * {@link UniverseFilteringService} internally to scope results by activation state. Callers
 * (renderer tooltip in Step 3; Fictional Info tab in Step 4) don't manage the active-set
 * state themselves.
 *
 * <p>Uniqueness enforcement: the V18 schema's {@code uk_alias_universe_target} unique
 * constraint guarantees one alias per (universe, target) pair, but the service pre-checks
 * via {@link AliasRepository#findByUniverseIdAndTargetKindAndTargetId} on {@link #save(Alias)}
 * to surface a friendlier {@link IllegalStateException} message instead of a low-level JPA
 * constraint violation. The DB constraint remains the source of truth; the pre-check is a UX
 * affordance.
 */
@Slf4j
@Service
public class AliasDesignerService {

    private final AliasRepository repository;
    private final AliasMapper mapper;
    private final UniverseFilteringService filteringService;

    public AliasDesignerService(AliasRepository repository,
                                AliasMapper mapper,
                                UniverseFilteringService filteringService) {
        this.repository = repository;
        this.mapper = mapper;
        this.filteringService = filteringService;
    }

    // ---------------------------------------------------------------- reads

    /** @return every saved alias, as domain objects */
    public List<Alias> findAll() {
        return repository.findAll().stream().map(mapper::toDomain).toList();
    }

    /** @return the number of aliases in the catalog */
    public long count() {
        return repository.count();
    }

    /** @return the alias with the given id, if present */
    public Optional<Alias> findById(String id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    /** @return all aliases belonging to the given universe (regardless of activation state) */
    public List<Alias> findByUniverseId(String universeId) {
        return repository.findByUniverseId(universeId).stream().map(mapper::toDomain).toList();
    }

    /** @return all aliases for the given target (regardless of universe activation state) */
    public List<Alias> findByTargetKindAndTargetId(AliasTargetKind kind, String targetId) {
        return repository.findByTargetKindAndTargetId(kind, targetId).stream()
                .map(mapper::toDomain).toList();
    }

    /**
     * Phase F.2 §4.5 — the §5-invariants-aware lookup. Returns aliases for the target whose
     * universe is currently active per {@link UniverseFilteringService#getActiveUniverseIds()}.
     * This is the call the renderer tooltip + Fictional Info tab make to populate display.
     *
     * <p>Implementation: pulls all aliases for the (kind, targetId) pair from the indexed
     * {@code idx_alias_target} V18 index, then filters in-memory by active universe ids.
     * Cheap — typically returns 0-3 rows per target before filtering. Alternative
     * implementations (in-DB filter via WHERE universe_id IN (...)) would be O(N) in active
     * universe count and don't move the needle at F.2 scale.
     *
     * @return aliases for this target whose universe is currently active; empty list if none
     */
    public List<Alias> findActiveAliasesForTarget(AliasTargetKind kind, String targetId) {
        Set<String> activeIds = filteringService.getActiveUniverseIds();
        if (activeIds.isEmpty()) {
            return List.of();
        }
        return repository.findByTargetKindAndTargetId(kind, targetId).stream()
                .filter(e -> activeIds.contains(e.getUniverseId()))
                .map(mapper::toDomain)
                .toList();
    }

    // --------------------------------------------------------------- writes

    /**
     * Creates or updates an alias. On create, pre-checks the (universe, target) pair to surface
     * a friendly {@link IllegalStateException} if a duplicate exists, rather than letting the
     * DB's unique constraint produce a low-level JPA error. Updates (when the id already
     * exists) bypass the uniqueness check.
     *
     * @throws IllegalStateException if creating an alias and (universeId, targetKind, targetId)
     *                               already has another alias
     */
    @Transactional
    public Alias save(Alias alias) {
        boolean isUpdate = repository.existsById(alias.id());
        if (!isUpdate) {
            Optional<AliasEntity> conflicting = repository.findByUniverseIdAndTargetKindAndTargetId(
                    alias.universeId(), alias.targetKind(), alias.targetId());
            if (conflicting.isPresent()) {
                throw new IllegalStateException(String.format(
                        "An alias already exists for universe '%s' + target %s/%s "
                                + "(existing alias: '%s', id=%s). Edit or delete the existing "
                                + "alias instead of creating a duplicate.",
                        alias.universeId(), alias.targetKind(), alias.targetId(),
                        conflicting.get().getAliasText(), conflicting.get().getId()));
            }
        }
        AliasEntity saved = repository.save(mapper.toEntity(alias));
        log.info("Saved alias '{}' (universe={}, target={}/{}, id={})",
                saved.getAliasText(), saved.getUniverseId(), saved.getTargetKind(),
                saved.getTargetId(), saved.getId());
        return mapper.toDomain(saved);
    }

    /** Deletes an alias by id. No-op if the id is unknown. */
    @Transactional
    public void deleteById(String id) {
        repository.findById(id).ifPresent(entity -> {
            repository.delete(entity);
            log.info("Deleted alias '{}' (id={})", entity.getAliasText(), entity.getId());
        });
    }
}
