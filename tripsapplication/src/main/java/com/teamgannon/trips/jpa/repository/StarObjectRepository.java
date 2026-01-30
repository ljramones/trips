package com.teamgannon.trips.jpa.repository;

import com.teamgannon.trips.jpa.model.StarObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * Repository for accessing StarObject data.
 * <p>
 * Created by larrymitchell on 2017-03-28.
 */
public interface StarObjectRepository
        extends JpaRepository<StarObject, String>, StarObjectRepositoryCustom {

    // ========== Basic ID queries ==========

    /**
     * Find a star by ID with aliases eagerly loaded.
     * Use this when the star will be accessed outside a transaction (e.g., in UI code).
     */
    @Query("SELECT DISTINCT s FROM STAR_OBJ s LEFT JOIN FETCH s.aliasList WHERE s.id = :id")
    StarObject findByIdWithAliases(@Param("id") String id);

    /**
     * Find stars by a list of IDs (paged).
     */
    Page<StarObject> findByIdIn(Collection<String> ids, Pageable page);

    /**
     * Find stars by a list of IDs.
     */
    List<StarObject> findByIdIn(Collection<String> ids);

    // ========== Dataset queries ==========

    /**
     * Find all stars in a dataset (paged).
     */
    Page<StarObject> findByDataSetName(String dataSetName, Pageable page);

    /**
     * Stream all stars in a dataset.
     */
    @Transactional
    Stream<StarObject> findByDataSetName(String dataset);

    /**
     * Find all stars in a dataset ordered by display name.
     */
    List<StarObject> findByDataSetNameOrderByDisplayName(String dataSetName);

    /**
     * Delete all stars in a dataset.
     */
    void deleteByDataSetName(String dataSetName);

    /**
     * Count stars in a dataset.
     */
    long countByDataSetName(String dataSetName);

    // ========== Distance queries ==========

    /**
     * Find stars within a distance limit (paged, ordered by display name).
     */
    @Query("""
           SELECT s FROM STAR_OBJ s WHERE s.dataSetName = :dataSetName \
           AND s.distance < :maxDistance ORDER BY s.displayName\
           """)
    Page<StarObject> findByDistanceLessThan(
            @Param("dataSetName") String dataSetName,
            @Param("maxDistance") double maxDistance,
            Pageable page);

    /**
     * Stream stars within a distance limit.
     */
    @Query("""
           SELECT s FROM STAR_OBJ s WHERE s.dataSetName = :dataSetName \
           AND s.distance <= :maxDistance\
           """)
    Stream<StarObject> streamByDistanceWithin(
            @Param("dataSetName") String dataSetName,
            @Param("maxDistance") double maxDistance);

    /**
     * Count stars within a distance limit.
     */
    @Query("""
           SELECT COUNT(s) FROM STAR_OBJ s WHERE s.dataSetName = :dataSetName \
           AND s.distance <= :maxDistance\
           """)
    long countByDistanceWithin(
            @Param("dataSetName") String dataSetName,
            @Param("maxDistance") double maxDistance);

    // ========== 3D Coordinate bounding box queries ==========

    /**
     * Find stars within a 3D coordinate bounding box (ordered by display name).
     */
    @Query("""
           SELECT s FROM STAR_OBJ s WHERE s.dataSetName = :dataSetName \
           AND s.x > :minX AND s.x < :maxX \
           AND s.y > :minY AND s.y < :maxY \
           AND s.z > :minZ AND s.z < :maxZ \
           ORDER BY s.displayName\
           """)
    List<StarObject> findInBoundingBox(
            @Param("dataSetName") String dataSetName,
            @Param("minX") double minX, @Param("maxX") double maxX,
            @Param("minY") double minY, @Param("maxY") double maxY,
            @Param("minZ") double minZ, @Param("maxZ") double maxZ);

    /**
     * Find stars within a 3D coordinate bounding box (paged, ordered by display name).
     */
    @Query("""
           SELECT s FROM STAR_OBJ s WHERE s.dataSetName = :dataSetName \
           AND s.x > :minX AND s.x < :maxX \
           AND s.y > :minY AND s.y < :maxY \
           AND s.z > :minZ AND s.z < :maxZ \
           ORDER BY s.displayName\
           """)
    Page<StarObject> findInBoundingBoxPaged(
            @Param("dataSetName") String dataSetName,
            @Param("minX") double minX, @Param("maxX") double maxX,
            @Param("minY") double minY, @Param("maxY") double maxY,
            @Param("minZ") double minZ, @Param("maxZ") double maxZ,
            Pageable pageable);

    /**
     * Count stars within a 3D coordinate bounding box.
     */
    @Query("""
           SELECT COUNT(s) FROM STAR_OBJ s WHERE s.dataSetName = :dataSetName \
           AND s.x > :minX AND s.x < :maxX \
           AND s.y > :minY AND s.y < :maxY \
           AND s.z > :minZ AND s.z < :maxZ\
           """)
    int countInBoundingBox(
            @Param("dataSetName") String dataSetName,
            @Param("minX") double minX, @Param("maxX") double maxX,
            @Param("minY") double minY, @Param("maxY") double maxY,
            @Param("minZ") double minZ, @Param("maxZ") double maxZ);

    /**
     * Stream stars within a 3D coordinate bounding box.
     * Used by NightSkyService for efficient star queries on large datasets.
     */
    @Query("""
           SELECT s FROM STAR_OBJ s WHERE s.dataSetName = :dataSetName \
           AND s.x >= :minX AND s.x <= :maxX \
           AND s.y >= :minY AND s.y <= :maxY \
           AND s.z >= :minZ AND s.z <= :maxZ\
           """)
    @Transactional(readOnly = true)
    Stream<StarObject> streamInBoundingBox(
            @Param("dataSetName") String dataSetName,
            @Param("minX") double minX, @Param("maxX") double maxX,
            @Param("minY") double minY, @Param("maxY") double maxY,
            @Param("minZ") double minZ, @Param("maxZ") double maxZ);

    // ========== Magnitude-based queries for night sky rendering ==========

    /**
     * Stream all stars with V-band magnitude brighter than limit.
     * Used for efficient night sky rendering - filters at database level.
     */
    @Query("SELECT s FROM STAR_OBJ s WHERE s.magv <= :maxMag AND s.magv <> 0")
    @Transactional(readOnly = true)
    Stream<StarObject> streamByMagnitudeBrighterThan(@Param("maxMag") double maxMag);

    /**
     * Stream stars in a dataset with V-band magnitude brighter than limit.
     * Used for efficient night sky rendering - filters at database level.
     */
    @Query("""
           SELECT s FROM STAR_OBJ s WHERE s.dataSetName = :dataSetName \
           AND s.magv <= :maxMag AND s.magv <> 0\
           """)
    @Transactional(readOnly = true)
    Stream<StarObject> streamByDatasetAndMagnitude(
            @Param("dataSetName") String dataSetName,
            @Param("maxMag") double maxMag);

    /**
     * Count stars with magnitude brighter than limit (for progress reporting).
     */
    @Query("SELECT COUNT(s) FROM STAR_OBJ s WHERE s.magv <= :maxMag AND s.magv <> 0")
    long countByMagnitudeBrighterThan(@Param("maxMag") double maxMag);

    /**
     * Stream all stars (for night sky when no magnitude filter is practical).
     * More efficient than findAll() as it doesn't load into memory all at once.
     */
    @Query("SELECT s FROM STAR_OBJ s")
    @Transactional(readOnly = true)
    Stream<StarObject> streamAll();

    // ========== Name search queries ==========

    /**
     * Find star by alias.
     */
    @Query("SELECT s FROM STAR_OBJ s JOIN s.aliasList t WHERE t = LOWER(:alias)")
    List<StarObject> findByAlias(@Param("alias") String alias);

    /**
     * Find stars by partial display name match in a dataset.
     */
    @Query("""
           SELECT s FROM STAR_OBJ s WHERE s.dataSetName = :dataSetName \
           AND LOWER(s.displayName) LIKE LOWER(CONCAT('%', :nameMatch, '%'))\
           """)
    List<StarObject> findByDisplayNameContaining(
            @Param("dataSetName") String dataSetName,
            @Param("nameMatch") String nameMatch);

    /**
     * Find stars by partial common name match.
     */
    @Query("SELECT s FROM STAR_OBJ s WHERE LOWER(s.commonName) LIKE LOWER(CONCAT('%', :commonName, '%'))")
    List<StarObject> findByCommonNameContaining(@Param("commonName") String commonName);

    /**
     * Find stars by partial common name match in a dataset.
     */
    @Query("""
           SELECT s FROM STAR_OBJ s WHERE s.dataSetName = :dataSetName \
           AND LOWER(s.commonName) LIKE LOWER(CONCAT('%', :commonName, '%'))\
           """)
    List<StarObject> findByCommonNameContaining(
            @Param("dataSetName") String dataSetName,
            @Param("commonName") String commonName);

    /**
     * Find star by display name (case insensitive, returns first match).
     */
    StarObject findFirstByDisplayNameIgnoreCase(String displayName);

    // ========== Constellation queries ==========

    /**
     * Find stars by constellation name.
     */
    List<StarObject> findByConstellationName(String constellationName);

    /**
     * Find stars by constellation name in a dataset.
     */
    @Query("""
           SELECT s FROM STAR_OBJ s WHERE s.dataSetName = :dataSetName \
           AND s.constellationName = :constellationName\
           """)
    List<StarObject> findByConstellation(
            @Param("dataSetName") String dataSetName,
            @Param("constellationName") String constellationName);

    // ========== Catalog ID queries ==========

    /**
     * Find stars by catalog ID list containing a value.
     * Note: Case-sensitive search due to Hibernate 6 LOB handling.
     */
    @Query("SELECT s FROM STAR_OBJ s WHERE s.catalogIds.catalogIdList LIKE CONCAT('%', :catalogId, '%')")
    List<StarObject> findByCatalogId(@Param("catalogId") String catalogId);

    /**
     * Find stars by catalog ID list containing a value in a dataset.
     * Note: Case-sensitive search due to Hibernate 6 LOB handling.
     */
    @Query("SELECT s FROM STAR_OBJ s WHERE s.dataSetName = :dataSetName " +
           "AND s.catalogIds.catalogIdList LIKE CONCAT('%', :catalogId, '%')")
    List<StarObject> findByCatalogId(
            @Param("dataSetName") String dataSetName,
            @Param("catalogId") String catalogId);

    /**
     * Find star by Bayer catalog ID in a dataset.
     */
    @Query("""
           SELECT s FROM STAR_OBJ s WHERE s.dataSetName = :dataSetName \
           AND s.catalogIds.bayerCatId = :bayerId\
           """)
    StarObject findByBayerId(
            @Param("dataSetName") String dataSetName,
            @Param("bayerId") String bayerId);

    /**
     * Find star by Flamsteed catalog ID in a dataset.
     */
    @Query("""
           SELECT s FROM STAR_OBJ s WHERE s.dataSetName = :dataSetName \
           AND s.catalogIds.flamsteedCatId = :flamsteedId\
           """)
    StarObject findByFlamsteedId(
            @Param("dataSetName") String dataSetName,
            @Param("flamsteedId") String flamsteedId);

    /**
     * Find star by Gliese catalog ID in a dataset.
     */
    @Query("""
           SELECT s FROM STAR_OBJ s WHERE s.dataSetName = :dataSetName \
           AND s.catalogIds.glieseCatId = :glieseId\
           """)
    StarObject findByGlieseId(
            @Param("dataSetName") String dataSetName,
            @Param("glieseId") String glieseId);

    /**
     * Find star by Hipparcos catalog ID in a dataset.
     */
    @Query("""
           SELECT s FROM STAR_OBJ s WHERE s.dataSetName = :dataSetName \
           AND s.catalogIds.hipCatId = :hipId\
           """)
    StarObject findByHipId(
            @Param("dataSetName") String dataSetName,
            @Param("hipId") String hipId);

    /**
     * Find star by Henry Draper catalog ID in a dataset.
     */
    @Query("""
           SELECT s FROM STAR_OBJ s WHERE s.dataSetName = :dataSetName \
           AND s.catalogIds.hdCatId = :hdId\
           """)
    StarObject findByHdId(
            @Param("dataSetName") String dataSetName,
            @Param("hdId") String hdId);

    /**
     * Find star by CSI catalog ID in a dataset.
     */
    @Query("""
           SELECT s FROM STAR_OBJ s WHERE s.dataSetName = :dataSetName \
           AND s.catalogIds.csiCatId = :csiId\
           """)
    StarObject findByCsiId(
            @Param("dataSetName") String dataSetName,
            @Param("csiId") String csiId);

    /**
     * Find star by Tycho-2 catalog ID in a dataset.
     */
    @Query("""
           SELECT s FROM STAR_OBJ s WHERE s.dataSetName = :dataSetName \
           AND s.catalogIds.tycho2CatId = :tychoId\
           """)
    StarObject findByTychoId(
            @Param("dataSetName") String dataSetName,
            @Param("tychoId") String tychoId);

    /**
     * Find star by 2MASS catalog ID in a dataset.
     */
    @Query("""
           SELECT s FROM STAR_OBJ s WHERE s.dataSetName = :dataSetName \
           AND s.catalogIds.twoMassCatId = :twoMassId\
           """)
    StarObject findByTwoMassId(
            @Param("dataSetName") String dataSetName,
            @Param("twoMassId") String twoMassId);

    /**
     * Find star by Gaia DR2 catalog ID in a dataset.
     */
    @Query("""
           SELECT s FROM STAR_OBJ s WHERE s.dataSetName = :dataSetName \
           AND s.catalogIds.gaiaDR2CatId = :gaiaDr2Id\
           """)
    StarObject findByGaiaDR2Id(
            @Param("dataSetName") String dataSetName,
            @Param("gaiaDr2Id") String gaiaDr2Id);

    /**
     * Find star by Gaia DR3 catalog ID in a dataset.
     */
    @Query("""
           SELECT s FROM STAR_OBJ s WHERE s.dataSetName = :dataSetName \
           AND s.catalogIds.gaiaDR3CatId = :gaiaDr3Id\
           """)
    StarObject findByGaiaDR3Id(
            @Param("dataSetName") String dataSetName,
            @Param("gaiaDr3Id") String gaiaDr3Id);

    /**
     * Find star by Gaia EDR3 catalog ID in a dataset.
     */
    @Query("""
           SELECT s FROM STAR_OBJ s WHERE s.dataSetName = :dataSetName \
           AND s.catalogIds.gaiaEDR3CatId = :gaiaEdr3Id\
           """)
    StarObject findByGaiaEDR3Id(
            @Param("dataSetName") String dataSetName,
            @Param("gaiaEdr3Id") String gaiaEdr3Id);

    // ========== Solar system queries ==========

    /**
     * Find all stars in a solar system.
     */
    List<StarObject> findBySolarSystemId(String solarSystemId);

    /**
     * Count stars in a solar system.
     */
    long countBySolarSystemId(String solarSystemId);

    /**
     * Find all stars that have exoplanets.
     */
    @Query("""
           SELECT s FROM STAR_OBJ s WHERE s.id IN \
           (SELECT DISTINCT e.hostStarId FROM EXOPLANET e \
           WHERE e.hostStarId IS NOT NULL AND (e.isMoon IS NULL OR e.isMoon = false))\
           """)
    List<StarObject> findStarsWithPlanets();

    // ========== Coordinate queries ==========

    /**
     * Find stars near a given RA/Dec coordinate (within 0.01 degrees).
     */
    @Query("""
           SELECT s FROM STAR_OBJ s WHERE s.ra BETWEEN :ra - 0.01 AND :ra + 0.01 \
           AND s.declination BETWEEN :dec - 0.01 AND :dec + 0.01\
           """)
    List<StarObject> findByRaDecNear(
            @Param("ra") Double ra,
            @Param("dec") Double dec);

    // ========== Missing data queries ==========

    /**
     * Find stars with missing distance that have catalog IDs (for enrichment).
     */
    @Query("""
           SELECT s FROM STAR_OBJ s WHERE s.dataSetName = :dataSetName AND s.distance = 0 \
           AND ((s.catalogIds.gaiaDR3CatId IS NOT NULL AND s.catalogIds.gaiaDR3CatId <> '') \
           OR (s.catalogIds.hipCatId IS NOT NULL AND s.catalogIds.hipCatId <> '') \
           OR (s.catalogIds.catalogIdList IS NOT NULL AND s.catalogIds.catalogIdList <> ''))\
           """)
    Page<StarObject> findMissingDistanceWithIds(
            @Param("dataSetName") String dataSetName,
            Pageable pageable);

    /**
     * Count stars with missing distance.
     */
    @Query("SELECT COUNT(s) FROM STAR_OBJ s WHERE s.dataSetName = :dataSetName AND s.distance = 0")
    long countMissingDistance(@Param("dataSetName") String dataSetName);

    /**
     * Find stars eligible for photometric distance estimation.
     * These are stars with missing distance that have both apparent magnitude (magv)
     * and color data (bprp) needed for photometric calculations.
     */
    @Query("""
           SELECT s FROM STAR_OBJ s WHERE s.dataSetName = :dataSetName AND s.distance = 0 \
           AND s.magv <> 0 AND s.bprp <> 0\
           """)
    Page<StarObject> findMissingDistanceWithPhotometry(
            @Param("dataSetName") String dataSetName,
            Pageable pageable);

    /**
     * Count stars eligible for photometric distance estimation.
     */
    @Query("""
           SELECT COUNT(s) FROM STAR_OBJ s WHERE s.dataSetName = :dataSetName AND s.distance = 0 \
           AND s.magv <> 0 AND s.bprp <> 0\
           """)
    long countMissingDistanceWithPhotometry(@Param("dataSetName") String dataSetName);

    /**
     * Find stars with missing mass that have Gaia DR3 IDs (for mass enrichment).
     */
    @Query("""
           SELECT s FROM STAR_OBJ s WHERE s.dataSetName = :dataSetName \
           AND (s.mass = 0 OR s.mass IS NULL) \
           AND s.catalogIds.gaiaDR3CatId IS NOT NULL AND s.catalogIds.gaiaDR3CatId <> ''\
           """)
    Page<StarObject> findMissingMassWithGaiaId(
            @Param("dataSetName") String dataSetName,
            Pageable pageable);

    /**
     * Count stars with missing mass that have Gaia DR3 IDs.
     */
    @Query("""
           SELECT COUNT(s) FROM STAR_OBJ s WHERE s.dataSetName = :dataSetName \
           AND (s.mass = 0 OR s.mass IS NULL) \
           AND s.catalogIds.gaiaDR3CatId IS NOT NULL AND s.catalogIds.gaiaDR3CatId <> ''\
           """)
    long countMissingMassWithGaiaId(@Param("dataSetName") String dataSetName);

    /**
     * Count all stars with missing mass.
     */
    @Query("""
           SELECT COUNT(s) FROM STAR_OBJ s WHERE s.dataSetName = :dataSetName \
           AND (s.mass = 0 OR s.mass IS NULL)\
           """)
    long countMissingMass(@Param("dataSetName") String dataSetName);

    /**
     * Find stars eligible for photometric mass estimation.
     * These are stars with missing mass but have distance and apparent magnitude.
     */
    @Query("""
           SELECT s FROM STAR_OBJ s WHERE s.dataSetName = :dataSetName \
           AND (s.mass = 0 OR s.mass IS NULL) \
           AND s.distance > 0 AND s.magv <> 0\
           """)
    Page<StarObject> findMissingMassWithPhotometry(
            @Param("dataSetName") String dataSetName,
            Pageable pageable);

    /**
     * Count stars eligible for photometric mass estimation.
     */
    @Query("""
           SELECT COUNT(s) FROM STAR_OBJ s WHERE s.dataSetName = :dataSetName \
           AND (s.mass = 0 OR s.mass IS NULL) \
           AND s.distance > 0 AND s.magv <> 0\
           """)
    long countMissingMassWithPhotometry(@Param("dataSetName") String dataSetName);

    /**
     * Get IDs of stars eligible for photometric mass estimation.
     * Much faster than loading full objects - use with findAllById for batch processing.
     */
    @Query("""
           SELECT s.id FROM STAR_OBJ s WHERE s.dataSetName = :dataSetName \
           AND (s.mass = 0 OR s.mass IS NULL) \
           AND s.distance > 0 AND s.magv <> 0\
           """)
    List<String> findMissingMassWithPhotometryIds(@Param("dataSetName") String dataSetName);

    /**
     * Get IDs of stars with BP-RP color but missing temperature.
     */
    @Query("""
           SELECT s.id FROM STAR_OBJ s WHERE s.dataSetName = :dataSetName \
           AND s.bprp <> 0 AND (s.temperature = 0 OR s.temperature IS NULL)\
           """)
    List<String> findMissingTemperatureWithBprpIds(@Param("dataSetName") String dataSetName);

    /**
     * Get IDs of stars with BP-RP color but missing spectral class.
     */
    @Query("""
           SELECT s.id FROM STAR_OBJ s WHERE s.dataSetName = :dataSetName \
           AND s.bprp <> 0 AND (s.spectralClass IS NULL OR s.spectralClass = '')\
           """)
    List<String> findMissingSpectralWithBprpIds(@Param("dataSetName") String dataSetName);

    /**
     * Get IDs of stars with temperature but missing spectral class.
     */
    @Query("""
           SELECT s.id FROM STAR_OBJ s WHERE s.dataSetName = :dataSetName \
           AND s.temperature > 0 AND (s.spectralClass IS NULL OR s.spectralClass = '')\
           """)
    List<String> findMissingSpectralWithTempIds(@Param("dataSetName") String dataSetName);

    /**
     * Get IDs of stars with spectral class but missing temperature.
     */
    @Query("""
           SELECT s.id FROM STAR_OBJ s WHERE s.dataSetName = :dataSetName \
           AND s.spectralClass IS NOT NULL AND s.spectralClass <> '' \
           AND (s.temperature = 0 OR s.temperature IS NULL)\
           """)
    List<String> findMissingTempWithSpectralIds(@Param("dataSetName") String dataSetName);

}
