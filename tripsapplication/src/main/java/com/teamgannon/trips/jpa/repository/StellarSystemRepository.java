package com.teamgannon.trips.jpa.repository;

import com.teamgannon.trips.jpa.model.StellarSystem;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by larrymitchell on 2017-01-25.
 */
public interface StellarSystemRepository extends PagingAndSortingRepository<StellarSystem, String> {

    StellarSystem findByName(String name);

//    List<StellarSystem> findBy

}
