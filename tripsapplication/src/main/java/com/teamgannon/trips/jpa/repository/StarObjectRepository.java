package com.teamgannon.trips.jpa.repository;

import com.teamgannon.trips.jpa.model.StarObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * used to access the data
 * <p>
 * Created by larrymitchell on 2017-03-28.
 */
public interface StarObjectRepository
        extends PagingAndSortingRepository<StarObject, String>, StarObjectRepositoryCustom {

    /**
     * find by a list of ids
     *
     * @param astrographicDataList the list of stars to search for
     * @param page                 the page to position by
     * @return the stars
     */
    Page<StarObject> findByIdIn(Collection<String> astrographicDataList, Pageable page);

    /**
     * find by an alias for this star
     *
     * @param alias the alias to find
     * @return the stars that matches
     */
    @Query("SELECT s FROM STAR_OBJ s JOIN s.aliasList t WHERE t = LOWER(:alias)")
    List<StarObject> retrieveByAlias(@Param("alias") String alias);

    /**
     * find by a list of ids
     *
     * @param astrographicDataList the list of stars to search for
     * @return the stars
     */
    List<StarObject> findByIdIn(Collection<String> astrographicDataList);

    /**
     * this is the distance from Sol
     *
     * @param limitDistance the distance to search
     * @param page          the limit of pages to search for
     * @return the stars
     */
    Page<StarObject> findByDataSetNameAndDistanceIsLessThanOrderByDisplayName(
            String dataSetName, double limitDistance, Pageable page);

    /**
     * find all objects by dataset name
     *
     * @param dataSetName the name
     * @return the list of objects
     */
    Page<StarObject> findByDataSetName(String dataSetName, Pageable page);

    /**
     * get by dataset name
     *
     * @param dataset the data set name
     * @return a stream of star objects
     */
    @Transactional
    Stream<StarObject> findByDataSetName(String dataset);


    /**
     * find a star containing a partial match on a name in a specified dataset
     *
     * @param dataSetName the dataset
     * @param nameMatch   the partial match
     * @return the list of objects that match what we search for
     */
    List<StarObject> findByDataSetNameAndDisplayNameContainsIgnoreCase(String dataSetName, String nameMatch);


    /**
     * find all objects by dataset name
     *
     * @param dataSetName the name
     * @return the list of objects
     */
    List<StarObject> findByDataSetNameOrderByDisplayName(String dataSetName);

    /**
     * delete all stars stored with a specific dataset name
     *
     * @param dataSetName the name
     */
    void deleteByDataSetName(String dataSetName);

    /**
     * this is the distance from Sol
     *
     * @param dataSetName   the name of the dataset to search by
     * @param limitDistance the distance to search
     * @return the list of applicable stars
     */
    Stream<StarObject> findByDataSetNameAndDistanceIsLessThanEqual(String dataSetName, double limitDistance);

    /**
     * get a count of the stars under a specified range
     *
     * @param dataSetName   the dataset name
     * @param limitDistance the max distance
     * @return the count of starts
     */
    long countByDataSetNameAndDistanceIsLessThanEqual(String dataSetName, double limitDistance);

    /**
     * get a count on the number of stars in this data set
     *
     * @param dataSetName name of the datset
     * @return the count
     */
    long countByDataSetName(String dataSetName);

    List<StarObject> findByDataSetNameAndXGreaterThanAndXLessThanAndYGreaterThanAndYLessThanAndZGreaterThanAndZLessThanOrderByDisplayName(
            String dataSetName,
            double xg,
            double xl,
            double yg,
            double yl,
            double zg,
            double zl
    );

    Page<StarObject> findByDataSetNameAndXGreaterThanAndXLessThanAndYGreaterThanAndYLessThanAndZGreaterThanAndZLessThanOrderByDisplayName(
            String dataSetName,
            double xg,
            double xl,
            double yg,
            double yl,
            double zg,
            double zl,
            Pageable pageable
    );

    int countByDataSetNameAndXGreaterThanAndXLessThanAndYGreaterThanAndYLessThanAndZGreaterThanAndZLessThanOrderByDisplayName(
            String dataSetName,
            double xg,
            double xl,
            double yg,
            double yl,
            double zg,
            double zl
    );

    /**
     * Stream stars within a 3D coordinate bounding box for a specific dataset.
     * Used by NightSkyService for efficient star queries on large datasets.
     *
     * @param datasetName the dataset name
     * @param minX minimum X coordinate (light years)
     * @param maxX maximum X coordinate (light years)
     * @param minY minimum Y coordinate (light years)
     * @param maxY maximum Y coordinate (light years)
     * @param minZ minimum Z coordinate (light years)
     * @param maxZ maximum Z coordinate (light years)
     * @return stream of matching stars
     */
    @Query("SELECT s FROM STAR_OBJ s WHERE s.dataSetName = :datasetName " +
           "AND s.x >= :minX AND s.x <= :maxX " +
           "AND s.y >= :minY AND s.y <= :maxY " +
           "AND s.z >= :minZ AND s.z <= :maxZ")
    @Transactional(readOnly = true)
    Stream<StarObject> streamByDataSetNameAndCoordinateRange(
            @Param("datasetName") String datasetName,
            @Param("minX") double minX, @Param("maxX") double maxX,
            @Param("minY") double minY, @Param("maxY") double maxY,
            @Param("minZ") double minZ, @Param("maxZ") double maxZ);

    List<StarObject> findByCatalogIds_CatalogIdListContainsIgnoreCase(String catalogId);

    List<StarObject> findByCatalogIds_CatalogIdListContainsIgnoreCaseAndDataSetName(String catalogId, String dataSetName);

    List<StarObject> findByCommonNameContainsIgnoreCase(String commonName);

    List<StarObject> findByCommonNameContainsIgnoreCaseAndDataSetName(String commonName, String dataSetName);

    List<StarObject> findByConstellationName(String constellationName);

    List<StarObject> findByConstellationNameAndDataSetName(String constellationName, String dataSetName);

    StarObject findByCatalogIds_BayerCatIdAndDataSetName(String bayerId, String dataSetName);

    StarObject findByCatalogIds_FlamsteedCatIdAndDataSetName(String flamsteedId, String dataSetName);

    StarObject findByCatalogIds_GlieseCatIdAndDataSetName(String glieseId, String dataSetName);

    StarObject findByCatalogIds_HipCatIdAndDataSetName(String hipparcosId, String dataSetName);

    StarObject findByCatalogIds_HdCatIdAndDataSetName(String henryDraperId, String dataSetName);

    StarObject findByCatalogIds_CsiCatIdAndDataSetName(String csiId, String dataSetName);

    StarObject findByCatalogIds_Tycho2CatIdAndDataSetName(String tycId, String dataSetName);

    StarObject findByCatalogIds_TwoMassCatIdAndDataSetName(String twoMassId, String dataSetName);

    StarObject findByCatalogIds_GaiaDR2CatIdAndDataSetName(String gaiaDr2Id, String dataSetName);

    StarObject findByCatalogIds_GaiaDR3CatIdAndDataSetName(String gaiaDr3Id, String dataSetName);

    StarObject findByCatalogIds_GaiaEDR3CatIdAndDataSetName(String gaiaEdr3Id, String dataSetName);

    @Query("SELECT s FROM STAR_OBJ s WHERE s.dataSetName = :dataSetName AND s.distance = 0 "
            + "AND ((s.catalogIds.gaiaDR3CatId is not null and s.catalogIds.gaiaDR3CatId <> '') "
            + "OR (s.catalogIds.hipCatId is not null and s.catalogIds.hipCatId <> '') "
            + "OR (s.catalogIds.catalogIdList is not null and s.catalogIds.catalogIdList <> ''))")
    Page<StarObject> findMissingDistanceWithIds(@Param("dataSetName") String dataSetName, Pageable pageable);

    @Query("SELECT COUNT(s) FROM STAR_OBJ s WHERE s.dataSetName = :dataSetName AND s.distance = 0")
    long countMissingDistance(@Param("dataSetName") String dataSetName);

    /**
     * Find all stars that belong to a specific solar system
     *
     * @param solarSystemId the solar system ID
     * @return list of stars in the system
     */
    List<StarObject> findBySolarSystemId(String solarSystemId);

    /**
     * Count stars in a solar system
     *
     * @param solarSystemId the solar system ID
     * @return count of stars
     */
    long countBySolarSystemId(String solarSystemId);

    /**
     * Find a star by its display name (case insensitive)
     *
     * @param displayName the star's display name
     * @return the star, or null if not found
     */
    StarObject findFirstByDisplayNameIgnoreCase(String displayName);

    /**
     * Find all stars that have exoplanets linked via solarSystemId.
     * Uses a subquery to find star IDs that appear as hostStarId in exoplanets.
     *
     * @return list of stars with planets
     */
    @Query("SELECT s FROM STAR_OBJ s WHERE s.id IN (SELECT DISTINCT e.hostStarId FROM EXOPLANET e WHERE e.hostStarId IS NOT NULL AND (e.isMoon IS NULL OR e.isMoon = false))")
    List<StarObject> findStarsWithPlanets();

    /**
     * Find stars near a given RA/Dec coordinate (within 0.01 degrees)
     *
     * @param ra the right ascension
     * @param dec the declination
     * @return list of stars near the coordinates
     */
    @Query("SELECT s FROM STAR_OBJ s WHERE s.ra BETWEEN :ra - 0.01 AND :ra + 0.01 AND s.declination BETWEEN :dec - 0.01 AND :dec + 0.01")
    List<StarObject> findByRaAndDecNear(@Param("ra") Double ra, @Param("dec") Double dec);

}
