package com.teamgannon.trips.jpa.repository;

import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.search.AstroSearchQuery;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

/**
 * Created by larrymitchell on 2017-04-19.
 */
public interface StarObjectRepositoryCustom {

    /**
     * get a list of star objects
     *
     * @param astroSearchQuery the astro query
     * @return the list of StarObjects
     */
    @Transactional
    List<StarObject> findBySearchQuery(AstroSearchQuery astroSearchQuery);

    /**
     * get a list of star objects
     *
     * @param astroSearchQuery the astro query
     * @return the list of StarObjects
     */
    @Transactional(readOnly = true)
    Page<StarObject> findBySearchQueryPaged(AstroSearchQuery astroSearchQuery, Pageable pageable);

    /**
     * same as above but by stream
     *
     * @param astroSearchQuery the astro query
     * @return the stream of StarObjects
     */
    @Transactional(readOnly = true)
    Stream<StarObject> findBySearchQueryStream(@NotNull AstroSearchQuery astroSearchQuery);

    /**
     * count the number of stars matching a search query
     *
     * @param astroSearchQuery the astro query
     * @return the count of matching stars
     */
    @Transactional(readOnly = true)
    long countBySearchQuery(AstroSearchQuery astroSearchQuery);

}
