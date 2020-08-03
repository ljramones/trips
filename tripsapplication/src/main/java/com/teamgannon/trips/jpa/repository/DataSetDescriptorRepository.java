package com.teamgannon.trips.jpa.repository;

import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * used to access datasets
 * <p>
 * Created by larrymitchell on 2017-03-28.
 */
public interface DataSetDescriptorRepository extends PagingAndSortingRepository<DataSetDescriptor, String> {

    DataSetDescriptor findByDataSetName(String name);

}
