package com.teamgannon.trips.elasticsearch.repository;

import com.teamgannon.trips.elasticsearch.model.Planet;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by larrymitchell on 2017-01-25.
 */
public interface PlanetRepository extends PagingAndSortingRepository<Planet, String> {
}
