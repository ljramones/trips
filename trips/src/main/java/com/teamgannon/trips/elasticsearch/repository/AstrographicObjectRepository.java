package com.teamgannon.trips.elasticsearch.repository;

import com.teamgannon.trips.elasticsearch.model.AstrographicObject;
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
     * find by the id of the star
     *
     * @param id the id
     * @return the star
     */
    AstrographicObject findById(UUID id);

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
     * this is the distance from Sol
     *
     * @param limitDistance the distance to search
     * @return the list of applicable stars
     */
    List<AstrographicObject> findByDistanceIsLessThan(double limitDistance);

    List<AstrographicObject> findByXGreaterThanAndXLessThanAndYGreaterThanAndYLessThanAndZGreaterThanAndZLessThan(
            double xg,
            double xl,
            double yg,
            double yl,
            double zg,
            double zl
    );
}
