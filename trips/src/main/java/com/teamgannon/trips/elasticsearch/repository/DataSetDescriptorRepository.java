package com.teamgannon.trips.elasticsearch.repository;

import com.teamgannon.trips.elasticsearch.model.DataSetDescriptor;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * used to access datasets
 * <p>
 * Created by larrymitchell on 2017-03-28.
 */
public interface DataSetDescriptorRepository extends PagingAndSortingRepository<DataSetDescriptor, String> {

    DataSetDescriptor findByDataSetName(String name);

}
