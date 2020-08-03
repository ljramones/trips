package com.teamgannon.trips.search;

import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Used to keep track of what we have searched for
 * <p>
 * Created by larrymitchell on 2017-04-19.
 */
@Slf4j
public class SearchContext {

    private final Map<String, DataSetDescriptor> dataSetDescriptorMap = new HashMap<>();

    public String getCurrentDataSet() {
        return currentDataSet;
    }

    public void setCurrentDataSet(String currentDataSet) {
        this.currentDataSet = currentDataSet;
    }

    private String currentDataSet;

    private AstroSearchQuery astroSearchQuery = new AstroSearchQuery();

    public AstroSearchQuery getAstroSearchQuery() {
        return astroSearchQuery;
    }

    public void setAstroSearchQuery(AstroSearchQuery astroSearchQuery) {
        this.astroSearchQuery = astroSearchQuery;
    }

    public String getCurrentDataset() {
        return currentDataSet;
    }

    public void addDataSet(DataSetDescriptor dataSetDescriptor) {
        // this ensures that we set a default if it is null
        if (currentDataSet == null) {
            currentDataSet = dataSetDescriptor.getDataSetName();
        }
        dataSetDescriptorMap.put(dataSetDescriptor.getDataSetName(), dataSetDescriptor);
    }


    public void removeDataSet(DataSetDescriptor dataSetDescriptor) {
        if (currentDataSet.equals(dataSetDescriptor.getDataSetName())) {
            currentDataSet = null;
        }
        dataSetDescriptorMap.remove(dataSetDescriptor.getDataSetName());
    }

    public void addDataSets(List<DataSetDescriptor> dataSetDescriptors) {
        dataSetDescriptors.forEach(descriptor -> dataSetDescriptorMap.put(descriptor.getDataSetName(), descriptor));
        if (dataSetDescriptors.size() != 0) {
            currentDataSet = dataSetDescriptors.get(0).getDataSetName();
            astroSearchQuery.setDataSetName(currentDataSet);
        }
    }

    public Map<String, DataSetDescriptor> getDatasetMap() {
        return dataSetDescriptorMap;
    }

    public boolean setCurrentDataset(String currentDataSet) {
        DataSetDescriptor descriptor = dataSetDescriptorMap.get(currentDataSet);
        if (descriptor != null) {
            this.currentDataSet = currentDataSet;
            astroSearchQuery.setDataSetName(currentDataSet);
            return true;
        } else {
            return false;
        }
    }

    public boolean isDatasetPresent(String name) {
        return dataSetDescriptorMap.containsKey(name);
    }

    public DataSetDescriptor getDataSetDescriptor(String name) {
        return dataSetDescriptorMap.get(name);
    }

    public List<String> getDataSetNames() {
        return new ArrayList<>(dataSetDescriptorMap.keySet());
    }

}
