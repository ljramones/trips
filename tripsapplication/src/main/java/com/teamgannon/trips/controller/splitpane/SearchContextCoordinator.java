package com.teamgannon.trips.controller.splitpane;

import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.search.AstroSearchQuery;
import com.teamgannon.trips.search.SearchContext;
import com.teamgannon.trips.service.StarService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SearchContextCoordinator {

    private final TripsContext tripsContext;
    private final StarService starService;

    public SearchContextCoordinator(TripsContext tripsContext, StarService starService) {
        this.tripsContext = tripsContext;
        this.starService = starService;
    }

    public SearchContext getSearchContext() {
        return tripsContext.getSearchContext();
    }

    public AstroSearchQuery getAstroSearchQuery() {
        return tripsContext.getSearchContext().getAstroSearchQuery();
    }

    public void setAstroSearchQuery(AstroSearchQuery searchQuery) {
        tripsContext.getSearchContext().setAstroSearchQuery(searchQuery);
    }

    public void setDescriptor(DataSetDescriptor descriptor) {
        tripsContext.getSearchContext().getAstroSearchQuery().setDescriptor(descriptor);
    }

    public DataSetDescriptor getCurrentDescriptor() {
        return tripsContext.getSearchContext().getDataSetDescriptor();
    }

    public String getCurrentDataSetName() {
        DataSetDescriptor descriptor = getCurrentDescriptor();
        return descriptor == null ? null : descriptor.getDataSetName();
    }

    public void recenter(@NotNull StarDisplayRecord record, double upperDistanceLimit) {
        AstroSearchQuery query = getAstroSearchQuery();
        query.setCenterRanging(record, upperDistanceLimit);
    }

    public List<StarObject> getAstrographicObjectsOnQuery() {
        return starService.getAstrographicObjectsOnQuery(tripsContext.getSearchContext());
    }
}
