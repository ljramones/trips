package com.teamgannon.trips.jpa.repository;

import com.teamgannon.trips.jpa.model.StarObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * used to access the data
 * <p>
 * Created by larrymitchell on 2017-03-28.
 */
public interface StarObjectRepository
        extends PagingAndSortingRepository<StarObject, UUID>, StarObjectRepositoryCustom {

    /**
     * find by a list of ids
     *
     * @param astrographicDataList the list of stars to search for
     * @param page                 the page to position by
     * @return the stars
     */
    Page<StarObject> findByIdIn(Collection<UUID> astrographicDataList, Pageable page);

    /**
     * find by a list of ids
     *
     * @param astrographicDataList the list of stars to search for
     * @return the stars
     */
    List<StarObject> findByIdIn(Collection<UUID> astrographicDataList);

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

    List<StarObject> findByCommonNameContainsIgnoreCase(String commonName);

    List<StarObject> findByConstellationName(String constellationName);

}
