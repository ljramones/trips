package com.teamgannon.trips.service;

import com.teamgannon.trips.jpa.model.Nebula;
import com.teamgannon.trips.jpa.model.NebulaType;
import com.teamgannon.trips.jpa.repository.NebulaRepository;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for nebula CRUD operations.
 * <p>
 * Provides methods to create, read, update, and delete nebulae
 * as well as query methods for finding nebulae by dataset or within plot range.
 */
@Slf4j
@Service
public class NebulaService {

    private final NebulaRepository nebulaRepository;

    public NebulaService(NebulaRepository nebulaRepository) {
        this.nebulaRepository = nebulaRepository;
    }

    /**
     * Save a new nebula or update an existing one.
     *
     * @param nebula the nebula to save
     * @return the saved nebula with generated ID (if new)
     */
    @Transactional
    public Nebula save(@NotNull Nebula nebula) {
        Nebula saved = nebulaRepository.save(nebula);
        log.info("Saved nebula '{}' (id={})", saved.getName(), saved.getId());
        return saved;
    }

    /**
     * Find a nebula by its ID.
     *
     * @param id the nebula ID
     * @return Optional containing the nebula, or empty if not found
     */
    public Optional<Nebula> findById(String id) {
        return nebulaRepository.findById(id);
    }

    /**
     * Find all nebulae in a dataset.
     *
     * @param datasetName the dataset name
     * @return list of nebulae in the dataset, ordered by name
     */
    public List<Nebula> findByDataset(String datasetName) {
        return nebulaRepository.findByDataSetNameOrderByNameAsc(datasetName);
    }

    /**
     * Find all nebulae in a dataset of a specific type.
     *
     * @param datasetName the dataset name
     * @param type        the nebula type
     * @return list of matching nebulae
     */
    public List<Nebula> findByDatasetAndType(String datasetName, NebulaType type) {
        return nebulaRepository.findByDataSetNameAndTypeOrderByNameAsc(datasetName, type);
    }

    /**
     * Find nebulae within plot range.
     *
     * @param datasetName the dataset name
     * @param centerX     plot center X in light-years
     * @param centerY     plot center Y in light-years
     * @param centerZ     plot center Z in light-years
     * @param plotRadius  plot radius in light-years
     * @return list of nebulae that intersect the plot sphere
     */
    public List<Nebula> findInPlotRange(String datasetName,
                                         double centerX, double centerY, double centerZ,
                                         double plotRadius) {
        return nebulaRepository.findInPlotRange(datasetName, centerX, centerY, centerZ, plotRadius);
    }

    /**
     * Delete a nebula by ID.
     *
     * @param id the nebula ID
     * @return true if deleted, false if not found
     */
    @Transactional
    public boolean delete(String id) {
        Optional<Nebula> nebula = nebulaRepository.findById(id);
        if (nebula.isPresent()) {
            nebulaRepository.delete(nebula.get());
            log.info("Deleted nebula '{}' (id={})", nebula.get().getName(), id);
            return true;
        }
        log.warn("Attempted to delete non-existent nebula with id={}", id);
        return false;
    }

    /**
     * Delete a nebula.
     *
     * @param nebula the nebula to delete
     */
    @Transactional
    public void delete(@NotNull Nebula nebula) {
        nebulaRepository.delete(nebula);
        log.info("Deleted nebula '{}' (id={})", nebula.getName(), nebula.getId());
    }

    /**
     * Delete all nebulae in a dataset.
     *
     * @param datasetName the dataset name
     * @return number of nebulae deleted
     */
    @Transactional
    public int deleteByDataset(String datasetName) {
        List<Nebula> nebulae = nebulaRepository.findByDataSetNameOrderByNameAsc(datasetName);
        int count = nebulae.size();
        nebulaRepository.deleteAll(nebulae);
        log.info("Deleted {} nebulae from dataset '{}'", count, datasetName);
        return count;
    }

    /**
     * Count nebulae in a dataset.
     *
     * @param datasetName the dataset name
     * @return count of nebulae
     */
    public long countByDataset(String datasetName) {
        return nebulaRepository.countByDataSetName(datasetName);
    }

    /**
     * Check if a nebula with the given name exists in the dataset.
     *
     * @param datasetName the dataset name
     * @param name        the nebula name
     * @return true if exists
     */
    public boolean existsByName(String datasetName, String name) {
        return nebulaRepository.existsByDataSetNameAndName(datasetName, name);
    }

    /**
     * Create a new nebula with default values for the given type.
     * The nebula is not saved; call save() to persist.
     *
     * @param name        display name
     * @param type        nebula type (determines default colors and parameters)
     * @param datasetName dataset to associate with
     * @param x           center X in light-years
     * @param y           center Y in light-years
     * @param z           center Z in light-years
     * @param radius      outer radius in light-years
     * @return new Nebula entity with type defaults applied
     */
    public Nebula createWithDefaults(String name, NebulaType type, String datasetName,
                                      double x, double y, double z, double radius) {
        Nebula nebula = new Nebula(name, type, datasetName, x, y, z, radius);
        nebula.applyTypeDefaults();
        return nebula;
    }

    /**
     * Duplicate an existing nebula with a new name.
     * The duplicate is not saved; call save() to persist.
     *
     * @param original the nebula to duplicate
     * @param newName  name for the duplicate
     * @return new Nebula entity with copied properties
     */
    public Nebula duplicate(@NotNull Nebula original, String newName) {
        Nebula copy = new Nebula(
                newName,
                original.getType(),
                original.getDataSetName(),
                original.getCenterX(),
                original.getCenterY(),
                original.getCenterZ(),
                original.getOuterRadius()
        );

        // Copy all properties
        copy.setInnerRadius(original.getInnerRadius());
        copy.setParticleDensity(original.getParticleDensity());
        copy.setNumElementsOverride(original.getNumElementsOverride());
        copy.setRadialPower(original.getRadialPower());
        copy.setNoiseStrength(original.getNoiseStrength());
        copy.setNoiseOctaves(original.getNoiseOctaves());
        copy.setPrimaryColor(original.getPrimaryColor());
        copy.setSecondaryColor(original.getSecondaryColor());
        copy.setOpacity(original.getOpacity());
        copy.setEnableAnimation(original.isEnableAnimation());
        copy.setBaseAngularSpeed(original.getBaseAngularSpeed());
        // Generate new seed for variation
        copy.setSeed(System.currentTimeMillis());
        copy.setSourceCatalog(original.getSourceCatalog());
        copy.setCatalogId(null); // Clear catalog ID for user copy
        copy.setNotes(original.getNotes());

        return copy;
    }
}
