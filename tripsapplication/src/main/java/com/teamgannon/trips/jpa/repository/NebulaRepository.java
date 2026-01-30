package com.teamgannon.trips.jpa.repository;

import com.teamgannon.trips.jpa.model.Nebula;
import com.teamgannon.trips.jpa.model.NebulaType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Nebula entities.
 * Provides methods to query nebulae by various criteria including spatial queries.
 */
public interface NebulaRepository extends JpaRepository<Nebula, String> {

    // ==================== Basic Queries ====================

    /**
     * Find a nebula by name within a dataset
     */
    Optional<Nebula> findByNameAndDataSetName(String name, String dataSetName);

    /**
     * Find a nebula by catalog ID (e.g., "M42", "NGC 1976")
     */
    Optional<Nebula> findByCatalogId(String catalogId);

    /**
     * Find a nebula by catalog ID within a dataset
     */
    Optional<Nebula> findByCatalogIdAndDataSetName(String catalogId, String dataSetName);

    /**
     * Find all nebulae in a dataset
     */
    List<Nebula> findByDataSetName(String dataSetName);

    /**
     * Find all nebulae in a dataset ordered by name
     */
    List<Nebula> findByDataSetNameOrderByNameAsc(String dataSetName);

    /**
     * Find all nebulae of a specific type in a dataset
     */
    List<Nebula> findByDataSetNameAndType(String dataSetName, NebulaType type);

    /**
     * Find all nebulae of a specific type in a dataset, ordered by name
     */
    List<Nebula> findByDataSetNameAndTypeOrderByNameAsc(String dataSetName, NebulaType type);

    /**
     * Find all nebulae from a specific catalog source
     */
    List<Nebula> findBySourceCatalog(String sourceCatalog);

    /**
     * Find all nebulae from a specific catalog source in a dataset
     */
    List<Nebula> findByDataSetNameAndSourceCatalog(String dataSetName, String sourceCatalog);

    // ==================== Spatial Queries ====================

    /**
     * Find all nebulae within a spherical plot region.
     * A nebula is considered within the region if any part of it
     * (center - outerRadius to center + outerRadius) intersects the plot sphere.
     * <p>
     * Uses the formula: distance(nebula_center, plot_center) - nebula_radius <= plot_radius
     *
     * @param dataSetName dataset to search
     * @param cx          plot center X coordinate (light-years)
     * @param cy          plot center Y coordinate (light-years)
     * @param cz          plot center Z coordinate (light-years)
     * @param plotRadius  radius of the plot sphere (light-years)
     * @return list of nebulae that intersect the plot region
     */
    @Query("""
           SELECT n FROM NEBULA n WHERE n.dataSetName = :dataSetName \
           AND (SQRT(POWER(n.centerX - :cx, 2) + POWER(n.centerY - :cy, 2) + \
           POWER(n.centerZ - :cz, 2)) - n.outerRadius) <= :plotRadius \
           ORDER BY n.name ASC\
           """)
    List<Nebula> findInPlotRange(
            @Param("dataSetName") String dataSetName,
            @Param("cx") double cx,
            @Param("cy") double cy,
            @Param("cz") double cz,
            @Param("plotRadius") double plotRadius);

    /**
     * Find all nebulae within a bounding box.
     * Faster than spherical query for initial filtering.
     */
    @Query("""
           SELECT n FROM NEBULA n WHERE n.dataSetName = :dataSetName \
           AND n.centerX BETWEEN :minX AND :maxX \
           AND n.centerY BETWEEN :minY AND :maxY \
           AND n.centerZ BETWEEN :minZ AND :maxZ \
           ORDER BY n.name ASC\
           """)
    List<Nebula> findInBoundingBox(
            @Param("dataSetName") String dataSetName,
            @Param("minX") double minX, @Param("maxX") double maxX,
            @Param("minY") double minY, @Param("maxY") double maxY,
            @Param("minZ") double minZ, @Param("maxZ") double maxZ);

    /**
     * Find nebulae within a distance from a point.
     * Does not account for nebula radius - just center point distance.
     */
    @Query("""
           SELECT n FROM NEBULA n WHERE n.dataSetName = :dataSetName \
           AND SQRT(POWER(n.centerX - :x, 2) + POWER(n.centerY - :y, 2) + \
           POWER(n.centerZ - :z, 2)) <= :maxDistance \
           ORDER BY SQRT(POWER(n.centerX - :x, 2) + POWER(n.centerY - :y, 2) + \
           POWER(n.centerZ - :z, 2)) ASC\
           """)
    List<Nebula> findByDistanceFrom(
            @Param("dataSetName") String dataSetName,
            @Param("x") double x,
            @Param("y") double y,
            @Param("z") double z,
            @Param("maxDistance") double maxDistance);

    // ==================== Count Queries ====================

    /**
     * Count nebulae in a dataset
     */
    long countByDataSetName(String dataSetName);

    /**
     * Count nebulae of a specific type in a dataset
     */
    long countByDataSetNameAndType(String dataSetName, NebulaType type);

    /**
     * Count nebulae from a specific catalog source in a dataset
     */
    long countByDataSetNameAndSourceCatalog(String dataSetName, String sourceCatalog);

    // ==================== Existence Queries ====================

    /**
     * Check if a nebula with the given name exists in a dataset
     */
    boolean existsByNameAndDataSetName(String name, String dataSetName);

    /**
     * Check if a nebula with the given name exists in a dataset (alternate parameter order)
     */
    boolean existsByDataSetNameAndName(String dataSetName, String name);

    /**
     * Check if a nebula with the given catalog ID exists
     */
    boolean existsByCatalogId(String catalogId);

    /**
     * Check if a nebula with the given catalog ID exists in a dataset
     */
    boolean existsByCatalogIdAndDataSetName(String catalogId, String dataSetName);

    // ==================== Delete Queries ====================

    /**
     * Delete all nebulae in a dataset
     */
    void deleteByDataSetName(String dataSetName);

    /**
     * Delete all nebulae from a specific catalog source in a dataset
     */
    void deleteByDataSetNameAndSourceCatalog(String dataSetName, String sourceCatalog);

    // ==================== Search Queries ====================

    /**
     * Find nebulae by name pattern (case-insensitive)
     */
    @Query("""
           SELECT n FROM NEBULA n WHERE n.dataSetName = :dataSetName \
           AND LOWER(n.name) LIKE LOWER(CONCAT('%', :pattern, '%')) \
           ORDER BY n.name ASC\
           """)
    List<Nebula> findByNameContaining(
            @Param("dataSetName") String dataSetName,
            @Param("pattern") String pattern);

    /**
     * Find nebulae by catalog ID pattern (case-insensitive)
     */
    @Query("""
           SELECT n FROM NEBULA n WHERE n.dataSetName = :dataSetName \
           AND LOWER(n.catalogId) LIKE LOWER(CONCAT('%', :pattern, '%')) \
           ORDER BY n.catalogId ASC\
           """)
    List<Nebula> findByCatalogIdContaining(
            @Param("dataSetName") String dataSetName,
            @Param("pattern") String pattern);

    // ==================== Statistics Queries ====================

    /**
     * Get all distinct nebula types in a dataset
     */
    @Query("SELECT DISTINCT n.type FROM NEBULA n WHERE n.dataSetName = :dataSetName")
    List<NebulaType> findDistinctTypesByDataSetName(@Param("dataSetName") String dataSetName);

    /**
     * Get all distinct catalog sources in a dataset
     */
    @Query("SELECT DISTINCT n.sourceCatalog FROM NEBULA n WHERE n.dataSetName = :dataSetName")
    List<String> findDistinctSourceCatalogsByDataSetName(@Param("dataSetName") String dataSetName);

    /**
     * Find the largest nebulae in a dataset (by outer radius)
     */
    @Query("""
           SELECT n FROM NEBULA n WHERE n.dataSetName = :dataSetName \
           ORDER BY n.outerRadius DESC\
           """)
    List<Nebula> findLargestNebulae(@Param("dataSetName") String dataSetName);

    /**
     * Find nebulae closest to Sol (origin)
     */
    @Query("""
           SELECT n FROM NEBULA n WHERE n.dataSetName = :dataSetName \
           ORDER BY SQRT(POWER(n.centerX, 2) + POWER(n.centerY, 2) + POWER(n.centerZ, 2)) ASC\
           """)
    List<Nebula> findClosestToSol(@Param("dataSetName") String dataSetName);
}
