package com.teamgannon.trips.elasticsearch.repository;

import com.teamgannon.trips.elasticsearch.model.StellarSystem;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by larrymitchell on 2017-01-25.
 */
public interface StellarSystemRepository extends PagingAndSortingRepository<StellarSystem, String> {

    StellarSystem findByName(String name);

//    List<StellarSystem> findBy

}
