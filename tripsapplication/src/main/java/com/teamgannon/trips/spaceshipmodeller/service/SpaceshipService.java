package com.teamgannon.trips.spaceshipmodeller.service;

import com.teamgannon.trips.spaceshipmodeller.core.ShipClass;
import com.teamgannon.trips.spaceshipmodeller.core.SpaceshipDesign;
import com.teamgannon.trips.spaceshipmodeller.persistence.SpaceshipDesignMapper;
import com.teamgannon.trips.spaceshipmodeller.persistence.SpaceshipEntity;
import com.teamgannon.trips.spaceshipmodeller.persistence.SpaceshipRepository;
import com.teamgannon.trips.spaceshipmodeller.propulsion.Category;
import com.teamgannon.trips.spaceshipmodeller.propulsion.DriveType;
import com.teamgannon.trips.spaceshipmodeller.rules.ValidationEngine;
import com.teamgannon.trips.spaceshipmodeller.rules.ValidationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Application service for managing a library of spaceship designs.
 * <p>
 * It is the single Spring-managed entry point for CRUD over {@link SpaceshipDesign}s, returning and
 * accepting immutable domain objects (the JPA {@link SpaceshipEntity} never leaves this layer). It also
 * exposes validation via the {@link ValidationEngine}; persistence is permitted even for designs with
 * warnings or errors (drafts), so callers that want to block invalid saves should check
 * {@link #validate(SpaceshipDesign)} first.
 * <p>
 * Conventions match the rest of TRIPS: constructor injection, read methods untransacted, write methods
 * {@code @Transactional}.
 */
@Slf4j
@Service
public class SpaceshipService {

    private final SpaceshipRepository repository;
    private final SpaceshipDesignMapper mapper;
    private final ValidationEngine validationEngine = new ValidationEngine();

    public SpaceshipService(SpaceshipRepository repository, SpaceshipDesignMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    // ---------------------------------------------------------------- reads

    /** @return every saved design, as domain objects */
    public List<SpaceshipDesign> findAll() {
        return repository.findAll().stream().map(mapper::toDomain).toList();
    }

    /** @return the number of designs in the library */
    public long count() {
        return repository.count();
    }

    /**
     * @param id design id
     * @return the design, if present
     */
    public Optional<SpaceshipDesign> findById(String id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    /**
     * @param name design name (case-insensitive)
     * @return the design, if present
     */
    public Optional<SpaceshipDesign> findByName(String name) {
        return repository.findByNameIgnoreCase(name).map(mapper::toDomain);
    }

    /**
     * @param name design name (case-insensitive)
     * @return {@code true} if a design with this name already exists
     */
    public boolean existsByName(String name) {
        return repository.existsByNameIgnoreCase(name);
    }

    /**
     * @param shipClass class to filter by
     * @return designs of the given class
     */
    public List<SpaceshipDesign> findByShipClass(ShipClass shipClass) {
        return repository.findByShipClass(shipClass).stream().map(mapper::toDomain).toList();
    }

    /**
     * @param driveType drive to filter by
     * @return designs using the given drive
     */
    public List<SpaceshipDesign> findByDriveType(DriveType driveType) {
        return repository.findByDriveType(driveType).stream().map(mapper::toDomain).toList();
    }

    /**
     * Filters by propulsion category, which is derived from each design's drive.
     *
     * @param category category to filter by
     * @return designs whose drive belongs to the given category
     */
    public List<SpaceshipDesign> findByCategory(Category category) {
        return findAll().stream()
                .filter(d -> d.driveType().category() == category)
                .toList();
    }

    /**
     * Validates a design against the rules engine without persisting it.
     *
     * @param design the design to validate
     * @return the validation result
     */
    public ValidationResult validate(SpaceshipDesign design) {
        return validationEngine.validate(design);
    }

    // --------------------------------------------------------------- writes

    /**
     * Creates or updates a design (upsert by id). Validation findings are logged but do not block the
     * save, so partial/draft designs can be stored; check {@link #validate(SpaceshipDesign)} beforehand to
     * enforce validity.
     *
     * @param design the design to persist
     * @return the persisted design, round-tripped through the database
     */
    @Transactional
    public SpaceshipDesign save(SpaceshipDesign design) {
        ValidationResult result = validate(design);
        if (!result.isValid()) {
            log.warn("Saving spaceship '{}' with {} validation error(s)",
                    design.name(), result.errors().size());
        }
        SpaceshipEntity saved = repository.save(mapper.toEntity(design));
        log.info("Saved spaceship '{}' (class={}, drive={})",
                saved.getName(), saved.getShipClass(), saved.getDriveType());
        return mapper.toDomain(saved);
    }

    /**
     * Seeds template designs into the library, skipping any whose name already exists (case-insensitive).
     * Safe to call repeatedly; it never creates duplicates.
     *
     * @param templates the candidate designs to add
     * @return how many were actually added
     */
    @Transactional
    public int seedTemplates(List<SpaceshipDesign> templates) {
        int added = 0;
        for (SpaceshipDesign template : templates) {
            if (!repository.existsByNameIgnoreCase(template.name())) {
                repository.save(mapper.toEntity(template));
                added++;
            }
        }
        if (added > 0) {
            log.info("Seeded {} spaceship template(s) into the library", added);
        }
        return added;
    }

    /**
     * Deletes a design by id.
     *
     * @param id design id
     */
    @Transactional
    public void delete(String id) {
        repository.findById(id).ifPresent(entity -> {
            repository.delete(entity);
            log.info("Deleted spaceship '{}'", entity.getName());
        });
    }
}
