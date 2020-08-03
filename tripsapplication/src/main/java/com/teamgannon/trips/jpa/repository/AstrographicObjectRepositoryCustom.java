package com.teamgannon.trips.jpa.repository;

import com.teamgannon.trips.jpa.model.AstrographicObject;
import com.teamgannon.trips.search.AstroSearchQuery;

import java.util.List;

/**
 * Created by larrymitchell on 2017-04-19.
 */
public interface AstrographicObjectRepositoryCustom {

    List<AstrographicObject> findBySearchQuery(AstroSearchQuery astroSearchQuery);

}
