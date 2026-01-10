package com.teamgannon.trips.controller.splitpane;

import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.search.SearchContext;
import com.teamgannon.trips.search.AstroSearchQuery;
import com.teamgannon.trips.service.DatasetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class DataSetPanelCoordinator {

    private final TripsContext tripsContext;
    private final DatasetService datasetService;

    public DataSetPanelCoordinator(TripsContext tripsContext,
                                   DatasetService datasetService) {
        this.tripsContext = tripsContext;
        this.datasetService = datasetService;
    }

    public List<DataSetDescriptor> loadDataSets() {
        List<DataSetDescriptor> dataSetDescriptorList = datasetService.getDataSets();
        if (!dataSetDescriptorList.isEmpty()) {
            SearchContext searchContext = tripsContext.getSearchContext();
            searchContext.addDataSets(dataSetDescriptorList);
        }
        return dataSetDescriptorList;
    }

    public void refreshDataSets() {
        List<DataSetDescriptor> dataSetDescriptorList = datasetService.getDataSets();
        SearchContext searchContext = tripsContext.getSearchContext();
        searchContext.getDatasetMap().clear();
        searchContext.setAstroSearchQuery(new AstroSearchQuery());
        if (!dataSetDescriptorList.isEmpty()) {
            searchContext.addDataSets(dataSetDescriptorList);
        }
        log.debug("Datasets refreshed");
    }

    public void addDataSet(DataSetDescriptor descriptor) {
        tripsContext.addDataSet(descriptor);
    }

    public void removeDataSet(DataSetDescriptor descriptor) {
        tripsContext.removeDataSet(descriptor);
    }
}
