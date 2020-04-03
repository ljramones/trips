package com.teamgannon.trips.elasticsearch.repository;

import com.teamgannon.trips.elasticsearch.model.SimbadEntry;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * The repository for saving simbad entries
 *
 * Created by larrymitchell on 2017-02-24.
 */
public interface SimbadEntryRepository extends PagingAndSortingRepository<SimbadEntry, String> {
}
