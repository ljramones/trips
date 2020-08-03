package com.teamgannon.trips.jpa.repository;

import com.teamgannon.trips.jpa.model.SimbadEntry;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * The repository for saving simbad entries
 * <p>
 * Created by larrymitchell on 2017-02-24.
 */
public interface SimbadEntryRepository extends PagingAndSortingRepository<SimbadEntry, String> {
}
