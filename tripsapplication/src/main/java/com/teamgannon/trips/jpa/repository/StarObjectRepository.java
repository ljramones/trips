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

    List<StarObject> findByCatalogIdListContainsIgnoreCase(String catalogId);

    List<StarObject> findByCatalogIdListContainsIgnoreCaseAndDataSetName(String catalogId, String dataSetName);

    List<StarObject> findByCommonNameContainsIgnoreCase(String commonName);

    List<StarObject> findByCommonNameContainsIgnoreCaseAndDataSetName(String commonName, String dataSetName);

    List<StarObject> findByConstellationName(String constellationName);

    List<StarObject> findByConstellationNameAndDataSetName(String constellationName, String dataSetName);

   StarObject findByBayerCatIdAndDataSetName(String bayerId, String dataSetName);

   StarObject findByFlamsteedCatIdAndDataSetName(String flamsteedId, String dataSetName);

   StarObject findByGlieseCatIdAndDataSetName(String glieseId, String dataSetName);

   StarObject findByHipCatIdAndDataSetName(String hipparcosId, String dataSetName);

    StarObject findByHdCatIdAndDataSetName(String henryDraperId, String dataSetName);

    StarObject findByCsiCatIdAndDataSetName(String csiId, String dataSetName);

    StarObject findByTycho2CatIdAndDataSetName(String tycId, String dataSetName);

    StarObject findByTwoMassCatIdAndDataSetName(String twoMassId, String dataSetName);

    StarObject findByGaiaDR2CatIdAndDataSetName(String gaiaDr2Id, String dataSetName);

    StarObject findByGaiaDR3CatIdAndDataSetName(String gaiaDr3Id, String dataSetName);

    StarObject findByGaiaEDR3CatIdAndDataSetName(String gaiaEdr3Id, String dataSetName);

    @Query("SELECT s FROM STAR_OBJ s WHERE s.dataSetName = :dataSetName AND s.distance = 0 "
            + "AND ((s.gaiaDR3CatId is not null and s.gaiaDR3CatId <> '') "
            + "OR (s.hipCatId is not null and s.hipCatId <> '') "
            + "OR (s.catalogIdList is not null and s.catalogIdList <> ''))")
    Page<StarObject> findMissingDistanceWithIds(@Param("dataSetName") String dataSetName, Pageable pageable);



}
