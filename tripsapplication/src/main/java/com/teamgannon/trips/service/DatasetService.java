package com.teamgannon.trips.service;

import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.jpa.repository.DataSetDescriptorRepository;
import com.teamgannon.trips.measure.TrackExecutionTime;
import com.teamgannon.trips.routing.model.Route;
import com.teamgannon.trips.transits.TransitDefinitions;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DatasetService {

    private final DataSetDescriptorRepository dataSetDescriptorRepository;


    public DatasetService(DataSetDescriptorRepository dataSetDescriptorRepository) {
        this.dataSetDescriptorRepository = dataSetDescriptorRepository;
    }


    /**
     * get the data sets
     *
     * @return the list of all descriptors in the database
     */
    @TrackExecutionTime
    public @NotNull
    List<DataSetDescriptor> getDataSets() {
        Iterable<DataSetDescriptor> dataSetDescriptors = dataSetDescriptorRepository.findAll();
        List<DataSetDescriptor> descriptors = new ArrayList<>();
        dataSetDescriptors.forEach(descriptors::add);
        return descriptors;
    }


    @TrackExecutionTime
    public DataSetDescriptor getDatasetFromName(String dataSetName) {
        return dataSetDescriptorRepository.findByDataSetName(dataSetName);
    }


    @TrackExecutionTime
    @Transactional
    public void addRouteToDataSet(@NotNull DataSetDescriptor dataSetDescriptor, @NotNull RouteDescriptor routeDescriptor) {

        // pull all routes
        List<Route> routeList = dataSetDescriptor.getRoutes();
        // convert to a Route and add to current list
        routeList.add(routeDescriptor.toRoute());
        // overwrite the list of routes
        dataSetDescriptor.setRoutes(routeList);
        dataSetDescriptorRepository.save(dataSetDescriptor);

    }


    /**
     * does a dataset with this name exist?
     *
     * @param name the dataset name that we are looking for
     * @return true if we found one
     */
    public boolean hasDataSet(String name) {
        return dataSetDescriptorRepository.findByDataSetName(name) != null;
    }

    @TrackExecutionTime
    @Transactional
    public DataSetDescriptor deleteRoute(String descriptorName, RouteDescriptor routeDescriptor) {
        DataSetDescriptor descriptor = dataSetDescriptorRepository.findByDataSetName(descriptorName);
        List<Route> routeList = descriptor.getRoutes();
        List<Route> updatedRoutes = routeList.stream().filter(route -> !routeDescriptor.getId().equals(route.getUuid())).collect(Collectors.toList());
        descriptor.setRoutes(updatedRoutes);
        dataSetDescriptorRepository.save(descriptor);
        return descriptor;
    }

    @TrackExecutionTime
    @Transactional
    public DataSetDescriptor updateRoute(String descriptorName, RouteDescriptor routeDescriptor) {
        DataSetDescriptor descriptor = dataSetDescriptorRepository.findByDataSetName(descriptorName);
        List<Route> routeList = descriptor.getRoutes();
        for (Route route : routeList) {
            if (route.getUuid().equals(routeDescriptor.getId())) {
                route.setRouteColor(routeDescriptor.getColor().toString());
                route.setRouteName(routeDescriptor.getName());
                route.setRouteNotes(routeDescriptor.getRouteNotes());
            }
        }
        descriptor.setRoutes(routeList);
        dataSetDescriptorRepository.save(descriptor);
        return descriptor;
    }


    @TrackExecutionTime
    @Transactional
    public void clearRoutesFromCurrent(DataSetDescriptor descriptor) {
        DataSetDescriptor descriptorCurrent = dataSetDescriptorRepository.findByDataSetName(descriptor.getDataSetName());
        descriptorCurrent.clearRoutes();
        dataSetDescriptorRepository.save(descriptorCurrent);
    }

    @TrackExecutionTime
    @Transactional
    public void setTransitPreferences(TransitDefinitions transitDefinitions) {
        DataSetDescriptor descriptorCurrent = dataSetDescriptorRepository.findByDataSetName(transitDefinitions.getDataSetName());
        descriptorCurrent.setTransitDefinitions(transitDefinitions);
        dataSetDescriptorRepository.save(descriptorCurrent);
    }

    @TrackExecutionTime
    public boolean doesDatasetExist(String name) {
        return dataSetDescriptorRepository.existsById(name);
    }

    @TrackExecutionTime
    public DataSetDescriptor changeDatasetName(DataSetDescriptor selectedDataset, String newName) {
        if (dataSetDescriptorRepository.existsById(newName)) {
            return null;
        }
        // get dataset based on name
        DataSetDescriptor descriptor = dataSetDescriptorRepository.findByDataSetName(selectedDataset.getDataSetName());

        // remove old dataset
        dataSetDescriptorRepository.delete(descriptor);

        // save as new
        descriptor.setDataSetName(newName);
        dataSetDescriptorRepository.save(descriptor);
        return descriptor;
    }


    /**
     * used to create a descriptor that we read in
     *
     * @param descriptor the descriptor
     */
    public void saveDescriptor(DataSetDescriptor descriptor) {
        dataSetDescriptorRepository.save(descriptor);
    }


    public List<DataSetDescriptor> getDescriptors() {
        return dataSetDescriptorRepository.findAllByOrderByDataSetNameAsc();
    }

}
