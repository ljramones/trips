package com.teamgannon.trips.elasticsearch.repository;

import com.teamgannon.trips.elasticsearch.model.ChViewRecord;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Used to store the chv record
 * <p>
 * Created by larrymitchell on 2017-02-24.
 */
public interface CHViewRecordRepository extends PagingAndSortingRepository<ChViewRecord, String> {
}
