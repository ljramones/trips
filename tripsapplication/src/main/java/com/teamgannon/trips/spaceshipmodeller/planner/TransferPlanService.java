package com.teamgannon.trips.spaceshipmodeller.planner;

import com.teamgannon.trips.spaceshipmodeller.integration.TransferPlan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Application service for the saved transfer-plan library. Returns immutable {@link SavedTransferPlan}
 * domain objects; the JPA entity stays in the persistence layer. Conventions match the rest of the module:
 * constructor injection, untransacted reads, {@code @Transactional} writes.
 */
@Slf4j
@Service
public class TransferPlanService {

    private final TransferPlanRepository repository;
    private final TransferPlanMapper mapper;

    public TransferPlanService(TransferPlanRepository repository, TransferPlanMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    /** @return all saved plans, newest first */
    public List<SavedTransferPlan> findAll() {
        return repository.findAllByOrderByCreatedAtDesc().stream().map(mapper::toDomain).toList();
    }

    /**
     * @param id plan id
     * @return the plan, if present
     */
    public Optional<SavedTransferPlan> findById(String id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    /**
     * Persists a saved plan (create or update by id).
     *
     * @param plan the plan to persist
     * @return the persisted plan, round-tripped through the database
     */
    @Transactional
    public SavedTransferPlan save(SavedTransferPlan plan) {
        SavedTransferPlan saved = mapper.toDomain(repository.save(mapper.toEntity(plan)));
        log.info("Saved transfer plan '{}' for ship '{}' ({})",
                saved.route(), saved.shipName(), saved.status());
        return saved;
    }

    /**
     * Convenience: build a {@link SavedTransferPlan} from a freshly computed {@link TransferPlan} and
     * persist it.
     *
     * @param plan             the computed plan
     * @param shipId           the ship id (may be {@code null})
     * @param solarSystemId    the solar system id (may be {@code null})
     * @param centralMassSolar the central star mass used
     * @return the persisted plan
     */
    @Transactional
    public SavedTransferPlan saveComputed(TransferPlan plan, String shipId,
                                          String solarSystemId, double centralMassSolar) {
        return save(SavedTransferPlan.fromComputed(plan, shipId, solarSystemId, centralMassSolar));
    }

    /**
     * Deletes a plan by id.
     *
     * @param id plan id
     */
    @Transactional
    public void delete(String id) {
        repository.findById(id).ifPresent(entity -> {
            repository.delete(entity);
            log.info("Deleted transfer plan '{}'", entity.getId());
        });
    }
}
