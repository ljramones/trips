package com.teamgannon.trips.jpa.repository;

import com.teamgannon.trips.jpa.model.AstrographicObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * used to access the data
 * <p>
 * Created by larrymitchell on 2017-03-28.
 */
public interface AstrographicObjectRepository
        extends PagingAndSortingRepository<AstrographicObject, UUID>, AstrographicObjectRepositoryCustom {

    /**
     * find by a list of ids
     *
     * @param astrographicDataList the list of stars to search for
     * @param page                 the page to position by
     * @return the stars
     */
    Page<AstrographicObject> findByIdIn(Collection<UUID> astrographicDataList, Pageable page);

    /**
     * this is the distance from Sol
     *
     * @param limitDistance the distance to search
     * @param page          the limit of pages to search for
     * @return the stars
     */
    Page<AstrographicObject> findByDistanceIsLessThan(double limitDistance, Pageable page);

    /**
     * find all objects by dataset name
     *
     * @param name the name
     * @return the lsit of objects
     */
    List<AstrographicObject> findByDataSetName(String name);

    /**
     * delete all stars stored with a specific dataset name
     *
     * @param name the name
     */
    void deleteByDataSetName(String name);

    /**
     * this is the distance from Sol
     *
     * @param limitDistance the distance to search
     * @return the list of applicable stars
     */
    List<AstrographicObject> findByDistanceIsLessThan(double limitDistance);

    List<AstrographicObject> findByDataSetNameAndXGreaterThanAndXLessThanAndYGreaterThanAndYLessThanAndZGreaterThanAndZLessThan(
            String dataSetName,
            double xg,
            double xl,
            double yg,
            double yl,
            double zg,
            double zl
    );
}
