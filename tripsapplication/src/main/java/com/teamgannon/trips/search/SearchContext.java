package com.teamgannon.trips.search;

import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    private @Nullable String currentDataSet;
    private AstroSearchQuery astroSearchQuery = new AstroSearchQuery();

    public void setCurrentDataSet(String currentDataSet) {
        this.currentDataSet = currentDataSet;
    }

    public AstroSearchQuery getAstroSearchQuery() {
        return astroSearchQuery;
    }

    public void setAstroSearchQuery(AstroSearchQuery astroSearchQuery) {
        this.astroSearchQuery = astroSearchQuery;
    }


    public void addDataSet(@NotNull DataSetDescriptor dataSetDescriptor) {
        // this ensures that we set a default if it is null
        if (currentDataSet == null) {
            currentDataSet = dataSetDescriptor.getDataSetName();
            astroSearchQuery.setDescriptor(dataSetDescriptor);
        }
        dataSetDescriptorMap.put(dataSetDescriptor.getDataSetName(), dataSetDescriptor);
    }


    public void removeDataSet(@NotNull DataSetDescriptor dataSetDescriptor) {
        if (currentDataSet.equals(dataSetDescriptor.getDataSetName())) {
            currentDataSet = null;
            astroSearchQuery.setDescriptor(null);
        }
        dataSetDescriptorMap.remove(dataSetDescriptor.getDataSetName());
    }

    public void addDataSets(@NotNull List<DataSetDescriptor> dataSetDescriptors) {
        dataSetDescriptors.forEach(descriptor -> dataSetDescriptorMap.put(descriptor.getDataSetName(), descriptor));
        if (dataSetDescriptors.size() != 0) {
            currentDataSet = dataSetDescriptors.get(0).getDataSetName();
            astroSearchQuery.setDescriptor(dataSetDescriptors.get(0));
        }
    }

    public @NotNull Map<String, DataSetDescriptor> getDatasetMap() {
        return dataSetDescriptorMap;
    }

    public boolean setCurrentDataset(String currentDataSet) {
        DataSetDescriptor descriptor = dataSetDescriptorMap.get(currentDataSet);
        if (descriptor != null) {
            this.currentDataSet = currentDataSet;
            astroSearchQuery.setDescriptor(descriptor);
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

    public @NotNull List<String> getDataSetNames() {
        return new ArrayList<>(dataSetDescriptorMap.keySet());
    }

    public @NotNull List<DataSetDescriptor> getDatasetDescriptors() {
        return new ArrayList<>(dataSetDescriptorMap.values());
    }

}
