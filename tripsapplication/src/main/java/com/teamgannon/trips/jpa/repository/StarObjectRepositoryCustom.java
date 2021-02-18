package com.teamgannon.trips.jpa.repository;

import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.search.AstroSearchQuery;

import java.util.List;
import java.util.stream.Stream;

/**
 * Created by larrymitchell on 2017-04-19.
 */
public interface StarObjectRepositoryCustom {

    List<StarObject> findBySearchQuery(AstroSearchQuery astroSearchQuery);

    Stream<StarObject> getAllFromDataset(String dataset);

}
