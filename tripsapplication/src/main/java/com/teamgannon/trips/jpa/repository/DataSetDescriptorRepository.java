package com.teamgannon.trips.jpa.repository;

import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * used to access datasets
 * <p>
 * Created by larrymitchell on 2017-03-28.
 */
public interface DataSetDescriptorRepository extends PagingAndSortingRepository<DataSetDescriptor, String> {

    @NotNull DataSetDescriptor findByDataSetName(String name);

    @NotNull List<DataSetDescriptor> findAllByOrderByDataSetNameAsc();

}
